package com.hanvon.hwepen;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import com.hanvon.application.AppManage;
import com.hanvon.bean.FileInfo;
import com.hanvon.bean.FileInfo.FileType;
import com.hanvon.common.ServiceWS;
import com.hanvon.splash.SplashActivity;
import com.hanvon.util.ConnectionDetector;
import com.hanvon.util.FileUtil;
import com.hanvon.util.HttpClientHelper;
import com.hanvon.util.HvnCloudManager;
import com.hanvon.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class ExcerptEditActivity extends BaseActivity
{
	
	private Context mContext;
	private ImageView back;
	private ImageView finish;
	private ImageView share;
	private TextView time;
	private EditText title;
	private EditText content;
	private TextView size;
	private TextView tv_save;
	
	private String fuuid;
	private String titleValue;
	private String contentValue;
	private String titleGetValue = "";
	private String contentGetValue = "";
	private String serVer;
	private String latestSerVer;
	
	FileInfo file;
	
	private static final int CREATE = 0;
	private static final int UPDATE = 1;

	private final static int UPLLOAD_FILE_CLOUD_SUCCESS = 5;
	private final static int UPLLOAD_FILE_CLOUD_FAIL = 6;

	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
	
	private ProgressDialog pd;
	private String  strLinkPath = null;
	private Bitmap bitmapLaunch;
	private Boolean bShareClick = false;

	private static final String TAG = "ExcerptEditActivity";


	private Handler handler = new Handler() {
		@SuppressLint("ShowToast")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

				case UPLLOAD_FILE_CLOUD_SUCCESS:
					pd.dismiss();
					showShare();

					break;
				case UPLLOAD_FILE_CLOUD_FAIL:
					pd.dismiss();
					Toast.makeText(ExcerptEditActivity.this, "获取链接失败，不能分享!", Toast.LENGTH_SHORT).show();
					bShareClick = false;

					break;

				default:
					break;
			}
		}
	};


	private void showShare() {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		//关闭sso授权
		oks.disableSSOWhenAuthorize();
		Log.i(TAG, "tong------strLinkPath:"+strLinkPath);
		// 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
		//oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(getString(R.string.share_from_hanvon));
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl(strLinkPath);
		// text是分享文本，所有平台都需要这个字段
//		 String title = etNoteTitle.getText().toString();
//		 if(title == "")
//		 {
//			 String strContent = etScanContent.getText().toString();
//			 title = strContent;
//		 }
		 oks.setText(title.getText().toString());
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		String curPath = getApplicationContext().getFilesDir().getPath();

		copyPhoto();
		String srcPath = curPath + "/"+"image.png";
		//String newPath= "/sdcard/app_launcher.png";
		Log.i(TAG, "tong-----------srcPath:"+srcPath);

		//copyFile(srcPath,newPath);
		oks.setImagePath(srcPath);//确保SDcard下面存在此张图片
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(strLinkPath);
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment("我是测试评论文本");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl(strLinkPath);

		// 启动分享GUI
		oks.show(this);
		bShareClick = false;
	}

	public void copyPhoto()
	{

		bitmapLaunch = BitmapFactory.decodeResource(getResources(), R.drawable.app_launcher);
		FileOutputStream fos = null;
		try {
			fos = openFileOutput("image.png", Context.MODE_PRIVATE);
			bitmapLaunch.compress(Bitmap.CompressFormat.PNG, 100, fos);
		} catch (FileNotFoundException e) {
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.excerpt_edit);
		try {
			init();
			
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			backFinish();
			startActivity(new Intent(mContext, ExcerptActivity.class));
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void init() throws IOException, NameNotFoundException
	{
		mContext = ExcerptEditActivity.this;
		back = (ImageView) findViewById(R.id.excerpt_edit_back);
		finish = (ImageView) findViewById(R.id.excerpt_edit_finish);
		share = (ImageView) findViewById(R.id.excerpt_edit_share);
		time = (TextView) findViewById(R.id.excerpt_edit_time);
		size = (TextView) findViewById(R.id.excerpt_edit_size);
		title = (EditText) findViewById(R.id.excerpt_edit_title);
		content = (EditText) findViewById(R.id.excerpt_edit_content);
		tv_save=(TextView) findViewById(R.id.tv_edit_save);
		
		back.setOnClickListener(listener);
		finish.setOnClickListener(listener);
		share.setOnClickListener(listener);
		
	
		fuuid = getIntent().getStringExtra("fuuid");
		if (StringUtil.isEmpty(fuuid)) { //创建
			time.setText(format.format(new Date()));
		} else { //编辑 查看
			file = SplashActivity.dbManager.file_queryById(fuuid);
			time.setText(file.getCreateTime());
			if (!StringUtil.isEmpty(file.getLength())) {
				size.setText(FileUtil.getFormatSize(Double.parseDouble(file.getLength())));
			}
			
			serVer = file.getSerVer();
			if (!StringUtil.isEmpty(file.getTitle())) {
				title.setText(file.getTitle());
				title.setSelection(file.getTitle().length());
				
				titleGetValue = file.getTitle();
			}
			if (!StringUtil.isEmpty(file.getPath())) {
				if (FileUtil.fileExist(file.getPath())) {
					String tmp = FileUtil.loadTextFromSdcard(file.getPath());
					if (!StringUtil.isEmpty(tmp)) {
						content.setText(tmp);
						
						contentGetValue = tmp;
					}
				}
			}
		}
		
		setListener();
	}
	
	OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				
			case R.id.excerpt_edit_back:
				backFinish();
				startActivity(new Intent(mContext, ExcerptActivity.class));
				finish();
				break;
				
			case R.id.excerpt_edit_finish:
				//点击完成 新建保存 or 修改保存
				titleValue = title.getText().toString();
				contentValue = content.getText().toString();
				if (StringUtil.isEmpty(titleValue) && StringUtil.isEmpty(contentValue)) {
					Toast.makeText(mContext, "无任何内容", Toast.LENGTH_SHORT).show();
					return;
				}
				if (StringUtil.isEmpty(fuuid)) { //新建保存 无fuuid
					if (StringUtil.isEmpty(titleValue)) {
						if (contentValue.length() > 15) {
							titleValue = contentValue.substring(0, 15);
						} else {
							titleValue = contentValue;
						}
					}
					if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
						pd = ProgressDialog.show(mContext, "", "正在保存......");
						new Thread(createThread).start();
					} else {
//						Toast.makeText(mContext, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
						//保存在本地 联网后同步 同步冲突时 新建
						saveNav(CREATE);
					}
				} else { //编辑保存
					if (titleValue.equals(titleGetValue) && contentValue.equals(contentGetValue)) {
						Toast.makeText(mContext, "无任何修改", Toast.LENGTH_SHORT).show();
						startActivity(new Intent(mContext, ExcerptActivity.class));
						finish();
					} else {
						if (StringUtil.isEmpty(titleValue)) {
							if (contentValue.length() > 15) {
								titleValue = contentValue.substring(0, 15);
							} else {
								titleValue = contentValue;
							}
						}
						if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
							pd = ProgressDialog.show(mContext, "", "正在保存......");
							new Thread(saveThread).start();
						} else {
//							Toast.makeText(mContext, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
							//保存在本地 联网后同步 同步冲突时 新建
							saveNav(UPDATE);
						}
					}
				}
				break;
				
			case R.id.excerpt_edit_share:
//				Toast.makeText(mContext, "分享", Toast.LENGTH_SHORT).show();
				//initShareIntent();
				if(!bShareClick)
				{
					pd = ProgressDialog.show(ExcerptEditActivity.this, "", getString(R.string.link_mess));
					shareExcerp();
				}
				break;

			default:
				break;
			}
			
		}
	};
	
	void saveNav (int status){
		switch (status) {
		case CREATE:
			String nFuuid = System.currentTimeMillis() + "";
			String nPath = "/hanvonepen/excerpt/" + MainActivity.curUserId + "/" + nFuuid + ".dat";
			try {
				if (FileUtil.fileExist(nPath)) {
					FileUtil.delFile(nPath);
					FileUtil.createSDFile(nPath);
					FileUtil.saveTextInSdcard(nPath, contentValue);
				} else {
					FileUtil.createSDFile(nPath);
					FileUtil.saveTextInSdcard(nPath, contentValue);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			FileInfo navFile = new FileInfo();
			navFile.setFuuid(nFuuid);
//			navFile.setContent(contentValue); //摘抄文件较大 以文件方式存储
			navFile.setTitle(titleValue);
			navFile.setCreateTime(format.format(new Date()));
			navFile.setPath(nPath);
			navFile.setSyn("1");  //未同步
			navFile.setType(FileType.EXCERPT.getValue());
			navFile.setUserId(MainActivity.curUserId);
			
			SplashActivity.dbManager.file_add(navFile);
			Toast.makeText(mContext, "保存成功", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(mContext, ExcerptActivity.class));
			finish();
			break;

		case UPDATE:
			if (StringUtil.isEmpty(file.getPath())) {
				String p = "/hanvonepen/excerpt/" + MainActivity.curUserId + "/" + fuuid + ".dat";
				file.setPath(p);
			}
			try {
				if (FileUtil.fileExist(file.getPath())) {
					FileUtil.delFile(file.getPath());
					FileUtil.createSDFile(file.getPath());
					FileUtil.saveTextInSdcard(file.getPath(), contentValue);
				} else {
					FileUtil.createSDFile(file.getPath());
					FileUtil.saveTextInSdcard(file.getPath(), contentValue);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			file.setTitle(titleValue);
//			file.setContent(contentValue); //摘抄文件较大 以文件方式存储
			file.setSyn("1");  //未同步
			
			SplashActivity.dbManager.file_add(file);
			Toast.makeText(mContext, "保存成功", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(mContext, ExcerptActivity.class));
			finish();
			break;
			
		default:
			break;
		}
	}
	
	/**
	 * 获取内容
	 */
//	Runnable getContentThread = new Runnable() {
//
//		@Override
//		public void run() {
//			try {
//				String url = ServiceWS.FILE_GETCNT;
//				JSONObject paramJson = new JSONObject();
//				paramJson.put("devid", "");
//				paramJson.put("ftype", "");
//				paramJson.put("sid", "");
//				paramJson.put("uid", "");
//				paramJson.put("userid", "");
//				paramJson.put("ver", "");
//				paramJson.put("fuid", fuuid);
//				String params = paramJson.toString();
//				String responce = HttpClientHelper.sendPostRequest(url, params);
//				
//				Bundle mBundle = new Bundle();
//				mBundle.putString("responce", responce);
//				Message msg = new Message();
//				msg.setData(mBundle);
//				getContentHandler.sendMessage(msg);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//	};
//	Handler getContentHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			pd.dismiss();
//			Bundle bundle = msg.getData();
//			String responce = bundle.getString("responce");
//			try {
//				if (StringUtil.isEmpty(responce)) {
//					Toast.makeText(mContext, "网络连接不可用，请稍后再试", Toast.LENGTH_SHORT).show();
//				} else {
//					JSONObject responceJson = new JSONObject(responce);
//					if (responceJson.get("code").equals("0")) {
//						titleGetValue = responceJson.getString("title");
//						contentGetValue = responceJson.getString("content");
//						title.setText(titleGetValue);
//						title.setSelection(titleGetValue.length());
//						content.setText(contentGetValue);
//						serVer = responceJson.getString("serVer"); //服务器版本
//						
//						//保存到手机
//						String path = "/hanvonepen/excerpt/" + MainActivity.curUserId + "/" + fuuid + ".dat";//通过length判断文件是否有更新
//						if (FileUtil.fileExist(path)) {
//							FileUtil.delFile(path);
//							FileUtil.createSDFile(path);
//							FileUtil.saveTextInSdcard(path, contentValue);
//						} else {
//							FileUtil.createSDFile(path);
//							FileUtil.saveTextInSdcard(path, contentValue);
//						}
//						Log.i(TAG, "save file sucessfully");
//						//更新sqlite
//						FileInfo file = new FileInfo();
//						file.setFuuid(fuuid);
//						file.setPath(path);
//						SplashActivity.dbManager.file_update(file);
//						Log.i(TAG, "update SQLite sucessfully");
//					} else {
//						Toast.makeText(mContext, "服务不可用，请稍后再试", Toast.LENGTH_SHORT).show();
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	};
	/**
	 * 编辑保存
	 */
	Runnable saveThread = new Runnable() {
		
		@Override
		public void run() {
			try {
				String url = ServiceWS.FILE_SAVE;
				
				JSONObject paramJson = new JSONObject();
				paramJson.put("devid", "");
				paramJson.put("sid", "");
				paramJson.put("uid", "");
				paramJson.put("ver", "");
				paramJson.put("userid", MainActivity.curUserId);
				paramJson.put("ftype", FileType.EXCERPT.getValue());
				paramJson.put("fuid", fuuid);
				paramJson.put("title", titleValue);
				paramJson.put("content", contentValue);
				paramJson.put("serVer", serVer);
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);
				
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				saveHandler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	};
	Handler saveHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			try {
				if (StringUtil.isEmpty(responce)) { // 修改后保存需要 判断服务器上的版本  故不提供本地保存功能
					Log.i(TAG, "network is not support");
					Toast.makeText(mContext, "连接服务器超时", Toast.LENGTH_SHORT).show();
				} else {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						Toast.makeText(ExcerptEditActivity.this, "保存" + titleValue + "成功！", Toast.LENGTH_SHORT).show();
						startActivity(new Intent(ExcerptEditActivity.this, ExcerptActivity.class));
						finish();
					} else if(responceJson.get("code").equals("530")){
						Log.i(TAG, "version conflict");
						latestSerVer = responceJson.get("result").toString();
						ShowPickDialog();
					} else {
						Toast.makeText(mContext, "服务不可用，请稍后再试", Toast.LENGTH_SHORT).show();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};
	
	
	private void setListener(){
		tv_save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String p = "/hanvonepen/download/" + fuuid + ".txt";
				try {
				FileUtil.createSDDir("/hanvonepen/download/");
					FileUtil.createSDFile(p);
					FileUtil.saveTextInSdcard(p, file.getContent());
					Toast.makeText(ExcerptEditActivity.this, "已保存到/hanvonepen/download/目录下", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(ExcerptEditActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
				
			}
		});
	}
	private void ShowPickDialog() {
		AlertDialog.Builder builder = new Builder(ExcerptEditActivity.this);
		builder.setTitle("");
		builder.setMessage("文件与服务器上版本冲突");
		builder.setCancelable(false);
		builder.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 版本号改为服务器上的版本再次修改上传
				serVer = latestSerVer;
				pd = ProgressDialog.show(mContext, "", "正在保存......");
				new Thread(saveThread).start();
			}
		});
		builder.setNegativeButton("新建", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 调用新建接口
				pd = ProgressDialog.show(mContext, "", "正在保存......");
				new Thread(createThread).start();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * 新建保存
	 */
	Runnable createThread = new Runnable() {

		@Override
		public void run() {
			try {
				String url = ServiceWS.FILE_ADD;

				JSONObject paramJson = new JSONObject();
				paramJson.put("devid", "");
				paramJson.put("sid", "");
				paramJson.put("uid", "");
				paramJson.put("ver", "");
				paramJson.put("userid", MainActivity.curUserId);
				paramJson.put("ftype", FileType.EXCERPT.getValue());
				paramJson.put("title", titleValue);
				paramJson.put("summary", "");
				paramJson.put("content", contentValue);
				String params = paramJson.toString();
				Log.d(TAG, "!!!!!!! add file json string is " + params);
				String responce = HttpClientHelper.sendPostRequest(url, params);

				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				ExcerptEditActivity.this.createHandler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};
	Handler createHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			try
			{
				pd.dismiss();
				if (StringUtil.isEmpty(responce))
				{
					Toast.makeText(mContext, "连接服务器超时", Toast.LENGTH_SHORT).show();
				}
				else
				{
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0"))
					{
						Toast.makeText(mContext, "保存" + titleValue + "成功！", Toast.LENGTH_SHORT).show();

						// 新建成功后保存到手机  创建时间暂时设为手机时间
						String path = "/hanvonepen/excerpt/" + MainActivity.curUserId + "/" + responceJson.getString("fuid") + ".dat";
						if (FileUtil.fileExist(path))
						{
							FileUtil.delFile(path);
							FileUtil.createSDFile(path);
							FileUtil.saveTextInSdcard(path, contentValue);
						}
						else
						{
							FileUtil.createSDFile(path);
							FileUtil.saveTextInSdcard(path, contentValue);
						}
						Log.i(TAG, "save file sucessfully");
						FileInfo f = new FileInfo();
						f.setFuuid(responceJson.getString("fuid"));
						f.setUserId(MainActivity.curUserId);
						f.setType(FileType.EXCERPT.getValue());
						f.setCreateTime(format.format(new Date()));
						f.setTitle(titleValue);
						f.setSyn("0");
						f.setPath(path);
						SplashActivity.dbManager.file_add(f);

						startActivity(new Intent(mContext, ExcerptActivity.class));
						finish();
					}
					else
					{
						Toast.makeText(mContext, "服务不可用，请稍后再试", Toast.LENGTH_SHORT).show();
					}
				}
			} catch (Exception e) {
				pd.dismiss();
				e.printStackTrace();
			}
		};
	};


	private void shareExcerp()
	{
		String titleStr = title.getText().toString();
		String contentStr = content.getText().toString();
		if (null == contentStr)
		{
			Log.d(TAG, "content is null");
			return;
		}
		bShareClick = true;
		HvnCloudManager hvnCloud = new HvnCloudManager();
		try
		{
			hvnCloud.WriteFileForShareSelect(titleStr, contentStr);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		UploadFilesToHvnCloudForShare();
	}

	public void UploadFilesToHvnCloudForShare()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				String result = null;
				HvnCloudManager hvnCloud = new HvnCloudManager();
				result = hvnCloud.ShareForSelect();
				Log.i(TAG, result);

				if (result == null)
				{
					Message msg = new Message();
					msg.what = UPLLOAD_FILE_CLOUD_FAIL;
					handler.sendMessage(msg);
				}
				else
				{
					strLinkPath = result;
					Message msg = new Message();
					msg.what = UPLLOAD_FILE_CLOUD_SUCCESS;
					handler.sendMessage(msg);
				}
			}
		}.start();
	}

	private void initShareIntent() {
		// 一般分享
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		// intent.putExtra(Intent.EXTRA_TITLE, "title");
		// intent.putExtra(Intent.EXTRA_SUBJECT, "subject");
		String tmp = content.getText().toString();
		if (tmp.length()>100) {
			tmp = tmp.substring(0, 100) + "......";
		}
		if (StringUtil.isEmpty(tmp)) {
			tmp = "我正在使用汉王E典笔手机客户端，能同步E典笔扫描内容。\n网站请访问：http://cloud.hanvon.com";
		} else {
			tmp = "来自汉王E典笔  http://cloud.hanvon.com\n" + tmp;
		}
		intent.putExtra(Intent.EXTRA_TEXT, tmp);

		Intent chooserIntent = Intent.createChooser(intent, "Select app to share");
		if (chooserIntent == null) {
			return;
		}
		try {
			startActivity(chooserIntent);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "Can't find share component to share", Toast.LENGTH_SHORT).show();
		}
	}
	
	void backFinish () {
		titleValue = title.getText().toString();
		contentValue = content.getText().toString();
		if (StringUtil.isEmpty(titleValue) && StringUtil.isEmpty(contentValue)) {
//			Toast.makeText(mContext, "无任何内容", Toast.LENGTH_SHORT).show();
			return;
		}
		if (StringUtil.isEmpty(fuuid)) { //新建保存
			if (StringUtil.isEmpty(titleValue)) {
				if (contentValue.length() > 15) {
					titleValue = contentValue.substring(0, 15);
				} else {
					titleValue = contentValue;
				}
			}
			saveNav(CREATE);
		} else { //编辑保存
			if (titleValue.equals(titleGetValue) && contentValue.equals(contentGetValue)) {
				return;
			} else {
				if (StringUtil.isEmpty(titleValue)) {
					if (contentValue.length() > 15) {
						titleValue = contentValue.substring(0, 15);
					} else {
						titleValue = contentValue;
					}
				}
				saveNav(UPDATE);
			}
		}
	}
	
//	void changeSearchTextColor() {
//		String searchTextValue = DevCons.searchText;
//		if (StringUtil.isEmpty(searchTextValue)) {
//			return;
//		}
//		if (titleGetValue.contains(searchTextValue)) {
//			SpannableStringBuilder style = new SpannableStringBuilder(titleGetValue);
//			int begin = 0;
//			while (titleGetValue.indexOf(searchTextValue, begin) >= 0) {
//				int start = titleGetValue.indexOf(searchTextValue, begin);
//				int ent = start + searchTextValue.length();
//				begin += searchTextValue.length();
//				style.setSpan(new ForegroundColorSpan(Color.RED), start, ent, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			}
//			title.setText(style);
//		}
//		
//		if (contentGetValue.contains(searchTextValue)) {
//			SpannableStringBuilder style = new SpannableStringBuilder(contentGetValue);
//			int begin = 0;
//			while (contentGetValue.indexOf(searchTextValue, begin) >= 0) {
//				int start = contentGetValue.indexOf(searchTextValue, begin);
//				int ent = start + searchTextValue.length();
//				begin += searchTextValue.length();
//				style.setSpan(new ForegroundColorSpan(Color.RED), start, ent, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			}
//			content.setText(style);
//		}
//	}
}
