package com.wljsms.util;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.SeekBar;
import android.widget.Toast;

public class MyGenstureListener extends SimpleOnGestureListener {
	private Context mContext = null;
	private SeekBar seek = null;

	public MyGenstureListener(Context context) {

		mContext = context;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		Toast.makeText(mContext, "DOWN " + e.getAction(), Toast.LENGTH_SHORT)
				.show();
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		Toast.makeText(mContext, "SHOW " + e.getAction(), Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (e.getAction() == 0) {

			return true;
		}
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		Toast.makeText(mContext, "SCROLL " + e2.getAction(), Toast.LENGTH_SHORT)
				.show();
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		Toast.makeText(mContext, "LONG " + e.getAction(), Toast.LENGTH_SHORT)
				.show();
		seek.setVisibility(View.VISIBLE);

	}
}
