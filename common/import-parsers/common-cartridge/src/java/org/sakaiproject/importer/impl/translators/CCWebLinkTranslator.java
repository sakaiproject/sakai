/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.XPathHelper;
import org.sakaiproject.importer.impl.importables.WebLink;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CCWebLinkTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		return "imswl_xmlv1p0";
	}

	public boolean processResourceChildren() {
		return false;
	}

	public Importable translate(Node resourceNode, Document descriptor,
			String contextPath, String archiveBasePath) {
		WebLink rv = new WebLink();
		String url = XPathHelper.getNodeValue("//url/@href", descriptor);
		String title = ((Element)resourceNode).getAttribute("title");
		int priority = Integer.parseInt(((Element)resourceNode).getAttribute("priority"));
		rv.setTitle(title);
		rv.setUrl(url);
		rv.setAbsolute(url.lastIndexOf("://") > 0);
		rv.setContextPath(contextPath + title);
		rv.setSequenceNum(priority);
		return rv;
	}

}
