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

package org.sakaiproject.tool.assessment.business.entity.helper;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.sakaiproject.tool.assessment.business.entity.XmlStringBuffer;
import org.sakaiproject.tool.assessment.business.entity.asi.Assessment;
import org.sakaiproject.tool.assessment.business.entity.asi.Item;
import org.sakaiproject.tool.assessment.business.entity.asi.Section;
import org.sakaiproject.tool.assessment.business.entity.constants.QTIVersion;
import org.sakaiproject.tool.assessment.business.entity.helper.assessment.AssessmentHelperIfc;
import org.sakaiproject.tool.assessment.business.entity.helper.item.ItemHelperIfc;
import org.sakaiproject.tool.assessment.business.entity.helper.section.SectionHelperIfc;
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
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.util.XmlUtil;

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
  private AuthoringXml ax ;

  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss a";
  private  static final String DISPLAY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss a";
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
    this.qtiVersion =qtiVersion;
    if (!QTIVersion.isValid(qtiVersion))
    {
      throw new IllegalArgumentException(
        "Version Codes supported: QTIVersion.VERSION_1_2, QTIVersion.VERSION_2_0");
    }
    ax = new AuthoringXml(qtiVersion);
    System.out.println("debug: setting qtiversion: " + qtiVersion);
  }

  /**
   * Get a published assessment in Document form from Faces context.
   *
   * @param assessmentId the published assessment's Id
   * @return the Document with the published assessment data
   */
  public Document getAssessment(String assessmentId)
  {
    InputStream is = getBlankAssessmentTemplateContextStream();
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
      log.debug("getAssessment() Getting assessment document for" +
        assessmentId + "from AssessmentService.");

      AssessmentFacade assessment =
        assessmentService.getAssessment(assessmentId);
      // convert assessment to document
      AssessmentHelperIfc assessmentHelper =
        factory.getAssessmentHelperInstance(this.qtiVersion);
      Assessment assessmentXml = assessmentHelper.readXMLDocument(is);
      assessmentXml.setIdent(assessmentId);
      assessmentXml.setTitle(assessment.getTitle());
      assessmentHelper.setDescriptiveText(assessment.getDescription(), assessmentXml);

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

//       System.out.println("************* global settings");
//       System.out.println("authors="+authors);;
//       System.out.println("objectives="+objectives);;
//       System.out.println("keywords="+keywords);;
//       System.out.println("rubrics="+rubrics);;
//       System.out.println("bgColor="+bgColor);;
//       System.out.println("bgImage="+bgImage);;

      if (authors!= null)
      {
        assessmentXml.setFieldentry("AUTHORS", authors);
        log.debug("\n\ngetAssessmentMetaDataByLabel AUTHORS = " + authors);
      }
      else
      {
        String createdBy = assessment.getCreatedBy();
        if (createdBy!= null)
        {
          assessmentXml.setFieldentry("AUTHORS", createdBy);
          log.debug("\n\ngetCreatedBy AUTHORS = " + createdBy);
        }
        else
        {
          log.debug("\n\nNO AUTHORS");
        }
      }

      if (objectives!= null )
      {
        assessmentXml.setFieldentry("ASSESSMENT_OBJECTIVES", objectives);
      }
      if (keywords!= null )
      {
        assessmentXml.setFieldentry("ASSESSMENT_KEYWORDS", keywords);
      }
      if (rubrics!= null )
      {
        assessmentXml.setFieldentry("ASSESSMENT_RUBRICS", rubrics);
      }
      if (bgColor!= null )
      {
        assessmentXml.setFieldentry("BGCOLOR", bgColor);
      }
      if (bgImage!= null )
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
      AssessmentFeedbackIfc assessmentFeedback = assessment.getAssessmentFeedback();
      if (assessmentFeedback != null)
      {
        assessmentHelper.updateFeedbackModel(assessmentXml, assessmentFeedback);
      }
      AssessmentAccessControlIfc assessmentAccessControl = assessment.getAssessmentAccessControl();
      if (assessmentAccessControl != null)
      {
        assessmentHelper.updateAccessControl(assessmentXml, assessmentAccessControl);
      }
      log.debug("\nupdateMetaData(assessmentXml, assessment)");
      assessmentHelper.updateMetaData(assessmentXml, assessment);

      // sections
//      SectionHelper sectionHelper = new SectionHelper();
      factory = new QTIHelperFactory();
      SectionHelperIfc sectionHelper =
          factory.getSectionHelperInstance(this.qtiVersion);
//      List sectionList = assessment.getSectionArray();
      List sectionList = assessment.getSectionArraySorted();
      for (int i=0 ; i< sectionList.size(); i++)
      {
        SectionDataIfc section = (SectionDataIfc) sectionList.get(i);
        Section sectionXml =
          sectionHelper.readXMLDocument(getBlankSectionTemplateContextStream());
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
   * Get an item in Document form.
   * @param itemId the item id
   * @return the Document with the item data
   */
  public Document getItem(String itemId)
  {
    log.debug("AuthoringHelper.getItem()");
    Item itemXml = new Item(this.qtiVersion);
    try
    {
      ItemService itemService = new ItemService();
      QTIHelperFactory factory = new QTIHelperFactory();
      ItemHelperIfc itemHelper =
        factory.getItemHelperInstance(this.qtiVersion);
      ItemDataIfc item = itemService.getItem(itemId);
      TypeIfc type = item.getType();
      log.debug("Getting item type:");
      log.debug("type.AUDIO_RECORDING.equals(type): " + type.AUDIO_RECORDING.equals(type));
      log.debug("type.ESSAY_QUESTION.equals(type): " + type.ESSAY_QUESTION.equals(type));
      log.debug("type.FILE_UPLOAD.equals(type): " + type.FILE_UPLOAD.equals(type));
      log.debug("type.FILL_IN_BLANK.equals(type): " + type.FILL_IN_BLANK.equals(type));
      log.debug("type.MATCHING.equals(type): " + type.MATCHING.equals(type));
      log.debug("type.MULTIPLE_CHOICE.equals(type): " + type.MULTIPLE_CHOICE.equals(type));
      log.debug("type.MULTIPLE_CHOICE_SURVEY.equals(type): " + type.MULTIPLE_CHOICE_SURVEY.equals(type));
      log.debug("type.MULTIPLE_CORRECT.equals(type): " + type.MULTIPLE_CORRECT.equals(type));
      log.debug("type.TRUE_FALSE.equals(type): " + type.TRUE_FALSE.equals(type));

      if ((type.MULTIPLE_CHOICE_SURVEY).equals(type))

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
   * Get an InputStream to an unpopulated assessment XML from context.
   * @param context the FacesContext
   * @return InputStream to an unpopulated assessment XML
   */
  public InputStream getBlankAssessmentTemplateContextStream()
  {
    InputStream is = ax.getTemplateInputStream(ax.ASSESSMENT,
                     FacesContext.getCurrentInstance());
    return is;
  }

  /**
   * Get an InputStream to an unpopulated assessment XML from file system.
   * @return InputStream to an unpopulated assessment XML
   */
  public InputStream getBlankAssessmentTemplateFileStream()
  {
    InputStream is = ax.getTemplateInputStream(ax.ASSESSMENT);
    return is;
  }

  /**
   * Get an InputStream to an unpopulated section XML from context.
   * @param context the FacesContext
   * @return InputStream to an unpopulated section XML
   */
  public InputStream getBlankSectionTemplateContextStream()
  {
    InputStream is = ax.getTemplateInputStream(ax.SECTION,
                     FacesContext.getCurrentInstance());
    return is;
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
   * DOCUMENTATION PENDING
   *
   * @param fileName DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public XmlStringBuffer readFile(String fileName)
  {
    if(log.isDebugEnabled())
    {
      log.debug("readFile(String" + fileName + ")");
    }

    XmlStringBuffer xmlString = null;
    InputStreamReader reader;
    String xmlExample;
    try
    {
      InputStream is = new FileInputStream(fileName);
      reader = new InputStreamReader(is);
      StringWriter out = new StringWriter();
      int c;

      while((c = reader.read()) != -1)
      {
        out.write(c);
      }

      reader.close();
      xmlExample = (String) out.toString();
      xmlString = new XmlStringBuffer(xmlExample);
    }
    catch(FileNotFoundException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(IOException e1)
    {
      log.error(e1.getMessage(), e1);
    }

    return xmlString;
  }


  /**
   * Import an assessment XML document in QTI format, extract & persist the data.
   * @param document the assessment XML document in QTI format
   * @return a persisted assessment
   */
  public AssessmentFacade createImportedAssessment(Document document)
  {
    log.debug(
        document==null?
        "DOCUMENT IS NULL IN createPublishedAssessment(Document)":
        "createPublishedAssessment(Document)");
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
//      assessment = exHelper.createAssessment(assessmentMap);
      String description = (String) assessmentMap.get("description");
      String title = (String) assessmentMap.get("title");
      assessment = assessmentService.createAssessmentWithoutDefaultSection(
        title, description, null, null);

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


      for (int sec = 0; sec < sectionListSize; sec++)// for each section...
      {
        Section sectionXml =(Section) sectionList.get(sec);
        Map sectionMap = exHelper.mapSection(sectionXml);
        log.debug("SECTION MAP=" + sectionMap);
        // create the assessment section
        SectionFacade section =
            assessmentService.addSection("" + assessment.getAssessmentId());
        exHelper.updateSection(section, sectionMap);
        // make sure we are the creator
        log.debug("section " + section.getTitle() +
          "created by '" + me+ "'.");
        section.setCreatedBy(me);
        section.setCreatedDate(assessment.getCreatedDate());
        section.setLastModifiedBy(me);
        section.setLastModifiedDate(assessment.getCreatedDate());
        section.setTypeId(TypeIfc.DEFAULT_SECTION);
        section.setStatus(new Integer(1));
        // set the sequence
        section.setSequence(new Integer(sec + 1));
//        // add the section to the assessment
//        section.setAssessmentId(assessment.getAssessmentId());//many to one
//        section.setAssessment(assessment);
//        assessment.getSectionArray().add(section);// one to many

        List itemList = exHelper.getItemXmlList(sectionXml);
        for (int itm = 0; itm < itemList.size(); itm++)// for each item
        {
          log.debug("items=" + itemList.size());
          Item itemXml = (Item) itemList.get(itm);
          Map itemMap = exHelper.mapItem(itemXml);
          log.debug("ITEM MAP=" + itemMap);

          ItemFacade item = new ItemFacade();
          exHelper.updateItem(item, itemMap);
          // make sure required fields are set
          item.setCreatedBy(me);
          item.setCreatedDate(assessment.getCreatedDate());
          item.setLastModifiedBy(me);
          item.setLastModifiedDate(assessment.getCreatedDate());
          log.debug("ITEM TYPE IS: " +item.getTypeId());
          item.setStatus(ItemDataIfc.ACTIVE_STATUS);
          // assign the next sequence number
          item.setSequence(new Integer(itm + 1));
          // add item to section
          item.setSection(section);// one to many
          section.addItem(item);// many to one
          itemService.saveItem(item);
          // debugging
          log.debug("ITEM:  ans key" + item.getAnswerKey() );
          log.debug("ITEM:  correct feed" + item.getCorrectItemFeedback() );
          log.debug("ITEM:  incorrect feed " + item.getInCorrectItemFeedback() );
          log.debug("ITEM:  by " + item.getCreatedBy() );
          log.debug("ITEM:  date" + item.getCreatedDate() );
          log.debug("ITEM:  desc " + item.getDescription() );
          log.debug("ITEM:  duration" + item.getDuration() );
          log.debug("ITEM:  general feed " + item.getGeneralItemFeedback() );
          log.debug("ITEM:  incorrect " + item.getInCorrectItemFeedback() );
          log.debug("ITEM:  is true " + item.getIsTrue() );
          log.debug("ITEM DEBUG item text" + item.getText() );
          log.debug("ITEM:  item text" + item.getText() );
        }// ... end for each item
        assessmentService.saveOrUpdateSection(section);
        log.debug("SECTION title set to: " + section.getTitle());
        System.out.println("SECTION description set to: " + section.getDescription());

      }// ... end for each section

      log.debug("assessment created by '" + assessment.getCreatedBy() + "'.");
      assessmentService.update(assessment);
      // debugging
      log.debug("ASSESSMENT:  meta " + assessment.getAssessmentMetaDataMap());
      log.debug("ASSESSMENT:  feed " + assessment.getAssessmentFeedback());
      log.debug("ASSESSMENT:  comments  " + assessment.getComments());
      log.debug("ASSESSMENT:  by " + assessment.getCreatedBy());
      log.debug("ASSESSMENT:  by date " + assessment.getCreatedDate());
      log.debug("ASSESSMENT:  desc" + assessment.getDescription());
      log.debug("ASSESSMENT:  disp " + assessment.getDisplayName());
      log.debug("ASSESSMENT:  last by " + assessment.getLastModifiedBy());
      log.debug("ASSESSMENT:  last date" + assessment.getLastModifiedDate());
      log.debug("ASSESSMENT:  mult " + assessment.getMultipartAllowed());
      log.debug("ASSESSMENT:  title " + assessment.getTitle());
      log.debug("ASSESSMENT DEBUG title " + assessment.getTitle());
      assessmentService.saveAssessment(assessment);
    }
    catch(Exception e)
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
    log.debug(
        document==null?
        "DOCUMENT IS NULL IN createImportedItem(Document)":
        "createImportedItem(Document)");
    ItemFacade item = new ItemFacade();

    try
    {
      // create the item
      ExtractionHelper exHelper = new ExtractionHelper(this.qtiVersion);
      log.debug("XSLT Path: " + exHelper.getTransformPath());
      Item itemXml = new Item(document, QTIVersion.VERSION_1_2);
      Map itemMap = exHelper.mapItem(itemXml);
      log.debug("ITEM MAP=" + itemMap);
      log.debug("updating item");
      exHelper.updateItem(item, itemMap);
      ItemService itemService = new ItemService();
      log.debug("Saving item");
      itemService.saveItem(item);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
//      e.printStackTrace();
    }

    return item;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @param inputStream DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public XmlStringBuffer readXMLDocument(InputStream inputStream)
  {
    if(log.isDebugEnabled())
    {
      log.debug("readDocument(InputStream " + inputStream);
    }

    Document document = null;
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
//    builderFactory.setNamespaceAware(true);
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

    return new XmlStringBuffer(document);
  }


  public ArrayList changeDelimitedStringtoArray(String inputStr, String delimiter)
    {
      ArrayList selectedList = new ArrayList();
      if(inputStr != null && inputStr.trim().length() >0 )
      {
        StringTokenizer st = new StringTokenizer(inputStr, delimiter );
        if(st != null)
        {

        while(st.hasMoreTokens())
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

    /**
     * current date default format
     * @return date string
     */
    public String getCurrentDateAndTime()
    {
         return getCurrentDateAndTime(DATE_FORMAT);
    }
    /**
     * current date default format
     * @return date string
     */
    public String getCurrentDisplayDateAndTime()
    {
         return getCurrentDateAndTime(DISPLAY_DATE_FORMAT);
    }

    /**
     * current date format
     * @param dateFormat
     * @return date string
     */
    public String getCurrentDateAndTime(String dateFormat)
    {
      Calendar cal = Calendar.getInstance(TimeZone.getDefault());
      java.text.SimpleDateFormat sdf =
        new java.text.SimpleDateFormat(dateFormat);
      sdf.setTimeZone(TimeZone.getDefault());
      log.debug("Now : " + sdf.format(cal.getTime()));
      return sdf.format(cal.getTime());
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


