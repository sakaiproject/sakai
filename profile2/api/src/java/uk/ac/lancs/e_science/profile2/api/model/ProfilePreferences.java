package uk.ac.lancs.e_science.profile2.api.model;

import java.io.Serializable;


/**
 * Hibernate model
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfilePreferences implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String userUuid;
	private boolean requestEmailEnabled;
	private boolean confirmEmailEnabled;
	private boolean twitterEnabled;
	private String twitterUsername;
	private String twitterPasswordEncrypted; //this is persisted
	private String twitterPasswordDecrypted; //this is used for display

	
	/** 
	 * Empty constructor
	 */
	public ProfilePreferences(){
	}
	
	/**
	 * Basic constructor for creating default records
	 */
	public ProfilePreferences(String userUuid, boolean requestEmailEnabled, boolean confirmEmailEnabled, boolean twitterEnabled){
		this.userUuid=userUuid;
		this.requestEmailEnabled=requestEmailEnabled;
		this.confirmEmailEnabled=confirmEmailEnabled;
		this.twitterEnabled=twitterEnabled;
	}
	
	
	public String getUserUuid() {
		return userUuid;
	}


	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public void setRequestEmailEnabled(boolean requestEmailEnabled) {
		this.requestEmailEnabled = requestEmailEnabled;
	}

	public boolean isRequestEmailEnabled() {
		return requestEmailEnabled;
	}

	public void setConfirmEmailEnabled(boolean confirmEmailEnabled) {
		this.confirmEmailEnabled = confirmEmailEnabled;
	}

	public boolean isConfirmEmailEnabled() {
		return confirmEmailEnabled;
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


	public String getTwitterPasswordEncrypted() {
		return twitterPasswordEncrypted;
	}

	public void setTwitterPasswordEncrypted(String twitterPasswordEncrypted) {
		this.twitterPasswordEncrypted = twitterPasswordEncrypted;
	}

	public String getTwitterPasswordDecrypted() {
		return twitterPasswordDecrypted;
	}

	public void setTwitterPasswordDecrypted(String twitterPasswordDecrypted) {
		this.twitterPasswordDecrypted = twitterPasswordDecrypted;
	}

	
	
	/**
	 * for the form feedback, to get around a weird thing in Wicket where it needs a backing model for the FeedbackLabel component 
	 * since this model is used directly by the Preferences page
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
