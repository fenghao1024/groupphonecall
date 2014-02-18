/**
 * Name : SmsInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

import java.io.Serializable;

/**
 * 
 * com.eteng.info.SmsInfo
 * @author wanglaoji <br/>
 * Create at 2013-2-5 上午10:00:48
 * Description : 短信信息类
 * Modified :
 */
public class SmsInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	/**
	 * 短信内容
	 */
	private String smsbody = "";
	/**
	 * 发送短信的电话号码
	 */
	private String phoneNumber;
	/**
	 * 发送或收到短信的日期和时间
	 */
	private String date;

	public String getSmsbody() {
		return smsbody;
	}

	public void setSmsbody(String smsbody) {
		this.smsbody = smsbody;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}
