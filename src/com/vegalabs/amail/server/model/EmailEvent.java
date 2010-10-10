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

import com.google.appengine.api.datastore.Text;
import com.google.gson.annotations.Expose;
import com.google.wave.api.Attachment;
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class EmailEvent {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Expose
	Long id;
	
	@Persistent
	@Expose
	Date created;
	@Persistent
	@Expose
	String activityType;
	
	@Persistent
	@Expose
	String subject;
	@Persistent
	@Expose
	Text msgBody;
	@Persistent
	@Expose
	List<String> from;
	@Persistent
	@Expose
	List<String> to;
	@Persistent
	@Expose
	List<String> cc;
	@Persistent
	@Expose
	List<String> bcc;
	@Persistent
	@Expose
	String source;
	
	@Persistent
	@Expose
	Integer subjectHash;
	@Persistent
	@Expose
	Integer msgBodyHash;
	@Persistent
	@Expose
	Date sentDate;
	
	@Persistent(serialized = "true", defaultFetchGroup = "true")
	@Expose
	private Map<String,String> fullWaveIdPerUserMap;
	
	@Persistent(serialized = "true", defaultFetchGroup = "true")
	@Expose
	List<Attachment> attachments;
	
	
	public EmailEvent(String activityType, String subject, Text msgBody, List<String> from,List<String> to, List<String> cc,String source, Date sentDate, List<Attachment> attachments){
		this.activityType = activityType;
		this.subject = subject;
		this.msgBody = new Text(msgBody.getValue());
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.source = source;
		this.subjectHash = subject.hashCode();
		this.msgBodyHash = msgBody.getValue().hashCode();
		this.created = new Date(System.currentTimeMillis());
		this.attachments = attachments;
		this.sentDate = sentDate;
	}

	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public String getActivityType() {
		return activityType;
	}
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Text getMsgBody() {
		return msgBody;
	}
	public void setMsgBody(Text msgBody) {
		this.msgBody = msgBody;
	}
	public List<Attachment> getAttachments() {
		return attachments != null ? attachments : new ArrayList<Attachment>();
	}
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
	public List<String> getFrom() {
		return from != null ? from : new ArrayList<String>();
	}
	public void setFrom(List<String> from) {
		this.from = from;
	}
	public List<String> getTo() {
		return to  != null ? to : new ArrayList<String>();
	}
	public void setTo(List<String> to) {
		this.to = to;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	public Integer getMsgBodyHash() {
		return msgBodyHash;
	}
	public void setMsgBodyHash(Integer msgBodyHash) {
		this.msgBodyHash = msgBodyHash;
	}

	public Integer getSubjectHash() {
		return subjectHash;
	}

	public void setSubjectHash(Integer subjectHash) {
		this.subjectHash = subjectHash;
	}

	public Map<String, String> getFullWaveIdPerUserMap() {
		if(fullWaveIdPerUserMap == null){
			fullWaveIdPerUserMap = new HashMap<String, String>();
		}
		return fullWaveIdPerUserMap; 
	}

	public void setFullWaveIdPerUserMap(Map<String, String> fullWaveIdPerUserMap) {
		this.fullWaveIdPerUserMap = fullWaveIdPerUserMap;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public Long getId() {
		return id;
	}

	public List<String> getCc() {
		return cc;
	}

	public void setCc(List<String> cc) {
		this.cc = cc;
	}
}
