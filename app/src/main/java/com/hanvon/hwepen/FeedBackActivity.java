package com.hanvon.hwepen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hanvon.util.ConnectionDetector;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeedBackActivity extends Activity implements OnClickListener
{
	private LinearLayout mLlContent;
	private LinearLayout mLlThanks;
	private ImageView mIvBackBtn;
	private EditText mEtContent;
	private EditText mEtEmail;
	private TextView mTvCommit;
	
	private Handler handler;

	private ProgressDialog progressDialog;

	private String mFeedbackAddress = "http://cloud.hwyun.com/ws-dcloud/rt/ap/v1/statistics/gather/feedback";
	private SharedPreferences mDefaultPreference;
	
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_feedback);
		initView();
	}

	private void initView()
	{
		((ImageView) findViewById(R.id.iv_feedback_backbtn))
				.setOnClickListener(this);

		mTvCommit = (TextView) findViewById(R.id.tv_feedback_commit);
		mTvCommit.setOnClickListener(this);

		mEtContent = (EditText) findViewById(R.id.et_feedback_content);
		mEtEmail = (EditText) findViewById(R.id.et_feedback_email);

		mLlContent = (LinearLayout) findViewById(R.id.ll_feedback_conten);
		mLlThanks = (LinearLayout) findViewById(R.id.ll_feedback_thanks);
		
		mDefaultPreference = PreferenceManager.getDefaultSharedPreferences(this);
		
		initHandler();
	}
	
	private void initHandler()
	{
		handler = new Handler() 
		{
			@Override
			public void handleMessage(Message msg) 
			{
				switch (msg.what) 
				{
				case 0:
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					/*
					if (feedbackDialog != null) {
						feedbackDialog.dismiss();
						feedbackDialog = null;
					}
					*/
					//Toast.makeText(this, R.string.feedback_error, Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					/*
					if (feedbackDialog != null) {
						feedbackDialog.dismiss();
						feedbackDialog = null;
					}
					*/
					//Toast.makeText(this,R.string.feedback_upload_done, Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}
		};
	}

	public boolean isMailNO(String mail)
	{
		Pattern p = Pattern.compile("^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)$");
		Matcher m = p.matcher(mail);
		return m.matches();
	}

	private void showProgress()
	{
		progressDialog = new ProgressDialog(this);
		progressDialog
				.setOnDismissListener(new DialogInterface.OnDismissListener()
				{
					public void onDismiss(DialogInterface dialog)
					{
						progressDialog = null;
					}
				});
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getString(R.string.feedback_progress));
		progressDialog.show();
	}

	private String UploadBody(String mPackageName, String version, String content, String mail)
	{
		JSONObject mJsonObject = new JSONObject();
		try
		{
			mJsonObject.putOpt("userid", "suluepen" + UUID.randomUUID());
			mJsonObject.putOpt("devid", "0");
			mJsonObject.putOpt("sid", mPackageName);
			mJsonObject.putOpt("ver", version);
			mJsonObject.putOpt("fbcontent", content);
			mJsonObject.putOpt("mobile", "");
			mJsonObject.putOpt("email", mail);
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		return mJsonObject.toString();
	}

	// 向服务器提交反馈信息
	public int feedback(final String content, final String mail)
	{
		try
		{
			String mPackageName = getApplicationInfo().packageName;
			PackageInfo mPackageInfo = getPackageManager().getPackageInfo(mPackageName, 0);
			String version = mPackageInfo.versionName;
			HttpUtils mHttpUtils = new HttpUtils();
			mHttpUtils.configTimeout(5000);
			mHttpUtils.configCurrentHttpCacheExpiry(100);
			mHttpUtils.configSoTimeout(5000);
			mHttpUtils.configRequestRetryCount(0);
			RequestParams params = new RequestParams();
			params.addBodyParameter("Content-Type", "application/octet-stream");
			params.setBodyEntity(new StringEntity(UploadBody(mPackageName,version, content, mail), "UTF-8"));
			mHttpUtils.send(HttpRequest.HttpMethod.POST, mFeedbackAddress, params, new RequestCallBack<String>()
			{
				@Override
				public void onFailure(HttpException arg0, String arg1)
				{
					try
					{
						Thread.sleep(500);
					} 
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					handler.sendEmptyMessage(0);
					mDefaultPreference.edit().putString("feedback_msg", content).commit();
					mDefaultPreference.edit().putString("feedback_mail", mail).commit();
				}

				@Override
				public void onSuccess(ResponseInfo<String> responseInfo)
				{
					try
					{
						final JSONObject mJsonObject = new JSONObject(responseInfo.result.toString());
						final String code = mJsonObject.getString("code");
						if (code.equals("0"))
						{
							String result = mJsonObject.getString("result");
							if (result.equals("success"))
							{
								try
								{
									Thread.sleep(500);
								} 
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
								handler.sendEmptyMessage(1);
								mDefaultPreference.edit().putString("feedback_msg",content).commit();
								mDefaultPreference.edit().putString("feedback_mail",mail).commit();
							}
						} 
						else
						{
							try
							{
								Thread.sleep(500);
							} catch (InterruptedException e)
							{
								e.printStackTrace();
							}
							handler.sendEmptyMessage(0);
							mDefaultPreference.edit().putString("feedback_msg", content).commit();
							mDefaultPreference.edit().putString("feedback_mail",mail).commit();
						}
					}
					catch (JSONException e1)
					{
						e1.printStackTrace();
					}
				}
			});
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return -1;
	}

	private void commitFeedBack()
	{
		if (mEtContent.getText().toString().length() == 0)
		{
			Toast.makeText(this, R.string.feedback_content_null,Toast.LENGTH_SHORT).show();
		} 
		else if (!isMailNO(mEtEmail.getText().toString()) && mEtEmail.getText().toString().length() != 0)
		{
			Toast.makeText(this, R.string.feedback_mail_null, Toast.LENGTH_SHORT).show();
		} 
		else
		{
			showProgress();
			if (!new ConnectionDetector(this).isConnectingTOInternet())
			{
				handler.sendEmptyMessage(0);
				mDefaultPreference.edit().putString("feedback_msg", mEtContent.getText().toString()).commit();
				mDefaultPreference.edit().putString("feedback_mail", mEtEmail.getText().toString()).commit();
			}
			else
			{
				new Thread()
				{
					public void run()
					{
						if (feedback(mEtContent.getText().toString(), mEtContent.getText().toString()) == 200)
						{

						} 
						else
						{

						}
					};
				}.start();

			}
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.iv_feedback_backbtn:
			finish();
			break;

		case R.id.tv_feedback_commit:
			commitFeedBack();
			break;
		default:
			break;
		}
	}
}
