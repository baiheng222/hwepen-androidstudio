package com.hanvon.hwepen;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.hanvon.application.AppManage;
import com.hanvon.bean.FileInfo;
import com.hanvon.bean.FileInfo.FileType;
import com.hanvon.bean.User;
import com.hanvon.common.ServiceWS;
import com.hanvon.db.DBManager;
import com.hanvon.util.FileUtil;
import com.hanvon.util.HttpClientHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ExcerptCreateActivity extends BaseActivity
{
	private ImageView back;
	private ImageView home;
	private EditText title;
	private EditText content;
	private ImageView finish;
	private ImageView delete;
	
	private String titleValue = "";
	private String contentValue= "";
	private CreateHandler createHandler;
	private DBManager dbManager;
	private User userInfo;
	private ProgressDialog pd;
	private static final String TAG = "ExcerptSearchActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.excerpt_create);
		
		
		createHandler = new CreateHandler(this);
		dbManager = new DBManager(this);
//		userInfo = dbManager.user_currentUser(); //获取当前登录用户
		
		back = (ImageView) findViewById(R.id.excerpt_create_back);
		home = (ImageView) findViewById(R.id.excerpt_create_home);
		title = (EditText) findViewById(R.id.excerpt_create_title);
		content = (EditText) findViewById(R.id.excerpt_create_content);
		finish = (ImageView) findViewById(R.id.excerpt_create_finish);
		delete = (ImageView) findViewById(R.id.excerpt_create_delete);
		
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				title.setText("");
				content.setText("");
			}
		});

		finish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d(TAG, "finish clicked !!!!!!!!");
				titleValue = title.getText().toString();
				contentValue = content.getText().toString();
				if (titleValue.equals("") && contentValue.equals("")) {
					Toast.makeText(getApplication(), "请输入标题或内容内容后保存！", Toast.LENGTH_SHORT).show();
				} else {
					pd = ProgressDialog.show(ExcerptCreateActivity.this, "", "正在保存......");
					saveExcerpt();
				}
			}
		});
		
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				titleValue = title.getText().toString();
				contentValue = content.getText().toString();
				if (!titleValue.equals("") || !contentValue.equals("")) {
					saveExcerpt();
				}
				ExcerptCreateActivity.this.setResult(RESULT_OK, null);
				ExcerptCreateActivity.this.finish();
			}
		});
		
		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ExcerptCreateActivity.this, MainActivity.class));
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
			titleValue = title.getText().toString();
			contentValue = content.getText().toString();
			if (!titleValue.equals("") || !contentValue.equals("")) {
				saveExcerpt();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void saveExcerpt(){
		if (titleValue.equals("")) {
			if (contentValue.length() > 15) {
				titleValue = contentValue.substring(0, 15);
			} else {
				titleValue = contentValue;
			}
		}
		CreateThread thread = new CreateThread(titleValue, contentValue);
		new Thread(thread).start();
		
//		if (!contentValue.equals("")) { //title为空可以新建
//			if (titleValue.equals("")) {
//				if (contentValue.length() > 15) {
//					titleValue = contentValue.substring(0, 15);
//				} else {
//					titleValue = contentValue;
//				}
//			}
//			pd = ProgressDialog.show(ExcerptCreateActivity.this, "", "正在保存......");
//			CreateThread thread = new CreateThread(titleValue, contentValue);
//			new Thread(thread).start();
//		} else {
//			Toast.makeText(getApplication(), "请输入内容后保存！", Toast.LENGTH_SHORT).show();
//		}
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
	
	public class CreateThread implements Runnable
	{
		private String title;
		private String content;
		public CreateThread(String title, String content) {
			super();
			this.title = title;
			this.content = content;
		}
		@Override
		public void run() {
			try {
				String url = ServiceWS.FILE_ADD;
				
				JSONObject paramJson = new JSONObject();
				paramJson.put("devid", "");
				paramJson.put("sid", "");
				paramJson.put("uid", "");
				paramJson.put("ver", "");
				//paramJson.put("userid", userInfo.getUserId());
				paramJson.put("userid", MainActivity.curUserId);
				paramJson.put("ftype", FileType.EXCERPT.getValue());
				paramJson.put("title", title);
				paramJson.put("summary", "");
				paramJson.put("content", content);
				String params = paramJson.toString();
				Log.d(TAG, "!!!!!!! add file json string is " + params);
				String responce = HttpClientHelper.sendPostRequest(url, params);
				
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				createHandler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressLint("HandlerLeak")
	public class CreateHandler extends Handler
	{
		private Context context;
		public CreateHandler(Context context) {
			super();
			this.context = context;
		}
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (null != pd) {
				pd.dismiss();
			}
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			try {
				if (null == responce) { // 没有网络连接先存到手机
					Log.i(TAG, "network is not support");
					Toast.makeText(context, "连接服务器超时，请稍后再试", Toast.LENGTH_SHORT).show();
//					Log.i(TAG, "saving to SDCard where network is not support");
//					String path = "/hwnotes/excerpt/" + userInfo.getUserId() + "/" + titleValue + contentValue.length() + ".dat";
//					FileUtil.createSDFile(path);
//					FileUtil.saveTextInSdcard(path, contentValue);
//					Log.i(TAG, "save file sucessfully");
//					//更新sqlite
//					FileInfo file = new FileInfo();
////        			file.setFuuid(StringUtil.generateUUID());
//					file.setFuuid(StringUtil.generateUUID());
//					file.setUserId(userInfo.getUserId());
//					file.setTitle(titleValue);
//					file.setType(FileType.EXCERPT.getValue());
//					file.setSummary("summary");
//					file.setLength(contentValue.length()+"");
//        			file.setCreateTime(DateTimeEx.getDateTimeStr(new Date()));
//					file.setModifyTime(DateTimeEx.getDateTimeStr(new Date()));
//					file.setAccessTime(DateTimeEx.getDateTimeStr(new Date()));
//					file.setSyn("0");
//					file.setPath(path);
//					dbManager.file_add(file);
				} else {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						Toast.makeText(ExcerptCreateActivity.this, "保存" + titleValue + "成功！", Toast.LENGTH_SHORT).show();
						
						//新建成功后保存到手机
						//String path = "/hwepen/excerpt/" + userInfo.getUserId() + "/" + responceJson.getString("fuid") + ".dat";//通过length判断文件是否有更新
						String path = "/hwepen/excerpt/" + MainActivity.curUserId + "/" + responceJson.getString("fuid") + ".dat";//通过length判断文件是否有更新
						if (FileUtil.fileExist(path)) {
							FileUtil.delFile(path);
							FileUtil.createSDFile(path);
							FileUtil.saveTextInSdcard(path, contentValue);
						} else {
							FileUtil.createSDFile(path);
							FileUtil.saveTextInSdcard(path, contentValue);
						}
						Log.i(TAG, "save file sucessfully");
						FileInfo f = new FileInfo();
						f.setFuuid(responceJson.getString("fuid"));
						//f.setUserId(userInfo.getUserId());
						f.setUserId(MainActivity.curUserId);
						f.setType(FileType.EXCERPT.getValue());
						f.setSyn("0");
						f.setPath(path);
						dbManager.file_add(f);
						
						startActivity(new Intent(ExcerptCreateActivity.this, ExcerptActivity.class));
					} else {
						Toast.makeText(context, "网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
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
