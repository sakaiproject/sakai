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
