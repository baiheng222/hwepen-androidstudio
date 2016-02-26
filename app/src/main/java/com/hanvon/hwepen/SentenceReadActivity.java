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
import com.hanvon.bean.FileInfo.FileType;
import com.hanvon.bean.User;
import com.hanvon.common.DevCons;
import com.hanvon.common.ServiceWS;
import com.hanvon.db.DBManager;
import com.hanvon.util.HttpClientHelper;
import com.hanvon.util.JsonUtil;
import com.hanvon.widget.ObservableScrollView;
import com.hanvon.widget.ScrollViewListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SentenceReadActivity extends BaseActivity implements ScrollViewListener
{
	
	private ImageView back;
	private ImageView home;
	private TextView original;
	private TextView translate;
	private ImageView delete;
//	private ImageView edit;
	private ImageView search;
	private ImageView share;
	
	private String uuid;
	private DBManager dbManager;
	private DeleteHandler delHandler;
	private EditHandler editHandler;
	private User userInfo;
	private ProgressDialog pd;
	private static final String TAG = "SentenceReadActivity";
	
	private ObservableScrollView scrollView1 = null;
    private ObservableScrollView scrollView2 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.sentence_read);
		
		delHandler = new DeleteHandler();
		editHandler = new EditHandler();
		dbManager = new DBManager(this);
//		userInfo = dbManager.user_currentUser();
		
		back = (ImageView) findViewById(R.id.sentence_read_back);
		home = (ImageView) findViewById(R.id.sentence_read_home);
		original = (TextView) findViewById(R.id.sentence_read_original);
		translate = (TextView) findViewById(R.id.sentence_read_translate);
		delete = (ImageView) findViewById(R.id.sentence_read_original_delete);
//		edit = (ImageView) findViewById(R.id.sentence_read_translate_edit);
		search = (ImageView) findViewById(R.id.srb_search);
		share = (ImageView) findViewById(R.id.srb_share);
		
		//接收整句列表传递过来的参数
		uuid = getIntent().getStringExtra("uuid");
		original.setText(getIntent().getStringExtra("sentence"));
		translate.setText(getIntent().getStringExtra("translate"));
		
		//删除监听
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pd = ProgressDialog.show(SentenceReadActivity.this, "", "请稍后......");
				DeleteThread delThread = new DeleteThread();
				new Thread(delThread).start();
			}
		});
		
		//编辑监听
//		edit.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Toast.makeText(SentenceReadActivity.this, "waiting......", Toast.LENGTH_SHORT).show();
//				EditThread editThread = new EditThread();
//				new Thread(editThread).start();
//			}
//		});
		
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SentenceReadActivity.this.setResult(RESULT_OK, null);
				SentenceReadActivity.this.finish();
			}
		});
		
		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SentenceReadActivity.this, MainActivity.class));
			}
		});
		
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SentenceReadActivity.this, SentenceSearchActivity.class));
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
                SentenceReadActivity.this.startActivity(Intent.createChooser(intent, "分享"));
			}
		});
		
		scrollView1 = (ObservableScrollView) findViewById(R.id.sentence_read_original_scrollView);
		scrollView1.setScrollViewListener(this);
		scrollView2 = (ObservableScrollView) findViewById(R.id.sentence_read_translate_scrollView);
		scrollView2.setScrollViewListener(this);
	}
	public void onScrollChanged(ObservableScrollView scrollView, int x, int y,
			int oldx, int oldy) {
		if (0 >= y) {
			return;
		}
		int length1 = original.getHeight()-scrollView1.getHeight(); //textView总长而非scrollView总长
		int length2 = translate.getHeight()-scrollView2.getHeight();
		float tmp = (float)length1/length2;
		
		if (scrollView == scrollView1) {
			scrollView2.scrollTo(x, Math.round(y / tmp));
		} else if (scrollView == scrollView2) {
			scrollView1.scrollTo(x, Math.round(y * tmp));
		}
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
	
	private class DeleteThread implements Runnable
	{
		@Override
		public void run() {
			try {
				String url = ServiceWS.SENTENCE_DELETE;
				JSONObject paramJson = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("ver", "");
				paramJson.put("devid", "");
				paramJson.put("userid", "Lucy0001");
				paramJson.put("fuid", uuid);
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);
				
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				delHandler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	private class DeleteHandler extends Handler
	{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            pd.dismiss();
            Bundle bundle = msg.getData();
            String responce = bundle.getString("responce");
            if (null == responce) {
            	Toast.makeText(SentenceReadActivity.this, "连接服务器超时，请稍后再试", Toast.LENGTH_SHORT).show();
			} else {
				try {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						Toast.makeText(SentenceReadActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
						startActivity(new Intent(SentenceReadActivity.this, SentenceActivity.class));
					} else {
						Toast.makeText(SentenceReadActivity.this, "删除失败，请稍后再试", Toast.LENGTH_SHORT).show();
						return;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
        }
	}
	
	@SuppressLint("HandlerLeak")
	private class EditThread implements Runnable
	{
		@Override
		public void run() {
			try {
				String url = ServiceWS.SENTENCE_DELETE;
				JSONObject paramJson = new JSONObject();
				JSONObject qstrJson = new JSONObject();
				JSONObject sortJson = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("ver", "");
				paramJson.put("devid", "");
				paramJson.put("userid", "Lucy0001");
				paramJson.put("start", "");
				paramJson.put("count", "");
				paramJson.put("ftype", FileType.SENTENCE.getValue());
				sortJson.put("date", "desc");
				paramJson.put("sort", sortJson);
				qstrJson.put("start", "");
				qstrJson.put("end", "");
				qstrJson.put("month", "");
				qstrJson.put("sentence", "");
				qstrJson.put("trans", "");
				paramJson.put("qstr", qstrJson);
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);
				
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				editHandler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	private class EditHandler extends Handler
	{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            pd.dismiss();
            Bundle bundle = msg.getData();
            String responce = bundle.getString("responce");
            List<FileInfo> files = new ArrayList<FileInfo>();
            if (null == responce) { //没有网络连接查询android数据库
            	Log.i(TAG, "querying from SQLite where network is not support");
            	Toast.makeText(SentenceReadActivity.this, "连接服务器超时，请稍后再试", Toast.LENGTH_SHORT).show();
//				files = dbManager.file_queryAll();
			} else {
				try {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						if (0 == responceJson.getJSONArray("list").length()) {
							Toast.makeText(SentenceReadActivity.this, "没有摘抄内容！", Toast.LENGTH_SHORT).show();
							return;
						} 
						files = JsonUtil.FileJsonParse(responceJson.getJSONArray("list"));
						//将数据添加android数据库
						Log.i(TAG, "conneting SQLite and set ExcerptListAdapter");
						for (FileInfo f : files) {
							dbManager.file_delete(f.getFuuid()); //删除原来记录，保证数据是最新的
							f.setUserId(userInfo.getUserId());
							f.setType(FileType.EXCERPT.getValue());
							f.setSyn("1");
						}
						dbManager.files_add(files);
					} else {
						Toast.makeText(SentenceReadActivity.this, "网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
						return;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
        }
	}
}
