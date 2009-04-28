package uk.ac.lancs.e_science.profile2.api;


public class ProfilePreferencesManager {

	//types of messages
	public static final int EMAIL_NOTIFICATION_REQUEST = 1;
	public static final int EMAIL_NOTIFICATION_CONFIRM = 2;

	//these values are used when creating a default privacy record for a user
	public static final boolean DEFAULT_EMAIL_REQUEST_SETTING = true;
	public static final boolean DEFAULT_EMAIL_CONFIRM_SETTING = true;
	public static final boolean DEFAULT_TWITTER_SETTING = false;

	//if no record, this is the default for sending email messages
	public static final boolean DEFAULT_EMAIL_NOTIFICATION_SETTING = true;

	public static final String PROP_EMAIL_REQUEST_ENABLED="emailRequestEnabled";
	public static final String PROP_EMAIL_CONFIRM_ENABLED="emailConfirmEnabled";
	public static final String PROP_TWITTER_ENABLED="twitterEnabled";
	
}
