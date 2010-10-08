package com.vegalabs.amail.server.data;

public class FullWaveAddress {
	public FullWaveAddress(String domain, String id, String blipId) {
		this.domain = domain;
		this.id = id;
		this.blipId = blipId;
	}
	public FullWaveAddress(String fullWaveId) {
		String[] split = fullWaveId.split("/");
		this.domain = split[2];
		this.id = split[3];
		this.blipId = split[6];
	}
	
	String domain;
	String id;
	String blipId;
	@Override
	public String toString() {
		return String.format("waveid://%s/%s/~/conv+root/%s",domain,id,blipId);
	}
	public String toFullWaveId() {
		return String.format("%s#%s#%s",domain,id,blipId);
	}
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBlipId() {
		return blipId;
	}
	public void setBlipId(String blipId) {
		this.blipId = blipId;
	}
	
	public String getFullWaveId(){
		return domain + "!" + id;
	}
}
