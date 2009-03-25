package uk.ac.lancs.e_science.profile2.api;

public class ProfileUtilityManager {

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

	
	
	
}
