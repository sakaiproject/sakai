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
 * Class which stores regular expressions Implementation for regex package in
 * JDK 1.4 @author stephan @team sonicteam
 * 
 * @version $Id$
 */

public class JdkPattern implements Pattern
{
	private String regex;

	private boolean multiline;

	private java.util.regex.Pattern internPattern;

	public JdkPattern(String regex, boolean multiline)
	{
		this.regex = regex;
		this.multiline = multiline;

		internPattern = java.util.regex.Pattern.compile(regex,
				java.util.regex.Pattern.DOTALL
						| (multiline ? java.util.regex.Pattern.MULTILINE : 0));
	}

	protected java.util.regex.Pattern getPattern()
	{
		return internPattern;
	}

	public String getRegex()
	{
		return regex;
	}

	public boolean getMultiline()
	{
		return multiline;
	}
}