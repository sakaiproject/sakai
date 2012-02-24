/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.component.adapter.util;

import java.util.HashMap;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @author ieb
 */
public class SimpleDigester implements ContentHandler
{

	private Stack stack = new Stack();

	private StringBuilder buffer = new StringBuilder();

	private HashMap tags = new HashMap();

	public static final String[] tagList = { "p", "\n", "div", "\n", "a", "\n",
			"span", " ", "td", "\n", "th", "\n", "li", "\n", "content", ""

	};

	public SimpleDigester()
	{
		for (int i = 0; i < tagList.length; i += 2)
		{
			tags.put(tagList[i], tagList[i + 1]);
		}
	}

	public String toString()
	{
		return buffer.toString();
	}

	public void setDocumentLocator(Locator arg0)
	{
	}

	public void startDocument() throws SAXException
	{
	}

	public void endDocument() throws SAXException
	{
	}

	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException
	{
	}

	public void endPrefixMapping(String arg0) throws SAXException
	{
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException
	{
		stack.push(tags.get(localName.toLowerCase()));
	}

	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException
	{
		String marker = (String) stack.pop();
		if (marker != null)
		{
			buffer.append(marker);
		}
	}

	public void characters(char[] arg0, int arg1, int arg2) throws SAXException
	{
		if (stack.peek() != null)
		{
			String s = new String(arg0, arg1, arg2);
			buffer.append(s.trim()).append(" ");
		}

	}

	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException
	{

	}

	public void processingInstruction(String arg0, String arg1)
			throws SAXException
	{

	}

	public void skippedEntity(String arg0) throws SAXException
	{

	}
}
