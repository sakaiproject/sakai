package org.sakaiproject.site.util;

public class SiteConstants {
	
	/** the list of criteria for sorting */
	public static final String SORTED_BY_TITLE = "title";

	public static final String SORTED_BY_DESCRIPTION = "description";

	public static final String SORTED_BY_TYPE = "type";

	public static final String SORTED_BY_STATUS = "status";

	public static final String SORTED_BY_CREATION_DATE = "creationdate";

	public static final String SORTED_BY_JOINABLE = "joinable";

	public static final String SORTED_BY_PARTICIPANT_NAME = "participant_name";

	public static final String SORTED_BY_PARTICIPANT_UNIQNAME = "participant_uniqname";

	public static final String SORTED_BY_PARTICIPANT_ROLE = "participant_role";

	public static final String SORTED_BY_PARTICIPANT_ID = "participant_id";

	public static final String SORTED_BY_PARTICIPANT_COURSE = "participant_course";

	public static final String SORTED_BY_PARTICIPANT_CREDITS = "participant_credits";
	
	public static final String SORTED_BY_PARTICIPANT_STATUS = "participant_status";

	public static final String SORTED_BY_MEMBER_NAME = "member_name";
	
	public static final String SORTED_BY_GROUP_TITLE = "group_title";
	
	public static final String SORTED_BY_GROUP_SIZE = "group_size";
	
	public static final String GROUP_PROP_ROLE_PROVIDERID = "group_prop_role_providerid";
	
	public static final int SITE_GROUP_TITLE_LIMIT = 99;
	
	// system property variable to hide PageOrder tab for certain types of sites, e.g. if set to "course,project", the PageOrder tool tab will be hidden for all course sites and project sites. 
	public final static String SAKAI_PROPERTY_HIDE_PAGEORDER_SITE_TYPES = "hide.pageorder.site.types";
	
	// site property variable to override the above settings. If true, the PageOrder tab will be shown.
	public final static String SITE_PROPERTY_OVERRIDE_HIDE_PAGEORDER_SITE_TYPES = "site.override.hide.pageorder.site.types";
	
	public final static String SITE_TYPE_MYWORKSPACE = "site_type_myworkspace";

	// All deleted sites.
	public final static String SITE_TYPE_DELETED = "site_type_deleted";
	
	public final static String SITE_TYPE_ALL = "site_type_all";
}
