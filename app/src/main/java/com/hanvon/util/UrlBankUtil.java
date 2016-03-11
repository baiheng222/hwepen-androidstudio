package com.hanvon.util;

import com.hanvon.common.ServiceWS;

public class UrlBankUtil
{
	// 服务器对应ip地址:61.135.185.83
	private static String ip = "http://hanwang.duapp.com:80";
	//private static String ipHanvon = "http://dpi.hanvon.com/";
	private static String ipHanvon = ServiceWS.IPADDRESS;
	//	private static String HanvanApkIp = "http://cloud.hwyun.com/dws-cloud/rt/ap/v1";
	//private static String HanvanApkIp = "http://dpi.hanvon.com/rt/ap/v1";
	private static String HanvanApkIp = ipHanvon + "/rt/ap/v1";
	// 本地IP地址
	// public static String UrlRootPath="http://192.168.133.45:8080";
	private static String baiduTranslateIp = "http://openapi.baidu.com/public/2.0/bmt/translate";
	private static String mServerIp;

	/**
	 * 获取服务器IP URL
	 * 
	 * @return 服务器 IP
	 */
	public static String getServerAddress() {
		mServerIp = ip;
		return mServerIp;
	}

	/**
	 * 获取服务器IP URL
	 * 
	 * @return 服务器 IP
	 */
	public static String getHanvonServerAddress() {
		mServerIp = ipHanvon;
		return mServerIp;
	}

	/**
	 * 获取服务器上注册用户信息的URL
	 * 
	 * @return
	 */
	public static String getRegisterUser() {
		return getServerAddress() + "/mysql/basic";
	}

	/**
	 * 获取服务器上更新用户信息的URL
	 * 
	 * @return
	 */
	public static String getUpdateUser() {
		return getServerAddress() + "";
	}

	/**
	 * 获取服务器上app版本信息的URL
	 * 
	 * @return
	 */
	public static String getAppVersion() {
		return getServerAddress() + "/app/version";
	}

	/**
	 * 通讯录备份URL
	 * 
	 * @return
	 */
	public static String getMailListBackup() {
		return getServerAddress() + "/mailList/backup";
	}

	/**
	 * 翻译URL
	 * 
	 * @return
	 */
	public static String getTranslate() {
		return baiduTranslateIp;
	}

	/**
	 * 汉王云用户登录URL
	 * 
	 * @return
	 */
	public static String getUserLogin() {
		return getHanvonServerAddress() + "rt/ap/v1/user/login";
	}

	/**
	 * 汉王云用户注册URL
	 * 
	 * @return
	 */
	public static String getUserRegister() {
	//	return getHanvonServerAddress() + "rt/ap/v1/user/register";
		return HanvanApkIp + "/user/register";
	}

	/**
	 * 汉王云用户重命名检测URL
	 * 
	 * @return
	 */
	public static String getCheckName() {
		return getHanvonServerAddress() + "rt/ap/v1/user/chkname";
	}

	/**
	 * 用于汉王服务器的 版本升级URL
	 * 
	 * @return
	 */
	public static String getAppVersionUpdate() {
		return getHanvonServerAddress() + "rt/ap/v1/pub/std/soft";
	}
	/*获取验证码*/
	public static String getauthcode(){
		return  HanvanApkIp + "/user/getauthcode";
	}
	/**
	 * 重置密码
	 */
	public static String resetpassword(){
		return  HanvanApkIp + "/user/phoneResetPwd";
	}
	
	/**
	 * 软件升级接口
	 */
	public static String softUpdate(){
		return  HanvanApkIp + "/pub/std/soft/upg";
	}
	
	/**
	 * qq第三方账号注册到汉王云
	 */
	public static String qqUserToHvn(){
		return  HanvanApkIp + "/user/qqregister";
	}
	
	/**
	 * 微信第三方账号注册到汉王云
	 */
	public static String wxUserToHvn(){
		return  HanvanApkIp + "/user/wxregister";
	}
	
	/**
	 * 汉王用户通过用户名获取用户信息
	 */
	public static String getUserInfo(){
		return  HanvanApkIp + "/user/getcontractinfo";
	}
	
	/**
	 * 汉王用户通过手机注册获取验证码
	 */
	public static String getphoneauthcode(){
		return  HanvanApkIp + "/user/getphoneauthcode";
	}
	
	/**
	 * 汉王用户通过邮箱注册找回密码
	 */
	public static String getEmailToUser(){
		return  HanvanApkIp + "/user/findPwdByEmail";
	}
	/**
	 * 汉王用户通过邮箱注册获取激活邮件
	 */
	public static String getActivityEmai(){
		return  HanvanApkIp + "/user/sendActiveEmail";
	}

	/**
	 * 汉王用户通过手机注册获取验证码
	 */
	public static String getcodeForPasswd(){
		return  HanvanApkIp + "/user/getauthcode";
	}

	/**
	 * 汉王用户通过手机注册校验验证码
	 */
	public static String checkphoneauthcode(){
		return  HanvanApkIp + "/user/checkphoneauthcode";
	}
	
	/**
	 * 修改昵称
	 */
	public static String modifyNickname(){
		return  HanvanApkIp + "/user/changenickname";
	}
	
	/**
	 * 修改密码
	 */
	public static String modifyPassword(){
		return  HanvanApkIp + "/user/changepassword";
	}
	
	/**
	 * 文件上传
	 */
	public static String uploadFile(){
		return  HanvanApkIp + "/store/upload";
	}
	
	/**
	 * 三方绑定
	 */
	public static String thirdBind(){
		return  HanvanApkIp + "/user/thirdbinding";
	}
	
	/**
	 * 三方解绑定
	 */
	public static String thirdUnBind(){
		return  HanvanApkIp + "/user/thirdunbind";
	}
}

