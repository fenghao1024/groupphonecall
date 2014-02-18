package com.eteng.world;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Delayed;
import java.util.jar.Attributes.Name;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wljsms.adapter.MeetingStatusListViewAdapter;
import com.wljsms.config.AppConstant;
import com.wljsms.config.MeetingSoap;
import com.wljsms.config.MyHttpClient;
import com.wljsms.info.ContactInfo;
import com.wljsms.tools.Base64Tool;
import com.wljsms.tools.DesEncrypter;
import com.wljsms.tools.JsonParamMaker;
import com.wljsms.util.GlobalData;
import com.wljsms.util.Utils;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.opengl.Visibility;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


@SuppressLint("HandlerLeak")
class DefaultPhoneStateListener extends PhoneStateListener {
	String strIncomingNumber = ""; // 来电号码
	Context context; // 上下文
	static private WindowManager wm; // 窗口管理器
	static WindowManager.LayoutParams mLayoutParams;// 布局参数
	private float mTouchStartX; // 按下时的触摸位置
	private float mTouchStartY; //
	private float x; // 当前的触摸位置
	private float y; //
	private float StartX; // 按下时的窗口位置
	private float StartY; //
	static View viewIncomingPhone; // 来电视图
	public static final String ACTION_UPDATE_CALLING_VIEW = "shengfang.action.UPDATE_CALLING_VIEW";
	List<Map<String, String>> meetingStatusList;
	
	private static final int CONNECT_FAILD = 100000;
	private static final int CONNECT_SUCCESS = 111111;
	
	
	MeetingStatusListViewAdapter meetingStatusListViewAdapter;

	public Handler handler = new Handler()// 用于处理关闭来电窗口
	{
		@Override
		public void handleMessage(Message msg) {
			CloseIncomingPhoneView();
		}
	};

	public DefaultPhoneStateListener(Context context) {
		this.context = context;
		context.registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if ((viewIncomingPhone != null) && (wm != null)
						&& (mLayoutParams != null)) {
					// 更新浮动窗口位置参数
					mLayoutParams.gravity = Gravity.TOP;
					wm.updateViewLayout(viewIncomingPhone, mLayoutParams);

				}
			}
		}, new IntentFilter(ACTION_UPDATE_CALLING_VIEW));
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		super.onCallStateChanged(state, incomingNumber);

		strIncomingNumber = incomingNumber;

		switch (state) {
		case TelephonyManager.CALL_STATE_RINGING:


				//CreateIncomingPhoneView();

			break;

		case TelephonyManager.CALL_STATE_IDLE:
			Log.d("CALL", "CALL_STATE_IDLE :" + strIncomingNumber);
			CloseIncomingPhoneView();
			break;

		case TelephonyManager.CALL_STATE_OFFHOOK:
			Log.d("CALL", "CALL_STATE_OFFHOOK :" + strIncomingNumber);
			CreateIncomingPhoneView();
			break;
		}
	}


	public void CreateIncomingPhoneView() {

		if (viewIncomingPhone == null)// 不重复创建，一个反复用
		{
			viewIncomingPhone = LayoutInflater.from(context).inflate(
					R.layout.toast_call_coming, null);
		}
//		TextView text = (TextView) viewIncomingPhone
//				.findViewById(R.id.toast_text);
		Button hangUpButton = (Button)viewIncomingPhone.findViewById(R.id.hang_up_button);
		ListView meetingStatusListView = (ListView)viewIncomingPhone.findViewById(R.id.meeting_status_listview);
				

		
		meetingStatusListViewAdapter = new MeetingStatusListViewAdapter(viewIncomingPhone.getContext());
		meetingStatusListView.setAdapter(meetingStatusListViewAdapter);
		meetingStatusListViewAdapter.SetSourceData(null);
		
		hangUpButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				hangUpButtonClick(v);
			}
		});

//		text.setText("来电信息：" + "\n　" + strIncomingNumber);

		// 获取WindowManager
		if (wm == null)
			wm = (WindowManager) context.getSystemService("window");
		Log.d("CALL", "wm :" + wm.toString());
		if (mLayoutParams == null)
			mLayoutParams = new WindowManager.LayoutParams();
		mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		mLayoutParams.windowAnimations = 0;
		mLayoutParams.gravity = Gravity.TOP;
		//mLayoutParams.alpha = 0.9f;
		mLayoutParams.width = LayoutParams.WRAP_CONTENT;
		mLayoutParams.height = LayoutParams.WRAP_CONTENT;
		mLayoutParams.format = PixelFormat.TRANSLUCENT;

		try {
			wm.addView(viewIncomingPhone, mLayoutParams);
			//handler.sendEmptyMessageDelayed(0, 30000);
		} catch (Exception e) {
			Log.e("CALL", "addView:" + e.toString());
		}
		Log.d("CALL", "addView :" + wm);
		viewIncomingPhone.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 获取相对屏幕的坐标，即以屏幕左上角为原点

				x = event.getRawX();
				y = event.getRawY();
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					StartX = mLayoutParams.x;
					StartY = mLayoutParams.y;
					// 获取相对View的坐标，即以此View左上角为原点
					mTouchStartX = x;
					mTouchStartY = y;
					break;
				case MotionEvent.ACTION_MOVE:
					UpdateIncomingPhoneView();
					break;
				case MotionEvent.ACTION_UP:
					UpdateIncomingPhoneView();
					mTouchStartX = 0;
					mTouchStartY = 0;
					break;
				}
				return true;
			}
		});
		checkMeetingStatus();
	}
	
	private String fineNameByPhonenumInContactList(List<ContactInfo> list, String PhoneNum)
	{
		String nameString = "";
		for(int i = 0; i < list.size(); i++)
		{
			if(PhoneNum.equals(list.get(i).getPhone()))
			{
				nameString = list.get(i).getName();
			}
			else {
				nameString = "";
			}
		}
		return nameString;	
	}
	
	public void hangUpButtonClick(View v)
	{
		// hang up the phone
		
		//Utils.showCustomToast(viewIncomingPhone.getContext(), "会议结束！");
		
		closeMeeting();

		
	}
	
	private void closeMeeting()
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				String param = "{\"meetingid\":" + GlobalData.getInstance().getMetingDetails().get("meetingid") + "}";
				String base64Param = Base64Tool.encode(param);
				
				String resultString = MyHttpClient.DoSomething(AppConstant.METHOD_CLOSE_MEETING, base64Param);
				
				Message msg = meetingHandle.obtainMessage();
				
				msg.what = AppConstant.MSG_CLOSE_MEETING;
				msg.obj = resultString;
				
			}
		}).start();
		
	}

	public void UpdateIncomingPhoneView() {
		if ((viewIncomingPhone != null) && (wm != null)
				&& (mLayoutParams != null)) {
			// 更新浮动窗口位置参数
			mLayoutParams.x = (int) (StartX + x - mTouchStartX);
			mLayoutParams.y = (int) (StartY + y - mTouchStartY);
			wm.updateViewLayout(viewIncomingPhone, mLayoutParams);

		}
	}

	public void CloseIncomingPhoneView() {
		// 去除浮动窗口 cxy 2013年3月29日 去除 isRunning &&，确保任何状态发出关闭窗口命令都得到执行
		if ((viewIncomingPhone != null) && (wm != null)
				&& (mLayoutParams != null)) {
			try {
				wm.removeView(viewIncomingPhone);
			} catch (Exception e) {
				Log.e("CALL", "removeView:" + e.toString());
			}
		}
//		task.cancel();
	}
	
	private Handler meetingHandle = new Handler(){
		public void handleMessage(Message msg)
		{
			String resultString  = "";
			switch (msg.what) {
			case AppConstant.MSG_GET_MEETING_STATUS:
				resultString = (String)msg.obj;
				
				meetingStatusList = parseGetMeetingStatusResult(resultString);
				if(meetingStatusList == null)
				{
					Log.e("meetingStatusList = parseGetMeetingStatusResult(resultString);", "Something WRONG!!!");
				}
				else
				{
					meetingStatusListViewAdapter.SetSourceData(meetingStatusList);
					meetingStatusListViewAdapter.notifyDataSetChanged();
				}
				
				
				break;
				
			case AppConstant.MSG_JOIN_MEETING:
				resultString = (String)msg.obj;
				
				
				break;
				
			case CONNECT_FAILD:
				Log.e("wtf...", "wtf....");
				
				break;
				
			case AppConstant.MSG_CLOSE_MEETING:
				
				
				viewIncomingPhone.setVisibility(View.GONE);
				timer.cancel();
				
				break;

			default:
				Log.e("shit.....", "Shit.......!!");
				
				break;
			}
			
			
		}
	
	};
	
	private List<Map<String, String>> parseGetMeetingStatusResult_byWebService(String jsonString)
	{
		JSONObject jObject;
		try {
			jObject = new JSONObject(jsonString);
			return parseGetMeetingStatusResult(jObject.getString("responseData"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	private List<Map<String, String>>  parseGetMeetingStatusResult(String jsonMessage)
	{
		
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		
		try {
			
			JSONObject jobj = new JSONObject(jsonMessage);
			
			String jsonResponseData = jobj.getString("responseData");
			String sysStatusString = jobj.getString("status");
			String sysSuccessString = jobj.getString("success");
			
			if(sysSuccessString != "true")
			{
				
				return null;
			}
			
			JSONObject jobj2 = new JSONObject(jsonResponseData);
			String jsonArrayMeetingStateList = jobj2.getString("meetingStateList");
			String logicStatus = jobj2.getString("msg");
			String logicSuccess = jobj2.getString("success");
			
			if(logicSuccess != "true")
			{
				
				return null;
			}
			
			JSONArray jArray = new JSONArray(jsonArrayMeetingStateList);
			
			
			 int l = jArray.length();
			for(int i = 0; i < l; i++)
			{
				JSONObject temp = (JSONObject)jArray.get(i);
				String phone = temp.getString("phone");
				String callStatusCode = temp.getString("callstatus");
				String callStatusStr = temp.getString("callstateStr");
				
				Map<String, String> map = new HashMap<String, String>();
				map.put("phone", phone	);
				map.put("callstatus", callStatusCode);
				map.put("callstatusstr", callStatusStr);
				map.put("name", findNameInContactListByPhoneNum(phone, GlobalData.getInstance().getCallOutContactInfo()));
				
				list.add(map);
				
			}
			
			return list;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			list = null;
			return null;
		}
		
		
	}
	
	private String findNameInContactListByPhoneNum(String phoneNum, List<ContactInfo> list)
	{
		String nameString = "";
		for(int i = 0; i < list.size(); i++)
		{
			ContactInfo ci = list.get(i);
			if(ci.getPhone().equals(phoneNum))
			{
				nameString = ci.getName();
				break;
			}
		}
		
		return nameString;
	}
	
	
	TimerTask task = new TimerTask(){  
		 public void run() {
			 
			 Message msg = meetingHandle.obtainMessage();
			 
			 String httpParamString = GetMeetingStatus_Param_Maker(GlobalData.getInstance().getMetingDetails().get("meetingid"));
			 String paramString = GetMeetingStatus_WebServiceParam_Maker(httpParamString);
				if(paramString == null)
				{
					return;
				}
			 
			 String resultString = MyHttpClient.DoSomething(AppConstant.METHOD_GET_MEETING_STATUS, httpParamString);
			 
//			 MeetingSoap soap = new MeetingSoap();
//			 String resultString = soap.Launch(AppConstant.GET_MEETING_STATUS, paramString);
			 
				if(resultString == null || resultString.equals(""))
				{
					msg.what = CONNECT_FAILD;
					meetingHandle.sendMessage(msg);
					return;
				}
			 
			 
			 msg.obj = resultString;
			 
			 msg.what = AppConstant.MSG_GET_MEETING_STATUS;
			 
			 meetingHandle.sendMessage(msg);


		 }  
	};
	
	private String checkMeetingStatus()
	{
		String resultString = "";
		
		Map<String, String> map = GlobalData.getInstance().getMetingDetails();
		List<ContactInfo> list = GlobalData.getInstance().getCallOutContactInfo();
		
		String meetingId = (String)map.get("meetingid");
		String msg= (String)map.get("msg");
		String isSuccess = (String)map.get("success");
		
		meetingSchedule(1000, 5000);
		
		return resultString;
	}
	
	private String GetMeetingStatus_Param_Maker(String meetingId)
	{
		String resultString = "";
		
		Map<String, String> paramMap = new HashMap<String, String>();

		paramMap.put("userName", "txl");
		paramMap.put("password", "txlpassword");
		String userInfo = JsonParamMaker.MapToJStr(paramMap);
		
		resultString = JsonParamMaker.toCompleteJson("\"meetingid\"" + ":" + meetingId + "," + userInfo);
		
		resultString = Base64Tool.encode(resultString);
		
		return resultString;
	}
	
	private String GetMeetingStatus_WebServiceParam_Maker(String httpParam)
	{
		String resultString = "";
		
	       long now = System.currentTimeMillis(); //微秒级当前时间
	        Calendar c = Calendar.getInstance();
	        c.setTimeInMillis(now);
	 
	        String uuidString = String.valueOf(c.get(Calendar.YEAR)) + String.valueOf((c.get(Calendar.MONTH) + 1)) + String.valueOf(c.get(Calendar.DATE))
	                        + String.valueOf(c.get(Calendar.HOUR)) + String.valueOf(c.get(Calendar.MINUTE))
	                        + String.valueOf(c.get(Calendar.SECOND)) + String.valueOf(c.get(Calendar.MILLISECOND));

			Map<String, String> paramMap = new HashMap<String, String>();
			
			
			paramMap.put("username", "13985561586");
			paramMap.put("password", "123456");
			paramMap.put("meettype", String.valueOf(AppConstant.METHOD_GET_MEETING_STATUS));
			paramMap.put("uuid", uuidString);
			paramMap.put("url",  AppConstant.MEETING_REQPARAM + "=" + httpParam);
			
			String param = JsonParamMaker.MapToJStr(paramMap);
			
			param = JsonParamMaker.toCompleteJson(param);
			
			try {
				resultString = DesEncrypter.encode(param);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				resultString = null;
			}

		
		return resultString;
	}
	
	
	Timer timer;
	public void meetingSchedule(int delay, int duration)
	{
		timer = new Timer(true);
		timer.schedule(task, delay, duration);
	}
	

}