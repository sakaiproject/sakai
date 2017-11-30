/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.qti;

import java.io.InputStream;
import java.io.Serializable;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringXml;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;

/**
 * <p>Bean for QTI XML or XML fragments and descriptive information. </p>
 * <p>Used to maintain information or to dump XML to client.</p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * @author Ed Smiley esmiley@stanford.edu
   * @version $Id$
 */
@Slf4j
public class XMLController implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 7064783681056628447L;

  private static final String XML_DECL =
    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "\n";

  private XMLDisplay xmlBean;
  private String documentType;
  private String id;
  private int qtiVersion;

  public XMLController()
  {
    qtiVersion = QTIVersion.VERSION_1_2; //default
  }

  public XMLDisplay getXmlBean()
  {
    return xmlBean;
  }

  public void setXmlBean(XMLDisplay xmlBean)
  {
    this.xmlBean = xmlBean;
  }

  /**
   * sets needed info in xml display bean when id set to assessment id
   * @return
   */
  public String displayAssessmentXml()
  {
    log.debug(
      "XMLController debug getQtiVersion(): " + this.getQtiVersion());
    documentType = AuthoringXml.ASSESSMENT;
    return display();
  }

  public String displaySectionXmlTemplate()
  {
    documentType = AuthoringXml.SECTION;
    return display();
  }

  public String displayItemXml()
  {
    documentType = AuthoringXml.ITEM_MCSC; // this is just a default, we will override
    item();
    return "xmlDisplay";
  }

  public String displayItemBankXml()
  {
    this.itemBank();
    return "xmlDisplay";
  }

  public String display()
  {
    AuthoringXml ax = getAuthoringXml();

    try
    {
      if (ax.isAssessment(documentType))
      {
        assessment();
      }
      else if (ax.isSection(documentType))
      {
        section();
      }
      else if (ax.isItem(documentType))
      {
        log.debug("item type detected");
        item();
      }
      else if (ax.isSurveyFragment(documentType))
      {
        frag();
      }
    }
    catch (Exception ex)
    {
      setUpXmlNoDecl("<error-report>" + "\n" +
                     ex.toString() + "\n" +
                     "</error-report>" + "\n");
      xmlBean.setDescription(ex.toString());
      xmlBean.setName("error");
      xmlBean.setId("");
      log.error(ex.getMessage(), ex);
    }

    return "xmlDisplay";

  }

  public String getDocumentType()
  {
    return documentType;
  }

  public void setDocumentType(String documentType)
  {
    this.documentType = documentType;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  private void assessment()
  {
    xmlBean.setId(id);
    xmlBean.setName(documentType);

    if (id != null && id.length() > 0) // return populated xml from template
    {
      QTIService qtiService = new QTIService();
      log.debug("XMLController.assessment() assessment " +
                         "qtiService.getExportedAssessment(id=" + id +
                         ", qtiVersion=" +
                         qtiVersion + ")");
      Document doc = qtiService.getExportedAssessment(id, qtiVersion);
      xmlBean.setDescription(
        "Exported QTI XML produced by Sakai's Tests and Quizzes tool (Samigo)");
      xmlBean.setName("assessment " + id);
      setUpXmlNoDecl(XmlUtil.getDOMString(doc));
    }
    else // return  xml template, for testing
    {
      xmlBean.setDescription("assessment template");
      //AuthoringHelper authHelper = new AuthoringHelper(qtiVersion);
      AuthoringXml ax = getAuthoringXml();

      String xml =
        ax.getTemplateAsString(
        ax.getTemplateInputStream(AuthoringXml.ASSESSMENT));
      setUpXmlNoDecl(xml);
    }
  }


  /**
   * A utility method to set xml string in xml bean.
   * @param xml the XML string
   */
  private void setUpXmlNoDecl(String xml)
  {
    setUpXml(xml, false);
  }

  /**
   * Logic for set xml string in xml bean with or without xml declaration.
   * @param xml the XML string
   * @param includeXmlDecl include "<?xml version... ? true or false?
   */
  private void setUpXml(String xml, boolean includeXmlDecl)
  {
    boolean hasXmlDecl = false;
    String startDecl = "<?xml version";
    String endDecl = "?>";
    int endDeclLength = endDecl.length();

    if (xml.startsWith(startDecl))
    {
      hasXmlDecl = true;
    }

    // if we want a decl add it if it doesn't have one
    // if we don't want decl remove it if it is there
    if (includeXmlDecl)
    {
      if (!hasXmlDecl)
      {
        xml = XML_DECL + xml;
      }
    }
    else
    {
      if (hasXmlDecl)
      {
        int declEndIndex = xml.indexOf(endDecl);
        xml = xml.substring(declEndIndex + endDeclLength);
      }
    }
    xmlBean.setXml(xml);
  }

  /**
   * @todo add code to populate from SectionHelper
   */
  private void section()
  {
    xmlBean.setId(id);
    AuthoringXml ax = getAuthoringXml();
    if (id != null && id.length() > 0)
    {
      xmlBean.setDescription("section fragment id=" + id);
      xmlBean.setName(documentType); // get from document later
      InputStream is = ax.getTemplateInputStream(AuthoringXml.SECTION);
      setUpXmlNoDecl(ax.getTemplateAsString(is));
    }
    else
    {
      xmlBean.setDescription("section template");
      xmlBean.setName(documentType); // get from document later
      InputStream is = ax.getTemplateInputStream(AuthoringXml.SECTION);
      setUpXmlNoDecl(ax.getTemplateAsString(is));
    }
  }

  /**
   * read in XML from item or item template
   */
  private void item()
  {
    xmlBean.setId(id);
    if (id != null && id.length() > 0)
    {
      QTIService qtiService = new QTIService();
      Document doc = qtiService.getExportedItem(id, qtiVersion);
      xmlBean.setDescription(
        "Exported QTI XML produced by Sakai's Tests and Quizzes tool (Samigo)");
      xmlBean.setName("item " + id); // get from document later
      setUpXmlNoDecl(XmlUtil.getDOMString(doc));
    }
    else // for testing
    {
      //AuthoringHelper ah = new AuthoringHelper(qtiVersion);
      AuthoringXml ax = getAuthoringXml();
      xmlBean.setDescription("item template");
      xmlBean.setName(documentType); // get from document later
      InputStream is = ax.getTemplateInputStream(documentType);
      setUpXmlNoDecl(ax.getTemplateAsString(is));
    }
  }

  /**
   * read in XML from item list (comma separated id string)
   */
  private void itemBank()
  {
    xmlBean.setId(id); // this will be an item list
    if (id != null && id.length() > 0)
    {
      QTIService qtiService = new QTIService();
      StringTokenizer st = new StringTokenizer(id, ",");
      int tokens = st.countTokens();
      String[] ids = new String[tokens];
      for (int i = 0; st.hasMoreTokens(); i++)
      {
        ids[i] = st.nextToken();
      }
      Document doc = qtiService.getExportedItemBank(ids, qtiVersion);
      xmlBean.setDescription(
        "Exported QTI XML produced by Sakai's Tests and Quizzes tool (Samigo)");
      xmlBean.setName("object bank for items " + id); // get from document later
      setUpXmlNoDecl(XmlUtil.getDOMString(doc));
    }
    else
    {
      log.debug("object bank empty");
    }
  }

  private void frag()
  {
    xmlBean.setDescription("survey item fragment template");
    xmlBean.setName(documentType); // get from document later
    InputStream is = getAuthoringXml().getTemplateInputStream(documentType);
    setUpXmlNoDecl(getAuthoringXml().getTemplateAsString(is));
  }

//  /**
//   * derived property, uses String value of qtiVersion
//   * @return String value of qtiVersion
//   */
//  public String getVersion()
//  {
//    return "" + getQtiVersion();
//  }

//  /**
//   * derived property, uses String value of qtiVersion
//   * if invalid will not set it
//   * @param version String value of qtiVersion
//   */
//  public void setVersion(String version)
//  {
//
//    try {
//      int v = Integer.parseInt(version);
//      setQtiVersion(v);
//    }
//    catch (NumberFormatException ex) {
//      // leave value alone
//    }
//  }

  /**
   * Always returns a valid QTI version.
   * @return
   */
  public int getQtiVersion()
  {
    if (!QTIVersion.isValid(qtiVersion))
    {
      qtiVersion = QTIVersion.VERSION_1_2; // default
    }
    log.debug("xml controller getQtiVersion()=" + qtiVersion);
    return qtiVersion;
  }

  /**
   * Only accepts valid QTI version.
   * @param qtiVersion
   */
  public void setQtiVersion(int qtiVersion)
  {
    if (!QTIVersion.isValid(qtiVersion))
    {
      throw new IllegalArgumentException("NOT Legal Qti Version.");
    }

    this.qtiVersion = qtiVersion;
    log.debug("xml controller setQtiVersion()=" + qtiVersion);
  }

  public AuthoringXml getAuthoringXml()
  {
    return new AuthoringXml(getQtiVersion());
  }

}
