/**
 * Name : ErrorLogInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

/**
 * com.eteng.info.ErrorLogInfo
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-5 下午1:11:07 Description : 错误日志信息类 Modified :
 */
public class ErrorLogInfo {

	private int id;
	private String imsi;
	private String error_code;
	private String error_type;
	private String extra_content;
	private String time;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
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
	 * @return the error_code
	 */
	public String getError_code() {
		return error_code;
	}

	/**
	 * @param error_code
	 *            the error_code to set
	 */
	public void setError_code(String error_code) {
		this.error_code = error_code;
	}

	/**
	 * @return the error_type
	 */
	public String getError_type() {
		return error_type;
	}

	/**
	 * @param error_type
	 *            the error_type to set
	 */
	public void setError_type(String error_type) {
		this.error_type = error_type;
	}

	/**
	 * @return the extra_content
	 */
	public String getExtra_content() {
		return extra_content;
	}

	/**
	 * @param extra_content
	 *            the extra_content to set
	 */
	public void setExtra_content(String extra_content) {
		this.extra_content = extra_content;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

}
