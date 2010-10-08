package com.vegalabs.amail.server.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gson.annotations.Expose;
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Person implements Serializable {
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
		if(properties == null){
			properties = new HashMap<String,String>();
		}
		return properties;
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
		if(contactsName == null){
			contactsName = new HashMap<String,String>();
		}
		return contactsName;
	}
	public void setContactsName(Map<String, String> contactsName) {
		this.contactsName = contactsName;
	}
	
	public Map<String, String> getContacts() {
		if(contacts == null){
			contacts = new HashMap<String,String>();
		}
		return contacts;
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

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("Person [created=");
		builder.append(created);
		builder.append(", email=");
		builder.append(email);
		builder.append(", wavemail=");
		builder.append(wavemail);
		builder.append(", waveAddress=");
		builder.append(waveAddress);
		builder.append(", name=");
		builder.append(name);
		builder.append(", gmailPass=");
		builder.append(gmailPass);
		builder.append(", gmailToken=");
		builder.append(gmailToken);
		builder.append(", contactsToken=");
		builder.append(contactsToken);
		builder.append(", iconUrl=");
		builder.append(iconUrl);
		builder.append(", properties=");
		builder.append(properties != null ? toString(properties.entrySet(),
				maxLen) : null);
		builder.append(", updated=");
		builder.append(updated);
		builder.append(", isActive=");
		builder.append(isActive);
		builder.append(", contacts=");
		builder.append(contacts != null ? toString(contacts.entrySet(), maxLen)
				: null);
		builder.append(", contactsName=");
		builder.append(contactsName != null ? toString(contactsName.entrySet(),
				maxLen) : null);
		builder.append("]");
		return builder.toString();
	}

	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext()
				&& i < maxLen; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}

	public Long getId() {
		return id;
	}
	
}
