package org.sakaiproject.profile2.api;

/**
 * Class to hold static constants for Profile2, like defaults etc.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileConstants {

	/*
	 * CONNECTIONS
	 */
	
	//number of friends to show in friends feed
	public static final int MAX_FRIENDS_FEED_ITEMS = 6; 
	
	//relationship type
	public static final int RELATIONSHIP_FRIEND = 1;
	//public static final int RELATIONSHIP_COLLEAGUE = 2;
	
	
	
	/*
	 * IMAGE
	 */
	
	//default if not specified in sakai.properties as profile.picture.max (megs)
	public static final int MAX_PROFILE_IMAGE_UPLOAD_SIZE = 2; 
	
	//one side will be scaled to this if larger. 400 is large enough
	public static final int MAX_IMAGE_XY = 400; 	
	
	//one side will be scaled to this if larger. 
	public static final int MAX_THUMBNAIL_IMAGE_XY = 100; 	
    
	//directories in content hosting that these images live in
	//also used by ProfileImageExternal
	public static final int PROFILE_IMAGE_MAIN = 1;		
	public static final int PROFILE_IMAGE_THUMBNAIL = 2;
	
	//default images for certain things
	public static final String UNAVAILABLE_IMAGE = "images/no_image.gif";
	public static final String UNAVAILABLE_IMAGE_FULL = "/sakai-profile2-tool/images/no_image.gif";
	public static final String CLOSE_IMAGE = "/library/image/silk/cross.png";
	public static final String INFO_IMAGE = "/library/image/silk/information.png";

	public static final String RSS_IMG = "/library/image/silk/feed.png";
	public static final String ACCEPT_IMG = "/library/image/silk/accept.png";
	public static final String ADD_IMG = "/library/image/silk/add.png";
	public static final String CANCEL_IMG = "/library/image/silk/cancel.png";
	public static final String DELETE_IMG = "/library/image/silk/delete.png";
	public static final String CROSS_IMG = "/library/image/silk/cross.png";
	
	//profile picture settings for use in API and tool and their values for sakai.properties
	//and the default if not specified or invalid one specified
	public static final int PICTURE_SETTING_UPLOAD = 1;
	public static final String PICTURE_SETTING_UPLOAD_PROP = "upload";
	public static final int PICTURE_SETTING_URL = 2;
	public static final String PICTURE_SETTING_URL_PROP = "url";

	public static final int PICTURE_SETTING_DEFAULT = PICTURE_SETTING_UPLOAD;
	
	/*
	 * PREFERENCES
	 */
	
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
	
	
	
	/*
	 * PRIVACY
	 */
	
	//setup the profile privacy values (2 is only used in strict mode)
	public static final int PRIVACY_OPTION_EVERYONE = 0; 
	public static final int PRIVACY_OPTION_ONLYFRIENDS = 1; 
	public static final int PRIVACY_OPTION_ONLYME = 2; 

	//TODO allow these to be overriden in sakai.properties?
	
	//these values are used when creating a default privacy record for a user
	public static final int DEFAULT_PRIVACY_OPTION_PROFILEIMAGE = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_BASICINFO = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_CONTACTINFO = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_ACADEMICINFO = PRIVACY_OPTION_EVERYONE;
	public static final int DEFAULT_PRIVACY_OPTION_PERSONALINFO = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_SEARCH = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_MYFRIENDS = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_MYSTATUS = PRIVACY_OPTION_EVERYONE; 
	
	//if they have no privacy record, ie have not turned explicitly turned searches on or off
	public static final boolean DEFAULT_SEARCH_VISIBILITY = true;
	
	//if they have no privacy record, ie have not turned explicitly turned these options on or off
	public static final boolean DEFAULT_PROFILEIMAGE_VISIBILITY = true;
	public static final boolean DEFAULT_BASICINFO_VISIBILITY = true;
	public static final boolean DEFAULT_CONTACTINFO_VISIBILITY = true;
	public static final boolean DEFAULT_ACADEMICINFO_VISIBILITY = true;
	public static final boolean DEFAULT_PERSONALINFO_VISIBILITY = true;
	public static final boolean DEFAULT_MYFRIENDS_VISIBILITY = true;
	public static final boolean DEFAULT_BIRTHYEAR_VISIBILITY = true;
	public static final boolean DEFAULT_MYSTATUS_VISIBILITY = true;
	
	//if the user doing a search finds themself in the results, should they be included in the results?
	public static final boolean SELF_SEARCH_VISIBILITY = true;
	
	public static final String PROP_BIRTH_YEAR_VISIBLE="birthYearVisible";

	
	
	/**
	 * MISC
	 */
	
	//date format display
	public static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy"; 
	public static final String DEFAULT_DATE_FORMAT_HIDE_YEAR = "dd MMMM"; 

	//max number of search result items before its limited
	public static final int MAX_SEARCH_RESULTS = 30;
	
	//record limits used in searchUsers (could we combine with above?)
	public static final int FIRST_RECORD = 0;		
	public static final int MAX_RECORDS = 99;
	
	public static final String TOOL_ID = "sakai.profile2";
		
	//email constants
	public static final String EMAIL_NEWLINE = "<br />\n";
	
	//event constants
	//this is so granular so we can get good reports on what and how much is being used 
	public static final String EVENT_PROFILE_VIEW_OWN = "profile.view.own";
	public static final String EVENT_PROFILE_VIEW_OTHER = "profile.view.other";
	public static final String EVENT_PROFILE_IMAGE_CHANGE_UPLOAD = "profile.image.change.upload";
	public static final String EVENT_PROFILE_IMAGE_CHANGE_URL = "profile.image.change.url";
	public static final String EVENT_PROFILE_NEW = "profile.new";
		
	public static final String EVENT_PROFILE_INFO_UPDATE = "profile.info.update";
	public static final String EVENT_PROFILE_CONTACT_UPDATE = "profile.contact.update";
	public static final String EVENT_PROFILE_INTERESTS_UPDATE = "profile.interests.update";
	
	public static final String EVENT_FRIEND_REQUEST = "profile.friend.request";
	public static final String EVENT_FRIEND_CONFIRM = "profile.friend.confirm";
	public static final String EVENT_FRIEND_IGNORE = "profile.friend.ignore";
	public static final String EVENT_FRIEND_REMOVE = "profile.friend.remove";

	public static final String EVENT_PRIVACY_NEW = "profile.privacy.new";
	public static final String EVENT_PRIVACY_UPDATE = "profile.privacy.update";
	
	public static final String EVENT_FRIENDS_VIEW_OWN = "profile.friends.view.own";
	public static final String EVENT_FRIENDS_VIEW_OTHER = "profile.friends.view.other";

	public static final String EVENT_SEARCH_BY_NAME = "profile.search.name";
	public static final String EVENT_SEARCH_BY_INTEREST = "profile.search.interest";
	
	public static final String EVENT_PREFERENCES_NEW = "profile.prefs.new";
	public static final String EVENT_PREFERENCES_UPDATE = "profile.prefs.update";

	public static final String EVENT_STATUS_UPDATE = "profile.status.update";
	public static final String EVENT_TWITTER_UPDATE = "profile.twitter.update";

	//custom entity types
	public static final int ENTITY_PROFILE_FULL = 0;
	public static final int ENTITY_PROFILE_MINIMAL = 1;
	public static final int ENTITY_PROFILE_ACADEMIC = 2;
	
	//entity set defaults
	public static final String ENTITY_SET_ACADEMIC = "displayName,imageUrl";
	public static final String ENTITY_SET_MINIMAL = "displayName,statusMessage,statusDate";
	
	//entity css
	public static final String ENTITY_CSS_PROFILE = "/sakai-profile2-tool/css/profile2-profile-entity.css";
	
}
