/**
 * Name : SmsTaskInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

import java.util.List;

/**
 * 
 * com.eteng.info.SmsInfo
 * @author wanglaoji <br/>
 * Create at 2013-2-5 上午10:00:48
 * Description : 通知任务类
 * Modified :
 */
public class SmsTaskInfo {

	/**
	 * 默认构造方法
	 */
	public SmsTaskInfo() {

	}

	/**
	 * 有两个参数的构造方法
	 * 
	 * @param id
	 *            : 通知ID
	 * @param mContactList
	 *            : 通知收件人列表
	 */
	public SmsTaskInfo(String id, List<ContactInfo> mContactList) {
		this.id = id;
		this.mContactList = mContactList;
	}

	/**
	 * 通知id
	 */
	private String id;
	/**
	 * 通知的收件人列表
	 */
	private List<ContactInfo> mContactList;
	/**
	 * 通知内容
	 */
	private String mContent;
	/**
	 * 通知创建时间，即点击发送按钮的时间
	 */
	private long time;
	/**
	 * 字符串格式之间值
	 */
	private String mDateStr;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getmContent() {
		return mContent;
	}

	public void setmContent(String mContent) {
		this.mContent = mContent;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getmDateStr() {
		return mDateStr;
	}

	public void setmDateStr(String mDateStr) {
		this.mDateStr = mDateStr;
	}
	
	public List<ContactInfo> getmContactList() {
		return mContactList;
	}

	public void setmContactList(List<ContactInfo> mContactList) {
		this.mContactList = mContactList;
	}
	
	/**
	 * 判断通知任务对象是否可删除
	 * @return True : 可删除,False : 不可删除
	 */
	public boolean deletable(){
		if(mContactList == null || mContactList.size() == 0)
			return true;
		return false;
	}
	
	public void clean(){
		if(mContactList != null)
			mContactList.clear();
	}
}
