package org.sakaiproject.profile2.types;

/**
 * These are the types of preferences in Profile2.
 * <p>
 * We use these when checking if different options are enabled.
 * <p>
 * See also {@link EmailType}.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * @since 1.5
 *
 */
public enum PreferenceType {

	EMAIL_NOTIFICATION_REQUEST,
	EMAIL_NOTIFICATION_CONFIRM,
	EMAIL_NOTIFICATION_MESSAGE_NEW,
	EMAIL_NOTIFICATION_MESSAGE_REPLY,
	EMAIL_NOTIFICATION_WALL_EVENT_NEW,
	EMAIL_NOTIFICATION_WALL_POST_MY_NEW,
	EMAIL_NOTIFICATION_WALL_POST_CONNECTION_NEW,
	EMAIL_NOTIFICATION_WALL_STATUS_NEW,
	EMAIL_NOTIFICATION_WORKSITE_NEW,
	EMAIL_NOTIFICATION_PROFILE_CHANGE,
	OFFICIAL_IMAGE,
	GRAVATAR_IMAGE,
	KUDOS_RATING,
	PICTURES,
	ONLINE_STATUS;
	
}
