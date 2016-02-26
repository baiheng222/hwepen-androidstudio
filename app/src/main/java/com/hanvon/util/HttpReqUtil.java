package com.hanvon.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class HttpReqUtil
{
private static String mEncoding="UTF-8";
/**
 * pwc 
 * @param url 请求地址 
 * @param params 请求参数
 * @return 服务器响应结果
 */
    public static String post(String url, Map<String, String> params) {
    	//封装请求体参数
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        DefaultHttpClient client = new DefaultHttpClient();
        if((params != null) && !params.isEmpty()) {
            for(Map.Entry<String, String> param : params.entrySet()) {
                list.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }
        }
        try {
        	//对请求体参数进行URL编码
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, mEncoding);
            //创建post方式的HttpRequest对象
            HttpPost httpPost = new HttpPost(url);
            //设置post方式的请求体
            httpPost.setEntity(entity);

            //执行post请求
            HttpResponse httpResponse = client.execute(httpPost);
            //ִ获得服务器的响应码
            int reponseCode = httpResponse.getStatusLine().getStatusCode();
            String result= EntityUtils.toString(httpResponse.getEntity());
            //LogUtil.getInstance().e("reponseCode:"+reponseCode+"msg:"+result);
           return result;
//            if(reponseCode == HttpStatus.SC_OK) {
//                String resultData = EntityUtils.toString(httpResponse.getEntity());
//                return resultData;
//            }
//            else
//            {
////            	 String resultData = EntityUtils.toString(httpResponse.getEntity());
//            	 String resultData = EntityUtils.toString(httpResponse.getEntity());
//                 return "Saved Info Failed-----errorCode:"+reponseCode+"\n:"+resultData;
//
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
        	if(client!=null)
        	{
        		 client.getConnectionManager().shutdown();
        	}
        }
        return "";
    }


    /**
     *
     * @param url
     * @param params
     * @param files
     * @return
     */
    public static String post(String url, Map<String, String> params,Map<String,File> files) {
    	//封装请求体参数
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        DefaultHttpClient client = new DefaultHttpClient();
        MultipartEntity mEntity= new MultipartEntity();
         int size=0;
       try {
            //params 参数
    	   if((params != null) && (size=params.size())>0) {
            for(Map.Entry<String, String> param : params.entrySet()) {
                ContentBody contentBody=new StringBody(param.getValue());
                mEntity.addPart(param.getKey(), contentBody);
            }
        } 
    	   if(files!=null){
    		   for (String key:files.keySet()) {
                   File file = files.get(key);
                   ContentBody contentBody = new FileBody(file);
                   mEntity.addPart(key, contentBody);
               }
    	   }
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(mEntity);
            HttpResponse httpResponse = client.execute(httpPost);
            int reponseCode = httpResponse.getStatusLine().getStatusCode();
           return EntityUtils.toString(httpResponse.getEntity());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
        	if(client!=null)
        	{
        		 client.getConnectionManager().shutdown();
        	}
        }
        return "";
    }
    /**
     * post 请求方式 
     * @param url 请求地址
     * @param jsonStr 请求的json串参数
     * @return
     */
        public static String post(String url,JSONObject jsonStr) {
            DefaultHttpClient client = new DefaultHttpClient();
            String resultData="";
            try {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new StringEntity(jsonStr.toString(),"utf-8"));
                HttpResponse httpResponse = client.execute(httpPost);
                //LogUtil.i("excute()");
                
                int reponseCode = httpResponse.getStatusLine().getStatusCode();            
                if(reponseCode == HttpStatus.SC_OK) {
                     resultData = EntityUtils.toString(httpResponse.getEntity());
                    return resultData;
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally{
            	if(client!=null)
            	{
            		 client.getConnectionManager().shutdown();
            	}
            }
            return resultData;
        }
        /**
         * get请求方式
         * @param url
         * @param params
         * @return
         */
        public static String get(String url,Map<String,String> params ){
        	   DefaultHttpClient client = new DefaultHttpClient();
        	   String urlPath=url+"?";
        	   int size=0;
        	  if(params!=null&&(size=params.size())>0){
        		  for(Entry<String, String> str:params.entrySet()){
        			   urlPath+=str.getKey()+"="+ str.getValue()+"&";
        		  }
        	  }
               try {
            	   //LogUtil.i(urlPath);
                   HttpGet httpPost = new HttpGet(urlPath);
                   HttpResponse httpResponse = client.execute(httpPost);
                   int reponseCode = httpResponse.getStatusLine().getStatusCode();    
                   //LogUtil.getInstance().e("reponseCode:"+reponseCode);
                       String resultData = EntityUtils.toString(httpResponse.getEntity());
                       return resultData;
                   
               } catch (IOException e) {
                   e.printStackTrace();
               }
               finally{
               	if(client!=null)
               	{
               		 client.getConnectionManager().shutdown();
               	}
               }
               return "";
        }
        
}


