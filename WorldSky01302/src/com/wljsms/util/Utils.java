/**
 * Name : Utils.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.util;

import java.io.File;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONStringer;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.eteng.world.R;
import com.wljsms.database.MySqliteHelper;
import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.info.ErrorLogInfo;
import com.wljsms.webservice.GetPhone;
import com.wljsms.webservice.PostErrorLogData;
import com.wljsms.webservice.PostSmsData;

/**
 * com.eteng.world.util.Utils
 * 
 * @author Eteng <br/>
 *         Create at 2012-12-24 下午3:58:46 Description : 工具类 Modified :
 */
public class Utils {

	public static String DOWN_DIR;
	                             
	/**
	 * 计算字符串的md5值
	 * 
	 * @param s
	 *            : 待计算md5值的字符串
	 * @return md5值
	 * 
	 */
	public static String EncodedByMD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * 
	 * 汉字转换为拼音，因为单词不变
	 * 
	 * @param 待转换的汉字或英文单词
	 * @return 转换后的汉字拼音或英文单词
	 * 
	 */
	public static String converterToFirstSpell(String chinese) {
		String pinyinName = "";
		char[] nameChar = chinese.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < nameChar.length; i++) {
			if (nameChar[i] > 128) {
				try {
					pinyinName += PinyinHelper.toHanyuPinyinStringArray(
							nameChar[i], defaultFormat)[0].charAt(0);
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				pinyinName += nameChar[i];
			}
		}
		return pinyinName;
	}

	/**
	 * 从整型的时间值得到字符串日期
	 * 
	 * @param time
	 *            ： 长整型时间值
	 * 
	 */
	public static String formatDate(long time) {
		try {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			date.setTime(time);
			return format.format(date);
		} catch (Exception e) {

		}
		return "";

	}

	/**
	 * 从整型的时间值得到分钟,只取最后一位，比如：54，返回4
	 * 
	 * @param time
	 *            ： 长整型时间值
	 * 
	 */
	public static int getMinuteFromTime(long time) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date date = new Date();
			date.setTime(time);
			String str = format.format(date);
			return Integer.parseInt(str.substring(str.length() - 1,
					str.length()));
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 从整型的时间值得到秒数,，比如：54:04，返回4
	 * 
	 * @param time
	 *            ： 长整型时间值
	 * 
	 */
	public static int getSecFromTime(long time) {
		try {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			date.setTime(time);
			String str = format.format(date);
			return Integer.parseInt(str.substring(str.length() - 2,
					str.length()));
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 把时间转换为整数，如2012-01-17为20120107
	 */
	public static int formatDateForDay(long time) {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(time);
		String str = format1.format(date);
		String[] str1 = str.split("-");
		String tmp = "";
		for (int i = 0; i < str1.length; i++)
			tmp = tmp + str1[i];
		return Integer.parseInt(tmp);
	}

	/**
	 * 
	 * 去除字符串中的空格、回车、换行符、制表符
	 * 
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	/**
	 * 检测存储卡，设置更新apk下载路径
	 * 
	 * @return True：设置下载路径成功，False ： 设置失败
	 */
	public static boolean checkExternalStorage() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			DOWN_DIR = Environment.getExternalStorageDirectory()
					+ "/.wlj_down/";
			return true;
		} else if (new File("/mnt/flash").exists()) {
			DOWN_DIR = "/mnt/flash/.wlj_down/";
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 弹出自定义Toast
	 * 
	 * @param context
	 *            ：上下文，msg：提示信息
	 */
	public static void showCustomToast(Context context, String msg) {
		View toastRoot = LayoutInflater.from(context).inflate(
				R.layout.toast_layout, null);
		TextView message = (TextView) toastRoot.findViewById(R.id.message);
		message.setText(msg);

		Toast toastStart = new Toast(context);
		toastStart.setDuration(Toast.LENGTH_SHORT);
		toastStart.setView(toastRoot);
		toastStart.show();
	}

	/**
	 * 获取0-99999之间的随机数，以5位字符串形式返回，随机数不足5位的，前面加0
	 * 
	 */
	public static String get5Random() {
		String newString = null;

		// 得到0.0到1.0之间的数字,并扩大100000倍
		double doubleP = Math.random() * 100000;

		// 如果数据等于100000,则减少1
		if (doubleP >= 100000) {
			doubleP = 99999;
		}

		// 然后把这个数字转化为不包含小数点的整数
		int tempString = (int) Math.ceil(doubleP);

		// 转化为字符串
		newString = "" + tempString;

		// 把得到的数增加为固定长度,为5位
		while (newString.length() < 5) {
			newString = "0" + newString;
		}

		return newString;
	}

	/**
	 * 
	 * 检测是否有网络链接，有：true，无：false
	 */
	public static boolean isNetworkConnected(Context context) {
		
		
		if (context != null) {  
			          ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
			                .getSystemService(Context.CONNECTIVITY_SERVICE);  
			        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
			         if (mNetworkInfo != null) {  
			             return mNetworkInfo.isAvailable();  
			         }  
			      }  
			     return false;  
		
//		ConnectivityManager cm = (ConnectivityManager) context
//				.getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo[] infos = cm.getAllNetworkInfo();
//		for (int i = 0; i < infos.length; i++) {
//			if (infos[i].getDetailedState() == NetworkInfo.State.CONNECTED) {
//				return true;
//			}
//		}
//		return false;
	}

	/**
	 * 判断字符串是否是纯数字
	 * 
	 * @param str
	 *            :字符串
	 * @return true:纯数字，false:非纯数字
	 * 
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		return isNum.matches();
	}

	/**
	 * 判断字符串是否是纯字母a-zA-Z
	 * 
	 * @param str
	 *            :字符串
	 * @return true:纯字母，false:非纯字母
	 * 
	 */
	public static boolean isLetter(String str) {
		Pattern pattern = Pattern.compile("[a-zA-Z]+");
		Matcher m = pattern.matcher(str);
		return m.matches();
	}

	/**
	 * 获取急短信当前版本号
	 * 
	 */
	public static String getCurrentVersion(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取当前版本号
	 */
	public static int getCurrentCode(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 判断是否是有效的手机号码
	 * 
	 * @param phone
	 *            : 字符串
	 * @return True ： 是有效的手机号码，False ： 不是有效的手机号码
	 */
	public static boolean isPhoneNumber(String phone) {
		if (phone == null || phone.equals(""))
			return false;

		Pattern p = Pattern.compile("^(1[0-9])\\d{9}$");
		Matcher m = p.matcher(phone);
		return m.matches();
	}

	/**
	 * 插入一条查询/登录失败错误日志，插入前先判断记录是否达到上限，达到上限后删除第一条
	 * 
	 * @param database
	 *            :数据库对象
	 * @param imsi
	 *            ：imsi号
	 * @param error_code
	 *            ：错误原因编码
	 * @param time
	 *            ：日志生成时间
	 */
	public static void insertQueryErrorLog(MySqliteHelper database,
			String imsi, String error_code, String time) {
		try {
			Cursor cur = database.getErrorLogFromTypeAndTime(
					ConstantsInfo.QUERY_ERROR_LOG, time);
			if (cur != null && cur.getCount() != 0) {
				// 相同时间的记录已经存在，直接返回，不插入数据库。
				DebugFlags.EtengLog("相同时间的查询记录已经存在，直接返回，不插入数据库。");
				cur.close();
				return;
			}
			if (cur != null)
				cur.close();

			cur = database.getErrorLogs(ConstantsInfo.QUERY_ERROR_LOG);
			if (cur == null) {
				// 如果获取不到数据库游标，则无法对数据库操作，直接返回
				return;
			}
			if (cur.getCount() == ConstantsInfo.QUERY_ERROR_LOG_MAX) {
				// 查询失败错误日志记录已达到上限，删除第一条记录
				if (cur.moveToFirst()) {
					database.deleteOneErrorLog(cur.getInt(cur
							.getColumnIndexOrThrow(MySqliteHelper.ID)));
				}
			}
			database.insertErrorLog(imsi, error_code,
					ConstantsInfo.QUERY_ERROR_LOG, "", time);
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入一条注册失败错误日志，插入前先判断记录是否达到上限，达到上限后删除第一条
	 * 
	 * @param database
	 *            :数据库对象
	 * @param imsi
	 *            ：imsi号
	 * @param error_code
	 *            ：错误原因编码
	 * @param time
	 *            ：日志生成时间
	 * @param extra_content
	 *            ：额外数据
	 */
	public static void insertRegisterErrorLog(MySqliteHelper database,
			String imsi, String error_code, String extra_content, String time) {
		try {
			Cursor cur = database.getErrorLogFromTypeAndTime(
					ConstantsInfo.REGISTER_ERROR_LOG, time);
			if (cur != null && cur.getCount() != 0) {
				// 相同时间的记录已经存在，直接返回，不插入数据库。
				DebugFlags.EtengLog("相同时间的注册记录已经存在，直接返回，不插入数据库。");
				cur.close();
				return;
			}
			if (cur != null)
				cur.close();

			cur = database.getErrorLogs(ConstantsInfo.REGISTER_ERROR_LOG);
			if (cur == null) {
				// 如果获取不到数据库游标，则无法对数据库操作，直接返回
				return;
			}
			if (cur.getCount() == ConstantsInfo.REGISTER_ERROR_LOG_MAX) {
				// 查询失败错误日志记录已达到上限，删除第一条记录
				if (cur.moveToFirst()) {
					database.deleteOneErrorLog(cur.getInt(cur
							.getColumnIndexOrThrow(MySqliteHelper.ID)));
				}
			}
			database.insertErrorLog(imsi, error_code,
					ConstantsInfo.REGISTER_ERROR_LOG, extra_content, time);
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入一条心跳失败错误日志，插入前先判断记录是否达到上限，达到上限后删除第一条
	 * 
	 * @param database
	 *            :数据库对象
	 * @param imsi
	 *            ：imsi号
	 * @param error_code
	 *            ：错误原因编码
	 * @param time
	 *            ：日志生成时间
	 */
	public static void insertHeartbeatErrorLog(MySqliteHelper database,
			String imsi, String error_code, String time) {
		try {
			Cursor cur = database.getErrorLogFromTypeAndTime(
					ConstantsInfo.HEARTBEAT_ERROR_LOG, time);
			if (cur != null && cur.getCount() != 0) {
				// 相同时间的记录已经存在，直接返回，不插入数据库。
				DebugFlags.EtengLog("相同时间的心跳记录已经存在，直接返回，不插入数据库。");
				cur.close();
				return;
			}
			if (cur != null)
				cur.close();

			cur = database.getErrorLogs(ConstantsInfo.HEARTBEAT_ERROR_LOG);
			if (cur == null) {
				// 如果获取不到数据库游标，则无法对数据库操作，直接返回
				return;
			}
			if (cur.getCount() == ConstantsInfo.HEARTBEAT_ERROR_LOG_MAX) {
				// 查询失败错误日志记录已达到上限，删除第一条记录
				if (cur.moveToFirst()) {
					database.deleteOneErrorLog(cur.getInt(cur
							.getColumnIndexOrThrow(MySqliteHelper.ID)));
				}
			}
			database.insertErrorLog(imsi, error_code,
					ConstantsInfo.HEARTBEAT_ERROR_LOG, "", time);
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入一条短信发送失败错误日志，插入前先判断记录是否达到上限，达到上限后删除第一条
	 * 
	 * @param database
	 *            :数据库对象
	 * @param imsi
	 *            ：imsi号
	 * @param extra_content
	 *            ：发送短信失败的收件人号码列表
	 * @param time
	 *            ：日志生成时间
	 */
	public static void insertSmsSendFailedErrorLog(MySqliteHelper database,
			String imsi, String error_code, String extra_content, String time) {
		try {
			Cursor cur = database.getErrorLogFromTypeAndTime(
					ConstantsInfo.SMS_SEND_FAILED_ERROR_LOG, time);
			if (cur != null && cur.getCount() != 0) {
				// 相同时间的记录已经存在，直接返回，不插入数据库。
				DebugFlags.EtengLog("相同时间的短信记录已经存在，直接返回，不插入数据库。");
				cur.close();
				return;
			}

			if (cur != null)
				cur.close();

			cur = database
					.getErrorLogs(ConstantsInfo.SMS_SEND_FAILED_ERROR_LOG);
			if (cur == null) {
				// 如果获取不到数据库游标，则无法对数据库操作，直接返回
				return;
			}

			if (cur.getCount() == ConstantsInfo.SMS_SEND_FAILED_ERROR_LOG_MAX) {
				// 查询失败错误日志记录已达到上限，删除第一条记录
				if (cur.moveToFirst()) {
					database.deleteOneErrorLog(cur.getInt(cur
							.getColumnIndexOrThrow(MySqliteHelper.ID)));
				}
			}
			database.insertErrorLog(imsi, error_code,
					ConstantsInfo.SMS_SEND_FAILED_ERROR_LOG, extra_content,
					time);
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入一条提交呼叫列表失败错误日志，插入前先判断记录是否达到上限，达到上限后删除第一条
	 * 
	 * @param database
	 *            :数据库对象
	 * @param imsi
	 *            ：imsi号
	 * @param error_code
	 *            ：错误原因编码
	 * @param extra_content
	 *            ：发送短信失败的收件人号码列表
	 * @param time
	 *            ：日志生成时间
	 */
	public static void insertCommitCallFailedErrorLog(MySqliteHelper database,
			String imsi, String error_code, String extra_content, String time) {
		try {

			Cursor cur = database.getErrorLogFromTypeAndTime(
					ConstantsInfo.COMMIT_CALL_FAILED_ERROR_LOG, time);
			if (cur != null && cur.getCount() != 0) {
				// 相同时间的记录已经存在，直接返回，不插入数据库。
				DebugFlags.EtengLog("相同时间的提交记录已经存在，直接返回，不插入数据库。");
				cur.close();
				return;
			}
			if (cur != null)
				cur.close();

			cur = database
					.getErrorLogs(ConstantsInfo.COMMIT_CALL_FAILED_ERROR_LOG);
			if (cur == null) {
				// 如果获取不到数据库游标，则无法对数据库操作，直接返回
				return;
			}
			if (cur.getCount() == ConstantsInfo.COMMIT_CALL_FAILED_ERROR_LOG_MAX) {
				// 查询失败错误日志记录已达到上限，删除第一条记录
				if (cur.moveToFirst()) {
					database.deleteOneErrorLog(cur.getInt(cur
							.getColumnIndexOrThrow(MySqliteHelper.ID)));
				}
			}
			database.insertErrorLog(imsi, error_code,
					ConstantsInfo.COMMIT_CALL_FAILED_ERROR_LOG, extra_content,
					time);
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 检测本地是否缓存有imsi号对应的电话号码以及缓存的imsi号与程序取到的imsi号是否一致。
	 * 
	 * @param imsi
	 *            : 程序取到的imsi号
	 * @param database
	 *            : 数据库对象
	 * @return True：匹配成功，False：匹配失败
	 */
	public static boolean getCachePhoneImsi(String imsi, MySqliteHelper database) {
		Cursor cur = database.getClientInfos();
		if (cur == null || cur.getCount() == 0) {
			if (cur != null)
				cur.close();
			DebugFlags.EtengLog("本地没有缓存imsi号和手机好的对应关系");
			return false;
		}
		String cachedImsi, cachedPhone;
		try {
			cur.moveToNext();
			cachedImsi = cur.getString(cur
					.getColumnIndexOrThrow(MySqliteHelper.CLINET_IMSI));
			cachedPhone = cur.getString(cur
					.getColumnIndexOrThrow(MySqliteHelper.CLINET_PHONE));
		} catch (Exception e) {
			cachedImsi = null;
			cachedPhone = null;
		}
		cur.close();
		DebugFlags.EtengLog("本地缓存的imsi号和手机号为：imsi : " + cachedImsi
				+ ",phone : " + cachedPhone  + "，程序取到的imsi号为：" + imsi);
		if (cachedImsi == null || cachedPhone == null) {
			return false;
		}

		if (cachedImsi.equals(imsi) && !cachedPhone.equals(""))
			return true;
		else
			return false;
	}

	/**
	 * 检测imsi号是否注册。先检测本地缓存库，没有的话访问接口，成功得到imsi号注册信息的话，把imsi号缓存到本地
	  @param imsi
	 *            : 程序取到的imsi号
	 * @param database
	 *            : 数据库对象
	 * @return True：用户已注册，False：用户未注册
	 */
	public static boolean checkRegister(String imsi, MySqliteHelper database,Context context) {
		if (getCachePhoneImsi(imsi, database))
			return true;
		DebugFlags.EtengLog("Utils.java  本地没有缓存imsi号与手机号的对应关系，从服务器端取imsi号对应的号码，判断用户是否注册");
		// 构造服务通讯类
		if(!isNetworkConnected(context)){
			DebugFlags.EtengLog("Utils.java  从服务器判断imsi号是否注册时没有网络，认为没有注册");
			return false;
		}
	
		GetPhone getPhoneNumFromImsi = new GetPhone();
		String phoneNum = null;
		try {
			// 调用服务接口方法，从服务器获取imsi号对应的电话号码。
			phoneNum = getPhoneNumFromImsi.init(imsi, null);
		} catch (Exception e) {
			phoneNum = "";
		}
		if (phoneNum == null || phoneNum.equals("")) {
			// 查询失败，无法从服务器获取返回结果，认为用户没有注册，返回false
			DebugFlags.EtengLog("Utils.java  查询失败，无法从服务器获取返回结果，认为用户没有注册，返回false");
			return false;
		} else {
			// 如果获取成功，本地缓存imsi号对应的电话号码
			DebugFlags.EtengLog("Utils.java   查询成功，服务器获取返回结果为：" + phoneNum);
			if (!phoneNum.equals("1") && !phoneNum.equals("2")) {
				DebugFlags.EtengLog("Utils.java   用户已注册，缓存imsi号与手机号的对应关系");
				database.insertOrUpdateClientImsiPhoneInfo(imsi, phoneNum);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 把本地通知任务缓存数据库中没成功提交的记录提交到服务器
	 * 
	 * @param context
	 *            : Context对象
	 */
	public static void postSmsTaskDataFromLocal(Context context,MySqliteHelper database) {
		if (database == null) {
			DebugFlags.EtengLog("提交通知任务数据，初始化数据库对象出错，数据库对象为空，直接退出");
			return;
		}
		try {
			Cursor cur = database.getTasks();
			if (cur == null) {
				// 读取数据库失败，直接返回
				return;
			}
			DebugFlags.EtengLog("本地通知任务数据库中的记录数为：" + cur.getCount());
			if(cur.getCount() == 0){
				cur.close();
				return;
			}
			// 以下得到列的索引
			int imsiColumn = cur.getColumnIndex(MySqliteHelper.IMSI);
			int taskIdColumn = cur.getColumnIndex(MySqliteHelper.TASK_ID);
			int phonesColumn = cur.getColumnIndex(MySqliteHelper.TASK_PHONES);
			int modelColumn = cur.getColumnIndex(MySqliteHelper.TASK_MODEL);
			int versionColumn = cur.getColumnIndex(MySqliteHelper.TASK_VERSION);
			int lenColumn = cur.getColumnIndex(MySqliteHelper.TASK_LEN);
			int timeColumn = cur.getColumnIndex(MySqliteHelper.TASK_TIME);
			int sourceColumn = cur.getColumnIndex(MySqliteHelper.TASK_SOURCE);
			// 遍历记录，先提交，然后再删除
			while (cur.moveToNext()) {
				// 因为提交数据到服务器可能持续比较长的时间，在此期间若是断网的话，立刻结束提交
				if (!Utils.isNetworkConnected(context)) {
					// 断网,结束循环
					break;
				}
				// 以下获取记录中的字段
				String imsi = cur.getString(imsiColumn);
				String task_id = cur.getString(taskIdColumn);
				String phones = cur.getString(phonesColumn);
				String model = cur.getString(modelColumn);
				String version = cur.getString(versionColumn);
				String len = cur.getString(lenColumn);
				String time = cur.getString(timeColumn);
				String source = cur.getString(sourceColumn);
				
				DebugFlags.EtengLog("开始提交记录，IMSI号：" + imsi + ",id：" + task_id
							+ ",收件人列表 ： " + phones + ",机型：" + model + ",版本信息："
							+ version + ",通知内容长度 ： " + len);
				if (postTaskDataToServer(imsi, task_id, phones, model, version,
						len,time,source)) {
					// 成功提交，设置标志为成功
					database.updateTask(task_id, "1");
					
					DebugFlags.EtengLog("通知任务提交成功");
				} else {
					DebugFlags.EtengLog("通知任务提交失败");
				}
			}
			cur.close();
			// 删除成功提交的记录
			database.deleteTasks();
		} catch (Exception e) {

		}
	}

	/**
	 * 提交数据到服务器
	 * 
	 * @param imsi
	 *            : IMSI号
	 * @param task_id
	 *            : 任务ID
	 * @param phones
	 *            : 电话号码列表，逗号分割
	 * @param model
	 *            : 手机型号
	 * @param version
	 *            : 急短信和android版本号，逗号分割
	 * @param lenth
	 *            : 通知内容长度
	 * @return True : 成功提交，False：提交失败
	 */
	private static boolean postTaskDataToServer(String imsi, String task_id,
			String phones, String model, String version, String lenth,String date,String source) {
		PostSmsData postSmsData = new PostSmsData();
		String result = postSmsData.init(imsi, task_id, phones, model, version,
				Integer.parseInt(lenth),date,source, null);
		return (result == null || result.equals("1")) ? false : true;
	}

	/**
	 * 提交失败日志信息到服务器，批量提交
	 * 
	 * @param context
	 *            : Context对象
	 */
	public static void postErrorLogData(Context context,MySqliteHelper database) {
		if (database == null) {
			
			DebugFlags.EtengLog("提交注册数据，初始化数据库对象出错，数据库对象为空，直接退出");
			return;
		}
		List<ErrorLogInfo> list = new ArrayList<ErrorLogInfo>();
		ErrorLogInfo info = null;
		try {
			Cursor cur = database.getAllErrorLogs();
			if (cur == null) {
				// 读取数据库失败，直接返回
				return;
			}
			
			DebugFlags.EtengLog("本地错误日志数据库中的记录数为：" + cur.getCount());
			if (cur.getCount() == 0) {
				// 记录数为0，直接返回
				cur.close();
				return;
			}
			// 以下得到列的索引
			int idColumn = cur.getColumnIndexOrThrow(MySqliteHelper.ID);
			int imsiColumn = cur
					.getColumnIndexOrThrow(MySqliteHelper.ERROR_IMSI);
			int errorCodeColumn = cur
					.getColumnIndexOrThrow(MySqliteHelper.ERROR_CODE);
			int errorTypeColumn = cur
					.getColumnIndexOrThrow(MySqliteHelper.ERROR_TYPE);
			int extraContentColumn = cur
					.getColumnIndexOrThrow(MySqliteHelper.ERROR_EXTRA_CONTENT);
			int timeColumn = cur
					.getColumnIndexOrThrow(MySqliteHelper.ERROR_TIME);
			// 遍历记录,把记录报错到错误对象列表，生成JSON格式数据，批量提交
			while (cur.moveToNext()) {
				info = new ErrorLogInfo();
				info.setId(cur.getInt(idColumn));
				info.setImsi(cur.getString(imsiColumn));
				info.setError_code(cur.getString(errorCodeColumn));
				info.setError_type(cur.getString(errorTypeColumn));
				info.setExtra_content(cur.getString(extraContentColumn));
				info.setTime(cur.getString(timeColumn));
				list.add(info);
			}
			cur.close();

			DebugFlags.EtengLog("开始提交错误日志信息，先生成JSON格式数据");
			JSONStringer stringer = new JSONStringer();
			try {
				stringer.array();
				for (ErrorLogInfo eli : list) {
					DebugFlags.EtengLog("错误日志信息：id : " + eli.getId() + ",imsi："
										+ eli.getImsi() + ",错误原因编码："
										+ eli.getError_code() + ",错误类型："
										+ eli.getError_type() + ",额外信息："
										+ eli.getExtra_content() + "，生成时间:"
										+ eli.getTime());
					stringer.object().key("imsi").value(eli.getImsi())
							.key("errid").value(eli.getError_code())
							.key("typeid").value(eli.getError_type())
							.key("content").value(eli.getExtra_content())
							.key("clientdate").value(eli.getTime()).endObject();
				}
				stringer.endArray();
			} catch (Exception e) {
				DebugFlags.EtengLog("生成JSON格式数据出错，不提交");
				list.clear();
				stringer = null;
				return;
			}
			if (!Utils.isNetworkConnected(context)) {
				// 断网,结束
				DebugFlags.EtengLog("断网，不提交");
				list.clear();
				stringer = null;
				return;
			}
			DebugFlags.EtengLog("JSON格式数据为：" + stringer.toString());
			if (postErrorLogDataToServer(stringer.toString())) {
				// 成功提交
				
				DebugFlags.EtengLog("提交错误日志成功");
				// 删除成功提交的记录,以list中的id为条件，开启事物操作
				database.getWritableDatabase().beginTransaction();
				for(ErrorLogInfo eli : list){
					database.deleteOneErrorLog(eli.getId());
				}
				database.getWritableDatabase().setTransactionSuccessful();
				database.getWritableDatabase().endTransaction();
			} else {
				DebugFlags.EtengLog("提交错误日志失败");
			}
			list.clear();
			stringer = null;
		} catch (Exception e) {
			list.clear();
		}
	}

	/**
	 * 提交数据到服务器
	 * 
	 * @param content
	 *            : JSON格式的数据
	 * @return True : 成功提交，False：提交失败
	 */
	private static boolean postErrorLogDataToServer(String content) {
		PostErrorLogData postData = new PostErrorLogData();
		String result = postData.init(content, null);
		return (result == null || result.equals("1")) ? false : true;
	}
	
	
	// 还未做补零处理
	public static String UUID_Maker()
	{
		
	       long now = System.currentTimeMillis(); //微秒级当前时间
	        Calendar c = Calendar.getInstance();
	        c.setTimeInMillis(now);
		
		String uuid = String.valueOf(c.get(Calendar.YEAR)) + String.valueOf((c.get(Calendar.MONTH) + 1)) + String.valueOf(c.get(Calendar.DATE))
        + String.valueOf(c.get(Calendar.HOUR)) + String.valueOf(c.get(Calendar.MINUTE))
        + String.valueOf(c.get(Calendar.SECOND)) + String.valueOf(c.get(Calendar.MILLISECOND));
		
		return uuid;
	}
	
}
