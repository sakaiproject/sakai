/**
 * 
 */
package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.sakaiproject.content.api.ContentResource;

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

		try
		{
			WordExtractor wordExtractor = new WordExtractor(contentResource
					.streamContent());
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
			throw new RuntimeException("Failed to read content for indexing ",e);
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
