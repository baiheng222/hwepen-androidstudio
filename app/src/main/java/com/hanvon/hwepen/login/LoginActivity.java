package com.hanvon.hwepen.login;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;

import com.hanvon.application.HanvonApplication;
import com.hanvon.hwepen.MainActivity;
import com.hanvon.hwepen.R;
import com.hanvon.splash.SplashActivity;
import com.hanvon.util.HttpClientHelper;
import com.hanvon.net.JsonData;
import com.hanvon.net.RequestResult;
import com.hanvon.net.RequestServerData;
import com.hanvon.util.LogUtil;
import com.hanvon.util.ClearEditText;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.LoginUtils;

import com.mob.tools.utils.UIHandler;
//import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.tauth.Tencent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements Callback, 
OnClickListener, PlatformActionListener  {

	private TextView TVSkip;
	private ClearEditText ETUserName;
	private ClearEditText ETPassWord;
	private Button BTLogin;
	private TextView TVRegist;
	private TextView TVForgetPassword;
	private String strUserName;
	private String strPassWord;
	
	private ProgressDialog pd;

	private ImageView LLQQUser;
	private ImageView LLWXUser;
	
	private int userflag = 0;
	public static LoginActivity instance = null;
	public String flag;   // 0 从其他界面跳转 1 从云信息登陆跳转  2 从上传界面跳转
	
	private static final int MSG_USERID_FOUND = 1;
	private static final int MSG_LOGIN = 2;
	private static final int MSG_AUTH_CANCEL = 3;
	private static final int MSG_AUTH_ERROR= 4;
	private static final int MSG_AUTH_COMPLETE = 5;
	private static final int MSG_CLIENT_ERROR= 6;
	
	private String openid;
	private String figureurl;
	private String nickname;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	//	ShareSDK.initSDK(this);
		instance = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);

	    TVSkip = (TextView) findViewById(R.id.login_quit);
		ETUserName = (ClearEditText) findViewById(R.id.username_editText);
		ETPassWord = (ClearEditText) findViewById(R.id.passwd_editText);
		BTLogin = (Button) findViewById(R.id.login_button);
		TVRegist = (TextView) findViewById(R.id.registuser);
		TVForgetPassword = (TextView) findViewById(R.id.remember_pwd);

		LLQQUser = (ImageView)findViewById(R.id.login_qq);
		LLWXUser = (ImageView)findViewById(R.id.login_weixin);

		TVSkip.setOnClickListener(this);
		ETUserName.setOnClickListener(this);
		ETPassWord.setOnClickListener(this);
		BTLogin.setOnClickListener(this);
		TVRegist.setOnClickListener(this);
		TVForgetPassword.setOnClickListener(this);
		LLQQUser.setOnClickListener(this);
		LLWXUser.setOnClickListener(this);

		if (HanvonApplication.mTencent == null) {
			HanvonApplication.mTencent = Tencent.createInstance("1104705079", this);
	    }
		
		Intent intent = getIntent();
		if (intent != null){
			flag = intent.getStringExtra("flag");
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		    case R.id.login_quit:
		    	goHome();
			    if (flag != null){
					if (Integer.valueOf(flag) == 1){
					//	startActivity(new Intent(LoginActivity.this, MyCloudActivity.class));
					//    LoginActivity.this.finish();
					}else if (Integer.valueOf(flag) == 2){
					///	startActivity(new Intent(LoginActivity.this, ScanNoteDetailActivity.class));
					 //   LoginActivity.this.finish();
					}
				}else{
				//	goHome();
				}
			    break;
            case R.id.login_button:
            	strPassWord = ETPassWord.getText().toString();
            	strUserName = ETUserName.getText().toString();
				if (strPassWord.equals("") || strUserName.equals("")){
					Toast.makeText(LoginActivity.this, "用户名或者密码不允许为空", Toast.LENGTH_SHORT).show();
					return;
				}
				/*
				if ((strPassWord.length() < 6) || (strPassWord.length() > 16)){
					Toast.makeText(LoginActivity.this, "密码应为6-16位字母和数字组合!", Toast.LENGTH_SHORT).show();
					return;
				} else {
					Pattern pN = Pattern.compile("[0-9]{6,16}");
				    Matcher mN = pN.matcher(strPassWord);
				    Pattern pS = Pattern.compile("[a-zA-Z]{6,16}");
				    Matcher mS = pS.matcher(strPassWord);
				    if((mN.matches()) || (mS.matches())){
				        Toast.makeText(LoginActivity.this,"请输入符合规则的密码!", Toast.LENGTH_SHORT).show();
				        return;
				    }
				}*/
				LogUtil.i("username:"+strUserName+", passwd:"+strPassWord);
				InputMethodManager m=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
				m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            	judgeUserIsOk();
				break;
            case R.id.registuser:
            	LogUtil.i("INTO Create user Before");
            	Intent intent = new Intent(LoginActivity.this, RegisterUserGetCodePhone.class);
                LoginActivity.this.startActivity(intent);
                LoginActivity.this.finish();
                break;
            case R.id.remember_pwd:
            	Intent intent1 = new Intent(LoginActivity.this, RememberPassword.class);
                LoginActivity.this.startActivity(intent1);
                LoginActivity.this.finish();
                break;
                
            case R.id.login_qq:
            	QQUserLogin();
            	break;
            
            case R.id.login_weixin:
            	weiXinUserLogin();
            	break;

			default:
			    break;
		}
	}

	private void goHome() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		LoginActivity.this.startActivity(intent);
		LoginActivity.this.finish();
	}

	public void judgeUserIsOk(){
		if (new ConnectionDetector(LoginActivity.this).isConnectingTOInternet()) {
			pd = ProgressDialog.show(LoginActivity.this, "", "正在登录......");
			new Thread(loginThread).start();
		} else {
			Toast.makeText(LoginActivity.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
		}
		
	}
	
    Runnable loginThread = new Runnable() {
		
		@Override
		public void run() {
			try {
				JSONObject paramJson = new JSONObject();
				paramJson.put("uid",HanvonApplication.AppUid);
				paramJson.put("sid", HanvonApplication.AppSid);
				paramJson.put("user", strUserName);
				paramJson.put("pwd", strPassWord);
				LogUtil.i(paramJson.toString());
				String responce = HttpClientHelper.sendPostRequest("http://dpi.hanvon.com/rt/ap/v1/user/login", paramJson.toString());

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("responce", responce);
				message.setData(bundle);
				LoginActivity.this.loginHandler.sendMessage(message);
			} catch (Exception e) {
				pd.dismiss();
				e.printStackTrace();
			}
		}
	};
	
	Handler loginHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			try {
				Bundle bundle = msg.getData();
				String responce = bundle.getString("responce");
				JSONObject jsonObj = new JSONObject(responce);
				if (jsonObj.get("code").equals("0"))
				{
					LogUtil.i("***************************");
					new RequestTask().execute();
				}
				else if (jsonObj.get("code").equals("520"))
				{
					pd.dismiss();
					Toast.makeText(LoginActivity.this, "服务器异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				}
				else
				{
					pd.dismiss();
					Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				pd.dismiss();
				Toast.makeText(getApplication(), "网络连接超时", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};
	
	class RequestTask  extends AsyncTask<Void, Void, RequestResult>{
  	  @Override
	    protected void onPreExecute() {
	    	super.onPreExecute();
	    }
		@Override
		protected RequestResult doInBackground(Void... arg0) {
			RequestResult result=null;
		    result = getUserInfoFromServer();
			return result;
		}
		 //响应结果
	    protected void onPostExecute(RequestResult result)
		{
	        JsonData data = result.getData();
	        String jsonCode= data.getJson();
	        try
			{
	        	pd.dismiss();
			    JSONObject json=new JSONObject(jsonCode);
			    LogUtil.i(json.toString());
			    if (json.getString("code").equals("0") )
				{
			    	boolean isHasNick = true;
			    //	String email = json.getString("email");
			    //	String phone = json.getString("phone");
                    String nickname = json.getString("nickname");
                    if (nickname.equals("null"))
					{
                    	nickname ="";
                    }
                    if(json.getString("isActive").equals("1"))
					{
                    	HanvonApplication.isActivity = true;
                    }
					else
					{
                    	HanvonApplication.isActivity = false;
                    }
                    if(nickname.equals(""))
					{
                    //	nickname = strUserName;
                    	isHasNick = false;
                    }
                    String username = json.getString("user");
			    	SharedPreferences mSharedPreferences=getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
					Editor mEditor=	mSharedPreferences.edit();
					mEditor.putString("nickname", nickname);
					mEditor.putString("username", username);
					mEditor.putBoolean("isActivity", HanvonApplication.isActivity);
					HanvonApplication.hvnName = username;
			        HanvonApplication.strName = nickname;
					mEditor.putBoolean("isHasNick", isHasNick);
				//	if (!email.equals("")){
				//		mEditor.putString("email", email);
				//		HanvonApplication.strEmail = email;
				//	}
				//	if (!phone.equals("")){
				//		mEditor.putString("phone", phone);
				//		HanvonApplication.strPhone = phone;
				//	}
					mEditor.putString("passwd", strPassWord);
					mEditor.putInt("flag", 0);
					mEditor.putInt("status", 1);
					mEditor.commit();

					SplashActivity.userId = username;
					
					SharedPreferences mSharedCloudPreferences=getSharedPreferences("Cloud_Info", Activity.MODE_MULTI_PROCESS);
					int cloudType = mSharedCloudPreferences.getInt("cloudtype", 0);
					if (cloudType != 2){
					    Editor mCloudEditor = mSharedCloudPreferences.edit();
					    mCloudEditor.putString("cloudname", username);
					    mCloudEditor.putInt("cloudtype", 1);
				        HanvonApplication.cloudType = 1;
				        mCloudEditor.commit();
					}

					LogUtil.i("--------nickname:"+nickname+"  username:"+username);
					HanvonApplication.userFlag = 0;
					// 创建用户在sd上的文件目录
					String outputDirectory = "/data/data/com.hanvon.sulupen/users/"+username+"/";
					File file = new File(outputDirectory);
					// 如果目标目录不存在，则创建
					if (!file.exists()) {
					    file.mkdirs();
					}

					if (flag != null){
						if (Integer.valueOf(flag) == 1){
							//startActivity(new Intent(LoginActivity.this, MyCloudMsg.class));
						    LoginActivity.this.finish();
						}else if (Integer.valueOf(flag) == 2){
						//	startActivity(new Intent(LoginActivity.this, ScanNoteDetailActivity.class));
						    LoginActivity.this.finish();
						}else{
							Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
							startActivity(new Intent(LoginActivity.this, MainActivity.class));
						    LoginActivity.this.finish();
						}
					}else{
						Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
					  	startActivity(new Intent(LoginActivity.this, MainActivity.class));
					    LoginActivity.this.finish();
					}
			    }
		    } catch (JSONException e) {
			    e.printStackTrace();
		    }
	    }
    }

    public RequestResult getUserInfoFromServer(){
    	JSONObject paramJson=new JSONObject();
  	    try {
  		    paramJson.put("user", strUserName);
  	    } catch (JSONException e) {
  		    e.printStackTrace();
  	    }

  	    LogUtil.i(paramJson.toString());
  	    RequestResult result=new RequestResult();
 	    result=RequestServerData.getUserInfo(paramJson);
 	    LogUtil.i(result.toString());
  	    return result;
    }

	public void QQUserLogin(){
		if (new ConnectionDetector(LoginActivity.this).isConnectingTOInternet()) {
		}else{
			Toast.makeText(LoginActivity.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
			return;
		}
		LogUtil.i("INTO QQUserLogin!!!!!!!!");
		userflag = 1;
		pd = ProgressDialog.show(this, "", "正在登陆中，请稍后...");
		authorize(new QQ(this));
	}
	
	public void hvnUserLogin(){
	//	Drawable drawable = getResources().getDrawable(R.drawable.logo); 
	//	IVloginImage.setImageDrawable(drawable);
	}

	public void weiXinUserLogin(){
		if (new ConnectionDetector(LoginActivity.this).isConnectingTOInternet()) {
		}else{
			Toast.makeText(LoginActivity.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
			return;
		}
		LogUtil.i("INTO WeixinUserLogin!!!!!!!!");
		userflag = 2;
		pd = ProgressDialog.show(this, "", "正在登陆中，请稍后...");
		authorize(new Wechat(this));
	}
	
	public synchronized Drawable byteToDrawable(String icon) { 
	    byte[] img=Base64.decode(icon.getBytes(), Base64.DEFAULT);
	    Bitmap bitmap;
	    if (img != null) {
	        bitmap = BitmapFactory.decodeByteArray(img,0, img.length);  
	        @SuppressWarnings("deprecation")
	        Drawable drawable = new BitmapDrawable(bitmap);

	        return drawable;
	    }
	    return null;
	}

	@Override
	protected void onDestroy() {
		LogUtil.i("INTO onDestroy!!!!!!!!");
		super.onDestroy();
		
		if (pd != null){
			pd.dismiss();
		}
	}
	
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK )
	    {
	    	startActivity(new Intent(LoginActivity.this, MainActivity.class));
	    	this.finish();
	    }
	    return false;                                                                  
	}
	
	
	
	
	private void authorize(Platform plat) {	
		

		if(plat.isValid()) {
			LogUtil.i("------isValid --11111111-------" + plat.isValid());
			String userId = plat.getDb().getUserId();
			if (!TextUtils.isEmpty(userId)) {
				UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
				nickname = plat.getDb().getUserName();
				openid = plat.getDb().getUserId();
				figureurl = plat.getDb().getUserIcon();

				LogUtil.i("---nickname:" + nickname+"  openid:"+openid);
				
				login(plat.getName(), userId, null);
				return;
			}
		}
		LogUtil.i("------isValid --22222222222-------");
		plat.setPlatformActionListener(this);
		plat.SSOSetting(true);
		plat.showUser(null);
		plat.getDb().putExpiresIn(15*24*3600);
	}
	
	public void onComplete(Platform platform, int action,
			HashMap<String, Object> res) {
		if (action == Platform.ACTION_USER_INFOR) {
			UIHandler.sendEmptyMessage(MSG_AUTH_COMPLETE, this);
			nickname = platform.getDb().getUserName();
			openid = platform.getDb().getUserId();
			figureurl = platform.getDb().getUserIcon();

			LogUtil.i("---nickname:" + nickname+"  openid:"+openid);
			login(platform.getName(), platform.getDb().getUserId(), res);
		}
		LogUtil.i(res.toString());
		nickname = platform.getDb().getUserName();
		openid = platform.getDb().getUserId();
		figureurl = platform.getDb().getUserIcon();
		
		LogUtil.i("---nickname:" + nickname+"  openid:"+openid);
		LoginUtils login = new LoginUtils(this,userflag);
		login.setFigureurl(platform.getDb().getUserIcon());
		login.setNickName(platform.getDb().getUserName());
		login.setOpenid(platform.getDb().getUserId());
		
		login.LoginToHvn();
		
	}
	
	public void onError(Platform platform, int action, Throwable t) {
		if (action == Platform.ACTION_USER_INFOR) {
			if (t.toString().contains("ClientNotExistException")){
				UIHandler.sendEmptyMessage(MSG_CLIENT_ERROR, this);
			}else{
				UIHandler.sendEmptyMessage(MSG_AUTH_ERROR, this);
			}
		}
		t.printStackTrace();
	}
	
	public void onCancel(Platform platform, int action) {
		if (action == Platform.ACTION_USER_INFOR) {
			UIHandler.sendEmptyMessage(MSG_AUTH_CANCEL, this);
		}
	}
	
	private void login(String plat, String userId, HashMap<String, Object> userInfo) {
		Message msg = new Message();
		msg.what = MSG_LOGIN;
		msg.obj = plat;
		UIHandler.sendMessage(msg, this);
	}
	
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
			case MSG_USERID_FOUND: {
				Toast.makeText(this, R.string.userid_found, Toast.LENGTH_SHORT).show();
				LoginUtils login = new LoginUtils(this,userflag);
				login.setFigureurl(figureurl);
				login.setNickName(nickname);
				login.setOpenid(openid);
				
				login.LoginToHvn();
			}
			break;
			case MSG_LOGIN: {
			//	String text = getString(R.string.logining, msg.obj);
			//	Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
				System.out.println("---------------");
			//	pd.dismiss();
			}
			break;
			case MSG_AUTH_CANCEL: {
				Toast.makeText(this, R.string.auth_cancel, Toast.LENGTH_SHORT).show();
				System.out.println("-------MSG_AUTH_CANCEL--------");
				pd.dismiss();
			}
			break;
			case MSG_AUTH_ERROR: {
				Toast.makeText(this, R.string.auth_error, Toast.LENGTH_SHORT).show();
				System.out.println("-------MSG_AUTH_ERROR--------");
				pd.dismiss();
			}
			break;
			case MSG_AUTH_COMPLETE: {
				Toast.makeText(this, R.string.auth_complete, Toast.LENGTH_SHORT).show();
				System.out.println("--------MSG_AUTH_COMPLETE-------");
			}
			break;
			case MSG_CLIENT_ERROR:
				Toast.makeText(this, R.string.client_error, Toast.LENGTH_SHORT).show();
				System.out.println("-------MSG_CLIENT_ERROR--------");
				pd.dismiss();
				break;
		}
		return false;
	}
}

