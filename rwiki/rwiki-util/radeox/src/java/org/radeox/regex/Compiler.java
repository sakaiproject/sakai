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
 * Class that compiles regular expressions to patterns @author stephan @team
 * sonicteam
 * 
 * @version $Id$
 */

public abstract class Compiler
{
	/**
	 * Create a new Compiler object depending on the used implementation
	 * 
	 * @return Compiler object with the used implementation
	 */
	public static Compiler create()
	{
		return new JdkCompiler();
	}

	/**
	 * Whether the compiler should create multiline patterns or single line
	 * patterns.
	 * 
	 * @param multiline
	 *        True if the pattern is multiline, otherwise false
	 */
	public abstract void setMultiline(boolean multiline);

	/**
	 * Compile a String regular expression to a regex pattern
	 * 
	 * @param regex
	 *        String representation of a regular expression
	 * @return Compiled regular expression
	 */
	public abstract Pattern compile(String regex);
}