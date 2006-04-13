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

package org.radeox.regex;

/*
 * Matcher matches regular expressions (Pattern) to input Implementation for
 * regex package in JDK 1.4 @author stephan @team sonicteam
 * 
 * @version $Id$
 */

public class JdkMatcher extends Matcher
{
	private JdkPattern pattern;

	private String input;

	private java.util.regex.Matcher internalMatcher;

	public String substitute(Substitution substitution)
	{
		MatchResult matchResult = new JdkMatchResult(internalMatcher);

		StringBuffer buffer = new StringBuffer();
		while (internalMatcher.find())
		{
			internalMatcher.appendReplacement(buffer, "");
			substitution.handleMatch(buffer, matchResult);
		}
		internalMatcher.appendTail(buffer);
		return buffer.toString();
	}

	public String substitute(String substitution)
	{
		return internalMatcher.replaceAll(substitution);
	}

	protected java.util.regex.Matcher getMatcher()
	{
		return internalMatcher;
	}

	public JdkMatcher(String input, Pattern pattern)
	{
		this.input = input;
		this.pattern = (JdkPattern) pattern;
		internalMatcher = this.pattern.getPattern().matcher(this.input);

	}

	public boolean contains()
	{
		internalMatcher.reset();
		return internalMatcher.find();
	}

	public boolean matches()
	{
		return internalMatcher.matches();
	}
}