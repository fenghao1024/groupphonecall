/**
 * Name : GroupListAdapter.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.adapter;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
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
 * 
 * com.eteng.adapter.GroupListAdapter
 * @author wanglaoji <br/>
 * Create at 2013-2-4 下午4:35:33
 * Description : 群组使用的折叠列表适配器
 * Modified :
 */
public class GroupListAdapter extends BaseExpandableListAdapter {

	/**
	 * 群组数据
	 */
	private List<GroupInfo> parents;
	/**
	 * 每个群组中的联系人数据
	 */
	private List<List<ContactInfo>>	childs;
	/**
	 * 解析layout配置文件对象
	 */
	private LayoutInflater mInflater;
	/**
	 * 群组或群组里联系人选择按钮的点击事件监听器，用于选择或反选联系人
	 */
	private OnClickListener listener;
	private Resources res;
	/**
	 * 群组正常和选中的背景图片
	 */
	private Drawable mGroupparentNormal,mGroupparentPressed;
	/**
	 * 没选择，部分选择，全部选择对应的图片
	 */
	private Drawable mNoSelected,mSomeSelected,mSelected;
	/**
	 * 群组里联系人正常和选择状态的背景颜色
	 */
	private int mChildNormal,mChildPressed;
	
	public GroupListAdapter(Context context,List<GroupInfo> groups){
		mInflater = LayoutInflater.from(context);
		res = context.getResources();
		mGroupparentNormal = res.getDrawable(R.drawable.group_parent_normal);
		mGroupparentPressed = res.getDrawable(R.drawable.group_parent_pressed);
		mNoSelected = res.getDrawable(R.drawable.group_no_selected_bg);
		mSomeSelected = res.getDrawable(R.drawable.group_some_selected_bg);
		mSelected = res.getDrawable(R.drawable.group_selected_bg);
		mChildPressed = res.getColor(R.color.group_child_item_selected_color);
		mChildNormal = res.getColor(R.color.white);
		
		parents = groups;
		childs = new ArrayList<List<ContactInfo>>();
		for(GroupInfo info : parents)
			childs.add(info.getChilds());
	}
	
	static class ChildViewHolder {
		ImageView image;
		TextView name;
		TextView phone;
	}
	
	static class ParentViewHolder {
		ImageView image;
		TextView name;
		TextView count;
		ImageView arrow;
		ImageView edit;
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
			childHolder.name = (TextView)convertView.findViewById(R.id.group_child_name);
			childHolder.phone = (TextView)convertView.findViewById(R.id.group_child_phone);
			convertView.setTag(childHolder);
		}else {
			childHolder = (ChildViewHolder)convertView.getTag();
		}
		ContactInfo info = childs.get(groupPosition).get(childPosition);
		if(info.isChecked()){
			//联系人处于选中状态，设置背景和对应的图片
			convertView.setBackgroundColor(mChildPressed);
			childHolder.image.setBackgroundDrawable(mSelected);
		}else {
			//联系人处于未选中状态，设置背景和对应的图片
			convertView.setBackgroundColor(mChildNormal);
			childHolder.image.setBackgroundDrawable(mNoSelected);
		}
		childHolder.name.setText(info.getName());
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
			convertView = mInflater.inflate(R.layout.group_list_parent, null);
			parentHolder = new ParentViewHolder();
			parentHolder.image = (ImageView)convertView.findViewById(R.id.group_list_parent_img);
			parentHolder.arrow = (ImageView)convertView.findViewById(R.id.group_list_parent_arrow);
			parentHolder.name = (TextView)convertView.findViewById(R.id.group_list_parent_name);
			parentHolder.count = (TextView)convertView.findViewById(R.id.group_list_parent_count);
			parentHolder.edit = (ImageView)convertView.findViewById(R.id.group_list_parent_edt);
			convertView.setTag(parentHolder);
		}else {
			parentHolder = (ParentViewHolder)convertView.getTag();
		}
		GroupInfo info = parents.get(groupPosition);
		if(info.isAllChecked()){
			//群组里联系全部选择，设置背景和图片
			convertView.setBackgroundDrawable(mGroupparentPressed);
			parentHolder.image.setBackgroundDrawable(mSelected);
		}else if(info.isSomeChecked()){
			//群组里联系部分选择，设置背景和图片
			convertView.setBackgroundDrawable(mGroupparentNormal);
			parentHolder.image.setBackgroundDrawable(mSomeSelected);
		}else {
			//群组里联系全部未选择，设置背景和图片
			convertView.setBackgroundDrawable(mGroupparentNormal);
			parentHolder.image.setBackgroundDrawable(mNoSelected);
		}
		if(info.isExpandable()){
			//群组处于咱开状态，设置箭头向下
			parentHolder.arrow.setBackgroundResource(R.drawable.group_arrow_down);
		}else {
			//群组处于折叠状态，设置箭头向右
			parentHolder.arrow.setBackgroundResource(R.drawable.group_arrow_right);
		}
		parentHolder.image.setTag(groupPosition);
		parentHolder.edit.setTag(groupPosition);
		parentHolder.image.setOnClickListener(listener);
		parentHolder.edit.setOnClickListener(listener);
		parentHolder.name.setText(info.getName());
		parentHolder.count.setText("(" + info.getChilds().size()+")");
		return convertView;
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
	
	public void setOnClickListener(OnClickListener listener){
		this.listener = listener;
	}

}
