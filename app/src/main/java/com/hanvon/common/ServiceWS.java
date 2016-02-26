package com.hanvon.common;

public class ServiceWS {
	/**
	 * 升级 和 反馈 接口单独定义
	 */
	public static final String IPADDRESS = "http://cloud.hwyun.com/dws-cloud/";
//	public static final String IPADDRESS = "http://dpi.hanvon.com/";
//	public static final String IPADDRESS = "http://192.168.134.72:8080/ws/";
	
	//登录
	public static String LOGIN = IPADDRESS + "rt/ap/v1/user/login";
	//注册
	public static String REGISTER = IPADDRESS + "rt/ap/v1/user/register";
	//检查重名
	public static String CHKNAME = IPADDRESS + "rt/ap/v1/user/chkname";
	//新建文件
	public static String FILE_ADD = IPADDRESS + "rt/ap/v1/app/file/add";
	//保存文件
	public static String FILE_SAVE = IPADDRESS + "rt/ap/v1/app/file/save";
	//获取文件列表
	public static String FILE_LIST = IPADDRESS + "rt/ap/v1/app/file/list";
	//获取文件内容
	public static String FILE_GETCNT = IPADDRESS + "rt/ap/v1/app/file/getcnt";
	//查询文件
	public static String FILE_SEARCH = IPADDRESS + "rt/ap/v1/app/file/search";
	//删除文件
	public static String FILE_DELETE = IPADDRESS + "rt/ap/v1/app/file/delete";
	//下载多个文件（返回zip流）
	public static String FILE_DOWNLOAD = IPADDRESS + "pub/file/download.do";
	//下载单个文件（返回文件流）
	public static String FILE_SINGLE_DOWNLOAD = IPADDRESS + "pub/file/singledownload.do";
	//生词本应用接口
	public static String WORDS_SEARCH = IPADDRESS + "rt/ap/v1/app/words/search";
	public static String WORDS_UPDATE = IPADDRESS + "rt/ap/v1/app/words/update";
	//整句应用接口
	public static String SENTENCE_SEARCH = IPADDRESS + "rt/ap/v1/app/sentence/search";
	public static String SENTENCE_DELETE = IPADDRESS + "rt/ap/v1/app/sentence/delete";
	//录音应用接口
	public static String LABLE_LIST = IPADDRESS + "rt/ap/v1/app/recording/getlabel";
	public static String RECORDGETCNT = IPADDRESS + "rt/ap/v1/app/recording/getcnt";
	public static String RECORDSAVECNT = IPADDRESS + "rt/ap/v1/app/recording/savecnt";
	public static String WAVELENGT = IPADDRESS + "rt/ap/v1/app/recording/wavelengt";
	
	public static String UPDATE = IPADDRESS + "rt/ap/v1/pub/std/soft/upg";
	public static String FEEDBACK = IPADDRESS + "rt/ap/v1/pub/std/heatmap/send";
	
	
}
