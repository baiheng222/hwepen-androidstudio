package com.hanvon.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @desc 获取网络状态帮助类
 * @author  PengWenCai
 * @time 2015-6-25 上午11:38:56
 * @version
 */
public class NetWorkHelper {
	public static String getNetState(Context context) {
		String state = "";

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isWifiConnected = (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) ? true
				: false;

		netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isGprsConnected = (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) ? true
				: false;

		if (isWifiConnected) {
			state += ".wifi.";
		}
		if (isGprsConnected) {
			state += ".gprs.";
		}

		return state;
	}

}
