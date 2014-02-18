/**
 * Name : SendList.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.eteng.world;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.wljsms.adapter.SendAdapter;
import com.wljsms.database.MySqliteHelper;
import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ConstantsInfo;

/**
 * 
 * com.eteng.world.SendList
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-25 下午2:12:22 Description : 电话，短信回执信息处理类
 */

public class SendList extends Activity {

	/**
	 * 显示收件人清单的listview对象
	 */
	private ListView mSendListView;
	/**
	 * 收件人清单的listview对象的适配器对象
	 */
	private SendAdapter mSendAdapter;
	/**
	 * 数据库游标对象
	 */
	private Cursor mCursor;
	/**
	 * 数据集合
	 */
	private ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
	/**
	 * 数据库对象
	 */
	private MySqliteHelper database;
	/**
	 * 临时数据集合
	 */
	private ArrayList<HashMap<String, Object>> tempList = new ArrayList<HashMap<String, Object>>();
	/**
	 * 字符时间
	 */
	private String mDateStr;
	/**
	 * 通知发送回复情况
	 */
	private TextView mHintTv;
	/**
	 * 应答数
	 */
	private int mReplyCount;
	/**
	 * 通知内容
	 */
	private String mContent;
	/**
	 * 短信内容文本对象
	 */
	private TextView mContentTv;
	/**
	 * 通知收件人总数
	 */
	private int mSendcount;
	/**
	 * 剩余为回复数
	 */
	private int mSurplusCount;
	/**
	 * 通知发送日期
	 */
	private TextView mDateTv;
	/**
	 * 返回按钮对象
	 */
	private ImageView mBackImg;
	// 月，日。从通知类传入的字符串时间解析得到
	private int mMonth, mDay;
	/**
	 * 时，分时间，从通知类传入的字符串时间解析得到
	 */
	private String mTime = "";
	/**
	 * 加载数据是否完成标志：True：完成，False：未完成。此标志的作用是短时间内收到大量回复短信，如果前一次刷新数据未来完成，后面接着刷新数据，
	 * 会导致混乱和页面频繁跳动
	 */
	private boolean mLoadComplete = true;
	/**
	 * 运行标志
	 */
	private boolean mRun = true;
	
	private boolean mdel=false;
	
	private SmsApplication mApplication; // 应用对象

	/**
	 * 获取从通知类传入的数据
	 */
	private void getIntentData() {
		try {
			// 发送信息时间字符
			mDateStr = this.getIntent().getStringExtra("CreateDate").toString();
			// 回复数
			mReplyCount = Integer.parseInt(this.getIntent()
					.getStringExtra("replyCount").toString());
			// 短信内容
			mContent = this.getIntent().getStringExtra("contextMax").toString();
			// 收件人总数
			mSendcount = Integer.parseInt(this.getIntent()
					.getStringExtra("count").toString());
			// 剩余未回复人数
			mSurplusCount = Integer.parseInt(this.getIntent()
					.getStringExtra("surplusCount").toString());
			// 是否删除了通知
			mdel = this.getIntent().getBooleanExtra("del", false);
			if (mdel) updateMessageData();
			
			if (mDateStr != null && mDateStr.length() > 0) {

				// 取得月
				mMonth = Integer.parseInt(mDateStr.substring(5, 7));
				// 取得日
				mDay = Integer.parseInt(mDateStr.substring(8, 10));
				// 取得时间
				mTime = mDateStr.substring(11, 16);

			}
		} catch (Exception e) {
		}

	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receiver_info_list_layout);
		initUI();
		mApplication = (SmsApplication) getApplication();
		// 获取数据库对象
		database = mApplication.getDatabase();
		// 获取从通知类传入的数据
		getIntentData();
		registerRecv();
		// 设置通知内容
		mContentTv.setText(mContent);
		// 设置通知发送时间
		mDateTv.setText(mMonth + "月" + mDay + "日  " + mTime);
		setHintData();
		// 初始化适配器对象
		mSendAdapter = new SendAdapter(SendList.this, arrayList);
		mSendListView.setAdapter(mSendAdapter);
		// 返回按钮点击事件
		mBackImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mApplication.isShowLook()) {
					SendList.this.finish();
				} else {
					Intent intent = new Intent();
					intent.setClass(SendList.this, Look.class);
					startActivity(intent);
					SendList.this.finish();
				}
			}
		});
		// 启动异步任务加载数据
		new SmsTry().execute(null, null, null);

	}
	
	
	private void updateMessageData()
	{
		int i= database.updateIsShowEx();
		DebugFlags.EtengLog("##############################################通知已经被清除" + "清除数为" + "  " + i);
	}

 
	/**
	 * 设置页面头部的提示信息
	 */
	private void setHintData() {
		//已向15人发出通知，回复5人，余下10人暂未回复    通知15人，已成功通知15人
		String content;
		if(mSendcount == mReplyCount){
			//全部回复
			content = "通知" + mSendcount + "人，已成功通知<font color=\"#9fbe01\">"
					+ mReplyCount + "</font>人";
		}else {
			content = "已向" + mSendcount + "人发出通知，回复<font color=\"#9fbe01\">"
					+ mReplyCount + "</font>人，余下<font color=\"#e14812\">"
					+ mSurplusCount + "</font>人暂未回复";
		}
		mHintTv.setText(Html.fromHtml(content));
	}

	/**
	 * 初始化UI(从布局文件获取控件对象)
	 */
	private void initUI() {
		mBackImg = (ImageView) findViewById(R.id.receiver_info_back);
		mHintTv = (TextView) findViewById(R.id.receiver_info_count);
		mContentTv = (TextView) findViewById(R.id.receiver_info_msg);
		mDateTv = (TextView) findViewById(R.id.receiver_info_date);
		mSendListView = (ListView) findViewById(R.id.receiver_info_listview);
	}

	/**
	 * 把临时数据转换为UI同步数据
	 */
	private void addElements(ArrayList<HashMap<String, Object>> src) {
		for (int i = 0; i < src.size(); i++)
			// 遍历临时详细列表数据集合
			arrayList.add(src.get(i)); // 把对象添加到详细列表同步到UI
		src.clear(); // 清除临时列表集合
	}

	/**
	 * 获取详细列表数据到详细列表数据集合
	 */
	private void getBaseValue(Cursor mCursor) {
		String ReplyContent = mCursor.getString(mCursor
				.getColumnIndex(MySqliteHelper.ReplyContext)); // 回复内容

		String user = mCursor.getString(mCursor // 联系人姓名
				.getColumnIndex(MySqliteHelper.User));

		String phone = mCursor.getString(mCursor // 电话号码
				.getColumnIndex(MySqliteHelper.Phone));

		String sendState = mCursor.getString(mCursor // 发送状态
				.getColumnIndex(MySqliteHelper.SendState));

		String reply = mCursor.getString(mCursor // 应答状态
				.getColumnIndex(MySqliteHelper.Reply));

		String CreateDate = mCursor.getString(mCursor // 发送时间
				.getColumnIndex(MySqliteHelper.CreateDate));

		String CallOKOnce = mCursor.getString(mCursor // 呼叫成功次数
				.getColumnIndex(MySqliteHelper.CallOKOnce));

		HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
		tempHashMap.put("userName", user);
		tempHashMap.put("ReplyContent", ReplyContent);
		tempHashMap.put("SendState", sendState);
		tempHashMap.put("Reply", reply);
		if (reply != null && reply.equals("1"))
			mReplyCount++;
		tempHashMap.put("Phone", phone);
		tempHashMap.put("CreateDate", CreateDate);
		tempHashMap.put("CallOKOnce", CallOKOnce);
		tempList.add(tempHashMap); // 放入哈希表
	}

	// 广播接收器
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(ConstantsInfo.AFTER_RECEIVED_SMS_BROADCAST)){
				//收到了回复短信广播，先判断回复短信号码是否在改通知任务中，不在的话，不刷新数据
				String phone = intent.getStringExtra(ConstantsInfo.PHONE_NUM);
				DebugFlags.EtengLog("收到回复的电话号码为：" + phone);
				//遍历列表，查看是否有该号码
				for(HashMap<String, Object> map : arrayList){
					if(map.get("Phone") != null && map.get("Phone").toString().equals(phone) && mLoadComplete){
						// 启动异步任务加载数据
						mLoadComplete = false;
						new SmsTry().execute(null, null, null);
						//退出循环
						break;
					}
				}
			}
		}

	};
	
	/**
	 * Description:注册要接收的广播
	 * 
	 */
	private void registerRecv() {
		IntentFilter filter = new IntentFilter();
		// 注册收到短信广播
		filter.addAction(ConstantsInfo.AFTER_RECEIVED_SMS_BROADCAST);

		registerReceiver(receiver, filter);
	}

	// 异步任务，从数据库中加载数据
	class SmsTry extends AsyncTask<String, String, Double> {

		@Override
		protected void onPostExecute(Double result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mSurplusCount = mSendcount - mReplyCount;
			setHintData();
			arrayList.clear();
			addElements(tempList);
			mSendAdapter.notifyDataSetChanged();
			mLoadComplete = true;
		}

		@Override
		protected Double doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			try {
				// 数据访问方法获取运算数据
				mCursor = database.getReplyList(mDateStr);
				if (mCursor == null)
					return null;
				mReplyCount = 0;
				// 遍历数据
				while (mCursor.moveToNext() && mRun) {
					getBaseValue(mCursor);
				}
				mCursor.close(); // 关闭数据游标
			} catch (Exception e) {
				// TODO: handle exception
				if (mCursor != null)
					mCursor.close();
			}
			return null;
		}

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mSendListView.setAdapter(null);
		arrayList.clear();
		tempList.clear();
		mRun = false;
		this.unregisterReceiver(receiver);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			if (mApplication.isShowLook()) {
				SendList.this.finish();
			} else {
				Intent intent = new Intent();
				intent.setClass(SendList.this, Look.class);
				startActivity(intent);
				SendList.this.finish();
			}
			 //这里不需要执行父类的点击事件，所以直接return
			return true;

		}
		//继续执行父类的其他点击事件
		return super.onKeyDown(keyCode, event);
	}

}
