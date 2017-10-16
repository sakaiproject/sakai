/**
 * Copyright (c) 2005-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.impl.importables.HtmlDocument;
import org.sakaiproject.importer.impl.XPathHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Bb6StaffInfoTranslator implements IMSResourceTranslator{

	public String getTypeName() {
		return "resource/x-bb-staffinfo";
	}

	public Importable translate(Node resourceNode, Document descriptor, String contextPath, String archiveBasePath) {
		HtmlDocument rv = new HtmlDocument();
		String lastName = XPathHelper.getNodeValue("/STAFFINFO/CONTACT/NAME/FAMILY/@value", descriptor);
		String firstName = XPathHelper.getNodeValue("/STAFFINFO/CONTACT/NAME/GIVEN/@value", descriptor);
		String email = XPathHelper.getNodeValue("/STAFFINFO/CONTACT/EMAIL/@value", descriptor);
		String phone = XPathHelper.getNodeValue("/STAFFINFO/CONTACT/PHONE/@value", descriptor);
		String bio = XPathHelper.getNodeValue("/STAFFINFO/BIOGRAPHY/TEXT", descriptor);
		String address = XPathHelper.getNodeValue("/STAFFINFO/CONTACT/OFFICE/ADDRESS/@value", descriptor);
		String hours = XPathHelper.getNodeValue("/STAFFINFO/CONTACT/OFFICE/HOURS/@value", descriptor);
		String image = XPathHelper.getNodeValue("/STAFFINFO/IMAGE/@value", descriptor);
		String homepage = XPathHelper.getNodeValue("/STAFFINFO/HOMEPAGE/@value", descriptor);
		String title = (firstName + " " + lastName);
		int priority = Integer.parseInt(((Element)resourceNode).getAttribute("priority"));
		StringBuffer page = new StringBuffer();
		page.append("<html><head><title>" + title + "</title></head>\n");
		page.append("<body><h1>" + title + "</h1>\n");
		page.append("<p>" + bio +"</p>\n");
		page.append("<p>email: <a href=\"mailto:" + email + "\">" + email + "</a></p>\n");
		page.append("<p>phone: " + phone + "</p>\n");
		page.append("<p>office: " + address + "</p>\n");
		page.append("<p>hours: " + hours + "</p>\n");
		page.append("<p>homepage: <a href=\"" + homepage + "\">" + homepage + "</a></p>\n");
		if ((image != null) && !("".equals(image))) {
			page.append("<img src='" + image + "' />\n");
		}
		page.append("</body></html>\n");
		contextPath = contextPath + title;
		rv.setContent(page.toString().replaceAll("&lt;", "<"));
		rv.setTitle(title);
		rv.setContextPath(contextPath);
		rv.setSequenceNum(priority);
		return rv;
	}

	public boolean processResourceChildren() {
		return true;
	}

}
