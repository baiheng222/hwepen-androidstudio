package com.hanvon.hwepen;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.hanvon.common.ServiceWS;
import com.hanvon.util.HttpClientHelper;

import org.json.JSONObject;

public class AutoUpdate {
	//private final String TAG = "AutoUpdate";
	private Context mContext;
	
	private static final String TAG = "AutoUpdate";

	public AutoUpdate(Context context) {
		this.mContext = context;
	}

	public void autoUpdate() {
		new Thread(autoUpdateThread).start();
	}

	Runnable autoUpdateThread = new Runnable()
	{

		@Override
		public void run() {
			Log.d("AutoUpdate", "AutoUpdate Runnable !!!!!");
			try {
				String version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
//				String url = "http://api.hanvon.com/rt/ap/v1/pub/std/soft/upg";
//				String url = "http://cloud.hwyun.com/ws-cloud/rt/ap/v1/pub/std/soft/upg";
				String url = ServiceWS.UPDATE;
				JSONObject json = new JSONObject();
				json.put("uid", "");
				json.put("sid", "hwepen-android");
				json.put("ver", version);
				json.put("type", "");
				String responce = HttpClientHelper.postData(url, json.toString());
				
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("responce", new String(Base64.decode(responce, Base64.DEFAULT), "UTF-8"));
				message.setData(bundle);
				updateHandler.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	Handler updateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			try
			{
				Bundle bundle = msg.getData();
				String responce = bundle.getString("responce");
				JSONObject obj = new JSONObject(responce);
				if (obj.get("code").equals("0"))
				{
					final String updateUrl = obj.getString("result");
					if (null != updateUrl && !"".equals(updateUrl))
					{
						AlertDialog.Builder builder = new Builder(mContext);
						builder.setCancelable(false); // 点击对话框以外的屏幕其他区域，不关闭对话框
						builder.setTitle("版本升级");
						builder.setMessage("版本更新了，赶快来下载吧！");
						builder.setPositiveButton("升级",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										Intent intent = new Intent();
										intent.setAction("android.intent.action.VIEW");
										Uri content_url = Uri.parse(updateUrl);
										intent.setData(content_url);
										mContext.startActivity(intent);
										Log.d(TAG, "版本升级......");
									}
								});
						builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										//取消无操作
										Log.d(TAG, "取消了版本升级......");
									}
								});
						AlertDialog dialog = builder.create();
						dialog.show();
					}
				}
				else
				{
					Log.d(TAG, "已是最新版本......");
				}
			}
			catch (Exception e)
			{
//				Toast.makeText(mContext, "网络连接异常", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		};
	};
}
