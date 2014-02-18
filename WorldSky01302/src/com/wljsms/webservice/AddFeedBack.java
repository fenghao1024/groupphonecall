/**
 * Name : AddFeedBack.java
 * Version : 0.0.1
 * Copyright : Copyright (c) Eteng Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.webservice;

import java.util.HashMap;
import java.util.Map;

import com.wljsms.config.ICallBack;
import com.wljsms.config.SoapHelper;

/**
 * 
 * com.eteng.world.AddFeedBack
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-30 下午2:12:22 Description : 电话，短信回执信息处理类
 */
public class AddFeedBack {

	private static final String METHOD = "AddFeedback";

	/**
	 * 创建网络连接，提交数据到服务器
	 */
	public boolean init(String imsi, String content, String contact,
			String version, ICallBack callBack) {
		boolean returnStr = false;
		SoapHelper soapHelper = new SoapHelper(METHOD);

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("content", content);
		m.put("contact", contact);
		m.put("imsi", imsi);
		m.put("version", version);

		String result = soapHelper.initReturnString(callBack, m);

		if (result != null && !result.equals("false")) {
			returnStr = true;
		}
		return returnStr;
	}
}
