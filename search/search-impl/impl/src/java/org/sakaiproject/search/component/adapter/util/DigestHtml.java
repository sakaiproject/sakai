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

import java.io.StringReader;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Digests XHTML into a string representation
 * 
 * @author ieb
 */
public class DigestHtml
{

	public static String digest(String todigest)
	{
		SimpleDigester d = new SimpleDigester();
		try
		{
			XMLReader reader = XMLReaderFactory
					.createXMLReader("org.apache.xerces.parsers.SAXParser");
			reader.setContentHandler(d);
			reader.parse(new InputSource(new StringReader("<content>"
					+ todigest + "</content>")));
			return d.toString();
		}
		catch (Exception ex)
		{
			return d.toString() + "\n Failed at " + ex.getMessage();
		}
	}

}
