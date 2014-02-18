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
 * c com.eteng.entity.StateInfo
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-4-1 下午2:12:22 Description :
 *         通知人状态信息实体
 */
@SuppressWarnings("serial")
public class StateInfo implements Serializable{
	/**
	 * 姓名
	 */
	private String name;  
	/**
	 * 回复状态 
	 */
	private String currentstate;  
	
	public String getCurrentstate() {
		return currentstate;
	}

	public void setCurrentstate(String currentstate) {
		this.currentstate = currentstate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
