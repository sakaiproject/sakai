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
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;

public abstract class BaseResourceType implements ResourceType {

	protected EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>> actionMap =
	      new EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>>(ResourceToolAction.ActionType.class);
	protected Map<String, ResourceToolAction> actions = new Hashtable<String, ResourceToolAction>();

	
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
	
	public String getIconLocation(ContentEntity entity) {
		return null;
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
		return false;
	}
	
	protected List<ResourceToolAction> makeList(ResourceToolAction create) {
	      List<ResourceToolAction> returned = new ArrayList<ResourceToolAction>();
	      returned.add(create);
	      return returned;
	}

}
