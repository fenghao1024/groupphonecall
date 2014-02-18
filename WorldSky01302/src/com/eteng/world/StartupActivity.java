package com.eteng.world;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.util.GlobalData;
import com.wljsms.util.Utils;

/**
 * com.eteng.world.StartupActivity
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-20 上午9:42:03 Description : 启动引导类
 */
public class StartupActivity extends Activity {

	private SharedPreferences sp;
	private String imsi;
	private Toast toastStart;
	private TelephonyManager tm;
	private SmsApplication mApplication;
	//取imsi号重试次数，3次，每次间隔200毫秒
	private int mCount = 0;
	//取imsi号的定时器对象
	private Timer timer;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.startup);
		sp = getSharedPreferences(ConstantsInfo.SHARED_FILE_NAME, MODE_PRIVATE);
		mApplication = (SmsApplication) this.getApplication();
		mApplication.loadContacter();
		mApplication.loadRecentContacter();
		
		// 根据 SharedPreferences 设置本机号码
		setMyPhoneNum_by_File();

		View toastRoot = getLayoutInflater().inflate(R.layout.toast_layout,
				null);
		TextView message = (TextView) toastRoot.findViewById(R.id.message);
		message.setText("启动中...请稍候...");

		toastStart = new Toast(this);
		toastStart.setGravity(Gravity.BOTTOM, toastStart.getXOffset() / 2,
				toastStart.getYOffset() / 3);
		toastStart.setDuration(Toast.LENGTH_LONG);
		toastStart.setView(toastRoot);
		toastStart.show();
		if(mApplication.getImsi() == null || mApplication.getImsi().equals("")){
			//没有imsi号，重新获取
			tm = (TelephonyManager) this
					.getSystemService(Context.TELEPHONY_SERVICE);
			timer = new Timer();
//			String phoneNum = tm.getLine1Number();
//			Log.d("PhoneNum is :", phoneNum);
			TimerTask timerTask = new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mCount++;
					if(mCount > 3){
						//超出重试次数，退出
						DebugFlags.EtengLog("取imsi号超出3次，不再获取，给出提示");
						handler.sendEmptyMessage(1);
						timer.cancel();
						timer = null;
						return;
					}
					imsi = tm.getSubscriberId();
					
					if (imsi != null && !imsi.equals("")) {
						DebugFlags.EtengLog("第" + mCount + "次成功获取到imsi号，imsi号为：" + imsi + ",结束获取过程");
						mApplication.setImsi(imsi);
						timer.cancel();
						timer = null;
						return;
					}
				}

			};
			timer.schedule(timerTask, 1, 200);
			
			handler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					if(msg.what == 1) {
						if(imsi == null || imsi.equals("")){
							//没有取到imsi号，弹出提示
							Utils.showCustomToast(StartupActivity.this, "检测不到手机卡，无法发通知和语音呼叫");
						}
					}
				}
				
			};
		}
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
					if (checkFirstBoot()) {
						setFirstBoot(false);
						Intent intent = new Intent();
						intent.setClass(StartupActivity.this, NewActivity.class);
						startActivity(intent);
						StartupActivity.this.finish();
					} else {
						Intent intent = new Intent();
						
						//intent.setClass(StartupActivity.this, Look.class);
						intent.setClass(StartupActivity.this, SmsGroupSend.class);
						
						startActivity(intent);
						StartupActivity.this.finish();
					}
					if (toastStart != null)
						toastStart.cancel();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	/**
	 * 检查是否第一次启动
	 */
	public boolean checkFirstBoot() {
		return sp.getBoolean(ConstantsInfo.PREFERENCE_FIRST_BOOT, true);
	}

	/**
	 * 设置启动状态
	 */
	private void setFirstBoot(boolean b) {
		Editor editor = sp.edit();
		editor.putBoolean(ConstantsInfo.PREFERENCE_FIRST_BOOT, b);
		editor.commit();
	}
	
	private void setMyPhoneNum_by_File()
	{
		String phonenumString = sp.getString("my_phone_num", "");
		GlobalData.getInstance().setMyPhoneNum(phonenumString);
	}
}
