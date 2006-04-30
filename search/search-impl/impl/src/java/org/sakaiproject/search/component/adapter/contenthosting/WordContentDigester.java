/**
 * 
 */
package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.sakaiproject.content.api.ContentResource;

import sun.awt.image.ByteArrayImageSource;

/**
 * @author ieb
 */
public class WordContentDigester extends BaseContentDigester
{

	private static Log log = LogFactory.getLog(WordContentDigester.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.BaseContentDigester#getContent(org.sakaiproject.content.api.ContentResource)
	 */
	public String getContent(ContentResource contentResource)
	{

		InputStream contentStream = null;
		try
		{
			contentStream = contentResource.streamContent();
			WordExtractor wordExtractor = new WordExtractor(contentStream);
			String[] paragraphs = wordExtractor.getParagraphText();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < paragraphs.length; i++)
			{
				sb.append(paragraphs[i]).append(" ");
			}
			return sb.toString();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to read content for indexing ",
					e);
		}
		finally
		{
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
	 * @see org.sakaiproject.search.component.adapter.contenthosting.BaseContentDigester#getContentReader(org.sakaiproject.content.api.ContentResource)
	 */
	public Reader getContentReader(ContentResource contentResource)
	{
		return new StringReader(getContent(contentResource));
	}

}
