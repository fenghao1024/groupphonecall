/**
 * Name : GetApkVersion.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.webservice;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;

import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.info.VersionInfo;

/**
 * com.eteng.world.webservice.GetApkVersion
 * @author wanglaoji <br/>
 * Create at 2013-1-16 上午11:20:26
 * Description : 调用服务器接口，取得版本信息
 * Modified : 
 */
public class GetApkVersion {

	private static final String METHOD = "VersionAndLogin";

	public VersionInfo init(String imsi,String channel,ICallBack callBack) {

		VersionInfo versionInfo = null;
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("imsi", imsi);
		m.put("channel", channel);
		versionInfo = getVersion(m);
		return versionInfo;
	}

	private VersionInfo getVersion(Map<String, Object> parameters) {
		VersionInfo info = new VersionInfo();
		String SOAP_ACTION = "http://tempuri.org/VersionAndLogin";
		Object detail;
		try {
			int timeout = 5000;  //set timeout 15s  
			SoapObject rpc = new SoapObject(ConstantsInfo.SERVICE_NAMESPACE, METHOD);
			if (parameters != null) {
				for (String key : parameters.keySet()) {
					rpc.addProperty(key, parameters.get(key));
				}
			}
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			MyAndroidHttpTransport ht = new MyAndroidHttpTransport(ConstantsInfo.SERVICE_URL, timeout);  
			ht.debug = true;  
			ht.call(SOAP_ACTION, envelope);
			detail = (Object) envelope.getResponse();
			String content = detail.toString();
			DebugFlags.EtengLog(content);
			JSONArray arrayJson = new JSONArray(content);
			if(arrayJson.length() == 0)
				return null;
			JSONObject obj = arrayJson.optJSONObject(0);
			
			String VersionCode = obj.optString("VersionCode");
			String VersionLink = obj.optString("VersionLink");
			String VersionName = obj.optString("VersionName");
			String forceUpdate = obj.optString("UpType");
			String Message = obj.optString("Message");

			info.setVersionCode(VersionCode);
			info.setVersionName(VersionName);
			info.setVersionLink(VersionLink);
			info.setForceUpdate(forceUpdate);
			info.setMessage(Message);

			return info;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
