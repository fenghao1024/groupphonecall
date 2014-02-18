/**
 * Name : PostSmsData.java
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
 * com.eteng.world.webservice.PostSmsData
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-3 上午10:58:09 Description :
 *         提交短信统计数据，包括：ISMI号、手机号码、发送时间、发送短信内容长度、接收人清单、机型 Modified :
 */
public class PostSmsData {

	private static final String METHOD = "SmsSendSource";
	public String init(String imsi,String groupId,String receiverPhones,String model,String version,int smsLength, String sendDate,String appCome ,ICallBack callBack) {
		SoapHelper soapHelper = new SoapHelper(METHOD);

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("imsi", imsi);
		m.put("phones", receiverPhones);
		m.put("groupid", groupId);
		m.put("mobileModel", model);
		m.put("version", version);
		m.put("smslength", smsLength);
		m.put("sendDate", sendDate);
		m.put("appCome", appCome);
		
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
