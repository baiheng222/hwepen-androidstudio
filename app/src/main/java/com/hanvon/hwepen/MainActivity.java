package com.hanvon.hwepen;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hanvon.application.HanvonApplication;
import com.hanvon.application.AppManage;
import com.hanvon.common.DevCons;
import com.hanvon.splash.SplashActivity;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.LogUtil;
import com.hanvon.util.StringUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.hanvon.util.CircleImageView;
import com.hanvon.hwepen.login.ShowUserMessage;
import com.hanvon.hwepen.login.LoginActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapCommonUtils;

import org.json.JSONException;

public class MainActivity extends BaseActivity implements OnClickListener
{
	private final String TAG = "MainActivity";
	private ImageView excerpt;
	private ImageView recording;
//	private ImageView words;
//	private ImageView translate;
	private TextView versionView;
	private ImageView mLeftMenuBtn;

	SlidingMenu leftMenu;
	private CircleImageView mIvLogin;
	private TextView TVusername;
	private TextView TVnickname;
	private RelativeLayout mRlSetting;
	private RelativeLayout mRlCloudSync;
	//private RelativeLayout mRlCount;
	
	public static String curUserId;
	public static String version;

	private int loginStatus = 0;
	private String mUserName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().finishPreActivities();
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.main);


		initLeftMenu();
		init();
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}

		mLeftMenuBtn = (ImageView) findViewById(R.id.btn_leftmenu);
		mLeftMenuBtn.setOnClickListener(this);
		excerpt = (ImageView) findViewById(R.id.main_excerpt);
		recording = (ImageView) findViewById(R.id.main_record);
		versionView = (TextView) findViewById(R.id.main_version);
//		words = (ImageView) findViewById(R.id.main_words);
//		translate = (ImageView) findViewById(R.id.main_translate);
		
		if (!StringUtil.isEmpty(version)) {
			versionView.setText("版本 " + version);
		}


		if (new ConnectionDetector(this).isConnectingTOInternet())
		{
			Log.d(TAG, "!!!!! connected to internet");
			boolean autoUpdateflag = Settings.getKeyVersionUpdate(MainActivity.this);
			if (autoUpdateflag)
			{
				Log.d(TAG, "!!!!! autoupdate");
				SoftUpdate updateInfo = new SoftUpdate(this,0);
				updateInfo.checkVersion();
			}
		}

		excerpt.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View arg0)
			{
				//if (StringUtil.isEmpty(SplashActivity.userId))
				//if (HanvonApplication.hvnName.equals(""))
				if (0 == loginStatus)
				{ //登录验证异常重新登录
					Toast.makeText(getApplication(), "登录验证超时或异常，请重新登录", Toast.LENGTH_LONG).show();
					startActivity(new Intent(getApplication(), LoginActivity.class));
					finish();
				}
				else
				{
					startActivity(new Intent(MainActivity.this, ExcerptActivity.class));
				}
				/*
				else if (!(new ConnectionDetector(MainActivity.this).isConnectingTOInternet()))
				//else if (SplashActivity.userId.equals(DevCons.LOCAL))
				{ //登录验证超时（无网络） 使用本地数据
					//curUserId = SplashActivity.lastUser.getUserId();
					curUserId = DevCons.LOCAL;
					startActivity(new Intent(MainActivity.this, ExcerptActivity.class));
				}
				else
				{
					curUserId = SplashActivity.userId;
					startActivity(new Intent(MainActivity.this, ExcerptActivity.class));
				}
				*/
			}
		});
		
		recording.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//if (StringUtil.isEmpty(SplashActivity.userId))
				if (0 == loginStatus)
				{ //登录验证异常重新登录
					Toast.makeText(getApplication(), "登录验证超时或异常，请重新登录", Toast.LENGTH_LONG).show();
					startActivity(new Intent(getApplication(), LoginActivity.class));
					finish();
				}
				else
				{
					startActivity(new Intent(MainActivity.this, RecordingActivity.class));
				}
				/*
				else if (SplashActivity.userId.equals(DevCons.LOCAL))
				{ //登录验证超时（无网络） 使用本地数据
					curUserId = SplashActivity.lastUser.getUserId();
					startActivity(new Intent(MainActivity.this, RecordingActivity.class));
				}
				else
				{
					curUserId = SplashActivity.userId;
					startActivity(new Intent(MainActivity.this, RecordingActivity.class));
				}
				*/
			}
		});
		
//		words.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				startActivity(new Intent(MainActivity.this, WordsActivity.class));
//			}
//		});
//		
//		translate.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				startActivity(new Intent(MainActivity.this, SentenceActivity.class));
//			}
//		});
		
	}

	/*
	public void showUpdataDialog()
	{
		AlertDialog.Builder builer = new AlertDialog.Builder(MainActivity.this);
		builer.setTitle("硬件版本升级");
		builer.setMessage("有最新的硬件版本，是否需要下载更新？");
		builer.setCancelable(false);
		builer.setPositiveButton("下载", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				new UpdateAppService(MainActivity.this,2).CreateInform(HanvonApplication.HardUpdateUrl);
			}
		});
		//当点取消按钮时进行登录
		builer.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		AlertDialog dialog = builer.create();
		dialog.show();
	}
	*/


	@Override
	protected void onStart()
	{
		super.onStart();
		/*
		if (!HanvonApplication.hvnName.equals(""))
		{
			LogUtil.i("---------MAIN---true---------------------");
			ShowUserInfo();
		}
		else
		{
			LogUtil.i("---------MAIN---false---------------------");
			TVusername.setText("");
			TVnickname.setText("未登录");
			if (HanvonApplication.userFlag == 0)
			{
				mIvLogin.setBackgroundResource(R.drawable.logicon);
			}
			else
			{
				mIvLogin.setImageDrawable((getResources().getDrawable(R.drawable.logicon)));
			}
		}

		Log.d(TAG, "!!!!! onstart !!!!! MainActivity.curUserId is " + MainActivity.curUserId);
		*/
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Log.d(TAG, " !!! onResume");

		ShowUserInfo();

		/*
		String hvnname = "";
		String username = "";

		SharedPreferences mSharedPreferences = this.getSharedPreferences("BitMapUrl", Activity.MODE_MULTI_PROCESS);
		int flag = mSharedPreferences.getInt("flag", 0);
		loginStatus = mSharedPreferences.getInt("status", 0);

		curUserId = mSharedPreferences.getString("username", "");
		*/
		/*
		if (flag == 0)
		{
			username = mSharedPreferences.getString("username", "");
			curUserId = username;
		}
		else
		{
			hvnname = mSharedPreferences.getString("username", "");
			curUserId = username;
		}
		*/

		Log.d(TAG, "!!!! onresume , !!!!MainActivity.curUserId is " + MainActivity.curUserId);



	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.finish();
	}
	
	//按两下返回键退出
	private long exitTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
		{
			if (leftMenu != null)
			{
				Log.d(TAG, "leftmenu is not null");
				if (leftMenu.isMenuShowing())
				{
					Log.d(TAG, "!!!!! left menu shown");
					leftMenu.toggle();
					return true;
				}
				else
				{
					Log.d(TAG, "!!!!! left menu hidden");
				}
			}

		    if((System.currentTimeMillis()-exitTime) > 2000){//System.currentTimeMillis()无论何时调用，肯定大于2000
		        Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
		        exitTime = System.currentTimeMillis();
		    } else {
		        AppManage.getInstance().exit();
		    }
	            return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private void init(){
		DevCons.searching = false;
		DevCons.searchFiles.clear();
//		if (StringUtil.isEmpty(SplashActivity.userId)) { //登录验证异常重新登录
//			Toast.makeText(getApplication(), "登录验证超时或异常，请重新登录", Toast.LENGTH_LONG).show();
//			startActivity(new Intent(getApplication(), LogInActivity.class));
//			finish();
//		} else if (SplashActivity.userId.equals(DevCons.LOCAL)) { //登录验证超时（无网络） 使用本地数据
//			curUserId = SplashActivity.lastUser.getUserId();
//		} else {
//			curUserId = SplashActivity.userId;
//		}

		/*
		ConnectionDetector connectionDetector = new ConnectionDetector(getApplication());
		if (connectionDetector.isConnectingTOInternet()) {
//			new AutoUpdate(getApplication(), 0).autoUpdate();
			new AutoUpdate(MainActivity.this).autoUpdate();
			
			new SyncNative(this).syncExcerpt();
			new SyncNative(this).syncRecording();
			
		} else {
			Toast.makeText(getApplication(), "温馨提示：没有检查到网络连接", Toast.LENGTH_LONG).show();
		}
		*/
	}


	private String getCurVersion() throws Exception
	{
		PackageManager packageManager = this.getPackageManager();
		PackageInfo packInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
		return packInfo.versionName;
	}

	public void initLeftMenu()
	{
		// configure the SlidingMenu
		leftMenu = new SlidingMenu(this);
		leftMenu.setMode(SlidingMenu.LEFT);
		//设置触摸屏幕的模式

		leftMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		leftMenu.setShadowWidthRes(R.dimen.shadow_width);
		leftMenu.setShadowDrawable(R.drawable.shadow);

		//设置滑动菜单视图的宽度
		leftMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		//设置渐入渐出效果的值
		leftMenu.setFadeDegree(0.35f);

		/**
		 * SLIDING_WINDOW will include the Title/ActionBar in the content
		 *section of the SlidingMenu, while SLIDING_CONTENT does not.
		 */
		//把滑动菜单添加进所有的Activity中，可选值SLIDING_CONTENT ， SLIDING_WINDOW
		leftMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		//为侧滑菜单设置布局
		leftMenu.setMenu(R.layout.leftmenu);

		mIvLogin = (CircleImageView) findViewById(R.id.iv_login_icon);
		mIvLogin.setOnClickListener(this);

		TVusername = (TextView)findViewById(R.id.ivUserName);
		TVnickname = (TextView)findViewById(R.id.ivhvnUserName);

		mRlSetting = (RelativeLayout) findViewById(R.id.rl_setting);
		mRlSetting.setOnClickListener(this);

		/*
		mRlCount = (RelativeLayout) findViewById(R.id.rl_count);
		mRlCount.setOnClickListener(this);
		*/

		mRlCloudSync = (RelativeLayout) findViewById(R.id.rl_cloud);
		mRlCloudSync.setOnClickListener(this);

		TextView version = (TextView) findViewById(R.id.tv_version);

		try
		{
			version.setText("Version" + getCurVersion());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		//ShowUserInfo();
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.iv_login_icon:

				if (HanvonApplication.hvnName.equals(""))
				//if (SplashActivity.userId.equals(""))
				{
					Intent newIntent1 = new Intent(this, LoginActivity.class);
					startActivity(newIntent1);
				}
				else
				{
					Intent newIntent1 = new Intent(this, ShowUserMessage.class);
					startActivity(newIntent1);
				}

				break;

			case R.id.rl_setting:

				Intent settingItent = new Intent(this, SettingActivity.class);
				startActivity(settingItent);

				break;



			case R.id.rl_cloud:
				/******************ceshi***********************/
				/*
				CloudSynchroProcess sync = new CloudSynchroProcess(MainActivity.this,0);
				try {
					sync.QuerySync();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				//	Toast.makeText(this, "此版本暂不支持该功能！", Toast.LENGTH_SHORT).show();
				break;

			case R.id.btn_leftmenu:
				if (null != leftMenu)
				{
					//leftMenu.toggle();
					leftMenu.showMenu();
				}
			break;
		}
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

		Log.d(TAG, "!!!! showuserinfo , !!!!MainActivity.curUserId is " + MainActivity.curUserId);

		if (flag == 0)
		{
			//    email = mSharedPreferences.getString("email", "");
			//    phone = mSharedPreferences.getString("phone", "");
			username = mSharedPreferences.getString("username", "");
			//curUserId = username;
		}
		else
		{
			figureurl = mSharedPreferences.getString("figureurl", "");
			hvnname = mSharedPreferences.getString("username", "");
			//curUserId = username;
		}

		mUserName = hvnname;

		Log.d(TAG, "!!!! showuserinfo , !!!!MainActivity.curUserId is " + MainActivity.curUserId);

		int status = mSharedPreferences.getInt("status", 0);
		loginStatus = status;
		LogUtil.i("flag:"+flag+"  nickname:"+nickname+"   status:"+status+"  username:"+hvnname+"  figureurl:"+figureurl);

		if (status == 1)	//已经登录
		{
			if (flag == 0) //汉王注册的账户
			{
				if(!nickname.isEmpty())
				{
					TVusername.setText(nickname);
					TVnickname.setText(username);
					HanvonApplication.hvnName = username;
					HanvonApplication.strName = nickname;
					mIvLogin.setBackgroundResource(R.drawable.login_head_default);
					LogUtil.i("hvnName:"+username+"  strName:"+nickname);
				}
				else
				{
					TVusername.setText("");
					TVnickname.setText(username);
					HanvonApplication.hvnName = username;
					HanvonApplication.strName = nickname;
					mIvLogin.setBackgroundResource(R.drawable.login_head_default);
					LogUtil.i("hvnName:"+username+"  strName:"+nickname);
				}

				curUserId = username;
			}
			if (flag == 1 || flag == 2) //第三方登录，qq, 微信
			{
				if(!nickname.isEmpty())
				{
					TVusername.setText(nickname);
					TVnickname.setText(hvnname);
					HanvonApplication.strName = nickname;
					HanvonApplication.hvnName = hvnname;
				}
				else
				{
					TVusername.setText("");
					TVnickname.setText(hvnname);
					HanvonApplication.strName = nickname;
					HanvonApplication.hvnName = hvnname;
				}

				curUserId = hvnname;
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
			TVusername.setText("");
			TVnickname.setText("未登录");
			if (HanvonApplication.userFlag == 0){
				mIvLogin.setBackgroundResource(R.drawable.logicon);
			}else{
				mIvLogin.setImageDrawable((getResources().getDrawable(R.drawable.logicon)));
			}
			//mIvLogin.setImageResource(R.drawable.logicon);
		}
	}
	
}
