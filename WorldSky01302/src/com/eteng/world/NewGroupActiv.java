package com.eteng.world;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
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
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.view.KeyEvent;
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
import com.wljsms.adapter.AllContacterAdapter;
import com.wljsms.info.ContactInfo;
import com.wljsms.info.GroupInfo;
import com.wljsms.util.Utils;

/**
 * 
 * com.eteng.world.NewGroupActiv
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-20 下午2:22:41 
 *         Description : 新建群组。 Modified :
 */
public class NewGroupActiv extends Activity implements
		OnTouchingLetterChangedListener {
	/**
	 * 群组名字输入框
	 */
	private EditText mGroupNameEt;
	//保存，取消，所有联系人TEXTVIEW
	private TextView mSaveTv, mCancelTv, mAllContactTv;
	/**
	 *  所有群组列表
	 */
	private ListView mAllContactLv; 
	//以下3行用于右侧边文字搜索
	private MyLetterView myView;
	private TextView letterTv;
	private OverlayThread overlayThread = new OverlayThread();
	 /**
	  * 返回按钮
	  */
	private ImageView mBackImg;
	/**
	 * 所有联系人集合
	 */
	private ArrayList<ContactInfo> mAllContactList;
	/**
	 * 应用对象
	 */
	private SmsApplication mApplication; 
	/**
	 * 所有联系人适配器
	 */
	private AllContacterAdapter mAllContacterAdapter;
	/**
	 * 联系人已选择提示信息
	 */
	private String mToastItemStr;
	/**
	 * 页面父布局，用于点击输入框意外的区域输入法消失
	 */
	private View ll;
	/**
	 * 新建群组标志，防止用户快速点击多次创建多个群组。True:正在创建，False：没有创建
	 */
	private boolean mIsCreated = false;
	/**
	 * 提示正在创建群组对话框
	 */
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_group_layout);
		mApplication = (SmsApplication) getApplication();
		mToastItemStr = getResources().getString(
				R.string.no_select_toast_item_str);
		mAllContactList = mApplication.getArrayList();
		initUI();
		mAllContactTv.setText("全部联系人(" + mAllContactList.size() + ")");
		mGroupNameEt.setText("群组" + mApplication.index);
		mAllContacterAdapter = new AllContacterAdapter(NewGroupActiv.this,
				mAllContactList, mSaveTv, mApplication);
		mAllContactLv.setAdapter(mAllContacterAdapter);
		//所有联系人列表单击事件
		mAllContactLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				//取得被点击的项
				ContactInfo tmp = mAllContactList.get(arg2);
				if (tmp.isChecked()) {
					//选中状态，设置为未选中
					tmp.setChecked(false);
					mApplication.removeContactInfo(tmp);
					mAllContacterAdapter.selectMinusOne();
				} else {
					//未选中
					if (mApplication.addContactInfo(tmp)) {
						//成功添加到已选择联系人列表
						tmp.setChecked(true);
						mAllContacterAdapter.selectAddOne();
					} else {
						//联系人已经被选择，弹出提示信息
						Utils.showCustomToast(NewGroupActiv.this, mToastItemStr);
						return;
					}

				}
				mAllContacterAdapter.notifyDataSetChanged();
			}
		});
	}

	/**
	 * 初始化UI
	 */
	private void initUI() {
		mGroupNameEt = (EditText) findViewById(R.id.new_group_edittext);
		mSaveTv = (TextView) findViewById(R.id.new_group_save);
		mCancelTv = (TextView) findViewById(R.id.new_group_cancel);
		mAllContactTv = (TextView) findViewById(R.id.new_group_all_contact);
		mAllContactLv = (ListView) findViewById(R.id.lv_all_contacts);
		mBackImg = (ImageView) findViewById(R.id.new_group_back);
		myView = (MyLetterView) findViewById(R.id.myGroupView);
		letterTv = (TextView) findViewById(R.id.new_group_tvLetter);
		letterTv.setVisibility(View.GONE);
		ll = findViewById(R.id.new_group_ll);
		
		ll.setOnClickListener(listener);
		mBackImg.setOnClickListener(listener);
		myView.setOnTouchingLetterChangedListener(this);
		mSaveTv.setOnClickListener(listener);
		mCancelTv.setOnClickListener(listener);
	}

	//按钮点击事件监听器
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.new_group_save) {
				// 点击确定，开始新建群组，弹出提示框
				if (mIsCreated) {
					Utils.showCustomToast(NewGroupActiv.this, "正在创建群组，请稍后重试！");
					return;
				}
				String name = mGroupNameEt.getText().toString();
				if (name == null || name.equals("")) {
					Utils.showCustomToast(NewGroupActiv.this, "请输入群组名");
					return;
				}
				dialog = ProgressDialog.show(NewGroupActiv.this, null,
						"正在创建群组,请等待...");
				mIsCreated = true;
				new NewGroupTast().execute(null, null, null);
			} else if (v.getId() == R.id.new_group_cancel) {
				//点击取消，返回群组页面
				mApplication.getmContactsList().clear();
				mApplication.selectedContactsCount = 0;
				Intent intent = new Intent();
				intent.setClass(NewGroupActiv.this, GroupMain.class);
				startActivity(intent);
				NewGroupActiv.this.finish();
			} else if (v.getId() == R.id.new_group_back) {
				//点击返回
				mApplication.getmContactsList().clear();
				mApplication.selectedContactsCount = 0;
				Intent intent = new Intent();
				intent.setClass(NewGroupActiv.this, GroupMain.class);
				startActivity(intent);
				NewGroupActiv.this.finish();
			} else if (v.getId() == R.id.new_group_ll){
				//点击页面其他地方，隐藏输入法
				hideIM(v);
			}	
		}
	};

	//异步任务，用于建立群组
	class NewGroupTast extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			//新建群组完毕，取消等待对话框，返回群组页面
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}
			mIsCreated = false;
			Intent intent = new Intent();
			intent.setClass(NewGroupActiv.this, GroupMain.class);
			mApplication.getmContactsList().clear();
			mApplication.selectedContactsCount = 0;
			startActivity(intent);
			NewGroupActiv.this.finish();
		}

		/**
		 * 运行在后台的线程
		 */
		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			String name = mGroupNameEt.getText().toString();
			//取得选择联系人列表
			List<ContactInfo> temp = getSelectedContact();
			//新建一个群组
			Uri groupUri = saveNewGroup(name);
			//解析群组的id
			long groupId = ContentUris.parseId(groupUri);
			//创建一个群组对象，并设置群组对象的相关信息
			GroupInfo group = new GroupInfo();
			group.setName(name);
			group.setId(groupId);
			group.setChilds(temp);
			mApplication.getmGroups().add(group);
			mApplication.sortGroupDesc();
			mApplication.index++;
			for (ContactInfo info : temp) {
				//把联系人移入群组并设置联系人的群组id
				moveIntoGroup(info.getContactId(), groupId);
				info.setGroupId(groupId);
			}
			return null;
		}

	}

	/**
	 * 新建一个群组
	 */
	private Uri saveNewGroup(String newGroupName) {
		ContentValues values = new ContentValues();
		values.put(Groups.TITLE, newGroupName);
		return getContentResolver().insert(Groups.CONTENT_URI, values);
	}

	/**
	 * 把id为contactId的联系人移进一个分组
	 * 
	 * @param contactId：联系人ID
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
	 * 取得用户选择的联系人
	 */
	private List<ContactInfo> getSelectedContact() {
		List<ContactInfo> temp = new ArrayList<ContactInfo>();
		for (ContactInfo info : mAllContactList) {
			if (info.isChecked()) {
				info.setChecked(false);
				temp.add((ContactInfo) info.clone());
			}
		}
		return temp;
	}

	/**
	 * 销毁对象
	 */

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		dialog = null;

	}

	/**
	 * 隐藏输入法
	 */
	private void hideIM(View v) {
		InputMethodManager imm = (InputMethodManager) NewGroupActiv.this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	private Handler letterHandler = new Handler() {
	};

	private class OverlayThread implements Runnable {

		public void run() {
			letterTv.setVisibility(View.GONE);
		}

	}

	/**
	 * 拼音字母检查
	 * @param s : 拼音字母
	 * @return listview中的索引位置
	 */
	public int alphaIndexer(String s) {
		int i = -1, j = -1;
		for (ContactInfo o : mAllContactList) {
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

	/**
	 *右侧字母触控方法
	 */
	@Override
	public void onTouchingLetterChanged(String s) {
		// TODO Auto-generated method stub
		if (alphaIndexer(s) > 0) {
			int position = alphaIndexer(s);
			mAllContactLv.setSelection(position);
			letterTv.setText(s);
			letterTv.setVisibility(View.VISIBLE);
			letterHandler.removeCallbacks(overlayThread);
			letterHandler.postDelayed(overlayThread, 500);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

}
