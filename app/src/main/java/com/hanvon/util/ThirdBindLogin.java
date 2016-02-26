package com.hanvon.util;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.hanvon.application.HanvonApplication;
import com.hanvon.hwepen.login.ThirdBind;
import com.hanvon.net.JsonData;
import com.hanvon.net.RequestResult;
import com.hanvon.net.RequestServerData;


public class ThirdBindLogin {
	
	private Context mContext;
	private int userflag;
	private String openid;
	private String nickname;
	
	private String hvnNickname = "";
	
	public ThirdBindLogin(Context mcontext,int userflag){
		this.mContext = mcontext;
		this.userflag = userflag;
	}
	
	public void setOpenid(String openid){
		this.openid = openid;
	}
	
	public void setNickName(String nickname){
		this.nickname = nickname;
	}
	
	public void BindToHvn(){
		new RequestTask(1).execute();
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
				result = getUserInfo();
			}else if(flagTask == 2){
		        result = registerToHvn();
			}else if (flagTask == 3){
				result = bindToMainAccount();
			}
			return result;
		}
		 //响应结果
	    protected void onPostExecute(RequestResult result) {
	        JsonData data = result.getData();
	        String jsonCode= data.getJson();
	        try {
			    JSONObject json=new JSONObject(jsonCode);
			    LogUtil.i("flagTask:"+flagTask+"    json:"+json.toString());
			    if (flagTask == 1){
			    	if (json.getString("code").equals("0")){
			    		hvnNickname = json.getString("nickname");
			    		new RequestTask(3).execute();
			    	}else if (json.getString("code").equals("426")){
			    		new RequestTask(2).execute();
			    	}else if(json.getString("code").equals("520")){
			    		Toast.makeText(mContext, "服务器忙，请稍后重试", Toast.LENGTH_SHORT).show();
			    		mContext.startActivity(new Intent(mContext, ThirdBind.class));
			    	}else{
			    		Toast.makeText(mContext, "注册汉王云失败，请稍后重试", Toast.LENGTH_SHORT).show();
			    		mContext.startActivity(new Intent(mContext, ThirdBind.class));
			    	}
			    }else if (flagTask == 2){
			        if (json.getString("code").equals("0")){
			        	new RequestTask(3).execute();
			        }else{
			    	    Toast.makeText(mContext, "注册汉王云失败，请稍后重试", Toast.LENGTH_SHORT).show();
			    	    mContext.startActivity(new Intent(mContext, ThirdBind.class));
			        }
			    }else if (flagTask == 3){
			    	if (json.getString("code").equals("0")){
			    		SharedPreferences mSharedPreferences=mContext.getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
						Editor mEditor=	mSharedPreferences.edit();
						if (userflag == 1){
						    if (hvnNickname.equals("")){
							    mEditor.putString("qqNickname", nickname);
						    }else{
						    	mEditor.putString("qqNickname", hvnNickname);
						    }
						    mEditor.putString("qqOpenId", openid);
						}else if (userflag == 2){
							if (hvnNickname.equals("")){
							    mEditor.putString("wxNickname", nickname);
						    }else{
						    	mEditor.putString("wxNickname", hvnNickname);
						    }
							mEditor.putString("wxOpenId", openid);
						}else if (userflag == 3){
							if (hvnNickname.equals("")){
							    mEditor.putString("wbNickname", nickname);
						    }else{
						    	mEditor.putString("wbNickname", hvnNickname);
						    }
							mEditor.putString("wbOpenId", openid);
						}
						mEditor.commit();
						mContext.startActivity(new Intent(mContext, ThirdBind.class));
						ThirdBind.instance.finish();
			    	}else if (json.getString("code").equals("439")){
			    		Toast.makeText(mContext, "该账户已绑定第三方，请先解绑定再进行绑定!", Toast.LENGTH_SHORT).show();
			    		mContext.startActivity(new Intent(mContext, ThirdBind.class));
			    	}else{
			    		Toast.makeText(mContext, "第三方绑定失败，请稍后重试!", Toast.LENGTH_SHORT).show();
			    		mContext.startActivity(new Intent(mContext, ThirdBind.class));
			    	}
			    }
		    } catch (JSONException e) {
			    e.printStackTrace();
		    }
	    }
    }
	
	 public RequestResult getUserInfo(){
	    JSONObject paramJson=new JSONObject();
	  	try {
	  		if (userflag == 1){
	    	    paramJson.put("user", "qq_"+SHA1Util.encodeBySHA(openid));
	  		}else if (userflag == 2){
	  			paramJson.put("user", "wx_"+SHA1Util.encodeBySHA(openid));
	  		}
	  	} catch (JSONException e) {
	  		e.printStackTrace();
	  	}

	  	LogUtil.i(paramJson.toString());
	  	RequestResult result=new RequestResult();
	 	result=RequestServerData.getUserInfo(paramJson);
	 	LogUtil.i(result.toString());
	  	return result;
	}

    public RequestResult registerToHvn(){
    	JSONObject paramJson=new JSONObject();
  	    try {
  		    paramJson.put("openId", openid);
		    paramJson.put("nickName", nickname);
  	    } catch (JSONException e) {
  		    e.printStackTrace();
  	    }

  	    LogUtil.i(paramJson.toString());
  	    RequestResult result=new RequestResult();
  	    if (userflag == 1){
 	        result=RequestServerData.QQuserToHvn(paramJson);
  	    }else if (userflag == 2){
  	    	result=RequestServerData.WXuserToHvn(paramJson);
  	    }
 	    LogUtil.i(result.toString());
  	    return result;
    }

    public RequestResult bindToMainAccount(){
    	JSONObject paramJson=new JSONObject();
  	    try {
  		    paramJson.put("userid", HanvonApplication.hvnName);
		    paramJson.put("openId", openid);
		    paramJson.put("bindType", userflag-1);
  	    } catch (JSONException e) {
  		    e.printStackTrace();
  	    }

  	    LogUtil.i(paramJson.toString());
  	    RequestResult result=new RequestResult();
  	    result=RequestServerData.thirdBind(paramJson);
 
 	    LogUtil.i(result.toString());
  	    return result;
    }
}

