/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.site.tool;

import java.io.IOException;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
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
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
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
import org.sakaiproject.entity.api.Reference;
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
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.exception.ServerOverloadException;
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
import org.sakaiproject.sitemanage.api.model.*;
import org.sakaiproject.site.util.SiteSetupQuestionFileParser;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteComparator;
import org.sakaiproject.site.util.ToolComparator;
import org.sakaiproject.sitemanage.api.SectionField;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
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
import org.sakaiproject.util.FormattedText;
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
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SiteAction.class);
	
	private ContentHostingService m_contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");

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
	
	private ContentHostingService contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
	
	private static org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService questionService = (org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService) ComponentManager
	.get(org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService.class);
	
	private static final String SITE_MODE_SITESETUP = "sitesetup";

	private static final String SITE_MODE_SITEINFO = "siteinfo";
	
	private static final String SITE_MODE_HELPER = "helper";
	
	private static final String SITE_MODE_HELPER_DONE = "helper.done";

	private static final String STATE_SITE_MODE = "site_mode";

	protected final static String[] TEMPLATE = {
			"-list",// 0
			"-type",
			"-newSiteInformation",
			"-editFeatures",
			"",
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
			"",
			"",// 20
			"",
			"",
			"",
			"",
			"",// 25
			"-modifyENW", 
			"-importSites",
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
			"",//"-siteInfo-group", // 49					moved to the group helper
			"",//"-siteInfo-groupedit", // 50				moved to the group helper
			"",//"-siteInfo-groupDeleteConfirm", // 51,		moved to the group helper
			"",
			"-findCourse", // 53
			"-questions", // 54
			"",// 55
			"",// 56
			"",// 57
			"-siteInfo-importSelection",   //58
			"-siteInfo-importMigrate",    //59
			"-importSitesMigrate"  //60
	};

	/** Name of state attribute for Site instance id */
	private static final String STATE_SITE_INSTANCE_ID = "site.instance.id";

	/** Name of state attribute for Site Information */
	private static final String STATE_SITE_INFO = "site.info";

	/** Name of state attribute for CHEF site type */
	private static final String STATE_SITE_TYPE = "site-type";

	/** Name of state attribute for possible site types */
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

	private final static String STATE_MULTIPLE_TOOL_ID_SET = "multipleToolIdSet";
	private final static String STATE_MULTIPLE_TOOL_ID_TITLE_MAP = "multipleToolIdTitleMap";
	private final static String STATE_MULTIPLE_TOOL_CONFIGURATION = "multipleToolConfiguration";

	private final static String SITE_DEFAULT_LIST = ServerConfigurationService
			.getString("site.types");

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

	private final static String FORM_URL_BASE = "form_url_base";
	
	private final static String FORM_URL_ALIAS = "form_url_alias";

	private final static String FORM_URL_ALIAS_FULL = "form_url_alias_full";

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

	/** State attribute for state initialization. */
	private static final String STATE_TEMPLATE_SITE = "site.templateSite";

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

	// types of site whose title can be editable
	public static final String TITLE_EDITABLE_SITE_TYPE = "title_editable_site_type";
	
	// maximum length of a site title
	private  static final String STATE_SITE_TITLE_MAX = "site_title_max_length";

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

	private static final String IMPORT_DATA_SOURCE = "import_data_source";

	// Special tool id for Home page
	private static final String SITE_INFORMATION_TOOL="sakai.iframe.site";

	private static final String STATE_CM_LEVELS = "site.cm.levels";
	
	private static final String STATE_CM_LEVEL_OPTS = "site.cm.level_opts";

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
	
	private static final String SITE_TEMPLATE_PREFIX = "template";
	
	private static final String STATE_TYPE_SELECTED = "state_type_selected";

	// the template index after exist the question mode
	private static final String STATE_SITE_SETUP_QUESTION_NEXT_TEMPLATE = "state_site_setup_question_next_template";
	
	// SAK-12912, the answers to site setup questions
	private static final String STATE_SITE_SETUP_QUESTION_ANSWER = "state_site_setup_question_answer";
	
	// SAK-13389, the non-official participant
	private static final String ADD_NON_OFFICIAL_PARTICIPANT = "add_non_official_participant";
	
	// the list of visited templates
	private static final String STATE_VISITED_TEMPLATES = "state_visited_templates";
	
	private String STATE_GROUP_HELPER_ID = "state_group_helper_id";

	// used in the configuration file to specify which tool attributes are configurable through WSetup tool, and what are the default value for them.
	private String CONFIG_TOOL_ATTRIBUTE = "wsetup.config.tool.attribute_";
	private String CONFIG_TOOL_ATTRIBUTE_DEFAULT = "wsetup.config.tool.attribute.default_";
	
	/**
	 * what is the main tool id within Home page?
	 * @param state
	 * @param siteType
	 * @return
	 */
	private String getHomeToolId(SessionState state)
	{
		String rv = "";
		
		String siteType = state.getAttribute(STATE_SITE_TYPE) != null? (String) state.getAttribute(STATE_SITE_TYPE):"";
		Set categories = new HashSet();
		categories.add(siteType);
		Set toolRegistrationList = ToolManager.findTools(categories, null);
		
		if (siteType.equalsIgnoreCase("myworkspace"))
		{
			// first try with the myworkspace information tool
			if (ToolManager.getTool("sakai.iframe.myworkspace") != null)
				rv = "sakai.iframe.myworkspace";
			
			if (rv.equals(""))
			{
				// try again with MOTD tool
				if (ToolManager.getTool("sakai.motd") != null)
					rv = "sakai.motd";
			}
		}
		else
		{
			// try the site information tool
			if (ToolManager.getTool("sakai.iframe.site") != null)
				rv = "sakai.iframe.site";
		}
		return rv;
	}
	
	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet,
			JetspeedRunData rundata) {
		

		// Cleanout if the helper has been asked to start afresh.
		if (state.getAttribute(SiteHelper.SITE_CREATE_START) != null) {
			cleanState(state);
			cleanStateHelper(state);
			
			// Removed from possible previous invokations.
			state.removeAttribute(SiteHelper.SITE_CREATE_START);
			state.removeAttribute(SiteHelper.SITE_CREATE_CANCELLED);
			state.removeAttribute(SiteHelper.SITE_CREATE_SITE_ID);
			
		}
		
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
		
		if (ServerConfigurationService.getStrings("titleEditableSiteType") != null) {
			state.setAttribute(TITLE_EDITABLE_SITE_TYPE, new ArrayList(Arrays
					.asList(ServerConfigurationService
							.getStrings("titleEditableSiteType"))));
		} else {
			state.setAttribute(TITLE_EDITABLE_SITE_TYPE, new Vector());
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

		if (state.getAttribute(STATE_SITE_MODE) == null) {
				// get site tool mode from tool registry
				String site_mode = config.getInitParameter(STATE_SITE_MODE);
		 
				// When in helper mode we don't have 
				if (site_mode == null) {
					site_mode = SITE_MODE_HELPER;
				}
	
				state.setAttribute(STATE_SITE_MODE, site_mode);
			}


		
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
		state.removeAttribute(FORM_ADDITIONAL); // don't we need to clean this
		// too? -daisyf
		state.removeAttribute(STATE_TEMPLATE_SITE);
		state.removeAttribute(STATE_TYPE_SELECTED);
		state.removeAttribute(STATE_SITE_SETUP_QUESTION_ANSWER);					
		state.removeAttribute(STATE_SITE_SETUP_QUESTION_NEXT_TEMPLATE);

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
		rb = new ResourceLoader("sitesetupgeneric");
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
		
		String indexString = (String) state.getAttribute(STATE_TEMPLATE_INDEX);

		// update the visited template list with the current template index
		addIntoStateVisitedTemplates(state, indexString);
		
		template = buildContextForTemplate(getPrevVisitedTemplate(state), Integer.valueOf(indexString), portlet, context, data, state);
		return template;

	} // buildMainPanelContext

	/**
	 * add index into the visited template indices list
	 * @param state
	 * @param index
	 */
	private void addIntoStateVisitedTemplates(SessionState state, String index) {
		List<String> templateIndices = (List<String>) state.getAttribute(STATE_VISITED_TEMPLATES);
		if (templateIndices.size() == 0 || !templateIndices.contains(index))
		{
			// this is to prevent from page refreshing accidentally updates the list
			templateIndices.add(index);
			state.setAttribute(STATE_VISITED_TEMPLATES, templateIndices);
		}
	}
	
	/**
	 * remove the last index
	 * @param state
	 */
	private void removeLastIndexInStateVisitedTemplates(SessionState state) {
		List<String> templateIndices = (List<String>) state.getAttribute(STATE_VISITED_TEMPLATES);
		if (templateIndices!=null && templateIndices.size() > 0)
		{
			// this is to prevent from page refreshing accidentally updates the list
			templateIndices.remove(templateIndices.size()-1);
			state.setAttribute(STATE_VISITED_TEMPLATES, templateIndices);
		}
	}
	
	private String getPrevVisitedTemplate(SessionState state) {
		List<String> templateIndices = (List<String>) state.getAttribute(STATE_VISITED_TEMPLATES);
		if (templateIndices != null && templateIndices.size() >1 )
		{
			return templateIndices.get(templateIndices.size()-2);
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * whether template indexed has been visited
	 * @param state
	 * @param templateIndex
	 * @return
	 */
	private boolean isTemplateVisited(SessionState state, String templateIndex)
	{
		boolean rv = false;
		List<String> templateIndices = (List<String>) state.getAttribute(STATE_VISITED_TEMPLATES);
		if (templateIndices != null && templateIndices.size() >0 )
		{
			rv = templateIndices.contains(templateIndex);
		}
		return rv;
	}

	/**
	 * Build the context for each template using template_index parameter passed
	 * in a form hidden field. Each case is associated with a template. (Not all
	 * templates implemented). See String[] TEMPLATES.
	 * 
	 * @param index
	 *            is the number contained in the template's template_index
	 */

	private String buildContextForTemplate(String preIndex, int index, VelocityPortlet portlet,
			Context context, RunData data, SessionState state) {
		String realmId = "";
		String site_type = "";
		String sortedBy = "";
		String sortedAsc = "";
		ParameterParser params = data.getParameters();
		context.put("tlang", rb);
		context.put("alertMessage", state.getAttribute(STATE_MESSAGE));
		
		// the last visited template index
		if (preIndex != null)
			context.put("backIndex", preIndex);
		
		context.put("templateIndex", String.valueOf(index));
		
		
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
		
		// get alias base path
		String aliasBaseUrl = ServerConfigurationService.getPortalUrl() + Entity.SEPARATOR + "site" + Entity.SEPARATOR;

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
						M_log.warn(this + "buildContextForTemplate chef_site-list.vm list GradToolsStudent sites", e);
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
					M_log.warn(this + "buildContextForTemplate chef_site-type.vm ", e);
				}
			} else {
				context.put("withDissertation", Boolean.FALSE);
			}

			List types = (List) state.getAttribute(STATE_SITE_TYPES);
			context.put("siteTypes", types);

			// put selected/default site type into context
			String typeSelected = (String) state.getAttribute(STATE_TYPE_SELECTED);
			context.put("typeSelected", state.getAttribute(STATE_TYPE_SELECTED) != null?state.getAttribute(STATE_TYPE_SELECTED):types.get(0));
			
			setTermListForContext(context, state, true); // true => only
			
			// upcoming terms
			setSelectedTermForContext(context, state, STATE_TERM_SELECTED);
			
			// template site - Denny
			setTemplateListForContext(context, state);
			
			return (String) getContext(data).get("template") + TEMPLATE[1];

		case 2:
			/*
			 * buildContextForTemplate chef_site-newSiteInformation.vm
			 * 
			 */
			context.put("siteTypes", state.getAttribute(STATE_SITE_TYPES));
			String siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			context.put("type", siteType);
			context.put("siteTitleEditable", Boolean.valueOf(siteTitleEditable(state, siteType)));

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
				}

				List<SectionObject> cmAuthorizerSectionList = (List<SectionObject>) state
						.getAttribute(STATE_CM_AUTHORIZER_SECTIONS);
				if (cmAuthorizerSectionList != null) {
					context
							.put("cmAuthorizerSections",
									cmAuthorizerSectionList);
				}

				if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
					int number = ((Integer) state
							.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER))
							.intValue();
					context.put("manualAddNumber", new Integer(number - 1));
					context.put("manualAddFields", state
							.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
				} else {
					if (courseManagementIsImplemented()) {
					} else {
						context.put("templateIndex", "37");
					}
				}

				// whether to show course skin selection choices or not
				courseSkinSelection(context, state, null, siteInfo);
				
			} else {
				context.put("isCourseSite", Boolean.FALSE);
				if (siteType.equalsIgnoreCase("project")) {
					context.put("isProjectSite", Boolean.TRUE);
				}

				if (StringUtil.trimToNull(siteInfo.iconUrl) != null) {
					context.put(FORM_ICON_URL, siteInfo.iconUrl);
				}
			}

			if (state.getAttribute(SiteHelper.SITE_CREATE_SITE_TITLE) != null) {
				context.put("titleEditableSiteType", Boolean.FALSE);
				siteInfo.title = (String)state.getAttribute(SiteHelper.SITE_CREATE_SITE_TITLE);
			} else {
				context.put("titleEditableSiteType", state
						.getAttribute(TITLE_EDITABLE_SITE_TYPE));
			}
			context.put(FORM_TITLE, siteInfo.title);
			context.put(FORM_URL_BASE, aliasBaseUrl);
			context.put(FORM_URL_ALIAS, siteInfo.url_alias);
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
			 * buildContextForTemplate chef_site-editFeatures.vm
			 * 
			 */
			String type = (String) state.getAttribute(STATE_SITE_TYPE);
			if (type != null && type.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("isProjectSite", Boolean.FALSE);
			} else {
				context.put("isCourseSite", Boolean.FALSE);
				if (type.equalsIgnoreCase("project")) {
					context.put("isProjectSite", Boolean.TRUE);
				}
			}
			
			List requiredTools = ServerConfigurationService.getToolsRequired(type);
			// look for legacy "home" tool
			context.put("defaultTools", replaceHomeToolId(state, requiredTools));

			toolRegistrationSelectedList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			// If this is the first time through, check for tools
			// which should be selected by default.
			List defaultSelectedTools = ServerConfigurationService.getDefaultTools(type);
			defaultSelectedTools = replaceHomeToolId(state, defaultSelectedTools);
			if (toolRegistrationSelectedList == null) {
				toolRegistrationSelectedList = new Vector(defaultSelectedTools);
			}
			context.put(STATE_TOOL_REGISTRATION_SELECTED_LIST, toolRegistrationSelectedList);

			boolean myworkspace_site = false;
			// Put up tool lists filtered by category
			List siteTypes = (List) state.getAttribute(STATE_SITE_TYPES);
			if (siteTypes.contains(type)) {
				myworkspace_site = false;
			}
			if (site != null && SiteService.isUserSite(site.getId())
					|| (type != null && type.equalsIgnoreCase("myworkspace"))) {
				myworkspace_site = true;
				type = "myworkspace";
			}
			context.put("myworkspace_site", new Boolean(myworkspace_site));
			
			context.put(STATE_TOOL_REGISTRATION_LIST, state.getAttribute(STATE_TOOL_REGISTRATION_LIST));
			
			// titles for multiple tool instances
			context.put(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP ));

			// The Home tool checkbox needs special treatment to be selected
			// by
			// default.
			Boolean checkHome = (Boolean) state.getAttribute(STATE_TOOL_HOME_SELECTED);
			if (checkHome == null) {
				if ((defaultSelectedTools != null)
						&& defaultSelectedTools.contains(getHomeToolId(state))) {
					checkHome = Boolean.TRUE;
				}
			}
			context.put("check_home", checkHome);
			
			// get the email alias when an Email Archive tool has been selected
			String channelReference = site!=null?mailArchiveChannelReference(site.getId()):"";
			List aliases = AliasService.getAliases(channelReference, 1, 1);
			if (aliases.size() > 0) {
				state.setAttribute(STATE_TOOL_EMAIL_ADDRESS, ((Alias) aliases
						.get(0)).getId());
			}
			
			if (state.getAttribute(STATE_TOOL_EMAIL_ADDRESS) != null) {
				context.put("emailId", state
						.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			}
			context.put("serverName", ServerConfigurationService
					.getServerName());
			
			context.put("sites", SiteService.getSites(
					org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
					null, null, null, SortType.TITLE_ASC, null));
			context.put("import", state.getAttribute(STATE_IMPORT));
			context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));
			
			if (site != null)
			{
				context.put("SiteTitle", site.getTitle());
				context.put("existSite", Boolean.TRUE);
				context.put("backIndex", "12");	// back to site info list page
			}
			else
			{
				context.put("existSite", Boolean.FALSE);
				context.put("backIndex", "2");	// back to new site information page
			}

			context.put("homeToolId", getHomeToolId(state));
			
			return (String) getContext(data).get("template") + TEMPLATE[3];
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
			String pickerAction = ServerConfigurationService.getString("officialAccountPickerAction");
			if (pickerAction != null && !"".equals(pickerAction))
			{
				context.put("hasPickerDefined", Boolean.TRUE);
				context.put("officialAccountPickerLabel", ServerConfigurationService
					.getString("officialAccountPickerLabel"));
				context.put("officialAccountPickerAction", pickerAction);
			}
			if (state.getAttribute("officialAccountValue") != null) {
				context.put("officialAccountValue", (String) state
						.getAttribute("officialAccountValue"));
			}
			
			// whether to show the non-official participant section or not
			String addNonOfficialParticipant = (String) state.getAttribute(ADD_NON_OFFICIAL_PARTICIPANT);
			if (addNonOfficialParticipant != null)
			{
				if (addNonOfficialParticipant.equalsIgnoreCase("true"))
				{
					context.put("nonOfficialAccount", Boolean.TRUE);
					context.put("nonOfficialAccountSectionTitle", ServerConfigurationService
							.getString("nonOfficialAccountSectionTitle"));
					context.put("nonOfficialAccountName", ServerConfigurationService
							.getString("nonOfficialAccountName"));
					context.put("nonOfficialAccountLabel", ServerConfigurationService
							.getString("nonOfficialAccountLabel"));
					if (state.getAttribute("nonOfficialAccountValue") != null) {
						context.put("nonOfficialAccountValue", (String) state
								.getAttribute("nonOfficialAccountValue"));
					}
				}
				else
				{
					context.put("nonOfficialAccount", Boolean.FALSE);
				}
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
							M_log.warn(this + "buildContextForTemplate chef_site-siteDeleteConfirm.vm - IdUnusedException " + id, e);
							addAlert(state, rb.getString("java.sitewith") + " "
									+ id + " " + rb.getString("java.couldnt")
									+ " ");
						}
						if (SiteService.allowRemoveSite(id)) {
							try {
								Site removeSite = SiteService.getSite(id);
								remove.add(removeSite);
							} catch (IdUnusedException e) {
								M_log.warn(this + ".buildContextForTemplate chef_site-siteDeleteConfirm.vm: IdUnusedException", e);
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
				context.put("disableCourseSelection", ServerConfigurationService.getString("disable.course.site.skin.selection", "false").equals("true")?Boolean.TRUE:Boolean.FALSE);
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
			
			if (StringUtil.trimToNull(siteInfo.getUrlAlias()) != null) {
				String urlAliasFull = aliasBaseUrl + siteInfo.getUrlAlias();
				context.put(FORM_URL_ALIAS_FULL, urlAliasFull);
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
					.getAttribute(STATE_TOOL_REGISTRATION_LIST)); // %%% use Tool
			
			// all info related to multiple tools
			multipleToolIntoContext(context, state);
			
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
				
				if (site.getProviderGroupId() != null) {
					M_log.debug("site has provider");
					context.put("hasProviderSet", Boolean.TRUE);
				} else {
					M_log.debug("site has no provider");
					context.put("hasProviderSet", Boolean.FALSE);
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
						
						// in particular, need to check site types for showing the tool or not
						if (isPageOrderAllowed(siteType))
						{
							b.add(new MenuEntry(rb.getString("java.orderpages"), "doPageOrderHelper"));
						}
						
					}
					
				}

				if (allowUpdateSiteMembership) 
				{
					// show add participant menu
					if (!isMyWorkspace) {
						// if the add participant helper is available, not
						// stealthed and not hidden, show the link
						if (notStealthOrHiddenTool("sakai-site-manage-participant-helper")) {
							b.add(new MenuEntry(rb.getString("java.addp"),
									"doParticipantHelper"));
						}
						
						// show the Edit Class Roster menu
						if (ServerConfigurationService.getBoolean("site.setup.allow.editRoster", true) && siteType != null && siteType.equals((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
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
						// if the manage group helper is available, not
						// stealthed and not hidden, show the link
						// read the helper name from configuration variable: wsetup.group.helper.name
						// the default value is: "sakai-site-manage-group-section-role-helper"
						// the older version of group helper which is not section/role aware is named:"sakai-site-manage-group-helper"
						String groupHelper = ServerConfigurationService.getString("wsetup.group.helper.name", "sakai-site-manage-group-section-role-helper");
						if (setHelper("wsetup.groupHelper", groupHelper, state, STATE_GROUP_HELPER_ID)) {
							b.add(new MenuEntry(rb.getString("java.group"),
									"doManageGroupHelper"));
						}
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
								//a configuration param for showing/hiding Import From Site with Clean Up
								String importFromSite = ServerConfigurationService.getString("clean.import.site",Boolean.TRUE.toString());
								if (importFromSite.equalsIgnoreCase("true")) {
									b.add(new MenuEntry(
										rb.getString("java.import"),
										"doMenu_siteInfo_importSelection"));
								}
								else {
									b.add(new MenuEntry(
										rb.getString("java.import"),
										"doMenu_siteInfo_import"));
								}
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
						state.setAttribute(SORTED_BY, SiteConstants.SORTED_BY_PARTICIPANT_NAME);
						sortedBy = SiteConstants.SORTED_BY_PARTICIPANT_NAME;
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
				M_log.warn(this + " buildContextForTemplate chef_site-siteInfo-list.vm ", e);
			}

			roles = getRoles(state);
			context.put("roles", roles);

			// will have the choice to active/inactive user or not
			String activeInactiveUser = ServerConfigurationService.getString(
					"activeInactiveUser", Boolean.FALSE.toString());
			if (activeInactiveUser.equalsIgnoreCase("true")) {
				context.put("activeInactiveUser", Boolean.TRUE);
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
			context.put("siteTitleEditable", Boolean.valueOf(siteTitleEditable(state, site.getType())));
			context.put("type", site.getType());
			context.put("titleMaxLength", state.getAttribute(STATE_SITE_TITLE_MAX));

			siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (siteType != null && siteType.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				context.put("isCourseSite", Boolean.TRUE);
				
				// whether to show course skin selection choices or not
				courseSkinSelection(context, state, site, null);

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

			return (String) getContext(data).get("template") + TEMPLATE[13];
		case 14:
			/*
			 * buildContextForTemplate chef_site-siteInfo-editInfoConfirm.vm
			 * 
			 */
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			siteProperties = site.getProperties();
			siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (siteType != null && siteType.equalsIgnoreCase((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("disableCourseSelection", ServerConfigurationService.getString("disable.course.site.skin.selection", "false").equals("true")?Boolean.TRUE:Boolean.FALSE);
				context.put("siteTerm", state.getAttribute(FORM_SITEINFO_TERM));
			} else {
				context.put("isCourseSite", Boolean.FALSE);
			}
			context.put("oTitle", site.getTitle());
			context.put("title", siteInfo.title);

			context.put("description", siteInfo.description);
			context.put("oDescription", site.getDescription());
			context.put("short_description", siteInfo.short_description);
			context.put("oShort_description", site.getShortDescription());
			context.put("skin", siteInfo.iconUrl);
			context.put("oSkin", site.getIconUrl());
			context.put("skins", state.getAttribute(STATE_ICONS));
			context.put("oIcon", site.getIconUrl());
			context.put("icon", siteInfo.iconUrl);
			context.put("include", siteInfo.include);
			context.put("oInclude", Boolean.valueOf(site.isPubView()));
			context.put("name", siteInfo.site_contact_name);
			context.put("oName", siteProperties.getProperty(PROP_SITE_CONTACT_NAME));
			context.put("email", siteInfo.site_contact_email);
			context.put("oEmail", siteProperties.getProperty(PROP_SITE_CONTACT_EMAIL));

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
			context.put("selectedTools", orderToolIds(state, checkNullSiteType(state, site), (List) state
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
			
			// all info related to multiple tools
			multipleToolIntoContext(context, state);

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
				
				// the template site, if using one
				Site templateSite = (Site) state.getAttribute(STATE_TEMPLATE_SITE);			

				// use the type's template, if defined
				String realmTemplate = "!site.template";
				// if create based on template, use the roles from the template
				if (templateSite != null) {
					realmTemplate = SiteService.siteReference(templateSite.getId());
				} else if (siteInfo.site_type != null) {
					realmTemplate = realmTemplate + "." + siteInfo.site_type;
				}
				try {
					AuthzGroup r = AuthzGroupService.getAuthzGroup(realmTemplate);
					context.put("roles", r.getRoles());
				} catch (GroupNotDefinedException e) {
					try {
						AuthzGroup rr = AuthzGroupService.getAuthzGroup("!site.template");
						context.put("roles", rr.getRoles());
					} catch (GroupNotDefinedException ee) {
					}
				}

				// new site, go to confirmation page
				context.put("continue", "10");

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
				context.put("continue", "15");
			} else {
				// new site
				context.put("existingSite", Boolean.FALSE);
				context.put("continue", "18");
			}

			context.put("function", "eventSubmit_doAdd_features");

			context.put(STATE_TOOL_REGISTRATION_LIST, state
					.getAttribute(STATE_TOOL_REGISTRATION_LIST));
			toolRegistrationSelectedList = (List) state
					.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			context.put(STATE_TOOL_REGISTRATION_SELECTED_LIST,
					toolRegistrationSelectedList); // String toolId's
			
			// all info related to multiple tools
			multipleToolIntoContext(context, state);
			
			context.put("toolManager", ToolManager.getInstance());
			String emailId = (String) state.getAttribute(STATE_TOOL_EMAIL_ADDRESS);
			if (emailId != null) {
				context.put("emailId", emailId);
			}

			context.put("serverName", ServerConfigurationService
					.getServerName());

			context.put("oldSelectedTools", state
					.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST));

			context.put("homeToolId", getHomeToolId(state));
			
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
				context.put("totalSteps", "2");
				context.put("step", "2");
				context.put("currentSite", site);
			} else {
				// new site, go to edit access page
				if (fromENWModifyView(state)) {
					context.put("continue", "26");
				} else {
					context.put("continue", "18");
				}
			}
			context.put("existingSite", Boolean.valueOf(existingSite));
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
		case 60:
			/*
			 * buildContextForTemplate chef_site-importSitesMigrate.vm
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

			// get the tool id list
			List<String> toolIdList = new Vector<String>();
			if (existingSite)
			{
				// list all site tools which are displayed on its own page
				List<SitePage> sitePages = site.getPages();
				if (sitePages != null)
				{
					for (SitePage page: sitePages)
					{
						List<ToolConfiguration> pageToolsList = page.getTools(0);
						// we only handle one tool per page case
						if ( page.getLayout() == SitePage.LAYOUT_SINGLE_COL && pageToolsList.size() == 1)
						{
							toolIdList.add(pageToolsList.get(0).getToolId());
						}
					}
				}
			}
			else
			{
				// during site creation
				toolIdList = (List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			}
			state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, toolIdList);
			// order it
			SortedIterator iToolIdList = new SortedIterator(getToolsAvailableForImport(state, toolIdList).iterator(),new ToolComparator());
			Hashtable<String, String> toolTitleTable = new Hashtable<String, String>();
			for(;iToolIdList.hasNext();)
			{
				String toolId = (String) iToolIdList.next();
				try
				{
					String toolTitle = ToolManager.getTool(toolId).getTitle();
					toolTitleTable.put(toolId, toolTitle);
				}
				catch (Exception e)
				{
					Log.info("chef", this + " buildContexForTemplate case 60: cannot get tool title for " + toolId + e.getMessage()); 
				}
			}
			context.put("selectedTools", toolTitleTable); // String toolId's
			context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));
			context.put("importSitesTools", state
					.getAttribute(STATE_IMPORT_SITE_TOOL));
			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));
			context.put("importSupportedTools", importTools());

			return (String) getContext(data).get("template") + TEMPLATE[60];		
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
		case 58:
			/*
			 * buildContextForTemplate chef_siteinfo-importSelection.vm
			 * 
			 */
			context.put("currentSite", site);
			context.put("importSiteList", state
					.getAttribute(STATE_IMPORT_SITES));
			context.put("sites", SiteService.getSites(
					org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
					null, null, null, SortType.TITLE_ASC, null));
			return (String) getContext(data).get("template") + TEMPLATE[58];
		case 59:
			/*
			 * buildContextForTemplate chef_siteinfo-importMigrate.vm
			 * 
			 */
			context.put("currentSite", site);
			context.put("importSiteList", state
					.getAttribute(STATE_IMPORT_SITES));
			context.put("sites", SiteService.getSites(
					org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
					null, null, null, SortType.TITLE_ASC, null));
			return (String) getContext(data).get("template") + TEMPLATE[59];

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
				if (state.getAttribute(STATE_AUTO_ADD) != null) {
					context.put("autoAdd", Boolean.TRUE);
				}
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
		// case 49, 50, 51 have been implemented in helper mode
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
			
			int cmLevelSize = 0;

			if (cms == null || !courseManagementIsImplemented()
					|| cmLevels == null || cmLevels.size() < 1) {
				// TODO: redirect to manual entry: case #37
			} else {
				cmLevelSize = cmLevels.size();
				Object levelOpts[] = state.getAttribute(STATE_CM_LEVEL_OPTS) == null?new Object[cmLevelSize]:(Object[])state.getAttribute(STATE_CM_LEVEL_OPTS);
				int numSelections = 0;

				if (selections != null)
				{
					numSelections = selections.size();

					// execution will fall through these statements based on number of selections already made
					if (numSelections == cmLevelSize - 1)
					{
						levelOpts[numSelections] = getCMSections((String) selections.get(numSelections-1));
					}
					else if (numSelections == cmLevelSize - 2)
					{
						levelOpts[numSelections] = getCMCourseOfferings((String) selections.get(numSelections-1), t.getEid());
					}
					else if (numSelections < cmLevelSize)
					{
						levelOpts[numSelections] = sortCmObject(cms.findCourseSets((String) cmLevels.get(numSelections==0?0:numSelections-1)));
					}
					// always set the top level 
					levelOpts[0] = sortCmObject(cms.findCourseSets((String) cmLevels.get(0)));
					// clean further element inside the array
					for (int i = numSelections + 1; i<cmLevelSize; i++)
					{
						levelOpts[i] = null;
					}
				}

				context.put("cmLevelOptions", Arrays.asList(levelOpts));
				state.setAttribute(STATE_CM_LEVEL_OPTS, levelOpts);
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
		case 54:
			/*
			 * build context for chef_site-questions.vm
			 */
			SiteTypeQuestions siteTypeQuestions = questionService.getSiteTypeQuestions((String) state.getAttribute(STATE_SITE_TYPE));
			if (siteTypeQuestions != null)
			{
				context.put("questionSet", siteTypeQuestions);
				context.put("userAnswers", state.getAttribute(STATE_SITE_SETUP_QUESTION_ANSWER));
			}
			context.put("continueIndex", state.getAttribute(STATE_SITE_SETUP_QUESTION_NEXT_TEMPLATE));
			return (String) getContext(data).get("template") + TEMPLATE[54];

		}
		// should never be reached
		return (String) getContext(data).get("template") + TEMPLATE[0];

	}

	/**
	 * just in case there is still a notion of "home" for Home tool
	 * change it to more proper home tool id
	 * @param state
	 * @param toolIdList
	 * @return
	 */
	private List replaceHomeToolId(SessionState state, List toolIdList) {
		if (toolIdList != null && toolIdList.contains("home"))
		{
			toolIdList.remove("home");
			toolIdList.add(getHomeToolId(state));
		}
		return toolIdList;
	} // replaceHomeToolId

	/**
	 * whether the PageOrderHelper is allowed to be shown in this site type
	 * @param siteType
	 * @return
	 */
	private boolean isPageOrderAllowed(String siteType) {
		boolean rv = true;
		String hidePageOrderSiteTypes = ServerConfigurationService.getString("hide.pageorder.site.types", "");
		if ( hidePageOrderSiteTypes.length() != 0)
		{
			if (new ArrayList<String>(Arrays.asList(StringUtil.split(hidePageOrderSiteTypes, ","))).contains(siteType))
			{
				rv = false;
			}
		}
		return rv;
	}

	private void multipleToolIntoContext(Context context, SessionState state) {
		// titles for multiple tool instances
		context.put(STATE_MULTIPLE_TOOL_ID_SET, state.getAttribute(STATE_MULTIPLE_TOOL_ID_SET ));
		context.put(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP ));
		context.put(STATE_MULTIPLE_TOOL_CONFIGURATION, state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION));
	}


	/**
	 * show course site skin selection or not
	 * @param context
	 * @param state
	 * @param site
	 * @param siteInfo
	 */
	private void courseSkinSelection(Context context, SessionState state, Site site, SiteInfo siteInfo) {
		// Display of appearance icon/url list with course site based on
		// "disable.course.site.skin.selection" value set with
		// sakai.properties file.
		// The setting defaults to be false.
		context.put("disableCourseSelection", ServerConfigurationService.getString("disable.course.site.skin.selection", "false").equals("true")?Boolean.TRUE:Boolean.FALSE);
		context.put("skins", state.getAttribute(STATE_ICONS));
		if (state.getAttribute(FORM_SITEINFO_SKIN) != null) {
			context.put("selectedIcon", state.getAttribute(FORM_SITEINFO_SKIN));
		} else 
		{
			if (site != null && site.getIconUrl() != null) 
			{
				context.put("selectedIcon", site.getIconUrl());
			}
			else if (siteInfo != null && StringUtil.trimToNull(siteInfo.getIconUrl()) != null) 
			{
				context.put("selectedIcon", siteInfo.getIconUrl());
			}
		}
	}

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
	
	/**
	 * Launch the participant Helper Tool -- for adding participant
	 * 
	 * @see case 12
	 * 
	 */
	public void doParticipantHelper(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// pass in the siteId of the site to be ordered (so it can configure
		// sites other then the current site)
		SessionManager.getCurrentToolSession().setAttribute(
				HELPER_ID + ".siteId", ((Site) getStateSite(state)).getId());

		// launch the helper
		startHelper(data.getRequest(), "sakai-site-manage-participant-helper");
	}
	
	/**
	 * Launch the Manage Group helper Tool -- for adding, editing and deleting groups
	 * 
	 */
	public void doManageGroupHelper(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		

		// pass in the siteId of the site to be ordered (so it can configure
		// sites other then the current site)
		SessionManager.getCurrentToolSession().setAttribute(
				HELPER_ID + ".siteId", ((Site) getStateSite(state)).getId());

		// launch the helper
		startHelper(data.getRequest(), (String) state.getAttribute(STATE_GROUP_HELPER_ID));//"sakai-site-manage-group-helper");
		
	}
	
	public boolean setHelper(String helperName, String defaultHelperId, SessionState state, String stateHelperString)
	{
		String helperId = ServerConfigurationService.getString(helperName, defaultHelperId);
		
		// if the state variable regarding the helper is not set yet, set it with the configured helper id
		if (state.getAttribute(stateHelperString) == null)
		{
			state.setAttribute(stateHelperString, helperId);
		}
		if (notStealthOrHiddenTool(helperId)) {
			return true;
		}
		return false;
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
			M_log.warn(this + ".doUpload_Mtrl_Frm_File: wrong setting of content.upload.max = " + max_file_size_mb, e);
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
					addAlert(state, rb.getString("importFile.invalidfile"));
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
		List providerCourseList = SiteParticipantHelper.getProviderCourseList((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
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
					M_log.warn(this + ".coursesIntoContext " + e.getMessage() + " sectionId=" + sectionId, e);
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
						M_log.warn(this + ".courseListFromStringIntoContext: cannot find section " + courseEid, e);
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
					M_log.warn(this + "sizeResources, template index = 0: Cannot find user "
							+ SessionManager.getCurrentSessionUserId()
							+ "'s My Workspace site.", e);
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
					M_log.warn(this + "readResourcesPage template index = 0 :Cannot find user " + SessionManager.getCurrentSessionUserId() + "'s My Workspace site.", e);
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
					|| isMultipleInstancesAllowed(findOriginalToolId(state, toolId))) {
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
		if (siteTypes != null) 
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "1");
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
					M_log.warn(this + ".doSite_delete_confirmed - IdUnusedException " + id, e);
					addAlert(state, rb.getString("java.sitewith") + " " + id
							+ " " + rb.getString("java.couldnt") + " ");
				}
				if (SiteService.allowRemoveSite(id)) {

					try {
						Site site = SiteService.getSite(id);
						site_title = site.getTitle();
						SiteService.removeSite(site);
					} catch (IdUnusedException e) {
						M_log.warn(this +".doSite_delete_confirmed - IdUnusedException " + id, e);
						addAlert(state, rb.getString("java.sitewith") + " "
								+ site_title + "(" + id + ") "
								+ rb.getString("java.couldnt") + " ");
					} catch (PermissionException e) {
						M_log.warn(this + ".doSite_delete_confirmed -  PermissionException, site " + site_title + "(" + id + ").", e);
						addAlert(state, site_title + " "
								+ rb.getString("java.dontperm") + " ");
					}
				} else {
					M_log.warn(this + ".doSite_delete_confirmed -  allowRemoveSite failed for site "+ id);
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

			state.setAttribute(SORTED_BY, SiteConstants.SORTED_BY_PARTICIPANT_NAME);
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
		int index = Integer.valueOf(params.getString("templateIndex"))
				.intValue();
		actionForTemplate("continue", index, params, state);

		String type = StringUtil.trimToNull(params.getString("itemType"));

		int totalSteps = 0;

		if (type == null) {
			addAlert(state, rb.getString("java.select") + " ");
		} else {
			state.setAttribute(STATE_TYPE_SELECTED, type);
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
			// get the user selected template
			getSelectedTemplate(state, params, type);
		}

		if (state.getAttribute(SITE_CREATE_TOTAL_STEPS) == null) {
			state.setAttribute(SITE_CREATE_TOTAL_STEPS, new Integer(totalSteps));
		}

		if (state.getAttribute(SITE_CREATE_CURRENT_STEP) == null) {
			state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer(1));
		}
		
		redirectToQuestionVM(state, type);

	} // doSite_type
	
	/**
	 * see whether user selected any template
	 * @param state
	 * @param params
	 * @param type
	 */
	private void getSelectedTemplate(SessionState state,
			ParameterParser params, String type) {
		String templateSiteId = params.getString("selectTemplate" + type);
		if (templateSiteId != null)
		{
			Site templateSite = null;
			try
			{
				templateSite = SiteService.getSite(templateSiteId);
				// save the template site in state
				state.setAttribute(STATE_TEMPLATE_SITE, templateSite);
			     
				// the new site type is based on the template site
				setNewSiteType(state, templateSite.getType());
			}catch (Exception e) {  
				// should never happened, as the list of templates are generated
				// from existing sites
				M_log.warn(this + ".doSite_type" + e.getClass().getName(), e);
			}
			
			// grab site info from template
			SiteInfo siteInfo = new SiteInfo();
			if (state.getAttribute(STATE_SITE_INFO) != null) {
				siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			}
			
			// copy information from template site
			siteInfo.description = templateSite.getDescription();
			siteInfo.short_description = templateSite.getShortDescription();
			siteInfo.iconUrl = templateSite.getIconUrl();
			siteInfo.infoUrl = templateSite.getInfoUrl();
			siteInfo.joinable = templateSite.isJoinable();
			siteInfo.joinerRole = templateSite.getJoinerRole();
			//siteInfo.include = false;
			
			List<String> toolIdsSelected = new Vector<String>();
			List pageList = templateSite.getPages();
			if (!((pageList == null) || (pageList.size() == 0))) {
				for (ListIterator i = pageList.listIterator(); i.hasNext();) {
					SitePage page = (SitePage) i.next();

					List pageToolList = page.getTools();
					if (pageToolList != null && pageToolList.size() > 0)
					{
						Tool tConfig = ((ToolConfiguration) pageToolList.get(0)).getTool();
						if (tConfig != null)
						{
							toolIdsSelected.add(tConfig.getId());
						}
					}
				}
			}
			state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, toolIdsSelected);
			state.setAttribute(STATE_SITE_INFO, siteInfo);
		}
	}

	/**
	 * Depend on the setup question setting, redirect the site setup flow
	 * @param state
	 * @param type
	 */
	private void redirectToQuestionVM(SessionState state, String type) {
		// SAK-12912: check whether there is any setup question defined
		SiteTypeQuestions siteTypeQuestions = questionService.getSiteTypeQuestions(type);
		if (siteTypeQuestions != null)
		{
			List questionList = siteTypeQuestions.getQuestions();
			if (questionList != null && !questionList.isEmpty())
			{
				// there is at least one question defined for this type
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					state.setAttribute(STATE_SITE_SETUP_QUESTION_NEXT_TEMPLATE, state.getAttribute(STATE_TEMPLATE_INDEX));
					state.setAttribute(STATE_TEMPLATE_INDEX, "54");
				}
			}
		}
	}

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

		if (t.getStartDate() != null && c.getTimeInMillis() < t.getStartDate().getTime()) {
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
							String eid = StringUtil.trimToZero((String) iInstructors.next());
							try {
								UserDirectoryService.getUserByEid(eid);
							} catch (UserNotDefinedException e) {
								addAlert(
										state,
										rb.getString("java.validAuthor1")
												+ " "
												+ ServerConfigurationService
														.getString("officialAccountName")
												+ " "
												+ rb.getString("java.validAuthor2"));
								M_log.warn(this + ".doManual_add_course: cannot find user with eid=" + eid, e);
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
			// if create based on template, skip the feature selection
			Site templateSite = (Site) state.getAttribute(STATE_TEMPLATE_SITE);
			if (templateSite != null) 
			{
				state.setAttribute(STATE_TEMPLATE_INDEX, "18");
			}
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
				// input invalid
				validInput = false;
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
			String title = sectionFieldProvider.getSectionTitle(t.getEid(), (List) multiCourseInputs.get(0));
			try {
				title = cms.getSection(sectionEid).getTitle();
			} catch (IdNotFoundException e) {
				// cannot find section, use the default title 
				M_log.warn(this + ":readCourseSectionInfo: cannot find section with eid=" + sectionEid);
			}
			siteInfo.title = title;
		}
		state.setAttribute(STATE_SITE_INFO, siteInfo);

	} // readCourseSectionInfo

	/**
	 * 
	 * @param state
	 * @param type
	 */
	private void setNewSiteType(SessionState state, String type) {
		state.setAttribute(STATE_SITE_TYPE, type);
		
		// start out with fresh site information
		SiteInfo siteInfo = new SiteInfo();
		siteInfo.site_type = type;
		siteInfo.published = true;
		state.setAttribute(STATE_SITE_INFO, siteInfo);

		// set tool registration list
		setToolRegistrationList(state, type);
	}

	/**
	 * Set the state variables for tool registration list basd on site type
	 * @param state
	 * @param type
	 */
	private void setToolRegistrationList(SessionState state, String type) {
		state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME);
		state.removeAttribute(STATE_MULTIPLE_TOOL_ID_SET);
		
		// get the tool id set which allows for multiple instances
		Set multipleToolIdSet = new HashSet();
		Hashtable multipleToolConfiguration = new Hashtable<String, Hashtable<String, String>>();
		// get registered tools list
		Set categories = new HashSet();
		categories.add(type);
		Set toolRegistrations = ToolManager.findTools(categories, null);
		if ((toolRegistrations == null || toolRegistrations.size() == 0)
			&& state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null)
		{
			// use default site type and try getting tools again
			type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);
			categories.clear();
			categories.add(type);
			toolRegistrations = ToolManager.findTools(categories, null);
		}

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
			
			String originalToolId = findOriginalToolId(state, tr.getId());
			if (isMultipleInstancesAllowed(originalToolId))
			{
				// of a tool which allows multiple instances
				multipleToolIdSet.add(tr.getId());
				
				// get the configuration for multiple instance
				Hashtable<String, String> toolConfigurations = getMultiToolConfiguration(originalToolId);
				multipleToolConfiguration.put(tr.getId(), toolConfigurations);
			}
		}
		
		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, tools);
		state.setAttribute(STATE_MULTIPLE_TOOL_ID_SET, multipleToolIdSet);
		state.setAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION, multipleToolConfiguration);
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
		int index = Integer.valueOf(params.getString("templateIndex"))
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
		
		// remove the last template index from the list
		removeLastIndexInStateVisitedTemplates(state);

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
			int index = Integer.valueOf(params.getString("templateIndex"))
					.intValue();
			actionForTemplate("continue", index, params, state);

			addNewSite(params, state);

			Site site = getStateSite(state);

			Site templateSite = (Site) state.getAttribute(STATE_TEMPLATE_SITE);
			if (templateSite == null) 
			{
				// normal site creation: add the features.
				saveFeatures(params, state, site);
			}
			else
			{
				// create based on template: skip add features, and copying all the contents from the tools in template site
				importToolContent(site.getId(), templateSite.getId(), site, true);
			}
				
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
			
			// transfer site content from template site
			if (templateSite != null) 
			{
				sendTemplateUseNotification(site, UserDirectoryService.getCurrentUser(), templateSite);					
			}

			String siteId = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);

			// now that the site exists, we can set the email alias when an
			// Email Archive tool has been selected
			setSiteAlias(state, siteId);
			
			// save user answers
			saveSiteSetupQuestionUserAnswers(state, siteId);
			
			// TODO: hard coding this frame id is fragile, portal dependent, and
			// needs to be fixed -ggolden
			// schedulePeerFrameRefresh("sitenav");
			scheduleTopRefresh();

			resetPaging(state);

			// clean state variables
			cleanState(state);

			if (SITE_MODE_HELPER.equals(state.getAttribute(STATE_SITE_MODE))) {
				state.setAttribute(SiteHelper.SITE_CREATE_SITE_ID, site.getId());
				state.setAttribute(STATE_SITE_MODE, SITE_MODE_HELPER_DONE);
			}
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");

		}

	}// doFinish


	/**
	 * set site mail alias
	 * @param state
	 * @param siteId
	 */
	private void setSiteAlias(SessionState state, String siteId) {
		List oTools = (List) state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		if (oTools == null || (oTools!=null && !oTools.contains("sakai.mailbox")))
		{
			// set alias only if the email archive tool is newly added
			String alias = StringUtil.trimToNull((String) state
					.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			if (alias != null) {
				String channelReference = mailArchiveChannelReference(siteId);
				try {
					AliasService.setAlias(alias, channelReference);
				} catch (IdUsedException ee) {
					addAlert(state, rb.getString("java.alias") + " " + alias
							+ " " + rb.getString("java.exists"));
					M_log.warn(this + ".setSiteAlias: " + rb.getString("java.alias") + " " + alias + " " + rb.getString("java.exists"), ee);
				} catch (IdInvalidException ee) {
					addAlert(state, rb.getString("java.alias") + " " + alias
							+ " " + rb.getString("java.isinval"));
					M_log.warn(this + ".setSiteAlias: " + rb.getString("java.alias") + " " + alias + " " + rb.getString("java.isinval"), ee);
				} catch (PermissionException ee) {
					addAlert(state, rb.getString("java.addalias") + " ");
					M_log.warn(this + ".setSiteAlias: " + rb.getString("java.addalias") + ee);
				}
			}
		}
	}

	/**
	 * save user answers
	 * @param state
	 * @param siteId
	 */
	private void saveSiteSetupQuestionUserAnswers(SessionState state,
			String siteId) {
		// update the database with user answers to SiteSetup questions
		if (state.getAttribute(STATE_SITE_SETUP_QUESTION_ANSWER) != null)
		{
			Set<SiteSetupUserAnswer> userAnswers = (Set<SiteSetupUserAnswer>) state.getAttribute(STATE_SITE_SETUP_QUESTION_ANSWER);
			for(Iterator<SiteSetupUserAnswer> aIterator = userAnswers.iterator(); aIterator.hasNext();)
			{
				SiteSetupUserAnswer userAnswer = aIterator.next();
				userAnswer.setSiteId(siteId);
				// save to db
				questionService.saveSiteSetupUserAnswer(userAnswer);
			}
		}
	}

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
				M_log.warn(this + ".updateCourseSiteSections: IdUnusedException, not found, or not an AuthzGroup object", e);
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
				M_log.warn(this + ".updateCourseSiteSections: " + rb.getString("java.problem"), e);
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
							
							rb.setContextLocale(rb.getLocale(instructor.getId()));
							
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
							// revert back the local setting to default
							rb.setContextLocale(Locale.getDefault());
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
			// set locale to system default
			rb.setContextLocale(Locale.getDefault());
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
			// set the locale to individual receipient's setting
			rb.setContextLocale(rb.getLocale(cUser.getId()));
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
			// revert the locale to system default
			rb.setContextLocale(Locale.getDefault());
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
				M_log.warn(this + ".sendSiteNotification:" + rb.getString("user.notdefined") + " " + userId, e);
			}

			// To Support
			//set local to default
			rb.setContextLocale(Locale.getDefault());
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
		
		if (SITE_MODE_HELPER.equals(state.getAttribute(STATE_SITE_MODE))) {
			state.setAttribute(STATE_SITE_MODE, SITE_MODE_HELPER_DONE);
			state.setAttribute(SiteHelper.SITE_CREATE_CANCELLED, Boolean.TRUE);
		} else {
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");
		}
		
		resetVisitedTemplateListToIndex(state, (String) state.getAttribute(STATE_TEMPLATE_INDEX));

	} // doCancel_create

	/**
	 * doCancel called when "eventSubmit_doCancel" is in the request parameters
	 * to c int index = Integer.valueOf(params.getString
	 * ("templateIndex")).intValue();
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

		if (currentIndex.equals("3")) {
			state.removeAttribute(STATE_TOOL_EMAIL_ADDRESS);
			state.removeAttribute(STATE_MESSAGE);
			removeEditToolState(state);
		} else if (currentIndex.equals("13") || currentIndex.equals("14")) {
			// clean state attributes
			state.removeAttribute(FORM_SITEINFO_TITLE);
			state.removeAttribute(FORM_SITEINFO_DESCRIPTION);
			state.removeAttribute(FORM_SITEINFO_SHORT_DESCRIPTION);
			state.removeAttribute(FORM_SITEINFO_SKIN);
			state.removeAttribute(FORM_SITEINFO_INCLUDE);
			state.removeAttribute(FORM_SITEINFO_ICON_URL);
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		} else if (currentIndex.equals("15")) {
			params = data.getParameters();
			state.setAttribute(STATE_TEMPLATE_INDEX, params
					.getString("cancelIndex"));
			removeEditToolState(state);
		}
		// htripath: added 'currentIndex.equals("45")' for import from file
		// cancel
		else if (currentIndex.equals("45")) {
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
		} else if (currentIndex.equals("27") || currentIndex.equals("28") || currentIndex.equals("59") || currentIndex.equals("60")) {
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
		// if all fails to match
		else if (isTemplateVisited(state, "12")) {
			// go to site info list view
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		} else {
			// go to WSetup list view
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");
		}
		
		resetVisitedTemplateListToIndex(state, (String) state.getAttribute(STATE_TEMPLATE_INDEX));

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
		
		// reset
		resetVisitedTemplateListToIndex(state, "0");

	} // doBack_to_list


	/**
	 * reset to sublist with index as the last item
	 * @param state
	 * @param index
	 */
	private void resetVisitedTemplateListToIndex(SessionState state, String index) {
		if (state.getAttribute(STATE_VISITED_TEMPLATES) != null)
		{
			List<String> l = (List<String>) state.getAttribute(STATE_VISITED_TEMPLATES);
			if (l != null && l.indexOf(index) >=0 && l.indexOf(index) < l.size())
			{	
				state.setAttribute(STATE_VISITED_TEMPLATES, l.subList(0, l.indexOf(index)+1));
			}
		}
	}

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
					.getString("templateIndex"));
		}

	} // doAdd_custom_link

	/**
	 * toolId might be of form original tool id concatenated with number
	 * find whether there is an counterpart in the the multipleToolIdSet
	 * @param state
	 * @param toolId
	 * @return
	 */
	private String findOriginalToolId(SessionState state, String toolId) {
		// treat home tool differently
		if (toolId.equals(getHomeToolId(state)))
		{
			return toolId;
		}
		else
		{
			Set categories = new HashSet();
			categories.add((String) state.getAttribute(STATE_SITE_TYPE));
			Set toolRegistrationList = ToolManager.findTools(categories, null);
			String rv = null;
			if (toolRegistrationList != null)
			{
				for (Iterator i=toolRegistrationList.iterator(); rv == null && i.hasNext();)
				{
					Tool tool = (Tool) i.next();
					String tId = tool.getId();
					rv = originalToolId(toolId, tId);
				}
			}
			return rv;
		}
	}



	private String originalToolId(String toolId, String toolRegistrationId) {
		String rv = null;
		
		if (toolId.indexOf(toolRegistrationId) != -1)
		{
			// the multiple tool id format is of TOOL_IDx, where x is an intger >= 1
			if (toolId.endsWith(toolRegistrationId))
			{
				rv = toolRegistrationId;
			} else
			{
				String suffix = toolId.substring(toolId.indexOf(toolRegistrationId) + toolRegistrationId.length());
				try
				{
					Integer.parseInt(suffix);
					rv = toolRegistrationId;
				}
				catch (Exception e)
				{
					// not the right tool id
					M_log.debug(this + ".findOriginalToolId not matchign tool id = " + toolRegistrationId + " original tool id=" + toolId + e.getMessage(), e);
				}
			}
			
		}
		return rv;
	}

	/**
	 * Read from tool registration whether multiple registration is allowed for this tool
	 * @param toolId
	 * @return
	 */
	private boolean isMultipleInstancesAllowed(String toolId)
	{
		Tool tool = ToolManager.getTool(toolId);
		if (tool != null)
		{
			Properties tProperties = tool.getRegisteredConfig();
			return (tProperties.containsKey("allowMultipleInstances") 
					&& tProperties.getProperty("allowMultipleInstances").equalsIgnoreCase(Boolean.TRUE.toString()))?true:false;
		}
		return false;
	}
	
	private Hashtable<String, String> getMultiToolConfiguration(String toolId)
	{
		Hashtable<String, String> rv = new Hashtable<String, String>();
		
		// read from configuration file
		ArrayList<String> attributes=new ArrayList<String>();
		String attributesConfig = ServerConfigurationService.getString(CONFIG_TOOL_ATTRIBUTE + toolId);
		if ( attributesConfig != null && attributesConfig.length() > 0)
		{
			attributes = new ArrayList(Arrays.asList(attributesConfig.split(",")));
		}
		else
		{
			if (toolId.equals("sakai.news"))
			{
				// default setting for News tool
				attributes.add("channel-url");
			}
			else if (toolId.equals("sakai.iframe"))
			{
				// default setting for Web Content tool
				attributes.add("source");
			}
		}
		
		ArrayList<String> defaultValues =new ArrayList<String>();
		String defaultValueConfig = ServerConfigurationService.getString(CONFIG_TOOL_ATTRIBUTE_DEFAULT + toolId);
		if ( defaultValueConfig != null && defaultValueConfig.length() > 0)
		{
			defaultValues = new ArrayList(Arrays.asList(defaultValueConfig.split(",")));
		}
		else
		{
			if (toolId.equals("sakai.news"))
			{
				// default value
				defaultValues.add("http://www.sakaiproject.org/news-rss-feed");
			}
			else if (toolId.equals("sakai.iframe"))
			{
				// default setting for Web Content tool
				defaultValues.add("http://");
			}
		}
		
		if (attributes != null && attributes.size() > 0)
		{
			for (int i = 0; i<attributes.size();i++)
			{
				rv.put(attributes.get(i), defaultValues.get(i));
			}
		}
		return rv;
	}
	
	/**
	 * doSave_revised_features
	 */
	public void doSave_revised_features(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		
		Site site = getStateSite(state);
		
		saveFeatures(params, state, site);
		
		String id = site.getId();

		// now that the site exists, we can set the email alias when an Email
		// Archive tool has been selected
		setSiteAlias(state, id);
		
		if (state.getAttribute(STATE_MESSAGE) == null) {
			// clean state variables
			state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
			state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME);
			state.removeAttribute(STATE_MULTIPLE_TOOL_ID_SET);
			state.removeAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP);

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
	 * doMenu_siteInfo_importSelection
	 */
	public void doMenu_siteInfo_importSelection(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the tools
		siteToolsIntoState(state);

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "58");
		}

	} // doMenu_siteInfo_importSelection
	
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
	
	public void doMenu_siteInfo_importMigrate(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the tools
		siteToolsIntoState(state);

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "59");
		}

	} // doMenu_siteInfo_importMigrate

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
							M_log.warn(this + ".doMenu_siteinfo_addClass: " + e.getMessage() + site.getId(), e);
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
			M_log.warn(this + ".doMenu_siteinfo_addClass: " + e.getMessage() + termEid, e);
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
			state.setAttribute(STATE_TEMPLATE_INDEX, "3");
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
		
		// reset
		resetVisitedTemplateListToIndex(state, "0");

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
		SiteInfo siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);

		if (siteTitleEditable(state, site_type)) 
		{
			Site.setTitle(siteInfo.title);
		}

		Site.setDescription(siteInfo.description);
		Site.setShortDescription(siteInfo.short_description);

		if (site_type != null) {
			if (site_type.equals((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) {
				// set icon url for course
				setAppearance(state, Site, siteInfo.iconUrl);
			} else {
				// set icon url for others
				Site.setIconUrl(siteInfo.iconUrl);
			}

		}

		// site contact information
		String contactName = siteInfo.site_contact_name;
		if (contactName != null) {
			siteProperties.addProperty(PROP_SITE_CONTACT_NAME, contactName);
		}

		String contactEmail = siteInfo.site_contact_email;
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
	 * Check to see whether the site's title is editable or not
	 * @param state
	 * @param site_type
	 * @return
	 */
	private boolean siteTitleEditable(SessionState state, String site_type) {
		return site_type != null 
				&& (!site_type.equals((String) state.getAttribute(STATE_COURSE_SITE_TYPE))
					||	(state.getAttribute(TITLE_EDITABLE_SITE_TYPE) != null 
							&& ((List) state.getAttribute(TITLE_EDITABLE_SITE_TYPE)).contains(site_type)));
	}

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

		if (SITE_MODE_SITESETUP.equalsIgnoreCase((String) state.getAttribute(STATE_SITE_MODE))) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");
		} else if (SITE_MODE_HELPER.equalsIgnoreCase((String) state.getAttribute(STATE_SITE_MODE))) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "1");
		} else if (SITE_MODE_SITEINFO.equalsIgnoreCase((String) state.getAttribute(STATE_SITE_MODE))){

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
				t = (String)state.getAttribute(SiteHelper.SITE_CREATE_SITE_TYPES);
				if (t != null) {
					state.setAttribute(STATE_SITE_TYPES, new ArrayList(Arrays
						.asList(t.split(","))));
				} else {
					state.setAttribute(STATE_SITE_TYPES, new Vector());
				}
			}
		}
		
		// need to watch out for the config question.xml existence.
		// read the file and put it to backup folder.
		if (SiteSetupQuestionFileParser.isConfigurationXmlAvailable())
		{
			SiteSetupQuestionFileParser.updateConfig();
		}
		
		// show UI for adding non-official participant(s) or not
		// if nonOfficialAccount variable is set to be false inside sakai.properties file, do not show the UI section for adding them.
		// the setting defaults to be true
		if (state.getAttribute(ADD_NON_OFFICIAL_PARTICIPANT) == null)
		{
			state.setAttribute(ADD_NON_OFFICIAL_PARTICIPANT, ServerConfigurationService.getString("nonOfficialAccount", "true"));
		}
		
		if (state.getAttribute(STATE_VISITED_TEMPLATES) == null)
		{
			List<String> templates = new Vector<String>();
			if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITESETUP)) {
				templates.add("0"); // the default page of WSetup tool
			} else if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITEINFO)) {
				templates.add("12");// the default page of Site Info tool
			}

			state.setAttribute(STATE_VISITED_TEMPLATES, templates);
		}
		if (state.getAttribute(STATE_SITE_TITLE_MAX) == null) {
			int siteTitleMaxLength = ServerConfigurationService.getInt("site.title.maxlength", 20);
			state.setAttribute(STATE_SITE_TITLE_MAX, siteTitleMaxLength);
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
			M_log.warn(this + ".getReviseSite: " +  e.toString() + " site id = " + siteId, e);
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
							realmEdit.removeMember(user.getId());
						} catch (UserNotDefinedException e) {
							M_log.warn(this + ".doUpdate_participant: IdUnusedException " + rId + ". ", e);
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
					
					AuthzGroupService.save(realmEdit);
					
					// post event about the participant update
					EventTrackingService.post(EventTrackingService.newEvent(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP, realmEdit.getId(),false));
					
					// then update all related group realms for the role
					doUpdate_related_group_participants(s, realmId);
				}
			} catch (GroupNotDefinedException e) {
				addAlert(state, rb.getString("java.problem2"));
				M_log.warn(this + ".doUpdate_participant: IdUnusedException " + s.getTitle() + "(" + realmId + "). ", e);
			} catch (AuthzPermissionException e) {
				addAlert(state, rb.getString("java.changeroles"));
				M_log.warn(this + ".doUpdate_participant: PermissionException " + s.getTitle() + "(" + realmId + "). ", e);
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
			try
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
								// check for Site Info-managed groups: don't change roles for other groups (e.g. section-managed groups)
								String gProp = g.getProperties().getProperty(SiteConstants.GROUP_PROP_WSETUP_CREATED);
								
								// if there is a difference between the role setting, remove the entry from group and add it back with correct role, all are marked "not provided"
								if (gProp != null && gProp.equals(Boolean.TRUE.toString()) &&
										!g.getUserRole(gMemberId).equals(siteMember.getRole()))
								{
									Role siteRole = siteMember.getRole();
									if (g.getRole(siteRole.getId()) == null)
									{
										// in case there is no matching role as that in the site, create such role and add it to the user
										g.addRole(siteRole.getId(), siteRole);
									}
									g.removeMember(gMemberId);
									g.addMember(gMemberId, siteRole.getId(), siteMember.isActive(), false);
								}
							}
						}
						// post event about the participant update
						EventTrackingService.post(EventTrackingService.newEvent(SiteService.SECURE_UPDATE_GROUP_MEMBERSHIP, g.getId(),false));
					}
					catch (Exception ee)
					{
						M_log.warn(this + ".doUpdate_related_group_participants: " + ee.getMessage() + g.getId(), ee);
					}
					
				}
				// commit, save the site
				SiteService.save(s);
			}
			catch (Exception e)
			{
				M_log.warn(this + ".doUpdate_related_group_participants: " + e.getMessage() + s.getId(), e);
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
			updateSiteInfo(params, state);

			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			
			// alerts after clicking Continue but not Back
			if (forward) {
				if (StringUtil.trimToNull(siteInfo.title) == null) {
					addAlert(state, rb.getString("java.reqfields"));
					state.setAttribute(STATE_TEMPLATE_INDEX, "2");
					return;
				}
			} else {
				// removing previously selected template site
					state.removeAttribute(STATE_TEMPLATE_SITE);				
			}
			
			updateSiteAttributes(state);

			if (state.getAttribute(STATE_MESSAGE) == null) {
				updateCurrentStep(state, forward);
			}

			break;
		case 3:
			/*
			 * actionForTemplate chef_site-editFeatures.vm
			 * 
			 */
			if (forward) {
				// editing existing site or creating a new one?
				Site site = getStateSite(state);
				getFeatures(params, state, site==null?"18":"15");
				
				if (state.getAttribute(STATE_MESSAGE) == null && site==null) {
					updateCurrentStep(state, forward);
				}
			}
			break;
		case 5:
			/*
			 * actionForTemplate chef_site-addParticipant.vm
			 * 
			 */
			/*if (forward) {
				checkAddParticipant(params, state);
			} else {
				// remove related state variables
				removeAddParticipantContext(state);
			}*/
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
				updateSiteInfo(params, state);

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
			updateSelectedToolList(state, params, true);
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
		case 60:
			/*
			 * actionForTemplate chef_site-importSitesMigrate.vm
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
						// Remove all old contents before importing contents from new site
						importToolIntoSiteMigrate(selectedTools, importTools,
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
		case 58:
			/*
			 * actionForTemplate chef_siteinfo-importSelection.vm
			 * 
			 */
			break;
		case 59:
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
				
				// validate the alias
				if (StringUtil.trimToNull(siteInfo.url_alias) != null &&
						!siteInfo.url_alias.equals(NULL_STRING)) {
					try {
						AliasService.getTarget(siteInfo.url_alias);
						addAlert(state, rb.getString("java.alias") + " " + siteInfo.url_alias
								+ " " + rb.getString("java.exists"));
						state.setAttribute(STATE_TEMPLATE_INDEX, "2");
						return;
					} catch (IdUnusedException e) {
						// Do nothing. We want the alias to be unused.
					}
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

						String nSiteId = IdManager.createUuid();
						try {
							String oSiteId = (String) state
									.getAttribute(STATE_SITE_INSTANCE_ID);
							
							Site site = SiteService.addSite(nSiteId,
									getStateSite(state));
							
							// get the new site icon url
							if (site.getIconUrl() != null)
							{
								site.setIconUrl(transferSiteResource(oSiteId, nSiteId, site.getIconUrl()));
							}
							
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
								importToolContent(nSiteId, oSiteId, site, false);

							} catch (Exception e1) {
								// if goes here, IdService
								// or SiteService has done
								// something wrong.
								M_log.warn(this + ".actionForTemplate chef_siteinfo-duplicate: " + e1 + ":" + nSiteId + "when duplicating site", e1);
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
								
								if (site.getType().equals((String) state.getAttribute(STATE_COURSE_SITE_TYPE))) 
								{
									// also remove the provider id attribute if any
									String realm = SiteService.siteReference(site.getId());
									try 
									{
										AuthzGroup realmEdit = AuthzGroupService.getAuthzGroup(realm);
										realmEdit.setProviderGroupId(null);
										AuthzGroupService.save(realmEdit);
									} catch (GroupNotDefinedException e) {
										M_log.warn(this + ".actionForTemplate chef_siteinfo-duplicate: IdUnusedException, not found, or not an AuthzGroup object "+ realm, e);
										addAlert(state, rb.getString("java.realm"));
									} catch (Exception e) {
										addAlert(state, this + rb.getString("java.problem"));
										M_log.warn(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.problem"), e);
									}
								}
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
							M_log.warn(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.siteinval") + " site id = " + nSiteId, e);
						} catch (IdUsedException e) {
							addAlert(state, rb.getString("java.sitebeenused"));
							M_log.warn(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.sitebeenused") + " site id = " + nSiteId, e);
						} catch (PermissionException e) {
							addAlert(state, rb.getString("java.allowcreate"));
							M_log.warn(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.allowcreate") + " site id = " + nSiteId, e);
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
							M_log.warn( this + e.getMessage() + sectionId, e);
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
					M_log.warn(this + ".actionForTemplate chef_siteinfo-addCourseConfirm: " +  e.getMessage() + site.getId(), e);
				}
				
				removeAddClassContext(state);
			}

			break;
		case 54:
			if (forward) {
				
				// store answers to site setup questions
				if (getAnswersToSetupQuestions(params, state))
				{
					state.setAttribute(STATE_TEMPLATE_INDEX, state.getAttribute(STATE_SITE_SETUP_QUESTION_NEXT_TEMPLATE));
				}
			}
			break;
		}

	}// actionFor Template

	/**
	 * This is used to update exsiting site attributes with encoded site id in it. A new resource item is added to new site when needed
	 * 
	 * @param oSiteId
	 * @param nSiteId
	 * @param siteAttribute
	 * @return the new migrated resource url
	 */
	private String transferSiteResource(String oSiteId, String nSiteId, String siteAttribute) {
		String rv = "";
		
		String accessUrl = ServerConfigurationService.getAccessUrl();
		if (siteAttribute!= null && siteAttribute.indexOf(oSiteId) != -1 && accessUrl != null)
		{
			// stripe out the access url, get the relative form of "url"
			Reference ref = EntityManager.newReference(siteAttribute.replaceAll(accessUrl, ""));
			try
			{
				ContentResource resource = m_contentHostingService.getResource(ref.getId());
				// the new resource
				ContentResource nResource = null;
				String nResourceId = resource.getId().replaceAll(oSiteId, nSiteId);
				try
				{
					nResource = m_contentHostingService.getResource(nResourceId);
				}
				catch (Exception n2Exception)
				{
					// copy the resource then
					try
					{
						nResourceId = m_contentHostingService.copy(resource.getId(), nResourceId);
						nResource = m_contentHostingService.getResource(nResourceId);
					}
					catch (Exception n3Exception)
					{
					}
				}
				
				// get the new resource url
				rv = nResource != null?nResource.getUrl(false):"";
				
			}
			catch (Exception refException)
			{
				M_log.warn(this + ":transferSiteResource: cannot find resource with ref=" + ref.getReference() + " " + refException.getMessage());
			}
		}
		
		return rv;
	}
	
	/**
	 * 
	 * @param nSiteId
	 * @param oSiteId
	 * @param site
	 */
	private void importToolContent(String nSiteId, String oSiteId, Site site, boolean bypassSecurity) {
		// import tool content
		
		if (bypassSecurity)
		{
			// importing from template, bypass the permission checking:
			// temporarily allow the user to read and write from assignments (asn.revise permission)
	        SecurityService.pushAdvisor(new SecurityAdvisor()
	            {
	                public SecurityAdvice isAllowed(String userId, String function, String reference)
	                {
	                    return SecurityAdvice.ALLOWED;
	                }
	            });
		}
				
		List pageList = site.getPages();
		if (!((pageList == null) || (pageList.size() == 0))) {
			for (ListIterator i = pageList
					.listIterator(); i.hasNext();) {
				SitePage page = (SitePage) i.next();

				List pageToolList = page.getTools();
				if (!(pageToolList == null || pageToolList.size() == 0))
				{
					Tool tool = ((ToolConfiguration) pageToolList.get(0)).getTool();
					String toolId = tool != null?tool.getId():"";
					if (toolId.equalsIgnoreCase("sakai.resources")) {
						// handle
						// resource
						// tool
						// specially
						transferCopyEntities(
								toolId,
								m_contentHostingService
										.getSiteCollection(oSiteId),
								m_contentHostingService
										.getSiteCollection(nSiteId));
					} else if (toolId.equalsIgnoreCase(SITE_INFORMATION_TOOL)) {
						// handle Home tool specially, need to update the site infomration display url if needed
						String newSiteInfoUrl = transferSiteResource(oSiteId, nSiteId, site.getInfoUrl());
						site.setInfoUrl(newSiteInfoUrl);
					}
					else {
						// other
						// tools
						transferCopyEntities(toolId,
								oSiteId, nSiteId);
					}
				}
			}
		}
		
		if (bypassSecurity)
		{
			SecurityService.clearAdvisors();
		}
	}
	/**
	 * get user answers to setup questions
	 * @param params
	 * @param state
	 * @return
	 */
	protected boolean getAnswersToSetupQuestions(ParameterParser params, SessionState state)
	{
		boolean rv = true;
		String answerString = null;
		String answerId = null;
		Set userAnswers = new HashSet();
		
		SiteTypeQuestions siteTypeQuestions = questionService.getSiteTypeQuestions((String) state.getAttribute(STATE_SITE_TYPE));
		if (siteTypeQuestions != null)
		{
			List<SiteSetupQuestion> questions = siteTypeQuestions.getQuestions();
			for (Iterator i = questions.iterator(); i.hasNext();)
			{
				SiteSetupQuestion question = (SiteSetupQuestion) i.next();
				// get the selected answerId
				answerId = params.get(question.getId());
				if (question.isRequired() && answerId == null)
				{
					rv = false;
					addAlert(state, rb.getString("sitesetupquestion.alert"));
				}
				else if (answerId != null)
				{
					SiteSetupQuestionAnswer answer = questionService.getSiteSetupQuestionAnswer(answerId);
					if (answer != null)
					{
						if (answer.getIsFillInBlank())
						{
							// need to read the text input instead
							answerString = params.get("fillInBlank_" + answerId);
						}
						
						SiteSetupUserAnswer uAnswer = questionService.newSiteSetupUserAnswer();
						uAnswer.setAnswerId(answerId);
						uAnswer.setAnswerString(answerString);
						uAnswer.setQuestionId(question.getId());
						uAnswer.setUserId(SessionManager.getCurrentSessionUserId());
						//update the state variable
						userAnswers.add(uAnswer);
					}
				}
			}
			state.setAttribute(STATE_SITE_SETUP_QUESTION_ANSWER, userAnswers);	
		}
		return rv;
	}
	
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
				M_log.warn(this + ".updateCourseClasses: IdUnusedException, " + site.getTitle()
						+ "(" + realmId
						+ ") not found, or not an AuthzGroup object", e);
				addAlert(state, rb.getString("java.cannotedit"));
				return;
			} catch (AuthzPermissionException e) {
				M_log.warn(this + ".updateCourseClasses: PermissionException, user does not have permission to edit AuthzGroup object "
								+ site.getTitle() + "(" + realmId + "). ", e);
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
				M_log.warn(this + ".updateCourseClasses: IdUnusedException, " + site.getTitle()
						+ "(" + realmId
						+ ") not found, or not an AuthzGroup object", e);
				addAlert(state, rb.getString("java.cannotclasses"));
				return;
			} catch (AuthzPermissionException e) {
				M_log.warn(this
								+ ".updateCourseClasses: PermissionException, user does not have permission to edit AuthzGroup object "
								+ site.getTitle() + "(" + realmId + "). ", e);
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
				M_log.warn(this + ".updateCourseClasses: cannot remove authzgroup : " + group.getReference(), e);
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
				M_log.warn(this +".updateCourseClasses:" + e.toString(), e);
			}
		}
		if (notifyClasses != null && notifyClasses.size() > 0) {
			try {
				// send out class access confirmation notifications
				sendSiteNotification(state, notifyClasses);
			} catch (Exception e) {
				M_log.warn(this + ".updateCourseClasses:" + e.toString(), e);
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
					M_log.warn(this + ".updateSiteAttributes: IdUnusedException "
							+ siteInfo.getTitle() + "(" + id + ") not found", e);
				}
			}

			// no permission
			else {
				addAlert(state, rb.getString("java.makechanges"));
				M_log.warn(this + ".updateSiteAttributes: PermissionException "
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
			StringBuilder alertMsg = new StringBuilder();
			String description = params.getString("description");
			siteInfo.description = FormattedText.processFormattedText(description, alertMsg);
		}
		if (params.getString("short_description") != null) {
			siteInfo.short_description = params.getString("short_description");
		}
		if (params.getString("additional") != null) {
			siteInfo.additional = params.getString("additional");
		}
		if (params.getString("iconUrl") != null) {
			siteInfo.iconUrl = Validator.escapeHtml(params.getString("iconUrl"));
		} else if (params.getString("skin") != null) {
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

		String alias = params.getString("url_alias");
		if (alias != null) {
			try
			{
				alias =  java.net.URLEncoder.encode(params.getString("url_alias"), "UTF-8");
				siteInfo.url_alias = alias;
				try {
					AliasService.getTarget(alias);
					// the alias has been used
					addAlert(state, rb.getString("java.alias") + " " + alias + " " + rb.getString("java.exists"));
				} catch (IdUnusedException ee) {
					// wanted situation: the alias has not been used
				}
			} catch (java.io.UnsupportedEncodingException e)
			{
				// log exception
				M_log.warn( this + " error of encoding url alias " + alias );
			}
			 
		}
		
		state.setAttribute(STATE_SITE_INFO, siteInfo);
		
		// check for site title length
		if (siteInfo.title.length() > SiteConstants.SITE_GROUP_TITLE_LIMIT)
		{
			addAlert(state, rb.getString("site_group_title_length_limit_1") + SiteConstants.SITE_GROUP_TITLE_LIMIT + " " + rb.getString("site_group_title_length_limit_2"));
		}

	} // updateSiteInfo

	/**
	 * getParticipantList
	 * 
	 */
	private Collection getParticipantList(SessionState state) {
		List members = new Vector();
		String siteId = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);

		List providerCourseList = null;
		providerCourseList = SiteParticipantHelper.getProviderCourseList(siteId);
		if (providerCourseList != null && providerCourseList.size() > 0) {
			state.setAttribute(SITE_PROVIDER_COURSE_LIST, providerCourseList);
		}

		Collection participants = SiteParticipantHelper.prepareParticipants(siteId, providerCourseList);
		state.setAttribute(STATE_PARTICIPANT_LIST, participants);

		return participants;

	} // getParticipantList

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
			M_log.warn( this + ".getRoles: IdUnusedException " + realmId, e);
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

	private void saveFeatures(ParameterParser params, SessionState state, Site site) {
		
		// get the list of Worksite Setup configured pages
		List wSetupPageList = state.getAttribute(STATE_WORKSITE_SETUP_PAGE_LIST)!=null?(List) state.getAttribute(STATE_WORKSITE_SETUP_PAGE_LIST):new Vector();

		Set multipleToolIdSet = (Set) state.getAttribute(STATE_MULTIPLE_TOOL_ID_SET);
		// get the map of titles of multiple tool instances
		Map multipleToolIdTitleMap = state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP) != null? (Map) state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP):new HashMap();
		
		WorksiteSetupPage wSetupPage = new WorksiteSetupPage();
		WorksiteSetupPage wSetupHome = new WorksiteSetupPage();
		
		
		List pageList = new Vector();
		// declare some flags used in making decisions about Home, whether to
		// add, remove, or do nothing
		boolean hasHome = false;
		boolean homeInWSetupPageList = false;

		List chosenList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		// if features were selected, diff wSetupPageList and chosenList to get
		// page adds and removes
		// boolean values for adding synoptic views
		boolean hasAnnouncement = false;
		boolean hasSchedule = false;
		boolean hasChat = false;
		boolean hasDiscussion = false;
		boolean hasEmail = false;
		boolean hasSiteInfo = false;
		boolean hasMessageCenter = false;
		
		// tools to be imported from other sites?
		Hashtable importTools = null;
		if (state.getAttribute(STATE_IMPORT_SITE_TOOL) != null) {
			importTools = (Hashtable) state.getAttribute(STATE_IMPORT_SITE_TOOL);
		}
		
		// Home tool chosen?
		if (chosenList.contains(getHomeToolId(state))) {
			// add home tool later
			hasHome = true;
		}
		
		// order the id list
		chosenList = orderToolIds(state, checkNullSiteType(state, site), chosenList);
		
		// Special case - Worksite Setup Home comes from a hardcoded checkbox on
		// the vm template rather than toolRegistrationList
		// see if Home was chosen
		for (ListIterator j = chosenList.listIterator(); j.hasNext();) {
			String choice = (String) j.next();
			if (choice.equals("sakai.mailbox")) {
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
			}  else if (choice.equals("sakai.siteinfo")) {
				hasSiteInfo = true;
			}
			
		}

		// see if Home and/or Help in the wSetupPageList (can just check title
		// here, because we checked patterns before adding to the list)
		for (ListIterator i = wSetupPageList.listIterator(); !homeInWSetupPageList && i.hasNext();) {
			wSetupPage = (WorksiteSetupPage) i.next();
			if (wSetupPage.getToolId().equals(getHomeToolId(state))) {
				homeInWSetupPageList = true;
			}
		}

		if (hasHome) {
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
						if ((comparePage.getToolId()).equals(getHomeToolId(state))) {
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
				wSetupHome.toolId = getHomeToolId(state);
				wSetupPageList.add(wSetupHome);

				// Add worksite information tool
				ToolConfiguration tool = page.addTool();
				Tool reg = ToolManager.getTool(SITE_INFORMATION_TOOL);
				tool.setTool(SITE_INFORMATION_TOOL, reg);
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
					M_log.warn(this + ".saveFeatures: " + e.getMessage() + " site id = " + site.getId(), e);
				}
			}
		} // add Home

		// if Home is in wSetupPageList and not chosen, remove Home feature from
		// wSetupPageList and site
		if (!hasHome && homeInWSetupPageList) {
			// remove Home from wSetupPageList
			WorksiteSetupPage removePage = new WorksiteSetupPage();
			for (ListIterator i = wSetupPageList.listIterator(); i.hasNext();) {
				WorksiteSetupPage comparePage = (WorksiteSetupPage) i.next();
				if (comparePage.getToolId().equals(getHomeToolId(state))) {
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

		Set categories = new HashSet();
		categories.add((String) state.getAttribute(STATE_SITE_TYPE));
		Set toolRegistrationSet = ToolManager.findTools(categories, null);

		// first looking for any tool for removal
		Vector removePageIds = new Vector();
		for (ListIterator k = wSetupPageList.listIterator(); k.hasNext();) {
			wSetupPage = (WorksiteSetupPage) k.next();
			String pageToolId = wSetupPage.getToolId();

			// use page id + tool id for multiple tool instances
			if (isMultipleInstancesAllowed(findOriginalToolId(state, pageToolId))) {
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

				// use page Id + toolId for multiple tool instances
				if (isMultipleInstancesAllowed(findOriginalToolId(state, pageToolId))) {
					pageToolId = wSetupPage.getPageId() + pageToolId;
				}

				if (pageToolId.equals(toolId)) {
					inWSetupPageList = true;
					// but for tool of multiple instances, need to change the title
					if (isMultipleInstancesAllowed(findOriginalToolId(state, toolId))) {
						SitePage pEdit = (SitePage) site
								.getPage(wSetupPage.pageId);
						pEdit.setTitle((String) multipleToolIdTitleMap.get(toolId));
						List toolList = pEdit.getTools();
						for (ListIterator jTool = toolList.listIterator(); jTool
								.hasNext();) {
							ToolConfiguration tool = (ToolConfiguration) jTool
									.next();
							String tId = tool.getTool().getId();
							if (isMultipleInstancesAllowed(findOriginalToolId(state, tId))) {
								// set tool title
								tool.setTitle((String) multipleToolIdTitleMap.get(toolId));
								// save tool configuration
								saveMultipleToolConfiguration(state, tool, toolId);
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
				Tool toolRegFound = null;
				for (Iterator i = toolRegistrationSet.iterator(); i.hasNext();) {
					Tool toolReg = (Tool) i.next();
					if (toolId.indexOf(toolReg.getId()) != -1) {
						toolRegFound = toolReg;
					}
				}

				if (toolRegFound != null) {
					// we know such a tool, so add it
					WorksiteSetupPage addPage = new WorksiteSetupPage();
					SitePage page = site.addPage();
					addPage.pageId = page.getId();
					if (isMultipleInstancesAllowed(findOriginalToolId(state, toolId))) {
						// set tool title
						page.setTitle((String) multipleToolIdTitleMap.get(toolId));
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
					if (isMultipleInstancesAllowed(findOriginalToolId(state, toolId))) {
						// set tool title
						tool.setTitle((String) multipleToolIdTitleMap.get(toolId));
						// save tool configuration
						saveMultipleToolConfiguration(state, tool, toolId);
					} else {
						tool.setTitle(toolRegFound.getTitle());
					}
				}
			}
		} // for

		// reorder Home and Site Info only if the site has not been customized order before
		if (!site.isCustomPageOrdered())
		{
			// the steps for moving page within the list
			int moves = 0;
			if (hasHome) {
				SitePage homePage = null;
				// Order tools - move Home to the top - first find it
				pageList = site.getPages();
				if (pageList != null && pageList.size() != 0) {
					for (ListIterator i = pageList.listIterator(); i.hasNext();) {
						SitePage page = (SitePage) i.next();
						if (pageHasToolId(page.getTools(), getHomeToolId(state)))
						{
							homePage = page;
							break;
						}
					}
				}
				if (homePage != null)
				{
					moves = pageList.indexOf(homePage);
					for (int n = 0; n < moves; n++) {
						homePage.moveUp();
					}
				}
			}
	
			// if Site Info is newly added, more it to the last
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
							break;
						}
					}
					if (siteInfoPage != null)
					{
						// move home from it's index to the first position
						moves = pageList.indexOf(siteInfoPage);
						for (int n = moves; n < pageList.size(); n++) {
							siteInfoPage.moveDown();
						}
					}
				}
			}
		}

		// if there is no email tool chosen
		if (!hasEmail) {
			state.removeAttribute(STATE_TOOL_EMAIL_ADDRESS);
		}

		// commit
		commitSite(site);

		// import
		importToolIntoSite(chosenList, importTools, site);
		
	} // saveFeatures

	/**
	 * Save configuration values for multiple tool instances
	 */
	private void saveMultipleToolConfiguration(SessionState state, ToolConfiguration tool, String toolId) {
		// get the configuration of multiple tool instance
		Hashtable<String, Hashtable<String, String>> multipleToolConfiguration = state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION) != null?(Hashtable<String, Hashtable<String, String>>) state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION):new Hashtable<String, Hashtable<String, String>>();
		
		// set tool attributes
		Hashtable<String, String> attributes = multipleToolConfiguration.get(toolId);
		
		if (attributes != null)
		{
			for(String attribute : attributes.keySet())
			{
				String attributeValue = attributes.get(attribute);
				// if we have a value
				if (attributeValue != null)
				{
					// if this value is not the same as the tool's registered, set it in the placement
					if (!attributeValue.equals(tool.getTool().getRegisteredConfig().getProperty(attribute)))
					{
						tool.getPlacementConfig().setProperty(attribute, attributeValue);
					}

					// otherwise clear it
					else
					{
						tool.getPlacementConfig().remove(attribute);
					}
				}

				// if no value
				else
				{
					tool.getPlacementConfig().remove(attribute);
				}
			}
		}
	}

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
	private void getFeatures(ParameterParser params, SessionState state, String continuePageIndex) {
		List idsSelected = new Vector();
		
		List existTools = state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST) == null? new Vector():(List) state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		// to reset the state variable of the multiple tool instances
		Set multipleToolIdSet = state.getAttribute(STATE_MULTIPLE_TOOL_ID_SET) != null? (Set) state.getAttribute(STATE_MULTIPLE_TOOL_ID_SET):new HashSet();
		// get the map of titles of multiple tool instances
		Map multipleToolIdTitleMap = state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP) != null? (Map) state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP):new HashMap();


		boolean goToToolConfigPage = false;
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

				if (toolId.equals(getHomeToolId(state))) {
					homeSelected = true;
					idsSelected.add(toolId);
				} else
				{ 	
					String originId = findOriginalToolId(state, toolId);	
					if (isMultipleInstancesAllowed(originId)) 
					{
						// if user is adding either EmailArchive tool, News tool
						// or Web Content tool, go to the Customize page for the
						// tool
						if (!existTools.contains(toolId)) {
							goToToolConfigPage = true;
							if (!multipleToolIdSet.contains(toolId))
								multipleToolIdSet.add(toolId);
							if (!multipleToolIdTitleMap.containsKey(toolId))
								multipleToolIdTitleMap.put(toolId, ToolManager.getTool(originId).getTitle());
						}
					}
					else if (toolId.equals("sakai.mailbox") && !existTools.contains(toolId)) {
						// get the email alias when an Email Archive tool
						// has been selected
						goToToolConfigPage = true;
						String channelReference = mailArchiveChannelReference((String) state
								.getAttribute(STATE_SITE_INSTANCE_ID));
						List aliases = AliasService.getAliases(
								channelReference, 1, 1);
						if (aliases.size() > 0) {
							state.setAttribute(STATE_TOOL_EMAIL_ADDRESS,
									((Alias) aliases.get(0)).getId());
						}
					}
					idsSelected.add(toolId);
				}

			}

			state.setAttribute(STATE_TOOL_HOME_SELECTED, new Boolean(
					homeSelected));
		}

		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, idsSelected); // List of ToolRegistration toolId's
		
		// in case of import
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

		// of
		// ToolRegistration
		// toolId's
		if (state.getAttribute(STATE_MESSAGE) == null) {
			if (state.getAttribute(STATE_IMPORT) != null) {
				// go to import tool page
				state.setAttribute(STATE_TEMPLATE_INDEX, "27");
			} else if (goToToolConfigPage) {
				// go to the configuration page for multiple instances of tools
				state.setAttribute(STATE_TEMPLATE_INDEX, "26");
			} else {
				// go to next page
				state.setAttribute(STATE_TEMPLATE_INDEX, continuePageIndex);
			}
			state.setAttribute(STATE_MULTIPLE_TOOL_ID_SET, multipleToolIdSet);
			state.setAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, multipleToolIdTitleMap);
		}
	} // getFeatures

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

						String fromSiteCollectionId = m_contentHostingService
								.getSiteCollection(fromSiteId);
						String toSiteCollectionId = m_contentHostingService
								.getSiteCollection(toSiteId);

						transferCopyEntities(toolId, fromSiteCollectionId,
								toSiteCollectionId);
						resourcesImported = true;
					}
				}
			}

			// import other tools then
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

	
	private void importToolIntoSiteMigrate(List toolIds, Hashtable importTools,
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

						String fromSiteCollectionId = m_contentHostingService
								.getSiteCollection(fromSiteId);
						String toSiteCollectionId = m_contentHostingService
								.getSiteCollection(toSiteId);
						transferCopyEntitiesMigrate(toolId, fromSiteCollectionId,
								toSiteCollectionId);
						resourcesImported = true;
					}
				}
			}

			// import other tools then
			for (int i = 0; i < toolIds.size(); i++) {
				String toolId = (String) toolIds.get(i);
				if (!toolId.equalsIgnoreCase("sakai.resources")
						&& importTools.containsKey(toolId)) {
					List importSiteIds = (List) importTools.get(toolId);
					for (int k = 0; k < importSiteIds.size(); k++) {
						String fromSiteId = (String) importSiteIds.get(k);
						String toSiteId = site.getId();
						transferCopyEntitiesMigrate(toolId, fromSiteId, toSiteId);
					}
				}
			}
		}
	} // importToolIntoSiteMigrate


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
				Site site = null;
							
				// if create based on template,
				Site templateSite = (Site) state.getAttribute(STATE_TEMPLATE_SITE);
				if (templateSite != null) {
					site = SiteService.addSite(id, templateSite);
				} else {
					site = SiteService.addSite(id, siteInfo.site_type);
				}
				
				// add current user as the maintainer
				site.addMember(UserDirectoryService.getCurrentUser().getId(), site.getMaintainRole(), true, false);

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
				
				// create an alias for the site
				if (!siteInfo.url_alias.equals(NULL_STRING)) {
					String alias = siteInfo.url_alias;
					String siteReference = site.getReference();
					try {
						AliasService.setAlias(alias, siteReference);
						// In case of failure, return to the confirmation page
						// with an error and undo the site creation we've done so far
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

				// commit newly added site in order to enable related realm
				commitSite(site);

			} catch (IdUsedException e) {
				addAlert(state, rb.getString("java.sitewithid") + " " + id + " " + rb.getString("java.exists"));
				M_log.warn(this + ".addNewSite: " + rb.getString("java.sitewithid") + " " + id + " " + rb.getString("java.exists"), e);
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("templateIndex"));
				return;
			} catch (IdInvalidException e) {
				addAlert(state, rb.getString("java.thesiteid") + " " + id + " " + rb.getString("java.notvalid"));
				M_log.warn(this + ".addNewSite: " + rb.getString("java.thesiteid") + " " + id + " " + rb.getString("java.notvalid"), e);
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("templateIndex"));
				return;
			} catch (PermissionException e) {
				addAlert(state, rb.getString("java.permission") + " " + id + ".");
				M_log.warn(this + ".addNewSite: " + rb.getString("java.permission") + " " + id + ".", e);
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("templateIndex"));
				return;
			}
		}
	} // addNewSite



	private void sendTemplateUseNotification(Site site, User currentUser,
			Site templateSite) {
		// send an email to track who are using the template
		String from = getSetupRequestEmailAddress();
 
		// send it to the email archive of the template site
		// TODO: need a better way to get the email archive address
		//String domain = from.substring(from.indexOf('@'));
		String templateEmailArchive = templateSite.getId() 
			+ "@" + ServerConfigurationService.getServerName();
		String to = templateEmailArchive;
		String headerTo = templateEmailArchive;
		String replyTo = templateEmailArchive;
		String message_subject = templateSite.getId() + ": copied by " + currentUser.getDisplayId ();					

		if (from != null && templateEmailArchive != null) {
			StringBuffer buf = new StringBuffer();
			buf.setLength(0);

			// email body
			buf.append("Dear template maintainer,\n\n");
			buf.append("Congratulations!\n\n");
			buf.append("The following user just created a new site based on your template.\n\n");
			buf.append("Template name: " + templateSite.getTitle() + "\n");
			buf.append("User         : " + currentUser.getDisplayName() + " (" 
					+ currentUser.getDisplayId () + ")\n");
			buf.append("Date         : " + new java.util.Date() + "\n");
			buf.append("New site Id  : " + site.getId() + "\n");
			buf.append("New site name: " + site.getTitle() + "\n\n");
			buf.append("Cheers,\n");
			buf.append("Alliance Team\n");
			String content = buf.toString();
			
			EmailService.send(from, to, message_subject, content, headerTo,
					replyTo, null);
		}
	}
	
	/**
	 * created based on setTermListForContext - Denny
	 * @param context
	 * @param state
	 */
	private void setTemplateListForContext(Context context, SessionState state)
	{   
		Hashtable<String, List<Site>> templateList = new Hashtable<String, List<Site>>();
		
		// find all template sites.
		// need to have a default OOTB template site definition to faciliate testing without changing the sakai.properties file.
		String[] siteTemplates = siteTemplates = StringUtil.split(ServerConfigurationService.getString("site.templates", "template"), ",");
		
		for (String siteTemplateId:siteTemplates) {
			try
			{
				Site siteTemplate = SiteService.getSite(siteTemplateId);
				if (siteTemplate != null)
				{
					// get the type of template
					String type = siteTemplate.getType();
					if (type != null)
					{
						// populate the list according to template site type
						List<Site> subTemplateList = new Vector<Site>();
						if (templateList.containsKey(type))
						{
							subTemplateList = templateList.get(type);
						}
						subTemplateList.add(siteTemplate);
						templateList.put(type, subTemplateList);
					}
				}
			}
			catch (IdUnusedException e)
			{
				M_log.info(this + ".setTemplateListForContext: cannot find site with id " + siteTemplateId);
			}
		}
		
	    context.put("templateList", templateList);
	} // setTemplateListForContext
	
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
			M_log.warn(this + ".sitePropertiesIntoState: " + e.getMessage(), e);
		}

	} // sitePropertiesIntoState

	/**
	 * pageMatchesPattern returns tool id if a SitePage matches a WorkSite Setuppattern
	 * otherwise return null
	 * @param state
	 * @param page
	 * @return
	 */
	private String pageMatchesPattern(SessionState state, SitePage page) {
		List pageToolList = page.getTools();

		// if no tools on the page, return false
		if (pageToolList == null || pageToolList.size() == 0) {
			return null;
		}

		// don't compare tool properties, which may be changed using Options
		List toolList = new Vector();
		int count = pageToolList.size();
		
		// check Home tool first
		if (pageHasToolId(pageToolList, getHomeToolId(state))) 
			return getHomeToolId(state);

		// Other than Home page, no other page is allowed to have more than one tool within. Otherwise, WSetup/Site Info tool won't handle it
		if (count != 1)
		{
			return null;
		}
		// if the page layout doesn't match, return false
		else if (page.getLayout() != SitePage.LAYOUT_SINGLE_COL) {
			return null;
		}
		else
		{
			// for the case where the page has one tool
			ToolConfiguration toolConfiguration = (ToolConfiguration) pageToolList.get(0);
			
			toolList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
	
			if (pageToolList != null || pageToolList.size() != 0) {
				// if tool attributes don't match, return false
				String match = null;
				for (ListIterator i = toolList.listIterator(); i.hasNext();) {
					MyTool tool = (MyTool) i.next();
					if (toolConfiguration.getTitle() != null) {
						if (toolConfiguration.getTool() != null
								&& toolConfiguration.getTool().getId().indexOf(
										tool.getId()) != -1) {
							match = tool.getId();
						}
					}
				}
				return match;
			}
		}
		
		return null;

	} // pageMatchesPattern


	/**
	 * check whether the page tool list contains certain toolId
	 * @param pageToolList
	 * @param toolId
	 * @return
	 */
	private boolean pageHasToolId(List pageToolList, String toolId) {
		for (Iterator iPageToolList = pageToolList.iterator(); iPageToolList.hasNext();)
		{
			ToolConfiguration toolConfiguration = (ToolConfiguration) iPageToolList.next();
			Tool t = toolConfiguration.getTool();
			if (t != null && toolId.equals(toolConfiguration.getTool().getId()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * siteToolsIntoState is the replacement for siteToolsIntoState_ Make a list
	 * of pages and tools that match WorkSite Setup configurations into state
	 */
	private void siteToolsIntoState(SessionState state) {
		// get the map of titles of multiple tool instances
		Map multipleToolIdTitleMap = state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP) != null? (Map) state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP):new HashMap();
		
		String wSetupTool = NULL_STRING;
		List wSetupPageList = new Vector();
		Site site = getStateSite(state);
		List pageList = site.getPages();

		// Put up tool lists filtered by category
		String type = checkNullSiteType(state, site);
		if (type == null) {
			M_log.warn(this + ": - unknown STATE_SITE_TYPE");
		} else {
			state.setAttribute(STATE_SITE_TYPE, type);
		}
		
		// set tool registration list
		setToolRegistrationList(state, type);
		List toolRegList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		
		// for the selected tools
		boolean check_home = false;
		Vector idSelected = new Vector();
		if (!((pageList == null) || (pageList.size() == 0))) {
			for (ListIterator i = pageList.listIterator(); i.hasNext();) {
				SitePage page = (SitePage) i.next();
				// collect the pages consistent with Worksite Setup patterns
				wSetupTool = pageMatchesPattern(state, page);
				if (wSetupTool != null) {
					if (wSetupTool.equals(getHomeToolId(state)))
					{
						check_home = true;
					}
					else 
					{
						if (isMultipleInstancesAllowed(findOriginalToolId(state, wSetupTool)))
						{
							String mId = page.getId() + wSetupTool;
							idSelected.add(mId);
							multipleToolIdTitleMap.put(mId, page.getTitle());

							MyTool newTool = new MyTool();
							newTool.title = ToolManager.getTool(wSetupTool).getTitle();
							newTool.id = mId;
							newTool.selected = false;

							boolean hasThisMultipleTool = false;
							int j = 0;
							for (; j < toolRegList.size() && !hasThisMultipleTool; j++) {
								MyTool t = (MyTool) toolRegList.get(j);
								if (t.getId().equals(wSetupTool)) {
									hasThisMultipleTool = true;
									newTool.description = t.getDescription();
								}
							}
							if (hasThisMultipleTool) {
								toolRegList.add(j - 1, newTool);
							} else {
								toolRegList.add(newTool);
							}
						}
						else
						{
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
		
		state.setAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, multipleToolIdTitleMap);
		state.setAttribute(STATE_TOOL_HOME_SELECTED, new Boolean(check_home));
		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, idSelected); // List
		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, toolRegList);
		// of
		// ToolRegistration
		// toolId's
		state.setAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST,idSelected); // List of ToolRegistration toolId's
		state.setAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME, Boolean.valueOf(check_home));
		state.setAttribute(STATE_WORKSITE_SETUP_PAGE_LIST, wSetupPageList);

	} // siteToolsIntoState

	/**
	 * adjust site type
	 * @param state
	 * @param site
	 * @return
	 */
	private String checkNullSiteType(SessionState state, Site site) {
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
		return type;
	}

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
		state.removeAttribute(STATE_WORKSITE_SETUP_PAGE_LIST);
		state.removeAttribute(STATE_MULTIPLE_TOOL_ID_SET);
		//state.removeAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP);
	}

	private List orderToolIds(SessionState state, String type, List toolIdList) {
		List rv = new Vector();
		if (state.getAttribute(STATE_TOOL_HOME_SELECTED) != null
				&& ((Boolean) state.getAttribute(STATE_TOOL_HOME_SELECTED))
						.booleanValue()) {
			rv.add(getHomeToolId(state));
		}

		// look for null site type
		if (type == null && state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null)
		{
			type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);
		}
		
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
					String rToolId = originalToolId(toolId, tool_id);
					if (rToolId != null)
					{
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
		participant.active = true;
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

		// to reset the state variable of the multiple tool instances
		Set multipleToolIdSet = state.getAttribute(STATE_MULTIPLE_TOOL_ID_SET) != null? (Set) state.getAttribute(STATE_MULTIPLE_TOOL_ID_SET):new HashSet();
		// get the map of titles of multiple tool instances
		Map multipleToolIdTitleMap = state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP) != null? (Map) state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP):new HashMap();
		
		// editing existing site or creating a new one?
		Site site = getStateSite(state);
		
		// dispatch
		if (option.startsWith("add_")) {
			// this could be format of originalToolId plus number of multiplication
			String addToolId = option.substring("add_".length(), option.length());

			// find the original tool id
			String originToolId = findOriginalToolId(state, addToolId);
			if (originToolId != null)
			{
				Tool tool = ToolManager.getTool(originToolId);
				if (tool != null)
				{
					insertTool(state, originToolId, tool.getTitle(), tool.getDescription(), Integer.parseInt(params.getString("num_"+ addToolId)));
					updateSelectedToolList(state, params, false);
					state.setAttribute(STATE_TEMPLATE_INDEX, "26");
				}
			}
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
			updateSelectedToolList(state, params, false);
			doContinue(data);
		} else if (option.equalsIgnoreCase("back")) {
			// back
			doBack(data);
		} else if (option.equalsIgnoreCase("cancel")) {
			if (site == null)
			{
				// cancel
				doCancel_create(data);
			}
			else
			{
				// cancel editing
				doCancel(data);
			}
		}

	} // doAdd_features

	/**
	 * update the selected tool list
	 * 
	 * @param params
	 *            The ParameterParser object
	 * @param updateConfigVariables
	 * 			  Need to update configuration variables
	 */
	private void updateSelectedToolList(SessionState state, ParameterParser params, boolean updateConfigVariables) {
		List selectedTools = new ArrayList(Arrays.asList(params
				.getStrings("selectedTools")));
		Set multipleToolIdSet = (Set) state.getAttribute(STATE_MULTIPLE_TOOL_ID_SET);
		Map multipleToolIdTitleMap = state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP) != null? (Map) state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP):new HashMap();
		Hashtable<String, Hashtable<String, String>> multipleToolConfiguration = state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION) != null?(Hashtable<String, Hashtable<String, String>>) state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION):new Hashtable<String, Hashtable<String, String>>();
		Vector<String> idSelected = (Vector<String>) state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		boolean has_home = false;
		String emailId = null;

		for (int i = 0; i < selectedTools.size(); i++) 
		{
			String id = (String) selectedTools.get(i);
			if (id.equalsIgnoreCase(getHomeToolId(state))) {
				has_home = true;
			} else if (id.equalsIgnoreCase("sakai.mailbox")) {
				if ( updateConfigVariables ) {
					// if Email archive tool is selected, check the email alias
					emailId = StringUtil.trimToNull(params.getString("emailId"));
					
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

					state.setAttribute(STATE_TOOL_EMAIL_ADDRESS, emailId);
				}
			}
			else if (isMultipleInstancesAllowed(findOriginalToolId(state, id)) && (idSelected != null && !idSelected.contains(id) || idSelected == null) && updateConfigVariables)
			{
				// newly added mutliple instances
				String title = StringUtil.trimToNull(params.getString("title_" + id));
				if (title != null) 
				{
					// save the titles entered
					multipleToolIdTitleMap.put(id, title);
				}
				
				// get the attribute input
				Hashtable<String, String> attributes = multipleToolConfiguration.get(id);
				if (attributes == null)
				{
					// if missing, get the default setting for original id
					attributes = multipleToolConfiguration.get(findOriginalToolId(state, id));
				}
				
				if (attributes != null)
				{
					for(Enumeration<String> e = attributes.keys(); e.hasMoreElements();)
					{
						String attribute = e.nextElement();
						String attributeInput = StringUtil.trimToNull(params.getString(attribute + "_" + id));
						if (attributeInput != null)
						{
							// save the attribute input
							attributes.put(attribute, attributeInput);
						}
					}
					multipleToolConfiguration.put(id, attributes);
				}

				// update the state objects
				state.setAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, multipleToolIdTitleMap);
				state.setAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION, multipleToolConfiguration);
			}
		}
		
		state.setAttribute(STATE_TOOL_HOME_SELECTED, new Boolean(has_home));
	} // updateSelectedToolList

	/**
	 * find the tool in the tool list and insert another tool instance to the list
	 * @param state
	 * @param toolId
	 * @param defaultTitle
	 * @param defaultDescription
	 * @param insertTimes
	 */
	private void insertTool(SessionState state, String toolId, String defaultTitle, String defaultDescription, int insertTimes) {
		// the list of available tools
		List toolList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		List oTools = state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST) == null? new Vector():(List) state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		
		// get the map of titles of multiple tool instances
		Map multipleToolIdTitleMap = state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP) != null? (Map) state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP):new HashMap();
		// get the attributes of multiple tool instances
		Hashtable<String, Hashtable<String, String>> multipleToolConfiguration = state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION) != null?(Hashtable<String, Hashtable<String, String>>) state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION):new Hashtable<String, Hashtable<String, String>>();
		
		int toolListedTimes = 0;
		int index = 0;
		int insertIndex = 0;
		while (index < toolList.size()) {
			MyTool tListed = (MyTool) toolList.get(index);
			if (tListed.getId().indexOf(toolId) != -1 && !oTools.contains(tListed.getId())) {
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

		// insert multiple tools
		for (int i = 0; i < insertTimes; i++) {
			toolSelected.add(toolId + toolListedTimes);

			// We need to insert a specific tool entry only if all the specific
			// tool entries have been selected
			String newToolId = toolId + toolListedTimes;
			MyTool newTool = new MyTool();
			newTool.title = defaultTitle;
			newTool.id = newToolId;
			newTool.description = defaultDescription;
			toolList.add(insertIndex, newTool);
			toolListedTimes++;
			
			// add title
			multipleToolIdTitleMap.put(newTool.id, defaultTitle);
			
			// get the attribute input
			Hashtable<String, String> attributes = multipleToolConfiguration.get(newToolId);
			if (attributes == null)
			{
				// if missing, get the default setting for original id
				attributes = new Hashtable<String, String>();
				
				Hashtable<String, String> oAttributes = multipleToolConfiguration.get(findOriginalToolId(state, newToolId));
				// add the entry for the newly added tool
				if (attributes != null)
				{
					attributes = (Hashtable<String, String>) oAttributes.clone();
					multipleToolConfiguration.put(newToolId, attributes);
				}
			}
		}

		state.setAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, multipleToolIdTitleMap);
		state.setAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION, multipleToolConfiguration);
		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, toolList);
		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, toolSelected);

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
		
		public String url_alias = NULL_STRING; // the url alias for the site

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

		public void setUrlAlias(String urlAlias) {
			this.url_alias = urlAlias;
		}

		public String getUrlAlias() {
			return url_alias;
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
		int index = Integer.valueOf(params.getString("templateIndex"))
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
			M_log.warn(this + ".addGradToolsFeatures:" + e.getMessage() + SITE_GTS_TEMPLATE, e);
		}
		if (template != null) {
			// create a new site based on the template
			try {
				edit = SiteService.addSite(id, template);
			} catch (Exception e) {
				M_log.warn(this + ".addGradToolsFeatures:" + " add/edit site id=" + id, e);
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
					M_log.warn(this + ".addGradToolsFeatures:" + " commitEdit site id=" + id, e);
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
					addAlert(state, rb.getString("java.alias") + " " + alias + " " + rb.getString("java.exists"));
					M_log.warn(this + ".addGradToolsFeatures:" + rb.getString("java.alias") + " " + alias + " " + rb.getString("java.exists"), ee);
				} catch (IdInvalidException ee) {
					addAlert(state, rb.getString("java.alias") + " " + alias + " " + rb.getString("java.isinval"));
					M_log.warn(this + ".addGradToolsFeatures:" + rb.getString("java.alias") + " " + alias + " " + rb.getString("java.isinval"), ee);
				} catch (PermissionException ee) {
					M_log.warn(this + ".addGradToolsFeatures:" + SessionManager.getCurrentSessionUserId() + " does not have permission to add alias. ", ee);
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
			M_log.warn(this + ".addGradToolsStudentSite:" + e.getMessage(), e);
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
					M_log.warn(this + ".transferCopyEntities: Error encountered while asking EntityTransfer to transferCopyEntities from: "
									+ fromContext + " to: " + toContext, t);
				}
			}
		}
	}

	protected void transferCopyEntitiesMigrate(String toolId, String fromContext,
			String toContext) {
		
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i
				.hasNext();) {
			EntityProducer ep = (EntityProducer) i.next();
			if (ep instanceof EntityTransferrer) {
				try {
					EntityTransferrer et = (EntityTransferrer) ep;

					// if this producer claims this tool id
					if (ArrayUtil.contains(et.myToolIds(), toolId)) {
						et.transferCopyEntities(fromContext, toContext,
								new Vector(), true);
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
	protected List getToolsAvailableForImport(SessionState state, List<String> toolIdList) {
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
		
		if (displayWebContent && !toolIdList.contains("sakai.iframe"))
			toolIdList.add("sakai.iframe");
		if (displayNews && !toolIdList.contains("sakai.news"))
			toolIdList.add("sakai.news");

		return toolIdList;
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
			M_log.warn(this + ".getCourseOfferingAndSectionMap:" + " cannot find section id=" + sectionEid, e);
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
			M_log.warn(this + ".getRolesAllowedToAttachSection: Could not find authz group " + azgId, e);
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

	private List<String> getCMLevelLabels() {
		List<String> rv = new Vector<String>();
		Set courseSets = cms.getCourseSets();
		String currentLevel = "";
		rv = addCategories(rv, courseSets);
		
		// course and section exist in the CourseManagementService
		rv.add(rb.getString("cm.level.course"));
		rv.add(rb.getString("cm.level.section"));
		return rv;
	}


	/**
	 * a recursive function to add courseset categories
	 * @param rv
	 * @param courseSets
	 */
	private List<String> addCategories(List<String> rv, Set courseSets) {
		if (courseSets != null)
		{
			for (Iterator i = courseSets.iterator(); i.hasNext();)
			{
				// get the CourseSet object level
				CourseSet cs = (CourseSet) i.next();
				String level = cs.getCategory();
				if (!rv.contains(level))
				{
					rv.add(level);
				}
				try
				{
					// recursively add child categories
					rv = addCategories(rv, cms.getChildCourseSets(cs.getEid()));
				}
				catch (IdNotFoundException e)
				{
					// current CourseSet not found
				}
			}
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
							addAlert(state, rb.getString("java.validAuthor1") + " " 
									+ ServerConfigurationService.getString("officialAccountName") + " " + rb.getString("java.validAuthor2"));
							M_log.warn(this + ".doFind_course:" + rb.getString("java.validAuthor1") + " " 
									+ ServerConfigurationService.getString("officialAccountName") + " " + rb.getString("java.validAuthor2"), e);
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

		int cmLevel = getCMLevelLabels().size();
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
	
	
	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
	throws ToolException
	{
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		SessionState state = getState(req);
 
		if (SITE_MODE_HELPER_DONE.equals(state.getAttribute(STATE_SITE_MODE)))
		{
			String url = (String) SessionManager.getCurrentToolSession().getAttribute(Tool.HELPER_DONE_URL);

			SessionManager.getCurrentToolSession().removeAttribute(Tool.HELPER_DONE_URL);

			// TODO: Implement cleanup.
			cleanState(state);
			// Helper cleanup.

			cleanStateHelper(state);
			
			if (M_log.isDebugEnabled())
			{
				M_log.debug("Sending redirect to: "+ url);
			}
			try
			{
				res.sendRedirect(url);
			}
			catch (IOException e)
			{
				M_log.warn("Problem sending redirect to: "+ url,  e);
			}
			return;
		}
		else
		{
			super.toolModeDispatch(methodBase, methodExt, req, res);
		}
	}

	private void cleanStateHelper(SessionState state) {
		state.removeAttribute(STATE_SITE_MODE);
		state.removeAttribute(STATE_TEMPLATE_INDEX);
		state.removeAttribute(STATE_INITIALIZED);
	}

 }
