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

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;

/**
 * @author ieb
 */
public class DefaultContentDigester implements ContentDigester
{
	private static final Log log = LogFactory
			.getLog(DefaultContentDigester.class);

	public String getContent(ContentResource contentResource)
	{
		try
		{
			char[] content = (new String(contentResource.getContent()))
					.toCharArray();
			for (int i = 0; i < content.length; i++)
			{
				if (!Character.isLetterOrDigit(content[i]))
				{
					content[i] = ' ';
				}
			}
			return new String(content);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to get content", e);
		}
	}

	public Reader getContentReader(ContentResource contentResource)
	{ 
 		InputStream contentStream = null;
 		// we dont close this as its used to stream,
 		// the caller MUST close the stream
		try
		{
			contentStream = contentResource.streamContent();
			FilterStreamReader filterReader = new FilterStreamReader(
					contentStream);
			return filterReader;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to stream content ", e);
		}
		
	}

	public boolean accept(String mimeType)
	{
		return true;
	}

	public class FilterStreamReader extends FilterReader
	{

		private InputStream inputStream = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FilterReader#read()
		 */
		public int read() throws IOException
		{
			char i = (char) super.read();
			if (Character.isLetterOrDigit(i)) return i;
			return ' ';
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FilterReader#read(char[], int, int)
		 */
		public int read(char[] buffer, int start, int end) throws IOException
		{
			int size = super.read(buffer, start, end);
			int last = start + size;
			for (int i = size; i < last; i++)
			{
				if (!Character.isLetterOrDigit(buffer[i]))
				{
					buffer[i] = ' ';
				}
			}
			return size;
		}
		

		protected FilterStreamReader(Reader arg0)
		{
			super(arg0);
		}

		public FilterStreamReader(InputStream stream)
		{
			super(new InputStreamReader(stream));
			inputStream = stream;
		}

		/* (non-Javadoc)
		 * @see java.io.FilterReader#close()
		 */
		public void close() throws IOException
		{
			super.close();
			try {
				this.in.close();
			} catch ( Exception ex ) {
			}
			try { 
				inputStream.close();
			} catch ( Exception ex ) {
			}
			inputStream = null;
		}

	}

}
