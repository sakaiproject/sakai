/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
	public static final int SEARCH_COMPARE_ACCESS = 6;
	public static final int SEARCH_COMPARE_START_DATE = 7;
	public static final int SEARCH_COMPARE_END_DATE = 8;
	public static final int SEARCH_COMPARE_SHOW_TOOLS = 9;
	public static final int SEARCH_COMPARE_ACCESS_MODIFIED = 10;
	public static final int SEARCH_COMPARE_ACCESS_MODIFIED_BY = 11;
	public static final int SEARCH_COMPARE_LEVEL = 12;
	public static final int SEARCH_COMPARE_PUBLISHED = 13;
	public static final int SEARCH_COMPARE_PROVIDERS = 14;
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
	public static final String NODE_PERM_DENY_TOOL2_PREFIX = "deny2Tool:";
	public static final String NODE_PERM_TERM_PREFIX = "term:";
	public static final String NODE_PERM_SITE_VISIT = "site.visit";
	public static final String NODE_PERM_MODIFIED = "modified:";
	public static final String NODE_PERM_MODIFIED_BY = "modifiedBy:";
	public static final String NODE_PERM_SHOPPING_START_DATE = "shoppingStartDate:";
	public static final String NODE_PERM_SHOPPING_END_DATE = "shoppingEndDate:";
	public static final String NODE_PERM_SHOPPING_ADMIN = "shoppingAdmin";
	public static final String NODE_PERM_SHOPPING_ADMIN_MODIFIED = "shoppingAdminModified:";
	public static final String NODE_PERM_SHOPPING_ADMIN_MODIFIED_BY = "shoppingAdminModifiedBy:";
	public static final String NODE_PERM_SHOPPING_REVOKE_INSTRUCTOR_EDITABLE = "shoppingRevokeInstructorEditable";
	public static final String NODE_PERM_SHOPPING_REVOKE_INSTRUCTOR_PUBLIC_OPT = "shoppingRevokeInstructorPublicOpt";
	public static final String NODE_PERM_ALLOW_BECOME_USER = "allowBecomeUser";
	public static final String NODE_PERM_INSTRUCTOR_EDITED = "instructorEdited";
	public static final String NODE_PERM_ACCESS_ADMIN = "accessAdmin";
	/**
	 * A list of all DA permissions that can be assigned to a user.
	 */
	public static final String[] NODE_PERMS = {NODE_PERM_REALM_PREFIX, NODE_PERM_ROLE_PREFIX, NODE_PERM_DENY_TOOL_PREFIX, NODE_PERM_DENY_TOOL2_PREFIX,
												NODE_PERM_TERM_PREFIX, NODE_PERM_SITE_VISIT, NODE_PERM_MODIFIED, NODE_PERM_MODIFIED_BY, NODE_PERM_SHOPPING_START_DATE,
												NODE_PERM_SHOPPING_END_DATE, NODE_PERM_SHOPPING_ADMIN, NODE_PERM_SHOPPING_ADMIN_MODIFIED, NODE_PERM_SHOPPING_ADMIN_MODIFIED_BY,
												NODE_PERM_SHOPPING_REVOKE_INSTRUCTOR_EDITABLE, NODE_PERM_SHOPPING_REVOKE_INSTRUCTOR_PUBLIC_OPT, NODE_PERM_ACCESS_ADMIN, NODE_PERM_ALLOW_BECOME_USER,
												NODE_PERM_INSTRUCTOR_EDITED};
	public static final String EVENT_ADD_USER_PERMS = "dac.nodeperms.add";
	public static final String EVENT_DELETE_USER_PERMS = "dac.nodeperms.delete";
	public static final String EVENT_MODIFIED_USER_PERMS = "dac.nodeperms.modified";
	public static final String EVENT_CHECK_ACCESS = "dac.checkaccess";
	public static final String EVENT_ADD_USER_SHOPPING_ADMIN = "dac.shoppingAdmin.add";
	public static final String EVENT_DELETE_USER_SHOPPING_ADMIN = "dac.shoppingAdmin.delete";
	public static final String EVENT_ADD_USER_ACCESS_ADMIN = "dac.accessAdmin.add";
	public static final String EVENT_DELETE_USER_ACCESS_ADMIN = "dac.accessAdmin.delete";
	public static final String SESSION_ATTRIBUTE_ACCESS_MAP = "delegatedaccess.accessmap";
	public static final String SESSION_ATTRIBUTE_DELEGATED_ACCESS_FLAG = "delegatedaccess.accessmapflag";
	public static final String SESSION_ATTRIBUTE_DENIED_TOOLS = "delegatedaccess.deniedToolsMap";
	public static final String SESSION_ATTRIBUTE_DENIED_TOOLS2 = "delegatedaccess.deniedToolsMap2";
	public static final String SHOPPING_PERIOD_USER = "120dv0f43cv90sdf0asv9";	
	public static final int TYPE_ACCESS = 1;
	public static final int TYPE_ACCESS_SHOPPING_PERIOD_USER = 2;
	public static final int TYPE_ACCESS_ADMIN = 4;
	public static final int TYPE_ADVANCED_OPT = 5;
	public static final int TYPE_LISTFIELD_TOOLS = 1;
	public static final int TYPE_LISTFIELD_TERMS = 2;
	public static final int TYPE_SHOPPING_PERIOD_ADMIN = 3;
	public static final String SITE_PROP_AUTH_TOOLS = "shopping-period-auth-tools";
	public static final String SITE_PROP_PUBLIC_TOOLS = "shopping-period-public-tools";
	public static final String PROP_TOOL_LIST = "delegatedaccess.toolslist";
	public static final String PROP_TOOL_LIST_EXCLUDE = "delegatedaccess.toolslistexclude";
	public static final String PROP_TOOL_LIST_TEMPLATE = "delegatedaccess.toolslist.sitetype";
	public static final String ADVANCED_SEARCH_INSTRUCTOR = "instructorField";
	public static final String ADVANCED_SEARCH_INSTRUCTOR_TYPE = "instructorFieldType";
	public static final String ADVANCED_SEARCH_INSTRUCTOR_TYPE_INSTRUCTOR = "instructor";
	public static final String ADVANCED_SEARCH_INSTRUCTOR_TYPE_MEMBER = "member";
	public static final String ADVANCED_SEARCH_TERM = "termField";
	public static final String ADVANCED_SEARCH_HIERARCHY_FIELDS = "hierarchyFields";
	public static final String PROPERTIES_TERMFIELD = "delegatedaccess.termfield";
	public static final String PROPERTIES_TERM_USE_CM_API = "delegatedaccess.term.useCourseManagementApi";
	public static final String PROPERTIES_TERM_SHOW_LATEST_X_TERMS = "delegatedaccess.term.showLatestXTerms";
	public static final String PROPERTIES_HOME_TOOLS = "delegatedaccess.hometools";
	public static final String PROPERTIES_REALM_OPTIONS_SHOPPING = "delegatedaccess.realmoptions.shopping";
	public static final String PROPERTIES_ROLE_OPTIONS_SHOPPING = "delegatedaccess.roleoptions.shopping";
	public static final String PROPERTIES_REALM_OPTIONS_ACCESS = "delegatedaccess.realmoptions.delegatedaccess";
	public static final String PROPERTIES_ROLE_OPTIONS_ACCESS = "delegatedaccess.roleoptions.delegatedaccess";
	public static final String PROPERTIES_EMAIL_ERRORS = "delegatedaccess.email.errors";
	public static final String PROPERTIES_SYNC_MYWORKSPACE_TOOL = "delegatedaccess.sync.myworkspacetool";
	public static final String PROPERTIES_SHOPPING_INSTRUCTOR_EDITABLE = "delegatedaccess.shopping.instructorEditable";
	public static final String PROPERTIES_HIDE_ROLES_FOR_VIEW_ACCESS = "delegatedaccess.siteaccess.instructorViewable.hiddenRoles";
	public static final String PROPERTIES_ACCESS_ADMIN_ALLOW_SET_ALLOW_BECOME_USER = "delegatedaccess.allow.accessadmin.set.allowBecomeUser";
	public static final String PROPERTIES_ACCESS_ENABLE_PROVIDER_ID_LOOKUP= "delegatedaccess.enableProviderIdLookup";
	public static final String PROPERTIES_SEARCH_HIDE_TERM = "delegatedaccess.search.hideTerm";
	public static final String PROPERTIES_SEARCH_HIERARCH_LABEL = "delegatedaccess.search.hierarchyLabel.";
	public static final String NODE_PERM_SITE_HIERARCHY_JOB_LAST_RUN_DATE = "siteHierarchyJobLastRunDate:";
	public static final String SITE_HIERARCHY_USER = "777dv0f43bd90sdf012uf";
	public static final int MAX_SITES_PER_PAGE = 10000;
	public static final String PROP_DISABLE_USER_TREE_VIEW = "delegatedaccess.disable.user.tree.view";
	public static final String PROP_DISABLE_SHOPPING_TREE_VIEW = "delegatedaccess.disable.shopping.tree.view";
	public static final String[] DEFAULT_HIERARCHY = new String[]{SCHOOL_PROPERTY, DEPEARTMENT_PROPERTY, SUBJECT_PROPERTY};
	public static final String SHOPPING_PERIOD_AUTH_OPTION_ANY = "any";
	public static final String PROPERTIES_SUBADMIN_REALM_ROLE_ORDER = "delegatedaccess.subadmin.realmrole.order";
	public static final String PROPERTIES_ENABLE_ACTIVE_SITE_FLAG = "delegatedaccess.enable.active.site.flag";
	public static final String NODE_PERM_MYWORKSPACE_JOB_STATUS = "wsjstatus:";
}
