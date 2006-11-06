/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.content.types;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionController;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

public class TextDocumentType extends BaseResourceType 
{
	public static final String MY_HELPER_ID = "sakai.resource.type.helper";

	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("types");
	
	protected Map actions = new Hashtable();
	protected UserDirectoryService userDirectoryService;
	
	protected String typeId = ResourceType.TYPE_TEXT;
	protected String helperId = "sakai.resource.type.helper";
	
	public class TextDocumentCopyAction implements ServiceLevelAction
	{

		public String getId() 
		{
			return ResourceToolAction.COPY;
		}

		public String getLabel() 
		{
			return rb.getString("action.copy");
		}

		public void invokeAction(Reference reference) 
		{
			// TODO Auto-generated method stub
			
		}

		public boolean isMultipleItemAction() 
		{
			return true;
		}

		public String getTypeId() 
		{
			return typeId;
		}

	}

	public class TextDocumentCreateAction implements InteractionAction
	{
		public void cancelAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		public String getId() 
		{
			return ResourceToolAction.CREATE;
		}

		public String getLabel() 
		{
			return rb.getString("create.text");
		}

		public String getTypeId() 
		{
			return typeId;
		}

		public String getHelperId() 
		{
			return helperId;
		}

		public List getRequiredPropertyKeys() 
		{
			return null;
		}

		public String initializeAction(Reference reference) 
		{
			return null;
			
		}

	}

	public class TextDocumentDeleteAction implements ServiceLevelAction
	{

		public String getId() 
		{
			return ResourceToolAction.DELETE;
		}

		public String getLabel() 
		{
			return rb.getString("action.delete"); 
		}

		public void invokeAction(Reference reference) 
		{
			// TODO Auto-generated method stub
			
		}

		public boolean isMultipleItemAction() 
		{
			return true;
		}

		public String getTypeId() 
		{
			return typeId;
		}

	}

	public class TextDocumentDuplicateAction implements ServiceLevelAction
	{

		public String getId() 
		{
			return ResourceToolAction.DUPLICATE;
		}

		public String getLabel() 
		{
			return rb.getString("action.duplicate"); 
		}

		public void invokeAction(Reference reference) 
		{
			// TODO Auto-generated method stub
			
		}

		public boolean isMultipleItemAction() 
		{
			// TODO Auto-generated method stub
			return false;
		}

		public String getTypeId() 
		{
			return typeId;
		}

	}

	public class TextDocumentMoveAction implements ServiceLevelAction
	{

		public String getId() 
		{
			return ResourceToolAction.MOVE;
		}

		public String getLabel() 
		{
			return rb.getString("action.move"); 
		}

		public void invokeAction(Reference reference) 
		{
			// TODO Auto-generated method stub
			
		}

		public boolean isMultipleItemAction() 
		{
			// TODO Auto-generated method stub
			return true;
		}

		public String getTypeId() 
		{
			return typeId;
		}

	}

	public class TextDocumentReviseAction implements InteractionAction
	{

		public void cancelAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		public String getId() 
		{
			return ResourceToolAction.REVISE_CONTENT;
		}

		public String getLabel() 
		{
			return rb.getString("action.revise"); 
		}

		public String getTypeId() 
		{
			return typeId;
		}

		public String getHelperId() 
		{
			return helperId;
		}

		public List getRequiredPropertyKeys() 
		{
			return null;
		}

		public String initializeAction(Reference reference) 
		{
			return null;
		}

	}
	
	public class TextDocumentAccessAction implements InteractionAction
	{

		public void cancelAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		public String getId() 
		{
			return ResourceToolAction.ACCESS_CONTENT;
		}

		public String getLabel() 
		{
			return rb.getString("action.access"); 
		}

		public String getTypeId() 
		{
			return typeId;
		}

		public String getHelperId() 
		{
			return helperId;
		}

		public List getRequiredPropertyKeys() 
		{
			return null;
		}

		public String initializeAction(Reference reference) 
		{
			// TODO Auto-generated method stub
			return null;
		}

	}
	
	public TextDocumentType()
	{
		this.userDirectoryService = (UserDirectoryService) ComponentManager.get("org.sakaiproject.user.api.UserDirectoryService");
		
		actions.put(ResourceToolAction.CREATE, new TextDocumentCreateAction());
		actions.put(ResourceToolAction.ACCESS_CONTENT, new TextDocumentAccessAction());
		actions.put(ResourceToolAction.REVISE_CONTENT, new TextDocumentReviseAction());
		actions.put(ResourceToolAction.DUPLICATE, new TextDocumentDuplicateAction());
		actions.put(ResourceToolAction.COPY, new TextDocumentCopyAction());
		actions.put(ResourceToolAction.MOVE, new TextDocumentMoveAction());
		actions.put(ResourceToolAction.DELETE, new TextDocumentDeleteAction());
	}

	public ResourceToolAction getAction(String actionId) 
	{
		return (ResourceToolAction) actions.get(actionId);
	}

	public List getActions(Reference entityRef) 
	{
		// TODO: use entityRef to filter actions
		List rv = new Vector();
		rv.addAll(actions.values());
		return rv;
	}

	public List getActions(Reference entityRef, User user) 
	{
		// TODO: use entityRef and user to filter actions
		List rv = new Vector();
		rv.addAll(actions.values());
		return rv;
	}

	public ResourceToolAction getCreateAction(Reference collectionRef, User user) 
	{
		if(! this.isCreateActionAllowed(collectionRef, user))
		{
			return null;
		}
		return (ResourceToolAction) actions.get(ResourceToolAction.CREATE);
	}
	
	public String getIconLocation() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getId() 
	{
		return typeId;
	}

	public String getLabel() 
	{
		return rb.getString("type.text");
	}
	
	public boolean isActionAllowed(String actionId, Reference entityRef, User user) 
	{
		// TODO Auto-generated method stub
		return true;
	}
	
	public boolean isCreateActionAllowed(Reference collectionRef) 
	{
		return this.isCreateActionAllowed(collectionRef, null);
	}
	
	public boolean isCreateActionAllowed(Reference collectionRef, User user) 
	{
		if(user == null)
		{
			user = userDirectoryService.getCurrentUser();
		}
		return this.isActionAllowed(ResourceToolAction.CREATE, collectionRef, user);
	}
	
}
