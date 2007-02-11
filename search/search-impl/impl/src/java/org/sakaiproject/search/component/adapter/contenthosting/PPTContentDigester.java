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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.search.api.SearchUtils;

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
		if ( contentResource != null && 
				contentResource.getContentLength() > maxDigestSize  ) {
			throw new RuntimeException("Attempt to get too much content as a string on "+contentResource.getReference());
		}
		InputStream contentStream = null;

		try
		{
			contentStream = contentResource.streamContent();
			PowerPointExtractor pptExtractor = new PowerPointExtractor(contentStream);
			StringBuilder sb = new StringBuilder();
			SearchUtils.appendCleanString(pptExtractor.getText(), sb);
			sb.append(" ");
			SearchUtils.appendCleanString(pptExtractor.getNotes(), sb);
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
