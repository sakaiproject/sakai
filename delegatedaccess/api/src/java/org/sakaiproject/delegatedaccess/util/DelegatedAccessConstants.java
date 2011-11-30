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
	public static final int TYPE_SHOPPING_PERIOD_ADMIN = 3;
	public static final String SITE_PROP_HIERARCHY_NODE_ID = "hierarchy-node-id";
	public static final String SITE_PROP_RESTRICTED_TOOLS = "shopping-period-restricted-tools";
	public static final String PROP_TOOL_LIST = "delegatedaccess.toolslist";
	public static final String PROP_TOOL_LIST_TEMPLATE = "delegatedaccess.toolslist.sitetype";

}
