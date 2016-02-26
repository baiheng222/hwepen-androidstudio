package com.hanvon.hwepen;

import java.util.WeakHashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity implements OnClickListener
{
    private final String TAG = "SettingActivity";
    
    RelativeLayout mRlInputMethod;
    RelativeLayout mRlAutoUpgrade;
    RelativeLayout mRlClearBuffer;
    RelativeLayout mRlHelpManual;
    RelativeLayout mRlFeedBack;
    RelativeLayout mRlAboutUs;
    
  //  TextView mTvLogout;
    ImageView mTvBackBtn;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_setting);
        
     //   if (HanvonApplication.hvnName == ""){
      //  	findViewById(R.id.tv_setting_logout).setVisibility(View.GONE);
      //  }
        
        initView();
    }
    
    
    private void initView()
    {
        mRlInputMethod = (RelativeLayout) findViewById(R.id.rl_setting_inputmethod);
        mRlAutoUpgrade = (RelativeLayout) findViewById(R.id.rl_setting_upgrade);
        mRlClearBuffer = (RelativeLayout) findViewById(R.id.rl_setting_clear);
        mRlHelpManual = (RelativeLayout) findViewById(R.id.rl_setting_help);
        mRlFeedBack = (RelativeLayout) findViewById(R.id.rl_setting_feedback);
        mRlAboutUs = (RelativeLayout) findViewById(R.id.rl_setting_about);
        
    //    mTvLogout = (TextView) findViewById(R.id.tv_setting_logout);
        mTvBackBtn = (ImageView) findViewById(R.id.tv_backbtn);
        
        mRlInputMethod.setOnClickListener(this);
        mRlAutoUpgrade.setOnClickListener(this);
        mRlClearBuffer.setOnClickListener(this);
        mRlHelpManual.setOnClickListener(this);
        mRlFeedBack.setOnClickListener(this);
        mRlAboutUs.setOnClickListener(this);
        
    //    mTvLogout.setOnClickListener(this);
        mTvBackBtn.setOnClickListener(this);
        
    }
    
    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.rl_setting_about:
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
            break;
            
            case R.id.rl_setting_feedback:
            	Intent feedback = new Intent(this, FeedBackActivity.class);
                startActivity(feedback);
            break;
            
            case R.id.rl_setting_help:
                Intent help = new Intent(this, HelpActivity.class);
                startActivity(help);
            break;
                
            case R.id.rl_setting_clear:
                /*
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage(R.string.setting_clear_buffer);
                dialog.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() 
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) 
                    {

                    }
                });
                        
                dialog.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() 
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) 
                    {
                        NoteRecordDao noteDao = new NoteRecordDao(SettingActivity.this);
                        noteDao.deleteRecordsDB();
                        
                        //在清空缓存的时候，同时清空保存的同步的时间，--------有待讨论
                        SharedPreferences mSharedPreferences=getSharedPreferences("syncTime", Activity.MODE_MULTI_PROCESS);
                        if (mSharedPreferences != null){
						    Editor mEditor = mSharedPreferences.edit();
						    mEditor.putString("OldSyncTime","");
						    mEditor.commit();
                        }
                    }
                });
                
                dialog.show();
                   */
            break;

            case R.id.rl_setting_upgrade:
            //	if (HanvonApplication.isUpdate){
            //		Toast.makeText(this, "正在下载升级文件，请稍后再试!", Toast.LENGTH_SHORT).show();
            //	}else{
            	    SoftUpdate updateInfo = new SoftUpdate(this,1);
    	            updateInfo.checkVersion();
            //	}
            break;
            
            case R.id.tv_backbtn:
               Log.d(TAG, "back btn licked");
                finish();
            break;
        }
    }

    /*
	public void UserLoginOut(){
		HanvonApplication.strName = "";
		HanvonApplication.hvnName = "";
		HanvonApplication.BitHeadImage = null;
		SharedPreferences mSharedPreferences=getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
		int flag =mSharedPreferences.getInt("flag", 0);
	//	String plat = mSharedPreferences.getString("plat", "");
	//	LogUtil.i("---quit:---"+plat);
	//	HanvonApplication.plat = ;
		HanvonApplication.userFlag = flag;
		if (flag == 0){
		}else if(flag == 1){
		//	HanvonApplication.plat.removeAccount();
		  //  LoginUtil loginUtil = new LoginUtil(SettingActivity.this,SettingActivity.this);
		  //  loginUtil.QQLoginOut();
			Platform QQplat = ShareSDK.getPlatform(this, QQ.NAME);
			LogUtil.i("---quit:---"+QQplat);
			if (QQplat.isValid ()) {
				QQplat.removeAccount();
			}
	    }else if (flag == 2){
	    //	HanvonApplication.plat.removeAccount();
	    	Platform WXplat = ShareSDK.getPlatform(this, Wechat.NAME);
			LogUtil.i("---quit:---"+WXplat.toString());
			if (WXplat.isValid ()) {
				WXplat.removeAccount();
			}
	    }
		Editor mEditor=	mSharedPreferences.edit();
		mEditor.putInt("status", 0);
		mEditor.putString("nickname", "");
		mEditor.putString("username", "");
		HanvonApplication.isActivity = false;
		mEditor.commit();

		SharedPreferences mSharedCloudPreferences=getSharedPreferences("Cloud_Info", Activity.MODE_MULTI_PROCESS);
		int cloudFlag = mSharedCloudPreferences.getInt("cloudtype", 0);
		if (cloudFlag != 2){
		    Editor mCloudEditor = mSharedCloudPreferences.edit();
		    mCloudEditor.putString("token", "");
		    mCloudEditor.putInt("cloudtype", 0);
	        HanvonApplication.cloudType = 0;
	        mCloudEditor.commit();
		}

		startActivity(new Intent(SettingActivity.this, MainActivity.class));
		SettingActivity.this.finish();
	}
	*/
}
