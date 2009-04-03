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

package org.sakaiproject.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * <p>
 * FileItem is ...
 * </p>
 */
public class FileItem
{
	/** The chunk size used when streaming (100k). */
	protected static final int STREAM_BUFFER_SIZE = 102400;

	/** Body stored in memory, filled in using this stream. */
	protected ByteArrayOutputStream m_body = new ByteArrayOutputStream();

	protected byte[] m_bodyBytes = null;

	/** file name. */
	protected String m_name = null;

	/** file type. */
	protected String m_type = null;

	/** Stream from which body can be read */ 
	protected InputStream m_inputStream;

	/**
	 * Construct
	 * 
	 * @param fileName
	 *        The file name.
	 * @param fileType
	 *        The file type.
	 */
	public FileItem(String fileName, String fileType)
	{
		if (fileName != null) m_name = fileName.trim();
		if (fileType != null) m_type = fileType.trim();
		m_body = null;
		m_bodyBytes = null;
		m_inputStream = null;
	}

	public FileItem(String fileName, String fileType, byte[] body)
	{
		if (fileName != null) m_name = fileName.trim();
		if (fileType != null) m_type = fileType.trim();
		m_body = null;
		m_bodyBytes = body;
		m_inputStream = null;
	}

	public FileItem(String fileName, String fileType, InputStream stream)
	{
		if (fileName != null) m_name = fileName.trim();
		if (fileType != null) m_type = fileType.trim();
		m_body = null;
		m_bodyBytes = null;
		m_inputStream = stream;
	}

	/**
	 */
	public String getFileName()
	{
		return m_name;
	}

	/**
	 */
	public String getContentType()
	{
		return m_type;
	}

	/**
	 * Access the body as a String.
	 */
	public String getString()
	{
		String rv = null;
		
		if(m_body == null && m_bodyBytes == null && this.m_inputStream != null)
		{
			stream2bodyBytes();
		}
		
		try
		{
			// this should give us byte for byte translation, no encoding/decoding
			if (m_body != null)
			{
				rv = m_body.toString("ISO8859_1");
			}
			else
			{
				rv = new String(m_bodyBytes, "ISO8859_1");
			}
		}
		catch (UnsupportedEncodingException ignore)
		{
		}

		m_body = null;
		m_bodyBytes = null;

		return rv;
	}

	/**
	 * Access the body as a byte array. This consumes the entry.
	 */
	public byte[] get()
	{
		if(m_body == null && m_bodyBytes == null && this.m_inputStream != null)
		{
			stream2bodyBytes();
		}
		
		if (m_body != null)
		{
			byte[] content = m_body.toByteArray();
			m_body = null;
			return content;
		}
		else
		{
			byte[] content = m_bodyBytes;
			m_bodyBytes = null;
			return content;
		}
	}
	
	/**
	 * Access the input stream from which the body can be read.
	 * @return
	 */
	public InputStream getInputStream()
	{
		return this.m_inputStream;
	}

	/**
	 */
	OutputStream getOutputStream()
	{
		return m_body;
	}
	
	protected void stream2bodyBytes()
	{
		if(this.m_inputStream != null)
		{
			m_body = new ByteArrayOutputStream();
			
			// chunk
			byte[] chunk = new byte[STREAM_BUFFER_SIZE];
			int lenRead;
			try
            {
	            while ((lenRead = this.m_inputStream.read(chunk)) != -1)
	            {
	            	m_body.write(chunk, 0, lenRead);
	            }
            }
            catch (IOException ignoree)
            {
            }
            finally
            {
            	if(m_inputStream != null)
            	{
            		try
                    {
            			m_inputStream.close();
            			m_inputStream = null;
                    }
                    catch (IOException e)
                    {
                    }
            	}
            }


		}
	}
}
