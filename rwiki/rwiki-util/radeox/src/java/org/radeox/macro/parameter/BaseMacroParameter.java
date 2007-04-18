/*
 * This file is part of "SnipSnap Radeox Rendering Engine".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://radeox.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --LICENSE NOTICE--
 */

package org.radeox.macro.parameter;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.radeox.api.engine.context.RenderContext;
import org.radeox.api.macro.MacroParameter;

/**
 * @author
 * @version $Id: BaseMacroParameter.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class BaseMacroParameter implements MacroParameter
{
	private String content;

	protected Map params;

	private int size;

	protected RenderContext context;

	private int start;

	private int end;

	private int contentStart;

	private int contentEnd;

	public BaseMacroParameter()
	{
	}

	public BaseMacroParameter(RenderContext context)
	{
		this.context = context;
	}

	public void setParams(String stringParams)
	{
		params = split(stringParams, "|");
		size = params.size();
	}

	public RenderContext getContext()
	{
		return context;
	}

	public Map getParams()
	{
		return params;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public int getLength()
	{
		return size;
	}

	public String get(String index, int idx)
	{
		String result = get(index);
		if (result == null)
		{
			result = get(idx);
		}
		return result;
	}

	public String get(String index)
	{
		return (String) params.get(index);
	}

	public String get(int index)
	{
		return get("" + index);
	}

	/**
	 * Splits a String on a delimiter to a List. The function works like the
	 * perl-function split.
	 * 
	 * @param aString
	 *        a String to split
	 * @param delimiter
	 *        a delimiter dividing the entries
	 * @return a Array of splittet Strings
	 */

	private Map split(String aString, String delimiter)
	{
		Map result = new HashMap();

		if (null != aString)
		{
			StringTokenizer st = new StringTokenizer(aString, delimiter);
			int i = 0;

			while (st.hasMoreTokens())
			{
				String value = st.nextToken();
				String key = "" + i;
				if (value.indexOf("=") != -1)
				{
					// Store this for
					result.put(key, insertValue(value));
					int index = value.indexOf("=");
					key = value.substring(0, index);
					value = value.substring(index + 1);

					result.put(key, insertValue(value));
				}
				else
				{
					result.put(key, insertValue(value));
				}
				i++;
			}
		}
		return result;
	}

	private String insertValue(String s)
	{
		int idx = s.indexOf('$');
		if (idx != -1)
		{
			StringBuffer tmp = new StringBuffer();
			Map globals = context.getParameters();
			String var = s.substring(idx + 1);
			if (idx > 0) tmp.append(s.substring(0, idx));
			if (globals.containsKey(var))
			{
				tmp.append(globals.get(var));
			}
			return tmp.toString();
		}
		return s;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public void setEnd(int end)
	{
		this.end = end;
	}

	public int getStart()
	{
		return this.start;
	}

	public int getEnd()
	{
		return this.end;
	}

	public int getContentStart()
	{
		return contentStart;
	}

	public void setContentStart(int contentStart)
	{
		this.contentStart = contentStart;
	}

	public int getContentEnd()
	{
		return contentEnd;
	}

	public void setContentEnd(int contentEnd)
	{
		this.contentEnd = contentEnd;
	}

}
