/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.types;

import static org.sakaiproject.content.api.ResourceToolAction.ACCESS_PROPERTIES;
import static org.sakaiproject.content.api.ResourceToolAction.COLLAPSE;
import static org.sakaiproject.content.api.ResourceToolAction.COMPRESS_ZIP_FOLDER;
import static org.sakaiproject.content.api.ResourceToolAction.COPY;
import static org.sakaiproject.content.api.ResourceToolAction.CREATE;
import static org.sakaiproject.content.api.ResourceToolAction.DELETE;
import static org.sakaiproject.content.api.ResourceToolAction.EXPAND;
import static org.sakaiproject.content.api.ResourceToolAction.MAKE_SITE_PAGE;
import static org.sakaiproject.content.api.ResourceToolAction.MOVE;
import static org.sakaiproject.content.api.ResourceToolAction.PASTE_COPIED;
import static org.sakaiproject.content.api.ResourceToolAction.PASTE_MOVED;
import static org.sakaiproject.content.api.ResourceToolAction.PERMISSIONS;
import static org.sakaiproject.content.api.ResourceToolAction.REORDER;
import static org.sakaiproject.content.api.ResourceToolAction.REVISE_METADATA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceAction.Localizer;
import org.sakaiproject.content.util.BaseResourceType;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.content.util.ZipContentUtil;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class FolderType extends BaseResourceType implements ExpandableResourceType 
{
	protected String typeId = ResourceType.TYPE_FOLDER;
	protected String helperId = "sakai.resource.type.helper";
	
	/** localized tool properties **/
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.TypeProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.type.types";
	private static final String RESOURCECLASS = "resource.class.type";
	private static final String RESOURCEBUNDLE = "resource.bundle.type";
	private String resourceClass = ServerConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
	private String resourceBundle = ServerConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
	private ResourceLoader rb = new Resource().getLoader(resourceClass, resourceBundle);
	// private static ResourceLoader rb = new ResourceLoader("types");
	
	protected EnumMap<ActionType, List<ResourceToolAction>> actionMap = new EnumMap<ActionType, List<ResourceToolAction>>(ActionType.class);

	protected Map<String, ResourceToolAction> actions = new HashMap<String, ResourceToolAction>();	
	protected ContentHostingService contentService;
	
	private Localizer localizer(final String string)
	{
		return new Localizer()
		{
			public String getLabel()
			{
				return rb.getString(string);
			}
			
		};
	}
	
	public FolderType()
	{
		actions.put(CREATE, new BaseInteractionAction(CREATE, ActionType.NEW_FOLDER, typeId, helperId, localizer("create.folder") ));
		actions.put(ACCESS_PROPERTIES, new BaseServiceLevelAction(ACCESS_PROPERTIES, ActionType.VIEW_METADATA, typeId, false, localizer("action.access")));
		actions.put(REVISE_METADATA, new BaseServiceLevelAction(REVISE_METADATA, ActionType.REVISE_METADATA, typeId, false, localizer("action.props")));
		actions.put(PASTE_COPIED, new BaseServiceLevelAction(PASTE_COPIED, ActionType.PASTE_COPIED, typeId, false, localizer("action.pastecopy")));
		actions.put(PASTE_MOVED, new BaseServiceLevelAction(PASTE_MOVED, ActionType.PASTE_MOVED, typeId, false, localizer("action.pastemove")));
		actions.put(COPY, new FolderCopyAction(COPY, ActionType.COPY, typeId, true, localizer("action.copy")));
		actions.put(MOVE, new FolderMoveAction(MOVE, ActionType.MOVE, typeId, true, localizer("action.move")));
		actions.put(DELETE, new FolderDeleteAction(DELETE, ActionType.DELETE, typeId, true, localizer("action.delete")));
		actions.put(REORDER, new FolderReorderAction(REORDER, ActionType.REVISE_ORDER, typeId, false, localizer("action.reorder")));
		actions.put(PERMISSIONS, new FolderPermissionsAction(PERMISSIONS, ActionType.REVISE_PERMISSIONS, typeId, "sakai.permissions.helper", localizer("action.permissions")));
		actions.put(EXPAND, new BaseServiceLevelAction(EXPAND, ActionType.EXPAND_FOLDER, typeId, false, localizer("expand.item")));
		actions.put(COLLAPSE, new BaseServiceLevelAction(COLLAPSE, ActionType.COLLAPSE_FOLDER, typeId, false, localizer("collapse.item")));
		actions.put(COMPRESS_ZIP_FOLDER, new FolderCompressAction(COMPRESS_ZIP_FOLDER, ActionType.COMPRESS_ZIP_FOLDER, typeId, false, localizer("action.compresszipfolder")));
		actions.put(MAKE_SITE_PAGE, new MakeSitePageAction(MAKE_SITE_PAGE, ActionType.MAKE_SITE_PAGE, typeId));
		// initialize actionMap with an empty List for each ActionType
		for(ActionType type : ActionType.values())
		{
			actionMap.put(type, new ArrayList<ResourceToolAction>());
		}
		
		// for each action in actions, add a link in actionMap
		Iterator<String> it = actions.keySet().iterator();
		while(it.hasNext())
		{
			String id = it.next();
			ResourceToolAction action = actions.get(id);
			List<ResourceToolAction> list = actionMap.get(action.getActionType());
			if(list == null)
			{
				list = new ArrayList<ResourceToolAction>();
				actionMap.put(action.getActionType(), list);
			}
			list.add(action);
		}
		

	}

	protected ContentHostingService getContentService() {
		if(contentService == null)
		{
			contentService = (ContentHostingService) ComponentManager.get(ContentHostingService.class);
		}
		return contentService;
	}

	public class FolderPermissionsAction extends BaseInteractionAction
	{

		public FolderPermissionsAction(String id, ActionType actionType,
				String typeId, String helperId, Localizer localizer) {
			super(id, actionType, typeId, helperId, localizer);
		}

		public String initializeAction(Reference reference)
		{
			ToolSession toolSession = SessionManager.getCurrentToolSession();

			toolSession.setAttribute(PermissionsHelper.TARGET_REF, reference.getReference());

			// use the folder's context (as a site and as a resource) for roles
			Collection<String> rolesRefs = new ArrayList<String>();
			rolesRefs.add(SiteService.siteReference(reference.getContext()));
			rolesRefs.add(reference.getReference());
			toolSession.setAttribute(PermissionsHelper.ROLES_REF, rolesRefs);

			// ... with this description
			String title = reference.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			String[] args = { title };

			toolSession.setAttribute(PermissionsHelper.DESCRIPTION, rb.getFormattedMessage("title.permissions", args));

			// ... showing only locks that are prpefixed with this
			toolSession.setAttribute(PermissionsHelper.PREFIX, "content.");

			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}

		public boolean available(ContentEntity entity)
		{
			boolean ok = true;
			if(entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId()))
			{
				ok = false;
			}
			else if(entity.getId().startsWith(ContentHostingService.COLLECTION_DROPBOX))
			{
				ok = false;
			}
			else
			{
				ContentCollection parent = entity.getContainingCollection();
				if(parent == null)
				{
					ok = false;
				}
			}
			return ok;
		}

	}

	public class FolderReorderAction extends BaseServiceLevelAction
	{

		public FolderReorderAction(String id, ActionType actionType,
				String typeId, boolean multipleItemAction, Localizer localizer) {
			super(id, actionType, typeId, multipleItemAction, localizer);
		}

		@Override
		public boolean available(ContentEntity entity)
		{
			boolean isAvailable = true;

			if(entity != null && entity instanceof ContentCollection)
			{
				isAvailable = ((ContentCollection) entity).getMemberCount() > 1;
			}
			return isAvailable;
		}

		
	}



	public class FolderCopyAction extends BaseServiceLevelAction
	{
		public FolderCopyAction(String id, ActionType actionType,
				String typeId, boolean multipleItemAction, Localizer localizer)
		{
			super(id, actionType, typeId, multipleItemAction, localizer);
		}
		
		@Override
		public boolean available(ContentEntity entity)
		{
			boolean ok = true;
			if(entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId()))
			{
				ok = false;
			}
			else
			{
				ContentCollection parent = entity.getContainingCollection();
				if(parent == null)
				{
					ok = false;
				}
			}
			return ok;
		}

	}

	
	public class FolderDeleteAction extends BaseServiceLevelAction
	{
		public FolderDeleteAction(String id, ActionType actionType,
				String typeId, boolean multipleItemAction, Localizer localizer) {
			super(id, actionType, typeId, multipleItemAction, localizer);
		}

		public boolean available(ContentEntity entity)
		{
			boolean ok = true;
			if(entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId()))
			{
				ok = false;
			}
			else
			{
				ContentCollection parent = entity.getContainingCollection();
				if(parent == null)
				{
					ok = false;
				}
				else
				{
					ContentCollection grandparent = parent.getContainingCollection();
					if(grandparent != null && ContentHostingService.COLLECTION_DROPBOX.equals(grandparent.getId()))
					{
						Reference ref = EntityManager.newReference(entity.getReference());
						if(ref != null)
						{
							String siteId = ref.getContext();
							if(siteId != null)
							{
								String dropboxId = getContentService().getDropboxCollection(siteId);
								if(entity.getId().equals(dropboxId))
								{
									ok = false;
								}
							}
						}
					}
				}
			}
			return ok;
		}

	}

	public class FolderMoveAction extends BaseServiceLevelAction
	{

		public FolderMoveAction(String id, ActionType actionType,
				String typeId, boolean multipleItemAction, Localizer localizer) {
			super(id, actionType, typeId, multipleItemAction, localizer);
		}

		@Override
		public boolean available(ContentEntity entity)
		{
			boolean ok = true;
			if(entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId()))
			{
				ok = false;
			}
			else if(entity.getId().startsWith(ContentHostingService.COLLECTION_DROPBOX))
			{
				ok = false;
			}
			else
			{
				ContentCollection parent = entity.getContainingCollection();
				if(parent == null)
				{
					ok = false;
				}
			}
			return ok;
		}
	}
	
	public class FolderCompressAction extends BaseServiceLevelAction {
				
		public FolderCompressAction(String id, ActionType actionType,
				String typeId, boolean multipleItemAction, Localizer localizer) {
			super(id, actionType, typeId, multipleItemAction, localizer);
		}

		private ZipContentUtil zipUtil = new ZipContentUtil();

		public void initializeAction(Reference reference) {
			try {
				zipUtil.compressFolder(reference);
			} catch (Exception e) {
				log.warn(e.getMessage());
			}			
		}
		
		public boolean available(ContentEntity entity) {
			return ServerConfigurationService.getBoolean(ContentHostingService.RESOURCES_ZIP_ENABLE, true)
					|| ServerConfigurationService.getBoolean(ContentHostingService.RESOURCES_ZIP_ENABLE_COMPRESS, true);
		}
	}
	
	public ResourceToolAction getAction(String actionId) 
	{
		return (ResourceToolAction) actions.get(actionId);
	}

	public String getIconLocation(ContentEntity entity, boolean expanded)
    {
		String iconLocation = "sakai/dir_openroot.gif";
		if(entity.isCollection())
		{
			ContentCollection collection = (ContentCollection) entity;
			int memberCount = collection.getMemberCount();
			if(memberCount == 0)
			{
				iconLocation = "sakai/dir_closed.gif";
			}
			else if(memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT)
			{
				iconLocation = "sakai/dir_unexpand.gif";
			}
			else if(expanded) 
			{
				iconLocation = "sakai/dir_openminus.gif";
			}
			else 
			{
				iconLocation = "sakai/dir_closedplus.gif";
			}
		}
		return iconLocation;
    }
	
	public String getIconLocation(ContentEntity entity) 
	{
		String iconLocation = "sakai/dir_openroot.gif";
		if(entity != null && entity.isCollection())
		{
			ContentCollection collection = (ContentCollection) entity;
			int memberCount = collection.getMemberCount();
			if(memberCount == 0)
			{
				iconLocation = "sakai/dir_closed.gif";
			}
			else if(memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT)
			{
				iconLocation = "sakai/dir_unexpand.gif";
			}
		}
		return iconLocation;
	}
	
	public String getIconClass(ContentEntity entity, boolean expanded)
	{
		String iconClass = "fa fa-folder-open-o";
		if(entity.isCollection())
		{
			ContentCollection collection = (ContentCollection) entity;
			int memberCount = collection.getMemberCount();
			if(memberCount == 0)
			{
				iconClass = "fa fa-folder-o";
			}
			else if(memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT)
			{
				iconClass = "fa fa-folder";
			}
			else if(expanded) 
			{
				iconClass = "fa fa-folder-open";
			}
			else 
			{
				iconClass = "fa fa-folder";
			}
		}
		return iconClass;
	}
	
	public String getIconClass(ContentEntity entity) 
	{
		String iconClass = "fa fa-folder-open-o";
		if(entity != null && entity.isCollection())
		{
			ContentCollection collection = (ContentCollection) entity;
			int memberCount = collection.getMemberCount();
			if(memberCount == 0)
			{
				iconClass = "fa fa-folder-o";
			}
			else if(memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT)
			{
				iconClass = "fa fa-folder";
			}
		}
		return iconClass;
	}
	
	public String getId() 
	{
		return typeId;
	}

	public String getLabel() 
	{
		return rb.getString("type.folder");
	}
	
	public String getLocalizedHoverText(ContentEntity entity, boolean expanded)
    {
		String hoverText = rb.getString("type.folder");
		if(entity.isCollection())
		{
			ContentCollection collection = (ContentCollection) entity;
			int memberCount = collection.getMemberCount();
			if(memberCount == 0)
			{
				hoverText = rb.getString("type.folder");
			}
			else if(memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT)
			{
				hoverText = rb.getString("list.toobig");
			}
			else if(expanded) 
			{
				hoverText = rb.getString("sh.close");
			}
			else 
			{
				hoverText = rb.getString("sh.open");
			}
		}
		return hoverText;
    }
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getLocalizedHoverText(org.sakaiproject.entity.api.Reference)
	 */
	public String getLocalizedHoverText(ContentEntity member)
	{
		return rb.getString("type.folder");
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getActions(org.sakaiproject.content.api.ResourceType.ActionType)
	 */
	public List<ResourceToolAction> getActions(ActionType type)
	{
		List<ResourceToolAction> list = actionMap.get(type);
		if(list == null)
		{
			list = new ArrayList<ResourceToolAction>();
			actionMap.put(type, list);
		}
		return new ArrayList<ResourceToolAction>(list);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getActions(java.util.List)
	 */
	public List<ResourceToolAction> getActions(List<ActionType> types)
	{
		List<ResourceToolAction> list = new ArrayList<ResourceToolAction>();
		if(types != null)
		{
			Iterator<ActionType> it = types.iterator();
			while(it.hasNext())
			{
				ActionType type = it.next();
				List<ResourceToolAction> sublist = actionMap.get(type);
				if(sublist == null)
				{
					sublist = new ArrayList<ResourceToolAction>();
					actionMap.put(type, sublist);
				}
				list.addAll(sublist);
			}
		}
		return list;
	}
	
	public boolean hasRightsDialog() 
	{
		return false;
	}
	
	public ServiceLevelAction getCollapseAction()
    {
	    return (ServiceLevelAction) this.actions.get(COLLAPSE);
    }

	public ServiceLevelAction getExpandAction()
    {
	    return (ServiceLevelAction) this.actions.get(EXPAND);
    }

	public boolean allowAddAction(ResourceToolAction action, ContentEntity entity)
    {
	    // allow all add actions in regular folders
	    return true;
    }

}
