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
 * com.eteng.services.GetCallReturn
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-30 下午2:12:22 Description :
 *         发起电话呼叫服务接口
 */

public class GetCallReturn {

	private static final String METHOD = "GetCallStatusNew";
	
	/**
	 * 创建网络连接，异步回调获取数据，并且返回数据实体对象
	 */
	
	public List<ReturnInfo> init(String IDs, ICallBack callBack) {
		List<ReturnInfo> ReturnList = null;

		SoapHelper1 soapHelper = new SoapHelper1(METHOD);

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("groupid", IDs);

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
	 * 把XML数据转换为任务编号数据实体集合
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
