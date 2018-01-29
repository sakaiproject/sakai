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
import org.radeox.regex.Matcher;
import org.radeox.regex.Pattern;

/*
 * Class that applies a RegexFilter, can be subclassed for special Filters.
 * Regular expressions in the input are replaced with strings. @author stephan
 * @team sonicteam
 * 
 * @version $Id: RegexReplaceFilter.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */
@Slf4j
public class RegexReplaceFilter extends RegexFilter
{

	public RegexReplaceFilter()
	{
		super();
	}

	public RegexReplaceFilter(String regex, String substitute)
	{
		super(regex, substitute);
	}

	public RegexReplaceFilter(String regex, String substitute, boolean multiline)
	{
		super(regex, substitute, multiline);
	}

	public String filter(String input, FilterContext context)
	{
		String result = input;
		int size = pattern.size();
		Pattern p;
		String s;
		for (int i = 0; i < size; i++)
		{
			p = (Pattern) pattern.get(i);
			s = (String) substitute.get(i);
			try
			{
				Matcher matcher = Matcher.create(result, p);
				result = matcher.substitute(s);

				// Util.substitute(matcher, p, new Perl5Substitution(s,
				// interps), result, limit);
			}
			catch (Exception e)
			{
				// log.warn("<span class=\"error\">Exception</span>: " + this +
				// ": " + e);
				log.warn("Exception for: " + this + " " + e);
			}
			catch (Error err)
			{
				// log.warn("<span class=\"error\">Error</span>: " + this + ": "
				// + err);
				log.warn("Error for: " + this);
			}
		}
		return result;
	}
}
