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
package org.radeox.filter;

import java.util.Map;

import org.radeox.filter.context.FilterContext;
import org.radeox.filter.regex.LocaleRegexTokenFilter;
import org.radeox.regex.MatchResult;

/*
 * ParamFilter replaces parametes from from the MacroFilter in the input. These
 * parameters could be read from an HTTP request and put in MacroFilter. A
 * parameter is replaced in {$paramName} @author stephan @team sonicteam
 * 
 * @version $Id$
 */

public class ParamFilter extends LocaleRegexTokenFilter
{
	public void handleMatch(StringBuffer buffer, MatchResult result,
			FilterContext context)
	{
		Map param = context.getRenderContext().getParameters();

		String name = result.group(1);
		if (param.containsKey(name))
		{
			Object value = param.get(name);
			if (value instanceof String[])
			{
				buffer.append(((String[]) value)[0]);
			}
			else
			{
				buffer.append(value);
			}
		}
		else
		{
			buffer.append("<");
			buffer.append(name);
			buffer.append(">");
		}
	}

	protected String getLocaleKey()
	{
		return "filter.param";
	}

	protected boolean isSingleLine()
	{
		return true;
	}
}