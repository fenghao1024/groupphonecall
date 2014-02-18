/**
 * Name : AppConstant.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.config;

import android.R.integer;

/**
 * 
 * com.eteng.config.AppConstant
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-30 下午2:12:22 Description :
 *         服务地址配置文件类
 */
public class AppConstant {

	//命名空间
	public static final String SERVICE_NAMESPACE = "http://tempuri.org/";
	//服务器地址,用于急短信提交数据
	public static final String SERVICE_URL = "http://219.151.3.13:8002/wlj/iWanglaojiInfo.asmx";
	
	
	
	//public static final String HTTP_SERVER_NAMESPACE = "http://117.135.196.139/";   
	public static final String HTTP_SERVER_ADDR = "http://117.135.196.139:9388/txl-pbx";
	
	public static final String SF_SERVICE_NAMESPACE = "http://connector.sf.com";
	//public static final String SERVICE_WSDL = "http://192.168.1.117:8799/YdMeet/services";
	public static final String SERVICE_WSDL = "http://192.168.1.117:8799/YdMeet/services/YdMeeting";
	
	public static final String MEETING_REQPARAM = "reqparam";
	
	public static final String METHOD_CREATE_MEETING = "/app/createMeeting.app";
	public static final String METHOD_JOIN_MEETING = "/app/joinMeetingByPhone.app";
	public static final String METHOD_GET_MEETING_STATUS = "/app/getMeetingStateList.app";
	public static final String METHOD_CLOSE_MEETING = "/app/kickAllPhoneFromMeeting.app";
	
	public static final int MSG_CREATE_MEETING = 1;
	public static final int MSG_JOIN_MEETING = 2;
	public static final int MSG_GET_MEETING_STATUS = 3;
	public static final int MSG_CLOSE_MEETING = 5;
	
	public static final String DES_PASSWORD = "application";
	
	public static final String WEBSERVICE_METHOD = "Meet";
	
	public static final int CONNECTING = 1;
	public static final int CONNECT_FAILD = 3;
	public static final int HANG_UP_ED = 4;
	public static final int TALKING = 2;

}
