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
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.hanvon.application.AppManage;
import com.hanvon.bean.FileInfo;
import com.hanvon.bean.FileInfo.FileType;
import com.hanvon.bean.User;
import com.hanvon.common.ServiceWS;
import com.hanvon.db.DBManager;
import com.hanvon.util.HttpClientHelper;
import com.hanvon.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecordingSearchActivity extends BaseActivity
{
	
	private ImageView back;
	private ImageView home;
	private EditText searchText;
	private ImageView search;
	private ImageView clear;
	
	private SearchHandler searchHandler;
	private DBManager dbManager;
	private User userInfo;
	private ProgressDialog pd;
	private static final String TAG = "RecordingSearchActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.recording_search);
		
		
		searchHandler = new SearchHandler(this);
		dbManager = new DBManager(this);
		
		back = (ImageView) findViewById(R.id.recording_search_back);
		home = (ImageView) findViewById(R.id.recording_search_home);
		searchText = (EditText) findViewById(R.id.recording_search_text);
		search = (ImageView) findViewById(R.id.recording_search);
		clear = (ImageView) findViewById(R.id.recording_search_clear);
		
//		userInfo = dbManager.user_currentUser(); //获取当前登录用户
		
		clear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchText.setText("");
			}
		});

		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String searchTextValue = searchText.getText().toString();
				if (null != searchTextValue && !searchTextValue.equals("") && !searchTextValue.equals("搜索")) {
					pd = ProgressDialog.show(RecordingSearchActivity.this, "", "正在查询......");
					SearchThread thread = new SearchThread(searchTextValue);
					new Thread(thread).start();
				}
			}
		});
		//监听软键盘回车
		searchText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				Log.i(TAG, actionId + "");
				String searchTextValue = searchText.getText().toString();
				if (null != searchTextValue && !searchTextValue.equals("") && !searchTextValue.equals("搜索")) {
					pd = ProgressDialog.show(RecordingSearchActivity.this, "", "正在查询......");
					SearchThread thread = new SearchThread(searchTextValue);
					new Thread(thread).start();
				}
				return false;
			}
		});
		
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RecordingSearchActivity.this.setResult(RESULT_OK, null);
				RecordingSearchActivity.this.finish();
			}
		});
		
		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(RecordingSearchActivity.this, MainActivity.class));
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
	
	public class SearchThread implements Runnable
	{
		private String searchTextValue;
		public SearchThread(String searchTextValue) {
			super();
			this.searchTextValue = searchTextValue;
		}
		@Override
		public void run() {
			try {
				String url = ServiceWS.FILE_SEARCH;
				
				JSONObject paramJson = new JSONObject();
				JSONObject jsonQstr = new JSONObject();
				JSONObject jsonSort = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("ver", "");
				paramJson.put("devid", "");
				paramJson.put("userid", userInfo.getUserId());
				paramJson.put("ftype", FileType.RECORDING.getValue());
				
				jsonSort.put("modifyTime", "desc");
				paramJson.put("sort", jsonSort);
				
				paramJson.put("start", "0");
				paramJson.put("count", "0");
				
				jsonQstr.put("start", "");
				jsonQstr.put("end", "");
				jsonQstr.put("month", "");
				jsonQstr.put("title", "");
				jsonQstr.put("cnt", searchTextValue);
				paramJson.put("qstr", jsonQstr);
				
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);
				
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				searchHandler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressLint("HandlerLeak")
	public class SearchHandler extends Handler
	{
		private Context context;
		public SearchHandler(Context context) {
			super();
			this.context = context;
		}
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			pd.dismiss();
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			List<FileInfo> files = new ArrayList<FileInfo>();
			if (null == responce) { // 没有网络连接查询android数据库
				Log.i(TAG, "querying from SQLite where network is not support");
				Toast.makeText(context, "连接服务器超时，请稍后再试", Toast.LENGTH_SHORT).show();
			} else {
				try {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						if (0 == responceJson.getJSONArray("list").length()) {
							Toast.makeText(context, "未查询与之相关的内容！", Toast.LENGTH_SHORT).show();
							return;
						}
						files = JsonUtil.FileJsonParse(responceJson.getJSONArray("list"));
					} else {
						Toast.makeText(context, "网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
						return;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (0 != files.size()) {
				Intent intent = new Intent();
				intent.setClass(context, RecordingActivity.class);
				intent.putExtra("searchFiles", (Serializable) files);
				startActivity(intent);
			}
		}
	}
}
