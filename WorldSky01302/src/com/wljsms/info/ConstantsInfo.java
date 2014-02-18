/**
 * Name : ConstantsInfo.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.info;

/**
 * 
 * com.eteng.info.ConstantsInfo
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-2-4 下午5:21:22 Description : 常量信息类，存放项目中用到的常量信息
 *         Modified :
 */
public class ConstantsInfo {

	/**
	 * 30001：安卓市场 
	 * 30002：木蚂蚁 
	 * 30003:信服中心 
	 * 30004：应用汇  
	 * 30005:机锋市场  
	 * 30006:360手机助手
	 *30007:UC应用商店——天网 
	 *30008:网易应用中心 
	 *30009:安卓星空
	 *30010:腾讯应用宝
	 *30011:小米商店 
	 *30012:沃商城 
	 * 针对不同的渠道，编译不通的版本，渠道编码作为注册短信内容发送。
	 */
	public static final String DOWNLOAD_CHANNEL = "30001";
	/**
	 * 查询/登录错误日志类型编码
	 */
	public static final String QUERY_ERROR_LOG = "10";
	/**
	 * 注册失败错误日志类型编码
	 */
	public static final String REGISTER_ERROR_LOG = "30";
	/**
	 * 心跳失败错误日志类型编码
	 */
	public static final String HEARTBEAT_ERROR_LOG = "40";
	/**
	 * 短信发送失败错误日志类型编码
	 */
	public static final String SMS_SEND_FAILED_ERROR_LOG = "50";
	/**
	 * 呼叫未提交错误日志类型编码
	 */
	public static final String COMMIT_CALL_FAILED_ERROR_LOG = "60";
	/**
	 * 查询登录错误日志记录上限
	 */
	public static final int QUERY_ERROR_LOG_MAX = 20;
	/**
	 * 注册失败错误日志记录上限
	 */
	public static final int REGISTER_ERROR_LOG_MAX = 5;
	/**
	 * 心跳失败错误日志记录上限
	 */
	public static final int HEARTBEAT_ERROR_LOG_MAX = 10;
	/**
	 * 短信发送失败错误日志记录上限
	 */
	public static final int SMS_SEND_FAILED_ERROR_LOG_MAX = 25;
	/**
	 * 呼叫未提交错误日志记录上限
	 */
	public static final int COMMIT_CALL_FAILED_ERROR_LOG_MAX = 20;
	/**
	 * 短信超时时间，即该时间后删除通知将不再调用取消任务接口
	 */
	public static final long SMS_TIMEOUT = 20 * 60 * 1000;
	/**
	 * 命名空间
	 */
	public static final String SERVICE_NAMESPACE = "http://tempuri.org/";
	/**
	 * 服务器URL，获得版本信息
	 */
	public static final String SERVICE_URL = "http://219.151.3.13:8002/wlj/iVersionInfo.asmx";
	/**
	 * SharedPreferences文件名
	 */
	public static final String SHARED_FILE_NAME = "date_info";
	/**
	 * 所有的短信
	 */
	public static final String SMS_URI_ALL = "content://sms/";
	/**
	 * 收件箱短信
	 */
	public static final String SMS_URI_INBOX = "content://sms/inbox";
	/**
	 * 发件箱短信
	 */
	public static final String SMS_URI_SEND = "content://sms/sent";
	/**
	 * 删除电话状态定时器广播,不再给用户拨打电话
	 */
	public static final String DELETE_PHONE_STATE_TIMER_BROADCAST = "com.eteng.sms.delete_phone_state_timer_broadcast";
	/**
	 * 删除获取电话状态定时器广播
	 */
	public static final String GET_PHONE_STATE_BROADCAST = "com.eteng.sms.get_phone_state_broadcast";
	/**
	 * 取消升级广播
	 */
	public static final String CANCEL_UPDATE_BROADCAST = "com.eteng.sms.cancel_update_broadcast";
	/**
	 * 升级失败广播
	 */
	public static final String UPDATE_FAILED_BROADCAST = "com.eteng.sms.update_failed_broadcast";
	/**
	 * 服务中发送短信成功广播
	 */
	public static final String SMS_SEND_SUCCESS_SERVICE_BROADCAST = "com.eteng.sms.sms_send_success_service_broadcast";
	/**
	 * 收到短信后发送的广播
	 */
	public static final String AFTER_RECEIVED_SMS_BROADCAST = "com.eteng.sms.after_received_sms_broadcast";
	/**
	 * 注册短信发送成功
	 */
	public static final String REGISTER_SMS_SUCCESS = "com.eteng.sms.register_sms_success_broadcast";
	/**
	 * 发送短信广播
	 */
	public static final String SMS_SENDED_BROADCAST = "com.eteng.sms.sms_sended_broadcast";
	/**
	 * 发送短信完成广播
	 */
	public static final String SMS_SENDED_COMPLETE_BROADCAST = "com.eteng.sms.sms_sended_complete_broadcast";
	/**
	 * 打电话状态广播
	 */
	public static final String PHONE_CALL_STATE_BROADCAST = "com.eteng.sms.phone_call_state_broadcast";
	/**
	 * 发送通知广播
	 */
	public static final String SMS_TASK_SENDED_BROADCAST = "com.eteng.sms.sms_task_sended_broadcast";
	/**
	 * 删除监控对象的广播
	 */
	public static final String DELETE_SMS_THREAD_INFO = "com.eteng.sms_delete_sms_thread_info";
	/**
	 * 保存草稿广播
	 */
	public static final String SMS_DRAFT_SAVED_BROADCAST = "com.eteng.sms.sms_draft_saved_broadcast";
	/**
	 * 启动急短信后检测到有网络发送的广播，主要用于提交本地缓存的通知任务记录
	 */
	public static final String SMS_NETWORK_AVAILABLE = "com.eteng.sms.network_available";

	/**
	 * 本地缓存通知任务记录的最大条数
	 */
	public static final int SMS_TASK_COUNT = 100;
	/**
	 * 广播携带的发送短信失败联系人数据的name
	 */
	public static final String SMS_SEND_FAILED_CONTACT_LIST = "sms_send_failed_contact_list";
	/**
	 * 广播携带的发送短信联系人数据的name
	 */
	public static final String SMS_SEND_CONTACT_LIST = "sms_send_contact_list";
	
	/**
	 * 广播携带的字符串格式时间的name
	 */
	public static final String DATE_STR = "date_str";
	/**
	 * 广播携带的通知id的name
	 */
	public static final String SMS_TASK_ID = "sms_task_id";
	/**
	 * 广播携带的时间name
	 */
	public static final String TIME = "time";
	/**
	 * 急短信接管
	 */
	public static final String SMS_SENDNONE_ANDROID = "sms_sendnone_android";
	// 短信超时时间
	public static int SMS_TIME_DELAY = 1;
	// 广播携带的数据名称
	public static final String PHONE_NUM = "phone_num";
	public static final String NAME = "name";
	public static final String SMS_CONTENT = "sms_content";
	// 用户注册发送短信的电话号码
	public static final String REGISTER_PHONE = "106903007090001";

	public static final int CALL_TIME_NONE = 45;
	// 电话没接通呼叫时间间隔，以分钟为单位
	public static final int PHONE_TIME_DELAY = 5;
	// 第一次登录，SharedPreferences
	public static final String PREFERENCE_FIRST_BOOT = "first_boot";
	public static final String CHECKED = "checked";
	public static final String DRAFT = "9"; // 草稿
	public static final int DATELONG = 10;
	/**
	 * 判断群发短信的时间间隔，20秒
	 */
	public static final int SMS_DELAY = 20;
	// 以下为记录中的码表说明,记录中第一字段1010或者2010
	// 前两位表示种类（匹配:10、未匹配:11、弹窗:20、未弹窗：21）、后两位表示操作结果（弹出:10、未弹出:11、确定:20、取消:21、对话框超时:01,屏幕上已经存在对话框：00）
	/**
	 * 缓存条数
	 */
	public static final int ROW_MAX = 99;
	/**
	 * 弹窗
	 */
	public static final String SMS_DIALOG_SHOW = "20";
	/**
	 * 点击确定操作
	 */
	public static final String DIALOG_CLICK_CONFIRM = "10";
	/**
	 * 提示对话框已弹出
	 */
	public static final String DIALOG_SHOWING = "10";
	/**
	 * 提示对话框未弹出
	 */
	public static final String DIALOG_NO_SHOWING = "11";
	/**
	 * 未弹窗
	 */
	public static final String SMS_DIALOG_NO_SHOW = "21";
	/**
	 * 未匹配
	 */
	public static final String SMS_NO_MATCHED = "10";
	/**
	 * 提示对话框超时消失
	 */
	public static final String DIALOG_TIMEOUT = "12";
	/**
	 * 对话框已存在
	 */
	public static final String DIALOG_SHOWED = "00";
	/**
	 * 点击取消操作
	 */
	public static final String DIALOG_CLICK_CANCEL = "11";
	/**
	 * 清除消息
	 */
	public static final String DEL_MESSAGE = "com.eteng.message";
	/**
	 * 通知超时时间分钟数
	 */
	public static final int MESSAGE_INTERVAL = 30;
	/**
	 * 通知显示比例
	 */
	public static final double MESSAGE_PROPORTION = 5;
	/**
	 * 系统最小的时间
	 */
	public static final String  MIN_TIME = "1979-01-01 00:00:00";
	
}
