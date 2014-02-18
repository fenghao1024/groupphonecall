/**
 * Name : RecentContactAdapter.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.adapter;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.eteng.world.R;
import com.eteng.world.SmsApplication;
import com.wljsms.info.ContactInfo;
import com.wljsms.util.Utils;
/**
 * 
 * com.eteng.adapter.RecentContactAdapter
 * @author wanglaoji <br/>
 * Create at 2013-2-4 下午4:49:52
 * Description : 最近联系人适配器
 * Modified :
 */
public class RecentContactAdapter extends BaseAdapter {

	private Context context;
	private List<ContactInfo> listData;
	private TextView tv;
	private SmsApplication application;
	private Resources res;
	private String mToastItemStr;
	private int color_white;

	public RecentContactAdapter(Context context, List<ContactInfo> listData,
			TextView tv, SmsApplication application) {
		this.context = context;
		res = context.getResources();
		mToastItemStr = res.getString(R.string.no_select_toast_item_str);
		this.listData = listData;
		this.tv = tv;
		this.application = application;
		color_white = res.getColor(R.color.white);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listData.size();

	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			ContactInfo info = listData.get(position);
			if (info.isChecked()) {
				info.setChecked(false);
				application.removeContactInfo(info);
				application.selectedContactsCount--;
			} else {
				if (application.addContactInfo(info)) {
					info.setChecked(true);
					application.selectedContactsCount++;
				} else {
					Utils.showCustomToast(context, mToastItemStr);
					return;
				}
			}
			RecentContactAdapter.this.notifyDataSetChanged();
			tv.setText("确  定 (" + application.selectedContactsCount + ")");
		}

	};

	// 重写View
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.recent_contact_item_layout,
					null);

			holder.username = (TextView) convertView
					.findViewById(R.id.recent_contact_name);
			holder.userphone = (TextView) convertView
					.findViewById(R.id.recent_contact_phone);
			holder.cBox = (CheckBox) convertView
					.findViewById(R.id.recent_contact_cb);
			holder.LayoutName = (LinearLayout) convertView
					.findViewById(R.id.LayoutName);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ContactInfo info = listData.get(position);

		holder.username.setText(info.getName());

		holder.userphone.setText("(" + info.getPhone() + ")");

		holder.cBox.setTag(position);
		holder.cBox.setOnClickListener(listener);
		if (info.isChecked()) {
			holder.cBox.setButtonDrawable(R.drawable.sys_choose);
			convertView.setBackgroundResource(R.drawable.contacter_item_bg);
		} else {
			holder.cBox.setButtonDrawable(R.drawable.sys_choose_2);
			convertView.setBackgroundColor(color_white);
		}

		return convertView;
	}

	public void selectAddOne() {
		application.selectedContactsCount++;
		tv.setText("确  定 (" + application.selectedContactsCount + ")");
	}

	public void selectMinusOne() {
		application.selectedContactsCount--;
		tv.setText("确  定 (" + application.selectedContactsCount + ")");
	}

	public final class ViewHolder {
		public TextView username;
		public TextView userphone;
		public CheckBox cBox;
		public LinearLayout LayoutName;
	}

}
