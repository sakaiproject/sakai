/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ReferenceMap;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import org.sakaiproject.importer.impl.XPathHelper;

/**
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author rshastri
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */
 @Slf4j
 public class XmlStringBuffer
  implements java.io.Serializable
{

  /**
   * Explicitly setting serialVersionUID insures future versions can be
   * successfully restored. It is essential this variable name not be changed
   * to SERIALVERSIONUID, as the default serialization methods expects this
   * exact name.
   */
  private static final long serialVersionUID = 1;

  //The following need not be persisted and so are declared transient
  private transient Document document = null;
  private transient DocumentBuilder builder = null;
  private transient ReferenceMap cache = null;
  private StringBuffer xml;

  /**
   * Constructor to be accessed by subclasses.
   */
  protected XmlStringBuffer()
  {
    this.xml = new StringBuffer();
  }

  /**
   * Constructs an XmlStringBuffer whose initial value is String.
   *
   * @param xml XML string
   */
  public XmlStringBuffer(String xml)
  {
    this.xml = new StringBuffer(xml);
  }

  /**
   * Constructs an XmlStringBuffer whose initial value is Document
   *
   * @param document XML document
   */
  public XmlStringBuffer(Document document)
  {
    this.document = document;
  }

  /**
   * Constructs an XmlStringBuffer whose initial value is Document
   *
   * @param jdomDoc
   *
   * @deprecated using XmlStringBuffer(org.w3c.dom.Document document) instead.
   */
  public XmlStringBuffer(org.jdom.Document jdomDoc)
  {
    try
    {
      this.document = new DOMOutputter().output(jdomDoc);
    }
    catch(JDOMException e)
    {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Clears the xml
   */
  public final void clear()
  {
    this.xml.setLength(0);
    this.reset();
  }

  /**
   * replace the current xml with the given string
   *
   * @param xml XML replacement string
   *
   * @deprecated
   */
  public final void replace(String xml)
  {
    this.xml = new StringBuffer(xml);
    this.reset();
  }

  /**
   * Get a document
   *
   * @return document
   *
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public final Document getDocument()
    throws ParserConfigurationException, SAXException, IOException
  {
    if(this.document == null)
    {
      this.parseContent();
    }

    return this.document;
  }

  /**
   * internal
   *
   * @return  reference map
   */
  private ReferenceMap getCache()
  {
    if(this.cache == null)
    {
      this.cache = new ReferenceMap();
    }

    return this.cache;
  }

  /**
   * inernal, clear cache
   */
  private void clearCache()
  {
    if(this.cache == null)
    {
      this.cache = new ReferenceMap();
    }
    else
    {
      this.cache.clear();
    }
  }

  /**
   * parse content to JDOM
   *
   * @return JDOM  document
   *
   * @throws JDOMException  =
   * @throws IOException
   */
  
  /*
  private final org.jdom.Document parseContentToJDOM()
    throws JDOMException, IOException
  {
    if(log.isDebugEnabled())
    {
      log.debug("parseContentToJDOM()");
    }

    String xmlString = this.stringValue();
    org.jdom.Document result = null;
    try
    {
      SAXBuilder saxbuilder = new SAXBuilder();
      result = saxbuilder.build(new StringReader(xmlString));
    }
    catch(JDOMException ex)
    {
      log.error("Exception thrown while parsing XML:\n" + ex.getMessage(), ex);
      throw ex;
    }
    catch(IOException ie)
    {
      log.error("Exception thrown while parsing XML:\n" + ie.getMessage(), ie);
      throw ie;
    }

    return result;
  }
*/
  
  /**
   * parse the content
   *
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  private final void parseContent()
    throws ParserConfigurationException, SAXException, IOException
  {
    if(log.isDebugEnabled())
    {
      log.debug("parseContent()");
    }

    this.clearCache();
    DocumentBuilderFactory dbfi = null;
    DocumentBuilder builder = null;
    StringReader sr = null;
    org.xml.sax.InputSource is = null;
    try
    {
    	dbfi = DocumentBuilderFactory.newInstance();
    	builder = dbfi.newDocumentBuilder();
    	String s = this.xml.toString();
    	if (s==null)
    	{
    		log.warn("string value null");
    		s = "";
    	}
    	sr = new StringReader(s);
    	is = new org.xml.sax.InputSource(sr);
    	this.document = builder.parse(is);
    }
    catch(ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
      throw e;
    }
    catch(SAXException e)
    {
      log.error(e.getMessage(), e);
      log.error("DocumentBuilderFactory dbfi = " + dbfi);
      log.error("StringReader sr = " + sr);
      log.error("InputSource is = " + is);
      log.error("StringBuffer xml = " + this.xml);
      throw e;
    }
    catch(IOException e)
    {
      log.error(e.getMessage(), e);
      throw e;
    }
  }

  /**
   * string value of document
   *
   * @return the string
   */
  public final String stringValue()
  {
    if(log.isDebugEnabled())
    {
      log.debug("stringValue()");
    }

    if(document == null)
    {
      return this.xml.toString();
    }
    else
    {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			DOMImplementationRegistry registry = DOMImplementationRegistry
					.newInstance();
			DOMImplementationLS impl = (DOMImplementationLS) registry
					.getDOMImplementation("LS");
			LSSerializer writer = impl.createLSSerializer();
			writer.getDomConfig().setParameter("format-pretty-print",
					Boolean.TRUE);
			LSOutput output = impl.createLSOutput();
			output.setByteStream(out);
			writer.write(document, output);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return out.toString();	
    }
  }

  /**
   * is the xml empty?
   *
   * @return true/false
   */
  public final boolean isEmpty()
  {
    return ((this.xml == null) || (this.xml.length() == 0));
  }

  /**
   *
   */
  private void reset()
  {
    this.document = null;
    this.cache = null;
  }

  /**
   * xpath lookup
   *
   * @param xpath
   * @param type
   *
   * @return value
   */
  public String selectSingleValue(String xpath, String type)
  {
    // user passes the if its an element or attribute
    String value = null;
    List list = this.selectNodes(xpath);
    if(list != null)
    {
    	int no = list.size();

    	if(list.size() > 0)
    	{
    		if((type != null) && type.equals("element"))
    		{
    			Element element = (Element) list.get(0);

    			CharacterData elementText =
    				(CharacterData) element.getFirstChild();
    			Integer getTime = null;
    			if(
    					(elementText != null) && (elementText.getNodeValue() != null) &&
    					(elementText.getNodeValue().trim().length() > 0))
    			{
    				value = elementText.getNodeValue();
    			}
    		}

    		if((type != null) && type.equals("attribute"))
    		{
    			Attr attr = (Attr) list.get(0);

    			CharacterData elementText =
    				(CharacterData) attr.getFirstChild();

    			Integer getTime = null;
    			if(
    					(elementText != null) && (elementText.getNodeValue() != null) &&
    					(elementText.getNodeValue().trim().length() > 0))
    			{
    				value = elementText.getNodeValue();
    			}
    		}
    	}
    }

    return value;
  }

  /**
   * get nodes
   *
   * @param xpath
   *
   * @return list of nodes
   */
  public final List selectNodes(String xpath)
  {
	  if(log.isDebugEnabled())
	  {
		  log.debug("selectNodes(String " + xpath + ")");
	  }

	  return XPathHelper.selectNodes(xpath, this.document);
  }
  
	public String getValueOf(String xpath) {
		return XPathHelper.getNodeValue(xpath, this.document);
	}

  /**
   * perform Update on this object
   *
   * @param xpath :- xpath and
   * @param value :-  Value of xpath
   *
   * @return XmlStringBuffer
   *
   * @throws DOMException
   * @throws Exception
   */

  // Rashmi Aug 19th changed by Pamela on Sept 10th.
  // Rashmi - replacing updateJDOM as on Sep 15
  public final XmlStringBuffer update(String xpath, String value)
    throws DOMException, Exception
  {
    if(log.isDebugEnabled())
    {
      log.debug("update(String " + xpath + ", String " + value + ")");
    }

    try
    {
      Element newElement = null;
      Attr newAttribute = null;
      List newElementList = this.selectNodes(xpath);
      //only look at the last part of the path
      int aIndex = xpath.lastIndexOf("/");
      if(aIndex == -1){
    	  aIndex = 0;
      }
      aIndex = xpath.indexOf("@", aIndex);
      int size = newElementList.size();
      if(size > 1)
      {
        log.info("UPDATING MORE THAN ONE ELEMENT");
      }

      if((aIndex == -1) && (size != 0))
      {
        for(int i = 0; i < size; i++)
        {
          newElement = (Element) newElementList.get(i);
          Node childNode = newElement.getFirstChild();

          if(childNode == null)
          {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.newDocument();
            Text newElementText = document.createTextNode(newElement.getNodeName());
            newElementText.setNodeValue(value);
            Text clonedText =
              (Text) newElement.getOwnerDocument().importNode(
                newElementText, true);
            newElement.appendChild(clonedText);
          }
          else
          {
            CharacterData newElementText =
              (CharacterData) newElement.getFirstChild();
            newElementText.setNodeValue(value);
          }
        }
      }

      if((aIndex != -1) && (size != 0))
      {
        newAttribute = (Attr) newElementList.set(0, null);
        if(newAttribute != null)
        {
          newAttribute.setValue(value);
        }
      }
    }
    catch(Exception ex)
    {
      log.error(ex.getMessage(), ex);
    }

    return this;
  }

  /**
   * update element, xpath
   *
   * @param xpath
   * @param element
   */
  public final void update(String xpath, Element element)
  {
    if(log.isDebugEnabled())
    {
      log.debug("update(String " + xpath + ", Element " + element + ")");
    }

    List itemResults = this.selectNodes(xpath);
    Iterator iterator = itemResults.iterator();
    while(iterator.hasNext())
    {
      Element node = (Element) iterator.next();
      Element replacement =
        (Element) node.getOwnerDocument().importNode(element, true);
      node.getParentNode().replaceChild(replacement, node);
    }

    if(itemResults.size() == 0)
    {
      String parentPath = xpath.substring(0, xpath.lastIndexOf("/"));
      addElement(parentPath, element);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param xpath
   * @param element
   *
   * @deprecated addElement(String, org.w3c.dom.Element)
   */
  public final void addJDOMElement(String xpath, org.jdom.Element element)
  {
    try
    {
      List nodes = this.selectNodes(xpath);
      int size = nodes.size();
      for(int i = 0; i < size; i++)
      {
        org.jdom.Element node = (org.jdom.Element) nodes.get(i);
        node.addContent(element);
      }
    }
    catch(Exception ex)
    {
      log.error(ex.getMessage(), ex);
    }
  }

  /**
   * insert element
   *
   * @param afterNode
   * @param parentXpath
   * @param childXpath
   */
  public void insertElement(
    String afterNode, String parentXpath, String childXpath)
  {
	try {  
      String nextXpath = parentXpath + "/" + afterNode;

      //*************************************************************
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document document = db.newDocument();
      Element element = document.createElement(childXpath);

      //**************************************************************
      Element parent = null;
      List parentNodes = this.selectNodes(parentXpath);
      Iterator iteratorNext = parentNodes.iterator();
      while(iteratorNext.hasNext())
      {
        parent = (Element) iteratorNext.next();
      }

      if(parent != null)
      {
        List nodes = this.selectNodes(nextXpath);
        Iterator iterator = nodes.iterator();
        Element nextSibling = null;
        while(iterator.hasNext())
        {
          nextSibling = (Element) iterator.next();
        }

        if(
          (nextSibling != null) &&
            ! nextSibling.getOwnerDocument().equals(element.getOwnerDocument()))
        {
          element = (Element) parent.getOwnerDocument().importNode(element, true);
          parent.insertBefore(element, nextSibling);
        }
      }
    } catch(ParserConfigurationException pce) {
    	log.error("Exception thrown from insertElement() : " + pce.getMessage());
    }
  }

  /**
   *
   *
   * @param parentXpath
   * @param childXpath
   */
  public final void add(String parentXpath, String childXpath)
  {
    Element childElement = createChildElement(childXpath);
    this.addElement(parentXpath, childElement);
  }

  /**
   * create child
   *
   * @param childXpath
   *
   * @return
   */
  protected final Element createChildElement(String childXpath)
  {
    int index = childXpath.indexOf("/");
    String elementName = childXpath;
    String subChildXpath = null;
    Element element = null;
    Element child = null;
    if(index > 0)
    {
      elementName = childXpath.substring(0, index);
      subChildXpath = childXpath.substring(index + 1);
      child = createChildElement(subChildXpath);
    }
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document document = db.newDocument();
      element = document.createElement(elementName);
      if(child != null)
      {
        Node importedNode = document.importNode(child, true);
        element.appendChild(importedNode);
      }
    } catch(ParserConfigurationException pce) {
    	log.error("Exception thrown from createChildElement(): " + pce.getMessage());
    }
    
    return element;
  }

  /**
   * add element
   *
   * @param parentXpath
   * @param element
   */
  public final void addElement(String parentXpath, Element element)
  {
    if(log.isDebugEnabled())
    {
      log.debug(
        "addElement(String " + parentXpath + ", Element " + element + ")");
    }

    List<Element> nodes = this.selectNodes(parentXpath);
    for(Element parent: nodes){
      if(! parent.getOwnerDocument().equals(element.getOwnerDocument()))
      {
        element = (Element) parent.getOwnerDocument().importNode(element, true);
      }

      parent.appendChild(element);
    }
  }

  /**
   * add attribute
   *
   * @param elementXpath
   * @param attributeName
   */
  public final void addAttribute(String elementXpath, String attributeName)
  {
    if(log.isDebugEnabled())
    {
      log.debug(
        "addAttribute(String " + elementXpath + ", String" + attributeName +
        ")");
    }

    List nodes = this.selectNodes(elementXpath);
    int size = nodes.size();
    for(int i = 0; i < size; i++)
    {
      Element element = (Element) nodes.get(i);
      element.setAttribute(attributeName, "");
    }
  }

  /**
   * remove element
   *
   * @param xpath
   */
  public final void removeElement(String xpath)
  {
    if(log.isDebugEnabled())
    {
      log.debug("removeElement(String " + xpath + ")");
    }

    List nodes = this.selectNodes(xpath);
    Iterator iterator = nodes.iterator();
    while(iterator.hasNext())
    {
      Node node = (Node) iterator.next();
      Node parent = node.getParentNode();
      parent.removeChild(node);
    }
  }

  /**
   * Synchronizes object prior to serialization
   *
   * @param out ObjectOutputStream
   *
   * @throws IOException
   */
  private void writeObject(java.io.ObjectOutputStream out)
    throws IOException
  {
    if(log.isDebugEnabled())
    {
      log.debug("writeObject(ObjectOutputStream " + out + ")");
    }

    this.xml = new StringBuffer(this.stringValue());
    out.defaultWriteObject();
  }
}

