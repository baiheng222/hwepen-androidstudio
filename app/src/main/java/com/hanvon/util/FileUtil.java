package com.hanvon.util;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.zip.ZipInputStream;

public class FileUtil {
	
	private static String SDPATH = Environment.getExternalStorageDirectory()+"";
	
	/**
	 * 保存文本到内存
	 * @param context
	 * @param filename
	 * @param content
	 * @param mode
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static void saveTextInMemory(Context context, String filename,
			String content, int mode) throws UnsupportedEncodingException, IOException
	{
		FileOutputStream out = context.openFileOutput(filename, mode);
		out.write(content.getBytes("UTF-8"));
		out.close();
	}
	
	/**
	 * 从内存读取文件
	 * @param context
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static String loadTextFromMemory(Context context, String filename) throws IOException
	{
		FileInputStream in = context.openFileInput(filename);
		byte[] data = read2byte(in);
		return new String(data, "UTF-8");
	}

	/**
	 * 保存文本到sdcard
	 * @param filename
	 * @param content
	 * @throws IOException
	 */
	public static void saveTextInSdcard(String filename, String content) throws IOException
	{
		File f = new File(Environment.getExternalStorageDirectory(), filename);
		FileOutputStream out = new FileOutputStream(f);
		out.write(content.getBytes("UTF-8"));
		out.close();
	}

	/**
	 * 从sdcard读取文本文件
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static String loadTextFromSdcard(String filename) throws IOException
	{
		File f = new File(Environment.getExternalStorageDirectory(), filename);
		FileInputStream in = new FileInputStream(f);
		byte[] data = read2byte(in);
		return new String(data, "UTF-8");
	}
	
	private static byte[] read2byte(InputStream in) throws IOException
	{
		byte[] data;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			bout.write(buf, 0, len);
		}
		data = bout.toByteArray();
		return data;
	}
	
	/**
	 * 在SD卡上创建文件 （带创建文件夹）
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
    public static File createSDFile(String fileName) throws IOException
	{
    	//文件夹不存在先创建文件夹
    	String[] dirs = fileName.split("/");
    	String path = "";
    	for (int i = 0; i < dirs.length-1; i++) {
    		path += "/" + dirs[i];
    		if (!fileExist(dirs[i])) {
				createSDDir(path);
			}
		}
        File file = new File(SDPATH + "/" + fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 在SD卡上创建目录
     * @param dirName
     * @return
     */
    public static File createSDDir(String dirName) {
        File dir = new File(SDPATH + "/" + dirName);
        dir.mkdir();
        return dir;
    }

    /**
     * 判断SD卡上的文件及文件夹夹是否存在
     * @param filePath
     * @return
     */
    public static boolean fileExist(String filePath) {
		File file = new File(SDPATH + "/" + filePath);
		return file.exists();
	}
    
    /**
     * 删除文件（删除空文件夹）
     * @param filePath
     * @return
     */
    public static boolean delFile(String filePath) {
    	File file = new File(SDPATH + "/" + filePath);
    	return file.delete();
    }
    
    /**
     * 保存流文件
     * @param filePath
     * @param in
     * @return
     * @throws IOException
     */
    public static File writeToSDCard(String filePath, InputStream in) throws IOException
	{
    	OutputStream out = null;
    	File file = new File(SDPATH + "/" + filePath);
    	if (fileExist(filePath)) {
    		file.delete();
		}
    	createSDFile(filePath);
    	out = new FileOutputStream(file);
    	byte[] buffer = new byte[1024];
    	while ((in.read(buffer)) != -1) {
    		out.write(buffer);
    	}
    	out.flush();
    	out.close();
    	in.close();
    	return file;
    }
    
    
    
//    public static File write2SDFromInput(String path, String fileName,
//    		InputStream input) {
//    	File file = null;
//    	OutputStream output = null;
//    	try {
//    		createSDDir(path);
//    		file = createSDFile(path + fileName);
//    		output = new FileOutputStream(file);
//    		byte[] buffer = new byte[4 * 1024];
//    		while ((input.read(buffer)) != -1) {
//    			output.write(buffer);
//    		}
//    		output.flush();
//    	} catch (Exception e) {
//    		e.printStackTrace();
//    	} finally {
//    		try {
//    			output.close();
//    		} catch (IOException e) {
//    			e.printStackTrace();
//    		}
//    	}
//    	return file;
//    }

    /**
     * 在sd卡中读出文本文件 返回String * strFullPath 读取文件的完整路径
     */
    public static String ReadSDFiled(String strFullPath) {

        File file = new File(SDPATH + "/" + strFullPath);
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e3) {
            e3.printStackTrace();
        }

        BufferedReader br = null;
        try {
            /* 转换编码 */
            br = new BufferedReader(new InputStreamReader(in, "gb2312"));
            /* 不转换编码 */
            // br = new BufferedReader(new InputStreamReader(in));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        StringBuffer sb = new StringBuffer();
        String tmp;
        try {
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp + "\n");
            }
            br.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
    
    /** 
     * 解压一个压缩文档 到指定位置 
     * @param zipFileString 压缩包的名字 
     * @param outPathString 指定的路径 
     * @throws Exception
     */  
    public static void UnZipFolder(InputStream in, String outPathString)throws Exception
	{
        android.util.Log.v("XZip", "UnZipFolder(String, String)");  
        ZipInputStream inZip = new ZipInputStream(new BufferedInputStream(in));
        java.util.zip.ZipEntry zipEntry;  
        String szName = "";
          
        while ((zipEntry = inZip.getNextEntry()) != null) {  
            szName = zipEntry.getName();  
          
            if (zipEntry.isDirectory()) {  
          
                // get the folder name of the widget  
                szName = szName.substring(0, szName.length() - 1);  
                java.io.File folder = new java.io.File(outPathString + java.io.File.separator + szName);
                folder.mkdirs();

            } else {
            	System.out.println(szName);

                java.io.File file = new java.io.File(outPathString + java.io.File.separator + szName);
                file.createNewFile();
                // get the output stream of the file
                java.io.FileOutputStream out = new java.io.FileOutputStream(file);
                int len;  
                byte[] buffer = new byte[1024];  
                // read (len) bytes into buffer  
                while ((len = inZip.read(buffer)) != -1) {  
                    // write (len) byte from buffer at the position 0  
                    out.write(buffer, 0, len);  
                    out.flush();  
                }  
                out.close();  
            }  
        }//end of while  
          
        inZip.close();  
      
    }//end of func  
    
	public static String getFormatSize(double size) {
		double kiloByte = size / 1024;
		if (kiloByte < 1) {
			return size + "B";
		}

		double megaByte = kiloByte / 1024;
		if (megaByte < 1) {
			BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
			return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "K";
		}

		double gigaByte = megaByte / 1024;
		if (gigaByte < 1) {
			BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
			return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "M";
		}

		double teraBytes = gigaByte / 1024;
		if (teraBytes < 1) {
			BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
			return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "G";
		}
		BigDecimal result4 = new BigDecimal(teraBytes);
		return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "T";
	}
}
