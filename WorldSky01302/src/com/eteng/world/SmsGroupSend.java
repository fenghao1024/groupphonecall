/**
 * Name : SmsGroupSend.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.eteng.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.NullCipher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kobjects.base64.Base64;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.StaticLayout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wljsms.adapter.SelectContactAdapter;
import com.wljsms.adapter.SmsGroupAdapter;
import com.wljsms.adapter.SelectContactAdapter.ViewHolder;
import com.wljsms.config.AppConstant;
import com.wljsms.config.MeetingSoap;
import com.wljsms.config.MyHttpClient;
import com.wljsms.database.MySqliteHelper;
import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.info.ContactInfo;
import com.wljsms.info.GroupInfo;
import com.wljsms.sms.service.SmsSendingService;
import com.wljsms.tools.Base64Tool;
import com.wljsms.tools.DES;
import com.wljsms.tools.DesEncrypter;
import com.wljsms.tools.JsonParamMaker;
import com.wljsms.util.GlobalData;
import com.wljsms.util.Utils;

/**
 * com.eteng.world.SmsGroupSend
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-20 上午9:42:03 Description : 发短信页面，加了群组功能 Modified :
 */
public class SmsGroupSend extends Activity {
	/**
	 * 短信编辑文本
	 */
	//private EditText mSmsEdittext;
	/**
	 * 网格对象
	 */
	private GridView mGridview;
	/**
	 * 群组列表
	 */
	private ExpandableListView mGroupList;
	/**
	 * 应用对象
	 */
	private SmsApplication mApplication;
	/**
	 * 返回图形对象
	 */
	private ImageView mSmsBackImage;
	/**
	 * 添加联系人按钮
	 */
	private ImageView mSmsAddGroupImage;
	/**
	 * 发送文本对象
	 */
	//private TextView mSendTv;
	/**
	 * 收件人数量
	 */
	private TextView mContactsCount;
	/**
	 * 群组数量
	 */
	private TextView mGroupCount;
	/**
	 * 选择的联系人
	 */
	private List<ContactInfo> mSelectedContacts = new ArrayList<ContactInfo>();
	/**
	 * 联系人组的信息
	 */
	private List<GroupInfo> mGroups;
	/**
	 * 消息处理对象
	 */
	private Handler handler;
	/**
	 * 加载等待对话框
	 */
	private ProgressDialog dialog;
	/**
	 * 选择联系人适配器
	 */
	private SelectContactAdapter mGridAdapter;
	/**
	 * 群组适配器
	 */
	private SmsGroupAdapter mGroupAdapter;
	/**
	 * 收件人为空的提示Textview
	 */
	private View mGvEmptyView;
	/**
	 * 数据库对象
	 */
	private MySqliteHelper database;
	/**
	 * 页面父布局，用于点击EditText以外的其他区域隐藏输入法
	 */
	private View ll;
	/**
	 * SharedPreferences对象
	 */
	private SharedPreferences sp;
	/**
	 * 草稿内容
	 */
	private String mDraft;
	/**
	 * 判断是否以草稿箱的方式进入
	 */
	private boolean mIsDraft = false;
	/**
	 * 是否从返回页面点击修改
	 */
	private boolean mIsModify = false;
	/**
	 * 通知创建时间，删除草稿用。
	 */
	private String groupDate;
	/**
	 * 文字提示信息
	 */
	private String mToastItemStr, mToastGroupStr;
	/**
	 * 无群组提示view
	 */
	private View mNoGroupView;
	private View viewLL;
	/**
	 * 短信数
	 */
	private int mSmsCount = 0;
	/**
	 * 短信条数
	 */
	private int mWordNum = 0;
	/**
	 * 是否单击
	 */
	private boolean mClicked = false;
	/**
	 * 是否保存草稿
	 */
	private boolean mSaveDrafted = false;
	/**
	 * 后台正在发送短信过程中上一次点击发送按钮的时间
	 */
	private long mLastSendingClickedTime;
	
	
	
	private static final int CONNECT_FAILD = 100000;
	private static final int CONNECT_SUCCESS = 111111;
	public static final int NO_SELF_PHONENUM = 111;
	public static final int NO_SELECTEDCONTACTS = 101010;
	public static final int CONTACTS_OVERFLOW = 101111;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sms_send_group_layout);
		mLastSendingClickedTime = System.currentTimeMillis();
		mApplication = (SmsApplication) getApplication();
		mToastItemStr = getResources().getString(
				R.string.no_select_toast_item_str);
		mToastGroupStr = getResources().getString(
				R.string.no_select_toast_group_str);
		database = mApplication.getDatabase();
		sp = getSharedPreferences(ConstantsInfo.SHARED_FILE_NAME, 0);
		// 取得传递过来的草稿信息,包括在短信页面输入内容，然后再添加联系人，回到发短信页面，把输入的内容带回来
		mDraft = getIntent().getStringExtra("draft_str");
		mIsDraft = this.getIntent().getBooleanExtra("draft", false);
		// 是否是在确认页面点击修改返回
		mIsModify = this.getIntent().getBooleanExtra("modify", false);
		// 通知时间，用户删除草稿
		groupDate = getIntent().getStringExtra("groupDate");
		mApplication.setGroupDate(groupDate);
		
		
		
		DefaultPhoneStateListener myPhoneStateListener = new DefaultPhoneStateListener(this.getApplicationContext());
        
	    ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
			.listen(myPhoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE);
				
		initUIView();
		//mSmsEdittext.setText(mDraft);
		if (!mApplication.ismLoadContacterComplete()) {
			dialog = ProgressDialog.show(SmsGroupSend.this, null,
					"正在加载联系人信息...");
			dialog.setCancelable(true);
			return;
		}

		initData();

	}

	/**
	 * 以草稿方式进入时，更改群组的状态
	 * 
	 * @param temp
	 *            ： 联系人列表
	 */
	private void updateGroupStatusAsDraft(List<ContactInfo> temp) {
		DebugFlags.EtengLog("以草稿方式进入，更新组和联系人状态,草稿中联系人为 ：" + temp.size());
		int count = 0;// temp中已更新数目，等于temp.size是所有的草稿人都处理了，可以停止遍历
		List<ContactInfo> mContacts;// 零时联系人列表
		// 遍历群组
		for (GroupInfo gi : mGroups) {
			// 群组里联系人列表
			mContacts = gi.getChilds();
			if (mContacts.size() == 0) {
				// 组里没有联系人，遍历下一组
				continue;
			}
			boolean someSelected = false;// 选择部分联系人
			int groupCount = 0;// 组在草稿人列表中的成员数
			for (ContactInfo ci : mContacts) {
				for (ContactInfo ci1 : temp) {
					if (ci1.getmNewContact().equals("2")) {
						// 新联系人，通讯录里没有，跳过此联系人，遍历下一个
						continue;
					}
					if (ci.getName().equals(ci1.getName())
							&& ci.getPhone().equals(ci1.getPhone())) {
						// 匹配一个联系人，更新群组里的联系人状态，并把此联系人加入选择联系人列表,从群组里加入联系人
						// 匹配一个联系人信息
						if (ci1.isChecked())// 草稿中的此联系人已经匹配，匹配下一个联系人，此种情况用于处理联系人中有重复联系人的情况
							break;
						ci.setChecked(true);
						ci1.setChecked(true);
						ci.setGroupId(gi.getId());
						if (mApplication.addContactInfo(ci)) {
							someSelected = true;
							count++;
							groupCount++;
						}
						// 因为草稿里联系人没有重复的，找到一个后，可以结束本次遍历。
						break;
					}
				}

				if (count == temp.size()) {
					// 草稿联系人已全部处理完毕,结束函数的执行
					// 设置组的状态
					if (groupCount == mContacts.size()) {
						// 组的所有成员都包含在草稿人中
						gi.setSomeChecked(false);
						gi.setAllChecked(true);
					} else {
						gi.setSomeChecked(someSelected);
						gi.setAllChecked(false);
					}
					// 草稿列表中的数据可以清除了。
					temp.clear();
					return;
				}
			}
			// 设置组的状态
			if (groupCount == mContacts.size()) {
				// 组的所有成员都包含在草稿人中
				gi.setSomeChecked(false);
				gi.setAllChecked(true);
			} else {
				gi.setSomeChecked(someSelected);
				gi.setAllChecked(false);
			}
		}

		// 遍历所有联系人
		mContacts = mApplication.getArrayList();
		for (ContactInfo ci : mContacts) {
			for (ContactInfo ci1 : temp) {
				if (ci1.getmNewContact().equals("2")) {
					// 新联系人，通讯录里没有，跳过此联系人，遍历下一个
					continue;
				}
				if (ci.getName().equals(ci1.getName())
						&& ci.getPhone().equals(ci1.getPhone())) {
					// 匹配一个联系人信息
					if (ci1.isChecked())// 草稿中的此联系人已经匹配，匹配下一个联系人，此种情况用于处理联系人中有重复联系人的情况
						break;
					ci.setChecked(true);
					ci1.setChecked(true);// 草稿中的此联系人已经匹配，
					if (mApplication.addContactInfo(ci)) {
						count++;
					}
					break;
				}
			}
			if (count == temp.size()) {
				// 处理完毕
				temp.clear();
				return;
			}
		}

		// 遍历列表，查找新联系人，加入到新联系人选择列表
		for (ContactInfo info : temp) {
			if (!info.isChecked()) {
				// 在群组和所有联系人中都没有匹配的，即系统通讯录里没有此联系人，认为是新联系人
				mApplication.addNewContactInfo(info.getPhone());
			}
		}
		temp.clear();
	}

	/**
	 * 初始化数据
	 */
	@SuppressWarnings("unchecked")
	private void initData() {
		mGroups = mApplication.getmGroups();
		mGroupCount.setText("(" + mGroups.size() + ")");
		if (mIsDraft) {
			// 以草稿的方式进入，取得传过来的电话号码和名称,传入的数据是ContactInfo列表
			List<ContactInfo> temp = (List<ContactInfo>) getIntent()
					.getExtras().getSerializable("draft_phone");
			updateGroupStatusAsDraft(temp);
			mDraft = getIntent().getStringExtra("draft_data");
			//mSmsEdittext.setText(mDraft);
			mApplication.setGroupDate(groupDate);
		} else if (mIsModify) {
			// 从确认页面点击修改返回
			mDraft = getIntent().getStringExtra("draft_data");
			//mSmsEdittext.setText(mDraft);
		}
		// 重置草稿的内容
		mApplication.mDraft = mDraft;
		// 把通讯录里现在的联系人加入到选择列表
		for (ContactInfo info : mApplication.getmContactsList())
			mSelectedContacts.add(info);
		// 把新联系人加入到选择列表
		for (ContactInfo info : mApplication.getmNewContactsList())
			mSelectedContacts.add(info);

		if (mSelectedContacts.size() == 0)
			mGvEmptyView.setVisibility(View.VISIBLE);
		else {
			mGvEmptyView.setVisibility(View.GONE);
			mContactsCount.setText("(" + mSelectedContacts.size() + ")");
		}

		mGroupAdapter = new SmsGroupAdapter(this, mGroups);
		mGroupAdapter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 选择或反选组里的联系人
				int groupPosition = (Integer) v.getTag();

				GroupInfo info = mGroups.get(groupPosition);
				if (info.getChilds().size() == 0)// 组里没有联系人，直接返回
					return;
				if (!info.isAllChecked() && !info.isSomeChecked()) {
					// 组里没有联系人被选择,选择全部联系人
					List<ContactInfo> temp = info.getChilds();
					boolean flag = false;// 联系人没被选择
					boolean flag1 = false;// 选择了联系人
					for (ContactInfo ci : temp) {
						if (mApplication.addContactInfo(ci)) {
							ci.setChecked(true);
							mSelectedContacts.add(ci);
							flag1 = true;
						} else {
							flag = true;
						}
					}

					if (!flag1) {// 没有选择任何联系人

						Utils.showCustomToast(SmsGroupSend.this, mToastGroupStr);
						return;
					}
					if (flag) {// 选择了部分联系人

						Utils.showCustomToast(SmsGroupSend.this, mToastGroupStr);
						info.setAllChecked(false);
						info.setSomeChecked(true);
					} else {
						// 全部选择
						info.setAllChecked(true);
						info.setSomeChecked(false);
					}
					mGvEmptyView.setVisibility(View.GONE);
				} else {
					// 组里部分或全部联系人被选择，反选择组里的所有联系人
					info.setAllChecked(false);
					info.setSomeChecked(false);
					List<ContactInfo> temp = info.getChilds();
					for (ContactInfo ci : temp) {
						if (ci.isChecked()) {
							// 联系人被选择，反选,并从已选择联系人列表中去除
							ci.setChecked(false);
							mSelectedContacts.remove(ci);
							mApplication.removeContactInfo(ci);
						}

					}
					if (mSelectedContacts.size() == 0)
						mGvEmptyView.setVisibility(View.VISIBLE);
					else {
						mGvEmptyView.setVisibility(View.GONE);
					}
				}
				mContactsCount.setText("(" + mSelectedContacts.size() + ")");
				mGridAdapter.notifyDataSetChanged();
				mGroupAdapter.notifyDataSetChanged();

			}

		});
		mGroupList.setAdapter(mGroupAdapter);

		// 如果群组页面选择了组的状态为展开，这里也要展开组，以对应
		int i = 0;
		for (GroupInfo gi : mGroups) {
			if (gi.isExpandable())
				mGroupList.expandGroup(i);
			i++;
		}

		mGridAdapter = new SelectContactAdapter(this, mSelectedContacts);
		mGridAdapter.setOnClick(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 点击了已选择联系人里的减号，去除联系人，并更新数据状态
				int pos = ((ViewHolder) v.getTag()).position;
				ContactInfo info = mSelectedContacts.remove(pos);
				// 从application中删除联系人信息
				mApplication.removeContactInfo(info);
				mApplication.removeNewContactInfo(info.getPhone());
				info.setChecked(false);
				if (mSelectedContacts.size() == 0)
					mGvEmptyView.setVisibility(View.VISIBLE);
				else {
					mGvEmptyView.setVisibility(View.GONE);
					mContactsCount.setText("(" + mSelectedContacts.size() + ")");
				}

				// 更新群组的状态
				GroupInfo group = null;
				long groupId = info.getGroupId();
				for (GroupInfo gi : mGroups) {
					if (gi.getId() == groupId) {
						group = gi;
						break;
					}
				}
				updateGroupState(group);
				mGridAdapter.notifyDataSetChanged();
				mGroupAdapter.notifyDataSetChanged();
			}

		});
		mGridview.setAdapter(mGridAdapter);

		mGroupList.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				GroupInfo group = mGroups.get(groupPosition);
				List<ContactInfo> childs = group.getChilds();
				ContactInfo info = childs.get(childPosition);
				if (info.isChecked()) {
					info.setChecked(false);
					mSelectedContacts.remove(info);
					mApplication.removeContactInfo(info);
					if (mSelectedContacts.size() == 0)
						mGvEmptyView.setVisibility(View.VISIBLE);
					else {
						mGvEmptyView.setVisibility(View.GONE);
					}
				} else {
					mGvEmptyView.setVisibility(View.GONE);
					if (mApplication.addContactInfo(info)) {
						info.setChecked(true);
						mSelectedContacts.add(info);
					} else {
						Utils.showCustomToast(SmsGroupSend.this, mToastItemStr);
					}
				}
				mContactsCount.setText("(" + mSelectedContacts.size() + ")");
				updateGroupState(group);
				mGridAdapter.notifyDataSetChanged();
				mGroupAdapter.notifyDataSetChanged();
				return true;
			}

		});
		mGroupList.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				// TODO Auto-generated method stub
				GroupInfo info = mGroups.get(groupPosition);
				if (info.isExpandable()) {
					info.setExpandable(false);
					mGroupList.collapseGroup(groupPosition);
				} else {
					info.setExpandable(true);
					mGroupList.expandGroup(groupPosition);
				}
				return true;
			}

		});

		if (mGroups.size() == 0) {
			mNoGroupView.setVisibility(View.VISIBLE);
			viewLL.setVisibility(View.GONE);
		}
//		mSmsEdittext.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void afterTextChanged(Editable s) {
//				// TODO Auto-generated method stub
//				computeSmsCount(s.toString());
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before,
//					int count) {
//				// TODO Auto-generated method stub
//
//			}
//
//		});

		//computeSmsCount(mSmsEdittext.getText().toString());
	}

	/**
	 * 保存用户选择的联系人和输入的内容到草稿箱，选择了多少联系人，保存多少条记录，草稿箱状态为9
	 */
	private boolean saveToDraft() {
		if (mSaveDrafted)
			return false;
		mSaveDrafted = true;
		database.deleteNotify(mApplication.getGroupDate());
		//String tmpContent = mSmsEdittext.getText().toString();
		String tmpContent = "sdfsdf";
		if (!tmpContent.equals("") || mSelectedContacts.size() != 0) {

			if (mSelectedContacts.size() == 0) {
				ContactInfo contactInfo = new ContactInfo();
				contactInfo.setPhone("");
				contactInfo.setName("");
				mSelectedContacts.add(contactInfo);
			}
			long time = System.currentTimeMillis();
			String date = Utils.formatDate(time);
			String time_str = Long.toString(time);
			String taskId = time_str + mApplication.getImsi();
			database.getWritableDatabase().beginTransaction();
			for (ContactInfo info : mSelectedContacts) {
				database.insertData(info.getName(), info.getPhone(),
						tmpContent, "9", "0", date, "", date, "0", time_str,
						taskId, "0");
			}
			database.getWritableDatabase().setTransactionSuccessful();
			database.getWritableDatabase().endTransaction();
			clearCheckedData();
			return true;
		} else {
			clearCheckedData();
			return false;
		}
	}

	/**
	 * 判断字符串是中问，英文还是中英文混合
	 * 
	 * @param 说明参数含义
	 * @return 1:中文，2：英文，3：中英文
	 */
	private int checkString(String str) {
		char[] c1 = str.toCharArray();
		boolean chinese = false, english = false;
		for (int i = 0; i < c1.length; i++) {
			int num = c1[i];
			String numstr = Integer.toBinaryString(num);
			if (numstr.length() > 8) {
				chinese = true;
			} else {
				english = true;
			}
		}

		if (chinese && !english)
			return 1;

		if (english && !chinese)
			return 2;

		return 3;
	}

	/**
	 * 计算输入的文字短信数量,中文70个字，英文140，中英文混合算中文
	 */
	private void computeSmsCount(String s) {
		int flag = checkString(s.toString());
		mWordNum = s.length();
		if (flag == 2) {
			// 纯英文
			mSmsCount = s.length() / 160 + 1;
			int a = s.length() % 160;
			if (a != 0)
				mSmsCount = s.length() / 160 + 1;
			else
				mSmsCount = s.length() / 160;

		} else {
			// 纯中文或中英文，当中问处理
			int a = s.length() % 70;
			if (a != 0)
				mSmsCount = s.length() / 70 + 1;
			else
				mSmsCount = s.length() / 70;
		}
		//mSendTv.setText("发送" + "\n" + "(" + mWordNum + "/" + mSmsCount + ")");
	}

	/**
	 * 把选择的联系人的信息清零
	 */
	public void clearCheckedData() {
		List<ContactInfo> mContacts;
		mContacts = mApplication.getArrayList();
		for (ContactInfo ci : mContacts)
			ci.setChecked(false);
		for (GroupInfo gi : mApplication.getmGroups()) {
			gi.setAllChecked(false);
			gi.setSomeChecked(false);
			gi.setExpandable(false);
			mContacts = gi.getChilds();
			for (ContactInfo ci : mContacts) {
				ci.setChecked(false);
			}
		}
		mContacts = mApplication.getmRecentContactList();

		for (ContactInfo ci : mApplication.getmRecentContactList())
			ci.setChecked(false);
		mApplication.getmContactsList().clear();
		mApplication.getmNewContactsList().clear();
		mSelectedContacts.clear();
		mApplication.selectedContactsCount = 0;
	}

	/**
	 * 更新组的状态,主要是依据组里成员的状态设置全部选择和部分选择的状态。
	 * 
	 * @param group
	 *            : 群组信息
	 * 
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
	 * 初始化UI
	 */
	private void initUIView() {
		mSmsBackImage = (ImageView) findViewById(R.id.sms_send_group_back);
		mSmsAddGroupImage = (ImageView) findViewById(R.id.sms_send_group_add);
		//mSmsEdittext = (EditText) findViewById(R.id.sms_group_edittext);
		mGridview = (GridView) findViewById(R.id.sms_gridview);
		mGroupList = (ExpandableListView) findViewById(R.id.lv_group);
		mGvEmptyView = findViewById(R.id.gv_empty_view);
		mContactsCount = (TextView) findViewById(R.id.sms_contacter_count);
		mGroupCount = (TextView) findViewById(R.id.sms_contacter_group_count);
		//mSendTv = (TextView) findViewById(R.id.sms_group_send);
		ll = findViewById(R.id.sms_send_group_layout_parent);
		mNoGroupView = findViewById(R.id.sms_no_group_img);
		viewLL = findViewById(R.id.view_ll);

		ll.setOnClickListener(listener);
		//mSendTv.setOnClickListener(listener);
		mSmsBackImage.setOnClickListener(listener);
		mSmsAddGroupImage.setOnClickListener(listener);

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (msg.what == 1) {
					if (dialog != null && dialog.isShowing())
						dialog.dismiss();
					initData();

				}
			}

		};
		mApplication.setHandler(handler);

	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			Intent intent ;
			switch (id) {
			case R.id.sms_send_group_back:
				intent = new Intent();
				intent.setClass(SmsGroupSend.this, SettingActivity.class);
				startActivity(intent);
				
				break;
				
			case R.id.sms_send_group_add:
				// 选择联系人
				intent = new Intent();
				intent.setClass(SmsGroupSend.this, GroupMain.class);
				startActivity(intent);
				//mApplication.mDraft = mSmsEdittext.getText().toString();
				mApplication.selectedContactsCount = mSelectedContacts.size();
				SmsGroupSend.this.finish();
				break;
				
//			case R.id.sms_group_send:
//				// 发送
//				mApplication.setDialog(false);
//				mApplication.setPost(false);
//				// 发送
//				SendSms();
//				break;
				
			case R.id.sms_send_group_layout_parent:
				hideIM(v);
				break;

			default:
				break;
			}
			
		}
			
//			if (v.getId() == R.id.sms_send_group_back) {
//				// 退出
//				if (saveToDraft()) {
//					Utils.showCustomToast(SmsGroupSend.this, "保存到草稿箱成功！");
//					sendBroadcast(new Intent(
//							ConstantsInfo.SMS_DRAFT_SAVED_BROADCAST));
//				}
//				mApplication.getmContactsList().clear();
//				mApplication.getmNewContactsList().clear();
//				mApplication.selectedContactsCount = 0;
//				mSelectedContacts.clear();
//				mApplication.mDraft = "";
//				SmsGroupSend.this.finish();
//			} else if (v.getId() == R.id.sms_send_group_add) {
//				// 选择联系人
//				Intent intent = new Intent();
//				intent.setClass(SmsGroupSend.this, GroupMain.class);
//				startActivity(intent);
//				mApplication.mDraft = mSmsEdittext.getText().toString();
//				mApplication.selectedContactsCount = mSelectedContacts.size();
//				SmsGroupSend.this.finish();
//			} else if (v.getId() == R.id.sms_group_send) {
//				// 发送
//				mApplication.setDialog(false);
//				mApplication.setPost(false);
//				// 发送
//				SendSms();
//			} else if (v.getId() == R.id.sms_send_group_layout_parent)
//				hideIM(v);
//		}

	};

	/**
	 * 隐藏输入法
	 */
	private void hideIM(View v) {
		// 隐藏输入法
		InputMethodManager imm = (InputMethodManager) SmsGroupSend.this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	/**
	 * 发送短信方法
	 */
	private void SendSms() {
		if (mApplication.getImsi() == null || mApplication.getImsi().equals("")) {
			Utils.showCustomToast(SmsGroupSend.this, "检测不到手机卡，无法发通知");
			StringBuffer sb = new StringBuffer();
			// 取得被通知人的号码清单
			for (ContactInfo info : mSelectedContacts) {
				sb.append(info.getPhone());
				sb.append(",");
			}
			String phones = sb.toString();
			// 去掉最后一个逗号
			phones = phones.substring(0, phones.length() - 1);
			// 插入一条错误记录
			Utils.insertSmsSendFailedErrorLog(database, "", "50006", phones,
					Utils.formatDate(System.currentTimeMillis()));
			return;
		}
		
		if (!mApplication.ismSendingSmsComplete()
				|| System.currentTimeMillis()
						- mApplication.getmSmsSendedTime() < 4 * 1000L) {
			if (System.currentTimeMillis() - mLastSendingClickedTime > 4 * 1000L) {
				mLastSendingClickedTime = System.currentTimeMillis();
				Utils.showCustomToast(SmsGroupSend.this, "后台正在发送短信，请稍后重试！");
			}
			return;
		}
		
		if (!checkSendInfo())
			return;

		if (mClicked)
			return;
		mClicked = true;

		if (!getChecked()) {
			// 跳转到发送确认页面
			Intent intent = new Intent();
			intent.setClass(SmsGroupSend.this, SmsConfirmActiv.class);
			//intent.putExtra("sms_content", mSmsEdittext.getText().toString());
			intent.putExtra("groupDate", groupDate);
			intent.putExtra("sms_count", mSmsCount);
			intent.putExtra("word_number", mWordNum);
			intent.putExtra("draft", mIsDraft);
			startActivity(intent);
			SmsGroupSend.this.finish();
			return;
		}

		stopService(new Intent(SmsGroupSend.this, SmsSendingService.class));

		// 发送前情况草稿箱里的数据,调用删除草稿箱代码
		if (mIsDraft)
			database.deleteNotify(groupDate);

		//String tmpContent = mSmsEdittext.getText().toString();
		
		String tmpContent = "asdfasd";
		// 把数据插入到急短信数据库
		long time = System.currentTimeMillis();
		String time_str = Long.toString(time);
		String taskId = time_str + mApplication.getImsi();
		String mDateStr = Utils.formatDate(time);
		List<ContactInfo> tmpData = new ArrayList<ContactInfo>();
		database.getWritableDatabase().beginTransaction();
		for (ContactInfo info : mSelectedContacts) {
			ContactInfo tmp = new ContactInfo();
			tmp.setName(info.getName());
			tmp.setPhone(info.getPhone());
			tmpData.add(tmp);
			insertDataToDB(info.getName(), info.getPhone(), tmpContent, "1",
					mDateStr, time_str, taskId);
		}
		database.getWritableDatabase().setTransactionSuccessful();
		database.getWritableDatabase().endTransaction();
		// 发送发通知广播，让通知页面刷新数据
		sendBroadcast(new Intent(ConstantsInfo.SMS_TASK_SENDED_BROADCAST));
		// 启动发短信服务
		Intent iii = new Intent();
		Bundle bundle1 = new Bundle();
		bundle1.putSerializable(ConstantsInfo.SMS_SEND_CONTACT_LIST,
				(Serializable) tmpData);
		iii.putExtra(ConstantsInfo.TIME, time);
		iii.putExtra(ConstantsInfo.DATE_STR, mDateStr);
		iii.setClass(SmsGroupSend.this, SmsSendingService.class);
		iii.putExtras(bundle1);
		iii.putExtra(ConstantsInfo.SMS_CONTENT, tmpContent);
		startService(iii);

		clearCheckedData();
		mApplication.loadRecentContacter();
		SmsGroupSend.this.finish();

	}

	/**
	 * 发送前检测内容，收件人和群组均不能为空
	 */
	private boolean checkSendInfo() {
		//String content = Utils.replaceBlank(mSmsEdittext.getText().toString());
		String content = Utils.replaceBlank("asdfasdf");
		if (mSelectedContacts.size() == 0) {
			Utils.showCustomToast(SmsGroupSend.this, "老板，你还没选择被通知人");
			return false;
		}
		if (content == null || content.equals("")) {
			Utils.showCustomToast(SmsGroupSend.this, "老板，总得写点啥才能发的...");
			return false;
		}

		return true;
	}
	
	public void contact_grid_on_click(View v)
	{
		Utils.showCustomToast(SmsGroupSend.this, "老板，你还没选择被通知人");
	}
	
	// 插入数据到数据库
	private void insertDataToDB(String name, String phone, String content,
			String status, String date_str, String time_str, String taskId) {
		database.insertData(name, phone, content, status, "0", date_str, "",
				date_str, "0", time_str, taskId, "0");
	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//			if (saveToDraft()) {
//				Utils.showCustomToast(SmsGroupSend.this, "保存到草稿箱成功！");
//				sendBroadcast(new Intent(
//						ConstantsInfo.SMS_DRAFT_SAVED_BROADCAST));
//			}
//			// 清除选择的联系人信息
//			mApplication.getmContactsList().clear();
//			mApplication.getmNewContactsList().clear();
//			mApplication.selectedContactsCount = 0;
//			mSelectedContacts.clear();
//			mApplication.mDraft = "";
//			finish();
//			return true;
//		}
//		return false;
//
//	}

	/**
	 * 用户在发送确认页面是否点击了不再提示，True:不再跳转，False：跳转到确认页面
	 */
	private boolean getChecked() {
		return sp.getBoolean(ConstantsInfo.CHECKED, false);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		mClicked = false;
//		mApplication.setHandler(null);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	

	public List<String[]> ContactListmaking(List<ContactInfo> contactList)
	{
		List<String[]> list = new ArrayList<String[]>();
		if (contactList == null && contactList.size() == 0) 
		{	
			return null;
		}
		
		for (ContactInfo contactInfo : contactList) {
			String[] str = new String[2];
			str[0] = "phone";
			str[1] = contactInfo.getPhone();
			list.add(str);
		} 
		return list;
	}
	
	public void call_click(View view)
	{
		//str = "asdfqwerasdf";
	
		this.call();		
	}
	
	public void call()
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				Message msg = handler2.obtainMessage();
				
				List<ContactInfo> callOut_list = new ArrayList<ContactInfo>();
				if(GlobalData.getInstance().getMyPhoneNum() == null || GlobalData.getInstance().getMyPhoneNum().equals(""))
				{
					msg.what = NO_SELF_PHONENUM ;
					handler2.sendMessage(msg);
					return;
				}
				
				callOut_list.add(GlobalData.getInstance().getMyselfContactInfo());
				
				if(mSelectedContacts.size() == 0 || mSelectedContacts == null)
				{
					msg.what = NO_SELECTEDCONTACTS;
					handler2.sendMessage(msg);
					return;
				}
				
				callOut_list.addAll(mSelectedContacts);
				
				if(callOut_list.size() > 10)
				{
					msg.what = CONTACTS_OVERFLOW;
					handler2.sendMessage(msg);
					return;
				}
				
				GlobalData.getInstance().setCallOutContactInfo(callOut_list);
				
				List<String[]> contactList =  ContactListmaking(callOut_list);
				
				if(contactList == null)
				{
					Utils.showCustomToast(getApplicationContext(), "非常抱歉，程序出现问题，请重试");
					return;
				}
				
				String param64String = CreateMeeting_Param_Maker(contactList);
				
				String resultString = MyHttpClient.DoSomething(AppConstant.METHOD_CREATE_MEETING, param64String);
				
				String paramString = CreateMeeting_WebServiceParam_Maker(param64String);
				if(paramString == null)
				{
					return;
				}
				
				//MeetingSoap soap = new MeetingSoap();
				//String resultString = soap.Launch(AppConstant.WEBSERVICE_METHOD, paramString);
				
				if(resultString == null || resultString.equals(""))
				{
					msg.what = CONNECT_FAILD;
					handler2.sendMessage(msg);
					return;
				}
				
				msg.what = CONNECT_SUCCESS;
				msg.obj = resultString;
				handler2.sendMessage(msg);

			}
		}).start();
	}
	
	private String CreateMeeting_Param_Maker(List<String[]> contactList)
	{
		String contactJString = JsonParamMaker.listToJString(contactList);
		contactJString = JsonParamMaker.toJsonArray("recevList", contactJString);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		
		paramMap.put("userName", "txl");
		paramMap.put("password", "txlpassword");
		
		String userInfo = JsonParamMaker.MapToJStr(paramMap);
		
		contactJString += ",";
		
		contactJString += userInfo;
		
		contactJString = JsonParamMaker.toCompleteJson(contactJString);
		
		String param64String = Base64Tool.encode(contactJString);
		return param64String;
	}
	
	
	private String CreateMeeting_WebServiceParam_Maker(String httpParam)
	{
		String resultString = "";
		
	        String uuidString = Utils.UUID_Maker(); 

			Map<String, String> paramMap = new HashMap<String, String>();
			
			
			paramMap.put("username", "13985561586");
			paramMap.put("password", "123456");
			paramMap.put("meettype", String.valueOf(AppConstant.MSG_CREATE_MEETING));
			paramMap.put("uuid", uuidString);
			paramMap.put("url",  AppConstant.MEETING_REQPARAM + "=" + httpParam);
			
			String param = JsonParamMaker.MapToJStr(paramMap);
			
			param = JsonParamMaker.toCompleteJson(param);
			
			try {
				resultString = DesEncrypter.encode(param);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				resultString = null;
			}
			
		return resultString;
	}
	
	
	private Map<String, String> parseResultString(String sourceString)
	{
		Map<String, String> map = new HashMap<String, String>();
		
		try
		{
			JSONObject jsonObject = new JSONObject(sourceString);
			String resultMsgString = jsonObject.getString("remsg");
			String uuidString = jsonObject.getString("uuid");
			
				JSONObject jObject = new JSONObject(resultMsgString);
				String resString = jObject.getString("responseData");
				JSONObject subJsonObject = new JSONObject(resString);
				
				String resultMsgSys = jObject.getString("status");
				Boolean isSuccBoolean = jObject.getBoolean("success");
				if(!isSuccBoolean)
				{
					Log.e( " .......... ", "也许是 IMS 系统错误");
					return null;
				}
				
				String mettingIDString = subJsonObject.getString("meetingid");
				String resultMsg = subJsonObject.getString("msg");
				String isSucess = subJsonObject.getString("success");
				
				map.put("meetingid", mettingIDString);
				map.put("msg", resultMsg);
				map.put("success", isSucess);
				map.put("uuid", uuidString);
				
				Log.d("231423412341234", mettingIDString + "  " + resultMsg + "   " + isSucess + " SYS: "  + resultMsgSys + "   " );
				
		}
		catch (Exception e) 
		{
			// TODO: handle exception
			map = null;
		}
		
		return map;
	}
	
	private Map<String, String> parseResultStringHttpRes(String sourceString)
	{
		Map<String, String> map = new HashMap<String, String>();
		
		try
		{	
				JSONObject jObject = new JSONObject(sourceString);
				String resString = jObject.getString("responseData");
				JSONObject subJsonObject = new JSONObject(resString);
				
				String resultMsgSys = jObject.getString("status");
				Boolean isSuccBoolean = jObject.getBoolean("success");
				if(!isSuccBoolean)
				{
					Log.e( " .......... ", "也许是 IMS 系统错误");
					return null;
				}
				
				String mettingIDString = subJsonObject.getString("meetingid");
				String resultMsg = subJsonObject.getString("msg");
				String isSucess = subJsonObject.getString("success");
				
				map.put("meetingid", mettingIDString);
				map.put("msg", resultMsg);
				map.put("success", isSucess);
				
				Log.d("231423412341234", mettingIDString + "  " + resultMsg + "   " + isSucess + " SYS: "  + resultMsgSys + "   " );
				
		}
		catch (Exception e) 
		{
			// TODO: handle exception
			map = null;
		}
		
		return map;
	}

	

	private Handler handler2 = new Handler(){
		public void handleMessage(android.os.Message msg)
		{

			switch (msg.what) {
			case NO_SELF_PHONENUM:
				
				Utils.showCustomToast(getApplicationContext(), "请先设置本机号码");
				Intent intent = new Intent();
				intent.setClass(SmsGroupSend.this, SettingActivity.class);
				startActivity(intent);
				
				break;
				
			case CONNECT_FAILD:
				Utils.showCustomToast(getApplicationContext(), "连接失败，请检查网络");
				//Toast.makeText(getApplicationContext(), "连接失败，请检查网络", 1).show();
				break;
			
			case CONNECT_SUCCESS:
				
				String resultString = ((String)msg.obj);
				
				Map<String, String> resultMap = parseResultStringHttpRes(resultString);
				//	Map<String, String> resultMap = parseResultString(resultString);
					
					if(resultMap == null)
					{
						Utils.showCustomToast(getApplicationContext(), "请联系 程序员");
						//Toast.makeText(getApplicationContext(), "请联系 程序员", 1).show();
					}
					
					if(resultMap.get("success").equals("true"))
					{
						
						Utils.showCustomToast(getApplicationContext(), "呼叫成功，请稍后");
						//Toast.makeText(getApplicationContext(), "呼叫成功，请稍后", 1).show();
						
						GlobalData.getInstance().setMetingDetails(resultMap);
						GlobalData.getInstance().setSelectedContacts(mSelectedContacts);
					}
					else 
					{
						Utils.showCustomToast(getApplicationContext(), "呼叫失败，如需要，请联系客服");
						//Toast.makeText(getApplicationContext(), "呼叫失败，请检查网络", 1).show();
					}

				break;
				
			case NO_SELECTEDCONTACTS:
				
				Utils.showCustomToast(getApplicationContext(), "请选择至少一位联系人");
				
				break;
				
			case CONTACTS_OVERFLOW:
				
				Utils.showCustomToast(getApplicationContext(), "目前移动公司那边对多只支持10人会议，不关本程序的事");
				
				break;

			default:
				break;
			}
			//Log.d("12341235234562345", ((String)msg.obj));
			
			
			
		}
		
	};

}

