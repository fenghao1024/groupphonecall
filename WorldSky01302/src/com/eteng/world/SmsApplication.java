/**
 * Name : SmsApplication.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.eteng.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;

import com.hp.hpl.sparta.xpath.ThisNodeTest;

import com.wljsms.database.MySqliteHelper;
import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ContactInfo;
import com.wljsms.info.GroupInfo;
import com.wljsms.info.VersionInfo;
import com.wljsms.util.GroupInfoComparator;
import com.wljsms.util.Utils;

/**
 * 
 * com.eteng.world.SendAdapter
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-30 下午2:12:22 Description : 应用公共类
 */
public class SmsApplication extends Application {

	/**
	 * 通知创建日期
	 */
	private String groupDate;
	/**
	 * 数据库对象
	 */
	private MySqliteHelper database;
	/**
	 * 所有联系人
	 */
	private ArrayList<ContactInfo> arrayList = new ArrayList<ContactInfo>();
	/**
	 * 最近联系人信息
	 */
	private List<ContactInfo> mRecentContactList = new ArrayList<ContactInfo>();
	/**
	 * 群组列表
	 */
	private List<GroupInfo> mGroups = new ArrayList<GroupInfo>();
	/**
	 * 加载联系人完成标志
	 */
	private boolean mLoadContacterComplete = true;
	/**
	 * 加载最近联系人完成
	 */
	private boolean mLoadRecentContactComplete = true;
	/**
	 * 后台发送短信完成标志
	 */
	private boolean mSendingSmsComplete = true;
	private Handler handler;
	private ContentResolver cr;
	private String content = "";
	/**
	 * 后台服务短信发送完成时间
	 */
	private long mSmsSendedTime = 0L;
	/**
	 * 短信内容草稿
	 */
	public String mDraft;
	/**
	 * 发短信页面的联系人信息,即选择的的联系人
	 */
	private List<ContactInfo> mContactsList = new ArrayList<ContactInfo>();
	/**
	 * 直接输入的号码信息，即通讯录里没有，用户直接输入的收件人号码
	 */
	private List<ContactInfo> mNewContactsList = new ArrayList<ContactInfo>();
	/**
	 * 主应用版本信息（服务端）
	 */
	public static VersionInfo versionInfo = new VersionInfo();
	/**
	 * 新建群组时自动起名的索引值，如群组3，群组4等，值为当前群组数+1
	 */
	public int index;
	/**
	 * 选择的联系人数
	 */
	public int selectedContactsCount;
	/**
	 * imsi号
	 */
	private String imsi;
	/**
	 * 手机型号
	 */
	private String model = android.os.Build.MODEL;
	/**
	 * android系统版本号
	 */
	private String release = android.os.Build.VERSION.RELEASE;
	/**
	 * 急短信版本号,name
	 */
	private String wlj_version;
	/**
	 * 黑名单表
	 */
	/**
	 * 是否弹出窗口
	 */
	private boolean isDialog = true;
	/**
	 * 通知是否显示
	 */
	private boolean isShowLook = false;
	/**
	 * 是否直接关闭
	 */
	private boolean isClose = false;

	private boolean isPost = false;
	private List<String> mBlackList = new ArrayList<String>();

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		database = new MySqliteHelper(this);
		cr = getContentResolver();
		wlj_version = Utils.getCurrentVersion(this);
	}

	public MySqliteHelper getDatabase() {
		return database;
	}

	/**
	 * 
	 * 获取最近联系人信息
	 * 
	 */
	private void getRecentContacter() {

		try {
			Cursor mCursor = database.allNearData();
			while (mCursor.moveToNext()) {
				String phone = mCursor.getString(mCursor
						.getColumnIndex(MySqliteHelper.Phone));

				String user = mCursor.getString(mCursor
						.getColumnIndex(MySqliteHelper.User));

				String context = mCursor.getString(mCursor
						.getColumnIndex(MySqliteHelper.Context));
				ContactInfo info = new ContactInfo();
				info.setContext(context);
				info.setName(user);
				info.setPhone(phone);
				info.setPingyin(Utils.converterToFirstSpell(user));
				mRecentContactList.add(info);
			}
			mCursor.close();

		} catch (Exception e) {
		}
	}

	/**
	 * 加载最近联系人
	 */
	public void loadRecentContacter() {
		if (!mLoadRecentContactComplete)
			return;
		if (mRecentContactList.size() > 0)
			mRecentContactList.clear();
		mLoadRecentContactComplete = false;
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				getRecentContacter();
				mLoadRecentContactComplete = true;
				if (handler != null) {
					handler.sendMessage(handler.obtainMessage(2));
				}
			}

		}).start();
	}

	/**
	 * 加载联系人和群组
	 * 
	 */
	public void loadContacter() {
		if (!mLoadContacterComplete)
			return;
		mLoadContacterComplete = false;
		if (arrayList.size() > 0)
			arrayList.clear();
		if (mGroups.size() > 0) {
			for (GroupInfo info : mGroups)
				info.getChilds().clear();
			mGroups.clear();
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Cursor cursor = null;
				cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
						new String[] { ContactsContract.Contacts._ID,
								ContactsContract.Contacts.DISPLAY_NAME,
								ContactsContract.Contacts.HAS_PHONE_NUMBER,
								"sort_key" }, null, null,
						"sort_key COLLATE LOCALIZED asc");
				int contactId = -1;
				String name = "";
				String sort_key = "";
				String has_phoneNumber = "0";
				ContactInfo info;
				while (cursor.moveToNext()) {
					has_phoneNumber = cursor.getString(cursor
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
					if (has_phoneNumber.equals("1")) {
						name = cursor.getString(cursor
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						if (name != null) {
							contactId = cursor.getInt(cursor
									.getColumnIndex(ContactsContract.Contacts._ID));

							sort_key = cursor.getString(cursor
									.getColumnIndex("sort_key"));
							has_phoneNumber = cursor.getString(cursor
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
							info = new ContactInfo();
							info.setName(name);
							info.setContactId(contactId);
							info.setSortKey(sort_key);
							// 设置拼音首字母并转换为大写字母。目前系统的拼音为大写，之所以再次转换是为了兼容以后系统版本或其他厂家把拼音转换为小写。
							info.setAlpha(getAlpha(sort_key, name)
									.toUpperCase());
							arrayList.add(info);
						}
					}
				}
				cursor.close();
				getFirstPhone();
				loadGroup();
				DebugFlags.EtengLog("群组数为 ：" + mGroups.size());
				index = mGroups.size() + 1;
				mLoadContacterComplete = true;
				if (handler != null) {
					handler.sendMessage(handler.obtainMessage(1));
				}
			}

		}).start();
	}

	/**
	 * 群组按联系人数量降序排序
	 */
	public void sortGroupDesc() {
		GroupInfoComparator comparator = new GroupInfoComparator();
		Collections.sort(mGroups, comparator);

	}

	/**
	 * 取得联系人的电话号码
	 * 
	 */
	public void getFirstPhone() {
		Cursor phones = cr
				.query(ContactsContract.Data.CONTENT_URI,
						new String[] { ContactsContract.Data.CONTACT_ID,
								ContactsContract.CommonDataKinds.Phone.NUMBER },
						Data.MIMETYPE
								+ "='"
								+ ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
								+ "'", null, null);

		int contactId = -1;
		String phoneNumber = "";

		while (phones.moveToNext()) {
			contactId = phones.getInt(phones
					.getColumnIndex(ContactsContract.Data.CONTACT_ID));

			phoneNumber = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

			for (ContactInfo temp : arrayList) {

				if (temp.getPhone() == null) {

					if (temp.getContactId() == contactId) {

						phoneNumber = phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						phoneNumber = Utils.replaceBlank(phoneNumber);
						if (phoneNumber.length() > 11)
							phoneNumber = phoneNumber.substring(
									phoneNumber.length() - 11,
									phoneNumber.length());
						temp.setPhone(phoneNumber);
						break;
					}
				}
			}
		}

		phones.close();
	}

	/**
	 * 
	 * 加载群组和群组里的联系人
	 * 
	 */
	private void loadGroup() {
		String[] projection = new String[] { Groups._ID, Groups.TITLE,
				Groups.SYSTEM_ID };
		// 查询未被删除的记录
		String selection = Groups.DELETED + "=?";
		String[] selectionArgs = new String[] { String.valueOf(0) };
		Cursor c = getContentResolver().query(Groups.CONTENT_URI, projection,
				selection, selectionArgs, null);
		if (c == null)
			return;
		while (c.moveToNext()) {
			String name = c.getString(c.getColumnIndex(Groups.TITLE));
			String systemId = c.getString(c.getColumnIndex(Groups.SYSTEM_ID));
			if (name == null || name.equals(""))
				continue;
			GroupInfo group = new GroupInfo();
			group.setId(c.getInt(c.getColumnIndex(Groups._ID)));
			group.setName(name);
			group.setSystemId(systemId);
			if (loadContactsInGroup(group))
				mGroups.add(group);
			sortGroupDesc();
		}
	}

	/**
	 * 
	 * 加载一个群组里的所有联系人
	 * 
	 * @param 传入的群组的信息
	 * @return 成功返回true
	 */
	private boolean loadContactsInGroup(GroupInfo info) {
		String groupSelection = Data.MIMETYPE + " = ?" + " AND "
				+ GroupMembership.GROUP_ROW_ID + " = ?";
		String[] groupSelectionArgs = new String[] {
				GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(info.getId()) };
		Cursor groupCursor = getContentResolver().query(Data.CONTENT_URI, null,
				groupSelection, groupSelectionArgs, null);
		if (groupCursor == null)
			return false;
		int count = groupCursor.getCount();
		// 系统内置群组且不包含联系人的，不显示
		if (count == 0 && info.getSystemId() != null)
			return false;

		StringBuilder sb = new StringBuilder();
		long rawContactId;
		long contactId;
		for (int i = 0; i < count; i++) {
			groupCursor.moveToPosition(i);
			rawContactId = groupCursor.getLong(groupCursor
					.getColumnIndex(GroupMembership.RAW_CONTACT_ID));
			contactId = queryForContactId(getContentResolver(), rawContactId);
			sb.append(contactId);
			if (i != count - 1) {
				sb.append(',');
			}
		}
		groupCursor.close();
		List<ContactInfo> temp = queryMemberOfGroup(sb.toString(), info.getId());
		if (temp != null)
			info.setChilds(temp);
		return true;
	}

	/**
	 * 查询组里联系人信息
	 */
	private List<ContactInfo> queryMemberOfGroup(String id, long groupId) {

		List<ContactInfo> temp = null;
		ContactInfo info;
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY }; // 查询的列

		Cursor cursor = getContentResolver().query(
				uri,
				projection,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " in ("
						+ id + " )", null, "sort_key COLLATE LOCALIZED asc");
		if (cursor != null && cursor.getCount() > 0) {
			temp = new ArrayList<ContactInfo>();
			while (cursor.moveToNext()) {
				String name = cursor.getString(1);
				String number = cursor.getString(2);
				String sortKey = cursor.getString(3);
				int contactId = cursor.getInt(4);

				info = new ContactInfo();
				info.setName(name);
				if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
					number = number.substring(3);
				}
				info.setPhone(Utils.replaceBlank(number));
				info.setSortKey(sortKey);
				info.setContactId(contactId);
				info.setGroupId(groupId);
				temp.add(info);
			}
		}
		cursor.close();
		return temp;
	}

	/**
	 * 查询RawContacts中_id等于rawContactId的记录的contact_id字段的值
	 */
	public long queryForContactId(ContentResolver cr, long rawContactId) {
		Cursor contactIdCursor = null;
		long contactId = -1;
		try {
			contactIdCursor = cr.query(RawContacts.CONTENT_URI,
					new String[] { RawContacts.CONTACT_ID }, RawContacts._ID
							+ "=" + rawContactId, null, null);
			if (contactIdCursor != null && contactIdCursor.moveToFirst()) {
				contactId = contactIdCursor.getLong(0);
			}
		} finally {
			if (contactIdCursor != null) {
				contactIdCursor.close();
			}
		}
		return contactId;
	}

	/**
	 * 返回姓名的拼音首字母字符串
	 * 
	 * @param sort_key
	 *            : 姓名和拼音字符串
	 * @param name
	 *            ： 姓名
	 * @return 拼音首字母字符串或空
	 */
	private String getAlpha(String sort_key, String name) {
		// 联系人拼音首字母组成的字符串
		String alpha = "";
		// 姓名和拼音字符串，去除空格
		String right = Utils.replaceBlank(sort_key);
		// 去除姓名中的空格
		name = Utils.replaceBlank(name);
		int length = name.length();
		for (int k = 0; k < length; k++) {
			// 取得姓名中的一个字符
			String str = name.substring(k, k + 1);
			// 利用联系人姓名分割字符串，第一个字母即为拼音的首字母
			int pos = right.indexOf(str);
			if (pos == -1)
				continue;
			String array = right.substring(0, pos);
			// 分割失败查询下一个姓名字符
			if (array == null || array.length() == 0)
				continue;
			alpha += array.substring(0, 1);

			// 到达字符串末尾直接退出
			if (pos == right.length() - 1) {
				break;
			}
			// 改变right的值为str之后的字符串
			right = right.substring(pos + 1, right.length());
		}

		return alpha;
	}

	public ArrayList<ContactInfo> getArrayList() {
		return arrayList;
	}

	public String getGroupDate() {
		return groupDate;
	}

	public void setGroupDate(String groupDate) {
		this.groupDate = groupDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setArrayList(ArrayList<ContactInfo> arrayList) {
		this.arrayList = arrayList;
	}

	public boolean ismLoadContacterComplete() {
		return mLoadContacterComplete;
	}

	public void setmLoadContacterComplete(boolean mLoadContacterComplete) {
		this.mLoadContacterComplete = mLoadContacterComplete;
	}

	public boolean ismSendingSmsComplete() {
		return mSendingSmsComplete;
	}

	public void setmSendingSmsComplete(boolean mSendingSmsComplete) {
		this.mSendingSmsComplete = mSendingSmsComplete;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public List<ContactInfo> getmContactsList() {
		return mContactsList;
	}

	public void setmContactsList(List<ContactInfo> mContactsList) {
		this.mContactsList = mContactsList;
	}

	public boolean addContactInfo(ContactInfo info) {
		if (!checkIfContain(info)) {
			mContactsList.add(info);
			return true;
		}
		return false;
	}

	public void removeContactInfo(ContactInfo info) {
		mContactsList.remove(info);
	}

	public List<ContactInfo> getmNewContactsList() {
		return mNewContactsList;
	}

	public void setmNewContactsList(List<ContactInfo> mNewContactsList) {
		this.mNewContactsList = mNewContactsList;
	}

	/**
	 * 添加一个号码到新联系人列表里，创建一个新联系人，此联系人只有号码
	 * 
	 * @param phone
	 *            : 电话号码
	 * @return True:添加成功，False：添加失败
	 * 
	 */
	public boolean addNewContactInfo(String phone) {
		for (ContactInfo info : mNewContactsList) {
			if (info.getPhone().equals(phone)) {
				// 号码已存在，直接返回
				return false;
			}
		}
		ContactInfo info = new ContactInfo();
		info.setPhone(phone);
		info.setName(phone);
		info.setmNewContact("2");
		mNewContactsList.add(info);
		return true;

	}

	/**
	 * 从新联系人列表里删除一项
	 * 
	 * @param phone
	 *            : 电话号码
	 */
	public void removeNewContactInfo(String phone) {

		for (ContactInfo info : mNewContactsList) {
			if (info.getPhone().equals(phone)) {
				// 找到一项，直接删除，因为新联系人列表里的号码是惟一的，无需再查找，可以直接返回
				mNewContactsList.remove(info);
				return;
			}
		}
	}

	private boolean checkIfContain(ContactInfo info) {
		for (ContactInfo ci : mContactsList)
			if (ci.getPhone().equals(info.getPhone())
					&& ci.getName().equals(info.getName()))
				return true;
		return false;
	}

	public List<GroupInfo> getmGroups() {
		return mGroups;
	}

	public void setmGroups(List<GroupInfo> mGroups) {
		this.mGroups = mGroups;
	}

	public boolean ismLoadRecentContactComplete() {
		return mLoadRecentContactComplete;
	}

	public void setmLoadRecentContactComplete(boolean mLoadRecentContactComplete) {
		this.mLoadRecentContactComplete = mLoadRecentContactComplete;
	}

	public List<ContactInfo> getmRecentContactList() {
		return mRecentContactList;
	}

	public void setmRecentContactList(List<ContactInfo> mRecentContactList) {
		this.mRecentContactList = mRecentContactList;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getModel() {
		return model;
	}

	public String getRelease() {
		return release;
	}

	public String getWlj_version() {
		return wlj_version;
	}

	/**
	 * 加载黑名单数据
	 */
	public void loadBlackList() {
		// database.insertDialogRulse();
		// 取得数据库的游标
		DebugFlags.EtengLog("开始加载黑名单中的数据");
		database.insertBlackList();
		Cursor cur = database.getBlackList();
		if (cur == null) {
			DebugFlags.EtengLog("加载黑名单失败");
			return;
		}

		DebugFlags.EtengLog("黑名单中的数据量为：" + cur.getCount());
		mBlackList.clear();
		// 循环数据库游标获取数据
		while (cur.moveToNext()) {
			mBlackList.add(cur.getString(cur
					.getColumnIndex(MySqliteHelper.LIST_CONTENT)));
		}
		cur.close();
		// 打印出黑名单中的数据
		StringBuffer s = new StringBuffer();
		for (String str : mBlackList)
			s.append(str + ",");
		DebugFlags.EtengLog("黑名单中的数据为：" + s.toString());
		s = null;
	}

	/**
	 * @return the mBlackList
	 */
	public List<String> getmBlackList() {
		return mBlackList;
	}

	public boolean isPost() {
		return isPost;
	}

	public void setPost(boolean isPost) {
		this.isPost = isPost;
	}

	public boolean isClose() {
		return isClose;
	}

	public void setClose(boolean isClose) {
		this.isClose = isClose;
	}

	public boolean isShowLook() {
		return isShowLook;
	}

	public void setShowLook(boolean isShowLook) {
		this.isShowLook = isShowLook;
	}

	public boolean isDialog() {
		return isDialog;
	}

	public void setDialog(boolean isDialog) {
		this.isDialog = isDialog;
	}

	/**
	 * @return the mSmsSendedTime
	 */
	public long getmSmsSendedTime() {
		return mSmsSendedTime;
	}

	/**
	 * @param mSmsSendedTime the mSmsSendedTime to set
	 */
	public void setmSmsSendedTime(long mSmsSendedTime) {
		this.mSmsSendedTime = mSmsSendedTime;
	}
	
}
