/**********************************************************************************

 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.site.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.velocity.tools.generic.SortTool;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.archive.cover.ArchiveService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.RoleProvider;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.api.AuthzGroupService;
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
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.entity.api.ContentExistsAware;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.EntityTransferrerRefMigrator;
import org.sakaiproject.entity.api.HardDeleteAware;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportService;
import org.sakaiproject.importer.api.SakaiArchive;
import org.sakaiproject.importer.api.ResetOnCloseInputStream;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteComparator;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.site.util.SiteSetupQuestionFileParser;
import org.sakaiproject.site.util.SiteTextEditUtil;
import org.sakaiproject.site.util.SiteTypeUtil;
import org.sakaiproject.site.util.ToolComparator;
import org.sakaiproject.sitemanage.api.SectionField;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.sitemanage.api.model.SiteSetupQuestion;
import org.sakaiproject.sitemanage.api.model.SiteSetupQuestionAnswer;
import org.sakaiproject.sitemanage.api.model.SiteSetupUserAnswer;
import org.sakaiproject.sitemanage.api.model.SiteTypeQuestions;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;
import org.sakaiproject.util.*;
// for basiclti integration


/**
 * <p>
 * SiteAction controls the interface for worksite setup.
 * </p>
 */
public class SiteAction extends PagedResourceActionII {
	// SAK-23491 add template_used property
	private static final String TEMPLATE_USED = "template_used";
	
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SiteAction.class);
	
	private LTIService m_ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
	private ContentHostingService m_contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");

	private ImportService importService = org.sakaiproject.importer.cover.ImportService
			.getInstance();

	private static String showOrphanedMembers = ServerConfigurationService.getString("site.setup.showOrphanedMembers", "admins");

	/** portlet configuration parameter values* */
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("sitesetupgeneric");
	private static ResourceLoader cfgRb = new ResourceLoader("multipletools");

	private Locale comparator_locale = rb.getLocale();	
	
	private org.sakaiproject.user.api.UserDirectoryService userDirectoryService = (org.sakaiproject.user.api.UserDirectoryService) ComponentManager.get(
			org.sakaiproject.user.api.UserDirectoryService.class );
	
	private org.sakaiproject.coursemanagement.api.CourseManagementService cms = (org.sakaiproject.coursemanagement.api.CourseManagementService) ComponentManager
			.get(org.sakaiproject.coursemanagement.api.CourseManagementService.class);

	private org.sakaiproject.authz.api.GroupProvider groupProvider = (org.sakaiproject.authz.api.GroupProvider) ComponentManager
			.get(org.sakaiproject.authz.api.GroupProvider.class);

	private AuthzGroupService authzGroupService = ComponentManager.get(AuthzGroupService.class);

	private org.sakaiproject.archive.api.ArchiveService archiveService = (org.sakaiproject.archive.api.ArchiveService) ComponentManager
			.get(org.sakaiproject.archive.api.ArchiveService.class);
	
	private org.sakaiproject.sitemanage.api.SectionFieldProvider sectionFieldProvider = (org.sakaiproject.sitemanage.api.SectionFieldProvider) ComponentManager
			.get(org.sakaiproject.sitemanage.api.SectionFieldProvider.class);
	
	private org.sakaiproject.sitemanage.api.AffiliatedSectionProvider affiliatedSectionProvider = (org.sakaiproject.sitemanage.api.AffiliatedSectionProvider) ComponentManager
	.get(org.sakaiproject.sitemanage.api.AffiliatedSectionProvider.class);
	
	private org.sakaiproject.sitemanage.api.SiteTypeProvider siteTypeProvider = (org.sakaiproject.sitemanage.api.SiteTypeProvider) ComponentManager
	.get(org.sakaiproject.sitemanage.api.SiteTypeProvider.class);

	private AliasService aliasService = ComponentManager.get(AliasService.class);
	
	private static org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService questionService = (org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService) ComponentManager
	.get(org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService.class);
	
	private static org.sakaiproject.sitemanage.api.UserNotificationProvider userNotificationProvider = (org.sakaiproject.sitemanage.api.UserNotificationProvider) ComponentManager
	.get(org.sakaiproject.sitemanage.api.UserNotificationProvider.class);
	
	private static ShortenedUrlService shortenedUrlService = (ShortenedUrlService) ComponentManager.get(ShortenedUrlService.class);

	private static final String SITE_MODE_SITESETUP = "sitesetup";

	private static final String SITE_MODE_SITEINFO = "siteinfo";
	
	private static final String SITE_MODE_HELPER = "helper";
	
	private static final String SITE_MODE_HELPER_DONE = "helper.done";

	private static final String STATE_SITE_MODE = "site_mode";
	
	private static final String TERM_OPTION_ALL = "-1";

	protected final static String[] TEMPLATE = {
			"-list",// 0
			"-type",
			"",// combined with 13
			"",// chef_site-editFeatures.vm deleted. Functions are combined with chef_site-editToolGroupFeatures.vm
			"-editToolGroupFeatures",
			"",// moved to participant helper
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
			"-type-confirm",// 42
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
			"-importSitesMigrate",  //60
			"-siteInfo-importUser",
			"-uploadArchive"
	};

	/** Name of state attribute for Site instance id */
	private static final String STATE_SITE_INSTANCE_ID = "site.instance.id";

	/** Name of state attribute for Site Information */
	private static final String STATE_SITE_INFO = "site.info";

	private static final String STATE_SITE_TYPE = "site-type";

	/** Name of state attribute for possible site types */
	private static final String STATE_SITE_TYPES = "site_types";

	private static final String STATE_DEFAULT_SITE_TYPE = "default_site_type";

	private static final String STATE_PUBLIC_CHANGEABLE_SITE_TYPES = "changeable_site_types";

	private static final String STATE_PUBLIC_SITE_TYPES = "public_site_types";

	private static final String STATE_PRIVATE_SITE_TYPES = "private_site_types";

	private static final String STATE_DISABLE_JOINABLE_SITE_TYPE = "disable_joinable_site_types";

	
	private final static String PROP_SITE_LANGUAGE = "locale_string";
	
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
	
	private static final String STATE_TERM_VIEW_SELECTED = "site.termview.selected";

	/*******************
	 *  SAK-16600
	 *
	 * Refactor methods to update these values in state/context
	 */

	/** Names of lists related to tools groups */
	private static final String STATE_TOOL_GROUP_LIST = "toolsByGroup";

	/** Names of lists related to tools groups */
	private static final String STATE_TOOL_GROUP_MULTIPLES = "toolGroupMultiples";

	/** Names of lists related to tools */
	private static final String STATE_TOOL_REGISTRATION_LIST = "toolRegistrationList";
	
	private static final String STATE_TOOL_REGISTRATION_TITLE_LIST = "toolRegistrationTitleList";

	private static final String STATE_TOOL_REGISTRATION_SELECTED_LIST = "toolRegistrationSelectedList";

	private static final String STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST = "toolRegistrationOldSelectedList";

	private static final String STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME = "toolRegistrationOldSelectedHome";

	private static final String STATE_EXTRA_SELECTED_TOOL_LIST = "extraSelectedToolList";

	private static final String STATE_TOOL_HOME_SELECTED = "toolHomeSelected";

	private static final String UNGROUPED_TOOL_TITLE = "systoolgroups.ungrouped";
	private static final String LTI_TOOL_TITLE		 = "systoolgroups.lti";

    //********************

	private static final String STATE_TOOL_EMAIL_ADDRESS = "toolEmailAddress";

	private static final String STATE_PROJECT_TOOL_LIST = "projectToolList";

	private final static String STATE_MULTIPLE_TOOL_ID_SET = "multipleToolIdSet";
	private final static String STATE_MULTIPLE_TOOL_ID_TITLE_MAP = "multipleToolIdTitleMap";
	private final static String STATE_MULTIPLE_TOOL_CONFIGURATION = "multipleToolConfiguration";

	private final static String SITE_DEFAULT_LIST = ServerConfigurationService
			.getString("site.types");

	private final static String DEFAULT_SITE_TYPE_SAK_PROP = ServerConfigurationService.getString("site.types.defaultType", null);
	private final static String[] PUBLIC_CHANGEABLE_SITE_TYPES_SAK_PROP = ServerConfigurationService.getStrings("site.types.publicChangeable");
	private final static String[] PUBLIC_SITE_TYPES_SAK_PROP = ServerConfigurationService.getStrings("site.types.publicOnly");
	private final static String[] PRIVATE_SITE_TYPES_SAK_PROP = ServerConfigurationService.getStrings("site.types.privateOnly");

	private final static String STATE_SITE_QUEST_UNIQNAME = "site_quest_uniqname";
	
	private static final String STATE_SITE_ADD_COURSE = "canAddCourse";
	
	private static final String STATE_SITE_ADD_PORTFOLIO = "canAddPortfolio";
	
	private static final String STATE_PORTFOLIO_SITE_TYPE = "portfolio";
		
	private static final String STATE_SITE_ADD_PROJECT = "canAddProject";
		
	private static final String STATE_PROJECT_SITE_TYPE = "project";
	
	private static final String STATE_SITE_IMPORT_ARCHIVE = "canImportArchive";
	
	// SAK-23468
	private static final String STATE_NEW_SITE_STATUS_ISPUBLISHED = "newSiteStatusIsPublished";
	private static final String STATE_NEW_SITE_STATUS_TITLE = "newSiteStatusTitle";
	private static final String STATE_NEW_SITE_STATUS_ID = "newSiteStatusID";
	private static final String STATE_DUPE_SITE_STATUS_ID = "dupeSiteStatusID";
	private static final String STATE_DUPE_SITE_URL = "dupeSiteUrl";

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

	private final static String FORM_SITE_URL_BASE = "form_site_url_base";
	
	private final static String FORM_SITE_ALIAS = "form_site_alias";
	
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
	
	private final static String FORM_SITEINFO_URL_BASE = "form_site_url_base";
	
	private final static String FORM_SITEINFO_ALIASES = "form_site_aliases";

	private final static String FORM_WILL_NOTIFY = "form_will_notify";

	/** Context action */
	private static final String CONTEXT_ACTION = "SiteAction";

	/** The name of the Attribute for display template index */
	private static final String STATE_TEMPLATE_INDEX = "site.templateIndex";

	/** The name of the Attribute for display template index */
	private static final String STATE_OVERRIDE_TEMPLATE_INDEX = "site.overrideTemplateIndex";

	/** The name of the Attribute to indicate we are operating in shortcut mode */
	private static final String STATE_IN_SHORTCUT = "site.currentlyInShortcut";

	/** State attribute for state initialization. */
	private static final String STATE_INITIALIZED = "site.initialized";

	/** State attributes for using templates in site creation. */
	private static final String STATE_TEMPLATE_SITE = "site.templateSite";
	private static final String STATE_TEMPLATE_SITE_COPY_USERS = "site.templateSiteCopyUsers";
	private static final String STATE_TEMPLATE_SITE_COPY_CONTENT = "site.templateSiteCopyContent";
	private static final String STATE_TEMPLATE_PUBLISH = "site.templateSitePublish";

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
	
	private static final String STATE_SITE_ACCESS_PUBLISH = "state_site_access_publish";
	
	private static final String STATE_SITE_ACCESS_INCLUDE = "state_site_access_include";

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
	
	private final static String STATE_ADD_CLASS_PROVIDER_DESCRIPTION_CHOSEN = "state_add_class_provider_description_chosen";

	private final static String STATE_ADD_CLASS_MANUAL = "state_add_class_manual";

	private final static String STATE_AUTO_ADD = "state_auto_add";

	private final static String STATE_MANUAL_ADD_COURSE_NUMBER = "state_manual_add_course_number";

	private final static String STATE_MANUAL_ADD_COURSE_FIELDS = "state_manual_add_course_fields";

	public final static String PROP_SITE_REQUEST_COURSE = "site-request-course-sections";

	public final static String SITE_PROVIDER_COURSE_LIST = "site_provider_course_list";

	public final static String SITE_MANUAL_COURSE_LIST = "site_manual_course_list";

	private final static String STATE_SUBJECT_AFFILIATES = "site.subject.affiliates";

	private final static String STATE_ICONS = "icons";

	public static final String SITE_DUPLICATED = "site_duplicated";

	public static final String SITE_DUPLICATED_NAME = "site_duplicated_named";

	// types of site whose title can not be editable
	public static final String TITLE_NOT_EDITABLE_SITE_TYPE = "title_not_editable_site_type";
	
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
	
	/**
	 * {@link org.sakaiproject.component.api.ServerConfigurationService} property.
	 * If <code>false</code>, ensures that a site's joinability settings are not affected should
	 * that site be <em>edited</em> after its type has been enumerated by a
	 * "wsetup.disable.joinable" property. Code should cause this prop value to
	 * default to <code>true</code> to preserve backward compatibility. Property
	 * naming tries to match mini-convention established by "wsetup.disable.joinable"
	 * (which has no corresponding constant).
	 * 
	 * <p>Has no effect on the site creation process -- only site editing</p>
	 * 
	 * @see #doUpdate_site_access_joinable(RunData, SessionState, ParameterParser, Site)
	 */
	public static final String CONVERT_NULL_JOINABLE_TO_UNJOINABLE = "wsetup.convert.null.joinable.to.unjoinable";
	
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
	// used in the configuration file to specify the default tool title 
	private String CONFIG_TOOL_TITLE = "wsetup.config.tool.title_";
	
	// home tool id
	private static final String TOOL_ID_HOME = "home";
	// Site Info tool id
	private static final String TOOL_ID_SITEINFO = "sakai.siteinfo";
	
	// synoptic tool ids
	private static final String TOOL_ID_SUMMARY_CALENDAR = "sakai.summary.calendar";
	private static final String TOOL_ID_SYNOPTIC_ANNOUNCEMENT = "sakai.synoptic.announcement";
	private static final String TOOL_ID_SYNOPTIC_CHAT = "sakai.synoptic.chat";
	private static final String TOOL_ID_SYNOPTIC_MESSAGECENTER = "sakai.synoptic.messagecenter";
	private static final String TOOL_ID_SYNOPTIC_DISCUSSION = "sakai.synoptic.discussion";
	
	private static final String IMPORT_QUEUED = "import.queued";
	
	// map of synoptic tool and the related tool ids
	private final static Map<String, List<String>> SYNOPTIC_TOOL_ID_MAP;
	static
	{
		SYNOPTIC_TOOL_ID_MAP = new HashMap<String, List<String>>();
		SYNOPTIC_TOOL_ID_MAP.put(TOOL_ID_SUMMARY_CALENDAR, new ArrayList(Arrays.asList("sakai.schedule")));
		SYNOPTIC_TOOL_ID_MAP.put(TOOL_ID_SYNOPTIC_ANNOUNCEMENT, new ArrayList(Arrays.asList("sakai.announcements")));
		SYNOPTIC_TOOL_ID_MAP.put(TOOL_ID_SYNOPTIC_CHAT, new ArrayList(Arrays.asList("sakai.chat")));
		SYNOPTIC_TOOL_ID_MAP.put(TOOL_ID_SYNOPTIC_MESSAGECENTER, new ArrayList(Arrays.asList("sakai.messages", "sakai.forums", "sakai.messagecenter")));
		SYNOPTIC_TOOL_ID_MAP.put(TOOL_ID_SYNOPTIC_DISCUSSION, new ArrayList(Arrays.asList("sakai.discussion")));
	}
	
	// map of synoptic tool and message bundle properties, used to lookup an internationalized tool title
	private final static Map<String, String> SYNOPTIC_TOOL_TITLE_MAP;
	static
	{
		SYNOPTIC_TOOL_TITLE_MAP = new HashMap<String, String>();
		SYNOPTIC_TOOL_TITLE_MAP.put(TOOL_ID_SUMMARY_CALENDAR, "java.reccal");
		SYNOPTIC_TOOL_TITLE_MAP.put(TOOL_ID_SYNOPTIC_ANNOUNCEMENT, "java.recann");
		SYNOPTIC_TOOL_TITLE_MAP.put(TOOL_ID_SYNOPTIC_CHAT, "java.recent");
		SYNOPTIC_TOOL_TITLE_MAP.put(TOOL_ID_SYNOPTIC_MESSAGECENTER, "java.recmsg");
		SYNOPTIC_TOOL_TITLE_MAP.put(TOOL_ID_SYNOPTIC_DISCUSSION, "java.recdisc");
	}
	
	/** the web content tool id **/
	private final static String WEB_CONTENT_TOOL_ID = "sakai.iframe";
	private final static String SITE_INFO_TOOL_ID = "sakai.iframe.site";
	private final static String WEB_CONTENT_TOOL_SOURCE_CONFIG = "source";
	private final static String WEB_CONTENT_TOOL_SOURCE_CONFIG_VALUE = "http://";

	/** the news tool **/
	private final static String NEWS_TOOL_ID = "sakai.simple.rss";
	private final static String NEWS_TOOL_CHANNEL_CONFIG = "javax.portlet-feed_url";
	private final static String NEWS_TOOL_CHANNEL_CONFIG_VALUE = "http://sakaiproject.org/feed";
	
   	private final static String LESSONS_TOOL_ID = "sakai.lessonbuildertool";

	private final static int UUID_LENGTH = 36;
	
	/** the course set definition from CourseManagementService **/
	private final static String STATE_COURSE_SET = "state_course_set";
	
	// the maximum tool title length enforced in UI
	private final static int MAX_TOOL_TITLE_LENGTH = 20;
	
	private final static String SORT_KEY_SESSION = "worksitesetup.sort.key.session";
	private final static String SORT_ORDER_SESSION = "worksitesetup.sort.order.session";
	private final static String SORT_KEY_COURSE_SET = "worksitesetup.sort.key.courseSet";
	private final static String SORT_ORDER_COURSE_SET = "worksitesetup.sort.order.courseSet";
	private final static String SORT_KEY_COURSE_OFFERING = "worksitesetup.sort.key.courseOffering";
	private final static String SORT_ORDER_COURSE_OFFERING = "worksitesetup.sort.order.courseOffering";
	private final static String SORT_KEY_SECTION = "worksitesetup.sort.key.section";
	private final static String SORT_ORDER_SECTION = "worksitesetup.sort.order.section";
	
	// bjones86 - SAK-23255
	private final static String CONTEXT_IS_ADMIN = "isAdmin";
	private final static String CONTEXT_SKIP_MANUAL_COURSE_CREATION = "skipManualCourseCreation";
	private final static String CONTEXT_SKIP_COURSE_SECTION_SELECTION = "skipCourseSectionSelection";
	private final static String CONTEXT_FILTER_TERMS = "filterTerms";
	private final static String SAK_PROP_SKIP_MANUAL_COURSE_CREATION = "wsetup.skipManualCourseCreation";
	private final static String SAK_PROP_SKIP_COURSE_SECTION_SELECTION = "wsetup.skipCourseSectionSelection";
	
	//SAK-22432 Template descriptions are not copied
	private final static String SAK_PROP_COPY_TEMPLATE_DESCRIPTION = "site.setup.copy.template.description";

	//Setup property to require (or not require) authorizer
	private static final String SAK_PROP_REQUIRE_AUTHORIZER = "wsetup.requireAuthorizer";
	//Setup property to email authorizer (default to true)
	private static final String SAK_PROP_EMAIL_AUTHORIZER = "wsetup.emailAuthorizer";

	private List prefLocales = new ArrayList();
	
	private static final String VM_ALLOWED_ROLES_DROP_DOWN 	= "allowedRoles";
	
	// bjones86 - SAK-23256
	private static final String SAK_PROP_FILTER_TERMS = "worksitesetup.filtertermdropdowns";
	private static final String CONTEXT_HAS_TERMS = "hasTerms";
	
	private static final String SAK_PROP_AUTO_FILTER_TERM = "site.setup.autoFilterTerm";

	private static final String SAK_PROP_IMPORT_NOTIFICATION = "site.setup.import.notification";
	
	// state variable for whether any multiple instance tool has been selected
	private String STATE_MULTIPLE_TOOL_INSTANCE_SELECTED = "state_multiple_tool_instance_selected";
	// state variable for lti tools
	private String STATE_LTITOOL_LIST = "state_ltitool_list";
	// state variable for selected lti tools in site
	private String STATE_LTITOOL_EXISTING_SELECTED_LIST = "state_ltitool_existing_selected_list";
	// state variable for selected lti tools during tool modification
	private String STATE_LTITOOL_SELECTED_LIST = "state_ltitool_selected_list";
	// special prefix String for basiclti tool ids
	private String LTITOOL_ID_PREFIX = "lti_";
	
	private String m_filePath;
	private String moreInfoPath;
	private String libraryPath;
	
	private static final String STATE_HARD_DELETE = "hardDelete";
	
	private static final String STATE_CREATE_FROM_ARCHIVE = "createFromArchive";
	private static final String STATE_UPLOADED_ARCHIVE_PATH = "uploadedArchivePath";
	private static final String STATE_UPLOADED_ARCHIVE_NAME = "uploadedArchiveNAme";
	
	private static UserAuditRegistration userAuditRegistration = (UserAuditRegistration) ComponentManager.get("org.sakaiproject.userauditservice.api.UserAuditRegistration.sitemanage");
	private static UserAuditService userAuditService = (UserAuditService) ComponentManager.get(UserAuditService.class);
	
	private PrivacyManager privacyManager = (PrivacyManager) ComponentManager.get(PrivacyManager.class);
	
	// SAK-28990 - enable/disable continue with no roster
	private static final String VM_CONT_NO_ROSTER_ENABLED = "contNoRosterEnabled";
	private static final String SAK_PROP_CONT_NO_ROSTER_ENABLED = "sitemanage.continueWithNoRoster";
	
	private static final String VM_ADD_ROSTER_AUTH_REQUIRED = "authorizationRequired";

	/**
	 * what are the tool ids within Home page?
	 * If this is for a newly added Home tool, get the tool ids from template site or system set default
	 * Else if this is an existing Home tool, get the tool ids from the page
	 * @param state
	 * @param newHomeTool
	 * @param homePage
	 * @return
	 */
	private List<String> getHomeToolIds(SessionState state, boolean newHomeTool, SitePage homePage)
	{
		List<String> rv = new Vector<String>();
		
		// if this is a new Home tool page to be added, get the tool ids from definition (template site first, and then configurations)
		Site site  = getStateSite(state);
		
		String siteType = site != null? site.getType() : "";
		
		// First: get the tool ids from configuration files
		// initially by "wsetup.home.toolids" + site type, and if missing, use "wsetup.home.toolids"
		if (ServerConfigurationService.getStrings("wsetup.home.toolids." + siteType) != null) {
			rv = new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("wsetup.home.toolids." + siteType)));
		} else if (ServerConfigurationService.getStrings("wsetup.home.toolids") != null) {
			rv = new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("wsetup.home.toolids")));
		}
		
		// Second: if tool list is empty, get it from the template site settings
		if (rv.isEmpty())
		{
			// template site
			Site templateSite = null;
			String templateSiteId = "";
			
			if (SiteService.isUserSite(site.getId()))
			{
				// myworkspace type site: get user type first, and then get the template site
				try
				{
					User user = UserDirectoryService.getUser(SiteService.getSiteUserId(site.getId()));
					templateSiteId = SiteService.USER_SITE_TEMPLATE + "." + user.getType();
					templateSite = SiteService.getSite(templateSiteId);
				}
				catch (Throwable t)
				{
	
					M_log.debug(this + ": getHomeToolIds cannot find site " + templateSiteId + t.getMessage());
					// use the fall-back, user template site
					try
					{
						templateSiteId = SiteService.USER_SITE_TEMPLATE;
						templateSite = SiteService.getSite(templateSiteId);
					}
					catch (Throwable tt)
					{
						M_log.debug(this + ": getHomeToolIds cannot find site " + templateSiteId + tt.getMessage());
					}
				}
			}
			else
			{
				// not myworkspace site
				// first: see whether it is during site creation process and using a template site
				templateSite = (Site) state.getAttribute(STATE_TEMPLATE_SITE);
				
				if (templateSite == null)
				{
					// second: if no template is chosen by user, then use template based on site type 
					templateSiteId = SiteService.SITE_TEMPLATE + "." + siteType;
					try
					{
						templateSite = SiteService.getSite(templateSiteId);
					}
					catch (Throwable t)
					{
						M_log.debug(this + ": getHomeToolIds cannot find site " + templateSiteId + t.getMessage());
					
						// thrid: if cannot find template site with the site type, use the default template
						templateSiteId = SiteService.SITE_TEMPLATE;
						try
						{
							templateSite = SiteService.getSite(templateSiteId);
						}
						catch (Throwable tt)
						{
							M_log.debug(this + ": getHomeToolIds cannot find site " + templateSiteId + tt.getMessage());
						}			
					}
				}
			}
			if (templateSite != null)
			{
				// get Home page and embedded tool ids
				for (SitePage page: (List<SitePage>)templateSite.getPages())
				{
					String title = page.getTitle();
					
					if (isHomePage(page))
					{
						// found home page, add all tool ids to return value
						for(ToolConfiguration tConfiguration : (List<ToolConfiguration>) page.getTools())
						{
							String toolId = tConfiguration.getToolId();
							if (ToolManager.getTool(toolId) != null)
								rv.add(toolId);
						}
						break;
					}
				}
			}
		}
		
		// Third: if the tool id list is still empty because we cannot find any template site yet, use the default settings
		if (rv.isEmpty())
		{
			if (siteType.equalsIgnoreCase("myworkspace"))
			{
				// first try with MOTD tool
				if (ToolManager.getTool("sakai.motd") != null)
					rv.add("sakai.motd");
				
				if (rv.isEmpty())
				{
					// then try with the myworkspace information tool
					if (ToolManager.getTool("sakai.iframe.myworkspace") != null)
						rv.add("sakai.iframe.myworkspace");
				}
			}
			else
			{
				// try the site information tool
				if (ToolManager.getTool("sakai.iframe.site") != null)
					rv.add("sakai.iframe.site");
			}
			
			// synoptical tools
			if (ToolManager.getTool(TOOL_ID_SUMMARY_CALENDAR) != null)
			{
				rv.add(TOOL_ID_SUMMARY_CALENDAR);
			}
			
			if (ToolManager.getTool(TOOL_ID_SYNOPTIC_ANNOUNCEMENT) != null)
			{
				rv.add(TOOL_ID_SYNOPTIC_ANNOUNCEMENT);
			}
			
			if (ToolManager.getTool(TOOL_ID_SYNOPTIC_CHAT) != null)
			{
				rv.add(TOOL_ID_SYNOPTIC_CHAT);
			}
			if (ToolManager.getTool(TOOL_ID_SYNOPTIC_MESSAGECENTER) != null)
			{
				rv.add(TOOL_ID_SYNOPTIC_MESSAGECENTER);
			}
		}
		
		// Fourth: if this is an existing Home tool page, get any extra tool ids in the page already back to the list
		if (!newHomeTool)
		{
			// found home page, add all tool ids to return value
			for(ToolConfiguration tConfiguration : (List<ToolConfiguration>) homePage.getTools())
			{
				String hToolId = tConfiguration.getToolId();
				if (!rv.contains(hToolId))
				{
					rv.add(hToolId);
				}
			}
		}
		
		return rv;
	}
	

	/*
	 * Configure directory for moreInfo content
	 */
	private String setMoreInfoPath(ServletContext ctx) {
		String rpath = ctx.getRealPath("");
		String ctxPath = ctx.getServletContextName();
		String rserve = StringUtils.remove(rpath,ctxPath);
		return rserve; 
	}

	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet,
			JetspeedRunData rundata) {
		ServletContext ctx = rundata.getRequest().getSession().getServletContext();
		String serverContextPath = ServerConfigurationService.getString("config.sitemanage.moreInfoDir", "library/image/");
		libraryPath = File.separator + serverContextPath;
		moreInfoPath = setMoreInfoPath(ctx) + serverContextPath;
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
		addSiteTypesToStateFromPropertyOrToolReg(state, STATE_PUBLIC_CHANGEABLE_SITE_TYPES,
				PUBLIC_CHANGEABLE_SITE_TYPES_SAK_PROP, "publicChangeableSiteTypes", config);

		// type of sites that are always public
		addSiteTypesToStateFromPropertyOrToolReg(state, STATE_PUBLIC_SITE_TYPES,
				PUBLIC_SITE_TYPES_SAK_PROP, "publicSiteTypes", config);

		// types of sites that are always private
		addSiteTypesToStateFromPropertyOrToolReg(state, STATE_PRIVATE_SITE_TYPES,
				PRIVATE_SITE_TYPES_SAK_PROP, "privateSiteTypes", config);

		// default site type
		if (state.getAttribute(STATE_DEFAULT_SITE_TYPE) == null)
		{
			String defaultType = DEFAULT_SITE_TYPE_SAK_PROP;
			if (defaultType == null) // not set via sakai properties, fall back to tool registration
			{
				defaultType = StringUtils.trimToEmpty(config.getInitParameter("defaultSiteType"));
			}
			state.setAttribute(STATE_DEFAULT_SITE_TYPE, defaultType);
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

		if (state.getAttribute(STATE_TOP_PAGE_MESSAGE) == null) {
			state.setAttribute(STATE_TOP_PAGE_MESSAGE, Integer.valueOf(0));
		}

		// skins if any
		if (state.getAttribute(STATE_ICONS) == null) {
			setupIcons(state);
		}
		
		if (ServerConfigurationService.getStrings("site.type.titleNotEditable") != null) {
			state.setAttribute(TITLE_NOT_EDITABLE_SITE_TYPE, new ArrayList(Arrays
					.asList(ServerConfigurationService
							.getStrings("site.type.titleNotEditable"))));
		} else {
			state.setAttribute(TITLE_NOT_EDITABLE_SITE_TYPE, new ArrayList(Arrays
					.asList(new String[]{ServerConfigurationService
							.getString("courseSiteType", "course")})));
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
	 * If not yet set, adds the given site type list to the state, either from sakai.properties if available, or from the tool registration configuration
	 * 
	 * @param state the session state
	 * @param stateKey state attribute the site type list should be added as
	 * @param typesFromProperty site type list from sakai.properties, may be null if property not set
	 * @param toolRegKey the key for the site type list property in the tool registration
	 * @param config the tool registration configuration
	 */
	private void addSiteTypesToStateFromPropertyOrToolReg(SessionState state, String stateKey, String[] typesFromProperty,
			String toolRegKey, PortletConfig config)
	{
		if (state.getAttribute(stateKey) == null)
		{
			List<String> siteTypes = new ArrayList<>();
			if (typesFromProperty != null) // override tool registration with sakai properties
			{
				siteTypes.addAll(Arrays.asList(typesFromProperty));
			}
			else // sakai property not set, fall back to tool registration
			{
				String toolRegTypes = StringUtils.trimToEmpty(config.getInitParameter(toolRegKey));
				siteTypes.addAll(Arrays.asList(toolRegTypes.split(",")));
			}
			state.setAttribute(stateKey, siteTypes);
		}
	}

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
		// remove the state attributes related to multi-tool selection
		state.removeAttribute(STATE_WORKSITE_SETUP_PAGE_LIST);
		state.removeAttribute(STATE_MULTIPLE_TOOL_ID_SET);
		state.removeAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP);
		state.removeAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION);
		state.removeAttribute(STATE_TOOL_REGISTRATION_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_TITLE_LIST);
		// remove those state attributes related to course site creation
		state.removeAttribute(STATE_TERM_COURSE_LIST);
		state.removeAttribute(STATE_TERM_COURSE_HASH);
		state.removeAttribute(STATE_TERM_SELECTED);
		state.removeAttribute(STATE_INSTRUCTOR_SELECTED);
		state.removeAttribute(STATE_FUTURE_TERM_SELECTED);
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER);
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER_DESCRIPTION_CHOSEN);
		state.removeAttribute(STATE_ADD_CLASS_MANUAL);
		state.removeAttribute(STATE_AUTO_ADD);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_NUMBER);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
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
		// lti tools
		state.removeAttribute(STATE_LTITOOL_EXISTING_SELECTED_LIST);
		state.removeAttribute(STATE_LTITOOL_SELECTED_LIST);

		// bjones86 - SAK-24423 - remove joinable site settings from the state
		JoinableSiteSettings.removeJoinableSiteSettingsFromState( state );

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
	 * Build the context for shortcut display
	 */
	public String buildShortcutPanelContext(VelocityPortlet portlet,
			Context context, RunData data, SessionState state) {
		return buildMainPanelContext(portlet, context, data, state, true);
	}

	/**
	 * Build the context for normal display
	 */
	public String buildMainPanelContext(VelocityPortlet portlet,
			Context context, RunData data, SessionState state) {
		return buildMainPanelContext(portlet, context, data, state, false);
	}

	/**
	 * Build the context for normal/shortcut display and detect switches
	 */
	public String buildMainPanelContext(VelocityPortlet portlet,
			Context context, RunData data, SessionState state,
			boolean inShortcut) {
		rb = new ResourceLoader("sitesetupgeneric");
		context.put("tlang", rb);
		context.put("clang", cfgRb);
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

		ToolSession session = SessionManager.getCurrentToolSession();
		if(session.getAttribute(ATTR_TOP_REFRESH) != null && session.getAttribute(ATTR_TOP_REFRESH).equals(Boolean.TRUE)) {
			session.removeAttribute(ATTR_TOP_REFRESH);
			return "sitesetup/chef_refresh";
		}
		// updatePortlet(state, portlet, data);
		if (state.getAttribute(STATE_INITIALIZED) == null) {
			init(portlet, data, state);
			String overRideTemplate = (String) state.getAttribute(STATE_OVERRIDE_TEMPLATE_INDEX);
			if ( overRideTemplate != null ) {
				state.removeAttribute(STATE_OVERRIDE_TEMPLATE_INDEX);
				state.setAttribute(STATE_TEMPLATE_INDEX, overRideTemplate);
			}
		}

		// Track when we come into Main panel most recently from Shortcut Panel
		// Reset the state and template if we *just* came into Main from Shortcut
		if ( inShortcut ) {
			state.setAttribute(STATE_IN_SHORTCUT, "true");
		} else {
			String fromShortcut = (String) state.getAttribute(STATE_IN_SHORTCUT);
			if ( "true".equals(fromShortcut) ) {
				cleanState(state);
				state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			}
			state.removeAttribute(STATE_IN_SHORTCUT);
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
		if (templateIndices == null)
		{
			templateIndices = new Vector<String>();
		}
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
		String alert=(String)state.getAttribute(STATE_MESSAGE);
		context.put("alertMessage", state.getAttribute(STATE_MESSAGE));
		context.put("siteTextEdit", new SiteTextEditUtil());
		
		//SAK-29525 Open Template list by default when creating site
		context.put("isExpandTemplates", ServerConfigurationService.getBoolean("site.setup.creation.expand.template", false));
		
		// the last visited template index
		if (preIndex != null)
			context.put("backIndex", preIndex);
		
		// SAK-16600 adjust index for toolGroup mode 
		if (index==3) 
			index = 4;
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

		// all site types
		context.put("courseSiteTypeStrings", SiteService.getSiteTypeStrings("course"));
		context.put("portfolioSiteTypeStrings", SiteService.getSiteTypeStrings("portfolio"));
		context.put("projectSiteTypeStrings", SiteService.getSiteTypeStrings("project"));
		
		//can the user create course sites?
		context.put(STATE_SITE_ADD_COURSE, SiteService.allowAddCourseSite());
		
		// can the user create portfolio sites?
		context.put("portfolioSiteType", STATE_PORTFOLIO_SITE_TYPE);
		context.put(STATE_SITE_ADD_PORTFOLIO, SiteService.allowAddPortfolioSite());
		
		// can the user create project sites?
		context.put("projectSiteType", STATE_PROJECT_SITE_TYPE);
		context.put(STATE_SITE_ADD_PROJECT, SiteService.allowAddProjectSite());
		
		// can the user user create sites from archives?
		context.put(STATE_SITE_IMPORT_ARCHIVE, SiteService.allowImportArchiveSite());
		

		
		Site site = getStateSite(state);
		
		List unJoinableSiteTypes = (List) state.getAttribute(STATE_DISABLE_JOINABLE_SITE_TYPE);

		
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

			// Allow a user to see their deleted sites.
			if (ServerConfigurationService.getBoolean("site.soft.deletion", false)) {
				views.put(SiteConstants.SITE_TYPE_DELETED, rb.getString("java.sites.deleted"));
				if (SiteConstants.SITE_TYPE_DELETED.equals((String) state.getAttribute(STATE_VIEW_SELECTED))) {
					context.put("canSeeSoftlyDeletedSites", true);
				}
			}
			
			// top menu bar
			Menu bar = new MenuImpl(portlet, data, (String) state
					.getAttribute(STATE_ACTION));
			context.put("menu", bar);
			if (SiteService.allowAddSite(null)) {
				bar.add(new MenuEntry(rb.getString("java.new"), "doNew_site"));
			}
			bar.add(new MenuEntry(rb.getString("java.revise"), null, true,
					MenuItem.CHECKED_NA, "doGet_site", "sitesForm"));
			bar.add(new MenuEntry(rb.getString("java.delete"), null, true,
					MenuItem.CHECKED_NA, "doMenu_site_delete", "sitesForm"));

			// If we're in the restore view
			context.put("showRestore", SiteConstants.SITE_TYPE_DELETED.equals((String) state.getAttribute(STATE_VIEW_SELECTED)));

			if (SecurityService.isSuperUser()) {
				context.put("superUser", Boolean.TRUE);
			} else {
				context.put("superUser", Boolean.FALSE);
			}
			views.put(SiteConstants.SITE_TYPE_ALL, rb.getString("java.allmy"));
			views.put(SiteConstants.SITE_TYPE_MYWORKSPACE, rb.getFormattedMessage("java.sites", new Object[]{rb.getString("java.my")}));
			for (int sTypeIndex = 0; sTypeIndex < sTypes.size(); sTypeIndex++) {
				String type = (String) sTypes.get(sTypeIndex);
				views.put(type, rb.getFormattedMessage("java.sites", new Object[]{type}));
			}
			List<String> moreTypes = siteTypeProvider.getTypesForSiteList();
			if (!moreTypes.isEmpty())
			{
				for(String mType : moreTypes)
				{
					views.put(mType, rb.getFormattedMessage("java.sites", new Object[]{mType}));
				}
			}
				// Allow SuperUser to see all deleted sites.
				if (ServerConfigurationService.getBoolean("site.soft.deletion", false)) {
					views.put(SiteConstants.SITE_TYPE_DELETED, rb.getString("java.sites.deleted"));
				}

			// default view
			if (state.getAttribute(STATE_VIEW_SELECTED) == null) {
				state.setAttribute(STATE_VIEW_SELECTED, SiteConstants.SITE_TYPE_ALL);
			}

			if (ServerConfigurationService.getBoolean("sitesetup.show.unpublished", false) && !SecurityService.isSuperUser()) {
				views.put(SiteConstants.SITE_ACTIVE, rb.getString("java.myActive"));
				views.put(SiteConstants.SITE_INACTIVE, rb.getString("java.myInactive"));
			}
			
			// sort the keys in the views lookup
			List<String> viewKeys = Collections.list(views.keys());
			Collections.sort(viewKeys);
			context.put("viewKeys", viewKeys);
			context.put("views", views);

			if (state.getAttribute(STATE_VIEW_SELECTED) != null) {
				context.put("viewSelected", (String) state
						.getAttribute(STATE_VIEW_SELECTED));
			}

			//term filter:
			Hashtable termViews = new Hashtable();
			termViews.put(TERM_OPTION_ALL, rb.getString("list.allTerms"));
			
			// bjones86 - SAK-23256
			List<AcademicSession> aSessions = setTermListForContext( context, state, false, false );
			
			if(aSessions != null){
				for(AcademicSession s : aSessions){
					termViews.put(s.getTitle(), s.getTitle());
				}
			}
			
			// sort the keys in the termViews lookup
			List<String> termViewKeys = Collections.list(termViews.keys());
			Collections.sort(termViewKeys);
			context.put("termViewKeys", termViewKeys);
			context.put("termViews", termViews);
						
			// default term view
			if (state.getAttribute(STATE_TERM_VIEW_SELECTED) == null) {
				state.setAttribute(STATE_TERM_VIEW_SELECTED, TERM_OPTION_ALL);
				
				if ( ServerConfigurationService.getBoolean(SAK_PROP_AUTO_FILTER_TERM, Boolean.FALSE)) {
					// SAK-28059 auto filter term to use the most current term
					List<AcademicSession> currentTerms = cms.getCurrentAcademicSessions();
					// current terms are sorted by start date we will just take the first
					if (!currentTerms.isEmpty()) {
						int termIndex = termViewKeys.indexOf(currentTerms.get(0).getTitle());
						if (termIndex > -1) {
							state.setAttribute(STATE_TERM_VIEW_SELECTED, termViewKeys.get(termIndex));
							context.put("viewTermSelected", termViewKeys.get(termIndex));
						}
					}
				}
			}else {
				context.put("viewTermSelected", (String) state
						.getAttribute(STATE_TERM_VIEW_SELECTED));
			}
			
			if(termViews.size() == 1){
				//this means the terms are empty, only the default option exist
				context.put("hideTermFilter", true);
			}else{
				context.put("hideTermFilter", false);
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

			List<Site> allSites = prepPage(state);
						
			state.setAttribute(STATE_SITES, allSites);
			context.put("sites", allSites);

			context.put("totalPageNumber", Integer.valueOf(totalPageNumber(state)));
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
			context.put("sortby_id", SortType.ID_ASC.toString());
			context.put("show_id_column", ServerConfigurationService.getBoolean("site.setup.showSiteIdColumn", false));
			context.put("sortby_type", SortType.TYPE_ASC.toString());
			context.put("sortby_createdby", SortType.CREATED_BY_ASC.toString());
			context.put("sortby_publish", SortType.PUBLISHED_ASC.toString());
			context.put("sortby_createdon", SortType.CREATED_ON_ASC.toString());
			context.put("sortby_softlydeleted", SortType.SOFTLY_DELETED_ASC.toString());

			// default to be no paging
			context.put("paged", Boolean.FALSE);

			Menu bar2 = new MenuImpl(portlet, data, (String) state
					.getAttribute(STATE_ACTION));

			// add the search commands
			addSearchMenus(bar2, state);
			context.put("menu2", bar2);

			pagingInfoToContext(state, context);
			
			//SAK-22438 if user can add one of these site types then they can see the link to add a new site
			boolean allowAddSite = false;
			if(SiteService.allowAddCourseSite()) {
				allowAddSite = true;
			} else if (SiteService.allowAddPortfolioSite()) {
				allowAddSite = true;
			} else if (SiteService.allowAddProjectSite()) {
				allowAddSite = true;
			}
			
			context.put("allowAddSite",allowAddSite);

			//Add flash notification when new site is created
			if(state.getAttribute(STATE_NEW_SITE_STATUS_ID) != null){
				String  flashNotifMsg = "<a title=\"" + state.getAttribute(STATE_NEW_SITE_STATUS_TITLE) + "\"href=\"/portal/site/"+
				state.getAttribute(STATE_NEW_SITE_STATUS_ID) + "\" target=\"_top\">"+
				state.getAttribute(STATE_NEW_SITE_STATUS_TITLE)+"</a>" +" "+
				rb.getString("sitdup.hasbeedup");
				addFlashNotif(state,flashNotifMsg);
				StringBuilder sbFlashNotifAction =  new StringBuilder();
				if (state.getAttribute(STATE_NEW_SITE_STATUS_ISPUBLISHED).equals(Boolean.FALSE)) {
					sbFlashNotifAction = new StringBuilder();
					sbFlashNotifAction.append("<div id=\"newSiteAlertActions\" class=\"newSiteAlertActions\">");
					sbFlashNotifAction.append("<a href=\"#\" id=\"newSiteAlertPublish\" class=\""+state.getAttribute(STATE_NEW_SITE_STATUS_ID)+"\""+">" + rb.getString("sitetype.publishSite") + "</a>");
					sbFlashNotifAction.append("<span id=\"newSiteAlertPublishMess\" class=\"messageSuccess\" style=\"display:none\">" + rb.getString("list.publi") + "</span>");
					sbFlashNotifAction.append("</div>");
					addFlashNotif(state, sbFlashNotifAction.toString());
				}
				clearNewSiteStateParameters(state);
			}

			
			return (String) getContext(data).get("template") + TEMPLATE[0];
		case 1:
			/*
			 * buildContextForTemplate chef_site-type.vm
			 * 
			 */
			List types = (List) state.getAttribute(STATE_SITE_TYPES);
			List<String> mTypes = siteTypeProvider.getTypesForSiteCreation();
			if (mTypes != null && !mTypes.isEmpty())
			{
				types.addAll(mTypes);
			}
			context.put("siteTypes", types);
			context.put("templateControls", ServerConfigurationService.getString("site.setup.templateControls",ServerConfigurationService.getString("templateControls", "")));
			// put selected/default site type into context
			String typeSelected = (String) state.getAttribute(STATE_TYPE_SELECTED);
			context.put("typeSelected", state.getAttribute(STATE_TYPE_SELECTED) != null?state.getAttribute(STATE_TYPE_SELECTED):types.get(0));
			
			// bjones86 - SAK-23256
			Boolean hasTerms = Boolean.FALSE;
			List<AcademicSession> termList = setTermListForContext( context, state, true, true ); // true => only
			if( termList != null && termList.size() > 0 )
			{
				hasTerms = Boolean.TRUE;
			}
			context.put( CONTEXT_HAS_TERMS, hasTerms );
			
			// upcoming terms
			setSelectedTermForContext(context, state, STATE_TERM_SELECTED);
			
			// template site
			setTemplateListForContext(context, state);
			
			return (String) getContext(data).get("template") + TEMPLATE[1];
	case 4:
			/*
			 * buildContextForTemplate chef_site-editToolGroups.vm
			 * 
			 */
			state.removeAttribute(STATE_TOOL_GROUP_LIST);
			
			String type = (String) state.getAttribute(STATE_SITE_TYPE);
			setTypeIntoContext(context, type);

			Map<String,List> groupTools = getTools(state, type, site);
			state.setAttribute(STATE_TOOL_GROUP_LIST, groupTools);

			// information related to LTI tools
			buildLTIToolContextForTemplate(context, state, site, true);
			
			if (SecurityService.isSuperUser()) {
				context.put("superUser", Boolean.TRUE);
			} else {
				context.put("superUser", Boolean.FALSE);
			}
			
			
			// save all lists to context			
			pageOrderToolTitleIntoContext(context, state, type, (site == null), site==null?null:site.getProperties().getProperty(SiteConstants.SITE_PROPERTY_OVERRIDE_HIDE_PAGEORDER_SITE_TYPES));
			Boolean checkToolGroupHome = (Boolean) state.getAttribute(STATE_TOOL_HOME_SELECTED);

			context.put("check_home", checkToolGroupHome);
			context.put("ltitool_id_prefix", LTITOOL_ID_PREFIX);
			context.put("serverName", ServerConfigurationService
					.getServerName());
			context.put("sites", SiteService.getSites(
					org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
					null, null, null, SortType.TITLE_ASC, null));
			context.put("import", state.getAttribute(STATE_IMPORT));
			context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));
			if (site != null)
			{
				MathJaxEnabler.addMathJaxSettingsToEditToolsContext(context, site, state);  // SAK-22384
				context.put("SiteTitle", site.getTitle());
				context.put("existSite", Boolean.TRUE);
				context.put("backIndex", "12");	// back to site info list page
			}
			else
			{
				context.put("existSite", Boolean.FALSE);
				context.put("backIndex", "13");	// back to new site information page
			}
			context.put("homeToolId", TOOL_ID_HOME);
			context.put("toolsByGroup", (LinkedHashMap<String,List>) state.getAttribute(STATE_TOOL_GROUP_LIST));
			
			context.put("toolGroupMultiples", getToolGroupMultiples(state, (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST)));

			return (String) getContext(data).get("template") + TEMPLATE[4];

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
			// Are we attempting to softly delete a site.
			boolean softlyDeleting = ServerConfigurationService.getBoolean("site.soft.deletion", false);
			if (removals != null && removals.length != 0) {
				for (int i = 0; i < removals.length; i++) {
					String id = (String) removals[i];
					if (!(id.equals(workspace))) {
						if (SiteService.allowRemoveSite(id)) {
							try {
								// check whether site exists
								Site removeSite = SiteService.getSite(id);
								
								//check site isn't already softly deleted
								if(softlyDeleting && removeSite.isSoftlyDeleted()) {
									softlyDeleting = false;
								}
								remove.add(removeSite);
							} catch (IdUnusedException e) {
								M_log.warn(this + "buildContextForTemplate chef_site-siteDeleteConfirm.vm - IdUnusedException " + id + e.getMessage());
								addAlert(state, rb.getFormattedMessage("java.couldntlocate", new Object[]{id}));
							}
						} else {
							addAlert(state, rb.getFormattedMessage("java.couldntdel", new Object[]{site_title}));
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
			
			//check if hard deletes are wanted
			if(StringUtils.equalsIgnoreCase((String)state.getAttribute(STATE_HARD_DELETE), Boolean.TRUE.toString())) {
				context.put("hardDelete", true);
				//SAK-29678 - If it's hard deleted, it's not soft deleted.
				softlyDeleting = false;
			}
			
			//check if soft deletes are activated
			context.put("softDelete", softlyDeleting);
			
			return (String) getContext(data).get("template") + TEMPLATE[8];
		case 10:
			/*
			 * buildContextForTemplate chef_site-newSiteConfirm.vm
			 * 
			 */
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			String siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (SiteTypeUtil.isCourseSite(siteType)) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("disableCourseSelection", ServerConfigurationService.getString("disable.course.site.skin.selection", "false").equals("true")?Boolean.TRUE:Boolean.FALSE);
				context.put("isProjectSite", Boolean.FALSE);
				putSelectedProviderCourseIntoContext(context, state);
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
					context.put("manualAddNumber", Integer.valueOf(number - 1));
					context.put("manualAddFields", state
							.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
				}
				else if (state.getAttribute(STATE_CM_REQUESTED_SECTIONS) != null) {
					context.put("manualAddNumber", Integer.valueOf(((List) state.getAttribute(STATE_CM_REQUESTED_SECTIONS)).size()));
				}

				context.put("skins", state.getAttribute(STATE_ICONS));
				if (StringUtils.trimToNull(siteInfo.getIconUrl()) != null) {
					context.put("selectedIcon", siteInfo.getIconUrl());
				}
			} else {
				context.put("isCourseSite", Boolean.FALSE);
				if (SiteTypeUtil.isProjectSite(siteType)) {
					context.put("isProjectSite", Boolean.TRUE);
				}

				if (StringUtils.trimToNull(siteInfo.iconUrl) != null) {
					context.put("iconUrl", siteInfo.iconUrl);
				}
			}
			
			context.put("siteUrls", getSiteUrlsForAliasIds(siteInfo.siteRefAliases));

			context.put("title", siteInfo.title);
			context.put("description", siteInfo.description);
			context.put("short_description", siteInfo.short_description);
			context.put("siteContactName", siteInfo.site_contact_name);
			context.put("siteContactEmail", siteInfo.site_contact_email);
			
			/// site language information
 							
 			String locale_string_selected = (String) state.getAttribute("locale_string");
 			if(locale_string_selected == ""  || locale_string_selected == null)		
 				context.put("locale_string_selected", "");			
 			else
 			{
 				Locale locale_selected = getLocaleFromString(locale_string_selected);
 				context.put("locale_string_selected", locale_selected);
 			}

 			// put tool selection into context
			toolSelectionIntoContext(context, state, siteType, null, null/*site.getProperties().getProperty(SiteConstants.SITE_PROPERTY_OVERRIDE_HIDE_PAGEORDER_SITE_TYPES)*/);
			
			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));
			context
					.put("emailId", state
							.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			context.put("serverName", ServerConfigurationService
					.getServerName());
			context.put("include", Boolean.valueOf(siteInfo.include));
			context.put("published", Boolean.valueOf(siteInfo.published));
			context.put("joinable", Boolean.valueOf(siteInfo.joinable));
			context.put("joinerRole", siteInfo.joinerRole);
			context.put("additionalAccess", getAdditionRoles(siteInfo));

			// bjones86 - SAK-24423 - add joinable site settings to context
			JoinableSiteSettings.addJoinableSiteSettingsToNewSiteConfirmContext( context, siteInfo );

			context.put("importSiteTools", state
					.getAttribute(STATE_IMPORT_SITE_TOOL));
			context.put("siteService", SiteService.getInstance());

			// those manual inputs
			context.put("form_requiredFields", sectionFieldProvider
					.getRequiredFields());
			context.put("fieldValues", state
					.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
			
			context.put("fromArchive", state.getAttribute(STATE_UPLOADED_ARCHIVE_NAME));

			return (String) getContext(data).get("template") + TEMPLATE[10];
		case 12:
			/*
			 * buildContextForTemplate chef_site-siteInfo-list.vm
			 * 
			 */
			// put the link for downloading participant
			putPrintParticipantLinkIntoContext(context, data, site);
			
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
						if (StringUtils.equals(s.getUrl(), site.getIconUrl())) {
							context.put("siteUnit", s.getName());
							break;
						}
					}
				}
				if (state.getAttribute(SITE_DUPLICATED) != null) {
						String flashNotifMsg = "<a title=\""+state.getAttribute(SITE_DUPLICATED_NAME) +"\" href=\""+state.getAttribute(STATE_DUPE_SITE_URL)+"\" target=\"_top\">"+state.getAttribute(SITE_DUPLICATED_NAME)+"</a>";
						addFlashNotif(state, rb.getString("sitdup.dupsit") + " " + flashNotifMsg + " " + rb.getString("sitdup.hasbeedup"));
					}
				state.removeAttribute(SITE_DUPLICATED);
				state.removeAttribute(SITE_DUPLICATED_NAME);
				
				context.put("siteFriendlyUrls", getSiteUrlsForSite(site));
				context.put("siteDefaultUrl", getDefaultSiteUrl(siteId));
				
				context.put("siteId", site.getId());
				context.put("siteIcon", site.getIconUrl());
				context.put("siteTitle", site.getTitle());
				context.put("siteDescription", site.getDescription());
				context.put("siteId", site.getId());
				if (unJoinableSiteTypes != null && !unJoinableSiteTypes.contains(siteType))
				{
					context.put("siteJoinable", Boolean.valueOf(site.isJoinable()));
					context.put("allowUnjoin", SiteService.allowUnjoinSite(site.getId()));
				}

				// Is the current user a member
				context.put("siteUserMember", site.getUserRole(UserDirectoryService.getCurrentUser().getId()) != null);

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

				context.put("additionalAccess", getAdditionRoles(site));

				Menu b = new MenuImpl(portlet, data, (String) state
						.getAttribute(STATE_ACTION));
				if (allowUpdateSite) 
				{
					// Site modified by information
					User siteModifiedBy = site.getModifiedBy();
					Time siteModifiedTime = site.getModifiedTime();
					if (siteModifiedBy != null) {
						context.put("siteModifiedBy", siteModifiedBy.getSortName());
					}
					if (siteModifiedTime != null) {
						context.put("siteModifiedTime", siteModifiedTime.toStringLocalFull());
					}
					
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
						if (isPageOrderAllowed(siteType, siteProperties.getProperty(SiteConstants.SITE_PROPERTY_OVERRIDE_HIDE_PAGEORDER_SITE_TYPES)))
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
						if (ServerConfigurationService.getBoolean("site.setup.allow.editRoster", true) && siteType != null && SiteTypeUtil.isCourseSite(siteType)) {
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
					// show add parent sites menu
					if (!isMyWorkspace) {
						if (notStealthOrHiddenTool("sakai-site-manage-link-helper")) {
							b.add(new MenuEntry(rb.getString("java.link"),
									"doLinkHelper"));
						}

						if (notStealthOrHiddenTool("sakai.basiclti.admin.helper")) {
							b.add(new MenuEntry(rb.getString("java.external"),
									"doExternalHelper"));
						}
						
					}
				}
				
				
				if (allowUpdateSite) 
				{
					if (!isMyWorkspace) {
						List<String> providedSiteTypes = siteTypeProvider.getTypes();
						boolean isProvidedType = false;
						if (siteType != null
								&& providedSiteTypes.contains(siteType)) {
							isProvidedType = true;
						}
						if (!isProvidedType) {
							// hide site access for provided site types
							// type of sites
							b.add(new MenuEntry(
									rb.getString("java.siteaccess"),
									"doMenu_edit_site_access"));
							
							// hide site duplicate and import
							if (SiteService.allowAddSite(null) && ServerConfigurationService.getBoolean("site.setup.allowDuplicateSite", true))
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
				
				if (allowUpdateSite) 
				{
					// show add parent sites menu
					if (!isMyWorkspace) {
						boolean eventLog = "true".equals(ServerConfigurationService.getString("user_audit_log_display", "true"));
						if (notStealthOrHiddenTool("sakai.useraudit") && eventLog) {
							b.add(new MenuEntry(rb.getString("java.userAuditEventLog"),
									"doUserAuditEventLog"));
						}
					}
				}
				
				if (b.size() > 0)
				{
					// add the menus to vm
					context.put("menu", b);
				}

				if(state.getAttribute(IMPORT_QUEUED) != null){
					context.put("importQueued", true);
					state.removeAttribute(IMPORT_QUEUED);
					if(StringUtils.isBlank(UserDirectoryService.getCurrentUser().getEmail()) || !ServerConfigurationService.getBoolean(SAK_PROP_IMPORT_NOTIFICATION, true)){
						context.put("importQueuedNoEmail", true);
					}
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
					context.put("participantListSize", Integer.valueOf(participantsCollection.size()));
					context.put("participantList", prepPage(state));
					pagingInfoToContext(state, context);
				}

				context.put("include", Boolean.valueOf(site.isPubView()));

				// site contact information
				String contactName = siteProperties.getProperty(Site.PROP_SITE_CONTACT_NAME);
				String contactEmail = siteProperties.getProperty(Site.PROP_SITE_CONTACT_EMAIL);
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
				
				if (SiteTypeUtil.isCourseSite(siteType)) {
					context.put("isCourseSite", Boolean.TRUE);
					
					coursesIntoContext(state, context, site);
					
					context.put("term", siteProperties
							.getProperty(Site.PROP_SITE_TERM));
				} else {
					context.put("isCourseSite", Boolean.FALSE);
				}
				
				Collection<Group> groups = null;
				if (ServerConfigurationService.getBoolean("wsetup.group.support.summary", true))
				{
					if ((allowUpdateSite || allowUpdateGroupMembership) 
							&& (!isMyWorkspace && ServerConfigurationService.getBoolean("wsetup.group.support", true)))
					{
						// show all site groups
						groups = site.getGroups();
					}
					else
					{
						// show groups that the current user is member of
						groups = site.getGroupsWithMember(UserDirectoryService.getCurrentUser().getId());
					}
				}
				if (groups != null)
				{
					// filter out only those groups that are manageable by site-info
					List<Group> filteredGroups = new ArrayList<Group>();
					List<Group> filteredSections = new ArrayList<Group>();
					Collection<String> viewMembershipGroups = new ArrayList<String>();
					Collection<String> unjoinableGroups = new ArrayList<String>();
					for (Group g : groups)
					{
						Object gProp = g.getProperties().getProperty(g.GROUP_PROP_WSETUP_CREATED);
						if (gProp != null && gProp.equals(Boolean.TRUE.toString()))
						{
							filteredGroups.add(g);
						}
						else
						{
							filteredSections.add(g);
					}
						Object vProp = g.getProperties().getProperty(g.GROUP_PROP_VIEW_MEMBERS);
						if (vProp != null && vProp.equals(Boolean.TRUE.toString())){
							viewMembershipGroups.add(g.getId());
						}
						Object joinableProp = g.getProperties().getProperty(g.GROUP_PROP_JOINABLE_SET);
						Object unjoinableProp = g.getProperties().getProperty(g.GROUP_PROP_JOINABLE_UNJOINABLE);
						if (joinableProp != null && !"".equals(joinableProp.toString())
								&& unjoinableProp != null && unjoinableProp.equals(Boolean.TRUE.toString())
								&& g.getMember(UserDirectoryService.getCurrentUser().getId()) != null){
							unjoinableGroups.add(g.getId());
						}
					}
					Collections.sort(filteredGroups, new Comparator<Group>(){
						public int compare(Group o1, Group o2) {
							return o1.getTitle().compareToIgnoreCase(o2.getTitle());
						}
					});
					context.put("groups", filteredGroups);
					Collections.sort(filteredSections, new Comparator<Group>(){
						public int compare(Group o1, Group o2) {
							return o1.getTitle().compareToIgnoreCase(o2.getTitle());
						}
					});
					context.put("sections", filteredSections);
					context.put("viewMembershipGroups", viewMembershipGroups);
					context.put("unjoinableGroups", unjoinableGroups);
				}
				
				//joinable groups:
				List<JoinableGroup> joinableGroups = new ArrayList<JoinableGroup>();
				if(site.getGroups() != null){
					//find a list of joinable-sets this user is already a member of
					//in order to not display those groups as options
					Set<String> joinableSetsMember = new HashSet<String>();
					for(Group group : site.getGroupsWithMember(UserDirectoryService.getCurrentUser().getId())){
						String joinableSet = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);
						if(joinableSet != null && !"".equals(joinableSet.trim())){
							joinableSetsMember.add(joinableSet);
						}
					}
					for(Group group : site.getGroups()){
						String joinableSet = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);
						if(joinableSet != null && !"".equals(joinableSet.trim()) && !joinableSetsMember.contains(joinableSet)){
							String reference = group.getReference();
							String title = group.getTitle();
							int max = 0;
							try{
								max = Integer.parseInt(group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET_MAX));
							}catch (Exception e) {
							}
							boolean preview = Boolean.valueOf(group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET_PREVIEW));
							String groupMembers = "";
							int size = 0;
							try
							{
								AuthzGroup g = authzGroupService.getAuthzGroup(group.getReference()); 
								Collection<Member> gMembers = g != null ? g.getMembers():new Vector<Member>();
								size = gMembers.size();
								if (size > 0)
								{
									Set<String> hiddenUsers = new HashSet<String>();
									boolean viewHidden = viewHidden = SecurityService.unlock("roster.viewHidden", site.getReference()) || SecurityService.unlock("roster.viewHidden", g.getReference());
									if(!SiteService.allowViewRoster(siteId) && !viewHidden){
										//find hidden users in this group:
										//add hidden users to set so we can filter them out
										Set<String> memberIds = new HashSet<String>();
										for(Member member : gMembers){
											memberIds.add(member.getUserId());
										}
										hiddenUsers = privacyManager.findHidden(site.getReference(), memberIds);
									}
									for (Iterator<Member> gItr=gMembers.iterator(); gItr.hasNext();){
										Member p = (Member) gItr.next();
										
										// exclude those user with provided roles and rosters
										String userId = p.getUserId();
										if(!hiddenUsers.contains(userId)){
											try
											{
												User u = UserDirectoryService.getUser(userId);
												if(!"".equals(groupMembers)){
													groupMembers += ", ";
												}
												groupMembers += u.getDisplayName();
											}
											catch (Exception e)
											{
												M_log.debug(this + "joinablegroups: cannot find user with id " + userId);
												// need to remove the group member
												size--;
											}
									}
									}
								}
							}
							catch (GroupNotDefinedException e)
							{
								M_log.debug(this + "joinablegroups: cannot find group " + group.getReference());
							}
							joinableGroups.add(new JoinableGroup(reference, title, joinableSet, size, max, groupMembers, preview));
						}
					}
					if(joinableGroups.size() > 0){
						Collections.sort(joinableGroups, new Comparator<JoinableGroup>(){
							public int compare(JoinableGroup g1, JoinableGroup g2){
								return g1.getTitle().compareToIgnoreCase(g2.getTitle());
							}
						});
						context.put("joinableGroups", joinableGroups);
					}
				}
				
			} catch (Exception e) {
				M_log.error(this + " buildContextForTemplate chef_site-siteInfo-list.vm ", e);
			}

			roles = getRoles(state);
			context.put("roles", roles);
			
			// SAK-23257 - add the allowed roles to the context for UI rendering
			context.put( VM_ALLOWED_ROLES_DROP_DOWN, SiteParticipantHelper.getAllowedRoles( site.getType(), roles ) );

			// will have the choice to active/inactive user or not
			String activeInactiveUser = ServerConfigurationService.getString(
					"activeInactiveUser", Boolean.FALSE.toString());
			if (activeInactiveUser.equalsIgnoreCase("true")) {
				context.put("activeInactiveUser", Boolean.TRUE);
			} else {
				context.put("activeInactiveUser", Boolean.FALSE);
			}
			
			// UVa add realm object to context so we can provide last modified time
			realmId = SiteService.siteReference(site.getId());
			try {
				AuthzGroup realm = authzGroupService.getAuthzGroup(realmId);
				context.put("realmModifiedTime",realm.getModifiedTime().toStringLocalFullZ());
			} catch (GroupNotDefinedException e) {
				M_log.warn(this + "  IdUnusedException " + realmId);
			}

			// SAK-22384 mathjax support
			MathJaxEnabler.addMathJaxSettingsToSiteInfoContext(context, site, state);

			return (String) getContext(data).get("template") + TEMPLATE[12];

		case 13:
			/*
			 * buildContextForTemplate chef_site-siteInfo-editInfo.vm
			 * 
			 */
			if (site != null) {
				// revising a existing site's tool
				context.put("existingSite", Boolean.TRUE);
				context.put("continue", "14");
				
				ResourcePropertiesEdit props = site.getPropertiesEdit();
						
				String locale_string = StringUtils.trimToEmpty(props.getProperty(PROP_SITE_LANGUAGE));
				context.put("locale_string",locale_string);
			} else {
				// new site
				context.put("existingSite", Boolean.FALSE);
				context.put("continue", "4");
				
				// get the system default as locale string
				context.put("locale_string", "");
			}
			
			boolean displaySiteAlias = displaySiteAlias();
			context.put("displaySiteAlias", Boolean.valueOf(displaySiteAlias));
			if (displaySiteAlias)
			{
				context.put(FORM_SITE_URL_BASE, getSiteBaseUrl());
				context.put(FORM_SITE_ALIAS, siteInfo.getFirstAlias());
			}
			
			siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			context.put("type", siteType);
			context.put("siteTitleEditable", Boolean.valueOf(siteTitleEditable(state, siteType)));
			context.put("titleMaxLength", state.getAttribute(STATE_SITE_TITLE_MAX));

			if (SiteTypeUtil.isCourseSite(siteType)) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("isProjectSite", Boolean.FALSE);

				boolean hasRosterAttached = putSelectedProviderCourseIntoContext(context, state);

				List<SectionObject> cmRequestedList = (List<SectionObject>) state
						.getAttribute(STATE_CM_REQUESTED_SECTIONS);

				if (cmRequestedList != null) {
					context.put("cmRequestedSections", cmRequestedList);
					if (!hasRosterAttached && cmRequestedList.size() > 0)
					{
						hasRosterAttached = true;
					}
				}

				List<SectionObject> cmAuthorizerSectionList = (List<SectionObject>) state
						.getAttribute(STATE_CM_AUTHORIZER_SECTIONS);
				if (cmAuthorizerSectionList != null) {
					context
							.put("cmAuthorizerSections",
									cmAuthorizerSectionList);
					if (!hasRosterAttached && cmAuthorizerSectionList.size() > 0)
					{
						hasRosterAttached = true;
					}
				}

				if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
					int number = ((Integer) state
							.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER))
							.intValue();
					context.put("manualAddNumber", Integer.valueOf(number - 1));
					context.put("manualAddFields", state
							.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
					if (!hasRosterAttached)
					{
						hasRosterAttached = true;
					}
				} else {
					if (site != null)
						if (!hasRosterAttached)
						{
							hasRosterAttached = coursesIntoContext(state, context, site);
						}
						else
						{
							coursesIntoContext(state, context, site);
						}

					if (courseManagementIsImplemented()) {
					} else {
						context.put("templateIndex", "37");
					}
				}
				context.put("hasRosterAttached", Boolean.valueOf(hasRosterAttached));
				
				if (StringUtils.trimToNull(siteInfo.term) == null) {
					if (site != null)
					{
						// existing site
						siteInfo.term = site.getProperties().getProperty(Site.PROP_SITE_TERM);
					}
					else
					{
						// creating new site
						AcademicSession t = (AcademicSession) state.getAttribute(STATE_TERM_SELECTED);
						siteInfo.term = t != null?t.getEid() : "";
					}
				}
				context.put("selectedTerm", siteInfo.term != null? siteInfo.term:"");
				
			} else {
				context.put("isCourseSite", Boolean.FALSE);
				if (SiteTypeUtil.isProjectSite(siteType)) {
					context.put("isProjectSite", Boolean.TRUE);
				}

				if (StringUtils.trimToNull(siteInfo.iconUrl) != null) {
					context.put(FORM_ICON_URL, siteInfo.iconUrl);
				}
			}

			// about skin and icon selection
			skinIconSelection(context, state, SiteTypeUtil.isCourseSite(siteType), site, siteInfo);

			// those manual inputs
			context.put("form_requiredFields", sectionFieldProvider.getRequiredFields());
			context.put("fieldValues", state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
			
			context.put("title", siteInfo.title);
			context.put(FORM_SITE_URL_BASE, getSiteBaseUrl());
			context.put(FORM_SITE_ALIAS, siteInfo.getFirstAlias());
			context.put("description", siteInfo.description);
			context.put("short_description", siteInfo.short_description);
			context.put("form_site_contact_name", siteInfo.site_contact_name);
			context.put("form_site_contact_email", siteInfo.site_contact_email);
			
			context.put("site_aliases", state.getAttribute(FORM_SITEINFO_ALIASES));
			context.put("site_url_base", state.getAttribute(FORM_SITEINFO_URL_BASE));
			context.put("site_aliases_editable", aliasesEditable(state, site == null ? null:site.getReference()));
			context.put("site_alias_assignable", aliasAssignmentForNewSitesEnabled(state));

			// available languages in sakai.properties
			List locales = getPrefLocales();	
			context.put("locales",locales);

			// SAK-22384 mathjax support
			MathJaxEnabler.addMathJaxSettingsToSiteInfoContext(context, site, state);
						
			return (String) getContext(data).get("template") + TEMPLATE[13];
		case 14:
			/*
			 * buildContextForTemplate chef_site-siteInfo-editInfoConfirm.vm
			 * 
			 */
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			context.put("displaySiteAlias", Boolean.valueOf(displaySiteAlias()));
			siteProperties = site.getProperties();
			siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (SiteTypeUtil.isCourseSite(siteType)) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("siteTerm", siteInfo.term);
			} else {
				context.put("isCourseSite", Boolean.FALSE);
			}
			// about skin and icon selection
			skinIconSelection(context, state, SiteTypeUtil.isCourseSite(siteType), site, siteInfo);
			
			context.put("oTitle", site.getTitle());
			context.put("title", siteInfo.title);
			
			// get updated language
			String new_locale_string = (String) state.getAttribute("locale_string");			
			if(new_locale_string == ""  || new_locale_string == null)							
				context.put("new_locale", "");			
			else
			{
				Locale new_locale = getLocaleFromString(new_locale_string);
				context.put("new_locale", new_locale);
			}
						
			// get site language saved
			ResourcePropertiesEdit props = site.getPropertiesEdit();					
			String oLocale_string = props.getProperty(PROP_SITE_LANGUAGE);			
			if(oLocale_string == "" || oLocale_string == null)				
				context.put("oLocale", "");			
			else
			{
				Locale oLocale = getLocaleFromString(oLocale_string);
				context.put("oLocale", oLocale);
			}
									
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
			context.put("oName", siteProperties.getProperty(Site.PROP_SITE_CONTACT_NAME));
			context.put("email", siteInfo.site_contact_email);
			context.put("oEmail", siteProperties.getProperty(Site.PROP_SITE_CONTACT_EMAIL));
			context.put("siteUrls",  getSiteUrlsForAliasIds(siteInfo.siteRefAliases));
			context.put("oSiteUrls", getSiteUrlsForSite(site));

			// SAK-22384 mathjax support
			MathJaxEnabler.addMathJaxSettingsToSiteInfoContext(context, site, state);

			return (String) getContext(data).get("template") + TEMPLATE[14];
		case 15:
			/*
			 * buildContextForTemplate chef_site-addRemoveFeatureConfirm.vm
			 * 
			 */
			context.put("title", site.getTitle());

			site_type = (String) state.getAttribute(STATE_SITE_TYPE);
			boolean myworkspace_site = false;
			if (SiteService.isUserSite(site.getId())) {
				if (SiteService.getSiteUserId(site.getId()).equals(
						SessionManager.getCurrentSessionUserId())) {
					myworkspace_site = true;
					site_type = "myworkspace";
				}
			}

			String overridePageOrderSiteTypes = site.getProperties().getProperty(SiteConstants.SITE_PROPERTY_OVERRIDE_HIDE_PAGEORDER_SITE_TYPES);
			// put tool selection into context
			toolSelectionIntoContext(context, state, site_type, site.getId(), overridePageOrderSiteTypes);
			MathJaxEnabler.addMathJaxSettingsToEditToolsConfirmationContext(context, site, state, STATE_TOOL_REGISTRATION_TITLE_LIST);  // SAK-22384            

			return (String) getContext(data).get("template") + TEMPLATE[15];
		case 18:
			/*
			 * buildContextForTemplate chef_siteInfo-editAccess.vm
			 * 
			 */
			List publicChangeableSiteTypes = (List) state
					.getAttribute(STATE_PUBLIC_CHANGEABLE_SITE_TYPES);

			context.put("authAllowed", ServerConfigurationService.getBoolean("sitemanage.grant.auth", false));
			context.put("anonAllowed", ServerConfigurationService.getBoolean("sitemanage.grant.anon", false));

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
				context.put("published", state.getAttribute(STATE_SITE_ACCESS_PUBLISH));
				context.put("include", state.getAttribute(STATE_SITE_ACCESS_INCLUDE));

				context.put("shoppingPeriodInstructorEditable", ServerConfigurationService.getBoolean("delegatedaccess.shopping.instructorEditable", false));
				context.put("viewDelegatedAccessUsers", ServerConfigurationService.getBoolean("delegatedaccess.siteaccess.instructorViewable", false));

				// bjones86 - SAK-24423 - add joinable site settings to context
				JoinableSiteSettings.addJoinableSiteSettingsToEditAccessContextWhenSiteIsNotNull( context, state, site, !unJoinableSiteTypes.contains( siteType ) );

				if (siteType != null && !unJoinableSiteTypes.contains(siteType)) {
					// site can be set as joinable
					context.put("disableJoinable", Boolean.FALSE);
					if (state.getAttribute(STATE_JOINABLE) == null) {
						state.setAttribute(STATE_JOINABLE, Boolean.valueOf(site
								.isJoinable()));
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
				
				Map<String, AdditionalRole> access = getAdditionalAccess(site);
				
				addAccess(context, access);

				// bjones86 - SAK-23257
				context.put("roles", getJoinerRoles(site.getReference(), state, site.getType()));
			} else {
				// In the site creation process...
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
				
				// bjones86 - SAK-24423 - add joinable site settings to context
				JoinableSiteSettings.addJoinableSiteSettingsToEditAccessContextWhenSiteIsNull( context, siteInfo, true );
				
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
					AuthzGroup r = authzGroupService.getAuthzGroup(realmTemplate);
					
					// bjones86 - SAK-23257
					context.put("roles", getJoinerRoles(r.getId(), state, null));
				} catch (GroupNotDefinedException e) {
					try {
						AuthzGroup rr = authzGroupService.getAuthzGroup("!site.template");
						
						// bjones86 - SAK-23257
						context.put("roles", getJoinerRoles(rr.getId(), state, null));
					} catch (GroupNotDefinedException ee) {
					}
				}

				Map<String, AdditionalRole> additionalRoles = loadAdditionalRoles();
				for (AdditionalRole role: additionalRoles.values()) {
					if (siteInfo.additionalRoles.contains(role.getId())) {
						role.granted = true;
					}
				}
				addAccess(context,additionalRoles);
				context.put("continue", "10");

				siteType = (String) state.getAttribute(STATE_SITE_TYPE);
				setTypeIntoContext(context, siteType);
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

			context.put(STATE_TOOL_REGISTRATION_LIST, state
					.getAttribute(STATE_TOOL_REGISTRATION_LIST));
			toolRegistrationSelectedList = (List) state
					.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			context.put(STATE_TOOL_REGISTRATION_SELECTED_LIST,
					toolRegistrationSelectedList); // String toolId's
			
			// all info related to multiple tools
			multipleToolIntoContext(context, state);
			
			// put the lti tool selection into context
			if (state.getAttribute(STATE_LTITOOL_SELECTED_LIST) != null)
			{
				HashMap<String, Map<String, Object>> currentLtiTools = (HashMap<String, Map<String, Object>>) state.getAttribute(STATE_LTITOOL_SELECTED_LIST);
				for (Map.Entry<String, Map<String, Object>> entry : currentLtiTools.entrySet() ) {
					 Map<String, Object> toolMap = entry.getValue();
					 String toolId = entry.getKey();
					// get the proper html for tool input
					String ltiToolId = toolMap.get("id").toString();
					String[] contentToolModel=m_ltiService.getContentModel(Long.valueOf(ltiToolId));
					// attach the ltiToolId to each model attribute, so that we could have the tool configuration page for multiple tools
					for(int k=0; k<contentToolModel.length;k++)
					{
						contentToolModel[k] = ltiToolId + "_" + contentToolModel[k];
					}
					Map<String, Object> ltiTool = m_ltiService.getTool(Long.valueOf(ltiToolId));
					String formInput=m_ltiService.formInput(ltiTool, contentToolModel);
					toolMap.put("formInput", formInput);
					currentLtiTools.put(ltiToolId, toolMap);
				}
				context.put("ltiTools", currentLtiTools);
				context.put("ltiService", m_ltiService);
				context.put("oldLtiTools", state.getAttribute(STATE_LTITOOL_EXISTING_SELECTED_LIST));
			}
			
			context.put("toolManager", ToolManager.getInstance());

			AcademicSession thisAcademicSession = (AcademicSession) state.getAttribute(STATE_TERM_SELECTED);
			String emailId = null;

			boolean prePopulateEmail = ServerConfigurationService.getBoolean("wsetup.mailarchive.prepopulate.email",true);
			if(prePopulateEmail == true && state.getAttribute(STATE_TOOL_EMAIL_ADDRESS)==null){
				if(thisAcademicSession!=null){
					String siteTitle1 = siteInfo.title.replaceAll("[(].*[)]", "");
					siteTitle1 = siteTitle1.trim();
					siteTitle1 = siteTitle1.replaceAll(" ", "-");
					emailId = siteTitle1;
				}else{
					emailId = StringUtils.deleteWhitespace(siteInfo.title);
				}
			}else{
				emailId = (String) state.getAttribute(STATE_TOOL_EMAIL_ADDRESS);
			}

			if (emailId != null) {
				context.put("emailId", emailId);
			}

			context.put("serverName", ServerConfigurationService
					.getServerName());

			context.put("oldSelectedTools", state
					.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST));

			context.put("homeToolId", TOOL_ID_HOME);
			
			context.put("maxToolTitleLength", MAX_TOOL_TITLE_LENGTH);
			
			return (String) getContext(data).get("template") + TEMPLATE[26];
		case 27: {
			/*
			 * buildContextForTemplate chef_site-importSites.vm
			 * 
			 * This is called before the list of tools to choose the content to import from (when merging) is presented.
			 * This is also called in the new site workflow if re-using content from an existing site
			 * 
			 */
			existingSite = site != null ? true : false;
			site_type = (String) state.getAttribute(STATE_SITE_TYPE);
			
			// define the tools available for import. defaults to those tools in the 'destination' site
			List<String> importableToolsIdsInDestinationSite = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			if (existingSite) {
				// revising a existing site's tool
				context.put("continue", "12");
				context.put("step", "2");
				context.put("currentSite", site);
				
				// if the site exists, there may be other tools available for import
				importableToolsIdsInDestinationSite = getToolsAvailableForImport(state, importableToolsIdsInDestinationSite);
				
			} else {
				// new site, go to edit access page
				if (fromENWModifyView(state)) {
					context.put("continue", "26");
				} else {
					context.put("continue", "18");
				}
			}
			
			// list of all tools that participate in the archive/merge process that are in the site selected to import from
			List<Site> importSites = new ArrayList<Site>(((Hashtable) state.getAttribute(STATE_IMPORT_SITES)).keySet());
			List<String> allImportableToolIdsInOriginalSites = getToolsInSitesAvailableForImport(importSites);
			
			context.put("existingSite", Boolean.valueOf(existingSite));
			
			//sort the list of all tools by title and extract into a list of toolIds
			//we then use this as the basis for sorting the other toolId lists
			List<MyTool> allTools = (List<MyTool>)state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
						
			Collections.sort(allTools, new Comparator<MyTool>(){
				public int compare(MyTool t1, MyTool t2) {
					return t1.getTitle().compareTo(t2.getTitle());
				}
			});
			
			final List<String> sortedToolIds = new ArrayList<String>();
			for(MyTool m: allTools) {
				sortedToolIds.add(m.getId());
			}
			
			//use the above sorted list as the basis to sort the following two toolId lists
			Collections.sort(allImportableToolIdsInOriginalSites, new Comparator<String>() {
			    public int compare(String s1, String s2) {
			        return Integer.compare(sortedToolIds.indexOf(s1), sortedToolIds.indexOf(s2));
			    }
			});
			
			Collections.sort(importableToolsIdsInDestinationSite, new Comparator<String>() {
			    public int compare(String s1, String s2) {
			        return Integer.compare(sortedToolIds.indexOf(s1), sortedToolIds.indexOf(s2));
			    }
			});
			
			context.put(STATE_TOOL_REGISTRATION_LIST, allTools);

			
			//if option is enabled, show the import for all tools in the original site, not just the ones in this site
			//otherwise, only import content for the tools that already exist in the 'destination' site
			boolean addMissingTools = isAddMissingToolsOnImportEnabled();
			
			//helper var to hold the list we use for the selectedTools context variable, as we use it for the alternate toolnames too
			List<String> selectedTools = new ArrayList<>();
			
			if(addMissingTools) {
				selectedTools = allImportableToolIdsInOriginalSites;
				context.put("selectedTools", selectedTools);
				//set tools in destination site into context so we can markup the lists and show which ones are new
				context.put("toolsInDestinationSite", importableToolsIdsInDestinationSite);
			} else {
				//just just the ones in the destination site
				selectedTools = importableToolsIdsInDestinationSite;
				context.put("selectedTools", selectedTools);
			}
			
			//get all known tool names from the sites selected to import from (importSites) and the selectedTools list
			Map<String,Set<String>> toolNames = this.getToolNames(selectedTools, importSites);
			
			//filter this list so its just the alternate ones and turn it into a string for the UI
			Map<String,String> alternateToolTitles = new HashMap<>();
			for(MyTool myTool : allTools) {
				String toolId = myTool.getId();
				String toolTitle = myTool.getTitle();
				Set<String> allToolNames = toolNames.get(toolId);
				if(allToolNames != null) {
					allToolNames.remove(toolTitle);
				
					//if we have something left then we have alternates, so process them
					if(!allToolNames.isEmpty()) {
						alternateToolTitles.put(toolId, StringUtils.join(allToolNames, ", "));
					}
				}
			}
			context.put("alternateToolTitlesMap", alternateToolTitles);
			
			//build a map of sites and tools in those sites that have content
			Map<String,Set<String>> siteToolsWithContent = this.getSiteImportToolsWithContent(importSites, selectedTools);
			context.put("siteToolsWithContent", siteToolsWithContent);
			
			// set the flag for the UI
			context.put("addMissingTools", addMissingTools);
			
			context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));
			context.put("importSitesTools", state
					.getAttribute(STATE_IMPORT_SITE_TOOL));
			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));
			context.put("importSupportedTools", allImportableToolIdsInOriginalSites);
			context.put("hideImportedContent", ServerConfigurationService.getBoolean("content.import.hidden", false));
			
			if(ServerConfigurationService.getBoolean("site-manage.importoption.siteinfo", false)){
				try{
					String siteInfoToolTitle = ToolManager.getTool(SITE_INFO_TOOL_ID).getTitle();
					context.put("siteInfoToolTitle", siteInfoToolTitle);
				}catch(Exception e){
					
				}
			}
			
			
			return (String) getContext(data).get("template") + TEMPLATE[27];
		}
		case 60: {
			/*
			 * buildContextForTemplate chef_site-importSitesMigrate.vm
			 * 
			 * This is called before the list of tools to choose the content to import from (when replacing) is presented.
			 * 
			 */
			existingSite = site != null ? true : false;
			site_type = (String) state.getAttribute(STATE_SITE_TYPE);
			
			// define the tools available for import. defaults to those tools in the 'destination' site
			List<String> importableToolsIdsInDestinationSite = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			
			if (existingSite) {
				// revising a existing site's tool
				context.put("continue", "12");
				context.put("back", "28");
				context.put("step", "2");
				context.put("currentSite", site);
				
				// if the site exists, there may be other tools available for import
				importableToolsIdsInDestinationSite = getToolsAvailableForImport(state, importableToolsIdsInDestinationSite);
				
			} else {
				// new site, go to edit access page
				context.put("back", "4");
				if (fromENWModifyView(state)) {
					context.put("continue", "26");
				} else {
					context.put("continue", "18");
				}
				
				//use the toolId list for the new site we are creating
				importableToolsIdsInDestinationSite = (List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			}
			
			// list of all tools that participate in the archive/merge process that are in the site selected to import from
			List<Site> importSites = new ArrayList<Site>(((Hashtable) state.getAttribute(STATE_IMPORT_SITES)).keySet());
			List<String> allImportableToolIdsInOriginalSites = getToolsInSitesAvailableForImport(importSites);
			
			//sort the list of all tools by title and extract into a list of toolIds
			//we then use this as the basis for sorting the other toolId lists
			List<MyTool> allTools = (List<MyTool>)state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
						
			Collections.sort(allTools, new Comparator<MyTool>(){
				public int compare(MyTool t1, MyTool t2) {
					return t1.getTitle().compareTo(t2.getTitle());
				}
			});
				
			final List<String> sortedToolIds = new ArrayList<String>();
			for(MyTool m: allTools) {
				sortedToolIds.add(m.getId());
			}
			
			//use the above sorted list as the basis to sort the following two toolId lists
			Collections.sort(allImportableToolIdsInOriginalSites, new Comparator<String>() {
			    public int compare(String s1, String s2) {
			        return Integer.compare(sortedToolIds.indexOf(s1), sortedToolIds.indexOf(s2));
			    }
			});
			
			Collections.sort(importableToolsIdsInDestinationSite, new Comparator<String>() {
			    public int compare(String s1, String s2) {
			        return Integer.compare(sortedToolIds.indexOf(s1), sortedToolIds.indexOf(s2));
			    }
			});
			
			//ensure this is the original tool list and set the sorted list back into context.
			context.put(STATE_TOOL_REGISTRATION_LIST, allTools);
			state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST));
			
			//if option is enabled, import into ALL tools, not just the ones in this site
			//otherwise, only import content for the tools that already exist in the 'destination' site
			boolean addMissingTools = isAddMissingToolsOnImportEnabled();
			
			//helper var to hold the list we use for the selectedTools context variable, as we use it for the alternate toolnames too
			List<String> selectedTools = new ArrayList<>();
			
			if(addMissingTools) {
				
                selectedTools = allImportableToolIdsInOriginalSites;
				
				context.put("selectedTools", selectedTools); 
				//set tools in destination site into context so we can markup the lists and show which ones are new
				context.put("toolsInDestinationSite", importableToolsIdsInDestinationSite);
				
			} else {
				
                selectedTools = importableToolsIdsInDestinationSite;

				//just just the ones in the destination site
				context.put("selectedTools", selectedTools); 
			}
			
			//get all known tool names from the sites selected to import from (importSites) and the selectedTools list
			Map<String,Set<String>> toolNames = this.getToolNames(selectedTools, importSites);
			
			//filter this list so its just the alternate ones and turn it into a string for the UI
			Map<String,String> alternateToolTitles = new HashMap<>();
			for(MyTool myTool : allTools) {
				String toolId = myTool.getId();
				String toolTitle = myTool.getTitle();
				Set<String> allToolNames = toolNames.get(toolId);
				if(allToolNames != null) {
					allToolNames.remove(toolTitle);
				
					//if we have something left then we have alternates, so process them
					if(!allToolNames.isEmpty()) {
						alternateToolTitles.put(toolId, StringUtils.join(allToolNames, ", "));
					}
				}
			}
			context.put("alternateToolTitlesMap", alternateToolTitles);
			
			//build a map of sites and tools in those sites that have content
			Map<String,Set<String>> siteToolsWithContent = this.getSiteImportToolsWithContent(importSites, selectedTools);
			context.put("siteToolsWithContent", siteToolsWithContent);
						
			// set the flag for the UI
			context.put("addMissingTools", addMissingTools);

			context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));
			context.put("importSitesTools", state
					.getAttribute(STATE_IMPORT_SITE_TOOL));
			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));
			context.put("importSupportedTools", allImportableToolIdsInOriginalSites);

			
			
			
			return (String) getContext(data).get("template") + TEMPLATE[60];
		}
		case 28:
			/*
			 * buildContextForTemplate chef_siteinfo-import.vm
			 * 
			 * This is called before the list of sites to import from is presented
			 * 
			 */
			putImportSitesInfoIntoContext(context, site, state, false);
			return (String) getContext(data).get("template") + TEMPLATE[28];
		case 58:
			/*
			 * buildContextForTemplate chef_siteinfo-importSelection.vm
			 * 
			 */
			putImportSitesInfoIntoContext(context, site, state, false);
			return (String) getContext(data).get("template") + TEMPLATE[58];
		case 59:
			/*
			 * buildContextForTemplate chef_siteinfo-importMigrate.vm
			 * 
			 */
			putImportSitesInfoIntoContext(context, site, state, false);
			return (String) getContext(data).get("template") + TEMPLATE[59];

		case 29:
			/*
			 * buildContextForTemplate chef_siteinfo-duplicate.vm
			 * 
			 */
			context.put("siteTitle", site.getTitle());
			String sType = site.getType();
			if (sType != null && SiteTypeUtil.isCourseSite(sType)) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("currentTermId", site.getProperties().getProperty(
						Site.PROP_SITE_TERM));
				
				// bjones86 - SAK-23256
				setTermListForContext( context, state, true, false ); // true upcoming only
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
			
			// SAK-20797 - display checkboxes only if sitespecific value exists
			long quota = getSiteSpecificQuota(site);
			if (quota > 0) {
				context.put("hasSiteSpecificQuota", true);
				context.put("quotaSize", formatSize(quota*1024));		
				}
			else {
				context.put("hasSiteSpecificQuota", false);
			}
			
			context.put("titleMaxLength", state.getAttribute(STATE_SITE_TITLE_MAX));
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

				List providerCourseList = (List) state
						.getAttribute(SITE_PROVIDER_COURSE_LIST);
				coursesIntoContext(state, context, site);

				// bjones86 - SAK-23256
				List<AcademicSession> terms = setTermListForContext( context, state, true, true ); // true -> upcoming only
				
				AcademicSession t = (AcademicSession) state.getAttribute(STATE_TERM_SELECTED);
				
				if (terms != null && terms.size() > 0)
				{
					boolean foundTerm = false;
					for(AcademicSession testTerm : terms)
					{
						if (t != null && testTerm.getEid().equals(t.getEid()))
						{
							foundTerm = true;
							break;
						}
					}
					if (!foundTerm)
					{
						// if the term is no longer listed in the term list, choose the first term in the list instead
						t = terms.get(0);
					}
				}
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
							if (providerCourseList == null || !providerCourseList.contains(c.getEid())) {
								notIncludedCourse.add(c);
							}
						}
						state.setAttribute(STATE_TERM_COURSE_LIST,
								notIncludedCourse);
					} else {
						state.removeAttribute(STATE_TERM_COURSE_LIST);
					}
				}
			} else {
				// need to include both list 'cos STATE_CM_AUTHORIZER_SECTIONS
				// contains sections that doens't belongs to current user and
				// STATE_ADD_CLASS_PROVIDER_CHOSEN contains section that does -
				// v2.4 daisyf
				if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null
						|| state.getAttribute(STATE_CM_AUTHORIZER_SECTIONS) != null) {
					
					putSelectedProviderCourseIntoContext(context, state);

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
				context.put("authorizers", getAuthorizers(state, STATE_CM_AUTHORIZER_LIST));
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

			// SAK-29000
			Boolean isAuthorizationRequired = ServerConfigurationService.getBoolean( SAK_PROP_REQUIRE_AUTHORIZER, Boolean.TRUE );
			context.put( VM_ADD_ROSTER_AUTH_REQUIRED, isAuthorizationRequired );

			// added for 2.4 -daisyf
			context.put("campusDirectory", getCampusDirectory());
			context.put("userId", state.getAttribute(STATE_INSTRUCTOR_SELECTED) != null ? (String) state.getAttribute(STATE_INSTRUCTOR_SELECTED) : UserDirectoryService.getCurrentUser().getId());
			/*
			 * for measuring how long it takes to load sections java.util.Date
			 * date = new java.util.Date(); M_log.debug("***2. finish at:
			 * "+date); M_log.debug("***3. userId:"+(String) state
			 * .getAttribute(STATE_INSTRUCTOR_SELECTED));
			 */
			
			context.put("basedOnTemplate",  state.getAttribute(STATE_TEMPLATE_SITE) != null ? Boolean.TRUE:Boolean.FALSE);
			context.put("publishTemplate", (Boolean) state.getAttribute(STATE_TEMPLATE_PUBLISH));
			
			// bjones86 - SAK-21706
			context.put( CONTEXT_SKIP_COURSE_SECTION_SELECTION, 
					ServerConfigurationService.getBoolean( SAK_PROP_SKIP_COURSE_SECTION_SELECTION, Boolean.FALSE ) );
			context.put( CONTEXT_SKIP_MANUAL_COURSE_CREATION, 
					ServerConfigurationService.getBoolean( SAK_PROP_SKIP_MANUAL_COURSE_CREATION, Boolean.FALSE ) );
			
			context.put("siteType", state.getAttribute(STATE_TYPE_SELECTED));
			
			// SAK-28990 remove continue with no roster
			if( "true".equalsIgnoreCase( ServerConfigurationService.getString( SAK_PROP_CONT_NO_ROSTER_ENABLED, "true" ) ) )
			{
				context.put( VM_CONT_NO_ROSTER_ENABLED, Boolean.TRUE );
			}
			else
			{
				context.put( VM_CONT_NO_ROSTER_ENABLED, Boolean.FALSE);
			}
			
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
			if (state.getAttribute(STATE_SITE_QUEST_UNIQNAME) == null)
			{
				context.put("value_uniqname", getAuthorizers(state, STATE_SITE_QUEST_UNIQNAME));
			}
			int number = 1;
			if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
				number = ((Integer) state
						.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER))
						.intValue();
				context.put("currentNumber", Integer.valueOf(number));
			}
			context.put("currentNumber", Integer.valueOf(number));
			context.put("listSize", number>0?Integer.valueOf(number - 1):0);
			if (state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS) != null && ((List) state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS)).size() > 0)
			{
				context.put("fieldValues", state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
			}

			putSelectedProviderCourseIntoContext(context, state);

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
			
			context.put("basedOnTemplate",  state.getAttribute(STATE_TEMPLATE_SITE) != null ? Boolean.TRUE:Boolean.FALSE);
			context.put("publishTemplate", (Boolean) state.getAttribute(STATE_TEMPLATE_PUBLISH));
			
			context.put("requireAuthorizer", ServerConfigurationService.getString(SAK_PROP_REQUIRE_AUTHORIZER, "true").equals("true")?Boolean.TRUE:Boolean.FALSE);
			
			// bjones86 - SAK-21706/SAK-23255
			context.put( CONTEXT_IS_ADMIN, SecurityService.isSuperUser() );
			context.put( CONTEXT_SKIP_COURSE_SECTION_SELECTION, ServerConfigurationService.getBoolean( SAK_PROP_SKIP_COURSE_SECTION_SELECTION, Boolean.FALSE ) );
			context.put( CONTEXT_FILTER_TERMS, ServerConfigurationService.getBoolean( SAK_PROP_FILTER_TERMS, Boolean.FALSE ) );
			
			return (String) getContext(data).get("template") + TEMPLATE[37];
		case 42:
			/*
			 * buildContextForTemplate chef_site-type-confirm.vm
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
			context.put("include", Boolean.valueOf(siteInfo.include));
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

			putSelectedProviderCourseIntoContext(context, state);
			
			if (state.getAttribute(STATE_CM_SELECTED_SECTIONS) != null)
			{
				context.put("cmSelectedSections", state.getAttribute(STATE_CM_SELECTED_SECTIONS));
			}
			if (state.getAttribute(STATE_CM_REQUESTED_SECTIONS) != null)
			{
				context.put("cmRequestedSections", state.getAttribute(STATE_CM_REQUESTED_SECTIONS));
			}
			if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
				int addNumber = ((Integer) state
						.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER))
						.intValue() - 1;
				context.put("manualAddNumber", Integer.valueOf(addNumber));
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
				cmLevels = getCMLevelLabels(state);
			}

			List<SectionObject> selectedSect = (List<SectionObject>) state
					.getAttribute(STATE_CM_SELECTED_SECTION);
			List<SectionObject> requestedSections = (List<SectionObject>) state
					.getAttribute(STATE_CM_REQUESTED_SECTIONS);

			if (courseManagementIsImplemented() && cms != null) {
				context.put("cmsAvailable", Boolean.valueOf(true));
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
				}

				if (numSelections != 0)
				{
					// execution will fall through these statements based on number of selections already made
					if (numSelections == cmLevelSize - 1)
					{
						levelOpts[numSelections] = getCMSections((String) selections.get(numSelections-1));
					}
					else if (numSelections == cmLevelSize - 2)
					{
						levelOpts[numSelections] = getCMCourseOfferings(getSelectionString(selections, numSelections), t.getEid());
					}
					else if (numSelections < cmLevelSize)
					{
						levelOpts[numSelections] = sortCourseSets(cms.findCourseSets(getSelectionString(selections, numSelections)));
					}
				}
				// always set the top level
				Set<CourseSet> courseSets = filterCourseSetList(getCourseSet(state));
				levelOpts[0] = sortCourseSets(courseSets);
				
				// clean further element inside the array
				for (int i = numSelections + 1; i<cmLevelSize; i++)
				{
					levelOpts[i] = null;
				}

				context.put("cmLevelOptions", Arrays.asList(levelOpts));
				context.put("cmBaseCourseSetLevel", Integer.valueOf((levelOpts.length-3) >= 0 ? (levelOpts.length-3) : 0)); // staring from that selection level, the lookup will be for CourseSet, CourseOffering, and Section
				context.put("maxSelectionDepth", Integer.valueOf(levelOpts.length-1));
				state.setAttribute(STATE_CM_LEVEL_OPTS, levelOpts);
			}

			putSelectedProviderCourseIntoContext(context, state);
			
			if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
				int courseInd = ((Integer) state
						.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER))
						.intValue();
				context.put("manualAddNumber", Integer.valueOf(courseInd - 1));
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

			context.put("authzGroupService", authzGroupService);

			if (selectedSect !=null && !selectedSect.isEmpty() && state.getAttribute(STATE_SITE_QUEST_UNIQNAME) == null){
				context.put("value_uniqname", selectedSect.get(0).getAuthorizerString());
			}
			context.put("value_uniqname", state.getAttribute(STATE_SITE_QUEST_UNIQNAME));
			context.put("basedOnTemplate",  state.getAttribute(STATE_TEMPLATE_SITE) != null ? Boolean.TRUE:Boolean.FALSE);
			context.put("requireAuthorizer", ServerConfigurationService.getString(SAK_PROP_REQUIRE_AUTHORIZER, "true").equals("true")?Boolean.TRUE:Boolean.FALSE);
			
			// bjones86 - SAK-21706/SAK-23255
			context.put( CONTEXT_IS_ADMIN, SecurityService.isSuperUser() );
			context.put( CONTEXT_SKIP_MANUAL_COURSE_CREATION, ServerConfigurationService.getBoolean( SAK_PROP_SKIP_MANUAL_COURSE_CREATION, Boolean.FALSE ) );
			context.put( CONTEXT_FILTER_TERMS, ServerConfigurationService.getBoolean( SAK_PROP_FILTER_TERMS, Boolean.FALSE ) );
			
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
		
		case 61:
			/*
			 * build context for chef_site-importUser.vm
			 */
			context.put("toIndex", "12");
			// only show those sites with same site type
			putImportSitesInfoIntoContext(context, site, state, true);
			return (String) getContext(data).get("template") + TEMPLATE[61];
		
		case 62:
			/*
			 * build context for chef_site-uploadArchive.vm
			 */
			
			//back to access, continue to confirm
			context.put("back", "18");

			//now go to uploadArchive template
			return (String) getContext(data).get("template") + TEMPLATE[62];
		}
			
		// should never be reached
		return (String) getContext(data).get("template") + TEMPLATE[0];
	}
	
	private void addAccess(Context context, Map<String, AdditionalRole> access) {
		boolean disableAdditional = access.size() == 0;
		context.put("disableAdditional", disableAdditional);
		if (!disableAdditional) {
			List<AdditionalRoleGroup> roleSets = sortAdditionalRoles(access);
			context.put("additionalRoleGroups", roleSets);
		}
	}
	
	private List<AdditionalRoleGroup> sortAdditionalRoles(Map<String, AdditionalRole> access) {
		HashMap<String, AdditionalRoleGroup> roleMap = new HashMap<String, AdditionalRoleGroup>();
		for (String roleId : access.keySet()) {
			//take key (role group label) from role prefix (until last dot). 
			//.aaa.eee.iii.roleid => key=.aaa.eee.iii 
			int index = roleId.lastIndexOf(".");
			String key = (index >= 0) ? roleId.substring(0, index) : "";
			AdditionalRoleGroup arg = roleMap.get(key);
			if(arg == null) {
				arg = new AdditionalRoleGroup(authzGroupService.getRoleGroupName(key));
				roleMap.put(key, arg);
			}
			
			List <AdditionalRole> roles = arg.getRoles();
			roles.add(access.get(roleId));
		}
		List<AdditionalRoleGroup> roleSets = new ArrayList<AdditionalRoleGroup>();
		roleSets.addAll(roleMap.values());
		//order categories by name
		Collections.sort(roleSets);
		//order roles in categories by name
		for (AdditionalRoleGroup roleGroup: roleSets) {
			Collections.sort(roleGroup.getRoles());
		}
		return roleSets;
	}

	private List<String> getAdditionRoles(AuthzGroup realm) {
		List<String> roles = new ArrayList<String>();
		for (Role role : (Set<Role>)realm.getRoles()) {
			if (!authzGroupService.isRoleAssignable(role.getId())) {
				roles.add(authzGroupService.getRoleName(role.getId()));
			}
		}
		// Make sure it's always in the same order.
		Collections.sort(roles);
		return roles;
	}
	
	private List<String> getAdditionRoles(SiteInfo siteInfo) {
		List<String> roles = new ArrayList<String>();
		for (String roleId : siteInfo.additionalRoles) {
			roles.add(authzGroupService.getRoleName(roleId));
		}
		// Make sure it's always in the same order.
		Collections.sort(roles);
		return roles;
	}
	
	private Map<String, AdditionalRole> getAdditionalAccess(AuthzGroup realm) {
		// Check for .auth/.anon
		Map<String, AdditionalRole> additionalRoles = loadAdditionalRoles(); 
		for (Role role : (Set<Role>)realm.getRoles()) {
			if (!authzGroupService.isRoleAssignable(role.getId())) {
				AdditionalRole additionalRole = additionalRoles.get(role.getId());
				if (additionalRole == null) {
					additionalRole = new AdditionalRole();
					additionalRole.id = role.getId();
					additionalRole.name = authzGroupService.getRoleName(role.getId());
					additionalRole.editable = false;
					additionalRoles.put(additionalRole.id, additionalRole);
				}
				additionalRole.granted = true;
			}
		}
		return additionalRoles;
	}
	
	/**
	 * Load the possible additional roles for this site.
	 * This should really all be behind an API.
	 * @return
	 */
	protected Map<String, AdditionalRole> loadAdditionalRoles() {
		Map<String, AdditionalRole> additionalRoles = new HashMap<String, AdditionalRole>();
		for (String roleId : authzGroupService.getAdditionalRoles()) {			
				AdditionalRole role = new AdditionalRole();
				role.id = roleId;
				role.name = authzGroupService.getRoleName(role.id);
				role.editable = true;
				additionalRoles.put(role.id, role);
			}
		return additionalRoles;
	}

	private void toolSelectionIntoContext(Context context, SessionState state, String siteType, String siteId, String overridePageOrderSiteTypes) {
		List toolRegistrationList;
		List toolRegistrationSelectedList;
		toolRegistrationSelectedList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		toolRegistrationList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		context.put(STATE_TOOL_REGISTRATION_LIST, toolRegistrationList);
		if (toolRegistrationSelectedList != null && toolRegistrationList != null)
		{
			// see if any tool is added outside of Site Info tool, which means the tool is outside of the allowed tool set for this site type
			context.put("extraSelectedToolList", state.getAttribute(STATE_EXTRA_SELECTED_TOOL_LIST));
		}
		// put tool title into context if PageOrderHelper is enabled
		pageOrderToolTitleIntoContext(context, state, siteType, false, overridePageOrderSiteTypes);

		context.put("check_home", state
				.getAttribute(STATE_TOOL_HOME_SELECTED));
		context.put("selectedTools", orderToolIds(state, checkNullSiteType(state, siteType, siteId), toolRegistrationSelectedList, false));
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

		context.put("homeToolId", TOOL_ID_HOME);
		
		// put the lti tools information into context
		context.put("ltiTools", state.getAttribute(STATE_LTITOOL_SELECTED_LIST));
		context.put("oldLtiTools", state.getAttribute(STATE_LTITOOL_EXISTING_SELECTED_LIST));
		context.put("ltitool_id_prefix", LTITOOL_ID_PREFIX);
	}

	/**
	 * prepare lti tool information in context and state variables
	 * @param context
	 * @param state
	 * @param site
	 * @param updateToolRegistration
	 */
	private void buildLTIToolContextForTemplate(Context context,
			SessionState state, Site site, boolean updateToolRegistration) {
		List<Map<String, Object>> visibleTools, allTools;
		// get the visible and all (including stealthed) list of lti tools
		visibleTools = m_ltiService.getTools(null,null,0,0);
		if (site == null)
			allTools = visibleTools;
		else
			allTools = m_ltiService.getToolsDao(null,null,0,0,site.getId());
      
		if (visibleTools != null && !visibleTools.isEmpty())
		{
			HashMap<String, Map<String, Object>> ltiTools = new HashMap<String, Map<String, Object>>();
			// get invoke count for all lti tools
			List<Map<String,Object>> contents = m_ltiService.getContents(null,null,0,0);
			HashMap<String, Map<String, Object>> linkedLtiContents = new HashMap<String, Map<String, Object>>();
			for ( Map<String,Object> content : contents ) {
				String ltiToolId = content.get(m_ltiService.LTI_TOOL_ID).toString();
				String siteId = StringUtils.trimToNull((String) content.get(m_ltiService.LTI_SITE_ID));
				if (siteId != null)
				{
					// whether the tool is already enabled in site
					String pstr = (String) content.get(LTIService.LTI_PLACEMENT);
					if (StringUtils.trimToNull(pstr) != null && site != null)
					{
						// the lti tool is enabled in the site
						ToolConfiguration toolConfig = SiteService.findTool(pstr);
						if (toolConfig != null && toolConfig.getSiteId().equals(siteId))
						{
							Map<String, Object> m = new HashMap<String, Object>();
							Map<String, Object> ltiToolValues = m_ltiService.getTool(Long.valueOf(ltiToolId));
							if ( ltiToolValues != null )
							{
								m.put("toolTitle", ltiToolValues.get(LTIService.LTI_TITLE));
								m.put("pageTitle", ltiToolValues.get(LTIService.LTI_PAGETITLE));
								m.put(LTIService.LTI_TITLE, (String) content.get(LTIService.LTI_TITLE));
								m.put("contentKey", content.get(LTIService.LTI_ID));
								linkedLtiContents.put(ltiToolId, m);
							}
						}
					}
				}
			}
         
         // First search list of visibleTools for those not selected (excluding stealthed tools)
			for (Map<String, Object> toolMap : visibleTools ) {
				String ltiToolId = toolMap.get("id").toString();
				String siteId = StringUtils.trimToNull((String) toolMap.get(m_ltiService.LTI_SITE_ID));
				toolMap.put("selected", linkedLtiContents.containsKey(ltiToolId));
				if ( siteId == null || (site != null && siteId.equals(site.getId())))
				{
					// only show the system-range lti tools (siteId = null)
					// or site-range lti tools for current site (siteId != null), and current siteId equals to current site's id
					ltiTools.put(ltiToolId, toolMap);
				}
			}
         
         // Second search list of allTools for those already selected (including stealthed)
			for (Map<String, Object> toolMap : allTools ) {
				String ltiToolId = toolMap.get("id").toString();
				boolean selected = linkedLtiContents.containsKey(ltiToolId);
				toolMap.put( "selected", selected);
				if ( selected && ltiTools.get(ltiToolId)==null )
            {
               ltiTools.put(ltiToolId, toolMap);
            }
			}
         
         
			state.setAttribute(STATE_LTITOOL_LIST, ltiTools);
			state.setAttribute(STATE_LTITOOL_EXISTING_SELECTED_LIST, linkedLtiContents);
			context.put("ltiTools", ltiTools);
			context.put("selectedLtiTools",linkedLtiContents);
			
			if (updateToolRegistration)
				{
				// put the selected lti tool ids into state attribute
				List<String> idSelected = state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST) != null? (List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST):new ArrayList<String>();
				for(String ltiId :linkedLtiContents.keySet())
				{
					// attach the prefix
					idSelected.add(LTITOOL_ID_PREFIX+ltiId);
				}
				state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, idSelected);
			}
		}
	}

	private String getSelectionString(List selections, int numSelections) {
		StringBuffer eidBuffer = new StringBuffer();
		for (int i = 0; i < numSelections;i++)
		{
			eidBuffer.append(selections.get(i)).append(",");
		}
		String eid = eidBuffer.toString();
		// trim off last ","
		if (eid.endsWith(","))
			eid = eid.substring(0, eid.lastIndexOf(","));
		return eid;
	}

	/**
	 * get CourseSet from CourseManagementService and update state attribute
	 * @param state
	 * @return
	 */
	private Set getCourseSet(SessionState state) {
		Set courseSet = null;
		if (state.getAttribute(STATE_COURSE_SET) != null)
		{
			courseSet = (Set) state.getAttribute(STATE_COURSE_SET);
		}
		else
		{
			courseSet = cms.getCourseSets();
			state.setAttribute(STATE_COURSE_SET, courseSet);
		}
		return courseSet;
	}

	/**
	 * put customized page title into context during an editing process for an existing site and the PageOrder tool is enabled for this site
	 * @param context
	 * @param state
	 * @param siteType
	 * @param newSite
	 */
	private void pageOrderToolTitleIntoContext(Context context, SessionState state, String siteType, boolean newSite, String overrideSitePageOrderSetting) {
		// check if this is an existing site and PageOrder is enabled for the site. If so, show tool title
		if (!newSite && notStealthOrHiddenTool("sakai-site-pageorder-helper") && isPageOrderAllowed(siteType, overrideSitePageOrderSetting))
		{
			// the actual page titles
			context.put(STATE_TOOL_REGISTRATION_TITLE_LIST, state.getAttribute(STATE_TOOL_REGISTRATION_TITLE_LIST));
			context.put("allowPageOrderHelper", Boolean.TRUE);
		}
		else
		{
			context.put("allowPageOrderHelper", Boolean.FALSE);
		}
	}

	/**
	 * Depending on institutional setting, all or part of the CourseSet list will be shown in the dropdown list in find course page
	 * for example, sakai.properties could have following setting:
	 * sitemanage.cm.courseset.categories.count=1
	 * sitemanage.cm.courseset.categories.1=Department
	 * Hence, only CourseSet object with category of "Department" will be shown
	 * @param courseSets
	 * @return
	 */
	private Set<CourseSet> filterCourseSetList(Set<CourseSet> courseSets) {
		if (ServerConfigurationService.getStrings("sitemanage.cm.courseset.categories") != null) {
			List<String> showCourseSetTypes = new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("sitemanage.cm.courseset.categories")));
			Set<CourseSet> rv = new HashSet<CourseSet>();
			for(CourseSet courseSet:courseSets)
			{
				if (showCourseSetTypes.contains(courseSet.getCategory()))
				{
					rv.add(courseSet);
				}
			}
			courseSets = rv;
		}
		return courseSets;
	}

	/**
	 * put all info necessary for importing site into context
	 * @param context
	 * @param site
	 */
	private void putImportSitesInfoIntoContext(Context context, Site site, SessionState state, boolean ownTypeOnly) {
		context.put("currentSite", site);
		context.put("importSiteList", state
				.getAttribute(STATE_IMPORT_SITES));
		context.put("sites", SiteService.getSites(
				org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
				ownTypeOnly?site.getType():null, null, null, SortType.TITLE_ASC, null));
	}

	/**
	 * get the titles of list of selected provider courses into context
	 * @param context
	 * @param state
	 * @return true if there is selected provider course, false otherwise
	 */
	private boolean putSelectedProviderCourseIntoContext(Context context, SessionState state) {
		boolean rv = false;
		if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null) {
			
			List<String> providerSectionList = (List<String>) state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
			context.put("selectedProviderCourse", providerSectionList);
			context.put("selectedProviderCourseDescription", state.getAttribute(STATE_ADD_CLASS_PROVIDER_DESCRIPTION_CHOSEN));
			if (providerSectionList != null && providerSectionList.size() > 0)
			{
				// roster attached
				rv = true;
			}

			HashMap<String, String> providerSectionListTitles = new HashMap<String, String>();
			if (providerSectionList != null)
			{
				for (String providerSectionId : providerSectionList)
				{
					try
					{
						Section s = cms.getSection(providerSectionId);
						if (s != null)
						{
							providerSectionListTitles.put(s.getEid(), s.getTitle()); 
						}
					}
					catch (IdNotFoundException e)
					{
						providerSectionListTitles.put(providerSectionId, providerSectionId);
						M_log.warn("putSelectedProviderCourseIntoContext Cannot find section " + providerSectionId);
					}
				}
				context.put("size", Integer.valueOf(providerSectionList.size() - 1));
			}
			context.put("selectedProviderCourseTitles", providerSectionListTitles);		
		}
		return rv;
	}

	/**
	 * whether the PageOrderHelper is allowed to be shown in this site type
	 * @param siteType
	 * @param overrideSitePageOrderSetting
	 * @return
	 */
	private boolean isPageOrderAllowed(String siteType, String overrideSitePageOrderSetting) {
		if (overrideSitePageOrderSetting != null && Boolean.valueOf(overrideSitePageOrderSetting))
		{
			// site-specific setting, show PageOrder tool
			return true;
		}
		else
		{
			// read the setting from sakai properties
			boolean rv = true;
			String hidePageOrderSiteTypes = ServerConfigurationService.getString(SiteConstants.SAKAI_PROPERTY_HIDE_PAGEORDER_SITE_TYPES, "");
			if ( hidePageOrderSiteTypes.length() != 0)
			{
				if (new ArrayList<String>(Arrays.asList(StringUtils.split(hidePageOrderSiteTypes, ","))).contains(siteType))
				{
					rv = false;
				}
			}
			return rv;
		}
	}

	/*
	 * SAK-16600 TooGroupMultiples come from toolregistrationselectedlist
	 * @param	state		current session 
	 * @param	list		list of all tools
	 * @return	set of tools that are multiples
	 */
	private Map getToolGroupMultiples(SessionState state, List list) {
		Set multipleToolIdSet = (Set) state.getAttribute(STATE_MULTIPLE_TOOL_ID_SET);
		Map multipleToolIdTitleMap = state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP) != null? (Map) state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP):new HashMap();
		Map<String,List> toolGroupMultiples = new HashMap<String, List>();
		if ( list != null )
		{
			for(Iterator iter = list.iterator(); iter.hasNext();)
			{
				String toolId = ((MyTool)iter.next()).getId();
				String originId = findOriginalToolId(state, toolId);
				// is this tool in the list of multipeToolIds?
				if (multipleToolIdSet.contains(originId)) {
					// is this the original tool or a multiple having uniqueId+originalToolId?
					if (!originId.equals(toolId)) {
						if (!toolGroupMultiples.containsKey(originId)) {
							toolGroupMultiples.put(originId,	 new ArrayList());
						}
						List tools = toolGroupMultiples.get(originId);
						MyTool tool = new MyTool();
						tool.id = toolId;
						tool.title = (String) multipleToolIdTitleMap.get(toolId);
						// tool comes from toolRegistrationSelectList so selected should be true
						tool.selected = true;
						// is a toolMultiple ever *required*?
						tools.add(tool);
						// update the tools list for this tool id
						toolGroupMultiples.put(originId, tools);
					}
				}
			}
		}
		return toolGroupMultiples;
	}

	private void multipleToolIntoContext(Context context, SessionState state) {
		// titles for multiple tool instances
		context.put(STATE_MULTIPLE_TOOL_ID_SET, state.getAttribute(STATE_MULTIPLE_TOOL_ID_SET ));
		context.put(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP ));
		context.put(STATE_MULTIPLE_TOOL_CONFIGURATION, state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION));
		context.put(STATE_MULTIPLE_TOOL_INSTANCE_SELECTED, state.getAttribute(STATE_MULTIPLE_TOOL_INSTANCE_SELECTED));
	}
	
	
	// SAK-23468 
	private void setNewSiteStateParameters(Site site, SessionState state){
		if (site != null) {
			state.setAttribute(STATE_NEW_SITE_STATUS_ISPUBLISHED, Boolean.valueOf(site.isPublished()));
			state.setAttribute(STATE_NEW_SITE_STATUS_ID, site.getId());
			state.setAttribute(STATE_NEW_SITE_STATUS_TITLE, site.getTitle());
		}
	}	

	// SAK-23468 
	private void clearNewSiteStateParameters(SessionState state) {
		state.removeAttribute(STATE_NEW_SITE_STATUS_ISPUBLISHED);
		state.removeAttribute(STATE_NEW_SITE_STATUS_ID);
		state.removeAttribute(STATE_NEW_SITE_STATUS_TITLE);
		state.removeAttribute(STATE_DUPE_SITE_URL);
	}
	
	/**
	 * show site skin and icon selections or not
	 * @param state
	 * @param site
	 * @param siteInfo
	 */
	private void skinIconSelection(Context context, SessionState state, boolean isCourseSite, Site site, SiteInfo siteInfo) {
		// 1. the skin list
		// For course site, display skin list based on "disable.course.site.skin.selection" value set with sakai.properties file. The setting defaults to be false.
		boolean disableCourseSkinChoice = ServerConfigurationService.getString("disable.course.site.skin.selection", "false").equals("true");
		//Do we allow them to use a specific icon for the site. Related to SAK-29458 for Sakai 11
		//Default to true for now. Requires the portal.siteicon.allow value in the sakai.properties file.
		context.put("allowSiteIcon",ServerConfigurationService.getBoolean("portal.siteicon.allow",true));
		// For non-course site, display skin list based on "disable.noncourse.site.skin.selection" value set with sakai.properties file. The setting defaults to be true.
		boolean disableNonCourseSkinChoice = ServerConfigurationService.getString("disable.noncourse.site.skin.selection", "true").equals("true");
		if ((isCourseSite && !disableCourseSkinChoice) || (!isCourseSite && !disableNonCourseSkinChoice))
		{
			context.put("allowSkinChoice", Boolean.TRUE);
			context.put("skins", state.getAttribute(STATE_ICONS));
		}
		else
		{
			context.put("allowSkinChoice", Boolean.FALSE);
		}
			
		if (siteInfo != null && StringUtils.trimToNull(siteInfo.getIconUrl()) != null) 
		{
			context.put("selectedIcon", siteInfo.getIconUrl());
		} else if (site != null && site.getIconUrl() != null) 
		{
			context.put("selectedIcon", site.getIconUrl());
		}
	}

	/**
	 * 
	 */
	public void doPageOrderHelper(RunData data) {
               	SessionState state = ((JetspeedRunData) data)
			.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// pass in the siteId of the site to be ordered (so it can configure
		// 		// sites other then the current site)
		//
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

	/**
	 * Launch the Link Helper Tool -- for setting/clearing parent site
	 * 
	 * @see case 12  // TODO
	 * 
	 */
	public void doLinkHelper(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// pass in the siteId of the site to be ordered (so it can configure
		// sites other then the current site)
		SessionManager.getCurrentToolSession().setAttribute(
				HELPER_ID + ".siteId", ((Site) getStateSite(state)).getId());

		// launch the helper
		startHelper(data.getRequest(), "sakai-site-manage-link-helper");
	}

	/**
	 * Launch the External Tools Helper -- For managing external tools
	 */
	public void doExternalHelper(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// pass in the siteId of the site to be ordered (so it can configure
		// sites other then the current site)
		SessionManager.getCurrentToolSession().setAttribute(
				HELPER_ID + ".siteId", ((Site) getStateSite(state)).getId());

		// launch the helper
		startHelper(data.getRequest(), "sakai.basiclti.admin.helper");
	}
	
	public void doUserAuditEventLog(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// pass in the siteId of the site to be ordered (so it can configure
		// sites other then the current site)
		SessionManager.getCurrentToolSession().setAttribute(
				HELPER_ID + ".siteId", ((Site) getStateSite(state)).getId());

		// launch the helper
		startHelper(data.getRequest(), "sakai.useraudit");
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
		long max_bytes = 1024 * 1024;
		try {
			max_bytes = Long.parseLong(max_file_size_mb) * 1024 * 1024;
		} catch (Exception e) {
			// if unable to parse an integer from the value
			// in the properties file, use 1 MB as a default
			max_file_size_mb = "1";
			max_bytes = 1024 * 1024;
			M_log.error(this + ".doUpload_Mtrl_Frm_File: wrong setting of content.upload.max = " + max_file_size_mb, e);
		}
		if (fileFromUpload == null) {
			// "The user submitted a file to upload but it was too big!"
			addAlert(state, rb.getFormattedMessage("importFile.size", new Object[]{max_file_size_mb}));
		} else if (fileFromUpload.getFileName() == null
				|| fileFromUpload.getFileName().length() == 0) {
			addAlert(state, rb.getString("importFile.choosefile"));
		} else {
			//Need some other kind of input stream?
			ResetOnCloseInputStream fileInput = null;
			InputStream fileInputStream = null;
			long fileSize=0;
			try { 
				// Write to temp file, this should probably be in the velocity util?
				File tempFile = null;
				tempFile = File.createTempFile("importFile", ".tmp");
				// Delete temp file when program exits.
				tempFile.deleteOnExit();
	
				fileInputStream = fileFromUpload.getInputStream();
				
				FileOutputStream outBuf = new FileOutputStream(tempFile);
				byte[] bytes = new byte[102400];
				int read = 0;
				while ((read = fileInputStream.read(bytes)) != -1) {
					outBuf.write(bytes, 0, read);
				}

				outBuf.flush();
				outBuf.close();
			
				fileSize = tempFile.length();
				fileInput = new ResetOnCloseInputStream(tempFile);
			}
			catch (FileNotFoundException fnfe) {
				M_log.error("FileNotFoundException creating temp import file",fnfe);
			}
			catch (IOException ioe) {
				M_log.error("IOException creating temp import file",ioe);
			}
			finally {
				IOUtils.closeQuietly(fileInputStream);
			}

			if (fileSize >= max_bytes) {
				addAlert(state, rb.getFormattedMessage("importFile.size", new Object[]{max_file_size_mb}));
			}
			else if (fileSize > 0) {

				if (fileInput != null && importService.isValidArchive(fileInput)) {
					ImportDataSource importDataSource = importService
							.parseFromFile(fileInput);
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

			IOUtils.closeQuietly(fileInput);
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
		state.removeAttribute(DIRECT_ZIP_IMPORT_SITES);
		state.removeAttribute(CLASSIC_ZIP_FILE_NAME);
		state.removeAttribute(SESSION_CONTEXT_ID);
		state.removeAttribute(IMPORT_DATA_SOURCE);

		state.setAttribute(STATE_TEMPLATE_INDEX, "48");

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
		String search = StringUtils.trimToNull(data.getParameters().getString(
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

	/**
	 * 
	 * @param state
	 * @param context
	 * @param site
	 * @return true if there is any roster attached, false otherwise
	 */
	private boolean coursesIntoContext(SessionState state, Context context,
			Site site) {
		boolean rv = false;
		List providerCourseList = SiteParticipantHelper.getProviderCourseList((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
		if (providerCourseList != null && providerCourseList.size() > 0) {
			rv = true;
			state.setAttribute(SITE_PROVIDER_COURSE_LIST, providerCourseList);
			
			Hashtable<String, String> sectionTitles = new Hashtable<String, String>();
			for(int i = 0; i < providerCourseList.size(); i++)
			{
				String sectionId = (String) providerCourseList.get(i);
				try
				{
					Section s = cms.getSection(sectionId);
					if (s != null)
					{
						sectionTitles.put(sectionId, s.getTitle());
					}
				}
				catch (IdNotFoundException e)
				{
					sectionTitles.put(sectionId, sectionId);
					M_log.warn("coursesIntoContext: Cannot find section " + sectionId);
				}
			}
			context.put("providerCourseTitles", sectionTitles);
			context.put("providerCourseList", providerCourseList);
		}

		// put manual requested courses into context
		boolean rv2 = courseListFromStringIntoContext(state, context, site, STATE_CM_REQUESTED_SECTIONS, STATE_CM_REQUESTED_SECTIONS, "cmRequestedCourseList");
		
		// put manual requested courses into context
		boolean rv3 = courseListFromStringIntoContext(state, context, site, PROP_SITE_REQUEST_COURSE, SITE_MANUAL_COURSE_LIST, "manualCourseList");
		
		return (rv || rv2 || rv3);
	}

	/**
	 * 
	 * @param state
	 * @param context
	 * @param site
	 * @param site_prop_name
	 * @param state_attribute_string
	 * @param context_string
	 * @return true if there is any roster attached; false otherwise
	 */
	private boolean courseListFromStringIntoContext(SessionState state, Context context, Site site, String site_prop_name, String state_attribute_string, String context_string) {
		boolean rv = false;
		String courseListString = StringUtils.trimToNull(site != null?site.getProperties().getProperty(site_prop_name):null);
		if (courseListString != null) {
			rv = true;
			List courseList = new Vector();
			if (courseListString.indexOf("+") != -1) {
				courseList = new ArrayList(Arrays.asList(groupProvider.unpackId(courseListString)));
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
						{
							soList.add(new SectionObject(s));
						}
					}
					catch (IdNotFoundException e)
					{
						M_log.warn("courseListFromStringIntoContext: cannot find section " + courseEid);
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
		return rv;
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
		
		// bjones86 - SAK-23256
		setTermListForContext( context, state, true, false ); //-> future terms only
		
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
		String term = (String) state.getAttribute(STATE_TERM_VIEW_SELECTED);
		Map<String,String> termProp = null;
		if(term != null && !"".equals(term) && !TERM_OPTION_ALL.equals(term)){
			termProp = new HashMap<String,String>();
			termProp.put(Site.PROP_SITE_TERM, term);
		}
		
		// if called from the site list page
		if (((String) state.getAttribute(STATE_TEMPLATE_INDEX)).equals("0")) {
			search = StringUtils.trimToNull((String) state
					.getAttribute(STATE_SEARCH));
			if (SecurityService.isSuperUser()) {
				// admin-type of user
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {
					if (view.equals(SiteConstants.SITE_TYPE_ALL)) {
						// search for non-user sites, using
						// the criteria
						size = SiteService
								.countSites(
										org.sakaiproject.site.api.SiteService.SelectionType.NON_USER,
										null, search, termProp);
					} else if (view.equals(SiteConstants.SITE_TYPE_MYWORKSPACE)) {
						// search for a specific user site
						// for the particular user id in the
						// criteria - exact match only
						try {
							SiteService.getSite(SiteService
									.getUserSiteId(search));
							size++;
						} catch (IdUnusedException e) {
						}
					} else if (view.equalsIgnoreCase(SiteConstants.SITE_TYPE_DELETED)) {
						size = SiteService
								.countSites(
										org.sakaiproject.site.api.SiteService.SelectionType.ANY_DELETED,
										null, search, null);
					} else {
						// search for specific type of sites
						size = SiteService
								.countSites(
										org.sakaiproject.site.api.SiteService.SelectionType.NON_USER,
										view, search, termProp);
					}
				}
			} else {
				Site userWorkspaceSite = null;
				try {
					userWorkspaceSite = SiteService.getSite(SiteService
							.getUserSiteId(userId));
				} catch (IdUnusedException e) {
					M_log.error(this + "sizeResources, template index = 0: Cannot find user "
							+ SessionManager.getCurrentSessionUserId()
							+ "'s My Workspace site.", e);
				}

				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {

					org.sakaiproject.site.api.SiteService.SelectionType selectionType;
					if (ServerConfigurationService.getBoolean("sitesetup.show.unpublished", false)) {
						selectionType = org.sakaiproject.site.api.SiteService.SelectionType.MEMBER;
					} else {
						selectionType = org.sakaiproject.site.api.SiteService.SelectionType.ACCESS;
					}

					if (view.equals(SiteConstants.SITE_TYPE_ALL)) {
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
										selectionType,
										null, search, termProp);
					} else if (view.equalsIgnoreCase(SiteConstants.SITE_TYPE_DELETED)) {
						size += SiteService
								.countSites(
										org.sakaiproject.site.api.SiteService.SelectionType.DELETED,
										null,search, null);
					} else if (view.equals(SiteConstants.SITE_TYPE_MYWORKSPACE)) {
						// get the current user MyWorkspace site
						try {
							SiteService.getSite(SiteService
									.getUserSiteId(userId));
							size++;
						} catch (IdUnusedException e) {
						}
					} else if (view.equals(SiteConstants.SITE_ACTIVE)) {
						view = null;
						size += SiteService.countSites(
							org.sakaiproject.site.api.SiteService.SelectionType.PUBVIEW,
								view, search, termProp);
					} else if (view.equals(SiteConstants.SITE_INACTIVE)) {
						view = null;
						size += SiteService.countSites(
										org.sakaiproject.site.api.SiteService.SelectionType.INACTIVE_ONLY,
										null, search, termProp);
					} else {
						// search for specific type of sites
						size += SiteService.countSites(
										selectionType,
										view, search, termProp);
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
		String search = StringUtils.trimToNull((String) state
				.getAttribute(STATE_SEARCH));

		// if called from the site list page
		if (((String) state.getAttribute(STATE_TEMPLATE_INDEX)).equals("0")) {
			// get sort type
			SortType sortType = null;
			String sortBy = (String) state.getAttribute(SORTED_BY);
			boolean sortAsc = (Boolean.valueOf((String) state
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
			} else if (sortBy.equals(SortType.ID_ASC.toString())){
				sortType = sortAsc ? SortType.ID_ASC
						: SortType.ID_DESC;
			} else if (sortBy.equals(SortType.SOFTLY_DELETED_ASC.toString())) {
				sortType = sortAsc ? SortType.SOFTLY_DELETED_ASC
						: SortType.SOFTLY_DELETED_DESC;
			}
			
			String term = (String) state.getAttribute(STATE_TERM_VIEW_SELECTED);
			Map<String,String> termProp = null;
			if(term != null && !"".equals(term) && !TERM_OPTION_ALL.equals(term)){
				termProp = new HashMap<String,String>();
				termProp.put(Site.PROP_SITE_TERM, term);
			}
			
			if (SecurityService.isSuperUser()) {
				// admin-type of user
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {
					if (view.equals(SiteConstants.SITE_TYPE_ALL)) {
						// search for non-user sites, using the
						// criteria
						return SiteService
								.getSites(
										org.sakaiproject.site.api.SiteService.SelectionType.NON_USER,
										null, search, termProp, sortType,
										new PagingPosition(first, last));
					} else if (view.equalsIgnoreCase(SiteConstants.SITE_TYPE_MYWORKSPACE)) {
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
					} else if (view.equalsIgnoreCase(SiteConstants.SITE_TYPE_DELETED)) {
						return SiteService
						.getSites(
								org.sakaiproject.site.api.SiteService.SelectionType.ANY_DELETED,
								null,
								search, null, sortType,
								new PagingPosition(first, last));
					} else {
						// search for a specific site
						return SiteService
								.getSites(
										org.sakaiproject.site.api.SiteService.SelectionType.ANY,
										view, search, termProp, sortType,
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
					M_log.error(this + "readResourcesPage template index = 0 :Cannot find user " + SessionManager.getCurrentSessionUserId() + "'s My Workspace site.", e);
				}
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {

					org.sakaiproject.site.api.SiteService.SelectionType selectionType;

					if (ServerConfigurationService.getBoolean("sitesetup.show.unpublished", false)) {
						selectionType = org.sakaiproject.site.api.SiteService.SelectionType.MEMBER;
					} else {
						selectionType = org.sakaiproject.site.api.SiteService.SelectionType.ACCESS;
					}

					if (view.equals(SiteConstants.SITE_TYPE_ALL)) {
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
												selectionType,
												null, search, termProp, sortType,
												new PagingPosition(first, last)));
					}
					else if (view.equals(SiteConstants.SITE_TYPE_MYWORKSPACE)) {
						// get the current user MyWorkspace site
						try {
							rv.add(SiteService.getSite(SiteService.getUserSiteId(userId)));
						} catch (IdUnusedException e) {
						}
					} else if (view.equalsIgnoreCase(SiteConstants.SITE_TYPE_DELETED)) {
						return SiteService
						.getSites(
								org.sakaiproject.site.api.SiteService.SelectionType.DELETED,
								null,
								search, null, sortType,
								new PagingPosition(first, last));
					} else if (view.equals(SiteConstants.SITE_ACTIVE)) {
						rv.addAll(SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.PUBVIEW,
								null, search, termProp, sortType,
								new PagingPosition(first, last)));
					} else if (view.equals(SiteConstants.SITE_INACTIVE)) {
						rv.addAll(SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.INACTIVE_ONLY,
								null, search, termProp, sortType,
								new PagingPosition(first, last)));
					} else {
						rv.addAll(SiteService
								.getSites(
										selectionType,
										view, search, termProp, sortType,
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
						.iterator(), new SiteComparator(sortedBy,sortedAsc,comparator_locale));
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
		List<String> currentSiteTools = originalToolIds((List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST), state); // String
		
		//all importable tools. 
		//depnding on the config, either one could be selected, which is valid
		List<String> allImportableTools = new ArrayList<String>(importTools());
		
		if(isAddMissingToolsOnImportEnabled()) {
			if (allImportableTools != null) {
				for (int i = 0; i < allImportableTools.size(); i++) {
					// just verify a valid tool was chosen
					String toolId = (String) allImportableTools.get(i);
					if (params.getStrings(toolId) != null) {
						importTools.put(toolId, new ArrayList(Arrays.asList(params.getStrings(toolId))));
						if (!anyToolSelected) {
							anyToolSelected = true;
						}
					}
				}
			}
		} else {
			if (currentSiteTools != null) {
				for (int i = 0; i < currentSiteTools.size(); i++) {
					// any tools chosen from import sites?
					String toolId = (String) currentSiteTools.get(i);
					if (params.getStrings(toolId) != null) {
						importTools.put(toolId, new ArrayList(Arrays.asList(params.getStrings(toolId))));
						if (!anyToolSelected) {
							anyToolSelected = true;
						}
					}
				}
			}
		}
		
		M_log.debug("tools to import: " + importTools);
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
		
		if (toolList != null)
		{
			for (int i = 0; i < toolList.size() && !fromENW; i++) {
				String toolId = (String) toolList.get(i);
				if ("sakai.mailbox".equals(toolId)
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

		if (state.getAttribute(STATE_INITIALIZED) == null) {
			state.setAttribute(STATE_OVERRIDE_TEMPLATE_INDEX, "1");
		} else {
			List siteTypes = (List) state.getAttribute(STATE_SITE_TYPES);
			if (siteTypes != null) 
			{
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
	
	/**
	 * doMenu_site_hard_delete is called when the Site list tool bar Hard Delete button is
	 * clicked
	 * 
	 */
	public void doMenu_site_hard_delete(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_HARD_DELETE, Boolean.TRUE.toString());
		
		//piggyback on the normal delete method
		doMenu_site_delete(data);
		
	} // doMenu_site_delete
	
	/**
	 * Restore a softly deleted site
	 * 
	 */
	public void doMenu_site_restore(RunData data) {
		SessionState state = ((JetspeedRunData) data) .getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		if (params.getStrings("selectedMembers") == null) {
			addAlert(state, rb.getString("java.nosites"));
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			return;
		}

		String[] toRestore = (String[]) params.getStrings("selectedMembers");

		for (String siteId: toRestore) {
			try {
				Site s = SiteService.getSite(siteId);

				//check if softly deleted
				if(!s.isSoftlyDeleted()){
					M_log.warn("Tried to restore site that has not been marked for deletion: " + siteId);
					continue;
				}

				//reverse it
				s.setSoftlyDeleted(false);
				SiteService.save(s);

			} catch (IdUnusedException e) {
				M_log.warn("Error restoring site:" + siteId + ":" + e.getClass() + ":" + e.getMessage());
				addAlert(state, rb.getString("softly.deleted.invalidsite"));
			} catch (PermissionException e) {
				M_log.warn("Error restoring site:" + siteId + ":" + e.getClass() + ":" + e.getMessage());
				addAlert(state, rb.getString("softly.deleted.restore.nopermission"));
			}
		}
	} // doSite_restore

	public void doSite_delete_confirmed(RunData data) {
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		if (params.getStrings("selectedMembers") == null) {
			M_log.warn("SiteAction.doSite_delete_confirmed selectedMembers null");
			state.setAttribute(STATE_TEMPLATE_INDEX, "0"); // return to the site list
			return;
		}
		List chosenList = new ArrayList(Arrays.asList(params.getStrings("selectedMembers"))); // Site id's of checked sites
		
		boolean hardDelete = false;
		if(StringUtils.equalsIgnoreCase((String)state.getAttribute(STATE_HARD_DELETE), Boolean.TRUE.toString())) {
			hardDelete = true;
		}
		
		if (!chosenList.isEmpty()) {
			
			for (ListIterator i = chosenList.listIterator(); i.hasNext();) {
				String id = (String) i.next();
				String site_title = NULL_STRING;
				if (SiteService.allowRemoveSite(id)) {
					try {
						Site site = SiteService.getSite(id);
						site_title = site.getTitle();
						
						if(hardDelete) {
							//hard delete. call upon all implementing services to hard delete their own content
							doHardDelete(site.getId());
						}
						
						//now delete the site
						SiteService.removeSite(site);
						M_log.debug("Removed site: " + site.getId());
					} catch (IdUnusedException e) {
						M_log.error(this +".doSite_delete_confirmed - IdUnusedException " + id, e);
						addAlert(state, rb.getFormattedMessage("java.couldnt", new Object[]{site_title,id}));
					} catch (PermissionException e) {
						M_log.error(this + ".doSite_delete_confirmed -  PermissionException, site " + site_title + "(" + id + ").", e);
						addAlert(state, rb.getFormattedMessage("java.dontperm", new Object[]{site_title}));
					}
				} else {
					M_log.warn(this + ".doSite_delete_confirmed -  allowRemoveSite failed for site "+ id);
					addAlert(state, rb.getFormattedMessage("java.dontperm", new Object[]{site_title}));
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
		return getStateSite(state, false);

	} // getStateSite

	/**
	 * get the Site object based on SessionState attribute values
	 * 
	 * @param autoContext - If true, we fall back to a context if it exists
	 * @return Site object related to current state; null if no such Site object
	 *         could be found
	 */
	protected Site getStateSite(SessionState state, boolean autoContext) {
		Site site = null;

		if (state.getAttribute(STATE_SITE_INSTANCE_ID) != null) {
			try {
				site = SiteService.getSite((String) state
						.getAttribute(STATE_SITE_INSTANCE_ID));
			} catch (Exception ignore) {
			}
		}
		if ( site == null && autoContext ) {
			String siteId = ToolManager.getCurrentPlacement().getContext();
			try {
				site = SiteService.getSite(siteId);
				state.setAttribute(STATE_SITE_INSTANCE_ID, siteId);
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
			state.setAttribute(STATE_PAGESIZE, Integer.valueOf(200));

			// update
			h.put(siteId, Integer.valueOf(200));
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
		state.setAttribute(STATE_TERM_VIEW_SELECTED, params.getString("termview"));
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
		actionForTemplate("continue", index, params, state, data);

		List<String> pSiteTypes = siteTypeProvider.getTypesForSiteCreation();
		String type = StringUtils.trimToNull(params.getString("itemType"));
		
		if (type == null) {
			addAlert(state, rb.getString("java.select") + " ");
		} else {
			state.setAttribute(STATE_TYPE_SELECTED, type);
			setNewSiteType(state, type);
			if (SiteTypeUtil.isCourseSite(type)) { // UMICH-1035
				// redirect
				redirectCourseCreation(params, state, "selectTerm");
			} else if (SiteTypeUtil.isProjectSite(type)) { // UMICH-1035
				state.setAttribute(STATE_TEMPLATE_INDEX, "13");
			} else if (pSiteTypes != null && pSiteTypes.contains(SiteTypeUtil.getTargetSiteType(type))) {  // UMICH-1035
				// if of customized type site use pre-defined site info and exclude
				// from public listing
				SiteInfo siteInfo = new SiteInfo();
				if (state.getAttribute(STATE_SITE_INFO) != null) {
					siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
				}
				User currentUser = UserDirectoryService.getCurrentUser();
				List<String> idList = new ArrayList<String>();
				idList.add(currentUser.getEid());
				List<String> nameList = new ArrayList<String>();
				nameList.add(currentUser.getDisplayName());
				siteInfo.title = siteTypeProvider.getSiteTitle(type, idList);
				siteInfo.description = siteTypeProvider.getSiteDescription(type, nameList);
				siteInfo.short_description = siteTypeProvider.getSiteShortDescription(type, idList);
				siteInfo.include = false;
				state.setAttribute(STATE_SITE_INFO, siteInfo);

				// skip directly to confirm creation of site
				state.setAttribute(STATE_TEMPLATE_INDEX, "42");
			} else {
				state.setAttribute(STATE_TEMPLATE_INDEX, "13");
			}
			// get the user selected template
			getSelectedTemplate(state, params, type);
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
				M_log.error(this + ".doSite_type" + e.getClass().getName(), e);
				state.removeAttribute(STATE_TEMPLATE_SITE);
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
			
			// bjones86 - SAK-24423 - update site info for joinable site settings
			JoinableSiteSettings.updateSiteInfoFromSitePropertiesOnSelectTemplate( templateSite.getProperties(), siteInfo );
			
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
		else
		{
			// no template selected
			state.removeAttribute(STATE_TEMPLATE_SITE);
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

		if (t != null && t.getStartDate() != null && c.getTimeInMillis() < t.getStartDate().getTime()) {
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
		
		List providerChosenList = (List) state
				.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		
		collectNewSiteInfo(state, params, providerChosenList);
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

			String uniqname = StringUtils.trimToNull(params
					.getString("uniqname"));
			state.setAttribute(STATE_SITE_QUEST_UNIQNAME, uniqname);
			
			// update site information
			SiteInfo siteInfo = state.getAttribute(STATE_SITE_INFO) != null? (SiteInfo) state.getAttribute(STATE_SITE_INFO):new SiteInfo();
			if (params.getString("additional") != null) {
				siteInfo.additional = params.getString("additional");
			}
			state.setAttribute(STATE_SITE_INFO, siteInfo);

			if (option.equalsIgnoreCase("add")) {

				if (state.getAttribute(STATE_FUTURE_TERM_SELECTED) != null
						&& !((Boolean) state
								.getAttribute(STATE_FUTURE_TERM_SELECTED))
								.booleanValue()) {
					// if a future term is selected, do not check authorization
					// uniqname
					if (uniqname == null) {
						addAlert(state, rb.getFormattedMessage("java.author", new Object[]{ServerConfigurationService.getString("officialAccountName")}));
					} else {
						// in case of multiple instructors
						List instructors = new ArrayList(Arrays.asList(uniqname.split(",")));
						for (Iterator iInstructors = instructors.iterator(); iInstructors.hasNext();)
						{
							String eid = StringUtils.trimToEmpty((String) iInstructors.next());
							try {
								UserDirectoryService.getUserByEid(eid);
							} catch (UserNotDefinedException e) {
								addAlert(state, rb.getFormattedMessage("java.validAuthor", new Object[]{ServerConfigurationService.getString("officialAccountName")}));
								M_log.error(this + ".doManual_add_course: cannot find user with eid=" + eid, e);
							}
						}
					}
				}
				if (state.getAttribute(STATE_MESSAGE) == null) {
					if (state.getAttribute(STATE_TEMPLATE_SITE) != null)
					{
						// create site based on template
						doFinish(data);
					}
					else
					{
						if (getStateSite(state) == null) {
							state.setAttribute(STATE_TEMPLATE_INDEX, "13");
						} else {
							state.setAttribute(STATE_TEMPLATE_INDEX, "44");
						}
					}
				}
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
			removeAnyFlagedSection(state, params);
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
				String fieldInput = StringUtils.trimToEmpty(params
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
				state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, Integer.valueOf(oldNumber + newNumber));

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
			state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, Integer.valueOf(	validInputSites>1?validInputSites:1)); 
		}

		// set state attributes
		state.setAttribute(FORM_ADDITIONAL, StringUtils.trimToEmpty(params
				.getString("additional")));

		SiteInfo siteInfo = new SiteInfo();
		if (state.getAttribute(STATE_SITE_INFO) != null) {
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
		}
		if (siteInfo.title == null || siteInfo.title.length() == 0)
		{
			// if SiteInfo doesn't have title, construct the title
			List providerCourseList = (List) state
					.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
			AcademicSession t = (AcademicSession) state.getAttribute(STATE_TERM_SELECTED);
			if ((providerCourseList == null || providerCourseList.size() == 0)
					&& multiCourseInputs.size() > 0) {
				String sectionEid = sectionFieldProvider.getSectionEid(t.getEid(),
						(List) multiCourseInputs.get(0));
				// default title
				String title = sectionFieldProvider.getSectionTitle(t.getEid(), (List) multiCourseInputs.get(0));
				try {
					Section s = cms.getSection(sectionEid);
					title = s != null?s.getTitle():title;
				} catch (IdNotFoundException e) {
					M_log.warn("readCourseSectionInfo: Cannot find section " + sectionEid);
				}
				siteInfo.title = title;
				state.setAttribute(STATE_SITE_INFO, siteInfo);
			}
		}

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
		User u = UserDirectoryService.getCurrentUser();
		if (u != null)
		{
			siteInfo.site_contact_name=u.getDisplayName();
			siteInfo.site_contact_email=u.getEmail();
		}
		state.setAttribute(STATE_SITE_INFO, siteInfo);

		// set tool registration list
		if (!"copy".equals(type))
		{
			setToolRegistrationList(state, type);
		}
	}


	/** SAK16600 insert current site type into context
	 * @param	context			current context 
	 * @param	type				current type
	 * @return	courseSiteType	type of 'course'
	 */	
	private void setTypeIntoContext(Context context, String type) {
		if (type != null && SiteTypeUtil.isCourseSite(type)) {
			context.put("isCourseSite", Boolean.TRUE);
			context.put("isProjectSite", Boolean.FALSE);
		} else {
			context.put("isCourseSite", Boolean.FALSE);
			if (type != null && SiteTypeUtil.isProjectSite(type)) {
				context.put("isProjectSite", Boolean.TRUE);
			}
		}
	}


/** 
 * Set the state variables for tool registration list basd on current site type, save to STATE_TOOL_GROUP_LIST.  This list should include
 * all tool types - normal, home, multiples and blti.  Note that if the toolOrder.xml is in the original format, this list will consist of 
 * all tools in a single group
 * @param state
 * @param is type
 * @param site
 */
private Map<String,List> getTools(SessionState state, String type, Site site) {

	boolean checkhome =  state.getAttribute(STATE_TOOL_HOME_SELECTED) != null ?((Boolean) state.getAttribute(STATE_TOOL_HOME_SELECTED)).booleanValue():true;
	boolean isNewToolOrderType = ServerConfigurationService.getBoolean("config.sitemanage.useToolGroup", false);
	Map<String,List> toolGroup = new LinkedHashMap<String,List>();
	MyTool newTool = null;
	
	File moreInfoDir = new File(moreInfoPath);
	List toolList;
	
	// if this is legacy format toolOrder.xml file, get all tools by siteType
	if (isNewToolOrderType == false) {
		String defaultGroupName = rb.getString("tool.group.default");
		toolGroup.put(defaultGroupName, getOrderedToolList(state, defaultGroupName, type, checkhome));		
	} else {	
		// get all the groups that are available for this site type
		List groups = ServerConfigurationService.getCategoryGroups(SiteTypeUtil.getTargetSiteType(type));
		for(Iterator<String> itr = groups.iterator(); itr.hasNext();) {
			String groupId = itr.next();
			String groupName = getGroupName(groupId);
			toolList = getGroupedToolList(groupId, groupName, type, checkhome, moreInfoDir);
			if (toolList.size() > 0) {
				toolGroup.put(groupName, toolList);
			}
		}

		// add ungroups tools to end of toolGroup list
		String ungroupedName = getGroupName(UNGROUPED_TOOL_TITLE);
		List ungroupedList = getUngroupedTools(ungroupedName,	 toolGroup, state, moreInfoDir, site);
		if (ungroupedList.size() > 0) {
			toolGroup.put(ungroupedName, ungroupedList );
		}	 
	}

	// add external tools to end of toolGroup list
	String externaltoolgroupname = getGroupName(LTI_TOOL_TITLE);
	List externalTools = getLtiToolGroup(externaltoolgroupname, moreInfoDir, site);
	if (externalTools.size() > 0 ) 
		toolGroup.put(externaltoolgroupname, externalTools);
	
	// Home page should be auto-selected
	if (checkhome==true) {
		state.setAttribute(STATE_TOOL_HOME_SELECTED, new Boolean(true));
	}
	
	// refresh selectedList
	List<String> selectedTools = new ArrayList<String>();
	for(Iterator itr = toolGroup.keySet().iterator();  itr.hasNext(); )  {
		String key = (String) itr.next();
		List toolGroupSelectedList =(List) toolGroup.get(key);
		for (Iterator listItr = toolGroupSelectedList.iterator(); listItr.hasNext();) {
			MyTool tool = (MyTool) listItr.next();
			if (tool.selected) {
				selectedTools.add(tool.id);
			}
		}
	}
	
	List toolRegistrationSelectedList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
	
	//If this is the first time through add these selected tools as the default otherwise don't touch this
	if (toolRegistrationSelectedList==null) {
		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, selectedTools);
	}

	return toolGroup;
}

	/**
	 * Get ordered, ungrouped list of tools
	 * @param groupName - name of default group to add all tools
	 * @param type - site type
	 * @param checkhome
	 */
	private List getOrderedToolList(SessionState state, String groupName, String type, boolean checkhome) {
		MyTool newTool = null;
		List toolsInOrderedList = new ArrayList();
		
		// see setToolRegistrationList()
		List toolList = (List)state.getAttribute(STATE_TOOL_REGISTRATION_LIST);

		// mark the required tools
		List requiredTools = ServerConfigurationService.getToolsRequired(SiteTypeUtil.getTargetSiteType(type));
		
		// mark the default tools
		List defaultTools = ServerConfigurationService.getDefaultTools(SiteTypeUtil.getTargetSiteType(type));
		
		// add Home tool only once
		boolean hasHomeTool = false;
		for (Iterator itr = toolList.iterator(); itr.hasNext(); ) 
		{
			MyTool tr = (MyTool)itr.next();
			String toolId = tr.getId();
			if (TOOL_ID_HOME.equals(tr.getId()))
			{
				hasHomeTool = true;
			} 
			if (tr != null) {
				newTool = new MyTool();
				newTool.title = tr.getTitle();
				newTool.id = toolId;
				newTool.description = tr.getDescription();
				newTool.group = groupName;
				// does tool allow multiples and if so are they already defined?
				newTool.multiple = isMultipleInstancesAllowed(toolId); // SAK-16600 - this flag will allow action for template#3 to massage list into new format
				
				if (requiredTools != null && requiredTools.contains(toolId)) {
					newTool.required = true;
				}

				if (defaultTools != null && defaultTools.contains(toolId)) {
					newTool.selected = true;
				}
				toolsInOrderedList.add(newTool);
			}
		}
		
		if (!hasHomeTool)
		{
			// add Home tool to the front of the tool list
			newTool = new MyTool();
			newTool.id = TOOL_ID_HOME;
			newTool.selected = checkhome;
			newTool.required =  false;
			newTool.multiple = false;
			toolsInOrderedList.add(0, newTool);
		} 
		return toolsInOrderedList;
	}

	// SAK-23811
	private List getGroupedToolList(String groupId, String groupName, String type, boolean checkhome, File moreInfoDir ) {
		List toolsInGroup = new ArrayList();
		MyTool newTool = null;
		List toolList = ServerConfigurationService.getToolGroup(groupId);
		// empty list
		if (toolList != null) {
			for(Iterator<String> iter = toolList.iterator(); iter.hasNext();) {
					String id = iter.next();
					String relativeWebPath = null;
					if (id.equals(TOOL_ID_HOME)) { // SAK-23208
						newTool = new MyTool();
						newTool.id = id;
						newTool.title = rb.getString("java.home");
						newTool.description = rb.getString("java.home");
						newTool.selected = checkhome;
						newTool.required = ServerConfigurationService.toolGroupIsRequired(groupId,TOOL_ID_HOME);
						newTool.multiple = false;
					} else {
						Tool tr = ToolManager.getTool(id);
						if (tr != null) 
						{
								String toolId = tr.getId();
								if (isSiteTypeInToolCategory(SiteTypeUtil.getTargetSiteType(type), tr) && notStealthOrHiddenTool(toolId) ) // SAK 23808
								{
									newTool = new MyTool();
									newTool.title = tr.getTitle();
									newTool.id = toolId;
									newTool.description = tr.getDescription();
									newTool.group = groupName;
									newTool.moreInfo =  getMoreInfoUrl(moreInfoDir, toolId);
									newTool.required = ServerConfigurationService.toolGroupIsRequired(groupId,toolId);
									newTool.selected = ServerConfigurationService.toolGroupIsSelected(groupId,toolId);
									// does tool allow multiples and if so are they already defined?
									newTool.multiple = isMultipleInstancesAllowed(toolId); // SAK-16600 - this flag will allow action for template#3 to massage list into new format
								}
						}
					}
					if (newTool != null) {
						toolsInGroup.add(newTool);
						newTool = null;
					}
			}
		}
		M_log.debug(groupName + ": loaded " + new Integer(toolsInGroup.size()).toString() + " tools");
		return toolsInGroup;
		
	}

	/*
	 * Given groupId, return localized name from tools.properties
	 */
	private String getGroupName(String groupId) {
		// undefined group will return standard '[missing key]' error string
		return ToolManager.getLocalizedToolProperty(groupId,"title");
	}
	/*
	 * Using moreInfoDir, if toolId is found in the dir return path otherwise return null
	 */
	private String getMoreInfoImg(File infoDir, String toolId) {
		String moreInfoUrl = null;
		try {
			Collection<File> files = FileUtils.listFiles(infoDir, new WildcardFileFilter(toolId+"*"), null);
			if (files.isEmpty()==false) {
				File mFile = files.iterator().next();
				moreInfoUrl = libraryPath + mFile.getName(); // toolId;
			}
		} catch (Exception e) {
			M_log.info("unable to read moreinfo: " + e.getMessage() );
		}
		return moreInfoUrl;
	}

	/*
	 * Using moreInfoDir, if toolId is found in the dir return path otherwise return null
	 */
	private String getMoreInfoUrl(File infoDir, String toolId) {
		String moreInfoUrl = null;
		try {
			Collection<File> files = FileUtils.listFiles(infoDir, new WildcardFileFilter(toolId+"*"), null);
			if (files.isEmpty()==false) {
					for (File mFile : files) {
						String name = mFile.getName();
						int lastIndexOf = name.lastIndexOf('.');
						String fNameWithOutExtension = name.substring(0,lastIndexOf);
						if (fNameWithOutExtension.equals(toolId)) {
							moreInfoUrl = libraryPath + mFile.getName();
							break;
						}
						
					}
			}
		} catch (Exception e) {
			M_log.info("unable to read moreinfo" + e.getMessage());
		}
		return moreInfoUrl;

	}

	/* SAK-16600 given list, return only those tools tha are BLTI
	 * NOTE - this method is not yet used
	 * @param	selToolList		list where tool.selected = true 
	 * @return	list of tools that are selected and blti
	 */
	private List<String> getToolGroupLtiTools(List<String> selToolList) {
		List<String> onlyLtiTools = new ArrayList<String>();
		List<Map<String, Object>> allTools = m_ltiService.getTools(null, null,0, 0);
		if (allTools != null && !allTools.isEmpty()) {
			for (Map<String, Object> tool : allTools) {
				Set keySet = tool.keySet();
				Integer ltiId = (Integer) tool.get("id");
				if (ltiId != null) {
					String toolId = ltiId.toString();
					if (selToolList.contains(toolId)) {
						onlyLtiTools.add(toolId);
					}
				}
		}
	}
		return onlyLtiTools;
	}

	/* SAK-16600 return selected list with blti tools removed
	 * NOTE this method is not yet used
	 * @param	selToolList		list where tool.selected = true 
	 * @return	list of tools that are selected and not blti
	 */
	private List<String> removeLtiTools(List<String> selToolList) {
		List<String> noLtiList = new ArrayList<String>();
		noLtiList.addAll(selToolList);
		List<String> ltiList = new ArrayList<String>();
		List<Map<String, Object>> allTools = m_ltiService.getTools(null, null,0, 0);
			if (allTools != null && !allTools.isEmpty()) {
			for (Map<String, Object> tool : allTools) {
				Set keySet = tool.keySet();
				Integer ltiId = (Integer) tool.get("id");
				if (ltiId != null) {
					String toolId = ltiId.toString();
					if (!ltiList.contains(toolId)) {
						ltiList.add(toolId);
					}
				}
			}
		}
		boolean result =  noLtiList.removeAll(ltiList);
		return noLtiList;
	}


	private List selectedLTITools(Site site) {
		List selectedLTI = new ArrayList();
		if (site !=null) {
			String siteId = site.getId();
			List<Map<String,Object>> contents = m_ltiService.getContents(null,null,0,0);
			HashMap<String, Map<String, Object>> linkedLtiContents = new HashMap<String, Map<String, Object>>();
			for ( Map<String,Object> content : contents ) {
				String ltiToolId = content.get(m_ltiService.LTI_TOOL_ID).toString();
				String ltiSiteId = StringUtils.trimToNull((String) content.get(m_ltiService.LTI_SITE_ID));
				if ((ltiSiteId!=null) && ltiSiteId.equals(siteId))	{
					selectedLTI.add(ltiToolId);
				}
			}
		}
		return selectedLTI;
	}


	// SAK-16600 find selected flag for ltiTool
	// MAR25 create list of all selected ltitools
	private boolean isLtiToolInSite(Long toolId, String siteId) {
		if (siteId==null) return false;

		Map<String,Object> toolProps= m_ltiService.getContent(toolId, siteId); // getTool(toolId);
		if (toolProps==null){
			return false;
		} else {
			String toolSite = StringUtils.trimToNull((String) toolProps.get(m_ltiService.LTI_SITE_ID));

			return siteId.equals(toolSite);
		}
	}


	/* SAK 16600  if toolGroup mode is active; and toolGroups don't use all available tools, put remaining tools into 
	 * 'ungrouped' group having name 'GroupName'
	 * @param	moreInfoDir		file pointer to directory of MoreInfo content
	 * @param	site				current site
	 * @return	list of MyTool items 
	 */
	private List getUngroupedTools(String ungroupedName, Map<String,List> toolsByGroup, SessionState state, File moreInforDir, Site site) {
		// Get all tools for site
		List ungroupedToolsOld = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		
		// copy the list of tools to avoid ConcurrentModificationException
		List<MyTool> ungroupedTools = new Vector<MyTool>();
		if ( ungroupedToolsOld != null )
			ungroupedTools.addAll(ungroupedToolsOld);
		
		// get all the tool groups that are available for this site  
		for (Iterator<String> toolgroupitr = toolsByGroup.keySet().iterator(); toolgroupitr.hasNext();) {
			String groupName = toolgroupitr.next();
			List toolList = toolsByGroup.get(groupName);
			for (Iterator toollistitr = toolList.iterator(); toollistitr.hasNext();) {
				MyTool ungroupedTool = (MyTool) toollistitr.next();
				if (ungroupedTools.contains(ungroupedTool)) { 
					// remove all tools that are in a toolGroup list
					ungroupedTools.remove(ungroupedTool);
				}
			}
		}
		// remove all toolMultiples
		Map toolMultiples = getToolGroupMultiples(state, ungroupedTools);
		if (toolMultiples != null) {
			for(Iterator tmiter = toolMultiples.keySet().iterator(); tmiter.hasNext();) {
				String toolId = (String) tmiter.next();
				List multiToolList = (List) toolMultiples.get(toolId);
				for (Iterator toollistitr = multiToolList.iterator(); toollistitr.hasNext();) {
					MyTool multitoolId = (MyTool) toollistitr.next();
					if (ungroupedTools.contains(multitoolId)){
						ungroupedTools.remove(multitoolId);
					}
				}
			}
		}
		
		// assign group name to all remaining tools
		for (Iterator listitr = ungroupedTools.iterator(); listitr.hasNext(); ) {
			MyTool tool = (MyTool) listitr.next();
			tool.group = ungroupedName;
		}
		return ungroupedTools;		
	}
	
	/* SAK 16600  Create  list of ltitools to add to toolgroups; set selected for those
	// tools already added to a sites with properties read to add to toolsByGroup list
	 * @param	groupName		name of the current group
	 * @param	moreInfoDir		file pointer to directory of MoreInfo content
	 * @param	site				current site
	 * @return	list of MyTool items 
	 */
	private List getLtiToolGroup(String groupName, File moreInfoDir, Site site) {
		List ltiSelectedTools = selectedLTITools(site);
		List ltiTools = new ArrayList();
		List<Map<String, Object>> allTools;
		String siteId = "";
		if ( site == null )
			allTools = m_ltiService.getTools(null,null,0,0);
		else
		{
			siteId = Objects.toString(site.getId(), "");
			allTools = m_ltiService.getToolsDao(null,null,0,0,siteId);
		}
		if (allTools != null && !allTools.isEmpty()) {
			for (Map<String, Object> tool : allTools) {
				Set keySet = tool.keySet();
				String toolIdString = ObjectUtils.toString(tool.get(m_ltiService.LTI_ID));
				boolean toolStealthed = "1".equals(ObjectUtils.toString(tool.get(m_ltiService.LTI_VISIBLE)));
				boolean ltiToolSelected = ltiSelectedTools.contains(toolIdString); 
				String siteRestriction = Objects.toString(tool.get(LTIService.LTI_SITE_ID), "");
				boolean allowedForSite = siteRestriction.isEmpty() || siteRestriction.equals(siteId);
				try
				{
					// in Oracle, the lti tool id is returned as BigDecimal, which cannot be casted into Integer directly
					Integer ltiId = Integer.valueOf(toolIdString);
					if (ltiId != null) {
						String ltiToolId = ltiId.toString(); 
						if (ltiToolId != null && ((!toolStealthed && allowedForSite) || ltiToolSelected) ) {
							String relativeWebPath = null;
							MyTool newTool = new MyTool();
							newTool.title = tool.get("title").toString();
							newTool.id = LTITOOL_ID_PREFIX + ltiToolId;
							newTool.description = (String) tool.get("description");
							newTool.group = groupName;
							relativeWebPath = getMoreInfoUrl(moreInfoDir, ltiToolId);
							if (relativeWebPath != null) {
								newTool.moreInfo = relativeWebPath;
							}
							// SAK16600 should this be a property or specified in  toolOrder.xml?
							newTool.required = false; 
							newTool.selected = ltiToolSelected;
							ltiTools.add(newTool);
						}
					}
				}
				catch (NumberFormatException e)
				{
					M_log.error(this + " Cannot cast tool id String " + toolIdString + " into integer value.");
				}
			}
		}
		return ltiTools;
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
		HashMap multipleToolConfiguration = new HashMap<String, HashMap<String, String>>();
		// get registered tools list
		Set categories = new HashSet();
		categories.add(type);
		categories.add(SiteTypeUtil.getTargetSiteType(type)); // UMICH-1035  
		Set toolRegistrations = ToolManager.findTools(categories, null);
		if ((toolRegistrations == null || toolRegistrations.size() == 0)
			&& state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null)
		{
			// use default site type and try getting tools again
			type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);
			categories.clear();
			categories.add(SiteTypeUtil.getTargetSiteType(type)); //UMICH-1035
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
			
			String originalToolId = findOriginalToolId(state, tr.getId());
			if (isMultipleInstancesAllowed(originalToolId))
			{
				// of a tool which allows multiple instances
				multipleToolIdSet.add(tr.getId());
				
				// get the configuration for multiple instance
				HashMap<String, String> toolConfigurations = getMultiToolConfiguration(originalToolId, null);
				multipleToolConfiguration.put(tr.getId(), toolConfigurations);
			}
			tools.add(newTool);
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

		actionForTemplate(direction, index, params, state, data);
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
		if ("continue".equals(option)) {
			doContinue(data);
		} else if ("cancel".equals(option)) {
			doCancel_create(data);
		} else if ("back".equals(option)) {
			doBack(data);
		} else if ("cancel".equals(option)) {
			doCancel_create(data);
		} else if ("norosters".equals(option)) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "13");
		}
		else if (option.equalsIgnoreCase("change_user")) {  // SAK-22915
			doChange_user(data);
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
		actionForTemplate(direction, currentIndex, params, state, data);
		
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

			addNewSite(params, state);

			Site site = getStateSite(state);
			
			// SAK-23468  Add new site params to state
			setNewSiteStateParameters(site, state);
			
			
			// Since the option to input aliases is presented to users prior to
			// the new site actually being created, it doesn't really make sense 
			// to check permissions on the newly created site when we assign 
			// aliases, hence the advisor here.
			//
			// Set site aliases before dealing with tools b/c site aliases
			// are more general and can, for example, serve the same purpose
			// as mail channel aliases but the reverse is not true.
			if ( aliasAssignmentForNewSitesEnabled(state) ) {
				SecurityService.pushAdvisor(new SecurityAdvisor()
				{
					public SecurityAdvice isAllowed(String userId, String function, String reference)
					{
						if ( AliasService.SECURE_ADD_ALIAS.equals(function) ||
								AliasService.SECURE_UPDATE_ALIAS.equals(function) ) {
							return SecurityAdvice.ALLOWED; 
						}
						return SecurityAdvice.PASS;
					}
				});
				try {
					setSiteReferenceAliases(state, site.getId()); // sets aliases for the site entity itself
				} finally {
					SecurityService.popAdvisor();
				}
			}

			SiteInfo siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			if (siteInfo != null) {
				for (String role : siteInfo.additionalRoles) {
					updateAdditionalRole(state, site, role, true);
				}
			}

			Site templateSite = (Site) state.getAttribute(STATE_TEMPLATE_SITE);
			if (templateSite == null) 
			{
				// normal site creation: add the features.
				saveFeatures(params, state, site);
				try {
				    site = SiteService.getSite(site.getId());
				} catch (Exception ee) {
				    M_log.error(this + "doFinish: unable to reload site " + site.getId() + " after copying tools");
				}
			}
			else
			{
				// creating based on template
				if (state.getAttribute(STATE_TEMPLATE_SITE_COPY_CONTENT) != null)
				{
					// create based on template: skip add features, and copying all the contents from the tools in template site
					importToolContent(templateSite.getId(), site, true);
					try {
					    site = SiteService.getSite(site.getId());
					} catch (Exception ee) {
					    M_log.error(this + "doFinish: unable to reload site " + site.getId() + " after importing tools");
					}
				}
				// copy members
				if(state.getAttribute(STATE_TEMPLATE_SITE_COPY_USERS) != null) 
				{
					try
					{
						AuthzGroup templateGroup = authzGroupService.getAuthzGroup(templateSite.getReference());
						AuthzGroup newGroup = authzGroupService.getAuthzGroup(site.getReference());
						
						for(Iterator mi = templateGroup.getMembers().iterator();mi.hasNext();) {
							Member member = (Member) mi.next();
							if (newGroup.getMember(member.getUserId()) == null)
							{
								// only add those user who is not in the new site yet
								newGroup.addMember(member.getUserId(), member.getRole().getId(), member.isActive(), member.isProvided());
							}
						}
						
						authzGroupService.save(newGroup);
					}
					catch (Exception copyUserException)
					{
						M_log.warn(this + "doFinish: copy user exception template site =" + templateSite.getReference() + " new site =" + site.getReference() + " " + copyUserException.getMessage());
					}
					
				}
				else
				{
					// if not bringing user over, remove the provider information
					try
					{
						AuthzGroup newGroup = authzGroupService.getAuthzGroup(site.getReference());
						newGroup.setProviderGroupId(null);
						authzGroupService.save(newGroup);
						
						// make sure current user stays in the site
						newGroup = authzGroupService.getAuthzGroup(site.getReference());
						String currentUserId = UserDirectoryService.getCurrentUser().getId();
						if (newGroup.getUserRole(currentUserId) == null)
						{
							// add advisor
							SecurityService.pushAdvisor(new SecurityAdvisor()
							{
								public SecurityAdvice isAllowed(String userId, String function, String reference)
								{
									return SecurityAdvice.ALLOWED;
								}
							});
							newGroup.addMember(currentUserId, newGroup.getMaintainRole(), true, false);
							authzGroupService.save(newGroup);
							
							// remove advisor
							SecurityService.popAdvisor();
						}
					}
					catch (Exception removeProviderException)
					{
						M_log.warn(this + "doFinish: remove provider id " + " new site =" + site.getReference() + " " + removeProviderException.getMessage());
					}
					
					try {
					    site = SiteService.getSite(site.getId());
					} catch (Exception ee) {
					    M_log.error(this + "doFinish: unable to reload site " + site.getId() + " after updating roster.");
					}
				}
				// We don't want the new site to automatically be a template
				site.getPropertiesEdit().removeProperty("template");
				
				// publish the site or not based on the template choice
				site.setPublished(state.getAttribute(STATE_TEMPLATE_PUBLISH) != null?true:false);
				
				// Update the icons URL.
				String newSiteIconUrl = transferSiteResource(templateSite.getId(), site.getId(), site.getIconUrl());
				site.setIconUrl(newSiteIconUrl);
				
				userNotificationProvider.notifyTemplateUse(templateSite, UserDirectoryService.getCurrentUser(), site);	
			}
				
			ResourcePropertiesEdit rp = site.getPropertiesEdit();

			// for course sites
			String siteType = site.getType();
			if (SiteTypeUtil.isCourseSite(siteType)) {
				AcademicSession term = null;
				if (state.getAttribute(STATE_TERM_SELECTED) != null) {
					term = (AcademicSession) state
							.getAttribute(STATE_TERM_SELECTED);
					rp.addProperty(Site.PROP_SITE_TERM, term.getTitle());
					rp.addProperty(Site.PROP_SITE_TERM_EID, term.getEid());
				}

				// update the site and related realm based on the rosters chosen or requested
				updateCourseSiteSections(state, site.getId(), rp, term);
			}
			else
			{
				// for non course type site, send notification email
				sendSiteNotification(state, getStateSite(state), null);
			}

			// commit site
			commitSite(site);
			
			//merge uploaded archive if required
			if(state.getAttribute(STATE_CREATE_FROM_ARCHIVE) == Boolean.TRUE) {
				doMergeArchiveIntoNewSite(site.getId(), state);
			}

			if (templateSite == null) 
			{	
				// save user answers
				saveSiteSetupQuestionUserAnswers(state, site.getId());
			}
			
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
	 * get one alias for site, if it exists
	 * @param reference
	 * @return
	 */
	private String getSiteAlias(String reference)
	{
		String alias = null;
		if (reference != null)
		{
			// get the email alias when an Email Archive tool has been selected
			List aliases = aliasService.getAliases(reference, 1, 1);
			if (aliases.size() > 0) {
				alias = ((Alias) aliases.get(0)).getId();
			}
		}
		return alias;
	}

 	/**
	* Processes site entity aliases associated with the {@link SiteInfo}
	* object currently cached in the session. Checked exceptions during
	* processing of any given alias results in an alert and a log message, 
	* but all aliases will be processed. This behavior is an attempt to be
	* consistent with established, heads-down style request processing 
	* behaviors, e.g. in {@link #doFinish(RunData)}.
	* 
	* <p>Processing should work for both site creation and modification.</p>
	* 
	* <p>Implements no permission checking of its own, so insufficient permissions
	* will result in an alert being cached in the current session. Thus it
	* is typically appropriate for the caller to check permissions first,
	* especially because insufficient permissions may result in a
	* misleading {@link SiteInfo) state. Specifically, the alias collection
	* is likely empty, which is consistent with handling of other read-only
	* fields in that object, but which would cause this method to attempt
	* to delete all aliases for the current site.</p>
	* 
	* <p>Exits quietly if no {@link SiteInfo} object has been cached under
	* the {@link #STATE_SITE_INFO} key.</p>
	* 
	* @param state
	* @param siteId
	*/
	private void setSiteReferenceAliases(SessionState state, String siteId) {
		SiteInfo siteInfo = (SiteInfo)state.getAttribute(STATE_SITE_INFO);
		if ( siteInfo == null ) {
			return;
		}
		String siteReference = SiteService.siteReference(siteId);
		List<String> existingAliasIds = toIdList(aliasService.getAliases(siteReference));
		Set<String> proposedAliasIds = siteInfo.siteRefAliases;
		Set<String> aliasIdsToDelete = new HashSet<String>(existingAliasIds);
		aliasIdsToDelete.removeAll(proposedAliasIds);
		Set<String> aliasIdsToAdd = new HashSet<String>(proposedAliasIds);
		aliasIdsToAdd.removeAll(existingAliasIds);
		for ( String aliasId : aliasIdsToDelete ) {
			try {
				aliasService.removeAlias(aliasId);
			} catch ( PermissionException e ) {
				addAlert(state, rb.getFormattedMessage("java.delalias", new Object[]{aliasId}));
				M_log.error(this + ".setSiteReferenceAliases: " + rb.getFormattedMessage("java.delalias", new Object[]{aliasId}), e);
			} catch ( IdUnusedException e ) {
				// no problem
			} catch ( InUseException e ) {
				addAlert(state, rb.getFormattedMessage("java.delalias", new Object[]{aliasId}) + rb.getFormattedMessage("java.alias.locked", new Object[]{aliasId}));
				M_log.error(this + ".setSiteReferenceAliases: " + rb.getFormattedMessage("java.delalias", new Object[]{aliasId}) + rb.getFormattedMessage("java.alias.locked", new Object[]{aliasId}), e);
	 			}
	 		}
		for ( String aliasId : aliasIdsToAdd ) {
			try {
				aliasService.setAlias(aliasId, siteReference);
			} catch ( PermissionException e ) {
				addAlert(state, rb.getString("java.addalias") + " ");
				M_log.error(this + ".setSiteReferenceAliases: " + rb.getString("java.addalias"), e);
			} catch ( IdInvalidException e ) {
				addAlert(state, rb.getFormattedMessage("java.alias.isinval", new Object[]{aliasId}));
				M_log.error(this + ".setSiteReferenceAliases: " + rb.getFormattedMessage("java.alias.isinval", new Object[]{aliasId}), e);
			} catch ( IdUsedException e ) {
				addAlert(state, rb.getFormattedMessage("java.alias.exists", new Object[]{aliasId}));
				M_log.error(this + ".setSiteReferenceAliases: " + rb.getFormattedMessage("java.alias.exists", new Object[]{aliasId}), e);
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
		
		List providerCourseList = state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) == null ? new ArrayList() : (List) state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
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
				AuthzGroup realmEdit = authzGroupService
						.getAuthzGroup(realm);
				String providerRealm = buildExternalRealm(siteId, state,
						providerCourseList, StringUtils.trimToNull(realmEdit.getProviderGroupId()));
				realmEdit.setProviderGroupId(providerRealm);
				authzGroupService.save(realmEdit);
			} catch (GroupNotDefinedException e) {
				M_log.error(this + ".updateCourseSiteSections: IdUnusedException, not found, or not an AuthzGroup object", e);
				addAlert(state, rb.getString("java.realm"));
			}
			catch (AuthzPermissionException e)
			{
				M_log.warn(this + rb.getString("java.notaccess"));
				addAlert(state, rb.getString("java.notaccess"));
			}

			sendSiteNotification(state, getStateSite(state), providerCourseList);
			//Track add changes
			trackRosterChanges(org.sakaiproject.site.api.SiteService.EVENT_SITE_ROSTER_ADD,providerCourseList);
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
		
		if (cmAuthorizerSections != null
				&& cmAuthorizerSections.size() > 0 || state.getAttribute(STATE_CM_SELECTED_SECTIONS) != null) {
			// set the cmAuthorizer sections to the site property
			
			String cmAuthorizerSectionString = "";
			
			if (!editingSite)
			{
				// but we want to feed a list of a list of String (input of
				// the required fields)
				for (int j = 0; j < cmAuthorizerSections.size(); j++) {
					cmAuthorizerSectionString = cmAuthorizerSectionString.concat(( cmAuthorizerSections.get(j)).eid).concat("+");
				}
	
				// trim the trailing plus sign
				cmAuthorizerSectionString = trimTrailingString(cmAuthorizerSectionString, "+");
				
				sendSiteRequest(state, "new", cmAuthorizerSections.size(), cmAuthorizerSections, "cmRequest");
			}
			else
			{
				cmAuthorizerSectionString = rp.getProperty(STATE_CM_AUTHORIZER_SECTIONS) != null ? (String) rp.getProperty(STATE_CM_AUTHORIZER_SECTIONS):"";
				
				// get the selected cm section
				if (state.getAttribute(STATE_CM_SELECTED_SECTIONS) != null )
				{
					List<SectionObject> cmSelectedSections = (List) state.getAttribute(STATE_CM_SELECTED_SECTIONS);
					if (cmAuthorizerSectionString.length() != 0)
					{
						cmAuthorizerSectionString = cmAuthorizerSectionString.concat("+");
					}
					for (int j = 0; j < cmSelectedSections.size(); j++) {
						cmAuthorizerSectionString = cmAuthorizerSectionString.concat(( cmSelectedSections.get(j)).eid).concat("+");
					}
		
					// trim the trailing plus sign
					cmAuthorizerSectionString = trimTrailingString(cmAuthorizerSectionString, "+");
					
					sendSiteRequest(state, "new", cmSelectedSections.size(), cmSelectedSections, "cmRequest");
				}
			}
			
			// update site property
			if (cmAuthorizerSectionString.length() > 0)
			{
				rp.addProperty(STATE_CM_AUTHORIZER_SECTIONS, cmAuthorizerSectionString);
			}
			else
			{
				rp.removeProperty(STATE_CM_AUTHORIZER_SECTIONS);
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
		if (!authzGroupService.allowUpdate(realm)) {
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
		boolean requireAuthorizer = ServerConfigurationService.getString(SAK_PROP_REQUIRE_AUTHORIZER, "true").equals("true")?true:false;
		String emailAuthorizer = ServerConfigurationService.getString(SAK_PROP_EMAIL_AUTHORIZER, "");

		// get the request email from configuration
		String requestEmail = getSetupRequestEmailAddress();
		
		// get the request replyTo email from configuration
		String requestReplyToEmail = getSetupRequestReplyToEmailAddress();
		
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
			String content = NULL_STRING;

			String sessionUserName = cUser.getDisplayName();
			String additional = NULL_STRING;
			if ("new".equals(request)) {
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

			// there is no offical instructor for future term sites
			String requestId = (String) state
					.getAttribute(STATE_SITE_QUEST_UNIQNAME);
			
			// SAK-18976:Site Requests Generated by entering instructor's User ID fail to add term properties and fail to send site request approval email

			List<String> authorizerList = (List) state
							.getAttribute(STATE_CM_AUTHORIZER_LIST);
			
			
			if (authorizerList == null) {
				authorizerList = new ArrayList();
			}
			if (requestId != null) {
				// in case of multiple instructors
				List instructors = new ArrayList(Arrays.asList(requestId.split(",")));
				
				for (Iterator iInstructors = instructors.iterator(); iInstructors.hasNext();)
				{
					String instructorId = (String) iInstructors.next();

					authorizerList.add(instructorId);
				}
			}

			String requestSectionInfo = "";
			// requested sections
			if ("manual".equals(fromContext))
			{
				requestSectionInfo = addRequestedSectionIntoNotification(state, requestFields);
			}
			else if ("cmRequest".equals(fromContext))
			{
				requestSectionInfo = addRequestedCMSectionIntoNotification(state, requestFields);
			}
 			
			String authorizerNotified = "";
			String authorizerNotNotified = "";
			if (!isFutureTerm) {
				for (Iterator iInstructors = authorizerList.iterator(); iInstructors.hasNext();)
				{
					String instructorId = (String) iInstructors.next();
					//If emailAuthorizer is defined to be true  or if requireAuthorizer is set
					
					//If emailAuthrozier is true always send email, if it's unset send if requireAuthrozier is set
					//Otherwise don't send
					if (("".equals(emailAuthorizer) && requireAuthorizer) || "true".equals(emailAuthorizer))
					{
						// 1. email to course site authorizer
						boolean result = userNotificationProvider.notifyCourseRequestAuthorizer(instructorId, requestEmail, requestReplyToEmail, term != null? term.getTitle():"", requestSectionInfo, title, id, additional, productionSiteName);
						if (!result)
						{
							// append authorizer who doesn't received an notification
							authorizerNotNotified += instructorId + ", ";
						}
						else
						{
							// append authorizer who does received an notification
							authorizerNotified += instructorId + ", ";
						}
						
					}
				}
			}

			// 2. email to system support team
			String supportEmailContent = userNotificationProvider.notifyCourseRequestSupport(requestEmail, productionSiteName, request, term != null?term.getTitle():"", requestListSize, requestSectionInfo,
						officialAccountName, title, id, additional, requireAuthorizer, authorizerNotified, authorizerNotNotified);
			
			// 3. email to site requeser
			userNotificationProvider.notifyCourseRequestRequester(requestEmail, supportEmailContent, term != null?term.getTitle():"");
			
			// revert the locale to system default			
			rb.setContextLocale(Locale.getDefault());
			state.setAttribute(REQUEST_SENT, Boolean.valueOf(true));

		} // if
		
		// reset locale to user default		
		rb.setContextLocale(null);

	} // sendSiteRequest

	private String addRequestedSectionIntoNotification(SessionState state, List requestFields) {
		StringBuffer buf = new StringBuffer();
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
			buf.append("\n");
		}
		return buf.toString();
	}
	
	private String addRequestedCMSectionIntoNotification(SessionState state, List cmRequestedSections) {
		StringBuffer buf = new StringBuffer();
		// what are the required fields shown in the UI
		for (int i = 0; i < cmRequestedSections.size(); i++) {
			SectionObject so = (SectionObject) cmRequestedSections.get(i);

			buf.append(so.getTitle() + "(" + so.getEid()
					+ ")" + so.getCategory() + "\n");
		}
		return buf.toString();
	}

	/**
	 * Notification sent when a course site is set up automatcally
	 * 
	 */
	private void sendSiteNotification(SessionState state, Site site, List notifySites) {
		boolean courseSite = SiteTypeUtil.isCourseSite(site.getType());
		
		String term_name = "";
		if (state.getAttribute(STATE_TERM_SELECTED) != null) {
			term_name = ((AcademicSession) state
					.getAttribute(STATE_TERM_SELECTED)).getEid();
		}
		// get the request email from configuration
		String requestEmail = getSetupRequestEmailAddress();
		User currentUser = UserDirectoryService.getCurrentUser();
		// read from configuration whether to send out site notification emails, which defaults to be true
		boolean sendSiteNotificationChoice = ServerConfigurationService.getBoolean("site.setup.creation.notification", true);
		if (requestEmail != null && currentUser != null && sendSiteNotificationChoice) {
			userNotificationProvider.notifySiteCreation(site, notifySites, courseSite, term_name, requestEmail);
		} // if

		// reset locale to user default
		
		rb.setContextLocale(null);
		
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
		
		// remove state variables in tool editing
		removeEditToolState(state);

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

		if ("4".equals(currentIndex)) {
			state.removeAttribute(STATE_TOOL_EMAIL_ADDRESS);
			state.removeAttribute(STATE_MESSAGE);
			removeEditToolState(state);
		} else if (getStateSite(state) != null && ("13".equals(currentIndex) || "14".equals(currentIndex)))
		{
			MathJaxEnabler.removeMathJaxAllowedAttributeFromState(state);  // SAK-22384
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		} else if ("15".equals(currentIndex)) {
			params = data.getParameters();
			state.setAttribute(STATE_TEMPLATE_INDEX, params
					.getString("cancelIndex"));
			removeEditToolState(state);
		}
		// htripath: added '"45".equals(currentIndex)' for import from file
		// cancel
		else if ("45".equals(currentIndex)) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		} else if ("4".equals(currentIndex)) {
			// from adding class
			if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITESETUP)) {
				state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			} else if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITEINFO)) {
				state.setAttribute(STATE_TEMPLATE_INDEX, "18");
			}
		} else if ("27".equals(currentIndex) || "28".equals(currentIndex) || "59".equals(currentIndex) || "60".equals(currentIndex)) {
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
		} else if ("26".equals(currentIndex)) {
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
		} else if ("37".equals(currentIndex) || "44".equals(currentIndex) || "53".equals(currentIndex) || "36".equals(currentIndex)) {
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
	 * toolId might be of form original tool id concatenated with number
	 * find whether there is an counterpart in the the multipleToolIdSet
	 * @param state
	 * @param toolId
	 * @return
	 */
	private String findOriginalToolId(SessionState state, String toolId) {
		// treat home tool differently
		if (toolId.equals(TOOL_ID_HOME) || SITE_INFO_TOOL_ID.equals(toolId))
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

	// replace fake tool ids with real ones. Don't duplicate, since several fake tool ids may appear for the same real one
	// it's far from clear that we need to be using fake tool ids at all. But I don't know the code well enough
	// to get rid of the fakes completely.
	// modified to exclude nulls as sakai.assignment is still in the list (as of Nov 2014) and is unresolvable
	private List<String> originalToolIds(List<String>toolIds, SessionState state) {    
		Set<String>found = new HashSet<String>();
		List<String>rv = new ArrayList<String>();

		for (String toolId: toolIds) {
		    String origToolId = findOriginalToolId(state, toolId);
		    if (StringUtils.isNotBlank(origToolId) && !found.contains(origToolId)) {
			    rv.add(origToolId);
			    found.add(origToolId);
		    }
		}
		return rv;
	}

	private String originalToolId(String toolId, String toolRegistrationId) {
		String rv = null;
		if (toolId.equals(toolRegistrationId))
		{
			rv = toolRegistrationId;
		}
		else if (toolId.indexOf(toolRegistrationId) != -1 && isMultipleInstancesAllowed(toolRegistrationId))
		{
			// the multiple tool id format is of SITE_IDTOOL_IDx, where x is an intger >= 1
			if (toolId.endsWith(toolRegistrationId))
			{
				// get the site id part out
				String uuid = toolId.replaceFirst(toolRegistrationId, "");
				if (uuid != null && uuid.length() == UUID_LENGTH)
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
					M_log.debug(this + ".findOriginalToolId not matching tool id = " + toolRegistrationId + " original tool id=" + toolId + e.getMessage(), e);
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
	
	private HashMap<String, String> getMultiToolConfiguration(String toolId, ToolConfiguration toolConfig)
	{
		HashMap<String, String> rv = new HashMap<String, String>();
	
		// read attribute list from configuration file
		ArrayList<String> attributes=new ArrayList<String>();
		String attributesConfig = ServerConfigurationService.getString(CONFIG_TOOL_ATTRIBUTE + toolId);
		if ( attributesConfig != null && attributesConfig.length() > 0)
		{
			// read attributes from config file
			attributes = new ArrayList(Arrays.asList(attributesConfig.split(",")));
		}
		else
		{
			if (toolId.equals(NEWS_TOOL_ID))
			{
				// default setting for News tool
				attributes.add(NEWS_TOOL_CHANNEL_CONFIG);
			}
			else if (toolId.equals(WEB_CONTENT_TOOL_ID))
			{
				// default setting for Web Content tool
				attributes.add(WEB_CONTENT_TOOL_SOURCE_CONFIG);
			}
		}
		
		// read the defaul attribute setting from configuration
		ArrayList<String> defaultValues =new ArrayList<String>();
		String defaultValueConfig = ServerConfigurationService.getString(CONFIG_TOOL_ATTRIBUTE_DEFAULT + toolId);
		if ( defaultValueConfig != null && defaultValueConfig.length() > 0)
		{
			defaultValues = new ArrayList(Arrays.asList(defaultValueConfig.split(",")));
		}
		else
		{
			// otherwise, treat News tool and Web Content tool differently
			if (toolId.equals(NEWS_TOOL_ID))
			{
				// default value
				defaultValues.add(NEWS_TOOL_CHANNEL_CONFIG_VALUE);
			}
			else if (toolId.equals(WEB_CONTENT_TOOL_ID))
			{
				// default value
				defaultValues.add(WEB_CONTENT_TOOL_SOURCE_CONFIG_VALUE);
			}
		}
			
		if (attributes != null && attributes.size() > 0 && defaultValues != null && defaultValues.size() > 0 && attributes.size() == defaultValues.size())
		{
			for (int i = 0; i < attributes.size(); i++)
			{
				String attribute = attributes.get(i);
				
				// check to see the current settings first
				Properties config = toolConfig != null ? toolConfig.getConfig() : null;
				if (config != null && config.containsKey(attribute))
				{
					rv.put(attribute, config.getProperty(attribute));
				}
				else
				{
					// set according to the default setting
					rv.put(attribute, defaultValues.get(i));
				}
			}
		}
		
		
		return rv;
	}
	
	/**
	 * The triage function for saving modified features page
	 * @param data
	 */
	public void doAddRemoveFeatureConfirm_option(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		
		String option = params.getString("option");
		if ("revise".equals(option))
		{
			// save the modified features
			doSave_revised_features(state, params);
		}
		else if ("back".equals(option))
		{
			// back a step
			doBack(data);
		}
		else if ("cancel".equals(option))
		{
			// cancel out
			doCancel(data);
		}
	
	}
	
	/**
	 * doSave_revised_features
	 */
	public void doSave_revised_features(SessionState state, ParameterParser params) {
		
		Site site = getStateSite(state);
		
		saveFeatures(params, state, site);
		
		if (state.getAttribute(STATE_MESSAGE) == null) {
			// clean state variables
			state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
			state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME);
			state.removeAttribute(STATE_MULTIPLE_TOOL_ID_SET);
			state.removeAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP);

			state.setAttribute(STATE_SITE_INSTANCE_ID, site.getId());

			state.setAttribute(STATE_TEMPLATE_INDEX, params
					.getString("continue"));
			resetVisitedTemplateListToIndex(state, (String) state.getAttribute(STATE_TEMPLATE_INDEX));

			// refresh the whole page
			scheduleTopRefresh();
		}

	} // doSave_revised_features

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
	
	/**
	 * doMenu_siteInfo_import_user
	 */
	public void doMenu_siteInfo_import_user(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "61");	// import users
		}

	} // doMenu_siteInfo_import_user
	
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
		String termEid = site.getProperties().getProperty(Site.PROP_SITE_TERM_EID);
		if (termEid == null)
		{
			// no term eid stored, need to get term eid from the term title
			String termTitle = site.getProperties().getProperty(Site.PROP_SITE_TERM);
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
						site.getPropertiesEdit().addProperty(Site.PROP_SITE_TERM_EID, termEid);
						
						try
						{
							SiteService.save(site);
						}
						catch (Exception e)
						{
							M_log.error(this + ".doMenu_siteinfo_addClass: " + e.getMessage() + site.getId(), e);
						}
						found=true;
					}
				}
			}
		}
		
		if (termEid != null)
		{
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
				M_log.error(this + ".doMenu_siteinfo_addClass: " + e.getMessage() + termEid, e);
			}
		}
		else
		{
			List currentTerms = cms.getCurrentAcademicSessions();
			if (currentTerms != null && !currentTerms.isEmpty())
			{
				// if the term information is missing for the site, assign it to the first current term in list
				state.setAttribute(STATE_TERM_SELECTED, currentTerms.get(0));
			}
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

		sitePropertiesIntoState(state);

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

		// Clean up state on our first entry from a shortcut
		String panel = data.getParameters().getString("panel");
		if ( "Shortcut".equals(panel) ) cleanState(state);

		// get the tools
		siteToolsIntoState(state);

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "4");
			if (state.getAttribute(STATE_INITIALIZED) == null) {
				state.setAttribute(STATE_OVERRIDE_TEMPLATE_INDEX, "4");
			}
		}

	} // doMenu_edit_site_tools

	/**
	 * doMenu_edit_site_access
	 * 
	 */
	public void doMenu_edit_site_access(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		try {
			Site site = getStateSite(state);
			state.setAttribute(STATE_SITE_ACCESS_PUBLISH, Boolean.valueOf(site.isPublished()));
			state.setAttribute(STATE_SITE_ACCESS_INCLUDE, Boolean.valueOf(site.isPubView()));
			boolean joinable = site.isJoinable();
			state.setAttribute(STATE_JOINABLE, Boolean.valueOf(joinable));
			String joinerRole = site.getJoinerRole();
			if (joinerRole == null || joinerRole.length() == 0)
			{
				String[] joinerRoles = ServerConfigurationService.getStrings("siteinfo.default_joiner_role");
				Set<Role> roles = site.getRoles();
				if (roles != null && joinerRole != null && joinerRoles.length > 0)
				{
					// find the role match
					for (Role r : roles)
					{
						for(int i = 0; i < joinerRoles.length; i++)
						{
							if (r.getId().equalsIgnoreCase(joinerRoles[i]))
							{
								joinerRole = r.getId();
								break;
							}
						}
						if (joinerRole != null)
						{
							break;
						}
					}
				}
			}
			state.setAttribute(STATE_JOINERROLE, joinerRole); 

			// bjones86 - SAK-24423 - update state for joinable site settings
			JoinableSiteSettings.updateStateFromSitePropertiesOnEditAccessOrNewSite( site.getProperties(), state );
		}
		catch (Exception e)
		{
			M_log.warn(this + " doMenu_edit_site_access problem of getting site" + e.getMessage());
		}
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

		if (siteTitleEditable(state, SiteTypeUtil.getTargetSiteType(site_type))) 
		{
			Site.setTitle(siteInfo.title);
		}

		//Process description so it doesn't give an error on home
		siteInfo.description = FormattedText.processFormattedText(siteInfo.description, new StringBuilder());
		
		Site.setDescription(siteInfo.description);
		Site.setShortDescription(siteInfo.short_description);

		if (site_type != null) {
			// set icon url for course
			setAppearance(state, Site, siteInfo.iconUrl);
		}

		// site contact information
		String contactName = siteInfo.site_contact_name;
		if (contactName != null) {
			siteProperties.addProperty(Site.PROP_SITE_CONTACT_NAME, contactName);
		}

		String contactEmail = siteInfo.site_contact_email;
		if (contactEmail != null) {
			siteProperties.addProperty(Site.PROP_SITE_CONTACT_EMAIL, contactEmail);
		}
		
		Collection<String> oldAliasIds = getSiteReferenceAliasIds(Site);
		boolean updateSiteRefAliases = aliasesEditable(state, Site.getId()); 
		if ( updateSiteRefAliases ) {
			setSiteReferenceAliases(state, Site.getId());
		}
	
		/// site language information
				
		String locale_string = (String) state.getAttribute("locale_string");							
				
		siteProperties.removeProperty(PROP_SITE_LANGUAGE);		
		siteProperties.addProperty(PROP_SITE_LANGUAGE, locale_string);

		// SAK-22384 mathjax support
		MathJaxEnabler.prepareMathJaxAllowedSettingsForSave(Site, state);
				
		if (state.getAttribute(STATE_MESSAGE) == null) {
			try {
				SiteService.save(Site);
			} catch (IdUnusedException e) {
				// TODO:
			} catch (PermissionException e) {
				// TODO:
			}

			// back to site info view
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");

			// Need to refresh the entire page because, e.g. the current site's name
			// may have changed. This is problematic, though, b/c the current 
			// top-level portal URL may reference a just-deleted alias. A temporary
			// alias translation map is one option, but it is difficult to know when 
			// to clean up. So we send a redirect instead of just scheduling 
			// a reload.
			//
			// One problem with this is we have no good way to know what the 
			// top-level portal handler should actually be. We also don't have a 
			// particularly good way of knowing how the portal expects to receive 
			// page references. We can't just use SitePage.getUrl() because that 
			// method assumes that site reference roots are identical to portal 
			// handler URL fragments, which is not guaranteed. Hence the approach
			// below which tries to guess at the right portal handler, but just
			// punts on page reference roots, using SiteService.PAGE_SUBTYPE for
			// that portion of the URL
			//
			// None of this helps other users who may try to reload an aliased site 
			// URL that no longer resolves.
			if ( updateSiteRefAliases ) {
				sendParentRedirect((HttpServletResponse) ThreadLocalManager.get(RequestFilter.CURRENT_HTTP_RESPONSE),
					getDefaultSiteUrl(ToolManager.getCurrentPlacement().getContext()) + "/" +
					SiteService.PAGE_SUBTYPE + "/" + 
					((ToolConfiguration) ToolManager.getCurrentPlacement()).getPageId());
			} else {
				scheduleTopRefresh();
			}

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
				&& ((state.getAttribute(TITLE_NOT_EDITABLE_SITE_TYPE) != null 
					&& !((List) state.getAttribute(TITLE_NOT_EDITABLE_SITE_TYPE)).contains(site_type)));
	}
	
	/**
	 * Tests if the alias editing feature has been enabled 
	 * ({@link #aliasEditingEnabled(SessionState, String)}) and that 
	 * current user has set/remove aliasing permissions for the given
	 * {@link Site} ({@link #aliasEditingPermissioned(SessionState, String)}).
	 * 
	 * <p>(Method name and signature is an attempt to be consistent with
	 * {@link #siteTitleEditable(SessionState, String)}).</p>
	 * 
	 * @param state not used
	 * @param siteId a site identifier (not a {@link Reference}); must not be <code>null</code>
	 * @return
	 */
	private boolean aliasesEditable(SessionState state, String siteId) {
		return aliasEditingEnabled(state, siteId) && 
			aliasEditingPermissioned(state, siteId);
	}
	
	/**
	 * Tests if alias editing has been enabled by configuration. This is 
	 * independent of any permissioning considerations. Also note that this 
	 * feature is configured separately from alias assignment during worksite 
	 * creation. This feature applies exclusively to alias edits and deletes 
	 * against existing sites.
	 * 
	 * <p>(Method name and signature is an attempt to be consistent with 
	 * {@link #siteTitleEditable(SessionState, String)}).</p>
	 * 
	 * @see #aliasAssignmentForNewSitesEnabled(SessionState)
	 * @param state
	 * @param siteId
	 * @return
	 */
	private boolean aliasEditingEnabled(SessionState state, String siteId) {
		return ServerConfigurationService.getBoolean("site-manage.enable.alias.edit", false);
	}
	
	/**
	 * Tests if alias assignment for new sites has been enabled by configuration. 
	 * This is independent of any permissioning considerations. 
	 * 
	 * <p>(Method name and signature is an attempt to be consistent with 
	 * {@link #siteTitleEditable(SessionState, String)}).</p>
	 * 
	 * @param state
	 * @param siteId
	 * @return
	 */
	private boolean aliasAssignmentForNewSitesEnabled(SessionState state) {
		return ServerConfigurationService.getBoolean("site-manage.enable.alias.new", false);
	}
	
	/**
	 * Tests if the current user has set and remove permissions for aliases
	 * of the given site. <p>(Method name and signature is an attempt to be 
	 * consistent with {@link #siteTitleEditable(SessionState, String)}).</p>
	 * 
	 * @param state
	 * @param siteId
	 * @return
	 */
	private boolean aliasEditingPermissioned(SessionState state, String siteId) {
		String siteRef = SiteService.siteReference(siteId);
		return aliasService.allowSetAlias("", siteRef) &&
			aliasService.allowRemoveTargetAliases(siteRef);
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
			
			// need to watch out for the config question.xml existence.
			// read the file and put it to backup folder.
			if (SiteSetupQuestionFileParser.isConfigurationXmlAvailable())
			{
				SiteSetupQuestionFileParser.updateConfig();
			}
		} else if (SITE_MODE_HELPER.equalsIgnoreCase((String) state.getAttribute(STATE_SITE_MODE))) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "1");
		} else if (SITE_MODE_SITEINFO.equalsIgnoreCase((String) state.getAttribute(STATE_SITE_MODE))){

			String siteId = ToolManager.getCurrentPlacement().getContext();
			getReviseSite(state, siteId);
			Hashtable h = (Hashtable) state
					.getAttribute(STATE_PAGESIZE_SITEINFO);
			if (!h.containsKey(siteId)) {
				// update
				h.put(siteId, Integer.valueOf(200));
				state.setAttribute(STATE_PAGESIZE_SITEINFO, h);
				state.setAttribute(STATE_PAGESIZE, Integer.valueOf(200));
			}
		}
		if (state.getAttribute(STATE_SITE_TYPES) == null) {
			PortletConfig config = portlet.getPortletConfig();

			// all site types (SITE_DEFAULT_LIST overrides tool config)
			String t = StringUtils.trimToNull(SITE_DEFAULT_LIST);
			if ( t == null )
				t = StringUtils.trimToNull(config.getInitParameter("siteTypes"));
			if (t != null) {
				List types = new ArrayList(Arrays.asList(t.split(",")));
				if (cms == null)
				{
					// if there is no CourseManagementService, disable the process of creating course site
					List<String> courseTypes = SiteTypeUtil.getCourseSiteTypes();
					types.remove(courseTypes);
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
			int siteTitleMaxLength = ServerConfigurationService.getInt("site.title.maxlength", 25);
			state.setAttribute(STATE_SITE_TITLE_MAX, siteTitleMaxLength);
		}

	} // init

	public void doNavigate_to_site(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String siteId = StringUtils.trimToNull(data.getParameters().getString(
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
			M_log.error(this + ".getReviseSite: " +  e.toString() + " site id = " + siteId, e);
		}

		// one site has been selected
		state.setAttribute(STATE_TEMPLATE_INDEX, "12");

	} // getReviseSite

	/**
	 * when user clicks "join" for a joinable set
	 * @param data
	 */
	public void doJoinableSet(RunData data){
		ParameterParser params = data.getParameters();
		String groupRef = params.getString("joinable-group-ref");
		
		Site currentSite;
		try {
			currentSite = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			if(currentSite != null){
				Group siteGroup = currentSite.getGroup(groupRef);
				//make sure its a joinable set:
				String joinableSet = siteGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
				if(joinableSet != null && !"".equals(joinableSet.trim())){
					//check that the max limit hasn't been reached:
					int max = 0;
					try{
						max = Integer.parseInt(siteGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET_MAX));
						AuthzGroup group = authzGroupService.getAuthzGroup(groupRef);
						int size = group.getMembers().size();
						if(size < max){
							//check that the user isn't already a member
							String userId = UserDirectoryService.getCurrentUser().getId();
							boolean found =  false;
							for(Member member : group.getMembers()){
								if(member.getUserId().equals(userId)){
									found = true;
									break;
								}
							}
							if(!found){
								// add current user as the maintainer
								Member member = currentSite.getMember(userId);
								if(member != null){
									siteGroup.addMember(userId, member.getRole().getId(), true, false);
									SecurityAdvisor yesMan = new SecurityAdvisor() {
										public SecurityAdvice isAllowed(String userId, String function, String reference) {
											return SecurityAdvice.ALLOWED;
										}
									};
									try{
										SecurityService.pushAdvisor(yesMan);
										commitSite(currentSite);
									}catch (Exception e) {
										M_log.debug(e);
									}finally{
										SecurityService.popAdvisor();
									}
								}
							}
						}	
					}catch (Exception e) {
						M_log.debug("Error adding user to group: " + groupRef + ", " + e.getMessage(), e);
					}
				}
			}
		} catch (IdUnusedException e) {
			M_log.debug("Error adding user to group: " + groupRef + ", " + e.getMessage(), e);
		}
	}
	
	/**
	 * when user clicks "join" for a joinable set
	 * @param data
	 */
	public void doUnjoinableSet(RunData data){
		ParameterParser params = data.getParameters();
		String groupRef = params.getString("group-ref");
		
		Site currentSite;
		try {
			currentSite = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			if(currentSite != null){
				Group siteGroup = currentSite.getGroup(groupRef);
				//make sure its a joinable set:
				String joinableSet = siteGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
				if(joinableSet != null && !"".equals(joinableSet.trim())){
					try{
						AuthzGroup group = authzGroupService.getAuthzGroup(groupRef);
						//check that the user is already a member
						String userId = UserDirectoryService.getCurrentUser().getId();
						boolean found =  false;
						for(Member member : group.getMembers()){
							if(member.getUserId().equals(userId)){
								found = true;
								break;
							}
						}
						if(found){
							// remove current user as the maintainer
							Member member = currentSite.getMember(userId);
							if(member != null){
								siteGroup.removeMember(userId);
								SecurityAdvisor yesMan = new SecurityAdvisor() {
									public SecurityAdvice isAllowed(String userId, String function, String reference) {
										return SecurityAdvice.ALLOWED;
									}
								};
								try{
									SecurityService.pushAdvisor(yesMan);
									commitSite(currentSite);
								}catch (Exception e) {
									M_log.debug(e);
								}finally{
									SecurityService.popAdvisor();
								}
							}
						}
					}catch (Exception e) {
						M_log.debug("Error removing user to group: " + groupRef + ", " + e.getMessage(), e);
					}
				}
			}
		} catch (IdUnusedException e) {
			M_log.debug("Error removing user to group: " + groupRef + ", " + e.getMessage(), e);
		}
	}
	

	/**
 	* SAK 23029 -  iterate through changed partiants to see how many would have maintain role if all roles, status and deletion changes went through
 	*
 	*/ 
	private List<Participant> testProposedUpdates(List<Participant> participants, ParameterParser params, String maintainRole) {
		List<Participant> maintainersAfterUpdates = new ArrayList<Participant>();
		// create list of all partcipants that have been 'Charles Bronson-ed'
		Set<String> removedParticipantIds = new HashSet();
		Set<String> deactivatedParticipants = new HashSet();
		if (params.getStrings("selectedUser") != null) {
			List removals = new ArrayList(Arrays.asList(params.getStrings("selectedUser")));
			for (int i = 0; i < removals.size(); i++) {
				String rId = (String) removals.get(i);
				removedParticipantIds.add(rId);
			}
		}

		// create list of all participants that have been deactivated
		for(Participant statusParticipant : participants ) {
			String activeGrantId = statusParticipant.getUniqname();
			String activeGrantField = "activeGrant" + activeGrantId;
		
			if (params.getString(activeGrantField) != null) { 
				boolean activeStatus = params.getString(activeGrantField).equalsIgnoreCase("true") ? true : false;
				if (activeStatus == false) {
					deactivatedParticipants.add(activeGrantId);
				}
			}
		}


		// now add only those partcipants whose new/current role is maintainer, is (still) active, and not marked for deletion
		for(Participant roleParticipant : participants ) {
			String id = roleParticipant.getUniqname();
			String roleId = "role" + id;
			String newRole = params.getString(roleId);
			if ((deactivatedParticipants.contains(id)==false) && roleParticipant.isActive() != false) { // skip any that are not already inactive or are not  candidates for deactivation
				 if (removedParticipantIds.contains(id) == false) {
					if (newRole != null){
						if (newRole.equals(maintainRole)) {
							maintainersAfterUpdates.add(roleParticipant);
						}
					} else { 
						// participant has no new role; was participant already maintainer?
						if (roleParticipant.getRole().equals(maintainRole)) {
							maintainersAfterUpdates.add(roleParticipant);
						}
					}
				}
			}	
		}
		return maintainersAfterUpdates;
	}
	
	/**
	 * doUpdate_participant
	 * 
	 */
	public void doUpdate_participant(RunData data) {
		if (!"POST".equals(data.getRequest().getMethod())) {
			M_log.warn("Ignoring non-POST request to update site access.");
			return;
		}
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		Site s = getStateSite(state);
		String realmId = SiteService.siteReference(s.getId());
		
		// list of updated users
		List<String> userUpdated = new Vector<String>();
		// list of all removed user
		List<String> usersDeleted = new Vector<String>();
	
		if (authzGroupService.allowUpdate(realmId)
						|| SiteService.allowUpdateSiteMembership(s.getId())) {
			try {
				// init variables useful for actual edits and mainainersAfterProposedChanges check
				AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realmId);
				String maintainRoleString = realmEdit.getMaintainRole();
				List participants = collectionToList((Collection) state.getAttribute(STATE_PARTICIPANT_LIST));

				// SAK 23029 Test proposed removals/updates; reject all where activeMainainer count would = 0 if all proposed changes were made
				List<Participant> maintainersAfterProposedChanges = testProposedUpdates(participants, params, maintainRoleString);

				if (maintainersAfterProposedChanges.size() == 0) {
					addAlert(state, 
						rb.getFormattedMessage("sitegen.siteinfolist.lastmaintainuseractive", new Object[]{maintainRoleString} ));
					return;
				}

				// SAK23029 - proposed changes do not leave site w/o maintainers; proceed with any allowed updates
			
				// list of roles being added or removed
				HashSet<String>roles = new HashSet<String>();

				// List used for user auditing
				List<String[]> userAuditList = new ArrayList<String[]>();

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
						String oldRoleId = participant.getRole();
						boolean roleChange = roleId != null && !roleId.equals(oldRoleId);
						
						// get the grant active status
						boolean activeGrant = true;
						String activeGrantField = "activeGrant" + id;
						if (params.getString(activeGrantField) != null) {
							activeGrant = params
									.getString(activeGrantField)
									.equalsIgnoreCase("true") ? true
									: false;
						}
						boolean activeGrantChange = roleId != null && (participant.isActive() && !activeGrant || !participant.isActive() && activeGrant);
						
						// save any roles changed for permission check
						if (roleChange) {
						    roles.add(roleId);
						    roles.add(oldRoleId);
						}
						
						// SAK-23257 - display an error message if the new role is in the restricted role list
						String siteType = s.getType();
						List<Role> allowedRoles = SiteParticipantHelper.getAllowedRoles( siteType, getRoles( state ) );
						for( String roleName : roles )
						{
							Role r = realmEdit.getRole( roleName );
							if( !allowedRoles.contains( r ) )
							{
								addAlert( state, rb.getFormattedMessage( "java.roleperm", new Object[] { roleName } ) );
							    return;
							}
						}
						
						if (roleChange || activeGrantChange)
						{
								boolean fromProvider = !participant.isRemoveable();
								if (fromProvider && !roleId.equals(participant.getRole())) {
									    fromProvider = false;
								}
								realmEdit.addMember(id, roleId, activeGrant,
									fromProvider);
							String currentUserId = (String) state.getAttribute(STATE_CM_CURRENT_USERID);
							String[] userAuditString = {s.getId(),participant.getEid(),roleId,userAuditService.USER_AUDIT_ACTION_UPDATE,userAuditRegistration.getDatabaseSourceKey(),currentUserId};
							userAuditList.add(userAuditString);
							
								// construct the event string
								String userUpdatedString = "uid=" + id;
								if (roleChange)
								{
									userUpdatedString += ";oldRole=" + oldRoleId + ";newRole=" + roleId;
								}
								else
								{
									userUpdatedString += ";role=" + roleId;
								}
								if (activeGrantChange)
								{
									userUpdatedString += ";oldActive=" + participant.isActive() + ";newActive=" + activeGrant;
								}
								else
								{
									userUpdatedString += ";active=" + activeGrant;
								}
								userUpdatedString += ";provided=" + fromProvider;
								
								// add to the list for all participants that have role changes
								userUpdated.add(userUpdatedString);

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
							 // save role for permission check
							if (user != null) {
								String userId = user.getId();
								Member userMember = realmEdit
										.getMember(userId);
								if (userMember != null) {
									Role role = userMember.getRole();
									if (role != null) {
										roles.add(role.getId());
									}
									realmEdit.removeMember(userId);
									usersDeleted.add("uid=" + userId);
									String currentUserId = (String) state.getAttribute(STATE_CM_CURRENT_USERID);
									String[] userAuditString = {s.getId(),user.getEid(),role.getId(),userAuditService.USER_AUDIT_ACTION_REMOVE,userAuditRegistration.getDatabaseSourceKey(),currentUserId};
									userAuditList.add(userAuditString);
								}
							}
						} catch (UserNotDefinedException e) {
							M_log.error(this + ".doUpdate_participant: IdUnusedException " + rId + ". ", e);
							if (("admins".equals(showOrphanedMembers) && SecurityService.isSuperUser()) || ("maintainers".equals(showOrphanedMembers))) {
								Member userMember = realmEdit.getMember(rId);
								if (userMember != null) {
									Role role = userMember.getRole();
									if (role != null) {
										roles.add(role.getId());
									}
									realmEdit.removeMember(rId);
								}
							}
						}
					}
				}

				// if user doesn't have update, don't let them add or remove any role with site.upd in it.

				if (!authzGroupService.allowUpdate(realmId)) {
					// see if any changed have site.upd
					for (String rolename: roles) {
						Role role = realmEdit.getRole(rolename);
						if (role != null && role.isAllowed("site.upd")) {
							addAlert(state, rb.getFormattedMessage("java.roleperm", new Object[]{rolename}));
							return;
							}
						}
				}
				authzGroupService.save(realmEdit);
				
				// do the audit logging - Doing this in one bulk call to the database will cause the actual audit stamp to be off by maybe 1 second at the most
				// but seems to be a better solution than call this multiple time for every update
				if (!userAuditList.isEmpty())
				{
					userAuditRegistration.addToUserAuditing(userAuditList);
				}

				// then update all related group realms for the role
				doUpdate_related_group_participants(s, realmId);

				// post event about the participant update
				EventTrackingService.post(EventTrackingService.newEvent(
						SiteService.SECURE_UPDATE_SITE_MEMBERSHIP,
						realmEdit.getId(), false));

				// check the configuration setting, whether logging membership
				// change at individual level is allowed
				if (ServerConfigurationService.getBoolean(
						SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, false)) {
					// event for each individual update
					for (String userChangedRole : userUpdated) {
						EventTrackingService
								.post(EventTrackingService
										.newEvent(
												org.sakaiproject.site.api.SiteService.EVENT_USER_SITE_MEMBERSHIP_UPDATE,
												userChangedRole, true));
					}
					// event for each individual remove
					for (String userDeleted : usersDeleted) {
						EventTrackingService
								.post(EventTrackingService
										.newEvent(
												org.sakaiproject.site.api.SiteService.EVENT_USER_SITE_MEMBERSHIP_REMOVE,
												userDeleted, true));
					}
				}
			} catch (GroupNotDefinedException e) {
				addAlert(state, rb.getString("java.problem2"));
				M_log.error(this + ".doUpdate_participant: IdUnusedException " + s.getTitle() + "(" + realmId + "). ", e);
			} catch (AuthzPermissionException e) {
				addAlert(state, rb.getString("java.changeroles"));
				M_log.error(this + ".doUpdate_participant: PermissionException " + s.getTitle() + "(" + realmId + "). ", e);
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
		boolean trackIndividualChange = ServerConfigurationService.getBoolean(SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, false);
		if (groups != null)
		{
			try
			{
				for (Iterator iGroups = groups.iterator(); iGroups.hasNext();)
				{
					Group g = (Group) iGroups.next();
					if (g != null)
					{
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
									String gProp = g.getProperties().getProperty(g.GROUP_PROP_WSETUP_CREATED);
									
									// if there is a difference between the role setting, remove the entry from group and add it back with correct role, all are marked "not provided"
									Role groupRole = g.getUserRole(gMemberId);
									Role siteRole = siteMember.getRole();
									if (gProp != null && gProp.equals(Boolean.TRUE.toString()) &&
											groupRole != null && siteRole != null && !groupRole.equals(siteRole))
									{
										if (g.getRole(siteRole.getId()) == null)
										{
											// in case there is no matching role as that in the site, create such role and add it to the user
											g.addRole(siteRole.getId(), siteRole);
										}
										g.removeMember(gMemberId);
										g.addMember(gMemberId, siteRole.getId(), siteMember.isActive(), false);
										// track the group membership change at individual level
										if (trackIndividualChange)
										{
											// an event for each individual member role change
											EventTrackingService.post(EventTrackingService.newEvent(org.sakaiproject.site.api.SiteService.EVENT_USER_GROUP_MEMBERSHIP_UPDATE, "uid=" + gMemberId + ";groupId=" + g.getId() + ";oldRole=" + groupRole + ";newRole=" + siteRole + ";active=" + siteMember.isActive() + ";provided=false", true/*update event*/));
										}
									}
								}
							}
							// post event about the participant update
							EventTrackingService.post(EventTrackingService.newEvent(SiteService.SECURE_UPDATE_GROUP_MEMBERSHIP, g.getId(),true));
						}
						catch (Exception ee)
						{
							M_log.error(this + ".doUpdate_related_group_participants: " + ee.getMessage() + g.getId(), ee);
						}
					}
					
				}
				// commit, save the site
				SiteService.save(s);
			}
			catch (Exception e)
			{
				M_log.error(this + ".doUpdate_related_group_participants: " + e.getMessage() + s.getId(), e);
			}
		}
	}

	/**
	 * doUpdate_site_access
	 * 
	 */
	public void doUpdate_site_access(RunData data) {
		if (!"POST".equals(data.getRequest().getMethod())) {
			M_log.warn("Ignoring non-POST request to update site access.");
			return;
		}
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		Site sEdit = getStateSite(state);

		ParameterParser params = data.getParameters();
		
		// get all form inputs
		readInputAndUpdateStateVariable(state, params, "publishunpublish", STATE_SITE_ACCESS_PUBLISH, true);
		readInputAndUpdateStateVariable(state, params, "include", STATE_SITE_ACCESS_INCLUDE, true);
		readInputAndUpdateStateVariable(state, params, "joinable", STATE_JOINABLE, true);
		readInputAndUpdateStateVariable(state, params, "joinerRole", STATE_JOINERROLE, false);
		
		// bjones86 - SAK-24423 - get all joinable site settings from the form input
		JoinableSiteSettings.getAllFormInputs( state, params );
		
		boolean publishUnpublish = state.getAttribute(STATE_SITE_ACCESS_PUBLISH) != null ? ((Boolean) state.getAttribute(STATE_SITE_ACCESS_PUBLISH)).booleanValue() : false;
		
		// the site publish status before update
		boolean currentSitePublished = sEdit != null ? sEdit.isPublished():false;
		
		boolean include = state.getAttribute(STATE_SITE_ACCESS_INCLUDE) != null ? ((Boolean) state.getAttribute(STATE_SITE_ACCESS_INCLUDE)).booleanValue() : false;

		if (sEdit != null) {
			// editing existing site
			// publish site or not
			sEdit.setPublished(publishUnpublish);

			// site public choice
			List publicChangeableSiteTypes = (List) state.getAttribute(STATE_PUBLIC_CHANGEABLE_SITE_TYPES);
			if (publicChangeableSiteTypes != null && sEdit.getType() != null && !publicChangeableSiteTypes.contains(sEdit.getType()))
			{
				// set pubview to true for those site types which pubview change is not allowed
				sEdit.setPubView(true);
			}
			else
			{
				// set pubview according to UI selection
				sEdit.setPubView(include);
			}

			doUpdate_site_access_joinable(data, state, params, sEdit);
			
			Map<String, AdditionalRole> additionalRoles = getAdditionalAccess(sEdit);
			for(String role : additionalRoles.keySet()) {
				boolean userChoice = params.getBoolean("role"+role);
				AdditionalRole additionalRole = additionalRoles.get(role);
				if (additionalRole.editable) {
					if (additionalRole.granted != userChoice) {
						updateAdditionalRole(state, sEdit, role, userChoice);
					}
				}
			}

			if (state.getAttribute(STATE_MESSAGE) == null) {
				commitSite(sEdit);
				if (currentSitePublished && !publishUnpublish)
				{
					// unpublishing a published site
					EventTrackingService.post(EventTrackingService.newEvent(SiteService.EVENT_SITE_UNPUBLISH, sEdit.getReference(), true));
				}
				else if (!currentSitePublished && publishUnpublish)
				{
					// publishing a published site
					EventTrackingService.post(EventTrackingService.newEvent(SiteService.EVENT_SITE_PUBLISH, sEdit.getReference(), true));
				}
				state.setAttribute(STATE_TEMPLATE_INDEX, "12");

				// TODO: hard coding this frame id is fragile, portal dependent,
				// and needs to be fixed -ggolden
				// schedulePeerFrameRefresh("sitenav");
				scheduleTopRefresh();

				state.removeAttribute(STATE_JOINABLE);
				state.removeAttribute(STATE_JOINERROLE);

				// bjones86 - SAK-24423 - remove joinable site settings from the state
				JoinableSiteSettings.removeJoinableSiteSettingsFromState( state );
			}
		} else {
			// adding new site
			if (state.getAttribute(STATE_SITE_INFO) != null) {
				SiteInfo siteInfo = (SiteInfo) state
						.getAttribute(STATE_SITE_INFO);

				siteInfo.published = publishUnpublish;

				// site public choice
				siteInfo.include = include;

				// joinable site or not
				boolean joinable = state.getAttribute(STATE_JOINABLE) != null ? ((Boolean) state.getAttribute(STATE_JOINABLE)).booleanValue() : null;

				// bjones86 - SAK-24423 - update site info for joinable site settings
				JoinableSiteSettings.updateSiteInfoFromStateOnSiteUpdate( state, siteInfo, joinable );

				if (joinable) {
					siteInfo.joinable = true;
					String joinerRole = state.getAttribute(STATE_JOINERROLE) != null ? (String) state.getAttribute(STATE_JOINERROLE) : null;
					if (joinerRole != null) {
						siteInfo.joinerRole = joinerRole;
					} else {
						siteInfo.joinerRole = null;
						addAlert(state, rb.getString("java.joinsite") + " ");
					}
				} else {
					siteInfo.joinable = false;
					siteInfo.joinerRole = null;
				}
				// Stash the list of roles to add to the site.
				siteInfo.additionalRoles = new HashSet<String>();
				Map<String, AdditionalRole> additionalRoles = loadAdditionalRoles();
				for(String role : additionalRoles.keySet()) {
					boolean userChoice = params.getBoolean("role"+role);
					if (userChoice) {
						siteInfo.additionalRoles.add(role);
					}
				}

				state.setAttribute(STATE_SITE_INFO, siteInfo);
			}
						
			//if creating a site from an archive, go to that template
			//otherwise go to confirm page
			if (state.getAttribute(STATE_MESSAGE) == null) {
				if (state.getAttribute(STATE_CREATE_FROM_ARCHIVE) == Boolean.TRUE) {
					state.setAttribute(STATE_TEMPLATE_INDEX, "62");
				} else {
					state.setAttribute(STATE_TEMPLATE_INDEX, "10");
				}
			}
		}
			

	} // doUpdate_site_access
	
	private void updateAdditionalRole(SessionState state, Site site, String roleId, boolean add) {
		try {
			if (add) {
				AuthzGroup templateGroup = authzGroupService.getAuthzGroup("!site.roles");
				Role role = templateGroup.getRole(roleId);
				if (role == null) {
					role = templateGroup.getRole(".default");
				}
				if (site.getRole(roleId) == null) {
					try {
						site.addRole(roleId, role);
					} catch (RoleAlreadyDefinedException e) {
						addAlert(state, "java.authroleexists");
					}
				} else {
					M_log.warn("Attempting to add a role ("+ roleId+ ") that already exists in site: "+ site.getId());
				}
			} else {
				if (site.getRole(roleId) != null) {
					site.removeRole(roleId);
				} else {
					M_log.warn("Attempting to remove a role ("+ roleId+ ") that isn't defined in site: "+ site.getId());
				}
			}
		} catch (GroupNotDefinedException gnde) {
			addAlert(state, rb.getString("java.rolenotfound"));
		}
	}

	private void addAuthAnonRoles(SessionState state, Site site, boolean auth, boolean anon) {
		try {
			AuthzGroup templateGroup = authzGroupService.getAuthzGroup("!site.roles");
			if (auth) {
				if (site.getRole(".anon") != null) {
					site.removeRole(".anon");
				}
				if (site.getRole(".auth") == null) {
					try {
						site.addRole(".auth", templateGroup.getRole(".auth"));
					} catch (RoleAlreadyDefinedException e) {
						addAlert(state, "java.authroleexists");
					}
				}
			} else if (anon) {
				if (site.getRole(".auth") != null) {
					site.removeRole(".auth");
				}
				if (site.getRole(".anon") == null) {
					try {
						site.addRole(".anon", templateGroup.getRole(".anon"));
					} catch (RoleAlreadyDefinedException e) {
						addAlert(state, "java.anonroleexists");
					}
				}
			} else {
				if (site.getRole(".anon") != null) {
					site.removeRole(".anon");
				}
				if (site.getRole(".auth") != null) {
					site.removeRole(".auth");
				}
			}
		} catch (GroupNotDefinedException gnde) {
			addAlert(state, rb.getString("java.rolenotfound"));
		}
	}
	
	private void readInputAndUpdateStateVariable(SessionState state, ParameterParser params, String paramName, String stateAttributeName, boolean isBoolean)
	{
		String paramValue = StringUtils.trimToNull(params.getString(paramName));
		if (paramValue != null) {
			if (isBoolean)
			{
				state.setAttribute(stateAttributeName, Boolean.valueOf(paramValue));
			}
			else
			{
				state.setAttribute(stateAttributeName, paramValue);
			}
		} else {
			state.removeAttribute(stateAttributeName);
		}
	}
	
	/**
	 * Apply requested changes to a site's joinability. Only relevant for
	 * site edits, not new site creation.
	 * 
	 * <p>Not intended for direct execution from a Velocity-rendered form
	 * submit.</p>
	 * 
	 * <p>Originally extracted from {@link #doUpdate_site_access(RunData)} to 
	 * increase testability when adding special handling for an unspecified
	 * joinability parameter. The <code>sEdit</code> param is passed in
	 * to avoid repeated hits to the <code>SiteService</code> from
	 * {@link #getStateSite(SessionState)}. (It's called <code>sEdit</code> to
	 * reduce the scope of the refactoring diff just slightly.) <code>state</code> 
	 * is passed in to avoid more proliferation of <code>RunData</code> 
	 * downcasts.</p>
	 * 
	 * @see #CONVERT_NULL_JOINABLE_TO_UNJOINABLE
	 * @param data request context -- must not be <code>null</code>
	 * @param state session state -- must not be <code>null</code>
	 * @param params request parameter facade -- must not be <code>null</code>
	 * @param sEdit site to be edited -- must not be <code>null</code>
	 */
	void doUpdate_site_access_joinable(RunData data,
			SessionState state, ParameterParser params, Site sEdit) {
		boolean joinable = state.getAttribute(STATE_JOINABLE) != null ? ((Boolean) state.getAttribute(STATE_JOINABLE)).booleanValue() : null;
		if (!sEdit.isPublished())
		{
			// reset joinable role if the site is not published
			sEdit.setJoinable(false);
			sEdit.setJoinerRole(null);
		} else if (joinable) {
			sEdit.setJoinable(true);
			String joinerRole = state.getAttribute(STATE_JOINERROLE) != null ? (String) state.getAttribute(STATE_JOINERROLE) : null;
			if (joinerRole != null) {
				sEdit.setJoinerRole(joinerRole);
			} else {
				addAlert(state, rb.getString("java.joinsite") + " ");
			}

			// Handle invalid joinable site settings
			try
			{
				// Update site properties for joinable site settings
				JoinableSiteSettings.updateSitePropertiesFromStateOnSiteUpdate( sEdit.getPropertiesEdit(), state );
			}
			catch (InvalidJoinableSiteSettingsException invalidSettingsException)
			{
				addAlert(state, invalidSettingsException.getFormattedMessage(rb));
			}

		} else if ( !joinable || 
				(!joinable && ServerConfigurationService.getBoolean(CONVERT_NULL_JOINABLE_TO_UNJOINABLE, true))) {
			sEdit.setJoinable(false);
			sEdit.setJoinerRole(null);
		} // else just leave joinability alone
		
	}
	
	/**
	 * /* Actions for vm templates under the "chef_site" root. This method is
	 * called by doContinue. Each template has a hidden field with the value of
	 * template-index that becomes the value of index for the switch statement
	 * here. Some cases not implemented.
	 */
	private void actionForTemplate(String direction, int index,
			ParameterParser params, final SessionState state, RunData data) {
		// Continue - make any permanent changes, Back - keep any data entered
		// on the form
		boolean forward = "continue".equals(direction) ? true : false;

		SiteInfo siteInfo = new SiteInfo();
		// SAK-16600 change to new template for tool editing
			if (index==3) { index= 4;}			

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
		case 4:
			/*
			 * actionForTemplate chef_site-editFeatures.vm
			 * actionForTemplate chef_site-editToolGroupFeatures.vm
			 * 
			 */
			if (forward) {
				// editing existing site or creating a new one?
				Site site = getStateSite(state);
				getFeatures(params, state, site==null?"18":"15");
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
				if (getStateSite(state) == null)
				{
					// alerts after clicking Continue but not Back
					if (!forward) {
						// removing previously selected template site
						state.removeAttribute(STATE_TEMPLATE_SITE);				
					}
					
					updateSiteAttributes(state);
				}
				
				updateSiteInfo(params, state);
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
			break;
		case 27:
			/*
			 * actionForTemplate chef_site-importSites.vm
			 * 
			 * This is called after the tools have been selected on the import page (merge) and the finish button is clicked
			 * and is also called in the new site workflow when importing from an existing site
			 */
			if (forward) {
				Site existingSite = getStateSite(state);
				if (existingSite != null) {
					// revising a existing site's tool
					if (select_import_tools(params, state)) {
						String threadName = "SiteImportThread" + existingSite.getId();
						boolean found = false;
						//check all running threads for our named thread
						//this isn't cluster safe, but this check it more targeted towards
						//a single user re-importing multiple times during a slow import (which would be on the same server)
						for(Thread t : Thread.getAllStackTraces().keySet()){
							if(threadName.equals(t.getName())){
								found = true;
								break;
							}
						}
						if(found){
							//an existing thread is running for this site import, throw warning
							addAlert(state, rb.getString("java.import.existing"));
						}else{
							
							// list of tools that were selected for import
							final Hashtable importTools = (Hashtable) state.getAttribute(STATE_IMPORT_SITE_TOOL);
							
							//list of existing tools in the destination site
							final List existingTools = originalToolIds((List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST), state);
														
							final String userEmail = UserDirectoryService.getCurrentUser().getEmail();
							final Session session = SessionManager.getCurrentSession();
							final ToolSession toolSession = SessionManager.getCurrentToolSession();
							final String siteId = existingSite.getId();
							Thread siteImportThread = new Thread(){
								public void run() {
									Site existingSite;
									try {
										existingSite = SiteService.getSite(siteId);
										SessionManager.setCurrentSession(session);
										SessionManager.setCurrentToolSession(toolSession);
										EventTrackingService.post(EventTrackingService.newEvent(SiteService.EVENT_SITE_IMPORT_START, existingSite.getReference(), false));
										importToolIntoSite(existingTools, importTools, existingSite);
										if (ServerConfigurationService.getBoolean(SAK_PROP_IMPORT_NOTIFICATION, true)) {
											userNotificationProvider.notifySiteImportCompleted(userEmail, existingSite.getId(), existingSite.getTitle());
										}
										EventTrackingService.post(EventTrackingService.newEvent(SiteService.EVENT_SITE_IMPORT_END, existingSite.getReference(), false));
									} catch (IdUnusedException e) {
										M_log.error(e.getMessage(), e);
									}
								}
							};
							siteImportThread.setName(threadName);
							siteImportThread.start();
							state.setAttribute(IMPORT_QUEUED, rb.get("importQueued"));
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
			break;
		case 60:
			/*
			 * actionForTemplate chef_site-importSitesMigrate.vm
			 * 
			 * This is called after the tools have been selected on the import page (replace) and the finish button is clicked
			 *
			 */
			if (forward) {
				Site existingSite = getStateSite(state);
				if (existingSite != null) {
					// revising a existing site's tool
					if (select_import_tools(params, state)) {
						String threadName = "SiteImportThread" + existingSite.getId();
						boolean found = false;
						//check all running threads for our named thread
						//this isn't cluster safe, but this check it more targeted towards
						//a single user re-importing multiple times during a slow import (which would be on the same server)
						for(Thread t : Thread.getAllStackTraces().keySet()){
							if(threadName.equals(t.getName())){
								found = true;
								break;
							}
						}
						if(found){
							//an existing thread is running for this site import, throw warning
							addAlert(state, rb.getString("java.import.existing"));
						}else{
							
							// list of tools that were selected for import
							final Hashtable importTools = (Hashtable) state.getAttribute(STATE_IMPORT_SITE_TOOL);
							
							//list of existing tools in the destination site
							final List existingTools = originalToolIds((List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST), state);
							
							final String userEmail = UserDirectoryService.getCurrentUser().getEmail();
							final Session session = SessionManager.getCurrentSession();
							final ToolSession toolSession = SessionManager.getCurrentToolSession();
							final String siteId = existingSite.getId();
							Thread siteImportThread = new Thread(){
								public void run() {
									Site existingSite;
									try {
										existingSite = SiteService.getSite(siteId);
										SessionManager.setCurrentSession(session);
										SessionManager.setCurrentToolSession(toolSession);
										EventTrackingService.post(EventTrackingService.newEvent(SiteService.EVENT_SITE_IMPORT_START, existingSite.getReference(), false));
										// Remove all old contents before importing contents from new site
										importToolIntoSiteMigrate(existingTools, importTools, existingSite);
										if (ServerConfigurationService.getBoolean(SAK_PROP_IMPORT_NOTIFICATION, true)) {
											userNotificationProvider.notifySiteImportCompleted(userEmail, existingSite.getId(), existingSite.getTitle());
										}
										EventTrackingService.post(EventTrackingService.newEvent(SiteService.EVENT_SITE_IMPORT_END, existingSite.getReference(), false));
									} catch (IdUnusedException e) {
										M_log.error(e.getMessage(), e);
									}
								}
							};
							siteImportThread.setName(threadName);
							siteImportThread.start();
							state.setAttribute(IMPORT_QUEUED, rb.get("importQueued"));
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
			break;
		case 28:
			/*
			 * actionForTemplate chef_siteinfo-import.vm
			 * 
			 * This is called after the sites to import from have been selected on the import page and the next button is clicked
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
				
			}
			break;
		case 29:
			/*
			 * actionForTemplate chef_siteinfo-duplicate.vm
			 * 
			 */
			if (forward) {
				if (state.getAttribute(SITE_DUPLICATED) == null) {
					if (StringUtils.trimToNull(params.getString("title")) == null) {
						addAlert(state, rb.getString("java.dupli") + " ");
					} else {
						String title = params.getString("title");
						state.setAttribute(SITE_DUPLICATED_NAME, title);

						String newSiteId = IdManager.createUuid();
						try {
							String oldSiteId = (String) state
									.getAttribute(STATE_SITE_INSTANCE_ID);
							
							// Retrieve the source site reference to be used in the EventTrackingService
							// notification of the start/end of a site duplication.
							String sourceSiteRef = null;
							try {
								Site sourceSite = SiteService.getSite(oldSiteId);
								sourceSiteRef = sourceSite.getReference();
								
							} catch (IdUnusedException e) {
								M_log.warn(this + ".actionForTemplate; case29: invalid source siteId: "+oldSiteId);
								return;
							}

							// SAK-20797
							long oldSiteQuota = this.getSiteSpecificQuota(oldSiteId);
							
							Site site = SiteService.addSite(newSiteId,
									getStateSite(state));
							
							// An event for starting the "duplicate site" action
							EventTrackingService.post(EventTrackingService.newEvent(SiteService.EVENT_SITE_DUPLICATE_START, sourceSiteRef, site.getId(), false, NotificationService.NOTI_OPTIONAL));

							// get the new site icon url
							if (site.getIconUrl() != null)
							{
								site.setIconUrl(transferSiteResource(oldSiteId, newSiteId, site.getIconUrl()));
							}

							// set title
							site.setTitle(title);
							
							// SAK-20797 alter quota if required
							boolean	duplicateQuota = params.getString("dupequota") != null ? params.getBoolean("dupequota") : false;
							if (duplicateQuota==true) {
								
								if (oldSiteQuota > 0) {
									M_log.info("Saving quota");
									try {
										String collId = m_contentHostingService
												.getSiteCollection(site.getId());

										ContentCollectionEdit col = m_contentHostingService.editCollection(collId);

										ResourcePropertiesEdit resourceProperties = col.getPropertiesEdit();
										resourceProperties.addProperty(
												ResourceProperties.PROP_COLLECTION_BODY_QUOTA,
												new Long(oldSiteQuota)
														.toString());
										m_contentHostingService.commitCollection(col);										
										
										
									} catch (Exception ignore) {
										M_log.warn("saveQuota: unable to duplicate site-specific quota for site : "
												+ site.getId() + " : " + ignore);
									}
								}
							} 
							
							try {
								SiteService.save(site);

								// import tool content
								importToolContent(oldSiteId, site, false);
	
								String siteType = site.getType();
								if (SiteTypeUtil.isCourseSite(siteType)) {
									// for course site, need to
									// read in the input for
									// term information
									String termId = StringUtils.trimToNull(params
											.getString("selectTerm"));
									if (termId != null) {
										AcademicSession term = cms.getAcademicSession(termId);
										if (term != null) {
											ResourcePropertiesEdit rp = site.getPropertiesEdit();
											rp.addProperty(Site.PROP_SITE_TERM, term.getTitle());
											rp.addProperty(Site.PROP_SITE_TERM_EID, term.getEid());
										} else {
											M_log.warn("termId=" + termId + " not found");
										}
									}
								}
								
								// save again
								SiteService.save(site);
								state.setAttribute(STATE_DUPE_SITE_STATUS_ID, site.getId());
								state.setAttribute(STATE_DUPE_SITE_URL, site.getUrl());
								String realm = SiteService.siteReference(site.getId());
								try 
								{
									AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realm);
									if (SiteTypeUtil.isCourseSite(siteType)) 
									{
										// also remove the provider id attribute if any
										realmEdit.setProviderGroupId(null);
									}
									
									// add current user as the maintainer
									realmEdit.addMember(UserDirectoryService.getCurrentUser().getId(), site.getMaintainRole(), true, false);
									
									authzGroupService.save(realmEdit);
								} catch (GroupNotDefinedException e) {
									M_log.error(this + ".actionForTemplate chef_siteinfo-duplicate: IdUnusedException, not found, or not an AuthzGroup object "+ realm, e);
									addAlert(state, rb.getString("java.realm"));
								} catch (AuthzPermissionException e) {
									addAlert(state, this + rb.getString("java.notaccess"));
									M_log.error(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.notaccess"), e);
								}
							
							} catch (IdUnusedException e) {
								M_log.warn(this + " actionForTemplate chef_siteinfo-duplicate:: IdUnusedException when saving " + newSiteId);
							} catch (PermissionException e) {
								M_log.warn(this + " actionForTemplate chef_siteinfo-duplicate:: PermissionException when saving " + newSiteId);
							}

							// TODO: hard coding this frame id
							// is fragile, portal dependent, and
							// needs to be fixed -ggolden
							// schedulePeerFrameRefresh("sitenav");
							scheduleTopRefresh();
							
							// send site notification
							sendSiteNotification(state, site, null);

							state.setAttribute(SITE_DUPLICATED, Boolean.TRUE);
							
							// An event for ending the "duplicate site" action
							EventTrackingService.post(EventTrackingService.newEvent(SiteService.EVENT_SITE_DUPLICATE_END, sourceSiteRef, site.getId(), false, NotificationService.NOTI_OPTIONAL));

						} catch (IdInvalidException e) {
							addAlert(state, rb.getString("java.siteinval"));
							M_log.error(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.siteinval") + " site id = " + newSiteId, e);
						} catch (IdUsedException e) {
							addAlert(state, rb.getString("java.sitebeenused"));
							M_log.error(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.sitebeenused") + " site id = " + newSiteId, e);
						} catch (PermissionException e) {
							addAlert(state, rb.getString("java.allowcreate"));
							M_log.error(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.allowcreate") + " site id = " + newSiteId, e);
						}
					}
				}

				if (state.getAttribute(STATE_MESSAGE) == null) {
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
				List providerDescriptionChosenList = new Vector();
				
				if (params.getStrings("providerCourseAdd") == null) {
					state.removeAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
					state.removeAttribute(STATE_ADD_CLASS_PROVIDER_DESCRIPTION_CHOSEN);
					if (params.getString("manualAdds") == null) {
						addAlert(state, rb.getString("java.manual") + " ");
					}
				}
				if (state.getAttribute(STATE_MESSAGE) == null) {
					// The list of courses selected from provider listing
					if (params.getStrings("providerCourseAdd") != null) {
						providerChosenList = new ArrayList(Arrays.asList(params
								.getStrings("providerCourseAdd"))); // list of
						// description choices
						if (params.getStrings("providerCourseAddDescription") != null) {
							providerDescriptionChosenList = new ArrayList(Arrays.asList(params
									.getStrings("providerCourseAddDescription"))); // list of
							state.setAttribute(STATE_ADD_CLASS_PROVIDER_DESCRIPTION_CHOSEN,
								providerDescriptionChosenList);
						}
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
							String additional = StringUtils.trimToEmpty(params
									.getString("additional"));
							state.setAttribute(FORM_ADDITIONAL, additional);
						}
					}
					collectNewSiteInfo(state, params, providerChosenList);
					
					String find_course = params.getString("find_course");
					if (state.getAttribute(STATE_TEMPLATE_SITE) != null && (find_course == null || !"true".equals(find_course)))
					{
						// creating based on template
						doFinish(data);
					}
				}
			}
			break;
		case 38:
			break;
		case 39:
			break;
		case 42:
			/*
			 * actionForTemplate chef_site-type-confirm.vm
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
						&& !"back".equals(direction)) {
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

					//Track provider deletes, seems like the only place to do it. If a confirmation is ever added somewhere, don't do this.
					trackRosterChanges(org.sakaiproject.site.api.SiteService.EVENT_SITE_ROSTER_REMOVE,providerCourseDeleteList);
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
						catch (IdNotFoundException e)
						{
							M_log.warn("actionForTemplate 43 editClass: Cannot find section " + sectionId);
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
					M_log.error(this + ".actionForTemplate chef_siteinfo-addCourseConfirm: " +  e.getMessage() + site.getId(), e);
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
		case 61:
			// import users
			if (forward) {
				if (params.getStrings("importSites") == null) {
					addAlert(state, rb.getString("java.toimport") + " ");
				} else {
					importSitesUsers(params, state);
				}
			}
			break;
		}

	}// actionFor Template
	
	/**
	 * 
	 * @param action
	 * @param providers
	 */
	private void trackRosterChanges(String event, List <String> rosters) {
		if (ServerConfigurationService.getBoolean(
				SiteHelper.WSETUP_TRACK_ROSTER_CHANGE, false)) {
			// event for each individual update
			if (rosters != null) {
				for (String roster : rosters) {
					EventTrackingService.post(EventTrackingService.newEvent(event, "roster="+roster, true));
				}
			}
		}
	}

	/**
	 * import not-provided users from selected sites
	 * @param params
	 */
	private void importSitesUsers(ParameterParser params, SessionState state) {
		// the target site
		Site site = getStateSite(state);
		try {
			// the target realm
			AuthzGroup realm = authzGroupService.getAuthzGroup(SiteService.siteReference(site.getId()));
			
			List importSites = new ArrayList(Arrays.asList(params.getStrings("importSites")));
			for (int i = 0; i < importSites.size(); i++) {
				String fromSiteId = (String) importSites.get(i);
				try {
					Site fromSite = SiteService.getSite(fromSiteId);
					
					// get realm information
					String fromRealmId = SiteService.siteReference(fromSite.getId());
					AuthzGroup fromRealm = authzGroupService.getAuthzGroup(fromRealmId);
					// get all users in the from site
					Set fromUsers = fromRealm.getUsers();
					for (Iterator iFromUsers = fromUsers.iterator(); iFromUsers.hasNext();)
					{
						String fromUserId = (String) iFromUsers.next();
						Member fromMember = fromRealm.getMember(fromUserId);
						if (!fromMember.isProvided())
						{
							// add user
							realm.addMember(fromUserId, fromMember.getRole().getId(), fromMember.isActive(), false);
						}
					}
				} catch (GroupNotDefinedException e) {
					M_log.error(this + ".importSitesUsers: GroupNotDefinedException, " + fromSiteId + " not found, or not an AuthzGroup object", e);
					addAlert(state, rb.getString("java.cannotedit"));
				} catch (IdUnusedException e) {
					M_log.error(this + ".importSitesUsers: IdUnusedException, " + fromSiteId + " not found, or not an AuthzGroup object", e);
				
				}
			}
			
			// post event about the realm participant update
			EventTrackingService.post(EventTrackingService.newEvent(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP, realm.getId(), false));
			
			// save realm
			authzGroupService.save(realm);
			
		} catch (GroupNotDefinedException e) {
			M_log.error(this + ".importSitesUsers: IdUnusedException, " + site.getTitle() + "(" + site.getId() + ") not found, or not an AuthzGroup object", e);
			addAlert(state, rb.getString("java.cannotedit"));
		} catch (AuthzPermissionException e) {
			M_log.error(this + ".importSitesUsers: PermissionException, user does not have permission to edit AuthzGroup object " + site.getTitle() + "(" + site.getId() + "). ", e);
			addAlert(state, rb.getString("java.notaccess"));
		}
	}

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
		
		String access = ServerConfigurationService.getAccessUrl();
		if (siteAttribute!= null && siteAttribute.indexOf(oSiteId) != -1 && access != null)
		{
			Reference ref = null;
			try
			{
				URI accessUrl = new URI(access);
				URI url = new URI(siteAttribute);
				String path = url.getPath();
				String accessPath = accessUrl.getPath();
				
				// stripe out the access url, get the relative form of "url"
				String contentRef = path.replaceAll(accessPath, "");
				
				ref = EntityManager.newReference(contentRef);
				
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
					M_log.warn(this + ":transferSiteResource: cannot find resource with id=" + nResource + " copying it from the original resource " + n2Exception.getMessage());
					// copy the resource then
					try
					{
						nResourceId = m_contentHostingService.copy(resource.getId(), nResourceId);
						nResource = m_contentHostingService.getResource(nResourceId);
					}
					catch (Exception n3Exception)
					{
						M_log.warn(this + ":transferSiteResource: something happened copying the resource with id="+ resource.getId() + " " + n3Exception.getMessage());
					}
				}
				
				// get the new resource url
				rv = nResource != null?nResource.getUrl(false):"";
				
			}
			catch (URISyntaxException use)
			{
				M_log.warn("Couldn't update site resource: "+ siteAttribute + " "+ use.getMessage());
			}
			catch (Exception refException)
			{
				M_log.warn(this + ":transferSiteResource: cannot find resource with ref=" + ref.getReference() + " " + refException.getMessage());
			}
		}
		
		return rv;
	}
	
	/**
	 * copy tool content from old site
	 * @param oSiteId source (old) site id
	 * @param site destination site
	 * @param bypassSecurity use SecurityAdvisor if true
	 */
	private void importToolContent(String oSiteId, Site site, boolean bypassSecurity) {
		String nSiteId = site.getId();

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
		Set<String> toolsCopied = new HashSet<String>();
		
		Map transversalMap = new HashMap();

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
						Map<String,String> entityMap = transferCopyEntities(
								toolId,
								m_contentHostingService
										.getSiteCollection(oSiteId),
								m_contentHostingService
										.getSiteCollection(nSiteId));
						if(entityMap != null){							 
							transversalMap.putAll(entityMap);
						}
					} else if (toolId.equalsIgnoreCase(SITE_INFORMATION_TOOL)) {
						// handle Home tool specially, need to update the site infomration display url if needed
						String newSiteInfoUrl = transferSiteResource(oSiteId, nSiteId, site.getInfoUrl());
						site.setInfoUrl(newSiteInfoUrl);
					}
					else {
						// other
						// tools
						// SAK-19686 - added if statement and toolsCopied.add
						if (!toolsCopied.contains(toolId)) {
							Map<String,String> entityMap = transferCopyEntities(toolId,
										oSiteId, nSiteId);
							if(entityMap != null){							 
								transversalMap.putAll(entityMap);
							}
							toolsCopied.add(toolId);
						}
					}
				}
			}
			
			//update entity references
			toolsCopied = new HashSet<String>();
			for (ListIterator i = pageList
					.listIterator(); i.hasNext();) {
				SitePage page = (SitePage) i.next();

				List pageToolList = page.getTools();
				if (!(pageToolList == null || pageToolList.size() == 0))
				{					
					Tool tool = ((ToolConfiguration) pageToolList.get(0)).getTool();
					String toolId = tool != null?tool.getId():"";
					
					updateEntityReferences(toolId, nSiteId, transversalMap, site);
				}
			}
		}
		
		if (bypassSecurity)
		{
			SecurityService.popAdvisor();
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
	 * remove related state variable for adding class
	 * 
	 * @param state
	 *            SessionState object
	 */
	private void removeAddClassContext(SessionState state) {
		// remove related state variables
		state.removeAttribute(STATE_ADD_CLASS_MANUAL);
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER_DESCRIPTION_CHOSEN);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_NUMBER);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
		state.removeAttribute(STATE_SITE_QUEST_UNIQNAME);
		state.removeAttribute(STATE_AUTO_ADD);
		state.removeAttribute(STATE_IMPORT_SITE_TOOL);
		state.removeAttribute(STATE_IMPORT_SITES);
		state.removeAttribute(STATE_CM_REQUESTED_SECTIONS);
		state.removeAttribute(STATE_CM_SELECTED_SECTIONS);
		state.removeAttribute(FORM_SITEINFO_ALIASES);
		state.removeAttribute(FORM_SITEINFO_URL_BASE);
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
				AuthzGroup realmEdit1 = authzGroupService
						.getAuthzGroup(realmId);
				
				boolean hasNonProvidedMainroleUser = false;
				String maintainRoleString = realmEdit1.getMaintainRole();
				Set<String> maintainRoleUsers = realmEdit1.getUsersHasRole(maintainRoleString);
				if (!maintainRoleUsers.isEmpty()) 
				{
					for(Iterator<String> users = maintainRoleUsers.iterator(); !hasNonProvidedMainroleUser && users.hasNext();)
					{
						String userId = users.next();
						if (!realmEdit1.getMember(userId).isProvided())
							hasNonProvidedMainroleUser = true;
					}
				}
				if (!hasNonProvidedMainroleUser)
				{
					// if after the removal, there is no provider id, and there is no maintain role user anymore, show alert message and don't save the update
					addAlert(state, rb.getString("sitegen.siteinfolist.nomaintainuser")
							+ maintainRoleString + ".");
				}
				else
				{
					realmEdit1.setProviderGroupId(NULL_STRING);
					authzGroupService.save(realmEdit1);
				}
			} catch (GroupNotDefinedException e) {
				M_log.error(this + ".updateCourseClasses: IdUnusedException, " + site.getTitle()
						+ "(" + realmId
						+ ") not found, or not an AuthzGroup object", e);
				addAlert(state, rb.getString("java.cannotedit"));
				return;
			} catch (AuthzPermissionException e) {
				M_log.error(this + ".updateCourseClasses: PermissionException, user does not have permission to edit AuthzGroup object "
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
				AuthzGroup realmEdit2 = authzGroupService
						.getAuthzGroup(realmId);
				realmEdit2.setProviderGroupId(externalRealm);
				authzGroupService.save(realmEdit2);
			} catch (GroupNotDefinedException e) {
				M_log.error(this + ".updateCourseClasses: IdUnusedException, " + site.getTitle()
						+ "(" + realmId
						+ ") not found, or not an AuthzGroup object", e);
				addAlert(state, rb.getString("java.cannotclasses"));
				return;
			} catch (AuthzPermissionException e) {
				M_log.error(this
								+ ".updateCourseClasses: PermissionException, user does not have permission to edit AuthzGroup object "
								+ site.getTitle() + "(" + realmId + "). ", e);
				addAlert(state, rb.getString("java.notaccess"));
				return;
			}

		}

		//reload the site object after changes group realms have been removed from the site.
		site = getStateSite(state); 

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
				AuthzGroup gRealm = authzGroupService.getAuthzGroup(group.getReference());
				String gProviderId = StringUtils.trimToNull(gRealm.getProviderGroupId());
				if (gProviderId != null)
				{
					if (!listContainsString(manualCourseSectionList, gProviderId) 
						&& !listContainsString(cmRequestedCourseList, gProviderId) 
						&& !listContainsString(providerCourseSectionList, gProviderId))
					{
						// if none of those three lists contains the provider id, remove the group and realm
						authzGroupService.removeAuthzGroup(group.getReference());
					}
				}
			}
			catch (Exception e)
			{
				M_log.error(this + ".updateCourseClasses: cannot remove authzgroup : " + group.getReference(), e);
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
				M_log.error(this +".updateCourseClasses:" + e.toString(), e);
			}
		}
		if (notifyClasses != null && notifyClasses.size() > 0) {
			try {
				// send out class access confirmation notifications
				sendSiteNotification(state, getStateSite(state), notifyClasses);
			} catch (Exception e) {
				M_log.error(this + ".updateCourseClasses:" + e.toString(), e);
			}
		}
	} // updateCourseClasses

	/**
	 * test whether 
	 * @param list
	 * @param providerId
	 * @return
	 */
	boolean listContainsString(List list, String s)
	{
		boolean rv = false;
		if (list != null && !list.isEmpty() && s != null && s.length() != 0)
		{
			for (Object o : list)
			{
				// deals with different object type
				if (o instanceof SectionObject)
				{
					rv = ((SectionObject) o).getEid().equals(s);
				}
				else if (o instanceof String)
				{
					rv = ((String) o).equals(s);
				}
				
				// exit when find match
				if (rv)
					break;
			}
		}
		return rv;
	}
	private void setSiteSectionProperty(List courseSectionList, Site site, String propertyName) {
		if ((courseSectionList != null) && (courseSectionList.size() != 0)) {
			// store the requested sections in one site property
			StringBuffer sections = new StringBuffer();
			for (int j = 0; j < courseSectionList.size();) {
				sections = sections.append(courseSectionList.get(j));
				if (courseSectionList.get(j) instanceof SectionObject)
				{	 
					SectionObject so = (SectionObject) courseSectionList.get(j);	 
					sections = sections.append(so.getEid());	 
				}	 
				else if (courseSectionList.get(j) instanceof String)	 
				{	 
					sections = sections.append((String) courseSectionList.get(j));	 
				}
				j++;
				if (j < courseSectionList.size()) {
					sections = sections.append("+");
				}
			}
			ResourcePropertiesEdit rp = site.getPropertiesEdit();
			rp.addProperty(propertyName, sections.toString());
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
				String rId = StringUtils.trimToNull(params.getString("role"
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
	} // doSiteinfo_edit_role

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

			// Handle invalid joinable site settings
			try
			{
				// Update site properties for joinable site settings
				JoinableSiteSettings.updateSitePropertiesFromStateOnSiteInfoSaveGlobalAccess( s.getPropertiesEdit(), state );
			}
			catch (InvalidJoinableSiteSettingsException invalidSettingsException)
			{
				addAlert(state, invalidSettingsException.getFormattedMessage(rb));
			}
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
			if (StringUtils.trimToNull(siteInfo.title) != null) {
				site.setTitle(siteInfo.title);
			}
			if (siteInfo.description != null) {
				site.setDescription(siteInfo.description);
			}
			site.setPublished(siteInfo.published);

			setAppearance(state, site, siteInfo.iconUrl);

			site.setJoinable(siteInfo.joinable);
			if (StringUtils.trimToNull(siteInfo.joinerRole) != null) {
				site.setJoinerRole(siteInfo.joinerRole);
			}
			// Make changes and then put changed site back in state
			String id = site.getId();

			// bjones86 - SAK-24423 - update site properties for joinable site settings
			JoinableSiteSettings.updateSitePropertiesFromStateOnUpdateSiteAttributes( site, state );

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
					M_log.error(this + ".updateSiteAttributes: IdUnusedException "
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
		// title
		boolean hasRosterAttached = params.getString("hasRosterAttached") != null ? Boolean.getBoolean(params.getString("hasRosterAttached")) : false;
		if ((siteTitleEditable(state, siteInfo.site_type) || !hasRosterAttached) && params.getString("title") != null) 	 
		{ 	 
			// site titel is editable and could not be null
			String title = StringUtils.trimToNull(params.getString("title"));
			siteInfo.title = title;
			
			if (title == null) { 	 
				addAlert(state, rb.getString("java.specify") + " "); 	 
			} 	 
			// check for site title length 	 
			else if (title.length() > SiteConstants.SITE_GROUP_TITLE_LIMIT) 	 
			{ 	 
				addAlert(state, rb.getFormattedMessage("site_group_title_length_limit", new Object[]{SiteConstants.SITE_GROUP_TITLE_LIMIT})); 	 
			}
		}
				
		if (params.getString("description") != null) {
			StringBuilder alertMsg = new StringBuilder();
			String description = params.getString("description");
			siteInfo.description = FormattedText.processFormattedText(description, alertMsg);
		}
		if (params.getString("short_description") != null) {
			siteInfo.short_description = params.getString("short_description");
		}
		String skin = params.getString("skin"); 	 
		if (skin != null) { 	 
			// if there is a skin input for course site 	 
			skin = StringUtils.trimToNull(skin);
			siteInfo.iconUrl = skin; 	 
		} else { 	 
			// if ther is a icon input for non-course site 	 
			String icon = StringUtils.trimToNull(params.getString("icon")); 	 
			if (icon != null) { 	 
				if (icon.endsWith(PROTOCOL_STRING)) { 	 
					addAlert(state, rb.getString("alert.protocol")); 	 
				} 	 
				siteInfo.iconUrl = icon; 	 
			} else { 	 
				siteInfo.iconUrl = "";
			} 	 
		} 	 
		if (params.getString("additional") != null) {
			siteInfo.additional = params.getString("additional");
		}
		if (params.getString("iconUrl") != null) {
			siteInfo.iconUrl = params.getString("iconUrl");
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

		// bjones86 - SAK-24423 - update site info for joinable site settings
		JoinableSiteSettings.updateSiteInfoFromParams( params, siteInfo );

		// site contact information
		String name = StringUtils.trimToEmpty(params.getString("siteContactName"));
		if (name.length() == 0)
		{
			addAlert(state, rb.getString("alert.sitediinf.sitconnam"));
		}
		siteInfo.site_contact_name = name;
		String email = StringUtils.trimToEmpty(params
				.getString("siteContactEmail"));
		if (email != null) {
			if (!email.isEmpty() && !EmailValidator.getInstance().isValid(email)) {
				// invalid email
				addAlert(state, rb.getFormattedMessage("java.invalid.email", new Object[]{FormattedText.escapeHtml(email,false)}));
			}
			siteInfo.site_contact_email = email;
		}

		int aliasCount = params.getInt("alias_count", 0);
		siteInfo.siteRefAliases.clear();
		for ( int j = 0; j < aliasCount ; j++ ) {
			String alias = StringUtils.trimToNull(params.getString("alias_" + j));
			if ( alias == null ) {
				continue;
			} 
			// Kernel will force these to lower case anyway. Forcing
			// to lower case whenever reading out of the form simplifies
			// comparisons at save time, though, and provides consistent 
			// on-screen display.
			alias = alias.toLowerCase();
			// An invalid alias will set an alert, which theoretically
			// disallows further progress in the workflow, but we
			// still need to re-render the invalid form contents.
			// Thus siteInfo.aliases contains all input aliases, even if
			// invalid. (Same thing happens above for email.)
			validateSiteAlias(alias, state);
			siteInfo.siteRefAliases.add(alias);
		}

		
		state.setAttribute(STATE_SITE_INFO, siteInfo);
		
	} // updateSiteInfo

	private boolean validateSiteAlias(String aliasId, SessionState state) {
		if ( (aliasId = StringUtils.trimToNull(aliasId)) == null ) {
			addAlert(state, rb.getFormattedMessage("java.alias.isinval", new Object[]{aliasId}));
			return false;
		}
		boolean isSimpleResourceName = aliasId.equals(Validator.escapeResourceName(aliasId));
		boolean isSimpleUrl = aliasId.equals(Validator.escapeUrl(aliasId));
		if ( !(isSimpleResourceName) || !(isSimpleUrl) ) {
			// The point of these site aliases is to have easy-to-recall,
			// easy-to-guess URLs. So we take a very conservative approach
			// here and disallow any aliases which would require special 
			// encoding or would simply be ignored when building a valid 
			// resource reference or outputting that reference as a URL.
			addAlert(state, rb.getFormattedMessage("java.alias.isinval", new Object[]{aliasId}));
			return false;
		} else {
			String currentSiteId = StringUtils.trimToNull((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
			boolean editingSite = currentSiteId != null;
			try {
				String targetRef = aliasService.getTarget(aliasId);
				if ( editingSite ) {
					String siteRef = SiteService.siteReference(currentSiteId);
					boolean targetsCurrentSite = siteRef.equals(targetRef);
					if ( !(targetsCurrentSite) ) {
						addAlert(state, rb.getFormattedMessage("java.alias.exists", new Object[]{aliasId}));
						return false;
					}
				} else {
					addAlert(state, rb.getFormattedMessage("java.alias.exists", new Object[]{aliasId}));
					return false;
				}
			} catch (IdUnusedException e) {
				// No conflicting aliases
			}
			return true;
		}
	}
	
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
			AuthzGroup realm = authzGroupService.getAuthzGroup(realmId);
			// Filter the roles so we only display user roles
			for (Role role: (Set<Role>)realm.getRoles()) {
				if (authzGroupService.isRoleAssignable(role.getId())) {
					roles.add(role);
				}
			}
			Collections.sort(roles);
		} catch (GroupNotDefinedException e) {
			M_log.error( this + ".getRoles: IdUnusedException " + realmId, e);
		}
		return roles;

	} // getRoles
	
	/**
	 * getRoles
	 * 
	 * bjones86 - SAK-23257 - added state and siteType parameters so list 
	 * of joiner roles can respect the restricted role lists in sakai.properties.
	 * 
	 */
	private List<Role> getJoinerRoles(String realmId, SessionState state, String siteType) {
		List roles = new ArrayList();
		/** related to SAK-18462, this is a list of permissions that the joinable roles shouldn't have ***/
		String[] prohibitPermissionForJoinerRole = ServerConfigurationService.getStrings("siteinfo.prohibited_permission_for_joiner_role");
		if (prohibitPermissionForJoinerRole == null) {
			prohibitPermissionForJoinerRole = new String[]{"site.upd"};
		}
		if (realmId != null)
		{
			try {
				AuthzGroup realm = authzGroupService.getAuthzGroup(realmId);
				// get all roles that allows at least one permission in the list
				Set<String> permissionAllowedRoleIds = new HashSet<String>();
				for(String permission:prohibitPermissionForJoinerRole)
				{
					permissionAllowedRoleIds.addAll(realm.getRolesIsAllowed(permission));
				}
				
				// bjones86 - SAK-23257
				List<Role> allowedRoles = null;
				Set<String> restrictedRoles = null;
				if (null == state.getAttribute(STATE_SITE_INSTANCE_ID)) {
					restrictedRoles = SiteParticipantHelper.getRestrictedRoles(state.getAttribute(STATE_SITE_TYPE ).toString());
				}
				else {
					allowedRoles = SiteParticipantHelper.getAllowedRoles(siteType, getRoles(state));
				}
				
				for(Role role:realm.getRoles())
				{
					if (isUserRole(role) && (permissionAllowedRoleIds == null 
							|| permissionAllowedRoleIds!= null && !permissionAllowedRoleIds.contains(role.getId())))
					{
						// bjones86 - SAK-23257
						if (allowedRoles != null && allowedRoles.contains(role)) {
							roles.add(role);
						}
						else if (restrictedRoles != null &&
								(!restrictedRoles.contains(role.getId()) || !restrictedRoles.contains(role.getId().toLowerCase()))) {
							roles.add(role);
						}
					}
				}
				Collections.sort(roles);
			} catch (GroupNotDefinedException e) {
				M_log.error( this + ".getRoles: IdUnusedException " + realmId, e);
			}
		}
		return roles;

	} // getRolesWithoutPermission

	private boolean isUserRole(Role role) {
		return !role.getId().startsWith(".");
	}

	private void addSynopticTool(SitePage page, String toolId,
			String toolTitle, String layoutHint, int position) {
		page.setLayout(SitePage.LAYOUT_DOUBLE_COL);
		
		// Add synoptic announcements tool
		ToolConfiguration tool = page.addTool();
		Tool reg = ToolManager.getTool(toolId);
		tool.setTool(toolId, reg);
		tool.setTitle(toolTitle);
		tool.setLayoutHints(layoutHint);
		
		// count how many synoptic tools in the second/right column
		int totalSynopticTools = 0;
		for (ToolConfiguration t : page.getTools())
		{
			if (t.getToolId() != null && SYNOPTIC_TOOL_ID_MAP.containsKey(t.getToolId()))
			{
				totalSynopticTools++;
			}
		}
		// now move the newly added synoptic tool to proper position
		for (int i=0; i< (totalSynopticTools-position-1);i++)
		{
			tool.moveUp();
		}
	}

	private void saveFeatures(ParameterParser params, SessionState state, Site site) {
		
		String siteType = checkNullSiteType(state, site.getType(), site.getId());
		
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
		String homePageId = null;
		boolean homeInWSetupPageList = false;

		List chosenList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);

		boolean hasEmail = false;
		boolean hasSiteInfo = false;
		
		// tools to be imported from other sites?
		Hashtable importTools = null;
		if (state.getAttribute(STATE_IMPORT_SITE_TOOL) != null) {
			importTools = (Hashtable) state.getAttribute(STATE_IMPORT_SITE_TOOL);
		}
		
		// Home tool chosen?
		if (chosenList.contains(TOOL_ID_HOME)) {
			// add home tool later
			hasHome = true;
		}
		
		// order the id list
		chosenList = orderToolIds(state, siteType, chosenList, false);
		
		// Special case - Worksite Setup Home comes from a hardcoded checkbox on
		// the vm template rather than toolRegistrationList
		// see if Home was chosen
		for (ListIterator j = chosenList.listIterator(); j.hasNext();) {
			String choice = (String) j.next();
			if ("sakai.mailbox".equals(choice)) {
				hasEmail = true;
				String alias = StringUtils.trimToNull((String) state
						.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
				String channelReference = mailArchiveChannelReference(site.getId());
				if (alias != null) {
					if (!Validator.checkEmailLocal(alias)) {
						addAlert(state, rb.getString("java.theemail"));
					} else if (!aliasService.allowSetAlias(alias, channelReference )) {
						addAlert(state, rb.getString("java.addalias"));
					} else {
						try {
							String target = aliasService.getTarget(alias);
							boolean targetsThisSite = site.getReference().equals(target) ||
									channelReference.equals(target);
							if (!(targetsThisSite)) {
								addAlert(state, rb.getString("java.emailinuse") + " ");
							}
						} catch (IdUnusedException ee) {
							try {
								aliasService.setAlias(alias,
										channelReference);
							} catch (IdUsedException exception) {
								M_log.error(this + ".saveFeatures setAlias IdUsedException:"+exception.getMessage()+" alias="+ alias + " channelReference="+channelReference, exception);
							} catch (IdInvalidException exception) {
								M_log.error(this + ".saveFeatures setAlias IdInvalidException:"+exception.getMessage()+" alias="+ alias + " channelReference="+channelReference, exception);
							} catch (PermissionException exception) {
								M_log.error(this + ".saveFeatures setAlias PermissionException:"+exception.getMessage()+" alias="+ alias + " channelReference="+channelReference, exception);
							}
						}
					}
				}
			}else if (choice.equals(TOOL_ID_SITEINFO)) {
				hasSiteInfo = true;
			}
			
		}

		// see if Home and/or Help in the wSetupPageList (can just check title
		// here, because we checked patterns before adding to the list)
		for (ListIterator i = wSetupPageList.listIterator(); i.hasNext();) {
			wSetupPage = (WorksiteSetupPage) i.next();
			if (isHomePage(site.getPage(wSetupPage.getPageId()))) {
				homeInWSetupPageList = true;
				homePageId = wSetupPage.getPageId();
				break;
			}
		}

		if (hasHome) {
			SitePage page = site.getPage(homePageId);
			
			if (!homeInWSetupPageList) {
				// if Home is chosen and Home is not in wSetupPageList, add Home
				// to site and wSetupPageList
				page = site.addPage();

				page.setTitle(rb.getString("java.home"));

				wSetupHome.pageId = page.getId();
				wSetupHome.pageTitle = page.getTitle();
				wSetupHome.toolId = TOOL_ID_HOME;
				wSetupPageList.add(wSetupHome);
			}
			// the list tools on the home page
			List<ToolConfiguration> toolList = page.getTools();
			// get tool id set for Home page from configuration
			List<String> homeToolIds = getHomeToolIds(state, !homeInWSetupPageList, page);
			
			// count
			int nonSynopticToolIndex=0, synopticToolIndex = 0;
			
			for (String homeToolId: homeToolIds)
			{
				if (!SYNOPTIC_TOOL_ID_MAP.containsKey(homeToolId))
				{
					if (!pageHasToolId(toolList, homeToolId))
					{
						// not a synoptic tool and is not in Home page yet, just add it
						Tool reg = ToolManager.getTool(homeToolId);
						if (reg != null)
						{
							ToolConfiguration tool = page.addTool();
							tool.setTool(homeToolId, reg);
							tool.setTitle(reg.getTitle() != null?reg.getTitle():"");
							tool.setLayoutHints("0," + nonSynopticToolIndex++);
						}
					}
				}
				else
				{
					// synoptic tool 
					List<String> parentToolList = (List<String>) SYNOPTIC_TOOL_ID_MAP.get(homeToolId);
					List chosenListClone = new Vector();
					// chosenlist may have things like bcf89cd4-fa3a-4dda-80bd-ed0b89981ce7sakai.chat
					// get list of the actual tool names
					List<String>chosenOrigToolList = new ArrayList<String>();
					for (String chosenTool: (List<String>)chosenList)
					    chosenOrigToolList.add(findOriginalToolId(state, chosenTool));
					chosenListClone.addAll(chosenOrigToolList);
					boolean hasAnyParentToolId = chosenListClone.removeAll(parentToolList);
					
					//first check whether the parent tool is available in site but its parent tool is no longer selected
					if (pageHasToolId(toolList, homeToolId))
					{
						if (!hasAnyParentToolId && !SiteService.isUserSite(site.getId()))
						{
							for (ListIterator iToolList = toolList.listIterator(); iToolList.hasNext();) 
							{
								ToolConfiguration tConf= (ToolConfiguration) iToolList.next();
								// avoid NPE when the tool definition is missing
								if (tConf.getTool() != null && homeToolId.equals(tConf.getTool().getId()))
								{
									page.removeTool((ToolConfiguration) tConf);
									break;
								}
							}
						}
						else
						{
							synopticToolIndex++;
						}
					}
					
					// then add those synoptic tools which wasn't there before
					if (!pageHasToolId(toolList, homeToolId) && hasAnyParentToolId)
					{
						try
						{
							// use value from map to find an internationalized tool title
							String toolTitleText = rb.getString(SYNOPTIC_TOOL_TITLE_MAP.get(homeToolId));
							addSynopticTool(page, homeToolId, toolTitleText, synopticToolIndex + ",1", synopticToolIndex++);
						} catch (Exception e) {
							M_log.error(this + ".saveFeatures addSynotpicTool: " + e.getMessage() + " site id = " + site.getId() + " tool = " + homeToolId, e);
						}
					}
					
				}
			}
			
			if (page.getTools().size() == 1)
			{
				// only use one column layout
				page.setLayout(SitePage.LAYOUT_SINGLE_COL);
			}
			
			// mark this page as Home page inside its property
			if (page.getProperties().getProperty(SitePage.IS_HOME_PAGE) == null)
			{
				page.getPropertiesEdit().addProperty(SitePage.IS_HOME_PAGE, Boolean.TRUE.toString());
			}
			
		} // add Home

		// if Home is in wSetupPageList and not chosen, remove Home feature from
		// wSetupPageList and site
		if (!hasHome && homeInWSetupPageList) {
			// remove Home from wSetupPageList
			for (ListIterator i = wSetupPageList.listIterator(); i.hasNext();) {
				WorksiteSetupPage comparePage = (WorksiteSetupPage) i.next();
				SitePage sitePage = site.getPage(comparePage.getPageId());
				if (sitePage != null && isHomePage(sitePage)) {
					// remove the Home page
					site.removePage(sitePage);
					wSetupPageList.remove(comparePage);
					break;
				}
			}
		}

		// declare flags used in making decisions about whether to add, remove,
		// or do nothing
		boolean inChosenList;
		boolean inWSetupPageList;

		Set categories = new HashSet();
		// UMICH 1035
		categories.add(siteType);
		categories.add(SiteTypeUtil.getTargetSiteType(siteType));
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

			// exclude the Home page if there is any
			if (!inChosenList && !(homePageId != null && wSetupPage.getPageId().equals(homePageId))) {
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
		for (ListIterator j = orderToolIds(state, siteType, chosenList, false)
				.listIterator(); j.hasNext();) {
			String toolId = (String) j.next();
			boolean multiAllowed = isMultipleInstancesAllowed(findOriginalToolId(state, toolId));
			// exclude Home tool
			if (!toolId.equals(TOOL_ID_HOME))
			{
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
					if (multiAllowed) {
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
					String toolRegId = toolReg.getId();
					if (toolId.equals(toolRegId)) {
						toolRegFound = toolReg;
						break;
					}
					else if (multiAllowed && toolId.startsWith(toolRegId))
					{
						try
						{
							// in case of adding multiple tools, tool id is of format ORIGINAL_TOOL_ID + INDEX_NUMBER
							Integer.parseInt(toolId.replace(toolRegId, ""));
							toolRegFound = toolReg;
							break;
						}
						catch (Exception parseException)
						{
							// ignore parse exception
						}
					}
				}

				if (toolRegFound != null) {
					// we know such a tool, so add it
					WorksiteSetupPage addPage = new WorksiteSetupPage();
					SitePage page = site.addPage();
					addPage.pageId = page.getId();
					if (multiAllowed) {
						// set tool title
						page.setTitle((String) multipleToolIdTitleMap.get(toolId));
						page.setTitleCustom(true);
					} else {
						// other tools with default title
						page.setTitle(toolRegFound.getTitle());
					}
					page.setLayout(SitePage.LAYOUT_SINGLE_COL);

					// if so specified in the tool's registration file, 
					// configure the tool's page to open in a new window.
					if ("true".equals(toolRegFound.getRegisteredConfig().getProperty("popup"))) {
					    page.setPopup(true);
					}
					ToolConfiguration tool = page.addTool();
					tool.setTool(toolRegFound.getId(), toolRegFound);
					addPage.toolId = toolId;
					wSetupPageList.add(addPage);

					// set tool title
					if (multiAllowed) {
						// set tool title
						tool.setTitle((String) multipleToolIdTitleMap.get(toolId));
						// save tool configuration
						saveMultipleToolConfiguration(state, tool, toolId);
					} else {
						tool.setTitle(toolRegFound.getTitle());
					}
				}
			}
			}
		} // for
		
		// commit
		commitSite(site);
		
		site = refreshSiteObject(site);

		// check the status of external lti tools
		// 1. any lti tool to remove?
		HashMap<String, Map<String, Object>> ltiTools = state.getAttribute(STATE_LTITOOL_SELECTED_LIST) != null?(HashMap<String, Map<String, Object>>) state.getAttribute(STATE_LTITOOL_SELECTED_LIST):null;
		Map<String, Map<String, Object>> oldLtiTools = state.getAttribute(STATE_LTITOOL_EXISTING_SELECTED_LIST) != null? (Map<String, Map<String, Object>>) state.getAttribute(STATE_LTITOOL_EXISTING_SELECTED_LIST) : null;;
		if (oldLtiTools != null)
		{
			// get all the old enalbed lti tools
			for(String oldLtiToolsId : oldLtiTools.keySet())
			{
				if (ltiTools == null || !ltiTools.containsKey(oldLtiToolsId))
				{
					// the tool is not selectd now. Remove it from site
					Map<String, Object> oldLtiToolValues = oldLtiTools.get(oldLtiToolsId);
					Long contentKey = Long.valueOf(oldLtiToolValues.get("contentKey").toString());
					m_ltiService.deleteContent(contentKey);
					// refresh the site object
					site = refreshSiteObject(site);
				}
			}
		}

		// 2. any lti tool to add?
		if (ltiTools != null)
		{
			// then looking for any lti tool to add
			for (Map.Entry<String, Map<String, Object>> ltiTool : ltiTools.entrySet()) {
				String ltiToolId = ltiTool.getKey();
				if (!oldLtiTools.containsKey(ltiToolId))
				{
					Map<String, Object> toolValues = ltiTool.getValue();
					Properties reqProperties = (Properties) toolValues.get("reqProperties");
					if (reqProperties==null) {
						reqProperties = new Properties();
					}
					Object retval = m_ltiService.insertToolContent(null, ltiToolId, reqProperties, site.getId());
					if (retval instanceof String)
					{
						// error inserting tool content
						addAlert(state, (String) retval);
						break;
					}
					else
					{
						// success inserting tool content
						String pageTitle = reqProperties.getProperty("pagetitle");
						retval = m_ltiService.insertToolSiteLink(((Long) retval).toString(), pageTitle, site.getId());
						if (retval instanceof String)
						{
							addAlert(state, ((String) retval).substring(2));
							break;
						}
					}
				}
				// refresh the site object
				site = refreshSiteObject(site);
			}
		} // if


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
						if (isHomePage(page))
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
				String[] toolIds = { TOOL_ID_SITEINFO };
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
		
		site = refreshSiteObject(site);

		// import
		importToolIntoSite(chosenList, importTools, site);
		
		// SAK-22384 add LaTeX (MathJax) support
		if (MathJaxEnabler.prepareMathJaxToolSettingsForSave(site, state))
		{
			commitSite(site);
		}
		
	} // saveFeatures

	/**
	 * refresh site object
	 * @param site
	 * @return
	 */
	private Site refreshSiteObject(Site site) {
		// refresh the site object
        try
        {
        	site = SiteService.getSite(site.getId());
        }
        catch (Exception e)
        {
        	// error getting site after tool modification
        	M_log.warn(this + " - cannot get site " + site.getId() + " after inserting lti tools");
        }
		return site;
	}

	/**
	 * Save configuration values for multiple tool instances
	 */
	private void saveMultipleToolConfiguration(SessionState state, ToolConfiguration tool, String toolId) {
		// get the configuration of multiple tool instance
		HashMap<String, HashMap<String, String>> multipleToolConfiguration = state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION) != null?(HashMap<String, HashMap<String, String>>) state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION):new HashMap<String, HashMap<String, String>>();
		
		// set tool attributes
		HashMap<String, String> attributes = multipleToolConfiguration.get(toolId);
		
		if (attributes != null)
		{
			for(Map.Entry<String, String> attributeEntry : attributes.entrySet())
			{
				String attribute = attributeEntry.getKey();
				String attributeValue = attributeEntry.getValue();
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
		Tool tool = ToolManager.getTool(toolId);
		Set<Tool> tools = ToolManager.findTools(Collections.emptySet(), null);
		boolean result =  tool != null && tools.contains(tool);
		return result;

	}


	/**
	 * Is the siteType listed in the tool properties list of categories?
	 * @param siteType
	 * @param tool
	 * @return
	 * SAK 23808
	 */
	private boolean isSiteTypeInToolCategory(String siteType, Tool tool) {
		Set<Tool> tools = ToolManager.findTools(Collections.emptySet(), null);
		Set<String> categories = tool.getCategories();
		Iterator<String> iterator = categories.iterator();
		while(iterator.hasNext()) {
			String nextCat = iterator.next();
			if(nextCat.equals(siteType)) {
				return true;
			}
		}
		return false;
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

		// related to LTI Tool selection
		Map<String, Map<String, Object>> existingLtiIds = state.getAttribute(STATE_LTITOOL_EXISTING_SELECTED_LIST) != null? (Map<String, Map<String, Object>>) state.getAttribute(STATE_LTITOOL_EXISTING_SELECTED_LIST):null;
		HashMap<String, Map<String, Object>> ltiTools = (HashMap<String, Map<String, Object>>) state.getAttribute(STATE_LTITOOL_LIST);
		HashMap<String, Map<String, Object>> ltiSelectedTools = new HashMap<String, Map<String, Object>> ();
		
		boolean goToToolConfigPage = false;
		boolean homeSelected = false;
		// lti tool selection
		boolean ltiToolSelected = false;

		// Add new pages and tools, if any
		if (params.getStrings("selectedTools") == null && params.getStrings("selectedLtiTools") == null) {
			addAlert(state, rb.getString("atleastonetool"));
		} else {
			List l = new ArrayList(Arrays.asList(params
					.getStrings("selectedTools"))); // toolId's of chosen tools

			for (int i = 0; i < l.size(); i++) {
				String toolId = (String) l.get(i);

				if (toolId.equals(TOOL_ID_HOME)) {
					homeSelected = true;
					if (!idsSelected.contains(toolId)) 
						idsSelected.add(toolId);
				} 
				else if (toolId.startsWith(LTITOOL_ID_PREFIX))
				{
					String ltiToolId = toolId.substring(LTITOOL_ID_PREFIX.length());
					// whether there is any lti tool been selected
					if (existingLtiIds == null)
					{
						ltiToolSelected = true;
					}
					else
					{
						if (!existingLtiIds.keySet().contains(ltiToolId))
						{
							// there are some new lti tool(s) selected
							ltiToolSelected = true;
						}
					}
						
					// add tool entry to list
					ltiSelectedTools.put(ltiToolId, ltiTools.get(ltiToolId));
				}
				else
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
							{
								// reset tool title if there is a different title config setting
								String titleConfig = ServerConfigurationService.getString(CONFIG_TOOL_TITLE + originId);
								if (titleConfig != null && titleConfig.length() > 0 )
								{
									multipleToolIdTitleMap.put(toolId, titleConfig);
								}
								else
								{
									multipleToolIdTitleMap.put(toolId, ToolManager.getTool(originId).getTitle());
								}
							}
						}
					}
					else if ("sakai.mailbox".equals(toolId)) {
						// get the email alias when an Email Archive tool
						// has been selected
						String alias = getSiteAlias(mailArchiveChannelReference((String) state.getAttribute(STATE_SITE_INSTANCE_ID)));
						if (alias != null) {
							state.setAttribute(STATE_TOOL_EMAIL_ADDRESS, alias);
						}
						// go to the config page
						if (!existTools.contains(toolId))
						{
							goToToolConfigPage = true;
						}
						
					}
					if (!idsSelected.contains(toolId)) 
						idsSelected.add(toolId);
				}

			}

			state.setAttribute(STATE_TOOL_HOME_SELECTED, Boolean.valueOf(
					homeSelected));

			if (!ltiSelectedTools.isEmpty())
			{
				state.setAttribute(STATE_LTITOOL_SELECTED_LIST, ltiSelectedTools);
			}
			else
			{
				state.removeAttribute(STATE_LTITOOL_SELECTED_LIST);
			}
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
			} else if (goToToolConfigPage || ltiToolSelected) {
				state.setAttribute(STATE_MULTIPLE_TOOL_INSTANCE_SELECTED, Boolean.valueOf(goToToolConfigPage));
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

	/**
	 * Import tools and content into the site
	 * 
	 * If the isAddMissingToolsOnImportEnabled setting is true, tools that are selected for import and don't already exist in the target site will be added automatically.
	 * 
	 * @param toolIds list of tools already in the site
	 * @param importTools list of tools that were selected for import
	 * @param site the site to import content into
	 */
	private void importToolIntoSite(List toolIds, Hashtable importTools,
			Site site) {
		if (importTools != null) {
			
			//if add missing tools is enabled, add the tools ito the site before importing content
			if(isAddMissingToolsOnImportEnabled()) {
				
				//add the toolId lists into a set to remove dupes
				Set<String> toolsToProcess = new HashSet<String>(toolIds);
				toolsToProcess.addAll(importTools.keySet());
				
				//now compare what we have to what we need to add
				final List<String> selectedTools = new ArrayList<String>(toolsToProcess);
				M_log.debug("selectedTools: " + selectedTools);

				List<String> missingToolIds = new ArrayList<String>(selectedTools);
				missingToolIds.removeAll(toolIds);
				M_log.debug("missingToolIds: " + missingToolIds);

				//and add
				for(String missingToolId: missingToolIds) {
					site = addToolToSiteIfMissing(site, missingToolId);
					commitSite(site);
				}
				
				//now update toolIds to match importTools so that the content is imported
				toolIds.clear();
				toolIds.addAll(importTools.keySet());
			}
			
			Map transversalMap = new HashMap();
			
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

						Map<String,String> entityMap = transferCopyEntities(toolId, fromSiteCollectionId,
								toSiteCollectionId);
						if(entityMap != null){							 
							transversalMap.putAll(entityMap);
						}
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
						if(SITE_INFO_TOOL_ID.equals(toolId)){
								copySiteInformation(fromSiteId, site);
						}else{
							Map<String,String> entityMap = transferCopyEntities(toolId, fromSiteId, toSiteId);
							if(entityMap != null){							 
								transversalMap.putAll(entityMap);
							}
							resourcesImported = true;
						}
					}
				}
			}
			
			//update entity references
			for (int i = 0; i < toolIds.size(); i++) {
				String toolId = (String) toolIds.get(i);
				if(importTools.containsKey(toolId)){
					List importSiteIds = (List) importTools.get(toolId);
					for (int k = 0; k < importSiteIds.size(); k++) {
						String toSiteId = site.getId();
						updateEntityReferences(toolId, toSiteId, transversalMap, site);
					}
				}
			}
		}
	}

	
	private void importToolIntoSiteMigrate(List toolIds, Hashtable importTools,
			Site site) {
		
		if (importTools != null) {
			
			//if add missing tools is enabled, add the tools ito the site before importing content
			if(isAddMissingToolsOnImportEnabled()) {
				
				//add the toolId lists into a set to remove dupes
				Set<String> toolsToProcess = new HashSet<String>(toolIds);
				toolsToProcess.addAll(importTools.keySet());
				
				//now compare what we have to what we need to add
				final List<String> selectedTools = new ArrayList<String>(toolsToProcess);
				M_log.debug("selectedTools: " + selectedTools);

				List<String> missingToolIds = new ArrayList<String>(selectedTools);
				missingToolIds.removeAll(toolIds);
				M_log.debug("missingToolIds: " + missingToolIds);

				//and add
				for(String missingToolId: missingToolIds) {
					site = addToolToSiteIfMissing(site, missingToolId);
					commitSite(site);
				}
				
				//now update toolIds to match importTools so that the content is imported
				toolIds.clear();
				toolIds.addAll(importTools.keySet());
			}
			
			Map transversalMap = new HashMap();
			
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
						Map<String,String> entityMap = transferCopyEntitiesMigrate(toolId, fromSiteCollectionId,
								toSiteCollectionId);
						if(entityMap != null){							 
							transversalMap.putAll(entityMap);
						}						
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
						if(SITE_INFO_TOOL_ID.equals(toolId)){
							copySiteInformation(fromSiteId, site);
						}else{
							Map<String,String> entityMap = transferCopyEntitiesMigrate(toolId, fromSiteId, toSiteId);
							if(entityMap != null){
								transversalMap.putAll(entityMap);
							}
						}
					}
				}
			}
			
			//update entity references
			for (int i = 0; i < toolIds.size(); i++) {
				String toolId = (String) toolIds.get(i);
				if(importTools.containsKey(toolId)){
					List importSiteIds = (List) importTools.get(toolId);
					for (int k = 0; k < importSiteIds.size(); k++) {
						String toSiteId = site.getId();
						updateEntityReferences(toolId, toSiteId, transversalMap, site);
					}
				}
			}
		}
	} // importToolIntoSiteMigrate

	private void copySiteInformation(String fromSiteId, Site toSite){
		try {
			Site fromSite = SiteService.getSite(fromSiteId);
			//we must get the new site again b/c some tools (lesson builder) can make changes to the site structure (i.e. add pages).
			Site editToSite = SiteService.getSite(toSite.getId());
			editToSite.setDescription(fromSite.getDescription());
			editToSite.setInfoUrl(fromSite.getInfoUrl());
			commitSite(editToSite);
			toSite = editToSite;
		} catch (IdUnusedException e) {

		}
	}

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
	 * get the setup.request.replyTo setting. If missing, use setup.request setting.
	 * @return
	 */
	private String getSetupRequestReplyToEmailAddress() {
		String rv = ServerConfigurationService.getString("setup.request.replyTo", null);
		if (rv == null) {
			rv = getSetupRequestEmailAddress();
		}
		return rv;
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
		String id = StringUtils.trimToNull(siteInfo.getSiteId());
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
					// set site type
					site.setType(SiteTypeUtil.getTargetSiteType(templateSite.getType()));
				} else {
					site = SiteService.addSite(id, siteInfo.site_type);
				}
				
				// add current user as the maintainer
				site.addMember(UserDirectoryService.getCurrentUser().getId(), site.getMaintainRole(), true, false);

				String title = StringUtils.trimToNull(siteInfo.title);
				String description = siteInfo.description;
				setAppearance(state, site, siteInfo.iconUrl);
				site.setDescription(description);
				if (title != null) {
					site.setTitle(title);
				}

				ResourcePropertiesEdit rp = site.getPropertiesEdit();
				
				/// site language information
							
				String locale_string = (String) state.getAttribute("locale_string");							
								
				rp.addProperty(PROP_SITE_LANGUAGE, locale_string);
															
				site.setShortDescription(siteInfo.short_description);
				site.setPubView(siteInfo.include);
				site.setJoinable(siteInfo.joinable);
				site.setJoinerRole(siteInfo.joinerRole);
				site.setPublished(siteInfo.published);
				// site contact information
				rp.addProperty(Site.PROP_SITE_CONTACT_NAME,
						siteInfo.site_contact_name);
				rp.addProperty(Site.PROP_SITE_CONTACT_EMAIL,
						siteInfo.site_contact_email);
				
				// SAK-22790 add props from SiteInfo object
				rp.addAll(siteInfo.getProperties());
				
				// SAK-23491 add template_used property
				if (templateSite != null) {
					// if the site was created from template
					rp.addProperty(TEMPLATE_USED, templateSite.getId());
				}

				// bjones86 - SAK-24423 - update site properties for joinable site settings
				JoinableSiteSettings.updateSitePropertiesFromSiteInfoOnAddNewSite( siteInfo, rp );

				state.setAttribute(STATE_SITE_INSTANCE_ID, site.getId());

				// commit newly added site in order to enable related realm
				commitSite(site);
				
			} catch (IdUsedException e) {
				addAlert(state, rb.getFormattedMessage("java.sitewithid.exists", new Object[]{id}));
				M_log.error(this + ".addNewSite: " + rb.getFormattedMessage("java.sitewithid.exists", new Object[]{id}), e);
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("templateIndex"));
				return;
			} catch (IdInvalidException e) {
				addAlert(state, rb.getFormattedMessage("java.sitewithid.notvalid", new Object[]{id}));
				M_log.error(this + ".addNewSite: " + rb.getFormattedMessage("java.sitewithid.notvalid", new Object[]{id}), e);
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("templateIndex"));
				return;
			} catch (PermissionException e) {
				addAlert(state, rb.getFormattedMessage("java.permission", new Object[]{id}));
				M_log.error(this + ".addNewSite: " + rb.getFormattedMessage("java.permission", new Object[]{id}), e);
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("templateIndex"));
				return;
			}
		}
	} // addNewSite
	
	/**
	 * created based on setTermListForContext - Denny
	 * @param context
	 * @param state
	 */
	private void setTemplateListForContext(Context context, SessionState state)
	{
		List<Site> templateSites = new ArrayList<Site>();
	
		boolean allowedForTemplateSites = true;
		
		// system wide setting for disable site creation based on template sites
		if (ServerConfigurationService.getString("wsetup.enableSiteTemplate", "true").equalsIgnoreCase(Boolean.FALSE.toString()))
		{
			allowedForTemplateSites = false;
		}
		else
		{
			if (ServerConfigurationService.getStrings("wsetup.enableSiteTemplate.userType") != null) {
				List<String> userTypes = new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("wsetup.enableSiteTemplate.userType")));
				if (userTypes != null && userTypes.size() > 0)
				{
					User u = UserDirectoryService.getCurrentUser();
					if (!(u != null && (SecurityService.isSuperUser() || userTypes.contains(u.getType()))))
					{
						// be an admin type user or any type of users defined in the configuration
						allowedForTemplateSites = false;
					}
				}
			}
		}
				
		if (allowedForTemplateSites)
		{
			// We're searching for template sites and these are marked by a property
			// called 'template' with a value of true
			Map templateCriteria = new HashMap(1);
			templateCriteria.put("template", "true");
			
			templateSites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ANY, null, null, templateCriteria, SortType.TITLE_ASC, null);
		}
		
		// If no templates could be found, stick an empty list in the context
		if(templateSites == null || templateSites.size() <= 0)
			templateSites = new ArrayList();

                //SAK25400 sort templates by type
                context.put("templateSites",sortTemplateSitesByType(templateSites));
		context.put("titleMaxLength", state.getAttribute(STATE_SITE_TITLE_MAX));
		
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
				ResourceProperties siteProperties = site.getProperties();
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
				siteInfo.site_type = site.getType();
				// term information
				String term = siteProperties.getProperty(Site.PROP_SITE_TERM);
				if (term != null) {
					siteInfo.term = term;
				}
				
				// bjones86 - SAK-24423 - update site info for joinable site settings
				JoinableSiteSettings.updateSiteInfoFromSiteProperties( siteProperties, siteInfo );
				
				// site contact information
				String contactName = siteProperties.getProperty(Site.PROP_SITE_CONTACT_NAME);
				String contactEmail = siteProperties.getProperty(Site.PROP_SITE_CONTACT_EMAIL);
				if (contactName == null && contactEmail == null) {
					User u = site.getCreatedBy();
					if (u != null)
					{
						String email = u.getEmail();
						if (email != null) {
							contactEmail = u.getEmail();
						}
						contactName = u.getDisplayName();
					}
				}
				if (contactName != null) {
					siteInfo.site_contact_name = contactName;
				}
				if (contactEmail != null) {
					siteInfo.site_contact_email = contactEmail;
				}
				
				state.setAttribute(FORM_SITEINFO_ALIASES, getSiteReferenceAliasIds(site));
			}
			
			siteInfo.additional = "";
			state.setAttribute(STATE_SITE_TYPE, siteInfo.site_type);
			state.setAttribute(STATE_SITE_INFO, siteInfo);
			
			state.setAttribute(FORM_SITEINFO_URL_BASE, getSiteBaseUrl());
			
		} catch (Exception e) {
			M_log.error(this + ".sitePropertiesIntoState: " + e.getMessage(), e);
		}

	} // sitePropertiesIntoState

	/**
	 * pageMatchesPattern returns tool id if a SitePage matches a WorkSite Setuppattern
	 * otherwise return null
	 * @param state
	 * @param page
	 * @return
	 */
	private List<String> pageMatchesPattern(SessionState state, SitePage page) {
		List<String> rv = new Vector<String>();
		
		List pageToolList = page.getTools();

		// if no tools on the page, return false
		if (pageToolList == null) {
			return null;
		}

		// don't compare tool properties, which may be changed using Options
		List toolList = new Vector();
		int count = pageToolList.size();
		
		// check Home tool first
		if (isHomePage(page))
		{
			rv.add(TOOL_ID_HOME);
			rv.add(TOOL_ID_HOME);
			return rv;
		}
		
		// check whether the page has Site Info tool
		boolean foundSiteInfoTool = false;
		for (int i = 0; i < count; i++)
		{
			ToolConfiguration toolConfiguration = (ToolConfiguration) pageToolList.get(i);
			if (toolConfiguration.getToolId().equals(TOOL_ID_SITEINFO))
			{
				foundSiteInfoTool = true;
				break;
			}
		}
		if (foundSiteInfoTool)
		{
			rv.add(TOOL_ID_SITEINFO);
			rv.add(TOOL_ID_SITEINFO);
			return rv;
		}

		// Other than Home, Site Info page, no other page is allowed to have more than one tool within. Otherwise, WSetup/Site Info tool won't handle it
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

			if (pageToolList != null && pageToolList.size() != 0) {
				// if tool attributes don't match, return false
				String match = null;
				for (ListIterator i = toolList.listIterator(); i.hasNext();) {
					MyTool tool = (MyTool) i.next();
					if (toolConfiguration.getTitle() != null) {
						if (toolConfiguration.getTool() != null
								&& originalToolId(toolConfiguration.getTool().getId(), tool.getId()) != null) {
							match = tool.getId();
							rv.add(match);
							rv.add(toolConfiguration.getId());
							
						}
					}
				}
				// no tool registeration is found (tool is not editable within Site Info tool), set return value to be null
				if (match == null)
				{
					rv = null;
				}
			}
		}
		
		return rv;

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
		Map<String, Map<String, String>> multipleToolIdAttributeMap = state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION) != null? (Map<String, Map<String, String>>) state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION):new HashMap();
		
		String wSetupTool = NULL_STRING;
		String wSetupToolId = NULL_STRING;
		List wSetupPageList = new Vector();
		Site site = getStateSite(state, true);
		List pageList = site.getPages();

		// Put up tool lists filtered by category
		String type = checkNullSiteType(state, site.getType(), site.getId());
		if (type == null) {
			M_log.warn(this + ": - unknown STATE_SITE_TYPE");
		} else {
			state.setAttribute(STATE_SITE_TYPE, type);
		}
		
		// set tool registration list
		setToolRegistrationList(state, type);
		multipleToolIdAttributeMap = state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION) != null? (Map<String, Map<String, String>>) state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION):new HashMap();
		
		// for the selected tools
		boolean check_home = false;
		Vector idSelected = new Vector();
		HashMap<String, String> toolTitles = new HashMap<String, String>();
		
		List toolRegList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		// populate the tool title list
		if (toolRegList != null)
		{
			for (Object t: toolRegList) {
				toolTitles.put(((MyTool) t).getId(),((MyTool) t).getTitle());
			}
		}
		
		if (!((pageList == null) || (pageList.size() == 0))) {
			for (ListIterator i = pageList.listIterator(); i.hasNext();) {
				// reset
				wSetupTool = null;
				wSetupToolId = null;
				
				SitePage page = (SitePage) i.next();
				// collect the pages consistent with Worksite Setup patterns
				List<String> pmList = pageMatchesPattern(state, page);
				if (pmList != null)
				{
					wSetupTool = pmList.get(0);
					wSetupToolId = pmList.get(1);
				}
				if (wSetupTool != null) {
					if (isHomePage(page))
					{
						check_home = true;
						toolTitles.put("home", page.getTitle());
					}
					else 
					{
						if (isMultipleInstancesAllowed(findOriginalToolId(state, wSetupTool)))
						{
							String mId = page.getId() + wSetupTool;
							idSelected.add(mId);
							toolTitles.put(mId, page.getTitle());
							multipleToolIdTitleMap.put(mId, page.getTitle());
							
							// get the configuration for multiple instance
							HashMap<String, String> toolConfigurations = getMultiToolConfiguration(wSetupTool, page.getTool(wSetupToolId));
							multipleToolIdAttributeMap.put(mId, toolConfigurations);
							
							MyTool newTool = new MyTool();
							String titleConfig = ServerConfigurationService.getString(CONFIG_TOOL_TITLE + mId);
							if (titleConfig != null && titleConfig.length() > 0)
							{
								// check whether there is a different title setting
								newTool.title = titleConfig;
							}
							else
							{
								// use the default
								newTool.title = ToolManager.getTool(wSetupTool).getTitle();
							}
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
							toolTitles.put(wSetupTool, page.getTitle());
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
		state.setAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION, multipleToolIdAttributeMap);
		state.setAttribute(STATE_TOOL_HOME_SELECTED, Boolean.valueOf(check_home));
		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, idSelected); // List
		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, toolRegList);
		state.setAttribute(STATE_TOOL_REGISTRATION_TITLE_LIST, toolTitles);

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
	private String checkNullSiteType(SessionState state, String type, String siteId) {
		if (type == null) {
			if (siteId != null && SiteService.isUserSite(siteId)) {
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
		state.removeAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP);
		state.removeAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION);
		state.removeAttribute(STATE_TOOL_REGISTRATION_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_TITLE_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		MathJaxEnabler.removeMathJaxToolsAttributeFromState(state);  // SAK-22384
	}

	private List orderToolIds(SessionState state, String type, List<String> toolIdList, boolean synoptic) {
		List rv = new Vector();

		// look for null site type
		if (type == null && state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null)
		{
			type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);
		}
		if (type != null && toolIdList != null) {
			List<String> orderedToolIds = ServerConfigurationService.getToolOrder(SiteTypeUtil.getTargetSiteType(type)); // UMICH-1035  
			for (String tool_id : orderedToolIds) {
				for (String toolId : toolIdList) {
					String rToolId = originalToolId(toolId, tool_id);
					if (rToolId != null)
					{
						rv.add(toolId);
						break;
					}
					else
					{
						List<String> parentToolList = (List<String>) SYNOPTIC_TOOL_ID_MAP.get(toolId);
						if (parentToolList != null && parentToolList.contains(tool_id))
						{
							rv.add(toolId);
							break;
						}
					}
				}
			}
		}
		
		// add those toolids without specified order
		if (toolIdList != null)
		{
			for (String toolId : toolIdList) {
				if (!rv.contains(toolId)) {
					rv.add(toolId);
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

		String[] iconNames = {"*default*"};
		String[] iconUrls = {""};
		String[] iconSkins = {""};

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
				MyIcon s = new MyIcon(StringUtils.trimToNull((String) iconNames[i]),
						StringUtils.trimToNull((String) iconUrls[i]), StringUtils.trimToNull((String) iconSkins[i]));
				icons.add(s);
			}
		}

		state.setAttribute(STATE_ICONS, icons);
	}

	private void setAppearance(SessionState state, Site edit, String iconUrl) {
		// set the icon
		iconUrl = StringUtils.trimToNull(iconUrl);
		
		//SAK-18721 convert spaces in URL to %20
		iconUrl = StringUtils.replace(iconUrl, " ", "%20");
		
		edit.setIconUrl(iconUrl);

		// if this icon is in the config appearance list, find a skin to set
		List icons = (List) state.getAttribute(STATE_ICONS);
		for (Iterator i = icons.iterator(); i.hasNext();) {
			Object icon = (Object) i.next();
			if (icon instanceof MyIcon && StringUtils.equals(((MyIcon) icon).getUrl(), iconUrl)) {
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
		}else if (option.startsWith("remove_")) {
			// this could be format of originalToolId plus number of multiplication
			String removeToolId = option.substring("remove_".length(), option.length());

			// find the original tool id
			String originToolId = findOriginalToolId(state, removeToolId);
			if (originToolId != null)
			{
				Tool tool = ToolManager.getTool(originToolId);
				if (tool != null)
				{
					updateSelectedToolList(state, params, false);
					removeTool(state, removeToolId, originToolId);
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
		} else if (option.equalsIgnoreCase("continueENW")) {
			// continue in multiple tools page
			updateSelectedToolList(state, params, false);
			doContinue(data);
		} else if (option.equalsIgnoreCase("continue")) {
			// continue
			MathJaxEnabler.applyToolSettingsToState(state, site, params);  // SAK-22384

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
		if (params.getStrings("selectedTools") != null)
		{
			// read entries for multiple tool customization
			List selectedTools = new ArrayList(Arrays.asList(params.getStrings("selectedTools")));
	
			HashMap<String, String> toolTitles = state.getAttribute(STATE_TOOL_REGISTRATION_TITLE_LIST) != null ? (HashMap<String, String>) state.getAttribute(STATE_TOOL_REGISTRATION_TITLE_LIST) : new HashMap<String, String>();
			Set multipleToolIdSet = (Set) state.getAttribute(STATE_MULTIPLE_TOOL_ID_SET);
			Map multipleToolIdTitleMap = state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP) != null? (Map) state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP):new HashMap();
			HashMap<String, HashMap<String, String>> multipleToolConfiguration = state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION) != null?(HashMap<String, HashMap<String, String>>) state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION):new HashMap<String, HashMap<String, String>>();
			Vector<String> idSelected = (Vector<String>) state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
			boolean has_home = false;
			String emailId = state.getAttribute(STATE_TOOL_EMAIL_ADDRESS) != null?(String) state.getAttribute(STATE_TOOL_EMAIL_ADDRESS):null;
	
			for (int i = 0; i < selectedTools.size(); i++) 
			{
				String id = (String) selectedTools.get(i);
				if (id.equalsIgnoreCase(TOOL_ID_HOME)) {
					has_home = true;
				} else if (id.equalsIgnoreCase("sakai.mailbox")) {
					// read email id
					emailId = StringUtils.trimToNull(params.getString("emailId"));
					state.setAttribute(STATE_TOOL_EMAIL_ADDRESS, emailId);
					if ( updateConfigVariables ) {
						// if Email archive tool is selected, check the email alias
						String siteId = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);
						String channelReference = mailArchiveChannelReference(siteId);
						if (emailId == null) {
							addAlert(state, rb.getString("java.emailarchive") + " ");
						} else {
							if (!Validator.checkEmailLocal(emailId)) {
								addAlert(state, rb.getString("java.theemail"));
							} else if (!aliasService.allowSetAlias(emailId, channelReference )) {
								addAlert(state, rb.getString("java.addalias"));
							} else {
								// check to see whether the alias has been used by
								// other sites
								try {
									String target = aliasService.getTarget(emailId);
									if (target != null) {
										if (siteId != null) {
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
				else if (isMultipleInstancesAllowed(findOriginalToolId(state, id)) && (idSelected != null && !idSelected.contains(id) || idSelected == null))
				{
					// newly added mutliple instances
					String title = StringUtils.trimToNull(params.getString("title_" + id));
					if (title != null) 
					{
						// truncate the title to maxlength as defined
						if (title.length() > MAX_TOOL_TITLE_LENGTH)
						{
							title = title.substring(0, MAX_TOOL_TITLE_LENGTH);
						}
						
						// save the titles entered
						multipleToolIdTitleMap.put(id, title);
					}
					toolTitles.put(id, title);
					
					// get the attribute input
					HashMap<String, String> attributes = multipleToolConfiguration.get(id);
					if (attributes == null)
					{
						// if missing, get the default setting for original id
						attributes = multipleToolConfiguration.get(findOriginalToolId(state, id));
					}
					
					if (attributes != null)
					{
						for(Iterator<String> e = attributes.keySet().iterator(); e.hasNext();)
						{
							String attribute = e.next();
							String attributeInput = StringUtils.trimToNull(params.getString(attribute + "_" + id));
							if (attributeInput != null)
							{
								// save the attribute input if valid, otherwise generate alert
								if ( FormattedText.validateURL(attributeInput) )
									attributes.put(attribute, attributeInput);
								else
									addAlert(state, rb.getString("java.invurl"));
							}
						}
						multipleToolConfiguration.put(id, attributes);
					}
				}
			}
			
			// update the state objects
			state.setAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, multipleToolIdTitleMap);
			state.setAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION, multipleToolConfiguration);
			state.setAttribute(STATE_TOOL_HOME_SELECTED, Boolean.valueOf(has_home));
			state.setAttribute(STATE_TOOL_REGISTRATION_TITLE_LIST, toolTitles);
		}
		
		// read in the input for external tool list
		updateSelectedExternalToolList(state, params);
		
	} // updateSelectedToolList

	/**
	 * read in the input for external tool list
	 * @param state
	 * @param params
	 */
	private void updateSelectedExternalToolList(SessionState state,
			ParameterParser params) {
		// update the lti tool list
		if (state.getAttribute(STATE_LTITOOL_SELECTED_LIST) != null)
		{
			Properties reqProps = params.getProperties();
			// remember the reqProps may contain multiple lti inputs, so we need to differentiate those inputs and store one tool specific input into the map
			HashMap<String, Map<String, Object>> ltiTools = (HashMap<String, Map<String, Object>>) state.getAttribute(STATE_LTITOOL_SELECTED_LIST);
			for ( Map.Entry<String,Map<String, Object>> ltiToolEntry : ltiTools.entrySet())
			{
				String ltiToolId = ltiToolEntry.getKey();
				Map<String, Object> ltiToolAttributes = ltiToolEntry.getValue();
				String[] contentToolModel=m_ltiService.getContentModel(Long.valueOf(ltiToolId));
				Properties reqForCurrentTool = new Properties();
				// the input page contains attributes prefixed with lti tool id, need to look for those attribute inut values
				for (int k=0; k< contentToolModel.length;k++)
				{
					// sample format of contentToolModel[k]: 
					// title:text:label=bl_content_title:required=true:maxlength=255
					String contentToolModelAttribute = contentToolModel[k].substring(0, contentToolModel[k].indexOf(":"));
					String k_contentToolModelAttribute = ltiToolId + "_" + contentToolModelAttribute;
					if (reqProps.containsKey(k_contentToolModelAttribute))
					{
						reqForCurrentTool.put(contentToolModelAttribute, reqProps.get(k_contentToolModelAttribute));
					}
				}
				// add the tool id field
				reqForCurrentTool.put(LTIService.LTI_TOOL_ID, ltiToolId);
				ltiToolAttributes.put("reqProperties", reqForCurrentTool);
				// update the lti tool list
				ltiTools.put(ltiToolId, ltiToolAttributes);
			}
			state.setAttribute(STATE_LTITOOL_SELECTED_LIST, ltiTools);
		}
	}

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
		HashMap<String, String> toolTitles = state.getAttribute(STATE_TOOL_REGISTRATION_TITLE_LIST) != null ? (HashMap<String, String>) state.getAttribute(STATE_TOOL_REGISTRATION_TITLE_LIST) : new HashMap<String, String>();
		
		List oTools = state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST) == null? new Vector():(List) state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		
		// get the map of titles of multiple tool instances
		Map multipleToolIdTitleMap = state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP) != null? (Map) state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP):new HashMap();
		// get the attributes of multiple tool instances
		HashMap<String, HashMap<String, String>> multipleToolConfiguration = state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION) != null?(HashMap<String, HashMap<String, String>>) state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION):new HashMap<String, HashMap<String, String>>();
		
		int toolListedTimes = 0;
		
		// get the proper insert index for the whole tool list
		int index = 0;
		int insertIndex = 0;
		while (index < toolList.size()) {
			MyTool tListed = (MyTool) toolList.get(index);
			if (tListed.getId().indexOf(toolId) != -1 && !oTools.contains(tListed.getId())) {
				toolListedTimes++;
				// update the insert index
				insertIndex = index+1;
			}

			index++;
		}
		
		// get the proper insert index for the selected tool list
		List toolSelected = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		index = 0;
		int insertSelectedToolIndex = 0;
		while (index < toolSelected.size()) {
			String selectedId = (String) toolSelected.get(index);
			if (selectedId.indexOf(toolId) != -1 ) {
				// update the insert index
				insertSelectedToolIndex = index+1;
			}

			index++;
		}

		// insert multiple tools
		for (int i = 0; i < insertTimes; i++) {
			toolSelected.add(insertSelectedToolIndex, toolId + toolListedTimes);

			// We need to insert a specific tool entry only if all the specific
			// tool entries have been selected
			String newToolId = toolId + toolListedTimes;
			MyTool newTool = new MyTool();
			String titleConfig = ServerConfigurationService.getString(CONFIG_TOOL_TITLE + toolId);
			if (titleConfig != null && titleConfig.length() > 0)
			{
				// check whether there is a different title setting
				defaultTitle = titleConfig;
			}
			newTool.title = defaultTitle;
			newTool.id = newToolId;
			newTool.description = defaultDescription;
			toolList.add(insertIndex, newTool);
			toolListedTimes++;
			
			// add title
			multipleToolIdTitleMap.put(newToolId, defaultTitle);
			toolTitles.put(newToolId, defaultTitle);
			
			// get the attribute input
			HashMap<String, String> attributes = multipleToolConfiguration.get(newToolId);
			if (attributes == null)
			{
				// if missing, get the default setting for original id
				attributes = getMultiToolConfiguration(toolId, null);
				multipleToolConfiguration.put(newToolId, attributes);
			}
		}

		state.setAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, multipleToolIdTitleMap);
		state.setAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION, multipleToolConfiguration);
		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, toolList);
		state.setAttribute(STATE_TOOL_REGISTRATION_TITLE_LIST, toolTitles);
		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, toolSelected);

	} // insertTool
	
	/**
	 * find the tool in the tool list and remove the tool instance
	 * @param state
	 * @param toolId
	 * @param originalToolId
	 */
	private void removeTool(SessionState state, String toolId, String originalToolId) {
		List toolList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		// get the map of titles of multiple tool instances
		Map multipleToolIdTitleMap = state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP) != null? (Map) state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP):new HashMap();
		// get the attributes of multiple tool instances
		HashMap<String, HashMap<String, String>> multipleToolConfiguration = state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION) != null?(HashMap<String, HashMap<String, String>>) state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION):new HashMap<String, HashMap<String, String>>();
		// the selected tool list
		List toolSelected = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);

		// remove the tool from related state variables
		toolSelected.remove(toolId);
		// remove the tool from the title map
		multipleToolIdTitleMap.remove(toolId);
		// remove the tool from the configuration map
		boolean found = false;
		for (ListIterator i = toolList.listIterator(); i.hasNext() && !found;) 
		{
			MyTool tool = (MyTool) i.next();
			if (tool.getId().equals(toolId)) 
			{
				toolList.remove(tool);
				found = true;
			}
		}
		multipleToolConfiguration.remove(toolId);

		state.setAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, multipleToolIdTitleMap);
		state.setAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION, multipleToolConfiguration);
		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, toolList);
		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, toolSelected);

	} // removeTool

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
		state.setAttribute(STATE_SELECTED_PARTICIPANTS, selectedParticipantList);

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
		
		public boolean multiple = false;

		public String group = NULL_STRING;

		public String moreInfo = NULL_STRING;

		public HashMap<String,MyTool> multiples = new HashMap<String,MyTool>();

		public boolean required = false;

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

		public boolean isRequired() {
			return required;
		}

		public boolean hasMultiples() {
			return multiple;
		}

		public String getGroup() {
			return group;
		}

		public String getMoreInfo() {
			return moreInfo;
		}

		// SAK-16600
		public HashMap<String,MyTool> getMultiples(String toolId) {
			if (multiples == null) {
				return new HashMap<String,MyTool>();
			} else {
				return multiples;
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MyTool other = (MyTool) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

		private SiteAction getOuterType() {
			return SiteAction.this;
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
		
		public Set<String> additionalRoles = Collections.EMPTY_SET;

		public String joinerRole = NULL_STRING;

		public String title = NULL_STRING; // the short name of the site
		
		public Set<String> siteRefAliases = new HashSet<String>(); // the aliases for the site itself

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
		
		public String term = NULL_STRING; // academic term
		
		public ResourceProperties properties = new BaseResourcePropertiesEdit();

		// bjones86 - SAK-24423 - joinable site settings
		public String joinerGroup = NULL_STRING;
		public String getJoinerGroup()
		{ 
			return joinerGroup;
		}
		
		public boolean joinExcludePublic = false;
		public boolean getJoinExcludePublic()
		{ 
			return joinExcludePublic;
		}
		
		public boolean joinLimitByAccountType = false;
		public boolean getJoinLimitByAccountType()
		{ 
			return joinLimitByAccountType;
		}
		
		public String joinLimitedAccountTypes = NULL_STRING;
		public String getJoinLimitedAccountTypes()
		{ 
			return joinLimitedAccountTypes;
		} // end joinable site settings

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
		
		public String getFirstAlias() {
			return siteRefAliases.isEmpty() ? NULL_STRING : siteRefAliases.iterator().next();
		}
		
		public void addProperty(String key, String value) {
			properties.addProperty(key, value);
		}
		
		public ResourceProperties getProperties() {
			return properties;
		}
		

		public Set<String> getSiteRefAliases() {
			return siteRefAliases;
		}

		public void setSiteRefAliases(Set<String> siteRefAliases) {
			this.siteRefAliases = siteRefAliases;
		}
		
		public String getTerm() {
			return term;
		}
		
		public void setTerm(String term) {
			this.term = term;
		}		

	} // SiteInfo

	public class AdditionalRoleGroup implements Comparable<AdditionalRoleGroup>{
		public String name;
		public List<AdditionalRole> roles;
		
		public AdditionalRoleGroup(String name) {
			this.name = name;
			this.roles = new ArrayList<AdditionalRole>();
		}
		public String getName() {
			return name;
		}
		public List<AdditionalRole> getRoles() {
			return roles;
		}
		
		public int getSize() {
			return roles.size();
		}
		
		@Override
		public int compareTo(AdditionalRoleGroup arg0) {
			if(arg0 == null) return 1;
			if(this.name == null && arg0.name == null) return 0;
			if(this.name == null || arg0.name == null) return (this.name == null) ? -1 : 1;
			return this.name.compareTo(arg0.name);
		}
	}

	public class AdditionalRole implements Comparable<AdditionalRole>{
		public String id;
		public String name;
		public boolean editable;
		public boolean granted;
		
		public String getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		public boolean isEditable() {
			return editable;
		}
		public boolean isGranted() {
			return granted;
		}
		
		@Override
		public int compareTo(AdditionalRole arg0) {
			if(arg0 == null) return 1;
			if(this.name == null && arg0.name == null) return 0;
			if(this.name == null || arg0.name == null) return (this.name == null) ? -1 : 1;
			return this.name.compareTo(arg0.name);
		}
	}

	// customized type tool related
	/**
	 * doFinish_site_type_tools is called when creation of a customized type site is
	 * confirmed
	 */
	public void doFinish_site_type_tools(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		// set up for the coming template
		state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("continue"));
		int index = Integer.valueOf(params.getString("templateIndex"))
				.intValue();
		actionForTemplate("continue", index, params, state, data);

		// add the pre-configured site type tools to a new site
		addSiteTypeFeatures(state);

		// TODO: hard coding this frame id is fragile, portal dependent, and
		// needs to be fixed -ggolden
		// schedulePeerFrameRefresh("sitenav");
		scheduleTopRefresh();

		resetPaging(state);

	}// doFinish_site-type_tools

	/**
	 * addSiteTypeToolsFeatures adds features to a new customized type site
	 * 
	 */
	private void addSiteTypeFeatures(SessionState state) {
		Site edit = null;
		Site template = null;
		String type = (String) state.getAttribute(STATE_SITE_TYPE);
		HashMap<String, String> templates = siteTypeProvider.getTemplateForSiteTypes();
		// get the template site id for this site type
		if (templates != null && templates.containsKey(type))
		{
			String templateId = templates.get(type);
			
			// get a unique id
			String id = IdManager.createUuid();
			
	
			// get the site template
			try {
				template = SiteService.getSite(templateId);
			} catch (Exception e) {
				M_log.error(this + ".addSiteTypeFeatures:" + e.getMessage() + templateId, e);
			}
			if (template != null) {
				// create a new site based on the template
				try {
					edit = SiteService.addSite(id, template);
					// set site type
					edit.setType(SiteTypeUtil.getTargetSiteType(template.getType()));
				} catch (Exception e) {
					M_log.error(this + ".addSiteTypeFeatures:" + " add/edit site id=" + id, e);
				}
	
				// set the tab, etc.
				if (edit != null) {
					SiteInfo siteInfo = (SiteInfo) state
							.getAttribute(STATE_SITE_INFO);
					edit.setShortDescription(siteInfo.short_description);
					edit.setTitle(siteInfo.title);
					edit.setPublished(true);
					edit.setPubView(false);
					// SAK-23491 add template_used property
					edit.getPropertiesEdit().addProperty(TEMPLATE_USED, templateId);
					
					try {
						SiteService.save(edit);
					} catch (Exception e) {
						M_log.error(this + ".addSiteTypeFeatures:" + " commitEdit site id=" + id, e);
					}
	
					// now that the site and realm exist, we can set the email alias
					// set the site alias as:
					User currentUser = UserDirectoryService.getCurrentUser();
					List<String> pList = new ArrayList<String>();
					pList.add(currentUser != null ? currentUser.getEid():"");
					String alias = siteTypeProvider.getSiteAlias(type, pList);
					String channelReference = mailArchiveChannelReference(id);
					try {
						aliasService.setAlias(alias, channelReference);
					} catch (IdUsedException ee) {
						addAlert(state, rb.getFormattedMessage("java.alias.exists", new Object[]{alias}));
						M_log.error(this + ".addSiteTypeFeatures:" + rb.getFormattedMessage("java.alias.exists", new Object[]{alias}), ee);
					} catch (IdInvalidException ee) {
						addAlert(state, rb.getFormattedMessage("java.alias.isinval", new Object[]{alias}));
						M_log.error(this + ".addSiteTypeFeatures:" + rb.getFormattedMessage("java.alias.isinval", new Object[]{alias}), ee);
					} catch (PermissionException ee) {
						addAlert(state, rb.getString("java.addalias"));
						M_log.error(this + ".addSiteTypeFeatures:" + SessionManager.getCurrentSessionUserId() + " does not have permission to add alias. ", ee);
					}
				}
			}
		}

	} // addSiteTypeFeatures

	/**
	 * handle with add site options
	 * 
	 */
	public void doAdd_site_option(RunData data) {
		String option = data.getParameters().getString("option");
		if ("finish".equals(option)) {
			SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
			Site site = getStateSite(state);
			if (site == null)
			{
				doFinish(data);
			}
			else  // There is a site in the state already, likely from a previous request that is still processing
			{
				// Abort this request to prevent adding multiple copies of tools to the same site.
				String msg = "Detected request to create a site while site %s already exists in current session state. Aborting request.";
				if (M_log.isDebugEnabled())
				{
					M_log.debug(String.format(msg, StringUtils.trimToEmpty(site.getId())));
				}
			}
		} else if ("cancel".equals(option)) {
			doCancel_create(data);
		} else if ("back".equals(option)) {
			doBack(data);
		}
	} // doAdd_site_option

	/**
	 * handle with duplicate site options
	 * 
	 */
	public void doDuplicate_site_option(RunData data) {
		String option = data.getParameters().getString("option");
		if ("duplicate".equals(option)) {
			doContinue(data);
		} else if ("cancel".equals(option)) {
			doCancel(data);
		} else if ("finish".equals(option)) {
			doContinue(data);
		}
	} // doDuplicate_site_option

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
	protected Map transferCopyEntities(String toolId, String fromContext,
			String toContext) {
		// TODO: used to offer to resources first - why? still needed? -ggolden

		Map transversalMap = new HashMap();

		// offer to all EntityProducers
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i
				.hasNext();) {
			EntityProducer ep = (EntityProducer) i.next();
			if (ep instanceof EntityTransferrer) {
				try {
					EntityTransferrer et = (EntityTransferrer) ep;

					// if this producer claims this tool id
					if (ArrayUtil.contains(et.myToolIds(), toolId)) {
						if(ep instanceof EntityTransferrerRefMigrator){
							EntityTransferrerRefMigrator etMp = (EntityTransferrerRefMigrator) ep;
							Map<String,String> entityMap = etMp.transferCopyEntitiesRefMigrator(fromContext, toContext,
									new Vector());
							if(entityMap != null){							 
								transversalMap.putAll(entityMap);
							}
						}else{
							et.transferCopyEntities(fromContext, toContext,	new Vector());
						}
					}
				} catch (Throwable t) {
					M_log.error(this + ".transferCopyEntities: Error encountered while asking EntityTransfer to transferCopyEntities from: "
									+ fromContext + " to: " + toContext, t);
				}
			}
		}
		
		// record direct URL for this tool in old and new sites, so anyone using the URL in HTML text will 
		// get a proper update for the HTML in the new site
		// Some tools can have more than one instance. Because getTools should always return tools
		// in order, we can assume that if there's more than one instance of a tool, the instances
		// correspond

		Site fromSite = null;
		Site toSite = null;
		Collection<ToolConfiguration> fromTools = null;
		Collection<ToolConfiguration> toTools = null;
		try {
		    fromSite = SiteService.getSite(fromContext);
		    toSite = SiteService.getSite(toContext);
		    fromTools = fromSite.getTools(toolId);
		    toTools = toSite.getTools(toolId);
		} catch (Exception e) {
		    M_log.warn(this + "transferCopyEntities: can't get site:" + e.getMessage());
		}

		// getTools appears to return tools in order. So we should be able to match them
		if (fromTools != null && toTools != null) {
		    Iterator<ToolConfiguration> toToolIt = toTools.iterator();
		    // step through tools in old and new site in parallel
		    // I believe the first time this is called for a given
		    // tool all instances will be copied, but stop if not
		    // all instances have been copied yet
		    for (ToolConfiguration fromTool: fromTools) {
			if (toToolIt.hasNext()) {
			    ToolConfiguration toTool = toToolIt.next();
			    String fromUrl = ServerConfigurationService.getPortalUrl() + "/directtool/" + Web.escapeUrl(fromTool.getId()) + "/";
			    String toUrl = ServerConfigurationService.getPortalUrl() + "/directtool/" + Web.escapeUrl(toTool.getId()) + "/";
			    if (transversalMap.get(fromUrl) == null)
				transversalMap.put(fromUrl, toUrl);
			    if (shortenedUrlService.shouldCopy(fromUrl)) {
				fromUrl = shortenedUrlService.shorten(fromUrl, false);
				toUrl = shortenedUrlService.shorten(toUrl, false);
				if (fromUrl != null && toUrl != null)
				    transversalMap.put(fromUrl, toUrl);
			    }
			} else
			    break;
		    }
		}

		return transversalMap;
	}

	private void updateSiteInfoToolEntityReferences(Map transversalMap, Site newSite){
		if(transversalMap != null && transversalMap.size() > 0 && newSite != null){
			Set<Entry<String, String>> entrySet = (Set<Entry<String, String>>) transversalMap.entrySet();
			
			String msgBody = newSite.getDescription();
			if(msgBody != null && !"".equals(msgBody)){
				boolean updated = false;
				Iterator<Entry<String, String>> entryItr = entrySet.iterator();
				while(entryItr.hasNext()) {
					Entry<String, String> entry = (Entry<String, String>) entryItr.next();
					String fromContextRef = entry.getKey();
					if(msgBody.contains(fromContextRef)){									
						msgBody = msgBody.replace(fromContextRef, entry.getValue());
						updated = true;
					}								
				}	
				if(updated){
					//update the site b/c some tools (Lessonbuilder) updates the site structure (add/remove pages) and we don't want to
					//over write this
					try {
						newSite = SiteService.getSite(newSite.getId());
						newSite.setDescription(msgBody);
						SiteService.save(newSite);
					} catch (IdUnusedException e) {
						// TODO:
					} catch (PermissionException e) {
						// TODO:
					}
				}
			}
		}		
	}
	
	protected void updateEntityReferences(String toolId, String toContext, Map transversalMap, Site newSite) {
		if (toolId.equalsIgnoreCase(SITE_INFORMATION_TOOL)) {
			updateSiteInfoToolEntityReferences(transversalMap, newSite);
		}else{		
			for (Iterator i = EntityManager.getEntityProducers().iterator(); i
			.hasNext();) {
				EntityProducer ep = (EntityProducer) i.next();
				if (ep instanceof EntityTransferrerRefMigrator && ep instanceof EntityTransferrer) {
					try {
						EntityTransferrer et = (EntityTransferrer) ep;
						EntityTransferrerRefMigrator etRM = (EntityTransferrerRefMigrator) ep;

						// if this producer claims this tool id
						if (ArrayUtil.contains(et.myToolIds(), toolId)) {
							etRM.updateEntityReferences(toContext, transversalMap);
						}
					} catch (Throwable t) {
						M_log.error(
								"Error encountered while asking EntityTransfer to updateEntityReferences at site: "
								+ toContext, t);
					}
				}
			}
		}
	}
	
	protected Map transferCopyEntitiesMigrate(String toolId, String fromContext,
			String toContext) {
		
		Map transversalMap = new HashMap();
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i
				.hasNext();) {
			EntityProducer ep = (EntityProducer) i.next();
			if (ep instanceof EntityTransferrer) {
				try {
					EntityTransferrer et = (EntityTransferrer) ep;

					// if this producer claims this tool id
					if (ArrayUtil.contains(et.myToolIds(), toolId)) {
						if(ep instanceof EntityTransferrerRefMigrator){
							EntityTransferrerRefMigrator etRM = (EntityTransferrerRefMigrator) ep;
							Map<String,String> entityMap = etRM.transferCopyEntitiesRefMigrator(fromContext, toContext,
									new Vector(), true);
							if(entityMap != null){							 
								transversalMap.putAll(entityMap);
							}
						}else{
							et.transferCopyEntities(fromContext, toContext,
									new Vector(), true);
						}
					}
				} catch (Throwable t) {
					M_log.error(
							"Error encountered while asking EntityTransfer to transferCopyEntities from: "
									+ fromContext + " to: " + toContext, t);
				}
			}
		}
		
		return transversalMap;
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

		if (ServerConfigurationService.getBoolean("site-manage.importoption.siteinfo", false)){
			rv.add(SITE_INFO_TOOL_ID);
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
		boolean displayLessons = false;

		Set importSites = ((Hashtable) state.getAttribute(STATE_IMPORT_SITES))
				.keySet();
		Iterator sitesIter = importSites.iterator();
		while (sitesIter.hasNext()) {
			Site site = (Site) sitesIter.next();

			// web content is a little tricky because worksite setup has the same tool id. you
			// can differentiate b/c worksite setup has a property with the key "special"
			Collection iframeTools = new ArrayList<Tool>();
			iframeTools = site.getTools(new String[] {WEB_CONTENT_TOOL_ID});
			if (iframeTools != null && iframeTools.size() > 0) {
				for (Iterator i = iframeTools.iterator(); i.hasNext();) {
					ToolConfiguration tool = (ToolConfiguration) i.next();
					if (!tool.getPlacementConfig().containsKey("special")) {
						displayWebContent = true;
					}
				}
			}

			if (site.getToolForCommonId(NEWS_TOOL_ID) != null)
				displayNews = true;
			if (site.getToolForCommonId(LESSONS_TOOL_ID) != null)
				displayLessons = true;

		}
		
		if (displayWebContent && !toolIdList.contains(WEB_CONTENT_TOOL_ID))
			toolIdList.add(WEB_CONTENT_TOOL_ID);
		if (displayNews && !toolIdList.contains(NEWS_TOOL_ID))
			toolIdList.add(NEWS_TOOL_ID);
		if (displayLessons && !toolIdList.contains(LESSONS_TOOL_ID))
			toolIdList.add(LESSONS_TOOL_ID);
		if (ServerConfigurationService.getBoolean("site-manage.importoption.siteinfo", false)){
			toolIdList.add(SITE_INFO_TOOL_ID);
		}
		
		
		return toolIdList;
	} // getToolsAvailableForImport

	// bjones86 - SAK-23256 - added userFilteringIfEnabled parameter
	private List<AcademicSession> setTermListForContext(Context context, SessionState state,
			boolean upcomingOnly, boolean useFilteringIfEnabled) {
		List<AcademicSession> terms;
		if (upcomingOnly) {
			terms = cms != null?cms.getCurrentAcademicSessions():null;
		} else { // get all
			terms = cms != null?cms.getAcademicSessions():null;
		}
		if (terms != null && terms.size() > 0) {
			
			// bjones86 - SAK-23256
			if( useFilteringIfEnabled )
			{
				if( !SecurityService.isSuperUser() )
				{
					if( ServerConfigurationService.getBoolean( SAK_PROP_FILTER_TERMS,  false ) )
					{
						terms = filterTermDropDowns();
					}
				}
			}
			
			context.put("termList", sortAcademicSessions(terms));
		}
		return terms;
	} // setTermListForContext

	private void setSelectedTermForContext(Context context, SessionState state,
			String stateAttribute) {
		if (state.getAttribute(stateAttribute) != null) {
			context.put("selectedTerm", state.getAttribute(stateAttribute));
		}
	} // setSelectedTermForContext
	
	/**
	 * Removes any academic sessions that the user is not currently enrolled in
	 * 
	 * @author bjones86 - SAK-23256
	 * 
	 * @return the filtered list of academic sessions
	 */
	public List<AcademicSession> filterTermDropDowns()
	{
		List<AcademicSession> academicSessions = new ArrayList<AcademicSession>();
		User user = UserDirectoryService.getCurrentUser();
		
		if( cms != null && user != null)
		{
			Map<String, String> sectionRoles = cms.findSectionRoles( user.getEid() );			
			if( sectionRoles != null )
			{
				// Iterate through all the sections and add their corresponding academic sessions to our return value
				Set<String> sectionEids = sectionRoles.keySet();
				Iterator<String> itr = sectionEids.iterator();
				while( itr.hasNext() )
				{
					String sectionEid = itr.next();
					Section section = cms.getSection( sectionEid );
					if( section != null )
					{
						CourseOffering courseOffering = cms.getCourseOffering( section.getCourseOfferingEid() );
						if( courseOffering != null )
						{
							academicSessions.add( courseOffering.getAcademicSession() );
						}
					}
				}
			}
		}
		
		// Remove duplicates
		for( int i = 0; i < academicSessions.size(); i++ )
		{
			for( int j = i + 1; j < academicSessions.size(); j++ )
			{
				if( academicSessions.get( i ).getEid().equals( academicSessions.get( j ).getEid() ) )
				{
					academicSessions.remove( academicSessions.get( j ) );
					j--; //stay on this index (ie. j will get incremented back)
				}
			}
		}
		
		return academicSessions;
	}

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
			M_log.warn("getCourseOfferingAndSectionMap: cannot find section " + sectionEid);
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
			azgTemplate = authzGroupService.getAuthzGroup(azgId);
		} catch (GroupNotDefinedException e) {
			M_log.error(this + ".getRolesAllowedToAttachSection: Could not find authz group " + azgId, e);
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

		Collection offeringListSorted = sortCourseOfferings(offeringList);
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

	/* SAK-25400 template site types duplicated in list
	 * Sort template sites by type   
	 **/
	private Collection sortTemplateSitesByType(Collection<Site> templates) {
		String[] sortKey = {"type"};
		String[] sortOrder = {"asc"};
		return sortCmObject(templates, sortKey, sortOrder);
	}

	/**
	 * Helper method for sortCmObject 
	 * by order from sakai properties if specified or 
	 * by default of eid, title
	 * using velocity SortTool
	 * 
	 * @param offerings
	 * @return
	 */
	private Collection sortCourseOfferings(Collection<CourseOffering> offerings) {
		// Get the keys from sakai.properties
		String[] keys = ServerConfigurationService.getStrings(SORT_KEY_COURSE_OFFERING);
		String[] orders = ServerConfigurationService.getStrings(SORT_ORDER_COURSE_OFFERING);

		return sortCmObject(offerings, keys, orders);
	} // sortCourseOffering

	/**
	 * Helper method for sortCmObject 
	 * by order from sakai properties if specified or 
	 * by default of eid, title
	 * using velocity SortTool
	 * 
	 * @param courses
	 * @return
	 */
	private Collection sortCourseSets(Collection<CourseSet> courses) {
		// Get the keys from sakai.properties
		String[] keys = ServerConfigurationService.getStrings(SORT_KEY_COURSE_SET);
		String[] orders = ServerConfigurationService.getStrings(SORT_ORDER_COURSE_SET);

		return sortCmObject(courses, keys, orders);
	} // sortCourseOffering

	/**
	 * Helper method for sortCmObject 
	 * by order from sakai properties if specified or 
	 * by default of eid, title
	 * using velocity SortTool
	 * 
	 * @param sections
	 * @return
	 */
	private Collection sortSections(Collection<Section> sections) {
		// Get the keys from sakai.properties
		String[] keys = ServerConfigurationService.getStrings(SORT_KEY_SECTION);
		String[] orders = ServerConfigurationService.getStrings(SORT_ORDER_SECTION);

		return sortCmObject(sections, keys, orders);
	} // sortCourseOffering

	/**
	 * Helper method for sortCmObject 
	 * by order from sakai properties if specified or 
	 * by default of eid, title
	 * using velocity SortTool
	 * 
	 * @param sessions
	 * @return
	 */
	private Collection sortAcademicSessions(Collection<AcademicSession> sessions) {
		// Get the keys from sakai.properties
		String[] keys = ServerConfigurationService.getStrings(SORT_KEY_SESSION);
		String[] orders = ServerConfigurationService.getStrings(SORT_ORDER_SESSION);

		return sortCmObject(sessions, keys, orders);
	} // sortCourseOffering
	
	/**
	 * Custom sort CM collections using properties provided object has getter & setter for 
	 * properties in keys and orders
	 * defaults to eid & title if none specified
	 * 
	 * @param collection a collection to be sorted
	 * @param keys properties to sort on
	 * @param orders properties on how to sort (asc, dsc)
	 * @return Collection the sorted collection
	 */
	private Collection sortCmObject(Collection collection, String[] keys, String[] orders) {
		if (collection != null && !collection.isEmpty()) {
			// Add them to a list for the SortTool (they must have the form
			// "<key:order>" in this implementation)
			List propsList = new ArrayList();
			
			if (keys == null || orders == null || keys.length == 0 || orders.length == 0) {
				// No keys are specified, so use the default sort order
				propsList.add("eid");
				propsList.add("title");
			} else {
				// Populate propsList
				for (int i = 0; i < Math.min(keys.length, orders.length); i++) {
					String key = keys[i];
					String order = orders[i];
					propsList.add(key + ":" + order);
				}
			}
			// Sort the collection and return
			SortTool sort = new SortTool();
			return sort.sort(collection, propsList);
		}
			
		return Collections.emptyList();

	} // sortCmObject

	/**
	 * Custom sort CM collections provided object has getter & setter for 
	 *  eid & title
	 * 
	 * @param collection a collection to be sorted
	 * @return Collection the sorted collection
	 */
	private Collection sortCmObject(Collection collection) {
		return sortCmObject(collection, null, null);
	}
	
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

		public List<String> authorizer;
		
		public String description;

		public SectionObject(Section section) {
			this.section = section;
			this.eid = section.getEid();
			this.title = section.getTitle();
			this.category = section.getCategory();
			List<String> authorizers = new ArrayList<String>();
			if (section.getEnrollmentSet() != null){
				Set<String> instructorset = section.getEnrollmentSet().getOfficialInstructors();
				if (instructorset != null) {
					for (String instructor:instructorset) {
						authorizers.add(instructor);
					}
				}
			}
			this.authorizer = authorizers;
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
			this.description = section.getDescription();
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
		
		public String getDescription() {
			return description;
		}
		
		public List<String> getAuthorizer() {
			return authorizer;
		}
		
		public String getAuthorizerString() {
			StringBuffer rv = new StringBuffer();
			if (authorizer != null && !authorizer.isEmpty())
			{
				for (int count = 0; count < authorizer.size(); count++)
				{
					// concatenate all authorizers into a String
					if (count > 0)
					{
						rv.append(", ");
					}
					rv.append(authorizer.get(count));
				}
			}
			return rv.toString();
		}

		public void setAuthorizer(List<String> authorizer) {
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
		
		// if list is empty, set to null. This is important 'cos null is
		// the indication that the list is empty in the code. See case 2 on line
		// 1081
		if (manualCourseList != null && manualCourseList.size() == 0)
			manualCourseList = null;
		if (providerCourseList != null && providerCourseList.size() == 0)
			providerCourseList = null;
		
		removeAnyFlaggedSectionFromState(state, params, STATE_CM_REQUESTED_SECTIONS);
		
		removeAnyFlaggedSectionFromState(state, params, STATE_CM_SELECTED_SECTIONS);
		
		removeAnyFlaggedSectionFromState(state, params, STATE_CM_AUTHORIZER_SECTIONS);
		
		// remove manually requested sections
		if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
			int number = ((Integer) state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER)).intValue();
			List requiredFields = state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS) != null ?(List) state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS):new Vector();
			List removeRequiredFieldList = null;
			for (int i = 0; i < requiredFields.size(); i++) {
				
				String sectionTitle = "";
				List requiredFieldList = (List) requiredFields.get(i);
				for (int j = 0; j < requiredFieldList.size(); j++) {
					SectionField requiredField = (SectionField) requiredFieldList.get(j);
					sectionTitle = sectionTitle.concat(requiredField.getValue() + " ");
				}
				String field = "removeSection" + sectionTitle.trim();
				String toRemove = params.getString(field);
				if ("true".equals(toRemove)) {
					removeRequiredFieldList = requiredFieldList; 
					break;
				}
			}
			
			if (removeRequiredFieldList != null)
			{
				requiredFields.remove(removeRequiredFieldList);
				if (number > 1)
				{
					state.setAttribute(STATE_MANUAL_ADD_COURSE_FIELDS, requiredFields);
					state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, Integer.valueOf(number -1));
				}
				else
				{
					state.removeAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
					state.removeAttribute(STATE_MANUAL_ADD_COURSE_NUMBER);
				}
			}
		}
	}
	
	private void removeAnyFlaggedSectionFromState(SessionState state, ParameterParser params, String state_variable)
	{
		List<SectionObject> rv = (List<SectionObject>) state.getAttribute(state_variable);
		if (rv != null) {
			for (int i = 0; i < rv.size(); i++) {
				SectionObject so = (SectionObject) rv.get(i);
		
				String field = "removeSection" + so.getEid();
				String toRemove = params.getString(field);
		
				if ("true".equals(toRemove)) {
					rv.remove(so);
				}
		
			}
		
			if (rv.size() == 0)
				state.removeAttribute(state_variable);
			else
				state.setAttribute(state_variable, rv);
}
	}

	private void collectNewSiteInfo(SessionState state,
			ParameterParser params, List providerChosenList) {
		if (state.getAttribute(STATE_MESSAGE) == null) {
			
			SiteInfo siteInfo = state.getAttribute(STATE_SITE_INFO) != null? (SiteInfo) state.getAttribute(STATE_SITE_INFO): new SiteInfo();

			// site title is the title of the 1st section selected -
			// daisyf's note
			if (providerChosenList != null && providerChosenList.size() >= 1) {
				String title = prepareTitle((List) state
						.getAttribute(STATE_PROVIDER_SECTION_LIST),
						providerChosenList);
				siteInfo.title = title;
			}
			
			if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_DESCRIPTION_CHOSEN) != null)
			{
				List<String> providerDescriptionChosenList = (List<String>) state.getAttribute(STATE_ADD_CLASS_PROVIDER_DESCRIPTION_CHOSEN);
				if (providerDescriptionChosenList != null)
				{
					for (String providerSectionId : providerDescriptionChosenList)
					{
						try
						{
							Section s = cms.getSection(providerSectionId);
							if (s != null)
							{
								String sDescription = StringUtils.trimToNull(s.getDescription());
								if (sDescription != null && !siteInfo.description.contains(sDescription))
								{
									siteInfo.description = siteInfo.description.concat(sDescription);
								}
							}
						}
						catch (IdNotFoundException e)
						{
							M_log.warn("collectNewSiteInfo: cannot find section " + providerSectionId);
						}
					}
				}
				
			}
			state.setAttribute(STATE_SITE_INFO, siteInfo);

			if (params.getString("manualAdds") != null
					&& ("true").equals(params.getString("manualAdds"))) {
				// if creating a new site
				state.setAttribute(STATE_TEMPLATE_INDEX, "37");

				state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, Integer.valueOf(
						1));

			} else if (params.getString("find_course") != null
					&& ("true").equals(params.getString("find_course"))) {
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
					state.setAttribute(STATE_TEMPLATE_INDEX, "13");
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
			try
			{
				Set sections = cms.getSections(offeringEid);
				if (sections != null)
				{
					Collection c = sortSections(new ArrayList(sections));
					return (List) c;
				}
			}
			catch (IdNotFoundException e)
			{
				M_log.warn("getCMSections: Cannot find sections for " + offeringEid);
			}
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
			if (offerings != null)
			{
				Iterator coIt = offerings.iterator();
	
				while (coIt.hasNext()) {
					CourseOffering co = (CourseOffering) coIt.next();
					AcademicSession as = co.getAcademicSession();
					if (as != null && as.getEid().equals(termID))
						returnList.add(co);
				}
			}
			Collection c = sortCourseOfferings(returnList);

			return (List) c;
		}

		return new ArrayList(0);
	}

	private List<String> getCMLevelLabels(SessionState state) {
		List<String> rv = new Vector<String>();

		// get CourseSet
		Set courseSets = getCourseSet(state);
		String currentLevel = "";
		if (courseSets != null)
		{
			// Hieriarchy of CourseSet, CourseOffering and Section are multiple levels in CourseManagementService
			List<SectionField> sectionFields = sectionFieldProvider.getRequiredFields();
			for (SectionField field : sectionFields)
			{
				rv.add(field.getLabelKey());
			}
		}
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
		// check the configuration setting for choosing next screen
		Boolean skipCourseSectionSelection = ServerConfigurationService.getBoolean(SAK_PROP_SKIP_COURSE_SECTION_SELECTION, Boolean.FALSE);
		if (!skipCourseSectionSelection.booleanValue())
		{
			// go to the course/section selection page
			state.setAttribute(STATE_TEMPLATE_INDEX, "53");
			
			// get cm levels
			final List cmLevels = getCMLevelLabels(state), selections = (List) state.getAttribute(STATE_CM_LEVEL_SELECTIONS);
			int lvlSz = 0;
		
			if (cmLevels == null || (lvlSz = cmLevels.size()) < 1) {
				// TODO: no cm levels configured, redirect to manual add
				return;
			}
		
			if (selections != null && selections.size() >= lvlSz) {
				// multiple selections for the section level
				List<SectionObject> soList = new Vector<SectionObject>();
				for (int k = cmLevels.size() -1; k < selections.size(); k++)
				{
					String string = (String) selections.get(k);
					if (string != null && string.length() > 0)
					{
						try
						{
							Section sect = cms.getSection(string);
							if (sect != null)
							{
								SectionObject so = new SectionObject(sect);
								soList.add(so);
							}
						}
						catch (IdNotFoundException e)
						{
							M_log.warn("prepFindPage: Cannot find section " + string);
						}
					}
				}
				
				state.setAttribute(STATE_CM_SELECTED_SECTION, soList);
			} else
				state.removeAttribute(STATE_CM_SELECTED_SECTION);
		
			state.setAttribute(STATE_CM_LEVELS, cmLevels);
			state.setAttribute(STATE_CM_LEVEL_SELECTIONS, selections);
		}
		else
		{
			// skip the course/section selection page, go directly into the manually create course page
			state.setAttribute(STATE_TEMPLATE_INDEX, "37");
		}
	}

	private void addRequestedSection(SessionState state) {
		List<SectionObject> soList = (List<SectionObject>) state
				.getAttribute(STATE_CM_SELECTED_SECTION);
		String uniqueName = (String) state
				.getAttribute(STATE_SITE_QUEST_UNIQNAME);

		if (soList == null || soList.isEmpty())
			return;
		String s = ServerConfigurationService.getString("officialAccountName");
		
		if (uniqueName == null) 
		{
			addAlert(state, rb.getFormattedMessage("java.author", new Object[]{ServerConfigurationService.getString("officialAccountName")}));
			return;
		} 
		
		if (getStateSite(state) == null)
		{
			// creating new site
			List<SectionObject> requestedSections = (List<SectionObject>) state.getAttribute(STATE_CM_REQUESTED_SECTIONS);
		
			for (SectionObject so : soList)
			{
				so.setAuthorizer(new ArrayList(Arrays.asList(uniqueName.split(","))));
		
				if (requestedSections == null) {
					requestedSections = new ArrayList<SectionObject>();
				}
		
				// don't add duplicates
				if (!requestedSections.contains(so))
					requestedSections.add(so);
			}
	
			state.setAttribute(STATE_CM_REQUESTED_SECTIONS, requestedSections);
			state.removeAttribute(STATE_CM_SELECTED_SECTION);
			
			// if the title has not yet been set and there is just
			// one section, set the title to that section's EID
			SiteInfo siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			if (siteInfo == null) {
				siteInfo = new SiteInfo();
			}
			if (siteInfo.title == null || siteInfo.title.trim().length() == 0) {
				if (requestedSections.size() >= 1) {
					siteInfo.title = requestedSections.get(0).getTitle();
					state.setAttribute(STATE_SITE_INFO, siteInfo);
				}
			}
		}
		else
		{
			// editing site		
			for (SectionObject so : soList)
			{
				so.setAuthorizer(new ArrayList(Arrays.asList(uniqueName.split(","))));
			
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
		}
		state.removeAttribute(STATE_CM_LEVEL_SELECTIONS);
	}

	public void doFind_course(RunData data) {
		final SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		final ParameterParser params = data.getParameters();
		final String option = params.get("option");

		if (option != null && option.length() > 0) 
		{
			if ("continue".equals(option)) 
			{
				String uniqname = StringUtils.trimToNull(params
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
						addAlert(state, rb.getFormattedMessage("java.author", new Object[]{ServerConfigurationService.getString("officialAccountName")}));
					} 
					else 
					{
						// check instructors
						List instructors = new ArrayList(Arrays.asList(uniqname.split(",")));
						for (Iterator iInstructors = instructors.iterator(); iInstructors.hasNext();)
						{
							String instructorId = (String) iInstructors.next();
							try
							{
								UserDirectoryService.getUserByEid(instructorId);
							}
							catch (UserNotDefinedException e) 
							{
								addAlert(state, rb.getFormattedMessage("java.validAuthor", new Object[]{ServerConfigurationService.getString("officialAccountName")}));
								M_log.warn(this + ".doFind_course:" + rb.getFormattedMessage("java.validAuthor", new Object[]{ServerConfigurationService.getString("officialAccountName")}));
							}
						}
						if (state.getAttribute(STATE_MESSAGE) == null) {
							addRequestedSection(state);
						}
					}
				}
				else
				{
					addRequestedSection(state);
				}
				if (state.getAttribute(STATE_MESSAGE) == null) {
					// no manual add
					state.removeAttribute(STATE_MANUAL_ADD_COURSE_NUMBER);
					state.removeAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);

					if (getStateSite(state) == null) {
						if (state.getAttribute(STATE_TEMPLATE_SITE) != null)
						{
							// if creating site using template, stop here and generate the new site
							// create site based on template
							doFinish(data);
						}
						else
						{
							// else follow the normal flow
							state.setAttribute(STATE_TEMPLATE_INDEX, "13");
						}
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
			} else if ("add".equals(option)) {
				// get the uniqname input
				String uniqname = StringUtils.trimToNull(params.getString("uniqname"));
				state.setAttribute(STATE_SITE_QUEST_UNIQNAME, uniqname);
				addRequestedSection(state);
				return;
			} else if ("manual".equals(option)) {
				// TODO: send to case 37
				state.setAttribute(STATE_TEMPLATE_INDEX, "37");

				state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, Integer.valueOf(
						1));

				return;
			} else if ("remove".equals(option))
				removeAnyFlagedSection(state, params);
		}

		final List selections = new ArrayList(3);

		int cmLevel = getCMLevelLabels(state).size();
		String cmLevelChanged = params.get("cmLevelChanged");
		if ("true".equals(cmLevelChanged)) {
			// when cm level changes, set the focus to the new level
			String cmChangedLevel = params.get("cmChangedLevel");
			cmLevel = cmChangedLevel != null ? Integer.valueOf(cmChangedLevel).intValue() + 1:cmLevel;
		}
		for (int i = 0; i < cmLevel; i++) {
			String[] val = params.getStrings("idField_" + i);

			if (val == null || val.length == 0) {
				break;
			}
			if (val.length == 1)
			{
				selections.add(val[0]);
			}
			else
			{
				for (int k=0; k<val.length;k++)
				{
					selections.add(val[k]);
				}
			}
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

	private String getAuthorizers(SessionState state, String attributeName) {
		String authorizers = "";
		ArrayList list = (ArrayList) state
				.getAttribute(attributeName);
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
				try
				{
					Section s = cms.getSection(sectionEid);
					if (s != null)
					{
						SectionObject so = new SectionObject(s);
						so.setAuthorizer(new ArrayList(Arrays.asList(userId.split(","))));
						list.add(so);
					}
				}
				catch (IdNotFoundException e)
				{
					M_log.warn("prepareSectionObject: Cannot find section " + sectionEid);
				}
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
				M_log.error("Problem sending redirect to: "+ url,  e);
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
	
	
	private String getSiteBaseUrl() {
		return ServerConfigurationService.getPortalUrl() + "/" + 
			ServerConfigurationService.getString("portal.handler.default", "site") + 
			"/";
	}
	
	private String getDefaultSiteUrl(String siteId) {
		return prefixString(getSiteBaseUrl(), siteId);
	}
	
	private Collection<String> getSiteReferenceAliasIds(Site forSite) {
		return prefixSiteAliasIds(null, forSite);
	}
	
	private Collection<String> getSiteUrlsForSite(Site site) {
		return prefixSiteAliasIds(getSiteBaseUrl(), site);
	}
	
	private Collection<String> getSiteUrlsForAliasIds(Collection<String> aliasIds) {
		return prefixSiteAliasIds(getSiteBaseUrl(), aliasIds);
	}
	
	private String getSiteUrlForAliasId(String aliasId) {
		return prefixString(getSiteBaseUrl(), aliasId);
	}
	
	private Collection<String> prefixSiteAliasIds(String prefix, Site site) {
		return prefixSiteAliasIds(prefix, aliasService.getAliases(site.getReference()));
	}
	
	private Collection<String> prefixSiteAliasIds(String prefix, Collection<? extends Object> aliases) {
		List<String> siteAliases = new ArrayList<String>();
		for (Object alias : aliases) {
			String aliasId = null;
			if ( alias instanceof Alias ) {
				aliasId = ((Alias)alias).getId();
			} else {
				aliasId = alias.toString();
			}
			siteAliases.add(prefixString(prefix,aliasId));
		}
		return siteAliases;
	}
	
	private String prefixString(String prefix, String aliasId) {
		return (prefix == null ? "" : prefix) + aliasId;
	}
	
	private List<String> toIdList(List<? extends Entity> entities) {
		List<String> ids = new ArrayList<String>(entities.size());
		for ( Entity entity : entities ) {
			ids.add(entity.getId());
		}
		return ids;
	}
	
	/**
	 * whether this tool title is of Home tool title
	 * @param toolTitle
	 * @return
	 */
	private boolean isHomePage(SitePage page)
	{
		if (page.getProperties().getProperty(SitePage.IS_HOME_PAGE) != null)
		{
			// check based on the page property first
			return true;
		}
		else
		{
			// if above fails, check based on the page title
			String pageTitle = page.getTitle();
			return TOOL_ID_HOME.equalsIgnoreCase(pageTitle) || rb.getString("java.home").equalsIgnoreCase(pageTitle);
		}
	}

	public boolean displaySiteAlias() {
		if (ServerConfigurationService.getBoolean("wsetup.disable.siteAlias", false)) {
			return false;
		}
		return true;
	}
	
	private void putPrintParticipantLinkIntoContext(Context context, RunData data, Site site) {
		// the status servlet reqest url
		String url = Web.serverUrl(data.getRequest()) + "/sakai-site-manage-tool/tool/printparticipant/" + site.getId();
		context.put("printParticipantUrl", url);
	}
	
	/**
	 * dispatch function for site type vm
	 * @param data
	 */
	public void doSite_type_option(RunData data)
	{
		ParameterParser params = data.getParameters();
		String option = StringUtils.trimToNull(params.getString("option"));
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (option != null)
		{
			if ("cancel".equals(option))
			{
				doCancel_create(data);
			}
			else if ("siteType".equals(option))
			{
				doSite_type(data);
			}
			else if ("createOnTemplate".equals(option))
			{
				doSite_copyFromTemplate(data);
			}
			else if ("createCourseOnTemplate".equals(option))
			{
				doSite_copyFromCourseTemplate(data);
			}
			else if ("createCourseOnTemplate".equals(option))
			{
				doSite_copyFromCourseTemplate(data);
			}
			else if ("createFromArchive".equals(option))
			{
				state.setAttribute(STATE_CREATE_FROM_ARCHIVE, Boolean.TRUE);
				//continue with normal workflow
				doSite_type(data);
			}			
		}
	}
	
	/**
	 * create site from template
	 * @param params
	 * @param state
	 */
	private void doSite_copyFromTemplate(RunData data)
	{	
		ParameterParser params = data.getParameters();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
	
		// read template information
		readCreateSiteTemplateInformation(params, state);
		
		// create site
		doFinish(data);
	}
	
	/**
	 * create course site from template, next step would be select roster
	 * @param params
	 * @param state
	 */
	private void doSite_copyFromCourseTemplate(RunData data)
	{
		ParameterParser params = data.getParameters();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
	
		// read template information
		readCreateSiteTemplateInformation(params, state);
		
		// redirect for site roster selection
		redirectCourseCreation(params, state, "selectTermTemplate");
	}
	
	/**
	 * read the user input for creating site based on template
	 * @param params
	 * @param state
	 */
	private void readCreateSiteTemplateInformation(ParameterParser params, SessionState state)
	{
		// get the template site id
		String templateSiteId = params.getString("templateSiteId");
		try {
			Site templateSite = SiteService.getSite(templateSiteId);
			state.setAttribute(STATE_TEMPLATE_SITE, templateSite);
			state.setAttribute(STATE_SITE_TYPE, templateSite.getType());
			
			SiteInfo siteInfo = new SiteInfo();
			if (state.getAttribute(STATE_SITE_INFO) != null) {
				siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			}
			siteInfo.site_type = templateSite.getType();
			siteInfo.title = StringUtils.trimToNull(params.getString("siteTitleField"));
			siteInfo.term = StringUtils.trimToNull(params.getString("selectTermTemplate"));
			siteInfo.iconUrl = templateSite.getIconUrl();
			
			Boolean copyTemplateDescription = ServerConfigurationService.getBoolean(SAK_PROP_COPY_TEMPLATE_DESCRIPTION, Boolean.TRUE);
			if(copyTemplateDescription.booleanValue()){
				siteInfo.description = templateSite.getDescription();
			}
			
			siteInfo.short_description = templateSite.getShortDescription();
			siteInfo.joinable = templateSite.isJoinable();
			siteInfo.joinerRole = templateSite.getJoinerRole();
			state.setAttribute(STATE_SITE_INFO, siteInfo);
			
			// bjones86 - SAK-24423 - update site info for joinable site settings
			JoinableSiteSettings.updateSiteInfoFromParams( params, siteInfo );
			
			// whether to copy users or site content over?
			if (params.getBoolean("copyUsers")) state.setAttribute(STATE_TEMPLATE_SITE_COPY_USERS, Boolean.TRUE); else state.removeAttribute(STATE_TEMPLATE_SITE_COPY_USERS);
			if (params.getBoolean("copyContent")) state.setAttribute(STATE_TEMPLATE_SITE_COPY_CONTENT, Boolean.TRUE); else state.removeAttribute(STATE_TEMPLATE_SITE_COPY_CONTENT);
			if (params.getBoolean("publishSite")) state.setAttribute(STATE_TEMPLATE_PUBLISH, Boolean.TRUE); else state.removeAttribute(STATE_TEMPLATE_PUBLISH);
		}
		catch(Exception e){
			M_log.warn(this + "readCreateSiteTemplateInformation: problem of getting template site: " + templateSiteId);
		}
	}

	/**
	 * redirect course creation process after the term selection step
	 * @param params
	 * @param state
	 * @param termFieldName
	 */
	private void redirectCourseCreation(ParameterParser params, SessionState state, String termFieldName) {
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

		String academicSessionEid = params.getString(termFieldName);
		// check whether the academicsession might be null
		if (academicSessionEid != null)
		{
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
					
					Boolean skipCourseSectionSelection = ServerConfigurationService.getBoolean(SAK_PROP_SKIP_COURSE_SECTION_SELECTION, Boolean.FALSE);
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
			}
		}
	}
	
	public void doEdit_site_info(RunData data)
	{

		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
					
		String locale_string = params.getString("locales"); 
										
		state.setAttribute("locale_string",locale_string);
			
		String option = params.getString("option");
		if ("removeSection".equals(option))
		{
			// remove section
			removeAnyFlagedSection(state, params);
		}
		else if ("continue".equals(option))
		{
			// continue with site information edit

			MathJaxEnabler.applyAllowedSettingsToState(state, params);  // SAK-22384

			doContinue(data);
		}
		else if ("back".equals(option))
		{
			// go back to previous pages
			doBack(data);
		}
		else if ("cancel".equals(option))
		{
			// cancel
			doCancel(data);
		}
	}
		
	/**
	 * *
	 * 
	 * @return Locale based on its string representation (language_region)
	 */
	private Locale getLocaleFromString(String localeString)
	{
	    org.sakaiproject.component.api.ServerConfigurationService scs = (org.sakaiproject.component.api.ServerConfigurationService) ComponentManager.get(org.sakaiproject.component.api.ServerConfigurationService.class);
	    return scs.getLocaleFromString(localeString);
	}
	
	/**
	 * Handle the eventSubmit_doUnjoin command to have the user un-join this site.
	 */
	public void doUnjoin(RunData data) {
		
		SessionState state = ((JetspeedRunData) data)
			.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		final ParameterParser params = data.getParameters();

		final String id = params.get("itemReference");
		String siteTitle = null;
		
		if (id != null)	{
			try	{
				siteTitle = SiteService.getSite(id).getTitle();
				SiteService.unjoin(id);
				String msg = rb.getString("sitinfimp.youhave") + " " + siteTitle;
				addAlert(state, msg);
				
			} catch (IdUnusedException ignore) {
				
			} catch (PermissionException e)	{
				// This could occur if the user's role is the maintain role for the site, and we don't let the user
				// unjoin sites they are maintainers of
				Log.warn("chef", this + ".doUnjoin(): " + e);
				//TODO can't access site so redirect to portal
				
			} catch (InUseException e) {
				addAlert(state, siteTitle + " "
						+ rb.getString("sitinfimp.sitebeing") + " ");
			}
		}
		
		// refresh the whole page
		scheduleTopRefresh();
		
	} // doUnjoin

	
	/**
	 * @return Returns the prefLocales
	 */
	public List<Locale> getPrefLocales()
	{
		// Initialize list of supported locales, if necessary
		if (prefLocales.size() == 0) {
			org.sakaiproject.component.api.ServerConfigurationService scs = (org.sakaiproject.component.api.ServerConfigurationService) ComponentManager.get(org.sakaiproject.component.api.ServerConfigurationService.class);
			Locale[] localeArray = scs.getSakaiLocales();
			// Add to prefLocales list
			for (int i = 0; i < localeArray.length; i++) {
				prefLocales.add(localeArray[i]);
			}
		}
		return prefLocales;
	}

	// SAK-20797
	/**
	 * return quota on site specified by siteId
	 * @param siteId
	 * @return value of site-specific quota or 0 if not found
	 */
	private long getSiteSpecificQuota(String siteId) {
		long quota = 0;
		try {
			Site site = SiteService.getSite(siteId);
			if (site != null) {
				quota = getSiteSpecificQuota(site);
			}
		} catch (IdUnusedException e) {
			M_log.error("Quota calculation could not find the site " + siteId
					+ "for site specific quota calculation",
					M_log.isDebugEnabled() ? e : null);
		}
		return quota;
	}

	
	// SAK-20797
	/**
	 * return quota set on this specific site
	 * @param collection
	 * @return value of site-specific quota or 0 if not found
	 */
	private long getSiteSpecificQuota(Site site) {
		long quota = 0;
		try
		{
			String collId = m_contentHostingService
					.getSiteCollection(site.getId());
			ContentCollection site_collection = m_contentHostingService.getCollection(collId);
			long siteSpecific = site_collection.getProperties().getLongProperty(
					ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
			quota = siteSpecific;
		}
		catch (Exception ignore)
		{
			M_log.warn("getQuota: reading quota property for site : " + site.getId() + " : " + ignore);
			quota = 0;
		}
		return quota;
	}

	// SAK-20797
	/**
	 * return file size in bytes as formatted string
	 * @param quota
	 * @return formatted string (i.e. 2048 as 2 KB)
	 */
	private String formatSize(long quota) {
		String size = "";
		NumberFormat formatter = NumberFormat.getInstance(rb.getLocale());
		formatter.setMaximumFractionDigits(1);
		if (quota > 700000000L) {
			String[] args = { formatter.format(1.0 * quota
					/ (1024L * 1024L * 1024L)) };
			size = rb.getFormattedMessage("size.gb", args);
		} else if (quota > 700000L) {
			String[] args = { formatter.format(1.0 * quota / (1024L * 1024L)) };
			size = rb.getFormattedMessage("size.mb", args);
		} else if (quota > 700L) {
			String[] args = { formatter.format(1.0 * quota / 1024L) };
			size = rb.getFormattedMessage("size.kb", args);
		} else {
			String[] args = { formatter.format(quota) };
			size = rb.getFormattedMessage("size.bytes", args);
		}
		return size;
	}

	public class JoinableGroup{
		private String reference;
		private String title;
		private String joinableSet;
		private int size;
		private int max;
		private String members;
		private boolean preview;

		public JoinableGroup(String reference, String title, String joinableSet, int size, int max, String members, boolean preview){
			this.reference = reference;
			this.title = title;
			this.joinableSet = joinableSet;
			this.size = size;
			this.max = max;
			this.members = members;
			this.preview = preview;
		}

		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getJoinableSet() {
			return joinableSet;
		}
		public void setJoinableSet(String joinableSet) {
			this.joinableSet = joinableSet;
		}
		public int getSize() {
			return size;
		}
		public void setSize(int size) {
			this.size = size;
		}
		public int getMax() {
			return max;
		}
		public void setMax(int max) {
			this.max = max;
		}
		public String getMembers() {
			return members;
		}
		public void setMembers(String members) {
			this.members = members;
		}

		public boolean isPreview() {
			return preview;
		}

		public void setPreview(boolean preview) {
			this.preview = preview;
		}

		public String getReference() {
			return reference;
		}

		public void setReference(String reference) {
			this.reference = reference;
		}

	}
	
	
	/**
	 * Handles uploading an archive file as part of the site creation workflow
	 * @param data
	 */
	public void doUploadArchive(RunData data)
	{	
		ParameterParser params = data.getParameters();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
	
		//get params
		FileItem fi = data.getParameters().getFileItem ("importFile");
		
		//get uploaded file into a location we can process
		String archiveUnzipBase = ServerConfigurationService.getString("archive.storage.path", FileUtils.getTempDirectoryPath());
	
		//convert inputstream into actual file so we can unzip it
		String zipFilePath = archiveUnzipBase + File.separator + fi.getFileName();
		
		//rudimentary check that the file is a zip file
		if(!StringUtils.endsWith(fi.getFileName(), ".zip")){
			addAlert(state, rb.getString("archive.createsite.failedupload"));
			return;
		}
		
		
		
		File tempZipFile = new File(zipFilePath);
		if(tempZipFile.exists()) {
			tempZipFile.delete();
		}
		
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(tempZipFile);
			//copy contents into this file
			IOUtils.copyLarge(fi.getInputStream(), fileOutputStream);
			
			//set path into state so we can process it later
			state.setAttribute(STATE_UPLOADED_ARCHIVE_PATH, tempZipFile.getAbsolutePath());
			state.setAttribute(STATE_UPLOADED_ARCHIVE_NAME, tempZipFile.getName());
			
		} catch (Exception e) {
			M_log.error(e.getMessage(), e); //general catch all for the various exceptions that occur above. all are failures.
			addAlert(state, rb.getString("archive.createsite.failedupload"));
		}
		finally {
			IOUtils.closeQuietly(fileOutputStream);
		}
		
		//go to confirm screen
		state.setAttribute(STATE_TEMPLATE_INDEX, "10");
	}
	
	/**
	 * Handles merging an uploaded archive into the newly created site
	 * This is done after the site has been created in the site creation workflow
	 * 
	 * @param siteId
	 * @param state
	 */
	private void doMergeArchiveIntoNewSite(String siteId, SessionState state) {	
		
		String currentUserId = userDirectoryService.getCurrentUser().getId();
		
		try {
		
			String archivePath = (String)state.getAttribute(STATE_UPLOADED_ARCHIVE_PATH);
			
			//merge the zip into our new site
			//we ignore the return because its not very useful. See ArchiveService for more details
			archiveService.mergeFromZip(archivePath, siteId, currentUserId);
			
		} catch (Exception e) {
			M_log.error(e.getMessage(), e); //general catch all for the various exceptions that occur above. all are failures.
			addAlert(state, rb.getString("archive.createsite.failedmerge"));
		}
		
	}
	
	/**
	 * Helper to hard delete all implementing services
	 * @param siteId siteId that we want to delete the content for
	 * @return
	 */
	private void doHardDelete(String siteId) {
				
		// leverage the entityproducer registration system
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i.hasNext();) {
			EntityProducer ep = (EntityProducer) i.next();
			
			//if a registered service implements hard delete, then ask it to delete itself
			if (ep instanceof HardDeleteAware) {
				HardDeleteAware hd = (HardDeleteAware) ep;
				M_log.info("Requesting hard delete for site:" + siteId + ", tool: " + ep.getLabel());
				hd.hardDelete(siteId);
			}
		}
		
	}
	
	/**
	 * Check if we are adding missing tools when importing 
	 * @return
	 */
	private boolean isAddMissingToolsOnImportEnabled() {
		return ServerConfigurationService.getBoolean("site.setup.import.addmissingtools", false);
	}
	
	/**
	 * Helper to add a tool to a site if the site does not contain an instance of the tool
	 * 
	 * Note that it does NOT save the site. The caller must handle this.
	 * 
	 * @param site the site to check
	 * @param toolId the tool to add (eg sakai.resources)
	 * 
	 * @return site the updated site object
	 */
	private Site addToolToSiteIfMissing(Site site, String toolId) {
		
		if(site.getToolForCommonId(toolId) != null) {
			return site;
		}
		
		M_log.debug("Adding tool to site: " + site.getId() + ", tool: " + toolId);
				
		SitePage page = site.addPage(); //inherit the tool's title.
				
		ToolConfiguration tool = page.addTool();
		tool.setTool(toolId, ToolManager.getTool(toolId));
		tool.setTitle(ToolManager.getTool(toolId).getTitle());
		
		return site;
	}
	
	/**
	 * Get the list of tools that are in a list of sites that are available for import.
	 * 
	 * Only tools with content will be collected. See hasContent(toolId, siteId) for the behaviour.
	 * 
	 * @param list of siteids to check, could be a singleton
	 * @return a list of toolIds that are in the sites that are available for import
	 * 
	 */
	private List<String> getToolsInSitesAvailableForImport(List<Site> sites) {
		List<String> allImportToolIds = new ArrayList<String>(importTools());
		
		List<String> importToolsInSites = new ArrayList<String>(); 
		
		for(Site site: sites) {
			for(String toolId: allImportToolIds) {
				if(site.getToolForCommonId(toolId) != null) {
					
					//check the tool has content. 
					//this caters for the case where we only selected one site for import, this means the tool won't show in the list at all.
					if(hasContent(toolId, site.getId())) {
						importToolsInSites.add(toolId);
					}
					
				}
			}
		}
	
		return importToolsInSites;
	}
	
	/**
	 * Get a map of all names for the given list of tools in the given sites. For example if a tool with id sakai.mytool
	 * is called My Tool in one site and An Amazing Tool in another site, the set will contain both for that tool id.
	 * @param toolIds
	 * @param sites
	 * @return
	 */
	private Map<String,Set<String>> getToolNames(List<String> toolIds, List<Site> sites) {
		Map<String,Set<String>> rval = new HashMap<>();
		
		//foreach toolid
		for(String toolId : toolIds){
			
			//init a set
			Set<String> toolNames = new HashSet<>();
			
			//for each site
			for(Site s: sites) {
				
				//get the name of this tool in the site for the page it is on, if it exists, add to the list
				List<ToolConfiguration> toolConfigs = (List<ToolConfiguration>)s.getTools(toolId);
				for(ToolConfiguration config: toolConfigs){
					toolNames.add(config.getContainingPage().getTitle());
				}
			}
			
			rval.put(toolId, toolNames);			
		}
		
		return rval;
	}
	
	
	
	/**
	 * Get a map of tools in each site that have content.
	 * The list of tools are only those that have been selected for import
	 * 
	 * The algorithm for determining this is documented as part of hasContent(siteId, toolid);
	 * 
	 * @param sites
	 * @param toolIds
	 * @return Map keyed on siteId. Set contains toolIds that have content.
	 */
	private Map<String, Set<String>> getSiteImportToolsWithContent(List<Site> sites, List<String> toolIds) {
		
		Map<String, Set<String>> siteToolsWithContent = new HashMap<>(); 
		
		for(Site site: sites) {
			
			Set<String> toolsWithContent = new HashSet<>();
			
			for(String toolId: toolIds) {
				if(site.getToolForCommonId(toolId) != null) {
					
					//check the tool has content
					if(hasContent(toolId, site.getId())) {
						toolsWithContent.add(toolId);
					}
				}
			}
			
			M_log.debug("Site: " + site.getId() + ", has the following tools with content: " + toolsWithContent);
			
			siteToolsWithContent.put(site.getId(), toolsWithContent);
		}
	
		return siteToolsWithContent;
	}
	
	/**
	 * Helper to check if a tool in a site has content. 
	 * This leverages the EntityProducer system and then checks each producer to see if it is the one we are interested in
	 * If the tool implements the ContentExistsAware interface, then it asks the tool explicitly if it has content.
	 * If the tool does not implement this interface, then we have no way to tell, so for backwards compatibility we assume it has content.
	 * 
	 * @param toolId
	 * @param siteId
	 */
	private boolean hasContent(String toolId, String siteId) {
		
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i.hasNext();) {
			EntityProducer ep = (EntityProducer) i.next();
			
			if (ep instanceof EntityTransferrer) {
				EntityTransferrer et = (EntityTransferrer) ep;
				
				if (ArrayUtils.contains(et.myToolIds(), toolId)) {
					
					if(ep instanceof ContentExistsAware){
						ContentExistsAware cea = (ContentExistsAware) ep;
						M_log.debug("Checking tool content for site:" + siteId + ", tool: " + et.myToolIds());
						return cea.hasContent(siteId);
					} 
				}
			}
		}
		return true; //backwards compatibility
	}

	
}
