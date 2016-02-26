package com.hanvon.hwepen;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanvon.application.AppManage;
import com.hanvon.bean.FileInfo;
import com.hanvon.bean.User;
import com.hanvon.common.DevCons;
import com.hanvon.common.ServiceWS;
import com.hanvon.db.DBManager;
import com.hanvon.util.FileUtil;
import com.hanvon.util.HttpClientHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ExcerptReadActivity extends BaseActivity
{
	
	private ImageView back;
	private ImageView home;
	private TextView title;
	private TextView content;
	private ImageView edit;
	private ImageView create;
	private ImageView search;
	private ImageView share;
//	private ImageView delete;
	
	private String titleValue;
	private String contentValue;
	
	private String serVer;
	
	private String fuuid;
	private ReadHandler readHandler;
	private DBManager dbManager;
	private User userInfo;
	private ProgressDialog pd;
	private static final String TAG = "ExcerptReadActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.excerpt_read);
		
		
		readHandler = new ReadHandler();
		dbManager = new DBManager(this);
//		userInfo = dbManager.user_currentUser(); //获取当前登录用户
		
		back = (ImageView) findViewById(R.id.excerpt_read_back);
		home = (ImageView) findViewById(R.id.excerpt_read_home);
		title = (TextView) findViewById(R.id.excerpt_read_title);
		content = (TextView) findViewById(R.id.excerpt_read_content);
		//底部按钮
		create = (ImageView) findViewById(R.id.erb_create);
		edit = (ImageView) findViewById(R.id.erb_edit);
		search = (ImageView) findViewById(R.id.erb_search);
		share = (ImageView) findViewById(R.id.erb_share);
//		delete = (ImageView) findViewById(R.id.excerpt_read_delete);
		
		fuuid = getIntent().getStringExtra("fuuid");
		pd = ProgressDialog.show(ExcerptReadActivity.this, "", "请稍后......");
		ReadThread thread = new ReadThread(fuuid);
		new Thread(thread).start();
		
		edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String tilteValue = title.getText().toString();
				String contentValue = content.getText().toString();
				Intent intent = new Intent();
				intent.setClass(ExcerptReadActivity.this, ExcerptEditActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("fuuid", fuuid);
				bundle.putString("title", tilteValue);
				bundle.putString("content", contentValue);
				bundle.putString("serVer", serVer);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		create.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(ExcerptReadActivity.this, ExcerptCreateActivity.class));
			}
		});
		
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ExcerptReadActivity.this, ExcerptSearchActivity.class));
			}
		});
		
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "hwCloud");
                intent.putExtra(Intent.EXTRA_TEXT, contentValue==null? DevCons.SHARE:contentValue);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ExcerptReadActivity.this.startActivity(Intent.createChooser(intent, "分享"));
			}
		});

//		delete.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				DeleteThread t = new DeleteThread(fuuid);
//				new Thread(t).start();
//			}
//		});
		
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ExcerptReadActivity.this.setResult(RESULT_OK, null);
				ExcerptReadActivity.this.finish();
			}
		});
		
		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ExcerptReadActivity.this, MainActivity.class));
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		dbManager.closeDB();
		this.finish();
		super.onDestroy();
	}
	
	public class ReadThread implements Runnable
	{
		private String fuuid;
		public ReadThread(String fuuid) {
			super();
			this.fuuid = fuuid;
		}
		@Override
		public void run() {
			try {
				String url = ServiceWS.FILE_GETCNT;
				JSONObject paramJson = new JSONObject();
				paramJson.put("devid", "");
				paramJson.put("ftype", "");
				paramJson.put("sid", "");
				paramJson.put("uid", "");
				paramJson.put("userid", "");
				paramJson.put("ver", "");
				paramJson.put("fuid", fuuid);
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);
				
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				readHandler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressLint("HandlerLeak")
	public class ReadHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			pd.dismiss();
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			try {
				if (null == responce) { // 没有网络连接查询android数据库
					Log.i(TAG, "querying from SQLite where network is not support");
					//read from local
					FileInfo file = dbManager.file_queryById(fuuid);
					if (null == file.getPath() || file.getPath().equals("")) {
						return;
					}
					String cnt = FileUtil.loadTextFromSdcard(file.getPath());
					title.setText(file.getTitle());
					content.setText(cnt);
				} else {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						titleValue = responceJson.getString("title");
						contentValue = responceJson.getString("content");
						title.setText(titleValue);
						content.setText(contentValue);
						serVer = responceJson.getString("serVer");
						
						//保存到手机
						String path = "/hwepen/excerpt/" + userInfo.getUserId() + "/" + fuuid + ".dat";//通过length判断文件是否有更新
						if (FileUtil.fileExist(path)) {
							FileUtil.delFile(path);
							FileUtil.createSDFile(path);
							FileUtil.saveTextInSdcard(path, contentValue);
						} else {
							FileUtil.createSDFile(path);
							FileUtil.saveTextInSdcard(path, contentValue);
							Log.i(TAG, "save file sucessfully");
						}
						//更新sqlite
						FileInfo file = new FileInfo();
						file.setFuuid(fuuid);
						file.setPath(path);
						dbManager.file_update(file);
						Log.i(TAG, "update SQLite sucessfully");
					} else {
						Toast.makeText(ExcerptReadActivity.this, "网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
						return;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}