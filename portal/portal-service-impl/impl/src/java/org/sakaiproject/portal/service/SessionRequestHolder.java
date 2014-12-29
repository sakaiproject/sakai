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

package org.sakaiproject.portal.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */

public class SessionRequestHolder
{
	private Map<String, List> headers;

	private String contextPath;

	private String method;

	private String queryString;

	private Map parameterMap;

	public SessionRequestHolder(HttpServletRequest request, String marker,
			String replacement)
	{
		headers = new HashMap<String, List>();
		Enumeration e = request.getHeaderNames();
		while (e.hasMoreElements())
		{
			String s = (String) e.nextElement();
			List v = new ArrayList();
			Enumeration e1 = request.getHeaders(s);
			while (e1.hasMoreElements())
			{
				v.add(e1.nextElement());
			}
			headers.put(s, v);
		}
		Map m = request.getParameterMap();
		parameterMap = new HashMap();
		for (Iterator<Entry<Object, Object>> i = m.entrySet().iterator(); i.hasNext();)
		{
			Entry<Object, Object> entry = i.next();
			parameterMap.put(entry.getKey(), entry.getValue());
		}
		contextPath = PortalStringUtil.replaceFirst(request.getContextPath(), marker,
				replacement);
		method = request.getMethod();
		queryString = request.getQueryString();
	}

	public String getContextPath()
	{
		return contextPath;
	}

	public long getDateHeader(String arg0)
	{
		try
		{
			SimpleDateFormat f = new SimpleDateFormat();
			Date d = f.parse(getHeader(arg0));
			return d.getTime();
		}
		catch (Throwable t)
		{
			return 0;
		}
	}

	public String getHeader(String arg0)
	{
		try
		{
			List v = (List) headers.get(arg0);
			return (String) v.get(0);
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	public Enumeration getHeaderNames()
	{
		final Iterator<String> i = headers.keySet().iterator();
		return new Enumeration()
		{

			public boolean hasMoreElements()
			{
				return i.hasNext();
			}

			public Object nextElement()
			{
				return i.next();
			}

		};
	}

	public Enumeration getHeaders(String arg0)
	{
		try
		{
			final Iterator i = headers.get(arg0).iterator();
			return new Enumeration()
			{
				public boolean hasMoreElements()
				{
					return i.hasNext();
				}

				public Object nextElement()
				{
					return i.next();
				}

			};
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	public int getIntHeader(String arg0)
	{
		try
		{
			return Integer.parseInt(getHeader(arg0));
		}
		catch (Throwable t)
		{
			return 0;
		}
	}

	public String getMethod()
	{
		return method;
	}

	public String getQueryString()
	{
		return queryString;
	}

	public String getParameter(String arg0)
	{
		Object o = parameterMap.get(arg0);
		if (o instanceof String[])
		{
			String[] s = (String[]) o;
			return s[0];
		}
		else if (o instanceof String)
		{
			return (String) o;
		}
		else if (o != null)
		{
			return o.toString();
		}
		else
		{
			return null;
		}
	}

	public Map getParameterMap()
	{
		return parameterMap;
	}

	public Enumeration getParameterNames()
	{
		final Iterator i = parameterMap.keySet().iterator();
		return new Enumeration()
		{

			public boolean hasMoreElements()
			{
				return i.hasNext();
			}

			public Object nextElement()
			{
				return i.next();
			}

		};
	}

	public String[] getParameterValues(String arg0)
	{
		Object o = parameterMap.get(arg0);
		if (o instanceof String[])
		{
			String[] s = (String[]) o;
			return s;
		}
		else if (o instanceof String)
		{
			return new String[] { (String) o };
		}
		else if (o != null)
		{
			return new String[] { o.toString() };
		}
		else
		{
			return null;
		}
	}
}
