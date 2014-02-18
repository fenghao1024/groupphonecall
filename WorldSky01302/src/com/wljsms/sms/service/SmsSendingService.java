/**
 * Name : SmsSendingService.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.sms.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.SmsManager;
import com.eteng.world.SmsApplication;
import com.wljsms.database.MySqliteHelper;
import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.info.ContactInfo;
import com.wljsms.util.Threads;
import com.wljsms.util.Utils;
import com.wljsms.webservice.PostSmsData;

/**
 * 
 * com.eteng.sms.service.SmsSendingService
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-5 上午10:57:04 Description : 发送短信服务 Modified :
 */
public class SmsSendingService extends Service {

	private WakeLock wakeLock;
	/**
	 * 短信内容列表，即要发送的内容作为几条短信发送
	 */
	private ArrayList<String> mSmsContents;
	/**
	 * 短信内容
	 */
	private String mSmsContent;
	/**
	 * 短信数量
	 */
	private int mSmsCount;
	/**
	 * 收到发送短信成功的广播数量，理论上应等于mSmsCount
	 */
	private int mReceiverCount = 0;
	/**
	 * 短信管理器
	 */
	private SmsManager manager_sms;
	/**
	 * 短信发送失败
	 */
	private boolean mSendSmsFail = false;
	/**
	 * 待发短信联系人列表
	 */
	private List<ContactInfo> mContacts;
	/**
	 * 收件人数量
	 */
	private int mRececers;
	/**
	 * 发短信失败列表，失败的号码不提交给服务器拨号。
	 */
	private List<ContactInfo> mSmsFailedPhoneList = new ArrayList<ContactInfo>();
	/**
	 * 格林时间
	 */
	private long time;
	/**
	 * 字符串形式的时间
	 */
	private String mDateStr;
	/**
	 * 数据库对象
	 */
	private MySqliteHelper database;
	/**
	 * imsi号
	 */
	private String imsi;
	/**
	 * android系统版本号
	 */
	private String release;
	/**
	 * 机型
	 */
	private String model;
	/**
	 * 客户端版本号
	 */
	private String wlj_version;
	/**
	 * 应用对象
	 */
	private SmsApplication mApplication;

	private int index;
	
	private ArrayList<PendingIntent> sentIntents;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		// 电源管理
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				SmsSendingService.class.getName());
		wakeLock.acquire();
		super.onCreate();
		// 获取消息管理器
		manager_sms = SmsManager.getDefault();
		mApplication = (SmsApplication) getApplication();
		mApplication.setmSendingSmsComplete(false);
		database = mApplication.getDatabase();
		imsi = mApplication.getImsi();
		release = mApplication.getRelease();
		model = mApplication.getModel();
		wlj_version = mApplication.getWlj_version();
		registerRecv();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		// 获取intent携带的数据
		mSmsContent = intent.getStringExtra(ConstantsInfo.SMS_CONTENT);
		mSmsContents = manager_sms.divideMessage(mSmsContent);
		mContacts = (List<ContactInfo>) intent.getExtras().getSerializable(
				ConstantsInfo.SMS_SEND_CONTACT_LIST);
		if (mSmsContents == null || mContacts == null || mContacts.size() == 0)
			return;
		mRececers = mContacts.size();
		mSmsCount = mSmsContents.size();
		sentIntents = new ArrayList<PendingIntent>();
		DebugFlags.EtengLog("SmsSendingService 启动，在此调用发短信方法");
		time = intent.getLongExtra(ConstantsInfo.TIME, -1);
		mDateStr = intent.getStringExtra(ConstantsInfo.DATE_STR);
		// 发送发短信广播，传过去电话号码，用于监听回复的短信
		Intent i = new Intent(ConstantsInfo.SMS_SENDED_BROADCAST);
		i.putExtra(ConstantsInfo.SMS_CONTENT, mSmsContent);
		i.putExtra(ConstantsInfo.DATE_STR, mDateStr);// 传递字符串时间值
		i.putExtra(ConstantsInfo.TIME, time);
		i.putExtra(ConstantsInfo.SMS_TASK_ID, time + imsi);// 传递格林时间+imsi号
		Bundle bundle1 = new Bundle();
		bundle1.putSerializable(ConstantsInfo.SMS_SEND_CONTACT_LIST,
				(Serializable) mContacts);
		i.putExtras(bundle1);
		sendBroadcast(i);
		mApplication.setDialog(false);
		index = -1;
		// 发送短信，一条一条的发
		sendMessage();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	// 发短信监听广播，监听每一条短信的发送状态，状态为成功或失败。
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			mReceiverCount++;
			DebugFlags
			.EtengLog("收件人数量为 : "
					 + mRececers + ")的所有短信发送完成广播");
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				DebugFlags.EtengLog("SmsSendingService.java 收到发送短信成功广播");
				// 发送短信成功
				if (mReceiverCount == mSmsCount && !mSendSmsFail) {
					// 给一个号码的短信全部发送成功
					mReceiverCount = 0;
					mSendSmsFail = false;

					index++;
					if(index >= mRececers){
						//由于例外原因，导致广播异常，可以结束接受广播
						SmsSendingService.this.stopSelf();
						return;
					}
					String phone = mContacts.get(index).getPhone();
					String name = mContacts.get(index).getName();
					DebugFlags
							.EtengLog("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$收到: "
									+ name + "(" + phone + ")的所有短信发送完成广播");
					
					if (index == mRececers - 1) {
						// 所有短信均发送完毕
						DebugFlags.EtengLog("所有短信发送完毕，调用发短信完成函数");
						smsSendedComplete();
					}
					break;
				} else if (mReceiverCount == mSmsCount) {
					// 给某一个号码发送的短信中有失败的，有一个失败，可认为发给该号码的短信失败。
					mReceiverCount = 0;
					mSendSmsFail = false;
					index++;
					if(index >= mRececers){
						//由于例外原因，导致广播异常，可以结束接受广播
						SmsSendingService.this.stopSelf();
						return;
					}
					String phone = mContacts.get(index).getPhone();
					String name = mContacts.get(index).getName();
					DebugFlags
							.EtengLog("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$收到: "
									+ name + "(" + phone + ")的所有短信发送完成广播,发送失败");
					// 把号码加入到失败列表
					mSmsFailedPhoneList.add(new ContactInfo(name, phone));
					DebugFlags.EtengLog("收到一个人的所有短信发送完成广播");
					if (index == mRececers - 1) {
						// 所有短信均发送完毕
						DebugFlags.EtengLog("所有短信发送完毕，调用发短信完成函数");
						smsSendedComplete();
					}
					break;
				}
				break;
			default:
				// 发送短信失败
				DebugFlags.EtengLog("SmsSendingService.java 收到发送短信失败广播");
				mSendSmsFail = true;
				if (mReceiverCount == mSmsCount) {
					mSendSmsFail = false;
					mReceiverCount = 0;
					// 把号码加入到失败列表
					index++;
					if(index >= mRececers){
						//由于例外原因，导致广播异常，可以结束接受广播
						SmsSendingService.this.stopSelf();
						return;
					}
					String phone = mContacts.get(index).getPhone();
					String name = mContacts.get(index).getName();
					DebugFlags
							.EtengLog("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$收到: "
									+ name + "(" + phone + ")的所有短信发送完成广播,发送失败");
					// 把号码加入到失败列表
					mSmsFailedPhoneList.add(new ContactInfo(name, phone));
					DebugFlags.EtengLog("收到一个人的所有短信发送完成广播");
					if (index == mRececers - 1) {
						// 所有短信均发送完毕
						DebugFlags.EtengLog("所有短信发送完毕，调用发短信完成函数");
						smsSendedComplete();
					}
				}
				break;

			}

		}

	};

	private void smsSendedComplete() {
		// 发送短信发送完成广播,把发短信失败号码传递过去
		Intent intent = new Intent(ConstantsInfo.SMS_SENDED_COMPLETE_BROADCAST);
		Bundle bundle = new Bundle();
		bundle.putSerializable(ConstantsInfo.SMS_SEND_FAILED_CONTACT_LIST,
				(Serializable) mSmsFailedPhoneList);
		intent.putExtras(bundle);
		intent.putExtra(ConstantsInfo.SMS_TASK_ID, time + imsi);//
		// 传递格林时间+imsi号
		sendBroadcast(intent);

		// 把发送短信失败的号码列表保存到失败日志中
		if (mSmsFailedPhoneList.size() > 0) {
			// 有发送失败的号码,取得发送失败的号码列表，号码之间用逗号分割
			StringBuffer sb = new StringBuffer();
			for (ContactInfo ci : mSmsFailedPhoneList) {
				sb.append(ci.getPhone());
				sb.append(",");
			}
			// 转换为字符串
			String phones = sb.toString();
			// 去除最后一个逗号
			phones = phones.substring(0, phones.length() - 1);

			DebugFlags.EtengLog("发送短信失败的号码列表为：" + phones + ",保存发送短信失败错误日志");
			Utils.insertSmsSendFailedErrorLog(database, imsi, "50001", phones,
					mDateStr);
		}

		// 把短信插入到系统短信会话库中
		Set<String> addr = new HashSet<String>();
		StringBuffer sb = new StringBuffer();

		for (ContactInfo info : mContacts) {
			addr.add(info.getPhone());
			sb.append(info.getPhone());
			sb.append(",");
		}
		long thread_id = Threads.getOrCreateThreadId(this, addr);

		ContentValues cv = new ContentValues();
		for (ContactInfo info : mContacts) {
			cv.put("thread_id", thread_id);
			cv.put("date", time);
			cv.put("body", mSmsContent);
			cv.put("type", 2);
			cv.put("address", info.getPhone());
			this.getContentResolver().insert(
					Uri.parse(ConstantsInfo.SMS_URI_SEND), cv);
		}
		
		String phones = sb.toString();
		phones = phones.substring(0, phones.length() - 1);
		
		// 提交短信统计信息
		postSmsTaskData(time + imsi,phones);
		// 短信发送完成，停止服务
		this.stopSelf();

	}

	/**
	 * 提交短信统计信息到服务器,开启线程处理
	 * 
	 * @param 格式时间加imsi号
	 * 
	 */
	private void postSmsTaskData(final String id,final String phones) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 拼接号码列表
				try {
					// 拼接版本号
					String version = wlj_version + "," + release;
					// 内容长度
					String len = Integer.toString(mSmsContent.length());
					// 是否有网络连接标志，True : 有网络，False ： 无网络
					boolean networkFlag = true;
					String result = null;
					if (!Utils.isNetworkConnected(SmsSendingService.this)) {
						// 没有可用的网络
						networkFlag = false;
					}
					if (networkFlag) {
						// 有网络，提交信息到服务器
						DebugFlags.EtengLog("开始提交通知任务信息到服务器");
						PostSmsData postSmsData = new PostSmsData();
						result = postSmsData.init(imsi, id, phones, model,
								version, mSmsContent.length(), mDateStr, "急短信",
								null);

					}

					if (result == null || result.equals("1") || !networkFlag) {
						// 无网络或提交短信记录到服务器失败，保存到通知任务记录表中，有网络时再次提交
						DebugFlags
								.EtengLog("无网络或服务器接口访问失败，保存通知任务信息到本地数据库中，IMSI号："
										+ imsi
										+ ",id："
										+ id
										+ ",收件人列表 ： "
										+ phones
										+ ",机型："
										+ model
										+ ",版本信息："
										+ version + ",通知内容长度 ： " + len);
						if (database != null) {
							Cursor cur = database.getTasks();
							if (cur != null) {
								if (cur.getCount() == ConstantsInfo.SMS_TASK_COUNT) {
									// 记录条数达到记录上限，删除一条记录
									cur.moveToFirst();
									database.deleteTask(cur.getString(cur
											.getColumnIndex(MySqliteHelper.TASK_ID)));

									DebugFlags.EtengLog("从本地通知任务记录数据库中删除一条记录");
								}
							}
							database.insertTask(imsi, id, phones, model,
									version, len, mDateStr, "急短信");

							DebugFlags.EtengLog("添加一条记录到本地通知任务库中");
						}
					} else {
						DebugFlags.EtengLog("提交通知任务信息到服务器成功");
					}
				} catch (Exception e) {

				}
			}

		}).start();

	}

	/**
	 * 发送短信方法
	 * 
	 */
	private void sendMessage() {
		Intent it = new Intent(ConstantsInfo.SMS_SEND_SUCCESS_SERVICE_BROADCAST);
		for (int i = 0; i < mSmsContents.size(); i++) {
			PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, it,
					PendingIntent.FLAG_UPDATE_CURRENT);
			sentIntents.add(sentPI);
		}

		for (ContactInfo info : mContacts) {
			String dPhone = info.getPhone();
			DebugFlags.EtengLog("SmsSendingService 调用发短信函数sendMessage(),正在给号码："
					+ dPhone + "发送短信");
			try {
				manager_sms.sendMultipartTextMessage(dPhone, null,
						mSmsContents, sentIntents, null);
			} catch (Exception e) {
				// 出现异常，认为此次发送失败，发送下一条。
				mReceiverCount = mSmsCount - 1;
				Intent itt = new Intent(
						ConstantsInfo.SMS_SEND_SUCCESS_SERVICE_BROADCAST);
				sendBroadcast(itt);
			}
		}

	}

	/**
	 * 注册广播接收器
	 * 
	 */
	private void registerRecv() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConstantsInfo.SMS_SEND_SUCCESS_SERVICE_BROADCAST);
		registerReceiver(receiver, filter); 
	}
  
	@Override
	public void onDestroy() {
		super.onDestroy();
		// TODO Auto-generated method stub
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
		this.unregisterReceiver(receiver);
		
		mContacts.clear();
		mSmsFailedPhoneList.clear();
		for(PendingIntent pendingIntent : sentIntents){
			pendingIntent.cancel();
			pendingIntent = null;
		}
		sentIntents.clear();
		sentIntents = null;
		mApplication.setmSmsSendedTime(System.currentTimeMillis());
		mApplication.setmSendingSmsComplete(true);
		mApplication.setDialog(true);
		DebugFlags.EtengLog("SmsSendingService 停止");
		
	}

}
