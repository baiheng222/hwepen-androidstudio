package com.hanvon.hwepen;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hanvon.bean.FileInfo;
import com.hanvon.bean.FileInfo.FileType;
import com.hanvon.common.ServiceWS;
import com.hanvon.splash.SplashActivity;
import com.hanvon.util.FileUtil;
import com.hanvon.util.HttpClientHelper;
import com.hanvon.util.StringUtil;

import org.json.JSONObject;

import java.util.List;

public class SyncNative {

//	private Context context;
	
	private static final String TAG = "SyncNative";

	public SyncNative(Context context) {
		super();
	}

	public void syncExcerpt() {
		List<FileInfo> navFiles = SplashActivity.dbManager.file_queryByStatus(MainActivity.curUserId, FileType.EXCERPT.getValue(), "1"); //未同步的
		if (0 == navFiles.size()) {
			return;
		}
		Log.d(TAG, "同步未保存的摘抄----------数量：" + navFiles.size());
		for (FileInfo file : navFiles) {
			if (StringUtil.isEmpty(file.getAccessTime()) && StringUtil.isEmpty(file.getModifyTime())) { //新建
				ExcerptCreateThread thread = new ExcerptCreateThread(file);
				new Thread(thread).start();
			} else { //保存
				ExcerptUpdateThread thread = new ExcerptUpdateThread(file);
				new Thread(thread).start();
			}
			
			file.setSyn("0");
			SplashActivity.dbManager.file_add(file);
		}
	}
	
	public void syncRecording() {
		List<FileInfo> navFiles = SplashActivity.dbManager.file_queryByStatus(MainActivity.curUserId, FileType.RECORDING.getValue(), "1"); //未同步的
		if (0 == navFiles.size()) {
			return;
		}
		Log.d(TAG, "同步未保存的录音----------数量：" + navFiles.size());
		for (FileInfo file : navFiles) {
			RecordingRecgThread thread = new RecordingRecgThread(file);
			new Thread(thread).start();
			
			file.setSyn("0");
			SplashActivity.dbManager.file_add(file);
		}
	}

	private class ExcerptCreateThread implements Runnable
	{
		private FileInfo fileInfo;

		public ExcerptCreateThread(FileInfo fileInfo) {
			super();
			this.fileInfo = fileInfo;
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
				paramJson.put("userid", fileInfo.getUserId());
				paramJson.put("ftype", fileInfo.getType());
				paramJson.put("title", fileInfo.getTitle());
				paramJson.put("summary", "");
				if (FileUtil.fileExist(fileInfo.getPath())) {
					paramJson.put("content", FileUtil.loadTextFromSdcard(fileInfo.getPath()));
				} else {
					paramJson.put("content", "");
				}
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);

				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				mBundle.putSerializable("file", fileInfo);
				Message msg = new Message();
				msg.setData(mBundle);
				excerptCreateHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	Handler excerptCreateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			FileInfo fileInfo = (FileInfo) bundle.getSerializable("file");
			try {
				JSONObject responceJson = new JSONObject(responce);
				if (responceJson.get("code").equals("0")) {
					// 新建成功后 删除原纪录 （新建后同步时会刷新）  fuuid 可能不一样 删除diao
					SplashActivity.dbManager.file_delete(fileInfo.getFuuid());
					if (FileUtil.fileExist(fileInfo.getPath())) {
						FileUtil.delFile(fileInfo.getPath());
					}
				} else {
					fileInfo.setSyn("1");
					SplashActivity.dbManager.file_add(fileInfo);
				}
			} catch (Exception e) {
				fileInfo.setSyn("1");
				SplashActivity.dbManager.file_add(fileInfo);
				e.printStackTrace();
			}
		};
	};
	
	private class ExcerptUpdateThread implements Runnable
	{
		private FileInfo fileInfo;
		
		public ExcerptUpdateThread(FileInfo fileInfo) {
			super();
			this.fileInfo = fileInfo;
		}
		
		@Override
		public void run() {
			try {
				String url = ServiceWS.FILE_SAVE;
				
				JSONObject paramJson = new JSONObject();
				paramJson.put("devid", "");
				paramJson.put("sid", "");
				paramJson.put("uid", "");
				paramJson.put("ver", "");
				paramJson.put("userid", fileInfo.getUserId());
				paramJson.put("ftype", fileInfo.getType());
				paramJson.put("fuid", fileInfo.getFuuid());
				paramJson.put("title", fileInfo.getTitle());
				if (FileUtil.fileExist(fileInfo.getPath())) {
					paramJson.put("content", FileUtil.loadTextFromSdcard(fileInfo.getPath()));
				} else {
					paramJson.put("content", "");
				}
				paramJson.put("serVer", fileInfo.getSerVer());
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);
				
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				mBundle.putSerializable("file", fileInfo);
				Message msg = new Message();
				msg.setData(mBundle);
				excerptUpdateHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	Handler excerptUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			FileInfo fileInfo = (FileInfo) bundle.getSerializable("file");
			try {
				JSONObject responceJson = new JSONObject(responce);
				if (responceJson.get("code").equals("0")) { //无版本冲突  直接保存
//					SplashActivity.dbManager.file_delete(fileInfo.getFuuid());
//					if (FileUtil.fileExist(fileInfo.getPath())) {
//						FileUtil.delFile(fileInfo.getPath());
//					}
				} else if(responceJson.get("code").equals("530")){ //版本冲突 新建文件
//					SplashActivity.dbManager.file_delete(fileInfo.getFuuid());
//					if (FileUtil.fileExist(fileInfo.getPath())) {
//						FileUtil.delFile(fileInfo.getPath());
//					} //新建成功后会删除
					ExcerptCreateThread thread = new ExcerptCreateThread(fileInfo);
					new Thread(thread).start();
				} else {
					fileInfo.setSyn("1");
					SplashActivity.dbManager.file_add(fileInfo);
				}
			} catch (Exception e) {
				fileInfo.setSyn("1");
				SplashActivity.dbManager.file_add(fileInfo);
				e.printStackTrace();
			}
		};
	};
	
	/**
	 * 录音 备注
	 * @author Hu
	 *
	 */
	private class RecordingRecgThread implements Runnable
	{
		
		private FileInfo fileInfo;
		
		public RecordingRecgThread(FileInfo fileInfo) {
			super();
			this.fileInfo = fileInfo;
		}
		
		@Override
		public void run() {
			try {
				String url = ServiceWS.RECORDSAVECNT;

				JSONObject paramJson = new JSONObject();
				paramJson.put("devid", "");
				paramJson.put("sid", "");
				paramJson.put("uid", "");
				paramJson.put("ver", "");
				paramJson.put("userid", fileInfo.getUserId());
				paramJson.put("ftype", fileInfo.getType());
				paramJson.put("title", "");
				paramJson.put("content", fileInfo.getContent()); //保存在本地录音备注信息
				paramJson.put("fuid", fileInfo.getFuuid()); //音频文件对应的 备注文件（识别文件）的文件id
				paramJson.put("serVer", fileInfo.getSerVer());
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);
				
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				mBundle.putSerializable("file", fileInfo);
				Message msg = new Message();
				msg.setData(mBundle);
				recordingRecgHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	Handler recordingRecgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			FileInfo fileInfo = (FileInfo) bundle.getSerializable("file");
			try {
				JSONObject responceJson = new JSONObject(responce);
				if (responceJson.get("code").equals("0")) { //无版本冲突  直接保存
					
					SplashActivity.dbManager.file_delete(fileInfo.getFuuid()); //删除本地记录 content
					
				} else if(responceJson.get("code").equals("530")){ //版本冲突 覆盖
					
					fileInfo.setSerVer(responceJson.get("result").toString());
					RecordingRecgThread thread = new RecordingRecgThread(fileInfo);
					new Thread(thread).start();
					
				} else {
					fileInfo.setSyn("1");
					SplashActivity.dbManager.file_add(fileInfo);
				}
			} catch (Exception e) {
				fileInfo.setSyn("1");
				SplashActivity.dbManager.file_add(fileInfo);
				e.printStackTrace();
			}
		};
	};
	
	
}
