package com.hanvon.hwepen.login;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

import com.hanvon.hwepen.MainActivity;
import com.hanvon.hwepen.R;
import com.hanvon.application.HanvonApplication;
import com.hanvon.net.JsonData;
import com.hanvon.net.RequestResult;
import com.hanvon.net.RequestServerData;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.LogUtil;
import com.hanvon.util.SHA1Util;
import com.hanvon.util.ThirdBindLogin;

import com.mob.tools.utils.UIHandler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler.Callback;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ThirdBind extends Activity implements Callback, 
OnClickListener, PlatformActionListener   {

	private LinearLayout LLthirdSina;
	private LinearLayout LLthirdweiXin;
	private LinearLayout LLthirdQQ;
	
	
	private TextView TVsinaNickname;
	private TextView TVwxNickname;
	private TextView TVqqNickname;
	private TextView TVback;
	
	private String SinaName = "";
	private String QQname = "";
	private String WXname = "";
	
	private String openid;
	private String nickname;
	
	private ProgressDialog pd;
	private int userflag;
	private int mainuser;
	
	
	private static final int MSG_USERID_FOUND = 1;
	private static final int MSG_LOGIN = 2;
	private static final int MSG_AUTH_CANCEL = 3;
	private static final int MSG_AUTH_ERROR= 4;
	private static final int MSG_AUTH_COMPLETE = 5;
	
	private String unBindOpenId = "";
	public static ThirdBind instance = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.third_bind);
		instance = this;
		
		LLthirdSina = (LinearLayout) findViewById(R.id.third_sinabind);
		LLthirdweiXin = (LinearLayout) findViewById(R.id.third_wxbind);
		LLthirdQQ = (LinearLayout) findViewById(R.id.third_qqbind);
		TVsinaNickname = (TextView) findViewById(R.id.third_sinanickname);
		TVwxNickname = (TextView) findViewById(R.id.third_wxnickname);
		TVqqNickname = (TextView) findViewById(R.id.third_qqnickname);
		TVback = (TextView) findViewById(R.id.third_bind_quit);

		LLthirdSina.setOnClickListener(this);
		LLthirdweiXin.setOnClickListener(this);
		LLthirdQQ.setOnClickListener(this);
		TVsinaNickname.setOnClickListener(this);
		TVwxNickname.setOnClickListener(this);
		TVqqNickname.setOnClickListener(this);
		TVback.setOnClickListener(this);

	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		SharedPreferences mSharedPreferences=getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
		mainuser = mSharedPreferences.getInt("flag", 1);
		LogUtil.i("----------mainuser------------"+mainuser);
		if (mainuser == 1){
			SinaName = mSharedPreferences.getString("wbNickname", "");
			WXname = mSharedPreferences.getString("wxNickname", "");
			TVqqNickname.setText(HanvonApplication.hvnName);
			if (!SinaName.equals("")){
				TVsinaNickname.setText(SinaName);
			}
			if (!WXname.equals("")){
				TVwxNickname.setText(WXname);
			}
		}else if(mainuser == 2){
			SinaName = mSharedPreferences.getString("wbNickname", "");
			QQname = mSharedPreferences.getString("qqNickname", "");
			TVwxNickname.setText(HanvonApplication.hvnName);
            if (!SinaName.equals("")){
			    TVsinaNickname.setText(SinaName);	
			}
			if (!QQname.equals("")){
				TVqqNickname.setText(QQname);
			}
		}else if(mainuser == 3){
			QQname = mSharedPreferences.getString("qqNickname", "");
			WXname = mSharedPreferences.getString("wxNickname", "");
			TVsinaNickname.setText(HanvonApplication.hvnName);
			if (!QQname.equals("")){
				TVqqNickname.setText(QQname);
			}
			if (!WXname.equals("")){
				TVwxNickname.setText(WXname);
			}
		}
		ProgressDialogDimiss();
	}
	
	private void ProgressDialogDimiss(){
		if (pd != null){
			pd.dismiss();
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		    case R.id.third_sinabind:
		    case R.id.third_sinanickname:
		    	Toast.makeText(ThirdBind.this, "该版本暂不支持该功能", Toast.LENGTH_SHORT).show();
		    	//if (mainuser == 3){
		    	///	Toast.makeText(ThirdBind.this, "不允许账户绑定自己!", Toast.LENGTH_SHORT).show();
		    	//	break;
		    	//}
		    	break;
		    	
		    case R.id.third_wxbind:
		    case R.id.third_wxnickname:
		    	if (mainuser == 2){
		    		Toast.makeText(ThirdBind.this, "不允许账户绑定自己!", Toast.LENGTH_SHORT).show();
		    		break;
		    	}
		    	if (WXname.equals("")){
		    		if (new ConnectionDetector(ThirdBind.this).isConnectingTOInternet()) {
		    		}else{
		    			Toast.makeText(ThirdBind.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
		    			return;
		    		}
		    		LogUtil.i("INTO QQUserLogin!!!!!!!!");
		    		userflag = 2;
		    		pd = ProgressDialog.show(this, "", "正在进行绑定，请稍后...");
		    		authorize(new Wechat(this));
		    	}else{
		    		 ShowUnbindView menuWindow = new ShowUnbindView(ThirdBind.this);
		    		 menuWindow.showAtLocation(ThirdBind.this.findViewById(R.id.ll_top), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); 
		    	
		    		 userflag = 2;
		    		 SharedPreferences mSharedPreferences=getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
		    		 unBindOpenId = mSharedPreferences.getString("wxOpenId", "");
		    	}
		    	break;
		    	
		    case R.id.third_qqbind:
		    case R.id.third_qqnickname:
		    	if (mainuser == 1){
		    		Toast.makeText(ThirdBind.this, "不允许账户绑定自己!", Toast.LENGTH_SHORT).show();
		    		break;
		    	}
		    	if (QQname.equals("")){
		    		if (new ConnectionDetector(ThirdBind.this).isConnectingTOInternet()) {
		    		}else{
		    			Toast.makeText(ThirdBind.this, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
		    			return;
		    		}
		    		LogUtil.i("INTO QQUserLogin!!!!!!!!");
		    		userflag = 1;
		    		pd = ProgressDialog.show(this, "", "正在进行绑定，请稍后...");
		    		authorize(new QQ(this));
		    	}else{
		    		ShowUnbindView menuWindow = new ShowUnbindView(ThirdBind.this);
		    		 menuWindow.showAtLocation(ThirdBind.this.findViewById(R.id.ll_top), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
		    	
		    		 userflag = 1;
		    		 SharedPreferences mSharedPreferences=getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
		    		 unBindOpenId = mSharedPreferences.getString("qqOpenId", "");
		    	}
		    	break;
		    case R.id.third_bind_quit:
		    	Intent intent = new Intent(ThirdBind.this, MainActivity.class);
		    	ThirdBind.this.startActivity(intent);
		    	ThirdBind.this.finish();
                break;
		}
	}

	public class ShowUnbindView extends PopupWindow {
	    public ShowUnbindView(Context mContext){
	    	super(mContext);
	    //	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  

			View view = View.inflate(mContext,R.layout.third_unbind, null);

			setWidth(LayoutParams.MATCH_PARENT);
			setHeight(LayoutParams.MATCH_PARENT);
			setFocusable(true);
			setOutsideTouchable(true);
			setContentView(view);
	//		showAtLocation(parent, Gravity.BOTTOM, 0, 0);
			update();

			Button bt1 = (Button) view.findViewById(R.id.third_unbind);
			Button bt2 = (Button) view.findViewById(R.id.third_cancelunbind);
			bt1.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					new RequestTask().execute();
					dismiss();
				}
			});
			bt2.setOnClickListener(new OnClickListener() {
		
				public void onClick(View v) {
					LogUtil.i("------cancel unbind--------------");
					dismiss();
				}
			});
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
			result = ThirdUnbind();
			return result;
		}
		 //响应结果
	    protected void onPostExecute(RequestResult result) {
	        JsonData data = result.getData();
	        String jsonCode= data.getJson();
	        try {
			    JSONObject json=new JSONObject(jsonCode);
			    LogUtil.i("-----json:"+json.toString());
			    
			    if (json.getString("code").equals("0")){
				    Toast.makeText(ThirdBind.this, "解除绑定成功!", Toast.LENGTH_SHORT).show();
				    SharedPreferences mSharedPreferences = getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
		            Editor mEditor=	mSharedPreferences.edit();
				    if (userflag == 1){
				    	mEditor.putString("qqOpenId", "");
				    	mEditor.putString("qqNickname", "");
				    	TVqqNickname.setText(R.string.third_unbind);
				    	Platform QQplat = ShareSDK.getPlatform(ThirdBind.this, QQ.NAME);
						LogUtil.i("---quit:---"+QQplat);
						if (QQplat.isValid ()) {
							QQplat.removeAccount();
						}
						QQname = "";
				    }else if (userflag == 2){
				    	mEditor.putString("wxOpenId", "");
				    	mEditor.putString("wxNickname", "");
				    	TVwxNickname.setText(R.string.third_unbind);

				    	Platform WXplat = ShareSDK.getPlatform(ThirdBind.this, Wechat.NAME);
						LogUtil.i("---quit:---"+WXplat.toString());
						if (WXplat.isValid ()) {
							WXplat.removeAccount();
						}
						WXname = "";
				    }else if (userflag == 3){
				    	mEditor.putString("wbOpenId", "");
				    	mEditor.putString("wbNickname", "");
				    	TVsinaNickname.setText(R.string.third_unbind);
				    	SinaName = "";
				    }
				    mEditor.commit();
			    }
		    } catch (JSONException e) {
			    e.printStackTrace();
		    }
	    }
    }
	
	public RequestResult ThirdUnbind(){
		JSONObject paramJson=new JSONObject();
	  	try {
	    	paramJson.put("userid", HanvonApplication.hvnName);
	  	    paramJson.put("openId", unBindOpenId);
	  	} catch (JSONException e) {
	  		e.printStackTrace();
	  	}

	  	LogUtil.i(paramJson.toString());
	  	RequestResult result=new RequestResult();
	 	result=RequestServerData.thirdUnBind(paramJson);
	 	LogUtil.i(result.toString());
	  	return result;
		
	}
	
    private void authorize(Platform plat) {		

		if(plat.isValid()) {
			LogUtil.i("------isValid --11111111-------" + plat.isValid());
			String userId = plat.getDb().getUserId();
			if (!TextUtils.isEmpty(userId)) {
				UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
				nickname = plat.getDb().getUserName();
				openid = plat.getDb().getUserId();

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

			LogUtil.i("---nickname:" + nickname+"  openid:"+openid);
			login(platform.getName(), platform.getDb().getUserId(), res);
		}
		LogUtil.i(res.toString());
		nickname = platform.getDb().getUserName();
		openid = platform.getDb().getUserId();

		ThirdBindLogin login = new ThirdBindLogin(this,userflag);
		login.setNickName(platform.getDb().getUserName());
		login.setOpenid(platform.getDb().getUserId());
		
		login.BindToHvn();
	}
	
	public void onError(Platform platform, int action, Throwable t) {
		if (action == Platform.ACTION_USER_INFOR) {
			UIHandler.sendEmptyMessage(MSG_AUTH_ERROR, this);
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
				ThirdBindLogin login = new ThirdBindLogin(this,userflag);
				login.setNickName(nickname);
				login.setOpenid(openid);
				
				login.BindToHvn();
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
				ProgressDialogDimiss();
			}
			break;
			case MSG_AUTH_ERROR: {
				Toast.makeText(this, R.string.auth_error, Toast.LENGTH_SHORT).show();
				System.out.println("-------MSG_AUTH_ERROR--------");
				ProgressDialogDimiss();
			}
			break;
			case MSG_AUTH_COMPLETE: {
				Toast.makeText(this, R.string.auth_complete, Toast.LENGTH_SHORT).show();
				System.out.println("--------MSG_AUTH_COMPLETE-------");
			}
			break;
		}
		return false;
	}
	
	
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK )
	    {
	    	startActivity(new Intent(ThirdBind.this, MainActivity.class));
	    	this.finish();
	    }
	    return false;                                                                  
	}
}
