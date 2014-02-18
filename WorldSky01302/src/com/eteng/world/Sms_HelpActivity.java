/**
 * Name : Sms_HelpActivity.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.eteng.world;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;

/**
 * com.eteng.world.Sms_HelpActivity
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-20 下午2:12:22 Description :帮组页面
 */
public class Sms_HelpActivity extends Activity {
	/** Called when the activity is first created. */
	private WebView mWebView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sms_help);

		ImageView btn_back = (ImageView) findViewById(R.id.sms_back);
		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mWebView = (WebView) findViewById(R.id.myWebView);
		mWebView.loadUrl("file:///android_asset/help.html");
	}

}
