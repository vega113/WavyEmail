package com.vegalabs.amail.server.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.gson.annotations.Expose;
import com.vegalabs.amail.server.data.FullWaveAddress;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class EmailThread {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Expose
	Long id;
	public EmailThread(Integer threadSubjectHash, String wavemail, String waveId,
			String domain, String blipId) {
		this.threadSubjectHash = threadSubjectHash;
		this.wavemail = wavemail;
		this.waveId = waveId;
		this.domain = domain;
		this.blipId = blipId;
		blipsCount = 0;
	}
	public EmailThread(Integer threadSubjectHash, String wavemail, FullWaveAddress fullWaveAddress) {
		this.threadSubjectHash = threadSubjectHash;
		this.wavemail = wavemail;
		this.waveId = fullWaveAddress.getId();
		this.domain = fullWaveAddress.getDomain();
		this.blipId = fullWaveAddress.getBlipId();
		this.updated = new Date();
		this.created = new Date();
		blipsCount = 0;
	}
	
	@Persistent
	@Expose
	Integer threadSubjectHash;
	@Persistent
	@Expose
	String wavemail;
	@Persistent
	@Expose
	String waveId;
	@Persistent
	@Expose
	String domain;
	@Persistent
	@Expose
	String blipId;
	@Persistent
	@Expose
	Integer blipsCount;
	@Persistent
	@Expose
	Date created;
	@Persistent
	@Expose
	Date updated;
	
	public Integer getThreadSubjectHash() {
		return threadSubjectHash;
	}
	public void setThreadSubjectHash(Integer threadSubjectHash) {
		this.threadSubjectHash = threadSubjectHash;
	}
	
	public String getWaveId() {
		return waveId;
	}
	public void setWaveId(String waveId) {
		this.waveId = waveId;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getBlipId() {
		return blipId;
	}
	public void setBlipId(String blipId) {
		this.blipId = blipId;
	}
	public Integer getBlipsCount() {
		return blipsCount;
	}
	public void setBlipsCount(Integer blipsCount) {
		this.blipsCount = blipsCount;
		this.updated = new Date();
	}
	public String getWavemail() {
		return wavemail;
	}
	public void setWavemail(String wavemail) {
		this.wavemail = wavemail;
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
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EmailThread [threadSubjectHash=");
		builder.append(threadSubjectHash);
		builder.append(", wavemail=");
		builder.append(wavemail);
		builder.append(", waveId=");
		builder.append(waveId);
		builder.append(", domain=");
		builder.append(domain);
		builder.append(", blipId=");
		builder.append(blipId);
		builder.append(", blipsCount=");
		builder.append(blipsCount);
		builder.append(", created=");
		builder.append(created);
		builder.append(", updated=");
		builder.append(updated);
		builder.append("]");
		return builder.toString();
	}
	
	
	
}
