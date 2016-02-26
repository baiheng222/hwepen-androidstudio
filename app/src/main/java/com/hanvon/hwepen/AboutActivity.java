package com.hanvon.hwepen;


import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity implements OnClickListener 
{
	private final String TAG = "SettingActivity";

	private ImageView mBack;
	private TextView mVersion;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);

		mVersion = (TextView) findViewById(R.id.tvVersionName);
		mBack = (ImageView) findViewById(R.id.iv_about_backbtn);
		mBack.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) 
	{
		switch (view.getId()) 
		{
		case R.id.iv_about_backbtn:
			finish();
			break;

		}

	}

	@Override
	protected void onStart() 
	{
		super.onStart();
		try 
		{
			mVersion.setText("Version:" + getCurVersion());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * 获取当前程序的版本
	 * 
	 * @return
	 */
	private String getCurVersion() throws Exception 
	{
		PackageManager packageManager = this.getPackageManager();
		PackageInfo packInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
		return packInfo.versionName;
	}

}
