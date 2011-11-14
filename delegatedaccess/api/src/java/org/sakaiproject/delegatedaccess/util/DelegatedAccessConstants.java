package org.sakaiproject.delegatedaccess.util;

public class DelegatedAccessConstants {

	public static final int SEARCH_RESULTS_MAX = 99;
	public static final int SEARCH_RESULTS_PAGE_SIZE = 20;
	public static final int SEARCH_COMPARE_EID = 1;
	public static final int SEARCH_COMPARE_SORT_NAME = 2;
	public static final int SEARCH_COMPARE_EMAIL = 3;
	public static final int SEARCH_COMPARE_TYPE = 4;
	public static final int SEARCH_COMPARE_DEFAULT = SEARCH_COMPARE_EID;
	public static final int SEARCH_COMPARE_SITE_TITLE = 1;
	public static final int SEARCH_COMPARE_SITE_ID = 2;
	public static final String SCHOOL_PROPERTY = "School";
	public static final String DEPEARTMENT_PROPERTY = "Department";
	public static final String SUBJECT_PROPERTY = "Subject";
	public static final String HIERARCHY_ID = "delegatedAccessHierarchyId";
	public static final String HIERARCHY_UNCATEGORIZED = "uncategorized";
	public static final String HIERARCHY_ROOT_TITLE_DEFAULT = "Sakai";
	public static final String HIERARCHY_ROOT_TITLE_PROPERTY = "delegatedaccess.root.title";
	public static final String HIERARCHY_SITE_PROPERTIES = "delegatedaccess.hierarchy.site.properties";
	public static final String NODE_PERM_REALM_PREFIX = "realm:";
	public static final String NODE_PERM_ROLE_PREFIX = "role:";
	public static final String NODE_PERM_DENY_TOOL_PREFIX = "denyTool:";
	public static final String EVENT_ADD_USER_PERMS = "delegatedaccess.nodeperms.add";
	public static final String EVENT_DELETE_USER_PERMS = "delegatedaccess.nodeperms.delete";
}
