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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.search.api.SearchUtils;

/**
 * @author ieb
 */
@Slf4j
public class PPTContentDigester extends BaseContentDigester
{

	static
	{
		System.setProperty("org.apache.poi.util.POILogger",
				"org.apache.poi.util.NullLogger");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.BaseContentDigester#getContent(org.sakaiproject.content.api.ContentResource)
	 */

	public String getContent(ContentResource contentResource)
	{
		if (contentResource == null) {
			throw new RuntimeException("Null contentResource passed to getContent");
		}
		InputStream contentStream = null;

		try
		{
			// this is informed by the text extractors in Jackrabbit

			final ByteArrayOutputStream os = new ByteArrayOutputStream();

			POIFSReaderListener listener = new POIFSReaderListener()
			{
				public void processPOIFSReaderEvent(POIFSReaderEvent event)
				{
					try
					{
						if (!event.getName().equalsIgnoreCase("PowerPoint Document"))
						{
							return;
						}
						DocumentInputStream input = event.getStream();
						byte[] buffer = new byte[input.available()];
						input.read(buffer, 0, input.available());
						for (int i = 0; i < buffer.length - 20; i++)
						{
							long type = LittleEndian.getUShort(buffer, i + 2);
							long size = LittleEndian.getUInt(buffer, i + 4);
							if (type == 4008)
							{
								os.write(buffer, i + 4 + 1, (int) size + 3);
								i = i + 4 + 1 + (int) size - 1;
							}
						}
					}
					catch (Exception e)
					{
						log.debug(e.getMessage());
					}
				}
			};

			POIFSReader reader = new POIFSReader();
			reader.registerListener(listener);
			contentStream = contentResource.streamContent();
			reader.read(contentStream);
			os.flush();
			StringBuilder sb = new StringBuilder();
			SearchUtils.appendCleanString(new String(os.toByteArray(), "UTF-8"), sb);
			return sb.toString();
		}
		catch (Exception e)
		{
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
