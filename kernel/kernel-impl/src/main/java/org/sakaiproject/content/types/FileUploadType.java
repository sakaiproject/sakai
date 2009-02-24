/**********************************************************************************
 * $URL:  $
 * $Id:  $
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

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.content.impl.util.ZipContentUtil;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceType;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

public class FileUploadType extends BaseResourceType 
{
	protected String typeId = ResourceType.TYPE_UPLOAD;
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
	
	protected EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>> actionMap = new EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>>(ResourceToolAction.ActionType.class);

	protected Map<String, ResourceToolAction> actions = new HashMap<String, ResourceToolAction>();	
	protected UserDirectoryService userDirectoryService;
	
	public FileUploadType()
	{
		this.userDirectoryService = (UserDirectoryService) ComponentManager.get("org.sakaiproject.user.api.UserDirectoryService");
		
		actions.put(ResourceToolAction.CREATE, new FileUploadCreateAction());
		//actions.put(ResourceToolAction.ACCESS_CONTENT, new FileUploadAccessAction());
		actions.put(ResourceToolAction.REVISE_CONTENT, new FileUploadReviseAction());
		actions.put(ResourceToolAction.REPLACE_CONTENT, new FileUploadReplaceAction());
		actions.put(ResourceToolAction.ACCESS_PROPERTIES, new FileUploadViewPropertiesAction());
		actions.put(ResourceToolAction.REVISE_METADATA, new FileUploadPropertiesAction());
		actions.put(ResourceToolAction.DUPLICATE, new FileUploadDuplicateAction());
		actions.put(ResourceToolAction.COPY, new FileUploadCopyAction());
		actions.put(ResourceToolAction.MOVE, new FileUploadMoveAction());
		actions.put(ResourceToolAction.DELETE, new FileUploadDeleteAction());
		actions.put(ResourceToolAction.EXPAND_ZIP_ARCHIVE, new FileUploadExpandAction());
		
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

	public class FileUploadPropertiesAction implements ServiceLevelAction
	{

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			
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

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
		
	}
	
	public class FileUploadViewPropertiesAction implements ServiceLevelAction
	{

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			
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

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
		
	}
	
	public class FileUploadCopyAction implements ServiceLevelAction
	{
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
		 * @see org.sakaiproject.content.api.ResourceToolAction#getPermission()
		 */
		public Set getPermission()
		{
			Set rv = new TreeSet();
			rv.add(ContentHostingService.AUTH_RESOURCE_READ);
			rv.add(ContentHostingService.AUTH_RESOURCE_ALL_GROUPS);
			rv.add(ContentHostingService.AUTH_RESOURCE_HIDDEN);
			return rv;
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
	}

	public class FileUploadCreateAction implements InteractionAction
	{

		public void cancelAction(Reference reference, String initializationId) 
		{
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.NEW_UPLOAD;
		}

		public String getId() 
		{
			return ResourceToolAction.CREATE;
		}

		public String getLabel() 
		{
			return rb.getString("create.uploads"); 
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
			List<String> rv = new ArrayList<String>();
			rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
			return rv;
		}

		public String initializeAction(Reference reference) 
		{
			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
	}
	
	public class FileUploadDeleteAction implements ServiceLevelAction
	{
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
		 * @see org.sakaiproject.content.api.ResourceToolAction#getPermission()
		 */
		public Set getPermission()
		{
			Set rv = new TreeSet();
			rv.add(ContentHostingService.AUTH_RESOURCE_REMOVE_ANY);
			rv.add(ContentHostingService.AUTH_RESOURCE_REMOVE_OWN);
			rv.add(ContentHostingService.AUTH_RESOURCE_ALL_GROUPS);
			rv.add(ContentHostingService.AUTH_RESOURCE_HIDDEN);
			return rv;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
	}

	public class FileUploadDuplicateAction implements ServiceLevelAction
	{
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
		 * @see org.sakaiproject.content.api.ResourceToolAction#getPermission()
		 */
		public Set getPermission()
		{
			Set rv = new TreeSet();
			rv.add(ContentHostingService.AUTH_RESOURCE_ADD);
			rv.add(ContentHostingService.AUTH_RESOURCE_ALL_GROUPS);
			rv.add(ContentHostingService.AUTH_RESOURCE_HIDDEN);
			return rv;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
	}

	public class FileUploadMoveAction implements ServiceLevelAction
	{
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
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			
		}

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
	}

	public class FileUploadReviseAction implements InteractionAction
	{

		public void cancelAction(Reference reference, String initializationId) 
		{
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.REVISE_CONTENT;
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
			List<String> rv = new ArrayList<String>();
			rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
			return rv;
		}

		public String initializeAction(Reference reference) 
		{
			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
			boolean available = false;
			if(entity instanceof ContentResource)
			{
				String mimetype = ((ContentResource) entity).getContentType();
				if(mimetype == null)
				{
					mimetype = entity.getProperties().getProperty(ResourceProperties.PROP_CONTENT_TYPE);
				}
				available = (mimetype != null) && (ResourceType.MIME_TYPE_HTML.equals(mimetype) || ResourceType.MIME_TYPE_TEXT.equals(mimetype));
			}
	        return available;
        }
	}
	
	public class FileUploadAccessAction implements InteractionAction
	{
		public String initializeAction(Reference reference) 
		{
			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}

		public void cancelAction(Reference reference, String initializationId) 
		{
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			
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

		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
	}
		
	public class FileUploadExpandAction implements ServiceLevelAction {
						
		private ZipContentUtil extractZipArchive = new ZipContentUtil();
			
		public void cancelAction(Reference reference) {
			// TODO Auto-generated method stub
			
		}
		
		public void finalizeAction(Reference reference) {
			// TODO Auto-generated method stub
			
		}
		
		public void initializeAction(Reference reference) {			
			try {
				extractZipArchive.extractArchive(reference);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		public boolean isMultipleItemAction() {
			// TODO Auto-generated method stub
			return false;
		}
		
		public boolean available(ContentEntity entity) {
			return entity.getId().toLowerCase().endsWith(".zip");
		}
		
		public ActionType getActionType() {
			return ResourceToolAction.ActionType.EXPAND_ZIP_ARCHIVE;
		}
		
		public String getId() {
			return ResourceToolAction.EXPAND_ZIP_ARCHIVE;
		}
		
		public String getLabel() {
			return rb.getString("action.expandziparchive"); 
		}
		
		public String getTypeId() {
			return typeId;
		}
			
	}
	
	public ResourceToolAction getAction(String actionId) 
	{
		return (ResourceToolAction) actions.get(actionId);
	}

	public List getActions(Reference entityRef, Set permissions) 
	{
		// TODO: use entityRef to filter actions
		List rv = new ArrayList();
		rv.addAll(actions.values());
		return rv;
	}

	public List getActions(Reference entityRef, User user, Set permissions) 
	{
		// TODO: use entityRef and user to filter actions
		List rv = new ArrayList();
		rv.addAll(actions.values());
		return rv;
	}

	public String getIconLocation(ContentEntity entity) 
	{
		String iconLocation = null;
		if(entity != null && entity instanceof ContentResource)
		{
			String mimetype = ((ContentResource) entity).getContentType();
			if(mimetype != null && ! "".equals(mimetype.trim()))
			{
				iconLocation = ContentTypeImageService.getContentTypeImage(mimetype);
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
		return rb.getString("type.upload");
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getLocalizedHoverText(org.sakaiproject.entity.api.Reference)
	 */
	public String getLocalizedHoverText(ContentEntity entity)
	{
		String hoverText = rb.getString("type.upload");
		if(entity != null && entity instanceof ContentResource)
		{
			String mimetype = ((ContentResource) entity).getContentType();
			if(mimetype != null && ! "".equals(mimetype.trim()))
			{
				hoverText = ContentTypeImageService.getContentTypeDisplayName(mimetype);
			}
		}
		return hoverText;
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

	public class FileUploadReplaceAction implements InteractionAction 
	{

		public boolean available(ContentEntity entity) 
		{
			return true;
		}

		public ActionType getActionType() 
		{
			return ResourceToolAction.ActionType.REPLACE_CONTENT;
		}

		public String getId() 
		{
			return ResourceToolAction.REPLACE_CONTENT;
		}

		public String getLabel() 
		{
			return rb.getString("action.replace"); 
		}

		public String getTypeId() 
		{
			return typeId;
		}

		public void cancelAction(Reference reference, String initializationId) 
		{
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			
		}

		public String getHelperId() 
		{
			return helperId;
		}

		public List getRequiredPropertyKeys() 
		{
			List<String> rv = new ArrayList<String>();
			rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
			return rv;
		}

		public String initializeAction(Reference reference) 
		{
			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}
	}

}
