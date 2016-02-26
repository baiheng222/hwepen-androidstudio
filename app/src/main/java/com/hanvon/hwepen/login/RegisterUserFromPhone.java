package com.hanvon.hwepen.login;

import org.json.JSONException;
import org.json.JSONObject;

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

public class RegisterUserFromPhone extends Activity implements OnClickListener{

	private TextView TVregistPhone;
	private ClearEditText CEauthCode;
	private Button BTensure;
	private Button BTtime;
	private ImageView IVback;

	private String strAuthCode;
	private String strPassword;
	private String strPhoneNumber;
	
	private ProgressDialog pd;
	JSONObject JSuserInfoJson;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.rgst_user_second);

		TVregistPhone = (TextView)findViewById(R.id.rgst_getphone);
		CEauthCode = (ClearEditText)findViewById(R.id.regist_user_getcode);
		BTensure = (Button)findViewById(R.id.rgst_getcode_ensure);
	//	BTtime = (Button)findViewById(R.id.rgst_getcode_time);
        IVback = (ImageView)findViewById(R.id.rgst_back);
		
        CEauthCode.setOnClickListener(this);
        BTensure.setOnClickListener(this);
     //   BTtime.setOnClickListener(this);
        IVback.setOnClickListener(this);
        
        Intent intent = getIntent();
		if (intent != null){
			strPassword = intent.getStringExtra("password");
			strPhoneNumber = intent.getStringExtra("phone");
		}
		TVregistPhone.setText(strPhoneNumber);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		    case R.id.rgst_getcode_ensure:
		    	strAuthCode = CEauthCode.getText().toString();
		    	if (!strAuthCode.equals("")){
		    		if (new ConnectionDetector(RegisterUserFromPhone.this).isConnectingTOInternet()) {
			    		pd = ProgressDialog.show(RegisterUserFromPhone.this, "", "正在进行注册......");
						new RequestTask(1).execute();
					} else {
						Toast.makeText(RegisterUserFromPhone.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
					}
		    	}
			    break;
		  //  case R.id.rgst_getcode_time:
			//    break;
		    case R.id.rgst_back:
		    	startActivity(new Intent(RegisterUserFromPhone.this, LoginActivity.class));
		    	this.finish();
			    break;
		    default:
			    break;
		}
	}
	
	
	@SuppressLint("NewApi") class RequestTask  extends AsyncTask<Void, Void, RequestResult>{
    	int flagTask;
        public RequestTask(int flagTask){
    	  this.flagTask=flagTask;
        }
  	  @TargetApi(Build.VERSION_CODES.CUPCAKE) @Override
	    protected void onPreExecute() {
	    	super.onPreExecute();
	    }
		@Override
		protected RequestResult doInBackground(Void... arg0) {
			RequestResult result=null;
			LogUtil.i("INTO RequestTask:doInBackground,and flag = " + flagTask);
			if(flagTask==1){
				result = CheckAuthCodeToServer();
			} else if(flagTask == 2) {
				result = registerApi();
			}
			return result;
		}
		 //响应结果
	    @SuppressLint("NewApi") protected void onPostExecute(RequestResult result) {
	    	if (result == null){
	    		pd.dismiss();
			    Toast.makeText(RegisterUserFromPhone.this, "检查失败！", Toast.LENGTH_SHORT).show();
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
				    }else if (json.get("code").equals("520")){
				    	pd.dismiss();
						Toast.makeText(RegisterUserFromPhone.this,"服务器异常，请稍后再试!", Toast.LENGTH_SHORT).show();
					}else if (json.get("code").equals("425")){
						pd.dismiss();
						Toast.makeText(RegisterUserFromPhone.this,"验证码已过期，请重新注册!", Toast.LENGTH_SHORT).show();
					}else{
						pd.dismiss();
						Toast.makeText(RegisterUserFromPhone.this,"校验失败，请稍后注册!", Toast.LENGTH_SHORT).show();
					}
			    }else if (flagTask == 2){
			    	if (json.get("code").equals("0")) {
			    		HanvonApplication.isActivity = true;
				    	SharedPreferences mSharedPreferences=getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
						Editor mEditor=	mSharedPreferences.edit();
						mEditor.putString("username", strPhoneNumber);
						HanvonApplication.isActivity = true;
						mEditor.putBoolean("isActivity", HanvonApplication.isActivity);
						mEditor.putString("nickname", "");
						HanvonApplication.hvnName = strPhoneNumber;
						HanvonApplication.strName = "";
					//	mEditor.putString("email", "");
					//	mEditor.putString("phone", strPhoneNumber);
					    mEditor.putInt("flag", 0);
					    mEditor.putInt("status", 1);
					    mEditor.commit();

					    startActivity(new Intent(RegisterUserFromPhone.this, MainActivity.class));
					    RegisterUserFromPhone.this.finish();
					    pd.dismiss();
				    } else if (json.get("code").equals("520")){
				    	pd.dismiss();
					    Toast.makeText(RegisterUserFromPhone.this, "服务器异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				    } else {
				    	pd.dismiss();
					    Toast.makeText(RegisterUserFromPhone.this, "注册失败!", Toast.LENGTH_SHORT).show();
				    }
			    }
		    } catch (JSONException e) {
		    	pd.dismiss();
			    e.printStackTrace();
		    }
	    }
    }
	
	
	public RequestResult CheckAuthCodeToServer(){
		JSONObject JSuserInfoJson = new JSONObject();
	  	try {
	  		JSuserInfoJson.put("uid", HanvonApplication.AppUid);
	  		JSuserInfoJson.put("sid", HanvonApplication.AppSid);
	  	  	JSuserInfoJson.put("ver", HanvonApplication.AppVer);
	  	    JSuserInfoJson.put("phone", strPhoneNumber);
	  	  	JSuserInfoJson.put("authcode", strAuthCode);
	  	}catch (JSONException e) {
	  		e.printStackTrace();
	  	}

	  	LogUtil.i(JSuserInfoJson.toString());
	  	RequestResult result=new RequestResult();
	  	result=RequestServerData.checkphoneauthcode(JSuserInfoJson);

	  	return result;
	}

	public RequestResult  registerApi(){
		LogUtil.i("user:"+strPhoneNumber+"    pwd:"+strPassword);
    	JSuserInfoJson = new JSONObject();
  	    try {
  	    	JSuserInfoJson.put("uid",HanvonApplication.AppUid);
  	    	JSuserInfoJson.put("sid", HanvonApplication.AppSid);
  	    	JSuserInfoJson.put("user", strPhoneNumber);
  	    	JSuserInfoJson.put("pwd", strPassword);
  	    	JSuserInfoJson.put("mobile", strPhoneNumber);
  	    	JSuserInfoJson.put("registeWay","1");
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
	    	startActivity(new Intent(RegisterUserFromPhone.this, LoginActivity.class));
	    	this.finish();
	    }
	    return false; 
	}

}
