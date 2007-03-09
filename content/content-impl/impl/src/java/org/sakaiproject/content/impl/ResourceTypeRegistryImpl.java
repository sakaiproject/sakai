/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.content.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.javax.Filter;

public class ResourceTypeRegistryImpl implements ResourceTypeRegistry 
{
	/** Our logger. */
	protected static Log M_log = LogFactory.getLog(ResourceTypeRegistryImpl.class);

	/** Map of ResourceType objects indexed by typeId */
	protected Map typeIndex = new HashMap();

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			M_log.info("init()");
		}
		catch (Throwable t)
		{
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
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
		List types = new Vector();
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
		List types = new Vector();
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
		if(type == null || type.getId() == null)
		{
			return;
		}
//		System.out.println("----------> ResourceTypeRegistry.register(" + type.getId() + ", " + type.getLabel() + ")");
//		List actions = type.getActions(null);
//		Iterator it = actions.iterator();
//		while(it.hasNext())
//		{
//			ResourceToolAction action = (ResourceToolAction) it.next();
//			System.out.println("          > " + action.getId() + " ==> " + action.getLabel() );
//			
//		}
		typeIndex.put(type.getId(), type);
		
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

}
