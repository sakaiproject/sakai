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

package org.sakaiproject.content.tool;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceHelperAction;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.GroupAwareEntity;
import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.content.api.MultiFileUploadPipe;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.SiteSpecificResourceType;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.api.providers.SiteContentAdvisor;
import org.sakaiproject.content.api.providers.SiteContentAdvisorProvider;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.event.cover.UsageSessionService;
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
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
* <p>ResourceAction is a ContentHosting application</p>
*
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
public class ResourcesAction 
	extends PagedResourceHelperAction // VelocityPortletPaneledAction
{
	/**
	 * Action
	 *
	 */
	public class Action
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
	
	/**
	 *
	 * inner class encapsulates information about groups of metadata tags (such as DC, LOM, etc.)
	 *
	 */
	public static class MetadataGroup
		extends ArrayList
	{
		/**
		 *
		 */
		private static final long serialVersionUID = -821054142728929236L;
		protected boolean m_isShowing;
		protected String m_name;

		/**
		 * @param name
		 */
		public MetadataGroup(String name)
		{
			super();
			m_name = name;
			m_isShowing = false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 * needed to determine List.contains()
		 */
		public boolean equals(Object obj)
		{
			MetadataGroup mg = (MetadataGroup) obj;
			boolean rv = (obj != null) && (m_name.equals(mg));
			return rv;
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
		public boolean isShowing()
		{
			return m_isShowing;
		}

		/**
		 * @param name
		 */
		public void setName(String name)
		{
			m_name = name;
		}

		/**
		 * @param isShowing
		 */
		public void setShowing(boolean isShowing)
		{
			m_isShowing = isShowing;
		}

		public String getShowLabel()
		{
			return trb.getFormattedMessage("metadata.show", new String[]{this.m_name});
		}
		
		public String getHideLabel()
		{
			return trb.getFormattedMessage("metadata.hide", new String[]{this.m_name});
		}
	}
	
	/** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("content");
	/** Resource bundle using current language locale */
    public static ResourceLoader trb = new ResourceLoader("types");
	static final Log logger = LogFactory.getLog(ResourcesAction.class);

	public static final String PREFIX = "resources.";
	
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


	private static final String STATE_DEFAULT_COPYRIGHT = PREFIX + "default_copyright";

	private static final String STATE_DEFAULT_COPYRIGHT_ALERT = PREFIX + "default_copyright_alert";
	
	private static final String COPYRIGHT_ALERT_URL = ServerConfigurationService.getAccessUrl() + COPYRIGHT_PATH;

	private static final String STATE_COPYRIGHT_FAIRUSE_URL = PREFIX + "copyright_fairuse_url";
	
	private static final String STATE_COPYRIGHT_NEW_COPYRIGHT = PREFIX + "new_copyright";
	
	/** copyright related info */
	private static final String STATE_COPYRIGHT_TYPES = PREFIX + "copyright_types";
	
	private static final int CREATE_MAX_ITEMS = 10;
    
	/** The default number of site collections per page. */
	protected static final int DEFAULT_PAGE_SIZE = 50;
	
	public static final String DELIM = "@";

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

	public static final String MODE_ATTACHMENT_CONFIRM = "resources.attachment_confirm";

	public static final String MODE_ATTACHMENT_CONFIRM_INIT = "resources.attachment_confirm_initialized";

	public static final String MODE_ATTACHMENT_CREATE = "resources.attachment_create";

	public static final String MODE_ATTACHMENT_CREATE_INIT = "resources.attachment_create_initialized";

	public static final String MODE_ATTACHMENT_DONE = "resources.attachment_done";

	public static final String MODE_ATTACHMENT_EDIT_ITEM = "resources.attachment_edit_item";

	public static final String MODE_ATTACHMENT_EDIT_ITEM_INIT = "resources.attachment_edit_item_initialized";

	public static final String MODE_ATTACHMENT_NEW_ITEM = "resources.attachment_new_item";

	public static final String MODE_ATTACHMENT_NEW_ITEM_INIT = "resources.attachment_new_item_initialized";

	/** modes for attachment helper */
	public static final String MODE_ATTACHMENT_SELECT = "resources.attachment_select";

	public static final String MODE_ATTACHMENT_SELECT_INIT = "resources.attachment_select_initialized";

	private static final String MODE_CREATE_WIZARD = "createWizard";

	/************** the more context *****************************************/

	private static final String MODE_DAV = "webdav";

	/************** the edit context *****************************************/

	private static final String MODE_DELETE_FINISH = "deleteFinish";

	public  static final String MODE_HELPER = "helper";
	
	/** Modes. */
	private static final String MODE_LIST = "list";
	private static final String MODE_MORE = "more";

	private static final String MODE_PROPERTIES = "properties";
	
	private static final String MODE_REORDER = "reorder";
	
	protected static final String MODE_REVISE_METADATA = "revise_metadata";

	private static final String STATE_NEW_COPYRIGHT_INPUT = PREFIX + "new_copyright_input";

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
	private static final String STATE_COLLECTION_ID = PREFIX + "collection_id";
	
	
	/** The collection id path */
	private static final String STATE_COLLECTION_PATH = PREFIX + "collection_path";
	
	/** The name of the state attribute containing BrowseItems for all content collections the user has access to */
	private static final String STATE_COLLECTION_ROOTS = PREFIX + "collection_rootie_tooties";
	
	public static final String STATE_COLUMN_ITEM_ID = PREFIX + "state_column_item_id";

	/** The content hosting service in the State. */
	private static final String STATE_CONTENT_SERVICE = PREFIX + "content_service";
	
	/** The content type image lookup service in the State. */
	private static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = PREFIX + "content_type_image_service";
	
	/** The copied item ids */
	private static final String STATE_COPIED_IDS = PREFIX + "revise_copied_ids";
	
	/** The copy flag */
	private static final String STATE_COPY_FLAG = PREFIX + "copy_flag";
	
	//	public static final String STATE_CREATE_TYPE = PREFIX + "create_type";
	public static final String STATE_CREATE_COLLECTION_ID = PREFIX + "create_collection_id";

	protected static final String STATE_CREATE_MESSAGE = PREFIX + "create_message";

	public static final String STATE_CREATE_NUMBER = PREFIX + "create_number";

	protected static final String STATE_CREATE_WIZARD_ACTION = PREFIX + "create_wizard_action";

	protected static final String STATE_CREATE_WIZARD_COLLECTION_ID = PREFIX + "create_wizard_collection_id";

	protected static final String STATE_CREATE_WIZARD_ITEM = PREFIX + "create_wizard_item";

	/** name of state attribute for the default retract time */
	protected static final String STATE_DEFAULT_RETRACT_TIME = PREFIX + "default_retract_time";

	/** The name of the state attribute containing a list of ListItem objects corresponding to resources selected for deletion */
	private static final String STATE_DELETE_ITEMS = PREFIX + "delete_items";

	/** The name of the state attribute containing a list of ListItem objects corresponding to nonempty folders selected for deletion */
	private static final String STATE_DELETE_ITEMS_NOT_EMPTY = PREFIX + "delete_items_not_empty";

	protected static final String STATE_DELETE_SET = PREFIX + "delete_set";
	
	protected static final String STATE_DROPBOX_HIGHLIGHT = PREFIX + "dropbox_highlight";

	/** The name of the state attribute indicating whether the hierarchical list is expanded */
	private static final String STATE_EXPAND_ALL_FLAG = PREFIX + "expand_all_flag";

	/** Name of state attribute indicating number of members for a collection at which this tool should refuse to expand the collection. */
	private static final String STATE_EXPANDABLE_FOLDER_SIZE_LIMIT = PREFIX + "expandable_folder_size_limit";

	/** Name of state attribute containing a list of opened/expanded collections */
	private static final String STATE_EXPANDED_COLLECTIONS = PREFIX + "expanded_collections";
	
	protected static final String STATE_EXPANDED_FOLDER_SORT_MAP = PREFIX + "expanded_folder_sort_map";
	
	/** state attribute for the maximum size for file upload */
	static final String STATE_FILE_UPLOAD_MAX_SIZE = PREFIX + "file_upload_max_size";
	
	/** The from state name */
	private static final String STATE_FROM = PREFIX + "from";
	
	/** State attribute for where there is at least one attachment before invoking attachment tool */
	public static final String STATE_HAS_ATTACHMENT_BEFORE = PREFIX + "has_attachment_before";
	
	/**
	 *  the name of the state attribute indicating that the user canceled out of the helper.  Is set only if the user canceled out of the helper. 
	 */
	public static final String STATE_HELPER_CANCELED_BY_USER = PREFIX + "state_attach_canceled_by_user";

	protected static final String STATE_HIGHLIGHTED_ITEMS = PREFIX + "highlighted_items";
	
	/** The display name of the "home" collection (can't go up from here.) */
	private static final String STATE_HOME_COLLECTION_DISPLAY_NAME = PREFIX + "collection_home_display_name";
	
	/** The id of the "home" collection (can't go up from here.) */
	private static final String STATE_HOME_COLLECTION_ID = PREFIX + "collection_home";

	/** Name of state attribute for status of initialization.  */
	private static final String STATE_INITIALIZED = PREFIX + "initialized";
	
	protected static final String STATE_ITEMS_TO_BE_COPIED = PREFIX + "items_to_be_copied";
	
	protected static final String STATE_ITEMS_TO_BE_MOVED = PREFIX + "items_to_be_moved";

	private static final String STATE_LIST_PREFERENCE = PREFIX + "state_list_preference";
	
	/** The name of the state attribute containing a java.util.Set with the id's of selected items */
	private static final String STATE_LIST_SELECTIONS = PREFIX + "ignore_delete_selections";
	
	protected static final String STATE_LIST_VIEW_SORT = PREFIX + "list_view_sort";

	private static final String STATE_MESSAGE_LIST = PREFIX + "message_list";

	/** The resources, helper or dropbox mode. */
	public static final String STATE_MODE_RESOURCES = PREFIX + "resources_mode";
	
	/** The more collection id */
	private static final String STATE_MORE_COLLECTION_ID = PREFIX + "more_collection_id";
	
	/** The more id */
	private static final String STATE_MORE_ID = PREFIX + "more_id";
	
	private static final String STATE_MORE_ACTION = PREFIX + "more_action";
	
	private static final String STATE_MORE_ITEM = PREFIX + "more_item";
	
	/** The move flag */
	private static final String STATE_MOVE_FLAG = PREFIX + "move_flag";
	
	/** The copied item ids */
	private static final String STATE_MOVED_IDS = PREFIX + "revise_moved_ids";
	
	/** The user copyright string */
	private static final String	STATE_MY_COPYRIGHT = PREFIX + "mycopyright";
	
	/** The root of the navigation breadcrumbs for a folder, either the home or another site the user belongs to */
	private static final String STATE_NAVIGATION_ROOT = PREFIX + "navigation_root";

	/** The name of the state attribute indicating whether the hierarchical list needs to be expanded */
	private static final String STATE_NEED_TO_EXPAND_ALL = PREFIX + "need_to_expand_all";

	protected static final String STATE_NON_EMPTY_DELETE_SET = PREFIX + "non-empty_delete_set";
	
	/** The can-paste flag */
	private static final String STATE_PASTE_ALLOWED_FLAG = PREFIX + "can_paste_flag";
	
	/** state attribute indicating whether users in current site should be denied option of making resources public */
	private static final String STATE_PREVENT_PUBLIC_DISPLAY = PREFIX + "prevent_public_display";
	
	protected static final String STATE_REMOVED_ATTACHMENTS = PREFIX + "removed_attachments";

	protected static final String STATE_REORDER_FOLDER = PREFIX + "reorder_folder_id";

	protected static final String STATE_REORDER_SORT = PREFIX + "reorder_sort";
	
	/** The sort ascending or decending for the reorder context */
	protected static final String STATE_REORDER_SORT_ASC = PREFIX + "sort_asc";

	/** The property (column) to sort by in the reorder context */
	protected static final String STATE_REORDER_SORT_BY = PREFIX + "reorder_sort_by";

	/** The resources, helper or dropbox mode. */
	public static final String STATE_RESOURCES_HELPER_MODE = PREFIX + "resources_helper_mode";
	
	private static final String STATE_RESOURCES_TYPE_REGISTRY = PREFIX + "type_registry";
	
	protected static final String STATE_REVISE_PROPERTIES_ACTION = PREFIX + "revise_properties_action";
	
	protected static final String STATE_REVISE_PROPERTIES_ENTITY_ID = PREFIX + "revise_properties_entity_id";
	
	protected static final String STATE_REVISE_PROPERTIES_ITEM = PREFIX + "revise_properties_item";
	
	/** The select all flag */
	private static final String STATE_SELECT_ALL_FLAG = PREFIX + "select_all_flag";
	
	/** The name of a state attribute indicating whether the resources tool/helper is allowed to show all sites the user has access to */
	public static final String STATE_SHOW_ALL_SITES = PREFIX + "allow_user_to_see_all_sites";
	
	protected static final String STATE_SHOW_COPY_ACTION = PREFIX + "show_copy_action";
	
	private static final String STATE_SHOW_FORM_ITEMS = PREFIX + "show_form_items";
	
	protected static final String STATE_SHOW_MOVE_ACTION = PREFIX + "show_move_action";

	/** The name of a state attribute indicating whether the wants to see other sites if that is enabled */
	public static final String STATE_SHOW_OTHER_SITES = PREFIX + "user_chooses_to_see_other_sites";

	protected static final String STATE_SHOW_REMOVE_ACTION = PREFIX + "show_remove_action";

	/** the site title */
	private static final String STATE_SITE_TITLE = PREFIX + "site_title";

	/** The sort ascending or decending */
	private static final String STATE_SORT_ASC = PREFIX + "sort_asc";

	/** The sort by */
	private static final String STATE_SORT_BY = PREFIX + "sort_by";

	public static final String STATE_STACK_CREATE_COLLECTION_ID = PREFIX + "stack_create_collection_id";

	public static final String STATE_STACK_CREATE_NUMBER = PREFIX + "stack_create_number";

	public static final String STATE_STACK_CREATE_TYPE = PREFIX + "stack_create_type";

	public static final String STATE_STACK_EDIT_COLLECTION_ID = PREFIX + "stack_edit_collection_id";

	public static final String STATE_STACK_EDIT_ID = PREFIX + "stack_edit_id";

	public static final String STATE_STACK_STRUCTOBJ_TYPE = PREFIX + "stack_create_structured_object_type";

	public static final String STATE_SUSPENDED_OPERATIONS_STACK = PREFIX + "suspended_operations_stack";

	public static final String STATE_SUSPENDED_OPERATIONS_STACK_DEPTH = PREFIX + "suspended_operations_stack_depth";

	protected static final String STATE_TOP_MESSAGE_INDEX = PREFIX + "top_message_index";

	/** state attribute indicating whether we're using the Creative Commons dialog instead of the "old" copyright dialog */
	protected static final String STATE_USING_CREATIVE_COMMONS = PREFIX + "usingCreativeCommons";

	/** vm files for each mode. */
	private static final String TEMPLATE_DAV = "content/chef_resources_webdav";

	private static final String TEMPLATE_DELETE_CONFIRM = "content/chef_resources_deleteConfirm";

	private static final String TEMPLATE_DELETE_FINISH = "content/sakai_resources_deleteFinish";

	private static final String TEMPLATE_MORE = "content/chef_resources_more";

	private static final String TEMPLATE_NEW_LIST = "content/sakai_resources_list";

	private static final String TEMPLATE_OPTIONS = "content/sakai_resources_options";
	
	private static final String TEMPLATE_REORDER = "content/chef_resources_reorder";

	private static final String TEMPLATE_REVISE_METADATA = "content/sakai_resources_properties";

	public static final String TYPE_HTML = MIME_TYPE_DOCUMENT_HTML;

	public static final String TYPE_TEXT = MIME_TYPE_DOCUMENT_PLAINTEXT;
	
	public static final String TYPE_UPLOAD = "file";
	
	public static final String TYPE_URL = "Url";
	
	public static final String UTF_8_ENCODING = "UTF-8";
	
	// may need to distinguish permission on entity vs permission on its containing collection
	static
	{
		CONTENT_NEW_ACTIONS.add(ActionType.NEW_UPLOAD);
		CONTENT_NEW_ACTIONS.add(ActionType.NEW_FOLDER);
		CONTENT_NEW_ACTIONS.add(ActionType.NEW_URLS);
		CONTENT_NEW_ACTIONS.add(ActionType.CREATE);
		
		PASTE_COPIED_ACTIONS.add(ActionType.PASTE_COPIED);
		PASTE_MOVED_ACTIONS.add(ActionType.PASTE_MOVED);
		
		CONTENT_NEW_FOR_PARENT_ACTIONS.add(ActionType.DUPLICATE);
		
		CONTENT_READ_ACTIONS.add(ActionType.VIEW_CONTENT);
		CONTENT_READ_ACTIONS.add(ActionType.COPY);
		
		CONTENT_PROPERTIES_ACTIONS.add(ActionType.VIEW_METADATA);
		
		CONTENT_MODIFY_ACTIONS.add(ActionType.REVISE_METADATA);
		CONTENT_MODIFY_ACTIONS.add(ActionType.REVISE_CONTENT);
		CONTENT_MODIFY_ACTIONS.add(ActionType.REPLACE_CONTENT);
		CONTENT_MODIFY_ACTIONS.add(ActionType.REVISE_ORDER);
		CONTENT_MODIFY_ACTIONS.add(ActionType.COMPRESS_ZIP_FOLDER);
		CONTENT_MODIFY_ACTIONS.add(ActionType.EXPAND_ZIP_ARCHIVE);
		
		CONTENT_DELETE_ACTIONS.add(ActionType.MOVE);
		CONTENT_DELETE_ACTIONS.add(ActionType.DELETE);
		
		SITE_UPDATE_ACTIONS.add(ActionType.REVISE_PERMISSIONS);

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

		ACTIONS_ON_MULTIPLE_ITEMS.add(ActionType.COPY);
		ACTIONS_ON_MULTIPLE_ITEMS.add(ActionType.MOVE);
		ACTIONS_ON_MULTIPLE_ITEMS.add(ActionType.DELETE);
		
		CREATION_ACTIONS.add(ActionType.NEW_UPLOAD);
		CREATION_ACTIONS.add(ActionType.NEW_FOLDER);
		CREATION_ACTIONS.add(ActionType.NEW_URLS);
		CREATION_ACTIONS.add(ActionType.CREATE);
		CREATION_ACTIONS.add(ActionType.PASTE_MOVED);
		CREATION_ACTIONS.add(ActionType.PASTE_COPIED);
	}

	/**
	 * Add additional resource pattern to the observer
	 *@param pattern The pattern value to be added
	 *@param state The state object
	 */
	private static void addObservingPattern(String pattern, SessionState state)
	{
//		// get the observer and add the pattern
//		ContentObservingCourier o = (ContentObservingCourier) state.getAttribute(STATE_OBSERVER);
//		o.addResourcePattern(ContentHostingService.getReference(pattern));
//
//		// add it back to state
//		state.setAttribute(STATE_OBSERVER, o);

	}	// addObservingPattern

	/**
	* Build the context to show the list of resource properties
	*/
	public String buildMoreContext (	VelocityPortlet portlet,
									Context context,
									RunData data,
									SessionState state)
	{
		context.put("tlang",rb);
		
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		String entityId = (String) state.getAttribute(STATE_MORE_ID);
		context.put ("id", entityId);
		String collectionId = (String) state.getAttribute(STATE_MORE_COLLECTION_ID);
		context.put ("collectionId", collectionId);
		String homeCollectionId = (String) (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
		context.put("homeCollectionId", homeCollectionId);
		//List cPath = getCollectionPath(state);
		//context.put ("collectionPath", cPath);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);
		context.put("navRoot", navRoot);
		
		ListItem item = new ListItem(entityId);
		context.put("item", item);


		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			context.put("notExistFlag", new Boolean(false));
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
				boolean pubview = ContentHostingService.isInheritingPubView(entityId);
				if (!pubview) pubview = ContentHostingService.isPubView(entityId);
				context.put("pubview", new Boolean(pubview));
			}

		}
		
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));

		return TEMPLATE_MORE;

	}	// buildMoreContext

	//	/**
//	 * Retrieve values for one or more items from create context.  Create context contains up to ten items at a time
//	 * all of the same type (folder, file, text document, structured-artifact, etc).  This method retrieves the data
//	 * apppropriate to the type and updates the values of the ResourcesEditItem objects stored as the STATE_STACK_CREATE_ITEMS
//	 * attribute in state. If the third parameter is "true", missing/incorrect user inputs will generate error messages
//	 * and attach flags to the input elements.
//	 * @param state
//	 * @param params
//	 * @param markMissing Should this method generate error messages and add flags for missing/incorrect user inputs?
//	 */
//	protected static void captureMultipleValues(SessionState state, ParameterParser params, boolean markMissing)
//	{
//		Map current_stack_frame = peekAtStack(state);
//		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
//		if(number == null)
//		{
//			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
//			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
//		}
//		if(number == null)
//		{
//			number = new Integer(1);
//			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
//		}
//
//		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
//		if(new_items == null)
//		{
//			String collectionId = params.getString("collectionId");
//			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
//			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
//			{
//				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
//				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
//			}
//
//			
//			String encoding = (String) state.getAttribute(STATE_ENCODING);
//
//			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
//
//			Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
//			if(defaultRetractDate == null)
//			{
//				defaultRetractDate = TimeService.newTime();
//				state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
//			}
//
//		}
//
//		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
//		if(alerts == null)
//		{
//			alerts = new HashSet();
//			state.setAttribute(STATE_CREATE_ALERTS, alerts);
//		}
//		int actualCount = 0;
//		Set first_item_alerts = null;
//
//		String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
//		int max_bytes = 1024 * 1024;
//		try
//		{
//			max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
//		}
//		catch(Exception e)
//		{
//			// if unable to parse an integer from the value
//			// in the properties file, use 1 MB as a default
//			max_file_size_mb = "1";
//			max_bytes = 1024 * 1024;
//		}
//
//		/*
//		// params.getContentLength() returns m_req.getContentLength()
//		if(params.getContentLength() > max_bytes)
//		{
//			alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
//			state.setAttribute(STATE_CREATE_ALERTS, alerts);
//
//			return;
//		}
//		*/
//		for(int i = 0; i < number.intValue(); i++)
//		{
//			ResourcesEditItem item = (ResourcesEditItem) new_items.get(i);
//			Set item_alerts = captureValues(item, i, state, params, markMissing);
//			if(i == 0)
//			{
//				first_item_alerts = item_alerts;
//			}
//			else if(item.isBlank())
//			{
//				item.clearMissing();
//			}
//			if(! item.isBlank())
//			{
//				alerts.addAll(item_alerts);
//				actualCount ++;
//			}
//		}
//		if(actualCount > 0)
//		{
//			ResourcesEditItem item = (ResourcesEditItem) new_items.get(0);
//			if(item.isBlank())
//			{
//				item.clearMissing();
//			}
//		}
//		else if(markMissing)
//		{
//			alerts.addAll(first_item_alerts);
//		}
//		state.setAttribute(STATE_CREATE_ALERTS, alerts);
//		current_stack_frame.put(STATE_STACK_CREATE_ACTUAL_COUNT, Integer.toString(actualCount));
//
//	}	// captureMultipleValues
//
	/**
	 *
	 * put copyright info into context
	 */
	public static void copyrightChoicesIntoContext(SessionState state, Context context)
	{
		boolean usingCreativeCommons = state.getAttribute(STATE_USING_CREATIVE_COMMONS) != null && state.getAttribute(STATE_USING_CREATIVE_COMMONS).equals(Boolean.TRUE.toString());		
		
		if(usingCreativeCommons)
		{
			
			String ccOwnershipLabel = "Who created this resource?";
			List ccOwnershipList = new ArrayList();
			ccOwnershipList.add("-- Select --");
			ccOwnershipList.add("I created this resource");
			ccOwnershipList.add("Someone else created this resource");
			
			String ccMyGrantLabel = "Terms of use";
			List ccMyGrantOptions = new ArrayList();
			ccMyGrantOptions.add("-- Select --");
			ccMyGrantOptions.add("Use my copyright");
			ccMyGrantOptions.add("Use Creative Commons License");
			ccMyGrantOptions.add("Use Public Domain Dedication");
			
			String ccCommercialLabel = "Allow commercial use?";
			List ccCommercialList = new ArrayList();
			ccCommercialList.add("Yes");
			ccCommercialList.add("No");
			
			String ccModificationLabel = "Allow Modifications?";
			List ccModificationList = new ArrayList();
			ccModificationList.add("Yes");
			ccModificationList.add("Yes, share alike");
			ccModificationList.add("No");
			
			String ccOtherGrantLabel = "Terms of use";
			List ccOtherGrantList = new ArrayList();
			ccOtherGrantList.add("Subject to fair-use exception");
			ccOtherGrantList.add("Public domain (created before copyright law applied)");
			ccOtherGrantList.add("Public domain (copyright has expired)");
			ccOtherGrantList.add("Public domain (government document not subject to copyright)");
			
			String ccRightsYear = "Year";
			String ccRightsOwner = "Copyright owner";
			
			String ccAcknowledgeLabel = "Require users to acknowledge author's rights before access?";
			List ccAcknowledgeList = new ArrayList();
			ccAcknowledgeList.add("Yes");
			ccAcknowledgeList.add("No");
			
			String ccInfoUrl = "";
			
			int year = TimeService.newTime().breakdownLocal().getYear();
			String username = UserDirectoryService.getCurrentUser().getDisplayName(); 

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
			if (state.getAttribute(STATE_COPYRIGHT_FAIRUSE_URL) != null)
			{
				context.put("fairuseurl", state.getAttribute(STATE_COPYRIGHT_FAIRUSE_URL));
			}
			if (state.getAttribute(STATE_NEW_COPYRIGHT_INPUT) != null)
			{
				context.put("newcopyrightinput", state.getAttribute(STATE_NEW_COPYRIGHT_INPUT));
			}
	
			if (state.getAttribute(STATE_COPYRIGHT_TYPES) != null)
			{
				List copyrightTypes = (List) state.getAttribute(STATE_COPYRIGHT_TYPES);
				context.put("copyrightTypes", copyrightTypes);
				context.put("copyrightTypesSize", new Integer(copyrightTypes.size() - 1));
				context.put("USE_THIS_COPYRIGHT", copyrightTypes.get(copyrightTypes.size() - 1));
			}
		}
		
	}	// copyrightChoicesIntoContext
	
	public static void publicDisplayChoicesIntoContext(SessionState state, Context context)
	{
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
		List<ContentCollection> new_collections = new ArrayList<ContentCollection>();
		String collectionId = pipe.getContentEntity().getId();
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) pipe;
		Iterator<ResourceToolActionPipe> pipeIt = mfp.getPipes().iterator();
		while(pipeIt.hasNext())
		{
			ResourceToolActionPipe fp = pipeIt.next();
			String name = fp.getFileName();
			if(name == null || name.trim().equals(""))
			{
				continue;
			}
			try
			{
				ContentCollectionEdit edit = ContentHostingService.addCollection(collectionId, Validator.escapeResourceName(name));
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
				if(displayName == null || displayName.trim().equals(""))
				{
					displayName = name;
				}
				if(displayName != null)
				{
					resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
				}
				Map values = pipe.getRevisedResourceProperties(); 	 
				Iterator valueIt = values.keySet().iterator(); 	 
				while(valueIt.hasNext()) 	 
				{ 	 
					String pname = (String) valueIt.next(); 	 
					String pvalue = (String) values.get(pname); 	 
					resourceProperties.addProperty(pname, pvalue);
				}
				ContentHostingService.commitCollection(edit);
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
				logger.warn("IdInvalidException " + collectionId + name, e);
			}
			catch (IdUsedException e)
			{
				String[] args = { name };
				addAlert(state, trb.getFormattedMessage("alert.exists", args));
				// logger.warn("IdUsedException ", e);
			}
			catch (IdUnusedException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdUnusedException " + collectionId + name, e);
				break;
			}
			catch (IdLengthException e)
			{
				String[] args = { name };
				addAlert(state, trb.getFormattedMessage("alert.toolong", args));
				logger.warn("IdLengthException " + collectionId + name, e);
			}
			catch (TypeException e)
			{
				// TODO Auto-generated catch block
				logger.warn("TypeException id = " + collectionId + name, e);
			}
		}
		return (new_collections.isEmpty() ? null : new_collections);
	}

	/**
	 * @param pipe
	 */
	public static List<ContentResource> createResources(ResourceToolActionPipe pipe)
	{
		ToolSession toolSession = null;
		boolean item_added = false;
		String collectionId = null;
		List<ContentResource> new_resources = new ArrayList<ContentResource>();
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) pipe;
		Iterator<ResourceToolActionPipe> pipeIt = mfp.getPipes().iterator();
		while(pipeIt.hasNext())
		{
			ResourceToolActionPipe fp = pipeIt.next();
			collectionId = pipe.getContentEntity().getId();
			String name = fp.getFileName();
			if(name == null || name.trim().equals(""))
			{
				continue;
			}
			String basename = name.trim();
			String extension = "";
			if(name.contains("."))
			{
				String[] parts = name.split("\\.");
				basename = parts[0];
				if(parts.length > 1)
				{
					extension = parts[parts.length - 1];
				}
				
				for(int i = 1; i < parts.length - 1; i++)
				{
					basename += "." + parts[i];
					// extension = parts[i + 1];
				}
			}
			try
			{
				ContentResourceEdit resource = ContentHostingService.addResource(collectionId,Validator.escapeResourceName(basename),Validator.escapeResourceName(extension),MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				
				extractContent(fp, resource);
//			    byte[] content = fp.getRevisedContent();
//			    if(content == null)
//			    {
//			    	InputStream stream = fp.getRevisedContentStream();
//			    	if(stream == null)
//			    	{
//			    		logger.debug("pipe with null content and null stream: " + pipe.getFileName());
//			    	}
//			    	else
//			    	{
//			    		resource.setContent(stream);
//			    	}
//			    }
//			    else
//			    {
//			    	resource.setContent(content);
//			    }

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
				if(displayName == null || displayName.trim().equals(""))
				{
					displayName = name;
				}
				if(displayName != null)
				{
					resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
				}
				Map values = pipe.getRevisedResourceProperties(); 	 
				Iterator valueIt = values.keySet().iterator(); 	 
				while(valueIt.hasNext()) 	 
				{ 	 
					String pname = (String) valueIt.next(); 	 
					String pvalue = (String) values.get(pname); 	 
					resourceProperties.addProperty(pname, pvalue);
				}
				
//				if(MIME_TYPE_DOCUMENT_HTML.equals(fp.getRevisedMimeType()) || MIME_TYPE_DOCUMENT_PLAINTEXT.equals(fp.getRevisedMimeType()))
//				{
//					resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, UTF_8_ENCODING);
//				}
				
				try
				{
					ContentHostingService.commitResource(resource, notification);
					item_added = true;
					new_resources.add(resource);
				}
				catch(OverQuotaException e)
				{
					addAlert(trb.getFormattedMessage("alert.overquota", new String[]{name}));
					logger.debug("OverQuotaException " + e);
					try
					{
						ContentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						logger.debug("Unable to remove partially completed resource: " + resource.getId() + "\n" + e); 
					}
				}
				catch(ServerOverloadException e)
				{
					addAlert(trb.getFormattedMessage("alert.unable1", new String[]{name}));
					logger.debug("ServerOverloadException " + e);
					try
					{
						ContentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						logger.debug("Unable to remove partially completed resource: " + resource.getId() + "\n" + e); 
					}
				}
			}
			catch (PermissionException e)
			{
				addAlert(trb.getString("alert.perm"));
				logger.warn("PermissionException ", e);
			}
			catch (IdUnusedException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdUsedException ", e);
			}
			catch (IdInvalidException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdInvalidException ", e);
			}
			catch (IdUniquenessException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdUniquenessException ", e);
			}
			catch (IdLengthException e)
			{
				addAlert(trb.getFormattedMessage("alert.toolong", new String[]{name}));
				
				// TODO Need to give error message to user
				logger.warn("IdLengthException " + e);
			}
			catch (OverQuotaException e)
			{
				addAlert(trb.getFormattedMessage("alert.overquota", new String[]{name}));
				logger.warn("OverQuotaException " + e);
			}
			catch (ServerOverloadException e)
			{
				addAlert(trb.getFormattedMessage("alert.unable1", new String[]{name}));
				logger.warn("ServerOverloadException " + e);
			}
		}
		
		return (item_added ? new_resources : null);
	}
	
	/**
     * Utility method to get revised content either from a byte array or a stream.
     */
    protected static void extractContent(ResourceToolActionPipe pipe, ContentResourceEdit resource)
    {
	    byte[] content = pipe.getRevisedContent();
	    if(content == null)
	    {
	    	InputStream stream = pipe.getRevisedContentStream();
	    	if(stream == null)
	    	{
	    		logger.debug("pipe with null content and null stream: " + pipe.getFileName());
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
		ParameterParser params = data.getParameters ();

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		List items = (List) state.getAttribute(STATE_MOVED_IDS);

		String collectionId = params.getString ("collectionId");

		Iterator itemIter = items.iterator();
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
					ContentHostingService.moveIntoFolder(itemId, collectionId);
				}	// if-else
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("notpermis8") + " " + originalDisplayName + ". ");
			}
			catch (IdUnusedException e)
			{
				addAlert(state,rb.getString("notexist1"));
			}
			catch (InUseException e)
			{
				addAlert(state, rb.getString("someone") + " " + originalDisplayName);
			}
			catch (TypeException e)
			{
				addAlert(state, rb.getString("pasteitem") + " " + originalDisplayName + " " + rb.getString("mismatch"));
			}
			catch (InconsistentException e)
			{
				addAlert(state, rb.getString("recursive") + " " + itemId);
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
				addAlert(state, trb.getFormattedMessage("alert.overquota", new String[]{ itemId }) );
			}
			catch(RuntimeException e)
			{
				logger.debug("ResourcesAction.doMoveitems ***** Unknown Exception ***** " + e.getMessage());
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
				SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
				if(! expandedCollections.contains(collectionId))
				{
					expandedCollections.add(collectionId);
				}

				state.setAttribute (STATE_MOVE_FLAG, Boolean.FALSE.toString());
			}

		}

	}	// doMoveitems
	
	/**
	* Paste the previously copied item(s)
	*/
	public static void doPasteitem ( RunData data)
	{
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
		ParameterParser params = data.getParameters ();

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		List items = (List) state.getAttribute(STATE_COPIED_IDS);

		String collectionId = params.getString ("collectionId");

		Iterator itemIter = items.iterator();
		while (itemIter.hasNext())
		{
			// get the copied item to be pasted
			String itemId = (String) itemIter.next();

			String originalDisplayName = NULL_STRING;

			try
			{
				String id = ContentHostingService.copyIntoFolder(itemId, collectionId);
				String mode = (String) state.getAttribute(STATE_MODE);
				if(MODE_HELPER.equals(mode))
				{
				}
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("notpermis8") + " " + originalDisplayName + ". ");
			}
			catch (IdUnusedException e)
			{
				addAlert(state,rb.getString("notexist1"));
			}
			catch (InUseException e)
			{
				addAlert(state, rb.getString("someone") + " " + originalDisplayName);
			}
			catch (TypeException e)
			{
				addAlert(state, rb.getString("pasteitem") + " " + originalDisplayName + " " + rb.getString("mismatch"));
			}
			catch(IdUsedException e)
			{
				addAlert(state, rb.getString("toomany"));
			}
			catch(IdLengthException e)
			{
				addAlert(state, trb.getFormattedMessage("alert.toolong", new String[]{e.getMessage()}));
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
				addAlert(state, rb.getString("recursive") + " " + itemId);
			}
			catch (OverQuotaException e)
			{
				addAlert(state, rb.getString("overquota"));
			}	// try-catch
			catch(RuntimeException e)
			{
				logger.debug("ResourcesAction.doPasteitems ***** Unknown Exception ***** " + e.getMessage());
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
				SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
				if(expandedCollections == null)
				{
					expandedCollections = new TreeSet();
					state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
				}
				if(! expandedCollections.contains(collectionId))
				{
					expandedCollections.add(collectionId);
				}

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
		String originalDisplayName = NULL_STRING;

		String newId = null;
		String displayName = "";
		try
		{
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
			{
				// paste the resource
				ContentResource resource = ContentHostingService.getResource (itemId);
				ResourceProperties p = ContentHostingService.getProperties(itemId);
				String[] args = { p.getProperty(ResourceProperties.PROP_DISPLAY_NAME) };
				displayName = rb.getFormattedMessage("copy.name", args);

				String newItemId = ContentHostingService.copyIntoFolder(itemId, collectionId);

				ContentResourceEdit copy = ContentHostingService.editResource(newItemId);
				ResourcePropertiesEdit pedit = copy.getPropertiesEdit();
				pedit.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
				try
				{
					ContentHostingService.commitResource(copy, NotificationService.NOTI_NONE);
					newId = copy.getId();
				}
				catch(OverQuotaException e)
				{
					addAlert(trb.getFormattedMessage("alert.overquota", new String[]{displayName}));
					logger.debug("OverQuotaException " + e);
					try
					{
						ContentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						logger.debug("Unable to remove partially completed resource: " + resource.getId(), e); 
					}
				}
				catch(ServerOverloadException e)
				{
					addAlert(trb.getFormattedMessage("alert.unable1", new String[]{displayName}));
					logger.debug("ServerOverloadException " + e);
					try
					{
						ContentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						logger.debug("Unable to remove partially completed resource: " + resource.getId(), e); 
					}
				}
			}	// if-else
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("notpermis8") + " " + originalDisplayName + ". ");
		}
		catch (IdUnusedException e)
		{
			addAlert(state,rb.getString("notexist1"));
		}
		catch (IdUsedException e)
		{
			addAlert(state, rb.getString("notaddreso") + " " + originalDisplayName + " " + rb.getString("used2"));
		}
		catch(IdLengthException e)
		{
			addAlert(state, trb.getFormattedMessage("alert.toolong", new String[]{e.getMessage()}));
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
			addAlert(state, rb.getString("someone") + " " + originalDisplayName + ". ");
		}
		catch(OverQuotaException e)
		{
			addAlert(state, rb.getString("overquota"));
		}
		catch(ServerOverloadException e)
		{
			// this represents temporary unavailability of server's filesystem
			// for server configured to save resource body in filesystem
			addAlert(state, rb.getString("failed"));
		}
		catch (TypeException e)
		{
			addAlert(state, rb.getString("pasteitem") + " " + originalDisplayName + " " + rb.getString("mismatch"));
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
			SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			if(STATE_EXPANDED_COLLECTIONS == null)
			{
				expandedCollections = new TreeSet();
				state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
			}
			if(! expandedCollections.contains(collectionId))
			{
				expandedCollections.add(collectionId);
			}

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
	    Reference ref = EntityManager.newReference(selectedItem.getReference());
	    List<ResourceToolAction> actions = new ArrayList<ResourceToolAction>();
	    
	    ResourceType typeDef = getResourceType(selectedItem, registry);
	    
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
		    if(contentModifyActions != null)
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
	    	if(! action.available((ContentEntity)ref.getEntity()))
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
	    List<ResourceToolAction> actions = new ArrayList<ResourceToolAction>();
	    
	    // if nothing to paste, just return an empty list
    	if((items_to_be_moved == null || items_to_be_moved.isEmpty()) && (items_to_be_copied == null || items_to_be_copied.isEmpty()))
    	{
    		return actions;
    	}
    	
	    Reference ref = EntityManager.newReference(selectedItem.getReference());
	    	    
	    Set<String> memberIds = new TreeSet<String>();
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
	    	ContentEntity entity = (ContentEntity) ref.getEntity();
			if(! action.available(entity))
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
	    Reference ref = EntityManager.newReference(selectedItem.getReference());
	    
	    List<ResourceToolAction> actions = new ArrayList<ResourceToolAction>();
	    
	    ResourceType typeDef = getResourceType(selectedItem, registry);
	    
	    if(permissions.contains(ContentPermissions.CREATE))
	    {		    
		    // certain actions are defined elsewhere but pertain only to ExpandableResourceTypes (collections)
		    if(typeDef.isExpandable())
		    {
		    	ExpandableResourceType expTypeDef = (ExpandableResourceType) typeDef;
		    	
		    	// if item is collection and user has content.new for item, user may be able to create new items in the collection 
		    	{
		    		// iterate over resource-types and get all the registered types and find actions requiring "content.new" permission
		    		Collection types = registry.getTypes(ref.getContext());
		    		Iterator<ActionType> actionTypeIt = CONTENT_NEW_ACTIONS.iterator();
		    		while(actionTypeIt.hasNext())
		    		{
		    			ActionType actionType = actionTypeIt.next();
		    			Iterator typeIt = types.iterator();
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
	 * @param homeCollectionId
	 * @param currentCollectionId
	 * @return
	 */
	public static List getCollectionPath(SessionState state)
	{
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		// make sure the channedId is set
		String currentCollectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);

		LinkedList collectionPath = new LinkedList();

		String previousCollectionId = "";
		List pathitems = new ArrayList();
		while ((currentCollectionId != null) && (!currentCollectionId.equals(navRoot)) && (!currentCollectionId.equals(previousCollectionId)) 
				&& !(contentService.ROOT_COLLECTIONS.contains(currentCollectionId)) && (!contentService.isRootCollection(previousCollectionId)))
		{
			pathitems.add(currentCollectionId);
			previousCollectionId = currentCollectionId;
			currentCollectionId = contentService.getContainingCollectionId(currentCollectionId);
		}
		
		if(navRoot != null && (pathitems.isEmpty() || (! navRoot.equals(previousCollectionId) && ! navRoot.equals(currentCollectionId))))
		{
			pathitems.add(navRoot);

		}
		if(homeCollectionId != null && (pathitems.isEmpty() || (!homeCollectionId.equals(navRoot) && ! homeCollectionId.equals(previousCollectionId) && ! homeCollectionId.equals(currentCollectionId))))
		{
			pathitems.add(homeCollectionId);
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
				if(contentService.COLLECTION_DROPBOX.equals(containingCollectionId))
				{
					Reference ref = EntityManager.newReference(contentService.getReference(id));
					Site site = SiteService.getSite(ref.getContext());
					String[] args = {site.getTitle()};
					name = trb.getFormattedMessage("title.dropbox", args);
				}
				else if(contentService.COLLECTION_SITE.equals(containingCollectionId))
				{
					Reference ref = EntityManager.newReference(contentService.getReference(id));
					Site site = SiteService.getSite(ref.getContext());
					String[] args = {site.getTitle()};
					name = trb.getFormattedMessage("title.resources", args);
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
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);

		Map current_stack_frame = peekAtStack(state);

		ResourcesEditItem item = null;

		// populate an ResourcesEditItem object with values from the resource and return the ResourcesEditItem
		try
		{
			ResourceProperties properties = ContentHostingService.getProperties(id);

			boolean isCollection = false;
			try
			{
				isCollection = properties.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);
			}
			catch(Exception e)
			{
				// assume isCollection is false if property is not set
			}

			ContentEntity entity = null;
			String itemType = "";
			byte[] content = null;
			if(isCollection)
			{
				itemType = "folder";
				entity = ContentHostingService.getCollection(id);
			}
			else
			{
				entity = ContentHostingService.getResource(id);
				itemType = ((ContentResource) entity).getContentType();
				content = ((ContentResource) entity).getContent();
			}

			String itemName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

			item = new ResourcesEditItem(id, itemName, itemType);
			
			item.setInDropbox(ContentHostingService.isInDropbox(id));
			boolean isUserSite = false;
			String refstr = entity.getReference();
			Reference ref = EntityManager.newReference(refstr);
			String contextId = ref.getContext();
			if(contextId != null)
			{
				isUserSite = SiteService.isUserSite(contextId);
			}
			item.setInWorkspace(isUserSite);
			
			BasicRightsAssignment rightsObj = new BasicRightsAssignment(item.getItemNum(), properties);
			item.setRights(rightsObj);

			String encoding = data.getRequest().getCharacterEncoding();
			if(encoding != null)
			{
				item.setEncoding(encoding);
			}

			String defaultCopyrightStatus = (String) state.getAttribute(STATE_DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(STATE_DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}
			item.setCopyrightStatus(defaultCopyrightStatus);

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

			String containerId = ContentHostingService.getContainingCollectionId (id);
			item.setContainer(containerId);

			boolean canRead = ContentHostingService.allowGetCollection(id);
			boolean canAddFolder = ContentHostingService.allowAddCollection(id);
			boolean canAddItem = ContentHostingService.allowAddResource(id);
			boolean canDelete = ContentHostingService.allowRemoveResource(id);
			boolean canRevise = ContentHostingService.allowUpdateResource(id);
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
			
			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			Collection site_groups = site.getGroups();
			item.setAllSiteGroups(site_groups);
			
			List access_groups = new ArrayList(((GroupAwareEntity) entity).getGroups());
			item.setEntityGroupRefs(access_groups);
//			if(access_groups != null)
//			{
//				
//				Iterator it = access_groups.iterator();
//				while(it.hasNext())
//				{
//					String groupRef = (String) it.next();
//					Group group = site.getGroup(groupRef);
//					item.addGroup(group.getId());
//				}
//			}

			List inherited_access_groups = new ArrayList(((GroupAwareEntity) entity).getInheritedGroups());
			item.setInheritedGroupRefs(inherited_access_groups);
//			if(inherited_access_groups != null)
//			{
//				Iterator it = inherited_access_groups.iterator();
//				while(it.hasNext())
//				{
//					String groupRef = (String) it.next();
//					Group group = site.getGroup(groupRef);
//					item.addInheritedGroup(group.getId());
//				}
//			}
			
			Collection allowedRemoveGroups = null;
			if(AccessMode.GROUPED == access)
			{
				allowedRemoveGroups = ContentHostingService.getGroupsWithRemovePermission(id);
				Collection more = ContentHostingService.getGroupsWithRemovePermission(collectionId);
				if(more != null && ! more.isEmpty())
				{
					allowedRemoveGroups.addAll(more);
				}
			}
			else if(AccessMode.GROUPED == inherited_access)
			{
				allowedRemoveGroups = ContentHostingService.getGroupsWithRemovePermission(collectionId);
			}
			else
			{
				allowedRemoveGroups = ContentHostingService.getGroupsWithRemovePermission(ContentHostingService.getSiteCollection(site.getId()));
			}
			item.setAllowedRemoveGroupRefs(allowedRemoveGroups);
			
			Collection allowedAddGroups = null;
			if(AccessMode.GROUPED == access)
			{
				allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(id);
				Collection more = ContentHostingService.getGroupsWithAddPermission(collectionId);
				if(more != null && ! more.isEmpty())
				{
					allowedAddGroups.addAll(more);
				}
			}
			else if(AccessMode.GROUPED == inherited_access)
			{
				allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(collectionId);
			}
			else
			{
				allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(ContentHostingService.getSiteCollection(site.getId()));
			}
			item.setAllowedAddGroupRefs(allowedAddGroups);
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			if(preventPublicDisplay.booleanValue())
			{
				item.setPubviewPossible(false);
				item.setPubviewInherited(false);
				item.setPubview(false);
			}
			else
			{
				item.setPubviewPossible(true);
				// find out about pubview
				boolean pubviewset = ContentHostingService.isInheritingPubView(id);
				item.setPubviewInherited(pubviewset);
				boolean pubview = pubviewset;
				if (!pubviewset) 
				{
					pubview = ContentHostingService.isPubView(id);
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
					item.setReleaseDate(TimeService.newTime());
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
						defaultRetractDate = TimeService.newTime();
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

			String url = ContentHostingService.getUrl(id);
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
	                logger.warn("EntityPropertyNotDefinedException for size of " + item.getId());
                }
                catch (EntityPropertyTypeException e)
                {
	                // TODO Auto-generated catch block
	                logger.warn("EntityPropertyTypeException for size of " + item.getId());
                }
				size = getFileSizeString(size_long, rb);
			}
			item.setSize(size);

			String copyrightStatus = properties.getProperty(properties.getNamePropCopyrightChoice());
			if(copyrightStatus == null || copyrightStatus.trim().equals(""))
			{
				copyrightStatus = (String) state.getAttribute(STATE_DEFAULT_COPYRIGHT);

			}
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
			
//			Map metadata = new HashMap();
//			List groups = (List) state.getAttribute(ListItem.STATE_METADATA_GROUPS);
//			if(groups != null && ! groups.isEmpty())
//			{
//				Iterator it = groups.iterator();
//				while(it.hasNext())
//				{
//					MetadataGroup group = (MetadataGroup) it.next();
//					Iterator propIt = group.iterator();
//					while(propIt.hasNext())
//					{
//						ResourcesMetadata prop = (ResourcesMetadata) propIt.next();
//						String name = prop.getFullname();
//						String widget = prop.getWidget();
//						if(widget.equals(ResourcesMetadata.WIDGET_DATE) || widget.equals(ResourcesMetadata.WIDGET_DATETIME) || widget.equals(ResourcesMetadata.WIDGET_TIME))
//						{
//							Time time = TimeService.newTime();
//							try
//							{
//								time = properties.getTimeProperty(name);
//							}
//							catch(Exception ignore)
//							{
//								// use "now" as default in that case
//							}
//							metadata.put(name, time);
//						}
//						else
//						{
//							String value = properties.getPropertyFormatted(name);
//							metadata.put(name, value);
//						}
//					}
//				}
//				item.setMetadata(metadata);
//			}
//			else
//			{
//				item.setMetadata(new HashMap());
//			}
			// for collections only
			if(item.isFolder())
			{
				// setup for quota - ADMIN only, site-root collection only
				if (SecurityService.isSuperUser())
				{
					String siteCollectionId = ContentHostingService.getSiteCollection(contextId);
					if(siteCollectionId.equals(entity.getId()))
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
			addAlert(state, rb.getString("notpermis2") + " " + id + ". " );
		}
		catch(TypeException e)
		{
			addAlert(state," " + rb.getString("typeex") + " "  + id);
		}
		catch(ServerOverloadException e)
		{
			// this represents temporary unavailability of server's filesystem
			// for server configured to save resource body in filesystem
			addAlert(state, rb.getString("failed"));
		}
		catch(RuntimeException e)
		{
			logger.debug("ResourcesAction.getEditItem ***** Unknown Exception ***** " + e.getMessage());
			addAlert(state, rb.getString("failed"));
		}

		return item;

	}

	/**
	 * @param size_long
	 * @param rl 
	 * @return
	 */
	public static String getFileSizeString(long size_long, ResourceLoader rl) {
		String size;
		NumberFormat formatter = NumberFormat.getInstance(rl.getLocale());
		formatter.setMaximumFractionDigits(1);
		if(size_long > 700000000L)
		{
			String[] args = { formatter.format(1.0 * size_long / (1024L * 1024L * 1024L)) };
			size = rl.getFormattedMessage("size.gb", args);
		}
		else if(size_long > 700000L)
		{
			String[] args = { formatter.format(1.0 * size_long / (1024L * 1024L)) };
			size = rl.getFormattedMessage("size.mb", args);
		}
		else if(size_long > 700L)
		{
			String[] args = { formatter.format(1.0 * size_long / 1024L) };
			size = rl.getFormattedMessage("size.kb", args);
		}
		else 
		{
			String[] args = { formatter.format(size_long) };
			size = rl.getFormattedMessage("size.bytes", args);
		}
		return size;
	}

	/**
	 * Get the items in this folder that should be seen.
	 * @param collectionId - String version of
	 * @param expandedCollections - Hash of collection resources
	 * @param sortedBy  - pass through to ContentHostingComparator
	 * @param sortedAsc - pass through to ContentHostingComparator
	 * @param parent - The folder containing this item
	 * @param isLocal - true if navigation root and home collection id of site are the same, false otherwise
	 * @param state - The session state
	 * @return a List of ResourcesBrowseItem objects
	 */
	protected static List getListView(String collectionId, Set highlightedItems, ResourcesBrowseItem parent, boolean isLocal, SessionState state)
	{
		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

		boolean need_to_expand_all = Boolean.TRUE.toString().equals((String)state.getAttribute(STATE_NEED_TO_EXPAND_ALL));
		
		Comparator userSelectedSort = (Comparator) state.getAttribute(STATE_LIST_VIEW_SORT);
		
		Map expandedFolderSortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
		if(expandedFolderSortMap == null)
		{
			expandedFolderSortMap = new HashMap();
			state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, expandedFolderSortMap);
		}
		
		SortedSet<String> expandedCollections = (SortedSet<String>) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(expandedCollections == null)
		{
			expandedCollections = new TreeSet<String>();
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
		}
		String mode = (String) state.getAttribute (STATE_MODE);

		List newItems = new LinkedList();
		try
		{
			// get the collection
			// try using existing resource first
			ContentCollection collection = null;

			// get the collection
			collection = contentService.getCollection(collectionId);
			if(need_to_expand_all || expandedCollections.contains(collectionId))
			{
				Comparator comparator = null;
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
				expandedFolderSortMap.put(collectionId, comparator);
				expandedCollections.add(collectionId);
				// state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, expandedFolderSortMap);
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

			boolean canRead = false;
			boolean canDelete = false;
			boolean canRevise = false;
			boolean canAddFolder = false;
			boolean canAddItem = false;
			boolean canUpdate = false;
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
				canUpdate = AuthzGroupService.allowUpdate(collectionId);
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
			folder.setSortable(ContentHostingService.isSortByPriorityEnabled() && collection_size > 1 && collection_size < EXPANDABLE_FOLDER_SIZE_LIMIT);
			Integer expansionLimit = (Integer) state.getAttribute(STATE_EXPANDABLE_FOLDER_SIZE_LIMIT);
			if(expansionLimit == null)
			{
				expansionLimit = new Integer(EXPANDABLE_FOLDER_SIZE_LIMIT);
			}
			folder.setIsTooBig(collection_size > expansionLimit.intValue());
				
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
	 * @param inheritedPermissions TODO
	 * @param context
	 * @return
	 */
	protected static Collection<ContentPermissions> getPermissions(String id, Collection<ContentPermissions> inheritedPermissions)
	{
		Collection<ContentPermissions> permissions = new ArrayList<ContentPermissions>();
		if(ContentHostingService.isCollection(id))
		{
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.CREATE)) || ContentHostingService.allowAddCollection(id) && !ContentHostingService.isRootCollection(id))
			{
				permissions.add(ContentPermissions.CREATE);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.DELETE)) || ContentHostingService.allowRemoveCollection(id))
			{
				permissions.add(ContentPermissions.DELETE);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.READ)) || ContentHostingService.allowGetCollection(id))
			{
				permissions.add(ContentPermissions.READ);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.REVISE)) || ContentHostingService.allowUpdateCollection(id))
			{
				permissions.add(ContentPermissions.REVISE);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.SITE_UPDATE)) || SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
			{
				permissions.add(ContentPermissions.SITE_UPDATE);
			}
		}
		else
		{
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.CREATE)) || ContentHostingService.allowAddResource(id) && !ContentHostingService.isRootCollection(id))
			{
				permissions.add(ContentPermissions.CREATE);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.DELETE)) || ContentHostingService.allowRemoveResource(id))
			{
				permissions.add(ContentPermissions.DELETE);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.READ)) || ContentHostingService.allowGetResource(id))
			{
				permissions.add(ContentPermissions.READ);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.REVISE)) || ContentHostingService.allowUpdateResource(id))
			{
				permissions.add(ContentPermissions.REVISE);
			}
			if((inheritedPermissions != null && inheritedPermissions.contains(ContentPermissions.SITE_UPDATE)) || SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
			{
				permissions.add(ContentPermissions.SITE_UPDATE);
			}
		}
		
		return permissions;
	}



	protected static User getUserProperty(ResourceProperties props, String name)
	{
		String id = props.getProperty(name);
		if (id != null)
		{
			try
			{
				return UserDirectoryService.getUser(id);
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
		state.setAttribute (STATE_COPIED_IDS, new ArrayList ());

		state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());

	}	// initCopyContent

	/**
	* initialize the copy context
	*/
	private static void initMoveContext (SessionState state)
	{
		state.setAttribute (STATE_MOVED_IDS, new ArrayList ());

		state.setAttribute (STATE_MOVE_FLAG, Boolean.FALSE.toString());

	}	// initCopyContent

	/**
	 * Find the resource name of a given resource id or filepath.
	 * 
	 * @param id
	 *        The resource id.
	 * @return the resource name.
	 */
	protected static String isolateName(String id)
	{
		if (id == null) return null;
		if (id.length() == 0) return null;

		// take after the last resource path separator, not counting one at the very end if there
		boolean lastIsSeparator = id.charAt(id.length() - 1) == '/';
		return id.substring(id.lastIndexOf('/', id.length() - 2) + 1, (lastIsSeparator ? id.length() - 1 : id.length()));

	} // isolateName

/**
	 * Returns true if the suspended operations stack contains no elements.
	 * @param state The current session state, including the STATE_SUSPENDED_OPERATIONS_STACK attribute.
	 * @return true if the suspended operations stack contains no elements
	 */
	private static boolean isStackEmpty(SessionState state)
	{
		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);
		if(operations_stack == null)
		{
			operations_stack = new Stack();
			state.setAttribute(STATE_SUSPENDED_OPERATIONS_STACK, operations_stack);
		}
		return operations_stack.isEmpty();
	}


	protected static List newEditItems(String collectionId, String itemtype, String encoding, String defaultCopyrightStatus, boolean preventPublicDisplay, Time defaultRetractDate, int number)
	{
		List new_items = new ArrayList();
		
		ContentCollection collection = null;
		AccessMode inheritedAccess = AccessMode.INHERITED;
//		Collection inheritedGroups = new ArrayList();
		try
		{
			collection = ContentHostingService.getCollection(collectionId);
			
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
			logger.warn("ResourcesAction.newEditItems() PermissionException ", e);
		} 
		catch (IdUnusedException e) 
		{
			// TODO Auto-generated catch block
			logger.warn("ResourcesAction.newEditItems() IdUnusedException ", e);
		} 
		catch (TypeException e) 
		{
			// TODO Auto-generated catch block
			logger.warn("ResourcesAction.newEditItems() TypeException ", e);
		}
		
		boolean isUserSite = false;
		String refstr = collection.getReference();
		Reference ref = EntityManager.newReference(refstr);
		String contextId = ref.getContext();
		if(contextId != null)
		{
			isUserSite = SiteService.isUserSite(contextId);
		}

		boolean pubviewset = ContentHostingService.isInheritingPubView(collectionId) || ContentHostingService.isPubView(collectionId);
		
		
		//Collection possibleGroups = ContentHostingService.getGroupsWithReadAccess(collectionId);
		boolean isInDropbox = ContentHostingService.isInDropbox(collectionId);
		
		Collection possibleGroups = ContentHostingService.getGroupsWithAddPermission(collectionId);
		Site site = null;
		Collection site_groups = null;
		
		try 
		{
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
		} 
		catch (IdUnusedException e) 
		{
			logger.warn("resourcesAction.newEditItems() IdUnusedException ", e);
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

		Collection allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(collectionId); // null;
//		if(AccessMode.GROUPED == inheritedAccess)
//		{
//			allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(collectionId);
//		}
//		else
//		{
//			allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(ContentHostingService.getSiteCollection(site.getId()));
//		}
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
			item.setReleaseDate(TimeService.newTime());
			item.setUseRetractDate(false);
			item.setRetractDate(defaultRetractDate);
			item.setInWorkspace(isUserSite);

			item.setCopyrightStatus(defaultCopyrightStatus);
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
	 * Remove a resource pattern from the observer
	 *@param pattern The pattern value to be removed
	 *@param state The state object
	 */
	private static void removeObservingPattern(String pattern, SessionState state)
	{
//		// get the observer and remove the pattern
//		ContentObservingCourier o = (ContentObservingCourier) state.getAttribute(STATE_OBSERVER);
//		o.removeResourcePattern(ContentHostingService.getReference(pattern));
//
//		// add it back to state
//		state.setAttribute(STATE_OBSERVER, o);

	}	// removeObservingPattern

	/**
	 *
	 * Whether a resource item can be replaced
	 * @param p The ResourceProperties object for the resource item
	 * @return true If it can be replaced; false otherwise
	 */
	private static boolean replaceable(ResourceProperties p)
	{
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
		if (displayName.indexOf(rb.getString("shortcut")) != -1)
		{
			rv = false;
		}

		return rv;

	}	// replaceable

	/**
	 * @param pipe
	 * @param action
	 */
	public static void reviseContent(ResourceToolActionPipe pipe)
	{
		ResourceToolAction action = pipe.getAction();
		ContentEntity entity = pipe.getContentEntity();
		try
		{
			ContentResourceEdit edit = ContentHostingService.editResource(entity.getId());
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			// update content
			extractContent(pipe, edit);
//			byte[] content = pipe.getRevisedContent();
//			if(content == null)
//			{
//				InputStream stream = pipe.getRevisedContentStream();
//				if(stream == null)
//				{
//					logger.debug("pipe with null content and null stream: " + pipe.getFileName());
//				}
//				else
//				{
//					edit.setContent(stream);
//				}
//			}
//			else
//			{
//				edit.setContent(content);
//			}
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
			ContentHostingService.commitResource(edit, pipe.getNotification());
		}
		catch (PermissionException e)
		{
			addAlert(trb.getString("alert.noperm"));
			// TODO Auto-generated catch block
			logger.warn("PermissionException " + e);
		}
		catch (IdUnusedException e)
		{
			// TODO Auto-generated catch block
			logger.warn("IdUnusedException ", e);
		}
		catch (TypeException e)
		{
			// TODO Auto-generated catch block
			logger.warn("TypeException ", e);
		}
		catch (InUseException e)
		{
			// TODO Auto-generated catch block
			logger.warn("InUseException ", e);
		}
		catch (OverQuotaException e)
		{
			addAlert(trb.getString("alert.quota"));
			logger.warn("OverQuotaException " + e);
		}
		catch (ServerOverloadException e)
		{
			addAlert(rb.getString("failed"));
			logger.warn("ServerOverloadException ", e);
		}
	}

	/**
	 * @param pedit
	 * @param metadataGroups
	 * @param metadata
	 */
	private static void saveMetadata(ResourcePropertiesEdit pedit, List metadataGroups, ResourcesEditItem item)
	{
		if(metadataGroups != null && !metadataGroups.isEmpty())
		{
			MetadataGroup group = null;
			Iterator it = metadataGroups.iterator();
			while(it.hasNext())
			{
				group = (MetadataGroup) it.next();
				Iterator props = group.iterator();
				while(props.hasNext())
				{
					ResourcesMetadata prop = (ResourcesMetadata) props.next();

					if(ResourcesMetadata.WIDGET_DATETIME.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_DATE.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_TIME.equals(prop.getWidget()))
					{
						Time val = (Time)item.getMetadata().get(prop.getFullname());
						if(val != null)
						{
							pedit.addProperty(prop.getFullname(), val.toString());
						}
					}
					else
					{
						String val = (String) item.getMetadata().get(prop.getFullname());
						pedit.addProperty(prop.getFullname(), val);
					}
				}
			}
		}

	}
   
	/**
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 */
	protected static String validateURL(String url) throws MalformedURLException
	{
		if (url.equals (NULL_STRING))
		{
			// ignore the empty url field
		}
		else if (url.indexOf ("://") == -1)
		{
			// if it's missing the transport, add http://
			url = "http://" + url;
		}

		if(!url.equals(NULL_STRING))
		{
			// valid protocol?
			try
			{
				// test to see if the input validates as a URL.
				// Checks string for format only.
				URL u = new URL(url);
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
						URL test = new URL("http://" + matcher.group(2));
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
		context.put("tlang",trb);
		
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
		List<List<ListItem>> folders = new ArrayList<List<ListItem>>();
		ContentCollection collection = null;
		ContentEntity selectedItem = null;
		
		// need a list of roots (ListItem objects) in context as $roots
		List<ListItem> roots = new ArrayList<ListItem>();
		Map othersites = ContentHostingService.getCollectionMap();
		Iterator it = othersites.keySet().iterator();
		while(it.hasNext())
		{
			String rootId = (String) it.next();
			String rootName = (String) othersites.get(rootId);
			ListItem root = new ListItem(rootId);
			root.setName(rootName);
			root.setHoverText(rootName);
			root.setAccessUrl(ContentHostingService.getUrl(rootId));
			root.setIconLocation(ContentTypeImageService.getContentTypeImage("folder"));
			
			if(selectedItemId != null && selectedItemId.startsWith(rootId))
			{
				root.setSelected(true);
				folderId = rootId;
				try
				{
					selectedItem = ContentHostingService.getCollection(rootId);
				}
				catch (IdUnusedException e)
				{
					// TODO Auto-generated catch block
					logger.warn("IdUnusedException ", e);
				}
				catch (TypeException e)
				{
					// TODO Auto-generated catch block
					logger.warn("TypeException ", e);
				}
				catch (PermissionException e)
				{
					// TODO Auto-generated catch block
					logger.warn("PermissionException ", e);
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

			List<ListItem> folder = new ArrayList<ListItem>();
			try 
			{
				if(collection == null)
				{
					collection = ContentHostingService.getCollection(collectionId);
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
				logger.warn("IdUnusedException " + e.getMessage());
			} 
			catch (TypeException e) 
			{
				// TODO Auto-generated catch block
				logger.warn("TypeException " + e.getMessage());
			} 
			catch (PermissionException e) 
			{
				// TODO Auto-generated catch block
				logger.warn("PermissionException " + e.getMessage());
			}
			
		}
		context.put("folders", folders);
		
		if(selectedItem != null)
		{
			// if copy or move is in progress AND user has content.new for this folder, user can paste in the collection 
			// (the paste action will only be defined for collections)
			List<String> items_to_be_copied = (List<String>) state.getAttribute(STATE_ITEMS_TO_BE_COPIED);
			
			List<String> items_to_be_moved = (List<String>) state.getAttribute(STATE_ITEMS_TO_BE_MOVED);
			
			List<ResourceToolAction> actions = getActions(selectedItem, new TreeSet(getPermissions(selectedItem.getId(), null)), registry);
			
			// TODO: need to deal with paste actions
			
			context.put("actions", actions);
			context.put("labeler", new ResourceTypeLabeler());
		}
		
		return "content/sakai_resources_columns";
	}


	public String buildCreateWizardContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) 
	{
		context.put("tlang",trb);
		
		context.put("DETAILS_FORM_NAME", "detailsForm");

		String template = "content/sakai_resources_cwiz_finish";
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe == null)
		{
			// go back to list view
		}
		else if(pipe.isActionCanceled())
		{
			// go back to list view
		}
		else if(pipe.isErrorEncountered())
		{
			// report the error?
			// go back to list view
		}
		else
		{
			// complete the create wizard
			String defaultCopyrightStatus = (String) state.getAttribute(STATE_DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(STATE_DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			String encoding = data.getRequest().getCharacterEncoding();

			Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
			if(defaultRetractDate == null)
			{
				defaultRetractDate = TimeService.newTime();
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
			//item.setPubviewPossible(! preventPublicDisplay);
			item.metadataGroupsIntoContext(context);
			
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
			context.put("instruction", trb.getFormattedMessage("instr.create", new String[]{typeDef.getLabel()}));
			context.put("required", trb.getFormattedMessage("instr.require", new String[]{"<span class=\"reqStarInline\">*</span>"}));
			
			if(ContentHostingService.isAvailabilityEnabled())
			{
				context.put("availability_is_enabled", Boolean.TRUE);
			}
			
			copyrightChoicesIntoContext(state, context);
			publicDisplayChoicesIntoContext(state, context);
			
			context.put("SITE_ACCESS", AccessMode.SITE.toString());
			context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
			context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());
			context.put("PUBLIC_ACCESS", PUBLIC_ACCESS);
		}
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
		context.put("tlang",rb);
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
			String[] args = { folder.getName() };
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
		}
		context.put("homeCollection", (String) state.getAttribute (STATE_HOME_COLLECTION_ID));
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));
		context.put ("resourceProperties", ContentHostingService.newResourceProperties ());

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
		context.put("tlang",trb);
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
			String[] args = { folder.getName() };
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
		context.put ("resourceProperties", ContentHostingService.newResourceProperties ());

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
		context.put("clang",rb);
		context.put("tlang",trb);
		
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
		org.sakaiproject.content.api.ContentHostingService contentService = ContentHostingService.getInstance();
		//context.put ("service", contentService);
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		boolean inMyWorkspace = SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
		context.put("inMyWorkspace", Boolean.toString(inMyWorkspace));

		boolean atHome = false;

		// %%STATE_MODE_RESOURCES%%

		boolean dropboxMode = RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES));
		if (dropboxMode)
		{
			// notshow the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
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

		String siteTitle = (String) state.getAttribute (STATE_SITE_TITLE);
		if (collectionId.equals(homeCollectionId))
		{
			atHome = true;
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
		
		if(!dropboxMode && atHome && SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
		{
			if(!inMyWorkspace )
			{
				context.put("showPermissions", Boolean.TRUE.toString());
				//buildListMenu(portlet, context, data, state);
			}
			
			String home = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
			Reference ref = EntityManager.newReference(ContentHostingService.getReference(home));
			String siteId = ref.getContext();
			Map<String,Boolean> statusMap = registry.getMapOfResourceTypesForContext(siteId);
			if(statusMap != null && ! statusMap.isEmpty())
			{
				context.put("showOptions", Boolean.TRUE.toString());
			}

		}
		
		context.put("atHome", Boolean.toString(atHome));

		if(ContentHostingService.isAvailabilityEnabled())
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
				logger.warn(this + "IdUnusedException: " + collectionId);
				try
				{
					ContentCollectionEdit coll = contentService.addCollection(collectionId);
					contentService.commitCollection(coll);
				}
				catch(IdUsedException inner)
				{
					// how can this happen??
					logger.warn(this + "IdUsedException: " + collectionId);
					throw ex;
				}
				catch(IdInvalidException inner)
				{
					logger.warn(this + "IdInvalidException: " + collectionId);
					// what now?
					throw ex;
				}
				catch(InconsistentException inner)
				{
					logger.warn(this + "InconsistentException: " + collectionId);
					// what now?
					throw ex;
				}
			}
			catch(TypeException ex)
			{
				logger.warn(this + "TypeException.");
				throw ex;				
			}
			catch(PermissionException ex)
			{
				logger.warn(this + "PermissionException.");
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
			SortedSet<String> expandedCollections = (SortedSet<String>) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			expandedCollections.add(collectionId);
			context.put("expandedCollections", expandedCollections);

			ContentCollection collection = ContentHostingService.getCollection(collectionId);
			
			ListItem item = ListItem.getListItem(collection, null, registry, need_to_expand_all, expandedCollections, items_to_be_moved, items_to_be_copied, 0, userSelectedSort, false, null);
			
			Map<String, ResourceToolAction> listActions = new HashMap<String, ResourceToolAction>();
			
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
			
			String containingCollectionId = contentService.getContainingCollectionId(item.getId());
			if(contentService.COLLECTION_DROPBOX.equals(containingCollectionId))
			{
				Reference ref = EntityManager.newReference(contentService.getReference(item.getId()));
				Site site = SiteService.getSite(ref.getContext());
				String[] args = {site.getTitle()};
				item.setName(trb.getFormattedMessage("title.dropbox", args));
				
				showHotDropboxWidget = true;
			}
			else if(contentService.COLLECTION_SITE.equals(containingCollectionId))
			{
				Reference ref = EntityManager.newReference(contentService.getReference(item.getId()));
				Site site = SiteService.getSite(ref.getContext());
				String[] args = {site.getTitle()};
				item.setName(trb.getFormattedMessage("title.resources", args));
			}
			
			
//			if(atHome && dropboxMode)
//			{
//				item.setName(siteTitle + " " + rb.getString("gen.drop"));
//			}
//			else if(atHome)
//			{
//				item.setName(siteTitle + " " + rb.getString("gen.reso"));
//			}

			context.put("site", items);

			boolean show_all_sites = false;
			
			String allowed_to_see_other_sites = (String) state.getAttribute(STATE_SHOW_ALL_SITES);
			String show_other_sites = (String) state.getAttribute(STATE_SHOW_OTHER_SITES);
			context.put("show_other_sites", show_other_sites);
			if(Boolean.TRUE.toString().equals(allowed_to_see_other_sites))
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
				List<ListItem> otherSites = new ArrayList<ListItem>();
				for(ListItem siteCollection : siteCollections)
				{
					otherSites.addAll(siteCollection.convert2list());
					
					// looking for expanded site-level dropboxes
					// first check whether it's a dropbox
					if(siteCollection.isDropbox())
					{
						// check whether it's a site-level dropbox
						if(contentService.COLLECTION_DROPBOX.equals(siteCollection.getEntity().getContainingCollection().getId()))
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
				
				if (state.getAttribute(STATE_NUM_MESSAGES) != null)
				{
					context.put("allMsgNumber", state.getAttribute(STATE_NUM_MESSAGES).toString());
					context.put("allMsgNumberInt", state.getAttribute(STATE_NUM_MESSAGES));
				}

				context.put("pagesize", ((Integer) state.getAttribute(STATE_PAGESIZE)).toString());

				// find the position of the message that is the top first on the page
				if ((state.getAttribute(STATE_TOP_MESSAGE_INDEX) != null) && (state.getAttribute(STATE_PAGESIZE) != null))
				{
					int topMsgPos = ((Integer)state.getAttribute(STATE_TOP_MESSAGE_INDEX)).intValue() + 1;
					context.put("topMsgPos", Integer.toString(topMsgPos));
					int btmMsgPos = topMsgPos + ((Integer)state.getAttribute(STATE_PAGESIZE)).intValue() - 1;
					if (state.getAttribute(STATE_NUM_MESSAGES) != null)
					{
						int allMsgNumber = ((Integer)state.getAttribute(STATE_NUM_MESSAGES)).intValue();
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
			logger.warn(this + "TypeException.");
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
			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			context.put("siteTitle", site.getTitle());
		}
		catch (IdUnusedException e)
		{
			logger.debug(this + e.toString());
		}

		context.put("expandallflag", state.getAttribute(STATE_EXPAND_ALL_FLAG));
		state.removeAttribute(STATE_NEED_TO_EXPAND_ALL);
		
		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(state);

		// pick the "show" template based on the standard template name
		// String template = (String) getContext(data).get("template");
		
		context.put("labeler", new ResourceTypeLabeler());
		
		return TEMPLATE_NEW_LIST;

	}	// buildListContext

	/**
	* Build the context for normal display
	*/
	public String buildMainPanelContext (	VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		// find the ContentTypeImage service
		
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
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe != null)
		{
			if(pipe.isActionCanceled())
			{
				state.setAttribute(STATE_MODE, MODE_LIST);
				toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			}
			else if(pipe.isErrorEncountered())
			{
				String msg = pipe.getErrorMessage();
				if(msg != null && ! msg.trim().equals(""))
				{
					addAlert(state, msg);
				}
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
		
		String template = null;
		
		// place if notification is enabled and current site is not of My Workspace type
		boolean isUserSite = SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
		context.put("notification", new Boolean(!isUserSite && notificationEnabled(state)));
		// get the mode
		String mode = (String) state.getAttribute (STATE_MODE);
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
		else if (mode.equals (MODE_OPTIONS))
		{
			template = buildOptionsPanelContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_REORDER))
		{
			template = buildReorderContext (portlet, context, data, state);
		}
		else if(mode.equals(MODE_DAV))
		{
			template = buildWebdavContext (portlet, context, data, state);
		}
		else if(mode.equals(MODE_REVISE_METADATA))
		{
			template = buildReviseMetadataContext(portlet, context, data, state);
		}
		else if(mode.equals(MODE_PROPERTIES))
		{
			template = buildMoreContext(portlet, context, data, state);
		}

		return template;

	}	// buildMainPanelContext

	/**
	*  Setup for customization
	**/
	public String buildOptionsPanelContext( VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		context.put("tlang",trb);
		String home = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		Reference ref = EntityManager.newReference(ContentHostingService.getReference(home));
		String siteId = ref.getContext();

		context.put("siteId", siteId);
		context.put("form-submit", BUTTON + "doUpdateOptions");
		context.put("form-cancel", BUTTON + "doCancelOptions");
		String[] args = { SiteService.getSiteDisplay(siteId) };
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
		Collections.sort(types, new Comparator(){

			public int compare(Object arg0, Object arg1) {
				ResourceType type0 = (ResourceType) arg0;
				
				ResourceType type1 = (ResourceType) arg1;
				return type0.getLabel().compareToIgnoreCase(type1.getLabel());
			}});
		
		context.put("types", types);

		return TEMPLATE_OPTIONS;

	}	// buildOptionsPanelContext

	/**
	 * Build the context to establish a custom-ordering of resources/folders within a folder.
	 */
	public String buildReorderContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) 
	{
		context.put("tlang",rb);
		
		String folderId = (String) state.getAttribute(STATE_REORDER_FOLDER);
		context.put("folderId", folderId);
		
		// save expanded folder lists
		SortedSet<String> expandedCollections = (SortedSet<String>) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		Map expandedFolderSortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
		String need_to_expand_all = (String) state.getAttribute(STATE_NEED_TO_EXPAND_ALL);

		// create temporary expanded folder lists for this invocation of getListView
		Map tempExpandedFolderSortMap = new HashMap();
		state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, tempExpandedFolderSortMap);
		SortedSet tempExpandedCollections = new TreeSet();
		tempExpandedCollections.add(folderId);
		state.setAttribute(STATE_EXPANDED_COLLECTIONS, tempExpandedCollections);

		Set highlightedItems = new TreeSet();
		List all_roots = new ArrayList();
		List this_site = new ArrayList();

		List members = getListView(folderId, highlightedItems, (ResourcesBrowseItem) null, true, state);

		// restore expanded folder lists 
		expandedCollections.addAll(tempExpandedCollections);
		state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
		expandedFolderSortMap.putAll(tempExpandedFolderSortMap);
		state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, expandedFolderSortMap);

		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

		boolean atHome = false;

		context.put("atHome", Boolean.toString(atHome));

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
			atHome = true;
			String siteTitle = (String) state.getAttribute (STATE_SITE_TITLE);
			rootTitle = siteTitle + " " + rb.getString("gen.reso");
		}
		else
		{
			// should be not PermissionException thrown at this time, when the user can successfully navigate to this collection
			try
			{
				rootTitle = ContentHostingService.getCollection(folderId).getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
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
		context.put("tlang", trb);
		
		context.put("DETAILS_FORM_NAME", "detailsForm");
		
		ResourceToolAction action = (ResourceToolAction) state.getAttribute(STATE_REVISE_PROPERTIES_ACTION);
		context.put("action", action);
		
		context.put("showItemSummary", Boolean.TRUE.toString());
		
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
		
		context.put("required", trb.getFormattedMessage("instr.require", new String[]{"<span class=\"reqStarInline\">*</span>"}));
		
		ListItem item = (ListItem) state.getAttribute(STATE_REVISE_PROPERTIES_ITEM);
		if(item == null)
		{
			item = getListItem(state);
			state.setAttribute(STATE_REVISE_PROPERTIES_ITEM, item);
		}
		item.metadataGroupsIntoContext(context);
		context.put("item", item);
		
		String chhbeanname = item.entity.getProperties().getProperty(
				ContentHostingHandlerResolver.CHH_BEAN_NAME);
		if (chhbeanname == null || chhbeanname.equals("")) chhbeanname = "";
		context.put("CHHmountpoint", chhbeanname);
		

		
		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}
		
		context.put("SITE_ACCESS", AccessMode.SITE.toString());
		context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
		context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());
		context.put("PUBLIC_ACCESS", PUBLIC_ACCESS);

		if(ContentHostingService.isContentHostingHandlersEnabled())
		{
			context.put("showMountPointProperty", Boolean.TRUE.toString());
		}

		return TEMPLATE_REVISE_METADATA;
	}

	/**
     * @param state
     * @return
     */
    protected ListItem getListItem(SessionState state)
    {
	    // complete the create wizard
		String defaultCopyrightStatus = (String) state.getAttribute(STATE_DEFAULT_COPYRIGHT);
		if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
		{
			defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
			state.setAttribute(STATE_DEFAULT_COPYRIGHT, defaultCopyrightStatus);
		}

		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
		if(defaultRetractDate == null)
		{
			defaultRetractDate = TimeService.newTime();
			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		String entityId = (String) state.getAttribute(STATE_REVISE_PROPERTIES_ENTITY_ID);
		String refstr = ContentHostingService.getReference(entityId);
		Reference ref = EntityManager.newReference(refstr);
		ContentEntity entity = (ContentEntity) ref.getEntity();

		ListItem item = new  ListItem(entity);
		if(item.getReleaseDate() == null)
		{
			item.setReleaseDate(TimeService.newTime());
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
		context.put("tlang",rb);
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		boolean inMyWorkspace = SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
		context.put("inMyWorkspace", Boolean.toString(inMyWorkspace));
		String homeCollectionId = (String) state.getAttribute (STATE_HOME_COLLECTION_ID);

		boolean dropboxMode = RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES));
		context.put("dropboxMode", Boolean.toString(dropboxMode));
		boolean maintainer = false;
		if(dropboxMode)
		{
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
		context.put("site_id", ToolManager.getCurrentPlacement().getContext());
		context.put("site_title", state.getAttribute(STATE_SITE_TITLE));
		context.put("user_id", UserDirectoryService.getCurrentUser().getEid());
		
		if (ContentHostingService.isShortRefs())
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
		context.put("webdav_instructions" ,webdav_instructions);

		// TODO: Consider whether we should return a trivial session
		// or a null in the case of anonymous users.
		UsageSession session = UsageSessionService.getSession();
		if ( session != null )
		{
			String browserID = UsageSessionService.getSession().getBrowserId();
			if(browserID.equals(UsageSession.WIN_IE))
			{
				context.put("isWinIEBrowser", Boolean.TRUE.toString());
			}
		}

		return TEMPLATE_DAV;

	}	// buildWebdavContext


	/**
	 * Iterate over attributes in ToolSession and remove all attributes starting with a particular prefix.
	 * @param toolSession
	 * @param prefix
	 */
	protected void cleanup(ToolSession toolSession, String prefix) 
	{
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
	 * @param deleteIdSet
	 */
	protected void deleteItem(SessionState state, String itemId)
	{
		List deleteItems = new ArrayList();
		List notDeleteItems = new ArrayList();
		List nonEmptyFolders = new ArrayList();
		
		boolean isFolder = itemId.endsWith(Entity.SEPARATOR);
		
		try
		{
			ContentEntity entity = null;
			if(isFolder)
			{
				entity = ContentHostingService.getCollection(itemId);
			}
			else
			{
				entity = ContentHostingService.getResource(itemId);
			}
			
			ListItem member = new ListItem(entity);
			
			if(isFolder)
			{
				ContentCollection collection = (ContentCollection) entity;
				if(ContentHostingService.allowRemoveCollection(itemId))
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
			else if(ContentHostingService.allowRemoveResource(member.getId()))
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
			logger.warn("IdUnusedException ", e);
		}
		catch (TypeException e)
		{
			// TODO Auto-generated catch block
			logger.warn("TypeException ", e);
		}
		catch (PermissionException e)
		{
			// TODO Auto-generated catch block
			logger.warn("PermissionException ", e);
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
			addAlert(state, rb.getString("notpermis14") + notDeleteNames);
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
	 * @param deleteIds
	 */
	protected void deleteItems(SessionState state, Set deleteIdSet)
	{
		List deleteItems = new ArrayList();
		List notDeleteItems = new ArrayList();
		List nonEmptyFolders = new ArrayList();
		
		org.sakaiproject.content.api.ContentHostingService contentService = ContentHostingService.getInstance();
		
		for(String deleteId : (Set<String>) deleteIdSet)
		{
			ContentEntity entity = null;
			try
			{
				if(contentService.isCollection(deleteId))
				{
					entity = contentService.getCollection(deleteId);
				}
				else if(contentService.allowRemoveResource(deleteId))
				{
					entity = contentService.getResource(deleteId);
				}
				else
				{
					
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
				logger.warn("PermissionException", e);
			} 
			catch (IdUnusedException e) 
			{
				logger.warn("IdUnusedException", e);
			} 
			catch (TypeException e) 
			{
				logger.warn("TypeException", e);
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
			addAlert(state, rb.getString("notpermis14") + notDeleteNames);
		}

		state.setAttribute (STATE_DELETE_SET, deleteItems);
		state.setAttribute (STATE_NON_EMPTY_DELETE_SET, nonEmptyFolders);
	}


	/**
	* doCancel to return to the previous state
	*/
	public void doCancel ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());
		
		state.setAttribute(STATE_MODE, MODE_LIST);

//		if(!isStackEmpty(state))
//		{
//			Map current_stack_frame = peekAtStack(state);
//			current_stack_frame.put(STATE_HELPER_CANCELED_BY_USER, Boolean.TRUE.toString());
//
//			popFromStack(state);
//		}
//
//		resetCurrentMode(state);

	}	// doCancel

	/**
	* Remove the collection id from the expanded collection list
	*/
	public void doCollapse_collection(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		SortedSet expandedItems = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(expandedItems == null)
		{
			expandedItems = new TreeSet();
		}
		Map folderSortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
		if(folderSortMap == null)
		{
			folderSortMap = new HashMap();
			state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, folderSortMap);
		}

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
		Iterator l = expandedItems.iterator();
		while (l.hasNext ())
		{
			// remove the collection id and all of the subcollections
//		    Resource collection = (Resource) l.next();
//			String id = (String) collection.getId();
		    String id = (String) l.next();

			if (id.startsWith(collectionId))
			{
				String refstr = ContentHostingService.getReference(id);
				if(refstr != null)
				{
					Reference reference = EntityManager.newReference(refstr);
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
								ServiceLevelAction collapseAction = ((ExpandableResourceType) typeDef).getCollapseAction();
								if(collapseAction != null && collapseAction.available(entity))
								{
									collapseAction.initializeAction(reference);
									
									collapseAction.finalizeAction(reference);
									
									folderSortMap.remove(id);

									// add this folder id into the set to be event-observed
									addObservingPattern(id, state);
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

		state.setAttribute(STATE_EXPANDED_COLLECTIONS, newSet);

		// remove this folder id into the set to be event-observed
		removeObservingPattern(collectionId, state);

	}	// doCollapse_collection


	public void doColumns(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.setAttribute(STATE_LIST_PREFERENCE, LIST_COLUMNS);
	}

	/**
	 * @param data
	 */
	public void doCompleteCreateWizard(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		ListItem item = (ListItem) state.getAttribute(STATE_CREATE_WIZARD_ITEM);
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		String user_action = params.getString("user_action");
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		if(user_action == null)
		{
			
		}
		else if(user_action.equals("save"))
		{
			item.captureProperties(params, ListItem.DOT + "0");
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
			
			String collectionId = (String) state.getAttribute(STATE_CREATE_WIZARD_COLLECTION_ID);
			try 
			{
				// title
				String basename = name;
				String extension = "";
				if(name.contains("."))
				{
					String[] parts = name.split("\\.");
					basename = parts[0];
					if(parts.length > 1)
					{
						extension = parts[parts.length - 1];
					}
					
					for(int i = 1; i < parts.length - 1; i++)
					{
						basename += "." + parts[i];
					}
				}
				
				// create resource
				ContentResourceEdit resource = ContentHostingService.addResource(collectionId, Validator.escapeResourceName(basename), Validator.escapeResourceName(extension), MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				
				String resourceType = null;
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
							iAction.finalizeAction(EntityManager.newReference(resource.getReference()), pipe.getInitializationId());
						}
						resourceType = action.getTypeId();
					}
				}
				
				resource.setResourceType(resourceType);
				
				item.updateContentResourceEdit(resource);
				
				extractContent(pipe, resource);
//				byte[] content = pipe.getRevisedContent();
//				if(content == null)
//				{
//					InputStream stream = pipe.getRevisedContentStream();
//					if(stream == null)
//					{
//						logger.debug("pipe with null content and null stream: " + pipe.getFileName());
//					}
//					else
//					{
//						resource.setContent(stream);
//					}
//				}
//				else
//				{
//					resource.setContent(content);
//				}

				resource.setContentType(pipe.getRevisedMimeType());
				
				ResourcePropertiesEdit resourceProperties = resource.getPropertiesEdit();
				Map values = pipe.getRevisedResourceProperties(); 	 
				Iterator valueIt = values.keySet().iterator(); 	 
				while(valueIt.hasNext()) 	 
				{ 	 
					String pname = (String) valueIt.next(); 	 
					String pvalue = (String) values.get(pname); 	 
					resourceProperties.addProperty(pname, pvalue); 
				
				} 	 

				// notification
				int noti = NotificationService.NOTI_NONE;
				// %%STATE_MODE_RESOURCES%%
				if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
				{
	   				boolean notification = params.getBoolean("notify_dropbox");
	  				if(notification)
	   				{
	   					noti = NotificationService.NOTI_REQUIRED;
	   				}
	   				else
	   				{
	   					// set noti to none if in dropbox mode
	   					noti = NotificationService.NOTI_NONE;
	   				}
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
				
				List<String> alerts = item.checkRequiredProperties();
				
				if(alerts.isEmpty())
				{
					try
					{
						ContentHostingService.commitResource(resource, noti);
						
						toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
		
						// show folder if in hierarchy view
						SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
						expandedCollections.add(collectionId);
		
						state.setAttribute(STATE_MODE, MODE_LIST);
					}
					catch(OverQuotaException e)
					{
						addAlert(trb.getFormattedMessage("alert.overquota", new String[]{resource.getId()}));
						logger.debug("OverQuotaException " + e);
						try
						{
							ContentHostingService.removeResource(resource.getId());
						}
						catch(Exception e1)
						{
							logger.debug("Unable to remove partially completed resource: " + resource.getId() + "\n" + e); 
						}
					}
					catch(ServerOverloadException e)
					{
						addAlert(trb.getFormattedMessage("alert.unable1", new String[]{resource.getId()}));
						logger.debug("ServerOverloadException " + e);
						try
						{
							ContentHostingService.removeResource(resource.getId());
						}
						catch(Exception e1)
						{
							logger.debug("Unable to remove partially completed resource: " + resource.getId() + "\n" + e); 
						}
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
			catch (IdUnusedException e) 
			{
				logger.warn("IdUnusedException", e);
			} 
			catch (PermissionException e) 
			{
				logger.warn("PermissionException", e);
			} 
			catch (IdInvalidException e) 
			{
				logger.warn("IdInvalidException", e);
			} 
			catch (ServerOverloadException e) 
			{
				addAlert(trb.getFormattedMessage("alert.unable1", new String[]{name}));
				logger.warn("ServerOverloadException" + e);
			}
			catch (OverQuotaException e)
			{
				addAlert(trb.getFormattedMessage("alert.overquota", new Object[]{name}));
				logger.warn("OverQuotaException " + e);
			}
            catch (IdUniquenessException e)
            {
	            addAlert(state, trb.getFormattedMessage("paste.error", new Object[]{name}));
            }
            catch (IdLengthException e)
            {
				addAlert(state, trb.getFormattedMessage("alert.toolong", new String[]{e.getMessage()}));
	            // TODO Auto-generated catch block
	            logger.warn("IdLengthException " + e);
            }
			
		}
		else if(user_action.equals("cancel"))
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
			String copyId = NULL_STRING;
			for (int i = 0; i < copyItems.length; i++)
			{
				copyId = copyItems[i];
				try
				{
					ResourceProperties properties = ContentHostingService.getProperties (copyId);
					/*
					if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
					{
						String alert = (String) state.getAttribute(STATE_MESSAGE);
						if (alert == null || ((alert != null) && (alert.indexOf(rb.getString("notsupported")) == -1)))
						{
							addAlert(state, rb.getString("notsupported"));
						}
					}
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
				ContentHostingService.eliminateDuplicates(copyItemsVector);
				state.setAttribute (STATE_COPIED_IDS, copyItemsVector);

			}	// if-else
		}	// if-else

	}	// doCopy

	/**
	* set the state name to be "deletecofirm" if any item has been selected for deleting
	*/
	public void doDeleteconfirm ( RunData data)
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
			state.setAttribute(STATE_LIST_SELECTIONS, deleteIdSet);
		}


	}	// doDeleteconfirm

	public void doDispatchAction(RunData data)
	{
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
		
		Reference reference = EntityManager.newReference(ContentHostingService.getReference(selectedItemId));
		
		ResourceToolAction action = type.getAction(actionId);
		if(action == null)
		{
			
		}
		else if(action instanceof InteractionAction)
		{
			ToolSession toolSession = SessionManager.getCurrentToolSession();
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
		}
		else if(action instanceof ServiceLevelAction)
		{
			ServiceLevelAction sAction = (ServiceLevelAction) action;
			switch(sAction.getActionType())
			{
				case COPY:
					List<String> items_to_be_copied = new ArrayList<String>();
					if(selectedItemId != null)
					{
						items_to_be_copied.add(selectedItemId);
					}
					state.removeAttribute(STATE_ITEMS_TO_BE_MOVED);
					state.setAttribute(STATE_ITEMS_TO_BE_COPIED, items_to_be_copied);
					break;
				case DUPLICATE:
					sAction.initializeAction(reference);
					String newId = duplicateItem(state, selectedItemId, ContentHostingService.getContainingCollectionId(selectedItemId));
					if(newId == null)
					{
						sAction.cancelAction(reference);
					}
					else
					{
						reference = EntityManager.newReference(ContentHostingService.getReference(newId));
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
					sAction.finalizeAction(reference);
					break;
				case MOVE:
					List<String> items_to_be_moved = new ArrayList<String>();
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
					// sAction.initializeAction(reference);
					state.setAttribute(STATE_REVISE_PROPERTIES_ENTITY_ID, selectedItemId);
					state.setAttribute(STATE_REVISE_PROPERTIES_ACTION, action);
					state.setAttribute (STATE_MODE, MODE_REVISE_METADATA);
					ListItem item = getListItem(state);
					state.setAttribute(STATE_REVISE_PROPERTIES_ITEM, item);
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
			logger.warn("doDispatchAction ", e);
		}
	}
	
	/**
	 * Change the number of days for which individual dropboxes are highlighted in the instructor's list view.
	 */
	public void doSetHotDropbox(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		int dropboxHighlight = params.getInt("dropboxHighlight", 1);
		
		state.setAttribute(STATE_DROPBOX_HIGHLIGHT, new Integer(dropboxHighlight));
	}

	/**
	* Add the collection id into the expanded collection list
	 * @throws PermissionException
	 * @throws TypeException
	 * @throws IdUnusedException
	*/
	public void doExpand_collection(RunData data) throws IdUnusedException, TypeException, PermissionException
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		SortedSet expandedItems = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(expandedItems == null)
		{
			expandedItems = new TreeSet();
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedItems);
		}

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
		
		String refstr = ContentHostingService.getReference(id);
		if(refstr != null)
		{
			Reference reference = EntityManager.newReference(refstr);
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

							// add this folder id into the set to be event-observed
							addObservingPattern(id, state);
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

		ParameterParser params = data.getParameters ();

		List items = (List) state.getAttribute(STATE_DELETE_SET);

		// List deleteIds = (List) state.getAttribute (STATE_DELETE_IDS);

		// delete the lowest item in the hireachy first
		Map deleteItems = new HashMap();
		// String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		int maxDepth = 0;
		int depth = 0;

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
			List v = (List) deleteItems.get(new Integer(depth));
			if(v == null)
			{
				v = new ArrayList();
			}
			v.add(item);
			deleteItems.put(new Integer(depth), v);
		}

		boolean isCollection = false;
		for (int j=maxDepth; j>0; j--)
		{
			List v = (List) deleteItems.get(new Integer(j));
			if (v==null)
			{
				v = new ArrayList();
			}
			Iterator itemIt = v.iterator();
			while(itemIt.hasNext())
			{
				ListItem item = (ListItem) itemIt.next();
				try
				{
					if (item.isCollection())
					{
						ContentHostingService.removeCollection(item.getId());
					}
					else
					{
						ContentHostingService.removeResource(item.getId());
					}
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis6") + " " + item.getName() + ". ");
				}
				catch (IdUnusedException e)
				{
					addAlert(state,rb.getString("notexist1"));
				}
				catch (TypeException e)
				{
					addAlert(state, rb.getString("deleteres") + " " + item.getName() + " " + rb.getString("wrongtype"));
				}
				catch (ServerOverloadException e)
				{
					addAlert(state, rb.getString("failed"));
				}
				catch (InUseException e)
				{
					addAlert(state, rb.getString("deleteres") + " " + item.getName() + " " + rb.getString("locked"));
				}// try - catch
				catch(RuntimeException e)
				{
					logger.debug("ResourcesAction.doDelete ***** Unknown Exception ***** " + e.getMessage());
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

//    /**
//	 * @param data
//	 */
//	public void doHide_metadata(RunData data)
//	{
//		ParameterParser params = data.getParameters ();
//		String name = params.getString("metadataGroup");
//
//		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
//		List metadataGroups = (List) state.getAttribute(ListItem.STATE_METADATA_GROUPS);
//		if(metadataGroups != null && ! metadataGroups.isEmpty())
//		{
//			boolean found = false;
//			MetadataGroup group = null;
//			Iterator it = metadataGroups.iterator();
//			while(!found && it.hasNext())
//			{
//				group = (MetadataGroup) it.next();
//				found = (name.equals(Validator.escapeUrl(group.getName())) || name.equals(group.getName()));
//			}
//			if(found)
//			{
//				group.setShowing(false);
//			}
//		}
//
//	}	// doHide_metadata
//
	/**
	 * @param data
	 */
	public void doHideOtherSites(RunData data)
	{
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
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.setAttribute(STATE_LIST_PREFERENCE, LIST_HIERARCHY);
	}

	//	private static void resetCurrentMode(SessionState state)
//	{
//		String mode = (String) state.getAttribute(STATE_MODE);
//		if(isStackEmpty(state))
//		{
//			if(MODE_HELPER.equals(mode))
//			{
//				cleanupState(state);
//				state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_DONE);
//			}
//			else
//			{
//				state.setAttribute(STATE_MODE, MODE_LIST);
//				state.removeAttribute(STATE_RESOURCES_HELPER_MODE);
//			}
//			return;
//		}
//		Map current_stack_frame = peekAtStack(state);
//		String helper_mode = (String) current_stack_frame.get(STATE_RESOURCES_HELPER_MODE);
//		if(helper_mode != null)
//		{
//			state.setAttribute(STATE_RESOURCES_HELPER_MODE, helper_mode);
//		}
//
//	}
//
	/**
	* Expand all the collection resources and put in EXPANDED_COLLECTIONS attribute.
	*/
	public void doList ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute (STATE_MODE, MODE_LIST);

	}	// doList

	/**
	* Handle user's selection of items to be moved.
	*/
	public void doMove ( RunData data )
	{
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
			String moveId = NULL_STRING;
			for (int i = 0; i < moveItems.length; i++)
			{
				moveId = moveItems[i];
				try
				{
					ResourceProperties properties = ContentHostingService.getProperties (moveId);
					/*
					if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
					{
						String alert = (String) state.getAttribute(STATE_MESSAGE);
						if (alert == null || ((alert != null) && (alert.indexOf(rb.getString("notsupported")) == -1)))
						{
							addAlert(state, rb.getString("notsupported"));
						}
					}
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

				ContentHostingService.eliminateDuplicates(moveItemsVector);

				state.setAttribute (STATE_MOVED_IDS, moveItemsVector);

			}	// if-else
		}	// if-else

	}	// doMove
	
	public void doMultiItemDispatch ( RunData data )
	{
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
		
	}

	/**
	* Navigate in the resource hireachy
	*/
	public void doNavigate ( RunData data )
	{
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
			ContentHostingService.checkCollection(collectionId);
		}
		catch(PermissionException e)
		{
			addAlert(state, " " + rb.getString("notpermis3") + " " );
		}
		catch (IdUnusedException e)
		{
			addAlert(state, " " + rb.getString("notexist2") + " ");
		}
		catch (TypeException e)
		{
			addAlert(state," " + rb.getString("notexist2") + " ");
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			String oldCollectionId = (String) state.getAttribute(STATE_COLLECTION_ID);
			// update this folder id in the set to be event-observed
			removeObservingPattern(oldCollectionId, state);
			addObservingPattern(collectionId, state);

			state.setAttribute(STATE_COLLECTION_ID, collectionId);
			
			SortedSet currentMap = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			if(currentMap == null)
			{
				currentMap = new TreeSet();
				state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);
			}
			
			Map sortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
			if(sortMap == null)
			{
				sortMap = new HashMap();
				state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, sortMap);
			}
			
			Iterator it = currentMap.iterator();
			while(it.hasNext())
			{
				String id = (String) it.next();
				if(id.startsWith(collectionId))
				{
					it.remove();
					sortMap.remove(id);
					removeObservingPattern(id, state);
				}
			}
			
			if(!currentMap.contains(collectionId))
			{
				currentMap.add (collectionId);

				// add this folder id into the set to be event-observed
				addObservingPattern(collectionId, state);
			}
			//state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, new HashMap());
		}

	}	// doNavigate

	/**
	* Fire up the permissions editor for the tool's permissions
	*/
	public void doPermissions(RunData data, Context context)
	{
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

		// get the current home collection id and the related site
		String collectionId = (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
		Reference ref = EntityManager.newReference(ContentHostingService.getReference(collectionId));
		String siteRef = SiteService.siteReference(ref.getContext());

		// setup for editing the permissions of the site for this tool, using the roles of this site, too
		state.setAttribute(PermissionsHelper.TARGET_REF, siteRef);

		// ... with this description
		state.setAttribute(PermissionsHelper.DESCRIPTION, rb.getString("setpermis1")
				+ SiteService.getSiteDisplay(ref.getContext()));

		// ... showing only locks that are prpefixed with this
		state.setAttribute(PermissionsHelper.PREFIX, "content.");

		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.permissions.helper");

	}	// doPermissions

	/**
	* Sort based on the given property
	*/
	public void doReorder ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();
		
		boolean isPrioritySortEnabled = ContentHostingService.isSortByPriorityEnabled();


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

		Comparator comparator = ContentHostingService.newContentHostingComparator(sortBy, Boolean.getBoolean(sortedAsc));
		state.setAttribute(STATE_REORDER_SORT, comparator);

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_REORDER_FOLDER, folderId);
			state.setAttribute (STATE_MODE, MODE_REORDER);

		}	// if-else

	}	// doReorder

	public void doReviseProperties(RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		String user_action = params.getString("user_action");
		
		if(user_action.equals("save"))
		{
			String entityId = (String) state.getAttribute(STATE_REVISE_PROPERTIES_ENTITY_ID);
			ListItem item = (ListItem) state.getAttribute(STATE_REVISE_PROPERTIES_ITEM);
			ResourceToolAction action = (ResourceToolAction) state.getAttribute(STATE_REVISE_PROPERTIES_ACTION);
			
			if(item == null)
			{
				
			}
			item.captureProperties(params, ListItem.DOT + "0");
			
			// notification
			int noti = NotificationService.NOTI_NONE;
			// %%STATE_MODE_RESOURCES%%
			if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
			{
   				boolean notification = params.getBoolean("notify_dropbox");
  				if(notification)
   				{
   					noti = NotificationService.NOTI_REQUIRED;
   				}
   				else
   				{
   					// set noti to none if in dropbox mode
   					noti = NotificationService.NOTI_NONE;
   				}
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
			
			List<String> alerts = item.checkRequiredProperties();
			
			if(alerts.isEmpty())
			{
				try 
				{
					if(item.isCollection())
					{
						ContentCollectionEdit entity = ContentHostingService.editCollection(entityId);
						item.updateContentCollectionEdit(entity);
						
						ContentHostingService.commitCollection(entity);
					}
					else
					{
						ContentResourceEdit entity = ContentHostingService.editResource(entityId);
						item.updateContentResourceEdit(entity);
						ContentHostingService.commitResource(entity, noti);
					}

					state.setAttribute(STATE_MODE, MODE_LIST);
				} 
				catch (IdUnusedException e) 
				{
					logger.warn("IdUnusedException", e);
				} 
				catch (TypeException e) 
				{
					logger.warn("TypeException", e);
				} 
				catch (PermissionException e) 
				{
					logger.warn("PermissionException", e);
				} 
				catch (ServerOverloadException e) 
				{
					logger.warn("ServerOverloadException", e);
				}
				catch (OverQuotaException e)
				{
					// TODO Auto-generated catch block
					logger.warn("OverQuotaException ", e);
				}
				catch (InUseException e)
				{
					// TODO Auto-generated catch block
					logger.warn("InUseException ", e);
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
		else if(user_action.equals("cancel"))
		{
			state.setAttribute(STATE_MODE, MODE_LIST);
		}
	}

	/**
	* Sort based on the given property
	*/
	public void doSaveOrder ( RunData data)
	{
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
					ContentCollectionEdit collection = ContentHostingService.editCollection(folderId);
					List memberIds = collection.getMembers();
					Map priorities = new HashMap();
					Iterator it = memberIds.iterator();
					while(it.hasNext())
					{
						String memberId = (String) it.next();
						int position = params.getInt("position_" + Validator.escapeUrl(memberId));
						priorities.put(memberId, new Integer(position));
					}
					collection.setPriorityMap(priorities);
					
					ContentHostingService.commitCollection(collection);
					
					SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
					if(expandedCollections == null)
					{
						expandedCollections = (SortedSet) new TreeSet();
						state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
					}
					expandedCollections.add(folderId);
					
					Comparator comparator = ContentHostingService.newContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, true);
					Map expandedFolderSortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
					if(expandedFolderSortMap == null)
					{
						expandedFolderSortMap = new HashMap();
						state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, expandedFolderSortMap);
					}
					expandedFolderSortMap.put(folderId, comparator);
				}
				catch(IdUnusedException e)
				{
					addAlert(state, trb.getString("alert.nosort"));
					logger.warn("IdUnusedException" + e);
				}
				catch(TypeException e)
				{
					addAlert(state, trb.getString("alert.nosort"));
					logger.warn("TypeException" + e);
				}
				catch(PermissionException e)
				{
					addAlert(state, trb.getString("alert.nosort"));
					logger.warn("PermissionException" + e);
				}
				catch(InUseException e)
				{
					addAlert(state, trb.getString("alert.nosort"));
					logger.warn("InUseException" + e);
				}
			}
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_MODE, MODE_LIST);

		}	// if-else

	}	// doSaveOrder

//	/**
//	 * @param data
//	 */
//	public void doShow_metadata(RunData data)
//	{
//		ParameterParser params = data.getParameters ();
//		String name = params.getString("metadataGroup");
//
//		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
//		
//		
//		
//		List metadataGroups = (List) state.getAttribute(ListItem.STATE_METADATA_GROUPS);
//		if(metadataGroups != null && ! metadataGroups.isEmpty())
//		{
//			boolean found = false;
//			MetadataGroup group = null;
//			Iterator it = metadataGroups.iterator();
//			while(!found && it.hasNext())
//			{
//				group = (MetadataGroup) it.next();
//				found = (name.equals(Validator.escapeUrl(group.getName())) || name.equals(group.getName()));
//			}
//			if(found)
//			{
//				group.setShowing(true);
//			}
//		}
//
//	}	// doShow_metadata

	/**
	* Show information about WebDAV
	*/
	public void doShow_webdav ( RunData data )
	{
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

			


	public void doShowMembers(RunData data)
	{
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

		if (criteria.equals ("title"))
		{
			criteria = ResourceProperties.PROP_DISPLAY_NAME;
		}
		else if (criteria.equals ("size"))
		{
			criteria = ResourceProperties.PROP_CONTENT_LENGTH;
		}
		else if (criteria.equals ("created by"))
		{
			criteria = ResourceProperties.PROP_CREATOR;
		}
		else if (criteria.equals ("last modified"))
		{
			criteria = ResourceProperties.PROP_MODIFIED_DATE;
		}
		else if (criteria.equals("priority") && ContentHostingService.isSortByPriorityEnabled())
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
		String asc = NULL_STRING;
		boolean bValue = true;
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
			if (asc.equals (Boolean.TRUE.toString()))
			{
				bValue = false;
			}
			else
			{
				bValue = true;
			}
			asc = Boolean.toString(bValue);
			state.setAttribute (sortAsc_attribute, asc);
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			Comparator comparator = ContentHostingService.newContentHostingComparator(criteria, bValue);
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

		state.setAttribute(STATE_EXPANDED_COLLECTIONS, new TreeSet());
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

		List<ResourceType> typeDefs = new ArrayList<ResourceType>(registry.getTypes());

		String siteId = params.getString("siteId");
		if(siteId == null || siteId.trim().equals(""))
		{
			String home = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
			Reference ref = EntityManager.newReference(ContentHostingService.getReference(home));
			siteId = ref.getContext();
		}
		
		Map<String,Boolean> statusMap = new HashMap<String,Boolean>();

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
				statusMap.put(typeDef.getId(), new Boolean(enabledTypes.contains(typeDef.getId())));
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
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_MODE, MODE_LIST);
		
	}
	
	/**
	* Find the resource with this id in the list.
	* @param messages The list of messages.
	* @param id The message id.
	* @return The index position in the list of the message with this id, or -1 if not found.
	*/
	protected int findResourceInList(List resources, String id)
	{
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
				SortedSet<String> expandedCollections = (SortedSet<String>) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
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
				SortedSet<String> expandedCollections = (SortedSet<String>) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
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
				SortedSet<String> expandedCollections = (SortedSet<String>) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
				expandedCollections.add(pipe.getContentEntity().getId());
				toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			}
			state.setAttribute(STATE_MODE, MODE_LIST);
			break;
		case REVISE_CONTENT:
			reviseContent(pipe);
			if(pipe.isErrorEncountered())
			{
				String msg = pipe.getErrorMessage();
				if(msg == null || msg.trim().equals(""))
				{
					msg = rb.getString("alert.unknown");
				}
				addAlert(state, msg);
			}
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			state.setAttribute(STATE_MODE, MODE_LIST);
			break;
		case REPLACE_CONTENT:
			replaceContent(pipe);
			if(pipe.isErrorEncountered())
			{
				String msg = pipe.getErrorMessage();
				if(msg == null || msg.trim().equals(""))
				{
					msg = rb.getString("alert.unknown");
				}
				addAlert(state, msg);
				toolSession.removeAttribute(ResourceToolAction.DONE);
				return;
			}
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
		ResourceToolAction action = pipe.getAction();
		ContentEntity entity = pipe.getContentEntity();
		try
		{
			ContentResourceEdit edit = ContentHostingService.editResource(entity.getId());
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			// update content
			extractContent(pipe, edit);
//			byte[] content = pipe.getRevisedContent();
//			if(content == null)
//			{
//				InputStream stream = pipe.getRevisedContentStream();
//				if(stream == null)
//				{
//					logger.debug("pipe with null content and null stream: " + pipe.getFileName());
//				}
//				else
//				{
//					edit.setContent(stream);
//				}
//			}
//			else
//			{
//				edit.setContent(content);
//			}
			
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
			ContentHostingService.commitResource(edit, notification);
		}
		catch (PermissionException e)
		{
			pipe.setErrorEncountered(true);
			pipe.setErrorMessage(trb.getString("alert.noperm"));
			logger.warn("PermissionException " + e);
		}
		catch (IdUnusedException e)
		{
			pipe.setErrorEncountered(true);
			logger.warn("IdUnusedException ", e);
		}
		catch (TypeException e)
		{
			pipe.setErrorEncountered(true);
			logger.warn("TypeException ", e);
		}
		catch (InUseException e)
		{
			pipe.setErrorEncountered(true);
			logger.warn("InUseException ", e);
		}
		catch (OverQuotaException e)
		{
			pipe.setErrorEncountered(true);
			pipe.setErrorMessage(trb.getString("alert.quota"));
			logger.warn("OverQuotaException " + e);
		}
		catch (ServerOverloadException e)
		{
			pipe.setErrorEncountered(true);
			pipe.setErrorMessage(trb.getString("alert.unable"));
			logger.warn("ServerOverloadException ", e);
		}
	}

	/**
	* Populate the state object, if needed - override to do something!
	*/
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData data)
	{
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
		if (state.getAttribute (STATE_INITIALIZED) != null) return;

		if (state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE) == null)
		{
			String uploadMax = ServerConfigurationService.getString("content.upload.max");
			String uploadCeiling = ServerConfigurationService.getString("content.upload.ceiling");
			
			if(uploadMax == null && uploadCeiling == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, "1");
			}
			else if(uploadCeiling == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, uploadMax);
			}
			else if(uploadMax == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, uploadMax);
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
			Integer size = new Integer(config.getInitParameter(PARAM_PAGESIZE));
			if(size == null || size.intValue() < 1)
			{
				size = new Integer(DEFAULT_PAGE_SIZE);
			}
			state.setAttribute(STATE_PAGESIZE, size);
		}
		catch(Exception any)
		{
			state.setAttribute(STATE_PAGESIZE, new Integer(DEFAULT_PAGE_SIZE));
		}

		// state.setAttribute(STATE_TOP_PAGE_MESSAGE_ID, "");

		state.setAttribute (STATE_CONTENT_SERVICE, ContentHostingService.getInstance());
		state.setAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE, ContentTypeImageService.getInstance());
		state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry"));

		TimeBreakdown timeBreakdown = (TimeService.newTime()).breakdownLocal ();
		String mycopyright = rb.getString("cpright1") + " " + timeBreakdown.getYear () +", " + UserDirectoryService.getCurrentUser().getDisplayName () + ". All Rights Reserved. ";
		state.setAttribute (STATE_MY_COPYRIGHT, mycopyright);

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

		boolean show_other_sites = false;
		if(RESOURCES_MODE_HELPER.equals(resources_mode))
		{
			show_other_sites = ServerConfigurationService.getBoolean("resources.show_all_collections.helper", SHOW_ALL_SITES_IN_FILE_PICKER);
		}
		else if(RESOURCES_MODE_DROPBOX.equals(resources_mode))
		{
			show_other_sites = ServerConfigurationService.getBoolean("resources.show_all_collections.dropbox", SHOW_ALL_SITES_IN_DROPBOX);
		}
		else
		{
			show_other_sites = ServerConfigurationService.getBoolean("resources.show_all_collections.tool", SHOW_ALL_SITES_IN_RESOURCES);
		}
		
		/** set attribute for the maximum size at which the resources tool will expand a collection. */
		int expandableFolderSizeLimit = ServerConfigurationService.getInt("resources.expanded_folder_size_limit", EXPANDABLE_FOLDER_SIZE_LIMIT);
		state.setAttribute(STATE_EXPANDABLE_FOLDER_SIZE_LIMIT, new Integer(expandableFolderSizeLimit));
		
		/** This attribute indicates whether "Other Sites" twiggle should show */
		state.setAttribute(STATE_SHOW_ALL_SITES, Boolean.toString(show_other_sites));
		/** This attribute indicates whether "Other Sites" twiggle should be open */
		state.setAttribute(STATE_SHOW_OTHER_SITES, Boolean.FALSE.toString());

		// set the home collection to the parameter, if present, or the default if not
		String home = StringUtil.trimToNull(portlet.getPortletConfig().getInitParameter("home"));
		state.setAttribute (STATE_HOME_COLLECTION_DISPLAY_NAME, home);
		if ((home == null) || (home.length() == 0))
		{
			// no home set, see if we are in dropbox mode
			if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase(resources_mode))
			{
				home = ContentHostingService.getDropboxCollection();

				// if it came back null, we will pretend not to be in dropbox mode
				if (home != null)
				{
					state.setAttribute(STATE_HOME_COLLECTION_DISPLAY_NAME, ContentHostingService.getDropboxDisplayName());

					// create/update the collection of folders in the dropbox
					ContentHostingService.createDropboxCollection();
				}
			}

			// if we still don't have a home,
			if ((home == null) || (home.length() == 0))
			{
				home = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());

				// TODO: what's the 'name' of the context? -ggolden
				// we'll need this to create the home collection if needed
				state.setAttribute (STATE_HOME_COLLECTION_DISPLAY_NAME, ToolManager.getCurrentPlacement().getContext()
						/*SiteService.getSiteDisplay(ToolManager.getCurrentPlacement().getContext()) */);
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
				title = ((Site) SiteService.getSite(ToolManager.getCurrentPlacement().getContext())).getTitle();
			}
			catch (IdUnusedException e)
			{	// ignore
			}
			state.setAttribute(STATE_SITE_TITLE, title);
		}

		SortedSet expandedCollections = new TreeSet();
		//expandedCollections.add (state.getAttribute (STATE_HOME_COLLECTION_ID));
		state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
		state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, new HashMap());
		
		state.setAttribute(STATE_DROPBOX_HIGHLIGHT, new Integer(1));
		
		if(state.getAttribute(STATE_USING_CREATIVE_COMMONS) == null)
		{
			String usingCreativeCommons = ServerConfigurationService.getString("copyright.use_creative_commons");
			if( usingCreativeCommons != null && usingCreativeCommons.equalsIgnoreCase(Boolean.TRUE.toString()))
			{
				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.TRUE.toString());
			}
			else
			{
				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.FALSE.toString());
			}
		}

		if (state.getAttribute(STATE_COPYRIGHT_TYPES) == null)
		{
			if (ServerConfigurationService.getStrings("copyrighttype") != null)
			{
				state.setAttribute(STATE_COPYRIGHT_TYPES, new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("copyrighttype"))));
			}
		}

		if (state.getAttribute(STATE_DEFAULT_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("default.copyright") != null)
			{
				state.setAttribute(STATE_DEFAULT_COPYRIGHT, ServerConfigurationService.getString("default.copyright"));
			}
		}

		if (state.getAttribute(STATE_DEFAULT_COPYRIGHT_ALERT) == null)
		{
			if (ServerConfigurationService.getString("default.copyright.alert") != null)
			{
				state.setAttribute(STATE_DEFAULT_COPYRIGHT_ALERT, ServerConfigurationService.getString("default.copyright.alert"));
			}
		}

		if (state.getAttribute(STATE_NEW_COPYRIGHT_INPUT) == null)
		{
			if (ServerConfigurationService.getString("newcopyrightinput") != null)
			{
				state.setAttribute(STATE_NEW_COPYRIGHT_INPUT, ServerConfigurationService.getString("newcopyrightinput"));
			}
		}

		if (state.getAttribute(STATE_COPYRIGHT_FAIRUSE_URL) == null)
		{
			if (ServerConfigurationService.getString("fairuse.url") != null)
			{
				state.setAttribute(STATE_COPYRIGHT_FAIRUSE_URL, ServerConfigurationService.getString("fairuse.url"));
			}
		}

		if (state.getAttribute(STATE_COPYRIGHT_NEW_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("copyrighttype.new") != null)
			{
				state.setAttribute(STATE_COPYRIGHT_NEW_COPYRIGHT, ServerConfigurationService.getString("copyrighttype.new"));
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
		String siteType = null;
		Site site = null;
		try
		{
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			siteType = site.getType();
			if(siteTypes != null)
			{
				for(int i = 0; i < siteTypes.length; i++)
				{
					if ((StringUtil.trimToNull(siteTypes[i])).equals(siteType))
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
		
		Time defaultRetractTime = null;
		defaultRetractTime = TimeService.newTime(TimeService.newTime().getTime() + ONE_WEEK);			
		org.sakaiproject.content.api.ContentHostingService chs = ContentHostingService.getInstance();
		if ( site != null && chs instanceof SiteContentAdvisorProvider ) {
			SiteContentAdvisorProvider scap = (SiteContentAdvisorProvider) chs;
			SiteContentAdvisor sca =  scap.getContentAdvisor(site);
			if ( sca != null ) {
				defaultRetractTime = TimeService.newTime(sca.getDefaultRetractTime());
			}
		}
		
		
		state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractTime);
		
		if(state.getAttribute(STATE_LIST_PREFERENCE) == null)
		{
			state.setAttribute(STATE_LIST_PREFERENCE, LIST_HIERARCHY);
		}
			
		state.setAttribute (STATE_INITIALIZED, Boolean.TRUE.toString());

	}

/**
	* is notification enabled?
	*/
	protected boolean notificationEnabled(SessionState state)
	{
		return true;

	}	// notificationEnabled

	/**
	 * @param state
	 */
	protected void pasteItem(SessionState state, String collectionId)
	{
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
				ContentEntity entity = null;
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
				
				ResourceToolAction action = null;
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
					
					ref = EntityManager.newReference(entity.getReference());
					
					slAction.initializeAction(ref);
					
					// paste copied item into collection 
					String newId = null;
					
					if(moving)
					{
						newId = contentService.moveIntoFolder(entityId, collectionId);
					}
					else
					{
						newId = contentService.copyIntoFolder(entityId, collectionId);
					}
					
					ref = EntityManager.newReference(contentService.getReference(newId));
					
					slAction.finalizeAction(ref);
					
					SortedSet<String> expandedCollections = (SortedSet<String>) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
					if(expandedCollections == null)
					{
						expandedCollections = new TreeSet();
						state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
					}
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
				logger.warn(ref.getReference(), e);
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
		List<ListItem> rv = new ArrayList<ListItem>();

		// access the page size
		int pageSize = ((Integer) state.getAttribute(STATE_PAGESIZE)).intValue();

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
		state.setAttribute(STATE_NUM_MESSAGES, new Integer(numMessages));

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
		int numMessagesOnThisPage = (posEnd - posStart) + 1;

		// select the messages on this page
		for (int i = posStart; i <= posEnd; i++)
		{
			rv.add(allMessages.get(i));
		}

		// save which message is at the top of the page
		ListItem itemAtTheTopOfThePage = (ListItem) allMessages.get(posStart);
		state.setAttribute(STATE_TOP_PAGE_MESSAGE_ID, itemAtTheTopOfThePage.getId());
		state.setAttribute(STATE_TOP_MESSAGE_INDEX, new Integer(posStart));


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
	 * Processes the HTML document that is coming back from the browser
	 * (from the formatted text editing widget).
	 * @param state Used to pass in any user-visible alerts or errors when processing the text
	 * @param strFromBrowser The string from the browser
	 * @return The formatted text
	 */
	private String processHtmlDocumentFromBrowser(SessionState state, String strFromBrowser)
	{
		StringBuilder alertMsg = new StringBuilder();
		String text = FormattedText.processHtmlDocument(strFromBrowser, alertMsg);
		if (alertMsg.length() > 0) addAlert(state, alertMsg.toString());
		return text;
	}

	/**
	* Develop a list of all the site collections that there are to page.
	* Sort them as appropriate, and apply search criteria.
	*/
	protected List<ListItem> readAllResources(SessionState state)
	{
		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		List<ListItem> other_sites = new ArrayList<ListItem>();

		String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		SortedSet<String> expandedCollections = (SortedSet<String>) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(expandedCollections == null)
		{
			expandedCollections = new TreeSet();
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
		}
		
		Comparator userSelectedSort = (Comparator) state.getAttribute(STATE_LIST_VIEW_SORT);
		
		// set the sort values
		String sortedBy = (String) state.getAttribute (STATE_SORT_BY);
		String sortedAsc = (String) state.getAttribute (STATE_SORT_ASC);
		
		Boolean showRemove = (Boolean) state.getAttribute(STATE_SHOW_REMOVE_ACTION);
		boolean showRemoveAction = showRemove != null && showRemove.booleanValue();
		
		Boolean showMove = (Boolean) state.getAttribute(STATE_SHOW_MOVE_ACTION);
		boolean showMoveAction = showMove != null && showMove.booleanValue();
		
		Boolean showCopy = (Boolean) state.getAttribute(STATE_SHOW_COPY_ACTION);
		boolean showCopyAction = showCopy != null && showCopy.booleanValue();
		
		Set highlightedItems = (Set) state.getAttribute(STATE_HIGHLIGHTED_ITEMS);

		// add user's personal workspace
		User user = UserDirectoryService.getCurrentUser();
		String userId = user.getId();
		String userName = user.getDisplayName();
		String wsId = SiteService.getUserSiteId(userId);
		String wsCollectionId = ContentHostingService.getSiteCollection(wsId);
		List<String> items_to_be_copied = (List<String>) state.getAttribute(STATE_ITEMS_TO_BE_COPIED);
		List<String> items_to_be_moved = (List<String>) state.getAttribute(STATE_ITEMS_TO_BE_MOVED);
		
		if(! collectionId.equals(wsCollectionId))
		{
            try
            {
            	ContentCollection wsCollection = ContentHostingService.getCollection(wsCollectionId);
				ListItem wsRoot = ListItem.getListItem(wsCollection, null, registry, false, expandedCollections, items_to_be_moved, items_to_be_copied, 0, userSelectedSort, false, null);
		        other_sites.add(wsRoot);
            }
            catch (IdUnusedException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("IdUnusedException ", e);
            }
            catch (TypeException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("TypeException ", e);
            }
            catch (PermissionException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("PermissionException ", e);
            }
		}
		
 		/*
		 * add all other sites user has access to
		 * NOTE: This does not (and should not) get all sites for admin.  
		 *       Getting all sites for admin is too big a request and
		 *       would result in too big a display to render in html.
		 */
		Map othersites = ContentHostingService.getCollectionMap();
		Iterator siteIt = othersites.keySet().iterator();
		SortedSet sort = new TreeSet();
		while(siteIt.hasNext())
		{
              String collId = (String) siteIt.next();
              String displayName = (String) othersites.get(collId);
              sort.add(displayName + DELIM + collId);
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
	                collection = ContentHostingService.getCollection(collId);
					ListItem root = ListItem.getListItem(collection, null, registry, false, expandedCollections, items_to_be_moved, items_to_be_copied, 0, null, false, null);
					root.setName(displayName);
					other_sites.add(root);
                }
                catch (IdUnusedException e)
                {
	                // TODO Auto-generated catch block
	                logger.warn("IdUnusedException " + e);
                }
                catch (TypeException e)
                {
	                // TODO Auto-generated catch block
	                logger.warn("TypeException " + e);
                }
                catch (PermissionException e)
                {
	                // TODO Auto-generated catch block
	                logger.warn("PermissionException " + e);
                }
			}
          }
		
		return other_sites;
	}

	/**
	* Setup our observer to be watching for change events for the collection
 	*/
 	private void updateObservation(SessionState state, String peid)
 	{
// 		ContentObservingCourier observer = (ContentObservingCourier) state.getAttribute(STATE_OBSERVER);
//
// 		// the delivery location for this tool
// 		String deliveryId = clientWindowId(state, peid);
// 		observer.setDeliveryId(deliveryId);
	}

	public static List<ContentResource> createUrls(SessionState state, ResourceToolActionPipe pipe)
    {
		boolean item_added = false;
		String collectionId = null;
		List<ContentResource> new_resources = new ArrayList<ContentResource>();
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) pipe;
		Iterator<ResourceToolActionPipe> pipeIt = mfp.getPipes().iterator();
		while(pipeIt.hasNext())
		{
			ResourceToolActionPipe fp = pipeIt.next();
			collectionId = pipe.getContentEntity().getId();
			String name = fp.getFileName();
			if(name == null || name.trim().equals(""))
			{
				continue;
			}
			String basename = name.trim();
			String extension = "";
			if(name.contains("."))
			{
				String[] parts = name.split("\\.");
				basename = parts[0];
				if(parts.length > 1)
				{
					extension = parts[parts.length - 1];
				}
				
				for(int i = 1; i < parts.length - 1; i++)
				{
					basename += "." + parts[i];
					// extension = parts[i + 1];
				}
			}
			try
			{
				ContentResourceEdit resource = ContentHostingService.addResource(collectionId,Validator.escapeResourceName(basename),Validator.escapeResourceName(extension),MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				
				extractContent(fp, resource);
//				byte[] content = fp.getRevisedContent();
//				if(content == null)
//				{
//					InputStream stream = fp.getRevisedContentStream();
//					if(stream == null)
//					{
//						logger.debug("pipe with null content and null stream: " + pipe.getFileName());
//					}
//					else
//					{
//						resource.setContent(stream);
//					}
//				}
//				else
//				{
//					resource.setContent(content);
//				}

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
				if(displayName == null || displayName.trim().equals(""))
				{
					displayName = name;
				}
				if(displayName != null)
				{
					resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
				}
				Map values = pipe.getRevisedResourceProperties(); 	 
				Iterator valueIt = values.keySet().iterator(); 	 
				while(valueIt.hasNext()) 	 
				{ 	 
					String pname = (String) valueIt.next(); 	 
					String pvalue = (String) values.get(pname); 	 
					resourceProperties.addProperty(pname, pvalue);
				}
				try
				{
					ContentHostingService.commitResource(resource, notification);
					item_added = true;
					new_resources.add(resource);
				}
				catch(OverQuotaException e)
				{
					addAlert(trb.getFormattedMessage("alert.overquota", new String[]{name}));
					logger.debug("OverQuotaException " + e);
					try
					{
						ContentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						logger.debug("Unable to remove partially completed resource: " + resource.getId() + "\n" + e); 
					}
				}
				catch(ServerOverloadException e)
				{
					addAlert(trb.getFormattedMessage("alert.unable1", new String[]{name}));
					logger.debug("ServerOverloadException " + e);
					try
					{
						ContentHostingService.removeResource(resource.getId());
					}
					catch(Exception e1)
					{
						logger.debug("Unable to remove partially completed resource: " + resource.getId() + "\n" + e); 
					}
				}
			}
			catch (PermissionException e)
			{
				addAlert(state, trb.getString("alert.perm"));
				logger.warn("PermissionException ", e);
			}
			catch (IdUnusedException e)
			{
				logger.warn("IdUsedException ", e);
			}
			catch (IdInvalidException e)
			{
				logger.warn("IdInvalidException ", e);
			}
			catch (IdUniquenessException e)
			{
				logger.warn("IdUniquenessException ", e);
			}
			catch (IdLengthException e)
			{
				addAlert(state, trb.getFormattedMessage("alert.toolong", new String[]{e.getMessage()}));
				// TODO Auto-generated catch block
				logger.warn("IdLengthException ", e);
			}
			catch (OverQuotaException e)
			{
				addAlert(trb.getFormattedMessage("alert.overquota", new String[]{name}));
				logger.warn("OverQuotaException ", e);
			}
			catch (ServerOverloadException e)
			{
				addAlert(trb.getFormattedMessage("alert.unable1", new String[]{name}));
				logger.warn("ServerOverloadException ", e);
			}
		}
		
		return (item_added ? new_resources : null);
   }

	public static void addAlert(String message)
	{
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		Collection<String> errorMessages = (Collection<String>) toolSession.getAttribute(STATE_MESSAGE_LIST);
		if(errorMessages == null)
		{
			errorMessages = new TreeSet();
			toolSession.setAttribute(STATE_MESSAGE_LIST, errorMessages);
		}
		errorMessages.add(message);
	}
	
}	// ResourcesAction
