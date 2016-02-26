package com.hanvon.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceChecker {
	
	//check mobile phone
	public static boolean isMobileNO(String mobiles) {
		String str = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
		Pattern pattern = Pattern.compile(str);
		Matcher m = pattern.matcher(mobiles);
		return m.matches();
	}
	
	//check email
	public static boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
		Pattern pattern = Pattern.compile(str);
		Matcher m = pattern.matcher(email);
		return m.matches();
	}
	
	//is all Number
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher m = pattern.matcher(str);
		return m.matches();
	}
}
