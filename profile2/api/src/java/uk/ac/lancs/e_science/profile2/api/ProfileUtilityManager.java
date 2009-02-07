package uk.ac.lancs.e_science.profile2.api;

public class ProfileUtilityManager {

	//date format display
	public static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy"; 
	public static final String DEFAULT_DATE_FORMAT_HIDE_YEAR = "dd MMMM"; 

	//max number of search result items before its limited
	public static final int MAX_SEARCH_RESULTS = 3;
	
	//event constants
	public static final String EVENT_PROFILE_VIEW_OWN = "profile.view.own";
	public static final String EVENT_PROFILE_VIEW_OTHER = "profile.view.other";
	public static final String EVENT_PROFILE_UPDATE = "profile.update";
	public static final String EVENT_PROFILE_IMAGE_CHANGE = "profile.image.change";
	public static final String EVENT_PROFILE_NEW = "profile.new";
	
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

}
