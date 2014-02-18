/**
 * Name : VersionInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

import java.util.Date;
/**
 * 
 * com.eteng.info.VersionInfo
 * @author wanglaoji <br/>
 * Create at 2013-2-5 上午10:54:46
 * Description : 版本信息类
 * Modified :
 */
public class VersionInfo {
	/**
	 * 版本号
	 */
	public String versionCode;
	/**
	 * 版本名称
	 */
	public String versionName;
	/**
	 * 版本下载地址
	 */
	public String versionLink;   
	/**
	 * 版本更新时间
	 */
	public Date updateTime;
	/**
	 * 当前客户端大小
	 */
	public String fileSize;   
	/**
	 * 当前客户端更新信息
	 */
	public String message;   
	/**
	 * 是否强制升级1：是，2：否
	 */
	public String forceUpdate = "2";
	
	public String getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}
	
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	
	public String getVersionLink() {
		return versionLink;
	}
	public void setVersionLink(String versionLink) {
		this.versionLink = versionLink;
	}
	
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getFileSize() {
		return fileSize;
	}
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getForceUpdate() {
		return forceUpdate;
	}
	public void setForceUpdate(String forceUpdate) {
		this.forceUpdate = forceUpdate;
	}
	
}
