package com.hanvon.net;

import com.hanvon.util.HttpReqUtil;
import com.hanvon.util.UrlBankUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.Map;

/**
 * @desc 与服务器端的交互类，所有接口的调用
 * @author  PengWenCai
 * @time 2015-6-25 上午11:17:33
 * @version
 */
public class RequestServerData
{
	private static final String CUT_TOKEN = "{\"";

	public static enum HTTPTYPE {
		GET, POST, POSTFILES
	}

	public static RequestResult request(HTTPTYPE httpType, String url,
			Map<String, String> params, Map<String, File> files) {
		RequestResult result = new RequestResult();
		String info = null;
		try {

			if (httpType == HTTPTYPE.POST) {
				// 可做写参数的加密处理
				info = HttpReqUtil.post(url, params);
			} else if (httpType == HTTPTYPE.GET) {
				info = HttpReqUtil.get(url, params);
				// 可做写参数的加密处理
			} else if (httpType == HTTPTYPE.POSTFILES) {
				info = HttpReqUtil.post(url, params, files);
			}

		} catch (Exception e) {
			//LogUtil.e(e.toString());
			result.setResultCode(RequestResult.RESULT_NET_ERR);

		}
		if (info == null) {
			return result;
		}
		try {
			int cut = info.indexOf(CUT_TOKEN);
			if (cut != -1) {
				info = info.substring(cut);
			}
			JsonData data = new JsonData(info);
			result.setData(data);
			result.setResultCode(RequestResult.RESULT_OK);
		} catch (Exception e) {
			//LogUtil.e(e.toString());
			result.setResultCode(RequestResult.RESULT_PARSE_ERR);
		}
		return result;
	}

	/**
	 * json为载体
	 * 
	 * @param httpType
	 * @param url
	 * @param jsonData
	 * @return
	 */
	public static RequestResult simpleRequest(HTTPTYPE httpType, String url,
			JSONObject jsonData) {
		RequestResult result = new RequestResult();
		String info = null;
		//LogUtil.i("++++++++"+jsonData.toString());
		try {
			if (httpType == HTTPTYPE.POST) {
				// 可做写参数的加密处理
				info = HttpReqUtil.post(url, jsonData);
			}
		} catch (Exception e) {
			//LogUtil.e(e.toString());
			result.setResultCode(RequestResult.RESULT_NET_ERR);
		}
		if (info == null) {
			return result;
		}
		try {
			int cut = info.indexOf(CUT_TOKEN);
			if (cut != -1) {
				info = info.substring(cut);
			}
			JsonData data = new JsonData(info);
			result.setData(data);
			result.setResultCode(RequestResult.RESULT_OK);
		} catch (Exception e) {
			//LogUtil.e(e.toString());
			result.setResultCode(RequestResult.RESULT_PARSE_ERR);
		}
		return result;
	}

	public static RequestResult appVersion(Map<String, String> params) {
		return request(HTTPTYPE.POST, UrlBankUtil.getAppVersion(), params, null);

	}

	public static RequestResult mailListBackup(Map<String, String> params) {
		return request(HTTPTYPE.POST, UrlBankUtil.getMailListBackup(), params,
				null);

	}

	public static RequestResult translate(Map<String, String> params) {
		return request(HTTPTYPE.GET, UrlBankUtil.getTranslate(), params, null);
	}

	/*
	 * 用户登录接口 已测，ok
	 * 
	 * @param params
	 * @return
	 */
	public static RequestResult userLogin(JSONObject jsonData) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.getUserLogin(),
				jsonData);
	}

	/*
	 * 用户注册接口 已测，ok
	 * 
	 * @param params
	 * @return
	 */
	public static RequestResult userRegister(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.getUserRegister(), json);
	}

	/*
	 * 用户名重名检测接口
	 * 
	 * @param params
	 * @return
	 */
	 public static RequestResult checkName(JSONObject json){
		 return simpleRequest(HTTPTYPE.POST, UrlBankUtil.getCheckName(), json);
	 }
	//public static RequestResult checkName(Map<String, String> params) {
	//	return request(HTTPTYPE.POST, UrlBankUtil.getCheckName(), params, null);
	//}

	public static RequestResult appVersionUpdate(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.getAppVersionUpdate(),
				json);

	}
	
	/*发送验证码*/
	public static RequestResult sendCodeToUser(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.getauthcode(),json);
	}
	/*重置密码*/
	public static RequestResult resetPasswdForUser(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.resetpassword(),json);
	}
	
	/*软件升级*/
	public static RequestResult softUpdate(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.softUpdate(),json);
	}
	
	/*注册qq三方账号到汉王云*/
	public static RequestResult QQuserToHvn(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.qqUserToHvn(),json);
	}
	
	/*注册微信三方账号到汉王云*/
	public static RequestResult WXuserToHvn(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.wxUserToHvn(),json);
	}
	
	/*汉王用户通过用户名获取用户基本信息*/
	public static RequestResult getUserInfo(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.getUserInfo(),json);
	}
	
	/*汉王用户通过手机注册获取验证码*/
	public static RequestResult getphoneauthcode(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.getphoneauthcode(),json);
	}
	/*汉王用户通过邮箱找回密码*/
	public static RequestResult getEmailToUser(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.getEmailToUser(),json);
	}
	
	/*汉王用户通过邮箱注册获取激活邮件*/
	public static RequestResult getActivityEmail(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.getActivityEmai(),json);
	}
	
	/*汉王用户通过手机找回密码获取验证码*/
	public static RequestResult getauthcodeForRmbPasswd(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.getcodeForPasswd(),json);
	}
	
	/*汉王用户通过手机注册校验验证码*/
	public static RequestResult checkphoneauthcode(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.checkphoneauthcode(),json);
	}
	
	/*修改昵称*/
	public static RequestResult modifyNickname(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.modifyNickname(),json);
	}
	
	/*修改昵称*/
	public static RequestResult modifyPassword(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.modifyPassword(),json);
	}
	
	/*文件上传*/
	public static RequestResult uploadFile(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.uploadFile(),json);
	}
	
	/*三方绑定*/
	public static RequestResult thirdBind(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.thirdBind(),json);
	}
	
	/*三方解绑定*/
	public static RequestResult thirdUnBind(JSONObject json) {
		return simpleRequest(HTTPTYPE.POST, UrlBankUtil.thirdUnBind(),json);
	}
}
