
package com.wljsms.webservice;

import java.util.HashMap;
import java.util.Map;

import com.wljsms.config.ICallBack;
import com.wljsms.config.SoapHelper;

import android.util.Log;
/**
 * 
 * com.eteng.services.GetPhone
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-3-14 下午2:12:22 Description :
 *        
 */
public class SetDialogBoxInfo {

	private static final String METHOD = "SetDialogBoxInfo";//接口方法名

	private static final String TAG = "SetDialogBoxInfo"; // 类标识
	
	//创建网络连接，异步回调获取数据
	public Boolean init(String data, ICallBack callBack) {

		SoapHelper soapHelper = new SoapHelper(METHOD);// 实例化SOAP对象

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("data", data); 
		String result = soapHelper.initReturnString(callBack, m);//调用数据通讯并且返回SoapObject对象
		if(result!=null && ! result.equals("")) {
			Log.d(TAG, result);
			if(result.equals("true")) {
				return true;
			}
		}
		return false;
	}

}
