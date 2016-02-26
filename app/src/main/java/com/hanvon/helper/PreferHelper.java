package com.hanvon.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @desc SharedPreference 帮助类
 * @author  PengWenCai
 * @time 2015-6-25 上午11:39:21
 * @version
 */
public class PreferHelper {
	private static final String PREFERENCES_NAME = "com.hanvon.sulupen";
	private static SharedPreferences prefer = null;

	public synchronized static void init(Context context) {
		if (prefer == null) {
			prefer = context.getSharedPreferences(PREFERENCES_NAME,
					Context.MODE_PRIVATE);
		}
	}

	public static void saveBoolean(String name, boolean value) {

		SharedPreferences.Editor editor = prefer.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}

	public static boolean getBoolean(String name, boolean defaultValue) {
		return prefer.getBoolean(name, defaultValue);
	}

	public static void saveLong(String name, long value) {
		SharedPreferences.Editor editor = prefer.edit();
		editor.putLong(name, value);
		editor.commit();
	}

	public static long getLong(String name, long defaultValue) {
		return prefer.getLong(name, defaultValue);
	}

	public static void saveInt(String name, int value) {
		SharedPreferences.Editor editor = prefer.edit();
		editor.putInt(name, value);
		editor.commit();
	}

	public static int getInt(String name, int defaultValue) {
		return prefer.getInt(name, defaultValue);
	}

	public static void saveFloat(String name, float value) {
		SharedPreferences.Editor editor = prefer.edit();
		editor.putFloat(name, value);
		editor.commit();
	}

	public static float getFloat(String name, float defaultValue) {
		return prefer.getFloat(name, defaultValue);
	}

	public static void saveString(String name, String value) {
		SharedPreferences.Editor editor = prefer.edit();
		editor.putString(name, value);
		editor.commit();
	}

	public static String getString(String name, String defaultValue) {
		return prefer.getString(name, defaultValue);
	}

}
