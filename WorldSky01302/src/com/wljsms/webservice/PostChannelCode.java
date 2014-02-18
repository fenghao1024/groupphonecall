/**
 * Name : PostChannelCode.java
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
 * com.eteng.world.webservice.PostChannelCode
 * @author ZhangZhengdong <br/>
 * Create at 2013-3-22 下午5:27:37
 * Description : 提交安装取到号到服务器
 * Modified : 
 */
public class PostChannelCode {
	private static final String METHOD = "UpUserInfo";

	public String init(String Imsi,String modelversion, ICallBack callBack) {

		SoapHelper1 soapHelper = new SoapHelper1(METHOD);

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("imsi", Imsi);
		m.put("modelversion", modelversion);

		String result = soapHelper.initReturnString(callBack, m);
		if (result != null && !result.trim().equals("")) {
			return result;
		}
		return "";
		
	}
}
