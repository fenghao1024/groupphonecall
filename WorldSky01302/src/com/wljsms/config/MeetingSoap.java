package com.wljsms.config;

import java.io.IOException;
import java.util.Map;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.LauncherActivity;
import android.app.Service;
import android.util.Log;

public class MeetingSoap {
	
	public String _method;
	
	
	public MeetingSoap()
	{
		
	}
	
	public String Launch(String method, String param)
	{
		String resultString = "";
		
		SoapObject request = new SoapObject(AppConstant.SF_SERVICE_NAMESPACE, method);
		
		request.addProperty("message", param);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.bodyOut = request;
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);
		
		HttpTransportSE ht = new HttpTransportSE(AppConstant.SERVICE_WSDL);
		
		try 
		{
			String callStr = AppConstant.SF_SERVICE_NAMESPACE + "/" + method;
			ht.call(callStr,envelope);
			if (envelope.getResponse() != null)
            {
				resultString = envelope.getResponse().toString();
				
				return resultString;
            }
			else
            {
            	return "";
            }
		} 
		catch (Exception e) 
		{
			 
	         e.printStackTrace();
	         return "";
		}
		
	}

}
