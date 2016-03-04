package com.hanvon.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;
import android.widget.Toast;

import com.hanvon.application.HanvonApplication;
import com.hanvon.datas.ImageItem;
//import com.hanvon.sulupen.db.bean.NotePhotoRecord;
import com.hanvon.util.HttpClientHelper;

public class HvnCloudManager {
	private static int BUF_SIZE = 32768;

	private String htmlPageUrl = null;
	
	public String UploadNotesToHvnCloud(String title,String content,List<ImageItem> mDataList) throws IOException{
		
		String filename = SHA1Util.encodeBySHA(title)+".txt";
		String path = "/sdcard/" + filename;

		FileWriter writer = new FileWriter(path, true);
		writer.write("<div>/r/n<h1>"+title+"</h1>\r\n<p>"+content+"</p>");
		
		for(ImageItem item:mDataList){
			writer.write("\r\n<image src=\"data:image/png;base64,");
			
			String base64 = getImageBase64(item.getSourcePath());
    	    writer.write(base64);
			writer.write("\">");
			writer.flush();
		}
		writer.write("</div>");
		writer.flush();

		writer.close();
		
		String result = UploadFiletoHvn(path,title,filename);
		LogUtil.i("=============test:"+result);
		DelteTmpFile(path);
		return result;
		
	}
	
	private void DelteTmpFile(String path){
		File file = new File(path);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
	}
	
	//public void WriteFileForShareSelect(String title,String content,ArrayList<NotePhotoRecord> mDataList) throws IOException{
	public void WriteFileForShareSelect(String title,String content) throws IOException
	{
		
		String filename = SHA1Util.encodeBySHA("selectshare")+".txt";
		String path = "/sdcard/" + filename;

		FileWriter writer = new FileWriter(path, true);
		writer.write("<div>/r/n<h1>"+title+"</h1>\r\n<p>"+content+"</p>");

		/*
		for(int i=0;i<mDataList.size();i++){
			writer.write("\r\n<image src=\"data:image/png;base64,");
			
			String base64 = getImageBase64(mDataList.get(i).getLocalUrl());
    	    writer.write(base64);
			writer.write("\">");
			writer.flush();
		}
		*/
		writer.write("</div>");
		writer.flush();

		writer.close();
	}
	
	public String ShareForSelect()
	{
		String filename = SHA1Util.encodeBySHA("selectshare")+".txt";
		String path = "/sdcard/" + filename;
		
		String result = UploadFiletoHvnForShare(path,"",filename);
		LogUtil.i("=============test:"+result);
		DelteTmpFile(path);
		return result;
		
	}
	
	
	 
	
	public String UploadNotesToHvnCloudForShare(String title,String content,List<ImageItem> mDataList) throws IOException{
		
		String filename = SHA1Util.encodeBySHA(title)+".txt";
		String path = "/sdcard/" + filename;

		FileWriter writer = new FileWriter(path, true);
		writer.write("<div>/r/n<h1>"+title+"</h1>\r\n<p>"+content+"</p>");
		
		for(ImageItem item:mDataList){
			writer.write("\r\n<image src=\"data:image/png;base64,");
			
			String base64 = getImageBase64(item.getSourcePath());
    	    writer.write(base64);
			writer.write("\">");
			writer.flush();
		}
		writer.write("</div>");
		writer.flush();

		writer.close();
		
		String result = UploadFiletoHvnForShare(path,title,filename);
		LogUtil.i("=============test:"+result);
		DelteTmpFile(path);
		return result;
		
	}
	
	
	private String UploadFiletoHvnForShare(final String filePath,final String title,final String filname){
		try {
			final int blocknum;
			final byte[] buffer;
			int readBytes = BUF_SIZE;
		    boolean isSuccess;
			String requestData;
		    final File file = new File(filePath);
				
			HttpURLConnection httpurlconnection = CreateConnectionForShare();
		    String checksum =  MD5Util.getFileMD5String(file);
	
			FileInputStream fis = new FileInputStream(file); 
			int length = fis.available();

			if (length%BUF_SIZE != 0){
	            blocknum = length/BUF_SIZE + 1;
	        }else{
	            blocknum = length/BUF_SIZE;
	        }
	        if (blocknum <= 1){
	        	buffer =  new byte[length];
	        }else{
	        	buffer =  new byte[BUF_SIZE];
	        }

	        for(int i = 0;i < blocknum;i++){
	            if (i == blocknum -1){
	                readBytes = length - i*BUF_SIZE;
	            	byte[] buffer1 = new byte[readBytes];
	                readBytes = fis.read(buffer1);
	            	requestData = SendBodyForShare(checksum,Base64Utils.encode(buffer1),filname,i*BUF_SIZE,length,title);
	            }else{
	            	readBytes = BUF_SIZE;
	            	readBytes = fis.read(buffer);
	            	requestData = SendBodyForShare(checksum,Base64Utils.encode(buffer),filname,i*BUF_SIZE,length,title);
	            }

				String str = uploadFileForShare(requestData,length);
				LogUtil.i("---------str:"+str);
				JSONObject jsonObj = null;
				jsonObj = new JSONObject(str);
				if (jsonObj.get("code").equals("0")) {
				    LogUtil.i("************文件上传成功***************");
				    int offset = jsonObj.getInt("offset");
				    if (offset >= length){
				    	htmlPageUrl = jsonObj.getString("htmlPageUrl");
				    	if(htmlPageUrl == null){
				    		return null;
				    	}
				    	return htmlPageUrl;
				    }
				} else{
				    LogUtil.i("************文件上传失败***************");
				    return null;
				}
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
		    e.printStackTrace();
	    } catch (JSONException e) {
		    // TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return null;
	}
	
	
	private String SendBodyForShare(String checksum,String data,String filename,int offset,int totalLength,String title){
		JSONObject JSuserInfoJson = new JSONObject();
		
		LogUtil.i("---totalLength:"+totalLength+" ----filename:"+filename);
	  	try {

	  	  	JSuserInfoJson.put("ver", HanvonApplication.AppVer);
	  	  	JSuserInfoJson.put("userid", "");
	  	    JSuserInfoJson.put("devid", HanvonApplication.AppDeviceId);
	  	    JSuserInfoJson.put("fuid", "");
	  	    JSuserInfoJson.put("ftype", "4");
	  	    JSuserInfoJson.put("fname", filename);
	  	    JSuserInfoJson.put("title", title);
	  	    JSuserInfoJson.put("flength", totalLength);
	  	    JSuserInfoJson.put("offset", offset);
			JSuserInfoJson.put("checksum", MD5Util.md5(data));
	  	    JSuserInfoJson.put("iszip", String.valueOf(false));
	  	    JSuserInfoJson.put("data", data);
	  	}catch (JSONException e) {
	  		e.printStackTrace();
	  	}
	//  	LogUtil.i(JSuserInfoJson.toString());

	  	return JSuserInfoJson.toString();
	}
	

	private String UploadFiletoHvn(final String filePath,final String title,final String filname){
		try {
			final int blocknum;
			final byte[] buffer;
			int readBytes = BUF_SIZE;
		    boolean isSuccess;
			String requestData;
		    final File file = new File(filePath);
				
			HttpURLConnection httpurlconnection = CreateConnection();
		    String checksum =  MD5Util.getFileMD5String(file);
	
			FileInputStream fis = new FileInputStream(file); 
			int length = fis.available();

			if (length%BUF_SIZE != 0){
	            blocknum = length/BUF_SIZE + 1;
	        }else{
	            blocknum = length/BUF_SIZE;
	        }
	        if (blocknum <= 1){
	        	buffer =  new byte[length];
	        }else{
	        	buffer =  new byte[BUF_SIZE];
	        }

	        for(int i = 0;i < blocknum;i++){
	            if (i == blocknum -1){
	                readBytes = length - i*BUF_SIZE;
	            	byte[] buffer1 = new byte[readBytes];
	                readBytes = fis.read(buffer1);
	            	requestData = SendBody(checksum,Base64Utils.encode(buffer1),filname,i*BUF_SIZE,length,title);
	            }else{
	            	readBytes = BUF_SIZE;
	            	readBytes = fis.read(buffer);
	            	requestData = SendBody(checksum,Base64Utils.encode(buffer),filname,i*BUF_SIZE,length,title);
	            }

				String str = uploadFile(requestData,length);
				LogUtil.i("---------str:"+str);
				JSONObject jsonObj = null;
				jsonObj = new JSONObject(str);
				if (jsonObj.get("code").equals("0")) {
				    LogUtil.i("************文件上传成功***************");
				    int offset = jsonObj.getInt("offset");
				    if (offset >= length){
				    	htmlPageUrl = jsonObj.getString("htmlPageUrl");
				    	if(htmlPageUrl == null){
				    		return null;
				    	}
				    	return htmlPageUrl;
				    }
				} else{
				    LogUtil.i("************文件上传失败***************");
				    return null;
				}
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
		    e.printStackTrace();
	    } catch (JSONException e) {
		    // TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return null;
	}
	
	private String SendBody(String checksum,String data,String filename,int offset,int totalLength,String title){
		JSONObject JSuserInfoJson = new JSONObject();
		
		LogUtil.i("---totalLength:"+totalLength+" ----filename:"+filename);
	  	try {
	  		JSuserInfoJson.put("uid", HanvonApplication.AppUid);
	  		JSuserInfoJson.put("sid", HanvonApplication.AppSid);
	  	  	JSuserInfoJson.put("ver", HanvonApplication.AppVer);
	  	  	JSuserInfoJson.put("userid", "");
	  	    JSuserInfoJson.put("devid", HanvonApplication.AppDeviceId);
	  	    JSuserInfoJson.put("fuid", "");
	  	    JSuserInfoJson.put("ftype", "4");
	  	    JSuserInfoJson.put("fname", filename);
	  	    JSuserInfoJson.put("title", title);
	  	    JSuserInfoJson.put("flength", totalLength);
	  	    JSuserInfoJson.put("offset", offset);
			JSuserInfoJson.put("checksum", MD5Util.md5(data));
	  	    JSuserInfoJson.put("iszip", String.valueOf(false));
	  	    JSuserInfoJson.put("data", data);
	  	}catch (JSONException e) {
	  		e.printStackTrace();
	  	}
	//  	LogUtil.i(JSuserInfoJson.toString());

	  	return JSuserInfoJson.toString();
	}
	
	private HttpURLConnection CreateConnection() throws IOException{
		URL url = null;
		HttpURLConnection httpurlconnection = null;

		url = new URL("http://dpi.hanvon.com/rt/ap/v1/store/upload");
		httpurlconnection = (HttpURLConnection) url.openConnection();
		httpurlconnection.setConnectTimeout(60*1000);
		httpurlconnection.setUseCaches(false);
		httpurlconnection.setDoInput(true);
		httpurlconnection.setDoOutput(true);
		
		//1.设备请求类型
		httpurlconnection.setRequestMethod("POST");
		
		//2.设备请求头
		httpurlconnection.setRequestProperty("token","wdf34568koisjfsjkj");
		httpurlconnection.setRequestProperty("Content-Type","application/octet-stream");

		return httpurlconnection;
	}
	
	
	private HttpURLConnection CreateConnectionForShare() throws IOException{
		URL url = null;
		HttpURLConnection httpurlconnection = null;

		//url = new URL("http://cloud.hwyun.com/dws-cloud/rt/ap/v1/store/sharedata");
		url = new URL("http://dpi.hanvon.com/rt/ap/v1/app/note/sharedata");               
		httpurlconnection = (HttpURLConnection) url.openConnection();
		httpurlconnection.setConnectTimeout(60*1000);
		httpurlconnection.setUseCaches(false);
		httpurlconnection.setDoInput(true);
		httpurlconnection.setDoOutput(true);
		
		//1.设备请求类型
		httpurlconnection.setRequestMethod("POST");
		
		//2.设备请求头
		httpurlconnection.setRequestProperty("token","wdf34568koisjfsjkj");
		httpurlconnection.setRequestProperty("Content-Type","application/octet-stream");

		return httpurlconnection;
	}
	
	public String uploadFileForShare(final String requestData,int length) throws IOException, JSONException{
	    HttpURLConnection httpurlconnection = null;
		httpurlconnection = CreateConnectionForShare();
		//3加密请求数据
		httpurlconnection.getOutputStream().write(requestData.getBytes());
		//5发送数据
		httpurlconnection.getOutputStream().flush();
		//5接收结果数据
		InputStream in = null;
		in = httpurlconnection.getInputStream();
		
		BufferedReader r = null;
		r = new BufferedReader(new InputStreamReader(in,"utf-8"));

		String line;
		StringBuilder sb = new StringBuilder();

		while ((line = r.readLine()) != null) {
			sb.append(line);
		}

		LogUtil.i("--------"+sb.toString());
		return sb.toString();
    }

	public String uploadFile(final String requestData,int length) throws IOException, JSONException{
	    HttpURLConnection httpurlconnection = null;
		httpurlconnection = CreateConnection();
		//3加密请求数据
		httpurlconnection.getOutputStream().write(requestData.getBytes());
		//5发送数据
		httpurlconnection.getOutputStream().flush();
		//5接收结果数据
		InputStream in = null;
		in = httpurlconnection.getInputStream();
		
		BufferedReader r = null;
		r = new BufferedReader(new InputStreamReader(in,"utf-8"));

		String line;
		StringBuilder sb = new StringBuilder();

		while ((line = r.readLine()) != null) {
			sb.append(line);
		}

		LogUtil.i("--------"+sb.toString());
		return sb.toString();
    }
	
	private String getImageBase64(String srcPath) throws IOException{
		String imageBase64 = "";
		Bitmap rawBitmap = BitmapFactory.decodeFile(srcPath,null);
		int w = rawBitmap.getWidth();
        int h = rawBitmap.getHeight();
        LogUtil.i("w:"+w+"         h:"+h);
        if (w > 400 || h > 400){
        	float hh = 400f;//这里设置高度为800f
            float ww = 400f;//这里设置宽度为480f
            float be = 1;
            if ((w > h) && (w > ww)) {//如果宽度大的话根据宽度固定大小缩放
            	be = (float)(ww/w);
            } else if ((w < h) && (h > hh)) {//如果高度高的话根据宽度固定大小缩放
            	be = (float)(hh/h);
            }
            if (be <= 0)
                be = 1;
            Matrix matrix = new Matrix();
            matrix.postScale(be, be);
            Bitmap bitmap = Bitmap.createBitmap(rawBitmap, 0, 0, w, h,matrix,true);

      //      File file=new File("/sdcard/DCIM/01_1.png");//将要保存图片的路径  头像文件
            try {
            	ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);
                out.flush();
                out.close();
                byte[] imgBytes = out.toByteArray();
             //   imageBase64 = Base64.encodeToString(imgBytes, Base64.DEFAULT);
                imageBase64 = Base64Utils.encode(imgBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
        	ByteArrayOutputStream out = new ByteArrayOutputStream();
        	rawBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            byte[] imgBytes = out.toByteArray();
         //   imageBase64 = Base64.encodeToString(imgBytes, Base64.DEFAULT);
            imageBase64 = Base64Utils.encode(imgBytes);
        }

     //   base64ToBitmap(imageBase64,"DCIM/01_3.png","JPG");
 //       LogUtil.i("imageBase64:"+imageBase64);
        return imageBase64;
	}

	public String getHtmlPageUrl(){
		return htmlPageUrl;
	}
}
