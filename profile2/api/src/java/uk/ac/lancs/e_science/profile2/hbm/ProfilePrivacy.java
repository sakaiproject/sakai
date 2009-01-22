package uk.ac.lancs.e_science.profile2.hbm;

import java.io.Serializable;



public class ProfilePrivacy implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String userUuid;
	private int profile;
	private int basicInfo;
	private int contactInfo;
	private int personalInfo;
	private boolean showBirthYear;
	private int search;
	private int myFriends;
	

	public ProfilePrivacy(){
	}
	
	/*
	 * Constructor to allow creation of object in one go (ie for new entries when a profile is first created)
	 */
	public ProfilePrivacy(String userUuid, int profile, int basicInfo, int contactInfo, int personalInfo, boolean showBirthYear, int search, int myFriends) {
		super();
		this.userUuid = userUuid;
		this.profile = profile;
		this.basicInfo = basicInfo;
		this.contactInfo = contactInfo;
		this.personalInfo = personalInfo;
		this.showBirthYear = showBirthYear;
		this.search = search;
		this.setMyFriends(myFriends);
	}

	public String getUserUuid() {
		return userUuid;
	}


	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}


	public int getProfile() {
		return profile;
	}


	public void setProfile(int profile) {
		this.profile = profile;
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


	
}
