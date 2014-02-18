/**
 * Name : NetworkBroadcastReceiver.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.sms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.wljsms.debug.DebugFlags;
import com.wljsms.sms.service.SendSmsDialogService;
import com.wljsms.sms.service.SmsService;

/**
 * com.eteng.sms.receiver.NetworkBroadcastReceiver
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-25 下午3:21:55 Description : 网络状态变化广播接收器，用户监控有网络的情况
 *         Modified :
 */
public class NetworkBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
			DebugFlags.EtengLog("收到开机广播，启动后台服务 : " + intent.getAction());
			// 开启监听回复短信服务
			context.startService(new Intent(context, SmsService.class));
			context.startService(new Intent(context,SendSmsDialogService.class));
		}
	}

}
