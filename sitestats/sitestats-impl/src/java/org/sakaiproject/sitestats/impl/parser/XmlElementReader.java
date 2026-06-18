/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class XmlElementReader {

	private XmlElementReader() {
	}

	static Element parseRoot(InputStream input, String expectedRootName) throws Exception {
		Document document = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder().parse(input);
		Element root = document.getDocumentElement();
		if(!expectedRootName.equals(root.getTagName())) {
			throw new Exception("Expected " + expectedRootName + " root element.");
		}
		return root;
	}

	static String requiredValue(Element element, String name, String tagName) throws Exception {
		String value = optionalValue(element, name);
		if(value == null) {
			throw new Exception("Mandatory '" + name + "' value not present on " + tagName + " tag.");
		}
		return value;
	}

	static String optionalValue(Element element, String name) {
		if(element.hasAttribute(name)) {
			return element.getAttribute(name);
		}
		Element child = getDirectChild(element, name);
		if(child == null) {
			return null;
		}
		String value = child.getTextContent();
		return value == null ? null : value.trim();
	}

	static boolean optionalBoolean(Element element, String name, boolean defaultValue) throws Exception {
		String value = optionalValue(element, name);
		if(value == null) {
			return defaultValue;
		}
		return parseBoolean(value, element.getTagName(), name);
	}

	static float optionalFloat(Element element, String name, float defaultValue) throws Exception {
		String value = optionalValue(element, name);
		if(value == null) {
			return defaultValue;
		}
		try {
			return Float.parseFloat(value);
		} catch(NumberFormatException e) {
			throw new Exception("Invalid float value '" + value + "' for '" + name + "' on " + element.getTagName() + " tag.", e);
		}
	}

	static Element getDirectChild(Element element, String tagName) {
		for(Element child : getDirectChildren(element, tagName)) {
			return child;
		}
		return null;
	}

	static List<Element> getDirectChildren(Element element, String tagName) {
		List<Element> children = new ArrayList<Element>();
		NodeList childNodes = element.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if(childNode instanceof Element) {
				Element child = (Element) childNode;
				if(tagName.equals(child.getTagName())) {
					children.add(child);
				}
			}
		}
		return children;
	}

	private static boolean parseBoolean(String value, String tagName, String name) throws Exception {
		if("true".equalsIgnoreCase(value)) {
			return true;
		}
		if("false".equalsIgnoreCase(value)) {
			return false;
		}
		throw new Exception("Invalid boolean value '" + value + "' for '" + name + "' on " + tagName + " tag.");
	}
}
