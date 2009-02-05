package uk.ac.lancs.e_science.profile2.hbm;

import java.io.Serializable;



public class ProfilePreferences implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String userUuid;
	private int email;
	private String twitterUsername;
	private String twitterPassword;

	/** 
	 * Empty constructor
	 */
	public ProfilePreferences(){
	}
	
	
	public String getUserUuid() {
		return userUuid;
	}


	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public int getEmail() {
		return email;
	}


	public void setEmail(int email) {
		this.email = email;
	}


	public String getTwitterUsername() {
		return twitterUsername;
	}


	public void setTwitterUsername(String twitterUsername) {
		this.twitterUsername = twitterUsername;
	}


	public String getTwitterPassword() {
		return twitterPassword;
	}


	public void setTwitterPassword(String twitterPassword) {
		this.twitterPassword = twitterPassword;
	}


	

	

	
}
