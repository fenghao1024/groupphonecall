package com.eteng.world;

import com.wljsms.util.Utils;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * com.eteng.world.Sms_aboutActivity
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-20 下午2:12:22 
 *         Description :关于页面   
 */
public class Sms_aboutActivity extends Activity {
    /** Called when the activity is first created. */
	
	private  TextView txtEmail;
	private String email = "wanglaoji@189.cn";
	private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.about);
        mWebView = (WebView) findViewById(R.id.about_webview);
		mWebView.loadUrl("file:///android_asset/about.html");
        ImageView backImage = (ImageView) findViewById(R.id.sms_back);
        backImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
        
        TextView txtVersion = (TextView) findViewById(R.id.version);
        String txt = Utils.getCurrentVersion(Sms_aboutActivity.this);
        txtVersion.setText("版本号: v" + txt);
        
        txtEmail = (TextView) findViewById(R.id.emailAddress);
        txtEmail.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try{
					callEmailApp(v);
					Sms_aboutActivity.this.finish();
				} catch (Exception e) {
					return;
				}
			}
		});
    }
    
    /**
	 * 调用邮箱发送
	 */
    public void callEmailApp(View view) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"
				+ txtEmail.getText().toString()));
		intent.putExtra(Intent.EXTRA_SUBJECT, "急短信,我有话对你说:");
		intent.putExtra(Intent.EXTRA_BCC, email);
		intent.putExtra(Intent.EXTRA_CC, email);
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {  });
		startActivity(intent);
	}
 
}