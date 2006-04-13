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

package org.radeox.macro.code;

/*
 * JavaCodeFilter colourizes Java source code @author stephan @team sonicteam
 * 
 * @version $Id: JavaCodeFilter.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class JavaCodeFilter extends DefaultRegexCodeFormatter implements
		SourceCodeFormatter
{

	private static final String KEYWORDS = "\\b(abstract|break|byvalue|case|cast|catch|"
			+ "const|continue|default|do|else|extends|"
			+ "false|final|finally|for|future|generic|goto|if|"
			+ "implements|import|inner|instanceof|interface|"
			+ "native|new|null|operator|outer|package|private|"
			+ "protected|public|rest|return|static|super|switch|"
			+ "synchronized|this|throw|throws|transient|true|try|"
			+ "var|volatile|while)\\b";

	private static final String OBJECTS = "\\b(Boolean|Byte|Character|Class|ClassLoader|Cloneable|Compiler|"
			+ "Double|Float|Integer|Long|Math|Number|Object|Process|"
			+ "Runnable|Runtime|SecurityManager|Short|String|StringBuffer|"
			+ "System|Thread|ThreadGroup|Void|boolean|char|byte|short|int|long|float|double)\\b";

	private static final String QUOTES = "\"(([^\"\\\\]|\\.)*)\"";

	public JavaCodeFilter()
	{
		super(QUOTES, "<span class=\"java-quote\">\"$1\"</span>");
		addRegex(KEYWORDS, "<span class=\"java-keyword\">$1</span>");
		addRegex(OBJECTS, "<span class=\"java-object\">$1</span>");
	}

	public String getName()
	{
		return "java";
	}
}