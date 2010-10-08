package com.vegalabs.amail.server.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;
import com.google.gson.annotations.Expose;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class EmailFailedEvent {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Expose
	Long id;
	@Persistent
	@Expose
	Date created;
	@Persistent
	@Expose
	Date lastUpdated;
	@Persistent
	@Expose
	String source;
	@Persistent
	@Expose
	String status;
	Integer retryCount;
	@Persistent
	@Expose
	Long personId;
	@Persistent
	@Expose
	Long emailEventId;
	@Persistent
	@Expose
	String exceptionMsg;
	@Persistent
	@Expose
	Text exceptionStackTrace;
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}
	public EmailFailedEvent(String source, Long personId,
			Long emailEventId, String exceptionMsg, Text exceptionStackTrace) {
		this.source = source;
		this.personId = personId;
		this.emailEventId = emailEventId;
		this.exceptionMsg = exceptionMsg;
		this.exceptionStackTrace = exceptionStackTrace;
		created = new Date();
		lastUpdated = created;
		status = "FAILED";
		retryCount = 0;
	}
	public Long getPersonId() {
		return personId;
	}
	public void setPersonId(Long personId) {
		this.personId = personId;
	}
	public Long getEmailEventId() {
		return emailEventId;
	}
	public void setEmailEventId(Long emailEventId) {
		this.emailEventId = emailEventId;
	}
	public String getExceptionMsg() {
		return exceptionMsg;
	}
	public void setExceptionMsg(String exceptionMsg) {
		this.exceptionMsg = exceptionMsg;
	}
	public Text getExceptionStackTrace() {
		return exceptionStackTrace;
	}
	public void setExceptionStackTrace(Text exceptionStackTrace) {
		this.exceptionStackTrace = exceptionStackTrace;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EmailFailedEvent [id=");
		builder.append(id);
		builder.append(", created=");
		builder.append(created);
		builder.append(", to=");
		builder.append(source);
		builder.append(", status=");
		builder.append(status);
		builder.append(", retryCount=");
		builder.append(retryCount);
		builder.append(", personId=");
		builder.append(personId);
		builder.append(", EmailEventId=");
		builder.append(emailEventId);
		builder.append(", exceptionMsg=");
		builder.append(exceptionMsg);
		builder.append(", exceptionStackTrace=");
		builder.append(exceptionStackTrace);
		builder.append("]");
		return builder.toString();
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
}
