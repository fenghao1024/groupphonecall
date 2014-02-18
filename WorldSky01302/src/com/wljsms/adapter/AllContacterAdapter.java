/**
 * Name : AllContacterAdapter.java
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
 * com.eteng.adapter.AllContacterAdapter 
 * @author wanglaoji <br/>
 * Create at 2013-2-21 下午4:56:39  
 * Description : 所有联系人适配器 
 */
public class AllContacterAdapter extends BaseAdapter {

	private Context context;
	/**
	 * 数据列表
	 */
	private List<ContactInfo> listData;
	/**
	 * 显示选择了多少联系人的底部TextView
	 */
	private TextView tv;
	/**
	 *Application对象
	 */
	private SmsApplication application;
	/**
	 * 系统资源对象
	 */
	private Resources res; 
	/**
	 * 颜色值
	 */
	private int color_white;

	public AllContacterAdapter(Context context, List<ContactInfo> listData,
			TextView tv, SmsApplication application) {
		this.context = context;
		res = context.getResources();
		this.listData = listData;
		this.tv = tv;
		this.application = application;
		color_white =res.getColor(R.color.white);
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

	//点击事件监听器，响应点击了listview每一项的的小图标。
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			// TODO Auto-generated method stub
			//取得点击项的信息
			ContactInfo info = listData.get(position);
			if (info.isChecked()) {
				//选中状态，设置状态为未选中，并从application的选择列表中删除。
				info.setChecked(false);
				application.removeContactInfo(info);
				application.selectedContactsCount--;
			} else {
				//未选中，设置为选中状态并加入到已选择列表中
				if (application.addContactInfo(info)) {
					info.setChecked(true);
					application.selectedContactsCount++;
				} else {
					//弹出自定义toast提示信息
					Utils.showCustomToast(context, "联系人已被选择");
					return;
				}
			}
			//更新listview和选择人数
			AllContacterAdapter.this.notifyDataSetChanged();
			tv.setText("确定 (" + application.selectedContactsCount + ")");
		}

	};

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.user, null);

			holder.username = (TextView) convertView
					.findViewById(R.id.userName);
			holder.userphone = (TextView) convertView
					.findViewById(R.id.userPhone);
			holder.cBox = (CheckBox) convertView.findViewById(R.id.chk_Select);
			holder.LayoutName = (LinearLayout) convertView
					.findViewById(R.id.LayoutName);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ContactInfo info = listData.get(position);

		holder.username.setText(info.getName());

		holder.userphone.setText(info.getPhone());

		holder.cBox.setTag(position);
		holder.cBox.setOnClickListener(listener);
		if (info.isChecked()) {
			//设置显示选中图片
			holder.cBox.setButtonDrawable(R.drawable.sys_choose);
			convertView.setBackgroundResource(R.drawable.contacter_item_bg);
		} else {
			//设置显示未选中图片
			holder.cBox.setButtonDrawable(R.drawable.sys_choose_2);
			convertView
			.setBackgroundColor(color_white);
		}

		return convertView;
	}

	/**
	 * 底部选择人数加1并更新提示信息
	 */
	public void selectAddOne() {
		application.selectedContactsCount++;
		tv.setText("确定 (" + application.selectedContactsCount + ")");
	}

	/**
	 * 底部选择人数减1并更新提示信息
	 */
	public void selectMinusOne() {
		application.selectedContactsCount--;
		tv.setText("确定 (" + application.selectedContactsCount + ")");
	}

	public final class ViewHolder {
		public TextView username;
		public TextView userphone;
		public CheckBox cBox;
		public LinearLayout LayoutName;
	}
}
