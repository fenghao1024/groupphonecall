/**
 * Name : SmsThreadInfoComparator.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.util;

import java.util.Comparator;

import com.wljsms.info.SmsThreadInfo;

/**
 * com.eteng.world.util.SmsThreadInfoComparator
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-16 下午3:27:05 Description : 比较器类，对短信信息升序排序 Modified :
 */
public class SmsThreadInfoComparator implements Comparator<SmsThreadInfo> {
	@Override
	public int compare(SmsThreadInfo lhs, SmsThreadInfo rhs) {
		// TODO Auto-generated method stub
		if (lhs.getTime() > rhs.getTime())
			return 1;
		return -1;
	}

}
