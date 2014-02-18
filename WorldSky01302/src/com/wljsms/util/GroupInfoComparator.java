/**
 * Name : GroupInfoComparator.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.util;

import java.util.Comparator;

import com.wljsms.info.GroupInfo;

/**
 * com.eteng.world.util.GroupInfoComparator
 * @author wanglaoji <br/>
 * Create at 2013-1-25 下午12:32:33
 * Description : 
 * Modified : 
 */
public class GroupInfoComparator implements Comparator<GroupInfo> {
	
	@Override
	public int compare(GroupInfo lhs, GroupInfo rhs) {
		// TODO Auto-generated method stub
		if(lhs.getChilds().size() > rhs.getChilds().size())
			return -1;
		return 1;
	}

}
