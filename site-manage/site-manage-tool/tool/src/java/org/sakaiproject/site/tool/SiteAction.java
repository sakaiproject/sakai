/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.tool;

import static org.sakaiproject.site.util.SiteConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.velocity.tools.generic.SortTool;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.AuthzRealmLockException;
import org.sakaiproject.authz.api.GroupFullException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.entity.api.ContentExistsAware;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportService;
import org.sakaiproject.importer.api.ResetOnCloseInputStream;
import org.sakaiproject.importer.api.SakaiArchive;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.lti.api.LTIService;
import org.tsugi.lti.LTIUtil;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.scoringservice.api.ScoringAgent;
import org.sakaiproject.scoringservice.api.ScoringService;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SiteTitleValidationStatus;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.tool.MenuBuilder.SiteInfoActiveTab;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteComparator;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.site.util.SiteSetupQuestionFileParser;
import org.sakaiproject.site.util.SiteTextEditUtil;
import org.sakaiproject.site.util.SiteTypeUtil;
import org.sakaiproject.sitemanage.api.AffiliatedSectionProvider;
import org.sakaiproject.sitemanage.api.PublishingSiteScheduleService;
import org.sakaiproject.sitemanage.api.SectionField;
import org.sakaiproject.sitemanage.api.SectionFieldProvider;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.sitemanage.api.SiteManageConstants;
import org.sakaiproject.sitemanage.api.SiteManageService;
import org.sakaiproject.sitemanage.api.SiteTypeProvider;
import org.sakaiproject.sitemanage.api.UnpublishingSiteScheduleService;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.sitemanage.api.model.SiteSetupQuestion;
import org.sakaiproject.sitemanage.api.model.SiteSetupQuestionAnswer;
import org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService;
import org.sakaiproject.sitemanage.api.model.SiteSetupUserAnswer;
import org.sakaiproject.sitemanage.api.model.SiteTypeQuestions;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.sakaiproject.util.comparator.AlphaNumericComparator;
import org.sakaiproject.util.comparator.GroupTitleComparator;
import org.sakaiproject.util.comparator.ToolTitleComparator;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * SiteAction controls the interface for worksite setup.
 * </p>
 */
@Slf4j
public class SiteAction extends PagedResourceActionII {
	// SAK-23491 add template_used property
	private static final String TEMPLATE_USED = "template_used";

	private static final ResourceLoader rb = new ResourceLoader("sitesetupgeneric");
	private static final ResourceLoader cfgRb = new ResourceLoader("multipletools");

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
			"-uploadArchive",
			"-siteInfo-manageParticipants",  // 63
			"-newSite",
			"-siteInfo-manageOverview" // 65
	};

	/** Name of state attribute for Site instance id */
	private static final String STATE_SITE_INSTANCE_ID = "site.instance.id";

	/** Name of state attribute for Site Information */
	private static final String STATE_SITE_INFO = "site.info";

	static final String STATE_SITE_TYPE = "site-type";

	/** Name of state attribute for possible site types */
	private static final String STATE_SITE_TYPES = "site_types";

	static final String STATE_DEFAULT_SITE_TYPE = "default_site_type";

	private static final String STATE_PUBLIC_CHANGEABLE_SITE_TYPES = "changeable_site_types";

	private static final String STATE_PUBLIC_SITE_TYPES = "public_site_types";

	private static final String STATE_PRIVATE_SITE_TYPES = "private_site_types";

	private static final String STATE_DISABLE_JOINABLE_SITE_TYPE = "disable_joinable_site_types";

	
	private static final String PROP_SITE_LANGUAGE = "locale_string";

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

	private static final String STATE_TOOL_HOME_SELECTED = "toolHomeSelected";

	private static final String UNGROUPED_TOOL_TITLE = "systoolgroups.ungrouped";
	private static final String LTI_TOOL_TITLE		 = "systoolgroups.lti";

    //********************

	private static final String STATE_TOOL_EMAIL_ADDRESS = "toolEmailAddress";
	private static final String STATE_DUP_SITE_HAS_EMAIL_ARCHIVE = "dupSiteHasEmailArchive";

	private static final String STATE_PROJECT_TOOL_LIST = "projectToolList";

	private static final String STATE_MULTIPLE_TOOL_ID_SET = "multipleToolIdSet";
	private static final  String STATE_MULTIPLE_TOOL_ID_TITLE_MAP = "multipleToolIdTitleMap";
	private static final String STATE_MULTIPLE_TOOL_CONFIGURATION = "multipleToolConfiguration";

	private static String SITE_DEFAULT_LIST;
	private static String DEFAULT_SITE_TYPE_SAK_PROP;
	private static String[] PUBLIC_CHANGEABLE_SITE_TYPES_SAK_PROP;
	private static String[] PUBLIC_SITE_TYPES_SAK_PROP;
	private static String[] PRIVATE_SITE_TYPES_SAK_PROP;

	private static final String SAK_PROP_DEFAULT_SITE_VIS = "wsetup.defaultSiteVisibility";
	private static final boolean SAK_PROP_DEFAULT_SITE_VIS_DFLT = true;

	private static final String STATE_SITE_QUEST_UNIQNAME = "site_quest_uniqname";
	
	private static final String STATE_SITE_ADD_COURSE = "canAddCourse";
	
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
	private static final String STATE_WORKSITE_SETUP_PAGE_LIST = "wSetupPageList"; // the

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
	private static final String FORM_TITLE = "form_title";

	private static final String FORM_SITE_URL_BASE = "form_site_url_base";
	
	private static final String FORM_SITE_ALIAS = "form_site_alias";
	
	private static final String FORM_DESCRIPTION = "form_description";

	private static final String FORM_HONORIFIC = "form_honorific";

	private static final String FORM_INSTITUTION = "form_institution";

	private static final String FORM_SUBJECT = "form_subject";

	private static final String FORM_PHONE = "form_phone";

	private static final String FORM_EMAIL = "form_email";

	private static final String FORM_REUSE = "form_reuse";

	private static final String FORM_RELATED_CLASS = "form_related_class";

	private static final String FORM_RELATED_PROJECT = "form_related_project";

	private static final String FORM_NAME = "form_name";

	private static final String FORM_SHORT_DESCRIPTION = "form_short_description";

	private static final String FORM_ICON_URL = "iconUrl";
	
	private static final String FORM_SITEINFO_URL_BASE = "form_site_url_base";
	
	private static final String FORM_SITEINFO_ALIASES = "form_site_aliases";

	private static final String FORM_WILL_NOTIFY = "form_will_notify";

	/** Context action */
	private static final String CONTEXT_ACTION = "SiteAction";

	/** Integer index for the Manage Participants UI */
	private static final String STATE_TEMPLATE_INDEX_MANAGE_PARTICIPANTS = "63";
	private static final int STATE_TEMPLATE_INDEX_MANAGE_PARTICIPANTS_INT = 63;

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
	public static final String STATE_ACTION = "site.action";

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

	private static final String STATE_IMPORT_SITE_TOOL_ITEMS = "state_import_site_tool_items";

	private static final String STATE_IMPORT_SITE_TOOL_OPTIONS = "state_import_site_tool_options";

	/** for navigating between sites in site list */
	private static final String STATE_SITES = "state_sites";

	private static final String STATE_PREV_SITE = "state_prev_site";

	private static final String STATE_NEXT_SITE = "state_next_site";

	/** for course information */
	private static final String STATE_TERM_COURSE_LIST = "state_term_course_list";

	private static final String STATE_TERM_COURSE_HASH = "state_term_course_hash";

	private static final String STATE_TERM_SELECTED = "state_term_selected";

	private static final String STATE_INSTRUCTOR_SELECTED = "state_instructor_selected";

	private static final String STATE_FUTURE_TERM_SELECTED = "state_future_term_selected";

	private static final String STATE_ADD_CLASS_PROVIDER = "state_add_class_provider";

	private static final String STATE_ADD_CLASS_PROVIDER_CHOSEN = "state_add_class_provider_chosen";
	
	private static final String STATE_ADD_CLASS_PROVIDER_DESCRIPTION_CHOSEN = "state_add_class_provider_description_chosen";

	private static final String STATE_ADD_CLASS_MANUAL = "state_add_class_manual";

	private static final String STATE_AUTO_ADD = "state_auto_add";

	private static final String STATE_MANUAL_ADD_COURSE_NUMBER = "state_manual_add_course_number";

	private static final String STATE_MANUAL_ADD_COURSE_FIELDS = "state_manual_add_course_fields";

	public final static String PROP_SITE_REQUEST_COURSE = "site-request-course-sections";

	public final static String SITE_PROVIDER_COURSE_LIST = "site_provider_course_list";

	public final static String SITE_MANUAL_COURSE_LIST = "site_manual_course_list";

	private static final String STATE_SUBJECT_AFFILIATES = "site.subject.affiliates";

	private static final String STATE_ICONS = "icons";

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

	//list to store participants for a user search in site_info
	private static final String STATE_SITE_PARTICIPANT_LIST = "site_participants";

	//for user search in site_info page
	private static final String SITE_USER_SEARCH = "search_user";

	public static final String STATE_SITE_PARTICIPANT_FILTER = "site_participant_filter";
    
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

	// used in the configuration file to specify which tool attributes are configurable through WSetup tool, and what are the default value for them.
	private static final String CONFIG_TOOL_ATTRIBUTE = "wsetup.config.tool.attribute_";
	private static final String CONFIG_TOOL_ATTRIBUTE_DEFAULT = "wsetup.config.tool.attribute.default_";
	// used in the configuration file to specify the default tool title 
	private static final String CONFIG_TOOL_TITLE = "wsetup.config.tool.title_";
	
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
	private static final Map<String, List<String>> SYNOPTIC_TOOL_ID_MAP;
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
	private static final Map<String, String> SYNOPTIC_TOOL_TITLE_MAP;
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
	private static final String WEB_CONTENT_TOOL_ID = "sakai.iframe";
	private static final String WEB_CONTENT_TOOL_SOURCE_CONFIG = "source";
	private static final String WEB_CONTENT_TOOL_SOURCE_CONFIG_VALUE = "http://";

	/** the news tool **/
	private static final String NEWS_TOOL_ID = "sakai.simple.rss";
	private static final String NEWS_TOOL_CHANNEL_CONFIG = "javax.portlet-feed_url";
	private static final String NEWS_TOOL_CHANNEL_CONFIG_VALUE = "https://www.sakailms.org/blog-feed.xml";
	
   	private static final String LESSONS_TOOL_ID = "sakai.lessonbuildertool";
   	private static final String GRADEBOOK_TOOL = "sakai.gradebook";
   	private static final String GRADEBOOK_TOOL_ID = "sakai.gradebook.tool";
   	private static final String GRADEBOOKNG_TOOL_ID = "sakai.gradebookng";

	private static final int UUID_LENGTH = 36;
	
	/** the course set definition from CourseManagementService **/
	private static final String STATE_COURSE_SET = "state_course_set";
	
	// the maximum tool title length enforced in UI
	private static final int MAX_TOOL_TITLE_LENGTH = 20;
	
	private static final String SORT_KEY_SESSION = "worksitesetup.sort.key.session";
	private static final String SORT_ORDER_SESSION = "worksitesetup.sort.order.session";
	private static final String SORT_KEY_COURSE_SET = "worksitesetup.sort.key.courseSet";
	private static final String SORT_ORDER_COURSE_SET = "worksitesetup.sort.order.courseSet";
	private static final String SORT_KEY_COURSE_OFFERING = "worksitesetup.sort.key.courseOffering";
	private static final String SORT_ORDER_COURSE_OFFERING = "worksitesetup.sort.order.courseOffering";
	private static final String SORT_KEY_SECTION = "worksitesetup.sort.key.section";
	private static final String SORT_ORDER_SECTION = "worksitesetup.sort.order.section";

	public final static String SAK_PROP_SITE_SETUP_GROUP_SUPPORT = "wsetup.group.support";
	public final static boolean SAK_PROP_SITE_SETUP_GROUP_SUPPORT_DEFAULT = true;

	// SAK-23255
	private static final String CONTEXT_IS_ADMIN = "isAdmin";
	private static final String CONTEXT_SKIP_MANUAL_COURSE_CREATION = "skipManualCourseCreation";
	private static final String CONTEXT_SKIP_COURSE_SECTION_SELECTION = "skipCourseSectionSelection";
	private static final String CONTEXT_FILTER_TERMS = "filterTerms";
	private static final String SAK_PROP_SKIP_MANUAL_COURSE_CREATION = "wsetup.skipManualCourseCreation";
	private static final String SAK_PROP_SKIP_COURSE_SECTION_SELECTION = "wsetup.skipCourseSectionSelection";
	
	//SAK-22432 Template descriptions are not copied
	private static final String SAK_PROP_COPY_TEMPLATE_DESCRIPTION = "site.setup.copy.template.description";

	private static final String SAK_PROP_SHOW_ROSTER_EID = "wsetup.showRosterEIDs";
	private static final boolean SAK_PROP_SHOW_ROSTER_EID_DEFAULT = false;

	//Setup property to require (or not require) authorizer
	private static final String SAK_PROP_REQUIRE_AUTHORIZER = "wsetup.requireAuthorizer";
	//Setup property to email authorizer (default to true)
	private static final String SAK_PROP_EMAIL_AUTHORIZER = "wsetup.emailAuthorizer";

	private static final String VM_ALLOWED_ROLES_DROP_DOWN 	= "allowedRoles";
	
	// SAK-23256
	private static final String SAK_PROP_FILTER_TERMS = "worksitesetup.filtertermdropdowns";
	private static final String CONTEXT_HAS_TERMS = "hasTerms";
	
	private static final String SAK_PROP_AUTO_FILTER_TERM = "site.setup.autoFilterTerm";

	private static final String SAK_PROP_RM_STLTH_ON_DUP = "site.duplicate.removeStealthTools";
	private static final boolean SAK_PROP_RM_STLTH_ON_DUP_DEFAULT = false;

	private static final String SAK_PROP_ALLOW_DEL_LAST_ROSTER = "site.setup.allowDelLastRoster";
	private static final boolean SAK_PROP_ALLOW_DEL_LAST_ROSTER_DFLT = false;

	// state variable for whether any multiple instance tool has been selected
	private static final String STATE_MULTIPLE_TOOL_INSTANCE_SELECTED = "state_multiple_tool_instance_selected";
	// state variable for lti tools
	private static final String STATE_LTITOOL_LIST = "state_ltitool_list";
	// state variable for selected lti tools in site
	private static final String STATE_LTITOOL_EXISTING_SELECTED_LIST = "state_ltitool_existing_selected_list";
	// state variable for selected lti tools during tool modification
	private static final String STATE_LTITOOL_SELECTED_LIST = "state_ltitool_selected_list";
	// state variable for stranded lti tools (deployed in site but no longer available)
	private static final String STATE_LTITOOL_STRANDED_LIST = "state_tool_stranded_lti_tool_list";
	// special prefix String for basiclti tool ids
	private static final String LTITOOL_ID_PREFIX = "lti_";
	
	private static final String STATE_HARD_DELETE = "hardDelete";
	private static final String STATE_SOFT_DELETE = "softDelete";

	private static final String STATE_CREATE_FROM_ARCHIVE = "createFromArchive";
	private static final String STATE_UPLOADED_ARCHIVE_PATH = "uploadedArchivePath";
	private static final String STATE_UPLOADED_ARCHIVE_NAME = "uploadedArchiveNAme";
	
	// SAK-28990 - enable/disable continue with no roster
	private static final String VM_CONT_NO_ROSTER_ENABLED = "contNoRosterEnabled";
	private static final String SAK_PROP_CONT_NO_ROSTER_ENABLED = "sitemanage.continueWithNoRoster";
	
	private static final String VM_ADD_ROSTER_AUTH_REQUIRED = "authorizationRequired";

	private static final String GB_GROUP_PROPERTY = "gb-group";

	private Cache m_userSiteCache;
	private ImportService importService;
	private List prefLocales;
	private Locale comparator_locale;
	private String libraryPath;
	private String moreInfoPath;
	private String showOrphanedMembers;
	private String defaultPublishType;

	private AffiliatedSectionProvider affiliatedSectionProvider;
	private AliasService aliasService;
	private ArchiveService archiveService;
	private AuthzGroupService authzGroupService;
	private ContentHostingService contentHostingService;
	private CourseManagementService courseManagementService;
	private DeveloperHelperService devHelperService;
	private EntityManager entityManager;
	private EventTrackingService eventTrackingService;
	private FormattedText formattedText;
	private GroupProvider groupProvider;
	private IdManager idManager;
	private LTIService ltiService;
	private LinkMigrationHelper linkMigrationHelper;
	private MemoryService memoryService;
	private PreferencesService preferencesService;
	private PrivacyManager privacyManager;
	private PublishingSiteScheduleService publishingSiteScheduleService;
	private RubricsService rubricsService;
	private SectionFieldProvider sectionFieldProvider;
	private SecurityService securityService;
	private ServerConfigurationService serverConfigurationService;
	private SessionManager sessionManager;
	private ShortenedUrlService shortenedUrlService;
	private SiteService siteService;
	private SiteManageService siteManageService;
	private SiteSetupQuestionService questionService;
	private SiteTypeProvider siteTypeProvider;
	private ThreadLocalManager threadLocalManager;
	private ToolManager toolManager;
	private UnpublishingSiteScheduleService unpublishingSiteScheduleService;
	private UserAuditRegistration userAuditRegistration;
	private UserAuditService userAuditService;
	private UserDirectoryService userDirectoryService;
	private UserNotificationProvider userNotificationProvider;
	private UserTimeService userTimeService;
	private GradingService gradingService;
	private SiteTypeUtil siteTypeUtil;

	public SiteAction() {
		affiliatedSectionProvider = ComponentManager.get(AffiliatedSectionProvider.class);
		aliasService = ComponentManager.get(AliasService.class);
		archiveService = ComponentManager.get(ArchiveService.class);
		authzGroupService = ComponentManager.get(AuthzGroupService.class);
		courseManagementService = ComponentManager.get(CourseManagementService.class);
		contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		devHelperService = ComponentManager.get(DeveloperHelperService.class);
		entityManager = ComponentManager.get(EntityManager.class);
		eventTrackingService = ComponentManager.get(EventTrackingService.class);
		formattedText = ComponentManager.get(FormattedText.class);
		groupProvider = ComponentManager.get(GroupProvider.class);
		idManager = ComponentManager.get(IdManager.class);
		linkMigrationHelper = (LinkMigrationHelper) ComponentManager.get("org.sakaiproject.util.api.LinkMigrationHelper");
		ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		memoryService = ComponentManager.get(MemoryService.class);
		preferencesService = ComponentManager.get(PreferencesService.class);
		privacyManager = ComponentManager.get(PrivacyManager.class);
		publishingSiteScheduleService = ComponentManager.get(PublishingSiteScheduleService.class);
		questionService = ComponentManager.get(SiteSetupQuestionService.class);
		rubricsService = ComponentManager.get(RubricsService.class);
		sectionFieldProvider = ComponentManager.get(SectionFieldProvider.class);
		securityService = ComponentManager.get(SecurityService.class);
		serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
		sessionManager = ComponentManager.get(SessionManager.class);
		shortenedUrlService = ComponentManager.get(ShortenedUrlService.class);
		siteService = ComponentManager.get(SiteService.class);
		siteManageService = ComponentManager.get(SiteManageService.class);
		siteTypeProvider = ComponentManager.get(SiteTypeProvider.class);
		threadLocalManager = ComponentManager.get(ThreadLocalManager.class);
		toolManager = ComponentManager.get(ToolManager.class);
		unpublishingSiteScheduleService = ComponentManager.get(UnpublishingSiteScheduleService.class);
		userAuditRegistration = (UserAuditRegistration) ComponentManager.get("org.sakaiproject.userauditservice.api.UserAuditRegistration.sitemanage");
		userAuditService = ComponentManager.get(UserAuditService.class);
		userDirectoryService = ComponentManager.get(UserDirectoryService.class );
		userNotificationProvider = ComponentManager.get(UserNotificationProvider.class);
		userTimeService = ComponentManager.get(UserTimeService.class);
		gradingService = ComponentManager.get(GradingService.class);

		siteTypeUtil = new SiteTypeUtil(siteService, serverConfigurationService);

		importService = org.sakaiproject.importer.cover.ImportService.getInstance();
		comparator_locale = rb.getLocale();
		prefLocales = new ArrayList<>();

		SITE_DEFAULT_LIST = serverConfigurationService.getString("site.types");
		DEFAULT_SITE_TYPE_SAK_PROP = serverConfigurationService.getString("site.types.defaultType", null);
		PUBLIC_CHANGEABLE_SITE_TYPES_SAK_PROP = serverConfigurationService.getStrings("site.types.publicChangeable");
		PUBLIC_SITE_TYPES_SAK_PROP = serverConfigurationService.getStrings("site.types.publicOnly");
		PRIVATE_SITE_TYPES_SAK_PROP = serverConfigurationService.getStrings("site.types.privateOnly");

		showOrphanedMembers = serverConfigurationService.getString("site.setup.showOrphanedMembers", "admins");
		m_userSiteCache = memoryService.newCache("org.sakaiproject.site.api.siteService.userSiteCache");
		memoryService.destroyCache("org.sakaiproject.tool.gradebook.group.enabled");
		memoryService.destroyCache("org.sakaiproject.tool.gradebook.group.instances");

		defaultPublishType = serverConfigurationService.getString("site.setup.publish.default", SITE_PUBLISH_TYPE_MANUAL);
		if (!StringUtils.equalsAny(defaultPublishType, SITE_PUBLISH_TYPE_AUTO, SITE_PUBLISH_TYPE_SCHEDULED, SITE_PUBLISH_TYPE_MANUAL)) {
			log.warn("Default publish type is not valid [{}], setting to manual", defaultPublishType);
			defaultPublishType = SITE_PUBLISH_TYPE_MANUAL;
		}

	}

	private static final long ONE_DAY_IN_MS = 1000L * 60L * 60L * 24L;

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
		if (serverConfigurationService.getStrings("wsetup.home.toolids." + siteType) != null) {
			rv = new ArrayList(Arrays.asList(serverConfigurationService.getStrings("wsetup.home.toolids." + siteType)));
		} else if (serverConfigurationService.getStrings("wsetup.home.toolids") != null) {
			rv = new ArrayList(Arrays.asList(serverConfigurationService.getStrings("wsetup.home.toolids")));
		}
		
		// Second: if tool list is empty, get it from the template site settings
		if (rv.isEmpty())
		{
			// template site
			Site templateSite = null;
			String templateSiteId = "";
			
			if (siteService.isUserSite(site.getId()))
			{
				// myworkspace type site: get user type first, and then get the template site
				try
				{
					User user = userDirectoryService.getUser(siteService.getSiteUserId(site.getId()));
					templateSiteId = siteService.USER_SITE_TEMPLATE + "." + user.getType();
					templateSite = siteService.getSite(templateSiteId);
				}
				catch (Throwable t)
				{
	
					log.debug(this + ": getHomeToolIds cannot find site " + templateSiteId + t.getMessage());
					// use the fall-back, user template site
					try
					{
						templateSiteId = siteService.USER_SITE_TEMPLATE;
						templateSite = siteService.getSite(templateSiteId);
					}
					catch (Throwable tt)
					{
						log.debug(this + ": getHomeToolIds cannot find site " + templateSiteId + tt.getMessage());
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
					templateSiteId = siteService.SITE_TEMPLATE + "." + siteType;
					try
					{
						templateSite = siteService.getSite(templateSiteId);
					}
					catch (Throwable t)
					{
						log.debug(this + ": getHomeToolIds cannot find site " + templateSiteId + t.getMessage());
					
						// thrid: if cannot find template site with the site type, use the default template
						templateSiteId = siteService.SITE_TEMPLATE;
						try
						{
							templateSite = siteService.getSite(templateSiteId);
						}
						catch (Throwable tt)
						{
							log.debug(this + ": getHomeToolIds cannot find site " + templateSiteId + tt.getMessage());
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
							if (toolManager.getTool(toolId) != null)
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
				if (toolManager.getTool("sakai.motd") != null)
					rv.add("sakai.motd");
				
				if (rv.isEmpty())
				{
					// then try with the myworkspace information tool
					if (toolManager.getTool("sakai.iframe.myworkspace") != null)
						rv.add("sakai.iframe.myworkspace");
				}
			}
			else
			{
				// try the site information tool
				if (toolManager.getTool("sakai.iframe.site") != null)
					rv.add("sakai.iframe.site");
			}
			
			// synoptical tools
			if (toolManager.getTool(TOOL_ID_SUMMARY_CALENDAR) != null)
			{
				rv.add(TOOL_ID_SUMMARY_CALENDAR);
			}
			
			if (toolManager.getTool(TOOL_ID_SYNOPTIC_ANNOUNCEMENT) != null)
			{
				rv.add(TOOL_ID_SYNOPTIC_ANNOUNCEMENT);
			}
			
			if (toolManager.getTool(TOOL_ID_SYNOPTIC_CHAT) != null)
			{
				rv.add(TOOL_ID_SYNOPTIC_CHAT);
			}
			if (toolManager.getTool(TOOL_ID_SYNOPTIC_MESSAGECENTER) != null)
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
		String serverContextPath = serverConfigurationService.getString("config.sitemanage.moreInfoDir", "library/image/");
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
		User user = userDirectoryService.getCurrentUser();
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
			if (serverConfigurationService.getStrings("wsetup.disable.joinable") != null) {
				state.setAttribute(STATE_DISABLE_JOINABLE_SITE_TYPE,
						new ArrayList(Arrays.asList(serverConfigurationService.getStrings("wsetup.disable.joinable"))));
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
		
		if (serverConfigurationService.getStrings("site.type.titleNotEditable") != null) {
			state.setAttribute(TITLE_NOT_EDITABLE_SITE_TYPE, new ArrayList(Arrays
					.asList(serverConfigurationService.getStrings("site.type.titleNotEditable"))));
		} else {
			state.setAttribute(TITLE_NOT_EDITABLE_SITE_TYPE, new ArrayList(Arrays
					.asList(new String[]{serverConfigurationService.getString("courseSiteType", "course")})));
		}

		if (state.getAttribute(EDIT_VIEW_ROSTER_SITE_TYPE) == null) {
			List siteTypes = new Vector();
			if (serverConfigurationService.getStrings("editViewRosterSiteType") != null) {
				siteTypes = new ArrayList(Arrays
						.asList(serverConfigurationService.getStrings("editViewRosterSiteType")));
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
		state.removeAttribute(STATE_IMPORT_SITE_TOOL_ITEMS);
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
		state.removeAttribute(STATE_SITE_PARTICIPANT_LIST);
		state.removeAttribute(SITE_USER_SEARCH);
		state.removeAttribute(STATE_SITE_PARTICIPANT_FILTER);

		// SAK-24423 - remove joinable site settings from the state
		JoinableSiteSettings.removeJoinableSiteSettingsFromState( state );

		GradebookGroupEnabler.removeFromState(state);

		SubNavEnabler.removeFromState(state);

		state.removeAttribute(STATE_CREATE_FROM_ARCHIVE);

	} // cleanState

	/**
	 * Fire up the permissions editor
	 */
	public void doPermissions(RunData data, Context context) {
		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.permissions.helper");

		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String contextString = toolManager.getCurrentPlacement().getContext();
		String siteRef = siteService.siteReference(contextString);

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
				+ " " + siteService.getSiteDisplay(contextString));

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
		context.put("tlang", rb);
		context.put("clang", cfgRb);
		context.put("userTimeService", userTimeService);
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

		ToolSession session = sessionManager.getCurrentToolSession();
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

		log.debug("buildMainPanelContext template={}", template);
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
		context.put("isExpandTemplates", serverConfigurationService.getBoolean("site.setup.creation.expand.template", false));

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

		// all site types
		context.put("courseSiteTypeStrings", siteService.getSiteTypeStrings("course"));
		context.put("projectSiteTypeStrings", siteService.getSiteTypeStrings("project"));
		
		//can the user create course sites?
		context.put(STATE_SITE_ADD_COURSE, siteService.allowAddCourseSite());
		
		// can the user create project sites?
		context.put("projectSiteType", STATE_PROJECT_SITE_TYPE);
		context.put(STATE_SITE_ADD_PROJECT, siteService.allowAddProjectSite());
		
		// can the user user create sites from archives?
		context.put(STATE_SITE_IMPORT_ARCHIVE, siteService.allowImportArchiveSite());

		Site site = getStateSite(state);

		List unJoinableSiteTypes = (List) state.getAttribute(STATE_DISABLE_JOINABLE_SITE_TYPE);

		log.debug("buildContextForTemplate index={}", index);

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
			if (serverConfigurationService.getBoolean("site.soft.deletion", true)) {
				views.put(SITE_TYPE_DELETED, rb.getString("java.sites.deleted"));
				if (SITE_TYPE_DELETED.equals(state.getAttribute(STATE_VIEW_SELECTED))) {
					context.put("canSeeSoftlyDeletedSites", true);
				}
			}

			// Add the menus to the vm
			MenuBuilder.buildMenuForWorksiteSetup(portlet, data, state, context, rb);

			// If we're in the restore view
			context.put("showRestore", SITE_TYPE_DELETED.equals(state.getAttribute(STATE_VIEW_SELECTED)));

			boolean isSuperUser = securityService.isSuperUser();
			if (isSuperUser) {
				context.put("superUser", Boolean.TRUE);
				context.put("canDelSoftDel", Boolean.TRUE);
			} else {
				context.put("superUser", Boolean.FALSE);
			}
			context.put("viewDeleted", SITE_TYPE_DELETED);
			views.put(SITE_TYPE_ALL, rb.getString("java.allmy"));
			views.put(SITE_TYPE_MYWORKSPACE, rb.getFormattedMessage("java.sites", rb.getString("java.my")));
			for (int sTypeIndex = 0; sTypeIndex < sTypes.size(); sTypeIndex++) {
				String type = (String) sTypes.get(sTypeIndex);
				views.put(type, rb.getFormattedMessage("java.sites", type));
			}
			List<String> moreTypes = siteTypeProvider.getTypesForSiteList();
			if (!moreTypes.isEmpty())
			{
				for(String mType : moreTypes)
				{
					views.put(mType, rb.getFormattedMessage("java.sites", mType));
				}
			}
				// Allow SuperUser to see all deleted sites.
				if (serverConfigurationService.getBoolean("site.soft.deletion", true)) {
					views.put(SITE_TYPE_DELETED, rb.getString("java.sites.deleted"));
				}

			// default view
			if (state.getAttribute(STATE_VIEW_SELECTED) == null) {
				state.setAttribute(STATE_VIEW_SELECTED, SITE_TYPE_ALL);
			}

			if (serverConfigurationService.getBoolean("sitesetup.show.unpublished", false) && !securityService.isSuperUser()) {
				views.put(SITE_INACTIVE, rb.getString("java.myInactive"));
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
			
			// SAK-23256
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
				
				if ( serverConfigurationService.getBoolean(SAK_PROP_AUTO_FILTER_TERM, Boolean.FALSE)) {
					// SAK-28059 auto filter term to use the most current term
					List<AcademicSession> currentTerms = courseManagementService.getCurrentAcademicSessions();
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

			String portalUrl = serverConfigurationService.getPortalUrl();
			context.put("portalUrl", portalUrl);

			List<Site> allSites = prepPage(state);
			state.setAttribute(STATE_SITES, allSites);
			context.put("sites", allSites);

			if (!isSuperUser) {
				boolean canDelSoftDel = false;
				for (Site s : allSites) {
					canDelSoftDel = securityService.unlock("site.del.softly.deleted", s.getReference());
					if (canDelSoftDel) {
						break;
					}
				}

				context.put("canDelSoftDel", canDelSoftDel);
			}

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
			context.put("service", siteService);
			context.put("sortby_title", SortType.TITLE_ASC.toString());
			context.put("sortby_id", SortType.ID_ASC.toString());
			context.put("show_id_column", serverConfigurationService.getBoolean("site.setup.showSiteIdColumn", false));
			context.put("sortby_type", SortType.TYPE_ASC.toString());
			context.put("sortby_createdby", SortType.CREATED_BY_ASC.toString());
			context.put("sortby_publish", SortType.PUBLISHED_ASC.toString());
			context.put("sortby_createdon", SortType.CREATED_ON_ASC.toString());
			context.put("sortby_softlydeleted", SortType.SOFTLY_DELETED_ASC.toString());

			// default to be no paging
			context.put("paged", Boolean.FALSE);

			pagingInfoToContext(state, context);

			//Add flash notification when new site is created
			if(state.getAttribute(STATE_NEW_SITE_STATUS_ID) != null){
				String siteTitle = formattedText.escapeHtml((String)state.getAttribute(STATE_NEW_SITE_STATUS_TITLE));
				String  flashNotifMsg = "<a title=\"" + siteTitle + "\"href=\"/portal/site/"+
				state.getAttribute(STATE_NEW_SITE_STATUS_ID) + "\" target=\"_top\">"+
				siteTitle+"</a>" +" "+
				rb.getString("sitdup.hasbeedup");
				addFlashNotif(state,flashNotifMsg);
				StringBuilder sbFlashNotifAction =  new StringBuilder();
				if (state.getAttribute(STATE_NEW_SITE_STATUS_ISPUBLISHED).equals(Boolean.FALSE)) {
					sbFlashNotifAction = new StringBuilder();
					sbFlashNotifAction.append("<div id=\"newSiteAlertActions\" class=\"newSiteAlertActions\">");
					sbFlashNotifAction.append("<a href=\"#\" id=\"newSiteAlertPublish\" class=\""+state.getAttribute(STATE_NEW_SITE_STATUS_ID)+"\""+">" + rb.getString("sitetype.publishSite") + "</a>");
					sbFlashNotifAction.append("<span id=\"newSiteAlertPublishMess\" style=\"display:none\">" + rb.getString("list.publi") + "</span>");
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
			context.put("templateControls", serverConfigurationService.getString("site.setup.templateControls",serverConfigurationService.getString("templateControls", "")));
			// put selected/default site type into context
			String typeSelected = (String) state.getAttribute(STATE_TYPE_SELECTED);
			context.put("typeSelected", state.getAttribute(STATE_TYPE_SELECTED) != null?state.getAttribute(STATE_TYPE_SELECTED):types.get(0));
			
			// SAK-23256
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

			Map<String, List<MyTool>> groupTools = getTools(state, type, site);
			state.setAttribute(STATE_TOOL_GROUP_LIST, groupTools);

			// information related to LTI tools
			buildLTIToolContextForTemplate(context, state, site, true);
			
			if (securityService.isSuperUser()) {
				context.put("superUser", Boolean.TRUE);
			} else {
				context.put("superUser", Boolean.FALSE);
			}
			
			
			// save all lists to context
			pageOrderToolTitleIntoContext(context, state, type, (site == null), site == null ? null : site.getProperties().getProperty(SITE_PROPERTY_OVERRIDE_HIDE_PAGEORDER_SITE_TYPES));
			Boolean checkToolGroupHome = (Boolean) state.getAttribute(STATE_TOOL_HOME_SELECTED);

			context.put("check_home", checkToolGroupHome);
			context.put("ltitool_id_prefix", LTITOOL_ID_PREFIX);
			context.put("serverName", serverConfigurationService.getServerName());
			context.put("sites", siteService.getSites(SelectionType.UPDATE, null, null, null, SortType.TITLE_ASC, null));
			context.put("import", state.getAttribute(STATE_IMPORT));
			context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));
			context.put(MathJaxEnabler.CONTEXT_MATHJAX_HELP_URL, MathJaxEnabler.HELP_URL);
			if (site != null)
			{
				// Add the menus to vm
				MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.MANAGE_TOOLS);

				MathJaxEnabler.addMathJaxSettingsToEditToolsContext(context, site, state);  // SAK-22384
				SubNavEnabler.addToContext(context, site);
				context.put("SiteTitle", site.getTitle());
				context.put("existSite", Boolean.TRUE);
				context.put("backIndex", SITE_INFO_TEMPLATE_INDEX);	// back to site info list page
			}
			else
			{
				context.put("existSite", Boolean.FALSE);
				context.put("backIndex", "13");	// back to new site information page
			}
			context.put("homeToolId", TOOL_ID_HOME);
			context.put("toolsByGroup", (LinkedHashMap<String,List>) state.getAttribute(STATE_TOOL_GROUP_LIST));
			
			context.put("toolGroupMultiples", getToolGroupMultiples(state, (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST)));
			
			//get expanded groups
			List<String> expandedGroups_lst = new ArrayList<>();
			String[] tokens = serverConfigurationService.getStrings("sitemanage.tools.groups.expanded");
			if(tokens != null) {
				for(String token : tokens) {
					if(StringUtils.isNotEmpty(token)) {
						String groupName = getGroupName(token);
						if(StringUtils.isNotEmpty(groupName)) {
							expandedGroups_lst.add(groupName);
						}
					}
				}
			}
			context.put("expandedGroups", expandedGroups_lst);

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
			String user = sessionManager.getCurrentSessionUserId();
			String workspace = siteService.getUserSiteId(user);
			// Are we attempting to softly delete a site.
			boolean softlyDeleting = serverConfigurationService.getBoolean("site.soft.deletion", true);
			boolean hardDeleting = false;
			if (removals != null && removals.length != 0) {
				for (int i = 0; i < removals.length; i++) {
					String id = (String) removals[i];
					if (!(id.equals(workspace))) {
						if (siteService.allowRemoveSite(id)) {
							try {
								// check whether site exists
								Site removeSite = siteService.getSite(id);
								
								//check site isn't already softly deleted
								if(softlyDeleting && removeSite.isSoftlyDeleted()) {
									softlyDeleting = false;
									hardDeleting = true;
								}
								remove.add(removeSite);
							} catch (IdUnusedException e) {
								log.warn(this + "buildContextForTemplate chef_site-siteDeleteConfirm.vm - IdUnusedException " + id + e.getMessage());
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
				//SAK-29678 - If it's hard deleted, it's not soft deleted.
				softlyDeleting = false;
				hardDeleting =true;
			}
			
			//check if soft deletes are activated
			context.put(STATE_SOFT_DELETE, softlyDeleting);
			context.put(STATE_HARD_DELETE, hardDeleting);
			state.setAttribute(STATE_HARD_DELETE, String.valueOf(hardDeleting));

			return (String) getContext(data).get("template") + TEMPLATE[8];
		case 10:
			/*
			 * buildContextForTemplate chef_site-newSiteConfirm.vm
			 * 
			 */
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			String siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (siteTypeUtil.isCourseSite(siteType)) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("disableCourseSelection", serverConfigurationService.getString("disable.course.site.skin.selection", "false").equals("true")?Boolean.TRUE:Boolean.FALSE);
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
				if (siteTypeUtil.isProjectSite(siteType)) {
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
 			if("".equals( locale_string_selected )  || locale_string_selected == null)		
 				context.put("locale_string_selected", "");			
 			else
 			{
 				Locale locale_selected = getLocaleFromString(locale_string_selected);
 				context.put("locale_string_selected", locale_selected);
 			}

 			// put tool selection into context
			toolSelectionIntoContext(context, state, siteType, null, null, 10);
			
			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));
			context
					.put("emailId", state
							.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			context.put("serverName", serverConfigurationService.getServerName());
			context.put("include", Boolean.valueOf(siteInfo.include));
			context.put("published", Boolean.valueOf(siteInfo.published));
			context.put("joinable", Boolean.valueOf(siteInfo.joinable));
			context.put("joinerRole", siteInfo.joinerRole);
			context.put("additionalAccess", getAdditionRoles(siteInfo));

			// SAK-24423 - add joinable site settings to context
			JoinableSiteSettings.addJoinableSiteSettingsToNewSiteConfirmContext( context, siteInfo );

			context.put("importSiteTools", state
					.getAttribute(STATE_IMPORT_SITE_TOOL));
			context.put("siteService", siteService);

			// those manual inputs
			context.put("form_requiredFields", sectionFieldProvider
					.getRequiredFields());
			context.put("fieldValues", state
					.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
			
			context.put("fromArchive", state.getAttribute(STATE_UPLOADED_ARCHIVE_NAME));

			return (String) getContext(data).get("template") + TEMPLATE[10];
		case STATE_TEMPLATE_INDEX_MANAGE_PARTICIPANTS_INT:
			/*
			 * buildContextForTemplate chef_site-siteInfo-manageParticipants.vm
			 */

			// Put the link for downloading participant list PDF into the context
			putDownloadParticipantPDFLinkIntoContext(context, data, site);

			boolean allowUpdateSiteMembership = siteService.allowUpdateSiteMembership(site.getId());
			boolean allowUpdateSite = siteService.allowUpdateSite(site.getId());
			boolean allowViewRoster = siteService.allowViewRoster(site.getId());
			boolean isMyWorkspace = isSiteMyWorkspace(site);

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.MANAGE_PARTICIPANTS);

			context.put("userSearch", state.getAttribute(SITE_USER_SEARCH));

			// Put the filter entries into the context
			ParticipantFilterHandler.putFilterEntriesInContext(context, rb, site);

			if (site.getProviderGroupId() != null) {
				log.debug("site has provider");
				context.put("hasProviderSet", Boolean.TRUE);
			} else {
				log.debug("site has no provider");
				context.put("hasProviderSet", Boolean.FALSE);
			}

			if (isMyWorkspace) {
				context.put("siteUserId", siteService.getSiteUserId(site.getId()));
			}

			context.put("allowUpdateSiteMembership", allowUpdateSiteMembership);
			context.put("isMyWorkspace", isMyWorkspace);
			context.put("siteTitle", site.getTitle());
			context.put("isCourseSite", siteTypeUtil.isCourseSite(site.getType()));

			// Set participant list
			if (allowUpdateSite || allowViewRoster || allowUpdateSiteMembership) {
				Collection<Participant> participantsCollection = getParticipantList(state);
				sortedBy = (String) state.getAttribute(SORTED_BY);
				sortedAsc = (String) state.getAttribute(SORTED_ASC);
				if (sortedBy == null) {
					state.setAttribute(SORTED_BY, SORTED_BY_PARTICIPANT_NAME);
					sortedBy = SORTED_BY_PARTICIPANT_NAME;
				}
				if (sortedAsc == null) {
					sortedAsc = Boolean.TRUE.toString();
					state.setAttribute(SORTED_ASC, sortedAsc);
				}

				context.put("currentSortedBy", sortedBy);
				context.put("currentSortAsc", sortedAsc);
				context.put("participantListSize", participantsCollection.size());
				context.put("participantList", prepPage(state));

				boolean hasCredits = participantsCollection.stream().anyMatch(p -> StringUtils.isNotEmpty(p.getCredits()));
				context.put("hasCredits", hasCredits);

				ParticipantFilterHandler.putSelectedFilterIntoContext(state, context);

				pagingInfoToContext(state, context);
			}

			// SAK-23257 - add the allowed roles to the context for UI rendering
			List<Role> allRoles = getRoles(state);
			context.put(VM_ALLOWED_ROLES_DROP_DOWN, SiteParticipantHelper.getAllowedRoles(site.getType(), allRoles));
			context.put("allRoles", allRoles);

			// Will have the choice to active/inactive user or not
			context.put("activeInactiveUser", serverConfigurationService.getBoolean("activeInactiveUser", false));

			context.put("showEnrollmentStatus", serverConfigurationService.getBoolean(
				"sitemanage.manageParticipants.showEnrollmentStatus", false));

			// Provide last modified time
			realmId = siteService.siteReference(site.getId());
			try {
				AuthzGroup realm = authzGroupService.getAuthzGroup(realmId);
				context.put("realmModifiedTime",getDateFormat(realm.getModifiedDate()));
			} catch (GroupNotDefinedException e) {
				log.warn("{} IdUnusedException {}", this, realmId);
			}

			return (String) getContext(data).get("template") + TEMPLATE[STATE_TEMPLATE_INDEX_MANAGE_PARTICIPANTS_INT];
		case 12:
			/*
			 * buildContextForTemplate chef_site-siteInfo-list.vm
			 * 
			 */
			context.put("searchString", state.getAttribute(STATE_SEARCH));

			// Site modified by information
			User siteModifiedBy = site.getModifiedBy();
			Date siteModifiedTime = site.getModifiedDate();
			if( siteModifiedBy != null )
			{
				context.put( "siteModifiedBy", siteModifiedBy.getSortName() );
			}
			if( siteModifiedTime != null )
			{
				context.put( "siteModifiedTime", getDateFormat(siteModifiedTime));
			}

			try {
				siteType = site.getType();
				if (siteType != null) {
					state.setAttribute(STATE_SITE_TYPE, siteType);
				}

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
						addFlashNotif(state, rb.getString("java.duplicate") + " " + flashNotifMsg + " " + rb.getString("sitdup.hasbeedup"));
					}
				state.removeAttribute(SITE_DUPLICATED);
				state.removeAttribute(SITE_DUPLICATED_NAME);
				
				context.put("siteFriendlyUrls", getSiteUrlsForSite(site));
				context.put("siteDefaultUrl", getDefaultSiteUrl(siteId));
				
				context.put("siteId", site.getId());
				context.put("siteIcon", site.getIconUrl());
				context.put("siteTitle", site.getTitle());
				context.put("siteDescription", site.getDescription());
				if (unJoinableSiteTypes != null && !unJoinableSiteTypes.contains(siteType))
				{
					context.put("siteJoinable", Boolean.valueOf(site.isJoinable()));
					context.put("allowUnjoin", siteService.allowUnjoinSite(site.getId()));
				}

				if (site.isPublished()) {
					context.put("published", Boolean.TRUE);
				} else {
					context.put("published", Boolean.FALSE);
				}
				Date creationTime = site.getCreatedDate();
				if (creationTime != null) {
					context.put("siteCreationDate", getDateFormat(creationTime));
				}

				ResourceProperties siteProperties = site.getProperties();

				allowUpdateSite = siteService.allowUpdateSite(site.getId());
				isMyWorkspace = isSiteMyWorkspace(site);
				boolean allowUpdateGroupMembership = siteService.allowUpdateGroupMembership(site.getId());
				allowViewRoster = siteService.allowViewRoster(site.getId());

				context.put("allowUpdate", allowUpdateSite);
				context.put("additionalAccess", getAdditionRoles(site));
				context.put("isMyWorkspace", isMyWorkspace);
				context.put("viewRoster", allowViewRoster);

				// Add the menus to vm
				MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.SITE_INFO);

				User currentUser = userDirectoryService.getCurrentUser();
				if(state.getAttribute(IMPORT_QUEUED) != null){
					context.put("importQueued", true);
					state.removeAttribute(IMPORT_QUEUED);
					if(StringUtils.isBlank(currentUser.getEmail()) || !serverConfigurationService.getBoolean(SiteManageConstants.SAK_PROP_IMPORT_NOTIFICATION, true)){
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
				
				if (siteTypeUtil.isCourseSite(siteType)) {
					context.put("isCourseSite", Boolean.TRUE);
					
					coursesIntoContext(state, context, site);
					
					context.put("term", siteProperties
							.getProperty(Site.PROP_SITE_TERM));
				} else {
					context.put("isCourseSite", Boolean.FALSE);
				}
				
				Collection<Group> groups = null;
				if (serverConfigurationService.getBoolean("wsetup.group.support.summary", true))
				{
					if ((allowUpdateSite || allowUpdateGroupMembership) 
							&& (!isMyWorkspace && serverConfigurationService.getBoolean(SAK_PROP_SITE_SETUP_GROUP_SUPPORT, SAK_PROP_SITE_SETUP_GROUP_SUPPORT_DEFAULT)))
					{
						// show all site groups
						groups = site.getGroups();
					}
					else
					{
						// show groups that the current user is member of
						groups = site.getGroupsWithMember(currentUser.getId());
					}
				}
				if (groups != null)
				{
					// filter out only those groups that are manageable by site-info
					List<Group> filteredGroups = new ArrayList<Group>();
					List<Group> filteredSections = new ArrayList<Group>();
					Collection<String> viewMembershipGroups = new ArrayList<String>();

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
					}

					context.put("viewMembershipGroups", viewMembershipGroups);

					Collections.sort(filteredGroups, new GroupTitleComparator());
					context.put("groups", filteredGroups);

					Collections.sort(filteredSections, new GroupTitleComparator());
					context.put("sections", filteredSections);
				}

				Set<JoinableGroup> joinableGroups = new HashSet<>();
				Set<JoinableGroup> joinedGroups = new HashSet<>();
				if(site.getGroups() != null){
					//find a list of joinable-sets this user is already a member of
					//in order to not display those groups as options
					Set<String> joinableSetsMember = new HashSet<String>();
					String userId = userDirectoryService.getCurrentUser().getId();
					for(Group group : site.getGroupsWithMember(userId)){
						String joinableSet = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);
						if(joinableSet != null && !"".equals(joinableSet.trim())){
							joinableSetsMember.add(joinableSet);
						}
					}
					for (Group group : site.getGroups()) {
						String joinableSetProp = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);

						if (StringUtils.isNotBlank(joinableSetProp)) {
							JoinableGroup joinableGroup = new JoinableGroup(group);
							String joinableOpenDate = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_OPEN_DATE);
							String joinableCloseDate = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_CLOSE_DATE);
							boolean isInteractableByDate = true;
							// Make the corresponding checks if the current joinableSet has any associated datetime.
							if (joinableOpenDate != null || joinableCloseDate != null) {
								LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);
								LocalDateTime openDate = null;
								LocalDateTime closeDate = null;
								// Compare openDate & closeDate with currentDate to determine if the jGroup is joinable/unjoinable
								if (joinableCloseDate != null) {
									closeDate = LocalDateTime.parse(joinableCloseDate);
									joinableGroup.setJoinableCloseDate(userTimeService.dateFromUtcToUserTimeZone(joinableCloseDate, true));
								}

								if (joinableOpenDate != null) {
									openDate = LocalDateTime.parse(joinableOpenDate);
									joinableGroup.setJoinableOpenDate(userTimeService.dateFromUtcToUserTimeZone(joinableOpenDate, true));

									if (closeDate != null) {
										// If both openDate & closeDate are available, make sure currenData is between them to display the group.
										if (currentDate.isBefore(openDate) || currentDate.isAfter(closeDate)) {
											isInteractableByDate = false;
										}
										// If only openDate available, check it's before currentDate.
									} else if (currentDate.isBefore(openDate)) {
										isInteractableByDate = false;
									}
									// If only closeDate available, check it's after currentDate.
								} else if (closeDate != null && currentDate.isAfter(closeDate)) {
									isInteractableByDate = false;
								}
							}
							// Add jGroup if the current user IS part of it, or has the right permission (e.g. admin).
							Member groupMember = group.getMember(currentUser.getId());
							if (groupMember != null || allowViewRoster) {
								boolean isEnrolled = true;
								// To let admin user know why the unjoin button might not be displayed.
								if (allowViewRoster && groupMember == null) {
									isEnrolled = false;
								}
								joinableGroup.setEnrolled(isEnrolled);
								// Unjoin button will be enabled if unjoinable property & datetimes allow for it.
								joinableGroup.setInteractableByDate(isInteractableByDate);
								joinedGroups.add(joinableGroup);
							}
							// Add jGroup if the current user IS NOT part of it.
							if (!joinableSetsMember.contains(joinableSetProp)) {
								joinableGroup.setInteractableByDate(isInteractableByDate);
								joinableGroups.add(joinableGroup);
							}
						}
					}
				}
				context.put("joinedJoinableGroups", new ArrayList<>(joinedGroups));

				List<JoinableGroup> sortedJoinableGroups = joinableGroups.stream()
						.sorted((g1, g2) -> new AlphaNumericComparator().compare(g1.getTitle(), g2.getTitle()))
						.collect(Collectors.toList());
				context.put("joinableGroups", sortedJoinableGroups);
				
			} catch (Exception e) {
				log.error(this + " buildContextForTemplate chef_site-siteInfo-list.vm ", e);
			}

			// SAK-22384 mathjax support
			MathJaxEnabler.addMathJaxSettingsToSiteInfoContext(context, site, state);
			context.put("isGradebookGroupEnabledForSite", GradebookGroupEnabler.isEnabledForSite(site));
			SubNavEnabler.addToContext(context, site);

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

				// Add the menus to vm
				MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.EDIT_SITE_INFO);
			} else {
				// new site
				context.put("existingSite", Boolean.FALSE);

				// Are we creating from a template?
				if (state.getAttribute(STATE_TEMPLATE_SITE) != null) {
					context.put("continue", "18");
				} else {
					context.put("continue", "4");
				}
				
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

			if (siteTypeUtil.isCourseSite(siteType)) {
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
				if (siteTypeUtil.isProjectSite(siteType)) {
					context.put("isProjectSite", Boolean.TRUE);
				}
			}

			// about skin and icon selection
			skinIconSelection(context, state, siteTypeUtil.isCourseSite(siteType), site, siteInfo);

			// those manual inputs
			context.put("form_requiredFields", sectionFieldProvider.getRequiredFields());
			context.put("fieldValues", state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
			
			context.put("title", siteInfo.title);
			context.put(FORM_SITE_URL_BASE, getSiteBaseUrl());
			context.put(FORM_SITE_ALIAS, siteInfo.getFirstAlias());
			context.put(FORM_ICON_URL, siteInfo.iconUrl);
			context.put("description", siteInfo.description);
			context.put("short_description", siteInfo.short_description);
			context.put("form_site_contact_name", siteInfo.site_contact_name);
			context.put("form_site_contact_email", siteInfo.site_contact_email);
			
			context.put("site_aliases", state.getAttribute(FORM_SITEINFO_ALIASES));
			context.put("site_url_base", state.getAttribute(FORM_SITEINFO_URL_BASE));
			context.put("site_aliases_editable", aliasesEditable(state, site == null ? null : site.getId()));
			context.put("site_alias_assignable", aliasAssignmentForNewSitesEnabled(state));

			// available languages in sakai.properties
			List locales = getPrefLocales();	
			context.put("locales",locales);

			// SAK-22384 mathjax support
			MathJaxEnabler.addMathJaxSettingsToSiteInfoContext(context, site, state);
			context.put("isGradebookGroupEnabledForSite", GradebookGroupEnabler.isEnablingForSite(state));

			SubNavEnabler.addToContext(context, site);

			return (String) getContext(data).get("template") + TEMPLATE[13];
		case 14:
			/*
			 * buildContextForTemplate chef_site-siteInfo-editInfoConfirm.vm
			 * 
			 */
			ResourceProperties siteProperties = null;
			if (site != null) {
				siteProperties = site.getProperties();

				// Add the menus to vm
				MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.EDIT_SITE_INFO);
			}
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
			context.put("displaySiteAlias", Boolean.valueOf(displaySiteAlias()));
			siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (siteTypeUtil.isCourseSite(siteType)) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("siteTerm", siteInfo.term);
			} else {
				context.put("isCourseSite", Boolean.FALSE);
			}
			// about skin and icon selection
			skinIconSelection(context, state, siteTypeUtil.isCourseSite(siteType), site, siteInfo);
			
			context.put("oTitle", site.getTitle());
			context.put("title", siteInfo.title);
			
			// get updated language
			String new_locale_string = (String) state.getAttribute("locale_string");
			if(StringUtils.isBlank(new_locale_string))
				context.put("new_locale", "");
			else
			{
				Locale new_locale = getLocaleFromString(new_locale_string);
				context.put("new_locale", new_locale);
			}
						
			// get site language saved
			ResourcePropertiesEdit props = site.getPropertiesEdit();
			String oLocale_string = props.getProperty(PROP_SITE_LANGUAGE);
			if(StringUtils.isBlank(oLocale_string))
				context.put("oLocale", "");
			else
			{
				Locale oLocale = getLocaleFromString(oLocale_string);
				context.put("oLocale", oLocale);
			}

			context.put("description", siteInfo.description);
			context.put("descriptionUpdated", !StringUtils.equals(StringUtils.strip(site.getDescription()), StringUtils.strip(siteInfo.description)));
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
			SubNavEnabler.addToContext(context, site);

			return (String) getContext(data).get("template") + TEMPLATE[14];
		case 15:
			/*
			 * buildContextForTemplate chef_site-addRemoveFeatureConfirm.vm
			 * 
			 */
			context.put("title", site.getTitle());

			site_type = (String) state.getAttribute(STATE_SITE_TYPE);
			if (isSiteMyWorkspace(site)) {
				site_type = "myworkspace";
			}

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.MANAGE_TOOLS);

			String overridePageOrderSiteTypes = site.getProperties().getProperty(SITE_PROPERTY_OVERRIDE_HIDE_PAGEORDER_SITE_TYPES);
			// put tool selection into context
			toolSelectionIntoContext(context, state, site_type, site.getId(), overridePageOrderSiteTypes, 15);
			MathJaxEnabler.addMathJaxSettingsToEditToolsConfirmationContext(context, site, state, STATE_TOOL_REGISTRATION_TITLE_LIST);  // SAK-22384
			GradebookGroupEnabler.addSettingsToEditToolsConfirmationContext(context, site, state);
			SubNavEnabler.addStateToEditToolsConfirmationContext(context, state);

			return (String) getContext(data).get("template") + TEMPLATE[15];
		case 18:
			/*
			 * buildContextForTemplate chef_siteInfo-editAccess.vm
			 * 
			 */
			List publicChangeableSiteTypes = (List) state
					.getAttribute(STATE_PUBLIC_CHANGEABLE_SITE_TYPES);

			context.put("authAllowed", serverConfigurationService.getBoolean("sitemanage.grant.auth", false));
			context.put("anonAllowed", serverConfigurationService.getBoolean("sitemanage.grant.anon", false));

			int daysbefore = serverConfigurationService.getInt("course_site_publish_service.num_days_before_term_starts", 0);
			int daysafter = serverConfigurationService.getInt("course_site_removal_service.num_days_after_term_ends", 14);
			if(daysbefore > 0){
				context.put("daysbefore", daysbefore);
			}
			if(daysafter > 0){
				context.put("daysafter",daysafter);
			}
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			if (site != null) {
				// editing existing site
				context.put("site", site);
				siteType = state.getAttribute(STATE_SITE_TYPE) != null ? (String) state
						.getAttribute(STATE_SITE_TYPE)
						: null;

				// Add the menus to the vm
				MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.MANAGE_ACCESS);

				if (siteType != null
						&& publicChangeableSiteTypes.contains(siteType)) {
					context.put("publicChangeable", Boolean.TRUE);
				} else {
					context.put("publicChangeable", Boolean.FALSE);
				}
				context.put("published", state.getAttribute(STATE_SITE_ACCESS_PUBLISH));
				context.put("include", state.getAttribute(STATE_SITE_ACCESS_INCLUDE));
				context.put("sitetype", siteType);
				if (StringUtils.isBlank(site.getProperties().getProperty(SITE_PUBLISH_TYPE))) {
					// default to Manual for older sites or if it became null another way
					context.put("publishType", defaultPublishType);
				} else {
					context.put("publishType", site.getProperties().getProperty(SITE_PUBLISH_TYPE));
				}
				Date termPublishDate = new Date();
				try{
					AcademicSession academicSession = courseManagementService.getAcademicSession(site.getProperties().getProperty(Site.PROP_SITE_TERM_EID));
					long courseStartTime = academicSession.getStartDate().getTime();
					long courseEndTime = academicSession.getEndDate().getTime();
					ZoneId localZoneId = userTimeService.getLocalTimeZone().toZoneId();

					termPublishDate = new Date(courseStartTime - (ONE_DAY_IN_MS * daysbefore));

					ZonedDateTime termStartDate = termPublishDate.toInstant().atZone(localZoneId);
					ZonedDateTime termEndDate = new Date(courseEndTime).toInstant().atZone(localZoneId);
					ZonedDateTime termUnpublishDate = new Date(courseEndTime + (ONE_DAY_IN_MS * daysafter)).toInstant().atZone(localZoneId);

					context.put("termStartDate", termStartDate.format(formatter));
					context.put("termEndDate", termEndDate.format(formatter));
					context.put("termUnpublishDate", termUnpublishDate.format(formatter));
					context.put("readableTermStartDate", userTimeService.dateFormat(termPublishDate, rb.getLocale(), DateFormat.LONG));	//create readable versions of all dates
					context.put("readableTermStartDateTime", userTimeService.dateTimeFormat(termPublishDate, rb.getLocale(), DateFormat.SHORT));
					context.put("readableTermEndDate", userTimeService.dateFormat(academicSession.getEndDate(), rb.getLocale(), DateFormat.LONG));
					context.put("readableTermUnpublishDate", userTimeService.dateFormat(new Date(courseEndTime + (ONE_DAY_IN_MS * daysafter)), rb.getLocale(), DateFormat.LONG));
					context.put("readableTermUnpublishDateTime", userTimeService.dateTimeFormat(new Date(courseEndTime + (ONE_DAY_IN_MS * daysafter)), rb.getLocale(), DateFormat.SHORT));
				} catch(IdNotFoundException i) {	//no session ID means this is Project, or term-free

				}

				context.put("shoppingPeriodInstructorEditable", serverConfigurationService.getBoolean("delegatedaccess.shopping.instructorEditable", false));
				context.put("viewDelegatedAccessUsers", serverConfigurationService.getBoolean("delegatedaccess.siteaccess.instructorViewable", false));

				if(site.getProperties().getProperty(SITE_UNPUBLISH_DATE) != null) {
					context.put("readableUnpublishDate", userTimeService.dateTimeFormat(userTimeService.parseISODateInUserTimezone(site.getProperties().getProperty(SITE_UNPUBLISH_DATE)), rb.getLocale(), DateFormat.SHORT));
					context.put("unpublishDate", site.getProperties().getProperty(SITE_UNPUBLISH_DATE));
				} else {
					context.put("readableUnpublishDate", "");
					context.put("unpublishDate", "");
				}

				String sitePublishDate = site.getProperties().getProperty(SITE_PUBLISH_DATE);
				String readablePublishDate = "";
				Date publishingDate = new Date();

				if(sitePublishDate != null){
					publishingDate = userTimeService.parseISODateInUserTimezone(String.valueOf(sitePublishDate));
					readablePublishDate = userTimeService.dateTimeFormat(publishingDate, rb.getLocale(), DateFormat.SHORT);
					context.put("publishDate", sitePublishDate);
				}
				else {
					context.put("publishDate", "");
				}

				context.put("readablePublishDate", readablePublishDate);

				if(site.isPublished()){
					context.put("existingStatus", "Published");
					context.put("statusLabel", rb.getString("list.publi"));
				} else if(StringUtils.equals(site.getProperties().getProperty(SITE_PUBLISH_TYPE), SITE_PUBLISH_TYPE_SCHEDULED) && publishingDate.toInstant().isAfter(Instant.now())){
					context.put("existingStatus", "Scheduled");
					context.put("statusLabel", rb.getString("pubuncon.sched") + ' ' + readablePublishDate);
				} else if (StringUtils.equals(site.getProperties().getProperty(SITE_PUBLISH_TYPE), SITE_PUBLISH_TYPE_AUTO)
						&& termPublishDate.toInstant().isAfter(Instant.now())) {
					context.put("existingStatus", "Scheduled");
					context.put("statusLabel", rb.getString("pubuncon.auto") + ' ' + userTimeService.dateFormat(termPublishDate, rb.getLocale(), DateFormat.LONG));
				} else {
					context.put("existingStatus", "Unpublished");
					context.put("statusLabel", rb.getString("list.unpub"));
				}
				// SAK-24423 - add joinable site settings to context
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

				// SAK-23257
				context.put("roles", getJoinerRoles(site.getReference(), state, site.getType()));
			} else {
				// In the site creation process...
				siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
				// the template site, if using one
				Site templateSite = (Site) state.getAttribute(STATE_TEMPLATE_SITE);

				if (siteInfo.site_type != null && publicChangeableSiteTypes.contains(siteInfo.site_type)) {
					context.put("publicChangeable", Boolean.TRUE);
				} else {
					context.put("publicChangeable", Boolean.FALSE);
				}
				context.put("include", Boolean.valueOf(siteInfo.getInclude()));

				// If this site is a course site, publish if we're inside the term dates
				if (siteInfo.site_type != null && siteTypeUtil.isCourseSite(siteInfo.site_type) && templateSite == null) {
					AcademicSession academicSession = courseManagementService.getAcademicSession(siteInfo.term);
					if (Instant.now().isAfter(new Date(academicSession.getStartDate().getTime()).toInstant())
						&& Instant.now().isBefore(new Date(academicSession.getEndDate().getTime()).toInstant())) {
						// We are currently inside the term dates, so publish.
						context.put("published", true);
					} else {
						context.put("published", false);
					}
				} else {
					context.put("published", true);
				}
				context.put("sitetype", siteInfo.site_type);
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
				
				// SAK-24423 - add joinable site settings to context
				JoinableSiteSettings.addJoinableSiteSettingsToEditAccessContextWhenSiteIsNull( context, siteInfo, true );

				try {
					AcademicSession academicSession = courseManagementService.getAcademicSession(siteInfo.term);
					long courseStartTime = academicSession.getStartDate().getTime();
					long courseEndTime = academicSession.getEndDate().getTime();
					ZoneId localZoneId = userTimeService.getLocalTimeZone().toZoneId();

					ZonedDateTime termStartDate = new Date(courseStartTime - (ONE_DAY_IN_MS * daysbefore)).toInstant().atZone(localZoneId);
					ZonedDateTime termEndDate = new Date(courseEndTime).toInstant().atZone(localZoneId);
					ZonedDateTime termUnpublishDate = new Date(courseEndTime + (ONE_DAY_IN_MS * daysafter)).toInstant().atZone(localZoneId);

					context.put("termStartDate", termStartDate.format(formatter));
					context.put("termEndDate", termEndDate.format(formatter));
					context.put("termUnpublishDate", termUnpublishDate.format(formatter));
					context.put("readableTermStartDate", userTimeService.dateFormat(new Date(courseStartTime - (ONE_DAY_IN_MS * daysbefore)), rb.getLocale(), DateFormat.LONG));	//create readable versions of all dates
					context.put("readableTermEndDate", userTimeService.dateFormat(academicSession.getEndDate(), rb.getLocale(), DateFormat.LONG));
					context.put("readableTermUnpublishDate", userTimeService.dateFormat(new Date(courseEndTime + (ONE_DAY_IN_MS * daysafter)), rb.getLocale(), DateFormat.LONG));
					context.put("readableTermStartDateTime", userTimeService.dateTimeFormat(new Date(courseStartTime - (ONE_DAY_IN_MS * daysbefore)), rb.getLocale(), DateFormat.SHORT));
					context.put("readableTermUnpublishDateTime", userTimeService.dateTimeFormat(new Date(courseEndTime + (ONE_DAY_IN_MS * daysafter)), rb.getLocale(), DateFormat.SHORT));

					if (templateSite != null && StringUtils.isNotBlank(templateSite.getProperties().getProperty(SITE_PUBLISH_TYPE))) {
						//when we need to get settings from a template site
						context.put("basedOnTemplate", true);
						context.put("publishType", templateSite.getProperties().getProperty(SITE_PUBLISH_TYPE));
						Date publishingDate = new Date();
						String publishingDateReadable = "";
						try {
							if (templateSite.getProperties().getProperty(SITE_PUBLISH_DATE)!=null && !StringUtils.isBlank(templateSite.getProperties().getProperty(SITE_PUBLISH_DATE))){
								context.put("readablePublishDate", userTimeService.dateTimeFormat(userTimeService.parseISODateInUserTimezone(templateSite.getProperties().getProperty(SITE_PUBLISH_DATE)), rb.getLocale(), DateFormat.SHORT));
								context.put("publishDate", templateSite.getProperties().getProperty(SITE_PUBLISH_DATE));
							} else {
								context.put("readablePublishDate", "");
								context.put("publishDate", "");
							}
							if(templateSite.getProperties().getProperty(SITE_UNPUBLISH_DATE)!=null && !StringUtils.isBlank(templateSite.getProperties().getProperty(SITE_UNPUBLISH_DATE))){
								context.put("readableUnpublishDate", userTimeService.dateTimeFormat(userTimeService.parseISODateInUserTimezone(templateSite.getProperties().getProperty(SITE_UNPUBLISH_DATE)), rb.getLocale(), DateFormat.SHORT));
								context.put("unpublishDate", templateSite.getProperties().getProperty(SITE_UNPUBLISH_DATE));
							} else {
								context.put("readableUnpublishDate", "");
								context.put("unpublishDate", "");
							}
						} catch (java.lang.NullPointerException ignored) {
							// nothing needed for NPE
						}
					} else {
						// default to publishing management
						context.put("publishType", defaultPublishType);
						context.put("readableUnpublishDate", "");	//clear dates
						context.put("unpublishDate", "");
						context.put("readablePublishDate", "");
						context.put("publishDate", "");
					}
					if (academicSession.getStartDate().before(new Date())){
						context.put("existingStatus", "Published");
						context.put("statusLabel", rb.getString("list.publi"));
					} else {
						context.put("existingStatus", "Scheduled");
						context.put("statusLabel", rb.getString("pubuncon.sched") + ' ' + userTimeService.dateTimeFormat(academicSession.getStartDate(), rb.getLocale(), DateFormat.SHORT));
					}
				} catch(IdNotFoundException i) {	//no session ID means this is Project, or term-free
					context.put("publishType", SITE_PUBLISH_TYPE_MANUAL);	//default to Manual for these situations,
					try {																//but we still need to handle the possibility of dates coming in from the template.
						if(templateSite.getProperties().getProperty(SITE_PUBLISH_DATE)!=null && !StringUtils.isBlank(templateSite.getProperties().getProperty(SITE_PUBLISH_DATE))){
							context.put("readablePublishDate", userTimeService.dateTimeFormat(userTimeService.parseISODateInUserTimezone(templateSite.getProperties().getProperty(SITE_PUBLISH_DATE)), rb.getLocale(), DateFormat.SHORT));
							context.put("publishDate", templateSite.getProperties().getProperty(SITE_PUBLISH_DATE));
						} else {
							context.put("readablePublishDate", "");
							context.put("publishDate", "");
						}
						if(templateSite.getProperties().getProperty(SITE_UNPUBLISH_DATE)!=null && !StringUtils.isBlank(templateSite.getProperties().getProperty(SITE_UNPUBLISH_DATE))){
							context.put("readableUnpublishDate", userTimeService.dateTimeFormat(userTimeService.parseISODateInUserTimezone(templateSite.getProperties().getProperty(SITE_UNPUBLISH_DATE)), rb.getLocale(), DateFormat.SHORT));
							context.put("unpublishDate", templateSite.getProperties().getProperty(SITE_UNPUBLISH_DATE));
						} else {
							context.put("readableUnpublishDate", "");
							context.put("unpublishDate", "");
						}
					} catch (java.lang.NullPointerException n) {	// but if we get an NPE we need to just clear the dates as we're most likely in new site creation from scratch.
						context.put("readableUnpublishDate", "");	//clear dates
						context.put("unpublishDate", "");
						context.put("readablePublishDate", "");
						context.put("publishDate", "");
					}
					context.put("existingStatus", "Published");
					context.put("statusLabel", rb.getString("list.publi"));
				}
				// use the type's template, if defined
				String realmTemplate = "!site.template";
				// if create based on template, use the roles from the template
				if (templateSite != null) {
					realmTemplate = siteService.siteReference(templateSite.getId());
					context.put("basedOnTemplate", true);	//tell the page if this is template-based site creation
				} else if (siteInfo.site_type != null) {
					realmTemplate = realmTemplate + "." + siteInfo.site_type;
				}
				try {
					AuthzGroup r = authzGroupService.getAuthzGroup(realmTemplate);
					
					// SAK-23257
					context.put("roles", getJoinerRoles(r.getId(), state, null));
				} catch (GroupNotDefinedException e) {
					try {
						AuthzGroup rr = authzGroupService.getAuthzGroup("!site.template");
						
						// SAK-23257
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
			 * When editing the list of tools this is called to set options that some tools require.
			 * For example the mail archive tools needs an alias before it can start to be used.
			 */
			if (site != null) {
				// revising a existing site's tool
				context.put("existingSite", Boolean.TRUE);
				context.put("continue", "15");

				// Add the menus to vm
				MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.MANAGE_TOOLS);
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
			multipleToolIntoContext(context, state, index);
			
			// put the lti tool selection into context
			if (state.getAttribute(STATE_LTITOOL_SELECTED_LIST) != null)
			{
				HashMap<String, Map<String, Object>> currentLtiTools = (HashMap<String, Map<String, Object>>) state.getAttribute(STATE_LTITOOL_SELECTED_LIST);
				HashMap<String, Map<String, Object>> dialogLtiTools = new HashMap<>();

				for (Map.Entry<String, Map<String, Object>> entry : currentLtiTools.entrySet() ) {
					Map<String, Object> toolMap = entry.getValue();
					// get the configuration html for tool is post-add configuration has been requested (by Laura)
					Object showDialog = toolMap.get(LTIService.LTI_SITEINFOCONFIG);
					if ( showDialog == null || ! "1".equals(showDialog.toString()) ) continue;

					String ltiToolId = toolMap.get("id").toString();
					String[] contentToolModel = ltiService.getContentModelIfConfigurable(Long.valueOf(ltiToolId), site.getId());
					if (contentToolModel != null) {

						// attach the ltiToolId to each model attribute, so that we could have the tool configuration page for multiple tools
						for(int k = 0; k < contentToolModel.length; k++) {
							contentToolModel[k] = ltiToolId + "_" + contentToolModel[k];
						}
						Map<String, Object> ltiTool = ltiService.getTool(Long.valueOf(ltiToolId), site.getId());
						String formInput = ltiService.formInput(ltiTool, contentToolModel);
						toolMap.put("formInput", formInput);
						toolMap.put("hasConfiguration", true);

						// Add the entry to the tools that need a dialog
						dialogLtiTools.put(ltiToolId, toolMap);
					}
				}
				context.put("ltiTools", dialogLtiTools);
				context.put("ltiService", ltiService);
				context.put("oldLtiTools", state.getAttribute(STATE_LTITOOL_EXISTING_SELECTED_LIST));
			}
			
			context.put("toolManager", toolManager);

			AcademicSession thisAcademicSession = (AcademicSession) state.getAttribute(STATE_TERM_SELECTED);
			String emailId = null;

			boolean prePopulateEmail = serverConfigurationService.getBoolean("wsetup.mailarchive.prepopulate.email",true);
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

			context.put("serverName", serverConfigurationService.getServerName());

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
			boolean existingSite = site != null;

			// define the tools available for import. defaults to those tools in the 'destination' site
			List<String> importableToolsIdsInDestinationSite = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			if (existingSite) {
				// revising a existing site's tool
				context.put("continue", SITE_INFO_TEMPLATE_INDEX);
				context.put("step", "2");
				context.put("currentSite", site);

				// Add the menus to vm
				MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.IMPORT_FROM_SITE);

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

			Map<String, Optional<List<String>>> importableToolsWithOptions = getToolsInSitesAvailableForImport(importSites);

			List<String> allImportableToolIdsInOriginalSites = new ArrayList<>(importableToolsWithOptions.keySet());
			
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
			boolean addMissingTools = siteManageService.isAddMissingToolsOnImportEnabled();
			
			List<String> toolsToInclude;
			if (addMissingTools) {
				toolsToInclude = allImportableToolIdsInOriginalSites;
				//set tools in destination site into context so we can markup the lists and show which ones are new
				context.put("toolsInDestinationSite", importableToolsIdsInDestinationSite);
			} else {
				//just just the ones in the destination site
				toolsToInclude = importableToolsIdsInDestinationSite;
			}

			List<String> selectedTools = new ArrayList<>();
			List<String> filteredTools = new ArrayList<>();
			for (String toolId : toolsToInclude) {
				if ((!filteredTools.contains(toolId) && !toolManager.isStealthed(toolId)) || securityService.isSuperUser()) {
					filteredTools.add(toolId);
				}

				selectedTools.add(toolId);
			}
			context.put("selectedTools", filteredTools);
			
			//build a map of sites and tools in those sites that have content
			Map<String,Set<String>> siteToolsWithContent = this.getSiteImportToolsWithContent(importSites, selectedTools);
			context.put("siteToolsWithContent", siteToolsWithContent);

			//build a map of sites and tools in those sites that have selectable entities
			Map<String,Set<String>> siteToolsWithSelectableContent = this.getSiteImportToolsWithSelectableContent(importSites, selectedTools);
			context.put("siteToolsWithSelectableContent", siteToolsWithSelectableContent);

			// set the flag for the UI
			context.put("addMissingTools", addMissingTools);
			context.put("isGradebookGroupEnabled", gradingService.isGradebookGroupEnabled(site.getId()));

			context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));
			context.put("importSitesTools", state
					.getAttribute(STATE_IMPORT_SITE_TOOL));
			context.put("importOptions", importableToolsWithOptions);
			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));
			context.put("importSupportedTools", allImportableToolIdsInOriginalSites);
			context.put("hideImportedContent", serverConfigurationService.getBoolean("content.import.hidden", false));
			
			Tool siteInfoTool = toolManager.getTool(SiteManageConstants.SITE_INFO_TOOL_ID);
			if (siteInfoTool != null) {
				context.put("siteInfoToolTitle", siteInfoTool.getTitle());
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

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.IMPORT_FROM_SITE);

			// define the tools available for import. defaults to those tools in the 'destination' site
			List<String> importableToolsIdsInDestinationSite = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			
			if (site != null) {
				// revising a existing site's tool
				context.put("continue", SITE_INFO_TEMPLATE_INDEX);
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
			Map<String, Optional<List<String>>> importableToolsWithOptions = getToolsInSitesAvailableForImport(importSites);
			List<String> allImportableToolIdsInOriginalSites = new ArrayList<String>(importableToolsWithOptions.keySet());
			
			//sort the list of all tools by title and extract into a list of toolIds
			//we then use this as the basis for sorting the other toolId lists
			List<MyTool> allTools = (List<MyTool>)state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
						
			Collections.sort(allTools, new Comparator<MyTool>(){
				public int compare(MyTool t1, MyTool t2) {
					return t1.getTitle().compareTo(t2.getTitle());
				}
			});
			final List<String> sortedToolIds = new ArrayList<String>();
			for (MyTool m: allTools) {
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
			boolean addMissingTools = siteManageService.isAddMissingToolsOnImportEnabled();
			
			//helper var to hold the list we use for the selectedTools context variable, as we use it for the alternate toolnames too
			List<String> selectedTools = new ArrayList<>();
			
			if (addMissingTools) {
				
                selectedTools = allImportableToolIdsInOriginalSites;

				//set tools in destination site into context so we can markup the lists and show which ones are new
				context.put("toolsInDestinationSite", importableToolsIdsInDestinationSite);
				
			} else {
				//just just the ones in the destination site
                selectedTools = importableToolsIdsInDestinationSite;
			}

			if (!securityService.isSuperUser()) {
				selectedTools = selectedTools.stream().filter(toolID -> !toolManager.isStealthed(toolID)).collect(Collectors.toList());
			}
			context.put("selectedTools", selectedTools);

			//build a map of sites and tools in those sites that have content
			Map<String,Set<String>> siteToolsWithContent = this.getSiteImportToolsWithContent(importSites, selectedTools);
			context.put("siteToolsWithContent", siteToolsWithContent);

			//build a map of sites and tools in those sites that have selectable entities
			Map<String,Set<String>> siteToolsWithSelectableContent = this.getSiteImportToolsWithSelectableContent(importSites, selectedTools);
			context.put("siteToolsWithSelectableContent", siteToolsWithSelectableContent);

			// set the flag for the UI
			context.put("addMissingTools", addMissingTools);
			context.put("isGradebookGroupEnabled", gradingService.isGradebookGroupEnabled(site.getId()));

			context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));
			context.put("importOptions", importableToolsWithOptions);
			context.put("importSitesTools", state
					.getAttribute(STATE_IMPORT_SITE_TOOL));
			context.put("check_home", state
					.getAttribute(STATE_TOOL_HOME_SELECTED));
			context.put("importSupportedTools", allImportableToolIdsInOriginalSites);

			Tool siteInfoTool = toolManager.getTool(SiteManageConstants.SITE_INFO_TOOL_ID);
			if (siteInfoTool != null) {
				context.put("siteInfoToolTitle", siteInfoTool.getTitle());
			}
			
			return (String) getContext(data).get("template") + TEMPLATE[60];
		}
		case 28:
			/*
			 * buildContextForTemplate chef_siteinfo-import.vm
			 * 
			 * This is called before the list of sites to import from is presented
			 * 
			 */

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.IMPORT_FROM_SITE);

			putImportSitesInfoIntoContext(context, site, state, false);
			return (String) getContext(data).get("template") + TEMPLATE[28];
		case 58:
			/*
			 * buildContextForTemplate chef_siteinfo-importSelection.vm
			 * 
			 */

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.IMPORT_FROM_SITE);

			putImportSitesInfoIntoContext(context, site, state, false);
			return (String) getContext(data).get("template") + TEMPLATE[58];
		case 59:
			/*
			 * buildContextForTemplate chef_siteinfo-importMigrate.vm
			 * 
			 */

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.IMPORT_FROM_SITE);

			putImportSitesInfoIntoContext(context, site, state, false);
			return (String) getContext(data).get("template") + TEMPLATE[59];

		case 29:
			/*
			 * buildContextForTemplate chef_siteinfo-duplicate.vm
			 * 
			 */

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.DUPLICATE_SITE);

			context.put("siteTitle", site.getTitle());

			// Determine if site contains Email Archive tool
			boolean hasEmailArchive = site.getToolForCommonId("sakai.mailbox") == null ? false : true;
			if (hasEmailArchive) {
				context.put("hasEmailArchive", hasEmailArchive);
				context.put("emailAddress", state.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
				context.put("serverName", serverConfigurationService.getServerName());
				state.setAttribute(STATE_DUP_SITE_HAS_EMAIL_ARCHIVE, hasEmailArchive);
			}
			

			String sType = site.getType();
			if (sType != null && siteTypeUtil.isCourseSite(sType)) {
				context.put("isCourseSite", Boolean.TRUE);
				context.put("currentTermId", site.getProperties().getProperty(
						Site.PROP_SITE_TERM));
				
				// SAK-23256
				setTermListForContext( context, state, true, false ); // true upcoming only
			} else {
				context.put("isCourseSite", Boolean.FALSE);
			}
			if (state.getAttribute(SITE_DUPLICATED) == null) {
				context.put("siteDuplicated", Boolean.FALSE);
			} else {
				context.put("siteDuplicated", Boolean.TRUE);
			}
			if (state.getAttribute(SITE_DUPLICATED_NAME) != null) {
				context.put("duplicatedName", state.getAttribute(SITE_DUPLICATED_NAME));
			}
			context.put( CONTEXT_IS_ADMIN, securityService.isSuperUser() );
			// Add option to also copy ScoringComponent associations
			ScoringService scoringService = (ScoringService)  ComponentManager.get("org.sakaiproject.scoringservice.api.ScoringService"); 
			ScoringAgent scoringAgent = scoringService.getDefaultScoringAgent();
			if (scoringAgent != null && scoringAgent.isEnabled(site.getId(), null)) {
				// check to see if the site has any associated ScoringComponents to duplicate
				List components = scoringAgent.getScoringComponents(site.getId());
				if (components != null && !components.isEmpty()) {
					context.put("scoringAgentOption", Boolean.TRUE);
					context.put("scoringAgentName", scoringAgent.getName());
				}
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
			context.put("siteIdMaxLength", 99);
			return (String) getContext(data).get("template") + TEMPLATE[29];
		case 36:
			/*
			 * buildContextForTemplate chef_site-newSiteCourse.vm
			 */		
			// SAK-9824
			Boolean enableCourseCreationForUser = serverConfigurationService.getBoolean("site.enableCreateAnyUser", Boolean.FALSE);
			context.put("enableCourseCreationForUser", enableCourseCreationForUser);
				
			if (site != null) {
				context.put("site", site);
				context.put("siteTitle", site.getTitle());

				// Add the menus to vm
				MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.EDIT_CLASS_ROSTERS);

				List<String> providerCourseList = (List<String>) state.getAttribute(SITE_PROVIDER_COURSE_LIST);
				coursesIntoContext(state, context, site);

				// SAK-23256
				List<AcademicSession> terms = setTermListForContext( context, state, true, true ); // true -> upcoming only
				
				AcademicSession selectedTerm = (AcademicSession) state.getAttribute(STATE_TERM_SELECTED);
				final String selectedTermEid = selectedTerm.getEid();

				if (!terms.isEmpty() && terms.stream().noneMatch(t -> t.getEid().equals(selectedTermEid))) {
					selectedTerm = terms.get(0);
				}
				context.put("term", selectedTerm);

				if (selectedTerm != null) {
					String userId = userDirectoryService.getCurrentUser().getEid();
					List<CourseObject> courses = prepareCourseAndSectionListing(userId, selectedTerm.getEid(), state);
					if (!courses.isEmpty()) {
						List<CourseObject> notIncludedCourse = new ArrayList<>();
						// remove included sites
                        for (CourseObject c : courses) {
                            if (providerCourseList == null || !providerCourseList.contains(c.getEid())) {
                                notIncludedCourse.add(c);
                            }
                        }
						state.setAttribute(STATE_TERM_COURSE_LIST, notIncludedCourse);
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

					List<SectionObject> authorizerSectionList = (List<SectionObject>) state.getAttribute(STATE_CM_AUTHORIZER_SECTIONS);
					if (authorizerSectionList != null) {
						context.put("selectedAuthorizerCourse", authorizerSectionList.stream().map(SectionObject::getEid).collect(Collectors.toList()));
					}
				}

				if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null) {
					context.put("selectedManualCourse", Boolean.TRUE);
				}
				context.put("term", (AcademicSession) state.getAttribute(STATE_TERM_SELECTED));
				context.put("currentUserId", (String) state.getAttribute(STATE_CM_CURRENT_USERID));
				context.put("form_additional", (String) state.getAttribute(FORM_ADDITIONAL));
				context.put("authorizers", getAuthorizers(state, STATE_CM_AUTHORIZER_LIST));
			}
			if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITESETUP)) {
				context.put("backIndex", "1");
			} else if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITEINFO)) {
				context.put("backIndex", "");
			}

			// Sort the course offerings if necessary
			List<CourseObject> courseList = (List) state.getAttribute(STATE_TERM_COURSE_LIST);
			if (CollectionUtils.isNotEmpty(courseList)) {
				courseList.sort(Comparator.comparing(CourseObject::getTitle, new AlphaNumericComparator()));
				state.setAttribute(STATE_TERM_COURSE_LIST, courseList);
			}
			context.put("termCourseList", state.getAttribute(STATE_TERM_COURSE_LIST));

			Boolean showRosterEIDs = serverConfigurationService.getBoolean(SAK_PROP_SHOW_ROSTER_EID, SAK_PROP_SHOW_ROSTER_EID_DEFAULT);
			context.put("showRosterEIDs", showRosterEIDs);

			// SAK-29000
			Boolean isAuthorizationRequired = serverConfigurationService.getBoolean( SAK_PROP_REQUIRE_AUTHORIZER, Boolean.TRUE );
			context.put( VM_ADD_ROSTER_AUTH_REQUIRED, isAuthorizationRequired );

			// added for 2.4 -daisyf
			context.put("campusDirectory", getCampusDirectory());
			context.put("userId", state.getAttribute(STATE_INSTRUCTOR_SELECTED) != null ? (String) state.getAttribute(STATE_INSTRUCTOR_SELECTED) : userDirectoryService.getCurrentUser().getId());
			/*
			 * for measuring how long it takes to load sections java.util.Date
			 * date = new java.util.Date(); log.debug("***2. finish at:
			 * "+date); log.debug("***3. userId:"+(String) state
			 * .getAttribute(STATE_INSTRUCTOR_SELECTED));
			 */
			
			context.put("basedOnTemplate",  state.getAttribute(STATE_TEMPLATE_SITE) != null ? Boolean.TRUE:Boolean.FALSE);
			context.put("publishTemplate", (Boolean) state.getAttribute(STATE_TEMPLATE_PUBLISH));
			
			// SAK-21706
			context.put( CONTEXT_SKIP_COURSE_SECTION_SELECTION, 
					serverConfigurationService.getBoolean( SAK_PROP_SKIP_COURSE_SECTION_SELECTION, Boolean.FALSE ) );
			context.put( CONTEXT_SKIP_MANUAL_COURSE_CREATION, 
					serverConfigurationService.getBoolean( SAK_PROP_SKIP_MANUAL_COURSE_CREATION, Boolean.FALSE ) );
			
			context.put("siteType", state.getAttribute(STATE_TYPE_SELECTED));
			
			// SAK-28990 remove continue with no roster
			context.put(VM_CONT_NO_ROSTER_ENABLED, serverConfigurationService.getBoolean(SAK_PROP_CONT_NO_ROSTER_ENABLED, false));
			
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
			context.put("officialAccountName", serverConfigurationService.getString("officialAccountName", ""));
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
			context.put("weeksAhead", serverConfigurationService.getString(
					"roster.available.weeks.before.term.start", "0"));
			
			context.put("basedOnTemplate",  state.getAttribute(STATE_TEMPLATE_SITE) != null ? Boolean.TRUE:Boolean.FALSE);
			context.put("publishTemplate", (Boolean) state.getAttribute(STATE_TEMPLATE_PUBLISH));
			
			context.put("requireAuthorizer", serverConfigurationService.getString(SAK_PROP_REQUIRE_AUTHORIZER, "true").equals("true")?Boolean.TRUE:Boolean.FALSE);
			
			// SAK-21706/SAK-23255
			context.put( CONTEXT_IS_ADMIN, securityService.isSuperUser() );
			context.put( CONTEXT_SKIP_COURSE_SECTION_SELECTION, serverConfigurationService.getBoolean( SAK_PROP_SKIP_COURSE_SECTION_SELECTION, Boolean.FALSE ) );
			context.put( CONTEXT_FILTER_TERMS, serverConfigurationService.getBoolean( SAK_PROP_FILTER_TERMS, Boolean.FALSE ) );
			
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
			context.put("serverName", serverConfigurationService.getServerName());
			context.put("include", Boolean.valueOf(siteInfo.include));
			return (String) getContext(data).get("template") + TEMPLATE[42];
		case 43:
			/*
			 * buildContextForTemplate chef_siteInfo-editClass.vm
			 * 
			 */

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.EDIT_CLASS_ROSTERS);

			context.put("allowAddRoster", securityService.unlock(siteService.SECURE_UPDATE_SITE_MEMBERSHIP, site.getReference()));
			context.put("siteTitle", site.getTitle());
			coursesIntoContext(state, context, site);

			return (String) getContext(data).get("template") + TEMPLATE[43];
		case 44:
			/*
			 * buildContextForTemplate chef_siteInfo-addCourseConfirm.vm
			 * 
			 */

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.EDIT_CLASS_ROSTERS);

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

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.IMPORT_FROM_ARCHIVE);

			return (String) getContext(data).get("template") + TEMPLATE[45];

		case 46:
			/*
			 * buildContextForTemplate chef_siteInfo-importMtrlCopy.vm
			 * 
			 */
			// this is for list display in listbox

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.IMPORT_FROM_ARCHIVE);

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

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.IMPORT_FROM_ARCHIVE);

			context.put("finalZipSites", state
					.getAttribute(FINAL_ZIP_IMPORT_SITES));

			return (String) getContext(data).get("template") + TEMPLATE[47];

		case 48:
			/*
			 * buildContextForTemplate chef_siteInfo-importMtrlCopyConfirm.vm
			 * 
			 */

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.IMPORT_FROM_ARCHIVE);

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

			if (courseManagementIsImplemented() && courseManagementService != null) {
				context.put("cmsAvailable", Boolean.valueOf(true));
			}
			
			int cmLevelSize = 0;

			if (courseManagementService == null || !courseManagementIsImplemented() || cmLevels == null || cmLevels.size() < 1) {
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
						levelOpts[numSelections] = sortCourseSets(courseManagementService.findCourseSets(getSelectionString(selections, numSelections)));
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
			context.put("requireAuthorizer", serverConfigurationService.getString(SAK_PROP_REQUIRE_AUTHORIZER, "true").equals("true")?Boolean.TRUE:Boolean.FALSE);
			
			// SAK-21706/SAK-23255
			context.put( CONTEXT_IS_ADMIN, securityService.isSuperUser() );
			context.put( CONTEXT_SKIP_MANUAL_COURSE_CREATION, serverConfigurationService.getBoolean( SAK_PROP_SKIP_MANUAL_COURSE_CREATION, Boolean.FALSE ) );
			context.put( CONTEXT_FILTER_TERMS, serverConfigurationService.getBoolean( SAK_PROP_FILTER_TERMS, Boolean.FALSE ) );
			
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
			context.put("toIndex", SITE_INFO_TEMPLATE_INDEX);

			// Add the menus to vm
			MenuBuilder.buildMenuForSiteInfo(portlet, data, state, context, site, rb, siteTypeProvider, SiteInfoActiveTab.IMPORT_FROM_SITE);

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
		case 65:
			/*
			 * build context for chef_site-siteInfo-manageOverview
			 */
			SitePage page = (SitePage) state.getAttribute("overview");
			Site s = getStateSite(state, true);
			List<SitePage> pages = s.getPages();

			//this will be all widgets available to use on overview page.
			List<Tool> widgets;
			if(state.getAttribute("allWidgets") == null){
				widgets = findWidgets();
			}else {
				widgets = (List<Tool>) state.getAttribute("allWidgets");
			}

			//maps widgets to their respective tools. If the tool is not in the site, the widget will not be available.
			Map<String, String> requiredToolMap = new HashMap<>();
			for(Tool tool : widgets){
				List<String> reqId = SYNOPTIC_TOOL_ID_MAP.get(tool.getId());
				boolean toolRequired = !StringUtils.equalsIgnoreCase(tool.getFinalConfig().getProperty("require.tool"), "false");
				if(reqId != null  && toolRequired){
					for(String req : reqId){
						requiredToolMap.put(req, tool.getId());
					}
				}
			}

			//loop through all pages in site to determine which widgets are unavailable.
			//while in this loop, also check for home page in case it is not in the state.
			for (SitePage pg : pages) {
				if (pg.isHomePage() && page == null) {
					page = pg;
				}
				List<ToolConfiguration> pageTools = pg.getTools();
				for(ToolConfiguration pageTool : pageTools){
					String toolId = pageTool.getToolId();
					String val = requiredToolMap.get(toolId);
					//this removes all items with this value.
					//using values().remove(val) only removes the first one.
					requiredToolMap.values().removeAll(Collections.singleton(val));
				}
			}

			List<String> requiredTools = new ArrayList<>(requiredToolMap.values());

			List<ToolConfiguration> tools = new ArrayList<>();
			if (state.getAttribute("tools") == null) {
				tools.addAll(page.getTools());
			} else {
				tools.addAll((List<ToolConfiguration>) state.getAttribute("tools"));
			}
			tools = sortTools(tools, page);

			//left and right tool lists used for maneuvering on-the-fly for double column layout
			List<ToolConfiguration> leftTools = new ArrayList<>();
			List<ToolConfiguration> rightTools = new ArrayList<>();
			for (ToolConfiguration toolConfiguration : tools) {
				int[] layoutHints = toolConfiguration.parseLayoutHints();
				if (layoutHints != null) {
					if (layoutHints[1] == 0) {
						leftTools.add(toolConfiguration);
					} else if (layoutHints[1] == 1) {
						rightTools.add(toolConfiguration);
					}
				}
			}
			leftTools = sortTools(leftTools, page);
			rightTools = sortTools(rightTools, page);

			int layout = page.getLayout() + 1; //we need layout to be 1-based for context, but it is stored 0-based.

			state.setAttribute("tools", tools);
			state.setAttribute("leftTools", leftTools);
			state.setAttribute("rightTools", rightTools);
			state.setAttribute("overview", page);
			state.setAttribute("site", site);
			state.setAttribute("allWidgets", widgets);

			context.put("tools", tools);
			context.put("allWidgets", widgets);
			context.put("requiredTools", requiredTools);
			context.put("leftTools", leftTools);
			context.put("rightTools", rightTools);
			context.put("pagelayout", layout);
			context.put("page", page);
			context.put("site", site);
			context.put("layouts", layoutsList());
			boolean fromHome = state.getAttribute("fromHome") != null ? (boolean) state.getAttribute("fromHome") : false;
			if(fromHome) {
				context.put("back", page.getId());
			}

			return (String) getContext(data).get("template") + TEMPLATE[65];
		}
		// should never be reached
		return (String) getContext(data).get("template") + TEMPLATE[0];
	}

	public static boolean isSiteMyWorkspace(Site site) {
		return org.sakaiproject.site.cover.SiteService.isUserSite(site.getId()) && org.sakaiproject.site.cover.SiteService.getSiteUserId(site.getId()).equals(org.sakaiproject.tool.cover.SessionManager.getCurrentSessionUserId());
	}

	//sort tools based on their layout hints
	private List<ToolConfiguration> sortTools(List<ToolConfiguration> tools, SitePage page){
		int layout = page.getLayout();
		if(tools == null || tools.isEmpty() || tools.size() == 1) return tools;

		List<ToolConfiguration> sortedTools = new ArrayList<>();

		for(int i=0; i< tools.size(); i++){
			ToolConfiguration tool = tools.get(i);
			String hint = tool.getLayoutHints();
			if(StringUtils.isEmpty(hint)){
				String[] hintArr = {Integer.toString(i), Integer.toString(layout)};
				hint = String.join(",", hintArr);
				tool.setLayoutHints(hint);
			}
			String[] hintArr = hint.split(",");

			if(layout == 0){
				//everything has to be col 0 for layout hints
				hintArr[1]="0"; //replace column with "0".

			}
			//row is going to be i, to stop elements from saving with identical layout hints
			hintArr[0]=Integer.toString(i);
			hint = String.join(",", hintArr);
			tool.setLayoutHints(hint);
			sortedTools.add(tool);
		}
		for(int i = 0; i< sortedTools.size(); i++){
			sortedTools.get(i).setPageOrder(i+1);
		}
		return sortedTools;
	}
	/**
	 * Finds the tool ID to use for the adding participants to the site.
	 * Also checks that the configured tool is a valid helper.
	 * @param site The site to add users to.
	 * @return The tool ID.
	 */
	public static String getAddUserHelper(Site site) {
		String helperId = site.getProperties().getProperty("sitemanage.add.user.tool");
		if (helperId == null) {
			helperId = org.sakaiproject.component.cover.ServerConfigurationService.getString(
					"sitemanage.add.user.tool", "sakai-site-manage-participant-helper"
			);
		}
		// Validate it's a helpers before attempting to use it.
		Tool tool = org.sakaiproject.tool.cover.ToolManager.getTool(helperId);
		if (tool == null || !tool.getCategories().contains("sakai.helper")) {
			helperId = "sakai-site-manage-participant-helper";
		}
		return helperId;
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
				// Check if the role is allowed to be granted in the realm
				boolean allowedRoleId = serverConfigurationService.getBoolean("sitemanage.grant"+roleId, false);
				if(!allowedRoleId){
					continue;
				}
				AdditionalRole role = new AdditionalRole();
				role.id = roleId;
				role.name = authzGroupService.getRoleName(role.id);
				role.editable = true;
				additionalRoles.put(role.id, role);
			}
		return additionalRoles;
	}

	private void toolSelectionIntoContext(Context context, SessionState state, String siteType, String siteId, String overridePageOrderSiteTypes, int index) {
		List toolRegistrationSelectedList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		List toolRegistrationList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		context.put(STATE_TOOL_REGISTRATION_LIST, toolRegistrationList);
		// put tool title into context if PageOrderHelper is enabled
		pageOrderToolTitleIntoContext(context, state, siteType, false, overridePageOrderSiteTypes);

		context.put("check_home", state
				.getAttribute(STATE_TOOL_HOME_SELECTED));
		context.put("selectedTools", orderToolIds(state, checkNullSiteType(state, siteType, siteId), toolRegistrationSelectedList, false));
		context.put("oldSelectedTools", state
				.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST));
		context.put("oldSelectedHome", state
				.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME));
		context.put("continueIndex", SITE_INFO_TEMPLATE_INDEX);
		if (state.getAttribute(STATE_TOOL_EMAIL_ADDRESS) != null) {
			context.put("emailId", state
					.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
		}
		context.put("serverName", serverConfigurationService.getServerName());
		
		// all info related to multiple tools
		multipleToolIntoContext(context, state, index);

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
		String siteId = site == null? UUID.randomUUID().toString(): site.getId();

		// Determine if course navigation placement is required
		boolean requireCourseNavPlacement = serverConfigurationService.getBoolean("site-manage.requireCourseNavPlacement", true);

		// Get stranded LTI tools (deployed in site but no longer available)
		List<MyTool> strandedTools = getStrandedLTITools(site, requireCourseNavPlacement);
		state.setAttribute(STATE_LTITOOL_STRANDED_LIST, strandedTools);
		context.put("strandedLtiTools", strandedTools);

		// get the list of launchable tools - visible and including stealthed
		visibleTools = ltiService.getToolsLaunch(siteId, true);
		if (site == null) {
			allTools = visibleTools;
		} else {
			// Get tools specfic for this site or that are available in all sites.
			allTools = ltiService.getToolsLaunch(site.getId(), true);
		}
		if (visibleTools != null && !visibleTools.isEmpty()) {
			HashMap<String, Map<String, Object>> ltiTools = new HashMap<>();
			HashMap<String, Map<String, Object>> linkedLtiContents = new HashMap<>();
			// Find the tools that exist in the site, this should only be done if we already have a site.
			if (site != null) {
				List<Map<String, Object>> contents = ltiService.getContentsDao(null, null, 0, 0, site.getId(), ltiService.isAdmin(site.getId()));
				for (Map<String, Object> content : contents) {
					String ltiToolId = content.get(LTIService.LTI_TOOL_ID).toString();
					String ltiSiteId = StringUtils.trimToNull((String) content.get(LTIService.LTI_SITE_ID));
					if (siteId != null) {
						// whether the tool is already enabled in site
						String pstr = (String) content.get(LTIService.LTI_PLACEMENT);
						if (StringUtils.trimToNull(pstr) != null) {
							// the lti tool is enabled in the site
							ToolConfiguration toolConfig = siteService.findTool(pstr);
							if (toolConfig != null && toolConfig.getSiteId().equals(ltiSiteId)) {
								Map<String, Object> m = new HashMap<>();
								Map<String, Object> ltiToolValues = ltiService.getTool(Long.valueOf(ltiToolId), ltiSiteId);
								if (ltiToolValues != null) {
									m.put(LTIService.LTI_TITLE, SakaiLTIUtil.getToolTitle(ltiToolValues, content, null));
									m.put("toolTitle", SakaiLTIUtil.getToolTitle(ltiToolValues, content, null));
									m.put("contentKey", content.get(LTIService.LTI_ID));
									linkedLtiContents.put(ltiToolId, m);
								}
							}
						}
					}
				}
			}

         // First search list of visibleTools for those not selected (excluding stealthed tools)
			for (Map<String, Object> toolMap : visibleTools ) {
				String ltiToolId = toolMap.get("id").toString();
				String ltiSiteId = StringUtils.trimToNull((String) toolMap.get(LTIService.LTI_SITE_ID));
				toolMap.put("selected", linkedLtiContents.containsKey(ltiSiteId));
				if ( ltiSiteId == null || (site != null && siteId.equals(site.getId())))
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
			courseSet = courseManagementService.getCourseSets();
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
		if (!newSite && !toolManager.isStealthed("sakai-site-pageorder-helper") && isPageOrderAllowed(siteType, overrideSitePageOrderSetting))
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
		if (serverConfigurationService.getStrings("sitemanage.cm.courseset.categories") != null) {
			List<String> showCourseSetTypes = new ArrayList(Arrays.asList(serverConfigurationService.getStrings("sitemanage.cm.courseset.categories")));
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
		context.put("importSiteList", state.getAttribute(STATE_IMPORT_SITES));
		final List<Site> siteList = siteService.getSites(SelectionType.UPDATE, ownTypeOnly ? site.getType() : null, null, null, SortType.TITLE_ASC, null);
		List<String> hiddenSiteIdList = new ArrayList<>();
		List<Site> hiddenSiteList = new ArrayList<>();
		List<Site> visibleSiteList = new ArrayList<>();
		Preferences preferences = preferencesService.getPreferences(userDirectoryService.getCurrentUser().getId());
		if (preferences != null) {
			ResourceProperties properties = preferences.getProperties(PreferencesService.SITENAV_PREFS_KEY);
			hiddenSiteIdList = (List<String>) properties.getPropertyList(PreferencesService.SITENAV_PREFS_EXCLUDE_KEY);
		}

		if (hiddenSiteIdList != null && !hiddenSiteIdList.isEmpty()) {
			for (Site s : siteList) {
				if (hiddenSiteIdList.contains(s.getId())) {
					hiddenSiteList.add(s);
				} else {
					visibleSiteList.add(s);
				}
			}
		} else {
			visibleSiteList.addAll(siteList);
		}

		List<Site> templateSiteList = siteService.getSites(SelectionType.ANY, null, null, Map.of("template", "true"), SortType.TITLE_ASC, null);

		context.put("sites", visibleSiteList);
		context.put("templateSites", templateSiteList);
		context.put("hiddenSites", hiddenSiteList);

		context.put("cleanImport", serverConfigurationService.getBoolean(SiteConstants.SAK_PROP_CLEAN_IMPORT_SITE, SiteConstants.SAK_PROP_CLEAN_IMPORT_SITE_DEFAULT));
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
						Section s = courseManagementService.getSection(providerSectionId);
						if (s != null)
						{
							providerSectionListTitles.put(s.getEid(), s.getTitle()); 
						}
					}
					catch (IdNotFoundException e)
					{
						providerSectionListTitles.put(providerSectionId, providerSectionId);
						log.warn("putSelectedProviderCourseIntoContext Cannot find section " + providerSectionId);
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
	public static boolean isPageOrderAllowed(String siteType, String overrideSitePageOrderSetting) {
		if (overrideSitePageOrderSetting != null && Boolean.valueOf(overrideSitePageOrderSetting))
		{
			// site-specific setting, show PageOrder tool
			return true;
		}
		else
		{
			// read the setting from sakai properties
			boolean rv = true;
			String hidePageOrderSiteTypes = org.sakaiproject.component.cover.ServerConfigurationService.getString(SAKAI_PROPERTY_HIDE_PAGEORDER_SITE_TYPES, "");
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

	private void multipleToolIntoContext(Context context, SessionState state, int index) {
		// titles for multiple tool instances
		context.put(STATE_MULTIPLE_TOOL_ID_SET, state.getAttribute(STATE_MULTIPLE_TOOL_ID_SET ));
		context.put(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, state.getAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP ));
		context.put(STATE_MULTIPLE_TOOL_CONFIGURATION, state.getAttribute(STATE_MULTIPLE_TOOL_CONFIGURATION));
		context.put(STATE_MULTIPLE_TOOL_INSTANCE_SELECTED, state.getAttribute(STATE_MULTIPLE_TOOL_INSTANCE_SELECTED));

		gradebookInstancesIntoContext(context, state, index);

	}

	private void gradebookInstancesIntoContext(Context context, SessionState state, int index) {

		String currentSiteId = StringUtils.trimToNull((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
		if (currentSiteId != null) {

			Site site = getStateSite(state, false);

			Collection<Group> groups = site.getGroups();
			if (groups.size() > 0) {
				context.put("groupsList", new ArrayList<>(groups));
			}

			List<String> gbGroups = new ArrayList<>();
			Collection<ToolConfiguration> gbs = site.getTools(SiteManageConstants.GRADEBOOK_TOOL_ID);
			for (ToolConfiguration tc : gbs) {
				Properties props = tc.getPlacementConfig();
				if (props.getProperty(GB_GROUP_PROPERTY) != null) {
					gbGroups.add(props.getProperty(GB_GROUP_PROPERTY));
				}
			}

			switch (index) {
				case 10: // new site
				case 26: // multiple instance tools - radiobuttons
				default: // other
					if (GradebookGroupEnabler.isEnabledForSite(site)) {
						context.put("value_gb", GradebookGroupEnabler.VALUE_GRADEBOOK_GROUPS);
					} else {
						context.put("value_gb", GradebookGroupEnabler.VALUE_GRADEBOOK_SITE);
					}
					break;
				case 15: // confirmation screen
					if (GradebookGroupEnabler.isEnablingForSite(state)) {
						context.put("value_gb", GradebookGroupEnabler.VALUE_GRADEBOOK_GROUPS);
					} else {
						context.put("value_gb", GradebookGroupEnabler.VALUE_GRADEBOOK_SITE);
					}

					context.put(GradebookGroupEnabler.FORM_INPUT_ID, state.getAttribute(GradebookGroupEnabler.FORM_INPUT_ID));
					break;
			}
			context.put(GradebookGroupEnabler.SELECTED_GROUPS, state.getAttribute(GradebookGroupEnabler.SELECTED_GROUPS));
			context.put("siteId", currentSiteId);
			context.put("gbGroups", gbGroups);
			context.put("value_gbSite", GradebookGroupEnabler.VALUE_GRADEBOOK_SITE);
			context.put("value_gbGroups", GradebookGroupEnabler.VALUE_GRADEBOOK_GROUPS);
		}

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
		boolean disableCourseSkinChoice = serverConfigurationService.getString("disable.course.site.skin.selection", "false").equals("true");
		//Do we allow them to use a specific icon for the site. Related to SAK-29458 for Sakai 11
		//Default to true for now. Requires the portal.siteicon.allow value in the sakai.properties file.
		context.put("allowSiteIcon",serverConfigurationService.getBoolean("portal.siteicon.allow",true));
		// For non-course site, display skin list based on "disable.noncourse.site.skin.selection" value set with sakai.properties file. The setting defaults to be true.
		boolean disableNonCourseSkinChoice = serverConfigurationService.getString("disable.noncourse.site.skin.selection", "true").equals("true");
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
		sessionManager.getCurrentToolSession().setAttribute(HELPER_ID + ".siteId", getStateSite(state).getId());

		// launch the helper
		startHelper(data.getRequest(), "sakai-site-pageorder-helper");
	}

	public void doDateManagerHelper(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		startHelper(data.getRequest(), "sakai.datemanager");
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
		sessionManager.getCurrentToolSession().setAttribute(HELPER_ID + ".siteId", getStateSite(state).getId());

		// launch the helper
		startHelper(data.getRequest(), getAddUserHelper(getStateSite(state)));
	}

	/**
	 * Launch the Manage Group helper Tool -- for adding, editing and deleting groups
	 * 
	 */
	public void doManageGroupHelper(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		startHelper(data.getRequest(), "sakai-site-group-manager");
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

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		// pass in the siteId of the site to be ordered (so it can configure
		// sites other then the current site)
		sessionManager.getCurrentToolSession().setAttribute(HELPER_ID + ".siteId", getStateSite(state).getId());

		// launch the helper
		startHelper(data.getRequest(), "sakai-site-manage-link-helper");
	}

	/**
	 * Launch the Manage Overview helper from home
	 */
	public void doManageOverviewFromHome(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		state.setAttribute("fromHome", true);
		doManageOverview(data);
	}
		
	/**
	 * Launch the Manage Overview helper -- for managing overview layout
	 */
	public void doManageOverview(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		siteToolsIntoState(state);

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "65");
			if (state.getAttribute(STATE_INITIALIZED) == null) {
				state.setAttribute(STATE_OVERRIDE_TEMPLATE_INDEX, "65");
			}
		}
	}
	/**
	 * Launch the External Tools Helper -- For managing external tools
	 */
	public void doExternalHelper(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// pass in the siteId of the site to be ordered (so it can configure
		// sites other then the current site)
		sessionManager.getCurrentToolSession().setAttribute(HELPER_ID + ".siteId", getStateSite(state).getId());

		// launch the helper
		startHelper(data.getRequest(), "sakai.lti.admin.helper");
	}
	
	public void doUserAuditEventLog(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		// pass in the siteId of the site to be ordered (so it can configure
		// sites other then the current site)
		sessionManager.getCurrentToolSession().setAttribute(HELPER_ID + ".siteId", getStateSite(state).getId());

		// launch the helper
		startHelper(data.getRequest(), "sakai.useraudit");
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

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		// state.setAttribute(FILE_UPLOAD_MAX_SIZE,
		// serverConfigurationService.getString("content.upload.max", "1"));
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

		String max_file_size_mb = serverConfigurationService.getString(
				"content.upload.max", "1");
		long max_bytes = 1024 * 1024;
		try {
			max_bytes = Long.parseLong(max_file_size_mb) * 1024 * 1024;
		} catch (Exception e) {
			// if unable to parse an integer from the value
			// in the properties file, use 1 MB as a default
			max_file_size_mb = "1";
			max_bytes = 1024 * 1024;
			log.error(this + ".doUpload_Mtrl_Frm_File: wrong setting of content.upload.max = " + max_file_size_mb, e);
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
				log.error("FileNotFoundException creating temp import file",fnfe);
			}
			catch (IOException ioe) {
				log.error("IOException creating temp import file",ioe);
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
				 	log.info("Getting import items from manifest.");
					List lst = importDataSource.getItemCategories();
					if (lst != null && lst.size() > 0) {
						Iterator iter = lst.iterator();
						while (iter.hasNext()) {
							ImportMetadata importdata = (ImportMetadata) iter
									.next();
							// Logger.info("chef","Preparing import
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
	 	log.info("about to import {} top level items", fnlList.size());
	 	log.info("the importDataSource is {}", importDataSource.getClass().getName());
		if (importDataSource instanceof SakaiArchive) {
		 	log.info("our data source is a Sakai format");
			((SakaiArchive) importDataSource).buildSourceFolder(fnlList);
		 	log.info("source folder is {}", ((SakaiArchive) importDataSource).getSourceFolder());
			archiveService.merge(((SakaiArchive) importDataSource).getSourceFolder(), siteId, null);
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

		state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);

	}

	// htripath-end

	/**
	 * Handle the site search request.
	 */
	public void doSite_search(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the search form field into the state object
		String search = StringUtils.trimToNull(data.getParameters().getString(FORM_SEARCH));
		//The search input has been encoded and should be decoded.
		try {
			search = URLDecoder.decode(search, StandardCharsets.UTF_8.toString());
		} catch(UnsupportedEncodingException ex) {
			log.error("Error decoding the input search '{}'.", search);
		}
		resetPaging(state);
		// set the flag to go to the prev page on the next list
		if (StringUtils.isBlank(search)) {
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
	 * Handle a Search Clear request.
	 */
	public void doUser_search_clear(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// clear the search
		state.removeAttribute(SITE_USER_SEARCH);
		resetPaging(state);

	} // doUser_search_clear

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
					Section s = courseManagementService.getSection(sectionId);
					if (s != null)
					{
						sectionTitles.put(sectionId, s.getTitle());
					}
				}
				catch (IdNotFoundException e)
				{
					sectionTitles.put(sectionId, sectionId);
					log.warn("coursesIntoContext: Cannot find section " + sectionId);
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

	public void doUser_search(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the search form field into the state object
		String search = StringUtils.trimToNull(data.getParameters().getString(FORM_SEARCH));

		// If there is no search term provided, remove any previous search term from state
		if (StringUtils.isBlank(search)) {
			state.removeAttribute(SITE_USER_SEARCH);
		} else {
			// Search term is present, reset the paging and set the search term in state
			resetPaging(state);
			state.setAttribute(SITE_USER_SEARCH, search);
		}

	} // doUser_search

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
						Section s = courseManagementService.getSection(courseEid);
						if (s!=null)
						{
							soList.add(new SectionObject(s));
						}
					}
					catch (IdNotFoundException e)
					{
						log.warn("courseListFromStringIntoContext: cannot find section " + courseEid);
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
		
		// SAK-23256
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
		String userId = sessionManager.getCurrentSessionUserId();
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
			if (securityService.isSuperUser()) {
				// admin-type of user
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {
					if (view.equals(SITE_TYPE_ALL)) {
						// search for non-user sites, using
						// the criteria
						size = siteService.countSites(SelectionType.NON_USER, null, search, termProp);
					} else if (view.equals(SITE_TYPE_MYWORKSPACE)) {
						// search for a specific user site
						// for the particular user id in the
						// criteria - exact match only
						try {
							siteService.getSite(siteService.getUserSiteId(search));
							size++;
						} catch (IdUnusedException e) {
						}
					} else if (view.equalsIgnoreCase(SITE_TYPE_DELETED)) {
						size = siteService.countSites(SelectionType.ANY_DELETED, null, search, null);
					} else {
						// search for specific type of sites
						size = siteService.countSites(SelectionType.NON_USER, view, search, termProp);
					}
				}
			} else {
				Site userWorkspaceSite = null;
				try {
					userWorkspaceSite = siteService.getSite(siteService.getUserSiteId(userId));
				} catch (IdUnusedException e) {
					log.error(this + "sizeResources, template index = 0: Cannot find user "
							+ sessionManager.getCurrentSessionUserId()
							+ "'s My Workspace site.", e);
				}

				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {

					SelectionType selectionType = SelectionType.ACCESS;
					if (serverConfigurationService.getBoolean("sitesetup.show.unpublished", false)) {
						selectionType = SelectionType.MEMBER;
					}

					if (view.equals(SITE_TYPE_ALL)) {
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
						size += siteService.countSites(selectionType, null, search, termProp);
					} else if (view.equalsIgnoreCase(SITE_TYPE_DELETED)) {
						size += siteService.countSites(SelectionType.DELETED, null,search, null);
					} else if (view.equals(SITE_TYPE_MYWORKSPACE)) {
						// get the current user MyWorkspace site
						try {
							siteService.getSite(siteService.getUserSiteId(userId));
							size++;
						} catch (IdUnusedException e) {
						}
					} else if (view.equals(SITE_INACTIVE)) {
						size += siteService.countSites(SelectionType.INACTIVE_ONLY,null, search, termProp);
					} else {
						// search for specific type of sites
						size += siteService.countSites(selectionType, view, search, termProp);
					}
				}
			}
		}
		// for SiteInfo list page
		else if (state.getAttribute(STATE_TEMPLATE_INDEX).equals(STATE_TEMPLATE_INDEX_MANAGE_PARTICIPANTS)) {
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
			
			if (securityService.isSuperUser()) {
				// admin-type of user
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {
					if (view.equals(SITE_TYPE_ALL)) {
						// search for non-user sites, using the
						// criteria
						return siteService.getSites(SelectionType.NON_USER, null, search, termProp, sortType, new PagingPosition(first, last));
					} else if (view.equalsIgnoreCase(SITE_TYPE_MYWORKSPACE)) {
						// search for a specific user site for
						// the particular user id in the
						// criteria - exact match only
						List rv = new Vector();
						try {
							Site userSite = siteService.getSite(siteService.getUserSiteId(search));
							rv.add(userSite);
						} catch (IdUnusedException e) {
						}

						return rv;
					} else if (view.equalsIgnoreCase(SITE_TYPE_DELETED)) {
						return siteService.getSites(SelectionType.ANY_DELETED, null, search, null, sortType, new PagingPosition(first, last));
					} else {
						// search for a specific site
						return siteService.getSites(SelectionType.ANY, view, search, termProp, sortType, new PagingPosition(first, last));
					}
				}
			} else {
				List rv = new Vector();
				Site userWorkspaceSite = null;
				String userId = sessionManager.getCurrentSessionUserId();

				try {
					userWorkspaceSite = siteService.getSite(siteService.getUserSiteId(userId));
				} catch (IdUnusedException e) {
					log.error(this + "readResourcesPage template index = 0 :Cannot find user " + sessionManager.getCurrentSessionUserId() + "'s My Workspace site.", e);
				}
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null) {

					SelectionType selectionType = SelectionType.ACCESS;
					if (serverConfigurationService.getBoolean("sitesetup.show.unpublished", false)) {
						selectionType = SelectionType.MEMBER;
					}

					if (view.equals(SITE_TYPE_ALL)) {
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
						rv.addAll(siteService.getSites(selectionType, null, search, termProp, sortType, new PagingPosition(first, last)));
					}
					else if (view.equals(SITE_TYPE_MYWORKSPACE)) {
						// get the current user MyWorkspace site
						try {
							rv.add(siteService.getSite(siteService.getUserSiteId(userId)));
						} catch (IdUnusedException e) {
						}
					} else if (view.equalsIgnoreCase(SITE_TYPE_DELETED)) {
						return siteService.getSites(SelectionType.DELETED, null, search, null, sortType, new PagingPosition(first, last));
					} else if (view.equals(SITE_INACTIVE)) {
						rv.addAll(siteService.getSites(SelectionType.INACTIVE_ONLY, null, search, termProp, sortType, new PagingPosition(first, last)));
					} else {
						rv.addAll(siteService.getSites(selectionType, view, search, termProp, sortType, new PagingPosition(first, last)));
					}
				}

				return rv;

			}
		}
		// if in Site Info list view
		else if (state.getAttribute(STATE_TEMPLATE_INDEX).equals(STATE_TEMPLATE_INDEX_MANAGE_PARTICIPANTS)) {
			List participants = (state.getAttribute(STATE_PARTICIPANT_LIST) != null) ? collectionToList((Collection) state.getAttribute(STATE_PARTICIPANT_LIST)): new Vector();
			String sortedBy = (String) state.getAttribute(SORTED_BY);
			String sortedAsc = (String) state.getAttribute(SORTED_ASC);
			Iterator sortedParticipants;
			if (sortedBy != null) {
				sortedParticipants = new SortedIterator(participants.iterator(), new SiteComparator(sortedBy,sortedAsc,comparator_locale));
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
	private boolean select_import_tools(ParameterParser params, SessionState state) {

		Map<String, List<String>> toolSiteMap = new HashMap<>();
		for (Iterator<String> iter = params.getNames(); iter.hasNext();) {
			String name = iter.next();

			if (!name.contains("-item-")) continue;

			String[] toolAndSiteId = name.substring(0, name.indexOf("-item-")).split("\\$");
			String toolId = toolAndSiteId[0];
			String siteId = toolAndSiteId[1];

			List<String> sites = toolSiteMap.get(toolId);
			if (sites == null) {
				sites = new ArrayList<>();
				toolSiteMap.put(toolId, sites);
			}

			sites.add(siteId);
		}

		Map<String, List<String>> importTools = new HashMap<>();
		Map<String, List<String>> fullyImportedToolMap = new HashMap<>();
		Map<String, List<String>> partiallyImportedToolMap = new HashMap<>();

		Consumer<String> adder = toolId -> {

			for (Iterator<String> iter = params.getNames(); iter.hasNext();) {
				String name = iter.next();
				if (name.equals(toolId)) {
					List<String> siteIds = Arrays.asList(params.getStrings(name));
					importTools.put(toolId, siteIds);
					fullyImportedToolMap.put(toolId, siteIds);
				} else if (name.contains(toolId)) {
					List<String> toolSites = toolSiteMap.get(toolId);
					if (toolSites != null) {
						partiallyImportedToolMap.put(toolId, toolSites);
					}
				}
			}
		};

		// has the user selected any tool for importing?
		boolean anyToolSelected = false;

		//all importable tools.
		//depending on the config, either one could be selected, which is valid
		if (siteManageService.isAddMissingToolsOnImportEnabled()) {
			getImportableTools().keySet().stream().forEach(adder);
			anyToolSelected = !importTools.isEmpty() || !partiallyImportedToolMap.isEmpty();
		} else {
			// the tools for current site
			getOriginalToolIds((List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST), state)
				.stream().forEach(adder);
			anyToolSelected = !importTools.isEmpty() || !partiallyImportedToolMap.isEmpty();
		}

		// Scan the parameters for individual tool items.
		Map<String, Map<String, List<String>>> individualItemMap = new HashMap<>();

		for (Iterator<String> iter = params.getNames(); iter.hasNext();) {
			String name = iter.next();

			if (!name.contains("-item-")) continue;

			String[] toolAndSiteId = name.substring(0, name.indexOf("-item-")).split("\\$");
			String toolId = toolAndSiteId[0];
			String siteId = toolAndSiteId[1];

			List<String> fullyImportedSiteIds = fullyImportedToolMap.get(toolId);
			boolean fullyImportedForSite = fullyImportedSiteIds != null && fullyImportedSiteIds.contains(siteId);

			if (fullyImportedForSite) continue;

			Map<String, List<String>> siteItemMap = individualItemMap.get(toolId);
			if (siteItemMap == null) {
				siteItemMap = new HashMap<>();
				individualItemMap.put(toolId, siteItemMap);
			}

			List<String> items = siteItemMap.get(siteId);
			if (items == null) {
				items = new ArrayList<>();
				siteItemMap.put(siteId, items);
			}
			items.add(params.getString(name));

			anyToolSelected = true;
		}

		state.setAttribute(STATE_IMPORT_SITE_TOOL_ITEMS, individualItemMap);

		log.debug("tools to import: {}", importTools);

		state.setAttribute(STATE_IMPORT_SITE_TOOL, importTools);

		Map<String, Map<String, List<String>>> toolOptions = new HashMap<>();

		for (Iterator<String> iter = params.getNames(); iter.hasNext();) {
			String name = iter.next();

			if (!name.contains("-import-option-")) continue;

			String[] toolAndSiteId = name.substring(0, name.indexOf("-import-option-")).split("\\$");
			String toolId = toolAndSiteId[0];
			String siteId = toolAndSiteId[1];

			Map<String, List<String>> siteOptionsMap = toolOptions.get(toolId);
			if (siteOptionsMap == null) {
				siteOptionsMap = new HashMap<>();
				toolOptions.put(toolId, siteOptionsMap);
			}

			List<String> options = siteOptionsMap.get(siteId);
			if (options == null) {
				options = new ArrayList<>();
				siteOptionsMap.put(siteId, options);
			}

			options.add(name.substring(name.indexOf("-import-option-") + 15));
		}

		state.setAttribute(STATE_IMPORT_SITE_TOOL_OPTIONS, toolOptions);

		return anyToolSelected || toolOptions.size() > 0;
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
		
	} // doMenu_site_hard_delete
	
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
				Site s = siteService.getSite(siteId);

				//check if softly deleted
				if(!s.isSoftlyDeleted()){
					log.warn("Tried to restore site that has not been marked for deletion: " + siteId);
					continue;
				}

				//reverse it
				s.setSoftlyDeleted(false);
				siteService.save(s);

			} catch (IdUnusedException e) {
				log.warn("Error restoring site:" + siteId + ":" + e.getClass() + ":" + e.getMessage());
				addAlert(state, rb.getString("softly.deleted.invalidsite"));
			} catch (PermissionException e) {
				log.warn("Error restoring site:" + siteId + ":" + e.getClass() + ":" + e.getMessage());
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
			log.warn("SiteAction.doSite_delete_confirmed selectedMembers null");
			state.setAttribute(STATE_TEMPLATE_INDEX, "0"); // return to the site list
			return;
		}
		List chosenList = new ArrayList(Arrays.asList(params.getStrings("selectedMembers"))); // Site id's of checked sites
		
		boolean hardDelete = false;
		if(StringUtils.equalsIgnoreCase((String)state.getAttribute(STATE_HARD_DELETE), Boolean.TRUE.toString())) {
			hardDelete = true;
			state.removeAttribute(STATE_HARD_DELETE);
		}
		if (!chosenList.isEmpty()) {
			
			for (ListIterator i = chosenList.listIterator(); i.hasNext();) {
				String id = (String) i.next();
				String site_title = NULL_STRING;
				if (siteService.allowRemoveSite(id)) {
					try {
						Site site = siteService.getSite(id);
						site_title = site.getTitle();

						//now delete the site
						siteService.removeSite(site, hardDelete);
						log.debug("Removed site: " + site.getId());

						// As we do not want to introduce Rubrics dependencies in the Kernel, delete the Site Rubrics here.
						if (hardDelete) {
							try {
								rubricsService.deleteSiteRubrics(site.getId());
							} catch(Exception ex) {
								log.error("Error deleting site Rubrics for the site {}. {}", site.getId(), ex.getMessage());
							}
						}

					} catch (IdUnusedException e) {
						log.error(this +".doSite_delete_confirmed - IdUnusedException " + id, e);
						addAlert(state, rb.getFormattedMessage("java.couldnt", new Object[]{site_title,id}));
					} catch (PermissionException e) {
						log.error(this + ".doSite_delete_confirmed -  PermissionException, site " + site_title + "(" + id + ").", e);
						addAlert(state, rb.getFormattedMessage("java.dontperm", new Object[]{site_title}));
					}
				} else {
					log.warn(this + ".doSite_delete_confirmed -  allowRemoveSite failed for site "+ id);
					addAlert(state, rb.getFormattedMessage("java.dontperm", new Object[]{site_title}));
				}
			}
		}
		state.setAttribute(STATE_TEMPLATE_INDEX, "0"); // return to the site
		// list

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
				site = siteService.getSite((String) state
						.getAttribute(STATE_SITE_INSTANCE_ID));
			} catch (Exception ignore) {
			}
		}
		if ( site == null && autoContext ) {
			String siteId = toolManager.getCurrentPlacement().getContext();
			try {
				site = siteService.getSite(siteId);
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
		 * new java.util.Date(); log.debug("***1. start preparing
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
			if (siteTypeUtil.isCourseSite(type)) { // UMICH-1035
				// redirect
				redirectCourseCreation(params, state, "selectTerm");
			} else if (siteTypeUtil.isProjectSite(type)) { // UMICH-1035
				state.setAttribute(STATE_TEMPLATE_INDEX, "13");
			} else if (pSiteTypes != null && pSiteTypes.contains(siteTypeUtil.getTargetSiteType(type))) {  // UMICH-1035
				// if of customized type site use pre-defined site info and exclude
				// from public listing
				SiteInfo siteInfo = new SiteInfo();
				if (state.getAttribute(STATE_SITE_INFO) != null) {
					siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
				}
				User currentUser = userDirectoryService.getCurrentUser();
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
				templateSite = siteService.getSite(templateSiteId);
				// save the template site in state
				state.setAttribute(STATE_TEMPLATE_SITE, templateSite);
			     
				// the new site type is based on the template site
				setNewSiteType(state, templateSite.getType());
			}catch (Exception e) {  
				// should never happened, as the list of templates are generated
				// from existing sites
				log.error(this + ".doSite_type" + e.getClass().getName(), e);
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
			
			// SAK-24423 - update site info for joinable site settings
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
	 */
	private void isFutureTermSelected(SessionState state) {
		AcademicSession t = (AcademicSession) state.getAttribute(STATE_TERM_SELECTED);
		int weeks = 0;
		Calendar c = (Calendar) Calendar.getInstance().clone();
		try {
			weeks = Integer.parseInt(serverConfigurationService.getString("roster.available.weeks.before.term.start", "0"));
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
						addAlert(state, rb.getFormattedMessage("java.author", new Object[]{serverConfigurationService.getString("officialAccountName")}));
					} else {
						// in case of multiple instructors
						List instructors = new ArrayList(Arrays.asList(uniqname.split(",")));
						for (Iterator iInstructors = instructors.iterator(); iInstructors.hasNext();)
						{
							String eid = StringUtils.trimToEmpty((String) iInstructors.next());
							try {
								userDirectoryService.getUserByEid(eid);
							} catch (UserNotDefinedException e) {
								addAlert(state, rb.getFormattedMessage("java.validAuthor", new Object[]{serverConfigurationService.getString("officialAccountName")}));
								log.error(this + ".doManual_add_course: cannot find user with eid=" + eid, e);
							}
						}
					}
				}
				if (state.getAttribute(STATE_MESSAGE) == null) {
					if (state.getAttribute(STATE_TEMPLATE_SITE) != null)
					{
						// create site based on template
						state.setAttribute(STATE_TEMPLATE_INDEX, "18");
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
		} else if (option.equalsIgnoreCase("norosters")) {
			prepareStateForContinueWithNoRoster(state);
		}

	} // doManual_add_course

	private void prepareStateForContinueWithNoRoster(SessionState state)
	{
		// clear title if it has been populated by previous selection of roster
		// and not altered by the user.
		SiteInfo sinfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
		List providerChosenList = (List) state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		if (providerChosenList != null && providerChosenList.size() >= 1) {
			String preparedTitle = prepareTitle((List) state.getAttribute(STATE_PROVIDER_SECTION_LIST),	providerChosenList);
			if (sinfo != null && StringUtils.equals(preparedTitle, sinfo.title))
			{
				sinfo.title = "";
			}
		}

		String manualTitle = constructManualAddTitle(state);
		if (sinfo != null && StringUtils.equals(manualTitle, sinfo.title))
		{
			sinfo.title = "";
		}

		// clear description if it has been populated by previous selection of roster
		// and not altered by the user
		String constructedDesc = constructDescription(state, "");
		if (sinfo != null && StringUtils.equals(constructedDesc, sinfo.description))
		{
			sinfo.description = "";
		}

		// clear any previous selection of roster
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER_DESCRIPTION_CHOSEN);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_NUMBER);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);

		state.setAttribute(STATE_TEMPLATE_INDEX, "13");
	}

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
			siteInfo.title = constructManualAddTitle(state);
			state.setAttribute(STATE_SITE_INFO, siteInfo);
		}

	} // readCourseSectionInfo

	private String constructManualAddTitle(SessionState state)
	{
		String title = "";
		List providerCourseList = (List) state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		List multiCourseInputs = (List) state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
		AcademicSession t = (AcademicSession) state.getAttribute(STATE_TERM_SELECTED);
		if ((providerCourseList == null || providerCourseList.size() == 0)
				&& multiCourseInputs != null && multiCourseInputs.size() > 0) {
			String sectionEid = sectionFieldProvider.getSectionEid(t.getEid(),
					(List) multiCourseInputs.get(0));
			// default title
			title = sectionFieldProvider.getSectionTitle(t.getEid(), (List) multiCourseInputs.get(0));
			try {
				Section s = courseManagementService.getSection(sectionEid);
				title = s != null?s.getTitle():title;
			} catch (IdNotFoundException e) {
				log.warn("readCourseSectionInfo: Cannot find section " + sectionEid);
			}
		}

		return title;
	}

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
		User u = userDirectoryService.getCurrentUser();
		if (u != null)
		{
			siteInfo.site_contact_name=u.getDisplayName();
			siteInfo.site_contact_email=u.getEmail();
		}
		state.setAttribute(STATE_SITE_INFO, siteInfo);

		// set tool registration list
		if (!"copy".equals(type))
		{
			setToolRegistrationList(state, type, false);
		}
	}


	/** SAK16600 insert current site type into context
	 * @param	context			current context 
	 * @param	type				current type
	 * @return	courseSiteType	type of 'course'
	 */	
	private void setTypeIntoContext(Context context, String type) {
		if (type != null && siteTypeUtil.isCourseSite(type)) {
			context.put("isCourseSite", Boolean.TRUE);
			context.put("isProjectSite", Boolean.FALSE);
		} else {
			context.put("isCourseSite", Boolean.FALSE);
			if (type != null && siteTypeUtil.isProjectSite(type)) {
				context.put("isProjectSite", Boolean.TRUE);
			}
		}
	}


/** 
 * Set the state variables for tool registration list basd on current site type, save to STATE_TOOL_GROUP_LIST.  This list should include
 * all tool types - normal, home, multiples and lti.  Note that if the toolOrder.xml is in the original format, this list will consist of
 * all tools in a single group
 * @param state
 * @param site
 */
private Map<String, List<MyTool>> getTools(SessionState state, String type, Site site) {

	boolean checkHome = BooleanUtils.toBooleanDefaultIfNull((Boolean) state.getAttribute(STATE_TOOL_HOME_SELECTED), true);
	boolean isNewToolOrderType = serverConfigurationService.getBoolean("config.sitemanage.useToolGroup", false);
	boolean requireCourseNavPlacement = serverConfigurationService.getBoolean("site-manage.requireCourseNavPlacement", true);
	boolean useSeparateExternalToolsGroup = serverConfigurationService.getBoolean("site-manage.useExternalToolsGroup", false);
	String defaultGroupName = rb.getString("tool.group.default");
	Map<String, List<MyTool>> toolGroup = new LinkedHashMap<>();

	File moreInfoDir = new File(moreInfoPath);
	
	// if this is legacy format toolOrder.xml file, get all tools by siteType
	if (!isNewToolOrderType) {
		toolGroup.put(defaultGroupName, getOrderedToolList(state, defaultGroupName, type, checkHome));
	} else {	
		// get all the groups that are available for this site type
		List<String> groups = serverConfigurationService.getCategoryGroups(siteTypeUtil.getTargetSiteType(type));
		for (String groupId : groups) {
			String groupName = getGroupName(groupId);
			List<MyTool> toolList = getGroupedToolList(groupId, groupName, type, checkHome, moreInfoDir);
			if (!toolList.isEmpty()) toolGroup.put(groupName, toolList);
		}

		// add ungroups tools to end of toolGroup list
		String ungroupedName = getGroupName(UNGROUPED_TOOL_TITLE);
		List<MyTool> ungroupedList = getUngroupedTools(ungroupedName, toolGroup, state, moreInfoDir, site);
		if (!ungroupedList.isEmpty()) toolGroup.put(ungroupedName, ungroupedList);
	}

	// add external tools to end of toolGroup list
	String externaltoolgroupname = getGroupName(LTI_TOOL_TITLE);

	List<MyTool> externalTools = getLtiToolGroup(externaltoolgroupname, moreInfoDir, site, requireCourseNavPlacement);

	if (!externalTools.isEmpty() && useSeparateExternalToolsGroup) {
		toolGroup.put(externaltoolgroupname, externalTools);
	} else if (!externalTools.isEmpty()) {
		List<MyTool> combinedList = toolGroup.get(defaultGroupName);
		combinedList.addAll(externalTools);
		Collections.sort(combinedList, new Comparator<MyTool>(){
			public int compare(MyTool t1, MyTool t2) {
				return t1.getTitle().compareToIgnoreCase(t2.getTitle());
			}
		});
		toolGroup.put(defaultGroupName, combinedList);
	}
	
	if (checkHome) {
		// Home page should be auto-selected
		state.setAttribute(STATE_TOOL_HOME_SELECTED, Boolean.TRUE);
	}
	
	List<String> toolRegistrationSelectedList = (List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
	if (toolRegistrationSelectedList == null) {
		// If this is a new site add these selected tools as the default
		List<String> selectedTools = toolGroup.values().stream().flatMap(list -> list.stream().filter(MyTool::getSelected).map(MyTool::getId)).collect(Collectors.toList());
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
	private List<MyTool> getOrderedToolList(SessionState state, String groupName, String type, boolean checkhome) {
		MyTool newTool = null;
		List<MyTool> toolsInOrderedList = new ArrayList<>();
		
		// see setToolRegistrationList()
		List<MyTool> toolList = (List)state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		List<String> chosenList = (List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		List<String> stealthedToolIds = serverConfigurationService.getStringList("stealthTools@org.sakaiproject.tool.api.ActiveToolManager", Collections.emptyList());

		// mark the required tools
		List requiredTools = serverConfigurationService.getToolsRequired(siteTypeUtil.getTargetSiteType(type));
		
		// mark the default tools
		List defaultTools = serverConfigurationService.getDefaultTools(siteTypeUtil.getTargetSiteType(type));
		
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

				if (!stealthedToolIds.contains(toolId) || (stealthedToolIds.contains(toolId) && chosenList.contains(toolId))) {
					toolsInOrderedList.add(newTool);
				}
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
	private List<MyTool> getGroupedToolList(String groupId, String groupName, String type, boolean checkhome, File moreInfoDir ) {
		List toolsInGroup = new ArrayList();
		MyTool newTool = null;
		List toolList = serverConfigurationService.getToolGroup(groupId);
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
						newTool.required = serverConfigurationService.toolGroupIsRequired(groupId,TOOL_ID_HOME);
						newTool.multiple = false;
					} else {
						Tool tr = toolManager.getTool(id);
						if (tr != null) 
						{
								String toolId = tr.getId();
								if (isSiteTypeInToolCategory(siteTypeUtil.getTargetSiteType(type), tr) && !toolManager.isStealthed(toolId) ) // SAK 23808
								{
									newTool = new MyTool();
									newTool.title = tr.getTitle();
									newTool.id = toolId;
									newTool.description = tr.getDescription();
									newTool.group = groupName;
									newTool.moreInfo =  getMoreInfoUrl(moreInfoDir, toolId);
									newTool.required = serverConfigurationService.toolGroupIsRequired(groupId,toolId);
									newTool.selected = serverConfigurationService.toolGroupIsSelected(groupId,toolId);
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
		log.debug(groupName + ": loaded " + new Integer(toolsInGroup.size()).toString() + " tools");
		return toolsInGroup;
		
	}

	/*
	 * Given groupId, return localized name from tools.properties
	 */
	private String getGroupName(String groupId) {
		// undefined group will return standard '[missing key]' error string
		return toolManager.getLocalizedToolProperty(groupId,"title");
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
			log.info("unable to read moreinfo: " + e.getMessage() );
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
			log.info("unable to read moreinfo" + e.getMessage());
		}
		return moreInfoUrl;

	}


	private List<String> selectedLTITools(Site site) {
		List selectedLTI = new ArrayList();
		if (site !=null) {
			String siteId = site.getId();
			List<Map<String,Object>> contents = ltiService.getContents(null,null,0,0, site.getId());
			for ( Map<String,Object> content : contents ) {
				String ltiToolId = content.get(ltiService.LTI_TOOL_ID).toString();
				String ltiSiteId = StringUtils.trimToNull((String) content.get(LTIService.LTI_SITE_ID));
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

		Map<String,Object> toolProps= ltiService.getContent(toolId, siteId); // getTool(toolId);
		if (toolProps==null){
			return false;
		} else {
			String toolSite = StringUtils.trimToNull((String) toolProps.get(LTIService.LTI_SITE_ID));

			return siteId.equals(toolSite);
		}
	}


	/* SAK 16600  if toolGroup mode is active; and toolGroups don't use all available tools, put remaining tools into 
	 * 'ungrouped' group having name 'GroupName'
	 * @param	moreInfoDir		file pointer to directory of MoreInfo content
	 * @param	site				current site
	 * @return	list of MyTool items 
	 */
	private List<MyTool> getUngroupedTools(String ungroupedName, Map<String, List<MyTool>> toolsByGroup, SessionState state, File moreInforDir, Site site) {
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

	/**
	 * Get list of LTI tools that are deployed in a site but no longer appear in the available tools list
	 * (stranded tools). This can happen when tools are stealthed, deleted, or have restrictions changed
	 * after being deployed to sites.
	 *
	 * @param site The site to check for stranded tools
	 * @param requireCourseNavPlacement Limit tools to those that have Course Navigation placement indicated
	 * @return List of MyTool objects representing stranded tools
	 */
	private List<MyTool> getStrandedLTITools(Site site, boolean requireCourseNavPlacement) {
		List<MyTool> strandedTools = new ArrayList<>();
		if (site == null) {
			return strandedTools;
		}

		String siteId = site.getId();
		List<String> ltiSelectedTools = selectedLTITools(site);

		// Get the list of currently available LTI tools
		List<Map<String, Object>> allTools;
		if (requireCourseNavPlacement) {
			allTools = ltiService.getToolsLaunchCourseNav(siteId, false);
		} else {
			allTools = ltiService.getToolsLaunch(siteId, true);
		}

		// Build a set of all available tool IDs for efficient lookup
		Set<String> allToolIds = new HashSet<>();
		if (allTools != null) {
			for (Map<String, Object> tool : allTools) {
				String toolIdString = ObjectUtils.toString(tool.get(LTIService.LTI_ID));
				allToolIds.add(toolIdString);
			}
		}

		// Find tools that are selected but not in the allTools list
		List<String> missingToolIds = new ArrayList<>();
		for (String selectedToolId : ltiSelectedTools) {
			if (!allToolIds.contains(selectedToolId)) {
				missingToolIds.add(selectedToolId);
			}
		}

		// Build MyTool objects for each stranded tool
		if (!missingToolIds.isEmpty()) {
			log.debug("Found {} stranded LTI tools in site {} not in available tools list: {}",
				missingToolIds.size(), siteId, missingToolIds);

			for (String missingToolId : missingToolIds) {
				try {
					Map<String, Object> toolInfo = ltiService.getToolDao(Long.valueOf(missingToolId), siteId);
					if (toolInfo != null) {
						String title = ObjectUtils.toString(toolInfo.get("title"), "Unknown");
						String visible = ObjectUtils.toString(toolInfo.get(LTIService.LTI_VISIBLE), "0");
						String description = ObjectUtils.toString(toolInfo.get("description"), "");

						log.debug("Stranded tool ID {}: title='{}', visible='{}', site_id='{}'",
							missingToolId, title, visible, siteId);

						// Create a MyTool for this stranded tool
						MyTool strandedTool = new MyTool();
						strandedTool.title = title;
						strandedTool.id = LTITOOL_ID_PREFIX + missingToolId;
						strandedTool.description = description;
						strandedTool.selected = true; // It's in the site
						strandedTool.required = false;
						strandedTools.add(strandedTool);
					} else {
						log.debug("Stranded tool ID {}: Unable to retrieve tool information (tool may have been deleted)",
							missingToolId);
					}
				} catch (Exception e) {
					log.debug("Stranded tool ID {}: Error retrieving tool information: {}",
						missingToolId, e.getMessage());
				}
			}
		}

		return strandedTools;
	}

	/* SAK 16600  Create  list of ltitools to add to toolgroups; set selected for those
	// tools already added to a sites with properties read to add to toolsByGroup list
	 * @param	groupName		name of the current group
	 * @param	moreInfoDir		file pointer to directory of MoreInfo content
	 * @param	site				current site
	 * @param   requireCourseNavPlacement Limit tools to those that have Course Navigation placement indicated
	 * @return	list of MyTool items
	 */
	private List<MyTool> getLtiToolGroup(String groupName, File moreInfoDir, Site site, boolean requireCourseNavPlacement) {
		List<String> ltiSelectedTools = selectedLTITools(site);
		List <MyTool> ltiTools = new ArrayList<>();
		List<Map<String, Object>> allTools;
		Map <String, Integer> toolOrder = new HashMap<>();
		String siteId = "";
		if ( site != null ) siteId = Objects.toString(site.getId(), "");

		if ( requireCourseNavPlacement ) {
			allTools = ltiService.getToolsLaunchCourseNav(siteId, false);
		} else {
			allTools = ltiService.getToolsLaunch(siteId, true);
		}

		if (allTools != null && !allTools.isEmpty()) {
			for (Map<String, Object> tool : allTools) {
				String toolIdString = ObjectUtils.toString(tool.get(LTIService.LTI_ID));
				boolean toolStealthed = "1".equals(ObjectUtils.toString(tool.get(LTIService.LTI_VISIBLE)));
				boolean ltiToolSelected = ltiSelectedTools.contains(toolIdString); 
				String siteRestriction = Objects.toString(tool.get(LTIService.LTI_SITE_ID), "");
				boolean allowedForSite = siteRestriction.isEmpty() || siteRestriction.equals(siteId);
				try
				{
					// in Oracle, both the lti tool id and the toolorder are returned as BigDecimal, which cannot be cast into Integer directly
					Integer ltiId = Integer.valueOf(toolIdString);
					if (ltiId != null) {
						String ltiToolId = ltiId.toString();
						boolean toolDeployed = ltiService.toolDeployed(Long.valueOf(toolIdString), siteId);

						if (ltiToolId != null && ((!toolStealthed && allowedForSite) || ltiToolSelected || toolDeployed) ) {
							String relativeWebPath = null;
							MyTool newTool = new MyTool();
							newTool.title = StringUtils.defaultString(tool.get("title").toString());
							newTool.id = LTITOOL_ID_PREFIX + ltiToolId;
							newTool.description = (String) tool.get("description");
							newTool.group = groupName;
							relativeWebPath = getMoreInfoUrl(moreInfoDir, ltiToolId);
							Integer order = NumberUtils.toInt(Objects.toString(tool.get("toolorder")), -1);
							if (order >= 0) {
								toolOrder.put(newTool.id, order);
							}
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
					log.error(this + " Cannot cast tool id String " + toolIdString + " into integer value.");
				}
			}
		}
		Collections.sort(ltiTools, new Comparator<MyTool>() {
		    public int compare(MyTool t1, MyTool t2) {
		    	int result = ObjectUtils.compare(toolOrder.get(t1.getId()),toolOrder.get(t2.getId()),true);
		    	if (result == 0) {
		    		result =  ObjectUtils.compare(t1.getTitle(), t2.getTitle());
		    	}
		    	return result;
		    }
		});
		return ltiTools;
	}


	/**
	 * Set the state variables for tool registration list basd on site type
	 * @param state
	 * @param type
	 */
	private void setToolRegistrationList(SessionState state, String type, boolean includeStealthed) {
		state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME);
		state.removeAttribute(STATE_MULTIPLE_TOOL_ID_SET);
		
		// get the tool id set which allows for multiple instances
		Set multipleToolIdSet = new HashSet();
		HashMap multipleToolConfiguration = new HashMap<String, HashMap<String, String>>();
		// get registered tools list
		Set<Tool> toolRegistrations = getToolRegistrations(state, type, includeStealthed);

		List<MyTool> tools = new Vector();
		SortedIterator i = new SortedIterator(toolRegistrations.iterator(), new ToolTitleComparator());
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
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		int index = Integer.valueOf(params.getString("templateIndex")).intValue();

		// Let actionForTemplate know to make any permanent changes before
		// continuing to the next template
		String direction = "continue";
		String option = params.getString("option");

		log.debug("doContinue index={} option={}", index, option);
		actionForTemplate(direction, index, params, state, data);
		if (state.getAttribute(STATE_MESSAGE) == null) {
			if (index == 36 && ("add").equals(option)) {
				// this is the Add extra Roster(s) case after a site is created
				state.setAttribute(STATE_TEMPLATE_INDEX, "44");
			} else if(index == 65) { //after manage overview, go back to where the call was made
				String pageId = params.getString("back");
				if(StringUtils.isNotEmpty(pageId) && !"12".equals(pageId)) {
					String redirectionUrl = getDefaultSiteUrl(toolManager.getCurrentPlacement().getContext()) + "/" + siteService.PAGE_SUBTYPE + "/" + pageId;
					sendParentRedirect((HttpServletResponse) threadLocalManager.get(RequestFilter.CURRENT_HTTP_RESPONSE), redirectionUrl);
				}
				state.setAttribute(STATE_TEMPLATE_INDEX, "12");
			}else if (params.getString("continue") != null) {
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
			prepareStateForContinueWithNoRoster(state);
		}
		else if (option.equalsIgnoreCase("change_user")) {  // SAK-22915
			doChange_user(data);
		}
		else if (option.equalsIgnoreCase("change")) {
			// change term
			String termId = params.getString("selectTerm");
			AcademicSession t = courseManagementService.getAcademicSession(termId);
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
				securityService.pushAdvisor(new SecurityAdvisor()
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
					securityService.popAdvisor();
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
				    site = siteService.getSite(site.getId());
				} catch (Exception ee) {
				    log.error(this + "doFinish: unable to reload site " + site.getId() + " after copying tools");
				}
			}
			else
			{
				// creating based on template
				if (state.getAttribute(STATE_TEMPLATE_SITE_COPY_CONTENT) != null)
				{
					// create based on template: skip add features, and copying all the contents from the tools in template site
					siteManageService.importToolContent(templateSite.getId(), site, true);
					try {
					    site = siteService.getSite(site.getId());
					} catch (Exception ee) {
					    log.error(this + "doFinish: unable to reload site " + site.getId() + " after importing tools");
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
						log.warn(this + "doFinish: copy user exception template site =" + templateSite.getReference() + " new site =" + site.getReference() + " " + copyUserException.getMessage());
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
						String currentUserId = userDirectoryService.getCurrentUser().getId();
						if (newGroup.getUserRole(currentUserId) == null)
						{
							// add advisor
							securityService.pushAdvisor(new SecurityAdvisor()
							{
								public SecurityAdvice isAllowed(String userId, String function, String reference)
								{
									return SecurityAdvice.ALLOWED;
								}
							});
							newGroup.addMember(currentUserId, newGroup.getMaintainRole(), true, false);
							authzGroupService.save(newGroup);
							
							// remove advisor
							securityService.popAdvisor();
						}
					}
					catch (Exception removeProviderException)
					{
						log.warn(this + "doFinish: remove provider id " + " new site =" + site.getReference() + " " + removeProviderException.getMessage());
					}
					
					try {
					    site = siteService.getSite(site.getId());
					} catch (Exception ee) {
					    log.error(this + "doFinish: unable to reload site " + site.getId() + " after updating roster.");
					}
				}

				String skin = templateSite.getSkin();
				if (StringUtils.isNotBlank(skin)) {
					site.setSkin(skin);
				}

				String isMathjaxEnabled = templateSite.getPropertiesEdit().getProperty(Site.PROP_SITE_MATHJAX_ALLOWED);
				if (StringUtils.isNotBlank(isMathjaxEnabled)) {
					site.getPropertiesEdit().addProperty(Site.PROP_SITE_MATHJAX_ALLOWED, isMathjaxEnabled);
				}

				// We don't want the new site to automatically be a template
				site.getPropertiesEdit().removeProperty("template");
				
				// publish the site or not based on the template choice
				site.setPublished(state.getAttribute(STATE_TEMPLATE_PUBLISH) != null?true:false);
				
				// Update the icons URL.
				String newSiteIconUrl = siteManageService.transferSiteResource(templateSite.getId(), site.getId(), site.getIconUrl());
				site.setIconUrl(newSiteIconUrl);
				
				userNotificationProvider.notifyTemplateUse(templateSite, userDirectoryService.getCurrentUser(), site);
			}
				
			ResourcePropertiesEdit rp = site.getPropertiesEdit();

			// for course sites
			String siteType = site.getType();
			if (siteTypeUtil.isCourseSite(siteType)) {
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

			String sitePublishType = rp.getProperty(SITE_PUBLISH_TYPE);
			if (!StringUtils.equalsAny(sitePublishType, SITE_PUBLISH_TYPE_AUTO, SITE_PUBLISH_TYPE_SCHEDULED, SITE_PUBLISH_TYPE_MANUAL)) {
				rp.addProperty(SITE_PUBLISH_TYPE, defaultPublishType);
				sitePublishType = defaultPublishType;
			}

			if (SITE_PUBLISH_TYPE_AUTO.equals(sitePublishType)) {
				Date termstart = new Date(courseManagementService.getAcademicSession(rp.getProperty(Site.PROP_SITE_TERM_EID)).getStartDate().getTime() + (ONE_DAY_IN_MS * serverConfigurationService.getInt("course_site_publish_service.num_days_before_term_starts", 0)));
				Date termend = new Date(courseManagementService.getAcademicSession(rp.getProperty(Site.PROP_SITE_TERM_EID)).getEndDate().getTime() + (ONE_DAY_IN_MS * serverConfigurationService.getInt("course_site_removal_service.num_days_after_term_ends", 14)));
				if(Instant.now().isAfter(termstart.toInstant()) && Instant.now().isBefore(termend.toInstant())) {
					site.setPublished(true);
				} else {
					site.setPublished(false);
				}
			} else if (SITE_PUBLISH_TYPE_SCHEDULED.equals(sitePublishType)) {
				Date publishingDate;
				Date unpublishingDate;
				if (rp.getProperty(SITE_PUBLISH_DATE) != null) {
					publishingDate = userTimeService.parseISODateInUserTimezone(String.valueOf(state.getAttribute(SITE_PUBLISH_DATE)));
					if (Instant.now().isAfter(publishingDate.toInstant())) {
						site.setPublished(true);
					}
					if (rp.getProperty(SITE_UNPUBLISH_DATE) != null) {
						unpublishingDate = userTimeService.parseISODateInUserTimezone(String.valueOf(state.getAttribute(SITE_UNPUBLISH_DATE)));
						if (Instant.now().isAfter(unpublishingDate.toInstant())) {
							site.setPublished(false);
						}
					}
				}
			} else if (SITE_PUBLISH_TYPE_MANUAL.equals(sitePublishType)) {
				site.setPublished((boolean) state.getAttribute(STATE_SITE_ACCESS_PUBLISH));
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
		String siteReference = siteService.siteReference(siteId);
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
				log.error(this + ".setSiteReferenceAliases: " + rb.getFormattedMessage("java.delalias", new Object[]{aliasId}), e);
			} catch ( IdUnusedException e ) {
				// no problem
			} catch ( InUseException e ) {
				addAlert(state, rb.getFormattedMessage("java.delalias", new Object[]{aliasId}) + rb.getFormattedMessage("java.alias.locked", new Object[]{aliasId}));
				log.error(this + ".setSiteReferenceAliases: " + rb.getFormattedMessage("java.delalias", new Object[]{aliasId}) + rb.getFormattedMessage("java.alias.locked", new Object[]{aliasId}), e);
	 			}
	 		}
		for ( String aliasId : aliasIdsToAdd ) {
			try {
				aliasService.setAlias(aliasId, siteReference);
			} catch ( PermissionException e ) {
				addAlert(state, rb.getString("java.addalias") + " ");
				log.error(this + ".setSiteReferenceAliases: " + rb.getString("java.addalias"), e);
			} catch ( IdInvalidException e ) {
				addAlert(state, rb.getFormattedMessage("java.alias.isinval", new Object[]{aliasId}));
				log.error(this + ".setSiteReferenceAliases: " + rb.getFormattedMessage("java.alias.isinval", new Object[]{aliasId}), e);
			} catch ( IdUsedException e ) {
				addAlert(state, rb.getFormattedMessage("java.alias.exists", new Object[]{aliasId}));
				log.error(this + ".setSiteReferenceAliases: " + rb.getFormattedMessage("java.alias.exists", new Object[]{aliasId}), e);
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

		String realm = siteService.siteReference(siteId);

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
				log.error(this + ".updateCourseSiteSections: IdUnusedException, not found, or not an AuthzGroup object", e);
				addAlert(state, rb.getString("java.realm"));
			}
			catch (AuthzPermissionException e)
			{
				log.warn(this + rb.getString("java.notaccess"));
				addAlert(state, rb.getString("java.notaccess"));
			}

			sendSiteNotification(state, getStateSite(state), providerCourseList);
			//Track add changes
			trackRosterChanges(SiteService.EVENT_SITE_ROSTER_ADD, providerCourseList);
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
	 * @param id
	 *            The site id
	 */
	private String buildExternalRealm(String id, SessionState state,
			List<String> providerIdList, String existingProviderIdString) {
		String realm = siteService.siteReference(id);
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
		User cUser = userDirectoryService.getCurrentUser();
		String sendEmailToRequestee = null;
		StringBuilder buf = new StringBuilder();
		boolean requireAuthorizer = serverConfigurationService.getString(SAK_PROP_REQUIRE_AUTHORIZER, "true").equals("true")?true:false;
		String emailAuthorizer = serverConfigurationService.getString(SAK_PROP_EMAIL_AUTHORIZER, "");

		// get the request email from configuration
		String requestEmail = getSetupRequestEmailAddress();
		
		// get the request replyTo email from configuration
		String requestReplyToEmail = getSetupRequestEmailAddress();
		
		if (requestEmail != null) {
			String officialAccountName = serverConfigurationService.getString("officialAccountName", "");

			SiteInfo siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);

			Site site = getStateSite(state);
			String id = site.getId();
			String title = site.getTitle();

			AcademicSession term = null;
			boolean termExist = false;
			if (state.getAttribute(STATE_TERM_SELECTED) != null) {
				termExist = true;
				term = (AcademicSession) state
						.getAttribute(STATE_TERM_SELECTED);
			}
			String productionSiteName = serverConfigurationService.getServerName();

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
					// If emailAuthorizer is defined to be true  or if requireAuthorizer is set
					// If emailAuthorizer is true always send email, if it's unset send if requireAuthorizer is set
					// Otherwise don't send
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
		boolean courseSite = siteTypeUtil.isCourseSite(site.getType());
		
		String term_name = "";
		if (state.getAttribute(STATE_TERM_SELECTED) != null) {
			term_name = ((AcademicSession) state
					.getAttribute(STATE_TERM_SELECTED)).getTitle();
		}
		// get the request email from configuration
		String requestEmail = getSetupRequestEmailAddress();
		User currentUser = userDirectoryService.getCurrentUser();
		// read from configuration whether to send out site notification emails, which defaults to be true
		boolean sendToRequestEmail = serverConfigurationService.getBoolean("site.setup.creation.notification", true);
		boolean sendToUser = serverConfigurationService.getBoolean("site.setup.creation.notification.user", true);
		if (requestEmail != null && currentUser != null && (sendToRequestEmail || sendToUser)) {
			userNotificationProvider.notifySiteCreation(site, notifySites, courseSite, term_name, requestEmail, sendToRequestEmail, sendToUser);
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

		state.setAttribute(STATE_CREATE_FROM_ARCHIVE, Boolean.FALSE);
		
		resetVisitedTemplateListToIndex(state, (String) state.getAttribute(STATE_TEMPLATE_INDEX));
		
		// remove state variables in tool editing
		removeEditToolState(state);

	} // doCancel_create

	/**
	 * doCancel_overview does a bit of cleanup before calling doCancel
	 */
	public void doCancel_overview(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.removeAttribute("tools");
		state.removeAttribute("leftTools");
		state.removeAttribute("rightTools");
		state.removeAttribute("overview");
		state.removeAttribute("site");
		state.removeAttribute("allWidgets");
		state.removeAttribute("fromHome");

		doCancel(data);
	}

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
			state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);
		} else if (getStateSite(state) != null && ("13".equals(currentIndex) || "14".equals(currentIndex)))
		{
			MathJaxEnabler.removeMathJaxAllowedAttributeFromState(state);  // SAK-22384
			state.setAttribute(STATE_TEMPLATE_INDEX, SiteConstants.SITE_INFO_TEMPLATE_INDEX);
			GradebookGroupEnabler.removeFromState(state);
			SubNavEnabler.removeFromState(state);
		} else if ("15".equals(currentIndex)) {
			params = data.getParameters();
			state.setAttribute(STATE_TEMPLATE_INDEX, params
					.getString("cancelIndex"));
			removeEditToolState(state);
		}
		// htripath: added '"45".equals(currentIndex)' for import from file
		// cancel
		else if ("45".equals(currentIndex)) {
			state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);
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
					state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);
				}
			} else if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITEINFO)) {
				// site info
				state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);
			}
			state.removeAttribute(STATE_IMPORT_SITE_TOOL);
			state.removeAttribute(STATE_IMPORT_SITE_TOOL_ITEMS);
			state.removeAttribute(STATE_IMPORT_SITES);
		} else if ("26".equals(currentIndex)) {
			if (((String) state.getAttribute(STATE_SITE_MODE))
					.equalsIgnoreCase(SITE_MODE_SITESETUP)
					&& getStateSite(state) == null) {
				// from creating site
				state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			} else {
				// from revising site
				state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);
			}
			removeEditToolState(state);
		} else if ("37".equals(currentIndex) || "44".equals(currentIndex) || "53".equals(currentIndex) || "36".equals(currentIndex)) {
			// cancel back to edit class view
			state.removeAttribute(STATE_TERM_SELECTED);
			removeAddClassContext(state);
			state.setAttribute(STATE_TEMPLATE_INDEX, "43");
		} else if (STATE_TEMPLATE_INDEX_MANAGE_PARTICIPANTS.equals(currentIndex)) {
			state.removeAttribute(SITE_USER_SEARCH);
			state.removeAttribute(STATE_SITE_PARTICIPANT_FILTER);
			state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);
		}
		else if ("65".equals(currentIndex)) { //after manage overview, go back to where the call was made
			String pageId = params.getString("back");
			if(StringUtils.isNotEmpty(pageId) && !"12".equals(pageId)) {
				String redirectionUrl = getDefaultSiteUrl(toolManager.getCurrentPlacement().getContext()) + "/" + siteService.PAGE_SUBTYPE + "/" + pageId;
				sendParentRedirect((HttpServletResponse) threadLocalManager.get(RequestFilter.CURRENT_HTTP_RESPONSE), redirectionUrl);
			}
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		}
		// if all fails to match
		else if (isTemplateVisited(state, SITE_INFO_TEMPLATE_INDEX)) {
			// go to site info list view
			state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);
		} else {
			if ("8".equals(currentIndex)) {
				state.removeAttribute(STATE_HARD_DELETE);
			}
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
	 * find whether there is an counterpart in the the multipleToolIdSet.
	 * Also only returns tools that are considered valid tools for this site type.
	 *
	 * @param state The session state to get the site type from.
	 * @param toolId The tool ID to find.
	 * @return <code>null</code> if the tool couldn't be found or the matched tool ID.
	 */
	private String findOriginalToolId(SessionState state, String toolId) {
		// treat home tool differently
		if (toolId.equals(TOOL_ID_HOME) || SiteManageConstants.SITE_INFO_TOOL_ID.equals(toolId))
		{
			return toolId;
		}
		else
		{
			Set<Tool>toolRegistrationSet = getToolRegistrations(state, (String) state.getAttribute(STATE_SITE_TYPE), false);
			String rv = null;
			if (toolRegistrationSet != null)
			{
				for (Iterator i=toolRegistrationSet.iterator(); rv == null && i.hasNext();)
				{
					Tool tool = (Tool) i.next();
					String tId = tool.getId();
					rv = originalToolId(toolId, tId);
				}
			}
			return rv;
		}
	}

	/**
	 * This extracts original tools IDs from compound IDs. The compound tools IDs are used when there
	 * is more than once copy of a tool on a site. For example lessonbuilder and web content can have
	 * multiple pages in the site on which they appear. It also filters out any tool IDs that aren't
	 * considered valid in this site.
     *
	 * @param toolIds The list of tool IDs
	 * @param state The session state.
	 * @return A filtered list of tool IDs.
	 */
	List<String> getOriginalToolIds(List<String>toolIds, SessionState state) {
		Set<String>rv = new LinkedHashSet<>();

		for (String toolId: toolIds) {
			String origToolId = findOriginalToolId(state, toolId);
			if (StringUtils.isNotBlank(origToolId)) {
				rv.add(origToolId);
			}
		}
		return new ArrayList<>(rv);
	}

	private String originalToolId(String toolId, String toolRegistrationId) {
		String rv = null;
		if (toolId.equals(toolRegistrationId))
		{
			rv = toolRegistrationId;
		}
		else if (toolId.indexOf(toolRegistrationId) != -1 && isMultipleInstancesAllowed(toolRegistrationId))
		{
			if (toolId.endsWith(toolRegistrationId))
			{
				// the multiple tool id format is of {page_id}{tool_id}
				// get the page id part out
				String uuid = toolId.replaceFirst(toolRegistrationId, "");
				if (uuid != null && uuid.length() == UUID_LENGTH)
					rv = toolRegistrationId;
			}
			else
			{
				// the multiple tool id format is of {tool_id}{x}, where x is an intger >= 1
				String suffix = toolId.substring(toolId.indexOf(toolRegistrationId) + toolRegistrationId.length());
				try
				{
					Integer.parseInt(suffix);
					rv = toolRegistrationId;
				}
				catch (Exception e)
				{
					// not the right tool id
					log.debug(this + ".findOriginalToolId not matching tool id = " + toolRegistrationId + " original tool id=" + toolId + e.getMessage(), e);
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
		Tool tool = toolManager.getTool(toolId);
		if (tool != null)
		{
			Properties tProperties = tool.getRegisteredConfig();
			return ((!"sakai.gradebookng".equals(toolId) || serverConfigurationService.getBoolean("gradebookng.multipleGroupInstances", false))
					&& tProperties.containsKey("allowMultipleInstances") && tProperties.getProperty("allowMultipleInstances").equalsIgnoreCase(Boolean.TRUE.toString()))?true:false;
		}
		return false;
	}
	
	/**
	 * Read from tool registration whether the tool may be duplicated across sites
	 * @param toolId
	 * @return
	*/
	private boolean isDuplicateAllowed(String toolId)
	{
		Tool tool = toolManager.getTool(toolId);
		if (tool != null) {
			Properties tProperties = tool.getRegisteredConfig();
			if (tProperties.containsKey("allowToolDuplicate") &&
			    tProperties.getProperty("allowToolDuplicate").equalsIgnoreCase(Boolean.FALSE.toString())) {
				return false;
			}
		}
		return true;
	}


	private void removeToolsNotForDuplication(Site site)
	{

		List<String> removePageIds = new ArrayList();

		for (SitePage page : site.getPages()) {

			boolean keepPage = false;

			List<String> removeToolIds = new ArrayList();

			for (ToolConfiguration t : page.getTools()) {
				if (isDuplicateAllowed(t.getToolId())) {
					keepPage = true;
				} else {
					removeToolIds.add(t.getId());
				}
			}

			// Remove tool if it may not be duplicated
			for (String removeId : removeToolIds) {
				ToolConfiguration t = page.getTool(removeId);
				page.removeTool(t);
			}

			if (!keepPage) {
				removePageIds.add(page.getId());
			}
		}

		// Remove page if it contains no tools that may be duplicated
		for (String removeId : removePageIds) {
			SitePage sitePage = site.getPage(removeId);
			site.removePage(sitePage);
		}

		return;
	}


	private HashMap<String, String> getMultiToolConfiguration(String toolId, ToolConfiguration toolConfig)
	{
		HashMap<String, String> rv = new HashMap<String, String>();
	
		// read attribute list from configuration file
		ArrayList<String> attributes=new ArrayList<String>();
		String attributesConfig = serverConfigurationService.getString(CONFIG_TOOL_ATTRIBUTE + toolId);
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
		String defaultValueConfig = serverConfigurationService.getString(CONFIG_TOOL_ATTRIBUTE_DEFAULT + toolId);
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
		state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);

	} // doMenu_siteInfo_cancel_access

	/**
	 * doMenu_siteInfo_importSelection
	 */
	public void doMenu_siteInfo_importSelection(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

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

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

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

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "61");	// import users
		}

	} // doMenu_siteInfo_import_user
	
	public void doMenu_siteInfo_importMigrate(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		// get the tools
		siteToolsIntoState(state);

		if (state.getAttribute(STATE_MESSAGE) == null) {
			state.setAttribute(STATE_TEMPLATE_INDEX, "59");
		}

	} // doMenu_siteInfo_importMigrate

	/**
	 * doMenu_siteInfo_manageParticipants
	 * @param data
	 */
	public void doMenu_siteInfo_manageParticipants(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		state.setAttribute(STATE_TEMPLATE_INDEX, STATE_TEMPLATE_INDEX_MANAGE_PARTICIPANTS);
	}

	/**
	 * doMenu_siteInfo
	 * @param data
	 */
	public void doMenu_siteInfo(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);
	}

	public void do_manageParticipants_changeFilter(RunData data) {
		ParticipantFilterHandler.putSelectedFilterIntoState(data);
	}

	/**
	 * doMenu_siteInfo_editClass
	 */
	public void doMenu_siteInfo_editClass(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		state.setAttribute(STATE_TEMPLATE_INDEX, "43");

	} // doMenu_siteInfo_editClass

	/**
	 * doMenu_siteInfo_addClass
	 */
	public void doMenu_siteInfo_addClass(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		Site site = getStateSite(state);
		String termEid = site.getProperties().getProperty(Site.PROP_SITE_TERM_EID);
		if (termEid == null)
		{
			// no term eid stored, need to get term eid from the term title
			String termTitle = site.getProperties().getProperty(Site.PROP_SITE_TERM);
			List asList = courseManagementService.getAcademicSessions();
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
							siteService.save(site);
						}
						catch (Exception e)
						{
							log.error(this + ".doMenu_siteinfo_addClass: " + e.getMessage() + site.getId(), e);
						}
						found=true;
					}
				}
			}
		}
		
		if (termEid != null)
		{
			state.setAttribute(STATE_TERM_SELECTED, courseManagementService.getAcademicSession(termEid));
			
			try
			{
			List<CourseObject> sections = prepareCourseAndSectionListing(userDirectoryService.getCurrentUser().getEid(), courseManagementService.getAcademicSession(termEid).getEid(), state);
			isFutureTermSelected(state);
			if (sections != null && sections.size() > 0) 
				state.setAttribute(STATE_TERM_COURSE_LIST, sections);
			}
			catch (Exception e)
			{
				log.error(this + ".doMenu_siteinfo_addClass: " + e.getMessage() + termEid, e);
			}
		}
		else
		{
			List currentTerms = courseManagementService.getCurrentAcademicSessions();
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

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

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

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

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

    public void doMenu_edit_access(RunData data){
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(STATE_TEMPLATE_INDEX, "18");
            if (state.getAttribute(STATE_INITIALIZED) == null) {
                state.setAttribute(STATE_OVERRIDE_TEMPLATE_INDEX, "18");
            }
        }
    }

	/**
	 * doMenu_edit_site_access
	 *
	 */
	public void doMenu_edit_site_access(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// Check if the user has appropriate permissions
		if (!siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext())) {
			addAlert(state, rb.getString("java.notaccess"));
			return;
		}

		try {
			Site site = getStateSite(state);
			state.setAttribute(STATE_SITE_ACCESS_PUBLISH, Boolean.valueOf(site.isPublished()));
			state.setAttribute(STATE_SITE_ACCESS_INCLUDE, Boolean.valueOf(site.isPubView()));
			boolean joinable = site.isJoinable();
			state.setAttribute(STATE_JOINABLE, Boolean.valueOf(joinable));
			String joinerRole = site.getJoinerRole();
			if (joinerRole == null || joinerRole.length() == 0)
			{
				String[] joinerRoles = serverConfigurationService.getStrings("siteinfo.default_joiner_role");
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

			// SAK-24423 - update state for joinable site settings
			JoinableSiteSettings.updateStateFromSitePropertiesOnEditAccessOrNewSite( site.getProperties(), state );
		}
		catch (Exception e)
		{
			log.warn(this + " doMenu_edit_site_access problem of getting site" + e.getMessage());
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

		if (siteTitleEditable(state, siteTypeUtil.getTargetSiteType(site_type))) 
		{
			Site.setTitle(siteInfo.title);
		}

		//Process description so it doesn't give an error on home
		siteInfo.description = formattedText.processFormattedText(siteInfo.description, new StringBuilder());
		
		Site.setDescription(siteInfo.description);
		Site.setShortDescription(siteInfo.short_description);

		// set icon url for course
		setAppearance(state, Site, siteInfo.iconUrl);

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
		GradebookGroupEnabler.prepareSiteForSave(Site, state);

		SubNavEnabler.prepareSiteForSave(Site, state);

		if (state.getAttribute(STATE_MESSAGE) == null) {
			try {
				siteService.save(Site);
			} catch (IdUnusedException e) {
				// TODO:
			} catch (PermissionException e) {
				// TODO:
			}

			// back to site info view
			state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);

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
			// punts on page reference roots, using siteService.PAGE_SUBTYPE for
			// that portion of the URL
			//
			// None of this helps other users who may try to reload an aliased site 
			// URL that no longer resolves.
			if ( updateSiteRefAliases ) {
				sendParentRedirect((HttpServletResponse) threadLocalManager.get(RequestFilter.CURRENT_HTTP_RESPONSE),
					getDefaultSiteUrl(toolManager.getCurrentPlacement().getContext()) + "/" +
					siteService.PAGE_SUBTYPE + "/" + 
					((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId());
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
		if ( StringUtils.isBlank(site_type) ) return true;
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
		return serverConfigurationService.getBoolean("site-manage.enable.alias.edit", false);
	}
	
	/**
	 * Tests if alias assignment for new sites has been enabled by configuration. 
	 * This is independent of any permissioning considerations. 
	 * 
	 * <p>(Method name and signature is an attempt to be consistent with 
	 * {@link #siteTitleEditable(SessionState, String)}).</p>
	 * 
	 * @param state
	 * @return
	 */
	private boolean aliasAssignmentForNewSitesEnabled(SessionState state) {
		return serverConfigurationService.getBoolean("site-manage.enable.alias.new", false);
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
		String siteRef = siteService.siteReference(siteId);
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
		if (state.getAttribute(STATE_SITE_TYPES) == null) {
			PortletConfig config = portlet.getPortletConfig();

			// all site types (SITE_DEFAULT_LIST overrides tool config)
			String t = StringUtils.trimToNull(SITE_DEFAULT_LIST);
			if ( t == null )
				t = StringUtils.trimToNull(config.getInitParameter("siteTypes"));
			if (t != null) {
				List types = new ArrayList(Arrays.asList(t.split(",")));
				if (courseManagementService == null)
				{
					// if there is no CourseManagementService, disable the process of creating course site
					List<String> courseTypes = siteTypeUtil.getCourseSiteTypes();
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

			String siteId = toolManager.getCurrentPlacement().getContext();
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

		
		// show UI for adding non-official participant(s) or not
		// if nonOfficialAccount variable is set to be false inside sakai.properties file, do not show the UI section for adding them.
		// the setting defaults to be true
		if (state.getAttribute(ADD_NON_OFFICIAL_PARTICIPANT) == null)
		{
			state.setAttribute(ADD_NON_OFFICIAL_PARTICIPANT, serverConfigurationService.getString("nonOfficialAccount", "true"));
		}
		
		if (state.getAttribute(STATE_VISITED_TEMPLATES) == null)
		{
			List<String> templates = new Vector<String>();
			if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITESETUP)) {
				templates.add("0"); // the default page of WSetup tool
			} else if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITEINFO)) {
				templates.add(SITE_INFO_TEMPLATE_INDEX);// the default page of Site Info tool
			}

			state.setAttribute(STATE_VISITED_TEMPLATES, templates);
		}
		if (state.getAttribute(STATE_SITE_TITLE_MAX) == null) {
			int siteTitleMaxLength = serverConfigurationService.getInt("site.title.maxlength", 25);
			state.setAttribute(STATE_SITE_TITLE_MAX, siteTitleMaxLength);
		}

	} // init

	public void doNavigate_to_site(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		//remove search user attribute from the state
		state.removeAttribute(SITE_USER_SEARCH);
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
			Site site = siteService.getSite(siteId);
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
			log.error(this + ".getReviseSite: " +  e.toString() + " site id = " + siteId, e);
		}

		// one site has been selected
		state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);

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
			currentSite = siteService.getSite(toolManager.getCurrentPlacement().getContext());
			if(currentSite != null){
				SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
				Group siteGroup = currentSite.getGroup(groupRef);
				//make sure its a joinable set:
				String currentJoinableSet = siteGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
				if (StringUtils.isNotBlank(currentJoinableSet)) {

					String userId = userDirectoryService.getCurrentUser().getId();
					// the following conditions must not be met for the user to join the group
					boolean isGroupClosedByDate = false;
					boolean isUserInJoinableSet = false;
					boolean isGroupFull = false;

					// 1st. make sure the close date hasn't been reached (if there is)
					String joinableCloseDate = siteGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_CLOSE_DATE);
					if (isGroupClosedByDate(joinableCloseDate)) {
						isGroupClosedByDate = true;
						addAlert(state, rb.getString("sinfo.list.joinable.closedByDate"));
					}

					// 2nd. each joinable set can have multiple associated groups, make sure the user doesn't join more than one of them
					for(Group group : currentSite.getGroupsWithMember(userId)) {
						String joinableSet = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
						if(StringUtils.isNotBlank(joinableSet)) {
							if (joinableSet.equals(currentJoinableSet)) {
								isUserInJoinableSet = true;
								addAlert(state, rb.getString("sinfo.list.joinable.onePerSet"));
							}
						}
					}

					// 3rd. make sure group max limit hasn't been reached:
					int max = NumberUtils.toInt(siteGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET_MAX), 0);
					int size = siteGroup.getMembers().size();
					if(size >= max) {
						isGroupFull = true;
					}

					// if all conditions keep being false, the user can be added to the group
					if(!isUserInJoinableSet && !isGroupClosedByDate && !isGroupFull) {
						// add current user as the maintainer
						Member member = currentSite.getMember(userId);
						if(member != null) {
							try{
								authzGroupService.joinGroup(groupRef, member.getRole().getId(), max);
							} catch (GroupNotDefinedException | AuthzPermissionException | AuthzRealmLockException e) {
								log.error("User [{}] cannot be inserted into group [{}], {}", userId, siteGroup.getId(), e.toString());
							} catch (GroupFullException e) {
								log.warn("User [{}] cannot be inserted into group [{}] because it is full.", userId, siteGroup.getId() );
								isGroupFull = true;
							}
						}
					}

					if (isGroupFull) {
						addAlert(state, rb.getString("sinfo.list.joinable.full"));
					}
				} else {
					addAlert(state, rb.getString("sinfo.list.joinable.notAnymore"));
				}

			}
		} catch (IdUnusedException e) {
			log.error("IdUnusedException while adding user to group: {}", groupRef, e);
		}
	}
	
	/**
	 * when user clicks "unjoin" for a joinable set
	 * @param data
	 */
	public void doUnjoinableSet(RunData data){
		ParameterParser params = data.getParameters();
		String groupRef = params.getString("group-ref");
		
		Site currentSite;
		try {
			currentSite = siteService.getSite(toolManager.getCurrentPlacement().getContext());
			if(currentSite != null){
				SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
				Group siteGroup = currentSite.getGroup(groupRef);
				//make sure its a joinable set:
				String joinableSet = siteGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
				if(StringUtils.isNotBlank(joinableSet)){
					// perform the action only if the close date hasn't been reached (if there is)
					String joinableCloseDate = siteGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_CLOSE_DATE);
					if (!isGroupClosedByDate(joinableCloseDate)) {
						try{
							AuthzGroup group = authzGroupService.getAuthzGroup(groupRef);
							//check that the user is already a member
							String userId = userDirectoryService.getCurrentUser().getId();
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
									SecurityAdvisor yesMan = new SecurityAdvisor() {
										public SecurityAdvice isAllowed(String userId, String function, String reference) {
											if (StringUtils.equalsIgnoreCase(function, siteService.SECURE_UPDATE_SITE)) {
												return SecurityAdvice.ALLOWED;
											} else {
												return SecurityAdvice.PASS;
											}
										}
									};

									try{
										siteGroup.deleteMember(userId);

										securityService.pushAdvisor(yesMan);
										siteService.saveGroupMembership(currentSite);
									} catch (AuthzRealmLockException e) {
										log.error(".doUnjoinableSet: User with id {} cannot be deleted from group with id {} because the group is locked", userId, siteGroup.getId());
									} catch (PermissionException e) {
										log.error("doUnjoinableSet: permission exception as userId={}", userId, e);
									} finally {
										securityService.popAdvisor(yesMan);
									}
								}
							}
						} catch (GroupNotDefinedException e) {
							log.error("Error removing user from group: {}", groupRef, e);
						}
					} else {
						addAlert(state, rb.getString("sinfo.list.joinable.closedByDate"));
					}
				} else {
					addAlert(state, rb.getString("sinfo.list.joinable.notAnymore"));
				}
			}
		} catch (IdUnusedException e) {
			log.error("IdUnusedException while removing user to group: {}", groupRef, e);
		}
	}
	
	private boolean isGroupClosedByDate(String joinableCloseDate) {
		if (joinableCloseDate != null) {
			try {
				LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);
				LocalDateTime closeDate = LocalDateTime.parse(joinableCloseDate);
				if (currentDate.isAfter(closeDate)) {
					return true;
				}
			} catch(DateTimeParseException e) {
				log.error("Error parsing joinable group close date: {}", joinableCloseDate, e);
			}
		}
		return false;
	}

	/**
	* SAK-23029 -  iterate through changed participants to see how many would have maintain role if all roles, status and deletion changes went through
	*
	*/ 
	private List<Participant> testProposedUpdates(List<Participant> participants, ParameterParser params, String maintainRole) {

		// create list of all partcipants that have been removed
		Set<String> removedParticipantIds = new HashSet();
		if (params.getStrings("selectedUser") != null) {
			removedParticipantIds.addAll(new ArrayList(Arrays.asList(params.getStrings("selectedUser"))));
		}

		// create list of all participants that have been inactivated
		Set<String> inactivatedParticipants = new HashSet();
		// create list of all participants that have been activated
		Set<String> activatedParticipants = new HashSet();
		for(Participant statusParticipant : participants ) {
			String activeGrantId = statusParticipant.getUniqname();
			String activeGrantField = "activeGrant" + activeGrantId;
		
			if (params.getString(activeGrantField) != null) { 
				boolean activeStatus = params.getString(activeGrantField).equalsIgnoreCase("true") ? true : false;
				if (activeStatus == false) {
					inactivatedParticipants.add(activeGrantId);
				}
				else {
					activatedParticipants.add(activeGrantId);
				}
			}
		}

		// now add only those partcipants whose new/current role is maintainer, is (still) active, and not marked for removal
		List<Participant> maintainersAfterUpdates = new ArrayList<Participant>();
		for(Participant roleParticipant : participants ) {
			String id = roleParticipant.getUniqname();
			String roleId = "role" + id;
			String newRole = params.getString(roleId);

			// skip any that are not already inactive or are not candidates for inactivation
			if ((!inactivatedParticipants.contains(id) && roleParticipant.isActive()) || (activatedParticipants.contains(id))) {
				 if (!removedParticipantIds.contains(id)) {
					if (StringUtils.isNotBlank(newRole)){
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
			log.warn("Ignoring non-POST request to update site access.");
			return;
		}
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		Site s = getStateSite(state);
		String realmId = siteService.siteReference(s.getId());
		
		// list of updated users
		List<String> userUpdated = new Vector<String>();
		// list of all removed user
		List<String> usersDeleted = new Vector<String>();
	
		if (authzGroupService.allowUpdate(realmId)
						|| siteService.allowUpdateSiteMembership(s.getId())) {
			try {
				// init variables useful for actual edits and mainainersAfterProposedChanges check
				AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realmId);
				String maintainRoleString = realmEdit.getMaintainRole();
				List<Participant> participants;
				//Check for search term
				String search = (String)state.getAttribute(SITE_USER_SEARCH);
				if(StringUtils.isNotBlank(search)) {
					// search term is provided, get the search-filtered list of participants from the other attribute.
					participants = new ArrayList<>((Collection) state.getAttribute(STATE_SITE_PARTICIPANT_LIST));
				} else {
					// search term not provided, get the list (either full list or filtered by 'view' drop down)
					participants = new ArrayList<>((Collection) state.getAttribute(STATE_PARTICIPANT_LIST));
				}

				// SAK-23029 Test proposed removals/updates; reject all where activeMainainer count would = 0 if all proposed changes were made
				// SAK-42185 need to provide full list of participants to test proposed updates, not filtered/search list
				List<Participant> allParticipants = new ArrayList<>(SiteParticipantHelper.prepareParticipants(s.getId(), SiteParticipantHelper.getProviderCourseList(s.getId())));
				List<Participant> maintainersAfterProposedChanges = testProposedUpdates(allParticipants, params, maintainRoleString);
				if (maintainersAfterProposedChanges.size() == 0) {
					addAlert(state, rb.getFormattedMessage("sitegen.siteinfolist.lastmaintainuseractive", new Object[] {maintainRoleString}));
					return;
				}

				// SAK23029 - proposed changes do not leave site w/o maintainers; proceed with any allowed updates
			
				// list of roles being added or removed
				HashSet<String>roles = new HashSet<String>();

				// List used for user auditing
				List<String[]> userAuditList = new ArrayList<String[]>();

				// remove all roles and then add back those that were checked
				for (Participant participant : participants) {
					String id = null;

					// added participant
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
							String internalUserId = userDirectoryService.getUserId(currentUserId);
							String[] userAuditString = {
									s.getId(),
									id,
									roleId,
									userAuditService.USER_AUDIT_ACTION_UPDATE,
									userAuditRegistration.getDatabaseSourceKey(),
									internalUserId
							};
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
							User user = userDirectoryService.getUser(rId);
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
									String internalUserId = userDirectoryService.getUserId(currentUserId);
									String[] userAuditString = {
											s.getId(),
											userId,
											role.getId(),
											userAuditService.USER_AUDIT_ACTION_REMOVE,
											userAuditRegistration.getDatabaseSourceKey(),
											internalUserId
									};
									userAuditList.add(userAuditString);
								}
							}
						} catch (UserNotDefinedException e) {
							log.error(this + ".doUpdate_participant: IdUnusedException " + rId + ". ", e);
							if (("admins".equals(showOrphanedMembers) && securityService.isSuperUser()) || ("maintainers".equals(showOrphanedMembers))) {
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

				// SAK-41181
				usersDeleted.stream().map(ud -> ud.substring(4)).collect(Collectors.toList()).forEach(ud -> {
					log.debug("Removing user uuid {} from the user site cache", ud);
					m_userSiteCache.remove(ud);
				});
				
				// do the audit logging - Doing this in one bulk call to the database will cause the actual audit stamp to be off by maybe 1 second at the most
				// but seems to be a better solution than call this multiple time for every update
				if (!userAuditList.isEmpty())
				{
					userAuditRegistration.addToUserAuditing(userAuditList);
				}

				// then update all related group realms for the role
				doUpdate_related_group_participants(s, realmId);

				// post event about the participant update
				eventTrackingService.post(eventTrackingService.newEvent(siteService.SECURE_UPDATE_SITE_MEMBERSHIP, realmEdit.getId(), false));

				// check the configuration setting, whether logging membership
				// change at individual level is allowed
				if (serverConfigurationService.getBoolean(
						SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, false)) {
					// event for each individual update
					for (String userChangedRole : userUpdated) {
						eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_USER_SITE_MEMBERSHIP_UPDATE, userChangedRole, true));
					}
					// event for each individual remove
					for (String userDeleted : usersDeleted) {
						eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_USER_SITE_MEMBERSHIP_REMOVE, userDeleted, true));
					}
				}
			} catch (GroupNotDefinedException | UserNotDefinedException e) {
				addAlert(state, rb.getString("java.problem2"));
				log.warn("Could not update participants in site {} ({}), {}", s.getId(), realmId, e.toString());
			} catch (AuthzPermissionException e) {
				addAlert(state, rb.getString("java.changeroles"));
				log.warn("Could not update participants in site {} ({}), {}", s.getId(), realmId, e.toString());
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
		boolean trackIndividualChange = serverConfigurationService.getBoolean(SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, false);
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
									try {
										g.deleteMember(gMemberId);
									} catch (IllegalStateException e) {
										log.error(".doUpdate_related_group_participants: User with id {} cannot be deleted from group with id {} because the group is locked", gMemberId, g.getId());
									}
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
										try {
											g.deleteMember(gMemberId);
											g.insertMember(gMemberId, siteRole.getId(), siteMember.isActive(), false);
										} catch (IllegalStateException e) {
											log.error(".doUpdate_related_group_participants: User with id {} cannot be deleted from group with id {} because the group is locked", gMemberId, g.getId());
										}
										// track the group membership change at individual level
										if (trackIndividualChange)
										{
											// an event for each individual member role change
											eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_USER_GROUP_MEMBERSHIP_UPDATE, "uid=" + gMemberId + ";groupId=" + g.getId() + ";oldRole=" + groupRole + ";newRole=" + siteRole + ";active=" + siteMember.isActive() + ";provided=false", true/*update event*/));
										}
									}
								}
							}
							// post event about the participant update
							eventTrackingService.post(eventTrackingService.newEvent(siteService.SECURE_UPDATE_GROUP_MEMBERSHIP, g.getId(),true));
						}
						catch (Exception ee)
						{
							log.error(this + ".doUpdate_related_group_participants: " + ee.getMessage() + g.getId(), ee);
						}
					}
					
				}
				// commit, save the site
				siteService.save(s);
			}
			catch (Exception e)
			{
				log.error(this + ".doUpdate_related_group_participants: " + e.getMessage() + s.getId(), e);
			}
		}
	}

	/**
	 * doUpdate_site_access
	 * 
	 */
	public void doUpdate_site_access(RunData data) {
		if (!"POST".equals(data.getRequest().getMethod())) {
			log.warn("Ignoring non-POST request to update site access.");
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
		readInputAndUpdateStateVariable(state, params, "startdate_iso8601", SITE_PUBLISH_DATE, false);
		readInputAndUpdateStateVariable(state, params, "enddate_iso8601", SITE_UNPUBLISH_DATE, false);
		readInputAndUpdateStateVariable(state, params, "publishType", SITE_PUBLISH_TYPE, false);
        String publishType = String.valueOf(state.getAttribute(SITE_PUBLISH_TYPE));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(userTimeService.getLocalTimeZone());
		// SAK-24423 - get all joinable site settings from the form input
		JoinableSiteSettings.getAllFormInputs( state, params );

		boolean publishUnpublish = state.getAttribute(STATE_SITE_ACCESS_PUBLISH) != null && (Boolean) state.getAttribute(STATE_SITE_ACCESS_PUBLISH);

		// the site publish status before update
		boolean currentSitePublished = sEdit != null && sEdit.isPublished();

		boolean include = state.getAttribute(STATE_SITE_ACCESS_INCLUDE) != null && (Boolean) state.getAttribute(STATE_SITE_ACCESS_INCLUDE);
		int daysbefore = serverConfigurationService.getInt("course_site_publish_service.num_days_before_term_starts", 0);
		int daysafter = serverConfigurationService.getInt("course_site_removal_service.num_days_after_term_ends", 14);
		if (sEdit != null) {
			// editing existing site
			// publish site or not
			sEdit.getPropertiesEdit().addProperty(SITE_PUBLISH_TYPE, (String) state.getAttribute(SITE_PUBLISH_TYPE));
			if(StringUtils.equals(publishType, SITE_PUBLISH_TYPE_MANUAL)) {	//for manual publishing: just use value from the form, no other info needed.
				sEdit.setPublished(publishUnpublish);
				sEdit.getPropertiesEdit().removeProperty(SITE_PUBLISH_DATE);
				sEdit.getPropertiesEdit().removeProperty(SITE_UNPUBLISH_DATE);
			} else if(StringUtils.equals(publishType, SITE_PUBLISH_TYPE_SCHEDULED)) {	//for Scheduled date-based publishing, do it either now or scheduled based on the dates
				sEdit.setPublished(false);	//don't publish for now; we will publish later if necessary
				if(state.getAttribute(SITE_PUBLISH_DATE) == null){
					addAlert(state, rb.getString("ediacc.errorblank"));
				}
				sEdit.getPropertiesEdit().addProperty(SITE_PUBLISH_DATE, (String) state.getAttribute(SITE_PUBLISH_DATE));	//use toString, not nullable, to trigger an error
				if(state.getAttribute(SITE_UNPUBLISH_DATE) != null){
					sEdit.getPropertiesEdit().addProperty(SITE_UNPUBLISH_DATE, String.valueOf(state.getAttribute(SITE_UNPUBLISH_DATE)));	//use valueOf, nullable, because unpublish may be blank
				} else {
					sEdit.getPropertiesEdit().addProperty(SITE_UNPUBLISH_DATE, null);
				}
				try {
					Date publishingDate = null;
					Date unpublishingDate = null;
					if(state.getAttribute(SITE_PUBLISH_DATE) != null){
						publishingDate = sdf.parse(String.valueOf(state.getAttribute(SITE_PUBLISH_DATE)));
					} else {
						addAlert(state, rb.getString("ediacc.errorblank"));
					}
					if(state.getAttribute(SITE_UNPUBLISH_DATE) != null){
						unpublishingDate = sdf.parse(String.valueOf(state.getAttribute(SITE_UNPUBLISH_DATE)));
						if(publishingDate != null && unpublishingDate.toInstant().isBefore(publishingDate.toInstant())){	//make sure unpublish date is actually after publish date
							addAlert(state, rb.getString("ediacc.errorafter"));
						}
					}
					if(publishingDate != null && Instant.now().isAfter(publishingDate.toInstant()) && (unpublishingDate == null || Instant.now().isBefore(unpublishingDate.toInstant()))){
						sEdit.setPublished(true);	//publish right now if we're between the dates, or without unpublishing
					} else if(publishingDate != null) {
						publishingSiteScheduleService.schedulePublishing(publishingDate.toInstant(), sEdit.getId());	//make future publishing event
					}
					if(unpublishingDate!=null) {
						if (Instant.now().isAfter(unpublishingDate.toInstant())) {
							sEdit.setPublished(false);    //unpublish now if it's after the closing date
						} else {
							unpublishingSiteScheduleService.scheduleUnpublishing(unpublishingDate.toInstant(), sEdit.getId());    //make future unpublishing event.
						}
					}
				} catch (java.text.ParseException p){
					addAlert(state, rb.getString("ediacc.errorparse"));
				}
			} else {	//auto publishing, "1"
				try{
					Date termstart = new Date(courseManagementService.getAcademicSession(sEdit.getProperties().getProperty(Site.PROP_SITE_TERM_EID)).getStartDate().getTime() - (ONE_DAY_IN_MS * daysbefore));
					Date termend = new Date(courseManagementService.getAcademicSession(sEdit.getProperties().getProperty(Site.PROP_SITE_TERM_EID)).getEndDate().getTime() + (ONE_DAY_IN_MS * daysafter));
					if(Instant.now().isAfter(termstart.toInstant()) && Instant.now().isBefore(termend.toInstant())) {
						sEdit.setPublished(true);	//go ahead and publish when it's within the term's window
					} else {
						sEdit.setPublished(false);
					}
				} catch(IdNotFoundException i) {
					addAlert(state, rb.getString("ediacc.errorparse"));
				}
			}
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
					eventTrackingService.post(eventTrackingService.newEvent(siteService.EVENT_SITE_UNPUBLISH, sEdit.getReference(), true));
				}
				else if (!currentSitePublished && publishUnpublish)
				{
					// publishing a published site
					eventTrackingService.post(eventTrackingService.newEvent(siteService.EVENT_SITE_PUBLISH, sEdit.getReference(), true));
				}
				state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);

				scheduleTopRefresh();

				state.removeAttribute(STATE_JOINABLE);
				state.removeAttribute(STATE_JOINERROLE);

				// SAK-24423 - remove joinable site settings from the state
				JoinableSiteSettings.removeJoinableSiteSettingsFromState( state );
			}
		} else {
			// adding new site
			boolean erroradded = false;
			if (state.getAttribute(STATE_SITE_INFO) != null) {
				SiteInfo siteInfo = (SiteInfo) state
						.getAttribute(STATE_SITE_INFO);
                siteInfo.properties.addProperty(SITE_PUBLISH_TYPE, (String) state.getAttribute(SITE_PUBLISH_TYPE));
                if(StringUtils.equals(publishType, SITE_PUBLISH_TYPE_MANUAL)) {	//for manual publishing: just use value from the form, no other info needed.
                    siteInfo.published = publishUnpublish;
					siteInfo.properties.removeProperty(SITE_PUBLISH_DATE);
					siteInfo.properties.removeProperty(SITE_UNPUBLISH_DATE);
                } else if(StringUtils.equals(publishType, SITE_PUBLISH_TYPE_SCHEDULED)) {	//for Scheduled date-based publishing, do it either now or scheduled based on the dates
                    siteInfo.published = false;	//don't publish for now; we will publish later if necessary
                    Date publishDate = null;
					if(state.getAttribute(SITE_PUBLISH_DATE)==null || StringUtils.isBlank(state.getAttribute(SITE_PUBLISH_DATE).toString())){
                        addAlert(state, rb.getString("ediacc.errorblank"));
						erroradded = true;
                    } else {	//if it's not blank, we need to also make sure it parses.
						try{
							publishDate = sdf.parse(state.getAttribute(SITE_PUBLISH_DATE).toString());
						} catch (java.text.ParseException p){
							addAlert(state, rb.getString("ediacc.errorparse"));
							erroradded = true;
						}
					}
					siteInfo.properties.addProperty(SITE_PUBLISH_DATE, (String) state.getAttribute(SITE_PUBLISH_DATE));
                    if(state.getAttribute(SITE_UNPUBLISH_DATE) != null && !StringUtils.isBlank(state.getAttribute(SITE_UNPUBLISH_DATE).toString())){
						Date unpublishDate = null;
						try{
							unpublishDate = sdf.parse(state.getAttribute(SITE_UNPUBLISH_DATE).toString());
							if(unpublishDate.toInstant().isBefore(publishDate.toInstant())){	//make sure unpublish date is actually after publish date
								addAlert(state, rb.getString("ediacc.errorafter"));
								erroradded = true;
							}
						} catch (java.text.ParseException p){
							addAlert(state, rb.getString("ediacc.errorparse"));
							erroradded = true;
						}
						siteInfo.properties.addProperty(SITE_UNPUBLISH_DATE, String.valueOf(state.getAttribute(SITE_UNPUBLISH_DATE)));	//use valueOf, nullable, because unpublish may be blank
                    } else {
						siteInfo.properties.addProperty(SITE_UNPUBLISH_DATE, null);
                    }
                }
				// site public choice
				siteInfo.include = include;

				// joinable site or not
				boolean joinable = state.getAttribute(STATE_JOINABLE) != null ? ((Boolean) state.getAttribute(STATE_JOINABLE)).booleanValue() : null;

				// SAK-24423 - update site info for joinable site settings
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
				} else if(state.getAttribute(STATE_TEMPLATE_SITE) != null){	//go ahead and Finish if this is duplicating from a template
					if(erroradded){	//on errors, reload the same page
						state.setAttribute(STATE_TEMPLATE_INDEX, "18");
					}
					doFinish(data);
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
					log.warn("Attempting to add a role ("+ roleId+ ") that already exists in site: "+ site.getId());
				}
			} else {
				if (site.getRole(roleId) != null) {
					site.removeRole(roleId);
				} else {
					log.warn("Attempting to remove a role ("+ roleId+ ") that isn't defined in site: "+ site.getId());
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
				(!joinable && serverConfigurationService.getBoolean(CONVERT_NULL_JOINABLE_TO_UNJOINABLE, true))) {
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

		log.debug("actionForTemplate index={} direction={}", index, direction);

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
			break;
		case STATE_TEMPLATE_INDEX_MANAGE_PARTICIPANTS_INT:
			/*
			 * actionForTemplate chef_siteInfo-manageParticipants.vm
			 *
			 */
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
					boolean siteVisibilityDefault = serverConfigurationService.getBoolean(SAK_PROP_DEFAULT_SITE_VIS, SAK_PROP_DEFAULT_SITE_VIS_DFLT);
					siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
					siteInfo.include = siteVisibilityDefault;

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
						// list of tools that were selected for import
						Map<String, List<String>> importTools = (Map<String, List<String>>) state.getAttribute(STATE_IMPORT_SITE_TOOL);
						Map<String, Map<String, List<String>>> toolItemMap = (Map<String, Map<String, List<String>>>) state.getAttribute(STATE_IMPORT_SITE_TOOL_ITEMS);
						Map<String, Map<String, List<String>>> toolOptions = (Map<String, Map<String, List<String>>>) state.getAttribute(STATE_IMPORT_SITE_TOOL_OPTIONS);

						//list of existing tools in the destination site
						List<String> existingTools = getOriginalToolIds((List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST), state);

						boolean importTaskStarted = siteManageService.importToolsIntoSiteThread(existingSite, existingTools, importTools, toolItemMap, toolOptions, false);
						if (importTaskStarted) {
							// ***** import tools here
							state.setAttribute(IMPORT_QUEUED, rb.get("importQueued"));
							state.removeAttribute(STATE_IMPORT_SITE_TOOL);
							state.removeAttribute(STATE_IMPORT_SITE_TOOL_ITEMS);
							state.removeAttribute(STATE_IMPORT_SITES);
						} else {
							//an existing thread is running for this site import, throw warning
							addAlert(state, rb.getString("java.import.existing"));
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
						// list of tools that were selected for import
						Map<String, List<String>> importTools = (Map<String, List<String>>) state.getAttribute(STATE_IMPORT_SITE_TOOL);
						Map<String, Map<String, List<String>>> toolOptions = (Map<String, Map<String, List<String>>>) state.getAttribute(STATE_IMPORT_SITE_TOOL_OPTIONS);
						Map<String, Map<String, List<String>>> toolItemMap = (Map<String, Map<String, List<String>>>) state.getAttribute(STATE_IMPORT_SITE_TOOL_ITEMS);

						//list of existing tools in the destination site
						List<String> existingTools = getOriginalToolIds((List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST), state);

						boolean importTaskStarted = siteManageService.importToolsIntoSiteThread(existingSite, existingTools, importTools, toolItemMap, toolOptions, true);

						if (importTaskStarted) {
							// ***** import tools here
							state.setAttribute(IMPORT_QUEUED, rb.get("importQueued"));
							state.removeAttribute(STATE_IMPORT_SITE_TOOL);
							state.removeAttribute(STATE_IMPORT_SITE_TOOL_ITEMS);
							state.removeAttribute(STATE_IMPORT_SITES);
						} else {
							//an existing thread is running for this site import, throw warning
							addAlert(state, rb.getString("java.import.existing"));
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
							Site s = siteService.getSite((String) importSites
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
							Site s = siteService.getSite((String) importSites
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
					if ((securityService.isSuperUser())&& ((StringUtils.trimToNull(params.getString("newSiteId")) != null)&&(siteService.siteExists(params.getString("newSiteId"))))){
					    addAlert(state, rb.getString("sitdup.idused") + " ");
					}

					if (state.getAttribute(STATE_MESSAGE) == null) {
						// duplicated site title is editable; cannot but null/empty after HTML stripping, and cannot exceed max length
						String titleOrig = params.getString("title");
						String titleStripped = formattedText.stripHtmlFromText(titleOrig, true, true);
						if (isSiteTitleValid(titleOrig, titleStripped, state)) {
							state.setAttribute(SITE_DUPLICATED_NAME, titleStripped);

							String newSiteId = null;
							if (StringUtils.trimToNull(params.getString("newSiteId")) == null) {
								newSiteId = idManager.createUuid();
							} else{
								newSiteId = params.getString("newSiteId");
							}

							try {
								String oldSiteId = (String) state
										.getAttribute(STATE_SITE_INSTANCE_ID);

								// Retrieve the source site reference to be used in the EventTrackingService
								// notification of the start/end of a site duplication.
								String sourceSiteRef = null;
								try {
									Site sourceSite = siteService.getSite(oldSiteId);
									sourceSiteRef = sourceSite.getReference();

								} catch (IdUnusedException e) {
									log.warn(this + ".actionForTemplate; case29: invalid source siteId: "+oldSiteId);
									return;
								}

								// SAK-20797
								long oldSiteQuota = this.getSiteSpecificQuota(oldSiteId);

								// Create the duplicate site
								Site site = siteService.addSite(newSiteId, getStateSite(state));
								site.setTitle(titleStripped);

								// If the site contains the Email Archive tool, we're going to check for valid/unique email address;
								// this requires the duplicate site to already exist...
								if (state.getAttribute(STATE_DUP_SITE_HAS_EMAIL_ARCHIVE) != null) {
									String newEmailID = StringUtils.trimToNull(params.getString("emailAddress"));
									if (StringUtils.isBlank(newEmailID)) {
										addAlert(state, rb.getString("java.emailarchive"));
										deleteTempDupSiteOnError(site);
									} else {
										state.setAttribute(STATE_TOOL_EMAIL_ADDRESS, newEmailID);
										if (StringUtils.isNotBlank(newEmailID)) {
											String channelReference = mailArchiveChannelReference(site.getId());
											if (!Validator.checkEmailLocal(newEmailID)) {
												addAlert(state, rb.getString("java.theemail"));
												deleteTempDupSiteOnError(site);
											} else if (!aliasService.allowSetAlias(newEmailID, channelReference)) {
												addAlert(state, rb.getString("java.addalias"));
												deleteTempDupSiteOnError(site);
											} else {
												try {
													// First clear any alias set to the channel
													aliasService.removeTargetAliases(channelReference);

													// Check to see whether the alias has been used
													String target = aliasService.getTarget(newEmailID);
													boolean targetsThisSite = site.getReference().equals(target);
													if (!targetsThisSite) {
														addAlert(state, rb.getFormattedMessage("java.emailinuse", new Object[]{newEmailID, serverConfigurationService.getServerName()}));
														deleteTempDupSiteOnError(site);
													}
												} catch (IdUnusedException ex) {
													// If aliasService.getTarget() throws this, it's all good: email alias not yet in use, so we set it
													try {
														aliasService.setAlias(newEmailID, channelReference);
													} catch (Exception e) {
														addAlert(state, rb.getFormattedMessage("unexpectedError", new Object[] {serverConfigurationService.getString("mail.support")}));
														deleteTempDupSiteOnError(site);
													}
												} catch (Exception ex) {
													addAlert(state, rb.getFormattedMessage("unexpectedError", new Object[] {serverConfigurationService.getString("mail.support")}));
													deleteTempDupSiteOnError(site);
												}
											}
										}
									}
								}

								if (state.getAttribute(STATE_MESSAGE) == null) {
									boolean removeStealthToolsFromDup = serverConfigurationService.getBoolean(SAK_PROP_RM_STLTH_ON_DUP, SAK_PROP_RM_STLTH_ON_DUP_DEFAULT);
									if (removeStealthToolsFromDup) {
										List<SitePage> pageList = site.getPages();
										if (CollectionUtils.isNotEmpty(pageList)) {
											List<SitePage> rmPageList = new ArrayList<>();

											// Check if each tool is stealthed; if so, queue for removal
											for (SitePage page : pageList) {
												List<ToolConfiguration> pageToolList = page.getTools();
												if (CollectionUtils.isNotEmpty(pageToolList)) {
													List<ToolConfiguration> rmToolList = new ArrayList<>();

													for (ToolConfiguration toolConf : pageToolList) {
														Tool tool = toolConf.getTool();
														String toolId = StringUtils.trimToEmpty(tool.getId());

														if (StringUtils.isNotBlank(toolId) && toolManager.isStealthed(toolId)) {
															// Found a stealthed tool, queue for removal
															log.debug("found stealthed tool {}", toolId);
															rmToolList.add(toolConf);
														}
													}

													// Remove stealthed tools from page
													if (!rmToolList.isEmpty()) {
														for (ToolConfiguration rmToolConf : rmToolList) {
															page.removeTool(rmToolConf);
														}

														if (page.getTools().isEmpty()) {
															// Queue page for removal if no tools remain
															log.debug("queueing page for removal: {}", page.getId());
															rmPageList.add(page);
														}
													}
												}
											}

											// Remove now-empty pages from site
											if (!rmPageList.isEmpty()) {
												for (SitePage rmPage : rmPageList) {
													log.debug("removing {} from site", rmPage.getId());
													site.removePage(rmPage);
												}
											}
										}
									}

									// An event for starting the "duplicate site" action
									eventTrackingService.post(eventTrackingService.newEvent(siteService.EVENT_SITE_DUPLICATE_START, sourceSiteRef, site.getId(), false, NotificationService.NOTI_OPTIONAL));

									// get the new site icon url
									if (site.getIconUrl() != null)
									{
										site.setIconUrl(siteManageService.transferSiteResource(oldSiteId, newSiteId, site.getIconUrl()));
									}

									// SAK-20797 alter quota if required
									boolean	duplicateQuota = params.getString("dupequota") != null ? params.getBoolean("dupequota") : false;
									if (duplicateQuota==true) {

										if (oldSiteQuota > 0) {
											log.info("Saving quota");
											try {
												String collId = contentHostingService
														.getSiteCollection(site.getId());

												ContentCollectionEdit col = contentHostingService.editCollection(collId);

												ResourcePropertiesEdit resourceProperties = col.getPropertiesEdit();
												resourceProperties.addProperty(
														ResourceProperties.PROP_COLLECTION_BODY_QUOTA,
														new Long(oldSiteQuota)
																.toString());
												contentHostingService.commitCollection(col);


											} catch (Exception ignore) {
												log.warn("saveQuota: unable to duplicate site-specific quota for site : "
														+ site.getId() + " : " + ignore);
											}
										}
									}

									try {
										siteService.save(site);

										// Remove tools and pages that may not be duplicated
										removeToolsNotForDuplication(site);

										// import tool content
										siteManageService.importToolContent(oldSiteId, site, false);

										String transferScoringData = params.getString("selectScoringData");
										if(transferScoringData != null && transferScoringData.equals("transferScoringData")) {
											ScoringService scoringService = (ScoringService)  ComponentManager.get("org.sakaiproject.scoringservice.api.ScoringService");
											ScoringAgent agent = scoringService.getDefaultScoringAgent();
											if (agent != null && agent.isEnabled(oldSiteId, null)) {
												agent.transferScoringComponentAssociations(oldSiteId, site.getId());
											}
										}

										String siteType = site.getType();
										if (siteTypeUtil.isCourseSite(siteType)) {
											// for course site, need to
											// read in the input for
											// term information
											String termId = StringUtils.trimToNull(params
													.getString("selectTerm"));
											if (termId != null) {
												AcademicSession term = courseManagementService.getAcademicSession(termId);
												if (term != null) {
													ResourcePropertiesEdit rp = site.getPropertiesEdit();
													rp.addProperty(Site.PROP_SITE_TERM, term.getTitle());
													rp.addProperty(Site.PROP_SITE_TERM_EID, term.getEid());

													// Need to set STATE_TERM_SELECTED so it shows in the notification email
													state.setAttribute(STATE_TERM_SELECTED, term);
												} else {
													log.warn("termId=" + termId + " not found");
												}
											}
										}

										// save again
										siteService.save(site);
										state.setAttribute(STATE_DUPE_SITE_STATUS_ID, site.getId());
										state.setAttribute(STATE_DUPE_SITE_URL, site.getUrl());
										String realm = siteService.siteReference(site.getId());
										try
										{
											AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realm);
											// also remove the provider id attribute if any
											realmEdit.setProviderGroupId(null);
											// add current user as the maintainer
											realmEdit.addMember(userDirectoryService.getCurrentUser().getId(), site.getMaintainRole(), true, false);

											authzGroupService.save(realmEdit);
										} catch (GroupNotDefinedException e) {
											log.error(this + ".actionForTemplate chef_siteinfo-duplicate: IdUnusedException, not found, or not an AuthzGroup object "+ realm, e);
											addAlert(state, rb.getString("java.realm"));
										} catch (AuthzPermissionException e) {
											addAlert(state, this + rb.getString("java.notaccess"));
											log.error(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.notaccess"), e);
										}
									} catch (IdUnusedException e) {
										log.warn(this + " actionForTemplate chef_siteinfo-duplicate:: IdUnusedException when saving " + newSiteId);
									} catch (PermissionException e) {
										log.warn(this + " actionForTemplate chef_siteinfo-duplicate:: PermissionException when saving " + newSiteId);
									}

									scheduleTopRefresh();

									// send site notification
									sendSiteNotification(state, site, null);

									state.setAttribute(SITE_DUPLICATED, Boolean.TRUE);

									// An event for ending the "duplicate site" action
									eventTrackingService.post(eventTrackingService.newEvent(siteService.EVENT_SITE_DUPLICATE_END, sourceSiteRef, site.getId(), false, NotificationService.NOTI_OPTIONAL));
								}
							} catch (IdInvalidException e) {
								addAlert(state, rb.getString("java.siteinval"));
								log.error(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.siteinval") + " site id = " + newSiteId, e);
							} catch (IdUsedException e) {
								addAlert(state, rb.getString("java.sitebeenused"));
								log.error(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.sitebeenused") + " site id = " + newSiteId, e);
							} catch (PermissionException e) {
								addAlert(state, rb.getString("java.allowcreate"));
								log.error(this + ".actionForTemplate chef_siteinfo-duplicate: " + rb.getString("java.allowcreate") + " site id = " + newSiteId, e);
							}
						}
					}
				}

				if (state.getAttribute(STATE_MESSAGE) == null) {
					// return to the list view
					state.setAttribute(STATE_TEMPLATE_INDEX, SITE_INFO_TEMPLATE_INDEX);
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
						state.setAttribute(STATE_TEMPLATE_INDEX, "18");
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
					trackRosterChanges(SiteService.EVENT_SITE_ROSTER_REMOVE,providerCourseDeleteList);
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
							SectionObject so = new SectionObject(courseManagementService.getSection(sectionId));
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
							log.warn("actionForTemplate 43 editClass: Cannot find section " + sectionId);
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
					siteService.save(site);
				}
				catch (Exception e)
				{
					log.error(this + ".actionForTemplate chef_siteinfo-addCourseConfirm: " +  e.getMessage() + site.getId(), e);
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
	 * Responsible for hard deleting a temporary duplicate site. This method
	 * is called during the duplication routine if an error is produced after
	 * the duplicate site has been created (some of the functions require the
	 * site to exist to perform some of the duplication processes, which can
	 * sometimes result in errors that the user needs to rectify).
	 *
	 * @param tempDupSite the temporary duplicate site to be hard deleted in entirety
	 */
	private void deleteTempDupSiteOnError(Site tempDupSite) {
		// We need to remove the duplicated site because errors were thrown
		try {
			SecurityAdvisor yesMan = new SecurityAdvisor() {
				public SecurityAdvice isAllowed(String userId, String function, String reference) {
					return SecurityAdvice.ALLOWED;
				}
			};
			securityService.pushAdvisor(yesMan);

			// Now hard delete the site
			siteService.removeSite(tempDupSite, true);
		} catch (Exception e) {
		} finally {
			securityService.popAdvisor();
		}
	}
	
	/**
	 * 
	 */
	private void trackRosterChanges(String event, List <String> rosters) {
		if (serverConfigurationService.getBoolean(
				SiteHelper.WSETUP_TRACK_ROSTER_CHANGE, false)) {
			// event for each individual update
			if (rosters != null) {
				for (String roster : rosters) {
					eventTrackingService.post(eventTrackingService.newEvent(event, "roster="+roster, true));
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
			AuthzGroup realm = authzGroupService.getAuthzGroup(siteService.siteReference(site.getId()));
			
			List importSites = new ArrayList(Arrays.asList(params.getStrings("importSites")));
			for (int i = 0; i < importSites.size(); i++) {
				String fromSiteId = (String) importSites.get(i);
				try {
					Site fromSite = siteService.getSite(fromSiteId);
					
					// get realm information
					String fromRealmId = siteService.siteReference(fromSite.getId());
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
					log.error(this + ".importSitesUsers: GroupNotDefinedException, " + fromSiteId + " not found, or not an AuthzGroup object", e);
					addAlert(state, rb.getString("java.cannotedit"));
				} catch (IdUnusedException e) {
					log.error(this + ".importSitesUsers: IdUnusedException, " + fromSiteId + " not found, or not an AuthzGroup object", e);
				
				}
			}
			
			// post event about the realm participant update
			eventTrackingService.post(eventTrackingService.newEvent(siteService.SECURE_UPDATE_SITE_MEMBERSHIP, realm.getId(), false));
			
			// save realm
			authzGroupService.save(realm);
			
		} catch (GroupNotDefinedException e) {
			log.error(this + ".importSitesUsers: IdUnusedException, " + site.getTitle() + "(" + site.getId() + ") not found, or not an AuthzGroup object", e);
			addAlert(state, rb.getString("java.cannotedit"));
		} catch (AuthzPermissionException e) {
			log.error(this + ".importSitesUsers: PermissionException, user does not have permission to edit AuthzGroup object " + site.getTitle() + "(" + site.getId() + "). ", e);
			addAlert(state, rb.getString("java.notaccess"));
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
						uAnswer.setUserId(sessionManager.getCurrentSessionUserId());
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
		state.removeAttribute(STATE_IMPORT_SITE_TOOL_ITEMS);
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
		String realmId = siteService.siteReference(id);

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

				String currentUserId = sessionManager.getCurrentSessionUserId();
				boolean allowDelLastRoster = serverConfigurationService.getBoolean(SAK_PROP_ALLOW_DEL_LAST_ROSTER, SAK_PROP_ALLOW_DEL_LAST_ROSTER_DFLT);
				if (allowDelLastRoster && !hasNonProvidedMainroleUser && realmEdit1.hasRole(currentUserId, maintainRoleString)) {
					realmEdit1.addMember(currentUserId, maintainRoleString, true, false);
					hasNonProvidedMainroleUser = true;
				}

				if (!hasNonProvidedMainroleUser)
				{
					// if after the removal, there is no provider id, and there is no maintain role user anymore, show alert message and don't save the update
					addAlert(state, rb.getString("sitegen.siteinfolist.nomaintainuser")
							+ " " + maintainRoleString + ".");
				}
				else
				{
					realmEdit1.setProviderGroupId(NULL_STRING);
					authzGroupService.save(realmEdit1);
				}
			} catch (GroupNotDefinedException e) {
				log.error(this + ".updateCourseClasses: IdUnusedException, " + site.getTitle()
						+ "(" + realmId
						+ ") not found, or not an AuthzGroup object", e);
				addAlert(state, rb.getString("java.cannotedit"));
				return;
			} catch (AuthzPermissionException e) {
				log.error(this + ".updateCourseClasses: PermissionException, user does not have permission to edit AuthzGroup object "
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
				log.error(this + ".updateCourseClasses: IdUnusedException, " + site.getTitle()
						+ "(" + realmId
						+ ") not found, or not an AuthzGroup object", e);
				addAlert(state, rb.getString("java.cannotclasses"));
				return;
			} catch (AuthzPermissionException e) {
				log.error(this
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
				log.error(this + ".updateCourseClasses: cannot remove authzgroup : " + group.getReference(), e);
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
				log.error(this +".updateCourseClasses:" + e.toString(), e);
			}
		}
		if (notifyClasses != null && notifyClasses.size() > 0) {
			try {
				// send out class access confirmation notifications
				sendSiteNotification(state, getStateSite(state), notifyClasses);
			} catch (Exception e) {
				log.error(this + ".updateCourseClasses:" + e.toString(), e);
			}
		}
	} // updateCourseClasses

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
			log.warn("SiteAction.updateSiteAttributes STATE_SITE_INFO == null");
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

			// SAK-24423 - update site properties for joinable site settings
			JoinableSiteSettings.updateSitePropertiesFromStateOnUpdateSiteAttributes( site, state );

			try {
				siteService.save(site);
			} catch (IdUnusedException e) {
				// TODO:
			} catch (PermissionException e) {
				// TODO:
			}

			if (siteService.allowUpdateSite(id)) {
				try {
					siteService.getSite(id);
					state.setAttribute(STATE_SITE_INSTANCE_ID, id);
				} catch (IdUnusedException e) {
					log.error(this + ".updateSiteAttributes: IdUnusedException "
							+ siteInfo.getTitle() + "(" + id + ") not found", e);
				}
			}

			// no permission
			else {
				addAlert(state, rb.getString("java.makechanges"));
				log.warn(this + ".updateSiteAttributes: PermissionException "
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
			// site title is editable; cannot but null/empty after HTML stripping, and cannot exceed max length
			String titleOrig = params.getString("title");
			String titleStripped = formattedText.stripHtmlFromText(titleOrig, true, true);
			if (isSiteTitleValid(titleOrig, titleStripped, state)) {
				siteInfo.title = titleStripped;
			}
		}
				
		if (params.getString("description") != null) {
			StringBuilder alertMsg = new StringBuilder();
			String description = params.getString("description");
			siteInfo.description = formattedText.processFormattedText(description, alertMsg);
		}
		if (params.getString("short_description") != null) {
			siteInfo.short_description = params.getString("short_description");
		}
		if (params.getString("additional") != null) {
			siteInfo.additional = params.getString("additional");
		}
		String icon = params.getString("iconUrl");
		if (icon != null) {
			if (!(icon.isEmpty() || formattedText.validateURL(icon))) {
				addAlert(state, rb.getString("alert.protocol"));
			}
			siteInfo.iconUrl = icon;
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

		// SAK-24423 - update site info for joinable site settings
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
				addAlert(state, rb.getFormattedMessage("java.invalid.email", new Object[]{formattedText.escapeHtml(email,false)}));
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
		boolean isSimpleUrl = aliasId.equals(formattedText.escapeUrl(aliasId));
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
					String siteRef = siteService.siteReference(currentSiteId);
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
	private Collection<Participant> getParticipantList(SessionState state) {
		List members = new Vector();
		String siteId = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);

		List<String> providerCourseList = null;
		providerCourseList = SiteParticipantHelper.getProviderCourseList(siteId);
		if (providerCourseList != null && providerCourseList.size() > 0) {
			state.setAttribute(SITE_PROVIDER_COURSE_LIST, providerCourseList);
		}

		// Apply filter if necessary
		String selectedFilter = (String) state.getAttribute(STATE_SITE_PARTICIPANT_FILTER);
		Collection<Participant> participants = ParticipantFilterHandler.prepareParticipantsWithFilter(siteId, providerCourseList, selectedFilter);

		//check for search user attribute in the state
		String search = (String)state.getAttribute(SITE_USER_SEARCH);
		if(StringUtils.isNotBlank(search) && (participants.size() > 0)) {
			for(Object object : participants){
				Participant participant = (Participant)object;
				//if search term is in the display name or in display Id, add into the list
				if (StringUtils.containsIgnoreCase(StringUtils.stripAccents(participant.getDisplayName()), StringUtils.stripAccents(search)) || StringUtils.containsIgnoreCase(participant.getDisplayId(),search)) {
					members.add(participant);
				}
			}
			state.setAttribute(STATE_PARTICIPANT_LIST, members);
			//STATE_PARTICIPANT_LIST will contain members which satisfy search and filter criteria therefore saving original participants list in new attribute
			state.setAttribute(STATE_SITE_PARTICIPANT_LIST, participants);
			return members;
		}
		state.setAttribute(STATE_PARTICIPANT_LIST, participants);

		return participants;

	} // getParticipantList

	/**
	 * getRoles
	 * 
	 */
	private List getRoles(SessionState state) {
		List roles = new Vector();
		String realmId = siteService.siteReference((String) state
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
			log.error( this + ".getRoles: IdUnusedException " + realmId, e);
		}
		return roles;

	} // getRoles
	
	/**
	 * getRoles
	 * 
	 * SAK-23257 - added state and siteType parameters so list 
	 * of joiner roles can respect the restricted role lists in sakai.properties.
	 * 
	 */
	private List<Role> getJoinerRoles(String realmId, SessionState state, String siteType) {
		List roles = new ArrayList();
		/** related to SAK-18462, this is a list of permissions that the joinable roles shouldn't have ***/
		String[] prohibitPermissionForJoinerRole = serverConfigurationService.getStrings("siteinfo.prohibited_permission_for_joiner_role");
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
				
				// SAK-23257
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
						// SAK-23257
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
				log.error( this + ".getRoles: IdUnusedException " + realmId, e);
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
		Tool reg = toolManager.getTool(toolId);
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

		boolean customOverview = StringUtils.equalsIgnoreCase(site.getProperties().getProperty(Site.PROP_CUSTOM_OVERVIEW), "true");
		
		List pageList = new Vector();
		// declare some flags used in making decisions about Home, whether to
		// add, remove, or do nothing
		boolean hasHome = false;
		String homePageId = null;
		boolean homeInWSetupPageList = false;

		List<String> chosenList = (List<String>) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);

		boolean hasEmail = false;
		boolean hasSiteInfo = false;
		
		// tools to be imported from other sites?
		Map<String, List<String>> importTools = null;
		if (state.getAttribute(STATE_IMPORT_SITE_TOOL) != null) {
			importTools = (Map<String, List<String>>) state.getAttribute(STATE_IMPORT_SITE_TOOL);
		}
		
		// Home tool chosen?
		if (chosenList.contains(TOOL_ID_HOME)) {
			// add home tool later
			hasHome = true;
		}
		
		// order the id list
		chosenList = orderToolIds(state, siteType, chosenList, false);

		List<String> deletedGroups = new ArrayList<>();
		List<String> newGroups = new ArrayList<>();
		boolean isGroupType = false;
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
								addAlert(state, rb.getFormattedMessage("java.emailinuse", new Object[] {alias, serverConfigurationService.getServerName()}));
							}
						} catch (IdUnusedException ee) {
							try {
								aliasService.setAlias(alias,
										channelReference);
							} catch (IdUsedException exception) {
								log.error(this + ".saveFeatures setAlias IdUsedException:"+exception.getMessage()+" alias="+ alias + " channelReference="+channelReference, exception);
							} catch (IdInvalidException exception) {
								log.error(this + ".saveFeatures setAlias IdInvalidException:"+exception.getMessage()+" alias="+ alias + " channelReference="+channelReference, exception);
							} catch (PermissionException exception) {
								log.error(this + ".saveFeatures setAlias PermissionException:"+exception.getMessage()+" alias="+ alias + " channelReference="+channelReference, exception);
							}
						}
					}
				}
			} else if (SiteManageConstants.GRADEBOOK_TOOL_ID.equals(choice)) {
				isGroupType = state.getAttribute(GradebookGroupEnabler.FORM_INPUT_ID) != null && GradebookGroupEnabler.VALUE_GRADEBOOK_GROUPS.equals(state.getAttribute(GradebookGroupEnabler.FORM_INPUT_ID));
				List<String> selectedGroups = isGroupType ? (List<String>)state.getAttribute(GradebookGroupEnabler.SELECTED_GROUPS) : new ArrayList<>();

				List<String> existing = new ArrayList<>();

				Collection<ToolConfiguration> gbs = site.getTools(SiteManageConstants.GRADEBOOK_TOOL_ID);
				if (serverConfigurationService.getBoolean("gradebookng.multipleGroupInstances", false)) {
					for (ToolConfiguration tc : gbs) {
						Properties props = tc.getPlacementConfig();
						if ((isGroupType && props.getProperty(GB_GROUP_PROPERTY) == null) || !selectedGroups.contains(props.getProperty(GB_GROUP_PROPERTY))) {
							site.removePage(tc.getContainingPage());
							deletedGroups.add(tc.getPageId()+SiteManageConstants.GRADEBOOK_TOOL_ID);
						} else {
							existing.add(props.getProperty(GB_GROUP_PROPERTY));
						}
					}
					for (String g : selectedGroups) {
						if (!existing.contains(g))
							newGroups.add(g);
					}
				}

				if (memoryService != null) {
					Cache gradebookGroupEnabledCache = memoryService.getCache("org.sakaiproject.tool.gradebook.group.enabled");
					Cache gradebookGroupInstancesCache = memoryService.getCache("org.sakaiproject.tool.gradebook.group.instances");
					if (gradebookGroupEnabledCache != null) gradebookGroupEnabledCache.clear();
					if (gradebookGroupInstancesCache != null) gradebookGroupInstancesCache.clear();
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
			//only do all the work for  adding synoptics if there is not a custom overview
			if(!customOverview) {
				for (String homeToolId: homeToolIds)
				{
					if (!SYNOPTIC_TOOL_ID_MAP.containsKey(homeToolId)) {
						if (!pageHasToolId(toolList, homeToolId)) {
							// not a synoptic tool and is not in Home page yet, just add it
							Tool reg = toolManager.getTool(homeToolId);
							if (reg != null) {
								ToolConfiguration tool = page.addTool();
								tool.setTool(homeToolId, reg);
								tool.setTitle(reg.getTitle() != null ? reg.getTitle() : "");
								tool.setLayoutHints("0," + nonSynopticToolIndex++);
							}
						}
					} else {
						// synoptic tool
						List<String> parentToolList = (List<String>) SYNOPTIC_TOOL_ID_MAP.get(homeToolId);
						List chosenListClone = new Vector();
						// chosenlist may have things like bcf89cd4-fa3a-4dda-80bd-ed0b89981ce7sakai.chat
						// get list of the actual tool names
						List<String> chosenOrigToolList = new ArrayList<String>();
						for (String chosenTool : (List<String>) chosenList)
							chosenOrigToolList.add(findOriginalToolId(state, chosenTool));
						chosenListClone.addAll(chosenOrigToolList);
						boolean hasAnyParentToolId = chosenListClone.removeAll(parentToolList);

						//first check whether the parent tool is available in site but its parent tool is no longer selected
						if (pageHasToolId(toolList, homeToolId) && !customOverview)
						{
							if (!hasAnyParentToolId && !siteService.isUserSite(site.getId())) {
								for (ListIterator iToolList = toolList.listIterator(); iToolList.hasNext(); ) {
									ToolConfiguration tConf = (ToolConfiguration) iToolList.next();
									// avoid NPE when the tool definition is missing
									if (tConf.getTool() != null && homeToolId.equals(tConf.getTool().getId())) {
										page.removeTool((ToolConfiguration) tConf);
										break;
									}
								}
							} else {
								synopticToolIndex++;
							}
						}

						// then add those synoptic tools which wasn't there before
						if (!pageHasToolId(toolList, homeToolId) && hasAnyParentToolId) {
							try {
								// use value from map to find an internationalized tool title
								String toolTitleText = rb.getString(SYNOPTIC_TOOL_TITLE_MAP.get(homeToolId));
								addSynopticTool(page, homeToolId, toolTitleText, synopticToolIndex + ",1", synopticToolIndex++);
							} catch (Exception e) {
								log.error(this + ".saveFeatures addSynotpicTool: " + e.getMessage() + " site id = " + site.getId() + " tool = " + homeToolId, e);
							}
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
			ResourcePropertiesEdit siteProperties = site.getPropertiesEdit();
			if (siteProperties.getProperty(Site.PROP_CUSTOM_OVERVIEW) != null) {
				siteProperties.removeProperty(Site.PROP_CUSTOM_OVERVIEW);
				customOverview = false;
			}
		}

		// declare flags used in making decisions about whether to add, remove,
		// or do nothing
		boolean inChosenList;
		boolean inWSetupPageList;

		Set toolRegistrationSet = getToolRegistrations(state, siteType, false);

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

					if (pageToolId.equals(toolId) && !deletedGroups.contains(toolId)) {
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
						if (SiteManageConstants.GRADEBOOK_TOOL_ID.equals(toolId) && isGroupType) {
							for (String gId : newGroups) {
								WorksiteSetupPage addPage = new WorksiteSetupPage();
								SitePage page = site.addPage();
								addPage.pageId = page.getId();
								page.setTitle(site.getGroup(gId).getTitle() + " " + multipleToolIdTitleMap.get(toolId) + " " + rb.getString( "sitegen.siteinfolist.filter.group.postfix" ) );
								page.setTitleCustom(true);
								page.setLayout(SitePage.LAYOUT_SINGLE_COL);
								ToolConfiguration tool = page.addTool();
								tool.setTool(toolRegFound.getId(), toolRegFound);
								tool.getPlacementConfig().setProperty(GB_GROUP_PROPERTY, gId);
								addPage.toolId = toolId;
								wSetupPageList.add(addPage);
								tool.setTitle((String) multipleToolIdTitleMap.get(toolId));
							}
						} else {
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
					ltiService.deleteContent(contentKey, site.getId());
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

						// any customized properties that need to be copied from lti_tool into lti_content should go here, but generally we detect null in lti_content and fallback to lti_tool
						reqProperties.put(LTIService.LTI_TOOL_ID, ltiToolId);
						reqProperties.put(LTIService.LTI_NEWPAGE, "1");
					}
					Object retval = ltiService.insertToolContent(null, ltiToolId, reqProperties, site.getId());
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
						retval = ltiService.insertToolSiteLink(((Long) retval).toString(), pageTitle, site.getId());
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

		// Commit the site to save all tool/page changes made above
		commitSite(site);
		
		// Remove stranded LTI tools (tools deployed in site but no longer available)
		// This is done AFTER committing the site so that:
		// 1. All changes above are safely saved first
		// 2. deleteContent() will save its own changes (removing pages)
		// 3. We can then refresh to pick up those deletions without losing other changes
		List<MyTool> strandedLtiTools = (List<MyTool>) state.getAttribute(STATE_LTITOOL_STRANDED_LIST);
		if (strandedLtiTools != null && !strandedLtiTools.isEmpty()) {
			String siteId = site.getId();
			int totalStrandedTools = strandedLtiTools.size();
			int successfulDeletions = 0;
			int failedDeletions = 0;
			int totalContentItemsFound = 0;

			log.debug("saveFeatures: Starting cleanup of {} stranded LTI tools in site {}", totalStrandedTools, siteId);

			for (MyTool stranded : strandedLtiTools) {
				try {
					String originalToolIdString = stranded.id;
					String toolIdString = originalToolIdString;

					log.debug("saveFeatures: Processing stranded tool - id='{}', title='{}', description='{}'",
						originalToolIdString, stranded.title, stranded.description);

					// Strip the prefix if present
					if (toolIdString != null && toolIdString.startsWith(LTITOOL_ID_PREFIX)) {
						toolIdString = toolIdString.substring(LTITOOL_ID_PREFIX.length());
						log.debug("saveFeatures: Stripped prefix, numeric tool ID: {}", toolIdString);
					}

					Long toolId = Long.valueOf(toolIdString);

					// Find all content items for this tool in this site and delete them
					String searchClause = "lti_content.tool_id = " + toolId;
					log.debug("saveFeatures: Searching for content items with query: {}", searchClause);

					List<Map<String, Object>> contents = ltiService.getContentsDao(searchClause, null, 0, 5000, siteId, ltiService.isAdmin(siteId));
					int contentCount = contents != null ? contents.size() : 0;
					totalContentItemsFound += contentCount;

					log.debug("saveFeatures: Found {} content item(s) for stranded tool {} in site {}",
						contentCount, toolId, siteId);

					if (contents != null) {
						for (Map<String, Object> content : contents) {
							Object contentIdObj = content.get(LTIService.LTI_ID);
							if (contentIdObj != null) {
								Long contentId = Long.valueOf(contentIdObj.toString());
								String contentTitle = content.get(LTIService.LTI_TITLE) != null ?
									content.get(LTIService.LTI_TITLE).toString() : "Untitled";
								String placementId = content.get(LTIService.LTI_PLACEMENT) != null ?
									content.get(LTIService.LTI_PLACEMENT).toString() : "null";

								log.debug("saveFeatures: Attempting to delete content - id={}, title='{}', placement={}, toolId={}, siteId={}",
									contentId, contentTitle, placementId, toolId, siteId);

								boolean deleted = ltiService.deleteContent(contentId, siteId);

								if (deleted) {
									successfulDeletions++;
									log.debug("saveFeatures: Successfully deleted stranded LTI content {} ('{}') for tool {} in site {}",
										contentId, contentTitle, toolId, siteId);
								} else {
									failedDeletions++;
									log.warn("saveFeatures: FAILED to delete stranded LTI content {} ('{}') for tool {} in site {} - deleteContent returned false",
										contentId, contentTitle, toolId, siteId);
								}
							} else {
								log.warn("saveFeatures: Content item missing LTI_ID field, cannot delete. Content map keys: {}",
									content.keySet());
							}
						}
					}
				} catch (NumberFormatException e) {
					failedDeletions++;
					log.error("saveFeatures: NumberFormatException processing stranded LTI tool '{}' in site {}: {}",
						stranded.id, site.getId(), e.getMessage(), e);
				} catch (Exception e) {
					failedDeletions++;
					log.error("saveFeatures: Exception processing stranded LTI tool '{}' ('{}') in site {}: {}",
						stranded.id, stranded.title, site.getId(), e.getMessage(), e);
				}
			}

			// Clear after processing so we don't process again on subsequent saves
			state.removeAttribute(STATE_LTITOOL_STRANDED_LIST);

			// Log summary
			log.debug("saveFeatures: Stranded LTI tool cleanup complete for site {} - {} tools processed, {} content items found, {} successful deletions, {} failed deletions",
				siteId, totalStrandedTools, totalContentItemsFound, successfulDeletions, failedDeletions);

			// Refresh the site object to pick up the page deletions made by deleteContent()
			// The deleteContent() call internally saved the site, so we just need to reload our object
			site = refreshSiteObject(site);
			log.debug("saveFeatures: Site object refreshed after stranded LTI tool cleanup for site {}", siteId);
		} else {
			log.debug("saveFeatures: No stranded LTI tools found in state for site {}", site.getId());
		}

		Map<String, Map<String, List<String>>> toolOptions = (Map<String, Map<String, List<String>>>) state.getAttribute(STATE_IMPORT_SITE_TOOL_OPTIONS);
		Map<String, Map<String, List<String>>> toolItemMap = (Map<String, Map<String, List<String>>>) state.getAttribute(STATE_IMPORT_SITE_TOOL_ITEMS);

		siteManageService.importToolsIntoSite(site, chosenList, importTools, toolItemMap, toolOptions, false);

		// after importing content we need to refresh the site
		site = refreshSiteObject(site);

		boolean updateSite;
		updateSite = MathJaxEnabler.prepareMathJaxToolSettingsForSave(site, state);
		updateSite = GradebookGroupEnabler.prepareSiteForSave(site, state) || updateSite;
		updateSite = SubNavEnabler.prepareSiteForSave(site, state) || updateSite;
		if (updateSite) {
			commitSite(site);
		}
	} // saveFeatures

	/**
	 * Gets the tools that are allowed in this site, this looks at the tools available for the site type but if
	 * there aren't any available then it will fallback to the tools available in the default site.
	 * @param state The session state.
	 * @param siteType The type of the site to get the list of tools for.
	 * @return A Set of possible tools.
	 */
	Set<Tool> getToolRegistrations(SessionState state, String siteType, boolean includeStealthed) {
		if (siteType == null) {
			return Collections.emptySet();
		}
		Set<String> categories = new HashSet<>();
		// UMICH 1035
		categories.add(siteTypeUtil.getTargetSiteType(siteType));
		Set<Tool> toolRegistrationSet = toolManager.findTools(categories, null, includeStealthed);
		if ((toolRegistrationSet == null || toolRegistrationSet.size() == 0)
				&& state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null)
		{
			// use default site type and try getting tools again
			String type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);
			categories.clear();
			categories.add(siteTypeUtil.getTargetSiteType(type));
			toolRegistrationSet = toolManager.findTools(categories, null, includeStealthed);
		}
		return toolRegistrationSet;
	}

	/**
	 * refresh site object
	 * @param site
	 * @return
	 */
	private Site refreshSiteObject(Site site) {
		// refresh the site object
        try
        {
        	site = siteService.getSite(site.getId());
        }
        catch (Exception e)
        {
        	// error getting site after tool modification
        	log.warn(this + " - cannot get site " + site.getId() + " after inserting lti tools");
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
	 * Is the siteType listed in the tool properties list of categories?
	 * @param siteType
	 * @param tool
	 * @return
	 * SAK 23808
	 */
	private boolean isSiteTypeInToolCategory(String siteType, Tool tool) {
		Set<Tool> tools = toolManager.findTools(Collections.emptySet(), null);
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
	 * This is used after selecting a list of tools for a site to decide if we need to ask the user for options.
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
		boolean ltiToolNeedsConfig = false;

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
					Map<String,Object> toolMap = ltiTools.get(ltiToolId);
					if ( toolMap == null ) continue;

					// Decide if any LTI tools need a configuration dialog
					Object showDialog = toolMap.get(LTIService.LTI_SITEINFOCONFIG);
					if (existingLtiIds == null)
					{
						ltiToolNeedsConfig = true;
					}
					else
					{
						if (!existingLtiIds.keySet().contains(ltiToolId) && 
							showDialog!= null && "1".equals(showDialog.toString()))
						{
							// there are some new lti tool(s) selected that need a configuration dialog
							ltiToolNeedsConfig = true;
						}
					}

						
					// add tool entry to list
					ltiSelectedTools.put(ltiToolId, toolMap);
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
								String titleConfig = serverConfigurationService.getString(CONFIG_TOOL_TITLE + originId);
								if (titleConfig != null && titleConfig.length() > 0 )
								{
									multipleToolIdTitleMap.put(toolId, titleConfig);
								}
								else
								{
									multipleToolIdTitleMap.put(toolId, toolManager.getTool(originId).getTitle());
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

			if (!ltiSelectedTools.isEmpty() || MapUtils.isNotEmpty(existingLtiIds))
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
						Site s = siteService.getSite((String) importSites
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
				state.setAttribute(STATE_MULTIPLE_TOOL_INSTANCE_SELECTED, Boolean.valueOf(goToToolConfigPage));
				// go to the configuration page for multiple instances of tools
				state.setAttribute(STATE_TEMPLATE_INDEX, "26");
			} else {
				boolean ltiToConfigure = false;
				if (ltiToolNeedsConfig) {
					// iterate over ltiSelectedTools; if any are configurable, go to 26
					Site site = getStateSite(state);
					for (String ltiToolId : ltiSelectedTools.keySet()) {
						// don't display configuration for LTI tools if all configuration is disabled
						if ((existingLtiIds == null || !existingLtiIds.keySet().contains(ltiToolId)) && ltiService.getContentModelIfConfigurable(Long.parseLong(ltiToolId), site.getId()) != null) {
							ltiToConfigure = true;
							break;
						}
					}
				}
				if (ltiToConfigure) {
					state.setAttribute(STATE_TEMPLATE_INDEX, "26");
				} else {
					// go to next page
					state.setAttribute(STATE_TEMPLATE_INDEX, continuePageIndex);
				}
			}
			state.setAttribute(STATE_MULTIPLE_TOOL_ID_SET, multipleToolIdSet);
			state.setAttribute(STATE_MULTIPLE_TOOL_ID_TITLE_MAP, multipleToolIdTitleMap);
		}
	} // getFeatures

	public void saveSiteStatus(SessionState state, boolean published) {
		Site site = getStateSite(state);
		site.setPublished(published);

	} // saveSiteStatus

	public void commitSite(Site site, boolean published) {
		site.setPublished(published);

		try {
			siteService.save(site);
		} catch (IdUnusedException e) {
			// TODO:
		} catch (PermissionException e) {
			// TODO:
		}

	} // commitSite

	public void commitSite(Site site) {
		try {
			siteService.save(site);
		} catch (IdUnusedException e) {
			// TODO:
		} catch (PermissionException e) {
			// TODO:
		}

	}// commitSite

	private String getSetupRequestEmailAddress() {
		return serverConfigurationService.getSmtpFrom();
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
			id = idManager.createUuid();
			siteInfo.site_id = id;
		}
		state.setAttribute(STATE_SITE_INFO, siteInfo);
		if(StringUtils.equals(siteInfo.properties.getProperty(SITE_PUBLISH_TYPE), SITE_PUBLISH_TYPE_SCHEDULED)) {	//see if the site is set to publish on a custom Schedule
			siteInfo.published = false;	//don't publish for now; we will publish later if necessary
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			sdf.setTimeZone(userTimeService.getLocalTimeZone());

			try {
				Date publishingDate = sdf.parse(String.valueOf(siteInfo.properties.getProperty(SITE_PUBLISH_DATE)));
				Date unpublishingDate = null;
				if(siteInfo.properties.getProperty(SITE_UNPUBLISH_DATE)!=null && !StringUtils.isBlank(siteInfo.properties.getProperty(SITE_UNPUBLISH_DATE).toString())){
					unpublishingDate = sdf.parse(String.valueOf(siteInfo.properties.getProperty(SITE_UNPUBLISH_DATE)));
				}
				if(Instant.now().isAfter(publishingDate.toInstant()) && (unpublishingDate == null ||Instant.now().isBefore(unpublishingDate.toInstant()))){
					siteInfo.published = true;	//publish right now if we're between the dates, or without unpublishing
				} else {
					publishingSiteScheduleService.schedulePublishing(publishingDate.toInstant(), id);	//make future publishing event
				}
				if(unpublishingDate!=null) {
					if (Instant.now().isAfter(unpublishingDate.toInstant())) {
						siteInfo.published = false;    //unpublish now if it's after the closing date
					} else {
						unpublishingSiteScheduleService.scheduleUnpublishing(unpublishingDate.toInstant(), id);    //make future unpublishing event.
					}
				}
			} catch (java.text.ParseException p){
				addAlert(state, rb.getString("ediacc.errorparse"));
			}
		} else if(StringUtils.equals(siteInfo.properties.getProperty(SITE_PUBLISH_TYPE), SITE_PUBLISH_TYPE_AUTO)){
			int daysbefore = serverConfigurationService.getInt("course_site_publish_service.num_days_before_term_starts", 0);
			int daysafter = serverConfigurationService.getInt("course_site_removal_service.num_days_after_term_ends", 14);
			siteInfo.published = false;
			Date publishingDate = new Date(courseManagementService.getAcademicSession(siteInfo.term).getStartDate().getTime() - (ONE_DAY_IN_MS * daysbefore));
			Date unpublishingDate = new Date(courseManagementService.getAcademicSession(siteInfo.term).getEndDate().getTime() + (ONE_DAY_IN_MS * daysafter));
			if(Instant.now().isAfter(publishingDate.toInstant()) && Instant.now().isBefore(unpublishingDate.toInstant())){	//if we're within the auto-publishing window, go ahead and publish now
				siteInfo.published = true;
			}
		}
		if (state.getAttribute(STATE_MESSAGE) == null) {
			try {
				Site site = null;
							
				// if create based on template,
				Site templateSite = (Site) state.getAttribute(STATE_TEMPLATE_SITE);
				if (templateSite != null) {
					site = siteService.addSite(id, templateSite);
					// set site type
					site.setType(siteTypeUtil.getTargetSiteType(templateSite.getType()));
				} else {
					site = siteService.addSite(id, siteInfo.site_type);
				}
				
				// add current user as the maintainer
				site.addMember(userDirectoryService.getCurrentUser().getId(), site.getMaintainRole(), true, false);

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
				
				// SAK-24423 - update site properties for joinable site settings
				JoinableSiteSettings.updateSitePropertiesFromSiteInfoOnAddNewSite( siteInfo, rp );

				state.setAttribute(STATE_SITE_INSTANCE_ID, site.getId());

				// commit newly added site in order to enable related realm
				commitSite(site);
				
			} catch (IdUsedException e) {
				addAlert(state, rb.getFormattedMessage("java.sitewithid.exists", new Object[]{id}));
				log.error(this + ".addNewSite: " + rb.getFormattedMessage("java.sitewithid.exists", new Object[]{id}), e);
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("templateIndex"));
				return;
			} catch (IdInvalidException e) {
				addAlert(state, rb.getFormattedMessage("java.sitewithid.notvalid", new Object[]{id}));
				log.error(this + ".addNewSite: " + rb.getFormattedMessage("java.sitewithid.notvalid", new Object[]{id}), e);
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("templateIndex"));
				return;
			} catch (PermissionException e) {
				addAlert(state, rb.getFormattedMessage("java.permission", new Object[]{id}));
				log.error(this + ".addNewSite: " + rb.getFormattedMessage("java.permission", new Object[]{id}), e);
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
		List<Site> templateSites = new ArrayList<>();
	
		boolean allowedForTemplateSites = true;
		
		// system-wide setting for disable site creation based on template sites
		if (serverConfigurationService.getString("wsetup.enableSiteTemplate", "true").equalsIgnoreCase(Boolean.FALSE.toString()))
		{
			allowedForTemplateSites = false;
		}
		else
		{
			if (serverConfigurationService.getStrings("wsetup.enableSiteTemplate.userType") != null) {
				List<String> userTypes = new ArrayList<>(Arrays.asList(serverConfigurationService.getStrings("wsetup.enableSiteTemplate.userType")));
				if (!userTypes.isEmpty())
				{
					User u = userDirectoryService.getCurrentUser();
					if (!(u != null && (securityService.isSuperUser() || userTypes.contains(u.getType()))))
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
			Map<String, String> templateCriteria = new HashMap<>(1);
			templateCriteria.put("template", "true");
			
			templateSites = siteService.getSites(SelectionType.ANY, null, null, templateCriteria, SortType.TITLE_ASC, null);
		}
		
		// If no templates could be found, stick an empty list in the context
		if(templateSites == null || templateSites.isEmpty()) templateSites = new ArrayList<>();

		// SAK-25400 sort templates by type
		context.put("templateSites", sortTemplateSitesByType(templateSites));
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
				
				// SAK-24423 - update site info for joinable site settings
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
			log.error(this + ".sitePropertiesIntoState: " + e.getMessage(), e);
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
			log.warn(this + ": - unknown STATE_SITE_TYPE");
		} else {
			state.setAttribute(STATE_SITE_TYPE, type);
		}
		
		// set tool registration list
		setToolRegistrationList(state, type, true);
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
							String titleConfig = serverConfigurationService.getString(CONFIG_TOOL_TITLE + mId);
							if (titleConfig != null && titleConfig.length() > 0)
							{
								// check whether there is a different title setting
								newTool.title = titleConfig;
							}
							else
							{
								// use the default
								newTool.title = toolManager.getTool(wSetupTool).getTitle();
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
	 * @return
	 */
	private String checkNullSiteType(SessionState state, String type, String siteId) {
		if (type == null) {
			if (siteId != null && siteService.isUserSite(siteId)) {
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
		GradebookGroupEnabler.removeFromState(state);
		SubNavEnabler.removeFromState(state);
	}

	private List orderToolIds(SessionState state, String type, List<String> toolIdList, boolean synoptic) {
		List rv = new Vector();

		// look for null site type
		if (type == null && state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null)
		{
			type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);
		}
		if (type != null && toolIdList != null) {
			List<String> orderedToolIds = serverConfigurationService.getToolOrder(siteTypeUtil.getTargetSiteType(type)); // UMICH-1035  
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
		String mycopyright = COPYRIGHT_SYMBOL + " " + Year.now().toString()
				+ ", " + userDirectoryService.getCurrentUser().getDisplayName()
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
		String[] iconUrls = null;
		String[] iconSkins = null;

		// get icon information
		if (serverConfigurationService.getStrings("iconNames") != null) {
			iconNames = serverConfigurationService.getStrings("iconNames");
		}
		if (serverConfigurationService.getStrings("iconUrls") != null) {
			iconUrls = serverConfigurationService.getStrings("iconUrls");
		}
		if (serverConfigurationService.getStrings("iconSkins") != null) {
			iconSkins = serverConfigurationService.getStrings("iconSkins");
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
				Tool tool = toolManager.getTool(originToolId);
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
				Tool tool = toolManager.getTool(originToolId);
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
					state.removeAttribute(STATE_IMPORT_SITE_TOOL_ITEMS);
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
			MathJaxEnabler.applySettingsToState(state, params);  // SAK-22384
			SubNavEnabler.applySettingsToState(state, params);

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
			boolean gbValidate = false;
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
												addAlert(state, rb.getFormattedMessage("java.emailinuse", new Object[] {emailId, serverConfigurationService.getServerName()}));
											}
										} else {
											addAlert(state, rb.getFormattedMessage("java.emailinuse", new Object[] {emailId, serverConfigurationService.getServerName()}));
										}
									}
								} catch (IdUnusedException ee) {
								}
							}
						}
					}
				} else if (id.endsWith(SiteManageConstants.GRADEBOOK_TOOL_ID)) {
					gbValidate = true;
				} else if (isMultipleInstancesAllowed(findOriginalToolId(state, id)) && (idSelected != null && !idSelected.contains(id) || idSelected == null))
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
								
								attributeInput = formattedText.sanitizeHrefURL(attributeInput);
								// save the attribute input if valid, otherwise generate alert
								if ( formattedText.validateURL(attributeInput) )
									attributes.put(attribute, attributeInput);
								else {
									addAlert(state, rb.getString("java.invurl"));
								}
							}
						}
						multipleToolConfiguration.put(id, attributes);
					}
				}
			}

			if(gbValidate) {//one validation for all gbs
				String[] selectedGroups = params.getStrings(GradebookGroupEnabler.SELECTED_GROUPS);
				if (selectedGroups != null) {
					state.setAttribute(GradebookGroupEnabler.SELECTED_GROUPS, new ArrayList(Arrays.asList(selectedGroups)));
				}
				if (params.getString("$gradebookType") != null) {
						state.setAttribute("gradebookType", params.getString("$gradebookType"));
				} else if (params.getString(GradebookGroupEnabler.VALUE_GRADEBOOK_GROUPS) != null) {
						state.setAttribute("gradebookType", params.getString(GradebookGroupEnabler.VALUE_GRADEBOOK_GROUPS));
				} else if (params.getString(GradebookGroupEnabler.VALUE_GRADEBOOK_SITE) != null) {
						state.setAttribute("gradebookType", params.getString(GradebookGroupEnabler.VALUE_GRADEBOOK_SITE));
				}
				if (GradebookGroupEnabler.VALUE_GRADEBOOK_GROUPS.equals(state.getAttribute("gradebookType")) && params.getStrings(GradebookGroupEnabler.SELECTED_GROUPS) == null) {
					addAlert(state, rb.getString("sinfo.gradebookgroupvnav.none"));
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
			Site site = getStateSite(state);
			if (site == null)
			{
				return;
			}
			Properties reqProps = params.getProperties();
			// remember the reqProps may contain multiple lti inputs, so we need to differentiate those inputs and store one tool specific input into the map
			HashMap<String, Map<String, Object>> ltiTools = (HashMap<String, Map<String, Object>>) state.getAttribute(STATE_LTITOOL_SELECTED_LIST);
			for ( Map.Entry<String,Map<String, Object>> ltiToolEntry : ltiTools.entrySet())
			{
				String ltiToolId = ltiToolEntry.getKey();
				Map<String, Object> ltiToolAttributes = ltiToolEntry.getValue();
				String[] contentToolModel= ltiService.getContentModel(Long.valueOf(ltiToolId), site.getId());
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
			String titleConfig = serverConfigurationService.getString(CONFIG_TOOL_TITLE + toolId);
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

		// SAK-24423 - joinable site settings
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
			String id = idManager.createUuid();
			
	
			// get the site template
			try {
				template = siteService.getSite(templateId);
			} catch (Exception e) {
				log.error(this + ".addSiteTypeFeatures:" + e.getMessage() + templateId, e);
			}
			if (template != null) {
				// create a new site based on the template
				try {
					edit = siteService.addSite(id, template);
					// set site type
					edit.setType(siteTypeUtil.getTargetSiteType(template.getType()));
				} catch (Exception e) {
					log.error(this + ".addSiteTypeFeatures:" + " add/edit site id=" + id, e);
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
						siteService.save(edit);
					} catch (Exception e) {
						log.error(this + ".addSiteTypeFeatures:" + " commitEdit site id=" + id, e);
					}
	
					// now that the site and realm exist, we can set the email alias
					// set the site alias as:
					User currentUser = userDirectoryService.getCurrentUser();
					List<String> pList = new ArrayList<String>();
					pList.add(currentUser != null ? currentUser.getEid():"");
					String alias = siteTypeProvider.getSiteAlias(type, pList);
					String channelReference = mailArchiveChannelReference(id);
					try {
						aliasService.setAlias(alias, channelReference);
					} catch (IdUsedException ee) {
						addAlert(state, rb.getFormattedMessage("java.alias.exists", new Object[]{alias}));
						log.error(this + ".addSiteTypeFeatures:" + rb.getFormattedMessage("java.alias.exists", new Object[]{alias}), ee);
					} catch (IdInvalidException ee) {
						addAlert(state, rb.getFormattedMessage("java.alias.isinval", new Object[]{alias}));
						log.error(this + ".addSiteTypeFeatures:" + rb.getFormattedMessage("java.alias.isinval", new Object[]{alias}), ee);
					} catch (PermissionException ee) {
						addAlert(state, rb.getString("java.addalias"));
						log.error(this + ".addSiteTypeFeatures:" + sessionManager.getCurrentSessionUserId() + " does not have permission to add alias. ", ee);
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
				if (log.isDebugEnabled())
				{
					log.debug(String.format(msg, StringUtils.trimToEmpty(site.getId())));
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
			return "/mailarchive"+Entity.SEPARATOR+"channel"+Entity.SEPARATOR+siteId+Entity.SEPARATOR+siteService.MAIN_CONTAINER;
		} else {
			return "";
		}
	}

	/**
	 * @return Get a map of all tools that support the import (transfer copy)
	 *         option
	 */
	protected Map<String, Optional<List<String>>> getImportableTools() {

		Map<String, Optional<List<String>>> rv = new HashMap<>();

		// offer to all EntityProducers
		for (EntityProducer ep : entityManager.getEntityProducers()) {
			if (ep instanceof EntityTransferrer) {
				EntityTransferrer et = (EntityTransferrer) ep;
				String[] tools = et.myToolIds();
				if (tools != null) {
					Arrays.stream(tools).forEach(t -> rv.put(t, et.getTransferOptions() ));
				}
			}
		}

		if (serverConfigurationService.getBoolean("site-manage.importoption.siteinfo", true)) {
			rv.put(SiteManageConstants.SITE_INFO_TOOL_ID, Optional.empty());
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
		boolean displayGradebook = false;

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

			if (site.getToolForCommonId(NEWS_TOOL_ID) != null) {
				displayNews = true;
			}
			if (site.getToolForCommonId(LESSONS_TOOL_ID) != null) {
				displayLessons = true;
			}
		}
		
		if (displayWebContent && !toolIdList.contains(WEB_CONTENT_TOOL_ID))
			toolIdList.add(WEB_CONTENT_TOOL_ID);
		if (displayNews && !toolIdList.contains(NEWS_TOOL_ID))
			toolIdList.add(NEWS_TOOL_ID);
		if (displayLessons && !toolIdList.contains(LESSONS_TOOL_ID))
			toolIdList.add(LESSONS_TOOL_ID);
		if (serverConfigurationService.getBoolean("site-manage.importoption.siteinfo", true)){
			toolIdList.add(SiteManageConstants.SITE_INFO_TOOL_ID);
		}
		
		
		return toolIdList;
	} // getToolsAvailableForImport

	// SAK-23256 - added userFilteringIfEnabled parameter
	private List<AcademicSession> setTermListForContext(Context context, SessionState state, boolean upcomingOnly, boolean useFilteringIfEnabled) {
		List<AcademicSession> terms = new ArrayList<>();
		if (upcomingOnly) {
			terms.addAll(courseManagementService.getCurrentAcademicSessions());
		} else {
			terms.addAll(courseManagementService.getAcademicSessions());
		}
		if (!terms.isEmpty()) {
			if (useFilteringIfEnabled)
			{
				if( !securityService.isSuperUser() )
				{
					if( serverConfigurationService.getBoolean( SAK_PROP_FILTER_TERMS,  false ) )
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
	 * SAK-23256
	 * 
	 * @return the filtered list of academic sessions
	 */
	public List<AcademicSession> filterTermDropDowns()
	{
		List<AcademicSession> academicSessions = new ArrayList<AcademicSession>();
		User user = userDirectoryService.getCurrentUser();
		
		if( courseManagementService != null && user != null && groupProvider != null)
		{
			Map<String, String> sectionsToRoles = groupProvider.getGroupRolesForUser(user.getEid());
			final Set<String> rolesAllowed = getRolesAllowedToAttachSection();
			Map<String, String> filteredSectionsToRoles = sectionsToRoles.entrySet().stream()
				.filter(entry->rolesAllowed.contains(entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			for (String sectionEid : filteredSectionsToRoles.keySet())
			{
				Section section = courseManagementService.getSection(sectionEid);
				if (section != null)
				{
					CourseOffering courseOffering = courseManagementService.getCourseOffering(section.getCourseOfferingEid());
					if (courseOffering != null)
					{
						academicSessions.add(courseOffering.getAcademicSession());
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
											String academicSessionEid,
											Map<String, CourseOffering> courseOfferingHash,
											Map<String, List<SectionObject>> sectionHash) {

		// looking for list of courseOffering and sections that should be
		// included in
		// the selection list. The course offering must be offered
		// 1. in the specific academic Session
		// 2. that the specified user has right to attach its section to a
		// course site
		// map = (section.eid, sakai rolename)
		if (groupProvider == null)
		{
			log.warn("Group provider not found");
			return;
		}
		
		Map<String, String> groupRoles = groupProvider.getGroupRolesForUser(userId, academicSessionEid);
		if (groupRoles == null) return;

		Set<String> roleSet = getRolesAllowedToAttachSection();
        for (String sectionEid : groupRoles.keySet()) {
            String role = groupRoles.get(sectionEid);
            if (includeRole(role, roleSet)) {
                getCourseOfferingAndSectionMap(academicSessionEid, courseOfferingHash, sectionHash, sectionEid);
            }
        }
		
		// now consider those user with affiliated sections
		List<String> affiliatedSectionEids = affiliatedSectionProvider.getAffiliatedSectionEids(userId, academicSessionEid);
		if (affiliatedSectionEids != null) {
            for (String affiliatedSectionEid : affiliatedSectionEids) {
                String sectionEid = affiliatedSectionEid;
                getCourseOfferingAndSectionMap(academicSessionEid, courseOfferingHash, sectionHash, sectionEid);
            }
		}
	} // prepareCourseAndSectionMap

	private void getCourseOfferingAndSectionMap(String academicSessionEid,
												Map<String, CourseOffering> courseOfferingHash,
												Map<String, List<SectionObject>> sectionHash,
												String sectionEid) {
		try {
			Section section = courseManagementService.getSection(sectionEid);
			String courseOfferingEid = section.getCourseOfferingEid();
			CourseOffering courseOffering = courseManagementService.getCourseOffering(courseOfferingEid);
			String sessionEid = courseOffering.getAcademicSession().getEid();
			if (academicSessionEid.equals(sessionEid)) {
				// a long way to the conclusion that yes, this course
				// offering
				// should be included in the selected list. Sigh...
				// -daisyf
				List<SectionObject> sectionList = sectionHash.get(courseOffering.getEid());
				if (sectionList == null) {
					sectionList = new ArrayList<>();
				}
				sectionList.add(new SectionObject(section));
				sectionHash.put(courseOffering.getEid(), sectionList);
				courseOfferingHash.put(courseOffering.getEid(), courseOffering);
			}
		} catch (IdNotFoundException e) {
			log.warn("Cannot find section {}, {}", sectionEid, e.toString());
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

	protected Set<String> getRolesAllowedToAttachSection() {
		// Use !site.template.[site_type]
		String azgId = "!site.template.course";
		AuthzGroup azgTemplate;
		try {
			azgTemplate = authzGroupService.getAuthzGroup(azgId);
		} catch (GroupNotDefinedException e) {
			log.error(this + ".getRolesAllowedToAttachSection: Could not find authz group " + azgId, e);
			return new HashSet();
		}
		Set<String> roles = azgTemplate.getRolesIsAllowed("site.upd");
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
	private List<CourseObject> prepareCourseAndSectionListing(String userId, String academicSessionEid, SessionState state) {
		// courseOfferingHash = (courseOfferingEid, courseOffering)
		// sectionHash = (courseOfferingEid, list of sections)
		Map<String, CourseOffering> courseOfferingHash = new HashMap<>();
		Map<String, List<SectionObject>> sectionHash = new HashMap<>();
		prepareCourseAndSectionMap(userId, academicSessionEid, courseOfferingHash, sectionHash);
		// courseOfferingHash & sectionHash should now be filled with stuffs
		// put section list in state for later use

		state.setAttribute(STATE_PROVIDER_SECTION_LIST, getSectionList(sectionHash));

		// use this to keep track of courseOffering that we have dealt with
		// already
		// this is important 'cos cross-listed offering is dealt with together
		// with its
		// equivalents
		List<String> dealtWith = new ArrayList<>();
		List<CourseObject> courseObjects = new ArrayList<>();
		for (CourseOffering o : sortCourseOfferings(courseOfferingHash.values())) {
			if (!dealtWith.contains(o.getEid())) {
				// 1. construct list of CourseOfferingObject for CourseObject
				List<CourseOfferingObject> lcoo = new ArrayList<>();
				CourseOfferingObject coo = new CourseOfferingObject(o, sectionHash.get(o.getEid()));
				lcoo.add(coo);

				// 2. check if course offering is cross-listed
				Set<CourseOffering> set = courseManagementService.getEquivalentCourseOfferings(o.getEid());
				if (set != null && !set.isEmpty()) {
                    for (CourseOffering eo : set) {
                        if (courseOfferingHash.containsKey(eo.getEid())) {
                            // => cross-listed, then list them together
                            CourseOfferingObject ecoo = new CourseOfferingObject(eo, sectionHash.get(eo.getEid()));
                            lcoo.add(ecoo);
                            dealtWith.add(eo.getEid());
                        }
                    }
				}
				CourseObject co = new CourseObject(o, lcoo);
				dealtWith.add(o.getEid());
				courseObjects.add(co);
			}
		}
		return courseObjects;
	} // prepareCourseAndSectionListing

	/* SAK-25400 template site types duplicated in list
	 * Sort template sites by type   
	 **/
	private Collection<Site> sortTemplateSitesByType(Collection<Site> templates) {
		String[] sortKey = {"type"};
		String[] sortOrder = {"asc"};
		return (Collection<Site>) sortCmObject(templates, sortKey, sortOrder);
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
	private Collection<CourseOffering> sortCourseOfferings(Collection<CourseOffering> offerings) {
		// Get the keys from sakai.properties
		String[] keys = serverConfigurationService.getStrings(SORT_KEY_COURSE_OFFERING);
		String[] orders = serverConfigurationService.getStrings(SORT_ORDER_COURSE_OFFERING);

		return (Collection<CourseOffering>) sortCmObject(offerings, keys, orders);
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
	private Collection<CourseSet> sortCourseSets(Collection<CourseSet> courses) {
		// Get the keys from sakai.properties
		String[] keys = serverConfigurationService.getStrings(SORT_KEY_COURSE_SET);
		String[] orders = serverConfigurationService.getStrings(SORT_ORDER_COURSE_SET);

		return (Collection<CourseSet>) sortCmObject(courses, keys, orders);
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
	private Collection<Section> sortSections(Collection<Section> sections) {
		// Get the keys from sakai.properties
		String[] keys = serverConfigurationService.getStrings(SORT_KEY_SECTION);
		String[] orders = serverConfigurationService.getStrings(SORT_ORDER_SECTION);

		return (Collection<Section>) sortCmObject(sections, keys, orders);
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
	private Collection<AcademicSession> sortAcademicSessions(Collection<AcademicSession> sessions) {
		// Get the keys from sakai.properties
		String[] keys = serverConfigurationService.getStrings(SORT_KEY_SESSION);
		String[] orders = serverConfigurationService.getStrings(SORT_ORDER_SESSION);

		return (Collection<AcademicSession>) sortCmObject(sessions, keys, orders);
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
	private Collection<?> sortCmObject(Collection<?> collection, String[] keys, String[] orders) {
		if (collection != null && !collection.isEmpty()) {
			// Add them to a list for the SortTool (they must have the form
			// "<key:order>" in this implementation)
			List<String> propsList = new ArrayList<>();
			
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
			SortTool sortTool = new SortTool();
			return sortTool.sort(collection, propsList);
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
	private Collection<?> sortCmObject(Collection<?> collection) {
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
			this.categoryDescription = courseManagementService
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
		public String description;
		public List<CourseOfferingObject> courseOfferingObjects;

		public CourseObject(CourseOffering offering, List<CourseOfferingObject> courseOfferingObjects) {
			this.eid = offering.getEid();
			this.title = offering.getTitle();
			this.description = offering.getDescription();
			this.courseOfferingObjects = courseOfferingObjects;
		}

		public String getEid() {
			return eid;
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
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
		return serverConfigurationService.getString(
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
				siteInfo.description = constructDescription(state, siteInfo.description);
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

	private String constructDescription(SessionState state, String startingDesc)
	{
		String description = startingDesc;
		List<String> providerDescriptionChosenList = (List<String>) state.getAttribute(STATE_ADD_CLASS_PROVIDER_DESCRIPTION_CHOSEN);
		if (providerDescriptionChosenList != null)
		{
			for (String providerSectionId : providerDescriptionChosenList)
			{
				try
				{
					Section s = courseManagementService.getSection(providerSectionId);
					if (s != null)
					{
						// only update the description if its not already present
						if (!StringUtils.containsIgnoreCase(description,  s.getDescription()))
						{
							description = StringUtils.defaultString(description) + " " + StringUtils.defaultString(s.getDescription());
						}
					}
				}
				catch (IdNotFoundException e)
				{
					log.warn("collectNewSiteInfo: cannot find section " + providerSectionId);
				}
			}
		}

		return description;
	}

	/**
	 * By default, courseManagement is implemented
	 * 
	 * @return
	 */
	private boolean courseManagementIsImplemented() {
		boolean returnValue = true;
		String isImplemented = serverConfigurationService.getString(
				"site-manage.courseManagementSystemImplemented", "true");
		if (("false").equals(isImplemented))
			returnValue = false;
		return returnValue;
	}

	private List getCMSections(String offeringEid) {
		if (offeringEid == null || offeringEid.trim().length() == 0)
			return null;

		if (courseManagementService != null) {
			try
			{
				Set sections = courseManagementService.getSections(offeringEid);
				if (sections != null)
				{
					Collection c = sortSections(new ArrayList(sections));
					return (List) c;
				}
			}
			catch (IdNotFoundException e)
			{
				log.warn("getCMSections: Cannot find sections for " + offeringEid);
			}
		}

		return new ArrayList(0);
	}

	private List getCMCourseOfferings(String subjectEid, String termID) {
		if (subjectEid == null || subjectEid.trim().length() == 0
				|| termID == null || termID.trim().length() == 0)
			return null;

		if (courseManagementService != null) {
			Set offerings = courseManagementService.getCourseOfferingsInCourseSet(subjectEid);// ,
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
					rv = addCategories(rv, courseManagementService.getChildCourseSets(cs.getEid()));
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
		Boolean skipCourseSectionSelection = serverConfigurationService.getBoolean(SAK_PROP_SKIP_COURSE_SECTION_SELECTION, Boolean.FALSE);
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
							Section sect = courseManagementService.getSection(string);
							if (sect != null)
							{
								SectionObject so = new SectionObject(sect);
								soList.add(so);
							}
						}
						catch (IdNotFoundException e)
						{
							log.warn("prepFindPage: Cannot find section " + string);
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
		String s = serverConfigurationService.getString("officialAccountName");
		
		if (uniqueName == null) 
		{
			addAlert(state, rb.getFormattedMessage("java.author", new Object[]{serverConfigurationService.getString("officialAccountName")}));
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
				String uniqname = StringUtils.trimToNull(params.getString("uniqname"));
				state.setAttribute(STATE_SITE_QUEST_UNIQNAME, uniqname);

				SiteInfo siteInfo = state.getAttribute(STATE_SITE_INFO) != null? (SiteInfo) state.getAttribute(STATE_SITE_INFO):new SiteInfo();
				if (params.getString("additional") != null) {
					siteInfo.additional = params.getString("additional");
				}
				state.setAttribute(STATE_SITE_INFO, siteInfo);

				if (state.getAttribute(STATE_FUTURE_TERM_SELECTED) != null
						&& !((Boolean) state
								.getAttribute(STATE_FUTURE_TERM_SELECTED))
								.booleanValue()) 
				{
					// if a future term is selected, do not check authorization
					// uniqname
					if (uniqname == null) 
					{
						addAlert(state, rb.getFormattedMessage("java.author", new Object[]{serverConfigurationService.getString("officialAccountName")}));
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
								userDirectoryService.getUserByEid(instructorId);
							}
							catch (UserNotDefinedException e) 
							{
								addAlert(state, rb.getFormattedMessage("java.validAuthor", new Object[]{serverConfigurationService.getString("officialAccountName")}));
								log.warn(this + ".doFind_course:" + rb.getFormattedMessage("java.validAuthor", new Object[]{serverConfigurationService.getString("officialAccountName")}));
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
							state.setAttribute(STATE_TEMPLATE_INDEX, "18");
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

	private List<SectionObject> getSectionList(Map<String, List<SectionObject>> sectionHash) {
        return sectionHash.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
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
					Section s = courseManagementService.getSection(sectionEid);
					if (s != null)
					{
						SectionObject so = new SectionObject(s);
						so.setAuthorizer(new ArrayList(Arrays.asList(userId.split(","))));
						list.add(so);
					}
				}
				catch (IdNotFoundException e)
				{
					log.warn("prepareSectionObject: Cannot find section " + sectionEid);
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
		SessionState state = getState(req);
 
		if (SITE_MODE_HELPER_DONE.equals(state.getAttribute(STATE_SITE_MODE)))
		{
			String url = (String) sessionManager.getCurrentToolSession().getAttribute(Tool.HELPER_DONE_URL);

			sessionManager.getCurrentToolSession().removeAttribute(Tool.HELPER_DONE_URL);

			// TODO: Implement cleanup.
			cleanState(state);
			// Helper cleanup.

			cleanStateHelper(state);
			
			if (log.isDebugEnabled())
			{
				log.debug("Sending redirect to: "+ url);
			}
			try
			{
				res.sendRedirect(url);
			}
			catch (IOException e)
			{
				log.error("Problem sending redirect to: "+ url,  e);
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
		return serverConfigurationService.getPortalUrl() + "/" + 
			serverConfigurationService.getString("portal.handler.default", "site") + 
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
	 * @return
	 */
	private boolean isHomePage(SitePage page)
	{
		if (page == null) {
			return false;
		}
		//removed "check by title" : that creates unexpected results with normal pages titled as "HOME"
		return page.isHomePage();
	}

	public boolean displaySiteAlias() {
		if (serverConfigurationService.getBoolean("wsetup.disable.siteAlias", false)) {
			return false;
		}
		return true;
	}
	
	private void putDownloadParticipantPDFLinkIntoContext(Context context, RunData data, Site site) {
		// the status servlet reqest url
		String url = RequestFilter.serverUrl(data.getRequest()) + "/sakai-site-manage-tool/tool/printparticipant/" + site.getId();
		context.put("downloadParticipantsPDF_URL", url);
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
	 */
	private void doSite_copyFromTemplate(RunData data)
	{	
		ParameterParser params = data.getParameters();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
	
		// read template information
		readCreateSiteTemplateInformation(params, state);
		
		// create site
		state.setAttribute(STATE_TEMPLATE_INDEX, "18");
	}
	
	/**
	 * create course site from template, next step would be select roster
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
			Site templateSite = siteService.getSite(templateSiteId);
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
			
			Boolean copyTemplateDescription = serverConfigurationService.getBoolean(SAK_PROP_COPY_TEMPLATE_DESCRIPTION, Boolean.TRUE);
			if(copyTemplateDescription.booleanValue()){
				siteInfo.description = templateSite.getDescription();
			}
			
			siteInfo.short_description = templateSite.getShortDescription();
			siteInfo.joinable = templateSite.isJoinable();
			siteInfo.joinerRole = templateSite.getJoinerRole();
			state.setAttribute(STATE_SITE_INFO, siteInfo);
			
			// SAK-24423 - update site info for joinable site settings
			JoinableSiteSettings.updateSiteInfoFromParams( params, siteInfo );
			
			// whether to copy users or site content over?
			if (params.getBoolean("copyUsers")) state.setAttribute(STATE_TEMPLATE_SITE_COPY_USERS, Boolean.TRUE); else state.removeAttribute(STATE_TEMPLATE_SITE_COPY_USERS);
			if (params.getBoolean("copyContent")) state.setAttribute(STATE_TEMPLATE_SITE_COPY_CONTENT, Boolean.TRUE); else state.removeAttribute(STATE_TEMPLATE_SITE_COPY_CONTENT);
			if (params.getBoolean("publishSite")) state.setAttribute(STATE_TEMPLATE_PUBLISH, Boolean.TRUE); else state.removeAttribute(STATE_TEMPLATE_PUBLISH);
		}
		catch(Exception e){
			log.warn(this + "readCreateSiteTemplateInformation: problem of getting template site: " + templateSiteId);
		}
	}

	/**
	 * redirect course creation process after the term selection step
	 * @param params
	 * @param state
	 * @param termFieldName
	 */
	private void redirectCourseCreation(ParameterParser params, SessionState state, String termFieldName) {
		User user = userDirectoryService.getCurrentUser();
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
			AcademicSession t = courseManagementService.getAcademicSession(academicSessionEid);
			state.setAttribute(STATE_TERM_SELECTED, t);
			if (t != null) {
				List<CourseObject> sections = prepareCourseAndSectionListing(userId, t.getEid(), state);

				isFutureTermSelected(state);

				if (sections != null && sections.size() > 0) {
					state.setAttribute(STATE_TERM_COURSE_LIST, sections);
					state.setAttribute(STATE_TEMPLATE_INDEX, "36");
					state.setAttribute(STATE_AUTO_ADD, Boolean.TRUE);
				} else {
					state.removeAttribute(STATE_TERM_COURSE_LIST);
					
					Boolean skipCourseSectionSelection = serverConfigurationService.getBoolean(SAK_PROP_SKIP_COURSE_SECTION_SELECTION, Boolean.FALSE);
					if (!skipCourseSectionSelection.booleanValue() && courseManagementIsImplemented())
					{
						state.setAttribute(STATE_TEMPLATE_INDEX, "53");
					}
					else
					{
						if (serverConfigurationService.getBoolean(SAK_PROP_FILTER_TERMS, Boolean.FALSE))
						{
							// Filter terms is intended to prevent users from hitting case 37 (manual creation).
							// This will handle element inspecting the academic session dropdown
							state.setAttribute(STATE_TEMPLATE_INDEX, "36");
						}
						else
						{
							state.setAttribute(STATE_TEMPLATE_INDEX, "37");
						}
					}		
				}

			} else { // not course type
				state.setAttribute(STATE_TEMPLATE_INDEX, "37");
			}
		}

		state.setAttribute(VM_CONT_NO_ROSTER_ENABLED, serverConfigurationService.getBoolean(SAK_PROP_CONT_NO_ROSTER_ENABLED, false));
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

			MathJaxEnabler.applySettingsToState(state, params);  // SAK-22384

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
	    return serverConfigurationService.getLocaleFromString(localeString);
	}
	
	/**
	 * Handle the eventSubmit_doUnjoin command to have the user un-join this site.
	 * @param data
	 */
	public void doUnjoin(RunData data) {
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		final ParameterParser params = data.getParameters();
		final String id = params.get("itemReference");

		if (id != null) {
			try {
				siteService.unjoin(id);
				String userHomeURL = devHelperService.getUserHomeLocationURL(devHelperService.getCurrentUserReference());
				addAlert(state, rb.getFormattedMessage("site.unjoin", userHomeURL));
			} catch (IdUnusedException e) {
				// Something strange happened, log and notify the user
				log.debug("Unexpected error: ", e);
				addAlert(state, rb.getFormattedMessage("unexpectedError", serverConfigurationService.getString("mail.support")));
			} catch (PermissionException e) {
				// This could occur if the user's role is the maintain role for the site, and unjoining would leave the site without
				// a user with the maintain role
				log.warn(e.getMessage());
				addAlert(state, rb.getString("site.unjoin.permissionException"));
			}
		}
	} // doUnjoin

	
	/**
	 * @return Returns the prefLocales
	 */
	public List<Locale> getPrefLocales()
	{
		// Initialize list of supported locales, if necessary
		if (prefLocales.size() == 0) {
			Locale[] localeArray = serverConfigurationService.getSakaiLocales();
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
			Site site = siteService.getSite(siteId);
			if (site != null) {
				quota = getSiteSpecificQuota(site);
			}
		} catch (IdUnusedException e) {
			log.error("Quota calculation could not find the site " + siteId
					+ "for site specific quota calculation",
					log.isDebugEnabled() ? e : null);
		}
		return quota;
	}

	
	// SAK-20797
	/**
	 * return quota set on this specific site
	 * @return value of site-specific quota or 0 if not found
	 */
	private long getSiteSpecificQuota(Site site) {
		long quota = 0;
		try
		{
			String collId = contentHostingService
					.getSiteCollection(site.getId());
			ContentCollection site_collection = contentHostingService.getCollection(collId);
			long siteSpecific = site_collection.getProperties().getLongProperty(
					ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
			quota = siteSpecific;
		}
		catch (Exception ignore)
		{
			log.warn("getQuota: reading quota property for site : " + site.getId() + " : " + ignore);
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
			String args = formatter.format(1.0 * quota / (1024L * 1024L * 1024L));
			size = rb.getFormattedMessage("size.gb", args);
		} else if (quota > 700000L) {
			String args = formatter.format(1.0 * quota / (1024L * 1024L));
			size = rb.getFormattedMessage("size.mb", args);
		} else if (quota > 700L) {
			String args = formatter.format(1.0 * quota / 1024L);
			size = rb.getFormattedMessage("size.kb", args);
		} else {
			String args = formatter.format(quota);
			size = rb.getFormattedMessage("size.bytes", args);
		}
		return size;
	}

	@Data
	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	public class JoinableGroup {

		@EqualsAndHashCode.Include
		private String id;
		private String reference;
		private String title;
		private String joinableSet;
		private String description;
		private String joinableOpenDate;
		private String joinableCloseDate;
		private boolean interactableByDate;
		private boolean unjoinable;
		private boolean enrolled;
		private int size;
		private int max;
		private String memberDisplayNames;
		private boolean preview;
		private boolean locked;

		public JoinableGroup(Group group) {
			Objects.requireNonNull(group, "Group can't be null");
			this.id = group.getId();
			this.reference = group.getReference();
			this.title = group.getTitle();
			this.description = group.getDescription();
			this.joinableSet = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);
			this.unjoinable = BooleanUtils.toBoolean(group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_UNJOINABLE));
			this.max = NumberUtils.toInt(group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET_MAX));
			this.preview = BooleanUtils.toBoolean(group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET_PREVIEW));
			this.locked = group.getRealmLock().equals(AuthzGroup.RealmLockMode.ALL) || group.getRealmLock().equals(AuthzGroup.RealmLockMode.MODIFY);
			this.size = group.getMembers().size();
			this.memberDisplayNames = this.size > 0 ? initMemberDisplayNames(group) : "";
		}

		private String initMemberDisplayNames(Group group) {
			Set<String> groupMemeberIds = group.getMembers().stream().map(Member::getUserId).collect(Collectors.toSet());
			Site site = group.getContainingSite();

			if(!siteService.allowViewRoster(site.getId())
					&& (securityService.unlock("roster.viewHidden", site.getReference()) || securityService.unlock("roster.viewHidden", this.reference))) {
				// filter hidden users as the user doesn't have permission to view them
				Set<String> privateUserIds = privacyManager.findHidden(site.getReference(), groupMemeberIds);
				groupMemeberIds.removeAll(privateUserIds);
			}

			return groupMemeberIds.stream().map(id -> {
				try {
					return userDirectoryService.getUser(id).getDisplayName();
				} catch (UserNotDefinedException unde) {
					log.debug("UserDirectoryService could not find user with id={}: {}", id, unde.toString());
					this.size--;
					return "";
				}
			}).collect(Collectors.joining(", "));
		}

		public boolean isFull() {
			return this.size >= this.max;
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
		String archiveUnzipBase = serverConfigurationService.getString("archive.storage.path", FileUtils.getTempDirectoryPath());
	
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
			log.error(e.getMessage(), e); //general catch all for the various exceptions that occur above. all are failures.
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
			log.error(e.getMessage(), e); //general catch all for the various exceptions that occur above. all are failures.
			addAlert(state, rb.getString("archive.createsite.failedmerge"));
		}
		
	}
	
	/**
	 * Get the list of tools that are in a list of sites that are available for import.
	 * 
	 * Only tools with content will be collected. See hasContent(toolId, siteId) for the behaviour.
	 * 
	 * @param sites of siteids to check, could be a singleton
	 * @return a list of toolIds that are in the sites that are available for import
	 * 
	 */
	private Map<String, Optional<List<String>>> getToolsInSitesAvailableForImport(List<Site> sites) {

		Map<String, Optional<List<String>>> allImportTools = getImportableTools();
		
		Map<String, Optional<List<String>>> importToolsInSites = new HashMap<>();
		
		for (Site site: sites) {
			for (String toolId: allImportTools.keySet()) {
				if (site.getToolForCommonId(toolId) != null) {
					//check the tool has content. 
					//this caters for the case where we only selected one site for import, this means the tool won't show in the list at all.
					if (hasContent(toolId, site.getId())) {
						importToolsInSites.put(toolId, allImportTools.get(toolId));
					}
				}
			}
		}
	
		return importToolsInSites;
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
				if(site.getToolForCommonId(toolId) != null ||
						(StringUtils.isNotBlank(toolId) && toolId.contains(GRADEBOOK_TOOL)) &&
						(site.getToolForCommonId(GRADEBOOK_TOOL_ID) != null || site.getToolForCommonId(GRADEBOOKNG_TOOL_ID) != null) &&
						(hasContent(toolId, site.getId()) || hasContent(GRADEBOOK_TOOL_ID, site.getId()) || hasContent(GRADEBOOKNG_TOOL_ID, site.getId()))) {
					toolsWithContent.add(toolId);
				}
			}
			
			log.debug("Site: " + site.getId() + ", has the following tools with content: " + toolsWithContent);
			
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

		for (EntityProducer ep : entityManager.getEntityProducers()) {
			if (ep instanceof EntityTransferrer) {
				EntityTransferrer et = (EntityTransferrer) ep;

				if (ArrayUtils.contains(et.myToolIds(), toolId)) {
					if (ep instanceof ContentExistsAware) {
						ContentExistsAware cea = (ContentExistsAware) ep;
						log.debug("Checking tool content for site:{}, tool: {}", siteId, et.myToolIds());
						return cea.hasContent(siteId);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Helper to check if a tool in a site has selectable entities, displayed as checkboxes in the Import from Site process.
	 */
	private Map<String, Set<String>> getSiteImportToolsWithSelectableContent(List<Site> sites, List<String> toolIds) {
		Map<String, Set<String>> siteToolsWithSelectableContent = new HashMap<>();
		for(Site site: sites) {
			Set<String> toolsWithSelectableContent = new HashSet<>();
			for(String toolId: toolIds) {
				if (toolHasSelectableContent(toolId, site.getId())) {
					toolsWithSelectableContent.add(toolId);
				}
			}

			log.debug("Site: {}, has the following tools with selectable content: {}", site.getId(), toolsWithSelectableContent);

			siteToolsWithSelectableContent.put(site.getId(), toolsWithSelectableContent);
		}
		return siteToolsWithSelectableContent;
	}

	private boolean toolHasSelectableContent(String toolId, String siteId) {

		for (EntityProducer ep : entityManager.getEntityProducers()) {
			if (ep instanceof EntityTransferrer) {
				EntityTransferrer et = (EntityTransferrer) ep;
				if (ArrayUtils.contains(et.myToolIds(), toolId)) {
					List<Map<String, String>> em = et.getEntityMap(siteId);
					return em != null && !em.isEmpty();
				}
			}
		}

		return false;
	}

	/**
	 * Responsible for checking validation status of the original versus stripped site title, and
	 * adding necessary error messages to the STATE.
	 * @param titleOrig the original site title as entered by the user
	 * @param titleStripped the produce of passing the original string into FormattedText.stripHtmlFromText(titleOrig, true, true);
	 * @param true if the stripped title passes all validation; false otherwise
	 */
	private boolean isSiteTitleValid(String titleOrig, String titleStripped, SessionState state) {
		SiteTitleValidationStatus status = siteService.validateSiteTitle(titleOrig, titleStripped);

		if (null != status) switch(status)
		{
			case STRIPPED_TO_EMPTY:
				addAlert(state, rb.getString("siteTitle.htmlStrippedToEmpty"));
				return false;
			case EMPTY:
				addAlert(state, rb.getString("java.specify"));
				return false;
			case TOO_LONG:
				addAlert(state, rb.getFormattedMessage("site_group_title_length_limit", new Object[] {SITE_GROUP_TITLE_LIMIT}));
				return false;
		}

		return true;
	}

	/**
	 * Generate a HashMap of all the variables stored within the state.
	 * @param state
	 * @return
	 */
	private HashMap<String, Object> generateStateMap(SessionState state) {
		HashMap<String, Object> stateMap = new HashMap<>();
		for (String name : state.getAttributeNames()) {
			stateMap.put(name, state.getAttribute(name));
		}
		return stateMap;
    }

	/**
	 * Create a list of the valid layout names.
	 *
	 * @return A List (String) of the value layout names.
	 */
	private List layoutsList()
	{
		List rv = new Vector();
		String[] layoutNames = siteService.getLayoutNames();
		for (int i = 0; i < layoutNames.length; i++)
		{
			rv.add(layoutNames[i]);
		}
		return rv;

	} // layoutsList


	/**
	 * handle with manage overview options
	 *
	 */
	public void doManage_overview_option(RunData data) {
		String option = data.getParameters().getString("option");
		if ("save".equals(option)) {
			doSave_overview(data);
		} else if ("cancel".equals(option)) {
			doCancel_overview(data);
		}else if (StringUtils.contains("layout", option)){
			update_layout_option(data);
		}
	} // doManage_overview_option

	public void update_layout_option(RunData data){
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		SitePage page = (SitePage) state.getAttribute("overview");

		try {
			int layout = Integer.parseInt(data.getParameters().getString("layout")) - 1; //convert back to 0 based.
			page.setLayout(layout);
			state.setAttribute("overview", page);
		}catch(Exception e){
			log.warn("Reading layout: {}" + e.getMessage());
		}
	}

	/**
	 * Move the tool up in the order.
	 */
	public void doEdit_tool_up(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String id = data.getParameters().getString("id");

		// get the tool
		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("overview");
		List<ToolConfiguration> tools = (List<ToolConfiguration>) state.getAttribute("tools");
		if ( tools == null ) return;
		ToolConfiguration tool = null;

		for(ToolConfiguration pageTool: tools){
			if(pageTool.getToolId().equals(StringUtils.trimToNull(id))){
				tool = pageTool;
			}
		}
		if ( tool == null ) return;
		String hints = tool.getLayoutHints();
		String[] hintArr = hints.split(",");
		String col = null;
		if(hintArr.length == 2 && page.getLayout() == 1){ // 2 col layout
			if(StringUtils.trimToNull(hintArr[1]).equals("1")) {
				col = "1";
			}else if(StringUtils.trimToNull(hintArr[1]).equals("0")){
				col = "0";
			}
		}
		// move it
		int indexOfLastItemInCol = -1;
		int toolIndex = -1;
		if(page.getLayout() == 1) {
			for(int i=0; i<tools.size(); i++){

				if (StringUtils.trimToNull(tools.get(i).getLayoutHints().split(",")[1]).equals(col) && !tools.get(i).equals(tool)) {
					indexOfLastItemInCol = i;
				}

				if (tools.get(i).equals(tool)) {
					if (i == 0) {
						return; //nothing actually needs to happen, its already on top
					}

					String prevHint = tools.get(indexOfLastItemInCol).getLayoutHints();
					tool.setLayoutHints(prevHint);
					tools.get(indexOfLastItemInCol).setLayoutHints(hints); //swap this layout hint with the tool above it.
					int prevPageOrder = tools.get(indexOfLastItemInCol).getPageOrder();
					tools.get(indexOfLastItemInCol).setPageOrder(tool.getPageOrder());
					tool.setPageOrder(prevPageOrder);

					toolIndex = i;
					break;
				}
			}
		}else{
			for(int i=0; i<tools.size(); i++){
				if(tools.get(i).equals(tool)){
					if( i == 0){
						return;
					}
					String prevHint = tools.get(indexOfLastItemInCol).getLayoutHints();
					tool.setLayoutHints(prevHint);
					tools.get(indexOfLastItemInCol).setLayoutHints(hints); //swap this layout hint with the tool above it.
					int prevPageOrder = tools.get(indexOfLastItemInCol).getPageOrder();
					tools.get(indexOfLastItemInCol).setPageOrder(tool.getPageOrder());
					tool.setPageOrder(prevPageOrder);

					toolIndex = i;
					break;
				}else{
					indexOfLastItemInCol = i;
				}
			}
		}
		Collections.swap(tools, indexOfLastItemInCol, toolIndex);

		page.setTools(tools);
		state.setAttribute("tools", tools);
		state.setAttribute("overview", page);
		
	} // doEdit_tool_up

	/**
	 * Move the tool down in the order.
	 */
	public void doEdit_tool_down(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String id = data.getParameters().getString("id");
		

		// get the tool
		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("overview");
		List<ToolConfiguration> tools = (List<ToolConfiguration>) state.getAttribute("tools");
		if ( tools == null ) return;
		ToolConfiguration tool = null;

		for(ToolConfiguration pageTool: tools){
			if(pageTool.getToolId().equals(StringUtils.trimToNull(id))){
				tool = pageTool;
			}
		}

		if ( tool == null ) return;
		String hints = tool.getLayoutHints();
		String[] hintArr = hints.split(",");
		String col = null;

		if(hintArr.length == 2 && page.getLayout() == 1){ // 2 col layout
			if(StringUtils.trimToNull(hintArr[1]).equals("1")) {
				col = "1";
			}else if(StringUtils.trimToNull(hintArr[1]).equals("0")){
				col = "0";
			}
		}

		int indexOfNextItemInCol = -1;
		int toolIndex = -1;
		// move it
		if(page.getLayout() == 1) {
			for (int i = 0; i < tools.size(); i++) {
				if (StringUtils.trimToNull(tools.get(i).getLayoutHints().split(",")[1]).equals(col) && tools.get(i).equals(tool)) {
					toolIndex = i;
					if (i == tools.size() - 1) {
						return; //already at bottom. do nothing.
					}
				}

				if (!tools.get(i).equals(tool) && toolIndex != -1 && StringUtils.trimToNull(tools.get(i).getLayoutHints().split(",")[1]).equals(col)) {
					String nextHint = tools.get(i).getLayoutHints();
					tools.get(toolIndex).setLayoutHints(nextHint);
					tools.get(i).setLayoutHints(hints); //swap this layout hint with the tool below it.
					tools.get(toolIndex);
					int nextPageOrder = tools.get(i).getPageOrder();
					tools.get(i).setPageOrder(tool.getPageOrder());
					tools.get(toolIndex).setPageOrder(nextPageOrder);
					indexOfNextItemInCol = i;
					break;
				}
			}
		}else{
			for(int i=0; i<tools.size(); i++){
				if(tools.get(i).equals(tool)){
					toolIndex = i;
				}else if(toolIndex != -1){
					String nextHint = tools.get(i).getLayoutHints();
					tools.get(toolIndex).setLayoutHints(nextHint);
					tools.get(i).setLayoutHints(hints); //swap this layout hint with the tool below it.
					tools.get(toolIndex);
					int nextPageOrder = tools.get(i).getPageOrder();
					tools.get(i).setPageOrder(tool.getPageOrder());
					tools.get(toolIndex).setPageOrder(nextPageOrder);
					indexOfNextItemInCol = i;
					break;
				}
			}
		}

		Collections.swap(tools, toolIndex, indexOfNextItemInCol);

		page.setTools(tools);
		state.setAttribute("tools", tools);
		state.setAttribute("overview", page);
		

	} // doEdit_tool_down

	/**
	 * Move the tool right in the 2-column layout option
	 */
	public void doEdit_tool_right(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String id = data.getParameters().getString("id");
		
		// get the tool
		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("overview");
		if ( page == null ) return;
		ToolConfiguration tool = page.getTool(id);
		if ( tool == null ) return;

		// move it
		String hints = tool.getLayoutHints();
		String[] hintArr = hints.split(",");
		if(hintArr.length == 0){
			tool.setLayoutHints("0,1"); //default to 0, 1 if hint doesnt exist.
		}else{
			hintArr[1]="1"; //replace column with "1".
			String hint = String.join(",", hintArr);
			tool.setLayoutHints(hint);
		}
	} // doEdit_tool_right

	/**
	 * Move the tool left 2-column layout option
	 */
	public void doEdit_tool_left(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String id = data.getParameters().getString("id");
		if ( id == null ) return;
		

		// get the tool
		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("overview");
		if ( page == null ) return;
		ToolConfiguration tool = page.getTool(id);
		if ( tool == null ) return;

		// move it
		String hints = tool.getLayoutHints();
		String[] hintArr = hints.split(",");
		if(hintArr.length == 0){
			tool.setLayoutHints("0,0"); //default to 0, 0 if hint doesnt exist.
		}else{
			hintArr[1]="0"; //replace column with "0".
			String hint = String.join(",", hintArr);
			tool.setLayoutHints(hint);
		}
	} // doEdit_tool_left

	/**
	 * Handle a request to save the edit from either page or tools list mode - no form to read in.
	 */
	public void doSave_overview(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// commit the change
		Site site = (Site) state.getAttribute("site");
		if (readPageForm(data, state))
		{
		SitePage page = (SitePage) state.getAttribute("overview");
		if (!validateMinWidget(page, state))
		{
			return;
		}

		List<ToolConfiguration> tools = page.getTools();

			try
			{
				SitePage savedPage = site.getPage(page.getId()); //old page, will update tool list.

				savedPage.setTools(tools);
				savedPage.setLayout(page.getLayout());
				savedPage.setTitle(page.getTitle());
				for(ToolConfiguration tool: savedPage.getTools()){
					tool.save();
				}

				ResourcePropertiesEdit rp = site.getPropertiesEdit();

				rp.addProperty(Site.PROP_CUSTOM_OVERVIEW, Boolean.TRUE.toString());
				siteService.save(site);
			}
			catch (PermissionException | IdUnusedException e)
			{
				log.warn(e.getMessage());
			}
		}

		// cleanup
		state.removeAttribute("tools");
		state.removeAttribute("leftTools");
		state.removeAttribute("rightTools");
		state.removeAttribute("overview");
		state.removeAttribute("site");
		state.removeAttribute("allWidgets");
		state.removeAttribute("fromHome");

		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		schedulePeerFrameRefresh("sitenav");

		doContinue(data);

	} // doSaveOverview

	/**
	 * Read the page form and update the site in state.
	 *
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readPageForm(RunData data, SessionState state)
	{
		update_layout_option(data);

		// get the page - it's there
		SitePage page = (SitePage) state.getAttribute("overview");
		ParameterParser params = data.getParameters();
		//save layout hints
		List<ToolConfiguration> tools;

		if(page.getLayout() == 0) {
			tools = (List<ToolConfiguration>) state.getAttribute("tools");
			if(tools != null){
				for(int i=0; i<tools.size(); i++){
					String hints = tools.get(i).getLayoutHints();
					tools.get(i).setLayoutHints(hints);
					String[] hintArr = hints.split(",");
					if(hintArr.length == 0){
						tools.get(i).setLayoutHints("0,0"); //default to 0, 0 if hint doesnt exist.
					}else{
						hintArr[1]="0"; //replace column with "0".
						String hint = String.join(",", hintArr);
						tools.get(i).setLayoutHints(hint);
					}
				}
			}
		}else {
			List<ToolConfiguration> leftTools = (List<ToolConfiguration>) state.getAttribute("leftTools");
			List<ToolConfiguration> rightTools = (List<ToolConfiguration>) state.getAttribute("rightTools");


			tools = new ArrayList<>();
			tools.addAll(leftTools);
			tools.addAll(rightTools);
		}

		if(tools != null && validateLayoutHints(tools)) {
			tools = sortTools(tools, page);
			page.setTools(tools);
			state.setAttribute("tools", tools);
			state.setAttribute("overview", page);
			return true;
		}else{
			addAlert(state, rb.getString("manover.layhintletter"));
			return false;
		}
	} // readPageForm

	private boolean validateLayoutHints(List<ToolConfiguration> tools){
		Pattern p = Pattern.compile("\\d{1,},\\d{1,}");
		for(ToolConfiguration tool : tools){
			String hint = tool.getLayoutHints();
			Matcher m = p.matcher(hint);
			if(!m.matches()){
				return false;
			}
		}
		return true;
	}

	private List<Tool> findWidgets() {
		Set<String> categories = new HashSet<>();
		categories.add("widget");

		Set<Tool> widgets = toolManager.findTools(categories, null, false);

		List<Tool> features = new ArrayList<>(widgets);
		features.sort(new ToolTitleComparator());
		return Collections.unmodifiableList(features);
	}

	public void doAdd_widget(RunData data){
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String id = params.getString("id");
		if (StringUtils.isBlank(id)) {
			return;
		}
		// make the tool so we have the id
		SitePage page = (SitePage) state.getAttribute("overview");
		if (page == null) {
			return;
		}
		ToolConfiguration tool = page.addTool(id);
		tool.setLayoutHints("0,0"); //assume top left, it will be sorted later-- val just cant be null

		List<Tool> widgets = (List<Tool>) state.getAttribute("allWidgets");
		if (widgets == null) {
			widgets = findWidgets();
			state.setAttribute("allWidgets", widgets);
		}
		List<ToolConfiguration> tools = (List<ToolConfiguration>) state.getAttribute("tools");
		if (tools == null) {
			tools = new ArrayList<>(page.getTools());
		}

		for(Tool widget: widgets){
			if(widget.getId().equals(id)){
				tool.setTitle(widget.getTitle());
			}
		}

		tools.add(tool);
		tools = sortTools(tools, page); //run a sort of the tools now that the new one has been added.
		state.setAttribute("tools", tools);
		state.setAttribute("overview", page);
	}

	public void doRemove_widget(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		SitePage page = (SitePage) state.getAttribute("overview");
		List<ToolConfiguration> tools = (List<ToolConfiguration>) state.getAttribute("tools");
		if ( tools == null ) return;

		if (tools.size() <= 1 || !validateMinWidget(page, state)) {
			return;
		}

		List<ToolConfiguration> removedTools = (List<ToolConfiguration>) state.getAttribute("removedTools");
		if(removedTools == null){
			removedTools = new ArrayList<>();
		}

		String id = params.getString("id");
		for(ToolConfiguration tool: tools){
			if(tool.getTool().getId().equals(id)){
				removedTools.add(tool);
			}
		}

		tools.removeAll(removedTools);

		state.setAttribute("tools", tools);
	}

	private boolean validateMinWidget(SitePage page, SessionState state) {
		if (page == null || CollectionUtils.isEmpty(page.getTools())) {
			addAlert(state, rb.getString("manover.minwidget"));
			return false;
		}

		return true;
	}

	private String getDateFormat(Date date) {
		String f = userTimeService.shortPreciseLocalizedTimestamp(date.toInstant(), userTimeService.getLocalTimeZone(), comparator_locale);
		return f;
	}
}
