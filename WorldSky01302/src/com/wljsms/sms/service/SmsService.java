/**
 * Name : SmsService.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.sms.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.ParseException;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;
import com.eteng.world.R;
import com.eteng.world.SendList;
import com.eteng.world.SmsApplication;
import com.wljsms.database.MySqliteHelper;
import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.info.ContactInfo;
import com.wljsms.info.ListSendInfo;
import com.wljsms.info.PhoneStateTimer;
import com.wljsms.info.SendedSmsInfo;
import com.wljsms.info.SmsInfo;
import com.wljsms.info.SmsTaskInfo;
import com.wljsms.info.StateInfo;
import com.wljsms.util.Utils;
import com.wljsms.webservice.CancelCallTask;
import com.wljsms.webservice.GetHeartbeat;
import com.wljsms.webservice.GetReturnList;
import com.wljsms.webservice.PostSmsData;

/**
 * 
 * com.eteng.sms.service.SmsService
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-5 下午3:11:57 Description : 短信监听服务，用户发通知后监听回复的短信，发起呼叫等
 *         Modified :
 */
public class SmsService extends Service {

	private WakeLock wakeLock;
	private MySqliteHelper database;
	/**
	 * 已发通知列表，每组通知对应一个SmsTaskInfo对象
	 */
	private List<SmsTaskInfo> mSmsTaskInfoList;
	/**
	 * 取通话状态列表，每组通知对应一个PhoneStateTimer
	 */
	private List<PhoneStateTimer> mPhoneStateTimerList;
	/**
	 * 服务启动时收件箱中短信数量，随收到短信和删除短信内容而变。
	 */
	private int mInboxCount;
	/**
	 * 监听系统短信数据库的变化
	 */
	private SmsObserver observer;
	/**
	 * 同步锁对象
	 */
	private byte[] lock = new byte[0];
	private byte[] lock2 = new byte[0];
	private List<StateInfo> contents;
	/**
	 * imsi号
	 */
	private String imsi;
	/**
	 * 10分钟心跳一次的定时器对象
	 */
	private Timer timer;
	/**
	 * 是否启动服务第一次心跳标志：True：第一次心跳，False：非第一次心跳
	 */
	private boolean mIsFirstHeartbeat;
	/**
	 * 是否停止服务心跳标志：True：停止服务，False：非停止服务
	 */
	private boolean mIsLastHeartbeat;
	/**
	 * 应用对象
	 */
	private SmsApplication mApplication;
	/**
	 * 数据库游标
	 */
	private Cursor mCursorEx, mCursor;
	/**
	 * 接收短信的时间字符
	 */
	private String receiveDate;
	/**
	 * 通知消息的临时时间集合
	 */
	private ListSendInfo tmp;
	/**
	 * 控制线程是否可以执行，true为可以，false为停止运行
	 */
	private boolean mRun = true;
	/**
	 * 监听来至线程的句柄对象
	 */
	private Handler handler;
	/**
	 * 电话号码
	 */
	private String phone;
	/**
	 * notification ID
	 */
	private int notification_id = 19172439;
	/**
	 * notification管理器
	 */
	private NotificationManager nm;
	/**
	 * 通知内容view
	 */
	private RemoteViews remoteViews;
	/**
	 * 通知消息对象
	 */
	private Notification notification;
	/**
	 * 待跳转的通知Intent
	 */
	private Intent notificationIntent;
	/**
	 * 
	 */
	private boolean mPosting = false;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		// 电源管理
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				SmsService.class.getName());
		wakeLock.acquire();
		super.onCreate();
		mApplication = (SmsApplication) this.getApplication();
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		registerRecv();
		database = mApplication.getDatabase();
		imsi = mApplication.getImsi();
		if (imsi == null || imsi.equals("")) {
			// imsi号为空，再取一次
			imsi = ((TelephonyManager) this
					.getSystemService(Context.TELEPHONY_SERVICE))
					.getSubscriberId();
			if (imsi != null && !imsi.equals(""))
				mApplication.setImsi(imsi);
		}
		// 初始化列表
		mSmsTaskInfoList = new ArrayList<SmsTaskInfo>();
		mPhoneStateTimerList = new ArrayList<PhoneStateTimer>();
		// 取得系统收件箱中的短信数量
		mInboxCount = this
				.getContentResolver()
				.query(Uri.parse(ConstantsInfo.SMS_URI_INBOX),
						new String[] { "_id" }, null, null, null).getCount();
		// 注册数据库监听器
		observer = new SmsObserver(new Handler());
		this.getContentResolver().registerContentObserver(
				Uri.parse(ConstantsInfo.SMS_URI_ALL), true, observer);

		mIsFirstHeartbeat = true;

		// 定时器第一次开始执行的时间
		int firstTime = 0;
		long time = System.currentTimeMillis();
		int curMinute = Utils.getMinuteFromTime(time);
		int curSec = Utils.getSecFromTime(time);
		DebugFlags.EtengLog("当前分钟为：" + curMinute + ",当前秒数为：" + curSec);
		if (curMinute == 0 && curSec == 0) {
			// 整时，立刻访问心跳接口
			firstTime = 0;
		} else {
			firstTime = ((10 - curMinute) * 60 - curSec) * 1000;
		}

		if (firstTime != 0) {
			// 启动一个线程，访问心跳接口
			DebugFlags.EtengLog("启动心跳服务时不是正点，启动一个线程访问心跳接口");
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					heartbeat();
				}

			}).start();
		} else {
			// 启动心跳服务时整点
			DebugFlags.EtengLog("启动心跳服务时整点，不单独启动线程访问心跳接口，由正点访问心跳接口作为第一次心跳接口");
		}

		DebugFlags.EtengLog("心跳接口服务启动，第一次访问心跳接口时间为：" + firstTime + "毫秒以后");

		timer = new Timer();
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				heartbeat();
			}

		};
		// 延迟1分钟后执行
		timer.schedule(timerTask, firstTime, 10 * 60 * 1000);

		mRun = true;

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 10) {
					if (contents != null) {
						for (int i = 0; i < 6 - contents.size(); i++) {
							StateInfo stateInfo = new StateInfo();
							stateInfo.setCurrentstate("");
							stateInfo.setName("");
							contents.add(stateInfo);
						}
						showNotificationMessage(contents);
					}
				}
			}

		};
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(ConstantsInfo.SMS_NETWORK_AVAILABLE)) {

				if (Utils.isNetworkConnected(context)) {
					// 检测到网络连接，提交记录到服务器
					if (mPosting) {
						// 正在提交，直接返回
						DebugFlags.EtengLog("检测到网络连上，正在提交本地缓存的记录");
						return;
					}
					DebugFlags.EtengLog("检测到网络连上，开始提交本地缓存的记录");
					mPosting = true;
					Utils.postSmsTaskDataFromLocal(context, database);
					Utils.postErrorLogData(context, database);
					mPosting = false;
				}
			} else if (intent.getAction().equals(
					ConstantsInfo.SMS_SENDED_BROADCAST)) {
				// 收到发短信广播，初始化一个SmsTaskInfo对象并加入到通知发送列表，监听回复的短信
				DebugFlags
						.EtengLog("SmsService 收到发送短信广播，初始化一个SmsTaskInfo对象并加入到通知发送列表");
				// 获取联系人列表
				@SuppressWarnings("unchecked")
				List<ContactInfo> temp = (List<ContactInfo>) intent.getExtras()
						.getSerializable(ConstantsInfo.SMS_SEND_CONTACT_LIST);
				// 获取短信内容
				String content = intent
						.getStringExtra(ConstantsInfo.SMS_CONTENT);
				// 字符串形式日期，格式时分秒
				String date_str = intent.getStringExtra(ConstantsInfo.DATE_STR);
				// 获取格林时间
				long time = intent.getLongExtra(ConstantsInfo.TIME, 0);
				// 格林时间加imis号
				String id = intent.getStringExtra(ConstantsInfo.SMS_TASK_ID);

				if (temp == null || temp.size() == 0 || content == null
						|| content == "")
					return;
				// 初始化一个通知对象并设置值
				SmsTaskInfo info = new SmsTaskInfo();
				info.setmContactList(temp);
				info.setmContent(content);
				info.setId(id);
				info.setmDateStr(date_str);
				info.setTime(time);
				// 添加到通知任务列表
				addSmsTaskInfo(info);
				DebugFlags.EtengLog("mSmsTimerInfoList.size() : "
						+ mSmsTaskInfoList.size());
			} else if (intent.getAction().equals(
					ConstantsInfo.SMS_SENDED_COMPLETE_BROADCAST)) {
				// 全部短信发送完成，把发送成功并且没有收到回复短信的号码发给服务器拨号
				DebugFlags.EtengLog("收到所有短信发送完毕广播，调用服务器接口，把号码传递给服务器拨号");
				@SuppressWarnings("unchecked")
				List<ContactInfo> temp = (List<ContactInfo>) intent.getExtras()
						.getSerializable(
								ConstantsInfo.SMS_SEND_FAILED_CONTACT_LIST);
				// 格林时间加imis号
				String id = intent.getStringExtra(ConstantsInfo.SMS_TASK_ID);

				delContactsFromTaskListAndPostToServer(id, temp);
			} else if (intent.getAction().equals(ConstantsInfo.DEL_MESSAGE)) {
				int i = database.updateIsShowEx();
				DebugFlags
						.EtengLog("##############################################通知已经被清除"
								+ "清除数为" + "  " + i);
			}

			else if (intent.getAction().equals(
					ConstantsInfo.GET_PHONE_STATE_BROADCAST)) {
				// 收到删除获取电话状态广播
				// 调用删除电话状态定时器方法
				deletePhoneStateTimer();
			} else if (intent.getAction().equals(
					ConstantsInfo.DELETE_PHONE_STATE_TIMER_BROADCAST)) {
				// 用户删除了通知，删除电话定时器，调用服务器接口不再给用户拨打电话，以通知ID为查询条件
				String taskId = intent.getStringExtra("taskId");
				deletePhoneAndSmsTimer(taskId);
				String time = intent.getStringExtra("time");
				long date;
				try {
					date = Long.parseLong(time);
				} catch (Exception e) {
					date = 0;
				}
				DebugFlags
						.EtengLog("SmsService 收到删除通知广播，删除电话定时器对象，不再拨打电话，通知ID为 ："
								+ taskId);
				if (System.currentTimeMillis() - date < ConstantsInfo.SMS_TIMEOUT) {
					DebugFlags.EtengLog("删除任务在任务超时时间之内，调用服务接口，取消拨号");
					String result = null;
					try {
						CancelCallTask cancelTask = new CancelCallTask();
						result = cancelTask.init("", taskId, null);
					} catch (Exception e) {
					}

					if (result != null) {
						DebugFlags.EtengLog("调用服务器接口取消通知拨号成功,返回值为：" + result);

					} else {
						DebugFlags.EtengLog("调用服务器接口取消通知拨号失败");
					}
				} else {
					DebugFlags.EtengLog("删除任务不在任务超时时间之内，不调用服务接口");
				}
			} else if (intent.getAction().equals(
					ConnectivityManager.CONNECTIVITY_ACTION)) {
				// 网络链接发生变化，连上或断开
				DebugFlags.EtengLog("网络链接发生变化，连上或断开");
				if (Utils.isNetworkConnected(SmsService.this)) {
					// 网络连上，心跳一次
					DebugFlags.EtengLog("网络连上，心跳一次");
					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							heartbeat();
						}

					}).start();
				} else {
					DebugFlags.EtengLog("网络断开，报错一条心跳记录");
					// 网络断开，报错一条心跳记录
					Utils.insertHeartbeatErrorLog(database, imsi, "40002",
							Utils.formatDate(System.currentTimeMillis()));
				}
			} else if (intent.getAction().equals(
					ConstantsInfo.SMS_SENDNONE_ANDROID)) {
				DebugFlags.EtengLog("22222222222222222222222点击确定按钮 "
						+ ((SmsApplication) SmsService.this.getApplication())
								.isPost());
				@SuppressWarnings("unchecked")
				List<SendedSmsInfo> mSmsList = (List<SendedSmsInfo>) intent
						.getSerializableExtra("Data");

				long time = System.currentTimeMillis();
				String time_str = Long.toString(time);
				String taskId = time_str + imsi;
				String mDateStr = Utils.formatDate(time);
				String content = "";
				int sms_length;
				if (mSmsList.size() > 0)
					sms_length = mSmsList.get(0).getBody().length();
				else
					sms_length = 0;
				List<ContactInfo> contacts = new ArrayList<ContactInfo>();
				List<ContactInfo> contacts1 = new ArrayList<ContactInfo>();
				database.getWritableDatabase().beginTransaction();
				for (SendedSmsInfo sendedSmsInfo : mSmsList) {
					content = sendedSmsInfo.getBody();
					sms_length = sendedSmsInfo.getBody().length();
					database.insertData(sendedSmsInfo.getName(),
							sendedSmsInfo.getPhone(), sendedSmsInfo.getBody(),
							"1", "0", mDateStr, "", "", "0", time_str, taskId,
							"0");
					ContactInfo contactInfo = new ContactInfo();
					contactInfo.setContext(sendedSmsInfo.getBody());
					contactInfo.setPhone(sendedSmsInfo.getPhone());
					DebugFlags
							.EtengLog("###############################手机号码为：sendedSmsInfo.getPhone()  :  "
									+ sendedSmsInfo.getPhone());
					contacts.add(contactInfo);
					contacts1.add(contactInfo);
				}
				database.getWritableDatabase().setTransactionSuccessful();
				database.getWritableDatabase().endTransaction();
				// 发送发通知广播，让通知页面刷新数据
				SmsTaskInfo info = new SmsTaskInfo();
				info.setmContactList(contacts);
				info.setmContent(content);
				info.setId(taskId);
				info.setmDateStr(mDateStr);
				info.setTime(time);
				// 添加到通知任务列表
				addSmsTaskInfo(info);
				DebugFlags.EtengLog("mSmsTimerInfoList.size() : "
						+ mSmsTaskInfoList.size());

				sendBroadcast(new Intent(
						ConstantsInfo.SMS_TASK_SENDED_BROADCAST));
				DebugFlags.EtengLog("调用服务器接口前   contacts.size()  :  "
						+ contacts.size());
				postSmsTaskData(taskId, mDateStr, contacts1, sms_length);
				synchronized (lock) {
					callServer(mDateStr, Long.toString(time / 1000), taskId,
							"非急短信");
				}
			}
		}

	};

	/**
	 * 提交短信统计信息到服务器,开启线程处理
	 * 
	 * @param 格式时间加imsi号
	 * 
	 */
	private void postSmsTaskData(final String id, final String mDateStr,
			final List<ContactInfo> mContacts1, final int sms_length) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 拼接号码列表
				try {
					StringBuffer sb = new StringBuffer();
					for (ContactInfo info : mContacts1) {
						sb.append(info.getPhone());
						sb.append(",");
					}
					String phones = sb.toString();
					phones = phones.substring(0, phones.length() - 1);
					// 拼接版本号
					String version = mApplication.getWlj_version() + ","
							+ mApplication.getRelease();
					// 内容长度
					String len = Integer.toString(sms_length);
					// 是否有网络连接标志，True : 有网络，False ： 无网络
					boolean networkFlag = true;
					String result = null;
					if (!Utils.isNetworkConnected(SmsService.this)) {
						// 没有可用的网络
						networkFlag = false;
					}
					if (networkFlag) {
						// 有网络，提交信息到服务器
						DebugFlags.EtengLog("开始提交通知任务信息到服务器");
						PostSmsData postSmsData = new PostSmsData();
						result = postSmsData.init(imsi, id, phones,
								mApplication.getModel(), version, sms_length,
								mDateStr, "非急短信", null);

					}

					if (result == null || result.equals("1") || !networkFlag) {
						// 无网络或提交短信记录到服务器失败，保存到通知任务记录表中，有网络时再次提交
						DebugFlags
								.EtengLog("无网络或服务器接口访问失败，保存通知任务信息到本地数据库中，IMSI号："
										+ imsi
										+ ",id："
										+ id
										+ ",收件人列表 ： "
										+ phones
										+ ",机型："
										+ mApplication.getModel()
										+ ",版本信息："
										+ version + ",通知内容长度 ： " + len);
						if (database != null) {
							Cursor cur = database.getTasks();
							if (cur != null) {
								if (cur.getCount() == ConstantsInfo.SMS_TASK_COUNT) {
									// 记录条数达到记录上限，删除一条记录
									cur.moveToFirst();
									database.deleteTask(cur.getString(cur
											.getColumnIndex(MySqliteHelper.TASK_ID)));

									DebugFlags.EtengLog("从本地通知任务记录数据库中删除一条记录");
								}
							}
							database.insertTask(imsi, id, phones,
									mApplication.getModel(), version, len,
									mDateStr, "非急短信");

							DebugFlags.EtengLog("添加一条记录到本地通知任务库中");
						}
					} else {
						DebugFlags.EtengLog("提交通知任务信息到服务器成功");
					}
				} catch (Exception e) {

				}
				mContacts1.clear();
			}

		}).start();

	}

	/**
	 * 删除电话和短信定时器对象
	 * 
	 * @param 通知创建时间
	 * 
	 */
	public void deletePhoneAndSmsTimer(String taskId) {
		synchronized (lock) {
			// 删除短信定时器：目的是没有到打电话时间，不再拨打电话
			int len = mSmsTaskInfoList.size();
			for (int i = 0; i < len; i++) {
				if (mSmsTaskInfoList.get(i).getId().equals(taskId)) {
					// 找到短信定时器对象，取消任务，删除，不再查找
					mSmsTaskInfoList.get(i).clean();
					mSmsTaskInfoList.remove(i);
					DebugFlags.EtengLog("收到删除通知广播，删除了通知任务对象，通知创ID为 ：" + taskId);
					if (Utils.isNetworkConnected(SmsService.this)) {
						// 有网络连接，调用服务器接口，删除拨号任务
					}
					break;
				}
			}
		}

		synchronized (lock2) {
			// 删除电话定时器：目的是不再拨打电话
			int len = mPhoneStateTimerList.size();
			for (int i = 0; i < len; i++) {
				if (mPhoneStateTimerList.get(i).getmTaskId().equals(taskId)) {
					mPhoneStateTimerList.get(i).clear();
					mPhoneStateTimerList.remove(i);
					DebugFlags.EtengLog("收到删除通知广播，删除了电话定时器，通知ID为 ：" + taskId);
					break;
				}
			}
		}
	}

	/**
	 * 服务器接口方法,提交拨号列表到服务器，失败将重试20次，间隔60秒。 失败的判断标准是取不到服务器的返回状态。 没有网络，不提交。
	 * 
	 * @param contacts
	 *            ：联系人列表
	 * @param date_str
	 *            ：通知创建时间，如2013-02-05 15：28：45
	 * @param time
	 *            ：通知创建时间，格林时间
	 * @param time_delay
	 *            ：通知超时时间
	 * @param id
	 *            ：通知任务id，格林时间+imsi号
	 * @return True : 成功提交号码到服务器，False : 提交失败
	 * 
	 */
	public boolean callServer(final String date_str, final String time,
			final String id, final String source) {
		DebugFlags.EtengLog("开始调用服务器接口,通知发送时间为 ：" + date_str);

		// 初始化一个定时器，通过定时器执行重复提交号码列表到服务器
		final Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			int count = 0;// 访问服务器次数

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (count++ < 16) {
					String phones = "";
					StringBuffer sb = new StringBuffer();
					// 利用任务id取得联系人信息，此联系人信息为删除回复号码后的。
					for (SmsTaskInfo info : mSmsTaskInfoList) {
						if (info.getId().equals(id)) {
							// 找到，取出联系人信息，拼接号码字符串，号码之间用","分割
							for (ContactInfo ci : info.getmContactList()) {
								sb.append(ci.getPhone());
								sb.append(",");
							}
							phones = sb.toString();
							// 去除最后的","
							if (phones.length() != 0)
								phones = phones.substring(0,
										phones.length() - 1);
							break;
						}
					}
					// 开始提交号码到服务器时间
					String postTime = "";
					if (!Utils.isNetworkConnected(SmsService.this)) {
						// 没有网络连接，不访问接口，直接返回
						DebugFlags.EtengLog("没有网络连接，不访问接口，直接返回");
						return;
					}
					try {
						GetReturnList getReturnList = new GetReturnList();
						String IDs = null;
						// 提交号码到服务器，提交成功服务器会有返回值
						long tmpTime = System.currentTimeMillis();
						postTime = Long.toString(tmpTime / 1000);
						DebugFlags.EtengLog("提交时间为："
								+ Utils.formatDate(tmpTime));

						DebugFlags
								.EtengLog("开始调用服务器接口，把呼叫号码提交给服务器进行拨号，参数 ： id : "
										+ id
										+ ",imsi : "
										+ imsi
										+ ",号码列表 ： "
										+ phones
										+ ",通知发送时间："
										+ time
										+ ",提交时间 ：" + postTime);
						IDs = getReturnList.init(id, imsi, phones, time,
								postTime, source, null);
						if (IDs != null && !IDs.equals("")) {
							// 成功提交，并取到了返回服务器返回结果
							DebugFlags.EtengLog("提交拨号列表到服务器成功，返回值为 ： " + IDs);
							if (IDs.equals("2")) {
								// 用户未注册，未注册用户不能发起语音呼叫，保存一条提交呼叫信息失败日志
								Utils.insertCommitCallFailedErrorLog(database,
										imsi, "60006", phones, Utils
												.formatDate(System
														.currentTimeMillis()));
							} else if (IDs.equals("3")) {
								// 提交失败，保存日志
								Utils.insertCommitCallFailedErrorLog(database,
										imsi, "60007", phones, Utils
												.formatDate(System
														.currentTimeMillis()));
							} else {
								// 成功提交呼叫信息
								PhoneStateTimer phoneTimer = new PhoneStateTimer(
										SmsService.this);
								// 设置字符串时间
								phoneTimer.setmDateStr(date_str);
								// 设置定时器初次执行时间，2秒后开始取状态
								phoneTimer.setFirst_delay_time(2 * 1000);
								phoneTimer.setmTaskId(id);
								synchronized (lock2) {
									// 同步代码
									mPhoneStateTimerList.add(phoneTimer);
								}
								phoneTimer.initTimer(database);
							}
							// 结束定时器的执行
							timer.cancel();
						} else {
							// 取不到任务ID，认为访问服务器失败。
							DebugFlags.EtengLog("提交拨号信息失败，取不到服务器返回状态");
							if (count == 15) {
								// 最后一次重试提交，保存失败信息
								if (!Utils.isNetworkConnected(SmsService.this)) {
									Utils.insertCommitCallFailedErrorLog(
											database, imsi, "60003", phones,
											Utils.formatDate(System
													.currentTimeMillis()));
								} else
									Utils.insertCommitCallFailedErrorLog(
											database, imsi, "60004", phones,
											Utils.formatDate(System
													.currentTimeMillis()));
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				} else {
					// 结束定时器的执行，提交次数达到上限，不再提交
					timer.cancel();
				}
			}

		};
		// 延迟半秒后执行，以后每隔1分钟执行一次
		timer.schedule(timerTask, 6000, 60 * 1000);
		return true;
	}

	private void heartbeat() {
		DebugFlags.EtengLog("访问心跳接口，当前时间为："
				+ Utils.formatDate(System.currentTimeMillis()));
		if (imsi == null || imsi.equals("")) {
			// imsi号为空，再取一次
			imsi = ((TelephonyManager) this
					.getSystemService(Context.TELEPHONY_SERVICE))
					.getSubscriberId();
			if (imsi != null && !imsi.equals("")) {
				mApplication.setImsi(imsi);
			} else {
				DebugFlags.EtengLog("imsi号为空，再次获取也获取不到imsi号，不访问心跳接口");
				return;
			}

		}
		if (!Utils.isNetworkConnected(SmsService.this)) {
			// 没有网络
			DebugFlags.EtengLog("访问心跳接口时没有网络，如果是第一次访问心跳接口，插入失败原因");
			if (mIsFirstHeartbeat) {
				DebugFlags.EtengLog("访问心跳接口时没有网络，是第一次访问心跳接口，插入失败原因");
				mIsFirstHeartbeat = false;
				Utils.insertHeartbeatErrorLog(database, imsi, "40005",
						Utils.formatDate(System.currentTimeMillis()));
			} else if (mIsLastHeartbeat) {
				DebugFlags.EtengLog("访问心跳接口时没有网络，是最后一次访问心跳接口，插入失败原因");
				mIsLastHeartbeat = false;
				Utils.insertHeartbeatErrorLog(database, imsi, "40006",
						Utils.formatDate(System.currentTimeMillis()));
			} else {
				DebugFlags
						.EtengLog("访问心跳接口时没有网络，不是第一次访问心跳接口也不是最后一次访问心跳接口，不插入失败原因");
			}
			return;
		}
		GetHeartbeat getHeartbeat = new GetHeartbeat();
		String result = null;
		try {
			// 调用服务接口方法，从服务器获取imsi号对应的电话号码。
			result = getHeartbeat.init(imsi, null);
		} catch (Exception e) {
			result = null;
		}
		if (result == null || result.equals("")) {
			// 心跳失败，无法从服务器获取返回结果，插入失败日志
			DebugFlags.EtengLog("访问心跳接口失败，无法获取服务器返回状态");
			if (mIsFirstHeartbeat) {
				DebugFlags.EtengLog("访问心跳接口时有网络，是第一次访问心跳接口，插入失败原因");
				mIsFirstHeartbeat = false;
				Utils.insertHeartbeatErrorLog(database, imsi, "40005",
						Utils.formatDate(System.currentTimeMillis()));
			} else if (mIsLastHeartbeat) {
				DebugFlags.EtengLog("访问心跳接口时有网络，是最后一次访问心跳接口，插入失败原因");
				mIsLastHeartbeat = false;
				Utils.insertHeartbeatErrorLog(database, imsi, "40006",
						Utils.formatDate(System.currentTimeMillis()));
			} else {
				DebugFlags
						.EtengLog("访问心跳接口时有网络，不是第一次访问心跳接口也不是最后一次访问心跳接口，插入失败原因");
				Utils.insertHeartbeatErrorLog(database, imsi, "40003",
						Utils.formatDate(System.currentTimeMillis()));
			}
		} else {
			// 有返回结果，认为心跳成功
			DebugFlags.EtengLog("访问心跳接口成功，服务器返回状态为：" + result);
		}
	}

	/**
	 * 从List<PhoneStateTimer>中删除一项，同步方法
	 */
	private void deletePhoneStateTimer() {
		List<PhoneStateTimer> tmp = new ArrayList<PhoneStateTimer>();
		synchronized (lock2) {
			int len = mPhoneStateTimerList.size();
			for (int i = 0; i < len; i++) {
				if (mPhoneStateTimerList.get(i).ismDeletable()) {
					mPhoneStateTimerList.get(i).clear();
					tmp.add(mPhoneStateTimerList.remove(i));
					i--;// 删除之后，后面项的索引依次减以，故i应该减1
					len--;
				}
			}
		}

		deleteSmsTaskInfoFromTastId(tmp);
	}

	/**
	 * 从通知任务列表中删除制定索引的项
	 * 
	 * @param index
	 *            : 待删除项的索引
	 */
	private void deleteSmsTaskInfo(int index) {

		mSmsTaskInfoList.get(index).clean();
		mSmsTaskInfoList.remove(index);

	}

	/**
	 * 从通知任务列表中删除指定id的项
	 * 
	 * @param index
	 *            : 待删除项的索引
	 */
	private void deleteSmsTaskInfoFromTastId(List<PhoneStateTimer> tmp) {
		synchronized (lock) {

			for (PhoneStateTimer info : tmp) {
				int len = mSmsTaskInfoList.size();
				for (int i = 0; i < len; i++) {
					if (mSmsTaskInfoList.get(i).getId()
							.equals(info.getmTaskId())) {
						mSmsTaskInfoList.get(i).clean();
						mSmsTaskInfoList.remove(i);
						break;
					}
				}
			}
			tmp.clear();
		}
	}

	/**
	 * 从通知任务列表中删除收到回复短信的号码
	 * 
	 * @param num
	 *            : 回复短信的电话号码
	 */
	private synchronized boolean deletePhoneNumFromSmsTaskList(String phone) {
		synchronized (lock) {
			int len = mSmsTaskInfoList.size();
			boolean flag = false;
			for (int i = 0; i < len; i++) {
				List<ContactInfo> tempList = mSmsTaskInfoList.get(i)
						.getmContactList();
				int len1 = tempList.size();
				for (int j = 0; j < len1; j++) {
					if (tempList.get(j).getPhone().equals(phone)) {
						tempList.remove(j);
						flag = true;
						if (tempList.size() <= 0) {
							deleteSmsTaskInfo(i);
							i--;
							len--;
						}
						break;
					}
				}
			}
			return flag;
		}
	}

	/**
	 * 向List<SmsTimerInfo>中增加一项，同步方法
	 */
	private void addSmsTaskInfo(SmsTaskInfo info) {
		synchronized (lock) {
			mSmsTaskInfoList.add(info);
		}
	}

	/**
	 * 从通知任务列表中查找id项并删除contact_list中的联系人信息,把剩下的联系人信息发送到服务器拨号
	 * 
	 * @param id
	 *            : 通知id
	 * @param contact_list
	 *            : 要删除的联系人列表
	 */
	private void delContactsFromTaskListAndPostToServer(String id,
			List<ContactInfo> contact_list) {
		synchronized (lock) {
			for (SmsTaskInfo info : mSmsTaskInfoList) {
				// 遍历通知任务列表
				if (info.getId().equals(id)) {
					// 查找到，进行删除联系人操作
					List<ContactInfo> tmpList = info.getmContactList();
					for (ContactInfo ci : contact_list) {
						// 遍历待删除列表
						for (ContactInfo ci1 : tmpList) {
							if (ci.getPhone().equals(ci1.getPhone())) {
								// 找到一个，删除
								tmpList.remove(ci1);
								// 遍历下一个待删除联系人
								break;
							}
						}
					}
					// 提交号码信息到服务器进行拨号
					if (tmpList.size() == 0) {
						// 没有联系人信息，无需提交,删除任务对象
						mSmsTaskInfoList.remove(info);
						break;
					}
					// 提交
					callServer(info.getmDateStr(),
							Long.toString(info.getTime() / 1000), info.getId(),
							"急短信");
					// 通知任务唯一，结束删除操作
					break;
				}
			}
		}
	}

	/**
	 * 注册收到短信广播接收器
	 */
	private void registerRecv() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConstantsInfo.SMS_SENDED_BROADCAST);
		filter.addAction(ConstantsInfo.GET_PHONE_STATE_BROADCAST);
		filter.addAction(ConstantsInfo.DELETE_PHONE_STATE_TIMER_BROADCAST);
		filter.addAction(ConstantsInfo.SMS_SENDED_COMPLETE_BROADCAST);
		filter.addAction(ConstantsInfo.SMS_SENDNONE_ANDROID);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(ConstantsInfo.SMS_NETWORK_AVAILABLE);
		filter.addAction(ConstantsInfo.DEL_MESSAGE);
		registerReceiver(receiver, filter);
	}

	/**
	 * Description:计算通知接收比例
	 * 
	 * @param: CreateDate 短信发送时间
	 * @param: tempHashMap 哈希表
	 * 
	 */
	private void mergeData(String CreateDate,
			HashMap<String, Object> tempHashMap) {
		tempHashMap.put("ReplyCount", "0");
		mCursorEx = database.getReply(CreateDate); // 计算回话和应答数的比例
		if (mCursorEx == null)
			return;
		while (mCursorEx.moveToNext() && mRun) {
			try {
				tempHashMap.put("ReplyCount", mCursorEx.getString(mCursorEx
						.getColumnIndex(MySqliteHelper.Count)));
			} catch (Exception e) {
				mCursorEx.close(); // 关闭数据游标
			}
		}
		mCursorEx.close();// 关闭数据游标
	}

	/**
	 * 把字符转换为long类型
	 * 
	 * @param: dateStr 时间字符
	 * @return : 浮点型的时间 long
	 */
	private long getDateLong(String dateStr) throws java.text.ParseException {
		// 格式化时间字符
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return df.parse(dateStr).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}

	/**
	 * 计算发送短信的时间到接收短信的时间差值
	 * 
	 * @param: CreateDate 发送短信的时间
	 * @return : 分钟数
	 */
	private int getInterval(String CreateDate) throws java.text.ParseException {
		long sendDate = getDateLong(CreateDate);
		long currentDate = getDateLong(receiveDate);
		// 接收短信的时间减发送短信的时间
		long l = currentDate - sendDate;
		long minute = l / 60000;
		int interval = Integer.parseInt(String.valueOf(minute));
		return interval; // 返回时间间隔分钟数
	}

	/**
	 * 启动线程监听通知消息
	 */
	private void noticeListening() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					mCursor = database.groupDataEx(); // 获取分组运算数据
					if (mCursor == null)
						return;

					while (mCursor.moveToNext() && mRun) {

						// 从数据库里获取发送短信总数
						String Count = mCursor.getString(mCursor
								.getColumnIndex(MySqliteHelper.Count));
						// 短信发送时间
						String CreateDate = mCursor.getString(mCursor
								.getColumnIndex(MySqliteHelper.CreateDate));
						HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
						mergeData(CreateDate, tempHashMap);
						int iReplyCount = Integer.valueOf(tempHashMap.get(
								"ReplyCount").toString());
						double doubleCount = Integer.valueOf(Count);
						int intCount = Integer.valueOf(Count);
						double mod = iReplyCount / doubleCount;
						contents = new ArrayList<StateInfo>();
						// 如果短信回复数大于50%
						if ((mod * 10 >= ConstantsInfo.MESSAGE_PROPORTION)) {
							Cursor cur = database.getGroupByUserData(phone,
									CreateDate);
							if (cur != null) {
								tmp = new ListSendInfo();
								tmp.setCount(String.valueOf(intCount));
								tmp.setReplyCount(String.valueOf(iReplyCount));
								while (cur.moveToNext()) {
									String sendSmsDate = cur.getString(cur
											.getColumnIndex(MySqliteHelper.CreateDate));
									// 如果不满足规则不显示通知
									if (!isShowMessage(sendSmsDate)) {
										cur.close();
										mCursor.close(); // 关闭游标
										return;
									}
									int interval = getInterval(sendSmsDate);
									// 如果大于30分钟不显示通知
									if (interval > ConstantsInfo.MESSAGE_INTERVAL) {
										cur.close();
										mCursor.close(); // 关闭游标
										return;
									}
									tmp.setCreateDate(sendSmsDate);
									tmp.setContextMax(cur.getString(cur
											.getColumnIndex("context")));
									StateInfo contactInfo = new StateInfo();
									contactInfo.setCurrentstate(cur.getString(
											cur.getColumnIndex("reply"))
											.equals("1") == true ? "回复" : "未回复");
									contactInfo.setName(cur.getString(cur
											.getColumnIndex("user")));

									contents.add(contactInfo);
								}
								cur.close(); // 关闭游标
								mCursor.close(); // 关闭游标
								handler.sendMessage(handler.obtainMessage(10)); // 发送消息通知更新UI列表
								return;
							}
						}
					}
					mCursor.close(); // 关闭数据游标
				} catch (Exception e) {
					mCursor.close();
				}
			}
		}).start();
	}

	/**
	 * 弹出通知消息
	 * @param: contents 当前通知要显示的详细信息集合
	 * 
	 */
	private void showNotificationMessage(List<StateInfo> contents) {

		if (remoteViews == null || notification == null) {
			notification = new Notification(R.drawable.logo, "已经接到通知",
					System.currentTimeMillis());
			remoteViews = new RemoteViews(getPackageName(),
					R.layout.layout_message);
			// 后面的参数分别是显示在顶部通知栏的小图标，小图标旁的文字（短暂显示，自动消失）系统当前时间
			notification.defaults = Notification.DEFAULT_ALL;
			notification.tickerText = "已经接到通知";
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notificationIntent = new Intent(this, SendList.class);
		}

		// 设置通知消息要显示的数据
		remoteViews.setTextViewText(R.id.tv_Name1, contents.get(0) == null ? ""
				: contents.get(0).getName());
		remoteViews.setTextViewText(R.id.tv_State1,
				contents.get(0) == null ? "" : contents.get(0)
						.getCurrentstate());
		remoteViews.setTextViewText(R.id.tv_Name2, contents.get(1) == null ? ""
				: contents.get(1).getName());
		remoteViews.setTextViewText(R.id.tv_State2,
				contents.get(1) == null ? "" : contents.get(1)
						.getCurrentstate());
		remoteViews.setTextViewText(R.id.tv_Name3, contents.get(2) == null ? ""
				: contents.get(2).getName());
		remoteViews.setTextViewText(R.id.tv_State3,
				contents.get(2) == null ? "" : contents.get(2)
						.getCurrentstate());
		remoteViews
				.setTextViewText(R.id.tv_messageContent, tmp.getContextMax());

		String value = tmp.getCreateDate();
		// 设置要提交到详细列表页面的参数
		notificationIntent.putExtra("CreateDate", value);
		int count = (int) Math.floor(Double.parseDouble(tmp.getCount()));
		notificationIntent.putExtra("count", String.valueOf(count));
		notificationIntent.putExtra("replyCount", tmp.getReplyCount());
		notificationIntent.putExtra("del", true);
		int surplusCount = count - Integer.valueOf(tmp.getReplyCount());
		notificationIntent.putExtra("surplusCount",
				String.valueOf(surplusCount));

		notificationIntent.putExtra("contextMax", tmp.getContextMax());

		String content = "通知" + tmp.getCount() + "人，" + tmp.getReplyCount()
				+ "人已回复，" + String.valueOf(surplusCount) + "人未回复";

		remoteViews.setTextViewText(R.id.tv_Proportion, content);
		notification.contentView = remoteViews;

		notification.deleteIntent = PendingIntent.getBroadcast(SmsService.this,
				0, new Intent(ConstantsInfo.DEL_MESSAGE), 0);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.contentIntent = contentIntent;
		// 显示通知
		nm.notify(notification_id, notification);

	}

	// 数据库监听对象
	private class SmsObserver extends ContentObserver {

		private Cursor mCursor;

		public SmsObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			mCursor = SmsService.this.getContentResolver().query(
					Uri.parse(ConstantsInfo.SMS_URI_INBOX),
					new String[] { "_id", "address", "body", "date" }, null,
					null, "date desc");

			if (mCursor == null)
				return;
			if (mCursor.getCount() != mInboxCount) {
				if (mCursor.getCount() > mInboxCount) {
					// 收件箱中短信数量增加，即收到短信，取得短信信息，并发送收到短信广播
					int phoneNumberColumn = mCursor.getColumnIndex("address");
					int smsbodyColumn = mCursor.getColumnIndex("body");
					int dateColumn = mCursor.getColumnIndex("date");
					if (mCursor.moveToFirst()) {

						String callPhone = mCursor.getString(phoneNumberColumn)
								.toString();
						if ((callPhone.length() < 11)
								|| (callPhone.length() > 14))
							return;
						callPhone = callPhone.substring(
								callPhone.length() - 11, callPhone.length());
						SmsInfo info = new SmsInfo();
						info.setPhoneNumber(callPhone);
						info.setSmsbody(mCursor.getString(smsbodyColumn));
						info.setDate(Utils.formatDate(Long.parseLong(mCursor
								.getString(dateColumn))));
						Intent bd = new Intent(
								ConstantsInfo.AFTER_RECEIVED_SMS_BROADCAST);
						bd.putExtra(ConstantsInfo.PHONE_NUM,
								info.getPhoneNumber());

						database.updateSmsReceiverData(info.getSmsbody(),
								info.getPhoneNumber());
						sendBroadcast(bd);
						phone = info.getPhoneNumber();
						DebugFlags.EtengLog("》》》》》》》》》》》回复的电话号码是:" + phone);
						receiveDate = info.getDate();
						DebugFlags.EtengLog("回复时间为:" + receiveDate);
						noticeListening();
						final String phone = info.getPhoneNumber();
						// 开启一个线程，删除拨号任务，
						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								DebugFlags
										.EtengLog("收到短信拨号取消线程运行，调用服务器接口取消拨号，号码为："
												+ phone);
								if (deletePhoneNumFromSmsTaskList(phone)) {
									// 只有成功从通知任务列表中删除了一个号码才开始调用取消拨号接口
									if (!Utils
											.isNetworkConnected(SmsService.this)) {
										// 没有网络，直接返回
										DebugFlags.EtengLog("没有网络，无法调用取消任务接口");
										return;
									}
									try {
										CancelCallTask cancelTask = new CancelCallTask();
										if (cancelTask.init(phone, "", null) != null) {
											DebugFlags
													.EtengLog("调用服务器接口取消拨号成功，号码为："
															+ phone);
										} else {
											DebugFlags
													.EtengLog("调用服务器接口取消拨号失败，号码为："
															+ phone);
										}
									} catch (Exception e) {
									}

								}

							}

						}).start();

						DebugFlags.EtengLog("收到新短信并发送广播成功，号码："
								+ info.getPhoneNumber() + ",内容 : "
								+ info.getSmsbody() + ",时间：" + info.getDate());
					}
				} else {
					DebugFlags.EtengLog("收件箱中短信减少，应该删除了短信");
				}
				mInboxCount = mCursor.getCount();
			} else {
				DebugFlags.EtengLog("收件箱中短信没有变化");
			}

			mCursor.close();
		}

	}

	/**
	 * 计算该通知消息是否满足规则显示
	 * 
	 * @param: currentCreate 接收短信的当前时间
	 * @Return 是否满足规则 true 满足 ，false 不满足
	 */
	private boolean isShowMessage(String currentCreate)
			throws java.text.ParseException {
		// 获取最大的已弹出过的最大时间字符
		String getMaxShowCreateDate = database
				.getShowMaxCreateDate(currentCreate);
		long MaxShowdate = getDateLong(getMaxShowCreateDate);
		long Currentdate = getDateLong(currentCreate);
		// 如果已弹出过的最大时间大于当前接收短信的时间，就不弹出
		if (MaxShowdate > Currentdate) {
			return false; // 最大的弹出时间大于当前时间不弹出通知
		}
		return true; // 弹出
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mInboxCount = -1;
		mIsLastHeartbeat = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				heartbeat();
			}

		}).start();
		mSmsTaskInfoList.clear();
		getContentResolver().unregisterContentObserver(observer);
		unregisterReceiver(receiver);
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
		DebugFlags.EtengLog("SmsService onDestroy() Service退出！");
		super.onDestroy();
	}
}
