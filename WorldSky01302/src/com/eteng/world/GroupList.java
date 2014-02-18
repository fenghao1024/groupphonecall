/**
 * Name : GroupList.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.eteng.world;

import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wljsms.adapter.GroupListAdapter;
import com.wljsms.info.ContactInfo;
import com.wljsms.info.GroupInfo;
import com.wljsms.util.Utils;

/**
 * 
 * com.eteng.world.GroupList
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-19 下午2:17:56 Description :
 *         群组展示。群组信息和所有联系人一样，采用预加载方式。 Modified :
 */
public class GroupList extends Activity {

	/**
	 * 新建群组
	 */
	private LinearLayout mFootView;
	/**
	 * 折叠群组列表
	 */
	private ExpandableListView mGroupListview;
	/**
	 * 群组适配器
	 */
	private GroupListAdapter mGroupAdapter;
	/**
	 * 群组列表
	 */
	private List<GroupInfo> mGroups;
	private SmsApplication mApplication;
	/**
	 * 选择组件
	 */
	private TextView mConfirmTv;
	/**
	 * 提示文字信息
	 */
	private String mToastItemStr, mToastGroupStr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.group_main);
		mApplication = (SmsApplication) getApplication();
		mToastItemStr = getResources().getString(
				R.string.no_select_toast_item_str);
		mToastGroupStr = getResources().getString(
				R.string.no_select_toast_group_str);
		initUI();
		initFooterView();
		initData();

	}

	/**
	 * 初始化UI
	 */
	private void initUI() {
		mGroupListview = (ExpandableListView) findViewById(R.id.group_list_expandablelistview);
		mConfirmTv = (TextView) findViewById(R.id.group_list_confirm);
		mConfirmTv.setOnClickListener(listener);
	}

	/**
	 * 从Application中获取数据
	 */
	private void initData() {
		mGroups = mApplication.getmGroups();
		
		if (mApplication.selectedContactsCount > 0)
			mConfirmTv.setText("确  定 (" + mApplication.selectedContactsCount
					+ ")");
		else
			mConfirmTv.setText("确  定");
		mGroupAdapter = new GroupListAdapter(this, mGroups);
		mGroupAdapter.setOnClickListener(listener);
		mGroupListview.addFooterView(mFootView);
		mGroupListview.setAdapter(mGroupAdapter);

		// 如果发短信页面选择了组的状态为展开，这里也要展开组，以对应
		int i = 0;
		for (GroupInfo gi : mGroups) {
			if (gi.isExpandable())
				mGroupListview.expandGroup(i);
			i++;
		}

		//群组里联系人点击事件，选择或反选联系人
		mGroupListview.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				//取得点击的群组对象
				GroupInfo group = mGroups.get(groupPosition);
				//取得群组里的联系人对象
				List<ContactInfo> childs = group.getChilds();
				//取得群组里被点击的联系人对象
				ContactInfo info = childs.get(childPosition);
				if (info.isChecked()) {
					//联系人已经被选择，设置为未选中
					info.setChecked(false);
					mApplication.removeContactInfo(info);
					mApplication.selectedContactsCount--;
				} else {
					//联系人为选中
					if (mApplication.addContactInfo(info)) {
						info.setChecked(true);
						mApplication.selectedContactsCount++;
					} else {
						//联系人已被选择，给出提示信息
						Utils.showCustomToast(GroupList.this, mToastItemStr);
						return true;
					}
				}
				//更新群组的选择状态，全部选中，部分选中，全部为选中对应不同的图标
				updateGroupState(group);
				mGroupAdapter.notifyDataSetChanged();
				mConfirmTv.setText("确  定 ("
						+ mApplication.selectedContactsCount + ")");
				return true;
			}

		});
		//群组点击事件，展开或折叠群组
		mGroupListview.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				// TODO Auto-generated method stub
				GroupInfo info = mGroups.get(groupPosition);

				if (info.isExpandable()) {
					//展开状态，折叠
					info.setExpandable(false);
					mGroupListview.collapseGroup(groupPosition);
				} else {
					//折叠状态，展开
					info.setExpandable(true);
					mGroupListview.expandGroup(groupPosition);
				}
				return true;
			}

		});
	}

	/**
	 * 按钮监听事件
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	/**
	 * 更新组的状态
	 */
	private void updateGroupState(GroupInfo group) {
		if (group == null)
			return;
		List<ContactInfo> childs = group.getChilds();
		boolean noSelected = false;
		boolean someSelected = false;
		for (ContactInfo ci : childs) {
			if (!ci.isChecked()) {
				// 没有选择
				noSelected = true;
				continue;
			}
			if (ci.isChecked()) {
				// 部分选择
				someSelected = true;
				continue;
			}
		}
		group.setSomeChecked(someSelected && noSelected);
		group.setAllChecked(!noSelected);
	}

	/**
	 * listview底部新建群组控件
	 */
	private void initFooterView() {
		if (mFootView == null) {
			mFootView = (LinearLayout) LayoutInflater.from(this).inflate(
					R.layout.group_list_footerview, null);
			mFootView.setOnClickListener(listener);
		}

	}

	/**
	 * 事件监听
	 */
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.group_list_parent_img) {
				//点击了群组的选择图片，全选或反选群组里的所有联系人
				int groupPosition = (Integer) v.getTag();
				//取得点击的群组对象
				GroupInfo info = mGroups.get(groupPosition);
				//如果群组里没有联系人，直接返回
				if (info.getChilds().size() == 0)
					return;
				if (!info.isAllChecked() && !info.isSomeChecked()) {
					//群组里联系人全部未选择，全选所有联系人
					boolean flag = false, flag1 = false;
					List<ContactInfo> temp = info.getChilds();
					for (ContactInfo ci : temp)
						if (mApplication.addContactInfo(ci)) {
							//成功添加到已选择联系人列表，设置选择了联系人标志
							ci.setChecked(true);
							flag1 = true;
							mApplication.selectedContactsCount++;
						} else {
							flag = true;
						}
					if (!flag1) {
						// 没有选择任何联系人
						Utils.showCustomToast(GroupList.this, mToastGroupStr);
						return;
					}
					if (flag) {
						// 选择了部分联系人
						Utils.showCustomToast(GroupList.this, mToastGroupStr);
						info.setAllChecked(false);
						info.setSomeChecked(true);
					} else {
						//全部联系人被选中
						info.setAllChecked(true);
						info.setSomeChecked(false);
					}

				} else {
					//群里联系人有部分已被选择，反选所有联系人
					info.setAllChecked(false);
					info.setSomeChecked(false);
					List<ContactInfo> temp = info.getChilds();
					for (ContactInfo ci : temp) {
						if (ci.isChecked()) {
							ci.setChecked(false);
							mApplication.removeContactInfo(ci);
							mApplication.selectedContactsCount--;
						}

					}

				}
				mGroupAdapter.notifyDataSetChanged();
				//更新底部的提示信息
				mConfirmTv.setText("确  定 ("
						+ mApplication.selectedContactsCount + ")");

			} else if (v.getId() == R.id.group_list_parent_edt) {
				// 编辑群组
				int pos = (Integer) v.getTag();
				Intent intent = new Intent();
				intent.setClass(GroupList.this, EditGroupActiv.class);
				intent.putExtra("index", pos);
				startActivity(intent);
				clearCheckedStatus();
				GroupList.this.finish();
			} else if (v.getId() == R.id.group_list_add) {
				// 新建群组
				Intent intent = new Intent();
				intent.setClass(GroupList.this, NewGroupActiv.class);
				startActivity(intent);
				clearCheckedStatus();
				GroupList.this.finish();
			} else if (v.getId() == R.id.group_list_confirm) {
				//底部确定按钮
				if (mApplication.getmContactsList().size() == 0) {
					Utils.showCustomToast(GroupList.this, "请选择联系人!");
					return;
				}
				Intent intent = new Intent();
				intent.setClass(GroupList.this, SmsGroupSend.class);
				intent.putExtra("groupDate", mApplication.getGroupDate());
				intent.putExtra("draft_str", mApplication.mDraft);
				startActivity(intent);
				GroupList.this.finish();

			}
		}

	};

	/**
	 * 清除联系人的选择状态
	 */
	private void clearCheckedStatus() {
		//清除所有联系人的选择状态
		List<ContactInfo> temp1 = mApplication.getArrayList();
		for (ContactInfo ci : temp1)
			ci.setChecked(false);
		temp1 = null;
		//清除群组里联系人的选择状态
		List<GroupInfo> temp2 = mApplication.getmGroups();
		for (GroupInfo gi : temp2) {
			temp1 = gi.getChilds();
			gi.setAllChecked(false);
			gi.setSomeChecked(false);
			for (ContactInfo ci : temp1)
				ci.setChecked(false);
		}
		//清除最近联系人的选择状态
		if (mApplication.ismLoadRecentContactComplete()) {
			for (ContactInfo ci : mApplication.getmRecentContactList())
				ci.setChecked(false);
		}
		//清空已选择联系人列表
		mApplication.getmContactsList().clear();
		//初始化已选择联系人数量
		mApplication.selectedContactsCount = 0;
	}

}
