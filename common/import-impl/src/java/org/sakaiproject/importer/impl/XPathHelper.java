/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.importer.impl;

import java.util.ArrayList;
import java.util.List;

import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Node;

public class XPathHelper {
	
	public static List selectNodes(String expression, Object doc) {
		if (doc == null) return new ArrayList();
		List nodes = null;
		try {
			XPath xpath = new DOMXPath(expression);
			nodes = xpath.selectNodes(doc);
		} catch (JaxenException e) {
			return null;
		}
		return nodes;  
	}
	
	public static Node selectNode(String expression, Object doc) {
		if (doc == null) return null;
		Object node = null;
		try {
			XPath xpath = new DOMXPath(expression);
			node = xpath.selectSingleNode(doc);
		} catch (JaxenException e) {
			return null;
		}
		return (Node)node;  
	}
	
	public static String getNodeValue(String expression, Node node) {
		if (node == null) return "";
		String value = null;
		try {
			XPath xpath = new DOMXPath(expression);
			value = xpath.stringValueOf(node);
		} catch (JaxenException e) {
			return null;
		}
		return value;
	}

}
