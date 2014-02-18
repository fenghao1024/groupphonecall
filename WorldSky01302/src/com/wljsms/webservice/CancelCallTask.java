/**
 * Name : CancelCallTask.java
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
 * com.eteng.world.webservice.CancelCallTask
 * @author wanglaoji <br/>
 * Create at 2013-3-1 上午10:40:36
 * Description : 调用服务器接口，取消拨号任务
 * Modified : 
 */
public class CancelCallTask {
	private static final String METHOD = "CancleTask";

	/**
	 * 调用服务器接口，进行取消拨号任务。phone不为空的话取消对该号码的拨号，taskId不为空的话，取消对一组通知的拨号
	 * @param phone：号码
	 * @param taskId：任务ID
	 * @param callBack：回调接口
	 * @return 成功返回非空，失败返回空
	 */
	public String init(String phone,String taskId, ICallBack callBack) {
		SoapHelper soapHelper = new SoapHelper(METHOD);

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("phone", phone);
		m.put("groupid", taskId);
		
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
