/**
 * Name : SmsInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

import java.io.Serializable;

/**
 * com.eteng.info.SmsInfo
 * @author wanglaoji <br/>
 * Create at 2013-2-18 下午5:19:54
 * Description : 已发送的短信信息类
 * Modified : 
 */
public class SendedSmsInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 短信所属的会话ID
	 */
	private String id;
	/**
	 * 收件人姓名
	 */
	private String name;
	/**
	 * 收件人号码
	 */
	private String phone;
	/**
	 * 短信内容
	 */
	private String body;
	/**
	 * 短信发送日期
	 */
	private long date;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}
	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}
	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}
	/**
	 * @return the date
	 */
	public long getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(long date) {
		this.date = date;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
}
