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

package org.sakaiproject.content.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;

public class BasicResourceToolActionPipe 
	implements ResourceToolActionPipe 
{
	protected byte[] content;
	protected Reference contentEntityReference;
	protected OutputStream contentOutputStream;
	protected String contentType;
	protected String initializationId;
	protected Map propertyValues = new Hashtable();
	protected Map revisedPropertyValues = new Hashtable();
	protected byte[] revisedContent;
	protected InputStream revisedContentStream;
	protected String revisedContentType;
	protected String helperId;
	protected ResourceToolAction action;
	private boolean actionCompleted;
	
	public BasicResourceToolActionPipe(String interactionId, ResourceToolAction action)
	{
		this.initializationId = interactionId;
		this.action = action;
	}

	public byte[] getContent() 
	{
		return this.content;
	}

	public Reference getContentEntityReference() 
	{
		return this.contentEntityReference;
	}

	public OutputStream getContentStream() 
	{
		return this.contentOutputStream;
	}

	public String getContentType() 
	{
		return this.contentType;
	}

	public String getInitializationId() 
	{
		return this.initializationId;
	}

	public Object getPropertyValue(String name) 
	{
		return (String) this.propertyValues.get(name);
	}

	public byte[] getRevisedContent() 
	{
		return this.revisedContent;
	}

	public InputStream getRevisedContentStream() 
	{
		return this.revisedContentStream;
	}

	public String getRevisedContentType() 
	{
		return this.revisedContentType;
	}

	public Map getRevisedResourceProperties() 
	{
		return this.revisedPropertyValues;
	}

	public void setContent(byte[] content) 
	{
		this.content = content;
	}

	public void setContentEntityReference(Reference reference) 
	{
		this.contentEntityReference = reference;
	}

	public void setContentStream(OutputStream ostream) 
	{
		this.contentOutputStream = ostream;
	}

	public void setContentType(String type) 
	{
		this.contentType = type;
	}

	public void setInitializationId(String id) 
	{
		this.initializationId = id;
	}

	public void setResourceProperty(String name, String value) 
	{
		if(value == null)
		{
			this.propertyValues.remove(name);
		}
		else
		{
			this.propertyValues.put(name, value);
		}
	}

	public void setRevisedContent(byte[] content) 
	{
		this.revisedContent = content;
	}

	public void setRevisedContentStream(InputStream istream) 
	{
		this.revisedContentStream = istream;
	}

	public void setRevisedContentType(String type) 
	{
		this.revisedContentType = type;
	}

	public void setRevisedResourceProperty(String name, String value) 
	{
		if(value == null)
		{
			this.revisedPropertyValues.remove(name);
		}
		else
		{
			this.revisedPropertyValues.put(name, value);
		}
	}

	public boolean isActionCanceled() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isErrorEncountered() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setActionCanceled(boolean actionCanceled) 
	{
		// TODO Auto-generated method stub
		
	}

	public void setErrorEncountered(boolean errorEncountered) 
	{
		// TODO Auto-generated method stub
		
	}

	public void setResourceProperty(String key, List list) 
	{
		this.propertyValues.put(key, list);
	}

	public ResourceToolAction getAction() 
	{
		return this.action;
	}

	public void setRevisedResourceProperty(String name, List list) 
	{
		// TODO Auto-generated method stub
		
	}

	public boolean isActionCompleted() 
	{
		return this.actionCompleted;
	}

	public void setActionCompleted(boolean actionCompleted) 
	{
		this.actionCompleted = actionCompleted;
	}

}
