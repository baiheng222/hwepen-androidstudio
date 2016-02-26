package com.hanvon.util;

import android.annotation.SuppressLint;
import java.security.MessageDigest;

//进行SHA-1加密
public class SHA1Util {

	/**
	 * 转换字节数组为十六进制字符串
	 * 
	 * @param b
	 * @return
	 */
	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i",
			"j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
			"w", "x", "y", "z" };

	public static String byteArrayToHexString(byte[] b) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < b.length; i += 2) {
			stringBuffer.append(byteToHexString(b[i], b[i + 1]));
		}
		return stringBuffer.toString();
	}

	/**
	 * 将一个字节转化成十六进制形式的字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String byteToHexString(byte b, byte p) {
		int n = b;
		if (n < 0) {
			n = 256 + n;
		}
		int m = p;
		if (m < 0) {
			m = m + 256;
		}
		int d1 = (m + n) % 36;
		// int d2 = m % 16;
		return hexDigits[d1];// + hexDigits[d2];
	}

	public static String byteArrayToNumber(byte[] rawHmac) {
		StringBuffer result = new StringBuffer(10);
		for (int i = 0; i < 20; i += 2) {
			int n = rawHmac[i] & 0xFF;
			result.append((char) ('0' + n % 10));
		}
		return result.toString();
	}

	/**
	 * 字符串SHA-1加密
	 * 
	 * @param string
	 * @return
	 */
	@SuppressLint("NewApi") public static String encodeBySHA(String string) {
		if (string != null && !string.isEmpty()) {
			try {
				// 创建具有指定算法名称的信息摘要
				MessageDigest messageDigest = MessageDigest
						.getInstance("SHA-1");
				// 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
				byte[] bytes = messageDigest.digest(string.getBytes());
				// 将得到的字节数组变成字符串返回
				// string = byteToNumber(bytes);
				string = byteArrayToHexString(bytes);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return string;
	}
}

