/**
 * Name : MySqliteHelper.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.database;

import java.util.HashMap;

import com.wljsms.debug.DebugFlags;
import com.wljsms.info.ConstantsInfo;
import com.wljsms.util.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * com.eteng.database.MySqliteHelper
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-4-1 下午2:12:22 Description : 数据库基础框架
 */
public class MySqliteHelper extends SQLiteOpenHelper {
	/**
	 * 数据库名
	 */
	private static final String DATABASE_NAME = "SMSSendDB"; 
	/**
	 * 版本号
	 */
	private static final int DATABASEVERSION = 9; 
	/**
	 * 表名
	 */
	private static final String TABLE_NAME = "Send_Info"; 
	/**
	 * 数据库操作对象
	 */
	private SQLiteDatabase db; 
	/**
	 * 编号
	 */
	public static final String ID = "_id"; 
	/**
	 * 用户名
	 */
	public static final String User = "user"; 
	/**
	 * 电话
	 */
	public static final String Phone = "phone"; 
	/**
	 * 内容
	 */
	public static final String Context = "context"; 
	// 发送状态 0：呼叫成功，1：未接通或挂断，2：无法接通，3：成功提交到语音平台，4：提交到语音平台失败，5：发生故障，未能访问语音平台接口
	// 6：取消呼叫，7：号码非法，8：发送成功，9：草稿，10：发送失败，11：短信已收到，12：网络异常
	public static final String SendState = "sendState";
	/**
	 * 短信回复状态，0：未回复短信，1：已回复短信
	 */
	public static final String Reply = "reply"; 
	/**
	 * 发送时间
	 */
	public static final String CreateDate = "createDate"; 
	/**
	 *  应答数
	 */
	public static final String Count = "Count(Reply)"; 
	/**
	 * 分组应答数
	 */
	public static final String ContextMax = "Max(Context)"; 
	/**
	 * 回复内容
	 */
	public static final String ReplyContext = "replyContext"; 
	/**
	 * 呼叫时间
	 */
	public static final String CallOKDate = "callOKDate"; 
	/**
	 * 第几次呼叫成功
	 */
	public static final String CallOKOnce = "callOKOnce"; 
	/**
	 * 时间
	 */
	public static final String Time = "time"; 
	/**
	 * 任务ID
	 */
	public static final String TaskID = "task_id"; 
	/**
	 * 分割串
	 */
	public static final String Phones = "phones"; 
	/**
	 * 分组发送状态
	 */
	public static final String SendStateMax = "Max(SendState)"; 
	/**
	 * 是否通知显示过
	 */
	public static final String IsShow = "isShow"; 

	/**
	 * 通知任务记录表，即短信记录表
	 */
	public static final String TABLE_TASK = "sms_task";
	/**
	 * IMSI号
	 */
	public static final String IMSI = "imsi";
	/**
	 * 通知任务id
	 */
	public static final String TASK_ID = "task_id";
	/**
	 * 通知任务的收件人号码列表，以","分割
	 */
	public static final String TASK_PHONES = "phones";
	/**
	 * 手机型号
	 */
	public static final String TASK_MODEL = "model";
	/**
	 * 急短信版本号和android系统版本号，逗号分割
	 */
	public static final String TASK_VERSION = "version";
	/**
	 * 通知内容长度
	 */
	public static final String TASK_LEN = "length";
	/**
	 * 通知生成时间
	 */
	public static final String TASK_TIME = "time";
	/**
	 * 通知内容长度
	 */
	public static final String TASK_SOURCE = "source";
	/**
	 * 信息提交状态，1：成功提交，可以删除，2：尚未提交或提交失败
	 */
	public static final String TASK_STATUS = "status";
	/**
	 * 错误日志表，记录各种错误失败日志
	 */
	public static final String TABLE_ERROR_LOG = "error_log";
	/**
	 * IMSI号
	 */
	public static final String ERROR_IMSI = "imis";
	/**
	 * 失败原因编码，详见码表
	 */
	public static final String ERROR_CODE = "error_code";
	/**
	 * 错误日志类型，详见码表
	 */
	public static final String ERROR_TYPE = "error_type";
	/**
	 * 额外补充信息，即除了失败原因，还需缓存记录的其他信息，内容之间用分号分割
	 */
	public static final String ERROR_EXTRA_CONTENT = "extra_content";
	/**
	 * 错误信息生成时间，精确到毫秒
	 */
	public static final String ERROR_TIME = "error_time";
	/**
	 * 信息提交状态，1：成功提交，可以删除，2：尚未提交或提交失败。用于单条记录的提交。
	 */
	public static final String ERROR_STATUS = "status";
	/**
	 * 缓存客户端版本号，渠道号，imsi号，imis号对应的号码
	 */
	public static final String TABLE_CLINET_INFO = "client_info";
	/**
	 * 客户端渠道号
	 */
	public static final String CLINET_CHANNEL = "client_channel";
	/**
	 * 客户端版本号
	 */
	public static final String CLINET_CODE = "client_code";
	/**
	 * imsi号和手机号
	 */
	public static final String CLINET_IMSI = "imsi";
	public static final String CLINET_PHONE = "phone";

	/**
	 * 黑名单表
	 */
	private static final String TABLE_BLACK_LIST = "black_list";
	/**
	 * 黑名单编号，黑名单编号以4开头，白名单编号以1，2，3开头，分别代码事件，时间和要求
	 */
	public static final String LIST_SERIAL_NUMBER = "serial_number";
	/**
	 * 黑名单内容
	 */
	public static final String LIST_CONTENT = "content";
	/**
	 *  黑名单日期
	 */
	public static final String LIST_DATE = "date";
	/**
	 * 黑名单其他字段，目前没用，主要是为了以后扩展
	 */
	public static final String LIST_OTHER = "other";

	/**
	 * 匹配记录表
	 */
	private static final String TABLE_MATCHED = "matched";
	/**
	 * 编号，见ConstantsInfo中的编码说明
	 */
	public static final String MATCHED_SERIAL_NUMBER = "serial_number";
	/**
	 * 日期
	 */
	public static final String MATCHED_DATE = "date";
	/**
	 * 匹配情况
	 */
	public static final String MATCHED_CONTENT = "content";
	/**
	 * 对话框弹出状态，1：弹出提示对话框，2：未弹出提示对话框
	 */
	public static final String MATCHED_DIALOG_STATUS = "dialog_status";
	/**
	 * imsi号
	 */
	public static final String MATCHED_IMSI = "imsi";

	/**
	 * 对话框操作记录表
	 */
	private static final String TABLE_DIALOG_OP = "dialog_op";
	/**
	 * 编号，见ConstantsInfo中的编码说明
	 */
	public static final String DIALOG_SERIAL_NUMBER = "serial_number";
	/**
	 *  日期
	 */
	public static final String DIALOG_DATE = "date";
	/**
	 * 匹配情况
	 */
	public static final String DIALOG_CONTENT = "content";
	/**
	 * 用户操作对话框的状态，1：点击确定，2：点击取消，3：10秒超时后自动消失，4：屏幕上前一个对话框还没有消失
	 */
	public static final String DIALOG_OP = "op_statuc";
	/**
	 *  imsi号
	 */
	public static final String DIALOG_IMSI = "imsi";
	

	public MySqliteHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASEVERSION);
		db = this.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE " + TABLE_NAME + " (" + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + User + " TEXT,"
				+ Phone + " TEXT," + Context + " TEXT," + SendState + " TEXT,"
				+ Reply + " TEXT," + CreateDate + " TEXT," + ReplyContext
				+ " TEXT," + CallOKDate + " TEXT," + CallOKOnce + " TEXT,"
				+ Time + " TEXT," + TaskID + " TEXT," + IsShow + " TEXT)";
		// 创建通知任务记录表的sql语句
		String sql_task = "CREATE TABLE " + TABLE_TASK + " (" + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + IMSI + " TEXT,"
				+ TASK_ID + " TEXT," + TASK_PHONES + " TEXT," + TASK_MODEL
				+ " TEXT," + TASK_VERSION + " TEXT," + TASK_LEN + " TEXT,"
				+ TASK_TIME + " TEXT," + TASK_SOURCE + " TEXT," + TASK_STATUS
				+ " TEXT)";
		// 创建错误日志记录表的sql语句
		String sql_register = "CREATE TABLE " + TABLE_ERROR_LOG + " (" + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + ERROR_IMSI + " TEXT,"
				+ ERROR_CODE + " TEXT," + ERROR_TYPE + " TEXT,"
				+ ERROR_EXTRA_CONTENT + " TEXT," + ERROR_TIME + " TEXT,"
				+ ERROR_STATUS + " TEXT)";

		String sql_clinet = "CREATE TABLE " + TABLE_CLINET_INFO + " (" + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + CLINET_CHANNEL
				+ " TEXT," + CLINET_CODE + " INTEGER," + CLINET_IMSI + " TEXT,"
				+ CLINET_PHONE + " TEXT)";

		db.execSQL(sql);
		db.execSQL(sql_task);
		db.execSQL(sql_register);
		db.execSQL(sql_clinet);

		sql = "create index idx on " + TABLE_NAME + "(" + CreateDate + ")";
		db.execSQL(sql);

		// 创建黑名单表
		sql = "CREATE TABLE " + TABLE_BLACK_LIST + " (" + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + LIST_SERIAL_NUMBER
				+ " TEXT," + LIST_CONTENT + " TEXT," + LIST_DATE + " TEXT,"
				+ LIST_OTHER + " TEXT)";
		db.execSQL(sql);
		

		// 创建匹配记录表
		sql = "CREATE TABLE " + TABLE_MATCHED + " (" + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + MATCHED_SERIAL_NUMBER
				+ " TEXT," + MATCHED_DATE + " TEXT," + MATCHED_CONTENT
				+ " TEXT," + MATCHED_DIALOG_STATUS + " TEXT," + MATCHED_IMSI
				+ " TEXT)";
		db.execSQL(sql);
		// 创建提示对话框操作记录表
		sql = "CREATE TABLE " + TABLE_DIALOG_OP + " (" + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + DIALOG_SERIAL_NUMBER
				+ " TEXT," + DIALOG_DATE + " TEXT," + DIALOG_CONTENT + " TEXT,"
				+ DIALOG_OP + " TEXT," + DIALOG_IMSI + " TEXT)";
		db.execSQL(sql);
	}

	/**
	 * 根据发送时间删除通知
	 * 
	 * @param date
	 *            : 通知发送时间
	 */
	public void deleteNotify(String date) {
		db.delete(TABLE_NAME, MySqliteHelper.CreateDate + "=?",
				new String[] { date });
	}

	/**
	 * 删除通知
	 * 
	 */
	public void deleteNotifyForTaskID(String taskID) {
		db.delete(TABLE_NAME, MySqliteHelper.TaskID + "=?",
				new String[] { taskID });
	}

	/**
	 * 更新数据库
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		sql = "DROP TABLE IF EXISTS " + TABLE_CLINET_INFO;
		db.execSQL(sql);
		sql = "DROP TABLE IF EXISTS " + TABLE_TASK;
		db.execSQL(sql);
		sql = "DROP TABLE IF EXISTS " + TABLE_ERROR_LOG;
		db.execSQL(sql);
		sql = "DROP TABLE IF EXISTS " + TABLE_MATCHED;
		db.execSQL(sql);
		sql = "DROP TABLE IF EXISTS " + TABLE_DIALOG_OP;
		db.execSQL(sql);
		onCreate(db);
	}

	/**
	 * 将字符串转换成数组，将数组翻转，再将数组转换成字符串
	 */
	public String toReverse(String str) {
		char[] c = str.toCharArray();
		for (int start = 0, end = c.length - 1; start <= end; start++, end--) {
			// 交换
			translate(c, start, end);
		}
		return String.valueOf(c);// 字符数组转换成字符串
	}

	/**
	 * 交换数组中元素
	 */
	public static void translate(char[] c, int start, int end) {
		char temp = c[start];
		c[start] = c[end];
		c[end] = temp;
	}

	/**
	 * 数据库关闭
	 * 
	 */
	@Override
	public synchronized void close() {
		// TODO Auto-generated method stub
		db.close();
		super.close();
	}

	/**
	 * 获取最近联系人数据
	 */
	public Cursor allNearData() {

		String[] columns = new String[] { User, Phone, Context };
		String groupBy = Phone;
		return db.query(TABLE_NAME, columns, "Phone <> ?", new String[] { "" },
				groupBy, null, "CreateDate Desc");
	}

	/**
	 * 获取分组数据（该分组数据是所有的分组数据）
	 */
	public Cursor groupData() {

		String[] columns = new String[] { CreateDate, Count, ContextMax,
				SendStateMax, Time, TaskID };
		String groupBy = CreateDate;
		return db.query(TABLE_NAME, columns, null, null, groupBy, null,
				"CreateDate Desc");
	}

	/**
	 * 获取分组数据（该分组数据是点击通知清除按钮以后排除已经清除过的分组）
	 */
	public Cursor groupDataEx() {

		String[] columns = new String[] { CreateDate, Count, ContextMax,
				SendStateMax, Time, TaskID };
		String groupBy = CreateDate;
		return db.query(TABLE_NAME, columns, "isShow<>?", new String[] { "2" },
				groupBy, null, "CreateDate Desc");
	}

	/**
	 * 根据分组时间字符获取分组以后的应答数据记录总数
	 * @param CreateDate 短信发送时间
	 * @param tempHashMap 临时变量存放运算的结果
	 */
	private void mergeData(String CreateDate,
			HashMap<String, Object> tempHashMap) {
		tempHashMap.put("ReplyCount", "0");
		Cursor mCursorEx = getReply(CreateDate); // 计算回话和应答数的比例
		while (mCursorEx.moveToNext()) {
			try {
				tempHashMap.put("ReplyCount", mCursorEx.getString(mCursorEx
						.getColumnIndex(MySqliteHelper.Count)));

			} catch (Exception e) {
				mCursorEx.close(); // 关闭数据游标
				tempHashMap.put("ReplyCount", "0");

			}
		}
		mCursorEx.close();// 关闭数据游标

	}

	/**
	 * 更新清除通知以后的数据
	 */
	public int updateIsShowEx() {
		int i = 0;
		Cursor mCursor = groupDataEx();
		if (mCursor != null)
			if (mCursor.getCount() != 0) {
				while (mCursor.moveToNext()) {
					HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
					String Count = mCursor.getString(mCursor
							.getColumnIndex(MySqliteHelper.Count));
					String CreateDate = mCursor.getString(mCursor
							.getColumnIndex(MySqliteHelper.CreateDate));
					mergeData(CreateDate, tempHashMap);
					int iReplyCount = Integer.valueOf(tempHashMap.get(
							"ReplyCount").toString());
					double iCount = Integer.valueOf(Count);
					double mod = iReplyCount / iCount;
					if ((mod * 10 > 0)) {
						ContentValues cv = new ContentValues();
						cv.put("isShow", "2");
						String[] args = { CreateDate };
						i = i + db.update(TABLE_NAME, cv, "createDate=?", args);
					}
				}
			}
		mCursor.close();
		return i;
	}

	/**
	 * 按日期获得分组统计数据
	 * @param createDate 发送短信的时间字符
	 * @Return 按日期获得分组统计数据
	 */
	public Cursor getGroupData(String createDate) {

		String selection = "CreateDate=?";
		String[] selectionArgs = new String[] { createDate };
		Cursor cur = db.query(TABLE_NAME, null, selection, selectionArgs, null,
				null, null);
		return cur;
	}

	/**
	 * 插入通知发送信息
	 */
	public void insertData(String name, String number, String context,
			String sendState, String reply, String createDate,
			String replyContext, String callOKDate, String callOKOnce,
			String time, String taskID, String isShow) {
		ContentValues values = new ContentValues();
		values.put(MySqliteHelper.User, name);
		values.put(MySqliteHelper.Phone, number);
		values.put(MySqliteHelper.Context, context);
		values.put(MySqliteHelper.SendState, sendState);
		values.put(MySqliteHelper.Reply, reply);
		values.put(MySqliteHelper.CreateDate, createDate);
		values.put(MySqliteHelper.ReplyContext, replyContext);
		values.put(MySqliteHelper.CallOKDate, callOKDate);
		values.put(MySqliteHelper.CallOKOnce, callOKOnce);
		values.put(MySqliteHelper.Time, time);
		values.put(MySqliteHelper.TaskID, taskID);
		values.put(MySqliteHelper.IsShow, isShow);
		long row = db.insert(TABLE_NAME, null, values);
		DebugFlags.EtengLog("插入一条通知信息：" + row);
	}

	/**
	 * 更新回复通知数据
	 */
	public void updateSmsReceiverData(String replyContext, String phone) {

		phone = toReverse(phone);
		phone = phone.substring(0, 11);
		phone = toReverse(phone);
		ContentValues cv = new ContentValues();
		cv.put("Reply", "1");
		cv.put("ReplyContext", replyContext);
		String[] args = { String.valueOf(phone) };
		db.update(TABLE_NAME, cv, "Phone=? and SendState <>9", args);
	}

	/**
	 * 更新电话呼叫状态
	 */
	public void updateCallState(String phone, int state, String createDate,
			String once) {
		phone = toReverse(phone);
		phone = phone.substring(0, 11);
		phone = toReverse(phone);
		ContentValues cv = new ContentValues();
		cv.put("SendState", Integer.toString(state));
		if (!once.equals("0")) {
			cv.put("CallOKDate", Utils.formatDate(System.currentTimeMillis()));
			cv.put("CallOKOnce", once);
		}

		String[] args = { String.valueOf(phone), createDate };
		db.update(TABLE_NAME, cv, "Phone=? and CreateDate=? and SendState <>9",
				args);
	}

	/**
	 *  获取应答数据集合游标
	 *  @param StrDate 发送短信的时间字符
	 *  @Return 应答数据集合游标
	 */
	public Cursor getReply(String StrDate) {
		String[] columns = new String[] { Count, CallOKOnce };
		String selection = "CreateDate=? and Reply=?";
		String[] selectionArgs = new String[] { StrDate, "1" };
		Cursor cur = db.query(TABLE_NAME, columns, selection, selectionArgs,
				null, null, null);

		return cur;
	}

	/**
	 * 按时间获取通知信息列表游标
	 * @param StrDate 发送短信的时间字符
	 * @Return 通知信息列表游标
	 */
	public Cursor getReplyList(String StrDate) {
		String selection = "CreateDate=?";
		String[] selectionArgs = new String[] { StrDate };
		Cursor cur = db.query(TABLE_NAME, null, selection, selectionArgs, null,
				null, null);

		return cur;
	}

	
	/**
	 * 获取最近弹出消息的时间字符
	 * @param phone  电话号码
	 * @param createDate   发送短信的时间字符
	 * @return 最近弹出消息的时间字符
	 */
	private String getShowCreate(String phone, String createDate) {
		String selection = "phone=? and CreateDate=? and isShow=? and sendState<>?";
		String[] selectionArgs = new String[] { phone, createDate, "1", "9" };
		Cursor cur = db.query(TABLE_NAME, null, selection, selectionArgs, null,
				null, null);
		if (cur != null)
			if (cur.getCount() != 0) {
				cur.close();
				return createDate;

			}
		cur.close();
		return  ConstantsInfo.MIN_TIME;
	}

	/**
	 * 检查是否没有通知过，如果没有通知过就返回游标，有通过知返回null
	 * @param phone 电话号码
	 * @param createDate 短信发送的时间
	 * @Return 如果没有通知过返回的游标数据记录
	 */
	public Cursor getGroupByUserData(String phone, String createDate) {

		String selection = "phone=? and CreateDate=? and isShow=? and sendState<>?";
		String[] selectionArgs = new String[] { phone, createDate, "0", "9" };
		Cursor cur = db.query(TABLE_NAME, null, selection, selectionArgs, null,
				null, null);
		// 判断指定分组的，接收到的这个电话是否是没有弹出过的。
		if (cur != null) {
			if (cur.getCount() != 0) { // 没有弹出过
				cur.close();
				Cursor cursor = db.query(TABLE_NAME, null, "CreateDate='"
						+ createDate + "'", null, null, null, null);
				return cursor;
			} else { // 弹出过了
				String getShowCreate = getShowCreate(phone, createDate); // 找到当前弹出过最大的
				if (getCreateMaxEx().equals(getShowCreate)) // 和没有删除通知过的最大的日期比较
				{
					cur.close();
					Cursor cursor = db.query(TABLE_NAME, null, "CreateDate='"
							+ createDate + "'", null, null, null, null); // 如果相等返回数据
					return cursor;
				}
			}
		}
		cur.close();
		return null;
	}

	/**
	 * 更新通知，更新以后下次不再显示
	 * @param createDate 发送短信的时间 
	 * @param isShow 是否弹出过通知的标识
	 */
	public void updateIsShow(String createDate, String isShow) {
		String selection = "CreateDate=?";
		String[] selectionArgs = new String[] { createDate };
		ContentValues cv = new ContentValues();
		cv.put("isShow", isShow);
		int i = db.update(TABLE_NAME, cv, selection, selectionArgs);
		if (i != 0) {
			DebugFlags.EtengLog("IsShow 标识更新成功");
		}
	}

	/**
	 * 往通知任务表中插入一条数据
	 * 
	 * @param imsi
	 *            : imsi号
	 * @param task_id
	 *            : 任务id
	 * @param phones
	 *            : 任务收件人号码列表，逗号分割
	 * @param model
	 *            : 手机型号
	 * @param version
	 *            : 急短信版本号和android系统版本号，逗号分割的字符串
	 * @param length
	 *            : 通知内容长度
	 */
	public void insertTask(String imsi, String task_id, String phones,
			String model, String version, String length, String time,
			String source) {
		ContentValues values = new ContentValues();
		values.put(IMSI, imsi);
		values.put(TASK_ID, task_id);
		values.put(TASK_PHONES, phones);
		values.put(TASK_MODEL, model);
		values.put(TASK_VERSION, version);
		values.put(TASK_LEN, length);
		values.put(TASK_TIME, time);
		values.put(TASK_SOURCE, source);
		values.put(TASK_STATUS, "2");
		long row = db.insert(TABLE_TASK, null, values);
		DebugFlags.EtengLog("insertOneTask row=" + row);
	}

	/**
	 * 更新通知任务的状态
	 * 
	 * @param task_id
	 *            : 通知任务id
	 * @param status
	 *            : 要更新的通知状态
	 */
	public void updateTask(String task_id, String status) {
		ContentValues cv = new ContentValues();
		cv.put(TASK_STATUS, status);
		String[] args = { task_id };
		db.update(TABLE_TASK, cv, TASK_ID + "=?", args);
	}

	/**
	 * 查询通知任务表中的所有记录
	 */
	public Cursor getTasks() {
		// 查询记录，按task_id升序，这样第一条记录就是最先插入的记录
		Cursor cur = db.query(TABLE_TASK, null, TASK_STATUS + "=2", null, null,
				null, TASK_ID + " asc");
		return cur;
	}

	/**
	 * 从通知任务表中删除所有状态为1(成功提交)的记录
	 */
	public void deleteTasks() {
		int rowaffected = db.delete(TABLE_TASK, MySqliteHelper.TASK_STATUS
				+ "=1", null);
		DebugFlags.EtengLog("deleteTasks  rowaffected=" + rowaffected);
	}

	/**
	 * 从通知任务表中删除制定task_id的记录
	 * 
	 * @param task_id
	 *            : 记录id
	 */
	public void deleteTask(String task_id) {
		if (task_id == null)
			return;
		int rowaffected = db.delete(TABLE_TASK, MySqliteHelper.TASK_ID + "='"
				+ task_id + "'", null);
		DebugFlags.EtengLog("deleteTasks  rowaffected=" + rowaffected);
	}

	/**
	 * 往错误日志表中插入一条数据
	 * 
	 * @param imsi
	 *            : imsi号
	 * @param error_code
	 *            : 错误原因编码
	 * @param error_type
	 *            : 错误日志类别
	 * @param extra_content
	 *            : 额外数据
	 * @param time
	 *            : 错误日志生成时间
	 */
	public void insertErrorLog(String imsi, String error_code,
			String error_type, String extra_content, String time) {
		ContentValues values = new ContentValues();
		values.put(ERROR_IMSI, imsi);
		values.put(ERROR_CODE, error_code);
		values.put(ERROR_TYPE, error_type);
		values.put(ERROR_EXTRA_CONTENT, extra_content);
		values.put(ERROR_TIME, time);
		values.put(ERROR_STATUS, "2");
		long row = db.insert(TABLE_ERROR_LOG, null, values);
		DebugFlags.EtengLog("向错误日志表中插入一条记录 row=" + row);
	}

	/**
	 * 更新错误日志信息的提交状态
	 * 
	 * @param time
	 *            : 日志生成时间
	 * @param status
	 *            : 状态
	 */
	public void updateErrorStatus(String time, String status) {
		ContentValues cv = new ContentValues();
		cv.put(ERROR_STATUS, status);
		String[] args = { time };
		db.update(TABLE_ERROR_LOG, cv, ERROR_TIME + "=?", args);
	}

	/**
	 * 查询错误日志表中的特定错误日志类型的所有记录
	 */
	public Cursor getErrorLogs(String error_type) {

		// 查询记录
		Cursor cur = db.query(TABLE_ERROR_LOG, null, ERROR_STATUS + "='2' and "
				+ ERROR_TYPE + "='" + error_type + "'", null, null, null,
				ERROR_TIME + " asc");
		return cur;
	}

	/**
	 * 查询错误日志表中的所有错误日志类型的所有记录
	 */
	public Cursor getAllErrorLogs() {
		// 查询记录
		Cursor cur = db.query(TABLE_ERROR_LOG, null, ERROR_STATUS + "='2'",
				null, null, null, ERROR_TIME + " asc");
		return cur;
	}

	/**
	 * 查询错误日志表中特定错误类型特定生成时间的错误日志记录是否存在
	 */
	public Cursor getErrorLogFromTypeAndTime(String type, String time) {
		// 查询记录
		return db.query(TABLE_ERROR_LOG, null, ERROR_TYPE + "='" + type
				+ "' and " + ERROR_TIME + "='" + time + "'", null, null, null,
				null);
	}

	/**
	 * 从错误日志表中删除所有记录
	 */
	public void deleteErrorLogs() {
		int rowaffected = db.delete(TABLE_ERROR_LOG, null, null);
		DebugFlags.EtengLog("删除错误日志记录成功  rowaffected=" + rowaffected);
	}

	/**
	 * 从错误日志中删除一条记录
	 * 
	 * @param id
	 *            : 记录id
	 */
	public void deleteOneErrorLog(int id) {
		int rowaffected = db.delete(TABLE_ERROR_LOG, MySqliteHelper.ID + "="
				+ id, null);
		DebugFlags.EtengLog("delete one error log rowaffected=" + rowaffected);
	}

	/**
	 * 从客户端信息表中取得缓存的信息
	 */
	public Cursor getClientInfos() {
		// 查询记录
		Cursor cur = db.query(TABLE_CLINET_INFO, null, null, null, null, null,
				null);
		return cur;
	}

	/**
	 * 更新客户端信息的渠道号和版本号
	 * 
	 * @param channel
	 *            :渠道号
	 * @param code
	 *            :客户端版本号
	 */
	public void insertOrUpdateClientChannelCodeInfo(String channel, int code) {
		ContentValues cv = new ContentValues();
		cv.put(CLINET_CHANNEL, channel);
		cv.put(CLINET_CODE, code);
		Cursor cur = getClientInfos();
		if (cur == null || cur.getCount() == 0) {
			// 没有记录插入数据
			if (cur != null)
				cur.close();
			DebugFlags.EtengLog("插入版本号和渠道号对应关系到本地数据库中");
			db.insert(TABLE_CLINET_INFO, null, cv);
		} else {
			// 有记录更新数据
			DebugFlags.EtengLog("跟新版本号和渠道号对应关系到本地数据库中");

			db.update(TABLE_CLINET_INFO, cv, null, null);
		}
	}

	/**
	 * 更新客户端信息的imsi号和手机号码
	 * 
	 * @param channel
	 *            :渠道号
	 * @param code
	 *            :客户端版本号
	 */
	public void insertOrUpdateClientImsiPhoneInfo(String imsi, String phone) {
		ContentValues cv = new ContentValues();
		cv.put(CLINET_IMSI, imsi);
		cv.put(CLINET_PHONE, phone);
		Cursor cur = getClientInfos();
		if (cur == null || cur.getCount() == 0) {
			if (cur != null)
				cur.close();
			// 没有记录插入数据
			DebugFlags.EtengLog("插入imsi号和手机号对应关系到本地数据库中");
			db.insert(TABLE_CLINET_INFO, null, cv);
		} else {
			// 有记录更新数据
			DebugFlags.EtengLog("跟新imsi号和手机号对应关系到本地数据库中");
			db.update(TABLE_CLINET_INFO, cv, null, null);
		}

	}

	
	/**
	 * 获取黑名单本地数据记录数
	 * 
	 */
	public int blackListCount() {
		int i = 0;
		Cursor cur = db.query(TABLE_BLACK_LIST, null, null, null, null, null,
				null);
		i = cur.getCount();
		cur.close();
		return i;
	}

	/**
	 * 初始化黑名单本地数据
	 * 
	 */
	public void insertBlackList() {
		if (blackListCount() == 0) {
			ContentValues values1 = new ContentValues();
			values1.put(MySqliteHelper.LIST_SERIAL_NUMBER, "40001");
			values1.put(MySqliteHelper.LIST_CONTENT, "晚安");
			values1.put(MySqliteHelper.LIST_DATE, "");
			values1.put(MySqliteHelper.LIST_OTHER, "");
			db.insert(MySqliteHelper.TABLE_BLACK_LIST, null, values1);

			ContentValues values2 = new ContentValues();
			values2.put(MySqliteHelper.LIST_SERIAL_NUMBER, "40002");
			values2.put(MySqliteHelper.LIST_CONTENT, "祝好");
			values2.put(MySqliteHelper.LIST_DATE, "");
			values2.put(MySqliteHelper.LIST_OTHER, "");
			db.insert(MySqliteHelper.TABLE_BLACK_LIST, null, values2);

			ContentValues values3 = new ContentValues();
			values3.put(MySqliteHelper.LIST_SERIAL_NUMBER, "40003");
			values3.put(MySqliteHelper.LIST_CONTENT, "节日快乐");
			values3.put(MySqliteHelper.LIST_DATE, "");
			values3.put(MySqliteHelper.LIST_OTHER, "");
			db.insert(MySqliteHelper.TABLE_BLACK_LIST, null, values3);

			ContentValues values4 = new ContentValues();
			values4.put(MySqliteHelper.LIST_SERIAL_NUMBER, "40004");
			values4.put(MySqliteHelper.LIST_CONTENT, "生日快乐");
			values4.put(MySqliteHelper.LIST_DATE, "");
			values4.put(MySqliteHelper.LIST_OTHER, "");
			db.insert(MySqliteHelper.TABLE_BLACK_LIST, null, values4);

			ContentValues values5 = new ContentValues();
			values5.put(MySqliteHelper.LIST_SERIAL_NUMBER, "40005");
			values5.put(MySqliteHelper.LIST_CONTENT, "好梦");
			values5.put(MySqliteHelper.LIST_DATE, "");
			values5.put(MySqliteHelper.LIST_OTHER, "");
			db.insert(MySqliteHelper.TABLE_BLACK_LIST, null, values5);

			ContentValues values6 = new ContentValues();
			values6.put(MySqliteHelper.LIST_SERIAL_NUMBER, "40006");
			values6.put(MySqliteHelper.LIST_CONTENT, "好好休息");
			values6.put(MySqliteHelper.LIST_DATE, "");
			values6.put(MySqliteHelper.LIST_OTHER, "");
			db.insert(MySqliteHelper.TABLE_BLACK_LIST, null, values6);

			ContentValues values7 = new ContentValues();
			values7.put(MySqliteHelper.LIST_SERIAL_NUMBER, "40007");
			values7.put(MySqliteHelper.LIST_CONTENT, "晚安好梦");
			values7.put(MySqliteHelper.LIST_DATE, "");
			values7.put(MySqliteHelper.LIST_OTHER, "");
			db.insert(MySqliteHelper.TABLE_BLACK_LIST, null, values7);

			ContentValues values8 = new ContentValues();
			values8.put(MySqliteHelper.LIST_SERIAL_NUMBER, "40008");
			values8.put(MySqliteHelper.LIST_CONTENT, "周末愉快");
			values8.put(MySqliteHelper.LIST_DATE, "");
			values8.put(MySqliteHelper.LIST_OTHER, "");
			db.insert(MySqliteHelper.TABLE_BLACK_LIST, null, values8);
		}
	}

	/**
	 * 获取黑名单列表中的数据
	 */
	public Cursor getBlackList() {
		String[] columns = new String[] { MySqliteHelper.ID,
				MySqliteHelper.LIST_CONTENT };
		return db
				.query(TABLE_BLACK_LIST, columns, null, null, null, null, null);
	}

	/**
	 * 获取匹配表中的数据
	 */
	public Cursor getMatched() {
		return db.query(MySqliteHelper.TABLE_MATCHED, null, null, null, null,
				null, null);
	}

	/**
	 * 删除匹配操作日志
	 */
	public void deleteMatchedOpt() {
		@SuppressWarnings("unused")
		int rowaffected = db.delete(TABLE_MATCHED, null, null);
	}

	/**
	 * 获取操作信息记录总数
	 */
	public int getDialogOpInfoCount() {
		int i = 0;
		Cursor cur = db.query(TABLE_DIALOG_OP, null, null, null, null, null,
				"_id asc");
		i = cur.getCount();
		int id = 0;
		if (i > 0) {
			if (cur.moveToFirst()) {
				id = cur.getInt(cur.getColumnIndex("_id"));
				if (i > ConstantsInfo.ROW_MAX) {
					db.delete(MySqliteHelper.TABLE_DIALOG_OP, MySqliteHelper.ID
							+ "=?", new String[] { String.valueOf(id) });
				}
			}
		}
		cur.close();
		return i;
	}

	/**
	 * 获取对话框操作表中的数据
	 */
	public Cursor getDialogOp() {
		return db.query(MySqliteHelper.TABLE_DIALOG_OP, null, null, null, null,
				null, null);
	}

	/**
	 * 删除黑名单
	 */
	public void deleteBlackList() {
		db.delete(TABLE_BLACK_LIST, null, null);
	}
	

	/**
	 * 删除弹出操作日志
	 */
	public void deleteDialogOpt() {
		@SuppressWarnings("unused")
		int rowaffected = db.delete(TABLE_DIALOG_OP, null, null);
	}


	/**
	 * 插入一条匹配记录到对话框操作库中
	 * 
	 * @param serial
	 *            ：记录序号
	 * @param date
	 *            ：插入日期
	 * @param content
	 *            ：操作结果
	 * @param op_status
	 *            ：对话框操作状态，参见字段说明
	 * @param imsi
	 *            ：imsi号
	 * @return 说明返回值含义
	 */
	public void insertDialogOpInfo(String serial, String date, String content,
			String op_status, String imsi) {
		getDialogOpInfoCount();
		DebugFlags.EtengLog("开始向对话框操作库中插入一条记录，数据为：编号：" + serial + ",日期：" + date
				+ "，操作结果：" + content + "，对话框操作状态：" + op_status + ",imsi:"
				+ imsi);
		ContentValues values = new ContentValues();
		values.put(MySqliteHelper.DIALOG_SERIAL_NUMBER, serial);
		values.put(MySqliteHelper.DIALOG_DATE, date);
		values.put(MySqliteHelper.DIALOG_CONTENT, content);
		values.put(MySqliteHelper.DIALOG_OP, op_status);
		values.put(MySqliteHelper.DIALOG_IMSI, imsi);
		long row = db.insert(MySqliteHelper.TABLE_DIALOG_OP, null, values);
		DebugFlags.EtengLog("成功向对话框操作库中插入一条记录 row=" + row);
	}

	
	/**
	 * 插入一条匹配记录到匹配库中
	 * 
	 * @param serial
	 *            ：记录序号
	 * @param date
	 *            ：匹配日期
	 * @param content
	 *            ：匹配结果
	 * @param dialog_status
	 *            ：对话框弹出状态，1：弹出，2：未弹出
	 * @param imsi
	 *            ：imsi号
	 * @return 说明返回值含义
	 */
	public void insertMatchInfo(String serial, String date, String content,
			String dialog_status, String imsi) {
		getMatchedOptCount();

		DebugFlags.EtengLog("开始向匹配库中插入一条记录，数据为：编号：" + serial + ",日期：" + date
				+ "，匹配结果：" + content + "，对话框弹出状态：" + dialog_status + ",imsi:"
				+ imsi);
		ContentValues values = new ContentValues();
		values.put(MATCHED_SERIAL_NUMBER, serial);
		values.put(MATCHED_DATE, date);
		values.put(MATCHED_CONTENT, content);
		values.put(MATCHED_DIALOG_STATUS, dialog_status);
		values.put(MATCHED_IMSI, imsi);

		long row = db.insert(MySqliteHelper.TABLE_MATCHED, null, values);
		DebugFlags.EtengLog("成功向匹配库中插入一条记录 row=" + row);
	}

	/**
	 * 获取匹配记录数
	 * 
	 */
	public int getMatchedOptCount() {
		int i = 0;
		int id = 0;
		Cursor cur = db.query(TABLE_MATCHED, null, null, null, null, null,
				"_id asc");
		i = cur.getCount();
		if (i > 0) {
			if (cur.moveToFirst()) {
				id = cur.getInt(cur.getColumnIndex("_id"));
			}
		}
		cur.close();
		if (i > ConstantsInfo.ROW_MAX) {
			db.delete(TABLE_MATCHED, MySqliteHelper.ID + "=" + id, null);
		}
		return i;
	}

	/**
	 * 获取最大的弹出时间
	 */
	private String getCreateMaxEx() {
		Cursor cur = db.query(TABLE_NAME, null, "isShow<>?",
				new String[] { "2" }, null, null, "CreateDate Desc");
		if (cur != null)
			if (cur.getCount() != 0) {
				if (cur.moveToFirst()) {
					String CreateDate = cur.getString(cur
							.getColumnIndex(MySqliteHelper.CreateDate));
					cur.close();
					return CreateDate;
				}
			}
		cur.close();
		return ConstantsInfo.MIN_TIME;
	}

	/**
	 * 获取最大的弹出时间，目的是为了和当前的时间做比较，如果大于当前时间，就不弹出，如果等于当前时间就弹出，如果没有弹出过就返回时间最小值，表示弹出。
	 *  @param currentCreateDate 当前的回复短信时间字符
	 *  @Return 最大的弹出过消息通知的时间字符
	 */
	public String getShowMaxCreateDate(String currentCreateDate) {
		String selection = "isShow=? and sendState<>?";
		String[] selectionArgs = new String[] { "1", "9" };
		Cursor cur = db.query(TABLE_NAME, null, selection, selectionArgs, null,
				null, "CreateDate Desc");
		// 获取最大的弹出时间
		if (cur != null)
			if (cur.getCount() != 0) { // 如果存在最大的弹出时间
				if (cur.moveToFirst()) {
					String ShowMaxDate = cur.getString(cur
							.getColumnIndex(MySqliteHelper.CreateDate)); // 弹出过的最大时间
					cur.close();
					updateIsShow(currentCreateDate, "1"); // 更新当前分组为弹出过的分组

					if (ShowMaxDate.equals(currentCreateDate)) { // 如果最大的弹出时间和当前的时间相等

						return ConstantsInfo.MIN_TIME; // 弹出

					} else {

						return ShowMaxDate; // 返回给定的最大弹出过的时间
					}
				}
			} else { // 如果不存在最大的弹出时间
				cur.close();
				updateIsShow(currentCreateDate, "1");
				return ConstantsInfo.MIN_TIME; // 弹出
			}
		cur.close();
		return ConstantsInfo.MIN_TIME;
	}

}
