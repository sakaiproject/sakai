/**
 * 
 */
package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.sakaiproject.content.api.ContentResource;

/**
 * @author ieb
 */
public class PPTContentDigester extends BaseContentDigester
{
	private static Log log = LogFactory.getLog(PPTContentDigester.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.BaseContentDigester#getContent(org.sakaiproject.content.api.ContentResource)
	 */
	public String getContent(ContentResource contentResource)
	{

		try
		{
			PowerPointExtractor pptExtractor = new PowerPointExtractor(
					contentResource.streamContent());
			StringBuffer sb = new StringBuffer();
			sb.append(pptExtractor.getText()).append(" ").append(
					pptExtractor.getNotes());
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error(e);
			throw new RuntimeException("Failed to read content for indexing ");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.BaseContentDigester#getContentReader(org.sakaiproject.content.api.ContentResource)
	 */
	public Reader getContentReader(ContentResource contentResource)
	{
		return new StringReader(getContent(contentResource));
	}

}
