/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.site.tool;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.tools.generic.SortTool;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.cover.AliasService;
import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.archive.cover.ArchiveService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.email.cover.EmailService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportService;
import org.sakaiproject.importer.api.SakaiArchive;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitemanage.api.SectionField;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ArrayUtil;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;

/**
 * <p>
 * SiteAction controls the interface for worksite setup.
 * </p>
 */
public class SiteAction extends PagedResourceActionII {
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SiteAction.class);

	private ImportService importService = org.sakaiproject.importer.cover.ImportService
			.getInstance();

	/** portlet configuration parameter values* */
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("sitesetupgeneric");

	private org.sakaiproject.coursemanagement.api.CourseManagementService cms = (org.sakaiproject.coursemanagement.api.CourseManagementService) ComponentManager
			.get(org.sakaiproject.coursemanagement.api.CourseManagementService.class);

	private org.sakaiproject.authz.api.GroupProvider groupProvider = (org.sakaiproject.authz.api.GroupProvider) ComponentManager
			.get(org.sakaiproject.authz.api.GroupProvider.class);

	private org.sakaiproject.authz.api.AuthzGroupService authzGroupService = (org.sakaiproject.authz.api.AuthzGroupService) ComponentManager
			.get(org.sakaiproject.authz.api.AuthzGroupService.class);

	private org.sakaiproject.sitemanage.api.SectionFieldProvider sectionFieldProvider = (org.sakaiproject.sitemanage.api.SectionFieldProvider) ComponentManager
			.get(org.sakaiproject.sitemanage.api.SectionFieldProvider.class);
	
	private org.sakaiproject.sitemanage.api.AffiliatedSectionProvider affiliatedSectionProvider = (org.sakaiproject.sitemanage.api.AffiliatedSectionProvider) ComponentManager
	.get(org.sakaiproject.sitemanage.api.AffiliatedSectionProvider.class);

	private org.sakaiproject.sitemanage.api.UserNotificationProvider userNotificationProvider = (org.sakaiproject.sitemanage.api.UserNotificationProvider) ComponentManager
	.get(org.sakaiproject.sitemanage.api.UserNotificationProvider.class);
	
	private static final String SITE_MODE_SITESETUP = "sitesetup";

	private static final String SITE_MODE_SITEINFO = "siteinfo";

	private static final String STATE_SITE_MODE = "site_mode";

	protected final static String[] TEMPLATE = {
			"-list",// 0
			"-type",
			"-newSiteInformation",
			"-newSiteFeatures",
			"-addRemoveFeature",
			"-addParticipant",
			"",
			"",
			"-siteDeleteConfirm",
			"",
			"-newSiteConfirm",// 10
			"",
			"-siteInfo-list",// 12
			"-siteInfo-editInfo",
			"-siteInfo-editInfoConfirm",
			"-addRemoveFeatureConfirm",// 15
			"",
			"",
			"-siteInfo-editAccess",
			"-addParticipant-sameRole",
			"-addParticipant-differentRole",// 20
			"-addParticipant-notification",
			"-addParticipant-confirm",
			"",
			"",
			"",// 25
			"-modifyENW", "-importSites",
			"-siteInfo-import",
			"-siteInfo-duplicate",
			"",// 30
			"",// 31
			"",// 32
			"",// 33
			"",// 34
			"",// 35
			"-newSiteCourse",// 36
			"-newSiteCourseManual",// 37
			"",// 38
			"",// 39
			"",// 40
			"",// 41
			"-gradtoolsConfirm",// 42
			"-siteInfo-editClass",// 43
			"-siteInfo-addCourseConfirm",// 44
			"-siteInfo-importMtrlMaster", // 45 -- htripath for import
			// material from a file
			"-siteInfo-importMtrlCopy", // 46
			"-siteInfo-importMtrlCopyConfirm",
			"-siteInfo-importMtrlCopyConfirmMsg", // 48
			"-siteInfo-group", // 49
			"-siteInfo-groupedit", // 50
			"-siteInfo-groupDeleteConfirm", // 51,
			"",
			"-findCourse" // 53
	};

	/** Name of state attribute for Site instance id */
	private static final String STATE_SITE_INSTANCE_ID = "site.instance.id";

	/** Name of state attribute for Site Information */
	private static final String STATE_SITE_INFO = "site.info";

	/** Name of state attribute for CHEF site type */
	private static final String STATE_SITE_TYPE = "site-type";

	/** Name of state attribute for poissible site types */
	private static final String STATE_SITE_TYPES = "site_types";

	private static final String STATE_DEFAULT_SITE_TYPE = "default_site_type";

	private static final String STATE_PUBLIC_CHANGEABLE_SITE_TYPES = "changeable_site_types";

	private static final String STATE_PUBLIC_SITE_TYPES = "public_site_types";

	private static final String STATE_PRIVATE_SITE_TYPES = "private_site_types";

	private static final String STATE_DISABLE_JOINABLE_SITE_TYPE = "disable_joinable_site_types";

	// Names of state attributes corresponding to properties of a site
	private final static String PROP_SITE_CONTACT_EMAIL = "contact-email";

	private final static String PROP_SITE_CONTACT_NAME = "contact-name";

	private final static String PROP_SITE_TERM = "term";

	private final static String PROP_SITE_TERM_EID = "term_eid";

	/**
	 * Name of the state attribute holding the site list column list is sorted
	 * by
	 */
	private static final String SORTED_BY = "site.sorted.by";

	/** the list of criteria for sorting */
	private static final String SORTED_BY_TITLE = "title";

	private static final String SORTED_BY_DESCRIPTION = "description";

	private static final String SORTED_BY_TYPE = "type";

	private static final String SORTED_BY_STATUS = "status";

	private static final String SORTED_BY_CREATION_DATE = "creationdate";

	private static final String SORTED_BY_JOINABLE = "joinable";

	private static final String SORTED_BY_PARTICIPANT_NAME = "participant_name";

	private static final String SORTED_BY_PARTICIPANT_UNIQNAME = "participant_uniqname";

	private static final String SORTED_BY_PARTICIPANT_ROLE = "participant_role";

	private static final String SORTED_BY_PARTICIPANT_ID = "participant_id";

	private static final String SORTED_BY_PARTICIPANT_COURSE = "participant_course";

	private static final String SORTED_BY_PARTICIPANT_CREDITS = "participant_credits";

	private static final String SORTED_BY_MEMBER_NAME = "member_name";

	/** Name of the state attribute holding the site list column to sort by */
	private static final String SORTED_ASC = "site.sort.asc";

	/** State attribute for list of sites to be deleted. */
	private static final String STATE_SITE_REMOVALS = "site.removals";

	/** Name of the state attribute holding the site list View selected */
	private static final String STATE_VIEW_SELECTED = "site.view.selected";

	/** Names of lists related to tools */
	private static final String STATE_TOOL_REGISTRATION_LIST = "toolRegistrationList";

	private static final String STATE_TOOL_REGISTRATION_SELECTED_LIST = "toolRegistrationSelectedList";

	private static final String STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST = "toolRegistrationOldSelectedList";

	private static final String STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME = "toolRegistrationOldSelectedHome";

	private static final String STATE_TOOL_EMAIL_ADDRESS = "toolEmailAddress";

	private static final String STATE_TOOL_HOME_SELECTED = "toolHomeSelected";

	private static final String STATE_PROJECT_TOOL_LIST = "projectToolList";

	private final static String STATE_NEWS_TITLES = "newstitles";

	private final static String STATE_NEWS_URLS = "newsurls";

	private final static String NEWS_DEFAULT_TITLE = ServerConfigurationService
			.getString("news.title");

	private final static String NEWS_DEFAULT_URL = ServerConfigurationService
			.getString("news.feedURL");

	private final static String SITE_DEFAULT_LIST = ServerConfigurationService
			.getString("site.types");

	private final static String STATE_WEB_CONTENT_TITLES = "webcontenttitles";

	private final static String STATE_WEB_CONTENT_URLS = "wcUrls";

	private final static String WEB_CONTENT_DEFAULT_TITLE = "Web Content";

	private final static String WEB_CONTENT_DEFAULT_URL = "http://";

	private final static String STATE_SITE_QUEST_UNIQNAME = "site_quest_uniqname";
	
	private static final String STATE_SITE_ADD_COURSE = "canAddCourse";
	

	// %%% get rid of the IdAndText tool lists and just use ToolConfiguration or
	// ToolRegistration lists
	// %%% same for CourseItems

	// Names for other state attributes that are lists
	private final static String STATE_WORKSITE_SETUP_PAGE_LIST = "wSetupPageList"; // the

	// list
	// of
	// site
	// pages
	// consistent
	// with
	// Worksite
	// Setup
	// page
	// patterns

	/**
	 * The name of the state form field containing additional information for a
	 * course request
	 */
	private static final String FORM_ADDITIONAL = "form.additional";

	/** %%% in transition from putting all form variables in state */
	private final static String FORM_TITLE = "form_title";

	private final static String FORM_DESCRIPTION = "form_description";

	private final static String FORM_HONORIFIC = "form_honorific";

	private final static String FORM_INSTITUTION = "form_institution";

	private final static String FORM_SUBJECT = "form_subject";

	private final static String FORM_PHONE = "form_phone";

	private final static String FORM_EMAIL = "form_email";

	private final static String FORM_REUSE = "form_reuse";

	private final static String FORM_RELATED_CLASS = "form_related_class";

	private final static String FORM_RELATED_PROJECT = "form_related_project";

	private final static String FORM_NAME = "form_name";

	private final static String FORM_SHORT_DESCRIPTION = "form_short_description";

	private final static String FORM_ICON_URL = "iconUrl";

	/** site info edit form variables */
	private final static String FORM_SITEINFO_TITLE = "siteinfo_title";

	private final static String FORM_SITEINFO_TERM = "siteinfo_term";

	private final static String FORM_SITEINFO_DESCRIPTION = "siteinfo_description";

	private final static String FORM_SITEINFO_SHORT_DESCRIPTION = "siteinfo_short_description";

	private final static String FORM_SITEINFO_SKIN = "siteinfo_skin";

	private final static String FORM_SITEINFO_INCLUDE = "siteinfo_include";

	private final static String FORM_SITEINFO_ICON_URL = "siteinfo_icon_url";

	private final static String FORM_SITEINFO_CONTACT_NAME = "siteinfo_contact_name";

	private final static String FORM_SITEINFO_CONTACT_EMAIL = "siteinfo_contact_email";

	private final static String FORM_WILL_NOTIFY = "form_will_notify";

	/** Context action */
	private static final String CONTEXT_ACTION = "SiteAction";

	/** The name of the Attribute for display template index */
	private static final String STATE_TEMPLATE_INDEX = "site.templateIndex";

	/** State attribute for state initialization. */
	private static final String STATE_INITIALIZED = "site.initialized";

	/** The action for menu */
	private static final String STATE_ACTION = "site.action";

	/** The user copyright string */
	private static final String STATE_MY_COPYRIGHT = "resources.mycopyright";

	/** The copyright character */
	private static final String COPYRIGHT_SYMBOL = "copyright (c)";

	/** The null/empty string */
	private static final String NULL_STRING = "";

	/** The state attribute alerting user of a sent course request */
	private static final String REQUEST_SENT = "site.request.sent";

	/** The state attributes in the make public vm */
	private static final String STATE_JOINABLE = "state_joinable";

	private static final String STATE_JOINERROLE = "state_joinerRole";

	/** the list of selected user */
	private static final String STATE_SELECTED_USER_LIST = "state_selected_user_list";

	private static final String STATE_SELECTED_PARTICIPANT_ROLES = "state_selected_participant_roles";

	private static final String STATE_SELECTED_PARTICIPANTS = "state_selected_participants";

	private static final String STATE_PARTICIPANT_LIST = "state_participant_list";

	private static final String STATE_ADD_PARTICIPANTS = "state_add_participants";

	/** for changing participant roles */
	private static final String STATE_CHANGEROLE_SAMEROLE = "state_changerole_samerole";

	private static final String STATE_CHANGEROLE_SAMEROLE_ROLE = "state_changerole_samerole_role";

	/** for remove user */
	private static final String STATE_REMOVEABLE_USER_LIST = "state_removeable_user_list";

	private static final String STATE_IMPORT = "state_import";

	private static final String STATE_IMPORT_SITES = "state_import_sites";

	private static final String STATE_IMPORT_SITE_TOOL = "state_import_site_tool";

	/** for navigating between sites in site list */
	private static final String STATE_SITES = "state_sites";

	private static final String STATE_PREV_SITE = "state_prev_site";

	private static final String STATE_NEXT_SITE = "state_next_site";

	/** for course information */
	private final static String STATE_TERM_COURSE_LIST = "state_term_course_list";

	private final static String STATE_TERM_COURSE_HASH = "state_term_course_hash";

	private final static String STATE_TERM_SELECTED = "state_term_selected";

	private final static String STATE_INSTRUCTOR_SELECTED = "state_instructor_selected";

	private final static String STATE_FUTURE_TERM_SELECTED = "state_future_term_selected";

	private final static String STATE_ADD_CLASS_PROVIDER = "state_add_class_provider";

	private final static String STATE_ADD_CLASS_PROVIDER_CHOSEN = "state_add_class_provider_chosen";

	private final static String STATE_ADD_CLASS_MANUAL = "state_add_class_manual";

	private final static String STATE_AUTO_ADD = "state_auto_add";

	private final static String STATE_MANUAL_ADD_COURSE_NUMBER = "state_manual_add_course_number";

	private final static String STATE_MANUAL_ADD_COURSE_FIELDS = "state_manual_add_course_fields";

	public final static String PROP_SITE_REQUEST_COURSE = "site-request-course-sections";

	public final static String SITE_PROVIDER_COURSE_LIST = "site_provider_course_list";

	public final static String SITE_MANUAL_COURSE_LIST = "site_manual_course_list";

	private final static String STATE_SUBJECT_AFFILIATES = "site.subject.affiliates";

	private final static String STATE_ICONS = "icons";

	// site template used to create a UM Grad Tools student site
	public static final String SITE_GTS_TEMPLATE = "!gtstudent";

	// the type used to identify a UM Grad Tools student site
	public static final String SITE_TYPE_GRADTOOLS_STUDENT = "GradToolsStudent";

	// list of UM Grad Tools site types for editing
	public static final String GRADTOOLS_SITE_TYPES = "gradtools_site_types";

	public static final String SITE_DUPLICATED = "site_duplicated";

	public static final String SITE_DUPLICATED_NAME = "site_duplicated_named";

	// used for site creation wizard title
	public static final String SITE_CREATE_TOTAL_STEPS = "site_create_total_steps";

	public static final String SITE_CREATE_CURRENT_STEP = "site_create_current_step";

	// types of site where site view roster permission is editable
	public static final String EDIT_VIEW_ROSTER_SITE_TYPE = "edit_view_roster_site_type";

	// htripath : for import material from file - classic import
	private static final String ALL_ZIP_IMPORT_SITES = "allzipImports";

	private static final String FINAL_ZIP_IMPORT_SITES = "finalzipImports";

	private static final String DIRECT_ZIP_IMPORT_SITES = "directzipImports";

	private static final String CLASSIC_ZIP_FILE_NAME = "classicZipFileName";

	private static final String SESSION_CONTEXT_ID = "sessionContextId";

	// page size for worksite setup tool
	private static final String STATE_PAGESIZE_SITESETUP = "state_pagesize_sitesetup";

	// page size for site info tool
	private static final String STATE_PAGESIZE_SITEINFO = "state_pagesize_siteinfo";

	// group info
	private static final String STATE_GROUP_INSTANCE_ID = "state_group_instance_id";

	private static final String STATE_GROUP_TITLE = "state_group_title";

	private static final String STATE_GROUP_DESCRIPTION = "state_group_description";

	private static final String STATE_GROUP_MEMBERS = "state_group_members";

	private static final String STATE_GROUP_REMOVE = "state_group_remove";

	private static final String GROUP_PROP_WSETUP_CREATED = "group_prop_wsetup_created";

	private static final String IMPORT_DATA_SOURCE = "import_data_source";

	private static final String EMAIL_CHAR = "@";

	// Special tool id for Home page
	private static final String HOME_TOOL_ID = "home";

	private static final String STATE_CM_LEVELS = "site.cm.levels";

	private static final String STATE_CM_LEVEL_SELECTIONS = "site.cm.level.selections";

	private static final String STATE_CM_SELECTED_SECTION = "site.cm.selectedSection";

	private static final String STATE_CM_REQUESTED_SECTIONS = "site.cm.requested";
	
	private static final String STATE_CM_SELECTED_SECTIONS = "site.cm.selectedSections";

	private static final String STATE_PROVIDER_SECTION_LIST = "site_provider_section_list";

	private static final String STATE_CM_CURRENT_USERID = "site_cm_current_userId";

	private static final String STATE_CM_AUTHORIZER_LIST = "site_cm_authorizer_list";

	private static final String STATE_CM_AUTHORIZER_SECTIONS = "site_cm_authorizer_sections";

	private String cmSubjectCategory;

	private boolean warnedNoSubjectCategory = false;

	// the string marks the protocol part in url
	private static final String PROTOCOL_STRING = "://";

	private static final String TOOL_ID_SUMMARY_CALENDAR = "sakai.summary.calendar";
	
	// the string for course site type
	private static final String STATE_COURSE_SITE_TYPE = "state_course_site_type";
	
	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet,
			JetspeedRunData rundata) {
		super.initState(state, portlet, rundata);

		// store current userId in state
		User user = UserDirectoryService.getCurrentUser();
		String userId = user.getEid();
		state.setAttribute(STATE_CM_CURRENT_USERID, userId);
		PortletConfig config = portlet.getPortletConfig();

		// types of sites that can either be public or private
		String changeableTypes = StringUtil.trimToNull(config
				.getInitParameter("publicChangeableSiteTypes"));
		if (state.getAttribute(STATE_PUBLIC_CHANGEABLE_SITE_TYPES) == null) {
			if (changeableTypes != null) {
				state
						.setAttribute(STATE_PUBLIC_CHANGEABLE_SITE_TYPES,
								new ArrayList(Arrays.asList(changeableTypes
										.split(","))));
			} else {
				state.setAttribute(STATE_PUBLIC_CHANGEABLE_SITE_TYPES,
						new Vector());
			}
		}

		// type of sites that are always public
		String publicTypes = StringUtil.trimToNull(config
				.getInitParameter("publicSiteTypes"));
		if (state.getAttribute(STATE_PUBLIC_SITE_TYPES) == null) {
			if (publicTypes != null) {
				state.setAttribute(STATE_PUBLIC_SITE_TYPES, new ArrayList(
						Arrays.asList(publicTypes.split(","))));
			} else {
				state.setAttribute(STATE_PUBLIC_SITE_TYPES, new Vector());
			}
		}

		// types of sites that are always private
		String privateTypes = StringUtil.trimToNull(config
				.getInitParameter("privateSiteTypes"));
		if (state.getAttribute(STATE_PRIVATE_SITE_TYPES) == null) {
			if (privateTypes != null) {
				state.setAttribute(STATE_PRIVATE_SITE_TYPES, new ArrayList(
						Arrays.asList(privateTypes.split(","))));
			} else {
				state.setAttribute(STATE_PRIVATE_SITE_TYPES, new Vector());
			}
		}

		// default site type
		String defaultType = StringUtil.trimToNull(config
				.getInitParameter("defaultSiteType"));
		if (state.getAttribute(STATE_DEFAULT_SITE_TYPE) == null) {
			if (defaultType != null) {
				state.setAttribute(STATE_DEFAULT_SITE_TYPE, defaultType);
			} else {
				state.setAttribute(STATE_PRIVATE_SITE_TYPES, new Vector());
			}
		}

		// certain type(s) of site cannot get its "joinable" option set
		if (state.getAttribute(STATE_DISABLE_JOINABLE_SITE_TYPE) == null) {
			if (ServerConfigurationService
					.getStrings("wsetup.disable.joinable") != null) {
				state.setAttribute(STATE_DISABLE_JOINABLE_SITE_TYPE,
						new ArrayList(Arrays.asList(ServerConfigurationService
								.getStrings("wsetup.disable.joinable"))));
			} else {
				state.setAttribute(STATE_DISABLE_JOINABLE_SITE_TYPE,
						new Vector());
			}
		}
		
		// course site type
		if (state.getAttribute(STATE_COURSE_SITE_TYPE) == null)
		{
			state.setAttribute(STATE_COURSE_SITE_TYPE, ServerConfigurationService.getString("courseSiteType", "course"));
		}

		if (state.getAttribute(STATE_TOP_PAGE_MESSAGE) == null) {
			state.setAttribute(STATE_TOP_PAGE_MESSAGE, new Integer(0));
		}

		// skins if any
		if (state.getAttribute(STATE_ICONS) == null) {
			setupIcons(state);
		}

		if (state.getAttribute(GRADTOOLS_SITE_TYPES) == null) {
			List gradToolsSiteTypes = new Vector();
			if (ServerConfigurationService.getStrings("gradToolsSiteType") != null) {
				gradToolsSiteTypes = new ArrayList(Arrays
						.asList(ServerConfigurationService
								.getStrings("gradToolsSiteType")));
			}
			state.setAttribute(GRADTOOLS_SITE_TYPES, gradToolsSiteTypes);
		}

		if (state.getAttribute(EDIT_VIEW_ROSTER_SITE_TYPE) == null) {
			List siteTypes = new Vector();
			if (ServerConfigurationService.getStrings("editViewRosterSiteType") != null) {
				siteTypes = new ArrayList(Arrays
						.asList(ServerConfigurationService
								.getStrings("editViewRosterSiteType")));
			}
			state.setAttribute(EDIT_VIEW_ROSTER_SITE_TYPE, siteTypes);
		}

		// get site tool mode from tool registry
		String site_mode = portlet.getPortletConfig().getInitParameter(
				STATE_SITE_MODE);
		state.setAttribute(STATE_SITE_MODE, site_mode);


		
	} // initState
	
	

	/**
	 * cleanState removes the current site instance and it's properties from
	 * state
	 */
	private void cleanState(SessionState state) {
		state.removeAttribute(STATE_SITE_INSTANCE_ID);
		state.removeAttribute(STATE_SITE_INFO);
		state.removeAttribute(STATE_SITE_TYPE);
		state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME);
		state.removeAttribute(STATE_TOOL_EMAIL_ADDRESS);
		state.removeAttribute(STATE_TOOL_HOME_SELECTED);
		state.removeAttribute(STATE_SELECTED_USER_LIST);
		state.removeAttribute(STATE_JOINABLE);
		state.removeAttribute(STATE_JOINERROLE);
		state.removeAttribute(STATE_NEWS_TITLES);
		state.removeAttribute(STATE_NEWS_URLS);
		state.removeAttribute(STATE_WEB_CONTENT_TITLES);
		state.removeAttribute(STATE_WEB_CONTENT_URLS);
		state.removeAttribute(STATE_SITE_QUEST_UNIQNAME);
		state.removeAttribute(STATE_IMPORT);
		state.removeAttribute(STATE_IMPORT_SITES);
		state.removeAttribute(STATE_IMPORT_SITE_TOOL);

		// remove those state attributes related to course site creation
		state.removeAttribute(STATE_TERM_COURSE_LIST);
		state.removeAttribute(STATE_TERM_COURSE_HASH);
		state.removeAttribute(STATE_TERM_SELECTED);
		state.removeAttribute(STATE_INSTRUCTOR_SELECTED);
		state.removeAttribute(STATE_FUTURE_TERM_SELECTED);
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER);
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		state.removeAttribute(STATE_ADD_CLASS_MANUAL);
		state.removeAttribute(STATE_AUTO_ADD);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_NUMBER);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
		state.removeAttribute(SITE_CREATE_TOTAL_STEPS);
		state.removeAttribute(SITE_CREATE_CURRENT_STEP);
		state.removeAttribute(STATE_PROVIDER_SECTION_LIST);
		state.removeAttribute(STATE_CM_LEVELS);
		state.removeAttribute(STATE_CM_LEVEL_SELECTIONS);
		state.removeAttribute(STATE_CM_SELECTED_SECTION);
		state.removeAttribute(STATE_CM_REQUESTED_SECTIONS);
		state.removeAttribute(STATE_CM_CURRENT_USERID);
		state.removeAttribute(STATE_CM_AUTHORIZER_LIST);
		state.removeAttribute(STATE_CM_AUTHORIZER_SECTIONS);
		state.removeAttribute(FORM_ADDITIONAL); // don't we need to clena this
		// too? -daisyf

	} // cleanState

	/**
	 * Fire up the permissions editor
	 */
	public void doPermissions(RunData data, Context context) {
		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.permissions.helper");

		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String contextString = ToolManager.getCurrentPlacement().getContext();
		String siteRef = SiteService.siteReference(contextString);

		// if it is in Worksite setup tool, pass the selected site's reference
		if (state.getAttribute(STATE_SITE_MODE) != null
				&& ((String) state.getAttribute(STATE_SITE_MODE))
						.equals(SITE_MODE_SITESETUP)) {
			if (state.getAttribute(STATE_SITE_INSTANCE_ID) != null) {
				Site s = getStateSite(state);
				if (s != null) {
					siteRef = s.getReference();
				}
			}
		}

		// setup for editing the permissions of the site for this tool, using
		// the roles of this site, too
		state.setAttribute(PermissionsHelper.TARGET_REF, siteRef);

		// ... with this description
		state.setAttribute(PermissionsHelper.DESCRIPTION, rb
				.getString("setperfor")
				+ " " + SiteService.getSiteDisplay(contextString));

		// ... showing only locks that are prpefixed with this
		state.setAttribute(PermissionsHelper.PREFIX, "site.");

	} // doPermissions

	/**
	 * Build the context for normal display
	 */
	public String buildMainPanelContext(VelocityPortlet portlet,
			Context context, RunData data, SessionState state) {
		context.put("tlang", rb);
		// TODO: what is all this doing? if we are in helper mode, we are
		// already setup and don't get called here now -ggolden
		/*
		 * String helperMode = (String)
		 * state.getAttribute(PermissionsAction.STATE_MODE); if (helperMode !=
		 * null) { Site site = getStateSite(state); if (site != null) { if
		 * (site.getType() != null && ((List)
		 * state.getAttribute(EDIT_VIEW_ROSTER_SITE_TYPE)).contains(site.getType())) {
		 * context.put("editViewRoster", Boolean.TRUE); } else {
		 * context.put("editViewRoster", Boolean.FALSE); } } else {
		 * context.put("editViewRoster", Boolean.FALSE); } // for new, don't
		 * show site.del in Permission page context.put("hiddenLock",
		 * "site.del");
		 * 
		 * String template = PermissionsAction.buildHelperContext(portlet,
		 * context, data, state); if (template == null) { addAlert(state,
		 * rb.getString("theisa")); } else { return template; } }
		 */

		String template = null;
		context.put("action", CONTEXT_ACTION);

		// updatePortlet(state, portlet, data);
		if (state.getAttribute(STATE_INITIALIZED) == null) {
			init(portlet, data, state);
		}
		int index = Integer.valueOf(
				(String) state.getAttribute(STATE_TEMPLATE_INDEX)).intValue();
		template = buildContextForTemplate(index, portlet, context, data, state);
		return template;

	} // buildMainPanelContext

	/**
	 * Build the context for each template using template_index parameter passed
	 * in a form hidden field. Each case is associated with a template. (Not all
	 * templates implemented). See String[] TEMPLATES.
	 * 
	 * @param index
	 *            is the number contained in the template's template_index
	 */

	private String buildContextForTemplate(int index, VelocityPortlet portlet,
			Context context, RunData data, SessionState state) {
		String realmId = "";
		String site_type = "";
		String sortedBy = "";
		String sortedAsc = "";
		ParameterParser params = data.getParameters();
		context.put("tlang", rb);
		context.put("alertMessage", state.getAttribute(STATE_MESSAGE));
		
		
		// If cleanState() has removed SiteInfo, get a new instance into state
		SiteInfo siteInfo = new SiteInfo();
		if (state.getAttribute(STATE_SITE_INFO) != null) {
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
		} else {
			state.setAttribute(STATE_SITE_INFO, siteInfo);
		}
		// Lists used in more than one template

		// Access
		List roles = new Vector();

		// the hashtables for News and Web Content tools
		Hashtable newsTitles = new Hashtable();
		Hashtable newsUrls = new Hashtable();
		Hashtable wcTitles = new Hashtable();
		Hashtable wcUrls = new Hashtable();

		List toolRegistrationList = new Vector();
		List toolRegistrationSelectedList = new Vector();

		ResourceProperties siteProperties = null;

		// for showing site creation steps
		if (state.getAttribute(SITE_CREATE_TOTAL_STEPS) != null) {
			context.put("totalSteps", state
					.getAttribute(SITE_CREATE_TOTAL_STEPS));
		}
		if (state.getAttribute(SITE_CREATE_CURRENT_STEP) != null) {
			context.put("step", state.getAttribute(SITE_CREATE_CURRENT_STEP));
		}

		String hasGradSites = ServerConfigurationService.getString(
				"withDissertation", Boolean.FALSE.toString());
		
		context.put("cms", cms);

		// course site type
		context.put("courseSiteType", state.getAttribute(STATE_COURSE_SITE_TYPE));
		
		//can the user create course sites?
		context.put(STATE_SITE_ADD_COURSE, SiteService.allowAddCourseSite());

		
		Site site = getStateSite(state);

		switch (index) {
		case 0:
			/*
			 * buildContextForTemplate chef_site-list.vm
			 * 
			 */
			// site types
			List sTypes = (List) state.getAttribute(STATE_SITE_TYPES);

			// make sure auto-updates are enabled
			Hashtable views = new Hashtable();
			if (SecurityService.isSuperUser()) {
				views.put(rb.getString("java.allmy"), rb
						.getString("java.allmy"));
				views.put(rb.getString("java.my") + " "
						+ rb.getString("java.sites"), rb.getString("java.my"));
				for (int sTypeIndex = 0; sTypeIndex < sTypes.size(); sTypeIndex++) {
					String type = (String) sTypes.get(sTypeIndex);
					views.put(type + " " + rb.getString("java.sites"), type);
				}
				if (hasGradSites.equalsIgnoreCase("true")) {
					views.put(rb.getString("java.gradtools") + " "
							+ rb.getString("java.sites"), rb
							.getString("java.gradtools"));
				}
				if (state.getAttribute(STATE_VIEW_SELECTED) == null) {
					state.setAttribute(STATE_VIEW_SELECTED, rb
							.getString("java.allmy"));
				}
				context.put("superUser", Boolean.TRUE);
			} else {
				context.put("superUser", Boolean.FALSE);
				views.put(rb.getString("java.allmy"), rb
						.getString("java.allmy"));

				// if there is a GradToolsStudent choice inside
				boolean remove = false;
				if (hasGradSites.equalsIgnoreCase("true")) {
					try {
						// the Grad Tools site option is only presented to
						// GradTools Candidates
						String userId = StringUtil.trimToZero(SessionManager
								.getCurrentSessionUserId());

						// am I a grad student?
						if (!isGradToolsCandidate(userId)) {
							// not a gradstudent
							remove = true;
						}
					} catch (Exception e) {
						remove = true;
					}
				} else {
					// not support for dissertation sites
					remove = true;
				}
				// do not show this site type in views
				// sTypes.remove(new String(SITE_TYPE_GRADTOOLS_STUDENT));

				for (int sTypeIndex = 0; sTypeIndex < sTypes.size(); sTypeIndex++) {
					String type = (String) sTypes.get(sTypeIndex);
					if (!type.equals(SITE_TYPE_GRADTOOLS_STUDENT)) {
						views
								.put(type + " " + rb.getString("java.sites"),
										type);
					}
				}
				if (!remove) {
					views.put(rb.getString("java.gradtools") + " "
							+ rb.getString("java.sites"), rb
							.getString("java.gradtools"));
				}

				// default view
				if (state.getAttribute(STATE_VIEW_SELECTED) == null) {
					state.setAttribute(STATE_VIEW_SELECTED, rb
							.getString("java.allmy"));
				}
			}
			context.put("views", views);

			if (state.getAttribute(STATE_VIEW_SELECTED) != null) {
				context.put("viewSelected", (String) state
						.getAttribute(STATE_VIEW_SELECTED));
			}

			String search = (String) state.getAttribute(STATE_SEARCH);
			context.put("search_term", search);

			sortedBy = (String) state.getAttribute(SORTED_BY);
			if (sortedBy == null) {
				state.setAttribute(SORTED_BY, SortType.TITLE_ASC.toString());
				sortedBy = SortType.TITLE_ASC.toString();
			}

			sortedAsc = (String) state.getAttribute(SORTED_ASC);
			if (sortedAsc == null) {
				sortedAsc = Boolean.TRUE.toString();
				state.setAttribute(SORTED_ASC, sortedAsc);
			}
			if (sortedBy != null)
				context.put("currentSortedBy", sortedBy);
			if (sortedAsc != null)
				context.put("currentSortAsc", sortedAsc);

			String portalUrl = ServerConfigurationService.getPortalUrl();
			context.put("portalUrl", portalUrl);

			List sites = prepPage(state);
			state.setAttribute(STATE_SITES, sites);
			context.put("sites", sites);

			context.put("totalPageNumber", new Integer(totalPageNumber(state)));
			context.put("searchString", state.getAttribute(STATE_SEARCH));
			context.put("form_search", FORM_SEARCH);
			context.put("formPageNumber", FORM_PAGE_NUMBER);
			context.put("prev_page_exists", state
					.getAttribute(STATE_PREV_PAGE_EXISTS));
			context.put("next_page_exists", state
					.getAttribute(STATE_NEXT_PAGE_EXISTS));
			context.put("current_page", state.getAttribute(STATE_CURRENT_PAGE));

			// put the service in the context (used for allow update calls on
			// each site)
			context.put("service", SiteService.getInstance());
			context.put("sortby_title", SortType.TITLE_ASC.toString());
			context.put("sortby_type", SortType.TYPE_ASC.toString());
			context.put("sortby_createdby", SortType.CREATED_BY_ASC.toString());
			context.put("sortby_publish", SortType.PUBLISHED_ASC.toString());
			context.put("sortby_createdon", SortType.CREATED_ON_ASC.toString());

			// top menu bar
			Menu bar = new MenuImpl(portlet, data, (String) state
					.getAttribute(STATE_ACTION));
			if (SiteService.allowAddSite(null)) {
				bar.add(new MenuEntry(rb.getString("java.new"), "doNew_site"));
			}
			bar.add(new MenuEntry(rb.getString("java.revise"), null, true,
					MenuItem.CHECKED_NA, "doGet_site", "sitesForm"));
			bar.add(new MenuEntry(rb.getString("java.delete"), null, true,
					MenuItem.CHECKED_NA, "doMenu_site_delete", "sitesForm"));
			context.put("menu", bar);
			// default to be no pageing
			context.put("paged", Boolean.FALSE);

			Menu bar2 = new MenuImpl(portlet, data, (String) state
					.getAttribute(STATE_ACTION));

			// add the search commands
			addSearchMenus(bar2, state);
			context.put("menu2", bar2);

			pagingInfoToContext(state, context);
			return (String) getContext(data).get("template") + TEMPLATE[0];
		case 1:
			/*
			 * buildContextForTemplate chef_site-type.vm
			 * 
			 */
			if (hasGradSites.equalsIgnoreCase("true")) {
				context.put("withDissertation", Boolean.TRUE);
				try {
					// the Grad Tools site option is only presented to UM grad
					// students
					String userId = StringUtil.trimToZero(SessionManager
							.getCurrentSessionUserId());

					// am I a UM grad student?
					Boolean isGradStudent = new Boolean(
							isGradToolsCandidate(userId));
					context.put("isGradStudent", isGradStudent);

					// if I am a UM grad student, do I already have a Grad Tools
					// site?
					boolean noGradToolsSite = true;
					if (hasGradToolsStudentSite(userId))
						noGradToolsSite = false;
					context
							.put("noGradToolsSite",
									new Boolean(noGradToolsSite));
				} catch (Exception e) {
					if (Log.isWarnEnabled()) {
						M_log.warn("buildContextForTemplate chef_site-type.vm "
								+ e);
					}
				}
			} else {
				context.put("withDissertation", Boolean.FALSE);
			}

			List types = (List) state.getAttribute(STATE_SITE_TYPES);
			context.put("siteTypes", types);

			// put selected/default site type into context
			if (siteInfo.site_type != null && siteInfo.site_type.length() > 0) {
				context.put("typeSelected", siteInfo.site_type);
			} else if (types.size() > 0) {
				context.put("typeSelected", types.get(0));
			}
			setTermListForContext(context, state, true); // true => only
			// upcoming terms
			setSelectedTermForContext(context, state, STATE_TERM_SELECTED);
			return (String) getContext(data).get("template") + TEMPLATE[1];

		case 2:
			/*
			 * buildContextForTemplate chef_site-newSiteInformation.vm
			 * 
			 */
			context.put("siteTypes", state.getAttribute(STATE_SITE_TYPES));
			String siteType = (String) state.getAttribute(STATE_SITE_TYPE);

			context.put("type", siteType);

			if (siteType.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("isProjectSite", Boolean.FALSE);

				if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null) {
					context.put("selectedProviderCourse", state
							.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN));
				}

				List<SectionObject> cmRequestedList = (List<SectionObject>) state
						.getAttribute(STATE_CM_REQUESTED_SECTIONS);

				if (cmRequestedList != null) {
					context.put("cmRequestedSections", cmRequestedList);
					context.put("back", "53");
				}

				List<SectionObject> cmAuthorizerSectionList = (List<SectionObject>) state
						.getAttribute(STATE_CM_AUTHORIZER_SECTIONS);
				if (cmAuthorizerSectionList != null) {
					context
							.put("cmAuthorizerSections",
									cmAuthorizerSectionList);
					context.put("back", "36");
				}

				if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
					int number = ((Integer) state
							.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER))
							.intValue();
					context.put("manualAddNumber", new Integer(number - 1));
					context.put("manualAddFields", state
							.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
					context.put("back", "37");
				} else {
					if (courseManagementIsImplemented()) {
						context.put("back", "36");
					} else {
						context.put("back", "0");
						context.put("template-index", "37");
					}
				}

				context.put("skins", state.getAttribute(STATE_ICONS));
				if (StringUtil.trimToNull(siteInfo.getIconUrl()) != null) {
					context.put("selectedIcon", siteInfo.getIconUrl());
				}
			} else {
				context.put("isCourseSite", Boolean.FALSE);
				if (siteType.equalsIgnoreCase("project")) {
					context.put("isProjectSite", Boolean.TRUE);
				}

				if (StringUtil.trimToNull(siteInfo.iconUrl) != null) {
					context.put(FORM_ICON_URL, siteInfo.iconUrl);
				}

				context.put("back", "1");
			}

			context.put(FORM_TITLE, siteInfo.title);
			context.put(FORM_SHORT_DESCRIPTION, siteInfo.short_description);
			context.put(FORM_DESCRIPTION, siteInfo.description);

			// defalt the site contact person to the site creator
			if (siteInfo.site_contact_name.equals(NULL_STRING)
					&& siteInfo.site_contact_email.equals(NULL_STRING)) {
				User user = UserDirectoryService.getCurrentUser();
				siteInfo.site_contact_name = user.getDisplayName();
				siteInfo.site_contact_email = user.getEmail();
			}
			context.put("form_site_contact_name", siteInfo.site_contact_name);
			context.put("form_site_contact_email", siteInfo.site_contact_email);

			// those manual inputs
			context.put("form_requiredFields", sectionFieldProvider
					.getRequiredFields());
			context.put("fieldValues", state
					.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
			return (String) getContext(data).get("template") + TEMPLATE[2];
		case 3:
			/*
			 * buildContextForTemplate chef_site-newSiteFeatures.vm
			 * 
			 */
			siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (siteType != null && siteType.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("isProjectSite", Boolean.FALSE);
			} else {
				context.put("isCourseSite", Boolean.FALSE);
				if (siteType.equalsIgnoreCase("project")) {
					context.put("isProjectSite", Boolean.TRUE);
				}
			}
			context.put("defaultTools", ServerConfigurationService
					.getToolsRequired(siteType));

			toolRegistrationSelectedList = (List) state
					.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);

			// If this is the first time through, check for tools
			// which should be selected by default.
			List defaultSelectedTools = ServerConfigurationService
					.getDefaultTools(siteType);
			if (toolRegistrationSelectedList == null) {
				toolRegistrationSelectedList = new Vector(defaultSelectedTools);
			}

			context.put(STATE_TOOL_REGISTRATION_SELECTED_LIST,
					toolRegistrationSelectedList); // String toolId's
			context.put(STATE_TOOL_REGISTRATION_LIST, state
					.getAttribute(STATE_TOOL_REGISTRATION_LIST)); // %%% use
			// ToolRegistrations
			// for
			// template
			// list
			context
					.put("emailId", state
							.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			context.put("serverName", ServerConfigurationService
					.getServerName());

			// The "Home" tool checkbox needs special treatment to be selected
			// by
			// default.
			Boolean checkHome = (Boolean) state
					.getAttribute(STATE_TOOL_HOME_SELECTED);
			if (checkHome == null) {
				if ((defaultSelectedTools != null)
						&& defaultSelectedTools.contains(HOME_TOOL_ID)) {
					checkHome = Boolean.TRUE;
				}
			}
			context.put("check_home", checkHome);

			// titles for news tools
			context.put("newsTitles", state.getAttribute(STATE_NEWS_TITLES));
			// titles for web content tools
			context.put("wcTitles", state
					.getAttribute(STATE_WEB_CONTENT_TITLES));
			// urls for news tools
			context.put("newsUrls", state.getAttribute(STATE_NEWS_URLS));
			// urls for web content tools
			context.put("wcUrls", state.getAttribute(STATE_WEB_CONTENT_URLS));
			context.put("sites", SiteService.getSites(
					org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
					null, null, null, SortType.TITLE_ASC, null));
			context.put("import", state.getAttribute(STATE_IMPORT));
			context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));

			return (String) getContext(data).get("template") + TEMPLATE[3];
		case 4:
			/*
			 * buildContextForTemplate chef_site-addRemoveFeatures.vm
			 * 
			 */
			context.put("SiteTitle", site.getTitle());
			String type = (String) state.getAttribute(STATE_SITE_TYPE);
			context.put("defaultTools", ServerConfigurationService
					.getToolsRequired(type));

			boolean myworkspace_site = false;
			// Put up tool lists filtered by category
			List siteTypes = (List) state.getAttribute(STATE_SITE_TYPES);
			if (siteTypes.contains(type)) {
				myworkspace_site = false;
			}

			if (SiteService.isUserSite(site.getId())
					|| (type != null && type.equalsIgnoreCase("myworkspace"))) {
				myworkspace_site = true;
				type = "myworkspace";
			}

			context.put("myworkspace_site", new Boolean(myworkspace_site));

			context.put(STATE_TOOL_REGISTRATION_SELECTED_LIST, state
					.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST));

			// titles for news tools
			context.put("newsTitles", state.getAttribute(STATE_NEWS_TITLES));
			// titles for web content tools
			context.put("wcTitles", state
					.getAttribute(STATE_WEB_CONTENT_TITLES));
			// urls for news tools
			context.put("newsUrls", state.getAttribute(STATE_NEWS_URLS));
			// urls for web content tools
			context.put("wcUrls", state.getAttribute(STATE_WEB_CONTENT_URLS));

			context.put(STATE_TOOL_REGISTRATION_LIST, state
					.getAttribute(STATE_TOOL_REGISTRATION_LIST));
			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));

			// get the email alias when an Email Archive tool has been selected
			String channelReference = mailArchiveChannelReference(site.getId());
			List aliases = AliasService.getAliases(channelReference, 1, 1);
			if (aliases.size() > 0) {
				state.setAttribute(STATE_TOOL_EMAIL_ADDRESS, ((Alias) aliases
						.get(0)).getId());
			} else {
				state.removeAttribute(STATE_TOOL_EMAIL_ADDRESS);
			}
			if (state.getAttribute(STATE_TOOL_EMAIL_ADDRESS) != null) {
				context.put("emailId", state
						.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			}
			context.put("serverName", ServerConfigurationService
					.getServerName());

			context.put("backIndex", "12");

			return (String) getContext(data).get("template") + TEMPLATE[4];
		case 5:
			/*
			 * buildContextForTemplate chef_site-addParticipant.vm
			 * 
			 */
			context.put("title", site.getTitle());
			roles = getRoles(state);
			context.put("roles", roles);
			
			siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			context.put("isCourseSite", siteType.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))?Boolean.TRUE:Boolean.FALSE);
			
			// Note that (for now) these strings are in both sakai.properties
			// and sitesetupgeneric.properties
			context.put("officialAccountSectionTitle", ServerConfigurationService
					.getString("officialAccountSectionTitle"));
			context.put("officialAccountName", ServerConfigurationService
					.getString("officialAccountName"));
			context.put("officialAccountLabel", ServerConfigurationService
					.getString("officialAccountLabel"));
			context.put("nonOfficialAccountSectionTitle", ServerConfigurationService
					.getString("nonOfficialAccountSectionTitle"));
			context.put("nonOfficialAccountName", ServerConfigurationService
					.getString("nonOfficialAccountName"));
			context.put("nonOfficialAccountLabel", ServerConfigurationService
					.getString("nonOfficialAccountLabel"));

			if (state.getAttribute("officialAccountValue") != null) {
				context.put("officialAccountValue", (String) state
						.getAttribute("officialAccountValue"));
			}
			if (state.getAttribute("nonOfficialAccountValue") != null) {
				context.put("nonOfficialAccountValue", (String) state
						.getAttribute("nonOfficialAccountValue"));
			}

			if (state.getAttribute("form_same_role") != null) {
				context.put("form_same_role", ((Boolean) state
						.getAttribute("form_same_role")).toString());
			} else {
				context.put("form_same_role", Boolean.TRUE.toString());
			}
			context.put("backIndex", "12");
			return (String) getContext(data).get("template") + TEMPLATE[5];
		case 8:
			/*
			 * buildContextForTemplate chef_site-siteDeleteConfirm.vm
			 * 
			 */
			String site_title = NULL_STRING;
			String[] removals = (String[]) state
					.getAttribute(STATE_SITE_REMOVALS);
			List remove = new Vector();
			String user = SessionManager.getCurrentSessionUserId();
			String workspace = SiteService.getUserSiteId(user);
			if (removals != null && removals.length != 0) {
				for (int i = 0; i < removals.length; i++) {
					String id = (String) removals[i];
					if (!(id.equals(workspace))) {
						try {
							site_title = SiteService.getSite(id).getTitle();
						} catch (IdUnusedException e) {
							M_log
									.warn("SiteAction.doSite_delete_confirmed - IdUnusedException "
											+ id);
							addAlert(state, rb.getString("java.sitewith") + " "
									+ id + " " + rb.getString("java.couldnt")
									+ " ");
						}
						if (SiteService.allowRemoveSite(id)) {
							try {
								Site removeSite = SiteService.getSite(id);
								remove.add(removeSite);
							} catch (IdUnusedException e) {
								M_log
										.warn("SiteAction.buildContextForTemplate chef_site-siteDeleteConfirm.vm: IdUnusedException");
							}
						} else {
							addAlert(state, site_title + " "
									+ rb.getString("java.couldntdel") + " ");
						}
					} else {
						addAlert(state, rb.getString("java.yourwork"));
					}
				}
				if (remove.size() == 0) {
					addAlert(state, rb.getString("java.click"));
				}
			}
			context.put("removals", remove);
			return (String) getContext(data).get("template") + TEMPLATE[8];
		case 10:
			/*
			 * buildContextForTemplate chef_site-newSiteConfirm.vm
			 * 
			 */
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (siteType.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("isProjectSite", Boolean.FALSE);
				if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null) {
					context.put("selectedProviderCourse", state
							.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN));
				}
				if (state.getAttribute(STATE_CM_AUTHORIZER_SECTIONS) != null) {
					context.put("selectedAuthorizerCourse", state
							.getAttribute(STATE_CM_AUTHORIZER_SECTIONS));
				}
				if (state.getAttribute(STATE_CM_REQUESTED_SECTIONS) != null) {
					context.put("selectedRequestedCourse", state
							.getAttribute(STATE_CM_REQUESTED_SECTIONS));
				}
				if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
					int number = ((Integer) state
							.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER))
							.intValue();
					context.put("manualAddNumber", new Integer(number - 1));
					context.put("manualAddFields", state
							.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
				}

				context.put("skins", state.getAttribute(STATE_ICONS));
				if (StringUtil.trimToNull(siteInfo.getIconUrl()) != null) {
					context.put("selectedIcon", siteInfo.getIconUrl());
				}
			} else {
				context.put("isCourseSite", Boolean.FALSE);
				if (siteType != null && siteType.equalsIgnoreCase("project")) {
					context.put("isProjectSite", Boolean.TRUE);
				}

				if (StringUtil.trimToNull(siteInfo.iconUrl) != null) {
					context.put("iconUrl", siteInfo.iconUrl);
				}
			}
			context.put("title", siteInfo.title);
			context.put("description", siteInfo.description);
			context.put("short_description", siteInfo.short_description);
			context.put("siteContactName", siteInfo.site_contact_name);
			context.put("siteContactEmail", siteInfo.site_contact_email);
			siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			toolRegistrationSelectedList = (List) state
					.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			context.put(STATE_TOOL_REGISTRATION_SELECTED_LIST,
					toolRegistrationSelectedList); // String toolId's
			context.put(STATE_TOOL_REGISTRATION_LIST, state
					.getAttribute(STATE_TOOL_REGISTRATION_LIST)); // %%% use
			// Tool
			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));
			context
					.put("emailId", state
							.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			context.put("serverName", ServerConfigurationService
					.getServerName());
			context.put("include", new Boolean(siteInfo.include));
			context.put("published", new Boolean(siteInfo.published));
			context.put("joinable", new Boolean(siteInfo.joinable));
			context.put("joinerRole", siteInfo.joinerRole);
			context.put("newsTitles", (Hashtable) state
					.getAttribute(STATE_NEWS_TITLES));
			context.put("wcTitles", (Hashtable) state
					.getAttribute(STATE_WEB_CONTENT_TITLES));

			// back to edit access page
			context.put("back", "18");

			context.put("importSiteTools", state
					.getAttribute(STATE_IMPORT_SITE_TOOL));
			context.put("siteService", SiteService.getInstance());

			// those manual inputs
			context.put("form_requiredFields", sectionFieldProvider
					.getRequiredFields());
			context.put("fieldValues", state
					.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));

			return (String) getContext(data).get("template") + TEMPLATE[10];
		case 12:
			/*
			 * buildContextForTemplate chef_site-siteInfo-list.vm
			 * 
			 */
			context.put("userDirectoryService", UserDirectoryService
					.getInstance());
			try {
				siteProperties = site.getProperties();
				siteType = site.getType();
				if (siteType != null) {
					state.setAttribute(STATE_SITE_TYPE, siteType);
				}

				boolean isMyWorkspace = false;
				if (SiteService.isUserSite(site.getId())) {
					if (SiteService.getSiteUserId(site.getId()).equals(
							SessionManager.getCurrentSessionUserId())) {
						isMyWorkspace = true;
						context.put("siteUserId", SiteService
								.getSiteUserId(site.getId()));
					}
				}
				context.put("isMyWorkspace", Boolean.valueOf(isMyWorkspace));

				String siteId = site.getId();
				if (state.getAttribute(STATE_ICONS) != null) {
					List skins = (List) state.getAttribute(STATE_ICONS);
					for (int i = 0; i < skins.size(); i++) {
						MyIcon s = (MyIcon) skins.get(i);
						if (!StringUtil
								.different(s.getUrl(), site.getIconUrl())) {
							context.put("siteUnit", s.getName());
							break;
						}
					}
				}
				context.put("siteIcon", site.getIconUrl());
				context.put("siteTitle", site.getTitle());
				context.put("siteDescription", site.getDescription());
				context.put("siteJoinable", new Boolean(site.isJoinable()));

				if (site.isPublished()) {
					context.put("published", Boolean.TRUE);
				} else {
					context.put("published", Boolean.FALSE);
					context.put("owner", site.getCreatedBy().getSortName());
				}
				Time creationTime = site.getCreatedTime();
				if (creationTime != null) {
					context.put("siteCreationDate", creationTime
							.toStringLocalFull());
				}
				boolean allowUpdateSite = SiteService.allowUpdateSite(siteId);
				context.put("allowUpdate", Boolean.valueOf(allowUpdateSite));

				boolean allowUpdateGroupMembership = SiteService
						.allowUpdateGroupMembership(siteId);
				context.put("allowUpdateGroupMembership", Boolean
						.valueOf(allowUpdateGroupMembership));

				boolean allowUpdateSiteMembership = SiteService
						.allowUpdateSiteMembership(siteId);
				context.put("allowUpdateSiteMembership", Boolean
						.valueOf(allowUpdateSiteMembership));

				Menu b = new MenuImpl(portlet, data, (String) state
						.getAttribute(STATE_ACTION));
				if (allowUpdateSite) 
				{
					// top menu bar
					if (!isMyWorkspace) {
						b.add(new MenuEntry(rb.getString("java.editsite"),
								"doMenu_edit_site_info"));
					}
					b.add(new MenuEntry(rb.getString("java.edittools"),
							"doMenu_edit_site_tools"));
					
					// if the page order helper is available, not
					// stealthed and not hidden, show the link
					if (notStealthOrHiddenTool("sakai-site-pageorder-helper")) {
						b.add(new MenuEntry(rb.getString("java.orderpages"),
								"doPageOrderHelper"));
					}
					
				}

				if (allowUpdateSiteMembership) 
				{
					// show add participant menu
					if (!isMyWorkspace) {
						// show the Add Participant menu
						b.add(new MenuEntry(rb.getString("java.addp"),
								"doMenu_siteInfo_addParticipant"));
						
						// show the Edit Class Roster menu
						if (siteType != null && siteType.equals((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
							b.add(new MenuEntry(rb.getString("java.editc"),
									"doMenu_siteInfo_editClass"));
						}
					}
				}
				
				if (allowUpdateGroupMembership) {
					// show Manage Groups menu
					if (!isMyWorkspace
							&& (ServerConfigurationService
									.getString("wsetup.group.support") == "" || ServerConfigurationService
									.getString("wsetup.group.support")
									.equalsIgnoreCase(Boolean.TRUE.toString()))) {
						// show the group toolbar unless configured
						// to not support group
						b.add(new MenuEntry(rb.getString("java.group"),
								"doMenu_group"));
					}
				}
				
				if (allowUpdateSite) 
				{
					if (!isMyWorkspace) {
						List gradToolsSiteTypes = (List) state
								.getAttribute(GRADTOOLS_SITE_TYPES);
						boolean isGradToolSite = false;
						if (siteType != null
								&& gradToolsSiteTypes.contains(siteType)) {
							isGradToolSite = true;
						}
						if (siteType == null || siteType != null
								&& !isGradToolSite) {
							// hide site access for GRADTOOLS
							// type of sites
							b.add(new MenuEntry(
									rb.getString("java.siteaccess"),
									"doMenu_edit_site_access"));
						}
						
						if (siteType == null || siteType != null
								&& !isGradToolSite) {
							// hide site duplicate and import
							// for GRADTOOLS type of sites
							if (SiteService.allowAddSite(null))
							{
								b.add(new MenuEntry(rb.getString("java.duplicate"),
										"doMenu_siteInfo_duplicate"));
							}

							List updatableSites = SiteService
									.getSites(
											org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
											null, null, null,
											SortType.TITLE_ASC, null);

							// import link should be visible even if only one
							// site
							if (updatableSites.size() > 0) {
								b.add(new MenuEntry(
										rb.getString("java.import"),
										"doMenu_siteInfo_import"));

								// a configuration param for
								// showing/hiding import
								// from file choice
								String importFromFile = ServerConfigurationService
										.getString("site.setup.import.file",
												Boolean.TRUE.toString());
								if (importFromFile.equalsIgnoreCase("true")) {
									// htripath: June
									// 4th added as per
									// Kris and changed
									// desc of above
									b.add(new MenuEntry(rb
											.getString("java.importFile"),
											"doAttachmentsMtrlFrmFile"));
								}
							}
						}
					}
				}
				
				if (b.size() > 0)
				{
					// add the menus to vm
					context.put("menu", b);
				}

				if (((String) state.getAttribute(STATE_SITE_MODE))
						.equalsIgnoreCase(SITE_MODE_SITESETUP)) {
					// editing from worksite setup tool
					context.put("fromWSetup", Boolean.TRUE);
					if (state.getAttribute(STATE_PREV_SITE) != null) {
						context.put("prevSite", state
								.getAttribute(STATE_PREV_SITE));
					}
					if (state.getAttribute(STATE_NEXT_SITE) != null) {
						context.put("nextSite", state
								.getAttribute(STATE_NEXT_SITE));
					}
				} else {
					context.put("fromWSetup", Boolean.FALSE);
				}
				// allow view roster?
				boolean allowViewRoster = SiteService.allowViewRoster(siteId);
				if (allowViewRoster) {
					context.put("viewRoster", Boolean.TRUE);
				} else {
					context.put("viewRoster", Boolean.FALSE);
				}
				// set participant list
				if (allowUpdateSite || allowViewRoster
						|| allowUpdateSiteMembership) {
					Collection participantsCollection = getParticipantList(state);
					sortedBy = (String) state.getAttribute(SORTED_BY);
					sortedAsc = (String) state.getAttribute(SORTED_ASC);
					if (sortedBy == null) {
						state.setAttribute(SORTED_BY,
								SORTED_BY_PARTICIPANT_NAME);
						sortedBy = SORTED_BY_PARTICIPANT_NAME;
					}
					if (sortedAsc == null) {
						sortedAsc = Boolean.TRUE.toString();
						state.setAttribute(SORTED_ASC, sortedAsc);
					}
					if (sortedBy != null)
						context.put("currentSortedBy", sortedBy);
					if (sortedAsc != null)
						context.put("currentSortAsc", sortedAsc);
					context.put("participantListSize", new Integer(participantsCollection.size()));
					context.put("participantList", prepPage(state));
					pagingInfoToContext(state, context);
				}

				context.put("include", Boolean.valueOf(site.isPubView()));

				// site contact information
				String contactName = siteProperties
						.getProperty(PROP_SITE_CONTACT_NAME);
				String contactEmail = siteProperties
						.getProperty(PROP_SITE_CONTACT_EMAIL);
				if (contactName == null && contactEmail == null) {
					User u = site.getCreatedBy();
					String email = u.getEmail();
					if (email != null) {
						contactEmail = u.getEmail();
					}
					contactName = u.getDisplayName();
				}
				if (contactName != null) {
					context.put("contactName", contactName);
				}
				if (contactEmail != null) {
					context.put("contactEmail", contactEmail);
				}
				if (siteType != null && siteType.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
					context.put("isCourseSite", Boolean.TRUE);
					
					coursesIntoContext(state, context, site);
					
					context.put("term", siteProperties
							.getProperty(PROP_SITE_TERM));
				} else {
					context.put("isCourseSite", Boolean.FALSE);
				}
			} catch (Exception e) {
				M_log.warn(this + " site info list: " + e.toString());
			}

			roles = getRoles(state);
			context.put("roles", roles);

			// will have the choice to active/inactive user or not
			String activeInactiveUser = ServerConfigurationService.getString(
					"activeInactiveUser", Boolean.FALSE.toString());
			if (activeInactiveUser.equalsIgnoreCase("true")) {
				context.put("activeInactiveUser", Boolean.TRUE);
				// put realm object into context
				realmId = SiteService.siteReference(site.getId());
				try {
					context.put("realm", AuthzGroupService
							.getAuthzGroup(realmId));
				} catch (GroupNotDefinedException e) {
					M_log.warn(this + "  IdUnusedException " + realmId);
				}
			} else {
				context.put("activeInactiveUser", Boolean.FALSE);
			}

			context.put("groupsWithMember", site
					.getGroupsWithMember(UserDirectoryService.getCurrentUser()
							.getId()));
			return (String) getContext(data).get("template") + TEMPLATE[12];

		case 13:
			/*
			 * buildContextForTemplate chef_site-siteInfo-editInfo.vm
			 * 
			 */
			siteProperties = site.getProperties();

			context.put("title", state.getAttribute(FORM_SITEINFO_TITLE));

			context.put("type", site.getType());

			siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (siteType != null && siteType.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("skins", state.getAttribute(STATE_ICONS));
				if (state.getAttribute(FORM_SITEINFO_SKIN) != null) {
					context.put("selectedIcon", state
							.getAttribute(FORM_SITEINFO_SKIN));
				} else if (site.getIconUrl() != null) {
					context.put("selectedIcon", site.getIconUrl());
				}

				setTermListForContext(context, state, true); // true->only future terms

				if (state.getAttribute(FORM_SITEINFO_TERM) == null) {
					String currentTerm = site.getProperties().getProperty(
							PROP_SITE_TERM);
					if (currentTerm != null) {
						state.setAttribute(FORM_SITEINFO_TERM, currentTerm);
					}
				}
				setSelectedTermForContext(context, state, FORM_SITEINFO_TERM);
			} else {
				context.put("isCourseSite", Boolean.FALSE);

				if (state.getAttribute(FORM_SITEINFO_ICON_URL) == null
						&& StringUtil.trimToNull(site.getIconUrl()) != null) {
					state.setAttribute(FORM_SITEINFO_ICON_URL, site
							.getIconUrl());
				}
				if (state.getAttribute(FORM_SITEINFO_ICON_URL) != null) {
					context.put("iconUrl", state
							.getAttribute(FORM_SITEINFO_ICON_URL));
				}
			}
			context.put("description", state
					.getAttribute(FORM_SITEINFO_DESCRIPTION));
			context.put("short_description", state
					.getAttribute(FORM_SITEINFO_SHORT_DESCRIPTION));
			context.put("form_site_contact_name", state
					.getAttribute(FORM_SITEINFO_CONTACT_NAME));
			context.put("form_site_contact_email", state
					.getAttribute(FORM_SITEINFO_CONTACT_EMAIL));

			// Display of appearance icon/url list with course site based on
			// "disable.course.site.skin.selection" value set with
			// sakai.properties file.
			if ((ServerConfigurationService
					.getString("disable.course.site.skin.selection"))
					.equals("true")) {
				context.put("disableCourseSelection", Boolean.TRUE);
			}

			return (String) getContext(data).get("template") + TEMPLATE[13];
		case 14:
			/*
			 * buildContextForTemplate chef_site-siteInfo-editInfoConfirm.vm
			 * 
			 */
			siteProperties = site.getProperties();
			siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (siteType != null && siteType.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("siteTerm", state.getAttribute(FORM_SITEINFO_TERM));
			} else {
				context.put("isCourseSite", Boolean.FALSE);
			}
			context.put("oTitle", site.getTitle());
			context.put("title", state.getAttribute(FORM_SITEINFO_TITLE));

			context.put("description", state
					.getAttribute(FORM_SITEINFO_DESCRIPTION));
			context.put("oDescription", site.getDescription());
			context.put("short_description", state
					.getAttribute(FORM_SITEINFO_SHORT_DESCRIPTION));
			context.put("oShort_description", site.getShortDescription());
			context.put("skin", state.getAttribute(FORM_SITEINFO_SKIN));
			context.put("oSkin", site.getIconUrl());
			context.put("skins", state.getAttribute(STATE_ICONS));
			context.put("oIcon", site.getIconUrl());
			context.put("icon", state.getAttribute(FORM_SITEINFO_ICON_URL));
			context.put("include", state.getAttribute(FORM_SITEINFO_INCLUDE));
			context.put("oInclude", Boolean.valueOf(site.isPubView()));
			context.put("name", state.getAttribute(FORM_SITEINFO_CONTACT_NAME));
			context.put("oName", siteProperties
					.getProperty(PROP_SITE_CONTACT_NAME));
			context.put("email", state
					.getAttribute(FORM_SITEINFO_CONTACT_EMAIL));
			context.put("oEmail", siteProperties
					.getProperty(PROP_SITE_CONTACT_EMAIL));

			return (String) getContext(data).get("template") + TEMPLATE[14];
		case 15:
			/*
			 * buildContextForTemplate chef_site-addRemoveFeatureConfirm.vm
			 * 
			 */
			context.put("title", site.getTitle());

			site_type = (String) state.getAttribute(STATE_SITE_TYPE);
			myworkspace_site = false;
			if (SiteService.isUserSite(site.getId())) {
				if (SiteService.getSiteUserId(site.getId()).equals(
						SessionManager.getCurrentSessionUserId())) {
					myworkspace_site = true;
					site_type = "myworkspace";
				}
			}

			context.put(STATE_TOOL_REGISTRATION_LIST, state
					.getAttribute(STATE_TOOL_REGISTRATION_LIST));

			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));
			context.put("selectedTools", orderToolIds(state, (String) state
					.getAttribute(STATE_SITE_TYPE), (List) state
					.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST)));
			context.put("oldSelectedTools", state
					.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST));
			context.put("oldSelectedHome", state
					.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME));
			context.put("continueIndex", "12");
			if (state.getAttribute(STATE_TOOL_EMAIL_ADDRESS) != null) {
				context.put("emailId", state
						.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			}
			context.put("serverName", ServerConfigurationService
					.getServerName());
			context.put("newsTitles", (Hashtable) state
					.getAttribute(STATE_NEWS_TITLES));
			context.put("wcTitles", (Hashtable) state
					.getAttribute(STATE_WEB_CONTENT_TITLES));

			if (fromENWModifyView(state)) {
				context.put("back", "26");
			} else {
				context.put("back", "4");
			}

			return (String) getContext(data).get("template") + TEMPLATE[15];
		case 18:
			/*
			 * buildContextForTemplate chef_siteInfo-editAccess.vm
			 * 
			 */
			List publicChangeableSiteTypes = (List) state
					.getAttribute(STATE_PUBLIC_CHANGEABLE_SITE_TYPES);
			List unJoinableSiteTypes = (List) state
					.getAttribute(STATE_DISABLE_JOINABLE_SITE_TYPE);

			if (site != null) {
				// editing existing site
				context.put("site", site);
				siteType = state.getAttribute(STATE_SITE_TYPE) != null ? (String) state
						.getAttribute(STATE_SITE_TYPE)
						: null;

				if (siteType != null
						&& publicChangeableSiteTypes.contains(siteType)) {
					context.put("publicChangeable", Boolean.TRUE);
				} else {
					context.put("publicChangeable", Boolean.FALSE);
				}
				context.put("include", Boolean.valueOf(site.isPubView()));

				if (siteType != null && !unJoinableSiteTypes.contains(siteType)) {
					// site can be set as joinable
					context.put("disableJoinable", Boolean.FALSE);
					if (state.getAttribute(STATE_JOINABLE) == null) {
						state.setAttribute(STATE_JOINABLE, Boolean.valueOf(site
								.isJoinable()));
					}
					if (state.getAttribute(STATE_JOINERROLE) == null
							|| state.getAttribute(STATE_JOINABLE) != null
							&& ((Boolean) state.getAttribute(STATE_JOINABLE))
									.booleanValue()) {
						state.setAttribute(STATE_JOINERROLE, site
								.getJoinerRole());
					}

					if (state.getAttribute(STATE_JOINABLE) != null) {
						context.put("joinable", state
								.getAttribute(STATE_JOINABLE));
					}
					if (state.getAttribute(STATE_JOINERROLE) != null) {
						context.put("joinerRole", state
								.getAttribute(STATE_JOINERROLE));
					}
				} else {
					// site cannot be set as joinable
					context.put("disableJoinable", Boolean.TRUE);
				}

				context.put("roles", getRoles(state));
				context.put("back", "12");
			} else {
				siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);

				if (siteInfo.site_type != null
						&& publicChangeableSiteTypes
								.contains(siteInfo.site_type)) {
					context.put("publicChangeable", Boolean.TRUE);
				} else {
					context.put("publicChangeable", Boolean.FALSE);
				}
				context.put("include", Boolean.valueOf(siteInfo.getInclude()));
				context.put("published", Boolean.valueOf(siteInfo
						.getPublished()));

				if (siteInfo.site_type != null
						&& !unJoinableSiteTypes.contains(siteInfo.site_type)) {
					// site can be set as joinable
					context.put("disableJoinable", Boolean.FALSE);
					context.put("joinable", Boolean.valueOf(siteInfo.joinable));
					context.put("joinerRole", siteInfo.joinerRole);
				} else {
					// site cannot be set as joinable
					context.put("disableJoinable", Boolean.TRUE);
				}

				// use the type's template, if defined
				String realmTemplate = "!site.template";
				if (siteInfo.site_type != null) {
					realmTemplate = realmTemplate + "." + siteInfo.site_type;
				}
				try {
					AuthzGroup r = AuthzGroupService
							.getAuthzGroup(realmTemplate);
					context.put("roles", r.getRoles());
				} catch (GroupNotDefinedException e) {
					try {
						AuthzGroup rr = AuthzGroupService
								.getAuthzGroup("!site.template");
						context.put("roles", rr.getRoles());
					} catch (GroupNotDefinedException ee) {
					}
				}

				// new site, go to confirmation page
				context.put("continue", "10");
				if (fromENWModifyView(state)) {
					context.put("back", "26");
				} else if (state.getAttribute(STATE_IMPORT) != null) {
					context.put("back", "27");
				} else {
					context.put("back", "3");
				}

				siteType = (String) state.getAttribute(STATE_SITE_TYPE);
				if (siteType != null && siteType.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
					context.put("isCourseSite", Boolean.TRUE);
					context.put("isProjectSite", Boolean.FALSE);
				} else {
					context.put("isCourseSite", Boolean.FALSE);
					if (siteType.equalsIgnoreCase("project")) {
						context.put("isProjectSite", Boolean.TRUE);
					}
				}
			}
			return (String) getContext(data).get("template") + TEMPLATE[18];
		case 19:
			/*
			 * buildContextForTemplate chef_site-addParticipant-sameRole.vm
			 * 
			 */
			context.put("title", site.getTitle());
			context.put("roles", getRoles(state));
			context.put("participantList", state
					.getAttribute(STATE_ADD_PARTICIPANTS));
			context.put("form_selectedRole", state
					.getAttribute("form_selectedRole"));
			return (String) getContext(data).get("template") + TEMPLATE[19];
		case 20:
			/*
			 * buildContextForTemplate chef_site-addParticipant-differentRole.vm
			 * 
			 */
			context.put("title", site.getTitle());
			context.put("roles", getRoles(state));
			context.put("selectedRoles", state
					.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES));
			context.put("participantList", state
					.getAttribute(STATE_ADD_PARTICIPANTS));
			return (String) getContext(data).get("template") + TEMPLATE[20];
		case 21:
			/*
			 * buildContextForTemplate chef_site-addParticipant-notification.vm
			 * 
			 */
			context.put("title", site.getTitle());
			context.put("sitePublished", Boolean.valueOf(site.isPublished()));
			if (state.getAttribute("form_selectedNotify") == null) {
				state.setAttribute("form_selectedNotify", Boolean.FALSE);
			}
			context.put("notify", state.getAttribute("form_selectedNotify"));
			boolean same_role = state.getAttribute("form_same_role") == null ? true
					: ((Boolean) state.getAttribute("form_same_role"))
							.booleanValue();
			if (same_role) {
				context.put("backIndex", "19");
			} else {
				context.put("backIndex", "20");
			}
			return (String) getContext(data).get("template") + TEMPLATE[21];
		case 22:
			/*
			 * buildContextForTemplate chef_site-addParticipant-confirm.vm
			 * 
			 */
			context.put("title", site.getTitle());
			context.put("participants", state
					.getAttribute(STATE_ADD_PARTICIPANTS));
			context.put("notify", state.getAttribute("form_selectedNotify"));
			context.put("roles", getRoles(state));
			context.put("same_role", state.getAttribute("form_same_role"));
			context.put("selectedRoles", state
					.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES));
			context
					.put("selectedRole", state
							.getAttribute("form_selectedRole"));
			return (String) getContext(data).get("template") + TEMPLATE[22];
		case 26:
			/*
			 * buildContextForTemplate chef_site-modifyENW.vm
			 * 
			 */
			site_type = (String) state.getAttribute(STATE_SITE_TYPE);
			boolean existingSite = site != null ? true : false;
			if (existingSite) {
				// revising a existing site's tool
				context.put("existingSite", Boolean.TRUE);
				context.put("back", "4");
				context.put("continue", "15");
				context.put("function", "eventSubmit_doAdd_remove_features");
			} else {
				// new site
				context.put("existingSite", Boolean.FALSE);
				context.put("function", "eventSubmit_doAdd_features");
				if (state.getAttribute(STATE_IMPORT) != null) {
					context.put("back", "27");
				} else {
					// new site, go to edit access page
					context.put("back", "3");
				}
				context.put("continue", "18");
			}

			context.put(STATE_TOOL_REGISTRATION_LIST, state
					.getAttribute(STATE_TOOL_REGISTRATION_LIST));
			toolRegistrationSelectedList = (List) state
					.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			context.put(STATE_TOOL_REGISTRATION_SELECTED_LIST,
					toolRegistrationSelectedList); // String toolId's
			String emailId = (String) state
					.getAttribute(STATE_TOOL_EMAIL_ADDRESS);
			if (emailId != null) {
				context.put("emailId", emailId);
			}

			// titles for news tools
			newsTitles = (Hashtable) state.getAttribute(STATE_NEWS_TITLES);
			if (newsTitles == null) {
				newsTitles = new Hashtable();
				newsTitles.put("sakai.news", NEWS_DEFAULT_TITLE);
				state.setAttribute(STATE_NEWS_TITLES, newsTitles);
			}
			context.put("newsTitles", newsTitles);
			// urls for news tools
			newsUrls = (Hashtable) state.getAttribute(STATE_NEWS_URLS);
			if (newsUrls == null) {
				newsUrls = new Hashtable();
				newsUrls.put("sakai.news", NEWS_DEFAULT_URL);
				state.setAttribute(STATE_NEWS_URLS, newsUrls);
			}
			context.put("newsUrls", newsUrls);
			// titles for web content tools
			wcTitles = (Hashtable) state.getAttribute(STATE_WEB_CONTENT_TITLES);
			if (wcTitles == null) {
				wcTitles = new Hashtable();
				wcTitles.put("sakai.iframe", WEB_CONTENT_DEFAULT_TITLE);
				state.setAttribute(STATE_WEB_CONTENT_TITLES, wcTitles);
			}
			context.put("wcTitles", wcTitles);
			// URLs for web content tools
			wcUrls = (Hashtable) state.getAttribute(STATE_WEB_CONTENT_URLS);
			if (wcUrls == null) {
				wcUrls = new Hashtable();
				wcUrls.put("sakai.iframe", WEB_CONTENT_DEFAULT_URL);
				state.setAttribute(STATE_WEB_CONTENT_URLS, wcUrls);
			}
			context.put("wcUrls", wcUrls);

			context.put("serverName", ServerConfigurationService
					.getServerName());

			context.put("oldSelectedTools", state
					.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST));

			return (String) getContext(data).get("template") + TEMPLATE[26];
		case 27:
			/*
			 * buildContextForTemplate chef_site-importSites.vm
			 * 
			 */
			existingSite = site != null ? true : false;
			site_type = (String) state.getAttribute(STATE_SITE_TYPE);
			if (existingSite) {
				// revising a existing site's tool
				context.put("continue", "12");
				context.put("back", "28");
				context.put("totalSteps", "2");
				context.put("step", "2");
				context.put("currentSite", site);
			} else {
				// new site, go to edit access page
				context.put("back", "3");
				if (fromENWModifyView(state)) {
					context.put("continue", "26");
				} else {
					context.put("continue", "18");
				}
			}
			context.put(STATE_TOOL_REGISTRATION_LIST, state
					.getAttribute(STATE_TOOL_REGISTRATION_LIST));
			context.put("selectedTools", orderToolIds(state, site_type,
					(List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST))); // String toolId's
			context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));
			context.put("importSitesTools", state
					.getAttribute(STATE_IMPORT_SITE_TOOL));
			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));
			context.put("importSupportedTools", importTools());

			return (String) getContext(data).get("template") + TEMPLATE[27];
		case 28:
			/*
			 * buildContextForTemplate chef_siteinfo-import.vm
			 * 
			 */
			context.put("currentSite", site);
			context.put("importSiteList", state
					.getAttribute(STATE_IMPORT_SITES));
			context.put("sites", SiteService.getSites(
					org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
					null, null, null, SortType.TITLE_ASC, null));
			return (String) getContext(data).get("template") + TEMPLATE[28];
		case 29:
			/*
			 * buildContextForTemplate chef_siteinfo-duplicate.vm
			 * 
			 */
			context.put("siteTitle", site.getTitle());
			String sType = site.getType();
			if (sType != null && sType.equals((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("currentTermId", site.getProperties().getProperty(
						PROP_SITE_TERM));
				setTermListForContext(context, state, true); // true upcoming only
			} else {
				context.put("isCourseSite", Boolean.FALSE);
			}
			if (state.getAttribute(SITE_DUPLICATED) == null) {
				context.put("siteDuplicated", Boolean.FALSE);
			} else {
				context.put("siteDuplicated", Boolean.TRUE);
				context.put("duplicatedName", state
						.getAttribute(SITE_DUPLICATED_NAME));
			}

			return (String) getContext(data).get("template") + TEMPLATE[29];
		case 36:
			/*
			 * buildContextForTemplate chef_site-newSiteCourse.vm
			 */		
			// SAK-9824
			Boolean enableCourseCreationForUser = ServerConfigurationService.getBoolean("site.enableCreateAnyUser", Boolean.FALSE);
			context.put("enableCourseCreationForUser", enableCourseCreationForUser);
				
			if (site != null) {
				context.put("site", site);
				context.put("siteTitle", site.getTitle());
				setTermListForContext(context, state, true); // true -> upcoming only

				List providerCourseList = (List) state
						.getAttribute(SITE_PROVIDER_COURSE_LIST);
				coursesIntoContext(state, context, site);

				AcademicSession t = (AcademicSession) state
						.getAttribute(STATE_TERM_SELECTED);
				context.put("term", t);
				if (t != null) {
					String userId = UserDirectoryService.getCurrentUser().getEid();
					List courses = prepareCourseAndSectionListing(userId, t
							.getEid(), state);
					if (courses != null && courses.size() > 0) {
						Vector notIncludedCourse = new Vector();

						// remove included sites
						for (Iterator i = courses.iterator(); i.hasNext();) {
							CourseObject c = (CourseObject) i.next();
							if (providerCourseList == null || providerCourseList != null && !providerCourseList.contains(c.getEid())) {
								notIncludedCourse.add(c);
							}
						}
						state.setAttribute(STATE_TERM_COURSE_LIST,
								notIncludedCourse);
					} else {
						state.removeAttribute(STATE_TERM_COURSE_LIST);
					}
				}

				// step number used in UI
				state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer("1"));
			} else {
				// need to include both list 'cos STATE_CM_AUTHORIZER_SECTIONS
				// contains sections that doens't belongs to current user and
				// STATE_ADD_CLASS_PROVIDER_CHOSEN contains section that does -
				// v2.4 daisyf
				if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null
						|| state.getAttribute(STATE_CM_AUTHORIZER_SECTIONS) != null) {
					List<String> providerSectionList = (List<String>) state
							.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
					if (providerSectionList != null) {
						/*
						List list1 = prepareSectionObject(providerSectionList,
								(String) state
										.getAttribute(STATE_CM_CURRENT_USERID));
										*/
						context.put("selectedProviderCourse", providerSectionList);
					}

					List<SectionObject> authorizerSectionList = (List<SectionObject>) state
							.getAttribute(STATE_CM_AUTHORIZER_SECTIONS);
					if (authorizerSectionList != null) {
						List authorizerList = (List) state
								.getAttribute(STATE_CM_AUTHORIZER_LIST);
						//authorizerList is a list of SectionObject
						/*
						String userId = null;
						if (authorizerList != null) {
							userId = (String) authorizerList.get(0);
						}
						List list2 = prepareSectionObject(
								authorizerSectionList, userId);
								*/
						ArrayList list2 = new ArrayList();
						for (int i=0; i<authorizerSectionList.size();i++){
							SectionObject so = (SectionObject)authorizerSectionList.get(i);
							list2.add(so.getEid());
						}
						context.put("selectedAuthorizerCourse", list2);
					}
				}

				if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
					context.put("selectedManualCourse", Boolean.TRUE);
				}
				context.put("term", (AcademicSession) state
						.getAttribute(STATE_TERM_SELECTED));
				context.put("currentUserId", (String) state
						.getAttribute(STATE_CM_CURRENT_USERID));
				context.put("form_additional", (String) state
						.getAttribute(FORM_ADDITIONAL));
				context.put("authorizers", getAuthorizers(state));
			}
			if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITESETUP)) {
				context.put("backIndex", "1");
			} else if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITEINFO)) {
				context.put("backIndex", "");
			}
			List ll = (List) state.getAttribute(STATE_TERM_COURSE_LIST);
			context.put("termCourseList", state
					.getAttribute(STATE_TERM_COURSE_LIST));

			// added for 2.4 -daisyf
			context.put("campusDirectory", getCampusDirectory());
			context.put("userId", (String) state
					.getAttribute(STATE_INSTRUCTOR_SELECTED));
			/*
			 * for measuring how long it takes to load sections java.util.Date
			 * date = new java.util.Date(); M_log.debug("***2. finish at:
			 * "+date); M_log.debug("***3. userId:"+(String) state
			 * .getAttribute(STATE_INSTRUCTOR_SELECTED));
			 */
			return (String) getContext(data).get("template") + TEMPLATE[36];
		case 37:
			/*
			 * buildContextForTemplate chef_site-newSiteCourseManual.vm
			 */
			if (site != null) {
				context.put("site", site);
				context.put("siteTitle", site.getTitle());
				coursesIntoContext(state, context, site);
			}
			buildInstructorSectionsList(state, params, context);
			context.put("form_requiredFields", sectionFieldProvider
					.getRequiredFields());
			context.put("form_additional", siteInfo.additional);
			context.put("form_title", siteInfo.title);
			context.put("form_description", siteInfo.description);
			context.put("officialAccountName", ServerConfigurationService
					.getString("officialAccountName", ""));
			context.put("value_uniqname", state
					.getAttribute(STATE_SITE_QUEST_UNIQNAME));
			int number = 1;
			if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
				number = ((Integer) state
						.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER))
						.intValue();
				context.put("currentNumber", new Integer(number));
			}
			context.put("currentNumber", new Integer(number));
			context.put("listSize", number>0?new Integer(number - 1):0);
			if (state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS) != null && ((List) state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS)).size() > 0)
			{
				context.put("fieldValues", state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
			}

			if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null) {
				List l = (List) state
						.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
				context.put("selectedProviderCourse", l);
				context.put("size", new Integer(l.size() - 1));
			}

			if (state.getAttribute(STATE_CM_REQUESTED_SECTIONS) != null) {
				List l = (List) state
						.getAttribute(STATE_CM_REQUESTED_SECTIONS);
				context.put("cmRequestedSections", l);
			}
			
			if (state.getAttribute(STATE_SITE_MODE).equals(SITE_MODE_SITEINFO))
			{
				context.put("editSite", Boolean.TRUE);
				context.put("cmSelectedSections", state.getAttribute(STATE_CM_SELECTED_SECTIONS));
			}

			if (site == null) {
				// v2.4 - added & modified by daisyf
				if (courseManagementIsImplemented() && state.getAttribute(STATE_TERM_COURSE_LIST) != null)
				{
					// back to the list view of sections
					context.put("back", "36");
				} else {
					context.put("back", "1");
				}
				if (state.getAttribute(STATE_AUTO_ADD) != null) {
					context.put("autoAdd", Boolean.TRUE);
					// context.put("back", "36");
				}
			}
			else
			{
				// editing site
				context.put("back", "36");
			}
			
			isFutureTermSelected(state);
			context.put("isFutureTerm", state
					.getAttribute(STATE_FUTURE_TERM_SELECTED));
			context.put("weeksAhead", ServerConfigurationService.getString(
					"roster.available.weeks.before.term.start", "0"));
			return (String) getContext(data).get("template") + TEMPLATE[37];
		case 42:
			/*
			 * buildContextForTemplate chef_site-gradtoolsConfirm.vm
			 * 
			 */
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			context.put("title", siteInfo.title);
			context.put("description", siteInfo.description);
			context.put("short_description", siteInfo.short_description);
			toolRegistrationList = (Vector) state
					.getAttribute(STATE_PROJECT_TOOL_LIST);
			toolRegistrationSelectedList = (List) state
					.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			context.put(STATE_TOOL_REGISTRATION_SELECTED_LIST,
					toolRegistrationSelectedList); // String toolId's
			context.put(STATE_TOOL_REGISTRATION_LIST, toolRegistrationList); // %%%
			// use
			// Tool
			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));
			context
					.put("emailId", state
							.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			context.put("serverName", ServerConfigurationService
					.getServerName());
			context.put("include", new Boolean(siteInfo.include));
			return (String) getContext(data).get("template") + TEMPLATE[42];
		case 43:
			/*
			 * buildContextForTemplate chef_siteInfo-editClass.vm
			 * 
			 */
			bar = new MenuImpl(portlet, data, (String) state
					.getAttribute(STATE_ACTION));
			if (SiteService.allowAddSite(null)) {
				bar.add(new MenuEntry(rb.getString("java.addclasses"),
						"doMenu_siteInfo_addClass"));
			}
			context.put("menu", bar);

			context.put("siteTitle", site.getTitle());
			coursesIntoContext(state, context, site);

			return (String) getContext(data).get("template") + TEMPLATE[43];
		case 44:
			/*
			 * buildContextForTemplate chef_siteInfo-addCourseConfirm.vm
			 * 
			 */

			context.put("siteTitle", site.getTitle());

			coursesIntoContext(state, context, site);

			context.put("providerAddCourses", state
					.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN));
			if (state.getAttribute(STATE_CM_SELECTED_SECTIONS) != null)
			{
				context.put("cmSelectedSections", state.getAttribute(STATE_CM_SELECTED_SECTIONS));
			}
			if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
				int addNumber = ((Integer) state
						.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER))
						.intValue() - 1;
				context.put("manualAddNumber", new Integer(addNumber));
				context.put("requestFields", state
						.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
				context.put("backIndex", "37");
			} else {
				context.put("backIndex", "36");
			}
			// those manual inputs
			context.put("form_requiredFields", sectionFieldProvider
					.getRequiredFields());
			context.put("fieldValues", state
					.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));

			return (String) getContext(data).get("template") + TEMPLATE[44];

			// htripath - import materials from classic
		case 45:
			/*
			 * buildContextForTemplate chef_siteInfo-importMtrlMaster.vm
			 * 
			 */
			return (String) getContext(data).get("template") + TEMPLATE[45];

		case 46:
			/*
			 * buildContextForTemplate chef_siteInfo-importMtrlCopy.vm
			 * 
			 */
			// this is for list display in listbox
			context
					.put("allZipSites", state
							.getAttribute(ALL_ZIP_IMPORT_SITES));

			context.put("finalZipSites", state
					.getAttribute(FINAL_ZIP_IMPORT_SITES));
			// zip file
			// context.put("zipreffile",state.getAttribute(CLASSIC_ZIP_FILE_NAME));

			return (String) getContext(data).get("template") + TEMPLATE[46];

		case 47:
			/*
			 * buildContextForTemplate chef_siteInfo-importMtrlCopyConfirm.vm
			 * 
			 */
			context.put("finalZipSites", state
					.getAttribute(FINAL_ZIP_IMPORT_SITES));

			return (String) getContext(data).get("template") + TEMPLATE[47];

		case 48:
			/*
			 * buildContextForTemplate chef_siteInfo-importMtrlCopyConfirm.vm
			 * 
			 */
			context.put("finalZipSites", state
					.getAttribute(FINAL_ZIP_IMPORT_SITES));
			return (String) getContext(data).get("template") + TEMPLATE[48];
		case 49:
			/*
			 * buildContextForTemplate chef_siteInfo-group.vm
			 * 
			 */
			context.put("site", site);
			bar = new MenuImpl(portlet, data, (String) state
					.getAttribute(STATE_ACTION));
			if (SiteService.allowUpdateSite(site.getId())
					|| SiteService.allowUpdateGroupMembership(site.getId())) {
				bar.add(new MenuEntry(rb.getString("java.newgroup"), "doGroup_new"));
			}
			context.put("menu", bar);

			// the group list
			sortedBy = (String) state.getAttribute(SORTED_BY);
			sortedAsc = (String) state.getAttribute(SORTED_ASC);

			if (sortedBy != null)
				context.put("currentSortedBy", sortedBy);
			if (sortedAsc != null)
				context.put("currentSortAsc", sortedAsc);

			// only show groups created by WSetup tool itself
			Collection groups = (Collection) site.getGroups();
			List groupsByWSetup = new Vector();
			for (Iterator gIterator = groups.iterator(); gIterator.hasNext();) {
				Group gNext = (Group) gIterator.next();
				String gProp = gNext.getProperties().getProperty(
						GROUP_PROP_WSETUP_CREATED);
				if (gProp != null && gProp.equals(Boolean.TRUE.toString())) {
					groupsByWSetup.add(gNext);
				}
			}
			if (sortedBy != null && sortedAsc != null) {
				context.put("groups", new SortedIterator(groupsByWSetup
						.iterator(), new SiteComparator(sortedBy, sortedAsc)));
			}
			return (String) getContext(data).get("template") + TEMPLATE[49];
		case 50:
			/*
			 * buildContextForTemplate chef_siteInfo-groupedit.vm
			 * 
			 */
			Group g = getStateGroup(state);
			if (g != null) {
				context.put("group", g);
				context.put("newgroup", Boolean.FALSE);
			} else {
				context.put("newgroup", Boolean.TRUE);
			}
			if (state.getAttribute(STATE_GROUP_TITLE) != null) {
				context.put("title", state.getAttribute(STATE_GROUP_TITLE));
			}
			if (state.getAttribute(STATE_GROUP_DESCRIPTION) != null) {
				context.put("description", state
						.getAttribute(STATE_GROUP_DESCRIPTION));
			}
			Iterator siteMembers = new SortedIterator(getParticipantList(state)
					.iterator(), new SiteComparator(SORTED_BY_PARTICIPANT_NAME,
					Boolean.TRUE.toString()));
			if (siteMembers != null && siteMembers.hasNext()) {
				context.put("generalMembers", siteMembers);
			}
			Set groupMembersSet = (Set) state.getAttribute(STATE_GROUP_MEMBERS);
			if (state.getAttribute(STATE_GROUP_MEMBERS) != null) {
				context.put("groupMembers", new SortedIterator(groupMembersSet
						.iterator(), new SiteComparator(SORTED_BY_MEMBER_NAME,
						Boolean.TRUE.toString())));
			}
			context.put("groupMembersClone", groupMembersSet);
			context.put("userDirectoryService", UserDirectoryService
					.getInstance());
			return (String) getContext(data).get("template") + TEMPLATE[50];
		case 51:
			/*
			 * buildContextForTemplate chef_siteInfo-groupDeleteConfirm.vm
			 * 
			 */
			context.put("site", site);

			context
					.put("removeGroupIds", new ArrayList(Arrays
							.asList((String[]) state
									.getAttribute(STATE_GROUP_REMOVE))));
			return (String) getContext(data).get("template") + TEMPLATE[51];
		case 53: {
			/*
			 * build context for chef_site-findCourse.vm
			 */

			AcademicSession t = (AcademicSession) state
					.getAttribute(STATE_TERM_SELECTED);

			List cmLevels = (List) state.getAttribute(STATE_CM_LEVELS), selections = (List) state
					.getAttribute(STATE_CM_LEVEL_SELECTIONS);
			
			if (cmLevels == null)
			{
				cmLevels = getCMLevelLabels();
			}

			SectionObject selectedSect = (SectionObject) state
					.getAttribute(STATE_CM_SELECTED_SECTION);
			List<SectionObject> requestedSections = (List<SectionObject>) state
					.getAttribute(STATE_CM_REQUESTED_SECTIONS);

			if (courseManagementIsImplemented() && cms != null) {
				context.put("cmsAvailable", new Boolean(true));
			}

			if (cms == null || !courseManagementIsImplemented()
					|| cmLevels == null || cmLevels.size() < 1) {
				// TODO: redirect to manual entry: case #37
			} else {
				Object levelOpts[] = new Object[cmLevels.size()];
				int numSelections = 0;

				if (selections != null)
					numSelections = selections.size();

				// populate options for dropdown lists
				switch (numSelections) {
				/*
				 * execution will fall through these statements based on number
				 * of selections already made
				 */
				case 3:
					// intentionally blank
				case 2:
					levelOpts[2] = getCMSections((String) selections.get(1));
				case 1:
					levelOpts[1] = getCMCourseOfferings((String) selections
							.get(0), t.getEid());
				default:
					levelOpts[0] = getCMSubjects();
				}

				context.put("cmLevelOptions", Arrays.asList(levelOpts));
			}
			if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null) {
				context.put("selectedProviderCourse", state
						.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN));
			}
			if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
				int courseInd = ((Integer) state
						.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER))
						.intValue();
				context.put("manualAddNumber", new Integer(courseInd - 1));
				context.put("manualAddFields", state
						.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
			}

			context.put("term", (AcademicSession) state
					.getAttribute(STATE_TERM_SELECTED));

			context.put("cmLevels", cmLevels);
			context.put("cmLevelSelections", selections);
			context.put("selectedCourse", selectedSect);
			context.put("cmRequestedSections", requestedSections);
			if (state.getAttribute(STATE_SITE_MODE).equals(SITE_MODE_SITEINFO))
			{
				context.put("editSite", Boolean.TRUE);
				context.put("cmSelectedSections", state.getAttribute(STATE_CM_SELECTED_SECTIONS));
			}
			if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITESETUP)) {
				if (state.getAttribute(STATE_TERM_COURSE_LIST) != null)
				{
					context.put("backIndex", "36");
				}
				else
				{
					context.put("backIndex", "1");
				}
			}
			else if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITEINFO)) 
			{
				context.put("backIndex", "36");
			}

			context.put("authzGroupService", AuthzGroupService.getInstance());
			return (String) getContext(data).get("template") + TEMPLATE[53];
		}

		}
		// should never be reached
		return (String) getContext(data).get("template") + TEMPLATE[0];

	} // buildContextForTemplate

	/**
	 * Launch the Page Order Helper Tool -- for ordering, adding and customizing
	 * pages
	 * 
	 * @see case 12
	 * 
	 */
	public void doPageOrderHelper(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// pass in the siteId of the site to be ordered (so it can configure
		// sites other then the current site)
		SessionManager.getCurrentToolSession().setAttribute(
				HELPER_ID + ".siteId", ((Site) getStateSite(state)).getId());

		// launch the helper
		startHelper(data.getRequest(), "sakai-site-pageorder-helper");
	}

	// htripath: import materials from classic
	/**
	 * Master import -- for import materials from a file
	 * 
	 * @see case 45
	 * 
	 */
	public void doAttachmentsMtrlFrmFile(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// state.setAttribute(FILE_UPLOAD_MAX_SIZE,
		// ServerConfigurationService.getString("content.upload.max", "1"));
		state.setAttribute(STATE_TEMPLATE_INDEX, "45");
	} // doImportMtrlFrmFile

	/**
	 * Handle File Upload request
	 * 
	 * @see case 46
	 * @throws Exception
	 */
	public void doUpload_Mtrl_Frm_File(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		List allzipList = new Vector();
		List finalzipList = new Vector();
		List directcopyList = new Vector();

		// see if the user uploaded a file
		FileItem fileFromUpload = null;
		String fileName = null;
		fileFromUpload = data.getParameters().getFileItem("file");

		String max_file_size_mb = ServerConfigurationService.getString(
				"content.upload.max", "1");
		int max_bytes = 1024 * 1024;
		try {
			max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
		} catch (Exception e) {
			// if unable to parse an integer from the value
			// in the properties file, use 1 MB as a default
			max_file_size_mb = "1";
			max_bytes = 1024 * 1024;
		}
		if (fileFromUpload == null) {
			// "The user submitted a file to upload but it was too big!"
			addAlert(state, rb.getString("importFile.size") + " "
					+ max_file_size_mb + "MB "
					+ rb.getString("importFile.exceeded"));
		} else if (fileFromUpload.getFileName() == null
				|| fileFromUpload.getFileName().length() == 0) {
			addAlert(state, rb.getString("importFile.choosefile"));
		} else {
			byte[] fileData = fileFromUpload.get();

			if (fileData.length >= max_bytes) {
				addAlert(state, rb.getString("size") + " " + max_file_size_mb
						+ "MB " + rb.getString("importFile.exceeded"));
			} else if (fileData.length > 0) {

				if (importService.isValidArchive(fileData)) {
					ImportDataSource importDataSource = importService
							.parseFromFile(fileData);
					Log.info("chef", "Getting import items from manifest.");
					List lst = importDataSource.getItemCategories();
					if (lst != null && lst.size() > 0) {
						Iterator iter = lst.iterator();
						while (iter.hasNext()) {
							ImportMetadata importdata = (ImportMetadata) iter
									.next();
							// Log.info("chef","Preparing import
							// item '" + importdata.getId() + "'");
							if ((!importdata.isMandatory())
									&& (importdata.getFileName()
											.endsWith(".xml"))) {
								allzipList.add(importdata);
							} else {
								directcopyList.add(importdata);
							}
						}
					}
					// set Attributes
					state.setAttribute(ALL_ZIP_IMPORT_SITES, allzipList);
					state.setAttribute(FINAL_ZIP_IMPORT_SITES, finalzipList);
					state.setAttribute(DIRECT_ZIP_IMPORT_SITES, directcopyList);
					state.setAttribute(CLASSIC_ZIP_FILE_NAME, fileName);
					state.setAttribute(IMPORT_DATA_SOURCE, importDataSource);

					state.setAttribute(STATE_TEMPLATE_INDEX, "46");
				} else { // uploaded file is not a valid archive

				}
			}
		}
	} // doImportMtrlFrmFile

	/**
	 * Handle addition to list request
	 * 
	 * @param data
	 */
	public void doAdd_MtrlSite(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		List zipList = (List) state.getAttribute(ALL_ZIP_IMPORT_SITES);
		List fnlList = (List) state.getAttribute(FINAL_ZIP_IMPORT_SITES);
		List importSites = new ArrayList(Arrays.asList(params
				.getStrings("addImportSelected")));

		for (int i = 0; i < importSites.size(); i++) {
			String value = (String) importSites.get(i);
			fnlList.add(removeItems(value, zipList));
		}

		state.setAttribute(ALL_ZIP_IMPORT_SITES, zipList);
		state.setAttribute(FINAL_ZIP_IMPORT_SITES, fnlList);

		state.setAttribute(STATE_TEMPLATE_INDEX, "46");
	} // doAdd_MtrlSite

	/**
	 * Helper class for Add and remove
	 * 
	 * @param value
	 * @param items
	 * @return
	 */
	public ImportMetadata removeItems(String value, List items) {
		ImportMetadata result = null;
		for (int i = 0; i < items.size(); i++) {
			ImportMetadata item = (ImportMetadata) items.get(i);
			if (value.equals(item.getId())) {
				result = (ImportMetadata) items.remove(i);
				break;
			}
		}
		return result;
	}

	/**
	 * Handle the request for remove
	 * 
	 * @param data
	 */
	public void doRemove_MtrlSite(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		List zipList = (List) state.getAttribute(ALL_ZIP_IMPORT_SITES);
		List fnlList = (List) state.getAttribute(FINAL_ZIP_IMPORT_SITES);

		List importSites = new ArrayList(Arrays.asList(params
				.getStrings("removeImportSelected")));

		for (int i = 0; i < importSites.size(); i++) {
			String value = (String) importSites.get(i);
			zipList.add(removeItems(value, fnlList));
		}

		state.setAttribute(ALL_ZIP_IMPORT_SITES, zipList);
		state.setAttribute(FINAL_ZIP_IMPORT_SITES, fnlList);

		state.setAttribute(STATE_TEMPLATE_INDEX, "46");
	} // doAdd_MtrlSite

	/**
	 * Handle the request for copy
	 * 
	 * @param data
	 */
	public void doCopyMtrlSite(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		List fnlList = (List) state.getAttribute(FINAL_ZIP_IMPORT_SITES);
		state.setAttribute(FINAL_ZIP_IMPORT_SITES, fnlList);

		state.setAttribute(STATE_TEMPLATE_INDEX, "47");
	} // doCopy_MtrlSite

	/**
	 * Handle the request for Save
	 * 
	 * @param data
	 * @throws ImportException
	 */
	public void doSaveMtrlSite(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String siteId = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);
		List fnlList = (List) state.getAttribute(FINAL_ZIP_IMPORT_SITES);
		List directList = (List) state.getAttribute(DIRECT_ZIP_IMPORT_SITES);
		ImportDataSource importDataSource = (ImportDataSource) state
				.getAttribute(IMPORT_DATA_SOURCE);

		// combine the selected import items with the mandatory import items
		fnlList.addAll(directList);
		Log.info("chef", "doSaveMtrlSite() about to import " + fnlList.size()
				+ " top level items");
		Log.info("chef", "doSaveMtrlSite() the importDataSource is "
				+ importDataSource.getClass().getName());
		if (importDataSource instanceof SakaiArchive) {
			Log.info("chef",
					"doSaveMtrlSite() our data source is a Sakai format");
			((SakaiArchive) importDataSource).buildSourceFolder(fnlList);
			Log.info("chef", "doSaveMtrlSite() source folder is "
					+ ((SakaiArchive) importDataSource).getSourceFolder());
			ArchiveService.merge(((SakaiArchive) importDataSource)
					.getSourceFolder(), siteId, null);
		} else {
			importService.doImportItems(importDataSource
					.getItemsForCategories(fnlList), siteId);
		}
		// remove attributes
		state.removeAttribute(ALL_ZIP_IMPORT_SITES);
		state.removeAttribute(FINAL_ZIP_IMPORT_SITES);
		state.removeAttribute(DIRECT_ZIP_IMPORT_SITES);
		state.removeAttribute(CLASSIC_ZIP_FILE_NAME);
		state.removeAttribute(SESSION_CONTEXT_ID);
		state.removeAttribute(IMPORT_DATA_SOURCE);

		state.setAttribute(STATE_TEMPLATE_INDEX, "48");

		// state.setAttribute(STATE_TEMPLATE_INDEX, "28");
	} // doSave_MtrlSite

	public void doSaveMtrlSiteMsg(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// remove attributes
		state.removeAttribute(ALL_ZIP_IMPORT_SITES);
		state.removeAttribute(FINAL_ZIP_IMPORT_SITES);
		state.removeAttribute(DIRECT_ZIP_IMPORT_SITES);
		state.removeAttribute(CLASSIC_ZIP_FILE_NAME);
		state.removeAttribute(SESSION_CONTEXT_ID);

		state.setAttribute(STATE_TEMPLATE_INDEX, "12");

	}

	// htripath-end

	/**
	 * Handle the site search request.
	 */
	public void doSite_search(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the search form field into the state object
		String search = StringUtil.trimToNull(data.getParameters().getString(
				FORM_SEARCH));

		// set the flag to go to the prev page on the next list
		if (search == null) {
			state.removeAttribute(STATE_SEARCH);
		} else {
			state.setAttribute(STATE_SEARCH, search);
		}

	} // doSite_search

	/**
	 * Handle a Search Clear request.
	 */
	public void doSite_search_clear(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// clear the search
		state.removeAttribute(STATE_SEARCH);

	} // doSite_search_clear

	private void coursesIntoContext(SessionState state, Context context,
			Site site) {
		List providerCourseList = getProviderCourseList(StringUtil
				.trimToNull(getExternalRealmId(state)));
		if (providerCourseList != null && providerCourseList.size() > 0) {
			state.setAttribute(SITE_PROVIDER_COURSE_LIST, providerCourseList);
			
			String sectionTitleString = "";
			for(int i = 0; i < providerCourseList.size(); i++)
			{
				String sectionId = (String) providerCourseList.get(i);
				try
				{
					Section s = cms.getSection(sectionId);
					sectionTitleString = (i>0)?sectionTitleString + "<br />" + s.getTitle():s.getTitle(); 
				}
				catch (Exception e)
				{
					M_log.warn("coursesIntoContext " + e.getMessage() + " sectionId=" + sectionId);
				}
			}
			context.put("providedSectionTitle", sectionTitleString);
			context.put("providerCourseList", providerCourseList);
		}

		// put manual requested courses into context
		courseListFromStringIntoContext(state, context, site, STATE_CM_REQUESTED_SECTIONS, STATE_CM_REQUESTED_SECTIONS, "cmRequestedCourseList");
		
		// put manual requested courses into context
		courseListFromStringIntoContext(state, context, site, PROP_SITE_REQUEST_COURSE, SITE_MANUAL_COURSE_LIST, "manualCourseList");
	}

	private void courseListFromStringIntoContext(SessionState state, Context context, Site site, String site_prop_name, String state_attribute_string, String context_string) {
		String courseListString = StringUtil.trimToNull(site.getProperties().getProperty(site_prop_name));
		if (courseListString != null) {
			List courseList = new Vector();
			if (courseListString.indexOf("+") != -1) {
				courseList = new ArrayList(Arrays.asList(courseListString.split("\\+")));
			} else {
				courseList.add(courseListString);
			}
			
			if (state_attribute_string.equals(STATE_CM_REQUESTED_SECTIONS))
			{
				// need to construct the list of SectionObjects
				List<SectionObject> soList = new Vector();
				for (int i=0; i<courseList.size();i++)
				{
					String courseEid = (String) courseList.get(i);
					
					try
					{
					Section s = cms.getSection(courseEid);
					if (s!=null)
						soList.add(new SectionObject(s));
					}
					catch (Exception e)
					{
						M_log.warn(e.getMessage() + courseEid);
					}
				}
				if (soList.size() > 0)
					state.setAttribute(STATE_CM_REQUESTED_SECTIONS, soList);
			}
			else
			{
				// the list is of String objects
				state.setAttribute(state_attribute_string, courseList);
			}
		}
		context.put(context_string, state.getAttribute(state_attribute_string));
	}

	/**
	 * buildInstructorSectionsList Build the CourseListItem list for this
	 * Instructor for the requested Term
	 * 
	 */
	private void buildInstructorSectionsList(SessionState state,
			ParameterParser params, Context context) {
		// Site information
		// The sections of the specified term having this person as Instructor
		context.put("providerCourseSectionList", state
				.getAttribute("providerCourseSectionList"));
		context.put("manualCourseSectionList", state
				.getAttribute("manualCourseSectionList"));
		context.put("term", (AcademicSession) state
				.getAttribute(STATE_TERM_SELECTED));
		setTermListForContext(context, state, true); //-> future terms only
		context.put(STATE_TERM_COURSE_LIST, state
				.getAttribute(STATE_TERM_COURSE_LIST));
		context.put("tlang", rb);
	} // buildInstructorSectionsList

	/**
	 * getProviderCourseList a course site/realm id in one of three formats, for
	 * a single section, for multiple sections of the same course, or for a
	 * cross-listing having multiple courses. getProviderCourseList parses a
	 * realm id into year, term, campus_code, catalog_nbr, section components.
	 * 
	 * @param id
	 *            is a String representation of the course realm id (external
	 *            id).
	 */
	private List getProviderCourseList(String id) {
		Vector rv = new Vector();
		if (id == null || id == NULL_STRING) {
			return rv;
		}
		// Break Provider Id into course id parts
		String[] courseIds = groupProvider.unpackId(id);
		
		// Iterate through course ids
		for (int i=0; i<courseIds.length; i++) {
			String courseId = (String) courseIds[i];

			rv.add(courseId);
		}
		return rv;

	} // getProviderCourseList

	/**
	 * {@inheritDoc}
	 */
	protected int sizeResources(SessionState state) {
		int size = 0;
		String search = "";
		String userId = SessionManager.getCurrentSessionUserId();

		// if called from the site list page
		if (((String) state.getAttribute(STATE_TEMPLATE_INDEX)).equals("0")) {
			search = StringUtil.trimToNull((String) state
					.getAttribute(STATE_SEARCH));
			if (SecurityService.isSuperUser()) {
				// admin-type of user
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {
					if (view.equals(rb.getString("java.allmy"))) {
						// search for non-user sites, using
						// the criteria
						size = SiteService
								.countSites(
										org.sakaiproject.site.api.SiteService.SelectionType.NON_USER,
										null, search, null);
					} else if (view.equals(rb.getString("java.my"))) {
						// search for a specific user site
						// for the particular user id in the
						// criteria - exact match only
						try {
							SiteService.getSite(SiteService
									.getUserSiteId(search));
							size++;
						} catch (IdUnusedException e) {
						}
					} else if (view.equalsIgnoreCase(rb
							.getString("java.gradtools"))) {
						// search for gradtools sites
						size = SiteService
								.countSites(
										org.sakaiproject.site.api.SiteService.SelectionType.NON_USER,
										state
												.getAttribute(GRADTOOLS_SITE_TYPES),
										search, null);
					} else {
						// search for specific type of sites
						size = SiteService
								.countSites(
										org.sakaiproject.site.api.SiteService.SelectionType.NON_USER,
										view, search, null);
					}
				}
			} else {
				Site userWorkspaceSite = null;
				try {
					userWorkspaceSite = SiteService.getSite(SiteService
							.getUserSiteId(userId));
				} catch (IdUnusedException e) {
					M_log.warn("Cannot find user "
							+ SessionManager.getCurrentSessionUserId()
							+ "'s My Workspace site.");
				}

				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {
					if (view.equals(rb.getString("java.allmy"))) {
						view = null;
						// add my workspace if any
						if (userWorkspaceSite != null) {
							if (search != null) {
								if (userId.indexOf(search) != -1) {
									size++;
								}
							} else {
								size++;
							}
						}
						size += SiteService
								.countSites(
										org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
										null, search, null);
					} else if (view.equalsIgnoreCase(rb
							.getString("java.gradtools"))) {
						// search for gradtools sites
						size += SiteService
								.countSites(
										org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
										state
												.getAttribute(GRADTOOLS_SITE_TYPES),
										search, null);
					} else {
						// search for specific type of sites
						size += SiteService
								.countSites(
										org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
										view, search, null);
					}
				}
			}
		}
		// for SiteInfo list page
		else if (state.getAttribute(STATE_TEMPLATE_INDEX).toString().equals(
				"12")) {
			Collection l = (Collection) state.getAttribute(STATE_PARTICIPANT_LIST);
			size = (l != null) ? l.size() : 0;
		}
		return size;

	} // sizeResources

	/**
	 * {@inheritDoc}
	 */
	protected List readResourcesPage(SessionState state, int first, int last) {
		String search = StringUtil.trimToNull((String) state
				.getAttribute(STATE_SEARCH));

		// if called from the site list page
		if (((String) state.getAttribute(STATE_TEMPLATE_INDEX)).equals("0")) {
			// get sort type
			SortType sortType = null;
			String sortBy = (String) state.getAttribute(SORTED_BY);
			boolean sortAsc = (new Boolean((String) state
					.getAttribute(SORTED_ASC))).booleanValue();
			if (sortBy.equals(SortType.TITLE_ASC.toString())) {
				sortType = sortAsc ? SortType.TITLE_ASC : SortType.TITLE_DESC;
			} else if (sortBy.equals(SortType.TYPE_ASC.toString())) {
				sortType = sortAsc ? SortType.TYPE_ASC : SortType.TYPE_DESC;
			} else if (sortBy.equals(SortType.CREATED_BY_ASC.toString())) {
				sortType = sortAsc ? SortType.CREATED_BY_ASC
						: SortType.CREATED_BY_DESC;
			} else if (sortBy.equals(SortType.CREATED_ON_ASC.toString())) {
				sortType = sortAsc ? SortType.CREATED_ON_ASC
						: SortType.CREATED_ON_DESC;
			} else if (sortBy.equals(SortType.PUBLISHED_ASC.toString())) {
				sortType = sortAsc ? SortType.PUBLISHED_ASC
						: SortType.PUBLISHED_DESC;
			}

			if (SecurityService.isSuperUser()) {
				// admin-type of user
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {
					if (view.equals(rb.getString("java.allmy"))) {
						// search for non-user sites, using the
						// criteria
						return SiteService
								.getSites(
										org.sakaiproject.site.api.SiteService.SelectionType.NON_USER,
										null, search, null, sortType,
										new PagingPosition(first, last));
					} else if (view.equalsIgnoreCase(rb.getString("java.my"))) {
						// search for a specific user site for
						// the particular user id in the
						// criteria - exact match only
						List rv = new Vector();
						try {
							Site userSite = SiteService.getSite(SiteService
									.getUserSiteId(search));
							rv.add(userSite);
						} catch (IdUnusedException e) {
						}

						return rv;
					} else if (view.equalsIgnoreCase(rb
							.getString("java.gradtools"))) {
						// search for gradtools sites
						return SiteService
								.getSites(
										org.sakaiproject.site.api.SiteService.SelectionType.NON_USER,
										state
												.getAttribute(GRADTOOLS_SITE_TYPES),
										search, null, sortType,
										new PagingPosition(first, last));
					} else {
						// search for a specific site
						return SiteService
								.getSites(
										org.sakaiproject.site.api.SiteService.SelectionType.ANY,
										view, search, null, sortType,
										new PagingPosition(first, last));
					}
				}
			} else {
				List rv = new Vector();
				Site userWorkspaceSite = null;
				String userId = SessionManager.getCurrentSessionUserId();

				try {
					userWorkspaceSite = SiteService.getSite(SiteService
							.getUserSiteId(userId));
				} catch (IdUnusedException e) {
					M_log.warn("Cannot find user "
							+ SessionManager.getCurrentSessionUserId()
							+ "'s My Workspace site.");
				}
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {
					if (view.equals(rb.getString("java.allmy"))) {
						view = null;
						// add my workspace if any
						if (userWorkspaceSite != null) {
							if (search != null) {
								if (userId.indexOf(search) != -1) {
									rv.add(userWorkspaceSite);
								}
							} else {
								rv.add(userWorkspaceSite);
							}
						}
						rv
								.addAll(SiteService
										.getSites(
												org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
												null, search, null, sortType,
												new PagingPosition(first, last)));
					} else if (view.equalsIgnoreCase(rb
							.getString("java.gradtools"))) {
						// search for a specific user site for
						// the particular user id in the
						// criteria - exact match only
						rv
								.addAll(SiteService
										.getSites(
												org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
												state
														.getAttribute(GRADTOOLS_SITE_TYPES),
												search, null, sortType,
												new PagingPosition(first, last)));

					} else {
						rv
								.addAll(SiteService
										.getSites(
												org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
												view, search, null, sortType,
												new PagingPosition(first, last)));
					}
				}

				return rv;

			}
		}
		// if in Site Info list view
		else if (state.getAttribute(STATE_TEMPLATE_INDEX).toString().equals(
				"12")) {
			List participants = (state.getAttribute(STATE_PARTICIPANT_LIST) != null) ? collectionToList((Collection) state.getAttribute(STATE_PARTICIPANT_LIST)): new Vector();
			String sortedBy = (String) state.getAttribute(SORTED_BY);
			String sortedAsc = (String) state.getAttribute(SORTED_ASC);
			Iterator sortedParticipants = null;
			if (sortedBy != null) {
				sortedParticipants = new SortedIterator(participants
						.iterator(), new SiteComparator(sortedBy,
						sortedAsc));
				participants.clear();
				while (sortedParticipants.hasNext()) {
					participants.add(sortedParticipants.next());
				}
			}
			PagingPosition page = new PagingPosition(first, last);
			page.validate(participants.size());
			participants = participants.subList(page.getFirst() - 1, page.getLast());

			return participants;
		}

		return null;

	} // readResourcesPage

	/**
	 * get the selected tool ids from import sites
	 */
	private boolean select_import_tools(ParameterParser params,
			SessionState state) {
		// has the user selected any tool for importing?
		boolean anyToolSelected = false;

		Hashtable importTools = new Hashtable();

		// the tools for current site
		List selectedTools = (List) state
				.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST); // String
		// toolId's
		for (int i = 0; i < selectedTools.size(); i++) {
			// any tools chosen from import sites?
			String toolId = (String) selectedTools.get(i);
			if (params.getStrings(toolId) != null) {
				importTools.put(toolId, new ArrayList(Arrays.asList(params
						.getStrings(toolId))));
				if (!anyToolSelected) {
					anyToolSelected = true;
				}
			}
		}

		state.setAttribute(STATE_IMPORT_SITE_TOOL, importTools);

		return anyToolSelected;

	} // select_import_tools

	/**
	 * Is it from the ENW edit page?
	 * 
	 * @return ture if the process went through the ENW page; false, otherwise
	 */
	private boolean fromENWModifyView(SessionState state) {
		boolean fromENW = false;
		List oTools = (List) state
				.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);

		List toolList = (List) state
				.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		for (int i = 0; i < toolList.size() && !fromENW; i++) {
			String toolId = (String) toolList.get(i);
			if (toolId.equals("sakai.mailbox")
					|| toolId.indexOf("sakai.news") != -1
					|| toolId.indexOf("sakai.iframe") != -1) {
				if (oTools == null) {
					// if during site creation proces
					fromENW = true;
				} else if (!oTools.contains(toolId)) {
					// if user is adding either EmailArchive tool, News tool or
					// Web Content tool, go to the Customize page for the tool
					fromENW = true;
				}
			}
		}
		return fromENW;
	}

	/**
	 * doNew_site is called when the Site list tool bar New... button is clicked
	 * 
	 */
	public void doNew_site(RunData data) throws Exception {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// start clean
		cleanState(state);

		List siteTypes = (List) state.getAttribute(STATE_SITE_TYPES);
		if (siteTypes != null) {
			if (siteTypes.size() == 1) {
				String siteType = (String) siteTypes.get(0);
				if (!siteType.equals(ServerConfigurationService.getString(
						"courseSiteType", (String) state.getAttribute(STATE_COURSE_SITE_TYPE)))) {
					// if only one site type is allowed and the type isn't
					// course type
					// skip the select site type step
					setNewSiteType(state, siteType);
					state.setAttribute(STATE_TEMPLATE_INDEX, "2");
				} else {
					state.setAttribute(STATE_TEMPLATE_INDEX, "1");
				}
			} else {
				state.setAttribute(STATE_TEMPLATE_INDEX, "1");
			}
		}

	} // doNew_site

	/**
	 * doMenu_site_delete is called when the Site list tool bar Delete button is
	 * clicked
	 * 
	 */
	public void doMenu_site_delete(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		if (params.getStrings("selectedMembers") == null) {
			addAlert(state, rb.getString("java.nosites"));
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			return;
		}
		String[] removals = (String[]) params.getStrings("selectedMembers");
		state.setAttribute(STATE_SITE_REMOVALS, removals);

		// present confirm delete template
		state.setAttribute(STATE_TEMPLATE_INDEX, "8");

	} // doMenu_site_delete

	public void doSite_delete_confirmed(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		if (params.getStrings("selectedMembers") == null) {
			M_log
					.warn("SiteAction.doSite_delete_confirmed selectedMembers null");
			state.setAttribute(STATE_TEMPLATE_INDEX, "0"); // return to the
			// site list
			return;
		}
		List chosenList = new ArrayList(Arrays.asList(params
				.getStrings("selectedMembers"))); // Site id's of checked
		// sites
		if (!chosenList.isEmpty()) {
			for (ListIterator i = chosenList.listIterator(); i.hasNext();) {
				String id = (String) i.next();
				String site_title = NULL_STRING;
				try {
					site_title = SiteService.getSite(id).getTitle();
				} catch (IdUnusedException e) {
					M_log
							.warn("SiteAction.doSite_delete_confirmed - IdUnusedException "
									+ id);
					addAlert(state, rb.getString("java.sitewith") + " " + id
							+ " " + rb.getString("java.couldnt") + " ");
				}
				if (SiteService.allowRemoveSite(id)) {

					try {
						Site site = SiteService.getSite(id);
						site_title = site.getTitle();
						SiteService.removeSite(site);
					} catch (IdUnusedException e) {
						M_log
								.warn("SiteAction.doSite_delete_confirmed - IdUnusedException "
										+ id);
						addAlert(state, rb.getString("java.sitewith") + " "
								+ site_title + "(" + id + ") "
								+ rb.getString("java.couldnt") + " ");
					} catch (PermissionException e) {
						M_log
								.warn("SiteAction.doSite_delete_confirmed -  PermissionException, site "
										+ site_title + "(" + id + ").");
						addAlert(state, site_title + " "
								+ rb.getString("java.dontperm") + " ");
					}
				} else {
					M_log
							.warn("SiteAction.doSite_delete_confirmed -  allowRemoveSite failed for site "
									+ id);
					addAlert(state, site_title + " "
							+ rb.getString("java.dontperm") + " ");
				}
			}
		}
		state.setAttribute(STATE_TEMPLATE_INDEX, "0"); // return to the site
		// list

		// TODO: hard coding this frame id is fragile, portal dependent, and
		// needs to be fixed -ggolden
		// schedulePeerFrameRefresh("sitenav");
		scheduleTopRefresh();

	} // doSite_delete_confirmed

	/**
	 * get the Site object based on SessionState attribute values
	 * 
	 * @return Site object related to current state; null if no such Site object
	 *         could be found
	 */
	protected Site getStateSite(SessionState state) {
		Site site = null;

		if (state.getAttribute(STATE_SITE_INSTANCE_ID) != null) {
			try {
				site = SiteService.getSite((String) state
						.getAttribute(STATE_SITE_INSTANCE_ID));
			} catch (Exception ignore) {
			}
		}
		return site;

	} // getStateSite

	/**
	 * get the Group object based on SessionState attribute values
	 * 
	 * @return Group object related to current state; null if no such Group
	 *         object could be found
	 */
	protected Group getStateGroup(SessionState state) {
		Group group = null;
		Site site = getStateSite(state);

		if (site != null && state.getAttribute(STATE_GROUP_INSTANCE_ID) != null) {
			try {
				group = site.getGroup((String) state
						.getAttribute(STATE_GROUP_INSTANCE_ID));
			} catch (Exception ignore) {
			}
		}
		return group;

	} // getStateGroup

	/**
	 * do called when "eventSubmit_do" is in the request parameters to c is
	 * called from site list menu entry Revise... to get a locked site as
	 * editable and to go to the correct template to begin DB version of writes
	 * changes to disk at site commit whereas XML version writes at server
	 * shutdown
	 */
	public void doGet_site(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		// check form filled out correctly
		if (params.getStrings("selectedMembers") == null) {
			addAlert(state, rb.getString("java.nosites"));
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			return;
		}
		List chosenList = new ArrayList(Arrays.asList(params
				.getStrings("selectedMembers"))); // Site id's of checked
		// sites
		String siteId = "";
		if (!chosenList.isEmpty()) {
			if (chosenList.size() != 1) {
				addAlert(state, rb.getString("java.please"));
				state.setAttribute(STATE_TEMPLATE_INDEX, "0");
				return;
			}

			siteId = (String) chosenList.get(0);
			getReviseSite(state, siteId);

			state.setAttribute(SORTED_BY, SORTED_BY_PARTICIPANT_NAME);
			state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());
		}
		
		// reset the paging info
		resetPaging(state);

		if (((String) state.getAttribute(STATE_SITE_MODE))
				.equalsIgnoreCase(SITE_MODE_SITESETUP)) {
			state.setAttribute(STATE_PAGESIZE_SITESETUP, state
					.getAttribute(STATE_PAGESIZE));
		}

		Hashtable h = (Hashtable) state.getAttribute(STATE_PAGESIZE_SITEINFO);
		if (!h.containsKey(siteId)) {
			// when first entered Site Info, set the participant list size to
			// 200 as default
			state.setAttribute(STATE_PAGESIZE, new Integer(200));

			// update
			h.put(siteId, new Integer(200));
			state.setAttribute(STATE_PAGESIZE_SITEINFO, h);
		} else {
			// restore the page size in site info tool
			state.setAttribute(STATE_PAGESIZE, h.get(siteId));
		}

	} // doGet_site

	/**
	 * do called when "eventSubmit_do" is in the request parameters to c
	 */
	public void doMenu_site_reuse(RunData data) throws Exception {
		// called from chef_site-list.vm after a site has been selected from
		// list
		// create a new Site object based on selected Site object and put in
		// state
		//
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_TEMPLATE_INDEX, "1");

	} // doMenu_site_reuse

	/**
	 * do called when "eventSubmit_do" is in the request parameters to c
	 */
	public void doMenu_site_revise(RunData data) throws Exception {
		// called from chef_site-list.vm after a site has been selected from
		// list
		// get site as Site object, check SiteCreationStatus and SiteType of
		// site, put in state, and set STATE_TEMPLATE_INDEX correctly
		// set mode to state_mode_site_type
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_TEMPLATE_INDEX, "1");

	} // doMenu_site_revise

	/**
	 * doView_sites is called when "eventSubmit_doView_sites" is in the request
	 * parameters
	 */
	public void doView_sites(RunData data) throws Exception {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		state.setAttribute(STATE_VIEW_SELECTED, params.getString("view"));
		state.setAttribute(STATE_TEMPLATE_INDEX, "0");

		resetPaging(state);

	} // doView_sites

	/**
	 * do called when "eventSubmit_do" is in the request parameters to c
	 */
	public void doView(RunData data) throws Exception {
		// called from chef_site-list.vm with a select option to build query of
		// sites
		// 
		// 
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_TEMPLATE_INDEX, "1");
	} // doView

	/**
	 * do called when "eventSubmit_do" is in the request parameters to c
	 */
	public void doSite_type(RunData data) {
		/*
		 * for measuring how long it takes to load sections java.util.Date date =
		 * new java.util.Date(); M_log.debug("***1. start preparing
		 * section:"+date);
		 */
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();
		int index = Integer.valueOf(params.getString("template-index"))
				.intValue();
		actionForTemplate("continue", index, params, state);

		String type = StringUtil.trimToNull(params.getString("itemType"));

		int totalSteps = 0;

		if (type == null) {
			addAlert(state, rb.getString("java.select") + " ");
		} else {
			setNewSiteType(state, type);

			if (type.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				User user = UserDirectoryService.getCurrentUser();
				String currentUserId = user.getEid();

				String userId = params.getString("userId");
				if (userId == null || "".equals(userId)) {
					userId = currentUserId;
				} else {
					// implies we are trying to pick sections owned by other
					// users. Currently "select section by user" page only
					// take one user per sitte request - daisy's note 1
					ArrayList<String> list = new ArrayList();
					list.add(userId);
					state.setAttribute(STATE_CM_AUTHORIZER_LIST, list);
				}
				state.setAttribute(STATE_INSTRUCTOR_SELECTED, userId);

				String academicSessionEid = params.getString("selectTerm");
				AcademicSession t = cms.getAcademicSession(academicSessionEid);
				state.setAttribute(STATE_TERM_SELECTED, t);
				if (t != null) {
					List sections = prepareCourseAndSectionListing(userId, t
							.getEid(), state);

					isFutureTermSelected(state);

					if (sections != null && sections.size() > 0) {
						state.setAttribute(STATE_TERM_COURSE_LIST, sections);
						state.setAttribute(STATE_TEMPLATE_INDEX, "36");
						state.setAttribute(STATE_AUTO_ADD, Boolean.TRUE);
					} else {
						state.removeAttribute(STATE_TERM_COURSE_LIST);
						
						Boolean skipCourseSectionSelection = ServerConfigurationService.getBoolean("wsetup.skipCourseSectionSelection", Boolean.FALSE);
						if (!skipCourseSectionSelection.booleanValue() && courseManagementIsImplemented())
						{
							state.setAttribute(STATE_TEMPLATE_INDEX, "53");
						}
						else
						{
							state.setAttribute(STATE_TEMPLATE_INDEX, "37");
						}		
					}

				} else { // not course type
					state.setAttribute(STATE_TEMPLATE_INDEX, "37");
					totalSteps = 5;
				}
			} else if (type.equals("project")) {
				totalSteps = 4;
				state.setAttribute(STATE_TEMPLATE_INDEX, "2");
			} else if (type.equals(SITE_TYPE_GRADTOOLS_STUDENT)) {
				// if a GradTools site use pre-defined site info and exclude
				// from public listing
				SiteInfo siteInfo = new SiteInfo();
				if (state.getAttribute(STATE_SITE_INFO) != null) {
					siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
				}
				User currentUser = UserDirectoryService.getCurrentUser();
				siteInfo.title = rb.getString("java.grad") + " - "
						+ currentUser.getId();
				siteInfo.description = rb.getString("java.gradsite") + " "
						+ currentUser.getDisplayName();
				siteInfo.short_description = rb.getString("java.grad") + " - "
						+ currentUser.getId();
				siteInfo.include = false;
				state.setAttribute(STATE_SITE_INFO, siteInfo);

				// skip directly to confirm creation of site
				state.setAttribute(STATE_TEMPLATE_INDEX, "42");
			} else {
				state.setAttribute(STATE_TEMPLATE_INDEX, "2");
			}
		}

		if (state.getAttribute(SITE_CREATE_TOTAL_STEPS) == null) {
			state
					.setAttribute(SITE_CREATE_TOTAL_STEPS, new Integer(
							totalSteps));
		}

		if (state.getAttribute(SITE_CREATE_CURRENT_STEP) == null) {
			state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer(1));
		}

	} // doSite_type

	/**
	 * Determine whether the selected term is considered of "future term"
	 * @param state
	 * @param t
	 */
	private void isFutureTermSelected(SessionState state) {
		AcademicSession t = (AcademicSession) state.getAttribute(STATE_TERM_SELECTED);
		int weeks = 0;
		Calendar c = (Calendar) Calendar.getInstance().clone();
		try {
			weeks = Integer
					.parseInt(ServerConfigurationService
							.getString(
									"roster.available.weeks.before.term.start",
									"0"));
			c.add(Calendar.DATE, weeks * 7);
		} catch (Exception ignore) {
		}

		if (c.getTimeInMillis() < t.getStartDate().getTime()) {
			// if a future term is selected
			state.setAttribute(STATE_FUTURE_TERM_SELECTED,
					Boolean.TRUE);
		} else {
			state.setAttribute(STATE_FUTURE_TERM_SELECTED,
					Boolean.FALSE);
		}
	}

	public void doChange_user(RunData data) {
		doSite_type(data);
	} // doChange_user

	/**
	 * cleanEditGroupParams clean the state parameters used by editing group
	 * process
	 * 
	 */
	public void cleanEditGroupParams(SessionState state) {
		state.removeAttribute(STATE_GROUP_INSTANCE_ID);
		state.removeAttribute(STATE_GROUP_TITLE);
		state.removeAttribute(STATE_GROUP_DESCRIPTION);
		state.removeAttribute(STATE_GROUP_MEMBERS);
		state.removeAttribute(STATE_GROUP_REMOVE);

	} // cleanEditGroupParams

	/**
	 * doGroup_edit
	 * 
	 */
	public void doGroup_update(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		Set gMemberSet = (Set) state.getAttribute(STATE_GROUP_MEMBERS);
		Site site = getStateSite(state);

		String title = StringUtil.trimToNull(params.getString(rb
				.getString("group.title")));
		state.setAttribute(STATE_GROUP_TITLE, title);

		String description = StringUtil.trimToZero(params.getString(rb
				.getString("group.description")));
		state.setAttribute(STATE_GROUP_DESCRIPTION, description);

		boolean found = false;
		String option = params.getString("option");

		if (option.equals("add")) {
			// add selected members into it
			if (params.getStrings("generallist") != null) {
				List addMemberIds = new ArrayList(Arrays.asList(params
						.getStrings("generallist")));
				for (int i = 0; i < addMemberIds.size(); i++) {
					String aId = (String) addMemberIds.get(i);
					found = false;
					for (Iterator iSet = gMemberSet.iterator(); !found
							&& iSet.hasNext();) {
						if (((Member) iSet.next()).getUserEid().equals(aId)) {
							found = true;
						}
					}
					if (!found) {
						try {
							User u = UserDirectoryService.getUser(aId);
							gMemberSet.add(site.getMember(u.getId()));
						} catch (UserNotDefinedException e) {
							try {
								User u2 = UserDirectoryService
										.getUserByEid(aId);
								gMemberSet.add(site.getMember(u2.getId()));
							} catch (UserNotDefinedException ee) {
								M_log.warn(this + ee.getMessage() + aId);
							}
						}
					}
				}
			}
			state.setAttribute(STATE_GROUP_MEMBERS, gMemberSet);
		} else if (option.equals("remove")) {
			// update the group member list by remove selected members from it
			if (params.getStrings("grouplist") != null) {
				List removeMemberIds = new ArrayList(Arrays.asList(params
						.getStrings("grouplist")));
				for (int i = 0; i < removeMemberIds.size(); i++) {
					found = false;
					for (Iterator iSet = gMemberSet.iterator(); !found
							&& iSet.hasNext();) {
						Member mSet = (Member) iSet.next();
						if (mSet.getUserId().equals(
								(String) removeMemberIds.get(i))) {
							found = true;
							gMemberSet.remove(mSet);
						}
					}
				}
			}
			state.setAttribute(STATE_GROUP_MEMBERS, gMemberSet);
		} else if (option.equals("cancel")) {
			// cancel from the update the group member process
			doCancel(data);
			cleanEditGroupParams(state);

		} else if (option.equals("save")) {
			Group group = null;
			if (site != null
					&& state.getAttribute(STATE_GROUP_INSTANCE_ID) != null) {
				try {
					group = site.getGroup((String) state
							.getAttribute(STATE_GROUP_INSTANCE_ID));
				} catch (Exception ignore) {
				}
			}

			if (title == null) {
				addAlert(state, rb.getString("editgroup.titlemissing"));
			} else {
				if (group == null) {
					// when adding a group, check whether the group title has
					// been used already
					boolean titleExist = false;
					for (Iterator iGroups = site.getGroups().iterator(); !titleExist
							&& iGroups.hasNext();) {
						Group iGroup = (Group) iGroups.next();
						if (iGroup.getTitle().equals(title)) {
							// found same title
							titleExist = true;
						}
					}
					if (titleExist) {
						addAlert(state, rb.getString("group.title.same"));
					}
				}
			}

			if (state.getAttribute(STATE_MESSAGE) == null) {
				if (group == null) {
					// adding new group
					group = site.addGroup();
					group.getProperties().addProperty(
							GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
				}

				if (group != null) {
					group.setTitle(title);
					group.setDescription(description);

					// save the modification to group members

					// remove those no longer included in the group
					Set members = group.getMembers();
					for (Iterator iMembers = members.iterator(); iMembers
							.hasNext();) {
						found = false;
						String mId = ((Member) iMembers.next()).getUserId();
						for (Iterator iMemberSet = gMemberSet.iterator(); !found
								&& iMemberSet.hasNext();) {
							if (mId.equals(((Member) iMemberSet.next())
									.getUserId())) {
								found = true;
							}

						}
						if (!found) {
							group.removeMember(mId);
						}
					}

					// add those seleted members
					for (Iterator iMemberSet = gMemberSet.iterator(); iMemberSet
							.hasNext();) {
						String memberId = ((Member) iMemberSet.next())
								.getUserId();
						if (group.getUserRole(memberId) == null) {
							Role r = site.getUserRole(memberId);
							Member m = site.getMember(memberId);
							// for every member added through the "Manage
							// Groups" interface, he should be defined as
							// non-provided
							group.addMember(memberId, r != null ? r.getId()
									: "", m != null ? m.isActive() : true,
									false);
						}
					}

					if (state.getAttribute(STATE_MESSAGE) == null) {
						try {
							SiteService.save(site);
						} catch (IdUnusedException e) {
						} catch (PermissionException e) {
						}

						// return to group list view
						state.setAttribute(STATE_TEMPLATE_INDEX, "49");
						cleanEditGroupParams(state);
					}
				}
			}
		}

	} // doGroup_updatemembers

	/**
	 * doGroup_new
	 * 
	 */
	public void doGroup_new(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		if (state.getAttribute(STATE_GROUP_TITLE) == null) {
			state.setAttribute(STATE_GROUP_TITLE, "");
		}
		if (state.getAttribute(STATE_GROUP_DESCRIPTION) == null) {
			state.setAttribute(STATE_GROUP_DESCRIPTION, "");
		}
		if (state.getAttribute(STATE_GROUP_MEMBERS) == null) {
			state.setAttribute(STATE_GROUP_MEMBERS, new HashSet());
		}
		state.setAttribute(STATE_TEMPLATE_INDEX, "50");

	} // doGroup_new

	/**
	 * doGroup_edit
	 * 
	 */
	public void doGroup_edit(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String groupId = data.getParameters().getString("groupId");
		state.setAttribute(STATE_GROUP_INSTANCE_ID, groupId);

		Site site = getStateSite(state);
		if (site != null) {
			Group g = site.getGroup(groupId);
			if (g != null) {
				if (state.getAttribute(STATE_GROUP_TITLE) == null) {
					state.setAttribute(STATE_GROUP_TITLE, g.getTitle());
				}
				if (state.getAttribute(STATE_GROUP_DESCRIPTION) == null) {
					state.setAttribute(STATE_GROUP_DESCRIPTION, g
							.getDescription());
				}
				if (state.getAttribute(STATE_GROUP_MEMBERS) == null) {
					// double check the member existance
					Set gMemberSet = g.getMembers();
					Set rvGMemberSet = new HashSet();
					for (Iterator iSet = gMemberSet.iterator(); iSet.hasNext();) {
						Member member = (Member) iSet.next();
						try {
							UserDirectoryService.getUser(member.getUserId());
							((Set) rvGMemberSet).add(member);
						} catch (UserNotDefinedException e) {
							// cannot find user
							M_log.warn(this + rb.getString("user.notdefined")
									+ member.getUserId());
						}
					}
					state.setAttribute(STATE_GROUP_MEMBERS, rvGMemberSet);
				}
			}
		}
		state.setAttribute(STATE_TEMPLATE_INDEX, "50");

	} // doGroup_edit

	/**
	 * doGroup_remove_prep Go to confirmation page before deleting group(s)
	 * 
	 */
	public void doGroup_remove_prep(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String[] removeGroupIds = data.getParameters().getStrings(
				"removeGroups");

		if (removeGroupIds.length > 0) {
			state.setAttribute(STATE_GROUP_REMOVE, removeGroupIds);
			state.setAttribute(STATE_TEMPLATE_INDEX, "51");
		}

	} // doGroup_remove_prep

	/**
	 * doGroup_remove_confirmed Delete selected groups after confirmation
	 * 
	 */
	public void doGroup_remove_confirmed(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String[] removeGroupIds = (String[]) state
				.getAttribute(STATE_GROUP_REMOVE);

		Site site = getStateSite(state);
		for (int i = 0; i < removeGroupIds.length; i++) {
			if (site != null) {
				Group g = site.getGroup(removeGroupIds[i]);
				if (g != null) {
					site.removeGroup(g);
				}
			}
		}
		try {
			SiteService.save(site);
		} catch (IdUnusedException e) {
			addAlert(state, rb.getString("editgroup.site.notfound.alert"));
		} catch (PermissionException e) {
			addAlert(state, rb.getString("editgroup.site.permission.alert"));
		}

		if (state.getAttribute(STATE_MESSAGE) == null) {
			cleanEditGroupParams(state);
			state.setAttribute(STATE_TEMPLATE_INDEX, "49");
		}

	} // doGroup_remove_confirmed

	/**
	 * doMenu_edit_site_info The menu choice to enter group view
	 * 
	 */
	public void doMenu_group(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// reset sort criteria
		state.setAttribute(SORTED_BY, rb.getString("group.title"));
		state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

		state.setAttribute(STATE_TEMPLATE_INDEX, "49");

	} // doMenu_group
	
	/**
	 * 
	 */
	private void removeSection(SessionState state, ParameterParser params)
	{
		// v2.4 - added by daisyf
		// RemoveSection - remove any selected course from a list of
		// provider courses
		// check if any section need to be removed
		removeAnyFlagedSection(state, params);
		
		SiteInfo siteInfo = new SiteInfo();
		if (state.getAttribute(STATE_SITE_INFO) != null) {
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
		}

		List providerChosenList = (List) state
				.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		collectNewSiteInfo(siteInfo, state, params, providerChosenList);
		// next step
		//state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer(2));
	}

	/**
	 * dispatch to different functions based on the option value in the
	 * parameter
	 */
	public void doManual_add_course(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String option = params.getString("option");
		if (option.equalsIgnoreCase("change") || option.equalsIgnoreCase("add")) {
			readCourseSectionInfo(state, params);

			String uniqname = StringUtil.trimToNull(params
					.getString("uniqname"));
			state.setAttribute(STATE_SITE_QUEST_UNIQNAME, uniqname);

			updateSiteInfo(params, state);

			if (option.equalsIgnoreCase("add")) {

				if (state.getAttribute(STATE_FUTURE_TERM_SELECTED) != null
						&& !((Boolean) state
								.getAttribute(STATE_FUTURE_TERM_SELECTED))
								.booleanValue()) {
					// if a future term is selected, do not check authorization
					// uniqname
					if (uniqname == null) {
						addAlert(state, rb.getString("java.author")
								+ " "
								+ ServerConfigurationService
										.getString("officialAccountName")
								+ ". ");
					} else {
						// in case of multiple instructors
						List instructors = new ArrayList(Arrays.asList(uniqname.split(",")));
						for (Iterator iInstructors = instructors.iterator(); iInstructors.hasNext();)
						{
							try {
								UserDirectoryService.getUserByEid(StringUtil.trimToZero((String) iInstructors.next()));
							} catch (UserNotDefinedException e) {
								addAlert(
										state,
										rb.getString("java.validAuthor1")
												+ " "
												+ ServerConfigurationService
														.getString("officialAccountName")
												+ " "
												+ rb.getString("java.validAuthor2"));
							}
						}
					}
				}
				if (state.getAttribute(STATE_MESSAGE) == null) {
					if (getStateSite(state) == null) {
						state.setAttribute(STATE_TEMPLATE_INDEX, "2");
					} else {
						state.setAttribute(STATE_TEMPLATE_INDEX, "44");
					}
				}
				updateCurrentStep(state, true);
			}
		} else if (option.equalsIgnoreCase("back")) {
			doBack(data);
			if (state.getAttribute(STATE_MESSAGE) == null) {
				updateCurrentStep(state, false);
			}
		} else if (option.equalsIgnoreCase("cancel")) {
			if (getStateSite(state) == null) {
				doCancel_create(data);
			} else {
				doCancel(data);
			}
		} else if (option.equalsIgnoreCase("removeSection"))
		{
			// remove selected section
			removeSection(state, params);
		}

	} // doManual_add_course
	

	/**
	 * dispatch to different functions based on the option value in the
	 * parameter
	 */
	public void doSite_information(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String option = params.getString("option");
		if (option.equalsIgnoreCase("continue"))
		{
			doContinue(data);
		} else if (option.equalsIgnoreCase("back")) {
			doBack(data);
		} else if (option.equalsIgnoreCase("cancel")) {
			if (getStateSite(state) == null) {
				doCancel_create(data);
			} else {
				doCancel(data);
			}
		} else if (option.equalsIgnoreCase("removeSection"))
		{
			// remove selected section
			removeSection(state, params);
		}

	} // doSite_information

	/**
	 * read the input information of subject, course and section in the manual
	 * site creation page
	 */
	private void readCourseSectionInfo(SessionState state,
			ParameterParser params) {
		String option = params.getString("option");
		int oldNumber = 1;
		if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
			oldNumber = ((Integer) state
					.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER)).intValue();
		}

		// read the user input
		int validInputSites = 0;
		boolean validInput = true;
		List multiCourseInputs = new Vector();
		for (int i = 0; i < oldNumber; i++) {
			List requiredFields = sectionFieldProvider.getRequiredFields();
			List aCourseInputs = new Vector();
			int emptyInputNum = 0;

			// iterate through all required fields
			for (int k = 0; k < requiredFields.size(); k++) {
				SectionField sectionField = (SectionField) requiredFields
						.get(k);
				String fieldLabel = sectionField.getLabelKey();
				String fieldInput = StringUtil.trimToZero(params
						.getString(fieldLabel + i));
				sectionField.setValue(fieldInput);
				aCourseInputs.add(sectionField);
				if (fieldInput.length() == 0) {
					// is this an empty String input?
					emptyInputNum++;
				}
			}

			// is any input invalid?
			if (emptyInputNum == 0) {
				// valid if all the inputs are not empty
				multiCourseInputs.add(validInputSites++, aCourseInputs);
			} else if (emptyInputNum == requiredFields.size()) {
				// ignore if all inputs are empty
				if (option.equalsIgnoreCase("change"))
				{
					multiCourseInputs.add(validInputSites++, aCourseInputs);
				}
			} else {
				if (state.getAttribute(STATE_FUTURE_TERM_SELECTED) != null
						&& ((Boolean) state
								.getAttribute(STATE_FUTURE_TERM_SELECTED))
								.booleanValue()) {
					// if future term selected, then not all fields are required
					// %%%
				} else {
					validInput = false;
				}
				multiCourseInputs.add(validInputSites++, aCourseInputs);
			}
		}

		// how many more course/section to include in the site?
		if (option.equalsIgnoreCase("change")) {
			if (params.getString("number") != null) {
				int newNumber = Integer.parseInt(params.getString("number"));
				state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, new Integer(oldNumber + newNumber));

				List requiredFields = sectionFieldProvider.getRequiredFields();
				for (int j = 0; j < newNumber; j++) {
					// add a new course input
					List aCourseInputs = new Vector();
					// iterate through all required fields
					for (int m = 0; m < requiredFields.size(); m++) {
						aCourseInputs = sectionFieldProvider.getRequiredFields();
					}
					multiCourseInputs.add(aCourseInputs);
				}
			}
		}

		state.setAttribute(STATE_MANUAL_ADD_COURSE_FIELDS, multiCourseInputs);

		if (!option.equalsIgnoreCase("change")) {
			if (!validInput || validInputSites == 0) {
				// not valid input
				addAlert(state, rb.getString("java.miss"));
			} 
			// valid input, adjust the add course number
			state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, new Integer(	validInputSites));
		}

		// set state attributes
		state.setAttribute(FORM_ADDITIONAL, StringUtil.trimToZero(params
				.getString("additional")));

		SiteInfo siteInfo = new SiteInfo();
		if (state.getAttribute(STATE_SITE_INFO) != null) {
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
		}
		List providerCourseList = (List) state
				.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		// store the manually requested sections in one site property
		if ((providerCourseList == null || providerCourseList.size() == 0)
				&& multiCourseInputs.size() > 0) {
			AcademicSession t = (AcademicSession) state
					.getAttribute(STATE_TERM_SELECTED);
			String sectionEid = sectionFieldProvider.getSectionEid(t.getEid(),
					(List) multiCourseInputs.get(0));
			// default title
			String title = sectionEid;
			try {
				title = cms.getSection(sectionEid).getTitle();
			} catch (Exception e) {
				// ignore
			}
			siteInfo.title = title;
		}
		state.setAttribute(STATE_SITE_INFO, siteInfo);

	} // readCourseSectionInfo

	/**
	 * set the site type for new site
	 * 
	 * @param type
	 *            The type String
	 */
	private void setNewSiteType(SessionState state, String type) {
		state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME);

		// start out with fresh site information
		SiteInfo siteInfo = new SiteInfo();
		siteInfo.site_type = type;
		siteInfo.published = true;
		state.setAttribute(STATE_SITE_INFO, siteInfo);

		// get registered tools list
		Set categories = new HashSet();
		categories.add(type);
		Set toolRegistrations = ToolManager.findTools(categories, null);

		List tools = new Vector();
		SortedIterator i = new SortedIterator(toolRegistrations.iterator(),
				new ToolComparator());
		for (; i.hasNext();) {
			// form a new Tool
			Tool tr = (Tool) i.next();
			MyTool newTool = new MyTool();
			newTool.title = tr.getTitle();
			newTool.id = tr.getId();
			newTool.description = tr.getDescription();

			tools.add(newTool);
		}
		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, tools);

		state.setAttribute(STATE_SITE_TYPE, type);
	}

	/**
	 * Set the field on which to sort the list of students
	 * 
	 */
	public void doSort_roster(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the field on which to sort the student list
		ParameterParser params = data.getParameters();
		String criterion = params.getString("criterion");

		// current sorting sequence
		String asc = "";
		if (!criterion.equals(state.getAttribute(SORTED_BY))) {
			state.setAttribute(SORTED_BY, criterion);
			asc = Boolean.TRUE.toString();
			state.setAttribute(SORTED_ASC, asc);
		} else {
			// current sorting sequence
			asc = (String) state.getAttribute(SORTED_ASC);

			// toggle between the ascending and descending sequence
			if (asc.equals(Boolean.TRUE.toString())) {
				asc = Boolean.FALSE.toString();
			} else {
				asc = Boolean.TRUE.toString();
			}
			state.setAttribute(SORTED_ASC, asc);
		}

	} // doSort_roster

	/**
	 * Set the field on which to sort the list of sites
	 * 
	 */
	public void doSort_sites(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// call this method at the start of a sort for proper paging
		resetPaging(state);

		// get the field on which to sort the site list
		ParameterParser params = data.getParameters();
		String criterion = params.getString("criterion");

		// current sorting sequence
		String asc = "";
		if (!criterion.equals(state.getAttribute(SORTED_BY))) {
			state.setAttribute(SORTED_BY, criterion);
			asc = Boolean.TRUE.toString();
			state.setAttribute(SORTED_ASC, asc);
		} else {
			// current sorting sequence
			asc = (String) state.getAttribute(SORTED_ASC);

			// toggle between the ascending and descending sequence
			if (asc.equals(Boolean.TRUE.toString())) {
				asc = Boolean.FALSE.toString();
			} else {
				asc = Boolean.TRUE.toString();
			}
			state.setAttribute(SORTED_ASC, asc);
		}

		state.setAttribute(SORTED_BY, criterion);

	} // doSort_sites

	/**
	 * doContinue is called when "eventSubmit_doContinue" is in the request
	 * parameters
	 */
	public void doContinue(RunData data) {
		// Put current form data in state and continue to the next template,
		// make any permanent changes
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		int index = Integer.valueOf(params.getString("template-index"))
				.intValue();

		// Let actionForTemplate know to make any permanent changes before
		// continuing to the next template
		String direction = "continue";
		String option = params.getString("option");

		actionForTemplate(direction, index, params, state);
		if (state.getAttribute(STATE_MESSAGE) == null) {
			if (index == 36 && ("add").equals(option)) {
				// this is the Add extra Roster(s) case after a site is created
				state.setAttribute(STATE_TEMPLATE_INDEX, "44");
			} else if (params.getString("continue") != null) {
				state.setAttribute(STATE_TEMPLATE_INDEX, params
						.getString("continue"));
			}
		}
	}// doContinue
	
	/**
	 * handle with continue add new course site options
	 * 
	 */
	public void doContinue_new_course(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		
		String option = data.getParameters().getString("option");
		if (option.equals("continue")) {
			doContinue(data);
		} else if (option.equals("cancel")) {
			doCancel_create(data);
		} else if (option.equals("back")) {
			doBack(data);
		} else if (option.equals("cancel")) {
			doCancel_create(data);
		}
		else if (option.equalsIgnoreCase("change")) {
			// change term
			String termId = params.getString("selectTerm");
			AcademicSession t = cms.getAcademicSession(termId);
			state.setAttribute(STATE_TERM_SELECTED, t);
			isFutureTermSelected(state);
		} else if (option.equalsIgnoreCase("cancel_edit")) {
			// cancel
			doCancel(data);
		} else if (option.equalsIgnoreCase("add")) {
			isFutureTermSelected(state);
			// continue
			doContinue(data);
		}
	} // doContinue_new_course

	/**
	 * doBack is called when "eventSubmit_doBack" is in the request parameters
	 * Pass parameter to actionForTemplate to request action for backward
	 * direction
	 */
	public void doBack(RunData data) {
		// Put current form data in state and return to the previous template
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		int currentIndex = Integer.parseInt((String) state
				.getAttribute(STATE_TEMPLATE_INDEX));
		state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("back"));

		// Let actionForTemplate know not to make any permanent changes before
		// continuing to the next template
		String direction = "back";
		actionForTemplate(direction, currentIndex, params, state);

	}// doBack

	/**
	 * doFinish is called when a site has enough information to be saved as an
	 * unpublished site
	 */
	public void doFinish(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, params
					.getString("continue"));
			int index = Integer.valueOf(params.getString("template-index"))
					.intValue();
			actionForTemplate("continue", index, params, state);

			addNewSite(params, state);

			addFeatures(state);

			Site site = getStateSite(state);

			// for course sites
			String siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (siteType != null && siteType.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				String siteId = site.getId();

				ResourcePropertiesEdit rp = site.getPropertiesEdit();

				AcademicSession term = null;
				if (state.getAttribute(STATE_TERM_SELECTED) != null) {
					term = (AcademicSession) state
							.getAttribute(STATE_TERM_SELECTED);
					rp.addProperty(PROP_SITE_TERM, term.getTitle());
					rp.addProperty(PROP_SITE_TERM_EID, term.getEid());
				}

				// update the site and related realm based on the rosters chosen or requested
				updateCourseSiteSections(state, siteId, rp, term);
			}

			// commit site
			commitSite(site);

			String siteId = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);

			// now that the site exists, we can set the email alias when an
			// Email Archive tool has been selected
			String alias = StringUtil.trimToNull((String) state
					.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			if (alias != null) {
				String channelReference = mailArchiveChannelReference(siteId);
				try {
					AliasService.setAlias(alias, channelReference);
				} catch (IdUsedException ee) {
					addAlert(state, rb.getString("java.alias") + " " + alias
							+ " " + rb.getString("java.exists"));
				} catch (IdInvalidException ee) {
					addAlert(state, rb.getString("java.alias") + " " + alias
							+ " " + rb.getString("java.isinval"));
				} catch (PermissionException ee) {
					addAlert(state, rb.getString("java.addalias") + " ");
				}
			}
			// TODO: hard coding this frame id is fragile, portal dependent, and
			// needs to be fixed -ggolden
			// schedulePeerFrameRefresh("sitenav");
			scheduleTopRefresh();

			resetPaging(state);

			// clean state variables
			cleanState(state);

			state.setAttribute(STATE_TEMPLATE_INDEX, "0");

		}

	}// doFinish

	/**
	 * Update course site and related realm based on the roster chosen or requested
	 * @param state
	 * @param siteId
	 * @param rp
	 * @param term
	 */
	private void updateCourseSiteSections(SessionState state, String siteId, ResourcePropertiesEdit rp, AcademicSession term) {
		// whether this is in the process of editing a site?
		boolean editingSite = ((String)state.getAttribute(STATE_SITE_MODE)).equals(SITE_MODE_SITEINFO)?true:false;
		
		List providerCourseList = (List) state
				.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		int manualAddNumber = 0;
		if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
			manualAddNumber = ((Integer) state
					.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER))
					.intValue();
		}

		List<SectionObject> cmRequestedSections = (List<SectionObject>) state
				.getAttribute(STATE_CM_REQUESTED_SECTIONS);

		List<SectionObject> cmAuthorizerSections = (List<SectionObject>) state
				.getAttribute(STATE_CM_AUTHORIZER_SECTIONS);

		String realm = SiteService.siteReference(siteId);

		if ((providerCourseList != null)
				&& (providerCourseList.size() != 0)) {
			try {
				AuthzGroup realmEdit = AuthzGroupService
						.getAuthzGroup(realm);
				String providerRealm = buildExternalRealm(siteId, state,
						providerCourseList, StringUtil.trimToNull(realmEdit.getProviderGroupId()));
				realmEdit.setProviderGroupId(providerRealm);
				AuthzGroupService.save(realmEdit);
			} catch (GroupNotDefinedException e) {
				M_log.warn(this + " IdUnusedException, not found, or not an AuthzGroup object");
				addAlert(state, rb.getString("java.realm"));
			}
			// catch (AuthzPermissionException e)
			// {
			// M_log.warn(this + " PermissionException, user does not
			// have permission to edit AuthzGroup object.");
			// addAlert(state, rb.getString("java.notaccess"));
			// return;
			// }
			catch (Exception e) {
				addAlert(state, this + rb.getString("java.problem"));
			}

			sendSiteNotification(state, providerCourseList);
		}

		if (manualAddNumber != 0) {
			// set the manual sections to the site property
			String manualSections = rp.getProperty(PROP_SITE_REQUEST_COURSE) != null?rp.getProperty(PROP_SITE_REQUEST_COURSE)+"+":"";

			// manualCourseInputs is a list of a list of SectionField
			List manualCourseInputs = (List) state
					.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);

			// but we want to feed a list of a list of String (input of
			// the required fields)
			for (int j = 0; j < manualAddNumber; j++) {
				manualSections = manualSections.concat(
						sectionFieldProvider.getSectionEid(
								term.getEid(),
								(List) manualCourseInputs.get(j)))
						.concat("+");
			}

			// trim the trailing plus sign
			manualSections = trimTrailingString(manualSections, "+");
			
			rp.addProperty(PROP_SITE_REQUEST_COURSE, manualSections);
			// send request
			sendSiteRequest(state, "new", manualAddNumber, manualCourseInputs, "manual");
		}

		if (cmRequestedSections != null
				&& cmRequestedSections.size() > 0 || state.getAttribute(STATE_CM_SELECTED_SECTIONS) != null) {
			// set the cmRequest sections to the site property
			
			String cmRequestedSectionString = "";
			
			if (!editingSite)
			{
				// but we want to feed a list of a list of String (input of
				// the required fields)
				for (int j = 0; j < cmRequestedSections.size(); j++) {
					cmRequestedSectionString = cmRequestedSectionString.concat(( cmRequestedSections.get(j)).eid).concat("+");
				}
	
				// trim the trailing plus sign
				cmRequestedSectionString = trimTrailingString(cmRequestedSectionString, "+");
				
				sendSiteRequest(state, "new", cmRequestedSections.size(), cmRequestedSections, "cmRequest");
			}
			else
			{
				cmRequestedSectionString = rp.getProperty(STATE_CM_REQUESTED_SECTIONS) != null ? (String) rp.getProperty(STATE_CM_REQUESTED_SECTIONS):"";
				
				// get the selected cm section
				if (state.getAttribute(STATE_CM_SELECTED_SECTIONS) != null )
				{
					List<SectionObject> cmSelectedSections = (List) state.getAttribute(STATE_CM_SELECTED_SECTIONS);
					if (cmRequestedSectionString.length() != 0)
					{
						cmRequestedSectionString = cmRequestedSectionString.concat("+");
					}
					for (int j = 0; j < cmSelectedSections.size(); j++) {
						cmRequestedSectionString = cmRequestedSectionString.concat(( cmSelectedSections.get(j)).eid).concat("+");
					}
		
					// trim the trailing plus sign
					cmRequestedSectionString = trimTrailingString(cmRequestedSectionString, "+");
					
					sendSiteRequest(state, "new", cmSelectedSections.size(), cmSelectedSections, "cmRequest");
				}
			}
			
			// update site property
			if (cmRequestedSectionString.length() > 0)
			{
				rp.addProperty(STATE_CM_REQUESTED_SECTIONS, cmRequestedSectionString);
			}
			else
			{
				rp.removeProperty(STATE_CM_REQUESTED_SECTIONS);
			}
		}
	}

	/**
	 * Trim the trailing occurance of specified string
	 * @param cmRequestedSectionString
	 * @param trailingString
	 * @return
	 */
	private String trimTrailingString(String cmRequestedSectionString, String trailingString) {
		if (cmRequestedSectionString.endsWith(trailingString)) {
			cmRequestedSectionString = cmRequestedSectionString.substring(0, cmRequestedSectionString.lastIndexOf(trailingString));
		}
		return cmRequestedSectionString;
	}

	/**
	 * buildExternalRealm creates a site/realm id in one of three formats, for a
	 * single section, for multiple sections of the same course, or for a
	 * cross-listing having multiple courses
	 * 
	 * @param sectionList
	 *            is a Vector of CourseListItem
	 * @param id
	 *            The site id
	 */
	private String buildExternalRealm(String id, SessionState state,
			List<String> providerIdList, String existingProviderIdString) {
		String realm = SiteService.siteReference(id);
		if (!AuthzGroupService.allowUpdate(realm)) {
			addAlert(state, rb.getString("java.rosters"));
			return null;
		}
		
		List<String> allProviderIdList = new Vector<String>();
		
		// see if we need to keep existing provider settings
		if (existingProviderIdString != null)
		{
			allProviderIdList.addAll(Arrays.asList(groupProvider.unpackId(existingProviderIdString)));
		}
		
		// update the list with newly added providers
		allProviderIdList.addAll(providerIdList);
		
		if (allProviderIdList == null || allProviderIdList.size() == 0)
			return null;
		
		String[] providers = new String[allProviderIdList.size()];
		providers = (String[]) allProviderIdList.toArray(providers);
		
		String providerId = groupProvider.packId(providers);
		return providerId;

	} // buildExternalRealm

	/**
	 * Notification sent when a course site needs to be set up by Support
	 * 
	 */
	private void sendSiteRequest(SessionState state, String request,
			int requestListSize, List requestFields, String fromContext) {
		User cUser = UserDirectoryService.getCurrentUser();
		String sendEmailToRequestee = null;
		StringBuilder buf = new StringBuilder();

		// get the request email from configuration
		String requestEmail = getSetupRequestEmailAddress();
		if (requestEmail != null) {
			String officialAccountName = ServerConfigurationService
					.getString("officialAccountName", "");

			SiteInfo siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);

			Site site = getStateSite(state);
			String id = site.getId();
			String title = site.getTitle();

			Time time = TimeService.newTime();
			String local_time = time.toStringLocalTime();
			String local_date = time.toStringLocalDate();

			AcademicSession term = null;
			boolean termExist = false;
			if (state.getAttribute(STATE_TERM_SELECTED) != null) {
				termExist = true;
				term = (AcademicSession) state
						.getAttribute(STATE_TERM_SELECTED);
			}
			String productionSiteName = ServerConfigurationService
					.getServerName();

			String from = NULL_STRING;
			String to = NULL_STRING;
			String headerTo = NULL_STRING;
			String replyTo = NULL_STRING;
			String message_subject = NULL_STRING;
			String content = NULL_STRING;

			String sessionUserName = cUser.getDisplayName();
			String additional = NULL_STRING;
			if (request.equals("new")) {
				additional = siteInfo.getAdditional();
			} else {
				additional = (String) state.getAttribute(FORM_ADDITIONAL);
			}

			boolean isFutureTerm = false;
			if (state.getAttribute(STATE_FUTURE_TERM_SELECTED) != null
					&& ((Boolean) state
							.getAttribute(STATE_FUTURE_TERM_SELECTED))
							.booleanValue()) {
				isFutureTerm = true;
			}

			// message subject
			if (termExist) {
				message_subject = rb.getString("java.sitereqfrom") + " "
						+ sessionUserName + " " + rb.getString("java.for")
						+ " " + term.getEid();
			} else {
				message_subject = rb.getString("java.official") + " "
						+ sessionUserName;
			}

			// there is no offical instructor for future term sites
			String requestId = (String) state
					.getAttribute(STATE_SITE_QUEST_UNIQNAME);
			if (!isFutureTerm) {
				// To site quest account - the instructor of record's
				if (requestId != null) {
					// in case of multiple instructors
					List instructors = new ArrayList(Arrays.asList(requestId.split(",")));
					for (Iterator iInstructors = instructors.iterator(); iInstructors.hasNext();)
					{
						String instructorId = (String) iInstructors.next();
						try {
							User instructor = UserDirectoryService.getUserByEid(instructorId);
							
							// reset 
							buf.setLength(0);
							
							to = instructor.getEmail();	
							from = requestEmail;
							headerTo = to;
							replyTo = requestEmail;
							buf.append(rb.getString("java.hello") + " \n\n");
							buf.append(rb.getString("java.receiv") + " "
									+ sessionUserName + ", ");
							buf.append(rb.getString("java.who") + "\n");
							if (termExist) {
								buf.append(term.getTitle());
							}
	
							// requested sections
							if (fromContext.equals("manual"))
							{
								addRequestedSectionIntoNotification(state, requestFields, buf);
							}
							else if (fromContext.equals("cmRequest"))
							{
								addRequestedCMSectionIntoNotification(state, requestFields, buf);
							}
							
							buf.append("\n" + rb.getString("java.sitetitle") + "\t"
									+ title + "\n");
							buf.append(rb.getString("java.siteid") + "\t" + id);
							buf.append("\n\n" + rb.getString("java.according")
									+ " " + sessionUserName + " "
									+ rb.getString("java.record"));
							buf.append(" " + rb.getString("java.canyou") + " "
									+ sessionUserName + " "
									+ rb.getString("java.assoc") + "\n\n");
							buf.append(rb.getString("java.respond") + " "
									+ sessionUserName
									+ rb.getString("java.appoint") + "\n\n");
							buf.append(rb.getString("java.thanks") + "\n");
							buf.append(productionSiteName + " "
									+ rb.getString("java.support"));
							content = buf.toString();
	
							// send the email
							EmailService.send(from, to, message_subject, content,
									headerTo, replyTo, null);
						}
						catch (Exception e)
						{
							sendEmailToRequestee = sendEmailToRequestee == null?instructorId:sendEmailToRequestee.concat(", ").concat(instructorId);
						}
					}
				}
			}

			// To Support
			from = cUser.getEmail();
			to = requestEmail;
			headerTo = requestEmail;
			replyTo = cUser.getEmail();
			buf.setLength(0);
			buf.append(rb.getString("java.to") + "\t\t" + productionSiteName
					+ " " + rb.getString("java.supp") + "\n");
			buf.append("\n" + rb.getString("java.from") + "\t"
					+ sessionUserName + "\n");
			if (request.equals("new")) {
				buf.append(rb.getString("java.subj") + "\t"
						+ rb.getString("java.sitereq") + "\n");
			} else {
				buf.append(rb.getString("java.subj") + "\t"
						+ rb.getString("java.sitechreq") + "\n");
			}
			buf.append(rb.getString("java.date") + "\t" + local_date + " "
					+ local_time + "\n\n");
			if (request.equals("new")) {
				buf.append(rb.getString("java.approval") + " "
						+ productionSiteName + " "
						+ rb.getString("java.coursesite") + " ");
			} else {
				buf.append(rb.getString("java.approval2") + " "
						+ productionSiteName + " "
						+ rb.getString("java.coursesite") + " ");
			}
			if (termExist) {
				buf.append(term.getTitle());
			}
			if (requestListSize > 1) {
				buf.append(" " + rb.getString("java.forthese") + " "
						+ requestListSize + " " + rb.getString("java.sections")
						+ "\n\n");
			} else {
				buf.append(" " + rb.getString("java.forthis") + "\n\n");
			}

			// requested sections
			if (fromContext.equals("manual"))
			{
				addRequestedSectionIntoNotification(state, requestFields, buf);
			}
			else if (fromContext.equals("cmRequest"))
			{
				addRequestedCMSectionIntoNotification(state, requestFields, buf);
			}
			
			buf.append(rb.getString("java.name") + "\t" + sessionUserName
					+ " (" + officialAccountName + " " + cUser.getEid()
					+ ")\n");
			buf.append(rb.getString("java.email") + "\t" + replyTo + "\n\n");
			buf.append(rb.getString("java.sitetitle") + "\t" + title + "\n");
			buf.append(rb.getString("java.siteid") + "\t" + id + "\n");
			buf.append(rb.getString("java.siteinstr") + "\n" + additional
					+ "\n\n");

			if (!isFutureTerm) {
				if (sendEmailToRequestee == null) {
					buf.append(rb.getString("java.authoriz") + " " + requestId
							+ " " + rb.getString("java.asreq"));
				} else {
					buf.append(rb.getString("java.thesiteemail") + " "
							+ sendEmailToRequestee + " " + rb.getString("java.asreq"));
				}
			}
			content = buf.toString();
			EmailService.send(from, to, message_subject, content, headerTo,
					replyTo, null);

			// To the Instructor
			from = requestEmail;
			to = cUser.getEmail();
			headerTo = to;
			replyTo = to;
			buf.setLength(0);
			buf.append(rb.getString("java.isbeing") + " ");
			buf.append(rb.getString("java.meantime") + "\n\n");
			buf.append(rb.getString("java.copy") + "\n\n");
			buf.append(content);
			buf.append("\n" + rb.getString("java.wish") + " " + requestEmail);
			content = buf.toString();
			EmailService.send(from, to, message_subject, content, headerTo,
					replyTo, null);
			state.setAttribute(REQUEST_SENT, new Boolean(true));

		} // if

	} // sendSiteRequest

	private void addRequestedSectionIntoNotification(SessionState state, List requestFields, StringBuilder buf) {
		// what are the required fields shown in the UI
		List requiredFields = state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS) != null ?(List) state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS):new Vector();
		for (int i = 0; i < requiredFields.size(); i++) {
			List requiredFieldList = (List) requestFields
					.get(i);
			for (int j = 0; j < requiredFieldList.size(); j++) {
				SectionField requiredField = (SectionField) requiredFieldList
						.get(j);

				buf.append(requiredField.getLabelKey() + "\t"
						+ requiredField.getValue() + "\n");
			}
		}
	}
	
	private void addRequestedCMSectionIntoNotification(SessionState state, List cmRequestedSections, StringBuilder buf) {
		// what are the required fields shown in the UI
		for (int i = 0; i < cmRequestedSections.size(); i++) {
			SectionObject so = (SectionObject) cmRequestedSections.get(i);

			buf.append(so.getTitle() + "(" + so.getEid()
					+ ")" + so.getCategory() + "\n");
		}
	}

	/**
	 * Notification sent when a course site is set up automatcally
	 * 
	 */
	private void sendSiteNotification(SessionState state, List notifySites) {
		// get the request email from configuration
		String requestEmail = getSetupRequestEmailAddress();
		if (requestEmail != null) {
			// send emails
			Site site = getStateSite(state);
			String id = site.getId();
			String title = site.getTitle();
			Time time = TimeService.newTime();
			String local_time = time.toStringLocalTime();
			String local_date = time.toStringLocalDate();
			String term_name = "";
			if (state.getAttribute(STATE_TERM_SELECTED) != null) {
				term_name = ((AcademicSession) state
						.getAttribute(STATE_TERM_SELECTED)).getEid();
			}
			String message_subject = rb.getString("java.official") + " "
					+ UserDirectoryService.getCurrentUser().getDisplayName()
					+ " " + rb.getString("java.for") + " " + term_name;

			String from = NULL_STRING;
			String to = NULL_STRING;
			String headerTo = NULL_STRING;
			String replyTo = NULL_STRING;
			String sender = UserDirectoryService.getCurrentUser()
					.getDisplayName();
			String userId = StringUtil.trimToZero(SessionManager
					.getCurrentSessionUserId());
			try {
				userId = UserDirectoryService.getUserEid(userId);
			} catch (UserNotDefinedException e) {
				M_log.warn(this + rb.getString("user.notdefined") + " "
						+ userId);
			}

			// To Support
			from = UserDirectoryService.getCurrentUser().getEmail();
			to = requestEmail;
			headerTo = requestEmail;
			replyTo = UserDirectoryService.getCurrentUser().getEmail();
			StringBuilder buf = new StringBuilder();
			buf.append("\n" + rb.getString("java.fromwork") + " "
					+ ServerConfigurationService.getServerName() + " "
					+ rb.getString("java.supp") + ":\n\n");
			buf.append(rb.getString("java.off") + " '" + title + "' (id " + id
					+ "), " + rb.getString("java.wasset") + " ");
			buf.append(sender + " (" + userId + ", "
					+ rb.getString("java.email2") + " " + replyTo + ") ");
			buf.append(rb.getString("java.on") + " " + local_date + " "
					+ rb.getString("java.at") + " " + local_time + " ");
			buf.append(rb.getString("java.for") + " " + term_name + ", ");
			int nbr_sections = notifySites.size();
			if (nbr_sections > 1) {
				buf.append(rb.getString("java.withrost") + " "
						+ Integer.toString(nbr_sections) + " "
						+ rb.getString("java.sections") + "\n\n");
			} else {
				buf.append(" " + rb.getString("java.withrost2") + "\n\n");
			}

			for (int i = 0; i < nbr_sections; i++) {
				String course = (String) notifySites.get(i);
				buf.append(rb.getString("java.course2") + " " + course + "\n");
			}
			String content = buf.toString();
			EmailService.send(from, to, message_subject, content, headerTo,
					replyTo, null);
		} // if

	} // sendSiteNotification

	/**
	 * doCancel called when "eventSubmit_doCancel_create" is in the request
	 * parameters to c
	 */
	public void doCancel_create(RunData data) {
		// Don't put current form data in state, just return to the previous
		// template
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		removeAddClassContext(state);
		state.setAttribute(STATE_TEMPLATE_INDEX, "0");

	} // doCancel_create

	/**
	 * doCancel called when "eventSubmit_doCancel" is in the request parameters
	 * to c int index = Integer.valueOf(params.getString
	 * ("template-index")).intValue();
	 */
	public void doCancel(RunData data) {
		// Don't put current form data in state, just return to the previous
		// template
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();

		state.removeAttribute(STATE_MESSAGE);

		String currentIndex = (String) state.getAttribute(STATE_TEMPLATE_INDEX);

		String backIndex = params.getString("back");
		state.setAttribute(STATE_TEMPLATE_INDEX, backIndex);

		if (currentIndex.equals("4")) {
			state.removeAttribute(STATE_TOOL_EMAIL_ADDRESS);
			state.removeAttribute(STATE_MESSAGE);
			removeEditToolState(state);
		} else if (currentIndex.equals("5")) {
			// remove related state variables
			removeAddParticipantContext(state);

			params = data.getParameters();
			state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("back"));
		} else if (currentIndex.equals("13") || currentIndex.equals("14")) {
			// clean state attributes
			state.removeAttribute(FORM_SITEINFO_TITLE);
			state.removeAttribute(FORM_SITEINFO_DESCRIPTION);
			state.removeAttribute(FORM_SITEINFO_SHORT_DESCRIPTION);
			state.removeAttribute(FORM_SITEINFO_SKIN);
			state.removeAttribute(FORM_SITEINFO_INCLUDE);
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		} else if (currentIndex.equals("15")) {
			params = data.getParameters();
			state.setAttribute(STATE_TEMPLATE_INDEX, params
					.getString("cancelIndex"));
			removeEditToolState(state);
		}
		// htripath: added 'currentIndex.equals("45")' for import from file
		// cancel
		else if (currentIndex.equals("19") || currentIndex.equals("20")
				|| currentIndex.equals("21") || currentIndex.equals("22")
				|| currentIndex.equals("45")) {
			// from adding participant pages
			// remove related state variables
			removeAddParticipantContext(state);

			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		} else if (currentIndex.equals("3")) {
			// from adding class
			if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITESETUP)) {
				state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			} else if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITEINFO)) {
				state.setAttribute(STATE_TEMPLATE_INDEX, "18");
			}
		} else if (currentIndex.equals("27") || currentIndex.equals("28")) {
			// from import
			if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITESETUP)) {
				// worksite setup
				if (getStateSite(state) == null) {
					// in creating new site process
					state.setAttribute(STATE_TEMPLATE_INDEX, "0");
				} else {
					// in editing site process
					state.setAttribute(STATE_TEMPLATE_INDEX, "12");
				}
			} else if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITEINFO)) {
				// site info
				state.setAttribute(STATE_TEMPLATE_INDEX, "12");
			}
			state.removeAttribute(STATE_IMPORT_SITE_TOOL);
			state.removeAttribute(STATE_IMPORT_SITES);
		} else if (currentIndex.equals("26")) {
			if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITESETUP)
					&& getStateSite(state) == null) {
				// from creating site
				state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			} else {
				// from revising site
				state.setAttribute(STATE_TEMPLATE_INDEX, "12");
			}
			removeEditToolState(state);
		} else if (currentIndex.equals("37") || currentIndex.equals("44") || currentIndex.equals("53") || currentIndex.equals("36")) {
			// cancel back to edit class view
			state.removeAttribute(STATE_TERM_SELECTED);
			removeAddClassContext(state);
			state.setAttribute(STATE_TEMPLATE_INDEX, "43");
		}
		

	} // doCancel

	/**
	 * doMenu_customize is called when "eventSubmit_doBack" is in the request
	 * parameters Pass parameter to actionForTemplate to request action for
	 * backward direction
	 */
	public void doMenu_customize(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_TEMPLATE_INDEX, "15");

	}// doMenu_customize

	/**
	 * doBack_to_list cancels an outstanding site edit, cleans state and returns
	 * to the site list
	 * 
	 */
	public void doBack_to_list(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		Site site = getStateSite(state);
		if (site != null) {
			Hashtable h = (Hashtable) state
					.getAttribute(STATE_PAGESIZE_SITEINFO);
			h.put(site.getId(), state.getAttribute(STATE_PAGESIZE));
			state.setAttribute(STATE_PAGESIZE_SITEINFO, h);
		}

		// restore the page size for Worksite setup tool
		if (state.getAttribute(STATE_PAGESIZE_SITESETUP) != null) {
			state.setAttribute(STATE_PAGESIZE, state
					.getAttribute(STATE_PAGESIZE_SITESETUP));
			state.removeAttribute(STATE_PAGESIZE_SITESETUP);
		}

		cleanState(state);
		setupFormNamesAndConstants(state);

		state.setAttribute(STATE_TEMPLATE_INDEX, "0");

	} // doBack_to_list

	/**
	 * do called when "eventSubmit_do" is in the request parameters to c
	 */
	public void doAdd_custom_link(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		if ((params.getString("name")) == null
				|| (params.getString("url") == null)) {
			Tool tr = ToolManager.getTool("sakai.iframe");
			Site site = getStateSite(state);
			SitePage page = site.addPage();
			page.setTitle(params.getString("name")); // the visible label on
			// the tool menu
			ToolConfiguration tool = page.addTool();
			tool.setTool("sakai.iframe", tr);
			tool.setTitle(params.getString("name"));
			commitSite(site);
		} else {
			addAlert(state, rb.getString("java.reqmiss"));
			state.setAttribute(STATE_TEMPLATE_INDEX, params
					.getString("template-index"));
		}

	} // doAdd_custom_link

	/**
	 * doAdd_remove_features is called when Make These Changes is clicked in
	 * chef_site-addRemoveFeatures
	 */
	public void doAdd_remove_features(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		List existTools = (List) state
				.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);

		ParameterParser params = data.getParameters();
		String option = params.getString("option");

		// dispatch
		if (option.equalsIgnoreCase("addNews")) {
			updateSelectedToolList(state, params, false);
			insertTool(state, "sakai.news", STATE_NEWS_TITLES,
					NEWS_DEFAULT_TITLE, STATE_NEWS_URLS, NEWS_DEFAULT_URL,
					Integer.parseInt(params.getString("newsNum")));
			state.setAttribute(STATE_TEMPLATE_INDEX, "26");
		} else if (option.equalsIgnoreCase("addWC")) {
			updateSelectedToolList(state, params, false);
			insertTool(state, "sakai.iframe", STATE_WEB_CONTENT_TITLES,
					WEB_CONTENT_DEFAULT_TITLE, STATE_WEB_CONTENT_URLS,
					WEB_CONTENT_DEFAULT_URL, Integer.parseInt(params
							.getString("wcNum")));
			state.setAttribute(STATE_TEMPLATE_INDEX, "26");
		} else if (option.equalsIgnoreCase("save")) {
			List idsSelected = new Vector();

			boolean goToENWPage = false;
			boolean homeSelected = false;

			// Add new pages and tools, if any
			if (params.getStrings("selectedTools") == null) {
				addAlert(state, rb.getString("atleastonetool"));
			} else {
				List l = new ArrayList(Arrays.asList(params
						.getStrings("selectedTools"))); // toolId's & titles of
				// chosen tools

				for (int i = 0; i < l.size(); i++) {
					String toolId = (String) l.get(i);

					if (toolId.equals(HOME_TOOL_ID)) {
						homeSelected = true;
					} else if (toolId.equals("sakai.mailbox")
							|| toolId.indexOf("sakai.news") != -1
							|| toolId.indexOf("sakai.iframe") != -1) {
						// if user is adding either EmailArchive tool, News tool
						// or Web Content tool, go to the Customize page for the
						// tool
						if (!existTools.contains(toolId)) {
							goToENWPage = true;
						}

						if (toolId.equals("sakai.mailbox")) {
							// get the email alias when an Email Archive tool
							// has been selected
							String channelReference = mailArchiveChannelReference((String) state
									.getAttribute(STATE_SITE_INSTANCE_ID));
							List aliases = AliasService.getAliases(
									channelReference, 1, 1);
							if (aliases.size() > 0) {
								state.setAttribute(STATE_TOOL_EMAIL_ADDRESS,
										((Alias) aliases.get(0)).getId());
							}
						}
					}
					idsSelected.add(toolId);

				}

				state.setAttribute(STATE_TOOL_HOME_SELECTED, new Boolean(
						homeSelected));
			}

			state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST,
					idsSelected); // List of ToolRegistration toolId's

			if (state.getAttribute(STATE_MESSAGE) == null) {
				if (goToENWPage) {
					// go to the configuration page for Email Archive, News and
					// Web Content tools
					state.setAttribute(STATE_TEMPLATE_INDEX, "26");
				} else {
					// go to confirmation page
					state.setAttribute(STATE_TEMPLATE_INDEX, "15");
				}
			}
		} else if (option.equalsIgnoreCase("continue")) {
			// continue
			doContinue(data);
		} else if (option.equalsIgnoreCase("Back")) {
			// back
			doBack(data);
		} else if (option.equalsIgnoreCase("Cancel")) {
			// cancel
			doCancel(data);
		}
	} // doAdd_remove_features

	/**
	 * doSave_revised_features
	 */
	public void doSave_revised_features(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		getRevisedFeatures(params, state);

		Site site = getStateSite(state);
		String id = site.getId();

		// now that the site exists, we can set the email alias when an Email
		// Archive tool has been selected
		String alias = StringUtil.trimToNull((String) state
				.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
		if (alias != null) {
			String channelReference = mailArchiveChannelReference(id);
			try {
				AliasService.setAlias(alias, channelReference);
			} catch (IdUsedException ee) {
			} catch (IdInvalidException ee) {
				addAlert(state, rb.getString("java.alias") + " " + alias + " "
						+ rb.getString("java.isinval"));
			} catch (PermissionException ee) {
				addAlert(state, rb.getString("java.addalias") + " ");
			}
		}
		if (state.getAttribute(STATE_MESSAGE) == null) {
			// clean state variables
			state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
			state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME);
			state.removeAttribute(STATE_NEWS_TITLES);
			state.removeAttribute(STATE_NEWS_URLS);
			state.removeAttribute(STATE_WEB_CONTENT_TITLES);
			state.removeAttribute(STATE_WEB_CONTENT_URLS);

			state.setAttribute(STATE_SITE_INSTANCE_ID, id);

			state.setAttribute(STATE_TEMPLATE_INDEX, params
					.getString("continue"));
		}

		// refresh the whole page
		scheduleTopRefresh();

	} // doSave_revised_features

	/**
	 * doMenu_add_participant
	 */
	public void doMenu_add_participant(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.removeAttribute(STATE_SELECTED_USER_LIST);
		state.setAttribute(STATE_TEMPLATE_INDEX, "5");

	} // doMenu_add_participant

	/**
	 * doMenu_siteInfo_addParticipant
	 */
	public void doMenu_siteInfo_addParticipant(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.removeAttribute(STATE_SELECTED_USER_LIST);

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "5");
		}

	} // doMenu_siteInfo_addParticipant

	/**
	 * doMenu_siteInfo_cancel_access
	 */
	public void doMenu_siteInfo_cancel_access(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.removeAttribute(STATE_SELECTED_USER_LIST);
		state.setAttribute(STATE_TEMPLATE_INDEX, "12");

	} // doMenu_siteInfo_cancel_access

	/**
	 * doMenu_siteInfo_import
	 */
	public void doMenu_siteInfo_import(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the tools
		siteToolsIntoState(state);

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "28");
		}

	} // doMenu_siteInfo_import

	/**
	 * doMenu_siteInfo_editClass
	 */
	public void doMenu_siteInfo_editClass(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.setAttribute(STATE_TEMPLATE_INDEX, "43");

	} // doMenu_siteInfo_editClass

	/**
	 * doMenu_siteInfo_addClass
	 */
	public void doMenu_siteInfo_addClass(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		Site site = getStateSite(state);
		String termEid = site.getProperties().getProperty(PROP_SITE_TERM_EID);
		if (termEid == null)
		{
			// no term eid stored, need to get term eid from the term title
			String termTitle = site.getProperties().getProperty(PROP_SITE_TERM);
			List asList = cms.getAcademicSessions();
			if (termTitle != null && asList != null)
			{
				boolean found = false;
				for (int i = 0; i<asList.size() && !found; i++)
				{
					AcademicSession as = (AcademicSession) asList.get(i);
					if (as.getTitle().equals(termTitle))
					{
						termEid = as.getEid();
						site.getPropertiesEdit().addProperty(PROP_SITE_TERM_EID, termEid);
						
						try
						{
							SiteService.save(site);
						}
						catch (Exception e)
						{
							M_log.warn(this + e.getMessage() + site.getId());
						}
						found=true;
					}
				}
			}
		}
		state.setAttribute(STATE_TERM_SELECTED, cms.getAcademicSession(termEid));
		
		try
		{
		List sections = prepareCourseAndSectionListing(UserDirectoryService.getCurrentUser().getEid(), cms.getAcademicSession(termEid).getEid(), state);
		isFutureTermSelected(state);
		if (sections != null && sections.size() > 0) 
			state.setAttribute(STATE_TERM_COURSE_LIST, sections);
		}
		catch (Exception e)
		{
			M_log.warn(e.getMessage() + termEid);
		}
		state.setAttribute(STATE_TEMPLATE_INDEX, "36");

	} // doMenu_siteInfo_addClass

	/**
	 * doMenu_siteInfo_duplicate
	 */
	public void doMenu_siteInfo_duplicate(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "29");
		}

	} // doMenu_siteInfo_import

	/**
	 * doMenu_edit_site_info
	 * 
	 */
	public void doMenu_edit_site_info(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		Site Site = getStateSite(state);
		ResourceProperties siteProperties = Site.getProperties();
		state.setAttribute(FORM_SITEINFO_TITLE, Site.getTitle());

		String site_type = (String) state.getAttribute(STATE_SITE_TYPE);
		if (site_type != null && !site_type.equalsIgnoreCase("myworkspace")) {
			state.setAttribute(FORM_SITEINFO_INCLUDE, Boolean.valueOf(
					Site.isPubView()).toString());
		}
		state.setAttribute(FORM_SITEINFO_DESCRIPTION, Site.getDescription());
		state.setAttribute(FORM_SITEINFO_SHORT_DESCRIPTION, Site
				.getShortDescription());
		state.setAttribute(FORM_SITEINFO_SKIN, Site.getIconUrl());
		if (Site.getIconUrl() != null) {
			state.setAttribute(FORM_SITEINFO_SKIN, Site.getIconUrl());
		}

		// site contact information
		String contactName = siteProperties.getProperty(PROP_SITE_CONTACT_NAME);
		String contactEmail = siteProperties
				.getProperty(PROP_SITE_CONTACT_EMAIL);
		if (contactName == null && contactEmail == null) {
			String creatorId = siteProperties
					.getProperty(ResourceProperties.PROP_CREATOR);
			try {
				User u = UserDirectoryService.getUser(creatorId);
				String email = u.getEmail();
				if (email != null) {
					contactEmail = u.getEmail();
				}
				contactName = u.getDisplayName();
			} catch (UserNotDefinedException e) {
			}
		}
		if (contactName != null) {
			state.setAttribute(FORM_SITEINFO_CONTACT_NAME, contactName);
		}
		if (contactEmail != null) {
			state.setAttribute(FORM_SITEINFO_CONTACT_EMAIL, contactEmail);
		}

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "13");
		}

	} // doMenu_edit_site_info

	/**
	 * doMenu_edit_site_tools
	 * 
	 */
	public void doMenu_edit_site_tools(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the tools
		siteToolsIntoState(state);

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "4");
		}

	} // doMenu_edit_site_tools

	/**
	 * doMenu_edit_site_access
	 * 
	 */
	public void doMenu_edit_site_access(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "18");
		}

	} // doMenu_edit_site_access

	/**
	 * Back to worksite setup's list view
	 * 
	 */
	public void doBack_to_site_list(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.removeAttribute(STATE_SELECTED_USER_LIST);
		state.removeAttribute(STATE_SITE_TYPE);
		state.removeAttribute(STATE_SITE_INSTANCE_ID);

		state.setAttribute(STATE_TEMPLATE_INDEX, "0");

	} // doBack_to_site_list

	/**
	 * doSave_site_info
	 * 
	 */
	public void doSave_siteInfo(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		Site Site = getStateSite(state);
		ResourcePropertiesEdit siteProperties = Site.getPropertiesEdit();
		String site_type = (String) state.getAttribute(STATE_SITE_TYPE);

		if (site_type != null && !site_type.equals((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
			Site.setTitle((String) state.getAttribute(FORM_SITEINFO_TITLE));
		}

		Site.setDescription((String) state
				.getAttribute(FORM_SITEINFO_DESCRIPTION));
		Site.setShortDescription((String) state
				.getAttribute(FORM_SITEINFO_SHORT_DESCRIPTION));

		if (site_type != null) {
			if (site_type.equals((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				// set icon url for course
				String skin = (String) state.getAttribute(FORM_SITEINFO_SKIN);
				setAppearance(state, Site, skin);
			} else {
				// set icon url for others
				String iconUrl = (String) state
						.getAttribute(FORM_SITEINFO_ICON_URL);
				Site.setIconUrl(iconUrl);
			}

		}

		// site contact information
		String contactName = (String) state
				.getAttribute(FORM_SITEINFO_CONTACT_NAME);
		if (contactName != null) {
			siteProperties.addProperty(PROP_SITE_CONTACT_NAME, contactName);
		}

		String contactEmail = (String) state
				.getAttribute(FORM_SITEINFO_CONTACT_EMAIL);
		if (contactEmail != null) {
			siteProperties.addProperty(PROP_SITE_CONTACT_EMAIL, contactEmail);
		}

		if (state.getAttribute(STATE_MESSAGE) == null) {
			try {
				SiteService.save(Site);
			} catch (IdUnusedException e) {
				// TODO:
			} catch (PermissionException e) {
				// TODO:
			}

			// clean state attributes
			state.removeAttribute(FORM_SITEINFO_TITLE);
			state.removeAttribute(FORM_SITEINFO_DESCRIPTION);
			state.removeAttribute(FORM_SITEINFO_SHORT_DESCRIPTION);
			state.removeAttribute(FORM_SITEINFO_SKIN);
			state.removeAttribute(FORM_SITEINFO_INCLUDE);
			state.removeAttribute(FORM_SITEINFO_CONTACT_NAME);
			state.removeAttribute(FORM_SITEINFO_CONTACT_EMAIL);

			// back to site info view
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");

			// refresh the whole page
			scheduleTopRefresh();

		}
	} // doSave_siteInfo

	/**
	 * init
	 * 
	 */
	private void init(VelocityPortlet portlet, RunData data, SessionState state) {
		state.setAttribute(STATE_ACTION, "SiteAction");
		setupFormNamesAndConstants(state);

		if (state.getAttribute(STATE_PAGESIZE_SITEINFO) == null) {
			state.setAttribute(STATE_PAGESIZE_SITEINFO, new Hashtable());
		}

		if (((String) state.getAttribute(STATE_SITE_MODE))
				.equalsIgnoreCase(SITE_MODE_SITESETUP)) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");
		} else if (((String) state.getAttribute(STATE_SITE_MODE))
				.equalsIgnoreCase(SITE_MODE_SITEINFO)) {

			String siteId = ToolManager.getCurrentPlacement().getContext();
			getReviseSite(state, siteId);
			Hashtable h = (Hashtable) state
					.getAttribute(STATE_PAGESIZE_SITEINFO);
			if (!h.containsKey(siteId)) {
				// update
				h.put(siteId, new Integer(200));
				state.setAttribute(STATE_PAGESIZE_SITEINFO, h);
				state.setAttribute(STATE_PAGESIZE, new Integer(200));
			}
		}
		if (state.getAttribute(STATE_SITE_TYPES) == null) {
			PortletConfig config = portlet.getPortletConfig();

			// all site types (SITE_DEFAULT_LIST overrides tool config)
			String t = StringUtil.trimToNull(SITE_DEFAULT_LIST);
			if ( t == null )
				t = StringUtil.trimToNull(config.getInitParameter("siteTypes"));
			if (t != null) {
				List types = new ArrayList(Arrays.asList(t.split(",")));
				if (cms == null)
				{
					// if there is no CourseManagementService, disable the process of creating course site
					String courseType = ServerConfigurationService.getString("courseSiteType", (String) state.getAttribute(STATE_COURSE_SITE_TYPE));
					types.remove(courseType);
				}
					
				state.setAttribute(STATE_SITE_TYPES, types);
			} else {
				state.setAttribute(STATE_SITE_TYPES, new Vector());
			}
		}
	} // init

	public void doNavigate_to_site(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String siteId = StringUtil.trimToNull(data.getParameters().getString(
				"option"));
		if (siteId != null) {
			getReviseSite(state, siteId);
		} else {
			doBack_to_list(data);
		}

	} // doNavigate_to_site

	/**
	 * Get site information for revise screen
	 */
	private void getReviseSite(SessionState state, String siteId) {
		if (state.getAttribute(STATE_SELECTED_USER_LIST) == null) {
			state.setAttribute(STATE_SELECTED_USER_LIST, new Vector());
		}

		List sites = (List) state.getAttribute(STATE_SITES);

		try {
			Site site = SiteService.getSite(siteId);
			state.setAttribute(STATE_SITE_INSTANCE_ID, site.getId());

			if (sites != null) {
				int pos = -1;
				for (int index = 0; index < sites.size() && pos == -1; index++) {
					if (((Site) sites.get(index)).getId().equals(siteId)) {
						pos = index;
					}
				}

				// has any previous site in the list?
				if (pos > 0) {
					state.setAttribute(STATE_PREV_SITE, sites.get(pos - 1));
				} else {
					state.removeAttribute(STATE_PREV_SITE);
				}

				// has any next site in the list?
				if (pos < sites.size() - 1) {
					state.setAttribute(STATE_NEXT_SITE, sites.get(pos + 1));
				} else {
					state.removeAttribute(STATE_NEXT_SITE);
				}
			}

			String type = site.getType();
			if (type == null) {
				if (state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null) {
					type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);
				}
			}
			state.setAttribute(STATE_SITE_TYPE, type);

		} catch (IdUnusedException e) {
			M_log.warn(this + e.toString());
		}

		// one site has been selected
		state.setAttribute(STATE_TEMPLATE_INDEX, "12");

	} // getReviseSite

	/**
	 * doUpdate_participant
	 * 
	 */
	public void doUpdate_participant(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		Site s = getStateSite(state);
		String realmId = SiteService.siteReference(s.getId());
		if (AuthzGroupService.allowUpdate(realmId)
				|| SiteService.allowUpdateSiteMembership(s.getId())) {
			try {
				AuthzGroup realmEdit = AuthzGroupService.getAuthzGroup(realmId);

				// does the site has maintain type user(s) before updating
				// participants?
				String maintainRoleString = realmEdit.getMaintainRole();
				boolean hadMaintainUser = !realmEdit.getUsersHasRole(
						maintainRoleString).isEmpty();

				// update participant roles
				List participants = collectionToList((Collection) state.getAttribute(STATE_PARTICIPANT_LIST));
				// remove all roles and then add back those that were checked
				for (int i = 0; i < participants.size(); i++) {
					String id = null;

					// added participant
					Participant participant = (Participant) participants.get(i);
					id = participant.getUniqname();

					if (id != null) {
						// get the newly assigned role
						String inputRoleField = "role" + id;
						String roleId = params.getString(inputRoleField);

						// only change roles when they are different than before
						if (roleId != null) {
							// get the grant active status
							boolean activeGrant = true;
							String activeGrantField = "activeGrant" + id;
							if (params.getString(activeGrantField) != null) {
								activeGrant = params
										.getString(activeGrantField)
										.equalsIgnoreCase("true") ? true
										: false;
							}

							boolean fromProvider = !participant.isRemoveable();
							if (fromProvider && !roleId.equals(participant.getRole())) {
							    fromProvider = false;
							}
							realmEdit.addMember(id, roleId, activeGrant,
									fromProvider);
						}
					}
				}

				// remove selected users
				if (params.getStrings("selectedUser") != null) {
					List removals = new ArrayList(Arrays.asList(params
							.getStrings("selectedUser")));
					state.setAttribute(STATE_SELECTED_USER_LIST, removals);
					for (int i = 0; i < removals.size(); i++) {
						String rId = (String) removals.get(i);
						try {
							User user = UserDirectoryService.getUser(rId);
							Participant selected = new Participant();
							selected.name = user.getDisplayName();
							selected.uniqname = user.getId();
							realmEdit.removeMember(user.getId());
						} catch (UserNotDefinedException e) {
							M_log.warn(this + " IdUnusedException " + rId
									+ ". ");
						}
					}
				}

				if (hadMaintainUser
						&& realmEdit.getUsersHasRole(maintainRoleString)
								.isEmpty()) {
					// if after update, the "had maintain type user" status
					// changed, show alert message and don't save the update
					addAlert(state, rb
							.getString("sitegen.siteinfolist.nomaintainuser")
							+ maintainRoleString + ".");
				} else {
					// post event about the participant update
					EventTrackingService.post(EventTrackingService.newEvent(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP, realmEdit.getId(),false));
					AuthzGroupService.save(realmEdit);
					
					// then update all related group realms for the role
					doUpdate_related_group_participants(s, realmId);
				}
			} catch (GroupNotDefinedException e) {
				addAlert(state, rb.getString("java.problem2"));
				M_log.warn(this + "  IdUnusedException " + s.getTitle() + "("
						+ realmId + "). ");
			} catch (AuthzPermissionException e) {
				addAlert(state, rb.getString("java.changeroles"));
				M_log.warn(this + "  PermissionException " + s.getTitle() + "("
						+ realmId + "). ");
			}
		}

	} // doUpdate_participant

	/**
	 * update realted group realm setting according to parent site realm changes
	 * @param s
	 * @param realmId
	 */
	private void doUpdate_related_group_participants(Site s, String realmId) {
		Collection groups = s.getGroups();
		if (groups != null)
		{
			for (Iterator iGroups = groups.iterator(); iGroups.hasNext();)
			{
				Group g = (Group) iGroups.next();
				try
				{
					Set gMembers = g.getMembers();
					for (Iterator iGMembers = gMembers.iterator(); iGMembers.hasNext();)
					{
						Member gMember = (Member) iGMembers.next();
						String gMemberId = gMember.getUserId();
						Member siteMember = s.getMember(gMemberId);
						if ( siteMember  == null)
						{
							// user has been removed from the site
							g.removeMember(gMemberId);
						}
						else
						{
							// if there is a difference between the role setting, remove the entry from group and add it back with correct role, all are marked "not provided"
							if (!g.getUserRole(gMemberId).equals(siteMember.getRole()))
							{
								g.removeMember(gMemberId);
								g.addMember(gMemberId, siteMember.getRole().getId(), siteMember.isActive(), false);
							}
						}
					}
					// commit
					// post event about the participant update
					EventTrackingService.post(EventTrackingService.newEvent(SiteService.SECURE_UPDATE_GROUP_MEMBERSHIP, g.getId(),false));
					SiteService.save(s);
				}
				catch (Exception ee)
				{
					M_log.warn(this + ee.getMessage() + g.getId());
				}
				
			}
		}
	}

	/**
	 * doUpdate_site_access
	 * 
	 */
	public void doUpdate_site_access(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		Site sEdit = getStateSite(state);

		ParameterParser params = data.getParameters();
		String publishUnpublish = params.getString("publishunpublish");
		String include = params.getString("include");
		String joinable = params.getString("joinable");

		if (sEdit != null) {
			// editing existing site
			// publish site or not
			if (publishUnpublish != null
					&& publishUnpublish.equalsIgnoreCase("publish")) {
				sEdit.setPublished(true);
			} else {
				sEdit.setPublished(false);
			}

			// site public choice
			if (include != null) {
				// if there is pubview input, use it
				sEdit.setPubView(include.equalsIgnoreCase("true") ? true
						: false);
			} else if (state.getAttribute(STATE_SITE_TYPE) != null) {
				String type = (String) state.getAttribute(STATE_SITE_TYPE);
				List publicSiteTypes = (List) state
						.getAttribute(STATE_PUBLIC_SITE_TYPES);
				List privateSiteTypes = (List) state
						.getAttribute(STATE_PRIVATE_SITE_TYPES);

				if (publicSiteTypes.contains(type)) {
					// sites are always public
					sEdit.setPubView(true);
				} else if (privateSiteTypes.contains(type)) {
					// site are always private
					sEdit.setPubView(false);
				}
			} else {
				sEdit.setPubView(false);
			}

			// publish site or not
			if (joinable != null && joinable.equalsIgnoreCase("true")) {
				state.setAttribute(STATE_JOINABLE, Boolean.TRUE);
				sEdit.setJoinable(true);
				String joinerRole = StringUtil.trimToNull(params
						.getString("joinerRole"));
				if (joinerRole != null) {
					state.setAttribute(STATE_JOINERROLE, joinerRole);
					sEdit.setJoinerRole(joinerRole);
				} else {
					state.setAttribute(STATE_JOINERROLE, "");
					addAlert(state, rb.getString("java.joinsite") + " ");
				}
			} else {
				state.setAttribute(STATE_JOINABLE, Boolean.FALSE);
				state.removeAttribute(STATE_JOINERROLE);
				sEdit.setJoinable(false);
				sEdit.setJoinerRole(null);
			}

			if (state.getAttribute(STATE_MESSAGE) == null) {
				commitSite(sEdit);
				state.setAttribute(STATE_TEMPLATE_INDEX, "12");

				// TODO: hard coding this frame id is fragile, portal dependent,
				// and needs to be fixed -ggolden
				// schedulePeerFrameRefresh("sitenav");
				scheduleTopRefresh();

				state.removeAttribute(STATE_JOINABLE);
				state.removeAttribute(STATE_JOINERROLE);
			}
		} else {
			// adding new site
			if (state.getAttribute(STATE_SITE_INFO) != null) {
				SiteInfo siteInfo = (SiteInfo) state
						.getAttribute(STATE_SITE_INFO);

				if (publishUnpublish != null
						&& publishUnpublish.equalsIgnoreCase("publish")) {
					siteInfo.published = true;
				} else {
					siteInfo.published = false;
				}

				// site public choice
				if (include != null) {
					siteInfo.include = include.equalsIgnoreCase("true") ? true
							: false;
				} else if (StringUtil.trimToNull(siteInfo.site_type) != null) {
					String type = StringUtil.trimToNull(siteInfo.site_type);
					List publicSiteTypes = (List) state
							.getAttribute(STATE_PUBLIC_SITE_TYPES);
					List privateSiteTypes = (List) state
							.getAttribute(STATE_PRIVATE_SITE_TYPES);

					if (publicSiteTypes.contains(type)) {
						// sites are always public
						siteInfo.include = true;
					} else if (privateSiteTypes.contains(type)) {
						// site are always private
						siteInfo.include = false;
					}
				} else {
					siteInfo.include = false;
				}

				// joinable site or not
				if (joinable != null && joinable.equalsIgnoreCase("true")) {
					siteInfo.joinable = true;
					String joinerRole = StringUtil.trimToNull(params
							.getString("joinerRole"));
					if (joinerRole != null) {
						siteInfo.joinerRole = joinerRole;
					} else {
						addAlert(state, rb.getString("java.joinsite") + " ");
					}
				} else {
					siteInfo.joinable = false;
					siteInfo.joinerRole = null;
				}

				state.setAttribute(STATE_SITE_INFO, siteInfo);
			}

			if (state.getAttribute(STATE_MESSAGE) == null) {
				state.setAttribute(STATE_TEMPLATE_INDEX, "10");
				updateCurrentStep(state, true);
			}
		}

	} // doUpdate_site_access

	/**
	 * /* Actions for vm templates under the "chef_site" root. This method is
	 * called by doContinue. Each template has a hidden field with the value of
	 * template-index that becomes the value of index for the switch statement
	 * here. Some cases not implemented.
	 */
	private void actionForTemplate(String direction, int index,
			ParameterParser params, SessionState state) {
		// Continue - make any permanent changes, Back - keep any data entered
		// on the form
		boolean forward = direction.equals("continue") ? true : false;

		SiteInfo siteInfo = new SiteInfo();

		switch (index) {
		case 0:
			/*
			 * actionForTemplate chef_site-list.vm
			 * 
			 */
			break;
		case 1:
			/*
			 * actionForTemplate chef_site-type.vm
			 * 
			 */
			break;
		case 2:
			/*
			 * actionForTemplate chef_site-newSiteInformation.vm
			 * 
			 */
			if (state.getAttribute(STATE_SITE_INFO) != null) {
				siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			}

			// defaults to be true
			siteInfo.include = true;

			state.setAttribute(STATE_SITE_INFO, siteInfo);
			updateSiteInfo(params, state);

			// alerts after clicking Continue but not Back
			if (forward) {
				if (StringUtil.trimToNull(siteInfo.title) == null) {
					addAlert(state, rb.getString("java.reqfields"));
					state.setAttribute(STATE_TEMPLATE_INDEX, "2");
					return;
				}
			}
			updateSiteAttributes(state);

			if (state.getAttribute(STATE_MESSAGE) == null) {
				updateCurrentStep(state, forward);
			}

			break;
		case 3:
			/*
			 * actionForTemplate chef_site-newSiteFeatures.vm
			 * 
			 */
			if (forward) {
				getFeatures(params, state);
			}
			if (state.getAttribute(STATE_MESSAGE) == null) {
				updateCurrentStep(state, forward);
			}
			break;

		case 4:
			/*
			 * actionForTemplate chef_site-addRemoveFeature.vm
			 * 
			 */
			break;
		case 5:
			/*
			 * actionForTemplate chef_site-addParticipant.vm
			 * 
			 */
			if (forward) {
				checkAddParticipant(params, state);
			} else {
				// remove related state variables
				removeAddParticipantContext(state);
			}
			break;
		case 8:
			/*
			 * actionForTemplate chef_site-siteDeleteConfirm.vm
			 * 
			 */
			break;
		case 10:
			/*
			 * actionForTemplate chef_site-newSiteConfirm.vm
			 * 
			 */
			if (!forward) {
				if (state.getAttribute(STATE_MESSAGE) == null) {
					updateCurrentStep(state, false);
				}
			}
			break;
		case 12:
			/*
			 * actionForTemplate chef_site_siteInfo-list.vm
			 * 
			 */
			break;
		case 13:
			/*
			 * actionForTemplate chef_site_siteInfo-editInfo.vm
			 * 
			 */
			if (forward) {
				Site Site = getStateSite(state);

				if (Site.getType() != null && !Site.getType().equals((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
					// site titel is editable and could not be null
					String title = StringUtil.trimToNull(params
							.getString("title"));
					state.setAttribute(FORM_SITEINFO_TITLE, title);
					if (title == null) {
						addAlert(state, rb.getString("java.specify") + " ");
					}
				}

				String description = StringUtil.trimToNull(params
						.getString("description"));
				state.setAttribute(FORM_SITEINFO_DESCRIPTION, description);

				String short_description = StringUtil.trimToNull(params
						.getString("short_description"));
				state.setAttribute(FORM_SITEINFO_SHORT_DESCRIPTION,
						short_description);

				String skin = params.getString("skin");
				if (skin != null) {
					// if there is a skin input for course site
					skin = StringUtil.trimToNull(skin);
					state.setAttribute(FORM_SITEINFO_SKIN, skin);
				} else {
					// if ther is a icon input for non-course site
					String icon = StringUtil.trimToNull(params
							.getString("icon"));
					if (icon != null) {
						if (icon.endsWith(PROTOCOL_STRING)) {
							addAlert(state, rb.getString("alert.protocol"));
						}
						state.setAttribute(FORM_SITEINFO_ICON_URL, icon);
					} else {
						state.removeAttribute(FORM_SITEINFO_ICON_URL);
					}
				}

				// site contact information
				String contactName = StringUtil.trimToZero(params
						.getString("siteContactName"));
				state.setAttribute(FORM_SITEINFO_CONTACT_NAME, contactName);

				String email = StringUtil.trimToZero(params
						.getString("siteContactEmail"));
				String[] parts = email.split("@");
				if (email.length() > 0
						&& (email.indexOf("@") == -1 || parts.length != 2
								|| parts[0].length() == 0 || !Validator
								.checkEmailLocal(parts[0]))) {
					// invalid email
					addAlert(state, email + " " + rb.getString("java.invalid")
							+ rb.getString("java.theemail"));
				}
				state.setAttribute(FORM_SITEINFO_CONTACT_EMAIL, email);

				if (state.getAttribute(STATE_MESSAGE) == null) {
					state.setAttribute(STATE_TEMPLATE_INDEX, "14");
				}
			}
			break;
		case 14:
			/*
			 * actionForTemplate chef_site_siteInfo-editInfoConfirm.vm
			 * 
			 */
			break;
		case 15:
			/*
			 * actionForTemplate chef_site_siteInfo-addRemoveFeatureConfirm.vm
			 * 
			 */
			break;
		case 18:
			/*
			 * actionForTemplate chef_siteInfo-editAccess.vm
			 * 
			 */
			if (!forward) {
				if (state.getAttribute(STATE_MESSAGE) == null) {
					updateCurrentStep(state, false);
				}
			}
		case 19:
			/*
			 * actionForTemplate chef_site-addParticipant-sameRole.vm
			 * 
			 */
			String roleId = StringUtil.trimToNull(params
					.getString("selectRole"));
			if (roleId == null && forward) {
				addAlert(state, rb.getString("java.pleasesel") + " ");
			} else {
				state.setAttribute("form_selectedRole", params
						.getString("selectRole"));
			}
			break;
		case 20:
			/*
			 * actionForTemplate chef_site-addParticipant-differentRole.vm
			 * 
			 */
			if (forward) {
				getSelectedRoles(state, params, STATE_ADD_PARTICIPANTS);
			}
			break;
		case 21:
			/*
			 * actionForTemplate chef_site-addParticipant-notification.vm '
			 */
			if (params.getString("notify") == null) {
				if (forward)
					addAlert(state, rb.getString("java.pleasechoice") + " ");
			} else {
				state.setAttribute("form_selectedNotify", new Boolean(params
						.getString("notify")));
			}
			break;
		case 22:
			/*
			 * actionForTemplate chef_site-addParticipant-confirm.vm
			 * 
			 */
			break;
		case 24:
			/*
			 * actionForTemplate
			 * chef_site-siteInfo-editAccess-globalAccess-confirm.vm
			 * 
			 */
			break;
		case 26:
			/*
			 * actionForTemplate chef_site-modifyENW.vm
			 * 
			 */
			updateSelectedToolList(state, params, forward);
			if (state.getAttribute(STATE_MESSAGE) == null) {
				updateCurrentStep(state, forward);
			}
			break;
		case 27:
			/*
			 * actionForTemplate chef_site-importSites.vm
			 * 
			 */
			if (forward) {
				Site existingSite = getStateSite(state);
				if (existingSite != null) {
					// revising a existing site's tool
					if (select_import_tools(params, state)) {
						Hashtable importTools = (Hashtable) state
								.getAttribute(STATE_IMPORT_SITE_TOOL);
						List selectedTools = (List) state
								.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
						importToolIntoSite(selectedTools, importTools,
								existingSite);

						existingSite = getStateSite(state); // refresh site for
						// WC and News

						if (state.getAttribute(STATE_MESSAGE) == null) {
							commitSite(existingSite);
							state.removeAttribute(STATE_IMPORT_SITE_TOOL);
							state.removeAttribute(STATE_IMPORT_SITES);
						}
					} else {
						// show alert and remain in current page
						addAlert(state, rb.getString("java.toimporttool"));
					}
				} else {
					// new site
					select_import_tools(params, state);
				}
			} else {
				// read form input about import tools
				select_import_tools(params, state);
			}
			if (state.getAttribute(STATE_MESSAGE) == null) {
				updateCurrentStep(state, forward);
			}
			break;
		case 28:
			/*
			 * actionForTemplate chef_siteinfo-import.vm
			 * 
			 */
			if (forward) {
				if (params.getStrings("importSites") == null) {
					addAlert(state, rb.getString("java.toimport") + " ");
					state.removeAttribute(STATE_IMPORT_SITES);
				} else {
					List importSites = new ArrayList(Arrays.asList(params
							.getStrings("importSites")));
					Hashtable sites = new Hashtable();
					for (index = 0; index < importSites.size(); index++) {
						try {
							Site s = SiteService.getSite((String) importSites
									.get(index));
							sites.put(s, new Vector());
						} catch (IdUnusedException e) {
						}
					}
					state.setAttribute(STATE_IMPORT_SITES, sites);
				}
			}
			break;
		case 29:
			/*
			 * actionForTemplate chef_siteinfo-duplicate.vm
			 * 
			 */
			if (forward) {
				if (state.getAttribute(SITE_DUPLICATED) == null) {
					if (StringUtil.trimToNull(params.getString("title")) == null) {
						addAlert(state, rb.getString("java.dupli") + " ");
					} else {
						String title = params.getString("title");
						state.setAttribute(SITE_DUPLICATED_NAME, title);

						try {
							String oSiteId = (String) state
									.getAttribute(STATE_SITE_INSTANCE_ID);
							String nSiteId = IdManager.createUuid();
							Site site = SiteService.addSite(nSiteId,
									getStateSite(state));

							try {
								SiteService.save(site);
							} catch (IdUnusedException e) {
								// TODO:
							} catch (PermissionException e) {
								// TODO:
							}

							try {
								site = SiteService.getSite(nSiteId);

								// set title
								site.setTitle(title);
								// import tool content
								List pageList = site.getPages();
								if (!((pageList == null) || (pageList.size() == 0))) {
									for (ListIterator i = pageList
											.listIterator(); i.hasNext();) {
										SitePage page = (SitePage) i.next();

										List pageToolList = page.getTools();
										String toolId = ((ToolConfiguration) pageToolList
												.get(0)).getTool().getId();
										if (toolId
												.equalsIgnoreCase("sakai.resources")) {
											// handle
											// resource
											// tool
											// specially
											transferCopyEntities(
													toolId,
													ContentHostingService
															.getSiteCollection(oSiteId),
													ContentHostingService
															.getSiteCollection(nSiteId));
										} else {
											// other
											// tools
											transferCopyEntities(toolId,
													oSiteId, nSiteId);
										}
									}
								}
							} catch (Exception e1) {
								// if goes here, IdService
								// or SiteService has done
								// something wrong.
								M_log.warn(this + "Exception" + e1 + ":"
										+ nSiteId + "when duplicating site");
							}

							if (site.getType().equals((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
								// for course site, need to
								// read in the input for
								// term information
								String termId = StringUtil.trimToNull(params
										.getString("selectTerm"));
								if (termId != null) {
									AcademicSession term = cms.getAcademicSession(termId);
									if (term != null) {
										ResourcePropertiesEdit rp = site.getPropertiesEdit();
										rp.addProperty(PROP_SITE_TERM, term.getTitle());
										rp.addProperty(PROP_SITE_TERM_EID, term.getEid());
									} else {
										M_log.warn("termId=" + termId + " not found");
									}
								}
							}
							try {
								SiteService.save(site);
							} catch (IdUnusedException e) {
								// TODO:
							} catch (PermissionException e) {
								// TODO:
							}

							// TODO: hard coding this frame id
							// is fragile, portal dependent, and
							// needs to be fixed -ggolden
							// schedulePeerFrameRefresh("sitenav");
							scheduleTopRefresh();

							state.setAttribute(SITE_DUPLICATED, Boolean.TRUE);
						} catch (IdInvalidException e) {
							addAlert(state, rb.getString("java.siteinval"));
						} catch (IdUsedException e) {
							addAlert(state, rb.getString("java.sitebeenused")
									+ " ");
						} catch (PermissionException e) {
							addAlert(state, rb.getString("java.allowcreate")
									+ " ");
						}
					}
				}

				if (state.getAttribute(STATE_MESSAGE) == null) {
					// site duplication confirmed
					state.removeAttribute(SITE_DUPLICATED);
					state.removeAttribute(SITE_DUPLICATED_NAME);

					// return to the list view
					state.setAttribute(STATE_TEMPLATE_INDEX, "12");
				}
			}
			break;
		case 33:
			break;
		case 36:
			/*
			 * actionForTemplate chef_site-newSiteCourse.vm
			 */
			if (forward) {
				List providerChosenList = new Vector();
				if (params.getStrings("providerCourseAdd") == null) {
					state.removeAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
					if (params.getString("manualAdds") == null) {
						addAlert(state, rb.getString("java.manual") + " ");
					}
				}
				if (state.getAttribute(STATE_MESSAGE) == null) {
					// The list of courses selected from provider listing
					if (params.getStrings("providerCourseAdd") != null) {
						providerChosenList = new ArrayList(Arrays.asList(params
								.getStrings("providerCourseAdd"))); // list of
						// course
						// ids
						String userId = (String) state
								.getAttribute(STATE_INSTRUCTOR_SELECTED);
						String currentUserId = (String) state
								.getAttribute(STATE_CM_CURRENT_USERID);

						if (userId == null
								|| (userId != null && userId
										.equals(currentUserId))) {
							state.setAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN,
									providerChosenList);
							state.removeAttribute(STATE_CM_AUTHORIZER_SECTIONS);
							state.removeAttribute(FORM_ADDITIONAL);
							state.removeAttribute(STATE_CM_AUTHORIZER_LIST);
						} else {
							// STATE_CM_AUTHORIZER_SECTIONS are SectionObject,
							// so need to prepare it
							// also in this page, u can pick either section from
							// current user OR
							// sections from another users but not both. -
							// daisy's note 1 for now
							// till we are ready to add more complexity
							List sectionObjectList = prepareSectionObject(
									providerChosenList, userId);
							state.setAttribute(STATE_CM_AUTHORIZER_SECTIONS,
									sectionObjectList);
							state
									.removeAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
							// set special instruction & we will keep
							// STATE_CM_AUTHORIZER_LIST
							String additional = StringUtil.trimToZero(params
									.getString("additional"));
							state.setAttribute(FORM_ADDITIONAL, additional);
						}
					}
					collectNewSiteInfo(siteInfo, state, params,
							providerChosenList);
				}
				// next step
				state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer(2));
			}
			break;
		case 38:
			break;
		case 39:
			break;
		case 42:
			/*
			 * actionForTemplate chef_site-gradtoolsConfirm.vm
			 * 
			 */
			break;
		case 43:
			/*
			 * actionForTemplate chef_site-editClass.vm
			 * 
			 */
			if (forward) {
				if (params.getStrings("providerClassDeletes") == null
						&& params.getStrings("manualClassDeletes") == null
						&& params.getStrings("cmRequestedClassDeletes") == null
						&& !direction.equals("back")) {
					addAlert(state, rb.getString("java.classes"));
				}

				if (params.getStrings("providerClassDeletes") != null) {
					// build the deletions list
					List providerCourseList = (List) state
							.getAttribute(SITE_PROVIDER_COURSE_LIST);
					List providerCourseDeleteList = new ArrayList(Arrays
							.asList(params.getStrings("providerClassDeletes")));
					for (ListIterator i = providerCourseDeleteList
							.listIterator(); i.hasNext();) {
						providerCourseList.remove((String) i.next());
					}
					state.setAttribute(SITE_PROVIDER_COURSE_LIST,
							providerCourseList);
				}
				if (params.getStrings("manualClassDeletes") != null) {
					// build the deletions list
					List manualCourseList = (List) state
							.getAttribute(SITE_MANUAL_COURSE_LIST);
					List manualCourseDeleteList = new ArrayList(Arrays
							.asList(params.getStrings("manualClassDeletes")));
					for (ListIterator i = manualCourseDeleteList.listIterator(); i
							.hasNext();) {
						manualCourseList.remove((String) i.next());
					}
					state.setAttribute(SITE_MANUAL_COURSE_LIST,
							manualCourseList);
				}
				
				if (params.getStrings("cmRequestedClassDeletes") != null) {
					// build the deletions list
					List<SectionObject> cmRequestedCourseList = (List) state.getAttribute(STATE_CM_REQUESTED_SECTIONS);
					List<String> cmRequestedCourseDeleteList = new ArrayList(Arrays.asList(params.getStrings("cmRequestedClassDeletes")));
					for (ListIterator i = cmRequestedCourseDeleteList.listIterator(); i
							.hasNext();) {
						String sectionId = (String) i.next();
						try
						{
							SectionObject so = new SectionObject(cms.getSection(sectionId));
							SectionObject soFound = null;
							for (Iterator j = cmRequestedCourseList.iterator(); soFound == null && j.hasNext();)
							{
								SectionObject k = (SectionObject) j.next();
								if (k.eid.equals(sectionId))
								{
									soFound = k;
								}
							}
							if (soFound != null) cmRequestedCourseList.remove(soFound);
						}
						catch (Exception e)
						{
							M_log.warn(e.getMessage() + sectionId);
						}
					}
					state.setAttribute(STATE_CM_REQUESTED_SECTIONS, cmRequestedCourseList);
				}

				updateCourseClasses(state, new Vector(), new Vector());
			}
			break;
		case 44:
			if (forward) {
				AcademicSession a = (AcademicSession) state.getAttribute(STATE_TERM_SELECTED);
				Site site = getStateSite(state);
				ResourcePropertiesEdit pEdit = site.getPropertiesEdit();
				
				// update the course site property and realm based on the selection
				updateCourseSiteSections(state, site.getId(), pEdit, a);
				try
				{
					SiteService.save(site);
				}
				catch (Exception e)
				{
					M_log.warn(e.getMessage() + site.getId());
				}
				
				removeAddClassContext(state);
			}

			break;
		case 49:
			if (!forward) {
				state.removeAttribute(SORTED_BY);
				state.removeAttribute(SORTED_ASC);
			}
			break;
		}

	}// actionFor Template

	/**
	 * update current step index within the site creation wizard
	 * 
	 * @param state
	 *            The SessionState object
	 * @param forward
	 *            Moving forward or backward?
	 */
	private void updateCurrentStep(SessionState state, boolean forward) {
		if (state.getAttribute(SITE_CREATE_CURRENT_STEP) != null) {
			int currentStep = ((Integer) state
					.getAttribute(SITE_CREATE_CURRENT_STEP)).intValue();
			if (forward) {
				state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer(
						currentStep + 1));
			} else {
				state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer(
						currentStep - 1));
			}
		}
	}

	/**
	 * remove related state variable for adding class
	 * 
	 * @param state
	 *            SessionState object
	 */
	private void removeAddClassContext(SessionState state) {
		// remove related state variables
		state.removeAttribute(STATE_ADD_CLASS_MANUAL);
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_NUMBER);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
		state.removeAttribute(STATE_SITE_QUEST_UNIQNAME);
		state.removeAttribute(STATE_AUTO_ADD);
		state.removeAttribute(SITE_CREATE_TOTAL_STEPS);
		state.removeAttribute(SITE_CREATE_CURRENT_STEP);
		state.removeAttribute(STATE_IMPORT_SITE_TOOL);
		state.removeAttribute(STATE_IMPORT_SITES);
		state.removeAttribute(STATE_CM_REQUESTED_SECTIONS);
		state.removeAttribute(STATE_CM_SELECTED_SECTIONS);
		sitePropertiesIntoState(state);

	} // removeAddClassContext

	private void updateCourseClasses(SessionState state, List notifyClasses,
			List requestClasses) {
		List providerCourseSectionList = (List) state.getAttribute(SITE_PROVIDER_COURSE_LIST);
		List manualCourseSectionList = (List) state.getAttribute(SITE_MANUAL_COURSE_LIST);
		List<SectionObject> cmRequestedCourseList = (List) state.getAttribute(STATE_CM_REQUESTED_SECTIONS);
		
		Site site = getStateSite(state);
		String id = site.getId();
		String realmId = SiteService.siteReference(id);

		if ((providerCourseSectionList == null)
				|| (providerCourseSectionList.size() == 0)) {
			// no section access so remove Provider Id
			try {
				AuthzGroup realmEdit1 = AuthzGroupService
						.getAuthzGroup(realmId);
				realmEdit1.setProviderGroupId(NULL_STRING);
				AuthzGroupService.save(realmEdit1);
			} catch (GroupNotDefinedException e) {
				M_log.warn(this + " IdUnusedException, " + site.getTitle()
						+ "(" + realmId
						+ ") not found, or not an AuthzGroup object");
				addAlert(state, rb.getString("java.cannotedit"));
				return;
			} catch (AuthzPermissionException e) {
				M_log
						.warn(this
								+ " PermissionException, user does not have permission to edit AuthzGroup object "
								+ site.getTitle() + "(" + realmId + "). ");
				addAlert(state, rb.getString("java.notaccess"));
				return;
			}
		}
		if ((providerCourseSectionList != null)
				&& (providerCourseSectionList.size() != 0)) {
			// section access so rewrite Provider Id, don't need the current realm provider String
			String externalRealm = buildExternalRealm(id, state,
					providerCourseSectionList, null);
			try {
				AuthzGroup realmEdit2 = AuthzGroupService
						.getAuthzGroup(realmId);
				realmEdit2.setProviderGroupId(externalRealm);
				AuthzGroupService.save(realmEdit2);
			} catch (GroupNotDefinedException e) {
				M_log.warn(this + " IdUnusedException, " + site.getTitle()
						+ "(" + realmId
						+ ") not found, or not an AuthzGroup object");
				addAlert(state, rb.getString("java.cannotclasses"));
				return;
			} catch (AuthzPermissionException e) {
				M_log
						.warn(this
								+ " PermissionException, user does not have permission to edit AuthzGroup object "
								+ site.getTitle() + "(" + realmId + "). ");
				addAlert(state, rb.getString("java.notaccess"));
				return;
			}

		}

		// the manual request course into properties
		setSiteSectionProperty(manualCourseSectionList, site, PROP_SITE_REQUEST_COURSE);
		
		// the cm request course into properties
		setSiteSectionProperty(cmRequestedCourseList, site, STATE_CM_REQUESTED_SECTIONS);
		
		// clean the related site groups
		// if the group realm provider id is not listed for the site, remove the related group
		for (Iterator iGroups = site.getGroups().iterator(); iGroups.hasNext();)
		{
			Group group = (Group) iGroups.next();
			try
			{
				AuthzGroup gRealm = AuthzGroupService.getAuthzGroup(group.getReference());
				String gProviderId = StringUtil.trimToNull(gRealm.getProviderGroupId());
				if (gProviderId != null)
				{ 
					if ((manualCourseSectionList== null && cmRequestedCourseList == null)
						|| (manualCourseSectionList != null && !manualCourseSectionList.contains(gProviderId) && cmRequestedCourseList == null)
						|| (manualCourseSectionList == null && cmRequestedCourseList != null && !cmRequestedCourseList.contains(gProviderId))
						|| (manualCourseSectionList != null && !manualCourseSectionList.contains(gProviderId) && cmRequestedCourseList != null && !cmRequestedCourseList.contains(gProviderId)))
					{
						AuthzGroupService.removeAuthzGroup(group.getReference());
					}
				}
			}
			catch (Exception e)
			{
				M_log.warn(this + ".updateCourseClasses: cannot remove authzgroup : " + group.getReference());
			}
		}

		if (state.getAttribute(STATE_MESSAGE) == null) {
			commitSite(site);
		} else {
		}
		if (requestClasses != null && requestClasses.size() > 0
				&& state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
			try {
				// send out class request notifications
				sendSiteRequest(state, "change", 
								((Integer) state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER)).intValue(), 
								(List<SectionObject>) state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS), 
								"manual");
			} catch (Exception e) {
				M_log.warn(this + e.toString());
			}
		}
		if (notifyClasses != null && notifyClasses.size() > 0) {
			try {
				// send out class access confirmation notifications
				sendSiteNotification(state, notifyClasses);
			} catch (Exception e) {
				M_log.warn(this + e.toString());
			}
		}
	} // updateCourseClasses

	private void setSiteSectionProperty(List courseSectionList, Site site, String propertyName) {
		if ((courseSectionList != null) && (courseSectionList.size() != 0)) {
			// store the requested sections in one site property
			String sections = "";
			for (int j = 0; j < courseSectionList.size();) {
				sections = sections
						+ (String) courseSectionList.get(j);
				j++;
				if (j < courseSectionList.size()) {
					sections = sections + "+";
				}
			}
			ResourcePropertiesEdit rp = site.getPropertiesEdit();
			rp.addProperty(propertyName, sections);
		} else {
			ResourcePropertiesEdit rp = site.getPropertiesEdit();
			rp.removeProperty(propertyName);
		}
	}

	/**
	 * Sets selected roles for multiple users
	 * 
	 * @param params
	 *            The ParameterParser object
	 * @param listName
	 *            The state variable
	 */
	private void getSelectedRoles(SessionState state, ParameterParser params,
			String listName) {
		Hashtable pSelectedRoles = (Hashtable) state
				.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES);
		if (pSelectedRoles == null) {
			pSelectedRoles = new Hashtable();
		}
		List userList = (List) state.getAttribute(listName);
		for (int i = 0; i < userList.size(); i++) {
			String userId = null;

			if (listName.equalsIgnoreCase(STATE_ADD_PARTICIPANTS)) {
				userId = ((Participant) userList.get(i)).getUniqname();
			} else if (listName.equalsIgnoreCase(STATE_SELECTED_USER_LIST)) {
				userId = (String) userList.get(i);
			}

			if (userId != null) {
				String rId = StringUtil.trimToNull(params.getString("role"
						+ userId));
				if (rId == null) {
					addAlert(state, rb.getString("java.rolefor") + " " + userId
							+ ". ");
					pSelectedRoles.remove(userId);
				} else {
					pSelectedRoles.put(userId, rId);
				}
			}
		}
		state.setAttribute(STATE_SELECTED_PARTICIPANT_ROLES, pSelectedRoles);

	} // getSelectedRoles

	/**
	 * dispatch function for changing participants roles
	 */
	public void doSiteinfo_edit_role(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String option = params.getString("option");
		// dispatch
		if (option.equalsIgnoreCase("same_role_true")) {
			state.setAttribute(STATE_CHANGEROLE_SAMEROLE, Boolean.TRUE);
			state.setAttribute(STATE_CHANGEROLE_SAMEROLE_ROLE, params
					.getString("role_to_all"));
		} else if (option.equalsIgnoreCase("same_role_false")) {
			state.setAttribute(STATE_CHANGEROLE_SAMEROLE, Boolean.FALSE);
			state.removeAttribute(STATE_CHANGEROLE_SAMEROLE_ROLE);
			if (state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES) == null) {
				state.setAttribute(STATE_SELECTED_PARTICIPANT_ROLES,
						new Hashtable());
			}
		} else if (option.equalsIgnoreCase("continue")) {
			doContinue(data);
		} else if (option.equalsIgnoreCase("back")) {
			doBack(data);
		} else if (option.equalsIgnoreCase("cancel")) {
			doCancel(data);
		}
	} // doSiteinfo_edit_globalAccess

	/**
	 * dispatch function for changing site global access
	 */
	public void doSiteinfo_edit_globalAccess(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String option = params.getString("option");

		// dispatch
		if (option.equalsIgnoreCase("joinable")) {
			state.setAttribute("form_joinable", Boolean.TRUE);
			state.setAttribute("form_joinerRole", getStateSite(state)
					.getJoinerRole());
		} else if (option.equalsIgnoreCase("unjoinable")) {
			state.setAttribute("form_joinable", Boolean.FALSE);
			state.removeAttribute("form_joinerRole");
		} else if (option.equalsIgnoreCase("continue")) {
			doContinue(data);
		} else if (option.equalsIgnoreCase("cancel")) {
			doCancel(data);
		}
	} // doSiteinfo_edit_globalAccess

	/**
	 * save changes to site global access
	 */
	public void doSiteinfo_save_globalAccess(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		Site s = getStateSite(state);
		boolean joinable = ((Boolean) state.getAttribute("form_joinable"))
				.booleanValue();
		s.setJoinable(joinable);
		if (joinable) {
			// set the joiner role
			String joinerRole = (String) state.getAttribute("form_joinerRole");
			s.setJoinerRole(joinerRole);
		}

		if (state.getAttribute(STATE_MESSAGE) == null) {
			// release site edit
			commitSite(s);

			state.setAttribute(STATE_TEMPLATE_INDEX, "18");
		}

	} // doSiteinfo_save_globalAccess

	/**
	 * updateSiteAttributes
	 * 
	 */
	private void updateSiteAttributes(SessionState state) {
		SiteInfo siteInfo = new SiteInfo();
		if (state.getAttribute(STATE_SITE_INFO) != null) {
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
		} else {
			M_log
					.warn("SiteAction.updateSiteAttributes STATE_SITE_INFO == null");
			return;
		}

		Site site = getStateSite(state);

		if (site != null) {
			if (StringUtil.trimToNull(siteInfo.title) != null) {
				site.setTitle(siteInfo.title);
			}
			if (siteInfo.description != null) {
				site.setDescription(siteInfo.description);
			}
			site.setPublished(siteInfo.published);

			setAppearance(state, site, siteInfo.iconUrl);

			site.setJoinable(siteInfo.joinable);
			if (StringUtil.trimToNull(siteInfo.joinerRole) != null) {
				site.setJoinerRole(siteInfo.joinerRole);
			}
			// Make changes and then put changed site back in state
			String id = site.getId();

			try {
				SiteService.save(site);
			} catch (IdUnusedException e) {
				// TODO:
			} catch (PermissionException e) {
				// TODO:
			}

			if (SiteService.allowUpdateSite(id)) {
				try {
					SiteService.getSite(id);
					state.setAttribute(STATE_SITE_INSTANCE_ID, id);
				} catch (IdUnusedException e) {
					M_log.warn("SiteAction.commitSite IdUnusedException "
							+ siteInfo.getTitle() + "(" + id + ") not found");
				}
			}

			// no permission
			else {
				addAlert(state, rb.getString("java.makechanges"));
				M_log.warn("SiteAction.commitSite PermissionException "
						+ siteInfo.getTitle() + "(" + id + ")");
			}
		}

	} // updateSiteAttributes

	/**
	 * %%% legacy properties, to be removed
	 */
	private void updateSiteInfo(ParameterParser params, SessionState state) {
		SiteInfo siteInfo = new SiteInfo();
		if (state.getAttribute(STATE_SITE_INFO) != null) {
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
		}
		siteInfo.site_type = (String) state.getAttribute(STATE_SITE_TYPE);

		if (params.getString("title") != null) {
			siteInfo.title = params.getString("title");
		}
		if (params.getString("description") != null) {
			siteInfo.description = params.getString("description");
		}
		if (params.getString("short_description") != null) {
			siteInfo.short_description = params.getString("short_description");
		}
		if (params.getString("additional") != null) {
			siteInfo.additional = params.getString("additional");
		}
		if (params.getString("iconUrl") != null) {
			siteInfo.iconUrl = params.getString("iconUrl");
		} else {
			siteInfo.iconUrl = params.getString("skin");
		}
		if (params.getString("joinerRole") != null) {
			siteInfo.joinerRole = params.getString("joinerRole");
		}
		if (params.getString("joinable") != null) {
			boolean joinable = params.getBoolean("joinable");
			siteInfo.joinable = joinable;
			if (!joinable)
				siteInfo.joinerRole = NULL_STRING;
		}
		if (params.getString("itemStatus") != null) {
			siteInfo.published = Boolean
					.valueOf(params.getString("itemStatus")).booleanValue();
		}

		// site contact information
		String name = StringUtil
				.trimToZero(params.getString("siteContactName"));
		siteInfo.site_contact_name = name;
		String email = StringUtil.trimToZero(params
				.getString("siteContactEmail"));
		if (email != null) {
			String[] parts = email.split("@");

			if (email.length() > 0
					&& (email.indexOf("@") == -1 || parts.length != 2
							|| parts[0].length() == 0 || !Validator
							.checkEmailLocal(parts[0]))) {
				// invalid email
				addAlert(state, email + " " + rb.getString("java.invalid")
						+ rb.getString("java.theemail"));
			}
			siteInfo.site_contact_email = email;
		}
		state.setAttribute(STATE_SITE_INFO, siteInfo);

	} // updateSiteInfo

	/**
	 * getExternalRealmId
	 * 
	 */
	private String getExternalRealmId(SessionState state) {
		String realmId = SiteService.siteReference((String) state
				.getAttribute(STATE_SITE_INSTANCE_ID));
		String rv = null;
		try {
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			rv = realm.getProviderGroupId();
		} catch (GroupNotDefinedException e) {
			M_log.warn("SiteAction.getExternalRealmId, site realm not found");
		}
		return rv;

	} // getExternalRealmId

	/**
	 * getParticipantList
	 * 
	 */
	private Collection getParticipantList(SessionState state) {
		List members = new Vector();
		String realmId = SiteService.siteReference((String) state
				.getAttribute(STATE_SITE_INSTANCE_ID));

		List providerCourseList = null;
		providerCourseList = getProviderCourseList(StringUtil
				.trimToNull(getExternalRealmId(state)));
		if (providerCourseList != null && providerCourseList.size() > 0) {
			state.setAttribute(SITE_PROVIDER_COURSE_LIST, providerCourseList);
		}

		Collection participants = prepareParticipants(realmId, providerCourseList);
		state.setAttribute(STATE_PARTICIPANT_LIST, participants);

		return participants;

	} // getParticipantList

	private Collection prepareParticipants(String realmId, List providerCourseList) {
		Map participantsMap = new ConcurrentHashMap();
		try {
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			realm.getProviderGroupId();
			
			// iterate through the provider list first
			for (Iterator i=providerCourseList.iterator(); i.hasNext();)
			{
				String providerCourseEid = (String) i.next();
				try
				{
					Section section = cms.getSection(providerCourseEid);
					if (section != null)
					{
						// in case of Section eid
						EnrollmentSet enrollmentSet = section.getEnrollmentSet();
						addParticipantsFromEnrollmentSet(participantsMap, realm, providerCourseEid, enrollmentSet, section.getTitle());
						// add memberships
						Set memberships = cms.getSectionMemberships(providerCourseEid);
						addParticipantsFromMemberships(participantsMap, realm, providerCourseEid, memberships, section.getTitle());
					}
				}
				catch (IdNotFoundException e)
				{
					M_log.warn("SiteAction prepareParticipants " + e.getMessage() + " sectionId=" + providerCourseEid);
				}
			}
			
			// now for those not provided users
			Set grants = realm.getMembers();
			for (Iterator i = grants.iterator(); i.hasNext();) {
				Member g = (Member) i.next();
				if (!g.isProvided())
				{
					try {
						User user = UserDirectoryService.getUserByEid(g.getUserEid());
						String userId = user.getId();
						Participant participant;
						if (participantsMap.containsKey(userId))
						{
							participant = (Participant) participantsMap.get(userId);
						}
						else
						{
							participant = new Participant();
						}
						participant.name = user.getSortName();
						participant.uniqname = userId;
						participant.role = g.getRole()!=null?g.getRole().getId():"";
						participant.removeable = true;
						participantsMap.put(userId, participant);
					} catch (UserNotDefinedException e) {
						// deal with missing user quietly without throwing a
						// warning message
						M_log.warn(e.getMessage());
					}
				}
			}

		} catch (GroupNotDefinedException ee) {
			M_log.warn(this + "  IdUnusedException " + realmId);
		}
		return participantsMap.values();
	}

	/**
	 * Add participant from provider-defined membership set
	 * @param participants
	 * @param realm
	 * @param providerCourseEid
	 * @param memberships
	 */
	private void addParticipantsFromMemberships(Map participantsMap, AuthzGroup realm, String providerCourseEid, Set memberships, String sectionTitle) {
		if (memberships != null)
		{
			for (Iterator mIterator = memberships.iterator();mIterator.hasNext();)
			{
				Membership m = (Membership) mIterator.next();
				try 
				{
					User user = UserDirectoryService.getUserByEid(m.getUserId());
					String userId = user.getId();
					Member member = realm.getMember(userId);
					if (member != null && member.isProvided())
					{
						// get or add provided participant
						Participant participant;
						if (participantsMap.containsKey(userId))
						{
							participant = (Participant) participantsMap.get(userId);
							if (!participant.getSectionEidList().contains(sectionTitle)) {
								participant.section = participant.section.concat(", <br />" + sectionTitle);
							}
						}
						else
						{
							participant = new Participant();
							participant.credits = "";
							participant.name = user.getSortName();
							participant.providerRole = member.getRole()!=null?member.getRole().getId():"";
							participant.regId = "";
							participant.removeable = false;
							participant.role = member.getRole()!=null?member.getRole().getId():"";
							participant.addSectionEidToList(sectionTitle);
							participant.uniqname = userId;
						}
						
						participantsMap.put(userId, participant);
					}
				} catch (UserNotDefinedException exception) {
					// deal with missing user quietly without throwing a
					// warning message
					M_log.warn(exception);
				}
			}
		}
	}

	/**
	 * Add participant from provider-defined enrollment set
	 * @param participants
	 * @param realm
	 * @param providerCourseEid
	 * @param enrollmentSet
	 */
	private void addParticipantsFromEnrollmentSet(Map participantsMap, AuthzGroup realm, String providerCourseEid, EnrollmentSet enrollmentSet, String sectionTitle) {
		if (enrollmentSet != null)
		{
			Set enrollments = cms.getEnrollments(enrollmentSet.getEid());
			if (enrollments != null)
			{
				for (Iterator eIterator = enrollments.iterator();eIterator.hasNext();)
				{
					Enrollment e = (Enrollment) eIterator.next();
					try 
					{
						User user = UserDirectoryService.getUserByEid(e.getUserId());
						String userId = user.getId();
						Member member = realm.getMember(userId);
						if (member != null && member.isProvided())
						{
							try
							{
							// get or add provided participant
							Participant participant;
							if (participantsMap.containsKey(userId))
							{
								participant = (Participant) participantsMap.get(userId);
								//does this section contain the eid already
								if (!participant.getSectionEidList().contains(sectionTitle)) {
									participant.addSectionEidToList(sectionTitle);
								}
								participant.credits = participant.credits.concat(", <br />" + e.getCredits());
							}
							else
							{
								participant = new Participant();
								participant.credits = e.getCredits();
								participant.name = user.getSortName();
								participant.providerRole = member.getRole()!=null?member.getRole().getId():"";
								participant.regId = "";
								participant.removeable = false;
								participant.role = member.getRole()!=null?member.getRole().getId():"";
								participant.addSectionEidToList(sectionTitle);
								participant.uniqname = userId;
							}
							participantsMap.put(userId, participant);
							}
							catch (Exception ee)
							{
								M_log.warn(ee.getMessage());
							}
						}
					} catch (UserNotDefinedException exception) {
						// deal with missing user quietly without throwing a
						// warning message
						M_log.warn(exception.getMessage());
					}
				}
			}
		}
	}

	/**
	 * getRoles
	 * 
	 */
	private List getRoles(SessionState state) {
		List roles = new Vector();
		String realmId = SiteService.siteReference((String) state
				.getAttribute(STATE_SITE_INSTANCE_ID));
		try {
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			roles.addAll(realm.getRoles());
			Collections.sort(roles);
		} catch (GroupNotDefinedException e) {
			M_log.warn("SiteAction.getRoles IdUnusedException " + realmId);
		}
		return roles;

	} // getRoles

	private void addSynopticTool(SitePage page, String toolId,
			String toolTitle, String layoutHint) {
		// Add synoptic announcements tool
		ToolConfiguration tool = page.addTool();
		Tool reg = ToolManager.getTool(toolId);
		tool.setTool(toolId, reg);
		tool.setTitle(toolTitle);
		tool.setLayoutHints(layoutHint);
	}

	private void getRevisedFeatures(ParameterParser params, SessionState state) {
		Site site = getStateSite(state);
		// get the list of Worksite Setup configured pages
		List wSetupPageList = (List) state
				.getAttribute(STATE_WORKSITE_SETUP_PAGE_LIST);

		WorksiteSetupPage wSetupPage = new WorksiteSetupPage();
		WorksiteSetupPage wSetupHome = new WorksiteSetupPage();
		List pageList = new Vector();

		// declare some flags used in making decisions about Home, whether to
		// add, remove, or do nothing
		boolean homeInChosenList = false;
		boolean homeInWSetupPageList = false;

		List chosenList = (List) state
				.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		// if features were selected, diff wSetupPageList and chosenList to get
		// page adds and removes
		// boolean values for adding synoptic views
		boolean hasAnnouncement = false;
		boolean hasSchedule = false;
		boolean hasChat = false;
		boolean hasDiscussion = false;
		boolean hasEmail = false;
		boolean hasNewSiteInfo = false;
		boolean hasMessageCenter = false;

		// Special case - Worksite Setup Home comes from a hardcoded checkbox on
		// the vm template rather than toolRegistrationList
		// see if Home was chosen
		for (ListIterator j = chosenList.listIterator(); j.hasNext();) {
			String choice = (String) j.next();
			if (choice.equalsIgnoreCase(HOME_TOOL_ID)) {
				homeInChosenList = true;
			} else if (choice.equals("sakai.mailbox")) {
				hasEmail = true;
				String alias = StringUtil.trimToNull((String) state
						.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
				if (alias != null) {
					if (!Validator.checkEmailLocal(alias)) {
						addAlert(state, rb.getString("java.theemail"));
					} else {
						try {
							String channelReference = mailArchiveChannelReference(site
									.getId());
							// first, clear any alias set to this channel
							AliasService.removeTargetAliases(channelReference); // check
							// to
							// see
							// whether
							// the
							// alias
							// has
							// been
							// used
							try {
								String target = AliasService.getTarget(alias);
								if (target != null) {
									addAlert(state, rb
											.getString("java.emailinuse")
											+ " ");
								}
							} catch (IdUnusedException ee) {
								try {
									AliasService.setAlias(alias,
											channelReference);
								} catch (IdUsedException exception) {
								} catch (IdInvalidException exception) {
								} catch (PermissionException exception) {
								}
							}
						} catch (PermissionException exception) {
						}
					}
				}
			} else if (choice.equals("sakai.announcements")) {
				hasAnnouncement = true;
			} else if (choice.equals("sakai.schedule")) {
				hasSchedule = true;
			} else if (choice.equals("sakai.chat")) {
				hasChat = true;
			} else if (choice.equals("sakai.discussion")) {
				hasDiscussion = true;
			}  else if (choice.equals("sakai.messages") || choice.equals("sakai.forums") || choice.equals("sakai.messagecenter")) {
				hasMessageCenter = true;
			}
		}

		// see if Home and/or Help in the wSetupPageList (can just check title
		// here, because we checked patterns before adding to the list)
		for (ListIterator i = wSetupPageList.listIterator(); i.hasNext();) {
			wSetupPage = (WorksiteSetupPage) i.next();
			if (wSetupPage.getToolId().equals(HOME_TOOL_ID)) {
				homeInWSetupPageList = true;
			}
		}

		if (homeInChosenList) {
			SitePage page = null;
			// Were the synoptic views of Announcement, Discussioin, Chat
			// existing before the editing
			boolean hadAnnouncement = false, hadDiscussion = false, hadChat = false, hadSchedule = false, hadMessageCenter = false;

			if (homeInWSetupPageList) {
				if (!SiteService.isUserSite(site.getId())) {
					// for non-myworkspace site, if Home is chosen and Home is
					// in the wSetupPageList, remove synoptic tools
					WorksiteSetupPage homePage = new WorksiteSetupPage();
					for (ListIterator i = wSetupPageList.listIterator(); i
							.hasNext();) {
						WorksiteSetupPage comparePage = (WorksiteSetupPage) i
								.next();
						if ((comparePage.getToolId()).equals(HOME_TOOL_ID)) {
							homePage = comparePage;
						}
					}
					page = site.getPage(homePage.getPageId());
					List toolList = page.getTools();
					List removeToolList = new Vector();
					// get those synoptic tools
					for (ListIterator iToolList = toolList.listIterator(); iToolList
							.hasNext();) {
						ToolConfiguration tool = (ToolConfiguration) iToolList
								.next();
						Tool t = tool.getTool();
						if (t!= null)
						{ 
							if (t.getId().equals("sakai.synoptic.announcement")) {
								hadAnnouncement = true;
								if (!hasAnnouncement) {
									removeToolList.add(tool);// if Announcement
									// tool isn't
									// selected, remove
									// the synotic
									// Announcement
								}
							}
							else if (t.getId().equals(TOOL_ID_SUMMARY_CALENDAR)) {
								hadSchedule = true;
								if (!hasSchedule || !notStealthOrHiddenTool(TOOL_ID_SUMMARY_CALENDAR)) {
									// if Schedule tool isn't selected, or the summary calendar tool is stealthed or hidden, remove the synotic Schedule
									removeToolList.add(tool);
								}
							}
							else if (t.getId().equals("sakai.synoptic.discussion")) {
								hadDiscussion = true;
								if (!hasDiscussion) {
									removeToolList.add(tool);// if Discussion
									// tool isn't
									// selected, remove
									// the synoptic
									// Discussion
								}
							}
							else if (t.getId().equals("sakai.synoptic.chat")) {
								hadChat = true;
								if (!hasChat) {
									removeToolList.add(tool);// if Chat tool
									// isn't selected,
									// remove the
									// synoptic Chat
								}
							}
							else if (t.getId().equals("sakai.synoptic.messagecenter")) {
								hadMessageCenter = true;
								if (!hasMessageCenter) {
									removeToolList.add(tool);// if Messages and/or Forums tools
									// isn't selected,
									// remove the
									// synoptic Message Center tool
								}
							}
						}
					}
					// remove those synoptic tools
					for (ListIterator rToolList = removeToolList.listIterator(); rToolList
							.hasNext();) {
						page.removeTool((ToolConfiguration) rToolList.next());
					}
				}
			} else {
				// if Home is chosen and Home is not in wSetupPageList, add Home
				// to site and wSetupPageList
				page = site.addPage();

				page.setTitle(rb.getString("java.home"));

				wSetupHome.pageId = page.getId();
				wSetupHome.pageTitle = page.getTitle();
				wSetupHome.toolId = HOME_TOOL_ID;
				wSetupPageList.add(wSetupHome);

				// Add worksite information tool
				ToolConfiguration tool = page.addTool();
				Tool reg = ToolManager.getTool("sakai.iframe.site");
				tool.setTool("sakai.iframe.site", reg);
				tool.setTitle(reg.getTitle());
				tool.setLayoutHints("0,0");
			}

			if (!SiteService.isUserSite(site.getId())) {
				// add synoptical tools to home tool in non-myworkspace site
				try {
					if (hasAnnouncement && !hadAnnouncement) {
						// Add synoptic announcements tool
						addSynopticTool(page, "sakai.synoptic.announcement", rb
								.getString("java.recann"), "0,1");
					}
					if (hasDiscussion && !hadDiscussion) {
						// Add synoptic discussion tool
						addSynopticTool(page, "sakai.synoptic.discussion", rb
								.getString("java.recdisc"), "1,1");
					}
					if (hasChat && !hadChat) {
						// Add synoptic chat tool
						addSynopticTool(page, "sakai.synoptic.chat", rb
								.getString("java.recent"), "2,1");
					}
					if (hasSchedule && !hadSchedule) {
						// Add synoptic schedule tool if not stealth or hidden
						if (notStealthOrHiddenTool(TOOL_ID_SUMMARY_CALENDAR))
						addSynopticTool(page, TOOL_ID_SUMMARY_CALENDAR, rb
								.getString("java.reccal"), "3,1");
					}
					if (hasMessageCenter && !hadMessageCenter) {
						// Add synoptic Message Center
						addSynopticTool(page, "sakai.synoptic.messagecenter", rb
								.getString("java.recmsg"), "4,1");
					}
					if (hasAnnouncement || hasDiscussion || hasChat
							|| hasSchedule || hasMessageCenter) {
						page.setLayout(SitePage.LAYOUT_DOUBLE_COL);
					} else {
						page.setLayout(SitePage.LAYOUT_SINGLE_COL);
					}

				} catch (Exception e) {
					M_log.warn("SiteAction getRevisedFeatures Exception: " + e.getMessage());
				}
			}
		} // add Home

		// if Home is in wSetupPageList and not chosen, remove Home feature from
		// wSetupPageList and site
		if (!homeInChosenList && homeInWSetupPageList) {
			// remove Home from wSetupPageList
			WorksiteSetupPage removePage = new WorksiteSetupPage();
			for (ListIterator i = wSetupPageList.listIterator(); i.hasNext();) {
				WorksiteSetupPage comparePage = (WorksiteSetupPage) i.next();
				if (comparePage.getToolId().equals(HOME_TOOL_ID)) {
					removePage = comparePage;
				}
			}
			SitePage siteHome = site.getPage(removePage.getPageId());
			site.removePage(siteHome);
			wSetupPageList.remove(removePage);

		}

		// declare flags used in making decisions about whether to add, remove,
		// or do nothing
		boolean inChosenList;
		boolean inWSetupPageList;

		Hashtable newsTitles = (Hashtable) state
				.getAttribute(STATE_NEWS_TITLES);
		Hashtable wcTitles = (Hashtable) state
				.getAttribute(STATE_WEB_CONTENT_TITLES);
		Hashtable newsUrls = (Hashtable) state.getAttribute(STATE_NEWS_URLS);
		Hashtable wcUrls = (Hashtable) state
				.getAttribute(STATE_WEB_CONTENT_URLS);

		Set categories = new HashSet();
		categories.add((String) state.getAttribute(STATE_SITE_TYPE));
		Set toolRegistrationList = ToolManager.findTools(categories, null);

		// first looking for any tool for removal
		Vector removePageIds = new Vector();
		for (ListIterator k = wSetupPageList.listIterator(); k.hasNext();) {
			wSetupPage = (WorksiteSetupPage) k.next();
			String pageToolId = wSetupPage.getToolId();

			// use page id + tool id for multiple News and Web Content tool
			if (pageToolId.indexOf("sakai.news") != -1
					|| pageToolId.indexOf("sakai.iframe") != -1) {
				pageToolId = wSetupPage.getPageId() + pageToolId;
			}

			inChosenList = false;

			for (ListIterator j = chosenList.listIterator(); j.hasNext();) {
				String toolId = (String) j.next();
				if (pageToolId.equals(toolId)) {
					inChosenList = true;
				}
			}

			if (!inChosenList) {
				removePageIds.add(wSetupPage.getPageId());
			}
		}
		for (int i = 0; i < removePageIds.size(); i++) {
			// if the tool exists in the wSetupPageList, remove it from the site
			String removeId = (String) removePageIds.get(i);
			SitePage sitePage = site.getPage(removeId);
			site.removePage(sitePage);

			// and remove it from wSetupPageList
			for (ListIterator k = wSetupPageList.listIterator(); k.hasNext();) {
				wSetupPage = (WorksiteSetupPage) k.next();
				if (!wSetupPage.getPageId().equals(removeId)) {
					wSetupPage = null;
				}
			}
			if (wSetupPage != null) {
				wSetupPageList.remove(wSetupPage);
			}
		}

		// then looking for any tool to add
		for (ListIterator j = orderToolIds(state,
				(String) state.getAttribute(STATE_SITE_TYPE), chosenList)
				.listIterator(); j.hasNext();) {
			String toolId = (String) j.next();
			// Is the tool in the wSetupPageList?
			inWSetupPageList = false;
			for (ListIterator k = wSetupPageList.listIterator(); k.hasNext();) {
				wSetupPage = (WorksiteSetupPage) k.next();
				String pageToolId = wSetupPage.getToolId();

				// use page Id + toolId for multiple News and Web Content tool
				if (pageToolId.indexOf("sakai.news") != -1
						|| pageToolId.indexOf("sakai.iframe") != -1) {
					pageToolId = wSetupPage.getPageId() + pageToolId;
				}

				if (pageToolId.equals(toolId)) {
					inWSetupPageList = true;
					// but for News and Web Content tool, need to change the
					// title
					if (toolId.indexOf("sakai.news") != -1) {
						SitePage pEdit = (SitePage) site
								.getPage(wSetupPage.pageId);
						pEdit.setTitle((String) newsTitles.get(toolId));
						List toolList = pEdit.getTools();
						for (ListIterator jTool = toolList.listIterator(); jTool
								.hasNext();) {
							ToolConfiguration tool = (ToolConfiguration) jTool
									.next();
							if (tool.getTool().getId().equals("sakai.news")) {
								// set News tool title
								tool.setTitle((String) newsTitles.get(toolId));

								// set News tool url
								String urlString = (String) newsUrls
										.get(toolId);
								try {
									URL url = new URL(urlString);
									// update the tool config
									tool.getPlacementConfig().setProperty(
											"channel-url",
											(String) url.toExternalForm());
								} catch (MalformedURLException e) {
									addAlert(state, rb.getString("java.invurl")
											+ " " + urlString + ". ");
								}
							}
						}
					} else if (toolId.indexOf("sakai.iframe") != -1) {
						SitePage pEdit = (SitePage) site
								.getPage(wSetupPage.pageId);
						pEdit.setTitle((String) wcTitles.get(toolId));

						List toolList = pEdit.getTools();
						for (ListIterator jTool = toolList.listIterator(); jTool
								.hasNext();) {
							ToolConfiguration tool = (ToolConfiguration) jTool
									.next();
							if (tool.getTool().getId().equals("sakai.iframe")) {
								// set Web Content tool title
								tool.setTitle((String) wcTitles.get(toolId));
								// set Web Content tool url
								String wcUrl = StringUtil
										.trimToNull((String) wcUrls.get(toolId));
								if (wcUrl != null
										&& !wcUrl
												.equals(WEB_CONTENT_DEFAULT_URL)) {
									// if url is not empty and not consists only
									// of "http://"
									tool.getPlacementConfig().setProperty(
											"source", wcUrl);
								}
							}
						}
					}
				}
			}
			if (inWSetupPageList) {
				// if the tool already in the list, do nothing so to save the
				// option settings
			} else {
				// if in chosen list but not in wSetupPageList, add it to the
				// site (one tool on a page)

				// if Site Info tool is being newly added
				if (toolId.equals("sakai.siteinfo")) {
					hasNewSiteInfo = true;
				}

				Tool toolRegFound = null;
				for (Iterator i = toolRegistrationList.iterator(); i.hasNext();) {
					Tool toolReg = (Tool) i.next();
					if ((toolId.indexOf("assignment") != -1 && toolId
							.equals(toolReg.getId()))
							|| (toolId.indexOf("assignment") == -1 && toolId
									.indexOf(toolReg.getId()) != -1)) {
						toolRegFound = toolReg;
					}
				}

				if (toolRegFound != null) {
					// we know such a tool, so add it
					WorksiteSetupPage addPage = new WorksiteSetupPage();
					SitePage page = site.addPage();
					addPage.pageId = page.getId();
					if (toolId.indexOf("sakai.news") != -1) {
						// set News tool title
						page.setTitle((String) newsTitles.get(toolId));
					} else if (toolId.indexOf("sakai.iframe") != -1) {
						// set Web Content tool title
						page.setTitle((String) wcTitles.get(toolId));
					} else {
						// other tools with default title
						page.setTitle(toolRegFound.getTitle());
					}
					page.setLayout(SitePage.LAYOUT_SINGLE_COL);
					ToolConfiguration tool = page.addTool();
					tool.setTool(toolRegFound.getId(), toolRegFound);
					addPage.toolId = toolId;
					wSetupPageList.add(addPage);

					// set tool title
					if (toolId.indexOf("sakai.news") != -1) {
						// set News tool title
						tool.setTitle((String) newsTitles.get(toolId));

						// set News tool url
						String urlString = (String) newsUrls.get(toolId);
						try {
							URL url = new URL(urlString);
							// update the tool config
							tool.getPlacementConfig().setProperty(
									"channel-url",
									(String) url.toExternalForm());
						} catch (MalformedURLException e) {
							// display message
							addAlert(state, "Invalid URL " + urlString + ". ");

							// remove the page because of invalid url
							site.removePage(page);
						}
					} else if (toolId.indexOf("sakai.iframe") != -1) {
						// set Web Content tool title
						tool.setTitle((String) wcTitles.get(toolId));
						// set Web Content tool url
						String wcUrl = StringUtil.trimToNull((String) wcUrls
								.get(toolId));
						if (wcUrl != null
								&& !wcUrl.equals(WEB_CONTENT_DEFAULT_URL)) {
							// if url is not empty and not consists only of
							// "http://"
							tool.getPlacementConfig().setProperty("source",
									wcUrl);
						}
					} else {
						tool.setTitle(toolRegFound.getTitle());
					}
				}
			}
		} // for

		if (homeInChosenList) {
			// Order tools - move Home to the top - first find it
			SitePage homePage = null;
			pageList = site.getPages();
			if (pageList != null && pageList.size() != 0) {
				for (ListIterator i = pageList.listIterator(); i.hasNext();) {
					SitePage page = (SitePage) i.next();
					if (rb.getString("java.home").equals(page.getTitle()))// if
					// ("Home".equals(page.getTitle()))
					{
						homePage = page;
						break;
					}
				}
			}

			// if found, move it
			if (homePage != null) {
				// move home from it's index to the first position
				int homePosition = pageList.indexOf(homePage);
				for (int n = 0; n < homePosition; n++) {
					homePage.moveUp();
				}
			}
		}

		// if Site Info is newly added, more it to the last
		if (hasNewSiteInfo) {
			SitePage siteInfoPage = null;
			pageList = site.getPages();
			String[] toolIds = { "sakai.siteinfo" };
			if (pageList != null && pageList.size() != 0) {
				for (ListIterator i = pageList.listIterator(); siteInfoPage == null
						&& i.hasNext();) {
					SitePage page = (SitePage) i.next();
					int s = page.getTools(toolIds).size();
					if (s > 0) {
						siteInfoPage = page;
					}
				}
			}

			// if found, move it
			if (siteInfoPage != null) {
				// move home from it's index to the first position
				int siteInfoPosition = pageList.indexOf(siteInfoPage);
				for (int n = siteInfoPosition; n < pageList.size(); n++) {
					siteInfoPage.moveDown();
				}
			}
		}

		// if there is no email tool chosen
		if (!hasEmail) {
			state.removeAttribute(STATE_TOOL_EMAIL_ADDRESS);
		}

		// commit
		commitSite(site);

	} // getRevisedFeatures

	/**
	 * Is the tool stealthed or hidden
	 * @param toolId
	 * @return
	 */
	private boolean notStealthOrHiddenTool(String toolId) {
		return (ToolManager.getTool(toolId) != null
		&& !ServerConfigurationService
				.getString(
						"stealthTools@org.sakaiproject.tool.api.ActiveToolManager")
				.contains(toolId)
		&& !ServerConfigurationService
				.getString(
						"hiddenTools@org.sakaiproject.tool.api.ActiveToolManager")
				.contains(toolId));
	}

	/**
	 * getFeatures gets features for a new site
	 * 
	 */
	private void getFeatures(ParameterParser params, SessionState state) {
		List idsSelected = new Vector();

		boolean goToENWPage = false;
		boolean homeSelected = false;

		// Add new pages and tools, if any
		if (params.getStrings("selectedTools") == null) {
			addAlert(state, rb.getString("atleastonetool"));
		} else {
			List l = new ArrayList(Arrays.asList(params
					.getStrings("selectedTools"))); // toolId's & titles of
			// chosen tools
			for (int i = 0; i < l.size(); i++) {
				String toolId = (String) l.get(i);

				if (toolId.equals("sakai.mailbox")
						|| toolId.indexOf("sakai.news") != -1
						|| toolId.indexOf("sakai.iframe") != -1) {
					goToENWPage = true;
				} else if (toolId.equals(HOME_TOOL_ID)) {
					homeSelected = true;
				}
				idsSelected.add(toolId);
			}
			state.setAttribute(STATE_TOOL_HOME_SELECTED, new Boolean(
					homeSelected));
		}

		String importString = params.getString("import");
		if (importString != null
				&& importString.equalsIgnoreCase(Boolean.TRUE.toString())) {
			state.setAttribute(STATE_IMPORT, Boolean.TRUE);

			List importSites = new Vector();
			if (params.getStrings("importSites") != null) {
				importSites = new ArrayList(Arrays.asList(params
						.getStrings("importSites")));
			}
			if (importSites.size() == 0) {
				addAlert(state, rb.getString("java.toimport") + " ");
			} else {
				Hashtable sites = new Hashtable();
				for (int index = 0; index < importSites.size(); index++) {
					try {
						Site s = SiteService.getSite((String) importSites
								.get(index));
						if (!sites.containsKey(s)) {
							sites.put(s, new Vector());
						}
					} catch (IdUnusedException e) {
					}
				}
				state.setAttribute(STATE_IMPORT_SITES, sites);
			}
		} else {
			state.removeAttribute(STATE_IMPORT);
		}

		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, idsSelected); // List
		// of
		// ToolRegistration
		// toolId's
		if (state.getAttribute(STATE_MESSAGE) == null) {
			if (state.getAttribute(STATE_IMPORT) != null) {
				// go to import tool page
				state.setAttribute(STATE_TEMPLATE_INDEX, "27");
			} else if (goToENWPage) {
				// go to the configuration page for Email Archive, News and Web
				// Content tools
				state.setAttribute(STATE_TEMPLATE_INDEX, "26");
			} else {
				// go to edit access page
				state.setAttribute(STATE_TEMPLATE_INDEX, "18");
			}

			int totalSteps = 4;
			if (state.getAttribute(STATE_SITE_TYPE) != null
					&& ((String) state.getAttribute(STATE_SITE_TYPE))
							.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				totalSteps = 5;
				if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null
						&& state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
					totalSteps++;
				}
			}
			if (state.getAttribute(STATE_IMPORT) != null) {
				totalSteps++;
			}
			if (goToENWPage) {
				totalSteps++;
			}
			state
					.setAttribute(SITE_CREATE_TOTAL_STEPS, new Integer(
							totalSteps));
		}
	} // getFeatures

	/**
	 * addFeatures adds features to a new site
	 * 
	 */
	private void addFeatures(SessionState state) {
		List toolRegistrationList = (Vector) state
				.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		Site site = getStateSite(state);

		List pageList = new Vector();
		int moves = 0;
		boolean hasHome = false;
		boolean hasAnnouncement = false;
		boolean hasSchedule = false;
		boolean hasChat = false;
		boolean hasDiscussion = false;
		boolean hasSiteInfo = false;
		boolean hasMessageCenter = false;

		List chosenList = (List) state
				.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);

		// tools to be imported from other sites?
		Hashtable importTools = null;
		if (state.getAttribute(STATE_IMPORT_SITE_TOOL) != null) {
			importTools = (Hashtable) state
					.getAttribute(STATE_IMPORT_SITE_TOOL);
		}

		// for tools other than home
		if (chosenList.contains(HOME_TOOL_ID)) {
			// add home tool later
			hasHome = true;
		}

		// order the id list
		chosenList = orderToolIds(state, site.getType(), chosenList);

		// titles for news tools
		Hashtable newsTitles = (Hashtable) state
				.getAttribute(STATE_NEWS_TITLES);
		// urls for news tools
		Hashtable newsUrls = (Hashtable) state.getAttribute(STATE_NEWS_URLS);
		// titles for web content tools
		Hashtable wcTitles = (Hashtable) state
				.getAttribute(STATE_WEB_CONTENT_TITLES);
		// urls for web content tools
		Hashtable wcUrls = (Hashtable) state
				.getAttribute(STATE_WEB_CONTENT_URLS);

		if (chosenList.size() > 0) {
			Tool toolRegFound = null;
			for (ListIterator i = chosenList.listIterator(); i.hasNext();) {
				String toolId = (String) i.next();

				// find the tool in the tool registration list
				toolRegFound = null;
				for (int j = 0; j < toolRegistrationList.size()
						&& toolRegFound == null; j++) {
					MyTool tool = (MyTool) toolRegistrationList.get(j);
					if ((toolId.indexOf("assignment") != -1 && toolId
							.equals(tool.getId()))
							|| (toolId.indexOf("assignment") == -1 && toolId
									.indexOf(tool.getId()) != -1)) {
						toolRegFound = ToolManager.getTool(tool.getId());
					}
				}

				if (toolRegFound != null) {
					if (toolId.indexOf("sakai.news") != -1) {
						// adding multiple news tool
						String newsTitle = (String) newsTitles.get(toolId);
						SitePage page = site.addPage();
						page.setTitle(newsTitle); // the visible label on the
						// tool menu
						page.setLayout(SitePage.LAYOUT_SINGLE_COL);
						ToolConfiguration tool = page.addTool();
						tool.setTool("sakai.news", ToolManager
								.getTool("sakai.news"));
						tool.setTitle(newsTitle);
						tool.setLayoutHints("0,0");
						String urlString = (String) newsUrls.get(toolId);
						// update the tool config
						tool.getPlacementConfig().setProperty("channel-url",
								urlString);
					} else if (toolId.indexOf("sakai.iframe") != -1) {
						// adding multiple web content tool
						String wcTitle = (String) wcTitles.get(toolId);
						SitePage page = site.addPage();
						page.setTitle(wcTitle); // the visible label on the tool
						// menu
						page.setLayout(SitePage.LAYOUT_SINGLE_COL);
						ToolConfiguration tool = page.addTool();
						tool.setTool("sakai.iframe", ToolManager
								.getTool("sakai.iframe"));
						tool.setTitle(wcTitle);
						tool.setLayoutHints("0,0");
						String wcUrl = StringUtil.trimToNull((String) wcUrls
								.get(toolId));
						if (wcUrl != null
								&& !wcUrl.equals(WEB_CONTENT_DEFAULT_URL)) {
							// if url is not empty and not consists only of
							// "http://"
							tool.getPlacementConfig().setProperty("source",
									wcUrl);
						}
					} else {
						SitePage page = site.addPage();
						page.setTitle(toolRegFound.getTitle()); // the visible
						// label on the
						// tool menu
						page.setLayout(SitePage.LAYOUT_SINGLE_COL);
						ToolConfiguration tool = page.addTool();
						tool.setTool(toolRegFound.getId(), toolRegFound);
						tool.setLayoutHints("0,0");

					} // Other features
				}

				// booleans for synoptic views
				if (toolId.equals("sakai.announcements")) {
					hasAnnouncement = true;
				} else if (toolId.equals("sakai.schedule")) {
					hasSchedule = true;
				} else if (toolId.equals("sakai.chat")) {
					hasChat = true;
				} else if (toolId.equals("sakai.discussion")) {
					hasDiscussion = true;
				} else if (toolId.equals("sakai.siteinfo")) {
					hasSiteInfo = true;
				} else if (toolId.equals("sakai.messages") || toolId.equals("sakai.forums") || toolId.equals("sakai.messagecenter")) {
					hasMessageCenter = true;
				}

			} // for

			// add home tool
			if (hasHome) {
				// Home is a special case, with several tools on the page.
				// "home" is hard coded in chef_site-addRemoveFeatures.vm.
				try {
					SitePage page = site.addPage();
					page.setTitle(rb.getString("java.home")); // the visible
					// label on the
					// tool menu
					if (hasAnnouncement || hasDiscussion || hasChat
							|| hasSchedule) {
						page.setLayout(SitePage.LAYOUT_DOUBLE_COL);
					} else {
						page.setLayout(SitePage.LAYOUT_SINGLE_COL);
					}

					// Add worksite information tool
					ToolConfiguration tool = page.addTool();
					Tool wsInfoTool = ToolManager.getTool("sakai.iframe.site");
					tool.setTool("sakai.iframe.site", wsInfoTool);
					tool.setTitle(wsInfoTool != null?wsInfoTool.getTitle():"");
					tool.setLayoutHints("0,0");

					if (hasAnnouncement) {
						// Add synoptic announcements tool
						tool = page.addTool();
						tool.setTool("sakai.synoptic.announcement", ToolManager
								.getTool("sakai.synoptic.announcement"));
						tool.setTitle(rb.getString("java.recann"));
						tool.setLayoutHints("0,1");
					}

					if (hasDiscussion) {
						// Add synoptic announcements tool
						tool = page.addTool();
						tool.setTool("sakai.synoptic.discussion", ToolManager
								.getTool("sakai.synoptic.discussion"));
						tool.setTitle("Recent Discussion Items");
						tool.setLayoutHints("1,1");
					}

					if (hasChat) {
						// Add synoptic chat tool
						tool = page.addTool();
						tool.setTool("sakai.synoptic.chat", ToolManager
								.getTool("sakai.synoptic.chat"));
						tool.setTitle("Recent Chat Messages");
						tool.setLayoutHints("2,1");
					}

					if (hasSchedule && notStealthOrHiddenTool(TOOL_ID_SUMMARY_CALENDAR)) {
						// Add synoptic schedule tool
						tool = page.addTool();
						tool.setTool(TOOL_ID_SUMMARY_CALENDAR, ToolManager
								.getTool(TOOL_ID_SUMMARY_CALENDAR));
						tool.setTitle(rb.getString("java.reccal"));
						tool.setLayoutHints("3,1");
					}

					if (hasMessageCenter) {
						// Add synoptic Message Center tool
						tool = page.addTool();
						tool.setTool("sakai.synoptic.messagecenter", ToolManager
								.getTool("sakai.synoptic.messagecenter"));
						tool.setTitle(rb.getString("java.recmsg"));
						tool.setLayoutHints("4,1");
					}

				} catch (Exception e) {
					M_log.warn("SiteAction addFeatures Exception:" + e.getMessage());
				}

				state.setAttribute(STATE_TOOL_HOME_SELECTED, Boolean.TRUE);

				// Order tools - move Home to the top
				pageList = site.getPages();
				if (pageList != null && pageList.size() != 0) {
					for (ListIterator i = pageList.listIterator(); i.hasNext();) {
						SitePage page = (SitePage) i.next();
						if ((page.getTitle()).equals(rb.getString("java.home"))) {
							moves = pageList.indexOf(page);
							for (int n = 0; n < moves; n++) {
								page.moveUp();
							}
						}
					}
				}
			} // Home feature

			// move Site Info tool, if selected, to the end of tool list
			if (hasSiteInfo) {
				SitePage siteInfoPage = null;
				pageList = site.getPages();
				String[] toolIds = { "sakai.siteinfo" };
				if (pageList != null && pageList.size() != 0) {
					for (ListIterator i = pageList.listIterator(); siteInfoPage == null
							&& i.hasNext();) {
						SitePage page = (SitePage) i.next();
						int s = page.getTools(toolIds).size();
						if (s > 0) {
							siteInfoPage = page;
						}
					}
				}

				// if found, move it
				if (siteInfoPage != null) {
					// move home from it's index to the first position
					int siteInfoPosition = pageList.indexOf(siteInfoPage);
					for (int n = siteInfoPosition; n < pageList.size(); n++) {
						siteInfoPage.moveDown();
					}
				}
			} // Site Info
		}

		// commit
		commitSite(site);

		// import
		importToolIntoSite(chosenList, importTools, site);
		
	} // addFeatures

	// import tool content into site
	private void importToolIntoSite(List toolIds, Hashtable importTools,
			Site site) {
		if (importTools != null) {
			// import resources first
			boolean resourcesImported = false;
			for (int i = 0; i < toolIds.size() && !resourcesImported; i++) {
				String toolId = (String) toolIds.get(i);

				if (toolId.equalsIgnoreCase("sakai.resources")
						&& importTools.containsKey(toolId)) {
					List importSiteIds = (List) importTools.get(toolId);

					for (int k = 0; k < importSiteIds.size(); k++) {
						String fromSiteId = (String) importSiteIds.get(k);
						String toSiteId = site.getId();

						String fromSiteCollectionId = ContentHostingService
								.getSiteCollection(fromSiteId);
						String toSiteCollectionId = ContentHostingService
								.getSiteCollection(toSiteId);
						transferCopyEntities(toolId, fromSiteCollectionId,
								toSiteCollectionId);
						resourcesImported = true;
					}
				}
			}

			// ijmport other tools then
			for (int i = 0; i < toolIds.size(); i++) {
				String toolId = (String) toolIds.get(i);
				if (!toolId.equalsIgnoreCase("sakai.resources")
						&& importTools.containsKey(toolId)) {
					List importSiteIds = (List) importTools.get(toolId);
					for (int k = 0; k < importSiteIds.size(); k++) {
						String fromSiteId = (String) importSiteIds.get(k);
						String toSiteId = site.getId();
						transferCopyEntities(toolId, fromSiteId, toSiteId);
					}
				}
			}
		}
	} // importToolIntoSite

	public void saveSiteStatus(SessionState state, boolean published) {
		Site site = getStateSite(state);
		site.setPublished(published);

	} // saveSiteStatus

	public void commitSite(Site site, boolean published) {
		site.setPublished(published);

		try {
			SiteService.save(site);
		} catch (IdUnusedException e) {
			// TODO:
		} catch (PermissionException e) {
			// TODO:
		}

	} // commitSite

	public void commitSite(Site site) {
		try {
			SiteService.save(site);
		} catch (IdUnusedException e) {
			// TODO:
		} catch (PermissionException e) {
			// TODO:
		}

	}// commitSite

	private boolean isValidDomain(String email) {
		String invalidNonOfficialAccountString = ServerConfigurationService
				.getString("invalidNonOfficialAccountString", null);

		if (invalidNonOfficialAccountString != null) {
			String[] invalidDomains = invalidNonOfficialAccountString.split(",");

			for (int i = 0; i < invalidDomains.length; i++) {
				String domain = invalidDomains[i].trim();

				if (email.toLowerCase().indexOf(domain.toLowerCase()) != -1) {
					return false;
				}
			}
		}
		return true;
	}

	private void checkAddParticipant(ParameterParser params, SessionState state) {
		// get the participants to be added
		int i;
		Vector pList = new Vector();
		HashSet existingUsers = new HashSet();

		Site site = null;
		String siteId = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);
		try {
			site = SiteService.getSite(siteId);
		} catch (IdUnusedException e) {
			addAlert(state, rb.getString("java.specif") + " " + siteId);
		}

		// accept officialAccounts and/or nonOfficialAccount account names
		String officialAccounts = "";
		String nonOfficialAccounts = "";

		// check that there is something with which to work
		officialAccounts = StringUtil.trimToNull((params
				.getString("officialAccount")));
		nonOfficialAccounts = StringUtil.trimToNull(params
				.getString("nonOfficialAccount"));
		state.setAttribute("officialAccountValue", officialAccounts);
		state.setAttribute("nonOfficialAccountValue", nonOfficialAccounts);

		// if there is no uniquname or nonOfficialAccount entered
		if (officialAccounts == null && nonOfficialAccounts == null) {
			addAlert(state, rb.getString("java.guest"));
			state.setAttribute(STATE_TEMPLATE_INDEX, "5");
			return;
		}

		String at = "@";

		if (officialAccounts != null) {
			// adding officialAccounts
			String[] officialAccountArray = officialAccounts
					.split("\r\n");

			for (i = 0; i < officialAccountArray.length; i++) {
				String officialAccount = StringUtil
						.trimToNull(officialAccountArray[i].replaceAll(
								"[\t\r\n]", ""));
				// if there is some text, try to use it
				if (officialAccount != null) {
					// automaticially add nonOfficialAccount account
					Participant participant = new Participant();
					try {
							//Changed user lookup to satisfy BSP-1010 (jholtzman)
						User u = null;
						// First try looking for the user by their email address
						Collection usersWithEmail = UserDirectoryService.findUsersByEmail(officialAccount);
						if(usersWithEmail != null) {
							if(usersWithEmail.size() == 0) {
								// If the collection is empty, we didn't find any users with this email address
								M_log.info("Unable to find users with email " + officialAccount);
							} else if (usersWithEmail.size() == 1) {
								// We found one user with this email address.  Use it.
								u = (User)usersWithEmail.iterator().next();
							} else if (usersWithEmail.size() > 1) {
								// If we have multiple users with this email address, pick one and log this error condition
								// TODO Should we not pick a user?  Throw an exception?
								M_log.warn("Found multiple user with email " + officialAccount);
								u = (User)usersWithEmail.iterator().next();
							}
						}
						// We didn't find anyone via email address, so try getting the user by EID
						if(u == null) {
							u = UserDirectoryService.getUserByEid(officialAccount);
							if (u != null)
								M_log.info("found user with eid " + officialAccount);
						}
						
						if (site != null && site.getUserRole(u.getId()) != null) {
							// user already exists in the site, cannot be added
							// again
							existingUsers.add(officialAccount);
						} else {
							participant.name = u.getDisplayName();
							participant.uniqname = u.getEid();
							pList.add(participant);
						}
					} catch (UserNotDefinedException e) {
						addAlert(state, officialAccount + " " + rb.getString("java.username") + " ");
					}
				}
			}
		} // officialAccounts

		if (nonOfficialAccounts != null) {
			String[] nonOfficialAccountArray = nonOfficialAccounts.split("\r\n");
			for (i = 0; i < nonOfficialAccountArray.length; i++) {
				String nonOfficialAccount = nonOfficialAccountArray[i];

				// if there is some text, try to use it
				nonOfficialAccount.replaceAll("[ \t\r\n]", "");

				// remove the trailing dots and empty space
				while (nonOfficialAccount.endsWith(".")
						|| nonOfficialAccount.endsWith(" ")) {
					nonOfficialAccount = nonOfficialAccount.substring(0,
							nonOfficialAccount.length() - 1);
				}

				if (nonOfficialAccount != null && nonOfficialAccount.length() > 0) {
					String[] parts = nonOfficialAccount.split(at);

					if (nonOfficialAccount.indexOf(at) == -1) {
						// must be a valid email address
						addAlert(state, nonOfficialAccount + " "
								+ rb.getString("java.emailaddress"));
					} else if ((parts.length != 2) || (parts[0].length() == 0)) {
						// must have both id and address part
						addAlert(state, nonOfficialAccount + " "
								+ rb.getString("java.notemailid"));
					} else if (!Validator.checkEmailLocal(parts[0])) {
						addAlert(state, nonOfficialAccount + " "
								+ rb.getString("java.emailaddress")
								+ rb.getString("java.theemail"));
					} else if (nonOfficialAccount != null
							&& !isValidDomain(nonOfficialAccount)) {
						// wrong string inside nonOfficialAccount id
						addAlert(state, nonOfficialAccount + " "
								+ rb.getString("java.emailaddress") + " ");
					} else {
						Participant participant = new Participant();
						try {
							// if the nonOfficialAccount user already exists
							User u = UserDirectoryService
									.getUserByEid(nonOfficialAccount);
							if (site != null
									&& site.getUserRole(u.getId()) != null) {
								// user already exists in the site, cannot be
								// added again
								existingUsers.add(nonOfficialAccount);
							} else {
								participant.name = u.getDisplayName();
								participant.uniqname = nonOfficialAccount;
								pList.add(participant);
							}
						} catch (UserNotDefinedException e) {
							// if the nonOfficialAccount user is not in the system
							// yet
							participant.name = nonOfficialAccount;
							participant.uniqname = nonOfficialAccount; // TODO:
							// what
							// would
							// the
							// UDS
							// case
							// this
							// name
							// to?
							// -ggolden
							pList.add(participant);
						}
					}
				} // if
			} // 	
		} // nonOfficialAccounts

		boolean same_role = true;
		if (params.getString("same_role") == null) {
			addAlert(state, rb.getString("java.roletype") + " ");
		} else {
			same_role = params.getString("same_role").equals("true") ? true
					: false;
			state.setAttribute("form_same_role", new Boolean(same_role));
		}

		if (state.getAttribute(STATE_MESSAGE) != null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "5");
		} else {
			if (same_role) {
				state.setAttribute(STATE_TEMPLATE_INDEX, "19");
			} else {
				state.setAttribute(STATE_TEMPLATE_INDEX, "20");
			}
		}

		// remove duplicate or existing user from participant list
		pList = removeDuplicateParticipants(pList, state);
		state.setAttribute(STATE_ADD_PARTICIPANTS, pList);

		// if the add participant list is empty after above removal, stay in the
		// current page
		if (pList.size() == 0) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "5");
		}

		// add alert for attempting to add existing site user(s)
		if (!existingUsers.isEmpty()) {
			int count = 0;
			String accounts = "";
			for (Iterator eIterator = existingUsers.iterator(); eIterator
					.hasNext();) {
				if (count == 0) {
					accounts = (String) eIterator.next();
				} else {
					accounts = accounts + ", " + (String) eIterator.next();
				}
				count++;
			}
			addAlert(state, rb.getString("add.existingpart.1") + accounts
					+ rb.getString("add.existingpart.2"));
		}

		return;

	} // checkAddParticipant

	private Vector removeDuplicateParticipants(List pList, SessionState state) {
		// check the uniqness of list member
		Set s = new HashSet();
		Set uniqnameSet = new HashSet();
		Vector rv = new Vector();
		for (int i = 0; i < pList.size(); i++) {
			Participant p = (Participant) pList.get(i);
			if (!uniqnameSet.contains(p.getUniqname())) {
				// no entry for the account yet
				rv.add(p);
				uniqnameSet.add(p.getUniqname());
			} else {
				// found duplicates
				s.add(p.getUniqname());
			}
		}

		if (!s.isEmpty()) {
			int count = 0;
			String accounts = "";
			for (Iterator i = s.iterator(); i.hasNext();) {
				if (count == 0) {
					accounts = (String) i.next();
				} else {
					accounts = accounts + ", " + (String) i.next();
				}
				count++;
			}
			if (count == 1) {
				addAlert(state, rb.getString("add.duplicatedpart.single")
						+ accounts + ".");
			} else {
				addAlert(state, rb.getString("add.duplicatedpart") + accounts
						+ ".");
			}
		}

		return rv;
	}

	public void doAdd_participant(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String siteTitle = getStateSite(state).getTitle();
		String nonOfficialAccountLabel = ServerConfigurationService.getString(
				"nonOfficialAccountLabel", "");

		Hashtable selectedRoles = (Hashtable) state
				.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES);

		boolean notify = false;
		if (state.getAttribute("form_selectedNotify") != null) {
			notify = ((Boolean) state.getAttribute("form_selectedNotify"))
					.booleanValue();
		}
		boolean same_role = ((Boolean) state.getAttribute("form_same_role"))
				.booleanValue();
		Hashtable eIdRoles = new Hashtable();

		List addParticipantList = (List) state
				.getAttribute(STATE_ADD_PARTICIPANTS);
		for (int i = 0; i < addParticipantList.size(); i++) {
			Participant p = (Participant) addParticipantList.get(i);
			String eId = p.getEid();

			// role defaults to same role
			String role = (String) state.getAttribute("form_selectedRole");
			if (!same_role) {
				// if all added participants have different role
				role = (String) selectedRoles.get(eId);
			}

			boolean officialAccount = eId.indexOf(EMAIL_CHAR) == -1;
			if (officialAccount) {
				// if this is a officialAccount
				// update the hashtable
				eIdRoles.put(eId, role);
			} else {
				// if this is an nonOfficialAccount
				try {
					UserDirectoryService.getUserByEid(eId);
				} catch (UserNotDefinedException e) {
					// if there is no such user yet, add the user
					try {
						UserEdit uEdit = UserDirectoryService
								.addUser(null, eId);

						// set email address
						uEdit.setEmail(eId);

						// set the guest user type
						uEdit.setType("guest");

						// set password to a positive random number
						Random generator = new Random(System
								.currentTimeMillis());
						Integer num = new Integer(generator
								.nextInt(Integer.MAX_VALUE));
						if (num.intValue() < 0)
							num = new Integer(num.intValue() * -1);
						String pw = num.toString();
						uEdit.setPassword(pw);

						// and save
						UserDirectoryService.commitEdit(uEdit);

						boolean notifyNewUserEmail = (ServerConfigurationService
								.getString("notifyNewUserEmail", Boolean.TRUE
										.toString()))
								.equalsIgnoreCase(Boolean.TRUE.toString());
						if (notifyNewUserEmail) {
						
								userNotificationProvider.notifyNewUserEmail(uEdit, pw, siteTitle);
							
							
						}
					} catch (UserIdInvalidException ee) {
						addAlert(state, nonOfficialAccountLabel + " id " + eId
								+ " " + rb.getString("java.isinval"));
						M_log.warn(this
								+ " UserDirectoryService addUser exception "
								+ e.getMessage());
					} catch (UserAlreadyDefinedException ee) {
						addAlert(state, "The " + nonOfficialAccountLabel + " "
								+ eId + " " + rb.getString("java.beenused"));
						M_log.warn(this
								+ " UserDirectoryService addUser exception "
								+ e.getMessage());
					} catch (UserPermissionException ee) {
						addAlert(state, rb.getString("java.haveadd") + " "
								+ eId);
						M_log.warn(this
								+ " UserDirectoryService addUser exception "
								+ e.getMessage());
					}
				}

				if (state.getAttribute(STATE_MESSAGE) == null) {
					eIdRoles.put(eId, role);
				}
			}
		}

		// batch add and updates the successful added list
		List addedParticipantEIds = addUsersRealm(state, eIdRoles, notify,
				false);

		// update the not added user list
		String notAddedOfficialAccounts = null;
		String notAddedNonOfficialAccounts = null;
		for (Iterator iEIds = eIdRoles.keySet().iterator(); iEIds.hasNext();) {
			String iEId = (String) iEIds.next();
			if (!addedParticipantEIds.contains(iEId)) {
				if (iEId.indexOf(EMAIL_CHAR) == -1) {
					// no email in eid
					notAddedOfficialAccounts = notAddedOfficialAccounts
							.concat(iEId + "\n");
				} else {
					// email in eid
					notAddedNonOfficialAccounts = notAddedNonOfficialAccounts
							.concat(iEId + "\n");
				}
			}
		}

		if (addedParticipantEIds.size() != 0
				&& (notAddedOfficialAccounts != null || notAddedNonOfficialAccounts != null)) {
			// at lease one officialAccount account or an nonOfficialAccount
			// account added, and there are also failures
			addAlert(state, rb.getString("java.allusers"));
		}

		if (notAddedOfficialAccounts == null
				&& notAddedNonOfficialAccounts == null) {
			// all account has been added successfully
			removeAddParticipantContext(state);
		} else {
			state.setAttribute("officialAccountValue",
					notAddedOfficialAccounts);
			state.setAttribute("nonOfficialAccountValue",
					notAddedNonOfficialAccounts);
		}
		if (state.getAttribute(STATE_MESSAGE) != null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "22");
		} else {
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		}
		return;

	} // doAdd_participant

	/**
	 * remove related state variable for adding participants
	 * 
	 * @param state
	 *            SessionState object
	 */
	private void removeAddParticipantContext(SessionState state) {
		// remove related state variables
		state.removeAttribute("form_selectedRole");
		state.removeAttribute("officialAccountValue");
		state.removeAttribute("nonOfficialAccountValue");
		state.removeAttribute("form_same_role");
		state.removeAttribute("form_selectedNotify");
		state.removeAttribute(STATE_ADD_PARTICIPANTS);
		state.removeAttribute(STATE_SELECTED_USER_LIST);
		state.removeAttribute(STATE_SELECTED_PARTICIPANT_ROLES);

	} // removeAddParticipantContext




	private String getSetupRequestEmailAddress() {
		String from = ServerConfigurationService.getString("setup.request",
				null);
		if (from == null) {
			from = "postmaster@".concat(ServerConfigurationService
					.getServerName());
			M_log.warn(this + " - no 'setup.request' in configuration, using: "+ from);
		}
		return from;
	}



	/*
	 * Given a list of user eids, add users to realm If the user account does
	 * not exist yet inside the user directory, assign role to it @return A list
	 * of eids for successfully added users
	 */
	private List addUsersRealm(SessionState state, Hashtable eIdRoles,
			boolean notify, boolean nonOfficialAccount) {
		// return the list of user eids for successfully added user
		List addedUserEIds = new Vector();

		StringBuilder message = new StringBuilder();

		if (eIdRoles != null && !eIdRoles.isEmpty()) {
			// get the current site
			Site sEdit = getStateSite(state);
			if (sEdit != null) {
				// get realm object
				String realmId = sEdit.getReference();
				try {
					AuthzGroup realmEdit = AuthzGroupService
							.getAuthzGroup(realmId);
					for (Iterator eIds = eIdRoles.keySet().iterator(); eIds
							.hasNext();) {
						String eId = (String) eIds.next();
						String role = (String) eIdRoles.get(eId);

						try {
							User user = UserDirectoryService.getUserByEid(eId);
							if (AuthzGroupService.allowUpdate(realmId)
									|| SiteService
											.allowUpdateSiteMembership(sEdit
													.getId())) {
								realmEdit.addMember(user.getId(), role, true,
										false);
								addedUserEIds.add(eId);

								// send notification
								if (notify) {
									String emailId = user.getEmail();
									String userName = user.getDisplayName();
									// send notification email
									if (this.userNotificationProvider == null)
										M_log.warn("notification provider is null!");
									userNotificationProvider.notifyAddedParticipant(nonOfficialAccount, user, sEdit.getTitle());
									
								}
							}
						} catch (UserNotDefinedException e) {
							message.append(eId + " "
									+ rb.getString("java.account") + " \n");
						} // try
					} // for

					try {
						AuthzGroupService.save(realmEdit);
					} catch (GroupNotDefinedException ee) {
						message.append(rb.getString("java.realm") + realmId);
					} catch (AuthzPermissionException ee) {
						message.append(rb.getString("java.permeditsite")
								+ realmId);
					}
				} catch (GroupNotDefinedException eee) {
					message.append(rb.getString("java.realm") + realmId);
				} catch (Exception eee) {
					M_log.warn("SiteActionaddUsersRealm " + eee.getMessage() + " realmId=" + realmId);
				}
			}
		}

		if (message.length() != 0) {
			addAlert(state, message.toString());
		} // if

		return addedUserEIds;

	} // addUsersRealm

	/**
	 * addNewSite is called when the site has enough information to create a new
	 * site
	 * 
	 */
	private void addNewSite(ParameterParser params, SessionState state) {
		if (getStateSite(state) != null) {
			// There is a Site in state already, so use it rather than creating
			// a new Site
			return;
		}

		// If cleanState() has removed SiteInfo, get a new instance into state
		SiteInfo siteInfo = new SiteInfo();
		if (state.getAttribute(STATE_SITE_INFO) != null) {
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
		}
		String id = StringUtil.trimToNull(siteInfo.getSiteId());
		if (id == null) {
			// get id
			id = IdManager.createUuid();
			siteInfo.site_id = id;
		}
		state.setAttribute(STATE_SITE_INFO, siteInfo);
		if (state.getAttribute(STATE_MESSAGE) == null) {
			try {
				Site site = SiteService.addSite(id, siteInfo.site_type);

				String title = StringUtil.trimToNull(siteInfo.title);
				String description = siteInfo.description;
				setAppearance(state, site, siteInfo.iconUrl);
				site.setDescription(description);
				if (title != null) {
					site.setTitle(title);
				}

				site.setType(siteInfo.site_type);

				ResourcePropertiesEdit rp = site.getPropertiesEdit();
				site.setShortDescription(siteInfo.short_description);
				site.setPubView(siteInfo.include);
				site.setJoinable(siteInfo.joinable);
				site.setJoinerRole(siteInfo.joinerRole);
				site.setPublished(siteInfo.published);
				// site contact information
				rp.addProperty(PROP_SITE_CONTACT_NAME,
						siteInfo.site_contact_name);
				rp.addProperty(PROP_SITE_CONTACT_EMAIL,
						siteInfo.site_contact_email);

				state.setAttribute(STATE_SITE_INSTANCE_ID, site.getId());

				// commit newly added site in order to enable related realm
				commitSite(site);

			} catch (IdUsedException e) {
				addAlert(state, rb.getString("java.sitewithid") + " " + id
						+ " " + rb.getString("java.exists"));
				state.setAttribute(STATE_TEMPLATE_INDEX, params
						.getString("template-index"));
				return;
			} catch (IdInvalidException e) {
				addAlert(state, rb.getString("java.thesiteid") + " " + id + " "
						+ rb.getString("java.notvalid"));
				state.setAttribute(STATE_TEMPLATE_INDEX, params
						.getString("template-index"));
				return;
			} catch (PermissionException e) {
				addAlert(state, rb.getString("java.permission") + " " + id
						+ ".");
				state.setAttribute(STATE_TEMPLATE_INDEX, params
						.getString("template-index"));
				return;
			}
		}
	} // addNewSite

	/**
	 * %%% legacy properties, to be cleaned up
	 * 
	 */
	private void sitePropertiesIntoState(SessionState state) {
		try {
			Site site = getStateSite(state);
			SiteInfo siteInfo = new SiteInfo();
			if (site != null)
			{
				// set from site attributes
				siteInfo.title = site.getTitle();
				siteInfo.description = site.getDescription();
				siteInfo.iconUrl = site.getIconUrl();
				siteInfo.infoUrl = site.getInfoUrl();
				siteInfo.joinable = site.isJoinable();
				siteInfo.joinerRole = site.getJoinerRole();
				siteInfo.published = site.isPublished();
				siteInfo.include = site.isPubView();
				siteInfo.short_description = site.getShortDescription();
			}
			siteInfo.additional = "";
			state.setAttribute(STATE_SITE_TYPE, siteInfo.site_type);
			state.setAttribute(STATE_SITE_INFO, siteInfo);
		} catch (Exception e) {
			M_log.warn("SiteAction.sitePropertiesIntoState " + e.getMessage());
		}

	} // sitePropertiesIntoState

	/**
	 * pageMatchesPattern returns true if a SitePage matches a WorkSite Setup
	 * pattern
	 * 
	 */
	private boolean pageMatchesPattern(SessionState state, SitePage page) {
		List pageToolList = page.getTools();

		// if no tools on the page, return false
		if (pageToolList == null || pageToolList.size() == 0) {
			return false;
		}

		// for the case where the page has one tool
		ToolConfiguration toolConfiguration = (ToolConfiguration) pageToolList
				.get(0);

		// don't compare tool properties, which may be changed using Options
		List toolList = new Vector();
		int count = pageToolList.size();
		boolean match = false;

		// check Worksite Setup Home pattern
		if (page.getTitle() != null
				&& page.getTitle().equals(rb.getString("java.home"))) {
			return true;

		} // Home
		else if (page.getTitle() != null
				&& page.getTitle().equals(rb.getString("java.help"))) {
			// if the count of tools on the page doesn't match, return false
			if (count != 1) {
				return false;
			}

			// if the page layout doesn't match, return false
			if (page.getLayout() != SitePage.LAYOUT_SINGLE_COL) {
				return false;
			}

			// if tooId isn't sakai.contactSupport, return false
			if (!(toolConfiguration.getTool().getId())
					.equals("sakai.contactSupport")) {
				return false;
			}

			return true;
		} // Help
		else if (page.getTitle() != null && page.getTitle().equals("Chat")) {
			// if the count of tools on the page doesn't match, return false
			if (count != 1) {
				return false;
			}

			// if the page layout doesn't match, return false
			if (page.getLayout() != SitePage.LAYOUT_SINGLE_COL) {
				return false;
			}

			// if the tool doesn't match, return false
			if (!(toolConfiguration.getTool().getId()).equals("sakai.chat")) {
				return false;
			}

			// if the channel doesn't match value for main channel, return false
			String channel = toolConfiguration.getPlacementConfig()
					.getProperty("channel");
			if (channel == null) {
				return false;
			}
			if (!(channel.equals(NULL_STRING))) {
				return false;
			}

			return true;
		} // Chat
		else {
			// if the count of tools on the page doesn't match, return false
			if (count != 1) {
				return false;
			}

			// if the page layout doesn't match, return false
			if (page.getLayout() != SitePage.LAYOUT_SINGLE_COL) {
				return false;
			}

			toolList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);

			if (pageToolList != null || pageToolList.size() != 0) {
				// if tool attributes don't match, return false
				match = false;
				for (ListIterator i = toolList.listIterator(); i.hasNext();) {
					MyTool tool = (MyTool) i.next();
					if (toolConfiguration.getTitle() != null) {
						if (toolConfiguration.getTool() != null
								&& toolConfiguration.getTool().getId().indexOf(
										tool.getId()) != -1) {
							match = true;
						}
					}
				}
				if (!match) {
					return false;
				}
			}
		} // Others
		return true;

	} // pageMatchesPattern

	/**
	 * siteToolsIntoState is the replacement for siteToolsIntoState_ Make a list
	 * of pages and tools that match WorkSite Setup configurations into state
	 */
	private void siteToolsIntoState(SessionState state) {
		String wSetupTool = NULL_STRING;
		List wSetupPageList = new Vector();
		Site site = getStateSite(state);
		List pageList = site.getPages();

		// Put up tool lists filtered by category
		String type = site.getType();
		if (type == null) {
			if (SiteService.isUserSite(site.getId())) {
				type = "myworkspace";
			} else if (state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null) {
				// for those sites without type, use the tool set for default
				// site type
				type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);
			}
		}

		List toolRegList = new Vector();
		if (type != null) {
			Set categories = new HashSet();
			categories.add(type);
			Set toolRegistrations = ToolManager.findTools(categories, null);
			SortedIterator i = new SortedIterator(toolRegistrations.iterator(),
					new ToolComparator());
			for (; i.hasNext();) {
				// form a new Tool
				Tool tr = (Tool) i.next();
				MyTool newTool = new MyTool();
				newTool.title = tr.getTitle();
				newTool.id = tr.getId();
				newTool.description = tr.getDescription();

				toolRegList.add(newTool);
			}
		}

		if (toolRegList.size() == 0
				&& state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null) {
			// use default site type and try getting tools again
			type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);

			Set nCategories = new HashSet();
			nCategories.add(type);
			Set toolRegistrations = ToolManager.findTools(nCategories, null);
			SortedIterator i = new SortedIterator(toolRegistrations.iterator(),
					new ToolComparator());
			for (; i.hasNext();) {
				// form a new Tool
				Tool tr = (Tool) i.next();
				MyTool newTool = new MyTool();
				newTool.title = tr.getTitle();
				newTool.id = tr.getId();
				newTool.description = tr.getDescription();

				toolRegList.add(newTool);
			}
		}
		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, toolRegList);
		state.setAttribute(STATE_SITE_TYPE, type);

		if (type == null) {
			M_log.warn(this + ": - unknown STATE_SITE_TYPE");
		} else {
			state.setAttribute(STATE_SITE_TYPE, type);
		}

		boolean check_home = false;
		boolean hasNews = false;
		boolean hasWebContent = false;
		int newsToolNum = 0;
		int wcToolNum = 0;
		Hashtable newsTitles = new Hashtable();
		Hashtable wcTitles = new Hashtable();
		Hashtable newsUrls = new Hashtable();
		Hashtable wcUrls = new Hashtable();

		Vector idSelected = new Vector();

		if (!((pageList == null) || (pageList.size() == 0))) {
			for (ListIterator i = pageList.listIterator(); i.hasNext();) {
				SitePage page = (SitePage) i.next();
				// collect the pages consistent with Worksite Setup patterns
				if (pageMatchesPattern(state, page)) {
					if (page.getTitle().equals(rb.getString("java.home"))) {
						wSetupTool = HOME_TOOL_ID;
						check_home = true;
					} else {
						List pageToolList = page.getTools();
						wSetupTool = ((ToolConfiguration) pageToolList.get(0))
								.getTool().getId();
						if (wSetupTool.indexOf("sakai.news") != -1) {
							String newsToolId = page.getId() + wSetupTool;
							idSelected.add(newsToolId);
							newsTitles.put(newsToolId, page.getTitle());
							String channelUrl = ((ToolConfiguration) pageToolList
									.get(0)).getPlacementConfig().getProperty(
									"channel-url");
							newsUrls.put(newsToolId,
									channelUrl != null ? channelUrl : "");
							newsToolNum++;

							// insert the News tool into the list
							hasNews = false;
							int j = 0;
							MyTool newTool = new MyTool();
							newTool.title = NEWS_DEFAULT_TITLE;
							newTool.id = newsToolId;
							newTool.selected = false;

							for (; j < toolRegList.size() && !hasNews; j++) {
								MyTool t = (MyTool) toolRegList.get(j);
								if (t.getId().equals("sakai.news")) {
									hasNews = true;
									newTool.description = t.getDescription();
								}
							}

							if (hasNews) {
								toolRegList.add(j - 1, newTool);
							} else {
								toolRegList.add(newTool);
							}

						} else if ((wSetupTool).indexOf("sakai.iframe") != -1) {
							String wcToolId = page.getId() + wSetupTool;
							idSelected.add(wcToolId);
							wcTitles.put(wcToolId, page.getTitle());
							String wcUrl = StringUtil
									.trimToNull(((ToolConfiguration) pageToolList
											.get(0)).getPlacementConfig()
											.getProperty("source"));
							if (wcUrl == null) {
								// if there is no source URL, seed it with the
								// Web Content default URL
								wcUrl = WEB_CONTENT_DEFAULT_URL;
							}
							wcUrls.put(wcToolId, wcUrl);
							wcToolNum++;

							MyTool newTool = new MyTool();
							newTool.title = WEB_CONTENT_DEFAULT_TITLE;
							newTool.id = wcToolId;
							newTool.selected = false;

							hasWebContent = false;
							int j = 0;
							for (; j < toolRegList.size() && !hasWebContent; j++) {
								MyTool t = (MyTool) toolRegList.get(j);
								if (t.getId().equals("sakai.iframe")) {
									hasWebContent = true;
									newTool.description = t.getDescription();
								}
							}
							if (hasWebContent) {
								toolRegList.add(j - 1, newTool);
							} else {
								toolRegList.add(newTool);
							}
						}
						/*
						 * else if(wSetupTool.indexOf("sakai.syllabus") != -1) {
						 * //add only one instance of tool per site if
						 * (!(idSelected.contains(wSetupTool))) {
						 * idSelected.add(wSetupTool); } }
						 */
						else {
							idSelected.add(wSetupTool);
						}
					}

					WorksiteSetupPage wSetupPage = new WorksiteSetupPage();
					wSetupPage.pageId = page.getId();
					wSetupPage.pageTitle = page.getTitle();
					wSetupPage.toolId = wSetupTool;
					wSetupPageList.add(wSetupPage);
				}
			}
		}

		newsTitles.put("sakai.news", NEWS_DEFAULT_TITLE);
		newsUrls.put("sakai.news", NEWS_DEFAULT_URL);
		wcTitles.put("sakai.iframe", WEB_CONTENT_DEFAULT_TITLE);
		wcUrls.put("sakai.iframe", WEB_CONTENT_DEFAULT_URL);

		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, toolRegList);
		state.setAttribute(STATE_TOOL_HOME_SELECTED, new Boolean(check_home));
		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, idSelected); // List
		// of
		// ToolRegistration
		// toolId's
		state.setAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST,
				idSelected); // List of ToolRegistration toolId's
		state.setAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME, Boolean
				.valueOf(check_home));
		state.setAttribute(STATE_NEWS_TITLES, newsTitles);
		state.setAttribute(STATE_WEB_CONTENT_TITLES, wcTitles);
		state.setAttribute(STATE_NEWS_URLS, newsUrls);
		state.setAttribute(STATE_WEB_CONTENT_URLS, wcUrls);
		state.setAttribute(STATE_WORKSITE_SETUP_PAGE_LIST, wSetupPageList);

	} // siteToolsIntoState

	/**
	 * reset the state variables used in edit tools mode
	 * 
	 * @state The SessionState object
	 */
	private void removeEditToolState(SessionState state) {
		state.removeAttribute(STATE_TOOL_HOME_SELECTED);
		state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST); // List
		// of
		// ToolRegistration
		// toolId's
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST); // List
		// of
		// ToolRegistration
		// toolId's
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME);
		state.removeAttribute(STATE_NEWS_TITLES);
		state.removeAttribute(STATE_NEWS_URLS);
		state.removeAttribute(STATE_WEB_CONTENT_TITLES);
		state.removeAttribute(STATE_WEB_CONTENT_URLS);
		state.removeAttribute(STATE_WORKSITE_SETUP_PAGE_LIST);
	}

	private List orderToolIds(SessionState state, String type, List toolIdList) {
		List rv = new Vector();
		if (state.getAttribute(STATE_TOOL_HOME_SELECTED) != null
				&& ((Boolean) state.getAttribute(STATE_TOOL_HOME_SELECTED))
						.booleanValue()) {
			rv.add(HOME_TOOL_ID);
		}

		// look for null site type
		if (type != null && toolIdList != null) {
			Set categories = new HashSet();
			categories.add(type);
			Set tools = ToolManager.findTools(categories, null);
			SortedIterator i = new SortedIterator(tools.iterator(),
					new ToolComparator());
			for (; i.hasNext();) {
				String tool_id = ((Tool) i.next()).getId();
				for (ListIterator j = toolIdList.listIterator(); j.hasNext();) {
					String toolId = (String) j.next();
					if (toolId.indexOf("assignment") != -1
							&& toolId.equals(tool_id)
							|| toolId.indexOf("assignment") == -1
							&& toolId.indexOf(tool_id) != -1) {
						rv.add(toolId);
					}
				}
			}
		}
		return rv;

	} // orderToolIds

	private void setupFormNamesAndConstants(SessionState state) {
		TimeBreakdown timeBreakdown = (TimeService.newTime()).breakdownLocal();
		String mycopyright = COPYRIGHT_SYMBOL + " " + timeBreakdown.getYear()
				+ ", " + UserDirectoryService.getCurrentUser().getDisplayName()
				+ ". All Rights Reserved. ";
		state.setAttribute(STATE_MY_COPYRIGHT, mycopyright);
		state.setAttribute(STATE_SITE_INSTANCE_ID, null);
		state.setAttribute(STATE_INITIALIZED, Boolean.TRUE.toString());
		SiteInfo siteInfo = new SiteInfo();
		Participant participant = new Participant();
		participant.name = NULL_STRING;
		participant.uniqname = NULL_STRING;
		state.setAttribute(STATE_SITE_INFO, siteInfo);
		state.setAttribute("form_participantToAdd", participant);
		state.setAttribute(FORM_ADDITIONAL, NULL_STRING);
		// legacy
		state.setAttribute(FORM_HONORIFIC, "0");
		state.setAttribute(FORM_REUSE, "0");
		state.setAttribute(FORM_RELATED_CLASS, "0");
		state.setAttribute(FORM_RELATED_PROJECT, "0");
		state.setAttribute(FORM_INSTITUTION, "0");
		// sundry form variables
		state.setAttribute(FORM_PHONE, "");
		state.setAttribute(FORM_EMAIL, "");
		state.setAttribute(FORM_SUBJECT, "");
		state.setAttribute(FORM_DESCRIPTION, "");
		state.setAttribute(FORM_TITLE, "");
		state.setAttribute(FORM_NAME, "");
		state.setAttribute(FORM_SHORT_DESCRIPTION, "");

	} // setupFormNamesAndConstants

	/**
	 * setupSkins
	 * 
	 */
	private void setupIcons(SessionState state) {
		List icons = new Vector();

		String[] iconNames = null;
		String[] iconUrls = null;
		String[] iconSkins = null;

		// get icon information
		if (ServerConfigurationService.getStrings("iconNames") != null) {
			iconNames = ServerConfigurationService.getStrings("iconNames");
		}
		if (ServerConfigurationService.getStrings("iconUrls") != null) {
			iconUrls = ServerConfigurationService.getStrings("iconUrls");
		}
		if (ServerConfigurationService.getStrings("iconSkins") != null) {
			iconSkins = ServerConfigurationService.getStrings("iconSkins");
		}

		if ((iconNames != null) && (iconUrls != null) && (iconSkins != null)
				&& (iconNames.length == iconUrls.length)
				&& (iconNames.length == iconSkins.length)) {
			for (int i = 0; i < iconNames.length; i++) {
				MyIcon s = new MyIcon(StringUtil.trimToNull((String) iconNames[i]),
						StringUtil.trimToNull((String) iconUrls[i]), StringUtil
								.trimToNull((String) iconSkins[i]));
				icons.add(s);
			}
		}

		state.setAttribute(STATE_ICONS, icons);
	}

	private void setAppearance(SessionState state, Site edit, String iconUrl) {
		// set the icon
		edit.setIconUrl(iconUrl);
		if (iconUrl == null) {
			// this is the default case - no icon, no (default) skin
			edit.setSkin(null);
			return;
		}

		// if this icon is in the config appearance list, find a skin to set
		List icons = (List) state.getAttribute(STATE_ICONS);
		for (Iterator i = icons.iterator(); i.hasNext();) {
			Object icon = (Object) i.next();
			if (icon instanceof MyIcon && !StringUtil.different(((MyIcon) icon).getUrl(), iconUrl)) {
				edit.setSkin(((MyIcon) icon).getSkin());
				return;
			}
		}
	}

	/**
	 * A dispatch funtion when selecting course features
	 */
	public void doAdd_features(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String option = params.getString("option");

		if (option.equalsIgnoreCase("addNews")) {
			updateSelectedToolList(state, params, false);
			insertTool(state, "sakai.news", STATE_NEWS_TITLES,
					NEWS_DEFAULT_TITLE, STATE_NEWS_URLS, NEWS_DEFAULT_URL,
					Integer.parseInt(params.getString("newsNum")));
			state.setAttribute(STATE_TEMPLATE_INDEX, "26");
		} else if (option.equalsIgnoreCase("addWC")) {
			updateSelectedToolList(state, params, false);
			insertTool(state, "sakai.iframe", STATE_WEB_CONTENT_TITLES,
					WEB_CONTENT_DEFAULT_TITLE, STATE_WEB_CONTENT_URLS,
					WEB_CONTENT_DEFAULT_URL, Integer.parseInt(params
							.getString("wcNum")));
			state.setAttribute(STATE_TEMPLATE_INDEX, "26");
		} else if (option.equalsIgnoreCase("import")) {
			// import or not
			updateSelectedToolList(state, params, false);
			String importSites = params.getString("import");
			if (importSites != null
					&& importSites.equalsIgnoreCase(Boolean.TRUE.toString())) {
				state.setAttribute(STATE_IMPORT, Boolean.TRUE);
				if (importSites.equalsIgnoreCase(Boolean.TRUE.toString())) {
					state.removeAttribute(STATE_IMPORT);
					state.removeAttribute(STATE_IMPORT_SITES);
					state.removeAttribute(STATE_IMPORT_SITE_TOOL);
				}
			} else {
				state.removeAttribute(STATE_IMPORT);
			}
		} else if (option.equalsIgnoreCase("continue")) {
			// continue
			doContinue(data);
		} else if (option.equalsIgnoreCase("back")) {
			// back
			doBack(data);
		} else if (option.equalsIgnoreCase("cancel")) {
			// cancel
			doCancel_create(data);
		}

	} // doAdd_features

	/**
	 * update the selected tool list
	 * 
	 * @param params
	 *            The ParameterParser object
	 * @param verifyData
	 *            Need to verify input data or not
	 */
	private void updateSelectedToolList(SessionState state,
			ParameterParser params, boolean verifyData) {
		List selectedTools = new ArrayList(Arrays.asList(params
				.getStrings("selectedTools")));

		Hashtable titles = (Hashtable) state.getAttribute(STATE_NEWS_TITLES);
		Hashtable urls = (Hashtable) state.getAttribute(STATE_NEWS_URLS);
		Hashtable wcTitles = (Hashtable) state
				.getAttribute(STATE_WEB_CONTENT_TITLES);
		Hashtable wcUrls = (Hashtable) state
				.getAttribute(STATE_WEB_CONTENT_URLS);
		boolean has_home = false;
		String emailId = null;

		for (int i = 0; i < selectedTools.size(); i++) {
			String id = (String) selectedTools.get(i);
			if (id.indexOf("sakai.news") != -1) {
				String title = StringUtil.trimToNull(params
						.getString("titlefor" + id));
				if (title == null) {
					// if there is no input, make the title for news tool
					// default to NEWS_DEFAULT_TITLE
					title = NEWS_DEFAULT_TITLE;
				}
				titles.put(id, title);

				String url = StringUtil.trimToNull(params.getString("urlfor"
						+ id));
				if (url == null) {
					// if there is no input, make the title for news tool
					// default to NEWS_DEFAULT_URL
					url = NEWS_DEFAULT_URL;
				}
				urls.put(id, url);

				try {
					new URL(url);
				} catch (MalformedURLException e) {
					addAlert(state, rb.getString("java.invurl") + " " + url
							+ ". ");
				}
			} else if (id.indexOf("sakai.iframe") != -1) {
				String wcTitle = StringUtil.trimToNull(params
						.getString("titlefor" + id));
				if (wcTitle == null) {
					// if there is no input, make the title for Web Content tool
					// default to WEB_CONTENT_DEFAULT_TITLE
					wcTitle = WEB_CONTENT_DEFAULT_TITLE;
				}
				wcTitles.put(id, wcTitle);

				String wcUrl = StringUtil.trimToNull(params.getString("urlfor"
						+ id));
				if (wcUrl == null) {
					// if there is no input, make the title for Web Content tool
					// default to WEB_CONTENT_DEFAULT_URL
					wcUrl = WEB_CONTENT_DEFAULT_URL;
				} else {
					if ((wcUrl.length() > 0) && (!wcUrl.startsWith("/"))
							&& (wcUrl.indexOf("://") == -1)) {
						wcUrl = "http://" + wcUrl;
					}
				}
				wcUrls.put(id, wcUrl);
			} else if (id.equalsIgnoreCase(HOME_TOOL_ID)) {
				has_home = true;
			} else if (id.equalsIgnoreCase("sakai.mailbox")) {
				// if Email archive tool is selected, check the email alias
				emailId = StringUtil.trimToNull(params.getString("emailId"));
				if (verifyData) {
					if (emailId == null) {
						addAlert(state, rb.getString("java.emailarchive") + " ");
					} else {
						if (!Validator.checkEmailLocal(emailId)) {
							addAlert(state, rb.getString("java.theemail"));
						} else {
							// check to see whether the alias has been used by
							// other sites
							try {
								String target = AliasService.getTarget(emailId);
								if (target != null) {
									if (state
											.getAttribute(STATE_SITE_INSTANCE_ID) != null) {
										String siteId = (String) state
												.getAttribute(STATE_SITE_INSTANCE_ID);
										String channelReference = mailArchiveChannelReference(siteId);
										if (!target.equals(channelReference)) {
											// the email alias is not used by
											// current site
											addAlert(state, rb.getString("java.emailinuse") + " ");
										}
									} else {
										addAlert(state, rb.getString("java.emailinuse") + " ");
									}
								}
							} catch (IdUnusedException ee) {
							}
						}
					}
				}
			}
		}
		state.setAttribute(STATE_TOOL_HOME_SELECTED, new Boolean(has_home));
		state.setAttribute(STATE_TOOL_EMAIL_ADDRESS, emailId);
		state.setAttribute(STATE_NEWS_TITLES, titles);
		state.setAttribute(STATE_NEWS_URLS, urls);
		state.setAttribute(STATE_WEB_CONTENT_TITLES, wcTitles);
		state.setAttribute(STATE_WEB_CONTENT_URLS, wcUrls);
	} // updateSelectedToolList

	/**
	 * find the tool in the tool list and insert another tool instance to the
	 * list
	 * 
	 * @param state
	 *            SessionState object
	 * @param toolId
	 *            The id for the inserted tool
	 * @param stateTitlesVariable
	 *            The titles
	 * @param defaultTitle
	 *            The default title for the inserted tool
	 * @param stateUrlsVariable
	 *            The urls
	 * @param defaultUrl
	 *            The default url for the inserted tool
	 * @param insertTimes
	 *            How many tools need to be inserted
	 */
	private void insertTool(SessionState state, String toolId,
			String stateTitlesVariable, String defaultTitle,
			String stateUrlsVariable, String defaultUrl, int insertTimes) {
		// the list of available tools
		List toolList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		int toolListedTimes = 0;
		int index = 0;
		int insertIndex = 0;
		while (index < toolList.size()) {
			MyTool tListed = (MyTool) toolList.get(index);
			if (tListed.getId().indexOf(toolId) != -1) {
				toolListedTimes++;
			}

			if (toolListedTimes > 0 && insertIndex == 0) {
				// update the insert index
				insertIndex = index;
			}

			index++;
		}

		List toolSelected = (List) state
				.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);

		// the titles
		Hashtable titles = (Hashtable) state.getAttribute(stateTitlesVariable);
		if (titles == null) {
			titles = new Hashtable();
		}

		// the urls
		Hashtable urls = (Hashtable) state.getAttribute(stateUrlsVariable);
		if (urls == null) {
			urls = new Hashtable();
		}

		// insert multiple tools
		for (int i = 0; i < insertTimes; i++) {
			toolListedTimes = toolListedTimes + i;

			toolSelected.add(toolId + toolListedTimes);

			// We need to insert a specific tool entry only if all the specific
			// tool entries have been selected
			MyTool newTool = new MyTool();
			newTool.title = defaultTitle;
			newTool.id = toolId + toolListedTimes;
			toolList.add(insertIndex, newTool);
			titles.put(newTool.id, defaultTitle);
			urls.put(newTool.id, defaultUrl);
		}

		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, toolList);
		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, toolSelected);
		state.setAttribute(stateTitlesVariable, titles);
		state.setAttribute(stateUrlsVariable, urls);

	} // insertTool

	/**
	 * 
	 * set selected participant role Hashtable
	 */
	private void setSelectedParticipantRoles(SessionState state) {
		List selectedUserIds = (List) state
				.getAttribute(STATE_SELECTED_USER_LIST);
		List participantList = collectionToList((Collection) state.getAttribute(STATE_PARTICIPANT_LIST));
		List selectedParticipantList = new Vector();

		Hashtable selectedParticipantRoles = new Hashtable();

		if (!selectedUserIds.isEmpty() && participantList != null) {
			for (int i = 0; i < participantList.size(); i++) {
				String id = "";
				Object o = (Object) participantList.get(i);
				if (o.getClass().equals(Participant.class)) {
					// get participant roles
					id = ((Participant) o).getUniqname();
					selectedParticipantRoles.put(id, ((Participant) o)
							.getRole());
				} else if (o.getClass().equals(Student.class)) {
					// get participant from roster role
					id = ((Student) o).getUniqname();
					selectedParticipantRoles.put(id, ((Student) o).getRole());
				}
				if (selectedUserIds.contains(id)) {
					selectedParticipantList.add(participantList.get(i));
				}
			}
		}
		state.setAttribute(STATE_SELECTED_PARTICIPANT_ROLES,
				selectedParticipantRoles);
		state
				.setAttribute(STATE_SELECTED_PARTICIPANTS,
						selectedParticipantList);

	} // setSelectedParticipantRol3es

	/**
	 * the SiteComparator class
	 */
	private class SiteComparator implements Comparator {
		
		Collator collator = Collator.getInstance();
		
		/**
		 * the criteria
		 */
		String m_criterion = null;

		String m_asc = null;

		/**
		 * constructor
		 * 
		 * @param criteria
		 *            The sort criteria string
		 * @param asc
		 *            The sort order string. TRUE_STRING if ascending; "false"
		 *            otherwise.
		 */
		public SiteComparator(String criterion, String asc) {
			m_criterion = criterion;
			m_asc = asc;

		} // constructor

		/**
		 * implementing the Comparator compare function
		 * 
		 * @param o1
		 *            The first object
		 * @param o2
		 *            The second object
		 * @return The compare result. 1 is o1 < o2; -1 otherwise
		 */
		public int compare(Object o1, Object o2) {
			int result = -1;

			if (m_criterion == null)
				m_criterion = SORTED_BY_TITLE;

			/** *********** for sorting site list ****************** */
			if (m_criterion.equals(SORTED_BY_TITLE)) {
				// sorted by the worksite title
				String s1 = ((Site) o1).getTitle();
				String s2 = ((Site) o2).getTitle();
				result = compareString(s1, s2);
			} else if (m_criterion.equals(SORTED_BY_DESCRIPTION)) {

				// sorted by the site short description
				String s1 = ((Site) o1).getShortDescription();
				String s2 = ((Site) o2).getShortDescription();
				result = compareString(s1, s2);
			} else if (m_criterion.equals(SORTED_BY_TYPE)) {
				// sorted by the site type
				String s1 = ((Site) o1).getType();
				String s2 = ((Site) o2).getType();
				result = compareString(s1, s2);
			} else if (m_criterion.equals(SortType.CREATED_BY_ASC.toString())) {
				// sorted by the site creator
				String s1 = ((Site) o1).getProperties().getProperty(
						"CHEF:creator");
				String s2 = ((Site) o2).getProperties().getProperty(
						"CHEF:creator");
				result = compareString(s1, s2);
			} else if (m_criterion.equals(SORTED_BY_STATUS)) {
				// sort by the status, published or unpublished
				int i1 = ((Site) o1).isPublished() ? 1 : 0;
				int i2 = ((Site) o2).isPublished() ? 1 : 0;
				if (i1 > i2) {
					result = 1;
				} else {
					result = -1;
				}
			} else if (m_criterion.equals(SORTED_BY_JOINABLE)) {
				// sort by whether the site is joinable or not
				boolean b1 = ((Site) o1).isJoinable();
				boolean b2 = ((Site) o2).isJoinable();
				if (b1 == b2) {
					result = 0;
				} else if (b1 == true) {
					result = 1;
				} else {
					result = -1;
				}
			} else if (m_criterion.equals(SORTED_BY_PARTICIPANT_NAME)) {
				// sort by whether the site is joinable or not
				String s1 = null;
				if (o1.getClass().equals(Participant.class)) {
					s1 = ((Participant) o1).getName();
				}

				String s2 = null;
				if (o2.getClass().equals(Participant.class)) {
					s2 = ((Participant) o2).getName();
				}
				
				result = compareString(s1, s2);

			} else if (m_criterion.equals(SORTED_BY_PARTICIPANT_UNIQNAME)) {
				// sort by whether the site is joinable or not
				String s1 = null;
				if (o1.getClass().equals(Participant.class)) {
					s1 = ((Participant) o1).getUniqname();
				}

				String s2 = null;
				if (o2.getClass().equals(Participant.class)) {
					s2 = ((Participant) o2).getUniqname();
				}

				result = compareString(s1, s2);
			} else if (m_criterion.equals(SORTED_BY_PARTICIPANT_ROLE)) {
				String s1 = "";
				if (o1.getClass().equals(Participant.class)) {
					s1 = ((Participant) o1).getRole();
				}

				String s2 = "";
				if (o2.getClass().equals(Participant.class)) {
					s2 = ((Participant) o2).getRole();
				}

				result = compareString(s1, s2);
			} else if (m_criterion.equals(SORTED_BY_PARTICIPANT_COURSE)) {
				// sort by whether the site is joinable or not
				String s1 = null;
				if (o1.getClass().equals(Participant.class)) {
					s1 = ((Participant) o1).getSection();
				}

				String s2 = null;
				if (o2.getClass().equals(Participant.class)) {
					s2 = ((Participant) o2).getSection();
				}

				result = compareString(s1, s2);
			} else if (m_criterion.equals(SORTED_BY_PARTICIPANT_ID)) {
				String s1 = null;
				if (o1.getClass().equals(Participant.class)) {
					s1 = ((Participant) o1).getRegId();
				}

				String s2 = null;
				if (o2.getClass().equals(Participant.class)) {
					s2 = ((Participant) o2).getRegId();
				}

				result = compareString(s1, s2);
			} else if (m_criterion.equals(SORTED_BY_PARTICIPANT_CREDITS)) {
				String s1 = null;
				if (o1.getClass().equals(Participant.class)) {
					s1 = ((Participant) o1).getCredits();
				}

				String s2 = null;
				if (o2.getClass().equals(Participant.class)) {
					s2 = ((Participant) o2).getCredits();
				}

				result = compareString(s1, s2);
			} else if (m_criterion.equals(SORTED_BY_CREATION_DATE)) {
				// sort by the site's creation date
				Time t1 = null;
				Time t2 = null;

				// get the times
				try {
					t1 = ((Site) o1).getProperties().getTimeProperty(
							ResourceProperties.PROP_CREATION_DATE);
				} catch (EntityPropertyNotDefinedException e) {
				} catch (EntityPropertyTypeException e) {
				}

				try {
					t2 = ((Site) o2).getProperties().getTimeProperty(
							ResourceProperties.PROP_CREATION_DATE);
				} catch (EntityPropertyNotDefinedException e) {
				} catch (EntityPropertyTypeException e) {
				}
				if (t1 == null) {
					result = -1;
				} else if (t2 == null) {
					result = 1;
				} else if (t1.before(t2)) {
					result = -1;
				} else {
					result = 1;
				}
			} else if (m_criterion.equals(rb.getString("group.title"))) {
				// sorted by the group title
				String s1 = ((Group) o1).getTitle();
				String s2 = ((Group) o2).getTitle();
				result = compareString(s1, s2);
			} else if (m_criterion.equals(rb.getString("group.number"))) {
				// sorted by the group title
				int n1 = ((Group) o1).getMembers().size();
				int n2 = ((Group) o2).getMembers().size();
				result = (n1 > n2) ? 1 : -1;
			} else if (m_criterion.equals(SORTED_BY_MEMBER_NAME)) {
				// sorted by the member name
				String s1 = null;
				String s2 = null;

				try {
					s1 = UserDirectoryService
							.getUser(((Member) o1).getUserId()).getSortName();
				} catch (Exception ignore) {

				}

				try {
					s2 = UserDirectoryService
							.getUser(((Member) o2).getUserId()).getSortName();
				} catch (Exception ignore) {

				}
				result = compareString(s1, s2);
			}

			if (m_asc == null)
				m_asc = Boolean.TRUE.toString();

			// sort ascending or descending
			if (m_asc.equals(Boolean.FALSE.toString())) {
				result = -result;
			}

			return result;

		} // compare

		private int compareString(String s1, String s2) {
			int result;
			if (s1 == null && s2 == null) {
				result = 0;
			} else if (s2 == null) {
				result = 1;
			} else if (s1 == null) {
				result = -1;
			} else {
				result = collator.compare(s1, s2);
			}
			return result;
		}

	} // SiteComparator

	private class ToolComparator implements Comparator {
		/**
		 * implementing the Comparator compare function
		 * 
		 * @param o1
		 *            The first object
		 * @param o2
		 *            The second object
		 * @return The compare result. 1 is o1 < o2; 0 is o1.equals(o2); -1
		 *         otherwise
		 */
		public int compare(Object o1, Object o2) {
			try {
				return ((Tool) o1).getTitle().compareTo(((Tool) o2).getTitle());
			} catch (Exception e) {
			}
			return -1;

		} // compare

	} // ToolComparator

	public class MyIcon {
		protected String m_name = null;

		protected String m_url = null;

		protected String m_skin = null;

		public MyIcon(String name, String url, String skin) {
			m_name = name;
			m_url = url;
			m_skin = skin;
		}

		public String getName() {
			return m_name;
		}

		public String getUrl() {
			return m_url;
		}

		public String getSkin() {
			return m_skin;
		}
	}

	// a utility class for form select options
	public class IdAndText {
		public int id;

		public String text;

		public int getId() {
			return id;
		}

		public String getText() {
			return text;
		}

	} // IdAndText

	// a utility class for working with ToolConfigurations and ToolRegistrations
	// %%% convert featureList from IdAndText to Tool so getFeatures item.id =
	// chosen-feature.id is a direct mapping of data
	public class MyTool {
		public String id = NULL_STRING;

		public String title = NULL_STRING;

		public String description = NULL_STRING;

		public boolean selected = false;

		public String getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}

		public boolean getSelected() {
			return selected;
		}

	}

	/*
	 * WorksiteSetupPage is a utility class for working with site pages
	 * configured by Worksite Setup
	 * 
	 */
	public class WorksiteSetupPage {
		public String pageId = NULL_STRING;

		public String pageTitle = NULL_STRING;

		public String toolId = NULL_STRING;

		public String getPageId() {
			return pageId;
		}

		public String getPageTitle() {
			return pageTitle;
		}

		public String getToolId() {
			return toolId;
		}

	} // WorksiteSetupPage

	/**
	 * Participant in site access roles
	 * 
	 */
	public class Participant {
		public String name = NULL_STRING;

		// Note: uniqname is really a user ID
		public String uniqname = NULL_STRING;

		public String role = NULL_STRING;

		/** role from provider */
		public String providerRole = NULL_STRING;

		/** The member credits */
		protected String credits = NULL_STRING;

		/** The section */
		public String section = NULL_STRING;

		private Set sectionEidList;
		
		/** The regestration id */
		public String regId = NULL_STRING;

		/** removeable if not from provider */
		public boolean removeable = true;

		public String getName() {
			return name;
		}

		public String getUniqname() {
			return uniqname;
		}

		public String getRole() {
			return role;
		} // cast to Role

		public String getProviderRole() {
			return providerRole;
		}

		public boolean isRemoveable() {
			return removeable;
		}

		// extra info from provider
		public String getCredits() {
			return credits;
		} // getCredits

		public String getSection() {
			if (sectionEidList == null)
				return "";
			
			StringBuilder sb = new StringBuilder();
			Iterator it = sectionEidList.iterator();
			for (int i = 0; i < sectionEidList.size(); i ++) {
				String sectionEid = (String)it.next();
				if (i < 0)
					sb.append(",<br />");
				sb.append(sectionEid);
			}
					
			return sb.toString();
		} // getSection
		
		public Set getSectionEidList() {
			if (sectionEidList == null)
				sectionEidList = new HashSet();
			
			return sectionEidList;
		}
		
		public void addSectionEidToList(String eid) {
			if (sectionEidList == null)
				sectionEidList = new HashSet();
				
				sectionEidList.add(eid);
		}

		public String getRegId() {
			return regId;
		} // getRegId

		/**
		 * Access the user eid, if we can find it - fall back to the id if not.
		 * 
		 * @return The user eid.
		 */
		public String getEid() {
			try {
				return UserDirectoryService.getUserEid(uniqname);
			} catch (UserNotDefinedException e) {
				return uniqname;
			}
		}

		/**
		 * Access the user display id, if we can find it - fall back to the id
		 * if not.
		 * 
		 * @return The user display id.
		 */
		public String getDisplayId() {
			try {
				User user = UserDirectoryService.getUser(uniqname);
				return user.getDisplayId();
			} catch (UserNotDefinedException e) {
				return uniqname;
			}
		}

	} // Participant

	/**
	 * Student in roster
	 * 
	 */
	public class Student {
		public String name = NULL_STRING;

		public String uniqname = NULL_STRING;

		public String id = NULL_STRING;

		public String level = NULL_STRING;

		public String credits = NULL_STRING;

		public String role = NULL_STRING;

		public String course = NULL_STRING;

		public String section = NULL_STRING;

		public String getName() {
			return name;
		}

		public String getUniqname() {
			return uniqname;
		}

		public String getId() {
			return id;
		}

		public String getLevel() {
			return level;
		}

		public String getCredits() {
			return credits;
		}

		public String getRole() {
			return role;
		}

		public String getCourse() {
			return course;
		}

		public String getSection() {
			return section;
		}

	} // Student

	public class SiteInfo {
		public String site_id = NULL_STRING; // getId of Resource

		public String external_id = NULL_STRING; // if matches site_id

		// connects site with U-M
		// course information

		public String site_type = "";

		public String iconUrl = NULL_STRING;

		public String infoUrl = NULL_STRING;

		public boolean joinable = false;

		public String joinerRole = NULL_STRING;

		public String title = NULL_STRING; // the short name of the site

		public String short_description = NULL_STRING; // the short (20 char)

		// description of the
		// site

		public String description = NULL_STRING; // the longer description of

		// the site

		public String additional = NULL_STRING; // additional information on

		// crosslists, etc.

		public boolean published = false;

		public boolean include = true; // include the site in the Sites index;

		// default is true.

		public String site_contact_name = NULL_STRING; // site contact name

		public String site_contact_email = NULL_STRING; // site contact email

		public String getSiteId() {
			return site_id;
		}

		public String getSiteType() {
			return site_type;
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}

		public String getIconUrl() {
			return iconUrl;
		}

		public String getInfoUrll() {
			return infoUrl;
		}

		public boolean getJoinable() {
			return joinable;
		}

		public String getJoinerRole() {
			return joinerRole;
		}

		public String getAdditional() {
			return additional;
		}

		public boolean getPublished() {
			return published;
		}

		public boolean getInclude() {
			return include;
		}

		public String getSiteContactName() {
			return site_contact_name;
		}

		public String getSiteContactEmail() {
			return site_contact_email;
		}

	} // SiteInfo

	// dissertation tool related
	/**
	 * doFinish_grad_tools is called when creation of a Grad Tools site is
	 * confirmed
	 */
	public void doFinish_grad_tools(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		// set up for the coming template
		state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("continue"));
		int index = Integer.valueOf(params.getString("template-index"))
				.intValue();
		actionForTemplate("continue", index, params, state);

		// add the pre-configured Grad Tools tools to a new site
		addGradToolsFeatures(state);

		// TODO: hard coding this frame id is fragile, portal dependent, and
		// needs to be fixed -ggolden
		// schedulePeerFrameRefresh("sitenav");
		scheduleTopRefresh();

		resetPaging(state);

	}// doFinish_grad_tools

	/**
	 * addGradToolsFeatures adds features to a new Grad Tools student site
	 * 
	 */
	private void addGradToolsFeatures(SessionState state) {
		Site edit = null;
		Site template = null;

		// get a unique id
		String id = IdManager.createUuid();

		// get the Grad Tools student site template
		try {
			template = SiteService.getSite(SITE_GTS_TEMPLATE);
		} catch (Exception e) {
			if (Log.isWarnEnabled())
				M_log.warn("addGradToolsFeatures template " + e);
		}
		if (template != null) {
			// create a new site based on the template
			try {
				edit = SiteService.addSite(id, template);
			} catch (Exception e) {
				if (Log.isWarnEnabled())
					M_log.warn("addGradToolsFeatures add/edit site " + e);
			}

			// set the tab, etc.
			if (edit != null) {
				SiteInfo siteInfo = (SiteInfo) state
						.getAttribute(STATE_SITE_INFO);
				edit.setShortDescription(siteInfo.short_description);
				edit.setTitle(siteInfo.title);
				edit.setPublished(true);
				edit.setPubView(false);
				edit.setType(SITE_TYPE_GRADTOOLS_STUDENT);
				// ResourcePropertiesEdit rpe = edit.getPropertiesEdit();
				try {
					SiteService.save(edit);
				} catch (Exception e) {
					if (Log.isWarnEnabled())
						M_log.warn("addGradToolsFeartures commitEdit " + e);
				}

				// now that the site and realm exist, we can set the email alias
				// set the GradToolsStudent site alias as:
				// gradtools-uniqname@servername
				String alias = "gradtools-"
						+ SessionManager.getCurrentSessionUserId();
				String channelReference = mailArchiveChannelReference(id);
				try {
					AliasService.setAlias(alias, channelReference);
				} catch (IdUsedException ee) {
					addAlert(state, rb.getString("java.alias") + " " + alias
							+ " " + rb.getString("java.exists"));
				} catch (IdInvalidException ee) {
					addAlert(state, rb.getString("java.alias") + " " + alias
							+ " " + rb.getString("java.isinval"));
				} catch (PermissionException ee) {
					M_log.warn(SessionManager.getCurrentSessionUserId()
							+ " does not have permission to add alias. ");
				}
			}
		}

	} // addGradToolsFeatures

	/**
	 * handle with add site options
	 * 
	 */
	public void doAdd_site_option(RunData data) {
		String option = data.getParameters().getString("option");
		if (option.equals("finish")) {
			doFinish(data);
		} else if (option.equals("cancel")) {
			doCancel_create(data);
		} else if (option.equals("back")) {
			doBack(data);
		}
	} // doAdd_site_option

	/**
	 * handle with duplicate site options
	 * 
	 */
	public void doDuplicate_site_option(RunData data) {
		String option = data.getParameters().getString("option");
		if (option.equals("duplicate")) {
			doContinue(data);
		} else if (option.equals("cancel")) {
			doCancel(data);
		} else if (option.equals("finish")) {
			doContinue(data);
		}
	} // doDuplicate_site_option

	/**
	 * Special check against the Dissertation service, which might not be
	 * here...
	 * 
	 * @return
	 */
	protected boolean isGradToolsCandidate(String userId) {
		// DissertationService.isCandidate(userId) - but the hard way

		Object service = ComponentManager
				.get("org.sakaiproject.api.app.dissertation.DissertationService");
		if (service == null)
			return false;

		// the method signature
		Class[] signature = new Class[1];
		signature[0] = String.class;

		// the method name
		String methodName = "isCandidate";

		// find a method of this class with this name and signature
		try {
			Method method = service.getClass().getMethod(methodName, signature);

			// the parameters
			Object[] args = new Object[1];
			args[0] = userId;

			// make the call
			Boolean rv = (Boolean) method.invoke(service, args);
			return rv.booleanValue();
		} catch (Throwable t) {
		}

		return false;
	}

	/**
	 * User has a Grad Tools student site
	 * 
	 * @return
	 */
	protected boolean hasGradToolsStudentSite(String userId) {
		boolean has = false;
		int n = 0;
		try {
			n = SiteService.countSites(
					org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
					SITE_TYPE_GRADTOOLS_STUDENT, null, null);
			if (n > 0)
				has = true;
		} catch (Exception e) {
			if (Log.isWarnEnabled())
				M_log.warn("hasGradToolsStudentSite " + e);
		}

		return has;

	}// hasGradToolsStudentSite

	/**
	 * Get the mail archive channel reference for the main container placement
	 * for this site.
	 * 
	 * @param siteId
	 *            The site id.
	 * @return The mail archive channel reference for this site.
	 */
	protected String mailArchiveChannelReference(String siteId) {
				
		Object m = ComponentManager
				.get("org.sakaiproject.mailarchive.api.MailArchiveService");

		if (m != null) {
			return "/mailarchive"+Entity.SEPARATOR+"channel"+Entity.SEPARATOR+siteId+Entity.SEPARATOR+SiteService.MAIN_CONTAINER;
		} else {
			return "";
		}
	}

	/**
	 * Transfer a copy of all entites from another context for any entity
	 * producer that claims this tool id.
	 * 
	 * @param toolId
	 *            The tool id.
	 * @param fromContext
	 *            The context to import from.
	 * @param toContext
	 *            The context to import into.
	 */
	protected void transferCopyEntities(String toolId, String fromContext,
			String toContext) {
		// TODO: used to offer to resources first - why? still needed? -ggolden

		// offer to all EntityProducers
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i
				.hasNext();) {
			EntityProducer ep = (EntityProducer) i.next();
			if (ep instanceof EntityTransferrer) {
				try {
					EntityTransferrer et = (EntityTransferrer) ep;

					// if this producer claims this tool id
					if (ArrayUtil.contains(et.myToolIds(), toolId)) {
						et.transferCopyEntities(fromContext, toContext,
								new Vector());
					}
				} catch (Throwable t) {
					M_log.warn(
							"Error encountered while asking EntityTransfer to transferCopyEntities from: "
									+ fromContext + " to: " + toContext, t);
				}
			}
		}
	}

	/**
	 * @return Get a list of all tools that support the import (transfer copy)
	 *         option
	 */
	protected Set importTools() {
		HashSet rv = new HashSet();

		// offer to all EntityProducers
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i
				.hasNext();) {
			EntityProducer ep = (EntityProducer) i.next();
			if (ep instanceof EntityTransferrer) {
				EntityTransferrer et = (EntityTransferrer) ep;

				String[] tools = et.myToolIds();
				if (tools != null) {
					for (int t = 0; t < tools.length; t++) {
						rv.add(tools[t]);
					}
				}
			}
		}

		return rv;
	}

	/**
	 * @param state
	 * @return Get a list of all tools that should be included as options for
	 *         import
	 */
	protected List getToolsAvailableForImport(SessionState state) {
		// The Web Content and News tools do not follow the standard rules for
		// import
		// Even if the current site does not contain the tool, News and WC will
		// be
		// an option if the imported site contains it
		boolean displayWebContent = false;
		boolean displayNews = false;

		Set importSites = ((Hashtable) state.getAttribute(STATE_IMPORT_SITES))
				.keySet();
		Iterator sitesIter = importSites.iterator();
		while (sitesIter.hasNext()) {
			Site site = (Site) sitesIter.next();
			if (site.getToolForCommonId("sakai.iframe") != null)
				displayWebContent = true;
			if (site.getToolForCommonId("sakai.news") != null)
				displayNews = true;
		}

		List toolsOnImportList = (List) state
				.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		if (displayWebContent && !toolsOnImportList.contains("sakai.iframe"))
			toolsOnImportList.add("sakai.iframe");
		if (displayNews && !toolsOnImportList.contains("sakai.news"))
			toolsOnImportList.add("sakai.news");

		return toolsOnImportList;
	} // getToolsAvailableForImport

	private void setTermListForContext(Context context, SessionState state,
			boolean upcomingOnly) {
		List terms;
		if (upcomingOnly) {
			terms = cms != null?cms.getCurrentAcademicSessions():null;
		} else { // get all
			terms = cms != null?cms.getAcademicSessions():null;
		}
		if (terms != null && terms.size() > 0) {
			context.put("termList", terms);
		}
	} // setTermListForContext

	private void setSelectedTermForContext(Context context, SessionState state,
			String stateAttribute) {
		if (state.getAttribute(stateAttribute) != null) {
			context.put("selectedTerm", state.getAttribute(stateAttribute));
		}
	} // setSelectedTermForContext

	/**
	 * rewrote for 2.4
	 * 
	 * @param userId
	 * @param academicSessionEid
	 * @param courseOfferingHash
	 * @param sectionHash
	 */
	private void prepareCourseAndSectionMap(String userId,
			String academicSessionEid, HashMap courseOfferingHash,
			HashMap sectionHash) {

		// looking for list of courseOffering and sections that should be
		// included in
		// the selection list. The course offering must be offered
		// 1. in the specific academic Session
		// 2. that the specified user has right to attach its section to a
		// course site
		// map = (section.eid, sakai rolename)
		if (groupProvider == null)
		{
			M_log.warn("Group provider not found");
			return;
		}
		
		Map map = groupProvider.getGroupRolesForUser(userId);
		if (map == null)
			return;

		Set keys = map.keySet();
		Set roleSet = getRolesAllowedToAttachSection();
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String sectionEid = (String) i.next();
			String role = (String) map.get(sectionEid);
			if (includeRole(role, roleSet)) {
				Section section = null;
				getCourseOfferingAndSectionMap(academicSessionEid, courseOfferingHash, sectionHash, sectionEid, section);
			}
		}
		
		// now consider those user with affiliated sections
		List affiliatedSectionEids = affiliatedSectionProvider.getAffiliatedSectionEids(userId, academicSessionEid);
		if (affiliatedSectionEids != null)
		{
			for (int k = 0; k < affiliatedSectionEids.size(); k++) {
				String sectionEid = (String) affiliatedSectionEids.get(k);
				Section section = null;
				getCourseOfferingAndSectionMap(academicSessionEid, courseOfferingHash, sectionHash, sectionEid, section);
			}
		}
		
		
	} // prepareCourseAndSectionMap

	private void getCourseOfferingAndSectionMap(String academicSessionEid, HashMap courseOfferingHash, HashMap sectionHash, String sectionEid, Section section) {
		try {
			section = cms.getSection(sectionEid);
		} catch (IdNotFoundException e) {
			M_log.warn(e.getMessage());
		}
		if (section != null) {
			String courseOfferingEid = section.getCourseOfferingEid();
			CourseOffering courseOffering = cms
					.getCourseOffering(courseOfferingEid);
			String sessionEid = courseOffering.getAcademicSession()
					.getEid();
			if (academicSessionEid.equals(sessionEid)) {
				// a long way to the conclusion that yes, this course
				// offering
				// should be included in the selected list. Sigh...
				// -daisyf
				ArrayList sectionList = (ArrayList) sectionHash
						.get(courseOffering.getEid());
				if (sectionList == null) {
					sectionList = new ArrayList();
				}
				sectionList.add(new SectionObject(section));
				sectionHash.put(courseOffering.getEid(), sectionList);
				courseOfferingHash.put(courseOffering.getEid(),
						courseOffering);
			}
		}
	}

	/**
	 * for 2.4
	 * 
	 * @param role
	 * @return
	 */
	private boolean includeRole(String role, Set roleSet) {
		boolean includeRole = false;
		for (Iterator i = roleSet.iterator(); i.hasNext();) {
			String r = (String) i.next();
			if (r.equals(role)) {
				includeRole = true;
				break;
			}
		}
		return includeRole;
	} // includeRole

	protected Set getRolesAllowedToAttachSection() {
		// Use !site.template.[site_type]
		String azgId = "!site.template.course";
		AuthzGroup azgTemplate;
		try {
			azgTemplate = AuthzGroupService.getAuthzGroup(azgId);
		} catch (GroupNotDefinedException e) {
			M_log.warn("Could not find authz group " + azgId);
			return new HashSet();
		}
		Set roles = azgTemplate.getRolesIsAllowed("site.upd");
		roles.addAll(azgTemplate.getRolesIsAllowed("realm.upd"));
		return roles;
	} // getRolesAllowedToAttachSection

	/**
	 * Here, we will preapre two HashMap: 1. courseOfferingHash stores
	 * courseOfferingId and CourseOffering 2. sectionHash stores
	 * courseOfferingId and a list of its Section We sorted the CourseOffering
	 * by its eid & title and went through them one at a time to construct the
	 * CourseObject that is used for the displayed in velocity. Each
	 * CourseObject will contains a list of CourseOfferingObject(again used for
	 * vm display). Usually, a CourseObject would only contain one
	 * CourseOfferingObject. A CourseObject containing multiple
	 * CourseOfferingObject implies that this is a cross-listing situation.
	 * 
	 * @param userId
	 * @param academicSessionEid
	 * @return
	 */
	private List prepareCourseAndSectionListing(String userId,
			String academicSessionEid, SessionState state) {
		// courseOfferingHash = (courseOfferingEid, vourseOffering)
		// sectionHash = (courseOfferingEid, list of sections)
		HashMap courseOfferingHash = new HashMap();
		HashMap sectionHash = new HashMap();
		prepareCourseAndSectionMap(userId, academicSessionEid,
				courseOfferingHash, sectionHash);
		// courseOfferingHash & sectionHash should now be filled with stuffs
		// put section list in state for later use

		state.setAttribute(STATE_PROVIDER_SECTION_LIST,
				getSectionList(sectionHash));

		ArrayList offeringList = new ArrayList();
		Set keys = courseOfferingHash.keySet();
		for (Iterator i = keys.iterator(); i.hasNext();) {
			CourseOffering o = (CourseOffering) courseOfferingHash
					.get((String) i.next());
			offeringList.add(o);
		}

		Collection offeringListSorted = sortOffering(offeringList);
		ArrayList resultedList = new ArrayList();

		// use this to keep track of courseOffering that we have dealt with
		// already
		// this is important 'cos cross-listed offering is dealt with together
		// with its
		// equivalents
		ArrayList dealtWith = new ArrayList();

		for (Iterator j = offeringListSorted.iterator(); j.hasNext();) {
			CourseOffering o = (CourseOffering) j.next();
			if (!dealtWith.contains(o.getEid())) {
				// 1. construct list of CourseOfferingObject for CourseObject
				ArrayList l = new ArrayList();
				CourseOfferingObject coo = new CourseOfferingObject(o,
						(ArrayList) sectionHash.get(o.getEid()));
				l.add(coo);

				// 2. check if course offering is cross-listed
				Set set = cms.getEquivalentCourseOfferings(o.getEid());
				if (set != null)
				{
					for (Iterator k = set.iterator(); k.hasNext();) {
						CourseOffering eo = (CourseOffering) k.next();
						if (courseOfferingHash.containsKey(eo.getEid())) {
							// => cross-listed, then list them together
							CourseOfferingObject coo_equivalent = new CourseOfferingObject(
									eo, (ArrayList) sectionHash.get(eo.getEid()));
							l.add(coo_equivalent);
							dealtWith.add(eo.getEid());
						}
					}
				}
				CourseObject co = new CourseObject(o, l);
				dealtWith.add(o.getEid());
				resultedList.add(co);
			}
		}
		return resultedList;
	} // prepareCourseAndSectionListing

	/**
	 * Sort CourseOffering by order of eid, title uisng velocity SortTool
	 * 
	 * @param offeringList
	 * @return
	 */
	private Collection sortOffering(ArrayList offeringList) {
		return sortCmObject(offeringList);
		/*
		 * List propsList = new ArrayList(); propsList.add("eid");
		 * propsList.add("title"); SortTool sort = new SortTool(); return
		 * sort.sort(offeringList, propsList);
		 */
	} // sortOffering

	/**
	 * sort any Cm object such as CourseOffering, CourseOfferingObject,
	 * SectionObject provided object has getter & setter for eid & title
	 * 
	 * @param list
	 * @return
	 */
	private Collection sortCmObject(List list) {
		if (list != null) {
			List propsList = new ArrayList();
			propsList.add("eid");
			propsList.add("title");
			SortTool sort = new SortTool();
			return sort.sort(list, propsList);
		} else {
			return list;
		}
	} // sortCmObject

	/**
	 * this object is used for displaying purposes in chef_site-newSiteCourse.vm
	 */
	public class SectionObject {
		public Section section;

		public String eid;

		public String title;

		public String category;

		public String categoryDescription;

		public boolean isLecture;

		public boolean attached;

		public String authorizer;

		public SectionObject(Section section) {
			this.section = section;
			this.eid = section.getEid();
			this.title = section.getTitle();
			this.category = section.getCategory();
			this.categoryDescription = cms
					.getSectionCategoryDescription(section.getCategory());
			if ("01.lct".equals(section.getCategory())) {
				this.isLecture = true;
			} else {
				this.isLecture = false;
			}
			Set set = authzGroupService.getAuthzGroupIds(section.getEid());
			if (set != null && !set.isEmpty()) {
				this.attached = true;
			} else {
				this.attached = false;
			}
		}

		public Section getSection() {
			return section;
		}

		public String getEid() {
			return eid;
		}

		public String getTitle() {
			return title;
		}

		public String getCategory() {
			return category;
		}

		public String getCategoryDescription() {
			return categoryDescription;
		}

		public boolean getIsLecture() {
			return isLecture;
		}

		public boolean getAttached() {
			return attached;
		}

		public String getAuthorizer() {
			return authorizer;
		}

		public void setAuthorizer(String authorizer) {
			this.authorizer = authorizer;
		}

	} // SectionObject constructor

	/**
	 * this object is used for displaying purposes in chef_site-newSiteCourse.vm
	 */
	public class CourseObject {
		public String eid;

		public String title;

		public List courseOfferingObjects;

		public CourseObject(CourseOffering offering, List courseOfferingObjects) {
			this.eid = offering.getEid();
			this.title = offering.getTitle();
			this.courseOfferingObjects = courseOfferingObjects;
		}

		public String getEid() {
			return eid;
		}

		public String getTitle() {
			return title;
		}

		public List getCourseOfferingObjects() {
			return courseOfferingObjects;
		}

	} // CourseObject constructor

	/**
	 * this object is used for displaying purposes in chef_site-newSiteCourse.vm
	 */
	public class CourseOfferingObject {
		public String eid;

		public String title;

		public List sections;

		public CourseOfferingObject(CourseOffering offering,
				List unsortedSections) {
			List propsList = new ArrayList();
			propsList.add("category");
			propsList.add("eid");
			SortTool sort = new SortTool();
			this.sections = new ArrayList();
			if (unsortedSections != null) {
				this.sections = (List) sort.sort(unsortedSections, propsList);
			}
			this.eid = offering.getEid();
			this.title = offering.getTitle();
		}

		public String getEid() {
			return eid;
		}

		public String getTitle() {
			return title;
		}

		public List getSections() {
			return sections;
		}
	} // CourseOfferingObject constructor

	/**
	 * get campus user directory for dispaly in chef_newSiteCourse.vm
	 * 
	 * @return
	 */
	private String getCampusDirectory() {
		return ServerConfigurationService.getString(
				"site-manage.campusUserDirectory", null);
	} // getCampusDirectory

	private void removeAnyFlagedSection(SessionState state,
			ParameterParser params) {
		List all = new ArrayList();
		List providerCourseList = (List) state
				.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		if (providerCourseList != null && providerCourseList.size() > 0) {
			all.addAll(providerCourseList);
		}
		List manualCourseList = (List) state
				.getAttribute(SITE_MANUAL_COURSE_LIST);
		if (manualCourseList != null && manualCourseList.size() > 0) {
			all.addAll(manualCourseList);
		}
		
		for (int i = 0; i < all.size(); i++) {
			String eid = (String) all.get(i);
			String field = "removeSection" + eid;
			String toRemove = params.getString(field);
			if ("true".equals(toRemove)) {
				// eid is in either providerCourseList or manualCourseList
				// either way, just remove it
				if (providerCourseList != null)
					providerCourseList.remove(eid);
				if (manualCourseList != null)
					manualCourseList.remove(eid);
			}
		}

		List<SectionObject> requestedCMSections = (List<SectionObject>) state
				.getAttribute(STATE_CM_REQUESTED_SECTIONS);

		if (requestedCMSections != null) {
			for (int i = 0; i < requestedCMSections.size(); i++) {
				SectionObject so = (SectionObject) requestedCMSections.get(i);

				String field = "removeSection" + so.getEid();
				String toRemove = params.getString(field);

				if ("true".equals(toRemove)) {
					requestedCMSections.remove(so);
				}

			}

			if (requestedCMSections.size() == 0)
				state.removeAttribute(STATE_CM_REQUESTED_SECTIONS);
		}

		List<SectionObject> authorizerSections = (List<SectionObject>) state
				.getAttribute(STATE_CM_AUTHORIZER_SECTIONS);
		if (authorizerSections != null) {
			for (int i = 0; i < authorizerSections.size(); i++) {
				SectionObject so = (SectionObject) authorizerSections.get(i);

				String field = "removeSection" + so.getEid();
				String toRemove = params.getString(field);

				if ("true".equals(toRemove)) {
					authorizerSections.remove(so);
				}

			}

			if (authorizerSections.size() == 0)
				state.removeAttribute(STATE_CM_AUTHORIZER_SECTIONS);
		}

		// if list is empty, set to null. This is important 'cos null is
		// the indication that the list is empty in the code. See case 2 on line
		// 1081
		if (manualCourseList != null && manualCourseList.size() == 0)
			manualCourseList = null;
		if (providerCourseList != null && providerCourseList.size() == 0)
			providerCourseList = null;
	}

	private void collectNewSiteInfo(SiteInfo siteInfo, SessionState state,
			ParameterParser params, List providerChosenList) {
		if (state.getAttribute(STATE_MESSAGE) == null) {
			siteInfo = new SiteInfo();
			if (state.getAttribute(STATE_SITE_INFO) != null) {
				siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			}

			// site title is the title of the 1st section selected -
			// daisyf's note
			if (providerChosenList != null && providerChosenList.size() >= 1) {
				String title = prepareTitle((List) state
						.getAttribute(STATE_PROVIDER_SECTION_LIST),
						providerChosenList);
				siteInfo.title = title;
			}
			state.setAttribute(STATE_SITE_INFO, siteInfo);

			if (params.getString("manualAdds") != null
					&& ("true").equals(params.getString("manualAdds"))) {
				// if creating a new site
				state.setAttribute(STATE_TEMPLATE_INDEX, "37");

				state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, new Integer(
						1));

			} else if (params.getString("findCourse") != null
					&& ("true").equals(params.getString("findCourse"))) {
				state.setAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN,
						providerChosenList);
				prepFindPage(state);
			} else {
				// no manual add
				state.removeAttribute(STATE_MANUAL_ADD_COURSE_NUMBER);
				state.removeAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
				state.removeAttribute(STATE_SITE_QUEST_UNIQNAME);

				if (getStateSite(state) != null) {
					// if revising a site, go to the confirmation
					// page of adding classes
					//state.setAttribute(STATE_TEMPLATE_INDEX, "37");
				} else {
					// if creating a site, go the the site
					// information entry page
					state.setAttribute(STATE_TEMPLATE_INDEX, "2");
				}
			}
		}
	}

	/**
	 * By default, courseManagement is implemented
	 * 
	 * @return
	 */
	private boolean courseManagementIsImplemented() {
		boolean returnValue = true;
		String isImplemented = ServerConfigurationService.getString(
				"site-manage.courseManagementSystemImplemented", "true");
		if (("false").equals(isImplemented))
			returnValue = false;
		return returnValue;
	}

	private List getCMSubjects() {
		String subjectCategory = getCMSubjectCategory();

		if (cms == null || subjectCategory == null) {
			return new ArrayList(0);
		}
		Collection c = sortCmObject(cms.findCourseSets(subjectCategory));
		return (List) c;

	}

	private List getCMSections(String offeringEid) {
		if (offeringEid == null || offeringEid.trim().length() == 0)
			return null;

		if (cms != null) {
			Set sections = cms.getSections(offeringEid);
			Collection c = sortCmObject(new ArrayList(sections));
			return (List) c;
		}

		return new ArrayList(0);
	}

	private List getCMCourseOfferings(String subjectEid, String termID) {
		if (subjectEid == null || subjectEid.trim().length() == 0
				|| termID == null || termID.trim().length() == 0)
			return null;

		if (cms != null) {
			Set offerings = cms.getCourseOfferingsInCourseSet(subjectEid);// ,
			// termID);
			ArrayList returnList = new ArrayList();
			Iterator coIt = offerings.iterator();

			while (coIt.hasNext()) {
				CourseOffering co = (CourseOffering) coIt.next();
				AcademicSession as = co.getAcademicSession();
				if (as != null && as.getEid().equals(termID))
					returnList.add(co);
			}
			Collection c = sortCmObject(returnList);

			return (List) c;
		}

		return new ArrayList(0);
	}

	private String getCMSubjectCategory() {
		if (cmSubjectCategory == null) {
			cmSubjectCategory = ServerConfigurationService
					.getString("site-manage.cms.subject.category");

			if (cmSubjectCategory == null) {
				if (warnedNoSubjectCategory)
					M_log
							.debug(rb
									.getString("nscourse.cm.configure.log.nosubjectcat"));
				else {
					M_log
							.info(rb
									.getString("nscourse.cm.configure.log.nosubjectcat"));
					warnedNoSubjectCategory = true;
				}
			}
		}
		return cmSubjectCategory;
	}

	private List<String> getCMLevelLabels() {
		List<String> rv = new Vector<String>();
		List<SectionField> fields = sectionFieldProvider.getRequiredFields();
		for (int k = 0; k < fields.size(); k++) 
		{
			SectionField sectionField = (SectionField) fields.get(k);
			rv.add(sectionField.getLabelKey());
		}
		return rv;
	}

	private void prepFindPage(SessionState state) {
		final List cmLevels = getCMLevelLabels(), selections = (List) state
				.getAttribute(STATE_CM_LEVEL_SELECTIONS);
		int lvlSz = 0;

		if (cmLevels == null || (lvlSz = cmLevels.size()) < 1) {
			// TODO: no cm levels configured, redirect to manual add
			return;
		}

		if (selections != null && selections.size() == lvlSz) {
			Section sect = cms.getSection((String) selections.get(selections
					.size() - 1));
			SectionObject so = new SectionObject(sect);

			state.setAttribute(STATE_CM_SELECTED_SECTION, so);
		} else
			state.removeAttribute(STATE_CM_SELECTED_SECTION);

		state.setAttribute(STATE_CM_LEVELS, cmLevels);
		state.setAttribute(STATE_CM_LEVEL_SELECTIONS, selections);

		// check the configuration setting for choosing next screen
		Boolean skipCourseSectionSelection = ServerConfigurationService.getBoolean("wsetup.skipCourseSectionSelection", Boolean.FALSE);
		if (!skipCourseSectionSelection.booleanValue())
		{
			// go to the course/section selection page
			state.setAttribute(STATE_TEMPLATE_INDEX, "53");
		}
		else
		{
			// skip the course/section selection page, go directly into the manually create course page
			state.setAttribute(STATE_TEMPLATE_INDEX, "37");
		}
	}

	private void addRequestedSection(SessionState state) {
		SectionObject so = (SectionObject) state
				.getAttribute(STATE_CM_SELECTED_SECTION);
		String uniqueName = (String) state
				.getAttribute(STATE_SITE_QUEST_UNIQNAME);

		if (so == null)
			return;
		so.setAuthorizer(uniqueName);
		
		if (getStateSite(state) == null)
		{
			// creating new site
			List<SectionObject> requestedSections = (List<SectionObject>) state.getAttribute(STATE_CM_REQUESTED_SECTIONS);
	
			if (requestedSections == null) {
				requestedSections = new ArrayList<SectionObject>();
			}
	
			// don't add duplicates
			if (!requestedSections.contains(so))
				requestedSections.add(so);
	
			// if the title has not yet been set and there is just
			// one section, set the title to that section's EID
			if (requestedSections.size() == 1) {
				SiteInfo siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
	
				if (siteInfo == null) {
					siteInfo = new SiteInfo();
				}
	
				if (siteInfo.title == null || siteInfo.title.trim().length() == 0) {
					siteInfo.title = so.getEid();
				}
	
				state.setAttribute(STATE_SITE_INFO, siteInfo);
			}
	
			state.setAttribute(STATE_CM_REQUESTED_SECTIONS, requestedSections);
			state.removeAttribute(STATE_CM_SELECTED_SECTION);
		}
		else
		{
			// editing site
			state.setAttribute(STATE_CM_SELECTED_SECTION, so);
			
			List<SectionObject> cmSelectedSections = (List<SectionObject>) state.getAttribute(STATE_CM_SELECTED_SECTIONS);
			
			if (cmSelectedSections == null) {
				cmSelectedSections = new ArrayList<SectionObject>();
			}
	
			// don't add duplicates
			if (!cmSelectedSections.contains(so))
				cmSelectedSections.add(so);
			state.setAttribute(STATE_CM_SELECTED_SECTIONS, cmSelectedSections);
			state.removeAttribute(STATE_CM_SELECTED_SECTION);
		}
		state.removeAttribute(STATE_CM_LEVEL_SELECTIONS);
	}

	public void doFind_course(RunData data) {
		final SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		final ParameterParser params = data.getParameters();
		final String option = params.get("option");

		if (option != null) 
		{
			if ("continue".equals(option)) 
			{
				String uniqname = StringUtil.trimToNull(params
						.getString("uniqname"));
				state.setAttribute(STATE_SITE_QUEST_UNIQNAME, uniqname);

				if (state.getAttribute(STATE_FUTURE_TERM_SELECTED) != null
						&& !((Boolean) state
								.getAttribute(STATE_FUTURE_TERM_SELECTED))
								.booleanValue()) 
				{
					// if a future term is selected, do not check authorization
					// uniqname
					if (uniqname == null) 
					{
						addAlert(state, rb.getString("java.author")
								+ " "
								+ ServerConfigurationService
										.getString("officialAccountName")
								+ ". ");
					} 
					else 
					{
						try 
						{
							UserDirectoryService.getUserByEid(uniqname);
							addRequestedSection(state);
						} 
						catch (UserNotDefinedException e) 
						{
							addAlert(
									state,
									rb.getString("java.validAuthor1")
											+ " "
											+ ServerConfigurationService
													.getString("officialAccountName")
											+ " "
											+ rb.getString("java.validAuthor2"));
						}
					}
				}
				else
				{
					addRequestedSection(state);
				}
				if (state.getAttribute(STATE_MESSAGE) == null) {
					if (getStateSite(state) == null) {
						state.setAttribute(STATE_TEMPLATE_INDEX, "2");
					} else {
						state.setAttribute(STATE_TEMPLATE_INDEX, "44");
					}
				}

				doContinue(data);
				return;
			} else if ("back".equals(option)) {
				doBack(data);
				return;
			} else if ("cancel".equals(option)) {
				if (getStateSite(state) == null) 
				{
					doCancel_create(data);// cancel from new site creation
				}
				else
				{
					doCancel(data);// cancel from site info editing
				}
				return;
			} else if (option.equals("add")) {
				addRequestedSection(state);
				return;
			} else if (option.equals("manual")) {
				// TODO: send to case 37
				state.setAttribute(STATE_TEMPLATE_INDEX, "37");

				state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, new Integer(
						1));

				return;
			} else if (option.equals("remove"))
				removeAnyFlagedSection(state, params);
		}

		final List selections = new ArrayList(3);

		int cmLevel = 3;
		String deptChanged = params.get("deptChanged");
		if ("true".equals(deptChanged)) {
			// when dept changes, remove selection on courseOffering and
			// courseSection
			cmLevel = 1;
		}
		for (int i = 0; i < cmLevel; i++) {
			String val = params.get("idField_" + i);

			if (val == null || val.trim().length() < 1) {
				break;
			}
			selections.add(val);
		}

		state.setAttribute(STATE_CM_LEVEL_SELECTIONS, selections);

		prepFindPage(state);
	}

	/**
	 * return the title of the 1st section in the chosen list that has an
	 * enrollment set. No discrimination on section category
	 * 
	 * @param sectionList
	 * @param chosenList
	 * @return
	 */
	private String prepareTitle(List sectionList, List chosenList) {
		String title = null;
		HashMap map = new HashMap();
		for (Iterator i = sectionList.iterator(); i.hasNext();) {
			SectionObject o = (SectionObject) i.next();
			map.put(o.getEid(), o.getSection());
		}
		for (int j = 0; j < chosenList.size(); j++) {
			String eid = (String) chosenList.get(j);
			Section s = (Section) map.get(eid);
			// we will always has a title regardless but we prefer it to be the
			// 1st section on the chosen list that has an enrollment set
			if (j == 0) {
				title = s.getTitle();
			}
			if (s.getEnrollmentSet() != null) {
				title = s.getTitle();
				break;
			}
		}
		return title;
	} // prepareTitle

	/**
	 * return an ArrayList of SectionObject
	 * 
	 * @param sectionHash
	 *            contains an ArrayList collection of SectionObject
	 * @return
	 */
	private ArrayList getSectionList(HashMap sectionHash) {
		ArrayList list = new ArrayList();
		// values is an ArrayList of section
		Collection c = sectionHash.values();
		for (Iterator i = c.iterator(); i.hasNext();) {
			ArrayList l = (ArrayList) i.next();
			list.addAll(l);
		}
		return list;
	}

	private String getAuthorizers(SessionState state) {
		String authorizers = "";
		ArrayList list = (ArrayList) state
				.getAttribute(STATE_CM_AUTHORIZER_LIST);
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				if (i == 0) {
					authorizers = (String) list.get(i);
				} else {
					authorizers = authorizers + ", " + list.get(i);
				}
			}
		}
		return authorizers;
	}

	private List prepareSectionObject(List sectionList, String userId) {
		ArrayList list = new ArrayList();
		if (sectionList != null) {
			for (int i = 0; i < sectionList.size(); i++) {
				String sectionEid = (String) sectionList.get(i);
				Section s = cms.getSection(sectionEid);
				SectionObject so = new SectionObject(s);
				so.setAuthorizer(userId);
				list.add(so);
			}
		}
		return list;
	}
	
	/**
	 * change collection object to list object
	 * @param c
	 * @return
	 */
	private List collectionToList(Collection c)
	{
		List rv = new Vector();
		if (c!=null)
		{
			for (Iterator i = c.iterator(); i.hasNext();)
			{
				rv.add(i.next());
			}
		}
		return rv;
	}
	

}
