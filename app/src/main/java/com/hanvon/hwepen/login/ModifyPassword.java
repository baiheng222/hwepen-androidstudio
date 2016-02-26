package com.hanvon.hwepen.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.hanvon.hwepen.R;
import com.hanvon.application.HanvonApplication;
import com.hanvon.net.JsonData;
import com.hanvon.net.RequestResult;
import com.hanvon.net.RequestServerData;
import com.hanvon.util.ClearEditText;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.LogUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyPassword extends Activity implements OnClickListener{

	private String strOldPasswd;
	
	private ClearEditText CEnewPasswd;
	private String strNewPasswd;
	
	private ClearEditText CErenewPasswd;
	private String strRenewPasswd;

	private String strUserName;
	private ImageView IVback;

	private TextView TVensure;
	private ProgressDialog pd;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.modify_password);
		
		CEnewPasswd = (ClearEditText)findViewById(R.id.modifypwd_newpwd);
		CErenewPasswd = (ClearEditText)findViewById(R.id.modifypwd_ensurenewpwd);
		TVensure = (TextView)findViewById(R.id.modifypwd_ensure);
		IVback = (ImageView)findViewById(R.id.modify_pwd_back);
		
		CEnewPasswd.setOnClickListener(this);
		CErenewPasswd.setOnClickListener(this);
		TVensure.setOnClickListener(this);
		IVback.setOnClickListener(this);
		
		SharedPreferences mSharedPreferences=getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
		strUserName = mSharedPreferences.getString("username", "");
		strOldPasswd = mSharedPreferences.getString("passwd", "");
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		    case R.id.modifypwd_ensure:
		    	strNewPasswd = CEnewPasswd.getText().toString();
		    	strRenewPasswd = CErenewPasswd.getText().toString();
		    	
		    	if (strOldPasswd == null || strNewPasswd == null || strRenewPasswd == null){
		    		Toast.makeText(ModifyPassword.this, "请输入密码!", Toast.LENGTH_SHORT).show();
		    		return;
		    	}
		    	if (!strNewPasswd.equals(strRenewPasswd)){
		    		Toast.makeText(ModifyPassword.this, "两次输入的密码不一致，请重新输入!", Toast.LENGTH_SHORT).show();
		    		return;
		    	}
		    	if (strOldPasswd.equals(strNewPasswd)){
		    		Toast.makeText(ModifyPassword.this, "新旧密码一样，请重新输入!", Toast.LENGTH_SHORT).show();
		    		return;
		    	}
		    	
		    	if ((strNewPasswd.length() < 6) || (strNewPasswd.length() > 16)){
					Toast.makeText(ModifyPassword.this, "密码应为6-16位字母和数字组合!", Toast.LENGTH_SHORT).show();
					return;
				} else{
					Pattern pN = Pattern.compile("[0-9]{6,16}");
				    Matcher mN = pN.matcher(strNewPasswd);
				    Pattern pS = Pattern.compile("[a-zA-Z]{6,16}");
				    Matcher mS = pS.matcher(strNewPasswd);
				    if((mN.matches()) || (mS.matches())){
				        Toast.makeText(ModifyPassword.this,"密码应为6-16位字母和数字组合!", Toast.LENGTH_SHORT).show();
				        return;
				    }
				}

		    	if (new ConnectionDetector(ModifyPassword.this).isConnectingTOInternet()) {
		    		pd = ProgressDialog.show(ModifyPassword.this, "", "正在加载....");
		    		new RequestTask().execute();
				} else {
					Toast.makeText(ModifyPassword.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
				}
		    	break;
		    case R.id.modify_pwd_back:
		    	startActivity(new Intent(ModifyPassword.this, ShowUserMessage.class));
		    	this.finish();
		    	break;
		    default:
		    	break;
		}
	}

	class RequestTask  extends AsyncTask<Void, Void, RequestResult>{
	    @Override
		protected void onPreExecute() {
		    super.onPreExecute();
		}
		@Override
		protected RequestResult doInBackground(Void... arg0) {
			RequestResult result=null;
			result = modifyPasswd();
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
				status = Integer.valueOf(json.get("code").toString()).intValue();
				if (status == 0){
					startActivity(new Intent(ModifyPassword.this, LoginActivity.class));
					ModifyPassword.this.finish();
				}else if (status == 520){
					Toast.makeText(ModifyPassword.this,"服务器异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				}else if (status == 425){
					Toast.makeText(ModifyPassword.this,"用户名和旧密码不匹配!", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(ModifyPassword.this,"修改失败，请稍后重试!", Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

    public RequestResult modifyPasswd(){
		JSONObject JSuserInfoJson = new JSONObject();
	  	try {
	  		JSuserInfoJson.put("uid",HanvonApplication.AppUid);
	  		JSuserInfoJson.put("sid", HanvonApplication.AppSid);
	  	  	JSuserInfoJson.put("ver", "");
	  	  	JSuserInfoJson.put("user", strUserName);
	  	    JSuserInfoJson.put("oldpwd", strOldPasswd);
	  	    JSuserInfoJson.put("newpwd", strNewPasswd);
	  	}catch (JSONException e) {
	  		e.printStackTrace();
	  	}

	  	LogUtil.i(JSuserInfoJson.toString());
	  	RequestResult result=new RequestResult();
	  	result=RequestServerData.modifyPassword(JSuserInfoJson);

	  	return result;
	}

    @Override  
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK )
	    {
	    	startActivity(new Intent(ModifyPassword.this, ShowUserMessage.class));
	    	this.finish();
	    }
	    return false; 
	}
	
	@Override
	protected void onDestroy() {
		LogUtil.i("INTO onDestroy!!!!!!!!");
		super.onDestroy();
	}

}

