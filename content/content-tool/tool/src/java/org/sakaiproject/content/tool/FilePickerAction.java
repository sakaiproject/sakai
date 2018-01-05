/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceHelperAction;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentResourceFilter;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.tool.ResourcesAction.ChefPathItem;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;

/**
 * The FilePickerAction drives the FilePicker helper.<br />
 * This works with the ResourcesTool to show a file picker / attachment editor that can be used by any Sakai tools as a helper.<br />
 * If the user ends without a cancel, the original collection of attachments is replaced with the edited list - otherwise it is left unchanged.
 */
@Slf4j
public class FilePickerAction extends PagedResourceHelperAction
{
	/** Version */
	private static final long serialVersionUID = 1L;

	/** Resource bundle using current language locale */
	private static ResourceLoader hrb = new ResourceLoader("helper");

	/** Resource bundle using current language locale */
	private static ResourceLoader trb = new ResourceLoader("types");

	/** Resource bundle using current language locale */
	private static ResourceLoader crb = new ResourceLoader("content");
	

	/** kernel api **/
	private static SecurityService securityService  = ComponentManager.get(SecurityService.class);
	private static SiteService siteService = ComponentManager.get(SiteService.class);
	private static EntityManager entityManager = ComponentManager.get(EntityManager.class);
	private static SessionManager sessionManager = ComponentManager.get(SessionManager.class);
	private static ToolManager toolManager = ComponentManager.get(ToolManager.class);
	private static UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);
	private static TimeService timeService = ComponentManager.get(TimeService.class);


	/** State attribute for where there is at least one attachment before invoking attachment tool */
	public static final String STATE_HAS_ATTACHMENT_BEFORE = "attachment.has_attachment_before";

	/** Shared messages */
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.sharedI18n.SharedProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.sharedI18n.bundle.shared";
	private static final String RESOURCECLASS = "resource.class.shared";
	private static final String RESOURCEBUNDLE = "resource.bundle.shared";
	private String resourceClass = ServerConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
	private String resourceBundle = ServerConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
	private ResourceLoader srb = new Resource().getLoader(resourceClass, resourceBundle);

	protected static final String PREFIX = "filepicker.";

	protected static final String MODE_ADD_METADATA = "mode_add_metadata";
	protected static final String MODE_ATTACHMENT_CREATE = "mode_attachment_create";
	protected static final String MODE_ATTACHMENT_CREATE_INIT = "mode_attachment_create_init";
	protected static final String MODE_ATTACHMENT_DONE = "mode_attachment_done";
	protected static final String MODE_ATTACHMENT_EDIT_ITEM = "mode_attachment_edit_item";
	protected static final String MODE_ATTACHMENT_EDIT_ITEM_INIT = "mode_attachment_edit_item_init";
	protected static final String MODE_ATTACHMENT_NEW_ITEM = "mode_attachment_new_item";
	protected static final String MODE_ATTACHMENT_NEW_ITEM_INIT = "mode_attachment_new_item_init";
	protected static final String MODE_ATTACHMENT_SELECT = "mode_attachment_select";
	protected static final String MODE_ATTACHMENT_SELECT_INIT = "mode_attachment_select_init";
	protected static final String MODE_HELPER = "mode_helper";

	/** The null/empty string */
	private static final String NULL_STRING = "";

	protected static final String STATE_ADDED_ITEMS = PREFIX + "added_items";

	/** The name of the state attribute containing the name of the tool that invoked Resources as attachment helper */
	public static final String STATE_ATTACH_TOOL_NAME = PREFIX + "attach_tool_name";

	/**
	 * The name of the state attribute for the maximum number of items to attach. The attribute value will be an Integer, 
	 * usually FilePickerHelper.CARDINALITY_SINGLE or FilePickerHelper.CARDINALITY_MULTIPLE. 
	 */
	protected static final String STATE_ATTACH_CARDINALITY = PREFIX + "attach_cardinality";
	protected static final String STATE_ATTACH_INSTRUCTION = PREFIX + "attach_instruction";

	protected static final String STATE_ATTACH_LINKS = PREFIX + "attach_links";
	protected static final String STATE_ATTACH_SUBTITLE = PREFIX + "attach_subtitle";
	protected static final String STATE_ATTACH_TITLE = PREFIX + "attach_title";

	protected static final String STATE_ATTACHMENT_FILTER = PREFIX + "attachment_filter";

	
	protected static final String STATE_ATTACHMENT_ORIGINAL_LIST = PREFIX + "attachment_original_list";
	protected static final String STATE_CONTENT_SERVICE = PREFIX + "content_service";

	/** The content type image lookup service in the State. */
	protected static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = PREFIX + "content_type_image_service";

	protected static final String STATE_DEFAULT_COLLECTION_ID = PREFIX + "default_collection_id";
	protected static final String STATE_DEFAULT_COPYRIGHT = PREFIX + "default_copyright";
	protected static final String STATE_DEFAULT_RETRACT_TIME = PREFIX + "default_retract_time";
	protected static final String STATE_EXPAND_ALL = PREFIX + "expand_all";
	protected static final String STATE_EXPAND_ALL_FLAG = PREFIX + "expand_all_flag";
	protected static final String STATE_EXPANDED_COLLECTIONS = PREFIX + "expanded_collections";
	protected static final String STATE_FILE_UPLOAD_MAX_SIZE = PREFIX + "file_upload_max_size";
	protected static final String STATE_FILEPICKER_MODE = PREFIX + "mode";
	protected static final String STATE_HELPER_CANCELED_BY_USER = PREFIX + "helper_canceled_by_user";
	protected static final String STATE_HELPER_CHANGED = PREFIX + "made_changes";
	protected static final String STATE_HOME_COLLECTION_ID  = PREFIX + "home_collection_id";
	protected static final String STATE_LIST_SELECTIONS = PREFIX + "list_selections";
	protected static final String STATE_LIST_VIEW_SORT = PREFIX + "list_view_sort";
	protected static final String STATE_NAVIGATION_ROOT = PREFIX + "navigation_root";
	protected static final String STATE_NEED_TO_EXPAND_ALL = PREFIX + "need_to_expand_all";
	protected static final String STATE_NEW_ATTACHMENT = PREFIX + "new_attachment";
	protected static final String STATE_PREVENT_PUBLIC_DISPLAY = PREFIX + "prevent_public_display";
	protected static final String STATE_REMOVED_ITEMS = PREFIX + "removed_items";
	protected static final String STATE_RESOURCES_TYPE_REGISTRY = PREFIX + "resource_type_registry";
	protected static final String STATE_SESSION_INITIALIZED = PREFIX + "session_initialized";
	protected static final String STATE_SHOW_ALL_SITES = PREFIX + "show_all_sites";
	protected static final String STATE_SHOW_OTHER_SITES = PREFIX + "show_other_sites";
	public static final String SAK_PROP_SHOW_ALL_SITES = PREFIX + "show_all_collections";

	/** The sort by */
	private static final String STATE_SORT_BY = PREFIX + "sort_by";
	
	protected static final String STATE_TOP_MESSAGE_INDEX = PREFIX + "top_message_index";


	/** The sort ascending or decending */
	private static final String STATE_SORT_ASC = PREFIX + "sort_asc";

	private static final String TEMPLATE_ATTACH = "content/sakai_filepicker_attach";
	private static final String TEMPLATE_SELECT = "content/sakai_filepicker_select";

	private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = ResourcesAction.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS;

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		initState(state, portlet, data);
		
		context.put("DOT", ListItem.DOT);
		context.put("calendarMap", new HashMap());

		// if we are in edit attachments...
		String mode = (String) state.getAttribute(ResourcesAction.STATE_MODE);
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		String helper_mode = (String) toolSession.getAttribute(STATE_FILEPICKER_MODE);

		if (mode == null || helper_mode == null || toolSession.getAttribute(FilePickerHelper.START_HELPER) != null)
		{
			state.removeAttribute(FilePickerHelper.START_HELPER);
			helper_mode = (String) toolSession.getAttribute(STATE_FILEPICKER_MODE);
		}

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe != null)
		{
			if(pipe.isActionCanceled())
			{
				toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);
			}
			else if(pipe.isErrorEncountered())
			{
				String msg = pipe.getErrorMessage();
				if(msg != null && ! msg.trim().equals(""))
				{
					addAlert(state, msg);
				}
				toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);
			}
			else if(pipe.isActionCompleted())
			{
				finishAction(state, toolSession, pipe);
			}
			toolSession.removeAttribute(ResourceToolAction.DONE);
		}
		helper_mode = (String) toolSession.getAttribute(STATE_FILEPICKER_MODE);

		if(MODE_ATTACHMENT_SELECT.equals(helper_mode))
		{
			helper_mode = MODE_ATTACHMENT_SELECT_INIT;
		}
		else if(MODE_ATTACHMENT_CREATE.equals(helper_mode))
		{
			helper_mode = MODE_ATTACHMENT_CREATE_INIT;
		}
		else if(MODE_ATTACHMENT_NEW_ITEM.equals(helper_mode))
		{
			helper_mode = MODE_ATTACHMENT_NEW_ITEM_INIT;
		}
		else if(MODE_ATTACHMENT_EDIT_ITEM.equals(helper_mode))
		{
			helper_mode = MODE_ATTACHMENT_EDIT_ITEM_INIT;
		}

		String helper_title = (String) state.getAttribute(STATE_ATTACH_TITLE);
		if(helper_title != null)
		{
			context.put("helper_title", helper_title);
		}
		
		String helper_instruction = (String) state.getAttribute(STATE_ATTACH_INSTRUCTION);
		if(helper_instruction != null)
		{
			context.put("helper_instruction", helper_instruction);
		}
		
		String template = null;
		if(MODE_ATTACHMENT_SELECT_INIT.equals(helper_mode))
		{
			template = buildSelectAttachmentContext(portlet, context, data, state);
		}
		else if(MODE_ADD_METADATA.equals(helper_mode))
		{
			template = buildAddMetadataContext(portlet, context, data, state);
		}

		return template;

	}

	/**
     * @param portlet
     * @param context
     * @param data
     * @param state
     * @return
     */
    private String buildAddMetadataContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
    {
		context.put("tlang",trb);
		context.put("stlang",srb);

		String template = "content/sakai_resources_cwiz_finish";
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe.isActionCanceled())
		{
			// go back to list view

		}
		else if(pipe.isErrorEncountered())
		{
			// report the error?

		}
		else
		{
			// complete the create wizard
			String defaultCopyrightStatus = (String) toolSession.getAttribute(STATE_DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				toolSession.setAttribute(STATE_DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			Time defaultRetractDate = (Time) toolSession.getAttribute(STATE_DEFAULT_RETRACT_TIME);
			if(defaultRetractDate == null)
			{
				defaultRetractDate = timeService.newTime();
				toolSession.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
			}

			Boolean preventPublicDisplay = (Boolean) toolSession.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				toolSession.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}

			ContentEntity collection = pipe.getContentEntity();

			ListItem parent = new ListItem(collection);
			parent.setPubviewPossible(! preventPublicDisplay);
			ListItem item = new ListItem(pipe, parent, defaultRetractDate);

			String typeId = pipe.getAction().getTypeId();

			context.put("item", item);

			toolSession.setAttribute(STATE_NEW_ATTACHMENT, item);

			ResourceTypeRegistry registry = (ResourceTypeRegistry) toolSession.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
			if(registry == null)
			{
				registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
				toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
			}
			ResourceType typeDef = registry.getType(typeId);
			context.put("type", typeDef);

			context.put("title", (new ResourceTypeLabeler()).getLabel(pipe.getAction()));
			context.put("instruction", trb.getFormattedMessage("instr.create", new String[]{typeDef.getLabel()}));
			context.put("required", trb.getFormattedMessage("instr.require", new String[]{"<span class=\"reqStarInline\">*</span>"}));

			// find the ContentHosting service
			ContentHostingService contentService = (ContentHostingService) toolSession.getAttribute (STATE_CONTENT_SERVICE);
			if(contentService.isAvailabilityEnabled())
			{
				context.put("availability_is_enabled", Boolean.TRUE);
			}

			ResourcesAction.copyrightChoicesIntoContext(state, context);
			ResourcesAction.publicDisplayChoicesIntoContext(state, context);

			context.put("SITE_ACCESS", AccessMode.SITE.toString());
			context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
			context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());
			context.put("PUBLIC_ACCESS", ResourcesAction.PUBLIC_ACCESS);
		}
		
		// Get default notification ("r", "o" or "n")
		context.put("noti", ServerConfigurationService.getString("content.default.notification", "n"));
		
		return template;
    }

	/**
	 * @param state
	 * @param toolSession
	 * @param pipe
	 */
	@SuppressWarnings("unchecked")
	protected void finishAction(SessionState state, ToolSession toolSession, ResourceToolActionPipe pipe)
	{
		ResourceTypeRegistry registry = (ResourceTypeRegistry) toolSession.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		ResourceToolAction action = pipe.getAction();
		// use ActionType for this
		switch(action.getActionType())
		{
		case CREATE:
			toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ADD_METADATA);
			break;
		case NEW_UPLOAD:
			List<ContentResource> resources = ResourcesAction.createResources(pipe);
			if(resources != null && ! resources.isEmpty())
			{
				// expand folder
				Set<String> expandedCollections = getExpandedCollections(toolSession);
				expandedCollections.add(resources.get(0).getContainingCollection().getId());
								
				List<AttachItem> new_items = (List<AttachItem>) toolSession.getAttribute(STATE_ADDED_ITEMS);
				if(new_items == null)
				{
					new_items = new Vector<AttachItem>();
					toolSession.setAttribute(STATE_ADDED_ITEMS, new_items);
				}
				
				ContentResourceFilter filter = (ContentResourceFilter) state.getAttribute(STATE_ATTACHMENT_FILTER);

				for(ContentResource resource : resources)
				{
					if(filter != null)
					{
						if(! filter.allowSelect(resource))
						{
							ResourceProperties props = resource.getProperties();
							String displayName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
							addAlert(state, (String) hrb.getFormattedMessage("filter", new Object[]{displayName}));
							continue;
						}
					}
					AttachItem item = new AttachItem(resource);
					String typeId = resource.getResourceType();
					item.setResourceType(typeId);
					ResourceType typedef = registry.getType(typeId);
					item.setHoverText(typedef.getLocalizedHoverText(resource));
					item.setIconLocation(typedef.getIconLocation(resource));
					item.setIconClass(typedef.getIconClass(resource));
					new_items.add(item);
				}
				toolSession.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
			}
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);
			break;
		case NEW_FOLDER:
			List<ContentCollection> folders = ResourcesAction.createFolders(state, pipe);
			if(folders != null && ! folders.isEmpty())
			{
				// expand folder
				Set<String> expandedCollections = getExpandedCollections(toolSession);
				expandedCollections.add(pipe.getContentEntity().getId());
			}
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			break;
		case NEW_URLS:
			List<ContentResource> urls = ResourcesAction.createUrls(state, pipe);
			if(urls != null && ! urls.isEmpty())
			{
				// expand folder
				Set<String> expandedCollections = getExpandedCollections(toolSession);
				expandedCollections.add(pipe.getContentEntity().getId());
			}
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			break;
		case REVISE_CONTENT:
			ResourcesAction.reviseContent(pipe);
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);
			break;
		default:
			toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);
		}
	}

	/**
     * @param portlet
     * @param context
     * @param data
     * @param state
     * @return
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
	protected String buildSelectAttachmentContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
    {
		context.put("tlang",hrb);
		context.put("stlang",srb);

		ToolSession toolSession = sessionManager.getCurrentToolSession();

		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) toolSession.getAttribute (STATE_CONTENT_SERVICE);

		context.put ("contentTypeImageService", toolSession.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("labeler", new ResourceTypeLabeler());
		context.put("ACTION_DELIMITER", ResourceToolAction.ACTION_DELIMITER);

		List new_items = (List) toolSession.getAttribute(STATE_ADDED_ITEMS);
		if(new_items == null)
		{
			new_items = new Vector();
			toolSession.setAttribute(STATE_ADDED_ITEMS, new_items);
		}
		context.put("attached", new_items);
		context.put("last", Integer.valueOf(new_items.size() - 1));

		Integer max_cardinality = (Integer) toolSession.getAttribute(STATE_ATTACH_CARDINALITY);
		if(max_cardinality == null)
		{
			max_cardinality = FilePickerHelper.CARDINALITY_MULTIPLE;
			toolSession.setAttribute(STATE_ATTACH_CARDINALITY, max_cardinality);
		}
		context.put("max_cardinality", max_cardinality);
		if(new_items.size() < max_cardinality.intValue())
		{
			context.put("can_attach_more", Boolean.TRUE);
		}

		if(new_items.size() >= max_cardinality.intValue())
		{
			context.put("disable_attach_links", Boolean.TRUE.toString());
		}

		if(toolSession.getAttribute(STATE_HELPER_CHANGED) != null)
		{
			context.put("list_has_changed", Boolean.TRUE.toString());
		}

		String homeCollectionId = (String) toolSession.getAttribute(STATE_HOME_COLLECTION_ID);
		if(homeCollectionId == null)
		{
			homeCollectionId = contentService.getSiteCollection(toolManager.getCurrentPlacement().getContext());
			toolSession.setAttribute(STATE_HOME_COLLECTION_ID, homeCollectionId);
		}


		// make sure the collectionId is set
		String collectionId = (String) toolSession.getAttribute(STATE_DEFAULT_COLLECTION_ID);
		if(collectionId == null)
		{
			collectionId = homeCollectionId;
		}

		context.put ("collectionId", collectionId);

		Comparator userSelectedSort = (Comparator) toolSession.getAttribute(STATE_LIST_VIEW_SORT);

		// set the sort values
		String sortedBy = (String) toolSession.getAttribute (STATE_SORT_BY);
		String sortedAsc = (String) toolSession.getAttribute (STATE_SORT_ASC);
		context.put ("currentSortedBy", sortedBy);
		context.put ("currentSortAsc", sortedAsc);
		context.put("TRUE", Boolean.TRUE.toString());

		try
		{
			try
			{
				contentService.checkCollection (collectionId);
				context.put ("collectionFlag", Boolean.TRUE.toString());
			}
			catch(IdUnusedException ex)
			{
				if(log.isDebugEnabled())
				{
					log.debug("ResourcesAction.buildSelectAttachment (static) : IdUnusedException: " + collectionId);
				}
				try
				{
					ContentCollectionEdit coll = contentService.addCollection(collectionId);
					contentService.commitCollection(coll);
				}
				catch(IdUsedException inner)
				{
					// how can this happen??
					log.warn("ResourcesAction.buildSelectAttachment (static) : IdUsedException: " + collectionId);
					throw ex;
				}
				catch(IdInvalidException inner)
				{
					log.warn("ResourcesAction.buildSelectAttachment (static) : IdInvalidException: " + collectionId);
					// what now?
					throw ex;
				}
				catch(InconsistentException inner)
				{
					log.warn("ResourcesAction.buildSelectAttachment (static) : InconsistentException: " + collectionId);
					// what now?
					throw ex;
				}
			}
			catch(TypeException ex)
			{
				log.warn("ResourcesAction.buildSelectAttachment (static) : TypeException.");
				throw ex;
			}
			catch(PermissionException ex)
			{
				// May occurs when user attempts to access siteCollection which is hidden, with contents accessible
				log.info("ResourcesAction.buildSelectAttachment (static) : PermissionException.");
			}

			Set<String> expandedCollections = getExpandedCollections(toolSession);
			expandedCollections.add(collectionId);

			ResourceTypeRegistry registry = (ResourceTypeRegistry) toolSession.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
			if(registry == null)
			{
				registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
				toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
			}

			boolean expandAll = Boolean.TRUE.toString().equals(toolSession.getAttribute(STATE_NEED_TO_EXPAND_ALL));

			ContentResourceFilter filter = (ContentResourceFilter) state.getAttribute(STATE_ATTACHMENT_FILTER);
			
			List<ListItem> this_site = new Vector<>();

			if(contentService.isInDropbox(collectionId))
			{
				User[] submitters = (User[]) state.getAttribute(FilePickerHelper.FILE_PICKER_SHOW_DROPBOXES);
				if(submitters != null)
				{
					String dropboxId = contentService.getDropboxCollection();
					if(dropboxId == null)
					{
						contentService.createDropboxCollection();
						dropboxId = contentService.getDropboxCollection();
					}

					if(dropboxId == null)
					{
						// do nothing
					}
					else if(contentService.isDropboxMaintainer())
					{
						for(int i = 0; i < submitters.length; i++)
						{
							User submitter = submitters[i];
							String dbId = dropboxId + StringUtil.trimToZero(submitter.getId()) + "/";
							try
							{
								ContentCollection db = contentService.getCollection(dbId);
								expandedCollections.add(dbId);

								ListItem item = ListItem.getListItem(db, (ListItem) null, registry, expandAll, expandedCollections, (List<String>) null, (List<String>) null, 0, userSelectedSort, false, null);
								List<ListItem> items = item.convert2list();
								if(filter != null)
								{
									items = filterList(items, filter);
								}
								this_site.addAll(items);

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
							ContentCollection db = contentService.getCollection(dropboxId);
							expandedCollections.add(dropboxId);

							ListItem item = ListItem.getListItem(db, null, registry, expandAll, expandedCollections, null, null, 0, null, false, null);
							this_site.addAll(item.convert2list());
						}
						catch(IdUnusedException e)
						{
							// if an id is unused, ignore it
						}
					}
				}
			}
			else
			{
				if(contentService.allowGetCollection(collectionId)) {
					ContentCollection collection = contentService.getCollection(collectionId);
					ListItem item = ListItem.getListItem(collection, null, registry, expandAll, expandedCollections, null, null, 0, null, false, filter);
					List<ListItem> items = item.convert2list();
					if (filter != null) {
						items = filterList(items, filter);
					}
					//Check if the ListItem in 'items' matches with the attach_item in the new_items list , if yes then it should not have option to be selected.
					for (Object new_item : new_items) {
						for (ListItem listItem : items) {
							if (listItem.getId().equals(((AttachItem) new_item).getId())) {
								listItem.setCanSelect(false);
								break;
							}

						}
					}
					this_site.addAll(items);
				}
			}

			if(!this_site.isEmpty()) {
				context.put("this_site", this_site);
			}

			boolean show_all_sites = false;

			String allowed_to_see_other_sites = (String) toolSession.getAttribute(STATE_SHOW_ALL_SITES);
			String show_other_sites = (String) toolSession.getAttribute(STATE_SHOW_OTHER_SITES);
			context.put("show_other_sites", show_other_sites);
			if(Boolean.TRUE.toString().equals(allowed_to_see_other_sites))
			{
				context.put("allowed_to_see_other_sites", Boolean.TRUE.toString());
				show_all_sites = Boolean.TRUE.toString().equals(show_other_sites);
			}

			if(show_all_sites)
			{
				// TODO move this to a separate class used by ResourcesAction and FilePickerAction to support paging
				
				List<ListItem> siteCollections = prepPage(state);
				List<ListItem> otherSites = new Vector<ListItem>();
				for(ListItem siteCollection : siteCollections)
				{
					otherSites.addAll(siteCollection.convert2list());
				}
				context.put("other_sites", otherSites);

				if (toolSession.getAttribute(STATE_NUM_MESSAGES) != null)
				{
					context.put("allMsgNumber", toolSession.getAttribute(STATE_NUM_MESSAGES).toString());
					context.put("allMsgNumberInt", toolSession.getAttribute(STATE_NUM_MESSAGES));
				}

				context.put("pagesize", ((Integer) toolSession.getAttribute(STATE_PAGESIZE)).toString());

				// find the position of the message that is the top first on the page
				if ((toolSession.getAttribute(STATE_TOP_MESSAGE_INDEX) != null) && (toolSession.getAttribute(STATE_PAGESIZE) != null))
				{
					int topMsgPos = ((Integer)toolSession.getAttribute(STATE_TOP_MESSAGE_INDEX)).intValue() + 1;
					context.put("topMsgPos", Integer.toString(topMsgPos));
					int btmMsgPos = topMsgPos + ((Integer)toolSession.getAttribute(STATE_PAGESIZE)).intValue() - 1;
					if (toolSession.getAttribute(STATE_NUM_MESSAGES) != null)
					{
						int allMsgNumber = ((Integer)toolSession.getAttribute(STATE_NUM_MESSAGES)).intValue();
						if (btmMsgPos > allMsgNumber)
							btmMsgPos = allMsgNumber;
					}
					context.put("btmMsgPos", Integer.toString(btmMsgPos));
				}

				boolean goPPButton = toolSession.getAttribute(STATE_PREV_PAGE_EXISTS) != null;
				context.put("goPPButton", Boolean.toString(goPPButton));
				boolean goNPButton = toolSession.getAttribute(STATE_NEXT_PAGE_EXISTS) != null;
				context.put("goNPButton", Boolean.toString(goNPButton));

				context.put("pagesize", toolSession.getAttribute(STATE_PAGESIZE));

			}
			
			context.put("counter", new EntityCounter());

			if(show_all_sites)
			{
				List messages = prepPage(state);
				context.put("other_sites", messages);

				if (toolSession.getAttribute(STATE_NUM_MESSAGES) != null)
				{
					context.put("allMsgNumber", toolSession.getAttribute(STATE_NUM_MESSAGES).toString());
					context.put("allMsgNumberInt", toolSession.getAttribute(STATE_NUM_MESSAGES));
				}

				context.put("pagesize", ((Integer) toolSession.getAttribute(STATE_PAGESIZE)).toString());

				// find the position of the message that is the top first on the page
				if ((toolSession.getAttribute(STATE_TOP_MESSAGE_INDEX) != null) && (toolSession.getAttribute(STATE_PAGESIZE) != null))
				{
					int topMsgPos = ((Integer)toolSession.getAttribute(STATE_TOP_MESSAGE_INDEX)).intValue() + 1;
					context.put("topMsgPos", Integer.toString(topMsgPos));
					int btmMsgPos = topMsgPos + ((Integer)toolSession.getAttribute(STATE_PAGESIZE)).intValue() - 1;
					if (toolSession.getAttribute(STATE_NUM_MESSAGES) != null)
					{
						int allMsgNumber = ((Integer)toolSession.getAttribute(STATE_NUM_MESSAGES)).intValue();
						if (btmMsgPos > allMsgNumber)
							btmMsgPos = allMsgNumber;
					}
					context.put("btmMsgPos", Integer.toString(btmMsgPos));
				}

				boolean goPPButton = toolSession.getAttribute(STATE_PREV_PAGE_EXISTS) != null;
				context.put("goPPButton", Boolean.toString(goPPButton));
				boolean goNPButton = toolSession.getAttribute(STATE_NEXT_PAGE_EXISTS) != null;
				context.put("goNPButton", Boolean.toString(goNPButton));

				context.put("pagesize", toolSession.getAttribute(STATE_PAGESIZE));
			}
		}
		catch (IdUnusedException e)
		{
			addAlert(state, crb.getString("cannotfind"));
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		catch(TypeException e)
		{
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		catch(PermissionException e)
		{
			addAlert(state, crb.getString("notpermis1"));
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}

		context.put("homeCollection", (String) toolSession.getAttribute (STATE_HOME_COLLECTION_ID));
		context.put ("resourceProperties", contentService.newResourceProperties ());

		try
		{
			// TODO: why 'site' here?
			Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
			context.put("siteTitle", site.getTitle());
		}
		catch (IdUnusedException e)
		{
		}

		context.put("expandallflag", toolSession.getAttribute(STATE_EXPAND_ALL_FLAG));
		state.removeAttribute(STATE_NEED_TO_EXPAND_ALL);
        List cPath = getCollectionPath(state);
        context.put ("collectionPath", cPath);
		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		// justDelivered(state);

		// pick the template based on whether client wants links or copies
		String template = TEMPLATE_SELECT;
		
		if(state.getAttribute(STATE_ATTACH_LINKS) == null)
		{
			// user wants copies in hidden attachments area
			template = TEMPLATE_ATTACH;
		}

		return template;
	    //return TEMPLATE_SELECT;
    }
    
    /**
     * remove all security advisors
     */
    protected void disableSecurityAdvisors()
    {
    	// remove all security advisors
    	securityService.popAdvisor();
    }

    /**
     * Establish a security advisor to allow the "embedded" azg work to occur
     * with no need for additional security permissions.
     */
    protected void enableSecurityAdvisor()
    {
      // put in a security advisor so we can create citationAdmin site without need
      // of further permissions
      securityService.pushAdvisor(new SecurityAdvisor() {
        public SecurityAdvice isAllowed(String userId, String function, String reference)
        {
          return SecurityAdvice.ALLOWED;
        }
      });
    }

	/**
     * @param filter 
	 * @param items
     * @return
     */
    private List<ListItem> filterList(List<ListItem> items, ContentResourceFilter filter)
    {
    	
    	List<ListItem> rv = new Vector<ListItem>();
	    for(ListItem item : items)
	    {
	    	ContentEntity entity = item.getEntity();
	    	if(entity.isCollection() || filter == null || filter.allowView((ContentResource) entity)) 
	    	{
	    		rv.add(item);
	    		item.setCanSelect(entity.isResource() && (filter == null || filter.allowSelect((ContentResource) entity)));
	    	}
	    }
	    return rv;
    }

	/**
	 * @param state
	 */
	protected void cleanup(SessionState state)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		
		Enumeration<String> attributeNames = toolSession.getAttributeNames();
		while(attributeNames.hasMoreElements())
		{
			String attributeName = attributeNames.nextElement();
			if(attributeName.startsWith(PREFIX))
			{
				state.removeAttribute(attributeName);
			}
		}
		
		if (toolSession != null) 
		{
			state.removeAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS);
			state.removeAttribute(FilePickerHelper.FILE_PICKER_RESOURCE_FILTER);
			state.removeAttribute(FilePickerHelper.DEFAULT_COLLECTION_ID);
			state.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS);
		}
		
 	}	// cleanup

	/**
	 * @param state
	 * @param toolSession
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected String initHelperAction(SessionState state, ToolSession toolSession)
	{
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);

		ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		toolSession.setAttribute (STATE_CONTENT_SERVICE, contentService);
		toolSession.setAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE, ComponentManager.get("org.sakaiproject.content.api.ContentTypeImageService"));
		toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry"));
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
		toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		
		// start with a copy of the original attachment list
		List attachments = (List) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
		if (attachments == null)
		{
			attachments = entityManager.newReferenceList();
		}
		toolSession.setAttribute(STATE_ATTACHMENT_ORIGINAL_LIST, attachments);

		Object attach_links = state.getAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS);
		if(attach_links == null)
		{
			toolSession.removeAttribute(STATE_ATTACH_LINKS);
		}
		else
		{
			toolSession.setAttribute(STATE_ATTACH_LINKS, Boolean.TRUE.toString());
		}
		
		List<AttachItem> new_items = new Vector<AttachItem>();
		Iterator<Reference> attachmentIt = attachments.iterator();
		while(attachmentIt.hasNext())
		{
			Reference ref = attachmentIt.next();
			String typeId = null;
			String contentType = null;
			try
            {
				ContentResource res = (ContentResource) ref.getEntity();
				ResourceProperties props = null;
				String accessUrl = null;
				if(res == null)
				{
				    // NOTE: some of the values bellow may be null if res is null
	                props = contentService.getProperties(ref.getId());
	                accessUrl = contentService.getUrl(ref.getId());
	                
	 			}
				else
				{
					props = res.getProperties();
					accessUrl = res.getUrl();
					contentService.getContainingCollectionId (res.getId());
					typeId = res.getResourceType();
					contentType = res.getContentType();
				}

				String displayName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				AttachItem item = new AttachItem(ref.getId(), displayName, null, accessUrl);
				item.setContentType(contentType);
				item.setResourceType(typeId);
				ResourceType typedef = registry.getType(typeId);
				item.setHoverText(typedef.getLocalizedHoverText(res));
				item.setIconLocation(typedef.getIconLocation(res));
				item.setIconClass(typedef.getIconClass(res));
				
				new_items.add(item);
            }
            catch (PermissionException e)
            {
                log.info("PermissionException -- User has permission to revise item but lacks permission to view attachment: " + ref.getId());
            }
            catch (IdUnusedException e)
            {
                log.info("IdUnusedException -- An attachment has been deleted: " + ref.getId());
            }
		}
		toolSession.setAttribute(STATE_ADDED_ITEMS, new_items);
		
		initMessage(toolSession, state);

		toolSession.setAttribute(STATE_ATTACHMENT_FILTER, toolSession.getAttribute(FilePickerHelper.FILE_PICKER_RESOURCE_FILTER));
		
		String defaultCollectionId = (String) toolSession.getAttribute(FilePickerHelper.DEFAULT_COLLECTION_ID);
		if(defaultCollectionId == null)
		{
			defaultCollectionId = contentService.getSiteCollection(toolManager.getCurrentPlacement().getContext());
		}
		toolSession.setAttribute(STATE_DEFAULT_COLLECTION_ID, defaultCollectionId);
		
		state.setAttribute(STATE_MODE, MODE_HELPER);
		toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT);
		
		boolean show_all_sites = ServerConfigurationService.getBoolean(SAK_PROP_SHOW_ALL_SITES, 
				ServerConfigurationService.getBoolean(ResourcesAction.SAK_PROP_SHOW_ALL_SITES_IN_HELPER, Boolean.TRUE));
		toolSession.setAttribute(STATE_SHOW_ALL_SITES, Boolean.toString(show_all_sites));

		// state attribute ResourcesAction.STATE_ATTACH_TOOL_NAME should be set with a string to indicate name of tool
		String toolName = toolManager.getCurrentPlacement().getTitle();
		toolSession.setAttribute(STATE_ATTACH_TOOL_NAME, toolName);

		Object max_cardinality = toolSession.getAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS);
		if (max_cardinality != null)
		{
			toolSession.setAttribute(STATE_ATTACH_CARDINALITY, max_cardinality);
		}

		if (toolSession.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE) == null)
		{
			toolSession.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, ServerConfigurationService.getString(ResourcesConstants.SAK_PROP_MAX_UPLOAD_FILE_SIZE, 
																									  ResourcesConstants.DEFAULT_MAX_FILE_SIZE_STRING));
		}
		
		return MODE_HELPER;
	}

	/**
	 * @param toolSession
	 * @param state
	 */
	protected void initMessage(ToolSession toolSession, SessionState state)
	{
		String title = (String) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT);
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT);
		if (title == null)
		{
			toolSession.removeAttribute(STATE_ATTACH_TITLE);
		}
		else
		{
			toolSession.setAttribute(STATE_ATTACH_TITLE, title);
		}

		String instruction = (String) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT);
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT);
		if (instruction == null)
		{
			toolSession.removeAttribute(STATE_ATTACH_INSTRUCTION);
		}
		else
		{
			toolSession.setAttribute(STATE_ATTACH_INSTRUCTION, instruction);
		}
		
		String subtitle = (String) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_SUBTITLE_TEXT);
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_SUBTITLE_TEXT);
		if (subtitle == null)
		{
			toolSession.removeAttribute(STATE_ATTACH_SUBTITLE);
		}
		else
		{
			toolSession.setAttribute(STATE_ATTACH_SUBTITLE, subtitle);
		}
		
	}

	/**
	* Populate the state object, if needed - override to do something!
	*/
	protected void initState(SessionState state, VelocityPortlet portlet, RunData data)
	{
		super.initState(state, portlet, (JetspeedRunData) data);
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		if(toolSession.getAttribute(STATE_SESSION_INITIALIZED) == null)
		{
			initHelperAction(state, toolSession);
			toolSession.setAttribute(STATE_SESSION_INITIALIZED, Boolean.TRUE);
		}

	}	// initState
	
	/**
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	public void doAttachitem(RunData data)
	{
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		ParameterParser params = data.getParameters ();

		//toolSession.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		String itemId = null;
		try {
		    itemId = URLDecoder.decode(params.getString("itemId"), "UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
		    // should be impossible. use original, with warning
		    log.warn("UTF-8 unsupported???");
		    itemId = params.getString("itemId");
		}

		Object attach_links = toolSession.getAttribute(STATE_ATTACH_LINKS);

		if(attach_links == null)
		{
			attachCopy(itemId, state);
		}
		else
		{
			attachLink(itemId, state);
		}

		List<AttachItem> removed = (List<AttachItem>) toolSession.getAttribute(STATE_REMOVED_ITEMS);
		if(removed == null)
		{
			removed = new Vector<AttachItem>();
			toolSession.setAttribute(STATE_REMOVED_ITEMS, removed);
		}
		Iterator<AttachItem> removeIt = removed.iterator();
		while(removeIt.hasNext())
		{
			AttachItem item = removeIt.next();
			if(item.getId().equals(itemId))
			{
				removeIt.remove();
				break;
			}
		}

		toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);

	}	// doAttachitem

	/**
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	public void doAttachupload(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		ParameterParser params = data.getParameters ();

		ResourceTypeRegistry registry = (ResourceTypeRegistry) toolSession.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		String max_file_size_mb = (String) toolSession.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
		long max_bytes = 1024L * 1024L;
		try
		{
			max_bytes = Long.parseLong(max_file_size_mb) * 1024L * 1024L;
		}
		catch(Exception e)
		{
			// if unable to parse an integer from the value
			// in the properties file, use 1 MB as a default
			max_file_size_mb = "1";
			max_bytes = 1024L * 1024L;
		}

		FileItem fileitem = null;
		fileitem = params.getFileItem("upload");
		if(fileitem == null)
		{
			// "The user submitted a file to upload but it was too big!"
			addAlert(state, trb.getFormattedMessage("size.exceeded", new Object[]{ max_file_size_mb }));
		}
		else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
		{
			addAlert(state, crb.getString("choosefile7"));
		}
		else if (fileitem.getFileName().length() > 0)
		{
			String filename = Validator.getFileName(fileitem.getFileName());
			InputStream fileContentStream = fileitem.getInputStream();
			
			// Store contentLength as long for future-proofing, though in many cases this
			// may simply be -1 (unknown), so the length check is of limited use
			long contentLength = data.getRequest().getContentLength();
			String contentType = fileitem.getContentType();

			if(contentLength >= max_bytes)
			{
				addAlert(state, trb.getFormattedMessage("size.exceeded", new Object[]{ max_file_size_mb }));
			}
			else if(fileContentStream != null)
			{
				// we just want the file name part - strip off any drive and path stuff
				String name = Validator.getFileName(filename);
				String resourceId = Validator.escapeResourceName(name);

				ContentHostingService contentService = (ContentHostingService) toolSession.getAttribute (STATE_CONTENT_SERVICE);

				// make a set of properties to add for the new resource
				ResourcePropertiesEdit props = contentService.newResourceProperties();
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
				props.addProperty(ResourceProperties.PROP_DESCRIPTION, filename);

				// make an attachment resource for this URL
				try
				{
					String siteId = toolManager.getCurrentPlacement().getContext();

					String toolName = (String) toolSession.getAttribute(STATE_ATTACH_TOOL_NAME);
					if(toolName == null)
					{
						toolName = toolManager.getCurrentPlacement().getTitle();
						toolSession.setAttribute(STATE_ATTACH_TOOL_NAME, toolName);
					}

					enableSecurityAdvisor();
					ContentResource attachment = contentService.addAttachmentResource(resourceId, siteId, toolName, contentType, fileContentStream, props);
					
					ContentResourceFilter filter = (ContentResourceFilter) state.getAttribute(STATE_ATTACHMENT_FILTER);
					if(filter == null || filter.allowSelect(attachment))
					{
						// do nothing
					}
					else
					{
						addAlert(state, (String) hrb.getFormattedMessage("filter", new Object[]{name}));
						return;
					}

					List<AttachItem> new_items = (List<AttachItem>) toolSession.getAttribute(STATE_ADDED_ITEMS);
					if(new_items == null)
					{
						new_items = new Vector<AttachItem>();
						toolSession.setAttribute(STATE_ADDED_ITEMS, new_items);
					}

					String containerId = contentService.getContainingCollectionId (attachment.getId());
					String accessUrl = attachment.getUrl();

					AttachItem item = new AttachItem(attachment.getId(), filename, containerId, accessUrl);
					item.setContentType(contentType);
					String typeId = ResourceType.TYPE_UPLOAD;
					item.setResourceType(typeId);
					ResourceType typedef = registry.getType(typeId);
					item.setHoverText(typedef.getLocalizedHoverText(attachment));
					item.setIconLocation(typedef.getIconLocation(attachment));
					item.setIconClass(typedef.getIconClass(attachment));
					new_items.add(item);
					disableSecurityAdvisors();
					
					toolSession.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
				}
				catch (VirusFoundException vfe)
				{
					addAlert(state, trb.getFormattedMessage("alert.virusfound", new String[] {vfe.getMessage()}));
				}
				catch (PermissionException e)
				{
					addAlert(state, crb.getString("notpermis4"));
				}
				catch(OverQuotaException e)
				{
					addAlert(state, crb.getString("overquota.site"));
				}
				catch(ServerOverloadException e)
				{
					addAlert(state, crb.getString("failed"));
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
					if(ContentHostingService.ID_LENGTH_EXCEPTION.equals(e.getMessage()))
					{
						// couldn't we just truncate the resource-id instead of rejecting the upload?
						addAlert(state, trb.getFormattedMessage("alert.toolong", new String[]{name}));
					}
					else
					{
						log.debug("ResourcesAction.doAttachupload ***** Unknown Exception ***** " + e.getMessage());
						addAlert(state, crb.getString("failed"));
					}
				}
			}
			else
			{
				addAlert(state, crb.getString("choosefile7"));
			}
		}

		toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);

	}	// doAttachupload

	/**
	 * @param data
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void doAttachurl(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		ParameterParser params = data.getParameters ();

		ResourceTypeRegistry registry = (ResourceTypeRegistry) toolSession.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		//ResourceType type = registry.getType(typeId); 
		
		String url = params.getCleanString("url");

		ContentHostingService contentService = (ContentHostingService) toolSession.getAttribute (STATE_CONTENT_SERVICE);

		ResourcePropertiesEdit resourceProperties = contentService.newResourceProperties ();
		resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, url);
		resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, url);

		resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());

		try
		{
			url = validateURL(url);

			byte[] newUrl = url.getBytes();
			String newResourceId = Validator.escapeResourceName(url);

			String siteId = toolManager.getCurrentPlacement().getContext();
			String toolName = (String) (String) toolSession.getAttribute(STATE_ATTACH_TOOL_NAME);
			if(toolName == null)
			{
				toolName = toolManager.getCurrentPlacement().getTitle();
				toolSession.setAttribute(STATE_ATTACH_TOOL_NAME, toolName);
			}

			enableSecurityAdvisor();
			ContentResource attachment = contentService.addAttachmentResource(newResourceId, siteId, toolName, ResourceProperties.TYPE_URL, newUrl, resourceProperties);

			List<AttachItem> new_items = (List<AttachItem>) toolSession.getAttribute(STATE_ADDED_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
				toolSession.setAttribute(STATE_ADDED_ITEMS, new_items);
			}

			String containerId = contentService.getContainingCollectionId (attachment.getId());
			String accessUrl = attachment.getUrl();

			AttachItem item = new AttachItem(attachment.getId(), url, containerId, accessUrl);
			item.setContentType(ResourceProperties.TYPE_URL);
			String typeId = ResourceType.TYPE_URL;
			item.setResourceType(typeId);
			ResourceType typedef = registry.getType(typeId);
			item.setHoverText(typedef.getLocalizedHoverText(attachment));
			item.setIconLocation(typedef.getIconLocation(attachment));
			item.setIconClass(typedef.getIconClass(attachment));
			new_items.add(item);
			disableSecurityAdvisors();
			toolSession.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
		}
		catch(MalformedURLException e)
		{
			// invalid url
			addAlert(state, trb.getFormattedMessage("url.invalid", new String[]{url}) + crb.getString("validurl"));
		}
		catch (PermissionException e)
		{
			addAlert(state, crb.getString("notpermis4"));
		}
		catch(OverQuotaException e)
		{
			addAlert(state, crb.getString("overquota.site"));
		}
		catch(ServerOverloadException e)
		{
			addAlert(state, crb.getString("failed"));
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
			if(ContentHostingService.ID_LENGTH_EXCEPTION.equals(e.getMessage()))
			{
				// couldn't we just truncate the resource-id instead of rejecting the upload?
				addAlert(state, trb.getFormattedMessage("alert.toolong", new String[]{url}));
			}
			else
			{
				log.debug("ResourcesAction.doAttachupload ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, crb.getString("failed"));
			}
		}

		toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);
		
	}	// doAttachurl

	/**
	* doCancel to return to the previous state
	*/
	public void doCancel ( RunData data)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		
		toolSession.setAttribute(STATE_HELPER_CANCELED_BY_USER, Boolean.TRUE.toString());

		toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_DONE);

	}	// doCancel



	@SuppressWarnings("unchecked")
	public void doRemoveitem(RunData data)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		ParameterParser params = data.getParameters ();

		String itemId = params.getString("itemId");

		List<AttachItem> new_items = (List<AttachItem>) toolSession.getAttribute(STATE_ADDED_ITEMS);

		AttachItem item = null;
		boolean found = false;

		Iterator<AttachItem> it = new_items.iterator();
		while(!found && it.hasNext())
		{
			item = it.next();
			if(item.getId().equals(itemId))
			{
				found = true;
			}
		}

		if(found && item != null)
		{
			new_items.remove(item);
			List<AttachItem> removed = (List<AttachItem>) toolSession.getAttribute(STATE_REMOVED_ITEMS);
			if(removed == null)
			{
				removed = new Vector<AttachItem>();
				toolSession.setAttribute(STATE_REMOVED_ITEMS, removed);
			}
			removed.add(item);

			toolSession.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
		}

	}	// doRemoveitem

	@SuppressWarnings("unchecked")
	public void doAddattachments(RunData data)
	{
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ToolSession toolSession = sessionManager.getCurrentToolSession();

		ContentHostingService contentService = (ContentHostingService) toolSession.getAttribute (STATE_CONTENT_SERVICE);

		List<AttachItem> new_items = (List<AttachItem>) toolSession.getAttribute(STATE_ADDED_ITEMS);
		if(new_items == null)
		{
			new_items = new Vector<AttachItem>();
			toolSession.setAttribute(STATE_ADDED_ITEMS, new_items);
		}

		List<AttachItem> removed = (List<AttachItem>) toolSession.getAttribute(STATE_REMOVED_ITEMS);
		if(removed == null)
		{
			removed = new Vector<AttachItem>();
			toolSession.setAttribute(STATE_REMOVED_ITEMS, removed);
		}
		
		Iterator<AttachItem> removeIt = removed.iterator();
		while(removeIt.hasNext())
		{
			AttachItem item = removeIt.next();
			try
			{
				if(contentService.isAttachmentResource(item.getId()))
				{
					ContentResourceEdit edit = contentService.editResource(item.getId());
					contentService.removeResource(edit);
					ContentCollectionEdit coll = contentService.editCollection(item.getCollectionId());
					contentService.removeCollection(coll);
				}
			}
			catch(Exception ignore)
			{
				// log failure 
			}
		}
		state.removeAttribute(STATE_REMOVED_ITEMS);

		// add to the attachments vector
		List<Reference> original_attachments = (List<Reference>) toolSession.getAttribute(STATE_ATTACHMENT_ORIGINAL_LIST);
		
		original_attachments.clear();

		Iterator<AttachItem> it = new_items.iterator();
		while(it.hasNext())
		{
			AttachItem item = it.next();

			try
			{
				Reference ref = entityManager.newReference(contentService.getReference(item.getId()));
				original_attachments.add(ref);
			}
			catch(Exception e)
			{
				log.warn("doAddattachments " + e);
			}
		}
		
		state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, original_attachments);

		// if there is at least one attachment
		if (original_attachments.size() > 0)
		{
			//check -- jim
			toolSession.setAttribute(STATE_HAS_ATTACHMENT_BEFORE, Boolean.TRUE);
		}
		
		toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_DONE);

	}

	/**
	 * @param itemId
	 * @param state
	 */
	@SuppressWarnings("unchecked")
	public void attachCopy(String itemId, SessionState state)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		ContentHostingService contentService = (ContentHostingService) toolSession.getAttribute (STATE_CONTENT_SERVICE);
		ResourceTypeRegistry registry = (ResourceTypeRegistry) toolSession.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}

		List<AttachItem> new_items = (List<AttachItem>) toolSession.getAttribute(STATE_ADDED_ITEMS);
		if(new_items == null)
		{
			new_items = new Vector<AttachItem>();
			toolSession.setAttribute(STATE_ADDED_ITEMS, new_items);
		}

		boolean found = false;
		Iterator<AttachItem> it = new_items.iterator();
		while(!found && it.hasNext())
		{
			AttachItem item = it.next();
			if(item.getId().equals(itemId))
			{
				found = true;
			}
		}

		if(!found)
		{
			ContentResourceFilter filter = (ContentResourceFilter) state.getAttribute(STATE_ATTACHMENT_FILTER);
			
			ContentResource resource = null;
			ResourceToolAction copyAction = null;
			ContentResource attachment = null;
			try
			{
				resource = contentService.getResource(itemId);
				Reference reference = entityManager.newReference(resource.getReference());
				
				// we're making a copy, so we need to invoke the copy methods related to the resource-type registration 
				String typeId = resource.getResourceType();
				ResourceType typedef = registry.getType(typeId);
				copyAction = typedef.getAction(ResourceToolAction.PASTE_COPIED);
				if(copyAction == null)
				{
					List<ResourceToolAction> actions = typedef.getActions(ResourcesAction.PASTE_COPIED_ACTIONS);
					if(actions != null && actions.size() > 0)
					{
						copyAction = actions.get(0);
					}
				}
				if(copyAction == null)
				{
					// TODO: why would the copy action be null?
                    // This is often null when it's invoked with file picker helper
                    if (!MODE_HELPER.equals(state.getAttribute(STATE_MODE))) {
					  log.warn("copyAction null. typeId == " + typeId + " itemId == " + itemId);
                    }
				}
				else if(copyAction instanceof ServiceLevelAction)
				{
					((ServiceLevelAction) copyAction).initializeAction(reference);
				}
				else
				{
					addAlert(state, "TODO: Unable to attach this item");
					return;
				}
				
				ResourceProperties props = resource.getProperties();
				if(filter != null)
				{
					if(! filter.allowSelect(resource))
					{
						String displayName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						addAlert(state, (String) hrb.getFormattedMessage("filter", new Object[]{displayName}));
						if(copyAction != null && copyAction instanceof ServiceLevelAction)
						{
							((ServiceLevelAction) copyAction).cancelAction(reference);
						}
						return;
					}
				}
				ResourcePropertiesEdit newprops = contentService.newResourceProperties();
				newprops.set(props);

				InputStream contentStream = resource.streamContent();
				String contentType = resource.getContentType();
				String filename = Validator.getFileName(itemId);
				String resourceId = Validator.escapeResourceName(filename);

				String siteId = toolManager.getCurrentPlacement().getContext();
				String toolName = (String) toolSession.getAttribute(STATE_ATTACH_TOOL_NAME);
				if(toolName == null)
				{
					toolName = toolManager.getCurrentPlacement().getTitle();
				}
			
				enableSecurityAdvisor();
				attachment = contentService.addAttachmentResource(resourceId, siteId, toolName, contentType, contentStream, props);

				String displayName = newprops.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				String containerId = contentService.getContainingCollectionId (attachment.getId());
				String accessUrl = attachment.getUrl();

				AttachItem item = new AttachItem(attachment.getId(), displayName, containerId, accessUrl);
				item.setContentType(contentType);
				item.setResourceType(typeId);
				item.setHoverText(typedef.getLocalizedHoverText(resource));
				item.setIconLocation(typedef.getIconLocation(resource));
				item.setIconClass(typedef.getIconClass(resource));
				new_items.add(item);
				toolSession.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
				disableSecurityAdvisors();
			}
			catch (PermissionException e)
			{
				addAlert(state, crb.getString("notpermis4"));
			}
			catch(OverQuotaException e)
			{
				addAlert(state, crb.getString("overquota.site"));
			}
			catch(ServerOverloadException e)
			{
				addAlert(state, crb.getString("failed"));
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
				log.debug("ResourcesAction.attachItem ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, crb.getString("failed"));
			}
			finally
			{
				if(copyAction == null)
				{
					// do nothing
				}
				else if(copyAction instanceof ServiceLevelAction)
				{
					if(attachment == null)
					{
						((ServiceLevelAction) copyAction).cancelAction(entityManager.newReference(resource.getReference()));
					}
					else
					{
						((ServiceLevelAction) copyAction).finalizeAction(entityManager.newReference(attachment.getReference()));
					}
				}
			}
		}
		toolSession.setAttribute(STATE_ADDED_ITEMS, new_items);
	}

	/**
	 * @param itemId
	 * @param state
	 */
	@SuppressWarnings("unchecked")
	public void attachLink(String itemId, SessionState state)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) toolSession.getAttribute (STATE_CONTENT_SERVICE);
		ResourceTypeRegistry registry = (ResourceTypeRegistry) toolSession.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}

		List<AttachItem> new_items = (List<AttachItem>) toolSession.getAttribute(STATE_ADDED_ITEMS);
		if(new_items == null)
		{
			new_items = new Vector<AttachItem>();
			toolSession.setAttribute(STATE_ADDED_ITEMS, new_items);
		}

		Integer max_cardinality = (Integer) toolSession.getAttribute(STATE_ATTACH_CARDINALITY);
		if(max_cardinality == null)
		{
			max_cardinality = FilePickerHelper.CARDINALITY_MULTIPLE;
			toolSession.setAttribute(STATE_ATTACH_CARDINALITY, max_cardinality);
		}

		boolean found = false;
		Iterator<AttachItem> it = new_items.iterator();
		while(!found && it.hasNext())
		{
			AttachItem item = it.next();
			if(item.getId().equals(itemId))
			{
				found = true;
			}
		}

		if(!found)
		{
			try
			{
				String toolName = (String) toolSession.getAttribute(STATE_ATTACH_TOOL_NAME);
				if(toolName == null)
				{
					toolName = toolManager.getCurrentPlacement().getTitle();
					toolSession.setAttribute(STATE_ATTACH_TOOL_NAME, toolName);
				}
				ContentResource res = contentService.getResource(itemId);
				
				ResourceProperties props = res.getProperties();
				
				ContentResourceFilter filter = (ContentResourceFilter) state.getAttribute(STATE_ATTACHMENT_FILTER);
				if(filter != null)
				{
					if(! filter.allowSelect(res))
					{
						String displayName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						addAlert(state, (String) hrb.getFormattedMessage("filter", new Object[]{displayName}));
						return;
					}
				}

				String displayName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				String containerId = contentService.getContainingCollectionId (itemId);
				String accessUrl = res.getUrl();

				AttachItem item = new AttachItem(itemId, displayName, containerId, accessUrl);
				item.setContentType(res.getContentType());
				String typeId = res.getResourceType();
				item.setResourceType(typeId);
				ResourceType typedef = registry.getType(typeId);
				item.setHoverText(typedef.getLocalizedHoverText(res));
				item.setIconLocation(typedef.getIconLocation(res));
				item.setIconClass(typedef.getIconClass(res));
				
				new_items.add(item);
				toolSession.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
			}
			catch (PermissionException e)
			{
				addAlert(state, crb.getString("notpermis4"));
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
				log.debug("ResourcesAction.attachItem ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, crb.getString("failed"));
			}
		}
		toolSession.setAttribute(STATE_ADDED_ITEMS, new_items);
	}

	/**
	 * Allow extension classes to control which build method gets called for this pannel
	 * @param panel
	 * @return
	 */
	protected String panelMethodName(String panel)
	{
		// we are always calling buildMainPanelContext
		return "buildMainPanelContext";
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.cheftool.VelocityPortletPaneledAction#toolModeDispatch(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@SuppressWarnings("unchecked")
	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
			throws ToolException
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		SessionState state = getState(req);
		
		Tool tool = toolManager.getCurrentTool();
		String url = (String) sessionManager.getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		if (url == null)
		{
			cleanup(state);
			return;
		}

		if (MODE_ATTACHMENT_DONE.equals(toolSession.getAttribute(STATE_FILEPICKER_MODE)))
		{
			// canceled, so restore the original list 
			List attachments = (List) toolSession.getAttribute(STATE_ATTACHMENT_ORIGINAL_LIST);

			if (attachments == null)
			{
				attachments = entityManager.newReferenceList();
			}
			
			if (toolSession.getAttribute(STATE_HELPER_CANCELED_BY_USER) == null)
			{
				// not canceled.  The attachments should have been added by doAddattachments
			}
			else
			{
				// canceled, so restore original list
				toolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, attachments);
				toolSession.setAttribute(FilePickerHelper.FILE_PICKER_CANCEL, Boolean.TRUE.toString());
			}

			cleanup(state);

			//Tool tool = toolManager.getCurrentTool();

			//String url = (String) sessionManager.getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);

			sessionManager.getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

			try
			{
				res.sendRedirect(url);
			}
			catch (IOException e)
			{
				log.warn("IOException: ", e);
			}
			return;
		}
		else if(sendToHelper(req, res, req.getPathInfo()))
		{
			return;
		}
		else
		{
			super.toolModeDispatch(methodBase, methodExt, req, res);
		}
	}

	/**
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	public void doCompleteCreateWizard(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// find the ContentHosting service
		ContentHostingService contentService = (ContentHostingService) toolSession.getAttribute (STATE_CONTENT_SERVICE);

		ResourceTypeRegistry registry = (ResourceTypeRegistry) toolSession.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		ListItem item = (ListItem) toolSession.getAttribute(STATE_NEW_ATTACHMENT);
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		String user_action = params.getString("user_action");
		
		String displayName = null;
		
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		if(user_action == null)
		{
			
		}
		else if("save".equals(user_action))
		{
			
			item.captureProperties(params, ListItem.DOT + "0");
			String collectionId = pipe.getContentEntity().getId();
			try 
			{
				// title
				displayName = item.getName();
					
				String basename = displayName.trim();
				String extension = "";
				if(displayName.contains("."))
				{
					String[] parts = displayName.split("\\.");
					StringBuffer sb = new StringBuffer(parts[0]);
					if(parts.length > 1)
					{
						extension = parts[parts.length - 1];
					}
					
					for(int i = 1; i < parts.length - 1; i++)
					{
						sb.append("." + parts[i]);
					}
					
					basename = sb.toString();
				}
				
				// create resource
				ContentResourceEdit resource = contentService.addResource(collectionId, basename, extension, MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				
				item.updateContentResourceEdit(resource);
				
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
							iAction.finalizeAction(entityManager.newReference(resource.getReference()), pipe.getInitializationId());
						}
						resourceType = action.getTypeId();
					}
				}
				
				resource.setResourceType(resourceType);
				
				byte[] content = pipe.getRevisedContent();
				if(content == null)
				{
					InputStream stream = pipe.getRevisedContentStream();
					if(stream == null)
					{
						log.warn("pipe with null content and null stream: " + pipe.getFileName());
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

				resource.setContentType(pipe.getRevisedMimeType());
				
				ResourcePropertiesEdit resourceProperties = resource.getPropertiesEdit();
				resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
				
				Map<String,String> values = pipe.getRevisedResourceProperties();
				for(Iterator<Entry<String, String>> mapIter = values.entrySet().iterator(); mapIter.hasNext();) 
				{
					Entry<String, String> entry = mapIter.next();
					resourceProperties.addProperty(entry.getKey(), entry.getValue());
				}
				
				// notification
				int noti = NotificationService.NOTI_NONE;
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
				
				contentService.commitResource(resource, noti);
				
				toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);

				// show folder if in hierarchy view
				Set<String> expandedCollections = getExpandedCollections(toolSession);
				expandedCollections.add(collectionId);
				
				if(checkSelctItemFilter(resource, state))
				{
					AttachItem new_item = new AttachItem(resource.getId(), displayName, collectionId, resource.getUrl());
					new_item.setContentType(resource.getContentType());
					String typeId = resource.getResourceType();
					new_item.setResourceType(typeId);
					ResourceType typedef = registry.getType(typeId);
					new_item.setHoverText(typedef.getLocalizedHoverText(resource));
					new_item.setIconLocation(typedef.getIconLocation(resource));
					new_item.setIconClass(typedef.getIconClass(resource));
					
					List new_items = (List) toolSession.getAttribute(STATE_ADDED_ITEMS);
					if(new_items == null)
					{
						new_items = new Vector();
						toolSession.setAttribute(STATE_ADDED_ITEMS, new_items);
					}
					
					new_items.add(new_item);
				}
				else
				{
					addAlert(state, (String) hrb.getFormattedMessage("filter", new Object[]{displayName}));
				}
				
				toolSession.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
				toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);
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
				addAlert(state, crb.getString("failed"));
			}
			catch (OverQuotaException e)
			{
                int quota = 20;
			    try {
                    ContentCollection collection = contentService.getCollection(collectionId);
                    long cq = contentService.getQuota(collection); // in kb
                    quota = (int)(cq / 1024);
                } catch (Exception e1) {
                    // nothing helpful to do here
                }
				addAlert(state, crb.getFormattedMessage("overquota", new Object[] {quota}));
			}
            catch (IdUniquenessException e)
            {
	            // TODO Auto-generated catch block
	            log.warn("IdUniquenessException ", e);
            }
            catch (IdLengthException e)
            {
	            // TODO Auto-generated catch block
	            log.warn("IdLengthException ", e);
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
				}
			}
			toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);
		}
	}

	/**
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	public void doDispatchAction(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		
		// find the ContentHosting service
		ContentHostingService contentService = (ContentHostingService) toolSession.getAttribute (STATE_CONTENT_SERVICE);
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		String action_string = params.getString("rt_action");
		String selectedItemId = params.getString("selectedItemId");
		
		String[] parts = action_string.split(ResourceToolAction.ACTION_DELIMITER);
		String typeId = parts[0];
		String actionId = parts[1];
		
		// ResourceType type = getResourceType(selectedItemId, state);
		ResourceTypeRegistry registry = (ResourceTypeRegistry) toolSession.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		ResourceType type = registry.getType(typeId); 
		
		Reference reference = entityManager.newReference(contentService.getReference(selectedItemId));
		
		ResourceToolAction action = type.getAction(actionId);
		if(action == null)
		{
			
		}
		else if(action instanceof InteractionAction)
		{
			toolSession.setAttribute(ResourcesAction.STATE_CREATE_WIZARD_COLLECTION_ID, selectedItemId);
			
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
				Iterator<String> it = propKeys.iterator();
				while(it.hasNext())
				{
					String key = it.next();
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
				try 
				{
					pipe.setMimeType(((ContentResource) entity).getContentType());
					pipe.setContent(((ContentResource) entity).getContent());
				} 
				catch (ServerOverloadException e) 
				{
					log.warn(this + ".doDispatchAction ServerOverloadException", e);
				}
			}

			startHelper(data.getRequest(), iAction.getHelperId(), MAIN_PANEL);
		}
		else if(action instanceof ServiceLevelAction)
		{
			ServiceLevelAction sAction = (ServiceLevelAction) action;
			sAction.initializeAction(reference);
			switch(sAction.getActionType())
			{
				case COPY:
					List<String> items_to_be_copied = new Vector<String>();
					if(selectedItemId != null)
					{
						items_to_be_copied.add(selectedItemId);
					}
					toolSession.setAttribute(ResourcesAction.STATE_ITEMS_TO_BE_COPIED, items_to_be_copied);
					break;
				case DUPLICATE:
					//duplicateItem(state, selectedItemId, contentService.getContainingCollectionId(selectedItemId));
					break;
				case DELETE:
					//deleteItem(state, selectedItemId);
					if (toolSession.getAttribute(STATE_MESSAGE) == null)
					{
						// need new context
						//toolSession.setAttribute (STATE_MODE, MODE_DELETE_FINISH);
					}
					break;
				case MOVE:
					List<String> items_to_be_moved = new Vector<String>();
					if(selectedItemId != null)
					{
						items_to_be_moved.add(selectedItemId);
					}
					//toolSession.setAttribute(STATE_ITEMS_TO_BE_MOVED, items_to_be_moved);
					break;
				case VIEW_METADATA:
					break;
				case REVISE_METADATA:
					toolSession.setAttribute(ResourcesAction.STATE_REVISE_PROPERTIES_ENTITY_ID, selectedItemId);
					toolSession.setAttribute(ResourcesAction.STATE_REVISE_PROPERTIES_ACTION, action);
					toolSession.setAttribute (STATE_FILEPICKER_MODE, ResourcesAction.MODE_REVISE_METADATA);
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
					//pasteItem(state, selectedItemId);
					break;
				case PASTE_COPIED:
					//pasteItem(state, selectedItemId);
					break;
				case REVISE_ORDER:
					//toolSession.setAttribute(STATE_REORDER_FOLDER, selectedItemId);
					//toolSession.setAttribute(STATE_FILEPICKER_MODE, MODE_REORDER);
					break;
				default:
					break;
			}
			// not quite right for actions involving user interaction in Resources tool.
			// For example, with delete, this should be after the confirmation and actual deletion
			// Need mechanism to remember to do it later
			sAction.finalizeAction(reference);
			
		}
	}
	
	/**
	* Add the collection id into the expanded collection list
	 * @throws PermissionException
	 * @throws TypeException
	 * @throws IdUnusedException
	*/
	@SuppressWarnings("unchecked")
	public void doExpand_collection(RunData data) throws IdUnusedException, TypeException, PermissionException
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		Set<String> expandedItems = getExpandedCollections(toolSession);

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		String id = params.getString("collectionId");
		expandedItems.add(id);

	}	// doExpand_collection

	/**
	* Remove the collection id from the expanded collection list
	*/
	@SuppressWarnings("unchecked")
	public void doCollapse_collection(RunData data)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		Set<String> expandedItems = getExpandedCollections(toolSession);

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();
		String collectionId = params.getString("collectionId");

		SortedSet newSet = new TreeSet();
		for (String id : expandedItems)
		{
			if (id.indexOf(collectionId)==-1)
			{
				newSet.add(id);
			}
		}
		expandedItems.clear();
		expandedItems.addAll(newSet);

	}	// doCollapse_collection

	/**
	* Expand all the collection resources.
	*/
	@SuppressWarnings("unchecked")
	public void doExpandall ( RunData data)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		toolSession.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		// expansion actually occurs in getBrowseItems method.
		toolSession.setAttribute(STATE_EXPAND_ALL_FLAG,  Boolean.TRUE.toString());
		toolSession.setAttribute(STATE_NEED_TO_EXPAND_ALL, Boolean.TRUE.toString());

	}	// doExpandall

	/**
	* Unexpand all the collection resources
	*/
	@SuppressWarnings("unchecked")
	public void doUnexpandall ( RunData data)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings ("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		toolSession.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		getExpandedCollections(toolSession).clear();
		toolSession.setAttribute(STATE_EXPAND_ALL_FLAG, Boolean.FALSE.toString());

	}	// doUnexpandall
	
	/**
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	public void doShowOtherSites(RunData data)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		toolSession.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		toolSession.setAttribute(STATE_SHOW_OTHER_SITES, Boolean.TRUE.toString());
	}

	/**
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	public void doHideOtherSites(RunData data)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();

		toolSession.setAttribute(STATE_SHOW_OTHER_SITES, Boolean.FALSE.toString());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		toolSession.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

	}
	
	/**
	 * @param resource
	 * @param state
	 * @return
	 */
	protected boolean checkSelctItemFilter(ContentResource resource, SessionState state) 
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		ContentResourceFilter filter = (ContentResourceFilter)toolSession.getAttribute(STATE_ATTACHMENT_FILTER);
		
		if (filter != null)
		{
			return filter.allowSelect(resource);
		}
		return true;
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
				@SuppressWarnings("unused")
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
						@SuppressWarnings("unused")
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
	 * AttachItem
	 *
	 */
	public static class AttachItem
	{
		protected String m_id;
		protected String m_displayName;
		protected String m_accessUrl;
		protected String m_collectionId;
		protected String m_contentType;
		protected String m_resourceType;
		protected String hoverText;
		protected String iconLocation;
		protected String iconClass;

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
         * @param entity
         */
        public AttachItem(ContentEntity entity)
        {
			m_id = entity.getId();
			m_displayName = entity.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			m_collectionId = entity.getContainingCollection().getId();
			m_accessUrl = entity.getUrl();
			if(entity instanceof ContentResource)
			{
				m_contentType = ((ContentResource) entity).getContentType();
			}
			m_resourceType = entity.getResourceType();

       }

		/**
         * @param resourceType
         */
        public void setResourceType(String resourceType)
        {
	        this.m_resourceType = resourceType;
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

		/**
         * @return the resourceType
         */
        public String getResourceType()
        {
        	return m_resourceType;
        }

		public String getHoverText()
        {
        	return hoverText;
        }

		public void setHoverText(String hoverText)
        {
        	this.hoverText = hoverText;
        }

		public String getIconLocation()
        {
        	return iconLocation;
        }

		public void setIconLocation(String iconLocation)
        {
			if(iconLocation == null)
			{
				ContentTypeImageService imageService = (ContentTypeImageService) ComponentManager.get("org.sakaiproject.content.api.ContentTypeImageService");
				if(this.m_contentType == null)
				{
					iconLocation = imageService.getContentTypeImage("application/binary");
				}
				else
				{
					iconLocation = imageService.getContentTypeImage(this.m_contentType);
				}
			}
        	this.iconLocation = iconLocation;
        }
		
		public String getIconClass()
		{
			return iconClass;
		}

		public void setIconClass(String iconClass)
		{
			if(iconClass == null)
			{
				ContentTypeImageService imageService = (ContentTypeImageService) ComponentManager.get("org.sakaiproject.content.api.ContentTypeImageService");
				if(this.m_contentType == null)
				{
					iconClass = imageService.getContentTypeImageClass("application/binary");
				}
				else
				{
					iconClass = imageService.getContentTypeImageClass(this.m_contentType);
				}
			}
			this.iconClass = iconClass;
		}

	}	// Inner class AttachItem
	
	/**
	* Prepare the current page of site collections to display.
	* @return List of ListItem objects to display on this page.
	*/
	protected List<ListItem> prepPage(SessionState state)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		List<ListItem> rv = new Vector<ListItem>();

		// access the page size
		int pageSize = ((Integer) toolSession.getAttribute(STATE_PAGESIZE)).intValue();

		// cleanup prior prep
		state.removeAttribute(STATE_NUM_MESSAGES);

		// are we going next or prev, first or last page?
		boolean goNextPage = toolSession.getAttribute(STATE_GO_NEXT_PAGE) != null;
		boolean goPrevPage = toolSession.getAttribute(STATE_GO_PREV_PAGE) != null;
		boolean goFirstPage = toolSession.getAttribute(STATE_GO_FIRST_PAGE) != null;
		boolean goLastPage = toolSession.getAttribute(STATE_GO_LAST_PAGE) != null;
		state.removeAttribute(STATE_GO_NEXT_PAGE);
		state.removeAttribute(STATE_GO_PREV_PAGE);
		state.removeAttribute(STATE_GO_FIRST_PAGE);
		state.removeAttribute(STATE_GO_LAST_PAGE);

		// are we going next or prev message?
		boolean goNext = toolSession.getAttribute(STATE_GO_NEXT) != null;
		boolean goPrev = toolSession.getAttribute(STATE_GO_PREV) != null;
		state.removeAttribute(STATE_GO_NEXT);
		state.removeAttribute(STATE_GO_PREV);

		// read all channel messages
		List<ListItem> allMessages = readAllResources(state);

		if (allMessages == null)
		{
			return rv;
		}
		
		String messageIdAtTheTopOfThePage = null;
		Object topMsgId = toolSession.getAttribute(STATE_TOP_PAGE_MESSAGE_ID);
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
							&&	(toolSession.getAttribute(STATE_PREV_PAGE_EXISTS) == null)
							&&	!goNextPage && !goPrevPage && !goNext && !goPrev && !goFirstPage && !goLastPage);

		// if we have no next page and do have a top message, then we will stay "pinned" to the bottom
		boolean pinToBottom = (	(messageIdAtTheTopOfThePage != null)
							&&	(toolSession.getAttribute(STATE_NEXT_PAGE_EXISTS) == null)
							&&	!goNextPage && !goPrevPage && !goNext && !goPrev && !goFirstPage && !goLastPage);

		// how many messages, total
		int numMessages = allMessages.size();

		if (numMessages == 0)
		{
			return rv;
		}

		// save the number of messges
		toolSession.setAttribute(STATE_NUM_MESSAGES, Integer.valueOf(numMessages));

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
		toolSession.setAttribute(STATE_TOP_PAGE_MESSAGE_ID, itemAtTheTopOfThePage.getId());
		toolSession.setAttribute(STATE_TOP_MESSAGE_INDEX, Integer.valueOf(posStart));


		// which message starts the next page (if any)
		int next = posStart + pageSize;
		if (next < numMessages)
		{
			toolSession.setAttribute(STATE_NEXT_PAGE_EXISTS, "");
		}
		else
		{
			state.removeAttribute(STATE_NEXT_PAGE_EXISTS);
		}

		// which message ends the prior page (if any)
		int prev = posStart - 1;
		if (prev >= 0)
		{
			toolSession.setAttribute(STATE_PREV_PAGE_EXISTS, "");
		}
		else
		{
			state.removeAttribute(STATE_PREV_PAGE_EXISTS);
		}

		if (toolSession.getAttribute(STATE_VIEW_ID) != null)
		{
			int viewPos = findResourceInList(allMessages, (String) toolSession.getAttribute(STATE_VIEW_ID));
	
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
			toolSession.setAttribute(STATE_VIEW_ID, ((ListItem) allMessages.get(viewPos)).getId());
			
			// if the view message is no longer on the current page, adjust the page
			// Note: next time through this will get processed
			if (viewPos < posStart)
			{
				toolSession.setAttribute(STATE_GO_PREV_PAGE, "");
			}
			else if (viewPos > posEnd)
			{
				toolSession.setAttribute(STATE_GO_NEXT_PAGE, "");
			}
			
			if (viewPos > 0)
			{
				toolSession.setAttribute(STATE_PREV_EXISTS,"");
			}
			else
			{
				state.removeAttribute(STATE_PREV_EXISTS);
			}
			
			if (viewPos < numMessages-1)
			{
				toolSession.setAttribute(STATE_NEXT_EXISTS,"");
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
	@SuppressWarnings("unchecked")
	protected List<ListItem> readAllResources(SessionState state)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		ContentHostingService contentService = (ContentHostingService) toolSession.getAttribute (STATE_CONTENT_SERVICE);
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) toolSession.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			toolSession.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		List<ListItem> other_sites = new Vector<ListItem>();

		String homeCollectionId = (String) toolSession.getAttribute(STATE_HOME_COLLECTION_ID);

		// make sure the collectionId is set
		String collectionId = (String) toolSession.getAttribute(STATE_DEFAULT_COLLECTION_ID);
		if(collectionId == null)
		{
			collectionId = homeCollectionId;
		}

		Set<String> expandedCollections = getExpandedCollections(toolSession);
		
		Comparator userSelectedSort = (Comparator) toolSession.getAttribute(STATE_LIST_VIEW_SORT);
		
		ContentResourceFilter filter = (ContentResourceFilter) state.getAttribute(STATE_ATTACHMENT_FILTER);
		
		// add user's personal workspace
		User user = userDirectoryService.getCurrentUser();
		String userId = user.getId();
		String wsId = siteService.getUserSiteId(userId);
		String wsCollectionId = contentService.getSiteCollection(wsId);
		
		if(! collectionId.equals(wsCollectionId))
		{
            try
            {
            	ContentCollection wsCollection = contentService.getCollection(wsCollectionId);
				ListItem wsRoot = ListItem.getListItem(wsCollection, null, registry, false, expandedCollections, null, null, 0, userSelectedSort, false, filter);
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
		Map othersites = contentService.getCollectionMap();
		SortedSet sort = new TreeSet();
		for(Iterator<Entry<String, String>> mapIter = othersites.entrySet().iterator(); mapIter.hasNext();) 
		{
			  Entry<String, String> entry = mapIter.next();
              sort.add(entry.getValue() + ResourcesAction.DELIM + entry.getKey());
		}
		
		Iterator<String> sortIt = sort.iterator();
		while(sortIt.hasNext())
		{
			String keyvalue = sortIt.next();
			String displayName = keyvalue.substring(0, keyvalue.lastIndexOf(ResourcesAction.DELIM));
			String collId = keyvalue.substring(keyvalue.lastIndexOf(ResourcesAction.DELIM) + 1);
			if(! collectionId.equals(collId) && ! wsCollectionId.equals(collId))
			{
				ContentCollection collection;
                try
                {
	                collection = contentService.getCollection(collId);
					ListItem root = ListItem.getListItem(collection, null, registry, false, expandedCollections, null, null, 0, null, false, null);
					root.setName(displayName);
					other_sites.add(root);
                }
                catch (IdUnusedException e)
                {
	                // its expected that some collections eg the drop box collection may not exit
	                log.debug("IdUnusedException (FilePickerAction.readAllResources()) collId == " + collId + " --> " + e);
                }
                catch (TypeException e)
                {
	                log.warn("TypeException (FilePickerAction.readAllResources()) collId == " + collId + " --> " + e);
                }
                catch (PermissionException e)
                {
                	//SAK-18496 we don't show a user collections they don't have access to 
                	log.debug("PermissionException (FilePickerAction.readAllResources()) collId == " + collId + " --> " + e);
                }
			}
          }
		
		return other_sites;
	}
	
	/**
	* Navigate in the resource hireachy
	*/
	@SuppressWarnings("unchecked")
	public void doNavigate ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ToolSession toolSession = sessionManager.getCurrentToolSession();

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
		ContentHostingService contentService = (ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		try
		{
			contentService.checkCollection(collectionId);
			toolSession.setAttribute(STATE_DEFAULT_COLLECTION_ID, collectionId);
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
			state.setAttribute(STATE_COLLECTION_ID, collectionId);
			Set<String> currentMap = getExpandedCollections(toolSession);
			SortedSet<String> newCurrentMap = new TreeSet<String>();
			for(String id: currentMap)
			{
				if(!id.startsWith(collectionId))
				{
					newCurrentMap.add(id);
				}
			}
			
			newCurrentMap.add(collectionId);
			currentMap.clear();
			currentMap.addAll(newCurrentMap);
		}

	}	// doNavigate

	/**
	* Find the resource with this id in the list.
	* @param resources The list of messages.
	* @param id The message id.
	* @return The index position in the list of the message with this id, or -1 if not found.
	*/
	@SuppressWarnings("unchecked")
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
	
	protected static boolean checkItemFilter(ContentResource resource, ListItem newItem, ContentResourceFilter filter) 
	{
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

	protected static boolean checkSelctItemFilter(ContentResource resource, ContentResourceFilter filter) 
	{
		if (filter != null)
		{
			return filter.allowSelect(resource);
		}
		return true;
	}

    // ---------------------------

    /** The collection id being browsed. */
    private static final String STATE_COLLECTION_ID = PREFIX /*+ REQUEST */+ "collection_id";

    /**
     * @param state
     * @return
     */
    public static List getCollectionPath(SessionState state)
    {
        log.debug("ResourcesAction.getCollectionPath()");
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
                    Reference ref = entityManager.newReference(contentService.getReference(id));
                    Site site = siteService.getSite(ref.getContext());
                    String[] args = {site.getTitle()};
                    name = trb.getFormattedMessage("title.dropbox", args);
                }
                else if(contentService.COLLECTION_SITE.equals(containingCollectionId))
                {
                    Reference ref = entityManager.newReference(contentService.getReference(id));
                    Site site = siteService.getSite(ref.getContext());
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
	
	/**
	 * get/init state attribute STATE_EXPANDED_COLLECTIONS
	 * @param session The tool session to get the object from or create it in.
	 * @return An {@link #STATE_EXPANDED_COLLECTIONS} but never <code>null</code>.
	 */
	private static Set<String> getExpandedCollections(ToolSession session) {
		Set<String> current = (Set<String>) session.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(current == null)
		{
			current = new CopyOnWriteArraySet<String>();
			session.setAttribute(STATE_EXPANDED_COLLECTIONS, current);
		}
		return current;
	}

}	// class FilePickerAction 
