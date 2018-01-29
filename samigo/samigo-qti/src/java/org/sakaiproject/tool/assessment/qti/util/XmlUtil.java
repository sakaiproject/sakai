/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.qti.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.xml.XMLConstants;
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.core.io.ClassPathResource;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author palcasi
 * mods:
 * @author Rachel Gollub rgollub@stanford.edu
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */
@Slf4j
public final class XmlUtil
{
  private static String[] M_goodTags = "a,abbr,acronym,address,b,big,blockquote,br,center,cite,code,dd,del,dir,div,dl,dt,em,font,hr,h1,h2,h3,h4,h5,h6,i,ins,kbd,li,marquee,menu,nobr,noembed,ol,p,pre,q,rt,ruby,rbc,rb,rtc,rp,s,samp,small,span,strike,strong,sub,sup,tt,u,ul,var,xmp,img,embed,object,table,tr,td,th,tbody,caption,thead,tfoot,colgroup,col,param".split(",");
  private static Pattern[] M_goodTagsPatterns;
  private static Pattern[] M_goodCloseTagsPatterns;
  private static Pattern M_htmlPattern = Pattern.compile("(<([a-z]\\w*)\\b[^>]*>)|(</\\s*[a-z]\\w*(\\s.*>|>))|(<([a-z]\\w*)\\b[^>]*/>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private static FormattedText formattedText = (FormattedText)ComponentManager.get(FormattedText.class);

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
      setDocumentBuilderFactoryFeatures(builderFactory);
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
      setDocumentBuilderFactoryFeatures(builderFactory);	
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

    Document document = null;
    InputStream inputStream = context.getResourceAsStream(path);
    String fullpath = context.getRealPath(path);
      log.debug("readDocument(full path) " + fullpath + ")");
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);

    try
    {
      setDocumentBuilderFactoryFeatures(builderFactory);	
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

    Document document = null;
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
    ClassPathResource resource = new ClassPathResource(path);

    try
    {

      InputStream inputStream = resource.getInputStream();

      setDocumentBuilderFactoryFeatures(builderFactory);
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
    	log.debug(e1.getMessage(), e1);    
    }
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);

    Document document = null;
    DOMSource source = null;
    try
    {
      setDocumentBuilderFactoryFeatures(builderFactory);	
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
 * @throws ParserConfigurationException 
 * @throws SAXException 
 * @throws IOException 
   */
 public static Document readDocument(String path, boolean trim) throws ParserConfigurationException, SAXException, IOException
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
      setDocumentBuilderFactoryFeatures(builderFactory);
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      document = documentBuilder.parse(inputStream);
    }
    catch(ParserConfigurationException e)
    {
    	log.debug(e.getMessage(), e);
      throw(e);
    }
    catch(SAXException e)
    {
    	log.debug(e.getMessage(), e);
    	throw(e);
    }
    catch(IOException e)
    {
      log.debug(e.getMessage(), e);
      throw(e);
    }
    finally {

    	if (inputStream != null) {
    		try {
    			inputStream.close();
    		}
    		catch (IOException e) {
    			log.debug(e.getMessage(), e);// tried
    		}
    	}
    	if (in !=null){
    		try {
    			in.close();
    		} catch (Exception e1) {
    			log.debug(e1.getMessage(), e1);
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

    if(log.isDebugEnabled()) log.debug("Document transformDocument(Document " + document + ", Trasformer " + transformer);

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
  
  public static String processFormattedText(String value) {
      if (StringUtils.isEmpty(value)) {
          return value;
      }
      StringBuilder alertMsg = new StringBuilder();
      String finalValue = "";
      Matcher matcher = M_htmlPattern.matcher(value);
      boolean hasHtmlPattern = false;
      int index = 0;
      StringBuilder textStringBuilder = new StringBuilder();
      String tmpText = "";
      if (M_goodTagsPatterns == null || M_goodCloseTagsPatterns == null) {
          M_goodTagsPatterns = new Pattern[M_goodTags.length];
          M_goodCloseTagsPatterns = new Pattern[M_goodTags.length];
          for (int i = 0; i < M_goodTags.length; i++) {
              M_goodTagsPatterns[i] = Pattern.compile(".*<\\s*" + M_goodTags[i] + "(\\s+.*>|>|/>).*",
                      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
              M_goodCloseTagsPatterns[i] = Pattern.compile("<\\s*/\\s*" + M_goodTags[i] + "(\\s.*>|>)",
                      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
          }
      }
      while (matcher.find()) {
          hasHtmlPattern = true;
          tmpText = value.substring(index, matcher.start());
          textStringBuilder.append(convertoLTGT(tmpText));
          String group = matcher.group();
          boolean isGoodTag = false;
          for (int i = 0; i < M_goodTags.length; i++) {
              if (M_goodTagsPatterns[i].matcher(group).matches() || M_goodCloseTagsPatterns[i].matcher(group).matches()) {
                  textStringBuilder.append(group);
                  isGoodTag = true;
                  break;
              }
          }
          if (!isGoodTag) {
              textStringBuilder.append(convertoLTGT(group));
          }
          index = matcher.end();
      }
      textStringBuilder.append(convertoLTGT(value.substring(index)));
      if (hasHtmlPattern) {
          finalValue = formattedText.processFormattedText(textStringBuilder.toString(), alertMsg);
      } else {
          finalValue = formattedText.processFormattedText(convertoLTGT(value), alertMsg);
      }
      if (alertMsg.length() > 0) {
          log.debug(alertMsg.toString());
      }
      return finalValue;
  }

  public static String convertoLTGT(String value) {
      return StringUtils.replaceEach(value, new String[]{"<", ">"}, new String[]{"&lt;", "&gt;"});
  }

  /**
 * @param myString
 * @return
 * @deprecated use convertToSingleCDATA
 */
@Deprecated
  public static String convertStrforCDATA(String myString)
  {
	  StringBuffer sbuff = new StringBuffer("<![CDATA[");
	  String sTemp = null;

	  String escapeStr = myString.replaceAll("]]>", "]]&gt;");

	  for (int i = 0; i < escapeStr.length(); i++){
		  if (escapeStr.charAt(i) < 32 || escapeStr.charAt(i) == 127){
			  sTemp = "]]>&#"+ (int)escapeStr.charAt(i)+ ";<![CDATA[";
		  }
		  else{
			  sTemp = Character.toString(escapeStr.charAt(i));
		  }
		  sbuff.append(sTemp);
		  sTemp =null;
	  }
	  sbuff.append("]]>");
	  return sbuff.toString();
  } 
  
  public static String convertToSingleCDATA(String text){
	  return "<![CDATA[" + text + "]]>";
  }
  
  private static void setDocumentBuilderFactoryFeatures(
	  DocumentBuilderFactory builderFactory) throws ParserConfigurationException {
	      builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
	      builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	      builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	  }
}
