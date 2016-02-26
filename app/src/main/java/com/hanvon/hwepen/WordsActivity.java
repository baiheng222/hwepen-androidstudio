package com.hanvon.hwepen;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordsActivity extends BaseActivity
{
	
	private ImageView back;
	private ImageView home;
	private ImageView wordsAll;
	private ImageView wordsHigh;
	private ImageView wordsMiddle;
	private ImageView wordsLow;
	private ImageView wordsRemember;
	
	private TextView word;
	private TextView times; //次数
	private GridView gridView;
	private ImageView imageLeft;
	private ImageView imageRigth;
	private TextView paraphrase; //单词释义
	private ImageView hidden;
	private ImageView master;
	
	private ImageView search;
	private ImageView share;
	private ImageView synch;
	
	private Integer left = 0;
	private Integer right = 0;
	private Map<Integer, List<TransInfo>> listMap;
	
	private List<String> remWords;
	private boolean hiddenValue = true;
	private String wordId;
	private WordsListHandler wlHandler;
	private WordMasterHandler wmHandler;
	private DBManager dbManager;
	private User userInfo;
	private ProgressDialog pd;
	private static final String TAG = "WordsActivity";
	
	//手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)  
    float x1 = 0;  
    float x2 = 0;  
    float y1 = 0;  
    float y2 = 0;

	@SuppressLint("UseSparseArrays")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.words);
		
		wlHandler = new WordsListHandler(this);
		wmHandler = new WordMasterHandler(this);
		dbManager = new DBManager(this);
//		userInfo = dbManager.user_currentUser();
		listMap = new HashMap<Integer, List<TransInfo>>();
		remWords = new ArrayList<String>();
		
		back = (ImageView) findViewById(R.id.words_back);
		home = (ImageView) findViewById(R.id.words_home);
		search = (ImageView) findViewById(R.id.wb_search);
		share = (ImageView) findViewById(R.id.wb_share);
		synch = (ImageView) findViewById(R.id.wb_synchro);
		wordsAll = (ImageView) findViewById(R.id.words_all);
		wordsHigh = (ImageView) findViewById(R.id.words_high);
		wordsMiddle = (ImageView) findViewById(R.id.words_middle);
		wordsLow = (ImageView) findViewById(R.id.words_low);
		wordsRemember = (ImageView) findViewById(R.id.words_remember);
		word = (TextView) findViewById(R.id.words);
		times = (TextView) findViewById(R.id.words_count);
		gridView = (GridView) findViewById(R.id.words_gridView);
		imageLeft = (ImageView) findViewById(R.id.words_left);
		imageRigth = (ImageView) findViewById(R.id.words_right);
		paraphrase = (TextView) findViewById(R.id.words_paraphrase);
		hidden = (ImageView) findViewById(R.id.words_hidden);
		master = (ImageView) findViewById(R.id.words_master);
		
		////接收单词查询过来的参数            如果为空则非查询显示全部项
		@SuppressWarnings("unchecked")
		List<TransInfo> searchWords = (List<TransInfo>) getIntent().getSerializableExtra("searchWords");
		if (null == searchWords) {
			pd = ProgressDialog.show(WordsActivity.this, "", "请稍后......");
			WordsListThread thread = new WordsListThread("1", "1000", "");
			new Thread(thread).start();
		} else {
			setWordsAdapter(searchWords);
		}
		
		
		wordsAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pd = ProgressDialog.show(WordsActivity.this, "", "请稍后......");
				WordsListThread thread = new WordsListThread("1", "1000", "");
				new Thread(thread).start();
				initImageView();
				wordsAll.setImageDrawable(getResources().getDrawable(R.drawable.all1));
				paraphrase.setText("");
			}
		});
		wordsHigh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pd = ProgressDialog.show(WordsActivity.this, "", "请稍后......");
				WordsListThread thread = new WordsListThread("8", "1000", "1");
				new Thread(thread).start();
				initImageView();
				wordsHigh.setImageDrawable(getResources().getDrawable(R.drawable.gao1));
				paraphrase.setText("");
			}
		});
		wordsMiddle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pd = ProgressDialog.show(WordsActivity.this, "", "请稍后......");
				WordsListThread thread = new WordsListThread("4", "7", "1");
				new Thread(thread).start();
				initImageView();
				wordsMiddle.setImageDrawable(getResources().getDrawable(R.drawable.zhong1));
				paraphrase.setText("");
			}
		});
		wordsLow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pd = ProgressDialog.show(WordsActivity.this, "", "请稍后......");
				WordsListThread thread = new WordsListThread("1", "3", "1");
				new Thread(thread).start();
				initImageView();
				wordsLow.setImageDrawable(getResources().getDrawable(R.drawable.di));
				paraphrase.setText("");
			}
		});
		wordsRemember.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pd = ProgressDialog.show(WordsActivity.this, "", "请稍后......");
				WordsListThread thread = new WordsListThread("1", "1000", "0");
				new Thread(thread).start();
				initImageView();
				wordsRemember.setImageDrawable(getResources().getDrawable(R.drawable.hui1));
				paraphrase.setText("");
			}
		});
		
		//监听单词点击事件
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Map<String, Object> data = (Map<String, Object>) gridView.getItemAtPosition(position);
				TransInfo tf = (TransInfo) data.get("wordInfo");
				if (null != tf.getTrans()) {
					paraphrase.setText(tf.getTrans());
					times.setText("扫描次数：" + tf.getCount());
					word.setText(tf.getWord());
				}
				//点击单元格改变背景
				for (int i = 0; i < parent.getCount(); i++) {
					Log.i(TAG, parent.getCount() + "");
					View v = parent.getChildAt(i);
					if (position == i) {
//						view.setBackground(getResources().getDrawable(R.drawable.lv));
						view.setBackgroundDrawable(getResources().getDrawable(R.drawable.lv));
					} else {
//						v.setBackground(getResources().getDrawable(R.drawable.lv1));
						v.setBackgroundDrawable(getResources().getDrawable(R.drawable.lv1));
					}
				}
				Log.i(TAG, tf.getUuid());
				Log.i(TAG, tf.getIsMaster());
				wordId = tf.getUuid();
				//如果单词未掌握，则显示显示 "记住了"选项
				if (tf.getIsMaster().equals("1")) {
					if (null!=remWords && remWords.contains(wordId)) {
						master.setVisibility(View.GONE);
					} else {
						master.setVisibility(View.VISIBLE);
					}
				} else {
					master.setVisibility(View.GONE);
				}
			}
		});
		gridView.setOnTouchListener(new OnTouchListener() { //gridView屏蔽了onTouchEvent 故将其传过去
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				WordsActivity.this.onTouchEvent(event);
				return false;
			}
		});
		
		//左翻页
		imageLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (0 != left) {
					left -= 1;
					right += 1;
					wordsAdapter(listMap.get(left), WordsActivity.this);
				} else {
					Toast.makeText(WordsActivity.this, "无更多页显示", Toast.LENGTH_SHORT).show();
				}
			}
		});
		//右翻页
		imageRigth.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (0 != right) {
					right -= 1;
					left += 1;
					wordsAdapter(listMap.get(left), WordsActivity.this);
				} else {
					Toast.makeText(WordsActivity.this, "无更多页显示", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		hidden.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (hiddenValue) {
					hidden.setBackgroundDrawable(getResources().getDrawable(R.drawable.xianshi));
					paraphrase.setVisibility(View.GONE);
					hiddenValue = false;
				} else {
					hidden.setBackgroundDrawable(getResources().getDrawable(R.drawable.yin));
					paraphrase.setVisibility(View.VISIBLE);
					hiddenValue = true;
				}
			}
		});
		
		master.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WordMasterThread t = new WordMasterThread(wordId);
				new Thread(t).start();
			}
		});
		
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WordsActivity.this.setResult(RESULT_OK, null);
				WordsActivity.this.finish();
			}
		});
		
		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(WordsActivity.this, MainActivity.class));
			}
		});
		
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(WordsActivity.this, WordsSearchActivity.class));
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
                WordsActivity.this.startActivity(Intent.createChooser(intent, "分享"));
			}
		});
		synch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WordsActivity.this.setResult(RESULT_OK, null);
				WordsActivity.this.finish();
				startActivity(new Intent(WordsActivity.this, WordsActivity.class));
			}
		});
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        //继承了Activity的onTouchEvent方法，直接监听点击事件
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候  
            x1 = event.getX();  
            y1 = event.getY();  
        }  
        if(event.getAction() == MotionEvent.ACTION_UP) {
            //当手指离开的时候  
            x2 = event.getX();  
            y2 = event.getY();  
            if(y1 - y2 > 50) {  
                //Toast.makeText(WordsActivity.this, "向上滑", Toast.LENGTH_SHORT).show();  
            } else if(y2 - y1 > 50) {  
                //Toast.makeText(WordsActivity.this, "向下滑", Toast.LENGTH_SHORT).show();  
            } else if(x1 - x2 > 50) {  
                //Toast.makeText(WordsActivity.this, "向左滑", Toast.LENGTH_SHORT).show();  
            	if (0 != right) {
					right -= 1;
					left += 1;
					wordsAdapter(listMap.get(left), WordsActivity.this);
				}
            } else if(x2 - x1 > 50) {
                //Toast.makeText(WordsActivity.this, "向右滑", Toast.LENGTH_SHORT).show();  
            	if (0 != left) {
					left -= 1;
					right += 1;
					wordsAdapter(listMap.get(left), WordsActivity.this);
				}
            }  
        }  
        return super.onTouchEvent(event);  
    }  
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		this.finish();
		super.onDestroy();
	}
	
	public class WordsListThread implements Runnable
	{
		private String countlow;
		private String counthigh;
		private String grasp;
		public WordsListThread(String countlow, String counthigh, String grasp) {
			this.countlow = countlow;
			this.counthigh = counthigh;
			this.grasp = grasp;
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
				qstrJson.put("countlow", countlow);
				qstrJson.put("counthigh", counthigh);
				qstrJson.put("grasp", grasp); //0掌握  1没掌握  ""全部
				qstrJson.put("words", "");
				qstrJson.put("trans", "");
				paramJson.put("qstr", qstrJson);
				
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);

				Message message = new Message();
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				message.setData(mBundle);
                wlHandler.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	public class WordsListHandler extends Handler
	{
		Context context;
		public WordsListHandler(Context context) {
			this.context = context;
		}
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			pd.dismiss();
			master.setVisibility(View.GONE);
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			List<TransInfo> words = new ArrayList<TransInfo>();
			try {
				if (null == responce) { // 没有网络连接先存到手机
					Log.i(TAG, "saving to SDCard where network is not support");
					Toast.makeText(WordsActivity.this, "连接服务器超时，请稍后再试", Toast.LENGTH_SHORT).show();
					words = dbManager.trans_queryByUserAndType(userInfo.getUserId(), FileType.WORDS.getValue());
				} else {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						
						if (0 == responceJson.getJSONArray("list").length()) {
							Toast.makeText(WordsActivity.this, "没有单词内容！", Toast.LENGTH_SHORT).show();
							return;
						}
						
						words = JsonUtil.WordJsonParse(responceJson.getJSONArray("list"));
						for (TransInfo word : words) {
							dbManager.trans_delete(word.getUuid());
							word.setUserId(userInfo.getUserId());
							word.setType(FileType.WORDS.getValue());
							dbManager.trans_add(word);
						}
						Log.i(TAG, "save to SQLite success");
					} else {
						Toast.makeText(context, "网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			setWordsAdapter(words);
//			if (null !=  words && 0 != words.size()) {
//				//将查询出来的words按页个数存入map
//				int size = 15; //显示的个数
//				int tmp = (words.size()-1) / size;
//				right = tmp;
//				for (int i = 0; i <= tmp; i++) {
//					if (i == tmp) {
//						listMap.put(i, words.subList(size * i, words.size()));
//					} else {
//						listMap.put(i, words.subList(size * i, size * i + size));
//					}
//				}
//				wordsAdapter(listMap.get(0), WordsActivity.this);
//			} else { //无数据清空gridView
//				gridView.setAdapter(null);
//			}
		}	
	}
	
	private void setWordsAdapter(List<TransInfo> words){
		if (null !=  words && 0 != words.size()) {
			//将查询出来的words按页个数存入map
			int size = 15; //显示的个数
			int tmp = (words.size()-1) / size;
			right = tmp;
			for (int i = 0; i <= tmp; i++) {
				if (i == tmp) {
					listMap.put(i, words.subList(size * i, words.size()));
				} else {
					listMap.put(i, words.subList(size * i, size * i + size));
				}
			}
			wordsAdapter(listMap.get(0), WordsActivity.this);
		} else { //无数据清空gridView
			gridView.setAdapter(null);
		}
	}
			
	private void wordsAdapter(List<TransInfo> words, Context context) {
		List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
		for (TransInfo word : words) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("textItem", word.getWord());
			item.put("wordInfo", word);
			items.add(item);
		}
		//实例化一个适配器
		SimpleAdapter mAdapter = new SimpleAdapter(context, items, R.layout.words_grid_item, new String[]{"textItem"}, new int[]{R.id.words_item});
		gridView.setAdapter(mAdapter);
	}
	
	private void initImageView(){
		wordsAll.setImageDrawable(getResources().getDrawable(R.drawable.all));
		wordsHigh.setImageDrawable(getResources().getDrawable(R.drawable.gao));
		wordsMiddle.setImageDrawable(getResources().getDrawable(R.drawable.zhong));
		wordsLow.setImageDrawable(getResources().getDrawable(R.drawable.di1));
		wordsRemember.setImageDrawable(getResources().getDrawable(R.drawable.hui));
		left = 0;
		right = 0;
		listMap.clear();
		word.setText("显示单词");
		times.setText("");
		gridView.setAdapter(null);
	}
	
	private class WordMasterThread implements Runnable
	{
		private String wordId;
		private WordMasterThread(String wordId) {
			this.wordId = wordId;
		}
		@Override
		public void run() {
			try {
				String url = ServiceWS.WORDS_UPDATE;
				JSONObject paramJson = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("ver", "");
				paramJson.put("devid", "");
				paramJson.put("userid", userInfo.getUserId());
				paramJson.put("fuid", wordId);
				paramJson.put("grasp", "0");
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);

				Message message = new Message();
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				message.setData(mBundle);
                wmHandler.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	public class WordMasterHandler extends Handler
	{
		Context context;
		public WordMasterHandler(Context context) {
			this.context = context;
		}
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			try {
				if (null == responce) { //没有网络连接
					Toast.makeText(WordsActivity.this, "连接服务器超时，请稍后再试", Toast.LENGTH_SHORT).show();
				} else {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						master.setVisibility(View.GONE);
						remWords.add((String) (responceJson.get("fuid")));
					} else {
						Toast.makeText(context, "网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
