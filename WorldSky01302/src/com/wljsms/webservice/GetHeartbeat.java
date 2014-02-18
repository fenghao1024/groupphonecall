/**
 * Name : GetHeartbeat.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.webservice;

import java.util.HashMap;
import java.util.Map;

import com.wljsms.config.ICallBack;
import com.wljsms.config.SoapHelper1;

/**
 * 
 * com.eteng.services.GetHeartbeat
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-3-14 下午11:06:15 Description : 访问服务器心跳接口，取得服务器状态
 */
public class GetHeartbeat {
	private static final String METHOD = "Heartbeatlog";

	/**
	 * 创建网络连接，调用服务器接口，取得imsi号对应的电话号码
	 * 
	 */
	public String init(String Imsi, ICallBack callBack) {

		SoapHelper1 soapHelper = new SoapHelper1(METHOD);

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("imsi", Imsi);

		String result = soapHelper.initReturnString(callBack, m);
		if (result != null && !result.trim().equals("")) {
			return result;
		}
		return "";
		
	}
}
