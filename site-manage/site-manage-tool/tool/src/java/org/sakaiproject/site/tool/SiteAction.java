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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.cover.AliasService;
import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.archive.cover.ArchiveService;
import org.sakaiproject.archive.cover.ImportMetadataService;
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
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.email.cover.EmailService;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.mailarchive.cover.MailArchiveService;
import org.sakaiproject.site.api.Course;
import org.sakaiproject.site.api.CourseMember;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.Term;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.cover.CourseManagementService;
import org.sakaiproject.site.cover.SiteService;
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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
* <p>SiteAction controls the interface for worksite setup.</p>
*/
public class SiteAction extends PagedResourceActionII
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SiteAction.class);

	/** portlet configuration parameter values**/
	/** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("sitesetupgeneric");
	
	private static final String SITE_MODE_SITESETUP = "sitesetup";
	private static final String SITE_MODE_SITEINFO= "siteinfo";
	private static final String SITE_MODE_SITEMANAGE = "sitemanage"; 
	private static final String STATE_SITE_MODE = "site_mode";
	private static final String NO_SHOW_SEARCH_TYPE = "noshow_search_sitetype";
	
	protected final static String[] TEMPLATE = 
	{
		"-list",//0
		"-type",
		"-newSiteInformation",
		"-newSiteFeatures",
		"-addRemoveFeature",
		"-addParticipant",
		"-removeParticipants",
		"-changeRoles",
		"-siteDeleteConfirm",
		"-publishUnpublish",
		"-newSiteConfirm",//10
		"-newSitePublishUnpublish",
		"-siteInfo-list",//12
		"-siteInfo-editInfo",
		"-siteInfo-editInfoConfirm",
		"-addRemoveFeatureConfirm",//15
		"-publishUnpublish-sendEmail",
		"-publishUnpublish-confirm",
		"-siteInfo-editAccess",
		"-addParticipant-sameRole",
		"-addParticipant-differentRole",//20
		"-addParticipant-notification",
		"-addParticipant-confirm",
		"-siteInfo-editAccess-globalAccess",
		"-siteInfo-editAccess-globalAccess-confirm",
		"-changeRoles-confirm",//25
		"-modifyENW",
		"-importSites",
		"-siteInfo-import",
		"-siteInfo-duplicate",
		"-sitemanage-search",//30
		"-sitemanage-list",//31
		"-sitemanage-participants",//32
		"-sitemanage-addParticipant",//33
		"-sitemanage-sameRole",//34
		"-sitemanage-differentRoles",//35
		"-newSiteCourse",//36
		"-newSiteCourseManual",//37
		"-sitemanage-editInfo",//38
		"-sitemanage-editAccess",//39
		"-sitemanage-siteDeleteConfirm",//40
		"-sitemanage-saveas",//41
		"-gradtoolsConfirm",//42
		"-siteInfo-editClass",//43
		"-siteInfo-addCourseConfirm",//44
		"-siteInfo-importMtrlMaster", //45 -- htripath for import material from a file
		"-siteInfo-importMtrlCopy", //46
		"-siteInfo-importMtrlCopyConfirm",
		"-siteInfo-importMtrlCopyConfirmMsg", //48
		"-siteInfo-group", //49
		"-siteInfo-groupedit", //50
		"-siteInfo-groupDeleteConfirm" //51
	};
	
	/** Name of state attribute for Site instance id  */
	private static final String STATE_SITE_INSTANCE_ID = "site.instance.id";
	
	/** Name of state attribute for Site Information  */
	private static final String STATE_SITE_INFO = "site.info";
	
	/** Name of state attribute for CHEF site type  */
	private static final String STATE_SITE_TYPE = "site-type";
	
	/** Name of state attribute for poissible site types */
	private static final String STATE_SITE_TYPES = "site_types";
	private static final String STATE_DEFAULT_SITE_TYPE = "default_site_type";
	private static final String STATE_PUBLIC_CHANGEABLE_SITE_TYPES = "changeable_site_types";
	private static final String STATE_PUBLIC_SITE_TYPES = "public_site_types";
	private static final String STATE_PRIVATE_SITE_TYPES = "private_site_types";
	private static final String STATE_DISABLE_JOINABLE_SITE_TYPE = "disable_joinable_site_types";
	
	//Names of state attributes corresponding to properties of a site
	private final static String PROP_SITE_CONTACT_EMAIL = "contact-email";
	private final static String PROP_SITE_CONTACT_NAME = "contact-name";
	private final static String PROP_SITE_TERM = "term";
	
	/** Name of the state attribute holding the site list column list is sorted by */
	private static final String SORTED_BY = "site.sorted.by";
	
	/** the list of criteria for sorting */
	private static final String SORTED_BY_TITLE = "title";
	private static final String SORTED_BY_DESCRIPTION = "description";
	private static final String SORTED_BY_TYPE = "type";
	private static final String SORTED_BY_OWNER = "owner";
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
		
	/** the list of View selection options **/
	private final static String ALL_MY_SITES = rb.getString("java.allmy");//"All My Sites";
	private final static String MYWORKSPACE = rb.getString("java.my");//"My Workspace";
   private final static String GRADTOOLS = rb.getString("java.gradtools");//"gradtools";
	
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
	private final static String NEWS_DEFAULT_TITLE = ServerConfigurationService.getString("news.title");
	private final static String NEWS_DEFAULT_URL = ServerConfigurationService.getString("news.feedURL");
	private final static String STATE_WEB_CONTENT_TITLES = "webcontenttitles";
	private final static String STATE_WEB_CONTENT_URLS = "wcUrls";
	private final static String WEB_CONTENT_DEFAULT_TITLE = "Web Content";
	private final static String WEB_CONTENT_DEFAULT_URL = "http://";
	private final static String STATE_SITE_QUEST_UNIQNAME = "site_quest_uniqname";
	
	// %%% get rid of the IdAndText tool lists and just use ToolConfiguration or ToolRegistration lists
	// %%% same for CourseItems
	
	// Names for other state attributes that are lists
	private final static String STATE_WORKSITE_SETUP_PAGE_LIST = "wSetupPageList"; // the list of site pages consistent with Worksite Setup page patterns
	
	/** The name of the state form field containing additional information for a course request */
	private static final String FORM_ADDITIONAL = "form.additional";
	
	/** %%% in transition from putting all form variables in state*/
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
	
	/** State attribute for state initialization.  */
	private static final String STATE_INITIALIZED = "site.initialized";
	
	/** The action for menu  */
	private static final String STATE_ACTION = "site.action";

	/** The user copyright string */
	private static final String	STATE_MY_COPYRIGHT = "resources.mycopyright";
	
	/** The copyright character */
	private static final String COPYRIGHT_SYMBOL = "copyright (c)";

	/** The null/empty string */
	private static final String NULL_STRING = "";
	
	/** The alert message shown when no site has been selected for the requested action. */
	private static final String NO_SITE_SELECTED_STRING = rb.getString("java.nosites");
	
	/** The alert message shown when Revise... has been clicked but more than one site was checked */
	private static final String MORE_THAN_ONE_SITE_SELECTED_STRING = rb.getString("java.please"); 
	
	/** The state attribute alerting user of a sent course request */
	private static final String REQUEST_SENT = "site.request.sent";
	
	/** The state attributes in the make public vm */
	private static final String STATE_JOINABLE = "state_joinable";
	private static final String STATE_JOINERROLE = "state_joinerRole";
	
	/** Invalid email address warning */
	private static final String INVALID_EMAIL = rb.getString("java.theemail");
	
	/** the list of selected user */
	private static final String STATE_SELECTED_USER_LIST = "state_selected_user_list";
	
	private static final String STATE_SELECTED_PARTICIPANT_ROLES = "state_selected_participant_roles";
	private static final String STATE_SELECTED_PARTICIPANTS = "state_selected_participants";
	private static final String STATE_PARTICIPANT_LIST = "state_participant_list";
	private static final String STATE_ADD_PARTICIPANTS = "state_add_participants";
	
	/** for changing participant roles*/
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
	
	/** for sitemanage tool */
	private final static String STATE_SEARCH_SITE_TYPE = "search_site_type";
	private final static String DEFAULT_SITE_SIZE_LIMIT = "1GB";
	private final static String STATE_SITE_SIZE_DEFAULT_SELECT = "default_size_selected";
	private final static String STATE_SITEMANAGE_SITETYPE = "sitemanage_siteinfo_type";
	private final static String SITE_TERM_ANY = "Any";
	private final static String STATE_TERM_SELECTION = "termSelection";
	private final static String STATE_PROP_SEARCH_MAP = "propertyCriteriaMap";
	private final static String SEARCH_TERM_SITE_TYPE = "termSearchSiteType";
	private final static String SEARCH_TERM_PROP = "termProp";
	
	/** for course information */
	private final static String STATE_TERM_COURSE_LIST = "state_term_course_list";
	private final static String STATE_TERM_SELECTED = "state_term_selected";
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
	
	//site template used to create a UM Grad Tools student site
	public static final String SITE_GTS_TEMPLATE = "!gtstudent";
	
	//the type used to identify a UM Grad Tools student site
	public static final String SITE_TYPE_GRADTOOLS_STUDENT = "GradToolsStudent";
	
	//list of UM Grad Tools site types for editing
	public static final String GRADTOOLS_SITE_TYPES = "gradtools_site_types";
	
	public static final String SITE_DUPLICATED = "site_duplicated";
	public static final String SITE_DUPLICATED_NAME = "site_duplicated_named";
	
	// used for site creation wizard title
	public static final String SITE_CREATE_TOTAL_STEPS = "site_create_total_steps";
	public static final String SITE_CREATE_CURRENT_STEP = "site_create_current_step";
	
	// types of site whose title can be editable
	public static final String TITLE_EDITABLE_SITE_TYPE = "title_editable_site_type";
	
	// types of site where site view roster permission is editable
	public static final String EDIT_VIEW_ROSTER_SITE_TYPE = "edit_view_roster_site_type";
	
	//htripath : for import material from file - classic import
	private static final String ALL_ZIP_IMPORT_SITES= "allzipImports";
	private static final String FINAL_ZIP_IMPORT_SITES= "finalzipImports";
	private static final String DIRECT_ZIP_IMPORT_SITES= "directzipImports";
	private static final String CLASSIC_ZIP_FILE_NAME="classicZipFileName" ;
	private static final String SESSION_CONTEXT_ID="sessionContextId";
  
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
  
	/**
	* Populate the state object, if needed.
	*/
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);
		
		PortletConfig config = portlet.getPortletConfig();
		
		// types of sites that can either be public or private
		String changeableTypes = StringUtil.trimToNull(config.getInitParameter("publicChangeableSiteTypes"));
		if (state.getAttribute(STATE_PUBLIC_CHANGEABLE_SITE_TYPES) == null)
		{
			if (changeableTypes != null)
			{
				state.setAttribute(STATE_PUBLIC_CHANGEABLE_SITE_TYPES, new ArrayList(Arrays.asList(changeableTypes.split(","))));
			}
			else
			{
				state.setAttribute(STATE_PUBLIC_CHANGEABLE_SITE_TYPES, new Vector());
			}
		}
		
		// type of sites that are always public
		String publicTypes = StringUtil.trimToNull(config.getInitParameter("publicSiteTypes"));
		if (state.getAttribute(STATE_PUBLIC_SITE_TYPES) == null)
		{
			if (changeableTypes != null)
			{
				state.setAttribute(STATE_PUBLIC_SITE_TYPES, new ArrayList(Arrays.asList(publicTypes.split(","))));
			}
			else
			{
				state.setAttribute(STATE_PUBLIC_SITE_TYPES, new Vector());
			}
		}
		
		// types of sites that are always private
		String privateTypes = StringUtil.trimToNull(config.getInitParameter("privateSiteTypes"));
		if (state.getAttribute(STATE_PRIVATE_SITE_TYPES) == null)
		{
			if (privateTypes != null)
			{
				state.setAttribute(STATE_PRIVATE_SITE_TYPES, new ArrayList(Arrays.asList(privateTypes.split(","))));
			}
			else
			{
				state.setAttribute(STATE_PRIVATE_SITE_TYPES, new Vector());
			}
		}
		
		// default site type
		String defaultType = StringUtil.trimToNull(config.getInitParameter("defaultSiteType"));
		if (state.getAttribute(STATE_DEFAULT_SITE_TYPE) == null)
		{
			if (defaultType != null)
			{
				state.setAttribute(STATE_DEFAULT_SITE_TYPE, defaultType);
			}
			else
			{
				state.setAttribute(STATE_PRIVATE_SITE_TYPES, new Vector());
			}
		}
		
		// certain type(s) of site cannot get its "joinable" option set
		if (state.getAttribute(STATE_DISABLE_JOINABLE_SITE_TYPE) == null)
		{
			if (ServerConfigurationService.getStrings("wsetup.disable.joinable") != null)
			{
				state.setAttribute(STATE_DISABLE_JOINABLE_SITE_TYPE, new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("wsetup.disable.joinable"))));
			}
			else
			{
				state.setAttribute(STATE_DISABLE_JOINABLE_SITE_TYPE, new Vector());
			}
		}
		
		if (state.getAttribute(STATE_TOP_PAGE_MESSAGE) == null)
		{
			state.setAttribute(STATE_TOP_PAGE_MESSAGE, new Integer(0));
		}
		
		// affiliates if any
		if (state.getAttribute(STATE_SUBJECT_AFFILIATES) == null)
		{
			setupSubjectAffiliates(state);
		}
		
		//skins if any
		if (state.getAttribute(STATE_ICONS) == null)
		{
			setupIcons(state);
		}
		
		if (state.getAttribute(GRADTOOLS_SITE_TYPES) == null)
		{
			List gradToolsSiteTypes = new Vector();
			if (ServerConfigurationService.getStrings("gradToolsSiteType") != null)
			{
				gradToolsSiteTypes = new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("gradToolsSiteType")));
			}
			state.setAttribute(GRADTOOLS_SITE_TYPES, gradToolsSiteTypes);
		}
		
		if (ServerConfigurationService.getStrings("titleEditableSiteType") != null)
		{
			state.setAttribute(TITLE_EDITABLE_SITE_TYPE, new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("titleEditableSiteType"))));
		}
		else
		{
			state.setAttribute(TITLE_EDITABLE_SITE_TYPE, new Vector());
		}
		
		if (state.getAttribute(EDIT_VIEW_ROSTER_SITE_TYPE) == null)
		{
			List siteTypes = new Vector();
			if (ServerConfigurationService.getStrings("editViewRosterSiteType") != null)
			{
				siteTypes = new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("editViewRosterSiteType")));
			}
			state.setAttribute(EDIT_VIEW_ROSTER_SITE_TYPE, siteTypes);
		}
		
		//get site tool mode from tool registry
		String site_mode = portlet.getPortletConfig().getInitParameter(STATE_SITE_MODE);
		state.setAttribute(STATE_SITE_MODE, site_mode);

	}   // initState
	
	/**
	* cleanState removes the current site instance and it's properties from state
	*/
	private void cleanState(SessionState state)
	{
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
		state.removeAttribute(STATE_TERM_SELECTED);
		state.removeAttribute(STATE_FUTURE_TERM_SELECTED);
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER);
		state.removeAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		state.removeAttribute(STATE_ADD_CLASS_MANUAL);
		state.removeAttribute(STATE_AUTO_ADD);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_NUMBER);
		state.removeAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
		state.removeAttribute(SITE_CREATE_TOTAL_STEPS);
		state.removeAttribute(SITE_CREATE_CURRENT_STEP);
	}	// cleanState
	
	/**
	* Fire up the permissions editor
	*/
	public void doPermissions(RunData data, Context context)
	{
		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.permissions.helper");

		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		String contextString = ToolManager.getCurrentPlacement().getContext();
		String siteRef = SiteService.siteReference(contextString);
		
		// if it is in Worksite setup tool, pass the selected site's reference
		if (state.getAttribute(STATE_SITE_MODE) != null && ((String) state.getAttribute(STATE_SITE_MODE)).equals(SITE_MODE_SITESETUP))
		{
			if (state.getAttribute(STATE_SITE_INSTANCE_ID) != null)
			{
				Site s = getStateSite(state);
				if (s != null)
				{
					siteRef = s.getReference();
				}
			}
		}

		// setup for editing the permissions of the site for this tool, using the roles of this site, too
		state.setAttribute(PermissionsHelper.TARGET_REF, siteRef);

		// ... with this description
		state.setAttribute(PermissionsHelper.DESCRIPTION, rb.getString("setperfor") + " "
				+ SiteService.getSiteDisplay(contextString));

		// ... showing only locks that are prpefixed with this
		state.setAttribute(PermissionsHelper.PREFIX, "site.");

	}	// doPermissions
	
	/**
	* Build the context for normal display
	*/
	public String buildMainPanelContext (	VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		context.put("tlang",rb);
		// TODO: what is all this doing? if we are in helper mode, we are already setup and don't get called here now -ggolden
		/*
		String helperMode = (String) state.getAttribute(PermissionsAction.STATE_MODE);
		if (helperMode != null)
		{
			Site site = getStateSite(state);
			if (site != null)
			{
				if (site.getType() != null && ((List) state.getAttribute(EDIT_VIEW_ROSTER_SITE_TYPE)).contains(site.getType()))
				{
					context.put("editViewRoster", Boolean.TRUE);
				}
				else
				{
					context.put("editViewRoster", Boolean.FALSE);
				}
			}
			else
			{
				context.put("editViewRoster", Boolean.FALSE);
			}
			
			// for new, don't show site.del in Permission page
			context.put("hiddenLock", "site.del");
			
			String template = PermissionsAction.buildHelperContext(portlet, context, data, state);
			if (template == null)
			{
				addAlert(state, rb.getString("theisa"));
			}
			else
			{
				return template;
			}
		}
		*/

		String template = null;
		context.put ("action", CONTEXT_ACTION);
		
		//updatePortlet(state, portlet, data);
		if (state.getAttribute (STATE_INITIALIZED) == null)
		{
			init (portlet, data, state);
		}
		int index = Integer.valueOf((String)state.getAttribute(STATE_TEMPLATE_INDEX)).intValue();
		template = buildContextForTemplate(index, portlet, context, data, state);
		return template;
		
	} // buildMainPanelContext
	
	/**
	* Build the context for each template using template_index parameter passed in a form hidden field.
	* Each case is associated with a template. (Not all templates implemented). See String[] TEMPLATES.
	* @param index is the number contained in the template's template_index 
	*/
	
	private String buildContextForTemplate (int index, VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{			
		String realmId = "";
		String site_type = "";
		String sortedBy = "";
		String sortedAsc = "";
		ParameterParser params = data.getParameters ();
		context.put("tlang",rb);
		context.put("alertMessage", state.getAttribute(STATE_MESSAGE));
		
		// If cleanState() has removed SiteInfo, get a new instance into state
		SiteInfo siteInfo = new SiteInfo();
		if (state.getAttribute(STATE_SITE_INFO) != null)
		{
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);	
		}
		else
		{
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
		if (state.getAttribute(SITE_CREATE_TOTAL_STEPS) != null)
		{
			context.put("totalSteps", state.getAttribute(SITE_CREATE_TOTAL_STEPS));
		}
		if (state.getAttribute(SITE_CREATE_CURRENT_STEP) != null)
		{
			context.put("step", state.getAttribute(SITE_CREATE_CURRENT_STEP));
		}
		
		String hasGradSites = ServerConfigurationService.getString("withDissertation", Boolean.FALSE.toString());
		
		Site site = getStateSite(state);
		
		switch (index)
		{
			case 0: 
				/*  buildContextForTemplate chef_site-list.vm
				*
				*/
				// site types
				List sTypes = (List) state.getAttribute(STATE_SITE_TYPES);
				
				//make sure auto-updates are enabled
				Hashtable views = new Hashtable();
				if (SecurityService.isSuperUser())
				{
					views.put(ALL_MY_SITES, ALL_MY_SITES);
					views.put(MYWORKSPACE + " sites", MYWORKSPACE);
					for(int sTypeIndex = 0; sTypeIndex < sTypes.size(); sTypeIndex++)
					{
						String type = (String) sTypes.get(sTypeIndex);
						views.put(type + " sites", type);
					}
					if (hasGradSites.equalsIgnoreCase("true"))
					{
						views.put(GRADTOOLS + " sites", GRADTOOLS);
					}
					if(state.getAttribute(STATE_VIEW_SELECTED) == null)
					{
						state.setAttribute(STATE_VIEW_SELECTED, ALL_MY_SITES);
					}
					context.put("superUser", Boolean.TRUE);
				}
				else
				{
					context.put("superUser", Boolean.FALSE);
					views.put(ALL_MY_SITES, ALL_MY_SITES);
					
					// if there is a GradToolsStudent choice inside
					boolean remove = false;
					if (hasGradSites.equalsIgnoreCase("true"))
					{
						try
						{
							//the Grad Tools site option is only presented to GradTools Candidates
							String userId = StringUtil.trimToZero(SessionManager.getCurrentSessionUserId());
							
							//am I a grad student?
							if (!isGradToolsCandidate(userId))
							{
								// not a gradstudent
								remove = true;
							}
						}
						catch(Exception e)
						{
							remove = true;
						}
					}
					else
					{
						// not support for dissertation sites
						remove=true;
					}
					//do not show this site type in views
					//sTypes.remove(new String(SITE_TYPE_GRADTOOLS_STUDENT));
					
					for(int sTypeIndex = 0; sTypeIndex < sTypes.size(); sTypeIndex++)
					{
						String type = (String) sTypes.get(sTypeIndex);
						if(!type.equals(SITE_TYPE_GRADTOOLS_STUDENT))
						{
							views.put(type + " "+rb.getString("java.sites"), type);
						}
					}
					if (!remove)
					{
						views.put(GRADTOOLS + " sites", GRADTOOLS);
					}
					
					//default view
					if(state.getAttribute(STATE_VIEW_SELECTED) == null)
					{
						state.setAttribute(STATE_VIEW_SELECTED, ALL_MY_SITES);
					}
				}
				context.put("views", views);
				
				if(state.getAttribute(STATE_VIEW_SELECTED) != null)
				{
					context.put("viewSelected", (String) state.getAttribute(STATE_VIEW_SELECTED));
				}
				
				String search = (String) state.getAttribute(STATE_SEARCH);
				context.put("search_term", search);
					
				sortedBy = (String) state.getAttribute (SORTED_BY);
				if (sortedBy == null)
				{
					state.setAttribute(SORTED_BY, SortType.TITLE_ASC.toString());
					sortedBy = SortType.TITLE_ASC.toString();
				}

				sortedAsc = (String) state.getAttribute (SORTED_ASC);
				if (sortedAsc == null)
				{
					state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());
					sortedAsc = Boolean.TRUE.toString();
				}
				if(sortedBy!=null) context.put ("currentSortedBy", sortedBy);
				if(sortedAsc!=null) context.put ("currentSortAsc", sortedAsc);

				String portalUrl = ServerConfigurationService.getPortalUrl();
				context.put("portalUrl", portalUrl);
				
				List sites = prepPage(state);
				state.setAttribute(STATE_SITES, sites);
				context.put("sites", sites);

				context.put("totalPageNumber", new Integer(totalPageNumber(state)));
				context.put("searchString", state.getAttribute(STATE_SEARCH));
				context.put("form_search", FORM_SEARCH);
				context.put("formPageNumber", FORM_PAGE_NUMBER);
				context.put("prev_page_exists", state.getAttribute(STATE_PREV_PAGE_EXISTS));
				context.put("next_page_exists", state.getAttribute(STATE_NEXT_PAGE_EXISTS));
				context.put("current_page", state.getAttribute(STATE_CURRENT_PAGE));
				
				// put the service in the context (used for allow update calls on each site)
				context.put("service", SiteService.getInstance());
				context.put("sortby_title", SortType.TITLE_ASC.toString());
				context.put("sortby_type", SortType.TYPE_ASC.toString());
				context.put("sortby_createdby", SortType.CREATED_BY_ASC.toString());
				context.put("sortby_publish", SortType.PUBLISHED_ASC.toString());
				context.put("sortby_createdon", SortType.CREATED_ON_ASC.toString());
				
				// top menu bar
				Menu bar = new MenuImpl(portlet, data, (String) state.getAttribute(STATE_ACTION));
				if (SiteService.allowAddSite(null))
				{
					bar.add( new MenuEntry(rb.getString("java.new"), "doNew_site"));
				}
				bar.add( new MenuEntry(rb.getString("java.revise"), null, true, MenuItem.CHECKED_NA, "doGet_site", "sitesForm"));
				bar.add( new MenuEntry(rb.getString("java.delete"), null, true, MenuItem.CHECKED_NA, "doMenu_site_delete",  "sitesForm"));
				context.put("menu", bar);
				// default to be no pageing
				context.put("paged", Boolean.FALSE);
				
				Menu bar2 = new MenuImpl(portlet, data, (String) state.getAttribute(STATE_ACTION));
				
				// add the search commands
				addSearchMenus(bar2, state);
				context.put("menu2", bar2);
				
				pagingInfoToContext(state, context);
				return (String)getContext(data).get("template") + TEMPLATE[0];
			case 1: 
				/*  buildContextForTemplate chef_site-type.vm
				*	
				*/
				if (hasGradSites.equalsIgnoreCase("true"))
				{
					context.put("withDissertation", Boolean.TRUE);
					try
					{
						//the Grad Tools site option is only presented to UM grad students
						String userId = StringUtil.trimToZero(SessionManager.getCurrentSessionUserId());
						
						//am I a UM grad student?
						Boolean isGradStudent = new Boolean(isGradToolsCandidate(userId));
						context.put("isGradStudent", isGradStudent);
					
						//if I am a UM grad student, do I already have a Grad Tools site?
						boolean noGradToolsSite = true;
						if(hasGradToolsStudentSite(userId))
							noGradToolsSite = false;
						context.put("noGradToolsSite", new Boolean(noGradToolsSite));
					}
					catch(Exception e)
					{
						if(Log.isWarnEnabled())
						{
							M_log.warn("buildContextForTemplate chef_site-type.vm " + e);
						}
					}
				}
				else
				{
					context.put("withDissertation", Boolean.FALSE);
				}
				
				List types = (List) state.getAttribute(STATE_SITE_TYPES);
				context.put("siteTypes", types);
				
				// put selected/default site type into context
				if (siteInfo.site_type != null && siteInfo.site_type.length() >0)
				{
					context.put("typeSelected", siteInfo.site_type);
				}
				else if (types.size() > 0)
				{
					context.put("typeSelected", types.get(0));
				}
				List terms = CourseManagementService.getTerms();
				List termsForSiteCreation = new Vector();
				if (terms != null && terms.size() >0)
				{
					for (int i=0; i<terms.size();i++)
					{
						Term t = (Term) terms.get(i);
						if (!t.getEndTime().before(TimeService.newTime()))
						{
							// don't show those terms which have ended already
							termsForSiteCreation.add(t);
						}
					}
				}
				if (termsForSiteCreation.size() > 0)
				{
					context.put("termList", termsForSiteCreation);
				}
				if (state.getAttribute(STATE_TERM_SELECTED) != null)
				{
					context.put("selectedTerm", state.getAttribute(STATE_TERM_SELECTED));
				}
				return (String)getContext(data).get("template") + TEMPLATE[1];
			case 2: 
				/*   buildContextForTemplate chef_site-newSiteInformation.vm 
				* 
				*/
				context.put("siteTypes", state.getAttribute(STATE_SITE_TYPES));
				String siteType = (String) state.getAttribute(STATE_SITE_TYPE);
				
				context.put("titleEditableSiteType", state.getAttribute(TITLE_EDITABLE_SITE_TYPE));
				context.put("type", siteType);
				
				if (siteType.equalsIgnoreCase("course"))
				{
					context.put ("isCourseSite", Boolean.TRUE);
					context.put("isProjectSite", Boolean.FALSE);
					
					if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null)
					{
						context.put ("selectedProviderCourse", state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN));
					}
					if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null)
					{
						int number = ((Integer) state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER)).intValue();
						context.put ("manualAddNumber", new Integer(number - 1));
						context.put ("manualAddFields", state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
						context.put("back", "37");
					}
					else
					{
						context.put("back", "36");
					}
					
					context.put ("skins", state.getAttribute(STATE_ICONS));
					if (StringUtil.trimToNull(siteInfo.getIconUrl()) != null)
					{
						context.put("selectedIcon", siteInfo.getIconUrl());
					}
				}
				else
				{
					context.put ("isCourseSite", Boolean.FALSE);
					if (siteType.equalsIgnoreCase("project"))
					{
						context.put("isProjectSite", Boolean.TRUE);
					}
					
					if (StringUtil.trimToNull(siteInfo.iconUrl) != null)
					{
						context.put(FORM_ICON_URL, siteInfo.iconUrl);
					}
					
					context.put ("back", "1");
				}
				
				context.put (FORM_TITLE,siteInfo.title);
				context.put(FORM_SHORT_DESCRIPTION, siteInfo.short_description);
				context.put (FORM_DESCRIPTION,siteInfo.description);
				
				// defalt the site contact person to the site creator
				if (siteInfo.site_contact_name.equals(NULL_STRING) && siteInfo.site_contact_email.equals(NULL_STRING))
				{
					User user = UserDirectoryService.getCurrentUser();
					siteInfo.site_contact_name = user.getDisplayName();
					siteInfo.site_contact_email = user.getEmail();
				}
				context.put("form_site_contact_name", siteInfo.site_contact_name);
				context.put("form_site_contact_email", siteInfo.site_contact_email);
				
				// those manual inputs
				context.put("form_requiredFields", CourseManagementService.getCourseIdRequiredFields());
				context.put("fieldValues", state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
				
				return (String)getContext(data).get("template") + TEMPLATE[2];
			case 3: 
				/*   buildContextForTemplate chef_site-newSiteFeatures.vm
				* 
				*/
				siteType = (String) state.getAttribute(STATE_SITE_TYPE);
				if (siteType!=null && siteType.equalsIgnoreCase("course"))
				{
					context.put ("isCourseSite", Boolean.TRUE);
					context.put("isProjectSite", Boolean.FALSE);
				}
				else
				{
					context.put ("isCourseSite", Boolean.FALSE);
					if (siteType.equalsIgnoreCase("project"))
					{
						context.put("isProjectSite", Boolean.TRUE);
					}
				}
				context.put("defaultTools", ServerConfigurationService.getToolsRequired(siteType));

				toolRegistrationSelectedList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);

				// If this is the first time through, check for tools
				// which should be selected by default.
				List defaultSelectedTools = ServerConfigurationService.getDefaultTools(siteType);
				if (toolRegistrationSelectedList == null) {
					toolRegistrationSelectedList = new Vector(defaultSelectedTools);
				}

				context.put (STATE_TOOL_REGISTRATION_SELECTED_LIST, toolRegistrationSelectedList); // String toolId's
				context.put (STATE_TOOL_REGISTRATION_LIST, state.getAttribute(STATE_TOOL_REGISTRATION_LIST) ); // %%% use ToolRegistrations for template list
				context.put("emailId", state.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
				context.put("serverName", ServerConfigurationService.getServerName());

				// The "Home" tool checkbox needs special treatment to be selected by
				// default.
				Boolean checkHome = (Boolean)state.getAttribute(STATE_TOOL_HOME_SELECTED);
				if (checkHome == null) {
					if ((defaultSelectedTools != null) && defaultSelectedTools.contains("home")) {
						checkHome = Boolean.TRUE;
					}
				}
				context.put("check_home", checkHome);

				//titles for news tools
				context.put("newsTitles", state.getAttribute(STATE_NEWS_TITLES));
				//titles for web content tools
				context.put("wcTitles", state.getAttribute(STATE_WEB_CONTENT_TITLES));
				//urls for news tools
				context.put("newsUrls", state.getAttribute(STATE_NEWS_URLS));
				//urls for web content tools
				context.put("wcUrls", state.getAttribute(STATE_WEB_CONTENT_URLS));
				context.put("sites", SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.UPDATE, null, null, null, SortType.TITLE_ASC, null));
				context.put("import", state.getAttribute(STATE_IMPORT));
				context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));
				return (String)getContext(data).get("template") + TEMPLATE[3];
			case 4: 
				/*  buildContextForTemplate chef_site-addRemoveFeatures.vm 
				*
				*/
				context.put("SiteTitle", site.getTitle());
				String type = (String) state.getAttribute(STATE_SITE_TYPE);
				context.put("defaultTools", ServerConfigurationService.getToolsRequired(type));
				
				boolean myworkspace_site = false;
				//Put up tool lists filtered by category
				List siteTypes = (List) state.getAttribute(STATE_SITE_TYPES);
				if (siteTypes.contains(type))
				{
					myworkspace_site = false;
				}
				
				if (SiteService.isUserSite(site.getId()) || (type!=null && type.equalsIgnoreCase("myworkspace")))
				{
					myworkspace_site = true;
					type="myworkspace";
				}
				
				context.put ("myworkspace_site", new Boolean(myworkspace_site));
				
				context.put(STATE_TOOL_REGISTRATION_SELECTED_LIST, state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST));
				
				//titles for news tools
				context.put("newsTitles", state.getAttribute(STATE_NEWS_TITLES));
				//titles for web content tools
				context.put("wcTitles", state.getAttribute(STATE_WEB_CONTENT_TITLES));
				//urls for news tools
				context.put("newsUrls", state.getAttribute(STATE_NEWS_URLS));
				//urls for web content tools
				context.put("wcUrls", state.getAttribute(STATE_WEB_CONTENT_URLS));
				
				context.put (STATE_TOOL_REGISTRATION_LIST, state.getAttribute(STATE_TOOL_REGISTRATION_LIST));
				context.put("check_home", state.getAttribute(STATE_TOOL_HOME_SELECTED));
	
				//get the email alias when an Email Archive tool has been selected
				String channelReference = mailArchiveChannelReference(site.getId());
				List aliases = AliasService.getAliases(channelReference, 1, 1);
				if (aliases.size() > 0)
				{
					state.setAttribute(STATE_TOOL_EMAIL_ADDRESS, ((Alias) aliases.get(0)).getId());
				}
				else
				{
					state.removeAttribute(STATE_TOOL_EMAIL_ADDRESS);
				}
				if (state.getAttribute(STATE_TOOL_EMAIL_ADDRESS) != null)
				{	
					context.put("emailId", state.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
				}
				context.put("serverName", ServerConfigurationService.getServerName());
				
				context.put("backIndex", "12");
							
				return (String)getContext(data).get("template") + TEMPLATE[4];
			case 5: 
				/*  buildContextForTemplate chef_site-addParticipant.vm 
				* 
				*/
				context.put("title", site.getTitle());
				roles = getRoles(state);
				context.put("roles", roles);
				
            // Note that (for now) these strings are in both sakai.properties and sitesetupgeneric.properties
				context.put("noEmailInIdAccountName", ServerConfigurationService.getString("noEmailInIdAccountName"));
				context.put("noEmailInIdAccountLabel", ServerConfigurationService.getString("noEmailInIdAccountLabel"));
				context.put("emailInIdAccountName", ServerConfigurationService.getString("emailInIdAccountName"));
				context.put("emailInIdAccountLabel", ServerConfigurationService.getString("emailInIdAccountLabel"));
				
				if(state.getAttribute("noEmailInIdAccountValue")!=null)
				{
					context.put("noEmailInIdAccountValue", (String)state.getAttribute("noEmailInIdAccountValue"));
				}
				if(state.getAttribute("emailInIdAccountValue")!=null)
				{
					context.put("emailInIdAccountValue", (String)state.getAttribute("emailInIdAccountValue"));
				}
				
				if(state.getAttribute("form_same_role") != null)
				{
					context.put("form_same_role", ((Boolean) state.getAttribute("form_same_role")).toString());
				}
				else
				{
					context.put("form_same_role", Boolean.TRUE.toString());
				}
				context.put("backIndex", "12");
				return (String)getContext(data).get("template") + TEMPLATE[5];
			case 6: 
				/*  buildContextForTemplate chef_site-removeParticipants.vm 
				* 
				*/
				context.put("title", site.getTitle());
				realmId = SiteService.siteReference(site.getId());
				try
				{
					AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
					try
					{
						List removeableList = (List) state.getAttribute(STATE_REMOVEABLE_USER_LIST);
						List removeableParticipants = new Vector();
						
						for (int k = 0; k < removeableList.size(); k++)
						{
							User user = UserDirectoryService.getUser((String) removeableList.get(k));
							Participant participant = new Participant();
							participant.name = user.getSortName();
							participant.uniqname = user.getId();
							Role r = realm.getUserRole(user.getId());
							if (r != null)
							{
								participant.role = r.getId();
							}
							removeableParticipants.add(participant);
						}
						context.put("removeableList", removeableParticipants);
					}
					catch (UserNotDefinedException ee)
					{
					}
				}
				catch (GroupNotDefinedException e)
				{
				}
				
				context.put("backIndex", "18");
				return (String)getContext(data).get("template") + TEMPLATE[6];
			case 7: 
				/*  buildContextForTemplate chef_site-changeRoles.vm
				* 
				*/
				context.put("same_role", state.getAttribute(STATE_CHANGEROLE_SAMEROLE));
				roles = getRoles(state);
				context.put("roles", roles);
				context.put("currentRole", state.getAttribute(STATE_CHANGEROLE_SAMEROLE_ROLE));
				context.put("participantSelectedList", state.getAttribute(STATE_SELECTED_PARTICIPANTS));
				context.put("selectedRoles", state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES));
				context.put("siteTitle", site.getTitle());
				return (String)getContext(data).get("template") + TEMPLATE[7];
			case 8: 
				/*  buildContextForTemplate chef_site-siteDeleteConfirm.vm
				* 
				*/
				String site_title = NULL_STRING;
				String[] removals = (String[]) state.getAttribute(STATE_SITE_REMOVALS);
				List remove = new Vector();
				String user = SessionManager.getCurrentSessionUserId();
				String workspace = SiteService.getUserSiteId(user);
				if( removals != null && removals.length != 0 )
				{
					for (int i = 0; i < removals.length; i++ )
					{
						String id = (String) removals[i];
						if(!(id.equals(workspace)))
						{
							try
							{
								site_title = SiteService.getSite(id).getTitle();
							}
							catch (IdUnusedException e)
							{
								M_log.warn("SiteAction.doSite_delete_confirmed - IdUnusedException " + id);
								addAlert(state, rb.getString("java.sitewith")+" " + id + " "+rb.getString("java.couldnt")+" ");
							}
							if(SiteService.allowRemoveSite(id))
							{
								try
								{
									Site removeSite = SiteService.getSite(id);
									remove.add(removeSite);
								}
								catch (IdUnusedException e)
								{
									M_log.warn("SiteAction.buildContextForTemplate chef_site-siteDeleteConfirm.vm: IdUnusedException");	
								}
							}
							else
							{
								addAlert(state, site_title + " "+rb.getString("java.couldntdel") + " ");
							}
						}
						else
						{
							addAlert(state, rb.getString("java.yourwork"));
						}
					}
					if(remove.size() == 0)
					{
						addAlert(state, rb.getString("java.click")); 
					}
				}
				context.put("removals", remove);
				return (String)getContext(data).get("template") + TEMPLATE[8];
			case 9:
				/*  buildContextForTemplate chef_site-publishUnpublish.vm
				* 
				*/
				context.put("publish", Boolean.valueOf(((SiteInfo)state.getAttribute(STATE_SITE_INFO)).getPublished()));
				context.put("backIndex", "12");
				return (String)getContext(data).get("template") + TEMPLATE[9];
			case 10:
				/*  buildContextForTemplate chef_site-newSiteConfirm.vm
				* 
				*/
				siteInfo = (SiteInfo)state.getAttribute(STATE_SITE_INFO);
				siteType = (String) state.getAttribute(STATE_SITE_TYPE);
				if (siteType.equalsIgnoreCase("course"))
				{
					context.put ("isCourseSite", Boolean.TRUE);
					context.put ("isProjectSite", Boolean.FALSE);
					if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null)
					{
						context.put ("selectedProviderCourse", state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN));
					}
					if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null)
					{
						int number = ((Integer) state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER)).intValue();
						context.put ("manualAddNumber", new Integer(number - 1));
						context.put ("manualAddFields", state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
					}
					
					context.put ("skins", state.getAttribute(STATE_ICONS));
					if (StringUtil.trimToNull(siteInfo.getIconUrl()) != null)
					{
						context.put("selectedIcon", siteInfo.getIconUrl());
					} 
				}
				else
				{
					context.put ("isCourseSite", Boolean.FALSE);
					if (siteType!=null && siteType.equalsIgnoreCase("project"))
					{
						context.put("isProjectSite", Boolean.TRUE);
					}
					
					if (StringUtil.trimToNull(siteInfo.iconUrl) != null)
					{
						context.put("iconUrl", siteInfo.iconUrl);
					}
				}
				context.put("title", siteInfo.title);
				context.put("description", siteInfo.description);
				context.put("short_description", siteInfo.short_description);
				context.put("siteContactName", siteInfo.site_contact_name);
				context.put("siteContactEmail", siteInfo.site_contact_email);
				siteType = (String) state.getAttribute(STATE_SITE_TYPE);
				toolRegistrationSelectedList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
				context.put (STATE_TOOL_REGISTRATION_SELECTED_LIST, toolRegistrationSelectedList); // String toolId's
				context.put (STATE_TOOL_REGISTRATION_LIST, state.getAttribute(STATE_TOOL_REGISTRATION_LIST)); // %%% use Tool
				context.put("check_home", state.getAttribute(STATE_TOOL_HOME_SELECTED));
				context.put("emailId", state.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
				context.put("serverName", ServerConfigurationService.getServerName());
				context.put("include", new Boolean(siteInfo.include));
				context.put("published", new Boolean(siteInfo.published));
				context.put("joinable", new Boolean(siteInfo.joinable));
				context.put("joinerRole", siteInfo.joinerRole);
				context.put("newsTitles", (Hashtable) state.getAttribute(STATE_NEWS_TITLES));
				context.put("wcTitles", (Hashtable) state.getAttribute(STATE_WEB_CONTENT_TITLES));
				
				// back to edit access page
				context.put("back", "18");
				
				context.put("importSiteTools", state.getAttribute(STATE_IMPORT_SITE_TOOL));
				context.put("siteService", SiteService.getInstance());
				
				// those manual inputs
				context.put("form_requiredFields", CourseManagementService.getCourseIdRequiredFields());
				context.put("fieldValues", state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));

				return (String)getContext(data).get("template") + TEMPLATE[10];
			case 11:
				/*  buildContextForTemplate chef_site-newSitePublishUnpublish.vm
				* 
				*/
				return (String)getContext(data).get("template") + TEMPLATE[11];	
			case 12:
				/*  buildContextForTemplate chef_site-siteInfo-list.vm
				* 
				*/
				try
				{
					siteProperties = site.getProperties();
					siteType = site.getType();
					if (siteType != null)
					{
						state.setAttribute(STATE_SITE_TYPE, siteType);
					}
					
					boolean isMyWorkspace = false;
					if (SiteService.isUserSite(site.getId()))
					{
						if (SiteService.getSiteUserId(site.getId()).equals(SessionManager.getCurrentSessionUserId()))
						{
							isMyWorkspace = true;
							context.put("siteUserId", SiteService.getSiteUserId(site.getId()));
						}
					}
					context.put("isMyWorkspace", Boolean.valueOf(isMyWorkspace));
					
					String siteId = site.getId();
					if (state.getAttribute(STATE_ICONS)!= null)
					{
						List skins = (List)state.getAttribute(STATE_ICONS);
						for (int i = 0; i < skins.size(); i++)
						{
							Icon s = (Icon)skins.get(i);
							if(!StringUtil.different(s.getUrl(), site.getIconUrl()))
							{
								context.put("siteUnit", s.getName());
								break;
							}
						}
					}
					context.put("siteIcon", site.getIconUrl());
					context.put("siteTitle", site.getTitle());
					context.put("siteDescription", site.getDescription());
					context.put("siteJoinable", new Boolean(site.isJoinable()));
					
					if(site.isPublished())
					{
						context.put("published", Boolean.TRUE);
					}
					else
					{
						context.put("published", Boolean.FALSE);
						context.put("owner", site.getCreatedBy().getSortName());
					}
					Time creationTime = site.getCreatedTime();
					if (creationTime != null)
					{
						context.put("siteCreationDate", creationTime.toStringLocalFull());
					}
					boolean allowUpdateSite = SiteService.allowUpdateSite(siteId);
					context.put("allowUpdate", Boolean.valueOf(allowUpdateSite));
					
					boolean allowUpdateGroupMembership = SiteService.allowUpdateGroupMembership(siteId);
					context.put("allowUpdateGroupMembership", Boolean.valueOf(allowUpdateGroupMembership));
					
					boolean allowUpdateSiteMembership = SiteService.allowUpdateSiteMembership(siteId);
					context.put("allowUpdateSiteMembership", Boolean.valueOf(allowUpdateSiteMembership));
					
					if (allowUpdateSite)
					{	
						// top menu bar
						Menu b = new MenuImpl(portlet, data, (String) state.getAttribute(STATE_ACTION));
						
						if (!isMyWorkspace)
						{
							b.add( new MenuEntry(rb.getString("java.editsite"), "doMenu_edit_site_info"));
						}
						b.add( new MenuEntry(rb.getString("java.edittools"), "doMenu_edit_site_tools"));
						
						if (!isMyWorkspace
								&& (ServerConfigurationService.getString("wsetup.group.support") == "" 
									|| ServerConfigurationService.getString("wsetup.group.support").equalsIgnoreCase(Boolean.TRUE.toString())))
						{
							// show the group toolbar unless configured to not support group
							b.add( new MenuEntry(rb.getString("java.group"), "doMenu_group"));
						}
						if (!isMyWorkspace)
						{
							List gradToolsSiteTypes = (List) state.getAttribute(GRADTOOLS_SITE_TYPES);
							boolean isGradToolSite = false;
							if (siteType != null && gradToolsSiteTypes.contains(siteType))
							{
								isGradToolSite = true;
							}
							if (siteType == null 
								|| siteType != null && !isGradToolSite)
							{
								// hide site access for GRADTOOLS type of sites
								b.add( new MenuEntry(rb.getString("java.siteaccess"), "doMenu_edit_site_access"));
							}
							b.add( new MenuEntry(rb.getString("java.addp"), "doMenu_siteInfo_addParticipant"));
							if (siteType != null && siteType.equals("course"))
							{
								b.add( new MenuEntry(rb.getString("java.editc"), "doMenu_siteInfo_editClass"));
							}
							if (siteType == null || siteType != null && !isGradToolSite)
							{
								// hide site duplicate and import for GRADTOOLS type of sites
								b.add( new MenuEntry(rb.getString("java.duplicate"), "doMenu_siteInfo_duplicate"));
							
								List updatableSites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.UPDATE, null, null, null, SortType.TITLE_ASC, null);
								
                                  // import link should be visible even if only one site 
                                  if (updatableSites.size() > 0)
								{
									b.add( new MenuEntry(rb.getString("java.import"), "doMenu_siteInfo_import"));
									
									// a configuration param for showing/hiding import from file choice
									String importFromFile = ServerConfigurationService.getString("site.setup.import.file", Boolean.TRUE.toString());
									if (importFromFile.equalsIgnoreCase("true"))
									{
										//htripath: June 4th added as per Kris and changed desc of above
										b.add(new MenuEntry(rb.getString("java.importFile"), "doAttachmentsMtrlFrmFile"));
									}
								}
							}
						}
						
						context.put("menu", b);
					}
					
					if (allowUpdateGroupMembership)
					{
						// show Manage Groups menu
						Menu b = new MenuImpl(portlet, data, (String) state.getAttribute(STATE_ACTION));
						if (!isMyWorkspace
								&& (ServerConfigurationService.getString("wsetup.group.support") == "" 
									|| ServerConfigurationService.getString("wsetup.group.support").equalsIgnoreCase(Boolean.TRUE.toString())))
						{
							// show the group toolbar unless configured to not support group
							b.add( new MenuEntry(rb.getString("java.group"), "doMenu_group"));
						}
						context.put("menu", b);
					}
					
					if (allowUpdateSiteMembership)
					{
						// show add participant menu
						Menu b = new MenuImpl(portlet, data, (String) state.getAttribute(STATE_ACTION));
						if (!isMyWorkspace)
						{
							// show the Add Participant menu
							b.add( new MenuEntry(rb.getString("java.addp"), "doMenu_siteInfo_addParticipant"));
						}
						context.put("menu", b);
					}
					
					if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITESETUP))
					{
						// editing from worksite setup tool
						context.put("fromWSetup", Boolean.TRUE);
						if (state.getAttribute(STATE_PREV_SITE) != null)
						{
							context.put("prevSite", state.getAttribute(STATE_PREV_SITE));
						}
						if (state.getAttribute(STATE_NEXT_SITE) != null)
						{
							context.put("nextSite", state.getAttribute(STATE_NEXT_SITE));
						}
					}
					else
					{
						context.put("fromWSetup", Boolean.FALSE);
					}
					//allow view roster?
					boolean allowViewRoster = SiteService.allowViewRoster(siteId);
					if (allowViewRoster)
					{
						context.put("viewRoster", Boolean.TRUE);
					}
					else
					{
						context.put("viewRoster", Boolean.FALSE);
					}
					//set participant list
					if (allowUpdateSite || allowViewRoster || allowUpdateSiteMembership)
					{
						List participants = new Vector(); 
						participants = getParticipantList(state);
						sortedBy = (String) state.getAttribute (SORTED_BY);
						sortedAsc = (String) state.getAttribute (SORTED_ASC);
						if(sortedBy==null) 
						{
							state.setAttribute(SORTED_BY, SORTED_BY_PARTICIPANT_NAME);
							sortedBy = SORTED_BY_PARTICIPANT_NAME;
						}
						if (sortedAsc==null)
						{
							state.setAttribute(SORTED_ASC, SORTED_ASC);
							sortedAsc = SORTED_ASC;
						}
						if(sortedBy!=null) context.put ("currentSortedBy", sortedBy);
						if(sortedAsc!=null) context.put ("currentSortAsc", sortedAsc);
						Iterator sortedParticipants = null;
						if (sortedBy != null)
						{
							sortedParticipants = new SortedIterator (participants.iterator (), new SiteComparator (sortedBy, sortedAsc));
							participants.clear();
							while (sortedParticipants.hasNext())
							{
								participants.add(sortedParticipants.next());
							}
						}
						context.put("participantListSize", new Integer(participants.size()));
						context.put("participantList", prepPage(state));
						pagingInfoToContext(state, context);
					}
					
					context.put("include", Boolean.valueOf(site.isPubView()));
					
					// site contact information
					String contactName = siteProperties.getProperty(PROP_SITE_CONTACT_NAME);
					String contactEmail = siteProperties.getProperty(PROP_SITE_CONTACT_EMAIL);
					if (contactName == null && contactEmail == null)
					{
						User u = site.getCreatedBy();
						String email = u.getEmail();
						if (email != null)
						{
							contactEmail = u.getEmail();	
						}
						contactName = u.getDisplayName();
					}
					if (contactName != null)
					{
						context.put("contactName", contactName);
					}
					if (contactEmail != null)
					{
						context.put("contactEmail", contactEmail);
					}
					if (siteType != null && siteType.equalsIgnoreCase("course"))
					{ 
						context.put("isCourseSite", Boolean.TRUE);
						List providerCourseList = getProviderCourseList(StringUtil.trimToNull(getExternalRealmId(state)));
						if (providerCourseList != null)
						{
							state.setAttribute(SITE_PROVIDER_COURSE_LIST, providerCourseList);
							context.put("providerCourseList", providerCourseList);
						}
						String manualCourseListString = site.getProperties().getProperty(PROP_SITE_REQUEST_COURSE);
						if (manualCourseListString != null)
						{
							List manualCourseList = new Vector();
							if (manualCourseListString.indexOf("+") != -1)
							{
								manualCourseList = new ArrayList(Arrays.asList(manualCourseListString.split("\\+")));
							}
							else
							{
								manualCourseList.add(manualCourseListString);
							}
							state.setAttribute(SITE_MANUAL_COURSE_LIST, manualCourseList);
							context.put("manualCourseList", manualCourseList);
						}
						context.put("term", siteProperties.getProperty(PROP_SITE_TERM));
					}
					else
					{
						context.put("isCourseSite", Boolean.FALSE);
					}
				}
				catch (Exception e)
				{
					M_log.warn(this + " site info list: " + e.toString());
				}
				
				roles = getRoles(state);
				context.put("roles", roles);
				
				// will have the choice to active/inactive user or not 
				String activeInactiveUser = ServerConfigurationService.getString("activeInactiveUser", Boolean.FALSE.toString());
				if (activeInactiveUser.equalsIgnoreCase("true"))
				{
					context.put("activeInactiveUser", Boolean.TRUE);
					// put realm object into context
					realmId = SiteService.siteReference(site.getId());
					try
					{
						context.put("realm", AuthzGroupService.getAuthzGroup(realmId));
					}
					catch (GroupNotDefinedException e)
					{
						M_log.warn(this + "  IdUnusedException " + realmId);
					}
				}
				else
				{
					context.put("activeInactiveUser", Boolean.FALSE);
				}
				
				context.put("groupsWithMember", site.getGroupsWithMember(UserDirectoryService.getCurrentUser().getId()));
				return (String)getContext(data).get("template") + TEMPLATE[12];		
			case 13:
				/*  buildContextForTemplate chef_site-siteInfo-editInfo.vm
				* 
				*/
				siteProperties = site.getProperties();
				
				context.put("title", state.getAttribute(FORM_SITEINFO_TITLE));
				
				context.put("titleEditableSiteType", state.getAttribute(TITLE_EDITABLE_SITE_TYPE));
				context.put("type", site.getType());
				
				siteType = (String) state.getAttribute(STATE_SITE_TYPE);
				if (siteType != null && siteType.equalsIgnoreCase("course"))
				{ 
					context.put("isCourseSite", Boolean.TRUE);
					context.put("skins", state.getAttribute(STATE_ICONS));
					if (state.getAttribute(FORM_SITEINFO_SKIN) != null)
					{
						context.put("selectedIcon",state.getAttribute(FORM_SITEINFO_SKIN));
					}
					else if (site.getIconUrl() != null)
					{
						context.put("selectedIcon",site.getIconUrl());
					}
					
					terms = CourseManagementService.getTerms();
					if (terms != null && terms.size() >0)
					{
						context.put("termList", terms);
					}
					
					if (state.getAttribute(FORM_SITEINFO_TERM) == null)
					{
						String currentTerm = site.getProperties().getProperty(PROP_SITE_TERM);
						if (currentTerm != null)
						{
							state.setAttribute(FORM_SITEINFO_TERM, currentTerm);
						}
					}
					if (state.getAttribute(FORM_SITEINFO_TERM) != null)
					{
						context.put("selectedTerm", state.getAttribute(FORM_SITEINFO_TERM));
					}
				}
				else
				{
					context.put("isCourseSite", Boolean.FALSE);
					
					if (state.getAttribute(FORM_SITEINFO_ICON_URL) == null && StringUtil.trimToNull(site.getIconUrl()) != null)
					{
						state.setAttribute(FORM_SITEINFO_ICON_URL, site.getIconUrl());
					}
					if (state.getAttribute(FORM_SITEINFO_ICON_URL) != null)
					{
						context.put("iconUrl", state.getAttribute(FORM_SITEINFO_ICON_URL));
					}
				}
				context.put("description", state.getAttribute(FORM_SITEINFO_DESCRIPTION));
				context.put("short_description", state.getAttribute(FORM_SITEINFO_SHORT_DESCRIPTION));
				context.put("form_site_contact_name", state.getAttribute(FORM_SITEINFO_CONTACT_NAME));
				context.put("form_site_contact_email", state.getAttribute(FORM_SITEINFO_CONTACT_EMAIL));

				//Display of appearance icon/url list with course site based on 
				// "disable.course.site.skin.selection" value set with sakai.properties file.
			  if ((ServerConfigurationService.getString("disable.course.site.skin.selection")).equals("true")){
			    context.put("disableCourseSelection", Boolean.TRUE);
			  }
			  
				return (String)getContext(data).get("template") + TEMPLATE[13];	
			case 14:
				/*  buildContextForTemplate chef_site-siteInfo-editInfoConfirm.vm
				* 
				*/
				siteProperties = site.getProperties();
				siteType = (String)state.getAttribute(STATE_SITE_TYPE);
				if (siteType != null && siteType.equalsIgnoreCase("course"))
				{ 
					context.put("isCourseSite", Boolean.TRUE);
					context.put("siteTerm", state.getAttribute(FORM_SITEINFO_TERM));
				}
				else
				{
					context.put("isCourseSite", Boolean.FALSE);
				}
				context.put("oTitle", site.getTitle());
				context.put("title", state.getAttribute(FORM_SITEINFO_TITLE));
				
				context.put("description", state.getAttribute(FORM_SITEINFO_DESCRIPTION));
				context.put("oDescription", site.getDescription());
				context.put("short_description", state.getAttribute(FORM_SITEINFO_SHORT_DESCRIPTION));
				context.put("oShort_description", site.getShortDescription());
				context.put("skin", state.getAttribute(FORM_SITEINFO_SKIN));
				context.put("oSkin", site.getIconUrl());
				context.put("skins", state.getAttribute(STATE_ICONS));
				context.put("oIcon", site.getIconUrl());
				context.put("icon", state.getAttribute(FORM_SITEINFO_ICON_URL));
				context.put("include", state.getAttribute(FORM_SITEINFO_INCLUDE));
				context.put("oInclude", Boolean.valueOf(site.isPubView()));
				context.put("name", state.getAttribute(FORM_SITEINFO_CONTACT_NAME));
				context.put("oName", siteProperties.getProperty(PROP_SITE_CONTACT_NAME));
				context.put("email", state.getAttribute(FORM_SITEINFO_CONTACT_EMAIL));
				context.put("oEmail", siteProperties.getProperty(PROP_SITE_CONTACT_EMAIL));
				
				return (String)getContext(data).get("template") + TEMPLATE[14];		
			case 15:
				/*  buildContextForTemplate chef_site-addRemoveFeatureConfirm.vm
				* 
				*/
				context.put("title", site.getTitle());
				
				site_type = (String)state.getAttribute(STATE_SITE_TYPE); 
				myworkspace_site = false;
				if (SiteService.isUserSite(site.getId()))
				{
					if (SiteService.getSiteUserId(site.getId()).equals(SessionManager.getCurrentSessionUserId()))
					{
						myworkspace_site = true;
						site_type = "myworkspace";
					}
				}
				
				context.put (STATE_TOOL_REGISTRATION_LIST, state.getAttribute(STATE_TOOL_REGISTRATION_LIST));
				
				context.put("check_home", state.getAttribute(STATE_TOOL_HOME_SELECTED));
				context.put("selectedTools", orderToolIds(state, (String) state.getAttribute(STATE_SITE_TYPE), (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST)));
				context.put("oldSelectedTools", state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST));	
				context.put("oldSelectedHome", state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME));
				context.put("continueIndex", "12");
				if (state.getAttribute(STATE_TOOL_EMAIL_ADDRESS) != null)
				{	
					context.put("emailId", state.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
				}
				context.put("serverName", ServerConfigurationService.getServerName());
				context.put("newsTitles", (Hashtable) state.getAttribute(STATE_NEWS_TITLES));
				context.put("wcTitles", (Hashtable) state.getAttribute(STATE_WEB_CONTENT_TITLES));
				
				if (fromENWModifyView(state))
				{
					context.put("back", "26");
				}
				else
				{
					context.put("back", "4");
				}
				
				return (String)getContext(data).get("template") + TEMPLATE[15];		
			case 16:
				/*  buildContextForTemplate chef_site-publishUnpublish-sendEmail.vm
				* 
				*/
				context.put("title", site.getTitle());
				context.put("willNotify", state.getAttribute(FORM_WILL_NOTIFY));
				return (String)getContext(data).get("template") + TEMPLATE[16];		
			case 17:
				/*  buildContextForTemplate chef_site-publishUnpublish-confirm.vm
				* 
				*/
				context.put("title", site.getTitle());
				context.put("continueIndex", "12");
				SiteInfo sInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
				if (sInfo.getPublished())
				{
					context.put("publish", Boolean.TRUE);
					context.put("backIndex", "16");
				}
				else
				{
					context.put("publish", Boolean.FALSE);
					context.put("backIndex", "9");
				}
				context.put("willNotify", state.getAttribute(FORM_WILL_NOTIFY));
				return (String)getContext(data).get("template") + TEMPLATE[17];	
		case 18:
			/*  buildContextForTemplate chef_siteInfo-editAccess.vm
			* 
			*/
			List publicChangeableSiteTypes = (List) state.getAttribute(STATE_PUBLIC_CHANGEABLE_SITE_TYPES);
			List unJoinableSiteTypes = (List) state.getAttribute(STATE_DISABLE_JOINABLE_SITE_TYPE);
			
			if (site != null)
			{
				//editing existing site
				context.put("site", site);
				siteType = state.getAttribute(STATE_SITE_TYPE)!=null?(String)state.getAttribute(STATE_SITE_TYPE):null;
				
				if ( siteType != null 
					&& publicChangeableSiteTypes.contains(siteType))
				{
					context.put ("publicChangeable", Boolean.TRUE);
				}
				else
				{
					context.put("publicChangeable", Boolean.FALSE);
				}
				context.put("include", Boolean.valueOf(site.isPubView()));
				
				if ( siteType != null && !unJoinableSiteTypes.contains(siteType))
				{
					// site can be set as joinable
					context.put("disableJoinable", Boolean.FALSE);
					if (state.getAttribute(STATE_JOINABLE) == null)
					{
						state.setAttribute(STATE_JOINABLE, Boolean.valueOf(site.isJoinable()));
					}
					if (state.getAttribute(STATE_JOINERROLE) == null 
						|| state.getAttribute(STATE_JOINABLE) != null && ((Boolean) state.getAttribute(STATE_JOINABLE)).booleanValue()) 
					{
						state.setAttribute(STATE_JOINERROLE, site.getJoinerRole());
					}
					
					if (state.getAttribute(STATE_JOINABLE) != null)
					{
						context.put("joinable", state.getAttribute(STATE_JOINABLE));
					}
					if (state.getAttribute(STATE_JOINERROLE) != null)
					{
						context.put("joinerRole", state.getAttribute(STATE_JOINERROLE));
					}
				}
				else
				{
					//site cannot be set as joinable
					context.put("disableJoinable", Boolean.TRUE);
				}
				
				context.put("roles", getRoles(state));
				context.put("back", "12");
			}
			else
			{
				siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
				
				if ( siteInfo.site_type != null 
					&& publicChangeableSiteTypes.contains(siteInfo.site_type))
				{
					context.put ("publicChangeable", Boolean.TRUE);
				}
				else
				{
					context.put("publicChangeable", Boolean.FALSE);
				}
				context.put("include", Boolean.valueOf(siteInfo.getInclude()));
				context.put("published", Boolean.valueOf(siteInfo.getPublished()));
				
				if ( siteInfo.site_type != null && !unJoinableSiteTypes.contains(siteInfo.site_type))
				{
					// site can be set as joinable
					context.put("disableJoinable", Boolean.FALSE);
					context.put("joinable", Boolean.valueOf(siteInfo.joinable));
					context.put("joinerRole", siteInfo.joinerRole);
				}
				else
				{
					// site cannot be set as joinable
					context.put("disableJoinable", Boolean.TRUE);
				}
				
				// use the type's template, if defined
				String realmTemplate = "!site.template";
				if (siteInfo.site_type != null)
				{
					realmTemplate = realmTemplate + "." + siteInfo.site_type;
				}
				try
				{
					AuthzGroup r = AuthzGroupService.getAuthzGroup(realmTemplate);
					context.put("roles", r.getRoles());
				}
				catch (GroupNotDefinedException e)
				{
					try
					{
						AuthzGroup rr = AuthzGroupService.getAuthzGroup("!site.template");
						context.put("roles", rr.getRoles());
					}
					catch (GroupNotDefinedException ee)
					{
					}
				}
				
				// new site, go to confirmation page
				context.put("continue", "10");
				if (fromENWModifyView(state))
				{
					context.put("back", "26");
				}
				else if (state.getAttribute(STATE_IMPORT) != null)
				{
					context.put("back", "27");
				}
				else
				{
					context.put("back", "3");
				}
				
				siteType = (String) state.getAttribute(STATE_SITE_TYPE);
				if (siteType!=null && siteType.equalsIgnoreCase("course"))
				{
					context.put ("isCourseSite", Boolean.TRUE);
					context.put("isProjectSite", Boolean.FALSE);
				}
				else
				{
					context.put ("isCourseSite", Boolean.FALSE);
					if (siteType.equalsIgnoreCase("project"))
					{
						context.put("isProjectSite", Boolean.TRUE);
					}
				}
			}
			return (String)getContext(data).get("template") + TEMPLATE[18];
		case 19:
			/*  buildContextForTemplate chef_site-addParticipant-sameRole.vm
			* 
			*/
			context.put("title", site.getTitle());
			context.put("roles", getRoles(state));
			context.put("participantList", state.getAttribute(STATE_ADD_PARTICIPANTS));
			context.put("form_selectedRole", state.getAttribute("form_selectedRole"));
			return (String)getContext(data).get("template") + TEMPLATE[19];
		case 20:
			/*  buildContextForTemplate chef_site-addParticipant-differentRole.vm
			* 
			*/
			context.put("title", site.getTitle());
			context.put("roles", getRoles(state));
			context.put("selectedRoles", state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES));
			context.put("participantList", state.getAttribute(STATE_ADD_PARTICIPANTS));
			return (String)getContext(data).get("template") + TEMPLATE[20];
		case 21:
			/*  buildContextForTemplate chef_site-addParticipant-notification.vm
			* 
			*/
			context.put("title", site.getTitle());
			if (state.getAttribute("form_selectedNotify") == null)
			{
				state.setAttribute("form_selectedNotify", Boolean.FALSE);
			}
			context.put("notify", state.getAttribute("form_selectedNotify"));
			boolean same_role = state.getAttribute("form_same_role")==null?true:((Boolean) state.getAttribute("form_same_role")).booleanValue();
			if (same_role)
			{
				context.put("backIndex", "19");
			}
			else
			{
				context.put("backIndex", "20");
			}
			return (String)getContext(data).get("template") + TEMPLATE[21];
		case 22:
			/*  buildContextForTemplate chef_site-addParticipant-confirm.vm
			* 
			*/
			context.put("title", site.getTitle());
			context.put("participants", state.getAttribute(STATE_ADD_PARTICIPANTS));
			context.put("notify", state.getAttribute("form_selectedNotify"));
			context.put("roles", getRoles(state));
			context.put("same_role", state.getAttribute("form_same_role"));
			context.put("selectedRoles", state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES));
			context.put("selectedRole", state.getAttribute("form_selectedRole"));
			return (String)getContext(data).get("template") + TEMPLATE[22];
		case 23:
			/*  buildContextForTemplate chef_siteInfo-editAccess-globalAccess.vm
			* 
			*/
			context.put("title", site.getTitle());
			context.put("roles", getRoles(state));
			if (state.getAttribute("form_joinable") == null)
			{
				state.setAttribute("form_joinable", new Boolean(site.isJoinable()));
			}
			context.put("form_joinable", state.getAttribute("form_joinable"));
			if (state.getAttribute("form_joinerRole") == null)
			{
				state.setAttribute("form_joinerRole", site.getJoinerRole());
			}
			context.put("form_joinerRole", state.getAttribute("form_joinerRole"));
			return (String)getContext(data).get("template") + TEMPLATE[23];
		case 24:
			/*  buildContextForTemplate chef_siteInfo-editAccess-globalAccess-confirm.vm
			* 
			*/
			context.put("title", site.getTitle());
			context.put("form_joinable", state.getAttribute("form_joinable"));
			context.put("form_joinerRole", state.getAttribute("form_joinerRole"));
			return (String)getContext(data).get("template") + TEMPLATE[24];
		case 25:
			/*  buildContextForTemplate chef_changeRoles-confirm.vm
			 * 
			 */
			Boolean sameRole = (Boolean) state.getAttribute(STATE_CHANGEROLE_SAMEROLE);
			context.put("sameRole", sameRole);
			if (sameRole.booleanValue())
			{
				// same role
				context.put("currentRole", state.getAttribute(STATE_CHANGEROLE_SAMEROLE_ROLE));
			}
			else
			{
				context.put("selectedRoles", state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES));
			}
			
			roles = getRoles(state);
			context.put("roles", roles);
			
			context.put("participantSelectedList", state.getAttribute(STATE_SELECTED_PARTICIPANTS));
			if (state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES) != null)
			{
				context.put("selectedRoles", state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES));
			}
			context.put("siteTitle", site.getTitle());
			return (String)getContext(data).get("template") + TEMPLATE[25];
		case 26: 
			/*  buildContextForTemplate chef_site-modifyENW.vm
			* 
			*/
			site_type = (String) state.getAttribute(STATE_SITE_TYPE);
			boolean existingSite = site != null? true:false;
			if (existingSite)
			{
				// revising a existing site's tool
				context.put("existingSite", Boolean.TRUE);
				context.put("back", "4");
				context.put("continue", "15");
				context.put("function", "eventSubmit_doAdd_remove_features");
			}
			else
			{
				// new site
				context.put("existingSite", Boolean.FALSE);
				context.put("function", "eventSubmit_doAdd_features");
				if (state.getAttribute(STATE_IMPORT) != null)
				{
					context.put("back", "27");
				}
				else
				{
					// new site, go to edit access page
					context.put("back", "3");
				}
				context.put("continue", "18");
			}
			
			context.put (STATE_TOOL_REGISTRATION_LIST, state.getAttribute(STATE_TOOL_REGISTRATION_LIST));
			toolRegistrationSelectedList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);	
			context.put (STATE_TOOL_REGISTRATION_SELECTED_LIST, toolRegistrationSelectedList); // String toolId's
			String emailId = (String) state.getAttribute(STATE_TOOL_EMAIL_ADDRESS);
			if (emailId != null)
			{
				context.put("emailId", emailId);
			}
			
			//titles for news tools
			newsTitles = (Hashtable) state.getAttribute(STATE_NEWS_TITLES);
			if (newsTitles == null)
			{
				newsTitles = new Hashtable();
				newsTitles.put("sakai.news", NEWS_DEFAULT_TITLE);
				state.setAttribute(STATE_NEWS_TITLES, newsTitles);
			}
			context.put("newsTitles", newsTitles);
			//urls for news tools
			newsUrls = (Hashtable) state.getAttribute(STATE_NEWS_URLS);
			if (newsUrls == null)
			{
				newsUrls = new Hashtable();
				newsUrls.put("sakai.news", NEWS_DEFAULT_URL);
				state.setAttribute(STATE_NEWS_URLS, newsUrls);
			}
			context.put("newsUrls", newsUrls);
			// titles for web content tools
			wcTitles = (Hashtable) state.getAttribute(STATE_WEB_CONTENT_TITLES);
			if (wcTitles == null)
			{
				wcTitles = new Hashtable();
				wcTitles.put("sakai.iframe", WEB_CONTENT_DEFAULT_TITLE);
				state.setAttribute(STATE_WEB_CONTENT_TITLES, wcTitles);
			}
			context.put("wcTitles", wcTitles);
			//URLs for web content tools
			wcUrls = (Hashtable) state.getAttribute(STATE_WEB_CONTENT_URLS);
			if (wcUrls == null)
			{
				wcUrls = new Hashtable();
				wcUrls.put("sakai.iframe", WEB_CONTENT_DEFAULT_URL);
				state.setAttribute(STATE_WEB_CONTENT_URLS, wcUrls);
			}
			context.put("wcUrls", wcUrls);
			
			context.put("serverName", ServerConfigurationService.getServerName());
			
			context.put("oldSelectedTools", state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST));	
			
			return (String)getContext(data).get("template") + TEMPLATE[26];
		case 27: 
			/*  buildContextForTemplate chef_site-importSites.vm
			* 
			*/
			existingSite = site != null? true:false;
			site_type = (String) state.getAttribute(STATE_SITE_TYPE);
			if (existingSite)
			{
				// revising a existing site's tool
				context.put("continue", "12");
				context.put("back", "28");
				context.put("totalSteps", "2");
				context.put("step", "2");
				context.put("currentSite", site);
			}
			else
			{
				// new site, go to edit access page
				context.put("back", "3");
				if (fromENWModifyView(state))
				{
					context.put("continue", "26");
				}
				else
				{
					context.put("continue", "18");
				}
			}
			context.put (STATE_TOOL_REGISTRATION_LIST, state.getAttribute(STATE_TOOL_REGISTRATION_LIST));
			context.put ("selectedTools", orderToolIds(state, site_type, (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST))); // String toolId's
			context.put("importSites", state.getAttribute(STATE_IMPORT_SITES));
			context.put("importSitesTools", state.getAttribute(STATE_IMPORT_SITE_TOOL));
			context.put("check_home", state.getAttribute(STATE_TOOL_HOME_SELECTED));
			context.put("importSupportedTools", importTools());

			return (String)getContext(data).get("template") + TEMPLATE[27];
		case 28: 
			/*  buildContextForTemplate chef_siteinfo-import.vm
			* 
			*/
			context.put("currentSite", site);
			context.put("importSiteList", state.getAttribute(STATE_IMPORT_SITES));
			context.put("sites", SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.UPDATE, null, null, null, SortType.TITLE_ASC, null));
			return (String)getContext(data).get("template") + TEMPLATE[28];
		case 29: 
			/*  buildContextForTemplate chef_siteinfo-duplicate.vm
			* 
			*/
			context.put("siteTitle", site.getTitle());
			String sType = site.getType();
			if (sType != null && sType.equals("course"))
			{
				context.put("isCourseSite", Boolean.TRUE);
			}
			else
			{
				context.put("isCourseSite", Boolean.FALSE);
			}
			if (state.getAttribute(SITE_DUPLICATED) == null)
			{
				context.put("siteDuplicated", Boolean.FALSE);
			}
			else
			{
				context.put("siteDuplicated", Boolean.TRUE);
				context.put("duplicatedName", state.getAttribute(SITE_DUPLICATED_NAME));
			}
			return (String)getContext(data).get("template") + TEMPLATE[29];
		case 30:
			/* 
			 *buildContextForTemplate chef_site-sitemanage-search.vm
			 */
			 
			List newTypes = new Vector();
			if (state.getAttribute(NO_SHOW_SEARCH_TYPE) != null)
			{
				String noType = state.getAttribute(NO_SHOW_SEARCH_TYPE).toString();
				List oldTypes = SiteService.getSiteTypes();
				for (int i = 0; i < oldTypes.size(); i++)
				{
					siteType = oldTypes.get(i).toString();
					if ((siteType.indexOf(noType)) == -1)
					{
						newTypes.add(siteType);
					}
				}
			}
			else
			{
				newTypes = SiteService.getSiteTypes();
			}

			// remove the "myworkspace" type
			for (Iterator i = newTypes.iterator(); i.hasNext();)
			{
				String t = (String) i.next();
				if ("myworkspace".equalsIgnoreCase(t))
				{
					i.remove();
				}
			}

			context.put("siteTypes", newTypes);
		
			terms = CourseManagementService.getTerms();
		
			String termSearchSiteType = (String)state.getAttribute(SEARCH_TERM_SITE_TYPE);
			if (termSearchSiteType != null)
			{
				context.put("termSearchSiteType", termSearchSiteType);
				context.put("terms", terms);
			}
			
			return (String)getContext(data).get("template") + TEMPLATE[30];
		case 31:
			/*
			 * buildContextForTemplate chef_site-sitemanage-list.vm
			 */
			Integer newPageSize = (Integer) state.getAttribute("inter_size");
			if (newPageSize != null)
			{
				context.put("pagesize", newPageSize);	
				state.setAttribute(STATE_PAGESIZE, newPageSize);
			}
			else
			{
				state.setAttribute(STATE_PAGESIZE, new Integer(DEFAULT_PAGE_SIZE));
				context.put("pagesize", new Integer(DEFAULT_PAGE_SIZE));
			}
		
			// put the service in the context (used for allow update calls on each site)
			context.put("service", SiteService.getInstance());

			// prepare the paging of realms
			sites = prepPage(state);
			context.put("sites", sites);
		
			pagingInfoToContext(state, context);

			bar = new MenuImpl();
			if (SiteService.allowAddSite(""))
			{
				bar.add( new MenuEntry(rb.getString("java.newsite"), "doNew") );
			}
			if (bar.size() > 0)
			{
				context.put(Menu.CONTEXT_MENU, bar);
			}
			
			Vector siteItems = new Vector();
			for (int i = 0; i < sites.size(); i++)
			{
				SiteItem siteItem = new SiteItem();
				site = (Site)sites.get(i);
				siteItem.setSite(site);
				
				siteType = site.getType();
				if (siteType != null && siteType.equalsIgnoreCase("course"))
				{ 
					realmId = SiteService.siteReference(site.getId());
					String rv = null;
					try
					{
						AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
						rv = realm.getProviderGroupId();
					}
					catch (GroupNotDefinedException e)
					{
						M_log.warn("SiteAction.getExternalRealmId, site realm not found");
					}
					List providerCourseList = getProviderCourseList(StringUtil.trimToNull(rv));
					if (providerCourseList != null)
					{
						siteItem.setProviderCourseList(providerCourseList);
					}
				}
				siteItems.add(siteItem);
			}
			context.put("siteItems", siteItems);

			context.put("termProp", (String)state.getAttribute(SEARCH_TERM_PROP));
			context.put("searchText", (String)state.getAttribute(STATE_SEARCH));
			context.put("siteType", (String)state.getAttribute(STATE_SEARCH_SITE_TYPE));
			context.put("termSelection", (String)state.getAttribute(STATE_TERM_SELECTION));
			context.put("termSearchSiteType", (String)state.getAttribute(SEARCH_TERM_SITE_TYPE));
			
			return (String)getContext(data).get("template") + TEMPLATE[31];
		
		case 32:
			/*  
			* buildContextForTemplate chef_site-sitemanage-participants.vm 
			*/	
			String siteId = (String) state.getAttribute("siteId");
			try
			{
				site = SiteService.getSite(siteId);
				context.put("site", site);
			
				bar = new MenuImpl();
				bar.add( new MenuEntry(rb.getString("java.addp"), null, true, MenuItem.CHECKED_NA, "doMenu_sitemanage_addParticipant", "site-form") );												
				context.put(Menu.CONTEXT_MENU, bar);
		
				// for paging
				newPageSize = (Integer) state.getAttribute("inter_size");
				if (newPageSize != null)
				{
					context.put("pagesize", newPageSize);	
					state.setAttribute(STATE_PAGESIZE, newPageSize);
				}
				else
				{
					state.setAttribute(STATE_PAGESIZE, new Integer(DEFAULT_PAGE_SIZE));
					context.put("pagesize", new Integer(DEFAULT_PAGE_SIZE));
				}

				// put the service in the context (used for allow update calls on each site)
				context.put("service", SiteService.getInstance());

				// prepare the paging of realms
				List participants = prepPage(state);
				context.put("participants", participants);
		
				pagingInfoToContext(state, context);
				
				roles = getRoles(state);
				context.put("roles", roles);
				
				// will have the choice to active/inactive user or not 
				activeInactiveUser = ServerConfigurationService.getString("activeInactiveUser", Boolean.FALSE.toString());
				if (activeInactiveUser.equalsIgnoreCase("true"))
				{
					context.put("activeInactiveUser", Boolean.TRUE);
					// put realm object into context
					realmId = SiteService.siteReference(site.getId());
					try
					{
						context.put("realm", AuthzGroupService.getAuthzGroup(realmId));
					}
					catch (GroupNotDefinedException e)
					{
						M_log.warn(this + "  IdUnusedException " + realmId);
					}
				}
				else
				{
					context.put("activeInactiveUser", Boolean.FALSE);
				}
				
				boolean allowUpdateSite = SiteService.allowUpdateSite(siteId);
				if (allowUpdateSite)
				{	
					context.put("allowUpdate", Boolean.TRUE);
				}
				else
				{
					context.put("allowUpate", Boolean.FALSE);
				}
			
				return (String)getContext(data).get("template") + TEMPLATE[32];
			}
			catch(IdUnusedException e)
			{
				return (String)getContext(data).get("template") + TEMPLATE[31];
			}
		case 33:
			/*  
			* buildContextForTemplate chef_site-sitemanage-addParticipant.vm 
			*/
         // Note that (for now) these strings are in both sakai.properties and sitesetupgeneric.properties
			context.put("noEmailInIdAccountName", ServerConfigurationService.getString("noEmailInIdAccountName"));
			context.put("noEmailInIdAccountLabel", ServerConfigurationService.getString("noEmailInIdAccountLabel"));
			context.put("emailInIdAccountName", ServerConfigurationService.getString("emailInIdAccountName"));
			context.put("emailInIdAccountLabel", ServerConfigurationService.getString("emailInIdAccountLabel"));
			
			try
			{
				site = SiteService.getSite(state.getAttribute("siteId").toString());
				context.put("title", site.getTitle());
				roles = getRoles(state);
				context.put("roles", roles);
				if(state.getAttribute("noEmailInIdAccountValue")!=null)
				{
					context.put("noEmailInIdAccountValue", (String)state.getAttribute("noEmailInIdAccountValue"));
				}
				if(state.getAttribute("emailInIdAccountValue")!=null)
				{
					context.put("emailInIdAccountValue", (String)state.getAttribute("emailInIdAccountValue"));
				}
				if(state.getAttribute("form_same_role") != null)
				{
					context.put("form_same_role", ((Boolean) state.getAttribute("form_same_role")).toString());
				}
				else
				{
					context.put("form_same_role", Boolean.TRUE.toString());
				}
				context.put("backIndex", "32");
				return (String)getContext(data).get("template") + TEMPLATE[33];
			}
			catch(Exception e)
			{
				return (String)getContext(data).get("template") + TEMPLATE[32];
			}
		case 34:
			/*  
			* buildContextForTemplate chef_site-sitemanage-sameRole.vm 
			*/
			context.put("title", site.getTitle());
			context.put("roles", getRoles(state));
			context.put("participantList", state.getAttribute(STATE_ADD_PARTICIPANTS));
			context.put("form_selectedRole", state.getAttribute("form_selectedRole"));
			return (String)getContext(data).get("template") + TEMPLATE[34];
		case 35:
			/*  
			* buildContextForTemplate chef_site-sitemanage-differentRoles.vm 
			*/
			context.put("title", site.getTitle());
			context.put("roles", getRoles(state));
			context.put("selectedRoles", state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES));
			context.put("participantList", state.getAttribute(STATE_ADD_PARTICIPANTS));
			return (String)getContext(data).get("template") + TEMPLATE[35];
		case 36:
			/*
			 * buildContextForTemplate chef_site-newSiteCourse.vm
			 */
			if (site != null)
			{
				context.put("site", site);
				context.put("siteTitle", site.getTitle());
				terms = CourseManagementService.getTerms();
				if (terms != null && terms.size() >0)
				{
					context.put("termList", terms);
				}
				
				List providerCourseList = (List) state.getAttribute(SITE_PROVIDER_COURSE_LIST);
				context.put("providerCourseList", providerCourseList);
				context.put("manualCourseList", state.getAttribute(SITE_MANUAL_COURSE_LIST));
				
				Term t = (Term) state.getAttribute(STATE_TERM_SELECTED);
				context.put ("term", t);
				if (t != null)
				{
					String userId = StringUtil.trimToZero(SessionManager.getCurrentSessionUserId());
					List courses = CourseManagementService.getInstructorCourses(userId, t.getYear(), t.getTerm());
					if (courses != null && courses.size() > 0)
					{
						Vector notIncludedCourse = new Vector();
						
						// remove included sites
						for (Iterator i = courses.iterator(); i.hasNext(); )
						{
							Course c = (Course) i.next();
							if (!providerCourseList.contains(c.getId()))
							{
								notIncludedCourse.add(c);
							}
						}
						state.setAttribute(STATE_TERM_COURSE_LIST, notIncludedCourse);
					}
					else
					{
						state.removeAttribute(STATE_TERM_COURSE_LIST);
					}
				}
				
				// step number used in UI
				state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer("1"));
			}
			else
			{
				if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null)
				{
					context.put ("selectedProviderCourse", state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN));
				}
				if(state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null)
				{
					context.put("selectedManualCourse", Boolean.TRUE);
				}
				context.put ("term", (Term) state.getAttribute(STATE_TERM_SELECTED));
			}		
			if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITESETUP))
			{
				context.put("backIndex", "1");
			}	
			else if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITEINFO))
			{	
				context.put("backIndex", "");
			}
			context.put("termCourseList", (List) state.getAttribute(STATE_TERM_COURSE_LIST));
			
			
			return (String)getContext(data).get("template") + TEMPLATE[36];
		case 37:
			/*
			 * buildContextForTemplate chef_site-newSiteCourseManual.vm
			 */
			if (site != null)
			{
				context.put("site", site);
				context.put("siteTitle", site.getTitle());
				coursesIntoContext(state, context, site);
			}
			buildInstructorSectionsList(state, params, context);
			context.put("form_requiredFields", CourseManagementService.getCourseIdRequiredFields());
			context.put("form_requiredFieldsSizes", CourseManagementService.getCourseIdRequiredFieldsSizes());
			context.put("form_additional", siteInfo.additional);
			context.put("form_title", siteInfo.title);
			context.put("form_description", siteInfo.description);
			context.put("noEmailInIdAccountName", ServerConfigurationService.getString("noEmailInIdAccountName", ""));
			context.put("value_uniqname", state.getAttribute(STATE_SITE_QUEST_UNIQNAME));
			int number = 1;
			if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null)
			{
				number = ((Integer) state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER)).intValue();
				context.put("currentNumber", new Integer(number));
			}
			context.put("currentNumber", new Integer(number));
			context.put("listSize", new Integer(number-1));
			context.put("fieldValues", state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
			
			if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null)
			{
				List l = (List) state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
				context.put ("selectedProviderCourse", l);
				context.put("size", new Integer(l.size()-1));
			}
			if (site != null)
			{
				context.put("back", "36");
			}
			else
			{
				if (state.getAttribute(STATE_AUTO_ADD) != null)
				{
					context.put("autoAdd", Boolean.TRUE);
					context.put("back", "36");
				}
				else
				{
					context.put("back", "1");
				}
			}
			context.put("isFutureTerm", state.getAttribute(STATE_FUTURE_TERM_SELECTED));
			context.put("weeksAhead", ServerConfigurationService.getString("roster.available.weeks.before.term.start", "0"));
			return (String)getContext(data).get("template") + TEMPLATE[37];	
		case 38:
			/*  
			* buildContextForTemplate chef_site-sitemanage-editInfo.vm
			*/
			bar = new MenuImpl(portlet, data, (String) state.getAttribute(STATE_ACTION));
			if (state.getAttribute("siteId") != null)
			{
				if (SiteService.allowRemoveSite(state.getAttribute("siteId").toString()))
				{
					bar.add( new MenuEntry(rb.getString("java.delete"), null, true, MenuItem.CHECKED_NA, "doSitemanage_site_delete",  "site-form"));
				}
			}
			bar.add( new MenuEntry(rb.getString("java.saveas"), null, true, MenuItem.CHECKED_NA, "doSitemanage_saveas_request", "site-form") );
			context.put("menu", bar);
			context.put("title", state.getAttribute(FORM_SITEINFO_TITLE));
			
			types = SiteService.getSiteTypes();
			context.put("types", types);
			context.put("siteType", state.getAttribute("siteType"));
			
			if (SecurityService.isSuperUser())
			{
				context.put("isSuperUser", Boolean.TRUE);
			}
			else
			{
				context.put("isSuperUser", Boolean.FALSE);
			}
			context.put("description", state.getAttribute(FORM_SITEINFO_DESCRIPTION));
			context.put("short_description", state.getAttribute(FORM_SITEINFO_SHORT_DESCRIPTION));
			context.put("skins", state.getAttribute("skins"));
			context.put("skin", state.getAttribute(FORM_SITEINFO_SKIN));
			context.put("iconUrl", state.getAttribute("siteIconUrl"));
			context.put("include", state.getAttribute(FORM_SITEINFO_INCLUDE));
			context.put("form_site_contact_name", state.getAttribute(FORM_SITEINFO_CONTACT_NAME));
			context.put("form_site_contact_email", state.getAttribute(FORM_SITEINFO_CONTACT_EMAIL));
			if (state.getAttribute(STATE_SITE_SIZE_DEFAULT_SELECT) == null)
			{
				context.put("default_size_selected", Boolean.TRUE);
			}
			else
			{
				context.put("default_size_selected", state.getAttribute(STATE_SITE_SIZE_DEFAULT_SELECT));
			}
			return (String)getContext(data).get("template") + TEMPLATE[38];	
		case 39:
			/*  
			* buildContextForTemplate chef_site-sitemanage-editInfo.vm
			*/
			context.put("site", site);
			context.put("include", Boolean.valueOf(site.isPubView()));
			context.put("roles", getRoles(state));
			return (String)getContext(data).get("template") + TEMPLATE[39];
		case 40:
			/*  
			* buildContextForTemplate chef_site-sitemanage-siteDeleteConfirmation.vm
			*/
			String id = (String) state.getAttribute("siteId");
			site_title = NULL_STRING;				
			user = SessionManager.getCurrentSessionUserId();
			workspace = SiteService.getUserSiteId(user);
			boolean removeable = true;
			
			if(!(id.equals(workspace)))
			{
				try
				{
					site_title = SiteService.getSite(id).getTitle();
					
				}
				catch (IdUnusedException e)
				{
					M_log.warn("SiteAction.doSitemanage_delete_confirmed - IdUnusedException " + id);
					addAlert(state, rb.getString("java.sitewith")+" " + id + " "+ rb.getString("java.couldnt")+" ");
				}
				if(SiteService.allowRemoveSite(id))
				{
					try
					{
						SiteService.getSite(id);
					}
					catch (IdUnusedException e)
					{
						M_log.warn("SiteAction.buildContextForTemplate chef_site-sitemanage-siteDeleteConfirm.vm: IdUnusedException");	
					}
				}
				else
				{
					removeable = false;
					addAlert(state, site_title + " "+rb.getString("java.couldntdel")+" ");
				}
			}
			else
			{
				removeable = false;
				addAlert(state, rb.getString("java.yourwork"));
			}

			if(!removeable)
			{
				addAlert(state, rb.getString("java.click"));
			}
			
			context.put("removeable", new Boolean(removeable));
			return (String)getContext(data).get("template") + TEMPLATE[40];	
		case 41:
			/*  
			* buildContextForTemplate chef_site-sitemanage-site-saveas.vm
			*/
			context.put("site", site);
			return (String)getContext(data).get("template") + TEMPLATE[41];	
		case 42:
			/*  buildContextForTemplate chef_site-gradtoolsConfirm.vm
			* 
			*/
			siteInfo = (SiteInfo)state.getAttribute(STATE_SITE_INFO);
			context.put("title", siteInfo.title);
			context.put("description", siteInfo.description);
			context.put("short_description", siteInfo.short_description);
			toolRegistrationList = (Vector) state.getAttribute(STATE_PROJECT_TOOL_LIST);
			toolRegistrationSelectedList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			context.put (STATE_TOOL_REGISTRATION_SELECTED_LIST, toolRegistrationSelectedList); // String toolId's
			context.put (STATE_TOOL_REGISTRATION_LIST, toolRegistrationList ); // %%% use Tool
			context.put("check_home", state.getAttribute(STATE_TOOL_HOME_SELECTED));
			context.put("emailId", state.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			context.put("serverName", ServerConfigurationService.getServerName());
			context.put("include", new Boolean(siteInfo.include));
			return (String)getContext(data).get("template") + TEMPLATE[42];
		case 43:
			/*  buildContextForTemplate chef_siteInfo-editClass.vm
			* 
			*/
			bar = new MenuImpl(portlet, data, (String) state.getAttribute(STATE_ACTION));
			if (SiteService.allowAddSite(null))
			{
				bar.add( new MenuEntry(rb.getString("java.addclasses"), "doMenu_siteInfo_addClass"));
			}
			context.put("menu", bar);
			
			context.put("siteTitle", site.getTitle());
			coursesIntoContext(state, context, site);
			
			return (String)getContext(data).get("template") + TEMPLATE[43];
		case 44:
			/*  buildContextForTemplate chef_siteInfo-addCourseConfirm.vm
			* 
			*/
			
			context.put("siteTitle", site.getTitle());
			
			coursesIntoContext(state, context, site);
			
			context.put("providerAddCourses", state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN));
			if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null)
			{
				int addNumber = ((Integer) state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER)).intValue() -1;
				context.put("manualAddNumber", new Integer(addNumber));
				context.put("requestFields", state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
				context.put("backIndex", "37");
			}
			else
			{
				context.put("backIndex", "36");
			}
			//those manual inputs
			context.put("form_requiredFields", CourseManagementService.getCourseIdRequiredFields());
			context.put("fieldValues", state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
			
			return (String)getContext(data).get("template") + TEMPLATE[44];
      
      //htripath - import materials from classic
    case 45:
      /*  buildContextForTemplate chef_siteInfo-importMtrlMaster.vm
      * 
      */      
      return (String)getContext(data).get("template") + TEMPLATE[45];

    case 46:
      /*  buildContextForTemplate chef_siteInfo-importMtrlCopy.vm
      * 
      */     
      // this is for list display in listbox
      context.put("allZipSites", state.getAttribute(ALL_ZIP_IMPORT_SITES));

      context.put("finalZipSites", state.getAttribute(FINAL_ZIP_IMPORT_SITES));
      //zip file
      //context.put("zipreffile",state.getAttribute(CLASSIC_ZIP_FILE_NAME));

      return (String)getContext(data).get("template") + TEMPLATE[46];

    case 47:
      /*  buildContextForTemplate chef_siteInfo-importMtrlCopyConfirm.vm
      * 
      */      
      context.put("finalZipSites", state.getAttribute(FINAL_ZIP_IMPORT_SITES));

      return (String)getContext(data).get("template") + TEMPLATE[47];

    case 48:
    		/*  buildContextForTemplate chef_siteInfo-importMtrlCopyConfirm.vm
	     * 
	     */      
    		context.put("finalZipSites", state.getAttribute(FINAL_ZIP_IMPORT_SITES));
    		return (String)getContext(data).get("template") + TEMPLATE[48];
    case 49:
    		/*  buildContextForTemplate chef_siteInfo-group.vm
    		 * 
    		 */
    		context.put("site", site);
    		bar = new MenuImpl(portlet, data, (String) state.getAttribute(STATE_ACTION));
		if (SiteService.allowUpdateSite(site.getId()) || SiteService.allowUpdateGroupMembership(site.getId()))
		{
			bar.add( new MenuEntry(rb.getString("java.new"), "doGroup_new"));
		}
		context.put("menu", bar);
		
		// the group list
		sortedBy = (String) state.getAttribute (SORTED_BY);
		sortedAsc = (String) state.getAttribute (SORTED_ASC);
		
		if(sortedBy!=null) context.put ("currentSortedBy", sortedBy);
		if(sortedAsc!=null) context.put ("currentSortAsc", sortedAsc);
		
		// only show groups created by WSetup tool itself
		Collection groups = (Collection) site.getGroups();
		List groupsByWSetup = new Vector();
		for(Iterator gIterator = groups.iterator(); gIterator.hasNext();)
		{
			Group gNext = (Group) gIterator.next();
			String gProp = gNext.getProperties().getProperty(GROUP_PROP_WSETUP_CREATED);
			if (gProp != null && gProp.equals(Boolean.TRUE.toString()))
			{
				groupsByWSetup.add(gNext);
			}
		}
		if (sortedBy != null && sortedAsc != null)
		{
			context.put("groups", new SortedIterator (groupsByWSetup.iterator (), new SiteComparator (sortedBy, sortedAsc)));
		}
    		return (String)getContext(data).get("template") + TEMPLATE[49]; 
    case 50:
		/*  buildContextForTemplate chef_siteInfo-groupedit.vm
		 * 
		 */
    		Group g = getStateGroup(state);
    		if (g != null)
    		{
    			context.put("group", g);
    			context.put("newgroup", Boolean.FALSE);
    		}
    		else
    		{
    			context.put("newgroup", Boolean.TRUE);
    		}
		if (state.getAttribute(STATE_GROUP_TITLE) != null)
		{
			context.put("title", state.getAttribute(STATE_GROUP_TITLE));
		}
		if (state.getAttribute(STATE_GROUP_DESCRIPTION) != null)
		{
			context.put("description", state.getAttribute(STATE_GROUP_DESCRIPTION));
		}
		Iterator siteMembers = new SortedIterator (getParticipantList(state).iterator (), new SiteComparator (SORTED_BY_PARTICIPANT_NAME, Boolean.TRUE.toString()));
		if (siteMembers != null && siteMembers.hasNext())
		{
			context.put("generalMembers", siteMembers);
		}
		Set groupMembersSet = (Set) state.getAttribute(STATE_GROUP_MEMBERS);
		if (state.getAttribute(STATE_GROUP_MEMBERS) != null)
		{
			context.put("groupMembers", new SortedIterator(groupMembersSet.iterator(), new SiteComparator (SORTED_BY_MEMBER_NAME, Boolean.TRUE.toString())));
		}
		context.put("groupMembersClone", groupMembersSet);
		context.put("userDirectoryService", UserDirectoryService.getInstance());
		return (String)getContext(data).get("template") + TEMPLATE[50];
    case 51:
		/*  buildContextForTemplate chef_siteInfo-groupDeleteConfirm.vm
		 * 
		 */
		context.put("site", site);
		
		context.put("removeGroupIds", new ArrayList(Arrays.asList((String[])state.getAttribute(STATE_GROUP_REMOVE))));
		return (String)getContext(data).get("template") + TEMPLATE[51];
    }
    // should never be reached
    return (String)getContext(data).get("template") + TEMPLATE[0];

  } // buildContextForTemplate

  //htripath: import materials from classic
  /**
   * Master import -- for import materials from a  file 
   * @see case 45
   * 
   */
  public void doAttachmentsMtrlFrmFile(RunData data)
  {
    SessionState state =
    ((JetspeedRunData) data).getPortletSessionState(
    ((JetspeedRunData) data).getJs_peid());

    //state.setAttribute(FILE_UPLOAD_MAX_SIZE, ServerConfigurationService.getString("content.upload.max", "1"));
    state.setAttribute(STATE_TEMPLATE_INDEX, "45");
  } // doImportMtrlFrmFile
  
  /**
   * Handle File Upload request 
   * @see case 46
   * @throws Exception
   */
  public void doUploadMtrlFrmFile(RunData data)
  {
    SessionState state =
    ((JetspeedRunData) data).getPortletSessionState(
    ((JetspeedRunData) data).getJs_peid());
    
    List allzipList = new Vector();
    List finalzipList = new Vector();
    List directcopyList = new Vector();

    // see if the user uploaded a file
    FileItem file =data.getParameters().getFileItem("file");
    String fileName =Validator.getFileName(file.getFileName());
    if(!fileName.endsWith(".zip")){
      addAlert(state, "Please select zip file to upload and extract to your site");
    }
    boolean mappingFilePresent = false;
    String sakaiHome = System.getProperty("sakai.home");
    // Rashmi: should use IdManager in future to get guid
    String contextString = IdManager.createUuid();//ToolManager.getCurrentPlacement().getContext();
    
    File dir = new File(sakaiHome + "/archive/" + contextString.toString()); //directory where file would be saved
    if (!dir.exists())
    {
      dir.mkdirs();
    }

    //Store the Zip file
    byte[] bytes = file.get();
    String fpath = dir.getPath() + "/" + fileName;
    fpath = fpath.replace('\\', '/');
    
    try
    {
      FileOutputStream fos = new FileOutputStream(fpath);
      final int BSIZE = 1024;
      int bytesRemaining = bytes.length;

      for (int i = 0; i < bytes.length; i += BSIZE)
      {
        int nbytes = (BSIZE < bytesRemaining) ? BSIZE : bytesRemaining;
        fos.write(bytes, i, nbytes);
        bytesRemaining -= nbytes;
      }
      fos.close();
    }
    catch (Exception e4)
    {
      M_log.warn("chef-site import" +e4.getMessage());
    }
    
    // store import_mapping.xml file and Attachment type files
    try
    {
      ZipFile zip = new ZipFile(dir.getPath() + "/" + fileName);
      Enumeration entries = zip.entries();
      while (entries.hasMoreElements())
      {
        //store import_mapping.xml file to read for display
        ZipEntry entry = (ZipEntry) entries.nextElement();
        if (entry.getName().equals("import_mappings.xml"))
        {
          mappingFilePresent = true;
          InputStream entryStream = zip.getInputStream(entry);
          FileOutputStream ofile = new FileOutputStream(dir.getPath() + "/"
              + entry.getName());
          byte[] buffer = new byte[1024 * 10];
          int bytesRead;
          while ((bytesRead = entryStream.read(buffer)) != -1)
          {
            ofile.write(buffer, 0, bytesRead);
          }

          ofile.close();
          entryStream.close();
        }
        //store site.xml file to check user permission
        if (entry.getName().equals("site.xml"))
        {
          InputStream entryStream = zip.getInputStream(entry);
          FileOutputStream ofile = new FileOutputStream(dir.getPath() + "/"
              + entry.getName());
          byte[] buffer = new byte[1024 * 10];
          int bytesRead;
          while ((bytesRead = entryStream.read(buffer)) != -1)
          {
            ofile.write(buffer, 0, bytesRead);
          }

          ofile.close();
          entryStream.close();
        }
        
        //for attachment type files
        if (!(entry.getName().endsWith(".xml")))
        {
          File maindir = new File(sakaiHome + "/archive/"
              + contextString.toString() + "/source/");
          if (!maindir.exists())
          {
            maindir.mkdirs();

          }
          InputStream attentryStream = zip.getInputStream(entry);
          FileOutputStream attfile = new FileOutputStream(maindir.getPath() + "/"
              + entry.getName());

          byte[] attbuffer = new byte[1024 * 256];
          int attbytesRead;
          while ((attbytesRead = attentryStream.read(attbuffer)) != -1)
          {
            attfile.write(attbuffer, 0, attbytesRead);
          }
          attfile.close();
          attentryStream.close();
        }
      }
      zip.close();
    }
    catch (Exception e5)
    {
      M_log.warn("chef-site import" + e5.getMessage());
    }    
    //read site.xml for user permission
    File sitefile = new File(dir.getPath() + "/" + "site.xml");
    String sitepath = dir.getPath() + "/" + "site.xml";
    sitepath = sitepath.replace('\\', '/');
    Document sitedoc = null;
    DocumentBuilder sitedocBuilder;
    try
    {
      sitedocBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      InputStream fis;
      try
      {
        fis = new FileInputStream(sitepath);
        try
        {
          sitedoc = (Document) sitedocBuilder.parse(fis);
        }
        catch (SAXException e3)
        {
          M_log.warn("chef-site import" + e3.getMessage());
        }
      }
      catch (FileNotFoundException e1)
      {
        M_log.warn("chef-site import" + e1.getMessage());
      }
      catch (IOException e2)
      {
        M_log.warn("chef-site import" + e2.getMessage());
      }
    }
    catch (ParserConfigurationException e)
    {
      M_log.warn("chef-site import" + e.getMessage());
    }
    if (sitedoc != null)
    {
      String sessionUserId = UserDirectoryService.getCurrentUser().getId();
      if(!(ImportMetadataService.hasMaintainRole(sessionUserId, sitedoc))){
        addAlert(state, "You don't have permission to import material from file");
  			state.setAttribute(STATE_TEMPLATE_INDEX, "45");
  			return;
      }

    }
    // read the import_mapping.xml file
    File mappingfile = new File(dir.getPath() + "/" + "import_mappings.xml");
    String absolutepath = dir.getPath() + "/" + "import_mappings.xml";
    absolutepath = absolutepath.replace('\\', '/');
    Document doc = null;
    DocumentBuilder docBuilder;
    try
    {
      docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      InputStream fis;
      try
      {
        fis = new FileInputStream(absolutepath);
        try
        {
          doc = (Document) docBuilder.parse(fis);
        }
        catch (SAXException e3)
        {
          M_log.warn("chef-site import" + e3.getMessage());
        }
      }
      catch (FileNotFoundException e1)
      {
        M_log.warn("chef-site import" + e1.getMessage());
      }
      catch (IOException e2)
      {
        M_log.warn("chef-site import" + e2.getMessage());
      }
    }
    catch (ParserConfigurationException e)
    {
      M_log.warn("chef-site import" + e.getMessage());
    }
    if (doc != null)
    {
      List lst = ImportMetadataService.getImportMetadataElements(doc);
      if (lst != null && lst.size() > 0)
      {
        Iterator iter = lst.iterator();
        while (iter.hasNext())
        {
          ImportMetadata importdata = (ImportMetadata) iter.next();
          if ((!importdata.isMandatory())
              && (importdata.getFileName().endsWith(".xml")))
          {
            allzipList.add(importdata);
          }
          else
          {
            directcopyList.add(importdata);
          }
        }
      }
    }
    //htripath- Sep02 - option to select only tools available IM257143
    List toolzipList = new Vector();
    List pageList=new Vector();
    String siteId=ToolManager.getCurrentPlacement().getContext(); 
    Site site=null;
    try
    {
      site=SiteService.getSite(siteId);
      pageList=site.getPages();
    }
    catch (IdUnusedException e1)
    {
      M_log.warn("chef-site import" + e1.getMessage());
    }
    //create toolzipList
    for (Iterator iter = allzipList.iterator(); iter.hasNext();)
    {
      boolean toolpresent=false ;
      ImportMetadata zipelement = (ImportMetadata) iter.next();
      for (Iterator iterator = pageList.iterator(); iterator.hasNext();)
      {
        SitePage pgelement = (SitePage) iterator.next();
        if (pgelement.getTitle().equals(zipelement.getSakaiTool())){
          toolpresent=true;
        }
      }
      if(toolpresent){
        toolzipList.add(zipelement) ;
      }      
    }

    state.setAttribute(ALL_ZIP_IMPORT_SITES, toolzipList);
    //set Attributes
    //state.setAttribute(ALL_ZIP_IMPORT_SITES, allzipList);
    state.setAttribute(FINAL_ZIP_IMPORT_SITES, finalzipList);
    state.setAttribute(DIRECT_ZIP_IMPORT_SITES, directcopyList);
    state.setAttribute(CLASSIC_ZIP_FILE_NAME, fileName);
    state.setAttribute(SESSION_CONTEXT_ID, contextString.toString());

    state.setAttribute(STATE_TEMPLATE_INDEX, "46");
  } // doImportMtrlFrmFile

  /**
   * Handle addition to list request
   * @param data
   */
  public void doAdd_MtrlSite(RunData data)
  {
    SessionState state =
    ((JetspeedRunData) data).getPortletSessionState(
    ((JetspeedRunData) data).getJs_peid());
    ParameterParser params = data.getParameters();
    
    List zipList = (List) state.getAttribute(ALL_ZIP_IMPORT_SITES);
    List fnlList = (List) state.getAttribute(FINAL_ZIP_IMPORT_SITES);
    List importSites = new ArrayList(Arrays.asList(params
        .getStrings("addImportSelected")));

    for (int i = 0; i < importSites.size(); i++)
    {
      String value = (String) importSites.get(i);
      fnlList.add(removeItems(value, zipList));
    }

    state.setAttribute(ALL_ZIP_IMPORT_SITES, zipList);
    state.setAttribute(FINAL_ZIP_IMPORT_SITES, fnlList);

    state.setAttribute(STATE_TEMPLATE_INDEX, "46");
  } // doAdd_MtrlSite

  /**
   * Helper class for Add and remove
   * @param value
   * @param items
   * @return
   */
  public ImportMetadata removeItems(String value, List items)
  {
    ImportMetadata result = null;
    for (int i = 0; i < items.size(); i++)
    {
      ImportMetadata item = (ImportMetadata) items.get(i);
      if (value.equals(item.getId()))
      {
        result = (ImportMetadata) items.remove(i);
        break;
      }
    }
    return result;
  }

  /**
   * Handle the request for remove
   * @param data
   */
  public void doRemove_MtrlSite(RunData data)
  {
    SessionState state =
    ((JetspeedRunData) data).getPortletSessionState(
    ((JetspeedRunData) data).getJs_peid());
    ParameterParser params = data.getParameters();

    List zipList = (List) state.getAttribute(ALL_ZIP_IMPORT_SITES);
    List fnlList = (List) state.getAttribute(FINAL_ZIP_IMPORT_SITES);

    List importSites = new ArrayList(Arrays.asList(params
        .getStrings("removeImportSelected")));

    for (int i = 0; i < importSites.size(); i++)
    {
      String value = (String) importSites.get(i);
      zipList.add(removeItems(value, fnlList));
    }

    state.setAttribute(ALL_ZIP_IMPORT_SITES, zipList);
    state.setAttribute(FINAL_ZIP_IMPORT_SITES, fnlList);

    state.setAttribute(STATE_TEMPLATE_INDEX, "46");
  } // doAdd_MtrlSite

  /**
   * Handle the request for copy
   * @param data
   */
  public void doCopyMtrlSite(RunData data)
  {
    SessionState state =
    ((JetspeedRunData) data).getPortletSessionState(
    ((JetspeedRunData) data).getJs_peid());

    List fnlList = (List) state.getAttribute(FINAL_ZIP_IMPORT_SITES);
    state.setAttribute(FINAL_ZIP_IMPORT_SITES, fnlList);

    state.setAttribute(STATE_TEMPLATE_INDEX, "47");
  } // doCopy_MtrlSite

  /**
   * Handle the request for Save
   * @param data
   */
  public void doSaveMtrlSite(RunData data)
  {
    SessionState state =
    ((JetspeedRunData) data).getPortletSessionState(
    ((JetspeedRunData) data).getJs_peid());

    List fnlList = (List) state.getAttribute(FINAL_ZIP_IMPORT_SITES);
    List directList = (List) state.getAttribute(DIRECT_ZIP_IMPORT_SITES);

    String sakaiHome = System.getProperty("sakai.home");
    String contextString = (String) state.getAttribute(SESSION_CONTEXT_ID);
    
    File dir = new File(sakaiHome + "/archive/" + contextString + "/source/"); //directory where file would be saved
    if (!dir.exists())
    {
      dir.mkdirs();
    }
    //FileItem zf = (FileItem) state.getAttribute(CLASSIC_ZIP_FILE_NAME);
    String fileName=(String)state.getAttribute(CLASSIC_ZIP_FILE_NAME);
    String upZipfile = sakaiHome + "/archive/" + contextString + "/"
        + fileName;
    try
    {
      ZipFile zip = new ZipFile(upZipfile);
      for (int i = 0; i < directList.size(); i++)
      {
        ImportMetadata impvalue = (ImportMetadata) directList.get(i);
        String value = impvalue.getFileName();
        ZipEntry entry = zip.getEntry(value);
        if (entry != null)
        {
          InputStream entryStream = zip.getInputStream(entry);
          try
          {
            FileOutputStream file = new FileOutputStream(dir.getPath() + "/"
                + entry.getName());
            try
            {
              byte[] buffer = new byte[1024 * 10];
              int bytesRead;
              while ((bytesRead = entryStream.read(buffer)) != -1)
              {
                file.write(buffer, 0, bytesRead);
              }
              file.close();
            }
            catch (IOException ioe)
            {
              M_log.warn("chef-site import" + ioe.getMessage());
              return;
            }
          }
          catch (IOException ioe)
          {
            M_log.warn("chef-site import" + ioe.getMessage());
            return;
          }
          entryStream.close();
        }
      } //for directList loop

      //copy user selected files
      for (int i = 0; i < fnlList.size(); i++)
      {
        ImportMetadata impvalue = (ImportMetadata) fnlList.get(i);
        String value = impvalue.getFileName();
        ZipEntry entry = zip.getEntry(value);

        if (entry != null)
        {
          InputStream entryStream = zip.getInputStream(entry);
          try
          {
            FileOutputStream file = new FileOutputStream(dir.getPath() + "/"
                + entry.getName());
            try
            {
              byte[] buffer = new byte[1024 * 10];
              int bytesRead;
              while ((bytesRead = entryStream.read(buffer)) != -1)
              {
                file.write(buffer, 0, bytesRead);
              }
              file.close();
            }
            catch (IOException ioe)
            {
              M_log.warn("chef-site import" + ioe.getMessage());
              return;
            }
          }
          catch (IOException ioe)
          {
            M_log.warn("chef-site import" + ioe.getMessage());
            return;
          }
          entryStream.close();
        }
      } //for fnlList loop

      //doImport
      String id = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);
      String folder = contextString.toString() + "/source/";
      ArchiveService.merge(folder, id, null);
      zip.close();
    }
    catch (IOException ioe)
    {
      M_log.warn("chef-site import" + ioe.getMessage());
      return;
    }

    //delete the directory
    dir.deleteOnExit();
    state.setAttribute(STATE_TEMPLATE_INDEX, "48");
    
    //state.setAttribute(STATE_TEMPLATE_INDEX, "28");
  } // doCopy_MtrlSite
  
  public void doSaveMtrlSiteMsg(RunData data)
  {
    SessionState state =
    ((JetspeedRunData) data).getPortletSessionState(
    ((JetspeedRunData) data).getJs_peid());
    
    //remove attributes
    state.removeAttribute(ALL_ZIP_IMPORT_SITES);
    state.removeAttribute(FINAL_ZIP_IMPORT_SITES);
    state.removeAttribute(DIRECT_ZIP_IMPORT_SITES);
    state.removeAttribute(CLASSIC_ZIP_FILE_NAME);
    state.removeAttribute(SESSION_CONTEXT_ID);    

    state.setAttribute(STATE_TEMPLATE_INDEX, "12");
    
  }
  //htripath-end
	
	/** 
	* Handle the site search request.
	**/	
	public void doSite_search(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		
		// read the search form field into the state object
		String search = StringUtil.trimToNull(data.getParameters().getString(FORM_SEARCH));

		// set the flag to go to the prev page on the next list
		if (search == null)
		{
			state.removeAttribute(STATE_SEARCH);
		}
		else
		{
			state.setAttribute(STATE_SEARCH, search);
		}

	}	// doSite_search
	
	/** 
	* Handle a Search Clear request.
	**/	
	public void doSite_search_clear(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		
		// clear the search
		state.removeAttribute(STATE_SEARCH);

	}	// doSite_search_clear
	
	private  void coursesIntoContext(SessionState state, Context context, Site site)
	{
		List providerCourseList = getProviderCourseList(StringUtil.trimToNull(getExternalRealmId(state)));
		if (providerCourseList != null && providerCourseList.size() > 0)
		{
			state.setAttribute(SITE_PROVIDER_COURSE_LIST, providerCourseList);
			context.put("providerCourseList", providerCourseList);
		}
		String manualCourseListString = StringUtil.trimToNull(site.getProperties().getProperty(PROP_SITE_REQUEST_COURSE));
		if (manualCourseListString != null)
		{
			List manualCourseList = new Vector();
			if (manualCourseListString.indexOf("+") != -1)
			{
				manualCourseList = new ArrayList(Arrays.asList(manualCourseListString.split("\\+")));
			}
			else
			{
				manualCourseList.add(manualCourseListString);
			}
			state.setAttribute(SITE_MANUAL_COURSE_LIST, manualCourseList);
			context.put("manualCourseList", manualCourseList);
		}
	}
	
	/**
	* buildInstructorSectionsList
	* Build the CourseListItem list for this Instructor for the requested Term
	*
	*/
	private void buildInstructorSectionsList(SessionState state, ParameterParser params, Context context)
	{
		//Site information
		// The sections of the specified term having this person as Instructor
		context.put ("providerCourseSectionList", state.getAttribute("providerCourseSectionList"));
		context.put ("manualCourseSectionList", state.getAttribute("manualCourseSectionList"));
		context.put ("term", (Term) state.getAttribute(STATE_TERM_SELECTED));
		context.put ("termList", CourseManagementService.getTerms());
		context.put(STATE_TERM_COURSE_LIST, (List) state.getAttribute(STATE_TERM_COURSE_LIST));
		context.put("tlang",rb);
	} // buildInstructorSectionsList
	
	/**
	* getProviderCourseList
	* a course site/realm id in one of three formats,
	* for a single section, for multiple sections of the same course, or
	* for a cross-listing having multiple courses. getProviderCourseList
	* parses a realm id into year, term, campus_code, catalog_nbr, section components.
	* @param id is a String representation of the course realm id (external id).
	*/
	private List getProviderCourseList(String id)
	{
		Vector rv = new Vector();
		if(id == null || id == NULL_STRING)
		{
			return rv;
		}
		String course_part = NULL_STRING;
		String section_part = NULL_STRING;
		String key = NULL_STRING;
		try
		{
			//Break Provider Id into course_nbr parts
			List course_nbrs = new ArrayList(Arrays.asList(id.split("\\+")));
		
			//Iterate through course_nbrs
			for (ListIterator i = course_nbrs.listIterator(); i.hasNext(); )
			{
				String course_nbr = (String) i.next();
			
				//Course_nbr pattern will be for either one section or more than one section
				if (course_nbr.indexOf("[") == -1)
				{
					// This course_nbr matches the pattern for one section
					try
					{
						rv.add(course_nbr);
					}
					catch (Exception e)
					{
						M_log.warn(this + ": cannot find class " + course_nbr);
					}
				}
				else
				{
					// This course_nbr matches the pattern for more than one section
					course_part = course_nbr.substring(0, course_nbr.indexOf("[")); // includes trailing ","
					section_part = course_nbr.substring(course_nbr.indexOf("[")+1, course_nbr.indexOf("]"));
					String[] sect = section_part.split(",");
					for (int j = 0; j < sect.length; j++)
					{
						key = course_part + sect[j];
						try
						{
							rv.add(key);
						}
						catch (Exception e)
						{
							M_log.warn(this + ": cannot find class " + key);
						}
					}	
				}
			}
		}
		catch (Exception ee)
		{
			M_log.warn(ee.getMessage());
		}
		return rv;
	
	} // getProviderCourseList
	
	/**
	 * {@inheritDoc}
	 */
	protected int sizeResources(SessionState state)
	{
		int size = 0;
		String search = "";
		String userId = SessionManager.getCurrentSessionUserId();
		
		//if called from the site list page
		if(((String)state.getAttribute(STATE_TEMPLATE_INDEX)).equals("0"))
		{
			search = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH));
			if (SecurityService.isSuperUser())
			{
				// admin-type of user
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null)
				{
					if (view.equals(ALL_MY_SITES))
					{
						//search for non-user sites, using the criteria
						size = SiteService.countSites(org.sakaiproject.site.api.SiteService.SelectionType.NON_USER, null, search, null);
					}
					else if (view.equals(MYWORKSPACE))
					{	
						//search for a specific user site for the particular user id in the criteria - exact match only
						try
						{
							SiteService.getSite(SiteService.getUserSiteId(search));
							size++;
						}
						catch (IdUnusedException e) {}
					}
					else if (view.equalsIgnoreCase(GRADTOOLS))
					{	
						//search for gradtools sites
						size = SiteService.countSites(org.sakaiproject.site.api.SiteService.SelectionType.NON_USER, state.getAttribute(GRADTOOLS_SITE_TYPES), search, null);
					}
					else
					{	
						//search for specific type of sites
						size = SiteService.countSites(org.sakaiproject.site.api.SiteService.SelectionType.NON_USER, view, search, null);
					}
				}
			}
			else
			{
				Site userWorkspaceSite = null;
				try
				{
					userWorkspaceSite = SiteService.getSite(SiteService.getUserSiteId(userId));
				}
				catch (IdUnusedException e) 
				{
					M_log.warn("Cannot find user " + SessionManager.getCurrentSessionUserId() + "'s My Workspace site.");
				}
				
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view!= null)
				{
					if (view.equals(ALL_MY_SITES))
					{
						view = null;
						//add my workspace if any
						if (userWorkspaceSite != null)
						{
							if (search!=null)
							{
								if (userId.indexOf(search) != -1)
								{
									size++;
								}
							}
							else
							{
								size++;
							}
						}
						size += SiteService.countSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, search, null);
					}
					else if (view.equalsIgnoreCase(GRADTOOLS))
					{	
						//search for gradtools sites
						size += SiteService.countSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, state.getAttribute(GRADTOOLS_SITE_TYPES), search, null);
					}
					else
					{
						// search for specific type of sites
						size += SiteService.countSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, view, search, null);
					}
				}
			}
		}
		//for SiteInfo list page
		else if (state.getAttribute(STATE_TEMPLATE_INDEX).toString().equals("12"))
		{
			List l = (List) state.getAttribute(STATE_PARTICIPANT_LIST); 
			size = (l!= null)?l.size():0;
		}
		// if this is about site list page 
		else if (state.getAttribute(STATE_TEMPLATE_INDEX).toString().equals("31"))
		{
			// search?
			search = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH));
			
			size = SiteService.countSites(org.sakaiproject.site.api.SiteService.SelectionType.ANY,
					state.getAttribute(STATE_SEARCH_SITE_TYPE), search, (HashMap)state.getAttribute(STATE_PROP_SEARCH_MAP));
		}
		// TODO: mode for participants list is needed
		else if (state.getAttribute(STATE_TEMPLATE_INDEX).toString().equals("32"))
		{
			size = getParticipantList(state).size();
		}
		return size;
		
	} // sizeResources
	
	/**
	 * {@inheritDoc}
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		String search = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH));

		//if called from the site list page
		if(((String)state.getAttribute(STATE_TEMPLATE_INDEX)).equals("0"))
		{
			// get sort type
			SortType sortType = null;
			String sortBy = (String) state.getAttribute(SORTED_BY);
			boolean sortAsc = (new Boolean((String) state.getAttribute(SORTED_ASC))).booleanValue();
			if (sortBy.equals(SortType.TITLE_ASC.toString()))
			{
				sortType=sortAsc?SortType.TITLE_ASC:SortType.TITLE_DESC;
			}
			else if (sortBy.equals(SortType.TYPE_ASC.toString()))
			{
				sortType=sortAsc?SortType.TYPE_ASC:SortType.TYPE_DESC;
			}
			else if (sortBy.equals(SortType.CREATED_BY_ASC.toString()))
			{
				sortType=sortAsc?SortType.CREATED_BY_ASC:SortType.CREATED_BY_DESC;
			}
			else if (sortBy.equals(SortType.CREATED_ON_ASC.toString()))
			{
				sortType=sortAsc?SortType.CREATED_ON_ASC:SortType.CREATED_ON_DESC;
			}
			else if (sortBy.equals(SortType.PUBLISHED_ASC.toString()))
			{
				sortType=sortAsc?SortType.PUBLISHED_ASC:SortType.PUBLISHED_DESC;
			}
			
			if (SecurityService.isSuperUser())
			{
				// admin-type of user
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view != null)
				{
					if (view.equals(ALL_MY_SITES))
					{
						//search for non-user sites, using the criteria
						return SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.NON_USER,
								null, search, null, sortType, new PagingPosition(first, last));
					}
					else if (view.equalsIgnoreCase(MYWORKSPACE))
					{	
						//search for a specific user site for the particular user id in the criteria - exact match only
						List rv = new Vector();
						try
						{
							Site userSite = SiteService.getSite(SiteService.getUserSiteId(search));
							rv.add(userSite);
						}
						catch (IdUnusedException e) {}
	
						return rv;
					}
					else if (view.equalsIgnoreCase(GRADTOOLS))
					{	
						//search for gradtools sites
						return SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.NON_USER,
								state.getAttribute(GRADTOOLS_SITE_TYPES), search, null, sortType, new PagingPosition(first, last));
	
					}
					else
					{	
						//search for a specific site
						return SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ANY,
								view, search, null, sortType, new PagingPosition(first, last));
					}
				}
			}
			else
			{
				List rv = new Vector();
				Site userWorkspaceSite = null;
				String userId = SessionManager.getCurrentSessionUserId();
				
				try
				{
					userWorkspaceSite = SiteService.getSite(SiteService.getUserSiteId(userId));
				}
				catch (IdUnusedException e) 
				{
					M_log.warn("Cannot find user " + SessionManager.getCurrentSessionUserId() + "'s My Workspace site.");
				}
				
				String view = (String) state.getAttribute(STATE_VIEW_SELECTED);
				if (view!= null)
				{
					if (view.equals(ALL_MY_SITES))
					{
						view = null;
						//add my workspace if any
						if (userWorkspaceSite != null)
						{
							if (search!=null)
							{
								if (userId.indexOf(search) != -1)
								{
									rv.add(userWorkspaceSite);
								}
							}
							else
							{
								rv.add(userWorkspaceSite);
							}
						}
						rv.addAll(SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
								null, search, null, sortType, new PagingPosition(first, last)));
					}
					else if (view.equalsIgnoreCase(GRADTOOLS))
					{	
						//search for a specific user site for the particular user id in the criteria - exact match only
						rv.addAll(SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
								state.getAttribute(GRADTOOLS_SITE_TYPES), search, null, sortType, new PagingPosition(first, last)));

					}
					else
					{
						rv.addAll(SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
								view, search, null, sortType, new PagingPosition(first, last)));
					}
				}
				
				return rv;
				
			}
		}
		//if in Site Info list view
		else if (state.getAttribute(STATE_TEMPLATE_INDEX).toString().equals("12"))
		{
			List participants = (state.getAttribute(STATE_PARTICIPANT_LIST) != null)?(List) state.getAttribute(STATE_PARTICIPANT_LIST):new Vector();
			PagingPosition page = new PagingPosition(first, last);
			page.validate(participants.size());
			participants = participants.subList(page.getFirst()-1, page.getLast());
			
			return participants;
		}
		// if this is about sitemanage site list page 
		else if (state.getAttribute(STATE_TEMPLATE_INDEX).toString().equals("31"))
		{

			search = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH));
			return SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ANY,
							state.getAttribute(STATE_SEARCH_SITE_TYPE), search, 
							(HashMap)state.getAttribute(STATE_PROP_SEARCH_MAP), SortType.TITLE_ASC,
							new PagingPosition(first, last));
		
		}
		// if this is for sitemanage participants list
		else if (state.getAttribute(STATE_TEMPLATE_INDEX).toString().equals("32"))
		{
			List rv = getParticipantList(state);
			PagingPosition page = new PagingPosition(first, last);
			page.validate(rv.size());
			rv = rv.subList(page.getFirst()-1, page.getLast());
			
			return rv;
		}
		
		return null;
		
	} // readResourcesPage
		
	/**
	 * get the selected tool ids from import sites
	 */
	private void select_import_tools(ParameterParser params, SessionState state)
	{
		Hashtable importTools = new Hashtable();
		
		// the tools for current site
		List selectedTools = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST); // String toolId's
		for (int i=0; i<selectedTools.size(); i++)
		{
			// any tools chosen from import sites?
			String toolId = (String) selectedTools.get(i);
			if (params.getStrings(toolId) != null)
			{
				importTools.put(toolId, new ArrayList(Arrays.asList(params.getStrings(toolId))));
			}
		}
		
		state.setAttribute(STATE_IMPORT_SITE_TOOL, importTools);
		
	}	// select_import_tools
	
	/**
	 * Is it from the ENW edit page?
	 * @return ture if the process went through the ENW page; false, otherwise
	 */
	private boolean fromENWModifyView(SessionState state)
	{
		boolean fromENW = false;
		List oTools = (List) state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		
		List toolList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		for (int i=0; i<toolList.size() && !fromENW; i++)
		{
			String toolId = (String) toolList.get(i);
			if (toolId.equals("sakai.mailbox") || toolId.indexOf("sakai.news") != -1 || toolId.indexOf("sakai.iframe") != -1)
			{
				if (oTools == null)
				{
					// if during site creation proces
					fromENW = true;
				}
				else if (!oTools.contains(toolId))
				{
					//if user is adding either EmailArchive tool, News tool or Web Content tool, go to the Customize page for the tool
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
	public void doNew_site ( RunData data )
		throws Exception
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// start clean
		cleanState(state);
		
		List siteTypes = (List) state.getAttribute(STATE_SITE_TYPES);
		if (siteTypes != null)
		{
			if (siteTypes.size() == 1)
			{
				String siteType = (String) siteTypes.get(0);
				if (!siteType.equals(ServerConfigurationService.getString("courseSiteType", "")))
				{
					// if only one site type is allowed and the type isn't course type
					// skip the select site type step
					setNewSiteType(state, siteType);
					state.setAttribute (STATE_TEMPLATE_INDEX, "2");
				}
				else
				{
					state.setAttribute (STATE_TEMPLATE_INDEX, "1");
				}
			}
			else
			{
				state.setAttribute (STATE_TEMPLATE_INDEX, "1");
			}
		}
		
	}	// doNew_site
	
	/**
	* doMenu_site_delete is called when the Site list tool bar Delete button is clicked
	* 
	*/
	public void doMenu_site_delete ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		if (params.getStrings ("selectedMembers") == null)
		{
			addAlert(state, NO_SITE_SELECTED_STRING);
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			return;
		}
		String[] removals = (String[]) params.getStrings ("selectedMembers");
		state.setAttribute(STATE_SITE_REMOVALS, removals );
		
		//present confirm delete template
		state.setAttribute(STATE_TEMPLATE_INDEX, "8");
		
	} // doMenu_site_delete
	
	/**
	* doSitemanage_site_delete is called when the Site list tool bar Delete button is clicked
	* 
	*/
	public void doSitemanage_site_delete ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		//present confirm delete template
		state.setAttribute(STATE_TEMPLATE_INDEX, "40");
		
	} // doSitemanage_site_delete
	
	public void doSite_delete_confirmed ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		if (params.getStrings ("selectedMembers") == null)
		{
			M_log.warn("SiteAction.doSite_delete_confirmed selectedMembers null");
			state.setAttribute(STATE_TEMPLATE_INDEX, "0"); // return to the site list
			return;
		}
		List chosenList = new ArrayList(Arrays.asList(params.getStrings ("selectedMembers"))); // Site id's of checked sites
		if(!chosenList.isEmpty())
		{
			for (ListIterator i = chosenList.listIterator(); i.hasNext(); )
			{
				String id = (String)i.next();
				String site_title = NULL_STRING;
				try
				{
					site_title = SiteService.getSite(id).getTitle();
				}
				catch (IdUnusedException e)
				{
					M_log.warn("SiteAction.doSite_delete_confirmed - IdUnusedException " + id);
					addAlert(state,rb.getString("java.sitewith") + " " + id + " "+ rb.getString("java.couldnt")+" ");
				}
				if(SiteService.allowRemoveSite(id))
				{
					
					try
					{
						Site site = SiteService.getSite(id);
						site_title = site.getTitle();					
						SiteService.removeSite(site);
					}
					catch (IdUnusedException e)
					{
						M_log.warn("SiteAction.doSite_delete_confirmed - IdUnusedException " + id);
						addAlert(state, rb.getString("java.sitewith")+" " +  site_title + "(" + id + ") "+ rb.getString("java.couldnt")+" ");
					}
					catch (PermissionException e)
					{
						M_log.warn("SiteAction.doSite_delete_confirmed -  PermissionException, site " +  site_title + "(" + id + ").");
						addAlert(state, site_title + " "+ rb.getString("java.dontperm")+" ");
					}
				}
				else
				{
					M_log.warn("SiteAction.doSite_delete_confirmed -  allowRemoveSite failed for site " + id);
					addAlert(state, site_title + " "+ rb.getString("java.dontperm") +" ");
				}
			}
		}
		state.setAttribute(STATE_TEMPLATE_INDEX, "0"); // return to the site list

		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		schedulePeerFrameRefresh("sitenav");
		
	} // doSite_delete_confirmed

	/*
	 * Handles the quest of confirming site deletion in Sitemanage
	 */
	public void doSitemanage_delete_confirmed ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		String id = (String) state.getAttribute("siteId");

		if(SiteService.allowRemoveSite(id))
		{
			String title = "";
			try
			{
				title = SiteService.getSite(id).getTitle();
				Site site = getStateSite(state);
				SiteService.removeSite(site);
				state.removeAttribute("siteId");
				state.removeAttribute(STATE_SITE_INSTANCE_ID);
			
			}
			catch (IdUnusedException e)
			{
				M_log.warn("SiteAction.doSite_sitemanage_delete_confirmed - IdUnusedException " + id);
				addAlert(state, rb.getString("java.sitewith")+" " + id + " "+rb.getString("java.couldnt") +" ");
			}
			catch (PermissionException e)
			{
				M_log.warn("SiteAction.doSite_sitemanage_delete_confirmed -  PermissionException, site " + id);
				addAlert(state, title + " "+ rb.getString("java.dontperm")+" ");
			}
		}
		else
		{
			M_log.warn("SiteAction.doSitemanage_delete_confirmed -  allowRemoveSite failed for site " + id);
			addAlert(state, id + " "+rb.getString("java.dontperm")+" ");
		}

		state.setAttribute(STATE_TEMPLATE_INDEX, "31"); // return to the site list

	} // doSitemanage_delete_confirmed

	/**
	* Go into saveas mode
	*/
	public void doSitemanage_saveas_request(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readSiteForm(data, state)) return;

		// go to saveas mode
		state.setAttribute(STATE_TEMPLATE_INDEX, "41");

	}	// doSaveas_request
	
	/**
	* Read the site form and update the site in state.
	* @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	*/
	private boolean readSiteForm(RunData data, SessionState state)
	{
		Site Site = getStateSite(state);
		ResourcePropertiesEdit siteProperties = Site.getPropertiesEdit();
		
		ParameterParser params = data.getParameters ();
		
		String title = StringUtil.trimToZero(params.getString("title"));
		Site.setTitle(title);
							
		String type = StringUtil.trimToZero(params.getString("type"));
		if (!(type.equals(""))){Site.setType(type);}
		
		String description = StringUtil.trimToNull(params.getString("description"));
		Site.setDescription(description);
		
		String short_description = StringUtil.trimToNull(params.getString("short_description"));
		Site.setShortDescription(short_description);
		
		// "skin" will be the icon from the list (course sites), or we have the icon as a full URL
		String skin = StringUtil.trimToNull(params.getString("skin"));
		if (skin != null)
		{
			setAppearance(state, Site, skin);
		}
		else
		{
			String iconUrl = StringUtil.trimToNull(params.getString("icon"));
			Site.setIconUrl(iconUrl);
		}

		String include = StringUtil.trimToNull(params.getString("include"));	
		boolean pubview = true;
		if (include != null && include.equalsIgnoreCase(Boolean.FALSE.toString()))
		{
			pubview = false;
		}
		Site.setPubView(pubview);			
		
		String contactName = StringUtil.trimToZero(params.getString ("siteContactName"));
		siteProperties = Site.getPropertiesEdit();
		siteProperties.addProperty(PROP_SITE_CONTACT_NAME, contactName);
		
		String email = StringUtil.trimToZero(params.getString ("siteContactEmail"));
		String[] parts = email.split("@");
		if(email.length() > 0 && (email.indexOf("@") == -1 || parts.length != 2 || parts[0].length() == 0 || !Validator.checkEmailLocal(parts[0])))
		{
			// invalid email
			addAlert(state, email + " "+rb.getString("java.invalid") + INVALID_EMAIL);
			return false;
		}
		else
		{
			siteProperties.addProperty(PROP_SITE_CONTACT_EMAIL, email);
		}
					
		// for site size limit
		String size = params.getString("size");
		if (size != null)
		{
			String currentSiteId = ToolManager.getCurrentPlacement().getContext();
			String rootCollectionId = ContentHostingService.getSiteCollection(currentSiteId);
			
			ContentCollectionEdit cedit = null;
			try 
			{
				cedit = ContentHostingService.editCollection(rootCollectionId);
			}
			catch (IdUnusedException e) 
			{
				try 
				{
					cedit = ContentHostingService.addCollection(rootCollectionId);
				}
				catch (Exception err) {}
			} 
			catch (TypeException e) {}
			catch (PermissionException e){}
			catch (InUseException e)
			{
				addAlert(state, rb.getString("java.someone"));
				return false;
			}

			if (cedit != null)
			{
				ResourcePropertiesEdit pedit = cedit.getPropertiesEdit();
							
				// default 1 GB = 1,048,576 Kilobyte
				String quota = "1048576";
						
				if (size.equals(DEFAULT_SITE_SIZE_LIMIT))
				{
					state.setAttribute(STATE_SITE_SIZE_DEFAULT_SELECT, Boolean.TRUE);
					// set the quota
					pedit.addProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA, quota);
				}
				else
				{
					String otherSize = StringUtil.trimToZeroLower(params.getString("otherSize"));
					if (otherSize.equals(""))
					{
						addAlert(state, rb.getString("java.pleasech"));
						state.setAttribute(STATE_SITE_SIZE_DEFAULT_SELECT, Boolean.FALSE);
					}
					else
					{
						String[] strings = null;
						long unit = 1;
						if (otherSize.endsWith("kb")){strings = StringUtil.splitFirst(otherSize,"kb");}
						else if (otherSize.endsWith("kilobytes")){strings = StringUtil.splitFirst(otherSize,"kilobytes");}
						else if (otherSize.endsWith("kilobyte")){strings = StringUtil.splitFirst(otherSize,"kilobyte");}
						else if (otherSize.endsWith("mb")){strings = StringUtil.splitFirst(otherSize,"mb"); unit = 1024;}
						else if (otherSize.endsWith("megabytes")){strings = StringUtil.splitFirst(otherSize,"megabytes"); unit = 1024;}
						else if (otherSize.endsWith("megabyte")){strings = StringUtil.splitFirst(otherSize,"megabytes"); unit = 1024;}
						else if (otherSize.endsWith("gb")){strings = StringUtil.splitFirst(otherSize,"gb"); unit = 1048576;}
						else if (otherSize.endsWith("gigabytes")){strings = StringUtil.splitFirst(otherSize,"gigabytes"); unit = 1048576;}
						else if (otherSize.endsWith("gigabyte")){strings = StringUtil.splitFirst(otherSize,"gigabyte"); unit = 1048576;}
									
						if (strings != null)
						{
							try
							{
								// strings{digital strings, size unit "kb/mb/gb"}
								int intSize = Integer.parseInt(strings[0]);
								unit = intSize * unit; // size is transferred to KB
								pedit.addProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA, (new Long(unit)).toString());										
							}
							catch(NumberFormatException error)
							{
								addAlert(state, rb.getString("java.pleaseval"));
							}
						}
						else
						{
							addAlert(state, rb.getString("java.pleaseval"));
						}
					} // if-else
				} // if-else
			}
			
			if (state.getAttribute(STATE_MESSAGE) != null)
			{
				return false;
			}
			
		} // if size not equals to null

		return true;

	}	// readSiteForm
	
	/**
	* Handle a request to save-as the site as a new site.
	*/
	public void doSitemanage_saveas(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		// read the form
		String id = data.getParameters().getString("id");

		// get the site to copy from
		Site Site = getStateSite(state);
		try
		{
			// make a new site with this id and as a structural copy of site
			SiteService.addSite(id, Site);
		}
		catch (IdUsedException e)
		{
			addAlert(state,  rb.getString("java.inuse"));
			return;
		}
		catch (IdInvalidException e)
		{
			addAlert(state,  rb.getString("java.idinvalid"));
			return;
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("java.nocreate"));
			return;
		}

		// return to sitemanage site list mode
		state.removeAttribute(STATE_SITE_INSTANCE_ID);
		state.removeAttribute("siteId");
		
		//return to sitemanage site list view
		state.setAttribute (STATE_TEMPLATE_INDEX, "31");

		// make sure auto-updates are enabled
		enableObserver(state);

		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		schedulePeerFrameRefresh("sitenav");

	}	// doSitemanage_saveas

	/**
	 * get the Site object based on SessionState attribute values
	 * @return Site object related to current state; null if no such Site object could be found
	 */
	protected Site getStateSite(SessionState state)
	{
		Site site = null;
		
		if (state.getAttribute(STATE_SITE_INSTANCE_ID) != null)
		{
			try
			{
				site = SiteService.getSite((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
			}
			catch (Exception ignore)
			{
			}
		}
		return site;
		
	}	// getStateSite
	
	/**
	 * get the Group object based on SessionState attribute values
	 * @return Group object related to current state; null if no such Group object could be found
	 */
	protected Group getStateGroup(SessionState state)
	{
		Group group = null;
		Site site = getStateSite(state);
		
		if (site != null && state.getAttribute(STATE_GROUP_INSTANCE_ID) != null)
		{
			try
			{
				group = site.getGroup((String) state.getAttribute(STATE_GROUP_INSTANCE_ID));
			}
			catch (Exception ignore)
			{
			}
		}
		return group;
		
	}	// getStateGroup
		
	/**
	* do called when "eventSubmit_do" is in the request parameters to c
	* is called from site list menu entry Revise... to get a locked site as editable and to go to the correct template to begin
	* DB version of writes changes to disk at site commit whereas XML version writes at server shutdown
	*/
	public void doGet_site ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		ParameterParser params = data.getParameters ();
		
		//check form filled out correctly
		if (params.getStrings ("selectedMembers") == null)
		{
			addAlert(state, NO_SITE_SELECTED_STRING);
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			return;
		}
		List chosenList = new ArrayList(Arrays.asList(params.getStrings ("selectedMembers"))); // Site id's of checked sites
		String siteId = "";
		if(!chosenList.isEmpty())
		{
			if(chosenList.size() != 1)
			{
				addAlert(state, MORE_THAN_ONE_SITE_SELECTED_STRING);
				state.setAttribute(STATE_TEMPLATE_INDEX, "0");
				return;
			}
			
			siteId = (String) chosenList.get(0);
			getReviseSite(state, siteId);
			
			state.setAttribute(SORTED_BY, SORTED_BY_PARTICIPANT_NAME);
			state.setAttribute (SORTED_ASC, Boolean.TRUE.toString());
		}
		
		if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITESETUP))
		{
			state.setAttribute(STATE_PAGESIZE_SITESETUP, state.getAttribute(STATE_PAGESIZE));
		}
		
		Hashtable h = (Hashtable) state.getAttribute(STATE_PAGESIZE_SITEINFO);
		if (!h.containsKey(siteId))
		{	
			// when first entered Site Info, set the participant list size to 200 as default
			state.setAttribute(STATE_PAGESIZE, new Integer(200));
			
			// update
			h.put(siteId, new Integer(200));
			state.setAttribute(STATE_PAGESIZE_SITEINFO, h);
		}
		else
		{
			//restore the page size in site info tool
			state.setAttribute(STATE_PAGESIZE, h.get(siteId));
		}
		
	} // doGet_site
	
	/**
	* do called when "eventSubmit_do" is in the request parameters to c
	*/
	public void doMenu_site_reuse ( RunData data )
		throws Exception
	{
		// called from chef_site-list.vm after a site has been selected from list
		// create a new Site object based on selected Site object and put in state
		//
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.setAttribute (STATE_TEMPLATE_INDEX, "1");
		
	}	// doMenu_site_reuse
	
	/**
	* do called when "eventSubmit_do" is in the request parameters to c
	*/
	public void doMenu_site_revise ( RunData data )
		throws Exception
	{
		// called from chef_site-list.vm after a site has been selected from list
		// get site as Site object, check SiteCreationStatus and SiteType of site, put in state, and set STATE_TEMPLATE_INDEX correctly
		// set mode to state_mode_site_type
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.setAttribute (STATE_TEMPLATE_INDEX, "1");
		
	}	// doMenu_site_revise
	
	/**
	* doView_sites is called when "eventSubmit_doView_sites" is in the request parameters
	*/
	public void doView_sites ( RunData data )
		throws Exception
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		state.setAttribute (STATE_VIEW_SELECTED, params.getString("view"));
		state.setAttribute (STATE_TEMPLATE_INDEX, "0");
		
		resetPaging(state);
		
	}	// doView_sites
	
	/**
	* do called when "eventSubmit_do" is in the request parameters to c
	*/
	public void doView ( RunData data )
		throws Exception
	{
		// called from chef_site-list.vm with a select option to build query of sites
		// 
		// 
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.setAttribute (STATE_TEMPLATE_INDEX, "1");
	}	// doView
	
	/**
	* do called when "eventSubmit_do" is in the request parameters to c
	*/
	public void doSite_type ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		
		ParameterParser params = data.getParameters ();
		int index = Integer.valueOf(params.getString ("template-index")).intValue();
		actionForTemplate("continue", index, params, state);

		String type = StringUtil.trimToNull(params.getString ("itemType"));
		
		int totalSteps = 0;
		
		if (type == null)
		{
			addAlert(state, rb.getString("java.select")+" ");
		}
		else
		{
			setNewSiteType(state, type);
			
			if (type.equalsIgnoreCase("course"))
			{
				String userId = StringUtil.trimToZero(SessionManager.getCurrentSessionUserId());
				String termId = params.getString("selectTerm");
				Term t = CourseManagementService.getTerm(termId);
				state.setAttribute(STATE_TERM_SELECTED, t);
				if (t != null)
				{
					List courses = CourseManagementService.getInstructorCourses(userId, t.getYear(), t.getTerm());
					
					// future term? roster information is not available yet?
					int weeks = 0;
					Calendar c = (Calendar) Calendar.getInstance().clone();
					try
					{
						weeks = Integer.parseInt(ServerConfigurationService.getString("roster.available.weeks.before.term.start", "0"));
						c.add(Calendar.DATE, weeks*7);
					}
					catch (Exception ignore)
					{
					}
					
					if ((courses == null || courses != null && courses.size() == 0)
						&& c.getTimeInMillis() < t.getStartTime().getTime())
					{
						//if a future term is selected
						state.setAttribute(STATE_FUTURE_TERM_SELECTED, Boolean.TRUE);							
					}
					else
					{
						state.setAttribute(STATE_FUTURE_TERM_SELECTED, Boolean.FALSE);							
					}
					
					if (courses != null && courses.size() > 0)
					{
						state.setAttribute(STATE_TERM_COURSE_LIST, courses);
						state.setAttribute (STATE_TEMPLATE_INDEX, "36");
						state.setAttribute(STATE_AUTO_ADD, Boolean.TRUE);
						
						totalSteps = 6;
					}
					else
					{
						state.removeAttribute(STATE_TERM_COURSE_LIST);
						state.setAttribute (STATE_TEMPLATE_INDEX, "37");
						
						totalSteps = 5;
					}
				}
				else
				{
					state.setAttribute (STATE_TEMPLATE_INDEX, "37");
					totalSteps = 5;
				}
			}
			else if (type.equals("project"))
			{
				totalSteps = 4;
				state.setAttribute (STATE_TEMPLATE_INDEX, "2");
			}
			else if (type.equals (SITE_TYPE_GRADTOOLS_STUDENT))
			{
				//if a GradTools site use pre-defined site info and exclude from public listing
				SiteInfo siteInfo = new SiteInfo();
				if(state.getAttribute(STATE_SITE_INFO) != null)
				{
					siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
				}
				User currentUser = UserDirectoryService.getCurrentUser();
				siteInfo.title = rb.getString("java.grad")+" - " + currentUser.getId();
				siteInfo.description =  rb.getString("java.gradsite")+" " + currentUser.getDisplayName();
				siteInfo.short_description = rb.getString("java.grad")+" - " + currentUser.getId();
				siteInfo.include = false;
				state.setAttribute(STATE_SITE_INFO, siteInfo);
				
				//skip directly to confirm creation of site 
				state.setAttribute (STATE_TEMPLATE_INDEX, "42");
			}
			else
			{
				state.setAttribute (STATE_TEMPLATE_INDEX, "2");
			}
		}
		
		if (state.getAttribute(SITE_CREATE_TOTAL_STEPS) == null)
		{
			state.setAttribute(SITE_CREATE_TOTAL_STEPS, new Integer(totalSteps));
		}
		
		if (state.getAttribute(SITE_CREATE_CURRENT_STEP) == null)
		{
			state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer(1));
		}
		
		
	}	// doSite_type
	
	/**
	*  cleanEditGroupParams
	*  clean the state parameters used by editing group process
	* 
	*/
	public void cleanEditGroupParams ( SessionState state )
	{
		state.removeAttribute(STATE_GROUP_INSTANCE_ID);
		state.removeAttribute(STATE_GROUP_TITLE);
		state.removeAttribute(STATE_GROUP_DESCRIPTION);
		state.removeAttribute(STATE_GROUP_MEMBERS);
		state.removeAttribute(STATE_GROUP_REMOVE);
		
	}	//cleanEditGroupParams
	
	/**
	*  doGroup_edit
	* 
	*/
	public void doGroup_update ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		Set gMemberSet = (Set) state.getAttribute(STATE_GROUP_MEMBERS);
		Site site = getStateSite(state);
		
		String title = StringUtil.trimToNull(params.getString(rb.getString("group.title")));
		state.setAttribute(STATE_GROUP_TITLE, title);
		
		String description = StringUtil.trimToZero(params.getString(rb.getString("group.description")));
		state.setAttribute(STATE_GROUP_DESCRIPTION, description);
		
		boolean found = false;
		String option = params.getString("option");
		
		if (option.equals("add"))
		{
			// add selected members into it
			if (params.getStrings("generallist") != null)
			{
				List addMemberIds = new ArrayList(Arrays.asList(params.getStrings("generallist")));
				for (int i=0; i<addMemberIds.size(); i++)
				{
					String aId = (String) addMemberIds.get(i);
					found = false;
					for(Iterator iSet = gMemberSet.iterator(); !found && iSet.hasNext();)
					{
						if (((Member) iSet.next()).getUserId().equals(aId))
						{
							found = true;
						}
					}
					if (!found)
					{
						gMemberSet.add(site.getMember(aId));
					}
				}
			}
			state.setAttribute(STATE_GROUP_MEMBERS, gMemberSet);
		}
		else if (option.equals("remove"))
		{
			// update the group member list by remove selected members from it
			if (params.getStrings("grouplist") != null)
			{
				List removeMemberIds = new ArrayList(Arrays.asList(params.getStrings("grouplist")));
				for (int i=0; i<removeMemberIds.size(); i++)
				{
					found = false;
					for(Iterator iSet = gMemberSet.iterator(); !found && iSet.hasNext();)
					{
						Member mSet = (Member) iSet.next();
						if (mSet.getUserId().equals((String) removeMemberIds.get(i)))
						{
							found = true;
							gMemberSet.remove(mSet);
						}
					}
				}
			}
			state.setAttribute(STATE_GROUP_MEMBERS, gMemberSet);
		} 
		else if (option.equals("cancel"))
		{
			// cancel from the update the group member process
			doCancel(data);
			cleanEditGroupParams(state);
			
		}
		else if (option.equals("save"))
		{	
			Group group = null;
			if (site != null && state.getAttribute(STATE_GROUP_INSTANCE_ID) != null)
			{
				try
				{
					group = site.getGroup((String) state.getAttribute(STATE_GROUP_INSTANCE_ID));
				}
				catch (Exception ignore)
				{
				}
			}
			
			if (title == null)
			{
				addAlert(state, rb.getString("editgroup.titlemissing"));
			}
			
			if (state.getAttribute(STATE_MESSAGE) == null)
			{	
				if (group == null)
				{
					// adding new group
					group = site.addGroup();
					group.getProperties().addProperty(GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
				}
				
				if (group != null)
				{
					group.setTitle(title);
					group.setDescription(description);
					
					// save the modification to group members
					
					// remove those no longer included in the group
					Set members = group.getMembers();
					for(Iterator iMembers = members.iterator(); iMembers.hasNext();)
					{
						found = false;
						String mId = ((Member)iMembers.next()).getUserId();
						for(Iterator iMemberSet = gMemberSet.iterator(); !found && iMemberSet.hasNext();)
						{
							if (mId.equals(((Member) iMemberSet.next()).getUserId()))
							{
								found = true;
							}
						
						}
						if (!found)
						{
							group.removeMember(mId);
						}
					}
						
					// add those seleted members
					for(Iterator iMemberSet = gMemberSet.iterator(); iMemberSet.hasNext();)
					{
						String memberId = ((Member) iMemberSet.next()).getUserId();
						if (group.getUserRole(memberId) == null)
						{
							Role r = site.getUserRole(memberId);
							Member m = site.getMember(memberId);
							// for every member added through the "Manage Groups" interface, he should be defined as non-provided
							group.addMember(memberId, r!= null?r.getId():"", m!=null?m.isActive():true, false);
						}
					}
					
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						try 
						{
							SiteService.save(site);
						} 
						catch (IdUnusedException e) 
						{
						} 
						catch (PermissionException e) 
						{
						}
						
						// return to group list view
						state.setAttribute (STATE_TEMPLATE_INDEX, "49");
						cleanEditGroupParams(state);
					}
				}
			}
		}
		
	}	// doGroup_updatemembers
	
	/**
	*  doGroup_new
	* 
	*/
	public void doGroup_new ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		if (state.getAttribute(STATE_GROUP_TITLE) == null)
		{
			state.setAttribute(STATE_GROUP_TITLE, "");
		}
		if (state.getAttribute(STATE_GROUP_DESCRIPTION) == null)
		{
			state.setAttribute(STATE_GROUP_DESCRIPTION, "");
		}
		if (state.getAttribute(STATE_GROUP_MEMBERS) == null)
		{
			state.setAttribute(STATE_GROUP_MEMBERS, new HashSet());
		}
		state.setAttribute (STATE_TEMPLATE_INDEX, "50");
		
	}	// doGroup_new
	
	/**
	*  doGroup_edit
	* 
	*/
	public void doGroup_edit ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		String groupId = data.getParameters ().getString("groupId");
		state.setAttribute(STATE_GROUP_INSTANCE_ID, groupId);
		
		Site site = getStateSite(state);
		if (site != null)
		{
			Group g = site.getGroup(groupId);
			if (g != null)
			{
				if (state.getAttribute(STATE_GROUP_TITLE) == null)
				{
					state.setAttribute(STATE_GROUP_TITLE, g.getTitle());
				}
				if (state.getAttribute(STATE_GROUP_DESCRIPTION) == null)
				{
					state.setAttribute(STATE_GROUP_DESCRIPTION, g.getDescription());
				}
				if (state.getAttribute(STATE_GROUP_MEMBERS) == null)
				{
					state.setAttribute(STATE_GROUP_MEMBERS, g.getMembers());
				}
			}
		}
		state.setAttribute (STATE_TEMPLATE_INDEX, "50");
		
	}	// doGroup_edit
	
	/**
	*  doGroup_remove_prep
	*  Go to confirmation page before deleting group(s)
	* 
	*/
	public void doGroup_remove_prep ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		String[] removeGroupIds = data.getParameters ().getStrings("removeGroups");
		
		if (removeGroupIds.length > 0)
		{
			state.setAttribute(STATE_GROUP_REMOVE, removeGroupIds);
			state.setAttribute (STATE_TEMPLATE_INDEX, "51");
		}
		
	}	// doGroup_remove_prep
	
	/**
	*  doGroup_remove_confirmed
	*  Delete selected groups after confirmation
	* 
	*/
	public void doGroup_remove_confirmed ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		String[] removeGroupIds = (String[]) state.getAttribute(STATE_GROUP_REMOVE);
		
		Site site = getStateSite(state);
		for (int i=0; i<removeGroupIds.length; i++)
		{
			if (site != null)
			{
				Group g = site.getGroup(removeGroupIds[i]);
				if (g != null)
				{
					site.removeGroup(g);
				}
			}
		}
		try 
		{
			SiteService.save(site);
		} 
		catch (IdUnusedException e) 
		{
			addAlert(state, rb.getString("editgroup.site.notfound.alert"));
		} 
		catch (PermissionException e) 
		{
			addAlert(state, rb.getString("editgroup.site.permission.alert"));
		}
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			cleanEditGroupParams(state);
			state.setAttribute (STATE_TEMPLATE_INDEX, "49");
		}
		
	}	// doGroup_remove_confirmed
	
	/**
	*  doMenu_edit_site_info
	*  The menu choice to enter group view
	* 
	*/
	public void doMenu_group ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// reset sort criteria
		state.setAttribute(SORTED_BY, rb.getString("group.title"));
		state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());
		
		state.setAttribute (STATE_TEMPLATE_INDEX, "49");
		
	}	// doMenu_group
	
	/**
	* dispatch to different functions based on the option value in the parameter
	*/
	public void doManual_add_course ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
			
		String option = params.getString("option");
		if (option.equalsIgnoreCase("change") || option.equalsIgnoreCase("add"))
		{
			readCourseSectionInfo(state, params);
		
			String uniqname = StringUtil.trimToNull(params.getString("uniqname"));
			state.setAttribute(STATE_SITE_QUEST_UNIQNAME, uniqname);
		
			if (getStateSite(state) == null)
			{
				// creating new site
				updateSiteInfo(params, state);
			}
			
			if (option.equalsIgnoreCase("add"))
			{
			
				if (state.getAttribute(STATE_FUTURE_TERM_SELECTED) != null
				&& !((Boolean) state.getAttribute(STATE_FUTURE_TERM_SELECTED)).booleanValue())
				{
					// if a future term is selected, do not check authorization uniqname 
					if (uniqname == null)
					{
						addAlert(state, rb.getString("java.author") + " " + ServerConfigurationService.getString("noEmailInIdAccountName") + ". "); 
					}
					else
					{
						try
						{
							UserDirectoryService.getUserByEid(uniqname);
						}
						catch (UserNotDefinedException e)
						{
							addAlert(state, rb.getString("java.validAuthor1")+" "+ ServerConfigurationService.getString("noEmailInIdAccountName") + " "+ rb.getString("java.validAuthor2"));
						}
					}
				}
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					if (getStateSite(state) == null)
					{
						state.setAttribute(STATE_TEMPLATE_INDEX, "2");
					}
					else
					{
						state.setAttribute(STATE_TEMPLATE_INDEX, "44");
					}
				}
				updateCurrentStep(state, true);
			}
		}
		else if (option.equalsIgnoreCase("back"))
		{
			doBack(data);
			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				updateCurrentStep(state, false);
			}
		}
		else if (option.equalsIgnoreCase("cancel"))
		{
			if (getStateSite(state) == null)
			{
				doCancel_create(data);
			}
			else
			{
				doCancel(data);
			}
		}
			
	}	// doManual_add_course
	
	/**
	* read the input information of subject, course and section in the manual site creation page
	*/
	private void readCourseSectionInfo (SessionState state, ParameterParser params)
	{	
		int oldNumber = 1;
		if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null)
		{
			oldNumber = ((Integer) state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER)).intValue();
		}
		
		// read the user input
		int validInputSites = 0;
		boolean validInput = true;
		List requiredFields = CourseManagementService.getCourseIdRequiredFields();
		List multiCourseInputs = new Vector();
		for (int i = 0; i < oldNumber; i++)
		{
			List aCourseInputs = new Vector();
			int emptyInputNum = 0;
			
			// iterate through all required fields
			for (int k = 0; k<requiredFields.size(); k++)
			{
				String field = (String) requiredFields.get(k);
				String fieldInput = StringUtil.trimToZero(params.getString(field + i));
				aCourseInputs.add(fieldInput);
				if (fieldInput.length() == 0)
				{
					// is this an empty String input?
					emptyInputNum++;
				}
			}
			
			// add to the multiCourseInput vector
			multiCourseInputs.add(i, aCourseInputs);
			
			// is any input invalid?
			if (emptyInputNum == 0)
			{
				// valid if all the inputs are not empty
				validInputSites++;
			}
			else if (emptyInputNum == requiredFields.size())
			{
				// ignore if all inputs are empty
			}
			else
			{
				if (state.getAttribute(STATE_FUTURE_TERM_SELECTED) != null
					&& ((Boolean) state.getAttribute(STATE_FUTURE_TERM_SELECTED)).booleanValue())
				{
					// if future term selected, then not all fields are required %%%
					validInputSites++;
				}
				else
				{
					validInput = false;
				}
			}
		}
		
		//how many more course/section to include in the site?
		String option = params.getString("option");
		if (option.equalsIgnoreCase("change"))
		{
			if (params.getString("number") != null)
			{
				int newNumber = Integer.parseInt(params.getString("number"));
				state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, new Integer(oldNumber + newNumber));

				for (int j = 0; j<newNumber; j++)
				{
					// add a new course input
					List aCourseInputs = new Vector();
					// iterate through all required fields
					for (int m = 0; m<requiredFields.size(); m++)
					{
						aCourseInputs.add("");
					}
					multiCourseInputs.add(aCourseInputs);
				}
			}
		}
		
		state.setAttribute(STATE_MANUAL_ADD_COURSE_FIELDS, multiCourseInputs);
		
		if (!option.equalsIgnoreCase("change"))
		{
			if (!validInput || validInputSites == 0)
			{
				// not valid input
				addAlert(state, rb.getString("java.miss"));
			}
			else
			{
				// valid input, adjust the add course number
				state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, new Integer(validInputSites));
			}
		}
		
		// set state attributes
		state.setAttribute(FORM_ADDITIONAL, StringUtil.trimToZero(params.getString("additional")));
		
		SiteInfo siteInfo = new SiteInfo();
		if(state.getAttribute(STATE_SITE_INFO) != null)
		{
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
		}
		List providerCourseList = (List) state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
		//store the manually requested sections in one site property
		if ((providerCourseList == null || providerCourseList.size() == 0) && multiCourseInputs.size() > 0)
		{ 
			String courseId = CourseManagementService.getCourseId((Term) state.getAttribute(STATE_TERM_SELECTED), (List) multiCourseInputs.get(0)); 
			String title = "";
			try
			{
				title = CourseManagementService.getCourseName(courseId);
			}
			catch (IdUnusedException e)
			{
				// ignore
			}
			siteInfo.title = appendTermInSiteTitle(state, title);
		}
		state.setAttribute(STATE_SITE_INFO, siteInfo);
		
	}	// readCourseSectionInfo
	
	/**
	 * set the site type for new site
	 * 
	 * @param type The type String
	 */
	private void setNewSiteType(SessionState state, String type)
	{
		state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME);
	
		//start out with fresh site information
		SiteInfo siteInfo = new SiteInfo();
		siteInfo.site_type = type;
		siteInfo.published = true;
		state.setAttribute(STATE_SITE_INFO, siteInfo);

		// get registered tools list
		Set categories = new HashSet();
		categories.add(type);
		Set toolRegistrations = ToolManager.findTools(categories, null);

		List tools = new Vector();
		SortedIterator i = new SortedIterator(toolRegistrations.iterator(), new ToolComparator());
		for (; i.hasNext();)
		{
			// form a new Tool
			Tool tr = (Tool) i.next();
			MyTool newTool = new MyTool();
			newTool.title = tr.getTitle();
			newTool.id = tr.getId();
			newTool.description = tr.getDescription();
			
			tools.add(newTool);
		}
		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, tools);
		
		state.setAttribute (STATE_SITE_TYPE, type);
	}
	
	/**
	* Set the field on which to sort the list of students
	* 
	*/
	public void doSort_roster ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		//get the field on which to sort the student list
		ParameterParser params = data.getParameters ();
		String criterion = params.getString ("criterion");
		
		// current sorting sequence
		String asc = "";
		if (!criterion.equals (state.getAttribute (SORTED_BY)))
		{
			state.setAttribute (SORTED_BY, criterion);
			asc = Boolean.TRUE.toString ();
			state.setAttribute (SORTED_ASC, asc);
		}
		else
		{
			// current sorting sequence
			asc = (String) state.getAttribute (SORTED_ASC);
			
			//toggle between the ascending and descending sequence
			if (asc.equals (Boolean.TRUE.toString ()))
			{
				asc = Boolean.FALSE.toString ();
			}
			else
			{
				asc = Boolean.TRUE.toString ();
			}
			state.setAttribute (SORTED_ASC, asc);
		}
		
	} //doSort_roster 
	
	/**
	* Set the field on which to sort the list of sites 
	* 
	*/
	public void doSort_sites ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		//call this method at the start of a sort for proper paging
		resetPaging(state);
		
		//get the field on which to sort the site list
		ParameterParser params = data.getParameters ();
		String criterion = params.getString ("criterion");
		
		// current sorting sequence
		String asc = "";
		if (!criterion.equals (state.getAttribute (SORTED_BY)))
		{
			state.setAttribute (SORTED_BY, criterion);
			asc = Boolean.TRUE.toString ();
			state.setAttribute (SORTED_ASC, asc);
		}
		else
		{
			// current sorting sequence
			asc = (String) state.getAttribute (SORTED_ASC);
			
			//toggle between the ascending and descending sequence
			if (asc.equals (Boolean.TRUE.toString ()))
			{
				asc = Boolean.FALSE.toString ();
			}
			else
			{
				asc = Boolean.TRUE.toString ();
			}
			state.setAttribute (SORTED_ASC, asc);
		}
		
		state.setAttribute(SORTED_BY, criterion);
		
	} //doSort_sites	
	
	/**
	* doContinue is called when "eventSubmit_doContinue" is in the request parameters
	*/
	public void doContinue ( RunData data )
	{
		// Put current form data in state and continue to the next template, make any permanent changes
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		ParameterParser params = data.getParameters ();
		int index = Integer.valueOf(params.getString ("template-index")).intValue();

		// Let actionForTemplate know to make any permanent changes before continuing to the next template
		String direction = "continue";
		actionForTemplate(direction, index, params, state);
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			if (index == 9)
			{
				// go to the send site publish email page if "publish" option is chosen
				SiteInfo sInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
				if (sInfo.getPublished())
				{
					state.setAttribute(STATE_TEMPLATE_INDEX, "16");		
				}
				else
				{
					state.setAttribute(STATE_TEMPLATE_INDEX, "17");
				}
			}
			else if (params.getString ("continue") != null)
			{
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString ("continue"));
			}
		}
		
	}// doContinue
	
	
	/**
	* doBack is called when "eventSubmit_doBack" is in the request parameters
	* Pass parameter to actionForTemplate to request action for backward direction
	*/
	public void doBack ( RunData data )
	{
		// Put current form data in state and return to the previous template
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		ParameterParser params = data.getParameters ();
		int currentIndex = Integer.parseInt((String)state.getAttribute(STATE_TEMPLATE_INDEX));
		state.setAttribute(STATE_TEMPLATE_INDEX, params.getString ("back"));
		
		// Let actionForTemplate know not to make any permanent changes before continuing to the next template
		String direction = "back";
		actionForTemplate(direction, currentIndex, params, state);
	
	}// doBack
	
	/**
	* doFinish is called when a site has enough information to be saved as an unpublished site
	*/
	public void doFinish ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		ParameterParser params = data.getParameters ();

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, params.getString ("continue"));
			int index = Integer.valueOf(params.getString ("template-index")).intValue();
			actionForTemplate("continue", index, params, state);
			
			addNewSite(params, state);
			
			addFeatures(state);
			
			Site site = getStateSite(state);
			
			// for course sites
			String siteType = (String) state.getAttribute(STATE_SITE_TYPE);
			if (siteType != null && siteType.equalsIgnoreCase("course"))
			{
				String siteId = site.getId();
				
				ResourcePropertiesEdit rp = site.getPropertiesEdit();
				
				Term term = null;
				if (state.getAttribute(STATE_TERM_SELECTED) != null)
				{
					term = (Term) state.getAttribute(STATE_TERM_SELECTED);
					rp.addProperty(PROP_SITE_TERM, term.getId());
				}
				
				List providerCourseList = (List) state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
				List fields = (List) state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
				int manualAddNumber = 0;
				if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null)
				{
					manualAddNumber = ((Integer) state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER)).intValue();
				}
				
				String realm = SiteService.siteReference(siteId);
				
				if ((providerCourseList != null) && (providerCourseList.size() != 0))
				{
					String providerRealm = buildExternalRealm(siteId, state, providerCourseList);
					
					try
					{
						AuthzGroup realmEdit = AuthzGroupService.getAuthzGroup(realm);
						realmEdit.setProviderGroupId(providerRealm);
						AuthzGroupService.save(realmEdit);
					}
					catch (GroupNotDefinedException e)
					{
						M_log.warn(this + " IdUnusedException, not found, or not an AuthzGroup object");
						addAlert(state, rb.getString("java.realm"));
						return;
					}
//					catch (AuthzPermissionException e)
//					{
//						M_log.warn(this + " PermissionException, user does not have permission to edit AuthzGroup object.");
//						addAlert(state, rb.getString("java.notaccess"));
//						return;
//					}
					catch (Exception e)
					{
						addAlert(state, this + rb.getString("java.problem"));
						return;
					}
					
					addSubjectAffliates(state, providerCourseList);
					
					sendSiteNotification(state, providerCourseList);
				}
				
				if (manualAddNumber != 0)
				{
					// set the manual sections to the site property
					String manualSections = "";
					List manualCourseInputs = (List) state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
					for (int j = 0; j < manualAddNumber; j++)
					{
						manualSections = manualSections.concat(CourseManagementService.getCourseId(term, (List) manualCourseInputs.get(j))).concat("+");
					}
					
					// trim the trailing plus sign
					if (manualSections.endsWith("+"))
					{
						manualSections = manualSections.substring(0, manualSections.lastIndexOf("+"));
					}
					rp.addProperty(PROP_SITE_REQUEST_COURSE, manualSections);
					// send request
					sendSiteRequest(state, "new", manualAddNumber, manualCourseInputs);
				}
				
			}
			
			// commit site
			commitSite(site);
			
			String siteId = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);
			
			//now that the site exists, we can set the email alias when an Email Archive tool has been selected
			String alias = StringUtil.trimToNull((String) state.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
			if (alias != null)
			{	
				String channelReference = mailArchiveChannelReference(siteId);
				try
				{
					AliasService.setAlias(alias, channelReference);
				}
				catch (IdUsedException ee) 
				{
					addAlert(state, rb.getString("java.alias")+" " + alias + " "+rb.getString("java.exists"));
				}
				catch (IdInvalidException ee) 
				{
					addAlert(state, rb.getString("java.alias")+" " + alias + " "+rb.getString("java.isinval"));
				}
				catch (PermissionException ee) 
				{
					addAlert(state, rb.getString("java.addalias")+" ");
				}
			}
			// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
			schedulePeerFrameRefresh("sitenav");
				
			resetPaging(state);
			
			// clean state variables
			cleanState(state);
			
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");	
					
		}
		
	}// doFinish
	
	private void addSubjectAffliates(SessionState state, List providerCourseList)
	{
		Vector subjAffiliates = new Vector();
		Vector affiliates = new Vector();
		String subject = "";
		String affiliate = "";
		
		//get all subject and campus pairs for this site
		for (ListIterator i = providerCourseList.listIterator(); i.hasNext(); )
		{
			String courseId = (String) i.next();
			
			try
			{
				Course c = CourseManagementService.getCourse(courseId);
				if(c.getSubject() != null && c.getSubject() != "") subject = c.getSubject();
				
				subjAffiliates.add(subject);
			}
			catch (IdUnusedException e)
			{
				// M_log.warn(this + " cannot find course " + courseId + ". ");
			}
		}
		
		// remove duplicates
		Collection noDups = new HashSet(subjAffiliates);
		
		// get affliates for subjects
		for (Iterator i = noDups.iterator(); i.hasNext(); )
		{
			subject = (String)i.next();
			
			Collection uniqnames = getSubjectAffiliates(state, subject);
			
			try
			{
				affiliates.addAll(uniqnames);
			}
			catch(Exception ignore){}
		}
		
		// remove duplicates
		Collection addAffiliates = new HashSet(affiliates);
		
		// try to add uniqnames with appropriate role
		for (Iterator i = addAffiliates.iterator(); i.hasNext(); )
		{
			affiliate = (String)i.next();
			
			try
			{
				User user = UserDirectoryService.getUserByEid(affiliate);
				String realmId = "/site/" + (String) state.getAttribute(STATE_SITE_INSTANCE_ID);
				if (AuthzGroupService.allowUpdate(realmId))
				{
					try
					{
						AuthzGroup realmEdit = AuthzGroupService.getAuthzGroup(realmId);
						Role role = realmEdit.getRole("Affiliate");
						realmEdit.addMember(user.getId(), role.getId(), true, false);
						AuthzGroupService.save(realmEdit);
					}
					catch(Exception ignore) {}
				}
			}
			catch(Exception ignore)
			{
				M_log.warn(this + " cannot find affiliate " + affiliate);
			}
		}
		
	} //addSubjectAffliates

	/**
	* @params - SessionState state
	* @params - String subject is the University's Subject code
	* @return - Collection of uniqnames of affiliates for this subject
	*/
	private Collection getSubjectAffiliates(SessionState state, String subject)
	{
		Collection rv = null;
		List allAffiliates = (Vector) state.getAttribute(STATE_SUBJECT_AFFILIATES);
		
		//iterate through the subjects looking for this subject
		for (Iterator i = allAffiliates.iterator(); i.hasNext(); )
		{
			SubjectAffiliates sa = (SubjectAffiliates)i.next();
			if(subject.equals(sa.getCampus() + "_" + sa.getSubject())) return sa.getUniqnames();
		}
		return rv;
		
	} //getSubjectAffiliates
	
	/**
	* buildExternalRealm creates a site/realm id in one of three formats,
	* for a single section, for multiple sections of the same course, or
	* for a cross-listing having multiple courses
	* @param sectionList is a Vector of CourseListItem
	* @param id The site id
	*/
	private String buildExternalRealm(String id, SessionState state, List sectionList)
	{
		String realm = SiteService.siteReference(id);
		if (!AuthzGroupService.allowUpdate(realm))
		{
			addAlert(state, rb.getString("java.rosters"));
			return null;
		}
		
		boolean same_course = true;
		// No sections in list
		if (sectionList.size() == 0) 
		{
			return null;
		}
		// One section in list
		else if (sectionList.size() == 1) 
		{
			// 2002,2,A,EDUC,406,001
			return (String) sectionList.get(0);
		}
		// More than one section in list
		else
		{
			String full_key = (String) sectionList.get(0);
			
			String course = full_key.substring(0, full_key.lastIndexOf(","));
			same_course = true;
			for (ListIterator i = sectionList.listIterator(); i.hasNext(); )
			{
				String item = (String) i.next();
				if (item.indexOf(course) == -1) same_course = false; // If there is a difference in course part, multiple courses
			}
			// Same course but with multiple sections
			if (same_course)
			{
				StringBuffer sections = new StringBuffer();
				sections.append(course);
				sections.append(",[");
				boolean first_section = true;
				for (ListIterator i = sectionList.listIterator(); i.hasNext(); )
				{
					String item = (String) i.next();
					// remove the "," from the first section string
					String section = new String();
					if (first_section)
					{
						section = item.substring(item.lastIndexOf(",")+1,item.length());
					}
					else
					{
						section = item.substring(item.lastIndexOf(","),item.length());
					}
					first_section = false;
					sections.append(section);
				}
				sections.append("]");
				// 2002,2,A,EDUC,406,[001,002,003]
				return sections.toString();
			}
			// Multiple courses 
			else
			{
				// First, put course section keys next to each other to establish the course demarcation points
				Vector keys = new Vector();
				for (int i = 0; i < sectionList.size(); i++ )
				{
					String item = (String) sectionList.get(i);
					keys.add(item);
				}
				Collections.sort(keys);
				StringBuffer buf = new StringBuffer();
				StringBuffer section_buf = new StringBuffer();
				String last_course = null;
				String last_section = null;
				String to_buf = null;
				// Compare previous and next keys. When the course changes, build a component part of the id.
				for (int i = 0; i < keys.size(); i++)
				{
					// Go through the list of keys, comparing this key with the previous key
					String this_key= (String) keys.get(i);
					String this_course = this_key.substring(0, this_key.lastIndexOf(","));
					String this_section = this_key.substring(this_key.lastIndexOf(","), this_key.length());
					last_course = this_course;
					if(i != 0)
					{
						// This is not the first key in the list, so it has a previous key
						String previous_key = (String) keys.get(i-1);
						String previous_course = previous_key.substring(0, previous_key.lastIndexOf(","));
						String previous_section = previous_key.substring(previous_key.lastIndexOf(","), previous_key.length());
						if (previous_course.equals(this_course))
						{
							same_course = true;
							section_buf.append(previous_section);
						}
						else
						{
							same_course = false; // Different course, so wrap up the realm component for the previous course
							buf.append(previous_course);
							section_buf.append(previous_section);
							if (section_buf.lastIndexOf(",") == 0) // ,001
							{
								to_buf = section_buf.toString();
								buf.append(to_buf);
							}
							else
							{
								buf.append(",[");
								to_buf = section_buf.toString();
								buf.append(to_buf.substring(1)); // 001,002
								buf.append("]");	
							}
							section_buf.setLength(0);
							buf.append("+");
						}
						last_section = this_section;
					} // one comparison
				}
				// Hit the end of the list, so wrap up the realm component for the last course in the list
				if (same_course)
				{
					buf.append(last_course);
					buf.append(",[");
					buf.append((section_buf.toString()).substring(1));
					buf.append(last_section);
					// There must be more than one section, because there the last course was the same as this course
					buf.append ("]");
				}
				else
				{
					// There can't be more than one section, because the last course was different from this course
					buf.append(last_course);
					buf.append(last_section);
				}
				// 2003,3,A,AOSS,172,001+2003,3,A,NRE,111,001+2003,3,A,ENVIRON,111,001+2003,3,A,SOC,111,001
				return buf.toString();
			}
		}
		
	} // buildExternalRealm

	/**
	* Notification sent when a course site needs to be set up by Support
	* 
	*/
	private void sendSiteRequest(SessionState state, String request, int requestListSize, List requestFields)
	{

		boolean sendEmailToRequestee = false;
		StringBuffer buf = new StringBuffer();
		
		// get the request email from configuration
		String requestEmail = ServerConfigurationService.getString("setup.request", null);
		if (requestEmail == null)
		{
			M_log.warn(this + " - no 'setup.request' in configuration");
		}
		else
		{
			String noEmailInIdAccountName = ServerConfigurationService.getString("noEmailInIdAccountName", "");
			
			SiteInfo siteInfo = (SiteInfo)state.getAttribute(STATE_SITE_INFO);
			
			Site site = getStateSite(state);
			String id = site.getId();
			String title = site.getTitle();
			
			Time time = TimeService.newTime();
			String local_time = time.toStringLocalTime();
			String local_date = time.toStringLocalDate();
			
			Term term = null;
			boolean termExist = false;
			if (state.getAttribute(STATE_TERM_SELECTED) != null)
			{
				termExist = true;
				term = (Term) state.getAttribute(STATE_TERM_SELECTED);
			}
			String productionSiteName = ServerConfigurationService.getServerName();
			
			String from = NULL_STRING;
			String to = NULL_STRING;
			String headerTo = NULL_STRING;
			String replyTo = NULL_STRING;
			String message_subject = NULL_STRING;
			String content = NULL_STRING;
			
			String sessionUserName = UserDirectoryService.getCurrentUser().getDisplayName();
			String sessionUserId = StringUtil.trimToZero(SessionManager.getCurrentSessionUserId());
			String additional = NULL_STRING;
			if (request.equals("new"))
			{
				additional = siteInfo.getAdditional();
			}
			else
			{
				additional = (String)state.getAttribute(FORM_ADDITIONAL);
			}
			
			boolean isFutureTerm = false;
			if (state.getAttribute(STATE_FUTURE_TERM_SELECTED) != null && ((Boolean) state.getAttribute(STATE_FUTURE_TERM_SELECTED)).booleanValue())
			{
				isFutureTerm = true;
			}
			
			// message subject
			if (termExist)
			{
				message_subject = rb.getString("java.sitereqfrom")+" " + sessionUserName + " " + rb.getString("java.for") + " " + term.getId();
			}
			else
			{
				message_subject = rb.getString("java.official")+" " + sessionUserName;
			}
			
			// there is no offical instructor for future term sites
			String requestId = (String) state.getAttribute(STATE_SITE_QUEST_UNIQNAME);
			if (!isFutureTerm)
			{
				//To site quest account - the instructor of record's
				if (requestId != null)
				{
					try
					{
						User instructor = UserDirectoryService.getUser(requestId);
						from = requestEmail;
						to = instructor.getEmail();
						headerTo = instructor.getEmail();
						replyTo = requestEmail;
						buf.append(rb.getString("java.hello")+" \n\n");
						buf.append(rb.getString("java.receiv")+" " + sessionUserName + ", ");
						buf.append(rb.getString("java.who")+"\n");
						if (termExist)
						{
							buf.append(term.getTerm() + " " + term.getYear() + "\n");
						
						}
						
						// what are the required fields shown in the UI
						List requiredFields = CourseManagementService.getCourseIdRequiredFields();
						for (int i = 0; i < requestListSize; i++)
						{
							List requiredFieldList = (List) requestFields.get(i);
							for (int j = 0; j < requiredFieldList.size(); j++)
							{
								String requiredField = (String) requiredFields.get(j);
								
								buf.append(requiredField +"\t" + requiredFieldList.get(j) + "\n");
							}
						}
						buf.append("\n\n"+rb.getString("java.according")+" " + sessionUserName + " "+rb.getString("java.record"));
						buf.append(" " + rb.getString("java.canyou")+" "  + sessionUserName + " "+ rb.getString("java.assoc")+"\n\n");
						buf.append(rb.getString("java.respond")+" " + sessionUserName + rb.getString("java.appoint")+"\n\n");
						buf.append(rb.getString("java.thanks")+"\n");
						buf.append(productionSiteName + " "+rb.getString("java.support"));
						content = buf.toString();	
						
						// send the email
						EmailService.send(from, to, message_subject, content, headerTo, replyTo, null);
						
						// email has been sent successfully
						sendEmailToRequestee = true;
					}
					catch (UserNotDefinedException ee)
					{
					}	// try
				}
			}
		
			//To Support
			from = UserDirectoryService.getCurrentUser().getEmail();
			to = requestEmail;
			headerTo = requestEmail;
			replyTo = UserDirectoryService.getCurrentUser().getEmail();
			buf.setLength(0);
			buf.append(rb.getString("java.to")+"\t\t" + productionSiteName + " "+rb.getString("java.supp")+"\n");
			buf.append("\n"+rb.getString("java.from")+"\t" + sessionUserName + "\n");
			if (request.equals("new"))
			{
				buf.append(rb.getString("java.subj")+"\t"+rb.getString("java.sitereq")+"\n");
			}
			else
			{
				buf.append(rb.getString("java.subj")+"\t"+rb.getString("java.sitechreq")+"\n");
			}
			buf.append(rb.getString("java.date")+"\t" + local_date + " " + local_time + "\n\n");
			if (request.equals("new"))
			{
				buf.append(rb.getString("java.approval") + " " + productionSiteName + " "+rb.getString("java.coursesite")+" ");
			}
			else
			{
				buf.append(rb.getString("java.approval2")+" " + productionSiteName + " "+rb.getString("java.coursesite")+" ");
			}
			if (termExist)
			{
				buf.append(term.getTerm() + " " + term.getYear());
			}
			if (requestListSize >1)
			{
				buf.append(" "+rb.getString("java.forthese")+" " + requestListSize + " "+rb.getString("java.sections")+"\n\n");
			}
			else
			{
				buf.append(" "+rb.getString("java.forthis")+"\n\n");
			}
			
			//what are the required fields shown in the UI
			List requiredFields = CourseManagementService.getCourseIdRequiredFields();
			for (int i = 0; i < requestListSize; i++)
			{
				List requiredFieldList = (List) requestFields.get(i);
				for (int j = 0; j < requiredFieldList.size(); j++)
				{
					String requiredField = (String) requiredFields.get(j);
					
					buf.append(requiredField +"\t" + requiredFieldList.get(j) + "\n");
				}
			}
			buf.append(rb.getString("java.name")+"\t" + sessionUserName + " (" + noEmailInIdAccountName + " " + sessionUserId + ")\n");
			buf.append(rb.getString("java.email")+"\t" + replyTo + "\n\n");
			buf.append(rb.getString("java.sitetitle")+"\t" + title + "\n"); 
			buf.append(rb.getString("java.siteid")+"\t" +  id + "\n");
			buf.append(rb.getString("java.siteinstr")+"\n"  + additional + "\n\n");
			
			if (!isFutureTerm)
			{
				if (sendEmailToRequestee)
				{
					buf.append(rb.getString("java.authoriz")+" " + requestId + " "+rb.getString("java.asreq"));
				}
				else
				{
					buf.append(rb.getString("java.thesiteemail")+" " + requestId + " "+rb.getString("java.asreq"));
				}
			}
			content = buf.toString();
			EmailService.send(from, to, message_subject, content, headerTo, replyTo, null);
			
			//To the Instructor
			User curUser = UserDirectoryService.getCurrentUser();
			from = requestEmail;
			to = curUser.getEmail();
			headerTo = to;
			replyTo = to;
			buf.setLength(0);
			buf.append(rb.getString("java.isbeing")+" ");
			buf.append(rb.getString("java.meantime")+"\n\n");
			buf.append(rb.getString("java.copy")+"\n\n");
			buf.append(content);
			buf.append("\n"+rb.getString("java.wish")+" " + requestEmail);
			content = buf.toString();
			EmailService.send(from, to, message_subject, content, headerTo, replyTo, null);
			state.setAttribute(REQUEST_SENT, new Boolean(true));
		}	// if
		
	} //  sendSiteRequest
	
	/**
	* Notification sent when a course site is set up automatcally
	* 
	*/
	private void sendSiteNotification(SessionState state, List notifySites)
	{
		// get the request email from configuration
		String requestEmail = ServerConfigurationService.getString("setup.request", null);
		if (requestEmail == null)
		{
			M_log.warn(this + " - no 'setup.request' in configuration");
		}
		else
		{
			// send emails
			Site site = getStateSite(state);
			String id = site.getId();
			String title = site.getTitle();
			Time time = TimeService.newTime();
			String local_time = time.toStringLocalTime();
			String local_date = time.toStringLocalDate();
			String term_name = "";
			if (state.getAttribute(STATE_TERM_SELECTED) != null)
			{
				term_name = ((Term) state.getAttribute(STATE_TERM_SELECTED)).getId();
			}
			String message_subject = rb.getString("java.official")+ " " + UserDirectoryService.getCurrentUser().getDisplayName() + " " + rb.getString("java.for") + " " + term_name;
			
			String from = NULL_STRING;
			String to = NULL_STRING;
			String headerTo = NULL_STRING;
			String replyTo = NULL_STRING;
			String sender = UserDirectoryService.getCurrentUser().getDisplayName();
			String userId = StringUtil.trimToZero(SessionManager.getCurrentSessionUserId());
			
			//To Support
			from = UserDirectoryService.getCurrentUser().getEmail();
			to = requestEmail;
			headerTo = requestEmail;
			replyTo = UserDirectoryService.getCurrentUser().getEmail();
			StringBuffer buf = new StringBuffer();
			buf.append("\n"+rb.getString("java.fromwork")+" " + ServerConfigurationService.getServerName() + " "+rb.getString("java.supp")+":\n\n");
			buf.append(rb.getString("java.off")+" '" + title + "' (id " + id + "), " +rb.getString("java.wasset")+" ");
			buf.append(sender + " (" + userId + ", " + rb.getString("java.email2")+ " " + replyTo + ") ");
			buf.append(rb.getString("java.on")+" " + local_date + " " + rb.getString("java.at")+ " " + local_time + " ");
			buf.append(rb.getString("java.for")+" " + term_name + ", ");
			int nbr_sections = notifySites.size();
			if (nbr_sections >1)
			{
				buf.append(rb.getString("java.withrost")+" " + Integer.toString(nbr_sections) + " "+rb.getString("java.sections")+"\n\n");
			}
			else
			{
				buf.append(" "+rb.getString("java.withrost2")+"\n\n");
			}
			
			for (int i = 0; i < nbr_sections; i++)
			{
				String course = (String) notifySites.get(i);
				buf.append(rb.getString("java.course2")+" " + course + "\n");
			}
			String content = buf.toString();
			EmailService.send(from, to, message_subject, content, headerTo, replyTo, null);
		}	// if

	} //  sendSiteNotification
	
	/**
	* doCancel called when "eventSubmit_doCancel_create" is in the request parameters to c
	*/
	public void doCancel_create ( RunData data )
	{
		// Don't put current form data in state, just return to the previous template
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		removeAddClassContext(state);
		state.setAttribute(STATE_TEMPLATE_INDEX, "0");
		
	} // doCancel_create
	
	/**
	* doCancel called when "eventSubmit_doCancel" is in the request parameters to c
	* int index = Integer.valueOf(params.getString ("template-index")).intValue();
	*/
	public void doCancel ( RunData data )
	{
		// Don't put current form data in state, just return to the previous template
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		ParameterParser params = data.getParameters ();
		
		state.removeAttribute(STATE_MESSAGE);
		
		String currentIndex = (String)state.getAttribute(STATE_TEMPLATE_INDEX);
		
		String backIndex = params.getString("back");
		state.setAttribute(STATE_TEMPLATE_INDEX, backIndex);
		
		if (currentIndex.equals("4"))
		{
			state.removeAttribute(STATE_TOOL_EMAIL_ADDRESS);
			state.removeAttribute(STATE_MESSAGE);
			removeEditToolState(state);
		}
		else if (currentIndex.equals("5"))
		{
			//remove related state variables 
			removeAddParticipantContext(state);
			
			params = data.getParameters ();
			state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("back"));
		}
		else if (currentIndex.equals("6"))
		{
			state.removeAttribute(STATE_REMOVEABLE_USER_LIST);
		}
		else if (currentIndex.equals("9"))
		{
			state.removeAttribute(FORM_WILL_NOTIFY);
		}
		else if (currentIndex.equals("17") || currentIndex.equals("16"))
		{
			state.removeAttribute(FORM_WILL_NOTIFY);
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		}
		else if (currentIndex.equals("13") || currentIndex.equals("14"))
		{
			// clean state attributes
			state.removeAttribute(FORM_SITEINFO_TITLE);
			state.removeAttribute(FORM_SITEINFO_DESCRIPTION);
			state.removeAttribute(FORM_SITEINFO_SHORT_DESCRIPTION);
			state.removeAttribute(FORM_SITEINFO_SKIN);
			state.removeAttribute(FORM_SITEINFO_INCLUDE);
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		}
		else if (currentIndex.equals("15"))
		{
			params = data.getParameters ();
			state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("cancelIndex"));
			removeEditToolState(state);
		}
		//htripath: added 'currentIndex.equals("45")' for import from file cancel
		else if (currentIndex.equals("19") || currentIndex.equals("20") || currentIndex.equals("21") || currentIndex.equals("22")|| currentIndex.equals("45"))
		{
			// from adding participant pages
			//remove related state variables 
			removeAddParticipantContext(state);
			
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		}
		else if (currentIndex.equals("23") || currentIndex.equals("24"))
		{
			// from change global access
			state.removeAttribute("form_joinable");
			state.removeAttribute("form_joinerRole");
			
			state.setAttribute(STATE_TEMPLATE_INDEX, "18");
		}
		else if (currentIndex.equals("7") || currentIndex.equals("25"))
		{
			//from change role
			removeChangeRoleContext(state);
			state.setAttribute(STATE_TEMPLATE_INDEX, "18");
		}
		else if (currentIndex.equals("3"))
		{
			//from adding class
			if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITESETUP))
			{
				state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			}	
			else if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITEINFO))
			{	
				state.setAttribute(STATE_TEMPLATE_INDEX, "18");
			}
		}
		else if (currentIndex.equals("27") || currentIndex.equals("28"))
		{
			// from import
			if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITESETUP))
			{
				// worksite setup
				if (getStateSite(state) == null)
				{
					//in creating new site process
					state.setAttribute(STATE_TEMPLATE_INDEX, "0");
				}
				else
				{
					// in editing site process
					state.setAttribute(STATE_TEMPLATE_INDEX, "12");
				}
			}	
			else if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITEINFO))
			{	
				// site info
				state.setAttribute(STATE_TEMPLATE_INDEX, "12");
			}
			state.removeAttribute(STATE_IMPORT_SITE_TOOL);
			state.removeAttribute(STATE_IMPORT_SITES);
		}
		else if (currentIndex.equals("26"))
		{
			if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITESETUP) 
				&& getStateSite(state) == null)
			{
				//from creating site
				state.setAttribute(STATE_TEMPLATE_INDEX, "0");
			}	
			else 
			{
				//from revising site
				state.setAttribute(STATE_TEMPLATE_INDEX, "12");
			}
			removeEditToolState(state);
		}
		else if (currentIndex.equals("32"))
		{
			//current is Sitemanage participants list
			// cancel back to the site list
			state.setAttribute(STATE_TEMPLATE_INDEX, "31");
			state.removeAttribute("siteId");
		}
		else if (currentIndex.equals("33"))
		{
			// current is Sitemanage add participant view
			// cancel back to the participants list
			state.setAttribute(STATE_TEMPLATE_INDEX, "32");
		}
		else if (currentIndex.equals("33"))
		{
			// current is Sitemanage add participant view
			// cancel back to the participants list
			state.setAttribute(STATE_TEMPLATE_INDEX, "32");
		}
		else if (currentIndex.equals("37") || currentIndex.equals("44"))
		{
			// cancel back to edit class view
			state.setAttribute(STATE_TEMPLATE_INDEX, "43");
			removeAddClassContext(state);
		}
		
	} // doCancel
	
	/**
	* doMenu_customize is called when "eventSubmit_doBack" is in the request parameters
	* Pass parameter to actionForTemplate to request action for backward direction
	*/
	public void doMenu_customize ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		state.setAttribute(STATE_TEMPLATE_INDEX, "15");
	
	}// doMenu_customize
	 	 
	/**
	* doBack_to_list cancels an outstanding site edit, cleans state and returns to the site list
	* 
	*/
	public void doBack_to_list ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		Site site = getStateSite(state);
		if (site != null)
		{
			Hashtable h = (Hashtable) state.getAttribute(STATE_PAGESIZE_SITEINFO);
			h.put(site.getId(), state.getAttribute(STATE_PAGESIZE));
			state.setAttribute(STATE_PAGESIZE_SITEINFO, h);
		}
		
		//restore the page size for Worksite setup tool
		if (state.getAttribute(STATE_PAGESIZE_SITESETUP) != null)
		{
			state.setAttribute(STATE_PAGESIZE, state.getAttribute(STATE_PAGESIZE_SITESETUP));
			state.removeAttribute(STATE_PAGESIZE_SITESETUP);
		}
		
		cleanState(state);
		setupFormNamesAndConstants(state);
		
		state.setAttribute(STATE_TEMPLATE_INDEX, "0");

	}	// doBack_to_list
		
	/**
	* do called when "eventSubmit_do" is in the request parameters to c
	*/
	public void doAdd_custom_link ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		if ((params.getString("name")) == null || (params.getString("url") == null))
		{
			Tool tr = ToolManager.getTool("sakai.iframe");
			Site site = getStateSite(state);
			SitePage page = site.addPage();
			page.setTitle(params.getString("name")); // the visible label on the tool menu
			ToolConfiguration tool = page.addTool();
			tool.setTool("sakai.iframe", tr);
			tool.setTitle(params.getString("name"));
			commitSite(site);
		}
		else
		{
			addAlert(state, rb.getString("java.reqmiss"));
			state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("template-index"));
		}
		
	} // doAdd_custom_link
	/**
	* doAdd_remove_features is called when Make These Changes is clicked in chef_site-addRemoveFeatures
	*/
	public void doAdd_remove_features ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		List existTools = (List) state.getAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
		
		ParameterParser params = data.getParameters ();
		String option = params.getString("option");
		
		// dispatch
		if (option.equalsIgnoreCase("addNews"))
		{
			updateSelectedToolList(state, params, false);
			insertTool(state, "sakai.news", STATE_NEWS_TITLES, NEWS_DEFAULT_TITLE, STATE_NEWS_URLS, NEWS_DEFAULT_URL, Integer.parseInt(params.getString("newsNum")));
			state.setAttribute(STATE_TEMPLATE_INDEX, "26");
		}
		else if (option.equalsIgnoreCase("addWC"))
		{
			updateSelectedToolList(state, params, false);
			insertTool(state, "sakai.iframe", STATE_WEB_CONTENT_TITLES, WEB_CONTENT_DEFAULT_TITLE, STATE_WEB_CONTENT_URLS, WEB_CONTENT_DEFAULT_URL, Integer.parseInt(params.getString("wcNum")));
			state.setAttribute(STATE_TEMPLATE_INDEX, "26");
		}
		else if (option.equalsIgnoreCase("save"))
		{
			List idsSelected = new Vector();
			
			boolean goToENWPage = false;	
			boolean homeSelected = false;
				
			// Add new pages and tools, if any
			if (params.getStrings ("selectedTools") == null)
			{
				addAlert(state, rb.getString("atleastonetool"));
			}
			else
			{
				List l = new ArrayList(Arrays.asList(params.getStrings ("selectedTools"))); // toolId's & titles of chosen tools
					
				for (int i = 0; i < l.size(); i++)
				{
					String toolId = (String) l.get(i);
					
					if (toolId.equals("home"))
					{
						homeSelected = true;
					}
					else if (toolId.equals("sakai.mailbox") || toolId.indexOf("sakai.news") != -1 || toolId.indexOf("sakai.iframe") != -1)
					{
						// if user is adding either EmailArchive tool, News tool or Web Content tool, go to the Customize page for the tool
						if (!existTools.contains(toolId))
						{
							goToENWPage = true;
						}
						
						if (toolId.equals("sakai.mailbox"))
						{
							//get the email alias when an Email Archive tool has been selected
							String channelReference = mailArchiveChannelReference((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
							List aliases = AliasService.getAliases(channelReference, 1, 1);
							if (aliases.size() > 0)
							{
								state.setAttribute(STATE_TOOL_EMAIL_ADDRESS, ((Alias) aliases.get(0)).getId());
							}
						}	
					}
				   idsSelected.add(toolId);
					  
				}
					
				state.setAttribute(STATE_TOOL_HOME_SELECTED, new Boolean(homeSelected));
			}
			
			state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, idsSelected); // List of ToolRegistration toolId's
			
			if (state.getAttribute(STATE_MESSAGE)== null)
			{
				if (goToENWPage)
				{
					// go to the configuration page for Email Archive, News and Web Content tools
					state.setAttribute(STATE_TEMPLATE_INDEX, "26");
				}
				else
				{
					// go to confirmation page
					state.setAttribute(STATE_TEMPLATE_INDEX, "15");
				}
			}
		}
		else if (option.equalsIgnoreCase("continue"))
		{
			// continue
			doContinue(data);
		}
		else if (option.equalsIgnoreCase("Back"))
		{
			//back
			doBack(data);
		}
		else if (option.equalsIgnoreCase("Cancel"))
		{
			//cancel
			doCancel(data);
		}
	} // doAdd_remove_features
	
	/**
	* doSave_revised_features
	*/
	public void doSave_revised_features ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		getRevisedFeatures(params, state);
		
		Site site = getStateSite(state);
		String id = site.getId();
		
		//now that the site exists, we can set the email alias when an Email Archive tool has been selected
		String alias = StringUtil.trimToNull((String) state.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
		if (alias != null)
		{	
			String channelReference = mailArchiveChannelReference(id);
			try
			{
				AliasService.setAlias(alias, channelReference);
			}
			catch (IdUsedException ee) 
			{
			}
			catch (IdInvalidException ee) 
			{
				addAlert(state, rb.getString("java.alias")+" " + alias + " "+rb.getString("java.isinval"));
			}
			catch (PermissionException ee) 
			{
				addAlert(state, rb.getString("java.addalias")+" ");
			}
		}
		if (state.getAttribute(STATE_MESSAGE)== null)
		{
			// clean state variables
			state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
			state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST);
			state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME);
			state.removeAttribute(STATE_NEWS_TITLES);
			state.removeAttribute(STATE_NEWS_URLS);
			state.removeAttribute(STATE_WEB_CONTENT_TITLES);
			state.removeAttribute(STATE_WEB_CONTENT_URLS);
			
			state.setAttribute(STATE_SITE_INSTANCE_ID, id);

			state.setAttribute(STATE_TEMPLATE_INDEX, params.getString ("continue"));
		}
		
		// refresh the whole page
		scheduleTopRefresh();
				
	}	// doSave_revised_features

	/**
	* doMenu_add_participant
	*/
	public void doMenu_add_participant ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.removeAttribute(STATE_SELECTED_USER_LIST);
		state.setAttribute (STATE_TEMPLATE_INDEX, "5");
		
	} //  doMenu_add_participant
	
	/**
	* doMenu_siteInfo_addParticipant
	*/
	public void doMenu_siteInfo_addParticipant ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		state.removeAttribute(STATE_SELECTED_USER_LIST);
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_TEMPLATE_INDEX, "5");
		}
		
	} //  doMenu_siteInfo_addParticipant
	
	/**
	* doMenu_sitemanage_addParticipant
	*/
	public void doMenu_sitemanage_addParticipant ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.setAttribute(STATE_TEMPLATE_INDEX, "33");
		
	} //  doMenu_sitemanage_addParticipant
	
	/**
	 * doMenu_siteInfo_removeParticipant
	 */
	public void doMenu_siteInfo_removeParticipant( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		ParameterParser params = data.getParameters ();
	
		if (params.getStrings ("selectedUser") == null)
		{
			addAlert(state, rb.getString("java.nousers"));
		}
		else
		{
			List removeUser = Arrays.asList(params.getStrings ("selectedUser")); 
			
			// all or some selected user(s) can be removed, go to confirmation page
			if (removeUser.size() > 0)
			{
				state.setAttribute (STATE_TEMPLATE_INDEX, "6");
			}
			else
			{
				addAlert(state, rb.getString("java.however"));
			}
			
			state.setAttribute (STATE_REMOVEABLE_USER_LIST, removeUser);
		}
			
	}	// doMenu_siteInfo_removeParticipant
	
	/**
	* doMenu_siteInfo_changeRole 
	*/
	public void doMenu_siteInfo_changeRole ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		ParameterParser params = data.getParameters ();
		if (params.getStrings ("selectedUser") == null)
		{
			state.removeAttribute(STATE_SELECTED_USER_LIST);
			addAlert(state, rb.getString("java.nousers2"));
		}
		else
		{
			state.setAttribute (STATE_CHANGEROLE_SAMEROLE, Boolean.TRUE);

			List selectedUserIds = Arrays.asList(params.getStrings ("selectedUser"));
			state.setAttribute (STATE_SELECTED_USER_LIST, selectedUserIds);
			
			// get roles for selected participants
			setSelectedParticipantRoles(state);
			
			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				state.setAttribute (STATE_TEMPLATE_INDEX, "7");	
			}
		} 
	
	} // doMenu_siteInfo_changeRole
	
	/**
	 *  doMenu_siteInfo_globalAccess
	 */
	public void doMenu_siteInfo_globalAccess(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "23");
		}
		
	}	//doMenu_siteInfo_globalAccess
	
	/**
	 * doMenu_siteInfo_cancel_access
	 */
	public void doMenu_siteInfo_cancel_access( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		state.removeAttribute(STATE_SELECTED_USER_LIST);
		state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		
	}	// doMenu_siteInfo_cancel_access
	
	/**
	* doMenu_siteInfo_import
	*/
	public void doMenu_siteInfo_import ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		//get the tools
		siteToolsIntoState(state);
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_TEMPLATE_INDEX, "28");
		}
		
	} //  doMenu_siteInfo_import
	
	/**
	 * doMenu_siteInfo_editClass
	 */
	public void doMenu_siteInfo_editClass( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		state.setAttribute (STATE_TEMPLATE_INDEX, "43");
			
	}	// doMenu_siteInfo_editClass
	
	/**
	 * doMenu_siteInfo_addClass
	 */
	public void doMenu_siteInfo_addClass( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		Site site = getStateSite(state);
		state.setAttribute(STATE_TERM_SELECTED, CourseManagementService.getTerm(site.getProperties().getProperty(PROP_SITE_TERM)));
		
		state.setAttribute (STATE_TEMPLATE_INDEX, "36");
			
	}	// doMenu_siteInfo_addClass
	
	/**
	 * first step of adding class
	 */
	public void doAdd_class_select( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		String option = params.getString("option");
		if (option.equalsIgnoreCase("change"))
		{
			// change term
			String termId = params.getString("selectTerm");
			Term t = CourseManagementService.getTerm(termId);
			state.setAttribute(STATE_TERM_SELECTED, t);
		}
		else if (option.equalsIgnoreCase("cancel"))
		{
			// cancel
			state.removeAttribute(STATE_TERM_SELECTED);
			removeAddClassContext(state);
			state.setAttribute (STATE_TEMPLATE_INDEX, "43");
		}
		else if (option.equalsIgnoreCase("add"))
		{
			String userId = StringUtil.trimToZero(SessionManager.getCurrentSessionUserId());
			Term t = (Term) state.getAttribute(STATE_TERM_SELECTED);
			if (t != null)
			{
				List courses = CourseManagementService.getInstructorCourses(userId, t.getYear(), t.getTerm());
				
				// future term? roster information is not available yet?
				int weeks = 0;
				try
				{
					weeks = Integer.parseInt(ServerConfigurationService.getString("roster.available.weeks.before.term.start", "0"));
				}
				catch (Exception ignore)
				{
				}
				if ((courses == null || courses != null && courses.size() == 0)
					&& System.currentTimeMillis() + weeks*7*24*60*60*1000 < t.getStartTime().getTime())
				{
					//if a future term is selected
					state.setAttribute(STATE_FUTURE_TERM_SELECTED, Boolean.TRUE);							
				}
				else
				{
					state.setAttribute(STATE_FUTURE_TERM_SELECTED, Boolean.FALSE);							
				}
			}
			
			// continue
			doContinue(data);
		}
			
	}	// doAdd_class_select
	
	
	/**
	* doMenu_siteInfo_duplicate
	*/
	public void doMenu_siteInfo_duplicate ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_TEMPLATE_INDEX, "29");
		}
		
	} //  doMenu_siteInfo_import
	
	/**
	* doMenu_change_roles 
	*/
	public void doMenu_change_roles ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		if (params.getStrings ("removeUser") != null)
		{
			state.setAttribute(STATE_SELECTED_USER_LIST, new ArrayList(Arrays.asList(params.getStrings ("removeUser"))));
			state.setAttribute (STATE_TEMPLATE_INDEX, "7");
		}
		else
		{
			addAlert(state, rb.getString("java.nousers2"));
		}
		
	} // doMenu_change_roles
	
	/**
	*  doMenu_edit_site_info
	* 
	*/
	public void doMenu_edit_site_info ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		Site Site = getStateSite(state);
		ResourceProperties siteProperties = Site.getProperties(); 
		state.setAttribute(FORM_SITEINFO_TITLE, Site.getTitle());
		
		String site_type = (String)state.getAttribute(STATE_SITE_TYPE); 
		if(site_type != null && !site_type.equalsIgnoreCase("myworkspace")) 
		{
			state.setAttribute(FORM_SITEINFO_INCLUDE, Boolean.valueOf(Site.isPubView()).toString());
		}
		state.setAttribute(FORM_SITEINFO_DESCRIPTION, Site.getDescription());
		state.setAttribute(FORM_SITEINFO_SHORT_DESCRIPTION, Site.getShortDescription());
		state.setAttribute(FORM_SITEINFO_SKIN, Site.getIconUrl());
		if (Site.getIconUrl() != null)
		{
			state.setAttribute(FORM_SITEINFO_SKIN, Site.getIconUrl());
		}
		
		// site contact information
		String contactName = siteProperties.getProperty(PROP_SITE_CONTACT_NAME);
		String contactEmail = siteProperties.getProperty(PROP_SITE_CONTACT_EMAIL);
		if (contactName == null && contactEmail == null)
		{
			String creatorId = siteProperties.getProperty(ResourceProperties.PROP_CREATOR);
			try
			{
				User u = UserDirectoryService.getUser(creatorId);
				String email = u.getEmail();
				if (email != null)
				{
					contactEmail = u.getEmail();	
				}
				contactName = u.getDisplayName();
			}
			catch (UserNotDefinedException e)
			{
			}
		}
		if (contactName != null)
		{
			state.setAttribute(FORM_SITEINFO_CONTACT_NAME, contactName);
		}
		if (contactEmail != null)
		{
			state.setAttribute(FORM_SITEINFO_CONTACT_EMAIL, contactEmail);
		}
				
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "13");
		}
		
	} // doMenu_edit_site_info
	
	/**
	*  doSitemanage_edit_site_info
	* 
	*/
	public void doSitemanage_edit_site_info ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		String siteId = StringUtil.trimToNull(data.getParameters().getString("id"));
		
		if (siteId != null)
		{
			if (SiteService.allowUpdateSite(siteId))
			{
				try
				{
					Site Site = SiteService.getSite(siteId);
					state.setAttribute(STATE_SITE_INSTANCE_ID, siteId);
					state.setAttribute("siteId", siteId);
					
					ResourceProperties siteProperties = Site.getProperties(); 
					state.setAttribute(FORM_SITEINFO_TITLE, Site.getTitle());
	
					String site_type = Site.getType(); 
					if(site_type != null && !site_type.equalsIgnoreCase("myworkspace")) 
					{
						state.setAttribute(FORM_SITEINFO_INCLUDE, Boolean.valueOf(Site.isPubView()).toString());
					}
					state.setAttribute("siteType", site_type);
					state.setAttribute(FORM_SITEINFO_DESCRIPTION, Site.getDescription());
					state.setAttribute(FORM_SITEINFO_SHORT_DESCRIPTION, Site.getShortDescription());
					state.setAttribute(FORM_SITEINFO_SKIN, Site.getIconUrl());
					state.setAttribute("siteIconUrl", Site.getIconUrl());
					if (Site.getIconUrl() != null)
					{
						state.setAttribute(FORM_SITEINFO_SKIN, Site.getIconUrl());
					}
	
					// site contact information
					String contactName = siteProperties.getProperty(PROP_SITE_CONTACT_NAME);
					String contactEmail = siteProperties.getProperty(PROP_SITE_CONTACT_EMAIL);
					if (contactName == null && contactEmail == null)
					{
						String creatorId = siteProperties.getProperty(ResourceProperties.PROP_CREATOR);
						try
						{
							User u = UserDirectoryService.getUser(creatorId);
							String email = u.getEmail();
							if (email != null)
							{
								contactEmail = u.getEmail();	
							}
							contactName = u.getDisplayName();
						}
						catch (UserNotDefinedException e)
						{
						}
					}
					if (contactName != null)
					{
						state.setAttribute(FORM_SITEINFO_CONTACT_NAME, contactName);
					}
					if (contactEmail != null)
					{
						state.setAttribute(FORM_SITEINFO_CONTACT_EMAIL, contactEmail);
					}
			
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						state.setAttribute(STATE_TEMPLATE_INDEX, "38");
					}
					
				}
				catch (IdUnusedException e)
				{
					addAlert(state, rb.getString("java.specif")+" " + siteId + ". ");
					M_log.warn(this + e.toString());
				}
			}
			
			// no permission
			else
			{
				addAlert(state, rb.getString("java.permeditsite")+" " + siteId + ".");
				M_log.warn(this + "no update permission to site : " + siteId);				
			}
		}
		else
		{
			// if siteId is null
			addAlert(state, rb.getString("java.error")+" "); 
			state.setAttribute(STATE_TEMPLATE_INDEX, "31");
		}
		
	} // doSitemanage_edit_site_info
	
	/**
	*  doMenu_edit_site_tools
	* 
	*/
	public void doMenu_edit_site_tools ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// get the tools
		siteToolsIntoState(state);
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "4");
		}
		
	} // doMenu_edit_site_tools
	
	/**
	*  doMenu_edit_site_access
	* 
	*/
	public void doMenu_edit_site_access ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "18");
		}
		
	} // doMenu_edit_site_access
	
	/**
	*  doSitemanage_edit_site_access
	* 
	*/
	public void doSitemanage_edit_site_access ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		String siteId = StringUtil.trimToNull(data.getParameters().getString("id"));
		
		if (siteId != null)
		{
			if (SiteService.allowUpdateSite(siteId))
			{
				try
				{
					SiteService.getSite(siteId);
					state.setAttribute(STATE_SITE_INSTANCE_ID, siteId);
				}
				catch (IdUnusedException e)
				{
					addAlert(state, rb.getString("java.cannot")+" " + siteId + ". ");
					M_log.warn(this + e.toString());
				}
				
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					state.setAttribute(STATE_TEMPLATE_INDEX, "39");
				}
			}
			
			// not permitted
			else
			{
				addAlert(state, rb.getString("java.permeditsite")+ " " + siteId + ".");
				M_log.warn(this + "no update permission to site : " + siteId);				
			}
		}
		else
		{
			// if siteId is null
			addAlert(state, rb.getString("java.error")+" "); 
			state.setAttribute(STATE_TEMPLATE_INDEX, "31");
		}
	} // doSitemanage_edit_site_access

	/**
	*  doMenu_publish_site
	* 
	*/
	public void doMenu_publish_site ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// get the site properties
		sitePropertiesIntoState(state);
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "9");
		}
	
	} // doMenu_publish_site
	
	/**
	*  Back to worksite setup's list view
	* 
	*/
	public void doBack_to_site_list ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.removeAttribute(STATE_SELECTED_USER_LIST);
		state.removeAttribute(STATE_SITE_TYPE);
		state.removeAttribute(STATE_SITE_INSTANCE_ID);
		
		state.setAttribute(STATE_TEMPLATE_INDEX, "0");
	
	} // doBack_to_site_list
	
	/**
	*  doSave_site_info
	* 
	*/
	public void doSave_siteInfo ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		Site Site = getStateSite(state);
		ResourcePropertiesEdit siteProperties = Site.getPropertiesEdit();
		String site_type = (String)state.getAttribute(STATE_SITE_TYPE); 
			
		List titleEditableSiteType = (List) state.getAttribute(TITLE_EDITABLE_SITE_TYPE);
		if (titleEditableSiteType.contains(Site.getType()))
		{
			Site.setTitle((String) state.getAttribute(FORM_SITEINFO_TITLE));
		}

		Site.setDescription((String) state.getAttribute(FORM_SITEINFO_DESCRIPTION));
		Site.setShortDescription((String) state.getAttribute(FORM_SITEINFO_SHORT_DESCRIPTION));

		if(site_type != null) 
		{
			if (site_type.equals("course"))
			{
				//set icon url for course
				String skin = (String) state.getAttribute(FORM_SITEINFO_SKIN);
				setAppearance(state, Site, skin);
			}
			else
			{
				//set icon url for others
				String iconUrl = (String) state.getAttribute(FORM_SITEINFO_ICON_URL);
				Site.setIconUrl(iconUrl);
			}
				
		}
		
		// site contact information
		String contactName = (String) state.getAttribute(FORM_SITEINFO_CONTACT_NAME);
		if (contactName != null)
		{
			siteProperties.addProperty(PROP_SITE_CONTACT_NAME, contactName);
		}
		
		String contactEmail = (String) state.getAttribute(FORM_SITEINFO_CONTACT_EMAIL);
		if (contactEmail != null)
		{
			siteProperties.addProperty(PROP_SITE_CONTACT_EMAIL, contactEmail);
		}
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			try
			{
				SiteService.save(Site);
			}
			catch (IdUnusedException e)
			{
				// TODO:
			}
			catch (PermissionException e)
			{
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
			
			//refresh the whole page
			scheduleTopRefresh();
				
		}
	} 	// doSave_siteInfo
	
	/**
	* Handle a request to search in the sitemanage tool.
	*/
	public void doSitemanage_search(RunData data, Context context)
	{
		super.doSearch(data, context);
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());
		
		state.removeAttribute(STATE_PROP_SEARCH_MAP);
		state.removeAttribute(STATE_TERM_SELECTION);
		
		// read the search form field into the state object
		String siteType = StringUtil.trimToNull(data.getParameters().getString("siteType"));
		if ((siteType == null) || (siteType.equalsIgnoreCase("Any")))
		{
			state.setAttribute(STATE_SEARCH_SITE_TYPE, null);
		}
		else
		{
			state.setAttribute(STATE_SEARCH_SITE_TYPE, siteType);
			
			String termSearchSiteType = (String)state.getAttribute(SEARCH_TERM_SITE_TYPE);
			if (termSearchSiteType != null)
			{
				if (siteType.equals(termSearchSiteType))
				{
					// search parameter - term; termId from UI	
					String term = StringUtil.trimToNull(data.getParameters().getString("selectTerm"));
					if (term != null)
					{
						state.setAttribute(STATE_TERM_SELECTION, term);
						
						// property criteria map
						Map pMap = null;
						if (!SITE_TERM_ANY.equals(term))
						{
							pMap = new HashMap();
							pMap.put((String)state.getAttribute(SEARCH_TERM_PROP), term);
							state.setAttribute(STATE_PROP_SEARCH_MAP, pMap);
						}
					}
					
				}
			}
		}

		state.setAttribute(STATE_PAGESIZE, new Integer(DEFAULT_PAGE_SIZE));
		state.removeAttribute("inter_size");
		
		state.setAttribute(STATE_TEMPLATE_INDEX, "31");
		
	}	// doSitemanage_search
	
	/**
	* Handle a request to go to Search Mode.
	*/
	public void doSitemanage_showsearch(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		state.setAttribute(STATE_TEMPLATE_INDEX, "30");

	}	// doSitemanage_showsearch
	
	/**
	* Handle a request to finalize the participants adding/role-assigning.
	*/
	public void doSitemanage_participants_save(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		ParameterParser params = data.getParameters ();
		
		String emailInIdAccountName = ServerConfigurationService.getString("emailInIdAccountName", "");
		
		String index = state.getAttribute(STATE_TEMPLATE_INDEX).toString();
		
		if(state.getAttribute("form_same_role") != null)
		{
			boolean same_role = ((Boolean) state.getAttribute("form_same_role")).booleanValue();
			if (same_role)
			{
				String roleId = StringUtil.trimToNull(params.getString("selectRole"));
				if (roleId == null)
				{
					addAlert(state, rb.getString("java.pleasesel")+" ");
				}
				else
				{
					state.setAttribute("form_selectedRole", params.getString("selectRole"));
				}
			}
			else
			{
				getSelectedRoles(state, params, STATE_ADD_PARTICIPANTS);
			}
			
			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				Hashtable selectedRoles = (Hashtable) state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES);
				int i;
		
				//accept noEmailInIdAccounts and/or emailInIdAccount account names
				String noEmailInIdAccounts = null;
				String emailInIdAccounts = null;
		
				if (state.getAttribute("noEmailInIdAccountValue") != null)
				{
					noEmailInIdAccounts = (String) state.getAttribute("noEmailInIdAccountValue");
				}
				if (state.getAttribute("emailInIdAccountValue") != null)
				{
					emailInIdAccounts = (String) state.getAttribute("emailInIdAccountValue");
				}
		
				String pw = "";
				String notAddedNames = null;
				String notAddedEmailInIdAccounts = null;

				Vector addedNames = new Vector();
				if (noEmailInIdAccounts != null)
				{
					// adding noEmailInIdAccounts
					String[] noEmailInIdAccountArray = noEmailInIdAccounts.split("\r\n");
					for (i = 0; i < noEmailInIdAccountArray.length; i++)
					{
						String noEmailInIdAccount = StringUtil.trimToNull(noEmailInIdAccountArray[i].replaceAll("[ \t\r\n]",""));
						if(noEmailInIdAccount != null)
						{
							// get role
							String role = null;
							if (same_role)
							{
								// if all added participants have a same role
								role = (String) state.getAttribute("form_selectedRole");
							}
							else
							{
								// if all added participants have different role
								role = (String) selectedRoles.get(noEmailInIdAccount);
							}
					
							if (addUserRealm(state, noEmailInIdAccount, role))
							{
								// successfully added
								addedNames.add(noEmailInIdAccount);
							}
							else
							{
								notAddedNames=notAddedNames.concat(noEmailInIdAccount);
							}
						}
					}
				}	// noEmailInIdAccounts					

				Vector addedEmailInIdAccounts = new Vector();
				if (emailInIdAccounts != null)
				{
					String[] emailInIdAccountArray = emailInIdAccounts.split("\r\n");
	
					for (i = 0; i < emailInIdAccountArray.length; i++)
					{
						String emailInIdAccount = StringUtil.trimToNull(emailInIdAccountArray[i].replaceAll("[ \t\r\n]",""));
				
						// remove the trailing dots and empty space
						while (emailInIdAccount.endsWith(".") || emailInIdAccount.endsWith(" "))
						{
							emailInIdAccount = emailInIdAccount.substring(0, emailInIdAccount.length() -1);
						}

						if(emailInIdAccount != null)
						{	
							//is the emailInIdAccount account user already exists?
							try
							{
								UserDirectoryService.getUser(emailInIdAccount);
							}
							catch (UserNotDefinedException e) 
							{
								//if there is no such user yet, add the user
								try
								{
									UserEdit uEdit = UserDirectoryService.addUser(null, emailInIdAccount);

									//set email address
									uEdit.setEmail(emailInIdAccount);
							
									// set the guest user type
									uEdit.setType("guest");

									// set password to a random positive number
									Random generator = new Random(System.currentTimeMillis());
									Integer num = new Integer(generator.nextInt(Integer.MAX_VALUE));
									if (num.intValue() < 0) num = new Integer(num.intValue() *-1);
									pw = num.toString();
									uEdit.setPassword(pw);

									// and save
									UserDirectoryService.commitEdit(uEdit);
								 }
								 catch(UserIdInvalidException ee)
								 {
									 addAlert(state, emailInIdAccountName + " " + emailInIdAccount + " " +rb.getString("java.isinval") );
									 M_log.warn("doSitemanage_participants_save: UserDirectoryService addUser exception " + e.getMessage());
								 }
								 catch(UserAlreadyDefinedException ee)
								 {
									 addAlert(state, emailInIdAccountName + " " + emailInIdAccount + " " +rb.getString("java.beenused") );
									 M_log.warn("doSitemanage_participants_save: UserDirectoryService addUser exception " + e.getMessage());
								 }
								 catch(UserPermissionException ee)
								 {
									 addAlert(state, rb.getString("java.haveadd")+" " + emailInIdAccount);
									 M_log.warn("doSitemanage_participants_save: UserDirectoryService addUser exception " + e.getMessage());
								 }	
							}
					
							// get role 
							String role = null;
							if (same_role)
							{
								// if all added participants have a same role
								role = (String) state.getAttribute("form_selectedRole"); 
							}
							else
							{
								// if all added participants have different role
								role = (String) selectedRoles.get(emailInIdAccount);
							}
						
							// add property role to the emailInIdAccount account
							if (addUserRealm(state, emailInIdAccount, role))
							{
								// emailInIdAccount account has been added successfully
								addedEmailInIdAccounts.add(emailInIdAccount);
							}
							else
							{
								notAddedEmailInIdAccounts = notAddedEmailInIdAccounts.concat(emailInIdAccount + "\n");
							}
						}	// if
					}	// 	
				} // emailInIdAccounts

				if (!(addedNames.size() == 0 && addedEmailInIdAccounts.size() == 0) && (notAddedNames != null || notAddedEmailInIdAccounts != null))
				{
					// at lease one noEmailInIdAccount account or a emailInIdAccount account added
					addAlert(state, rb.getString("java.allusers"));
				}

				if (notAddedNames == null && notAddedEmailInIdAccounts == null)
				{
					// all account has been added successfully
					removeAddParticipantContext(state);
				}
				else
				{
					state.setAttribute("noEmailInIdAccountValue", notAddedNames);
					state.setAttribute("emailInIdAccountValue", notAddedEmailInIdAccounts);
				}
				if (state.getAttribute(STATE_MESSAGE) != null)
				{
					state.setAttribute(STATE_TEMPLATE_INDEX, index);
				}
				else
				{
					// save the updates and go back to the participants list view
					state.setAttribute(STATE_TEMPLATE_INDEX, "32");
					//commitSiteAndRemoveEdit(state);
				}
			}
			else
			{
				state.setAttribute(STATE_TEMPLATE_INDEX, index);
			}
		}
	} // doSitemanage_participants_save
	
	
	/**
	* doParticipants is the request to go to participants list/edit view
	*/
	public void doSitemanage_participants(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());
		
		String siteId = data.getParameters().getString("id");
		state.setAttribute("siteId", siteId);
		
		if (siteId != null)
		{
			if (SiteService.allowUpdateSite(siteId))
			{
				try
				{
					state.setAttribute(STATE_SITE_INSTANCE_ID, siteId);
					state.setAttribute(STATE_TEMPLATE_INDEX, "32");
				}
				catch(Exception e)
				{
					addAlert(state, rb.getString("java.problem"));
					// stay at the same mode
				}
			}
			
			// no permission
			else
			{
				addAlert(state, rb.getString("java.problem"));
				// stay at the same mode				
			}
		}
		
		state.setAttribute(STATE_PAGESIZE, new Integer(DEFAULT_PAGE_SIZE));
		cleanStatePaging(state);
		
	}	// doSitemanage_participants
	
	/**
	*  init
	* 
	*/
	private void init (VelocityPortlet portlet, RunData data, SessionState state)
	{
		state.setAttribute(STATE_ACTION, "SiteAction");
		setupFormNamesAndConstants(state);
		
		if (state.getAttribute(STATE_PAGESIZE_SITEINFO) == null)
		{
			state.setAttribute(STATE_PAGESIZE_SITEINFO, new Hashtable());
		}
		
		if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITESETUP))
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "0");
		}
		else if (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITEINFO))
		{
			
			String siteId = ToolManager.getCurrentPlacement().getContext();
			getReviseSite(state, siteId);
			Hashtable h = (Hashtable) state.getAttribute(STATE_PAGESIZE_SITEINFO);
			if (!h.containsKey(siteId))
			{	
				// update
				h.put(siteId, new Integer(200));
				state.setAttribute(STATE_PAGESIZE_SITEINFO, h);
				state.setAttribute(STATE_PAGESIZE, new Integer(200));
			}
		}
		else if  (((String) state.getAttribute(STATE_SITE_MODE)).equalsIgnoreCase(SITE_MODE_SITEMANAGE))
		{
			// mode to display search page mode
			state.setAttribute(STATE_TEMPLATE_INDEX, "30");
			
			String termSearchSiteType = ServerConfigurationService.getString("sitebrowser.termsearch.type");
			if (termSearchSiteType != null)
			{
				state.setAttribute(SEARCH_TERM_SITE_TYPE, termSearchSiteType);
	
				String termSearchProperty = ServerConfigurationService.getString("sitebrowser.termsearch.property");
				state.setAttribute(SEARCH_TERM_PROP, termSearchProperty);
			}
			
			String noSearchSiteType = StringUtil.trimToNull(ServerConfigurationService.getString("sitesearch.noshow.sitetype"));
			if (noSearchSiteType != null)
			{
				state.setAttribute(NO_SHOW_SEARCH_TYPE, noSearchSiteType);
			}
		}
		if (state.getAttribute(STATE_SITE_TYPES) == null)
		{
			PortletConfig config = portlet.getPortletConfig();
			
			// all site types
			String t = StringUtil.trimToNull(config.getInitParameter("siteTypes"));
			if (t != null)
			{
				state.setAttribute(STATE_SITE_TYPES, new ArrayList(Arrays.asList(t.split(","))));
			}
			else
			{
				state.setAttribute(STATE_SITE_TYPES, new Vector());
			}
		}
	} // init
	
	public void doNavigate_to_site ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		String siteId = StringUtil.trimToNull(data.getParameters().getString("option"));
		if (siteId != null)
		{
			getReviseSite(state, siteId);
		}
		else
		{
			doBack_to_list(data);
		}
	
	}	// doNavigate_to_site
	
	/**
	 * Get site information for revise screen
	 */
	private void getReviseSite(SessionState state, String siteId)
	{
		if (state.getAttribute(STATE_SELECTED_USER_LIST) == null)
		{
			state.setAttribute(STATE_SELECTED_USER_LIST, new Vector());
		}
		 
		List sites = (List) state.getAttribute(STATE_SITES);
		 
		try
		{
			Site site = SiteService.getSite(siteId);
			state.setAttribute(STATE_SITE_INSTANCE_ID, site.getId());
		
			if (sites != null)
			{
				int pos = -1;
				for (int index=0;index<sites.size() && pos==-1;index++)
				{
					if (((Site) sites.get(index)).getId().equals(siteId))
				 	{
				 		pos = index;
				 	}
				}
				 
				// has any previous site in the list?
				if (pos > 0)
				{
					state.setAttribute(STATE_PREV_SITE, sites.get(pos-1));
				}
				else
				{
				 	state.removeAttribute(STATE_PREV_SITE);
				}
				 
				//has any next site in the list?
				if (pos < sites.size()-1)
				{
					state.setAttribute(STATE_NEXT_SITE,sites.get(pos+1));
				}
				else
				{
				 	state.removeAttribute(STATE_NEXT_SITE);
				}
			}
			 
			String type = site.getType();
			if (type == null)
			{
				if (state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null)
				{
					type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);
				}
			}
			state.setAttribute(STATE_SITE_TYPE, type);
			
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + e.toString());
		}
		 
		//one site has been selected
		state.setAttribute(STATE_TEMPLATE_INDEX, "12");
			 
	}	// getReviseSite
	
	/**
	*  doUpdate_participant
	* 
	*/
	public void doUpdate_participant(RunData data)
	{ 
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		Site s = getStateSite(state);
		String realmId = SiteService.siteReference(s.getId());
		if (AuthzGroupService.allowUpdate(realmId) || SiteService.allowUpdateSiteMembership(s.getId()))
		{
			try
			{
				AuthzGroup realmEdit = AuthzGroupService.getAuthzGroup(realmId);
				// update participant roles
				List participants = (List) state.getAttribute(STATE_PARTICIPANT_LIST);;
				// remove all roles and then add back those that were checked
				for (int i=0; i<participants.size(); i++)
				{
					String id = null;
					
					// added participant
					Object participant = (Object) participants.get(i);
					
					if (participant.getClass().equals(Participant.class))
					{	
						id = ((Participant) participant).getUniqname();
					}
					else if (participant.getClass().equals(CourseMember.class))
					{
						// course member
						id = ((CourseMember) participant).getUniqname();
					}
						
					if (id != null)
					{
						//get the newly assigned role
						String inputRoleField = "role" + id;
						String roleId = params.getString(inputRoleField);
					
						// only change roles when they are different than before
						if (roleId!= null)
						{
							// get the grant active status
							boolean activeGrant = true;
							String activeGrantField = "activeGrant" + id;
							if (params.getString(activeGrantField) != null)
							{
								activeGrant = params.getString(activeGrantField).equalsIgnoreCase("true")?true:false;
							}
							
							boolean fromProvider = false;
							if (participant.getClass().equals(CourseMember.class))
							{
								if (roleId.equals(((CourseMember) participant).getProviderRole()))
								{
									fromProvider = true;
								}
							}
							realmEdit.addMember(id, roleId, activeGrant, fromProvider);
						}
					}
				}
				
				//remove selected users
				if (params.getStrings ("selectedUser") != null)
				{
					List removals = new ArrayList(Arrays.asList(params.getStrings ("selectedUser")));
					state.setAttribute(STATE_SELECTED_USER_LIST, removals);
					for(int i = 0; i<removals.size(); i++)
					{
						String rId = (String) removals.get(i);
						try
						{
							User user = UserDirectoryService.getUser(rId);
							Participant selected = new Participant();
							selected.name = user.getDisplayName();
							selected.uniqname = user.getId();
							realmEdit.removeMember(user.getId());
						}
						catch (UserNotDefinedException e)
						{
							M_log.warn(this + " IdUnusedException " + rId + ". ");
						}
					}
				}
				
				String maintainRoleString = realmEdit.getMaintainRole();
				if (realmEdit.getUsersHasRole(maintainRoleString).isEmpty())
				{
					// if after update, there is no maintainer role user for the site, show alert message and don't save the update
					addAlert(state, rb.getString("sitegen.siteinfolist.nomaintainuser") + maintainRoleString + ".");
				}
				else
				{
					AuthzGroupService.save(realmEdit);
				}
			}
			catch (GroupNotDefinedException e)
			{
				addAlert(state, rb.getString("java.problem2"));
				M_log.warn(this + "  IdUnusedException " + s.getTitle() + "(" + realmId + "). ");
			}
			catch(AuthzPermissionException e)
			{
				addAlert(state, rb.getString("java.changeroles"));
				M_log.warn(this + "  PermissionException " + s.getTitle() + "(" + realmId + "). ");
			}
		}
		
	} // doUpdate_participant
	
	
	/**
	*  doUpdate_site_access
	* 
	*/
	public void doUpdate_site_access(RunData data)
	{ 
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		Site sEdit = getStateSite(state);
		
		ParameterParser params = data.getParameters ();
		String publishUnpublish = params.getString("publishunpublish");
		String include = params.getString("include");
		String joinable = params.getString("joinable");
		
		if (sEdit != null)
		{
			// editing existing site
			//publish site or not
			if (publishUnpublish != null&& publishUnpublish.equalsIgnoreCase("publish"))
			{
				sEdit.setPublished(true);
			}
			else
			{
				sEdit.setPublished(false);
			}
			
			//site public choice
			if (include != null)
			{
				// if there is pubview input, use it
				sEdit.setPubView(include.equalsIgnoreCase("true")?true:false);
			}
			else if (state.getAttribute(STATE_SITE_TYPE) != null)
			{
				String type = (String) state.getAttribute(STATE_SITE_TYPE);
				List publicSiteTypes = (List) state.getAttribute(STATE_PUBLIC_SITE_TYPES);
				List privateSiteTypes = (List) state.getAttribute(STATE_PRIVATE_SITE_TYPES);
				
				if (publicSiteTypes.contains(type))
				{
					//sites are always public
					sEdit.setPubView(true);
				}
				else if (privateSiteTypes.contains(type))
				{
					//site are always private
					sEdit.setPubView(false);
				}
			}
			else
			{
				sEdit.setPubView(false);
			}
			
			//publish site or not
			if (joinable != null && joinable.equalsIgnoreCase("true"))
			{
				state.setAttribute(STATE_JOINABLE, Boolean.TRUE);
				sEdit.setJoinable(true);
				String joinerRole = StringUtil.trimToNull(params.getString("joinerRole"));
				if (joinerRole!= null)
				{
					state.setAttribute(STATE_JOINERROLE, joinerRole);
					sEdit.setJoinerRole(joinerRole);
				}
				else
				{
					state.setAttribute(STATE_JOINERROLE, "");
					addAlert(state, rb.getString("java.joinsite")+" ");
				}
			}
			else
			{
				state.setAttribute(STATE_JOINABLE, Boolean.FALSE);
				state.removeAttribute(STATE_JOINERROLE);
				sEdit.setJoinable(false);
				sEdit.setJoinerRole(null);
			}
			
			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				commitSite(sEdit);
				state.setAttribute(STATE_TEMPLATE_INDEX, "12");

				// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
				schedulePeerFrameRefresh("sitenav");

				state.removeAttribute(STATE_JOINABLE);
				state.removeAttribute(STATE_JOINERROLE);
			}
		}
		else
		{
			// adding new site
			if(state.getAttribute(STATE_SITE_INFO) != null)
			{
				SiteInfo siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
				
				if (publishUnpublish != null&& publishUnpublish.equalsIgnoreCase("publish"))
				{
					siteInfo.published = true;
				}
				else
				{
					siteInfo.published = false;
				}
				
				//site public choice
				if (include != null)
				{
					siteInfo.include = include.equalsIgnoreCase("true")?true:false;
				}
				else if (StringUtil.trimToNull(siteInfo.site_type) != null)
				{
					String type = StringUtil.trimToNull(siteInfo.site_type);
					List publicSiteTypes = (List) state.getAttribute(STATE_PUBLIC_SITE_TYPES);
					List privateSiteTypes = (List) state.getAttribute(STATE_PRIVATE_SITE_TYPES);
					
					if (publicSiteTypes.contains(type))
					{
						//sites are always public
						siteInfo.include = true;
					}
					else if (privateSiteTypes.contains(type))
					{
						//site are always private
						siteInfo.include = false;
					}
				}
				else
				{
					siteInfo.include = false;
				}
				
				//joinable site or not
				if (joinable != null && joinable.equalsIgnoreCase("true"))
				{
					siteInfo.joinable = true;
					String joinerRole = StringUtil.trimToNull(params.getString("joinerRole"));
					if (joinerRole!= null)
					{
						siteInfo.joinerRole = joinerRole;
					}
					else
					{
						addAlert(state, rb.getString("java.joinsite")+" ");
					}
				}
				else
				{
					siteInfo.joinable = false;
					siteInfo.joinerRole = null;
				}
				
				state.setAttribute(STATE_SITE_INFO, siteInfo);
			}
			
			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				state.setAttribute(STATE_TEMPLATE_INDEX, "10");
				updateCurrentStep(state, true);
			}
		}
		
		// if editing an existing site, refresh the whole page so that the publish/unpublish icon could be updated
		if (sEdit != null)
		{
			scheduleTopRefresh();
		}
	
	} // doUpdate_site_access
	
	
	/**
	*  doSitemanage_update_site_access
	* 
	*/
	public void doSitemanage_update_site_access(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		Site sEdit = getStateSite(state);
			
		ParameterParser params = data.getParameters ();
				
		// publish site or not
		String publishUnpublish = params.getString("publishunpublish");
		if (publishUnpublish != null&& publishUnpublish.equalsIgnoreCase("publish"))
		{
			sEdit.setPublished(true);
		}
		else
		{
			sEdit.setPublished(false);
		}
				
		//site public choice
		String include = params.getString("include");
		if (include != null && include.equalsIgnoreCase("true"))
		{
			sEdit.setPubView(true);
		}
		else if (include != null && include.equalsIgnoreCase("false"))
		{
			sEdit.setPubView(false);
		}
				
		//publish site or not
		String joinable = params.getString("joinable");
		if (joinable != null && joinable.equalsIgnoreCase("true"))
		{
			sEdit.setJoinable(true);
			String joinerRole = params.getString("joinerRole");
			if (joinerRole!= null)
			{
				sEdit.setJoinerRole(joinerRole);
			}
		}
		else
		{
			sEdit.setJoinable(false);
			sEdit.setJoinerRole(null);
		}
		
		try
		{
			SiteService.save(sEdit);
		}
		catch (IdUnusedException e)
		{
			// TODO:
		}
		catch (PermissionException e)
		{
			// TODO:
		}
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "31");
			state.removeAttribute(STATE_SITE_INSTANCE_ID);
			state.removeAttribute("siteId");
		}
		else
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "39");
		}
		
		
	}
	/**
	 * remove related state variable for changing participants roles
	 * @param state SessionState object
	 */
	private void removeChangeRoleContext(SessionState state)
	{
		// remove related state variables
		state.removeAttribute(STATE_CHANGEROLE_SAMEROLE);
		state.removeAttribute(STATE_CHANGEROLE_SAMEROLE_ROLE);
		state.removeAttribute(STATE_ADD_PARTICIPANTS);
		state.removeAttribute(STATE_SELECTED_USER_LIST);
		state.removeAttribute(STATE_SELECTED_PARTICIPANT_ROLES);
		
	}	// removeChangeRoleContext
	
	/**
	/* Actions for vm templates under the "chef_site" root. This method is called by doContinue.
	*  Each template has a hidden field with the value of template-index that becomes the value of
	* index for the switch statement here. Some cases not implemented.
	*/
	private void actionForTemplate ( String direction, int index, ParameterParser params, SessionState state)
	{
		//	Continue - make any permanent changes, Back - keep any data entered on the form
		boolean forward = direction.equals("continue") ? true : false;
		
		SiteInfo siteInfo = new SiteInfo();
		
		switch (index)
		{
			case 0: 
				/* actionForTemplate chef_site-list.vm
				*
				*/
				break;
			case 1: 
				/* actionForTemplate chef_site-type.vm
				 *
				 */
				break;
			case 2: 
				/* actionForTemplate chef_site-newSiteInformation.vm
				*
				*/
				if(state.getAttribute(STATE_SITE_INFO) != null)
				{
					siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
				}
				
				//defaults to be true
				siteInfo.include=true;
				
				state.setAttribute(STATE_SITE_INFO, siteInfo);
				updateSiteInfo(params, state);
				
				String siteType = (String) state.getAttribute(STATE_SITE_TYPE);
				if (!siteType.equalsIgnoreCase("course"))
				{
					String form_title = params.getString ("title");
					// alerts after clicking Continue but not Back
					if(forward)
					{
						if ((form_title == null) || (form_title.trim().length() == 0))
						{
							addAlert(state, rb.getString("java.reqfields"));
							state.setAttribute(STATE_TEMPLATE_INDEX, "2");
							return;
						}
						if (!SiteService.allowAddSite(form_title))
						{
							addAlert(state, rb.getString("java.haveadd")+" " + form_title + ".");
							return;
						}
					}
				}
				updateSiteAttributes(state);
				
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					updateCurrentStep(state, forward);
				}
				
				break;
			case 3:
				/* actionForTemplate chef_site-newSiteFeatures.vm
				*
				*/
				if (forward)
				{
					getFeatures(params, state);
				}
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					updateCurrentStep(state, forward);
				}
				break;

			case 4:
				/* actionForTemplate chef_site-addRemoveFeature.vm
				*
				*/
				break;
			case 5:
				/* actionForTemplate chef_site-addParticipant.vm 
				*
				*/
				if(forward) 
				{
					checkAddParticipant(params, state);
				}
				else
				{
					// remove related state variables
					removeAddParticipantContext(state);		
				}
				break;
			case 6:
				/* actionForTemplate chef_site-removeParticipants.vm
				*
				*/
				
				break;
			case 7:
				/* actionForTemplate chef_site-changeRoles.vm
				*
				*/
				if (forward)
				{
					if (!((Boolean) state.getAttribute(STATE_CHANGEROLE_SAMEROLE)).booleanValue())
					{
						getSelectedRoles(state, params, STATE_SELECTED_USER_LIST);
					}
					else
					{
						String role = params.getString("role_to_all");
						if (role == null)
						{
							addAlert(state, rb.getString("java.pleasechoose")+" ");
						}
						else
						{
							state.setAttribute(STATE_CHANGEROLE_SAMEROLE_ROLE, role);
						}
					}
				}
				else
				{
					removeChangeRoleContext(state);
				}
				break;
			case 8:
				/* actionForTemplate chef_site-siteDeleteConfirm.vm
				*
				*/
				break;
			case 9:
				/* actionForTemplate chef_site-publishUnpublish.vm
				*
				*/
				updateSiteInfo(params, state);
				break;
			case 10:
				/* actionForTemplate chef_site-newSiteConfirm.vm
				*
				*/
				if (!forward)
				{
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						updateCurrentStep(state, false);
					}
				}
				break;
			case 11:
				/* actionForTemplate chef_site_newsitePublishUnpublish.vm
				*
				*/
				break;
			case 12:
				/* actionForTemplate chef_site_siteInfo-list.vm
				*
				*/
				break;
			case 13:
				/* actionForTemplate chef_site_siteInfo-editInfo.vm
				*
				*/
				if (forward)
				{
					Site Site = getStateSite(state);
					
					List titleEditableSiteType = (List) state.getAttribute(TITLE_EDITABLE_SITE_TYPE);
					if (titleEditableSiteType.contains(Site.getType()))
					{
						// site titel is editable and could not be null
						String title = StringUtil.trimToNull(params.getString("title"));
						state.setAttribute(FORM_SITEINFO_TITLE, title);
						if (title == null)
						{
							addAlert(state, rb.getString("java.specify")+" ");
						}
					}
	
					String description = StringUtil.trimToNull(params.getString("description"));
					state.setAttribute(FORM_SITEINFO_DESCRIPTION, description);
						
					String short_description = StringUtil.trimToNull(params.getString("short_description"));
					state.setAttribute(FORM_SITEINFO_SHORT_DESCRIPTION, short_description);
						
					String skin = params.getString("skin");
					if (skin != null)
					{
						// if there is a skin input for course site
						skin = StringUtil.trimToNull(skin);
						state.setAttribute(FORM_SITEINFO_SKIN, skin);
					}
					else
					{
						// if ther is a icon input for non-course site
						String icon = StringUtil.trimToNull(params.getString("icon"));		
						if (icon != null)
						{
							state.setAttribute(FORM_SITEINFO_ICON_URL, icon);
						}
						else
						{
							state.removeAttribute(FORM_SITEINFO_ICON_URL);
						}
					}
					
					// site contact information
					String contactName = StringUtil.trimToZero(params.getString ("siteContactName"));
					state.setAttribute(FORM_SITEINFO_CONTACT_NAME, contactName);
					
					String email = StringUtil.trimToZero(params.getString ("siteContactEmail"));
					String[] parts = email.split("@");
					if(email.length() > 0 && (email.indexOf("@") == -1 || parts.length != 2 || parts[0].length() == 0 || !Validator.checkEmailLocal(parts[0])))
					{
						// invalid email
						addAlert(state, email + " "+rb.getString("java.invalid") + INVALID_EMAIL);
					}
					state.setAttribute(FORM_SITEINFO_CONTACT_EMAIL, email);
					
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						state.setAttribute(STATE_TEMPLATE_INDEX, "14");
					}
				}
				break;			
			case 14:
				/* actionForTemplate chef_site_siteInfo-editInfoConfirm.vm
				*
				*/
				break;
			case 15:
				/* actionForTemplate chef_site_siteInfo-addRemoveFeatureConfirm.vm
				*
				*/
				break;
			case 16:
				/* actionForTemplate chef_site_siteInfo-publishUnpublish-sendEmail.vm
				*
				*/
				if (forward)
				{
					String notify = params.getString("notify");
					if (notify != null)
					{
						state.setAttribute(FORM_WILL_NOTIFY, new Boolean(notify));
					}
				}
				break;
			case 17:
				/* actionForTemplate chef_site_siteInfo--publishUnpublish-confirm.vm
				*
				*/
				if (forward) 
				{
					boolean oldStatus =  getStateSite(state).isPublished();
					boolean newStatus = ((SiteInfo) state.getAttribute(STATE_SITE_INFO)).getPublished();
					saveSiteStatus(state, newStatus);
				
					if (oldStatus == false || newStatus == true)
					{
						// if site's status been changed from unpublish to publish and notification is selected, send out notification to participants.
						if (((Boolean) state.getAttribute(FORM_WILL_NOTIFY)).booleanValue())
						{
							// %%% place holder for sending email
						}
					}
	
					// commit site edit
					Site site = getStateSite(state);
					
					try
					{
						SiteService.save(site);
					}
					catch (IdUnusedException e)
					{
						// TODO:
					}
					catch (PermissionException e)
					{
						// TODO:
					}
					
					// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
					schedulePeerFrameRefresh("sitenav");
				}
				break;
			case 18:
				/*  actionForTemplate chef_siteInfo-editAccess.vm
				* 
				*/
				if (!forward)
				{
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						updateCurrentStep(state, false);
					}
				}
			case 19:
				/*  actionForTemplate chef_site-addParticipant-sameRole.vm
				* 
				*/
				String roleId = StringUtil.trimToNull(params.getString("selectRole"));
				if (roleId == null && forward)
				{
					addAlert(state, rb.getString("java.pleasesel")+" ");
				}
				else
				{
					state.setAttribute("form_selectedRole", params.getString("selectRole"));
				}
				break;
			case 20:
				/*  actionForTemplate chef_site-addParticipant-differentRole.vm
				* 
				*/
				if (forward)
				{
					getSelectedRoles(state, params, STATE_ADD_PARTICIPANTS);
				}
				break;
			case 21:
				/*  actionForTemplate chef_site-addParticipant-notification.vm
				* 
'				*/
				if (params.getString("notify") == null)
				{
					if (forward)
					addAlert(state, rb.getString("java.pleasechoice")+" ");
				}
				else
				{
					state.setAttribute("form_selectedNotify", new Boolean(params.getString("notify")));
				}
				break;
			case 22:
				/*  actionForTemplate chef_site-addParticipant-confirm.vm
				* 
				*/
				break;
			case 23:
				/*  actionForTemplate chef_siteInfo-editAccess-globalAccess.vm
				* 
				*/
				if (forward)
				{
					String joinable = params.getString("joinable");
					state.setAttribute("form_joinable", Boolean.valueOf(joinable));
					String joinerRole = params.getString("joinerRole");
					state.setAttribute("form_joinerRole", joinerRole);
					if (joinable.equals("true"))
					{
						if (joinerRole == null)
						{
							addAlert(state, rb.getString("java.pleasesel")+" ");
						}
					}
				}
				else
				{
				}
				break;
			case 24:
				/*  actionForTemplate chef_site-siteInfo-editAccess-globalAccess-confirm.vm
				* 
				*/
				break;
			case 25:
				/*  actionForTemplate chef_site-changeRoles-confirm.vm
				* 
				*/
				break;
			case 26: 
				/*  actionForTemplate chef_site-modifyENW.vm
				* 
				*/
				updateSelectedToolList(state, params, forward);
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					updateCurrentStep(state, forward);
				}
				break;
			case 27: 
				/*  actionForTemplate chef_site-importSites.vm
				* 
				*/
				if (forward)
				{
					Site existingSite = getStateSite(state);
					if (existingSite != null)
					{
						// revising a existing site's tool
						select_import_tools(params, state);
						Hashtable importTools = (Hashtable) state.getAttribute(STATE_IMPORT_SITE_TOOL);
						List selectedTools = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
						importToolIntoSite(selectedTools, importTools, existingSite);
						
						if (state.getAttribute(STATE_MESSAGE) == null)
						{
							commitSite(existingSite);
							state.removeAttribute(STATE_IMPORT_SITE_TOOL);
							state.removeAttribute(STATE_IMPORT_SITES);
						}
					}
					else
					{
						// new site
						select_import_tools(params, state);
					}
				}
				else
				{
					// read form input about import tools
					select_import_tools(params, state);
				}
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					updateCurrentStep(state, forward);
				}
				break;
			case 28: 
				/*  actionForTemplate chef_siteinfo-import.vm
				* 
				*/
				if (forward)
				{
					if (params.getStrings("importSites") == null)
					{
						addAlert(state, rb.getString("java.toimport")+" ");
					}
					else
					{
						List importSites = new ArrayList(Arrays.asList(params.getStrings("importSites")));
						Hashtable sites = (Hashtable) state.getAttribute(STATE_IMPORT_SITES);
						if (sites == null)
						{
							sites = new Hashtable();
						}
						for (index = 0; index < importSites.size(); index ++)
						{
							try
							{
								Site s = SiteService.getSite((String) importSites.get(index));
								if	(!sites.containsKey(s))
								{
									sites.put(s, new Vector());
								}
							}
							catch (IdUnusedException e)
							{	
							}
						}
						state.setAttribute(STATE_IMPORT_SITES, sites);
					}
				}
				break;
			case 29:
				/*  actionForTemplate chef_siteinfo-duplicate.vm
				* 
				*/
				if (forward)
				{
					if (state.getAttribute(SITE_DUPLICATED) == null)
					{
						if (StringUtil.trimToNull(params.getString("title")) == null)
						{
							addAlert(state, rb.getString("java.dupli")+" ");
						}
						else
						{
							String title = params.getString("title");
							state.setAttribute(SITE_DUPLICATED_NAME, title);
							
							try
							{
								String oSiteId = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);
								String nSiteId = IdManager.createUuid();
								Site site = SiteService.addSite(nSiteId, getStateSite(state));
								
								try
								{
									SiteService.save(site);
								}
								catch (IdUnusedException e)
								{
									// TODO:
								}
								catch (PermissionException e)
								{
									// TODO:
								}
								
								try 
								{
									site = SiteService.getSite(nSiteId);
									
									// set title
									site.setTitle(title);
									// import tool content
									List pageList = site.getPages();
									if (!((pageList == null) || (pageList.size() == 0)))
									{
										for (ListIterator i = pageList.listIterator(); i.hasNext(); )
										{
											SitePage page = (SitePage) i.next();
		
											List pageToolList = page.getTools();
											String toolId = ((ToolConfiguration)pageToolList.get(0)).getTool().getId();
											transferCopyEntities(toolId, oSiteId, nSiteId);
										}
									}
								} 
								catch (Exception e1) 
								{
									//if goes here, IdService or SiteService has done something wrong.
									M_log.warn(this + "Exception" + e1 + ":"+ nSiteId + "when duplicating site");
								}

								try
								{
									SiteService.save(site);
								}
								catch (IdUnusedException e)
								{
									// TODO:
								}
								catch (PermissionException e)
								{
									// TODO:
								}
								
								// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
								schedulePeerFrameRefresh("sitenav");
								
								state.setAttribute(SITE_DUPLICATED, Boolean.TRUE);
							}
							catch (IdInvalidException e)
							{
								addAlert(state, rb.getString("java.siteinval"));
							}
							catch (IdUsedException e)
							{
								addAlert(state, rb.getString("java.sitebeenused")+" ");
							}
							catch (PermissionException e)
							{
								addAlert(state, rb.getString("java.allowcreate")+" ");
							}
						}
					}
					
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						// site duplication confirmed
						state.removeAttribute(SITE_DUPLICATED);
						state.removeAttribute(SITE_DUPLICATED_NAME);
					
						// return to the list view
						state.setAttribute(STATE_TEMPLATE_INDEX, "12");
					}
				}
				break;
			case 33:
				/*  
				* actionForTemplate chef_site-sitemanage-addParticipants.vm
				*/
				checkAddParticipant(params, state);
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					if(state.getAttribute("form_same_role") != null)
					{
						boolean same_role = ((Boolean) state.getAttribute("form_same_role")).booleanValue();
						if (same_role)
						{
							state.setAttribute(STATE_TEMPLATE_INDEX, "34");
						}
						else
						{
							state.setAttribute(STATE_TEMPLATE_INDEX, "35");
						}
					}
				}
				else
				{
					state.setAttribute(STATE_TEMPLATE_INDEX, "33");
				}
				break;
			case 36:
				/*
				 * actionForTemplate chef_site-newSiteCourse.vm
				 */
				if (forward)
				{
					List providerChosenList = new Vector();
					if (params.getStrings("providerCourseAdd") == null)
					{
						state.removeAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
						if (params.getString("manualAdds") == null)
						{
							addAlert(state, rb.getString("java.manual")+" ");
						}
					}
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						// The list of courses selected from provider listing
						if (params.getStrings("providerCourseAdd") != null)
						{
							providerChosenList = new ArrayList(Arrays.asList(params.getStrings("providerCourseAdd"))); // list of course ids
							state.setAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN, providerChosenList);
						}

						if (state.getAttribute(STATE_MESSAGE) == null)
						{	
							siteInfo = new SiteInfo();
							if(state.getAttribute(STATE_SITE_INFO) != null)
							{
								siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
							}
							if (providerChosenList.size() >= 1)
							{
								siteInfo.title = getCourseTab(state, (String) providerChosenList.get(0));
							}
							state.setAttribute(STATE_SITE_INFO, siteInfo);
							
							if (params.getString("manualAdds") != null)
							{
								// if creating a new site
								state.setAttribute(STATE_TEMPLATE_INDEX, "37");
								state.setAttribute(STATE_MANUAL_ADD_COURSE_NUMBER, new Integer(1));
							}
							else
							{	
								// no manual add
								state.removeAttribute(STATE_MANUAL_ADD_COURSE_NUMBER);
								state.removeAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
								state.removeAttribute(STATE_SITE_QUEST_UNIQNAME);
								
								if (getStateSite(state) != null)
								{
									// if revising a site, go to the confirmation page of adding classes
									state.setAttribute(STATE_TEMPLATE_INDEX, "44");
								}
								else
								{
									// if creating a site, go the the site information entry page
									state.setAttribute(STATE_TEMPLATE_INDEX, "2");
								}
							}
						}
					}
					
					//next step
					state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer(2));
				}
				break;
			case 38:
				/*
				 * actionForTemplate chef_site-sitemange-editInfo.vm
				 */
				if (forward)
				{
					Site siteEdit = getStateSite(state);
					ResourcePropertiesEdit siteProperties = siteEdit.getPropertiesEdit();
					if (SecurityService.isSuperUser())
					{
						String title = StringUtil.trimToNull(params.getString("title"));
						state.setAttribute(FORM_SITEINFO_TITLE, title);
						siteEdit.setTitle(title);
					}
					
					String type = StringUtil.trimToNull(params.getString("type"));
					state.setAttribute(STATE_SITEMANAGE_SITETYPE, type);
					siteEdit.setType(type);
					
					String description = StringUtil.trimToNull(params.getString("description"));
					state.setAttribute(FORM_SITEINFO_DESCRIPTION, description);
					siteEdit.setDescription(description);
							
					String short_description = StringUtil.trimToNull(params.getString("short_description"));
					state.setAttribute(FORM_SITEINFO_SHORT_DESCRIPTION, short_description);
					siteEdit.setShortDescription(short_description);

					// "skin" will be the icon from the list (course sites), or we have the icon as a full URL
					String skin = StringUtil.trimToNull(params.getString("skin"));
					state.setAttribute(FORM_SITEINFO_SKIN, skin);
					if (skin != null)
					{
						setAppearance(state, siteEdit, skin);
					}
					else
					{
						String iconUrl = StringUtil.trimToNull(params.getString("icon"));
						state.setAttribute("siteIconUrl", iconUrl);
						siteEdit.setIconUrl(iconUrl);
					}
						
					String include = StringUtil.trimToNull(params.getString("include"));		
					if (include != null && include.equalsIgnoreCase(Boolean.FALSE.toString()))
					{
						state.setAttribute(FORM_SITEINFO_INCLUDE, Boolean.FALSE.toString());
						siteEdit.setPubView(false);
					}
					else
					{
						state.setAttribute(FORM_SITEINFO_INCLUDE, Boolean.TRUE.toString());
						siteEdit.setPubView(true);
					}
						
					// site contact information
					String contactName = StringUtil.trimToZero(params.getString ("siteContactName"));
					state.setAttribute(FORM_SITEINFO_CONTACT_NAME, contactName);
					siteProperties = siteEdit.getPropertiesEdit();
					if (contactName != null)
					{
						siteProperties.addProperty(PROP_SITE_CONTACT_NAME, contactName);
					}
			
					String email = StringUtil.trimToZero(params.getString ("siteContactEmail"));
					String[] parts = email.split("@");
					if(email.length() > 0 && (email.indexOf("@") == -1 || parts.length != 2 || parts[0].length() == 0 || !Validator.checkEmailLocal(parts[0])))
					{
						// invalid email
						addAlert(state, email + " "+rb.getString("java.invalid") + INVALID_EMAIL);
					}
					state.setAttribute(FORM_SITEINFO_CONTACT_EMAIL, email);
					if (email != null)
					{
						siteProperties.addProperty(PROP_SITE_CONTACT_EMAIL, email);
					}
					
					// for site size limit
					String size = params.getString("size");
					if (size != null)
					{
						String currentSiteId = ToolManager.getCurrentPlacement().getContext();
						String rootCollectionId = ContentHostingService.getSiteCollection(currentSiteId);

						try 
						{
							ContentCollectionEdit cedit = ContentHostingService.editCollection(rootCollectionId);
							ResourcePropertiesEdit pedit = cedit.getPropertiesEdit();
							
							// default 1 GB = 1,048,576 Kilobyte
							String quota = "1048576";
						
							if (size.equals(DEFAULT_SITE_SIZE_LIMIT))
							{
								state.setAttribute(STATE_SITE_SIZE_DEFAULT_SELECT, Boolean.TRUE);
								// set the quota
								pedit.addProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA, quota);
							}
							else
							{
								String otherSize = StringUtil.trimToZeroLower(params.getString("otherSize"));
								if (otherSize.equals(""))
								{
									addAlert(state, rb.getString("java.pleasech"));
									state.setAttribute(STATE_SITE_SIZE_DEFAULT_SELECT, Boolean.FALSE);
								}
								else
								{
									String[] strings = null;
									long unit = 1;
									if (otherSize.endsWith("kb")){strings = StringUtil.splitFirst(otherSize,"kb");}
									else if (otherSize.endsWith("kilobytes")){strings = StringUtil.splitFirst(otherSize,"kilobytes");}
									else if (otherSize.endsWith("kilobyte")){strings = StringUtil.splitFirst(otherSize,"kilobyte");}
									else if (otherSize.endsWith("mb")){strings = StringUtil.splitFirst(otherSize,"mb"); unit = 1024;}
									else if (otherSize.endsWith("megabytes")){strings = StringUtil.splitFirst(otherSize,"megabytes"); unit = 1024;}
									else if (otherSize.endsWith("megabyte")){strings = StringUtil.splitFirst(otherSize,"megabytes"); unit = 1024;}
									else if (otherSize.endsWith("gb")){strings = StringUtil.splitFirst(otherSize,"gb"); unit = 1048576;}
									else if (otherSize.endsWith("gigabytes")){strings = StringUtil.splitFirst(otherSize,"gigabytes"); unit = 1048576;}
									else if (otherSize.endsWith("gigabyte")){strings = StringUtil.splitFirst(otherSize,"gigabyte"); unit = 1048576;}
									
									if (strings != null)
									{
										try
										{
											// strings{digital strings, size unit "kb/mb/gb"}
											int intSize = Integer.parseInt(strings[0]);
											unit = intSize * unit; // size is transferred to KB
											pedit.addProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA, (new Long(unit)).toString());										
										}
										catch(NumberFormatException error)
										{
											addAlert(state, rb.getString("java.pleaseval"));
										}
									}
									else
									{
										addAlert(state, rb.getString("java.pleaseval"));
									}
								} // if-else
							} // if-else
							
							ContentHostingService.commitCollection(cedit);
						} 
						catch (IdUnusedException e) 
						{
						} 
						catch (TypeException e) 
						{
						} 
						catch (PermissionException e) 
						{
						} 
						catch (InUseException e) 
						{
							addAlert(state, rb.getString("java.someone"));
						}
					}
						
					try
					{
						SiteService.save(siteEdit);
					}
					catch (IdUnusedException e)
					{
						// TODO:
					}
					catch (PermissionException e)
					{
						// TODO:
					}

					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						state.setAttribute(STATE_TEMPLATE_INDEX, "31");
					}
				}
				break;
			case 39:
				/*
				 * actionForTemplate chef_site-sitemange-editAccess.vm
				 */
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					updateCurrentStep(state, forward);
				}
				break;
			case 42:
				/* actionForTemplate chef_site-gradtoolsConfirm.vm
				*
				*/
				break;
			case 43:
				/* actionForTemplate chef_site-editClass.vm
				*
				*/
				if (forward)
				{
					if (params.getStrings("providerClassDeletes") == null && params.getStrings("manualClassDeletes") == null && 
							!direction.equals("back"))
					{
						addAlert(state, rb.getString("java.classes"));
					}
					
					if (params.getStrings("providerClassDeletes") != null)
					{
						// build the deletions list
						List providerCourseList = (List) state.getAttribute(SITE_PROVIDER_COURSE_LIST);
						List providerCourseDeleteList = new ArrayList(Arrays.asList(params.getStrings("providerClassDeletes")));
						for (ListIterator i = providerCourseDeleteList.listIterator(); i.hasNext(); )
						{
							providerCourseList.remove((String) i.next());
						}
						state.setAttribute(SITE_PROVIDER_COURSE_LIST, providerCourseList);
					}
					if (params.getStrings("manualClassDeletes") != null)
					{
						// build the deletions list
						List manualCourseList = (List) state.getAttribute(SITE_MANUAL_COURSE_LIST);
						List manualCourseDeleteList = new ArrayList(Arrays.asList(params.getStrings("manualClassDeletes")));
						for (ListIterator i = manualCourseDeleteList.listIterator(); i.hasNext(); )
						{
							manualCourseList.remove((String) i.next());
						}
						state.setAttribute(SITE_MANUAL_COURSE_LIST, manualCourseList);
					}
					
					updateCourseClasses (state, new Vector(), new Vector());
				}
				break;
			case 44:
				if (forward)
				{
					List providerList = (state.getAttribute(SITE_PROVIDER_COURSE_LIST) == null)?new Vector():(List) state.getAttribute(SITE_PROVIDER_COURSE_LIST);
					List addProviderList = (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) == null)?new Vector():(List) state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN);
					providerList.addAll(addProviderList);
					state.setAttribute(SITE_PROVIDER_COURSE_LIST, providerList);
					
					if (state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null)
					{
						// if manually added course
						List manualList = (state.getAttribute(SITE_MANUAL_COURSE_LIST) == null)?new Vector():(List) state.getAttribute(SITE_MANUAL_COURSE_LIST);
						int manualAddNumber = ((Integer) state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER)).intValue();
						
						List manualAddFields = (List) state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS);
						
						Term t = (Term) state.getAttribute(STATE_TERM_SELECTED);
						for (int m=0; m<manualAddNumber && t!=null; m++)
						{
							String manualAddClassId = CourseManagementService.getCourseId(t, (List) manualAddFields.get(m));
							manualList.add(manualAddClassId);
						}
						state.setAttribute(SITE_MANUAL_COURSE_LIST, manualList);
					}
					
					updateCourseClasses(state, (List) state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN), (List) state.getAttribute(SITE_MANUAL_COURSE_LIST));
					
					removeAddClassContext(state);
				}
				
				break;
			case 49: 
				if (!forward)
				{
					state.removeAttribute(SORTED_BY);
					state.removeAttribute(SORTED_ASC);
				}
				break;
		}
		
	}// actionFor Template
	
	/**
	 * update current step index within the site creation wizard
	 * @param state The SessionState object
	 * @param forward Moving forward or backward?
	 */
	private void updateCurrentStep(SessionState state, boolean forward)
	{
		if (state.getAttribute(SITE_CREATE_CURRENT_STEP) != null)
		{
			int currentStep = ((Integer) state.getAttribute(SITE_CREATE_CURRENT_STEP)).intValue();
			if (forward)
			{
				state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer(currentStep+1));
			}
			else
			{
				state.setAttribute(SITE_CREATE_CURRENT_STEP, new Integer(currentStep-1));
			}
		}
	}
	
	/**
	 * remove related state variable for adding class
	 * @param state SessionState object
	 */
	private void removeAddClassContext(SessionState state)
	{
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
		
	}	// removeAddClassContext

	
	private void updateCourseClasses (SessionState state, List notifyClasses, List requestClasses)
	{
		List providerCourseSectionList = (List) state.getAttribute(SITE_PROVIDER_COURSE_LIST);
		List manualCourseSectionList = (List) state.getAttribute(SITE_MANUAL_COURSE_LIST);
		Site site = getStateSite(state);
		String id = site.getId();
		String realmId = SiteService.siteReference(id);
		
		if ((providerCourseSectionList == null) || (providerCourseSectionList.size() == 0))
		{
			//no section access so remove Provider Id
			try
			{
				AuthzGroup realmEdit1 = AuthzGroupService.getAuthzGroup(realmId);
				realmEdit1.setProviderGroupId(NULL_STRING);
				AuthzGroupService.save(realmEdit1);
			}
			catch (GroupNotDefinedException e)
			{
				M_log.warn(this + " IdUnusedException, " + site.getTitle() + "(" + realmId + ") not found, or not an AuthzGroup object");
				addAlert(state, rb.getString("java.cannotedit"));
				return; 
			}
			catch (AuthzPermissionException e)
			{
				M_log.warn(this + " PermissionException, user does not have permission to edit AuthzGroup object " + site.getTitle() + "(" + realmId + "). ");
				addAlert(state, rb.getString("java.notaccess"));
				return;
			}
		}
		if ((providerCourseSectionList != null) && (providerCourseSectionList.size() != 0))
		{
			// section access so rewrite Provider Id
			String externalRealm = buildExternalRealm(id, state, providerCourseSectionList);
			try
			{
				AuthzGroup realmEdit2 = AuthzGroupService.getAuthzGroup(realmId);
				realmEdit2.setProviderGroupId(externalRealm);
				AuthzGroupService.save(realmEdit2);
			}
			catch (GroupNotDefinedException e)
			{
				M_log.warn(this + " IdUnusedException, " + site.getTitle() + "(" + realmId + ") not found, or not an AuthzGroup object");
				addAlert(state, rb.getString("java.cannotclasses"));
				return;
			}
			catch (AuthzPermissionException e)
			{
				M_log.warn(this + " PermissionException, user does not have permission to edit AuthzGroup object " + site.getTitle() + "(" + realmId + "). ");
				addAlert(state, rb.getString("java.notaccess"));
				return;
			}
	
		}
		
		if ((manualCourseSectionList != null) && (manualCourseSectionList.size() != 0))
		{
			// store the manually requested sections in one site property
			String manualSections = "";
			for (int j = 0; j < manualCourseSectionList.size(); )
			{
				manualSections = manualSections + (String) manualCourseSectionList.get(j);
				j++;
				if (j < manualCourseSectionList.size())
				{
					manualSections = manualSections + "+";
				}
			}
			ResourcePropertiesEdit rp = site.getPropertiesEdit();
			rp.addProperty(PROP_SITE_REQUEST_COURSE, manualSections);
		}
		else
		{
			ResourcePropertiesEdit rp = site.getPropertiesEdit();
			rp.removeProperty(PROP_SITE_REQUEST_COURSE);
		}
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			commitSite(site);
		}
		else
		{
		}
		if (requestClasses != null && requestClasses.size() > 0 && state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null)
		{
			try
			{
				// send out class request notifications
				sendSiteRequest(	state, 
								"change", 
								((Integer) state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER)).intValue(),
								(List) state.getAttribute(STATE_MANUAL_ADD_COURSE_FIELDS));
			}
			catch (Exception e)
			{
				M_log.warn(this + e.toString());
			}
		}
		if (notifyClasses != null && notifyClasses.size() > 0)
		{
			try
			{
				// send out class access confirmation notifications
				sendSiteNotification(state, notifyClasses);
			}
			catch (Exception e)
			{
				M_log.warn(this + e.toString());
			}
		}
	} // updateCourseClasses
	
	
	/**
	 * Sets selected roles for multiple users
	 * @param params The ParameterParser object
	 * @param listName The state variable
	 */
	private void getSelectedRoles(SessionState state, ParameterParser params, String listName)
	{
		Hashtable pSelectedRoles = (Hashtable) state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES);
		if (pSelectedRoles == null)
		{
			pSelectedRoles = new Hashtable();
		}
		List userList = (List) state.getAttribute(listName);
		for (int i = 0; i < userList.size(); i++)
		{
			String userId = null;
			
			if(listName.equalsIgnoreCase(STATE_ADD_PARTICIPANTS))
			{
				userId = ((Participant) userList.get(i)).getUniqname();
			}
			else if (listName.equalsIgnoreCase(STATE_SELECTED_USER_LIST))
			{
				userId = (String) userList.get(i);
			}
			
			if (userId != null)
			{
				String rId = StringUtil.trimToNull(params.getString("role" + userId));
				if (rId == null)
				{
					addAlert(state, rb.getString("java.rolefor")+" " + userId + ". ");
					pSelectedRoles.remove(userId);
				}
				else
				{
					pSelectedRoles.put(userId, rId);
				}
			}
		}
		state.setAttribute(STATE_SELECTED_PARTICIPANT_ROLES, pSelectedRoles);
		
	}	// getSelectedRoles

	/**
	 * dispatch function for changing participants roles
	 */
	public void doSiteinfo_edit_role(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		String option = params.getString("option");
		// dispatch
		if (option.equalsIgnoreCase("same_role_true"))
		{
			state.setAttribute(STATE_CHANGEROLE_SAMEROLE, Boolean.TRUE);
			state.setAttribute(STATE_CHANGEROLE_SAMEROLE_ROLE, params.getString("role_to_all"));
		}
		else if (option.equalsIgnoreCase("same_role_false"))
		{
			state.setAttribute(STATE_CHANGEROLE_SAMEROLE, Boolean.FALSE);
			state.removeAttribute(STATE_CHANGEROLE_SAMEROLE_ROLE);
			if (state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES) == null)
			{
				state.setAttribute(STATE_SELECTED_PARTICIPANT_ROLES, new Hashtable());
			}
		}
		else if (option.equalsIgnoreCase("continue"))
		{
			doContinue(data);
		}
		else if (option.equalsIgnoreCase("back"))
		{
			doBack(data);
		}
		else if (option.equalsIgnoreCase("cancel"))
		{
			doCancel(data);
		}
	}	// doSiteinfo_edit_globalAccess
	
	
	/**
	 * dispatch function for changing site global access
	 */
	public void doSiteinfo_edit_globalAccess(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		String option = params.getString("option");

		// dispatch
		if (option.equalsIgnoreCase("joinable"))
		{
			state.setAttribute("form_joinable", Boolean.TRUE);
			state.setAttribute("form_joinerRole", getStateSite(state).getJoinerRole());
		}
		else if (option.equalsIgnoreCase("unjoinable"))
		{
			state.setAttribute("form_joinable", Boolean.FALSE);
			state.removeAttribute("form_joinerRole"); 
		}
		else if (option.equalsIgnoreCase("continue"))
		{
			doContinue(data);
		}
		else if (option.equalsIgnoreCase("cancel"))
		{
			doCancel(data);
		}
	}	// doSiteinfo_edit_globalAccess
	
	/**
	 * save changes to site global access
	 */
	public void doSiteinfo_save_globalAccess(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		Site s = getStateSite(state);
		boolean joinable = ((Boolean) state.getAttribute("form_joinable")).booleanValue();
		s.setJoinable(joinable);
		if (joinable)
		{
			// set the joiner role
			String joinerRole = (String) state.getAttribute("form_joinerRole");
			s.setJoinerRole(joinerRole);
		}
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			//release site edit
			commitSite(s);
			
			state.setAttribute(STATE_TEMPLATE_INDEX, "18");
		}
		
	}	// doSiteinfo_save_globalAccess
	
	/**
	* updateSiteAttributes
	*
	*/
	private void updateSiteAttributes (SessionState state)
	{
		SiteInfo siteInfo = new SiteInfo();
		if (state.getAttribute(STATE_SITE_INFO) != null)
		{
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
		}
		else
		{
			M_log.warn("SiteAction.updateSiteAttributes STATE_SITE_INFO == null");
			return;
		}
		
		Site site = getStateSite(state);
		
		if (site != null)
		{
			if (StringUtil.trimToNull(siteInfo.title) != null)
			{
				site.setTitle(siteInfo.title);
			}
			if (siteInfo.description != null) 
			{
				site.setDescription(siteInfo.description);
			}
			site.setPublished(siteInfo.published);

			setAppearance(state, site, siteInfo.iconUrl);

			site.setJoinable(siteInfo.joinable);
			if (StringUtil.trimToNull(siteInfo.joinerRole) != null)
			{
				site.setJoinerRole(siteInfo.joinerRole);
			}
			// Make changes and then put changed site back in state
			String id = site.getId();

			try
			{
				SiteService.save(site);
			}
			catch (IdUnusedException e)
			{
				// TODO:
			}
			catch (PermissionException e)
			{
				// TODO:
			}

			if (SiteService.allowUpdateSite(id))
			{
				try
				{
					SiteService.getSite(id);
					state.setAttribute(STATE_SITE_INSTANCE_ID, id);
				}
				catch (IdUnusedException e)
				{
					M_log.warn("SiteAction.commitSite IdUnusedException " + siteInfo.getTitle() + "(" + id + ") not found");
				}
			}
			
			// no permission
			else
			{
				addAlert(state, rb.getString("java.makechanges"));
				M_log.warn("SiteAction.commitSite PermissionException " + siteInfo.getTitle() + "(" + id + ")");
			}
		}
		 
	} // updateSiteAttributes
	
	/**
	* %%% legacy properties, to be removed
	*/
	private void updateSiteInfo (ParameterParser params, SessionState state)
	{
		SiteInfo siteInfo = new SiteInfo();
		if(state.getAttribute(STATE_SITE_INFO) != null)
		{
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);
		}
		siteInfo.site_type = (String) state.getAttribute(STATE_SITE_TYPE);
		
		if (params.getString ("title") != null)
		{
			siteInfo.title = params.getString ("title");
		}
		if (params.getString ("description") != null)
		{
			siteInfo.description = params.getString ("description");
		}
		if (params.getString ("short_description") != null)
		{
			siteInfo.short_description = params.getString ("short_description");
		}
		if (params.getString ("additional") != null)
		{
			siteInfo.additional = params.getString ("additional");
		}
		if (params.getString ("iconUrl") != null)
		{
			siteInfo.iconUrl = params.getString ("iconUrl");
		}
		else
		{
			siteInfo.iconUrl = params.getString ("skin");
		}
		if (params.getString ("joinerRole") != null)
		{
			siteInfo.joinerRole = params.getString ("joinerRole");
		}
		if (params.getString ("joinable") != null)
		{
			boolean joinable = params.getBoolean("joinable");
			siteInfo.joinable = joinable;
			if(!joinable) siteInfo.joinerRole = NULL_STRING;
		}
		if (params.getString ("itemStatus") != null)
		{
			siteInfo.published = Boolean.valueOf(params.getString ("itemStatus")).booleanValue();
		}
		
		// site contact information
		String name = StringUtil.trimToZero(params.getString ("siteContactName"));
		siteInfo.site_contact_name = name;
		String email = StringUtil.trimToZero(params.getString ("siteContactEmail"));
		if (email != null)
		{
			String[] parts = email.split("@");
		
			if(email.length() > 0 && (email.indexOf("@") == -1 || parts.length != 2 || parts[0].length() == 0 || !Validator.checkEmailLocal(parts[0])))
			{
				// invalid email
				addAlert(state, email + " "+rb.getString("java.invalid") + INVALID_EMAIL);
			}
			siteInfo.site_contact_email = email;
		}
		state.setAttribute(STATE_SITE_INFO, siteInfo);

	} // updateSiteInfo
	
	/**
	* getExternalRealmId
	*
	*/
	private String getExternalRealmId (SessionState state)
	{
		String realmId = SiteService.siteReference((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
		String rv = null;
		try
		{
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			rv = realm.getProviderGroupId();
		}
		catch (GroupNotDefinedException e)
		{
			M_log.warn("SiteAction.getExternalRealmId, site realm not found");
		}
		return rv;
		
	} // getExternalRealmId
	
	/**
	* getParticipantList
	*
	*/
	private List getParticipantList(SessionState state)
	{
		List members = new Vector();
		List participants = new Vector();
		String realmId = SiteService.siteReference((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
		
		List providerCourseList = null;
		providerCourseList = getProviderCourseList(StringUtil.trimToNull(getExternalRealmId(state)));
		if (providerCourseList != null && providerCourseList.size() > 0)
		{
			state.setAttribute(SITE_PROVIDER_COURSE_LIST, providerCourseList);
		}
		
		if (providerCourseList != null)
		{
			for (int k = 0; k < providerCourseList.size(); k++)
			{
				String courseId = (String) providerCourseList.get(k);
				try
				{
					members.addAll(CourseManagementService.getCourseMembers(courseId));
				}
				catch (Exception e)
				{
					// M_log.warn(this + " Cannot find course " + courseId);
				}
			}
		}
		
		try
		{
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			Set grants = realm.getMembers();
			//Collections.sort(users);
			for (Iterator i = grants.iterator(); i.hasNext();)
			{
				Member g = (Member) i.next();
				String userString = g.getUserId();
				Role r = g.getRole();
				
				boolean alreadyInList = false;
				for (Iterator p = members.iterator(); p.hasNext() && !alreadyInList;)
				{
					CourseMember member = (CourseMember) p.next();
					String memberUniqname = member.getUniqname();
					if (userString.equalsIgnoreCase(memberUniqname))
					{
						alreadyInList = true;
						if (r != null)
						{
							member.setRole(r.getId());
						}
						participants.add(member);
					}
				}
				
				if (!alreadyInList)
				{
					try
					{
						User user = UserDirectoryService.getUser(userString);
						Participant participant = new Participant();
						participant.name = user.getSortName();
						participant.uniqname = user.getId();
						if (r != null)
						{
							participant.role = r.getId();
						}
						participants.add(participant);
					}
					catch (UserNotDefinedException e)
					{
						// deal with missing user quietly without throwing a warning message
					}
				}
			}
		}
		catch (GroupNotDefinedException e)
		{
			M_log.warn(this + "  IdUnusedException " + realmId);
		}
		
		state.setAttribute(STATE_PARTICIPANT_LIST, participants);
		
		return participants;
		
	} // getParticipantList
	
	/**
	* getRoles
	*
	*/
	private List getRoles (SessionState state)
	{
		List roles = new Vector();
		String realmId = SiteService.siteReference((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
		try
		{
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			roles.addAll(realm.getRoles());
			Collections.sort(roles);
		}
		catch (GroupNotDefinedException e)
		{
			M_log.warn("SiteAction.getRoles IdUnusedException " + realmId);
		}
		return roles;
		
	} // getRoles

	private void getRevisedFeatures(ParameterParser params, SessionState state)
	{	
		Site site = getStateSite(state);
		//get the list of Worksite Setup configured pages
		List wSetupPageList = (List)state.getAttribute(STATE_WORKSITE_SETUP_PAGE_LIST);
		
		WorksiteSetupPage wSetupPage = new WorksiteSetupPage();
		WorksiteSetupPage wSetupHome = new WorksiteSetupPage();
		List pageList = new Vector();
		
		//declare some flags used in making decisions about Home, whether to add, remove, or do nothing
		boolean homeInChosenList = false;
		boolean homeInWSetupPageList = false;
		
		
		List chosenList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		//if features were selected, diff wSetupPageList and chosenList to get page adds and removes
		// boolean values for adding synoptic views
		boolean hasAnnouncement = false;
		boolean hasChat = false;
		boolean hasDiscussion = false;
		boolean hasEmail = false;
		boolean hasNewSiteInfo = false;
		
		//Special case - Worksite Setup Home comes from a hardcoded checkbox on the vm template rather than toolRegistrationList
		//see if Home was chosen
		for (ListIterator j = chosenList.listIterator(); j.hasNext(); )
		{
			String choice = (String) j.next();
			if(choice.equalsIgnoreCase("home"))
			{ 
				homeInChosenList = true; 
			}
			else if (choice.equals("sakai.mailbox"))
			{
				hasEmail = true;
				String alias = StringUtil.trimToNull((String) state.getAttribute(STATE_TOOL_EMAIL_ADDRESS));
				if (alias != null)
				{
					if (!Validator.checkEmailLocal(alias))
					{
						addAlert(state, INVALID_EMAIL);
					}
					else
					{
						try
						{
							String channelReference = mailArchiveChannelReference(site.getId());
							//first, clear any alias set to this channel				
							AliasService.removeTargetAliases(channelReference);	// check to see whether the alias has been used
							try
							{
								String target = AliasService.getTarget(alias);
								if (target != null)
								{
									addAlert(state, rb.getString("java.emailinuse")+" ");
								}
							}
							catch (IdUnusedException ee)
							{
								try
								{
									AliasService.setAlias(alias, channelReference);
								}
								catch (IdUsedException exception) {}
								catch (IdInvalidException exception) {}
								catch (PermissionException exception) {}
							}	
						}
						catch (PermissionException exception) {}
					}
				}
			}
			else if (choice.equals("sakai.announcements"))
			{
				hasAnnouncement = true; 
			}
			else if (choice.equals("sakai.chat"))
			{
				hasChat = true; 
			}
			else if (choice.equals("sakai.discussion"))
			{
				hasDiscussion = true; 
			}
		}
		
		//see if Home and/or Help in the wSetupPageList (can just check title here, because we checked patterns before adding to the list)
		for (ListIterator i = wSetupPageList.listIterator(); i.hasNext(); )
		{
			wSetupPage = (WorksiteSetupPage) i.next();
			if((wSetupPage.getPageTitle()).equals("Home")){ homeInWSetupPageList = true; }
		}
		
		if (homeInChosenList)
		{
			SitePage page = null;
			if (homeInWSetupPageList)
			{
				if (!SiteService.isUserSite(site.getId()))
				{
					//for non-myworkspace site, if Home is chosen and Home is in the wSetupPageList, remove synoptic tools
					WorksiteSetupPage homePage = new WorksiteSetupPage();
					for (ListIterator i = wSetupPageList.listIterator(); i.hasNext(); )
					{
						WorksiteSetupPage comparePage = (WorksiteSetupPage) i.next();
						if((comparePage.getPageTitle()).equals("Home")) { homePage = comparePage; }
					}
					page = site.getPage(homePage.getPageId());
					List toolList = page.getTools();
					List removeToolList = new Vector();
					// get those synoptic tools
					for (ListIterator iToolList = toolList.listIterator(); iToolList.hasNext(); )
					{
						ToolConfiguration tool = (ToolConfiguration) iToolList.next();
						if (tool.getTool().getId().equals("sakai.synoptic.announcement")
							|| tool.getTool().getId().equals("sakai.synoptic.discussion")
							|| tool.getTool().getId().equals("sakai.synoptic.chat"))
						{
							removeToolList.add(tool);
						}
					}
					// remove those synoptic tools
					for (ListIterator rToolList = removeToolList.listIterator(); rToolList.hasNext(); )
					{
						page.removeTool((ToolConfiguration) rToolList.next());
					}
				}
			}	
			else
			{
				//if Home is chosen and Home is not in wSetupPageList, add Home to site and wSetupPageList
				page = site.addPage();
				
				page.setTitle(rb.getString("java.home"));
	
				wSetupHome.pageId = page.getId();
				wSetupHome.pageTitle = page.getTitle();
				wSetupHome.toolId = "home";
				wSetupPageList.add(wSetupHome);
			
				//Add worksite information tool
				ToolConfiguration tool = page.addTool();
				Tool reg = ToolManager.getTool("sakai.iframe.site");
				tool.setTool("sakai.iframe.site", reg);
				tool.setTitle(rb.getString("java.workinfo"));
				tool.setLayoutHints("0,0");
			}	
				
			if (!SiteService.isUserSite(site.getId()))
			{
				//add synoptical tools to home tool in non-myworkspace site
				try
				{
					if (hasAnnouncement)
					{
						//Add synoptic announcements tool
						ToolConfiguration tool = page.addTool();
						Tool reg = ToolManager.getTool("sakai.synoptic.announcement");
						tool.setTool("sakai.synoptic.announcement", reg);	
						tool.setTitle(rb.getString("java.recann"));
						tool.setLayoutHints("0,1");
					}
					
					if (hasDiscussion)
					{			
						//Add synoptic discussion tool
						ToolConfiguration tool = page.addTool();
						Tool reg = ToolManager.getTool("sakai.synoptic.discussion");
						tool.setTool("sakai.synoptic.discussion", reg);
						tool.setTitle(rb.getString("java.recdisc"));
						tool.setLayoutHints("1,1");
					}
								
					if (hasChat)
					{
						//Add synoptic chat tool
						ToolConfiguration tool = page.addTool();
						Tool reg = ToolManager.getTool("sakai.synoptic.chat");
						tool.setTool("sakai.synoptic.chat", reg);
						tool.setTitle(rb.getString("java.recent"));
						tool.setLayoutHints("2,1");
					}
					if (hasAnnouncement || hasDiscussion || hasChat )
					{
						page.setLayout(SitePage.LAYOUT_DOUBLE_COL);
					}
					else
					{
						page.setLayout(SitePage.LAYOUT_SINGLE_COL);
					}
					
				}
				catch (Exception e)
				{
					M_log.warn("SiteAction.getFeatures Exception " + e.getMessage());
				}
			}
		} // add Home
		
		//if Home is in wSetupPageList and not chosen, remove Home feature from wSetupPageList and site
		if (!homeInChosenList && homeInWSetupPageList)
		{
			//remove Home from wSetupPageList
			WorksiteSetupPage removePage = new WorksiteSetupPage();
			for (ListIterator i = wSetupPageList.listIterator(); i.hasNext(); )
			{
				WorksiteSetupPage comparePage = (WorksiteSetupPage) i.next();
				if((comparePage.getPageTitle()).equals(rb.getString("java.home"))) { removePage = comparePage; }
			}
			SitePage siteHome = site.getPage(removePage.getPageId());
			site.removePage(siteHome);
			wSetupPageList.remove(removePage);
			
		}
		
		//declare flags used in making decisions about whether to add, remove, or do nothing
		boolean inChosenList;
		boolean inWSetupPageList;
		
		Hashtable newsTitles = (Hashtable) state.getAttribute(STATE_NEWS_TITLES);
		Hashtable wcTitles = (Hashtable) state.getAttribute(STATE_WEB_CONTENT_TITLES);
		Hashtable newsUrls = (Hashtable) state.getAttribute(STATE_NEWS_URLS);
		Hashtable wcUrls = (Hashtable) state.getAttribute(STATE_WEB_CONTENT_URLS);
		
		Set categories = new HashSet();
		categories.add((String) state.getAttribute(STATE_SITE_TYPE));
		Set toolRegistrationList = ToolManager.findTools(categories, null);

		// first looking for any tool for removal
		Vector removePageIds = new Vector();
		for (ListIterator k =  wSetupPageList.listIterator(); k.hasNext(); )
		{
			wSetupPage = (WorksiteSetupPage)k.next();
			String pageToolId = wSetupPage.getToolId();
			
			// use page id + tool id for multiple News and Web Content tool
			if (pageToolId.indexOf("sakai.news") != -1 || pageToolId.indexOf("sakai.iframe") != -1)
			{
				pageToolId = wSetupPage.getPageId() + pageToolId;
			}
			
			inChosenList = false;
	
			for (ListIterator j = chosenList.listIterator(); j.hasNext(); )
			{
				String toolId = (String) j.next();
				if(pageToolId.equals(toolId)) 
				{ 
					inChosenList = true;
				}
			}
				
			if (!inChosenList)
			{
				removePageIds.add(wSetupPage.getPageId());
			}
		}
		for  (int i = 0; i < removePageIds.size(); i++)
		{
			//if the tool exists in the wSetupPageList, remove it from the site
			String removeId = (String) removePageIds.get(i);
			SitePage sitePage = site.getPage(removeId);
			site.removePage(sitePage);
			
			// and remove it from wSetupPageList
			for (ListIterator k =  wSetupPageList.listIterator(); k.hasNext(); )
			{
				wSetupPage = (WorksiteSetupPage)k.next();
				if (!wSetupPage.getPageId().equals(removeId))
				{
					wSetupPage = null;
				}
			}
			if (wSetupPage != null)
			{
				wSetupPageList.remove(wSetupPage);
			}
		}
		
		// then looking for any tool to add
		for (ListIterator j = orderToolIds(state, (String) state.getAttribute(STATE_SITE_TYPE), chosenList).listIterator(); j.hasNext(); )
		{
			String toolId = (String) j.next();
			//Is the tool in the wSetupPageList?
			inWSetupPageList = false;
			for (ListIterator k =  wSetupPageList.listIterator(); k.hasNext(); )
			{
				wSetupPage = (WorksiteSetupPage)k.next();
				String pageToolId = wSetupPage.getToolId();
				
				// use page Id + toolId for multiple News and Web Content tool
				if (pageToolId.indexOf("sakai.news") != -1 || pageToolId.indexOf("sakai.iframe") != -1)
				{
					pageToolId = wSetupPage.getPageId() + pageToolId;
				}
				
				if(pageToolId.equals(toolId)) 
				{ 
					inWSetupPageList = true;
					// but for News and Web Content tool, need to change the title
					if (toolId.indexOf("sakai.news") != -1)
					{
						SitePage pEdit = (SitePage) site.getPage(wSetupPage.pageId);
						pEdit.setTitle((String) newsTitles.get(toolId));
						List toolList = pEdit.getTools();
						for (ListIterator jTool = toolList.listIterator(); jTool.hasNext(); )
						{
							ToolConfiguration tool = (ToolConfiguration) jTool.next();
							if (tool.getTool().getId().equals("sakai.news"))
							{
								// set News tool title
								tool.setTitle((String) newsTitles.get(toolId));
						
								// set News tool url
								String urlString = (String) newsUrls.get(toolId);
								try
								{
									URL url = new URL(urlString);
									// update the tool config
									tool.getPlacementConfig().setProperty("channel-url", (String) url.toExternalForm());
								}
								catch (MalformedURLException e)
								{
									addAlert(state, rb.getString("java.invurl")+" " + urlString + ". ");
								}
							}
						}
					}
					else if (toolId.indexOf("sakai.iframe") != -1)
					{
						SitePage pEdit = (SitePage) site.getPage(wSetupPage.pageId);
						pEdit.setTitle((String) wcTitles.get(toolId));
						
						List toolList = pEdit.getTools();
						for (ListIterator jTool = toolList.listIterator(); jTool.hasNext(); )
						{
							ToolConfiguration tool = (ToolConfiguration) jTool.next();
							if (tool.getTool().getId().equals("sakai.iframe"))
							{
								// set Web Content tool title
								tool.setTitle((String) wcTitles.get(toolId));
								// set Web Content tool url
								String wcUrl = StringUtil.trimToNull((String) wcUrls.get(toolId));
								if (wcUrl != null && !wcUrl.equals(WEB_CONTENT_DEFAULT_URL))
								{
									// if url is not empty and not consists only of "http://"
									tool.getPlacementConfig().setProperty("source", wcUrl);
								}
							}
						}
					}
				}		
			}
			if (inWSetupPageList)
			{
				// if the tool already in the list, do nothing so to save the option settings 
			}
			else
			{
				// if in chosen list but not in wSetupPageList, add it to the site (one tool on a page)
				
				// if Site Info tool is being newly added
				if (toolId.equals("sakai.siteinfo"))
				{
					hasNewSiteInfo = true;
				}
				
				Tool toolRegFound = null;
				for (Iterator i = toolRegistrationList.iterator(); i.hasNext(); )
				{
					Tool toolReg = (Tool) i.next();
					if ((toolId.indexOf("assignment") != -1 && toolId.equals(toolReg.getId())) 
						|| (toolId.indexOf("assignment") == -1 && toolId.indexOf(toolReg.getId()) != -1))
					{
						toolRegFound = toolReg;
					}
				}
				
				if (toolRegFound != null)
				{
					// we know such a tool, so add it
					WorksiteSetupPage addPage = new WorksiteSetupPage();
					SitePage page = site.addPage();
					addPage.pageId = page.getId();
					if (toolId.indexOf("sakai.news") != -1)
					{
						// set News tool title
						page.setTitle((String) newsTitles.get(toolId));
					}
					else if (toolId.indexOf("sakai.iframe") != -1)
					{
						// set Web Content tool title
						page.setTitle((String) wcTitles.get(toolId));
					}
					else
					{
						// other tools with default title
						page.setTitle(toolRegFound.getTitle());
					}
					page.setLayout(SitePage.LAYOUT_SINGLE_COL);
					ToolConfiguration tool = page.addTool();
					tool.setTool(toolRegFound.getId(), toolRegFound);
					addPage.toolId = toolId;
					wSetupPageList.add(addPage);
					
					//set tool title
					if (toolId.indexOf("sakai.news") != -1)
					{
						// set News tool title
						tool.setTitle((String) newsTitles.get(toolId));
						
						//set News tool url
						String urlString = (String) newsUrls.get(toolId);
						try 
						{
							URL url = new URL(urlString);
							// update the tool config
							tool.getPlacementConfig().setProperty("channel-url", (String) url.toExternalForm());
						}
						catch(MalformedURLException e)
						{
							// display message
							addAlert(state, "Invalid URL " + urlString + ". ");
							
							// remove the page because of invalid url
							site.removePage(page);
						}
					}
					else if (toolId.indexOf("sakai.iframe") != -1)
					{
						// set Web Content tool title
						tool.setTitle((String) wcTitles.get(toolId));
						// set Web Content tool url
						String wcUrl = StringUtil.trimToNull((String) wcUrls.get(toolId));
						if (wcUrl != null && !wcUrl.equals(WEB_CONTENT_DEFAULT_URL))
						{
							// if url is not empty and not consists only of "http://"
							tool.getPlacementConfig().setProperty("source", wcUrl);
						}
					}
					else
					{
						tool.setTitle(toolRegFound.getTitle());
					}
				}
			}
		}	// for
		
		if (homeInChosenList)
		{
			//Order tools - move Home to the top - first find it
			SitePage homePage = null;
			pageList = site.getPages();
			if (pageList != null && pageList.size() != 0)
			{
				for (ListIterator i = pageList.listIterator(); i.hasNext(); )
				{
					SitePage page = (SitePage)i.next();
					if (rb.getString("java.home").equals(page.getTitle()))//if ("Home".equals(page.getTitle())) 
					{
						homePage = page;
						break;
					}
				}
			}
			
			// if found, move it
			if (homePage != null)
			{
				// move home from it's index to the first position
				int homePosition = pageList.indexOf(homePage);
				for (int n = 0; n < homePosition; n++)
				{
					homePage.moveUp();
				}
			}
		}
		
		// if Site Info is newly added, more it to the last
		if (hasNewSiteInfo)
		{
			SitePage siteInfoPage = null;
			pageList = site.getPages();
			String[] toolIds = {"sakai.siteinfo"};
			if (pageList != null && pageList.size() != 0)
			{
				for (ListIterator i = pageList.listIterator(); siteInfoPage==null && i.hasNext(); )
				{
					SitePage page = (SitePage)i.next();
					int s = page.getTools(toolIds).size();
					if (s > 0) 
					{
						siteInfoPage = page;
					}
				}
			}
			
			// if found, move it
			if (siteInfoPage != null)
			{
				// move home from it's index to the first position
				int siteInfoPosition = pageList.indexOf(siteInfoPage);
				for (int n = siteInfoPosition; n<pageList.size(); n++)
				{
					siteInfoPage.moveDown();
				}
			}
		}
		
		// if there is no email tool chosen
		if (!hasEmail)
		{
			state.removeAttribute(STATE_TOOL_EMAIL_ADDRESS);
		}

		//commit
		commitSite(site);
		
	} // getRevisedFeatures
	
	/**
	* getFeatures gets features for a new site
	*
	*/
	private void getFeatures(ParameterParser params, SessionState state)
	{
		List idsSelected = new Vector();
		
		boolean goToENWPage = false;	
		boolean homeSelected = false;
			
		// Add new pages and tools, if any
		if (params.getStrings ("selectedTools") == null)
		{
			addAlert(state, rb.getString("atleastonetool"));
		}
		else
		{
			List l = new ArrayList(Arrays.asList(params.getStrings ("selectedTools"))); // toolId's & titles of chosen tools
			for (int i = 0; i < l.size(); i++)
			{
				String toolId = (String) l.get(i);
				
				if (toolId.equals("sakai.mailbox") || toolId.indexOf("sakai.news") != -1 || toolId.indexOf("sakai.iframe") != -1)
				{
					goToENWPage = true;
				}
				else if (toolId.equals("home"))
				{
					homeSelected = true;
				}
			    idsSelected.add(toolId);
			}
			state.setAttribute(STATE_TOOL_HOME_SELECTED, new Boolean(homeSelected));
		}
		
		String importString = params.getString("import");
		if (importString!= null && importString.equalsIgnoreCase(Boolean.TRUE.toString()))
		{
			state.setAttribute(STATE_IMPORT, Boolean.TRUE);
		
			List importSites = new Vector();
			if (params.getStrings("importSites") != null)
			{
				importSites = new ArrayList(Arrays.asList(params.getStrings("importSites")));
			}
			if (importSites.size() == 0)
			{
				addAlert(state, rb.getString("java.toimport")+" ");
			}
			else
			{
				Hashtable sites = new Hashtable();
				for (int index = 0; index < importSites.size(); index ++)
				{
					try
					{
						Site s = SiteService.getSite((String) importSites.get(index));
						if	(!sites.containsKey(s))
						{
							sites.put(s, new Vector());
						}
					}
					catch (IdUnusedException e)
					{	
					}
				}
				state.setAttribute(STATE_IMPORT_SITES, sites);
			}
		}
		else
		{
			state.removeAttribute(STATE_IMPORT);
		}
		
		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, idsSelected); // List of ToolRegistration toolId's
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			if (state.getAttribute(STATE_IMPORT) != null)
			{
				// go to import tool page
				state.setAttribute(STATE_TEMPLATE_INDEX, "27");
			}
			else if (goToENWPage)
			{
				// go to the configuration page for Email Archive, News and Web Content tools
				state.setAttribute(STATE_TEMPLATE_INDEX, "26");
			} 
			else
			{
				// go to edit access page
				state.setAttribute(STATE_TEMPLATE_INDEX, "18");
			}
			
			int totalSteps = 4;
			if (state.getAttribute(STATE_SITE_TYPE) != null && ((String) state.getAttribute(STATE_SITE_TYPE)).equalsIgnoreCase("course"))
			{
				totalSteps = 5;
				if (state.getAttribute(STATE_ADD_CLASS_PROVIDER_CHOSEN) != null && state.getAttribute(STATE_MANUAL_ADD_COURSE_NUMBER) != null)
				{
					totalSteps++;
				}
			}
			if (state.getAttribute(STATE_IMPORT) != null)
			{
				totalSteps++;
			}
			if (goToENWPage)
			{
				totalSteps++;
			}
			state.setAttribute(SITE_CREATE_TOTAL_STEPS, new Integer(totalSteps));
		}
	}	// getFeatures
	
	/**
	* addFeatures adds features to a new site
	*
	*/
	private void addFeatures(SessionState state)
	{
		List toolRegistrationList = (Vector)state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		Site site = getStateSite(state);

		List pageList = new Vector();
		int moves = 0;
		boolean hasHome = false;
		boolean hasAnnouncement = false;
		boolean hasChat = false;
		boolean hasDiscussion = false;
		boolean hasSiteInfo = false;
			
		List chosenList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		
		// tools to be imported from other sites?
		Hashtable importTools = null;
		if (state.getAttribute(STATE_IMPORT_SITE_TOOL) != null)
		{
			importTools = (Hashtable) state.getAttribute(STATE_IMPORT_SITE_TOOL);
		}
		
		//for tools other than home
		if (chosenList.contains("home"))
		{
			// add home tool later 
			hasHome = true;
		}
		
		// order the id list
		chosenList = orderToolIds(state, site.getType(), chosenList);
		
		// titles for news tools
		Hashtable newsTitles = (Hashtable) state.getAttribute(STATE_NEWS_TITLES);
		// urls for news tools
		Hashtable newsUrls = (Hashtable) state.getAttribute(STATE_NEWS_URLS);
		// titles for web content tools
		Hashtable wcTitles = (Hashtable) state.getAttribute(STATE_WEB_CONTENT_TITLES);
		// urls for web content tools
		Hashtable wcUrls = (Hashtable) state.getAttribute(STATE_WEB_CONTENT_URLS);
		
		if (chosenList.size() > 0)
		{	
			Tool toolRegFound = null;
			for (ListIterator i = chosenList.listIterator(); i.hasNext(); )
			{
				String toolId = (String) i.next();
				
				// find the tool in the tool registration list
				toolRegFound = null;
				for (int j = 0; j < toolRegistrationList.size() && toolRegFound == null; j++)
				{
					MyTool tool = (MyTool) toolRegistrationList.get(j);
					if ((toolId.indexOf("assignment") != -1 && toolId.equals(tool.getId())) 
						|| (toolId.indexOf("assignment") == -1 && toolId.indexOf(tool.getId()) != -1))
					{
						toolRegFound = ToolManager.getTool(tool.getId());
					}
				}
				
				if (toolRegFound != null)
				{
					if (toolId.indexOf("sakai.news") != -1)
					{
						// adding multiple news tool
						String newsTitle = (String) newsTitles.get(toolId);
						SitePage page = site.addPage();
						page.setTitle(newsTitle); // the visible label on the tool menu
						page.setLayout(SitePage.LAYOUT_SINGLE_COL);
						ToolConfiguration tool = page.addTool();
						tool.setTool("sakai.news", ToolManager.getTool("sakai.news"));
						tool.setTitle(newsTitle);
						tool.setLayoutHints("0,0");
					  	String urlString = (String) newsUrls.get(toolId);
					  	//update the tool config
						tool.getPlacementConfig().setProperty("channel-url", urlString);
					}
					else if (toolId.indexOf("sakai.iframe") != -1)
					{
						// adding multiple web content tool
						String wcTitle = (String) wcTitles.get(toolId);
						SitePage page = site.addPage();
						page.setTitle(wcTitle); // the visible label on the tool menu
						page.setLayout(SitePage.LAYOUT_SINGLE_COL);
						ToolConfiguration tool = page.addTool();
						tool.setTool("sakai.iframe", ToolManager.getTool("sakai.iframe"));
						tool.setTitle(wcTitle);
						tool.setLayoutHints("0,0");
						String wcUrl = StringUtil.trimToNull((String) wcUrls.get(toolId));
						if (wcUrl != null && !wcUrl.equals(WEB_CONTENT_DEFAULT_URL))
						{
							// if url is not empty and not consists only of "http://"
							tool.getPlacementConfig().setProperty("source", wcUrl);
						}
					}
					else
					{
						SitePage page = site.addPage();
						page.setTitle(toolRegFound.getTitle()); // the visible label on the tool menu
						page.setLayout(SitePage.LAYOUT_SINGLE_COL);
						ToolConfiguration tool = page.addTool();
						tool.setTool(toolRegFound.getId(), toolRegFound);
						tool.setLayoutHints("0,0");
						
					} // Other features
				}
				
				// booleans for synoptic views
				if (toolId.equals("sakai.announcements"))
				{
					hasAnnouncement = true; 
				}
				else if (toolId.equals("sakai.chat"))
				{
					hasChat = true; 
				}
				else if (toolId.equals("sakai.discussion"))
				{
					hasDiscussion = true; 
				}
				else if (toolId.equals("sakai.siteinfo"))
				{
					hasSiteInfo = true;
				}
				 
			}	// for
			
			//import
			importToolIntoSite(chosenList, importTools, site);
			
			// add home tool
			if (hasHome)
			{
				// Home is a special case, with several tools on the page. "home" is hard coded in chef_site-addRemoveFeatures.vm.
				try
				{
					SitePage page = site.addPage();
					page.setTitle(rb.getString("java.home")); // the visible label on the tool menu
					if (hasAnnouncement || hasDiscussion || hasChat)
					{
						page.setLayout(SitePage.LAYOUT_DOUBLE_COL);
					}
					else
					{
						page.setLayout(SitePage.LAYOUT_SINGLE_COL);
					}
					
					//Add worksite information tool
					ToolConfiguration tool = page.addTool();
					tool.setTool("sakai.iframe.site", ToolManager.getTool("sakai.iframe.site"));
					tool.setTitle(rb.getString("java.workinfo"));
					tool.setLayoutHints("0,0");

					if (hasAnnouncement)
					{
						//Add synoptic announcements tool
						tool = page.addTool();
						tool.setTool("sakai.synoptic.announcement", ToolManager.getTool("sakai.synoptic.announcement"));
						tool.setTitle(rb.getString("java.recann"));
						tool.setLayoutHints("0,1");
					}
								
					if (hasDiscussion)
					{ 
						//Add synoptic announcements tool
						tool = page.addTool();
						tool.setTool("sakai.synoptic.discussion", ToolManager.getTool("sakai.synoptic.discussion"));	
						tool.setTitle("Recent Discussion Items");
						tool.setLayoutHints("1,1");
					}
						
					if (hasChat)
					{		
						//Add synoptic chat tool
						tool = page.addTool();
						tool.setTool("sakai.synoptic.chat", ToolManager.getTool("sakai.synoptic.chat"));
						tool.setTitle("Recent Chat Messages");
						tool.setLayoutHints("2,1");
					}
 
				}
				catch (Exception e)
				{
					M_log.warn("SiteAction.getFeatures Exception " + e.getMessage());
				}
				
				state.setAttribute(STATE_TOOL_HOME_SELECTED, Boolean.TRUE);
				
				//Order tools - move Home to the top 
				pageList = site.getPages();
				if(pageList != null && pageList.size() != 0)
				{
					for (ListIterator i = pageList.listIterator(); i.hasNext(); )
					{
						SitePage page = (SitePage)i.next();
						if((page.getTitle()).equals(rb.getString("java.home"))) 
						{
							moves = pageList.indexOf(page);
							for (int n = 0; n < moves; n++)
							{
								page.moveUp();
							}
						}
					}
				}
			} // Home feature 
			
			// move Site Info tool, if selected, to the end of tool list
			if (hasSiteInfo)
			{
				SitePage siteInfoPage = null;
				pageList = site.getPages();
				String[] toolIds = {"sakai.siteinfo"};
				if (pageList != null && pageList.size() != 0)
				{
					for (ListIterator i = pageList.listIterator(); siteInfoPage==null && i.hasNext(); )
					{
						SitePage page = (SitePage)i.next();
						int s = page.getTools(toolIds).size();
						if (s > 0) 
						{
							siteInfoPage = page;
						}
					}
				}
				
				//if found, move it
				if (siteInfoPage != null)
				{
					// move home from it's index to the first position
					int siteInfoPosition = pageList.indexOf(siteInfoPage);
					for (int n = siteInfoPosition; n<pageList.size(); n++)
					{
						siteInfoPage.moveDown();
					}
				}
			}	// Site Info
		}

		// commit
		commitSite(site);
		
	} // addFeatures
	
	// import tool content into site
	private void importToolIntoSite(List toolIds, Hashtable importTools, Site site)
	{
		if (importTools != null)
		{
			// import resources first
			boolean resourcesImported = false;
			for (int i = 0; i < toolIds.size() && !resourcesImported; i++)
			{
				String toolId = (String) toolIds.get(i);

				Object contentHosting = (Object) ContentHostingService.getInstance();
				if (toolId.equalsIgnoreCase("sakai.resources") && importTools.containsKey(toolId)
						&& contentHosting instanceof EntityTransferrer)
				{
					EntityTransferrer et = (EntityTransferrer) contentHosting;

					List importSiteIds = (List) importTools.get(toolId);

					for (int k = 0; k < importSiteIds.size(); k++)
					{
						String fromSiteId = (String) importSiteIds.get(k);
						String toSiteId = site.getId();

						String fromSiteCollectionId = ContentHostingService.getSiteCollection(fromSiteId);
						String toSiteCollectionId = ContentHostingService.getSiteCollection(toSiteId);
						et.transferCopyEntities(fromSiteCollectionId, toSiteCollectionId, new Vector());
						resourcesImported = true;
					}
				}
			}

			// ijmport other tools then
			for (int i = 0; i < toolIds.size(); i++)
			{
				String toolId = (String) toolIds.get(i);
				if (!toolId.equalsIgnoreCase("sakai.resources") && importTools.containsKey(toolId))
				{
					List importSiteIds = (List) importTools.get(toolId);
					for (int k = 0; k < importSiteIds.size(); k++)
					{
						String fromSiteId = (String) importSiteIds.get(k);
						String toSiteId = site.getId();
						transferCopyEntities(toolId, fromSiteId, toSiteId);
					}
				}
			}
		}
	} // importToolIntoSite
	
	public void saveSiteStatus(SessionState state, boolean published)
	{
		Site site = getStateSite(state);
		site.setPublished(published);
		
	} // saveSiteStatus

	public void commitSite(Site site, boolean published)
	{
		site.setPublished(published);
		
		try
		{
			SiteService.save(site);
		}
		catch (IdUnusedException e)
		{
			// TODO:
		}
		catch (PermissionException e)
		{
			// TODO:
		}

	} // commitSite
	
	public void commitSite(Site site)
	{
		try
		{
			SiteService.save(site);
		}
		catch (IdUnusedException e)
		{
			// TODO:
		}
		catch (PermissionException e)
		{
			// TODO:
		}

	}// commitSite
	
	private void checkAddParticipant(ParameterParser params, SessionState state)
	{
		// get the participants to be added
		int i;
		Vector pList = new Vector();
		
		String invalidEmailInIdAccountString = ServerConfigurationService.getString("invalidEmailInIdAccountString", null);
		
		//accept noEmailInIdAccounts and/or emailInIdAccount account names
		String noEmailInIdAccounts = "";
		String emailInIdAccounts = "";
		
		//check that there is something with which to work
		noEmailInIdAccounts = StringUtil.trimToNull((params.getString("noEmailInIdAccount")));
		emailInIdAccounts = StringUtil.trimToNull(params.getString("emailInIdAccount"));
		state.setAttribute("noEmailInIdAccountValue", noEmailInIdAccounts);
		state.setAttribute("emailInIdAccountValue", emailInIdAccounts);
		
		//if there is no uniquname or emailInIdAccount entered
		if (noEmailInIdAccounts == null && emailInIdAccounts == null)
		{
			addAlert(state, rb.getString("java.guest"));
			state.setAttribute(STATE_TEMPLATE_INDEX, "5");
			return;
		}
		
		String at = "@";
		
		if (noEmailInIdAccounts != null)
		{
			// adding noEmailInIdAccounts
			String[] noEmailInIdAccountArray = noEmailInIdAccounts.replaceAll(",","\r\n").split("\r\n");
			
			for (i = 0; i < noEmailInIdAccountArray.length; i++)
			{
				String noEmailInIdAccount = StringUtil.trimToNull(noEmailInIdAccountArray[i]);
				//if there is some text, try to use it
				noEmailInIdAccount.replaceAll("[ \t\r\n]","");
				if(noEmailInIdAccount != null)
				{
					//automaticially add emailInIdAccount account
					Participant participant = new Participant();
					try
					{
						User u = UserDirectoryService.getUserByEid(noEmailInIdAccount);
						participant.name = u.getDisplayName();
						participant.uniqname = noEmailInIdAccount;
						pList.add(participant);
					}
					catch (UserNotDefinedException e) 
					{
						addAlert(state, noEmailInIdAccount + " "+rb.getString("java.username")+" ");
					}
				}
			}
		}	// noEmailInIdAccounts
		
		if (emailInIdAccounts != null)
		{
			String[] emailInIdAccountArray = emailInIdAccounts.split("\r\n");
			for (i = 0; i < emailInIdAccountArray.length; i++)
			{
				String emailInIdAccount = emailInIdAccountArray[i];
				
				//if there is some text, try to use it
				emailInIdAccount.replaceAll("[ \t\r\n]","");
				
				//remove the trailing dots and empty space
				while (emailInIdAccount.endsWith(".") || emailInIdAccount.endsWith(" "))
				{
					emailInIdAccount = emailInIdAccount.substring(0, emailInIdAccount.length() -1);
				}
				
				if(emailInIdAccount != null && emailInIdAccount.length() > 0)
				{
					String[] parts = emailInIdAccount.split(at);
			
					if(emailInIdAccount.indexOf(at) == -1 )
					{
						// must be a valid email address	
						addAlert(state,  emailInIdAccount + " "+rb.getString("java.emailaddress"));
					}
					else if((parts.length != 2) || (parts[0].length() == 0))
					{
						// must have both id and address part
						addAlert(state, emailInIdAccount + " "+rb.getString("java.notemailid"));
					}
					else if (!Validator.checkEmailLocal(parts[0]))
					{
						addAlert(state, emailInIdAccount + " "+rb.getString("java.emailaddress") + INVALID_EMAIL);
					}
					else if (invalidEmailInIdAccountString != null && emailInIdAccount.indexOf(invalidEmailInIdAccountString) != -1)
					{
						// wrong string inside emailInIdAccount id
						addAlert(state, emailInIdAccount + " "+rb.getString("java.emailaddress")+" ");
					}
					else
					{
						Participant participant = new Participant();
						try
						{
							// if the emailInIdAccount user already exists
							User u = UserDirectoryService.getUserByEid(emailInIdAccount);
							participant.name = u.getDisplayName();
							participant.uniqname = emailInIdAccount;
							pList.add(participant);
						}
						catch (UserNotDefinedException e)
						{
							// if the emailInIdAccount user is not in the system yet
							participant.name = emailInIdAccount;
							participant.uniqname = emailInIdAccount; // TODO: what would the UDS case this name to? -ggolden
							pList.add(participant);
						}
					}
				}	// if
			}	// 	
		} // emailInIdAccounts
		
		boolean same_role = true;
		if (params.getString("same_role") == null)
		{
			addAlert(state, rb.getString("java.roletype")+" ");
		}
		else
		{
			same_role = params.getString("same_role").equals("true")?true:false;
			state.setAttribute("form_same_role", new Boolean(same_role));
		}
		
		if (state.getAttribute(STATE_MESSAGE) != null)
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "5");
		}
		else
		{
			if (same_role)
			{
				state.setAttribute(STATE_TEMPLATE_INDEX, "19");
			}
			else
			{
				state.setAttribute(STATE_TEMPLATE_INDEX, "20");
			}
		}
		
		// remove duplicate or existing user from participant list
		pList=removeDuplicateParticipants(pList, state);
		pList=removeExistingParticipants(pList, state);
		state.setAttribute(STATE_ADD_PARTICIPANTS, pList);
		
		// if the add participant list is empty after above removal, stay in the current page
		if (pList.size() == 0)
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "5");
			addAlert(state, rb.getString("java.guest"));
		}
		
		return;
	
	} // checkAddParticipant
	
	private Vector removeDuplicateParticipants(List pList, SessionState state)
	{
		// check the uniqness of list member
		Set s = new HashSet();
		Set uniqnameSet = new HashSet();
		Vector rv = new Vector();
		for (int i = 0; i < pList.size(); i++)
		{
			Participant p = (Participant) pList.get(i);
			if (!uniqnameSet.contains(p.getUniqname()))
			{
				// no entry for the account yet
				rv.add(p);
				uniqnameSet.add(p.getUniqname());
			}
			else
			{
				// found duplicates
				s.add(p.getUniqname());
			}
		}
		
		if (!s.isEmpty())
		{
			int count = 0;
			String accounts = "";
			for (Iterator i = s.iterator();i.hasNext();)
			{
				if (count == 0)
				{
					accounts = (String) i.next();
				}
				else
				{
					accounts = accounts + ", " + (String) i.next();
				}
				count++;
			}
			if (count == 1)
			{
				addAlert(state, rb.getString("add.duplicatedpart.single") + accounts + ".");
			}
			else
			{
				addAlert(state, rb.getString("add.duplicatedpart") + accounts + ".");
			}
		}
		
		return rv;
	}

	private Vector removeExistingParticipants(List pList, SessionState state)
	{
		String siteId=ToolManager.getCurrentPlacement().getContext();
		Vector rv = new Vector();
		Set s = new HashSet();
		
		try
		{
			
			Site site=SiteService.getSite(siteId);
		
			for (int i = 0; i < pList.size(); i++)
			{
				Participant p = (Participant) pList.get(i);
				if (site.getMember(p.getUniqname()) == null)
				{
					// no entry for the account yet
					rv.add(p);
				}
				else
				{
					// found duplicates
					s.add(p.getUniqname());
				}
			}
	    }
	    catch (Exception ignore)
	    {
	    		// ignore exceptions
	    }

		if (!s.isEmpty())
		{
			int count = 0;
			String accounts = "";
			for (Iterator i = s.iterator();i.hasNext();)
			{
				if (count == 0)
				{
					accounts = (String) i.next();
				}
				else
				{
					accounts = accounts + ", " + (String) i.next();
				}
				count++;
			}
			if (count == 1)
			{
				addAlert(state, rb.getString("add.existingpart.single") + accounts + ".");
			}
			else
			{
				addAlert(state, rb.getString("add.existingpart") + accounts + ".");
			}
		}
		return rv;
	}

	public void doAdd_participant(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		String siteTitle = getStateSite(state).getTitle();				
		Hashtable selectedRoles = (Hashtable) state.getAttribute(STATE_SELECTED_PARTICIPANT_ROLES);
		int i;
		
		//accept noEmailInIdAccounts and/or emailInIdAccount account names
		String emailInIdAccountName = ServerConfigurationService.getString("emailInIdAccountName", "");
		String noEmailInIdAccounts = null;
		String emailInIdAccounts = null;
		
		if (state.getAttribute("noEmailInIdAccountValue") != null)
		{
			noEmailInIdAccounts = (String) state.getAttribute("noEmailInIdAccountValue");
		}
		if (state.getAttribute("emailInIdAccountValue") != null)
		{
			emailInIdAccounts = (String) state.getAttribute("emailInIdAccountValue");
		}
		
		boolean notify = false;
		if (state.getAttribute("form_selectedNotify") != null)
		{
			notify = ((Boolean) state.getAttribute("form_selectedNotify")).booleanValue();
		}
		
		boolean same_role = ((Boolean) state.getAttribute("form_same_role")).booleanValue();
	
		String pw = null;
		String notAddedNames = null;
		String notAddedEmailInIdAccounts = null;

		Vector addedNames = new Vector();
		if (noEmailInIdAccounts != null)
		{
			// adding noEmailInIdAccounts
			String[] noEmailInIdAccountArray = noEmailInIdAccounts.replaceAll(",","\r\n").split("\r\n");
			for (i = 0; i < noEmailInIdAccountArray.length; i++)
			{
				String noEmailInIdAccount = StringUtil.trimToNull(noEmailInIdAccountArray[i].replaceAll("[ \t\r\n]",""));
				if(noEmailInIdAccount != null)
				{
					// get role
					String role = null;
					if (same_role)
					{
						// if all added participants have a same role
						role = (String) state.getAttribute("form_selectedRole");
					}
					else
					{
						// if all added participants have different role
						role = (String) selectedRoles.get(noEmailInIdAccount);
					}
					
					if (addUserRealm(state, noEmailInIdAccount, role))
					{
						// successfully added
						addedNames.add(noEmailInIdAccount);
						
						// send notification
						if (notify)
						{
							String emailId = null;
							String userName = null;
							try
							{
								User u = UserDirectoryService.getUserByEid(noEmailInIdAccount);
								emailId = u.getEmail();
								userName = u.getDisplayName();
							}
							catch (UserNotDefinedException e)
							{
								M_log.warn("cannot find user " + noEmailInIdAccount + ". ");
							}
							// send notification email
							notifyAddedParticipant(false, emailId, userName, siteTitle);
						}
					}
					else
					{
						notAddedNames=notAddedNames.concat(noEmailInIdAccount);
					}
				}
			}
		}	// noEmailInIdAccounts					

		Vector addedEmailInIdAccounts = new Vector();
		if (emailInIdAccounts != null)
		{
			String[] emailInIdAccountArray = emailInIdAccounts.split("\r\n");
	
			for (i = 0; i < emailInIdAccountArray.length; i++)
			{
				String emailInIdAccount = StringUtil.trimToNull(emailInIdAccountArray[i].replaceAll("[ \t\r\n]",""));
				
				// remove the trailing dots and empty space
				while (emailInIdAccount.endsWith(".") || emailInIdAccount.endsWith(" "))
				{
					emailInIdAccount = emailInIdAccount.substring(0, emailInIdAccount.length() -1);
				}

				if(emailInIdAccount != null)
				{	
					try
					{
						UserDirectoryService.getUserByEid(emailInIdAccount);
					}
					catch (UserNotDefinedException e) 
					{
						//if there is no such user yet, add the user
						try
						{
							UserEdit uEdit = UserDirectoryService.addUser(null, emailInIdAccount);

							//set email address
							uEdit.setEmail(emailInIdAccount);
							
							// set the guest user type
							uEdit.setType("guest");

							// set password to a positive random number
							Random generator = new Random(System.currentTimeMillis());
							Integer num = new Integer(generator.nextInt(Integer.MAX_VALUE));
							if (num.intValue() < 0) num = new Integer(num.intValue() *-1);
							pw = num.toString();
							uEdit.setPassword(pw);
							
							// and save
							UserDirectoryService.commitEdit(uEdit);
							
							boolean notifyNewUserEmail = (ServerConfigurationService.getString("notifyNewUserEmail", Boolean.TRUE.toString())).equalsIgnoreCase(Boolean.TRUE.toString());
							if (notifyNewUserEmail)
							{
								notifyNewUserEmail(uEdit.getId(), uEdit.getEmail(), pw, siteTitle);
							}
						 }
						 catch(UserIdInvalidException ee)
						 {
							 addAlert(state, emailInIdAccountName + " id " + emailInIdAccount + " "+rb.getString("java.isinval") );
							 M_log.warn("doAdd_participant: UserDirectoryService addUser exception " + e.getMessage());
						 }
						 catch(UserAlreadyDefinedException ee)
						 {
							 addAlert(state, "The " + emailInIdAccountName + " " + emailInIdAccount + " " + rb.getString("java.beenused"));
							 M_log.warn("doAdd_participant: UserDirectoryService addUser exception " + e.getMessage());
						 }
						 catch(UserPermissionException ee)
						 {
							 addAlert(state, rb.getString("java.haveadd")+ " " + emailInIdAccount);
							 M_log.warn("doAdd_participant: UserDirectoryService addUser exception " + e.getMessage());
						 }	
					}
					
					// add role if user exists
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						// get role 
						String role = null;
						if (same_role)
						{
							// if all added participants have a same role
							role = (String) state.getAttribute("form_selectedRole"); 
						}
						else
						{
							// if all added participants have different role
							role = (String) selectedRoles.get(emailInIdAccount);
						}
							
						// add property role to the emailInIdAccount account
						if (addUserRealm(state, emailInIdAccount, role))
						{
							// emailInIdAccount account has been added successfully
							addedEmailInIdAccounts.add(emailInIdAccount);
							
							// send notification
							if (notify)
							{	
								// send notification email
								notifyAddedParticipant(true, emailInIdAccount, emailInIdAccount, siteTitle);
							}
						}
						else
						{
							notAddedEmailInIdAccounts = notAddedEmailInIdAccounts.concat(emailInIdAccount + "\n");
						}
					}
					else
					{
						notAddedEmailInIdAccounts = notAddedEmailInIdAccounts.concat(emailInIdAccount + "\n");
					}
				}	// if
			}	// 	
		} // emailInIdAccounts

		if (!(addedNames.size() == 0 && addedEmailInIdAccounts.size() == 0) && (notAddedNames != null || notAddedEmailInIdAccounts != null))
		{
			// at lease one noEmailInIdAccount account or a emailInIdAccount account added
			addAlert(state, rb.getString("java.allusers"));
		}

		if (notAddedNames == null && notAddedEmailInIdAccounts == null)
		{
			// all account has been added successfully
			removeAddParticipantContext(state);
		}
		else
		{
			state.setAttribute("noEmailInIdAccountValue", notAddedNames);
			state.setAttribute("emailInIdAccountValue", notAddedEmailInIdAccounts);
		}
		if (state.getAttribute(STATE_MESSAGE) != null)
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "22");
		}
		else
		{
			state.setAttribute(STATE_TEMPLATE_INDEX, "12");
		}
		return;
				
	}	// doAdd_participant
	
	/**
	 * remove related state variable for adding participants
	 * @param state SessionState object
	 */
	private void removeAddParticipantContext(SessionState state)
	{
		// remove related state variables 
		state.removeAttribute("form_selectedRole");
		state.removeAttribute("noEmailInIdAccountValue");
		state.removeAttribute("emailInIdAccountValue");
		state.removeAttribute("form_same_role");
		state.removeAttribute("form_selectedNotify");
		state.removeAttribute(STATE_ADD_PARTICIPANTS);
		state.removeAttribute(STATE_SELECTED_USER_LIST);
		state.removeAttribute(STATE_SELECTED_PARTICIPANT_ROLES);
		
	}	// removeAddParticipantContext
	
	/**
	 * Send an email to newly added user informing password
	 * @param newEmailInIdAccount
	 * @param emailId
	 * @param userName
	 * @param siteTitle
	 */
	private void notifyNewUserEmail(String userName, String newUserEmail, String newUserPassword, String siteTitle)
	{
		String from = ServerConfigurationService.getString("setup.request", null);
		if (from == null)
		{
			M_log.warn(this + " - no 'setup.request' in configuration");
			from = "postmaster@".concat(ServerConfigurationService.getServerName());
		}
		String productionSiteName = ServerConfigurationService.getString("ui.service", "");
		String productionSiteUrl = ServerConfigurationService.getPortalUrl();
		
		String to = newUserEmail;
		String headerTo = newUserEmail;
		String replyTo = newUserEmail;
		String message_subject = productionSiteName + " "+rb.getString("java.newusernoti");
		String content = "";
		
		if (from != null && newUserEmail !=null)
		{
			StringBuffer buf = new StringBuffer();
			buf.setLength(0);
			
			// email body
			buf.append(userName + ":\n\n");
			
			buf.append(rb.getString("java.addedto")+" " + productionSiteName + " ("+ productionSiteUrl + ") ");
			buf.append(rb.getString("java.simpleby")+" ");
         	buf.append(UserDirectoryService.getCurrentUser().getDisplayName() + ". \n\n");
			buf.append(rb.getString("java.passwordis1")+"\n" + newUserPassword + "\n\n");
			buf.append(rb.getString("java.passwordis2")+ "\n\n");
			
			content = buf.toString();
			EmailService.send(from, to, message_subject, content, headerTo, replyTo, null);
		}
	}	// notifyNewUserEmail
	
	/**
	 * send email notification to added participant
	 */
	private void notifyAddedParticipant(boolean newEmailInIdAccount, String emailId, String userName, String siteTitle)
	{
		String from = ServerConfigurationService.getString("setup.request", null);
		if (from == null)
		{
			M_log.warn(this + " - no 'setup.request' in configuration");
		}
		else
		{
			String productionSiteName = ServerConfigurationService.getString("ui.service", "");
			String productionSiteUrl = ServerConfigurationService.getPortalUrl();
			String emailInIdAccountUrl = ServerConfigurationService.getString("emailInIdAccount.url", null);
			String to = emailId;
			String headerTo = emailId;
			String replyTo = emailId;
			String message_subject = productionSiteName + " "+rb.getString("java.sitenoti");
			String content = "";
			StringBuffer buf = new StringBuffer();
			buf.setLength(0);
			
			// email body differs between newly added emailInIdAccount account and other users
			buf.append(userName + ":\n\n");
			buf.append(rb.getString("java.following")+" " + productionSiteName + " "+rb.getString("java.simplesite")+ "\n");
			buf.append(siteTitle + "\n");
         	buf.append(rb.getString("java.simpleby")+" ");
         	buf.append(UserDirectoryService.getCurrentUser().getDisplayName() + ". \n\n");
			if (newEmailInIdAccount)
			{
				buf.append(ServerConfigurationService.getString("emailInIdAccountInstru", "") + "\n");
				
				if (emailInIdAccountUrl != null)
				{
					buf.append(rb.getString("java.togeta1") +"\n" + emailInIdAccountUrl + "\n");
					buf.append(rb.getString("java.togeta2")+"\n\n");
				}
				buf.append(rb.getString("java.once")+" " + productionSiteName + ": \n");
				buf.append(rb.getString("java.loginhow1")+" " + productionSiteName + ": " + productionSiteUrl + "\n");
				buf.append(rb.getString("java.loginhow2")+"\n");
				buf.append(rb.getString("java.loginhow3")+"\n");
			}
			else
			{
				buf.append(rb.getString("java.tolog")+"\n");
				buf.append(rb.getString("java.loginhow1")+" " + productionSiteName + ": " + productionSiteUrl + "\n");
				buf.append(rb.getString("java.loginhow2")+"\n");
				buf.append(rb.getString("java.loginhow3u")+"\n");
			}
			buf.append(rb.getString("java.tabscreen"));
			content = buf.toString();
			EmailService.send(from, to, message_subject, content, headerTo, replyTo, null);
			
		}	// if

	}	// notifyAddedParticipant
	
	/*
	* If the user account does not exist yet inside the user directory, assign role to it
	*/
	private boolean addUserRealm (SessionState state, String id, String role)
	{
		StringBuffer message = new StringBuffer();
		try
		{
			User user = UserDirectoryService.getUserByEid(id);
			Site sEdit = getStateSite(state);
			String realmId = SiteService.siteReference(sEdit.getId());
			if (AuthzGroupService.allowUpdate(realmId) || SiteService.allowUpdateSiteMembership(sEdit.getId()))
			{
				try
				{
					AuthzGroup realmEdit = AuthzGroupService.getAuthzGroup(realmId);
					realmEdit.addMember(user.getId(), role, true, false);
					AuthzGroupService.save(realmEdit);
				}
				catch (GroupNotDefinedException e)
				{
					message.append(id + " " +rb.getString("java.notvalidid")+" \n");
				}
//				catch( PermissionException e)
//				{
//					message.append(rb.getString("java.haveadd")+" " + sEdit.getTitle() + "(" + id + ") "+rb.getString("java.tothissite")+" \n");
//				}
				catch (Exception e)
				{
					message.append(rb.getString("java.unable")+ " "+ sEdit.getTitle() + "(" + id + ") "+rb.getString("java.tothissite")+" \n" + e.toString());
				}
			}
		}
		catch (UserNotDefinedException ee)
		{
			message.append(id + " " +rb.getString("java.account")+" \n");
		}	// try
	
		if (message.length() == 0)
		{
			return true;
		}
		else
		{
			addAlert(state, message.toString());
			return false;
		}	// if
	
	}	// addUserRealm
	
	/**
	* addNewSite is called when the site has enough information to create a new site
	* 
	*/
	private void addNewSite(ParameterParser params, SessionState state)
	{
		if(getStateSite(state) != null)
		{
			// There is a Site in state already, so use it rather than creating a new Site
			return;
		}
					
		//If cleanState() has removed SiteInfo, get a new instance into state
		SiteInfo siteInfo = new SiteInfo();
		if (state.getAttribute(STATE_SITE_INFO) != null)
		{
			siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);	
		}
		String id = StringUtil.trimToNull(siteInfo.getSiteId());
		if (id == null)
		{
			//get id
			id = IdManager.createUuid();
			siteInfo.site_id = id;
		}
		state.setAttribute(STATE_SITE_INFO, siteInfo);
		if (state.getAttribute(STATE_MESSAGE) == null)
		{		
			try
			{
				siteInfo = (SiteInfo) state.getAttribute(STATE_SITE_INFO);

				Site site = SiteService.addSite(id, siteInfo.site_type);				
				
				String title = StringUtil.trimToNull(siteInfo.title);
				String description = siteInfo.description;
				setAppearance(state, site, siteInfo.iconUrl);
				site.setDescription(description);
				if (title != null)
				{
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
				rp.addProperty(PROP_SITE_CONTACT_NAME, siteInfo.site_contact_name);
				rp.addProperty(PROP_SITE_CONTACT_EMAIL, siteInfo.site_contact_email);
				
				state.setAttribute(STATE_SITE_INSTANCE_ID, site.getId());	
				
				//commit newly added site in order to enable related realm
				commitSite(site);
				
			}
			catch (IdUsedException e)
			{
				addAlert(state, rb.getString("java.sitewithid")+" " + id + " "+rb.getString("java.exists"));
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("template-index"));
				return;
			}
			catch (IdInvalidException e)
			{
				addAlert(state,  rb.getString("java.thesiteid")+" " + id + " "+rb.getString("java.notvalid"));
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("template-index"));
				return;
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("java.permission")+" " + id +".");
				state.setAttribute(STATE_TEMPLATE_INDEX, params.getString("template-index"));
				return;
			}
		}
	} // addNewSite
	
	/**
	* Use the AuthzGroup Provider Id to build a Site tab
	 * @throws IdUnusedException 
	*
	*/
	private String getCourseTab(SessionState state, String id)
	{
		StringBuffer tab = new StringBuffer();
		
		try
		{
			String courseName = CourseManagementService.getCourseName(id);
			if (courseName != null && courseName.length() > 0)
			{
				tab.append(courseName);
				return appendTermInSiteTitle(state, tab.toString());
			}
		}
		catch (IdUnusedException ignore)
		{
			
		}
		
		return "";
		
	}//  getCourseTab
	
	private String appendTermInSiteTitle (SessionState state, String title)
	{
		//append term information into the tab in order to differenciate same course taught in different terms
		if (state.getAttribute(STATE_TERM_SELECTED) != null)
		{
			Term t = (Term) state.getAttribute(STATE_TERM_SELECTED);
			if (StringUtil.trimToNull(t.getListAbbreviation()) != null)
			{
				// use term abbreviation, if any
				title = title.concat(" ").concat(t.getListAbbreviation());
			}
			else
			{
				// use term id
				title = title.concat(" ").concat(t.getId());
			}
		}
		return title;
		
	}	// appendTermInSiteTitle
	
	/**
	* %%% legacy properties, to be cleaned up
	* 
	*/
	private void sitePropertiesIntoState (SessionState state)
	{
		try
		{
			Site site = getStateSite(state);
			SiteInfo siteInfo = new SiteInfo();
			
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
			state.setAttribute(STATE_SITE_TYPE, siteInfo.site_type);
			
			state.setAttribute(STATE_SITE_INFO, siteInfo);
		}
		catch (Exception e)
		{
			M_log.warn("SiteAction.sitePropertiesIntoState " + e.getMessage());
		}
		
	} // sitePropertiesIntoState 
	
	/**
	* pageMatchesPattern returns true if a SitePage matches a WorkSite Setup pattern
	* 
	*/
	private boolean  pageMatchesPattern (SessionState state, SitePage page)
	{
		List pageToolList =  page.getTools();
		
		// if no tools on the page, return false
		if(pageToolList == null || pageToolList.size() == 0) { return false; }

		//for the case where the page has one tool
		ToolConfiguration toolConfiguration = (ToolConfiguration)pageToolList.get(0);
		
		//don't compare tool properties, which may be changed using Options
		List toolList = new Vector();
		int count = pageToolList.size();
		boolean match = false;

		//check Worksite Setup Home pattern
		if(page.getTitle()!=null && page.getTitle().equals(rb.getString("java.home")))
		{
			return true;
			
		} // Home
		else if(page.getTitle() != null && page.getTitle().equals(rb.getString("java.help")))
		{
			//if the count of tools on the page doesn't match, return false
			if(count != 1) { return false;}
			
			//if the page layout doesn't match, return false
			if(page.getLayout() != SitePage.LAYOUT_SINGLE_COL) { return false; }

			//if tooId isn't sakai.contactSupport, return false
			if(!(toolConfiguration.getTool().getId()).equals("sakai.contactSupport")) { return false; }

			return true;
		} // Help
		else if(page.getTitle() != null && page.getTitle().equals("Chat"))
		{
			//if the count of tools on the page doesn't match, return false
			if(count != 1) { return false;}
			
			//if the page layout doesn't match, return false
			if(page.getLayout() != SitePage.LAYOUT_SINGLE_COL) { return false; }
			
			//if the tool doesn't match, return false
			if(!(toolConfiguration.getTool().getId()).equals("sakai.chat")) { return false; }
			
			//if the channel doesn't match value for main channel, return false
			String channel = toolConfiguration.getPlacementConfig().getProperty("channel");
			if(channel == null) { return false; }
			if(!(channel.equals(NULL_STRING))) { return false; }
			
			return true;
		} // Chat
		else
		{
			//if the count of tools on the page doesn't match, return false
			if(count != 1) { return false;}
			
			//if the page layout doesn't match, return false
			if(page.getLayout() != SitePage.LAYOUT_SINGLE_COL) { return false; }
			
			toolList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
			
			if(pageToolList != null || pageToolList.size() != 0)
			{
				//if tool attributes don't match, return false
				match = false;
				for (ListIterator i = toolList.listIterator(); i.hasNext(); )
				{
					MyTool tool = (MyTool) i.next();
					if(toolConfiguration.getTitle() != null)
					{
						if((toolConfiguration.getTool().getId()).indexOf(tool.getId()) != -1) { match = true; }
					}
				}
				if (!match) { return false; }
			}
		} // Others
		return true;
		
	} // pageMatchesPattern
	
	/**
	* siteToolsIntoState is the replacement for siteToolsIntoState_
	* Make a list of pages and tools that match WorkSite Setup configurations into state
	*/
	private void  siteToolsIntoState (SessionState state)
	{
		String wSetupTool = NULL_STRING;
		List wSetupPageList = new Vector();
		Site site = getStateSite(state);
		List pageList = site.getPages();
		
		//Put up tool lists filtered by category
		String type = site.getType();
		if (type == null)
		{
			if (SiteService.isUserSite(site.getId()))
			{
				type = "myworkspace";
			}
			else if (state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null)
			{
				// for those sites without type, use the tool set for default site type
				type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);
			}
		}
		
		List toolRegList = new Vector();
		if (type != null)
		{
			Set categories = new HashSet();
			categories.add(type);
			Set toolRegistrations = ToolManager.findTools(categories, null);
			SortedIterator i = new SortedIterator(toolRegistrations.iterator(), new ToolComparator());
			for (;i.hasNext();)
			{
				// form a new Tool
				Tool tr = (Tool) i.next();
				MyTool newTool = new MyTool();
				newTool.title = tr.getTitle();
				newTool.id = tr.getId();
				newTool.description = tr.getDescription();
				
				toolRegList.add(newTool);
			}
		}
		
		if (toolRegList.size() == 0 && state.getAttribute(STATE_DEFAULT_SITE_TYPE) != null)
		{
			// use default site type and try getting tools again
			type = (String) state.getAttribute(STATE_DEFAULT_SITE_TYPE);
			
			Set nCategories = new HashSet();
			nCategories.add(type);
			Set toolRegistrations = ToolManager.findTools(nCategories, null);
			SortedIterator i = new SortedIterator(toolRegistrations.iterator(), new ToolComparator());
			for (;i.hasNext();)
			{
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
		
		if (type == null)
		{
			M_log.warn(this + ": - unknown STATE_SITE_TYPE");
		}
		else
		{
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
		
		if (!((pageList == null) || (pageList.size() == 0)))
		{
			for (ListIterator i = pageList.listIterator(); i.hasNext(); )
			{
				SitePage page = (SitePage) i.next();
				//collect the pages consistent with Worksite Setup patterns
				if(pageMatchesPattern(state, page))
				{
					if(page.getTitle().equals(rb.getString("java.home")))
					{
						wSetupTool = "home";
						check_home = true;
					}
					else
					{
						List pageToolList = page.getTools();
						wSetupTool = ((ToolConfiguration)pageToolList.get(0)).getTool().getId();
						if (wSetupTool.indexOf("sakai.news") != -1) 
						{
							String newsToolId = page.getId() + wSetupTool;
							idSelected.add(newsToolId);
							newsTitles.put(newsToolId, page.getTitle());
							String channelUrl = ((ToolConfiguration)pageToolList.get(0)).getPlacementConfig().getProperty("channel-url");
							newsUrls.put(newsToolId, channelUrl!=null?channelUrl:"");
							newsToolNum++;
							
							// insert the News tool into the list
							hasNews = false;
							int j = 0;
							MyTool newTool = new MyTool();
							newTool.title = NEWS_DEFAULT_TITLE;
							newTool.id = newsToolId;
							newTool.selected = false;
							
							for (;j< toolRegList.size() && !hasNews; j++)
							{
								MyTool t = (MyTool) toolRegList.get(j);
								if (t.getId().equals("sakai.news"))
								{
									hasNews = true;
									newTool.description = t.getDescription();
								}
							}
					
							if (hasNews)
							{
								toolRegList.add(j-1, newTool);
							}
							else
							{
								toolRegList.add(newTool);
							}
							 
						}
						else if ((wSetupTool).indexOf("sakai.iframe") != -1) 
						{
							String wcToolId = page.getId() + wSetupTool;
							idSelected.add(wcToolId);
							wcTitles.put(wcToolId, page.getTitle());
							String wcUrl = StringUtil.trimToNull(((ToolConfiguration)pageToolList.get(0)).getPlacementConfig().getProperty("source"));
							if (wcUrl == null)
							{
								// if there is no source URL, seed it with the Web Content default URL
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
							for (;j< toolRegList.size() && !hasWebContent; j++)
							{
								MyTool t = (MyTool) toolRegList.get(j); 
								if (t.getId().equals("sakai.iframe"))
								{
									hasWebContent = true;
									newTool.description = t.getDescription();
								}
							}
							if (hasWebContent)
							{
								toolRegList.add(j-1, newTool);
							}
							else
							{
								toolRegList.add(newTool);
							}
						}
						/*else if(wSetupTool.indexOf("sakai.syllabus") != -1) 
						{
						  //add only one instance of tool per site
						  	if (!(idSelected.contains(wSetupTool)))
						  	{
						  	  idSelected.add(wSetupTool);
						  	}
						}*/
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
		
		newsTitles.put("sakai.news", NEWS_DEFAULT_TITLE);
		newsUrls.put("sakai.news", NEWS_DEFAULT_URL);
		wcTitles.put("sakai.iframe", WEB_CONTENT_DEFAULT_TITLE);
		wcUrls.put("sakai.iframe", WEB_CONTENT_DEFAULT_URL);
		
		state.setAttribute(STATE_TOOL_REGISTRATION_LIST, toolRegList);
		state.setAttribute(STATE_TOOL_HOME_SELECTED, new Boolean(check_home));
		state.setAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST, idSelected); // List of ToolRegistration toolId's
		state.setAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST, idSelected); // List of ToolRegistration toolId's
		state.setAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME, Boolean.valueOf(check_home));
		state.setAttribute(STATE_NEWS_TITLES, newsTitles);
		state.setAttribute(STATE_WEB_CONTENT_TITLES, wcTitles);
		state.setAttribute(STATE_NEWS_URLS, newsUrls);
		state.setAttribute(STATE_WEB_CONTENT_URLS, wcUrls);
		state.setAttribute(STATE_WORKSITE_SETUP_PAGE_LIST, wSetupPageList);
		
	} //siteToolsIntoState
	
	/**
	 * reset the state variables used in edit tools mode
	 * @state The SessionState object
	 */
	private void removeEditToolState(SessionState state)
	{
		state.removeAttribute(STATE_TOOL_HOME_SELECTED);
		state.removeAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST); // List of ToolRegistration toolId's
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_LIST); // List of ToolRegistration toolId's
		state.removeAttribute(STATE_TOOL_REGISTRATION_OLD_SELECTED_HOME);
		state.removeAttribute(STATE_NEWS_TITLES);
		state.removeAttribute(STATE_NEWS_URLS);
		state.removeAttribute(STATE_WEB_CONTENT_TITLES);
		state.removeAttribute(STATE_WEB_CONTENT_URLS);
		state.removeAttribute(STATE_WORKSITE_SETUP_PAGE_LIST);
	}
	
	private List orderToolIds(SessionState state, String type, List toolIdList)
	{
		List rv = new Vector();
		if (state.getAttribute(STATE_TOOL_HOME_SELECTED) != null
			&& ((Boolean) state.getAttribute(STATE_TOOL_HOME_SELECTED)).booleanValue())
		{
			rv.add("home");
		}

		// look for null site type
		if (type != null && toolIdList != null)
		{
			Set categories = new HashSet();
			categories.add(type);
			Set tools = ToolManager.findTools(categories, null);
			SortedIterator i = new SortedIterator(tools.iterator(), new ToolComparator());
			for (; i.hasNext(); )
			{
				String tool_id = ((Tool) i.next()).getId();
				for (ListIterator j = toolIdList.listIterator(); j.hasNext(); )
				{
					String toolId = (String) j.next();
					if(toolId.indexOf("assignment") != -1 && toolId.equals(tool_id)
						|| toolId.indexOf("assignment") == -1 && toolId.indexOf(tool_id) != -1)
					{
						rv.add(toolId);
					}
				}
			}
		}
		return rv;
		
	} // orderToolIds
	
	private void setupFormNamesAndConstants(SessionState state)
	{
		TimeBreakdown timeBreakdown = (TimeService.newTime ()).breakdownLocal ();
		String mycopyright = COPYRIGHT_SYMBOL + " " + timeBreakdown.getYear () + ", " + UserDirectoryService.getCurrentUser().getDisplayName () + ". All Rights Reserved. ";
		state.setAttribute (STATE_MY_COPYRIGHT, mycopyright);
		state.setAttribute (STATE_SITE_INSTANCE_ID, null);
		state.setAttribute (STATE_INITIALIZED, Boolean.TRUE.toString());
		SiteInfo siteInfo = new SiteInfo();
		Participant participant = new Participant();
		participant.name = NULL_STRING;
		participant.uniqname = NULL_STRING;
		state.setAttribute(STATE_SITE_INFO, siteInfo);
		state.setAttribute("form_participantToAdd", participant);
		state.setAttribute(FORM_ADDITIONAL, NULL_STRING);
		//legacy
		state.setAttribute(FORM_HONORIFIC,"0");
		state.setAttribute(FORM_REUSE, "0"); 
		state.setAttribute(FORM_RELATED_CLASS, "0");
		state.setAttribute(FORM_RELATED_PROJECT, "0");
		state.setAttribute(FORM_INSTITUTION, "0");
		//sundry form variables
		state.setAttribute(FORM_PHONE,"");
		state.setAttribute(FORM_EMAIL,"");
		state.setAttribute(FORM_SUBJECT,"");
		state.setAttribute(FORM_DESCRIPTION,"");
		state.setAttribute(FORM_TITLE,"");
		state.setAttribute(FORM_NAME,"");
		state.setAttribute(FORM_SHORT_DESCRIPTION,"");
		
	}	// setupFormNamesAndConstants
	
	/**
	* Add these Unit affliates to sites in these
	* Subject areas with Instructor role
	*
	*/
	private void setupSubjectAffiliates(SessionState state)
	{
		Vector affiliates = new Vector();
		
		List subjectList = new Vector();
		List campusList = new Vector();
		List uniqnameList = new Vector();
		
		//get term information
		if (ServerConfigurationService.getStrings("affiliatesubjects") != null)
		{
			subjectList = new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("affiliatesubjects")));
		}
		if (ServerConfigurationService.getStrings("affiliatecampus") != null)
		{
			campusList = new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("affiliatecampus")));
		}
		if (ServerConfigurationService.getStrings("affiliateuniqnames") != null)
		{
			uniqnameList = new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("affiliateuniqnames")));
		}
		
		if (subjectList.size() > 0 && subjectList.size() == campusList.size() && subjectList.size() == uniqnameList.size())
		{
			for (int i=0; i < subjectList.size();i++)
			{
				String[] subjectFields = ((String) subjectList.get(i)).split(",");
				String[] uniqnameFields = ((String) uniqnameList.get(i)).split(",");
				String campus = (String) campusList.get(i);
				
				for (int j=0; j < subjectFields.length; j++)
				{
					String subject = StringUtil.trimToZero(subjectFields[j]);
					
					SubjectAffiliates affiliate = new SubjectAffiliates();
					affiliate.setSubject(subject);
					affiliate.setCampus(campus);
					
					for (int k=0; k < uniqnameFields.length;k++)
					{
						affiliate.getUniqnames().add(StringUtil.trimToZero(uniqnameFields[k]));
					}
					affiliates.add(affiliate);
				}
			}
		}
		
		state.setAttribute(STATE_SUBJECT_AFFILIATES, affiliates);
		
	}	// setupSubjectAffiliates
	
	/**
	*  setupSkins
	* 
	*/
	private void setupIcons(SessionState state)
	{
		List icons = new Vector();
		
		String[] iconNames = null;
		String[] iconUrls = null;
		String[] iconSkins = null;
		
		//get icon information
		if (ServerConfigurationService.getStrings("iconNames") != null)
		{
			iconNames = ServerConfigurationService.getStrings("iconNames");
		}
		if (ServerConfigurationService.getStrings("iconUrls") != null)
		{
			iconUrls = ServerConfigurationService.getStrings("iconUrls");
		}
		if (ServerConfigurationService.getStrings("iconSkins") != null)
		{
			iconSkins = ServerConfigurationService.getStrings("iconSkins");
		}
		
		if ((iconNames != null) && (iconUrls != null) && (iconSkins != null) && (iconNames.length == iconUrls.length) && (iconNames.length == iconSkins.length))
		{
			for (int i = 0; i < iconNames.length; i++)
			{
				Icon s = new Icon(
						StringUtil.trimToNull((String) iconNames[i]),
						StringUtil.trimToNull((String) iconUrls[i]),
						StringUtil.trimToNull((String) iconSkins[i]));
				icons.add(s);
			}
		}
		
		state.setAttribute(STATE_ICONS, icons);
	}
	
	private void setAppearance(SessionState state, Site edit, String iconUrl)
	{
		// set the icon 
		edit.setIconUrl(iconUrl);
		if (iconUrl == null)
		{
			// this is the default case - no icon, no (default) skin
			edit.setSkin(null);
			return;
		}
		
		// if this icon is in the config appearance list, find a skin to set
		List icons = (List) state.getAttribute(STATE_ICONS);
		for (Iterator i = icons.iterator(); i.hasNext();)
		{
			Icon icon = (Icon) i.next();
			if (!StringUtil.different(icon.getUrl(), iconUrl))
			{
				edit.setSkin(icon.getSkin());
				return;
			}
		}
	}

	/**
	 * A dispatch funtion when selecting course features
	 */
	public void doAdd_features ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		String option = params.getString("option");
		
		if (option.equalsIgnoreCase("addNews"))
		{
			updateSelectedToolList(state, params, false);
			insertTool(state, "sakai.news", STATE_NEWS_TITLES, NEWS_DEFAULT_TITLE, STATE_NEWS_URLS, NEWS_DEFAULT_URL, Integer.parseInt(params.getString("newsNum")));
			state.setAttribute(STATE_TEMPLATE_INDEX, "26");
		}
		else if (option.equalsIgnoreCase("addWC"))
		{
			updateSelectedToolList(state, params, false);
			insertTool(state, "sakai.iframe", STATE_WEB_CONTENT_TITLES, WEB_CONTENT_DEFAULT_TITLE, STATE_WEB_CONTENT_URLS, WEB_CONTENT_DEFAULT_URL, Integer.parseInt(params.getString("wcNum")));
			state.setAttribute(STATE_TEMPLATE_INDEX, "26");
		}
		else if (option.equalsIgnoreCase("import"))
		{
			// import or not
			updateSelectedToolList(state, params, false);
			String importSites = params.getString("import");
			if (importSites != null && importSites.equalsIgnoreCase(Boolean.TRUE.toString()))
			{
				state.setAttribute(STATE_IMPORT, Boolean.TRUE);
				if (importSites.equalsIgnoreCase(Boolean.TRUE.toString()))
				{
					state.removeAttribute(STATE_IMPORT);
					state.removeAttribute(STATE_IMPORT_SITES);
					state.removeAttribute(STATE_IMPORT_SITE_TOOL);
				}
			}
			else
			{
				state.removeAttribute(STATE_IMPORT);
			}
		}
		else if (option.equalsIgnoreCase("continue"))
		{
			// continue
			doContinue(data);
		}
		else if (option.equalsIgnoreCase("back"))
		{
			// back
			doBack(data);
		}
		else if (option.equalsIgnoreCase("cancel"))
		{
			// cancel
			doCancel_create(data);
		}
		
	}	// doAdd_features

	/**
	 * update the selected tool list
	 * @param params The ParameterParser object
	 * @param verifyData Need to verify input data or not
	 */
	private void updateSelectedToolList (SessionState state, ParameterParser params, boolean verifyData)
	{
		List selectedTools = new ArrayList(Arrays.asList(params.getStrings ("selectedTools")));
		
		Hashtable titles = (Hashtable) state.getAttribute(STATE_NEWS_TITLES);
		Hashtable urls = (Hashtable) state.getAttribute(STATE_NEWS_URLS);
		Hashtable wcTitles = (Hashtable) state.getAttribute(STATE_WEB_CONTENT_TITLES);
		Hashtable wcUrls = (Hashtable) state.getAttribute(STATE_WEB_CONTENT_URLS);
		boolean has_home = false;
		String emailId = null;
		
		for (int i = 0; i < selectedTools.size(); i++)
		{
			String id = (String) selectedTools.get(i);
			if (id.indexOf("sakai.news") != -1)
			{
				String title = StringUtil.trimToNull(params.getString("titlefor" + id));
				if (title == null)
				{
					// if there is no input, make the title for news tool default to NEWS_DEFAULT_TITLE
					title = NEWS_DEFAULT_TITLE;
				}
				titles.put(id, title);
				
				String url = StringUtil.trimToNull(params.getString("urlfor" + id));
				if (url == null)
				{
					// if there is no input, make the title for news tool default to NEWS_DEFAULT_URL
					url = NEWS_DEFAULT_URL;
				}
				urls.put(id, url);
				
				try
				{
					URL u = new URL(url);
				}
				catch (MalformedURLException e)
				{
					addAlert(state, rb.getString("java.invurl")+" " + url + ". ");
				}
			}
			else if (id.indexOf("sakai.iframe") != -1)
			{
				String wcTitle = StringUtil.trimToNull(params.getString("titlefor" + id));
				if (wcTitle == null)
				{
					// if there is no input, make the title for Web Content tool default to WEB_CONTENT_DEFAULT_TITLE
					wcTitle = WEB_CONTENT_DEFAULT_TITLE;
				}
				wcTitles.put(id, wcTitle);
				
				String wcUrl = StringUtil.trimToNull(params.getString("urlfor" + id));
				if (wcUrl == null)
				{
					// if there is no input, make the title for Web Content tool default to WEB_CONTENT_DEFAULT_URL
					wcUrl = WEB_CONTENT_DEFAULT_URL;
				}
				else
				{
					if ((wcUrl.length() > 0) && (!wcUrl.startsWith("/")) && (wcUrl.indexOf("://") == -1))
					{
						wcUrl = "http://" + wcUrl;
					}
				}
				wcUrls.put(id, wcUrl);
			}
			else if (id.equalsIgnoreCase("home"))
			{
				has_home = true;
			}
			else if (id.equalsIgnoreCase("sakai.mailbox"))
			{
				// if Email archive tool is selected, check the email alias
				emailId = StringUtil.trimToNull(params.getString("emailId"));
				if(verifyData)
				{
					if (emailId == null)
					{
						addAlert(state, rb.getString("java.emailarchive")+" ");
					}
					else 
					{
						if (!Validator.checkEmailLocal(emailId))
						{
							addAlert(state, INVALID_EMAIL);
						}
						else
						{
							//check to see whether the alias has been used by other sites
							try
							{
								String target = AliasService.getTarget(emailId);
								if (target != null)
								{
									if (state.getAttribute(STATE_SITE_INSTANCE_ID) != null)
									{
										String siteId = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);
										String channelReference = mailArchiveChannelReference(siteId);
										if (!target.equals(channelReference))
										{
											// the email alias is not used by current site
											addAlert(state, rb.getString("java.emailinuse")+" ");
										}
									}
									else
									{
										addAlert(state, rb.getString("java.emailinuse")+" ");
									}
								} 
							}
							catch (IdUnusedException ee){}
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
	}	 // updateSelectedToolList
	
	/**
	 * find the tool in the tool list and insert another tool instance to the list
	 * @param state SessionState object
	 * @param toolId The id for the inserted tool
	 * @param stateTitlesVariable The titles
	 * @param defaultTitle The default title for the inserted tool
	 * @param stateUrlsVariable The urls
	 * @param defaultUrl The default url for the inserted tool
	 * @param insertTimes How many tools need to be inserted
	 */
	private void insertTool(SessionState state, String toolId, String stateTitlesVariable, String defaultTitle, String stateUrlsVariable, String defaultUrl, int insertTimes)
	{
		//the list of available tools
		List toolList = (List) state.getAttribute(STATE_TOOL_REGISTRATION_LIST);
		int toolListedTimes = 0;
		int index = 0;
		int insertIndex = 0;
		while ( index < toolList.size())
		{
			MyTool tListed = (MyTool) toolList.get(index);
			if (tListed.getId().indexOf(toolId) != -1 )
			{
				toolListedTimes++;
			}
			
			if (toolListedTimes > 0 && insertIndex == 0)
			{
				// update the insert index
				insertIndex = index;
			}
			
			index ++;
		}
		
		List toolSelected = (List) state.getAttribute(STATE_TOOL_REGISTRATION_SELECTED_LIST);
		
		// the titles
		Hashtable titles = (Hashtable) state.getAttribute(stateTitlesVariable);
		if (titles == null)
		{
			titles = new Hashtable();
		}
		
		// the urls
		Hashtable urls = (Hashtable) state.getAttribute(stateUrlsVariable);
		if (urls == null)
		{
			urls = new Hashtable();
		}
		
		// insert multiple tools
		for (int i = 0; i < insertTimes; i++)
		{
			toolListedTimes = toolListedTimes + i;
			
			toolSelected.add(toolId + toolListedTimes);
		
			// We need to insert a specific tool entry only if all the specific tool entries have been selected 
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
		
	}	//	insertTool
	
	/**
	 * 
	 * set selected participant role Hashtable
	 */
	private void setSelectedParticipantRoles(SessionState state)
	{
		List selectedUserIds = (List) state.getAttribute(STATE_SELECTED_USER_LIST);
		List participantList = (List) state.getAttribute(STATE_PARTICIPANT_LIST);
		List selectedParticipantList = new Vector();
		
		Hashtable selectedParticipantRoles = new Hashtable();
		
		if(!selectedUserIds.isEmpty() && participantList != null)
		{
			for (int i = 0; i < participantList.size(); i++)
			{
				String id= "";
				Object o = (Object) participantList.get(i);
				if (o.getClass().equals(Participant.class))
				{
					// get participant roles
					id = ((Participant) o).getUniqname();
					selectedParticipantRoles.put(id, ((Participant) o).getRole());
				}
				else if (o.getClass().equals(Student.class))
				{
					// get participant from roster role
					id = ((Student) o).getUniqname();
					selectedParticipantRoles.put(id, ((Student)o).getRole());
				}
				if (selectedUserIds.contains(id))
				{
					selectedParticipantList.add(participantList.get(i));
				}
			}
		}
		state.setAttribute(STATE_SELECTED_PARTICIPANT_ROLES, selectedParticipantRoles);
		state.setAttribute(STATE_SELECTED_PARTICIPANTS, selectedParticipantList);
		
	}	//  setSelectedParticipantRoles
	
	/**
	 * the SiteComparator class
	 */
	private class SiteComparator
		implements Comparator
	{
		/**
		 * the criteria
		 */
		String m_criterion = null;
		String m_asc = null;
		
		/**
		 * constructor
		 * @param criteria The sort criteria string
		 * @param asc The sort order string. TRUE_STRING if ascending; "false" otherwise.
		 */
		public SiteComparator (String criterion, String asc)
		{
			m_criterion = criterion;
			m_asc = asc;
			
		}	// constructor
		
		/**
		* implementing the Comparator compare function
		* @param o1 The first object
		* @param o2 The second object
		* @return The compare result. 1 is o1 < o2; -1 otherwise
		*/
		public int compare ( Object o1, Object o2)
		{
			int result = -1;
			
			if(m_criterion==null) m_criterion = SORTED_BY_TITLE;
			
			/************* for sorting site list *******************/
			if (m_criterion.equals (SORTED_BY_TITLE))
			{
				// sorted by the worksite title
				String s1 = ((Site) o1).getTitle();
				String s2 = ((Site) o2).getTitle();
				result =  s1.compareToIgnoreCase (s2);
			}
			else if (m_criterion.equals (SORTED_BY_DESCRIPTION))
			{
				
				// sorted by the site short description
				String s1 = ((Site) o1).getShortDescription();
				String s2 = ((Site) o2).getShortDescription();
				if (s1==null && s2==null)
				{
					result = 0;
				}
				else if (s2==null)
				{
					result = 1;
				}
				else if (s1==null)
				{
					result = -1;
				}
				else
				{
					result = s1.compareToIgnoreCase (s2);
				}
			}
			else if (m_criterion.equals (SORTED_BY_TYPE))
			{
				// sorted by the site type
				String s1 = ((Site) o1).getType();
				String s2 = ((Site) o2).getType();
				if (s1==null && s2==null)
				{
					result = 0;
				}
				else if (s2==null)
				{
					result = 1;
				}
				else if (s1==null)
				{
					result = -1;
				}
				else
				{
					result = s1.compareToIgnoreCase (s2);
				}
			}
			else if (m_criterion.equals (SORTED_BY_OWNER))
			{
				// sorted by the site creator
				String s1 = ((Site) o1).getProperties().getProperty("CHEF:creator");
				String s2 = ((Site) o2).getProperties().getProperty("CHEF:creator");
				if (s1==null && s2==null)
				{
					result = 0;
				}
				else if (s2==null)
				{
					result = 1;
				}
				else if (s1==null)
				{
					result = -1;
				}
				else
				{
					result = s1.compareToIgnoreCase (s2);
				}
			}
			else if (m_criterion.equals (SORTED_BY_STATUS))
			{
				// sort by the status, published or unpublished
				int i1 = ((Site)o1).isPublished() ? 1 : 0;
				int i2 = ((Site)o2).isPublished() ? 1 : 0;
				if (i1 > i2)
				{
					result = 1;
				}
				else
				{
					result = -1;
				}
			}
			else if (m_criterion.equals (SORTED_BY_JOINABLE))
			{
				// sort by whether the site is joinable or not
				boolean b1 = ((Site)o1).isJoinable();
				boolean b2 = ((Site)o2).isJoinable();
				if (b1 == b2)
				{
					result = 0;
				}
				else if (b1 == true)
				{
					result = 1;
				}
				else
				{
					result = -1;
				}
			}
			else if (m_criterion.equals (SORTED_BY_PARTICIPANT_NAME))
			{
				// sort by whether the site is joinable or not
				String s1 = null;
				if (o1.getClass().equals(Participant.class))
				{
					s1 = ((Participant) o1).getName();	
				}
				else if (o1.getClass().equals(CourseMember.class))
				{
					s1 = ((CourseMember) o1).getName();	
				}
				
				String s2 = null;
				if (o2.getClass().equals(Participant.class))
				{
					s2 = ((Participant) o2).getName();	
				}
				else if (o2.getClass().equals(CourseMember.class))
				{
					s2 = ((CourseMember) o2).getName();	
				}
				
				if (s1==null && s2==null)
				{
					result = 0;
				}
				else if (s2==null)
				{
					result = 1;
				}
				else if (s1==null)
				{
					result = -1;
				}
				else
				{
					result = s1.compareToIgnoreCase (s2);
				}
			}
			else if (m_criterion.equals (SORTED_BY_PARTICIPANT_UNIQNAME))
			{
				// sort by whether the site is joinable or not
				String s1 = null;
				if (o1.getClass().equals(Participant.class))
				{
					s1 = ((Participant) o1).getUniqname();	
				}
				else if (o1.getClass().equals(CourseMember.class))
				{
					s1 = ((CourseMember) o1).getUniqname();	
				}
				
				String s2 = null;
				if (o2.getClass().equals(Participant.class))
				{
					s2 = ((Participant) o2).getUniqname();	
				}
				else if (o2.getClass().equals(CourseMember.class))
				{
					s2 = ((CourseMember) o2).getUniqname();	
				}				

				if (s1==null && s2==null)
				{
					result = 0;
				}
				else if (s2==null)
				{
					result = 1;
				}
				else if (s1==null)
				{
					result = -1;
				}
				else
				{
					result = s1.compareToIgnoreCase (s2);
				}
			}
			else if (m_criterion.equals (SORTED_BY_PARTICIPANT_ROLE))
			{
				String s1 = "";
				if (o1.getClass().equals(Participant.class))
				{
					s1 = ((Participant) o1).getRole();
				}
				else if (o1.getClass().equals(CourseMember.class))
				{
					s1 = ((CourseMember) o1).getRole();	
				}
				
				String s2 = "";
				if (o2.getClass().equals(Participant.class))
				{
					s2 = ((Participant) o2).getRole();
				}
				else if (o2.getClass().equals(CourseMember.class))
				{
					s2 = ((CourseMember) o2).getRole();	
				}		
				
				if (s1.length() == 0 && s2.length() == 0)
				{
					result = 0;
				}
				else if (s2.length() == 0)
				{
					result = 1;
				}
				else if (s1.length() == 0)
				{
					result = -1;
				}
				else
				{
					result = s1.compareToIgnoreCase (s2);
				}
			}
			else if (m_criterion.equals (SORTED_BY_PARTICIPANT_COURSE))
			{
				// sort by whether the site is joinable or not
				String s1 = null;
				if (o1.getClass().equals(Participant.class))
				{	
				}
				else if (o1.getClass().equals(CourseMember.class))
				{
					s1 = ((CourseMember) o1).getCourse() + " " + ((CourseMember) o1).getSection();	
				}
				
				String s2 = null;
				if (o2.getClass().equals(Participant.class))
				{
				}
				else if (o2.getClass().equals(CourseMember.class))
				{
					s2 = ((CourseMember) o2).getCourse() + " " + ((CourseMember) o2).getSection();	
				}
				if (s1==null && s2==null)
				{
					result = 0;
				}
				else if (s2==null)
				{
					result = 1;
				}
				else if (s1==null)
				{
					result = -1;
				}
				else
				{
					result = s1.compareToIgnoreCase (s2);
				}
			}
			else if (m_criterion.equals (SORTED_BY_PARTICIPANT_ID))
			{
				int i1 = -1;
				if (o1.getClass().equals(Participant.class))
				{
					
				}
				else if (o1.getClass().equals(CourseMember.class))
				{
					try
					{
						i1 = Integer.parseInt(((CourseMember) o1).getId());	
					}
					catch (Exception e) {}
				}
				
				int i2 = -1;
				if (o2.getClass().equals(Participant.class))
				{
				}
				else if (o2.getClass().equals(CourseMember.class))
				{
					try
					{
						i2 = Integer.parseInt(((CourseMember) o2).getId());	
					}
					catch (Exception e) {}
				}
				if (i1 > i2)
				{
					result = 1;
				}
				else
				{
					result = -1;
				}
			}
			else if (m_criterion.equals (SORTED_BY_PARTICIPANT_CREDITS))
			{
				int i1 = -1;
				if (o1.getClass().equals(Participant.class))
				{
					
				}
				else if (o1.getClass().equals(CourseMember.class))
				{
					try
					{
						i1 = Integer.parseInt(((CourseMember) o1).getCredits());	
					}
					catch (Exception e) {}
				}
				
				int i2 = -1;
				if (o2.getClass().equals(Participant.class))
				{
				}
				else if (o2.getClass().equals(CourseMember.class))
				{
					try
					{
						i2 = Integer.parseInt(((CourseMember) o2).getCredits());	
					}
					catch (Exception e) {}
				}
				if (i1 > i2)
				{
					result = 1;
				}
				else
				{
					result = -1;
				}
			}
			else if (m_criterion.equals (SORTED_BY_CREATION_DATE))
			{
				// sort by the site's creation date
				Time t1 = null;  
				Time t2 = null;
	
				// get the times
				try
				{
					t1 = ((Site)o1).getProperties().getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
				}
				catch (EntityPropertyNotDefinedException e)
				{
				}
				catch (EntityPropertyTypeException e)
				{
				}
	
				try
				{
					t2 = ((Site)o2).getProperties().getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
				}
				catch (EntityPropertyNotDefinedException e)
				{
				}
				catch (EntityPropertyTypeException e)
				{
				}
				if (t1==null)
				{
					result = -1;
				}
				else if (t2==null)
				{
					result = 1;
				}
				else if (t1.before (t2))
				{
					result = -1;
				}
				else
				{
					result = 1;
				}
			}
			else if (m_criterion.equals (rb.getString("group.title")))
			{
				// sorted by the group title
				String s1 = ((Group) o1).getTitle();
				String s2 = ((Group) o2).getTitle();
				result =  s1.compareToIgnoreCase (s2);
			}
			else if (m_criterion.equals (rb.getString("group.number")))
			{
				// sorted by the group title
				int n1 = ((Group) o1).getMembers().size();
				int n2 = ((Group) o2).getMembers().size();
				result = (n1 > n2)?1:-1;
			}
			else if (m_criterion.equals (SORTED_BY_MEMBER_NAME))
			{
				// sorted by the member name
				String s1 = "";
				String s2 = "";
				
				try
				{
					s1 = UserDirectoryService.getUser(((Member) o1).getUserId()).getSortName();
				}
				catch(Exception ignore)
				{
					
				}
				
				try
				{
					s2 = UserDirectoryService.getUser(((Member) o2).getUserId()).getSortName();
				}
				catch (Exception ignore)
				{
					
				}
				result =  s1.compareToIgnoreCase (s2);
			}
			
			if(m_asc == null) m_asc = Boolean.TRUE.toString ();
			
			// sort ascending or descending
			if (m_asc.equals (Boolean.FALSE.toString ()))
			{
				result = -result;
			}
			
			return result;
			
		}	// compare
		
	} //SiteComparator
	
	private class ToolComparator
	implements Comparator
	{	
		/**
		* implementing the Comparator compare function
		* @param o1 The first object
		* @param o2 The second object
		* @return The compare result. 1 is o1 < o2; 0 is o1.equals(o2); -1 otherwise
		*/
		public int compare ( Object o1, Object o2)
		{
			try
			{
				return ((Tool) o1).getTitle().compareTo(((Tool) o2).getTitle());
			}
			catch (Exception e)
			{
			}
			return -1;
			
		}	// compare
		
	} //ToolComparator
	
	public class Icon
	{
		protected String m_name = null;
		protected String m_url = null;
		protected String m_skin = null;

		public Icon(String name, String url, String skin)
		{
			m_name = name;
			m_url = url;
			m_skin = skin;
		}

		public String getName() { return m_name; }
		public String getUrl() { return m_url; }
		public String getSkin() { return m_skin; }
	}
	
	// a utility class for form select options
	public class IdAndText
	{
		public int id;
		public String text;

		public int getId() { return id;}
		public String getText() { return text;}
		
	} // IdAndText
	
	// a utility class for working with ToolConfigurations and ToolRegistrations
	// %%% convert featureList from IdAndText to Tool so getFeatures item.id = chosen-feature.id is a direct mapping of data
	public class MyTool
	{
		public String id = NULL_STRING;
		public String title = NULL_STRING;
		public String description = NULL_STRING;
		public boolean selected = false;
		
		public String getId() { return id; }
		public String getTitle() { return title; }
		public String getDescription() { return description; }
		public boolean getSelected() { return selected; }
		
	}
	
	/*
	* WorksiteSetupPage is a utility class for working with site pages configured by Worksite Setup
	*
	*/
	public class WorksiteSetupPage
	{
		public String pageId = NULL_STRING;
		public String pageTitle = NULL_STRING;
		public String toolId = NULL_STRING;
		
		public String getPageId() { return pageId; }
		public String getPageTitle() { return pageTitle; }
		public String getToolId() { return toolId; }
		
	} // WorksiteSetupPage
	
	/**
	* Participant in site access roles
	*
	*/
	public class Participant
	{
		public String name = NULL_STRING;
		// Note: uniqname is really a user ID
		public String uniqname = NULL_STRING;
		public String role = NULL_STRING; 
		
		public String getName() {return name; }
		public String getUniqname() {return uniqname; }
		public String getRole() { return role; } // cast to Role
		public boolean isRemoveable(){return true;}

		/**
		 * Access the user eid, if we can find it - fall back to the id if not.
		 * @return The user eid.
		 */
		public String getEid()
		{
			try
			{
				return UserDirectoryService.getUserEid(uniqname);
			}
			catch (UserNotDefinedException e)
			{
				return uniqname;
			}
		}

	} // Participant
	
	/**
	* SiteItem for display purposes in Sitemanage
	*
	*/
	public class SiteItem
	{
		public Site m_site = null;
		
		public void setSite(Site site) { m_site = site; }
		public Site getSite() { return m_site; }
		
		public Boolean isCourseSite()
		{
			String siteType = m_site.getType();
			if (siteType != null && siteType.equalsIgnoreCase("course"))
				return Boolean.TRUE;
			else
				return Boolean.FALSE;
		}
		
		public Boolean disableCourseSelection()
		{
		  if ((ServerConfigurationService.getString("disable.course.site.skin.selection")).equals("true"))
		    return Boolean.TRUE;
		  else
		    return Boolean.FALSE;
		}
		
		List m_providerCourseList = null;
		public void setProviderCourseList(List list)
		{
			m_providerCourseList = list;
		}
		public List getProviderCourseList()
		{
			return m_providerCourseList;
		}
		
		public List getManualCourseList()
		{
			String manualCourseListString = m_site.getProperties().getProperty(PROP_SITE_REQUEST_COURSE);
			List manualCourseList = new Vector();
			if (manualCourseListString != null)
			{
				if (manualCourseListString.indexOf("+") != -1)
				{
					manualCourseList = new ArrayList(Arrays.asList(manualCourseListString.split("\\+")));
				}
				else
				{
					manualCourseList.add(manualCourseListString);
				}
			}
			return manualCourseList;
		}
	} // SiteItem
	
	/**
	* Student in roster
	*
	*/
	public class Student
	{
		public String name = NULL_STRING;
		public String uniqname = NULL_STRING;
		public String id = NULL_STRING;
		public String level = NULL_STRING;
		public String credits = NULL_STRING;
		public String role = NULL_STRING;
		public String course = NULL_STRING;
		public String section = NULL_STRING;
		
		public String getName() {return name; }
		public String getUniqname() {return uniqname; }
		public String getId() { return id; }
		public String getLevel() { return level; }
		public String getCredits() { return credits; }
		public String getRole() { return role; }
		public String getCourse() { return course; }
		public String getSection() { return section; }
		
	} // Student
	
	public class SiteInfo
	{
		public String site_id = NULL_STRING; // getId of Resource
		public String external_id = NULL_STRING; // if matches site_id connects site with U-M course information
		public String site_type = "";
		public String iconUrl = NULL_STRING;
		public String infoUrl = NULL_STRING;
		public boolean joinable = false;
		public String joinerRole = NULL_STRING;
		public String title = NULL_STRING; // the short name of the site
		public String short_description = NULL_STRING; // the short (20 char) description of the site
		public String description = NULL_STRING;  // the longer description of the site
		public String additional = NULL_STRING; // additional information on crosslists, etc.
		public boolean published = false;
		public boolean include = true;	// include the site in the Sites index; default is true.
		public String site_contact_name = NULL_STRING;	// site contact name
		public String site_contact_email = NULL_STRING;	// site contact email
		
		public String getSiteId() {return site_id;}
		public String getSiteType() { return site_type; }
		public String getTitle() { return title; } 
		public String getDescription() { return description; }
		public String getIconUrl() { return iconUrl; }
		public String getInfoUrll() { return infoUrl; }
		public boolean getJoinable() {return joinable; }
		public String getJoinerRole() {return joinerRole; }
		public String getAdditional() { return additional; }
		public boolean getPublished() { return published; }
		public boolean getInclude() {return include;}
		public String getSiteContactName() {return site_contact_name; }
		public String getSiteContactEmail() {return site_contact_email; }
		
	} // SiteInfo
	
	//dissertation tool related
	/**
	* doFinish_grad_tools is called when creation of a Grad Tools site is confirmed
	*/
	public void doFinish_grad_tools ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		ParameterParser params = data.getParameters ();

		//set up for the coming template
		state.setAttribute(STATE_TEMPLATE_INDEX, params.getString ("continue"));
		int index = Integer.valueOf(params.getString ("template-index")).intValue();
		actionForTemplate("continue", index, params, state);

		//add the pre-configured Grad Tools tools to a new site
		addGradToolsFeatures(state);
		
		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		schedulePeerFrameRefresh("sitenav");
		
		resetPaging(state);
		
	}// doFinish_grad_tools
	
	/**
	* addGradToolsFeatures adds features to a new Grad Tools student site
	*
	*/
	private void addGradToolsFeatures(SessionState state)
	{
		Site edit = null;
		Site template = null;
		
		//get a unique id
		String id = IdManager.createUuid();
		
		//get the Grad Tools student site template
		try
		{
			template = SiteService.getSite(SITE_GTS_TEMPLATE);
		}
		catch (Exception e)
		{
			if(Log.isWarnEnabled())
				M_log.warn("addGradToolsFeatures template " + e);
		}
		if(template != null)
		{
			//create a new site based on the template
			try
			{
				edit = SiteService.addSite(id, template);
			}
			catch(Exception e)
			{
				if(Log.isWarnEnabled())
					M_log.warn("addGradToolsFeatures add/edit site " + e);
			}
			
			//set the tab, etc.
			if(edit != null)
			{
				SiteInfo siteInfo = (SiteInfo)state.getAttribute(STATE_SITE_INFO);
				edit.setShortDescription(siteInfo.short_description);
				edit.setTitle(siteInfo.title);
				edit.setPublished(true);
				edit.setPubView(false);
				edit.setType(SITE_TYPE_GRADTOOLS_STUDENT);
				//ResourcePropertiesEdit rpe = edit.getPropertiesEdit();
				try
				{
					SiteService.save(edit);
				}
				catch(Exception e)
				{
					if(Log.isWarnEnabled())
						M_log.warn("addGradToolsFeartures commitEdit " + e);
				}
				
				//now that the site and realm exist, we can set the email alias
				//set the GradToolsStudent site alias as: gradtools-uniqname@servername
				String alias = "gradtools-" +  SessionManager.getCurrentSessionUserId();
				String channelReference = mailArchiveChannelReference(id);
				try
				{
					AliasService.setAlias(alias, channelReference);
				}
				catch (IdUsedException ee) 
				{
					addAlert(state, rb.getString("java.alias")+" " + alias + " "+rb.getString("java.exists"));
				}
				catch (IdInvalidException ee) 
				{
					addAlert(state, rb.getString("java.alias")+" " + alias + " "+rb.getString("java.isinval"));
				}
				catch (PermissionException ee) 
				{
					M_log.warn(SessionManager.getCurrentSessionUserId() + " does not have permission to add alias. ");
				}
			}
		}

	} // addGradToolsFeatures
	
	/**
	*  handle with add site options
	* 
	*/
	public void doAdd_site_option ( RunData data )
	{
		String option = data.getParameters().getString("option");
		if (option.equals("finish"))
		{
			doFinish(data);
		}
		else if (option.equals("cancel"))
		{
			doCancel_create(data);
		}
		else if (option.equals("back"))
		{
			doBack(data);
		}	
	}	// doAdd_site_option

	/**
	*  handle with duplicate site options
	* 
	*/
	public void doDuplicate_site_option ( RunData data )
	{
		String option = data.getParameters().getString("option");
		if (option.equals("duplicate"))
		{
			doContinue(data);
		}
		else if (option.equals("cancel"))
		{
			doCancel(data);
		}
		else if (option.equals("finish"))
		{
			doContinue(data);
		}
	}	// doDuplicate_site_option
	
	/**
	 * Special check against the Dissertation service, which might not be here...
	 * @return
	 */
	protected boolean isGradToolsCandidate(String userId)
	{
		// DissertationService.isCandidate(userId) - but the hard way

		Object service = ComponentManager.get("org.sakaiproject.api.app.dissertation.DissertationService");
		if (service == null) return false;

		// the method signature
		Class[] signature = new Class[1];
		signature[0] = String.class;

		// the method name
		String methodName = "isCandidate";

		// find a method of this class with this name and signature
		try
		{
			Method method = service.getClass().getMethod(methodName, signature);

			// the parameters
			Object[] args = new Object[1];
			args[0] = userId;

			// make the call
			Boolean rv = (Boolean) method.invoke(service, args);
			return rv.booleanValue();
		}
		catch (Throwable t) 
		{
		}
		
		return false;
	}
	/**
	 * User has a Grad Tools student site
	 * @return
	 */
	protected boolean hasGradToolsStudentSite(String userId)
	{
		boolean has = false;
		int n = 0;
		try
		{
			n = SiteService.countSites(org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
					SITE_TYPE_GRADTOOLS_STUDENT, null, null);
			if(n > 0)
				has = true;
		}
		catch(Exception e)
		{
			if(Log.isWarnEnabled())
				M_log.warn("hasGradToolsStudentSite "  + e);
		}
	
		return has;
			
	}//hasGradToolsStudentSite

	/**
	 * Get the mail archive channel reference for the main container placement for this site.
	 * @param siteId The site id.
	 * @return The mail archive channel reference for this site.
	 */
	protected String mailArchiveChannelReference(String siteId)
	{
		return MailArchiveService.channelReference(siteId, SiteService.MAIN_CONTAINER);
	}

	/**
	 * Transfer a copy of all entites from another context for any entity producer that claims this tool id.
	 * 
	 * @param toolId
	 *        The tool id.
	 * @param fromContext
	 *        The context to import from.
	 * @param toContext
	 *        The context to import into.
	 */
	protected void transferCopyEntities(String toolId, String fromContext, String toContext)
	{
		// TODO: used to offer to resources first - why? still needed? -ggolden

		// offer to all EntityProducers
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i.hasNext();)
		{
			EntityProducer ep = (EntityProducer) i.next();
			if (ep instanceof EntityTransferrer)
			{
				try
				{
					EntityTransferrer et = (EntityTransferrer) ep;

					// if this producer claims this tool id
					if (ArrayUtil.contains(et.myToolIds(), toolId))
					{
						et.transferCopyEntities(fromContext, toContext, new Vector());
					}
				}
				catch (Throwable t)
				{
					M_log.warn("Error encountered while asking EntityTransfer to transferCopyEntities from: " + fromContext
							+ " to: " + toContext, t);
				}
			}
		}
	}
	
	/**
	 * @return Get a list of all tools that support the import (transfer copy) option
	 */
	protected Set importTools()
	{
		HashSet rv = new HashSet();

		// offer to all EntityProducers
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i.hasNext();)
		{
			EntityProducer ep = (EntityProducer) i.next();
			if (ep instanceof EntityTransferrer)
			{
				EntityTransferrer et = (EntityTransferrer) ep;

				String[] tools = et.myToolIds();
				if (tools != null)
				{
					for (int t = 0; t < tools.length; t++)
					{
						rv.add(tools[t]);
					}
				}
			}
		}

		return rv;
	}
}
