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
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.tool;

import static org.sakaiproject.content.util.IdUtil.isolateContainingId;
import static org.sakaiproject.content.util.IdUtil.isolateName;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.sakaiproject.alias.api.AliasEdit;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceHelperAction;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentPrintService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.GroupAwareEntity;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.content.api.MultiFileUploadPipe;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.SiteSpecificResourceType;
import org.sakaiproject.content.api.providers.SiteContentAdvisor;
import org.sakaiproject.content.api.providers.SiteContentAdvisorProvider;
import org.sakaiproject.content.copyright.api.CopyrightInfo;
import org.sakaiproject.content.copyright.api.CopyrightItem;
import org.sakaiproject.content.exception.ZipMaxTotalSizeException;
import org.sakaiproject.content.tool.inputpreserver.*;
import org.sakaiproject.content.util.ZipContentUtil;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.comparator.GroupTitleComparator;
import org.sakaiproject.util.comparator.ResourceTypeLabelComparator;
import org.w3c.dom.Element;

import lombok.extern.slf4j.Slf4j;

/**
* <p>ResourceAction is a ContentHosting application</p>
*
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
@Slf4j
public class ResourcesAction 
	extends PagedResourceHelperAction // VelocityPortletPaneledAction
{
	 /** the content print service */
	 private static final ContentPrintService contentPrintService = (ContentPrintService) ComponentManager.get("org.sakaiproject.content.api.ContentPrintService");
	 	 
	 /** ContentTypeImageService **/
	 private static final ContentTypeImageService contentTypeImageService = ComponentManager.get(ContentTypeImageService.class);
	 
	 private static final SchedulerManager schedulerManager = (SchedulerManager) ComponentManager.get("org.sakaiproject.api.app.scheduler.SchedulerManager");

	 /** state variable name for the content print service call result */
	 private static final String CONTENT_PRINT_CALL_RESPONSE = "content_print_call_response";
	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String PIPE_INIT_ID = "pipe-init-id";

	/** kernel api **/
	private static final ContentHostingService contentHostingService = ComponentManager.get(ContentHostingService.class);
	private static final SecurityService securityService  = ComponentManager.get(SecurityService.class);
	private static final SiteService siteService = ComponentManager.get(SiteService.class);
	private static final EntityManager entityManager = ComponentManager.get(EntityManager.class);
	private static final SessionManager sessionManager = ComponentManager.get(SessionManager.class);
	private static final ToolManager toolManager = ComponentManager.get(ToolManager.class);
	private static final UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);
	private static final TimeService timeService = ComponentManager.get(TimeService.class);
	private static final EventTrackingService eventTrackingService = ComponentManager.get(EventTrackingService.class);


	public static final String MSG_KEY_COPYRIGHT_REQ_CHOICE = "copyright.requireChoice";
	public static final String MSG_KEY_COPYRIGHT_REQ_CHOICE_ERROR = "copyright.requireChoice.error";

	public static final String SAK_PROP_COPYRIGHT_REQ_CHOICE = "copyright.requireChoice";
	private static final String SAK_PROP_COPYRIGHT_DEFAULT_TYPE = "copyright.type.default";
	public static final boolean SAK_PROP_COPYRIGHT_REQ_CHOICE_DEFAULT = false;

	private static final org.sakaiproject.content.copyright.api.CopyrightManager copyrightManager = (org.sakaiproject.content.copyright.api.CopyrightManager)
			ComponentManager.get("org.sakaiproject.content.copyright.api.CopyrightManager");

	/**
	 * Action
	 *
	 */
	public static class Action
	{
		protected String actionId;
		protected String label;
		
		/**
		 * @return the actionId
		 */
		public String getActionId()
		{
			return this.actionId;
		}
		
		/**
		 * @return the label
		 */
		public String getLabel()
		{
			return this.label;
		}
		
		/**
		 * @param actionId the actionId to set
		 */
		
		public void setActionId(String actionId)
		{
			this.actionId = actionId;
		}
		
		/**
		 * @param label the label to set
		 */
		public void setLabel(String label)
		{
			this.label = label;
		}
		
	}
    
	/**
	 * Inner class encapsulates information about folders (and final item?) in a collection path (a.k.a. breadcrumb)
	 * This is being phased out as we switch to the resources type registry.
	 */
	public static class ChefPathItem
	{
		protected boolean m_canRead;
		protected String m_id;
		protected boolean m_isFolder;
		protected boolean m_isLast;
		protected boolean m_isLocal;
		protected String m_name;
		protected String m_root;
		protected String m_url;

		public ChefPathItem(String id, String name)
		{
			m_id = id;
			m_name = name;
			m_canRead = false;
			m_isFolder = false;
			m_isLast = false;
			m_url = "";
			m_isLocal = true;
		}

		/**
		 * @return
		 */
		public boolean canRead()
		{
			return m_canRead;
		}

		/**
		 * @return
		 */
		public String getId()
		{
			return m_id;
		}

		/**
		 * @return
		 */
		public String getName()
		{
			return m_name;
		}

		/**
		 * @return
		 */
		public String getRoot()
		{
			return m_root;
		}

		/**
		 * @return
		 */
		public String getUrl()
		{
			return m_url;
		}

		/**
		 * @return
		 */
		public boolean isFolder()
		{
			return m_isFolder;
		}

		/**
		 * @return
		 */
		public boolean isLast()
		{
			return m_isLast;
		}

		public boolean isLocal()
		{
			return m_isLocal;
		}

		/**
		 * @param canRead
		 */
		public void setCanRead(boolean canRead)
		{
			m_canRead = canRead;
		}

		/**
		 * @param id
		 */
		public void setId(String id)
		{
			m_id = id;
		}

		/**
		 * @param isFolder
		 */
		public void setIsFolder(boolean isFolder)
		{
			m_isFolder = isFolder;
		}

		public void setIsLocal(boolean isLocal)
		{
			m_isLocal = isLocal;
		}

		/**
		 * @param isLast
		 */
		public void setLast(boolean isLast)
		{
			m_isLast = isLast;
		}

		/**
		 * @param name
		 */
		public void setName(String name)
		{
			m_name = name;
		}

		/**
		 * @param root
		 */
		public void setRoot(String root)
		{
			m_root = root;
		}

		/**
		 * @param url
		 */
		public void setUrl(String url)
		{
			m_url = url;
		}

	}	// inner class ChefPathItem
    
	public enum ContentPermissions
	{
		CREATE, DELETE, READ, REVISE, SITE_UPDATE
	}
	
	public static class ElementCarrier
	{
		protected Element element;
		protected String parent;

		public ElementCarrier(Element element, String parent)
		{
			this.element = element;
			this.parent = parent;

		}

		public Element getElement()
		{
			return element;
		}

		public String getParent()
		{
			return parent;
		}

		public void setElement(Element element)
		{
			this.element = element;
		}

		public void setParent(String parent)
		{
			this.parent = parent;
		}

	}
	
	/** Resource bundle using current language locale */
    private static final ResourceLoader rb = new ResourceLoader("content");
	/** Resource bundle using current language locale */
    public static final ResourceLoader trb = new ResourceLoader("types");
    /** Resource bundle using current language locale */
    private static final ResourceLoader rrb = new ResourceLoader("right");
    /** Resource bundle using current language locale */
    private static final ResourceLoader metaLang = new ResourceLoader("metadata");

	/** Shared messages */
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.sharedI18n.SharedProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.sharedI18n.bundle.shared";
	private static final String RESOURCECLASS = "resource.class.shared";
	private static final String RESOURCEBUNDLE = "resource.bundle.shared";
	private final String resourceClass = ServerConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
	private final String resourceBundle = ServerConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
	private final ResourceLoader srb = Resource.getResourceLoader(resourceClass, resourceBundle);
	
	static final ResourceConditionsHelper conditionsHelper = new ResourceConditionsHelper();

	public static final String PREFIX = "resources.";
	public static final String SYS = "sys.";
	public static final String REQUEST = "request.";
	
	public static final List<ActionType> ACTIONS_ON_FOLDERS = new ArrayList<ActionType>();
	public static final List<ActionType> ACTIONS_ON_MULTIPLE_ITEMS = new ArrayList<ActionType>();
	public static final List<ActionType> ACTIONS_ON_RESOURCES = new ArrayList<ActionType>();
	
	public static final List<ActionType> CONTENT_DELETE_ACTIONS = new ArrayList<ActionType>();
	public static final List<ActionType> CONTENT_MODIFY_ACTIONS = new ArrayList<ActionType>();
	public static final List<ActionType> CONTENT_NEW_ACTIONS = new ArrayList<ActionType>();
	public static final List<ActionType> CONTENT_NEW_FOR_PARENT_ACTIONS = new ArrayList<ActionType>();
	public static final List<ActionType> CONTENT_READ_ACTIONS = new ArrayList<ActionType>();
	public static final List<ActionType> CONTENT_PROPERTIES_ACTIONS = new ArrayList<ActionType>();
	
	public static final List<ActionType> CREATION_ACTIONS = new ArrayList<ActionType>();

	public static final List<ActionType> PASTE_COPIED_ACTIONS = new ArrayList<ActionType>();
	public static final List<ActionType> PASTE_MOVED_ACTIONS = new ArrayList<ActionType>();
	
	public static final List<ActionType> SITE_UPDATE_ACTIONS = new ArrayList<ActionType>();

	/** copyright path -- MUST have same value as AccessServlet.COPYRIGHT_PATH */
	public static final String COPYRIGHT_PATH = Entity.SEPARATOR + "copyright";

	private static final String COPYRIGHT_ALERT_URL = ServerConfigurationService.getAccessUrl() + COPYRIGHT_PATH;
	
	private static final int CREATE_MAX_ITEMS = 10;
    
	/** The default number of site collections per page. */
	protected static final int DEFAULT_PAGE_SIZE = 50;
	
	public static final String DELIM = "@";
	
	public static final String DROPBOX_NOTIFICATIONS_PROPERTY = "dropbox_notifications_property";
	public static final String DROPBOX_NOTIFICATIONS_PARAMETER_NAME = "dropbox_notification";
	
	public static final String DROPBOX_NOTIFICATIONS_NONE = "dropbox-emails-none";
	public static final String DROPBOX_NOTIFICATIONS_ALLOW = "dropbox-emails-allowed";
	public static final String DROPBOX_NOTIFICATIONS_ALWAYS = "dropbox-emails-always";
	
	public static final String DROPBOX_NOTIFICATIONS_DEFAULT_VALUE = DROPBOX_NOTIFICATIONS_NONE;

	/** The default number of members for a collection at which this tool should refuse to expand the collection. Used only if value can't be read from config service. */
	protected static final int EXPANDABLE_FOLDER_SIZE_LIMIT = 256;

	private static final String LIST_COLUMNS = "columns";

	private static final String LIST_HIERARCHY = "hierarchy";

	public static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;

	/** The maximum number of suspended operations that can be on the stack. */
	private static final int MAXIMUM_SUSPENDED_OPERATIONS_STACK_DEPTH = 10;

	public static final String MIME_TYPE_DOCUMENT_HTML = "text/html";

	public static final String MIME_TYPE_DOCUMENT_PLAINTEXT = "text/plain";
	
	public static final String MIME_TYPE_STRUCTOBJ = "application/x-osp";

	public static final String MODE_ATTACHMENT_CONFIRM = PREFIX + "attachment_confirm";

	public static final String MODE_ATTACHMENT_CONFIRM_INIT = PREFIX + "attachment_confirm_initialized";

	public static final String MODE_ATTACHMENT_CREATE = PREFIX + "attachment_create";

	public static final String MODE_ATTACHMENT_CREATE_INIT = PREFIX + "attachment_create_initialized";

	public static final String MODE_ATTACHMENT_DONE = PREFIX + "attachment_done";

	public static final String MODE_ATTACHMENT_EDIT_ITEM = PREFIX + "attachment_edit_item";

	public static final String MODE_ATTACHMENT_EDIT_ITEM_INIT = PREFIX + "attachment_edit_item_initialized";

	public static final String MODE_ATTACHMENT_NEW_ITEM = PREFIX + "attachment_new_item";

	public static final String MODE_ATTACHMENT_NEW_ITEM_INIT = PREFIX + "attachment_new_item_initialized";

	/** modes for attachment helper */
	public static final String MODE_ATTACHMENT_SELECT = PREFIX + "attachment_select";

	public static final String MODE_ATTACHMENT_SELECT_INIT = PREFIX + "attachment_select_initialized";

	private static final String MODE_CREATE_WIZARD = "createWizard";

	/************** the more context *****************************************/

	private static final String MODE_DAV = "webdav";
	
	private static final String MODE_DROPBOX_MULTIPLE_FOLDERS_UPLOAD = "dropboxMultipleFoldersUpload";
	
	private static final String MODE_QUOTA = "quota";

	/************** the edit context *****************************************/

	private static final String MODE_DELETE_FINISH = "deleteFinish";
	private static final String MODE_SHOW_FINISH = "showFinish";
	private static final String MODE_HIDE_FINISH = "hideFinish"; 
	
	private static final String MODE_ZIPDOWNLOAD_FINISH = "zipDownloadFinish";
	
	private static final String MODE_DROPBOX_OPTIONS = "dropboxOptions";

	public  static final String MODE_HELPER = "helper";
	
	/** Modes. */
	private static final String MODE_LIST = "list";
	

	private static final String MODE_PROPERTIES = "properties";
	
	private static final String MODE_REORDER = "reorder";

	private static final String MODE_RESTORE = "restore";
	
	protected static final String MODE_REVISE_METADATA = "revise_metadata";
	
	protected static final String MODE_MAKE_SITE_PAGE = "make_site_page";

	protected static final String MODE_PERMISSIONS = "permissions_page";

	/** The null/empty string */
	private static final String NULL_STRING = "";
	
	/** A long representing the number of milliseconds in one week.  Used for date calculations */
		protected static final long ONE_WEEK = 1000L * 60L * 60L * 24L * 7L;

protected static final String PARAM_PAGESIZE = "collections_per_page";
	
	/** string used to represent "public" access mode in UI elements */
	protected static final String PUBLIC_ACCESS = "public";

	public static final String RESOURCES_MODE_DROPBOX = "dropbox";
	
	public static final String RESOURCES_MODE_HELPER = "helper";
	
	public static final String RESOURCES_MODE_RESOURCES = "resources";

	/** The default value for whether to show all sites in dropbox (used if global value can't be read from server config service) */
	private static final boolean SHOW_ALL_SITES_IN_DROPBOX = false;
	
	/** The default value for whether to show all sites in file-picker (used if global value can't be read from server config service) */
	public static final boolean SHOW_ALL_SITES_IN_FILE_PICKER = false;
	/** The default value for whether to show all sites in resources tool (used if global value can't be read from server config service) */
	private static final boolean SHOW_ALL_SITES_IN_RESOURCES = false;
	/** The collection id being browsed. */
	public static final String SAK_PROP_SHOW_ALL_SITES_IN_TOOL = PREFIX + "show_all_collections.tool";
	public static final String SAK_PROP_SHOW_ALL_SITES_IN_DROPBOX = PREFIX + "show_all_collections.dropbox";
	public static final String SAK_PROP_SHOW_ALL_SITES_IN_HELPER = PREFIX + "show_all_collections.helper";

	private static final String STATE_COLLECTION_ID = PREFIX + REQUEST + "collection_id";
	
	
	/** The collection id path */
	private static final String STATE_COLLECTION_PATH = PREFIX + REQUEST + "collection_path";
	
	public static final String STATE_COLUMN_ITEM_ID = PREFIX + REQUEST + "state_column_item_id";

	/** The content hosting service in the State. */
	private static final String STATE_CONTENT_SERVICE = PREFIX + SYS + "content_service";
	
	/** The content type image lookup service in the State. */
	private static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = PREFIX + SYS + "content_type_image_service";
	
	/** The copied item ids */
	private static final String STATE_COPIED_IDS = PREFIX + REQUEST + "revise_copied_ids";
	
	/** The copy flag */
	private static final String STATE_COPY_FLAG = PREFIX + REQUEST + "copy_flag";
	
	//	public static final String STATE_CREATE_TYPE = PREFIX + REQUEST + "create_type";
	public static final String STATE_CREATE_COLLECTION_ID = PREFIX + REQUEST + "create_collection_id";

	protected static final String STATE_CREATE_MESSAGE = PREFIX + REQUEST + "create_message";

	public static final String STATE_CREATE_NUMBER = PREFIX + REQUEST + "create_number";

	protected static final String STATE_CREATE_WIZARD_ACTION = PREFIX + REQUEST + "create_wizard_action";

	protected static final String STATE_CREATE_WIZARD_COLLECTION_ID = PREFIX + REQUEST + "create_wizard_collection_id";

	protected static final String STATE_CREATE_WIZARD_ITEM = PREFIX + REQUEST + "create_wizard_item";

	/** name of state attribute for the default retract time */
	protected static final String STATE_DEFAULT_RETRACT_TIME = PREFIX + SYS + "default_retract_time";

	/** The name of the state attribute containing a list of ListItem objects corresponding to resources selected for deletion */
	private static final String STATE_DELETE_ITEMS = PREFIX + REQUEST + "delete_items";

	/** The name of the state attribute containing a list of ListItem objects corresponding to nonempty folders selected for deletion */
	private static final String STATE_DELETE_ITEMS_NOT_EMPTY = PREFIX + REQUEST + "delete_items_not_empty";

	protected static final String STATE_DELETE_SET = PREFIX + REQUEST + "delete_set";
	
	protected static final String STATE_SHOW_SET = PREFIX + "show_set";
	protected static final String STATE_HIDE_SET = PREFIX + "hide_set"; 
	
	protected static final String STATE_ZIPDOWNLOAD_SET = PREFIX + "zipDownload_set";

	protected static final String STATE_DROPBOX_HIGHLIGHT = PREFIX + REQUEST + "dropbox_highlight";

	/** The name of the state attribute indicating whether the hierarchical list is expanded */
	private static final String STATE_EXPAND_ALL_FLAG = PREFIX + REQUEST + "expand_all_flag";

	/** Name of state attribute indicating number of members for a collection at which this tool should refuse to expand the collection. */
	private static final String STATE_EXPANDABLE_FOLDER_SIZE_LIMIT = PREFIX + SYS + "expandable_folder_size_limit";

	/** Name of state attribute containing a list of opened/expanded collections.
	 * It's a sorted set that is unmodifiable. */
	private static final String STATE_EXPANDED_COLLECTIONS = PREFIX + REQUEST + "expanded_collections";
	
	protected static final String STATE_EXPANDED_FOLDER_SORT_MAP = PREFIX + REQUEST + "expanded_folder_sort_map";
	
	/** state attribute for the maximum size for file upload */
	static final String STATE_FILE_UPLOAD_MAX_SIZE = PREFIX + SYS + "file_upload_max_size";
	
	/** The from state name */
	private static final String STATE_FROM = PREFIX + REQUEST + "from";
	
	/** The value of STATE_MODE from the most recent invocation of buildMainPanelContext */
	private static final String STATE_LAST_MODE = "last_mode";

	/** State attribute for where there is at least one attachment before invoking attachment tool */
	public static final String STATE_HAS_ATTACHMENT_BEFORE = PREFIX + REQUEST + "has_attachment_before";
	
	/**
	 *  the name of the state attribute indicating that the user canceled out of the helper.  Is set only if the user canceled out of the helper. 
	 */
	public static final String STATE_HELPER_CANCELED_BY_USER = PREFIX + REQUEST + "state_attach_canceled_by_user";

	protected static final String STATE_HIGHLIGHTED_ITEMS = PREFIX + REQUEST + "highlighted_items";
	
	/** The display name of the "home" collection (can't go up from here.) */
	private static final String STATE_HOME_COLLECTION_DISPLAY_NAME = PREFIX + REQUEST + "collection_home_display_name";
	
	/** The id of the "home" collection (can't go up from here.) */
	private static final String STATE_HOME_COLLECTION_ID = PREFIX + REQUEST + "collection_home";

	/** Name of state attribute for status of initialization.  */
	private static final String STATE_INITIALIZED = PREFIX + REQUEST + "initialized";
	
	protected static final String STATE_ITEMS_TO_BE_COPIED = PREFIX + REQUEST + "items_to_be_copied";
	
	protected static final String STATE_ITEMS_TO_BE_MOVED = PREFIX + REQUEST + "items_to_be_moved";

	private static final String STATE_LIST_PREFERENCE = PREFIX + REQUEST + "state_list_preference";
	
	/** The name of the state attribute containing a java.util.Set with the id's of selected items */
	private static final String STATE_LIST_SELECTIONS = PREFIX + REQUEST + "ignore_delete_selections";
	
	protected static final String STATE_LIST_VIEW_SORT = PREFIX + REQUEST + "list_view_sort";

	private static final String STATE_MESSAGE_LIST = PREFIX + REQUEST + "message_list";

	/** The resources, helper or dropbox mode. */
	public static final String STATE_MODE_RESOURCES = PREFIX + REQUEST + "resources_mode";
	
	/** The more collection id */
	private static final String STATE_MORE_COLLECTION_ID = PREFIX + REQUEST + "more_collection_id";
	
	/** The more id */
	private static final String STATE_MORE_ID = PREFIX + REQUEST + "more_id";
	
	/** The move flag */
	private static final String STATE_MOVE_FLAG = PREFIX + REQUEST + "move_flag";
	
	/** The copied item ids */
	private static final String STATE_MOVED_IDS = PREFIX + REQUEST + "revise_moved_ids";
	
	/** The root of the navigation breadcrumbs for a folder, either the home or another site the user belongs to */
	private static final String STATE_NAVIGATION_ROOT = PREFIX + REQUEST + "navigation_root";

	/** The name of the state attribute indicating whether the hierarchical list needs to be expanded */
	private static final String STATE_NEED_TO_EXPAND_ALL = PREFIX + REQUEST + "need_to_expand_all";

	protected static final String STATE_NON_EMPTY_DELETE_SET = PREFIX + REQUEST + "non-empty_delete_set";
	protected static final String STATE_NON_EMPTY_SHOW_SET = PREFIX + "non-empty_show_set";
	protected static final String STATE_NON_EMPTY_HIDE_SET = PREFIX + "non-empty_hide_set";
	
	/** The can-paste flag */
	private static final String STATE_PASTE_ALLOWED_FLAG = PREFIX + REQUEST + "can_paste_flag";
	
	/** state attribute indicating whether users in current site should be denied option of making resources public */
	private static final String STATE_PREVENT_PUBLIC_DISPLAY = PREFIX + REQUEST + "prevent_public_display";
	
	protected static final String STATE_REMOVED_ATTACHMENTS = PREFIX + REQUEST + "removed_attachments";

	protected static final String STATE_REORDER_FOLDER = PREFIX + REQUEST + "reorder_folder_id";

	protected static final String STATE_REORDER_SORT = PREFIX + REQUEST + "reorder_sort";
	
	/** The sort ascending or decending for the reorder context */
	protected static final String STATE_REORDER_SORT_ASC = PREFIX + REQUEST + "sort_asc";

	/** The property (column) to sort by in the reorder context */
	protected static final String STATE_REORDER_SORT_BY = PREFIX + REQUEST + "reorder_sort_by";

	/** The resources, helper or dropbox mode. */
	public static final String STATE_RESOURCES_HELPER_MODE = PREFIX + REQUEST + "resources_helper_mode";
	
	private static final String STATE_RESOURCES_TYPE_REGISTRY = PREFIX + SYS + "type_registry";
	
	protected static final String STATE_REVISE_PROPERTIES_ACTION = PREFIX + REQUEST + "revise_properties_action";
	
	protected static final String STATE_REVISE_PROPERTIES_ENTITY_ID = PREFIX + REQUEST + "revise_properties_entity_id";
	
	protected static final String STATE_REVISE_PROPERTIES_ITEM = PREFIX + REQUEST + "revise_properties_item";
	
	/** The select all flag */
	private static final String STATE_SELECT_ALL_FLAG = PREFIX + REQUEST + "select_all_flag";
	
	/** The name of a state attribute indicating whether the resources tool/helper is allowed to show all sites the user has access to */
	public static final String STATE_SHOW_ALL_SITES = PREFIX + SYS + "allow_user_to_see_all_sites";
	
	protected static final String STATE_SHOW_COPY_ACTION = PREFIX + REQUEST + "show_copy_action";
	
	protected static final String STATE_SHOW_MOVE_ACTION = PREFIX + REQUEST + "show_move_action";

	/** The name of a state attribute indicating whether the wants to see other sites if that is enabled */
	public static final String STATE_SHOW_OTHER_SITES = PREFIX + REQUEST + "user_chooses_to_see_other_sites";

	protected static final String STATE_SHOW_REMOVE_ACTION = PREFIX + REQUEST + "show_remove_action";

	/** the site title */
	private static final String STATE_SITE_TITLE = PREFIX + REQUEST + "site_title";

	/** the site ID */
	private static final String STATE_SITE_ID = PREFIX + REQUEST + "site_id";

	/** The sort ascending or decending */
	private static final String STATE_SORT_ASC = PREFIX + REQUEST + "sort_asc";

	/** The sort by */
	private static final String STATE_SORT_BY = PREFIX + REQUEST + "sort_by";

	public static final String STATE_STACK_CREATE_COLLECTION_ID = PREFIX + REQUEST + "stack_create_collection_id";

	public static final String STATE_STACK_CREATE_NUMBER = PREFIX + REQUEST + "stack_create_number";

	public static final String STATE_STACK_CREATE_TYPE = PREFIX + REQUEST + "stack_create_type";

	public static final String STATE_STACK_EDIT_COLLECTION_ID = PREFIX + REQUEST + "stack_edit_collection_id";

	public static final String STATE_STACK_EDIT_ID = PREFIX + REQUEST + "stack_edit_id";

	public static final String STATE_STACK_STRUCTOBJ_TYPE = PREFIX + REQUEST + "stack_create_structured_object_type";

	public static final String STATE_SUSPENDED_OPERATIONS_STACK = PREFIX + REQUEST + "suspended_operations_stack";

	public static final String STATE_SUSPENDED_OPERATIONS_STACK_DEPTH = PREFIX + REQUEST + "suspended_operations_stack_depth";

	protected static final String STATE_TOP_MESSAGE_INDEX = PREFIX + REQUEST + "top_message_index";

	/** state attribute indicating whether we're using the Creative Commons dialog instead of the "old" copyright dialog */
	protected static final String STATE_USING_CREATIVE_COMMONS = PREFIX + SYS + "usingCreativeCommons";

	/** The title of the new page to be created in the site */
	protected static final String STATE_PAGE_TITLE = PREFIX + REQUEST+ "page_title";
	
	protected static final String STATE_MAKE_PAGE_ENTITY_ID = PREFIX + REQUEST+ "entity_id";
	
	
	/** vm files for each mode. */
	private static final String TEMPLATE_DAV = "content/chef_resources_webdav";

	private static final String TEMPLATE_QUOTA = "resources/sakai_quota";

	private static final String TEMPLATE_DELETE_CONFIRM = "content/chef_resources_deleteConfirm";

	private static final String TEMPLATE_DELETE_FINISH = "content/sakai_resources_deleteFinish";
	
	private static final String TEMPLATE_SHOW_FINISH = "content/sakai_resources_showFinish";
	private static final String TEMPLATE_HIDE_FINISH = "content/sakai_resources_hideFinish";
	
	private static final String TEMPLATE_ZIPDOWNLOAD_FINISH = "content/sakai_resources_zipDownloadFinish";

	private static final String TEMPLATE_DROPBOX_OPTIONS = "content/sakai_dropbox_options";

	private static final String TEMPLATE_DROPBOX_MULTIPLE_FOLDERS_UPLOAD = "resources/sakai_dropbox_multiple_folders_upload";
	
	private static final String TEMPLATE_MORE = "content/chef_resources_more";

	private static final String TEMPLATE_NEW_LIST = "content/sakai_resources_list";

	private static final String TEMPLATE_OPTIONS = "content/sakai_resources_options";
	
	private static final String TEMPLATE_REORDER = "content/chef_resources_reorder";

	private static final String TEMPLATE_RESTORE = "content/sakai_resources_restore";

	private static final String TEMPLATE_REVISE_METADATA = "content/sakai_resources_properties";

	protected static final String TEMPLATE_MAKE_SITE_PAGE = "content/sakai_make_site_page";

	protected static final String TEMPLATE_PERMISSIONS = "content/permissions";


	public static final String TYPE_HTML = MIME_TYPE_DOCUMENT_HTML;

	public static final String TYPE_TEXT = MIME_TYPE_DOCUMENT_PLAINTEXT;
	
	public static final String TYPE_UPLOAD = "file";
	
	public static final String TYPE_URL = "Url";
	
	public static final String UTF_8_ENCODING = "UTF-8";
	
	/** Configuration: allow use of alias for site id in references. */
	protected boolean m_siteAlias = true;
	
	/** the interval (in days) the the soft-deleted content will be automatically permanently removed **/
	public static final String STATE_CLEANUP_DELETED_CONTENT_INTERVAL= "state_cleanup_deleted_content_interval";
	// may need to distinguish permission on entity vs permission on its containing collection
	static
	{
		CONTENT_NEW_ACTIONS.add(ActionType.NEW_UPLOAD);
		CONTENT_NEW_ACTIONS.add(ActionType.NEW_FOLDER);
		CONTENT_NEW_ACTIONS.add(ActionType.NEW_URLS);
		CONTENT_NEW_ACTIONS.add(ActionType.CREATE);
		CONTENT_NEW_ACTIONS.add(ActionType.CREATE_BY_HELPER);
		
		PASTE_COPIED_ACTIONS.add(ActionType.PASTE_COPIED);
		PASTE_MOVED_ACTIONS.add(ActionType.PASTE_MOVED);
		
		CONTENT_NEW_FOR_PARENT_ACTIONS.add(ActionType.DUPLICATE);
		
		CONTENT_READ_ACTIONS.add(ActionType.VIEW_CONTENT);
		CONTENT_READ_ACTIONS.add(ActionType.PRINT_FILE);
		
		CONTENT_PROPERTIES_ACTIONS.add(ActionType.VIEW_METADATA);
		
		CONTENT_MODIFY_ACTIONS.add(ActionType.REVISE_METADATA);
		CONTENT_MODIFY_ACTIONS.add(ActionType.REVISE_CONTENT);
		CONTENT_MODIFY_ACTIONS.add(ActionType.COPY);
		CONTENT_MODIFY_ACTIONS.add(ActionType.REPLACE_CONTENT);
		CONTENT_MODIFY_ACTIONS.add(ActionType.REVISE_ORDER);
		CONTENT_MODIFY_ACTIONS.add(ActionType.COMPRESS_ZIP_FOLDER);
		CONTENT_MODIFY_ACTIONS.add(ActionType.EXPAND_ZIP_ARCHIVE);
		
		CONTENT_DELETE_ACTIONS.add(ActionType.MOVE);
		CONTENT_DELETE_ACTIONS.add(ActionType.DELETE);
		
		SITE_UPDATE_ACTIONS.add(ActionType.REVISE_PERMISSIONS);
		SITE_UPDATE_ACTIONS.add(ActionType.MAKE_SITE_PAGE);

		ACTIONS_ON_FOLDERS.add(ActionType.VIEW_METADATA);
		ACTIONS_ON_FOLDERS.add(ActionType.REVISE_METADATA);
		ACTIONS_ON_FOLDERS.add(ActionType.DUPLICATE);
		ACTIONS_ON_FOLDERS.add(ActionType.COPY);
		ACTIONS_ON_FOLDERS.add(ActionType.MOVE);
		ACTIONS_ON_FOLDERS.add(ActionType.DELETE);
		ACTIONS_ON_FOLDERS.add(ActionType.REVISE_ORDER);
		ACTIONS_ON_FOLDERS.add(ActionType.REVISE_PERMISSIONS);
		// ACTIONS_ON_FOLDERS.add(ActionType.PASTE_MOVED);

		ACTIONS_ON_RESOURCES.add(ActionType.VIEW_CONTENT);
		ACTIONS_ON_RESOURCES.add(ActionType.VIEW_METADATA);
		ACTIONS_ON_RESOURCES.add(ActionType.REVISE_METADATA);
		ACTIONS_ON_RESOURCES.add(ActionType.REVISE_CONTENT);
		ACTIONS_ON_RESOURCES.add(ActionType.REPLACE_CONTENT);
		ACTIONS_ON_RESOURCES.add(ActionType.DUPLICATE);
		ACTIONS_ON_RESOURCES.add(ActionType.COPY);
		ACTIONS_ON_RESOURCES.add(ActionType.MOVE);
		ACTIONS_ON_RESOURCES.add(ActionType.DELETE);
		ACTIONS_ON_RESOURCES.add(ActionType.PRINT_FILE);

		ACTIONS_ON_MULTIPLE_ITEMS.add(ActionType.COPY);
		ACTIONS_ON_MULTIPLE_ITEMS.add(ActionType.MOVE);
		ACTIONS_ON_MULTIPLE_ITEMS.add(ActionType.DELETE);
		
		CREATION_ACTIONS.add(ActionType.NEW_UPLOAD);
		CREATION_ACTIONS.add(ActionType.NEW_FOLDER);
		CREATION_ACTIONS.add(ActionType.NEW_URLS);
		CREATION_ACTIONS.add(ActionType.CREATE);
		CREATION_ACTIONS.add(ActionType.CREATE_BY_HELPER);
		CREATION_ACTIONS.add(ActionType.PASTE_MOVED);
		CREATION_ACTIONS.add(ActionType.PASTE_COPIED);
	}

	private final AliasService aliasService;
	private final AuthzGroupService authzGroupService;

	public ResourcesAction() {
		aliasService = ComponentManager.get(AliasService.class);
		authzGroupService = ComponentManager.get(AuthzGroupService.class);
	}

	/**
	* Build the context to show the list of resource properties
	*/
	public String buildMoreContext (	VelocityPortlet portlet,
									Context context,
									RunData data,
									SessionState state)
	{
		log.debug("ResourcesAction.buildMoreContext()");
		
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		String entityId = (String) state.getAttribute(STATE_MORE_ID);
		context.put ("id", entityId);
		String collectionId = (String) state.getAttribute(STATE_MORE_COLLECTION_ID);
		context.put ("collectionId", collectionId);
		String homeCollectionId = (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
		context.put("homeCollectionId", homeCollectionId);
		//List cPath = getCollectionPath(state);
		//context.put ("collectionPath", cPath);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);
		context.put("navRoot", navRoot);
		
		ListItem item = new ListItem(entityId);
		context.put("item", item);


		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			context.put("notExistFlag", false);
		}
		
		if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// notshow the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
		}
		else
		{
			//context.put("dropboxMode", Boolean.FALSE);
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			context.put("preventPublicDisplay", preventPublicDisplay);
			if(preventPublicDisplay.equals(Boolean.FALSE))
			{
				// find out about pubview
				boolean pubview = contentHostingService.isInheritingPubView(entityId);
				if (!pubview) pubview = contentHostingService.isPubView(entityId);
				context.put("pubview", pubview);
			}

		}
		
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));

		copyrightChoicesIntoContext(state, context);

		if (item.isUrl())
		{
			context.put("contentString", getEditItem(entityId, homeCollectionId, data).getContentstring());
		}

		return TEMPLATE_MORE;

	}	// buildMoreContext

	/**
	 *
	 * put copyright info into context
	 */
	public static void copyrightChoicesIntoContext(SessionState state, Context context)
	{
		log.debug("ResourcesAction.copyrightChoicesIntoContext()");
		boolean usingCreativeCommons = state.getAttribute(STATE_USING_CREATIVE_COMMONS) != null && state.getAttribute(STATE_USING_CREATIVE_COMMONS).equals(Boolean.TRUE.toString());		
		
		if(usingCreativeCommons)
		{
			
			String ccOwnershipLabel = rrb.getString("creative.ownershipLabel");
			List<String> ccOwnershipList = new ArrayList<>(Arrays.asList(rrb.getStrings("creative.ownership")));
						
			String ccMyGrantLabel = rrb.getString("creative.myGrantLabel");
			List<String> ccMyGrantOptions = new ArrayList<>(Arrays.asList(rrb.getStrings("creative.myGrant")));
			
			String ccCommercialLabel = rrb.getString("creative.commercialLabel");
			List<String> ccCommercialList = new ArrayList<>(Arrays.asList(rrb.getStrings("creative.commercial")));
			
			String ccModificationLabel = rrb.getString("creative.modificationLabel");
			List<String> ccModificationList = new ArrayList<>(Arrays.asList(rrb.getStrings("creative.modification")));
			
			String ccOtherGrantLabel = rrb.getString("creative.otherGrantLabel");
			List<String> ccOtherGrantList = new ArrayList<>(Arrays.asList(rrb.getStrings("creative.otherGrant")));
			
			String ccRightsYear = rrb.getString("creative.rightsYear");
			String ccRightsOwner = rrb.getString("creative.rightsOwner");
			
			String ccAcknowledgeLabel = rrb.getString("creative.acknowledgeLabel");
			List<String> ccAcknowledgeList = new ArrayList<>(Arrays.asList(rrb.getStrings("creative.acknowledge")));
						
			String ccInfoUrl = "";
			
			int year = timeService.newTime().breakdownLocal().getYear();
			String username = userDirectoryService.getCurrentUser().getDisplayName(); 

			context.put("usingCreativeCommons", Boolean.TRUE);
			context.put("ccOwnershipLabel", ccOwnershipLabel);
			context.put("ccOwnershipList", ccOwnershipList);
			context.put("ccMyGrantLabel", ccMyGrantLabel);
			context.put("ccMyGrantOptions", ccMyGrantOptions);
			context.put("ccCommercialLabel", ccCommercialLabel);
			context.put("ccCommercialList", ccCommercialList);
			context.put("ccModificationLabel", ccModificationLabel);
			context.put("ccModificationList", ccModificationList);
			context.put("ccOtherGrantLabel", ccOtherGrantLabel);
			context.put("ccOtherGrantList", ccOtherGrantList);
			context.put("ccRightsYear", ccRightsYear);
			context.put("ccRightsOwner", ccRightsOwner);
			context.put("ccAcknowledgeLabel", ccAcknowledgeLabel);
			context.put("ccAcknowledgeList", ccAcknowledgeList);
			context.put("ccInfoUrl", ccInfoUrl);
			context.put("ccThisYear", Integer.toString(year));
			context.put("ccThisUser", username);
		}
		else
		{
			//copyright
			context.put("fairuseurl", rrb.getString("fairuse.url"));
			
			context.put("publicdomain", rrb.getString("copyrighttype.1"));
			context.put("copyrightError", rb.getString(MSG_KEY_COPYRIGHT_REQ_CHOICE_ERROR));

			boolean copyrightReqChoice = ServerConfigurationService.getBoolean(SAK_PROP_COPYRIGHT_REQ_CHOICE, SAK_PROP_COPYRIGHT_REQ_CHOICE_DEFAULT);
			context.put("copyright_requireChoice", copyrightReqChoice);

			// Only provide default copyright choice if require choice property is false
			if (!copyrightReqChoice) {
				context.put("copyright_defaultType", ServerConfigurationService.getString(SAK_PROP_COPYRIGHT_DEFAULT_TYPE));
			}

			CopyrightInfo copyrightInfo = copyrightManager.getCopyrightInfo(new ResourceLoader().getLocale(), rrb.getStrings("copyrighttype"), ResourcesAction.class.getResource("ResourcesAction.class"));
			List<CopyrightItem> copyrightTypes = copyrightInfo.getItems();

			context.put("copyrightTypes", copyrightTypes);
			context.put("copyrightTypesSize", copyrightTypes.size());
			context.put("USE_THIS_COPYRIGHT", copyrightManager.getUseThisCopyright(rrb.getStrings("copyrighttype")));
			
		}
		
	}	// copyrightChoicesIntoContext
	
	public static void publicDisplayChoicesIntoContext(SessionState state, Context context)
	{
		log.debug("ResourcesAction.publicDisplayChoicesIntoContext()");
		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		context.put("preventPublicDisplay", preventPublicDisplay);
	}

	/**
	 * @param pipe
	 * @param state 
	 */
	public static List<ContentCollection> createFolders(SessionState state, ResourceToolActionPipe pipe)
	{
		log.debug("ResourcesAction.createFolders()");
		List<ContentCollection> new_collections = new ArrayList<>();
		String collectionId = pipe.getContentEntity().getId();
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) pipe;
		Iterator<ResourceToolActionPipe> pipeIt = mfp.getPipes().iterator();
		while(pipeIt.hasNext())
		{
			ResourceToolActionPipe fp = pipeIt.next();
			String name = fp.getFileName();
			if(StringUtils.isBlank(name))
			{
				continue;
			}
			try
			{
				ContentCollectionEdit edit = contentHostingService.addCollection(collectionId, Validator.escapeResourceName(name), MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				ResourcePropertiesEdit props = edit.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
				Object obj = fp.getRevisedListItem();
				if(obj != null && obj instanceof ListItem)
				{
					((ListItem) obj).updateContentCollectionEdit(edit);
				}
				ResourcePropertiesEdit resourceProperties = edit.getPropertiesEdit();
				String displayName = null;
				if(obj != null && obj instanceof ListItem)
				{
					displayName = ((ListItem) obj).getName();
				}
				if(StringUtils.isBlank(displayName))
				{
					displayName = name;
				}
				if(displayName != null)
				{
					resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
				}
				Map<String, String> values = pipe.getRevisedResourceProperties(); 	 	 
				for (Entry<String, String> entry : values.entrySet())
				{
					resourceProperties.addProperty(entry.getKey(), entry.getValue());
				}
				contentHostingService.commitCollection(edit);
				ResourceConditionsHelper.notifyCondition(edit);
				new_collections.add(edit);
			}
			catch (PermissionException e)
			{
				addAlert(state, trb.getString("alert.perm"));
				break;
			}
			catch (IdInvalidException e)
			{
				// TODO Auto-generated catch block
				log.warn("IdInvalidException {}{}", collectionId, name, e);
			}
			catch (IdUsedException|IdUniquenessException e)
			{
				String[] args = { name };
				addAlert(state, trb.getFormattedMessage("alert.exists", (Object[]) args));
			}
			catch (IdUnusedException e)
			{
				// TODO Auto-generated catch block
				log.warn("IdUnusedException {}{}", collectionId, name, e);
				break;
			}
			catch (IdLengthException e)
			{
				String[] args = { name };
				addAlert(state, trb.getFormattedMessage("alert.toolong", (Object[]) args));
				log.warn("IdLengthException {}{}", collectionId, name, e);
			}
			catch (TypeException e)
			{
				// TODO Auto-generated catch block
				log.warn("TypeException id = {}{}", collectionId, name, e);
			}
		}
		return (new_collections.isEmpty() ? null : new_collections);
	}

	/**
	 * @param pipe
	 */
	public static List<ContentResource> createResources(ResourceToolActionPipe pipe)
	{
		log.debug("ResourcesAction.createResources()");
		boolean item_added = false;
		String collectionId;
		List<ContentResource> new_resources = new ArrayList<>();
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) pipe;
		Iterator<ResourceToolActionPipe> pipeIt = mfp.getPipes().iterator();
		while(pipeIt.hasNext())
		{
			ResourceToolActionPipe fp = pipeIt.next();
			collectionId = pipe.getContentEntity().getId();
			String name = fp.getFileName();
			if(StringUtils.isBlank(name))
			{
				continue;
			}
			String basename = name.trim();
			String extension = "";
			if(name.contains("."))
			{
				String[] parts = name.split("\\.");
				StringBuilder sb = new StringBuilder(parts[0]);
				if(parts.length > 1)
				{
					extension = parts[parts.length - 1];
				}
				
				for(int i = 1; i < parts.length - 1; i++)
				{
					sb.append(".").append(parts[i]);
					// extension = parts[i + 1];
				}
				
				basename = sb.toString();
			}
			try
			{
				ContentResourceEdit resource = contentHostingService.addResource(collectionId,Validator.escapeResourceName(basename),Validator.escapeResourceName(extension),MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				
				extractContent(fp, resource);

				resource.setContentType(fp.getRevisedMimeType());
				resource.setResourceType(pipe.getAction().getTypeId());
				int notification = NotificationService.NOTI_NONE;
				Object obj = fp.getRevisedListItem();
				if(obj != null && obj instanceof ListItem)
				{
					((ListItem) obj).updateContentResourceEdit(resource);
					notification = ((ListItem) obj).getNotification();
				}
				
				ResourcePropertiesEdit resourceProperties = resource.getPropertiesEdit();
				String displayName = null;
				if(obj != null && obj instanceof ListItem)
				{
					displayName = ((ListItem) obj).getName();
				}
				if(StringUtils.isBlank(displayName))
				{
					displayName = name;
				}
				if(displayName != null)
				{
					resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
				}
				Map<String, String> values = pipe.getRevisedResourceProperties(); 	 	 
				for (Entry<String, String> entry : values.entrySet())
				{ 	 
					resourceProperties.addProperty(entry.getKey(), entry.getValue());
				}
				
//				if(MIME_TYPE_DOCUMENT_HTML.equals(fp.getRevisedMimeType()) || MIME_TYPE_DOCUMENT_PLAINTEXT.equals(fp.getRevisedMimeType()))
//				{
//					resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, UTF_8_ENCODING);
//				}
				
				try
				{
					contentHostingService.commitResource(resource, notification);
					ResourceConditionsHelper.notifyCondition(resource);
					item_added = true;
					new_resources.add(resource);
				}
				catch(VirusFoundException vfe) 
				{
					addAlert(trb.getFormattedMessage("alert.virusfound", new Object[]{vfe.getMessage()}));
					contentHostingService.cancelResource(resource);
				}
				catch(OverQuotaException e)
				{
					addAlert(trb.getFormattedMessage("alert.overquota", new Object[]{name}));
					log.debug("OverQuotaException {}", (Object) e);
					try
					{
						contentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						log.debug("Unable to remove partially completed resource: {}\n", resource.getId(), e);
					}
				}
				catch(ServerOverloadException e)
				{
					addAlert(trb.getFormattedMessage("alert.unable1", new Object[]{name}));
					log.debug("ServerOverloadException {}", (Object) e);
					try
					{
						contentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						log.debug("Unable to remove partially completed resource: {}\n", resource.getId(), e);
					}
				}
			}
			catch (PermissionException e)
			{
				addAlert(trb.getString("alert.perm"));
				log.warn("PermissionException ", e);
			}
			catch (IdUnusedException e)
			{
				// TODO Auto-generated catch block
				log.warn("IdUsedException ", e);
			}
			catch (IdInvalidException e)
			{
				// TODO Auto-generated catch block
				log.warn("IdInvalidException ", e);
			}
			catch (IdUniquenessException e)
			{
				// TODO Auto-generated catch block
				log.warn("IdUniquenessException ", e);
			}
			catch (IdLengthException e)
			{
				addAlert(trb.getFormattedMessage("alert.toolong", new Object[]{name}));
				
				// TODO Need to give error message to user
				log.warn("IdLengthException {}", (Object) e);
			}
			catch (OverQuotaException e)
			{
				addAlert(trb.getFormattedMessage("alert.overquota", new Object[]{name}));
				log.warn("OverQuotaException {}", (Object) e);
			}
			catch (ServerOverloadException e)
			{
				addAlert(trb.getFormattedMessage("alert.unable1", new Object[]{name}));
				log.warn("ServerOverloadException {}", (Object) e);
			}
		}
		
		return (item_added ? new_resources : null);
	}
	
	/**
     * Utility method to get revised content either from a byte array or a stream.
     */
    protected static void extractContent(ResourceToolActionPipe pipe, ContentResourceEdit resource)
    {
		log.debug("ResourcesAction.extractContent()");
	    byte[] content = pipe.getRevisedContent();
	    if(content == null)
	    {
	    	InputStream stream = pipe.getRevisedContentStream();
	    	if(stream == null)
	    	{
	    		log.debug("pipe with null content and null stream: {}", pipe.getFileName());
	    	}
	    	else
	    	{
	    		resource.setContent(stream);
	    	}
	    }
	    else
	    {
	    	resource.setContent(content);
	    }
    }

	/**
	* Paste the item(s) selected to be moved
	*/
	public static void doMoveitems ( RunData data)
	{
		log.debug("ResourcesAction.doMoveItems()");
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		ParameterParser params = data.getParameters ();

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		List<String> items = (List<String>) state.getAttribute(STATE_MOVED_IDS);

		String collectionId = params.getString ("collectionId");

		Iterator<String> itemIter = items.iterator();
		while (itemIter.hasNext())
		{
			// get the copied item to be pasted
			String itemId = (String) itemIter.next();

			String originalDisplayName = NULL_STRING;

			try
			{
				/*
				ResourceProperties properties = ContentHostingService.getProperties (itemId);
				originalDisplayName = properties.getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME);

				// copy, cut and paste not operated on collections
				if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
				{
					String alert = (String) state.getAttribute(STATE_MESSAGE);
					if (alert == null || ((alert != null) && (alert.indexOf(rb.getString("notsupported")) == -1)))
					{
						addAlert(state, rb.getString("notsupported"));
					}
				}
				else
				*/
				{
					contentHostingService.moveIntoFolder(itemId, collectionId);
				}	// if-else
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getFormattedMessage("notpermis8", new Object[] {originalDisplayName}));
			}
			catch (IdUnusedException e)
			{
				addAlert(state,rb.getString("notexist1"));
			}
			catch (InUseException e)
			{
				addAlert(state, rb.getFormattedMessage("someone", new Object[] {originalDisplayName}));
			}
			catch (TypeException e)
			{
				addAlert(state, rb.getFormattedMessage("pasteitem.mismatch", new Object[] {originalDisplayName}));
			}
			catch (InconsistentException e)
			{
				addAlert(state, rb.getFormattedMessage("recursive", new Object[] {itemId}));
			}
			catch(IdUsedException e)
			{
				addAlert(state, rb.getString("toomany"));
			}
			catch(ServerOverloadException e)
			{
				addAlert(state, rb.getString("failed"));
			}
			catch (OverQuotaException e)
			{
				addAlert(state, trb.getFormattedMessage("alert.overquota", new Object[]{ itemId }) );
			}
			catch(RuntimeException e)
			{
				log.debug("ResourcesAction.doMoveitems ***** Unknown Exception ***** {}", e.getMessage());
				addAlert(state, rb.getString("failed"));
			}	// try-catch

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				// delete sucessful
				String mode = (String) state.getAttribute(STATE_MODE);
				if(MODE_HELPER.equals(mode))
				{
					state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT);
				}
				else
				{
					state.setAttribute (STATE_MODE, MODE_LIST);
				}

				// try to expand the collection
				Set<String> expandedCollections = getExpandedCollections(state);
				expandedCollections.add(collectionId);

				state.setAttribute (STATE_MOVE_FLAG, Boolean.FALSE.toString());
			}

		}

	}	// doMoveitems
	
	/**
	* Paste the previously copied item(s)
	*/
	public static void doPasteitem ( RunData data)
	{
		log.debug("ResourcesAction.doPasteItem()");
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		ParameterParser params = data.getParameters ();

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// get the copied item to be pasted
		String itemId = params.getString("itemId");

		String collectionId = params.getString ("collectionId");

		duplicateItem(state, itemId, collectionId);

	}	// doPasteitem
	
	/**
	* Paste the previously copied item(s)
	*/
	public static void doPasteitems ( RunData data)
	{
		log.debug("ResourcesAction.doPasteItems()");
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		ParameterParser params = data.getParameters ();

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		List<String> items = (List<String>) state.getAttribute(STATE_COPIED_IDS);

		String collectionId = params.getString ("collectionId");

		Iterator<String> itemIter = items.iterator();
		while (itemIter.hasNext())
		{
			// get the copied item to be pasted
			String itemId = (String) itemIter.next();

			String originalDisplayName = NULL_STRING;

			try
			{
				String id = contentHostingService.copyIntoFolder(itemId, collectionId);
				String mode = (String) state.getAttribute(STATE_MODE);
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getFormattedMessage("notpermis8", new Object[] {originalDisplayName}));
			}
			catch (IdUnusedException e)
			{
				addAlert(state,rb.getString("notexist1"));
			}
			catch (InUseException e)
			{
				addAlert(state, rb.getFormattedMessage("someone", new Object[] {originalDisplayName}));
			}
			catch (TypeException e)
			{
				addAlert(state, rb.getFormattedMessage("pasteitem.mismatch", new Object[] {originalDisplayName}));
			}
			catch(IdUsedException e)
			{
				addAlert(state, rb.getString("toomany"));
			}
			catch(IdLengthException e)
			{
				addAlert(state, trb.getFormattedMessage("alert.toolong", new Object[]{e.getMessage()}));
			}
			catch(IdUniquenessException e)
			{
				addAlert(state, trb.getFormattedMessage("paste.error", new Object[]{itemId}));
			}
			catch(ServerOverloadException e)
			{
				addAlert(state, rb.getString("failed"));
			}
			catch(InconsistentException e)
			{
				addAlert(state, rb.getFormattedMessage("recursive", new Object[] {itemId}));
			}
			catch (OverQuotaException e)
			{
                int quota = 20;
                try {
                    ContentCollection collection = contentHostingService.getCollection(collectionId);
                    long cq = contentHostingService.getQuota(collection); // in kb
                    quota = (int)(cq / 1024);
                } catch (Exception e1) {
                    // nothing helpful to do here
                }
                addAlert(state, rb.getFormattedMessage("overquota", new Object[] {quota}));
			}	// try-catch
			catch(RuntimeException e)
			{
				log.debug("ResourcesAction.doPasteitems ***** Unknown Exception ***** {}", e.getMessage());
				addAlert(state, rb.getString("failed"));
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				// delete sucessful
				String mode = (String) state.getAttribute(STATE_MODE);
				if(MODE_HELPER.equals(mode))
				{
					state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT);
				}
				else
				{
					state.setAttribute (STATE_MODE, MODE_LIST);
				}

				// try to expand the collection
				Set<String> expandedCollections = getExpandedCollections(state);
				expandedCollections.add(collectionId);

				// reset the copy flag
				state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());
			}
		}

	}	// doPasteitems
	
	/**
	 * @param state
	 * @param itemId
	 * @param collectionId
	 */
	protected static String duplicateItem(SessionState state, String itemId, String collectionId)
	{
		log.debug("ResourcesAction.duplicateItem()");
		String originalDisplayName = NULL_STRING;

		String newId = null;
		String displayName;
		try
		{
			ResourceProperties properties = contentHostingService.getProperties (itemId);
			originalDisplayName = properties.getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME);

			// copy, cut and paste not operated on collections
			if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
			{
				String alert = (String) state.getAttribute(STATE_MESSAGE);
				if (alert == null || !alert.contains(rb.getString("notsupported")))
				{
					addAlert(state, rb.getString("notsupported"));
				}
			}
			else
			{
				// paste the resource
				ContentResource resource = contentHostingService.getResource (itemId);
				ResourceProperties p = contentHostingService.getProperties(itemId);
				String[] args = { p.getProperty(ResourceProperties.PROP_DISPLAY_NAME) };
				displayName = rb.getFormattedMessage("copy.name", (Object[]) args);

				String newItemId = contentHostingService.copyIntoFolder(itemId, collectionId);

				ContentResourceEdit copy = contentHostingService.editResource(newItemId);
				ResourcePropertiesEdit pedit = copy.getPropertiesEdit();
				pedit.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
				try
				{
					contentHostingService.commitResource(copy, NotificationService.NOTI_NONE);
					newId = copy.getId();
				}
				catch(OverQuotaException e)
				{
					addAlert(state, trb.getFormattedMessage("alert.overquota", new Object[]{displayName}));
					log.debug("OverQuotaException {}", (Object) e);
					try
					{
						contentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						log.debug("Unable to remove partially completed resource: {}", resource.getId(), e);
					}
				}
				catch(ServerOverloadException e)
				{
					addAlert(state, trb.getFormattedMessage("alert.unable1", new Object[]{displayName}));
					log.debug("ServerOverloadException {}", (Object) e);
					try
					{
						contentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						log.debug("Unable to remove partially completed resource: {}", resource.getId(), e);
					}
				}
			}	// if-else
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getFormattedMessage("notpermis8", new Object[] {originalDisplayName}));
		}
		catch (IdUnusedException e)
		{
			addAlert(state,rb.getString("notexist1"));
		}
		catch (IdUsedException e)
		{
			addAlert(state, rb.getFormattedMessage("notaddreso.used2", new Object[] {originalDisplayName}));
		}
		catch(IdLengthException e)
		{
			addAlert(state, trb.getFormattedMessage("alert.toolong", new Object[]{e.getMessage()}));
		}
		catch(IdUniquenessException e)
		{
            addAlert(state, trb.getFormattedMessage("paste.error", new Object[]{originalDisplayName}));
		}
		catch (InconsistentException ee)
		{
			addAlert(state, rb.getString("titlecannot"));
		}
		catch(InUseException e)
		{
			addAlert(state, rb.getFormattedMessage("someone", new Object[] {originalDisplayName}));
		}
		catch(OverQuotaException e)
		{
            int quota = 20;
            try {
                ContentCollection collection = contentHostingService.getCollection(collectionId);
                long cq = contentHostingService.getQuota(collection); // in kb
                quota = (int)(cq / 1024);
            } catch (Exception e1) {
                // nothing helpful to do here
            }
            addAlert(state, rb.getFormattedMessage("overquota", new Object[] {quota}));
		}
		catch(ServerOverloadException e)
		{
			// this represents temporary unavailability of server's filesystem
			// for server configured to save resource body in filesystem
			addAlert(state, rb.getString("failed"));
		}
		catch (TypeException e)
		{
			addAlert(state, rb.getFormattedMessage("pasteitem.mismatch", new Object[] {originalDisplayName}));
		}	// try-catch

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// delete sucessful
			String mode = (String) state.getAttribute(STATE_MODE);
			if(MODE_HELPER.equals(mode))
			{
				state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT);
			}
			else
			{
				state.setAttribute (STATE_MODE, MODE_LIST);
			}

			// try to expand the collection
			Set<String> expandedCollections = getExpandedCollections(state);
			expandedCollections.add(collectionId);

			// reset the copy flag
			state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());
		}
		return newId;
	}
	
	/**
     * @param selectedItem
	 * @param permissions TODO
	 * @param registry
	 * @return
     */
    protected static List<ResourceToolAction> getActions(ContentEntity selectedItem, Set<ContentPermissions> permissions, ResourceTypeRegistry registry)
    {
		log.debug("ResourcesAction.getActions()");
	    List<ResourceToolAction> actions = new ArrayList<>();
	    
	    ResourceType typeDef = getResourceType(selectedItem, registry);
	    if(typeDef == null)
	    {
	    	return actions;
	    }
	    
	    // if user has content.read, user can view content, view metadata and/or copy
	    if(permissions.contains(ContentPermissions.READ))
	    {
		    List<ResourceToolAction> contentReadActions = typeDef.getActions(CONTENT_READ_ACTIONS);
		    if(contentReadActions != null)
		    {
		    	actions.addAll(contentReadActions);
		    }
	    }
	    
	    // if user has content.modify, user can revise metadata, revise content, and/or replace content
	    if(permissions.contains(ContentPermissions.REVISE))
	    {
		    List<ResourceToolAction> contentModifyActions = typeDef.getActions(CONTENT_MODIFY_ACTIONS);
		    if(contentModifyActions != null && !"/".equals(selectedItem.getId()))
		    {
		    	actions.addAll(contentModifyActions);
		    }
	    }
	    else if(permissions.contains(ContentPermissions.READ))
	    {
		    List<ResourceToolAction> contentPropertiesActions = typeDef.getActions(CONTENT_PROPERTIES_ACTIONS);
		    if(contentPropertiesActions != null)
		    {
		    	actions.addAll(contentPropertiesActions);
		    }
	    }
	    
	    // if user has content.delete, user can move item or delete item
	    if(permissions.contains(ContentPermissions.DELETE))
	    {
		    List<ResourceToolAction> contentDeleteActions = typeDef.getActions(CONTENT_DELETE_ACTIONS);
		    if(contentDeleteActions != null)
		    {
		    	actions.addAll(contentDeleteActions);
		    }
	    }
	    
	    // if user has content.new for item's parent and content.read for item, user can duplicate item
	    if(permissions.contains(ContentPermissions.CREATE))
	    {
		    List<ResourceToolAction> contentNewOnParentActions = typeDef.getActions(CONTENT_NEW_FOR_PARENT_ACTIONS);
		    if(contentNewOnParentActions != null)
		    {
		    	actions.addAll(contentNewOnParentActions);
		    }
	    }
	    
	    // if user has content.new for item's parent and content.read for item, user can duplicate item
	    if(permissions.contains(ContentPermissions.SITE_UPDATE))
	    {
		    List<ResourceToolAction> folderPermissionsActions = typeDef.getActions(SITE_UPDATE_ACTIONS);
		    if(folderPermissionsActions != null)
		    {
		    	actions.addAll(folderPermissionsActions);
		    }
	    }
	    
	    // filter -- remove actions that are not available to the current user in the context of this item
	    Iterator<ResourceToolAction> actionIt = actions.iterator();
	    while(actionIt.hasNext())
	    {
	    	ResourceToolAction action = actionIt.next();
	    	if(! action.available(selectedItem) )
	    	{
	    		actionIt.remove();
	    	}
	    }
	    return actions;
    }

	/**
     * @param selectedItem
     * @param registry
     * @return
     */
    protected static ResourceType getResourceType(ContentEntity selectedItem, ResourceTypeRegistry registry)
    {
		log.debug("ResourcesAction.getResourceType()");
	    String resourceType = selectedItem.getResourceType();
	    if(resourceType == null)
	    {
	    	if(selectedItem.isCollection())
	    	{
	    		resourceType = ResourceType.TYPE_FOLDER;
	    	}
		    else 
		    {
		    	resourceType = ResourceType.TYPE_UPLOAD;
		    }
	    }
	    
	    // get the registration for the current item's type 
	    ResourceType typeDef = registry.getType(resourceType);
	    return typeDef;
    }
	
    public static List<ResourceToolAction> getPasteActions(ContentEntity selectedItem, Set<ContentPermissions> permissions, ResourceTypeRegistry registry, List<String> items_to_be_moved, List<String> items_to_be_copied)
    {
		log.debug("ResourcesAction.getPasteActions()");
	    List<ResourceToolAction> actions = new ArrayList<>();
	    
	    // if nothing to paste, just return an empty list
    	if((items_to_be_moved == null || items_to_be_moved.isEmpty()) && (items_to_be_copied == null || items_to_be_copied.isEmpty()))
    	{
    		return actions;
    	}
    	
	    Reference ref = entityManager.newReference(selectedItem.getReference());
	    	    
	    Set<String> memberIds = new TreeSet<>();
	    if(permissions.contains(ContentPermissions.CREATE))
	    {
	    	if(selectedItem.isCollection())
	    	{
		    	memberIds.addAll(((ContentCollection) selectedItem).getMembers());
	    	}
	    	
		    // get the registration for the current item's type 
		    ResourceType typeDef = getResourceType(selectedItem, registry);
		    
		    if(items_to_be_moved != null && ! items_to_be_moved.isEmpty())
		    {
		    	// check items_to_be_moved to ensure there's at least one item that can be pasted here (SAK-9837)
	    		String slash1 = selectedItem.getId().endsWith("/") ? "" : "/";
		    	boolean movable = false;
		    	for(String itemId : items_to_be_moved)
		    	{
		    		if(! itemId.equals(selectedItem.getId()))
		    		{
		    			if(itemId.endsWith("/"))
		    			{
		    				String name = isolateName(itemId) + "/";
				    		if(! memberIds.contains(selectedItem.getId() + slash1 + name))
				    		{
				    			movable = true;
				    			break;
				    		}
		    			}
			    		else
			    		{
			    			movable = true;
			    			break;
			    		}
		    		}
		    	}
		    	
		    	List<ResourceToolAction> conditionalContentNewActions = typeDef.getActions(PASTE_MOVED_ACTIONS);
		    	if(movable && conditionalContentNewActions != null)
		    	{
		    		actions.addAll(conditionalContentNewActions);
		    	}
		    }
	
		    if(items_to_be_copied != null && ! items_to_be_copied.isEmpty())
		    {
		    	// check items_to_be_copied to ensure there's at least one item that can be pasted here (SAK-9837)
	    		String slash1 = selectedItem.getId().endsWith("/") ? "" : "/";
		    	boolean copyable = false;
		    	for(String itemId : items_to_be_copied)
		    	{
		    		if(! itemId.equals(selectedItem.getId()))
		    		{
		    			if(itemId.endsWith("/"))
		    			{
		    				String name = isolateName(itemId) + "/";
				    		if(! memberIds.contains(selectedItem.getId() + slash1 + name))
				    		{
				    			copyable = true;
				    			break;
				    		}
		    			}
			    		else
			    		{
			    			copyable = true;
			    			break;
			    		}
		    		}
	    		}
		    	
		    	List<ResourceToolAction> conditionalContentNewActions = typeDef.getActions(PASTE_COPIED_ACTIONS);
		    	if(copyable && conditionalContentNewActions != null)
		    	{
		    		actions.addAll(conditionalContentNewActions);
		    	}
		    }
	    }
	    
	    // filter -- remove actions that are not available to the current user in the context of this item
	    Iterator<ResourceToolAction> actionIt = actions.iterator();
	    while(actionIt.hasNext())
	    {
	    	ResourceToolAction action = actionIt.next();
			if(! action.available(selectedItem))
	    	{
	    		actionIt.remove();
	    	}
	    }

	    return actions; 
	    
    }
	
	/**
     * @param selectedItem
	 * @param permissions TODO
	 * @param registry
	 * @return
     */
    protected static List<ResourceToolAction> getAddActions(ContentEntity selectedItem, Set<ContentPermissions> permissions, ResourceTypeRegistry registry)
    {
		log.debug("ResourcesAction.getAddActions()");
	    Reference ref = entityManager.newReference(selectedItem.getReference());
	    
	    List<ResourceToolAction> actions = new ArrayList<>();
	    
	    ResourceType typeDef = getResourceType(selectedItem, registry);
	    
	    if(permissions.contains(ContentPermissions.CREATE))
	    {		    
		    // certain actions are defined elsewhere but pertain only to ExpandableResourceTypes (collections)
		    if(typeDef != null && typeDef.isExpandable())
		    {
		    	ExpandableResourceType expTypeDef = (ExpandableResourceType) typeDef;
		    	
		    	// if item is collection and user has content.new for item, user may be able to create new items in the collection 
		    	{
		    		// iterate over resource-types and get all the registered types and find actions requiring "content.new" permission
		    		Collection<ResourceType> types = registry.getTypes(ref.getContext());
		    		Iterator<ActionType> actionTypeIt = CONTENT_NEW_ACTIONS.iterator();
		    		while(actionTypeIt.hasNext())
		    		{
		    			ActionType actionType = actionTypeIt.next();
		    			Iterator<ResourceType> typeIt = types.iterator();
		    			while(typeIt.hasNext())
		    			{
		    				ResourceType type = (ResourceType) typeIt.next();
		    				
		    				List<ResourceToolAction> createActions = type.getActions(actionType);
		    				if(createActions != null)
		    				{
		    					actions.addAll(createActions);
		    				}
		    			}
		    		}
		    	}
		    	
			    // filter -- remove actions that are not available to the current user in the context of this item.
		    	// A registered action can restrict itself based on the context.
		    	// The type registration for the container can restrict what can be created within it.
			    Iterator<ResourceToolAction> actionIt = actions.iterator();
			    while(actionIt.hasNext())
			    {
			    	ResourceToolAction action = actionIt.next();
					if(! action.available(selectedItem) || ! expTypeDef.allowAddAction(action, selectedItem))
			    	{
			    		actionIt.remove();
			    	}
			    }
		    }

	    }
	    
	    return actions;
    }
	
	/**
	 * @param state
	 * @return
	 */
	public static List getCollectionPath(SessionState state)
	{
		log.debug("ResourcesAction.getCollectionPath()");
		//org.sakaiproject.content.api.contentHostingService contentService = (org.sakaiproject.content.api.contentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		ContentHostingService contentService = contentHostingService;
		// make sure the channedId is set
		String currentCollectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);

		LinkedList collectionPath = new LinkedList();

		String previousCollectionId = "";
		List pathitems = new ArrayList();
		while ((currentCollectionId != null) && (!currentCollectionId.equals(navRoot)) && (!currentCollectionId.equals(previousCollectionId)) 
				&& !(ContentHostingService.ROOT_COLLECTIONS.contains(currentCollectionId)) && (!contentService.isRootCollection(previousCollectionId)))
		{
			pathitems.add(currentCollectionId);
			previousCollectionId = currentCollectionId;
			currentCollectionId = contentService.getContainingCollectionId(currentCollectionId);
			if (previousCollectionId != null && previousCollectionId.startsWith(ContentHostingService.COLLECTION_DROPBOX))
			{
				String[] parts = previousCollectionId.split("/");
				// "/group-user/siteId/" parts: "", "group-user", "siteId", ""
				if (parts.length == 3 || (parts.length == 4 && StringUtils.isEmpty(parts[3])))
				{
					// This is the site dropbox; proceed no higher.
					break;
				}
			}
		}
		
		if (!ContentHostingService.COLLECTION_DROPBOX.equals(currentCollectionId))
		{
			if(navRoot != null && (pathitems.isEmpty() || (! navRoot.equals(previousCollectionId) && ! navRoot.equals(currentCollectionId))))
			{
				pathitems.add(navRoot);

			}
			if(homeCollectionId != null && (pathitems.isEmpty() || (!homeCollectionId.equals(navRoot) && ! homeCollectionId.equals(previousCollectionId) && ! homeCollectionId.equals(currentCollectionId))))
			{
				pathitems.add(homeCollectionId);
			}
		}

		Iterator items = pathitems.iterator();
		while(items.hasNext())
		{
			String id = (String) items.next();
			try
			{
				ResourceProperties props = contentService.getProperties(id);
				String name = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				String containingCollectionId = contentService.getContainingCollectionId(id);
				if(ContentHostingService.COLLECTION_DROPBOX.equals(containingCollectionId))
				{
					Reference ref = entityManager.newReference(contentService.getReference(id));
					Site site = siteService.getSite(ref.getContext());
					String[] args = {site.getTitle()};
					name = trb.getFormattedMessage("title.dropbox", (Object[]) args);
				}
				else if(ContentHostingService.COLLECTION_SITE.equals(containingCollectionId))
				{
					Reference ref = entityManager.newReference(contentService.getReference(id));
					Site site = siteService.getSite(ref.getContext());
					String[] args = {site.getTitle()};
					name = trb.getFormattedMessage("title.resources", (Object[]) args);
				}
				
				ChefPathItem item = new ChefPathItem(id, name);

				boolean canRead = contentService.allowGetCollection(id) || contentService.allowGetResource(id);
				item.setCanRead(canRead);

				if(canRead)
				{
					String url = contentService.getUrl(id);
					item.setUrl(url);
				}

				item.setLast(collectionPath.isEmpty());
				if(id.equals(homeCollectionId))
				{
					item.setRoot(homeCollectionId);
				}
				else
				{
					item.setRoot(navRoot);
				}

				try
				{
					boolean isFolder = props.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);
					item.setIsFolder(isFolder);
				}
				catch (EntityPropertyNotDefinedException e1)
				{
				}
				catch (EntityPropertyTypeException e1)
				{
				}

				collectionPath.addFirst(item);

			}
			catch (PermissionException e)
			{
			}
			catch (IdUnusedException e)
			{
			}
		}
		return collectionPath;
	}
	
	public static ResourcesEditItem getEditItem(String id, String collectionId, RunData data)
	{
		log.debug("ResourcesAction.getEditItem()");
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);

		Map current_stack_frame = peekAtStack(state);

		ResourcesEditItem item = null;

		// populate an ResourcesEditItem object with values from the resource and return the ResourcesEditItem
		try
		{
			ResourceProperties properties = contentHostingService.getProperties(id);

			boolean isCollection = false;
			try
			{
				isCollection = properties.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);
			}
			catch(Exception e)
			{
				// assume isCollection is false if property is not set
			}

			ContentEntity entity;
			String itemType;
			byte[] content = null;
			if(isCollection)
			{
				itemType = "folder";
				entity = contentHostingService.getCollection(id);
			}
			else
			{
				entity = contentHostingService.getResource(id);
				itemType = ((ContentResource) entity).getContentType();
				content = ((ContentResource) entity).getContent();
			}

			String itemName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

			item = new ResourcesEditItem(id, itemName, itemType);
			
			item.setInDropbox(contentHostingService.isInDropbox(id));
			boolean isUserSite = false;
			String refstr = entity.getReference();
			Reference ref = entityManager.newReference(refstr);
			String contextId = ref.getContext();
			if(contextId != null)
			{
				isUserSite = siteService.isUserSite(contextId);
			}
			item.setInWorkspace(isUserSite);
			
			BasicRightsAssignment rightsObj = new BasicRightsAssignment(item.getItemNum(), properties);
			item.setRights(rightsObj);

			String encoding = data.getRequest().getCharacterEncoding();
			if(encoding != null)
			{
				item.setEncoding(encoding);
			}

			if(content != null)
			{
				item.setContent(content);
			}

			String dummyId = collectionId.trim();
			if(dummyId.endsWith(Entity.SEPARATOR))
			{
				dummyId += "dummy";
			}
			else
			{
				dummyId += Entity.SEPARATOR + "dummy";
			}

			String containerId = contentHostingService.getContainingCollectionId (id);
			item.setContainer(containerId);

			boolean canRead = contentHostingService.allowGetCollection(id);
			boolean canAddFolder = contentHostingService.allowAddCollection(id);
			boolean canAddItem = contentHostingService.allowAddResource(id);
			boolean canDelete = contentHostingService.allowRemoveResource(id);
			boolean canRevise = contentHostingService.allowUpdateResource(id);
			item.setCanRead(canRead);
			item.setCanRevise(canRevise);
			item.setCanAddItem(canAddItem);
			item.setCanAddFolder(canAddFolder);
			item.setCanDelete(canDelete);
			// item.setIsUrl(isUrl);
			
			AccessMode access = ((GroupAwareEntity) entity).getAccess();
			if(access == null)
			{
				item.setAccess(AccessMode.INHERITED.toString());
			}
			else
			{
				item.setAccess(access.toString());
			}

			AccessMode inherited_access = ((GroupAwareEntity) entity).getInheritedAccess();
			if(inherited_access == null || inherited_access.equals(AccessMode.SITE))
			{
				item.setInheritedAccess(AccessMode.INHERITED.toString());
			}
			else
			{
				item.setInheritedAccess(inherited_access.toString());
			}
			
			Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
			Collection<Group> site_groups = site.getGroups();
			item.setAllSiteGroups(site_groups);
			
			List access_groups = new ArrayList(((GroupAwareEntity) entity).getGroups());
			item.setEntityGroupRefs(access_groups);

			List inherited_access_groups = new ArrayList(((GroupAwareEntity) entity).getInheritedGroups());
			item.setInheritedGroupRefs(inherited_access_groups);
			
			Collection allowedRemoveGroups;
			if(AccessMode.GROUPED == access)
			{
				allowedRemoveGroups = contentHostingService.getGroupsWithRemovePermission(id);
				Collection more = contentHostingService.getGroupsWithRemovePermission(collectionId);
				if(more != null && ! more.isEmpty())
				{
					allowedRemoveGroups.addAll(more);
				}
			}
			else if(AccessMode.GROUPED == inherited_access)
			{
				allowedRemoveGroups = contentHostingService.getGroupsWithRemovePermission(collectionId);
			}
			else
			{
				allowedRemoveGroups = contentHostingService.getGroupsWithRemovePermission(contentHostingService.getSiteCollection(site.getId()));
			}
			item.setAllowedRemoveGroupRefs(allowedRemoveGroups);
			
			Collection allowedAddGroups;
			if(AccessMode.GROUPED == access)
			{
				allowedAddGroups = contentHostingService.getGroupsWithAddPermission(id);
				Collection more = contentHostingService.getGroupsWithAddPermission(collectionId);
				if(more != null && ! more.isEmpty())
				{
					allowedAddGroups.addAll(more);
				}
			}
			else if(AccessMode.GROUPED == inherited_access)
			{
				allowedAddGroups = contentHostingService.getGroupsWithAddPermission(collectionId);
			}
			else
			{
				allowedAddGroups = contentHostingService.getGroupsWithAddPermission(contentHostingService.getSiteCollection(site.getId()));
			}
			item.setAllowedAddGroupRefs(allowedAddGroups);
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			if(preventPublicDisplay)
			{
				item.setPubviewPossible(false);
				item.setPubviewInherited(false);
				item.setPubview(false);
			}
			else
			{
				item.setPubviewPossible(true);
				// find out about pubview
				boolean pubviewset = contentHostingService.isInheritingPubView(id);
				item.setPubviewInherited(pubviewset);
				boolean pubview = pubviewset;
				if (!pubviewset) 
				{
					pubview = contentHostingService.isPubView(id);
					item.setPubview(pubview);
				}
			}

			if(entity.isHidden())
			{
				item.setHidden(true);
				//item.setReleaseDate(null);
				//item.setRetractDate(null);
			}
			else
			{
				item.setHidden(false);
				Time releaseDate = entity.getReleaseDate();
				if(releaseDate == null)
				{
					item.setUseReleaseDate(false);
					item.setReleaseDate(timeService.newTime());
				}
				else
				{
					item.setUseReleaseDate(true);
					item.setReleaseDate(releaseDate);
				}
				Time retractDate = entity.getRetractDate();
				if(retractDate == null)
				{
					item.setUseRetractDate(false);
					Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
					if(defaultRetractDate == null)
					{
						defaultRetractDate = timeService.newTime();
						state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
					}
					item.setRetractDate(defaultRetractDate);
				}
				else
				{
					item.setUseRetractDate(true);
					item.setRetractDate(retractDate);
				}
			}

			if(item.isUrl())
			{
				String url = new String(content);
				item.setFilename(url);
			}
			else if(item.isHtml() || item.isPlaintext() || item.isFileUpload())
			{
				String filename = properties.getProperty(ResourceProperties.PROP_ORIGINAL_FILENAME);
				if(filename == null)
				{
					// this is a hack to deal with the fact that original filenames were not saved for some time.
					if(containerId != null && item.getId().startsWith(containerId) && containerId.length() < item.getId().length())
					{
						filename = item.getId().substring(containerId.length());
					}
				}

				if(filename == null)
				{
					item.setFilename(itemName);
				}
				else
				{
					item.setFilename(filename);
				}
			}

			String description = properties.getProperty(ResourceProperties.PROP_DESCRIPTION);
			item.setDescription(description);

			try
			{
				Time creTime = properties.getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
				String createdTime = creTime.toStringLocalShortDate() + " " + creTime.toStringLocalShort();
				item.setCreatedTime(createdTime);
			}
			catch(Exception e)
			{
				String createdTime = properties.getProperty(ResourceProperties.PROP_CREATION_DATE);
				item.setCreatedTime(createdTime);
			}
			try
			{
				String createdBy = getUserProperty(properties, ResourceProperties.PROP_CREATOR).getDisplayName();
				item.setCreatedBy(createdBy);
			}
			catch(Exception e)
			{
				String createdBy = properties.getProperty(ResourceProperties.PROP_CREATOR);
				item.setCreatedBy(createdBy);
			}
			try
			{
				Time modTime = properties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
				String modifiedTime = modTime.toStringLocalShortDate() + " " + modTime.toStringLocalShort();
				item.setModifiedTime(modifiedTime);
			}
			catch(Exception e)
			{
				String modifiedTime = properties.getProperty(ResourceProperties.PROP_MODIFIED_DATE);
				item.setModifiedTime(modifiedTime);
			}
			try
			{
				String modifiedBy = getUserProperty(properties, ResourceProperties.PROP_MODIFIED_BY).getDisplayName();
				item.setModifiedBy(modifiedBy);
			}
			catch(Exception e)
			{
				String modifiedBy = properties.getProperty(ResourceProperties.PROP_MODIFIED_BY);
				item.setModifiedBy(modifiedBy);
			}

			String url = contentHostingService.getUrl(id);
			item.setUrl(url);

			String size = "";
			if(properties.getProperty(ResourceProperties.PROP_CONTENT_LENGTH) != null)
			{
				long size_long = 0;
                try
                {
	                size_long = properties.getLongProperty(ResourceProperties.PROP_CONTENT_LENGTH);
                }
                catch (EntityPropertyNotDefinedException e)
                {
	                // TODO Auto-generated catch block
	                log.warn("EntityPropertyNotDefinedException for size of {}", item.getId());
                }
                catch (EntityPropertyTypeException e)
                {
	                // TODO Auto-generated catch block
	                log.warn("EntityPropertyTypeException for size of {}", item.getId());
                }
				size = getFileSizeString(size_long, rb);
			}
			item.setSize(size);

			String copyrightStatus = properties.getProperty(properties.getNamePropCopyrightChoice());
			item.setCopyrightStatus(copyrightStatus);
			String copyrightInfo = properties.getPropertyFormatted(properties.getNamePropCopyright());
			item.setCopyrightInfo(copyrightInfo);
			String copyrightAlert = properties.getProperty(properties.getNamePropCopyrightAlert());

			if("true".equalsIgnoreCase(copyrightAlert))
			{
				item.setCopyrightAlert(true);
			}
			else
			{
				item.setCopyrightAlert(false);
			}
			
			log.info("here we are!");
			
			// for collections only
			if(item.isFolder())
			{
				// setup for quota - ADMIN only, site-root collection only
				if (securityService.isSuperUser())
				{
					item.setIsAdmin(true);
					
					String siteCollectionId = contentHostingService.getSiteCollection(contextId);
					String dropBoxCollectionId = ContentHostingService.COLLECTION_DROPBOX + contextId + Entity.SEPARATOR;
					if(siteCollectionId.equals(entity.getId()) || (entity.getId().startsWith(dropBoxCollectionId) && entity.getId().split(Entity.SEPARATOR).length<=4))
					{
						item.setCanSetQuota(true);
						try
						{
							long quota = properties.getLongProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
							item.setHasQuota(true);
							item.setQuota(Long.toString(quota));
					
						}
						catch (Exception any)
						{
							log.debug("got exception: ", any);
						}
					}
					
					
				}
			}

		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("notexist1"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getFormattedMessage("notpermis2", new Object[] {id}));
		}
		catch(TypeException e)
		{
			addAlert(state," " + rb.getFormattedMessage("typeex", new Object[] {id}));
		}
		catch(ServerOverloadException e)
		{
			// this represents temporary unavailability of server's filesystem
			// for server configured to save resource body in filesystem
			addAlert(state, rb.getString("failed"));
		}
		catch(RuntimeException e)
		{
			log.debug("ResourcesAction.getEditItem ***** Unknown Exception ***** {}", e.getMessage());
			addAlert(state, rb.getString("failed"));
		}

		return item;

	}

	/**
	 * @param size_long
	 * @param rl 
	 * @return
	 */
	public static String getFileSizeString(long size_long, ResourceLoader rl) 
	{
		log.debug("ResourcesAction.getFileSizeString()");
		String size;
		NumberFormat formatter = NumberFormat.getInstance(rl.getLocale());
		formatter.setMaximumFractionDigits(1);
		if(size_long > 700000000L)
		{
			String[] args = { formatter.format(1.0 * size_long / (1024L * 1024L * 1024L)) };
			size = rl.getFormattedMessage("size.gb", (Object[]) args);
		}
		else if(size_long > 700000L)
		{
			String[] args = { formatter.format(1.0 * size_long / (1024L * 1024L)) };
			size = rl.getFormattedMessage("size.mb", (Object[]) args);
		}
		else if(size_long > 700L)
		{
			String[] args = { formatter.format(1.0 * size_long / 1024L) };
			size = rl.getFormattedMessage("size.kb", (Object[]) args);
		}
		else 
		{
			String[] args = { formatter.format(size_long) };
			size = rl.getFormattedMessage("size.bytes", (Object[]) args);
		}
		return size;
	}

	/**
	 * 
	 * Get the items in this folder that should be seen.
	 * @param collectionId - String version of
	 * @param highlightedItems - Set of highlighted items from UI
	 * @param parent - The folder containing this item
	 * @param isLocal - true if navigation root and home collection id of site are the same, false otherwise
	 * @param state - The session state
	 */
	protected List<ResourcesBrowseItem> getListView(String collectionId, Set highlightedItems, ResourcesBrowseItem parent, boolean isLocal, SessionState state)
	{
		log.debug("ResourcesAction.getListView()");
		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

		boolean need_to_expand_all = Boolean.TRUE.toString().equals((String)state.getAttribute(STATE_NEED_TO_EXPAND_ALL));
		
		Comparator userSelectedSort = (Comparator) state.getAttribute(STATE_LIST_VIEW_SORT);
		
		Map expandedFolderSortMap = setStateAttributeExpandedFolderSortMap(state);
		
		Set<String> expandedCollections = getExpandedCollections(state);

		List<ResourcesBrowseItem> newItems = new LinkedList<>();
		try
		{
			// get the collection
			// try using existing resource first
			ContentCollection collection;

			// get the collection
			collection = contentService.getCollection(collectionId);
			if(need_to_expand_all || expandedCollections.contains(collectionId))
			{
				Comparator comparator;
				if(userSelectedSort != null)
				{
					comparator = userSelectedSort;
				}
				else
				{
					boolean hasCustomSort = false;
					try
					{
						hasCustomSort = collection.getProperties().getBooleanProperty(ResourceProperties.PROP_HAS_CUSTOM_SORT);
					}
					catch(Exception e)
					{
						// ignore -- let value be false
					}
					if(hasCustomSort)
					{
						comparator = ListItem.PRIORITY_SORT_COMPARATOR;
					}
					else
					{
						comparator = ListItem.DEFAULT_COMPARATOR;
					}
				}
				expandedCollections.add(collectionId);
				expandedFolderSortMap.put(collectionId, comparator);
			}

			String dummyId = collectionId.trim();
			if(dummyId.endsWith(Entity.SEPARATOR))
			{
				dummyId += "dummy";
			}
			else
			{
				dummyId += Entity.SEPARATOR + "dummy";
			}

			boolean canRead;
			boolean canDelete;
			boolean canRevise;
			boolean canAddFolder;
			boolean canAddItem;
			boolean canUpdate;
			int depth = 0;

			if(parent == null || ! parent.canRead())
			{
				canRead = contentService.allowGetCollection(collectionId);
			}
			else
			{
				canRead = parent.canRead();
			}
			if(parent == null || ! parent.canDelete())
			{
				canDelete = contentService.allowRemoveCollection(collectionId);
			}
			else
			{
				canDelete = parent.canDelete();
			}
			if(parent == null || ! parent.canRevise())
			{
				canRevise = contentService.allowUpdateCollection(collectionId);
			}
			else
			{
				canRevise = parent.canRevise();
			}
			if(parent == null || ! parent.canAddFolder())
			{
				canAddFolder = contentService.allowAddCollection(dummyId);
			}
			else
			{
				canAddFolder = parent.canAddFolder();
			}
			if(parent == null || ! parent.canAddItem())
			{
				canAddItem = contentService.allowAddResource(dummyId);
			}
			else
			{
				canAddItem = parent.canAddItem();
			}
			if(parent == null || ! parent.canUpdate())
			{
				canUpdate = authzGroupService.allowUpdate(collectionId);
			}
			else
			{
				canUpdate = parent.canUpdate();
			}
			if(parent != null)
			{
				depth = parent.getDepth() + 1;
			}

			if(canAddItem)
			{
				state.setAttribute(STATE_PASTE_ALLOWED_FLAG, Boolean.TRUE.toString());
			}
			// each child will have it's own delete status based on: delete.own or delete.any
			boolean hasDeletableChildren = true; 
         
			// may have perms to copy in another folder, even if no perms in this folder
			boolean hasCopyableChildren = canRead; 

			String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

			ResourceProperties cProperties = collection.getProperties();
			String folderName = cProperties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			if(collectionId.equals(homeCollectionId))
			{
				folderName = (String) state.getAttribute(STATE_HOME_COLLECTION_DISPLAY_NAME);
			}
			ResourcesBrowseItem folder = new ResourcesBrowseItem(collectionId, folderName, "folder");
			if(parent == null)
			{
				folder.setRoot(collectionId);
			}
			else
			{
				folder.setRoot(parent.getRoot());
			}
			
			boolean isInDropbox = contentService.isInDropbox(collectionId);
			folder.setInDropbox(isInDropbox);
			
			BasicRightsAssignment rightsObj = new BasicRightsAssignment(folder.getItemNum(), cProperties);
			folder.setRights(rightsObj);
			
			AccessMode access = collection.getAccess();
			if(access == null || AccessMode.SITE == access)
			{
				folder.setAccess(AccessMode.INHERITED.toString());
			}
			else
			{
				folder.setAccess(access.toString());
			}
			
			AccessMode inherited_access = collection.getInheritedAccess();
			if(inherited_access == null || AccessMode.SITE == inherited_access)
			{
				folder.setInheritedAccess(AccessMode.INHERITED.toString());
			}
			else
			{
				folder.setInheritedAccess(inherited_access.toString());
			}
			
			Collection access_groups = collection.getGroupObjects();
			if(access_groups == null)
			{
				access_groups = new ArrayList();
			}
			folder.setGroups(access_groups);
			Collection inherited_access_groups = collection.getInheritedGroupObjects();
			if(inherited_access_groups == null)
			{
				inherited_access_groups = new ArrayList();
			}
			folder.setInheritedGroups(inherited_access_groups);
			
			if(parent != null && (parent.isPubview() || parent.isPubviewInherited()))
			{
				folder.setPubviewInherited(true);
				folder.setPubview(false);
			}
			else if(contentService.isPubView(folder.getId()))
			{
				folder.setPubview(true);
			}

			if(highlightedItems == null || highlightedItems.isEmpty())
			{
				// do nothing
			}
			else if(parent != null && parent.isHighlighted())
			{
				folder.setInheritsHighlight(true);
				folder.setHighlighted(true);
			}
			else if(highlightedItems.contains(collectionId))
			{
				folder.setHighlighted(true);
				folder.setInheritsHighlight(false);
			}

			String containerId = contentService.getContainingCollectionId (collectionId);
			folder.setContainer(containerId);

			folder.setCanRead(canRead);
			folder.setCanRevise(canRevise);
			folder.setCanAddItem(canAddItem);
			folder.setCanAddFolder(canAddFolder);
			folder.setCanDelete(canDelete);
			folder.setCanUpdate(canUpdate);
			
			folder.setAvailable(collection.isAvailable());

			try
			{
				Time createdTime = cProperties.getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
				String createdTimeString = createdTime.toStringLocalShortDate();
				folder.setCreatedTime(createdTimeString);
			}
			catch(Exception e)
			{
				String createdTimeString = cProperties.getProperty(ResourceProperties.PROP_CREATION_DATE);
				folder.setCreatedTime(createdTimeString);
			}
			try
			{
				String createdBy = getUserProperty(cProperties, ResourceProperties.PROP_CREATOR).getDisplayName();
				folder.setCreatedBy(createdBy);
			}
			catch(Exception e)
			{
				String createdBy = cProperties.getProperty(ResourceProperties.PROP_CREATOR);
				folder.setCreatedBy(createdBy);
			}
			try
			{
				Time modifiedTime = cProperties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
				String modifiedTimeString = modifiedTime.toStringLocalShortDate();
				folder.setModifiedTime(modifiedTimeString);
			}
			catch(Exception e)
			{
				String modifiedTimeString = cProperties.getProperty(ResourceProperties.PROP_MODIFIED_DATE);
				folder.setModifiedTime(modifiedTimeString);
			}
			try
			{
				String modifiedBy = getUserProperty(cProperties, ResourceProperties.PROP_MODIFIED_BY).getDisplayName();
				folder.setModifiedBy(modifiedBy);
			}
			catch(Exception e)
			{
				String modifiedBy = cProperties.getProperty(ResourceProperties.PROP_MODIFIED_BY);
				folder.setModifiedBy(modifiedBy);
			}

			String url = contentService.getUrl(collectionId);
			folder.setUrl(url);
			
			// get the "size' of the collection, meaning the number of members one level down
			int collection_size = collection.getMemberCount(); // newMembers.size();
			folder.setIsEmpty(collection_size < 1);
			folder.setSortable(contentHostingService.isSortByPriorityEnabled() && collection_size > 1 && collection_size < EXPANDABLE_FOLDER_SIZE_LIMIT);
			Integer expansionLimit = (Integer) state.getAttribute(STATE_EXPANDABLE_FOLDER_SIZE_LIMIT);
			if(expansionLimit == null)
			{
				expansionLimit = EXPANDABLE_FOLDER_SIZE_LIMIT;
			}
			folder.setIsTooBig(collection_size > expansionLimit);
				
			folder.setDepth(depth);
			newItems.add(folder);

			if(need_to_expand_all || expandedFolderSortMap.keySet().contains(collectionId))
			{
				// Get the collection members from the 'new' collection
				List newMembers = collection.getMemberResources();

				Comparator comparator = userSelectedSort;
				if(comparator == null)
				{
					comparator = (Comparator) expandedFolderSortMap.get(collectionId);
					if(comparator == null)
					{
						comparator = ListItem.DEFAULT_COMPARATOR;
					}
				}

				Collections.sort(newMembers, comparator);

				// loop thru the (possibly) new members and add to the list
				Iterator it = newMembers.iterator();
				while(it.hasNext())
				{
					ContentEntity resource = (ContentEntity) it.next();
					ResourceProperties props = resource.getProperties();

					String itemId = resource.getId();
					
					if(contentService.isAvailabilityEnabled() && ! contentService.isAvailable(itemId))
					{
						continue;
					}

					if(resource.isCollection())
					{
						List offspring = getListView(itemId, highlightedItems, folder, isLocal, state);

						if(! offspring.isEmpty())
						{
							ResourcesBrowseItem child = (ResourcesBrowseItem) offspring.get(0);
							hasDeletableChildren = hasDeletableChildren || child.hasDeletableChildren();
							hasCopyableChildren = hasCopyableChildren || child.hasCopyableChildren();
						}

						// add all the items in the subfolder to newItems
						newItems.addAll(offspring);
					}
					else
					{
						AccessMode access_mode = ((GroupAwareEntity) resource).getAccess();
						if(access_mode == null)
						{
							access_mode = AccessMode.INHERITED;
						}
						else if(access_mode == AccessMode.GROUPED)
						{
							if(! contentService.allowGetResource(resource.getId()))
							{
								continue;
							}
						}
						
						String itemType = ((ContentResource)resource).getContentType();
						String itemName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						ResourcesBrowseItem newItem = new ResourcesBrowseItem(itemId, itemName, itemType);
						
						boolean isLocked = contentService.isLocked(itemId);
						newItem.setLocked(isLocked);
						
						boolean isAvailable = folder.isAvailable();
						if(isAvailable)
						{
							isAvailable = resource.isAvailable();
						}
						newItem.setAvailable(isAvailable);

						newItem.setAccess(access_mode.toString());
						newItem.setInheritedAccess(folder.getEffectiveAccess());

						newItem.setInDropbox(isInDropbox);
						
						BasicRightsAssignment rightsObj2 = new BasicRightsAssignment(newItem.getItemNum(), props);
						newItem.setRights(rightsObj2);
						Collection groups = ((GroupAwareEntity) resource).getGroupObjects();
						if(groups == null)
						{
							groups = new ArrayList();
						}
						Collection inheritedGroups = folder.getGroups();
						if(inheritedGroups == null || inheritedGroups.isEmpty())
						{
							inheritedGroups = folder.getInheritedGroups();
						}
						newItem.setGroups(groups);	
						newItem.setInheritedGroups(inheritedGroups);

						newItem.setContainer(collectionId);
						newItem.setRoot(folder.getRoot());

						// delete and revise permissions based on item (not parent)
						newItem.setCanDelete(contentService.allowRemoveResource(itemId) && ! isLocked);
						newItem.setCanRevise(contentService.allowUpdateResource(itemId)); 
						newItem.setCanRead(canRead);
						newItem.setCanCopy(canRead); // may have perms to copy in another folder, even if no perms in this folder
						newItem.setCanAddItem(canAddItem); // true means this user can add an item in the folder containing this item (used for "duplicate")

						if(highlightedItems == null || highlightedItems.isEmpty())
						{
							// do nothing
						}
						else if(folder.isHighlighted())
						{
							newItem.setInheritsHighlight(true);
							newItem.setHighlighted(true);
						}
						else if(highlightedItems.contains(itemId))
						{
							newItem.setHighlighted(true);
							newItem.setInheritsHighlight(false);
						}

						try
						{
							Time createdTime = props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
							String createdTimeString = createdTime.toStringLocalShortDate();
							newItem.setCreatedTime(createdTimeString);
						}
						catch(Exception e)
						{
							String createdTimeString = props.getProperty(ResourceProperties.PROP_CREATION_DATE);
							newItem.setCreatedTime(createdTimeString);
						}
						try
						{
							String createdBy = getUserProperty(props, ResourceProperties.PROP_CREATOR).getDisplayName();
							newItem.setCreatedBy(createdBy);
						}
						catch(Exception e)
						{
							String createdBy = props.getProperty(ResourceProperties.PROP_CREATOR);
							newItem.setCreatedBy(createdBy);
						}
						try
						{
							Time modifiedTime = props.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
							String modifiedTimeString = modifiedTime.toStringLocalShortDate();
							newItem.setModifiedTime(modifiedTimeString);
						}
						catch(Exception e)
						{
							String modifiedTimeString = props.getProperty(ResourceProperties.PROP_MODIFIED_DATE);
							newItem.setModifiedTime(modifiedTimeString);
						}
						try
						{
							String modifiedBy = getUserProperty(props, ResourceProperties.PROP_MODIFIED_BY).getDisplayName();
							newItem.setModifiedBy(modifiedBy);
						}
						catch(Exception e)
						{
							String modifiedBy = props.getProperty(ResourceProperties.PROP_MODIFIED_BY);
							newItem.setModifiedBy(modifiedBy);
						}

						if(folder.isPubview() || folder.isPubviewInherited())
						{
							newItem.setPubviewInherited(true);
							newItem.setPubview(false);
						}
						else if(contentService.isPubView(resource.getId()))
						{
							newItem.setPubview(true);
						}

						String size = props.getPropertyFormatted(ResourceProperties.PROP_CONTENT_LENGTH);
						newItem.setSize(size);

						String target = Validator.getResourceTarget(props.getProperty(ResourceProperties.PROP_CONTENT_TYPE));
						newItem.setTarget(target);

						String newUrl = contentService.getUrl(itemId);
						newItem.setUrl(newUrl);

						try
						{
							boolean copyrightAlert = props.getBooleanProperty(ResourceProperties.PROP_COPYRIGHT_ALERT);
							newItem.setCopyrightAlert(copyrightAlert);
						}
						catch(Exception e)
						{}
						newItem.setDepth(depth + 1);
						newItems.add(newItem);

					}
				}

			}
			folder.seDeletableChildren(hasDeletableChildren);
			folder.setCopyableChildren(hasCopyableChildren);
			// return newItems;
		}
		catch (IdUnusedException ignore)
		{
			// this condition indicates a site that does not have a resources collection (mercury?)
		}
		catch (TypeException e)
		{
			addAlert(state, "TypeException.");
		}
		catch (PermissionException e)
		{
			// ignore -- we'll just skip this collection since user lacks permission to access it.
			//addAlert(state, "PermissionException");
		}

		return newItems;

	}	// getListView
	
	/**
     * @param id
	 * @param inheritedPermissions TODO
	 * @return
	 */
	protected static Collection<ContentPermissions> getPermissions(String id, Collection<ContentPermissions> inheritedPermissions)
	{
		// determine the site id
		String siteId = null;
		if (StringUtils.startsWith(id, ContentHostingService.COLLECTION_SITE)) {
			siteId = ArrayUtils.get(StringUtils.split(id, Entity.SEPARATOR), 1);
		}

		if (siteId == null) {
			Reference ref = entityManager.newReference(id);
			if (ref != null) {
				siteId = ref.getContext();
			}
		}

		if (siteId == null && toolManager.getCurrentPlacement() != null) {
			siteId = toolManager.getCurrentPlacement().getContext();
		}

		log.debug("get permissions for id [{}] in context/site [{}]", id, siteId);

		Collection<ContentPermissions> permissions = new ArrayList<>();
		if(contentHostingService.isCollection(id))
		{
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.CREATE)) || contentHostingService.allowAddCollection(id) && !contentHostingService.isRootCollection(id))
			{
				permissions.add(ContentPermissions.CREATE);
			}
			if(contentHostingService.allowRemoveCollection(id))
			{
				permissions.add(ContentPermissions.DELETE);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.READ)) || contentHostingService.allowGetCollection(id))
			{
				permissions.add(ContentPermissions.READ);
			}
			if(contentHostingService.allowUpdateCollection(id))
			{
				permissions.add(ContentPermissions.REVISE);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.SITE_UPDATE)) || siteService.allowUpdateSite(siteId))
			{
				permissions.add(ContentPermissions.SITE_UPDATE);
			}
		}
		else
		{
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.CREATE)) || contentHostingService.allowAddResource(id) && !contentHostingService.isRootCollection(id))
			{
				permissions.add(ContentPermissions.CREATE);
			}
			if(contentHostingService.allowRemoveResource(id))
			{
				permissions.add(ContentPermissions.DELETE);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.READ)) || contentHostingService.allowGetResource(id))
			{
				permissions.add(ContentPermissions.READ);
			}
			if(contentHostingService.allowUpdateResource(id))
			{
				permissions.add(ContentPermissions.REVISE);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.SITE_UPDATE)) || siteService.allowUpdateSite(siteId))
			{
				permissions.add(ContentPermissions.SITE_UPDATE);
			}
		}
		
		return permissions;
	}

	protected static User getUserProperty(ResourceProperties props, String name)
	{
		log.debug("ResourcesAction.getUserProperty()");
		String id = props.getProperty(name);
		if (id != null)
		{
			try
			{
				return userDirectoryService.getUser(id);
			}
			catch (UserNotDefinedException e)
			{
			}
		}
		
		return null;
	}

	/**
	* initialize the copy context
	*/
	private static void initCopyContext (SessionState state)
	{
		log.debug("ResourcesAction.initCopyContext()");
		state.setAttribute (STATE_COPIED_IDS, new ArrayList ());

		state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());

	}	// initCopyContent

	/**
	* initialize the copy context
	*/
	private static void initMoveContext (SessionState state)
	{
		log.debug("ResourcesAction.initMoveContext()");
		state.setAttribute (STATE_MOVED_IDS, new ArrayList ());

		state.setAttribute (STATE_MOVE_FLAG, Boolean.FALSE.toString());

	}	// initCopyContent


	/**
	 * Returns true if the suspended operations stack contains no elements.
	 * @param state The current session state, including the STATE_SUSPENDED_OPERATIONS_STACK attribute.
	 * @return true if the suspended operations stack contains no elements
	 */
	private static boolean isStackEmpty(SessionState state)
	{
		log.debug("ResourcesAction.isStackEmpty()");
		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);
		if(operations_stack == null)
		{
			operations_stack = new Stack();
			state.setAttribute(STATE_SUSPENDED_OPERATIONS_STACK, operations_stack);
		}
		return operations_stack.isEmpty();
	}

	protected static List newEditItems(String collectionId, String itemtype, String encoding, boolean preventPublicDisplay, Time defaultRetractDate, int number)
	{
		log.debug("ResourcesAction.newEditItems()");
		List new_items = new ArrayList();
		
		ContentCollection collection = null;
		AccessMode inheritedAccess = AccessMode.INHERITED;
//		Collection inheritedGroups = new ArrayList();
		try
		{
			collection = contentHostingService.getCollection(collectionId);
			
			inheritedAccess = collection.getAccess();
//			inheritedGroups = collection.getGroups();
			if(AccessMode.INHERITED == inheritedAccess)
			{
				inheritedAccess = collection.getInheritedAccess();
//				inheritedGroups = collection.getInheritedGroups();
			}
		}
		catch(PermissionException e)
		{
			//alerts.add(rb.getString("notpermis4"));
			log.warn("ResourcesAction.newEditItems() PermissionException ", e);
		} 
		catch (IdUnusedException e) 
		{
			// TODO Auto-generated catch block
			log.warn("ResourcesAction.newEditItems() IdUnusedException ", e);
		} 
		catch (TypeException e) 
		{
			// TODO Auto-generated catch block
			log.warn("ResourcesAction.newEditItems() TypeException ", e);
		}
		
		boolean isUserSite = false;
		String refstr = collection.getReference();
		Reference ref = entityManager.newReference(refstr);
		String contextId = ref.getContext();
		if(contextId != null)
		{
			isUserSite = siteService.isUserSite(contextId);
		}

		boolean pubviewset = contentHostingService.isInheritingPubView(collectionId) || contentHostingService.isPubView(collectionId);
		
		
		//Collection possibleGroups = contentHostingService.getGroupsWithReadAccess(collectionId);
		boolean isInDropbox = contentHostingService.isInDropbox(collectionId);
		
		
		Site site = null;
		Collection site_groups;
		
		try 
		{
			site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
		} 
		catch (IdUnusedException e) 
		{
			log.warn("resourcesAction.newEditItems() IdUnusedException ", e);
		}
		if(site != null)
		{
			site_groups = site.getGroups();
		}
		else
		{
			site_groups = new ArrayList();
		}
				
		Collection inherited_access_groups = collection.getGroups();
		if(inherited_access_groups == null || inherited_access_groups.isEmpty())
		{
			inherited_access_groups = collection.getInheritedGroups();
		}
		if(inherited_access_groups == null)
		{
			inherited_access_groups = new ArrayList();
		}

		Collection allowedAddGroups = contentHostingService.getGroupsWithAddPermission(collectionId); // null;
		if(allowedAddGroups == null)
		{
			allowedAddGroups = new ArrayList();
		}

		for(int i = 0; i < CREATE_MAX_ITEMS; i++)
		{
			ResourcesEditItem item = new ResourcesEditItem(itemtype);
			if(encoding != null)
			{
				item.setEncoding(encoding);
			}
			item.setInDropbox(isInDropbox);

			if(inheritedAccess == null || AccessMode.SITE == inheritedAccess)
			{
				item.setInheritedAccess(AccessMode.INHERITED.toString());
			}
			else
			{
				item.setInheritedAccess(inheritedAccess.toString());
			}
			item.setAllSiteGroups(site_groups);
			item.setInheritedGroupRefs(inherited_access_groups);
			item.setAllowedAddGroupRefs(allowedAddGroups);
			
			item.setHidden(false);
			item.setUseReleaseDate(false);
			item.setReleaseDate(timeService.newTime());
			item.setUseRetractDate(false);
			item.setRetractDate(defaultRetractDate);
			item.setInWorkspace(isUserSite);

			new_items.add(item);
			// item.setPossibleGroups(new ArrayList(possibleGroups));
//			if(inheritedGroups != null)
//			{
//				item.setInheritedGroups(inheritedGroups);
//			}
			
			if(preventPublicDisplay)
			{
				item.setPubviewPossible(false);
				item.setPubviewInherited(false);
				item.setPubview(false);
			}
			else
			{
				item.setPubviewPossible(true);
				item.setPubviewInherited(pubviewset);
				//item.setPubview(pubviewset);
			}

		}

		return new_items;
	}

	/**
	 * Access the top item on the suspended-operations stack
	 * @param state The current session state, including the STATE_SUSPENDED_OPERATIONS_STACK attribute.
	 * @return The top item on the stack, or null if the stack is empty.
	 */
	private static Map peekAtStack(SessionState state)
	{
		log.debug("ResourcesAction.peekAtStack()");
		Map current_stack_frame = null;
		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);
		if(operations_stack == null)
		{
			operations_stack = new Stack();
			state.setAttribute(STATE_SUSPENDED_OPERATIONS_STACK, operations_stack);
		}
		if(! operations_stack.isEmpty())
		{
			current_stack_frame = (Map) operations_stack.peek();
		}
		return current_stack_frame;

	}

	/**
	 * Remove and return the top item from the suspended-operations stack.
	 * @param state The current session state, including the STATE_SUSPENDED_OPERATIONS_STACK attribute.
	 * @return The item that has just been removed from the stack, or null if the stack was empty.
	 */
	private static Map popFromStack(SessionState state)
	{
		log.debug("ResourcesAction.popFromStack()");
		Map current_stack_frame = null;
		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);
		if(operations_stack == null)
		{
			operations_stack = new Stack();
			state.setAttribute(STATE_SUSPENDED_OPERATIONS_STACK, operations_stack);
		}
		if(! operations_stack.isEmpty())
		{
			current_stack_frame = (Map) operations_stack.pop();
			if(operations_stack.isEmpty())
			{
				String canceled = (String) current_stack_frame.get(STATE_HELPER_CANCELED_BY_USER);
				if(canceled != null)
				{
					state.setAttribute(STATE_HELPER_CANCELED_BY_USER, canceled);
				}
			}
		}
		return current_stack_frame;

	}

	/**
	 * Push an item of the suspended-operations stack.
	 * @param state The current session state, including the STATE_SUSPENDED_OPERATIONS_STACK attribute.
	 * @return The new item that has just been added to the stack, or null if depth limit is exceeded.
	 */
	private static Map pushOnStack(SessionState state)
	{
		log.debug("ResourcesAction.pushOnStack()");
		Map current_stack_frame = null;
		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);
		if(operations_stack == null)
		{
			operations_stack = new Stack();
			state.setAttribute(STATE_SUSPENDED_OPERATIONS_STACK, operations_stack);
		}
		if(operations_stack.size() < MAXIMUM_SUSPENDED_OPERATIONS_STACK_DEPTH)
		{
			current_stack_frame = (Map) operations_stack.push(new HashMap());
		}
		Object helper_mode = state.getAttribute(STATE_RESOURCES_HELPER_MODE);
		if(helper_mode != null)
		{
			current_stack_frame.put(STATE_RESOURCES_HELPER_MODE, helper_mode);
		}
		return current_stack_frame;

	}

	
	/**
	 *
	 * Whether a resource item can be replaced
	 * @param p The ResourceProperties object for the resource item
	 * @return true If it can be replaced; false otherwise
	 */
	private static boolean replaceable(ResourceProperties p)
	{
		log.debug("ResourcesAction.replaceable()");
		boolean rv = true;

		if (p.getPropertyFormatted (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
		{
			rv = false;
		}
		else if (p.getProperty (ResourceProperties.PROP_CONTENT_TYPE).equals (ResourceProperties.TYPE_URL))
		{
			rv = false;
		}
		String displayName = p.getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME);
		if (displayName.contains(rb.getString("shortcut")))
		{
			rv = false;
		}

		return rv;

	}	// replaceable

	/**
	 * @param pipe
	 */
	public static void reviseContent(ResourceToolActionPipe pipe)
	{
		log.debug("ResourcesAction.reviseContent()");
		ResourceToolAction action = pipe.getAction();
		ContentEntity entity = pipe.getContentEntity();
		try
		{
			ContentResourceEdit edit = contentHostingService.editResource(entity.getId());
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			// update content
			extractContent(pipe, edit);
			// update properties
			if(action instanceof InteractionAction)
			{
				InteractionAction iAction = (InteractionAction) action;
				Map revprops = pipe.getRevisedResourceProperties();
				List propkeys = iAction.getRequiredPropertyKeys();
				if(propkeys != null)
				{
					Iterator keyIt = propkeys.iterator();
					while(keyIt.hasNext())
					{
						String key = (String) keyIt.next();
						String value = (String) revprops.get(key);
						if(value == null)
						{
							props.removeProperty(key);
						}
						else
						{
							// should we support multivalued properties?
							props.addProperty(key, value);
						}
					}
				}
			}
			// update mimetype
			edit.setContentType(pipe.getRevisedMimeType());
			contentHostingService.commitResource(edit, pipe.getNotification());
		}
		catch (PermissionException e)
		{
			addAlert(trb.getString("alert.noperm"));
			// TODO Auto-generated catch block
			log.warn("PermissionException ", e);
		}
		catch (IdUnusedException e)
		{
			// TODO Auto-generated catch block
			log.warn("IdUnusedException ", e);
		}
		catch (TypeException e)
		{
			// TODO Auto-generated catch block
			log.warn("TypeException ", e);
		}
		catch (InUseException e)
		{
			// TODO Auto-generated catch block
			log.warn("InUseException ", e);
		}
		catch (OverQuotaException e)
		{
			addAlert(trb.getString("alert.quota"));
			log.warn("OverQuotaException ", e);
		}
		catch (ServerOverloadException e)
		{
			addAlert(rb.getString("failed"));
			log.warn("ServerOverloadException ", e);
		}
	}

	/**
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 */
	protected static String validateURL(String url) throws MalformedURLException
	{
		log.debug("ResourcesAction.validateURL()");
		
		// ignore the empty url field
		if(StringUtils.isBlank(url)){
			return url;
		}
		
		// return relative URLs untouched (SAK-13787)
		if(StringUtils.startsWith(url, "/")){
			return url;
		}
		
		// if it's missing the transport, add http://
		if(!StringUtils.contains(url, "://")){
			url = "http://" + url;
		}
		
		// valid protocol?
		try
		{
			// test to see if the input validates as a URL.
			// Checks string for format only.
			new URL(url);
		}
		catch (MalformedURLException e1)
		{
			try
			{
				Pattern pattern = Pattern.compile("\\s*([a-zA-Z0-9]+)://([^\\n]+)");
				Matcher matcher = pattern.matcher(url);
				if(matcher.matches())
				{
					// if URL has "unknown" protocol, check remaider with
					// "http" protocol and accept input if that validates.
					new URL("http://" + matcher.group(2));
				}
				else
				{
					throw e1;
				}
			}
			catch (MalformedURLException e2)
			{
				throw e1;
			}
		}
		return url;
	}

	/**
	 * Search a flat list of ResourcesMetadata properties for one whose localname matches "field".
	 * If found and the field can have additional instances, increment the count for that item.
	 * @param field
	 * @param properties
	 * @return true if the field is found, false otherwise.
	 */
	protected  boolean addInstance(String field, List properties)
	{
		log.debug("{}.addInstance()", this);
		Iterator propIt = properties.iterator();
		boolean found = false;
		while(!found && propIt.hasNext())
		{
			ResourcesMetadata property = (ResourcesMetadata) propIt.next();
			if(field.equals(property.getDottedname()))
			{
				found = true;
				property.incrementCount();
			}
		}
		return found;
	}

	/**
	 * Build the context to establish a custom-ordering of resources/folders within a folder.
	 */
	public String buildColumnsContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) 
	{
		log.debug("{}.buildColumnsContext()", this);
		
		// need to check permissions
		
		// get the id of the item currently selected
		String selectedItemId = (String) state.getAttribute(STATE_COLUMN_ITEM_ID);
		if(selectedItemId == null)
		{
			selectedItemId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		}
		context.put("selectedItemId", selectedItemId);
		String folderId = null;
		
		// need a list of folders (ListItem objects) for one root in context as $folders
		List<List<ListItem>> folders = new ArrayList<>();
		ContentCollection collection = null;
		ContentEntity selectedItem = null;
		
		// need a list of roots (ListItem objects) in context as $roots
		List<ListItem> roots = new ArrayList<>();
		Map othersites = contentHostingService.getCollectionMap();
		for(Iterator<Entry<String, String>> mapIter = othersites.entrySet().iterator(); mapIter.hasNext();)
		{
			Entry<String, String> entry = mapIter.next();
			String rootId = entry.getKey();
			String rootName = entry.getValue();
			ListItem root = new ListItem(rootId);
			root.setName(rootName);
			root.setHoverText(rootName);
			root.setAccessUrl(contentHostingService.getUrl(rootId));
			root.setIconLocation(contentTypeImageService.getContentTypeImage("folder"));
			
			if(selectedItemId != null && selectedItemId.startsWith(rootId))
			{
				root.setSelected(true);
				folderId = rootId;
				try
				{
					selectedItem = contentHostingService.getCollection(rootId);
				}
				catch (IdUnusedException e)
				{
					// TODO Auto-generated catch block
					log.warn("IdUnusedException ", e);
				}
				catch (TypeException e)
				{
					// TODO Auto-generated catch block
					log.warn("TypeException ", e);
				}
				catch (PermissionException e)
				{
					// TODO Auto-generated catch block
					log.warn("PermissionException ", e);
				}
			}
			roots.add(root);
		}
		// sort by name?
		context.put("roots", roots);
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		while(folderId != null)
		{
			String collectionId = folderId;
			folderId = null;

			List<ListItem> folder = new ArrayList<>();
			try 
			{
				if(collection == null)
				{
					collection = contentHostingService.getCollection(collectionId);
				}
				List members = collection.getMemberResources();
				collection = null;
				Iterator memberIt = members.iterator();
				while(memberIt.hasNext())
				{
					ContentEntity member = (ContentEntity) memberIt.next();
					String itemId = member.getId();
					ListItem item = new ListItem(member);
					if(selectedItemId != null && (selectedItemId.equals(itemId) || (member.isCollection() && selectedItemId.startsWith(itemId))))
					{
						selectedItem = member;
						item.setSelected(true);
						if(member.isCollection())
						{
							folderId = itemId;
						}
					}
					else
					{
						item.setSelected(false);
					}
					folder.add(item);
				}
				folders.add(folder);
				
				
			} 
			catch (IdUnusedException e) 
			{
				// TODO Auto-generated catch block
				log.warn("IdUnusedException {}", e.getMessage());
			} 
			catch (TypeException e) 
			{
				// TODO Auto-generated catch block
				log.warn("TypeException {}", e.getMessage());
			} 
			catch (PermissionException e) 
			{
				// TODO Auto-generated catch block
				log.warn("PermissionException {}", e.getMessage());
			}
			
		}
		context.put("folders", folders);
		
		if(selectedItem != null)
		{
			// if copy or move is in progress AND user has content.new for this folder, user can paste in the collection 
			// (the paste action will only be defined for collections)
			List<ResourceToolAction> actions = getActions(selectedItem, new TreeSet(getPermissions(selectedItem.getId(), null)), registry);
			
			// TODO: need to deal with paste actions
			
			context.put("actions", actions);
			context.put("labeler", new ResourceTypeLabeler());
		}
		
		return "content/sakai_resources_columns";
	}


	public String buildCreateWizardContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) 
	{
		log.debug("{}.buildCreateWizardContext()", this);
		context.put("metaLang", metaLang);
		context.put("site_id", toolManager.getCurrentPlacement().getContext());

		context.put("DETAILS_FORM_NAME", "detailsForm");

		String template = "content/sakai_resources_cwiz_finish";
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		if(pipe == null)
		{
			// go back to list view
		}
		else if(pipe.isActionCanceled())
		{
			// go back to list view
			state.setAttribute(STATE_MODE, MODE_LIST);
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
		}
		else if(pipe.isErrorEncountered())
		{
			String msg = pipe.getErrorMessage();
			if(StringUtils.isBlank(msg))
			{
				msg = trb.getString("alert.unknown");
			}
			addAlert(state, msg);
			state.setAttribute(STATE_MODE, MODE_LIST);
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
		}
		else
		{
			context.put(PIPE_INIT_ID, pipe.getInitializationId());
			
			// complete the create wizard
			Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
			if(defaultRetractDate == null)
			{
				defaultRetractDate = timeService.newTime();
				state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
			}
	
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			

			ContentEntity collection = pipe.getContentEntity();

			String typeId = pipe.getAction().getTypeId();
			
			ListItem parent = new ListItem(collection);

			parent.setPubviewPossible(! preventPublicDisplay);
			ListItem item = new ListItem(pipe, parent, defaultRetractDate);
			item.initMetadataGroups();
			
			// copied from ResourcesHelperAction since the context created in that class is not available to a template used here.
			if(parent.isDropbox)
			{
				String dropboxNotificationsProperty = getDropboxNotificationsProperty();
				log.debug("dropboxNotificationAllowed: buildCreateWizardContext: {}", ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty));
				context.put("dropboxNotificationAllowed", ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty));
			}
			
			context.put("item", item);
			
			state.setAttribute(STATE_CREATE_WIZARD_ITEM, item);
			
			ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
			if(registry == null)
			{
				registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
				state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
			}
			ResourceType typeDef = registry.getType(typeId);
			context.put("type", typeDef);
			
			context.put("title", (new ResourceTypeLabeler()).getLabel(pipe.getAction()));
			context.put("instruction", trb.getFormattedMessage("instr.create", new Object[]{typeDef.getLabel()}));
			context.put("required", trb.getFormattedMessage("instr.require", new Object[]{"<span class=\"reqStarInline\">*</span>"}));
			
			if(contentHostingService.isAvailabilityEnabled())
			{
				context.put("availability_is_enabled", Boolean.TRUE);
			}
			
			copyrightChoicesIntoContext(state, context);
			publicDisplayChoicesIntoContext(state, context);
			
			context.put("SITE_ACCESS", AccessMode.SITE.toString());
			context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
			context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());
			context.put("PUBLIC_ACCESS", PUBLIC_ACCESS);
			
			ResourceConditionsHelper.buildConditionContext(context, state);
		}
		
		// Get default notification ("r", "o" or "n") 
		context.put("noti", ServerConfigurationService.getString("content.default.notification", "n"));
		
		return template;
	}

	/**
	* Build the context for delete confirmation page
	*/
	public String buildDeleteConfirmContext (	VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		log.debug("{}.buildDeleteConfirmContext()", this);
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put ("collectionId", state.getAttribute (STATE_COLLECTION_ID) );

		//%%%% FIXME
		context.put ("collectionPath", state.getAttribute (STATE_COLLECTION_PATH));

		List deleteItems = (List) state.getAttribute(STATE_DELETE_ITEMS);
		List nonEmptyFolders = (List) state.getAttribute(STATE_DELETE_ITEMS_NOT_EMPTY);

		context.put ("deleteItems", deleteItems);

		Iterator it = nonEmptyFolders.iterator();
		while(it.hasNext())
		{
			ListItem folder = (ListItem) it.next();
			Object[] args = { folder.getName() };
			addAlert(state, rb.getFormattedMessage("folder.notempty", args) + " ");
		}

		//  %%STATE_MODE_RESOURCES%%
		//not show the public option when in dropbox mode
		if (RESOURCES_MODE_RESOURCES.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			context.put("dropboxMode", Boolean.FALSE);
		}
		else if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// not show the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			log.debug("dropboxNotificationAllowed: buildDeleteConfirmContext: {}", DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty));
			context.put("dropboxNotificationAllowed", DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty));
		}
		context.put("homeCollection", (String) state.getAttribute (STATE_HOME_COLLECTION_ID));
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));
		context.put ("resourceProperties", contentHostingService.newResourceProperties ());

		// String template = (String) getContext(data).get("template");
		return TEMPLATE_DELETE_CONFIRM;

	}	// buildDeleteConfirmContext

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	public String buildDeleteFinishContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		log.debug("{}.buildDeleteFinishContext()", this);
		context.put ("collectionId", state.getAttribute (STATE_COLLECTION_ID) );

		//%%%% FIXME
		context.put ("collectionPath", state.getAttribute (STATE_COLLECTION_PATH));

		List deleteItems = (List) state.getAttribute(STATE_DELETE_SET);
		List nonEmptyFolders = (List) state.getAttribute(STATE_NON_EMPTY_DELETE_SET);

		context.put ("deleteItems", deleteItems);

		Iterator it = nonEmptyFolders.iterator();
		while(it.hasNext())
		{
			ListItem folder = (ListItem) it.next();
			Object[] args = { folder.getName() };
			String msg = rb.getFormattedMessage("folder.notempty", args) + " ";
			addAlert(state, msg);
		}

		//  %%STATE_MODE_RESOURCES%%
		//not show the public option when in dropbox mode
		if (RESOURCES_MODE_RESOURCES.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			context.put("dropboxMode", Boolean.FALSE);
		}
		else if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// not show the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
		}
		context.put("homeCollection", (String) state.getAttribute (STATE_HOME_COLLECTION_ID));
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));
		context.put ("resourceProperties", contentHostingService.newResourceProperties ());

		// String template = (String) getContext(data).get("template");
		return TEMPLATE_DELETE_FINISH;

	}

	/**
	* Build the context for the new list view, which uses the resources type registry
	*/
	public String buildListContext (	VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{
		log.debug("{}.buildListContext()", this);
		
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("TYPE_UPLOAD", TYPE_UPLOAD);
		
		context.put("SITE_ACCESS", AccessMode.SITE);
		context.put("GROUP_ACCESS", AccessMode.GROUPED);
		context.put("INHERITED_ACCESS", AccessMode.INHERITED);
		context.put("PUBLIC_ACCESS", PUBLIC_ACCESS);

		context.put("ACTION_DELIMITER", ResourceToolAction.ACTION_DELIMITER);
		
		Set selectedItems = (Set) state.getAttribute(STATE_LIST_SELECTIONS);
		if(selectedItems == null)
		{
			selectedItems = new TreeSet();
			state.setAttribute(STATE_LIST_SELECTIONS, selectedItems);
		}
		context.put("selectedItems", selectedItems);
		
		Integer dropboxHighlightObj = (Integer) state.getAttribute(STATE_DROPBOX_HIGHLIGHT);
		context.put("dropboxHighlight", dropboxHighlightObj);

		// find the ContentHosting service
		ContentHostingService contentService = contentHostingService;
		//context.put ("service", contentService);
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}

		String currentSiteId = toolManager.getCurrentPlacement().getContext();

		// %%STATE_MODE_RESOURCES%%

		boolean dropboxMode = RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES));
		if (dropboxMode)
		{
			// notshow the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
			// allow filtering of dropboxes by group (SAK-14625)
			Boolean showDropboxGroupFilter = isDropboxMaintainer() || isDropboxGroupMaintainer();
			if(showDropboxGroupFilter)
			{
				List<Group> site_groups = new ArrayList<>();
				try
				{
					Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
					site_groups.addAll(site.getGroups());
					if(site_groups.size() > 0)
					{
						Collections.sort(site_groups, new GroupTitleComparator());
						context.put("dropboxGroupFilter_groups", site_groups);
						context.put("showDropboxGroupFilter", showDropboxGroupFilter.toString());
						String dropboxGroupFilter_groupId = (String) state.getAttribute("dropboxGroupFilter_groupId");
						if(StringUtils.isNotBlank(dropboxGroupFilter_groupId)) {
							context.put("dropboxGroupFiltered", Boolean.TRUE);
							context.put("dropboxGroupFilter_groupId", dropboxGroupFilter_groupId);
							context.put("dropboxGroupFilter_groupUsers", (Set) state.getAttribute("dropboxGroupFilter_groupUsers"));
						}
				}
				}
				catch(IdUnusedException e)
				{
					// something failed, group filter will be hidden
				}
			}

			//SAK-11647 - Group-aware dropboxes
			try
			{
				String currentUser = sessionManager.getCurrentSessionUserId();
				Site site = siteService.getSite(currentSiteId);
				
				if ((!contentHostingService.isDropboxMaintainer(currentSiteId))&&(contentHostingService.isDropboxGroups(currentSiteId)))
				{
					context.put("dropboxGroupPermission_enabled",Boolean.TRUE);
					
					List<Group> site_groups = new ArrayList<>();
					
					Set allGroupsUsers = new TreeSet<>();
					
					site_groups.addAll(site.getGroupsWithMember(currentUser));
					if (site_groups.size()>0)
					{
						for (Group g : site_groups)
						{
							allGroupsUsers.addAll(g.getUsers());
						}
					}
					context.put("dropboxGroupPermission_allGroupsUsers",allGroupsUsers);
				}
				else
				{
					context.put("dropboxGroupPermission_enabled",Boolean.FALSE);
				}
			}
			catch (IdUnusedException e)
			{
				log.warn("DropboxGroupPermission error: {}", (Object) e);
			}
		}
		else
		{
			//context.put("dropboxMode", Boolean.FALSE);
		}
		
		// make sure the channedId is set
		String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		context.put ("collectionId", collectionId);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		boolean atHome = collectionId.equals(homeCollectionId);
		context.put("atHome", atHome);

		String siteTitle = (String) state.getAttribute (STATE_SITE_TITLE);
		if (collectionId.equals(homeCollectionId))
		{
			context.put ("collectionDisplayName", state.getAttribute (STATE_HOME_COLLECTION_DISPLAY_NAME));
		}
		else
		{
			// should be not PermissionException thrown at this time, when the user can successfully navigate to this collection
			try
			{
				context.put("collectionDisplayName", contentService.getCollection(collectionId).getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			}
			catch (IdUnusedException e){}
			catch (TypeException e) {}
			catch (PermissionException e) {}
		}

		if(contentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}

		Comparator userSelectedSort = (Comparator) state.getAttribute(STATE_LIST_VIEW_SORT);
		
		List cPath = getCollectionPath(state);
		context.put ("collectionPath", cPath);

		// set the sort values
		String sortedBy = (String) state.getAttribute (STATE_SORT_BY);
		String sortedAsc = (String) state.getAttribute (STATE_SORT_ASC);
		context.put ("currentSortedBy", sortedBy);
		context.put ("currentSortAsc", sortedAsc);
		context.put("TRUE", Boolean.TRUE.toString());

		boolean showRemoveAction = false;
		boolean showMoveAction = false;
		boolean showCopyAction = false;

		Set highlightedItems = new TreeSet();
		
		boolean showHotDropboxWidget = false;

		try
		{
			try
			{
				contentService.checkCollection (collectionId);
				context.put ("collectionFlag", Boolean.TRUE.toString());
			}
			catch(IdUnusedException ex)
			{
				log.warn("{}IdUnusedException: {}", this, collectionId);
				try
				{
					ContentCollectionEdit coll = contentService.addCollection(collectionId);
					contentService.commitCollection(coll);
				}
				catch(IdUsedException inner)
				{
					// how can this happen??
					log.warn("{}IdUsedException: {}", this, collectionId);
					throw ex;
				}
				catch(IdInvalidException inner)
				{
					log.warn("{}IdInvalidException: {}", this, collectionId);
					// what now?
					throw ex;
				}
				catch(InconsistentException inner)
				{
					log.warn("{}InconsistentException: {}", this, collectionId);
					// what now?
					throw ex;
				}
			}
			catch(TypeException ex)
			{
				log.warn("{}TypeException.", this);
				throw ex;				
			}
			catch(PermissionException ex)
			{
				log.warn("{}PermissionException.", this);
				throw ex;
			}
			
			String copyFlag = (String) state.getAttribute (STATE_COPY_FLAG);
			if (Boolean.TRUE.toString().equals(copyFlag))
			{
				context.put ("copyFlag", copyFlag);
				List copiedItems = (List) state.getAttribute(STATE_COPIED_IDS);
				// context.put ("copiedItem", state.getAttribute (STATE_COPIED_ID));
				highlightedItems.addAll(copiedItems);
				// context.put("copiedItems", copiedItems);
			}

			String moveFlag = (String) state.getAttribute (STATE_MOVE_FLAG);
			if (Boolean.TRUE.toString().equals(moveFlag))
			{
				context.put ("moveFlag", moveFlag);
				List movedItems = (List) state.getAttribute(STATE_MOVED_IDS);
				highlightedItems.addAll(movedItems);
				// context.put ("copiedItem", state.getAttribute (STATE_COPIED_ID));
				// context.put("movedItems", movedItems);
			}

			state.removeAttribute(STATE_PASTE_ALLOWED_FLAG);
			
			List<String> items_to_be_copied = (List<String>) state.getAttribute(STATE_ITEMS_TO_BE_COPIED);
			List<String> items_to_be_moved = (List<String>) state.getAttribute(STATE_ITEMS_TO_BE_MOVED);
			
			boolean need_to_expand_all = Boolean.TRUE.toString().equals((String)state.getAttribute(STATE_NEED_TO_EXPAND_ALL));
			Set<String> expandedCollections = getExpandedCollections(state);
			expandedCollections.add(collectionId);

			DropboxAuthz dropboxAuthz = dropboxMode ? DropboxHelper.getAuthorization() : null;

			ContentCollection collection = contentHostingService.getCollection(collectionId);
			
			ListItem item = ListItem.getListItem(collection, null, registry, need_to_expand_all, expandedCollections, items_to_be_moved, items_to_be_copied, 0, userSelectedSort, false, null, dropboxAuthz);
			
			Map<String, ResourceToolAction> listActions = new HashMap<>();
			
			List<ListItem> items = item.convert2list();
			
			for(ListItem lItem : items)
			{
				if(lItem.hasMultipleItemActions())
				{
					for(String listActionId : lItem.getMultipleItemActions().keySet())
					{
						ServiceLevelAction listAction = registry.getMultiItemAction(listActionId);
						if(listAction != null)
						{
							listActions.put(listActionId, listAction);
						}
					}
				}
			}
			
                          // listActions needs to add Show and Hide
                        boolean canShowHide = canReviseOwn() || canReviseAny();
                        context.put("canShowHide", canShowHide);

                        boolean canViewHidden= canViewHidden();
                        context.put("canViewHidden", canViewHidden); 

			String zipMaxIndividualFileSizeString = ServerConfigurationService.getString("content.zip.download.maxindividualfilesize","0");
			String zipMaxTotalSizeString = ServerConfigurationService.getString("content.zip.download.maxtotalsize","0");
			boolean canZipDownload = (!zipMaxIndividualFileSizeString.equals("0") && !zipMaxTotalSizeString.equals("0")); 
			context.put("canZipDownload", canZipDownload);
			
			String containingCollectionId = contentService.getContainingCollectionId(item.getId());
			if(ContentHostingService.COLLECTION_DROPBOX.equals(containingCollectionId))
			{
				Reference ref = entityManager.newReference(contentService.getReference(item.getId()));
				Site site = siteService.getSite(ref.getContext());
				String[] args = {site.getTitle()};
				item.setName(trb.getFormattedMessage("title.dropbox", (Object[]) args));
				
				showHotDropboxWidget = true;
			}
			else if(ContentHostingService.COLLECTION_SITE.equals(containingCollectionId))
			{
				Reference ref = entityManager.newReference(contentService.getReference(item.getId()));
				Site site = siteService.getSite(ref.getContext());
				String[] args = {site.getTitle()};
				item.setName(trb.getFormattedMessage("title.resources", (Object[])args));
			}

			context.put("site", items);

			boolean show_all_sites = false;
			
			String allowed_to_see_other_sites = (String) state.getAttribute(STATE_SHOW_ALL_SITES);
			String show_other_sites = (String) state.getAttribute(STATE_SHOW_OTHER_SITES);
			context.put("show_other_sites", show_other_sites);
			if(Boolean.TRUE.toString().equals(allowed_to_see_other_sites) && canReviseAny())
			{
				context.put("allowed_to_see_other_sites", Boolean.TRUE.toString());
				show_all_sites = Boolean.TRUE.toString().equals(show_other_sites);
			}
			
			if(atHome && show_all_sites)
			{
				state.setAttribute(STATE_HIGHLIGHTED_ITEMS, highlightedItems);
				// TODO: see call to prepPage below.  That also calls readAllResources.  Are both calls necessary?
				//other_sites.addAll(readAllResources(state));
				//all_roots.addAll(other_sites);

				List<ListItem> siteCollections = prepPage(state);
				List<ListItem> otherSites = new ArrayList<>();
				for(ListItem siteCollection : siteCollections)
				{
					otherSites.addAll(siteCollection.convert2list());
					
					// looking for expanded site-level dropboxes
					// first check whether it's a dropbox
					if(siteCollection.isDropbox())
					{
						// check whether it's a site-level dropbox
						if(ContentHostingService.COLLECTION_DROPBOX.equals(siteCollection.getEntity().getContainingCollection().getId()))
						{
							// check whether it's expanded
							if(need_to_expand_all || expandedCollections.contains(siteCollection.getId()))
							{
								// in that case, show the "hot folder" widget
								showHotDropboxWidget = true;
							}
	
						}
					}
				}
				context.put("other_sites", otherSites);
				
				// SAK-20927
				for(ListItem lItem : otherSites)
				{
					if(lItem.hasMultipleItemActions())
					{
						for(String listActionId : lItem.getMultipleItemActions().keySet())
						{
							ServiceLevelAction listAction = registry.getMultiItemAction(listActionId);
							if(listAction != null)
							{
								listActions.put(listActionId, listAction);
							}
						}
					}
				}

				if (state.getAttribute(STATE_NUM_MESSAGES) != null)
				{
					context.put("allMsgNumber", state.getAttribute(STATE_NUM_MESSAGES).toString());
					context.put("allMsgNumberInt", state.getAttribute(STATE_NUM_MESSAGES));
				}

				// find the position of the message that is the top first on the page
				if ((state.getAttribute(STATE_TOP_MESSAGE_INDEX) != null) && (state.getAttribute(STATE_PAGESIZE) != null))
				{
					context.put("pagesize", ((Integer) state.getAttribute(STATE_PAGESIZE)).toString());
					int topMsgPos = ((Integer)state.getAttribute(STATE_TOP_MESSAGE_INDEX)) + 1;
					context.put("topMsgPos", Integer.toString(topMsgPos));
					int btmMsgPos = topMsgPos + ((Integer)state.getAttribute(STATE_PAGESIZE)) - 1;
					if (state.getAttribute(STATE_NUM_MESSAGES) != null)
					{
						int allMsgNumber = ((Integer)state.getAttribute(STATE_NUM_MESSAGES));
						if (btmMsgPos > allMsgNumber)
							btmMsgPos = allMsgNumber;
					}
					context.put("btmMsgPos", Integer.toString(btmMsgPos));
				}

				boolean goPPButton = state.getAttribute(STATE_PREV_PAGE_EXISTS) != null;
				context.put("goPPButton", Boolean.toString(goPPButton));
				boolean goNPButton = state.getAttribute(STATE_NEXT_PAGE_EXISTS) != null;
				context.put("goNPButton", Boolean.toString(goNPButton));

				/*
				boolean goFPButton = state.getAttribute(STATE_FIRST_PAGE_EXISTS) != null;
				context.put("goFPButton", Boolean.toString(goFPButton));
				boolean goLPButton = state.getAttribute(STATE_LAST_PAGE_EXISTS) != null;
				context.put("goLPButton", Boolean.toString(goLPButton));
				*/

				context.put("pagesize", state.getAttribute(STATE_PAGESIZE));
				// context.put("pagesizes", PAGESIZES);

			}
			
			if(showHotDropboxWidget)
			{
				context.put("showHotDropboxWidget", Boolean.TRUE.toString());
			}

			context.put("listActions", listActions);
			context.put("counter", new EntityCounter());

			// context.put ("other_sites", other_sites);
			//state.setAttribute(STATE_COLLECTION_ROOTS, all_roots);
			// context.put ("root", root);

			if(state.getAttribute(STATE_PASTE_ALLOWED_FLAG) != null)
			{
				context.put("paste_place_showing", state.getAttribute(STATE_PASTE_ALLOWED_FLAG));
			}

			if(showRemoveAction)
			{
				context.put("showRemoveAction", Boolean.TRUE.toString());
			}

			if(showMoveAction)
			{
				context.put("showMoveAction", Boolean.TRUE.toString());
			}

			if(showCopyAction)
			{
				context.put("showCopyAction", Boolean.TRUE.toString());
			}

		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfind"));
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		catch(TypeException e)
		{
			log.warn("{}TypeException.", this);
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		catch(PermissionException e)
		{
			addAlert(state, rb.getString("notpermis1"));
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		
		context.put("homeCollection", (String) state.getAttribute (STATE_HOME_COLLECTION_ID));
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));
		context.put ("resourceProperties", contentService.newResourceProperties ());

		try
		{
			// TODO: why 'site' here?
			Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
			context.put("siteTitle", site.getTitle());
		}
		catch (IdUnusedException e)
		{
			log.debug("{}{}", this, e);
		}

		context.put("expandallflag", state.getAttribute(STATE_EXPAND_ALL_FLAG));
		state.removeAttribute(STATE_NEED_TO_EXPAND_ALL);
		
		// pick the "show" template based on the standard template name
		// String template = (String) getContext(data).get("template");
		
		context.put("labeler", new ResourceTypeLabeler());
		
		contentPrintResultIntoContext(data, context, state);
		// output the current session user id
		context.put("userId", sessionManager.getCurrentSessionUserId());
		
		return TEMPLATE_NEW_LIST;

	}	// buildListContext
	
	protected void contentPrintResultIntoContext(RunData data, Context context, SessionState state)
	{
		if (state.getAttribute(CONTENT_PRINT_CALL_RESPONSE) != null)
		{
			HashMap<String, String> result = (HashMap<String, String>) state.getAttribute(CONTENT_PRINT_CALL_RESPONSE);
			String status = result.get(ContentPrintService.CONTENT_PRINT_RESPONSE_STATUS);
			if (status != null && status.equals(ContentPrintService.CONTENT_PRINT_RESPONSE_STATUS_SUCCESS))
			{
				// put the success status, confirmation message, and possible popup url address
				context.put("content_print_status_success", Boolean.TRUE);
			}
			else
			{
				// put the failure status and message
				context.put("content_print_status_failure", Boolean.TRUE);
			}
			context.put("content_print_message", result.get(ContentPrintService.CONTENT_PRINT_RESPONSE_MESSAGE));
			context.put("content_print_result_url", result.get(ContentPrintService.CONTENT_PRINT_RESPONSE_URL));
			context.put("content_print_result_url_title", result.get(ContentPrintService.CONTENT_PRINT_RESPONSE_URL_TITLE));
			
			// clean the state object
			state.removeAttribute(CONTENT_PRINT_CALL_RESPONSE);
		}
	}

	/**
	 * Check if you have 'dropbox.maintain' in the site.
	 * @return true if you have the dropbox.maintain permission in the site; false otherwise
	 */
	public boolean isDropboxMaintainer() {
		boolean isDropboxMaintainer = false;
		try {
			Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
			isDropboxMaintainer = securityService.unlock(ContentHostingService.AUTH_DROPBOX_MAINTAIN, site.getReference());
		} catch (IdUnusedException ex) {
			log.debug("Can't find current site", ex);
		}

		return isDropboxMaintainer;
	}

	/**
	 * Check if you have 'dropbox.maintain.own.groups' in the site.
	 * @return true if you have the dropbox.maintain.own.groups permission in the site; false otherwise
	 */
	public boolean isDropboxGroupMaintainer() {
		boolean isDropboxGroupMaintainer = false;
		try {
			Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
			isDropboxGroupMaintainer = securityService.unlock(ContentHostingService.AUTH_DROPBOX_GROUPS, site.getReference());
		} catch (IdUnusedException ex) {
			log.debug("Can't find current site", ex);
		}

		return isDropboxGroupMaintainer;
	}

	/**
	 * Check if you have 'content.revise.own' in the site @return
	 * @return true if the user can revise
	 */
	public boolean canReviseOwn() {
	    boolean canReviseOwn = false;
	    try {
	        Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
	        canReviseOwn = securityService.unlock(
	                ContentHostingService.AUTH_RESOURCE_WRITE_OWN, site.getReference());

	    } catch (IdUnusedException e) {
	        log.debug("ResourcesAction.canReviseOwn: cannot find current site");
	    }
	    return canReviseOwn;
	}

	/**
	 * Check if you have 'content.view.hidden' in the site
	 * @return true if can view hidden
	 */
	public boolean canViewHidden() {
	    boolean canViewHidden= false;
	    try {
	        Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
	        canViewHidden= securityService.unlock(
	                ContentHostingService.AUTH_RESOURCE_HIDDEN, site.getReference());

	    } catch (IdUnusedException e) {
	        log.debug("ResourcesAction.canViewHidden: cannot find current site");
	    }
	    return canViewHidden;
	}

	/**
	 * Check if you have 'content.revise.any' in the site
	 * @return true if user can revise
	 */
	public boolean canReviseAny() {
	    boolean canReviseAny = false;
	    try {
	        Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
	        canReviseAny = securityService.unlock(
	                ContentHostingService.AUTH_RESOURCE_WRITE_ANY, site.getReference());

	    } catch (IdUnusedException e) {
	        log.debug("ResourcesAction.canReviseAny: cannot find current site");
	    }
	    return canReviseAny;
	}

	/**
	 * Check if you have 'content.delete.any' or 'content.delete.own' in the site
	 * @return true if user can delete
	 */
	public boolean canDeleteResource() {
	    boolean canDeleteResource = false;
	    try {
	        Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
	        canDeleteResource = securityService.unlock(
	                ContentHostingService.AUTH_RESOURCE_REMOVE_ANY, site.getReference())
	                || securityService.unlock(
	    	                ContentHostingService.AUTH_RESOURCE_REMOVE_OWN, site.getReference());

	    } catch (IdUnusedException e) {
	        log.debug("ResourcesAction.canDeleteResource: cannot find current site");
	    }
	    return canDeleteResource;
	}
	

	/**
	* Build the context for normal display
	*/
	public String buildMainPanelContext (	VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		log.debug("{}.buildMainPanelContext()", this);

		/********** Start of top menu attributes ********************************/
		String siteId = toolManager.getCurrentPlacement().getContext();
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		boolean atHome = StringUtils.isNotBlank(collectionId) && collectionId.equals(homeCollectionId);
		boolean allowUpdateSite = siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext());
		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null) {
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		Map<String,Boolean> statusMap = registry.getMapOfResourceTypesForContext(siteId);
		boolean dropboxMode = RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES));
		boolean inMyWorkspace = siteService.isUserSite(siteId);
		boolean isSpecialSite = StringUtils.equalsAny(siteId, "!admin", "~admin");

		context.put("canDeleteResource", canDeleteResource());
		context.put("tlang", trb);
		context.put("clang", rb);
		context.put("slang", srb);
		context.put("siteId", siteId);
		context.put("atHome", atHome);
		context.put("inMyWorkspace", inMyWorkspace);
		context.put("dropboxMode", dropboxMode);
		context.put("showDropboxOptions", atHome && allowUpdateSite && dropboxMode);
		context.put("showQuota", !isSpecialSite && (dropboxMode || allowUpdateSite));
		context.put("showPermissions", !inMyWorkspace && !isSpecialSite && !dropboxMode && allowUpdateSite);
		context.put("showOptions", statusMap != null && !statusMap.isEmpty() && !isSpecialSite && allowUpdateSite && !dropboxMode);
		context.put("showJumpToResourceForm", isSpecialSite);
		context.put("showWebdavLink", ServerConfigurationService.getBoolean("resources.show_webdav.link", Boolean.TRUE));
		/********** End of top menu attributes ********************************/

		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));
		
		context.put("copyright_alert_url", COPYRIGHT_ALERT_URL);
		context.put("ACTION_DELIMITER", ResourceToolAction.ACTION_DELIMITER);
		context.put("DOT", ListItem.DOT);
		context.put("calendarMap", new HashMap());
		
		context.put("dateFormat", getDateFormatString());
		
		context.put("TYPE_FOLDER", ResourceType.TYPE_FOLDER);
		context.put("TYPE_HTML", ResourceType.TYPE_HTML);
		context.put("TYPE_TEXT", ResourceType.TYPE_TEXT);
		context.put("TYPE_UPLOAD", ResourceType.TYPE_UPLOAD);
		context.put("TYPE_URL", ResourceType.TYPE_URL);
		
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe != null)
		{
			context.put(PIPE_INIT_ID, pipe.getInitializationId());
			if(pipe.isActionCanceled())
			{
				state.setAttribute(STATE_MODE, MODE_LIST);
				toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			}
			else if(pipe.isErrorEncountered())
			{
				String msg = pipe.getErrorMessage();
				if(StringUtils.isBlank(msg))
				{
					msg = trb.getString("alert.unknown");
				}
				addAlert(state, msg);
				state.setAttribute(STATE_MODE, MODE_LIST);
				toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			}
			else if(pipe.isActionCompleted())
			{
				finishAction(state, toolSession, pipe);
			}
			else
			{
				toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			}
			toolSession.removeAttribute(ResourceToolAction.DONE);
		}
		
		checkMessageList(state);
		
		String template = null;
		
		// place if notification is enabled and current site is not of My Workspace type
		boolean isUserSite = siteService.isUserSite(toolManager.getCurrentPlacement().getContext());
		context.put("notification", !isUserSite && notificationEnabled(state));

		// get the mode
		String mode = (String) state.getAttribute (STATE_MODE);

		String lastMode = (String)state.getAttribute(STATE_LAST_MODE);
		if (lastMode != null && !StringUtils.equals(mode, lastMode))
		{
			// new mode - clear previous UI's input fields. Add cases for each UI that supports input preservation on validation errors.
			switch(lastMode)
			{
				case MODE_DROPBOX_MULTIPLE_FOLDERS_UPLOAD:
					DropboxMultipleFoldersUploadInputPreserver.get().clearFormData(state);
			}
		}
		state.setAttribute(STATE_LAST_MODE, mode);

		if (mode.equals (MODE_LIST))
		{
			String list_pref = (String) state.getAttribute(STATE_LIST_PREFERENCE);
			if(list_pref == null)
			{
				list_pref = LIST_HIERARCHY;
			}
			if(LIST_COLUMNS.equals(list_pref))
			{
				// build the context for list view
				template = buildColumnsContext (portlet, context, data, state);
			}
			else
			{
				// build the context for list view
				template = buildListContext (portlet, context, data, state);
			}
		}
		else if(mode.equals(MODE_CREATE_WIZARD))
		{
			template = buildCreateWizardContext(portlet, context, data, state);
		}
		else if (mode.equals (MODE_DELETE_FINISH))
		{
			// build the context for the basic step of delete confirm page
			template = buildDeleteFinishContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_SHOW_FINISH))
		{
		    // build the context for the basic step of delete confirm page
		    template = buildShowFinishContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_HIDE_FINISH))
		{
		    // build the context for the basic step of delete confirm page
		    template = buildHideFinishContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_ZIPDOWNLOAD_FINISH))
		{
			template = buildZipDownloadFinishContext ( portlet, context, data, state);
		}
		else if (mode.equals (MODE_OPTIONS))
		{
			template = buildOptionsPanelContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_DROPBOX_OPTIONS))
		{
			template = buildDropboxOptionsPanelContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_DROPBOX_MULTIPLE_FOLDERS_UPLOAD))
		{
			template = buildDropboxMultipleFoldersUploadPanelContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_RESTORE))
		{
			template = buildRestoreContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_REORDER))
		{
			template = buildReorderContext (portlet, context, data, state);
		}
		else if(mode.equals(MODE_DAV))
		{
			template =  buildWebdavContext(portlet, context, data, state);
		}
		else if(mode.equals(MODE_QUOTA))
		{
			template = buildQuotaContext (portlet, context, data, state);
		}
		else if(mode.equals(MODE_REVISE_METADATA))
		{
			template = buildReviseMetadataContext(portlet, context, data, state);
		}
		else if(mode.equals(MODE_PROPERTIES))
		{
			template = buildMoreContext(portlet, context, data, state);
		}
		else if(mode.equals(MODE_MAKE_SITE_PAGE))
		{
			template = buildMakeSitePageContext(portlet, context, data, state);
		}
		else if (mode.equals (MODE_PERMISSIONS))
		{
			template = buildPermissionsPageContext(portlet, context, data, state);
		}

		return template;

	}	// buildMainPanelContext

	public String buildMakeSitePageContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		log.debug("{}.buildMakeSitePage()", this);
		context.put("page", state.getAttribute(STATE_PAGE_TITLE));
		return TEMPLATE_MAKE_SITE_PAGE;
	}

	public String buildPermissionsPageContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {

		log.debug("{}.buildPermissionsPageContext()", this);

		String reference = (String) state.getAttribute("folder_group_reference");
		String overrideReference = null;
		if (StringUtils.isNotBlank(reference)) {
			// Skip getting containing collection for site root paths
			// Site root paths look like /content/group/site-id/
			if (!reference.matches("/content/group/[^/]+/$")) {
				overrideReference = contentHostingService.getContainingCollectionId(reference);
			}
		}

		String folderName = (String) state.getAttribute("folder_name");
		if (StringUtils.isNoneBlank(reference, folderName)) {
			context.put("reference", reference);
			if (overrideReference != null && !reference.equals(overrideReference)) {
				context.put("overrideReference", overrideReference);
			}
			context.put("folderName", folderName);
			context.put("folderLabel", rb.getString("setpermis"));
			state.removeAttribute("folder_group_reference");
			state.removeAttribute("folder_name");
		}

		context.put("warning", rb.getString("permissions.warning"));
		context.put("permissionsLabel", rb.getString("list.fPerm"));

		String toolId = toolManager.getCurrentPlacement().getId();
		String startUrl = ServerConfigurationService.getPortalUrl() + "/site/" + toolManager.getCurrentPlacement().getContext() + "/tool/" + toolId + "?panel=Main";
		context.put("startPage", startUrl);

		state.setAttribute (STATE_MODE, MODE_LIST);

		return TEMPLATE_PERMISSIONS;
	}
	
	public void doMakeSitePage(RunData data) {

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		ToolSession toolSession = sessionManager.getCurrentToolSession();
		String entityId = (String) state.getAttribute(STATE_MAKE_PAGE_ENTITY_ID);
		ContentEntity entity;
		try {
			boolean isFolder = entityId.endsWith(Entity.SEPARATOR);
			
			if (isFolder) {
				entity = contentHostingService.getCollection(entityId);
			} else {
				entity = contentHostingService.getResource(entityId);
			}
		} catch (Exception e) {
			addAlert(state, trb.getString("alert.resource.not.found"));
			state.setAttribute(STATE_MODE, MODE_LIST);
			return;
		}
		String url = entity.getUrl();
		String title = params.getString("page");

		// if the url is hosted by sakai then take out the hostname to make it a relative link
		String serverName = ServerConfigurationService.getServerName();

		// if the supplied url starts with protocol//serverName:port/
		Pattern serverUrlPattern = Pattern.compile(String.format("^(https?:)?//%s:?\\d*/", serverName));
		url = serverUrlPattern.matcher(url).replaceFirst("/");

		state.setAttribute(STATE_PAGE_TITLE, title);
		if (StringUtils.isBlank(title)) {
			addAlert(state, trb.getString("alert.page.empty"));
			return;
		}
		
		Placement placement = toolManager.getCurrentPlacement();
		String context;
		if (placement != null) {
			context = placement.getContext();
			try {
				Tool tr = toolManager.getTool("sakai.iframe");
				
				Site site = siteService.getSite(context);
				for (SitePage page: (List<SitePage>)site.getPages()) {
					if (title.equals(page.getTitle())) {
						addAlert(state, trb.getString("alert.page.exists"));
						return;
					}
				}
				SitePage newPage = site.addPage();
				newPage.setTitle(title);
				ToolConfiguration tool = newPage.addTool();
				tool.setTool("sakai.iframe", tr);
				tool.setTitle(title);
				tool.getPlacementConfig().setProperty("source", url);
				siteService.save(site);
				
				// Get it to showup in the tool menu.
				scheduleTopRefresh();
				state.setAttribute(STATE_MODE, MODE_LIST);
				state.removeAttribute(STATE_PAGE_TITLE);

			} catch (IdUnusedException e) {
				log.warn("Somehow we couldn't find the site.", e);
			} catch (PermissionException e) {
				log.info("No permission to add page.", e);
				addAlert(state, trb.getString("alert.page.permission"));
			}
		}
	}

	/**
	*  Setup for customization
	**/
	public String buildOptionsPanelContext( VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		log.debug("{}.buildOptionsPanelContext()", this);
		String home = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		Reference ref = entityManager.newReference(contentHostingService.getReference(home));
		String siteId = ref.getContext();

		context.put("form-submit", BUTTON + "doUpdateOptions");
		context.put("form-cancel", BUTTON + "doCancelOptions");
		Object[] args = { siteService.getSiteDisplay(siteId) };
		context.put("title", trb.getFormattedMessage("title.options", args));

		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		Map<String,Boolean> statusMap = registry.getMapOfResourceTypesForContext(siteId);
		context.put("statusMap", statusMap);
		
		List types = new ArrayList(registry.getTypes());
		Collections.sort(types, new ResourceTypeLabelComparator());
		
		context.put("types", types);

		return TEMPLATE_OPTIONS;

	}	// buildOptionsPanelContext

	/**
	*  Setup for customization
	**/
	public String buildDropboxOptionsPanelContext( VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		String home = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		Reference ref = entityManager.newReference(contentHostingService.getReference(home));
		String siteId = ref.getContext();

		context.put("form-submit", BUTTON + "doUpdateDropboxOptions");
		context.put("form-cancel", BUTTON + "doCancelDropboxOptions");
		Object[] args = { siteService.getSiteDisplay(siteId) };
		context.put("title", trb.getFormattedMessage("title.dropbox.options", args));

		String dropboxNotifications = getDropboxNotificationsProperty();
		context.put("value_dropbox_instructor_notifications", dropboxNotifications);
		context.put("value_dropbox_instructor_notifications_none", DROPBOX_NOTIFICATIONS_NONE);
		context.put("value_dropbox_instructor_notifications_allow", DROPBOX_NOTIFICATIONS_ALLOW);
		context.put("value_dropbox_instructor_notifications_always", DROPBOX_NOTIFICATIONS_ALWAYS);
		context.put("name_dropbox_instructor_notifications", DROPBOX_NOTIFICATIONS_PARAMETER_NAME);
		
		return TEMPLATE_DROPBOX_OPTIONS;

	}	// buildDropboxOptionsPanelContext
	
	protected String getDropboxNotificationsProperty()
	{
		Placement placement = toolManager.getCurrentPlacement();
		Properties props = placement.getPlacementConfig();
		String dropboxNotifications = props.getProperty(DROPBOX_NOTIFICATIONS_PROPERTY);
		if(dropboxNotifications == null)
		{
			dropboxNotifications = DROPBOX_NOTIFICATIONS_DEFAULT_VALUE;
		}

		log.debug("{}.getDropboxNotificationsProperty() dropboxNotifications == {}", this, dropboxNotifications);

		return dropboxNotifications;
	}
	
	/**
	 * Handle a request to set options.
	 */
	public void doDropboxOptions(RunData runData)
	{
		// ignore if not allowed
		if (!allowedToOptions())
		{
			return;
			//msg = "you do not have permission to set options for this Worksite.";
		}

		SessionState state = ((JetspeedRunData) runData).getPortletSessionState (((JetspeedRunData) runData).getJs_peid ());

		// go into options mode
		state.setAttribute(STATE_MODE, MODE_DROPBOX_OPTIONS);

	} // doOptions



	/**
	* Read user inputs from options form and update accordingly
	*/
	public void doUpdateDropboxOptions(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		String dropboxNotifications = params.getString(DROPBOX_NOTIFICATIONS_PARAMETER_NAME);
		if(dropboxNotifications == null)
		{
			dropboxNotifications = DROPBOX_NOTIFICATIONS_DEFAULT_VALUE;
		}

		Placement placement = toolManager.getCurrentPlacement();
		Properties props = placement.getPlacementConfig();
		props.setProperty(DROPBOX_NOTIFICATIONS_PROPERTY, dropboxNotifications);
		placement.save();
		
		state.setAttribute(STATE_MODE, MODE_LIST);

	}
	
	/**
	 * cancel out of options mode
	 */
	public void doCancelDropboxOptions(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_MODE, MODE_LIST);
		
	}
	
	/**
	 * Build the context to establish restoring of resources/folders.
	 */
	public String buildRestoreContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) 
	{
		String rootFolderId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

		context.put("rootFolderId", rootFolderId);

		List cPath = getCollectionPath(state);
		context.put ("collectionPath", cPath);

		Set highlightedItems = new TreeSet();
		List this_site = new ArrayList();

		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		// put the service instance into context
		context.put("contentService", contentService);
		context.put("displayNameProp", ResourceProperties.PROP_DISPLAY_NAME);
		List<ContentResource> members = contentService.getAllDeletedResources(rootFolderId);
		
		String rootTitle = (String) state.getAttribute (STATE_SITE_TITLE);
		
		// this is a list of folder ids; sequence matters here
		List<String> folderIds = new ArrayList<>();

		if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES))) {
			// SAK-44760 regenerate dropbox folders before restoring
			contentHostingService.createDropboxCollection();
		}
			
		// this map holds folder id as the hash key, and folder attributes (e.g. depth, folder name, et al.) as the hashed value
		Map<String, ResourcesBrowseItem> folderMap = new ConcurrentHashMap<>();
		
		// initialize folderIds list and folderMap for site root folder
		folderIds.add(rootFolderId);
		ResourcesBrowseItem fItem = getResourceBrowseItemForFolder(rootTitle,
									contentService.getDepth(rootFolderId, rootFolderId),
									rootFolderId);
		folderMap.put(rootFolderId, fItem);
		
		// iterate through the deleted resources
		if(members != null && members.size() > 0)
		{
			for(ContentResource resource: members) {   

				ResourcesBrowseItem newItem = getResourcesBrowseItem(resource);
				
				String collectionId = isolateContainingId(resource.getId());
				if (collectionId != null)
				{
					
					if (!folderIds.contains(collectionId))
					{
						String currentFolderId = rootFolderId;
						String path = collectionId.replace(rootFolderId, "");
						String pathParts[]=path.split("/");
						// iterate all the parent folders until reaching the site root collection level
						// update folderIds and folderMap objects
						for(String pathPart : pathParts)
						{
							currentFolderId += pathPart + "/";
							ContentCollection currentFolder = null;
							String currentFolderName = null;
							try
							{
								currentFolder = contentService.getCollection(currentFolderId);
								currentFolderName = currentFolder.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
							}
							catch (IdUnusedException e)
							{
								log.warn("{} buildRestoreContext cannot get resource {} {}", this, currentFolderId, e.getMessage());
							}
							catch (TypeException e)
							{
								log.warn("{} buildRestoreContext cannot get resource {} {}", this, currentFolderId, e.getMessage());
							}
							catch (PermissionException e)
							{
								log.warn("{} buildRestoreContext cannot get resource {} {}", this, currentFolderId, e.getMessage());
							}
							finally
							{
								if (currentFolder == null)
								{
									// folder has been deleted; get folder name from its id
									currentFolderName=isolateName(currentFolderId);
								}
								
								if (!folderIds.contains(currentFolderId))
								{
									// add the folder id into collection
									folderIds.add(currentFolderId);
								}
								
								if (!folderMap.containsKey(currentFolderId))
								{
									// update the HashMap for folder attributes, with folder name and folder depth
									fItem = getResourceBrowseItemForFolder(currentFolderName,
																		   contentService.getDepth(currentFolderId, rootFolderId),
																		   currentFolderId);
									folderMap.put(currentFolderId, fItem);
								}
							}
						}
					}
					
					if (folderMap.containsKey(collectionId))
					{
						// add current item into the members list
						ResourcesBrowseItem attributes = folderMap.get(collectionId);
						List<ResourcesBrowseItem> itemList = new ArrayList<>();
						itemList.add(newItem);
						attributes.addMembers(itemList);
						folderMap.put(collectionId, attributes);
					}
				}
			}
			
			context.put("folderIds", folderIds);
			context.put("folderMap", folderMap);
			context.put("rootFolderId", rootFolderId);
			
			// restore item list not empty
			context.put("noRestoreItems", Boolean.FALSE);
		}
		else
		{
			// restore item list empty
			context.put("noRestoreItems", Boolean.TRUE);
		}

		context.put ("this_site", rootTitle);
		
		context.put("cleanupInterval", state.getAttribute(STATE_CLEANUP_DELETED_CONTENT_INTERVAL));
		
		if (state.getAttribute("restored_resources") != null)
		{
			// show restore confirmation message
			context.put("restoredResources", state.getAttribute("restored_resources"));
			state.removeAttribute("restored_resources");
		}
		if (state.getAttribute("removed_resources") != null)
		{
			// show remove confirmation message
			context.put("removedResources", state.getAttribute("removed_resources"));
			state.removeAttribute("removed_resources");
		}
	      
		return TEMPLATE_RESTORE;
	}
	
	/**
	 * return a ResourcesBrowseItem for given folder
	 * @param displayName
	 * @param depth
	 * @param currentFolderId
	 * @return
	 */
	private ResourcesBrowseItem getResourceBrowseItemForFolder(String displayName, int depth, String currentFolderId) {
		ResourcesBrowseItem fItem;
		fItem = new ResourcesBrowseItem(currentFolderId, displayName, "folder");
		fItem.setDepth(depth);
		return fItem;
	}

	/**
	 * get an ResourcesBrowseItem object based on a given ContentResource object
	 * @param resource
	 * @return
	 */
	private ResourcesBrowseItem getResourcesBrowseItem(ContentResource resource) {
		ResourceProperties props = resource.getProperties();
		String itemType = ((ContentResource)resource).getContentType();
		String itemName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		ResourcesBrowseItem newItem = new ResourcesBrowseItem(resource.getId(), itemName, itemType);
		try
		{
			Time modTime = props.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
			String modifiedTime = modTime.toStringLocalShortDate() + " " + modTime.toStringLocalShort();
			newItem.setModifiedTime(modifiedTime);
		}
		catch(Exception e)
		{
			String modifiedTimeString = props.getProperty(ResourceProperties.PROP_MODIFIED_DATE);
			newItem.setModifiedTime(modifiedTimeString);
		}
		try
		{
			String modifiedBy = getUserProperty(props, ResourceProperties.PROP_MODIFIED_BY).getDisplayName();
			newItem.setModifiedBy(modifiedBy);
		}
		catch(Exception e)
		{
			String modifiedBy = props.getProperty(ResourceProperties.PROP_MODIFIED_BY);
			newItem.setModifiedBy(modifiedBy);
		}
		return newItem;
	}

	/**
	 * Build the context to establish a custom-ordering of resources/folders within a folder.
	 */
	public String buildReorderContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) 
	{
		log.debug("{}.buildReorderContext()", this);
		String folderId = (String) state.getAttribute(STATE_REORDER_FOLDER);
		context.put("folderId", folderId);
		
		// save expanded folder lists
		Set<String> expandedCollections = getExpandedCollections(state);
		Map expandedFolderSortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);

		// create temporary expanded folder lists for this invocation of getListView
		// TODO Using session state to pass values to methods shouldn't be used.
		Map tempExpandedFolderSortMap = new HashMap();
		state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, tempExpandedFolderSortMap);
		state.removeAttribute(STATE_EXPANDED_COLLECTIONS);
		Set<String> tempExpandedCollections = getExpandedCollections(state);
		tempExpandedCollections.add(folderId);

		Set highlightedItems = new TreeSet();
		List all_roots = new ArrayList();
		List this_site = new ArrayList();

		List members = getListView(folderId, highlightedItems, (ResourcesBrowseItem) null, true, state);

		// restore expanded folder lists 
		state.removeAttribute(STATE_EXPANDED_COLLECTIONS);
		Set<String> newExpandedCollections = getExpandedCollections(state);
		newExpandedCollections.addAll(expandedCollections);
		newExpandedCollections.addAll(tempExpandedCollections);
		expandedFolderSortMap.putAll(tempExpandedFolderSortMap);
		state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, expandedFolderSortMap);

		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

		List cPath = getCollectionPath(state);
		context.put ("collectionPath", cPath);

		
		String sortBy = (String) state.getAttribute(STATE_REORDER_SORT_BY);
		context.put("sortBy", sortBy);
		String sortAsc = (String) state.getAttribute(STATE_REORDER_SORT_ASC);
		context.put("sortAsc", sortAsc);
		// Comparator comparator = (Comparator) state.getAttribute(STATE_REORDER_SORT);

		String rootTitle = (String) state.getAttribute (STATE_SITE_TITLE);
		if (folderId.equals(homeCollectionId))
		{
			String siteTitle = (String) state.getAttribute (STATE_SITE_TITLE);
			rootTitle = siteTitle + " " + rb.getString("gen.reso");
		}
		else
		{
			// should be not PermissionException thrown at this time, when the user can successfully navigate to this collection
			try
			{
				rootTitle = contentHostingService.getCollection(folderId).getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			}
			catch (IdUnusedException e){}
			catch (TypeException e) {}
			catch (PermissionException e) {}
		}

		if(members != null && members.size() > 0)
		{
			ResourcesBrowseItem root = (ResourcesBrowseItem) members.remove(0);
			root.addMembers(members);
			root.setName(rootTitle);
			this_site.add(root);
			all_roots.add(root);
		}
		context.put ("this_site", this_site);
		
		return TEMPLATE_REORDER;
	}

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	public String buildReviseMetadataContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		log.debug("{}.buildReviseMetadataContext()", this);
		context.put("metaLang", metaLang);

		context.put("DETAILS_FORM_NAME", "detailsForm");
		
		ResourceToolAction action = (ResourceToolAction) state.getAttribute(STATE_REVISE_PROPERTIES_ACTION);
		context.put("action", action);
		
		context.put("showItemSummary", Boolean.TRUE.toString());
		context.put("site_id", toolManager.getCurrentPlacement().getContext());
		
		String typeId = action.getTypeId();
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		ResourceType type = registry.getType(typeId);
		context.put("type", type);
		
		copyrightChoicesIntoContext(state, context);
		publicDisplayChoicesIntoContext(state, context);
		
		context.put("required", trb.getFormattedMessage("instr.require", new Object[]{"<span class=\"reqStarInline\">*</span>"}));
		
		ListItem item = (ListItem) state.getAttribute(STATE_REVISE_PROPERTIES_ITEM);
		if(item == null)
		{
			item = getListItem(state);
			state.setAttribute(STATE_REVISE_PROPERTIES_ITEM, item);
		}
		item.initMetadataGroups();
		
		if(item.isDropbox)
		{
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			log.debug("dropboxNotificationAllowed: buildReviseMetadataContext: {}", ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty));
			context.put("dropboxNotificationAllowed", ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty));
		}
		
		item.initMetadataGroups();
		context.put("item", item);

		final boolean showFilter = ServerConfigurationService.getBoolean("resources.filter.show", Boolean.FALSE);
		context.put("showFilter", showFilter);

		final boolean showQuirks = ServerConfigurationService.getBoolean("resources.filter.showquirks", Boolean.FALSE);
		context.put("showQuirks", showQuirks);
		
		String chhbeanname = "";
		if (item.entity != null && item.entity.getProperties() != null)
		{
			chhbeanname = item.entity.getProperties().getProperty(
					ContentHostingHandlerResolver.CHH_BEAN_NAME);
			if (chhbeanname == null) chhbeanname = "";
		}
		context.put("CHHmountpoint", chhbeanname);
		

		
		if(contentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}
		
		context.put("SITE_ACCESS", AccessMode.SITE.toString());
		context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
		context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());
		context.put("PUBLIC_ACCESS", PUBLIC_ACCESS);

		if(contentHostingService.isContentHostingHandlersEnabled() && securityService.isSuperUser())
		{
			context.put("showMountPointProperty", Boolean.TRUE.toString());
		}
		ResourceConditionsHelper.buildConditionContext(context, state);
		
		context.put("shortUrlEnabled", ServerConfigurationService.getBoolean("shortenedurl.resources.enabled", true));
		
		// Get default notification ("r", "o" or "n") 
		context.put("noti", ServerConfigurationService.getString("content.default.notification", "n"));
		
		return TEMPLATE_REVISE_METADATA;
	}

	/**
     * @param state
     * @return
     */
    protected ListItem getListItem(SessionState state)
    {
		log.debug("{}.getListItem()", this);
	    // complete the create wizard
		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
		if(defaultRetractDate == null)
		{
			defaultRetractDate = timeService.newTime();
			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		String entityId = (String) state.getAttribute(STATE_REVISE_PROPERTIES_ENTITY_ID);
		String refstr = contentHostingService.getReference(entityId);
		Reference ref = entityManager.newReference(refstr);
		ContentEntity entity = (ContentEntity) ref.getEntity();

		ListItem item = new  ListItem(entity);
		if(item.getReleaseDate() == null)
		{
			item.setReleaseDate(timeService.newTime());
		}
		if(item.getRetractDate() == null)
		{
			item.setRetractDate(defaultRetractDate);
		}
		item.setPubviewPossible(! preventPublicDisplay);
	    return item;
    }

	/**
	* Build the context for add display
	*/
	public String buildWebdavContext (	VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{
		log.debug("{}.buildWebdavContext()", this);
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		String homeCollectionId = (String) state.getAttribute (STATE_HOME_COLLECTION_ID);

		boolean maintainer = false;
		String modeResources = (String) state.getAttribute(STATE_MODE_RESOURCES);
		if(RESOURCES_MODE_DROPBOX.equalsIgnoreCase(modeResources)) {
			String[] parts = homeCollectionId.split(Entity.SEPARATOR);
			if(parts.length >= 4)
			{
				maintainer = false;
			}
			else if(parts.length >= 3)
			{
				maintainer = true;
			}
		}
		context.put("maintainer", Boolean.toString(maintainer));

		context.put("server_url", ServerConfigurationService.getServerUrl());
		context.put("site_id", toolManager.getCurrentPlacement().getContext());
		context.put("site_title", state.getAttribute(STATE_SITE_TITLE));

		String eid = userDirectoryService.getCurrentUser().getEid();
		// Check the user is logged in and doesn't have characters that cause problems in WebDAV urls.
		String userUrlId = (eid != null && eid.matches(".*(;|/|\\?|:|@|&|=|\\+).*"))
				? userDirectoryService.getCurrentUser().getId() : eid;
		context.put("user_id", userUrlId);
		
		if (contentHostingService.isShortRefs())
		{
			// with short refs, this is prettier
			context.put ("dav_group", "/dav/");
			context.put ("dav_user", "/dav/~");
			context.put ("dav_group_user", "/dav/group-user/");
		}
		else
		{
			context.put ("dav_group", "/dav/group/");
			context.put ("dav_user", "/dav/user/");
			context.put ("dav_group_user", "/dav/group-user/");
		}

		String webdav_instructions = ServerConfigurationService.getString("webdav.instructions.url");
		int extIndex = webdav_instructions.indexOf(".html");
		String webdav_doc = webdav_instructions.substring(0,extIndex).trim();
		String locale = new ResourceLoader().getLocale().getLanguage();
		String country = new ResourceLoader().getLocale().getCountry();

		if ((locale == null) || locale.equalsIgnoreCase("en") || (locale.trim().length()==0)){
			webdav_instructions = ServerConfigurationService.getString("webdav.instructions.url");
		}else{
			String locale_country_webdav_instructions = String.format("%s_%s_%s.html", webdav_doc, locale, country);
			File contentRoot = new File(getServletContext().getRealPath("/"));
			File localeFile;
			localeFile = new File(contentRoot.getParent(), locale_country_webdav_instructions);
			if (localeFile.exists()){
				webdav_instructions = locale_country_webdav_instructions;
			} else {
				String locale_webdav_instructions = String.format("%s_%s.html", webdav_doc, locale);
				localeFile = new File(contentRoot.getParent(), locale_webdav_instructions);
				if ( localeFile.exists() ) {
					webdav_instructions = locale_webdav_instructions;
				}
			}
		}

		context.put("webdav_instructions" ,webdav_instructions);

		String siteId = toolManager.getCurrentPlacement().getContext();
		boolean inMyWorkspace = siteService.isUserSite(toolManager.getCurrentPlacement().getContext());
		boolean dropboxMode = RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES));
		boolean changed = false;

		if (!inMyWorkspace && !dropboxMode && m_siteAlias)
		{
			// find site alias first
			List target = aliasService.getAliases("/site/" + siteId);
	
			if (!target.isEmpty()) {
				// take the first alias only
				AliasEdit alias = (AliasEdit) target.get(0);
				siteId = alias.getId();
	
				// if there is no a site id exists that matches the alias name
				if (!siteService.siteExists(siteId))
				{
					changed = true;
				}
			} else {
				// use mail archive alias
				target = aliasService.getAliases("/mailarchive/channel/" + siteId + "/main");
	
				if (!target.isEmpty()) {
					// take the first alias only
					AliasEdit alias = (AliasEdit) target.get(0);
					siteId = alias.getId();
	
					// if there is no a site id exists that matches the alias name
					if (!siteService.siteExists(siteId))
					{
						changed = true;
					}
				}
			}
		}
		
		if (changed) {
			context.put("site_alias", siteId);						
		} else {
			context.put("site_alias", "");								
		}

		return TEMPLATE_DAV;

	}	// buildWebdavContext
	
	/**
	* Build the context for add display
	*/
	public String buildQuotaContext(	VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{
		log.debug("{}.buildQuotaContext()", this);
		// find the ContentTypeImage service
		
		boolean dropboxMode = RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES));
		String siteCollectionId = dropboxMode ? contentHostingService.getDropboxCollection(toolManager.getCurrentPlacement().getContext()) : contentHostingService.getSiteCollection(toolManager.getCurrentPlacement().getContext());
		try
		{
			ContentCollection collection = contentHostingService.getCollection(siteCollectionId);
			long quota = contentHostingService.getQuota(collection);
			long usage = collection.getBodySizeK();
			
			String usageMsg = (usage == 0) ? rb.getFormattedMessage("size.kb", new Object[] { 0 })
					: ListItem.formatSize(usage * 1024);
			context.put("usage", usageMsg);
			
			if (quota > 0)
			{
				long usagePercent = usage * 100 / quota;
				context.put("usagePercent", usagePercent+"%");
				context.put("quota", ListItem.formatSize(quota * 1024));
			}
			else
			{
				context.put("quota", rb.get("quota.unlimited"));
				context.put("usagePercent", "");
			}
		}
		catch (IdUnusedException e)
		{
			log.warn("Can't find collection for site: {}", siteCollectionId, e);
		}
		catch(TypeException e){
			log.warn("Site collection is of wrong type.", e);
		}
		catch(PermissionException e){
			log.warn("User doesn't have permission to access site collection", e);
		}

		context.put("dropboxMode", Boolean.toString(dropboxMode));
		
		boolean maintainer = siteService.allowUpdateSite(siteCollectionId);
		context.put("maintainer", Boolean.toString(maintainer));

		return TEMPLATE_QUOTA;

	}	// buildWebdavContext
	

	/**
	 * Iterate over attributes in ToolSession and remove all attributes starting with a particular prefix.
	 * @param toolSession
	 * @param prefix
	 */
	protected void cleanup(ToolSession toolSession, String prefix) 
	{
		log.debug("{}.cleanup()", this);
		Enumeration attributeNames = toolSession.getAttributeNames();
		while(attributeNames.hasMoreElements())
		{
			String aName = (String) attributeNames.nextElement();
			if(aName.startsWith(prefix))
			{
				toolSession.removeAttribute(aName);
			}
		}
		
	}

	/**
	 * @param state
	 * @param itemId
	 */
	protected void deleteItem(SessionState state, String itemId)
	{
		log.debug("{}.deleteItem()", this);
		List deleteItems = new ArrayList();
		List notDeleteItems = new ArrayList();
		List nonEmptyFolders = new ArrayList();
		
		boolean isFolder = itemId.endsWith(Entity.SEPARATOR);
		
		try
		{
			ContentEntity entity;
			if(isFolder)
			{
				entity = contentHostingService.getCollection(itemId);
			}
			else
			{
				entity = contentHostingService.getResource(itemId);
			}
			
			ListItem member = new ListItem(entity);
			
			if(isFolder)
			{
				ContentCollection collection = (ContentCollection) entity;
				if(contentHostingService.allowRemoveCollection(itemId))
				{
					deleteItems.add(member);
					if(collection.getMemberCount() > 0)
					{
						nonEmptyFolders.add(member);
					}
				}
				else
				{
					notDeleteItems.add(member);
				}
			}
			else if(contentHostingService.allowRemoveResource(member.getId()))
			{
				deleteItems.add(member);
			}
			else
			{
				notDeleteItems.add(member);
			}
		}
		catch (IdUnusedException e)
		{
			// TODO Auto-generated catch block
			log.warn("IdUnusedException ", e);
		}
		catch (TypeException e)
		{
			// TODO Auto-generated catch block
			log.warn("TypeException ", e);
		}
		catch (PermissionException e)
		{
			// TODO Auto-generated catch block
			log.warn("PermissionException ", e);
		}
		

		if(! notDeleteItems.isEmpty())
		{
			String notDeleteNames = "";
			boolean first_item = true;
			Iterator notIt = notDeleteItems.iterator();
			while(notIt.hasNext())
			{
				ListItem item = (ListItem) notIt.next();
				if(first_item)
				{
					notDeleteNames = item.getName();
					first_item = false;
				}
				else if(notIt.hasNext())
				{
					notDeleteNames += ", " + item.getName();
				}
				else
				{
					notDeleteNames += " and " + item.getName();
				}
			}
			addAlert(state, rb.getFormattedMessage("notpermis14", new Object[] {notDeleteNames}));
		}

		if(state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_DELETE_SET, deleteItems);
			state.setAttribute (STATE_NON_EMPTY_DELETE_SET, nonEmptyFolders);
		}
	}

	/**
	 * @param state
	 * @param deleteIdSet
	 */
	protected void deleteItems(SessionState state, Set deleteIdSet)
	{
		log.debug("{}.deleteItems()", this);
		List deleteItems = new ArrayList();
		List notDeleteItems = new ArrayList();
		List nonEmptyFolders = new ArrayList();
		
		ContentHostingService contentService = contentHostingService;
		
		for(String deleteId : (Set<String>) deleteIdSet)
		{
			ContentEntity entity = null;
			try
			{
				if(contentService.isCollection(deleteId))
				{
					entity = contentService.getCollection(deleteId);
				}
				else
				{
					entity = contentService.getResource(deleteId);
				}
				
				ListItem item = new ListItem(entity);
				if(item.isCollection() && contentService.allowRemoveCollection(deleteId))
				{
					deleteItems.add(item);
					if(! item.isEmpty)
					{
						nonEmptyFolders.add(item);
					}
				}
				else if(!item.isCollection() && contentService.allowRemoveResource(deleteId))
				{
					deleteItems.add(item);
				}
				else
				{
					notDeleteItems.add(item);
				}
				
			}
			catch(PermissionException e)
			{
				log.warn("PermissionException", e);
			} 
			catch (IdUnusedException e) 
			{
				log.warn("IdUnusedException", e);
			} 
			catch (TypeException e) 
			{
				log.warn("TypeException", e);
			}
		}

		if(! notDeleteItems.isEmpty())
		{
			String notDeleteNames = "";
			boolean first_item = true;
			Iterator notIt = notDeleteItems.iterator();
			while(notIt.hasNext())
			{
				ListItem item = (ListItem) notIt.next();
				if(first_item)
				{
					notDeleteNames = item.getName();
					first_item = false;
				}
				else if(notIt.hasNext())
				{
					notDeleteNames += ", " + item.getName();
				}
				else
				{
					notDeleteNames += " and " + item.getName();
				}
			}
			addAlert(state, rb.getFormattedMessage("notpermis14", new Object[] {notDeleteNames}));
		}

		state.setAttribute (STATE_DELETE_SET, deleteItems);
		state.setAttribute (STATE_NON_EMPTY_DELETE_SET, nonEmptyFolders);
	}


	/**
	* doCancel to return to the previous state
	*/
	public void doCancel ( RunData data)
	{
		log.debug("{}.doCancel()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());
		
		state.setAttribute(STATE_MODE, MODE_LIST);
	}	// doCancel

	/**
	* Remove the collection id from the expanded collection list
	*/
	public void doCollapse_collection(RunData data)
	{
		log.debug("{}.doCollapse_collection()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		Set<String> expandedItems = getExpandedCollections(state);
		Map folderSortMap = setStateAttributeExpandedFolderSortMap(state);

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();
		String collectionId = params.getString("collectionId");

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = data.getParameters ().getStrings ("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		SortedSet newSet = new TreeSet();
		 for(String id : expandedItems)
		 {
			 // remove the collection id and all of the subcollections
//		    Resource collection = (Resource) l.next();
//			String id = (String) collection.getId();
			 if( id.startsWith(collectionId) )
			 {
				 String refstr = contentHostingService.getReference(id);
				 if(refstr != null)
				 {
					 Reference reference = entityManager.newReference(refstr);
					 if(reference != null)
					 {
						 ContentEntity entity = (ContentEntity) reference.getEntity();
						 //its possible that the contentEntity is null
						 if (entity != null)
						 {
							 String typeId = entity.getResourceType();
							 ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
							 if(typeId != null && registry != null)
							 {
								 ResourceType typeDef = registry.getType(typeId);
								 if(typeDef != null && typeDef.isExpandable())
								 {
									 ServiceLevelAction collapseAction = ((ExpandableResourceType) typeDef).getCollapseAction();
									 if(collapseAction != null && collapseAction.available(entity))
									 {
										 collapseAction.initializeAction(reference);

										 collapseAction.finalizeAction(reference);
										 
										 folderSortMap.remove(id);
									 }
								 }
							 }
						 }
					 }
				 }
			 }
			 else
			 {
				 // newSet.put(id,collection);
				 newSet.add(id);
			 }
		 }
		expandedItems.clear();
		expandedItems.addAll(newSet);

	}	// doCollapse_collection


	public void doColumns(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		log.debug("{}.doColumns()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.setAttribute(STATE_LIST_PREFERENCE, LIST_COLUMNS);
	}

	//Test if groups are selected when needed
	public static boolean checkGroups(ParameterParser params)
	{
			//Control if groups are selected
			String access_mode= params.getString("access_mode" + ListItem.DOT + "0");
			if (access_mode != null) 
			{
				if (access_mode.equals("grouped"))
				{
					String[] access_groups = params.getStrings("access_groups" + ListItem.DOT + "0");
					if (access_groups==null || access_groups.length==0) 
					{
						return false;
					}
				}
			}
			return true; 	
	}
	
	/**
	 * @param data
	 */
	public void doCompleteCreateWizard(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		log.debug("{}.doCompleteCreateWizard()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		String user_action = params.getString("user_action");
		
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe == null)
		{
			return;
		}
		
		String pipe_init_id = pipe.getInitializationId();
		String response_init_id = params.getString(PIPE_INIT_ID);

		if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
		{
			// in this case, prevent upload to wrong folder
			pipe.setErrorMessage(rb.getString("alert.try-again"));
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(true);
			pipe.setActionCompleted(false);
			return;
		}
		
		if(user_action == null)
		{
			user_action = pipe.getAction().getId();
		}
		
		if("save".equals(user_action))
		{
			ListItem item = (ListItem) state.getAttribute(STATE_CREATE_WIZARD_ITEM);
			item.captureProperties(params, ListItem.DOT + "0");
			if (item.numberFieldIsInvalid) {
				addAlert(state, rb.getString("conditions.invalid.condition.argument"));
				return;
			}
			if (item.numberFieldIsOutOfRange) {
				addAlert(state, rb.getFormattedMessage("conditions.condition.argument.outofrange", new Object[] { item.getConditionAssignmentPoints() }));
				return;
			}
			if(!"".equals(item.metadataValidationFails)) {
				addAlert(state, metaLang.getFormattedMessage("metadata.validation.error", item.metadataValidationFails));
				return;
			}
			String name = params.getString("name" + ListItem.DOT + "0");
			if(name == null)
			{
				name = item.getName();
			}
			else
			{
				item.setName(name);
			}
			if(name == null)
			{
				item.setNameIsMissing(true);
				addAlert(state, rb.getString("edit.missing"));
				return;
			}
			else
			{
				name = name.trim();
			}
			//Control groups
			if (!checkGroups(params)) {
				addAlert(state, trb.getString("alert.youchoosegroup")); 
				return;
			}
			
			String collectionId = (String) state.getAttribute(STATE_CREATE_WIZARD_COLLECTION_ID);
			try 
			{
				// title
				String basename = name;
				String extension = "";
				if(name.contains("."))
				{
					String[] parts = name.split("\\.");
					StringBuilder sb = new StringBuilder(parts[0]);
					if(parts.length > 1)
					{
						extension = parts[parts.length - 1];
					}
					
					for(int i = 1; i < parts.length - 1; i++)
					{
						sb.append( "." ).append(parts[i]);
					}
					
					basename = sb.toString();
				}
				
				// create resource
				ContentResourceEdit resource = contentHostingService.addResource(collectionId, Validator.escapeResourceName(basename), Validator.escapeResourceName(extension), MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				
				String resourceType = null;
				ResourceToolAction action = null;
				if(pipe != null)
				{
					action = pipe.getAction();
					if(action != null)
					{
						resourceType = action.getTypeId();
					}
				}
				
				resource.setResourceType(resourceType);
				item.setId(resource.getId());
				ResourceConditionsHelper.saveCondition(item, params, state, 0);
				item.updateContentResourceEdit(resource);
				
				extractContent(pipe, resource);

				resource.setContentType(pipe.getRevisedMimeType());
				
				ResourcePropertiesEdit resourceProperties = resource.getPropertiesEdit();
				Map values = pipe.getRevisedResourceProperties(); 	 	 
				for(Iterator<Entry<String, String>> mapIter = values.entrySet().iterator(); mapIter.hasNext();)	 
				{
					Entry<String, String> entry = mapIter.next(); 
					resourceProperties.addProperty(entry.getKey(), entry.getValue());
				
				} 	 

				// notification
				int noti = NotificationService.NOTI_NONE;
				// %%STATE_MODE_RESOURCES%%
				if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
				{
					boolean notification;
				
					if(item.userIsMaintainer())	// if the user is a site maintainer
					{
						notification = params.getBoolean("notify_dropbox");
		  				if(notification)
		   				{
		   					noti = NotificationService.NOTI_REQUIRED;
		   				}
					}
					else
					{
						String notifyDropbox = getDropboxNotificationsProperty();
						if(DROPBOX_NOTIFICATIONS_ALWAYS.equals(notifyDropbox))
						{
							noti = NotificationService.NOTI_OPTIONAL;
						}
						else if(DROPBOX_NOTIFICATIONS_ALLOW.equals(notifyDropbox))
						{
							notification = params.getBoolean("notify_dropbox");
			  				if(notification)
			   				{
			   					noti = NotificationService.NOTI_OPTIONAL;
			   				}
						}
					}
					log.debug("{}.doCompleteCreateWizard() noti == {}", this, noti);
				}
				else
				{
					// read the notification options
					String notification = params.getString("notify");
					if ("r".equals(notification))
					{
						noti = NotificationService.NOTI_REQUIRED;
					}
					else if ("o".equals(notification))
					{
						noti = NotificationService.NOTI_OPTIONAL;
					}
				}

				List<String> alerts = item.checkRequiredProperties(copyrightManager);

				if(alerts.isEmpty())
				{
					try
					{
						contentHostingService.commitResource(resource, noti);
						ResourceConditionsHelper.notifyCondition(resource);
						if(action instanceof InteractionAction)
						{
						    InteractionAction iAction = (InteractionAction) action;
						    iAction.finalizeAction(entityManager.newReference(contentHostingService.getReference(resource.getId())), pipe.getInitializationId());
						}
						toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);

						// show folder if in hierarchy view
						Set<String> expandedCollections = getExpandedCollections(state);
						expandedCollections.add(collectionId);

						state.setAttribute(STATE_MODE, MODE_LIST);
					}
					catch(OverQuotaException e)
					{
						addAlert(state, trb.getFormattedMessage("alert.overquota", new Object[]{resource.getId()}));
						log.debug("OverQuotaException {}", (Object) e);
						try
						{
							contentHostingService.removeResource(resource.getId());
						}
						catch(Exception e1)
						{
							log.debug("Unable to remove partially completed resource: {}\n{}", resource.getId(), e);
						}
					}
					catch(ServerOverloadException e)
					{
						addAlert(state, trb.getFormattedMessage("alert.unable1", new Object[]{resource.getId()}));
						log.debug("ServerOverloadException {}", (Object) e);
						try
						{
							contentHostingService.removeResource(resource.getId());
						}
						catch(Exception e1)
						{
							log.debug("Unable to remove partially completed resource: {}\n{}", resource.getId(), e);
						}
					}
				}
				else
				{
					for(String alert : alerts)
					{
						addAlert(state, alert);
					}
					contentHostingService.cancelResource(resource);
				}
			} 
			catch (IdUnusedException e) 
			{
				log.warn("IdUnusedException", e);
			} 
			catch (PermissionException e) 
			{
				log.warn("PermissionException", e);
			} 
			catch (IdInvalidException e) 
			{
				log.warn("IdInvalidException", e);
			} 
			catch (ServerOverloadException e) 
			{
				addAlert(state, trb.getFormattedMessage("alert.unable1", new Object[]{name}));
				log.warn("ServerOverloadException{}", (Object) e);
			}
			catch (OverQuotaException e)
			{
				addAlert(state, trb.getFormattedMessage("alert.overquota", new Object[]{name}));
				log.warn("OverQuotaException {}", (Object) e);
			}
            catch (IdUniquenessException e)
            {
	            addAlert(state, trb.getFormattedMessage("paste.error", new Object[]{name}));
            }
			catch (IdLengthException e)
			{
				addAlert(state, trb.getFormattedMessage("alert.toolong", e.getReference()));
			}
		}
		else if("cancel".equals(user_action))
		{
			if(pipe != null)
			{
				ResourceToolAction action = pipe.getAction();
				if(action == null)
				{
					
				}
				else 
				{
					if(action instanceof InteractionAction)
					{
						InteractionAction iAction = (InteractionAction) action;
						iAction.cancelAction(null, pipe.getInitializationId());
					}
					state.removeAttribute(ResourceToolAction.ACTION_PIPE);
				}
			}
			state.setAttribute(STATE_MODE, MODE_LIST);
		}
	}

	/**
	* set the state name to be "copy" if any item has been selected for copying
	*/
	public void doCopy ( RunData data )
	{
		log.debug("{}.doCopy()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		List copyItemsVector = new ArrayList ();

		String[] copyItems = data.getParameters ().getStrings ("selectedMembers");
		if (copyItems == null)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile6"));
			state.setAttribute (STATE_MODE, MODE_LIST);
		}
		else
		{
			String copyId;
			for(String copyItem : copyItems)
			{
				copyId = copyItem;
				try
				{
					ResourceProperties properties = contentHostingService.getProperties (copyId);
					/*
					 * if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
					 * {
					 * String alert = (String) state.getAttribute(STATE_MESSAGE);
					 * if (alert == null || ((alert != null) && (alert.indexOf(rb.getString("notsupported")) == -1)))
					 * {
					 * addAlert(state, rb.getString("notsupported"));
					 * }
					 * }
					 */
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis15"));
				}
				catch (IdUnusedException e)
				{
					addAlert(state,rb.getString("notexist1"));
				}	// try-catch
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				state.setAttribute (STATE_COPY_FLAG, Boolean.TRUE.toString());

				copyItemsVector.addAll(Arrays.asList(copyItems));
				contentHostingService.eliminateDuplicates(copyItemsVector);
				state.setAttribute (STATE_COPIED_IDS, copyItemsVector);

			}	// if-else
		}	// if-else

	}	// doCopy

	/**
	* set the state name to be "deletecofirm" if any item has been selected for deleting
	*/
	public void doDeleteconfirm ( RunData data)
	{
		log.debug("{}.doDeleteconfirm()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		Set deleteIdSet  = new TreeSet();
		String[] deleteIds = data.getParameters ().getStrings ("selectedMembers");
		if (deleteIds == null)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile3"));
		}
		else
		{
			deleteIdSet.addAll(Arrays.asList(deleteIds));
			deleteItems(state, deleteIdSet);
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_MODE, MODE_DELETE_FINISH);
			state.removeAttribute(STATE_LIST_SELECTIONS);
		}


	}	// doDeleteconfirm

	public void doDispatchAction(RunData data)
	{
		log.debug("{}.doDispatchAction()", this);

		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		try
		{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		String action_string = params.getString("rt_action");
		String selectedItemId = params.getString("selectedItemId");
		
		String[] parts = action_string.split(ResourceToolAction.ACTION_DELIMITER);
		String typeId = parts[0];
		String actionId = parts[1];
		
		// ResourceType type = getResourceType(selectedItemId, state);
		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}

		ResourceType type = registry.getType(typeId);

		Reference reference = entityManager.newReference(contentHostingService.getReference(selectedItemId));

		ResourceToolAction action = type.getAction(actionId);

		if(action == null)
		{
			
		} else if (StringUtils.equals(actionId, "revise_permissions")) {
			ContentEntity entity = (ContentEntity) reference.getEntity();
			state.setAttribute("folder_name", entity.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			state.setAttribute("folder_group_reference", reference.getReference());
			state.setAttribute(STATE_MODE, MODE_PERMISSIONS);
		} else if(action instanceof InteractionAction)
		{
			ToolSession toolSession = sessionManager.getCurrentToolSession();
			// toolSession.setAttribute(ResourceToolAction.ACTION_ID, actionId);
			// toolSession.setAttribute(ResourceToolAction.RESOURCE_TYPE, typeId);
			
			state.setAttribute(STATE_CREATE_WIZARD_COLLECTION_ID, selectedItemId);
			
			ContentEntity entity = (ContentEntity) reference.getEntity();
			InteractionAction iAction = (InteractionAction) action;
			String intitializationId = iAction.initializeAction(reference);
			
			ResourceToolActionPipe pipe = registry.newPipe(intitializationId, action);
			pipe.setContentEntity(entity);
			pipe.setHelperId(iAction.getHelperId());
			
			toolSession.setAttribute(ResourceToolAction.ACTION_PIPE, pipe);

			ResourceProperties props = entity.getProperties();

			List propKeys = iAction.getRequiredPropertyKeys();
			if(propKeys != null)
			{
				Iterator it = propKeys.iterator();
				while(it.hasNext())
				{
					String key = (String) it.next();
					Object value = props.get(key);
					if(value == null)
					{
						// do nothing
					}
					else if(value instanceof String)
					{
						pipe.setResourceProperty(key, (String) value);
					}
					else if(value instanceof List)
					{
						pipe.setResourceProperty(key, (List) value);
					}
				}
			}
			
			if(entity.isResource())
			{
				pipe.setMimeType(((ContentResource) entity).getContentType());
				//pipe.setContentStream(((ContentResource) entity).streamContent());
				//pipe.setContent(((ContentResource) entity).getContent());
			}

			startHelper(data.getRequest(), iAction.getHelperId());
			conditionsHelper.loadConditionData(state);
		}
		else if(action instanceof ServiceLevelAction)
		{
			ServiceLevelAction sAction = (ServiceLevelAction) action;
			switch(sAction.getActionType())
			{
				case COPY:
					List<String> items_to_be_copied = new ArrayList<>();
					if(selectedItemId != null)
					{
						items_to_be_copied.add(selectedItemId);
					}
					state.removeAttribute(STATE_ITEMS_TO_BE_MOVED);
					state.setAttribute(STATE_ITEMS_TO_BE_COPIED, items_to_be_copied);
					break;
				case DUPLICATE:
					sAction.initializeAction(reference);
					String newId = duplicateItem(state, selectedItemId, contentHostingService.getContainingCollectionId(selectedItemId));
					if(newId == null)
					{
						sAction.cancelAction(reference);
					}
					else
					{
						reference = entityManager.newReference(contentHostingService.getReference(newId));
						sAction.finalizeAction(reference);
					}
					state.removeAttribute(STATE_ITEMS_TO_BE_MOVED);
					break;
				case DELETE:
					sAction.initializeAction(reference);
					deleteItem(state, selectedItemId);
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						// need new context
						state.setAttribute (STATE_MODE, MODE_DELETE_FINISH);
					}
					break;
				case MOVE:
					List<String> items_to_be_moved = new ArrayList<>();
					if(selectedItemId != null)
					{
						items_to_be_moved.add(selectedItemId);
					}
					state.removeAttribute(STATE_ITEMS_TO_BE_COPIED);
					state.setAttribute(STATE_ITEMS_TO_BE_MOVED, items_to_be_moved);
					break;
				case VIEW_METADATA:
					// sAction.initializeAction(reference);
					state.setAttribute(STATE_MORE_ID, selectedItemId);
					//state.setAttribute(STATE_MORE_ACTION, action);
					state.setAttribute (STATE_MODE, MODE_PROPERTIES);
					break;
				case REVISE_METADATA:
					sAction.initializeAction(reference);
					state.setAttribute(STATE_REVISE_PROPERTIES_ENTITY_ID, selectedItemId);
					state.setAttribute(STATE_REVISE_PROPERTIES_ACTION, action);
					state.setAttribute (STATE_MODE, MODE_REVISE_METADATA);
					ListItem item = getListItem(state);
					state.setAttribute(STATE_REVISE_PROPERTIES_ITEM, item);
					conditionsHelper.loadConditionData(state);
					// sAction.finalizeAction(reference);
					break;
				case CUSTOM_TOOL_ACTION:
					// do nothing
					break;
				case NEW_UPLOAD:
					break;
				case NEW_FOLDER:
					break;
				case NEW_URLS:
					break;
				case CREATE:
					break;
				case CREATE_BY_HELPER:
					break;
				case REVISE_CONTENT:
					break;
				case REPLACE_CONTENT:
					break;
				case PASTE_MOVED:
					//sAction.initializeAction(reference);
					pasteItem(state, selectedItemId);
					//sAction.finalizeAction(reference);
					break;
				case PASTE_COPIED:
					//sAction.initializeAction(reference);
					pasteItem(state, selectedItemId);
					//sAction.finalizeAction(reference);
					break;
				case REVISE_ORDER:
					sAction.initializeAction(reference);
					state.setAttribute(STATE_REORDER_FOLDER, selectedItemId);
					state.setAttribute(STATE_MODE, MODE_REORDER);
					sAction.finalizeAction(reference);
					break;
				case PRINT_FILE:
					printFile(state, data, selectedItemId);
					break;
				case MAKE_SITE_PAGE:
					// Stash the selected entity ID.
					state.setAttribute(STATE_MAKE_PAGE_ENTITY_ID, selectedItemId);
					state.setAttribute(STATE_MODE, MODE_MAKE_SITE_PAGE);
					state.removeAttribute(STATE_PAGE_TITLE); // Remove title if cancel was pressed.
					break;
				default:
					sAction.initializeAction(reference);
					sAction.finalizeAction(reference);
					break;
			}
			// not quite right for actions involving user interaction in Resources tool.
			// For example, with delete, this should be after the confirmation and actual deletion
			// Need mechanism to remember to do it later
			
		}
		}
		catch(Exception e)
		{
			log.warn("doDispatchAction ", e);
		}
	}
	
	/**
	 * Change the number of days for which individual dropboxes are highlighted in the instructor's list view.
	 */
	public void doSetHotDropbox(RunData data)
	{
		log.debug("{}.doSetHotDropbox()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		int dropboxHighlight = params.getInt("dropboxHighlight", 1);
		
		state.setAttribute(STATE_DROPBOX_HIGHLIGHT, dropboxHighlight);
	}

	public void doSetDropboxGroupIdFilter(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();
 
		String dropboxGroupFilter_groupId = params.getString("dropboxGroupFilter_groupId");
		
		if(dropboxGroupFilter_groupId != null)
		{
			state.setAttribute("dropboxGroupFilter_groupId", dropboxGroupFilter_groupId);
			Set groupUsers = null;
			try
			{
				Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
				if(StringUtils.isNotBlank(dropboxGroupFilter_groupId))
				{
					groupUsers = site.getGroup(dropboxGroupFilter_groupId).getUsers();
				}
			}
			catch(Exception e)
			{
				log.warn("Something went wrong", e);
				// something failed, hide group filter
				groupUsers = null;
			}
			state.setAttribute("dropboxGroupFilter_groupUsers", groupUsers);
		}
	}

	/**
	* Add the collection id into the expanded collection list
	 * @throws PermissionException
	 * @throws TypeException
	 * @throws IdUnusedException
	*/
	public void doExpand_collection(RunData data) throws IdUnusedException, TypeException, PermissionException
	{
		log.debug("{}.doExpand_collection()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		Set<String> expandedItems = getExpandedCollections(state);

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings ("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		String id = params.getString("collectionId");
		
		String refstr = contentHostingService.getReference(id);
		if(refstr != null)
		{
			Reference reference = entityManager.newReference(refstr);
			if(reference != null)
			{
				ContentEntity entity = (ContentEntity) reference.getEntity();
				String typeId = entity.getResourceType();
				ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
				if(typeId != null && registry != null)
				{
					ResourceType typeDef = registry.getType(typeId);
					if(typeDef != null && typeDef.isExpandable())
					{
						ServiceLevelAction expandAction = ((ExpandableResourceType) typeDef).getExpandAction();
						if(expandAction != null && expandAction.available(entity))
						{
							expandAction.initializeAction(reference);
							
							expandAction.finalizeAction(reference);
							
							expandedItems.add(id);
						}
					}
				}
			}
		}
		
	}	// doExpand_collection

	/**
	* Expand all the collection resources.
	*/
	public void doExpandall ( RunData data)
	{
		log.debug("{}.doExpandall()", this);
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		// expansion actually occurs in getBrowseItems method.
		state.setAttribute(STATE_EXPAND_ALL_FLAG,  Boolean.TRUE.toString());
		state.setAttribute(STATE_NEED_TO_EXPAND_ALL, Boolean.TRUE.toString());

	}	// doExpandall

	/**
	* doDelete to delete the selected collection or resource items
	*/
	public void doFinalizeDelete( RunData data)
	{
		log.debug("{}.doFinalizeDelete()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		String oldCollectionId = (String) state.getAttribute(STATE_COLLECTION_ID);
		
		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		List items = (List) state.getAttribute(STATE_DELETE_SET);

		// List deleteIds = (List) state.getAttribute (STATE_DELETE_IDS);

		// delete the lowest item in the hireachy first
		Map deleteItems = new HashMap();
		// String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		int maxDepth = 0;
		int depth;

		Iterator it = items.iterator();
		while(it.hasNext())
		{
			ListItem item = (ListItem) it.next();
			String[] parts = item.getId().split(Entity.SEPARATOR);
			depth = parts.length;
			if (depth > maxDepth)
			{
				maxDepth = depth;
			}
			List v = (List) deleteItems.get(depth);
			if(v == null)
			{
				v = new ArrayList();
			}
			v.add(item);
			deleteItems.put(depth, v);
		}

		boolean isCollection = false;
		for (int j=maxDepth; j>0; j--)
		{
			List v = (List) deleteItems.get(j);
			if (v==null)
			{
				v = new ArrayList();
			}
			ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
			Iterator itemIt = v.iterator();
			while(itemIt.hasNext())
			{
				ListItem item = (ListItem) itemIt.next();
				try
				{
				    ResourceType typeDef = registry.getType(item.getResourceType());
				    ResourceToolAction action = typeDef.getAction(ResourceToolAction.DELETE);

				    if (action instanceof ServiceLevelAction) {
				        ServiceLevelAction slAction = (ServiceLevelAction) action;
				        slAction.finalizeAction(entityManager.newReference(contentHostingService.getReference(item.getId())));
				    }
					if (item.isCollection())
					{
						if (oldCollectionId.equals(item.getId())) {
							state.setAttribute(STATE_COLLECTION_ID, item.getParent().getId());
							log.debug("set current collection to parent: {}", item.getParent().getId());
						}
						contentHostingService.removeCollection(item.getId());
					}
					else
					{
						contentHostingService.removeResource(item.getId());
					}
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getFormattedMessage("notpermis6", new Object[]{item.getName()}));
				}
				catch (IdUnusedException e)
				{
					
					addAlert(state,rb.getString("notexist1"));
				}
				catch (TypeException e)
				{
					addAlert(state, rb.getFormattedMessage("deleteresType", new Object[]{item.getName()}));
				}
				catch (ServerOverloadException e)
				{
					addAlert(state, rb.getString("failed"));
				}
				catch (InUseException e)
				{
					addAlert(state, rb.getFormattedMessage("deleteresLocked", new Object[]{item.getName()}));
				}// try - catch
				catch(RuntimeException e)
				{
					log.debug("ResourcesAction.doDelete ***** Unknown Exception ***** {}", e.getMessage());
					addAlert(state, rb.getString("failed"));
				}
			}	// for

		}	// for

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// delete sucessful
			state.setAttribute (STATE_MODE, MODE_LIST);
			state.removeAttribute(STATE_DELETE_SET);
			state.removeAttribute(STATE_NON_EMPTY_DELETE_SET);

			if (((String) state.getAttribute (STATE_SELECT_ALL_FLAG)).equals (Boolean.TRUE.toString()))
			{
				state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());
			}

		}	// if-else

	}	// doDelete

	/**
	 * @param data
	 */
	public void doHideOtherSites(RunData data)
	{
		log.debug("{}.doHideOtherSites()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_SHOW_OTHER_SITES, Boolean.FALSE.toString());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

	}

	public void doHierarchy(RunData data)
	{
		log.debug("{}.doHierarchy()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.setAttribute(STATE_LIST_PREFERENCE, LIST_HIERARCHY);
	}

	/**
	* Expand all the collection resources and put in EXPANDED_COLLECTIONS attribute.
	*/
	public void doList ( RunData data)
	{
		log.debug("{}.doList()", this);
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute (STATE_MODE, MODE_LIST);

	}	// doList

	/**
	* Handle user's selection of items to be moved.
	*/
	public void doMove ( RunData data )
	{
		log.debug("{}.doMove()", this);
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		List moveItemsVector = new ArrayList();

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		String[] moveItems = data.getParameters ().getStrings ("selectedMembers");
		if (moveItems == null)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile6"));
			state.setAttribute (STATE_MODE, MODE_LIST);
		}
		else
		{
			String moveId;
			for(String moveItem : moveItems)
			{
				moveId = moveItem;
				try
				{
					ResourceProperties properties = contentHostingService.getProperties (moveId);
					/*
					 * if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
					 * {
					 * String alert = (String) state.getAttribute(STATE_MESSAGE);
					 * if (alert == null || ((alert != null) && (alert.indexOf(rb.getString("notsupported")) == -1)))
					 * {
					 * addAlert(state, rb.getString("notsupported"));
					 * }
					 * }
					 */
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis15"));
				}
				catch (IdUnusedException e)
				{
					addAlert(state,rb.getString("notexist1"));
				}	// try-catch
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				state.setAttribute (STATE_MOVE_FLAG, Boolean.TRUE.toString());

				moveItemsVector.addAll(Arrays.asList(moveItems));

				contentHostingService.eliminateDuplicates(moveItemsVector);

				state.setAttribute (STATE_MOVED_IDS, moveItemsVector);

			}	// if-else
		}	// if-else

	}	// doMove
	
	public void doMultiItemDispatch ( RunData data )
	{
		log.debug("{}.doMultiItemDispatch()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		ParameterParser params = data.getParameters();
		
		String actionId = params.getString("rt_action");
		
		if(actionId == null)
		{
			
		}
		else if(ResourceToolAction.COPY.equals(actionId))
		{
			List selectedSet  = new ArrayList();
			String[] selectedItems = params.getStrings ("selectedMembers");
			if(selectedItems != null)
			{
				selectedSet.addAll(Arrays.asList(selectedItems));
			}
			
			state.setAttribute(STATE_ITEMS_TO_BE_COPIED, selectedSet);
			state.removeAttribute(STATE_ITEMS_TO_BE_MOVED);
		}
		else if(ResourceToolAction.MOVE.equals(actionId))
		{
			List selectedSet  = new ArrayList();
			String[] selectedItems = params.getStrings ("selectedMembers");
			if(selectedItems != null)
			{
				selectedSet.addAll(Arrays.asList(selectedItems));
			}
			
			state.setAttribute(STATE_ITEMS_TO_BE_MOVED, selectedSet);
			state.removeAttribute(STATE_ITEMS_TO_BE_COPIED);
		}
		else if(ResourceToolAction.DELETE.equals(actionId))
		{
			doDeleteconfirm(data);
		}
		else if(ResourceToolAction.SHOW.equals(actionId))
		{
			doShowconfirm(data);
		}
		else if(ResourceToolAction.HIDE.equals(actionId))
		{
			doHideconfirm(data);
		}
		else if(ResourceToolAction.COPY_OTHER.equals(actionId))
		{
			List<String> selectedSet  = new ArrayList<>();
			String[] selectedItems = params.getStrings("selectedMembers-other");
			if(selectedItems != null)
			{
				selectedSet.addAll(Arrays.asList(selectedItems));
			}
			state.setAttribute(STATE_ITEMS_TO_BE_COPIED, selectedSet);
			state.removeAttribute(STATE_ITEMS_TO_BE_MOVED);
		}
		else if(ResourceToolAction.ZIPDOWNLOAD.equals(actionId))
		{
			doZipDownloadconfirm(data);
		}
	}

	/**
	* Navigate in the resource hireachy
	*/
	public void doNavigate ( RunData data )
	{
		log.debug("{}.doNavigate()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		if (state.getAttribute (STATE_SELECT_ALL_FLAG)!=null && state.getAttribute (STATE_SELECT_ALL_FLAG).equals (Boolean.TRUE.toString()))
		{
			state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());
		}

		if (state.getAttribute (STATE_EXPAND_ALL_FLAG)!=null && state.getAttribute (STATE_EXPAND_ALL_FLAG).equals (Boolean.TRUE.toString()))
		{
			state.setAttribute (STATE_EXPAND_ALL_FLAG, Boolean.FALSE.toString());
		}

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = data.getParameters ().getStrings ("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		String collectionId = data.getParameters().getString ("collectionId");
		String navRoot = data.getParameters().getString("navRoot");
		state.setAttribute(STATE_NAVIGATION_ROOT, navRoot);

		// the exception message

		try
		{
			contentHostingService.checkCollection(collectionId);
		}
		catch(PermissionException e)
		{
			addAlert(state, rb.getString("notpermis3"));
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("notexist2"));
		}
		catch (TypeException e)
		{
			addAlert(state, rb.getString("notexist2"));
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			String oldCollectionId = (String) state.getAttribute(STATE_COLLECTION_ID);

			state.setAttribute(STATE_COLLECTION_ID, collectionId);
			
			Set<String> expandedCollections = getExpandedCollections(state);
			
			Map sortMap = Collections.synchronizedMap(setStateAttributeExpandedFolderSortMap(state));
			
			// sync over sortMap removal
			Iterator it = expandedCollections.iterator();
			synchronized (sortMap)
			{
				while(it.hasNext())
				{
					String id = (String) it.next();
					if(id.startsWith(collectionId))
					{
						sortMap.remove(id);
					}
				}
			}
			
			SortedSet<String> newExpandedCollections = new TreeSet<>();
			for(String id: expandedCollections)
				{
				if(!id.startsWith(collectionId))
					{
					newExpandedCollections.add(id);
				}
			}
			newExpandedCollections.add(collectionId);
			
			expandedCollections.clear();
			expandedCollections.addAll(newExpandedCollections);
		}

	}	// doNavigate

	/**
	 * get/init state attribute STATE_EXPANDED_FOLDER_SORT_MAP
	 * @param state
	 * @return
	 */
	private static Map setStateAttributeExpandedFolderSortMap(SessionState state) {
		Map sortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
		if(sortMap == null)
		{
			sortMap = new HashMap();
			state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, sortMap);
		}
		return sortMap;
	}

	/**
	 * get/init state attribute STATE_EXPANDED_COLLECTIONS
	 * @param state
	 * @return An {@link Set} but never <code>null</code>.
	 */
	private static Set<String> getExpandedCollections(SessionState state) {
		Set<String> current = (Set<String>) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(current == null)
		{
			// We use a CopyOnWrite Set so that we don't have to do any sychronization when iterating over it.
			// Switching to HashSet results in runaway threads and concurrentmodificationsexceptions (from iterating).
			current = new CopyOnWriteArraySet<>();
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, current);
		}
		return current;
	}

	/**
	* Fire up the permissions editor for the tool's permissions
	*/
	public void doPermissions(RunData data, Context context)
	{
		log.debug("{}.doPermissions()", this);

		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		// should we save here?
		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		String siteCollectionId = contentHostingService.getSiteCollection(toolManager.getCurrentPlacement().getContext());
		try {
			ContentCollection siteCollection = contentHostingService.getCollection(siteCollectionId);
			String folderName = contentHostingService.getCollection(siteCollectionId).getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			state.setAttribute("folder_name", folderName);
			state.setAttribute("folder_group_reference", siteCollection.getReference());
		} catch (Exception e) {
			log.error("Failed to set folder_name and folder_group_reference: {}", e.toString());
		}

		state.setAttribute (STATE_MODE, MODE_PERMISSIONS);
	}	// doPermissions

	/**
	* Sort based on the given property
	*/
	public void doReorder ( RunData data)
	{
		log.debug("{}.doReorder()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();
		
		String folderId = params.getString ("folderId");
		if(folderId == null)
		{
			addAlert(state, "error");
		}
		
		String sortBy = (String) state.getAttribute(STATE_REORDER_SORT_BY);
		if(sortBy == null)
		{
			sortBy = ResourceProperties.PROP_CONTENT_PRIORITY;
			state.setAttribute(STATE_REORDER_SORT_BY, sortBy);
		}
		String sortedAsc = (String) state.getAttribute (STATE_REORDER_SORT_ASC);
		if(sortedAsc == null)
		{
			sortedAsc = Boolean.TRUE.toString();
			state.setAttribute(STATE_REORDER_SORT_ASC, sortedAsc);
		}

		Comparator comparator = contentHostingService.newContentHostingComparator(sortBy, Boolean.getBoolean(sortedAsc));
		state.setAttribute(STATE_REORDER_SORT, comparator);

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_REORDER_FOLDER, folderId);
			state.setAttribute (STATE_MODE, MODE_REORDER);

		}	// if-else

	}	// doReorder

	public void doReviseProperties(RunData data)
	{
		log.debug("{}.doReviseProperties()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		String user_action = params.getString("user_action");
		
		if("save".equals(user_action))
		{
			String entityId = (String) state.getAttribute(STATE_REVISE_PROPERTIES_ENTITY_ID);
			ListItem item = (ListItem) state.getAttribute(STATE_REVISE_PROPERTIES_ITEM);
			ResourceToolAction action = (ResourceToolAction) state.getAttribute(STATE_REVISE_PROPERTIES_ACTION);
		
			if(item == null)
			{
				
			}
			item.captureProperties(params, ListItem.DOT + "0");
			if (item.numberFieldIsInvalid) {
				addAlert(state, rb.getString("conditions.invalid.condition.argument"));
				return;
			}
			if (item.numberFieldIsOutOfRange) {
				addAlert(state, rb.getFormattedMessage("conditions.condition.argument.outofrange", new Object[] { item.getConditionAssignmentPoints() }));
				return;
			}
			if(!"".equals(item.metadataValidationFails)) {
				addAlert(state, metaLang.getFormattedMessage("metadata.validation.error", item.metadataValidationFails));
				return;
			}
			//Control if groups are selected
			if (!checkGroups(params)) { 
				addAlert(state, trb.getString("alert.youchoosegroup")); 
				return;
			}
			
			// notification
			int noti = NotificationService.NOTI_NONE;
			// %%STATE_MODE_RESOURCES%%
			if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
			{
				boolean notification;
				
				if(item.userIsMaintainer())	// if the user is a site maintainer
				{
					notification = params.getBoolean("notify_dropbox");
	  				if(notification)
	   				{
	   					noti = NotificationService.NOTI_REQUIRED;
	   				}
				}
				else
				{
					String notifyDropbox = getDropboxNotificationsProperty();
					if(DROPBOX_NOTIFICATIONS_ALWAYS.equals(notifyDropbox))
					{
						noti = NotificationService.NOTI_OPTIONAL;
					}
					else if(DROPBOX_NOTIFICATIONS_ALLOW.equals(notifyDropbox))
					{
						notification = params.getBoolean("notify_dropbox");
		  				if(notification)
		   				{
		   					noti = NotificationService.NOTI_OPTIONAL;
		   				}
					}
				}
				log.debug("{}.doReviseProperties() noti == {}", this, noti);
			}
			else
			{
				// read the notification options
				String notification = params.getString("notify");
				if ("r".equals(notification))
				{
					noti = NotificationService.NOTI_REQUIRED;
				}
				else if ("o".equals(notification))
				{
					noti = NotificationService.NOTI_OPTIONAL;
				}
			}
			
			List<String> alerts = item.checkRequiredProperties(copyrightManager);

			if(alerts.isEmpty())
			{
				try 
				{
					ResourceConditionsHelper.saveCondition(item, params, state, 0);
					
					Entity entity;
					if(item.isCollection())
					{
						entity = contentHostingService.editCollection(entityId);
						item.updateContentCollectionEdit((ContentCollectionEdit)entity);
						
						contentHostingService.commitCollection((ContentCollectionEdit)entity);
					}
					else
					{
						entity = contentHostingService.editResource(entityId);
						item.updateContentResourceEdit((ContentResourceEdit)entity);
						contentHostingService.commitResource((ContentResourceEdit)entity, noti);
					}
					
					if (action instanceof ServiceLevelAction) {
					    ServiceLevelAction slAction = (ServiceLevelAction) action;
					    slAction.finalizeAction(entityManager.newReference(contentHostingService.getReference(item.getId())));
					}

					ResourceConditionsHelper.notifyCondition(entity);
					state.setAttribute(STATE_MODE, MODE_LIST);
				} 
				catch (IdUnusedException e) 
				{
					log.warn("IdUnusedException", e);
				} 
				catch (TypeException e) 
				{
					log.warn("TypeException", e);
				} 
				catch (PermissionException e) 
				{
					log.warn("PermissionException", e);
				} 
				catch (ServerOverloadException e) 
				{
					log.warn("ServerOverloadException", e);
				}
				catch (OverQuotaException e)
				{
					// TODO Auto-generated catch block
					log.warn("OverQuotaException ", e);
				}
				catch (InUseException e)
				{
					// TODO Auto-generated catch block
					log.warn("InUseException ", e);
				}
			}
			else
			{
				for(String alert : alerts)
				{
					addAlert(state, alert);
				}
			}
			
		}
		else if("cancel".equals(user_action))
		{
			state.setAttribute(STATE_MODE, MODE_LIST);
		}
	}

	/**
	* Restore the files based on the selection
	*/
	public void doRestore( RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		String flow = params.getString("flow");
		
		if (!"cancel".equalsIgnoreCase(flow)) {
			String[] selectedItems = params.getStrings("selectedMembers");	
			StringBuilder restoredResources = new StringBuilder();
			StringBuilder removedResources = new StringBuilder();
			if ("restore".equalsIgnoreCase(flow))
			{
				for (String selectedItem : selectedItems) {
					try {
						contentHostingService.restoreResource(selectedItem);
						restoredResources.append( selectedItem ).append(";");
					} catch (Exception e) {
						String[] args = { e.getClass().getName(), selectedItem, e.getMessage()};
						addAlert(state, trb.getFormattedMessage("action.exception", (Object[]) args));
						log.error("Unable to restore recourse with ID {}", selectedItem, e);
					}
				}
			} 
			else if ("remove".equalsIgnoreCase(flow))
			{
				for (String selectedItem : selectedItems) {
					try {
						contentHostingService.removeDeletedResource(selectedItem);
						removedResources.append( selectedItem ).append(";");
					} catch (Exception ex) {
						String[] args = {ex.getClass().getName(),selectedItem, ex.getMessage() };
						addAlert(state, trb.getFormattedMessage("action.exception", (Object[]) args));
						log.error("Unable to permanently remove recourse with ID {}", selectedItem, ex);
					}
				}				
			}
			
			if (restoredResources.length() > 0)
			{
				state.setAttribute("restored_resources", restoredResources.toString());
			}
			if (removedResources.length() > 0)
			{
				state.setAttribute("removed_resources", removedResources.toString());
			}
		}
		else
		{
			// cancel and go back
			state.setAttribute (STATE_MODE, MODE_LIST);
		}
	}

	/**
	* Sort based on the given property
	*/
	public void doSaveOrder ( RunData data)
	{
		log.debug("{}.doSaveOrder()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		String flow = params.getString("flow");
		
		if("save".equalsIgnoreCase(flow))
		{
			String folderId = params.getString ("folderId");
			if(folderId == null)
			{
				// TODO: log error
				// TODO: move strings to rb
				addAlert(state, trb.getString("alert.nosort"));
			}
			else
			{
				try
				{
					ContentCollectionEdit collection = contentHostingService.editCollection(folderId);
					List memberIds = collection.getMembers();
					Map priorities = new HashMap();
					Iterator it = memberIds.iterator();
					while(it.hasNext())
					{
						String memberId = (String) it.next();
						int position = params.getInt("position_" + Validator.escapeUrl(memberId));
						priorities.put(memberId, position);
					}
					collection.setPriorityMap(priorities);
					
					contentHostingService.commitCollection(collection);
					
					Set<String> expandedCollections = getExpandedCollections(state);
					expandedCollections.add(folderId);
					
					Comparator comparator = contentHostingService.newContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, true);
					Map expandedFolderSortMap = setStateAttributeExpandedFolderSortMap(state);
					expandedFolderSortMap.put(folderId, comparator);
				}
				catch(IdUnusedException e)
				{
					addAlert(state, trb.getString("alert.nosort"));
					log.warn("IdUnusedException{}", (Object) e);
				}
				catch(TypeException e)
				{
					addAlert(state, trb.getString("alert.nosort"));
					log.warn("TypeException{}", (Object) e);
				}
				catch(PermissionException e)
				{
					addAlert(state, trb.getString("alert.nosort"));
					log.warn("PermissionException{}", (Object) e);
				}
				catch(InUseException e)
				{
					addAlert(state, trb.getString("alert.nosort"));
					log.warn("InUseException{}", (Object) e);
				}
			}
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_MODE, MODE_LIST);

		}	// if-else

	}	// doSaveOrder

	/**
	* Show information about WebDAV
	*/
	public void doShow_webdav ( RunData data )
	{
		log.debug("{}.doShow_webdav()", this);
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		state.setAttribute (STATE_MODE, MODE_DAV);

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

	}	// doShow_webdav

	/**
	* Show information about WebDAV
	*/
	public void doShowQuota ( RunData data )
	{
		log.debug("{}.doShowQuota()", this);
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		state.setAttribute (STATE_MODE, MODE_QUOTA);

	}	// doShowQuota		


	public void doShowMembers(RunData data)
	{
		log.debug("{}.doShowMembers()", this);

		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		// get the item to be expanded
		String itemId = params.getString("item");
		if(itemId != null)
		{
			// put the itemId into state
			state.setAttribute(STATE_COLUMN_ITEM_ID, itemId);
		}
	}

	/**
	 * @param data
	 */
	public void doShowOtherSites(RunData data)
	{
		log.debug("{}.doShowOtherSites()", this);

		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		state.setAttribute(STATE_SHOW_OTHER_SITES, Boolean.TRUE.toString());
	}

	/**
	* Sort based on the given property
	*/
	public void doSort ( RunData data)
	{
		log.debug("{}.doSort()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = data.getParameters ().getStrings ("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		String criteria = params.getString ("criteria");

		if ("title".equals(criteria))
		{
			criteria = ResourceProperties.PROP_DISPLAY_NAME;
		}
		else if ("size".equals(criteria))
		{
			criteria = ResourceProperties.PROP_CONTENT_LENGTH;
		}
		else if ("created by".equals(criteria))
		{
			criteria = ResourceProperties.PROP_CREATOR;
		}
		else if ("last modified".equals(criteria))
		{
			criteria = ResourceProperties.PROP_MODIFIED_DATE;
		}
		else if ("priority".equals(criteria) && contentHostingService.isSortByPriorityEnabled())
		{
			// if error, use title sort
			criteria = ResourceProperties.PROP_CONTENT_PRIORITY;
		}
		else
		{
			criteria = ResourceProperties.PROP_DISPLAY_NAME;
		}

		String sortBy_attribute = STATE_SORT_BY;
		String sortAsc_attribute = STATE_SORT_ASC;
		String comparator_attribute = STATE_LIST_VIEW_SORT;
		
		if(state.getAttribute(STATE_MODE).equals(MODE_REORDER))
		{
			sortBy_attribute = STATE_REORDER_SORT_BY;
			sortAsc_attribute = STATE_REORDER_SORT_ASC;
			comparator_attribute = STATE_REORDER_SORT;
		}
		// current sorting sequence
		String asc;
		boolean bValue;
		if (!criteria.equals (state.getAttribute (sortBy_attribute)))
		{
			state.setAttribute (sortBy_attribute, criteria);
			asc = Boolean.TRUE.toString();
			bValue = true;
			state.setAttribute (sortAsc_attribute, asc);
		}
		else
		{
			// current sorting sequence
			asc = (String) state.getAttribute (sortAsc_attribute);

			//toggle between the ascending and descending sequence
			bValue = !asc.equals (Boolean.TRUE.toString());
			asc = Boolean.toString(bValue);
			state.setAttribute (sortAsc_attribute, asc);
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			Comparator comparator = contentHostingService.newContentHostingComparator(criteria, bValue);
			state.setAttribute(comparator_attribute, comparator);
			
			// sort sucessful
			// state.setAttribute (STATE_MODE, MODE_LIST);

		}	// if-else

	}	// doSort

	/**
	* Unexpand all the collection resources
	*/
	public void doUnexpandall ( RunData data)
	{
		log.debug("{}.doUnexpandall()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings ("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		getExpandedCollections(state).clear();
		state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, new HashMap());
		
		// TODO: Should iterate over all collectionId's in expandedCollection 
		//       and call collapseAction.initializeAction() and 
		//       collapseAction.finalizeAction() for each one.
		
		state.setAttribute(STATE_EXPAND_ALL_FLAG, Boolean.FALSE.toString());

	}	// doUnexpandall
	
	/**
	* Read user inputs from options form and update accordingly
	*/
	public void doUpdateOptions(RunData data)
	{
		log.debug("{}.doUpdateOptions()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}

		List<ResourceType> typeDefs = new ArrayList<>(registry.getTypes());

		String siteId = params.getString("siteId");
		if(StringUtils.isBlank(siteId))
		{
			String home = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
			Reference ref = entityManager.newReference(contentHostingService.getReference(home));
			siteId = ref.getContext();
		}
		
		Map<String,Boolean> statusMap = new HashMap<>();

		String[] types = params.getStrings("types");
		SortedSet enabledTypes = new TreeSet();
		if(types != null)
		{
			enabledTypes.addAll(Arrays.asList(types));
		}

		for(ResourceType typeDef : typeDefs)
		{
			if(typeDef instanceof SiteSpecificResourceType)
			{
				statusMap.put(typeDef.getId(), enabledTypes.contains(typeDef.getId()));
			}
		}
		registry.setMapOfResourceTypesForContext(siteId, statusMap);
		
		state.setAttribute(STATE_MODE, MODE_LIST);

	}
	
	/**
	 * cancel out of options mode
	 */
	public void doCancelOptions(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		log.debug("{}.doCancelOptions()", this);
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_MODE, MODE_LIST);
		
	}
	
	/**
	* Find the resource with this id in the list.
	* @param id The message id.
	* @param resources list of resources.
	* @return The index position in the list of the message with this id, or -1 if not found.
	*/
	protected int findResourceInList(List resources, String id)
	{
		log.debug("{}.findResourceInList()", this);
		for (int i = 0; i < resources.size(); i++)
		{
			// if this is the one, return this index
			if (((ListItem) (resources.get(i))).getId().equals(id)) return i;
		}

		// not found
		return -1;

	}	// findResourceInList
	
	/**
	 * @param state
	 * @param toolSession
	 * @param pipe
	 */
	protected void finishAction(SessionState state, ToolSession toolSession, ResourceToolActionPipe pipe)
	{
		log.debug("{}.finishAction()", this);
		if(pipe.isErrorEncountered())
		{
			String msg = pipe.getErrorMessage();
			if(StringUtils.isBlank(msg))
			{
				msg = trb.getString("alert.unknown");
			}
			addAlert(state, msg);
		}
		
		ResourceToolAction action = pipe.getAction();

		// use ActionType for this 
		switch(action.getActionType())
		{
		case CREATE:
			state.setAttribute(STATE_MODE, MODE_CREATE_WIZARD);
			break;
		case NEW_UPLOAD:
			List<ContentResource> resources = createResources(pipe);
			if(resources != null && ! resources.isEmpty())
			{
				// expand folder
				Set<String> expandedCollections = getExpandedCollections(state);
				expandedCollections.add(pipe.getContentEntity().getId());
			}
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			state.setAttribute(STATE_MODE, MODE_LIST);
			break;
		case NEW_FOLDER:
			List<ContentCollection> folders = createFolders(state, pipe);
			if(folders != null && ! folders.isEmpty())
			{
				// expand folder
				Set<String> expandedCollections = getExpandedCollections(state);
				expandedCollections.add(pipe.getContentEntity().getId());
			}
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			state.setAttribute(STATE_MODE, MODE_LIST);
			break;
		case NEW_URLS:
			List<ContentResource> urls = createUrls(state, pipe);
			if(urls == null || urls.isEmpty())
			{
				// add an alert and return to the addUrl view?
			}
			else
			{
				// expand folder
				Set<String> expandedCollections = getExpandedCollections(state);
				expandedCollections.add(pipe.getContentEntity().getId());
				toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			}
			state.setAttribute(STATE_MODE, MODE_LIST);
			break;
		case CREATE_BY_HELPER:
			if(!pipe.isErrorEncountered() && !pipe.isActionCanceled()) {
				Set<String> expandedCollections = getExpandedCollections(state);
				expandedCollections.add(pipe.getContentEntity().getId());
			}
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			state.setAttribute(STATE_MODE, MODE_LIST);
			break;
		case REVISE_CONTENT:
			reviseContent(pipe);
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			state.setAttribute(STATE_MODE, MODE_LIST);
			break;
		case REPLACE_CONTENT:
			replaceContent(pipe);
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			state.setAttribute(STATE_MODE, MODE_LIST);
			break;
		default:
			state.setAttribute(STATE_MODE, MODE_LIST);
			break;
		}
		if(toolSession.getAttribute(STATE_MESSAGE_LIST) != null)
		{
			Collection<String> messages = (Collection<String>) toolSession.getAttribute(STATE_MESSAGE_LIST);
			for(String msg : messages)
			{
				addAlert(state, msg);
			}
			toolSession.removeAttribute(STATE_MESSAGE_LIST);
		}
	}

	protected void replaceContent(ResourceToolActionPipe pipe) 
	{
		log.debug("{}.replaceContent()", this);
		ResourceToolAction action = pipe.getAction();
		ContentEntity entity = pipe.getContentEntity();
		try
		{
			ContentResourceEdit edit = contentHostingService.editResource(entity.getId());
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			// update content
			extractContent(pipe, edit);
			
			// update properties
			if(action instanceof InteractionAction)
			{
				InteractionAction iAction = (InteractionAction) action;
				Map revprops = pipe.getRevisedResourceProperties();
				List propkeys = iAction.getRequiredPropertyKeys();
				if(propkeys != null)
				{
					Iterator keyIt = propkeys.iterator();
					while(keyIt.hasNext())
					{
						String key = (String) keyIt.next();
						String value = (String) revprops.get(key);
						if(value == null)
						{
							props.removeProperty(key);
						}
						else
						{
							// should we support multivalued properties?
							props.addProperty(key, value);
						}
					}
				}
			}
			
			int notification = NotificationService.NOTI_NONE;
			Object obj = pipe.getRevisedListItem();
			if(obj instanceof ListItem)
			{
				notification = ((ListItem) obj).getNotification();
			}
			
			// update mimetype
			edit.setContentType(pipe.getRevisedMimeType());
			contentHostingService.commitResource(edit, notification);
		}
		catch (PermissionException e)
		{
			pipe.setErrorEncountered(true);
			pipe.setErrorMessage(trb.getString("alert.noperm"));
			addAlert(pipe.getErrorMessage());
			log.warn("PermissionException {}", (Object) e);
		}
		catch (IdUnusedException e)
		{
			pipe.setErrorEncountered(true);
			pipe.setErrorMessage(trb.getString("alert.unknown"));
			addAlert(pipe.getErrorMessage());
			log.warn("IdUnusedException ", e);
		}
		catch (TypeException e)
		{
			pipe.setErrorEncountered(true);
			pipe.setErrorMessage(trb.getString("alert.unknown"));
			addAlert(pipe.getErrorMessage());
			log.warn("TypeException ", e);
		}
		catch (InUseException e)
		{
			pipe.setErrorEncountered(true);
			pipe.setErrorMessage(trb.getString("alert.unknown"));
			addAlert(pipe.getErrorMessage());
			log.warn("InUseException ", e);
		}
		catch (OverQuotaException e)
		{
			pipe.setErrorEncountered(true);
			pipe.setErrorMessage(trb.getString("alert.quota"));
			addAlert(trb.getString("alert.quota"));
			log.warn("OverQuotaException {}", (Object) e);
		}
		catch (ServerOverloadException e)
		{
			pipe.setErrorEncountered(true);
			pipe.setErrorMessage(trb.getString("alert.unable"));
			addAlert(trb.getString("alert.unable"));
			log.warn("ServerOverloadException ", e);
		}
		catch (VirusFoundException e) {
			pipe.setErrorEncountered(true);
			pipe.setErrorMessage(trb.getFormattedMessage("alert.virusfound", new Object[]{e.getMessage()}));
			addAlert(trb.getFormattedMessage("alert.virusfound", new Object[]{e.getMessage()}));
		}
	}

	/**
	* Populate the state object, if needed - override to do something!
	*/
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData data)
	{
		log.debug("{}.initState()", this);
		super.initState(state, portlet, data);
		
		if(state.getAttribute(STATE_INITIALIZED) == null)
		{
			initCopyContext(state);
			initMoveContext(state);
		}

		initStateAttributes(state, portlet);

	}	// initState

	public void initStateAttributes(SessionState state, VelocityPortlet portlet)
	{
		log.debug("{}.initStateAttributes()", this);
		if (state.getAttribute (STATE_INITIALIZED) != null) return;

		if (state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE) == null)
		{
			String uploadMax = ServerConfigurationService.getString(ResourcesConstants.SAK_PROP_MAX_UPLOAD_FILE_SIZE);
			String uploadCeiling = ServerConfigurationService.getString("content.upload.ceiling");
			
			if(uploadMax == null && uploadCeiling == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, ResourcesConstants.DEFAULT_MAX_FILE_SIZE_STRING);
			}
			else if(uploadCeiling == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, uploadMax);
			}
			else if(uploadMax == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, null);
			}
			else
			{
				int maxNum = Integer.MAX_VALUE;
				int ceilingNum = Integer.MAX_VALUE;
				try
				{
					maxNum = Integer.parseInt(uploadMax);
				}
				catch(Exception e)
				{
				}
				try
				{
					ceilingNum = Integer.parseInt(uploadCeiling);
				}
				catch(Exception e)
				{
				}

				if(ceilingNum < maxNum)
				{
					state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, uploadCeiling);
				}
				else
				{
					state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, uploadMax);
				}
			}
			
		}
		
		PortletConfig config = portlet.getPortletConfig();
		try
		{
			Integer size = Integer.valueOf(config.getInitParameter(PARAM_PAGESIZE));
			if(size == null || size < 1)
			{
				size = DEFAULT_PAGE_SIZE;
			}
			state.setAttribute(STATE_PAGESIZE, size);
		}
		catch(Exception any)
		{
			state.setAttribute(STATE_PAGESIZE, DEFAULT_PAGE_SIZE);
		}

		// state.setAttribute(STATE_TOP_PAGE_MESSAGE_ID, "");

		state.setAttribute (STATE_CONTENT_SERVICE, contentHostingService);
		state.setAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE, contentTypeImageService);
		state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry"));

		if(state.getAttribute(STATE_MODE) == null)
		{
			state.setAttribute (STATE_MODE, MODE_LIST);
			state.setAttribute (STATE_FROM, NULL_STRING);
		}
		state.setAttribute (STATE_SORT_BY, ResourceProperties.PROP_DISPLAY_NAME);

		state.setAttribute (STATE_SORT_ASC, Boolean.TRUE.toString());
		
		state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());

		state.setAttribute (STATE_EXPAND_ALL_FLAG, Boolean.FALSE.toString());

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		state.setAttribute (STATE_COLLECTION_PATH, new ArrayList ());

		// %%STATE_MODE_RESOURCES%%
		// In helper mode, calling tool should set attribute STATE_MODE_RESOURCES
		String resources_mode = (String) state.getAttribute(STATE_MODE_RESOURCES);
		if(resources_mode == null)
		{
			// get resources mode from tool registry
			resources_mode = portlet.getPortletConfig().getInitParameter("resources_mode");
			if(resources_mode != null)
			{
				state.setAttribute(STATE_MODE_RESOURCES, resources_mode);
			}
		}

		boolean show_other_sites;
		if(RESOURCES_MODE_HELPER.equals(resources_mode))
		{
			show_other_sites = ServerConfigurationService.getBoolean(SAK_PROP_SHOW_ALL_SITES_IN_HELPER, SHOW_ALL_SITES_IN_FILE_PICKER);
		}
		else if(RESOURCES_MODE_DROPBOX.equals(resources_mode))
		{
			show_other_sites = ServerConfigurationService.getBoolean(SAK_PROP_SHOW_ALL_SITES_IN_DROPBOX, SHOW_ALL_SITES_IN_DROPBOX);
		}
		else
		{
			show_other_sites = ServerConfigurationService.getBoolean(SAK_PROP_SHOW_ALL_SITES_IN_TOOL, SHOW_ALL_SITES_IN_RESOURCES);
		}
		
		/** set attribute for the maximum size at which the resources tool will expand a collection. */
		int expandableFolderSizeLimit = ServerConfigurationService.getInt("resources.expanded_folder_size_limit", EXPANDABLE_FOLDER_SIZE_LIMIT);
		state.setAttribute(STATE_EXPANDABLE_FOLDER_SIZE_LIMIT, expandableFolderSizeLimit);
		
		/** This attribute indicates whether "Other Sites" twiggle should show */
		state.setAttribute(STATE_SHOW_ALL_SITES, Boolean.toString(show_other_sites));
		/** This attribute indicates whether "Other Sites" twiggle should be open */
		state.setAttribute(STATE_SHOW_OTHER_SITES, Boolean.FALSE.toString());

		// set the home collection to the parameter, if present, or the default if not
		String home = StringUtils.trimToNull(portlet.getPortletConfig().getInitParameter("home"));
		state.setAttribute (STATE_HOME_COLLECTION_DISPLAY_NAME, home);
		if ((home == null) || (home.length() == 0))
		{
			// no home set, see if we are in dropbox mode
			if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase(resources_mode))
			{
				home = contentHostingService.getDropboxCollection();

				// if it came back null, we will pretend not to be in dropbox mode
				if (home != null)
				{
					state.setAttribute(STATE_HOME_COLLECTION_DISPLAY_NAME, contentHostingService.getDropboxDisplayName());

					// create/update the collection of folders in the dropbox
					contentHostingService.createDropboxCollection();
				}
			}

			// if we still don't have a home,
			if ((home == null) || (home.length() == 0))
			{
				home = contentHostingService.getSiteCollection(toolManager.getCurrentPlacement().getContext());
				try {
					state.setAttribute(STATE_HOME_COLLECTION_DISPLAY_NAME, ((Site) siteService.getSite(toolManager.getCurrentPlacement().getContext())).getTitle());
				} catch (IdUnusedException e) {
					log.warn("Error while trying to set {} attribute for site {}", STATE_HOME_COLLECTION_DISPLAY_NAME, toolManager.getCurrentPlacement().getContext());
				}
			}
		}
		state.setAttribute (STATE_HOME_COLLECTION_ID, home);
		state.setAttribute (STATE_COLLECTION_ID, home);
		state.setAttribute (STATE_NAVIGATION_ROOT, home);

		// state.setAttribute (STATE_COLLECTION_ID, state.getAttribute (STATE_HOME_COLLECTION_ID));

		if (state.getAttribute(STATE_SITE_TITLE) == null)
		{
			String title = "";
			try
			{
				title = ((Site) siteService.getSite(toolManager.getCurrentPlacement().getContext())).getTitle();
			}
			catch (IdUnusedException e)
			{	// ignore
			}
			state.setAttribute(STATE_SITE_TITLE, title);
		}

		if (state.getAttribute(STATE_SITE_ID) == null)
		{
			String id = "";
			try
			{
				id = ((Site) siteService.getSite(toolManager.getCurrentPlacement().getContext())).getId();
			}
			catch (IdUnusedException e)
			{	// ignore
			}
			state.setAttribute(STATE_SITE_ID, id);
		}

		getExpandedCollections(state).clear();
		state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, new HashMap());
		
		state.setAttribute(STATE_DROPBOX_HIGHLIGHT, 1);
		
		if(state.getAttribute(STATE_USING_CREATIVE_COMMONS) == null)
		{
			boolean usingCreativeCommons = ServerConfigurationService.getBoolean("copyright.use_creative_commons", false);
			if(usingCreativeCommons)
			{
				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.TRUE.toString());
			}
			else
			{
				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.FALSE.toString());
			}
		}

		// are optional properties enabled on the server/cluster?
		String optional_properties = portlet.getPortletConfig().getInitParameter("optional_properties");
		if(optional_properties != null && "true".equalsIgnoreCase(optional_properties))
		{
			ListItem.setOptionalPropertiesEnabled(true);
		}
		
		state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.FALSE);
		String[] siteTypes = ServerConfigurationService.getStrings("prevent.public.resources");
		String siteType;
		Site site = null;
		try
		{
			site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
			siteType = site.getType();
			if(siteTypes != null)
			{
				for(String siteType1 : siteTypes)
				{
					if( (StringUtils.trimToNull( siteType1 )).equals( siteType ) )
					{
						state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.TRUE);
						break;
					}
				}
			}
		}
		catch (IdUnusedException e)
		{
			// allow public display
		}
		catch(NullPointerException e)
		{
			// allow public display
		}
		
		Time defaultRetractTime;
		defaultRetractTime = timeService.newTime(timeService.newTime().getTime() + ONE_WEEK);			
		ContentHostingService chs = contentHostingService;
		if ( site != null && chs instanceof SiteContentAdvisorProvider ) {
			SiteContentAdvisorProvider scap = (SiteContentAdvisorProvider) chs;
			SiteContentAdvisor sca =  scap.getContentAdvisor(site);
			if ( sca != null ) {
				defaultRetractTime = timeService.newTime(sca.getDefaultRetractTime());
			}
		}
		
		
		state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractTime);
		
		if(state.getAttribute(STATE_LIST_PREFERENCE) == null)
		{
			state.setAttribute(STATE_LIST_PREFERENCE, LIST_HIERARCHY);
		}
			
		state.setAttribute (STATE_INITIALIZED, Boolean.TRUE.toString());

		/** init the delete content cleanup interval setting */
		if (state.getAttribute(STATE_CLEANUP_DELETED_CONTENT_INTERVAL) == null)
		{
			// Cleanup Deleted Content quartz job is enabled with trigger
			if (checkQuartzJobAndTrigger("org.sakaiproject.content.CleanupDeletedContent"))
			{
				// default to be 30 days if not set
				state.setAttribute(STATE_CLEANUP_DELETED_CONTENT_INTERVAL, ServerConfigurationService.getString("content.keep.deleted.files.days", "30"));
			}
		}
	}
	
	/**
	 * check whether there is an quartz job with active trigger for the specified job bean
	 * @param jobBeanName
	 * @return
	 */
	private boolean checkQuartzJobAndTrigger(String jobBeanName) {
		
		boolean rv = false;
		
		Scheduler scheduler = schedulerManager.getScheduler();
		try
		{
			// get the job scheduler setting and check for whether the content cleanup job has been enabled
			Set<JobKey> jobKeys = scheduler.getJobKeys(null);
			for (JobKey key : jobKeys) {
				JobDetail jobDetail = scheduler.getJobDetail(key);
				String beanName = jobDetail.getJobDataMap().getString(JobBeanWrapper.SPRING_BEAN_NAME);
				if (jobBeanName != null && jobBeanName.equals(beanName))
				{
					// found the right quartz job
					List<? extends Trigger> triggers = scheduler.getTriggersOfJob(key);
					// check whether there is any existence of trigger for this job
					if (!triggers.isEmpty())
					{
						return true;
					}
				}
			}
		}
		catch (SchedulerException e)
		{
			log.warn("{} exception to get Scheduler Jobs {}", this, e.getMessage());
		}
		return rv;
	}


/**
	* is notification enabled?
	*/
	protected boolean notificationEnabled(SessionState state)
	{
		log.debug("{}.notificationEnabled()", this);
		return true;

	}	// notificationEnabled

	/**
	 * @param state
	 */
	protected void pasteItem(SessionState state, String collectionId)
	{
		log.debug("{}.pasteItem()", this);
		boolean moving = true;
		boolean copying = false;
		List<String> items_to_be_pasted = (List<String>) state.removeAttribute(STATE_ITEMS_TO_BE_MOVED);
		if(items_to_be_pasted == null)
		{
			items_to_be_pasted = (List<String>) state.removeAttribute(STATE_ITEMS_TO_BE_COPIED);
			copying = true;
			moving = false;
		}

		if(items_to_be_pasted == null)
		{
			return;
		}
	
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute(STATE_CONTENT_SERVICE);

		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		ServiceLevelAction slAction = null;
		Reference ref = null;
		
		for(String entityId : items_to_be_pasted)
		{
			try
			{
				ContentEntity entity;
				if(contentService.isCollection(entityId))
				{
					entity = contentService.getCollection(entityId);
				}
				else
				{
					entity = contentService.getResource(entityId);
				}
				
				String resourceTypeId = entity.getResourceType();
				
				ResourceType typeDef = registry.getType(resourceTypeId);
				
				ResourceToolAction action;
				if(moving)
				{
					action = typeDef.getAction(ResourceToolAction.MOVE);
				}
				else
				{
					action = typeDef.getAction(ResourceToolAction.COPY);
				}
				if(action == null)
				{
					continue;
				}
				
				if(action instanceof ServiceLevelAction)
				{
					slAction = (ServiceLevelAction) action;
					
					ref = entityManager.newReference(entity.getReference());
					
					slAction.initializeAction(ref);
					
					// paste copied item into collection 
					String newId;
					
					if(moving)
					{
						newId = contentService.moveIntoFolder(entityId, collectionId);
					}
					else
					{
						newId = contentService.copyIntoFolder(entityId, collectionId);
					}
					
					ref = entityManager.newReference(contentService.getReference(newId));
					
					slAction.finalizeAction(ref);
					
					Set<String> expandedCollections = getExpandedCollections(state);
					expandedCollections.add(collectionId);
				}
				
				ref = null;
			}
			catch(IdUniquenessException e)
			{
				String name = isolateName(entityId);
				if(slAction != null && ref != null)
				{
					slAction.cancelAction(ref);
					name  = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				}
				addAlert(state, trb.getFormattedMessage("paste.error", new Object[]{name}));
			}
			catch(IdUsedException e)
			{
				String name = isolateName(entityId);
				if(slAction != null && ref != null)
				{
					slAction.cancelAction(ref);
					name  = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				}
				addAlert(state, trb.getFormattedMessage("paste.error", new Object[]{name}));
			}
			catch(InconsistentException e)
			{
				String name = isolateName(entityId);
				if(slAction != null && ref != null)
				{
					slAction.cancelAction(ref);
					name  = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				}
				addAlert(state, trb.getFormattedMessage("paste.error", new Object[]{name}));
			}
			catch (Exception e)
			{
				if(slAction != null && ref != null)
				{
					slAction.cancelAction(ref);
				}
				if (ref != null) {
					log.warn(ref.getReference(), e);
				} else {
					log.warn("exception pasting item: ", e);
				}
			}
		}
		// if no errors
		// TODO expand collection
		
	}

	/**
	* Prepare the current page of site collections to display.
	* @return List of ListItem objects to display on this page.
	*/
	protected List<ListItem> prepPage(SessionState state)
	{
		log.debug("{}.prepPage()", this);
		List<ListItem> rv = new ArrayList<>();

		// access the page size
		int pageSize = ((Integer) state.getAttribute(STATE_PAGESIZE));

		// cleanup prior prep
		state.removeAttribute(STATE_NUM_MESSAGES);

		// are we going next or prev, first or last page?
		boolean goNextPage = state.getAttribute(STATE_GO_NEXT_PAGE) != null;
		boolean goPrevPage = state.getAttribute(STATE_GO_PREV_PAGE) != null;
		boolean goFirstPage = state.getAttribute(STATE_GO_FIRST_PAGE) != null;
		boolean goLastPage = state.getAttribute(STATE_GO_LAST_PAGE) != null;
		state.removeAttribute(STATE_GO_NEXT_PAGE);
		state.removeAttribute(STATE_GO_PREV_PAGE);
		state.removeAttribute(STATE_GO_FIRST_PAGE);
		state.removeAttribute(STATE_GO_LAST_PAGE);

		// are we going next or prev message?
		boolean goNext = state.getAttribute(STATE_GO_NEXT) != null;
		boolean goPrev = state.getAttribute(STATE_GO_PREV) != null;
		state.removeAttribute(STATE_GO_NEXT);
		state.removeAttribute(STATE_GO_PREV);

		// read all channel messages
		List<ListItem> allMessages = readAllResources(state);

		if (allMessages == null)
		{
			return rv;
		}
		
		String messageIdAtTheTopOfThePage = null;
		Object topMsgId = state.getAttribute(STATE_TOP_PAGE_MESSAGE_ID);
		if(topMsgId == null)
		{
			// do nothing
		}
		else if(topMsgId instanceof Integer)
		{
			messageIdAtTheTopOfThePage = ((Integer) topMsgId).toString();
		}
		else if(topMsgId instanceof String)
		{
			messageIdAtTheTopOfThePage = (String) topMsgId;
		}

		// if we have no prev page and do have a top message, then we will stay "pinned" to the top
		boolean pinToTop = (	(messageIdAtTheTopOfThePage != null)
							&&	(state.getAttribute(STATE_PREV_PAGE_EXISTS) == null)
							&&	!goNextPage && !goPrevPage && !goNext && !goPrev && !goFirstPage && !goLastPage);

		// if we have no next page and do have a top message, then we will stay "pinned" to the bottom
		boolean pinToBottom = (	(messageIdAtTheTopOfThePage != null)
							&&	(state.getAttribute(STATE_NEXT_PAGE_EXISTS) == null)
							&&	!goNextPage && !goPrevPage && !goNext && !goPrev && !goFirstPage && !goLastPage);

		// how many messages, total
		int numMessages = allMessages.size();

		if (numMessages == 0)
		{
			return rv;
		}

		// save the number of messges
		state.setAttribute(STATE_NUM_MESSAGES, numMessages);

		// find the position of the message that is the top first on the page
		int posStart = 0;
		if (messageIdAtTheTopOfThePage != null)
		{
			// find the next page
			posStart = findResourceInList(allMessages, messageIdAtTheTopOfThePage);

			// if missing, start at the top
			if (posStart == -1)
			{
				posStart = 0;
			}
		}
		
		// if going to the next page, adjust
		if (goNextPage)
		{
			posStart += pageSize;
		}

		// if going to the prev page, adjust
		else if (goPrevPage)
		{
			posStart -= pageSize;
			if (posStart < 0) posStart = 0;
		}
		
		// if going to the first page, adjust
		else if (goFirstPage)
		{
			posStart = 0;
		}
		
		// if going to the last page, adjust
		else if (goLastPage)
		{
			posStart = numMessages - pageSize;
			if (posStart < 0) posStart = 0;
		}

		// pinning
		if (pinToTop)
		{
			posStart = 0;
		}
		else if (pinToBottom)
		{
			posStart = numMessages - pageSize;
			if (posStart < 0) posStart = 0;
		}

		// get the last page fully displayed
		if (posStart + pageSize > numMessages)
		{
			posStart = numMessages - pageSize;
			if (posStart < 0) posStart = 0;
		}

		// compute the end to a page size, adjusted for the number of messages available
		int posEnd = posStart + (pageSize-1);
		if (posEnd >= numMessages) posEnd = numMessages-1;

		// select the messages on this page
		for (int i = posStart; i <= posEnd; i++)
		{
			rv.add(allMessages.get(i));
		}

		// save which message is at the top of the page
		ListItem itemAtTheTopOfThePage = (ListItem) allMessages.get(posStart);
		state.setAttribute(STATE_TOP_PAGE_MESSAGE_ID, itemAtTheTopOfThePage.getId());
		state.setAttribute(STATE_TOP_MESSAGE_INDEX, posStart);

		// which message starts the next page (if any)
		int next = posStart + pageSize;
		if (next < numMessages)
		{
			state.setAttribute(STATE_NEXT_PAGE_EXISTS, "");
		}
		else
		{
			state.removeAttribute(STATE_NEXT_PAGE_EXISTS);
		}

		// which message ends the prior page (if any)
		int prev = posStart - 1;
		if (prev >= 0)
		{
			state.setAttribute(STATE_PREV_PAGE_EXISTS, "");
		}
		else
		{
			state.removeAttribute(STATE_PREV_PAGE_EXISTS);
		}

		if (state.getAttribute(STATE_VIEW_ID) != null)
		{
			int viewPos = findResourceInList(allMessages, (String) state.getAttribute(STATE_VIEW_ID));
	
			// are we moving to the next message
			if (goNext)
			{
				// advance
				viewPos++;
				if (viewPos >= numMessages) viewPos = numMessages-1;
			}
	
			// are we moving to the prev message
			if (goPrev)
			{
				// retreat
				viewPos--;
				if (viewPos < 0) viewPos = 0;
			}
			
			// update the view message
			state.setAttribute(STATE_VIEW_ID, ((ListItem) allMessages.get(viewPos)).getId());
			
			// if the view message is no longer on the current page, adjust the page
			// Note: next time through this will get processed
			if (viewPos < posStart)
			{
				state.setAttribute(STATE_GO_PREV_PAGE, "");
			}
			else if (viewPos > posEnd)
			{
				state.setAttribute(STATE_GO_NEXT_PAGE, "");
			}
			
			if (viewPos > 0)
			{
				state.setAttribute(STATE_PREV_EXISTS,"");
			}
			else
			{
				state.removeAttribute(STATE_PREV_EXISTS);
			}
			
			if (viewPos < numMessages-1)
			{
				state.setAttribute(STATE_NEXT_EXISTS,"");
			}
			else
			{
				state.removeAttribute(STATE_NEXT_EXISTS);
			}			
		}

		return rv;

	}	// prepPage

	/**
	* Develop a list of all the site collections that there are to page.
	* Sort them as appropriate, and apply search criteria.
	*/
	protected List<ListItem> readAllResources(SessionState state)
	{
		log.debug("{}.readAllResources()", this);
		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		List<ListItem> other_sites = new ArrayList<>();

		String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		Set<String> expandedCollections = getExpandedCollections(state);
		
		Comparator userSelectedSort = (Comparator) state.getAttribute(STATE_LIST_VIEW_SORT);
		
		Boolean showRemove = (Boolean) state.getAttribute(STATE_SHOW_REMOVE_ACTION);
		boolean showRemoveAction = showRemove != null && showRemove;
		
		Boolean showMove = (Boolean) state.getAttribute(STATE_SHOW_MOVE_ACTION);
		boolean showMoveAction = showMove != null && showMove;
		
		Boolean showCopy = (Boolean) state.getAttribute(STATE_SHOW_COPY_ACTION);
		boolean showCopyAction = showCopy != null && showCopy;

		// add user's personal workspace
		User user = userDirectoryService.getCurrentUser();
		String userId = user.getId();
		String wsId = siteService.getUserSiteId(userId);
		String wsCollectionId = contentHostingService.getSiteCollection(wsId);
		List<String> items_to_be_copied = (List<String>) state.getAttribute(STATE_ITEMS_TO_BE_COPIED);
		List<String> items_to_be_moved = (List<String>) state.getAttribute(STATE_ITEMS_TO_BE_MOVED);
		
		if(! collectionId.equals(wsCollectionId))
		{
            try
            {
            	ContentCollection wsCollection = contentHostingService.getCollection(wsCollectionId);
				ListItem wsRoot = ListItem.getListItem(wsCollection, null, registry, false, expandedCollections, items_to_be_moved, items_to_be_copied, 0, userSelectedSort, false, null);
		        other_sites.add(wsRoot);
            }
            catch (IdUnusedException e)
            {
	            // TODO Auto-generated catch block
	            log.warn("IdUnusedException ", e);
            }
            catch (TypeException e)
            {
	            // TODO Auto-generated catch block
	            log.warn("TypeException ", e);
            }
            catch (PermissionException e)
            {
	            // TODO Auto-generated catch block
	            log.warn("PermissionException ", e);
            }
		}
		
 		/*
		 * add all other sites user has access to
		 * NOTE: This does not (and should not) get all sites for admin.  
		 *       Getting all sites for admin is too big a request and
		 *       would result in too big a display to render in html.
		 */
		Map othersites = contentHostingService.getCollectionMap();
		SortedSet sort = new TreeSet();
		for(Iterator<Entry<String, String>> mapIter = othersites.entrySet().iterator(); mapIter.hasNext();) 
		{
			  Entry<String, String> entry = mapIter.next();
              sort.add(entry.getValue() + DELIM + entry.getKey());
		}
		
		Iterator sortIt = sort.iterator();
		while(sortIt.hasNext())
		{
			String keyvalue = (String) sortIt.next();
			String displayName = keyvalue.substring(0, keyvalue.lastIndexOf(DELIM));
			String collId = keyvalue.substring(keyvalue.lastIndexOf(DELIM) + 1);
			if(! collectionId.equals(collId) && ! wsCollectionId.equals(collId))
			{
				ContentCollection collection;
                try
                {
	                collection = contentHostingService.getCollection(collId);
					ListItem root = ListItem.getListItem(collection, null, registry, false, expandedCollections, items_to_be_moved, items_to_be_copied, 0, null, false, null);
					root.setName(displayName);
					other_sites.add(root);
                }
                catch (IdUnusedException e)
                {
	                // TODO Auto-generated catch block
	                log.warn("IdUnusedException {}", (Object) e);
                }
                catch (TypeException e)
                {
	                // TODO Auto-generated catch block
	                log.warn("TypeException {}", (Object) e);
                }
                catch (PermissionException e)
                {
	                // TODO Auto-generated catch block
	                log.warn("PermissionException {}", (Object) e);
                }
			}
          }
		
		return other_sites;
	}

	public static List<ContentResource> createUrls(SessionState state, ResourceToolActionPipe pipe)
    {
		log.debug("ResourcesAction.createUrls()");
		boolean item_added = false;
		String collectionId;
		List<ContentResource> new_resources = new ArrayList<>();
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) pipe;
		Iterator<ResourceToolActionPipe> pipeIt = mfp.getPipes().iterator();
		while(pipeIt.hasNext())
		{
			ResourceToolActionPipe fp = pipeIt.next();
			collectionId = pipe.getContentEntity().getId();
			String name = fp.getFileName();
			if(StringUtils.isBlank(name))
			{
				continue;
			}
			String basename = name.trim();
            String extension = ".URL";
            
            try
			{
				ContentResourceEdit resource = contentHostingService.addResource(collectionId,Validator.escapeResourceName(basename),Validator.escapeResourceName(extension),MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				
				extractContent(fp, resource);
								
				// SAK-23171 - cleanup the URL spaces
				String originalUrl = new String(resource.getContent());
				String cleanedURL = StringUtils.trim(originalUrl);
				//cleanedURL = StringUtils.replace(cleanedURL, " ", "%20");
				
				// SAK-23587 - properly escape the URL where required
				try {
					URL url = new URL(cleanedURL);
					URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
					cleanedURL = uri.toString();
				} catch (Exception e) {
					//ok to ignore, just use the original url
					log.debug("URL can not be encoded: {}:{}", e.getClass(), e.getCause());
				}
				
				if (!StringUtils.equals(originalUrl, cleanedURL)) {
				    // the url was cleaned up, log it and update it
				    log.info("Resources URL cleanup changed url to '{}' from '{}'", cleanedURL, originalUrl);
				    resource.setContent(cleanedURL.getBytes());
				}
				
				resource.setContentType(fp.getRevisedMimeType());
				resource.setResourceType(pipe.getAction().getTypeId());
				int notification = NotificationService.NOTI_NONE;
				Object obj = fp.getRevisedListItem();
				if(obj != null && obj instanceof ListItem)
				{
					((ListItem) obj).updateContentResourceEdit(resource);
					notification = ((ListItem) obj).getNotification();
				}
				ResourcePropertiesEdit resourceProperties = resource.getPropertiesEdit();
				String displayName = null;
				if(obj != null && obj instanceof ListItem)
				{
					displayName = ((ListItem) obj).getName();
					List<String> alerts = ((ListItem)obj).checkRequiredProperties();
					for (String alert : alerts) {
						addAlert(state, alert);
					}
				}
				if(StringUtils.isBlank(displayName))
				{
					displayName = name;
				}
				if(displayName != null)
				{
					resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
				}
				Map values = pipe.getRevisedResourceProperties(); 	 	 
				for(Iterator<Entry<String, String>> mapIter = values.entrySet().iterator(); mapIter.hasNext();) 	 
				{ 	 
					Entry<String, String> entry = mapIter.next();
					resourceProperties.addProperty(entry.getKey(), entry.getValue());
				}
				try
				{
					contentHostingService.commitResource(resource, notification);
					ResourceConditionsHelper.notifyCondition(resource);
					item_added = true;
					new_resources.add(resource);
				}
				catch(OverQuotaException e)
				{
					addAlert(state, trb.getFormattedMessage("alert.overquota", new Object[]{name}));
					log.debug("OverQuotaException {}", (Object) e);
					try
					{
						contentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						log.debug("Unable to remove partially completed resource: {}\n{}", resource.getId(), e);
					}
				}
				catch(ServerOverloadException e)
				{
					addAlert(state, trb.getFormattedMessage("alert.unable1", new Object[]{name}));
					log.debug("ServerOverloadException {}", (Object) e);
					try
					{
						contentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						log.debug("Unable to remove partially completed resource: {}\n{}", resource.getId(), e);
					}
				}
			}
			catch (PermissionException e)
			{
				addAlert(state, trb.getString("alert.perm"));
				log.warn("PermissionException ", e);
			}
			catch (IdUnusedException e)
			{
				log.warn("IdUsedException ", e);
			}
			catch (IdInvalidException e)
			{
				log.warn("IdInvalidException ", e);
			}
			catch (IdUniquenessException e)
			{
				log.warn("IdUniquenessException ", e);
			}
			catch (IdLengthException e)
			{
				addAlert(state, trb.getFormattedMessage("alert.toolong", new Object[]{e.getMessage()}));
				// TODO Auto-generated catch block
				log.warn("IdLengthException ", e);
			}
			catch (OverQuotaException e)
			{
				addAlert(state, trb.getFormattedMessage("alert.overquota", new Object[]{name}));
				log.warn("OverQuotaException ", e);
			}
			catch (ServerOverloadException e)
			{
				addAlert(state, trb.getFormattedMessage("alert.unable1", new Object[]{name}));
				log.warn("ServerOverloadException ", e);
			}
		}
		
		return (item_added ? new_resources : null);
   }

	public static void addAlert(String message)
	{
		log.debug("ResourcesAction.addAlert()");
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		Collection<String> errorMessages = (Collection<String>) toolSession.getAttribute(STATE_MESSAGE_LIST);
		if(errorMessages == null)
		{
			errorMessages = new TreeSet();
			toolSession.setAttribute(STATE_MESSAGE_LIST, errorMessages);
		}
		errorMessages.add(message);
	}
	
	public static void checkMessageList(SessionState state)
	{
		log.debug("ResourcesAction.checkMessageList()");
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		Collection<String> errorMessages = (Collection<String>) toolSession.getAttribute(STATE_MESSAGE_LIST);
		if(errorMessages == null)
		{
			return;
		}

		for(String msg : errorMessages)
		{
			addAlert(state, msg);
		}
	}
	
	public static int preserveRequestState(SessionState state, String[] prefixes)
	{
		Map requestState = new HashMap();
		
		int requestStateId = 0;
		Random random = new Random();
		while(requestStateId == 0)
		{
			requestStateId = random.nextInt();
		}
		
		List<String> attrNames = state.getAttributeNames();
		for(String attrName : attrNames)
		{
			for(String prefix : prefixes)
			{
				if(attrName.startsWith(prefix))
				{
					requestState.put(attrName,state.getAttribute(attrName));
					break;
				}
			}
		}
		
		Object pipe = state.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe != null)
		{
			requestState.put(ResourceToolAction.ACTION_PIPE, pipe);
		}
		
		Tool tool = toolManager.getCurrentTool();
		Object url = state.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		if( url != null)
		{
			requestState.put(tool.getId() + Tool.HELPER_DONE_URL, url);
		}

		state.setAttribute(PREFIX + SYS + requestStateId, requestState);
		log.debug("preserveRequestState() requestStateId == {}\n{}", requestStateId, requestState);
		return requestStateId;
	}
	
	public static void restoreRequestState(SessionState state, String[] prefixes, int requestStateId)
	{
		Map requestState = (Map) state.removeAttribute(PREFIX + SYS + requestStateId);
		log.debug("restoreRequestState() requestStateId == {}\n{}", requestStateId, requestState);
		if(requestState != null)
		{
			List<String> attrNames = state.getAttributeNames();
			for(String attrName : attrNames)
			{
				for(String prefix : prefixes)
				{
					if(attrName.startsWith(prefix))
					{
						state.removeAttribute(attrName);
						break;
					}
				}
			}
			
			for(Iterator<Entry<String, String>> mapIter = requestState.entrySet().iterator(); mapIter.hasNext();) 
			{
				Entry<String, String> entry = mapIter.next();
				state.setAttribute(entry.getKey(), entry.getValue());
			}
		}
		
	}

	// https://jira.sakaiproject.org/browse/SAK-5350

	/**
	 * Build the context to upload files to multiple users and groups
	 * SAK-5350
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	public String buildDropboxMultipleFoldersUploadPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
	    String home = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
	    DropboxMultipleFoldersUploadInputPreserver inputPreserver = DropboxMultipleFoldersUploadInputPreserver.get();
	    inputPreserver.reloadFormData(state, context);
	    List<List<String>> usersDropboxList = Collections.emptyList();
	    try {
	        Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
	        String siteType = site.getType();
	        if (siteType != null && "course".equals(siteType)) {
	            context.put("isCourseSite", true);
	        } else {
	            context.put("isCourseSite", false);
	        }
	        Collection<Group> site_groups = site.getGroups();

	        // form the azGroups for a context-as-implemented-by-site
	        Collection<String> azGroups = new ArrayList<>(2);
	        azGroups.add(siteService.siteReference(site.getId()));
	        azGroups.add("!site.helper");
	        // get the user ids who has dropbox.own permissions
	        Set userIds = authzGroupService.getUsersIsAllowed(ContentHostingService.AUTH_DROPBOX_OWN, azGroups);
	        List<User> users = userDirectoryService.getUsers(userIds);

	        usersDropboxList = inputPreserver.prepareGroupsAndUsersForContext(site_groups, users);
	    } catch (Exception ex) {
	        log.error("Exception while getting users collections", ex);
	    }
		copyrightChoicesIntoContext(state, context);
	    context.put("usersDropboxList", usersDropboxList);

	    // Max upload size:
	    String max_file_size_mb = (String)state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
	    max_file_size_mb = max_file_size_mb == null ? ResourcesConstants.DEFAULT_MAX_FILE_SIZE_STRING : max_file_size_mb;
	    context.put("uploadMaxSize", max_file_size_mb);

	    return TEMPLATE_DROPBOX_MULTIPLE_FOLDERS_UPLOAD;
	} // buildDropboxMultipleFoldersUploadPanelContext

	/**
	 * Handle a request to upload a file in multiple folders.
	 * SAK-5350
	 * @param data
	 */
	public void doDropboxMultipleFoldersUpload(RunData data) {
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
	    // go into upload file in multiple folders
	    state.setAttribute(STATE_MODE, MODE_DROPBOX_MULTIPLE_FOLDERS_UPLOAD);
	} // doDropboxMultipleFoldersUpload

	/**
	 * doMultipleFoldersUpload upload the file to multiple users folder
	 * SAK-5350
	 * @param data
	 */
	public void doMultipleFoldersUpload(RunData data) {
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
	    log.debug("{}.doMultipleFoldersUpload()", this);
	    SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
	    ParameterParser params = data.getParameters();
	    String siteId = toolManager.getCurrentPlacement().getContext();

	    // Preserve user input in case of a validation error
	    DropboxMultipleFoldersUploadInputPreserver inputPreserver = DropboxMultipleFoldersUploadInputPreserver.get();
	    inputPreserver.saveFormData(data, state);

	    /* If the user uploaded a file in this form submission, we can pull it from params.
	     * But consider when a user uploads a file, submits & faces a validation error, then corrects the issue without re-uploading the file.
	     * To cover both cases, we delegate responsibility to the InputPreserver to retrieve the latest uploaded file */
	    FileItem fileitem = inputPreserver.parseFileItem(params, state);

	    String displayName = params.getString("MultipleFolderDisplayName");
	    String[] multipleDropboxSelected = params.getStrings("usersDropbox-selection");
	    Set usersCollectionIds = new TreeSet();
		String copyright = "";
		String newCopyright = "";
		boolean copyrightAlert = false;

	    if (fileitem == null || StringUtils.isBlank(fileitem.getFileName())) {
	        // no file selected -- skip this one
	        addAlert(state, trb.getString("multiple.file.upload.nofileselected"));
	        state.setAttribute(STATE_MODE, MODE_DROPBOX_MULTIPLE_FOLDERS_UPLOAD);
	        return;

	    } else if (fileitem.getFileName().length() > 0) {

			// Check copyright
			copyright = StringUtils.trimToNull(params.getString("copyright"));
			if (copyright !=  null) {
				newCopyright = StringUtils.trimToNull(params.getString("newcopyright"));
				copyrightAlert = params.getBoolean("copyrightAlert");
				boolean requireChoice = ServerConfigurationService.getBoolean(SAK_PROP_COPYRIGHT_REQ_CHOICE, SAK_PROP_COPYRIGHT_REQ_CHOICE_DEFAULT);
				if (requireChoice && rb.getString(MSG_KEY_COPYRIGHT_REQ_CHOICE).equals(copyrightManager.getCopyrightString(copyright))) {
					addAlert(state, rb.getString(ResourcesAction.MSG_KEY_COPYRIGHT_REQ_CHOICE_ERROR));
					state.setAttribute(STATE_MODE, MODE_DROPBOX_MULTIPLE_FOLDERS_UPLOAD);
					return;
				}
			}

	        String filename = FilenameUtils.getName(fileitem.getFileName());
	        if (displayName == null) {
	            displayName = filename;
	        } else if ("".equals(displayName)) {
	            displayName = filename;
	        }
	        String SEPARATOR = "/";
	        String COLLECTION_DROPBOX = "/group-user/";
	        String contentType = fileitem.getContentType();
	        ContentResourceEdit cr;
	        String extension = "";
	        String basename = filename.trim();
	        if (filename.contains(".")) {
	            String[] parts = filename.split("\\.");
	            basename = parts[0];
	            if (parts.length > 1) {
	                extension = parts[parts.length - 1];
	            }
	            for (int i = 1; i < parts.length - 1; i++) {
	                basename += "." + parts[i];
	                // extension = parts[i + 1];
	            }
	        }
	        if (multipleDropboxSelected == null) {
	            addAlert(state, trb.getString("multiple.file.upload.nousersselected"));
	            state.setAttribute(STATE_MODE, MODE_DROPBOX_MULTIPLE_FOLDERS_UPLOAD);
	            return;
	        } else if (multipleDropboxSelected.length < 1) {
	            // no users selected
	            addAlert(state, trb.getString("multiple.file.upload.nousersselected"));
	            state.setAttribute(STATE_MODE, MODE_DROPBOX_MULTIPLE_FOLDERS_UPLOAD);
	            return;
	        } else {
				// Fill Collections with users
				for(String multipleDropboxSelected1 : multipleDropboxSelected) {
					try
					{
						userDirectoryService.getUser( multipleDropboxSelected1 );
						// If the user exists, add to collection
						usersCollectionIds.add( multipleDropboxSelected1 );
					}
					catch( UserNotDefinedException ex )
					{
						try
						{
							Site site = siteService.getSite(siteId);
							Group grp = site.getGroup( multipleDropboxSelected1 );
							// form the azGroups for a
							// context-as-implemented-by-site
							Collection<String> azGroups = new ArrayList<>(2);
							azGroups.add(siteService.siteReference(site.getId()));
							azGroups.add("!site.helper");
							// get the user ids who has dropbox.own permissions
							Set<String> dbOwnsUserIds = authzGroupService.getUsersIsAllowed(ContentHostingService.AUTH_DROPBOX_OWN, azGroups);
							for (Iterator<org.sakaiproject.authz.api.Member> it = grp.getMembers().iterator(); it.hasNext();) {
								String userIdInGroup = it.next().getUserId();
								if (dbOwnsUserIds.contains(userIdInGroup)) {
									usersCollectionIds.add(userIdInGroup);
								}
							}
						}catch (IdUnusedException e) {
							// Error finding a previously selected group.
							log.error("Error in {}.doMultipleFoldersUpload(): Unable to find selected Group", this, e);
						}
					}
				}
	        }

	        try {
				for (Iterator it = usersCollectionIds.iterator(); it.hasNext();) {
					try (InputStream stream = params.getFileItem("MultipleFolderContent").getInputStream()) {
						// A site Dropbox Collection ID will be /group-user/SITE_ID/USER_ID/
						String collectionId = COLLECTION_DROPBOX + siteId + SEPARATOR + it.next() + SEPARATOR;
						cr = contentHostingService.addResource(collectionId, Validator.escapeResourceName(basename),
								Validator.escapeResourceName(extension), MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);

						// Add the actual contents of the file and content type
						// We need a new inputstream because our internal FileItem doesn't re-create the
						// input stream on each call.
						cr.setContent(stream);
						cr.setContentType(contentType);

						// fill up its properties
						ResourcePropertiesEdit resourceProperties = cr.getPropertiesEdit();
						resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());
						resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);

						// Add copyright settings
						if (StringUtils.isNotBlank(copyright)) {
							resourceProperties.addProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE, copyright);
						}
						if (StringUtils.isNotBlank(newCopyright)) {
							resourceProperties.addProperty(ResourceProperties.PROP_COPYRIGHT, newCopyright);
						}
						if (copyrightAlert) {
							resourceProperties.addProperty(ResourceProperties.PROP_COPYRIGHT_ALERT, Boolean.TRUE.toString());
						}

						// now to commit the changes
						boolean notification = params.getBoolean("notify_dropbox");
						int noti = NotificationService.NOTI_NONE;

						if (notification) {
							noti = NotificationService.NOTI_REQUIRED;
						}
						contentHostingService.commitResource(cr, noti);
						DropboxMultipleFoldersUploadInputPreserver.get().clearFormData(state);
					} catch (IOException e) {
						log.warn("Failed to close stream.", e);
					}
				}
	        } catch (PermissionException e) {
	            addAlert(state, trb.getString("alert.perm"));
	            log.warn("PermissionException {}", (Object) e);
	        } catch (IdUnusedException e) {
	            log.warn("IdUnusedException: Error while getting dropbox collection, this error happens when a selected group contains a maintain user");
	        } catch (IdInvalidException e) {
	            log.warn("IdInvalidException {}", (Object) e);
	        } catch (IdUniquenessException e) {
	            log.warn("IdUniquenessException {}", (Object) e);
	        } catch (IdLengthException e) {
	            addAlert(state, trb.getFormattedMessage("alert.toolong", new Object[] { e.getMessage() }));
	            log.warn("IdLengthException {}", (Object) e);
	        } catch (OverQuotaException e) {
	            addAlert(state, trb.getFormattedMessage("alert.overquota", new Object[] { filename }));
	            log.warn("OverQuotaException {}", (Object) e);
	        } catch (ServerOverloadException e) {
	            addAlert(state, trb.getFormattedMessage("alert.unable1", new Object[] { filename }));
	            log.warn("ServerOverloadException {}", (Object) e);
	        }
	    }
	    state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());
	    state.setAttribute(STATE_MODE, MODE_LIST);
	}


	// BEGIN SAK-23304 additions:
	
	/**
	 * set the state name to be "showfinish" if any item has been selected for deleting
	 * @param data
	 */
	public void doShowconfirm(RunData data)
	{
	    SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
	    
	    // cancel copy if there is one in progress
	    if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
	    {
	        initCopyContext(state);
	    }

	    // cancel move if there is one in progress
	    if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
	    {
	        initMoveContext(state);
	    }

	    Set showIdSet  = new TreeSet();
	    String[] showIds = data.getParameters ().getStrings ("selectedMembers");
	    if (showIds == null)
	    {
	        // there is no resource selected, show the alert message to the user
	        addAlert(state, rb.getString("choosefile3"));
	    }
	    else
	    {
	        showIdSet.addAll(Arrays.asList(showIds));
	        showItems(state, showIdSet);
	    }

	    if (state.getAttribute(STATE_MESSAGE) == null)
	    {
	        state.setAttribute (STATE_MODE, MODE_SHOW_FINISH);
	        state.setAttribute(STATE_LIST_SELECTIONS, showIdSet);
	    }
	}       // doShowconfirm

	/**
	 * set the state name to be "hidefinish" if any item has been selected for deleting
	 * @param data
	 */
	public void doHideconfirm(RunData data)
	{
	    SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

	    // cancel copy if there is one in progress
	    if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
	    {
	        initCopyContext(state);
	    }

	    // cancel move if there is one in progress
	    if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
	    {
	        initMoveContext(state);
	    }

	    Set hideIdSet  = new TreeSet();
	    String[] hideIds = data.getParameters ().getStrings ("selectedMembers");
	    if (hideIds == null)
	    {
	        // there is no resource selected, show the alert message to the user
	        addAlert(state, rb.getString("choosefile3"));
	    }
	    else
	    {
	        hideIdSet.addAll(Arrays.asList(hideIds));
	        hideItems(state, hideIdSet);
	    }

	    if (state.getAttribute(STATE_MESSAGE) == null)
	    {
	        state.setAttribute (STATE_MODE, MODE_HIDE_FINISH);
	        state.setAttribute(STATE_LIST_SELECTIONS, hideIdSet);
	    }
	}       // doHideconfirm

	/**
	 * @param state
	 * @param showIdSet
	 */
	protected void showItems(SessionState state, Set showIdSet)
	{
	    List<ListItem> showItems = new ArrayList<>();
	    List<ListItem> notShowItems = new ArrayList<>();
	    List<ListItem> nonEmptyFolders = new ArrayList<>();

	    ContentHostingService contentService = contentHostingService;

	    for(String showId : (Set<String>) showIdSet)
	    {
	        ContentEntity entity = null;
	        try
	        {
	            if(contentService.isCollection(showId))
	            {
	                entity = contentService.getCollection(showId);
	            }
	            else if(contentService.allowUpdateResource(showId))
	            {
	                entity = contentService.getResource(showId);
	            }
	            else
	            {
	                // do nothing
	            }
	            ListItem item = new ListItem(entity);
	            if(item.isCollection() && contentService.allowUpdateCollection(showId))
	            {
	                showItems.add(item);
	                if(! item.isEmpty)
	                {
	                    nonEmptyFolders.add(item);
	                }
	            }
	            else if(!item.isCollection() && contentService.allowUpdateResource(showId))
	            {
	                showItems.add(item);
	            }
	            else
	            {
	                notShowItems.add(item);
	            }

	        }
	        catch(PermissionException e)
	        {
	            log.warn("PermissionException: {}", e,e);
	        }
	        catch (IdUnusedException e)
	        {
	            log.warn("IdUnusedException: {}", e,e);
	        }
	        catch (TypeException e)
	        {
	            log.warn("TypeException: {}", e,e);
	        }
	    }

	    if(! notShowItems.isEmpty())
	    {
	        String notShowNames = "";
	        boolean first_item = true;
	        Iterator notIt = notShowItems.iterator();
	        while(notIt.hasNext())
	        {
	            ListItem item = (ListItem) notIt.next();
	            if(first_item)
	            {
	                notShowNames = item.getName();
	                first_item = false;
	            }
	            else if(notIt.hasNext())
	            {
	                notShowNames += ", " + item.getName();
	            }
	            else
	            {
	                notShowNames += " and " + item.getName();
	            }
	        }
	        addAlert(state, rb.getString("notpermis_modify_remove") );
	    }

	    state.setAttribute (STATE_SHOW_SET, showItems);
	    state.setAttribute (STATE_NON_EMPTY_SHOW_SET, nonEmptyFolders);
	}

	/**
	 * @param state
	 * @param hideIdSet
	 */
	protected void hideItems(SessionState state, Set hideIdSet)
	{
	    List<ListItem> hideItems = new ArrayList<>();
	    List<ListItem> notHideItems = new ArrayList<>();
	    List<ListItem> nonEmptyFolders = new ArrayList<>();

	    ContentHostingService contentService = contentHostingService;

	    for(String hideId : (Set<String>) hideIdSet)
	    {
	        ContentEntity entity = null;
	        try
	        {
	            if(contentService.isCollection(hideId))
	            {
	                entity = contentService.getCollection(hideId);
	            }
	            else if(contentService.allowUpdateResource(hideId))
	            {
	                entity = contentService.getResource(hideId);
	            }
	            else
	            {
	                // do nothing
	            }
	            ListItem item = new ListItem(entity);
	            if(item.isCollection() && contentService.allowUpdateCollection(hideId))
	            {
	                hideItems.add(item);
	                if(! item.isEmpty)
	                {
	                    nonEmptyFolders.add(item);
	                }
	            }
	            else if(!item.isCollection() && contentService.allowUpdateResource(hideId))
	            {
	                hideItems.add(item);
	            }
	            else
	            {
	                notHideItems.add(item);
	            }

	        }
	        catch(PermissionException e)
	        {
	            log.warn("PermissionException: {}", e,e);
	        }
	        catch (IdUnusedException e)
	        {
	            log.warn("IdUnusedException: {}", e,e);
	        }
	        catch (TypeException e)
	        {
	            log.warn("TypeException: {}", e,e);
	        }
	    }

	    if(! notHideItems.isEmpty())
	    {
	        String notHideNames = "";
	        boolean first_item = true;
	        Iterator notIt = notHideItems.iterator();
	        while(notIt.hasNext())
	        {
	            ListItem item = (ListItem) notIt.next();
	            if(first_item)
	            {
	                notHideNames = item.getName();
	                first_item = false;
	            }
	            else if(notIt.hasNext())
	            {
	                notHideNames += ", " + item.getName();
	            }
	            else
	            {
	                notHideNames += " and " + item.getName();
	            }
	        }
	        addAlert(state, rb.getString("notpermis_modify_remove") );
	    }

	    state.setAttribute (STATE_HIDE_SET, hideItems);
	    state.setAttribute (STATE_NON_EMPTY_HIDE_SET, nonEmptyFolders);
	}

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	public String buildShowFinishContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
	    context.put ("collectionId", state.getAttribute (STATE_COLLECTION_ID) );


	    List showItems = (List) state.getAttribute(STATE_SHOW_SET);
	    List nonEmptyFolders = (List) state.getAttribute(STATE_NON_EMPTY_SHOW_SET);

	    context.put ("showItems", showItems);

	    /*
        Iterator it = nonEmptyFolders.iterator();
        while(it.hasNext())
        {
                ListItem folder = (ListItem) it.next();
                String[] args = { folder.getName() };
                String msg = rb.getFormattedMessage("folder.notempty_show", args) + " ";
                addAlert(state, msg);
        }
	     */
	    return TEMPLATE_SHOW_FINISH;
	}

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	public String buildHideFinishContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
	    context.put ("collectionId", state.getAttribute (STATE_COLLECTION_ID) );

	    List hideItems = (List) state.getAttribute(STATE_HIDE_SET);
	    List nonEmptyFolders = (List) state.getAttribute(STATE_NON_EMPTY_HIDE_SET);

	    context.put ("hideItems", hideItems);

	    /*
        Iterator it = nonEmptyFolders.iterator();
        while(it.hasNext())
        {
                ListItem folder = (ListItem) it.next();
                String[] args = { folder.getName() };
                String msg = rb.getFormattedMessage("folder.notempty_hide", args) + " ";
                addAlert(state, msg);
        }
	     */

	    // get site type
	    String siteType = null;
	    Site site;

	    try
	    {
	        site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
	        siteType = site.getType();
	    }
	    catch (IdUnusedException e)
	    {
	        log.debug("ResourcesAction.buildHideFinishContext: cannot find current site");
	    }
	    context.put ("sitetype",siteType);
	    return TEMPLATE_HIDE_FINISH;
	}

	/**
	 * show the selected collection or resource items
	 */
	public void doFinalizeShow( RunData data)
	{
	    SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
	    state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

	    // cancel copy if there is one in progress
	    if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
	    {
	        initCopyContext(state);
	    }

	    // cancel move if there is one in progress
	    if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
	    {
	        initMoveContext(state);
	    }

	    ParameterParser params = data.getParameters ();

	    List<ListItem> items = (List) state.getAttribute(STATE_SHOW_SET);

	    // delete the lowest item in the hireachy first
	    Map<Integer, List<ListItem>> showItems = new HashMap<>();
	    // String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
	    int maxDepth = 0;
	    int depth;

	    Iterator it = items.iterator();
	    while(it.hasNext())
	    {
	        ListItem item = (ListItem) it.next();
	        String[] parts = item.getId().split(Entity.SEPARATOR);
	        depth = parts.length;
	        if (depth > maxDepth)
	        {
	            maxDepth = depth;
	        }
	        List<ListItem> v = showItems.get(depth);
	        if(v == null)
	        {
	            v = new ArrayList<>();
	        }
	        v.add(item);
	        showItems.put(depth, v);
	    }

	    boolean isCollection = false;
	    for (int j=maxDepth; j>0; j--)
	    {
	        List<ListItem> v = showItems.get(j);
	        if (v==null)
	        {
	            v = new ArrayList<>();
	        }
	        Iterator<ListItem> itemIt = v.iterator();
	        while(itemIt.hasNext())
	        {
	            ListItem item = itemIt.next();
	            try
	            {
	                if (item.isCollection())
	                {
	                    log.debug("show this collection resource{}", item.getId());
	                    ContentCollectionEdit edit= contentHostingService.editCollection(item.getId());
	                    edit.setAvailability(false, null, null);
	                    contentHostingService.commitCollection(edit);

	                }
	                else
	                {
	                    log.debug("show this non-collection resource {}", item.getId());
	                    ContentResourceEdit edit= contentHostingService.editResource(item.getId());
	                    edit.setAvailability(false, null, null);
	                    contentHostingService.commitResource(edit, 0);

	                }
	            }
	            catch (IdUnusedException e)
	            {
	                log.warn("IdUnusedException", e);
	            }
	            catch (TypeException e)
	            {
	                log.warn("TypeException", e);
	            }
	            catch (PermissionException e)
	            {
	                log.warn("PermissionException", e);
	            }
	            catch (ServerOverloadException e)
	            {
	                log.warn("ServerOverloadException", e);
	            }
	            catch (OverQuotaException e)
	            {
	                log.warn("OverQuotaException ", e);
	            }
	            catch (InUseException e)
	            {
	                log.warn("InUseException ", e);
	            }
	        }       // for
	    }       // for

	    if (state.getAttribute(STATE_MESSAGE) == null)
	    {
	        // show sucessful
	        state.setAttribute (STATE_MODE, MODE_LIST);
	        state.removeAttribute(STATE_SHOW_SET);
	        state.removeAttribute(STATE_NON_EMPTY_SHOW_SET);

	        if (((String) state.getAttribute (STATE_SELECT_ALL_FLAG)).equals (Boolean.TRUE.toString()))
	        {
	            state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());
	        }

	    }       // if-else

	}       // doFinalizeShow

	/**
	 * Hide the selected collection or resource items
	 */
	public void doFinalizeHide(RunData data)
	{
	    SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
	    state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

	    // cancel copy if there is one in progress
	    if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
	    {
	        initCopyContext(state);
	    }

	    // cancel move if there is one in progress
	    if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
	    {
	        initMoveContext(state);
	    }

	    ParameterParser params = data.getParameters ();

	    List<ListItem> items = (List) state.getAttribute(STATE_HIDE_SET);

	    // hide the lowest item in the hireachy first
	    Map<Integer, List<ListItem>> hideItems = new HashMap<>();
	    // String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
	    int maxDepth = 0;
	    int depth;

	    Iterator<ListItem> it = items.iterator();
	    while(it.hasNext())
	    {
	        ListItem item = it.next();
	        String[] parts = item.getId().split(Entity.SEPARATOR);
	        depth = parts.length;
	        if (depth > maxDepth)
	        {
	            maxDepth = depth;
	        }
	        List<ListItem> v = hideItems.get(depth);
	        if(v == null)
	        {
	            v = new ArrayList<>();
	        }
	        v.add(item);
	        hideItems.put(depth, v);
	    }
	    boolean isCollection = false;
	    for (int j=maxDepth; j>0; j--)
	    {
	        List<ListItem> v = hideItems.get(j);
	        if (v==null)
	        {
	            v = new ArrayList<>();
	        }
	        Iterator<ListItem> itemIt = v.iterator();
	        while(itemIt.hasNext())
	        {
	            ListItem item = itemIt.next();
	            try
	            {
	                if (item.isCollection())
	                {
	                    log.debug("show this collection resource{}", item.getId());
	                    ContentCollectionEdit edit= contentHostingService.editCollection(item.getId());
	                    edit.setAvailability(true, null, null);
	                    contentHostingService.commitCollection(edit);

	                }
	                else
	                {
	                    log.debug("show this non-collection resource {}", item.getId());
	                    ContentResourceEdit edit= contentHostingService.editResource(item.getId());
	                    edit.setAvailability(true, null, null);
	                    contentHostingService.commitResource(edit, 0);

	                }
	            }

	            catch (IdUnusedException e)
	            {
	                log.warn("IdUnusedException", e);
	            }
	            catch (TypeException e)
	            {
	                log.warn("TypeException", e);
	            }
	            catch (PermissionException e)
	            {
	                log.warn("PermissionException", e);
	            }
	            catch (ServerOverloadException e)
	            {
	                log.warn("ServerOverloadException", e);
	            }
	            catch (OverQuotaException e)
	            {
	                log.warn("OverQuotaException ", e);
	            }
	            catch (InUseException e)
	            {
	                log.warn("InUseException ", e);
	            }
	        }       // for
	    }       // for

	    if (state.getAttribute(STATE_MESSAGE) == null)
	    {
	        // Hide sucessful
	        state.setAttribute (STATE_MODE, MODE_LIST);
	        state.removeAttribute(STATE_HIDE_SET);
	        state.removeAttribute(STATE_NON_EMPTY_HIDE_SET);

	        if (((String) state.getAttribute (STATE_SELECT_ALL_FLAG)).equals (Boolean.TRUE.toString()))
	        {
	            state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());
	        }

	    }       // if-else

	}       // doFinalizeShow

// END SAK-23304 additions

	/**
	 * 
	 * @param state
	 * @param data
	 * @param selectedItemId
	 */
	protected void printFile(SessionState state, RunData data, String selectedItemId)
	{
		log.info("{}.printFile()", this);
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		ToolSession toolSession = sessionManager.getCurrentToolSession();

		Cookie cookie = null;
		HttpServletRequest req = data.getRequest();
		
		List<Object> params = new ArrayList<>();
		Cookie[] cookies = req.getCookies();
		 for(Cookie cookie1 : cookies) {
			 params.add( cookie1.getName() + "=" + cookie1.getValue() );
		 }
		
		try
		{
			ContentResource r = contentHostingService.getResource(selectedItemId);
			if (r != null)
			{
				try
				{
					//Upload the file
					HashMap<String, String> result = contentPrintService.printResource(r, params);
					if (result != null)
					{
						state.setAttribute(CONTENT_PRINT_CALL_RESPONSE, result);
					}
				}
				catch (Exception e)
				{
					// TODO: do something
					log.warn("{}.printFile() error with executeMultiPartRequest {}", this, r.getReference());
				}
			}
		}
		catch (IdUnusedException e)
		{
			log.warn("{}.printFile() IdUnusedException {}", this, selectedItemId);
		}
		catch (TypeException e)
		{
			log.warn("{}.printFile() TypeException {}", this, selectedItemId);
		}
		catch (PermissionException e)
		{
			log.warn("{}.printFile() PermissionException {}", this, selectedItemId);
		}
		
	}
	
	public void doViewTrash(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.setAttribute(STATE_MODE, MODE_RESTORE);
	}

	/**
	 * set the state name to be "zipDownloadfinish" if any item has been selected for zip downloading
	 * @param data
	 */
	public void doZipDownloadconfirm(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		Set<String> zipDownloadIdSet  = new TreeSet<>();
		String[] zipDownloadIds = data.getParameters ().getStrings ("selectedMembers");
		if (zipDownloadIds == null)
		{
			addAlert(state, rb.getString("choosefile3"));
		}
		else
		{
			zipDownloadIdSet.addAll(Arrays.asList(zipDownloadIds));
			zipDownloadItems(state, zipDownloadIdSet); 
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_MODE, MODE_ZIPDOWNLOAD_FINISH);
			state.setAttribute(STATE_LIST_SELECTIONS, zipDownloadIdSet);
		}
	}       // doZipDownloadconfirm

	/**
	 * @param state
	 * @param zipDownloadIdSet
	 */
	protected void zipDownloadItems(SessionState state, Set<String> zipDownloadIdSet)
	{
		List<ListItem> zipDownloadItems = new ArrayList<>();
		// Set to hold the names of files that exceed that maximum size for zipping. 
		Set<String> zipSingleFileSizeExceeded = new HashSet<>();

		// Set to hold the names of files that will get included in the zip. 
		Set<String> zipIncludedFiles = new HashSet<>();

		long zipMaxIndividualFileSize = Long.parseLong(ServerConfigurationService.getString("content.zip.download.maxindividualfilesize","0"));
		long zipMaxTotalSize = Long.parseLong(ServerConfigurationService.getString("content.zip.download.maxtotalsize","0"));
		long accumulatedSize=0;
		long currentEntitySize=0;

		ContentHostingService contentService = contentHostingService;

		for (String showId : zipDownloadIdSet) {
			ContentEntity entity = null;
			try {
				if (contentService.isCollection(showId)) {
					if (contentService.allowGetCollection(showId)) {
						entity = contentService.getCollection(showId);
						currentEntitySize = getCollectionRecursiveSize((ContentCollection) entity, zipMaxIndividualFileSize, zipMaxTotalSize, zipSingleFileSizeExceeded, zipIncludedFiles);
					}
				} else if (contentService.allowGetResource(showId)) {
					entity = contentService.getResource(showId);
					currentEntitySize = ((ContentResource) entity).getContentLength();
					if (currentEntitySize > zipMaxIndividualFileSize) {
						// Work out the file path without the site ID.
						String filePath = entity.getId().replace("/group/" + toolManager.getCurrentPlacement().getContext(), "");
						zipSingleFileSizeExceeded.add(filePath);
					}
				}

				accumulatedSize = accumulatedSize + currentEntitySize;

				if (accumulatedSize > zipMaxTotalSize) {
					throw new ZipMaxTotalSizeException();
				}

				ListItem item = new ListItem(entity);
				if (item.isCollection() && contentService.allowGetCollection(showId)) {
					item.setSize(ResourcesAction.getFileSizeString(getCollectionRecursiveSize((ContentCollection) entity, zipMaxIndividualFileSize, zipMaxTotalSize, zipSingleFileSizeExceeded, new HashSet<>()), rb));
					zipDownloadItems.add(item);
				} else if (!item.isCollection() && contentService.allowGetResource(showId)) {
					zipDownloadItems.add(item);
				}
			} catch (ZipMaxTotalSizeException tse) {
				addAlert(state, trb.getFormattedMessage("zipdownload.maxTotalSize", getFileSizeString(zipMaxTotalSize, rb)));
				state.setAttribute(STATE_MODE, MODE_LIST);
				// abort loop so alert not repeated.
				break;
			} catch (IdUnusedException ide) {
				log.warn("IdUnusedException", ide);
			} catch (TypeException te) {
				log.warn("TypeException", te);
			} catch (PermissionException pe) {
				log.warn("PermissionException", pe);
			}
		}
		if (!zipSingleFileSizeExceeded.isEmpty()) {
			// We want to alert about all files that exceed the individual zip size so need to process the whole zipDownloadIdSet before alerting.
			for (String zipSingleFile : zipSingleFileSizeExceeded) {
				addAlert(state, trb.getFormattedMessage("zipdownload.maxIndividualSizeInFolder", zipSingleFile, getFileSizeString(zipMaxIndividualFileSize, rb)));
			}
			state.setAttribute(STATE_MODE, MODE_LIST);
		}
		state.setAttribute (STATE_ZIPDOWNLOAD_SET, zipDownloadItems);
	}

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	public String buildZipDownloadFinishContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		context.put ("collectionId", state.getAttribute (STATE_COLLECTION_ID) );

		List zipDownloadItems = (List) state.getAttribute(STATE_ZIPDOWNLOAD_SET);
		context.put ("zipDownloadItems", zipDownloadItems);

		return TEMPLATE_ZIPDOWNLOAD_FINISH;
	}

	public void doFinalizeZipDownload(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		List<ListItem> zipDownloadItems = (List<ListItem>) state.getAttribute(STATE_ZIPDOWNLOAD_SET);

		ThreadLocalManager threadLocalManager = ComponentManager.get(ThreadLocalManager.class);
		HttpServletResponse response = (HttpServletResponse)threadLocalManager.get(RequestFilter.CURRENT_HTTP_RESPONSE);

		List<String> selectedFolderIds = new ArrayList<>();
		List<String> selectedFiles = new ArrayList<>();
		for (ListItem listItem : zipDownloadItems) {
			if (listItem.isCollection()) {
				selectedFolderIds.add(listItem.getId());
			} else {
				selectedFiles.add(listItem.getId());
			}
			eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_ZIP_DOWNLOAD, "/content" + listItem.getId() , false));
		}

		// Use the site title for the zip name, remove spaces though.
		String siteTitle = (String) state.getAttribute(STATE_SITE_TITLE);
		siteTitle = siteTitle.replace(" ", "");
		new ZipContentUtil(contentHostingService, ServerConfigurationService.getInstance(), sessionManager).compressSelectedResources((String)state.getAttribute(STATE_SITE_ID), siteTitle, selectedFolderIds, selectedFiles, response);
	}

	private long getCollectionRecursiveSize(ContentCollection currentCollection, long maxIndividualFileSize, long zipMaxTotalSize, Set<String> zipSingleFileSizeExceeded, Set<String> zipIncludedFiles) throws ZipMaxTotalSizeException
	{
		long total=0;

		List items = currentCollection.getMemberResources();
		Iterator it = items.iterator();
		while(it.hasNext())
		{
			ContentEntity myElement = (ContentEntity) it.next();
			if (myElement.isResource()) 
			{
				long tempSize = ((ContentResource)myElement).getContentLength();
				String filePath = myElement.getId().replace("/group/" + toolManager.getCurrentPlacement().getContext(), "");
				if (tempSize > maxIndividualFileSize) {
					// Work out the file path without the site ID.

					zipSingleFileSizeExceeded.add(filePath);
				}
				else {
					if (!zipIncludedFiles.contains(filePath)) {
						total=total+tempSize;
						zipIncludedFiles.add(filePath);
					}
				}
			}
			else if (myElement.isCollection())
			{
				long tempSize = getCollectionRecursiveSize((ContentCollection)myElement, maxIndividualFileSize, zipMaxTotalSize, zipSingleFileSizeExceeded, zipIncludedFiles);

				if (tempSize > zipMaxTotalSize) {
					throw new ZipMaxTotalSizeException();
				}
				else {total=total+tempSize;}
			}
		}
		return total;
	}

}	// ResourcesAction
