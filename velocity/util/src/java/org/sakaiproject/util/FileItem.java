/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * <p>FileItem is ...</p>
 * 
 * @author University of Michigan, CHEF Software Development Team
 * @version $Revision$
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
	 * @param fileName The file name.
	 * @param fileType The file type.
	 */
	public FileItem(String fileName, String fileType)
	{
		if (fileName != null)
			m_name = fileName.trim();
		if (fileType != null)
			m_type = fileType.trim();
	}

	public FileItem(String fileName, String fileType, byte[] body)
	{
		if (fileName != null)
			m_name = fileName.trim();
		if (fileType != null)
			m_type = fileType.trim();
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
		catch (UnsupportedEncodingException ignore) {}

		m_body = null;
		m_bodyBytes = null;

		return rv;
	}

	/**
	 * Access the body as a byte array.  This consumes the entry.
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



