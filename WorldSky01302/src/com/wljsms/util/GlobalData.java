package com.wljsms.util;

import java.util.List;
import java.util.Map;

import com.wljsms.info.ContactInfo;

public class GlobalData {
	
	private GlobalData()
	{
		
	}
	
	private static GlobalData instance;
	
	public static GlobalData getInstance()
	{
		if(GlobalData.instance == null)
		{
			instance = new GlobalData();
		}
		
		return instance;
		
	}
	
	
	private List<ContactInfo> selectedContacts;
	private List<ContactInfo> callOutContacts;
	private ContactInfo myselfContactInfo;
	private Map<String, String> metingDetails;
	private String myPhoneNum;
	
	
	public List<ContactInfo> getSelectedContacts()
	{
		return selectedContacts;
	}
	
	public Map<String, String> getMetingDetails()
	{
		return metingDetails;
	}
	
	public void  setSelectedContacts(List<ContactInfo> list) 
	{
		this.selectedContacts = list;
	}
	
	public void setMetingDetails(Map<String, String> map)
	{
		this.metingDetails = map;
	}
	
	public void setMyPhoneNum(String phonenum)
	{
		this.myPhoneNum = phonenum;
		this.myselfContactInfo = new ContactInfo();
		
		this.myselfContactInfo.setName("本机");
		this.myselfContactInfo.setPhone(phonenum);
		
	}
	
	public String getMyPhoneNum()
	{
		return myPhoneNum;
	}
	
	public ContactInfo getMyselfContactInfo()
	{
		return this.myselfContactInfo;
	}
	
	public void setCallOutContactInfo(List<ContactInfo> list)
	{
		this.callOutContacts = list;
	}
	
	public List<ContactInfo> getCallOutContactInfo()
	{
		return this.callOutContacts;
	}
	
}
