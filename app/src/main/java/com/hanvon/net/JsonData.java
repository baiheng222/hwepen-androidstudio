package com.hanvon.net;

import android.text.TextUtils;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @desc JSON 数据操作类
 * @author  PengWenCai
 * @time 2015-6-25 上午11:16:10
 * @version
 */
public class JsonData
{

	private HashMap<String, Object> mMap = new HashMap<String, Object>();
	private ArrayList<JsonData> mList = new ArrayList<JsonData>();
	private boolean mValid = false;
	private JSONObject mJson;
	private JSONArray mJsonArray;

	/**
	 * 构造函数
	 * 
	 * @param data
	 *            JSON字符串
	 */
	public JsonData(String data) {

		if (TextUtils.isEmpty(data)) {
			return;
		}

		try {
			JSONObject jsonObj = new JSONObject(data);
			construct(jsonObj);
			mValid = true;
		} catch (Exception e) {
		}
	}

	/**
	 * 构造函数
	 * 
	 * @param array
	 *            JSON数组
	 */
	private JsonData(JSONArray array) {

		mJsonArray = array;
		try {
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				JsonData node = new JsonData(obj);
				mList.add(node);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 构造函数
	 * 
	 * @param jsonObj
	 *            JSON对象
	 */
	private JsonData(JSONObject jsonObj) {
		construct(jsonObj);
	}

	/**
	 * 根据JSON对象初始化数据
	 * 
	 * @param jsonObj
	 *            JSON对象
	 */
	private void construct(JSONObject jsonObj) {
		mJson = jsonObj;

		try {
			@SuppressWarnings("rawtypes")
			Iterator it = jsonObj.keys();
			while (it.hasNext()) {
				String key = it.next().toString();
				Object obj = jsonObj.opt(key);
				@SuppressWarnings("rawtypes")
				Class classType = obj.getClass();
				if (classType == String.class) {
					String value = jsonObj.getString(key);
					mMap.put(key, value);
				} else if (classType == Integer.class) {
					int value = jsonObj.getInt(key);
					mMap.put(key, value);
				} else if (classType == JSONObject.class) {
					JsonData child = new JsonData((JSONObject) obj);
					mMap.put(key, child);
				} else if (classType == JSONArray.class) {
					JSONArray array = jsonObj.getJSONArray(key);
					JsonData node = new JsonData(array);
					mMap.put(key, node);
				}
			}
		} catch (Exception e) {
			//LogUtil.getInstance().e(e.toString());
		}
	}

	/**
	 * 是否对象数据有效
	 * 
	 * @return 数据是否有效
	 */
	public boolean isValid() {
		return mValid;
	}

	/**
	 * 取得包含数据
	 * 
	 * @return 数据MAP
	 */
	public Map<String, Object> getMap() {
		return mMap;
	}

	public String getMapKey(int id) {
		int i = 0;
		for (String key : mMap.keySet()) {
			if (i == id) {
				return key;
			}
			i++;
		}
		return null;
	}

	public JsonData getChild(String name) {
		Object obj = mMap.get(name);
		if (obj != null && obj.getClass() == JsonData.class) {
			return (JsonData) obj;
		}
		return null;
	}

	public Map<String, Object> getRawValues() {
		HashMap<String, Object> map = new HashMap<String, Object>();

		for (String key : mMap.keySet()) {
			Object obj = mMap.get(key);
			if (obj.getClass() == int.class || obj.getClass() == String.class) {
				map.put(key, obj);
			}
		}

		return map;
	}

	public Map<String, String> getValues() {
		HashMap<String, String> map = new HashMap<String, String>();

		for (String key : mMap.keySet()) {
			Object obj = mMap.get(key);
			if (obj.getClass() == Integer.class
					|| obj.getClass() == String.class) {
				map.put(key, obj.toString());
			}
		}

		return map;
	}

	public Map<String, String> getValues(String keys[]) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < keys.length; i++) {
			Object obj = mMap.get(keys[i]);
			if (obj != null) {
				if (obj.getClass() == Integer.class
						|| obj.getClass() == String.class) {
					map.put(keys[i], obj.toString());
				}
			}
		}
		return map;
	}

	public Map<String, String> getValues(String node, String keys[]) {
		Map<String, String> map = null;
		JsonData data = getChild(node);
		if (data != null) {
			map = data.getValues(keys);
		}
		return map;
	}

	public ArrayList<JsonData> getList() {
		return mList;
	}

	public String getJson() {
		String str = "";
		if (mJsonArray != null) {
			str = mJsonArray.toString();
		} else if (mJson != null) {
			str = mJson.toString();
		}
		return str;
	}
}

