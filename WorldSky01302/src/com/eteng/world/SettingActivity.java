package com.eteng.world;

import com.wljsms.info.ConstantsInfo;
import com.wljsms.util.GlobalData;
import com.wljsms.util.Utils;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SettingActivity extends Activity {
	
	private SharedPreferences sp;
	private EditText phoneNumEditText ;
	private String phoneNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		sp = getSharedPreferences(ConstantsInfo.SHARED_FILE_NAME, MODE_PRIVATE);
		
		initView();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}
	
	void initView()
	{
		phoneNumEditText = (EditText)findViewById(R.id.phonenum_edit_text);
		
		if(GlobalData.getInstance().getMyPhoneNum() != null)
		{
			phoneNumEditText.setText(GlobalData.getInstance().getMyPhoneNum());
		}
		
	}
	
	public void phonenum_setting_button_click(View view)
	{
		if(phoneNumEditText.getText().toString().equals(""))
		{
			return ;
		}
		phoneNum = phoneNumEditText.getText().toString();
		GlobalData.getInstance().setMyPhoneNum(phoneNum);
		
		Editor e = sp.edit();
		e.putString("my_phone_num", phoneNum);
		e.commit();
		
		Utils.showCustomToast(SettingActivity.this, "本机号码设置成功！！");
		
		this.finish();
		
	}
	
	public void back_button_click(View view)
	{
		this.finish();
	}

}
