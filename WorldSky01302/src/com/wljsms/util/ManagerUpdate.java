/**
 * Name : ManagerUpdate.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.eteng.world.R;
import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.info.VersionInfo;

/**
 * com.eteng.world.util.ManagerUpdate
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-12 下午1:36:35 Description : 更新管理类 Modified :
 */
public class ManagerUpdate {

	private Activity activity;
	private VersionInfo versionInfo;
	private int currentVersionCode;
	private ProgressDialog mProgress;
	private Dialog dialog;
	private int progress;
	private View dialogView;
	private Button dialog1, dialog2;
	private TextView updateMsg;

	public ManagerUpdate(Activity activity, VersionInfo versionInfo) {
		this.activity = activity;
		this.versionInfo = versionInfo;
	}

	public void init() {
		getCurrentVersion();
	}

	private void initDialogView() {
		dialogView = activity.getLayoutInflater().inflate(
				R.layout.upate_dialog_layout, null);
		dialog1 = (Button) dialogView.findViewById(R.id.update_dialog_1);
		dialog2 = (Button) dialogView.findViewById(R.id.update_dialog_2);
		updateMsg = (TextView) dialogView.findViewById(R.id.update_msg);
		dialog1.setOnClickListener(listener);
		dialog2.setOnClickListener(listener);

	}

	/**
	 * 检测是否需要更新
	 */
	public boolean checkIfUpdate() {
		int remoteVersion = Integer.parseInt(this.versionInfo.getVersionCode());
		DebugFlags.EtengLog("remoteVersion="
				+ this.versionInfo.getVersionCode() + ",apkVersionCode="
				+ this.currentVersionCode);

		if (remoteVersion > currentVersionCode) {
			return true;
		}
		return false;
	}

	/**
	 * 弹出更新对话框
	 */
	public void showUpdateDialog() {
		initDialogView();
		updateMsg.setText(Html.fromHtml(versionInfo.getMessage()));
		updateMsg.setText(Html.fromHtml("1.优化呼叫状态，准确获取语音呼叫情况<br/>2.发送短信后即开始语音提醒对方，通知及时不等待<br/>3.优化联系人选择方式，更快捷找到对方<br/>4.修复部分Bug，运行更稳定"));
//		 设置是否强制升级标志，1：强制升级，2：非强制升级
		DebugFlags.EtengLog("升级，弹出升级对话框：强制升级标志：" + versionInfo.getForceUpdate()
				+ ",版本号：" + versionInfo.getVersionName());
		showDialog(versionInfo.getForceUpdate() == null ? true : versionInfo
				.getForceUpdate().equals("2"));
	}

	private void showDialog(boolean cancelable) {
		cancelable = true;
		if (dialog == null) {
			dialog = new Dialog(activity, R.style.dialogs);
			dialog.setCancelable(cancelable);
			dialog.setContentView(dialogView);
		}
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				DebugFlags.EtengLog("取消升级，后退按钮");
				Intent i = new Intent(ConstantsInfo.CANCEL_UPDATE_BROADCAST);
				activity.sendBroadcast(i);
			}

		});
		if (!cancelable) {
			dialog2.setVisibility(View.GONE);
		}
		dialog.show();
	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (dialog != null) {
				dialog.dismiss();
				dialog = null;
			}
			if (v.getId() == R.id.update_dialog_1) {
				// 显示更新状态，进度条
				showWaitDialog();
				// 通过地址下载文件
				downloadTheFile();
			} else if (v.getId() == R.id.update_dialog_2) {
				DebugFlags.EtengLog("取消升级,取消按钮");
				Intent i = new Intent(ConstantsInfo.CANCEL_UPDATE_BROADCAST);
				activity.sendBroadcast(i);
			}
		}
	};

	/**
	 * 下载loading
	 */
	public void showWaitDialog() {
		mProgress = new ProgressDialog(this.activity);
		mProgress.setTitle("软件版本更新中");
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgress.show();

	}

	/**
	 * 获取当前版本号
	 */
	public void getCurrentVersion() {

		this.currentVersionCode = Utils.getCurrentCode(activity);

	}

	/**
	 * 新线程中下载文件
	 */
	public void downloadTheFile() {
		try {
			new Thread(new Runnable() {
				public void run() {
					try {
						downloadApk(ManagerUpdate.this.versionInfo.versionLink);
					} catch (Exception e) {
						e.printStackTrace();
						DebugFlags.EtengLog("下载异常，退出程序");
						if (mProgress != null) {
							mProgress.dismiss();
							mProgress = null;
						}
						Intent i = new Intent(
								ConstantsInfo.UPDATE_FAILED_BROADCAST);
						activity.sendBroadcast(i);
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下载文件方法
	 */
	private void downloadApk(final String strPath) throws Exception {
		Utils.checkExternalStorage();
		File dir = new File(Utils.DOWN_DIR);
		if (!dir.exists())
			dir.mkdir();
		File file = new File(Utils.DOWN_DIR + Utils.EncodedByMD5(strPath)
				+ ".apk");
		if (file.exists()) {
			// 安装文件已存在，没必要再下载
			openFile(file);
			if (mProgress != null)
				mProgress.dismiss();
			return;
		}
		// 下载之前先删除下载文件夹下的文件
		for (File tmp : dir.listFiles()) {
			if (tmp.exists())
				tmp.delete();
		}
		URL myURL = new URL(strPath);
		HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
		conn.setConnectTimeout(3000);
		conn.setReadTimeout(3000);

		conn.connect();
		int length = conn.getContentLength();
		InputStream is = conn.getInputStream();

		if (is == null) {
			throw new RuntimeException("stream is null");
		}
		final File down_file = new File(Utils.DOWN_DIR
				+ Utils.EncodedByMD5(strPath) + ".tmp");
		down_file.createNewFile();
		FileOutputStream fos = new FileOutputStream(down_file);

		int count = 0;
		byte buf[] = new byte[1024];

		do {
			int numread = is.read(buf);
			count += numread;
			progress = (int) (((float) count / length) * 100);
			// 更新进度
			activity.runOnUiThread(new Runnable() {

				public void run() {
					mProgress.setProgress(progress);
				}
			});
			if (numread <= 0) {
				// 下载完成通知安装
				activity.runOnUiThread(new Runnable() {

					public void run() {
						mProgress.cancel();
						mProgress.dismiss();
						File newFile = new File(Utils.DOWN_DIR
								+ Utils.EncodedByMD5(strPath) + ".apk");
						if (down_file.renameTo(newFile))
							openFile(newFile);
						else {
							Utils.showCustomToast(activity, "升级失败，请稍后重试!");
							activity.finish();
						}
					}
				});
				break;
			}
			fos.write(buf, 0, numread);
		} while (true);

		try {
			is.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 打开文件进行安装
	 */
	private void openFile(File f) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(f),
				"application/vnd.android.package-archive");
		// 安装
		activity.startActivity(intent);
		if (mProgress != null) {
			mProgress.dismiss();
			mProgress = null;
		}
		activity.finish();
	}

}
