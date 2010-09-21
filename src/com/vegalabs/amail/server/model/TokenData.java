package com.vegalabs.amail.server.model;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gson.annotations.Expose;
@PersistenceCapable
public class TokenData {


	@Persistent
	@PrimaryKey
	private String user;
	@Expose
	@Persistent
	private String token;
	@Expose
	@Persistent
	private Date created;
	@Expose
	@Persistent
	private Date updated;
	
	public TokenData(String user, String token, Date created, Date updated) {
		super();
		this.user = user;
		this.token = token;
		this.created = created;
		this.updated = updated;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getUpdated() {
		return updated;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
}
