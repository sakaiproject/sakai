/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.exception.ServerOverloadException;

public class BasicResourceToolActionPipe 
	implements ResourceToolActionPipe 
{
	protected byte[] content;
	protected ContentEntity contentEntity;
	protected InputStream contentInputStream;
	protected String contentType;
	protected String initializationId;
	protected Map propertyValues = new HashMap();
	protected Map revisedPropertyValues = new HashMap();
	protected byte[] revisedContent;
	protected InputStream revisedContentStream;
	protected String revisedContentType;
	protected String helperId;
	protected ResourceToolAction action;
	protected boolean actionCompleted;
	protected String errorMessage;
	protected boolean actionCanceled;
	protected boolean errorEncountered;
	protected String fileName;
	protected Object revisedListItem;
	private int notification;
	
	/**
	 * @return the helperId
	 */
	public String getHelperId()
	{
		return this.helperId;
	}

	/**
	 * @param helperId the helperId to set
	 */
	public void setHelperId(String helperId)
	{
		this.helperId = helperId;
	}

	public BasicResourceToolActionPipe(String interactionId, ResourceToolAction action)
	{
		this.initializationId = interactionId;
		this.action = action;
	}

	public byte[] getContent() 
	{
		if(content == null || content.length < 1)
		{
			if(this.contentEntity instanceof ContentResource)
			{
				try
                {
	                return (((ContentResource) this.contentEntity).getContent());
                }
                catch (ServerOverloadException e)
                {
	                this.setErrorEncountered(true);
	                this.setErrorMessage("ServerOverloadException " + e);
                }
			}
		}
		return this.content;
	}

	public ContentEntity getContentEntity() 
	{
		return this.contentEntity;
	}

	public InputStream getContentStream() 
	{
		if(this.contentInputStream == null)
		{
			if(this.contentEntity == null)
			{
	            this.setErrorEncountered(true);
	            this.setErrorMessage("pipe.getContentStream() no stream and no emtity");
				return null;
			}
		}
		else
		{
			try
            {
				int available = this.contentInputStream.available();
	            if(available > 0)
	            {
	        		return this.contentInputStream;
	            }
            }
            catch (IOException e)
            {
	            this.setErrorEncountered(true);
	            this.setErrorMessage("pipe.getContentStream() IOException " + e);
           }

		}
		return getStreamFromEntity();
	}

	protected InputStream getStreamFromEntity()
    {
		if(this.contentEntity != null && this.contentEntity instanceof ContentResource)
		{
			try
            {
	            return ((ContentResource) this.contentEntity).streamContent();
            }
            catch (ServerOverloadException e)
            {
	            this.setErrorEncountered(true);
	            this.setErrorMessage("pipe.getStreamFromEntity() ServerOverloadException " + e);
            }
		}
		return null;
	}

	public String getMimeType() 
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

	public String getRevisedMimeType() 
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

	public void setContentEntity(ContentEntity entity) 
	{
		this.contentEntity = entity;
	}

	public void setContentStream(InputStream ostream) 
	{
		this.contentInputStream = ostream;
	}

	public void setMimeType(String type) 
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

	public void setRevisedMimeType(String type) 
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
		return this.actionCanceled;
	}

	public boolean isErrorEncountered() 
	{
		return this.errorEncountered;
	}

	public void setActionCanceled(boolean actionCanceled) 
	{
		this.actionCanceled = actionCanceled;
	}

	public void setErrorEncountered(boolean errorEncountered) 
	{
		this.errorEncountered = errorEncountered;
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
		this.revisedPropertyValues.put(name, list);
	}

	public boolean isActionCompleted() 
	{
		return this.actionCompleted;
	}

	public void setActionCompleted(boolean actionCompleted) 
	{
		this.actionCompleted = actionCompleted;
	}

	public String getErrorMessage() 
	{
		return this.errorMessage;
	}

	public void setErrorMessage(String msg) 
	{
		this.errorMessage = msg;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceToolActionPipe#getContentstring()
	 */
	public String getContentstring()
	{
		String rv = null;
		byte[] content = getContent();
		if(content != null)
		{
			rv = new String( content );
//			try
//			{
//				rv = new String( content, "UTF-8" );
//			}
//			catch(UnsupportedEncodingException e)
//			{
//				rv = new String( content );
//			}
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceToolActionPipe#getFileName()
	 */
	public String getFileName()
	{
		return this.fileName;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceToolActionPipe#getFileUploadSize()
	 */
	public int getFileUploadSize()
	{
		int rv = 0;
		if(this.revisedContent != null)
		{
			rv = this.revisedContent.length;
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceToolActionPipe#setFileName(java.lang.String)
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public void setRevisedListItem(Object item) 
	{
		this.revisedListItem = item;
	}

	public Object getRevisedListItem() 
	{
		return revisedListItem;
	}

	public int getNotification()
    {
	    // TODO Auto-generated method stub
	    return this.notification;
    }

	public void setNotification(int priority)
    {
	    this.notification = priority;
    }

}
