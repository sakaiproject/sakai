/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.Reader;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;

/**
 * @author ieb
 */
public abstract class BaseContentDigester implements ContentDigester
{

	private static Log log = LogFactory.getLog(BaseContentDigester.class);

	private ContentHostingContentProducer contentProducer = null;
	
	protected int maxDigestSize = 1024 * 1024 * 5; // 10M


	public void init()
	{
		try
		{
			contentProducer.addDigester(this);
		}
		catch (Throwable t)
		{
			log.error("Failed to init", t);
		}
	}

	public void destroy()
	{
		contentProducer.removeDigester(this);
	}

	private HashMap mimeTypes = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.ContentDigester#getContent(org.sakaiproject.content.api.ContentResource)
	 */
	public abstract String getContent(ContentResource contentResource);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.ContentDigester#getContentReader(org.sakaiproject.content.api.ContentResource)
	 */
	public abstract Reader getContentReader(ContentResource contentResource);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.ContentDigester#accept(java.lang.String)
	 */
	public boolean accept(String mimeType)
	{
		return (mimeTypes.get(mimeType) != null);
	}

	/**
	 * @return Returns the mimeTypes.
	 */
	public HashMap getMimeTypes()
	{
		return mimeTypes;
	}

	/**
	 * @param mimeTypes
	 *        The mimeTypes to set.
	 */
	public void setMimeTypes(HashMap mimeTypes)
	{
		this.mimeTypes = mimeTypes;
	}

	/**
	 * @return Returns the contentProducer.
	 */
	public ContentHostingContentProducer getContentProducer()
	{
		return contentProducer;
	}

	/**
	 * @param contentProducer
	 *        The contentProducer to set.
	 */
	public void setContentProducer(ContentHostingContentProducer contentProducer)
	{
		this.contentProducer = contentProducer;
	}

	/**
	 * @return Returns the maxDigestSize.
	 */
	public int getMaxDigestSize()
	{
		return maxDigestSize;
	}

	/**
	 * @param maxDigestSize The maxDigestSize to set.
	 */
	public void setMaxDigestSize(int maxDigestSize)
	{
		this.maxDigestSize = maxDigestSize;
	}

}
