package org.sakaiproject.scorm.content.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.entity.api.ResourceProperties;

public class CompressedResourceType extends BaseResourceType {
	public static final String COMPRESSED_ITEM_LABEL="Zip Entry";
	public static final String COMPRESSED_ITEM_TYPE_ID="org.sakaiproject.content.types.zipEntry";
	public static final String COMPRESSED_ITEM_HELPER_ID="sakai.zipentry.helper";
	
	public CompressedResourceType() {
		List<String> requiredKeys = new ArrayList<String>();
	    requiredKeys.add(ResourceProperties.PROP_STRUCTOBJ_TYPE);
		
	    ResourceToolAction create = new BaseInteractionAction(ResourceToolAction.CREATE, 
	    		ResourceToolAction.ActionType.NEW_UPLOAD, COMPRESSED_ITEM_TYPE_ID, 
	    		COMPRESSED_ITEM_HELPER_ID, requiredKeys) {

	    	public String getLabel() {
	    		return "Add file(s)";
	    	}
	    	
	    };

	    ResourceToolAction remove = new BaseServiceLevelAction(ResourceToolAction.DELETE, 
	    		ResourceToolAction.ActionType.DELETE, COMPRESSED_ITEM_TYPE_ID, false);

	    actionMap.put(create.getActionType(), makeList(create));
	    actionMap.put(remove.getActionType(), makeList(remove));
	    
	    actions.put(create.getId(), create);
	    actions.put(remove.getId(), remove);
	}

	public String getId() {
		return COMPRESSED_ITEM_TYPE_ID;
	}

	public String getLabel() {
		return COMPRESSED_ITEM_LABEL;
	}

	public String getLocalizedHoverText(ContentEntity entity) {
		return COMPRESSED_ITEM_LABEL;
	}
	
}
