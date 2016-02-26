package com.hanvon.bean;

import java.io.Serializable;

/**
 * @author Hu
 *
 */
public class Label implements Serializable
{
	
	private static final long serialVersionUID = 7707827854721699571L;
	
	private String id;
	private String label;
	private String position;
	private String recognition;
	private String fileId;
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getRecognition() {
		return recognition;
	}
	public void setRecognition(String recognition) {
		this.recognition = recognition;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
}