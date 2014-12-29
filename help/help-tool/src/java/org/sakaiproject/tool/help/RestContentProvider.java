/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.help;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import javax.servlet.ServletContext;
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
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.api.app.help.Resource;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;




public class RestContentProvider
{
  
  private static final String XML_PREPROCESS_XSL = "/xsl/kbxml-preprocess.xsl";
  private static final String XML_KB_XSL = "/xsl/kb.xsl";
  private static Boolean XSL_INITIALIZED = Boolean.FALSE;
  private static Object XSL_INITIALIZED_LOCK = new Object();

  private static Document xslDocumentPreprocess;
  private static Document xslDocumentAllInOne;
    
  private static final Log LOG = LogFactory
      .getLog(RestContentProvider.class);

  /**
   * @param htmlDocument
   * @return document with css link
   */
  private static void addLinkToCss(Document htmlDocument)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("addLinkToCss(Document " + htmlDocument + ")");
    }
        
    String skinRoot = ServerConfigurationService.getString("skin.repo",
        "/library/skin");
    String skin = ServerConfigurationService.getString("skin.default",
        "default");
    
    NodeList nodes = htmlDocument.getElementsByTagName("head");
    Node node = nodes.item(0);
        
    Element linkNodeBase = htmlDocument.createElement("link");
    linkNodeBase.setAttribute("href", skinRoot + "/tool_base.css");
    linkNodeBase.setAttribute("rel", "stylesheet");
    linkNodeBase.setAttribute("content-type", "text/css");
    
    Element linkNodeDefault = htmlDocument.createElement("link");
    linkNodeDefault.setAttribute("href", skinRoot + "/" + skin + "/tool.css");
    linkNodeDefault.setAttribute("rel", "stylesheet");
    linkNodeDefault.setAttribute("content-type", "text/css");
    
    Element linkNodeREST = htmlDocument.createElement("link");
    linkNodeREST.setAttribute("href", "css/REST.css");
    linkNodeREST.setAttribute("rel", "stylesheet");
    linkNodeREST.setAttribute("content-type", "text/css");
    
    if (node.getFirstChild() == null
        || !(node.getFirstChild().getNodeName().equals("link")))
    {
      //node.appendChild(linkNodeBase);
      //node.appendChild(linkNodeDefault);
      node.appendChild(linkNodeREST);
    }    
  }

  /**
   * 
   * @param document
   * @return serialized String
   */
  private static String serializeDocument(Document document)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("serializeDocumentDocument(Document " + document + ")");
    }

    if (document != null)
    {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	Source xmlSource = new DOMSource(document);
    	Result outputTarget = new StreamResult(out);
    	Transformer tf;
		try {
			tf = TransformerFactory.newInstance().newTransformer();
	    	tf.transform(xmlSource, outputTarget);
		} catch (TransformerException e) {
			LOG.warn(e);
		}
    	return out.toString();
    }
    else
    {
      return "<html><body>Unable to retrieve document</body></html>";
    }
  }

  /**
   * Apply transform
   * @param transformer
   * @param source
   * @param result
   */
  private static void transform(Transformer transformer, Source source,
      Result result)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("transform(Transformer " + transformer + ", Source" + source
          + ", Result " + result + ")");
    }

    try
    {
      transformer.transform(source, result);
    }
    catch (TransformerException e)
    {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * transform document
   * @param document
   * @param stylesheet
   * @return
   */
  private static Document transformDocument(Document document,
      Document stylesheet)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("transformDocument(Document " + document + ", Document "
          + stylesheet + ")");
    }

    Document transformedDoc = createDocument();
    DOMSource docSource = new DOMSource(document);
    DOMResult docResult = new DOMResult(transformedDoc);
    Transformer transformer = createTransformer(stylesheet);
    transform(transformer, docSource, docResult);

    return transformedDoc;
  }

  /**
   * create document
   * @return document
   */
  private static Document createDocument()
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("createDocument()");
    }

    Document document = null;
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory
        .newInstance();
    builderFactory.setNamespaceAware(true);

    try
    {
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      document = documentBuilder.newDocument();
    }
    catch (ParserConfigurationException e)
    {
      LOG.error(e.getMessage(), e);
      e.printStackTrace();
    }

    return document;
  }

  /**
   * create transformer
   * @param stylesheet
   * @return
   */
  private static Transformer createTransformer(Document stylesheet)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("createTransformer(Document " + stylesheet + ")");
    }

    Transformer transformer = null;
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    URIResolver resolver = new URIResolver();
    transformerFactory.setURIResolver(resolver);

    try
    {
      DOMSource source = new DOMSource(stylesheet);
      String systemId = "/xsl";
      source.setSystemId(systemId);
      transformer = transformerFactory.newTransformer(source);
    }
    catch (TransformerConfigurationException e)
    {
      LOG.error(e.getMessage(), e);
      e.printStackTrace();
    }

    return transformer;
  }

  /**
   * synchronize initialization of caching XSL
   * @param context
   */
  public static void initializeXsl(ServletContext context)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("initializeXsl(ServletContext " + context + ")");
    }
    
    if (XSL_INITIALIZED.booleanValue())
    {
      return;
    }
    else
    {
      synchronized (XSL_INITIALIZED_LOCK)
      {
        if (!XSL_INITIALIZED.booleanValue())
        {
          //read in and parse xsl
          InputStream iStreamPreprocess = null;
          InputStream iStreamAllInOne = null;
          try
          {
            iStreamPreprocess = context.getResourceAsStream(XML_PREPROCESS_XSL);
            iStreamAllInOne = context.getResourceAsStream(XML_KB_XSL);           
            
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                .newInstance();
            builderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = builderFactory
                .newDocumentBuilder();
            xslDocumentPreprocess = documentBuilder.parse(iStreamPreprocess);
            xslDocumentAllInOne = documentBuilder.parse(iStreamAllInOne);            
          }
          catch (ParserConfigurationException e)
          {
            LOG.error(e.getMessage(), e);
          }
          catch (IOException e)
          {
            LOG.error(e.getMessage(), e);
          }
          catch (SAXException e)
          {
            LOG.error(e.getMessage(), e);
          }
          try
          {
            iStreamPreprocess.close();
            iStreamAllInOne.close();
          }
          catch (IOException e)
          {
            LOG.error(e.getMessage(), e);
          }

          XSL_INITIALIZED = Boolean.TRUE;
        }
      }
    }
  }

  /**
   * get transformed document
   * @param servlet context
   * @param sBuffer
   * @return
   */
  private static Document getTransformedDocument(ServletContext context,
      StringBuilder sBuffer)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTransformedDocument(ServletContext " + context
          + ", StringBuilder " + sBuffer + ")");
    }

    initializeXsl(context);

    Document result = null;
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = dbf.newDocumentBuilder();
      StringReader sReader = new StringReader(sBuffer.toString());
      InputSource is = new org.xml.sax.InputSource(sReader);
      Document xmlDocument = builder.parse(is);
      
      // test for kb error condition
      if (xmlDocument.getElementsByTagName("kberror").getLength() > 0){        
        result = createErrorDocument();
      }
      else{
        /** debugging
        OutputFormat format = new OutputFormat(xmlDocument);
        XMLSerializer output = new XMLSerializer(System.out, format);
        output.serialize(xmlDocument);
        */
        
        result = transformDocument(xmlDocument, xslDocumentPreprocess);  
        result = transformDocument(result, xslDocumentAllInOne);
      } 
      
      /** debugging  
      OutputFormat format = new OutputFormat(result);
      XMLSerializer output = new XMLSerializer(System.out, format);
      output.serialize(result);
      */
           
      addLinkToCss(result);

    }
    catch (ParserConfigurationException e)
    {
      LOG.error(e.getMessage(), e);
    }
    catch (IOException e)
    {
      LOG.error(e.getMessage(), e);
    }
    catch (SAXException e)
    {
      LOG.error(e.getMessage(), e);
    }
    return result;
  }
   

  /**
   * get transformed document
   * @param context
   * @return transformed document
   */
  public static String getTransformedDocument(ServletContext context,
      HelpManager helpManager, Resource resource)
  {
    
	Long now = new Long((new Date()).getTime());
	  
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTransformedDocument(ServletContext " + context
          + ", HelpManager " + helpManager + "String " + resource.getDocId() + ")");
    }
    
    // test if resource is cached
    if (resource.getTstamp() != null){
      if ((now.longValue() - resource.getTstamp().longValue()) < helpManager.getRestConfiguration().getCacheInterval()){
        if (LOG.isDebugEnabled()){
          LOG.debug("retrieving document: " + resource.getDocId() + " from cache");                
        }
        return resource.getSource();
      }
    }
        
    URL url = null;
    String transformedString = null;
    try
    {
      url = new URL(helpManager.getRestConfiguration().getRestUrlInDomain() + resource.getDocId() + "?domain="
          + helpManager.getRestConfiguration().getRestDomain());
      URLConnection urlConnection = url.openConnection();

      String basicAuthUserPass = helpManager.getRestConfiguration().getRestCredentials();

      String encoding = Base64.encodeBase64(basicAuthUserPass.getBytes("utf-8")).toString();
      urlConnection.setRequestProperty("Authorization", "Basic " + encoding);

      StringBuilder sBuffer = new StringBuilder();

      BufferedReader br = new BufferedReader(
              new InputStreamReader(urlConnection.getInputStream(),"UTF-8"), 512);
      try {
          int readReturn = 0;
          char[] cbuf = new char[512];
          while ((readReturn = br.read(cbuf, 0, 512)) != -1)
          {
            sBuffer.append(cbuf, 0, readReturn);
          }
      } finally {
          br.close();
      }

      Document transformedDocument = getTransformedDocument(context, sBuffer);
      transformedString = serializeDocument(transformedDocument);            
    }
    catch (MalformedURLException e)
    {
      LOG.error("Malformed URL in REST document: " + url.getPath());
    }
    catch (IOException e)
    {
      LOG.error("Could not open connection to REST document: " + url.getPath());
    }
        
    resource.setSource(transformedString);
    resource.setTstamp(now);
    helpManager.storeResource(resource);
    
    return transformedString;
  }
  
  
  /**
   * Given any error condition, create an error document including css
   * @return Document
   */
  public static Document createErrorDocument(){
    Document errorDocument = createDocument();    
      
    Element html = errorDocument.createElement("html");    
    Element head = errorDocument.createElement("head");
    Element body = errorDocument.createElement("body");
    Element p = errorDocument.createElement("p");
    
    Text textNode = errorDocument.createTextNode("An error retrieving document from knowledge base has occurred.");
    
    p.appendChild(textNode);
    body.appendChild(p);    
    html.appendChild(head);
    html.appendChild(body);        
    errorDocument.appendChild(html);
                            
    return errorDocument;    
  }
}
