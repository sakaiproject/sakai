/**********************************************************************************
 * $URL:$
 * $Id:$
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

import static org.sakaiproject.content.api.ResourceToolAction.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ContentPrintService;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.content.util.ZipContentUtil;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceType;
import org.sakaiproject.content.util.BaseResourceAction.Localizer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
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
	
	private Localizer localizer(final String string) {
		return new Localizer() {

			public String getLabel() {
				return rb.getString(string);
			}
			
		};
	}
	
	protected EnumMap<ActionType, List<ResourceToolAction>> actionMap = new EnumMap<ActionType, List<ResourceToolAction>>(ActionType.class);

	protected Map<String, ResourceToolAction> actions = new HashMap<String, ResourceToolAction>();	
	protected UserDirectoryService userDirectoryService;
	protected ContentTypeImageService contentTypeImageService;
	protected ContentHostingService contentHostingService;
	protected ContentPrintService contentPrintService;
	
	public FileUploadType()
	{
		this.userDirectoryService = (UserDirectoryService) ComponentManager.get("org.sakaiproject.user.api.UserDirectoryService");
		this.contentTypeImageService = (ContentTypeImageService) ComponentManager.get("org.sakaiproject.content.api.ContentTypeImageService");
		this.contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		this.contentPrintService = (ContentPrintService) ComponentManager.get("org.sakaiproject.content.api.ContentPrintService");
				
		BaseInteractionAction createAction = new BaseInteractionAction(CREATE, ActionType.NEW_UPLOAD, typeId, helperId, localizer("create.uploads"));
		createAction.setRequiredPropertyKeys(Collections.singletonList(ResourceProperties.PROP_CONTENT_ENCODING));
		actions.put(CREATE, createAction);
		actions.put(REVISE_CONTENT, new FileUploadReviseAction(REVISE_CONTENT, ActionType.REVISE_CONTENT, typeId, helperId, localizer("action.revise")));
		actions.put(REPLACE_CONTENT, new BaseInteractionAction(REPLACE_CONTENT, ActionType.REPLACE_CONTENT, typeId, helperId, localizer("action.replace")));
		actions.put(ACCESS_PROPERTIES, new BaseServiceLevelAction(ACCESS_PROPERTIES, ActionType.VIEW_METADATA, typeId, false, localizer("action.access")));
		actions.put(REVISE_METADATA, new BaseServiceLevelAction(REVISE_METADATA, ActionType.REVISE_METADATA, typeId, false, localizer("action.props")));
		actions.put(DUPLICATE, new BaseServiceLevelAction(DUPLICATE, ActionType.DUPLICATE, typeId, false, localizer("action.duplicate")));
		actions.put(COPY, new BaseServiceLevelAction(COPY, ActionType.COPY, typeId, true, localizer("action.copy")));
		actions.put(MOVE, new BaseServiceLevelAction(MOVE, ActionType.MOVE, typeId, true, localizer("action.move")));
		actions.put(DELETE, new BaseServiceLevelAction(DELETE, ActionType.DELETE, typeId, true, localizer("action.delete")));
		
		actions.put(EXPAND_ZIP_ARCHIVE, new FileUploadExpandAction(EXPAND_ZIP_ARCHIVE, ActionType.EXPAND_ZIP_ARCHIVE, typeId, false, localizer("action.expandziparchive")));
		actions.put(MAKE_SITE_PAGE, new MakeSitePageAction(MAKE_SITE_PAGE, ActionType.MAKE_SITE_PAGE, typeId));
		if (ServerConfigurationService.getString(contentPrintService.CONTENT_PRINT_SERVICE_URL, null) != null)
		{
			// print service url is provided. Add the Print option.
			actions.put(PRINT_FILE, new BaseServiceLevelAction(PRINT_FILE, ActionType.PRINT_FILE, typeId, false, localizer("action.printfile")));
		}
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

	/**
	 * Cless for handling the Revising of HTML and TXT files through the web interface.
	 *
	 */
	private class FileUploadReviseAction extends BaseInteractionAction
	{

		public FileUploadReviseAction(String id, ActionType actionType,
				String typeId, String helperId, Localizer localizer) {
			super(id, actionType, typeId, helperId, localizer);
		}

		@Override
		public List<String> getRequiredPropertyKeys() 
		{
			List<String> rv = new ArrayList<String>();
			rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
			return rv;
		}

		@Override
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
	

	/**
	 * Class for handling the Expanding of ZIP items.
	 */
	private class FileUploadExpandAction extends BaseServiceLevelAction {

		// [WARN] Archive file handling compress/decompress feature contains bugs; exclude action item.
		// Disable property setting masking problematic code per will of the Community.
		// See Jira KNL-155/SAK-800 for more details.
		// also https://jira.sakaiproject.org/browse/KNL-273
		
		public FileUploadExpandAction(String id, ActionType actionType,
				String typeId, boolean multipleItemAction, Localizer localizer) {
			super(id, actionType, typeId, multipleItemAction, localizer);
		}

		private ZipContentUtil extractZipArchive = new ZipContentUtil();

		@Override
		public void initializeAction(Reference reference) {
			try {
					contentHostingService.expandZippedResource(reference.getId());
			} catch (Exception e) {
				log.error("Exception extracting zip content", e);
			}
		}
		
		@Override
		public boolean available(ContentEntity entity) {
			boolean enabled = false;
			if (entity instanceof ContentResource) {
				enabled = ServerConfigurationService.getBoolean(ContentHostingService.RESOURCES_ZIP_ENABLE, true) ||
				ServerConfigurationService.getBoolean(ContentHostingService.RESOURCES_ZIP_ENABLE_EXPAND, true);
				enabled = enabled && entity.getId().toLowerCase().endsWith(".zip");
			}
			return enabled;
		}
	}
	
	public ResourceToolAction getAction(String actionId) 
	{
		return actions.get(actionId);
	}

	public List<ResourceToolAction> getActions(Reference entityRef, Set permissions) 
	{
		// TODO: use entityRef to filter actions
		List<ResourceToolAction> rv = new ArrayList<ResourceToolAction>();
		rv.addAll(actions.values());
		return rv;
	}

	public List<ResourceToolAction> getActions(Reference entityRef, User user, Set permissions) 
	{
		// TODO: use entityRef and user to filter actions
		List<ResourceToolAction> rv = new ArrayList<ResourceToolAction>();
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
				iconLocation = contentTypeImageService.getContentTypeImage(mimetype);
			}
		}
		return iconLocation;
	}
	
	public String getIconClass(ContentEntity entity) 
	{
		String iconClass = null;
		if(entity != null && entity instanceof ContentResource)
		{
			String mimetype = ((ContentResource) entity).getContentType();
			if(mimetype != null && ! "".equals(mimetype.trim()))
			{
				iconClass = contentTypeImageService.getContentTypeImageClass(mimetype);
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
				hoverText = contentTypeImageService.getContentTypeDisplayName(mimetype);
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

}
