/**
 * Name : SoapHelper.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.config;

import java.io.IOException;
import java.util.Map;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

/**
 * com.eteng.config.SoapHelper
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-30 下午2:12:22 Description : KSOAP服务通讯类
 */
public class SoapHelper {

	/**
	 * 方法名称
	 */
	private String method;
	/**
	 * 超时时间
	 */
	private int timeout = 5000;

	/**
	 * 构造函数
	 * 
	 */
	public SoapHelper(String method) {
		this.method = method;
	}

	/**
	 * 通讯初始化操作
	 */
	public Object init(ICallBack callBack, Map<String, Object> parameters) {
		Object result = null;

		SoapObject soapObject = new SoapObject(AppConstant.SERVICE_NAMESPACE,
				method);

		if (parameters != null) {
			for (String key : parameters.keySet()) {
				soapObject.addProperty(key, parameters.get(key));
			}
		} 

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);
		envelope.setOutputSoapObject(soapObject);

		(new MarshalBase64()).register(envelope);

		try {

			HttpTransportSE httpTranstation = new HttpTransportSE(
					AppConstant.SERVICE_URL, timeout);
			httpTranstation.debug = true;
			envelope.dotNet = true;
			httpTranstation.call(AppConstant.SERVICE_NAMESPACE + method,
					envelope);
			result = (Object) envelope.getResponse();

			if (result != null) {
				return result;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (callBack != null) {
			callBack.netFailed();
		}
		return null;
	}

	/**
	 * 获取通讯返回数据信息
	 */
	public String initReturnString(ICallBack callBack,
			Map<String, Object> parameters) {

		SoapObject soapObject = new SoapObject(AppConstant.SERVICE_NAMESPACE,
				method);

		if (parameters != null) {
			for (String key : parameters.keySet()) {
				soapObject.addProperty(key, parameters.get(key));
			}
		}

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);
		envelope.setOutputSoapObject(soapObject);

		(new MarshalBase64()).register(envelope);

		try {

			HttpTransportSE httpTranstation = new HttpTransportSE(
					AppConstant.SERVICE_URL, timeout);
			httpTranstation.debug = true;
			envelope.dotNet = true;
			httpTranstation.call(AppConstant.SERVICE_NAMESPACE + method,
					envelope);

			if (envelope.getResponse() != null) {
				return envelope.getResponse().toString();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (Exception e) {

		}

		if (callBack != null) {
			callBack.netFailed();
		}
		return null;
	}

}
