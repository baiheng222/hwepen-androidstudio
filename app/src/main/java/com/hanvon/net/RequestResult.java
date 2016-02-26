package com.hanvon.net;

public class RequestResult
{
	public static final int RESULT_OK = 0;
	public static final int RESULT_OK_MSG = 5;
	public static final int RESULT_NET_ERR = -2;
	public static final int RESULT_PARSE_ERR = -3;
	public static final int RESULT_FORMAT_ERR = -4;
	public static final int RESULT_ACTION_FAIL = -5;
	private String mMessage = "";
	private JsonData mJsonData;
	private int mResultCode = RESULT_NET_ERR;

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		this.mMessage = message;
	}

	public int getResultCode() {
		return mResultCode;
	}

	public void setResultCode(int mResultCode) {
		this.mResultCode = mResultCode;
	}

	public void setData(JsonData data) {
		mJsonData = data;
	}

	public JsonData getData() {
		return mJsonData;
	}

	public boolean isDataValid() {
		return (mJsonData != null && mJsonData.isValid());
	}
}

