/**
 * Name : SendSmsDialogService.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.sms.service;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import com.eteng.world.SmsApplication;
import com.wljsms.database.MySqliteHelper;
import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.info.SendedSmsInfo;
import com.wljsms.info.SendedSmsThreadInfo;
import com.wljsms.util.Utils;
import com.wljsms.webservice.SetDialogBoxInfo;
import com.wljsms.webservice.SetMatchInfo;

/**
 * com.eteng.sendsmsdialog.service.SendSmsDialogService
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-4-1 下午3:17:19 Description :
 *         监听发短信弹出对话框服务，此服务设置为打开软件启动，采取监听系统短信数据库变化的方式监听系统发短信行为 Modified :
 */
public class SendSmsDialogService extends Service {
	/**
	 * 服务启动时发件箱中短信数量，随发送短信和删除已发短信而变。
	 */
	private int mSendCount = 0;
	/**
	 * 监听系统短信数据库的变化
	 */
	private SmsSendObserver observer;
	/**
	 * 弹出对话框
	 */
	private AlertDialog dialog;
	/**
	 * 最近一条发送短信信息
	 */
	private SendedSmsInfo mLastSms = new SendedSmsInfo();
	/**
	 * mLastSms属于的会话信息
	 */
	private SendedSmsThreadInfo mLastSmsThread;
	/**
	 * 短信会话信息列表
	 */
	private List<SendedSmsThreadInfo> mThreadInfoList = new ArrayList<SendedSmsThreadInfo>();
	/**
	 * 应用的application对象
	 */
	private SmsApplication mApplication;
	/**
	 * 数据库操作对象
	 */
	private MySqliteHelper database;
	/**
	 * 消息处理handler
	 */
	private Handler handler;
	/**
	 * imsi号，从Application对象中获得
	 */
	private String imsi;
	/**
	 * 匹配结果，即弹出提示对话框的匹配结果，每次弹出对话框会重新赋值，主要用于保存记录到对话框操作库
	 */
	private StringBuffer mMatchResult;
	/**
	 * 用户是否点击了弹出的提示对话框的按钮
	 */
	private boolean mClicked = false;
	/**
	 * 锁屏判断
	 */
	private KeyguardManager mKeyguardManager;
	/**
	 * 是否匹配
	 */
	private boolean isMatch = false;
	/**
	 * 序号
	 */
	private String serial;

	/**
	 * 提交本地数据的服务接口类
	 */
	private SetDialogBoxInfo dialogBoxLog;
	/**
	 * 后台服务线程，用于提交本地数据到服务接口
	 */
	private DialogBoxTry dialogBoxTry;
	/**
	 * 分别为类容，状态，时间
	 */
	private String content, status, clientdate;
	/**
	 * 网络连接管理器
	 */
	private ConnectivityManager connectivityManager;
	/**
	 * 网络对象
	 */
	private NetworkInfo info;
	/**
	 * 数据游标对象
	 */
	private Cursor cur;
	/**
	 * 是否联网
	 */
	private Boolean isNet;
	@SuppressWarnings("unused")
	/**
	 * 发送内容
	 */
	private String sendContent;

	/**
	 * 网络广播接收器，监听网络状态的改变
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				DebugFlags.EtengLog("网络状态已经改变");
				connectivityManager = (ConnectivityManager)

				getSystemService(Context.CONNECTIVITY_SERVICE);
				info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					String name = info.getTypeName();
					DebugFlags.EtengLog("当前网络名称：" + name);
					// 调用提交本地数据到服务接口的方法
					DebugFlags.EtengLog("调用提交本地数据到服务接口的方法");
					isNet = true;
					postLogInfo();
				} else {
					DebugFlags.EtengLog("没有可用网络");
					isNet = false;
				}
			}
		}
	};

	/**
	 * 调用提交本地数据到服务接口
	 */
	private void postLogInfo() {
		dialogBoxTry = new DialogBoxTry();
		dialogBoxTry.execute(null, null);
	}

	/**
	 * 提交弹窗操作数据到后台
	 */
	private void getDialogOp() {
		dialogBoxLog = new SetDialogBoxInfo();
		StringBuffer sb = new StringBuffer();
		try {
			cur = database.getDialogOp();
			if (cur != null) {
				// 如果获取的记录数不为0
				if (cur.getCount() != 0) {
					while (cur.moveToNext()) {
						// 拼接提交数据格式
						String DIALOG_SERIAL_NUMBER = cur
								.getString(cur
										.getColumnIndex(MySqliteHelper.DIALOG_SERIAL_NUMBER));
						String imsi = cur.getString(cur.getColumnIndex("imsi"));
						String DIALOG_CONTENT = cur.getString(cur
								.getColumnIndex(MySqliteHelper.DIALOG_CONTENT));
						String DIALOG_OP = cur.getString(cur
								.getColumnIndex(MySqliteHelper.DIALOG_OP));
						String DIALOG_DATE = cur.getString(cur
								.getColumnIndex(MySqliteHelper.DIALOG_DATE));
						sb.append(DIALOG_SERIAL_NUMBER + "|" + imsi + "|"
								+ DIALOG_CONTENT + "|" + DIALOG_OP + "|"
								+ DIALOG_DATE);

						sb.append(";");
					}
				}
				// 如果本地有有数据，提交到服务接口
				if (sb.length() > 0) {
					String strParam = sb.toString().substring(0,
							sb.toString().length() - 1);
					Boolean isNone = dialogBoxLog.init(strParam, null);
					// 如果提交完成，清除本地数据库
					if (isNone) {
						database.deleteDialogOpt();
					}
				}
			}
			// 关闭时间游标
			cur.close();
		} catch (Exception e) {
			// TODO: handle exception
			cur.close();
		}
	}

	/**
	 * 后台运行的线程，提交数据到后台服务接口
	 */
	class DialogBoxTry extends AsyncTask<String, String, Double> {

		@Override
		protected void onPostExecute(Double result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

		}

		@Override
		protected Double doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			getDialogOp(); // 获取本地操作数据并且提交
			getMatched(); // 获取本地匹配数据并且提交
			return null;
		}

	}

	/**
	 * 提交匹配数据到后台
	 */
	private void getMatched() {
		try {
			cur = database.getMatched();  //获取本地匹配数据
			SetMatchInfo setMatchInfo = new SetMatchInfo();
			StringBuffer sb = new StringBuffer();
			if (cur != null) {
				if (cur.getCount() != 0) {
					// 如果获取的记录数不为0
					while (cur.moveToNext()) {
						// 拼接提交数据格式
						String MATCHED_SERIAL_NUMBER = cur
								.getString(cur
										.getColumnIndex(MySqliteHelper.MATCHED_SERIAL_NUMBER));
						String imsi = cur.getString(cur.getColumnIndex("imsi"));
						String MATCHED_CONTENT = cur
								.getString(cur
										.getColumnIndex(MySqliteHelper.MATCHED_CONTENT));
						String MATCHED_DIALOG_STATUS = cur
								.getString(cur
										.getColumnIndex(MySqliteHelper.MATCHED_DIALOG_STATUS));
						String MATCHED_DATE = cur.getString(cur
								.getColumnIndex(MySqliteHelper.MATCHED_DATE));
						sb.append(MATCHED_SERIAL_NUMBER + "|" + imsi + "|"
								+ MATCHED_CONTENT + "|" + MATCHED_DIALOG_STATUS
								+ "|" + MATCHED_DATE);
						sb.append(";");
					}
				}
				// 如果本地有有数据，提交到服务接口
				if (sb.length() > 0) {
					String strParam = sb.toString().substring(0,
							sb.toString().length() - 1);
					Boolean isNone = setMatchInfo.init(strParam, null);
					// 如果提交完成，清除本地数据库
					if (isNone) {
						database.deleteMatchedOpt();
					}
				}
			}
			// 关闭时间游标
			cur.close();
		} catch (Exception e) {
			// TODO: handle exception
			// 关闭时间游标
			cur.close();
		}
	}

	@Override
	public void onCreate() {
		mApplication = (SmsApplication) getApplication();
		// 取得数据库对象
		database = mApplication.getDatabase();
		registerRecv();
		imsi = mApplication.getImsi();
		if (imsi == null || imsi.equals("")) {
			// imsi号为空，再取一次
			imsi = ((TelephonyManager) this
					.getSystemService(Context.TELEPHONY_SERVICE))
					.getSubscriberId();
			if (imsi != null && !imsi.equals(""))
				mApplication.setImsi(imsi);
		}

		mApplication.loadBlackList();
		mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		// 取得系统发件箱中的短信数量
		Cursor cur = getContentResolver().query(
				Uri.parse(ConstantsInfo.SMS_URI_SEND), null, null, null,
				"date desc");
		if (cur != null) {
			// 取得已发送短信数
			mSendCount = cur.getCount();
			// 取得最近一条发送短信的时间
			cur.close();
		}
		DebugFlags.EtengLog("发件箱中的短信数量为：" + mSendCount);
		// 注册数据库监听器
		observer = new SmsSendObserver(new Handler());
		this.getContentResolver().registerContentObserver(
				Uri.parse(ConstantsInfo.SMS_URI_ALL), true, observer);

		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mReceiver, mFilter);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (msg.what == 1) {
					// 找到对应的会话信息，弹出对话框消息
					SendedSmsInfo sendedSmsInfo = (SendedSmsInfo) msg.obj;
					DebugFlags.EtengLog("#############id ： "
							+ sendedSmsInfo.getId() + "mThreadInfoList.size : "
							+ mThreadInfoList.size());
					if (mThreadInfoList == null)
						return;
					for (SendedSmsThreadInfo threadInfo : mThreadInfoList) {
						if (threadInfo.getId().equals(sendedSmsInfo.getId())) {
							threadInfo.setmMatchingComplete(true);
							threadInfo.setmMatched(true);
							StringBuffer sb = new StringBuffer();
							boolean mDialogShowing = checkDialogShowEnv(sb,
									sendedSmsInfo);
							if (mDialogShowing && isMatch)
								showDialog(threadInfo);
							else
								threadInfo.setmMatched(false);
							break;
						}
					}
				} else if (msg.what == 2) {
					// 比对不成功，从会话列表中删除对应的会话信息
					SendedSmsInfo sendedSmsInfo = (SendedSmsInfo) msg.obj;
					if (mThreadInfoList == null)
						return;
					for (SendedSmsThreadInfo threadInfo : mThreadInfoList) {
						if (threadInfo.getId().equals(sendedSmsInfo.getId())) {
							threadInfo.setmMatched(false);
							threadInfo.cancel();
							threadInfo.setmMatchingComplete(true);
							threadInfo.setDeletable(true);
							break;
						}
					}
					// 清楚会话列表中可删除的对象，保证会话列表里的数据项不会太多，提高遍历数度
					int len = mThreadInfoList.size();
					for (int i = 0; i < len; i++) {
						if (mThreadInfoList.get(i).isDeletable()) {
							// 找到短信定时器对象，取消任务，删除，不再查找
							mThreadInfoList.get(i).getmSmsList().clear();
							mThreadInfoList.remove(i);
							i--;
							len--;
						}
					}
					DebugFlags.EtengLog("会话列表大小为：" + mThreadInfoList.size());
				}
			}

		};
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		DebugFlags.EtengLog("SendSmsDialogService服务启动");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 弹出框确定事件
	 * @param threadInfo: 已发送短信信息类
	 */
	private void PositiveButton(SendedSmsThreadInfo threadInfo) {
		mClicked = true; // 设置点击了按钮
		dialog.dismiss();
		dialog = null;
		// 插入数据到对话框操作库中
		serial = ConstantsInfo.SMS_DIALOG_SHOW
				+ ConstantsInfo.DIALOG_CLICK_CONFIRM;
		content = mMatchResult.toString() + ",用户点击确定";
		status = "1";
		DebugFlags.EtengLog("##############################点击确定按钮 "
				+ threadInfo.getId());
		threadInfo.setmClickedPositiveBtn(true);
		clientdate = Utils.formatDate(System.currentTimeMillis());
		database.insertDialogOpInfo(serial, clientdate, content, status, imsi);
		if (isNet) // 如果网络存在
			postLogInfo(); // 提交数据到服务

	}

	/**
	 * 弹出框取消事件
	 * @param threadInfo: 已发送短信信息类
	 */
	private void NegativeButton(SendedSmsThreadInfo threadInfo) {
		mClicked = true;
		dialog.dismiss();
		dialog = null;
		threadInfo.setmClickedPositiveBtn(false);
		DebugFlags.EtengLog("##############################点击取消按钮 "
				+ threadInfo.getId());
		if (mMatchResult != null) // 用户点击取消
			database.insertDialogOpInfo(ConstantsInfo.SMS_DIALOG_SHOW
					+ ConstantsInfo.DIALOG_CLICK_CANCEL,
					Utils.formatDate(System.currentTimeMillis()),
					mMatchResult.toString() + ",用户点击取消", "2", imsi);
		if (isNet) // 如果网络存在
			postLogInfo(); // 提交数据到服务接口

	}

	/**
	 * 弹出系统对话框，内容为发送的短信内容
	 * @param threadInfo: 已发送短信信息类
	 */
	private void showDialog(final SendedSmsThreadInfo threadInfo) {

		if (dialog != null && dialog.isShowing()) {
			// 已经弹出了对话框，直接返回
			DebugFlags.EtengLog("对话框已经弹出，直接返回，保存日志到数据库中");
			if (mMatchResult != null)
				database.insertDialogOpInfo(ConstantsInfo.SMS_DIALOG_SHOW
						+ ConstantsInfo.DIALOG_SHOWED,
						Utils.formatDate(System.currentTimeMillis()),
						mMatchResult.toString() + ",屏幕上已弹出对话框", "4", imsi);
			return;
		}
		// 如果弹出对话框为空
		if (dialog == null) {
			AlertDialog.Builder builder = new Builder(SendSmsDialogService.this);
			builder.setTitle("消息提示");
			builder.setMessage("短信已发送，是否需要对方尽快回复?");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// 点击了确定
							PositiveButton(threadInfo);
						}

					});

			builder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// 点击了取消
							NegativeButton(threadInfo);
						}
					});
			dialog = builder.create();
			// 设置对话框消失时执行的代码，用于判断是超时自动消失还是用户点击按钮消失
			dialog.setOnDismissListener(new OnDismissListener() {

				public void onDismiss(DialogInterface dialog) {
					// TODO Auto-generated method stub

					if (mClicked) {
						DebugFlags.EtengLog("对话框消失，用户点击了按钮");
						mClicked = false;
					} else {
						mApplication.setPost(false);
						if (mMatchResult != null) { // 如果没有匹配成功
							database.insertDialogOpInfo(
									// 插入匹配日志
									ConstantsInfo.SMS_DIALOG_SHOW
											+ ConstantsInfo.DIALOG_TIMEOUT,
									Utils.formatDate(System.currentTimeMillis()),
									mMatchResult.toString(), "3", imsi);
							if (isNet) // 如果有网络
								postLogInfo(); // 提交数据到服务接口
						}
					}

				}

			});
		}
		// 设置不能按下取消键
		dialog.setCancelable(false);
		// 设置允许对话框在服务里弹出
		dialog.getWindow()
				.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		mApplication.setDialog(true);
		DebugFlags.EtengLog("调用对话框弹出方法 dialog.show(),时间："
				+ Utils.formatDate(System.currentTimeMillis()));
		dialog.show();
		// 设置不能按下Home键
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	}

	/**
	 * 广播接收器，接收注册的广播，根据广播标识做相应的处理
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// 删除可删除的对象
			if (intent.getAction().equals(ConstantsInfo.DELETE_SMS_THREAD_INFO)) {
				int len = mThreadInfoList.size();
				for (int i = 0; i < len; i++) {
					if (mThreadInfoList.get(i).isDeletable()) {
						// 找到短信定时器对象，取消任务，删除，不再查找
						mThreadInfoList.get(i).getmSmsList().clear();
						mThreadInfoList.remove(i);
						i--;
						len--;
					}
				}
				DebugFlags.EtengLog("会话列表大小为：" + mThreadInfoList.size());
			}
		}

	};

	/**
	 * 注册收到短信广播接收器
	 */
	private void registerRecv() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConstantsInfo.DELETE_SMS_THREAD_INFO);
		registerReceiver(receiver, filter);
	}

	// 数据库监听对象，监听系统发短信行为
	private class SmsSendObserver extends ContentObserver {

		private Cursor mCursor;

		public SmsSendObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			// 取得发件箱的cursor对象

			mCursor = SendSmsDialogService.this.getContentResolver().query(
					Uri.parse(ConstantsInfo.SMS_URI_SEND),
					new String[] { "_id", "address", "body", "date" }, null,
					null, "_id desc");

			if (mCursor == null)
				return;

			if (mCursor.getCount() != mSendCount) {
				// 发件箱短信数量有变化
				DebugFlags.EtengLog("发件箱数量有变化");
				if (!mApplication.isDialog()) {
					// 急短信正在发短信，不进行内容的监控，直接返回
					DebugFlags.EtengLog("急短信正在发送短信，不监控，直接返回");
					// 重新设置发件箱数量
					mSendCount = mCursor.getCount();
					return;
				} else {
					DebugFlags.EtengLog("非急短信正在发送短信，监控");
				}
				if (mCursor.getCount() > mSendCount) {
					// 发件箱中短信数量增加，即发短信成功
					DebugFlags.EtengLog("发件箱中短信增加，启动一个线程更新今天已发送短信数");
					// 重新设置发件箱数量
					mSendCount = mCursor.getCount();
					
					if (mCursor.moveToFirst()) {
						// 从短信库中取得_id最大的联系人信息，此信息为最近插入的，即刚发送的短信
						SendedSmsInfo info = new SendedSmsInfo();
						info.setName("");
						// 获取电话号码
						info.setPhone(mCursor.getString(mCursor
								.getColumnIndex("address")));
						// 获取短信内容
						info.setBody(mCursor.getString(mCursor
								.getColumnIndex("body")));
						info.setDate(mCursor.getLong(mCursor
								.getColumnIndex("date")));
						DebugFlags.EtengLog("最近一条短信信息,号码：：" + info.getPhone()
								+ ",内容 ：" + info.getBody() + ",短信发送时间："
								+ Utils.formatDate(info.getDate()) + ",当前时间："
								+ Utils.formatDate(System.currentTimeMillis()));

						// 判断是否群发的短信，内容相同，时间在20秒以内，认为是通一个短信会话，即群发的短信，对于群发的短信，之比对一次，这样可以快书的弹出提示对话框
						if (info.getBody().equals(mLastSms.getBody())
								&& (info.getDate() - mLastSms.getDate() < ConstantsInfo.SMS_DELAY * 1000 && info
										.getDate() - mLastSms.getDate() > -ConstantsInfo.SMS_DELAY * 1000)) {
							// 同一个会话的短信，只比对一次，把短信信息加入已发短信列表
							DebugFlags
									.EtengLog("###########################收到同一会话短信信息");
							info.setId(mLastSms.getId());
							mLastSms = info;
							mLastSmsThread.add(info);
							// 匹配成功的会话，重新初始化提交数据任务
							if (mLastSmsThread.ismMatched())
								mLastSmsThread.init();
						} else {
							// 新的短信会话，比对内容，弹出提示对话框操作
							info.setId(mApplication.getImsi() + info.getDate());
							mLastSms = info;
							mLastSmsThread = new SendedSmsThreadInfo();
							mLastSmsThread.setId(info.getId());
							mLastSmsThread.setImsi(imsi);
							mLastSmsThread.setDatabase(database);
							mLastSmsThread
									.setContext(SendSmsDialogService.this);
							mLastSmsThread.setApplication(mApplication);
							mLastSmsThread.add(info);
							mThreadInfoList.add(mLastSmsThread);
							mLastSmsThread.init();
							DebugFlags
									.EtengLog("################################收到新会话短信信息,id 为： "
											+ info.getId());
							processSendedSms(info);
						}
					}

				} else {
					// 重新设置发件箱数量
					mSendCount = mCursor.getCount();
					DebugFlags.EtengLog("发件箱中短信减少，应该删除了短信,重新初始化最近一条发件信息");
					mLastSms = new SendedSmsInfo();
				}

			} else {
				DebugFlags.EtengLog("发件箱中短信没有变化");
			}
			mCursor.close();
		}

	}

	/**
	 * 处理已发送的短信信息，比对短信内容，把并对结果保存进数据库
	 * 
	 * @param info
	 *            : 已发送短信信息类
	 */
	private void processSendedSms(final SendedSmsInfo info) {
		// 因为比对可能比较耗时，开启一个线程处理
		DebugFlags.EtengLog("开启线程处理信息，检查匹配情况");

		new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				if (matchingSms(info)) {
					// 比对成功，加入到短信会话表，弹出对话框
					DebugFlags.EtengLog("比对成功，发送消息，弹出对话框");
					// 发送消息，弹出对话框
					handler.sendMessage(handler.obtainMessage(1, info));
				} else {
					// 短信内容匹配不成功，发消息，删除短信会话
					DebugFlags.EtengLog("短信内容匹配不成功，发消息，删除短信会话");
					handler.sendMessage(handler.obtainMessage(2, info));
				}

			}

		}).start();
	}

	/**
	 * 利用本地的白名单和黑名单比对短信内容
	 * 
	 * @param info
	 *            ：已发送短信内容
	 * @return True：比对成功需要弹出提示对话框，False：无需弹出提示对话框
	 */
	private boolean matchingSms(SendedSmsInfo info) {
		sendContent = info.getBody();
		isMatch = true;
		mMatchResult = new StringBuffer();
		StringBuffer sb = new StringBuffer();

		if (!mApplication.isDialog()) { // 判断是否允许对话框弹出，如果不允许
			mApplication.setDialog(true); // 设置为允许弹出
		}

		// 检查是否满足弹出条件
		boolean mDialogShowing = checkDialogShowEnv(sb, info);
		// 如果不满足弹出条件
		if (!mDialogShowing) {
			for (String content : mApplication.getmBlackList()) {
				if (info.getBody().contains(content)) {
					// 短信内容包含黑名单中的数据
					sb = sb.append(",存在黑名单内容");
					isMatch = false;
				}
			}

			if (isMatch)
				sb = sb.append(",无黑名单内容");

			database.insertMatchInfo(ConstantsInfo.DIALOG_SHOWING
					+ ConstantsInfo.SMS_NO_MATCHED,
					Utils.formatDate(System.currentTimeMillis()), sb.toString()
							+ " ，不弹出提示框 " + Utils.formatDate(info.getDate()),
					"2", imsi);
			return false;
		}

		// 首先比对黑名单
		for (String content : mApplication.getmBlackList()) {
			if (info.getBody().contains(content)) {
				// 短信内容包含黑名单中的数据
				database.insertMatchInfo(
						ConstantsInfo.SMS_DIALOG_NO_SHOW
								+ ConstantsInfo.DIALOG_NO_SHOWING,
						Utils.formatDate(System.currentTimeMillis()),
						"满足各项条件,存在黑名单 ，不弹出提示框"
								+ Utils.formatDate(info.getDate()), "2", imsi);
				isMatch = false;
				return false;
			}
		}

		String s = "满足各项条件，无黑名单内容，弹出提示框 " + Utils.formatDate(info.getDate());

		mMatchResult.append(s);
		database.insertMatchInfo(ConstantsInfo.DIALOG_SHOWING
				+ ConstantsInfo.DIALOG_SHOWING,
				Utils.formatDate(System.currentTimeMillis()), s, "1", imsi);
		return true;
	}

	/**
	 * 检测对话框的弹出条件
	 * 
	 * @param sb
	 *            : StringBuffer对象，存放检测到的条件
	 * @param phone
	 *            : 被通知号码
	 * @return True:外部条件允许弹出对话框,False:外部条件不允许弹出对话框
	 */
	private Boolean checkDialogShowEnv(StringBuffer sb, SendedSmsInfo info) {
		Boolean flag = true;// -1符合要求
		if (!Utils.isNetworkConnected(SendSmsDialogService.this)) {
			// 没有网络
			DebugFlags.EtengLog("没有网络");
			sb.append("不满足联网条件,");
			flag = false;
		}
		if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
			// 锁屏状态
			DebugFlags.EtengLog("锁屏状态");
			sb.append("不满足未锁屏条件,");
			flag = false;
		}

		if (info != null) {
			// 限制发送短信字数在3到140字之间
			int leng = info.getBody().toString().length();
			if (leng < 3 || leng > 140) {
				sb.append("不满足短信长度条件,");
				flag = false;
			}
		}

		if (!Utils.checkRegister(imsi, database, SendSmsDialogService.this)) {
			// 用户未注册或无法从网络获取到注册信息
			sb.append("未注册");
			flag = false;
		}

		return flag;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mReceiver);
		// 初始化发件箱
		mSendCount = 0;
		// 解除监听器
		getContentResolver().unregisterContentObserver(observer);
		// 释放dialog
		if (dialog != null) {
			if (dialog.isShowing())
				dialog.dismiss();
			dialog = null;
		}
	}

}
