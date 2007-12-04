/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.sakaiproject.scorm.content.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.CustomToolAction;
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.entity.api.ResourceProperties;

public class ZipCollectionType extends BaseResourceType implements ExpandableResourceType {	
	public static final String ZIP_COLLECTION_LABEL="Zip Archive";
	public static final String ZIP_COLLECTION_TYPE_ID="org.sakaiproject.content.types.zipArchive";
	public static final String ZIP_UPLOAD_HELPER_ID="sakai.ziparchive.helper";
	
	
	public class UploadZipArchiveAction extends BaseInteractionAction implements CustomToolAction {

		public UploadZipArchiveAction(String id, ActionType actionType, String typeId, String helperId, List requiredPropertyKeys) {
			super(id, actionType, typeId, helperId, requiredPropertyKeys);
		}

		public boolean isAllowed(String entityId, List<String> contentPermissions, boolean isCreator) {
			return true;
		}
	}
	
	public ZipCollectionType() {
		List<String> requiredKeys = new ArrayList<String>();
	    requiredKeys.add(ResourceProperties.PROP_STRUCTOBJ_TYPE);
		
	    ResourceToolAction create = new BaseInteractionAction(ResourceToolAction.CREATE, 
	    		ResourceToolAction.ActionType.NEW_UPLOAD, ZIP_COLLECTION_TYPE_ID, 
	    		ZIP_UPLOAD_HELPER_ID, requiredKeys) {

	    	public String getLabel() {
	    		return "Upload Zip Archive";
	    	}
	    	
	    }; 

	    ResourceToolAction remove = new BaseServiceLevelAction(ResourceToolAction.DELETE, 
	    		ResourceToolAction.ActionType.DELETE, ZIP_COLLECTION_TYPE_ID, false);

	    actionMap.put(create.getActionType(), makeList(create));
	    actionMap.put(remove.getActionType(), makeList(remove));
	    
	    actions.put(create.getId(), create);
	    actions.put(remove.getId(), remove);
	    
	}
	
	public ResourceToolAction getAction(String actionId) {
		return actions.get(actionId);
	}

	public List<ResourceToolAction> getActions(ActionType type) {
		List<ResourceToolAction> list = actionMap.get(type);
		if (list == null) {
			list = new Vector<ResourceToolAction>();
			actionMap.put(type, list);
		}
		return new Vector<ResourceToolAction>(list);
	}

	public List<ResourceToolAction> getActions(List<ActionType> types) {
		 List<ResourceToolAction> list = new Vector<ResourceToolAction>();
			if (types != null) {
				Iterator<ResourceToolAction.ActionType> it = types.iterator();
				while (it.hasNext()) {
					ResourceToolAction.ActionType type = it.next();
					List<ResourceToolAction> sublist = actionMap.get(type);
					if (sublist == null) {
						sublist = new Vector<ResourceToolAction>();
						actionMap.put(type, sublist);
					}
					list.addAll(sublist);
				}
			}
			return list;
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
	
	public String getId() {
		return ZIP_COLLECTION_TYPE_ID;
	}

	public String getLabel() {
		return ZIP_COLLECTION_LABEL;
	}

	public String getLocalizedHoverText(ContentEntity entity) {
		return ZIP_COLLECTION_LABEL;
	}

	public String getLocalizedHoverText(ContentEntity entity, boolean expanded) {
		return ZIP_COLLECTION_LABEL;
	}	

	public boolean isExpandable() {
		return true;
	}

	public boolean allowAddAction(ResourceToolAction action, ContentEntity entity) {
		return action.getTypeId().equals(CompressedResourceType.COMPRESSED_ITEM_TYPE_ID);
	}

	public ServiceLevelAction getCollapseAction() {
		return (ServiceLevelAction) this.actions.get(ResourceToolAction.COLLAPSE);
	}

	public ServiceLevelAction getExpandAction() {
		return (ServiceLevelAction) this.actions.get(ResourceToolAction.EXPAND);
	}
	
	public String getLongSizeLabel(ContentEntity entity) {
		return "files";
	}

	public String getSizeLabel(ContentEntity entity) {
		return "files";
	}

}
