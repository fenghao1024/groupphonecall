package com.eteng.world;

import com.wljsms.util.MyGenstureListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;

/**
 * 
 * com.eteng.world.NewGroupActiv
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-20 下午2:22:41 
 *         Description : 操作教程  Modified :
 */
public class OtherActivity extends Activity {

	private MyGenstureListener gestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.other);
		gestureDetector = new MyGenstureListener(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		boolean b = gestureDetector.onSingleTapUp(event);
		if (b) {
			Intent intent = new Intent();
			intent.setClass(this, Look.class);
			startActivity(intent);
		}
		return false;
	}

}
