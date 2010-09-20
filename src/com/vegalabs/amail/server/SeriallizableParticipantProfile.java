package com.vegalabs.amail.server;

import java.io.Serializable;

import com.google.wave.api.ParticipantProfile;

@SuppressWarnings("serial")
public class SeriallizableParticipantProfile  implements Serializable {
	  /**
	   * 
	   */
	  private String imageUrl;
	  private String name;
	  private String profileUrl;

	  public SeriallizableParticipantProfile (String imageUrl,String name,  String profileUrl){
		  this.imageUrl  = imageUrl;
		  this.name = name;
		  this.profileUrl = profileUrl;
	  }
	  public ParticipantProfile getProfile(){
		  return new ParticipantProfile(name,imageUrl,profileUrl);
	  }
};
