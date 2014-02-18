/**
 * Name : ContactInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

import java.io.Serializable;

/**
 * 
 * c com.eteng.entity.ListSendInfo
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-4-2 下午2:12:22 
 *         Description : 通知监听状态实体
 */
@SuppressWarnings("serial")
public class ListSendInfo implements Serializable {
	
	/**
	 * 是否允许弹出
	 */
	private String isShowLook;  
	/**
	 * 发送短信时间
	 */
	private String CreateDate;   
	/**
	 * 发送总数
	 */
	private String count;        
	/**
	 * 回复数
	 */
	private String replyCount;   
	/**
	 * 匹配情况
	 */
	private String contextMax;    
	/**
	 * 发送内容
	 */
	public String getIsShowLook() {
		return isShowLook;
	}

	public void setIsShowLook(String isShowLook) {
		this.isShowLook = isShowLook;
	}

	public String getCreateDate() {
		return CreateDate;
	}

	public void setCreateDate(String createDate) {
		CreateDate = createDate;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(String replyCount) {
		this.replyCount = replyCount;
	}

	public String getContextMax() {
		return contextMax;
	}

	public void setContextMax(String contextMax) {
		this.contextMax = contextMax;
	}

}
