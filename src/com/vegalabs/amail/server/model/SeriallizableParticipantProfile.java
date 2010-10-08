package com.vegalabs.amail.server.model;

import java.io.Serializable;
import java.util.Map;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.wave.api.ParticipantProfile;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@SuppressWarnings("serial")
public class SeriallizableParticipantProfile  implements Serializable {
	/**
	 * 
	 */
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Expose
	Long id;
	
	@Persistent
	@Expose
	private String imageUrl;
	@Persistent
	@Expose
	private String name;
	@Persistent
	@Expose
	private String profileUrl;
	
	@Persistent
	@Expose
	private String email;

	public SeriallizableParticipantProfile (String imageUrl,String email, String name,  String profileUrl){
		this.imageUrl  = imageUrl;
		this.name = name;
		this.profileUrl = profileUrl;
		this.email = email;
	}
	public ParticipantProfile getProfile(){
		if(name == null || "".equals(name)){
			name = email;
		}
		if(profileUrl == null){
			profileUrl = "";
		}
		return new ParticipantProfile(name,imageUrl,profileUrl);
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getProfileUrl() {
		return profileUrl;
	}
	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SeriallizableParticipantProfile [imageUrl=");
		builder.append(imageUrl);
		builder.append(", name=");
		builder.append(name);
		builder.append(", profileUrl=");
		builder.append(profileUrl);
		builder.append(", email=");
		builder.append(email);
		builder.append("]");
		return builder.toString();
	}
	public void updateWith(Map<String, String> gravatarProfile) {
		 String imageUrl = gravatarProfile.get("imageUrl");
		  if((getImageUrl() == null || "".equals(getImageUrl())) && imageUrl != null){
			  setImageUrl(imageUrl);
		  }
		  String profileName = gravatarProfile.get("name");
		  if(profileName != null && (getName() == null || "".equals(getName()))){
			  setName(profileName);
		  }
		  String profileUrl = gravatarProfile.get("profileUrl");
		  if(profileUrl != null && ( getProfileUrl() == null || "".equals(getProfileUrl()))){
			  setProfileUrl(profileUrl);
		  }
	}
};
