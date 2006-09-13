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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.search.api.SearchUtils;
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
		if ( contentResource != null && 
				contentResource.getContentLength() > maxDigestSize  ) {
			throw new RuntimeException("Attempt to get too much content as a string on "+contentResource.getReference());
		}
		InputStream contentStream = null;
		Tidy tidy = new Tidy();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			
			contentStream = contentResource.streamContent();
			tidy.setQuiet(true);
			tidy.setShowWarnings(false);
			tidy.setOnlyErrors(true);
			tidy.parse(contentStream, baos);
			
			String tidyOut = SearchUtils.getCleanString(new String(baos.toByteArray()));
			log.debug("Tidy Output was "+tidyOut);
			return DigestHtml.digest(tidyOut);
			
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
