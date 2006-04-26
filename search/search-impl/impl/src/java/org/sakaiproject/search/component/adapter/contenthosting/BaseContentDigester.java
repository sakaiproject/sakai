/**
 * 
 */
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

}
