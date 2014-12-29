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

import java.text.MessageFormat;

import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.filter.context.FilterContext;
import org.radeox.filter.regex.LocaleRegexTokenFilter;
import org.radeox.regex.MatchResult;

/*
 * Transforms header style lines into subsections. A header starts with a 1 for
 * first level headers and 1.1 for secend level headers. Headers are numbered
 * automatically @author leo @team other
 * 
 * @version $Id: HeadingFilter.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class HeadingFilter extends LocaleRegexTokenFilter implements
		CacheFilter
{
	private MessageFormat formatter;

	protected String getLocaleKey()
	{
		return "filter.heading";
	}

	public void handleMatch(StringBuffer buffer, MatchResult result,
			FilterContext context)
	{
		buffer.append(handleMatch(result, context));
	}

	public void setInitialContext(InitialRenderContext context)
	{
		super.setInitialContext(context);
		String outputTemplate = outputMessages.getString(getLocaleKey()
				+ ".print");
		formatter = new MessageFormat("");
		formatter.applyPattern(outputTemplate);
	}

	public String handleMatch(MatchResult result, FilterContext context)
	{
		String name = "";
		char[] nameChars = result.group(3).toCharArray();
		int end = 0;
		for (int i = 0; i < nameChars.length; i++)
		{
			if (Character.isLetterOrDigit(nameChars[i]))
			{
				nameChars[end++] = nameChars[i];
			}
		}
		if (end > 0)
		{
			name = new String(nameChars, 0, end);
		}
		return formatter.format(new Object[] {
				result.group(1).replace('.', '-'), result.group(3), name });
	}
}
