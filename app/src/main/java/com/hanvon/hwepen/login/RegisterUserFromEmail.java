package com.hanvon.hwepen.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanvon.hwepen.MainActivity;
import com.hanvon.hwepen.R;
import com.hanvon.application.HanvonApplication;
import com.hanvon.net.JsonData;
import com.hanvon.net.RequestResult;
import com.hanvon.net.RequestServerData;
import com.hanvon.util.ClearEditText;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.LogUtil;

public class RegisterUserFromEmail extends Activity implements OnClickListener{	

	private ClearEditText CEemail;
	private ClearEditText CEpasswd;
	private Button BTensure;
	private ImageView IVback;
	private ImageView IVregisterPhone;

	private String strEmail;
	private String strPassword;
	
	private ProgressDialog pd;
	JSONObject JSuserInfoJson;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register_user_email);

		CEemail = (ClearEditText)findViewById(R.id.rgst_email_user);
		CEpasswd = (ClearEditText)findViewById(R.id.rgst_email_pswd);
		BTensure = (Button)findViewById(R.id.rgst_email_rgstbutton);
        IVback = (ImageView)findViewById(R.id.rgst_back);
        IVregisterPhone = (ImageView)findViewById(R.id.email_register_phone_button);
		
        CEemail.setOnClickListener(this);
        CEpasswd.setOnClickListener(this);
        BTensure.setOnClickListener(this);
        IVback.setOnClickListener(this);
        IVregisterPhone.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		    case R.id.rgst_email_rgstbutton:
		    	strEmail = CEemail.getText().toString();
		    	strPassword = CEpasswd.getText().toString();
		    	
		    	if (strEmail == null || strPassword == null){
		    		Toast.makeText(RegisterUserFromEmail.this, "不允许有空项！", Toast.LENGTH_SHORT).show();
		    		return;
		    	}
		    	Pattern p = Pattern.compile("^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$");
			    Matcher m = p.matcher(strEmail);
			    if(!m.matches() ){
			        Toast.makeText(RegisterUserFromEmail.this,"邮箱地址不合法", Toast.LENGTH_SHORT).show();
			        return;
			    }
			    
			    if ((strPassword.length() < 6) || (strPassword.length() > 16)){
					Toast.makeText(RegisterUserFromEmail.this, "密码应为6-16位字母和数字组合!", Toast.LENGTH_SHORT).show();
					return;
				}else{
					Pattern pN = Pattern.compile("[0-9]{6,16}");
				    Matcher mN = pN.matcher(strPassword);
				    Pattern pS = Pattern.compile("[a-zA-Z]{6,16}");
				    Matcher mS = pS.matcher(strPassword);
				    if((mN.matches()) || (mS.matches())){
				        Toast.makeText(RegisterUserFromEmail.this,"密码应为6-16位字母和数字组合!", Toast.LENGTH_SHORT).show();
				        return;
				    }
				}
			    if (new ConnectionDetector(RegisterUserFromEmail.this).isConnectingTOInternet()) {
		    		pd = ProgressDialog.show(RegisterUserFromEmail.this, "", "正在进行注册......");
					new RequestTask(1).execute();
				} else {
					Toast.makeText(RegisterUserFromEmail.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
				}
			    break;
		    case R.id.rgst_back:
		    	startActivity(new Intent(RegisterUserFromEmail.this, LoginActivity.class));
		    	this.finish();
			    break;
		    case R.id.email_register_phone_button:
		    	startActivity(new Intent(RegisterUserFromEmail.this, RegisterUserGetCodePhone.class));
		    	this.finish();
		    	break;
		    default:
			    break;
		}
	}
	
	class RequestTask  extends AsyncTask<Void, Void, RequestResult>{
    	int flagTask;
        public RequestTask(int flagTask){
    	  this.flagTask=flagTask;
        }
  	  @Override
	    protected void onPreExecute() {
	    	super.onPreExecute();
	    }
		@Override
		protected RequestResult doInBackground(Void... arg0) {
			RequestResult result=null;
			LogUtil.i("INTO RequestTask:doInBackground,and flag = " + flagTask);
			if(flagTask==1){
				result = CheckSameName();
			} else if(flagTask == 3) {
				result = SendCodetoEmail();
			} else if(flagTask == 2) {
			    result = RigestUserApi();
		    }
			return result;
		}
		 //响应结果
	    @SuppressLint("NewApi") protected void onPostExecute(RequestResult result) {
	    	if (result == null){
	    		pd.dismiss();
			    Toast.makeText(RegisterUserFromEmail.this, "检查失败！", Toast.LENGTH_SHORT).show();
			    return;
	    	}
	    	
	        JsonData data = result.getData();
	        String jsonCode= data.getJson();
	        try {
			    JSONObject json=new JSONObject(jsonCode);
			    LogUtil.i(json.toString());
			    if (flagTask == 1){
				    if (json.get("code").equals("0")) {
					    new RequestTask(2).execute();
				    } else if (json.get("code").equals("422")){
				    	pd.dismiss();
					    Toast.makeText(RegisterUserFromEmail.this, "该邮箱已被注册，请直接登录!", Toast.LENGTH_SHORT).show();
				    } else if (json.get("code").equals("520")){
				    	pd.dismiss();
					    Toast.makeText(RegisterUserFromEmail.this, "服务器异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				    } else {
				    	pd.dismiss();
					    Toast.makeText(RegisterUserFromEmail.this, "用户名重名检查失败!", Toast.LENGTH_SHORT).show();
				    }
			    }else if (flagTask == 2){
			    	if (json.get("code").equals("0")) {
					    new RequestTask(3).execute();
				    }else{
				    	pd.dismiss();
					    Toast.makeText(RegisterUserFromEmail.this, "用户注册失败,请稍后重试!", Toast.LENGTH_SHORT).show();
				    }
			    }else if (flagTask == 3){
			    	if (json.get("code").equals("0")) {
				    	SharedPreferences mSharedPreferences=getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
						Editor mEditor=	mSharedPreferences.edit();
						mEditor.putString("nickname", "");
						mEditor.putString("username", strEmail);
						HanvonApplication.isActivity = false;
						mEditor.putBoolean("isActivity", HanvonApplication.isActivity);
					//	mEditor.putString("email", strEmail);
					//	mEditor.putString("phone", "");
						HanvonApplication.hvnName = strEmail;
						HanvonApplication.strName = "";
					    mEditor.putInt("flag", 0);
					    mEditor.putInt("status", 1);
					    mEditor.commit();

					    startActivity(new Intent(RegisterUserFromEmail.this, MainActivity.class));
					    RegisterUserFromEmail.this.finish();
					    pd.dismiss();
					    Toast.makeText(RegisterUserFromEmail.this, "注册成功，请进入邮箱激活!", Toast.LENGTH_LONG).show();
				    } else if (json.get("code").equals("520")){
				    	pd.dismiss();
					    Toast.makeText(RegisterUserFromEmail.this, "服务器异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				    } else {
				    	pd.dismiss();
					    Toast.makeText(RegisterUserFromEmail.this, "登录失败!", Toast.LENGTH_SHORT).show();
				    }
			    }
		    } catch (JSONException e) {
		    	pd.dismiss();
			    e.printStackTrace();
		    }
	    }
    }

	 public RequestResult CheckSameName(){
	  	    JSONObject paramJson=new JSONObject();
	  	    try {
	  	    	paramJson.put("uid",HanvonApplication.AppUid);
	  	    	paramJson.put("sid", HanvonApplication.AppSid);
			    paramJson.put("data", strEmail);
	  	    } catch (JSONException e) {
	  		    e.printStackTrace();
	  	    }

	  	    LogUtil.i(paramJson.toString());
	  	    RequestResult result=new RequestResult();
	 	    result=RequestServerData.checkName(paramJson);
	  	    return result;
	}
	 
	 public RequestResult SendCodetoEmail(){
			JSONObject JSuserInfoJson = new JSONObject();
		  	try {
		  	//	JSuserInfoJson.put("uid", HanvonApplication.AppUid);
		  	//	JSuserInfoJson.put("sid", HanvonApplication.AppSid);
		  	 // 	JSuserInfoJson.put("ver", HanvonApplication.AppVer);
		  		JSuserInfoJson.put("user", strEmail);
		  	}catch (JSONException e) {
		  		e.printStackTrace();
		  	}

		  	LogUtil.i(JSuserInfoJson.toString());
		  	RequestResult result=new RequestResult();
		  	result=RequestServerData.getActivityEmail(JSuserInfoJson);
		  	return result;
		}
	 
	 public RequestResult  RigestUserApi(){

	    	JSONObject JSuserInfoJson = new JSONObject();
	    	JSONObject userinfo = new JSONObject();
	  	    try {
	  	    	JSuserInfoJson.put("uid", HanvonApplication.AppUid);
	  	    	JSuserInfoJson.put("sid", HanvonApplication.AppSid);
	  	    	JSuserInfoJson.put("user", strEmail);
	  	    	JSuserInfoJson.put("pwd", strPassword);
	  	    	JSuserInfoJson.put("email", strEmail);
	  	    	JSuserInfoJson.put("registeWay","0");
	  	    } catch (JSONException e) {
	  		    e.printStackTrace();
	  	    }

	  	    LogUtil.i(JSuserInfoJson.toString());
	  	    RequestResult result=new RequestResult();
	  	    result=RequestServerData.userRegister(JSuserInfoJson);
	  	    return result;
	    }
	 
	   @Override  
		public boolean onKeyDown(int keyCode, KeyEvent event)
		{
		    if (keyCode == KeyEvent.KEYCODE_BACK )
		    {
		    	startActivity(new Intent(RegisterUserFromEmail.this, LoginActivity.class));
		    	this.finish();
		    }
		    return false; 
		}

}
