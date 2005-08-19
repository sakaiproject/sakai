/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package org.sakaiproject.tool.assessment.ui.bean.qti;

import java.io.InputStream;
import java.io.Serializable;
import java.util.StringTokenizer;
import javax.faces.context.FacesContext;

import org.w3c.dom.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.business.entity.constants.QTIVersion;
import org.sakaiproject.tool.assessment.business.entity.helper.AuthoringHelper;
import org.sakaiproject.tool.assessment.business.entity.helper.AuthoringXml;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.util.XmlUtil;

/**
 * <p>Bean for QTI XML or XML fragments and descriptive information. </p>
 * <p>Used to maintain information or to dump XML to client.</p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * @author Ed Smiley esmiley@stanford.edu
   * @version $Id$
 */

public class XMLController implements Serializable
{
  private static Log log = LogFactory.getLog(XMLController.class);

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
    documentType = getAuthoringXml().ASSESSMENT;
    return display();
  }

  public String displaySectionXmlTemplate()
  {
    documentType = getAuthoringXml().SECTION;
    return display();
  }

  public String displayItemXml()
  {
    documentType = getAuthoringXml().ITEM_MCSC; // this is just a default, we will override
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
      xmlBean.setXml("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "\n" +
                     "<error-report>" + "\n" +
                     ex.toString() + "\n" +
                     "</error-report>" + "\n");
      xmlBean.setDescription(ex.toString());
      xmlBean.setName("error");
      xmlBean.setId("");
      ex.printStackTrace();
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
        "Exported QTI XML Copyright: Copyright (c) 2005 Sakai");
      xmlBean.setName("assessment " + id);
      xmlBean.setXml(XmlUtil.getDOMString(doc));
    }
    else // return  xml template, for testing
    {
      xmlBean.setDescription("assessment template");
      AuthoringHelper authHelper = new AuthoringHelper(qtiVersion);
      AuthoringXml ax = getAuthoringXml();

      String xml =
        ax.getTemplateAsString(
        ax.getTemplateInputStream(ax.ASSESSMENT,
                                  FacesContext.getCurrentInstance()));
      xmlBean.setXml(xml);
    }
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
      InputStream is = ax.getTemplateInputStream(ax.SECTION,
                                                 FacesContext.getCurrentInstance());
      xmlBean.setXml(ax.getTemplateAsString(is));
    }
    else
    {
      xmlBean.setDescription("section template");
      xmlBean.setName(documentType); // get from document later
      InputStream is = ax.getTemplateInputStream(ax.SECTION,
                                                 FacesContext.getCurrentInstance());
      xmlBean.setXml(ax.getTemplateAsString(is));
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
        "Exported QTI XML Copyright: Copyright (c) 2005 Sakai");
      xmlBean.setName("item " + id); // get from document later
      xmlBean.setXml(XmlUtil.getDOMString(doc));
    }
    else // for testing
    {
      AuthoringHelper ah = new AuthoringHelper(qtiVersion);
      AuthoringXml ax = getAuthoringXml();
      xmlBean.setDescription("item template");
      xmlBean.setName(documentType); // get from document later
      InputStream is = ax.getTemplateInputStream(documentType,
                                                 FacesContext.getCurrentInstance());
      xmlBean.setXml(ax.getTemplateAsString(is));
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
        "Exported QTI XML Copyright: Copyright (c) 2005 Sakai");
      xmlBean.setName("object bank for items " + id); // get from document later
      xmlBean.setXml(XmlUtil.getDOMString(doc));
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
    InputStream is = getAuthoringXml().getTemplateInputStream(documentType,
      FacesContext.getCurrentInstance());
    xmlBean.setXml(getAuthoringXml().getTemplateAsString(is));
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