package com.hanvon.util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpsClient{
	public  String  HttpsRequest(final String path, final List<NameValuePair> parameters){
		try{
			HttpClient client = getCustomClient();
			HttpPost httpPost = new HttpPost(path);

			if (parameters != null){
		        httpPost.setEntity(new UrlEncodedFormEntity(parameters,"utf-8"));
			}
			HttpResponse response = null;

		    response = client.execute(httpPost);
			int status = response.getStatusLine().getStatusCode();
			
			if (status == 200){
				//    String resultData = EntityUtils.toString(response.getEntity());
				String resultData = EntityUtils.toString(response.getEntity(),HTTP.UTF_8);
				LogUtil.i("------"+resultData);
				    return resultData;
			}
		}catch (IOException e){
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public static HttpClient getCustomClient() {
	    BasicHttpParams params = new BasicHttpParams();
	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	    HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
	    HttpProtocolParams.setUseExpectContinue(params, true);
	    
	    SchemeRegistry schReg = new SchemeRegistry();
	//    schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    schReg.register(new Scheme("https", MySSLSocketFactory.getSocketFactory(), 443));
	    
	    ClientConnectionManager connMgr = new ThreadSafeClientConnManager(params, schReg);
	    
	    return new DefaultHttpClient(connMgr, params);
	  }
}

 class MySSLSocketFactory extends SSLSocketFactory {
	  SSLContext sslContext = SSLContext.getInstance("TLS");

	  public MySSLSocketFactory(KeyStore truststore)
	      throws NoSuchAlgorithmException, KeyManagementException,
	      KeyStoreException, UnrecoverableKeyException {
	    super(truststore);
	    TrustManager tm = new X509TrustManager() {

	      public X509Certificate[] getAcceptedIssuers() {
	        return null;
	      }

	      public void checkServerTrusted(X509Certificate[] chain,
	          String authType) throws CertificateException {

	      }

	      public void checkClientTrusted(X509Certificate[] chain,
	          String authType) throws CertificateException {

	      }
	    };

	    sslContext.init(null, new TrustManager[] { tm }, null);
	  }

	  @Override
	  public Socket createSocket() throws IOException {
	    return sslContext.getSocketFactory().createSocket();
	  }

	  @Override
	  public Socket createSocket(Socket socket, String host, int port,
	      boolean autoClose) throws IOException, UnknownHostException {
	    return sslContext.getSocketFactory().createSocket(socket, host, port,
	        autoClose);
	  }

	  public static SSLSocketFactory getSocketFactory() {
	    try {
	      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	      trustStore.load(null, null);
	      SSLSocketFactory factory = new MySSLSocketFactory(trustStore);
	      return factory;
	    } catch (Exception e) {
	      e.getMessage();
	      return null;
	    }
	  }
}
