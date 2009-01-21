package uk.ac.lancs.e_science.profile2.hbm;

import java.io.Serializable;

/*
 * SearchResult.java
 * 
 * This is a model for storing information returned from a search about a user
 * Because people can limit searches on their profile, we need to return an object like this in order
 * to minimise the number of database queries.
 * 
 * When we get the list of users, we then need to see if the user that searched for them is a friend,
 * then determine if the setings the person has on their profile limits searches to friends, everyone or only self.
 * If the privileges aren't high enough they need to be removed from the list.
 * 
 * But, when this data is being consumed, we also need the information about their friend status, privacy settings etc,
 * so this wraps that up so its already available.
 * 
 * 
 */


public class SearchResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String userUuid;
	private boolean friend;
	private boolean profileAllowed;
	
	
	public String getUserUuid() {
		return userUuid;
	}
	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}
	public boolean isFriend() {
		return friend;
	}
	public void setFriend(boolean friend) {
		this.friend = friend;
	}
	public boolean isProfileAllowed() {
		return profileAllowed;
	}
	public void setProfileAllowed(boolean profileAllowed) {
		this.profileAllowed = profileAllowed;
	}
	
	public SearchResult(String userUuid, boolean friend, boolean profileAllowed) {
		super();
		this.userUuid = userUuid;
		this.friend = friend;
		this.profileAllowed = profileAllowed;
	}
	
}
