/**
 * Name : ICallBack.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.config;
/**
 * 
 * com.eteng.config.SoapHelper
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-30 下午2:12:22 Description :
 *         回调接口
 */
public interface ICallBack {

	public void netFailed(); //异常接口
	public void netSuccess();  //成功接口
	
}
