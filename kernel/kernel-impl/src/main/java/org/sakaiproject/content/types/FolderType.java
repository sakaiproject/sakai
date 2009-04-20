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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.types;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.impl.util.ZipContentUtil;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceType;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

public class FolderType extends BaseResourceType implements ExpandableResourceType 
{
	protected String typeId = ResourceType.TYPE_FOLDER;
	protected String helperId = "sakai.resource.type.helper";
	private static final String RESOURCES_ZIP_ENABLE = "resources.zip.enable"; //sakai.properties hack
	
	/** localized tool properties **/
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.TypeProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.type.types";
	private static final String RESOURCECLASS = "resource.class.type";
	private static final String RESOURCEBUNDLE = "resource.bundle.type";
	private String resourceClass = ServerConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
	private String resourceBundle = ServerConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
	private ResourceLoader rb = new Resource().getLoader(resourceClass, resourceBundle);
	// private static ResourceLoader rb = new ResourceLoader("types");
	
	protected EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>> actionMap = new EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>>(ResourceToolAction.ActionType.class);

	protected Map<String, ResourceToolAction> actions = new HashMap<String, ResourceToolAction>();	
	protected UserDirectoryService userDirectoryService;
	protected ContentHostingService contentService;
	
	public FolderType()
	{
		this.userDirectoryService = (UserDirectoryService) ComponentManager.get("org.sakaiproject.user.api.UserDirectoryService");
		
		//actions.put(ResourceToolAction.PASTE_MOVED, new FolderPasteMovedAction());
		//actions.put(ResourceToolAction.PASTE_COPIED, new FolderPasteCopiedAction());
		actions.put(ResourceToolAction.CREATE, new FolderCreateAction());
		actions.put(ResourceToolAction.ACCESS_PROPERTIES, new FolderViewPropertiesAction());
		actions.put(ResourceToolAction.REVISE_METADATA, new FolderPropertiesAction());
		//actions.put(ResourceToolAction.DUPLICATE, new FolderDuplicateAction());
		actions.put(ResourceToolAction.PASTE_COPIED, new FolderPasteCopyAction());
		actions.put(ResourceToolAction.PASTE_MOVED, new FolderPasteMoveAction());
		actions.put(ResourceToolAction.COPY, new FolderCopyAction());
		actions.put(ResourceToolAction.MOVE, new FolderMoveAction());
		actions.put(ResourceToolAction.DELETE, new FolderDeleteAction());
		actions.put(ResourceToolAction.REORDER, new FolderReorderAction());
		actions.put(ResourceToolAction.PERMISSIONS, new FolderPermissionsAction());
		actions.put(ResourceToolAction.EXPAND, new FolderExpandAction());
		actions.put(ResourceToolAction.COLLAPSE, new FolderCollapseAction());
		
		// [WARN] Archive file handling compress/decompress feature contains bugs; exclude action item.
		// Disable property setting masking problematic code per will of the Community.
		// See Jira KNL-155/SAK-800 for more details.
		/*
		if (ServerConfigurationService.getBoolean(RESOURCES_ZIP_ENABLE,false)) {
			actions.put(ResourceToolAction.COMPRESS_ZIP_FOLDER, new FolderCompressAction());
		}
		*/
		
		// initialize actionMap with an empty List for each ActionType
		for(ResourceToolAction.ActionType type : ResourceToolAction.ActionType.values())
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
	
	public class FolderPasteCopyAction implements ServiceLevelAction
	{

		public void cancelAction(Reference reference)
        {
	        // no activity required
        }

		public void finalizeAction(Reference reference)
        {
	        // no activity required
        }

		public void initializeAction(Reference reference)
        {
	        // no activity required
        }

		public boolean isMultipleItemAction()
        {
	        return false;
        }

		public boolean available(ContentEntity entity)
        {
	        return true;
        }

		public ActionType getActionType()
        {
	        return ActionType.PASTE_COPIED;
        }

		public String getId()
        {
	        return ResourceToolAction.PASTE_COPIED;
        }

		public String getLabel()
        {
	        return rb.getString("action.pastecopy");
        }

		public String getTypeId()
        {
	        return typeId;
        }
		
	}

	public class FolderPasteMoveAction implements ServiceLevelAction
	{

		public void cancelAction(Reference reference)
        {
	        // no activity required
        }

		public void finalizeAction(Reference reference)
        {
	        // no activity required
        }

		public void initializeAction(Reference reference)
        {
	        // no activity required
        }

		public boolean isMultipleItemAction()
        {
	        return false;
        }

		public boolean available(ContentEntity entity)
        {
	        return true;
        }

		public ActionType getActionType()
        {
	        return ActionType.PASTE_MOVED;
        }

		public String getId()
        {
	        return ResourceToolAction.PASTE_MOVED;
        }

		public String getLabel()
        {
	        return rb.getString("action.pastemove");
        }

		public String getTypeId()
        {
	        return typeId;
        }
		
	}

	public class FolderPermissionsAction implements InteractionAction
	{

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.InteractionAction#cancelAction(org.sakaiproject.entity.api.Reference, java.lang.String)
         */
        public void cancelAction(Reference reference, String initializationId)
        {
	        // nothing to do
	        
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.InteractionAction#finalizeAction(org.sakaiproject.entity.api.Reference, java.lang.String)
         */
        public void finalizeAction(Reference reference, String initializationId)
        {
	        // nothing to do
	        
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.InteractionAction#getHelperId()
         */
        public String getHelperId()
        {
	        return "sakai.permissions.helper";
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.InteractionAction#getRequiredPropertyKeys()
         */
        public List getRequiredPropertyKeys()
        {
	        // TODO Auto-generated method stub
	        return null;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.InteractionAction#initializeAction(org.sakaiproject.entity.api.Reference)
         */
        public String initializeAction(Reference reference)
        {
    		ToolSession toolSession = SessionManager.getCurrentToolSession();

    		toolSession.setAttribute(PermissionsHelper.TARGET_REF, reference.getReference());

    		// use the folder's context (as a site) for roles
    		String siteRef = SiteService.siteReference(reference.getContext());
    		toolSession.setAttribute(PermissionsHelper.ROLES_REF, siteRef);

    		// ... with this description
    		String title = reference.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
    		String[] args = { title };
 
    		toolSession.setAttribute(PermissionsHelper.DESCRIPTION, rb.getFormattedMessage("title.permissions", args));

    		// ... showing only locks that are prpefixed with this
    		toolSession.setAttribute(PermissionsHelper.PREFIX, "content.");

 	        return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
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
    			if(parent == null || ContentHostingService.ROOT_COLLECTIONS.contains(parent.getId()))
    			{
    				ok = false;
    			}
    		}
 	        return ok;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
         */
        public ActionType getActionType()
        {
	        // TODO Auto-generated method stub
	        return ActionType.REVISE_PERMISSIONS;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#getId()
         */
        public String getId()
        {
	        // TODO Auto-generated method stub
	        return ResourceToolAction.PERMISSIONS;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#getLabel()
         */
        public String getLabel()
        {
	        // TODO Auto-generated method stub
	        return rb.getString("action.permissions");
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#getTypeId()
         */
        public String getTypeId()
        {
	        // TODO Auto-generated method stub
	        return typeId;
        }

		
	}

	public class FolderReorderAction implements ServiceLevelAction
	{

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
         */
        public void cancelAction(Reference reference)
        {
	        // nothing to do
	        
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
         */
        public void finalizeAction(Reference reference)
        {
	        // nothing to do
	        
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
         */
        public void initializeAction(Reference reference)
        {
	        // nothing to do
	        
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ServiceLevelAction#isMultipleItemAction()
         */
        public boolean isMultipleItemAction()
        {
	        return false;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
        	boolean isAvailable = true;
        	
        	if(entity != null && entity instanceof ContentCollection)
        	{
        		isAvailable = ((ContentCollection) entity).getMemberCount() > 1;
        	}
	        return isAvailable;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
         */
        public ActionType getActionType()
        {
	        return ActionType.REVISE_ORDER;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#getId()
         */
        public String getId()
        {
	        return ResourceToolAction.REORDER;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#getLabel()
         */
        public String getLabel()
        {
	        return rb.getString("action.reorder");
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#getTypeId()
         */
        public String getTypeId()
        {
	        return typeId;
        }
		
	}

	public class FolderPasteMovedAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
        
		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.PASTE_MOVED;
		}


		public String getId() 
		{
			return ResourceToolAction.PASTE_MOVED;
		}

		public String getLabel() 
		{
			return rb.getString("action.paste");
		}

		public boolean isMultipleItemAction() 
		{
			return false;
		}

		public String getTypeId() 
		{
			return typeId;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// do nothing
			
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// do nothing
			
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// do nothing
			
		}

	}

	public class FolderPasteCopiedAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
        
		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.PASTE_COPIED;
		}


		public String getId() 
		{
			return ResourceToolAction.PASTE_COPIED;
		}

		public String getLabel() 
		{
			return rb.getString("action.paste");
		}

		public boolean isMultipleItemAction() 
		{
			return false;
		}

		public String getTypeId() 
		{
			return typeId;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// do nothing
			
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// do nothing
			
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// do nothing
			
		}

	}

	public class FolderCopyAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
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
    			if(parent == null || ContentHostingService.ROOT_COLLECTIONS.contains(parent.getId()))
    			{
    				ok = false;
    			}
    		}
 	        return ok;
        }
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.COPY;
		}


		public String getId() 
		{
			return ResourceToolAction.COPY;
		}

		public String getLabel() 
		{
			return rb.getString("action.copy");
		}

		public boolean isMultipleItemAction() 
		{
			return true;
		}

		public String getTypeId() 
		{
			return typeId;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// do nothing
			
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// do nothing
			
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// do nothing
			
		}

	}

	public class FolderCreateAction implements InteractionAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }

		public void cancelAction(Reference reference, String initializationId) 
		{
			// do nothing
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.NEW_FOLDER;
		}

		public String getId() 
		{
			return ResourceToolAction.CREATE;
		}

		public String getLabel() 
		{
			return rb.getString("create.folder"); 
		}

		public String getTypeId() 
		{
			return typeId;
		}

		public String getHelperId() 
		{
			return helperId;
		}

		public List getRequiredPropertyKeys() 
		{
			return null;
		}

		public String initializeAction(Reference reference) 
		{
			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}

	}

	public class FolderDeleteAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
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
    			if(parent == null || ContentHostingService.ROOT_COLLECTIONS.contains(parent.getId()))
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
	        					if(contentService == null)
	        					{
	        						contentService = (ContentHostingService) ComponentManager.get(ContentHostingService.class);
	        					}
	    						String dropboxId = contentService.getDropboxCollection(siteId);
	    						if(entity.getId().equals(dropboxId))
	    						{
	    							ok = false;
	    						}
	    					}
    					}
    				}
    			}
    		}
// remove member count as condition for deletion - SAK-11790        	
//        	if(ok && entity instanceof ContentCollection)
//        	{
//        		ok = (((ContentCollection) entity).getMemberCount() == 0);
//        	}
	        return ok;
        }
        
		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.DELETE;
		}

		public String getId() 
		{
			return ResourceToolAction.DELETE;
		}

		public String getLabel() 
		{
			return rb.getString("action.delete"); 
		}

		public boolean isMultipleItemAction() 
		{
			return true;
		}
		
		public String getTypeId() 
		{
			return typeId;
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// do nothing
			
		}

	}

	public class FolderDuplicateAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
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
    			if(parent == null || ContentHostingService.ROOT_COLLECTIONS.contains(parent.getId()))
    			{
    				ok = false;
    			}
    		}
 	        return ok;
        }
        
		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.DUPLICATE;
		}

		public String getId() 
		{
			return ResourceToolAction.DUPLICATE;
		}

		public String getLabel() 
		{
			return rb.getString("action.duplicate"); 
		}

		public boolean isMultipleItemAction() 
		{
			return false;
		}
		
		public String getTypeId() 
		{
			return typeId;
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// do nothing
			
		}

	}
	
	public class FolderPropertiesAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
           	boolean ok = true;
//        	if(entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId()))
//    		{
//    			ok = false;
//    		}
//        	else if(entity.getId().startsWith(ContentHostingService.COLLECTION_DROPBOX))
//        	{
//        		ok = false;
//        	}
//        	else
//    		{
//    			ContentCollection parent = entity.getContainingCollection();
//    			if(parent == null || ContentHostingService.ROOT_COLLECTIONS.contains(parent.getId()))
//    			{
//    				ok = false;
//    			}
//    		}
 	        return ok;
        }

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#isMultipleItemAction()
		 */
		public boolean isMultipleItemAction()
		{
			return false;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.REVISE_METADATA;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getId()
		 */
		public String getId()
		{
			return ResourceToolAction.REVISE_METADATA;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getLabel()
		 */
		public String getLabel()
		{
			return rb.getString("action.props");
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getTypeId()
		 */
		public String getTypeId()
		{
			return typeId;
		}
		
	}

	public class FolderViewPropertiesAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
 	        return true;
        }

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#isMultipleItemAction()
		 */
		public boolean isMultipleItemAction()
		{
			return false;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.VIEW_METADATA;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getId()
		 */
		public String getId()
		{
			return ResourceToolAction.ACCESS_PROPERTIES;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getLabel()
		 */
		public String getLabel()
		{
			return rb.getString("action.access");
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getTypeId()
		 */
		public String getTypeId()
		{
			return typeId;
		}
		
	}

	public class FolderMoveAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
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
    			if(parent == null || ContentHostingService.ROOT_COLLECTIONS.contains(parent.getId()))
    			{
    				ok = false;
    			}
    		}
 	        return ok;
        }
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.MOVE;
		}

		public String getId() 
		{
			return ResourceToolAction.MOVE;
		}

		public String getLabel() 
		{
			return rb.getString("action.move"); 
		}

		public boolean isMultipleItemAction() 
		{
			return true;
		}
		
		public String getTypeId() 
		{
			return typeId;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// do nothing
			
		}

	}

	public class FolderReviseAction implements InteractionAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }

		public void cancelAction(Reference reference, String initializationId) 
		{
			// do nothing
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.REPLACE_CONTENT;
		}

		public String getId() 
		{
			return ResourceToolAction.REVISE_CONTENT;
		}

		public String getLabel() 
		{
			return rb.getString("action.revise"); 
		}
		
		public String getTypeId() 
		{
			return typeId;
		}

		public String getHelperId() 
		{
			return helperId;
		}

		public List getRequiredPropertyKeys() 
		{
			return null;
		}

		public String initializeAction(Reference reference) 
		{
			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}

	}
	
	public class FolderAccessAction implements InteractionAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }

		public String initializeAction(Reference reference) 
		{
			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}

		public void cancelAction(Reference reference, String initializationId) 
		{
			// do nothing
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.VIEW_CONTENT;
		}

		public String getId() 
		{
			return ResourceToolAction.ACCESS_CONTENT;
		}

		public String getLabel() 
		{
			return rb.getString("action.access"); 
		}
		
		public String getTypeId() 
		{
			return typeId;
		}

		public String getHelperId() 
		{
			return helperId;
		}

		public List getRequiredPropertyKeys() 
		{
			return null;
		}

	}
	
	public class FolderExpandAction implements ServiceLevelAction
	{

		public void cancelAction(Reference reference)
        {
	        
        }

		public void finalizeAction(Reference reference)
        {
	        
        }

		public void initializeAction(Reference reference)
        {
	        
        }

		public boolean isMultipleItemAction()
        {
	        return false;
        }

		public boolean available(ContentEntity entity)
        {
			boolean isAvailable = (entity != null);
			if(isAvailable && entity instanceof ContentCollection)
			{
				ContentCollection collection = (ContentCollection) entity;
				int memberCount = collection.getMemberCount();
				isAvailable = (memberCount > 0) && (memberCount < ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT);
			}
	        return isAvailable;
        }

		public ActionType getActionType()
        {
	        return ActionType.EXPAND_FOLDER;
        }

		public String getId()
        {
	        return ResourceToolAction.EXPAND;
        }

		public String getLabel()
        {
	        return rb.getString("expand.item");
        }

		public String getTypeId()
        {
	        return typeId;
        }
		
	}
	
	public class FolderCollapseAction implements ServiceLevelAction
	{

		public void cancelAction(Reference reference)
        {
	        
        }

		public void finalizeAction(Reference reference)
        {
	        
        }

		public void initializeAction(Reference reference)
        {
	        
        }

		public boolean isMultipleItemAction()
        {
	        return false;
        }

		public boolean available(ContentEntity entity)
        {
	        return true;
        }

		public ActionType getActionType()
        {
	        return ActionType.COLLAPSE_FOLDER;
        }

		public String getId()
        {
	        return ResourceToolAction.COLLAPSE;
        }

		public String getLabel()
        {
	        return rb.getString("collapse.item");
        }

		public String getTypeId()
        {
	        return typeId;
        }
		
	}
	
	public class FolderCompressAction implements ServiceLevelAction {
				
		private ZipContentUtil zipUtil = new ZipContentUtil();
				
		public void cancelAction(Reference reference) {
			// TODO Auto-generated method stub
		}
		
		public void finalizeAction(Reference reference) {
			// TODO Auto-generated method stub		
		}
		
		public void initializeAction(Reference reference) {
			try {
				zipUtil.compressFolder(reference);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}			
		}
		
		public boolean isMultipleItemAction() {
			// TODO Auto-generated method stub
			return false;
		}
		
		public boolean available(ContentEntity entity) {
			return true;
		}
		
		public ActionType getActionType() {
			return ResourceToolAction.ActionType.COMPRESS_ZIP_FOLDER;
		}
		
		public String getId() {
			return ResourceToolAction.COMPRESS_ZIP_FOLDER;
		}
		
		public String getLabel() {
			return rb.getString("action.compresszipfolder"); 
		}
		
		public String getTypeId() {
			return typeId;
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
	    return (ServiceLevelAction) this.actions.get(ResourceToolAction.COLLAPSE);
    }

	public ServiceLevelAction getExpandAction()
    {
	    return (ServiceLevelAction) this.actions.get(ResourceToolAction.EXPAND);
    }

	public boolean allowAddAction(ResourceToolAction action, ContentEntity entity)
    {
	    // allow all add actions in regular folders
	    return true;
    }

}
