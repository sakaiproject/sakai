package uk.ac.lancs.e_science.profile2.hbm;

import java.io.Serializable;



public class ProfilePreferences implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String userUuid;
	private int email;
	private boolean twitterEnabled;
	private String twitterUsername;
	private String twitterPassword;

	/** 
	 * Empty constructor
	 */
	public ProfilePreferences(){
	}
	
	/**
	 * Basic constructor for creating default records
	 */
	public ProfilePreferences(String userUuid, int email, boolean twitterEnabled){
		this.userUuid=userUuid;
		this.email=email;
		this.twitterEnabled=twitterEnabled;
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

	public boolean isTwitterEnabled() {
		return twitterEnabled;
	}

	public void setTwitterEnabled(boolean twitterEnabled) {
		this.twitterEnabled = twitterEnabled;
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


	
	
	
	/**
	 * for the form feedback, to get around a weird thing in Wicket where it needs a backing model for the FeedbackLabel component 
	 * since this is used directly by the Preferences page
	 */
	private String twitterUsernameFeedback;
	private String twitterPasswordFeedback;

	
	public String getTwitterUsernameFeedback() {
		return twitterUsernameFeedback;
	}

	public void setTwitterUsernameFeedback(String twitterUsernameFeedback) {
		this.twitterUsernameFeedback = twitterUsernameFeedback;
	}
	public String getTwitterPasswordFeedback() {
		return twitterPasswordFeedback;
	}

	public void setTwitterPasswordFeedback(String twitterPasswordFeedback) {
		this.twitterPasswordFeedback = twitterPasswordFeedback;
	}


	
}
