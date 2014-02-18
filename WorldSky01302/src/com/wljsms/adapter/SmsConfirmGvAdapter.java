/**
 * Name : SmsConfirmGvAdapter.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eteng.world.R;
import com.wljsms.info.ContactInfo;

/**
 * com.eteng.adapter.SmsConfirmGvAdapter
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-25 下午1:53:00 Description : 发短信确认页面的gridview适配器
 *         Modified :
 */
public class SmsConfirmGvAdapter extends BaseAdapter {

	/**
	 * 数据集合
	 */
	private List<ContactInfo> data;
	/**
	 * layout解析对象
	 */
	private LayoutInflater mInflater;

	public SmsConfirmGvAdapter(Context context, List<ContactInfo> data) {
		this.data = data;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.sms_confirm_gv_item_layout, null);
			holder.username = (TextView) convertView;
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.username.setText(data.get(position).getName());
		return convertView;
	}

	public final class ViewHolder {
		TextView username;
	}

}
