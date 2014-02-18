package com.wljsms.util;

import java.security.PublicKey;

import com.eteng.world.SmsApplication;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * 确定屏幕大小类
 * @author wanglaoji
 */
public class Configure {
	public static int screenHeight=0;
	public static int screenWidth=0;
	public static float screenDensity=0;
	
	public static void init(Activity context) {
		if(screenDensity==0||screenWidth==0||screenHeight==0){
			DisplayMetrics dm = new DisplayMetrics();
			context.getWindowManager().getDefaultDisplay().getMetrics(dm);
			Configure.screenDensity = dm.density;
			Configure.screenHeight = dm.heightPixels;
			Configure.screenWidth = dm.widthPixels;
		}
		
	}
	
}
