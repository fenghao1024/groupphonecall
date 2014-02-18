/**
 * Name : AllContacter.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.eteng.world;

import java.util.ArrayList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.eteng.world.MyLetterView.OnTouchingLetterChangedListener;
import com.wljsms.adapter.AllContacterAdapter;
import com.wljsms.info.ContactInfo;
import com.wljsms.util.Utils;

/**
 * 
 * com.eteng.world.AllContacter
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-19 下午2:12:22 Description :
 *         所有联系人类，所有联系人信息在项目启动时已通过Application预加载
 *         。通常情况下经如此页面时联系人以加载完毕，没加载完不时给吃提示对话框.
 *         预加载的好处是进入页面时数据已经加载完毕，直接使用，不用等待数据的加载。 Modified :
 */
public class AllContacter extends Activity implements
		OnTouchingLetterChangedListener {

	/**
	 * 联系人listview
	 */
	private ListView lvPhones;
	/**
	 * 底部显示选择的TEXTVIEW
	 */
	private TextView btnConfirm;
	/**
	 * 所有联系人适配器
	 */
	private AllContacterAdapter mAllContacterAdapter;
	/**
	 * 所有联系人数据列表，从Application中获得
	 */
	private ArrayList<ContactInfo> mAllContactInfoList = new ArrayList<ContactInfo>();
	/**
	 * 搜索框输入信息后过滤出的联系人列表，用于姓名，号码和联系人姓名首字母搜索，
	 * 没有输入搜索信息时内容为mAllContactInfoList中的联系人信息。
	 */
	private ArrayList<ContactInfo> mSearchFilterList = new ArrayList<ContactInfo>();
	/**
	 * Application对象
	 */
	private SmsApplication mApplication;

	// 以下3行用于侧边文字
	private MyLetterView myView;
	private TextView letterTv;
	private OverlayThread overlayThread = new OverlayThread();
	/**
	 * UI同步句柄
	 */
	private Handler handler;
	/**
	 * 提示等待对话框
	 */
	private ProgressDialog dialog;
	/**
	 * 重复先则联系人提示信息
	 */
	private String mToastItemStr;
	/**
	 * 搜索框
	 */
	private EditText mSearchEdit;

	/**
	 * 初始化UI
	 * 
	 */
	private void initUI() {
		btnConfirm = (TextView) findViewById(R.id.btn_SendSms);
		lvPhones = (ListView) findViewById(R.id.lv_Contacts);
		lvPhones.setTextFilterEnabled(true);
		mSearchEdit = (EditText) findViewById(R.id.all_contact_search_edittext);
		myView = (MyLetterView) findViewById(R.id.myView);
		letterTv = (TextView) findViewById(R.id.tvLetter);
		letterTv.setVisibility(View.GONE);
		myView.setOnTouchingLetterChangedListener(this);
	}

	/**
	 * 构造方法 savedInstanceState 要保存的实例句柄
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 提示文字信息
		mToastItemStr = getResources().getString(
				R.string.no_select_toast_item_str);
		initUI();

		mApplication = (SmsApplication) getApplication();

		/**
		 * 联系人监听事件
		 */
		lvPhones.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				// 取得点击项的联系人信息
				ContactInfo tmp = mSearchFilterList.get(arg2);
				if (tmp.isChecked()) {
					// 已经选择，设置为未选择，更新选择人数
					tmp.setChecked(false);
					mAllContacterAdapter.selectMinusOne();
					mApplication.removeContactInfo(tmp);
				} else {
					// 未选择
					if (mApplication.addContactInfo(tmp)) {
						// 添加联系人信息到选择列表成功，设置联系人为选中状态，更新选择人数
						tmp.setChecked(true);
						mAllContacterAdapter.selectAddOne();
					} else {
						// 添加到选择列表失败，即联系人已经选择了，给出提示信息
						Utils.showCustomToast(AllContacter.this, mToastItemStr);
						return;
					}

				}
				mAllContacterAdapter.notifyDataSetChanged();
			}
		});

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (msg.what == 1) {
					// 数据加载完成消息
					// 让等待对话框消失
					if (dialog != null && dialog.isShowing())
						dialog.dismiss();
					// 取得所有联系人数据信息
					mAllContactInfoList = mApplication.getArrayList();
					// 初始化选择的联系人数据
					mApplication.selectedContactsCount = mApplication
							.getmContactsList().size();
					// 设置底部显示的人数
					if (mApplication.selectedContactsCount > 0)
						btnConfirm.setText("确  定 ("
								+ mApplication.selectedContactsCount + ")");
					// 把所有联系人数据加到搜索过滤列表，开始没有输入搜索信息，过滤列表中为所有的联系人数据
					for (ContactInfo info : mAllContactInfoList)
						mSearchFilterList.add(info);
					// 初始化所有联系人适配器
					mAllContacterAdapter = new AllContacterAdapter(
							AllContacter.this, mSearchFilterList, btnConfirm,
							mApplication);
					// 设置适配器
					lvPhones.setAdapter(mAllContacterAdapter);
				}
			}
		};
		// 设置Application的handler
		mApplication.setHandler(handler);
		if (!mApplication.ismLoadContacterComplete()) {
			// 加载所有联系人数据没有完成，弹出等待对话框
			dialog = ProgressDialog.show(AllContacter.this, null,
					"正在加载联系人信息...");
			dialog.setCancelable(true);
		} else {
			// 所有联系人数据加载完成
			mAllContactInfoList = mApplication.getArrayList();
			
			if (mApplication.selectedContactsCount > 0)
				btnConfirm.setText("确  定 ("
						+ mApplication.selectedContactsCount + ")");
			for (ContactInfo info : mAllContactInfoList)
				mSearchFilterList.add(info);
			mAllContacterAdapter = new AllContacterAdapter(AllContacter.this,
					mSearchFilterList, btnConfirm, mApplication);
			lvPhones.setAdapter(mAllContacterAdapter);
		}

		//底部选择按钮单击事件
		btnConfirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String searchText = mSearchEdit.getText().toString();
				if (mApplication.getmContactsList().size() == 0 && !Utils.isPhoneNumber(searchText)) {
					// 没有选择联系人，且搜索框里输入的不是有效的手机号码，弹出提示信息
					Utils.showCustomToast(AllContacter.this, "请选择联系人!");
					return;
				}
				if(mSearchFilterList.size() == 0 && Utils.isPhoneNumber(searchText)){
					//在搜索框中输入了手机号码并且搜索过滤列表中没有该手机号对应的联系人信息，把该号码作为一个新的联系人信息，不保存联系人，但给该号码发短信
					mApplication.addNewContactInfo(mSearchEdit.getText().toString());
				}
				// 定义intent跳转到发通知页面
				Intent intent = new Intent();
				intent.setClass(AllContacter.this, SmsGroupSend.class);
				intent.putExtra("draft_str", mApplication.mDraft);
				intent.putExtra("groupDate", mApplication.getGroupDate());
				startActivity(intent);
				mAllContacterAdapter.notifyDataSetChanged();
				AllContacter.this.finish();
			}
		});

		/**
		 * 搜索监听事件
		 */
		mSearchEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				// 过滤联系人数据
				searchFilter(s.toString());
				// 让适配器更新数据
				mAllContacterAdapter.notifyDataSetChanged();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

		});
	}

	/**
	 * 控制句柄，用于侧边字母搜索
	 * 
	 */
	private Handler letterHandler = new Handler() {
	};

	/**
	 * 让提示字母消失的线程
	 */
	private class OverlayThread implements Runnable {

		public void run() {
			letterTv.setVisibility(View.GONE);
		}

	}

	/**
	 * 
	 * 字母检索
	 * 
	 * @param s
	 *            : 右侧边的文字
	 * @return 以s开头的项的索引
	 * 
	 */
	public int alphaIndexer(String s) {
		if (!mApplication.ismLoadContacterComplete())
			return -1;
		int i = -1, j = -1;
		for (ContactInfo o : mAllContactInfoList) {
			j++;
			String temp = o.getSortKey();
			if (temp.startsWith(s.toLowerCase())
					|| temp.startsWith(s.toUpperCase())) {
				i = j;
				break;
			}
		}
		return i;
	}

	/**
	 * 根据输入框中输入的内容过滤所有联系人列表，即只显示符合搜索结果的联系人
	 * 
	 * @param s
	 *            : 搜索框中输入的内容
	 */
	public void searchFilter(String s) {
		// 先清除过滤列表
		mSearchFilterList.clear();
		if (s == null || s.equals("")) {
			// 没有搜索内容，把所有联系人内容赋给搜索过滤列表
			for (ContactInfo info : mAllContactInfoList)
				mSearchFilterList.add(info);
			return;
		}
		if (Utils.isNumeric(s)) {
			// 输入的内容为纯数字，进行号码搜索
			// 遍历所有联系人信息，查找符合搜索条件的联系人信息
			for (ContactInfo info : mAllContactInfoList) {
				if (info.getPhone().startsWith(s)) {
					// 找到符合添加的一个联系人信息
					mSearchFilterList.add(info);
				}
			}
		} else if (Utils.isLetter(s)) {
			// 输入的内容为纯字母，进行联系人首字母搜索
			// 转换为大写字母，因为拼音首字母为大写
			s = s.toUpperCase();
			// 遍历所有联系人信息，查找符合搜索条件的联系人信息
			for (ContactInfo info : mAllContactInfoList) {
				if (info.getAlpha().startsWith(s)) {
					// 找到符合添加的一个联系人信息
					mSearchFilterList.add(info);
				}
			}
		} else {
			// 联系人姓名搜索
			// 遍历所有联系人信息，查找符合搜索条件的联系人信息
			for (ContactInfo info : mAllContactInfoList) {
				if (info.getName().startsWith(s)) {
					// 找到符合添加的一个联系人信息
					mSearchFilterList.add(info);
				}
			}
		}
	}

	/**
	 * 触控
	 * 
	 */
	@Override
	public void onTouchingLetterChanged(String s) {
		// TODO Auto-generated method stub
		if (alphaIndexer(s) > 0) {
			int position = alphaIndexer(s);
			lvPhones.setSelection(position);
			letterTv.setText(s);
			letterTv.setVisibility(View.VISIBLE);
			letterHandler.removeCallbacks(overlayThread);
			letterHandler.postDelayed(overlayThread, 500);
		}
	}

	/**
	 * 隐藏输入法
	 * 
	 * @param v
	 *            : 输入法依附的view
	 * 
	 */
	private void hideIM(View v) {
		// 隐藏输入法
		InputMethodManager imm = (InputMethodManager) AllContacter.this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	/**
	 * 控制键盘动作
	 * 
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			return false;
		return true;
	}

	/**
	 * 销毁对象
	 * 
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		lvPhones.setAdapter(null);
		mApplication.setHandler(null);
		mAllContactInfoList = null;
		hideIM(mSearchEdit);
		super.onDestroy();
	}

}