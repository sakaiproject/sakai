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
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.entity.api.ResourceProperties;

public class ZipCollectionType implements ExpandableResourceType {	
	public static final String ZIP_COLLECTION_LABEL="Zip Archive";
	public static final String ZIP_COLLECTION_TYPE_ID="org.sakaiproject.content.types.zipArchive";
	public static final String ZIP_UPLOAD_HELPER_ID="sakai.ziparchive.helper";
	
	private EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>> actionMap =
	      new EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>>(ResourceToolAction.ActionType.class);
	private Map<String, ResourceToolAction> actions = new Hashtable<String, ResourceToolAction>();
	
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
				iconLocation = "sakai/ziparchive_closed.gif";
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
				iconLocation = "sakai/ziparchive_closedplus.gif";
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
				iconLocation = "sakai/ziparchive_closed.gif";
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

	public boolean hasAvailabilityDialog() {
		return false;
	}

	public boolean hasDescription() {
		return false;
	}

	public boolean hasGroupsDialog() {
		return true;
	}

	public boolean hasNotificationDialog() {
		return false;
	}

	public boolean hasOptionalPropertiesDialog() {
		return false;
	}

	public boolean hasPublicDialog() {
		return true;
	}

	public boolean hasRightsDialog() {
		return false;
	}

	public boolean isExpandable() {
		return true;
	}

	
	protected List<ResourceToolAction> makeList(ResourceToolAction create) {
	      List<ResourceToolAction> returned = new ArrayList<ResourceToolAction>();
	      returned.add(create);
	      return returned;
	}

	public boolean allowAddAction(ResourceToolAction action, ContentEntity entity) {
		return action.getTypeId().equals(ZIP_COLLECTION_TYPE_ID);
	}

	public ServiceLevelAction getCollapseAction() {
		return (ServiceLevelAction) this.actions.get(ResourceToolAction.COLLAPSE);
	}

	public ServiceLevelAction getExpandAction() {
		return (ServiceLevelAction) this.actions.get(ResourceToolAction.EXPAND);
	}

	public String getLocalizedHoverText(ContentEntity entity, boolean expanded) {
		return ZIP_COLLECTION_LABEL;
	}	
}
