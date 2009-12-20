package org.sakaiproject.profile2.model;

import java.io.Serializable;

import org.sakaiproject.user.api.User;


/** 
 * This the main object that represents a person in Profile2. It is essentially a wrapper object around several other objects.
 * <p>There are standard fields and a User object that will always be set.<br />
 * There are also extended objects that you can get access to like their submitted Profile, Image, Status, Privacy and Preferences.</p>
 *  
 * <p>Depending on how you request this object, these extended objects are not guaranteed to be set so
 * you should check for their existence before using. If not set, and you need it, you can get it for a person
 * from the appropriate method in ProfileLogic. If you need it for a whole list, check your method that creates the List
 * of persons, you might be able to get all of the info you need from a different method.</p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class Person implements Serializable {

	private String uuid;
	private String displayName;
	private String type;
	
	private User user;
	private UserProfile profile;
	private ProfilePrivacy privacy;
	private ProfilePreferences preferences;
	
	/**
	 * No-arg constructor
	 */
	public Person() {
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public UserProfile getProfile() {
		return profile;
	}

	public void setProfile(UserProfile profile) {
		this.profile = profile;
	}

	public ProfilePrivacy getPrivacy() {
		return privacy;
	}

	public void setPrivacy(ProfilePrivacy privacy) {
		this.privacy = privacy;
	}

	public ProfilePreferences getPreferences() {
		return preferences;
	}

	public void setPreferences(ProfilePreferences preferences) {
		this.preferences = preferences;
	}
	
	
	
}
