/**
 * Name : SmsThreadInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

import android.graphics.Bitmap;

/**
 * 
 * com.eteng.info.SmsThreadInfo
 * @author wanglaoji <br/>
 * Create at 2013-2-5 上午10:05:47
 * Description : 短信会话信息类，即短信点对点页面ListView的item对象类
 * Modified :
 */
public class SmsThreadInfo {

	/**
	 * 联系人头像，没有为null
	 */
	private Bitmap bitmap;
	/**
	 * 联系人姓名
	 */
	private String name;
	/**
	 * 时间
	 */
	private long time;
	/**
	 * 短信类型：1：接收，2：发送
	 */
	private String type;
	/**
	 * 电话号码
	 */
	private String phone;
	/**
	 * 短信内容
	 */
	private String content;
	/**
	 * 字符串日期
	 */
	private String dateStr;
	/**
	 * 是否呼叫，"0"：没有呼叫，"1"：已呼叫
	 */
	private String isCall;

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDateStr() {
		return dateStr;
	}

	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}
	
	public String getIsCall() {
		return isCall;
	}

	public void setIsCall(String isCall) {
		this.isCall = isCall;
	}

}
