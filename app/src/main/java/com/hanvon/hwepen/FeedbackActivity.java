package com.hanvon.hwepen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanvon.application.AppManage;
import com.hanvon.common.ServiceWS;
import com.hanvon.util.HttpClientHelper;
import com.hanvon.util.StringUtil;

import org.json.JSONObject;

public class FeedbackActivity extends Activity implements OnClickListener
{
	
	private ImageView topBackBtn;
	private EditText feedback;
	private TextView submitBtn;
	private String result;

	private ProgressDialog pd;
	
	private String TAG = "FeedbackActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManage.getInstance().addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //remove title bar
		setContentView(R.layout.feedback);
	
		topBackBtn = (ImageView) findViewById(R.id.feedback_back);
		feedback = (EditText) findViewById(R.id.feekback_text);
		submitBtn = (TextView) findViewById(R.id.feedback_submit);
		
		topBackBtn.setOnClickListener(this);
		submitBtn.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.feedback_back:
			this.finish();
			break;
			
		case R.id.feedback_submit:
			result = feedback.getText().toString();
			if (StringUtil.isEmpty(result)) {
				Toast.makeText(FeedbackActivity.this, "请输入内容后提交", Toast.LENGTH_SHORT).show();
			} else {
				pd = ProgressDialog.show(FeedbackActivity.this, "", "正在提交......");
				new Thread(feedbackThread).start();
			}
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (pd != null) {
			pd.dismiss();
		}
	}
	
	private Runnable feedbackThread = new Runnable() {
		@Override
		public void run() {
			try {
				String url = ServiceWS.FEEDBACK;
//				String url = "http://api.hanvon.com/rt/ap/v1/pub/std/heatmap/send";
//				String url = "http://cloud.hwyun.com/ws-cloud/rt/ap/v1/pub/std/heatmap/send";
				
				JSONObject jsonParams = new JSONObject();
				jsonParams.put("uid", ""); 	//设备唯一ID 通产为cpu_id
				jsonParams.put("sid", "hwepen-android"); //app名称
				jsonParams.put("ver", MainActivity.version);
				jsonParams.put("data", result); //反馈结果
				jsonParams.put("type", ""); //自定义保留字段 用于区分反馈类型
				String responce = HttpClientHelper.postData(url, jsonParams.toString());
				
				Bundle mBundle = new Bundle();
				mBundle.putString("responce", responce);
				Message msg = new Message();
				msg.setData(mBundle);
				feedbackHandler.sendMessage(msg);
			} catch (Exception e) {
				pd.dismiss();
				e.printStackTrace();
			}
		}
	};
	
	Handler feedbackHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			String responce = bundle.getString("responce");
			pd.dismiss();
			try {
				if(null != responce && !responce.equals("")){
					JSONObject result = new JSONObject(responce);
					if (result.get("code").equals("0")) {
						Log.i(TAG, "submit ok");
						Toast.makeText(FeedbackActivity.this, "非常感谢您宝贵的建议！谢谢！", Toast.LENGTH_SHORT).show();
						FeedbackActivity.this.setResult(RESULT_OK, null);
						FeedbackActivity.this.finish();
					} else {
						Log.i(TAG, "submit faild");
						Toast.makeText(FeedbackActivity.this, "非常抱歉，提交失败请重试！", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(FeedbackActivity.this, "网络连接不可用，请稍后再试", Toast.LENGTH_SHORT).show();
				}	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

}
