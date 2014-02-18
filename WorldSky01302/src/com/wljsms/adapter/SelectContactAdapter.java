/**
 * Name : SelectContactAdapter.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eteng.world.R;
import com.wljsms.info.ContactInfo;

/**
 * com.eteng.adapter.SelectContactAdapter
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-22 下午1:33:53 Description : 发通知页面选择联系人信息适配器 Modified
 *         :
 */
public class SelectContactAdapter extends BaseAdapter {

	/**
	 * 数据集合
	 */
	private List<ContactInfo> data;
	/**
	 * 上下文对象
	 */
	private Context context;
	/**
	 * 每一个item的点击事件
	 */
	private OnClickListener listener;

	public SelectContactAdapter(Context context, List<ContactInfo> data) {
		this.context = context;
		this.data = data;
	}

	public final class ViewHolder {
		TextView username;
		public int position;
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
			holder.position = position;
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.selected_contact_gv_item,
					null);

			holder.username = (TextView) convertView
					.findViewById(R.id.select_contact_gv_item_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.position = position;
		}
		holder.username.setText(data.get(position).getName());
		//设置每一项的点击事件
		if (listener != null)
			convertView.setOnClickListener(listener);
		return convertView;
	}

	public void setOnClick(OnClickListener listener) {
		this.listener = listener;
	}

}
