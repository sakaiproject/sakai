/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.qti.helper;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.CharacterDataImpl;
import org.apache.xerces.dom.CoreDocumentImpl;
import org.apache.xerces.dom.TextImpl;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.util.PathInfo;


/**
 * <p>Utility to load XML templates from Faces context or local file system.</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005 Sakai</p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class AuthoringXml
{
  private static Log log = LogFactory.getLog(AuthoringXml.class);

  public static final String SETTINGS_FILE = "SAM.properties";
  // paths
  public static final String TEMPLATE_PATH = "/xml/author/";
  public static final String SURVEY_PATH = "survey/";
  //assessment template
  public static final String ASSESSMENT = "assessmentTemplate.xml";
  // section template
  public static final String SECTION = "sectionTemplate.xml";
  // item templates
  public static final String ITEM_AUDIO = "audioRecordingTemplate.xml";
  public static final String ITEM_ESSAY = "essayTemplate.xml";
  public static final String ITEM_FIB = "fibTemplate.xml";
  public static final String ITEM_FILE = "fileUploadTemplate.xml";
  public static final String ITEM_MATCH = "matchTemplate.xml";
  public static final String ITEM_MCMC = "mcMCTemplate.xml";
  public static final String ITEM_MCSC = "mcSCTemplate.xml";
  public static final String ITEM_SURVEY = "mcSurveyTemplate.xml";
  public static final String ITEM_TF = "trueFalseTemplate.xml";
  public static final String ITEM_MATCHING = "matchTemplate.xml";
  public static final String SURVEY_10 = SURVEY_PATH + "10.xml";
  public static final String SURVEY_5 = SURVEY_PATH + "5.xml";
  public static final String SURVEY_AGREE = SURVEY_PATH + "AGREE.xml";
  public static final String SURVEY_AVERAGE = SURVEY_PATH + "AVERAGE.xml";
  public static final String SURVEY_EXCELLENT = SURVEY_PATH +
    "EXCELLENT.xml";
  public static final String SURVEY_STRONGLY = SURVEY_PATH +
    "STRONGLY_AGREE.xml";
  public static final String SURVEY_UNDECIDED = SURVEY_PATH +
    "UNDECIDED.xml";
  public static final String SURVEY_YES = SURVEY_PATH + "YES.xml";

  private static final String QTI_12_PATH = "v1p2";
  private static final String QTI_20_PATH = "v2p0";

  public Map validTemplates = null;
  private int qtiVersion;
  private String qtiPath;

  private AuthoringXml()
  {
    initTemplates();
  }

  public AuthoringXml(int qtiVersion)
  {
    this.qtiVersion = qtiVersion;
    if (qtiVersion == QTIVersion.VERSION_1_2)
    {
      qtiPath = QTI_12_PATH;
    }
    else if (qtiVersion == QTIVersion.VERSION_2_0)
    {
      qtiPath = QTI_20_PATH;
    }
    else
    {
      throw new IllegalArgumentException("Unsupported qti version");
    }

    initTemplates();
  }

  private void initTemplates()
  {
    validTemplates = new HashMap();
    validTemplates.put(ASSESSMENT, "assessmentTemplate.xml");
    validTemplates.put(SECTION, "assessmentTemplate.xml");
    validTemplates.put(ITEM_AUDIO, "audioRecordingTemplate.xml");
    validTemplates.put(ITEM_ESSAY, "essayTemplate.xml");
    validTemplates.put(ITEM_FIB, "fibTemplate.xml");
    validTemplates.put(ITEM_FILE, "fileUploadTemplate.xml");
    validTemplates.put(ITEM_MATCH, "matchTemplate.xml");
    validTemplates.put(ITEM_MCMC, "mcMCTemplate.xml");
    validTemplates.put(ITEM_MCSC, "mcSCTemplate.xml");
    validTemplates.put(ITEM_SURVEY, "mcSurveyTemplate.xml");
    validTemplates.put(ITEM_TF, "trueFalseTemplate.xml");
    validTemplates.put(SURVEY_10, SURVEY_PATH + "10.xml");
    validTemplates.put(SURVEY_5, SURVEY_PATH + "5.xml");
    validTemplates.put(SURVEY_AGREE, SURVEY_PATH + "AGREE.xml");
    validTemplates.put(SURVEY_AVERAGE, SURVEY_PATH + "AVERAGE.xml");
    validTemplates.put(SURVEY_EXCELLENT, SURVEY_PATH + "EXCELLENT.xml");
    validTemplates.put(SURVEY_STRONGLY, SURVEY_PATH + "STRONGLY_AGREE.xml");
    validTemplates.put(SURVEY_UNDECIDED, SURVEY_PATH + "UNDECIDED.xml");
    validTemplates.put(SURVEY_YES, SURVEY_PATH + "YES.xml");
  }

  /**
   * test that a String is a valid template key
   * @param s a key
   * @return true if it is a valid key
   */
  public boolean valid(String s)
  {
    return validTemplates.containsKey(s);
  }

  /**
   * get template as stream using faces context
   * @param templateName
   * @param context
   * @return
   */
  public InputStream getTemplateInputStream(String templateName,
    FacesContext context)
  {
    InputStream is = null;

    if (context!=null) {
    try
    {
      if (!this.valid(templateName))
      {
        throw new IllegalArgumentException("not a valid template: " +
          templateName);
      }
        is =
          context.getExternalContext().getResourceAsStream(
          TEMPLATE_PATH + "/" + qtiPath + "/" + templateName);
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }
    }
    else {
      // if not coming from a jsf page
      is = getTemplateInputStream(templateName);
    }


    return is;
  }

  /**
   * get template as stream using local context
   * this presupposes a path of TEMPLATE_PATH off of /
   * this is useful for unit testing
   * @param templateName
   * @return the input stream
   */
  public InputStream getTemplateInputStream(String templateName)
  {
    InputStream is = null;

    try
    {

    Properties props =
        PathInfo.getInstance().getSettingsProperties(SETTINGS_FILE);
    String basePath = props.getProperty("templateBasePath");

      if (!this.valid(templateName))
      {
        throw new IllegalArgumentException("not a valid template: " +
          templateName);
      }
	is = new FileInputStream(basePath + TEMPLATE_PATH + "/" + qtiPath + "/" + templateName);

    }
    catch (FileNotFoundException e)
    {
      log.error(e.getMessage(), e);
    }
    catch (IOException e1)
    {
      log.error(e1.getMessage(), e1);
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }
    return is;
  }

  /**
   * get a template as a string from its input stream
   * @param templateName
   * @return the xml string
   */
  public String getTemplateAsString(InputStream templateStream)
  {
    InputStreamReader reader;
    String xmlString = null;
    try
    {
      reader = new InputStreamReader(templateStream);
      StringWriter out = new StringWriter();
      int c;

      while ( (c = reader.read()) != -1)
      {
        out.write(c);
      }

      reader.close();
      xmlString = (String) out.toString();
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }

    return xmlString;

  }

  public boolean isAssessment(String documentType)
  {
    if (ASSESSMENT.equals(documentType))
    {
      return true;
    }
    return false;
  }

  public boolean isSection(String documentType)
  {
    if (SECTION.equals(documentType))
    {
      return true;
    }
    return false;
  }

  public boolean isItem(String documentType)
  {
    if (valid(documentType) && documentType.startsWith("ITEM_"))
    {
      return true;
    }
    return false;
  }

  public boolean isSurveyFragment(String documentType)
  {
    if (valid(documentType) && documentType.startsWith("SURVEY_"))
    {
      return true;
    }
    return false;
  }

  /**
   * Based on method in XmlStringBuffer
   * @author rpembry
   * @author casong changed XmlStringBuffer to be org.w3c.dom compliance,
   * @author Ed Smiley esmiley@stanford.edu changed method signatures used Document
   * @param document Document
   * @param xpath
   * @param element
   * @return modified Document
   */
  public Document update(Document document, String xpath, Element element)
  {
    if (log.isDebugEnabled())
    {
      log.debug("update(String " + xpath + ", Element " + element + ")");
    }

    List itemResults = this.selectNodes(document, xpath);
    Iterator iterator = itemResults.iterator();
    while (iterator.hasNext())
    {
      Element node = (Element) iterator.next();
      Element replacement =
        (Element) node.getOwnerDocument().importNode(element, true);
      node.getParentNode().replaceChild(replacement, node);
    }

    if (itemResults.size() == 0)
    {
      String parentPath = xpath.substring(0, xpath.lastIndexOf("/"));
      addElement(document, parentPath, element);
    }

    return document;
  }

  /**
   * perform Update on this object
   * Based on method originally in XmlStringBuffer
   * @author rashmi
   * @author casong
   * @author Ed Smiley esmiley@stanford.edu changed method signatures used Document
   * @param document Document
   * @param xpath :- xpath and
   * @param value :-  Value of xpath
   *
   * @return modified Document
   * @throws DOMException DOCUMENTATION PENDING
   * @throws Exception DOCUMENTATION PENDING
   */
  public Document update(Document document, String xpath,
    String value) throws DOMException, Exception
  {
    if (log.isDebugEnabled())
    {
      log.debug("update(String " + xpath + ", String " + value + ")");
    }

    try
    {
      Element newElement = null;
      Attr newAttribute = null;
      List newElementList = this.selectNodes(document, xpath);
      int aIndex = xpath.indexOf("@");
      int size = newElementList.size();
      if (size > 1)
      {
        log.warn("UPDATING MORE THAN ONE ELEMENT");
      }

      if ( (aIndex == -1) && (size != 0))
      {
        for (int i = 0; i < size; i++)
        {
          newElement = (Element) newElementList.get(i);
          Node childNode = newElement.getFirstChild();

          if (childNode == null)
          {
            CoreDocumentImpl core = new CoreDocumentImpl();
            TextImpl newElementText =
              new TextImpl(core, newElement.getNodeName());
            newElementText.setNodeValue(value);
            TextImpl clonedText =
              (TextImpl) newElement.getOwnerDocument().importNode(
              newElementText, true);
            newElement.appendChild(clonedText);
          }
          else
          {
            CharacterDataImpl newElementText =
              (CharacterDataImpl) newElement.getFirstChild();
            newElementText.setNodeValue(value);
          }
        }
      }

      if ( (aIndex != -1) && (size != 0))
      {
        newAttribute = (Attr) newElementList.set(0, newAttribute);
        if (newAttribute != null)
        {
          newAttribute.setValue(value);
        }
      }
    }
    catch (Exception ex)
    {
      log.error(ex.getMessage(), ex);
    }

    return document;
  }

  /**
   * Based on method in XmlStringBuffer
   * @author rpembry
   * @author casong changed XmlStringBuffer to be org.w3c.dom compliance,
   * @author Ed Smiley esmiley@stanford.edu changed method signatures used Document
   *
   * @param document Document
   * @param parentXpath
   * @param element
   * @return modified Document
   */
  public Document addElement(Document document, String parentXpath,
    Element element)
  {
    if (log.isDebugEnabled())
    {
      log.debug(
        "addElement(String " + parentXpath + ", Element " + element + ")");
    }

    List nodes = this.selectNodes(document, parentXpath);
    Iterator iterator = nodes.iterator();
    while (iterator.hasNext())
    {
      Element parent = (Element) iterator.next();
      if (!parent.equals(element.getOwnerDocument()))
      {
        element = (Element) parent.getOwnerDocument().importNode(element, true);
      }

      parent.insertBefore(element, null);// inserts at end, as before-reference is null
    }
    return document;
  }

  /**
   * Based on method in XmlStringBuffer
   * @author rpembry
   * @author casong changed XmlStringBuffer to be org.w3c.dom compliance,
   * @author Ed Smiley esmiley@stanford.edu changed method signatures used Document
   *
   * @param document Document
   * @param elementXpath
   * @param attributeName
   * @return modified Document
   */
  public Document addAttribute(Document document, String elementXpath,
    String attributeName)
  {
    if (log.isDebugEnabled())
    {
      log.debug(
        "addAttribute(String " + elementXpath + ", String" + attributeName +
        ")");
    }

    List nodes = this.selectNodes(document, elementXpath);
    int size = nodes.size();
    for (int i = 0; i < size; i++)
    {
      Element element = (Element) nodes.get(i);
      element.setAttribute(attributeName, "");
    }

    return document;
  }

  /**
   * Based on method in XmlStringBuffer
   * @author rpembry
   * @author casong changed XmlStringBuffer to be org.w3c.dom compliance,
   * @author Ed Smiley esmiley@stanford.edu changed method signatures used Document
   *
   * @return a List of Nodes
   */
  public final List selectNodes(Document document, String xpath)
  {
    if (log.isDebugEnabled())
    {
      log.debug("selectNodes(String " + xpath + ")");
    }

    List result = new ArrayList();

    try
    {
      XPath path = new DOMXPath(xpath);
      result = path.selectNodes(document);
    }
    catch (JaxenException je)
    {
      log.error(je.getMessage(), je);
    }

    return result;
  }

  /**
   * read in XML document from input stream
   * @param inputStream source for XML document
   * @return the Document
   */
  public Document readXMLDocument(InputStream inputStream)
  {
    if (log.isDebugEnabled())
    {
      log.debug("readDocument(InputStream " + inputStream);
    }

    Document document = null;
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);

    try
    {
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      document = documentBuilder.newDocument();
      document = documentBuilder.parse(inputStream);
    }
    catch (ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }
    catch (SAXException e)
    {
      log.error(e.getMessage(), e);
    }
    catch (IOException e)
    {
      log.error(e.getMessage(), e);
    }

    return document;
  }

  /**
   * Read a DOM Document from xml in a string.
   * @param in The string containing the XML
   * @return A new DOM Document with the xml contents.
   */
  public static Document readDocumentFromString(String in)
  {
    try
    {
      DocumentBuilder docBuilder = null;

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(false);
      docBuilder = dbf.newDocumentBuilder();
      InputSource inputSource = new InputSource(new StringReader(in));
      Document doc = docBuilder.parse(inputSource);
      return doc;
    }
    catch (Exception any)
    {
      log.warn("Xml.readDocumentFromString: " + any.toString());
      return null;
    }

  }
  public int getQtiVersion()
  {
    return qtiVersion;
  }
  public void setQtiVersion(int qtiVersion)
  {
    this.qtiVersion = qtiVersion;
  }
  public String getQtiPath()
  {
    return qtiPath;
  }
  public void setQtiPath(String qtiPath)
  {
    this.qtiPath = qtiPath;
  }/// readDocumentFromString

}


