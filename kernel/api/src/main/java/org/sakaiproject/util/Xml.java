/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.binary.Base64;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * <p>
 * Xml is a DOM XML helper object with static functions to help with XML.
 * </p>
 */
@Slf4j
public class Xml
{
	private static SAXParserFactory parserFactory;

	/**
	 * Create a new DOM Document.
	 * 
	 * @return A new DOM document.
	 */
	public static Document createDocument()
	{
		try
		{
			DocumentBuilder builder = getDocumentBuilder();
			Document doc = builder.newDocument();

			return doc;
		}
		catch (Exception any)
		{
			log.warn("createDocument: " + any.toString());
			return null;
		}
	}

	/**
	 * Read a DOM Document from xml in a file.
	 * 
	 * @param name
	 *        The file name for the xml file.
	 * @return A new DOM Document with the xml contents.
	 */
	public static Document readDocument(String name)
	{
		Document doc = null;
		// first try using whatever character encoding the XML itself specifies
		InputStream fis = null;
		try
		{
			DocumentBuilder docBuilder = getDocumentBuilder();
			fis = new FileInputStream(name);
			doc = docBuilder.parse(fis);
		}
		catch (Exception e)
		{
			doc = null;
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}

		if (doc != null) return doc;

		// OK, that didn't work - the document is probably ISO-8859-1
		try
		{
			DocumentBuilder docBuilder = getDocumentBuilder();
			InputStreamReader in = new InputStreamReader(new FileInputStream(name), "ISO-8859-1");
			InputSource inputSource = new InputSource(in);
			doc = docBuilder.parse(inputSource);
		}
		catch (Exception any)
		{
			doc = null;
		}

		if (doc != null) return doc;

		// try forcing UTF-8
		try
		{
			DocumentBuilder docBuilder = getDocumentBuilder();
			InputStreamReader in = new InputStreamReader(new FileInputStream(name), "UTF-8");
			InputSource inputSource = new InputSource(in);
			doc = docBuilder.parse(inputSource);
		}
		catch (Exception any)
		{
			log.warn("readDocument failed on file: " + name + " with exception: " + any.toString());
			doc = null;
		}

		return doc;
	}

	/**
	 * Read a DOM Document from xml in a string.
	 * 
	 * @param in
	 *        The string containing the XML
	 * @return A new DOM Document with the xml contents.
	 */
	public static Document readDocumentFromString(String in)
	{
		try
		{
			DocumentBuilder docBuilder = getDocumentBuilder();
			InputSource inputSource = new InputSource(new StringReader(in));
			Document doc = docBuilder.parse(inputSource);
			return doc;
		}
		catch (Exception any)
		{
			log.warn("readDocumentFromString: " + any.toString());
			return null;
		}
	}
	

	/**
	 * Process a string of XML using SAX and a default handler
	 * @param in
	 * @param dh
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void processString( String in, DefaultHandler dh ) throws SAXException, IOException {
		Reader r = new StringReader(in);
		processReader(r,dh);
		r.close();
	}
	/**
	 * Process a stream of XML using SAX and a supplied default handler
	 * @param in
	 * @param dh
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void processStream( InputStream in, DefaultHandler dh ) throws SAXException, IOException {
		processReader(new InputStreamReader(in),dh);
	}
	
	/**
	 * SAX Process a Reader using the Default handler
	 * @param in
	 * @param dh
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void processReader(Reader in, DefaultHandler dh) throws SAXException,
			IOException
	{
		InputSource ss = new InputSource(in);

		SAXParser p = null;
		if (parserFactory == null)
		{
			parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(false);
			parserFactory.setValidating(false);
		}
		try
		{
			p = parserFactory.newSAXParser();

		}
		catch (ParserConfigurationException e)
		{
			throw new SAXException("Failed to get a parser ", e);
		}
		p.parse(ss, dh);
	}

	/**
	 * Read a DOM Document from xml in a stream.
	 * 
	 * @param in
	 *        The stream containing the XML
	 * @return A new DOM Document with the xml contents.
	 */
	public static Document readDocumentFromStream(InputStream in)
	{
		try
		{
			DocumentBuilder docBuilder = getDocumentBuilder();
			InputSource inputSource = new InputSource(in);
			Document doc = docBuilder.parse(inputSource);
			return doc;
		}
		catch (Exception any)
		{
			log.warn("readDocumentFromStream: " + any.toString());
			return null;
		}
	}

	/**
	 * Write a DOM Document to an xml file.
	 * 
	 * @param doc
	 *        The DOM Document to write.
	 * @param fileName
	 *        The complete file name path.
	 */
	public static void writeDocument(Document doc, String fileName)
	{
		OutputStream out = null;
		try
		{
			out = new FileOutputStream(fileName);
//			 get an instance of the DOMImplementation registry
			 DocumentBuilderFactory factory 
			   = DocumentBuilderFactory.newInstance();
			  DocumentBuilder builder = factory.newDocumentBuilder();
			  DOMImplementation impl = builder.getDOMImplementation();
			  
			DOMImplementationLS feature = (DOMImplementationLS) impl.getFeature("LS","3.0");
			LSSerializer serializer = feature.createLSSerializer();
			LSOutput output = feature.createLSOutput();
			output.setByteStream(out);
			output.setEncoding("UTF-8");
			serializer.write(doc, output);
			
			out.close();
		}
		catch (Exception any)
		{
			log.warn("writeDocument: " + any.toString());
		}
		finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					
				}
			}
		}
	}

	/**
	 * Write a DOM Document to an output stream.
	 * 
	 * @param doc
	 *        The DOM Document to write.
	 * @param out
	 *        The output stream.
	 */
	public static String writeDocumentToString(Document doc)
	{
		try
		{
			
			StringWriter sw = new StringWriter();
			
			 DocumentBuilderFactory factory 
			   = DocumentBuilderFactory.newInstance();
			  DocumentBuilder builder = factory.newDocumentBuilder();
			  DOMImplementation impl = builder.getDOMImplementation();
			  
			
			DOMImplementationLS feature = (DOMImplementationLS) impl.getFeature("LS",
			"3.0");
			LSSerializer serializer = feature.createLSSerializer();
			LSOutput output = feature.createLSOutput();
			output.setCharacterStream(sw);
			output.setEncoding("UTF-8");
			serializer.write(doc, output);
			
			sw.flush();
			return sw.toString();
		}
		catch (Exception any)
		{
			log.warn("writeDocumentToString: " + any.toString());
			return null;
		}
	}

	/**
	 * Place a string into the attribute <tag>of the element <el>, encoded so special characters can be used.
	 * 
	 * @param el
	 *        The element.
	 * @param tag
	 *        The attribute name.
	 * @param value
	 *        The string.
	 */
	public static void encodeAttribute(Element el, String tag, String value)
	{
		//KNL-688 avoid a NPE being logged - DH
		if (value == null)
		{
			return;
		}
		
		// encode the message body base64, and make it an attribute
		try
		{
			String encoded = new String(Base64.encodeBase64(value.getBytes("UTF-8")),"UTF-8");
			el.setAttribute(tag, encoded);
		}
		catch (Exception e)
		{
			log.warn("encodeAttribute: " + e);
		}
	}

	/**
	 * Decode a string from the attribute <tag>of the element <el>, that was made using encodeAttribute().
	 * 
	 * @param el
	 *        The element.
	 * @param tag
	 *        The attribute name.
	 * @return The string; may be empty, won't be null.
	 */
	public static String decodeAttribute(Element el, String tag)
	{
		String charset = StringUtils.trimToNull(el.getAttribute("charset"));
		if (charset == null) charset = "UTF-8";

		String body = StringUtils.trimToNull(el.getAttribute(tag));
		if (body != null)
		{
			try
			{
				byte[] decoded = Base64.decodeBase64(body.getBytes("UTF-8"));
				body = new String(decoded, charset);
			}
			catch (Exception e)
			{
				log.warn("decodeAttribute: " + e);
			}
		}

		if (body == null) body = "";

		return body;
	}
	/**
	 * Decode a value with a given charset
	 * @param charset
	 * @param value
	 * @return
	 */
	public static String decode(String charset, String value)
	{
		String body = StringUtils.trimToNull(value);
		if (body != null)
		{
			try
			{
				byte[] decoded = Base64.decodeBase64(body.getBytes("UTF-8"));
				body = new String(decoded, charset);
			}
			catch (Exception e)
			{
				log.warn("decodeAttribute: " + e);
			}
		}

		if (body == null) body = "";

		return body;
	}

	/**
	 * @return a DocumentBuilder object for XML parsing.
	 */
	protected static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		return dbf.newDocumentBuilder();
	}

	/**
	 * Serialize the properties into XML, adding an element to the doc under the top of the stack element.
	 * 
	 * @param propsToSerialize
	 *        The properties to serialize.
	 * @param doc
	 *        The DOM doc to contain the XML (or null for a string return).
	 * @param stack
	 *        The DOM elements, the top of which is the containing element of the new "resource" element.
	 * @return The newly added element.
	 */
	public static Element propertiesToXml(Properties propsToSerialize, Document doc, Stack<Element> stack)
	{
		Element properties = doc.createElement("properties");
		((Element) stack.peek()).appendChild(properties);
		Enumeration<?> props = propsToSerialize.propertyNames();
		while (props.hasMoreElements())
		{
			String name = (String) props.nextElement();
			String value = propsToSerialize.getProperty(name);
			Element propElement = doc.createElement("property");
			properties.appendChild(propElement);
			propElement.setAttribute("name", name);

			// encode to allow special characters in the value
			Xml.encodeAttribute(propElement, "value", (String) value);
			propElement.setAttribute("enc", "BASE64");
		}

		return properties;
	}

	/**
	 * Fill in a properties from XML.
	 * 
	 * @param properties
	 *        The properties to fill in.
	 * @param el
	 *        The XML DOM element.
	 */
	public static void xmlToProperties(Properties properties, Element el)
	{
		// the children (property)
		NodeList children = el.getChildNodes();
		final int length = children.getLength();
		for (int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) child;

			// look for property
			if (element.getTagName().equals("property"))
			{
				String name = element.getAttribute("name");
				String enc = StringUtils.trimToNull(element.getAttribute("enc"));
				String value = null;
				if ("BASE64".equalsIgnoreCase(enc))
				{
					value = decodeAttribute(element, "value");
				}
				else
				{
					value = element.getAttribute("value");
				}

				properties.put(name, value);
			}
		}
	}
}
