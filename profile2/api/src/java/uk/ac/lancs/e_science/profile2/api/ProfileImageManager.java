package uk.ac.lancs.e_science.profile2.api;

public class ProfileImageManager {

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
	

}
