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
 * XmlCodeFilter colourizes Xml Code @author stephan @team sonicteam
 * 
 * @version $Id: XmlCodeFilter.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class XmlCodeFilter extends DefaultRegexCodeFormatter implements
		SourceCodeFormatter
{
	private static final String KEYWORDS = "\\b(xsl:[^&\\s]*)\\b";

	private static final String TAGS = "(&#60;/?.*?&#62;)";

	private static final String QUOTE = "\"(([^\"\\\\]|\\.)*)\"";

	public XmlCodeFilter()
	{
		super(QUOTE, "<span class=\"xml-quote\">\"$1\"</span>");
		addRegex(TAGS, "<span class=\"xml-tag\">$1</span>");
		addRegex(KEYWORDS, "<span class=\"xml-keyword\">$1</span>");
	}

	public String getName()
	{
		return "xml";
	}
}
