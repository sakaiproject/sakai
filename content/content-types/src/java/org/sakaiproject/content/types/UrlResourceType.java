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

package org.sakaiproject.content.types;

import static org.sakaiproject.content.api.ResourceToolAction.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceType;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.content.util.BaseResourceAction.Localizer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

public class UrlResourceType extends BaseResourceType 
{
	protected String typeId = ResourceType.TYPE_URL;
	protected String helperId = "sakai.resource.type.helper";
	
	/** localized tool properties **/
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.TypeProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.type.types";
	private static final String RESOURCECLASS = "resource.class.type";
	private static final String RESOURCEBUNDLE = "resource.bundle.type";
	private ServerConfigurationService serverConfigurationService =  (ServerConfigurationService) ComponentManager.get("org.sakaiproject.component.api.ServerConfigurationService");;
	private String resourceClass = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
	private String resourceBundle = serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
	private ResourceLoader rb = new Resource().getLoader(resourceClass, resourceBundle);

	// private static ResourceLoader rb = new ResourceLoader("types");
	
	protected EnumMap<ActionType, List<ResourceToolAction>> actionMap = new EnumMap<ActionType, List<ResourceToolAction>>(ActionType.class);

	protected Map<String, ResourceToolAction> actions = new HashMap<String, ResourceToolAction>();	



	
	private Localizer localizer(final String string) {
		return new Localizer() {

			public String getLabel() {
				return rb.getString(string);
			}
			
		};
	}
	
	public UrlResourceType()
	{		
		actions.put(CREATE, new BaseInteractionAction(CREATE, ActionType.NEW_URLS, typeId, helperId, localizer("create.url")));
		actions.put(REVISE_CONTENT, new BaseInteractionAction(REVISE_CONTENT, ActionType.REVISE_CONTENT, typeId, helperId, localizer("action.revise")));
		actions.put(ACCESS_PROPERTIES, new BaseServiceLevelAction(ACCESS_PROPERTIES, ActionType.VIEW_METADATA, typeId, false, localizer("action.access")));
		actions.put(REVISE_METADATA, new BaseServiceLevelAction(REVISE_METADATA, ActionType.REVISE_METADATA, typeId, false, localizer("action.props")));
		actions.put(DUPLICATE, new BaseServiceLevelAction(DUPLICATE, ActionType.DUPLICATE, typeId, false, localizer("action.duplicate")));
		actions.put(COPY, new BaseServiceLevelAction(COPY, ActionType.COPY, typeId, true, localizer("action.copy")));
		actions.put(MOVE, new BaseServiceLevelAction(MOVE, ActionType.MOVE, typeId, true, localizer("action.move")));
		actions.put(DELETE, new BaseServiceLevelAction(DELETE, ActionType.DELETE, typeId, true, localizer("action.delete")));
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
		return null;
	}
	
	public String getId() 
	{
		return typeId;
	}

	public String getLabel() 
	{
		return rb.getString("type.url");
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getLocalizedHoverText(org.sakaiproject.entity.api.Reference)
	 */
	public String getLocalizedHoverText(ContentEntity member)
	{
		return rb.getString("type.url");
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

	@Override
    public boolean hasRightsDialog()
    {
		return serverConfigurationService.getBoolean("content.url.rightsdialog", false);
    }

}
