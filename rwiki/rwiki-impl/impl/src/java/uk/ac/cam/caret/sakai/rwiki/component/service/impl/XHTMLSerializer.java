/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.util.HashMap;

import org.apache.xml.serializer.ToXMLStream;
import org.xml.sax.SAXException;

/**
 * @author andrew
 */
public class XHTMLSerializer extends ToXMLStream
{

	private static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

	private static HashMap emptyTag = new HashMap();

	static
	{
		// inclusion els
		emptyTag.put("img", "img");
		emptyTag.put("area", "area");
		emptyTag.put("frame", "frame");
		// non-standard inclusion els
		emptyTag.put("layer", "layer");
		emptyTag.put("embed", "embed");
		// form el
		emptyTag.put("input", "input");
		// default els
		emptyTag.put("base", "base");
		// styling els
		emptyTag.put("col", "col");
		emptyTag.put("basefont", "basefont");
		// hidden els
		emptyTag.put("link", "link");
		emptyTag.put("meta", "meta");
		// separator els
		emptyTag.put("br", "br");
		emptyTag.put("hr", "hr");

	}

	public void endElement(String namespaceURI, String localName, String name)
			throws SAXException
	{
		if ((namespaceURI != null && !"".equals(namespaceURI) && !namespaceURI
				.equals(XHTML_NAMESPACE))
				|| emptyTag.containsKey(localName.toLowerCase()))
		{
			super.endElement(namespaceURI, localName, name);
			return;
		}

		this.characters("");

		super.endElement(namespaceURI, localName, name);

	}

}
