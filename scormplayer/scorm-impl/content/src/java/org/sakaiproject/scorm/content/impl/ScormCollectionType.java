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
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.entity.api.ResourceProperties;

@Slf4j
public class ScormCollectionType extends ZipCollectionType
{
	public static final String SCORM_CONTENT_LABEL = "SCORM Package";
	public static final String SCORM_CONTENT_TYPE_ID = "org.sakaiproject.content.types.scormContentPackage";
	public static final String SCORM_UPLOAD_LABEL = "Upload SCORM Package";
	public static final String SCORM_LAUNCH_LABEL = "Launch";
	public static final String SCORM_REMOVE_LABEL = "Remove";
	public static final String SCORM_UPLOAD_HELPER_ID = "sakai.scorm.helper"; //="sakai.resource.type.helper";
	public static final String SCORM_LAUNCH_TOOL_ID = "sakai.scorm.helper";
	public static final String SCORM_ACCESS_HELPER_ID = "sakai.scorm.access";
	public static final String SCORM_REMOVE_HELPER_ID = "sakai.scorm.remove.helper";

	public ScormCollectionType()
	{
		List<String> requiredKeys = new ArrayList<>();
		requiredKeys.add(ResourceProperties.PROP_STRUCTOBJ_TYPE);

		ResourceToolAction remove = new BaseInteractionAction(ResourceToolAction.DELETE, ResourceToolAction.ActionType.DELETE, SCORM_CONTENT_TYPE_ID, SCORM_REMOVE_HELPER_ID, requiredKeys)
		{
			@Override
			public String getLabel()
			{
				return SCORM_REMOVE_LABEL;
			}
		};

		actionMap.put(remove.getActionType(), makeList(remove));
		actions.put(remove.getId(), remove);
	}

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
			list = new ArrayList<>();
			actionMap.put(type, list);
		}

		return new ArrayList<>(list);
	}

	@Override
	public List<ResourceToolAction> getActions(List<ActionType> types)
	{
		List<ResourceToolAction> list = new ArrayList<>();
		if (types != null)
		{
			Iterator<ResourceToolAction.ActionType> it = types.iterator();
			while (it.hasNext())
			{
				ResourceToolAction.ActionType type = it.next();
				List<ResourceToolAction> sublist = actionMap.get(type);
				if (sublist == null)
				{
					sublist = new ArrayList<>();
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
		String iconLocation = "sakai/dir_openroot.gif";
		if (entity != null && entity.isCollection())
		{
			ContentCollection collection = (ContentCollection) entity;
			int memberCount = collection.getMemberCount();
			if (memberCount == 0)
			{
				iconLocation = "sakai/dir_closed.gif";
			}
			else if (memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT)
			{
				iconLocation = "sakai/dir_unexpand.gif";
			}
		}

		return iconLocation;
	}

	@Override
	public String getIconLocation(ContentEntity entity, boolean expanded)
	{
		String iconLocation = "sakai/dir_openroot.gif";
		if (entity.isCollection())
		{
			ContentCollection collection = (ContentCollection) entity;
			int memberCount = collection.getMemberCount();
			if (memberCount == 0)
			{
				iconLocation = "sakai/dir_closed.gif";
			}
			else if (memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT)
			{
				iconLocation = "sakai/dir_unexpand.gif";
			}
			else if (expanded)
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

	@Override
	public String getId()
	{
		return SCORM_CONTENT_TYPE_ID;
	}

	@Override
	public String getLabel()
	{
		return SCORM_CONTENT_LABEL;
	}

	@Override
	public String getLocalizedHoverText(ContentEntity entity)
	{
		return SCORM_CONTENT_LABEL;
	}

	@Override
	public String getLocalizedHoverText(ContentEntity entity, boolean expanded)
	{
		return "Scorm Content Package";
	}
}
