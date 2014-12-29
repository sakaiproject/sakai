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
 * Matcher matches regular expressions (Pattern) to input @author stephan @team
 * sonicteam
 * 
 * @version $Id$
 */

public abstract class Matcher
{

	/**
	 * Create a new matcher object, depending on the implementation
	 * 
	 * @param input
	 *        Input to match regular expressions agains
	 * @param pattern
	 *        Regular expression pattern
	 * @return A Matcher implementation
	 */
	public static Matcher create(String input, Pattern pattern)
	{
		return new JdkMatcher(input, pattern);
	}

	/**
	 * Replace all matches in the input with a substitution. For every match
	 * substition.handleMatch is called.
	 * 
	 * @param substitution
	 *        Code which handles every substitution
	 * @return String with all matches substituted
	 */
	public abstract String substitute(Substitution substitution);

	/**
	 * Replace all matches in the input with a string substitution.
	 * 
	 * @param substitution
	 *        String to replace all matches
	 * @return String with all matches substituted
	 */
	public abstract String substitute(String substitution);

	/**
	 * Test if a regular expression matches the complete input
	 * 
	 * @return True if the regex matches the complete input
	 */
	public abstract boolean matches();

	/**
	 * Test if a regular expression matches parts of the input
	 * 
	 * @return True if the regex matches a part of the input
	 */
	public abstract boolean contains();
}