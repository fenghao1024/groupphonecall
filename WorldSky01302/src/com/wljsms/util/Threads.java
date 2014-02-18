/**
 * Name : Threads.java
 * Version : 0.0.1
 * Copyright : Copyright (c) wanglaoji Inc. All rights reserved.
 * Description : 
 */
package com.wljsms.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wljsms.debug.DebugFlags;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/**
 * com.eteng.world.util.Threads
 * 
 * @author wanglaoji <br/>
 *         Create at 2013-1-9 下午1:36:51 Description : 操作android系统不对外开放的短信数据库接口
 *         Modified :
 */
public class Threads {
	private static final String[] ID_PROJECTION = { BaseColumns._ID };
	private static final Uri THREAD_ID_CONTENT_URI = Uri
			.parse("content://mms-sms/threadID");
	public static final Uri CONTENT_URI = Uri.withAppendedPath(
			Uri.parse("content://mms-sms/"), "conversations");
	public static final Uri OBSOLETE_THREADS_URI = Uri.withAppendedPath(
			CONTENT_URI, "obsolete");
	public static final Pattern NAME_ADDR_EMAIL_PATTERN = Pattern
			.compile("\\s*(\"[^\"]*\"|[^<>\"]+)\\s*<([^<>]+)>\\s*");

	public static final int COMMON_THREAD = 0;
	public static final int BROADCAST_THREAD = 1;
	
	private Threads() {
	}

	/**
	 * This is a single-recipient version of getOrCreateThreadId. It's
	 * convenient for use with SMS messages.
	 */
	public static long getOrCreateThreadId(Context context, String recipient) {
		Set<String> recipients = new HashSet<String>();

		recipients.add(recipient);
		return getOrCreateThreadId(context, recipients);
	}

	/**
	 * Given the recipients list and subject of an unsaved message, return its
	 * thread ID. If the message starts a new thread, allocate a new thread ID.
	 * Otherwise, use the appropriate existing thread ID.
	 * 
	 * Find the thread ID of the same set of recipients (in any order, without
	 * any additions). If one is found, return it. Otherwise, return a unique
	 * thread ID.
	 */
	public static long getOrCreateThreadId(Context context,
			Set<String> recipients) {
		Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();

		for (String recipient : recipients) {
			if (isEmailAddress(recipient)) {
				recipient = extractAddrSpec(recipient);
			}

			uriBuilder.appendQueryParameter("recipient", recipient);
		}

		Uri uri = uriBuilder.build();
	
		Cursor cursor = context.getContentResolver().query(uri, ID_PROJECTION,
				null, null, null);
		if (true) {
			DebugFlags.EtengLog(
						"getOrCreateThreadId cursor cnt: " + cursor.getCount());
		}
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					return cursor.getLong(0);
				} else {
					DebugFlags.EtengLog(
								"getOrCreateThreadId returned no rows!");
				}
			} finally {
				cursor.close();
			}
		}

		DebugFlags.EtengLog(
					"getOrCreateThreadId failed with uri " + uri.toString());
		throw new IllegalArgumentException(
				"Unable to find or allocate a thread ID.");
	}

	public static String extractAddrSpec(String address) {
		Matcher match = NAME_ADDR_EMAIL_PATTERN.matcher(address);

		if (match.matches()) {
			return match.group(2);
		}
		return address;
	}

	/**
	 * Returns true if the address is an email address
	 * 
	 * @param address
	 *            the input address to be tested
	 * @return true if address is an email address
	 */
	public static boolean isEmailAddress(String address) {
		if (TextUtils.isEmpty(address)) {
			return false;
		}

		String s = extractAddrSpec(address);

		Pattern pattern = Pattern
				.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		Matcher matcher = pattern.matcher(s);
		return matcher.matches();
	}
}
