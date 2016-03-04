package com.hanvon.hwepen;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.hanvon.application.AppManage;
import com.hanvon.autolistview.AutoListView;
import com.hanvon.autolistview.AutoListView.OnLoadListener;
import com.hanvon.autolistview.AutoListView.OnRefreshListener;
import com.hanvon.bean.FileInfo;
import com.hanvon.bean.FileInfo.FileType;
import com.hanvon.common.DevCons;
import com.hanvon.common.ServiceWS;
import com.hanvon.hwepen.PopupView.OnItemOnClickListener;
import com.hanvon.splash.SplashActivity;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.FileUtil;
import com.hanvon.util.HttpClientHelper;
import com.hanvon.util.JsonUtil;
import com.hanvon.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ExcerptActivity extends BaseActivity implements OnRefreshListener, OnLoadListener
{
	
	private Context mContext;
	private ImageView back;
	private ImageView create;
	private ImageView search;
	private ImageView mPopup;
	private PopupView mPopupView;
	
	private RelativeLayout slLayout;
	private ImageView searchClose;
	private EditText searchText;
	private String searchTextValue;
	private boolean searchSwitch = false;
	private ProgressDialog pd;
	
	private AutoListView listView;
	
	private ImageView refresh_progress; // 刷新的图片
	private AnimationDrawable anim;
	
	private String fuuid;
	List<FileInfo> files;
	private CategoryAdapter mCategoryAdapter;
	
	private static final String TAG = "ExcerptActivity";
	
	private int time = 0;
	private int count = 8;
	private boolean refreshed = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.excerpt);
		
		init();
		
		initPopupView();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		files = SplashActivity.dbManager.file_queryByUserAndType(MainActivity.curUserId, FileType.EXCERPT.getValue());
	}
//	/**
//	 * 创建测试数据
//	 */
//	@SuppressWarnings("unused")
//	private ArrayList<Category> getData() {
//		ArrayList<Category> listData = new ArrayList<Category>();
//        Category categoryOne = new Category("2013.07");
//        CategoryItem item = new CategoryItem();
//        item.setFuuid("1");
//        item.setDate("2013.07.05");
//        item.setSummary("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456790");
//        categoryOne.addItem(item);
//        
//        CategoryItem item1 = new CategoryItem();
//        item1.setFuuid("1");
//        item1.setDate("2013.07.05");
//        item1.setSummary("中华人民共和国万岁中华人民共和国万岁中华人民共和国万岁中华人民共和国万岁中华人民共和国万岁中华人民共和国万岁");
//        categoryOne.addItem(item1);
//        
//        CategoryItem ite2 = new CategoryItem();
//        ite2.setFuuid("1");
//        ite2.setDate("2013.07.05");
//        ite2.setSummary("fdsafdsalfjdslfjdlsa");
//        categoryOne.addItem(ite2);
//        
//        listData.add(categoryOne);
//        
//		return listData;
//	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			//TODO 监听不到第二次按menu ???
			//mPopupView.show(mPopup);
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (DevCons.searching) {
				DevCons.searching = false;
				slLayout.setVisibility(View.GONE);
				DevCons.searchFiles.clear();
				
				searchSwitch = true;
				
				setAdapterData(files);
				listView.setResultSize(files.size());
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void init() {
		mContext = ExcerptActivity.this;
		back = (ImageView) findViewById(R.id.excerpt_back);
		create = (ImageView) findViewById(R.id.excerpt_create);
		search = (ImageView) findViewById(R.id.excerpt_search);
		mPopup = (ImageView) findViewById(R.id.excerpt_sync);
		listView = (AutoListView) findViewById(R.id.custom_list);
		slLayout = (RelativeLayout) findViewById(R.id.relativeLayout03);
		searchClose = (ImageView) findViewById(R.id.search_del);
		searchText = (EditText) findViewById(R.id.search_cnt);
		refresh_progress = (ImageView) findViewById(R.id.excerpt_refresh_progress);
		anim = (AnimationDrawable) refresh_progress.getBackground();
		
		listView.setOnRefreshListener(this);
		listView.setOnLoadListener(this);
		back.setOnClickListener(listener);
		create.setOnClickListener(listener);
		search.setOnClickListener(listener);
		searchClose.setOnClickListener(listener);
		mPopup.setOnClickListener(listener);
		
		if (DevCons.searching)
		{
			setAdapterData(DevCons.searchFiles);
			listView.setResultSize(DevCons.searchFiles.size());
		}
		else
		{
			Log.d(TAG, "!!!!!! MainActivity.curUserId is " + MainActivity.curUserId);
			DevCons.searchFiles.clear();
			files = SplashActivity.dbManager.file_queryByUserAndType(MainActivity.curUserId, FileType.EXCERPT.getValue());
			setAdapterData(files);
			if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
				//先同步 本地为保存的数据
				refreshStart();
				loadData(AutoListView.REFRESH);
			} else {
				Toast.makeText(mContext, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
			}
//			if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
//				refreshStart(); //动画效果不能放在runnable中
//				new Thread(listThread).start();
//			}
		}
		
		// 监听软键盘回车
		searchText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				Log.i(TAG, actionId + "");
				searchTextValue = searchText.getText().toString();
				if (!StringUtil.isEmpty(searchTextValue)) {
					if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
						closeBoard(mContext);
						pd = ProgressDialog.show(mContext, "", "正在查询......");
						new Thread(searchThread).start();
					} else {
						Toast.makeText(mContext, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
					}
				}
				return false;
			}
		});
		
		//列表点击
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				CategoryItem item = (CategoryItem) mCategoryAdapter.getItem(position-1);
				if(item!=null){
					view.setBackgroundColor(Color.LTGRAY);
				Log.i(TAG, "intent to ExcerptReadActivity fuuid = " + item.getFuuid());
				if (null != item.getFuuid()) {
					if (item.getFuuid().equals(fuuid)) { //删除中阅读
						return;
					}
					Intent intent = new Intent();
					intent.putExtra("fuuid", item.getFuuid());
					intent.setClass(mContext, ExcerptEditActivity.class);
					startActivity(intent);
					finish();
				}
				}
			}
		});
		// 列表长按
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("选项");
				menu.add(0, 0, 0, "删除");
//				menu.add(0, 1, 0, "删除所有");
			}
		});
	}
	//列表长按响应
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item .getMenuInfo();
		CategoryItem cItem = (CategoryItem) mCategoryAdapter.getItem((int) info.id);
		fuuid = cItem.getFuuid();
		if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
			refreshStart();
			new Thread(deleteThread).start();
		} else {
			Toast.makeText(mContext, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
		}
		return super.onContextItemSelected(item);
	}
	
	OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				
			case R.id.excerpt_back:
				if (DevCons.searching) {
					DevCons.searching = false;
					slLayout.setVisibility(View.GONE);
					DevCons.searchFiles.clear();
					
					searchSwitch = true;
					
					setAdapterData(files);
					listView.setResultSize(files.size());
				} else {
					finish();
				}
				break;
				
			case R.id.excerpt_create:
				startActivity(new Intent(mContext, ExcerptEditActivity.class));
				finish();
				break;
				
			case R.id.excerpt_search:
				if (searchSwitch) {
					slLayout.setVisibility(View.VISIBLE);
				}
				break;
				
			case R.id.search_del:
				slLayout.setVisibility(View.GONE);
				break;

			/*
			case R.id.excerpt_more:
				//mPopupView.show(mPopup);
				break;
				*/
				case R.id.excerpt_sync:
				if (new ConnectionDetector(mContext).isConnectingTOInternet())
				{
					refreshStart();
					loadData(AutoListView.REFRESH);
				}
				else
				{
					Toast.makeText(mContext, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
			
		}
	};
	
	private void initPopupView(){
		mPopupView = new PopupView(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		mPopupView.addPopupItem(new PopupItem(this, "abcdefgh......", R.drawable.more_user));
		mPopupView.addPopupItem(new PopupItem(this,
				MainActivity.curUserId.length() > 9 ? (MainActivity.curUserId.substring(0, 9) + "...") : MainActivity.curUserId,
				R.drawable.more_user));
		mPopupView.addPopupItem(new PopupItem(this, "数据同步", R.drawable.more_sysn));
		mPopupView.addPopupItem(new PopupItem(this, "意见反馈", R.drawable.more_feedback));
		mPopupView.addPopupItem(new PopupItem(this, "退出", R.drawable.more_exit));
		
		mPopupView.setItemOnClickListener(new OnItemOnClickListener() {
			
			@Override
			public void onItemClick(PopupItem item, int position) {
				switch (position) {
				case 0:
					Toast.makeText(getApplication(), MainActivity.curUserId + "已登录", Toast.LENGTH_SHORT).show();
					break;
					
				case 1:
					if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
						refreshStart();
						loadData(AutoListView.REFRESH);
					} else {
						Toast.makeText(mContext, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
					}
//					startActivity(new Intent(mContext, ExcerptActivity.class));
//					finish();
					break;
					
				case 2:
					startActivity(new Intent(mContext, FeedbackActivity.class));
					break;
					
				case 3:
					AlertDialog.Builder builder = new Builder(mContext);
					builder.setTitle("" + MainActivity.curUserId);
//					builder.setMessage("123");
					builder.setPositiveButton("关闭程序",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
//									Toast.makeText(getApplication(), "关闭程序", Toast.LENGTH_SHORT).show();
									AppManage.getInstance().exit();
								}
							});
					builder.setNegativeButton("退出登录", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
//									Toast.makeText(getApplication(), "退出登录", Toast.LENGTH_SHORT).show();
									//退出登录 删除本地记录
									SplashActivity.dbManager.user_delete(MainActivity.curUserId);
									AppManage.getInstance().finishPreActivities();
									startActivity(new Intent(mContext, LogInActivity.class));
									finish();
								}
							});
					AlertDialog dialog = builder.create();
					dialog.show();
					break;
					
				default:
					break;
				}
			}
		});
	}
	
//	Runnable listThread = new Runnable() {
//		
//		@Override
//		public void run() {
//			try {
//				String url = ServiceWS.FILE_LIST;
//				JSONObject paramJson = new JSONObject();
//				JSONObject jsonSort = new JSONObject();
//				paramJson.put("uid", "");
//				paramJson.put("sid", "");
//				paramJson.put("ver", "");
//				paramJson.put("userid", MainActivity.curUserId);
//				paramJson.put("devid", "");
//				paramJson.put("ftype", FileType.EXCERPT.getValue());
//				paramJson.put("start", null);
//				paramJson.put("count", null);
//				jsonSort.put("createTime", "desc");  //创建时间降序
//				paramJson.put("sort", jsonSort);
//				String params = paramJson.toString();
//				String responce = HttpClientHelper.sendPostRequest(url, params);
//				
//				Bundle mBundle = new Bundle();
//				mBundle.putString("responce", responce);
//				Message msg = new Message();
//				msg.setData(mBundle);
//				listHandler.sendMessage(msg);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//	};
//	Handler listHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			refreshStop();
//			Bundle bundle = msg.getData();
//			String responce = bundle.getString("responce");
//			if (StringUtil.isEmpty(responce)) {
//				Log.i(TAG, "responce is null");
//				Toast.makeText(mContext, "网络连接不可用，请稍后再试", Toast.LENGTH_SHORT).show();
//			} else {
//				try {
//					JSONObject responceJson = new JSONObject(responce);
//					if (responceJson.get("code").equals("0")) {
//						if (0 == responceJson.getJSONArray("list").length()) {
//							Log.i(TAG, "excerpt list is null");
//							return;
//						}
//						files = JsonUtil.FileJsonParse(responceJson.getJSONArray("list"));
//						Log.i(TAG, "responce list size = " + files.size());
//						//添加到数据库
//						for (FileInfo f : files) {
//							FileInfo oldFile = SplashActivity.dbManager.file_queryById(f.getFuuid());
//							f.setUserId(MainActivity.curUserId);
//							f.setType(FileType.EXCERPT.getValue());
//							f.setSyn("0");
//							if (StringUtil.isEmpty(oldFile.getPath())) {
//								f.setPath(oldFile.getPath());
//							}
//						}
//						SplashActivity.dbManager.files_add(files);
//						
//						setAdapterData(files);
//					} else {
//						Log.e(TAG, "responce code != 0");
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//
//			
//		}
//	};
	
	private void setAdapterData(List<FileInfo> files){
		if (null != files && 0 != files.size()) {
			// ArrayList<Category> listData = getData();
			Collections.sort(files);
			ArrayList<Category> listData = ListFileData.listData(removeDuplicate(files));
			mCategoryAdapter = new CategoryAdapter(mContext, listData);
			// 适配器与ListView绑定
			listView.setAdapter(mCategoryAdapter);
			listView.setSelection(time*count);
			
		}
	}
	
	void refreshStart(){
		refresh_progress.setVisibility(View.VISIBLE);
		anim.start();
		searchSwitch = false;
	}
	void refreshStop(){
		refresh_progress.setVisibility(View.GONE);
		anim.stop();
		searchSwitch = true;
	}
	
	Runnable deleteThread = new Runnable() {

		@Override
		public void run() {
			try {
				String url = ServiceWS.FILE_DELETE;
				JSONObject paramJson = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("ver", "");
				paramJson.put("devid", "");
				paramJson.put("userid", MainActivity.curUserId);
				paramJson.put("ftype", FileType.EXCERPT.getValue());
				paramJson.put("fuid", fuuid);
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);

				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				deleteHandler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};
	Handler deleteHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			refreshStop();
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			try {
				if (null == responce) {
					Toast.makeText(ExcerptActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
				} else {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						Toast.makeText(ExcerptActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
						// 删除成功，同样删除本地保存数据
						FileInfo file = SplashActivity.dbManager.file_queryById(fuuid);
						FileUtil.delFile(file.getPath());
						SplashActivity.dbManager.file_delete(fuuid);
						Log.i(TAG, "delete file success!");
						// 删除成功跳到列表界面
						finish();
						startActivity(new Intent(mContext, ExcerptActivity.class));
					} else {
						Toast.makeText(ExcerptActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
						Log.e(TAG, "responce code != 0");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};
	
	Runnable searchThread = new Runnable() {
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
				paramJson.put("userid", MainActivity.curUserId);
				paramJson.put("ftype", FileType.EXCERPT.getValue());
				
				jsonSort.put("createTime", "desc");
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
	};

	Handler searchHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			pd.dismiss();
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			if (null == responce) { // 没有网络连接查询android数据库
				Log.i(TAG, "network is not support");
				Toast.makeText(ExcerptActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
			} else {
				try {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						if (0 == responceJson.getJSONArray("list").length()) {
							Toast.makeText(mContext, "未查询与之相关的内容！", Toast.LENGTH_SHORT).show();
							return;
						}
						List<FileInfo> resultFiles = JsonUtil.FileJsonParse(responceJson.getJSONArray("list"));
						DevCons.searchFiles = resultFiles;
						
						//搜索到的文件可能没有保存到本地
						for (FileInfo f : resultFiles) {
							f.setUserId(MainActivity.curUserId);
							f.setType(FileType.EXCERPT.getValue());
							f.setSyn("0");
							String path = "/hanvonepen/excerpt/" + MainActivity.curUserId + "/" + f.getFuuid() + ".dat";
							f.setPath(path);
							if (!StringUtil.isEmpty(f.getContent())) {
								if (FileUtil.fileExist(path)) {
									FileUtil.delFile(path);
									FileUtil.createSDFile(path);
									FileUtil.saveTextInSdcard(path, f.getContent());
								} else {
									FileUtil.createSDFile(path);
									FileUtil.saveTextInSdcard(path, f.getContent());
								}
							}
						}
						SplashActivity.dbManager.files_add(resultFiles);
						
//						finish();
//						startActivity(new Intent(mContext, ExcerptActivity.class));
						DevCons.searching = true;
						setAdapterData(DevCons.searchFiles);
						listView.setResultSize(DevCons.searchFiles.size());
					} else {
						Toast.makeText(mContext, "服务不可用，请稍后再试", Toast.LENGTH_SHORT).show();
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					pd.dismiss();
				}
			}
		}
	};

	@Override
	public void onRefresh() {
		searchSwitch = false;
		time = 0;
		loadData(AutoListView.REFRESH);
		
	}
	@Override
	public void onLoad() {
		searchSwitch = false;
		time  = time + 1;
		loadData(AutoListView.LOAD);
		
	}
	
	private void loadData(final int what)
	{
		if (new ConnectionDetector(mContext).isConnectingTOInternet())
		{
			new SyncNative(mContext).syncExcerpt();
		}
		// 这里从服务器获取数据
		new Thread(new Runnable()
		{

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
					paramJson.put("userid", MainActivity.curUserId);
					paramJson.put("ftype", FileType.EXCERPT.getValue());
					
					jsonSort.put("createTime", "desc");
					paramJson.put("sort", jsonSort);
					
					paramJson.put("start", time*count);
					paramJson.put("count", count);
					
					jsonQstr.put("start", "");
					jsonQstr.put("end", "");
					jsonQstr.put("month", "");
					jsonQstr.put("title", "");
					jsonQstr.put("cnt", "");
					paramJson.put("qstr", jsonQstr);
					
					String params = paramJson.toString();
					String responce = HttpClientHelper.sendPostRequest(url, params);
					
					Message msg = handler.obtainMessage();
					msg.what = what;
					msg.obj = responce;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				searchSwitch = true;
				refreshStop();
				List<FileInfo> loadFiles = new ArrayList<FileInfo>();
				String responce = (String) msg.obj;
				switch (msg.what) {
				case AutoListView.REFRESH: // 下拉刷新
					listView.onRefreshComplete();
					if (!StringUtil.isEmpty(responce)) {
						JSONObject responceJson = new JSONObject(responce);
						if (responceJson.get("code").equals("0")) {
							files = JsonUtil.FileJsonParse(responceJson.getJSONArray("list"));
							//添加到数据库 并 保存文件
							for (FileInfo f : files)
							{
								f.setUserId(MainActivity.curUserId);
								f.setType(FileType.EXCERPT.getValue());
								f.setSyn("0");
								String path = "/hanvonepen/excerpt/" + MainActivity.curUserId + "/" + f.getFuuid() + ".dat";
								f.setPath(path);
								if (!StringUtil.isEmpty(f.getContent()))
								{
									if (FileUtil.fileExist(path)) {
										FileUtil.delFile(path);
										FileUtil.createSDFile(path);
										FileUtil.saveTextInSdcard(path, f.getContent());
									} else {
										FileUtil.createSDFile(path);
										FileUtil.saveTextInSdcard(path, f.getContent());
									}
								}
							}
							SplashActivity.dbManager.files_add(files);
						}
					} else {
						Toast.makeText(mContext, "刷新失败", Toast.LENGTH_SHORT).show();
					}
					listView.setResultSize(files.size());
					break;
				case AutoListView.LOAD: // 上拉加载
					listView.onLoadComplete();
					if (!StringUtil.isEmpty(responce)) {
						JSONObject responceJson = new JSONObject(responce);
						if (responceJson.get("code").equals("0")) {
							loadFiles = JsonUtil.FileJsonParse(responceJson.getJSONArray("list"));
							//添加到数据库 并 保存文件
							for (FileInfo f : loadFiles) {
								f.setUserId(MainActivity.curUserId);
								f.setType(FileType.EXCERPT.getValue());
								f.setSyn("0");
								if (!StringUtil.isEmpty(f.getContent())) {
									String path = "/hanvonepen/excerpt/" + MainActivity.curUserId + "/" + f.getFuuid() + ".dat";
									if (FileUtil.fileExist(path)) {
										FileUtil.delFile(path);
										FileUtil.createSDFile(path);
										FileUtil.saveTextInSdcard(path, f.getContent());
									} else {
										FileUtil.createSDFile(path);
										FileUtil.saveTextInSdcard(path, f.getContent());
									}
									f.setPath(path);
								}
								files.add(f);
							}
							SplashActivity.dbManager.files_add(loadFiles);
						}
					} else {
						Toast.makeText(mContext, "加载失败", Toast.LENGTH_SHORT).show();
					}
					listView.setResultSize(loadFiles.size());
					break;
				}
				setAdapterData(files);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	};
	
	private List<FileInfo> removeDuplicate(List<FileInfo> list) {
		Set<String> set = new HashSet<String>();
		List<FileInfo> newList = new ArrayList<FileInfo>();
		for (Iterator<FileInfo> iter = list.iterator(); iter.hasNext();) {
			FileInfo element = (FileInfo) iter.next();
			if (set.add(element.getFuuid()))
				newList.add(element);
		}
		return newList;
	}
	
	public static void closeBoard(Context mcontext) {
		InputMethodManager imm = (InputMethodManager) mcontext.getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
		if (imm.isActive()) {// 一直是true
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
