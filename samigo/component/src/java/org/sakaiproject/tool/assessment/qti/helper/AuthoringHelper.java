/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.qti.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.qti.asi.Assessment;
import org.sakaiproject.tool.assessment.qti.asi.Item;
import org.sakaiproject.tool.assessment.qti.asi.Section;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.assessment.AssessmentHelperIfc;
import org.sakaiproject.tool.assessment.qti.helper.item.ItemHelperIfc;
import org.sakaiproject.tool.assessment.qti.helper.section.SectionHelperIfc;
import org.sakaiproject.tool.assessment.qti.util.XmlStringBuffer;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import java.util.Set;

/**
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @author Shastri, Rashmi <rshastri@iupui.edu>
 * @version $Id$
 */
public class AuthoringHelper
{
  private static Log log = LogFactory.getLog(AuthoringHelper.class);
//  private static final AuthoringXml ax = new AuthoringXml(QTIVersion.VERSION_1_2);
  private AuthoringXml ax;

  private int qtiVersion;

  private AuthoringHelper()
  {

  }

  /**
   * QTI versioned constructor
   * @param qtiVersion either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
   */
  public AuthoringHelper(int qtiVersion)
  {
    this.qtiVersion = qtiVersion;
    if (!QTIVersion.isValid(qtiVersion))
    {
      throw new IllegalArgumentException(
        "Version Codes supported: QTIVersion.VERSION_1_2, QTIVersion.VERSION_2_0");
    }
    ax = new AuthoringXml(qtiVersion);
  }

  /**
   * Get a published assessment in Document form from Faces context.
   *
   * @param assessmentId the published assessment's Id
   * @return the Document with the published assessment data
   */
  public Document getAssessment(String assessmentId)
  {
    InputStream is =
      ax.getTemplateInputStream(ax.ASSESSMENT, FacesContext.getCurrentInstance());

    return getAssessment(assessmentId, is);
  }

  /**
   * Get an assessment in Document form.
   *
   * @param assessmentId the assessment's Id
   * @param is a stream containing the unpopulated XML document
   * @return the Document with the published assessment data
   */
  public Document getAssessment(String assessmentId, InputStream is)
  {
    try
    {
      String authors;
      String objectives;
      String keywords;
      String rubrics;
      String bgColor;
      String bgImage;

      AssessmentService assessmentService = new AssessmentService();
      QTIHelperFactory factory = new QTIHelperFactory();

      AssessmentFacade assessment =
        assessmentService.getAssessment(assessmentId);
      // convert assessment to document
      AssessmentHelperIfc assessmentHelper =
        factory.getAssessmentHelperInstance(this.qtiVersion);
      Assessment assessmentXml = assessmentHelper.readXMLDocument(is);
      assessmentXml.setIdent(assessmentId);
      assessmentXml.setTitle(assessment.getTitle());
      assessmentHelper.setDescriptiveText(assessment.getDescription(),
                                          assessmentXml);

      authors =
        assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.AUTHORS);
      objectives = assessment.getAssessmentMetaDataByLabel(
        AssessmentMetaDataIfc.OBJECTIVES);
      keywords = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
        KEYWORDS);
      rubrics = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
        RUBRICS);
      bgColor = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
        BGCOLOR);
      bgImage = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
        BGIMAGE);

      if (authors != null)
      {
        assessmentXml.setFieldentry("AUTHORS", authors);
      }
      else
      {
        String createdBy = assessment.getCreatedBy();
        if (createdBy != null)
        {
          assessmentXml.setFieldentry("AUTHORS", createdBy);
        }
        else
        {
          log.debug("\n\nNO AUTHORS");
        }
      }

      if (objectives != null)
      {
        assessmentXml.setFieldentry("ASSESSMENT_OBJECTIVES", objectives);
      }
      if (keywords != null)
      {
        assessmentXml.setFieldentry("ASSESSMENT_KEYWORDS", keywords);
      }
      if (rubrics != null)
      {
        assessmentXml.setFieldentry("ASSESSMENT_RUBRICS", rubrics);
      }
      if (bgColor != null)
      {
        assessmentXml.setFieldentry("BGCOLOR", bgColor);
      }
      if (bgImage != null)
      {
        assessmentXml.setFieldentry("BGIMG", bgImage);
      }

      // fieldentry properties
      EvaluationModelIfc evaluationModel = assessment.getEvaluationModel();
      if (evaluationModel != null)
      {
        assessmentHelper.updateEvaluationModel(assessmentXml,
                                               evaluationModel);
      }
      AssessmentFeedbackIfc assessmentFeedback = assessment.
        getAssessmentFeedback();
      if (assessmentFeedback != null)
      {
        assessmentHelper.updateFeedbackModel(assessmentXml, assessmentFeedback);
      }
      AssessmentAccessControlIfc assessmentAccessControl = assessment.
        getAssessmentAccessControl();
      if (assessmentAccessControl != null)
      {
        assessmentHelper.updateAccessControl(assessmentXml,
                                             assessmentAccessControl);
      }
      Set securedIPAddressSet = (Set) assessment.getSecuredIPAddressSet();
      if (securedIPAddressSet != null)
      {
        assessmentHelper.updateIPAddressSet(assessmentXml,
                                             securedIPAddressSet);
      }


      assessmentHelper.updateMetaData(assessmentXml, assessment);

      // sections
      factory = new QTIHelperFactory();
      SectionHelperIfc sectionHelper =
        factory.getSectionHelperInstance(this.qtiVersion);
      List sectionList = assessment.getSectionArraySorted();
      for (int i = 0; i < sectionList.size(); i++)
      {
        SectionDataIfc section = (SectionDataIfc) sectionList.get(i);
        InputStream sis =
          ax.getTemplateInputStream(ax.SECTION, FacesContext.getCurrentInstance());
        Section sectionXml = sectionHelper.readXMLDocument(sis);
        sectionXml.update(section);
        addSection(assessmentXml, sectionXml);
      }

      return assessmentXml.getDocument();
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * Get an object bank of items in Document form.
   *
   * @param itemIds array of the the item ids
   * @return the Document with the item bank data
   */
  public Document getItemBank(String[] itemIds)
  {
    Document objectBank = XmlUtil.createDocument();

    Element root = objectBank.createElement("questestinterop");
    Element ob = objectBank.createElement("objectbank");
    String objectBankIdent = "object" + Math.random();
    ob.setAttribute("ident", objectBankIdent);
    for (int i = 0; i < itemIds.length; i++)
    {
      Document itemDoc = getItem(itemIds[i]);
      Element itemElement = itemDoc.getDocumentElement();
      Node itemImport = objectBank.importNode(itemElement, true);
      ob.appendChild(itemImport);
    }
    root.appendChild(ob);
    objectBank.appendChild(root);

    return objectBank;
  }

  /**
   * Get an object bank of asessments (asi) in Document form.
   *
   * @param assessmentIds array of the the assessment ids
   * @return the Document with the item bank data
   */
  public Document getAssessmentBank(String[] assessmentIds)
  {
    Document objectBank = XmlUtil.createDocument();

    Element root = objectBank.createElement("questestinterop");
    Element ob = objectBank.createElement("objectbank");
    String objectBankIdent = "object" + Math.random();
    ob.setAttribute("ident", objectBankIdent);
    for (int i = 0; i < assessmentIds.length; i++)
    {
      Document itemDoc = getAssessment(assessmentIds[i]);
      Element itemElement = itemDoc.getDocumentElement();
      Node itemImport = objectBank.importNode(itemElement, true);
      ob.appendChild(itemImport);
    }
    root.appendChild(ob);
    objectBank.appendChild(root);

    return objectBank;
  }

  /**
   * Get an item in Document form.
   * @param itemId the item id
   * @return the Document with the item data
   */
  public Document getItem(String itemId)
  {
    Item itemXml = new Item(this.qtiVersion);
    try
    {
      ItemService itemService = new ItemService();
      QTIHelperFactory factory = new QTIHelperFactory();
      ItemHelperIfc itemHelper =
        factory.getItemHelperInstance(this.qtiVersion);
      ItemDataIfc item = itemService.getItem(itemId);
      TypeIfc type = item.getType();

      if ( (type.MULTIPLE_CHOICE_SURVEY).equals(type))

      {
        String scale = item.getItemMetaDataByLabel(ItemMetaData.SCALENAME);
        itemXml = itemHelper.readTypeSurveyItem(scale);
      }
      else
      {
        itemXml = itemHelper.readTypeXMLItem(type);
      }
      itemXml.setIdent(item.getItemIdString());
      itemXml.update(item);
      return itemXml.getDocument();

    }
    catch (Exception ex)
    {
      log.error(ex);
      return null;
    }
  }

  /**
   *
   * @param assessmentXml
   * @param sectionXml
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  private void addSection(Assessment assessmentXml,
                          Section sectionXml) throws IOException, SAXException,
    ParserConfigurationException
  {
    ax.addElement(assessmentXml.getDocument(), "questestinterop/assessment",
                  sectionXml.getDocument().getDocumentElement());
  }


  /**
   * Get an InputStream to an unpopulated section XML from file system.
   * @return InputStream to an unpopulated section XML
   */
  public InputStream getBlankSectionTemplateFileStream()
  {
    InputStream is = ax.getTemplateInputStream(ax.SECTION);
    return is;
  }

  /**
   * Pull apart object bank into multiple assessments
   * @param objectBank
   * @return an array of AssesmentFacades
   */
  public AssessmentFacade[] createMultipleImportedAssessments(Document
    objectBank)
  {
    Document[] docs = getDocumentsFromObjectBankDoc(objectBank, "assessment");
    return createMultipleImportedAssessments(docs);
  }

  /**
   * Pull apart object bank based on elementName
   * @param objectBank
   * @param elementName e.g. "assessment", "item", etc.
   * @return
   */
  private Document[] getDocumentsFromObjectBankDoc(Document objectBank,
    String elementName)
  {
    Document[] documents = null;
    try
    {
      // Get the matching elements
      NodeList nodeList =
        XPathAPI.selectNodeList(objectBank, "//" + elementName);

      int numDocs = nodeList.getLength();
      if (numDocs == 0)
      {
        return null;
      }

      documents = new Document[numDocs];

      for (int i = 0; i < numDocs; i++)
      {
        Node node = (Node) nodeList.item(i);
        Document objectDoc = XmlUtil.createDocument();
        Node importNode = objectDoc.importNode(node, true);
        objectDoc.appendChild(importNode);
        documents[i] = objectDoc;
      }
    }
    catch (TransformerException e)
    {
      throw (new RuntimeException("TransformerException: Cannot extract " +
                                  elementName + " from object bank"));
    }

    return documents;
  }

  /**
   * Import multiple assessment documents in QTI format, extract & persist.
   * @param objectBank
   * @return an array of AssesmentFacades
   */
  public AssessmentFacade[] createMultipleImportedAssessments(Document[]
    documents)
  {
    AssessmentFacade[] assessments = new AssessmentFacade[documents.length];
    for (int i = 0; i < documents.length; i++)
    {
      assessments[i] = createImportedAssessment(documents[i]);
    }
    return assessments;
  }

  /**
     * Import an assessment XML document in QTI format, extract & persist the data.
   * @param document the assessment XML document in QTI format
   * @return a persisted assessment
   */
  public AssessmentFacade createImportedAssessment(Document document)
  {
    AssessmentFacade assessment = null;

    try
    {
      // we need to know who we are
      String me = AgentFacade.getAgentString();

      // create the assessment
      ExtractionHelper exHelper = new ExtractionHelper(this.qtiVersion);
      AssessmentService assessmentService = new AssessmentService();
      ItemService itemService = new ItemService();
      Assessment assessmentXml = new Assessment(document);
      Map assessmentMap = exHelper.mapAssessment(assessmentXml);
      String description = (String) assessmentMap.get("description");
      String title = (String) assessmentMap.get("title");
      assessment = assessmentService.createAssessmentWithoutDefaultSection(
        title, description, null, null);

      // now make sure we have a unique name for the assessment
      String baseId = assessment.getAssessmentBaseId().toString();
      boolean notUnique =
        !assessmentService.assessmentTitleIsUnique(baseId , title, false);

      if (notUnique)
      {
        synchronized (title)
        {
          log.debug("Assessment "+ title + " is not unique.");
          int count = 0; // alternate exit condition

          while (notUnique)
          {
            title = exHelper.renameDuplicate(title);
            log.debug("renameDuplicate(title): " + title);
            assessment.setTitle(title);
            notUnique =
               !assessmentService.assessmentTitleIsUnique(baseId , title, false);
            if (count++ > 99) break;// exit condition in case bug is introduced
          }
        }
      }

      // update the remaining assessment properties
      exHelper.updateAssessment(assessment, assessmentMap);

      // make sure required fields are set
      assessment.setCreatedBy(me);
      assessment.setCreatedDate(assessment.getCreatedDate());
      assessment.setLastModifiedBy(me);
      assessment.setLastModifiedDate(assessment.getCreatedDate());
      assessment.setTypeId(TypeIfc.QUIZ);
      assessment.setStatus(new Integer(1));

      // process each section and each item withiassessmentn each section
      List sectionList = exHelper.getSectionXmlList(assessmentXml);
      int sectionListSize = sectionList.size();
      log.debug("sections=" + sectionListSize);

      for (int sec = 0; sec < sectionListSize; sec++) // for each section...
      {
        Section sectionXml = (Section) sectionList.get(sec);
        Map sectionMap = exHelper.mapSection(sectionXml);
        // create the assessment section
        SectionFacade section =
          assessmentService.addSection("" + assessment.getAssessmentId());
        exHelper.updateSection(section, sectionMap);
        // make sure we are the creator
        section.setCreatedBy(me);
        section.setCreatedDate(assessment.getCreatedDate());
        section.setLastModifiedBy(me);
        section.setLastModifiedDate(assessment.getCreatedDate());
        section.setTypeId(TypeIfc.DEFAULT_SECTION);
        section.setStatus(new Integer(1));
        section.setSequence(new Integer(sec + 1));

        List itemList = exHelper.getItemXmlList(sectionXml);
        for (int itm = 0; itm < itemList.size(); itm++) // for each item
        {
          log.debug("items=" + itemList.size());
          Item itemXml = (Item) itemList.get(itm);
          Map itemMap = exHelper.mapItem(itemXml);

          ItemFacade item = new ItemFacade();
          exHelper.updateItem(item, itemMap);
          // make sure required fields are set
          item.setCreatedBy(me);
          item.setCreatedDate(assessment.getCreatedDate());
          item.setLastModifiedBy(me);
          item.setLastModifiedDate(assessment.getCreatedDate());
          item.setStatus(ItemDataIfc.ACTIVE_STATUS);
          // assign the next sequence number
          item.setSequence(new Integer(itm + 1));
          // add item to section
          item.setSection(section); // one to many
          section.addItem(item); // many to one
          itemService.saveItem(item);
        } // ... end for each item
        assessmentService.saveOrUpdateSection(section);

      } // ... end for each section

      // and add ip address restriction, if any
      String allowIp = assessment.getAssessmentMetaDataByLabel("ALLOW_IP");
      log.info("allowIp: " + allowIp);

      if (allowIp !=null)
      {
        log.info("NOT NULL: " + allowIp);
        exHelper.makeSecuredIPAddressSet(assessment, allowIp);
      }

      assessmentService.update(assessment);
      assessmentService.saveAssessment(assessment);
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return assessment;
  }

  /**
   * @deprecated
   * Import an item XML document in QTI format, extract & persist the data.
   * @param document the item XML document in QTI format
   * @return a persisted item
   */
  public ItemFacade createImportedItem(Document document)
  {
    ItemFacade item = new ItemFacade();

    try
    {
      // create the item
      ExtractionHelper exHelper = new ExtractionHelper(this.qtiVersion);
      Item itemXml = new Item(document, QTIVersion.VERSION_1_2);
      Map itemMap = exHelper.mapItem(itemXml);
      exHelper.updateItem(item, itemMap);
      ItemService itemService = new ItemService();
      itemService.saveItem(item);
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }

    return item;
  }

  /**
   * Create an XmlStringBuffer (base class for A,S,I XML classes)
   *
   * @param inputStream the input stram
   *
   * @return an XmlStringBuffer (this is the base class for A,S,I XML classes)
   */
  public XmlStringBuffer readXMLDocument(InputStream inputStream)
  {
    Document document = null;
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
//    builderFactory.setNamespaceAware(true);
    try
    {
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
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

    return new XmlStringBuffer(document);
  }

  /**
   * Helper method.
   * @param inputStr
   * @param delimiter
   * @return
   */
  public ArrayList changeDelimitedStringtoArray(String inputStr,
                                                String delimiter)
  {
    ArrayList selectedList = new ArrayList();
    if (inputStr != null && inputStr.trim().length() > 0)
    {
      StringTokenizer st = new StringTokenizer(inputStr, delimiter);
      if (st != null)
      {

        while (st.hasMoreTokens())
        {
          selectedList.add(st.nextToken());
        }
      }
      else
      {
        selectedList.add(inputStr);
      }
    }
    return selectedList;
  }

  public int getQtiVersion()
  {
    return qtiVersion;
  }

  public void setQtiVersion(int qtiVersion)
  {
    this.qtiVersion = qtiVersion;
  }

}
