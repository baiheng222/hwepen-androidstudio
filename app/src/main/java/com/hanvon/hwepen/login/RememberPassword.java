package com.hanvon.hwepen.login;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.hanvon.hwepen.MainActivity;
import com.hanvon.hwepen.R;
import com.hanvon.application.HanvonApplication;
import com.hanvon.net.JsonData;
import com.hanvon.net.RequestResult;
import com.hanvon.net.RequestServerData;
import com.hanvon.util.ClearEditText;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.LogUtil;
import com.hanvon.util.HttpsClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class RememberPassword extends Activity implements OnClickListener{

	private ClearEditText CEgetUser;
	private Button BTensure;
	
	private String strUserCode;
	private int RmbFlag; //0 - 不合法  1 手机找回  2 邮箱找回
	
	private ImageView Ivback;
	
	private ProgressDialog pd;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.remember_pwd);

		CEgetUser = (ClearEditText)findViewById(R.id.rmb_getuser);
		BTensure = (Button)findViewById(R.id.rmb_reset_ensure);
        Ivback = (ImageView)findViewById(R.id.rmbpwd_back);
        
		CEgetUser.setOnClickListener(this);
		BTensure.setOnClickListener(this);
		Ivback.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		    case R.id.rmb_reset_ensure:
		    	strUserCode = CEgetUser.getText().toString();
		    	if(strUserCode != null){
		    		Pattern p = Pattern.compile("[1][358]+\\d{9}");
	                Matcher m = p.matcher(strUserCode);
	                if(m.matches() ){
	                	//说明输入的不是手机号码，而是邮箱
	                	RmbFlag = 1;
	                }
	                if (RmbFlag != 1){
	                	p = Pattern.compile("^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$");
	    			    m = p.matcher(strUserCode);
	    			    if(m.matches() ){
	    			    	//说明输入的是邮箱
	    			    	RmbFlag = 2;
	    			    }
	                }
		    	}
		    	if (RmbFlag == 0){
		    		Toast.makeText(RememberPassword.this,"输入的邮箱或者手机号码不合法!", Toast.LENGTH_SHORT).show();
		    		return;
		    	}else if (RmbFlag == 1){
		    		pd = ProgressDialog.show(RememberPassword.this, "", "正在验证手机号....");
		    		new RequestTask(1).execute();
		    	}else if (RmbFlag == 2){
		    		pd = ProgressDialog.show(RememberPassword.this, "", "正在验证邮箱....");
			        sendCodeToEmail();
		    	}
			    break;
		    case R.id.rmbpwd_back:
		    	startActivity(new Intent(RememberPassword.this, LoginActivity.class));
    		    RememberPassword.this.finish();
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
				if (flagTask == 1){
				    result = sendCodeToUser();
				}else{
					result = sendEmailToUser();
				}
				return result;
			}
			 //响应结果
		    protected void onPostExecute(RequestResult result) {
		    	if (result == null){
				    return;
		    	}
		    	int status = 0;
		        JsonData data = result.getData();
		        String jsonCode= data.getJson();
		        pd.dismiss();
		        try {
				    JSONObject json=new JSONObject(jsonCode);
				    LogUtil.i(json.toString());
				    if (json.equals(null)){
				    	return;
				    }
			    	status = parseJsonFromRequest(json);
			    	if (flagTask == 1){
			       	    if (status == 0){
			    		    Toast.makeText(RememberPassword.this,"验证码已发送至手机，请查收!", Toast.LENGTH_SHORT).show();
			    		    Intent intent = new Intent();
			    		    intent.putExtra("phone", strUserCode);
			    		    intent.setClass(RememberPassword.this, RmbPwdCheckCode.class);  
			    	        startActivity(intent);
			                finish();
					}
			    	}else{
			    		if (status == 0){
			    		    Toast.makeText(RememberPassword.this,"密码重置邮件已发送至邮箱，请查收!", Toast.LENGTH_SHORT).show();
			    		    startActivity(new Intent(RememberPassword.this, MainActivity.class));
			    		    RememberPassword.this.finish();
			    		}
			    	}
			    } catch (JSONException e) {
				    e.printStackTrace();
			    }
		    }
	    }

		public RequestResult sendCodeToUser(){
			JSONObject JSuserInfoJson = new JSONObject();
	  	    try {
	  	    	JSuserInfoJson.put("uid", HanvonApplication.AppUid);
	  	    	JSuserInfoJson.put("sid", HanvonApplication.AppSid);
	  	  	    JSuserInfoJson.put("ver", HanvonApplication.AppVer);
	  	  	//    JSuserInfoJson.put("user", strUserCode);
	  	  	    JSuserInfoJson.put("phone", strUserCode);
	  	    } catch (JSONException e) {
	  		    e.printStackTrace();
	  	    }

	  	    LogUtil.i(JSuserInfoJson.toString());
	  	    RequestResult result=new RequestResult();
	  //	    result=RequestServerData.getauthcodeForRmbPasswd(JSuserInfoJson);
	  	  result=RequestServerData.getphoneauthcode(JSuserInfoJson);

	  	    return result;
	    }

		public RequestResult sendEmailToUser(){
			JSONObject JSuserInfoJson = new JSONObject();
	  	    try {
	  	    //	JSuserInfoJson.put("uid", HanvonApplication.AppUid);
	  	    //	JSuserInfoJson.put("sid", HanvonApplication.AppSid);
	  	  	//    JSuserInfoJson.put("ver", HanvonApplication.AppVer);
	  	  	    JSuserInfoJson.put("user", strUserCode);
	  	  	    JSuserInfoJson.put("email", strUserCode);
	  	    } catch (JSONException e) {
	  		    e.printStackTrace();
	  	    }

	  	    LogUtil.i(JSuserInfoJson.toString());
	  	    RequestResult result=new RequestResult();
	  //	    result=RequestServerData.getauthcodeForRmbPasswd(JSuserInfoJson);
	  	  result=RequestServerData.getEmailToUser(JSuserInfoJson);

	  	    return result;
	    }
		public int parseJsonFromRequest(JSONObject json) throws NumberFormatException, JSONException{
			int status = 0;
			status = Integer.valueOf(json.get("code").toString()).intValue();
			if (status == 0){
			}else if (status == 421){
				Toast.makeText(RememberPassword.this,"请求存在非法输入项!", Toast.LENGTH_SHORT).show();
			}else if (status == 425){
				Toast.makeText(RememberPassword.this,"验证失败,请输入注册时的电话号码 !", Toast.LENGTH_SHORT).show();
			}else if (status == 428){
				Toast.makeText(RememberPassword.this,"匹配成功,但发送验证码失败!", Toast.LENGTH_SHORT).show();
			}else if (status == 520){
				Toast.makeText(RememberPassword.this,"服务器异常，请稍后再试!", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(RememberPassword.this,"发送失败，请重试!", Toast.LENGTH_SHORT).show();
			}

			return status;
		}

		
		public void sendCodeToEmail() {

			   final List<NameValuePair> parameters=new ArrayList<NameValuePair>();
		       parameters.add(new BasicNameValuePair("userName", strUserCode));
			   parameters.add(new BasicNameValuePair("email", strUserCode));
				final String url = "https://account.hw99.com/user/checkUserNameEmail.action";
				final int result = 0;
				new Thread() {
					@Override
					public void run() {
						HttpsClient httpsClient = new HttpsClient();
						String result = httpsClient.HttpsRequest(url, parameters);
						LogUtil.i(result);

						if (result.equals("right")){
							Message msg = new Message();
		                    msg.what = 0; 
		                    handler.sendMessage(msg); 
						}else{
							Message msg = new Message();
		                    msg.what = 1;
		                    handler.sendMessage(msg);	
						}
					}
				}.start();
			}
			
			Handler handler=new Handler(){
				public void handleMessage(Message msg) {
					pd.dismiss();
				    switch (msg.what) {
			        case 0:  
			        	LogUtil.i("------------------");
						Toast.makeText(RememberPassword.this,"重置密码指令已发送至邮箱，请查收!", Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(RememberPassword.this, LoginActivity.class);
						RememberPassword.this.startActivity(intent);
						RememberPassword.this.finish();
			            break;
			        case 1:
			        	Toast.makeText(getApplicationContext(),"密码找回错误，请重新输入！", Toast.LENGTH_SHORT).show();
			            break; 
			        }
				};
			};
}
