/**
 * Name : DebugFlags.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.debug;

import android.util.Log;

/**
 * com.eteng.debug.DebugFlags
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-15 上午10:58:11 Description : 调试类，控制log信息的输出 Modified
 *         :
 */
public class DebugFlags {
	
	private static final boolean flag = true;

	private static final String TAG = "Eteng";

	public static void EtengLog(String msg) {
		if (flag)
			Log.d(TAG, msg);
	}
}
