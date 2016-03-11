package com.hanvon.hwepen;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.hanvon.application.AppManage;
import com.hanvon.autolistview.AutoListView;
import com.hanvon.autolistview.AutoListView.OnLoadListener;
import com.hanvon.autolistview.AutoListView.OnRefreshListener;
import com.hanvon.bean.FileInfo;
import com.hanvon.bean.FileInfo.FileType;
import com.hanvon.common.ServiceWS;
import com.hanvon.hwepen.PopupView.OnItemOnClickListener;
import com.hanvon.splash.SplashActivity;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.FileUtil;
import com.hanvon.util.HttpClientHelper;
import com.hanvon.util.JsonUtil;
import com.hanvon.util.SDCardStatus;
import com.hanvon.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RecordingActivity extends BaseActivity implements OnRefreshListener, OnLoadListener
{
	
	private Context mContext;
	private ImageView back;
	private ImageView mSync;
	private PopupView mPopupView;
	private AutoListView listView;
	private ImageView refresh_progress; // 刷新的图片
	private AnimationDrawable anim;
	
	private String fuuid;
	public static List<FileInfo> files;
	private RecordingCategoryAdapter mCategoryAdapter;
	private static final String TAG = "RecordingActivity";
	
	private int time = 0;
	private int count = 8;
	
//	private int index = 0;
//	private boolean flag = true;
//	private TextView rate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.recording);
		
		try {
			
			init();
			
			initPopupView();
			
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}
	
	private void init() throws IOException
	{
		mContext = RecordingActivity.this;
		back = (ImageView) findViewById(R.id.recording_back);
		mSync = (ImageView) findViewById(R.id.recording_sync);
		listView = (AutoListView) findViewById(R.id.recording_list);
		refresh_progress = (ImageView) findViewById(R.id.recording_refresh_progress);
		anim = (AnimationDrawable) refresh_progress.getBackground();
		
		listView.setOnRefreshListener(this);
		listView.setOnLoadListener(this);
		back.setOnClickListener(listener);
		mSync.setOnClickListener(listener);
		
		files = SplashActivity.dbManager.file_queryByUserAndType(MainActivity.curUserId, FileType.RECORDING.getValue());
		setAdapterData(files);
		if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
			refreshStart();
			loadData(AutoListView.REFRESH);
		} else {
			Toast.makeText(mContext, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
		}
//		if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
//			refreshStart(); //动画效果不能放在runnable中
//			new Thread(listThread).start();
//		}
		
		/**
	     * 监听recording List点击事件
	     * 1、检查sd卡是否存在、不存在则不支持音频播放
	     * 2、从sd卡中读取音频文件
	     * 3、若sd卡中不存在此文件，则从服务器下载
	     */
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (SDCardStatus.existSDCard()) {
					CategoryItem item = (CategoryItem) mCategoryAdapter.getItem(position-1);
					if(item!=null){
						view.setBackgroundColor(Color.LTGRAY);
						Intent intent = new Intent();
						intent.setClass(mContext, RecordingPlayActivity.class);
						intent.putExtra("fuuid", item.getFuuid());
						startActivity(intent);
						finish();
					}
					
				} else {
					Toast.makeText(mContext, "SD卡不存在，无法播放录音文件，请检查SD卡", Toast.LENGTH_SHORT).show();
				}
				
//				CategoryItem item = (CategoryItem) mCategoryAdapter.getItem(position);
//				if (null != item.getFuuid()) {
//					if (item.getFuuid().equals(fuuid)) { //删除中阅读
//						return;
//					}
//					Intent intent = new Intent();
//					intent.putExtra("fuuid", item.getFuuid());
//					intent.setClass(mContext, ExcerptEditActivity.class);
//					startActivity(intent);
//					finish();
//				}
			}
		});
		// 列表长按
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("选项");
				menu.add(0, 0, 0, "删除");
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
				
			case R.id.recording_back:
				finish();
				break;
				
			case R.id.recording_sync:
				//mPopupView.show(mPopup);
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
//		mPopupView.addPopupItem(new PopupItem(this, MainActivity.curUserId, R.drawable.more_user));
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
									AppManage.getInstance().exit();
								}
							});
					builder.setNegativeButton("退出登录", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
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
	
	private void setAdapterData(List<FileInfo> files)
	{
		if (null != files && 0 != files.size())
		{
			// ArrayList<Category> listData = getData();
			Collections.sort(files);
			ArrayList<Category> listData = ListFileData.listData(removeDuplicate(files));
			mCategoryAdapter = new RecordingCategoryAdapter(RecordingActivity.this, listData);
			// 适配器与ListView绑定
			listView.setAdapter(mCategoryAdapter);
			listView.setSelection(time*count);
		}
	}
	
	void refreshStart(){
		refresh_progress.setVisibility(View.VISIBLE);
		anim.start();
	}
	void refreshStop(){
		refresh_progress.setVisibility(View.GONE);
		anim.stop();
	}
	//获取列表
	Runnable listThread = new Runnable() {

		@Override
		public void run() {
			try {
				String url = ServiceWS.FILE_LIST;
				JSONObject paramJson = new JSONObject();
				JSONObject jsonSort = new JSONObject();
				paramJson.put("uid", "");
				paramJson.put("sid", "");
				paramJson.put("ver", "");
				paramJson.put("userid", MainActivity.curUserId);
				paramJson.put("devid", "");
				paramJson.put("ftype", FileType.RECORDING.getValue());
				paramJson.put("start", null);
				paramJson.put("count", null);
				jsonSort.put("createTime", "desc");
				paramJson.put("sort", jsonSort);
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);

				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				listHandler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

	Handler listHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			refreshStop();
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			if (null == responce) {
				Log.i(TAG, "responce is null");
				Toast.makeText(mContext, "网络连接不可用，请稍后再试", Toast.LENGTH_SHORT) .show();
			} else {
				try {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						if (0 == responceJson.getJSONArray("list").length()) {
							Toast.makeText(RecordingActivity.this, "没有录音内容", Toast.LENGTH_SHORT).show();
							return;
						}
						files = JsonUtil.FileJsonParse(responceJson.getJSONArray("list"));
						Log.i(TAG, "responce list size = " + files.size());
						// 将数据添加android数据库
						for (FileInfo f : files) {
							FileInfo oldFile = SplashActivity.dbManager.file_queryById(f.getFuuid());
							f.setUserId(MainActivity.curUserId);
							f.setType(FileType.RECORDING.getValue());
							f.setSyn("0");
							if (StringUtil.isEmpty(oldFile.getPath())) {
								f.setPath(oldFile.getPath());
							}
						}
						SplashActivity.dbManager.files_add(files);

						setAdapterData(files);
					} else {
						Log.e(TAG, "responce code != 0");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	// 删除
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
				paramJson.put("ftype", FileType.RECORDING.getValue());
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
				if (null == responce) { // 没有网络连接不执行删除操作
					Log.i(TAG, "network is not support");
					Toast.makeText(mContext, "网络连接超时", Toast.LENGTH_SHORT).show();
				} else {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
						// 删除成功，同样删除本地保存数据
						FileInfo file = SplashActivity.dbManager.file_queryById(fuuid);
						if (FileUtil.fileExist(file.getPath())) {
							FileUtil.delFile(file.getPath());
						}
						SplashActivity.dbManager.file_delete(fuuid);
						Log.i(TAG, "delete file success!");
						// 删除成功跳到列表界面
						finish();
						startActivity(new Intent(mContext, RecordingActivity.class));
					} else {
						Toast.makeText(mContext, "删除失败", Toast.LENGTH_SHORT).show();
						Log.e(TAG, "responce code != 0");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onRefresh() {
		time = 0;
		loadData(AutoListView.REFRESH);
	}
	
	@Override
	public void onLoad() {
		time  = time + 1;
		loadData(AutoListView.LOAD);
	}
	
	private void loadData(final int what) {
		if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
			new SyncNative(mContext).syncRecording();
		}
		// 这里从服务器获取数据
		new Thread(new Runnable() {

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
					paramJson.put("ftype", FileType.RECORDING.getValue());
					
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
							for (FileInfo f : files) {
								f.setUserId(MainActivity.curUserId);
								f.setType(FileType.RECORDING.getValue());
								f.setSyn("0");
								String path = "/hanvonepen/recording/" + MainActivity.curUserId + "/" + f.getFuuid() + ".mp3";
								f.setPath(path);
//								if (!StringUtil.isEmpty(f.getContent())) {
//									if (FileUtil.fileExist(path)) {
//										FileUtil.delFile(path);
//										FileUtil.createSDFile(path);
//										FileUtil.saveTextInSdcard(path, f.getContent());
//									} else {
//										FileUtil.createSDFile(path);
//										FileUtil.saveTextInSdcard(path, f.getContent());
//									}
//									f.setPath(path);
//								}
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
								f.setType(FileType.RECORDING.getValue());
								f.setSyn("0");
								String path = "/hanvonepen/recording/" + MainActivity.curUserId + "/" + f.getFuuid() + ".mp3";
								f.setPath(path);
//								if (!StringUtil.isEmpty(f.getContent())) {
//									String path = "/hanvonepen/recording/" + MainActivity.curUserId + "/" + f.getFuuid() + ".dat";
//									if (FileUtil.fileExist(path)) {
//										FileUtil.delFile(path);
//										FileUtil.createSDFile(path);
//										FileUtil.saveTextInSdcard(path, f.getContent());
//									} else {
//										FileUtil.createSDFile(path);
//										FileUtil.saveTextInSdcard(path, f.getContent());
//									}
//									f.setPath(path);
//								}
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
}