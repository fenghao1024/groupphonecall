/**
 * Name : GetReturnList.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.webservice;

import java.util.HashMap;
import java.util.Map;

import com.wljsms.config.ICallBack;
import com.wljsms.config.SoapHelper;

/**
 * 
 * com.eteng.services.GetReturnList
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-30 下午2:12:22 Description :
 *         获取电话回执状态服务接口
 */
public class GetReturnList {

	private static final String METHOD = "StartCall";

	/**
	 * 创建网络连接，异步回调获取数据，获取电话回执状态
	 */
	public String init(String Groupid, String Imsi, String Phones,String startTime,String endTime,String appSource,
			ICallBack callBack) {

		SoapHelper soapHelper = new SoapHelper(METHOD);

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("Imsi", Imsi);
		m.put("Phones", Phones);
		m.put("Groupid", Groupid);
		m.put("StartTime", startTime);
		m.put("EndTime", endTime);
		m.put("AppSource", appSource);
		String result = soapHelper.initReturnString(callBack, m);
		if (result != null && !result.trim().equals("")) {
			return result;
		}
		return "";
	}

}
