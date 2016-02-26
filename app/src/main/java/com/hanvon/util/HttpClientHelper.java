package com.hanvon.util;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HttpClientHelper {
	
	private static final String TAG="HttpClients";
	private static int timeoutConnection = 10000;  
	private static int timeoutSocket = 10000;  
	
	public static String sendPostRequest(String url, String params) {
		String strResult = null;
		try {
			HttpPost request = new HttpPost(url);
			
			//httpPost request param
			StringEntity stringEntity = new StringEntity(params, HTTP.UTF_8);
			request.setEntity(stringEntity);
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
			HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpResponse httpResponse = httpClient.execute(request);
			
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				byte[] bytes = EntityUtils.toByteArray(httpResponse.getEntity());
				strResult = new String(bytes, "UTF-8");
				Log.i(TAG, strResult);
			}
			return strResult;
		} catch (Exception e) {
			e.printStackTrace();
			return strResult;
		}
	}
	
	//with param name
	public static InputStream sendPostRequests(String url, String params) {
		InputStream in = null;
		try {
			HttpPost request = new HttpPost(url);
			
			//设置HTTP POST请求参数必须用NameValuePair对象
			List<NameValuePair> paramsPost = new ArrayList<NameValuePair>();
			paramsPost.add(new BasicNameValuePair("input", params));
			UrlEncodedFormEntity stringEntity = new UrlEncodedFormEntity(paramsPost, HTTP.UTF_8);
			
			request.setEntity(stringEntity);
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
			HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpResponse httpResponse = httpClient.execute(request);
			
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				in = httpResponse.getEntity().getContent();
//				System.out.println(new String(IOUtil.read2Byte(in)));
			}
			return in;
		} catch (Exception e) {
			e.printStackTrace();
			return in;
		}
	}
	
	public static String postData(String urlStr, String data) {

		StringBuilder sb = new StringBuilder();
		URL url = null;
		HttpURLConnection conn = null;

		try {
			url = new URL(urlStr);
			conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/octet-stream");
			conn.getOutputStream().write(data.getBytes());
			conn.getOutputStream().flush();
			conn.getOutputStream().close();
			InputStream in = conn.getInputStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(in,
					"utf-8"));
			String line;
			while ((line = r.readLine()) != null) {
				sb.append(line);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return sb.toString();
	}
	
}
