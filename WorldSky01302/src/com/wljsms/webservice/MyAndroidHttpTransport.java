/**
 * Name : MyAndroidHttpTransport.java
 * Version : 0.0.1
 * Copyright : Copyright (c) Eteng Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.webservice;

import java.io.IOException;

import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.ServiceConnection;

/**
 * com.eteng.world.webservice.MyAndroidHttpTransport
 * @author wanglaoji <br/>
 * Create at 2013-1-29 下午3:40:51
 * Description : 
 * Modified : 
 */
public class MyAndroidHttpTransport extends HttpTransportSE {
	private int timeout = 10000; //默认超时时间为30s  
    
    public MyAndroidHttpTransport(String url) {  
        super(url);  
    }  
      
    public MyAndroidHttpTransport(String url, int timeout) {  
        super(url);  
        this.timeout = timeout;  
    }  
   
    protected ServiceConnection getServiceConnection(String url) throws IOException {  
        ServiceConnectionSE serviceConnection = new ServiceConnectionSE(url);  
        serviceConnection.setConnectTimeOut(timeout);  
        return new ServiceConnectionSE(url);  
    } 
}
