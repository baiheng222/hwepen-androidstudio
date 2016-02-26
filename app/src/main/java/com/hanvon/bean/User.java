package com.hanvon.bean;

import java.io.Serializable;

public class User implements Serializable
{
	
	private static final long serialVersionUID = -4899977488723704788L;
	
	private Integer id;
	private String userId;
	private String password;
	private String token;
	private String status; // 0:正常  1:异常
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
