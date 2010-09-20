package com.vegalabs.amail.server.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gson.annotations.Expose;
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Person {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Expose
	Long id;
	
	@Persistent
	@Expose
	Date created;
	@Persistent
	@Expose
	String email;
	@Persistent
	@Expose
	String wavemail;
	@Persistent
	@Expose
	String waveAddress;
	@Persistent
	@Expose
	String name;
	@Persistent
	@Expose
	String gmailPass;
	@Persistent
	@Expose
	String gmailToken;
	@Persistent
	@Expose
	String contactsToken;
	@Persistent
	@Expose
	String iconUrl;
	
	@Persistent(serialized = "true", defaultFetchGroup = "true")
	@Expose
	private Map<String,String> properties;
	
	@Persistent
	@Expose
	Date updated;
	@Persistent
	@Expose
	Boolean isActive;
	@Persistent(serialized = "true", defaultFetchGroup = "true")
	@Expose
	private Map<String,String> contacts;
	
	@Persistent(serialized = "true", defaultFetchGroup = "true")
	@Expose
	private Map<String,String> contactsName;
	
	
	public Person(String wavemail){
		this.wavemail = wavemail;
		created = new Date(System.currentTimeMillis());
		updated = new Date(System.currentTimeMillis());
		isActive = true;
	}
	
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getWavemail() {
		return wavemail;
	}
	public void setWavemail(String wavemail) {
		this.wavemail = wavemail;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGmailPass() {
		return gmailPass;
	}
	public void setGmailPass(String gmailPass) {
		this.gmailPass = gmailPass;
	}
	public String getGmailToken() {
		return gmailToken;
	}
	public void setGmailToken(String gmailToken) {
		this.gmailToken = gmailToken;
	}
	public String getContactsToken() {
		return contactsToken;
	}
	public void setContactsToken(String contactsToken) {
		this.contactsToken = contactsToken;
	}
	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public Map<String, String> getProperties() {
		return properties != null ? properties : new HashMap<String,String>();
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	public Date getUpdated() {
		return updated;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	public Boolean isActive() {
		return isActive;
	}
	public void setActive(Boolean isActive) {
		this.isActive = isActive;
	}
	public Map<String, String> getContactsName() {
		return contactsName != null ? contactsName : new HashMap<String,String>();
	}
	public void setContactsName(Map<String, String> contactsName) {
		this.contactsName = contactsName;
	}
	
	public Map<String, String> getContacts() {
		return contacts != null ? contacts : new HashMap<String,String>();
	}
	public void setContacts(Map<String, String> contacts) {
		this.contacts = contacts;
	}

	public String getWaveAddress() {
		return waveAddress;
	}

	public void setWaveAddress(String waveAddress) {
		this.waveAddress = waveAddress;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	
}
