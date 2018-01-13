/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.ContentHandler;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import org.sakaiproject.content.api.ContentResource;

/**
 * @author stephen.marquard@uct.ac.za
 */
@Slf4j
public class TikaContentDigester extends BaseContentDigester
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.BaseContentDigester#getContent(org.sakaiproject.content.api.ContentResource)
	 */

	public String getContent(ContentResource contentResource)
	{
		log.debug("Digesting with TikaContentDigester");
		if (contentResource == null) {
			throw new RuntimeException("Null contentResource passed to getContent");
		}

		InputStream contentStream = null;

		try
		{
			contentStream = contentResource.streamContent();			
			
			Metadata metadata = new Metadata();
			
			metadata.set(Metadata.CONTENT_TYPE, contentResource.getContentType());
			ContentHandler handler = new BodyContentHandler();
			Parser parser = new AutoDetectParser();
				
			parser.parse(contentStream, handler, metadata, new ParseContext());
			return handler.toString();
		}
		catch (Exception e)
		{
			log.debug("Cannot index", e);
			throw new RuntimeException("Failed to read content for indexing ", e);
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
					log.debug(e.getMessage());
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
