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

package org.radeox.filter.regex;

import lombok.extern.slf4j.Slf4j;
import org.radeox.filter.context.FilterContext;
import org.radeox.regex.MatchResult;
import org.radeox.regex.Matcher;
import org.radeox.regex.Pattern;
import org.radeox.regex.Substitution;

/*
 * Filter that calls a special handler method handleMatch() for every occurance
 * of a regular expression. @author stephan @team sonicteam
 * 
 * @version $Id: RegexTokenFilter.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */
@Slf4j
public abstract class RegexTokenFilter extends RegexFilter
{

	public RegexTokenFilter()
	{
		super();
	}

	/**
	 * create a new regular expression and set
	 */
	public RegexTokenFilter(String regex, boolean multiline)
	{
		super(regex, "", multiline);
	}

	/**
	 * create a new regular expression and set
	 */
	public RegexTokenFilter(String regex)
	{
		super(regex, "");
	}

	protected void setUp(FilterContext context)
	{
	}

	/**
	 * Method is called for every occurance of a regular expression. Subclasses
	 * have to implement this mehtod.
	 * 
	 * @param buffer
	 *        Buffer to write replacement string to
	 * @param result
	 *        Hit with the found regualr expression
	 * @param context
	 *        FilterContext for filters
	 */
	public abstract void handleMatch(StringBuffer buffer, MatchResult result,
			FilterContext context);

	public String filter(String input, final FilterContext context)
	{
		setUp(context);

		String result = null;
		int size = pattern.size();
		for (int i = 0; i < size; i++)
		{
			Pattern p = (Pattern) pattern.get(i);
			try
			{
				Matcher m = Matcher.create(input, p);
				result = m.substitute(new Substitution()
				{
					public void handleMatch(StringBuffer buffer,
							MatchResult result)
					{
						RegexTokenFilter.this.handleMatch(buffer, result,
								context);
					}
				});

				// result = Util.substitute(matcher, p, new
				// ActionSubstitution(s, this, context), result, limit);
			}
			catch (Exception e)
			{
				log.warn("<span class=\"error\">Exception</span>: " + this, e);
			}
			catch (Error err)
			{
				log.warn("<span class=\"error\">Error</span>: " + this + ": "
						+ err);
			}
			input = result;
		}
		return input;
	}
}
