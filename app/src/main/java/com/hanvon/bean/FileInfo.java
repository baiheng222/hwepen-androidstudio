package com.hanvon.bean;

import java.io.Serializable;

public class FileInfo implements Comparable<FileInfo>, Serializable
{

	private static final long serialVersionUID = -3184534975667213224L;

	public static enum FileType{
		EXCERPT("摘抄","0"),
		RECORDING("录音","1"),
		WORDS("单词本","2"),
		SENTENCE("整句翻译","3"),
		NOTE("笔记","4"),
		TRACE("轨迹","5"),
		IMAGE("图像","6");
		private String name;
		private String value;
		private FileType(String name, String value){
			this.name=name;
			this.value=value;
		}
		public String getName() {
			return name;
		}
		public String getValue() {
			return value;
		}
	    public String toString(){
	    	return getName();
	    }	
	}
	private String fuuid;
	private String userId;
	private String title;
	private String summary; //概要
	private String length;
	private String createTime;
	private String modifyTime;
	private String accessTime;
	private String content;   //音频文件用以保存备注信息
	private String type;
	private String serVer;
	
	private String syn; //是否同步（上传到HDFS）  0:yes  1:no
	private String path; //SDCard上的路径
	
	public String getFuuid() {
		return fuuid;
	}
	public void setFuuid(String fuuid) {
		this.fuuid = fuuid;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getLength() {
		return length;
	}
	public void setLength(String length) {
		this.length = length;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}
	public String getAccessTime() {
		return accessTime;
	}
	public void setAccessTime(String accessTime) {
		this.accessTime = accessTime;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSyn() {
		return syn;
	}
	public void setSyn(String syn) {
		this.syn = syn;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getSerVer() {
		return serVer;
	}
	public void setSerVer(String serVer) {
		this.serVer = serVer;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public int compareTo(FileInfo another) {
		return another.getCreateTime().compareTo(this.getCreateTime());
	}
}
