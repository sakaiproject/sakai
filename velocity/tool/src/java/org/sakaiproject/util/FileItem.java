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

package org.sakaiproject.util;

import java.io.InputStream;

/**
 * FileItem is a file uploaded by the end user's browser.
 */
public class FileItem
{
	/** The chunk size used when streaming (100k). */
	protected static final int STREAM_BUFFER_SIZE = 102400;

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
		m_inputStream = null;
	}

	public FileItem(String fileName, String fileType, InputStream stream)
	{
		if (fileName != null) m_name = fileName.trim();
		if (fileType != null) m_type = fileType.trim();
		m_inputStream = stream;
	}

	public String getFileName()
	{
		return m_name;
	}

	public String getContentType()
	{
		return m_type;
	}

	/**
	 * @return the input stream from which the body can be read.
	 */
	public InputStream getInputStream()
	{
		return this.m_inputStream;
	}

}
