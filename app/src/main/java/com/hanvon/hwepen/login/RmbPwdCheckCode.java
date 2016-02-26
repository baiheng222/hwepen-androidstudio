package com.hanvon.hwepen.login;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanvon.hwepen.R;
import com.hanvon.application.HanvonApplication;
import com.hanvon.net.JsonData;
import com.hanvon.net.RequestResult;
import com.hanvon.net.RequestServerData;
import com.hanvon.util.ClearEditText;
import com.hanvon.util.LogUtil;

public class RmbPwdCheckCode extends Activity implements OnClickListener{
	
	private TextView TVgetPhone;
	private ClearEditText CEauthCode;
	private TextView TVensure;
	private ImageView IVback;
	
	private String strPhone;
	private String strAuthCode;

	ProgressDialog pd;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.rmbpwd_second);

		TVgetPhone = (TextView)findViewById(R.id.rmb_secode_getphone);
		CEauthCode = (ClearEditText)findViewById(R.id.rmb_secode_getcode);
		TVensure = (TextView)findViewById(R.id.rmb_secode_ensure);
		IVback = (ImageView)findViewById(R.id.rmbpwd_back);

		TVgetPhone.setOnClickListener(this);
		CEauthCode.setOnClickListener(this);
		TVensure.setOnClickListener(this);
		IVback.setOnClickListener(this);
		
		Intent intent = getIntent();
		if (intent != null){
			strPhone = intent.getStringExtra("phone");
			TVgetPhone.setText(strPhone);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()){
		    case R.id.rmb_secode_ensure:
		    	strAuthCode = CEauthCode.getText().toString();
		    	if (strAuthCode != null){
		    		pd = ProgressDialog.show(RmbPwdCheckCode.this, "", "正在进行校验码检查......");
					new RequestTask(1).execute();
		    	}
		    	break;
		    case R.id.rmbpwd_back:
		    	startActivity(new Intent(RmbPwdCheckCode.this, LoginActivity.class));
		    	this.finish();
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
			// = registerApi();
			}
			return result;
		}
		 //响应结果
	    @SuppressLint("NewApi") protected void onPostExecute(RequestResult result) {
	    	if (result == null){
	    		pd.dismiss();
			    Toast.makeText(RmbPwdCheckCode.this, "检查失败！", Toast.LENGTH_SHORT).show();
			    return;
	    	}
	        JsonData data = result.getData();
	        String jsonCode= data.getJson();
	        try {
			    JSONObject json=new JSONObject(jsonCode);
			    LogUtil.i(json.toString());
			    if (flagTask == 1){
				    if (json.get("code").equals("0")) {
				    	Intent intent = new Intent();
						intent.putExtra("authcode", strAuthCode);
						intent.putExtra("username", strPhone);
						intent.setClass(RmbPwdCheckCode.this, ResetPasswd.class); 
						RmbPwdCheckCode.this.startActivity(intent);
						RmbPwdCheckCode.this.finish();
				    }else if (json.get("code").equals("520")){
				    	pd.dismiss();
						Toast.makeText(RmbPwdCheckCode.this,"服务器异常，请稍后再试!", Toast.LENGTH_SHORT).show();
					}else if (json.get("code").equals("425")){
						pd.dismiss();
						Toast.makeText(RmbPwdCheckCode.this,"验证码已过期，请重新注册!", Toast.LENGTH_SHORT).show();
					}else{
						pd.dismiss();
						Toast.makeText(RmbPwdCheckCode.this,"校验失败，请稍后注册!", Toast.LENGTH_SHORT).show();
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
	  	    JSuserInfoJson.put("phone", strPhone);
	  	  	JSuserInfoJson.put("authcode", strAuthCode);
	  	}catch (JSONException e) {
	  		e.printStackTrace();
	  	}

	  	LogUtil.i(JSuserInfoJson.toString());
	  	RequestResult result=new RequestResult();
	  	result=RequestServerData.checkphoneauthcode(JSuserInfoJson);

	  	return result;
	}
	
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if(keyCode == KeyEvent.KEYCODE_BACK )
	    {
	    	startActivity(new Intent(RmbPwdCheckCode.this, LoginActivity.class));
	    	this.finish();
	    }
	    return false; 
	}
	
}
