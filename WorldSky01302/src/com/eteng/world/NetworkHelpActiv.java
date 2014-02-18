/**
 * Name : NetworkHelpActiv.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.eteng.world;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;

/**
 * com.eteng.testlistview.MyLetterView
 * 
 * @author wanglaoji <br/>
 *         Create at 2012-2-20 上午10:18:55 Description : 网络异常帮助类 Modified :
 */
public class NetworkHelpActiv extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.network_help_layout);
		ImageView img = (ImageView) findViewById(R.id.sms_network_back);
		img.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				NetworkHelpActiv.this.finish();
			}

		});
		WebView webView = (WebView) findViewById(R.id.network_help_mv);
		webView.loadUrl("file:///android_asset/WLAN.html");
	}
}
