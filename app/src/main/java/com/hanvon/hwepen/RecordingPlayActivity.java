package com.hanvon.hwepen;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
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
import com.hanvon.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RecordingPlayActivity extends BaseActivity
{
	
	private Context mContext;
	private static String SDPATH = Environment.getExternalStorageDirectory()+"";
	private ImageView back;
	private TextView topTime;
//	private ImageView download;
//	private TextView rate;
	private SeekBar seekBar;
	private ImageView playBtn;
	private ImageView preBtn;
	private ImageView nextBtn;
	private EditText recognition;
	private ImageView saveRec;
	private TextView status;
	private TextView size;
	private TextView tv_save;
	
	private TextView totalTime;
	private TextView reduceTime;
	private int baseLength = 28800000;
	private int volength = 0;
	private int reduceVolength = 0;
	SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	
	private ImageView refresh_progress; // 刷新的图片
	private AnimationDrawable anim;
	
	private String recognitionValue = "";
	private String serVer;
	
	FileInfo file;
	
	private boolean screenStatus = false;
	private String playStatus = "1"; //1暂停 0播放
	private String phonePlayStatus = "-1"; //1暂停 0播放
	
	private String fuuid;
//	private int buffered;
	int id = 0;
	
	private Player player;
	private boolean playPreparedOk = false;
	private static final String TAG = "RecordingPlayActivity";
	
	private long index = 0;
	private boolean flag = true;
	private String filePath;
	
	private boolean saved = false;
	
	private boolean finish = false;
	
	private int saveTyped = 1;
	
	TelephonyManager telManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this); //add this to container
		requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
		setContentView(R.layout.recording_play);
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			startActivity(new Intent(mContext, RecordingActivity.class));
		}
	}
	
	
	private void setListener(){
		tv_save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String newPath = SDPATH +"/hanvonepen/download/" +fuuid + ".mp3";
				String oldPath=SDPATH + "/" +filePath;
				FileUtil.createSDDir("/hanvonepen/download/");
				if (FileUtil.fileExist(filePath)) {
					try {
						saveMp3(oldPath,newPath);
					} catch (Exception e) {
						Toast.makeText(RecordingPlayActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
					  
					
				}
				
				
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
		if (playStatus.equals("0")) {
			player.play();
		}
		if (playStatus.equals("1")) {
			player.pause();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		player.stop();
		finish = true;
		
		telManager.listen(psListener, 0);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (!phonePlayStatus.equals("-1")) {
			if (phonePlayStatus.equals("1")) {
				player.pause();
			}
			phonePlayStatus = "-1";
			return;
		}
		
		if (playStatus.equals("0")) {
			player.play();
		}
		if (playStatus.equals("1")) {
			player.pause();
		}
	}
	
	private void init() throws IOException
	{
		mContext = RecordingPlayActivity.this;
		back = (ImageView) findViewById(R.id.recording_play_back);
		topTime = (TextView) findViewById(R.id.recording_play_time);
//		download = (ImageView) findViewById(R.id.recording_download);
//		rate = (TextView) findViewById(R.id.recording_play_rate);
		status = (TextView) findViewById(R.id.recording_play_status);
		size = (TextView) findViewById(R.id.recording_play_size);
		seekBar = (SeekBar) findViewById(R.id.recording_play_seekBar);
		playBtn = (ImageView) findViewById(R.id.recording_play_play);
		preBtn = (ImageView) findViewById(R.id.recording_play_pre);
		nextBtn = (ImageView) findViewById(R.id.recording_play_next);
		recognition = (EditText) findViewById(R.id.recording_play_text);
		saveRec = (ImageView) findViewById(R.id.recording_play_save);
		totalTime = (TextView) findViewById(R.id.recording_play_total_time);
		reduceTime = (TextView) findViewById(R.id.recording_play_reduce_time);
		refresh_progress = (ImageView) findViewById(R.id.recording_play_progress);
		anim = (AnimationDrawable) refresh_progress.getBackground();
		tv_save=(TextView) findViewById(R.id.tv_recordplay_save);
		
		playBtn.setClickable(false);
		
		back.setOnClickListener(listener);
//		download.setOnClickListener(listener);
		playBtn.setOnClickListener(listener);
		preBtn.setOnClickListener(listener);
		nextBtn.setOnClickListener(listener);
		saveRec.setOnClickListener(listener);
		
		// 接收传递过来的参数
		fuuid = getIntent().getStringExtra("fuuid");
		for (int i = 0; i < RecordingActivity.files.size(); i++) {
			if (RecordingActivity.files.get(i).getFuuid().equals(fuuid)) {
				id = i;
			}
		}
		Log.e(TAG, id + "");
		
		file = SplashActivity.dbManager.file_queryById(fuuid);
		if (StringUtil.isEmpty(file.getPath())) {
			filePath = "/hanvonepen/recording/" + MainActivity.curUserId + "/" + fuuid + ".mp3";
		} else {
			filePath = file.getPath();
		}
		
		topTime.setText(file.getCreateTime());
		size.setText(FileUtil.getFormatSize(Double.parseDouble(file.getLength())));
		
		//处理录音备注
		if (!StringUtil.isEmpty(file.getContent())) { // DB存的是修改后未上传的
			recognition.setText(file.getContent());
			recognition.setSelection(file.getContent().length());
		}
		
		if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
//			refreshStart();
//			new Thread(getRecTextThread).start();
//			new Thread(getWaveLengthThread).start();
		} else {
			Toast.makeText(mContext, "网络连接不可用，请检查网络后再试", Toast.LENGTH_SHORT).show();
		}
		
		/**
		 * 如果已下载就加载本地资源 如果未下载 就先进行下载 再播放
		 */
		player = new Player(seekBar);
		if (FileUtil.fileExist(filePath)) { // 已下载
			String path = Environment.getExternalStorageDirectory() + filePath;
			player.play(path);
		} else { // 未下载 去下载
			if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
//				downloadFile();
				if (new ConnectionDetector(mContext).isMobileNet()) {
					AlertDialog.Builder builder = new Builder(mContext);
					builder.setCancelable(false); // 点击对话框以外的屏幕其他区域，不关闭对话框
					builder.setTitle("提示：文件大小 " + FileUtil.getFormatSize(Double.parseDouble(file.getLength())));
					builder.setMessage("当前处于移动网络，是否继续");
					builder.setPositiveButton("继续",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									downloadFile();
								}
							});
					builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									startActivity(new Intent(mContext, RecordingActivity.class));
									RecordingPlayActivity.this.finish();
								}
							});
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					downloadFile();
				}
			}
		}
		
		/**
		 * 拖动进度条改变播放位置 
		 */
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progress;
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字
//				player.mediaPlayer.seekTo(progress);
//				int bufferTime = player.getVoiceLength()*buffered/100; //处理进度条大于缓冲的情况，继续当前播放不跳转
				int bufferTime = player.getVoiceLength();
				if (progress < bufferTime) {
					player.mediaPlayer.seekTo(progress);
					
					//改变播放时间
					reduceVolength = volength - progress;
					Date rDate = new Date();
					rDate.setTime(reduceVolength);
					reduceTime.setText(format.format(rDate));
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// 原本是(progress/seekBar.getMax())*player.mediaPlayer.getDuration()
				this.progress = progress * player.mediaPlayer.getDuration() / seekBar.getMax();
			}
		});
		
//		// 解锁屏监听
//		ScreenListener screenListener = new ScreenListener(this);
//		screenListener.begin(new ScreenStateListener() {
//
//			@Override
//			public void onUserPresent() {
//				Log.i(TAG, "onUserPresent");
//
//			}
//
//			@Override
//			public void onScreenOn() {
//				Log.i(TAG, "onScreenOn");
////				screenStatus = true;
//			}
//
//			@Override
//			public void onScreenOff() {
//				Log.i(TAG, "onScreenOff");
//				screenStatus = true;
//			}
//		});
		
		/* 取得电话服务 */
		telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// 监听电话的状态
		telManager.listen(psListener, PhoneStateListener.LISTEN_CALL_STATE);
		setListener();
	}
	
	PhoneStateListener psListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: /* 无任何状态时 */
				
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK: /* 接起电话时 */
				player.pause();
				phonePlayStatus = "1";
				break;
			case TelephonyManager.CALL_STATE_RINGING: /* 电话进来时 */
				player.pause();
				phonePlayStatus = "1";
				break;
			default:
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}; 
	
	// 监听按键事件（返回键、音量键）
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		AudioManager audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);// 声音管理类
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:// 返回键
			try {
				saveTyped = 1;
				saveRect();
			} catch (IOException e) {
				e.printStackTrace();
			}
			RecordingPlayActivity.this.finish();
			startActivity(new Intent(mContext, RecordingActivity.class));
			break;

		case KeyEvent.KEYCODE_VOLUME_UP:// 增大音量
			audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND
							| AudioManager.FLAG_SHOW_UI);
			break;

		case KeyEvent.KEYCODE_VOLUME_DOWN:// 减小音量
			audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND
							| AudioManager.FLAG_SHOW_UI);
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				
			case R.id.recording_play_back:
				try {
					saveTyped = 1;
					saveRect();
				} catch (IOException e) {
					e.printStackTrace();
				}
				RecordingPlayActivity.this.finish();
				startActivity(new Intent(mContext, RecordingActivity.class));
				break;
				
			case R.id.recording_play_play:
				if (player.isPlaying()) {
					player.pause();// pause
					playBtn.setImageDrawable(getResources().getDrawable(R.drawable.recording_play_play));
					playStatus = "1";
				} else {
					player.play(); // play
					playBtn.setImageDrawable(getResources().getDrawable(R.drawable.recording_play_pause));
					playStatus = "0";
				}
				break;
				
			case R.id.recording_play_pre:
				if (0 == id) {
					Toast toast = Toast.makeText(getApplicationContext(), "已经是第一个", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 50);
					toast.show();
				} else {
					RecordingPlayActivity.this.finish();
					
					Intent intent = new Intent();
					intent.setClass(RecordingPlayActivity.this, RecordingPlayActivity.class);
					intent.putExtra("fuuid", RecordingActivity.files.get(id - 1).getFuuid());
					startActivity(intent);
				}
				break;
				
			case R.id.recording_play_next:
				if (RecordingActivity.files.size() - 1 == id) {
					Toast toast = Toast.makeText(getApplicationContext(), "已经是最后一个", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 50);
					toast.show();
				} else {
					RecordingPlayActivity.this.finish();
					
					Intent intent = new Intent();
					intent.setClass(RecordingPlayActivity.this, RecordingPlayActivity.class);
					intent.putExtra("fuuid", RecordingActivity.files.get(id + 1).getFuuid());
					startActivity(intent);
				}
				break;
				
//			case R.id.recording_download:
//				if (FileUtil.fileExist(filePath)) {
//					new AlertDialog.Builder(mContext)
//						.setTitle("提示")
//						.setMessage("文件已存在，确定重新下载")
//						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								// TODO Auto-generated method stub
//								downloadFile();
//							}
//						})
//						.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								// TODO Auto-generated method stub
//							}
//						}).show();
//				} else {
//					downloadFile();
//				}
//				break;
				
				/**
				 * 录音备注 暂时按版本冲突 覆盖的方式
				 */
			case R.id.recording_play_save:
				saveTyped = 0;
				saved = true;
				recognitionValue = recognition.getText().toString();
				if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
					if (!StringUtil.isEmpty(recognitionValue)) {
						if (!StringUtil.isEmpty(fuuid)) {
							refreshStart();
							new Thread(saveRecTextThread).start();
						} else {
							Toast.makeText(getApplication(), "音频识别异常，无法保存备注信息", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getApplication(), "请输入内容后保存", Toast.LENGTH_SHORT).show();
					}
				} else {
//					Toast.makeText(getApplication(), "网络连接不可用，请稍后再试", Toast.LENGTH_SHORT).show();
					file.setContent(recognitionValue);
					file.setSyn("1");
					SplashActivity.dbManager.file_add(file);
					Toast.makeText(getApplication(), "无网络连接，暂存本地", Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
			
		}
	};
	
	void refreshStart(){
		recognition.setVisibility(View.GONE);
//		saveRec.setVisibility(View.GONE);
		refresh_progress.setVisibility(View.VISIBLE);
		anim.start();
	}
	void refreshStop(){
		recognition.setVisibility(View.VISIBLE);
//		saveRec.setVisibility(View.VISIBLE);
		refresh_progress.setVisibility(View.GONE);
		anim.stop();
	}
	

	
	/**
	 * 音频播放
	 */
	public class Player implements OnBufferingUpdateListener, OnCompletionListener
	{
		
		public MediaPlayer mediaPlayer;
		public SeekBar skbProgress;
		public Timer mTimer = new Timer();
		
		public Player(SeekBar skbProgress) {
			this.skbProgress = skbProgress;
			try {
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mediaPlayer.setOnBufferingUpdateListener(this);
				mediaPlayer.setOnCompletionListener(this);
			} catch (Exception e) {
				Log.e(TAG, "mediaPlayer Oncreate Error");
			}
			mTimer.schedule(mTimerTask, 0, 1000);
		}
		
		//通过定时器和Handler来更新进度条
		TimerTask mTimerTask = new TimerTask() {
			@Override
			public void run() {
				if (null == mediaPlayer) {
					return;
				}
				if (mediaPlayer.isPlaying() && skbProgress.isPressed() == false) {
					handleProgress.sendEmptyMessage(0);
				}
				
			}
		};
		
		@SuppressLint("HandlerLeak")
		Handler handleProgress = new Handler() {
			public void handleMessage(Message msg) {
				int position = mediaPlayer.getCurrentPosition();
				int duration = mediaPlayer.getDuration();
				if (duration > 0) {
					long pos = skbProgress.getMax() * position / duration;
					skbProgress.setProgress((int) pos);
				}
				
				Date rDate = new Date();
				rDate.setTime(reduceVolength);
				reduceTime.setText(format.format(rDate));
				reduceVolength = reduceVolength-1000;
			}
		};
		
		public void play() {
			mediaPlayer.start();
		}
		public void pause() {
			mediaPlayer.pause();
		}
		public void stop() {
			if (null != mediaPlayer) {
				mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
			}
		}
		public boolean isPlaying() {
			return mediaPlayer.isPlaying();
		}
		public int getVoiceLength(){ //get total time length
			return mediaPlayer.getDuration();
		}
		public int getCurLength(){
			return mediaPlayer.getCurrentPosition();
		}
		
		public void play(String uri) {
			try {
				mediaPlayer.reset();
				mediaPlayer.setDataSource(uri);
				mediaPlayer.prepare();
				mediaPlayer.start();
				
				playPreparedOk = true;
				playBtn.setClickable(true);
				playStatus = "0";
				status.setText("");
				volength = player.getVoiceLength() - baseLength;
				reduceVolength = volength;
				Date tDate = new Date();
				tDate.setTime(volength);
				totalTime.setText(format.format(tDate));
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		@Override
		public void onCompletion(MediaPlayer arg0) {
			if (playPreparedOk) {
				reduceVolength = volength;
				Date rDate = new Date();
				rDate.setTime(reduceVolength);
				reduceTime.setText(format.format(rDate));
				
				skbProgress.setProgress(0);
				playBtn.setImageDrawable(getResources().getDrawable(R.drawable.recording_play_play));
				Toast toast = Toast.makeText(getApplication(), "播放结束", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP| Gravity.CENTER, 0, 50);
				toast.show(); 
				Log.e(TAG, "mediaPlay OnCompletion");
				playStatus = "1";
			}
		}
		@Override
		public void onBufferingUpdate(MediaPlayer arg0, int bufferingProgress) {
			skbProgress.setSecondaryProgress(bufferingProgress);
//			buffered = bufferingProgress;
			int currentProgress = skbProgress.getMax() * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
			Log.e(currentProgress + "% play", bufferingProgress + "% buffer");
		}
	}
	
	
	public void saveMp3(String oldPath,String newPath) throws Exception
	{
		 try { 
	           int bytesum = 0; 
	           int byteread = 0; 
	           File oldfile= new File(oldPath);
	           if (oldfile.exists()) { //文件存在时 
	               InputStream inStream = new FileInputStream(oldPath); //读入原文件
	               FileOutputStream fs = new FileOutputStream(newPath);
	               byte[] buffer = new byte[1444]; 
	               int length; 
	               while ( (byteread = inStream.read(buffer)) != -1) { 
	                   bytesum += byteread; //字节数 文件大小 
	                   fs.write(buffer, 0, byteread); 
	               } 
	               inStream.close(); 
	               Toast.makeText(RecordingPlayActivity.this, "已保存到/hanvonepen/download/目录下", Toast.LENGTH_SHORT).show();
	           } 
	       } 
	       catch (Exception e) {
	           e.printStackTrace(); 

	       } 

	}
	
	public boolean downloadFile() {
		new Thread(new Runnable() {
			public void run() {
				long hasRead = 0;
				int len = 0;
				byte buffer[] = new byte[1024];
				try {
					String url = ServiceWS.FILE_SINGLE_DOWNLOAD;
					JSONObject paramJson = new JSONObject();
					paramJson.put("uid", "");
					paramJson.put("sid", "");
					paramJson.put("ver", "");
					paramJson.put("devid", "");
					paramJson.put("userid", MainActivity.curUserId);
					paramJson.put("ftype", FileType.RECORDING.getValue());
					paramJson.put("fuid", fuuid);
					String params = paramJson.toString();

					String urlStr = url + "?input=" + params.toString();
					URL resUrl = new URL(urlStr);
					HttpURLConnection conn = (HttpURLConnection) resUrl.openConnection();
					long size = conn.getContentLength();

					if (FileUtil.fileExist(filePath)) {
						FileUtil.delFile(filePath);
					}
					FileUtil.createSDFile(filePath);
					InputStream in = conn.getInputStream();
					OutputStream out = new FileOutputStream(FileUtil.createSDFile(filePath));
					while ((len = in.read(buffer)) != -1) { // 更新下载进度条
						
						//下载未结束 关闭当前acticity 退出线程
						if (finish) {
							if (FileUtil.fileExist(filePath)) {
								FileUtil.delFile(filePath);
							}
							out.close();
							in.close();
							return;
						}
						
						out.write(buffer, 0, len);
						
						hasRead += len;
						index = (hasRead * 100) / size;
						Message message = new Message();
						message.what = 0;
						downloadHandler.sendMessage(message);
//						Log.d(TAG, "has = " + hasRead + " size = " + size + " index = " + index);
					}
					out.close();
					in.close();
					Message msg = new Message();
					msg.what = 1;
					downloadOKHandler.sendMessage(msg);
				} catch (Exception e) {
					flag = false;
					e.printStackTrace();
				}
			}
		}).start();
		return flag;
	}

	Handler downloadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 0) {
//				Log.d(TAG, "setProgress index:" + index);
				status.setText(index + "% 已缓冲......");
				if (index >= 99) {
					// Toast.makeText(RecordingActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
					Log.e(TAG, "已完成:" + index);
				}
			}
		}
	};

	Handler downloadOKHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				Log.d(TAG, "download OK!");
				status.setText("");
//				// 下载成功后跟新DB
//				FileInfo file = new FileInfo();
//				file.setFuuid(fuuid);
//				file.setPath(filePath);
//				SplashActivity.dbManager.file_update(file);
				
				String path = Environment.getExternalStorageDirectory() + filePath;
				player.play(path);
			}
		}
	};
	
	void saveRect () throws IOException
	{
		if (saved) {
			return;
		}
		recognitionValue = recognition.getText().toString();
		String recognitionValueOld = "";
		if (!StringUtil.isEmpty(file.getContent())) {
			recognitionValueOld = file.getContent();
		}
		if (recognitionValueOld.equals(recognitionValue)) {
			//内容未改动 不变
		} else {
			//保存修改的备注
			if (new ConnectionDetector(mContext).isConnectingTOInternet()) {
				new Thread(saveRecTextThread).start();
				file.setSyn("0");
			} else {
				file.setContent(recognitionValue);
				file.setSyn("1");
			}
			SplashActivity.dbManager.file_add(file);
		}
	}
	
	Runnable saveRecTextThread = new Runnable() {
		@Override
		public void run() {
			try {
				String url = ServiceWS.RECORDSAVECNT;

				JSONObject paramJson = new JSONObject();
				paramJson.put("devid", "");
				paramJson.put("sid", "");
				paramJson.put("uid", "");
				paramJson.put("ver", "");
				paramJson.put("userid", MainActivity.curUserId);
				paramJson.put("ftype", FileType.RECORDING.getValue());
				paramJson.put("title", "");
				paramJson.put("content", recognitionValue);
				paramJson.put("fuid", fuuid);
				paramJson.put("serVer", serVer);
				String params = paramJson.toString();
				String responce = HttpClientHelper.sendPostRequest(url, params);

				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				RecordingPlayActivity.this.saveRecTextHandler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};
	Handler saveRecTextHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				Bundle bundle = msg.getData();
				String responce = bundle.getString("responce");
				if (null != responce) {
					JSONObject responceJson = new JSONObject(responce);
					if (responceJson.get("code").equals("0")) {
						//保存成功
						refreshStop();
						if (0 == saveTyped) {
							Toast.makeText(mContext, "备注信息保存成功", Toast.LENGTH_SHORT).show();
						}
					} else if (responceJson.get("code").equals("530")) {
						//覆盖 --- 改为服务器版本上传
						serVer = responceJson.get("result").toString();
						new Thread(saveRecTextThread).start();
					} else {
						//未保存成功 暂存DB
						file.setContent(recognitionValue);
						file.setSyn("1");
						SplashActivity.dbManager.file_add(file);
					}
				} else {
//					Toast.makeText(getApplication(), "服务器连接超时", Toast.LENGTH_SHORT).show();
					file.setContent(recognitionValue);
					file.setSyn("1");
					SplashActivity.dbManager.file_add(file);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		};
	};
}