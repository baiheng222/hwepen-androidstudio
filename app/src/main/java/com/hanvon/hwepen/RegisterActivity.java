package com.hanvon.hwepen;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.hanvon.application.AppManage;
import com.hanvon.common.ServiceWS;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.HttpClientHelper;
import com.hanvon.util.ResourceChecker;
import com.hanvon.util.StringUtil;

import org.json.JSONObject;

public class RegisterActivity extends BaseActivity
{
	
	private EditText username;
	private EditText password;
	private EditText passwordConfirm;
	private EditText mail;
	private ImageView register;
	private ImageView back;
	
	private String usernameValue;
	private String passwordValue;
	private String passwordConfirmValue;
	private String mailValue;
	
	public static String registerName;
	
	private ProgressDialog pd;
	
	private static final String TAG = "RegisterActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);//remove status bar
		setContentView(R.layout.register);
		
		username = (EditText) findViewById(R.id.register_username);
		password = (EditText) findViewById(R.id.register_password);
		passwordConfirm = (EditText) findViewById(R.id.register_password_confirm);
		mail = (EditText) findViewById(R.id.register_mail);
		register = (ImageView) findViewById(R.id.register);
		back = (ImageView) findViewById(R.id.register_back);
		
		register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				closeInputMethod();
				usernameValue = username.getText().toString();
				mailValue = mail.getText().toString();
				passwordValue = password.getText().toString();
				passwordConfirmValue = passwordConfirm.getText().toString();
				
				if (StringUtil.isEmpty(usernameValue) || StringUtil.isEmpty(mailValue) || StringUtil.isEmpty(passwordValue) || StringUtil.isEmpty(passwordConfirmValue)) {
					Toast.makeText(RegisterActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if (ResourceChecker.isNumeric(usernameValue)) {
					Toast.makeText(RegisterActivity.this, "用户名不能全为数字", Toast.LENGTH_SHORT).show();
					return;
				}
				if (usernameValue.length() < 6 || usernameValue.length() > 12) {
					Toast.makeText(RegisterActivity.this, "用户名6~12位数字、字母", Toast.LENGTH_SHORT).show();
					return;
				}
				if (passwordValue.length() < 6 || passwordValue.length() > 12) {
					Toast.makeText(RegisterActivity.this, "密码6~12位数字、字母", Toast.LENGTH_SHORT).show();
					return;
				}
				if (!passwordValue.equals(passwordConfirmValue)) {
					Toast.makeText(RegisterActivity.this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
					return;
				}
				if (!ResourceChecker.isEmail(mailValue)) {
					Toast.makeText(RegisterActivity.this, "邮箱格式不正确，请检查后重新输入！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (new ConnectionDetector(RegisterActivity.this).isConnectingTOInternet()) {
					new Thread(checkNameThread).start();
				} else {
					Toast.makeText(RegisterActivity.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
				}
				
				
			}
		});
		
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		this.finish();
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) { //屏蔽menu键
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	Runnable checkNameThread = new Runnable() {
		
		@Override
		public void run() {
			try {
				JSONObject paramJson = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("ver", "");
				paramJson.put("data", usernameValue);
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(ServiceWS.CHKNAME, params);
				
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("responce", responce);
				message.setData(bundle);
				RegisterActivity.this.checkNameHandler.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	Handler checkNameHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				Bundle bundle = msg.getData();
				String responce = bundle.getString("responce");
				if (StringUtil.isEmpty(responce)) {
					Toast.makeText(getApplication(), "网络连接超时", Toast.LENGTH_SHORT).show();
				} else {
					JSONObject jsonObj = new JSONObject(responce);
					if (jsonObj.get("code").equals("0")) {
						Log.i(TAG, "name is not overlapped");
						pd = ProgressDialog.show(RegisterActivity.this, "", "正在注册......");
						new Thread(registerThread).start();
					} else {
						Log.i(TAG, "code != 0");
						Toast.makeText(RegisterActivity.this, "用户名已经存在", Toast.LENGTH_SHORT).show();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	Runnable registerThread = new Runnable() {
		
		@Override
		public void run() {
			try {
				JSONObject paramJson = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("ver", "");
				paramJson.put("user", usernameValue);
				paramJson.put("pwd", passwordValue);
				paramJson.put("email", mailValue);
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(ServiceWS.REGISTER, params);
				
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("responce", responce);
				message.setData(bundle);
				RegisterActivity.this.registerHandler.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	Handler registerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				pd.dismiss();
				Bundle bundle = msg.getData();
				String responce = bundle.getString("responce");
				if (StringUtil.isEmpty(responce)) {
					Toast.makeText(getApplication(), "无法连接到服务器", Toast.LENGTH_SHORT).show();
				} else {
					JSONObject json = new JSONObject(responce);
					if (json.getString("code").equals("0")) {
						Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
						registerName = usernameValue;
						RegisterActivity.this.finish();
					} else {
						Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
					}
				}
			} catch (Exception e) {
				pd.dismiss();
				e.printStackTrace();
			}
		}
	};
	
	private void closeInputMethod() {
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(RegisterActivity.this
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
	}
}
