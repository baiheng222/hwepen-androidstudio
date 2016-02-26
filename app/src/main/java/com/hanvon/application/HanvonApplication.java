package com.hanvon.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;

import com.alibaba.fastjson.JSONObject;
import com.baidu.frontia.FrontiaApplication;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
//import com.hanvon.bluetooth.BluetoothService;
//import com.hanvon.bluetooth.BluetoothSetting;
import com.hanvon.helper.PreferHelper;
import com.hanvon.util.LogUtil;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.Tencent;

import java.io.File;
import java.util.List;
//import org.json.JSONObject;

//假如用到位置提醒功能，需要import该类
//import com.hanvon.bluetooth.BluetoothService;

/**
 * 
 * @desc 
 * @author  tonghuiyuan
 * @time 2015-10-27 上午11:15:28
 * @version
 */
public class HanvonApplication extends FrontiaApplication {
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener;
	public String curAddress = "";
	public String addrDetail="";
	public String netState = "";
	public String curCity="";
	
	
	
	
	public static String strName = "";
	public static String strEmail = "";
	public static String strPhone = "";
	public static int userFlag;/* 0  汉王用户   1 QQ账号   2 微信账号   3 微博账号*/
	

	public static String strToken;
	public static Bitmap BitHeadImage;
	public static int count;//用户扫描记录总数
	public static String hvnName = "";//第三方登陆时对应于汉王用户名称，汉王用户登陆时对应于邮箱
	public static int cloudType;//云类型，0 未登录 1汉王云 2百度云
	public static String noteCreateTime="";
	public static String AppSid = "SuluPen_Software";
	public static String AppUid = "";
	public static String AppVer = "";
	public static String AppDeviceId = "";
	public static String HardUpdateName = "";

	public static String lastConnectedTime = "0000-00-00 00:00";//记录应用最近一次连接速录笔的时间

	public static Tencent mTencent;
	public static IWXAPI api;
	private String QQ_APPID = "1104705079";
	private JSONObject obj;
	private String City;
	public static  String mWeather;
	public static String path;
	
	public static boolean isActivity;
	
	public static String HardSid = "";
	public static boolean isDormant = false;
	
	//public static boolean isUpdate = false;
	public static String HardUpdateUrl = "";
	@Override
	public void onCreate() {

		super.onCreate();
		ShareSDK.initSDK(this);
		mLocationClient = new LocationClient(getApplicationContext());
		myListener = new MyLocationListener();
		mLocationClient.registerLocationListener(myListener);
		InitLocation();
		mLocationClient.start();
//		netState = NetWorkHelper.getNetState(getApplicationContext());
//		Frontia.init(this.getApplicationContext(), Configs.APIKEY);
		//BluetoothService.startService(this);
		PreferHelper.init(this.getApplicationContext());
				
//		/**
//		 * push function
//		 */
//		JPushInterface.init(this.getApplicationContext());
//		JPushInterface.setDebugMode(true);		
		api = WXAPIFactory.createWXAPI(this, "wxdf64ce17dae09860", true);
		api.registerApp("wxdf64ce17dae09860");	

		mTencent = Tencent.createInstance(QQ_APPID, HanvonApplication.this);
//		removeTempFromPref();
	//	
//		/**获取uid**/
		ActivityManager am = (ActivityManager) getSystemService(this.getApplicationContext().ACTIVITY_SERVICE);
       ApplicationInfo appinfo = getApplicationInfo();
        List<RunningAppProcessInfo> run = am.getRunningAppProcesses();
        for (RunningAppProcessInfo runningProcess : run) {
           if ((runningProcess.processName != null) && runningProcess.processName.equals(appinfo.processName)) {
            	AppUid = String.valueOf(runningProcess.uid);
                break;
           }
       }
//获取软件版本
       PackageManager packageManager = this.getApplicationContext().getPackageManager();   
	    PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(this.getApplicationContext().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AppVer = packInfo.versionName;
		//获取设备sn号
		TelephonyManager telephonyManager = (TelephonyManager)this.getSystemService( this.getApplicationContext().TELEPHONY_SERVICE);
		AppDeviceId =  telephonyManager.getSimSerialNumber();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
	//	SharedPreferences sharedPref=getSharedPreferences("Blue", Activity.MODE_MULTI_PROCESS);
		//BluetoothSetting.getInstance(sharedPref);

		//BluetoothService.startService(this);
		//创建应用目录
		String outputDirectory = "/data/data/com.hanvon.sulupen/down/";
		File file = new File(outputDirectory);
		// 如果目标目录不存在，则创建
		if (!file.exists()) {
		    file.mkdirs();
		}
		outputDirectory = "/data/data/com.hanvon.sulupen/users/";
		File userfile = new File(outputDirectory);
		// 如果目标目录不存在，则创建
		if (!userfile.exists()) {
			userfile.mkdirs();
		}
	}

	private void InitLocation() {
		LogUtil.i("tong-----InitLocation");
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		// option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		// option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
	}

	/**
	 * 实现实位回调监听
	 */
	public class MyLocationListener implements BDLocationListener {
	
		@Override
		public void onReceiveLocation(BDLocation location) {
			LogUtil.i("tong-------MyLocationListener");
			// Receive Location
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				LogUtil.i("tong-------TypeGpsLocation");
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				sb.append("\ndirection : ");
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append(location.getDirection());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				LogUtil.i("tong-------TypeNetWorkLocation");
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				// 运营商信息
				sb.append("\noperationers : ");
				sb.append(location.getOperators());
			}
			// setAddress(location.getAddrStr());			
			if(location.getCity()!=null||location.getDistrict()!=null){
		    LogUtil.i("tong--------location.getCity()!=nulllocation.getDistrict()!=null");
			setAddress(location.getCity() + location.getDistrict());
			}
			if(location.getAddrStr()!=null){
				setAddrDetail(location.getAddrStr());
			}
			Log.i("HanvonApplication", "setAddress=="+location.getCity() + location.getDistrict()+"setAddrDetail:"+addrDetail);
			setCity(location.getCity());
			if(location.getCity()!=null){
				//GetFirstWeather(location.getCity());
			}else{
				return;	
			}
			
		}

	}

	public String getAddress() {
		return curAddress;
	}

	public void setAddress(String addr) {
		curAddress = addr;
	}
	public String getCity() {
		return curCity;
	}

	public void setCity(String City) {
		this.curCity = City;
	}
	public String getWeather() {
		return mWeather;
	}

	public void setWeather(String curWeather) {
		this.mWeather = curWeather;
	}
	
	public String getAddrDetail() {
		LogUtil.i("tong-----addrDetail:"+addrDetail);
		return addrDetail;
	}
	public void setAddrDetail(String addrDetail) {
		this.addrDetail = addrDetail;
	}
	/*
	public void GetFirstWeather(String City){
		URL url1, url2;
		if (WeatherUtil.hasInternet(getBaseContext())) {
			mLocationClient.requestLocation();
			//String city = "北京市";
			
			if (City.contains("市") || City.contains("省")||City.contains("区")||City.contains("县")) { 
				City = City.substring(0, City.length() - 1);
			}
			try {
				url1 = new URL(
						String.format(getString(R.string.weatherurl)
								+ "?cityCode=%1$s&weatherType=0",
								LocalCity.getCityIdByName(City)));

				GetWeather nt = new GetWeather(url1, "0");
				nt.start();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(getApplicationContext(), "网络异常，请检查网络是否连接",
					Toast.LENGTH_LONG).show();
		}
	}
	class GetWeather extends Thread {
		private URL url;
		private String IsNow;

		public GetWeather(URL url, String IsNow) {
			this.url = url;
			this.IsNow = IsNow;
		}

		@Override
		public void run() {
			HttpURLConnection conn = null; // 连接对象
			String resultData = WeatherUtil.getResault(conn, url);

			Log.i("天气", resultData);
			String resultmsg = null;

			Looper.prepare();
			resultmsg = json(resultData);
			setWeather(resultmsg);
			Log.i("ScanNoteDetailActivity", "resultmsg=="+resultmsg);
			Looper.loop();

		}
	}
	// 解析天气的json数据
	public String json(String json) {
		String jsonresult = null;
		Weather weather = new Weather();
		try {
			obj = new JSONObject(json);
			JSONObject contentObject = obj.getJSONObject("weatherinfo");
			weather.setCity(contentObject.getString("city"));
			weather.setToptemp(contentObject.getString("temp1"));
			weather.setWeather(contentObject.getString("weather1"));
			weather.setIndex(contentObject.getString("index"));
			weather.setIndex_d(contentObject.getString("index_d"));

			jsonresult = weather.getWeather();
		} catch (JSONException e) {
			Log.i("Tag", "解析json失败");
			e.printStackTrace();
		}
		weather = null;
		return jsonresult;
	}

	// 解析实时天气的json数据
	public String json1(String json) {
		String jsonresult = null;
		Weather weather = new Weather();
		try {
			obj = new JSONObject(json);
			JSONObject contentObject = obj.getJSONObject("weatherinfo");
			weather.setToptemp(contentObject.getString("temp"));
			weather.setWind(contentObject.getString("WD"));
			weather.setWindfl(contentObject.getString("WS"));
			weather.setGxtime(contentObject.getString("time"));

			jsonresult = "当前温度：" + weather.getToptemp() + "  风向风力：  "
					+ weather.getWind() + weather.getWindfl() + "  "
					+ weather.getGxtime() + "发布";
		} catch (JSONException e) {
			Log.i("Tag", "解析json失败");
			e.printStackTrace();
		}
		weather = null;
		return jsonresult;
	}
	private void removeTempFromPref()
	{
		SharedPreferences sp = getSharedPreferences(
				CustomConstants.APPLICATION_NAME, MODE_PRIVATE);
		sp.edit().remove(CustomConstants.PREF_TEMP_IMAGES).commit();
	}
	*/
	
}
