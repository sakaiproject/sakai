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

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.CustomToolAction;
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.entity.api.ResourceProperties;

public class ZipCollectionType extends BaseResourceType implements ExpandableResourceType
{
	public static class UploadZipArchiveAction extends BaseInteractionAction implements CustomToolAction
	{
		public UploadZipArchiveAction(String id, ActionType actionType, String typeId, String helperId, List requiredPropertyKeys)
		{
			super(id, actionType, typeId, helperId, requiredPropertyKeys);
		}

		@Override
		public boolean isAllowed(String entityId, List<String> contentPermissions, boolean isCreator)
		{
			return true;
		}
	}

	public static final String ZIP_COLLECTION_LABEL = "Zip Archive";
	public static final String ZIP_COLLECTION_TYPE_ID = "org.sakaiproject.content.types.zipArchive";
	public static final String ZIP_UPLOAD_HELPER_ID = "sakai.ziparchive.helper";

	public ZipCollectionType()
	{
		List<String> requiredKeys = new ArrayList<>();
		requiredKeys.add(ResourceProperties.PROP_STRUCTOBJ_TYPE);

		ResourceToolAction create = new BaseInteractionAction(ResourceToolAction.CREATE, ResourceToolAction.ActionType.NEW_UPLOAD, ZIP_COLLECTION_TYPE_ID, ZIP_UPLOAD_HELPER_ID, requiredKeys)
		{
			@Override
			public String getLabel()
			{
				return "Upload Zip Archive";
			}
		};

		ResourceToolAction remove = new BaseServiceLevelAction(ResourceToolAction.DELETE, ResourceToolAction.ActionType.DELETE, ZIP_COLLECTION_TYPE_ID, false);

		actionMap.put(create.getActionType(), makeList(create));
		actionMap.put(remove.getActionType(), makeList(remove));

		actions.put(create.getId(), create);
		actions.put(remove.getId(), remove);
	}

	@Override
	public boolean allowAddAction(ResourceToolAction action, ContentEntity entity)
	{
		return action.getTypeId().equals(CompressedResourceType.COMPRESSED_ITEM_TYPE_ID);
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
	public ServiceLevelAction getCollapseAction()
	{
		return (ServiceLevelAction) this.actions.get(ResourceToolAction.COLLAPSE);
	}

	@Override
	public ServiceLevelAction getExpandAction()
	{
		return (ServiceLevelAction) this.actions.get(ResourceToolAction.EXPAND);
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
		return ZIP_COLLECTION_TYPE_ID;
	}

	@Override
	public String getLabel()
	{
		return ZIP_COLLECTION_LABEL;
	}

	@Override
	public String getLocalizedHoverText(ContentEntity entity)
	{
		return ZIP_COLLECTION_LABEL;
	}

	@Override
	public String getLocalizedHoverText(ContentEntity entity, boolean expanded)
	{
		return ZIP_COLLECTION_LABEL;
	}

	@Override
	public String getLongSizeLabel(ContentEntity entity)
	{
		return "files";
	}

	@Override
	public String getSizeLabel(ContentEntity entity)
	{
		return "files";
	}

	@Override
	public boolean isExpandable()
	{
		return true;
	}

	@Override
	public String getIconClass(ContentEntity entity)
	{
		return "";
	}

	@Override
	public String getIconClass(ContentEntity entity, boolean expanded)
	{
		return "";
	}
}
