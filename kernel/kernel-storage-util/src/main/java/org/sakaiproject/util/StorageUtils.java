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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Special utils used in the storage utils
 * (some duplication from the other kernel Xml utils)
 */
@Slf4j
public class StorageUtils {
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
     * @return a DocumentBuilder object for XML parsing.
     */
    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        return dbf.newDocumentBuilder();
    }

}
