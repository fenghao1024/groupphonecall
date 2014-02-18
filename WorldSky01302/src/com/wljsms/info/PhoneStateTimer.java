/**
 * Name : PhoneStateTimer.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.Intent;
import com.wljsms.database.MySqliteHelper;
import com.wljsms.debug.DebugFlags;
import com.wljsms.util.Utils;
import com.wljsms.webservice.GetCallReturn;

/**
 * 
 * com.eteng.info.PhoneStateTimer
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-1 上午10:52:39 Description : 电话状态定时器，从服务器获取通话状态
 */
public class PhoneStateTimer {

	private Context context;

	public PhoneStateTimer(Context context) {
		this.context = context;
		this.mDeletable = false;
	}

	/**
	 * 定时器执行时间间隔，默认半分钟
	 */
	private long delay_time = 30 * 1000;
	/**
	 * 定时器首次执行的时间间隔,默认为2秒钟
	 */
	private long first_delay_time = 2 * 1000;
	/**
	 * 定时器对象
	 */
	private Timer mPhoneTimer;
	/**
	 * 电话号码列表
	 */
	private ArrayList<String> phoneList;
	/**
	 * 定时器是否可以删除，true:可删除,false:不可删除
	 */
	private boolean mDeletable;
	/**
	 * 字符串形式的时间值
	 */
	private String mDateStr;
	/**
	 * 通知的ID
	 */
	private String mTaskId;

	/**
	 * 初始化定时器，即一定时间间隔后循环从服务器获取通话状态
	 * 
	 * @param database
	 *            ：数据库对象
	 */
	public void initTimer(final MySqliteHelper database) {
		DebugFlags.EtengLog("初始化获取通话状态定时器，任务ID：" + mTaskId + ",首次执行时间间隔："
					+ first_delay_time / 1000 + "秒，后续执行时间间隔：" + delay_time
					/ 1000 + "秒");
		mPhoneTimer = new Timer();
		// 设置定时器要执行的任务
		TimerTask timerTask = new TimerTask() {
			Integer flag = 0;

			@Override
			public void run() {

				if (flag++ < 30) {
					DebugFlags.EtengLog("获取拨号状态在重试次数之内,调用服务器接口方法，获取通话状态");
					if(!Utils.isNetworkConnected(context)){
						//没有网络连接，不访问接口，直接返回
						DebugFlags.EtengLog("没有网络连接，不访问接口，直接返回");
						return;
					}
					try {
						DebugFlags.EtengLog("开始访问服务器，获取拨号状态");
						GetCallReturn getCallReturn = new GetCallReturn();
						// 获取通话状态列表
						List<ReturnInfo> ls = getCallReturn.init(mTaskId, null);
						if (ls != null) {
							// 成功获取到了拨号状态列表
							DebugFlags.EtengLog("成功获取到拨号状态,状态列表大小为：" + ls.size());
							if (ls.size() == 0) {
								// 没有取到状态信息
								return;
							}
							// 所有电话都接通或取消拨号
							boolean all_call = true;
							// 第5次呼叫
							boolean isFivth = false;
							// 是否有拨号成功或取消拨号
							boolean flag = true;
							// 开始数据库事物操作
							database.getWritableDatabase().beginTransaction();
							for (ReturnInfo returnInfo : ls) {
								// 更新数据库中通话状态，服务器端由于IVR原因，返回的状态为：0，1，2等，本地数据使用状态为4，5
								DebugFlags.EtengLog("拨号状态："
													+ returnInfo
															.getReturnState()
													+ "，拨号次数："
													+ returnInfo
															.getReturnOnce()
													+ ",拨号号码："
													+ returnInfo.getToPhone());
								database.updateCallState(returnInfo
										.getToPhone(), returnInfo
										.getReturnState(), mDateStr, Integer
										.toString(returnInfo.getReturnOnce()));
								if (returnInfo.getReturnState() == 0
										|| returnInfo.getReturnState() == 6
										|| returnInfo.getReturnState() == 7) {
									// 拨号成功或者取消了拨号
									flag = false;
								} else {
									all_call = false;
								}
								isFivth = returnInfo.getReturnOnce() == 5 ? true
										: false;
							}
							// 结束数据库事物操作
							database.getWritableDatabase()
									.setTransactionSuccessful();
							database.getWritableDatabase().endTransaction();
							if (all_call || isFivth) {
								// 所有电话都已接通，可以结束定时器了
								DebugFlags.EtengLog("所有电话都已接通或则已经是第5次呼叫了，结束获取拨号状态");
								mPhoneTimer.cancel();
								mDeletable = true;
								context.sendBroadcast(new Intent(
										ConstantsInfo.GET_PHONE_STATE_BROADCAST));
							}
							if (!flag) {
								// 至少有一个 电话接通，更新通知页面的进度。注：需求变动，暂时不用，保留作为以后的扩展
								// context.sendBroadcast(new Intent(
								// ConstantsInfo.PHONE_CALL_STATE_BROADCAST));

							}
						} else {
							// 访问拨号服务器失败，无法获取拨号状态类别
							DebugFlags.EtengLog("访问拨号服务器失败，无法获取拨号状态类别。");
						}
					} catch (Exception e) {
					}
				} else {
					DebugFlags.EtengLog("获取拨号状态超出重试次数，结束");
					mPhoneTimer.cancel();
					mDeletable = true;
					context.sendBroadcast(new Intent(
							ConstantsInfo.GET_PHONE_STATE_BROADCAST));
				}
			}

		};
		mPhoneTimer.schedule(timerTask, first_delay_time, 30 * 1000);
	}

	public long getDelay_time() {
		return delay_time;
	}

	public void setDelay_time(long delay_time) {
		this.delay_time = delay_time;
	}

	public boolean ismDeletable() {
		return mDeletable;
	}

	public void setmDeletable(boolean mDeletable) {
		this.mDeletable = mDeletable;
	}

	public String getmDateStr() {
		return mDateStr;
	}

	public void setmDateStr(String mDateStr) {
		this.mDateStr = mDateStr;
	}

	public String getmTaskId() {
		return mTaskId;
	}

	public void setmTaskId(String mTaskId) {
		this.mTaskId = mTaskId;
	}

	public long getFirst_delay_time() {
		return first_delay_time;
	}

	public void setFirst_delay_time(long first_delay_time) {
		this.first_delay_time = first_delay_time;
	}

	/**
	 * 取消定时器任务
	 */
	private void cancelTimer() {
		if (mPhoneTimer != null) {
			mPhoneTimer.cancel();
			mDeletable = true;
		}
	}

	/**
	 * 清除数据
	 */
	public void clear() {
		cancelTimer();
		if (phoneList != null)
			phoneList.clear();
	}

}
