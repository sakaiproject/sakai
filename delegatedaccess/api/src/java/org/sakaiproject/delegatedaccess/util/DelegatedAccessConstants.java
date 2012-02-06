package org.sakaiproject.delegatedaccess.util;

/**
 * Stores all constants for the delegated access tool
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
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
	public static final int SEARCH_COMPARE_TERM = 3;
	public static final int SEARCH_COMPARE_INSTRUCTOR = 4;
	public static final int SEARCH_COMPARE_AUTHORIZATION = 5;
	public static final int SEARCH_COMPARE_SHOPPERS_BECOME = 6;
	public static final int SEARCH_COMPARE_START_DATE = 7;
	public static final int SEARCH_COMPARE_END_DATE = 8;
	public static final int SEARCH_COMPARE_SHOW_TOOLS = 9;
	public static final String SCHOOL_PROPERTY = "School";
	public static final String DEPEARTMENT_PROPERTY = "Department";
	public static final String SUBJECT_PROPERTY = "Subject";
	public static final String HIERARCHY_ID = "delegatedAccessHierarchyId";
	public static final String SHOPPING_PERIOD_HIERARCHY_ID = "shoppingPeriodHierarchyId";
	public static final String HIERARCHY_UNCATEGORIZED = "uncategorized";
	public static final String HIERARCHY_ROOT_TITLE_DEFAULT = "Sakai";
	public static final String HIERARCHY_ROOT_TITLE_PROPERTY = "delegatedaccess.root.title";
	public static final String HIERARCHY_SITE_PROPERTIES = "delegatedaccess.hierarchy.site.properties";
	public static final String NODE_PERM_REALM_PREFIX = "realm:";
	public static final String NODE_PERM_ROLE_PREFIX = "role:";
	public static final String NODE_PERM_DENY_TOOL_PREFIX = "denyTool:";
	public static final String NODE_PERM_TERM_PREFIX = "term:";
	public static final String NODE_PERM_SITE_VISIT = "site.visit";
	public static final String NODE_PERM_SHOPPING_START_DATE = "shoppingStartDate:";
	public static final String NODE_PERM_SHOPPING_END_DATE = "shoppingEndDate:";
	public static final String NODE_PERM_SHOPPING_AUTH = "shoppingAuth:";
	public static final String NODE_PERM_SHOPPING_ADMIN = "shoppingAdmin";
	public static final String NODE_PERM_SHOPPING_PROCESSED_DATE = "processedDate:";
	public static final String NODE_PERM_SHOPPING_UPDATED_DATE = "updatedDate:";
	public static final String EVENT_ADD_USER_PERMS = "delegatedaccess.nodeperms.add";
	public static final String EVENT_DELETE_USER_PERMS = "delegatedaccess.nodeperms.delete";
	public static final String SESSION_ATTRIBUTE_ACCESS_MAP = "delegatedaccess.accessmap";
	public static final String SESSION_ATTRIBUTE_DENIED_TOOLS = "delegatedaccess.deniedToolsMap";
	public static final String SHOPPING_PERIOD_USER = "120dv0f43cv90sdf0asv9";	
	public static final int TYPE_ACCESS = 1;
	public static final int TYPE_ACCESS_SHOPPING_PERIOD_USER = 2;
	public static final int TYPE_LISTFIELD_TOOLS = 1;
	public static final int TYPE_LISTFIELD_TERMS = 2;
	public static final int TYPE_SHOPPING_PERIOD_ADMIN = 3;
	public static final String SITE_PROP_RESTRICTED_TOOLS = "shopping-period-restricted-tools";
	public static final String PROP_TOOL_LIST = "delegatedaccess.toolslist";
	public static final String PROP_TOOL_LIST_TEMPLATE = "delegatedaccess.toolslist.sitetype";
	public static final String ADVANCED_SEARCH_INSTRUCTOR = "instructorField";
	public static final String ADVANCED_SEARCH_TERM = "termField";
	public static final String PROPERTIES_TERMFIELD = "delegatedaccess.termfield";
	public static final String PROPERTIES_SHOW_TERM_SHOPPING = "delegatedaccess.showtermcolumn.shopping";
	public static final String PROPERTIES_SHOW_TERM_ACCESS = "delegatedaccess.showtermcolumn.access";
	public static final String PROPERTIES_TERM_USE_CM_API = "delegatedaccess.term.useCourseManagementApi";
	public static final String PROPERTIES_HOME_TOOLS = "delegatedaccess.hometools";
	public static final String PROPERTIES_REALM_OPTIONS_SHOPPING = "delegatedaccess.realmoptions.shopping";
	public static final String PROPERTIES_ROLE_OPTIONS_SHOPPING = "delegatedaccess.roleoptions.shopping";
	public static final String PROPERTIES_REALM_OPTIONS_ACCESS = "delegatedaccess.realmoptions.delegatedaccess";
	public static final String PROPERTIES_ROLE_OPTIONS_ACCESS = "delegatedaccess.roleoptions.delegatedaccess";
	public static final String PROPERTIES_EMAIL_ERRORS = "delegatedaccess.email.errors";
	public static final String NODE_PERM_SITE_HIERARCHY_JOB_LAST_RUN_DATE = "siteHierarchyJobLastRunDate:";
	public static final String SITE_HIERARCHY_USER = "777dv0f43bd90sdf012uf";
	public static final int MAX_SITES_PER_PAGE = 10000;
	public static final String PROP_DISABLE_USER_TREE_VIEW = "delegatedaccess.disable.user.tree.view";
	public static final String PROP_DISABLE_SHOPPING_TREE_VIEW = "delegatedaccess.disable.shopping.tree.view";
}
