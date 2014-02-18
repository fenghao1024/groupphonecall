package com.wljsms.webservice;

import java.util.HashMap;
import java.util.Map;

import com.wljsms.config.ICallBack;
import com.wljsms.config.SoapHelper;

/**
 * 
 * com.eteng.services.GetPhone
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-30 下午2:12:22 Description :
 *         通过IMSI获取电话号码服务接口
 */
public class GetPhone {

	private static final String METHOD = "GetIMSIInfo";
	
	/**
	 * 创建网络连接，调用服务器接口，取得imsi号对应的电话号码
	 * 
	 */
	public String init(String Imsi,ICallBack callBack) {

		SoapHelper soapHelper = new SoapHelper(METHOD);

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("imsi", Imsi);
		String result = soapHelper.initReturnString(callBack, m);
		if (result != null && !result.trim().equals("")) {
			return result;
		}
		return "";
	}

}
