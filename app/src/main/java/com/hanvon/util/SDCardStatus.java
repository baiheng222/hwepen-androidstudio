package com.hanvon.util;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;

public class SDCardStatus {

	//exits
	public static boolean existSDCard() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	
	//total capacity
	public static long getSDAllSize() {
		File path = Environment.getExternalStorageDirectory();//SDCard path
		StatFs sf = new StatFs(path.getPath());
		long blockSize = sf.getBlockSize();//single block size(Byte)
		long allBlocks = sf.getBlockCount();//all blocks
//		return allBlocks * blockSize; //Byte
//		return (allBlocks * blockSize)/1024; //KB
		return (allBlocks * blockSize)/1024/1024; //MB
	}
	
	//free size
	public static long getSDFreeSize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		long blockSize = sf.getBlockSize();
		long freeBlocks = sf.getAvailableBlocks();
//		return freeBlocks * blockSize; //Byte
//		return (freeBlocks * blockSize)/1024; //KB
		return (freeBlocks * blockSize)/1024/1024; //MB
	}
}
