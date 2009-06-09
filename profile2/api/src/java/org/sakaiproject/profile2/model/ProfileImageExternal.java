package org.sakaiproject.profile2.model;

import java.io.Serializable;


/**
 * Hibernate model
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileImageExternal implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String userUuid;
	private String mainUrl;
	private String thumbnailUrl; 

	
	/** 
	 * Empty constructor
	 */
	public ProfileImageExternal(){
	}
	
	/**
	 * Full constructor
	 */
	public ProfileImageExternal(String userUuid, String mainUrl, String thumbnailUrl){
		this.userUuid=userUuid;
		this.mainUrl=mainUrl;
		this.thumbnailUrl=thumbnailUrl;
	}
	
	/**
	 * Minimal constructor for when only one URL
	 */
	public ProfileImageExternal(String userUuid, String mainUrl){
		this.userUuid=userUuid;
		this.mainUrl=mainUrl;
	}
	
	

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public String getMainUrl() {
		return mainUrl;
	}

	public void setMainUrl(String mainUrl) {
		this.mainUrl = mainUrl;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	
}
