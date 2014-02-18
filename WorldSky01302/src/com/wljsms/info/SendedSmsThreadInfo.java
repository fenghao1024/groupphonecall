/**
 * Name : SendedSmsThreadInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) Eteng Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;

import com.eteng.world.SmsApplication;
import com.wljsms.database.MySqliteHelper;
import com.wljsms.debug.DebugFlags;
import com.wljsms.util.Utils;

/**
 * com.eteng.info.SendedSmsThreadInfo
 * 
 * @author Eteng <br/>
 *         Create at 2013-3-2 下午11:38:12 Description :
 *         已发送的短信会话信息类，每次发送短信属于同一个会话，包括单发和群发 Modified :
 */
public class SendedSmsThreadInfo {

	public SmsApplication getApplication() {
		return application;
	}

	public void setApplication(SmsApplication application) {
		this.application = application;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * 会话信息id，这个有程序维护，和系统会话短信数据库中的id不同,值为imsi+会话中第一条已发短信的时间
	 */
	private String id;
	/**
	 * 属于同一个会话的已发送短信信息类
	 */
	private List<SendedSmsInfo> mSmsList = new ArrayList<SendedSmsInfo>();
	/**
	 * 是否可删除，定时器执行完后，该对象可清除了
	 */
	private boolean deletable = false;
	/**
	 * 消息处理
	 */
	private Handler handler;
	/**
	 * 对短信内容的比对是否完成，True:完成，False:未完成
	 */
	private boolean mMatchingComplete = false;
	/**
	 * 会话是否匹配成功
	 */
	private boolean mMatched = false;
	/**
	 * 数据库对象
	 */
	private MySqliteHelper database;

	private Context context;
	
	private SmsApplication application;

	private String imsi;
	/**
	 * 匹配成功的情况下，弹出对话框，用户是否点击了确定按钮，默认没有点击
	 */
	private boolean mClickedPositiveBtn = false;

	/**
	 * 添加一个已发短信信息到列表中
	 * 
	 * @param info
	 *            ： 短信信息类
	 */
	public void add(SendedSmsInfo info) {
		for(SendedSmsInfo sendedInfo : mSmsList){
			if(sendedInfo.getPhone().equals(info.getPhone()))
				return;
		}
		mSmsList.add(info);
		deletable = true;
	}

	/**
	 * 初始化定时器，此方法会被多次调用，收到同一会话的已发短信即调用一次
	 */
	public void init() {

		if (handler == null) {
			// 初始化handler
			DebugFlags.EtengLog("方法第一次执行，初始化handler");
			handler = new Handler() {
			};
			handler.postDelayed(runnable, 20 * 1000);
		} else {
			DebugFlags.EtengLog("再次调用了该方法");
			handler.removeCallbacks(runnable);
			handler.postDelayed(runnable, 20 * 1000);

		}
	}

	/**
	 * 取消提交数据任务
	 */
	public void cancel() {
		handler.removeCallbacks(runnable);
	}

	/*
	 * 根据电话号码取得联系人姓名
	 */

	@SuppressWarnings("unused")
	public String getPeople(String mNumber) {
		mNumber = mNumber.trim();
		String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER };

		// 将自己添加到 msPeers 中
		Cursor cursor = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection,
				ContactsContract.CommonDataKinds.Phone.NUMBER + " = '"
						+ mNumber + "'", null, null);

		if (cursor == null) {
			return mNumber;
		}

		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);

			// 取得联系人名字
			int nameFieldColumnIndex = cursor
					.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
			String name = cursor.getString(nameFieldColumnIndex);

			return name;
		}
		return mNumber;
	}

	/**
	 * 执行提交数据的线程
	 */
	private Runnable runnable = new Runnable() {

		public void run() {
			// TODO Auto-generated method stub
			
			String s = "";
			for (SendedSmsInfo ssi : mSmsList) {
				s += ssi.getPhone() + ",";
				String strName=getPeople(ssi.getPhone());
                ssi.setName(strName);
			}
			if(mClickedPositiveBtn){
				DebugFlags.EtengLog(Utils.formatDate(System.currentTimeMillis())
						+ "  提交数据线程开始执行，提交数据到服务器拨号，提交的号码为：" + s);
				Intent intent = new Intent(ConstantsInfo.SMS_SENDNONE_ANDROID);
				Bundle bundle = new Bundle();
				bundle.putSerializable("Data", (Serializable) mSmsList);
				intent.putExtras(bundle);
				context.sendBroadcast(intent);
			}

			// TODO Auto-generated method stub
			DebugFlags.EtengLog(Utils.formatDate(System.currentTimeMillis())
					+ "  更新本地发件库中匹配的短信数，匹配的短信数为：" + mSmsList.size());

			deletable = true;
			context.sendBroadcast(new Intent(ConstantsInfo.DELETE_SMS_THREAD_INFO));
		}

	};

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the mSmsList
	 */
	public List<SendedSmsInfo> getmSmsList() {
		return mSmsList;
	}

	/**
	 * @param mSmsList
	 *            the mSmsList to set
	 */
	public void setmSmsList(List<SendedSmsInfo> mSmsList) {
		this.mSmsList = mSmsList;
	}

	/**
	 * @return the mDeletable
	 */
	public boolean isDeletable() {
		return deletable;
	}

	/**
	 * @param mDeletable
	 *            the mDeletable to set
	 */
	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	/**
	 * @return the mMatchingComplete
	 */
	public boolean ismMatchingComplete() {
		return mMatchingComplete;
	}

	/**
	 * @param mMatchingComplete
	 *            the mMatchingComplete to set
	 */
	public void setmMatchingComplete(boolean mMatchingComplete) {
		this.mMatchingComplete = mMatchingComplete;
	}

	/**
	 * @return the mMatched
	 */
	public boolean ismMatched() {
		return mMatched;
	}

	/**
	 * @param mMatched
	 *            the mMatched to set
	 */
	public void setmMatched(boolean mMatched) {
		this.mMatched = mMatched;
	}

	/**
	 * @return the database
	 */
	public MySqliteHelper getDatabase() {
		return database;
	}

	/**
	 * @param database
	 *            the database to set
	 */
	public void setDatabase(MySqliteHelper database) {
		this.database = database;
	}

	/**
	 * @return the imsi
	 */
	public String getImsi() {
		return imsi;
	}

	/**
	 * @param imsi
	 *            the imsi to set
	 */
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	/**
	 * @return the mClickedPositiveBtn
	 */
	public boolean ismClickedPositiveBtn() {
		return mClickedPositiveBtn;
	}

	/**
	 * @param mClickedPositiveBtn the mClickedPositiveBtn to set
	 */
	public void setmClickedPositiveBtn(boolean mClickedPositiveBtn) {
		this.mClickedPositiveBtn = mClickedPositiveBtn;
	}
	
}
