/**
 * COPYRIGHT NOTICE: 2010 www.hanvon.com
 *
 */

package com.hanvon.util;

import java.io.File;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	
	public static String generateUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	/**
	 * 
	 * @param word
	 */
	public static String firstLetterToUpper(String word) {
		word = word.substring(0, 1).toUpperCase() + word.substring(1);
		return word;
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String reverse(String str) {
		StringBuffer strBuf = new StringBuffer(str);
		return strBuf.reverse().toString();
	}

	/**
	 * 
	 * @param strs
	 * @return
	 */
	public static boolean isEmpty(Object... strs) {
		if (null == strs) {
			return true;
		}
		for (Object s : strs) {
			if (!(null == s || (null != s && "".equals(s)))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param strs
	 * @return
	 */
	public static boolean isEmptyByTrim(Object... strs) {
		if (null == strs) {
			return true;
		}
		for (Object s : strs) {
			if (!(null == s || (null != s && "".equals(s.toString().trim())))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param strs
	 * @return
	 */
	public static boolean isNotEmpty(Object... strs) {
		if (null == strs) {
			return false;
		}
		for (Object s : strs) {
			if (null == s || (null != s && "".equals(s))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param strs
	 * @return
	 */
	public static boolean isNotEmptyByTrim(Object... strs) {
		if (null == strs) {
			return false;
		}
		for (Object s : strs) {
			if (null == s || (null != s && "".equals(s.toString().trim()))) {
				return false;
			}
		}
		return true;
	}




	public static boolean isNullOrEmpty(String str) {
		return ((null == str) || (str.trim().equals("")));
	}

	

	public static String concatStr(String... strs){
		if(null!=strs&&strs.length>0){
			StringBuilder sb=new StringBuilder();
			for(String str:strs){
				sb.append(str);
			}
			return sb.toString();
		}else{
			return null;
		}
	}
	public static String getPath(String... paths){
		if(null!=paths&&paths.length>0){
			StringBuilder sb=new StringBuilder();
			for(String str:paths){
				sb.append(str);
				sb.append(File.separator);
			}
			String str=sb.toString();
			return str.substring(0,str.length()-1);
		}else{
			return null;
		}
	}
	

	public static boolean ishanzi(char c){
		 int count = 0;       
		 String regEx = "[\\u4e00-\\u9fa5]";       
		    
		 String str = String.valueOf(c);       
		 
		 Pattern p = Pattern.compile(regEx);       
		 Matcher m = p.matcher(str);       
		 while (m.find()) {       
			 for (int i = 0; i <= m.groupCount(); i++) {       
				 count = count + 1;       
			 }       
		 }       
		 
		 return count>0;
	}
	public static boolean iszimu(char c){
		int cc=(int)c;
		return (cc>=65&&cc<=90)||(cc>=95&&cc<=122);
	}
	public static boolean isshuzi(char c){
		int cc=(int)c;
		return (cc>=48&&cc<=57);
	}
	
}
