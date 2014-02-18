package com.eteng.world;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.eteng.world.MyLetterView.OnTouchingLetterChangedListener;
import com.wljsms.adapter.EditGroupAdapter;
import com.wljsms.info.ContactInfo;
import com.wljsms.info.GroupInfo;
import com.wljsms.util.Utils;

/**
 * 
 * com.eteng.world.EditGroupActiv
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-19 下午2:23:04 Description :
 *         编辑群组。群组中已有的联系人信息，在所有联系人中不显示 Modified :
 */
public class EditGroupActiv extends Activity implements
		OnTouchingLetterChangedListener {

	/**
	 *  群组名输入框
	 */
	private EditText mGroupNameEt;
	//文本对象，依次为保存，取消，所有联系人，群组里联系人
	private TextView mSaveTv, mCancelTv, mAllContactTv, mGroupContactTv; 
	/**
	 * 所有联系人和群组里联系人的listview
	 */
	private ListView mAllContactLv, mGroupLv;
	/**
	 * 编辑群组 弹出确认对话框的两个按钮
	 */
	private TextView mDialogTv1, mDialogTv2;
	//以下3行用于侧边字母搜索
	private MyLetterView myView;
	private TextView letterTv;  
	private OverlayThread overlayThread = new OverlayThread();
	//返回按钮
	private ImageView mBackImg;
	/**
	 * 群组信息实体
	 */
	private GroupInfo group; 
	/**
	 * 所有联系人集合
	 */
	private ArrayList<ContactInfo> mAllContactList; 
	/**
	 * 应用对象
	 */
	private SmsApplication mApplication;  
	//所有联系人和群组适配器
	private EditGroupAdapter mAllContacterAdapter, mGroupContactAdapter; 

	private LayoutInflater mInflater; 
	/**
	 * 编辑群组确认对话框
	 */
	private Dialog dialog;
	/**
	 * 对话框的view
	 */
	private View dialogView; 
	/**
	 * 联系人已选择提示文字
	 */
	private String mToastItemStr; 
	/**
	 *  已选择联系人列表
	 */
	private List<ContactInfo> mSelectedContacts;
	/**
	 *  排除群组中联系人后的所有联系人列表
	 */
	private List<ContactInfo> tempList = new ArrayList<ContactInfo>();
	/**
	 * 页面父组件，用户点击输入框以外区域输入法隐藏
	 */
	private View ll;
	/**
	 * 标志用户选择的是覆盖还是新建，1：覆盖，2：新建
	 */
	private int flag;

	/**
	 * 群组操作正在进行的提示对话框
	 */
	private ProgressDialog mProgressDialog;
	
	/**
	 * 原来群组中需要移出的联系人列表
	 */
	private List<ContactInfo> mEmigrationList = new ArrayList<ContactInfo>();
	/**
	 * 所有联系人中需要移入群组的联系人列表
	 */
	private List<ContactInfo> mImmigrationList = new ArrayList<ContactInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.edit_group_layout);
		int index = getIntent().getIntExtra("index", -1);
		if (index == -1)
			return;
		mToastItemStr = getResources().getString(
				R.string.no_select_toast_item_str);
		mApplication = (SmsApplication) getApplication();
		mInflater = LayoutInflater.from(this);
		initUI();
		// 根据传过来的索引值取得群组
		group = mApplication.getmGroups().get(index);
		// 初始化组成员的状态为选中，重复的不设置
		List<ContactInfo> tmp = group.getChilds();
		for (ContactInfo ci : tmp) {
			if (mApplication.addContactInfo(ci)) {
				ci.setChecked(true);
				mApplication.selectedContactsCount++;
			}
		}

		//设置底部的提示信息
		if (mApplication.selectedContactsCount > 0)
			mSaveTv.setText("确定(" + mApplication.selectedContactsCount + ")");
		//默认设置群组名称
		mGroupNameEt.setText(group.getName());
		// 取得所有联系人的信息,排除群组中已有联系人
		mAllContactList = mApplication.getArrayList();
		for (ContactInfo ci : mAllContactList) {
			boolean flag = false;
			for (ContactInfo ci1 : tmp) {
				if (ci.getName().equals(ci1.getName())
						&& ci.getPhone().equals(ci1.getPhone())) {
					flag = true;
					break;
				}
			}
			if (!flag)
				tempList.add(ci);
		}

		tmp = null;
		mAllContactList = null;
		//设置全部联系人数和群组里的成员数
		mAllContactTv.setText("全部联系人(" + tempList.size() + ")");
		mGroupContactTv.setText("群组成员(" + group.getChilds().size() + ")");
		// 创建全部联系人适配器
		mAllContacterAdapter = new EditGroupAdapter(EditGroupActiv.this,
				tempList, mSaveTv, mApplication);
		// 创建群组适配器
		mGroupContactAdapter = new EditGroupAdapter(EditGroupActiv.this,
				group.getChilds(), mSaveTv, mApplication);

		mAllContactLv.setAdapter(mAllContacterAdapter);
		mGroupLv.setAdapter(mGroupContactAdapter);
		//所有联系人listview的点击事件
		mAllContactLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				//取得点击项
				ContactInfo tmp = tempList.get(arg2);
				if (tmp.isChecked()) {
					//已选中，设置为未选中
					tmp.setChecked(false);
					//已选择联系人数减1
					mAllContacterAdapter.selectMinusOne();
					//从选择列表中移除
					mApplication.removeContactInfo(tmp);
				} else {
					//未选中，设置为选中
					if (mApplication.addContactInfo(tmp)) {
						//成功加入的选择列表，已选择联系人数据加1
						tmp.setChecked(true);
						mAllContacterAdapter.selectAddOne();
					} else {
						//联系人已经选择，弹出提示信息
						Utils.showCustomToast(EditGroupActiv.this,
								mToastItemStr);
						return;
					}
				}
				mSaveTv.setText("确定 (" + mApplication.selectedContactsCount
						+ ")");
				mAllContacterAdapter.notifyDataSetChanged();
			}
		});
		//群组成员listview的点击事件
		mGroupLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				//参考所有联系人listview点击事件的注释说明，类似
				ContactInfo tmp = group.getChilds().get(arg2);
				if (tmp.isChecked()) {
					tmp.setChecked(false);
					mGroupContactAdapter.selectMinusOne();
					mApplication.removeContactInfo(tmp);
				} else {
					if (mApplication.addContactInfo(tmp)) {
						tmp.setChecked(true);
						mGroupContactAdapter.selectAddOne();
					} else {
						
						Utils.showCustomToast(EditGroupActiv.this, "联系人已选择");
						return;
					}
				}
				mSaveTv.setText("确定 (" + mApplication.selectedContactsCount
						+ ")");
				mGroupContactAdapter.notifyDataSetChanged();
			}
		});
	}

	
	/**
	 * 初始化UI
	 */
	private void initUI() {
		mGroupNameEt = (EditText) findViewById(R.id.edit_group_edittext);
		mSaveTv = (TextView) findViewById(R.id.edit_group_save);
		mCancelTv = (TextView) findViewById(R.id.edit_group_cancel);
		mAllContactTv = (TextView) findViewById(R.id.edit_all_contact);
		mGroupContactTv = (TextView) findViewById(R.id.edit_group_name);
		mAllContactLv = (ListView) findViewById(R.id.edit_group_all_contacts);
		mGroupLv = (ListView) findViewById(R.id.edit_group_contacts);
		dialogView = mInflater.inflate(R.layout.dialog_layout, null);
		mDialogTv1 = (TextView) dialogView.findViewById(R.id.dialog_1);
		mDialogTv2 = (TextView) dialogView.findViewById(R.id.dialog_2);
		mBackImg = (ImageView) findViewById(R.id.edit_group_back);
		myView = (MyLetterView) findViewById(R.id.myGroupView);
		letterTv = (TextView) findViewById(R.id.edit_group_tvLetter);
		letterTv.setVisibility(View.GONE);
		ll = findViewById(R.id.edit_group_ll);

		mDialogTv1.setOnClickListener(listener);
		mDialogTv2.setOnClickListener(listener);
		mBackImg.setOnClickListener(listener);
		myView.setOnTouchingLetterChangedListener(this);
		ll.setOnClickListener(listener);
		mSaveTv.setOnClickListener(listener);
		mCancelTv.setOnClickListener(listener);
	}
	/**
	 * 点击事件监听
	 */
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (v.getId() == R.id.edit_group_back) {
				// 返回，设置组成员的状态为未选择，
				for (ContactInfo ci : group.getChilds())
					ci.setChecked(false);
				for (ContactInfo ci : tempList)
					ci.setChecked(false);
				mApplication.getmContactsList().clear();

				Intent intent = new Intent();
				intent.setClass(EditGroupActiv.this, GroupMain.class);
				startActivity(intent);
				EditGroupActiv.this.finish();
			} else if (v.getId() == R.id.edit_group_cancel) {
				// 点击底部确定取消按钮，设置组成员的状态为未选择，
				for (ContactInfo ci : group.getChilds())
					ci.setChecked(false);
				for (ContactInfo ci : tempList)
					ci.setChecked(false);
				mApplication.getmContactsList().clear();
				//返回到群组页面
				Intent intent = new Intent();
				intent.setClass(EditGroupActiv.this, GroupMain.class);
				startActivity(intent);
				EditGroupActiv.this.finish();
			} else if (v.getId() == R.id.edit_group_save) {
				// 点击底部确定，弹出确认对话框
				showDialog();
			} else if (v.getId() == R.id.dialog_1) {
				// 点击弹出对话框的覆盖按钮，覆盖原群组
				flag = 1;
				if (dialog != null)
					dialog.dismiss();

				String name = mGroupNameEt.getText().toString();
				if (name == null || name.equals("")){
					Utils.showCustomToast(EditGroupActiv.this, "请输入分组名");
					return;
				}
				
				mProgressDialog = ProgressDialog.show(EditGroupActiv.this,
						null, "正在建立群组，请稍等");
				new EditGroupTast().execute(null, null, null);

			} else if (v.getId() == R.id.dialog_2) {
				// 点击弹出对话框的新建按钮，新建一个群组
				flag = 2;
				if (dialog != null)
					dialog.dismiss();
				String name = mGroupNameEt.getText().toString();
				if (name == null || name.equals("")) {
					Utils.showCustomToast(EditGroupActiv.this, "请输入分组名");
					return;
				}
				mProgressDialog = ProgressDialog.show(EditGroupActiv.this,
						null, "正在建立群组，请稍等");
				new EditGroupTast().execute(null, null, null);
			} else if (v.getId() == R.id.edit_group_ll)
				hideIM(v);
		}
	};

	// 异步任务，用于建立群组
	class EditGroupTast extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			for (ContactInfo ci : group.getChilds()) {
				ci.setChecked(false);
			}
			mApplication.getmContactsList().clear();
			mApplication.selectedContactsCount = 0;
			
			mEmigrationList.clear();
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}

			Intent intent = new Intent();
			intent.setClass(EditGroupActiv.this, GroupMain.class);
			startActivity(intent);
			EditGroupActiv.this.finish();
		}
		/**
		 * 后台运行的线程
		 */
		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			String name = mGroupNameEt.getText().toString();
			if (flag == 1) {
				//覆盖源群组操作，移出移入联系人
				getImmigration();
				getEmigration();
				for(ContactInfo ci : mEmigrationList){
					moveOutOfGroup(ci.getContactId());
				}
				for(ContactInfo ci : mImmigrationList){
					moveIntoGroup(ci.getContactId(), group.getId());
					ci.setGroupId(group.getId());
					group.getChilds().add(ci);
				}
			} else if (flag == 2) {
				// 新建群组
				getSelectedContacts();
				GroupInfo info = new GroupInfo();
				info.setChilds(mSelectedContacts);
				if (name.equals(group.getName()))
					name = name + "1";
				info.setName(name);
				info.setId(ContentUris.parseId(saveNewGroup(name)));
				for (ContactInfo ci : mSelectedContacts)
					moveIntoGroup(ci.getContactId(), info.getId());
				mApplication.getmGroups().add(info);
				mApplication.sortGroupDesc();
			}
			return null;
		}

	}

	/**
	 * 添加一个分组
	 */
	private Uri saveNewGroup(String newGroupName) {
		ContentValues values = new ContentValues();
		values.put(Groups.TITLE, newGroupName);
		return getContentResolver().insert(Groups.CONTENT_URI, values);
	}

	/**
	 * 把id为contactId的联系人移进一个分组
	 * 
	 * @param contactId
	 */
	private void moveIntoGroup(long contactId, long groupId) {
		ContentValues values = new ContentValues();
		values.put(Data.RAW_CONTACT_ID,
				queryForRawContactId(getContentResolver(), contactId));
		values.put(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
		values.put(GroupMembership.GROUP_ROW_ID, groupId);
		getContentResolver().insert(Data.CONTENT_URI, values);
	}
	
	/**
	 * 把id为contactId的联系人移出群组
	 */
	private void moveOutOfGroup(long contactId) {
		long rawContactId = queryForRawContactId(getContentResolver(),
				contactId);
		Cursor aggregationCursor = getContentResolver().query(
				AggregationExceptions.CONTENT_URI,
				null,
				AggregationExceptions.RAW_CONTACT_ID1 + " = ? or "
						+ AggregationExceptions.RAW_CONTACT_ID2 + " = ? ",
				new String[] { String.valueOf(rawContactId),
						String.valueOf(rawContactId) }, null);
		int count = aggregationCursor.getCount();
		String where = null;
		String[] selectionArgs = null;
		if (count > 0) {// 该记录与其他记录有聚合(Aggregation)
			// 由于在queryForRawContactId()方法中我们是根据contactId查rawContactId,且只返回了第一个
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < count; i++) {
				aggregationCursor.moveToPosition(i);
				sb.append(aggregationCursor.getLong(aggregationCursor
						.getColumnIndex(AggregationExceptions.RAW_CONTACT_ID2)));
				sb.append(',');
			}
			sb.append(rawContactId);
			//
			where = GroupMembership.GROUP_ROW_ID + " = ? " + " AND "
					+ Data.MIMETYPE + " = ? " + " AND " + Data.RAW_CONTACT_ID
					+ " in ( " + sb.toString() + " ) ";
			selectionArgs = new String[] { String.valueOf(group.getId()),
					GroupMembership.CONTENT_ITEM_TYPE };
		} else {// 该记录没有聚合(Aggregation)
			where = Data.RAW_CONTACT_ID + " = ? " + " AND " + Data.MIMETYPE
					+ " = ? " + " AND " + GroupMembership.GROUP_ROW_ID
					+ " = ? ";
			selectionArgs = new String[] { String.valueOf(rawContactId),
					GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(group.getId()) };
		}
		getContentResolver().delete(Data.CONTENT_URI, where, selectionArgs);
	}

	/**
	 * 根据参数中的contactId,查询在raw_contacts表中谁属于参数中的contactId
	 */
	private long queryForRawContactId(ContentResolver cr, long contactId) {
		Cursor rawContactIdCursor = null;
		long rawContactId = -1;
		try {
			rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
					new String[] { RawContacts._ID }, RawContacts.CONTACT_ID
							+ "=" + contactId, null, null);
			if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
				// Just return the first one.
				rawContactId = rawContactIdCursor.getLong(0);
			}
		} finally {
			if (rawContactIdCursor != null) {
				rawContactIdCursor.close();
			}
		}
		return rawContactId;
	}

	/**
	 * 取得已选择联系人信息
	 */
	private void getSelectedContacts() {
		mSelectedContacts = new ArrayList<ContactInfo>();
		for (ContactInfo info : group.getChilds())
			if (info.isChecked()) {
				info.setChecked(false);
				mSelectedContacts.add((ContactInfo) info.clone());
			}

		for (ContactInfo info : tempList)
			if (info.isChecked()) {
				info.setChecked(false);
				mSelectedContacts.add((ContactInfo) info.clone());
			}
	}
	
	/**
	 * 获取需要移出原群组的联系人列表
	 */
	private void getEmigration(){
		int i,j;
		i = group.getChilds().size();
		
		for(j=0;j<i;j++){
			ContactInfo ci = group.getChilds().get(j);
			if(!ci.isChecked()){
				group.getChilds().remove(j);
				mEmigrationList.add(ci);
				j--;
				i--;
			}
		}	
	}
	
	/**
	 * 取得需要移入原群组的联系人，从所有联系人中获取
	 */
	private void getImmigration(){
		for (ContactInfo info : tempList)
			if (info.isChecked()) {
				info.setChecked(false);
				mImmigrationList.add((ContactInfo) info.clone());
			}
	}
	/**
	 * 弹出对话框
	 */
	private void showDialog() {
		if (dialog == null) {
			dialog = new Dialog(EditGroupActiv.this, R.style.dialogs);
			dialog.setCancelable(true);
			dialog.setContentView(dialogView);
		}
		dialog.show();
	}

	/**
	 * 隐藏输入法
	 */
	private void hideIM(View v) {
		// 隐藏输入法
		InputMethodManager imm = (InputMethodManager) EditGroupActiv.this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	/**
	 * 销毁对象
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		flag = 0;
	}
	/**
	 * 拼音检索
	 */
	public int alphaIndexer(String s) {
		int i = -1, j = -1;
		for (ContactInfo o : group.getChilds()) {
			j++;
			String temp = o.getSortKey();
			if (temp.startsWith(s.toLowerCase())
					|| temp.startsWith(s.toUpperCase())) {
				i = j;
				break;
			}
		}
		return i;
	}

	private Handler letterHandler = new Handler() {
	};

	private class OverlayThread implements Runnable {

		public void run() {
			letterTv.setVisibility(View.GONE);
		}

	}

	public int alphaIndexer2(String s) {
		int i = -1, j = -1;
		for (ContactInfo o : tempList) {
			j++;
			String temp = o.getSortKey();
			if (temp.startsWith(s.toLowerCase())
					|| temp.startsWith(s.toUpperCase())) {
				i = j;
				break;
			}
		}
		return i;
	}

	@Override
	public void onTouchingLetterChanged(String s) {
		// TODO Auto-generated method stub
		if (alphaIndexer(s) > 0) {
			int position = alphaIndexer(s);
			mGroupLv.setSelection(position);
		}

		if (alphaIndexer2(s) > 0) {
			int position = alphaIndexer2(s);
			mAllContactLv.setSelection(position);
		}
		letterTv.setText(s);
		letterTv.setVisibility(View.VISIBLE);
		letterHandler.removeCallbacks(overlayThread);
		letterHandler.postDelayed(overlayThread, 500);
	}
	/**
	 * 按键监听
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

}
