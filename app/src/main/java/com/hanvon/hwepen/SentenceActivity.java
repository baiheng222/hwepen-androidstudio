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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.hanvon.application.AppManage;
import com.hanvon.bean.FileInfo.FileType;
import com.hanvon.bean.TransInfo;
import com.hanvon.bean.User;
import com.hanvon.common.DevCons;
import com.hanvon.common.ServiceWS;
import com.hanvon.db.DBManager;
import com.hanvon.util.HttpClientHelper;
import com.hanvon.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SentenceActivity extends BaseActivity
{
	
	private ImageView back;
	private ImageView home;
	private ListView listView;
	private ImageView search;
	private ImageView share;
	private ImageView synch;
	
	private ListHandler handler;
	private DBManager dbManager;
	private User userInfo;
	private ProgressDialog pd;
	private SentenceAdapter mAdapter;
	private static final String TAG = "SentenceActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.sentence);
		
		handler = new ListHandler();
		dbManager = new DBManager(this);
//		userInfo = dbManager.user_currentUser();
		
		back = (ImageView) findViewById(R.id.sentence_back);
		home = (ImageView) findViewById(R.id.sentence_home);
		listView = (ListView) findViewById(R.id.sentence_list);
		search = (ImageView) findViewById(R.id.sb_search);
		share = (ImageView) findViewById(R.id.sb_share);
		synch = (ImageView) findViewById(R.id.sb_synchro);
		
		////接收整句查询过来的参数            如果为空则非查询显示全部项
		@SuppressWarnings("unchecked")
		List<TransInfo> sentences = (List<TransInfo>) getIntent().getSerializableExtra("searchFiles");
		if (null == sentences) { //全部显示
			pd = ProgressDialog.show(SentenceActivity.this, "", "正在查询......");
			ListThread thread = new ListThread(null);
			new Thread(thread).start();
		} else {
			ArrayList<TransInfo> listData = (ArrayList<TransInfo>) sentences;
        	mAdapter = new SentenceAdapter(SentenceActivity.this, listData);
			listView.setAdapter(mAdapter);
		}
		
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SentenceActivity.this, SentenceSearchActivity.class));
			}
		});
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "hwCloud");
                intent.putExtra(Intent.EXTRA_TEXT, DevCons.SHARE);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                SentenceActivity.this.startActivity(Intent.createChooser(intent, "分享"));
			}
		});
		synch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SentenceActivity.this.setResult(RESULT_OK, null);
				SentenceActivity.this.finish();
				startActivity(new Intent(SentenceActivity.this, SentenceActivity.class));
			}
		});
		
		//列表点击进入详细页
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				TransInfo item = mAdapter.getItem(position);
				Log.i(TAG, "intent to SentenceReadActivity uuid = " + item.getUuid());
				if (null != item.getUuid() && null != item.getWord()) {
					Intent intent = new Intent();
//					Bundle bundle = new Bundle();
					intent.putExtra("uuid", item.getUuid());
					intent.putExtra("sentence", item.getWord());
					intent.putExtra("translate", item.getTrans());
					intent.setClass(SentenceActivity.this, SentenceReadActivity.class);
					startActivity(intent);
				}
			}

		});
		
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SentenceActivity.this.setResult(RESULT_OK, null);
				SentenceActivity.this.finish();
			}
		});
		
		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SentenceActivity.this, MainActivity.class));
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
	
	private class ListThread implements Runnable
	{
		private String search;
		public ListThread(String search) {
			super();
			this.search = search;
		}
		@Override
		public void run() {
			try {
				String url = ServiceWS.SENTENCE_SEARCH;
				JSONObject paramJson = new JSONObject();
				JSONObject qstrJson = new JSONObject();
				JSONObject sortJson = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("ver", "");
				paramJson.put("devid", "");
				paramJson.put("userid", userInfo.getUserId());
				paramJson.put("start", "");
				paramJson.put("count", "1000");
				paramJson.put("ftype", FileType.SENTENCE.getValue());
				sortJson.put("date", "desc");
				paramJson.put("sort", sortJson);
				qstrJson.put("start", "");
				qstrJson.put("end", "");
				qstrJson.put("month", "");
				qstrJson.put("sentence", search);
				qstrJson.put("trans", "");
				paramJson.put("qstr", qstrJson);
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);
				
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				handler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	private class ListHandler extends Handler
	{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            pd.dismiss();
            Bundle bundle = msg.getData();
            String responce = bundle.getString("responce");
            List<TransInfo> sentences = new ArrayList<TransInfo>();
            if (null == responce) { //没有网络连接查询android数据库
            	Log.i(TAG, "querying from SQLite where network is not support");
            	Toast.makeText(SentenceActivity.this, "连接服务器超时，请稍后再试", Toast.LENGTH_SHORT).show();
            	//query from SQLite
            	sentences = dbManager.trans_queryByUserAndType(userInfo.getUserId(), FileType.SENTENCE.getValue());
			} else {
				try {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						if (0 == responceJson.getJSONArray("list").length()) {
							Toast.makeText(SentenceActivity.this, "没有整句内容！", Toast.LENGTH_SHORT).show();
							return;
						}
						sentences = JsonUtil.SentenceJsonParse(responceJson.getJSONArray("list"));
						//将数据添加android数据库
						Log.i(TAG, "conneting SQLite and set ExcerptListAdapter");
						for (TransInfo s : sentences) {
							dbManager.trans_delete(s.getUuid()); //删除原来记录，保证数据是最新的
							s.setUserId(userInfo.getUserId());
							s.setType(FileType.SENTENCE.getValue());
						}
						dbManager.transList_add(sentences);
					} else {
						Toast.makeText(SentenceActivity.this, "网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
						return;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
            
            if (0 != sentences.size()) {
            	ArrayList<TransInfo> listData = (ArrayList<TransInfo>) sentences;
            	mAdapter = new SentenceAdapter(SentenceActivity.this, listData);
				listView.setAdapter(mAdapter);
			}
        }
	}
}