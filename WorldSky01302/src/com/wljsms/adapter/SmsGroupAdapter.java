/**
 * Name : SmsGroupAdapter.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eteng.world.R;
import com.wljsms.info.ContactInfo;
import com.wljsms.info.GroupInfo;

/**
 * com.eteng.adapter.GroupAdapter
 * @author wanglaoji <br/>
 * Create at 2013-1-22 下午3:08:36
 * Description : 群组折叠列表适配器
 * Modified : 
 */
public class SmsGroupAdapter extends BaseExpandableListAdapter {
	/**
	 * 群组数据
	 */
	private List<GroupInfo> parents;
	/**
	 * 群组里联系人数据
	 */
	private List<List<ContactInfo>>	childs;
	/**
	 * layout解析对象
	 */
	private LayoutInflater mInflater;
	/**
	 * 事件监听对象
	 */
	private OnClickListener listener;
	/**
	 * 获取系统资源对象
	 */
	private Resources res;
	/**
	 * 白色和选中背景颜色
	 */
	private int color_white,color_pressed;
	
	public SmsGroupAdapter(Context context,List<GroupInfo> groups){
		mInflater = LayoutInflater.from(context);
		res = context.getResources();
		parents = groups;
		childs = new ArrayList<List<ContactInfo>>();
		for(GroupInfo info : parents)
			childs.add(info.getChilds());
		color_white = res.getColor(R.color.white);
		color_pressed = res.getColor(R.color.group_child_item_selected_color);
	}
	
	static class ChildViewHolder {
		ImageView image;
		TextView username;
		TextView phone;
	}
	
	static class ParentViewHolder {
		ImageView image;
		TextView username;
		TextView count;
		ImageView arrow;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childs.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ChildViewHolder childHolder;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.group_child, null);
			childHolder = new ChildViewHolder();
			childHolder.image = (ImageView)convertView.findViewById(R.id.group_child_img);
			childHolder.username = (TextView)convertView.findViewById(R.id.group_child_name);
			childHolder.phone = (TextView)convertView.findViewById(R.id.group_child_phone);
			convertView.setTag(childHolder);
		}else {
			childHolder = (ChildViewHolder)convertView.getTag();
		}
		ContactInfo info = childs.get(groupPosition).get(childPosition);
		if(info.isChecked()){
			//选中状态，设置选中的背景色
			convertView.setBackgroundColor(color_pressed);
			childHolder.image.setBackgroundResource(R.drawable.group_selected_bg);
		}else {
			//未选中状态，设置背景为白色
			convertView.setBackgroundColor(color_white);
			childHolder.image.setBackgroundResource(R.drawable.group_no_selected_bg);
		}
		childHolder.username.setText(info.getName());
		childHolder.phone.setText(info.getPhone());
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return childs.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return parents.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return parents.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ParentViewHolder parentHolder;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.group_parent, null);
			parentHolder = new ParentViewHolder();
			parentHolder.image = (ImageView)convertView.findViewById(R.id.group_parent_img);
			parentHolder.arrow = (ImageView)convertView.findViewById(R.id.group_parent_arrow);
			parentHolder.username = (TextView)convertView.findViewById(R.id.group_parent_name);
			parentHolder.count = (TextView)convertView.findViewById(R.id.group_parent_count);
			convertView.setTag(parentHolder);
		}else {
			parentHolder = (ParentViewHolder)convertView.getTag();
		}
		GroupInfo info = parents.get(groupPosition);
		
		if(info.isAllChecked()){
			//组中成员全部选中
			convertView.setBackgroundResource(R.drawable.group_parent_pressed);
			parentHolder.image.setBackgroundResource(R.drawable.group_selected_bg);
		}else if(info.isSomeChecked()){
			//组里成员部分选中
			convertView.setBackgroundResource(R.drawable.group_parent_normal);
			parentHolder.image.setBackgroundResource(R.drawable.group_some_selected_bg);
		}else {
			//组里成员全部未选中
			convertView.setBackgroundResource(R.drawable.group_parent_normal);
			parentHolder.image.setBackgroundResource(R.drawable.group_no_selected_bg);
		}
		
		if(info.isExpandable()){
			//组为展开状态，设置向下箭头
			parentHolder.arrow.setBackgroundResource(R.drawable.group_arrow_down);
		}else {
			//组为未选中状态，设置向右箭头
			parentHolder.arrow.setBackgroundResource(R.drawable.group_arrow_right);
		}
		
		parentHolder.image.setTag(groupPosition);
		parentHolder.image.setOnClickListener(listener);
		parentHolder.username.setText(info.getName());
		parentHolder.count.setText("(" + info.getChilds().size()+")");
		return convertView;
	}

	public void setOnClickListener(OnClickListener listener){
		this.listener = listener;
	}
	
	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

}
