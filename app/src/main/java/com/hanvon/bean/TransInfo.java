package com.hanvon.bean;

import java.io.Serializable;

/**
 * include words and sentence
 * @author Hu
 *
 */
public class TransInfo implements Serializable
{
	
	private static final long serialVersionUID = 7970242145899844874L;
	
	private String uuid;
	private String type; //word or sentence
	private String userId;
	private String word; //word or sentence
	private String trans;
	private String date;
	//整句没有以下两个字段
	private String count; //次数
	private String isMaster; // 是否掌握  0:掌握	1:没掌握
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getTrans() {
		return trans;
	}
	public void setTrans(String trans) {
		this.trans = trans;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getIsMaster() {
		return isMaster;
	}
	public void setIsMaster(String isMaster) {
		this.isMaster = isMaster;
	}
	
}