/**
 * Name : LookAdapter.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.eteng.world.R;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.util.Configure;
import com.wljsms.util.Utils;

/**
 * 
 * com.eteng.world.LookAdapter
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-24 下午2:12:22 Description :
 *         通知主体适配器类，完成通知数据桥接工作，数据通讯，读取本地数据库。
 */
public class LookAdapter extends BaseAdapter {
	/**
	 * 上下文对象
	 */
	private Context context;
	/**
	 * 通知消息数据集合
	 */
	private ArrayList<HashMap<String, Object>> listData;
	/**
	 * HashMap对象
	 */
	private HashMap<String, Object> map;
	/**
	 * 进入显示底部的图片宽度
	 */
	private int imageWidth;
	// 亿次表示草稿，发送中，发送完成图片,进度条,完成进度条
	private Drawable draft_img, sending_img, complete_img;
	
	// 通知发送完成时的颜色
	private int color_complete;
	// 分别表示进度条的底部和进度的图片布局参数
	private LinearLayout.LayoutParams params;

	public LookAdapter(Context context,
			ArrayList<HashMap<String, Object>> listData, Resources res) {
		this.context = context; // 环境对象
		this.listData = listData; // 通知消息数据集合
		// 计算通知进度图片所占的宽度
		imageWidth = Configure.screenWidth
				- res.getDimensionPixelSize(R.dimen.look_item_layout_status_image_width)
				- res.getDimensionPixelSize(R.dimen.look_item_layout_margin_left)
				- 2 * res.getDimensionPixelSize(R.dimen.look_item_layout_margin_left1)
				- res.getDimensionPixelSize(R.dimen.look_item_layout_summary_tv_widht);
		
		params = new LinearLayout.LayoutParams(imageWidth,
				LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		draft_img = res.getDrawable(R.drawable.notification_task_draft_img);
		sending_img = res.getDrawable(R.drawable.notification_task_sending_img);
		complete_img = res
				.getDrawable(R.drawable.notification_task_complete_img);
		color_complete = res.getColor(R.color.complete);
	}

	@Override
	public int getCount() {
		return listData.size();

	}

	@Override
	public Object getItem(int position) {
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 获取时间 小时:分;秒 HH:mm:ss
	 */
	public String getTimeShort(String dateStr) {
		dateStr = dateStr.substring(11, 16);
		return dateStr;
	}

	// 设置发送时间以及发送人列表信息
	private void setExplain(ViewHolder holder) {
		String userNameStr = (String) map.get("userNameStr"); // 获取发送人字符串
		
		String smsSendDate = (String) map.get("CreateDate");
		if (smsSendDate.substring(0, ConstantsInfo.DATELONG).equals(
				Utils.formatDate(System.currentTimeMillis()).substring(0,
						ConstantsInfo.DATELONG))) // 如果发送时间是今天

			holder.userNames.setText(getTimeShort((String) map
					.get("CreateDate")) + " " + userNameStr); // 设置今天的时间和发送人格式

		else { // 如果不是今天

			holder.userNames.setText(getTime((String) map.get("CreateDate"))
					+ " " + userNameStr); // 设置为往天的时间和发送人格式
		}
	}

	// 初始化布局控件对象
	private void initUI(ViewHolder holder, View view) {
		holder.reply = (TextView) view.findViewById(R.id.reply_summary);
		holder.smsContent = (TextView) view.findViewById(R.id.tv_sm);
		holder.userNames = (TextView) view.findViewById(R.id.tv_UserNames);
		holder.progress = (ProgressBar) view.findViewById(R.id.task_progress_ing);
		holder.complete = (ProgressBar) view.findViewById(R.id.task_progress_complete);
		holder.state = (ImageView) view.findViewById(R.id.img_StateEx);
		holder.complete.setLayoutParams(params);
		holder.progress.setLayoutParams(params);
		view.setTag(holder);
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		map = listData.get(position);
		if (convertView == null) { // 视图是否为空,如果为空就创建
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.lookitem, null);
			initUI(holder, convertView); // 初始化布局对象
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		setExplain(holder); // 设置发送时间以及发送人列表信息
	
		holder.smsContent.setText((String) map.get("ContextMax"));

		int replyCount = Integer.parseInt(map.get("ReplyCount").toString()); // 根据不同手机分辨率计算像素的宽
		int Count = Integer.parseInt(map.get("Count").toString());// 通知发送总数
		if (replyCount == Count) {
			// 发送数等于回复数
			holder.complete.setVisibility(View.VISIBLE);
			holder.progress.setVisibility(View.GONE);
			holder.state.setBackgroundDrawable(complete_img);
			holder.reply.setTextColor(color_complete);
			holder.reply.setText(replyCount + "/" + Count);
		} else {
			// 发送数不等于回复数
			holder.complete.setVisibility(View.GONE);
			holder.progress.setVisibility(View.VISIBLE);
			holder.progress.setProgress((int)((replyCount * 1.0 / Count) * 100));
			holder.reply.setText(Html.fromHtml("<font color=\"#e14812\">"
					+ replyCount + "</font><font color=\"#999999\">/" + Count
					+ "</font>"));
			if (map.get("SendStateMax").toString().equals(ConstantsInfo.DRAFT)) {
				// 草稿箱,设置草稿图片
				holder.state.setBackgroundDrawable(draft_img);
			} else {
				// 不是草稿,设置发送中图片
				holder.state.setBackgroundDrawable(sending_img);
			}
		}
		return convertView;
	}

	/**
	 * 项目数据定义
	 */
	public final class ViewHolder {
		TextView reply; // 回复数据
		TextView smsContent; // 短信内容
		TextView userNames; // 发送用户字符串
		ProgressBar progress; // 回复进度
		ProgressBar complete; // 总进度
		ImageView state; // 图形显示状态
	}

	// 格式化时间
	public String getTime(String dateStr) {
		dateStr = dateStr.substring(5, 16).replace("-", "月");
		dateStr = dateStr.replace(" ", "日");
		return dateStr;
	}

	// 通知数据改变通知适配器刷新
	public void changeData(ArrayList<HashMap<String, Object>> temp) {
		this.listData = temp;
		LookAdapter.this.notifyDataSetChanged();
	}
}