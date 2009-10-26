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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.component.adapter.util.DigestHtml;
import org.sakaiproject.search.util.HTMLParser;
import org.w3c.tidy.Tidy;

/**
 * @author ieb
 */
public class HtmlContentDigester extends BaseContentDigester
{
	private static Log log = LogFactory.getLog(HtmlContentDigester.class);

	private boolean useDirectParser = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.ContentDigester#getContent(org.sakaiproject.content.api.ContentResource)
	 */
	public String getContent(ContentResource contentResource)

	{
		if (contentResource == null )
		{
			throw new RuntimeException("null contentResource passed to getContent");
		}
		if (useDirectParser)
		{
			try
			{
				String content = new String(contentResource.getContent(),"UTF-8");
				StringBuilder sb = new StringBuilder();
				for (Iterator<String> i = new HTMLParser(content); i.hasNext();)
				{
					String s = i.next();
					if (s.length() > 0)
					{
						SearchUtils.appendCleanString(s, sb);
					}
				}
				return sb.toString();
			}
			catch (ServerOverloadException ex)
			{
				throw new RuntimeException("Failed get Resource Content ", ex);

			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException("Failed get Resource Content ", e);
			}
		}
		else
		{

			InputStream contentStream = null;
			Tidy tidy = new Tidy();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try
			{

				contentStream = contentResource.streamContent();
				log.info("Raw Content was " + contentStream);
				tidy.setQuiet(true);
				tidy.setShowWarnings(false);
				tidy.setOnlyErrors(true);
				tidy.parse(contentStream, baos);

				String tidyOut = SearchUtils.appendCleanString(new String(baos.toByteArray(),"UTF-8"),null).toString();
				log.info(contentResource.getReference() + " Tidy Output was " + tidyOut);
				log.debug("Tidy Output was " + tidyOut);
				return DigestHtml.digest(tidyOut);

			}
			catch (ServerOverloadException e)
			{
				throw new RuntimeException("Failed get Resource Content ", e);
			}
			catch (UnsupportedEncodingException e)
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
						log.debug(e);
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
						log.debug(e);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.ContentDigester#getContentReader(org.sakaiproject.content.api.ContentResource, int)
	 */
	public Reader getContentReader(ContentResource contentResource)
	{
		return new StringReader(getContent(contentResource));
	}
}
