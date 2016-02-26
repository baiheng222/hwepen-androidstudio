package com.hanvon.hwepen.login;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyUserName extends Activity implements OnClickListener{

	private ClearEditText CEnickName;
	private String strNickName;
	
	private String strOldNickname;
	private String strUserName;
	private TextView TVcommit;
	private ImageView IVback;

	private ProgressDialog pd;

	@SuppressLint("InlinedApi") protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.modify_name);
		
		CEnickName = (ClearEditText)findViewById(R.id.modify_username);
		TVcommit = (TextView)findViewById(R.id.modify_username_ensure);
		IVback =(ImageView)findViewById(R.id.modify_name_back);

		CEnickName.setOnClickListener(this);
		TVcommit.setOnClickListener(this);
		IVback.setOnClickListener(this);
		
		SharedPreferences mSharedPreferences=getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
		strUserName = mSharedPreferences.getString("username", "");
		strOldNickname = mSharedPreferences.getString("nickname", "");
		CEnickName.setText(strOldNickname);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		    case R.id.modify_username_ensure:
		    	strNickName = CEnickName.getText().toString();
		    	if(strOldNickname.equals(strNickName)){
		    		return;
		    	}
		    	if (strNickName.length() < 3 || strNickName.length() >= 20){
		    		Toast.makeText(ModifyUserName.this, "昵称至少应为3个字母且最大为20个字母!", Toast.LENGTH_SHORT).show();
					return;
		    	}
		    	if (new ConnectionDetector(ModifyUserName.this).isConnectingTOInternet()) {
		    		pd = ProgressDialog.show(ModifyUserName.this, "", "正在加载....");
		    		new RequestTask().execute();
				} else {
					Toast.makeText(ModifyUserName.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
				}
		    	break;
		    case R.id.modify_name_back:
		    	startActivity(new Intent(ModifyUserName.this, ShowUserMessage.class));
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
			result = modifyNickName();
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
					SharedPreferences mSharedPreferences=getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
					Editor mEditor=	mSharedPreferences.edit();
					mEditor.putString("nickname", strNickName);
					HanvonApplication.strName = strNickName;
					mEditor.commit();

					startActivity(new Intent(ModifyUserName.this, ShowUserMessage.class));
					ModifyUserName.this.finish();
				}else if (status == 520){
					Toast.makeText(ModifyUserName.this,"服务器异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(ModifyUserName.this,"发送失败，请稍后重试!", Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

    public RequestResult modifyNickName(){
		JSONObject JSuserInfoJson = new JSONObject();
	  	try {
	  		JSuserInfoJson.put("uid",HanvonApplication.AppUid);
	  		JSuserInfoJson.put("sid", HanvonApplication.AppSid);
	  	  	JSuserInfoJson.put("ver", "");
	  	  	JSuserInfoJson.put("user", strUserName);
	  	    JSuserInfoJson.put("nickname", strNickName);
	  	}catch (JSONException e) {
	  		e.printStackTrace();
	  	}

	  	LogUtil.i(JSuserInfoJson.toString());
	  	RequestResult result=new RequestResult();
	  	result=RequestServerData.modifyNickname(JSuserInfoJson);

	  	return result;
	}

    @Override  
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK )
	    {
	    	startActivity(new Intent(ModifyUserName.this, ShowUserMessage.class));
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

