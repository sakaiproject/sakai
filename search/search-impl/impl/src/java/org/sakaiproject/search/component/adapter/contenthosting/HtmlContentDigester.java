/**
 * 
 */
package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.search.component.adapter.util.DigestHtml;
import org.w3c.tidy.Tidy;

/**
 * @author ieb
 */
public class HtmlContentDigester extends BaseContentDigester
{
	private static Log log = LogFactory.getLog(HtmlContentDigester.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.ContentDigester#getContent(org.sakaiproject.content.api.ContentResource)
	 */
	public String getContent(ContentResource contentResource)
	{
		InputStream contentStream = null;
		Tidy tidy = new Tidy();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			contentStream = contentResource.streamContent();
			tidy.parse(contentStream, baos);
			return DigestHtml.digest(new String(baos.toByteArray()));
			
		}
		catch (ServerOverloadException e)
		{
			throw new RuntimeException("Failed get Resource Content ", e);
		}
		finally
		{
			if (baos != null)
			{
				try
				{
					baos.close();
				}
				catch (IOException e)
				{
				}
			}
			if (contentStream != null)
			{
				try
				{
					contentStream.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.ContentDigester#getContentReader(org.sakaiproject.content.api.ContentResource)
	 */
	public Reader getContentReader(ContentResource contentResource)
	{
		// we cant stream it, as Tidy wont allow us to
		return new StringReader(getContent(contentResource));
	}
}
