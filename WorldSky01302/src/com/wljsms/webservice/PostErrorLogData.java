/**
 * Name : PostErrorLogData.java
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
 * com.eteng.world.webservice.PostErrorLogData
 * @author wanglaoji <br/>
 * Create at 2013-2-28 下午7:27:38
 * Description : 提交错误日志信息到服务器
 * Modified :  
 */
public class PostErrorLogData {
	private static final String METHOD = "InterfaceErrLog";

	public String init(String content, ICallBack callBack) {
		SoapHelper soapHelper = new SoapHelper(METHOD);

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("content", content);

		Object result = soapHelper.init(callBack, m);

		if (callBack != null) {
			if (result != null) {
				callBack.netSuccess();
			} else {
				callBack.netFailed();
			}
		}

		if (result != null) {
			return result.toString();
		}

		return null;
	}
}
