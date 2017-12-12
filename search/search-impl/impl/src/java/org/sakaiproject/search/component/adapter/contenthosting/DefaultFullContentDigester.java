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

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.search.api.SearchUtils;

/**
 * @author ieb
 */
@Slf4j
public class DefaultFullContentDigester extends DefaultContentDigester
{

	private int maxDigestSize = 1024 * 1024 * 5; // 5M

	public String getContent(ContentResource contentResource)
	{
		if (contentResource == null) {
			throw new RuntimeException("null contentResource passed to getContent");
		}

		try
		{
			return SearchUtils.appendCleanString(new String(contentResource.getContent(),"UTF-8"), null).toString();
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
					contentStream, maxDigestSize);
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
		private int maxDigestSize;
		private int nread = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FilterReader#read()
		 */
		public int read() throws IOException
		{
			if  ( nread  > maxDigestSize ) {
				return -1;
			}
			char i = (char) super.read();
			nread++;
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
			if  ( nread  > maxDigestSize ) {
				return -1;
			}
			int size = super.read(buffer, start, end);
			nread += size;
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

		public FilterStreamReader(InputStream stream, int maxDigestSize)
		{
			super(new InputStreamReader(stream));
			inputStream = stream;
			this.maxDigestSize = maxDigestSize;
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
				log.debug(ex.getMessage());
			}
			try { 
				inputStream.close();
			} catch ( Exception ex ) {
				log.debug(ex.getMessage());
			}
			inputStream = null;
		}

	}

	/**
	 * @return Returns the maxDigestSize.
	 */
	public int getMaxDigestSize()
	{
		return maxDigestSize;
	}

	/**
	 * @param maxDigestSize The maxDigestSize to set.
	 */
	public void setMaxDigestSize(int maxDigestSize)
	{
		this.maxDigestSize = maxDigestSize;
	}

}
