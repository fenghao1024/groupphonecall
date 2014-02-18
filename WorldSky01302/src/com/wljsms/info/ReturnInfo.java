/**
 * Name : ReturnInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

import java.io.Serializable;
import java.util.Date;
/**
 * 
 * com.eteng.entity.ReturnInfo
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-30 下午2:12:22 Description :
 *         电话回执信息实体
 */
public class ReturnInfo implements Serializable {

	private String ID;  //编号
	private int ReturnState;  //返回状态
	private int ReturnOnce;   //返回次数
	private Date ReturnDate;  //返回时间
	private String ToPhone;    //呼叫电话
	private static final long serialVersionUID = 1L;

	public String getToPhone() {
		return ToPhone;
	}

	public void setToPhone(String toPhone) {
		ToPhone = toPhone;
	}
	
	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public int getReturnState() {
		return ReturnState;
	}

	public void setReturnState(int returnState) {
		ReturnState = returnState;
	}

	public int getReturnOnce() {
		return ReturnOnce;
	}

	public void setReturnOnce(int returnOnce) {
		ReturnOnce = returnOnce;
	}

	public Date getReturnDate() {
		return ReturnDate;
	}

	public void setReturnDate(Date returnDate) {
		ReturnDate = returnDate;
	}


}
