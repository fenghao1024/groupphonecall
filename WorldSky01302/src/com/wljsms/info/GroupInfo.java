/**
 * Name : GroupInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * com.eteng.info.GroupInfo
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-5 上午9:21:52 
 *         Description : 群组信息类
 */
public class GroupInfo {
	/**
	 * 群组名称
	 */
	private String name;
	/**
	 * 群组id
	 */
	private long id;
	/**
	 * 群组中的联系人列表
	 */
	private List<ContactInfo> childs;
	/**
	 * 群组是否展开，True:展开，False：折叠
	 */
	private boolean expandable = false;
	/**
	 * 群组中联系人是否全部选择，True：全部选择，False：没全部选择
	 */
	private boolean allChecked = false;
	/**
	 * 群组中联系人是否部分选择，True：部分选择，False：全选或没选
	 */
	private boolean someChecked = false;// 部分选中
	/**
	 * 系统内置群组号，用于标志系统内置群组
	 */
	private String systemId;
	
	public GroupInfo() {
		childs = new ArrayList<ContactInfo>();
	}

	public boolean isExpandable() {
		return expandable;
	}

	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}

	/**
	 * 添加一个联系人到群组的联系人列表中
	 * @param info:联系人信息类
	 */
	public void addOneChild(ContactInfo info) {
		childs.add(info);
	}

	/**
	 * 添加一批联系人到群组的联系人列表中
	 * @param info:联系人信息列表
	 */
	public void addSomeChilds(List<ContactInfo> info) {
		childs.addAll(info);
	}

	/**
	 * 从群组联系人列表中移除一个联系人
	 * @param info:联系人信息类
	 */
	public void removeOneChild(ContactInfo info) {
		childs.remove(info);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<ContactInfo> getChilds() {
		return childs;
	}

	public void setChilds(List<ContactInfo> childs) {
		this.childs = childs;
	}

	public boolean isAllChecked() {
		return allChecked;
	}

	public void setAllChecked(boolean allChecked) {
		this.allChecked = allChecked;
	}

	public boolean isSomeChecked() {
		return someChecked;
	}

	public void setSomeChecked(boolean someChecked) {
		this.someChecked = someChecked;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

}
