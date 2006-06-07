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

package org.sakaiproject.content.tool;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
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
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentResourceFilter;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.content.api.GroupAwareEntity;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
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
import org.sakaiproject.metaobj.shared.control.SchemaBean;
import org.sakaiproject.metaobj.shared.mgt.HomeFactory;
import org.sakaiproject.metaobj.shared.mgt.StructuredArtifactValidationService;
import org.sakaiproject.metaobj.shared.mgt.home.StructuredArtifactHomeInterface;
import org.sakaiproject.metaobj.shared.model.ElementBean;
import org.sakaiproject.metaobj.shared.model.ValidationError;
import org.sakaiproject.metaobj.utils.xml.SchemaNode;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
* <p>ResourceAction is a ContentHosting application</p>
*
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
public class ResourcesAction
	extends PagedResourceHelperAction // VelocityPortletPaneledAction
{
	/** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("content");

    private static final Log logger = LogFactory.getLog(ResourcesAction.class);

	/** Name of state attribute containing a list of opened/expanded collections */
	private static final String STATE_EXPANDED_COLLECTIONS = "resources.expanded_collections";

	/** Name of state attribute for status of initialization.  */
	private static final String STATE_INITIALIZED = "resources.initialized";

	/** The content hosting service in the State. */
	private static final String STATE_CONTENT_SERVICE = "resources.content_service";

	/** The content type image lookup service in the State. */
	private static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = "resources.content_type_image_service";

	/** The resources, helper or dropbox mode. */
	public static final String STATE_MODE_RESOURCES = "resources.resources_mode";

	/** The resources, helper or dropbox mode. */
	public static final String STATE_RESOURCES_HELPER_MODE = "resources.resources_helper_mode";

	/** state attribute for the maximum size for file upload */
	private static final String STATE_FILE_UPLOAD_MAX_SIZE = "resources.file_upload_max_size";
	
	/** state attribute indicating whether users in current site should be denied option of making resources public */
	private static final String STATE_PREVENT_PUBLIC_DISPLAY = "resources.prevent_public_display";

	/** The name of a state attribute indicating whether the resources tool/helper is allowed to show all sites the user has access to */
	public static final String STATE_SHOW_ALL_SITES = "resources.allow_user_to_see_all_sites";

	/** The name of a state attribute indicating whether the wants to see other sites if that is enabled */
	public static final String STATE_SHOW_OTHER_SITES = "resources.user_chooses_to_see_other_sites";

	/** The user copyright string */
	private static final String	STATE_MY_COPYRIGHT = "resources.mycopyright";

	/** copyright path -- MUST have same value as AccessServlet.COPYRIGHT_PATH */
	public static final String COPYRIGHT_PATH = Entity.SEPARATOR + "copyright";

	/** The collection id being browsed. */
	private static final String STATE_COLLECTION_ID = "resources.collection_id";

	/** The id of the "home" collection (can't go up from here.) */
	private static final String STATE_HOME_COLLECTION_ID = "resources.collection_home";

	/** The display name of the "home" collection (can't go up from here.) */
	private static final String STATE_HOME_COLLECTION_DISPLAY_NAME = "resources.collection_home_display_name";

	/** The inqualified input field */
	private static final String STATE_UNQUALIFIED_INPUT_FIELD = "resources.unqualified_input_field";

	/** The collection id path */
	private static final String STATE_COLLECTION_PATH = "resources.collection_path";

	/** The name of the state attribute containing BrowseItems for all content collections the user has access to */
	private static final String STATE_COLLECTION_ROOTS = "resources.collection_rootie_tooties";

	/** The sort by */
	private static final String STATE_SORT_BY = "resources.sort_by";

	/** The sort ascending or decending */
	private static final String STATE_SORT_ASC = "resources.sort_asc";

	/** The copy flag */
	private static final String STATE_COPY_FLAG = "resources.copy_flag";

	/** The cut flag */
	private static final String STATE_CUT_FLAG = "resources.cut_flag";

	/** The can-paste flag */
	private static final String STATE_PASTE_ALLOWED_FLAG = "resources.can_paste_flag";

	/** The move flag */
	private static final String STATE_MOVE_FLAG = "resources.move_flag";

	/** The select all flag */
	private static final String STATE_SELECT_ALL_FLAG = "resources.select_all_flag";

	/** The name of the state attribute indicating whether the hierarchical list is expanded */
	private static final String STATE_EXPAND_ALL_FLAG = "resources.expand_all_flag";

	/** The name of the state attribute indicating whether the hierarchical list needs to be expanded */
	private static final String STATE_NEED_TO_EXPAND_ALL = "resources.need_to_expand_all";

	/** The name of the state attribute containing a java.util.Set with the id's of selected items */
	private static final String STATE_LIST_SELECTIONS = "resources.ignore_delete_selections";

	/** The root of the navigation breadcrumbs for a folder, either the home or another site the user belongs to */
	private static final String STATE_NAVIGATION_ROOT = "resources.navigation_root";

	/************** the more context *****************************************/

	/** The more id */
	private static final String STATE_MORE_ID = "resources.more_id";

	/** The more collection id */
	private static final String STATE_MORE_COLLECTION_ID = "resources.more_collection_id";

	/************** the edit context *****************************************/

	/** The edit id */
	public static final String STATE_EDIT_ID = "resources.edit_id";
	public static final String STATE_STACK_EDIT_ID = "resources.stack_edit_id";
	public static final String STATE_EDIT_COLLECTION_ID = "resources.stack_edit_collection_id";
	public static final String STATE_STACK_EDIT_COLLECTION_ID = "resources.stack_edit_collection_id";

	private static final String STATE_EDIT_ALERTS = "resources.edit_alerts";
	private static final String STATE_STACK_EDIT_ITEM = "resources.stack_edit_item";
	private static final String STATE_STACK_EDIT_INTENT = "resources.stack_edit_intent";

	private static final String STATE_SHOW_FORM_ITEMS = "resources.show_form_items";

	private static final String STATE_STACK_EDIT_ITEM_TITLE = "resources.stack_title";

	/************** the create contexts *****************************************/

	public static final String STATE_SUSPENDED_OPERATIONS_STACK = "resources.suspended_operations_stack";
	public static final String STATE_SUSPENDED_OPERATIONS_STACK_DEPTH = "resources.suspended_operations_stack_depth";

	public static final String STATE_CREATE_TYPE = "resources.create_type";
	public static final String STATE_CREATE_COLLECTION_ID = "resources.create_collection_id";
	public static final String STATE_CREATE_NUMBER = "resources.create_number";
	public static final String STATE_STRUCTOBJ_TYPE = "resources.create_structured_object_type";
	public static final String STATE_STRUCTOBJ_TYPE_READONLY = "resources.create_structured_object_type_readonly";

	public static final String STATE_STACK_CREATE_TYPE = "resources.stack_create_type";
	public static final String STATE_STACK_CREATE_COLLECTION_ID = "resources.stack_create_collection_id";
	public static final String STATE_STACK_CREATE_NUMBER = "resources.stack_create_number";
	public static final String STATE_STACK_STRUCTOBJ_TYPE = "resources.stack_create_structured_object_type";
	public static final String STATE_STACK_STRUCTOBJ_TYPE_READONLY = "resources.stack_create_structured_object_type_readonly";

	private static final String STATE_STACK_CREATE_ITEMS = "resources.stack_create_items";
	private static final String STATE_STACK_CREATE_ACTUAL_COUNT = "resources.stack_create_actual_count";
	private static final String STATE_STACK_STRUCTOBJ_ROOTNAME = "resources.stack_create_structured_object_root";

	private static final String STATE_CREATE_ALERTS = "resources.create_alerts";
	protected static final String STATE_CREATE_MESSAGE = "resources.create_message";
	private static final String STATE_CREATE_MISSING_ITEM = "resources.create_missing_item";
	private static final String STATE_STRUCTOBJ_HOMES = "resources.create_structured_object_home";
	private static final String STATE_STACK_STRUCT_OBJ_SCHEMA = "resources.stack_create_structured_object_schema";

	private static final String MIME_TYPE_DOCUMENT_PLAINTEXT = "text/plain";
	private static final String MIME_TYPE_DOCUMENT_HTML = "text/html";
	public static final String MIME_TYPE_STRUCTOBJ = "application/x-osp";

	public static final String TYPE_FOLDER = "folder";
	public static final String TYPE_UPLOAD = "file";
	public static final String TYPE_URL = "Url";
	public static final String TYPE_FORM = MIME_TYPE_STRUCTOBJ;
	public static final String TYPE_HTML = MIME_TYPE_DOCUMENT_HTML;
	public static final String TYPE_TEXT = MIME_TYPE_DOCUMENT_PLAINTEXT;

	private static final int CREATE_MAX_ITEMS = 10;

	private static final int INTEGER_WIDGET_LENGTH = 12;
	private static final int DOUBLE_WIDGET_LENGTH = 18;

	private static final 	Pattern INDEXED_FORM_FIELD_PATTERN = Pattern.compile("(.+)\\.(\\d+)");

	/************** the metadata extension of edit/create contexts *****************************************/

	private static final String STATE_METADATA_GROUPS = "resources.metadata.types";

	private static final String INTENT_REVISE_FILE = "revise";
	private static final String INTENT_REPLACE_FILE = "replace";

	/** State attribute for where there is at least one attachment before invoking attachment tool */
	public static final String STATE_HAS_ATTACHMENT_BEFORE = "resources.has_attachment_before";

	/** The name of the state attribute containing a list of new items to be attached */
	private static final String STATE_HELPER_NEW_ITEMS = "resources.helper_new_items";

	/** The name of the state attribute indicating that the list of new items has changed */
	private static final String STATE_HELPER_CHANGED = "resources.helper_changed";


	/** The name of the optional state attribute indicating the id of the collection that should be treated as the "home" collection */
	public static final String STATE_ATTACH_COLLECTION_ID = "resources.attach_collection_id";

	/** The name of the state attribute containing the name of the tool that invoked Resources as attachment helper */
	public static final String STATE_ATTACH_TOOL_NAME = "resources.attach_tool_name";

	/** The name of the state attribute for "new-item" attachment indicating the type of item */
	public static final String STATE_ATTACH_TEXT = "resources.attach_text";

	/** The name of the state attribute for "new-item" attachment indicating the id of the item to edit */
	public static final String STATE_ATTACH_ITEM_ID = "resources.attach_collection_id";

	/** The name of the state attribute for "new-item" attachment indicating the id of the form-type if item-type 
	 * is TYPE_FORM (ignored otherwise) */
	public static final String STATE_ATTACH_FORM_ID = "resources.attach_form_id";

	/** The name of the state attribute indicating which form field a resource should be attached to */
	public static final String STATE_ATTACH_FORM_FIELD = "resources.attach_form_field";

	/************** the helper context (file-picker) *****************************************/

	/**
	 *  State attribute for the Vector of References, one for each attachment.
	 *  Using tools can pre-populate, and can read the results from here. 
	 */
	public static final String STATE_ATTACHMENTS = "resources.state_attachments";
	
	/**
	 *  The name of the state attribute indicating that the file picker should return links to
	 *  existing resources in an existing collection rather than copying it to the hidden attachments
	 *  area.  If this value is not set, all attachments are to copies in the hidden attachments area.
	 */
	public static final String STATE_ATTACH_LINKS = "resources.state_attach_links";

	/** 
	 * The name of the state attribute for the maximum number of items to attach. The attribute value will be an Integer, 
	 * usually CARDINALITY_SINGLE or CARDINALITY_MULTIPLE. 
	 */
	public static final String STATE_ATTACH_CARDINALITY = "resources.state_attach_cardinality";

	/** A constant indicating maximum of one item can be attached. */
	public static final Integer CARDINALITY_SINGLE = FilePickerHelper.CARDINALITY_SINGLE;

	/** A constant indicating any the number of attachments is unlimited. */
	public static final Integer CARDINALITY_MULTIPLE = FilePickerHelper.CARDINALITY_MULTIPLE;

	/**
	 *  The name of the state attribute for the title when a tool uses Resources as attachment helper (for create or attach but not for edit mode) 
	 */
	public static final String STATE_ATTACH_TITLE = "resources.state_attach_title_text";

	/** 
	 * The name of the state attribute for the instructions when a tool uses Resources as attachment helper 
	 * (for create or attach but not for edit mode) 
	 */
	public static final String STATE_ATTACH_INSTRUCTION = "resources.state_attach_instruction_text";

	/** 
	 * State Attribute for the org.sakaiproject.content.api.ContentResourceFilter
	 * object that the current filter should honor.  If this is set to null, then all files will
	 * be selectable and viewable 
	 */
	   public static final String STATE_ATTACH_FILTER = "resources.state_attach_filter";

	/**
	 * @deprecated use STATE_ATTACH_TITLE and STATE_ATTACH_INSTRUCTION instead
	 */
	public static final String STATE_FROM_TEXT = "attachment.from_text";

	/**
	 *  the name of the state attribute indicating that the user canceled out of the helper.  Is set only if the user canceled out of the helper. 
	 */
	public static final String STATE_HELPER_CANCELED_BY_USER = "resources.state_attach_canceled_by_user";
	
	/**
	 *  The name of the state attribute indicating that dropboxes should be shown as places from which
	 *  to select attachments. The value should be a List of user-id's.  The file picker will attempt to show 
	 *  the dropbox for each user whose id is included in the list. If this 
	 */
	public static final String STATE_ATTACH_SHOW_DROPBOXES = "resources.state_attach_show_dropboxes";

	/**
	 *  The name of the state attribute indicating that the current user's workspace Resources collection 
	 *  should be shown as places from which to select attachments. The value should be "true".  The file picker will attempt to show 
	 *  the workspace if this attribute is set to "true". 
	 */
	public static final String STATE_ATTACH_SHOW_WORKSPACE = "resources.state_attach_show_workspace";

	
	
	/************** the delete context *****************************************/

	/** The delete ids */
	private static final String STATE_DELETE_IDS = "resources.delete_ids";

	/** The not empty delete ids */
	private static final String STATE_NOT_EMPTY_DELETE_IDS = "resource.not_empty_delete_ids";

	/** The name of the state attribute containing a list of BrowseItem objects corresponding to resources selected for deletion */
	private static final String STATE_DELETE_ITEMS = "resources.delete_items";

	/** The name of the state attribute containing a list of BrowseItem objects corresponding to nonempty folders selected for deletion */
	private static final String STATE_DELETE_ITEMS_NOT_EMPTY = "resources.delete_items_not_empty";

	/** The name of the state attribute containing a list of BrowseItem objects selected for deletion that cannot be deleted */
	private static final String STATE_DELETE_ITEMS_CANNOT_DELETE = "resources.delete_items_cannot_delete";

	/************** the cut items context *****************************************/

	/** The cut item ids */
	private static final String STATE_CUT_IDS = "resources.revise_cut_ids";

	/************** the copied items context *****************************************/

	/** The copied item ids */
	private static final String STATE_COPIED_IDS = "resources.revise_copied_ids";

	/** The copied item id */
	private static final String STATE_COPIED_ID = "resources.revise_copied_id";

	/************** the moved items context *****************************************/

	/** The copied item ids */
	private static final String STATE_MOVED_IDS = "resources.revise_moved_ids";

	/** Modes. */
	private static final String MODE_LIST = "list";
	private static final String MODE_EDIT = "edit";
	private static final String MODE_DAV = "webdav";
	private static final String MODE_CREATE = "create";
	public  static final String MODE_HELPER = "helper";
	private static final String MODE_DELETE_CONFIRM = "deleteConfirm";
	private static final String MODE_MORE = "more";
	private static final String MODE_PROPERTIES = "properties";

	/** modes for attachment helper */
	public static final String MODE_ATTACHMENT_SELECT = "resources.attachment_select";
	public static final String MODE_ATTACHMENT_CREATE = "resources.attachment_create";
	public static final String MODE_ATTACHMENT_NEW_ITEM = "resources.attachment_new_item";
	public static final String MODE_ATTACHMENT_EDIT_ITEM = "resources.attachment_edit_item";
	public static final String MODE_ATTACHMENT_CONFIRM = "resources.attachment_confirm";
	public static final String MODE_ATTACHMENT_SELECT_INIT = "resources.attachment_select_initialized";
	public static final String MODE_ATTACHMENT_CREATE_INIT = "resources.attachment_create_initialized";
	public static final String MODE_ATTACHMENT_NEW_ITEM_INIT = "resources.attachment_new_item_initialized";
	public static final String MODE_ATTACHMENT_EDIT_ITEM_INIT = "resources.attachment_edit_item_initialized";
	public static final String MODE_ATTACHMENT_CONFIRM_INIT = "resources.attachment_confirm_initialized";
	public static final String MODE_ATTACHMENT_DONE = "resources.attachment_done";

	/** vm files for each mode. */
	private static final String TEMPLATE_LIST = "content/chef_resources_list";
	private static final String TEMPLATE_EDIT = "content/chef_resources_edit";
	private static final String TEMPLATE_CREATE = "content/chef_resources_create";
	private static final String TEMPLATE_DAV = "content/chef_resources_webdav";
	private static final String TEMPLATE_ITEMTYPE = "content/chef_resources_itemtype";
	private static final String TEMPLATE_SELECT = "content/chef_resources_select";
	private static final String TEMPLATE_ATTACH = "content/chef_resources_attach";

	private static final String TEMPLATE_MORE = "content/chef_resources_more";
	private static final String TEMPLATE_DELETE_CONFIRM = "content/chef_resources_deleteConfirm";
	private static final String TEMPLATE_PROPERTIES = "content/chef_resources_properties";
	// private static final String TEMPLATE_REPLACE = "_replace";

	/** the site title */
	private static final String STATE_SITE_TITLE = "site_title";

	/** copyright related info */
	private static final String COPYRIGHT_TYPES = "copyright_types";
	private static final String COPYRIGHT_TYPE = "copyright_type";
	private static final String DEFAULT_COPYRIGHT = "default_copyright";
	private static final String COPYRIGHT_ALERT = "copyright_alert";
	private static final String DEFAULT_COPYRIGHT_ALERT = "default_copyright_alert";
	private static final String COPYRIGHT_FAIRUSE_URL = "copyright_fairuse_url";
	private static final String NEW_COPYRIGHT_INPUT = "new_copyright_input";
	private static final String COPYRIGHT_SELF_COPYRIGHT = rb.getString("cpright2");
	private static final String COPYRIGHT_NEW_COPYRIGHT = rb.getString("cpright3");
	private static final String COPYRIGHT_ALERT_URL = ServerConfigurationService.getAccessUrl() + COPYRIGHT_PATH;
	
	/** state attribute indicating whether we're using the Creative Commons dialog instead of the "old" copyright dialog */
	protected static final String STATE_USING_CREATIVE_COMMONS = "resources.usingCreativeCommons";

	private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;

	/** The default value for whether to show all sites in file-picker (used if global value can't be read from server config service) */
	public static final boolean SHOW_ALL_SITES_IN_FILE_PICKER = false;

	/** The default value for whether to show all sites in resources tool (used if global value can't be read from server config service) */
	private static final boolean SHOW_ALL_SITES_IN_RESOURCES = false;

	/** The default value for whether to show all sites in dropbox (used if global value can't be read from server config service) */
	private static final boolean SHOW_ALL_SITES_IN_DROPBOX = false;

	/** The number of members for a collection at which this tool should refuse to expand the collection */
	protected static final int EXPANDABLE_FOLDER_SIZE_LIMIT = 256;

	protected static final String STATE_SHOW_REMOVE_ACTION = "resources.show_remove_action";

	protected static final String STATE_SHOW_MOVE_ACTION = "resources.show_move_action";

	protected static final String STATE_SHOW_COPY_ACTION = "resources.show_copy_action";

	protected static final String STATE_HIGHLIGHTED_ITEMS = "resources.highlighted_items";

	/** The default number of site collections per page. */
	protected static final int DEFAULT_PAGE_SIZE = 50;

	protected static final String PARAM_PAGESIZE = "collections_per_page";

	protected static final String STATE_TOP_MESSAGE_INDEX = "resources.top_message_index";

	protected static final String STATE_REMOVED_ATTACHMENTS = "resources.removed_attachments";


	/********* Global constants *********/

	/** The null/empty string */
	private static final String NULL_STRING = "";

	/** The string used when pasting the same resource to the same folder */
	private static final String DUPLICATE_STRING = rb.getString("copyof") + " ";

	/** The string used when pasting shirtcut of the same resource to the same folder */
	private static final String SHORTCUT_STRING = rb.getString("shortcut");

	/** The copyright character (Note: could be "\u00a9" if we supported UNICODE for specials -ggolden */
	private static final String COPYRIGHT_SYMBOL = rb.getString("cpright1");

	/** The String of new copyright */
	private static final String NEW_COPYRIGHT = "newcopyright";

	/** The resource not exist string */
	private static final String RESOURCE_NOT_EXIST_STRING = rb.getString("notexist1");

	/** The title invalid string */
	private static final String RESOURCE_INVALID_TITLE_STRING = rb.getString("titlecannot");

	/** The copy, cut, paste not operate on collection string */
	private static final String RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING = rb.getString("notsupported");

	/** The maximum number of suspended operations that can be on the stack. */
	private static final int MAXIMUM_SUSPENDED_OPERATIONS_STACK_DEPTH = 10;

	/** portlet configuration parameter values**/
	public static final String RESOURCES_MODE_RESOURCES = "resources";
	public static final String RESOURCES_MODE_DROPBOX = "dropbox";
	public static final String RESOURCES_MODE_HELPER = "helper";

	/** The from state name */
	private static final String STATE_FROM = "resources.from";

	private static final String STATE_ENCODING = "resources.encoding";

	private static final String DELIM = "@";

	/**
	* Build the context for normal display
	*/
	public String buildMainPanelContext (	VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		context.put("tlang",rb);
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("copyright_alert_url", COPYRIGHT_ALERT_URL);

		String template = null;

		// place if notification is enabled and current site is not of My Workspace type
		boolean isUserSite = SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
		context.put("notification", new Boolean(!isUserSite && notificationEnabled(state)));
		// get the mode
		String mode = (String) state.getAttribute (STATE_MODE);
		String helper_mode = (String) state.getAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE);
		if (!MODE_HELPER.equals(mode) && helper_mode != null)
		{
			// not in helper mode, but a helper context is needed

			// if the mode is not done, defer to the helper context
			if (!mode.equals(ResourcesAction.MODE_ATTACHMENT_DONE))
			{
				template = ResourcesAction.buildHelperContext(portlet, context, data, state);
				// template = AttachmentAction.buildHelperContext(portlet, context, runData, sstate);
				return template;
			}

			// clean up
			state.removeAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE);
			state.removeAttribute(ResourcesAction.STATE_ATTACHMENTS);
		}

		if (mode.equals (MODE_LIST))
		{
			// build the context for add item
			template = buildListContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_HELPER))
		{
			// build the context for add item
			template = buildHelperContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_CREATE))
		{
			// build the context for add item
			template = buildCreateContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_DELETE_CONFIRM))
		{
			// build the context for the basic step of delete confirm page
			template = buildDeleteConfirmContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_MORE))
		{
			// build the context to display the property list
			template = buildMoreContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_EDIT))
		{
			// build the context to display the property list
			template = buildEditContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_OPTIONS))
		{
			template = buildOptionsPanelContext (portlet, context, data, state);
		}
		else if(mode.equals(MODE_DAV))
		{
			template = buildWebdavContext (portlet, context, data, state);
		}

		return template;

	}	// buildMainPanelContext

	/**
	* Build the context for the list view
	*/
	public String buildListContext (	VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{
		context.put("tlang",rb);

		context.put("expandedCollections", state.getAttribute(STATE_EXPANDED_COLLECTIONS));

		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("TYPE_FOLDER", TYPE_FOLDER);
		context.put("TYPE_UPLOAD", TYPE_UPLOAD);

		context.put("SITE_ACCESS", AccessMode.SITE.toString());
		context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
		context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());

		Set selectedItems = (Set) state.getAttribute(STATE_LIST_SELECTIONS);
		if(selectedItems == null)
		{
			selectedItems = new TreeSet();
			state.setAttribute(STATE_LIST_SELECTIONS, selectedItems);
		}
		context.put("selectedItems", selectedItems);

		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		context.put ("service", contentService);

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
		if(!inMyWorkspace && !dropboxMode && atHome && SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
		{
			context.put("showPermissions", Boolean.TRUE.toString());
			//buildListMenu(portlet, context, data, state);
		}

		context.put("atHome", Boolean.toString(atHome));

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
			if (copyFlag.equals (Boolean.TRUE.toString()))
			{
				context.put ("copyFlag", copyFlag);
				List copiedItems = (List) state.getAttribute(STATE_COPIED_IDS);
				// context.put ("copiedItem", state.getAttribute (STATE_COPIED_ID));
				highlightedItems.addAll(copiedItems);
				// context.put("copiedItems", copiedItems);
			}

			String moveFlag = (String) state.getAttribute (STATE_MOVE_FLAG);
			if (moveFlag.equals (Boolean.TRUE.toString()))
			{
				context.put ("moveFlag", moveFlag);
				List movedItems = (List) state.getAttribute(STATE_MOVED_IDS);
				highlightedItems.addAll(movedItems);
				// context.put ("copiedItem", state.getAttribute (STATE_COPIED_ID));
				// context.put("movedItems", movedItems);
			}

			HashMap expandedCollections = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			
			ContentCollection coll = contentService.getCollection(collectionId);
			expandedCollections.put(collectionId, coll);

			state.removeAttribute(STATE_PASTE_ALLOWED_FLAG);

			List all_roots = new Vector();
			List this_site = new Vector();
			List members = getBrowseItems(collectionId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (BrowseItem) null, navRoot.equals(homeCollectionId), state);
			if(members != null && members.size() > 0)
			{
				BrowseItem root = (BrowseItem) members.remove(0);
				showRemoveAction = showRemoveAction || root.hasDeletableChildren();
				showMoveAction = showMoveAction || root.hasDeletableChildren();
				showCopyAction = showCopyAction || root.hasCopyableChildren();

				if(atHome && dropboxMode)
				{
					root.setName(siteTitle + " " + rb.getString("gen.drop"));
				}
				else if(atHome)
				{
					root.setName(siteTitle + " " + rb.getString("gen.reso"));
				}
				context.put("site", root);
				root.addMembers(members);
				this_site.add(root);
				all_roots.add(root);
			}
			context.put ("this_site", this_site);

			boolean show_all_sites = false;
			List other_sites = new Vector();

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
				other_sites.addAll(readAllResources(state));
				all_roots.addAll(other_sites);

				List messages = prepPage(state);
				context.put("other_sites", messages);

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

			// context.put ("other_sites", other_sites);
			state.setAttribute(STATE_COLLECTION_ROOTS, all_roots);
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
			// logger.warn(this + e.toString());
		}

		context.put("expandallflag", state.getAttribute(STATE_EXPAND_ALL_FLAG));
		state.removeAttribute(STATE_NEED_TO_EXPAND_ALL);

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(state);

		// pick the "show" template based on the standard template name
		// String template = (String) getContext(data).get("template");

		return TEMPLATE_LIST;

	}	// buildListContext

	/**
	* Build the context for the helper view
	*/
	public static String buildHelperContext (	VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{
		if(state.getAttribute(STATE_INITIALIZED) == null)
		{
			initStateAttributes(state, portlet);
			if(state.getAttribute(ResourcesAction.STATE_HELPER_CANCELED_BY_USER) != null)
			{
				state.removeAttribute(ResourcesAction.STATE_HELPER_CANCELED_BY_USER);
			}
		}
		String mode = (String) state.getAttribute(STATE_MODE);
		if(state.getAttribute(STATE_MODE_RESOURCES) == null && MODE_HELPER.equals(mode))
		{
			state.setAttribute(ResourcesAction.STATE_MODE_RESOURCES, ResourcesAction.MODE_HELPER);
		}

		Set selectedItems = (Set) state.getAttribute(STATE_LIST_SELECTIONS);
		if(selectedItems == null)
		{
			selectedItems = new TreeSet();
			state.setAttribute(STATE_LIST_SELECTIONS, selectedItems);
		}
		context.put("selectedItems", selectedItems);

		String helper_mode = (String) state.getAttribute(STATE_RESOURCES_HELPER_MODE);
		boolean need_to_push = false;

		if(MODE_ATTACHMENT_SELECT.equals(helper_mode))
		{
			need_to_push = true;
			helper_mode = MODE_ATTACHMENT_SELECT_INIT;
		}
		else if(MODE_ATTACHMENT_CREATE.equals(helper_mode))
		{
			need_to_push = true;
			helper_mode = MODE_ATTACHMENT_CREATE_INIT;
		}
		else if(MODE_ATTACHMENT_NEW_ITEM.equals(helper_mode))
		{
			need_to_push = true;
			helper_mode = MODE_ATTACHMENT_NEW_ITEM_INIT;
		}
		else if(MODE_ATTACHMENT_EDIT_ITEM.equals(helper_mode))
		{
			need_to_push = true;
			helper_mode = MODE_ATTACHMENT_EDIT_ITEM_INIT;
		}

		Map current_stack_frame = null;

		if(need_to_push)
		{
			current_stack_frame = pushOnStack(state);
			current_stack_frame.put(STATE_STACK_EDIT_INTENT, INTENT_REVISE_FILE);

			state.setAttribute(VelocityPortletPaneledAction.STATE_HELPER, ResourcesAction.class.getName());
			state.setAttribute(STATE_RESOURCES_HELPER_MODE, helper_mode);

			if(MODE_ATTACHMENT_EDIT_ITEM_INIT.equals(helper_mode))
			{
				String attachmentId = (String) state.getAttribute(STATE_EDIT_ID);
				if(attachmentId != null)
				{
					current_stack_frame.put(STATE_STACK_EDIT_ID, attachmentId);
					String collectionId = ContentHostingService.getContainingCollectionId(attachmentId);
					current_stack_frame.put(STATE_STACK_EDIT_COLLECTION_ID, collectionId);

					EditItem item = getEditItem(attachmentId, collectionId, data);

					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						// got resource and sucessfully populated item with values
						state.setAttribute(STATE_EDIT_ALERTS, new HashSet());
						current_stack_frame.put(STATE_STACK_EDIT_ITEM, item);
					}
				}
			}
			else
			{
				List attachments = (List) state.getAttribute(STATE_ATTACHMENTS);
				if(attachments == null)
				{
					attachments = EntityManager.newReferenceList();
				}

				List attached = new Vector();

				Iterator it = attachments.iterator();
				while(it.hasNext())
				{
					try
					{
						Reference ref = (Reference) it.next();
						String itemId = ref.getId();
						ResourceProperties properties = ref.getProperties();
						String displayName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						String containerId = ref.getContainer();
						String accessUrl = ContentHostingService.getUrl(itemId);
						String contentType = properties.getProperty(ResourceProperties.PROP_CONTENT_TYPE);

						AttachItem item = new AttachItem(itemId, displayName, containerId, accessUrl);
						item.setContentType(contentType);
						attached.add(item);
					}
					catch(Exception ignore) {}
				}
				current_stack_frame.put(STATE_HELPER_NEW_ITEMS, attached);
			}
		}
		else
		{
			current_stack_frame = peekAtStack(state);
			if(current_stack_frame.get(STATE_STACK_EDIT_INTENT) == null)
			{
				current_stack_frame.put(STATE_STACK_EDIT_INTENT, INTENT_REVISE_FILE);
			}
		}
		if(helper_mode == null)
		{
			helper_mode = (String) current_stack_frame.get(STATE_RESOURCES_HELPER_MODE);
		}
		else
		{
			current_stack_frame.put(STATE_RESOURCES_HELPER_MODE, helper_mode);
		}

		String helper_title = (String) current_stack_frame.get(STATE_ATTACH_TITLE);
		if(helper_title == null)
		{
			helper_title = (String) state.getAttribute(STATE_ATTACH_TITLE);
			if(helper_title != null)
			{
				current_stack_frame.put(STATE_ATTACH_TITLE, helper_title);
			}
		}
		if(helper_title != null)
		{
			context.put("helper_title", helper_title);
		}

		String helper_instruction = (String) current_stack_frame.get(STATE_ATTACH_INSTRUCTION);
		if(helper_instruction == null)
		{
			helper_instruction = (String) state.getAttribute(STATE_ATTACH_INSTRUCTION);
			if(helper_instruction != null)
			{
				current_stack_frame.put(STATE_ATTACH_INSTRUCTION, helper_instruction);
			}
		}
		if(helper_instruction != null)
		{
			context.put("helper_instruction", helper_instruction);
		}

		String title = (String) current_stack_frame.get(STATE_STACK_EDIT_ITEM_TITLE);
		if(title == null)
		{
			title = (String) state.getAttribute(STATE_ATTACH_TEXT);
			if(title != null)
			{
				current_stack_frame.put(STATE_STACK_EDIT_ITEM_TITLE, title);
			}
		}
		if(title != null && title.trim().length() > 0)
		{
			context.put("helper_subtitle", title);
		}

		String template = null;
		if(MODE_ATTACHMENT_SELECT_INIT.equals(helper_mode))
		{
			template = buildSelectAttachmentContext(portlet, context, data, state);
		}
		else if(MODE_ATTACHMENT_CREATE_INIT.equals(helper_mode))
		{
			template = buildCreateContext(portlet, context, data, state);
		}
		else if(MODE_ATTACHMENT_NEW_ITEM_INIT.equals(helper_mode))
		{
			template = buildItemTypeContext(portlet, context, data, state);
		}
		else if(MODE_ATTACHMENT_EDIT_ITEM_INIT.equals(helper_mode))
		{
			template = buildEditContext(portlet, context, data, state);
		}
		return template;
	}

	public static String buildItemTypeContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		context.put("tlang",rb);

		initStateAttributes(state, portlet);
		Map current_stack_frame = peekAtStack(state);

		String mode = (String) state.getAttribute(STATE_MODE);
		if(mode == null || mode.trim().length() == 0)
		{
			mode = MODE_HELPER;
			state.setAttribute(STATE_MODE, mode);
		}
		String helper_mode = null;
		if(MODE_HELPER.equals(mode))
		{
			helper_mode = (String) state.getAttribute(STATE_RESOURCES_HELPER_MODE);
			if(helper_mode == null || helper_mode.trim().length() == 0)
			{
				helper_mode = MODE_ATTACHMENT_NEW_ITEM;
				state.setAttribute(STATE_RESOURCES_HELPER_MODE, helper_mode);
			}
			current_stack_frame.put(STATE_RESOURCES_HELPER_MODE, helper_mode);
			if(MODE_ATTACHMENT_NEW_ITEM_INIT.equals(helper_mode))
			{
				context.put("attaching_this_item", Boolean.TRUE.toString());
			}
			state.setAttribute(VelocityPortletPaneledAction.STATE_HELPER, ResourcesAction.class.getName());
		}
		
		String msg = (String) state.getAttribute(STATE_CREATE_MESSAGE);
		if (msg != null)
		{
			context.put("itemAlertMessage", msg);
			state.removeAttribute(STATE_CREATE_MESSAGE);
		}

		context.put("max_upload_size", state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE));

		String collectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
		if(collectionId == null || collectionId.trim().length() == 0)
		{
			collectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			if(collectionId == null || collectionId.trim().length() == 0)
			{
				collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			}
			current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);
		}
		context.put("collectionId", collectionId);
		
		String itemType = (String) current_stack_frame.get(STATE_STACK_CREATE_TYPE);
		if(itemType == null || "".equals(itemType))
		{
			itemType = (String) state.getAttribute(STATE_CREATE_TYPE);
			if(itemType == null || "".equals(itemType))
			{
				itemType = TYPE_UPLOAD;
			}
			current_stack_frame.put(STATE_STACK_CREATE_TYPE, itemType);
		}

		context.put("itemType", itemType);

		Integer numberOfItems = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(numberOfItems == null)
		{
			numberOfItems = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, numberOfItems);
		}
		if(numberOfItems == null)
		{
			numberOfItems = new Integer(1);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, numberOfItems);
		}
		context.put("numberOfItems", numberOfItems);
		context.put("max_number", new Integer(1));
		
		Collection groups = ContentHostingService.getGroupsWithReadAccess(collectionId);
		// TODO: does this method filter groups for this subcollection??
		if(! groups.isEmpty())
		{
			context.put("siteHasGroups", Boolean.TRUE.toString());
		}

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		if(new_items == null)
		{
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			Site site;
			try {
				site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			} catch (IdUnusedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String encoding = data.getRequest().getCharacterEncoding();
			List inherited_access_groups = new Vector();
			
			AccessMode inherited_access = AccessMode.INHERITED;
			try 
			{
				ContentCollection parent = ContentHostingService.getCollection(collectionId);
				inherited_access = parent.getInheritedAccess();
				inherited_access_groups.addAll(parent.getInheritedGroups());
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
			
			new_items = new Vector();
			List theGroupsInThisSite = new Vector();
			for(int i = 0; i < CREATE_MAX_ITEMS; i++)
			{
				EditItem item = new EditItem(itemType);
				if(encoding != null)
				{
					item.setEncoding(encoding);
				}
				item.setCopyrightStatus(defaultCopyrightStatus);
				new_items.add(item);
				theGroupsInThisSite.add(new Vector(groups));
				if(inherited_access_groups != null)
				{
					item.setInheritedGroups(inherited_access_groups);
				}
				if(inherited_access == null || inherited_access.equals(AccessMode.SITE))
				{
					item.setInheritedAccess(AccessMode.INHERITED.toString());
				}
				else
				{
					item.setInheritedAccess(inherited_access.toString());
				}
				
			}
			context.put("theGroupsInThisSite", theGroupsInThisSite);

		}
		context.put("new_items", new_items);
		current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);
		
		String show_form_items = (String) current_stack_frame.get(STATE_SHOW_FORM_ITEMS);
		if(show_form_items == null)
		{
			show_form_items = (String) state.getAttribute(STATE_SHOW_FORM_ITEMS);
			if(show_form_items != null)
			{
				current_stack_frame.put(STATE_SHOW_FORM_ITEMS,show_form_items);
			}
		}
		if(show_form_items != null)
		{
			context.put("show_form_items", show_form_items);
		}

		context.put("TYPE_FOLDER", TYPE_FOLDER);
		context.put("TYPE_UPLOAD", TYPE_UPLOAD);
		context.put("TYPE_HTML", TYPE_HTML);
		context.put("TYPE_TEXT", TYPE_TEXT);
		context.put("TYPE_URL", TYPE_URL);
		context.put("TYPE_FORM", TYPE_FORM);

		// copyright
		copyrightChoicesIntoContext(state, context);

		// put schema for metadata into context
		metadataGroupsIntoContext(state, context);

		if(TYPE_FORM.equals(itemType))
		{
			List listOfHomes = (List) current_stack_frame.get(STATE_STRUCTOBJ_HOMES);
			if(listOfHomes == null)
			{
				setupStructuredObjects(state);
				listOfHomes = (List) current_stack_frame.get(STATE_STRUCTOBJ_HOMES);
			}
			context.put("homes", listOfHomes);

			String formtype = (String) current_stack_frame.get(STATE_STACK_STRUCTOBJ_TYPE);
			if(formtype == null)
			{
				formtype = (String) state.getAttribute(STATE_STRUCTOBJ_TYPE);
				if(formtype == null)
				{
					formtype = "";
				}
				current_stack_frame.put(STATE_STACK_STRUCTOBJ_TYPE, formtype);
			}
			context.put("formtype", formtype);

			String formtype_readonly = (String) current_stack_frame.get(STATE_STACK_STRUCTOBJ_TYPE_READONLY);
			if(formtype_readonly == null)
			{
				formtype_readonly = (String) state.getAttribute(STATE_STRUCTOBJ_TYPE_READONLY);
				if(formtype_readonly == null)
				{
					formtype_readonly = Boolean.FALSE.toString();
				}
				current_stack_frame.put(STATE_STACK_STRUCTOBJ_TYPE_READONLY, formtype_readonly);
			}
			if(formtype_readonly != null && formtype_readonly.equals(Boolean.TRUE.toString()))
			{
				context.put("formtype_readonly", formtype_readonly);
			}

			String rootname = (String) current_stack_frame.get(STATE_STACK_STRUCTOBJ_ROOTNAME);
			context.put("rootname", rootname);

			context.put("STRING", ResourcesMetadata.WIDGET_STRING);
			context.put("TEXTAREA", ResourcesMetadata.WIDGET_TEXTAREA);
			context.put("BOOLEAN", ResourcesMetadata.WIDGET_BOOLEAN);
			context.put("INTEGER", ResourcesMetadata.WIDGET_INTEGER);
			context.put("DOUBLE", ResourcesMetadata.WIDGET_DOUBLE);
			context.put("DATE", ResourcesMetadata.WIDGET_DATE);
			context.put("TIME", ResourcesMetadata.WIDGET_TIME);
			context.put("DATETIME", ResourcesMetadata.WIDGET_DATETIME);
			context.put("ANYURI", ResourcesMetadata.WIDGET_ANYURI);
			context.put("ENUM", ResourcesMetadata.WIDGET_ENUM);
			context.put("NESTED", ResourcesMetadata.WIDGET_NESTED);
			context.put("WYSIWYG", ResourcesMetadata.WIDGET_WYSIWYG);

			context.put("today", TimeService.newTime());

			context.put("DOT", ResourcesMetadata.DOT);
		}

		return TEMPLATE_ITEMTYPE;
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
			current_stack_frame = (Map) operations_stack.push(new Hashtable());
		}
		Object helper_mode = state.getAttribute(STATE_RESOURCES_HELPER_MODE);
		if(helper_mode != null)
		{
			current_stack_frame.put(STATE_RESOURCES_HELPER_MODE, helper_mode);
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

	private static void resetCurrentMode(SessionState state)
	{
		String mode = (String) state.getAttribute(STATE_MODE);
		if(isStackEmpty(state))
		{
			if(MODE_HELPER.equals(mode))
			{
				cleanupState(state);
				state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_DONE);
			}
			else
			{
				state.setAttribute(STATE_MODE, MODE_LIST);
				state.removeAttribute(STATE_RESOURCES_HELPER_MODE);
			}
			return;
		}
		Map current_stack_frame = peekAtStack(state);
		String helper_mode = (String) current_stack_frame.get(STATE_RESOURCES_HELPER_MODE);
		if(helper_mode != null)
		{
			state.setAttribute(STATE_RESOURCES_HELPER_MODE, helper_mode);
		}

	}

	/**
	* Build the context for selecting attachments
	*/
	public static String buildSelectAttachmentContext (	VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{
		context.put("tlang",rb);

		initStateAttributes(state, portlet);

		Map current_stack_frame = peekAtStack(state);
		if(current_stack_frame == null)
		{
			current_stack_frame = pushOnStack(state);
		}

		state.setAttribute(VelocityPortletPaneledAction.STATE_HELPER, ResourcesAction.class.getName());

		Set highlightedItems = new TreeSet();

		List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
		if(new_items == null)
		{
			new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
			}
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}
		context.put("attached", new_items);
		context.put("last", new Integer(new_items.size() - 1));

		Integer max_cardinality = (Integer) current_stack_frame.get(STATE_ATTACH_CARDINALITY);
		if(max_cardinality == null)
		{
			max_cardinality = (Integer) state.getAttribute(STATE_ATTACH_CARDINALITY);
			if(max_cardinality == null)
			{
				max_cardinality = CARDINALITY_MULTIPLE;
			}
			current_stack_frame.put(STATE_ATTACH_CARDINALITY, max_cardinality);
		}
		context.put("max_cardinality", max_cardinality);

		if(new_items.size() >= max_cardinality.intValue())
		{
			context.put("disable_attach_links", Boolean.TRUE.toString());
		}

		if(state.getAttribute(STATE_HELPER_CHANGED) != null)
		{
			context.put("list_has_changed", "true");
		}

		String form_field = (String) current_stack_frame.get(ResourcesAction.STATE_ATTACH_FORM_FIELD);
		if(form_field == null)
		{
			form_field = (String) state.getAttribute(ResourcesAction.STATE_ATTACH_FORM_FIELD);
			if(form_field != null)
			{
				current_stack_frame.put(ResourcesAction.STATE_ATTACH_FORM_FIELD, form_field);
				state.removeAttribute(ResourcesAction.STATE_ATTACH_FORM_FIELD);
			}
		}

		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("TYPE_FOLDER", TYPE_FOLDER);
		context.put("TYPE_UPLOAD", TYPE_UPLOAD);

		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		// context.put ("service", contentService);

		boolean inMyWorkspace = SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
		// context.put("inMyWorkspace", Boolean.toString(inMyWorkspace));

		boolean atHome = false;

		// %%STATE_MODE_RESOURCES%%
		boolean dropboxMode = RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES));

		// make sure the channedId is set
		String collectionId = (String) state.getAttribute(STATE_ATTACH_COLLECTION_ID);
		if(collectionId == null)
		{
			collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		}

		context.put ("collectionId", collectionId);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

		String siteTitle = (String) state.getAttribute (STATE_SITE_TITLE);
		if (collectionId.equals(homeCollectionId))
		{
			atHome = true;
			//context.put ("collectionDisplayName", state.getAttribute (STATE_HOME_COLLECTION_DISPLAY_NAME));
		}
		else
		{
			/*
			// should be not PermissionException thrown at this time, when the user can successfully navigate to this collection
			try
			{
				context.put("collectionDisplayName", contentService.getCollection(collectionId).getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			}
			catch (IdUnusedException e){}
			catch (TypeException e) {}
			catch (PermissionException e) {}
			*/
		}

		List cPath = getCollectionPath(state);
		context.put ("collectionPath", cPath);

		// set the sort values
		String sortedBy = (String) state.getAttribute (STATE_SORT_BY);
		String sortedAsc = (String) state.getAttribute (STATE_SORT_ASC);
		context.put ("currentSortedBy", sortedBy);
		context.put ("currentSortAsc", sortedAsc);
		context.put("TRUE", Boolean.TRUE.toString());

		// String current_user_id = UserDirectoryService.getCurrentUser().getId();

		try
		{
			try
			{
				contentService.checkCollection (collectionId);
				context.put ("collectionFlag", Boolean.TRUE.toString());
			}
			catch(IdUnusedException ex)
			{
				logger.warn("ResourcesAction.buildSelectAttachment (static) : IdUnusedException: " + collectionId);
				try
				{
					ContentCollectionEdit coll = contentService.addCollection(collectionId);
					contentService.commitCollection(coll);
				}
				catch(IdUsedException inner)
				{
					// how can this happen??
					logger.warn("ResourcesAction.buildSelectAttachment (static) : IdUsedException: " + collectionId);
					throw ex;
				}
				catch(IdInvalidException inner)
				{
					logger.warn("ResourcesAction.buildSelectAttachment (static) : IdInvalidException: " + collectionId);
					// what now?
					throw ex;
				}
				catch(InconsistentException inner)
				{
					logger.warn("ResourcesAction.buildSelectAttachment (static) : InconsistentException: " + collectionId);
					// what now?
					throw ex;
				}
			}
			catch(TypeException ex)
			{
				logger.warn("ResourcesAction.buildSelectAttachment (static) : TypeException.");
				throw ex;				
			}
			catch(PermissionException ex)
			{
				logger.warn("ResourcesAction.buildSelectAttachment (static) : PermissionException.");
				throw ex;
			}
		
			HashMap expandedCollections = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			ContentCollection coll = contentService.getCollection(collectionId);
			expandedCollections.put(collectionId, coll);

			state.removeAttribute(STATE_PASTE_ALLOWED_FLAG);

			List this_site = new Vector();
			User[] submitters = (User[]) state.getAttribute(STATE_ATTACH_SHOW_DROPBOXES);
			if(submitters != null)
			{
				String dropboxId = ContentHostingService.getDropboxCollection();
				if(dropboxId == null)
				{
					ContentHostingService.createDropboxCollection();
					dropboxId = ContentHostingService.getDropboxCollection();
				}

				if(dropboxId == null)
				{
					// do nothing
				}
				else if(ContentHostingService.isDropboxMaintainer())
				{
					for(int i = 0; i < submitters.length; i++)
					{
						User submitter = submitters[i];
						String dbId = dropboxId + StringUtil.trimToZero(submitter.getId()) + "/";
						try
						{
							ContentCollection db = ContentHostingService.getCollection(dbId);
							expandedCollections.put(dbId, db);
							List dbox = getBrowseItems(dbId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (BrowseItem) null, false, state);
							if(dbox != null && dbox.size() > 0)
							{
								BrowseItem root = (BrowseItem) dbox.remove(0);
								// context.put("site", root);
								root.setName(submitter.getDisplayName() + " " + rb.getString("gen.drop"));
								root.addMembers(dbox);
								this_site.add(root);
							}
						}
						catch(IdUnusedException e)
						{
							// ignore a user's dropbox if it's not defined
						}
					}
				}
				else
				{
					try
					{
						ContentCollection db = ContentHostingService.getCollection(dropboxId);
						expandedCollections.put(dropboxId, db);
						List dbox = getBrowseItems(dropboxId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (BrowseItem) null, false, state);
						if(dbox != null && dbox.size() > 0)
						{
							BrowseItem root = (BrowseItem) dbox.remove(0);
							// context.put("site", root);
							root.setName(ContentHostingService.getDropboxDisplayName());
							root.addMembers(dbox);
							this_site.add(root);
						}
					}
					catch(IdUnusedException e)
					{
						// if an id is unused, ignore it
					}
				}
			}
			List members = getBrowseItems(collectionId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (BrowseItem) null, navRoot.equals(homeCollectionId), state);
			if(members != null && members.size() > 0)
			{
				BrowseItem root = (BrowseItem) members.remove(0);
				if(atHome && dropboxMode)
				{
					root.setName(siteTitle + " " + rb.getString("gen.drop"));
				}
				else if(atHome)
				{
					root.setName(siteTitle + " " + rb.getString("gen.reso"));
				}
				context.put("site", root);
				root.addMembers(members);
				this_site.add(root);
			}


			context.put ("this_site", this_site);

			List other_sites = new Vector();
			boolean show_all_sites = false;

			String allowed_to_see_other_sites = (String) state.getAttribute(STATE_SHOW_ALL_SITES);
			String show_other_sites = (String) state.getAttribute(STATE_SHOW_OTHER_SITES);
			context.put("show_other_sites", show_other_sites);
			if(Boolean.TRUE.toString().equals(allowed_to_see_other_sites))
			{
				context.put("allowed_to_see_other_sites", Boolean.TRUE.toString());
				show_all_sites = Boolean.TRUE.toString().equals(show_other_sites);
			}

			if(show_all_sites)
			{

				state.setAttribute(STATE_HIGHLIGHTED_ITEMS, highlightedItems);
				other_sites.addAll(readAllResources(state));

				List messages = prepPage(state);
				context.put("other_sites", messages);

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




				// List other_sites = new Vector();
				/*
				 * NOTE: This does not (and should not) get all sites for admin.
				 *       Getting all sites for admin is too big a request and
				 *       would result in too big a display to render in html.
				 */
				/*
				Map othersites = ContentHostingService.getCollectionMap();
				Iterator siteIt = othersites.keySet().iterator();
				while(siteIt.hasNext())
				{
					String displayName = (String) siteIt.next();
					String collId = (String) othersites.get(displayName);
					if(! collectionId.equals(collId))
					{
						members = getBrowseItems(collId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (BrowseItem) null, false, state);
						if(members != null && members.size() > 0)
						{
							BrowseItem root = (BrowseItem) members.remove(0);
							root.addMembers(members);
							root.setName(displayName);
							other_sites.add(root);
						}
					}
				}

				context.put ("other_sites", other_sites);
				*/
			}

			// context.put ("root", root);
			context.put("expandedCollections", expandedCollections);
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfind"));
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		catch(TypeException e)
		{
			// logger.warn(this + "TypeException.");
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
			// logger.warn(this + e.toString());
		}

		context.put("expandallflag", state.getAttribute(STATE_EXPAND_ALL_FLAG));
		state.removeAttribute(STATE_NEED_TO_EXPAND_ALL);

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		// justDelivered(state);

		// pick the template based on whether client wants links or copies
		String template = TEMPLATE_SELECT;
		Object attach_links = current_stack_frame.get(STATE_ATTACH_LINKS);
		if(attach_links == null)
		{
			attach_links = state.getAttribute(STATE_ATTACH_LINKS);
			if(attach_links != null)
			{
				current_stack_frame.put(STATE_ATTACH_LINKS, attach_links);
			}
		}
		if(attach_links == null)
		{
			// user wants copies in hidden attachments area
			template = TEMPLATE_ATTACH;
		}

		return template;

	}	// buildSelectAttachmentContext

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

		context.put("server_url", ServerConfigurationService.getServerUrl());
		context.put("site_id", ToolManager.getCurrentPlacement().getContext());
		context.put("site_title", state.getAttribute(STATE_SITE_TITLE));
		context.put("user_id", UserDirectoryService.getCurrentUser().getId());
		context.put ("dav_group", "/dav/group/");
		context.put ("dav_user", "/dav/user/");
		String webdav_instructions = ServerConfigurationService.getString("webdav.instructions.url");
		context.put("webdav_instructions" ,webdav_instructions);

		// TODO: get browser id from somewhere.
		//Session session = SessionManager.getCurrentSession();
		//String browserId = session.;
		String browserID = UsageSessionService.getSession().getBrowserId();
		if(browserID.equals(UsageSession.WIN_IE))
		{
			context.put("isWinIEBrowser", Boolean.TRUE.toString());
		}

		return TEMPLATE_DAV;

	}	// buildWebdavContext

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
			BrowseItem folder = (BrowseItem) it.next();
			addAlert(state, rb.getString("folder2") + " " + folder.getName() + " " + rb.getString("contain2") + " ");
		}

		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put ("service", state.getAttribute (STATE_CONTENT_SERVICE));

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
	* Build the context to show the list of resource properties
	*/
	public static String buildMoreContext (	VelocityPortlet portlet,
									Context context,
									RunData data,
									SessionState state)
	{
		context.put("tlang",rb);
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));
		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService service = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		context.put ("service", service);

		Map current_stack_frame = peekAtStack(state);

		String id = (String) current_stack_frame.get(STATE_MORE_ID);
		context.put ("id", id);
		String collectionId = (String) current_stack_frame.get(STATE_MORE_COLLECTION_ID);
		context.put ("collectionId", collectionId);
		String homeCollectionId = (String) (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
		context.put("homeCollectionId", homeCollectionId);
		List cPath = getCollectionPath(state);
		context.put ("collectionPath", cPath);

		// for the resources of type URL or plain text, show the content also
		try
		{
			ResourceProperties properties = service.getProperties (id);
			context.put ("properties", properties);

			String isCollection = properties.getProperty (ResourceProperties.PROP_IS_COLLECTION);
			if ((isCollection != null) && isCollection.equals (Boolean.FALSE.toString()))
			{
				String copyrightAlert = properties.getProperty(properties.getNamePropCopyrightAlert());
				context.put("hasCopyrightAlert", copyrightAlert);

				String type = properties.getProperty (ResourceProperties.PROP_CONTENT_TYPE);
				if (type.equalsIgnoreCase (MIME_TYPE_DOCUMENT_PLAINTEXT) || type.equalsIgnoreCase (MIME_TYPE_DOCUMENT_HTML) || type.equalsIgnoreCase (ResourceProperties.TYPE_URL))
				{
					ContentResource moreResource = service.getResource (id);
					// read the body
					String body = "";
					byte[] content = null;
					try
					{
						content = moreResource.getContent();
						if (content != null)
						{
							body = new String(content);
						}
					}
					catch(ServerOverloadException e)
					{
						// this represents server's file system is temporarily unavailable
						// report problem to user? log problem?
					}
					context.put ("content", body);
				}	// if
			}	// if

			else
			{
				// setup for quota - ADMIN only, collection only
				if (SecurityService.isSuperUser())
				{
					try
					{
						// Getting the quota as a long validates the property
						long quota = properties.getLongProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
						context.put("hasQuota", Boolean.TRUE);
						context.put("quota", properties.getProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA));
					}
					catch (Exception any) {}
				}
			}
		}
		catch (IdUnusedException e)
		{
			addAlert(state,RESOURCE_NOT_EXIST_STRING);
			context.put("notExistFlag", new Boolean(true));
		}
		catch (TypeException e)
		{
			addAlert(state, rb.getString("typeex") + " ");
		}
		catch (PermissionException e)
		{
			addAlert(state," " + rb.getString("notpermis2") + " " + id + ". ");
		}	// try-catch

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
			context.put("dropboxMode", Boolean.FALSE);
			
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
				boolean pubview = ContentHostingService.isInheritingPubView(id);
				if (!pubview) pubview = ContentHostingService.isPubView(id);
				context.put("pubview", new Boolean(pubview));
			}

		}
		
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));

		if (state.getAttribute(COPYRIGHT_TYPES) != null)
		{
			List copyrightTypes = (List) state.getAttribute(COPYRIGHT_TYPES);
			context.put("copyrightTypes", copyrightTypes);
		}

		metadataGroupsIntoContext(state, context);

		// String template = (String) getContext(data).get("template");
		return TEMPLATE_MORE;

	}	// buildMoreContext

	/**
	* Build the context to edit the editable list of resource properties
	*/
	public static String buildEditContext (VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{

		context.put("tlang",rb);
		// find the ContentTypeImage service

		Map current_stack_frame = peekAtStack(state);

		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put ("from", state.getAttribute (STATE_FROM));
		context.put ("mycopyright", (String) state.getAttribute (STATE_MY_COPYRIGHT));

		context.put("SITE_ACCESS", AccessMode.SITE.toString());
		context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
		context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());

		String collectionId = (String) current_stack_frame.get(STATE_STACK_EDIT_COLLECTION_ID);
		context.put ("collectionId", collectionId);
		String id = (String) current_stack_frame.get(STATE_STACK_EDIT_ID);
		if(id == null)
		{
			id = (String) state.getAttribute(STATE_EDIT_ID);
			if(id == null)
			{
				id = "";
			}
			current_stack_frame.put(STATE_STACK_EDIT_ID, id);
		}
		context.put ("id", id);
		String homeCollectionId = (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
		if(homeCollectionId == null)
		{
			homeCollectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			state.setAttribute(STATE_HOME_COLLECTION_ID, homeCollectionId);
		}
		context.put("homeCollectionId", homeCollectionId);
		List collectionPath = getCollectionPath(state);
		context.put ("collectionPath", collectionPath);

		if(homeCollectionId.equals(id))
		{
			context.put("atHome", Boolean.TRUE.toString());
		}

		String intent = (String) current_stack_frame.get(STATE_STACK_EDIT_INTENT);
		if(intent == null)
		{
			intent = INTENT_REVISE_FILE;
			current_stack_frame.put(STATE_STACK_EDIT_INTENT, intent);
		}
		context.put("intent", intent);
		context.put("REVISE", INTENT_REVISE_FILE);
		context.put("REPLACE", INTENT_REPLACE_FILE);

		Collection groups = ContentHostingService.getGroupsWithReadAccess(collectionId);
		// TODO: does this method filter groups for this subcollection??
		if(! groups.isEmpty())
		{
			context.put("siteHasGroups", Boolean.TRUE.toString());
			context.put("theGroupsInThisSite", groups);
		}
		
		String show_form_items = (String) state.getAttribute(STATE_SHOW_FORM_ITEMS);
		if(show_form_items == null)
		{
			show_form_items = (String) state.getAttribute(STATE_SHOW_FORM_ITEMS);
			if(show_form_items != null)
			{
				current_stack_frame.put(STATE_SHOW_FORM_ITEMS,show_form_items);
			}
		}
		if(show_form_items != null)
		{
			context.put("show_form_items", show_form_items);
		}

		// put the item into context
		EditItem item = (EditItem) current_stack_frame.get(STATE_STACK_EDIT_ITEM);
		if(item == null)
		{
			item = getEditItem(id, collectionId, data);
			if(item == null)
			{
				// what??
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				// got resource and sucessfully populated item with values
				state.setAttribute(STATE_EDIT_ALERTS, new HashSet());
				current_stack_frame.put(STATE_STACK_EDIT_ITEM, item);
			}
		}

		context.put("item", item);

		if(item.isStructuredArtifact())
		{
			context.put("formtype", item.getFormtype());
			current_stack_frame.put(STATE_STACK_STRUCTOBJ_TYPE, item.getFormtype());

			List listOfHomes = (List) current_stack_frame.get(STATE_STRUCTOBJ_HOMES);
			if(listOfHomes == null)
			{
				ResourcesAction.setupStructuredObjects(state);
				listOfHomes = (List) current_stack_frame.get(STATE_STRUCTOBJ_HOMES);
			}
			context.put("homes", listOfHomes);

			String formtype_readonly = (String) current_stack_frame.get(STATE_STACK_STRUCTOBJ_TYPE_READONLY);
			if(formtype_readonly == null)
			{
				formtype_readonly = (String) state.getAttribute(STATE_STRUCTOBJ_TYPE_READONLY);
				if(formtype_readonly == null)
				{
					formtype_readonly = Boolean.FALSE.toString();
				}
				current_stack_frame.put(STATE_STACK_STRUCTOBJ_TYPE_READONLY, formtype_readonly);
			}
			if(formtype_readonly != null && formtype_readonly.equals(Boolean.TRUE.toString()))
			{
				context.put("formtype_readonly", formtype_readonly);
			}

			String rootname = (String) current_stack_frame.get(STATE_STACK_STRUCTOBJ_ROOTNAME);
			context.put("rootname", rootname);

			context.put("STRING", ResourcesMetadata.WIDGET_STRING);
			context.put("TEXTAREA", ResourcesMetadata.WIDGET_TEXTAREA);
			context.put("BOOLEAN", ResourcesMetadata.WIDGET_BOOLEAN);
			context.put("INTEGER", ResourcesMetadata.WIDGET_INTEGER);
			context.put("DOUBLE", ResourcesMetadata.WIDGET_DOUBLE);
			context.put("DATE", ResourcesMetadata.WIDGET_DATE);
			context.put("TIME", ResourcesMetadata.WIDGET_TIME);
			context.put("DATETIME", ResourcesMetadata.WIDGET_DATETIME);
			context.put("ANYURI", ResourcesMetadata.WIDGET_ANYURI);
			context.put("ENUM", ResourcesMetadata.WIDGET_ENUM);
			context.put("NESTED", ResourcesMetadata.WIDGET_NESTED);
			context.put("WYSIWYG", ResourcesMetadata.WIDGET_WYSIWYG);

			context.put("today", TimeService.newTime());

			context.put("TRUE", Boolean.TRUE.toString());
		}

		// copyright
		copyrightChoicesIntoContext(state, context);

		// put schema for metadata into context
		metadataGroupsIntoContext(state, context);

		// %%STATE_MODE_RESOURCES%%
		if (RESOURCES_MODE_RESOURCES.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			context.put("dropboxMode", Boolean.FALSE);
		}
		else if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// notshow the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
		}
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));

		// String template = (String) getContext(data).get("template");

		return TEMPLATE_EDIT;

	}	// buildEditContext

	/**
	* Navigate in the resource hireachy
	*/
	public static void doNavigate ( RunData data )
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
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, new HashMap());
		}

	}	// doNavigate

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

	/**
	 * initiate creation of one or more resource items (folders, file uploads, html docs, text docs, or urls)
	 * default type is folder
	 */
	public static void doCreate(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

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

		String itemType = params.getString("itemType");
		if(itemType == null || "".equals(itemType))
		{
			itemType = TYPE_UPLOAD;
		}

		String stackOp = params.getString("suspended-operations-stack");

		Map current_stack_frame = null;
		if(stackOp != null && stackOp.equals("peek"))
		{
			current_stack_frame = peekAtStack(state);
		}
		else
		{
			current_stack_frame = pushOnStack(state);
		}

		String encoding = data.getRequest().getCharacterEncoding();

		String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
		if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
		{
			defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
			state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
		}

		List new_items = new Vector();
		for(int i = 0; i < CREATE_MAX_ITEMS; i++)
		{
			EditItem item = new EditItem(itemType);
			if(encoding != null)
			{
				item.setEncoding(encoding);
			}
			item.setCopyrightStatus(defaultCopyrightStatus);
			new_items.add(item);
		}

		current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);
		current_stack_frame.put(STATE_STACK_CREATE_TYPE, itemType);

		String collectionId = params.getString ("collectionId");
		current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);

		current_stack_frame.put(STATE_STACK_CREATE_NUMBER, new Integer(1));

		state.setAttribute(STATE_CREATE_ALERTS, new HashSet());
		current_stack_frame.put(STATE_CREATE_MISSING_ITEM, new HashSet());
		current_stack_frame.remove(STATE_STACK_STRUCTOBJ_TYPE);

		current_stack_frame.put(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_CREATE_INIT);
		state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_CREATE_INIT);

	}	// doCreate

	
	public static void addCreateContextAlert(SessionState state, String message)
	{
		String soFar = (String) state.getAttribute(STATE_CREATE_MESSAGE);
		if (soFar != null)
		{
			soFar = soFar + " " + message;
		}
		else
		{
			soFar = message;
		}
		state.setAttribute(STATE_CREATE_MESSAGE, soFar);

	} // addItemTypeContextAlert

	/**
	 * initiate creation of one or more resource items (file uploads, html docs, text docs, or urls -- not folders)
	 * default type is file upload
	 */
	/**
	 * @param data
	 */
	public static void doCreateitem(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		Map current_stack_frame = peekAtStack(state);
		boolean pop = false;
		

		String itemType = params.getString("itemType");
		String flow = params.getString("flow");
		

		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}
		Set missing = new HashSet();
		if(flow == null || flow.equals("cancel"))
		{
			pop = true;
		}
		else if(flow.equals("updateNumber"))
		{
			captureMultipleValues(state, params, false);
			int number = params.getInt("numberOfItems");
			Integer numberOfItems = new Integer(number);
			current_stack_frame.put(ResourcesAction.STATE_STACK_CREATE_NUMBER, numberOfItems);

			// clear display of error messages
			state.setAttribute(STATE_CREATE_ALERTS, new HashSet());

			List items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
			if(items == null)
			{
				String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
				if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
				{
					defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
					state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
				}

				String encoding = data.getRequest().getCharacterEncoding();

				items = new Vector();
				for(int i = 0; i < CREATE_MAX_ITEMS; i++)
				{
					EditItem item = new EditItem(itemType);
					if(encoding != null)
					{
						item.setEncoding(encoding);
					}
					item.setCopyrightStatus(defaultCopyrightStatus);
					items.add(item);
				}

			}
			current_stack_frame.put(STATE_STACK_CREATE_ITEMS, items);
			Iterator it = items.iterator();
			while(it.hasNext())
			{
				EditItem item = (EditItem) it.next();
				item.clearMissing();
			}
			state.removeAttribute(STATE_MESSAGE);
		}
		else if(flow.equals("create") && TYPE_FOLDER.equals(itemType))
		{
			// Get the items
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts.isEmpty())
			{
				// Save the items
				createFolders(state);
				alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);

				if(alerts.isEmpty())
				{
					pop = true;
				}
			}
		}
		else if(flow.equals("create") && TYPE_UPLOAD.equals(itemType))
		{
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts.isEmpty())
			{
				createFiles(state);
				alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
				if(alerts.isEmpty())
				{
					pop = true;
				}
			}
		}
		else if(flow.equals("create") && MIME_TYPE_DOCUMENT_HTML.equals(itemType))
		{
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts.isEmpty())
			{
				createFiles(state);
				alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
				if(alerts.isEmpty())
				{
					pop = true;
				}
			}
		}
		else if(flow.equals("create") && MIME_TYPE_DOCUMENT_PLAINTEXT.equals(itemType))
		{
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts.isEmpty())
			{
				createFiles(state);
				alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
				if(alerts.isEmpty())
				{
					pop =true;
				}
			}
		}
		else if(flow.equals("create") && TYPE_URL.equals(itemType))
		{
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts.isEmpty())
			{
				createUrls(state);
				alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
				if(alerts.isEmpty())
				{
					pop = true;
				}
			}
		}
		else if(flow.equals("create") && TYPE_FORM.equals(itemType))
		{
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts == null)
			{
				alerts = new HashSet();
				state.setAttribute(STATE_CREATE_ALERTS, alerts);
			}
			if(alerts.isEmpty())
			{
				createStructuredArtifacts(state);
				alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
				if(alerts.isEmpty())
				{
					pop = true;
				}
			}
		}
		else if(flow.equals("create"))
		{
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts == null)
			{
				alerts = new HashSet();
				state.setAttribute(STATE_CREATE_ALERTS, alerts);
			}
			alerts.add("Invalid item type");
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}
		else if(flow.equals("updateDocType"))
		{
			// captureMultipleValues(state, params, false);
			String formtype = params.getString("formtype");
			if(formtype == null || formtype.equals(""))
			{
				alerts.add("Must select a form type");
				missing.add("formtype");
			}
			current_stack_frame.put(STATE_STACK_STRUCTOBJ_TYPE, formtype);
			setupStructuredObjects(state);
		}
		else if(flow.equals("addInstance"))
		{
			captureMultipleValues(state, params, false);
			String field = params.getString("field");
			List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
			if(new_items == null)
			{
				String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
				if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
				{
					defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
					state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
				}

				String encoding = data.getRequest().getCharacterEncoding();

				new_items = new Vector();
				for(int i = 0; i < CREATE_MAX_ITEMS; i++)
				{
					EditItem item = new EditItem(itemType);
					if(encoding != null)
					{
						item.setEncoding(encoding);
					}
					item.setCopyrightStatus(defaultCopyrightStatus);
					new_items.add(item);
				}
				current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);

			}
			EditItem item = (EditItem) new_items.get(0);
			addInstance(field, item.getProperties());
			ResourcesMetadata form = item.getForm();
			List flatList = form.getFlatList();
			item.setProperties(flatList);
		}
		else if(flow.equals("linkResource") && TYPE_FORM.equals(itemType))
		{
			captureMultipleValues(state, params, false);
			createLink(data, state);
			
		}
		else if(flow.equals("showOptional"))
		{
			captureMultipleValues(state, params, false);
			int twiggleNumber = params.getInt("twiggleNumber", 0);
			String metadataGroup = params.getString("metadataGroup");
			List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
			if(new_items == null)
			{
				String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
				if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
				{
					defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
					state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
				}

				String encoding = data.getRequest().getCharacterEncoding();

				new_items = new Vector();
				for(int i = 0; i < CREATE_MAX_ITEMS; i++)
				{
					EditItem item = new EditItem(itemType);
					if(encoding != null)
					{
						item.setEncoding(encoding);
					}
					item.setCopyrightStatus(defaultCopyrightStatus);
					new_items.add(item);
				}
				current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);

			}
			if(new_items != null && new_items.size() > twiggleNumber)
			{
				EditItem item = (EditItem) new_items.get(twiggleNumber);
				if(item != null)
				{
					item.showMetadataGroup(metadataGroup);
				}
			}

			// clear display of error messages
			state.setAttribute(STATE_CREATE_ALERTS, new HashSet());
			Iterator it = new_items.iterator();
			while(it.hasNext())
			{
				EditItem item = (EditItem) it.next();
				item.clearMissing();
			}
		}
		else if(flow.equals("hideOptional"))
		{
			captureMultipleValues(state, params, false);
			int twiggleNumber = params.getInt("twiggleNumber", 0);
			String metadataGroup = params.getString("metadataGroup");
			List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
			if(new_items == null)
			{
				String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
				if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
				{
					defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
					state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
				}

				String encoding = data.getRequest().getCharacterEncoding();

				new_items = new Vector();
				for(int i = 0; i < CREATE_MAX_ITEMS; i++)
				{
					EditItem item = new EditItem(itemType);
					if(encoding != null)
					{
						item.setEncoding(encoding);
					}
					item.setCopyrightStatus(defaultCopyrightStatus);
					new_items.add(item);
				}
				current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);
			}
			if(new_items != null && new_items.size() > twiggleNumber)
			{
				EditItem item = (EditItem) new_items.get(twiggleNumber);
				if(item != null)
				{
					item.hideMetadataGroup(metadataGroup);
				}
			}

			// clear display of error messages
			state.setAttribute(STATE_CREATE_ALERTS, new HashSet());
			Iterator it = new_items.iterator();
			while(it.hasNext())
			{
				EditItem item = (EditItem) it.next();
				item.clearMissing();
			}
		}

		alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}
		
		Iterator alertIt = alerts.iterator();
		while(alertIt.hasNext())
		{
			String alert = (String) alertIt.next();
			addCreateContextAlert(state, alert);
			//addAlert(state, alert);
		}
		alerts.clear();
		current_stack_frame.put(STATE_CREATE_MISSING_ITEM, missing);

		if(pop)
		{
			List new_items = (List) current_stack_frame.get(ResourcesAction.STATE_HELPER_NEW_ITEMS);
			String helper_changed = (String) state.getAttribute(STATE_HELPER_CHANGED);
			if(Boolean.TRUE.toString().equals(helper_changed))
			{
				// get list of attachments?
				if(new_items != null)
				{
					List attachments = (List) state.getAttribute(STATE_ATTACHMENTS);
					if(attachments == null)
					{
						attachments = EntityManager.newReferenceList();
						state.setAttribute(STATE_ATTACHMENTS, attachments);
					}
					Iterator it = new_items.iterator();
					while(it.hasNext())
					{
						AttachItem item = (AttachItem) it.next();
						try 
						{	
							ContentResource resource = ContentHostingService.getResource(item.getId());
							if (checkSelctItemFilter(resource, state))
							{
								attachments.add(resource.getReference());
							}
							else
							{
								it.remove();
								addAlert(state, (String) rb.getFormattedMessage("filter", new Object[]{item.getDisplayName()}));
							}
						} 
						catch (PermissionException e) 
						{
							addAlert(state, (String) rb.getFormattedMessage("filter", new Object[]{item.getDisplayName()}));
						} 
						catch (IdUnusedException e) 
						{
							addAlert(state, (String) rb.getFormattedMessage("filter", new Object[]{item.getDisplayName()}));
						} 
						catch (TypeException e) 
						{
							addAlert(state, (String) rb.getFormattedMessage("filter", new Object[]{item.getDisplayName()}));
						}
						
						Reference ref = EntityManager.newReference(ContentHostingService.getReference(item.getId()));

		               }
				}
			}
			popFromStack(state);
			resetCurrentMode(state);

			if(!ResourcesAction.isStackEmpty(state) && new_items != null)
			{
				current_stack_frame = peekAtStack(state);
				List old_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
				if(old_items == null)
				{
					old_items = new Vector();
					current_stack_frame.put(STATE_HELPER_NEW_ITEMS, old_items);
				}
				old_items.addAll(new_items);
			}
		}

	}	// doCreateitem

	private static void createLink(RunData data, SessionState state)
	{
		ParameterParser params = data.getParameters ();

		Map current_stack_frame = peekAtStack(state);

		String field = params.getString("field");
		if(field == null)
		{

		}
		else
		{
			current_stack_frame.put(ResourcesAction.STATE_ATTACH_FORM_FIELD, field);
		}

		//state.setAttribute(ResourcesAction.STATE_MODE, ResourcesAction.MODE_HELPER);
		state.setAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_SELECT);
		state.setAttribute(ResourcesAction.STATE_ATTACH_CARDINALITY, ResourcesAction.CARDINALITY_SINGLE);

		// put a copy of the attachments into the state

		// state.setAttribute(ResourcesAction.STATE_ATTACHMENTS, EntityManager.newReferenceList());
		// whether there is already an attachment
		/*
		if (attachments.size() > 0)
		{
			sstate.setAttribute(ResourcesAction.STATE_HAS_ATTACHMENT_BEFORE, Boolean.TRUE);
		}
		else
		{
			sstate.setAttribute(ResourcesAction.STATE_HAS_ATTACHMENT_BEFORE, Boolean.FALSE);
		}
		*/

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

	}

	/**
	 * Add a new StructuredArtifact to ContentHosting for each EditItem in the state attribute named STATE_STACK_CREATE_ITEMS.
	 * The number of items to be added is indicated by the state attribute named STATE_STACK_CREATE_NUMBER, and
	 * the items are added to the collection identified by the state attribute named STATE_STACK_CREATE_COLLECTION_ID.
	 * @param state
	 */
	private static void createStructuredArtifacts(SessionState state)
	{
		Map current_stack_frame = peekAtStack(state);

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		if(new_items == null)
		{
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}
			String itemType = (String) current_stack_frame.get(STATE_STACK_CREATE_TYPE);
			if(itemType == null)
			{
				itemType = (String) state.getAttribute(STATE_CREATE_TYPE);
				if(itemType == null)
				{
					itemType = ResourcesAction.TYPE_FORM;
				}
				current_stack_frame.put(STATE_STACK_CREATE_TYPE, itemType);
			}
			String encoding = (String) state.getAttribute(STATE_ENCODING);
			new_items = new Vector();
			for(int i = 0; i < CREATE_MAX_ITEMS; i++)
			{
				EditItem item = new EditItem(itemType);
				if(encoding != null)
				{
					item.setEncoding(encoding);
				}
				item.setCopyrightStatus(defaultCopyrightStatus);
				new_items.add(item);
			}
			current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);
		}

		String collectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
		if(collectionId == null || collectionId.trim().length() == 0)
		{
			collectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			if(collectionId == null || collectionId.trim().length() == 0)
			{
				collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			}
			current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);
		}

		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}

		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(number == null)
		{
			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			if(number == null)
			{
				number = new Integer(1);
			}
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}
		int numberOfItems = number.intValue();

		SchemaBean rootSchema = (SchemaBean) current_stack_frame.get(STATE_STACK_STRUCT_OBJ_SCHEMA);
		SchemaNode rootNode = rootSchema.getSchema();

		outerloop: for(int i = 0; i < numberOfItems; i++)
		{
			EditItem item = (EditItem) new_items.get(i);
			if(item.isBlank())
			{
				continue;
			}
			SaveArtifactAttempt attempt = new SaveArtifactAttempt(item, rootNode);
			validateStructuredArtifact(attempt);
			List errors = attempt.getErrors();

			if(errors.isEmpty())
			{
				try
				{
					ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties ();
					resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, item.getName());
					resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, item.getDescription());
					resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, "UTF-8");
					resourceProperties.addProperty(ResourceProperties.PROP_STRUCTOBJ_TYPE, item.getFormtype());
					resourceProperties.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, org.sakaiproject.metaobj.shared.mgt.MetaobjEntityManager.METAOBJ_ENTITY_PREFIX);
					List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
					saveMetadata(resourceProperties, metadataGroups, item);
					String filename = Validator.escapeResourceName(item.getName()).trim();
					String extension = ".xml";
					int attemptNum = 0;
					String attemptStr = "";
					String newResourceId = collectionId + filename + attemptStr + extension;

					if(newResourceId.length() > ContentHostingService.MAXIMUM_RESOURCE_ID_LENGTH)
					{
						alerts.add(rb.getString("toolong") + " " + newResourceId);
						continue outerloop;
					}

					try
					{
						ContentResource resource = ContentHostingService.addResource (filename + extension,
																					collectionId,
																					MAXIMUM_ATTEMPTS_FOR_UNIQUENESS,
																					MIME_TYPE_STRUCTOBJ,
																					item.getContent(),
																					resourceProperties,
																					item.getNotification());


						Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
						if(preventPublicDisplay == null)
						{
							preventPublicDisplay = Boolean.FALSE;
							state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
						}
						
						if(preventPublicDisplay.equals(Boolean.FALSE))
						{
							if (! RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
							{
								if (!item.isPubviewset())
								{
									ContentHostingService.setPubView(resource.getId(), item.isPubview());
								}
							}
						}

						String mode = (String) state.getAttribute(STATE_MODE);
						if(MODE_HELPER.equals(mode))
						{
							String helper_mode = (String) state.getAttribute(STATE_RESOURCES_HELPER_MODE);
							if(helper_mode != null && MODE_ATTACHMENT_NEW_ITEM_INIT.equals(helper_mode))
							{
								// add to the attachments vector
								List attachments = EntityManager.newReferenceList();
								Reference ref = EntityManager.newReference(ContentHostingService.getReference(resource.getId()));
								attachments.add(ref);
								cleanupState(state);
								state.setAttribute(STATE_ATTACHMENTS, attachments);
							}
							else
							{
								Object attach_links = current_stack_frame.get(STATE_ATTACH_LINKS);
								if(attach_links == null)
								{
									attach_links = state.getAttribute(STATE_ATTACH_LINKS);
									if(attach_links != null)
									{
										current_stack_frame.put(STATE_ATTACH_LINKS, attach_links);
									}
								}

								if(attach_links == null)
								{
									attachItem(resource.getId(), state);
								}
								else
								{
									attachLink(resource.getId(), state);
								}
							}
						}
					}
					catch(PermissionException e)
					{
						alerts.add(rb.getString("notpermis12"));
						continue outerloop;
					}
					catch(IdInvalidException e)
					{
						alerts.add(rb.getString("title") + " " + e.getMessage ());
						continue outerloop;
					}
					catch(IdLengthException e)
					{
						alerts.add(rb.getString("toolong") + " " + e.getMessage());
						continue outerloop;
					}
					catch(IdUniquenessException e)
					{
						alerts.add("Could not add this item to this folder");
						continue outerloop;
					}
					catch(InconsistentException e)
					{
						alerts.add(RESOURCE_INVALID_TITLE_STRING);
						continue outerloop;
					}
					catch(OverQuotaException e)
					{
						alerts.add(rb.getString("overquota"));
						continue outerloop;
					}
					catch(ServerOverloadException e)
					{
						alerts.add(rb.getString("failed"));
						continue outerloop;
					}
				}
				catch(RuntimeException e)
				{
					logger.warn("ResourcesAction.createStructuredArtifacts ***** Unknown Exception ***** " + e.getMessage());
					alerts.add(rb.getString("failed"));
				}

				HashMap currentMap = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
				if(currentMap == null)
				{
					// do nothing
				}
				else if(!currentMap.containsKey(collectionId))
				{
					try
					{
						currentMap.put (collectionId,ContentHostingService.getCollection (collectionId));
						state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);

						// add this folder id into the set to be event-observed
						addObservingPattern(collectionId, state);
					}
					catch (IdUnusedException ignore)
					{
					}
					catch (TypeException ignore)
					{
					}
					catch (PermissionException ignore)
					{
					}
				}
				state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);
			}
			else
			{
				Iterator errorIt = errors.iterator();
				while(errorIt.hasNext())
				{
					ValidationError error = (ValidationError) errorIt.next();
					alerts.add(error.getDefaultMessage());
				}
			}

		}
		state.setAttribute(STATE_CREATE_ALERTS, alerts);

	}

	/**
	 * Convert from a hierarchical list of ResourcesMetadata objects to an org.w3.dom.Document,
	 * then to a string representation, then to a metaobj ElementBean.  Validate the ElementBean
	 * against a SchemaBean.  If it validates, save the string representation. Otherwise, on
	 * return, the parameter contains a non-empty list of ValidationError objects describing the
	 * problems.
	 * @param attempt A wrapper for the EditItem object which contains the hierarchical list of
	 * ResourcesMetadata objects for this form.  Also contains an initially empty list of
	 * ValidationError objects that describes any of the problems found in validating the form.
	 */
	private static void validateStructuredArtifact(SaveArtifactAttempt attempt)
	{
		EditItem item = attempt.getItem();
		ResourcesMetadata form = item.getForm();

		Stack processStack = new Stack();
		processStack.push(form);
		Map parents = new Hashtable();
		Document doc = Xml.createDocument();

		int count = 0;

		while(!processStack.isEmpty())
		{
			Object object = processStack.pop();
			if(object instanceof ResourcesMetadata)
			{

				ResourcesMetadata element = (ResourcesMetadata) object;
				Element node = doc.createElement(element.getLocalname());

				if(element.isNested())
				{
					processStack.push(new ElementCarrier(node, element.getDottedname()));
					List children = element.getNestedInstances();
					//List children = element.getNested();

					for(int k = children.size() - 1; k >= 0; k--)
					{
						ResourcesMetadata child = (ResourcesMetadata) children.get(k);
						processStack.push(child);
						parents.put(child.getDottedname(), node);
					}
				}
				else
				{
					List values = element.getInstanceValues();
					Iterator valueIt = values.iterator();
					while(valueIt.hasNext())
					{
						Object value = valueIt.next();
						if(value == null)
						{
							// do nothing
						}
						else if(value instanceof String)
						{
							node.appendChild(doc.createTextNode((String)value));
						}
						else if(value instanceof Time)
						{
							Time time = (Time) value;
							TimeBreakdown breakdown = time.breakdownLocal();
							int year = breakdown.getYear();
							int month = breakdown.getMonth();
							int day = breakdown.getDay();
							String date = "" + year + (month < 10 ? "-0" : "-") + month + (day < 10 ? "-0" : "-") + day;
							node.appendChild(doc.createTextNode(date));
						}
						else if(value instanceof Date)
						{
							Date date = (Date) value;
							SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
							String formatted = df.format(date);
							node.appendChild(doc.createTextNode(formatted));
						}
						else if(value instanceof Reference)
						{
							node.appendChild(doc.createTextNode(((Reference)value).getId()));
						}
						else
						{
							node.appendChild(doc.createTextNode(value.toString()));
						}
					}

					Element parent = (Element) parents.get(element.getDottedname());
					if(parent == null)
					{
						doc.appendChild(node);
						count++;
					}
					else
					{
						parent.appendChild(node);
					}
				}
			}
			else if(object instanceof ElementCarrier)
			{
				ElementCarrier carrier = (ElementCarrier) object;
				Element node = carrier.getElement();
				Element parent = (Element) parents.get(carrier.getParent());
				if(parent == null)
				{
					doc.appendChild(node);
					count++;
				}
				else
				{
					parent.appendChild(node);
				}
			}

		}

		String content = Xml.writeDocumentToString(doc);
		item.setContent(content);

		StructuredArtifactValidationService validator = (StructuredArtifactValidationService) ComponentManager.get("org.sakaiproject.metaobj.shared.mgt.StructuredArtifactValidationService");
		List errors = new ArrayList();

		// convert the String representation to an ElementBean object.  If that fails,
		// add an error and return.
		ElementBean bean = null;

		SAXBuilder builder = new SAXBuilder();
		StringReader reader = new StringReader(content);
		try
		{
			org.jdom.Document jdoc = builder.build(reader);
			bean = new ElementBean(jdoc.getRootElement(), attempt.getSchema(), true);
		}
		catch (JDOMException e)
		{
			// add message to list of errors
			errors.add(new ValidationError("","",null,"JDOMException"));
		}
		catch (IOException e)
		{
			// add message to list of errors
			errors.add(new ValidationError("","",null,"IOException"));
		}

		// call this.validate(bean, rootSchema, errors) and add results to errors list.
		if(bean == null)
		{
			// add message to list of errors
			errors.add(new ValidationError("","",null,"Bean is null"));
		}
		else
		{
			errors.addAll(validator.validate(bean));
		}
		attempt.setErrors(errors);

	}	// validateStructuredArtifact

	/**
	 * Add a new folder to ContentHosting for each EditItem in the state attribute named STATE_STACK_CREATE_ITEMS.
	 * The number of items to be added is indicated by the state attribute named STATE_STACK_CREATE_NUMBER, and
	 * the items are added to the collection identified by the state attribute named STATE_STACK_CREATE_COLLECTION_ID.
	 * @param state
	 */
	protected static void createFolders(SessionState state)
	{
		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}

		Map current_stack_frame = peekAtStack(state);

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		if(new_items == null)
		{
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			new_items = new Vector();
			for(int i = 0; i < CREATE_MAX_ITEMS; i++)
			{
				EditItem item = new EditItem(TYPE_FOLDER);
				item.setCopyrightStatus(defaultCopyrightStatus);
				new_items.add(item);
			}
			current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);

		}
		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(number == null)
		{
			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}
		if(number == null)
		{
			number = new Integer(1);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}

		String collectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
		if(collectionId == null || collectionId.trim().length() == 0)
		{
			collectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			if(collectionId == null || collectionId.trim().length() == 0)
			{
				collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			}
			current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);
		}

		int numberOfFolders = 1;
		numberOfFolders = number.intValue();

		outerloop: for(int i = 0; i < numberOfFolders; i++)
		{
			EditItem item = (EditItem) new_items.get(i);
			if(item.isBlank())
			{
				continue;
			}
			String newCollectionId = collectionId + Validator.escapeResourceName(item.getName()) + Entity.SEPARATOR;

			if(newCollectionId.length() > ContentHostingService.MAXIMUM_RESOURCE_ID_LENGTH)
			{
				alerts.add(rb.getString("toolong") + " " + newCollectionId);
				continue outerloop;
			}

			ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties ();

			try
			{
				resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, item.getName());
				resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, item.getDescription());
				List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
				saveMetadata(resourceProperties, metadataGroups, item);

				ContentCollection collection = ContentHostingService.addCollection (newCollectionId, resourceProperties, item.getGroups());
				
				Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
				if(preventPublicDisplay == null)
				{
					preventPublicDisplay = Boolean.FALSE;
					state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
				}	
				
				if(preventPublicDisplay.equals(Boolean.FALSE))
				{
					if (! RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
					{
						if (!item.isPubviewset())
						{
							ContentHostingService.setPubView(collection.getId(), item.isPubview());
						}
					}
				}
				
			}
			catch (IdUsedException e)
			{
				alerts.add(rb.getString("resotitle") + " " + item.getName() + " " + rb.getString("used4"));
			}
			catch (IdInvalidException e)
			{
				alerts.add(rb.getString("title") + " " + e.getMessage ());
			}
			catch (PermissionException e)
			{
				alerts.add(rb.getString("notpermis5") + " " + item.getName());
			}
			catch (InconsistentException e)
			{
				alerts.add(RESOURCE_INVALID_TITLE_STRING);
			}	// try-catch
		}

		HashMap currentMap = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(!currentMap.containsKey(collectionId))
		{
			try
			{
				currentMap.put (collectionId,ContentHostingService.getCollection (collectionId));
				state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);

				// add this folder id into the set to be event-observed
				addObservingPattern(collectionId, state);
			}
			catch (IdUnusedException ignore)
			{
			}
			catch (TypeException ignore)
			{
			}
			catch (PermissionException ignore)
			{
			}
		}
		state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);

		state.setAttribute(STATE_CREATE_ALERTS, alerts);

	}	// createFolders

	/**
	 * Add a new file to ContentHosting for each EditItem in the state attribute named STATE_STACK_CREATE_ITEMS.
	 * The number of items to be added is indicated by the state attribute named STATE_STACK_CREATE_NUMBER, and
	 * the items are added to the collection identified by the state attribute named STATE_STACK_CREATE_COLLECTION_ID.
	 * @param state
	 */
	protected static void createFiles(SessionState state)
	{
		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}

		Map current_stack_frame = peekAtStack(state);

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		if(new_items == null)
		{
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			new_items = new Vector();
			for(int i = 0; i < CREATE_MAX_ITEMS; i++)
			{
				EditItem item = new EditItem(TYPE_UPLOAD);
				item.setCopyrightStatus(defaultCopyrightStatus);
				new_items.add(item);
			}
			current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);

		}
		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(number == null)
		{
			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}
		if(number == null)
		{
			number = new Integer(1);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}

		String collectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
		if(collectionId == null || collectionId.trim().length() == 0)
		{
			collectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			if(collectionId == null || collectionId.trim().length() == 0)
			{
				collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			}
			current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);
		}

		int numberOfItems = 1;
		numberOfItems = number.intValue();
		outerloop: for(int i = 0; i < numberOfItems; i++)
		{
			EditItem item = (EditItem) new_items.get(i);
			if(item.isBlank())
			{
				continue;
			}

			ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties ();

			resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, item.getName());
			resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, item.getDescription());

			resourceProperties.addProperty (ResourceProperties.PROP_COPYRIGHT, item.getCopyrightInfo());
			resourceProperties.addProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE, item.getCopyrightStatus());
			if (item.hasCopyrightAlert())
			{
				resourceProperties.addProperty (ResourceProperties.PROP_COPYRIGHT_ALERT, Boolean.toString(item.hasCopyrightAlert()));
			}
			else
			{
				resourceProperties.removeProperty (ResourceProperties.PROP_COPYRIGHT_ALERT);
			}
			
			BasicRightsAssignment rightsObj = item.getRights();
			rightsObj.addResourceProperties(resourceProperties);

			resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());
			if(item.isHtml())
			{
				resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, "UTF-8");
			}
			List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
			saveMetadata(resourceProperties, metadataGroups, item);
			String filename = Validator.escapeResourceName(item.getFilename().trim());
			if("".equals(filename))
			{
				filename = Validator.escapeResourceName(item.getName().trim());
			}


			resourceProperties.addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, filename);

			try
			{
				ContentResource resource = ContentHostingService.addResource (filename,
																			collectionId,
																			MAXIMUM_ATTEMPTS_FOR_UNIQUENESS,
																			item.getMimeType(),
																			item.getContent(),
																			resourceProperties, item.getNotification());

				item.setAdded(true);

				Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
				if(preventPublicDisplay == null)
				{
					preventPublicDisplay = Boolean.FALSE;
					state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
				}
				
				if(preventPublicDisplay.equals(Boolean.FALSE))
				{
					if (! RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
					{
						if (!item.isPubviewset())
						{
							ContentHostingService.setPubView(resource.getId(), item.isPubview());
						}
					}
				}
				
				try
				{
					Collection groupRefs = new Vector();
					Iterator it = item.getGroups().iterator();
					while(it.hasNext())
					{
						Group group = (Group) it.next();
						groupRefs.add(group.getReference());
					}
					// TODO: what if parent has groups and user tries to set no groups??  
					// Should tool prevent that without needing exception??
					if(!groupRefs.isEmpty())
					{
						// TODO: must be atomic with constructor
						ContentResourceEdit edit = ContentHostingService.editResource(resource.getId());
						edit.setGroupAccess(groupRefs);
						ContentHostingService.commitResource(edit);
					}
				}
				catch (IdUnusedException e)
				{
					
				}
				catch (TypeException e)
				{
					
				}
				catch (InUseException e)
				{
					
				}
				catch(InconsistentException e)
				{
					alerts.add(rb.getString("add.nogroups"));
				}


				String mode = (String) state.getAttribute(STATE_MODE);
				if(MODE_HELPER.equals(mode))
				{
					String helper_mode = (String) state.getAttribute(STATE_RESOURCES_HELPER_MODE);
					if(helper_mode != null && MODE_ATTACHMENT_NEW_ITEM_INIT.equals(helper_mode))
					{
						// add to the attachments vector
						List attachments = EntityManager.newReferenceList();
						Reference ref = EntityManager.newReference(ContentHostingService.getReference(resource.getId()));
						attachments.add(ref);
						cleanupState(state);
						state.setAttribute(STATE_ATTACHMENTS, attachments);
					}
					else
					{
						Object attach_links = current_stack_frame.get(STATE_ATTACH_LINKS);
						if(attach_links == null)
						{
							attach_links = state.getAttribute(STATE_ATTACH_LINKS);
							if(attach_links != null)
							{
								current_stack_frame.put(STATE_ATTACH_LINKS, attach_links);
							}
						}

						if(attach_links == null)
						{
							attachItem(resource.getId(), state);
						}
						else
						{
							attachLink(resource.getId(), state);
						}
					}
				}
			}
			catch(PermissionException e)
			{
				alerts.add(rb.getString("notpermis12"));
				continue outerloop;
			}
			catch(IdInvalidException e)
			{
				alerts.add(rb.getString("title") + " " + e.getMessage ());
				continue outerloop;
			}
			catch(IdLengthException e)
			{
				alerts.add(rb.getString("toolong") + " " + e.getMessage());
				continue outerloop;
			}
			catch(IdUniquenessException e)
			{
				alerts.add("Could not add this item to this folder");
				continue outerloop;
			}
			catch(InconsistentException e)
			{
				alerts.add(RESOURCE_INVALID_TITLE_STRING);
				continue outerloop;
			}
			catch(OverQuotaException e)
			{
				alerts.add(rb.getString("overquota"));
				continue outerloop;
			}
			catch(ServerOverloadException e)
			{
				alerts.add(rb.getString("failed"));
				continue outerloop;
			}
			catch(RuntimeException e)
			{
				logger.warn("ResourcesAction.createFiles ***** Unknown Exception ***** " + e.getMessage());
				alerts.add(rb.getString("failed"));
				continue outerloop;
			}

		}
		HashMap currentMap = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(currentMap == null)
		{
			// do nothing
		}
		else
		{
			if(!currentMap.containsKey(collectionId))
			{
				try
				{
					currentMap.put (collectionId,ContentHostingService.getCollection (collectionId));
					state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);

					// add this folder id into the set to be event-observed
					addObservingPattern(collectionId, state);
				}
				catch (IdUnusedException ignore)
				{
				}
				catch (TypeException ignore)
				{
				}
				catch (PermissionException ignore)
				{
				}
			}
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);
		}
		state.setAttribute(STATE_CREATE_ALERTS, alerts);

	}	// createFiles

	/**
	 * Process user's request to add an instance of a particular field to a structured object.
	 * @param data
	 */
	public static void doInsertValue(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		captureMultipleValues(state, params, false);

		Map current_stack_frame = peekAtStack(state);

		String field = params.getString("field");

		EditItem item = null;
		String mode = (String) state.getAttribute(STATE_MODE);
		if (MODE_CREATE.equals(mode))
		{
			int index = params.getInt("index");

			List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
			if(new_items != null)
			{
				item = (EditItem) new_items.get(index);
			}
		}
		else if(MODE_EDIT.equals(mode))
		{
			item = (EditItem) current_stack_frame.get(STATE_STACK_EDIT_ITEM);
		}

		if(item != null)
		{
			addInstance(field, item.getProperties());
		}

	}	// doInsertValue

	/**
	 * Search a flat list of ResourcesMetadata properties for one whose localname matches "field".
	 * If found and the field can have additional instances, increment the count for that item.
	 * @param field
	 * @param properties
	 * @return true if the field is found, false otherwise.
	 */
	protected static boolean addInstance(String field, List properties)
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

	public static void doAttachitem(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		String itemId = params.getString("itemId");

		Map current_stack_frame = peekAtStack(state);

		Object attach_links = current_stack_frame.get(STATE_ATTACH_LINKS);
		if(attach_links == null)
		{
			attach_links = state.getAttribute(STATE_ATTACH_LINKS);
			if(attach_links != null)
			{
				current_stack_frame.put(STATE_ATTACH_LINKS, attach_links);
			}
		}

		if(attach_links == null)
		{
			attachItem(itemId, state);
		}
		else
		{
			attachLink(itemId, state);
		}

		state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT_INIT);
		// popFromStack(state);
		// resetCurrentMode(state);

	}

	public static void doAttachupload(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		Map current_stack_frame = peekAtStack(state);

		String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
		int max_bytes = 1096 * 1096;
		try
		{
			max_bytes = Integer.parseInt(max_file_size_mb) * 1096 * 1096;
		}
		catch(Exception e)
		{
			// if unable to parse an integer from the value
			// in the properties file, use 1 MB as a default
			max_file_size_mb = "1";
			max_bytes = 1096 * 1096;
		}

		FileItem fileitem = null;
		try
		{
			fileitem = params.getFileItem("upload");
		}
		catch(Exception e)
		{

		}
		if(fileitem == null)
		{
			// "The user submitted a file to upload but it was too big!"
			addAlert(state, rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
		}
		else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
		{
			addAlert(state, rb.getString("choosefile7"));
		}
		else if (fileitem.getFileName().length() > 0)
		{
			String filename = Validator.getFileName(fileitem.getFileName());
			byte[] bytes = fileitem.get();
			String contentType = fileitem.getContentType();

			if(bytes.length >= max_bytes)
			{
				addAlert(state, rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
			}
			else if(bytes.length > 0)
			{
				// we just want the file name part - strip off any drive and path stuff
				String name = Validator.getFileName(filename);
				String resourceId = Validator.escapeResourceName(name);

				// make a set of properties to add for the new resource
				ResourcePropertiesEdit props = ContentHostingService.newResourceProperties();
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
				props.addProperty(ResourceProperties.PROP_DESCRIPTION, filename);

				// make an attachment resource for this URL
				try
				{
					String siteId = ToolManager.getCurrentPlacement().getContext();

					String toolName = (String) current_stack_frame.get(STATE_ATTACH_TOOL_NAME);
					if(toolName == null)
					{
						toolName = (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
						if(toolName == null)
						{
							toolName = ToolManager.getCurrentTool().getTitle();
						}
						current_stack_frame.put(STATE_ATTACH_TOOL_NAME, toolName);
					}

					ContentResource attachment = ContentHostingService.addAttachmentResource(resourceId, siteId, toolName, contentType, bytes, props);

					List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
					if(new_items == null)
					{
						new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
						if(new_items == null)
						{
							new_items = new Vector();
						}
						current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
					}

					String containerId = ContentHostingService.getContainingCollectionId (attachment.getId());
					String accessUrl = attachment.getUrl();

					AttachItem item = new AttachItem(attachment.getId(), filename, containerId, accessUrl);
					item.setContentType(contentType);
					new_items.add(item);
					//check -- jim
					state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());

					current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis4"));
				}
				catch(OverQuotaException e)
				{
					addAlert(state, rb.getString("overquota"));
				}
				catch(ServerOverloadException e)
				{
					addAlert(state, rb.getString("failed"));
				}
				catch(IdInvalidException ignore)
				{
					// other exceptions should be caught earlier
				}
				catch(InconsistentException ignore)
				{
					// other exceptions should be caught earlier
				}
				catch(IdUsedException ignore)
				{
					// other exceptions should be caught earlier
				}
				catch(RuntimeException e)
				{
					logger.warn("ResourcesAction.doAttachupload ***** Unknown Exception ***** " + e.getMessage());
					addAlert(state, rb.getString("failed"));
				}
			}
			else
			{
				addAlert(state, rb.getString("choosefile7"));
			}
		}

		state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT_INIT);
		//popFromStack(state);
		//resetCurrentMode(state);

	}	// doAttachupload

	public static void doAttachurl(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		Map current_stack_frame = peekAtStack(state);

		String url = params.getCleanString("url");

		ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties ();
		resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, url);
		resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, url);

		resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());

		try
		{
			url = validateURL(url);

			byte[] newUrl = url.getBytes();
			String newResourceId = Validator.escapeResourceName(url);

			String siteId = ToolManager.getCurrentPlacement().getContext();
			String toolName = (String) current_stack_frame.get(STATE_ATTACH_TOOL_NAME);
			if(toolName == null)
			{
				toolName = (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
				if(toolName == null)
				{
					toolName = ToolManager.getCurrentTool().getTitle();
				}
				current_stack_frame.put(STATE_ATTACH_TOOL_NAME, toolName);
			}

			ContentResource attachment = ContentHostingService.addAttachmentResource(newResourceId, siteId, toolName, ResourceProperties.TYPE_URL, newUrl, resourceProperties);

			List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
				if(new_items == null)
				{
					new_items = new Vector();
				}
				current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
			}

			String containerId = ContentHostingService.getContainingCollectionId (attachment.getId());
			String accessUrl = attachment.getUrl();

			AttachItem item = new AttachItem(attachment.getId(), url, containerId, accessUrl);
			item.setContentType(ResourceProperties.TYPE_URL);
			new_items.add(item);
			state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}
		catch(MalformedURLException e)
		{
			// invalid url
			addAlert(state, rb.getString("validurl") + " \"" + url + "\" " + rb.getString("invalid"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("notpermis4"));
		}
		catch(OverQuotaException e)
		{
			addAlert(state, rb.getString("overquota"));
		}
		catch(ServerOverloadException e)
		{
			addAlert(state, rb.getString("failed"));
		}
		catch(IdInvalidException ignore)
		{
			// other exceptions should be caught earlier
		}
		catch(IdUsedException ignore)
		{
			// other exceptions should be caught earlier
		}
		catch(InconsistentException ignore)
		{
			// other exceptions should be caught earlier
		}
		catch(RuntimeException e)
		{
			logger.warn("ResourcesAction.doAttachurl ***** Unknown Exception ***** " + e.getMessage());
			addAlert(state, rb.getString("failed"));
		}

		state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT_INIT);
		// popFromStack(state);
		// resetCurrentMode(state);

	}

	public static void doRemoveitem(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		Map current_stack_frame = peekAtStack(state);

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		String itemId = params.getString("itemId");

		List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
		if(new_items == null)
		{
			new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
			}
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}
		AttachItem item = null;
		boolean found = false;

		Iterator it = new_items.iterator();
		while(!found && it.hasNext())
		{
			item = (AttachItem) it.next();
			if(item.getId().equals(itemId))
			{
				found = true;
			}
		}

		if(found && item != null)
		{
			new_items.remove(item);
			List removed = (List) state.getAttribute(STATE_REMOVED_ATTACHMENTS);
			if(removed == null)
			{
				removed = new Vector();
				state.setAttribute(STATE_REMOVED_ATTACHMENTS, removed);
			}
			removed.add(item);

			state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
		}

	}	// doRemoveitem

	public static void doAddattachments(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

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

		Map current_stack_frame = peekAtStack(state);

		List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
		if(new_items == null)
		{
			new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
			}
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}
		List removed = (List) current_stack_frame.get(STATE_REMOVED_ATTACHMENTS);
		if(removed == null)
		{
			removed = (List) state.getAttribute(STATE_REMOVED_ATTACHMENTS);
			if(removed == null)
			{
				removed = new Vector();
			}
			current_stack_frame.put(STATE_REMOVED_ATTACHMENTS, removed);
		}
		Iterator removeIt = removed.iterator();
		while(removeIt.hasNext())
		{
			AttachItem item = (AttachItem) removeIt.next();
			try
			{
				if(ContentHostingService.isAttachmentResource(item.getId()))
				{
					ContentResourceEdit edit = ContentHostingService.editResource(item.getId());
					ContentHostingService.removeResource(edit);
					ContentCollectionEdit coll = ContentHostingService.editCollection(item.getCollectionId());
					ContentHostingService.removeCollection(coll);
				}
			}
			catch(Exception ignore)
			{
				// log failure
			}
		}
		state.removeAttribute(STATE_REMOVED_ATTACHMENTS);

		// add to the attachments vector
		List attachments = EntityManager.newReferenceList();

		Iterator it = new_items.iterator();
		while(it.hasNext())
		{
			AttachItem item = (AttachItem) it.next();

			try
			{
				Reference ref = EntityManager.newReference(ContentHostingService.getReference(item.getId()));
				attachments.add(ref);
			}
			catch(Exception e)
			{
			}
		}
		cleanupState(state);
		state.setAttribute(STATE_ATTACHMENTS, attachments);

		// end up in main mode
		popFromStack(state);
		resetCurrentMode(state);
		current_stack_frame = peekAtStack(state);

		String field = null;

		// if there is at least one attachment
		if (attachments.size() > 0)
		{
			//check -- jim
			state.setAttribute(AttachmentAction.STATE_HAS_ATTACHMENT_BEFORE, Boolean.TRUE);
			if(current_stack_frame == null)
			{
			
			}
			else
			{
				field = (String) current_stack_frame.get(STATE_ATTACH_FORM_FIELD);
			}
		}

		if(field != null)
		{
			int index = 0;
			String fieldname = field;
			Matcher matcher = INDEXED_FORM_FIELD_PATTERN.matcher(field.trim());
			if(matcher.matches())
			{
				fieldname = matcher.group(0);
				index = Integer.parseInt(matcher.group(1));
			}

			// we are trying to attach a link to a form field and there is at least one attachment
			if(new_items == null)
			{
				new_items = (List) current_stack_frame.get(ResourcesAction.STATE_HELPER_NEW_ITEMS);
				if(new_items == null)
				{
					new_items = (List) state.getAttribute(ResourcesAction.STATE_HELPER_NEW_ITEMS);
				}
			}
			EditItem edit_item = null;
			List edit_items = (List) current_stack_frame.get(ResourcesAction.STATE_STACK_CREATE_ITEMS);
			if(edit_items == null)
			{
				edit_item = (EditItem) current_stack_frame.get(ResourcesAction.STATE_STACK_EDIT_ITEM);
			}
			else
			{
				edit_item = (EditItem) edit_items.get(0);
			}
			if(edit_item != null)
			{
				Reference ref = (Reference) attachments.get(0);
				edit_item.setPropertyValue(fieldname, index, ref);
			}
		}
	}

	public static void attachItem(String itemId, SessionState state)
	{
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

		Map current_stack_frame = peekAtStack(state);

		List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
		if(new_items == null)
		{
			new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
			}
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}

		boolean found = false;
		Iterator it = new_items.iterator();
		while(!found && it.hasNext())
		{
			AttachItem item = (AttachItem) it.next();
			if(item.getId().equals(itemId))
			{
				found = true;
			}
		}

		if(!found)
		{
			try
			{
				ContentResource res = contentService.getResource(itemId);
				ResourceProperties props = res.getProperties();

				ResourcePropertiesEdit newprops = contentService.newResourceProperties();
				newprops.set(props);

				byte[] bytes = res.getContent();
				String contentType = res.getContentType();
				String filename = Validator.getFileName(itemId);
				String resourceId = Validator.escapeResourceName(filename);

				String siteId = ToolManager.getCurrentPlacement().getContext();
				String toolName = (String) current_stack_frame.get(STATE_ATTACH_TOOL_NAME);
				if(toolName == null)
				{
					toolName = (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
					if(toolName == null)
					{
						toolName = ToolManager.getCurrentTool().getTitle();
					}
					current_stack_frame.put(STATE_ATTACH_TOOL_NAME, toolName);
				}

				ContentResource attachment = ContentHostingService.addAttachmentResource(resourceId, siteId, toolName, contentType, bytes, props);

				String displayName = newprops.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				String containerId = contentService.getContainingCollectionId (attachment.getId());
				String accessUrl = attachment.getUrl();

				AttachItem item = new AttachItem(attachment.getId(), displayName, containerId, accessUrl);
				item.setContentType(contentType);
				new_items.add(item);
				state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("notpermis4"));
			}
			catch(OverQuotaException e)
			{
				addAlert(state, rb.getString("overquota"));
			}
			catch(ServerOverloadException e)
			{
				addAlert(state, rb.getString("failed"));
			}
			catch(IdInvalidException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(TypeException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(IdUnusedException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(IdUsedException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(InconsistentException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(RuntimeException e)
			{
				logger.warn("ResourcesAction.attachItem ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, rb.getString("failed"));
			}
		}
		current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
	}

	public static void attachLink(String itemId, SessionState state)
	{
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

		Map current_stack_frame = peekAtStack(state);

		List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
		if(new_items == null)
		{
			new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
			}
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}

		Integer max_cardinality = (Integer) current_stack_frame.get(STATE_ATTACH_CARDINALITY);
		if(max_cardinality == null)
		{
			max_cardinality = (Integer) state.getAttribute(STATE_ATTACH_CARDINALITY);
			if(max_cardinality == null)
			{
				max_cardinality = CARDINALITY_MULTIPLE;
			}
			current_stack_frame.put(STATE_ATTACH_CARDINALITY, max_cardinality);
		}

		boolean found = false;
		Iterator it = new_items.iterator();
		while(!found && it.hasNext())
		{
			AttachItem item = (AttachItem) it.next();
			if(item.getId().equals(itemId))
			{
				found = true;
			}
		}

		if(!found)
		{
			try
			{
				ContentResource res = contentService.getResource(itemId);
				ResourceProperties props = res.getProperties();

				String contentType = res.getContentType();
				String filename = Validator.getFileName(itemId);
				String resourceId = Validator.escapeResourceName(filename);

				String siteId = ToolManager.getCurrentPlacement().getContext();
				String toolName = (String) current_stack_frame.get(STATE_ATTACH_TOOL_NAME);
				if(toolName == null)
				{
					toolName = (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
					if(toolName == null)
					{
						toolName = ToolManager.getCurrentTool().getTitle();
					}
					current_stack_frame.put(STATE_ATTACH_TOOL_NAME, toolName);
				}

				String displayName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				String containerId = contentService.getContainingCollectionId (itemId);
				String accessUrl = res.getUrl();

				AttachItem item = new AttachItem(itemId, displayName, containerId, accessUrl);
				item.setContentType(contentType);
				new_items.add(item);
				state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("notpermis4"));
			}
			catch(TypeException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(IdUnusedException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(RuntimeException e)
			{
				logger.warn("ResourcesAction.attachItem ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, rb.getString("failed"));
			}
		}
		current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
	}

	/**
	 * Add a new URL to ContentHosting for each EditItem in the state attribute named STATE_STACK_CREATE_ITEMS.
	 * The number of items to be added is indicated by the state attribute named STATE_STACK_CREATE_NUMBER, and
	 * the items are added to the collection identified by the state attribute named STATE_STACK_CREATE_COLLECTION_ID.
	 * @param state
	 */
	protected static void createUrls(SessionState state)
	{
		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}

		Map current_stack_frame = peekAtStack(state);

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(number == null)
		{
			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}
		if(number == null)
		{
			number = new Integer(1);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}

		String collectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
		if(collectionId == null || collectionId.trim().length() == 0)
		{
			collectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			if(collectionId == null || collectionId.trim().length() == 0)
			{
				collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			}
			current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);
		}

		int numberOfItems = 1;
		numberOfItems = number.intValue();

		outerloop: for(int i = 0; i < numberOfItems; i++)
		{
			EditItem item = (EditItem) new_items.get(i);
			if(item.isBlank())
			{
				continue;
			}

			ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties ();
			resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, item.getName());
			resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, item.getDescription());

			resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());
			List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
			saveMetadata(resourceProperties, metadataGroups, item);

			byte[] newUrl = item.getFilename().getBytes();
			String name = Validator.escapeResourceName(item.getName());

			try
			{
				ContentResource resource = ContentHostingService.addResource (name,
																			collectionId,
																			MAXIMUM_ATTEMPTS_FOR_UNIQUENESS,
																			item.getMimeType(),
																			newUrl,
																			resourceProperties, item.getNotification());

				item.setAdded(true);

				Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
				if(preventPublicDisplay == null)
				{
					preventPublicDisplay = Boolean.FALSE;
					state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
				}
				
				if(preventPublicDisplay.equals(Boolean.FALSE))
				{
					if (! RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
					{
						if (!item.isPubviewset())
						{
							ContentHostingService.setPubView(resource.getId(), item.isPubview());
						}
					}
				}

				String mode = (String) state.getAttribute(STATE_MODE);
				if(MODE_HELPER.equals(mode))
				{
					String helper_mode = (String) state.getAttribute(STATE_RESOURCES_HELPER_MODE);
					if(helper_mode != null && MODE_ATTACHMENT_NEW_ITEM.equals(helper_mode))
					{
						// add to the attachments vector
						List attachments = EntityManager.newReferenceList();
						Reference ref = EntityManager.newReference(ContentHostingService.getReference(resource.getId()));
						attachments.add(ref);
						cleanupState(state);
						state.setAttribute(STATE_ATTACHMENTS, attachments);
					}
					else
					{
						Object attach_links = current_stack_frame.get(STATE_ATTACH_LINKS);
						if(attach_links == null)
						{
							attach_links = state.getAttribute(STATE_ATTACH_LINKS);
							if(attach_links != null)
							{
								current_stack_frame.put(STATE_ATTACH_LINKS, attach_links);
							}
						}

						if(attach_links == null)
						{
							attachItem(resource.getId(), state);
						}
						else
						{
							attachLink(resource.getId(), state);
						}
					}
				}

			}
			catch(PermissionException e)
			{
				alerts.add(rb.getString("notpermis12"));
				continue outerloop;
			}
			catch(IdInvalidException e)
			{
				alerts.add(rb.getString("title") + " " + e.getMessage ());
				continue outerloop;
			}
			catch(IdLengthException e)
			{
				alerts.add(rb.getString("toolong") + " " + e.getMessage());
				continue outerloop;
			}
			catch(IdUniquenessException e)
			{
				alerts.add("Could not add this item to this folder");
				continue outerloop;
			}
			catch(InconsistentException e)
			{
				alerts.add(RESOURCE_INVALID_TITLE_STRING);
				continue outerloop;
			}
			catch(OverQuotaException e)
			{
				alerts.add(rb.getString("overquota"));
				continue outerloop;
			}
			catch(ServerOverloadException e)
			{
				alerts.add(rb.getString("failed"));
				continue outerloop;
			}
			catch(RuntimeException e)
			{
				logger.warn("ResourcesAction.createFiles ***** Unknown Exception ***** " + e.getMessage());
				alerts.add(rb.getString("failed"));
				continue outerloop;
			}
		}

		HashMap currentMap = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(!currentMap.containsKey(collectionId))
		{
			try
			{
				currentMap.put (collectionId,ContentHostingService.getCollection (collectionId));
				state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);

				// add this folder id into the set to be event-observed
				addObservingPattern(collectionId, state);
			}
			catch (IdUnusedException ignore)
			{
			}
			catch (TypeException ignore)
			{
			}
			catch (PermissionException ignore)
			{
			}
		}
		state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);

		state.setAttribute(STATE_CREATE_ALERTS, alerts);

	}	// createUrls

	/**
	* Build the context for creating folders and items
	*/
	public static String buildCreateContext (VelocityPortlet portlet,
												Context context,
												RunData data,
												SessionState state)
	{
		context.put("tlang",rb);
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("TYPE_FOLDER", TYPE_FOLDER);
		context.put("TYPE_UPLOAD", TYPE_UPLOAD);
		context.put("TYPE_HTML", TYPE_HTML);
		context.put("TYPE_TEXT", TYPE_TEXT);
		context.put("TYPE_URL", TYPE_URL);
		context.put("TYPE_FORM", TYPE_FORM);
		
		context.put("SITE_ACCESS", AccessMode.SITE.toString());
		context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
		context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());

		context.put("max_upload_size", state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE));

		Map current_stack_frame = peekAtStack(state);

		String itemType = (String) current_stack_frame.get(STATE_STACK_CREATE_TYPE);
		if(itemType == null || itemType.trim().equals(""))
		{
			itemType = (String) state.getAttribute(STATE_CREATE_TYPE);
			if(itemType == null || itemType.trim().equals(""))
			{
				itemType = TYPE_UPLOAD;
			}
			current_stack_frame.put(STATE_STACK_CREATE_TYPE, itemType);
		}
		context.put("itemType", itemType);

		String field = (String) current_stack_frame.get(STATE_ATTACH_FORM_FIELD);
		if(field == null)
		{
			field = (String) state.getAttribute(STATE_ATTACH_FORM_FIELD);
			if(field != null)
			{
				current_stack_frame.put(STATE_ATTACH_FORM_FIELD, field);
				state.removeAttribute(STATE_ATTACH_FORM_FIELD);
			}
		}
		
		String msg = (String) state.getAttribute(STATE_CREATE_MESSAGE);
		if (msg != null)
		{
			context.put("createAlertMessage", msg);
			state.removeAttribute(STATE_CREATE_MESSAGE);
		}

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		if(new_items == null)
		{
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			String encoding = data.getRequest().getCharacterEncoding();

			new_items = new Vector();
			for(int i = 0; i < CREATE_MAX_ITEMS; i++)
			{
				EditItem item = new EditItem(itemType);
				if(encoding != null)
				{
					item.setEncoding(encoding);
				}
				item.setCopyrightStatus(defaultCopyrightStatus);
				new_items.add(item);
			}
			current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);
		}
		context.put("new_items", new_items);
		
		String collectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
		if(collectionId == null || collectionId.trim().length() == 0)
		{
			collectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			if(collectionId == null || collectionId.trim().length() == 0)
			{
				collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			}
			current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);
		}
		context.put("collectionId", collectionId);

		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(number == null)
		{
			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}
		context.put("numberOfItems", number);
		context.put("max_number", new Integer(CREATE_MAX_ITEMS));
		String homeCollectionId = (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
		context.put("homeCollectionId", homeCollectionId);
		List collectionPath = getCollectionPath(state);
		context.put ("collectionPath", collectionPath);

		if(homeCollectionId.equals(collectionId))
		{
			context.put("atHome", Boolean.TRUE.toString());
		}

		Collection groups = ContentHostingService.getGroupsWithReadAccess(collectionId);
		if(! groups.isEmpty())
		{
			context.put("siteHasGroups", Boolean.TRUE.toString());
			List theGroupsInThisSite = new Vector();
			for(int i = 0; i < CREATE_MAX_ITEMS; i++)
			{
				theGroupsInThisSite.add(groups.iterator());
			}
			context.put("theGroupsInThisSite", theGroupsInThisSite);
		}

		String show_form_items = (String) state.getAttribute(STATE_SHOW_FORM_ITEMS);
		if(show_form_items == null)
		{
			show_form_items = (String) state.getAttribute(STATE_SHOW_FORM_ITEMS);
			if(show_form_items != null)
			{
				current_stack_frame.put(STATE_SHOW_FORM_ITEMS,show_form_items);
			}
		}
		if(show_form_items != null)
		{
			context.put("show_form_items", show_form_items);
		}

		// copyright
		copyrightChoicesIntoContext(state, context);

		// put schema for metadata into context
		metadataGroupsIntoContext(state, context);

		// %%STATE_MODE_RESOURCES%%
		if (RESOURCES_MODE_RESOURCES.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			context.put("dropboxMode", Boolean.FALSE);
		}
		else if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// notshow the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
		}
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));

		/*
		Collection groups = ContentHostingService.getGroupsWithReadAccess(collectionId);
		if(! groups.isEmpty())
		{
			context.put("siteHasGroups", Boolean.TRUE.toString());
			context.put("theGroupsInThisSite", groups);
		}
		*/

		if(TYPE_FORM.equals(itemType))
		{
			List listOfHomes = (List) current_stack_frame.get(STATE_STRUCTOBJ_HOMES);
			if(listOfHomes == null)
			{
				setupStructuredObjects(state);
				listOfHomes = (List) current_stack_frame.get(STATE_STRUCTOBJ_HOMES);
			}
			context.put("homes", listOfHomes);

			String formtype = (String) current_stack_frame.get(STATE_STACK_STRUCTOBJ_TYPE);
			if(formtype == null)
			{
				formtype = (String) state.getAttribute(STATE_STRUCTOBJ_TYPE);
				if(formtype == null)
				{
					formtype = "";
				}
				current_stack_frame.put(STATE_STACK_STRUCTOBJ_TYPE, formtype);
			}
			context.put("formtype", formtype);

			String rootname = (String) current_stack_frame.get(STATE_STACK_STRUCTOBJ_ROOTNAME);
			context.put("rootname", rootname);

			context.put("STRING", ResourcesMetadata.WIDGET_STRING);
			context.put("TEXTAREA", ResourcesMetadata.WIDGET_TEXTAREA);
			context.put("BOOLEAN", ResourcesMetadata.WIDGET_BOOLEAN);
			context.put("INTEGER", ResourcesMetadata.WIDGET_INTEGER);
			context.put("DOUBLE", ResourcesMetadata.WIDGET_DOUBLE);
			context.put("DATE", ResourcesMetadata.WIDGET_DATE);
			context.put("TIME", ResourcesMetadata.WIDGET_TIME);
			context.put("DATETIME", ResourcesMetadata.WIDGET_DATETIME);
			context.put("ANYURI", ResourcesMetadata.WIDGET_ANYURI);
			context.put("ENUM", ResourcesMetadata.WIDGET_ENUM);
			context.put("NESTED", ResourcesMetadata.WIDGET_NESTED);
			context.put("WYSIWYG", ResourcesMetadata.WIDGET_WYSIWYG);

			context.put("today", TimeService.newTime());

			context.put("DOT", ResourcesMetadata.DOT);
		}
		Set missing = (Set) current_stack_frame.remove(STATE_CREATE_MISSING_ITEM);
		context.put("missing", missing);

		// String template = (String) getContext(data).get("template");
		return TEMPLATE_CREATE;

	}

	/**
	* show the resource properties
	*/
	public static void doMore ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		ParameterParser params = data.getParameters ();

		Map current_stack_frame = pushOnStack(state);

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

		// the hosted item ID
		String id = NULL_STRING;

		// the collection id
		String collectionId = NULL_STRING;

		try
		{
			id = params.getString ("id");
			if (id!=null)
			{
				// set the collection/resource id for more context
				current_stack_frame.put(STATE_MORE_ID, id);
			}
			else
			{
				// get collection/resource id from the state object
				id =(String) current_stack_frame.get(STATE_MORE_ID);
			}

			collectionId = params.getString ("collectionId");
			current_stack_frame.put(STATE_MORE_COLLECTION_ID, collectionId);

			if (collectionId.equals ((String) state.getAttribute(STATE_HOME_COLLECTION_ID)))
			{
				try
				{
					// this is a test to see if the collection exists.  If not, it is created.
					ContentCollection collection = ContentHostingService.getCollection (collectionId);
				}
				catch (IdUnusedException e )
				{
					try
					{
						// default copyright
						String mycopyright = (String) state.getAttribute (STATE_MY_COPYRIGHT);

						ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties ();
						String homeCollectionId = (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
						resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, ContentHostingService.getProperties (homeCollectionId).getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME));

						ContentCollection collection = ContentHostingService.addCollection (homeCollectionId, resourceProperties);
					}
					catch (IdUsedException ee)
					{
						addAlert(state, rb.getString("idused"));
					}
					catch (IdUnusedException ee)
					{
						addAlert(state,RESOURCE_NOT_EXIST_STRING);
					}
					catch (IdInvalidException ee)
					{
						addAlert(state, rb.getString("title") + " " + ee.getMessage ());
					}
					catch (PermissionException ee)
					{
						addAlert(state, rb.getString("permisex"));
					}
					catch (InconsistentException ee)
					{
						addAlert(state, RESOURCE_INVALID_TITLE_STRING);
					}
				}
				catch (TypeException e )
				{
					addAlert(state, rb.getString("typeex"));
				}
				catch (PermissionException e )
				{
					addAlert(state, rb.getString("permisex"));
				}
			}
		}
		catch (NullPointerException eE)
		{
			addAlert(state," " + rb.getString("nullex") + " " + id + ". ");
		}
		
		EditItem item = getEditItem(id, collectionId, data);
		
		

		// is there no error?
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// go to the more state
			state.setAttribute(STATE_MODE, MODE_MORE);

		}	// if-else

	}	// doMore

	/**
	* doDelete to delete the selected collection or resource items
	*/
	public void doDelete ( RunData data)
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

		List Items = (List) state.getAttribute(STATE_DELETE_ITEMS);

		// Vector deleteIds = (Vector) state.getAttribute (STATE_DELETE_IDS);

		// delete the lowest item in the hireachy first
		Hashtable deleteItems = new Hashtable();
		// String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		int maxDepth = 0;
		int depth = 0;

		Iterator it = Items.iterator();
		while(it.hasNext())
		{
			BrowseItem item = (BrowseItem) it.next();
			depth = ContentHostingService.getDepth(item.getId(), item.getRoot());
			if (depth > maxDepth)
			{
				maxDepth = depth;
			}
			List v = (List) deleteItems.get(new Integer(depth));
			if(v == null)
			{
				v = new Vector();
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
				v = new Vector();
			}
			Iterator itemIt = v.iterator();
			while(itemIt.hasNext())
			{
				BrowseItem item = (BrowseItem) itemIt.next();
				try
				{
					if (item.isFolder())
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
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
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
					logger.warn("ResourcesAction.doDelete ***** Unknown Exception ***** " + e.getMessage());
					addAlert(state, rb.getString("failed"));
				}
			}	// for

		}	// for

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// delete sucessful
			state.setAttribute (STATE_MODE, MODE_LIST);

			if (((String) state.getAttribute (STATE_SELECT_ALL_FLAG)).equals (Boolean.TRUE.toString()))
			{
				state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());
			}

		}	// if-else

	}	// doDelete

	/**
	* doCancel to return to the previous state
	*/
	public static void doCancel ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		if(!isStackEmpty(state))
		{
			Map current_stack_frame = peekAtStack(state);
			current_stack_frame.put(STATE_HELPER_CANCELED_BY_USER, Boolean.TRUE.toString());

			popFromStack(state);
		}

		resetCurrentMode(state);

	}	// doCancel

	/**
	* Paste the previously copied/cutted item(s)
	*/
	public void doHandlepaste ( RunData data)
	{
		ParameterParser params = data.getParameters ();

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// get the cut items to be pasted
		Vector pasteCutItems = (Vector) state.getAttribute (STATE_CUT_IDS);

		// get the copied items to be pasted
		Vector pasteCopiedItems = (Vector) state.getAttribute (STATE_COPIED_IDS);

		String collectionId = params.getString ("collectionId");
		String originalDisplayName = NULL_STRING;

		// handle cut and paste
		if (((String) state.getAttribute (STATE_CUT_FLAG)).equals (Boolean.TRUE.toString()))
		{
			for (int i = 0; i < pasteCutItems.size (); i++)
			{
				String currentPasteCutItem = (String) pasteCutItems.get (i);
				try
				{
					ResourceProperties properties = ContentHostingService.getProperties (currentPasteCutItem);

					originalDisplayName = properties.getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME);
					/*
					if (Boolean.TRUE.toString().equals(properties.getProperty (ResourceProperties.PROP_IS_COLLECTION)))
					{
						String alert = (String) state.getAttribute(STATE_MESSAGE);
						if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
						{
							addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
						}
					}
					else
					{
					*/
						// paste the resource
						ContentResource resource = ContentHostingService.getResource (currentPasteCutItem);
						ResourceProperties p = ContentHostingService.getProperties(currentPasteCutItem);
						String id = collectionId + Validator.escapeResourceName(p.getProperty(ResourceProperties.PROP_DISPLAY_NAME));

						// cut-paste to the same collection?
						boolean cutPasteSameCollection = false;
						String displayName = p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

						// till paste successfully or it fails
						ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties ();
						// add the properties of the pasted item
						Iterator propertyNames = properties.getPropertyNames ();
						while ( propertyNames.hasNext ())
						{
							String propertyName = (String) propertyNames.next ();
							if (!properties.isLiveProperty (propertyName))
							{
								if (propertyName.equals (ResourceProperties.PROP_DISPLAY_NAME)&&(displayName.length ()>0))
								{
									resourceProperties.addProperty (propertyName, displayName);
								}
								else
								{
									resourceProperties.addProperty (propertyName, properties.getProperty (propertyName));
								}	// if-else
							}	// if
						}	// while

						try
						{
							// paste the cutted resource to the new collection - no notification
							ContentResource newResource = ContentHostingService.addResource (id, resource.getContentType (), resource.getContent (), resourceProperties, NotificationService.NOTI_NONE);
							String uuid = ContentHostingService.getUuid(resource.getId());
							ContentHostingService.setUuid(id, uuid);
						}
						catch (InconsistentException e)
						{
							addAlert(state,RESOURCE_INVALID_TITLE_STRING);
						}
						catch (OverQuotaException e)
						{
							addAlert(state, rb.getString("overquota"));
						}
						catch (IdInvalidException e)
						{
							addAlert(state, rb.getString("title") + " " + e.getMessage ());
						}
						catch(ServerOverloadException e)
						{
							// this represents temporary unavailability of server's filesystem
							// for server configured to save resource body in filesystem
							addAlert(state, rb.getString("failed"));
						}
						catch (IdUsedException e)
						{
							// cut and paste to the same collection; stop adding new resource
							if (id.equals(currentPasteCutItem))
							{
								cutPasteSameCollection = true;
							}
							else
							{
								addAlert(state, rb.getString("notaddreso") + " " + id + rb.getString("used2"));
								/*
								// pasted to the same folder as before; add "Copy of "/ "copy (n) of" to the id
								if (countNumber==1)
								{
									displayName = DUPLICATE_STRING + p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
									id = collectionId + Validator.escapeResourceName(displayName);
								}
								else
								{
									displayName = "Copy (" + countNumber + ") of " + p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
									id = collectionId + Validator.escapeResourceName(displayName);
								}
								countNumber++;
								*/
							}
						}	// try-catch
						catch(RuntimeException e)
						{
							logger.warn("ResourcesAction.doHandlepaste ***** Unknown Exception ***** " + e.getMessage());
							addAlert(state, rb.getString("failed"));
						}

						if (!cutPasteSameCollection)
						{
							// remove the cutted resource
							ContentHostingService.removeResource (currentPasteCutItem);
						}

					// }	// if-else
				}
				catch (InUseException e)
				{
					addAlert(state, rb.getString("someone") + " " + originalDisplayName + ". ");
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis7") + " " + originalDisplayName + ". ");
				}
				catch (IdUnusedException e)
				{
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
				}
				catch (TypeException e)
				{
					addAlert(state, rb.getString("pasteitem") + " " + originalDisplayName + " " + rb.getString("mismatch"));
				}	// try-catch
				catch(RuntimeException e)
				{
					logger.warn("ResourcesAction.doHandlepaste ***** Unknown Exception ***** " + e.getMessage());
					addAlert(state, rb.getString("failed"));
				}

			}	// for
		}	// cut

		// handling copy and paste
		if (Boolean.toString(true).equalsIgnoreCase((String) state.getAttribute (STATE_COPY_FLAG)))
		{
			for (int i = 0; i < pasteCopiedItems.size (); i++)
			{
				String currentPasteCopiedItem = (String) pasteCopiedItems.get (i);
				try
				{
					ResourceProperties properties = ContentHostingService.getProperties (currentPasteCopiedItem);
					originalDisplayName = properties.getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME);
					// copy, cut and paste not operated on collections
					if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
					{
						String alert = (String) state.getAttribute(STATE_MESSAGE);
						if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
						{
							addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
						}
					}
					else
					{
						// paste the resource
						ContentResource resource = ContentHostingService.getResource (currentPasteCopiedItem);
						ResourceProperties p = ContentHostingService.getProperties(currentPasteCopiedItem);
						String displayName = DUPLICATE_STRING + p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						String id = collectionId + Validator.escapeResourceName(displayName);

						ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties ();

						// add the properties of the pasted item
						Iterator propertyNames = properties.getPropertyNames ();
						while ( propertyNames.hasNext ())
						{
							String propertyName = (String) propertyNames.next ();
							if (!properties.isLiveProperty (propertyName))
							{
								if (propertyName.equals (ResourceProperties.PROP_DISPLAY_NAME)&&(displayName.length ()>0))
								{
									resourceProperties.addProperty (propertyName, displayName);
								}
								else
								{
									resourceProperties.addProperty (propertyName, properties.getProperty (propertyName));
								}
							}
						}
						try
						{
							// paste the copied resource to the new collection
							ContentResource newResource = ContentHostingService.addResource (id, resource.getContentType (), resource.getContent (), resourceProperties, NotificationService.NOTI_NONE);
						}
						catch (InconsistentException e)
						{
							addAlert(state,RESOURCE_INVALID_TITLE_STRING);
						}
						catch (IdInvalidException e)
						{
							addAlert(state,rb.getString("title") + " " + e.getMessage ());
						}
						catch (OverQuotaException e)
						{
							addAlert(state, rb.getString("overquota"));
						}
						catch (ServerOverloadException e)
						{
							addAlert(state, rb.getString("failed"));
						}
						catch (IdUsedException e)
						{
							addAlert(state, rb.getString("notaddreso") + " " + id + rb.getString("used2"));
							/*
							// copying
							// pasted to the same folder as before; add "Copy of " to the id
							if (countNumber > 1)
							{
								displayName = "Copy (" + countNumber + ") of " + p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
							}
							else if (countNumber == 1)
							{
								displayName = "Copy of " + p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
							}
							id = collectionId + Validator.escapeResourceName(displayName);
							countNumber++;
							*/
						}	// try-catch
						catch(RuntimeException e)
						{
							logger.warn("ResourcesAction.doHandlepaste ***** Unknown Exception ***** " + e.getMessage());
							addAlert(state, rb.getString("failed"));
						}

					}	// if-else
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis8") + " " + originalDisplayName + ". ");
				}
				catch (IdUnusedException e)
				{
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
				}
				catch (TypeException e)
				{
					addAlert(state, rb.getString("pasteitem") + " " + originalDisplayName + " " + rb.getString("mismatch"));
				}	// try-catch

			}	// for
		}	// copy

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// delete sucessful
			state.setAttribute (STATE_MODE, MODE_LIST);

			// reset the cut flag
			if (((String)state.getAttribute (STATE_CUT_FLAG)).equals (Boolean.TRUE.toString()))
			{
				state.setAttribute (STATE_CUT_FLAG, Boolean.FALSE.toString());
			}

			// reset the copy flag
			if (Boolean.toString(true).equalsIgnoreCase((String)state.getAttribute (STATE_COPY_FLAG)))
			{
				state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());
			}

			// try to expand the collection
			HashMap expandedCollections = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			if(! expandedCollections.containsKey(collectionId))
			{
				org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
				try
				{
					ContentCollection coll = contentService.getCollection(collectionId);
					expandedCollections.put(collectionId, coll);
				}
				catch(Exception ignore){}
			}
		}

	}	// doHandlepaste

	/**
	* Paste the shortcut(s) of previously copied item(s)
	*/
	public void doHandlepasteshortcut ( RunData data)
	{
		ParameterParser params = data.getParameters ();

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// get the items to be pasted
		Vector pasteItems = new Vector ();

		if (((String) state.getAttribute (STATE_COPY_FLAG)).equals (Boolean.TRUE.toString()))
		{
			pasteItems = (Vector) ( (Vector) state.getAttribute (STATE_COPIED_IDS)).clone ();
		}
		if (((String) state.getAttribute (STATE_CUT_FLAG)).equals (Boolean.TRUE.toString()))
		{
			addAlert(state, rb.getString("choosecp"));
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			String collectionId = params.getString ("collectionId");

			String originalDisplayName = NULL_STRING;

			for (int i = 0; i < pasteItems.size (); i++)
			{
				String currentPasteItem = (String) pasteItems.get (i);

				try
				{
					ResourceProperties properties = ContentHostingService.getProperties (currentPasteItem);

					originalDisplayName = properties.getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME);

					if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
					{
						// paste the collection
					}
					else
					{
						// paste the resource
						ContentResource resource = ContentHostingService.getResource (currentPasteItem);
						ResourceProperties p = ContentHostingService.getProperties(currentPasteItem);
						String displayName = SHORTCUT_STRING + p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						String id = collectionId + Validator.escapeResourceName(displayName);

						//int countNumber = 2;
						ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties ();
						// add the properties of the pasted item
						Iterator propertyNames = properties.getPropertyNames ();
						while ( propertyNames.hasNext ())
						{
							String propertyName = (String) propertyNames.next ();
							if ((!properties.isLiveProperty (propertyName)) && (!propertyName.equals (ResourceProperties.PROP_DISPLAY_NAME)))
							{
								resourceProperties.addProperty (propertyName, properties.getProperty (propertyName));
							}
						}
						// %%%%% should be _blank for items that can be displayed in browser, _self for others
						// resourceProperties.addProperty (ResourceProperties.PROP_OPEN_NEWWINDOW, "_self");
						resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, displayName);

						try
						{
							ContentResource referedResource= ContentHostingService.getResource (currentPasteItem);
							ContentResource newResource = ContentHostingService.addResource (id, ResourceProperties.TYPE_URL, referedResource.getUrl().getBytes (), resourceProperties, NotificationService.NOTI_NONE);
						}
						catch (InconsistentException e)
						{
							addAlert(state, RESOURCE_INVALID_TITLE_STRING);
						}
						catch (OverQuotaException e)
						{
							addAlert(state, rb.getString("overquota"));
						}
						catch (IdInvalidException e)
						{
							addAlert(state, rb.getString("title") + " " + e.getMessage ());
						}
						catch (ServerOverloadException e)
						{
							addAlert(state, rb.getString("failed"));
						}
						catch (IdUsedException e)
						{
							addAlert(state, rb.getString("notaddreso") + " " + id + rb.getString("used2"));
							/*
							 // pasted shortcut to the same folder as before; add countNumber to the id
							displayName = "Shortcut (" + countNumber + ") to " + p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
							id = collectionId + Validator.escapeResourceName(displayName);
							countNumber++;
							*/
						}	// try-catch
						catch(RuntimeException e)
						{
							logger.warn("ResourcesAction.doHandlepasteshortcut ***** Unknown Exception ***** " + e.getMessage());
							addAlert(state, rb.getString("failed"));
						}
					}	// if-else
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis9") + " " +  currentPasteItem.substring (currentPasteItem.lastIndexOf (Entity.SEPARATOR)+1) + ". ");
				}
				catch (IdUnusedException e)
				{
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
				}
				catch (TypeException e)
				{
					addAlert(state, rb.getString("pasteitem") + " " +  currentPasteItem.substring (currentPasteItem.lastIndexOf (Entity.SEPARATOR)+1) + " " + rb.getString("mismatch"));
				}	// try-catch

			}	// for
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			if (((String) state.getAttribute (STATE_COPY_FLAG)).equals (Boolean.TRUE.toString()))
			{
				// reset the copy flag
				state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());
			}

			// paste shortcut sucessful
			state.setAttribute (STATE_MODE, MODE_LIST);
		}

	}	// doHandlepasteshortcut

	/**
	* Edit the editable collection/resource properties
	*/
	public static void doEdit ( RunData data )
	{
		ParameterParser params = data.getParameters ();
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		Map current_stack_frame = pushOnStack(state);

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

		String id = NULL_STRING;
		id = params.getString ("id");
		if(id == null || id.length() == 0)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile2"));
			return;
		}

		current_stack_frame.put(STATE_STACK_EDIT_ID, id);

		String collectionId = (String) params.getString("collectionId");
		if(collectionId == null)
		{
			collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			state.setAttribute(STATE_HOME_COLLECTION_ID, collectionId);
		}
		current_stack_frame.put(STATE_STACK_EDIT_COLLECTION_ID, collectionId);

		EditItem item = getEditItem(id, collectionId, data);

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// got resource and sucessfully populated item with values
			// state.setAttribute (STATE_MODE, MODE_EDIT);
			state.setAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_EDIT_ITEM_INIT);
			state.setAttribute(STATE_EDIT_ALERTS, new HashSet());
			current_stack_frame.put(STATE_STACK_EDIT_ITEM, item);

		}
		else
		{
			popFromStack(state);
		}

	}	// doEdit

	public static EditItem getEditItem(String id, String collectionId, RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);

		Map current_stack_frame = peekAtStack(state);

		EditItem item = null;

		// populate an EditItem object with values from the resource and return the EditItem
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

			item = new EditItem(id, itemName, itemType);
			
			BasicRightsAssignment rightsObj = new BasicRightsAssignment(item.getItemNum(), properties);
			item.setRights(rightsObj);

			String encoding = data.getRequest().getCharacterEncoding();
			if(encoding != null)
			{
				item.setEncoding(encoding);
			}

			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
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
			
			List access_groups = new Vector(((GroupAwareEntity) entity).getGroups());
			if(access_groups != null)
			{
				Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
				Iterator it = access_groups.iterator();
				while(it.hasNext())
				{
					String groupRef = (String) it.next();
					Group group = site.getGroup(groupRef);
					item.addGroup(group.getId());
				}
			}

			List inherited_access_groups = new Vector(((GroupAwareEntity) entity).getInheritedGroups());
			if(inherited_access_groups != null)
			{
				Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
				Iterator it = inherited_access_groups.iterator();
				while(it.hasNext())
				{
					String groupRef = (String) it.next();
					Group group = site.getGroup(groupRef);
					item.addGroup(group.getId());
				}
			}

			if(item.isUrl())
			{
				String url = new String(content);
				item.setFilename(url);
			}
			else if(item.isStructuredArtifact())
			{
				String formtype = properties.getProperty(ResourceProperties.PROP_STRUCTOBJ_TYPE);
				current_stack_frame.put(STATE_STACK_STRUCTOBJ_TYPE, formtype);
				current_stack_frame.put(STATE_STACK_EDIT_ITEM, item);
				setupStructuredObjects(state);
				Document doc = Xml.readDocumentFromString(new String(content));
				Element root = doc.getDocumentElement();
				importStructuredArtifact(root, item.getForm());
				List flatList = item.getForm().getFlatList();
				item.setProperties(flatList);
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
				size = properties.getPropertyFormatted(ResourceProperties.PROP_CONTENT_LENGTH) + " (" + Validator.getFileSizeWithDividor(properties.getProperty(ResourceProperties.PROP_CONTENT_LENGTH)) +" bytes)";
			}
			item.setSize(size);

			String copyrightStatus = properties.getProperty(properties.getNamePropCopyrightChoice());
			if(copyrightStatus == null || copyrightStatus.trim().equals(""))
			{
				copyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);

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

			boolean pubviewset = ContentHostingService.isInheritingPubView(containerId) || ContentHostingService.isPubView(containerId);
			item.setPubviewset(pubviewset);
			boolean pubview = pubviewset;
			if (!pubview)
			{
				pubview = ContentHostingService.isPubView(id);
			}
			item.setPubview(pubview);

			Map metadata = new Hashtable();
			List groups = (List) state.getAttribute(STATE_METADATA_GROUPS);
			if(groups != null && ! groups.isEmpty())
			{
				Iterator it = groups.iterator();
				while(it.hasNext())
				{
					MetadataGroup group = (MetadataGroup) it.next();
					Iterator propIt = group.iterator();
					while(propIt.hasNext())
					{
						ResourcesMetadata prop = (ResourcesMetadata) propIt.next();
						String name = prop.getFullname();
						String widget = prop.getWidget();
						if(widget.equals(ResourcesMetadata.WIDGET_DATE) || widget.equals(ResourcesMetadata.WIDGET_DATETIME) || widget.equals(ResourcesMetadata.WIDGET_TIME))
						{
							Time time = TimeService.newTime();
							try
							{
								time = properties.getTimeProperty(name);
							}
							catch(Exception ignore)
							{
								// use "now" as default in that case
							}
							metadata.put(name, time);
						}
						else
						{
							String value = properties.getPropertyFormatted(name);
							metadata.put(name, value);
						}
					}
				}
				item.setMetadata(metadata);
			}
			else
			{
				item.setMetadata(new Hashtable());
			}
			// for collections only
			if(item.isFolder())
			{
				// setup for quota - ADMIN only, site-root collection only
				if (SecurityService.isSuperUser())
				{
					Reference ref = EntityManager.newReference(entity.getReference());
					String context = ref.getContext();
					String siteCollectionId = ContentHostingService.getSiteCollection(context);
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
			addAlert(state, RESOURCE_NOT_EXIST_STRING);
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
			logger.warn("ResourcesAction.doEdit ***** Unknown Exception ***** " + e.getMessage());
			addAlert(state, rb.getString("failed"));
		}

		return item;

	}

	/**
	 * This method updates the session state with information needed to create or modify
	 * structured artifacts in the resources tool.  Among other things, it obtains a list
	 * of "forms" available to the user and places that list in state indexed as
	 * "STATE_STRUCTOBJ_HOMES".  If the current formtype is known (in state indexed as
	 * "STATE_STACK_STRUCTOBJ_TYPE"), the list of properties associated with that form type is
	 * generated.  If we are in a "create" context, the properties are added to each of
	 * the items in the list of items indexed as "STATE_STACK_CREATE_ITEMS".  If we are in an
	 * "edit" context, the properties are added to the current item being edited (a state
	 * attribute indexed as "STATE_STACK_EDIT_ITEM").  The metaobj SchemaBean associated with
	 * the current form and its root SchemaNode object are also placed in state for later
	 * reference.
	 */
	public static void setupStructuredObjects(SessionState state)
	{
		Map current_stack_frame = peekAtStack(state);

		String formtype = (String) current_stack_frame.get(STATE_STACK_STRUCTOBJ_TYPE);
		if(formtype == null)
		{
			formtype = (String) state.getAttribute(STATE_STRUCTOBJ_TYPE);
			if(formtype == null)
			{
				formtype = "";
			}
			current_stack_frame.put(STATE_STACK_STRUCTOBJ_TYPE, formtype);
		}

		HomeFactory factory = (HomeFactory) ComponentManager.get("homeFactory");

		Map homes = factory.getHomes(StructuredArtifactHomeInterface.class);
		List listOfHomes = new Vector();
		Iterator it = homes.keySet().iterator();
		while(it.hasNext())
		{
			String key = (String) it.next();
			try
			{
				Object obj = homes.get(key);
				listOfHomes.add(obj);
			}
			catch(Exception ignore)
			{}
		}
		current_stack_frame.put(STATE_STRUCTOBJ_HOMES, listOfHomes);

		StructuredArtifactHomeInterface home = null;
		SchemaBean rootSchema = null;
		ResourcesMetadata elements = null;

		if(formtype == null || formtype.equals(""))
		{
			formtype = "";
			current_stack_frame.put(STATE_STACK_STRUCTOBJ_TYPE, formtype);
		}
		else if(listOfHomes.isEmpty())
		{
			// hmmm
		}
		else
		{
			try
			{
				home = (StructuredArtifactHomeInterface) factory.getHome(formtype);
			}
			catch(NullPointerException ignore)
			{
				home = null;
			}
		}

		if(home != null)
		{
			rootSchema = new SchemaBean(home.getRootNode(), home.getSchema(), formtype, home.getType().getDescription());
			List fields = rootSchema.getFields();
			String docRoot = rootSchema.getFieldName();
			elements = new ResourcesMetadata("", docRoot, "", "", ResourcesMetadata.WIDGET_NESTED, ResourcesMetadata.WIDGET_NESTED);
			elements.setDottedparts(docRoot);
			elements.setContainer(null);

			elements = createHierarchicalList(elements, fields, 1);

			String instruction = home.getInstruction();

			current_stack_frame.put(STATE_STACK_STRUCTOBJ_ROOTNAME, docRoot);
			current_stack_frame.put(STATE_STACK_STRUCT_OBJ_SCHEMA, rootSchema);

			List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
			if(new_items != null)
			{
				Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
				if(number == null)
				{
					number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
					current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
				}
				if(number == null)
				{
					number = new Integer(1);
					current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
				}
				List flatList = elements.getFlatList();

				for(int i = 0; i < number.intValue(); i++)
				{
					//%%%%% doing this wipes out data that's been stored previously

					EditItem item = (EditItem) new_items.get(i);
					item.setRootname(docRoot);
					item.setFormtype(formtype);
					item.setInstruction(instruction);
					item.setProperties(flatList);
					item.setForm(elements);
				}
				current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);
			}
			else if(current_stack_frame.get(STATE_STACK_EDIT_ITEM) != null)
			{
				EditItem item = (EditItem) current_stack_frame.get(STATE_STACK_EDIT_ITEM);
				item.setRootname(docRoot);
				item.setFormtype(formtype);
				item.setInstruction(instruction);
				item.setForm(elements);
			}
		}

	}	// setupStructuredArtifacts

	/**
	 * This method navigates through a list of SchemaNode objects representing fields in a form,
	 * creates a ResourcesMetadata object for each field and adds those as nested fields within
	 * a root element.  If a field contains nested fields, a recursive call adds nested fields
	 * in the corresponding ResourcesMetadata object.
	 * @param element The root element to which field descriptions are added.
	 * @param fields A list of metaobj SchemaNode objects.
	 * @param depth The depth of nesting, corresponding to the amount of indent that will be used
	 * when displaying the list.
	 * @return The update root element.
	 */
	private static ResourcesMetadata createHierarchicalList(ResourcesMetadata element, List fields, int depth)
	{
		List properties = new Vector();
		for(Iterator fieldIt = fields.iterator(); fieldIt.hasNext(); )
		{
			SchemaBean field = (SchemaBean) fieldIt.next();
			SchemaNode node = field.getSchema();
			Map annotations = field.getAnnotations();
			Pattern pattern = null;
			String localname = field.getFieldName();
			String description = field.getDescription();
			String label = (String) annotations.get("label");
			if(label == null || label.trim().equals(""))
			{
				label = description;
			}

			String richText = (String) annotations.get("isRichText");
			boolean isRichText = richText != null && richText.equalsIgnoreCase(Boolean.TRUE.toString());

			Class javaclass = node.getObjectType();
			String typename = javaclass.getName();
			String widget = ResourcesMetadata.WIDGET_STRING;
			int length =  0;
			List enumerals = null;

			if(field.getFields().size() > 0)
			{
				widget = ResourcesMetadata.WIDGET_NESTED;
			}
			else if(node.hasEnumerations())
			{
				enumerals = node.getEnumeration();
				typename = String.class.getName();
				widget = ResourcesMetadata.WIDGET_ENUM;
			}
			else if(typename.equals(String.class.getName()))
			{
				length = node.getType().getMaxLength();
				String baseType = node.getType().getBaseType();

				if(isRichText)
				{
					widget = ResourcesMetadata.WIDGET_WYSIWYG;
				}
				else if(baseType.trim().equalsIgnoreCase(ResourcesMetadata.NAMESPACE_XSD_ABBREV + ResourcesMetadata.XSD_NORMALIZED_STRING))
				{
					widget = ResourcesMetadata.WIDGET_STRING;
					if(length > 50)
					{
						length = 50;
					}
				}
				else if(length > 100 || length < 1)
				{
					widget = ResourcesMetadata.WIDGET_TEXTAREA;
				}
				else if(length > 50)
				{
					length = 50;
				}

				pattern = node.getType().getPattern();
			}
			else if(typename.equals(Date.class.getName()))
			{
				widget = ResourcesMetadata.WIDGET_DATE;
			}
			else if(typename.equals(Boolean.class.getName()))
			{
				widget = ResourcesMetadata.WIDGET_BOOLEAN;
			}
			else if(typename.equals(URI.class.getName()))
			{
				widget = ResourcesMetadata.WIDGET_ANYURI;
			}
			else if(typename.equals(Number.class.getName()))
			{
				widget = ResourcesMetadata.WIDGET_INTEGER;

				//length = node.getType().getTotalDigits();
				length = INTEGER_WIDGET_LENGTH;
			}
			else if(typename.equals(Double.class.getName()))
			{
				widget = ResourcesMetadata.WIDGET_DOUBLE;
				length = DOUBLE_WIDGET_LENGTH;
			}
			int minCard = node.getMinOccurs();
			int maxCard = node.getMaxOccurs();
			if(maxCard < 1)
			{
				maxCard = Integer.MAX_VALUE;
			}
			if(minCard < 0)
			{
				minCard = 0;
			}
			minCard = java.lang.Math.max(0,minCard);
			maxCard = java.lang.Math.max(1,maxCard);
			int currentCount = java.lang.Math.min(java.lang.Math.max(1,minCard),maxCard);

			ResourcesMetadata prop = new ResourcesMetadata(element.getDottedname(), localname, label, description, typename, widget);
			List parts = new Vector(element.getDottedparts());
			parts.add(localname);
			prop.setDottedparts(parts);
			prop.setContainer(element);
			if(ResourcesMetadata.WIDGET_NESTED.equals(widget))
			{
				prop = createHierarchicalList(prop, field.getFields(), depth + 1);
			}
			prop.setMinCardinality(minCard);
			prop.setMaxCardinality(maxCard);
			prop.setCurrentCount(currentCount);
			prop.setDepth(depth);

			if(enumerals != null)
			{
				prop.setEnumeration(enumerals);
			}
			if(length > 0)
			{
				prop.setLength(length);
			}

			if(pattern != null)
			{
				prop.setPattern(pattern);
			}

			properties.add(prop);
		}

		element.setNested(properties);

		return element;

	}	// createHierarchicalList

	/**
	 * This method captures property values from an org.w3c.dom.Document and inserts them
	 * into a hierarchical list of ResourcesMetadata objects which describes the structure
	 * of the form.  The values are added by inserting nested instances into the properties.
	 *
	 * @param element	An org.w3c.dom.Element containing values to be imported.
	 * @param properties	A hierarchical list of ResourcesMetadata objects describing a form
	 */
	public static void importStructuredArtifact(Node node, ResourcesMetadata property)
	{
		if(property == null || node == null)
		{
			return;
		}

		String tagname = property.getLocalname();
		String nodename = node.getLocalName();
		if(! tagname.equals(nodename))
		{
			// return;
		}

		if(property.getNested().size() == 0)
		{
			boolean value_found = false;
			Node child = node.getFirstChild();
			while(! value_found && child != null)
			{
				if(child.getNodeType() == Node.TEXT_NODE)
				{
					Text value = (Text) child;
					if(ResourcesMetadata.WIDGET_DATE.equals(property.getWidget()) || ResourcesMetadata.WIDGET_DATETIME.equals(property.getWidget()) || ResourcesMetadata.WIDGET_TIME.equals(property.getWidget()))
					{
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						Time time = TimeService.newTime();
						try
						{
							Date date = df.parse(value.getData());
							time = TimeService.newTime(date.getTime());
						}
						catch(Exception ignore)
						{
							// use "now" as default in that case
						}
						property.setValue(0, time);
					}
					else if(ResourcesMetadata.WIDGET_ANYURI.equals(property.getWidget()))
					{
						Reference ref = EntityManager.newReference(ContentHostingService.getReference(value.getData()));
						property.setValue(0, ref);
					}
					else
					{
						property.setValue(0, value.getData());
					}
				}
				child = child.getNextSibling();
			}
		}
		else if(node instanceof Element)
		{
			// a nested element
			Iterator nestedIt = property.getNested().iterator();
			while(nestedIt.hasNext())
			{
				ResourcesMetadata prop = (ResourcesMetadata) nestedIt.next();
				NodeList nodes = ((Element) node).getElementsByTagName(prop.getLocalname());
				if(nodes == null)
				{
					continue;
				}
				for(int i = 0; i < nodes.getLength(); i++)
				{
					Node n = nodes.item(i);
					if(n != null)
					{
						ResourcesMetadata instance = prop.addInstance();
						if(instance != null)
						{
							importStructuredArtifact(n, instance);
						}
					}
				}
			}
		}

	}	// importStructuredArtifact

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
						// "http" protocol and accept input it that validates.
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
	 * Retrieve values for an item from edit context.  Edit context contains just one item at a time of a known type
	 * (folder, file, text document, structured-artifact, etc).  This method retrieves the data apppropriate to the
	 * type and updates the values of the EditItem stored as the STATE_STACK_EDIT_ITEM attribute in state.
	 * @param state
	 * @param params
	 * @param item
	 */
	protected static void captureValues(SessionState state, ParameterParser params)
	{
		Map current_stack_frame = peekAtStack(state);

		EditItem item = (EditItem) current_stack_frame.get(STATE_STACK_EDIT_ITEM);
		Set alerts = (Set) state.getAttribute(STATE_EDIT_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_EDIT_ALERTS, alerts);
		}
		String flow = params.getString("flow");
		boolean intentChanged = "intentChanged".equals(flow);
		String check_fileName = params.getString("check_fileName");
		boolean expectFile = "true".equals(check_fileName);
		String intent = params.getString("intent");
		String oldintent = (String) current_stack_frame.get(STATE_STACK_EDIT_INTENT);
		boolean upload_file = expectFile && item.isFileUpload() || ((item.isHtml() || item.isPlaintext()) && !intentChanged && INTENT_REPLACE_FILE.equals(intent) && INTENT_REPLACE_FILE.equals(oldintent));
		boolean revise_file = (item.isHtml() || item.isPlaintext()) && !intentChanged && INTENT_REVISE_FILE.equals(intent) && INTENT_REVISE_FILE.equals(oldintent);

		String name = params.getString("name");
		if(name == null || "".equals(name.trim()))
		{
			alerts.add(rb.getString("titlenotnull"));
			// addAlert(state, rb.getString("titlenotnull"));
		}
		else
		{
			item.setName(name.trim());
		}

		String description = params.getString("description");
		if(description == null)
		{
			item.setDescription("");
		}
		else
		{
			item.setDescription(description);
		}

		item.setContentHasChanged(false);

		if(upload_file)
		{
			String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
			int max_bytes = 1096 * 1096;
			try
			{
				max_bytes = Integer.parseInt(max_file_size_mb) * 1096 * 1096;
			}
			catch(Exception e)
			{
				// if unable to parse an integer from the value
				// in the properties file, use 1 MB as a default
				max_file_size_mb = "1";
				max_bytes = 1096 * 1096;
			}
			/*
			 // params.getContentLength() returns m_req.getContentLength()
			if(params.getContentLength() >= max_bytes)
			{
				alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
			}
			else
			*/
			{
				// check for file replacement
				FileItem fileitem = params.getFileItem("fileName");
				if(fileitem == null)
				{
					// "The user submitted a file to upload but it was too big!"
					alerts.clear();
					alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
					//item.setMissing("fileName");
				}
				else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
				{
					if(item.getContent() == null || item.getContent().length <= 0)
					{
						// "The user submitted the form, but didn't select a file to upload!"
						alerts.add(rb.getString("choosefile") + ". ");
						//item.setMissing("fileName");
					}
				}
				else if (fileitem.getFileName().length() > 0)
				{
					String filename = Validator.getFileName(fileitem.getFileName());
					byte[] bytes = fileitem.get();
					String contenttype = fileitem.getContentType();

					if(bytes.length >= max_bytes)
					{
						alerts.clear();
						alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
						// item.setMissing("fileName");
					}
					else if(bytes.length > 0)
					{
						item.setContent(bytes);
						item.setContentHasChanged(true);
						item.setMimeType(contenttype);
						item.setFilename(filename);
					}
				}
			}
		}
		else if(revise_file)
		{
			// check for input from editor (textarea)
			String content = params.getString("content");
			if(content != null)
			{
				item.setContent(content);
				item.setContentHasChanged(true);
			}
		}
		else if(item.isUrl())
		{
			String url = params.getString("Url");
			if(url == null || url.trim().equals(""))
			{
				item.setFilename("");
				alerts.add(rb.getString("validurl"));
			}
			else
			{
				// valid protocol?
				item.setFilename(url);
				try
				{
					// test format of input
					URL u = new URL(url);
				}
				catch (MalformedURLException e1)
				{
					try
					{
						// if URL did not validate, check whether the problem was an
						// unrecognized protocol, and accept input if that's the case.
						Pattern pattern = Pattern.compile("\\s*([a-zA-Z0-9]+)://([^\\n]+)");
						Matcher matcher = pattern.matcher(url);
						if(matcher.matches())
						{
							URL test = new URL("http://" + matcher.group(2));
						}
						else
						{
							url = "http://" + url;
							URL test = new URL(url);
							item.setFilename(url);
						}
					}
					catch (MalformedURLException e2)
					{
						// invalid url
						alerts.add(rb.getString("validurl"));
					}
				}
			}
		}
		else if(item.isFolder())
		{
			if(item.canSetQuota())
			{
				// read the quota fields
				String setQuota = params.getString("setQuota");
				boolean hasQuota = params.getBoolean("hasQuota");
				item.setHasQuota(hasQuota);
				if(hasQuota)
				{
					int q = params.getInt("quota");
					item.setQuota(Integer.toString(q));
				}
			}
		}
		else if(item.isStructuredArtifact())
		{
			String formtype = (String) current_stack_frame.get(STATE_STACK_STRUCTOBJ_TYPE);
			if(formtype == null)
			{
				formtype = (String) state.getAttribute(STATE_STRUCTOBJ_TYPE);
				if(formtype == null)
				{
					formtype = "";
				}
				current_stack_frame.put(STATE_STACK_STRUCTOBJ_TYPE, formtype);
			}
			String formtype_check = params.getString("formtype");

			if(formtype_check == null || formtype_check.equals(""))
			{
				alerts.add(rb.getString("type"));
				item.setMissing("formtype");
			}
			else if(formtype_check.equals(formtype))
			{
				item.setFormtype(formtype);
				capturePropertyValues(params, item, item.getProperties());
			}
		}

		if(! item.isFolder() && ! item.isStructuredArtifact() && ! item.isUrl())
		{
			String mime_category = params.getString("mime_category");
			String mime_subtype = params.getString("mime_subtype");

			if(mime_category != null && mime_subtype != null)
			{
				String mimetype = mime_category + "/" + mime_subtype;
				if(! mimetype.equals(item.getMimeType()))
				{
					item.setMimeType(mimetype);
					item.setContentTypeHasChanged(true);
				}
			}
		}

		if(item.isFileUpload() || item.isHtml() || item.isPlaintext())
		{
			BasicRightsAssignment rightsObj = item.getRights();
			rightsObj.captureValues(params);

			boolean usingCreativeCommons = state.getAttribute(STATE_USING_CREATIVE_COMMONS) != null && state.getAttribute(STATE_USING_CREATIVE_COMMONS).equals(Boolean.TRUE.toString());		
			
			if(usingCreativeCommons)
			{
				String ccOwnership = params.getString("ccOwnership");
				if(ccOwnership != null)
				{
					item.setRightsownership(ccOwnership);
				}
				String ccTerms = params.getString("ccTerms");
				if(ccTerms != null)
				{
					item.setLicense(ccTerms);
				}
				String ccCommercial = params.getString("ccCommercial");
				if(ccCommercial != null)
				{
					item.setAllowCommercial(ccCommercial);
				}
				String ccModification = params.getString("ccModification");
				if(ccCommercial != null)
				{
					item.setAllowModifications(ccModification);
				}
				String ccRightsYear = params.getString("ccRightsYear");
				if(ccRightsYear != null)
				{
					item.setRightstyear(ccRightsYear);
				}
				String ccRightsOwner = params.getString("ccRightsOwner");
				if(ccRightsOwner != null)
				{
					item.setRightsowner(ccRightsOwner);
				}

				/*
				ccValues.ccOwner = new Array();
				ccValues.myRights = new Array();
				ccValues.otherRights = new Array();
				ccValues.ccCommercial = new Array();
				ccValues.ccModifications = new Array();
				ccValues.ccRightsYear = new Array();
				ccValues.ccRightsOwner = new Array();
				*/
			}
			else
			{
				// check for copyright status
				// check for copyright info
				// check for copyright alert
	
				String copyrightStatus = StringUtil.trimToNull(params.getString ("copyrightStatus"));
				String copyrightInfo = StringUtil.trimToNull(params.getCleanString ("copyrightInfo"));
				String copyrightAlert = StringUtil.trimToNull(params.getString("copyrightAlert"));
	
				if (copyrightStatus != null)
				{
					if (state.getAttribute(COPYRIGHT_NEW_COPYRIGHT) != null && copyrightStatus.equals(state.getAttribute(COPYRIGHT_NEW_COPYRIGHT)))
					{
						if (copyrightInfo != null)
						{
							item.setCopyrightInfo( copyrightInfo );
						}
						else
						{
							alerts.add(rb.getString("specifycp2"));
							// addAlert(state, rb.getString("specifycp2"));
						}
					}
					else if (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT) != null && copyrightStatus.equals (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT)))
					{
						item.setCopyrightInfo((String) state.getAttribute (STATE_MY_COPYRIGHT));
					}
	
					item.setCopyrightStatus( copyrightStatus );
				}
				item.setCopyrightAlert(copyrightAlert != null);
			}
		}
		
		String access_mode = params.getString("access_mode");
		if(access_mode != null)
		{
			item.setAccess(access_mode);
			if(AccessMode.GROUPED.toString().equals(access_mode))
			{
				String xxx = params.getString("access_groups");

				String[] access_groups = params.getStrings("access_groups");
				item.clearGroups();
				for(int gr = 0; gr < access_groups.length; gr++)
				{
					item.addGroup(access_groups[gr]);
				}
			}
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		if(preventPublicDisplay.equals(Boolean.FALSE))
		{
			boolean pubviewset = item.isPubviewset();
			boolean pubview = false;
			if (! RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
			{
				if (!pubviewset)
				{
					pubview = params.getBoolean("pubview");
					item.setPubview(pubview);
				}
			}
		}

		int noti = NotificationService.NOTI_NONE;
		// %%STATE_MODE_RESOURCES%%
		if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// set noti to none if in dropbox mode
			noti = NotificationService.NOTI_NONE;
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
		item.setNotification(noti);

		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups != null && ! metadataGroups.isEmpty())
		{
			Iterator groupIt = metadataGroups.iterator();
			while(groupIt.hasNext())
			{
				MetadataGroup group = (MetadataGroup) groupIt.next();
				if(group.isShowing())
				{
					Iterator propIt = group.iterator();
					while(propIt.hasNext())
					{
						ResourcesMetadata prop = (ResourcesMetadata) propIt.next();
						String propname = prop.getFullname();
						if(ResourcesMetadata.WIDGET_DATE.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_DATETIME.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_TIME.equals(prop.getWidget()))
						{
							int year = 0;
							int month = 0;
							int day = 0;
							int hour = 0;
							int minute = 0;
							int second = 0;
							int millisecond = 0;
							String ampm = "";

							if(prop.getWidget().equals(ResourcesMetadata.WIDGET_DATE) ||
								prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
							{
								year = params.getInt(propname + "_year", year);
								month = params.getInt(propname + "_month", month);
								day = params.getInt(propname + "_day", day);


							}
							if(prop.getWidget().equals(ResourcesMetadata.WIDGET_TIME) ||
								prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
							{
								hour = params.getInt(propname + "_hour", hour);
								minute = params.getInt(propname + "_minute", minute);
								second = params.getInt(propname + "_second", second);
								millisecond = params.getInt(propname + "_millisecond", millisecond);
								ampm = params.getString(propname + "_ampm").trim();

								if("pm".equalsIgnoreCase("ampm"))
								{
									if(hour < 12)
									{
										hour += 12;
									}
								}
								else if(hour == 12)
								{
									hour = 0;
								}
							}
							if(hour > 23)
							{
								hour = hour % 24;
								day++;
							}

							Time value = TimeService.newTimeLocal(year, month, day, hour, minute, second, millisecond);
							item.setMetadataItem(propname,value);

						}
						else
						{

							String value = params.getString(propname);
							if(value != null)
							{
								item.setMetadataItem(propname, value);
							}
						}
					}
				}
			}
		}
		current_stack_frame.put(STATE_STACK_EDIT_ITEM, item);
		state.setAttribute(STATE_EDIT_ALERTS, alerts);

	}	// captureValues

	/**
	 * Retrieve from an html form all the values needed to create a new resource
	 * @param item The EditItem object in which the values are temporarily stored.
	 * @param index The index of the item (used as a suffix in the name of the form element)
	 * @param state
	 * @param params
	 * @param markMissing Indicates whether to mark required elements if they are missing.
	 * @return
	 */
	public static Set captureValues(EditItem item, int index, SessionState state, ParameterParser params, boolean markMissing)
	{
		Map current_stack_frame = peekAtStack(state);

		Set item_alerts = new HashSet();
		boolean blank_entry = true;
		item.clearMissing();

		String name = params.getString("name" + index);
		if(name == null || name.trim().equals(""))
		{
			if(markMissing)
			{
				item_alerts.add(rb.getString("titlenotnull"));
				item.setMissing("name");
			}
			item.setName("");
			// addAlert(state, rb.getString("titlenotnull"));
		}
		else
		{
			item.setName(name);
			blank_entry = false;
		}

		String description = params.getString("description" + index);
		if(description == null || description.trim().equals(""))
		{
			item.setDescription("");
		}
		else
		{
			item.setDescription(description);
			blank_entry = false;
		}

		item.setContentHasChanged(false);

		if(item.isFileUpload())
		{
			String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
			int max_bytes = 1096 * 1096;
			try
			{
				max_bytes = Integer.parseInt(max_file_size_mb) * 1096 * 1096;
			}
			catch(Exception e)
			{
				// if unable to parse an integer from the value
				// in the properties file, use 1 MB as a default
				max_file_size_mb = "1";
				max_bytes = 1096 * 1096;
			}
			/*
			 // params.getContentLength() returns m_req.getContentLength()
			if(params.getContentLength() >= max_bytes)
			{
				item_alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
			}
			else
			*/
			{
				// check for file replacement
				FileItem fileitem = null;
				try
				{
					fileitem = params.getFileItem("fileName" + index);
				}
				catch(Exception e)
				{
					// this is an error in Firefox, Mozilla and Netscape
					// "The user didn't select a file to upload!"
					if(item.getContent() == null || item.getContent().length <= 0)
					{
						item_alerts.add(rb.getString("choosefile") + " " + (index + 1) + ". ");
						item.setMissing("fileName");
					}
				}
				if(fileitem == null)
				{
					// "The user submitted a file to upload but it was too big!"
					item_alerts.clear();
					item_alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
					item.setMissing("fileName");
				}
				else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
				{
					if(item.getContent() == null || item.getContent().length <= 0)
					{
						// "The user submitted the form, but didn't select a file to upload!"
						item_alerts.add(rb.getString("choosefile") + " " + (index + 1) + ". ");
						item.setMissing("fileName");
					}
				}
				else if (fileitem.getFileName().length() > 0)
				{
					String filename = Validator.getFileName(fileitem.getFileName());
					byte[] bytes = fileitem.get();
					String contenttype = fileitem.getContentType();

					if(bytes.length >= max_bytes)
					{
						item_alerts.clear();
						item_alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
						item.setMissing("fileName");
					}
					else if(bytes.length > 0)
					{
						item.setContent(bytes);
						item.setContentHasChanged(true);
						item.setMimeType(contenttype);
						item.setFilename(filename);
						blank_entry = false;
					}
					else
					{
						item_alerts.add(rb.getString("choosefile") + " " + (index + 1) + ". ");
						item.setMissing("fileName");
					}
				}

			}
		}
		else if(item.isPlaintext())
		{
			// check for input from editor (textarea)
			String content = params.getString("content" + index);
			if(content != null)
			{
				item.setContentHasChanged(true);
				item.setContent(content);
				blank_entry = false;
			}
			item.setMimeType(MIME_TYPE_DOCUMENT_PLAINTEXT);
		}
		else if(item.isHtml())
		{
			// check for input from editor (textarea)
			String content = params.getCleanString("content" + index);
			StringBuffer alertMsg = new StringBuffer();
			content = FormattedText.processHtmlDocument(content, alertMsg);
			if (alertMsg.length() > 0)
			{
				item_alerts.add(alertMsg.toString());
			}
			if(content != null && !content.equals(""))
			{
				item.setContent(content);
				item.setContentHasChanged(true);
				blank_entry = false;
			}
			item.setMimeType(MIME_TYPE_DOCUMENT_HTML);
		}
		else if(item.isUrl())
		{
			item.setMimeType(ResourceProperties.TYPE_URL);
			String url = params.getString("Url" + index);
			if(url == null || url.trim().equals(""))
			{
				item.setFilename("");
				item_alerts.add(rb.getString("specifyurl"));
				item.setMissing("Url");
			}
			else
			{
				item.setFilename(url);
				blank_entry = false;
				// is protocol supplied and, if so, is it recognized?
				try
				{
					// check format of input
					URL u = new URL(url);
				}
				catch (MalformedURLException e1)
				{
					try
					{
						// if URL did not validate, check whether the problem was an
						// unrecognized protocol, and accept input if that's the case.
						Pattern pattern = Pattern.compile("\\s*([a-zA-Z0-9]+)://([^\\n]+)");
						Matcher matcher = pattern.matcher(url);
						if(matcher.matches())
						{
							URL test = new URL("http://" + matcher.group(2));
						}
						else
						{
							url = "http://" + url;
							URL test = new URL(url);
							item.setFilename(url);
						}
					}
					catch (MalformedURLException e2)
					{
						// invalid url
						item_alerts.add(rb.getString("validurl"));
						item.setMissing("Url");
					}
				}
			}
		}
		else if(item.isStructuredArtifact())
		{
			String formtype = (String) current_stack_frame.get(STATE_STACK_STRUCTOBJ_TYPE);
			if(formtype == null)
			{
				formtype = (String) state.getAttribute(STATE_STRUCTOBJ_TYPE);
				if(formtype == null)
				{
					formtype = "";
				}
				current_stack_frame.put(STATE_STACK_STRUCTOBJ_TYPE, formtype);
			}
			String formtype_check = params.getString("formtype");

			if(formtype_check == null || formtype_check.equals(""))
			{
				item_alerts.add("Must select a form type");
				item.setMissing("formtype");
			}
			else if(formtype_check.equals(formtype))
			{
				item.setFormtype(formtype);
				capturePropertyValues(params, item, item.getProperties());
				// blank_entry = false;
			}
			item.setMimeType(MIME_TYPE_STRUCTOBJ);

		}
		if(item.isFileUpload() || item.isHtml() || item.isPlaintext())
		{
			BasicRightsAssignment rightsObj = item.getRights();
			rightsObj.captureValues(params);
			
			boolean usingCreativeCommons = state.getAttribute(STATE_USING_CREATIVE_COMMONS) != null && state.getAttribute(STATE_USING_CREATIVE_COMMONS).equals(Boolean.TRUE.toString());
			
			if(usingCreativeCommons)
			{
				String ccOwnership = params.getString("ccOwnership" + index);
				if(ccOwnership != null)
				{
					item.setRightsownership(ccOwnership);
				}
				String ccTerms = params.getString("ccTerms" + index);
				if(ccTerms != null)
				{
					item.setLicense(ccTerms);
				}
				String ccCommercial = params.getString("ccCommercial" + index);
				if(ccCommercial != null)
				{
					item.setAllowCommercial(ccCommercial);
				}
				String ccModification = params.getString("ccModification" + index);
				if(ccCommercial != null)
				{
					item.setAllowModifications(ccModification);
				}
				String ccRightsYear = params.getString("ccRightsYear" + index);
				if(ccRightsYear != null)
				{
					item.setRightstyear(ccRightsYear);
				}
				String ccRightsOwner = params.getString("ccRightsOwner" + index);
				if(ccRightsOwner != null)
				{
					item.setRightsowner(ccRightsOwner);
				}

				/*
				ccValues.ccOwner = new Array();
				ccValues.myRights = new Array();
				ccValues.otherRights = new Array();
				ccValues.ccCommercial = new Array();
				ccValues.ccModifications = new Array();
				ccValues.ccRightsYear = new Array();
				ccValues.ccRightsOwner = new Array();
				*/
			}
			else
			{
				// check for copyright status
				// check for copyright info
				// check for copyright alert
	
				String copyrightStatus = StringUtil.trimToNull(params.getString ("copyright" + index));
				String copyrightInfo = StringUtil.trimToNull(params.getCleanString ("newcopyright" + index));
				String copyrightAlert = StringUtil.trimToNull(params.getString("copyrightAlert" + index));
	
				if (copyrightStatus != null)
				{
					if (state.getAttribute(COPYRIGHT_NEW_COPYRIGHT) != null && copyrightStatus.equals(state.getAttribute(COPYRIGHT_NEW_COPYRIGHT)))
					{
						if (copyrightInfo != null)
						{
							item.setCopyrightInfo( copyrightInfo );
						}
						else
						{
							item_alerts.add(rb.getString("specifycp2"));
							// addAlert(state, rb.getString("specifycp2"));
						}
					}
					else if (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT) != null && copyrightStatus.equals (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT)))
					{
						item.setCopyrightInfo((String) state.getAttribute (STATE_MY_COPYRIGHT));
					}
	
					item.setCopyrightStatus( copyrightStatus );
				}
				item.setCopyrightAlert(copyrightAlert != null);
			}

		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		if(preventPublicDisplay.equals(Boolean.FALSE))
		{
			boolean pubviewset = item.isPubviewset();
			boolean pubview = false;
			if (! RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
			{
				if (!pubviewset)
				{
					pubview = params.getBoolean("pubview");
					item.setPubview(pubview);
				}
			}
		}

		String access_mode = params.getString("access_mode" + index);
		if(access_mode != null)
		{
			item.setAccess(access_mode);
			if(AccessMode.GROUPED.toString().equals(access_mode))
			{
				String[] access_groups = params.getStrings("access_groups" + index);
				for(int gr = 0; gr < access_groups.length; gr++)
				{
					item.addGroup(access_groups[gr]);
				}
			}
		}

		int noti = NotificationService.NOTI_NONE;
		// %%STATE_MODE_RESOURCES%%
		if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// set noti to none if in dropbox mode
			noti = NotificationService.NOTI_NONE;
		}
		else
		{
			// read the notification options
			String notification = params.getString("notify" + index);
			if ("r".equals(notification))
			{
				noti = NotificationService.NOTI_REQUIRED;
			}
			else if ("o".equals(notification))
			{
				noti = NotificationService.NOTI_OPTIONAL;
			}
		}
		item.setNotification(noti);

		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups != null && ! metadataGroups.isEmpty())
		{
			Iterator groupIt = metadataGroups.iterator();
			while(groupIt.hasNext())
			{
				MetadataGroup group = (MetadataGroup) groupIt.next();
				if(item.isGroupShowing(group.getName()))
				{
					Iterator propIt = group.iterator();
					while(propIt.hasNext())
					{
						ResourcesMetadata prop = (ResourcesMetadata) propIt.next();
						String propname = prop.getFullname();
						if(ResourcesMetadata.WIDGET_DATE.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_DATETIME.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_TIME.equals(prop.getWidget()))
						{
							int year = 0;
							int month = 0;
							int day = 0;
							int hour = 0;
							int minute = 0;
							int second = 0;
							int millisecond = 0;
							String ampm = "";

							if(prop.getWidget().equals(ResourcesMetadata.WIDGET_DATE) ||
								prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
							{
								year = params.getInt(propname + "_" + index + "_year", year);
								month = params.getInt(propname + "_" + index + "_month", month);
								day = params.getInt(propname + "_" + index + "_day", day);
							}
							if(prop.getWidget().equals(ResourcesMetadata.WIDGET_TIME) ||
								prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
							{
								hour = params.getInt(propname + "_" + index + "_hour", hour);
								minute = params.getInt(propname + "_" + index + "_minute", minute);
								second = params.getInt(propname + "_" + index + "_second", second);
								millisecond = params.getInt(propname + "_" + index + "_millisecond", millisecond);
								ampm = params.getString(propname + "_" + index + "_ampm").trim();

								if("pm".equalsIgnoreCase(ampm))
								{
									if(hour < 12)
									{
										hour += 12;
									}
								}
								else if(hour == 12)
								{
									hour = 0;
								}
							}
							if(hour > 23)
							{
								hour = hour % 24;
								day++;
							}

							Time value = TimeService.newTimeLocal(year, month, day, hour, minute, second, millisecond);
							item.setMetadataItem(propname,value);

						}
						else
						{
							String value = params.getString(propname + "_" + index);
							if(value != null)
							{
								item.setMetadataItem(propname, value);
							}
						}
					}
				}
			}
		}
		item.markAsBlank(blank_entry);

		return item_alerts;

	}

	/**
	 * Retrieve values for one or more items from create context.  Create context contains up to ten items at a time
	 * all of the same type (folder, file, text document, structured-artifact, etc).  This method retrieves the data
	 * apppropriate to the type and updates the values of the EditItem objects stored as the STATE_STACK_CREATE_ITEMS
	 * attribute in state. If the third parameter is "true", missing/incorrect user inputs will generate error messages
	 * and attach flags to the input elements.
	 * @param state
	 * @param params
	 * @param markMissing Should this method generate error messages and add flags for missing/incorrect user inputs?
	 */
	protected static void captureMultipleValues(SessionState state, ParameterParser params, boolean markMissing)
	{
		Map current_stack_frame = peekAtStack(state);
		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(number == null)
		{
			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}
		if(number == null)
		{
			number = new Integer(1);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		if(new_items == null)
		{
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			String itemType = (String) current_stack_frame.get(STATE_STACK_CREATE_TYPE);
			if(itemType == null || itemType.trim().equals(""))
			{
				itemType = (String) state.getAttribute(STATE_CREATE_TYPE);
				if(itemType == null || itemType.trim().equals(""))
				{
					itemType = TYPE_UPLOAD;
				}
				current_stack_frame.put(STATE_STACK_CREATE_TYPE, itemType);
			}
			new_items = new Vector();
			for(int i = 0; i < CREATE_MAX_ITEMS; i++)
			{
				EditItem item = new EditItem(itemType);
				item.setCopyrightStatus(defaultCopyrightStatus);
				new_items.add(item);
			}
			current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);
		}

		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}
		int actualCount = 0;
		Set first_item_alerts = null;

		String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
		int max_bytes = 1096 * 1096;
		try
		{
			max_bytes = Integer.parseInt(max_file_size_mb) * 1096 * 1096;
		}
		catch(Exception e)
		{
			// if unable to parse an integer from the value
			// in the properties file, use 1 MB as a default
			max_file_size_mb = "1";
			max_bytes = 1096 * 1096;
		}

		/*
		// params.getContentLength() returns m_req.getContentLength()
		if(params.getContentLength() > max_bytes)
		{
			alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
			state.setAttribute(STATE_CREATE_ALERTS, alerts);

			return;
		}
		*/
		for(int i = 0; i < number.intValue(); i++)
		{
			EditItem item = (EditItem) new_items.get(i);
			Set item_alerts = captureValues(item, i, state, params, markMissing);
			if(i == 0)
			{
				first_item_alerts = item_alerts;
			}
			else if(item.isBlank())
			{
				item.clearMissing();
			}
			if(! item.isBlank())
			{
				alerts.addAll(item_alerts);
				actualCount ++;
			}
		}
		if(actualCount > 0)
		{
			EditItem item = (EditItem) new_items.get(0);
			if(item.isBlank())
			{
				item.clearMissing();
			}
		}
		else if(markMissing)
		{
			alerts.addAll(first_item_alerts);
		}
		state.setAttribute(STATE_CREATE_ALERTS, alerts);
		current_stack_frame.put(STATE_STACK_CREATE_ACTUAL_COUNT, Integer.toString(actualCount));

	}	// captureMultipleValues

	protected static void capturePropertyValues(ParameterParser params, EditItem item, List properties)
	{
		// use the item's properties if they're not supplied
		if(properties == null)
		{
			properties = item.getProperties();
		}
		// if max cardinality > 1, value is a list (Iterate over members of list)
		// else value is an object, not a list

		// if type is nested, object is a Map (iterate over name-value pairs for the properties of the nested object)
		// else object is type to store value, usually a string or a date/time

		Iterator it = properties.iterator();
		while(it.hasNext())
		{
			ResourcesMetadata prop = (ResourcesMetadata) it.next();
			String propname = prop.getDottedname();

			if(ResourcesMetadata.WIDGET_NESTED.equals(prop.getWidget()))
			{
				// do nothing
			}
			else if(ResourcesMetadata.WIDGET_BOOLEAN.equals(prop.getWidget()))
			{
				String value = params.getString(propname);
				if(value == null || Boolean.FALSE.toString().equals(value))
				{
					prop.setValue(0, Boolean.FALSE.toString());
				}
				else
				{
					prop.setValue(0, Boolean.TRUE.toString());
				}
			}
			else if(ResourcesMetadata.WIDGET_DATE.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_DATETIME.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_TIME.equals(prop.getWidget()))
			{
				int year = 0;
				int month = 0;
				int day = 0;
				int hour = 0;
				int minute = 0;
				int second = 0;
				int millisecond = 0;
				String ampm = "";

				if(prop.getWidget().equals(ResourcesMetadata.WIDGET_DATE) ||
					prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
				{
					year = params.getInt(propname + "_year", year);
					month = params.getInt(propname + "_month", month);
					day = params.getInt(propname + "_day", day);
				}
				if(prop.getWidget().equals(ResourcesMetadata.WIDGET_TIME) ||
					prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
				{
					hour = params.getInt(propname + "_hour", hour);
					minute = params.getInt(propname + "_minute", minute);
					second = params.getInt(propname + "_second", second);
					millisecond = params.getInt(propname + "_millisecond", millisecond);
					ampm = params.getString(propname + "_ampm");

					if("pm".equalsIgnoreCase(ampm))
					{
						if(hour < 12)
						{
							hour += 12;
						}
					}
					else if(hour == 12)
					{
						hour = 0;
					}
				}
				if(hour > 23)
				{
					hour = hour % 24;
					day++;
				}

				Time value = TimeService.newTimeLocal(year, month, day, hour, minute, second, millisecond);
				prop.setValue(0, value);
			}
			else if(ResourcesMetadata.WIDGET_ANYURI.equals(prop.getWidget()))
			{
				String value = params.getString(propname);
				if(value != null && ! value.trim().equals(""))
				{
					Reference ref = EntityManager.newReference(ContentHostingService.getReference(value));
					prop.setValue(0, ref);
				}
			}
			else
			{
				String value = params.getString(propname);
				if(value != null)
				{
					prop.setValue(0, value);
				}
			}
		}

	}	// capturePropertyValues

	/**
	* Modify the properties
	*/
	public static void doSavechanges ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		String flow = params.getString("flow").trim();

		if(flow == null || "cancel".equals(flow))
		{
			doCancel(data);
			return;
		}

		// get values from form and update STATE_STACK_EDIT_ITEM attribute in state
		captureValues(state, params);

		Map current_stack_frame = peekAtStack(state);

		EditItem item = (EditItem) current_stack_frame.get(STATE_STACK_EDIT_ITEM);

		if(flow.equals("showMetadata"))
		{
			doShow_metadata(data);
			return;
		}
		else if(flow.equals("hideMetadata"))
		{
			doHide_metadata(data);
			return;
		}
		else if(flow.equals("intentChanged"))
		{
			doToggle_intent(data);
			return;
		}
		else if(flow.equals("addInstance"))
		{
			String field = params.getString("field");
			addInstance(field, item.getProperties());
			ResourcesMetadata form = item.getForm();
			List flatList = form.getFlatList();
			item.setProperties(flatList);
			return;
		}
		else if(flow.equals("linkResource"))
		{
			// captureMultipleValues(state, params, false);
			createLink(data, state);
			//Map new_stack_frame = pushOnStack(state);
			//new_stack_frame.put(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_SELECT);
			state.setAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_SELECT);

			return;
		}


		Set alerts = (Set) state.getAttribute(STATE_EDIT_ALERTS);

		if(item.isStructuredArtifact())
		{
			SchemaBean bean = (SchemaBean) current_stack_frame.get(STATE_STACK_STRUCT_OBJ_SCHEMA);
			SaveArtifactAttempt attempt = new SaveArtifactAttempt(item, bean.getSchema());
			validateStructuredArtifact(attempt);

			Iterator errorIt = attempt.getErrors().iterator();
			while(errorIt.hasNext())
			{
				ValidationError error = (ValidationError) errorIt.next();
				alerts.add(error.getDefaultMessage());
			}
		}

		if(alerts.isEmpty())
		{
			// populate the property list
			try
			{
				// get an edit
				ContentCollectionEdit cedit = null;
				ContentResourceEdit redit = null;
				GroupAwareEdit gedit = null;
				ResourcePropertiesEdit pedit = null;

				if(item.isFolder())
				{
					cedit = ContentHostingService.editCollection(item.getId());
					gedit = cedit;
					pedit = cedit.getPropertiesEdit();
				}
				else
				{
					redit = ContentHostingService.editResource(item.getId());
					gedit = redit;
					pedit = redit.getPropertiesEdit();
				}
				
				try
				{
					if((AccessMode.INHERITED.toString().equals(item.getAccess()) || AccessMode.SITE.toString().equals(item.getAccess())) && AccessMode.GROUPED == gedit.getAccess())
					{
						gedit.clearGroupAccess();
					}
					else if(gedit.getAccess() == AccessMode.GROUPED && item.getGroups().isEmpty())
					{
						gedit.clearGroupAccess();
					}
					else if(!item.getGroups().isEmpty())
					{
						Collection groupRefs = new Vector();
						Iterator it = item.getGroups().iterator();
						while(it.hasNext())
						{
							Group group = (Group) it.next();
							groupRefs.add(group.getReference());
						}
						gedit.setGroupAccess(groupRefs);
					}
				}
				catch(InconsistentException e)
				{
					// TODO: Should this be reported to user??
					logger.warn("ResourcesAction.doSavechanges ***** InconsistentException changing groups ***** " + e.getMessage());
				}
				
				if(item.isFolder())
				{
				}
				else
				{
					if(item.isUrl())
					{
						redit.setContent(item.getFilename().getBytes());
					}
					else if(item.isStructuredArtifact())
					{
						redit.setContentType(item.getMimeType());
						redit.setContent(item.getContent());
					}
					else if(item.contentHasChanged())
					{
						redit.setContentType(item.getMimeType());
						redit.setContent(item.getContent());
					}
					else if(item.contentTypeHasChanged())
					{
						redit.setContentType(item.getMimeType());
					}

					BasicRightsAssignment rightsObj = item.getRights();
					rightsObj.addResourceProperties(pedit);
										
					String copyright = StringUtil.trimToNull(params.getString ("copyright"));
					String newcopyright = StringUtil.trimToNull(params.getCleanString (NEW_COPYRIGHT));
					String copyrightAlert = StringUtil.trimToNull(params.getString("copyrightAlert"));
					if (copyright != null)
					{
						if (state.getAttribute(COPYRIGHT_NEW_COPYRIGHT) != null && copyright.equals(state.getAttribute(COPYRIGHT_NEW_COPYRIGHT)))
						{
							if (newcopyright != null)
							{
								pedit.addProperty (ResourceProperties.PROP_COPYRIGHT, newcopyright);
							}
							else
							{
								alerts.add(rb.getString("specifycp2"));
								// addAlert(state, rb.getString("specifycp2"));
							}
						}
						else if (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT) != null && copyright.equals (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT)))
						{
							String mycopyright = (String) state.getAttribute (STATE_MY_COPYRIGHT);
							pedit.addProperty (ResourceProperties.PROP_COPYRIGHT, mycopyright);
						}

						pedit.addProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE, copyright);
					}

					if (copyrightAlert != null)
					{
						pedit.addProperty (ResourceProperties.PROP_COPYRIGHT_ALERT, copyrightAlert);
					}
					else
					{
						pedit.removeProperty (ResourceProperties.PROP_COPYRIGHT_ALERT);
					}
				}

				if (!(item.isFolder() && (item.getId().equals ((String) state.getAttribute (STATE_HOME_COLLECTION_ID)))))
				{
					pedit.addProperty (ResourceProperties.PROP_DISPLAY_NAME, item.getName());
				}	// the home collection's title is not modificable

				pedit.addProperty (ResourceProperties.PROP_DESCRIPTION, item.getDescription());
				// deal with quota (collections only)
				if ((cedit != null) && item.canSetQuota())
				{
					if (item.hasQuota())
					{
						// set the quota
						pedit.addProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA, item.getQuota());
					}
					else
					{
						// clear the quota
						pedit.removeProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
					}
				}

				List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);

				state.setAttribute(STATE_EDIT_ALERTS, alerts);
				saveMetadata(pedit, metadataGroups, item);
				alerts = (Set) state.getAttribute(STATE_EDIT_ALERTS);

				// commit the change
				if (cedit != null)
				{
					ContentHostingService.commitCollection(cedit);
				}
				else
				{
					ContentHostingService.commitResource(redit, item.getNotification());
				}

				current_stack_frame.put(STATE_STACK_EDIT_INTENT, INTENT_REVISE_FILE);

				Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
				if(preventPublicDisplay == null)
				{
					preventPublicDisplay = Boolean.FALSE;
					state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
				}
				
				if(preventPublicDisplay.equals(Boolean.FALSE))
				{
					if (! RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
					{
						if (!item.isPubviewset())
						{
							ContentHostingService.setPubView(item.getId(), item.isPubview());
						}
					}
				}

				// need to refresh collection containing current edit item make changes show up
				String containerId = ContentHostingService.getContainingCollectionId(item.getId());
				Map expandedCollections = (Map) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
				Object old = expandedCollections.remove(containerId);
				if (old != null)
				{
					try
					{
						ContentCollection container = ContentHostingService.getCollection(containerId);
						expandedCollections.put(containerId, container);
					}
					catch (Throwable ignore){}
				}
			}
			catch (TypeException e)
			{
				alerts.add(rb.getString("typeex") + " "  + item.getId());
				// addAlert(state," " + rb.getString("typeex") + " "  + item.getId());
			}
			catch (IdUnusedException e)
			{
				alerts.add(RESOURCE_NOT_EXIST_STRING);
				// addAlert(state,RESOURCE_NOT_EXIST_STRING);
			}
			catch (PermissionException e)
			{
				alerts.add(rb.getString("notpermis10") + " " + item.getId());
				// addAlert(state, rb.getString("notpermis10") + " " + item.getId() + ". " );
			}
			catch (InUseException e)
			{
				alerts.add(rb.getString("someone") + " " + item.getId());
				// addAlert(state, rb.getString("someone") + " " + item.getId() + ". ");
			}
			catch (ServerOverloadException e)
			{
				alerts.add(rb.getString("failed"));
			}
			catch (OverQuotaException e)
			{
				alerts.add(rb.getString("changing1") + " " + item.getId() + " " + rb.getString("changing2"));
				// addAlert(state, rb.getString("changing1") + " " + item.getId() + " " + rb.getString("changing2"));
			}
			catch(RuntimeException e)
			{
				logger.warn("ResourcesAction.doSavechanges ***** Unknown Exception ***** " + e.getMessage());
				logger.warn("ResourcesAction.doSavechanges ***** Unknown Exception ***** ", e);
				alerts.add(rb.getString("failed"));
			}
		}	// if - else

		if(alerts.isEmpty())
		{
			// modify properties sucessful
			String mode = (String) state.getAttribute(STATE_MODE);
			popFromStack(state);
			resetCurrentMode(state);
		}	//if-else
		else
		{
			Iterator alertIt = alerts.iterator();
			while(alertIt.hasNext())
			{
				String alert = (String) alertIt.next();
				addAlert(state, alert);
			}
			alerts.clear();
			state.setAttribute(STATE_EDIT_ALERTS, alerts);
			// state.setAttribute(STATE_CREATE_MISSING_ITEM, missing);
		}

	}	// doSavechanges

	/**
	 * @param pedit
	 * @param metadataGroups
	 * @param metadata
	 */
	private static void saveMetadata(ResourcePropertiesEdit pedit, List metadataGroups, EditItem item)
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
	 * @param data
	 */
	protected static void doToggle_intent(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		String intent = params.getString("intent");
		Map current_stack_frame = peekAtStack(state);
		current_stack_frame.put(STATE_STACK_EDIT_INTENT, intent);

	}	// doToggle_intent

	/**
	 * @param data
	 */
	public static void doHideOtherSites(RunData data)
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


	/**
	 * @param data
	 */
	public static void doShowOtherSites(RunData data)
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
	 * @param data
	 */
	public static void doHide_metadata(RunData data)
	{
		ParameterParser params = data.getParameters ();
		String name = params.getString("metadataGroup");

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups != null && ! metadataGroups.isEmpty())
		{
			boolean found = false;
			MetadataGroup group = null;
			Iterator it = metadataGroups.iterator();
			while(!found && it.hasNext())
			{
				group = (MetadataGroup) it.next();
				found = (name.equals(Validator.escapeUrl(group.getName())) || name.equals(group.getName()));
			}
			if(found)
			{
				group.setShowing(false);
			}
		}

	}	// doHide_metadata

	/**
	 * @param data
	 */
	public static void doShow_metadata(RunData data)
	{
		ParameterParser params = data.getParameters ();
		String name = params.getString("metadataGroup");

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups != null && ! metadataGroups.isEmpty())
		{
			boolean found = false;
			MetadataGroup group = null;
			Iterator it = metadataGroups.iterator();
			while(!found && it.hasNext())
			{
				group = (MetadataGroup) it.next();
				found = (name.equals(Validator.escapeUrl(group.getName())) || name.equals(group.getName()));
			}
			if(found)
			{
				group.setShowing(true);
			}
		}

	}	// doShow_metadata

	/**
	* Sort based on the given property
	*/
	public static void doSort ( RunData data)
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

		// current sorting sequence
		String asc = NULL_STRING;
		if (!criteria.equals (state.getAttribute (STATE_SORT_BY)))
		{
			state.setAttribute (STATE_SORT_BY, criteria);
			asc = Boolean.TRUE.toString();
			state.setAttribute (STATE_SORT_ASC, asc);
		}
		else
		{
			// current sorting sequence
			asc = (String) state.getAttribute (STATE_SORT_ASC);

			//toggle between the ascending and descending sequence
			if (asc.equals (Boolean.TRUE.toString()))
			{
				asc = Boolean.FALSE.toString();
			}
			else
			{
				asc = Boolean.TRUE.toString();
			}
			state.setAttribute (STATE_SORT_ASC, asc);
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// sort sucessful
			// state.setAttribute (STATE_MODE, MODE_LIST);

		}	// if-else

	}	// doSort

	/**
	* set the state name to be "deletecofirm" if any item has been selected for deleting
	*/
	public void doDeleteconfirm ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		Set deleteIdSet  = new TreeSet();

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

		String[] deleteIds = data.getParameters ().getStrings ("selectedMembers");
		if (deleteIds == null)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile3"));
		}
		else
		{
			deleteIdSet.addAll(Arrays.asList(deleteIds));
			List deleteItems = new Vector();
			List notDeleteItems = new Vector();
			List nonEmptyFolders = new Vector();
			List roots = (List) state.getAttribute(STATE_COLLECTION_ROOTS);
			Iterator rootIt = roots.iterator();
			while(rootIt.hasNext())
			{
				BrowseItem root = (BrowseItem) rootIt.next();

				List members = root.getMembers();
				Iterator memberIt = members.iterator();
				while(memberIt.hasNext())
				{
					BrowseItem member = (BrowseItem) memberIt.next();
					if(deleteIdSet.contains(member.getId()))
					{
						if(member.isFolder())
						{
							if(ContentHostingService.allowRemoveCollection(member.getId()))
							{
								deleteItems.add(member);
								if(! member.isEmpty())
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
				}
			}

			if(! notDeleteItems.isEmpty())
			{
				String notDeleteNames = "";
				boolean first_item = true;
				Iterator notIt = notDeleteItems.iterator();
				while(notIt.hasNext())
				{
					BrowseItem item = (BrowseItem) notIt.next();
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


			/*
					//htripath-SAK-1712 - Set new collectionId as resources are not deleted under 'more' requirement.
					if(state.getAttribute(STATE_MESSAGE) == null){
					  String newCollectionId=ContentHostingService.getContainingCollectionId(currentId);
					  state.setAttribute(STATE_COLLECTION_ID, newCollectionId);
					}
			*/

			// delete item
			state.setAttribute (STATE_DELETE_ITEMS, deleteItems);
			state.setAttribute (STATE_DELETE_ITEMS_NOT_EMPTY, nonEmptyFolders);
		}	// if-else

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_MODE, MODE_DELETE_CONFIRM);
			state.setAttribute(STATE_LIST_SELECTIONS, deleteIdSet);
		}


	}	// doDeleteconfirm


	/**
	* set the state name to be "cut" if any item has been selected for cutting
	*/
	public void doCut ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		String[] cutItems = data.getParameters ().getStrings ("selectedMembers");
		if (cutItems == null)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile5"));
			state.setAttribute (STATE_MODE, MODE_LIST);
		}
		else
		{
			Vector cutIdsVector = new Vector ();
			String nonCutIds = NULL_STRING;

			String cutId = NULL_STRING;
			for (int i = 0; i < cutItems.length; i++)
			{
				cutId = cutItems[i];
				try
				{
					ResourceProperties properties = ContentHostingService.getProperties (cutId);
					if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
					{
						String alert = (String) state.getAttribute(STATE_MESSAGE);
						if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
						{
							addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
						}
					}
					else
					{
						if (ContentHostingService.allowRemoveResource (cutId))
						{
							cutIdsVector.add (cutId);
						}
						else
						{
							nonCutIds = nonCutIds + " " + properties.getProperty (ResourceProperties.PROP_DISPLAY_NAME) + "; ";
						}
					}
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis15"));
				}
				catch (IdUnusedException e)
				{
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
				}	// try-catch
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				if (nonCutIds.length ()>0)
				{
					addAlert(state, rb.getString("notpermis16") +" " + nonCutIds);
				}

				if (cutIdsVector.size ()>0)
				{
					state.setAttribute (STATE_CUT_FLAG, Boolean.TRUE.toString());
					if (((String) state.getAttribute (STATE_SELECT_ALL_FLAG)).equals (Boolean.TRUE.toString()))
					{
						state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());
					}

					Vector copiedIds = (Vector) state.getAttribute (STATE_COPIED_IDS);
					for (int i = 0; i < cutIdsVector.size (); i++)
					{
						String currentId = (String) cutIdsVector.elementAt (i);
						if ( copiedIds.contains (currentId))
						{
							copiedIds.remove (currentId);
						}
					}
					if (copiedIds.size ()==0)
					{
						state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());
					}

					state.setAttribute (STATE_COPIED_IDS, copiedIds);

					state.setAttribute (STATE_CUT_IDS, cutIdsVector);
				}
			}
		}	// if-else

	}	// doCut

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

		Vector copyItemsVector = new Vector ();

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
						if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
						{
							addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
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
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
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
	* Handle user's selection of items to be moved.
	*/
	public void doMove ( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		List moveItemsVector = new Vector();

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
						if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
						{
							addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
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
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
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


	/**
	 * If copy-flag is set to false, erase the copied-id's list and set copied flags to false
	 * in all the browse items.  If copied-id's list is empty, set copy-flag to false and set
	 * copied flags to false in all the browse items. If copy-flag is set to true and copied-id's
	 * list is not empty, update the copied flags of all browse items so copied flags for the
	 * copied items are set to true and all others are set to false.
	 */
	protected void setCopyFlags(SessionState state)
	{
		String copyFlag = (String) state.getAttribute(STATE_COPY_FLAG);
		List copyItemsVector = (List) state.getAttribute(STATE_COPIED_IDS);

		if(copyFlag == null)
		{
			copyFlag = Boolean.FALSE.toString();
			state.setAttribute(STATE_COPY_FLAG, copyFlag);
		}

		if(copyFlag.equals(Boolean.TRUE.toString()))
		{
			if(copyItemsVector == null)
			{
				copyItemsVector = new Vector();
				state.setAttribute(STATE_COPIED_IDS, copyItemsVector);
			}
			if(copyItemsVector.isEmpty())
			{
				state.setAttribute(STATE_COPY_FLAG, Boolean.FALSE.toString());
			}
		}
		else
		{
			copyItemsVector = new Vector();
			state.setAttribute(STATE_COPIED_IDS, copyItemsVector);
		}

		List roots = (List) state.getAttribute(STATE_COLLECTION_ROOTS);
		Iterator rootIt = roots.iterator();
		while(rootIt.hasNext())
		{
			BrowseItem root = (BrowseItem) rootIt.next();
			boolean root_copied = copyItemsVector.contains(root.getId());
			root.setCopied(root_copied);

			List members = root.getMembers();
			Iterator memberIt = members.iterator();
			while(memberIt.hasNext())
			{
				BrowseItem member = (BrowseItem) memberIt.next();
				boolean member_copied = copyItemsVector.contains(member.getId());
				member.setCopied(member_copied);
			}
		}
		// check -- jim
		state.setAttribute(STATE_COLLECTION_ROOTS, roots);

	}	// setCopyFlags

	/**
	* Expand all the collection resources.
	*/
	static public void doExpandall ( RunData data)
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
	* Unexpand all the collection resources
	*/
	public static void doUnexpandall ( RunData data)
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

		state.setAttribute(STATE_EXPANDED_COLLECTIONS, new HashMap());
		state.setAttribute(STATE_EXPAND_ALL_FLAG, Boolean.FALSE.toString());

	}	// doUnexpandall

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

	/**
	* Remove the state variables used internally, on the way out.
	*/
	static private void cleanupState(SessionState state)
	{
		state.removeAttribute(STATE_FROM_TEXT);
		state.removeAttribute(STATE_HAS_ATTACHMENT_BEFORE);
		state.removeAttribute(STATE_ATTACH_SHOW_DROPBOXES);
		state.removeAttribute(STATE_ATTACH_COLLECTION_ID);

		state.removeAttribute(COPYRIGHT_FAIRUSE_URL);
		state.removeAttribute(COPYRIGHT_NEW_COPYRIGHT);
		state.removeAttribute(COPYRIGHT_SELF_COPYRIGHT);
		state.removeAttribute(COPYRIGHT_TYPES);
		state.removeAttribute(DEFAULT_COPYRIGHT_ALERT);
		state.removeAttribute(DEFAULT_COPYRIGHT);
		state.removeAttribute(STATE_EXPANDED_COLLECTIONS);
		state.removeAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
		state.removeAttribute(NEW_COPYRIGHT_INPUT);
		state.removeAttribute(STATE_COLLECTION_ID);
		state.removeAttribute(STATE_COLLECTION_PATH);
		state.removeAttribute(STATE_CONTENT_SERVICE);
		state.removeAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE);
		//state.removeAttribute(STATE_STACK_EDIT_INTENT);
		state.removeAttribute(STATE_EXPAND_ALL_FLAG);
		state.removeAttribute(STATE_HELPER_NEW_ITEMS);
		state.removeAttribute(STATE_HELPER_CHANGED);
		state.removeAttribute(STATE_HOME_COLLECTION_DISPLAY_NAME);
		state.removeAttribute(STATE_HOME_COLLECTION_ID);
		state.removeAttribute(STATE_LIST_SELECTIONS);
		state.removeAttribute(STATE_MY_COPYRIGHT);
		state.removeAttribute(STATE_NAVIGATION_ROOT);
		state.removeAttribute(STATE_PASTE_ALLOWED_FLAG);
		state.removeAttribute(STATE_SELECT_ALL_FLAG);
		state.removeAttribute(STATE_SHOW_ALL_SITES);
		state.removeAttribute(STATE_SITE_TITLE);
		state.removeAttribute(STATE_SORT_ASC);
		state.removeAttribute(STATE_SORT_BY);
		state.removeAttribute(STATE_STACK_STRUCTOBJ_TYPE);
		state.removeAttribute(STATE_STACK_STRUCTOBJ_TYPE_READONLY);
		state.removeAttribute(STATE_INITIALIZED);
		state.removeAttribute(VelocityPortletPaneledAction.STATE_HELPER);

	}	// cleanupState


	public static void initStateAttributes(SessionState state, VelocityPortlet portlet)
	{
		if (state.getAttribute (STATE_INITIALIZED) != null) return;

		if (state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE) == null)
		{
			state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, ServerConfigurationService.getString("content.upload.max", "1"));
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

		// state.setAttribute(STATE_TOP_PAGE_MESSAGE, "");

		state.setAttribute (STATE_CONTENT_SERVICE, ContentHostingService.getInstance());
		state.setAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE, ContentTypeImageService.getInstance());

		TimeBreakdown timeBreakdown = (TimeService.newTime()).breakdownLocal ();
		String mycopyright = COPYRIGHT_SYMBOL + " " + timeBreakdown.getYear () +", " + UserDirectoryService.getCurrentUser().getDisplayName () + ". All Rights Reserved. ";
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

		state.setAttribute (STATE_COLLECTION_PATH, new Vector ());

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

		HomeFactory factory = (HomeFactory) ComponentManager.get("homeFactory");
		if(factory != null)
		{
			Map homes = factory.getHomes(StructuredArtifactHomeInterface.class);
			if(! homes.isEmpty())
			{
				state.setAttribute(STATE_SHOW_FORM_ITEMS, Boolean.TRUE.toString());
			}
		}

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

		HashMap expandedCollections = new HashMap();
		//expandedCollections.add (state.getAttribute (STATE_HOME_COLLECTION_ID));
		state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
		
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

		if (state.getAttribute(COPYRIGHT_TYPES) == null)
		{
			if (ServerConfigurationService.getStrings("copyrighttype") != null)
			{
				state.setAttribute(COPYRIGHT_TYPES, new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("copyrighttype"))));
			}
		}

		if (state.getAttribute(DEFAULT_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("default.copyright") != null)
			{
				state.setAttribute(DEFAULT_COPYRIGHT, ServerConfigurationService.getString("default.copyright"));
			}
		}

		if (state.getAttribute(DEFAULT_COPYRIGHT_ALERT) == null)
		{
			if (ServerConfigurationService.getString("default.copyright.alert") != null)
			{
				state.setAttribute(DEFAULT_COPYRIGHT_ALERT, ServerConfigurationService.getString("default.copyright.alert"));
			}
		}

		if (state.getAttribute(NEW_COPYRIGHT_INPUT) == null)
		{
			if (ServerConfigurationService.getString("newcopyrightinput") != null)
			{
				state.setAttribute(NEW_COPYRIGHT_INPUT, ServerConfigurationService.getString("newcopyrightinput"));
			}
		}

		if (state.getAttribute(COPYRIGHT_FAIRUSE_URL) == null)
		{
			if (ServerConfigurationService.getString("fairuse.url") != null)
			{
				state.setAttribute(COPYRIGHT_FAIRUSE_URL, ServerConfigurationService.getString("fairuse.url"));
			}
		}

		if (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("copyrighttype.own") != null)
			{
				state.setAttribute(COPYRIGHT_SELF_COPYRIGHT, ServerConfigurationService.getString("copyrighttype.own"));
			}
		}

		if (state.getAttribute(COPYRIGHT_NEW_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("copyrighttype.new") != null)
			{
				state.setAttribute(COPYRIGHT_NEW_COPYRIGHT, ServerConfigurationService.getString("copyrighttype.new"));
			}
		}

		// get resources mode from tool registry
		String optional_properties = portlet.getPortletConfig().getInitParameter("optional_properties");
		if(optional_properties != null && "true".equalsIgnoreCase(optional_properties))
		{
			initMetadataContext(state);
		}
		
		state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.FALSE);
		String[] siteTypes = ServerConfigurationService.getStrings("prevent.public.resources");
		if(siteTypes != null)
		{
			Site site;
			try
			{
				site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
				for(int i = 0; i < siteTypes.length; i++)
				{
					if ((StringUtil.trimToNull(siteTypes[i])).equals(site.getType()))
					{
						state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.TRUE);
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
		}

		state.setAttribute (STATE_INITIALIZED, Boolean.TRUE.toString());

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
	* initialize the copy context
	*/
	private static void initCopyContext (SessionState state)
	{
		state.setAttribute (STATE_COPIED_IDS, new Vector ());

		state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());

	}	// initCopyContent

	/**
	* initialize the copy context
	*/
	private static void initMoveContext (SessionState state)
	{
		state.setAttribute (STATE_MOVED_IDS, new Vector ());

		state.setAttribute (STATE_MOVE_FLAG, Boolean.FALSE.toString());

	}	// initCopyContent


	/**
	* initialize the cut context
	*/
	private void initCutContext (SessionState state)
	{
		state.setAttribute (STATE_CUT_IDS, new Vector ());

		state.setAttribute (STATE_CUT_FLAG, Boolean.FALSE.toString());

	}	// initCutContent

	/**
	* find out whether there is a duplicate item in testVector
	* @param testVector The Vector to be tested on
	* @param testSize The integer of the test range
	* @return The index value of the duplicate ite
	*/
	private int repeatedName (Vector testVector, int testSize)
	{
		for (int i=1; i <= testSize; i++)
		{
			String currentName = (String) testVector.get (i);
			for (int j=i+1; j <= testSize; j++)
			{
				String comparedTitle = (String) testVector.get (j);
				if (comparedTitle.length()>0 && currentName.length()>0 && comparedTitle.equals (currentName))
				{
					return j;
				}
			}
		}
		return 0;

	}   // repeatedName

	/**
	* Is the id already exist in the current resource?
	* @param testVector The Vector to be tested on
	* @param testSize The integer of the test range
	* @parma isCollection Looking for collection or not
	* @return The index value of the exist id
	*/
	private int foundInResource (Vector testVector, int testSize, String collectionId, boolean isCollection)
	{
		try
		{
			ContentCollection c = ContentHostingService.getCollection(collectionId);
			Iterator membersIterator = c.getMemberResources().iterator();
			while (membersIterator.hasNext())
			{
				ResourceProperties p = ((Entity) membersIterator.next()).getProperties();
				String displayName = p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				if (displayName != null)
				{
					String collectionOrResource = p.getProperty(ResourceProperties.PROP_IS_COLLECTION);
					for (int i=1; i <= testSize; i++)
					{
						String testName = (String) testVector.get(i);
						if ((testName != null) && (displayName.equals (testName))
						      &&  ((isCollection && collectionOrResource.equals (Boolean.TRUE.toString()))
								        || (!isCollection && collectionOrResource.equals(Boolean.FALSE.toString()))))
						{
							return i;
						}
					}	// for
				}
			}
		}
		catch (IdUnusedException e){}
		catch (TypeException e){}
		catch (PermissionException e){}

		return 0;

	}	// foundInResource

	/**
	* empty String Vector object with the size sepecified
	* @param size The Vector object size -1
	* @return The Vector object consists of null Strings
	*/
	private static Vector emptyVector (int size)
	{
		Vector v = new Vector ();
		for (int i=0; i <= size; i++)
		{
			v.add (i, "");
		}
		return v;

	}	// emptyVector

	/**
	*  Setup for customization
	**/
	public String buildOptionsPanelContext( VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		context.put("tlang",rb);
		String home = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		Reference ref = EntityManager.newReference(ContentHostingService.getReference(home));
		String siteId = ref.getContext();

		context.put("form-submit", BUTTON + "doConfigure_update");
		context.put("form-cancel", BUTTON + "doCancel_options");
		context.put("description", "Setting options for Resources in worksite "
				+ SiteService.getSiteDisplay(siteId));

		// pick the "-customize" template based on the standard template name
		String template = (String)getContext(data).get("template");
		return template + "-customize";

	}	// buildOptionsPanelContext

	/**
	* Handle the configure context's update button
	*/
	public void doConfigure_update(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(peid);

		// we are done with customization... back to the main (browse) mode
		state.setAttribute(STATE_MODE, MODE_LIST);

		// commit the change
		// saveOptions();
		cancelOptions();

	}   // doConfigure_update

	/**
	* doCancel_options called for form input tags type="submit" named="eventSubmit_doCancel"
	* cancel the options process
	*/
	public void doCancel_options(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(peid);

		// cancel the options
		cancelOptions();

		// we are done with customization... back to the main (MODE_LIST) mode
		state.setAttribute(STATE_MODE, MODE_LIST);

	}   // doCancel_options

	/**
	* Add the collection id into the expanded collection list
	 * @throws PermissionException
	 * @throws TypeException
	 * @throws IdUnusedException
	*/
	public static void doExpand_collection(RunData data) throws IdUnusedException, TypeException, PermissionException
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		HashMap currentMap = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);

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
		currentMap.put (id,ContentHostingService.getCollection (id));

		state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);

		// add this folder id into the set to be event-observed
		addObservingPattern(id, state);

	}	// doExpand_collection

	/**
	* Remove the collection id from the expanded collection list
	*/
	static public void doCollapse_collection(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		HashMap currentMap = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);

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

		HashMap newSet = new HashMap();
		Iterator l = currentMap.keySet().iterator ();
		while (l.hasNext ())
		{
			// remove the collection id and all of the subcollections
//		    Resource collection = (Resource) l.next();
//			String id = (String) collection.getId();
		    String id = (String) l.next();

			if (id.indexOf (collectionId)==-1)
			{
	//			newSet.put(id,collection);
				newSet.put(id,currentMap.get(id));
			}
		}

		state.setAttribute(STATE_EXPANDED_COLLECTIONS, newSet);

		// remove this folder id into the set to be event-observed
		removeObservingPattern(collectionId, state);

	}	// doCollapse_collection

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
		if(! isStackEmpty(state))
		{
			Map current_stack_frame = peekAtStack(state);
			String createCollectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
			if(createCollectionId == null)
			{
				createCollectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			}
			if(createCollectionId != null)
			{
				currentCollectionId = createCollectionId;
			}
			else
			{
				String editCollectionId = (String) current_stack_frame.get(STATE_EDIT_COLLECTION_ID);
				if(editCollectionId == null)
				{
					editCollectionId = (String) state.getAttribute(STATE_EDIT_COLLECTION_ID);
				}
				if(editCollectionId != null)
				{
					currentCollectionId = editCollectionId;
				}
			}
		}
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);

		LinkedList collectionPath = new LinkedList();

		String previousCollectionId = "";
		Vector pathitems = new Vector();
		while(currentCollectionId != null && ! currentCollectionId.equals(navRoot) && ! currentCollectionId.equals(previousCollectionId))
		{
			pathitems.add(currentCollectionId);
			previousCollectionId = currentCollectionId;
			currentCollectionId = contentService.getContainingCollectionId(currentCollectionId);
		}
		pathitems.add(navRoot);

		if(!navRoot.equals(homeCollectionId))
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
				PathItem item = new PathItem(id, name);

				boolean canRead = contentService.allowGetCollection(id) || contentService.allowGetResource(id);
				item.setCanRead(canRead);

				String url = contentService.getUrl(id);
				item.setUrl(url);

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

	/**
	 * Get the items in this folder that should be seen.
	 * @param collectionId - String version of
	 * @param expandedCollections - Hash of collection resources
	 * @param sortedBy  - pass through to ContentHostingComparator
	 * @param sortedAsc - pass through to ContentHostingComparator
	 * @param parent - The folder containing this item
	 * @param isLocal - true if navigation root and home collection id of site are the same, false otherwise
	 * @param state - The session state
	 * @return a List of BrowseItem objects
	 */
	protected static List getBrowseItems(String collectionId, HashMap expandedCollections, Set highlightedItems, String sortedBy, String sortedAsc, BrowseItem parent, boolean isLocal, SessionState state)
	{
		boolean need_to_expand_all = Boolean.TRUE.toString().equals((String)state.getAttribute(STATE_NEED_TO_EXPAND_ALL));

		List newItems = new LinkedList();
		try
		{
			// find the ContentHosting service
			org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

			// get the collection
			// try using existing resource first
			ContentCollection collection = null;

			// get the collection
			if (expandedCollections.containsKey(collectionId))
			{
				collection = (ContentCollection) expandedCollections.get(collectionId);
			}
			else
			{
				collection = ContentHostingService.getCollection(collectionId);
				if(need_to_expand_all)
				{
					expandedCollections.put(collectionId, collection);
					state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
				}
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
				canDelete = contentService.allowRemoveResource(collectionId);
			}
			else
			{
				canDelete = parent.canDelete();
			}
			if(parent == null || ! parent.canRevise())
			{
				canRevise = contentService.allowUpdateResource(collectionId);
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
			boolean hasDeletableChildren = canDelete;
			boolean hasCopyableChildren = canRead;

			String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

			ResourceProperties cProperties = collection.getProperties();
			String folderName = cProperties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			if(collectionId.equals(homeCollectionId))
			{
				folderName = (String) state.getAttribute(STATE_HOME_COLLECTION_DISPLAY_NAME);
			}
			BrowseItem folder = new BrowseItem(collectionId, folderName, "folder");
			if(parent == null)
			{
				folder.setRoot(collectionId);
			}
			else
			{
				folder.setRoot(parent.getRoot());
			}
			
			BasicRightsAssignment rightsObj = new BasicRightsAssignment(folder.getItemNum(), cProperties);
			folder.setRights(rightsObj);
			
			AccessMode access = collection.getAccess();
			if(access == null || AccessMode.SITE.equals(access))
			{
				folder.setAccess(AccessMode.INHERITED.toString());
			}
			else
			{
				folder.setAccess(access.toString());
			}
			
			AccessMode inherited_access = collection.getInheritedAccess();
			if(inherited_access == null || AccessMode.SITE.equals(inherited_access))
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
				access_groups = new Vector();
			}
			Collection inherited_access_groups = collection.getInheritedGroupObjects();
			if(inherited_access_groups == null)
			{
				inherited_access_groups = new Vector();
			}
			folder.setInheritedGroups(access_groups);

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
			try
			{
				int collection_size = contentService.getCollectionSize(collectionId);
				folder.setIsEmpty(collection_size < 1);
				folder.setIsTooBig(collection_size > EXPANDABLE_FOLDER_SIZE_LIMIT);
			}
			catch(RuntimeException e)
			{
				folder.setIsEmpty(true);
				folder.setIsTooBig(false);
			}
			folder.setDepth(depth);
			newItems.add(folder);

			if(need_to_expand_all || expandedCollections.containsKey (collectionId))
			{
				// Get the collection members from the 'new' collection
				List newMembers = collection.getMemberResources ();

				Collections.sort (newMembers, ContentHostingService.newContentHostingComparator (sortedBy, Boolean.valueOf (sortedAsc).booleanValue ()));
				// loop thru the (possibly) new members and add to the list
				Iterator it = newMembers.iterator();
				while(it.hasNext())
				{
					ContentEntity resource = (ContentEntity) it.next();
					ResourceProperties props = resource.getProperties();

					String itemId = resource.getId();

					if(resource.isCollection())
					{
						List offspring = getBrowseItems(itemId, expandedCollections, highlightedItems, sortedBy, sortedAsc, folder, isLocal, state);
						if(! offspring.isEmpty())
						{
							BrowseItem child = (BrowseItem) offspring.get(0);
							hasDeletableChildren = hasDeletableChildren || child.hasDeletableChildren();
							hasCopyableChildren = hasCopyableChildren || child.hasCopyableChildren();
						}

						// add all the items in the subfolder to newItems
						newItems.addAll(offspring);
					}
					else
					{
						String itemType = ((ContentResource)resource).getContentType();
						String itemName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						BrowseItem newItem = new BrowseItem(itemId, itemName, itemType);

						BasicRightsAssignment rightsObj2 = new BasicRightsAssignment(newItem.getItemNum(), props);
						newItem.setRights(rightsObj2);
						AccessMode access_mode = ((GroupAwareEntity) resource).getAccess();
						if(access_mode == null)
						{
							newItem.setAccess(AccessMode.SITE.toString());
						}
						else
						{
							newItem.setAccess(access_mode.toString());
						}
						
						Collection groups = ((GroupAwareEntity) resource).getGroupObjects();
						if(groups == null)
						{
							groups = new Vector();
						}
						Collection inheritedGroups = ((GroupAwareEntity) resource).getInheritedGroupObjects();
						if(inheritedGroups == null)
						{
							inheritedGroups = new Vector();
						}
						newItem.setGroups(groups);	
						newItem.setInheritedGroups(inheritedGroups);

						newItem.setContainer(collectionId);
						newItem.setRoot(folder.getRoot());

						newItem.setCanDelete(canDelete);
						newItem.setCanRevise(canRevise);
						newItem.setCanRead(canRead);
						newItem.setCanCopy(canRead);
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

						if (checkItemFilter((ContentResource)resource, newItem, state)) 
						{
							newItems.add(newItem);
						}
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

	}	// getBrowseItems

	protected static boolean checkItemFilter(ContentResource resource, BrowseItem newItem, SessionState state) 
	{
		ContentResourceFilter filter = (ContentResourceFilter)state.getAttribute(STATE_ATTACH_FILTER);
	
	      if (filter != null) 
	      {
	    	  	if (newItem != null) 
	    	  	{
	    	  		newItem.setCanSelect(filter.allowSelect(resource));
	    	  	}
	    	  	return filter.allowView(resource);
	      }
	      else if (newItem != null) 
	      {
	    	  	newItem.setCanSelect(true);
	      }

	      return true;
	}

	protected static boolean checkSelctItemFilter(ContentResource resource, SessionState state) 
	{
		ContentResourceFilter filter = (ContentResourceFilter)state.getAttribute(STATE_ATTACH_FILTER);
		
		if (filter != null)
		{
			return filter.allowSelect(resource);
		}
		return true;
	}

    /**
	 * set the state name to be "copy" if any item has been selected for copying
	 */
	public void doCopyitem ( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		String itemId = data.getParameters ().getString ("itemId");

		if (itemId == null)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile6"));
			state.setAttribute (STATE_MODE, MODE_LIST);
		}
		else
		{
			try
			{
				ResourceProperties properties = ContentHostingService.getProperties (itemId);
				/*
				if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
				{
					String alert = (String) state.getAttribute(STATE_MESSAGE);
					if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
					{
						addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
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
				addAlert(state,RESOURCE_NOT_EXIST_STRING);
			}	// try-catch

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				state.setAttribute (STATE_COPY_FLAG, Boolean.TRUE.toString());

				state.setAttribute (STATE_COPIED_ID, itemId);
			}	// if-else
		}	// if-else

	}	// doCopyitem

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
					String helper_mode = (String) state.getAttribute(STATE_RESOURCES_HELPER_MODE);
					if(helper_mode != null && MODE_ATTACHMENT_NEW_ITEM.equals(helper_mode))
					{
						// add to the attachments vector
						List attachments = EntityManager.newReferenceList();
						Reference ref = EntityManager.newReference(ContentHostingService.getReference(id));
						attachments.add(ref);
						cleanupState(state);
						state.setAttribute(STATE_ATTACHMENTS, attachments);
					}
					else
					{
						if(state.getAttribute(STATE_ATTACH_LINKS) == null)
						{
							attachItem(id, state);
						}
						else
						{
							attachLink(id, state);
						}
					}
				}
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("notpermis8") + " " + originalDisplayName + ". ");
			}
			catch (IdUnusedException e)
			{
				addAlert(state,RESOURCE_NOT_EXIST_STRING);
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
				addAlert(state, rb.getString("toolong") + " " + e.getMessage());
			}
			catch(IdUniquenessException e)
			{
				addAlert(state, "Could not add this item to this folder");
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
				logger.warn("ResourcesAction.doPasteitems ***** Unknown Exception ***** " + e.getMessage());
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
				HashMap expandedCollections = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
				if(! expandedCollections.containsKey(collectionId))
				{
					org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
					try
					{
						ContentCollection coll = contentService.getCollection(collectionId);
						expandedCollections.put(collectionId, coll);
					}
					catch(Exception ignore){}
				}

				// reset the copy flag
				if (((String)state.getAttribute (STATE_COPY_FLAG)).equals (Boolean.TRUE.toString()))
				{
					state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());
				}
			}

		}

	}	// doPasteitems

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
					if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
					{
						addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
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
				addAlert(state,RESOURCE_NOT_EXIST_STRING);
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
				addAlert(state, rb.getString("overquota"));
			}	// try-catch
			catch(RuntimeException e)
			{
				logger.warn("ResourcesAction.doMoveitems ***** Unknown Exception ***** " + e.getMessage());
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
				HashMap expandedCollections = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
				if(! expandedCollections.containsKey(collectionId))
				{
					org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
					try
					{
						ContentCollection coll = contentService.getCollection(collectionId);
						expandedCollections.put(collectionId, coll);
					}
					catch(Exception ignore){}
				}

				// reset the copy flag
				if (((String)state.getAttribute (STATE_MOVE_FLAG)).equals (Boolean.TRUE.toString()))
				{
					state.setAttribute (STATE_MOVE_FLAG, Boolean.FALSE.toString());
				}
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

		String originalDisplayName = NULL_STRING;

		try
		{
			ResourceProperties properties = ContentHostingService.getProperties (itemId);
			originalDisplayName = properties.getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME);

			// copy, cut and paste not operated on collections
			if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
			{
				String alert = (String) state.getAttribute(STATE_MESSAGE);
				if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
				{
					addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
				}
			}
			else
			{
				// paste the resource
				ContentResource resource = ContentHostingService.getResource (itemId);
				ResourceProperties p = ContentHostingService.getProperties(itemId);
				String displayName = DUPLICATE_STRING + p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

				String newItemId = ContentHostingService.copyIntoFolder(itemId, collectionId);

				ContentResourceEdit copy = ContentHostingService.editResource(newItemId);
				ResourcePropertiesEdit pedit = copy.getPropertiesEdit();
				pedit.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
				ContentHostingService.commitResource(copy, NotificationService.NOTI_NONE);

			}	// if-else
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("notpermis8") + " " + originalDisplayName + ". ");
		}
		catch (IdUnusedException e)
		{
			addAlert(state,RESOURCE_NOT_EXIST_STRING);
		}
		catch (IdUsedException e)
		{
			addAlert(state, rb.getString("notaddreso") + " " + originalDisplayName + " " + rb.getString("used2"));
		}
		catch(IdLengthException e)
		{
			addAlert(state, rb.getString("toolong") + " " + e.getMessage());
		}
		catch(IdUniquenessException e)
		{
			addAlert(state, "Could not add this item to this folder");
		}
		catch (InconsistentException ee)
		{
			addAlert(state, RESOURCE_INVALID_TITLE_STRING);
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
			HashMap expandedCollections = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			if(! expandedCollections.containsKey(collectionId))
			{
				org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
				try
				{
					ContentCollection coll = contentService.getCollection(collectionId);
					expandedCollections.put(collectionId, coll);
				}
				catch(Exception ignore){}
			}

			// reset the copy flag
			if (((String)state.getAttribute (STATE_COPY_FLAG)).equals (Boolean.TRUE.toString()))
			{
				state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());
			}
		}

	}	// doPasteitem

	/**
	* Fire up the permissions editor for the current folder's permissions
	*/
	public void doFolder_permissions(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());
		ParameterParser params = data.getParameters();

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

		// get the current collection id and the related site
		String collectionId = params.getString("collectionId"); //(String) state.getAttribute (STATE_COLLECTION_ID);
		String title = "";
		try
		{
			title = ContentHostingService.getProperties(collectionId).getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("notread"));
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("notfindfol"));
		}

		// the folder to edit
		Reference ref = EntityManager.newReference(ContentHostingService.getReference(collectionId));
		state.setAttribute(PermissionsHelper.TARGET_REF, ref.getReference());

		// use the folder's context (as a site) for roles
		String siteRef = SiteService.siteReference(ref.getContext());
		state.setAttribute(PermissionsHelper.ROLES_REF, siteRef);

		// ... with this description
		state.setAttribute(PermissionsHelper.DESCRIPTION, rb.getString("setpermis") + " " + title);

		// ... showing only locks that are prpefixed with this
		state.setAttribute(PermissionsHelper.PREFIX, "content.");

		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.permissions.helper");

	}	// doFolder_permissions

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
	* is notification enabled?
	*/
	protected boolean notificationEnabled(SessionState state)
	{
		return true;

	}	// notificationEnabled

	/**
	 * Processes the HTML document that is coming back from the browser
	 * (from the formatted text editing widget).
	 * @param state Used to pass in any user-visible alerts or errors when processing the text
	 * @param strFromBrowser The string from the browser
	 * @return The formatted text
	 */
	private String processHtmlDocumentFromBrowser(SessionState state, String strFromBrowser)
	{
		StringBuffer alertMsg = new StringBuffer();
		String text = FormattedText.processHtmlDocument(strFromBrowser, alertMsg);
		if (alertMsg.length() > 0) addAlert(state, alertMsg.toString());
		return text;
	}

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
		if (displayName.indexOf(SHORTCUT_STRING) != -1)
		{
			rv = false;
		}

		return rv;

	}	// replaceable

	/**
	 *
	 * put copyright info into context
	 */
	private static void copyrightChoicesIntoContext(SessionState state, Context context)
	{
		boolean usingCreativeCommons = state.getAttribute(STATE_USING_CREATIVE_COMMONS) != null && state.getAttribute(STATE_USING_CREATIVE_COMMONS).equals(Boolean.TRUE.toString());		
		
		if(usingCreativeCommons)
		{
			
			String ccOwnershipLabel = "Who created this resource?";
			List ccOwnershipList = new Vector();
			ccOwnershipList.add("-- Select --");
			ccOwnershipList.add("I created this resource");
			ccOwnershipList.add("Someone else created this resource");
			
			String ccMyGrantLabel = "Terms of use";
			List ccMyGrantOptions = new Vector();
			ccMyGrantOptions.add("-- Select --");
			ccMyGrantOptions.add("Use my copyright");
			ccMyGrantOptions.add("Use Creative Commons License");
			ccMyGrantOptions.add("Use Public Domain Dedication");
			
			String ccCommercialLabel = "Allow commercial use?";
			List ccCommercialList = new Vector();
			ccCommercialList.add("Yes");
			ccCommercialList.add("No");
			
			String ccModificationLabel = "Allow Modifications?";
			List ccModificationList = new Vector();
			ccModificationList.add("Yes");
			ccModificationList.add("Yes, share alike");
			ccModificationList.add("No");
			
			String ccOtherGrantLabel = "Terms of use";
			List ccOtherGrantList = new Vector();
			ccOtherGrantList.add("Subject to fair-use exception");
			ccOtherGrantList.add("Public domain (created before copyright law applied)");
			ccOtherGrantList.add("Public domain (copyright has expired)");
			ccOtherGrantList.add("Public domain (government document not subject to copyright)");
			
			String ccRightsYear = "Year";
			String ccRightsOwner = "Copyright owner";
			
			String ccAcknowledgeLabel = "Require users to acknowledge author's rights before access?";
			List ccAcknowledgeList = new Vector();
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
			if (state.getAttribute(COPYRIGHT_FAIRUSE_URL) != null)
			{
				context.put("fairuseurl", state.getAttribute(COPYRIGHT_FAIRUSE_URL));
			}
			if (state.getAttribute(NEW_COPYRIGHT_INPUT) != null)
			{
				context.put("newcopyrightinput", state.getAttribute(NEW_COPYRIGHT_INPUT));
			}
	
			if (state.getAttribute(COPYRIGHT_TYPES) != null)
			{
				List copyrightTypes = (List) state.getAttribute(COPYRIGHT_TYPES);
				context.put("copyrightTypes", copyrightTypes);
				context.put("copyrightTypesSize", new Integer(copyrightTypes.size() - 1));
				context.put("USE_THIS_COPYRIGHT", copyrightTypes.get(copyrightTypes.size() - 1));
			}
		}
		
		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		context.put("preventPublicDisplay", preventPublicDisplay);
		
	}	// copyrightChoicesIntoContext

	/**
	 * Add variables and constants to the velocity context to render an editor
	 * for inputing and modifying optional metadata properties about a resource.
	 */
	private static void metadataGroupsIntoContext(SessionState state, Context context)
	{

		context.put("STRING", ResourcesMetadata.WIDGET_STRING);
		context.put("TEXTAREA", ResourcesMetadata.WIDGET_TEXTAREA);
		context.put("BOOLEAN", ResourcesMetadata.WIDGET_BOOLEAN);
		context.put("INTEGER", ResourcesMetadata.WIDGET_INTEGER);
		context.put("DOUBLE", ResourcesMetadata.WIDGET_DOUBLE);
		context.put("DATE", ResourcesMetadata.WIDGET_DATE);
		context.put("TIME", ResourcesMetadata.WIDGET_TIME);
		context.put("DATETIME", ResourcesMetadata.WIDGET_DATETIME);
		context.put("ANYURI", ResourcesMetadata.WIDGET_ANYURI);
		context.put("WYSIWYG", ResourcesMetadata.WIDGET_WYSIWYG);

		context.put("today", TimeService.newTime());

		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups != null && !metadataGroups.isEmpty())
		{
			context.put("metadataGroups", metadataGroups);
		}

	}	// metadataGroupsIntoContext

	/**
	 * initialize the metadata context
	 */
	private static void initMetadataContext(SessionState state)
	{
		// define MetadataSets map
		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups == null)
		{
			metadataGroups = new Vector();
			state.setAttribute(STATE_METADATA_GROUPS, metadataGroups);
		}
		// define DublinCore
		if( !metadataGroups.contains(new MetadataGroup(rb.getString("opt_props"))) )
		{
			MetadataGroup dc = new MetadataGroup( rb.getString("opt_props") );
			// dc.add(ResourcesMetadata.PROPERTY_DC_TITLE);
			// dc.add(ResourcesMetadata.PROPERTY_DC_DESCRIPTION);
			dc.add(ResourcesMetadata.PROPERTY_DC_ALTERNATIVE);
			dc.add(ResourcesMetadata.PROPERTY_DC_CREATOR);
			dc.add(ResourcesMetadata.PROPERTY_DC_PUBLISHER);
			dc.add(ResourcesMetadata.PROPERTY_DC_SUBJECT);
			dc.add(ResourcesMetadata.PROPERTY_DC_CREATED);
			dc.add(ResourcesMetadata.PROPERTY_DC_ISSUED);
			// dc.add(ResourcesMetadata.PROPERTY_DC_MODIFIED);
			// dc.add(ResourcesMetadata.PROPERTY_DC_TABLEOFCONTENTS);
			dc.add(ResourcesMetadata.PROPERTY_DC_ABSTRACT);
			dc.add(ResourcesMetadata.PROPERTY_DC_CONTRIBUTOR);
			// dc.add(ResourcesMetadata.PROPERTY_DC_TYPE);
			// dc.add(ResourcesMetadata.PROPERTY_DC_FORMAT);
			// dc.add(ResourcesMetadata.PROPERTY_DC_IDENTIFIER);
			// dc.add(ResourcesMetadata.PROPERTY_DC_SOURCE);
			// dc.add(ResourcesMetadata.PROPERTY_DC_LANGUAGE);
			// dc.add(ResourcesMetadata.PROPERTY_DC_COVERAGE);
			// dc.add(ResourcesMetadata.PROPERTY_DC_RIGHTS);
			dc.add(ResourcesMetadata.PROPERTY_DC_AUDIENCE);
			dc.add(ResourcesMetadata.PROPERTY_DC_EDULEVEL);
			metadataGroups.add(dc);
			state.setAttribute(STATE_METADATA_GROUPS, metadataGroups);
		}
		/*
		// define DublinCore
		if(!metadataGroups.contains(new MetadataGroup("Test of Datatypes")))
		{
			MetadataGroup dc = new MetadataGroup("Test of Datatypes");
			dc.add(ResourcesMetadata.PROPERTY_DC_TITLE);
			dc.add(ResourcesMetadata.PROPERTY_DC_DESCRIPTION);
			dc.add(ResourcesMetadata.PROPERTY_DC_ANYURI);
			dc.add(ResourcesMetadata.PROPERTY_DC_DOUBLE);
			dc.add(ResourcesMetadata.PROPERTY_DC_DATETIME);
			dc.add(ResourcesMetadata.PROPERTY_DC_TIME);
			dc.add(ResourcesMetadata.PROPERTY_DC_DATE);
			dc.add(ResourcesMetadata.PROPERTY_DC_BOOLEAN);
			dc.add(ResourcesMetadata.PROPERTY_DC_INTEGER);
			metadataGroups.add(dc);
			state.setAttribute(STATE_METADATA_GROUPS, metadataGroups);
		}
		*/
	}

	/**
	 * Internal class that encapsulates all information about a resource that is needed in the browse mode
	 */
	public static class BrowseItem
	{
		protected static Integer seqnum = new Integer(0);
		private String m_itemnum;
		
		// attributes of all resources
		protected String m_name;
		protected String m_id;
		protected String m_type;
		protected boolean m_canRead;
		protected boolean m_canRevise;
		protected boolean m_canDelete;
		protected boolean m_canCopy;
		protected boolean m_isCopied;
		protected boolean m_canAddItem;
		protected boolean m_canAddFolder;
		protected boolean m_canSelect;

		protected List m_members;
		protected boolean m_isEmpty;
		protected boolean m_isHighlighted;
		protected boolean m_inheritsHighlight;
		protected String m_createdBy;
		protected String m_createdTime;
		protected String m_modifiedBy;
		protected String m_modifiedTime;
		protected String m_size;
		protected String m_target;
		protected String m_container;
		protected String m_root;
		protected int m_depth;
		protected boolean m_hasDeletableChildren;
		protected boolean m_hasCopyableChildren;
		protected boolean m_copyrightAlert;
		protected String m_url;
		protected boolean m_isLocal;
		protected boolean m_isAttached;
		private boolean m_isMoved;
		private boolean m_canUpdate;
		private boolean m_toobig;
		protected String m_access;
		protected String m_inheritedAccess;
		protected List m_groups;
		protected List m_inheritedGroups;
		protected BasicRightsAssignment m_rights;


		/**
		 * @param id
		 * @param name
		 * @param type
		 */
		public BrowseItem(String id, String name, String type)
		{
			m_name = name;
			m_id = id;
			m_type = type;
			
			Integer snum; 
			synchronized(seqnum)
			{
				snum = seqnum;
				seqnum = new Integer((seqnum.intValue() + 1) % 10000);
			}
			m_itemnum = "Item00000000".substring(0,10 - snum.toString().length()) + snum.toString();

			// set defaults
			m_rights = new BasicRightsAssignment(m_itemnum, false);
			m_members = new LinkedList();
			m_canRead = false;
			m_canRevise = false;
			m_canDelete = false;
			m_canCopy = false;
			m_isEmpty = true;
			m_toobig = false;
			m_isCopied = false;
			m_isMoved = false;
			m_isAttached = false;
			m_canSelect = true; // default is true.
			m_hasDeletableChildren = false;
			m_hasCopyableChildren = false;
			m_createdBy = "";
			m_modifiedBy = "";
			// m_createdTime = TimeService.newTime().toStringLocalDate();
			// m_modifiedTime = TimeService.newTime().toStringLocalDate();
			m_size = "";
			m_depth = 0;
			m_copyrightAlert = false;
			m_url = "";
			m_target = "";
			m_root = "";

			m_isHighlighted = false;
			m_inheritsHighlight = false;

			m_canAddItem = false;
			m_canAddFolder = false;
			m_canUpdate = false;
			
			m_access = AccessMode.INHERITED.toString();
			m_groups = new Vector();
		
		}

		public String getItemNum()
		{
			return m_itemnum;
		}

		public void setIsTooBig(boolean toobig)
		{
			m_toobig = toobig;
		}

		public boolean isTooBig()
		{
			return m_toobig;
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
		 * @return
		 */
		public String getRoot()
		{
			return m_root;
		}

		/**
		 * @return
		 */
		public List getMembers()
		{
			List rv = new LinkedList();
			if(m_members != null)
			{
				rv.addAll(m_members);
			}
			return rv;
		}

		/**
		 * @param members
		 */
		public void addMembers(Collection members)
		{
			if(m_members == null)
			{
				m_members = new LinkedList();
			}
			m_members.addAll(members);
		}

		/**
		 * @return
		 */
		public boolean canAddItem()
		{
			return m_canAddItem;
		}

		/**
		 * @return
		 */
		public boolean canDelete()
		{
			return m_canDelete;
		}

		/**
		 * @return
		 */
		public boolean canRead()
		{
			return m_canRead;
		}

      public boolean canSelect() {
         return m_canSelect;
      }

		/**
		 * @return
		 */
		public boolean canRevise()
		{
			return m_canRevise;
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
		public int getDepth()
		{
			return m_depth;
		}

		/**
		 * @param depth
		 */
		public void setDepth(int depth)
		{
			m_depth = depth;
		}

		/**
		 * @param canCreate
		 */
		public void setCanAddItem(boolean canAddItem)
		{
			m_canAddItem = canAddItem;
		}

		/**
		 * @param canDelete
		 */
		public void setCanDelete(boolean canDelete)
		{
			m_canDelete = canDelete;
		}

		/**
		 * @param canRead
		 */
		public void setCanRead(boolean canRead)
		{
			m_canRead = canRead;
		}

      public void setCanSelect(boolean canSelect) {
         m_canSelect = canSelect;
      }

		/**
		 * @param canRevise
		 */
		public void setCanRevise(boolean canRevise)
		{
			m_canRevise = canRevise;
		}

		/**
		 * @return
		 */
		public boolean isFolder()
		{
			return TYPE_FOLDER.equals(m_type);
		}

		/**
		 * @return
		 */
		public String getType()
		{
			return m_type;
		}

		/**
		 * @return
		 */
		public boolean canAddFolder()
		{
			return m_canAddFolder;
		}

		/**
		 * @param b
		 */
		public void setCanAddFolder(boolean canAddFolder)
		{
			m_canAddFolder = canAddFolder;
		}

		/**
		 * @return
		 */
		public boolean canCopy()
		{
			return m_canCopy;
		}

		/**
		 * @param canCopy
		 */
		public void setCanCopy(boolean canCopy)
		{
			m_canCopy = canCopy;
		}

		/**
		 * @return
		 */
		public boolean hasCopyrightAlert()
		{
			return m_copyrightAlert;
		}

		/**
		 * @param copyrightAlert
		 */
		public void setCopyrightAlert(boolean copyrightAlert)
		{
			m_copyrightAlert = copyrightAlert;
		}

		/**
		 * @return
		 */
		public String getUrl()
		{
			return m_url;
		}

		/**
		 * @param url
		 */
		public void setUrl(String url)
		{
			m_url = url;
		}

		/**
		 * @return
		 */
		public boolean isCopied()
		{
			return m_isCopied;
		}

		/**
		 * @param isCopied
		 */
		public void setCopied(boolean isCopied)
		{
			m_isCopied = isCopied;
		}

		/**
		 * @return
		 */
		public boolean isMoved()
		{
			return m_isMoved;
		}

		/**
		 * @param isCopied
		 */
		public void setMoved(boolean isMoved)
		{
			m_isMoved = isMoved;
		}

		/**
		 * @return
		 */
		public String getCreatedBy()
		{
			return m_createdBy;
		}

		/**
		 * @return
		 */
		public String getCreatedTime()
		{
			return m_createdTime;
		}

		/**
		 * @return
		 */
		public String getModifiedBy()
		{
			return m_modifiedBy;
		}

		/**
		 * @return
		 */
		public String getModifiedTime()
		{
			return m_modifiedTime;
		}

		/**
		 * @return
		 */
		public String getSize()
		{
			if(m_size == null)
			{
				m_size = "";
			}
			return m_size;
		}

		/**
		 * @param creator
		 */
		public void setCreatedBy(String creator)
		{
			m_createdBy = creator;
		}

		/**
		 * @param time
		 */
		public void setCreatedTime(String time)
		{
			m_createdTime = time;
		}

		/**
		 * @param modifier
		 */
		public void setModifiedBy(String modifier)
		{
			m_modifiedBy = modifier;
		}

		/**
		 * @param time
		 */
		public void setModifiedTime(String time)
		{
			m_modifiedTime = time;
		}

		/**
		 * @param size
		 */
		public void setSize(String size)
		{
			m_size = size;
		}

		/**
		 * @return
		 */
		public String getTarget()
		{
			return m_target;
		}

		/**
		 * @param target
		 */
		public void setTarget(String target)
		{
			m_target = target;
		}

		/**
		 * @return
		 */
		public boolean isEmpty()
		{
			return m_isEmpty;
		}

		/**
		 * @param isEmpty
		 */
		public void setIsEmpty(boolean isEmpty)
		{
			m_isEmpty = isEmpty;
		}

		/**
		 * @return
		 */
		public String getContainer()
		{
			return m_container;
		}

		/**
		 * @param container
		 */
		public void setContainer(String container)
		{
			m_container = container;
		}

		public void setIsLocal(boolean isLocal)
		{
			m_isLocal = isLocal;
		}

		public boolean isLocal()
		{
			return m_isLocal;
		}

		/**
		 * @return Returns the isAttached.
		 */
		public boolean isAttached()
		{
			return m_isAttached;
		}
		/**
		 * @param isAttached The isAttached to set.
		 */
		public void setAttached(boolean isAttached)
		{
			this.m_isAttached = isAttached;
		}

		/**
		 * @return Returns the hasCopyableChildren.
		 */
		public boolean hasCopyableChildren()
		{
			return m_hasCopyableChildren;
		}

		/**
		 * @param hasCopyableChildren The hasCopyableChildren to set.
		 */
		public void setCopyableChildren(boolean hasCopyableChildren)
		{
			this.m_hasCopyableChildren = hasCopyableChildren;
		}

		/**
		 * @return Returns the hasDeletableChildren.
		 */
		public boolean hasDeletableChildren()
		{
			return m_hasDeletableChildren;
		}

		/**
		 * @param hasDeletableChildren The hasDeletableChildren to set.
		 */
		public void seDeletableChildren(boolean hasDeletableChildren)
		{
			this.m_hasDeletableChildren = hasDeletableChildren;
		}

		/**
		 * @return Returns the canUpdate.
		 */
		public boolean canUpdate()
		{
			return m_canUpdate;
		}

		/**
		 * @param canUpdate The canUpdate to set.
		 */
		public void setCanUpdate(boolean canUpdate)
		{
			m_canUpdate = canUpdate;
		}

		public void setHighlighted(boolean isHighlighted)
		{
			m_isHighlighted = isHighlighted;
		}

		public boolean isHighlighted()
		{
			return m_isHighlighted;
		}

		public void setInheritsHighlight(boolean inheritsHighlight)
		{
			m_inheritsHighlight = inheritsHighlight;
		}

		public boolean inheritsHighlighted()
		{
			return m_inheritsHighlight;
		}

		/**
		 * Access the access mode for this item.
		 * @return The access mode.
		 */
		public String getAccess()
		{
			return m_access;
		}

		/**
		 * Access the access mode for this item.
		 * @return The access mode.
		 */
		public String getInheritedAccess()
		{
			return m_inheritedAccess;
		}
		
		public String getEntityAccess()
		{
			String rv = AccessMode.INHERITED.toString();
			boolean sameGroups = true;
			if(AccessMode.GROUPED.toString().equals(m_access))
			{
				Iterator it = getGroups().iterator();
				while(sameGroups && it.hasNext())
				{
					Group g = (Group) it.next();
					sameGroups = inheritsGroup(g.getReference());
				}
				it = getInheritedGroups().iterator();
				while(sameGroups && it.hasNext())
				{
					Group g = (Group) it.next();
					sameGroups = hasGroup(g.getReference());
				}
				if(!sameGroups)
				{
					rv = AccessMode.GROUPED.toString();
				}
			}
			return rv;
		}
		
		public String getEffectiveAccess()
		{
			String rv = this.m_access;
			if(AccessMode.INHERITED.toString().equals(rv))
			{
				rv = this.m_inheritedAccess;
			}
			if(AccessMode.INHERITED.toString().equals(rv))
			{
				rv = AccessMode.SITE.toString();
			}
			return rv;
		}
		
		public String getEffectiveGroups()
		{
			String rv = rb.getString("access.site1");
			if(AccessMode.GROUPED.toString().equals(getEffectiveAccess()))
			{
				rv = rb.getString("access.group1") + " ";
				Collection groups = getGroups();
				if(groups == null || groups.isEmpty())
				{
					groups = getInheritedGroups();
				}
				if(groups == null || groups.isEmpty())
				{
					rv = rb.getString("access.site1");
				}
				else
				{
					Iterator it = groups.iterator();
					while(it.hasNext())
					{
						Group g = (Group) it.next();
						rv += g.getTitle();
						if(it.hasNext())
						{
							rv += ", ";
						}
					}
				}
			}
			return rv;
		}

		/**
		 * Set the access mode for this item.
		 * @param access
		 */
		public void setAccess(String access)
		{
			m_access = access;
		}

		/**
		 * Set the access mode for this item.
		 * @param access
		 */
		public void setInheritedAccess(String access)
		{
			m_inheritedAccess = access;
		}

		/**
		 * Access a list of Group objects that can access this item.
		 * @return Returns the groups.
		 */
		public List getGroups()
		{
			if(m_groups == null)
			{
				m_groups = new Vector();
			}
			return m_groups;
		}
		
		/**
		 * Access a list of Group objects that can access this item.
		 * @return Returns the groups.
		 */
		public List getInheritedGroups()
		{
			if(m_inheritedGroups == null)
			{
				m_inheritedGroups = new Vector();
			}
			return m_inheritedGroups;
		}
		
		/**
		 * Determine whether a group has access to this item. 
		 * @param groupRef The internal reference string that uniquely identifies the group.
		 * @return true if the group has access, false otherwise.
		 */
		public boolean hasGroup(String groupRef)
		{
			if(m_groups == null)
			{
				m_groups = new Vector();
			}
			boolean found = false;
			Iterator it = m_groups.iterator();
			while(it.hasNext() && !found)
			{
				Group gr = (Group) it.next();
				found = gr.getReference().equals(groupRef);
			}
	
			return found;
		}

		/**
		 * Determine whether a group has access to this item. 
		 * @param groupRef The internal reference string that uniquely identifies the group.
		 * @return true if the group has access, false otherwise.
		 */
		public boolean inheritsGroup(String groupRef)
		{
			if(m_inheritedGroups == null)
			{
				m_inheritedGroups = new Vector();
			}
			boolean found = false;
			Iterator it = m_inheritedGroups.iterator();
			while(it.hasNext() && !found)
			{
				Group gr = (Group) it.next();
				found = gr.getReference().equals(groupRef);
			}
	
			return found;
		}

		/**
		 * Replace the current list of groups with this list of Group objects representing the groups that have access to this item.
		 * @param groups The groups to set.
		 */
		public void setGroups(Collection groups)
		{
			if(groups == null)
			{
				return;
			}
			if(m_groups == null)
			{
				m_groups = new Vector();
			}
			m_groups.clear();
			Iterator it = groups.iterator();
			while(it.hasNext())
			{
				Object obj = it.next();
				if(obj instanceof Group && ! hasGroup(((Group) obj).getReference()))
				{
					m_groups.add(obj);
				}
				else if(obj instanceof String && ! hasGroup((String) obj))
				{
					addGroup((String) obj);
				}
			}
		}
		
		/**
		 * Replace the current list of groups with this list of Group objects representing the groups that have access to this item.
		 * @param groups The groups to set.
		 */
		public void setInheritedGroups(Collection groups)
		{
			if(groups == null)
			{
				return;
			}
			if(m_inheritedGroups == null)
			{
				m_inheritedGroups = new Vector();
			}
			m_inheritedGroups.clear();
			Iterator it = groups.iterator();
			while(it.hasNext())
			{
				Object obj = it.next();
				if(obj instanceof Group && ! inheritsGroup(((Group) obj).getReference()))
				{
					m_inheritedGroups.add(obj);
				}
				else if(obj instanceof String && ! hasGroup((String) obj))
				{
					addInheritedGroup((String) obj);
				}
			}
		}
		
		/**
		 * Add a string reference identifying a Group to the list of groups that have access to this item.
		 * @param groupRef
		 */
		public void addGroup(String groupId)
		{
			if(m_groups == null)
			{
				m_groups = new Vector();
			}
			if(m_container == null)
			{
				if(m_id == null)
				{
					m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
				}
				else
				{
					m_container = ContentHostingService.getContainingCollectionId(m_id);
				}
				if(m_container == null || m_container.trim() == "")
				{
					m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
				}

			}
			boolean found = false;
			Collection groups = ContentHostingService.getGroupsWithReadAccess(m_container);
			Iterator it = groups.iterator();
			while( it.hasNext() && !found )
			{
				Group group = (Group) it.next();
				if(group.getId().equals(groupId))
				{
					if(! hasGroup(group.getReference()))
					{
						m_groups.add(group);
					}
					found = true;
				}
			}

		}
		
		/**
		 * Add a string reference identifying a Group to the list of groups that have access to this item.
		 * @param groupRef
		 */
		public void addInheritedGroup(String groupId)
		{
			if(m_inheritedGroups == null)
			{
				m_inheritedGroups = new Vector();
			}
			if(m_container == null)
			{
				if(m_id == null)
				{
					m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
				}
				else
				{
					m_container = ContentHostingService.getContainingCollectionId(m_id);
				}
				if(m_container == null || m_container.trim() == "")
				{
					m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
				}

			}
			boolean found = false;
			Collection groups = ContentHostingService.getGroupsWithReadAccess(m_container);
			Iterator it = groups.iterator();
			while( it.hasNext() && !found )
			{
				Group group = (Group) it.next();
				if(group.getId().equals(groupId))
				{
					if(! inheritsGroup(group.getReference()))
					{
						m_inheritedGroups.add(group);
					}
					found = true;
				}
			}

		}
		
		/**
		 * Remove all groups from the item.
		 */
		public void clearGroups()
		{
			if(this.m_groups == null)
			{
				m_groups = new Vector();
			}
			m_groups.clear();
		}

		/**
		 * Remove all inherited groups from the item.
		 */
		public void clearInheritedGroups()
		{
			if(m_inheritedGroups == null)
			{
				m_inheritedGroups = new Vector();
			}
			m_inheritedGroups.clear();
		}

		/**
		 * @return Returns the rights.
		 */
		public BasicRightsAssignment getRights()
		{
			return m_rights;
		}

		/**
		 * @param rights The rights to set.
		 */
		public void setRights(BasicRightsAssignment rights)
		{
			this.m_rights = rights;
		}

	}	// inner class BrowseItem


	/**
	 * Inner class encapsulates information about resources (folders and items) for editing
	 */
	public static class EditItem
		extends BrowseItem
	{
		protected String m_copyrightStatus;
		protected String m_copyrightInfo;
		// protected boolean m_copyrightAlert;
		protected boolean m_pubview;
		protected boolean m_pubviewset;
		protected String m_filename;
		protected byte[] m_content;
		protected String m_encoding;

		protected String m_mimetype;
		protected String m_description;
		protected Map m_metadata;
		protected boolean m_hasQuota;
		protected boolean m_canSetQuota;
		protected String m_quota;
		protected boolean m_isUrl;
		protected boolean m_contentHasChanged;
		protected boolean m_contentTypeHasChanged;
		protected int m_notification = NotificationService.NOTI_NONE;

		protected String m_formtype;
		protected String m_rootname;
		protected Map m_structuredArtifact;
		protected List m_properties;

		protected Set m_metadataGroupsShowing;

		protected Set m_missingInformation;
		protected boolean m_hasBeenAdded;
		protected ResourcesMetadata m_form;
		protected boolean m_isBlank;
		protected String m_instruction;
		protected String m_ccRightsownership;
		protected String m_ccLicense;
		protected String m_ccCommercial;
		protected String m_ccModification;
		protected String m_ccRightsOwner;
		protected String m_ccRightsYear;

		/**
		 * @param id
		 * @param name
		 * @param type
		 */
		public EditItem(String id, String name, String type)
		{
			super(id, name, type);
			
			m_filename = "";
			m_contentHasChanged = false;
			m_contentTypeHasChanged = false;
			m_metadata = new Hashtable();
			m_structuredArtifact = new Hashtable();
			m_metadataGroupsShowing = new HashSet();
			m_mimetype = type;
			m_content = null;
			m_encoding = "UTF-8";
			m_notification = NotificationService.NOTI_NONE;
			m_hasQuota = false;
			m_canSetQuota = false;
			m_formtype = "";
			m_rootname = "";
			m_missingInformation = new HashSet();
			m_hasBeenAdded = false;
			m_properties = new Vector();
			m_isBlank = true;
			m_instruction = "";
			m_pubview = false;
			m_pubviewset = false;
			m_ccRightsownership = "";
			m_ccLicense = "";
			// m_copyrightStatus = ServerConfigurationService.getString("default.copyright");

		}
		
		public void setRightsowner(String ccRightsOwner)
		{
			m_ccRightsOwner = ccRightsOwner;
		}
		
		public String getRightsowner()
		{
			return m_ccRightsOwner;
		}

		public void setRightstyear(String ccRightsYear)
		{
			m_ccRightsYear = ccRightsYear;
		}
		
		public String getRightsyear()
		{
			return m_ccRightsYear;
		}

		public void setAllowModifications(String ccModification)
		{
			m_ccModification = ccModification;
		}
		
		public String getAllowModifications()
		{
			return m_ccModification;
		}

		public void setAllowCommercial(String ccCommercial)
		{
			m_ccCommercial = ccCommercial;
		}
		
		public String getAllowCommercial()
		{
			return m_ccCommercial;
		}

		/**
		 * 
		 * @param license
		 */
		public void setLicense(String license)
		{
			m_ccLicense = license;
		}
		
		/**
		 * 
		 * @return
		 */
		public String getLicense()
		{
			return m_ccLicense;
		}

		/**
		 * Record a value for instructions to be displayed to the user in the editor (for Form Items).
		 * @param instruction The value of the instructions.
		 */
		public void setInstruction(String instruction)
		{
			if(instruction == null)
			{
				instruction = "";
			}

			m_instruction = instruction.trim();
		}

		/**
		 * Access instructions to be displayed to the user in the editor (for Form Items).
		 * @return The instructions.
		 */
		public String getInstruction()
		{
			return m_instruction;
		}

		/**
		 * Set the character encoding type that will be used when converting content body between strings and byte arrays.
		 * Default is "UTF-8".
		 * @param encoding A valid name for a character set encoding scheme (@see java.lang.Charset)
		 */
		public void setEncoding(String encoding)
		{
			m_encoding = encoding;
		}

		/**
		 * Get the character encoding type that is used when converting content body between strings and byte arrays.
		 * Default is "UTF-8".
		 * @return The name of the character set encoding scheme (@see java.lang.Charset)
		 */
		public String getEncoding()
		{
			return m_encoding;
		}

		/**
		 * Set marker indicating whether current item is a blank entry
		 * @param isBlank
		 */
		public void markAsBlank(boolean isBlank)
		{
			m_isBlank = isBlank;
		}

		/**
		 * Access marker indicating whether current item is a blank entry
		 * @return true if current entry is blank, false otherwise
		 */
		public boolean isBlank()
		{
			return m_isBlank;
		}

		/**
		 * Change the root ResourcesMetadata object that defines the form for a Structured Artifact.
		 * @param form
		 */
		public void setForm(ResourcesMetadata form)
		{
			m_form = form;
		}

		/**
		 * Access the root ResourcesMetadata object that defines the form for a Structured Artifact.
		 * @return the form.
		 */
		public ResourcesMetadata getForm()
		{
			return m_form;
		}

		/**
		 * @param properties
		 */
		public void setProperties(List properties)
		{
			m_properties = properties;

		}

		public List getProperties()
		{
			return m_properties;
		}



		/**
		 * Replace current values of Structured Artifact with new values.
		 * @param map The new values.
		 */
		public void setValues(Map map)
		{
			m_structuredArtifact = map;

		}

		/**
		 * Access the entire set of values stored in the Structured Artifact
		 * @return The set of values.
		 */
		public Map getValues()
		{
			return m_structuredArtifact;

		}

		/**
		 * @param id
		 * @param name
		 * @param type
		 */
		public EditItem(String type)
		{
			this(null, "", type);
		}
		
		/**
		 * @param id
		 */
		public void setId(String id)
		{
			m_id = id;
		}

		/**
		 * Show the indicated metadata group for the item
		 * @param group
		 */
		public void showMetadataGroup(String group)
		{
			m_metadataGroupsShowing.add(group);
		}

		/**
		 * Hide the indicated metadata group for the item
		 * @param group
		 */
		public void hideMetadataGroup(String group)
		{
			m_metadataGroupsShowing.remove(group);
			m_metadataGroupsShowing.remove(Validator.escapeUrl(group));
		}

		/**
		 * Query whether the indicated metadata group is showing for the item
		 * @param group
		 * @return true if the metadata group is showing, false otherwise
		 */
		public boolean isGroupShowing(String group)
		{
			return m_metadataGroupsShowing.contains(group) || m_metadataGroupsShowing.contains(Validator.escapeUrl(group));
		}

		/**
		 * @return
		 */
		public boolean isFileUpload()
		{
			return !isFolder() && !isUrl() && !isHtml() && !isPlaintext() && !isStructuredArtifact();
		}

		/**
		 * @param type
		 */
		public void setType(String type)
		{
			m_type = type;
		}

		/**
		 * @param mimetype
		 */
		public void setMimeType(String mimetype)
		{
			m_mimetype = mimetype;
		}
		
		public String getRightsownership()
		{
			return m_ccRightsownership;
		}
		
		public void setRightsownership(String owner)
		{
			m_ccRightsownership = owner;
		}

		/**
		 * @return
		 */
		public String getMimeType()
		{
			return m_mimetype;
		}

		public String getMimeCategory()
		{
			if(this.m_mimetype == null || this.m_mimetype.equals(""))
			{
				return "";
			}
			int index = this.m_mimetype.indexOf("/");
			if(index < 0)
			{
				return this.m_mimetype;
			}
			return this.m_mimetype.substring(0, index);
		}

		public String getMimeSubtype()
		{
			if(this.m_mimetype == null || this.m_mimetype.equals(""))
			{
				return "";
			}
			int index = this.m_mimetype.indexOf("/");
			if(index < 0 || index + 1 == this.m_mimetype.length())
			{
				return "";
			}
			return this.m_mimetype.substring(index + 1);
		}

		/**
		 * @param formtype
		 */
		public void setFormtype(String formtype)
		{
			m_formtype = formtype;
		}

		/**
		 * @return
		 */
		public String getFormtype()
		{
			return m_formtype;
		}

		/**
		 * @return Returns the copyrightInfo.
		 */
		public String getCopyrightInfo() {
			return m_copyrightInfo;
		}
		/**
		 * @param copyrightInfo The copyrightInfo to set.
		 */
		public void setCopyrightInfo(String copyrightInfo) {
			m_copyrightInfo = copyrightInfo;
		}
		/**
		 * @return Returns the copyrightStatus.
		 */
		public String getCopyrightStatus() {
			return m_copyrightStatus;
		}
		/**
		 * @param copyrightStatus The copyrightStatus to set.
		 */
		public void setCopyrightStatus(String copyrightStatus) {
			m_copyrightStatus = copyrightStatus;
		}
		/**
		 * @return Returns the description.
		 */
		public String getDescription() {
			return m_description;
		}
		/**
		 * @param description The description to set.
		 */
		public void setDescription(String description) {
			m_description = description;
		}
		/**
		 * @return Returns the filename.
		 */
		public String getFilename() {
			return m_filename;
		}
		/**
		 * @param filename The filename to set.
		 */
		public void setFilename(String filename) {
			m_filename = filename;
		}
		/**
		 * @return Returns the metadata.
		 */
		public Map getMetadata() {
			return m_metadata;
		}
		/**
		 * @param metadata The metadata to set.
		 */
		public void setMetadata(Map metadata) {
			m_metadata = metadata;
		}
		/**
		 * @param name
		 * @param value
		 */
		public void setMetadataItem(String name, Object value)
		{
			m_metadata.put(name, value);
		}
		/**
		 * @return Returns the pubview.
		 */
		public boolean isPubview() {
			return m_pubview;
		}
		/**
		 * @param pubview The pubview to set.
		 */
		public void setPubview(boolean pubview) {
			m_pubview = pubview;
		}
		/**
		 * @return Returns the pubviewset.
		 */
		public boolean isPubviewset() {
			return m_pubviewset;
		}
		/**
		 * @param pubviewset The pubviewset to set.
		 */
		public void setPubviewset(boolean pubviewset) {
			m_pubviewset = pubviewset;
		}
		/**
		 * @return Returns the content.
		 */
		public byte[] getContent() {
			return m_content;
		}
		/**
		 * @return Returns the content as a String.
		 */
		public String getContentstring()
		{
			String rv = "";
			if(m_content != null && m_content.length > 0)
			{
				try
				{
					rv = new String( m_content, m_encoding );
				}
				catch(UnsupportedEncodingException e)
				{
					rv = new String( m_content );
				}
			}
			return rv;
		}
		/**
		 * @param content The content to set.
		 */
		public void setContent(byte[] content) {
			m_content = content;
		}
		/**
		 * @param content The content to set.
		 */
		public void setContent(String content) {
			try
			{
				m_content = content.getBytes(m_encoding);
			}
			catch(UnsupportedEncodingException e)
			{
				m_content = content.getBytes();
			}
		}
		/**
		 * @return Returns the canSetQuota.
		 */
		public boolean canSetQuota() {
			return m_canSetQuota;
		}
		/**
		 * @param canSetQuota The canSetQuota to set.
		 */
		public void setCanSetQuota(boolean canSetQuota) {
			m_canSetQuota = canSetQuota;
		}
		/**
		 * @return Returns the hasQuota.
		 */
		public boolean hasQuota() {
			return m_hasQuota;
		}
		/**
		 * @param hasQuota The hasQuota to set.
		 */
		public void setHasQuota(boolean hasQuota) {
			m_hasQuota = hasQuota;
		}
		/**
		 * @return Returns the quota.
		 */
		public String getQuota() {
			return m_quota;
		}
		/**
		 * @param quota The quota to set.
		 */
		public void setQuota(String quota) {
			m_quota = quota;
		}
		/**
		 * @return true if content-type of item indicates it represents a URL, false otherwise
		 */
		public boolean isUrl()
		{
			return TYPE_URL.equals(m_type) || ResourceProperties.TYPE_URL.equals(m_mimetype);
		}
		/**
		 * @return true if content-type of item indicates it represents a URL, false otherwise
		 */
		public boolean isStructuredArtifact()
		{
			return TYPE_FORM.equals(m_type);
		}
		/**
		 * @return true if content-type of item is "text/text" (plain text), false otherwise
		 */
		public boolean isPlaintext()
		{
			return MIME_TYPE_DOCUMENT_PLAINTEXT.equals(m_mimetype) || MIME_TYPE_DOCUMENT_PLAINTEXT.equals(m_type);
		}
		/**
		 * @return true if content-type of item is "text/html" (an html document), false otherwise
		 */
		public boolean isHtml()
		{
			return MIME_TYPE_DOCUMENT_HTML.equals(m_mimetype) || MIME_TYPE_DOCUMENT_HTML.equals(m_type);
		}

		public boolean contentHasChanged()
		{
			return m_contentHasChanged;
		}

		public void setContentHasChanged(boolean changed)
		{
			m_contentHasChanged = changed;
		}

		public boolean contentTypeHasChanged()
		{
			return m_contentTypeHasChanged;
		}

		public void setContentTypeHasChanged(boolean changed)
		{
			m_contentTypeHasChanged = changed;
		}

		public void setNotification(int notification)
		{
			m_notification = notification;
		}

		public int getNotification()
		{
			return m_notification;
		}

		/**
		 * @return Returns the artifact.
		 */
		public Map getStructuredArtifact()
		{
			return m_structuredArtifact;
		}
		/**
		 * @param artifact The artifact to set.
		 */
		public void setStructuredArtifact(Map artifact)
		{
			this.m_structuredArtifact = artifact;
		}
		/**
		 * @param name
		 * @param value
		 */
		public void setValue(String name, Object value)
		{
			setValue(name, 0, value);
		}
		/**
		 * @param name
		 * @param index
		 * @param value
		 */
		public void setValue(String name, int index, Object value)
		{
			List list = getList(name);
			try
			{
				list.set(index, value);
			}
			catch(ArrayIndexOutOfBoundsException e)
			{
				list.add(value);
			}
			m_structuredArtifact.put(name, list);
		}
		/**
		 * Access a value of a structured artifact field of type String.
		 * @param name	The name of the field to access.
		 * @return the value, or null if the named field is null or not a String.
		 */
		public String getString(String name)
		{
			if(m_structuredArtifact == null)
			{
				m_structuredArtifact = new Hashtable();
			}
			Object value = m_structuredArtifact.get(name);
			String rv = "";
			if(value == null)
			{
				// do nothing
			}
			else if(value instanceof String)
			{
				rv = (String) value;
			}
			else
			{
				rv = value.toString();
			}
			return rv;
		}

		public Object getValue(String name, int index)
		{
			List list = getList(name);
			Object rv = null;
			try
			{
				rv = list.get(index);
			}
			catch(ArrayIndexOutOfBoundsException e)
			{
				// return null
			}
			return rv;

		}

		public Object getPropertyValue(String name)
		{
			return getPropertyValue(name, 0);
		}

		/**
		 * Access a particular value in a Structured Artifact, as identified by the parameter "name".  This
		 * implementation of the method assumes that the name is a series of String identifiers delimited
		 * by the ResourcesAction.ResourcesMetadata.DOT String.
		 * @param name The delimited identifier for the item.
		 * @return The value identified by the name, or null if the name does not identify a valid item.
		 */
		public Object getPropertyValue(String name, int index)
		{
			String[] names = name.split(ResourcesMetadata.DOT);
			Object rv = null;
			if(m_properties == null)
			{
				m_properties = new Vector();
			}
			Iterator it = m_properties.iterator();
			while(rv == null && it.hasNext())
			{
				ResourcesMetadata prop = (ResourcesMetadata) it.next();
				if(name.equals(prop.getDottedname()))
				{
					rv = prop.getValue(index);
				}
			}
			return rv;

		}

		public void setPropertyValue(String name, Object value)
		{
			setPropertyValue(name, 0, value);
		}

		/**
		 * Access a particular value in a Structured Artifact, as identified by the parameter "name".  This
		 * implementation of the method assumes that the name is a series of String identifiers delimited
		 * by the ResourcesAction.ResourcesMetadata.DOT String.
		 * @param name The delimited identifier for the item.
		 * @return The value identified by the name, or null if the name does not identify a valid item.
		 */
		public void setPropertyValue(String name, int index, Object value)
		{
			if(m_properties == null)
			{
				m_properties = new Vector();
			}
			boolean found = false;
			Iterator it = m_properties.iterator();
			while(!found && it.hasNext())
			{
				ResourcesMetadata prop = (ResourcesMetadata) it.next();
				if(name.equals(prop.getDottedname()))
				{
					found = true;
					prop.setValue(index, value);
				}
			}

		}

		/**
		 * Access a particular value in a Structured Artifact, as identified by the parameter "name".  This
		 * implementation of the method assumes that the name is a series of String identifiers delimited
		 * by the ResourcesAction.ResourcesMetadata.DOT String.
		 * @param name The delimited identifier for the item.
		 * @return The value identified by the name, or null if the name does not identify a valid item.
		 */
		public Object getValue(String name)
		{
			String[] names = name.split(ResourcesMetadata.DOT);
			Object rv = m_structuredArtifact;
			if(rv != null && (rv instanceof Map) && ((Map) rv).isEmpty())
			{
				rv = null;
			}
			for(int i = 1; rv != null && i < names.length; i++)
			{
				if(rv instanceof Map)
				{
					rv = ((Map) rv).get(names[i]);
				}
				else
				{
					rv = null;
				}
			}
			return rv;

		}

		/**
		 * Access a list of values associated with a named property of a structured artifact.
		 * @param name The name of the property.
		 * @return The list of values associated with that name, or an empty list if the property is not defined.
		 */
		public List getList(String name)
		{
			if(m_structuredArtifact == null)
			{
				m_structuredArtifact = new Hashtable();
			}
			Object value = m_structuredArtifact.get(name);
			List rv = new Vector();
			if(value == null)
			{
				m_structuredArtifact.put(name, rv);
			}
			else if(value instanceof Collection)
			{
				rv.addAll((Collection)value);
			}
			else
			{
				rv.add(value);
			}
			return rv;

		}

		/**
		 * @return
		 */
		/*
		public Element exportStructuredArtifact(List properties)
		{
			return null;
		}
		*/

		/**
		 * @return Returns the name of the root of a structured artifact definition.
		 */
		public String getRootname()
		{
			return m_rootname;
		}
		/**
		 * @param rootname The name to be assigned for the root of a structured artifact.
		 */
		public void setRootname(String rootname)
		{
			m_rootname = rootname;
		}

		/**
		 * Add a property name to the list of properties missing from the input.
		 * @param propname The name of the property.
		 */
		public void setMissing(String propname)
		{
			m_missingInformation.add(propname);
		}

		/**
		 * Query whether a particular property is missing
		 * @param propname The name of the property
		 * @return The value "true" if the property is missing, "false" otherwise.
		 */
		public boolean isMissing(String propname)
		{
			return m_missingInformation.contains(propname) || m_missingInformation.contains(Validator.escapeUrl(propname));
		}

		/**
		 * Empty the list of missing properties.
		 */
		public void clearMissing()
		{
			m_missingInformation.clear();
		}

		public void setAdded(boolean added)
		{
			m_hasBeenAdded = added;
		}

		public boolean hasBeenAdded()
		{
			return m_hasBeenAdded;
		}


	}	// inner class EditItem


	/**
	 * Inner class encapsulates information about folders (and final item?) in a collection path (a.k.a. breadcrumb)
	 */
	public static class PathItem
	{
		protected String m_url;
		protected String m_name;
		protected String m_id;
		protected boolean m_canRead;
		protected boolean m_isFolder;
		protected boolean m_isLast;
		protected String m_root;
		protected boolean m_isLocal;

		public PathItem(String id, String name)
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

		/**
		 * @return
		 */
		public String getName()
		{
			return m_name;
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
		 * @return
		 */
		public String getUrl()
		{
			return m_url;
		}

		/**
		 * @param url
		 */
		public void setUrl(String url)
		{
			m_url = url;
		}

		/**
		 * @param root
		 */
		public void setRoot(String root)
		{
			m_root = root;
		}

		/**
		 * @return
		 */
		public String getRoot()
		{
			return m_root;
		}

		public void setIsLocal(boolean isLocal)
		{
			m_isLocal = isLocal;
		}

		public boolean isLocal()
		{
			return m_isLocal;
		}

	}	// inner class PathItem

	/**
	 *
	 * inner class encapsulates information about groups of metadata tags (such as DC, LOM, etc.)
	 *
	 */
	public static class MetadataGroup
		extends Vector
	{
		/**
		 *
		 */
		private static final long serialVersionUID = -821054142728929236L;
		protected String m_name;
		protected boolean m_isShowing;

		/**
		 * @param name
		 */
		public MetadataGroup(String name)
		{
			super();
			m_name = name;
			m_isShowing = false;
		}

		/**
		 * @return
		 */
		public boolean isShowing()
		{
			return m_isShowing;
		}

		/**
		 * @param isShowing
		 */
		public void setShowing(boolean isShowing)
		{
			m_isShowing = isShowing;
		}


		/**
		 * @return
		 */
		public String getName()
		{
			return m_name;
		}

		/**
		 * @param name
		 */
		public void setName(String name)
		{
			m_name = name;
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

	}

	public static class AttachItem
	{
		protected String m_id;
		protected String m_displayName;
		protected String m_accessUrl;
		protected String m_collectionId;
		protected String m_contentType;

		/**
		 * @param id
		 * @param displayName
		 * @param collectionId
		 * @param accessUrl
		 */
		public AttachItem(String id, String displayName, String collectionId, String accessUrl)
		{
			m_id = id;
			m_displayName = displayName;
			m_collectionId = collectionId;
			m_accessUrl = accessUrl;

		}

		/**
		 * @return Returns the accessUrl.
		 */
		public String getAccessUrl()
		{
			return m_accessUrl;
		}
		/**
		 * @param accessUrl The accessUrl to set.
		 */
		public void setAccessUrl(String accessUrl)
		{
			m_accessUrl = accessUrl;
		}
		/**
		 * @return Returns the collectionId.
		 */
		public String getCollectionId()
		{
			return m_collectionId;
		}
		/**
		 * @param collectionId The collectionId to set.
		 */
		public void setCollectionId(String collectionId)
		{
			m_collectionId = collectionId;
		}
		/**
		 * @return Returns the id.
		 */
		public String getId()
		{
			return m_id;
		}
		/**
		 * @param id The id to set.
		 */
		public void setId(String id)
		{
			m_id = id;
		}
		/**
		 * @return Returns the name.
		 */
		public String getDisplayName()
		{
			String displayName = m_displayName;
			if(displayName == null || displayName.trim().equals(""))
			{
				displayName = isolateName(m_id);
			}
			return displayName;
		}
		/**
		 * @param name The name to set.
		 */
		public void setDisplayName(String name)
		{
			m_displayName = name;
		}

		/**
		 * @return Returns the contentType.
		 */
		public String getContentType()
		{
			return m_contentType;
		}

		/**
		 * @param contentType
		 */
		public void setContentType(String contentType)
		{
			this.m_contentType = contentType;

		}

	}	// Inner class AttachItem

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

		public void setElement(Element element)
		{
			this.element = element;
		}

		public String getParent()
		{
			return parent;
		}

		public void setParent(String parent)
		{
			this.parent = parent;
		}

	}

	public static class SaveArtifactAttempt
	{
		protected EditItem item;
		protected List errors;
		protected SchemaNode schema;

		public SaveArtifactAttempt(EditItem item, SchemaNode schema)
		{
			this.item = item;
			this.schema = schema;
		}

		/**
		 * @return Returns the errors.
		 */
		public List getErrors()
		{
			return errors;
		}

		/**
		 * @param errors The errors to set.
		 */
		public void setErrors(List errors)
		{
			this.errors = errors;
		}

		/**
		 * @return Returns the item.
		 */
		public EditItem getItem()
		{
			return item;
		}

		/**
		 * @param item The item to set.
		 */
		public void setItem(EditItem item)
		{
			this.item = item;
		}

		/**
		 * @return Returns the schema.
		 */
		public SchemaNode getSchema()
		{
			return schema;
		}

		/**
		 * @param schema The schema to set.
		 */
		public void setSchema(SchemaNode schema)
		{
			this.schema = schema;
		}

	}

	/**
	* Develop a list of all the site collections that there are to page.
	* Sort them as appropriate, and apply search criteria.
	*/
	protected static List readAllResources(SessionState state)
	{
		List other_sites = new Vector();

		String collectionId = (String) state.getAttribute (STATE_ATTACH_COLLECTION_ID);
		if(collectionId == null)
		{
			collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		}
		HashMap expandedCollections = (HashMap) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		
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
		if(! collectionId.equals(wsCollectionId))
		{
            	List members = getBrowseItems(wsCollectionId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (BrowseItem) null, false, state);
            	if(members != null && members.size() > 0)
		    {
		        BrowseItem root = (BrowseItem) members.remove(0);
				showRemoveAction = showRemoveAction || root.hasDeletableChildren();
				showMoveAction = showMoveAction || root.hasDeletableChildren();
				showCopyAction = showCopyAction || root.hasCopyableChildren();
				
		        root.addMembers(members);
		        root.setName(userName + " " + rb.getString("gen.wsreso"));
		        other_sites.add(root);
		    }
		}
		
        	// add all other sites user has access to
		/*
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
			String item = (String) sortIt.next();
			String displayName = item.substring(0, item.lastIndexOf(DELIM));
			String collId = item.substring(item.lastIndexOf(DELIM) + 1);
			if(! collectionId.equals(collId) && ! wsCollectionId.equals(collId))
			{
				List members = getBrowseItems(collId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (BrowseItem) null, false, state);
				if(members != null && members.size() > 0)
				{
					BrowseItem root = (BrowseItem) members.remove(0);
					root.addMembers(members);
					root.setName(displayName);
					other_sites.add(root);
				}
              }
          }
		
		return other_sites;
	}
	
	/**
	* Prepare the current page of site collections to display.
	* @return List of BrowseItem objects to display on this page.
	*/
	protected static List prepPage(SessionState state)
	{
		List rv = new Vector();

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
		List allMessages = readAllResources(state);

		if (allMessages == null)
		{
			return rv;
		}
		
		String messageIdAtTheTopOfThePage = null;
		Object topMsgId = state.getAttribute(STATE_TOP_PAGE_MESSAGE);
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
		BrowseItem itemAtTheTopOfThePage = (BrowseItem) allMessages.get(posStart);
		state.setAttribute(STATE_TOP_PAGE_MESSAGE, itemAtTheTopOfThePage.getId());
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
			state.setAttribute(STATE_VIEW_ID, ((BrowseItem) allMessages.get(viewPos)).getId());
			
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
	* Find the resource with this id in the list.
	* @param messages The list of messages.
	* @param id The message id.
	* @return The index position in the list of the message with this id, or -1 if not found.
	*/
	protected static int findResourceInList(List resources, String id)
	{
		for (int i = 0; i < resources.size(); i++)
		{
			// if this is the one, return this index
			if (((BrowseItem) (resources.get(i))).getId().equals(id)) return i;
		}

		// not found
		return -1;

	}	// findResourceInList

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


}	// ResourcesAction
