package com.eteng.world;

import com.wljsms.util.Utils;
import com.wljsms.webservice.AddFeedBack;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 
 * com.eteng.world.Sms_FeedbackActivity
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-20 下午2:12:22 Description :反馈页面
 */
public class Sms_FeedbackActivity extends Activity {

	private EditText txtContent;
	private EditText txtContact;
	private AddFeedBack addFeedBack;
	private Button btn;
	private ImageView sms_back;
	private Handler handler;
	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sms_feedback);
		initView();

		sms_back.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Sms_FeedbackActivity.this.finish();
			}
		});

		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				initialData();
				String imsi = ((SmsApplication) getApplication()).getImsi();
				String content = txtContent.getText().toString().trim();
				String contact = txtContact.getText().toString().trim();
				String version = Utils
						.getCurrentVersion(Sms_FeedbackActivity.this);

				if (content.equals("") || content == null) {
					Toast.makeText(Sms_FeedbackActivity.this, "老板，总得给句话嘛",
							Toast.LENGTH_SHORT).show();
					return;
				}

				handlerProcess(imsi, content, contact, version);
			}
		});
	}

	private void initView() {
		sms_back = (ImageView) findViewById(R.id.sms_back);
		btn = (Button) findViewById(R.id.button1);
		txtContent = (EditText) findViewById(R.id.editContent);
		txtContact = (EditText) findViewById(R.id.editContact);

	}

	private boolean upLoad(String imsi, String content, String contact,
			String version) {
		addFeedBack = new AddFeedBack();
		return addFeedBack.init(imsi, content, contact, version, null);
	}

	private void initialData() {
		// 在初始化方法中 定义 handler :专门用于本Activity内部的 处理机制
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int resultCode = msg.what;
				if (resultCode > 1) {
					Toast.makeText(Sms_FeedbackActivity.this, "提交成功,感谢您支持!",
							Toast.LENGTH_LONG).show();

				} else {
					Toast.makeText(Sms_FeedbackActivity.this, "抱歉，提交失败，再试一下？",
							Toast.LENGTH_LONG).show();
				}
				dialog.dismiss();
				Sms_FeedbackActivity.this.finish();
			}
		};
	}

	private void handlerProcess(final String imsi, final String content,
			final String contact, final String version) {
		if (!Utils.isNetworkConnected(Sms_FeedbackActivity.this)) {
			Toast.makeText(Sms_FeedbackActivity.this, "抱歉，你的网络似乎有点问题，再试一下？",
					Toast.LENGTH_LONG).show();
			return;
		}
		dialog = ProgressDialog.show(Sms_FeedbackActivity.this, "提示",
				"正在发送你的意见，稍等片刻...");
		new Thread(new Runnable() {
			public void run() {
				Message msg = new Message();
				if (upLoad(imsi, content, contact, version))
					msg.what = 2;
				else
					msg.what = 0;
				handler.sendMessage(msg);
			}
		}).start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(Sms_FeedbackActivity.this
				.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
		return super.onTouchEvent(event);
	}
}