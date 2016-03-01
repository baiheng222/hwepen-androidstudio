package com.hanvon.splash;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import com.hanvon.application.HanvonApplication;
import com.hanvon.bean.User;
import com.hanvon.common.DevCons;
import com.hanvon.db.DBManager;
//import com.hanvon.hwepen.LogInActivity;
import com.hanvon.hwepen.login.LoginActivity;
import com.hanvon.hwepen.MainActivity;
import com.hanvon.hwepen.R;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.LogUtil;
import com.hanvon.util.StringUtil;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapCommonUtils;


/**
 * 启动画面 
 * (1)判断是否是首次加载应用--采取读取SharedPreferences的方法
 * (2)是，则进入GuideActivity；否，则进入LoginActivity 
 * (3) N s后执行(2)操作
 * 
 * @author Hu
 * 
 */
public class SplashActivity extends Activity
{
	
	public static DBManager dbManager;
	public static User lastUser;
	public static String userId = null;
	
	private static final String TAG = "SplashActivity";
	
	public static boolean isFirstIn = false;

	private static final int GO_HOME = 1000;
	private static final int GO_GUIDE = 1001;
	// 启动延迟
	private static final long SPLASH_DELAY_MILLIS = 3000;

	private static final String SHAREDPREFERENCES_NAME = "first_pref";
	private int loginStatus;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		
		initData();
		
		init();
	}

	private void init() {
		// 读取SharedPreferences中需要的数据
		// 使用SharedPreferences来记录程序的使用次数
		SharedPreferences preferences = getSharedPreferences(SHAREDPREFERENCES_NAME, MODE_PRIVATE);
		// 取得相应的值，如果没有该值，说明还未写入，用true作为默认值
		isFirstIn = preferences.getBoolean("isFirstIn", true);
		// 判断程序与第几次运行，如果是第一次运行则跳转到引导界面，否则跳转到主界面
//		if (!isFirstIn) {
			// 使用Handler的postDelayed方法，3秒后执行跳转
			mHandler.sendEmptyMessageDelayed(GO_HOME, SPLASH_DELAY_MILLIS);
//		} else {
//			mHandler.sendEmptyMessageDelayed(GO_GUIDE, SPLASH_DELAY_MILLIS);
//		}
	}
	
	private Handler mHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GO_HOME:
				goHome();
				break;
			case GO_GUIDE:
				goGuide();
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void goHome()
	{
		//if (!StringUtil.isEmpty(lastUser.getUserId()))
		//if (!StringUtil.isEmpty(userId))
		if (1 == loginStatus)
		{
			startActivity(new Intent(getApplication(), MainActivity.class));
			finish();
		}
		else
		{
			startActivity(new Intent(getApplication(), LoginActivity.class));
			finish();
		}
	}
	
	private void goGuide()
	{
		Intent intent = new Intent(SplashActivity.this, GuideActivity.class);
		startActivity(intent);
		finish();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD) @SuppressLint("NewApi")
	public void ShowUserInfo()
	{
		String email = "",phone = "",hvnname = "",figureurl = "",username = "";
		SharedPreferences mSharedPreferences = this.getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
		int flag = mSharedPreferences.getInt("flag", 0);
		HanvonApplication.userFlag = flag;
		HanvonApplication.isActivity = mSharedPreferences.getBoolean("isActivity", false);
		String nickname = mSharedPreferences.getString("nickname", "");
		boolean isHasNick = mSharedPreferences.getBoolean("isHasNick", true);
		if (flag == 0)
		{
			//    email = mSharedPreferences.getString("email", "");
			//    phone = mSharedPreferences.getString("phone", "");
			username = mSharedPreferences.getString("username", "");
		}
		else
		{
			figureurl = mSharedPreferences.getString("figureurl", "");
			hvnname = mSharedPreferences.getString("username", "");
		}

		//mUserName = hvnname;

		int status = mSharedPreferences.getInt("status", 0);
		loginStatus = status;
		LogUtil.i("flag:" + flag + "  nickname:" + nickname + "   status:" + status + "  username:" + hvnname + "  figureurl:" + figureurl);

		if (status == 1)	//已经登录
		{
			if (flag == 0) //汉王注册的账户
			{
				if(!nickname.isEmpty())
				{
					//TVusername.setText(nickname);
					//TVnickname.setText(username);
					HanvonApplication.hvnName = username;
					HanvonApplication.strName = nickname;
					//mIvLogin.setBackgroundResource(R.drawable.login_head_default);
					LogUtil.i("hvnName:"+username+"  strName:"+nickname);
				}
				else
				{
					//TVusername.setText("");
					//TVnickname.setText(username);
					HanvonApplication.hvnName = username;
					HanvonApplication.strName = nickname;
					//mIvLogin.setBackgroundResource(R.drawable.login_head_default);
					LogUtil.i("hvnName:"+username+"  strName:"+nickname);
				}
			}
			if (flag == 1 || flag == 2) //第三方登录，qq, 微信
			{
				if(!nickname.isEmpty())
				{
					//TVusername.setText(nickname);
					//TVnickname.setText(hvnname);
					HanvonApplication.strName = nickname;
					HanvonApplication.hvnName = hvnname;
				}
				else
				{
					//TVusername.setText("");
					//TVnickname.setText(hvnname);
					HanvonApplication.strName = nickname;
					HanvonApplication.hvnName = hvnname;
				}
				if(!figureurl.isEmpty())
				{
					BitmapUtils bitmapUtils = new  BitmapUtils(this);
					bitmapUtils.configDefaultLoadingImage(R.drawable.logicon);
					bitmapUtils.configDefaultBitmapMaxSize(BitmapCommonUtils.getScreenSize(
							this).scaleDown(3));
					bitmapUtils.configDefaultShowOriginal(true);
					bitmapUtils.display(((ImageView)findViewById(R.id.iv_login_icon)),figureurl);
				}
			}
		}
		else
		{
			//TVusername.setText("");
			//TVnickname.setText("未登录");
			if (HanvonApplication.userFlag == 0)
			{
				//mIvLogin.setBackgroundResource(R.drawable.logicon);
			}
			else
			{
				//mIvLogin.setImageDrawable((getResources().getDrawable(R.drawable.logicon)));
			}
			//mIvLogin.setImageResource(R.drawable.logicon);
		}
	}

	private void initData()
	{

		dbManager = new DBManager(this);
		lastUser = dbManager.user_lastUser();


		String email = "",phone = "",hvnname = "",figureurl = "",username = "";
		String passwd = "";
		SharedPreferences mSharedPreferences = this.getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
		int flag = mSharedPreferences.getInt("flag", 0);
		HanvonApplication.userFlag = flag;
		HanvonApplication.isActivity = mSharedPreferences.getBoolean("isActivity", false);
		String nickname = mSharedPreferences.getString("nickname", "");
		boolean isHasNick = mSharedPreferences.getBoolean("isHasNick", true);
		if (flag == 0)
		{
			//    email = mSharedPreferences.getString("email", "");
			//    phone = mSharedPreferences.getString("phone", "");
			username = mSharedPreferences.getString("username", "");
		}
		else
		{
			figureurl = mSharedPreferences.getString("figureurl", "");
			hvnname = mSharedPreferences.getString("username", "");
		}

		int status = mSharedPreferences.getInt("status", 0);
		loginStatus = status;
		passwd = mSharedPreferences.getString("passwd", "");


		//if (!StringUtil.isEmpty(lastUser.getUserId()))
		if (0 == status)	//not login
		{
			Log.d(TAG, "!!!!!!!!status == 0, not login ");
			if (0 == flag)	//hanwang user
			{
				Log.d(TAG, "!!!!!!!! flag == 0, hanwang user");
				if (!hvnname.isEmpty()) //未登录，并且用户名不为空
				{
					Log.d(TAG, "!!!!! hvname is not empty");
					if (new ConnectionDetector(this).isConnectingTOInternet())  //有网络
					{
						Log.d(TAG, "!!!!!! connect to intelnet");
						//new LoginCheck(lastUser.getUserId(), lastUser.getPassword()).loginCheck();
						new LoginCheck(hvnname, passwd).loginCheck();
					}
					else //无网络使用本定名字
					{
						Log.d(TAG, "!!!!! no intelnet ,use local user");
						//网络原因用户没有验证，继续使用本地数据
						SplashActivity.userId = DevCons.LOCAL;
						Log.e(TAG, "登录验证异常");
					}
				}
				else
				{
					Log.d(TAG, "本地无用户数据");
				}
			}
			else	//QQ, WeiChat thirdpart user
			{
				Log.d(TAG, "!!!! thirdpart user login !!!");
			}
		}
		else
		{
			Log.d(TAG, "!!!!!! login !!!!1");
		}
	}
	
}
