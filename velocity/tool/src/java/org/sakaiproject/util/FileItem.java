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

package org.sakaiproject.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * <p>
 * FileItem is ...
 * </p>
 */
public class FileItem
{
	/** Body stored in memory, filled in using this stream. */
	protected ByteArrayOutputStream m_body = new ByteArrayOutputStream();

	protected byte[] m_bodyBytes = null;

	/** file name. */
	protected String m_name = null;

	/** file type. */
	protected String m_type = null;

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
	}

	public FileItem(String fileName, String fileType, byte[] body)
	{
		if (fileName != null) m_name = fileName.trim();
		if (fileType != null) m_type = fileType.trim();
		m_body = null;
		m_bodyBytes = body;
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
	 */
	OutputStream getOutputStream()
	{
		return m_body;
	}
}
