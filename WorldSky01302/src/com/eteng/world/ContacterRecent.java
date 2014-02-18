/**
 * Name : ContacterRecent.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.eteng.world;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.eteng.world.MyLetterView.OnTouchingLetterChangedListener;
import com.wljsms.adapter.RecentContactAdapter;
import com.wljsms.info.ContactInfo;
import com.wljsms.util.Utils;

/**
 * com.eteng.world.ContacterRecent
 * 
 * @author wanglaoji Create at 2012-2-22 下午2:49:46 Description :
 *         获取最近联系人功能类,并且实现可以通过拼音首字母对联系人进行定位功能。
 */
public class ContacterRecent extends Activity implements
		OnTouchingLetterChangedListener {
	/**
	 * 最近联系人列表对象
	 */
	private ListView lvPhones;
	/**
	 * 底部显示选择的TEXTVIEW
	 */
	private TextView btnConfirm; 
	/**
	 * 最近联系人适配器
	 */
	private RecentContactAdapter mRecentContactAdapter; 
	/**
	 * 最近联系人数据集合
	 */
	private List<ContactInfo> arrayList = new ArrayList<ContactInfo>(); 
	// 以下3行用于侧边文字
	private MyLetterView myView; 
	private TextView letterTv;
	private OverlayThread overlayThread = new OverlayThread();
	
	/**
	 * 应用对象
	 */
	private SmsApplication mApplication; 
	/**
	 * 处理UI同步句柄对象
	 */
	private static Handler handler; 
	/**
	 * 数据未加载完毕提示等待对话框对象
	 */
	private ProgressDialog dialog; 
	/**
	 * 联系人已选择的提示字符
	 */
	private String mToastItemStr; 
	/**
	 * 搜索框，因为最近联系人里不要求有，故隐藏
	 */
	private View view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 获取主应用实例
		mApplication = (SmsApplication) this.getApplication(); 
		mToastItemStr = getResources().getString(
				R.string.no_select_toast_item_str);
		 // 初始化布局控件文件
		initUI();
		// 最近联系人列表点击事件
		lvPhones.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				ContactInfo info = arrayList.get(arg2); // 获取点击的对象
				if (info.isChecked()) {
					//选中
					info.setChecked(false); // 设置为未选中
					mRecentContactAdapter.selectMinusOne(); // 计数器减1
					mApplication.removeContactInfo(info); // 移除该对象
				} else {
					//未选中
					if (mApplication.addContactInfo(info)) { // 加入集合
						info.setChecked(true); // 设置被选择
						mRecentContactAdapter.selectAddOne(); // 计数器加1
					} else {
						//提示联系人已经被选择
						Utils.showCustomToast(ContacterRecent.this,
								mToastItemStr);
						return;
					}
				}
				mRecentContactAdapter.notifyDataSetChanged();
			}
		});

		// 用于获取后台运行的线程返回的执行结果，同步到UI,以防止主线程死锁
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (msg.what == 2) {
					if (dialog != null && dialog.isShowing())
						dialog.dismiss();
					arrayList = mApplication.getmRecentContactList(); // 获取最近联系人数据集合

					if (mApplication.selectedContactsCount > 0) // 如果大于0
						btnConfirm.setText("确  定 ("
								+ mApplication.selectedContactsCount + ")"); // 设置计数
					mRecentContactAdapter = new RecentContactAdapter(
							ContacterRecent.this, arrayList, btnConfirm,
							mApplication); // 实例化最近联系人适配器
					lvPhones.setAdapter(mRecentContactAdapter); // 绑定列表视图

				}
			}

		};

		mApplication.setHandler(handler);

		if (!mApplication.ismLoadRecentContactComplete()) { // 最近联系人是否加载完成，如果没有
			dialog = ProgressDialog.show(ContacterRecent.this, null,
					"正在加载联系人信息..."); // 弹出加载进度提示
			dialog.setCancelable(true);
		} else {
			arrayList = mApplication.getmRecentContactList(); // 获取最近联系人数据集合
			
			if (mApplication.selectedContactsCount > 0) // 如果大于0
				btnConfirm.setText("确  定 (" + mApplication.selectedContactsCount
						+ ")");
			mRecentContactAdapter = new RecentContactAdapter(
					ContacterRecent.this, arrayList, btnConfirm, mApplication);
			lvPhones.setAdapter(mRecentContactAdapter); // 绑定列表视图
		}

		//判断选中集合数是否大于0
		if (mApplication.selectedContactsCount > 0)
			btnConfirm.setText("确  定 (" + mApplication.selectedContactsCount + ")");
		// 确认选择点击事件
		btnConfirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mApplication.getmContactsList().size() == 0) {

					Utils.showCustomToast(ContacterRecent.this, "请选择联系人!");
					return;
				}
				Intent intent = new Intent();
				intent.setClass(ContacterRecent.this, SmsGroupSend.class);
				intent.putExtra("groupDate", mApplication.getGroupDate());
				intent.putExtra("draft_str", mApplication.mDraft);
				startActivity(intent);// 启动群组发送处理类
				mRecentContactAdapter.notifyDataSetChanged();// 通知最近联系人适配器数据发生改变
				ContacterRecent.this.finish();// 结束最近联系人处理类
			}
		});
	}

	// 从布局文件实例化用户控件对象
	private void initUI() {
		btnConfirm = (TextView) findViewById(R.id.btn_SendSms);
		lvPhones = (ListView) findViewById(R.id.lv_Contacts);
		lvPhones.setTextFilterEnabled(true);
		view = findViewById(R.id.seart_parent_ll);
		view.setVisibility(View.GONE);
		myView = (MyLetterView) findViewById(R.id.myView);
		letterTv = (TextView) findViewById(R.id.tvLetter);
		letterTv.setVisibility(View.GONE);
		myView.setOnTouchingLetterChangedListener(this);
	}

	// 控制键盘动作
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	// 销毁对象
	protected void onDestroy() {
		// TODO Auto-generated method stub
		lvPhones.setAdapter(null);  //释放资源
		mApplication.setHandler(null);
		arrayList = null;
		super.onDestroy();

	}

	//根据传入的字符获取所在最近联系人集合里面的位置
	public int alphaIndexer(String s) {
		int i = 0;
		for (; i < arrayList.size(); i++) { //遍历最近联系人数据集合
			String temp = arrayList.get(i).getPingyin(); //获取遍历的对象
			if (temp.startsWith(s.toLowerCase()) //如果首字母和传入的参数相等(小写转换)
					|| temp.startsWith(s.toUpperCase())) //如果首字母和传入的参数相等(大写转换)
				break;

		}
		return i;//查找到的索引
	}

	//文字改变的监听事件，当输入文字改变时设置显示内容的变化。
	@Override
	public void onTouchingLetterChanged(String s) {
		// TODO Auto-generated method stub
		if (alphaIndexer(s) > 0) {
			int position = alphaIndexer(s);  //获取拼音检索的位置
			lvPhones.setSelection(position);  //设置列表的显示区域
			letterTv.setText(s);             //设置拼音字母
			letterTv.setVisibility(View.VISIBLE); //设置可见
			letterHandler.removeCallbacks(overlayThread); //停止该线程
			letterHandler.postDelayed(overlayThread, 500); //延迟500毫秒执行
		}
	}

	// 申明一个句柄对象并且实例化
	private Handler letterHandler = new Handler() {
	};

	// 开启一个线程
	private class OverlayThread implements Runnable {

		public void run() {
			letterTv.setVisibility(View.GONE); //隐藏文字文本
		}

	}
}
