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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.SiteSpecificResourceType;
import org.sakaiproject.content.api.*;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.javax.Filter;

/**
 * 
 *
 */
@Slf4j
public class ResourceTypeRegistryImpl implements ResourceTypeRegistry 
{
	/** Map of ResourceType objects indexed by typeId */
	protected Map<String, ResourceType> typeIndex = new HashMap<String, ResourceType>();
	
	/** Map of ContentChangeHandler objects indexed by typeId */
	protected Map<String, ContentChangeHandler> typeIdToHandler = new HashMap<String, ContentChangeHandler>();

	protected Map<String,Map<String,Boolean>> enabledTypesMap = new HashMap <String,Map<String,Boolean>>();
	
	protected Map<String, ServiceLevelAction> multiItemActions = new HashMap<String, ServiceLevelAction>();
	
	protected static final SortedSet<String> nativeTypes = new TreeSet<String>();
	static
	{
		nativeTypes.add(ResourceType.TYPE_FOLDER);
		nativeTypes.add(ResourceType.TYPE_TEXT);
		nativeTypes.add(ResourceType.TYPE_UPLOAD);
		nativeTypes.add(ResourceType.TYPE_URL);
		nativeTypes.add(ResourceType.TYPE_HTML);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			log.info("init()");
		}
		catch (Exception t)
		{
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

	/**
	 * @inheritDoc
	 */
	public ResourceType getType(String typeId) 
	{
		return (ResourceType) typeIndex.get(typeId);
	}

	/**
	 * @inheritDoc
	 */
	public Collection getTypes() 
	{
		List types = new ArrayList();
		if(typeIndex != null)
		{
			types.addAll(typeIndex.values());
		}
		return types;
	}

	/**
	 * @inheritDoc
	 */
	public Collection getTypes(Filter filter) 
	{
		List types = new ArrayList();
		if(typeIndex != null)
		{
			Iterator it = typeIndex.values().iterator();
			while(it.hasNext())
			{
				ResourceType type = (ResourceType) it.next();
				if(filter.accept(type))
				{
					types.add(type);
				}
			}
		}
		return types;
	}

	/**
	 * @inheritDoc
	 */
	public void register(ResourceType type) 
	{
        register(type, null);
    }
	/**
	 * @inheritDoc
	 */
	public void register(ResourceType type, ContentChangeHandler cch)
	{
		if(type == null || type.getId() == null)
		{
			return;
		}
		typeIndex.put(type.getId(), type);
		typeIdToHandler.put(type.getId(), cch);

		for(ResourceToolAction action : type.getActions(Arrays.asList(ActionType.values())))
		{
			if(action instanceof ServiceLevelAction && ((ServiceLevelAction) action).isMultipleItemAction())
			{
				ResourceToolAction previouslyRegisteredAction = this.multiItemActions.get(action.getId());
				if(previouslyRegisteredAction == null)
				{
					this.multiItemActions.put(action.getId(), (ServiceLevelAction) action);
				}
				else if(nativeTypes.contains(action.getTypeId()))
				{
					this.multiItemActions.put(action.getId(), (ServiceLevelAction) action);
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#newPipe(java.lang.String, org.sakaiproject.content.api.ResourceToolAction)
	 */
	public ResourceToolActionPipe newPipe(String initializationId, ResourceToolAction action) 
	{
		ResourceToolActionPipe pipe = null;
		
		switch(action.getActionType())
		{
			case NEW_UPLOAD:
				pipe = new BasicMultiFileUploadPipe(initializationId, action);
				break;
			case NEW_FOLDER:
				pipe = new BasicMultiFileUploadPipe(initializationId, action);
				break;
			case NEW_URLS:
				pipe = new BasicMultiFileUploadPipe(initializationId, action);
				break;
			default:
				pipe = new BasicResourceToolActionPipe(initializationId, action);
				break;	
		}
		
		return pipe;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#getAction(java.lang.String, java.lang.String)
	 */
	public ResourceToolAction getAction(String typeId, String actionId)
	{
		ResourceToolAction action = null;
		ResourceType type = (ResourceType) typeIndex.get(typeId);
		if(type != null)
		{
			action = type.getAction(actionId);
		}
		return action;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#mimetype2resourcetype(java.lang.String)
	 */
	public String mimetype2resourcetype(String contentType)
	{
		String typeId = ResourceType.TYPE_UPLOAD;
		
		if(ResourceType.MIME_TYPE_HTML.equals(contentType))
		{
			typeId = ResourceType.TYPE_HTML;
		}
		else if(ResourceType.MIME_TYPE_TEXT.equals(contentType))
		{
			typeId = ResourceType.TYPE_TEXT;
		}
		else if(ResourceType.MIME_TYPE_URL.equals(contentType))
		{
			typeId = ResourceType.TYPE_URL;
		}
		else if(ResourceType.MIME_TYPE_METAOBJ.equals(contentType))
		{
			typeId = ResourceType.TYPE_METAOBJ;
		}
		else
		{
			// do nothing -- use ResourceType.TYPE_UPLOAD
			typeId = ResourceType.TYPE_UPLOAD;
		}
		
		return typeId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#getTypes(java.lang.String)
	 */
	public Collection<ResourceType> getTypes(String context) 
	{
		List<ResourceType> typeDefs = new ArrayList<ResourceType>();
		
		Map<String, Boolean> statusMap = getMapOfResourceTypesForContext(context);
		
		for(ResourceType type : typeIndex.values())
		{
			if(statusMap.containsKey(type.getId()))
			{
				if(statusMap.get(type.getId()).booleanValue())
				{
					typeDefs.add(type);
				}
			}
			else
			{
				typeDefs.add(type);
			}
		}
		
		return typeDefs;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#getContentChangeHandler(java.lang.String)
	 */
	public ContentChangeHandler getContentChangeHandler(String resourceType) {
		return typeIdToHandler.get(resourceType);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#setResourceTypesForContext(java.lang.String, java.util.Map)
	 */
	public void setMapOfResourceTypesForContext(String context, Map<String, Boolean> enabled) 
	{
		this.enabledTypesMap.put(context, new HashMap(enabled));
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#getResourceTypesForContext(java.lang.String)
	 */
	public Map<String, Boolean> getMapOfResourceTypesForContext(String context) 
	{
		Map<String, Boolean> enabled = this.enabledTypesMap.get(context);
		
		if(enabled == null)
		{
			enabled = new HashMap<String, Boolean>();
			for(ResourceType type : this.typeIndex.values())
			{
				if(type instanceof SiteSpecificResourceType)
				{
					enabled.put(type.getId(), Boolean.valueOf(((SiteSpecificResourceType) type).isEnabledByDefault()));
				}
			}
			
			return enabled;
		}
		return new HashMap<String, Boolean>(enabled);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#getMultiItemActions()
	 */
	public Collection<ServiceLevelAction> getMultiItemActions()
    {
	    return new ArrayList<ServiceLevelAction>(this.multiItemActions.values());
    }

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#getMultiItemAction(java.lang.String)
	 */
	public ServiceLevelAction getMultiItemAction(String listActionId)
    {
	    return this.multiItemActions.get(listActionId);
    } 

}
