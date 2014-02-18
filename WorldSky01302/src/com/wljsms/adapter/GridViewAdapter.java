package com.wljsms.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eteng.world.R;
import com.wljsms.info.ContactInfo;

/**
 * 
 * com.eteng.world.EditGroupActiv
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-24 下午2:23:04 
 *         Description : 最近联系人网格适配器 。
 */
public class GridViewAdapter extends BaseAdapter {

	static class ViewHolder {
		TextView text; //联系人姓名文本
	}
	/**
	 * 联系人数据集合
	 */
	private List<ContactInfo> data;  
	/**
	 * layout解析对象
	 */
	private LayoutInflater mInflater;

	public GridViewAdapter(Context context, List<ContactInfo> data) {
		this.data = data;
		mInflater = LayoutInflater.from(context); //用来获得布局文件对象的
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

		ViewHolder vh;
		ContactInfo info = data.get(position);
		if (convertView == null) {
			vh = new ViewHolder(); 
			convertView = mInflater.inflate(R.layout.grid_item, null); 
			vh.text = (TextView) convertView  
					.findViewById(R.id.grid_item_textview);
			convertView.setTag(vh); 
		} else {
			vh = (ViewHolder) convertView.getTag(); //取得标识
		}
		if (info.getStatus() == 1) {  //设置是标准图片背景资源还是按下背景资源。
			vh.text.setBackgroundResource(R.drawable.grid_item_pressed_bg);//按下
		} else if (info.getStatus() == 2) {
			vh.text.setBackgroundResource(R.drawable.grid_item_normal_bg);//标准
		}
		vh.text.setText(info.getName()); //设置文本内容
		return convertView;
	}

}
