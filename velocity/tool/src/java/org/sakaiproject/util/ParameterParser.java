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

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

/**
 * ParameterParser is a wrapper over the request that provides compatibility with Sakai 1.5 and before.
 */
@Slf4j
public class ParameterParser
{
	/** The request. */
	protected HttpServletRequest m_req = null;

	/**
	 * Construct with this request.
	 * 
	 * @param req
	 *        The current request.
	 */
	public ParameterParser(HttpServletRequest req)
	{
		m_req = req;
	}

	/**
	 * Access the parameter names.
	 * 
	 * @return An Iterator of parameter names (String).
	 */
	public Properties getProperties()
	{
		Properties retval = new Properties();
		Iterator<String> names = getNames();
		while (names.hasNext())
		{
			String name = names.next();
			retval.setProperty(name,getString(name));
		}
		return retval;
	}

	/**
	 * Access the parameter names.
	 * 
	 * @return An Iterator of parameter names (String).
	 */
	public Iterator getNames()
	{
		return new EnumerationIterator(m_req.getParameterNames());
	}

	/**
	 * Get a (String) parameter by name.
	 * 
	 * @param name
	 *        The parameter name.
	 * @return The parameter value, or null if it's not defined.
	 */
	public String get(String name)
	{
		return m_req.getParameter(name);
	}

	/**
	 * Get a (String) parameter by name.
	 * 
	 * @param name
	 *        The parameter name.
	 * @return The parameter value, or null if it's not defined.
	 */
	public String getString(String name)
	{
		return get(name);
	}

	/**
	 * Get a (String[]) multi-valued parameter by name.
	 * 
	 * @param name
	 *        The parameter name.
	 * @return The parameter values array (of String), or null if it's not defined.
	 */
	public String[] getStrings(String name)
	{
		return m_req.getParameterValues(name);
	}

	/**
	 * Get a boolean parameter by name.
	 * 
	 * @param name
	 *        The parameter name.
	 * @return The parameter boolean value, or false if it's not defined.
	 */
	public boolean getBoolean(String name)
	{
		return "true".equalsIgnoreCase(get(name));
	}

	/**
	 * Get an int parameter by name, with default.
	 * 
	 * @param name
	 *        The parameter name.
	 * @param deflt
	 *        The default value.
	 * @return The parameter int value, or the default if it's not defined or not int.
	 */
	public int getInt(String name, int deflt)
	{
		try
		{
			return Integer.parseInt(get(name));
		}
		catch (Throwable t)
		{
			return deflt;
		}
	}

	/**
	 * Get an int parameter by name.
	 * 
	 * @param name
	 *        The parameter name.
	 * @return The parameter int value, or 0 if it's not defined or not int.
	 */
	public int getInt(String name)
	{
		return getInt(name, 0);
	}

	/**
	 * Clean the user input string of strange newlines, etc.
	 * 
	 * @param name
	 *        The user input string.
	 * @return value cleaned of string newlines, etc.
	 */
	public String getCleanString(String name)
	{
		String value = getString(name);
		if (value == null) return null;
		if (value.length() == 0) return value;

		final int len = value.length();
		StringBuilder buf = new StringBuilder();

		for (int i = 0; i < len; i++)
		{
			char c = value.charAt(i);
			char next = 0;
			if (i + 1 < len) next = value.charAt(i + 1);

			switch (c)
			{
				case '\r':
				{
					// detect CR LF, make it a \n
					if (next == '\n')
					{
						buf.append('\n');
						// eat the next character
						i++;
					}
					else
					{
						buf.append(c);
					}

				}
					break;

				default:
				{
					buf.append(c);
				}
			}
		}

		if (buf.charAt(buf.length() - 1) == '\n')
		{
			buf.setLength(buf.length() - 1);
		}

		return buf.toString();
	}

	/**
	 * Access the pathInfo.
	 * 
	 * @return The pathInfo.
	 */
	public String getPath()
	{
		return m_req.getPathInfo();
	}

	/**
	 * Get a FileItem parameter by name.
	 * 
	 * @param name
	 *        The parameter name.
	 * @return The parameter FileItem value, or null if it's not defined.
	 */
	public FileItem getFileItem(String name) {
		// wrap the Apache FileItem in our own homegrown FileItem
		Object o = m_req.getAttribute(name);
		if (o instanceof org.apache.commons.fileupload.FileItem)
		{
			org.apache.commons.fileupload.FileItem item = (org.apache.commons.fileupload.FileItem) o;
			try
			{
				return new FileItem(
						Normalizer.normalize(item.getName(), Normalizer.Form.NFC),
						item.getContentType(), item.getInputStream()
				);
			}
			catch (IOException e)
			{
				log.warn("Failed to get InputStream for file upload of {}", name, e);
			}
		}

		return null;
	}
	
}
