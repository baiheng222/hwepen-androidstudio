package com.hanvon.hwepen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.hanvon.application.AppManage;
import com.hanvon.bean.User;
import com.hanvon.common.ServiceWS;
import com.hanvon.db.DBManager;
import com.hanvon.splash.SplashActivity;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.HttpClientHelper;
import com.hanvon.util.StringUtil;

import org.json.JSONObject;

public class LogInActivity extends BaseActivity
{
	
	private EditText username;
	private EditText password;
	private ImageView login;
	private ImageView toRegister;
	
	private String usernameValue;
	private String passwordValue;
	
	private DBManager dbManager;
	
	private ProgressDialog pd;
	
	private static final String TAG = "LogInActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title bar
		getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);//remove status bar
		setContentView(R.layout.login);
		
		dbManager = new DBManager(this);
		
		username = (EditText) findViewById(R.id.username_edit);
		password = (EditText) findViewById(R.id.password_edit);
		login = (ImageView) findViewById(R.id.login_image);
		toRegister = (ImageView) findViewById(R.id.login2register);
		
		//登录监听事件
		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				usernameValue = username.getText().toString();
				passwordValue = password.getText().toString();
				
				if (usernameValue.equals("") || passwordValue.equals(""))
				{
					Toast.makeText(LogInActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
				}
				else
				{
					if (new ConnectionDetector(LogInActivity.this).isConnectingTOInternet())
					{
						pd = ProgressDialog.show(LogInActivity.this, "", "正在登录......");
						new Thread(loginThread).start();
					}
					else
					{
						Toast.makeText(LogInActivity.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		//注册跳转
		toRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(LogInActivity.this, RegisterActivity.class));
			}
		});
		
		/**
		 * 获取屏幕尺寸、密度
		 */
		DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;  // 屏幕高度（像素）
        float density = metric.density;  // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
        Log.i(TAG, "屏幕大小：" + width + "x" + height);
        Log.i(TAG, "屏幕密度：" + densityDpi + "------" + density);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (!StringUtil.isEmpty(RegisterActivity.registerName)) {
			username.setText(RegisterActivity.registerName);
			password.requestFocus(); //用户名有了 密码框获取焦点
			RegisterActivity.registerName = null;
		} else {
			if (!StringUtil.isEmpty(MainActivity.curUserId)) {
				username.setText(MainActivity.curUserId);
				username.setSelection(MainActivity.curUserId.length());
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) { //屏蔽menu键
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	Runnable loginThread = new Runnable() {
		
		@Override
		public void run() {
			try {
				JSONObject paramJson = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("user", usernameValue);
				paramJson.put("pwd", passwordValue);
				String responce = HttpClientHelper.sendPostRequest(ServiceWS.LOGIN, paramJson.toString());
				
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("responce", responce);
				message.setData(bundle);
				LogInActivity.this.loginHandler.sendMessage(message);
			} catch (Exception e) {
				pd.dismiss();
				e.printStackTrace();
			}
		}
	};
	Handler loginHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			pd.dismiss();
			try {
				Bundle bundle = msg.getData();
				String responce = bundle.getString("responce");
				JSONObject jsonObj = new JSONObject(responce);
				if (jsonObj.get("code").equals("0")) {
					SplashActivity.userId = usernameValue;
					Toast.makeText(LogInActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
					startActivity(new Intent(LogInActivity.this, MainActivity.class));
					LogInActivity.this.finish();
					
					//登录成功信息保存至数据库
					User user = new User();
					user.setUserId(usernameValue);
					user.setPassword(passwordValue);
					user.setToken(jsonObj.getString("token"));
					user.setStatus("0");
					dbManager.user_add(user);
				} else {
					Toast.makeText(LogInActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				pd.dismiss();
				Toast.makeText(getApplication(), "网络连接超时", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};
}