/**
 * Name : SmsConfirmActiv.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.eteng.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.wljsms.adapter.SmsConfirmGvAdapter;
import com.wljsms.database.MySqliteHelper;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.info.ContactInfo;
import com.wljsms.info.GroupInfo;
import com.wljsms.sms.service.SmsSendingService;
import com.wljsms.util.Utils;

/**
 * com.eteng.world.SmsConfirmActiv
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-20 下午8:08:19 Description : 发短信确认页面 Modified :
 */
public class SmsConfirmActiv extends Activity {

	/**
	 * GridView对象
	 */
	private GridView mGridview;
	/**
	 * 收件人数量
	 */
	private TextView mContactsCount;
	/**
	 * GridView适配器
	 */
	private SmsConfirmGvAdapter mGridAdapter;
	/**
	 * 短信内容TEXTVIEW
	 */
	private TextView mSmsContent;
	// 发送和修改TEXTVIEW
	private TextView mConfirmSend, mConfirmModify;
	// 选择不再提示确认和返回IMAGEVIEW
	private ImageView mCheckIv, mSmsConfirmBack;
	/**
	 * 向多少人发送短信提示TEXTVIEW
	 */
	private TextView mReceiverCount;
	/**
	 * 短信条数TEXTVIEW
	 */
	private TextView mSmsCountTv;
	/**
	 * 短信内容
	 */
	private String content;
	private Resources res;
	/**
	 * 应用对象
	 */
	private SmsApplication mApplication;
	private SharedPreferences sp;
	// 不再显示确认页面IMAGEVIEW选中和未选中图片
	private Drawable mCheckedImg, mUnCheckedImg;
	/**
	 * 选择状态
	 */
	private boolean mChecked = false;
	/**
	 * 数据库对象
	 */
	private MySqliteHelper database;
	/**
	 * 通知创建日期
	 */
	private String groupDate;
	/**
	 * 短信数量
	 */
	private int mSmsCount;
	/**
	 * 短信字数
	 */
	private int mWordNum;
	/**
	 * 已选择联系人信息
	 */
	private List<ContactInfo> mSelectedContactsList = new ArrayList<ContactInfo>();
	/**
	 * 后台正在发送短信过程中上一次点击发送按钮的时间
	 */
	private long mLastSendingClickedTime;
	/**
	 * 判断是否以草稿箱的方式进入
	 */
	private boolean mIsDraft = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sms_confirm_layout);
		mApplication = (SmsApplication) getApplication();
		mLastSendingClickedTime = System.currentTimeMillis();
		database = mApplication.getDatabase();
		mIsDraft = this.getIntent().getBooleanExtra("draft", false);
		sp = getSharedPreferences(ConstantsInfo.SHARED_FILE_NAME, 0);
		res = getResources();
		content = getIntent().getStringExtra("sms_content");
		groupDate = getIntent().getStringExtra("groupDate");
		mSmsCount = getIntent().getIntExtra("sms_count", -1);
		mWordNum = getIntent().getIntExtra("word_number", -1);
		initUIView();
		for (ContactInfo info : mApplication.getmContactsList())
			mSelectedContactsList.add(info);
		for (ContactInfo info : mApplication.getmNewContactsList())
			mSelectedContactsList.add(info);
		mGridAdapter = new SmsConfirmGvAdapter(this, mSelectedContactsList);
		mGridview.setAdapter(mGridAdapter);

		mContactsCount.setText("(" + mSelectedContactsList.size() + ")");
		String text = "亲，你将向<font color=\"#781818\">("
				+ Integer.toString(mSelectedContactsList.size())
				+ ")</font>人发通知并产生短信费。记得查看通知详细进度，时刻掌握通知状态。";
		// 设置收件人的颜色
		mReceiverCount.setText(Html.fromHtml(text));
		mSmsContent.setText(content);
	}

	/**
	 * 初始化UI
	 */
	private void initUIView() {
		mCheckIv = (ImageView) findViewById(R.id.sms_confirm_check);
		mSmsConfirmBack = (ImageView) findViewById(R.id.sms_confirm_back);
		mReceiverCount = (TextView) findViewById(R.id.sms_tips_count);
		mGridview = (GridView) findViewById(R.id.sms_confirm_gridview);
		mConfirmModify = (TextView) findViewById(R.id.sms_confirm_modify);
		mConfirmSend = (TextView) findViewById(R.id.sms_confirm_send);
		mContactsCount = (TextView) findViewById(R.id.sms_confirm_count);
		mSmsContent = (TextView) findViewById(R.id.sms_confirm_content);
		mSmsCountTv = (TextView) findViewById(R.id.sms_count);
		if (mSmsCount != -1 && mWordNum != -1)
			mSmsCountTv.setText(mWordNum + "/" + mSmsCount);

		mConfirmSend.setOnClickListener(listener);
		mConfirmModify.setOnClickListener(listener);
		mCheckIv.setOnClickListener(listener);
		mSmsConfirmBack.setOnClickListener(listener);

		mCheckedImg = res.getDrawable(R.drawable.sms_confirm_select);
		mUnCheckedImg = res.getDrawable(R.drawable.sms_confirm_no_selected);
	}

	/**
	 * 发送短信
	 */
	private void SendSms() {

		if (mApplication.getImsi() == null || mApplication.getImsi().equals("")) {
			Utils.showCustomToast(SmsConfirmActiv.this, "检测不到手机卡，无法发通知");
			StringBuffer sb = new StringBuffer();
			// 取得被通知人的号码清单
			for (ContactInfo info : mSelectedContactsList) {
				sb.append(info.getPhone());
				sb.append(",");
			}
			String phones = sb.toString();
			// 去掉最后一个逗号
			phones = phones.substring(0, phones.length() - 1);
			// 插入一条错误记录
			Utils.insertSmsSendFailedErrorLog(database, "", "50006", phones,
					Utils.formatDate(System.currentTimeMillis()));
			return;
		}

		if (!mApplication.ismSendingSmsComplete()
				|| System.currentTimeMillis()
						- mApplication.getmSmsSendedTime() < 4 * 1000L) {
			if (System.currentTimeMillis() - mLastSendingClickedTime > 4 * 1000L) {
				mLastSendingClickedTime = System.currentTimeMillis();
				Utils.showCustomToast(SmsConfirmActiv.this, "后台正在发送短信，请稍后重试！");
			}
			return;
		}

		// 发送前情况草稿箱里的数据,调用删除草稿箱代码
		if (mIsDraft)
			database.deleteNotify(groupDate);
 
		// 把数据插入到急短信数据库
		long time = System.currentTimeMillis();
		String mDateStr = Utils.formatDate(time);
		String time_str = Long.toString(time);
		String taskId = mApplication.getImsi() + time_str;
		List<ContactInfo> tmpData = new ArrayList<ContactInfo>();
		database.getWritableDatabase().beginTransaction();
		for (ContactInfo info : mSelectedContactsList) {
			ContactInfo tmp = new ContactInfo();
			tmp.setName(info.getName());
			tmp.setPhone(info.getPhone());
			tmpData.add(tmp);
			insertDataToDB(info.getName(), info.getPhone(), content, "1",
					mDateStr, time_str, taskId);
		}
		database.getWritableDatabase().setTransactionSuccessful();
		database.getWritableDatabase().endTransaction();
		// 发送发通知广播，让通知页面刷新数据
		sendBroadcast(new Intent(ConstantsInfo.SMS_TASK_SENDED_BROADCAST));
		// 启动发短信服务
		Intent iii = new Intent();
		Bundle bundle1 = new Bundle();
		bundle1.putSerializable(ConstantsInfo.SMS_SEND_CONTACT_LIST,
				(Serializable) tmpData);
		iii.putExtra(ConstantsInfo.TIME, time);
		iii.putExtra(ConstantsInfo.DATE_STR, mDateStr);
		iii.setClass(SmsConfirmActiv.this, SmsSendingService.class);
		iii.putExtras(bundle1);
		iii.putExtra(ConstantsInfo.SMS_CONTENT, content);
		startService(iii);
		setChecked(mChecked);
		clearCheckedData();
		mApplication.loadRecentContacter();
		SmsConfirmActiv.this.finish();

	}

	// 插入数据到数据库
	private void insertDataToDB(String name, String phone, String content,
			String status, String date_str, String time_str, String taskId) {
		database.insertData(name, phone, content, status, "0", date_str, "",
				date_str, "0", time_str, taskId, "0");
	}

	/**
	 * 把选择的联系人的信息清零
	 */
	public void clearCheckedData() {
		List<ContactInfo> mContacts;
		mContacts = mApplication.getArrayList();
		for (ContactInfo ci : mContacts)
			ci.setChecked(false);
		for (GroupInfo gi : mApplication.getmGroups()) {
			gi.setAllChecked(false);
			gi.setSomeChecked(false);
			gi.setExpandable(false);
			mContacts = gi.getChilds();
			for (ContactInfo ci : mContacts) {
				ci.setChecked(false);
			}
		}
		mContacts = mApplication.getmRecentContactList();
		for (ContactInfo ci : mApplication.getmRecentContactList())
			ci.setChecked(false);
		mApplication.getmContactsList().clear();
		mApplication.getmNewContactsList().clear();
		mSelectedContactsList.clear();
		mApplication.selectedContactsCount = 0;

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 * 监听单击事件
	 */
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.sms_confirm_check) {
				if (mChecked) {
					// 从选择状态变为为选中状态
					mChecked = false;
					mCheckIv.setBackgroundDrawable(mUnCheckedImg);
				} else {
					mChecked = true;
					mCheckIv.setBackgroundDrawable(mCheckedImg);
				}
			} else if (v.getId() == R.id.sms_confirm_send) {
				// 发送
				mApplication.setDialog(false);
				mApplication.setPost(false);
				// 发送
				SendSms();
			} else if (v.getId() == R.id.sms_confirm_modify) {
				// 修改
				exitToSmsGroupSend();

			} else if (v.getId() == R.id.sms_confirm_back) {
				// 退出
				exitToSmsGroupSend();
			}
		}

	};

	/**
	 * 返回到发短信页面
	 */
	public void exitToSmsGroupSend() {
		Intent intent = new Intent();
		intent.setClass(SmsConfirmActiv.this, SmsGroupSend.class);
		intent.putExtra("modify", true);
		intent.putExtra("groupDate", groupDate);
		intent.putExtra("draft_data", content);
		startActivity(intent);
		SmsConfirmActiv.this.finish();
	}

	/**
	 * 键盘控制
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		exitToSmsGroupSend();
		return true;
	}

	/**
	 * 设置不再显示确认页面的状态
	 * 
	 * @param check
	 *            ： 选中的状态
	 */
	private void setChecked(boolean check) {
		sp.edit().putBoolean(ConstantsInfo.CHECKED, check).commit();
	}

}
