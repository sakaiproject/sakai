/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.content.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.api.ResourceType;

public abstract class BaseResourceType implements ResourceType
{
	protected EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>> actionMap = new EnumMap<>(ResourceToolAction.ActionType.class);
	protected Map<String, ResourceToolAction> actions = new Hashtable<>();

	@Override
	public ResourceToolAction getAction(String actionId)
	{
		return actions.get(actionId);
	}

	@Override
	public List<ResourceToolAction> getActions(ActionType type)
	{
		List<ResourceToolAction> list = actionMap.get(type);
		if (list == null)
		{
			list = new Vector<>();
			actionMap.put(type, list);
		}

		return new Vector<>(list);
	}

	@Override
	public List<ResourceToolAction> getActions(List<ActionType> types)
	{
		List<ResourceToolAction> list = new Vector<>();
		if (types != null)
		{
			Iterator<ResourceToolAction.ActionType> it = types.iterator();
			while (it.hasNext())
			{
				ResourceToolAction.ActionType type = it.next();
				List<ResourceToolAction> sublist = actionMap.get(type);
				if (sublist == null)
				{
					sublist = new Vector<>();
					actionMap.put(type, sublist);
				}

				list.addAll(sublist);
			}
		}

		return list;
	}

	@Override
	public String getIconLocation(ContentEntity entity)
	{
		return null;
	}

	@Override
	public boolean hasAvailabilityDialog()
	{
		return false;
	}

	@Override
	public boolean hasDescription()
	{
		return false;
	}

	@Override
	public boolean hasGroupsDialog()
	{
		return true;
	}

	@Override
	public boolean hasNotificationDialog()
	{
		return false;
	}

	@Override
	public boolean hasOptionalPropertiesDialog()
	{
		return false;
	}

	@Override
	public boolean hasPublicDialog()
	{
		return true;
	}

	@Override
	public boolean hasRightsDialog()
	{
		return false;
	}

	@Override
	public boolean isExpandable()
	{
		return false;
	}

	protected List<ResourceToolAction> makeList(ResourceToolAction create)
	{
		List<ResourceToolAction> returned = new ArrayList<>();
		returned.add(create);
		return returned;
	}
}
