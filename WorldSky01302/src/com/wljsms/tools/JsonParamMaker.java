package com.wljsms.tools;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.R.string;

public class JsonParamMaker {
	
	
	public static String toJsonItem(String key, String param)
	{
		String c = "\"";
		return c + key + c + ":" + c + param + c;
	}
	
	public static String MapToJStr(Map<String , String> map)
	{
		String resultString = "";
		
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			Object keyObject = entry.getKey();
			Object valueObject = entry.getValue();
			
			resultString += toJsonItem(((String)keyObject),(String)valueObject);
			resultString += ",";
			
		}
		resultString = resultString.substring(0, resultString.length() - 1);
		
		return resultString;
	}
	
	public static String listToJString(List<String[]> list)
	{
		String resultString = "";
		 
		for (String[] tempList : list) {
			String keyString = tempList[0];
			String valueString = tempList[1];
			
			resultString += "{";
			resultString += toJsonItem(keyString, valueString);
			resultString += "}";
		
			resultString += ",";
		}
		
		resultString = resultString.substring(0, resultString.length() - 1);
		
		return resultString;
	}
	
	public static String toJsonArray(String key, String jsonArrayValue)
	{
		String resultString = "";
		
		String bufferString = "[";
		bufferString += jsonArrayValue;
		bufferString += "]";
		
		String bufferString2 = "\"";
		bufferString2 += key;
		bufferString2 += "\"";
		
		bufferString2 += ":";
		
		bufferString2 += bufferString;
		
		resultString = bufferString2;
		
		return resultString;
	}
	
	public static String toCompleteJson(String jsonArrayString)
	{
		String resultString = "";
		resultString = "{";
		resultString += jsonArrayString;
		resultString += "}";
		
		return resultString;
	}
	

}
