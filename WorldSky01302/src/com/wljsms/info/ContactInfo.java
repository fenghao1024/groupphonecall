/**
 * Name : ContactInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

import java.io.Serializable;
/**
 * 
 * c com.eteng.entity.ContactInfo
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-30 下午2:12:22 Description :
 *         联系人数据实体
 */
public class ContactInfo implements Serializable , Cloneable{
	
	private static final long serialVersionUID = 1L;
	private String name;   //姓名
	private String phone;  //电话
	private long contactId;  //联系人编号
	private String sortKey;  //排序键
	private int status = 2;// 1:选中，2：未选中
	private boolean checked = false; 
	private String pingyin;  //拼音码
	private String context;  //内容
	private long groupId;//联系人属于哪个组
	/**
	 * name的拼音首字母
	 */
	private String alpha;
	/**
	 * 新联系人标志，即只有号码，且该号码在系统通讯录里找不到。1：非新联系人，2：新联系人
	 */
	private String mNewContact = "1";
	
	/**
	 * 默认构造方法
	 */
	public ContactInfo(){
		
	}
	
	/**
	 * 有2个参数的构造方法，传递联系人姓名和号码
	 * @param name : 姓名
	 * @param phone : 号码
	 */
	public ContactInfo(String name,String phone){
		this.name = name;
		this.phone = phone;
	}
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public long getContactId() {
		return contactId;
	}

	public void setContactId(long contactId) {
		this.contactId = contactId;
	}

	public String getSortKey() {
		return sortKey;
	}

	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getPingyin() {
		return pingyin;
	}

	public void setPingyin(String pingyin) {
		this.pingyin = pingyin;
	}
	
	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}
	
	public String getAlpha() {
		return alpha;
	}

	public void setAlpha(String alpha) {
		this.alpha = alpha;
	}
	
	public String getmNewContact() {
		return mNewContact;
	}

	public void setmNewContact(String mNewContact) {
		this.mNewContact = mNewContact;
	}

	/**
	 * 克隆方法，用于复制此对象
	 */
	public Object clone() {
		Object o = null;
		try {
			o = (ContactInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}

}
