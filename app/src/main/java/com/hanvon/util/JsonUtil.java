package com.hanvon.util;

import com.hanvon.bean.FileInfo;
import com.hanvon.bean.Label;
import com.hanvon.bean.TransInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
	/**
	 * jsonArray TO list
	 * @param jsonArray
	 * @return
	 * @throws JSONException
	 */
	public static List<FileInfo> FileJsonParse(JSONArray jsonArray) throws JSONException
	{
		List<FileInfo> files = new ArrayList<FileInfo>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject item = jsonArray.getJSONObject(i); //得到每个对象
			String fuuid = item.getString("fuid");	//获取对象对应的值
			String title = item.getString("title");
			String summary = item.getString("summary");
			String length = item.getString("length");
			String createTime = item.getString("createtime");
			String modifyTime = item.getString("modifytime");
			String accessTime = item.getString("accesstime");
			String serVer = item.getString("serVer");
			String content = item.getString("content");
			
			FileInfo fileInfo = new FileInfo();
			fileInfo.setFuuid(fuuid);
			fileInfo.setTitle(title);
			fileInfo.setSummary(summary);
			fileInfo.setLength(length);
			fileInfo.setCreateTime(createTime);
			fileInfo.setModifyTime(modifyTime);
			fileInfo.setAccessTime(accessTime);
			fileInfo.setSerVer(serVer);
			fileInfo.setContent(content);
			
			files.add(fileInfo);
		}
		return files;
	}
	
	public static List<TransInfo> WordJsonParse(JSONArray jsonArray) throws JSONException
	{
		List<TransInfo> words = new ArrayList<TransInfo>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject item = jsonArray.getJSONObject(i); //得到每个对象
			String uuid = item.getString("fuid");
			String word = item.getString("word");
			String time = item.getString("time");
			String trans = item.getString("trans");
			String scan1st = item.getString("scan1st");
			String grasp = item.getString("grasp");
			
			TransInfo wordInfo = new TransInfo();
			wordInfo.setUuid(uuid);
			wordInfo.setWord(word);
			wordInfo.setCount(time);
			wordInfo.setTrans(trans);
			wordInfo.setDate(scan1st);
			wordInfo.setIsMaster(grasp);
			
			words.add(wordInfo);
		}
		return words;
	}
	
	public static List<TransInfo> SentenceJsonParse(JSONArray jsonArray) throws JSONException
	{
		List<TransInfo> sens = new ArrayList<TransInfo>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject item = jsonArray.getJSONObject(i); //得到每个对象
			String uuid = item.getString("fuid");
			String sentence = item.getString("sentence");
			String trans = item.getString("trans");
//			String date = item.getString("scan1st");
			
			TransInfo sen = new TransInfo();
			sen.setUuid(uuid);
			sen.setWord(sentence);
			sen.setTrans(trans);
//			sen.setDate(date);
			
			sens.add(sen);
		}
		return sens;
	}
	
	public static List<Label> LableJsonParse(JSONArray jsonArray) throws JSONException
	{
		List<Label> lables = new ArrayList<Label>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject item = jsonArray.getJSONObject(i);
//			String uuid = item.getString("fuid");
			String label = item.getString("label");
			String position = item.getString("pos");
			String recognition = item.getString("recognition");
			
			Label lab = new Label();
			lab.setLabel(label);
			lab.setPosition(position);
			lab.setRecognition(recognition);
			
			lables.add(lab);
		}
		return lables;
	}
}
