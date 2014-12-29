/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.portal.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.tool.api.ToolURL;
import org.sakaiproject.tool.api.ToolURLManager;

/**
 * This is a default implementation of the ToolURLManager and it comes with a
 * default implementation of ToolURL for all three URL types (render, action and
 * resource). This implementation can be used by most portals that don't do any
 * special URL encoding.
 * 
 * @author <a href="mailto:vgoenka@sungardsct.com">Vishal Goenka</a>
 */
public class ToolURLManagerImpl implements ToolURLManager
{

	private HttpServletResponse m_response;

	private static final String PARAM_AMP = "&amp;";

	/**
	 * Constructor for ToolURLComponent
	 * 
	 * @param req
	 *        HttpServletRequest that the URLs will be generated for
	 * @param resp
	 *        HttpServletResponse that the URL will be encoded for
	 */

	public ToolURLManagerImpl(HttpServletResponse resp)
	{
		m_response = resp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.kernel.tool.ToolURLManager#createLinkURL()
	 */
	public ToolURL createRenderURL()
	{
		return new MyToolURL();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.kernel.tool.ToolURLManager#createActionURL()
	 */
	public ToolURL createActionURL()
	{
		return new MyToolURL();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.kernel.tool.ToolURLManager#createResourceURL()
	 */
	public ToolURL createResourceURL()
	{
		return new MyToolURL();
	}

	/**
	 * This is a simple implemention of ToolURL that does the most obvious
	 * encoding of URLs.
	 * 
	 * @author vgoenka
	 */
	public class MyToolURL implements ToolURL
	{
		protected String path;

		protected Map parameters;

		public MyToolURL()
		{
			path = "";
			parameters = new HashMap();
		}

		public void setPath(String path)
		{
			this.path = path;
		}

		public void setParameter(String name, String value)
		{
			if (value == null)
			{
				parameters.remove(name);
			}
			else
			{
				parameters.put(name, value);
			}
		}

		public void setParameter(String name, String[] values)
		{
			if ((values == null) || (values.length == 0))
			{
				parameters.remove(name);
			}
			else
			{
				parameters.put(name, values);
			}
		}

		public void setParameters(Map parameters)
		{
			this.parameters = parameters;
		}

		@Override
		public String toString()
		{
			StringBuilder rv = new StringBuilder(path);
			// Since we allow pre-formatted query strings to be added to the
			// path, it may already contain
			// some parameters
			String c = (path.indexOf('?') == -1) ? "?" : PARAM_AMP;
			if (parameters.size() > 0)
			{
				for (Iterator iEntries = parameters.entrySet().iterator(); iEntries
						.hasNext();)
				{
					Map.Entry entry = (Map.Entry) iEntries.next();
					String key = (String) entry.getKey();
					Object val = entry.getValue();
					if (val instanceof String[])
					{
						String[] values = (String[]) val;
						for (int i = 0; i < values.length; i++)
						{
							rv.append(c).append(key).append("=").append(values[i]);
							c = PARAM_AMP;
						}
					}
					else
					{
						rv.append(c).append(key).append("=").append((String) val);
						c = PARAM_AMP;
					}
				}
			}
			return m_response.encodeURL(rv.toString());
		}
	}

}
