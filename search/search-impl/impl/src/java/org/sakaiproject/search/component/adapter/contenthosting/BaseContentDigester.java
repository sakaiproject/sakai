/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.search.component.adapter.contenthosting;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ieb
 */
@Slf4j
public abstract class BaseContentDigester implements ContentDigester
{

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

	private Map mimeTypes = null;


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
	public Map getMimeTypes()
	{
		return mimeTypes;
	}

	/**
	 * @param mimeTypes
	 *        The mimeTypes to set.
	 */
	public void setMimeTypes(Map mimeTypes)
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
