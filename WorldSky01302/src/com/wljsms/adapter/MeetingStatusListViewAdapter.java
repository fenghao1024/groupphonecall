package com.wljsms.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eteng.world.R;
import com.wljsms.config.AppConstant;
import com.wljsms.config.MyHttpClient;
import com.wljsms.info.ContactInfo;
import com.wljsms.tools.Base64Tool;
import com.wljsms.tools.JsonParamMaker;
import com.wljsms.util.GlobalData;

import android.R.integer;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;



public class MeetingStatusListViewAdapter extends BaseAdapter{
	
	List <Map<String, String>> sourceList;
	Context context; 
	int curPos;
	
	public MeetingStatusListViewAdapter(Context _context) {
		// TODO Auto-generated constructor stub
		this.context = _context;
		
	}
	
	public void SetSourceData(List<Map<String, String>> sourcedata)
	{
		this.sourceList = sourcedata;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(sourceList == null)
			return 0;
		return sourceList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		
		return sourceList.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view  = convertView;
		ViewHolder vh = null;
		if(view == null)
		{
			//view = LayoutInflater.from(context).inflate(R.layout., null);
			LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			vh = new ViewHolder();
			
			view = inflater.inflate(R.layout.meeting_status_item, null);
			
			vh.nameTextView = (TextView)view.findViewById(R.id.meeting_status_item_contact_name);
			vh.phoneNumTextView = (TextView)view.findViewById(R.id.meeting_status_item_contact_phone_num);
			vh.meetingStatusButton = (Button)view.findViewById(R.id.meeting_status_item_button);
			
			view.setTag(vh);
		}
		else
		{
			vh = (ViewHolder)view.getTag();
		}
		
		Map<String, String> map = sourceList.get(position);
		vh.nameTextView.setText(map.get("name"));
		vh.phoneNumTextView.setText(map.get("phone"));
		
		final int meetingStatus = Integer.valueOf(map.get("callstatus"));
		
		Drawable shap = null;
		
		switch (meetingStatus) {
		case AppConstant.CONNECTING:
			shap = context.getResources().getDrawable(R.drawable.dial_ing);
			vh.meetingStatusButton.setBackgroundDrawable(shap);
			break;
			
		case AppConstant.HANG_UP_ED:
			
			shap = context.getResources().getDrawable(R.drawable.redial_button_click);
			vh.meetingStatusButton.setBackgroundDrawable(shap);
			break;

		case AppConstant.TALKING:
			
			shap = context.getResources().getDrawable(R.drawable.talking);
			vh.meetingStatusButton.setBackgroundDrawable(shap);
			break;


		default:
			break;
		}
		
		
		

		vh.meetingStatusButton.setTag(String.valueOf(position));
		
		
		vh.meetingStatusButton.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(meetingStatus == AppConstant.HANG_UP_ED)
				{
					Button button = (Button)v;
					int pos = Integer.valueOf(String.valueOf(button.getTag()));
					
					JoinSomeoneToMeeting(pos);
					
				}
				
				
			}
		});
		
		
		return view;
	}
	
	
	private String joinMeeting_HttpParam_Maker(Map<String, String> map) {
		String resultString = "";
		
		List<String> list = new ArrayList<String>();
		
		String phoneNum = map.get("phone");
		list.add(phoneNum);
		
		String JsonRecevList = JsonParamMaker.listToJString(phoneListMaker(list));
		JsonRecevList = JsonParamMaker.toJsonArray("recevList", JsonRecevList);
		
		String meetingId = GlobalData.getInstance().getMetingDetails().get("meetingid");
		String usernameString = "txl";
		String passwordString = "txlpassword";
		
		Map<String, String> mapParam = new HashMap<String, String>();
		
		mapParam.put("userName", usernameString);
		mapParam.put("password", passwordString);
		
		String mapParamString = JsonParamMaker.MapToJStr(mapParam);
		
		resultString = JsonRecevList;
		resultString += ",";
		
		String meetingId_KV = "\"meetingid\":" + meetingId + ",";
		
		resultString += meetingId_KV;
		
		resultString += mapParamString;
		
		resultString = JsonParamMaker.toCompleteJson(resultString);
		
		resultString = Base64Tool.encode(resultString);
		
		return resultString;
	}
	
	private void JoinSomeoneToMeeting(final int pos)
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				Map<String, String> map = sourceList.get(pos);
				String paramString = joinMeeting_HttpParam_Maker(map);
				
				String resultString = MyHttpClient.DoSomething(AppConstant.METHOD_JOIN_MEETING, paramString);
				
				
				
			}
		}){
			
		}.start();
	}
	
	public List phoneListMaker(List<String> phoneList)
	{
		List<String[]> list = new ArrayList<String[]>();
		if (phoneList == null && phoneList.size() == 0) 
		{	
			return null;
		}
		
		for (String contactInfo : phoneList) {
			String[] str = new String[2];
			str[0] = "phone";
			str[1] = contactInfo;
			list.add(str);
		} 
		return list;
	}

		
	public final class ViewHolder
	{
		TextView nameTextView;
		TextView phoneNumTextView;
		Button meetingStatusButton;
	}

}
