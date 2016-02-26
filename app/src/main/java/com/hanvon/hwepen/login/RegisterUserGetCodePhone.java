package com.hanvon.hwepen.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

//import com.hanvon.hwepen.MainActivity;
import com.hanvon.hwepen.R;
import com.hanvon.application.HanvonApplication;
import com.hanvon.net.JsonData;
import com.hanvon.net.RequestResult;
import com.hanvon.net.RequestServerData;
import com.hanvon.util.ClearEditText;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.LogUtil;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterUserGetCodePhone extends Activity implements OnClickListener{

	private ClearEditText CEphoneNumber;
	private ClearEditText CEpassword;
	private Button BTregist;
	private ImageView IVback;
	private ImageView IVregisterPhone;
	private ImageView IVregisterEmail;
	
	private String strPhoneNumber;
	private String strPassword;
	
	private ProgressDialog pd;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register_user_phone);

		CEphoneNumber = (ClearEditText)findViewById(R.id.rgst_user);
		CEpassword = (ClearEditText)findViewById(R.id.rgst_pswd);
		BTregist = (Button)findViewById(R.id.rgst_rgstbutton);
        IVback = (ImageView)findViewById(R.id.rgst_back);
        IVregisterEmail = (ImageView)findViewById(R.id.register_email_button);
        IVregisterPhone = (ImageView)findViewById(R.id.register_phone_button);
		
        CEphoneNumber.setOnClickListener(this);
        CEpassword.setOnClickListener(this);
        BTregist.setOnClickListener(this);
        IVback.setOnClickListener(this);
        IVregisterEmail.setOnClickListener(this);
        IVregisterPhone.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		    case R.id.rgst_rgstbutton:
		    	strPhoneNumber = CEphoneNumber.getText().toString();
		    	strPassword = CEpassword.getText().toString();
		    	if (strPhoneNumber.equals("") || strPassword.equals("")){
					Toast.makeText(RegisterUserGetCodePhone.this, "手机号和密码不允许为空", Toast.LENGTH_SHORT).show();
					return;
				}
		    	
		    	if ((strPassword.length() < 6) || (strPassword.length() > 16)){
					Toast.makeText(RegisterUserGetCodePhone.this, "密码应为6-16位字母和数字组合!", Toast.LENGTH_SHORT).show();
					return;
				} else {
					Pattern pN = Pattern.compile("[0-9]{6,16}");
				    Matcher mN = pN.matcher(strPassword);
				    Pattern pS = Pattern.compile("[a-zA-Z]{6,16}");
				    Matcher mS = pS.matcher(strPassword);
				    if((mN.matches()) || (mS.matches())){
				        Toast.makeText(RegisterUserGetCodePhone.this,"请输入符合规则的密码!", Toast.LENGTH_SHORT).show();
				        return;
				    }
				}
		    	
		    	if (strPhoneNumber != null){
		            Pattern p = Pattern.compile("[1][358]+\\d{9}");
	                Matcher m = p.matcher(strPhoneNumber);
	                if(!m.matches() ){
	                    Toast.makeText(RegisterUserGetCodePhone.this,"手机号码不合法", Toast.LENGTH_SHORT).show();
	                    return;
	                }
		        }
		    	
		    	if (new ConnectionDetector(RegisterUserGetCodePhone.this).isConnectingTOInternet()) {
		    		pd = ProgressDialog.show(RegisterUserGetCodePhone.this, "", "正在进行手机号检查......");
					new RequestTask(1).execute();
				} else {
					Toast.makeText(RegisterUserGetCodePhone.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
				}
			    break;
		    case R.id.register_email_button:
		    	Intent intent = new Intent(RegisterUserGetCodePhone.this, RegisterUserFromEmail.class);
		    	this.startActivity(intent);
		    	this.finish();
		    	break;
		    case R.id.rgst_back:
		    	startActivity(new Intent(RegisterUserGetCodePhone.this, LoginActivity.class));
		    	this.finish();
		    	break;
		    default:
		    	break;
		}
	}
	
	
	@TargetApi(Build.VERSION_CODES.CUPCAKE) @SuppressLint("NewApi") class RequestTask  extends AsyncTask<Void, Void, RequestResult>{
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
			} else if(flagTask == 2) {
				result = SendAuthCode();
			}
			return result;
		}
		 //响应结果
	    @SuppressLint("NewApi") protected void onPostExecute(RequestResult result) {
	    	if (result == null){
	    		pd.dismiss();
			    Toast.makeText(RegisterUserGetCodePhone.this, "检查失败！", Toast.LENGTH_SHORT).show();
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
					    Toast.makeText(RegisterUserGetCodePhone.this, "该手机号已被注册，请直接登录!", Toast.LENGTH_SHORT).show();
				    } else if (json.get("code").equals("520")){
				    	pd.dismiss();
					    Toast.makeText(RegisterUserGetCodePhone.this, "服务器异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				    } else {
				    	pd.dismiss();
					    Toast.makeText(RegisterUserGetCodePhone.this, "用户名重名检查失败!", Toast.LENGTH_SHORT).show();
				    }
			    }else if (flagTask == 2){
				    if (json.get("code").equals("0")) {
				    	pd.dismiss();
				    	Intent intent = new Intent();
						intent.putExtra("phone", strPhoneNumber);
						intent.putExtra("password", strPassword);
						intent.setClass(RegisterUserGetCodePhone.this, RegisterUserFromPhone.class); 
						RegisterUserGetCodePhone.this.startActivity(intent);
				    	RegisterUserGetCodePhone.this.finish();
				    } else if (json.get("code").equals("520")){
				    	pd.dismiss();
					    Toast.makeText(RegisterUserGetCodePhone.this, "服务器异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				    } else {
				    	pd.dismiss();
					    Toast.makeText(RegisterUserGetCodePhone.this, "注册失败!", Toast.LENGTH_SHORT).show();
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
			    paramJson.put("data", strPhoneNumber);
	  	    } catch (JSONException e) {
	  		    e.printStackTrace();
	  	    }

	  	    LogUtil.i(paramJson.toString());
	  	    RequestResult result=new RequestResult();
	 	    result=RequestServerData.checkName(paramJson);
	  	    return result;
	}
	 
	 public RequestResult SendAuthCode(){
			JSONObject JSuserInfoJson = new JSONObject();
		  	try {
		  		JSuserInfoJson.put("uid", HanvonApplication.AppUid);
		  		JSuserInfoJson.put("sid", HanvonApplication.AppSid);
		  	  	JSuserInfoJson.put("ver", HanvonApplication.AppVer);
		  	  	JSuserInfoJson.put("phone", strPhoneNumber);
		  	}catch (JSONException e) {
		  		e.printStackTrace();
		  	}

		  	LogUtil.i(JSuserInfoJson.toString());
		  	RequestResult result=new RequestResult();
		  	result=RequestServerData.getphoneauthcode(JSuserInfoJson);

		  	return result;
		}
	 
		@Override  
		public boolean onKeyDown(int keyCode, KeyEvent event)
		{
		    if (keyCode == KeyEvent.KEYCODE_BACK )
		    {
		    	startActivity(new Intent(RegisterUserGetCodePhone.this, LoginActivity.class));
		    	this.finish();
		    }
		    return false; 
		}
}

