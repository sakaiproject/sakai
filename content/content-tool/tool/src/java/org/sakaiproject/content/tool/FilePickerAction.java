/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
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
import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;

/**
 * The FilePickerAction drives the FilePicker helper.<br />
 * This works with the ResourcesTool to show a file picker / attachment editor that can be used by any Sakai tools as a helper.<br />
 * If the user ends without a cancel, the original collection of attachments is replaced with the edited list - otherwise it is left unchanged.
 */
public class FilePickerAction extends VelocityPortletPaneledAction
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("helper");
	
    private static final Log logger = LogFactory.getLog(FilePickerAction.class);
	
	protected static final String PREFIX = "filepicker.";
	
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
	
	protected static final String STATE_ATTACH_CARDINALITY = PREFIX + "attach_cardinality";
	
	protected static final String STATE_ATTACH_LINKS = PREFIX + "attach_links";

	protected static final String STATE_ATTACHMENT_FILTER = PREFIX + "attachment_filter";
	protected static final String STATE_ATTACHMENT_LIST = PREFIX + "attachment_list";
	protected static final String STATE_CONTENT_SERVICE = PREFIX + "content_service";
	
	/** The content type image lookup service in the State. */
	protected static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = PREFIX + "content_type_image_service";
	
	protected static final String STATE_DEFAULT_COLLECTION_ID = PREFIX + "default_collection_id";
	protected static final String STATE_EXPAND_ALL = PREFIX + "expand_all";
	protected static final String STATE_EXPAND_ALL_FLAG = PREFIX + "expand_all_flag";
	protected static final String STATE_EXPANDED_COLLECTIONS = PREFIX + "expanded_collections";
	protected static final String STATE_FILE_UPLOAD_MAX_SIZE = PREFIX + "file_upload_max_size";
	protected static final String STATE_FILEPICKER_MODE = PREFIX + "mode";
	protected static final String STATE_HELPER_CANCELED_BY_USER = PREFIX + "helper_canceled_by_user";
	protected static final String STATE_HELPER_CHANGED = PREFIX + "made_changes";
	protected static final String STATE_HOME_COLLECTION_ID  = PREFIX + "home_collection_id";
	protected static final String STATE_NAVIGATION_ROOT = PREFIX + "navigation_root";
	protected static final String STATE_NEED_TO_EXPAND_ALL = PREFIX + "need_to_expand_all";
	protected static final String STATE_REMOVED_ITEMS = PREFIX + "removed_items";
	protected static final String STATE_RESOURCES_TYPE_REGISTRY = PREFIX + "resource_type_registry";
	protected static final String STATE_SHOW_ALL_SITES = PREFIX + "show_all_sites";
	protected static final String STATE_SHOW_OTHER_SITES = PREFIX + "show_other_sites";

	
	/** The sort by */
	private static final String STATE_SORT_BY = PREFIX + "sort_by";

	/** The sort ascending or decending */
	private static final String STATE_SORT_ASC = PREFIX + "sort_asc";

	private static final String TEMPLATE_ATTACH = "content/sakai_filepicker_attach";
	private static final String TEMPLATE_SELECT = "content/sakai_filepicker_select";

	private static final String STATE_LIST_SELECTIONS = null;





	/**
     * @param portlet
     * @param context
     * @param data
     * @param state
     * @return
     */
    protected String buildCreateContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
    {
	    // TODO Auto-generated method stub
	    return null;
    }

	/**
     * @param portlet
     * @param context
     * @param data
     * @param state
     * @return
     */
    protected String buildItemTypeContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
    {
	    // TODO Auto-generated method stub
	    return null;
    }

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		// if we are in edit attachments...
		String mode = (String) state.getAttribute(ResourcesAction.STATE_MODE);
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		String helper_mode = (String) state.getAttribute(STATE_FILEPICKER_MODE);

		if (mode == null || helper_mode == null || toolSession.getAttribute(FilePickerHelper.START_HELPER) != null)
		{
			toolSession.removeAttribute(FilePickerHelper.START_HELPER);
			mode = initHelperAction(state, toolSession);
			helper_mode = (String) state.getAttribute(STATE_FILEPICKER_MODE);
		}

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


		String template = null;
		if(MODE_ATTACHMENT_SELECT_INIT.equals(helper_mode))
		{
			template = buildSelectAttachmentContext(portlet, context, data, state);
		}
//		else if(MODE_ATTACHMENT_CREATE_INIT.equals(helper_mode))
//		{
//			template = buildCreateContext(portlet, context, data, state);
//		}
//		else if(MODE_ATTACHMENT_NEW_ITEM_INIT.equals(helper_mode))
//		{
//			template = buildItemTypeContext(portlet, context, data, state);
//		}
//		else if(MODE_ATTACHMENT_EDIT_ITEM_INIT.equals(helper_mode))
//		{
//			template = buildCreateContext(portlet, context, data, state);
//		}
		
		return template;
		
	}

	/**
     * @param portlet
     * @param context
     * @param data
     * @param state
     * @return
     */
    protected String buildSelectAttachmentContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
    {
		context.put("tlang",rb);

		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));
		
		context.put("labeler", new ResourceTypeLabeler());
		context.put("ACTION_DELIMITER", ResourceToolAction.ACTION_DELIMITER);

		List new_items = (List) state.getAttribute(STATE_ADDED_ITEMS);
		if(new_items == null)
		{
			new_items = new Vector();
			state.setAttribute(STATE_ADDED_ITEMS, new_items);
		}
		context.put("attached", new_items);
		context.put("last", new Integer(new_items.size() - 1));

		Integer max_cardinality = (Integer) state.getAttribute(STATE_ATTACH_CARDINALITY);
		if(max_cardinality == null)
		{
			max_cardinality = FilePickerHelper.CARDINALITY_MULTIPLE;
			state.setAttribute(STATE_ATTACH_CARDINALITY, max_cardinality);
		}
		context.put("max_cardinality", max_cardinality);

		if(new_items.size() >= max_cardinality.intValue())
		{
			context.put("disable_attach_links", Boolean.TRUE.toString());
		}

		if(state.getAttribute(STATE_HELPER_CHANGED) != null)
		{
			context.put("list_has_changed", Boolean.TRUE.toString());
		}
		
		boolean inMyWorkspace = SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
		// context.put("inMyWorkspace", Boolean.toString(inMyWorkspace));

		boolean atHome = false;

		// %%STATE_MODE_RESOURCES%%
		//boolean dropboxMode = RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES));

		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

		// make sure the collectionId is set
		String collectionId = (String) state.getAttribute(STATE_DEFAULT_COLLECTION_ID);
		if(collectionId == null)
		{
			collectionId = homeCollectionId;
		}

		context.put ("collectionId", collectionId);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);

		// String siteTitle = (String) state.getAttribute (STATE_SITE_TITLE);
		if (collectionId.equals(homeCollectionId))
		{
			atHome = true;
			//context.put ("collectionDisplayName", state.getAttribute (STATE_HOME_COLLECTION_DISPLAY_NAME));
		}
		
		// set the sort values
		String sortedBy = (String) state.getAttribute (STATE_SORT_BY);
		String sortedAsc = (String) state.getAttribute (STATE_SORT_ASC);
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
				if(logger.isDebugEnabled())
				{
					logger.debug("ResourcesAction.buildSelectAttachment (static) : IdUnusedException: " + collectionId);
				}
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
		
			SortedSet<String> expandedCollections = (SortedSet<String>) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			if(expandedCollections == null)
			{
				expandedCollections = new TreeSet<String>();
				state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
			}
			expandedCollections.add(collectionId);

			ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
			if(registry == null)
			{
				registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
				state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
			}
			
			boolean expandAll = Boolean.TRUE.toString().equals(state.getAttribute(STATE_NEED_TO_EXPAND_ALL));

			//state.removeAttribute(STATE_PASTE_ALLOWED_FLAG);

			List<ListItem> this_site = new Vector<ListItem>();
			
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
								
								ListItem item = ListItem.getListItem(db, (ListItem) null, registry, expandAll, expandedCollections, (List<String>) null, (List<String>) null, 0);
								this_site.addAll(item.convert2list());
								
	//							List dbox = getListView(dbId, highlightedItems, (ChefBrowseItem) null, false, state); 
	//							getBrowseItems(dbId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (ChefBrowseItem) null, false, state);
	//							if(dbox != null && dbox.size() > 0)
	//							{
	//								ChefBrowseItem root = (ChefBrowseItem) dbox.remove(0);
	//								// context.put("site", root);
	//								root.setName(submitter.getDisplayName() + " " + rb.getString("gen.drop"));
	//								root.addMembers(dbox);
	//								this_site.add(root);
	//							}
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
	
							ListItem item = ListItem.getListItem(db, null, registry, expandAll, expandedCollections, null, null, 0);
							this_site.addAll(item.convert2list());
							
	//						List dbox = getListView(dropboxId, highlightedItems, (ChefBrowseItem) null, false, state); 
	//						// List dbox = getBrowseItems(dropboxId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (ChefBrowseItem) null, false, state);
	//						if(dbox != null && dbox.size() > 0)
	//						{
	//							ChefBrowseItem root = (ChefBrowseItem) dbox.remove(0);
	//							// context.put("site", root);
	//							root.setName(ContentHostingService.getDropboxDisplayName());
	//							root.addMembers(dbox);
	//							this_site.add(root);
	//						}
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
				ContentCollection collection = contentService.getCollection(collectionId);
				ListItem item = ListItem.getListItem(collection, null, registry, expandAll, expandedCollections, null, null, 0);
				this_site.addAll(item.convert2list());
				
			}
			
			
//			List members = getListView(collectionId, highlightedItems, (ChefBrowseItem) null, navRoot.equals(homeCollectionId), state);
//			// List members = getBrowseItems(collectionId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (ChefBrowseItem) null, navRoot.equals(homeCollectionId), state);
//			if(members != null && members.size() > 0)
//			{
//				ChefBrowseItem root = (ChefBrowseItem) members.remove(0);
//				if(atHome && dropboxMode)
//				{
//					root.setName(siteTitle + " " + rb.getString("gen.drop"));
//				}
//				else if(atHome)
//				{
//					root.setName(siteTitle + " " + rb.getString("gen.reso"));
//				}
//				context.put("site", root);
//				root.addMembers(members);
//				this_site.add(root);
//			}


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
//				List messages = prepPage(state);
//				context.put("other_sites", messages);
//
//				if (state.getAttribute(STATE_NUM_MESSAGES) != null)
//				{
//					context.put("allMsgNumber", state.getAttribute(STATE_NUM_MESSAGES).toString());
//					context.put("allMsgNumberInt", state.getAttribute(STATE_NUM_MESSAGES));
//				}
//
//				context.put("pagesize", ((Integer) state.getAttribute(STATE_PAGESIZE)).toString());
//
//				// find the position of the message that is the top first on the page
//				if ((state.getAttribute(STATE_TOP_MESSAGE_INDEX) != null) && (state.getAttribute(STATE_PAGESIZE) != null))
//				{
//					int topMsgPos = ((Integer)state.getAttribute(STATE_TOP_MESSAGE_INDEX)).intValue() + 1;
//					context.put("topMsgPos", Integer.toString(topMsgPos));
//					int btmMsgPos = topMsgPos + ((Integer)state.getAttribute(STATE_PAGESIZE)).intValue() - 1;
//					if (state.getAttribute(STATE_NUM_MESSAGES) != null)
//					{
//						int allMsgNumber = ((Integer)state.getAttribute(STATE_NUM_MESSAGES)).intValue();
//						if (btmMsgPos > allMsgNumber)
//							btmMsgPos = allMsgNumber;
//					}
//					context.put("btmMsgPos", Integer.toString(btmMsgPos));
//				}
//
//				boolean goPPButton = state.getAttribute(STATE_PREV_PAGE_EXISTS) != null;
//				context.put("goPPButton", Boolean.toString(goPPButton));
//				boolean goNPButton = state.getAttribute(STATE_NEXT_PAGE_EXISTS) != null;
//				context.put("goNPButton", Boolean.toString(goNPButton));
//
//				/*
//				boolean goFPButton = state.getAttribute(STATE_FIRST_PAGE_EXISTS) != null;
//				context.put("goFPButton", Boolean.toString(goFPButton));
//				boolean goLPButton = state.getAttribute(STATE_LAST_PAGE_EXISTS) != null;
//				context.put("goLPButton", Boolean.toString(goLPButton));
//				*/
//
//				context.put("pagesize", state.getAttribute(STATE_PAGESIZE));
//				// context.put("pagesizes", PAGESIZES);
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
//		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));
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
		
		if(state.getAttribute(STATE_ATTACH_LINKS) == null)
		{
			// user wants copies in hidden attachments area
			template = TEMPLATE_ATTACH;
		}

		return template;
	    //return TEMPLATE_SELECT;
    }

	/**
	 * @param state
	 */
	protected void cleanup(SessionState state)
	{
		Iterator<String> attributeNameIt = state.getAttributeNames().iterator();
		while(attributeNameIt.hasNext())
		{
			String attributeName = attributeNameIt.next();
			if(attributeName.startsWith(PREFIX))
			{
				state.removeAttribute(attributeName);
			}
		}
		
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
		if (toolSession != null) 
		{
			toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS);
			toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_RESOURCE_FILTER);
			toolSession.removeAttribute(FilePickerHelper.DEFAULT_COLLECTION_ID);
		}
		
 	}	// cleanup

	/**
	 * @param state
	 * @param toolSession
	 * @return
	 */
	protected String initHelperAction(SessionState state, ToolSession toolSession)
	{
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);

		ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		state.setAttribute (STATE_CONTENT_SERVICE, contentService);
		state.setAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE, ComponentManager.get("org.sakaiproject.content.api.ContentTypeImageService"));
		state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry"));
		
		// start with a copy of the original attachment list
		List attachments = (List) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
		if (attachments != null)
		{
			attachments = EntityManager.newReferenceList(attachments);
			state.setAttribute(STATE_ATTACHMENT_LIST, attachments);
		}
		
		Object attach_links = state.getAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS);
		if(attach_links == null)
		{
			state.removeAttribute(STATE_ATTACH_LINKS);
		}
		else
		{
			state.setAttribute(STATE_ATTACH_LINKS, Boolean.TRUE.toString());
		}
		
		List<AttachItem> new_items = new Vector<AttachItem>();
		Iterator attachmentIt = attachments.iterator();
		while(attachmentIt.hasNext())
		{
			Reference ref = (Reference) attachmentIt.next();
			ContentResource res = (ContentResource) ref.getEntity();
			ResourceProperties props = res.getProperties();

			String displayName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
			String containerId = contentService.getContainingCollectionId (res.getId());
			String accessUrl = res.getUrl();

			AttachItem item = new AttachItem(res.getId(), displayName, containerId, accessUrl);
			item.setContentType(res.getContentType());
			item.setResourceType(res.getResourceType());
			
			new_items.add(item);
		}
		state.setAttribute(STATE_ADDED_ITEMS, new_items);
		
		initMessage(toolSession, state);

		state.setAttribute(STATE_ATTACHMENT_FILTER, toolSession.getAttribute(FilePickerHelper.FILE_PICKER_RESOURCE_FILTER));
		
		String defaultCollectionId = (String) toolSession.getAttribute(FilePickerHelper.DEFAULT_COLLECTION_ID);
		if(defaultCollectionId == null)
		{
			defaultCollectionId = contentService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
		}
		state.setAttribute(STATE_DEFAULT_COLLECTION_ID, defaultCollectionId);
		
		state.setAttribute(STATE_MODE, MODE_HELPER);
		state.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT);
		
		// TODO: Should check sakai.properties
		state.setAttribute(STATE_SHOW_ALL_SITES, Boolean.TRUE.toString());

		// state attribute ResourcesAction.STATE_ATTACH_TOOL_NAME should be set with a string to indicate name of tool
		String toolName = ToolManager.getCurrentPlacement().getTitle();
		state.setAttribute(ResourcesAction.STATE_ATTACH_TOOL_NAME, toolName);

		Object max_cardinality = toolSession.getAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS);
		if (max_cardinality != null)
		{
			state.setAttribute(ResourcesAction.STATE_ATTACH_CARDINALITY, max_cardinality);
		}

		if (state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE) == null)
		{
			state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, ServerConfigurationService.getString("content.upload.max", "1"));
		}
		
		return MODE_HELPER;
	}

	/**
	 * @param toolSession
	 * @param state
	 */
	protected void initMessage(ToolSession toolSession, SessionState state)
	{
		String message = (String) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT);
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT);
		if (message == null)
		{
			message = rb.getString(FilePickerHelper.FILE_PICKER_TITLE_TEXT);
		}
		state.setAttribute(ResourcesAction.STATE_ATTACH_TITLE, message);

		message = (String) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT);
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT);
		if (message == null)
		{
			message = rb.getString(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT);
		}
		state.setAttribute(ResourcesAction.STATE_ATTACH_INSTRUCTION, message);
	}

	/**
	 * @param data
	 */
	public void doAttachitem(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		//state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		String itemId = params.getString("itemId");

		Object attach_links = state.getAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS);

		if(attach_links == null)
		{
			attachItem(itemId, state);
		}
		else
		{
			attachLink(itemId, state);
		}

		List<AttachItem> removed = (List<AttachItem>) state.getAttribute(STATE_REMOVED_ITEMS);
		if(removed == null)
		{
			removed = new Vector<AttachItem>();
			state.setAttribute(STATE_REMOVED_ITEMS, removed);
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

		state.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);

	}	// doAttachitem

	/**
	 * @param data
	 */
	public void doAttachupload(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
		int max_bytes = 1024 * 1024;
		try
		{
			max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
		}
		catch(Exception e)
		{
			// if unable to parse an integer from the value
			// in the properties file, use 1 MB as a default
			max_file_size_mb = "1";
			max_bytes = 1024 * 1024;
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

				ContentHostingService contentService = (ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

				// make a set of properties to add for the new resource
				ResourcePropertiesEdit props = contentService.newResourceProperties();
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
				props.addProperty(ResourceProperties.PROP_DESCRIPTION, filename);

				// make an attachment resource for this URL
				try
				{
					String siteId = ToolManager.getCurrentPlacement().getContext();

					String toolName = (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
					if(toolName == null)
					{
						toolName = ToolManager.getCurrentPlacement().getTitle();
						state.setAttribute(STATE_ATTACH_TOOL_NAME, toolName);
					}

					ContentResource attachment = contentService.addAttachmentResource(resourceId, siteId, toolName, contentType, bytes, props);

					List<AttachItem> new_items = (List<AttachItem>) state.getAttribute(STATE_ADDED_ITEMS);
					if(new_items == null)
					{
						new_items = new Vector<AttachItem>();
						state.setAttribute(STATE_ADDED_ITEMS, new_items);
					}

					String containerId = contentService.getContainingCollectionId (attachment.getId());
					String accessUrl = attachment.getUrl();

					AttachItem item = new AttachItem(attachment.getId(), filename, containerId, accessUrl);
					item.setResourceType(ResourceType.TYPE_UPLOAD);
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
					logger.debug("ResourcesAction.doAttachupload ***** Unknown Exception ***** " + e.getMessage());
					addAlert(state, rb.getString("failed"));
				}
			}
			else
			{
				addAlert(state, rb.getString("choosefile7"));
			}
		}

		state.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);

	}	// doAttachupload

	/**
	 * @param data
	 */
	public void doAttachurl(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		String url = params.getCleanString("url");

		ContentHostingService contentService = (ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

		ResourcePropertiesEdit resourceProperties = contentService.newResourceProperties ();
		resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, url);
		resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, url);

		resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());

		try
		{
			url = validateURL(url);

			byte[] newUrl = url.getBytes();
			String newResourceId = Validator.escapeResourceName(url);

			String siteId = ToolManager.getCurrentPlacement().getContext();
			String toolName = (String) (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
			if(toolName == null)
			{
				toolName = ToolManager.getCurrentPlacement().getTitle();
				state.setAttribute(STATE_ATTACH_TOOL_NAME, toolName);
			}

			ContentResource attachment = contentService.addAttachmentResource(newResourceId, siteId, toolName, ResourceProperties.TYPE_URL, newUrl, resourceProperties);

			List<AttachItem> new_items = (List<AttachItem>) state.getAttribute(STATE_ADDED_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
				state.setAttribute(STATE_ADDED_ITEMS, new_items);
			}

			String containerId = contentService.getContainingCollectionId (attachment.getId());
			String accessUrl = attachment.getUrl();

			AttachItem item = new AttachItem(attachment.getId(), url, containerId, accessUrl);
			item.setResourceType(ResourceType.TYPE_URL);
			item.setContentType(ResourceProperties.TYPE_URL);
			new_items.add(item);
			state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
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
			logger.debug("ResourcesAction.doAttachurl ***** Unknown Exception ***** " + e.getMessage());
			addAlert(state, rb.getString("failed"));
		}

		state.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_SELECT_INIT);
		
	}	// doAttachurl

	/**
	* doCancel to return to the previous state
	*/
	public void doCancel ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		//cleanup(state);

		state.setAttribute(STATE_HELPER_CANCELED_BY_USER, Boolean.TRUE.toString());

		state.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_DONE);

	}	// doCancel



	public void doRemoveitem(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		//state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		String itemId = params.getString("itemId");

		List new_items = (List) state.getAttribute(STATE_ADDED_ITEMS);

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
			List removed = (List) state.getAttribute(STATE_REMOVED_ITEMS);
			if(removed == null)
			{
				removed = new Vector();
				state.setAttribute(STATE_REMOVED_ITEMS, removed);
			}
			removed.add(item);

			state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
		}

	}	// doRemoveitem

	public void doAddattachments(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		ContentHostingService contentService = (ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

//		// cancel copy if there is one in progress
//		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
//		{
//			initCopyContext(state);
//		}
//
//		// cancel move if there is one in progress
//		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
//		{
//			initMoveContext(state);
//		}

//		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		List new_items = (List) state.getAttribute(STATE_ADDED_ITEMS);
		if(new_items == null)
		{
			new_items = new Vector();
			state.setAttribute(STATE_ADDED_ITEMS, new_items);
		}

		List removed = (List) state.getAttribute(STATE_REMOVED_ITEMS);
		if(removed == null)
		{
			removed = new Vector();
			state.setAttribute(STATE_REMOVED_ITEMS, removed);
		}
		
		Iterator removeIt = removed.iterator();
		while(removeIt.hasNext())
		{
			AttachItem item = (AttachItem) removeIt.next();
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
		List attachments = EntityManager.newReferenceList();

		Iterator it = new_items.iterator();
		while(it.hasNext())
		{
			AttachItem item = (AttachItem) it.next();

			try
			{
				Reference ref = EntityManager.newReference(contentService.getReference(item.getId()));
				attachments.add(ref);
			}
			catch(Exception e)
			{
			}
		}
		// cleanupState(state);
		state.setAttribute(STATE_ATTACHMENT_LIST, attachments);

		// end up in main mode
//		resetCurrentMode(state);

		String field = null;

		// if there is at least one attachment
		if (attachments.size() > 0)
		{
			//check -- jim
			state.setAttribute(AttachmentAction.STATE_HAS_ATTACHMENT_BEFORE, Boolean.TRUE);
		}
		
		state.setAttribute(STATE_FILEPICKER_MODE, MODE_ATTACHMENT_DONE);

//		if(field != null)
//		{
//			int index = 0;
//			String fieldname = field;
//			Matcher matcher = INDEXED_FORM_FIELD_PATTERN.matcher(field.trim());
//			if(matcher.matches())
//			{
//				fieldname = matcher.group(0);
//				index = Integer.parseInt(matcher.group(1));
//			}
//
//			// we are trying to attach a link to a form field and there is at least one attachment
//			if(new_items == null)
//			{
//				new_items = (List) current_stack_frame.get(ResourcesAction.STATE_HELPER_NEW_ITEMS);
//				if(new_items == null)
//				{
//					new_items = (List) state.getAttribute(ResourcesAction.STATE_HELPER_NEW_ITEMS);
//				}
//			}
//			ChefEditItem edit_item = null;
//			List edit_items = (List) current_stack_frame.get(ResourcesAction.STATE_STACK_CREATE_ITEMS);
//			if(edit_items == null)
//			{
//				edit_item = (ChefEditItem) current_stack_frame.get(ResourcesAction.STATE_STACK_EDIT_ITEM);
//			}
//			else
//			{
//				edit_item = (ChefEditItem) edit_items.get(0);
//			}
//			if(edit_item != null)
//			{
//				Reference ref = (Reference) attachments.get(0);
//				edit_item.setPropertyValue(fieldname, index, ref);
//			}
//		}
	}

	/**
	 * @param itemId
	 * @param state
	 */
	public void attachItem(String itemId, SessionState state)
	{
		ContentHostingService contentService = (ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

		List new_items = (List) state.getAttribute(STATE_ADDED_ITEMS);
		if(new_items == null)
		{
			new_items = new Vector();
			state.setAttribute(STATE_ADDED_ITEMS, new_items);
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
				String toolName = (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
				if(toolName == null)
				{
					toolName = ToolManager.getCurrentPlacement().getTitle();
				}
			
				ContentResource attachment = contentService.addAttachmentResource(resourceId, siteId, toolName, contentType, bytes, props);

				String displayName = newprops.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				String containerId = contentService.getContainingCollectionId (attachment.getId());
				String accessUrl = attachment.getUrl();

				AttachItem item = new AttachItem(attachment.getId(), displayName, containerId, accessUrl);
				item.setContentType(contentType);
				item.setResourceType(res.getResourceType());
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
				logger.debug("ResourcesAction.attachItem ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, rb.getString("failed"));
			}
		}
		state.setAttribute(STATE_ADDED_ITEMS, new_items);
	}

	/**
	 * @param itemId
	 * @param state
	 */
	public void attachLink(String itemId, SessionState state)
	{
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

		List<AttachItem> new_items = (List<AttachItem>) state.getAttribute(STATE_ADDED_ITEMS);
		if(new_items == null)
		{
			new_items = new Vector<AttachItem>();
			state.setAttribute(STATE_ADDED_ITEMS, new_items);
		}

		Integer max_cardinality = (Integer) state.getAttribute(STATE_ATTACH_CARDINALITY);
		if(max_cardinality == null)
		{
			max_cardinality = FilePickerHelper.CARDINALITY_MULTIPLE;
			state.setAttribute(STATE_ATTACH_CARDINALITY, max_cardinality);
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
				String toolName = (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
				if(toolName == null)
				{
					toolName = ToolManager.getCurrentPlacement().getTitle();
					state.setAttribute(STATE_ATTACH_TOOL_NAME, toolName);
				}
				ContentResource res = contentService.getResource(itemId);
				
				ResourceProperties props = res.getProperties();
				String displayName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				String containerId = contentService.getContainingCollectionId (itemId);

				AttachItem item = new AttachItem(itemId, displayName, containerId, res.getUrl());
				item.setContentType(res.getContentType());
				item.setResourceType(res.getResourceType());
				
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
				logger.debug("ResourcesAction.attachItem ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, rb.getString("failed"));
			}
		}
		state.setAttribute(STATE_ADDED_ITEMS, new_items);
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
	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
			throws ToolException
	{
		SessionState state = getState(req);

		if (MODE_ATTACHMENT_DONE.equals(state.getAttribute(STATE_FILEPICKER_MODE)))
		{
			ToolSession toolSession = SessionManager.getCurrentToolSession();

			if (state.getAttribute(STATE_HELPER_CANCELED_BY_USER) == null)
			{
				// not canceled, so populate the original list with the results
				List attachments = (List) state.getAttribute(STATE_ATTACHMENT_LIST);

				if (attachments != null)
				{
					// get the original list
					Collection original = (Collection) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
					if(original == null)
					{
						original = EntityManager.newReferenceList();
						toolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, original);
					}

					// replace its contents with the edited attachments
					original.clear();
					original.addAll(attachments);
				}

				// otherwise the original list remains unchanged

				else if (state.getAttribute(ResourcesAction.STATE_EDIT_ID) == null)
				{
					toolSession.setAttribute(FilePickerHelper.FILE_PICKER_CANCEL, Boolean.TRUE.toString());
				}
			}
			else
			{
				toolSession.setAttribute(FilePickerHelper.FILE_PICKER_CANCEL, Boolean.TRUE.toString());
			}

			cleanup(state);

			Tool tool = ToolManager.getCurrentTool();

			String url = (String) SessionManager.getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);

			SessionManager.getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

			try
			{
				res.sendRedirect(url);
			}
			catch (IOException e)
			{
				logger.warn("IOException: ", e);
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
	
	public void doDispatchAction(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// find the ContentHosting service
		ContentHostingService contentService = (ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		String action_element = params.getString("rt_action");
		String action_string = params.getString(action_element);
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
		
		Reference reference = EntityManager.newReference(contentService.getReference(selectedItemId));
		
		ResourceToolAction action = type.getAction(actionId);
		if(action == null)
		{
			
		}
		else if(action instanceof InteractionAction)
		{
			ToolSession toolSession = SessionManager.getCurrentToolSession();
			// toolSession.setAttribute(ResourceToolAction.ACTION_ID, actionId);
			// toolSession.setAttribute(ResourceToolAction.RESOURCE_TYPE, typeId);
			
			state.setAttribute(ResourcesAction.STATE_CREATE_WIZARD_COLLECTION_ID, selectedItemId);
			
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
				try 
				{
					pipe.setMimeType(((ContentResource) entity).getContentType());
					pipe.setContent(((ContentResource) entity).getContent());
				} 
				catch (ServerOverloadException e) 
				{
					logger.warn(this + ".doDispatchAction ServerOverloadException", e);
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
					state.setAttribute(ResourcesAction.STATE_ITEMS_TO_BE_COPIED, items_to_be_copied);
					break;
				case DUPLICATE:
					//duplicateItem(state, selectedItemId, contentService.getContainingCollectionId(selectedItemId));
					break;
				case DELETE:
					//deleteItem(state, selectedItemId);
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						// need new context
						//state.setAttribute (STATE_MODE, MODE_DELETE_FINISH);
					}
					break;
				case MOVE:
					List<String> items_to_be_moved = new Vector<String>();
					if(selectedItemId != null)
					{
						items_to_be_moved.add(selectedItemId);
					}
					//state.setAttribute(STATE_ITEMS_TO_BE_MOVED, items_to_be_moved);
					break;
				case VIEW_METADATA:
					break;
				case REVISE_METADATA:
					state.setAttribute(ResourcesAction.STATE_REVISE_PROPERTIES_ENTITY_ID, selectedItemId);
					state.setAttribute(ResourcesAction.STATE_REVISE_PROPERTIES_ACTION, action);
					state.setAttribute (STATE_MODE, ResourcesAction.MODE_REVISE_METADATA);
					break;
				case CUSTOM_TOOL_ACTION:
					// do nothing
					break;
				case NEW_UPLOAD:
					break;
				case NEW_FOLDER:
					break;
				case CREATE:
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
					//state.setAttribute(STATE_REORDER_FOLDER, selectedItemId);
					//state.setAttribute(STATE_MODE, MODE_REORDER);
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
	 * @param resource
	 * @param newItem
	 * @param state
	 * @return
	 */
	protected static boolean checkItemFilter(ContentResource resource, ListItem newItem, SessionState state) 
	{
		ContentResourceFilter filter = (ContentResourceFilter)state.getAttribute(STATE_ATTACHMENT_FILTER);
	
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

		String id = params.getString("collectionId");
		expandedItems.add(id);

	}	// doExpand_collection

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
//		Map folderSortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
//		if(folderSortMap == null)
//		{
//			folderSortMap = new Hashtable();
//			state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, folderSortMap);
//		}

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();
		String collectionId = params.getString("collectionId");

		SortedSet newSet = new TreeSet();
		Iterator l = expandedItems.iterator();
		while (l.hasNext ())
		{
			// remove the collection id and all of the subcollections
//		    Resource collection = (Resource) l.next();
//			String id = (String) collection.getId();
		    String id = (String) l.next();

			if (id.indexOf (collectionId)==-1)
			{
	//			newSet.put(id,collection);
				newSet.add(id);
			}
//			else
//			{
//				folderSortMap.remove(id);
//			}
		}

		state.setAttribute(STATE_EXPANDED_COLLECTIONS, newSet);

	}	// doCollapse_collection

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
		// state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, new Hashtable());
		state.setAttribute(STATE_EXPAND_ALL_FLAG, Boolean.FALSE.toString());

	}	// doUnexpandall
	
	/**
	 * @param resource
	 * @param state
	 * @return
	 */
	protected boolean checkSelctItemFilter(ContentResource resource, SessionState state) 
	{
		ContentResourceFilter filter = (ContentResourceFilter)state.getAttribute(STATE_ATTACHMENT_FILTER);
		
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

	}	// Inner class AttachItem
	
}	// class FilePickerAction 
