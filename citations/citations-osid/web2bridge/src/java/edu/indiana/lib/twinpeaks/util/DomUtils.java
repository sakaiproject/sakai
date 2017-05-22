/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.util;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.*;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;
import org.xml.sax.*;

@Slf4j
public class DomUtils
{
	/**
	 * Default encoding (NekoHTML)
	 */
  private static final String ENCODING_OPTION =
      "http://cyberneko.org/html/properties/default-encoding";

  private DomUtils() {
  }

  public final static String INPUT_ENCODING 	= "iso-8859-1";
  public final static String ENCODING 		= "UTF-8";

  /**
   * Create a new element
   * @param document Document to contain the new element
   * @param name the element name
   * @return new Element
   */
  public static Element createElement(Document document, String name) {
    Element element;

    return document.createElement(name);
  }

  /**
   * Add a new element to the given parent
   * @param parent the parent Element
   * @param name the child name
   * @return new Element
   */
  public static Element createElement(Element parent, String name) {
    Document document;
    Element element;

    document = parent.getOwnerDocument();
    element  = document.createElement(name);

    parent.appendChild(element);
    return element;
  }

  /**
   * Add Text object to an Element.
   * @param element the containing element
   * @param text the text to add
   */
  public static void addText(Element element, String text) {
    element.appendChild(element.getOwnerDocument().createTextNode(text));
  }

 /**
   * Add an entity to a specified Element.
   *		(eg <code>DomUtils.addEntity(element, "nbsp");</code>)
   * @param element the containing element
   * @param entity the entity to add
   */
  public static void addEntity(Element element, String entity) {
    element.appendChild(element.getOwnerDocument().createEntityReference(entity));
  }

  /**
   * "Normalize" XML text node content to create a simple string
   * @param update Text to add to the original string
   * @return Concatenated contents (trimmed, pagination characters (\r, \n, etc.)
   *         removed, with a space seperator)
   */
  public static String normalizeText(String update) {
    return normalizeText(null, update);
  }

  /**
   * "Normalize" XML text node content to create a simple string
   * @param original Original text
   * @param update Text to add to the original string
   * @return Concatenated contents (trimmed, pagination characters (\r, \n, etc.)
   *         removed, with a space seperator)
   */
  public static String normalizeText(String original, String update) {
  	StringBuilder	result;

    if (original == null) {
      return (update == null) ? ""
      												: StringUtils.replace(update.trim(), "\\s", " ");
    }

    result = new StringBuilder(original.trim());
    result.append(' ');
    result.append(update.trim());

    return StringUtils.replace(result.toString(), "\\s", " ");
  }

  /**
   * Get the text associated with this element, at this level only
   * @param parent the node containing text
   * @return Text (trimmed of leading/trailing whitespace, null if none)
   */
  public static String getText(Node parent)
  {
    return textSearch(parent, false);
  }

  /**
   * Get the text associated with a specified element, at this level only
   * @param parent the node containing text
   * @param elementName Element with the text we want to fetch
   * @return Text (trimmed of leading/trailing whitespace, null if none)
   */
  public static String getText(Node parent, String elementName)
  {
    Element element = getElement((Element) parent, elementName);

    if (element == null)
    {
      return null;
    }
    return textSearch(element, false);
  }

  /**
   * Get the text associated with a specified element, at this level
   * only - namespace aware
   *
   * @param namespace Namespace URI
   * @param parent the node containing text
   * @param elementName Element with the text we want to fetch
   * @return Text (trimmed of leading/trailing whitespace, null if none)
   */
  public static String getTextNS(String namespace, Node parent, String elementName)
  {
    Element element = getElementNS(namespace, (Element) parent, elementName);

    if (element == null)
    {
      return null;
    }
    return textSearch(element, false);
  }

  /**
   * Get the text associated with a specified element, at this level
   * only - namespace aware
   *
   * @param namespace Namespace URI
   * @param parent the node containing text
   * @param elementName Element with the text we want to fetch
   * @return Text (trimmed of leading/trailing whitespace, null if none)
   */
/*
  public static String getTextNS(String namespace, Element parent, String elementName)
  {
    Element element = getElementNS(namespace, parent, elementName);

    if (element == null)
    {
      return null;
    }
    return textSearch(element, false);
  }
*/
  /**
   * Get the text associated with this element, at all suboordinate levels
   * @param parent the node containing text
   * @return Text (trimmed of leading/trailing whitespace, null if none)
   */
  public static String getAllTextAtNode(Node parent) {
    return textSearch(parent, true);
  }

  /**
   * Get the text associated with this element at this level only, or
   * recursivley, searching through all child elements
   * @param parent the node containing text
   * @param recursiveSearch Search all child elements?
   * @return Text (trimmed of leading/trailing whitespace, null if none)
   */
  public static String textSearch(Node parent, boolean recursiveSearch) {
    String text = null;

    if (parent != null) {
      for (Node child = parent.getFirstChild();
                child != null;
                child = child.getNextSibling()) {

        switch (child.getNodeType())
        {
          case Node.TEXT_NODE:
            text = normalizeText(text, child.getNodeValue());
            break;

          case Node.ELEMENT_NODE:
            if (recursiveSearch) {
              text = normalizeText(text, getText(child));
            }
            break;

          default:
            break;
        }
      }
    }
    return text == null ? text : text.trim();
  }

  /**
   * Get the first text node associated with this element
   * @param parent the node containing text
   * @return Text (trimmed of leanding/trailing whitespace, null if none)
   */
  public static String getFirstText(Node parent) {
    return getTextNodeByNumber(parent, 1);
  }

  /**
   * Get the specified text node associated with this element
   * @param parent the node containing text
   * @param number The text node to fetch (1st, 2nd, etc)
   * @return Text (trimmed of leanding/trailing whitespace, null if none)
   */
  public static String getTextNodeByNumber(Node parent, int number) {
    String  text  = null;
    int     count = 1;

    if (parent != null) {
      for (Node child = parent.getFirstChild();
                child != null;
                child = child.getNextSibling()) {

        if ((child.getNodeType() == Node.TEXT_NODE) && (count++ == number)) {
          text = child.getNodeValue();
          return text.trim();
        }
      }
    }
    return text;
  }

  /**
   * Get any text associated with this element and it's children.  Null if none.
   * @param parent the node containing text
   * @return Text
   */
  public static String getAllText(Node parent) {
    String text = null;

    if (parent != null) {

      for (Node child = parent.getFirstChild();
                child != null;
                child = child.getNextSibling()) {

        if (child.getNodeType() == Node.TEXT_NODE) {
          text = normalizeText(text, child.getNodeValue());
          continue;
        }

        if (child.getNodeType() == Node.ELEMENT_NODE) {
          String childText = getText(child);

          if (childText != null) {
            text = normalizeText(text, childText);
          }
        }
      }
    }
    return text;
  }

  /**
   * Get an Attribute from an Element.  Returns an empty String if none found
   * @param element the containing Element
   * @param name the attribute name
   * @return Attribute as a String
   */
  public static String getAttribute(Element element, String name) {
    return element.getAttribute(name);
  }

  /**
   * Set an Attribute in an Element
   * @param element the containing Element
   * @param name the attribute name
   * @param value the attribute value
   */
  public static void setAttribute(Element element, String name, String value) {
    element.setAttribute(name, value);
  }

  /**
   * Return a list of named Elements.
   * @param element the containing Element
   * @param name the tag name
   * @return NodeList of matching elements
   */
  public static NodeList getElementList(Element element, String name) {
    return element.getElementsByTagName(name);
  }

  /**
   * Return a list of specified namespace:Elements
   * @param namespace Namespace URI
   * @param element the containing Element
   * @param name the tag name
   * @return NodeList of matching elements
   */
  public static NodeList getElementListNS(String namespace, Element element, String name)
  {
    return element.getElementsByTagNameNS(namespace, name);
  }

  /**
   * Return a list of named Elements with a specific attribute value.
   * @param element the containing Element
   * @param name the tag name
   * @param attribute Attribute name
   * @param value Attribute value
   * @return List of matching elements
   */
  public static List selectElementsByAttributeValue(Element element, String name,
  																									String attribute, String value) {
		return selectElementsByAttributeValue(element, name,
																				  attribute, value, false);
	}

  /**
   * Return the first named Element with a specific attribute value.
   * @param element the containing Element
   * @param name the tag name
   * @param attribute Attribute name
   * @param value Attribute value
   * @return The first matching Element (null if none)
   */
  public static Element selectFirstElementByAttributeValue(Element 	element,
  																												 String 	name,
  																												 String 	attribute,
  																											   String 	value) {

		ArrayList	resultList = (ArrayList) selectElementsByAttributeValue(element, name,
																																		  attribute, value,
																																		  true);
		return (resultList.size() == 0) ? null : (Element) resultList.get(0);
	}

  /**
   * Return a list of named Elements with a specific attribute value.
   * @param element the containing Element
   * @param name the tag name
   * @param attribute Attribute name
   * @param value Attribute value
   * @param returnFirst Return only the first matching value?
   * @return List of matching elements
   */
  public static List selectElementsByAttributeValue(Element element, String name,
  																									String attribute, String value,
  																									boolean returnFirst) {
    NodeList 	elementList = element.getElementsByTagName(name);
    List 			resultList	= new ArrayList();

    for (int i = 0; i < elementList.getLength(); i++) {
    	if (getAttribute((Element) elementList.item(i), attribute).equals(value)) {
    		resultList.add(elementList.item(i));
    		if (returnFirst) {
    			break;
    		}
    	}
    }
		return resultList;
  }

  /**
   * Return a list of named Elements with a specific attribute
   * value (namespace aware)
   *
   * @param namespace Namespace URI
   * @param element the containing Element
   * @param name the tag name
   * @param attribute Attribute name
   * @param value Attribute value
   * @return List of matching elements
   */
  public static List selectElementsByAttributeValueNS(String namespace,
                                                      Element element,
                                                      String name,
  																									  String attribute,
  																									  String value)
  {
		return selectElementsByAttributeValueNS(namespace, element, name,
																				    attribute, value, false);
	}

  /**
   * Return the first named Element with a specific attribute
   * value (namespace aware)
   *
   * namespace The namespace URI
   * @param element the containing Element
   * @param name the tag name
   * @param attribute Attribute name
   * @param value Attribute value
   * @return The first matching Element (null if none)
   */
  public static Element selectFirstElementByAttributeValueNS(String   namespace,
                                                             Element 	element,
  																												   String 	name,
  																												   String 	attribute,
  																											     String 	value)
  {
		ArrayList	resultList;

		resultList = (ArrayList) selectElementsByAttributeValueNS(namespace,
		                                                          element,
		                                                          name, attribute,
																														  value, true);

		return (resultList.size() == 0) ? null : (Element) resultList.get(0);
	}

  /**
   * Return a list of named Elements with a specific attribute
   * value (namespace aware)
   *
   * @param namespace Namespace URI
   * @param element the containing Element
   * @param name the tag name
   * @param attribute Attribute name
   * @param value Attribute value
   * @param returnFirst Return only the first matching value?
   * @return List of matching elements
   */
  public static List selectElementsByAttributeValueNS(String namespace,
                                                      Element element,
                                                      String name,
  																									  String attribute,
  																									  String value,
  																									  boolean returnFirst)
  {
    NodeList 	elementList = element.getElementsByTagNameNS(namespace, name);
    List 			resultList	= new ArrayList();

    for (int i = 0; i < elementList.getLength(); i++)
    {
    	if (getAttribute((Element) elementList.item(i), attribute).equals(value))
    	{
    		resultList.add(elementList.item(i));
    		if (returnFirst)
    		{
    			break;
    		}
    	}
    }
		return resultList;
  }

  /**
   * Return the first named Element found.
   * @param element the containing Element
   * @param name the tag name
   * @return matching Element (null if none)
   */
  public static Element getElement(Element element, String name)
  {
    NodeList nodeList = getElementList(element, name);

    return (nodeList.getLength() == 0) ? null : (Element) nodeList.item(0);
  }

  /**
   * Return the first named Element found - namespace aware
   * @param namespace Namespace URI
   * @param element the containing Element
   * @param name the tag name
   * @return matching Element (null if none)
   */
  public static Element getElementNS(String namespace, Element element, String name)
  {
    NodeList nodeList = getElementListNS(namespace, element, name);

    return (nodeList.getLength() == 0) ? null : (Element) nodeList.item(0);
  }

  /**
   * Remove this node from its parent.
   * @param node the node to remove
   * @return Node removed
   */
  public Node removeNode(Node node) {
    return node.getParentNode().removeChild(node);
  }

	/**
	 * Search up the tree for a given node
	 * @param currentNode Starting point for our search
	 * @param tagName Node name to look up
	 * @return matching Node (null if none)
	 */
  public static Node getPreviousNodeByName(Node currentNode, String tagName) {
    Node node = currentNode.getParentNode();

    while ((node != null) && (!node.getNodeName().equals(tagName))) {
      node = node.getParentNode();
    }
    return node;
  }

	/**
	 * Search earlier siblings for a given node
	 * @param currentNode Starting point for our search
	 * @param tagName Node name to look up
	 * @return matching Node (null if none)
	 */
  public static Node getPreviousSiblingByName(Node currentNode, String tagName) {
    Node node = currentNode.getPreviousSibling();

    while ((node != null) && (!node.getNodeName().equals(tagName))) {
      node = node.getPreviousSibling();
    }
    return node;
  }

	/**
	 * Search our next siblings for a given node
	 * @param currentNode Starting point for our search
	 * @param tagName Node name to look up
	 * @return matching Node (null if none)
	 */
  public static Node getNextSiblingByName(Node currentNode, String tagName) {
    Node node = currentNode.getNextSibling();

    while ((node != null) && (!node.getNodeName().equals(tagName))) {
      node = node.getNextSibling();
    }
    return node;
  }

	/**
	 * Search across the tree for a given sibling
	 * @param currentNode Starting point for our search
	 * @param tagName Node name to look up
	 * @return matching Node (null if none)
	 * @deprecated  Replaced by {@link #getNextSiblingByName(Node currentNode, String tagName)}
	 */
  public static Node getNextNodeByName(Node currentNode, String tagName) {
    return getNextSiblingByName(currentNode, tagName);
  }

	/**
	 * Search for a named child of a given node
	 * @param currentNode Starting point for our search
	 * @param tagName Node name to look up
	 * @return matching Node (null if none)
	 */
  public static Node getChildSiblingByName(Node currentNode, String tagName) {
    Node node = currentNode.getFirstChild();

    while ((node != null) && (!node.getNodeName().equals(tagName))) {
      node = node.getNextSibling();
    }
    return node;
  }

  /**
   * Get a DOM Document builder.
   * @return The DocumentBuilder
   * @throws DomException
   */
  public static DocumentBuilder getXmlDocumentBuilder() throws DomException
  {
    return getXmlDocumentBuilder(false);
  }

  /**
   * Get a DOM Document builder - namespace aware
   * @return The DocumentBuilder
   * @throws DomException
   */
  public static DocumentBuilder getXmlDocumentBuilderNS() throws DomException
  {
    return getXmlDocumentBuilder(true);
  }

  /**
   * Get a DOM Document builder.
   * @param namespaceAware true if we're to handle namespace details
   * @return The DocumentBuilder
   * @throws DomException
   */
  public static DocumentBuilder getXmlDocumentBuilder(boolean namespaceAware) throws DomException
  {
    try
    {
      DocumentBuilderFactory factory;

      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(namespaceAware);

      log.debug("DOM parse: namespace aware = " + namespaceAware);
      return factory.newDocumentBuilder();

    }
    catch (Exception e)
    {
      throw new DomException(e.toString());
    }
  }

  /**
   * Start a new XML Document (with root name = xml)
   * @return the Document
   * @throws DomException
   */
  public static Document createXmlDocument() throws DomException {
  	return createXmlDocument("xml");
  }

  /**
   * Start a new XML Document.
   * @param rootName The name of the Document root Element (created here)
   * @return the Document
   * @throws DomException
   */
  public static Document createXmlDocument(String rootName) throws DomException {
    try {
      Document 	document 	= getXmlDocumentBuilder().newDocument();
      Element		root			= document.createElement(rootName);

      document.appendChild(root);
      return document;

    } catch (Exception e) {
      throw new DomException(e.toString());
    }
  }

  /**
   * Copy an XML document, adding it as a child of the target document root
   * @param source Document to copy
   * @param target Document to contain copy
   */
  public static void copyDocument(Document source, Document target)
  {
  	Node node = target.importNode(source.getDocumentElement(), true);

   	target.getDocumentElement().appendChild(node);
	}

  /**
   * Copy a Node from one source document, adding it to the document
   * root of a different, target Document
   * @param source Document to copy
   * @param target Document to contain copy
   */
  public static void copyDocumentNode(Node source, Document target)
  {
  	Node node = target.importNode(source, true);

   	target.getDocumentElement().appendChild(node);
	}

  /**
   * Parse XML text (from an input stream) into a Document.
   * @param xmlStream The XML text stream
   * @return DOM Document
   * @throws DomException
   */
  public static Document parseXmlStream(InputStream xmlStream)
      																							throws DomException {
    try {
      return getXmlDocumentBuilder().parse(new InputSource(xmlStream));

    } catch (Exception e) {
      throw new DomException(e.toString());
    }
  }

  /**
   * Parse XML text (from an input stream) into a Document - namespace aware.
   * @param xmlStream The XML text stream
   * @return DOM Document
   * @throws DomException
   */
  public static Document parseXmlStreamNS(InputStream xmlStream)
      															  								throws DomException {
    try {
      return getXmlDocumentBuilderNS().parse(new InputSource(xmlStream));

    } catch (Exception e) {
      throw new DomException(e.toString());
    }
  }

  /**
   * Parse XML text (from a Reader) into a Document.
   * @param xmlReader The XML Reader
   * @return DOM Document
   * @throws DomException
   */
  public static Document parseXmlReader(Reader xmlReader) throws DomException {

    try {
      return getXmlDocumentBuilder().parse(new InputSource(xmlReader));

    } catch (Exception e) {
      throw new DomException(e.toString());
    }
  }

  /**
   * Parse XML text (from a raw byte array) into a Document.
   * @param xml The XML text
   * @return DOM Document
   * @throws DomException
   */
  public static Document parseXmlBytes(byte[] xml) throws DomException {
    return parseXmlStream(new ByteArrayInputStream(xml));
  }

  /**
   * Parse XML text (from a raw byte array) into a Document - namespace aware.
   * @param xml The XML text
   * @return DOM Document
   * @throws DomException
   */
  public static Document parseXmlBytesNS(byte[] xml) throws DomException {
    return parseXmlStreamNS(new ByteArrayInputStream(xml));
  }

  /**
   * Parse XML text (from a string) into a Document.
   * @param xml The XML text
   * @return DOM Document
   * @throws DomException
   */
  public static Document parseXmlString(String xml) throws DomException {
    return parseXmlStream(new ByteArrayInputStream(xml.getBytes()));
  }

  /**
   * Parse an XML file into a Document.
   * @param filename - The filename to parse
   * @return DOM Document
   * @throws DomException
   */
  public static Document parseXmlFile(String filename) throws DomException {
    try {
      return getXmlDocumentBuilder().parse(filename);
    } catch (Exception exception) {
      throw new DomException(exception.toString());
    }
  }

  /**
   * Set up and configure an HTML DOM parser.  We specifiy a
   * default encoding value to be used when no encoding information
   * is available in the HTML document itself.
   *
   * An appropriate META tag will override this default:
   * <code>
   *   <meta http-equiv="Content-Type" content="text/html; charset=XXXX">
   * </code>
   *
   * @return The parser
   */

/*******************************************************************************

  * We originally used the Neko HTML parser here.  This was a boon as it
  * gracefully handled both HTML and XML (which it wraped in HTML and
  * BODY tags).  Sadly, it is closely tied to Xerces,
  *
  * At a future date, we'll look for an appropriate substitute.  At present,
  * parsing only XML is good enough (the Sirsi Web2 Bridge is the only
  * supported search source, and it's an XML API to SingleSearch).

  private static org.cyberneko.html.parsers.DOMParser newHtmlDomParser()
                 throws SAXNotRecognizedException, SAXNotSupportedException {
    org.cyberneko.html.parsers.DOMParser domParser;

    domParser = new org.cyberneko.html.parsers.DOMParser();
    domParser.setProperty(ENCODING_OPTION, INPUT_ENCODING);

    return domParser;
  }

*******************************************************************************/

  /**
   * Parse HTML from a Reader
   * @param reader Reader input
   * @return DOM Document
   * @throws DomException
   */

/*******************************************************************************
  *
  * See notes on Neko HTML (above)
  *

  public static Document parseHtmlReader(Reader reader) throws DomException {
    return parseHtmlFromInputSource(new InputSource(reader));
  }

*******************************************************************************/

  /**
   * Parse HTML from an InputSource
   * @param in InputSource
   * @return DOM Document
   * @throws DomException
   */

/*******************************************************************************
  *
  * See notes on Neko HTML (above)
  *

  public static Document parseHtmlFromInputSource(InputSource in) throws DomException {
    try {
      org.cyberneko.html.parsers.DOMParser domParser;

      domParser = newHtmlDomParser();
      domParser.parse(in);
      return domParser.getDocument();

    } catch (Exception e) {
      throw new DomException(e.toString());
    }
  }

*******************************************************************************/

  /**
   * Parse HTML text (from a raw byte array) into a Document.
   * @param html The HTML text
   * @return DOM Document
   * @throws DomException
   *<p>
   * The used to be:
   *    <code>parseHtmlStream(new ByteArrayInputStream(html));</code>
   */
  public static Document parseHtmlBytes(byte[] html) throws DomException {
    return parseXmlStreamNS(new ByteArrayInputStream(html));
  }

  /**
   * Parse HTML text (from a String) into a Document.
   * @param html The HTML text
   * @return DOM Document
   * @throws DomException
   *<p>
   * This used to be:
   *    <code>return parseHtmlReader(new StringReader(html));</code>
   */
  public static Document parseHtmlString(String html) throws DomException {
    return parseXmlReader(new StringReader(html));
  }

  /**
   * Write formatted XML text to supplied OutputStream.
   * @param node Node to write
   * @param target stream to write to
   * @throws DomException
   */
  public static void serializeXml(Node node, OutputStream target)
                                  throws DomException
  {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(new DOMSource(node), new StreamResult(target));

    } catch (Exception e) {
      throw new DomException(e.toString());
    }
  }

  /**
   * Write formatted XML text to supplied Writer.
   * @param node the Node to write
   * @param writer Writer the document is written to
   * @throws DomException
   */
  public static void serializeXml(Node node, Writer writer) throws DomException {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(new DOMSource(node), new StreamResult(writer));

    } catch (Exception e) {
      throw new DomException(e.toString());
    }
  }

  /**
   * Write formatted XML text to a String.
   * @param object The XML Document, HTML Document, or Element to write
   * @return String containing the formatted document text
   * @throws DomException
   */
  public static String serialize(Object object) throws DomException {
    ByteArrayOutputStream stream = null;
    Writer writer = null;

    try {
      stream = new ByteArrayOutputStream();
      writer = new OutputStreamWriter(stream, ENCODING);

      if (object instanceof Document) {
        serializeXml((Node) ((Document) object).getDocumentElement(), writer);
      } else if (object instanceof Element) {
        serializeXml((Node) object, writer);
      } else {
      	throw new IllegalArgumentException("Unexpected object for serialzation: "
      																	+ 	object.toString());
			}
      return stream.toString();

    } catch (Exception e) {
      throw new DomException(e.toString());

    } finally {
      try { if (writer != null) writer.close(); } catch (Exception ignore) { }
      try { if (stream != null) stream.close(); } catch (Exception ignore) { }
    }
  }
}
