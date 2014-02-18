/**
 * Name : SendAdapter.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.adapter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eteng.world.R;

/**
 * 
 * com.eteng.world.SendAdapter
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-25 下午2:12:22 Description :
 *         完成通知数据桥接工作，数据通讯，读取本地数据库，分析呼叫状态。
 */
public class SendAdapter extends BaseAdapter {
	/**
	 * 上下文对象
	 */
	private Context context;
	/**
	 * 数据集
	 */
	private ArrayList<HashMap<String, Object>> listData;
	/**
	 * HashMap对象
	 */
	private HashMap<String, Object> map;

	/**
	 * 构造函数
	 */
	public SendAdapter(Context context,
			ArrayList<HashMap<String, Object>> listData) {
		this.context = context;
		this.listData = listData;
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

	/**
	 * 根据电话号码获取联系人头像
	 * 
	 * @param strPhoneNumber
	 *            ：电话号码
	 * @return 联系人图像或null
	 */
	public Bitmap getUserImage(String strPhoneNumber) {
		ContentResolver resolver = context.getContentResolver();
		Uri uriNumber2Contacts = Uri.parse("content://com.android.contacts/"
				+ "data/phones/filter/" + strPhoneNumber); // 获取联系人数据库地址
		Cursor cursorCantacts = resolver.query(uriNumber2Contacts, null, null,
				null, null); // 执行查询得到数据游标
		if (cursorCantacts.getCount() > 0) { // 若游标不为0则说明有头像,游标指向第一条记录
			cursorCantacts.moveToFirst(); // 移动到第一条记录
			Long contactID = cursorCantacts.getLong(cursorCantacts
					.getColumnIndex("contact_id")); // 获取联系人ID
			Uri uri = ContentUris.withAppendedId(
					ContactsContract.Contacts.CONTENT_URI, contactID); // 获取头像地址
			InputStream input = ContactsContract.Contacts
					.openContactPhotoInputStream(resolver, uri); // 查询并获取头像输入流
			Bitmap btContactImage = BitmapFactory.decodeStream(input); // 解码生成BMP格式
			return btContactImage;
		} else {
			return null;
		}
	}

	/**
	 * 初始化布局对象
	 */
	private void setInitView(ViewHolder holder, View view) {

		holder.username = (TextView) view
				.findViewById(R.id.receiver_info_list_item_username); // 获取布局对象
		holder.ReplyContent = (TextView) view
				.findViewById(R.id.receiver_info_list_item_msg); // 获取回复内容对象

		holder.img_Head = (ImageView) view
				.findViewById(R.id.receiver_info_list_item_userimage); // 获取头像对象

		holder.status_image = (ImageView) view
				.findViewById(R.id.receiver_info_list_item_status_image_1); // 获取状态对象

		view.setTag(holder);
	}

	/**
	 * 设置图形状态
	 */
	private void setImage(ViewHolder holder) {
		Bitmap bitmap = null;
		try {
			bitmap = getUserImage((String) map.get("Phone")); // 获取指定电话的头像
		} catch (Exception e) {
			// TODO: handle exception
			bitmap = null;
		}
		if (bitmap == null) // 如果位图不为空
			holder.img_Head.setBackgroundResource(R.drawable.user_image); // 设置头像
		else
			holder.img_Head.setImageBitmap(bitmap);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ViewHolder holder = null;
		map = listData.get(position);
		if (convertView == null) {
			holder = new ViewHolder(); // 获取视图句柄
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 获取扩展对象
			convertView = inflater.inflate(R.layout.receiver_info_list_item,
					null); // 获取布局文件
			setInitView(holder, convertView); // 初始化布局对象
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 设置联系人姓名
		holder.username.setText((String) map.get("userName"));

		if (map.get("Reply") != null && map.get("Reply").toString().equals("1")) {
			// 已回复了短信，设置短信内容
			holder.ReplyContent.setText("回复:" + map.get("ReplyContent").toString());
			// 已回复了短信，设置回复图片
			holder.status_image
					.setBackgroundResource(R.drawable.receiver_info_list_item_replied_img);
		} else {
			// 根据新需求，设置为未回复
			holder.ReplyContent.setText("未回复");
			// 未回复，设置未回复图片
			holder.status_image
					.setBackgroundResource(R.drawable.receiver_info_list_item_no_reply_img);
		}
		//设置联系人图片
		setImage(holder); 
		return convertView;
	}

	public final class ViewHolder {
		public TextView username; // 联系人姓名
		public TextView ReplyContent; // 回复内容
		public ImageView img_Head; // 头像
		public ImageView status_image; // 状态图形

	}
}