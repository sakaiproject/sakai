/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.util;

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
	
	//connection status
	public static final int CONNECTION_NONE = 0;
	public static final int CONNECTION_REQUESTED = 1;
	public static final int CONNECTION_INCOMING = 2;
	public static final int CONNECTION_CONFIRMED = 3;

	
	/*
	 * IMAGE
	 */
	
	//default if not specified in sakai.properties as profile.picture.max (megs)
	public static final int MAX_IMAGE_UPLOAD_SIZE = 2; 
	
	//one side will be scaled to this if larger. 200 is large enough
	public static final int MAX_IMAGE_XY = 200; 	
	
	//one side will be scaled to this if larger. 
	public static final int MAX_THUMBNAIL_IMAGE_XY = 100; 	
    
	//directories in content hosting that these images live in
	//also used by ProfileImageExternal
	public static final int PROFILE_IMAGE_MAIN = 1;		
	public static final int PROFILE_IMAGE_THUMBNAIL = 2;
	public static final int PROFILE_IMAGE_AVATAR = 3;
	
	//should images be marked up in a way that a browser can cache them?
	public static final boolean PROFILE_IMAGE_CACHE = true;
	
	//gallery-related constants
	public static final String GALLERY_IMAGE_MAIN = "images";
	public static final String GALLERY_IMAGE_THUMBNAILS = "thumbnails";
	public static final String GALLERY_IMAGE_DEFAULT_DESCRIPTION = "Gallery image";
	public static final int MAX_GALLERY_IMAGE_XY = 575;
	public static final int MAX_GALLERY_THUMBNAIL_IMAGE_XY = 125;
	public static final int MAX_GALLERY_FILE_UPLOADS = 10;
	public static final int MAX_GALLERY_IMAGES_PER_PAGE = 12; //max before pager kicks in

	
	//default images for certain things
	public static final String UNAVAILABLE_IMAGE = "images/no_image.gif";
	public static final String UNAVAILABLE_IMAGE_THUMBNAIL = "/profile2-tool/images/no_image_thumbnail.gif";
	public static final String UNAVAILABLE_IMAGE_FULL = "/profile2-tool/images/no_image.gif";
	public static final String CLOSE_IMAGE = "/library/image/silk/cross.png";
	public static final String INFO_IMAGE = "/library/image/silk/information.png";
	
	public static final String RSS_IMG = "/library/image/silk/feed.png";
	public static final String ACCEPT_IMG = "/library/image/silk/accept.png";
	public static final String ADD_IMG = "/library/image/silk/add.png";
	public static final String CANCEL_IMG = "/library/image/silk/cancel.png";
	public static final String DELETE_IMG = "/library/image/silk/delete.png";
	public static final String CROSS_IMG = "/library/image/silk/cross.png";
	public static final String CROSS_IMG_LOCAL = "images/cross.png";
	
	public static final String AWARD_NORMAL_IMG = "/library/image/silk/medal_silver_1.png";
	public static final String AWARD_BRONZE_IMG = "/library/image/silk/award_star_bronze_3.png";
	public static final String AWARD_SILVER_IMG = "/library/image/silk/award_star_silver_3.png";
	public static final String AWARD_GOLD_IMG = "/library/image/silk/award_star_gold_3.png";

	public static final String ONLINE_STATUS_ONLINE_IMG = "/library/image/silk/bullet_green.png";
	public static final String ONLINE_STATUS_OFFLINE_IMG = "/library/image/silk/bullet_black.png";
	public static final String ONLINE_STATUS_AWAY_IMG = "/library/image/silk/bullet_yellow.png";

	
	//profile picture settings for use in API and tool and their values for sakai.properties
	//and the default if not specified or invalid one specified
	public static final int PICTURE_SETTING_UPLOAD = 1;
	public static final String PICTURE_SETTING_UPLOAD_PROP = "upload";
	public static final int PICTURE_SETTING_URL = 2;
	public static final String PICTURE_SETTING_URL_PROP = "url";
	public static final int PICTURE_SETTING_OFFICIAL = 3;
	public static final String PICTURE_SETTING_OFFICIAL_PROP = "official";
	public static final int PICTURE_SETTING_GRAVATAR = 4;
	//n.b a gravatar is not an enforceable setting, hence no property here. it is purely a choice.
	//it can be disabled in sakai.properties if required.
	
	public static final int PICTURE_SETTING_DEFAULT = PICTURE_SETTING_UPLOAD;
	
	// if using official photo, where does that image come from?
	// can be url, provider or filesystem. 
	public static final String OFFICIAL_IMAGE_SETTING_URL = "url";
	public static final String OFFICIAL_IMAGE_SETTING_PROVIDER = "provider";
	public static final String OFFICIAL_IMAGE_SETTING_FILESYSTEM = "filesystem";
	
	public static final String OFFICIAL_IMAGE_SETTING_DEFAULT = OFFICIAL_IMAGE_SETTING_URL;

	//the property that an external provider may set into the user properties for the jpegPhoto field.
	public static final String USER_PROPERTY_JPEG_PHOTO = "jpegPhoto";
	
	//gravatar base URL
	public static final String GRAVATAR_BASE_URL = "//www.gravatar.com/avatar/";

    // Defines the name of the blank image, the one a user gets when nothing else is available
    public static final String BLANK = "blank";
	
	
	/*
	 * SEARCH
	 */
	public static final int DEFAULT_MAX_SEARCH_RESULTS = 50;
	public static final int DEFAULT_MAX_SEARCH_RESULTS_PER_PAGE = 25;
	public static final int DEFAULT_MAX_SEARCH_HISTORY = 5;
	public static final String SEARCH_COOKIE = "profile2-search";
	public static final String SEARCH_TYPE_NAME = "name";
	public static final String SEARCH_TYPE_INTEREST = "interest";
	public static final String SEARCH_COOKIE_VALUE_PAGE_MARKER = "[";
	public static final String SEARCH_COOKIE_VALUE_SEARCH_MARKER = "]";
	public static final String SEARCH_COOKIE_VALUE_CONNECTIONS_MARKER = "(";
	public static final String SEARCH_COOKIE_VALUE_WORKSITE_MARKER = ")";

	/*
	 * PREFERENCES
	 */

	//these values are used when creating a default preferences record for a user
	public static final boolean DEFAULT_EMAIL_REQUEST_SETTING = true;
	public static final boolean DEFAULT_EMAIL_CONFIRM_SETTING = true;
	public static final boolean DEFAULT_EMAIL_MESSAGE_NEW_SETTING = true;
	public static final boolean DEFAULT_EMAIL_MESSAGE_REPLY_SETTING = true;
	public static final boolean DEFAULT_EMAIL_MESSAGE_WALL_SETTING = true;
	public static final boolean DEFAULT_EMAIL_MESSAGE_WORKSITE_SETTING = true;
	public static final boolean DEFAULT_OFFICIAL_IMAGE_SETTING = false;
	public static final boolean DEFAULT_SHOW_KUDOS_SETTING = true;
	public static final boolean DEFAULT_SHOW_GALLERY_FEED_SETTING = true;
	public static final boolean DEFAULT_GRAVATAR_SETTING = false;
	public static final boolean DEFAULT_SHOW_ONLINE_STATUS_SETTING = true;
	
	
	/*
	 * PRIVACY
	 */
	
	//setup the profile privacy values (2 is only used in strict mode, 3 is only used in super strict mode)
	public static final int PRIVACY_OPTION_EVERYONE = 0; 
	public static final int PRIVACY_OPTION_ONLYFRIENDS = 1; 
	public static final int PRIVACY_OPTION_ONLYME = 2; 
	public static final int PRIVACY_OPTION_NOBODY = 3; 
	
	//these values are used when creating a default privacy record for a user
	public static final int DEFAULT_PRIVACY_OPTION_PROFILEIMAGE = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_BASICINFO = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_CONTACTINFO = PRIVACY_OPTION_EVERYONE;
	public static final int DEFAULT_PRIVACY_OPTION_PERSONALINFO = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_SEARCH = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_MYFRIENDS = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_MYSTATUS = PRIVACY_OPTION_EVERYONE; 
	public static final int DEFAULT_PRIVACY_OPTION_MYPICTURES = PRIVACY_OPTION_EVERYONE;
	public static final int DEFAULT_PRIVACY_OPTION_MESSAGES = PRIVACY_OPTION_ONLYFRIENDS;
	public static final int DEFAULT_PRIVACY_OPTION_BUSINESSINFO = PRIVACY_OPTION_EVERYONE;
	public static final int DEFAULT_PRIVACY_OPTION_STAFFINFO = PRIVACY_OPTION_EVERYONE;
	public static final int DEFAULT_PRIVACY_OPTION_STUDENTINFO = PRIVACY_OPTION_EVERYONE;
	public static final int DEFAULT_PRIVACY_OPTION_MYKUDOS = PRIVACY_OPTION_EVERYONE;
	public static final int DEFAULT_PRIVACY_OPTION_MYWALL = PRIVACY_OPTION_EVERYONE;
	public static final int DEFAULT_PRIVACY_OPTION_SOCIALINFO = PRIVACY_OPTION_EVERYONE;
	public static final int DEFAULT_PRIVACY_OPTION_ONLINESTATUS = PRIVACY_OPTION_EVERYONE;

	public static final boolean DEFAULT_BIRTHYEAR_VISIBILITY = true;
	
	/*
	 * DEFAULT SAKAI PROPERTIES
	 */
	
	public static final String SAKAI_PROP_INVISIBLE_USERS = "postmaster"; //string, comma separated
	public static final char SAKAI_PROP_LIST_SEPARATOR = ','; //char used to separate multi value lists
	public static final String SAKAI_PROP_SERVICE_NAME = "Sakai"; //ui.service
	public static final boolean SAKAI_PROP_PROFILE2_CONVERSION_ENABLED = false; //profile2.convert
	public static final boolean SAKAI_PROP_PROFILE2_TWITTER_INTEGRATION_ENABLED = true; //profile2.integration.twitter.enabled
	public static final String SAKAI_PROP_PROFILE2_TWITTER_INTEGRATION_SOURCE = "Profile2"; //profile2.integration.twitter.source
	public static final boolean SAKAI_PROP_PROFILE2_PICTURE_CHANGE_ENABLED = true; //profile2.picture.change.enabled
	public static final boolean SAKAI_PROP_PROFILE2_PRIVACY_CHANGE_ENABLED = true; //profile2.privacy.change.enabled
	public static final boolean SAKAI_PROP_PROFILE2_GALLERY_ENABLED = true; //profile2.gallery.enabled
	public static final boolean SAKAI_PROP_PROFILE2_PROFILE_BUSINESS_ENABLED = false; //profile2.profile.business.enabled
	public static final boolean SAKAI_PROP_PROFILE2_PROFILE_SOCIAL_ENABLED = true; //profile2.profile.social.enabled
	public static final boolean SAKAI_PROP_PROFILE2_PROFILE_STAFF_ENABLED = true; //profile2.profile.staff.enabled
	public static final boolean SAKAI_PROP_PROFILE2_PROFILE_STUDENT_ENABLED = true; //profile2.profile.student.enabled
	public static final boolean SAKAI_PROP_PROFILE2_PROFILE_INTERESTS_ENABLED = true; //profile2.profile.interests.enabled
	public static final boolean SAKAI_PROP_PROFILE2_OFFICIAL_IMAGE_ENABLED = false; //profile2.official.image.enabled
	public static final boolean SAKAI_PROP_PROFILE2_GRAVATAR_IMAGE_ENABLED = true; //profile2.gravatar.image.enabled
	public static final boolean SAKAI_PROP_PROFILE2_WALL_ENABLED = false; //profile2.wall.enabled
	public static final boolean SAKAI_PROP_PROFILE2_WALL_DEFAULT = false; //profile2.wall.default
	public static final boolean SAKAI_PROP_PROFILE2_GOOGLE_INTEGRATION_ENABLED = false; //profile2.integration.google.enabled
	public static final boolean SAKAI_PROP_PROFILE2_IMPORT_ENABLED = false; //profile2.import
	public static final boolean SAKAI_PROP_PROFILE2_PROFILE_FIELDS_ENABLED = true; //profile2.profile.fields.enabled
	public static final boolean SAKAI_PROP_PROFILE2_PROFILE_STATUS_ENABLED = true; //profile2.profile.status.enabled
	public static final boolean SAKAI_PROP_PROFILE2_IMPORT_IMAGES_ENABLED = false; // profile2.import.images
	public static final boolean SAKAI_PROP_PROFILE2_MENU_ENABLED = true; //profile2.menu.enabled
	public static final boolean SAKAI_PROP_PROFILE2_CONNECTIONS_ENABLED = true; //profile2.connections.enabled
	public static final boolean SAKAI_PROP_PROFILE2_MESSAGING_ENABLED = true; //profile2.messaging.enabled
	public static final boolean SAKAI_PROP_PROFILE2_SEARCH_ENABLED = true; //profile2.search.enabled
	public static final boolean SAKAI_PROP_PROFILE2_PRIVACY_ENABLED = true; //profile2.privacy.enabled
	public static final boolean SAKAI_PROP_PROFILE2_PREFERENCE_ENABLED = true; //profile2.preference.enabled
	public static final boolean SAKAI_PROP_PROFILE2_MY_KUDOS_ENABLED = true; //profile2.myKudos.enabled
	public static final boolean SAKAI_PROP_PROFILE2_ONLINE_STATUS_ENABLED = true; //profile2.onlineStatus.enabled


	
	/*
	 * MESSAGING
	 */
	//max number of connections that can be shown in an autocomplete search that match the criteria
	public static final int MAX_CONNECTIONS_PER_SEARCH = 15;
	
	//default subject if none supplied
	public static final String DEFAULT_PRIVATE_MESSAGE_SUBJECT = "(no subject)";
	
	//max number of messages to show per page
	public static final int MAX_MESSAGES_PER_PAGE = 15;
	
	//date format display for messages
	public static final String MESSAGE_DISPLAY_DATE_FORMAT = "dd MMMM 'at' HH:mm";
	
	//max length of the preview of a message
	public static final int MESSAGE_PREVIEW_MAX_LENGTH = 150;
	
	
	/*
	 * MISC
	 */
	
	//date format display
	public static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy"; 
	public static final String DEFAULT_DATE_FORMAT_HIDE_YEAR = "dd MMMM";
	
	//max number of connections to show per page
	public static final int MAX_CONNECTIONS_PER_PAGE = 15;
	
	//record limits used in searchUsers (could we combine with above?)
	public static final int FIRST_RECORD = 1;		
	public static final int MAX_RECORDS = 99;
	
	//tool id
	public static final String TOOL_ID = "sakai.profile2";
		
	//email constants
	public static final String EMAIL_NEWLINE = "<br />\n";
	
	/*
	 * TABS
	 */
	public static final String TAB_COOKIE = "profile2-tab";
	public static final int TAB_INDEX_PROFILE = 0;
	public static final int TAB_INDEX_WALL = 1;
	public static final String PROFILE = "profile";
	public static final String WALL = "wall";
	
	/*
	 * WALL 
	 */
	public static final int WALL_ITEM_TYPE_EVENT = 0;
	public static final int WALL_ITEM_TYPE_STATUS = 1;
	public static final int WALL_ITEM_TYPE_POST = 2;
	
	//date format display for wall items and wall comments
	public static final String WALL_DISPLAY_DATE_FORMAT = "dd MMMMM, HH:mm";
	
	// TODO possible candidates for sakai.properties
	public static final int MAX_WALL_ITEMS_SAVED_PER_USER = 30;
	public static final int MAX_WALL_ITEMS_PER_PAGE = 10;
	
	/*
	 * EVENTS
	 */
	
	//this is so granular so we can get good reports on what and how much is being used 
	public static final String EVENT_PROFILE_VIEW_OWN = "profile.view.own";
	public static final String EVENT_PROFILE_VIEW_OTHER = "profile.view.other";
	public static final String EVENT_PROFILE_IMAGE_CHANGE_UPLOAD = "profile.image.change.upload";
	public static final String EVENT_PROFILE_IMAGE_CHANGE_URL = "profile.image.change.url";
	public static final String EVENT_PROFILE_IMAGE_UPLOAD = "profile.image.upload";
	public static final String EVENT_PROFILE_NEW = "profile.new";
		
	public static final String EVENT_PROFILE_INFO_UPDATE = "profile.info.update";
	public static final String EVENT_PROFILE_CONTACT_UPDATE = "profile.contact.update";
	public static final String EVENT_PROFILE_INTERESTS_UPDATE = "profile.interests.update";
	public static final String EVENT_PROFILE_STAFF_UPDATE = "profile.staff.update";
	public static final String EVENT_PROFILE_STUDENT_UPDATE = "profile.student.update";
	public static final String EVENT_PROFILE_SOCIAL_NETWORKING_UPDATE = "profile.socialnetworking.update";
	public static final String EVENT_PROFILE_BUSINESS_UPDATE = "profile.business.update";
	
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
	
	public static final String EVENT_MESSAGE_SENT = "profile.message.sent";
	
	public static final String EVENT_GALLERY_IMAGE_UPLOAD = "profile.gallery.image.upload";

	public static final String EVENT_WALL_ITEM_NEW = "profile.wall.item.new";
	public static final String EVENT_WALL_ITEM_REMOVE = "profile.wall.item.remove";
	public static final String EVENT_WALL_ITEM_COMMENT_NEW = "profile.wall.item.comment.new";
	
	
	/*
	 * ENTITY
	 */
	
	//custom entity types
	public static final int ENTITY_PROFILE_FULL = 0;
	public static final int ENTITY_PROFILE_MINIMAL = 1;
	public static final int ENTITY_PROFILE_ACADEMIC = 2;
	
	//entity set defaults
	public static final String ENTITY_SET_ACADEMIC = "displayName,imageUrl";
	public static final String ENTITY_SET_MINIMAL = "displayName,statusMessage,statusDate";
	
	//entity css
	public static final String ENTITY_CSS_PROFILE = "/profile2-tool/css/profile2-profile-entity.css";
	
	//max length of the personal summary in the formatted profile
	public static final String FORMATTED_PROFILE_SUMMARY_MAX_LENGTH = "1000"; //profile2.formatted.profile.summary.max
	
	
	/*
	 * EMAIL TEMPLATING
	 */
	
	public static final String EMAIL_TEMPLATE_KEY_MESSAGE_NEW = "profile2.messageNew";
	public static final String EMAIL_TEMPLATE_KEY_MESSAGE_REPLY = "profile2.messageReply";
	public static final String EMAIL_TEMPLATE_KEY_CONNECTION_REQUEST = "profile2.connectionRequest";
	public static final String EMAIL_TEMPLATE_KEY_CONNECTION_CONFIRM = "profile2.connectionConfirm";
	public static final String EMAIL_TEMPLATE_KEY_WALL_EVENT_NEW = "profile2.wallEventNew";
	public static final String EMAIL_TEMPLATE_KEY_WALL_POST_MY_NEW = "profile2.wallPostMyWallNew";
	public static final String EMAIL_TEMPLATE_KEY_WALL_POST_CONNECTION_NEW = "profile2.wallPostConnectionWallNew";
	public static final String EMAIL_TEMPLATE_KEY_WALL_STATUS_NEW = "profile2.wallStatusNew";
	public static final String EMAIL_TEMPLATE_KEY_WORKSITE_NEW = "profile2.worksiteNew";
	public static final String EMAIL_TEMPLATE_KEY_PROFILE_CHANGE_NOTIFICATION = "profile2.profileChangeNotification";
	
	/*
	 * DIRECT LINKS
	 */
	
	public static final String ENTITY_BROKER_PREFIX = "/direct";
	public static final String LINK_ENTITY_PREFIX = "/my";
	public static final String LINK_ENTITY_PROFILE = "/profile";
	public static final String LINK_ENTITY_MESSAGES = "/messages";
	public static final String LINK_ENTITY_CONNECTIONS = "/connections";
	public static final String LINK_ENTITY_WALL = "/wall";

	//full class names for Wicket pages used when we translate a shortened URL back into the full one
	//and need to go directly to a page
	public static final String WICKET_PAGE_CONNECTIONS = "org.sakaiproject.profile2.tool.pages.MyFriends";
	public static final String WICKET_PAGE_MESSAGES = "org.sakaiproject.profile2.tool.pages.MyMessages";
	public static final String WICKET_PAGE_PROFILE_VIEW = "org.sakaiproject.profile2.tool.pages.ViewProfile";
	public static final String WICKET_PARAM_THREAD = "thread";
	public static final String WICKET_PARAM_USERID = "id";
	public static final String WICKET_PARAM_WALL_ITEM = "wallItemId";
	public static final String WICKET_PARAM_TAB = "tab";

	/*
	 * ONLINE STATUS
	 */
	
	public static final int ONLINE_STATUS_OFFLINE = 0;
	public static final int ONLINE_STATUS_ONLINE = 1;
	public static final int ONLINE_STATUS_AWAY = 2;

	public static final long ONLINE_INACTIVITY_INTERVAL = 5000000; // 5 minutes between events  = online -> away
	
	/*
	 * PERMISSIONS
	 */
	
	public static final String ROSTER_VIEW_PHOTO = "roster.viewofficialphoto";
	public static final String ROSTER_VIEW_EMAIL = "roster.viewemail";
	
	
	/*
	 * INTEGRATIONS
	 */
	public static final String GOOGLE_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	public static final String GOOGLE_DOCS_SCOPE = "https://docs.google.com/feeds/";

	
	
}
