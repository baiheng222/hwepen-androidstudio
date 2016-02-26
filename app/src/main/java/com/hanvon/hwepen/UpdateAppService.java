package com.hanvon.hwepen;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * App自动更新之通知栏下载 
 * @author 402-9 
 * 
 */  
@SuppressWarnings("unused")
public class UpdateAppService
{
    private Context context;
    private String updateUrl;
    private int updateType;
    private ProgressDialog pd;
    
    private boolean isCancel = false;
    
    public UpdateAppService() {
		super();
	}
    public UpdateAppService(Context context, int flag) {
    	this.context=context;
    	this.updateType = flag;
  	}

	//创建通知 
	public void CreateInform(String Url)
	{
		updateUrl = Url;

		pd = new ProgressDialog(context);
	    pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    if (updateType == 2){
	    	pd.setMessage("正在下载硬件新版本");
	    }else{
	    	pd.setMessage("正在下载更新");
	    }
	    
	    pd.setCancelable(false);
	    if (updateType != 2)
		{
	        pd.setButton(ProgressDialog.BUTTON2, "取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1)
			{
				    // TODO Auto-generated method stub
				    pd.dismiss();
				    isCancel = true;
			    }
		    });
	    }
	    pd.show();
	    
        new Thread(new updateRunnable()).start();
        
    }  
    class updateRunnable implements Runnable
	{
        int downnum = 0;//已下载的大小  
        int downcount= 0;//下载百分比  
	    
        @Override
        public void run() {  
            // TODO Auto-generated method stub  
            try {  
            	File file = getFileFromServer(updateUrl);
            //	installApk(file);
            } catch (Exception e) {
                // TODO Auto-generated catch block  
                e.printStackTrace();  
            }  
        }
        public File getFileFromServer(String path) throws Exception
		{
    		File file = null;
    	//    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
    	        URL url = new URL(path);
    	        HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
    	        conn.setConnectTimeout(5000);
    	        //获取到文件的大小
    	        pd.setMax(conn.getContentLength());
    	        int length = conn.getContentLength();
    	        InputStream is = conn.getInputStream();
    	        if (updateType == 1)
				{
    	        	 File f = new File("/sdcard/"+"SuluPen.apk");
    	        	 if (f.exists())
					 {
    	        		 f.delete();
    	        	 }
    	        	 file = new File("/sdcard/", "SuluPen.apk");
    	        }
				else if (updateType == 2)
				{

    	        }
    	        FileOutputStream fos = new FileOutputStream(file);
    	        BufferedInputStream bis = new BufferedInputStream(is);
    	        byte[] buffer = new byte[1024];
    	        int len = 0;
    	        int total = 0;

    	        while((len =bis.read(buffer))!=-1){
    	            fos.write(buffer, 0, len);
    	            downnum += len;  
                    if((downcount == 0)||(int) (downnum*100/length)-1>downcount){   
                        downcount += 1;
                       // downloadDialog.setProgress(downnum);
                        pd.setProgress(downnum);
                        if (isCancel){
                        	break;
                        }
                    }
    	        }

    	        fos.close();
    	        bis.close();
    	        is.close();
    	        if (isCancel){
    	        	isCancel = false;
    	        	return null;
    	        }
    	        if (downnum==length)
				{
    	        	if (updateType == 1)
					{
    	        		Intent installIntent = new Intent(Intent.ACTION_VIEW);
    	        	    installIntent.setClassName(context,installApk(file));
    	        	}
					else if(updateType == 2)
					{
    	        		/******************************/
    	        		 HashMap<String, String> params=new HashMap<String, String>();
    	    			 params.put("update_mode", ""+11);
    	    		     pd.dismiss();
    	        	}
                 
                }
    	        return file;
    	 
        }
        
    }
    	/**
    	 * 5.安装下载的Apk软件
    	 * @return 
    	  */
    	protected String installApk(File file)
		{
    		//HanvonApplication.path = Uri.fromFile(file).toString();

    	    Intent intent = new Intent(Intent.ACTION_VIEW);
    	    //执行动作
    	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    //执行的数据类型
    	    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
    	    context.startActivity(intent);
    	    android.os.Process.killProcess(android.os.Process.myPid());

    	    return null;
    	}


	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}  
	
	public void onDestory(){
	//	super.onDestroy();
	}
 }