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
package org.sakaiproject.scorm.client.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;


public class ScormContentType implements ResourceType {
	public static final String SCORM_CONTENT_LABEL="SCORM Package";
	public static final String SCORM_CONTENT_TYPE_ID="org.sakaiproject.content.types.scormContentPackage";
	
	public static final String SCORM_UPLOAD_LABEL="Upload SCORM Package";
	public static final String SCORM_LAUNCH_LABEL="Launch";
	public static final String SCORM_REMOVE_LABEL="Remove";

	public static final String SCORM_UPLOAD_HELPER_ID="sakai.resource.type.helper";
	public static final String SCORM_LAUNCH_TOOL_ID="sakai.scorm.helper";
	
	
	private ScormClientFacade scormClientService;
	private EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>> actionMap =
	      new EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>>(ResourceToolAction.ActionType.class);
	private Map<String, ResourceToolAction> actions = new Hashtable<String, ResourceToolAction>();
	
	public ScormContentType(ScormClientFacade scormClientService) {
		this.scormClientService = scormClientService;
		
		List<String> requiredKeys = new ArrayList<String>();
	    requiredKeys.add(ResourceProperties.PROP_STRUCTOBJ_TYPE);
	    //requiredKeys.add(ContentHostingService.PROP_ALTERNATE_REFERENCE);
	      
	    ResourceToolAction create = new BaseInteractionAction(ResourceToolAction.CREATE, ResourceToolAction.ActionType.NEW_UPLOAD, SCORM_CONTENT_TYPE_ID, SCORM_UPLOAD_HELPER_ID, requiredKeys);   
	    ResourceToolAction launch = new ScormLaunchAction();
	    ResourceToolAction remove = new BaseServiceLevelAction(ResourceToolAction.DELETE, ResourceToolAction.ActionType.DELETE, SCORM_CONTENT_TYPE_ID, false);
	    ResourceToolAction intercept = new BaseInteractionAction(ResourceToolAction.INTERCEPT_CONTENT, ResourceToolAction.ActionType.INTERCEPT_CONTENT, SCORM_CONTENT_TYPE_ID, SCORM_LAUNCH_TOOL_ID, requiredKeys);   
	    
	    actionMap.put(create.getActionType(), makeList(create));
	    actionMap.put(launch.getActionType(), makeList(launch));
	    actionMap.put(remove.getActionType(), makeList(remove));
	    actionMap.put(intercept.getActionType(), makeList(intercept));
	    
	    actions.put(create.getId(), create);
	    actions.put(launch.getId(), launch);
	    actions.put(remove.getId(), remove);
	    actions.put(intercept.getId(), intercept);
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

	public String getIconLocation(ContentEntity entity) {
		return null;
	}

	public String getId() {
		return SCORM_CONTENT_TYPE_ID;
	}

	public String getLabel() {
		return SCORM_CONTENT_LABEL;
	}

	public String getLocalizedHoverText(ContentEntity entity) {
		return SCORM_CONTENT_LABEL;
	}

	public boolean hasAvailabilityDialog() {
		return false;
	}

	public boolean hasDescription() {
		return false;
	}

	public boolean hasGroupsDialog() {
		return false;
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
	

	public class ScormUploadCreateAction implements InteractionAction {
		
		public ScormUploadCreateAction() { 
			
		}
		
		public void cancelAction(Reference reference, String initializationId) 
		{
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{

		}

		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.NEW_UPLOAD;
		}

		public String getId() 
		{
			return ResourceToolAction.CREATE;
		}

		public String getLabel() 
		{
			return SCORM_UPLOAD_LABEL; 
		}
		
		public String getTypeId() 
		{
			return SCORM_CONTENT_TYPE_ID;
		}

		public String getHelperId() 
		{
			return SCORM_UPLOAD_HELPER_ID;
		}

		public List getRequiredPropertyKeys() 
		{
			List<String> rv = new Vector<String>();
			rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
			rv.add(ContentHostingService.PROP_ALTERNATE_REFERENCE);
			return rv;
		}

		public String initializeAction(Reference reference) 
		{
			//ContentEntity entity = (ContentEntity) reference.getEntity();
			
			//entity.getProperties().addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, org.sakaiproject.scorm.client.api.ScormClientService.REFERENCE_ROOT);
			
			return null;
		}

        public boolean available(ContentEntity entity)
        {
	        return true;
        }
	}
	
	public class ScormLaunchAction implements InteractionAction {
		
		public ScormLaunchAction() {}
		
		public void cancelAction(Reference reference, String initializationId) 
		{
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			
		}

		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.VIEW_CONTENT;
		}

		public String getId() 
		{
			return "launch";
		}

		public String getLabel() 
		{
			return SCORM_LAUNCH_LABEL; 
		}
		
		public String getTypeId() 
		{
			return SCORM_CONTENT_TYPE_ID;
		}

		public String getHelperId() 
		{
			return SCORM_LAUNCH_TOOL_ID;
		}

		public List getRequiredPropertyKeys() 
		{
			List<String> rv = new Vector<String>();
			rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
			return rv;
		}

		public String initializeAction(Reference reference) 
		{
			
			return null;
		}

        public boolean available(ContentEntity entity)
        {
	        return true;
        }
	}
	

	protected List<ResourceToolAction> makeList(ResourceToolAction create) {
	      List returned = new ArrayList<ResourceToolAction>();
	      returned.add(create);
	      return returned;
	}
	
}
