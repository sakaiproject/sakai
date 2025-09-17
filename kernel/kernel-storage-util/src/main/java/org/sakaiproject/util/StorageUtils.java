/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/Xml.java $
 * $Id: Xml.java 90064 2011-03-19 12:04:59Z david.horwitz@uct.ac.za $
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
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Special utils used in the storage utils
 * (some duplication from the other kernel Xml utils)
 */
@Slf4j
public class StorageUtils {
	private static SAXParserFactory parserFactory;
	private static final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

	static {
		try {
			dbFactory.setNamespaceAware(true);
			dbFactory.setXIncludeAware(false);
			dbFactory.setExpandEntityReferences(false);
			dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		} catch (ParserConfigurationException | IllegalArgumentException e) {
			log.warn("Failed to apply secure XML parser features; continuing with defaults", e);
		}
	}

	/**
	 * Create a new DOM Document.
	 * 
	 * @return A new DOM document.
	 */
	public static Document createDocument()
	{
		try
		{
			DocumentBuilder builder = dbFactory.newDocumentBuilder();

            return builder.newDocument();
		}
		catch (Exception any)
		{
            log.warn("createDocument: {}", any.toString());
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
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			fis = new FileInputStream(name);
			doc = docBuilder.parse(fis);
		}
		catch (Exception e)
		{
			log.warn("readDocument failed on file: {} with exception: {}", name, e.toString());
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ignored) {
				}
			}
		}

		if (doc != null) return doc;

		// OK, that didn't work - the document is probably ISO-8859-1
		try
		{
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			InputStreamReader in = new InputStreamReader(new FileInputStream(name), StandardCharsets.ISO_8859_1);
			InputSource inputSource = new InputSource(in);
			doc = docBuilder.parse(inputSource);
		}
		catch (Exception any)
		{
			log.warn("readDocument ISO8859-read failed on file: {} with exception: {}", name, any.toString());
		}

		if (doc != null) return doc;

		// try forcing UTF-8
		try
		{
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			InputStreamReader in = new InputStreamReader(new FileInputStream(name), StandardCharsets.UTF_8);
			InputSource inputSource = new InputSource(in);
			doc = docBuilder.parse(inputSource);
		}
		catch (Exception any)
		{
            log.warn("readDocument UTF8 read failed on file: {} with exception: {}", name, any.toString());
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
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			docBuilder.setErrorHandler(new LoggingSaxErrorHandler());
			InputSource inputSource = new InputSource(new StringReader(in));
            return docBuilder.parse(inputSource);
		}
		catch (SAXParseException spe)
		{
			logParseFailure(in, spe);
			return null;
		}
		catch (Exception any)
		{
			log.warn("readDocumentFromString failure", any);
			return null;
		}
	}

	private static void logParseFailure(String xml, SAXParseException exception)
	{
		String context = extractContext(xml, exception.getLineNumber(), exception.getColumnNumber());
		if (context.isEmpty()) {
			log.error("readDocumentFromString failed at line {}, column {}: {}", exception.getLineNumber(),
					exception.getColumnNumber(), exception.getMessage(), exception);
		} else {
			log.error("readDocumentFromString failed at line {}, column {}: {} Context: {}",
					exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage(), context, exception);
		}
	}

	private static String extractContext(String xml, int line, int column)
	{
		if (xml == null || xml.isEmpty() || line <= 0 || column <= 0)
		{
			return "";
		}

		int len = xml.length();
        int index = 0;
		int currentLine = 1;
		int currentCol = 1;
		while (index < len)
		{
			if (currentLine == line && currentCol == column)
			{
				break;
			}
			char ch = xml.charAt(index);
			index++;
			if (ch == '\n')
			{
				currentLine++;
				currentCol = 1;
			}
			else if (ch == '\r')
			{
				currentLine++;
				currentCol = 1;
				if (index < len && xml.charAt(index) == '\n')
				{
					index++;
				}
			}
			else
			{
				currentCol++;
			}
		}

        int colIndex = Math.min(index, len - 1);
		int start = Math.max(0, colIndex - 80);
		int end = Math.min(len, colIndex + 80);
		if (start >= end)
		{
			return "";
		}
		String snippet = xml.substring(start, end);
		return snippet.replaceAll("\\s+", " ");
	}

	private static class LoggingSaxErrorHandler implements ErrorHandler
	{
		@Override
		public void warning(SAXParseException exception) throws SAXException
		{
			log.warn("XML parse warning at line {}, column {}: {}", exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
		}

		@Override
		public void error(SAXParseException exception) throws SAXException
		{
			throw exception;
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException
		{
			throw exception;
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
			parserFactory.setXIncludeAware(false);
			try {
				parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
				parserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				parserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
				parserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
				parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			} catch (Exception e) {
				log.warn("Failed to apply secure SAX parser features; continuing with defaults", e);
			}
		}
		try
		{
			p = parserFactory.newSAXParser();
			XMLReader reader = p.getXMLReader();
			try {
				reader.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
				reader.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
				log.warn("Failed to restrict external entity access on SAX reader; continuing with defaults", e);
			}
			try {
				reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
				log.warn("Failed to disable external DTD loading on SAX reader; continuing with defaults", e);
			}
		}
		catch (ParserConfigurationException e)
		{
			throw new SAXException("Failed to get a parser ", e);
		}
		p.parse(ss, dh);
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
			// get an instance of the DOMImplementation registry
			DocumentBuilder builder = dbFactory.newDocumentBuilder();
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
			
			  DocumentBuilder builder = dbFactory.newDocumentBuilder();
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
	 * Escape the SQL to ensure it is valid (and protect against SQL injection attacks)
	 * @param value SQL string or fragment
	 * @return escaped SQL string
	 */
    public static String escapeSql(String value) {
        if (value == null) return "";
        try {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '\'') {
                    buf.append("''");
                } else {
                    buf.append(c);
                }
            }

            String rv = buf.toString();
            return rv;
        } catch (Exception e) {
            log.warn("Validator.escapeSql: "+e, e);
            return "";
        }
    }

	/**
	 * Escape the wildcards in an SQL LIKE phrase
	 * @param value
	 * @return string with escaped wildcards
	 */
    public static String escapeSqlLike(final String value) {
        if (value == null) return "";
        return value
            .replaceAll("\\_", "\\\\_")
            .replaceAll("\\%", "\\\\%");
    }

	public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
