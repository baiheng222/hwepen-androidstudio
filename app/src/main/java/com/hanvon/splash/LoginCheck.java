package com.hanvon.splash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hanvon.bean.User;
import com.hanvon.common.DevCons;
import com.hanvon.common.ServiceWS;
import com.hanvon.util.HttpClientHelper;

import org.json.JSONObject;

public class LoginCheck
{

	private String userId;
	private String pwd;
	
	private static final String TAG = "LoginCheck";

	public LoginCheck(String userId, String pwd) {
		this.userId = userId;
		this.pwd = pwd;
	}

	public void loginCheck() {
		new Thread(loginThread).start();
	}

	Runnable loginThread = new Runnable() {
		
		@Override
		public void run() {
			try {
				JSONObject paramJson = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("user", userId);
				paramJson.put("pwd", pwd); 
				String responce = HttpClientHelper.sendPostRequest(ServiceWS.LOGIN, paramJson.toString());
				
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("responce", responce);
				message.setData(bundle);
				loginHandler.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	Handler loginHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				Bundle bundle = msg.getData();
				String responce = bundle.getString("responce");
				JSONObject jsonObj = new JSONObject(responce);
				if (jsonObj.get("code").equals("0"))
				{
					//登录成功

					SplashActivity.userId = userId;

					User user = new User();
					user.setUserId(userId);
					user.setPassword(pwd);
					user.setToken(jsonObj.getString("token"));
					user.setStatus("0");
					SplashActivity.dbManager.user_add(user);

					Log.d(TAG, "登录验证成功");
				}
				else
				{
					//登陆失败

					User user = new User();
					user.setUserId(userId);
					user.setPassword(pwd);
//					user.setToken(jsonObj.getString("token")); //登录失败无token
					user.setStatus("1");
					SplashActivity.dbManager.user_update(user);

					Log.d(TAG, "登录验证失败");
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	};
}
