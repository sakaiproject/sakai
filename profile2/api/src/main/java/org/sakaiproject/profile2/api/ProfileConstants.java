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
package org.sakaiproject.profile2.api;

/**
 * Class to hold static constants for Profile2, like defaults etc.
 *
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileConstants {

	/*
	 * IMAGE
	 */

	//default if not specified in sakai.properties as profile.picture.max (megs)
	public static final int MAX_IMAGE_UPLOAD_SIZE = 2;
	// Approx per second of pronouncation files in KB
	public static final int PRONUNCIATION_SIZE_PER_SECOND = 500;

	//one side will be scaled to this if larger. 200 is large enough
	public static final int MAX_IMAGE_XY = 200;

	//one side will be scaled to this if larger.
	public static final int MAX_THUMBNAIL_IMAGE_XY = 100;

	//directories in content hosting that these images live in
	//also used by ProfileImageExternal
	public static final int PROFILE_IMAGE_MAIN = 1;
	public static final int PROFILE_IMAGE_THUMBNAIL = 2;
	public static final int PROFILE_IMAGE_AVATAR = 3;

	//default images for certain things
	public static final String UNAVAILABLE_IMAGE_THUMBNAIL = "/library/image/no_profile_image_thumbnail.gif";
	public static final String UNAVAILABLE_IMAGE_FULL = "/library/image/no_profile_image.gif";
	public static final String DFLT_PROFILE_AVATAR_COLORS = "#1abc9c,#16a085,#f1c40f,#f39c12,#2ecc71,#27ae60,#e67e22,#d35400,#3498db,#2980b9,#e74c3c,#c0392b,#9b59b6,#8e44ad,#bdc3c7,#34495e,#2c3e50,#95a5a6,#7f8c8d,#ec87bf,#d870ad,#f69785,#9ba37e,#b49255,#b49255,#a94136";
	public static final int PROFILE_AVATAR_WIDTH = 200;
	public static final int PROFILE_AVATAR_HEIGHT = 200;
	public static final String DFLT_PROFILE_AVATAR_FONT_FAMILY = "sans-serif";
	public static final String DFLT_PROFILE_AVATAR_FONT_SIZE_1_CHAR = "120";
	public static final String DFLT_PROFILE_AVATAR_FONT_SIZE_2_CHAR = "90";

	//profile picture settings for use in API and tool and their values for sakai.properties
	//and the default if not specified or invalid one specified
	public static final int PICTURE_SETTING_UPLOAD = 1;
	public static final String PICTURE_SETTING_UPLOAD_PROP = "upload";
	public static final int PICTURE_SETTING_URL = 2;
	public static final String PICTURE_SETTING_URL_PROP = "url";
	public static final int PICTURE_SETTING_OFFICIAL = 3;
	public static final String PICTURE_SETTING_OFFICIAL_PROP = "official";

	public static final int PICTURE_SETTING_DEFAULT = PICTURE_SETTING_UPLOAD;

	// if using official photo, where does that image come from?
	// can be url, provider or filesystem.
	public static final String OFFICIAL_IMAGE_SETTING_URL = "url";
	public static final String OFFICIAL_IMAGE_SETTING_PROVIDER = "provider";
	public static final String OFFICIAL_IMAGE_SETTING_FILESYSTEM = "filesystem";

	public static final String OFFICIAL_IMAGE_SETTING_DEFAULT = OFFICIAL_IMAGE_SETTING_URL;

	//the property that an external provider may set into the user properties for the jpegPhoto field.
	public static final String USER_PROPERTY_JPEG_PHOTO = "jpegPhoto";

	// Defines the name of the blank image, the one a user gets when nothing else is available
	public static final String BLANK = "blank";

	/*
	 * DEFAULT SAKAI PROPERTIES
	 */

	public static final boolean SAKAI_PROP_PROFILE2_PICTURE_CHANGE_ENABLED = true; //profile2.picture.change.enabled
	public static final boolean SAKAI_PROP_PROFILE2_PROFILE_SOCIAL_ENABLED = true; //profile2.profile.social.enabled
	public static final boolean SAKAI_PROP_PROFILE2_PROFILE_PRONUNCIATION_ENABLED = true; //profile2.profile.name.pronunciation.enabled
	public static final boolean SAKAI_PROP_PROFILE2_OFFICIAL_IMAGE_ENABLED = false; //profile2.official.image.enabled

	public static final String TOOL_ID = "sakai.profile2";

	/*
	 * EVENTS
	 */
	public static final String EVENT_PROFILE_NEW = "profile.new";
	public static final String EVENT_PROFILE_NAME_PRONUN_UPDATE = "profile.name.pronunciation.update";
	public static final String EVENT_IMAGE_REQUEST = "profile.image.request";

	/*
	 * PERMISSIONS
	 */
	public static final String ROSTER_VIEW_PHOTO = "roster.viewofficialphoto";
	public static final String ROSTER_VIEW_EMAIL = "roster.viewemail";
	public static final String ROSTER_VIEW_PROFILE = "roster.viewprofile";
}
