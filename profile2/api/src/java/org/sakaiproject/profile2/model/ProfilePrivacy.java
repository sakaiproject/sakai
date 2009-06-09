package org.sakaiproject.profile2.api.model;

import java.io.Serializable;

import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;


/**
 * Hibernate and EntityProvider model
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfilePrivacy implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@EntityId
	private String userUuid;
	private int profileImage;
	private int basicInfo;
	private int contactInfo;
	private int academicInfo;
	private int personalInfo;
	private boolean showBirthYear;
	private int search;
	private int myFriends;
	private int myStatus;
	
	/** 
	 * Empty constructor
	 */
	public ProfilePrivacy(){
	}
	
	/**
	 * Constructor to allow creation of object in one go (ie for new entries when a profile is first created)
	 */
	public ProfilePrivacy(String userUuid, int profileImage, int basicInfo, int contactInfo, int academicInfo, int personalInfo, boolean showBirthYear, int search, int myFriends, int myStatus) {
		super();
		this.userUuid = userUuid;
		this.profileImage = profileImage;
		this.basicInfo = basicInfo;
		this.contactInfo = contactInfo;
		this.academicInfo = academicInfo;
		this.personalInfo = personalInfo;
		this.showBirthYear = showBirthYear;
		this.search = search;
		this.myFriends = myFriends;
		this.myStatus = myStatus;
	}

	public String getUserUuid() {
		return userUuid;
	}


	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}


	public int getProfileImage() {
		return profileImage;
	}


	public void setProfileImage(int profileImage) {
		this.profileImage = profileImage;
	}


	public int getBasicInfo() {
		return basicInfo;
	}


	public void setBasicInfo(int basicInfo) {
		this.basicInfo = basicInfo;
	}


	public int getContactInfo() {
		return contactInfo;
	}


	public void setContactInfo(int contactInfo) {
		this.contactInfo = contactInfo;
	}


	public void setAcademicInfo(int academicInfo) {
		this.academicInfo = academicInfo;
	}

	public int getAcademicInfo() {
		return academicInfo;
	}

	public int getPersonalInfo() {
		return personalInfo;
	}


	public void setPersonalInfo(int personalInfo) {
		this.personalInfo = personalInfo;
	}


	public void setShowBirthYear(boolean showBirthYear) {
		this.showBirthYear = showBirthYear;
	}

	public boolean isShowBirthYear() {
		return showBirthYear;
	}

	public int getSearch() {
		return search;
	}

	public void setSearch(int search) {
		this.search = search;
	}

	public void setMyFriends(int myFriends) {
		this.myFriends = myFriends;
	}

	public int getMyFriends() {
		return myFriends;
	}

	public void setMyStatus(int myStatus) {
		this.myStatus = myStatus;
	}

	public int getMyStatus() {
		return myStatus;
	}


	
}
