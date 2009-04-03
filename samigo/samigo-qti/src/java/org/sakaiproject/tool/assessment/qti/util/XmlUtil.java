/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/qti/util/XmlUtil.java $
 * $Id: XmlUtil.java 9274 2006-05-10 22:50:48Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.qti.util;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import javax.servlet.ServletContext;

import org.sakaiproject.util.FormattedText;
import org.springframework.core.io.ClassPathResource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.FileReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;


/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author palcasi
 * mods:
 * @author Rachel Gollub rgollub@stanford.edu
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id: XmlUtil.java 9274 2006-05-10 22:50:48Z daisyf@stanford.edu $
 */

public final class XmlUtil
{
  private static Log log = LogFactory.getLog(XmlUtil.class);

  /**
   * Create document object
   *
   * @return Document
   */
  public static Document createDocument()
  {
    log.debug("createDocument()");

    Document document = null;
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);

    try
    {
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      document = documentBuilder.newDocument();
    }
    catch(ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }

    return document;
  }

  /**
   * Create document object from XML string
   *
   * @return Document
   */
  public static Document createDocument(String xmlString)
  {
    log.debug("createDocument()");

    Document document = null;
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);

    try
    {
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        document = documentBuilder.parse(xmlString);
    }
    catch (IOException ex)
    {
      log.error(ex.getMessage(), ex);
    }
    catch (SAXException ex)
    {
      log.error(ex.getMessage(), ex);
    }
    catch(ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }

    return document;
  }
  /**
   * Read document from ServletContext/context path
   *
   * @param context ServletContext
   * @param path path
   *
   * @return Document
   */
  public static Document readDocument(ServletContext context, String path)
  {
    if(log.isDebugEnabled())
    {
      log.debug("readDocument(String " + path + ")");
    }

      log.debug("readDocument(String " + path + ")");

    Document document = null;
    InputStream inputStream = context.getResourceAsStream(path);
    String fullpath = context.getRealPath(path);
      log.debug("readDocument(full path) " + fullpath + ")");
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);

    try
    {
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      document = documentBuilder.parse(inputStream);
    }
    catch(ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(SAXException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(IOException e)
    {
      log.error(e.getMessage(), e);
    }

    return document;
  }

  /**
   * Read document from within spring context path
   *
   * @param path path
   *
   * @return Document
   */
  public static Document readDocument(String path)
  {
    if(log.isDebugEnabled())
    {
      log.debug("readDocument(String " + path + ")");
    }

      log.debug("readDocument(String " + path + ")");

    Document document = null;
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
    ClassPathResource resource = new ClassPathResource(path);

    try
    {

      InputStream inputStream = resource.getInputStream();

      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      document = documentBuilder.parse(inputStream);
    }
    catch(ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(SAXException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(IOException e)
    {
      log.error(e.getMessage(), e);
    }

    return document;
  }
  public static DOMSource getDocumentSource(ServletContext context, String path)
  {
    if(log.isDebugEnabled())
    {
      log.debug("readDocument(String " + path + ")");
    }


    InputStream inputStream = context.getResourceAsStream(path);
    String realPath = null;
    try
    {
      realPath = context.getResource(path).toString();
    }
    catch (MalformedURLException e1)
    {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);

    Document document = null;
    DOMSource source = null;
    try
    {
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      document = documentBuilder.parse(inputStream);
      source = new DOMSource(document, realPath);
    }
    catch(ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(SAXException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(IOException e)
    {
      log.error(e.getMessage(), e);
    }

    return source;
  }
  /**
   * direct parse of the file to XML document
   * @param path the path
   *
   * @return a Document
   */
/*
  public static Document readDocument(String path)
  {
    return readDocument(path, false);
  }
*/
  /**
   * This more forgiving version skips blank lines if trim = true
   * Otherwise it does a direct parse of the file.
   * @param path file path
   * @param trim trim blank lines true/false
   * @return
   */
 public static Document readDocument(String path, boolean trim)
  {
    if(log.isDebugEnabled())
    {
      log.debug("readDocument(String " + path + ")");
    }

    Document document = null;
    InputStream inputStream = null;
    BufferedReader in = null;
    try
    {
      if (trim)
      {
        in = new BufferedReader(new FileReader(path));

        StringBuilder buffer = new StringBuilder();
        String s = "";
        while ( (s = in.readLine()) != null)
        {
          if (s.trim().length()>0) // skip blank lines
          {
            buffer.append(s);
            buffer.append("\n");
          }
        }
        in.close();
        byte[] bytes = buffer.toString().getBytes();

        inputStream = new ByteArrayInputStream(bytes);
      }
      else
      {
        inputStream = new FileInputStream(path);
      }

      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(true);
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      document = documentBuilder.parse(inputStream);
    }
    catch(ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(SAXException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(IOException e)
    {
      log.error(e.getMessage(), e);
    }
    finally {

    	if (inputStream != null) {
    		try {
    			inputStream.close();
    		}
    		catch (IOException e) {
    			// tried
    		}
    	}
    	if (in !=null){
    		try {
    			in.close();
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
    	} 

    }

    return document;
  }

  /**
   * Create a transformer from a stylesheet
   *
   * @param stylesheet Document
   *
   * @return the Transformer
   */
  public static Transformer createTransformer(Document stylesheet)
  {

    if(log.isDebugEnabled())
    {
      log.debug("createTransformer(Document " + stylesheet + ")");
    }

    Transformer transformer = null;
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    URIResolver resolver = new URIResolver();
    transformerFactory.setURIResolver(resolver);


    try
    {
      DOMSource source = new DOMSource(stylesheet);
      String systemId = "/xml/xsl/report";
      source.setSystemId(systemId);
      transformer = transformerFactory.newTransformer(source);
    }
    catch(TransformerConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }

    return transformer;
  }

  /**
   * Create a transformer from a stylesheet
   *
   * @param source DOMSource
   *
   * @return the Transformer
   */
  public static Transformer createTransformer(DOMSource source)
  {
    if(log.isDebugEnabled())
    {
      log.debug("createTransformer(DOMSource " + source + ")");
    }

    Transformer transformer = null;
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    URIResolver resolver = new URIResolver();
    transformerFactory.setURIResolver(resolver);

    try
    {
      transformer = transformerFactory.newTransformer(source);
    }
    catch(TransformerConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }

    return transformer;

  }

  /**
   * Do XSLT transform.
   *
   * @param transformer the transsformer
   * @param source the source
   * @param result the result
   */
  private static void transform(
    Transformer transformer, Source source, Result result)
  {
    if(log.isDebugEnabled())
    {
      log.debug(
        "performTransform(Transformer " + transformer + ", Source" + source +
        ", Result " + result + ")");
    }

    try
    {
      transformer.transform(source, result);
    }
    catch(TransformerException e)
    {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Transform one document into another
   *
   * @param document source Document
   * @param stylesheet XSLT Document
   *
   * @return transformed Document
   */
  public static Document transformDocument(
    Document document, Document stylesheet)
  {
    if(log.isDebugEnabled())
    {
      log.debug(
        "Document transformDocument(Document " + document + ", Document " +
        stylesheet + ")");
    }
      log.debug(
        "Document transformDocument(Document " + document + ", Document " +
        stylesheet + ")");

    Document transformedDoc = createDocument();
    DOMSource docSource = new DOMSource(document);
    DOMResult docResult = new DOMResult(transformedDoc);
    Transformer transformer = createTransformer(stylesheet);
    transform(transformer, docSource, docResult);

//    log.debug("INPUT DOCUMENT: \n" + (document));
//    log.debug("TRANSFORM DOCUMENT: \n" + getDOMString(stylesheet));
//    log.debug("OUTPUT DOCUMENT: \n" + getDOMString(transformedDoc));

    return transformedDoc;
  }

  public static Document transformDocument(Document document, Transformer transformer)
  {

    log.debug("Document transformDocument(Document " + document + ", Trasformer " + transformer);
    Document transformedDoc = createDocument();
    DOMSource docSource = new DOMSource(document);
    DOMResult docResult = new DOMResult(transformedDoc);
    transform(transformer, docSource, docResult);

    return transformedDoc;
  }

  public static void transformNode(Node source, Node result, Transformer transformer)
  {
    if (log.isDebugEnabled())
    {
      log.debug("transformNode(Node " + source + ", Node " + result + ", Transformer ," + transformer);
    }
    DOMSource domSource = new DOMSource(source);
    DOMResult domResult = new DOMResult(result);
    transform(transformer, domSource, domResult);
  }

  /**
   * Get a textual representation of a Node.
   * @param node The Node
   * @return the document in a text string
   */
  public static String getDOMString(Node node)
  {
    //String domString = "";
    
    StringBuilder domStringbuf = new StringBuilder();
    
    int type = node.getNodeType();
    switch (type)
    {
      // print the document element
      case Node.DOCUMENT_NODE:
      {
        domStringbuf.append("<?xml version=\"1.0\" ?>\n");
        domStringbuf.append(getDOMString(((Document)node).getDocumentElement()));
        break;
      }

      // print element with attributes
      case Node.ELEMENT_NODE:
      {
    	domStringbuf.append("<");
    	domStringbuf.append(node.getNodeName());
        NamedNodeMap attrs = node.getAttributes();
        
        
        
        for (int i = 0; i < attrs.getLength(); i++)
        {
          Node attr = attrs.item(i);
          //domString += (" " + attr.getNodeName().trim() +
          //              "=\"" + attr.getNodeValue().trim() +
          //              "\"");
          
          domStringbuf.append((" " + attr.getNodeName().trim() +
                        "=\"" + attr.getNodeValue().trim() +
                        "\""));
        }
        //domString = domStringbuf.toString();
        domStringbuf.append(">");

        NodeList children = node.getChildNodes();
        if (children != null)
        {
          int len = children.getLength();
          for (int i = 0; i < len; i++)
        	  domStringbuf.append(getDOMString(children.item(i)));
        }
        domStringbuf.append("</");
        domStringbuf.append(node.getNodeName());
        domStringbuf.append(">\n");

        break;
      }

      // handle entity reference nodes
      case Node.ENTITY_REFERENCE_NODE:
      {
    	  domStringbuf.append("&");
    	  domStringbuf.append(node.getNodeName().trim());
    	  domStringbuf.append(";");

        break;
      }

      // print cdata sections
      case Node.CDATA_SECTION_NODE:
      {
    	  domStringbuf.append("");
        break;
      }

      // print text
      case Node.TEXT_NODE:
      {
        String val = node.getNodeValue();
        if (val==null) val = "";
        domStringbuf.append(val);//rshastri .trim() removed SAK-1671 
        break;
      }

      // print processing instruction
      case Node.PROCESSING_INSTRUCTION_NODE:
      {
    	  domStringbuf.append("");
        break;
      }
    }

    if (type == Node.ELEMENT_NODE) {
    	domStringbuf.append("\n");
    }

    String domString = domStringbuf.toString();
    return domString;
  }
  
  public static String processFormattedText(Log log, String value) {
	  if (value == null || value.length() == 0){
		  return value;
	  }
	  StringBuilder alertMsg = new StringBuilder();
	  String finalValue = FormattedText.processFormattedText(value, alertMsg);
	  if (alertMsg.length() > 0)
	  {
		  log.debug(alertMsg.toString());
	  }
	  return finalValue;
  }
}
