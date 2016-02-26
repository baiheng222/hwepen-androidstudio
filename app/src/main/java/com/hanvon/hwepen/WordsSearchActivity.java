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
import com.hanvon.bean.FileInfo.FileType;
import com.hanvon.bean.TransInfo;
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

public class WordsSearchActivity extends BaseActivity
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
	private static final String TAG = "WordsSearchActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.words_search);
		
		
		searchHandler = new SearchHandler(this);
		dbManager = new DBManager(this);
		
		back = (ImageView) findViewById(R.id.words_search_back);
		home = (ImageView) findViewById(R.id.words_search_home);
		searchText = (EditText) findViewById(R.id.words_search_text);
		search = (ImageView) findViewById(R.id.words_search);
		clear = (ImageView) findViewById(R.id.words_search_clear);
		
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
					pd = ProgressDialog.show(WordsSearchActivity.this, "", "正在查询......");
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
					pd = ProgressDialog.show(WordsSearchActivity.this, "", "正在查询......");
					SearchThread thread = new SearchThread(searchTextValue);
					new Thread(thread).start();
				}
				return false;
			}
		});
		
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WordsSearchActivity.this.setResult(RESULT_OK, null);
				WordsSearchActivity.this.finish();
			}
		});
		
		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(WordsSearchActivity.this, MainActivity.class));
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
				String url = ServiceWS.WORDS_SEARCH;
				JSONObject paramJson = new JSONObject();
				JSONObject qstrJson = new JSONObject();
				JSONObject sortJson = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("ver", "");
				paramJson.put("userid", userInfo.getUserId());
				paramJson.put("devid", "");
				paramJson.put("start", "");
				paramJson.put("count", "1000");
				paramJson.put("ftype", FileType.WORDS.getValue());
				sortJson.put("date", "desc");
				paramJson.put("sort", sortJson);
				qstrJson.put("start", "");
				qstrJson.put("end", "");
				qstrJson.put("month", "");
				qstrJson.put("countlow", "0");
				qstrJson.put("counthigh", "10000");
				qstrJson.put("grasp", ""); //0掌握  1没掌握  ""全部
				qstrJson.put("words", searchTextValue);
				qstrJson.put("trans", "");
				paramJson.put("qstr", qstrJson);
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
			List<TransInfo> words = new ArrayList<TransInfo>();
			if (null == responce) {
				Log.i(TAG, "network is not support");
				Toast.makeText(context, "连接服务器超时，请稍后再试", Toast.LENGTH_SHORT).show();
			} else {
				try {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						if (0 == responceJson.getJSONArray("list").length()) {
							Toast.makeText(context, "未查询与之相关的内容！", Toast.LENGTH_SHORT).show();
							return;
						}
						words = JsonUtil.WordJsonParse(responceJson.getJSONArray("list"));
					} else {
						Toast.makeText(context, "网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
						return;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (0 != words.size()) {
				Intent intent = new Intent();
				intent.setClass(context, WordsActivity.class);
				intent.putExtra("searchWords", (Serializable) words);
				startActivity(intent);
			}
		}
	}
}
