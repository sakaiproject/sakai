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

import javax.xml.transform.stream.StreamResult;

import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import org.w3c.dom.ls.LSInput;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Source;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Path;
import java.net.URI;
import java.io.File;
import javax.xml.validation.SchemaFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import org.xml.sax.XMLReader;
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
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
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
	private static final String ELEMENT_ATTRIBUTE_LIMIT = "jdk.xml.elementAttributeLimit";
	private static final String MAX_ELEMENT_DEPTH = "jdk.xml.maxElementDepth";
	private static final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
	private static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
	private static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
	private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
	private static final String DEFER_NODE_EXPANSION = "http://apache.org/xml/features/dom/defer-node-expansion";


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

		SAXParser p;
		try
		{
			p = createSecureSAXParser(false);
		}
		catch (ParserConfigurationException e)
		{
			throw new SAXException("Failed to create secure XML parser", e);
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
		DocumentBuilderFactory factory = createSecureDocumentBuilderFactoryWithDoctype(true);
        // Preserve CDATA nodes in the legacy shared-reader contract used by archive consumers.
        factory.setCoalescing(false);
        return factory.newDocumentBuilder();
	}

	/**
	 * @return a secure, namespace-aware DocumentBuilderFactory for DOM XML parsing.
	 */
	public static DocumentBuilderFactory createSecureDocumentBuilderFactory() throws ParserConfigurationException
	{
		return createSecureDocumentBuilderFactory(null);
	}

    /** Creates a streaming parser with DTD and external entity resolution disabled. */
    public static javax.xml.stream.XMLInputFactory createSecureXMLInputFactory() {
        javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newFactory();
        factory.setProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty(javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        return factory;
    }

	/** Creates a hardened DOM factory with the requested namespace mode. */
	public static DocumentBuilderFactory createSecureDocumentBuilderFactory(boolean namespaceAware) throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
		factory.setNamespaceAware(namespaceAware);
		return factory;
	}

	/**
	 * Allows legacy DOCTYPE declarations without loading DTDs or expanding entities.
	 * Use only for formats whose supported documents contain a DOCTYPE.
	 */
	public static DocumentBuilderFactory createSecureDocumentBuilderFactoryWithDoctype(boolean namespaceAware) throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = createSecureDocumentBuilderFactory(namespaceAware);
		setFeature(factory, DISALLOW_DOCTYPE_DECL, false);
		return factory;
	}

	/** Allows legacy DOCTYPE declarations while retaining the shared SAX external-resource restrictions. */
	public static SAXParser createSecureSAXParserWithDoctype(boolean namespaceAware) throws ParserConfigurationException, SAXException
	{
		SAXParser parser = createSecureSAXParser(namespaceAware);
		parser.getXMLReader().setFeature(DISALLOW_DOCTYPE_DECL, false);
		return parser;
	}

	/** Creates a new SAX parser; required security settings must succeed. */
	public static SAXParser createSecureSAXParser(boolean namespaceAware) throws ParserConfigurationException, SAXException
	{
		SAXParserFactory factory = SAXParserFactory.newDefaultInstance();
		factory.setNamespaceAware(namespaceAware);
		factory.setValidating(false);
		factory.setXIncludeAware(false);
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature(DISALLOW_DOCTYPE_DECL, true);
		factory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
		factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
		factory.setFeature(LOAD_EXTERNAL_DTD, false);
		SAXParser parser = factory.newSAXParser();
		parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		return parser;
	}

	/** Creates a hardened reader for SAX consumers, including JDOM adapters. */
	public static XMLReader createSecureXMLReader(boolean namespaceAware) throws ParserConfigurationException, SAXException
	{
		return createSecureSAXParser(namespaceAware).getXMLReader();
	}

	/** Supplies a hardened SAX reader to APIs that would otherwise parse a StreamSource themselves. */
	public static SAXSource createSecureSource(InputSource input) throws TransformerException
	{
		try
		{
			return new SAXSource(createSecureXMLReader(true), input);
		}
		catch (ParserConfigurationException | SAXException e)
		{
			throw new TransformerException("Unable to create secure XML source", e);
		}
	}

	public static SAXSource createSecureSource(InputStream input) throws TransformerException
	{
		return createSecureSource(new InputSource(input));
	}

	/** Resolves a stylesheet dependency within its local directory, using a hardened reader. */
	public static Source resolveSecureSource(String href, String base) throws TransformerException
	{
		try
		{
			if (base == null)
			{
				throw new IOException("Stylesheet base location is required");
			}
			URI baseURI = URI.create(base);
			if (baseURI.getScheme() == null)
			{
				baseURI = new File(base).toURI();
			}
			Path directory = new File(baseURI).toPath().toRealPath().getParent();
			Path resource = new File(baseURI.resolve(href)).toPath().toRealPath();
			if (!resource.startsWith(directory))
			{
				throw new IOException("Stylesheet resource is outside its directory");
			}
			return createSecureSource(new InputSource(resource.toUri().toString()));
		}
		catch (IOException | IllegalArgumentException e)
		{
			throw new TransformerException("Unable to resolve local stylesheet resource", e);
		}
	}

	/** Creates a transformer factory with external XML and stylesheet access disabled. */
	public static TransformerFactory createSecureTransformerFactory()
	{
		TransformerFactory factory = TransformerFactory.newDefaultInstance();
		try
		{
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		}
		catch (TransformerConfigurationException | IllegalArgumentException e)
		{
			throw new IllegalStateException("Unable to configure secure XML transformation", e);
		}
		return factory;
	}

	/** Creates a schema factory with external DTD and schema access disabled. */
	public static SchemaFactory createSecureSchemaFactory() throws SAXException
	{
		SchemaFactory factory = SchemaFactory.newDefaultInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		return factory;
	}

	/** Compiles package schemas without allowing network access or reads outside the package. */
	public static Schema createSecureSchema(File directory, List<File> schemaFiles) throws IOException, SAXException, ParserConfigurationException
	{
		Path root = directory.toPath().toRealPath();
		DocumentBuilder builder = createSecureDocumentBuilderFactory().newDocumentBuilder();
		DOMImplementationLS implementation = (DOMImplementationLS) builder.getDOMImplementation().getFeature("LS", "3.0");
		SchemaFactory factory = createSecureSchemaFactory();
		factory.setResourceResolver((type, namespace, publicId, systemId, baseURI) -> {
			try
			{
				URI location = baseURI == null ? root.toUri().resolve(systemId) : URI.create(baseURI).resolve(systemId);
				Path file = new File(location).toPath().toRealPath();
				if (!file.startsWith(root))
				{
					throw new IOException("Schema resource is outside the package");
				}
				LSInput input = implementation.createLSInput();
				// Parse imported schemas with the same hardened DOM policy, then supply XML text.
				StringWriter xml = new StringWriter();
				createSecureTransformerFactory().newTransformer().transform(new DOMSource(builder.parse(file.toFile())), new StreamResult(xml));
				input.setStringData(xml.toString());
				input.setSystemId(file.toUri().toString());
				input.setBaseURI(file.toUri().toString());
				return input;
			}
			catch (IOException | SAXException | TransformerException | IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Unable to resolve package schema", e);
			}
		});
		List<Source> sources = new ArrayList<>();
		for (File schemaFile : schemaFiles)
		{
			Path file = schemaFile.toPath().toRealPath();
			if (!file.startsWith(root))
			{
				throw new IOException("Schema is outside the package");
			}
			sources.add(new DOMSource(builder.parse(file.toFile()), file.toUri().toString()));
		}
		return factory.newSchema(sources.toArray(new Source[0]));
	}

	/**
	 * @param schema optional schema to attach to the factory
	 * @return a secure, namespace-aware DocumentBuilderFactory for DOM XML parsing.
	 */
	public static DocumentBuilderFactory createSecureDocumentBuilderFactory(Schema schema) throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();

		setFeature(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
		setFeature(factory, DISALLOW_DOCTYPE_DECL, true);
		setFeature(factory, EXTERNAL_GENERAL_ENTITIES, false);
		setFeature(factory, EXTERNAL_PARAMETER_ENTITIES, false);
		setFeature(factory, LOAD_EXTERNAL_DTD, false);
		setFeature(factory, DEFER_NODE_EXPANSION, false);

		setAttribute(factory, ELEMENT_ATTRIBUTE_LIMIT, "30");
		setAttribute(factory, MAX_ELEMENT_DEPTH, "25");
		setAttribute(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
		setAttribute(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

		factory.setCoalescing(true);
		factory.setExpandEntityReferences(false);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setNamespaceAware(true);
		factory.setSchema(schema);
		factory.setValidating(false);
		factory.setXIncludeAware(false);

		return factory;
	}

	private static void setFeature(DocumentBuilderFactory factory, String feature, boolean value) throws ParserConfigurationException
	{
		try
		{
			factory.setFeature(feature, value);
		}
		catch (ParserConfigurationException e)
		{
			throw new ParserConfigurationException("XML parser does not support required feature " + feature + ": " + e.getMessage());
		}
	}

	private static void setAttribute(DocumentBuilderFactory factory, String attribute, Object value) throws ParserConfigurationException
	{
		try
		{
			factory.setAttribute(attribute, value);
		}
		catch (IllegalArgumentException e)
		{
			throw new ParserConfigurationException("XML parser does not support required attribute " + attribute + ": " + e.getMessage());
		}
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

	/**
	 * Fetch text value of a child element
	 * @return Text content of the first child element matching the tag name,
	 * 	otherwise null if there is no matching element
	 */
	public static String getChildElementValue(Element e, String childName) {
		NodeList list = e.getElementsByTagName(childName);
		if (list.getLength() > 0) {
			Element c = (Element) list.item(0);
			return c.getTextContent();
		}
		return null;
	}

	/**
	 * Convert a Node and its children to a readable string representation
	 * @param node The node to convert
	 * @return A formatted string showing the node structure
	 */
	public static String nodeToString(Node node) {
		StringBuilder sb = new StringBuilder();
		appendNodeDetails(node, "", sb);
		return sb.toString();
	}

	private static void appendNodeDetails(Node node, String indent, StringBuilder sb) {
		sb.append(indent)
		  .append("Node Type: ")
		  .append(getNodeTypeName(node.getNodeType()))
		  .append(", Name: ")
		  .append(node.getNodeName());

		// Handle attributes if it's an element
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element elem = (Element) node;
			var attributes = elem.getAttributes();
			if (attributes.getLength() > 0) {
				sb.append("\n").append(indent).append("Attributes: ");
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attr = attributes.item(i);
					sb.append("\n").append(indent).append("  ")
					  .append(attr.getNodeName())
					  .append("=\"")
					  .append(attr.getNodeValue())
					  .append("\"");
				}
			}
		}

		// Handle node value if it exists and isn't just whitespace
		if (node.getNodeValue() != null && !node.getNodeValue().trim().isEmpty()) {
			sb.append("\n").append(indent).append("Value: \"")
			  .append(node.getNodeValue().trim())
			  .append("\"");
		}

		sb.append("\n");

		// Recursively process child nodes
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			appendNodeDetails(children.item(i), indent + "  ", sb);
		}
	}

	private static String getNodeTypeName(short nodeType) {
		switch (nodeType) {
			case Node.ELEMENT_NODE: return "Element";
			case Node.ATTRIBUTE_NODE: return "Attribute";
			case Node.TEXT_NODE: return "Text";
			case Node.CDATA_SECTION_NODE: return "CDATA";
			case Node.COMMENT_NODE: return "Comment";
			default: return "Other(" + nodeType + ")";
		}
	}
}
