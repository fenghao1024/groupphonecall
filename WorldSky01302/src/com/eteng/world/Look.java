/**
 * Name : Look.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.eteng.world;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.wljsms.adapter.LookAdapter;
import com.wljsms.database.MySqliteHelper;
import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.info.ContactInfo;
import com.wljsms.sms.service.SendSmsDialogService;
import com.wljsms.sms.service.SmsService;
import com.wljsms.util.Configure;
import com.wljsms.util.ManagerUpdate;
import com.wljsms.util.Utils;
import com.wljsms.webservice.GetApkVersion;
import com.wljsms.webservice.GetPhone;
import com.wljsms.webservice.PostChannelCode;

/**
 * 
 * com.eteng.world.Look
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-21 下午2:12:22 Description :
 *         通知主体类，完成通知信息列表，进程计算，短信消息广播接收，服务后台监听， 版本检查，自动升级，网络检查等主体功能。
 */
public class Look extends Activity {
	// 分别为“联系我们”，“关于”，“帮助”的线性布局
	private LinearLayout linCons, linAbout, linHelp;
	private ListView Lv_look; // 通知列表对象
	private LookAdapter lookAdapter; // 通知列表适配器
	private Cursor mCursor, mCursorEx, mDataCursor; // 数据库游标变量：分别用于获取和数据库通讯的数据游标。
	private MySqliteHelper database; // 数据库操作对象
	private ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>(); // 数据集合
	private ArrayList<HashMap<String, Object>> tempList = new ArrayList<HashMap<String, Object>>(); // 临时数据集合
	private Resources res; // 系统资源对象
	private boolean mRun = true; // 控制线程是否可以执行，true为可以，false为停止运行
	private ProgressDialog dialog; // 进度条对象
	private ManagerUpdate mUpdateManager; // 更新管理对象
	private boolean mIsCheckedUpdate = false;// 是否已检查过更新标志
	private Handler handler;
	private String imsi; // Imsi 号
	private String phoneNum = ""; // 电话号码
	private SmsApplication mApplication; // 应用对象
	private View mNetworkTips; // 网络提示布局父对象，用于无网络情况下的提示
	private ImageView mNetworkImage; // 无网络提示布局中的按钮
	private LinearLayout mNotificationLL; // 无通知布局的父对象
	private ImageView mNotificationImage; // 无通知布局中的添加新通知按钮
	private PopupWindow upPopWin; // 弹窗对象
	private ImageView imgView; // 底部左边按钮
	private View view; // 视图对象
	private boolean mHasSIM;// 是否有SIM卡，true:有，false:无
	private AlertDialog mDeleteDialog; // 删除消息提示对象
	private Button mDialog1, mDialog2; // 对话框中的两个按钮
	private View mDeleteDialogLL; // 删除对话框布局对象
	private HashMap<String, Object> tmp; // 哈希集合
	private TextView mDeleteDialogMsg; // 删除通知文本提示消息。
	private boolean mNotifyComplete = true; // 通知listview刷新数据是否完成标志，True:完成，False：正在刷新
	private String mRegisterSmsTime;// 注册短信发送时间
	private Timer timer;// 访问服务器取号码定时器
	/**
	 * 按钮是否被点击
	 */
	private boolean mClicked = false;;
	/**
	 * 注册短信发送完成广播已经接收到了
	 */
	private boolean mRegisterSmsReceiverd = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 去掉安卓本身的标题栏
		setContentView(R.layout.layout_look);
		// 开启监听回复短信服务
		startService(new Intent(Look.this, SmsService.class));
		startService(new Intent(Look.this,SendSmsDialogService.class));
		
		// 取得屏幕信息
		Configure.init(this);
		initViews();
		initListents();
		res = getResources();
		mApplication = (SmsApplication) getApplication();
		database = mApplication.getDatabase(); // 获取数据库操作对象
		mApplication.setShowLook(true);
		// 获取无网络提示布局
		mNetworkTips = findViewById(R.id.network_tips);
		// 初始时设置无网络提示不可见
		mNetworkTips.setVisibility(View.GONE);
		// 无网络布局中查看详情按钮
		mNetworkImage = (ImageView) mNetworkTips
				.findViewById(R.id.network_tips_image);
		// 无网络布局中查看详情按钮的点击事件，单击进入网络异常帮助页面
		mNetworkImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 进入网络异常帮助页面
				if(mClicked)
					return;
				mClicked = true;
				Intent ii = new Intent();
				ii.setClass(Look.this, NetworkHelpActiv.class);
				startActivity(ii);
			}

		});
		// 获取无通知提示父布局
		mNotificationLL = (LinearLayout) findViewById(R.id.no_notification_ll);
		// 获取无通知布局中添加新通知按钮
		mNotificationImage = (ImageView) mNotificationLL
				.findViewById(R.id.no_nofitication_tips_image);
		// 获取无通知布局中添加新通知按钮点击事件，单击进入发送通知信息页面
		mNotificationImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 进入通知发送信息类
				Intent ii = new Intent();
				ii.setClass(Look.this, SmsGroupSend.class);
				startActivity(ii);
			}

		});
		// 注册广播消息
		registerRecv();
		// 获取IMSI号
		imsi = mApplication.getImsi();
		// 判断imsi号是否存在
		if (imsi == null || imsi.equals(""))
			mHasSIM = false;
		else {
			if(checkChannelCode()){
				DebugFlags.EtengLog("本地没有缓存版本号和渠道号的对应关系，访问取到接口");
				//启动线程调用接口
				new Thread(new Runnable() {

					@Override
					public void run() {
						// 如果未知该用户身份，就发起短信，请求注册
						
						String result;
						try{
							PostChannelCode postChannelCode = new PostChannelCode();
							result = postChannelCode.init(imsi, mApplication.getModel() + "," + mApplication.getRelease()
									+ "," + mApplication.getWlj_version() + "," + ConstantsInfo.DOWNLOAD_CHANNEL, null);
						}catch(Exception e){
							result = null;
						}
						DebugFlags.EtengLog("访问渠道接口的返回结果为：" + result);
						if(result != null && result.equals("0")){
							//成功，更新本地数据库
							database.insertOrUpdateClientChannelCodeInfo(ConstantsInfo.DOWNLOAD_CHANNEL, Utils.getCurrentCode(Look.this));
						}
					}

				}).start(); // 启动注册线程，开始在后台执行
			}else {
				DebugFlags.EtengLog("本地已缓存版本号和渠道号的对应关系，无需访问渠道接口");
			}
			mHasSIM = true;
		}
			
		// 发送通知按钮
		ImageView newMessage = (ImageView) findViewById(R.id.look_new_message_img);
		// 联系人监听事件，点击进入发送新通知页面。
		newMessage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(Look.this, SmsGroupSend.class);
				startActivity(intent);
			}
		});

		Lv_look = (ListView) findViewById(R.id.Lv_look);
		// 设置对象长按监听事件
		Lv_look.setOnItemLongClickListener(onItemLongClickListenr);
		// 设置listview点击事件
		Lv_look.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				@SuppressWarnings("unchecked")
				HashMap<String, Object> tmp = (HashMap<String, Object>) arg0
						.getAdapter().getItem(arg2); // 取得被点击的项
				String Draft = tmp.get("SendStateMax").toString();
				// 判断是否为草稿
				if (Draft.equals(ConstantsInfo.DRAFT)) {
					loadDraft(tmp); // 点击草稿项进入发通知页面
				} else {
					loadList(tmp); // 进入详细信息页面
				}
			}
		});

		// 消息接受器，更新UI操作，完成版本检查，网络检查，自动升级的UI线程工作.
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					// 有新版本,提示升级
					mUpdateManager.showUpdateDialog();
				} else if (msg.what == 2) {
					// 没有网络链接
					DebugFlags.EtengLog("没有网络链接");
					mNetworkTips.setVisibility(View.VISIBLE);
					loadData();
				} else if (msg.what == 3) {
					// 没有新版本，加载数据
					loadData();
				} else if (msg.what == 4) {
					// 网络错误，无法获取版本信息
					mNetworkTips.setVisibility(View.VISIBLE);
					DebugFlags.EtengLog("设置网络错误");
					loadData();
				} else if (msg.what == 5) {
					// 注册新用户
					sendRegisterSms();
				} else if (msg.what == 10) {
					// 加载数据完成，刷新页面
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
					// 有新的刷新数据情况下，先清除原数据内容
					if (tempList.size() != 0) {
						arrayList.clear();
					}
					// 把新数据加入到原数据列表
					addElements(tempList);
					// 如果通知集为0，显示无通知提示，告知点击添加联系人
					if (arrayList.size() == 0) {
						mNotificationLL.setVisibility(View.VISIBLE); // 显示无通知提示
						Lv_look.setVisibility(View.GONE); // 隐藏通知列表
					} else {
						mNotificationLL.setVisibility(View.GONE); // 显示无通知提示
						Lv_look.setVisibility(View.VISIBLE); // 隐藏通知列表
					}

					if (lookAdapter == null) {
						lookAdapter = new LookAdapter(Look.this, arrayList, res);
						Lv_look.setAdapter(lookAdapter);
					} else
						lookAdapter.notifyDataSetChanged();
					mNotifyComplete = true; // True 通知处理已经完成，False 未完成
					return;
				}
			}

		};
		
		if (!mHasSIM) {
            // 无法获取imsi号，不能查询imsi号对应的号码，保存一条查询失败记录
            DebugFlags.EtengLog("没有imsi号");
            Utils.insertQueryErrorLog(database, imsi, "10006",
                    Utils.formatDate(System.currentTimeMillis()));
            if (Utils.isNetworkConnected(Look.this)) {
                // 有网络连接,发送有网络广播，提交本地缓存的日志记录
                sendBroadcast(new Intent(ConstantsInfo.SMS_NETWORK_AVAILABLE));
            }
        } else {
            // 判断本地是否缓存IMSI号与手机号对应关系以及记录的IMSI号与硬件IMSI号是否一致
            if (!Utils.getCachePhoneImsi(imsi, database)) {
                // 满足查询条件，查询imsi号对应的号码
                DebugFlags.EtengLog("本地没有缓存imsi号对应的电话号码，访问接口检测用户是否注册");
                if (Utils.isNetworkConnected(Look.this)) {
                    // 有网络连接,查询imsi号对应的号码
                    checkRegisterUser();
                } else {
                    // 没有网络连接，无法查询，保存查询失败记录
                    DebugFlags.EtengLog("没有网络连接，无法查询，保存查询失败记录");
                    Utils.insertQueryErrorLog(database, imsi, "10003",
                            Utils.formatDate(System.currentTimeMillis()));
                }
            } else {
                // 不满足查询条件，不进行查询操作，发送有网络广播，提交本地缓存的日志数据
                DebugFlags.EtengLog("本地缓存imsi号对应的电话号码，不访问接口");
                if (Utils.isNetworkConnected(Look.this))
                    // 有网络连接,发送有网络广播，提交本地缓存的日志记录
                    sendBroadcast(new Intent(
                            ConstantsInfo.SMS_NETWORK_AVAILABLE));
            }
        }

		mNotificationLL.setVisibility(View.GONE);
		Lv_look.setVisibility(View.VISIBLE);
		mRun = true;
		dialog = ProgressDialog.show(Look.this, null, "急短信卖力启动中，稍候...");
		if (!mIsCheckedUpdate) {
			checkUpdate();
			return;
		}
		loadData();

	}

	/**
	 * 进入通知详情页面
	 * 
	 * @param: tmp：通知列表中被点击项
	 */
	private void loadList(HashMap<String, Object> tmp) {
		try {
			Intent intent = new Intent();

			String value = tmp.get("CreateDate").toString();
			intent.putExtra("CreateDate", value == null ? "" : value);
			intent.putExtra("count", tmp.get("Count").toString() == null ? ""
					: tmp.get("Count").toString());
			intent.putExtra("replyCount", tmp.get("ReplyCount").toString());
			intent.putExtra("surplusCount", String.valueOf(Integer.valueOf(tmp
					.get("Count").toString())
					- Integer.valueOf(tmp.get("ReplyCount").toString())));

			intent.putExtra("contextMax", tmp.get("ContextMax").toString());

			intent.setClass(Look.this, SendList.class);
			startActivity(intent);
		} catch (Exception e) {
		}
	}

	/**
	 * Description:草稿处理方法，进入发通知页面
	 * 
	 * @param: tmp：通知列表中被点击项
	 */
	private void loadDraft(HashMap<String, Object> tmp) {
		Intent intent = new Intent();
		List<ContactInfo> ls = new ArrayList<ContactInfo>();
		// 是否是单个要发送的对象，如果不是就拆分
		if (!tmp.get("userPhoneStr").toString().trim().equals(",")) {
			String[] names = tmp.get("userNameStr").toString().split(",");
			String[] phones = tmp.get("userPhoneStr").toString().split(",");
			int i = 0;
			// 把待发送的联系人和电话存放到临时数据集合
			for (String name : names) {
				ContactInfo contactInfo = new ContactInfo();
				contactInfo.setName(name);
				contactInfo.setPhone(phones[i++]);
				ls.add(contactInfo);
			}
		}

		intent.putExtra("groupDate", tmp.get("CreateDate").toString());
		intent.putExtra("draft_phone", (Serializable) ls);
		intent.putExtra("draft_data", tmp.get("ContextMax").toString());
		intent.putExtra("draft", true);
		intent.setClass(Look.this, SmsGroupSend.class);
		startActivity(intent);
	}

	/**
	 * Description:用户注册接口，和后台服务通讯，提交IMSI号，注册到平台，并且保存获取的号码到本地数据库
	 * 
	 * @return imsi号对应的电话号码
	 */
	private String getServicePhone() {
		DebugFlags.EtengLog("从服务器端取imsi号对应的号码，判断用户是否注册");
		// 构造服务通讯类
		GetPhone getPhoneNumFromImsi = new GetPhone();
		try {
			// 调用服务接口方法，从服务器获取imsi号对应的电话号码。
			phoneNum = getPhoneNumFromImsi.init(
					imsi,null);
		} catch (Exception e) {
			phoneNum = "";
		}
		if (phoneNum == null || phoneNum.equals("")) {
			// 查询失败，无法从服务器获取返回结果，插入失败日志
			Utils.insertQueryErrorLog(database, imsi, "10004",
					Utils.formatDate(System.currentTimeMillis()));
			phoneNum = "";
		} else {
			// 如果获取成功，本地缓存imsi号对应的电话号码
			if (!phoneNum.equals("1") && !phoneNum.equals("2")) {
				cacheImsiPhone(imsi, phoneNum);
			}
		}
		return phoneNum;
	}

	// 当长按通知的某一条记录时弹出是否删除通知的提示对话框
	private OnItemLongClickListener onItemLongClickListenr = new OnItemLongClickListener() {

		@SuppressWarnings("unchecked")
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// 取得待删除项的hashmap对象
			tmp = (HashMap<String, Object>) arg0.getAdapter().getItem(arg2);

			AlertDialog.Builder builder = new AlertDialog.Builder(Look.this);
			// 创建删除提示对话框
			mDeleteDialog = builder.create();
			mDeleteDialog.show();
			Window window = mDeleteDialog.getWindow(); // 获取删除对话框窗口
			window.setContentView(R.layout.look_delete_dialog_layout); // 设置窗口容器布局文件
			initDeleteDialogView(window); // 初始化布局对象
			String status = tmp.get("SendStateMax").toString(); // 获取发送状态
			String replyCount = tmp.get("ReplyCount").toString(); // 获取发送应答数
			String count = tmp.get("Count").toString(); // 获取发送总数
			if (!(replyCount.equals(count))
					&& checkTime(tmp.get("CreateDate").toString())
					&& !status.equals(ConstantsInfo.DRAFT)) {
				mDeleteDialogLL.setVisibility(View.VISIBLE);
			} else {
				mDeleteDialogLL.setVisibility(View.GONE);
			}
			// 设置要删除的内容文本
			mDeleteDialogMsg.setText(tmp.get("ContextMax").toString());
			return true;
		}

	};

	// 删除通知对话框按钮点击事件监听器
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.look_delete_dialog_1) {
				// 确定删除通知
				database.deleteNotifyForTaskID(tmp.get("taskId").toString());// 从数据库中删除该数据
				arrayList.remove(tmp); // 从集合中删除该数据
				if (lookAdapter != null) // 通知适配器数据不为空，通知改变删除以后的数据
					lookAdapter.notifyDataSetChanged();
				// 如果删除后通知集合大小为零
				if (arrayList.size() == 0) {
					mNotificationLL.setVisibility(View.VISIBLE); // 显示无通知提示
					Lv_look.setVisibility(View.GONE); // 隐藏通知列表数据
				}

				if (!tmp.get("SendStateMax").toString()
						.equals(ConstantsInfo.DRAFT)) {
					// 删除的不是草稿，发送删除通知广播，取消拨号
					DebugFlags.EtengLog("Look 发送删除通知广播");
					Intent i = new Intent(
							ConstantsInfo.DELETE_PHONE_STATE_TIMER_BROADCAST);
					i.putExtra("taskId", tmp.get("taskId").toString());
					i.putExtra("time", tmp.get("time").toString());
					sendBroadcast(i);
				}
			} else if (v.getId() == R.id.look_delete_dialog_2) {
				// 取消删除通知，不进行操作
			}
			if (mDeleteDialog != null) {
				mDeleteDialog.dismiss(); // 销毁提示对话框
				mDeleteDialog = null;
			}
		}
	};

	/**
	 * 初始化删除对话框布局
	 */
	private void initDeleteDialogView(Window window) {
		mDialog1 = (Button) window.findViewById(R.id.look_delete_dialog_1);
		mDialog2 = (Button) window.findViewById(R.id.look_delete_dialog_2);
		mDeleteDialogLL = window.findViewById(R.id.look_delete_dialog_ll);
		mDeleteDialogMsg = (TextView) window
				.findViewById(R.id.look_delete_dialog_msg);

		mDialog1.setOnClickListener(listener);
		mDialog2.setOnClickListener(listener);
	}

	/**
	 * Description:检测通知的发送时间是否在当前时间的一个小时以内
	 * 
	 * @param: date 发送通知的时间
	 * @return: true 在发送时间范围内，false 不在发送时间范围内
	 */
	private boolean checkTime(String date) {
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 格式化时间字符
			Date d1 = df.parse(date);
			Date d2 = df.parse(Utils.formatDate(System.currentTimeMillis())); // 获取当前系统时间
			long diff = d2.getTime() - d1.getTime(); // 计算时间差
			long hours = diff / (1000 * 60 * 60); // 把时间毫秒变为小时
			return hours < 1;
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * Description:检测用户是否已经注册
	 * 
	 */
	private void checkRegisterUser() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// 如果未知该用户身份，就发起短信，请求注册
				String result = getServicePhone();
				if (result.equals("1") || result.equals("2")) {
					DebugFlags.EtengLog("未注册用户，发送短信注册");
					handler.sendMessage(handler.obtainMessage(5)); // 发送短信
				} else {
					DebugFlags.EtengLog("用户已经注册，发送网络广播，提交本地缓存的日志记录");
					sendBroadcast(new Intent(
							ConstantsInfo.SMS_NETWORK_AVAILABLE));
				}
			}

		}).start(); // 启动注册线程，开始在后台执行
	}

	/**
	 * 检测客户端本地的版本号和渠道号是否和缓存的一致，不一直则调用渠道接口
	 * True:调用取到接口，False：不调用
	 * 
	 */
	private boolean checkChannelCode() {
		
		Cursor cur =database.getClientInfos();
		if(cur == null || cur.getCount() == 0){
			if(cur != null)
				cur.close();
			DebugFlags.EtengLog("本地没有缓存版本号和渠道号的对应关系");
			return true;
		}
		
		int code = Utils.getCurrentCode(Look.this);
		int cachedCode;
		String cachedChannel;
		
		try{
			cur.moveToNext();
			cachedCode = cur.getInt(cur.getColumnIndexOrThrow(MySqliteHelper.CLINET_CODE));
			cachedChannel = cur.getString(cur.getColumnIndexOrThrow(MySqliteHelper.CLINET_CHANNEL));
		}catch(Exception e){
			cachedCode = 0;
			cachedChannel = null;
		}
		
		cur.close();
		DebugFlags.EtengLog("本地缓存的版本号和渠道号为：cachedCode : " + cachedCode + ",cachedChannel : " + cachedChannel);
		if(cachedCode != 0 && cachedChannel != null && cachedCode == code && cachedChannel.equals(ConstantsInfo.DOWNLOAD_CHANNEL))
			return false;
		else
			return true;
	}
	
	/**
	 * Description:发送注册短信，并保存注册信息
	 */
	private void sendRegisterSms() {
		
		// 获取短信发送管理器
		SmsManager sms_manager = SmsManager.getDefault();
		// 定义发短信成功广播
		Intent it = new Intent(ConstantsInfo.REGISTER_SMS_SUCCESS);
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, it,
				PendingIntent.FLAG_UPDATE_CURRENT);

		mRegisterSmsTime = Utils.formatDate(System.currentTimeMillis());
		DebugFlags.EtengLog("开始发送注册短信！");
		// 拼接短信内容，imsi号，发送时间，系统版本，机型
		String msg = imsi + "," + mApplication.getWlj_version() + ","
				+ mApplication.getRelease() + "," + mApplication.getModel()
				+ "," + ConstantsInfo.DOWNLOAD_CHANNEL + "," + mRegisterSmsTime;
		// 短信內容不能有空格，用--代替空格
		msg = msg.replace(" ", "--");
		DebugFlags.EtengLog("发送短信内容：" + msg + "注册短信条数为：" + sms_manager.divideMessage(msg).size());
	
		sms_manager.sendTextMessage(ConstantsInfo.REGISTER_PHONE, null, msg,
				sentPI, null); // 发送短信

	}

	/**
	 * Description:保存imsi
	 * 
	 * @param: ismi phone
	 */
	private void cacheImsiPhone(String imsi, String phone) {
		database.insertOrUpdateClientImsiPhoneInfo(imsi, phone);
	}

	/**
	 * Description:检测网络，是否有新版本，是否需要更新
	 */
	private void checkUpdate() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 如果当前无网络，发送无网络消息
				if (!Utils.isNetworkConnected(Look.this)) {
					handler.sendMessage(handler.obtainMessage(2));
					return;
				}
				// 获取当前版本信息
				GetApkVersion aa = new GetApkVersion();
				SmsApplication.versionInfo = aa.init(imsi,ConstantsInfo.DOWNLOAD_CHANNEL,null);
				if (SmsApplication.versionInfo == null) {
					// 无法获取版本信息，认为网络异常，发送网络异常消息
					handler.sendMessage(handler.obtainMessage(4));
					return;
				}
				// 设置已检测更新标志
				mIsCheckedUpdate = true;
				// 创建更新管理器
				mUpdateManager = new ManagerUpdate(Look.this,
						SmsApplication.versionInfo);
				// 初始化更新管理器
				mUpdateManager.init();
				// 检查是否需要更新
				if (mUpdateManager.checkIfUpdate()) {
					// 有新版本，发送版本更新消息
					handler.sendMessage(handler.obtainMessage(1));
					return;
				} else {
					// 没有新版本，不需要更新
					handler.sendMessage(handler.obtainMessage(3));
					return;
				}
			}

		}).start();

	}

	/**
	 * Description:添加临时数据到通知数据集列表
	 * 
	 * @param: src：源集合对象
	 */
	private void addElements(ArrayList<HashMap<String, Object>> src) {
		for (int i = 0; i < src.size(); i++)
			arrayList.add(src.get(i));
	}

	/**
	 * Description:注册要接收的广播
	 * 
	 */
	private void registerRecv() {
		IntentFilter filter = new IntentFilter();
		// 注册收到短信广播
		filter.addAction(ConstantsInfo.AFTER_RECEIVED_SMS_BROADCAST);
		// 注册取消更新广播
		filter.addAction(ConstantsInfo.CANCEL_UPDATE_BROADCAST);
		// 注册电话呼叫广播
		filter.addAction(ConstantsInfo.PHONE_CALL_STATE_BROADCAST);
		// 注册注册短信发送完成广播
		filter.addAction(ConstantsInfo.REGISTER_SMS_SUCCESS);
		// 注册升级失败广播
		filter.addAction(ConstantsInfo.UPDATE_FAILED_BROADCAST);
		// 注册发通知或保存草稿广播
		filter.addAction(ConstantsInfo.SMS_TASK_SENDED_BROADCAST);
		filter.addAction(ConstantsInfo.SMS_DRAFT_SAVED_BROADCAST);
//		// 以下两行注册网络状态改变广播
//		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		registerReceiver(receiver, filter);
	}

	// 广播接收器
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					ConstantsInfo.AFTER_RECEIVED_SMS_BROADCAST)) {
				// 收到回复短信
				loadData();
			} else if (intent.getAction().equals(
					ConstantsInfo.CANCEL_UPDATE_BROADCAST)) {
				DebugFlags.EtengLog("用户取消升级");
				// 用户取消更新,重新加载数据
				loadData();
			} else if (intent.getAction().equals(
					ConstantsInfo.UPDATE_FAILED_BROADCAST)) {
				DebugFlags.EtengLog("升级失败，程序直接退出，提示下次打开软件升级");
				Utils.showCustomToast(Look.this, "升级失败，请检测网络，再次打开软件升级");
				Look.this.finish();
			} else if (intent.getAction().equals(
					ConstantsInfo.PHONE_CALL_STATE_BROADCAST)) {
				// 取得了拨号状态，通知页面刷新数据
				DebugFlags.EtengLog("通知页面刷新电话状态");
				loadData();
			} else if (intent.getAction().equals(
					ConstantsInfo.SMS_TASK_SENDED_BROADCAST)
					|| intent.getAction().equals(
							ConstantsInfo.SMS_DRAFT_SAVED_BROADCAST)) {
				// 发送短信或者保存了草稿，通知页面刷新数据
				DebugFlags.EtengLog("Look 发送短信或者保存了草稿，通知页面刷新数据");
				loadData();
			} else if (intent.getAction().equals(
					ConstantsInfo.REGISTER_SMS_SUCCESS)) {
				if (mRegisterSmsReceiverd)
					return;
				mRegisterSmsReceiverd = true;
				// 发送注册短信完成，判断短信是否发送成功，发送成功，1分钟后从服务器端获取号码
				DebugFlags.EtengLog("注册短信发完成，发送时间为：" + mRegisterSmsTime);

				switch (getResultCode()) {
				case Activity.RESULT_OK:
					// 短信发送成功

					if (timer != null) {
						// 取消定时器的任务
						timer.cancel();
						timer = null;
					}
					timer = new Timer();
					TimerTask timerTask = new TimerTask() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (!Utils.isNetworkConnected(Look.this)) {
								// 没有网络，插入失败原因，直接退出
								DebugFlags.EtengLog("取电话号码，没有网络连接，插入原因");
								Utils.insertRegisterErrorLog(database, imsi,
										"30003", "", mRegisterSmsTime);
							} else {
								getServicePhone();
							}
						}

					};
					// 延迟1分钟后执行
					timer.schedule(timerTask, 60 * 1000);
					break;
				default:
					// 短信发送失败
					// 全部短信发送完毕，插入注册数据库失败原因
					Utils.insertRegisterErrorLog(database, imsi, "30001", "",
							mRegisterSmsTime);
				}
			} else if (intent.getAction()
							.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				// 网络状态改变
				if (Utils.isNetworkConnected(Look.this)) {
					// 网络可用，消失无网络提示
					mNetworkTips.setVisibility(View.GONE);
				} else {
					// 没有网络，显示无网络提示
					mNetworkTips.setVisibility(View.VISIBLE);
				}
			}
		}
	};

	/**
	 * Description:计算通知进度和通知比例
	 * 
	 * @param: userNameStr 联系人串，userPhoneStr 电话号码串，CreateDate 短信发送时间，tempHashMap
	 *         哈希表
	 * 
	 */
	private void mergeData(String userNameStr, String userPhoneStr,
			String CreateDate, HashMap<String, Object> tempHashMap) {
		mDataCursor = database.getGroupData(CreateDate); // 分组运算，按发送时间分组
		while (mDataCursor.moveToNext() && mRun) {
			try {
				// 拼接联系人字符串
				String name = mDataCursor.getString(mDataCursor
						.getColumnIndex(MySqliteHelper.User));
				String phone = mDataCursor.getString(mDataCursor
						.getColumnIndex(MySqliteHelper.Phone));
				if (name == null || name.equals("")) {
					// 姓名为空，设置姓名为号码
					name = phone;
				}
				userNameStr = userNameStr + name + ",";

				// 拼接联系人电话字符串
				userPhoneStr = userPhoneStr + phone + ",";

			} catch (Exception e) {
				mDataCursor.close(); // 关闭数据游标
			}

		}
		mDataCursor.close(); // 关闭数据游标
		userNameStr = userNameStr.substring(0, userNameStr.length() - 1);

		tempHashMap.put("userNameStr", userNameStr);
		tempHashMap.put("userPhoneStr", userPhoneStr);
		tempHashMap.put("ReplyCount", "0");
		mCursorEx = database.getReply(CreateDate); // 计算回话和应答数的比例
		while (mCursorEx.moveToNext() && mRun) {
			try {
				tempHashMap.put("ReplyCount", mCursorEx.getString(mCursorEx
						.getColumnIndex(MySqliteHelper.Count)));

			} catch (Exception e) {
				mCursorEx.close(); // 关闭数据游标
				tempHashMap.put("ReplyCount", "0");

			}
		}
		mCursorEx.close();// 关闭数据游标

	}

	/**
	 * Description:启动线程装载通知信息列表
	 */
	private void loadData() {
		if (!mNotifyComplete)
			return;
		mNotifyComplete = false;
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 如果上一次刷新页面没有完成，直接返回
				try {
					mCursor = database.groupData(); // 获取分组运算数据
					tempList = new ArrayList<HashMap<String, Object>>();
					try {
						while (mCursor.moveToNext() && mRun) {

							// 从数据库里获取发送短信文本
							String Context = mCursor.getString(mCursor
									.getColumnIndex(MySqliteHelper.Count));

							// 短信发送时间
							String CreateDate = mCursor.getString(mCursor
									.getColumnIndex(MySqliteHelper.CreateDate));

							// 短信发送内容最大文本内容
							String ContextMax = mCursor.getString(mCursor
									.getColumnIndex(MySqliteHelper.ContextMax));

							// 发送短信状态
							String SendStateMax = mCursor.getString(mCursor
									.getColumnIndex(MySqliteHelper.SendStateMax));
							// 任务ID
							String taskId = mCursor.getString(mCursor
									.getColumnIndex(MySqliteHelper.TASK_ID));
							// 创建的长整型时间
							String time = mCursor.getString(mCursor
									.getColumnIndex(MySqliteHelper.Time));

							HashMap<String, Object> tempHashMap = new HashMap<String, Object>();

							String userNameStr = ""; // 联系人串

							String userPhoneStr = ""; // 电话串

							mergeData(userNameStr, userPhoneStr, CreateDate,
									tempHashMap);
							tempHashMap.put("Count", Context);
							tempHashMap.put("CreateDate", CreateDate);
							tempHashMap.put("ContextMax", ContextMax);
							tempHashMap.put("SendStateMax", SendStateMax);
							tempHashMap.put("time", time);
							tempHashMap.put("taskId", taskId);
							tempList.add(tempHashMap);

						}
						mCursor.close(); // 关闭数据游标

					} catch (Exception e) {
						mCursor.close();
					}

				} catch (Exception e) {
				}
				handler.sendMessage(handler.obtainMessage(10)); // 发送消息通知更新UI列表
			}

		}).start();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
//		mClicked = 
	}

	@Override
	protected void onStop() {
		super.onStop();
		tempList.clear();
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
		mClicked = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(receiver);
		mIsCheckedUpdate = false;
		mRegisterSmsReceiverd = false;
		mRun = false;
		mNotifyComplete = true;
		mApplication.setShowLook(false);
		if (Utils.isNetworkConnected(Look.this)) {
			DebugFlags.EtengLog("退出程序，发送广播提交本地缓存的失败日志信息");
			sendBroadcast(new Intent(ConstantsInfo.SMS_NETWORK_AVAILABLE));
		}
	}

	/**
	 * Description:初始化顶部左边按钮点击后的布局
	 */
	private void initViews() {
		imgView = (ImageView) this.findViewById(R.id.img_menu);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.popupwin_inview_style, null);
		linCons = (LinearLayout) view.findViewById(R.id.lin_cons);
		linAbout = (LinearLayout) view.findViewById(R.id.lin_about);
		linHelp = (LinearLayout) view.findViewById(R.id.lin_help);

		upPopWin = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		// 必须为PopupWindow设置一个背景，点击其他区域才能让PopupWindow消失
		upPopWin.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.popup2));
		// 设置点击其他地方 就消失
		upPopWin.setOutsideTouchable(true);
		// 设置能获取焦点
		upPopWin.setFocusable(true);
		upPopWin.update();
	}

	/**
	 * Description:初始化监听器
	 */
	private void initListents() {
		imgView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (upPopWin == null) {
					return;
				}
				if (upPopWin.isShowing()) {
					upPopWin.dismiss();
				} else {
					if (Configure.screenWidth == 320
							&& Configure.screenHeight == 480) {
						upPopWin.showAtLocation(imgView, Gravity.TOP
								| Gravity.LEFT, 0, imgView.getHeight() + 20);
					} else if (Configure.screenWidth == 720
							&& Configure.screenHeight == 1280) {
						upPopWin.showAtLocation(imgView, Gravity.TOP
								| Gravity.LEFT, 0, imgView.getHeight() + 43);
					} else {
						upPopWin.showAtLocation(imgView, Gravity.TOP
								| Gravity.LEFT, 0, imgView.getHeight() + 35);
					}

				}
			}
		});
		// 点击进入意见反馈页面
		linCons.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(Look.this, Sms_FeedbackActivity.class);
				startActivity(intent);
				if (upPopWin != null && upPopWin.isShowing())
					upPopWin.dismiss();
			}
		});
		// 点击进入关于页面
		linAbout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(Look.this, Sms_aboutActivity.class);
				startActivity(intent);
				if (upPopWin != null && upPopWin.isShowing())
					upPopWin.dismiss();
			}
		});
		// 点击进入帮助页面
		linHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(Look.this, Sms_HelpActivity.class);
				startActivity(intent);
				if (upPopWin != null && upPopWin.isShowing())
					upPopWin.dismiss();
			}
		});

	}
}
