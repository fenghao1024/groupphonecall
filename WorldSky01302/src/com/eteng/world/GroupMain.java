/**
 * Name : GroupMain.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.eteng.world;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 
 * com.eteng.world.GroupMain
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-19 下午2:17:56 Description :
 *         群组，最近联系人，所有联系人容器类 Modified :
 */
public class GroupMain extends ActivityGroup {
	/**
	 * 所有联系人按钮
	 */
	private Button Btn_User;  
	/**
	 * 最近联系人按钮
	 */
	private Button Btn_UserEx; 
	/**
	 * 群组按钮
	 */
	private Button Btn_Group; 
	/**
	 * 容器对象
	 */
	public LinearLayout container; 
	/**
	 * 返回
	 */
	private ImageView mBackImg;  

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_mains);
		Btn_User = (Button) findViewById(R.id.Btn_User);
		Btn_UserEx = (Button) findViewById(R.id.Btn_UserEx);
		Btn_Group = (Button) findViewById(R.id.Btn_Group);
		mBackImg = (ImageView)findViewById(R.id.group_main_back);
		
		//返回按钮的单击事件
		mBackImg.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(GroupMain.this, SmsGroupSend.class);
				intent.putExtra("draft_str", ((SmsApplication)GroupMain.this.getApplication()).mDraft);
				intent.putExtra("groupDate", ((SmsApplication)GroupMain.this.getApplication()).getGroupDate());
				startActivity(intent);
				GroupMain.this.finish();
			}
			
		});
		//群组按钮的点击事件
		Btn_Group.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				SwitchActivity(0);
			}
		});
		//所有联系人按钮的点击事件
		Btn_User.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SwitchActivity(1);
			}
		});
		//最近联系人按钮的点击事件
		Btn_UserEx.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SwitchActivity(2);
			}

		});

		container = (LinearLayout) findViewById(R.id.Container);
		//默认显示群组
		SwitchActivity(0);
	} 

	/**
	 * 页面切换
	 */
	void SwitchActivity(int id) {
		try {
			container.removeAllViews();
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			if (id == 0) {
				intent = new Intent(GroupMain.this, GroupList.class);
				Btn_User.setBackgroundResource(R.drawable.sys_recently_normal);
				Btn_UserEx.setBackgroundResource(R.drawable.sys_all_normal);
				Btn_Group.setBackgroundResource(R.drawable.sys_group_pressed);
			} else if (id == 1) {
				intent.setClass(GroupMain.this, ContacterRecent.class);
				Btn_User.setBackgroundResource(R.drawable.sys_recently_pressed);
				Btn_UserEx.setBackgroundResource(R.drawable.sys_all_normal);
				Btn_Group.setBackgroundResource(R.drawable.sys_group_normal);
			} else if (id == 2) {
				intent = new Intent(GroupMain.this, AllContacter.class); 
				Btn_User.setBackgroundResource(R.drawable.sys_recently_normal);
				Btn_UserEx.setBackgroundResource(R.drawable.sys_all_pressed);
				Btn_Group.setBackgroundResource(R.drawable.sys_group_normal);
			}
			Window subActivity = getLocalActivityManager().startActivity(
					"subActivity", intent);
			container.addView(subActivity.getDecorView(),
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 手机键盘返回键监听
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClass(GroupMain.this, SmsGroupSend.class);
		intent.putExtra("draft_str", ((SmsApplication)GroupMain.this.getApplication()).mDraft);
		intent.putExtra("groupDate", ((SmsApplication)GroupMain.this.getApplication()).getGroupDate());
		startActivity(intent);
		GroupMain.this.finish();
		return true;
	}

}