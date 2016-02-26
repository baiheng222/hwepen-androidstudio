package com.hanvon.hwepen;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Class used to maintain settings. 设置类，对配置文件进行读取写入操作。该类采用了单例模式。
 * 
 * @ClassName Settings
 * @author keanbin
 */
public class Settings
{
    private static final String ANDPY_CONFS_UPDATE_KEY = "AutoCheckVersionUpdate";


	/***增加对软件升级检测的判断**/
	private static boolean mAutoCheckVersionUpdate;
	/*********end************/
	private static Settings mInstance = null;

	/**
	 * 引用计数
	 */
	private static int mRefCount = 0;

	public static SharedPreferences mSharedPref = null;

	protected Settings(SharedPreferences pref) {
		mSharedPref = pref;
		initConfs();
	}

	/**
	 * 获得该实例
	 * 
	 * @param pref
	 * @return
	 */
	public static Settings getInstance(SharedPreferences pref) {
		if (mInstance == null) {
			mInstance = new Settings(pref);
		}
		assert (pref == mSharedPref);
		mRefCount++;
		return mInstance;
	}

	/**
	 * 设置震动、声音、预报开关标记进入配置文件
	 */
	public static void writeBack() {
		Editor editor = mSharedPref.edit();
		editor.putBoolean(ANDPY_CONFS_UPDATE_KEY, mAutoCheckVersionUpdate);
		editor.commit();
	}

	/**
	 * 释放对该实例的使用。
	 */
	public static void releaseInstance() {
		mRefCount--;
		if (mRefCount == 0) {
			mInstance = null;
		}
	}

	/**
	 * 初始化，从配置文件中取出震动、声音、预报开关标记。
	 */
	private void initConfs() {
		mAutoCheckVersionUpdate = mSharedPref.getBoolean(ANDPY_CONFS_UPDATE_KEY, true);
	}

	/**
	 * 获取软件升级检测
	 * 
	 * */
	 public static boolean getKeyVersionUpdate(Context context) {
		
		if(mSharedPref==null){
			mSharedPref= PreferenceManager.getDefaultSharedPreferences(context);
		}
		mAutoCheckVersionUpdate =mSharedPref.getBoolean(ANDPY_CONFS_UPDATE_KEY, true);

		return mAutoCheckVersionUpdate;
	}
	 /**
	  * 设置软件升级检测
	  * 
	  * */
	public static void setKeyVersionUpdate(boolean v) {
			if (mAutoCheckVersionUpdate == v)
				return;
			Editor editor = mSharedPref.edit();
			editor.putBoolean(ANDPY_CONFS_UPDATE_KEY, v);
			editor.commit();
			mAutoCheckVersionUpdate = v;		
	}
}
