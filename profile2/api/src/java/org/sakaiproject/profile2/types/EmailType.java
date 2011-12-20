package org.sakaiproject.profile2.types;

/**
 * The types of emails that can be sent. See also {@link PreferenceType}.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * @since 1.5
 *
 */
public enum EmailType {
	
	
	//*********************
	//
	// DEVELOPERS TAKE NOTE
	//
	// All of these values have a reference up to PreferenceType so we can perform conversions.
	// If you are adding more values here, these *must* be kept in sync and *must* be named identically.
	//
	// ********************
	
	EMAIL_NOTIFICATION_REQUEST,
	EMAIL_NOTIFICATION_CONFIRM,
	EMAIL_NOTIFICATION_MESSAGE_NEW,
	EMAIL_NOTIFICATION_MESSAGE_REPLY,
	EMAIL_NOTIFICATION_WALL_EVENT_NEW,
	EMAIL_NOTIFICATION_WALL_POST_MY_NEW,
	EMAIL_NOTIFICATION_WALL_POST_CONNECTION_NEW,
	EMAIL_NOTIFICATION_WALL_STATUS_NEW,
	EMAIL_NOTIFICATION_WORKSITE_NEW,
	EMAIL_NOTIFICATION_PROFILE_CHANGE;
	
	/**
	 * Return the PreferenceType that matches this type.
	 * @return
	 */
	public PreferenceType toPreference() { 
		return PreferenceType.valueOf(this.name());
	}
}
