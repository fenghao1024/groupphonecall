/**
 * Name : SetTask.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.webservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ksoap2.serialization.SoapObject;
import com.wljsms.config.ICallBack;
import com.wljsms.config.SoapHelper1;
import com.wljsms.info.ReturnInfo;

/**
 * 
 * com.eteng.services.SetTask
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-30 下午2:12:22 Description :
 *         设置呼叫参数服务接口
 */
public class SetTask {

	private static final String METHOD = "SetTask";

	/**
	 * 创建网络连接，异步回调获取数据
	 */
	public List<ReturnInfo> init(String Imsi, String Phones, int Onces,
			int Interval, String TaskDate, ICallBack callBack) {
		List<ReturnInfo> ReturnList = null;

		SoapHelper1 soapHelper = new SoapHelper1(METHOD);

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("Imsi", Imsi);
		m.put("Phones", Phones);
		m.put("Onces", Onces);
		m.put("Interval", Interval);
		m.put("TaskDate", TaskDate);

		SoapObject result = soapHelper.init(callBack, m);

		if (result != null) {
			ReturnList = parse(result);
		}

		if (callBack != null) {
			if (ReturnList != null) {
				callBack.netSuccess();
			} else {
				callBack.netFailed();
			}
		}

		return ReturnList;
	}

	
	/**
	 * 把XML数据转换为数据实体集合
	 */
	private List<ReturnInfo> parse(SoapObject result) {

		if (result != null) {
			List<ReturnInfo> ReturnInfoList = new ArrayList<ReturnInfo>();
			for (int i = 0; i < result.getPropertyCount(); i++) {
				SoapObject itemObject = (SoapObject) result.getProperty(i);
				ReturnInfo returnInfo = new ReturnInfo();
				returnInfo.setID(itemObject.getProperty("ID").toString());
				returnInfo.setReturnState(Integer.valueOf(itemObject.getProperty(
						"ReturnState").toString()));
				returnInfo.setReturnOnce(Integer.valueOf(itemObject.getProperty(
						"ReturnOnce").toString()));
				returnInfo.setToPhone(itemObject.getProperty("ToPhone")
						.toString());
				ReturnInfoList.add(returnInfo);
			}
			return ReturnInfoList;
		}
		return null;

	}

}
