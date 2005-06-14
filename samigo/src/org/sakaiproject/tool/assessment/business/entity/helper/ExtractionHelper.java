/**********************************************************************************
*
* $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/business/entity/helper/ExtractionHelper.java,v 1.96 2005/06/06 22:30:04 esmiley.stanford.edu Exp $
*
***********************************************************************************
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
 */

package org.sakaiproject.tool.assessment.business.entity.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.faces.context.FacesContext;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.sakaiproject.tool.assessment.business.entity.asi.ASIBaseClass;
import org.sakaiproject.tool.assessment.business.entity.asi.Assessment;
import org.sakaiproject.tool.assessment.business.entity.asi.Item;
import org.sakaiproject.tool.assessment.business.entity.asi.Section;
import org.sakaiproject.tool.assessment.business.entity.constants.AuthoringConstantStrings;
import org.sakaiproject.tool.assessment.business.entity.constants.QTIVersion;
import org.sakaiproject.tool.assessment.business.exception.Iso8601FormatException;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.SecuredIPAddress;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.util.Iso8601DateFormat;
import org.sakaiproject.tool.assessment.util.Iso8601TimeInterval;
import org.sakaiproject.tool.assessment.util.XMLMapper;
import org.sakaiproject.tool.assessment.util.XmlUtil;

/**
 * <p>Has helper methods for data extraction (import) from QTI</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005 Sakai</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id: ExtractionHelper.java,v 1.96 2005/06/06 22:30:04 esmiley.stanford.edu Exp $
 */

public class ExtractionHelper
{
  private static final String QTI_VERSION_1_2_PATH = "v1p2";
  private static final String QTI_VERSION_2_0_PATH = "v2p0";
  private static final String TRANSFORM_PATH =
      "/xml/xsl/dataTransform/import";

  private static final String ASSESSMENT_TRANSFORM =
      "extractAssessment.xsl";
  private static final String SECTION_TRANSFORM = "extractSection.xsl";
  private static final String ITEM_TRANSFORM = "extractItem.xsl";
  private static Log log = LogFactory.getLog(ExtractionHelper.class);

  private int qtiVersion = QTIVersion.VERSION_1_2;
  private String overridePath = null; // override defaults and settings
  private String FIB_BLANK_INDICATOR = " {} ";

  /**
   * @deprecated
   */
  public ExtractionHelper()
  {
    this.setQtiVersion(QTIVersion.VERSION_1_2);

  }

  /**
   * Get ExtractionHelper for QTIVersion.VERSION_1_2
   * or QTIVersion.VERSION_2_0
   * @param qtiVersion
   */
  public ExtractionHelper(int qtiVersion)
  {
    this.setQtiVersion(qtiVersion);
  }

  /**
   * Path to XSL transform code.
   * @return context-relative path to XSL transform code.
   */
  public String getTransformPath()
  {
    // first check to see if normal computed path has been overridden
    if (overridePath != null)
    {
      return overridePath;
    }

    return TRANSFORM_PATH + "/" + getQtiPath();
  }

  private String getQtiPath()
  {
    return qtiVersion == QTIVersion.VERSION_1_2 ? QTI_VERSION_1_2_PATH :
        QTI_VERSION_2_0_PATH;
  }

  /**
   * Get QTI version flag.
   * Either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
   * @return QTI version flag
   */
  public int getQtiVersion()
  {
    return qtiVersion;
  }

  /**
   * Set QTI version flag.
   * Either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
   * @param qtiVersion
   */
  public void setQtiVersion(int qtiVersion)
  {
    if (!QTIVersion.isValid(qtiVersion))
    {
      throw new IllegalArgumentException("NOT Legal Qti Version.");
    }
    this.qtiVersion = qtiVersion;
  }

  /**
   * Get an XML document for the transform
   * @param template
   * @return
   */
  public Document getTransformDocument(String template)
  {
    Document document = null;

    if (!isOKtransform(template))
    {
      throw new IllegalArgumentException("NOT valid template.");
    }
    String templateContextPath = this.getTransformPath() + "/" + template;
    FacesContext context = FacesContext.getCurrentInstance();
    if (context == null)
    {
      return XmlUtil.readDocument(templateContextPath);
    }
    document = XmlUtil.readDocument(context, templateContextPath);

    return document;
  }

  /**
   * Get map of data to set from assessment XML
   * @param assessmentXml
   * @return a Map
   */
  public Map mapAssessment(Assessment assessmentXml)
  {
    log.debug("inside: mapAssessment");
    return map(ASSESSMENT_TRANSFORM, assessmentXml);
  }

  /**
   * Get map of data to set from section XML
   * @param sectionXml
   * @return a Map
   */
  public Map mapSection(Section sectionXml)
  {
    return map(SECTION_TRANSFORM, sectionXml);
  }

  /**
   * Get map of data to set from item XML
   * @param itemXml
   * @return a Map
   */
  public Map mapItem(Item itemXml)
  {
    return map(ITEM_TRANSFORM, itemXml);
  }

  /**
   * Helper method
       * @param transformType ASSESSMENT_TRANSFORM, SECTION_TRANSFORM, ITEM_TRANSFORM
   * @param asi ASIBaseClass: Assessment, Section, or Item XML
   * @return
   */
  private Map map(String transformType, ASIBaseClass asi)
  {
    if (!isOKasi(asi))
    {
      throw new IllegalArgumentException("Incorrect ASI subclass.");
    }
    if (!isOKtransform(transformType))
    {
      throw new IllegalArgumentException("Incorrect transform: " +
                                         transformType + ".");
    }
    Map map = null;
    try
    {
      Document transform = getTransformDocument(transformType);
      Document xml = asi.getDocument();
      Document model = XmlUtil.transformDocument(xml, transform);
      map = XMLMapper.map(model);
    }
    catch (IOException ex)
    {
      log.error(ex);
      ex.printStackTrace(System.out);
    }
    catch (SAXException ex)
    {
      log.error(ex);
      ex.printStackTrace(System.out);
    }
    catch (ParserConfigurationException ex)
    {
      log.error(ex);
      ex.printStackTrace(System.out);
    }
    return map;

  }

  /**
   * Look up a List of Section XML from Assessment Xml
   * @return a List of Section XML objects
   */
  public List getSectionXmlList(Assessment assessmentXml)
  {
    List nodeList = assessmentXml.selectNodes("//section");
    List sectionXmlList = new ArrayList();

    // now convert our list of Nodes to a list of section xml
    for (int i = 0; i < nodeList.size(); i++)
    {
      try
      {
        Node node = (Node) nodeList.get(i);
        // create a document for a section xml object
        Document sectionDoc = XmlUtil.createDocument();
        // Make a copy for inserting into the new document
        Node importNode = sectionDoc.importNode(node, true);
        // Insert the copy into sectionDoc
        sectionDoc.appendChild(importNode);
        Section sectionXml = new Section(sectionDoc,
               this.getQtiVersion());
        // add the new section xml object to the list
        sectionXmlList.add(sectionXml);
      }
      catch (DOMException ex)
      {
        log.error(ex);
        ex.printStackTrace(System.out);
      }
    }
    return sectionXmlList;
  }

  /**
   * Look up a List of Item XML from Section Xml
   * @param Section sectionXml
   * @return a List of Item XML objects
   */
  public List getItemXmlList(Section sectionXml)
  {
    String itemElementName =
        qtiVersion == QTIVersion.VERSION_1_2 ? "//item" : "//assessmentItem";

    // now convert our list of Nodes to a list of section xml
    List nodeList = sectionXml.selectNodes(itemElementName);
    List itemXmlList = new ArrayList();
    for (int i = 0; i < nodeList.size(); i++)
    {
      try
      {
        Node node = (Node) nodeList.get(i);
        // create a document for a item xml object
        Document itemDoc = XmlUtil.createDocument();
        // Make a copy for inserting into the new document
        Node importNode = itemDoc.importNode(node, true);
        // Insert the copy into itemDoc
        itemDoc.appendChild(importNode);
        Item itemXml = new Item(itemDoc,
               this.getQtiVersion());
        // add the new section xml object to the list
        itemXmlList.add(itemXml);
      }
      catch (DOMException ex)
      {
        log.error(ex);
        ex.printStackTrace(System.out);
      }
    }
    return itemXmlList;
  }

  /**
   * Used internally.
   * @param transform
   * @return true if OK
   */
  private boolean isOKtransform(String transform)
  {
    return (transform == this.ASSESSMENT_TRANSFORM ||
            transform == this.SECTION_TRANSFORM ||
            transform == this.ITEM_TRANSFORM) ? true : false;
  }

  /**
   * Used internally.
   * @param asi
   * @return true if OK
   */
  private boolean isOKasi(ASIBaseClass asi)
  {
    return (asi instanceof Assessment ||
            asi instanceof Section ||
            asi instanceof Item) ? true : false;
  }

  /**
   * Create assessment from the extracted properties.
   * @param assessmentMap the extracted properties
   * @return an assessment, which has been persisted
   */
  public AssessmentFacade createAssessment(Map assessmentMap)
  {
    String description = (String) assessmentMap.get("description");
    String title = (String) assessmentMap.get("title");
    AssessmentService assessmentService = new AssessmentService();
    AssessmentFacade assessment = assessmentService.createAssessment(
        title, description, null, null);
    return assessment;
  }

  /**
   * Update assessment from the extracted properties.
   * Note: you need to do a save when you are done.
   * @param assessment the assessment, which will  be persisted
   * @param assessmentMap the extracted properties
   */
  public void updateAssessment(AssessmentFacade assessment,
                               Map assessmentMap)
  {
    log.debug("Updating AssessmentFacade assessment<==Map assessmentMap");
    String title;
    String displayName;
    String description;
    String comments;

    String instructorNotification;
    String testeeNotification;
    String multipartAllowed;
    String createdBy;
    String createdDate;

    title = (String) assessmentMap.get("title");
    displayName = (String) assessmentMap.get("title");
    comments = (String) assessmentMap.get("comments");

    log.debug("ASSESSMENT updating metadata information");
    // set meta data
    List metadataList = (List) assessmentMap.get("metadata");
    makeMetaDataDefaults(assessment);// first turn all default switches
    addMetadata(metadataList, assessment);// get QTI metadata, if any
    createdBy = assessment.getAssessmentMetaDataByLabel("CREATOR");

    log.debug("ASSESSMENT updating basic information");
    // set basic properties
    assessment.setCreatedBy(createdBy);
    assessment.setComments(comments);
    assessment.setCreatedDate(new Date());
    assessment.setLastModifiedBy("Sakai Import");
    assessment.setLastModifiedDate(new Date());

    // additional information

    // restricted IP address
//    log.debug("ASSESSMENT updating access control, evaluation model, feedback");
//    String allowIp = assessment.getAssessmentMetaDataByLabel(
//        "CONSIDER_ALLOW_IP");
//    if ("TRUE".equalsIgnoreCase(allowIp))
//    {
//      makeSecuredIPAddressSet(assessment);
//    }

    // access control
    String duration = (String) assessmentMap.get("duration");
    makeAccessControl(assessment, duration);

    // evaluation model control
    makeEvaluationModel(assessment);

    // assessment feedback control
    makeAssessmentFeedback(assessment);
  }

  /**
   * Put feedback settings into assessment (bi-directional)
   * @param assessment
   */
  private void makeAssessmentFeedback(AssessmentFacade assessment)
  {
    AssessmentFeedback feedback =
        (AssessmentFeedback) assessment.getAssessmentFeedback();
    if (feedback == null){
      feedback = new AssessmentFeedback();
      // Need to fix AssessmentFeedback so it can take AssessmentFacade later
      feedback.setAssessmentBase(assessment.getData());
    }

    if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_SHOW_QUESTION")))
    {
      feedback.setShowQuestionText(Boolean.TRUE);
    }
    else
    {
      feedback.setShowQuestionText(Boolean.FALSE);
    }

    if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_SHOW_RESPONSE")))
    {
      feedback.setShowStudentResponse(Boolean.TRUE);
    }
    else
    {
      feedback.setShowStudentResponse(Boolean.FALSE);
    }

    if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_SHOW_CORRECT_RESPONSE")))
    {
      feedback.setShowCorrectResponse(Boolean.TRUE);
    }
    else
    {
      feedback.setShowCorrectResponse(Boolean.FALSE);
    }

    if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_SHOW_STUDENT_SCORE")))
    {
      feedback.setShowStudentScore(Boolean.TRUE);
    }
    else
    {
      feedback.setShowStudentScore(Boolean.FALSE);
    }

    if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_SHOW_ITEM_LEVEL")))
    {
      feedback.setShowQuestionLevelFeedback(Boolean.TRUE);
    }
    else
    {
      feedback.setShowQuestionLevelFeedback(Boolean.FALSE);
    }

    if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_SHOW_SELECTION_LEVEL")))
    {
      feedback.setShowSelectionLevelFeedback(Boolean.TRUE);
    }
    else
    {
      feedback.setShowSelectionLevelFeedback(Boolean.FALSE);
    }

    if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_SHOW_GRADER_COMMENT")))
    {
      feedback.setShowGraderComments(Boolean.TRUE);
    }
    else
    {
      feedback.setShowGraderComments(Boolean.FALSE);
    }

    if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_SHOW_STATS")))
    {
      feedback.setShowStatistics(Boolean.TRUE);
    }
    else
    {
      feedback.setShowStatistics(Boolean.FALSE);
    }

    if (
        this.notNullOrEmpty(assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_DELIVERY_DATE") )  ||
        "DATED".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_DELIVERY")))
    {
      feedback.setFeedbackDelivery(feedback.FEEDBACK_BY_DATE);
    }
    else   if ("IMMEDIATE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_DELIVERY")))
    {
      feedback.setFeedbackDelivery(feedback.IMMEDIATE_FEEDBACK);
    }
    else
    {
      feedback.setFeedbackDelivery(feedback.NO_FEEDBACK);
    }

    assessment.setAssessmentFeedback(feedback);
  }

  /**
   * Put evaluation settings into assessment (bi-directional)
   * @param assessment
   */
  private void makeEvaluationModel(AssessmentFacade assessment)
  {
    EvaluationModel evaluationModel =
        (EvaluationModel) assessment.getEvaluationModel();
    if (evaluationModel == null){
      evaluationModel = new EvaluationModel();
      // Need to fix EvaluationModel so it can take AssessmentFacade later
      evaluationModel.setAssessmentBase(assessment.getData());
    }

    // anonymous
    if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "ANONYMOUS_GRADING")))
    {
      evaluationModel.setAnonymousGrading(EvaluationModel.ANONYMOUS_GRADING);
    }
    else
    {
      evaluationModel.setAnonymousGrading(EvaluationModel.NON_ANONYMOUS_GRADING);
    }

    // gradebook options, don't know how this is supposed to work, leave alone for now
    if ("DEFAULT".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "GRADEBOOK_OPTIONS")))
    {
      evaluationModel.setToGradeBook(EvaluationModel.TO_DEFAULT_GRADEBOOK.toString());
    }
    else if ("SELECTED".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "GRADEBOOK_OPTIONS")))
    {
      evaluationModel.setToGradeBook(EvaluationModel.TO_SELECTED_GRADEBOOK.toString());
    }

    // highest or average
    if ("HIGHEST".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "GRADE_SCORE")))
    {
      evaluationModel.setScoringType(EvaluationModel.HIGHEST_SCORE);
    }
    else if ("AVERAGE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "GRADE_SCORE")))
    {
      evaluationModel.setScoringType(EvaluationModel.AVERAGE_SCORE);
    }

    assessment.setEvaluationModel(evaluationModel);
  }

  /**
   * Put access control settings into assessment (bi-directional)
   * @param assessment
   * @param duration Time interval for timed assessment (Iso8601 format)
   */
  private void makeAccessControl(AssessmentFacade assessment, String duration)
  {
    AssessmentAccessControl control =
        (AssessmentAccessControl)assessment.getAssessmentAccessControl();
    if (control == null){
      control = new AssessmentAccessControl();
      // need to fix accessControl so it can take AssessmentFacade later
      control.setAssessmentBase(assessment.getData());
    }

    // Control dates
    Iso8601DateFormat iso = new Iso8601DateFormat();
    String startDate = assessment.getAssessmentMetaDataByLabel("START_DATE");
    String dueDate = assessment.getAssessmentMetaDataByLabel("END_DATE");
    String retractDate = assessment.getAssessmentMetaDataByLabel("RETRACT_DATE");
    String feedbackDate = assessment.getAssessmentMetaDataByLabel(
        "FEEDBACK_DELIVERY_DATE");

    try
    {
      control.setStartDate(iso.parse(startDate).getTime());
      assessment.getData().addAssessmentMetaData("hasAvailableDate", "true");

    }
    catch (Iso8601FormatException ex)
    {
      log.debug("Cannot set startDate.");
    }
    try
    {
      control.setDueDate(iso.parse(dueDate).getTime());
//      assessment.getData().addAssessmentMetaData("hasDueDate", "true");
      assessment.getData().addAssessmentMetaData("dueDate", "true");
    }
    catch (Iso8601FormatException ex)
    {
      log.debug("Cannot set dueDate.");
    }
    try
    {
      control.setRetractDate(iso.parse(retractDate).getTime());
      assessment.getData().addAssessmentMetaData("hasRetractDate", "true");
    }
    catch (Iso8601FormatException ex)
    {
      log.debug("Cannot set retractDate.");
    }
    try
    {
      control.setFeedbackDate(iso.parse(feedbackDate).getTime());
      assessment.getData().addAssessmentMetaData("FEEDBACK_DELIVERY","DATED");
    }
    catch (Iso8601FormatException ex)
    {
      log.debug("Cannot set feedbackDate.");
    }

    // Released to (commented out)
    // don't know what site you will have in a new environment
    // but registered as a BUG in SAM-271 so turning it on.

      String releasedTo = assessment.getAssessmentMetaDataByLabel(
          "ASSESSMENT_RELEASED_TO");
      control.setReleaseTo(releasedTo);

    // Timed Assessment
    if (duration != null)
    {
      try
      {
        Iso8601TimeInterval tiso = new Iso8601TimeInterval(duration);
        control.setTimeLimit(tiso.getSeconds());
        control.setTimedAssessment(AssessmentAccessControl.TIMED_ASSESSMENT);
        assessment.getData().addAssessmentMetaData("hasTimeAssessment", "true");
      }
      catch (Iso8601FormatException ex)
      {
        log.debug("Can't format assessment duration. " + ex);
        control.setTimeLimit(new Integer(0));
        control.setTimedAssessment(AssessmentAccessControl.
                                   DO_NOT_TIMED_ASSESSMENT);
      }
    }
    else
    {
      control.setTimeLimit(new Integer(0));
      control.setTimedAssessment(AssessmentAccessControl.
                                 DO_NOT_TIMED_ASSESSMENT);
    }

    System.out.println(
        "Set: control.getTimeLimit()="+control.getTimeLimit());
    System.out.println(
        "Set: control.getTimedAssessment()="+ control.getTimedAssessment());

    if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "AUTO_SUBMIT")))
    {
      control.setAutoSubmit(AssessmentAccessControl.AUTO_SUBMIT);
      assessment.getData().addAssessmentMetaData("hasAutoSubmit", "true");
    }
    else
    {
      control.setAutoSubmit(AssessmentAccessControl.DO_NOT_AUTO_SUBMIT);
    }

    // Assessment Organization
    // navigation
    if ("LINEAR".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "NAVIGATION")))
    {
      control.setItemNavigation(control.LINEAR_ACCESS);
    }
    else
    {
      control.setItemNavigation(control.RANDOM_ACCESS);
    }

    // numbering
    if ("CONTINUOUS".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "QUESTION_NUMBERING")))
    {
      control.setItemNumbering(control.CONTINUOUS_NUMBERING);
    }
    else if ("RESTART".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "QUESTION_NUMBERING")))
    {
      control.setItemNumbering(control.RESTART_NUMBERING_BY_PART);
    }

    //question layout
    if ("I".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "QUESTION_LAYOUT")))
    {
      control.setAssessmentFormat(control.BY_QUESTION);
    }
    else if ("S".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "QUESTION_LAYOUT")))
    {
      control.setAssessmentFormat(control.BY_PART);
    }
    else
    {
      control.setAssessmentFormat(control.BY_ASSESSMENT);
    }

    //Submissions
    // submissions allowed
    String maxAttempts =
        assessment.getAssessmentMetaDataByLabel("MAX_ATTEMPTS");
    String unlimited = control.UNLIMITED_SUBMISSIONS.toString();
    if (
        unlimited.equalsIgnoreCase(maxAttempts))
    {
      control.setUnlimitedSubmissions(Boolean.TRUE);
      control.setSubmissionsAllowed(AssessmentAccessControlIfc.
                                    UNLIMITED_SUBMISSIONS);
    }
    else
    {
      control.setUnlimitedSubmissions(Boolean.FALSE);
      try
      {
        control.setSubmissionsAllowed(new Integer(maxAttempts));
      }
      catch (NumberFormatException ex1)
      {
        control.setSubmissionsAllowed(new Integer("1"));
      }
    }
    System.out.println("Set: control.getSubmissionsAllowed()="+control.getSubmissionsAllowed());
    System.out.println("Set: control.getUnlimitedSubmissions()="+control.getUnlimitedSubmissions());

    // late submissions
    // I am puzzled as to why there is no ACCEPT_LATE_SUBMISSION, assuming it =T
    if ("FALSE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "LATE_HANDLING")))
    {
      control.setLateHandling(control.NOT_ACCEPT_LATE_SUBMISSION);
    }
    else
    {
      control.setLateHandling(new Integer(1));

    }

    // auto save
    if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "AUTO_SAVE")))
    {
      control.setAutoSubmit(control.AUTO_SAVE);
    }
    else if ("FALSE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
        "AUTO_SAVE")))
    {
      control.setAutoSubmit(control.DO_NOT_AUTO_SUBMIT);
    }

    // Submission Message
    String submissionMessage = assessment.getAssessmentMetaDataByLabel(
        "SUBMISSION_MESSAGE");
    if (submissionMessage != null)
    {
      control.setSubmissionMessage(submissionMessage);
    }

    // Username, password, finalPageUrl
//    String considerUserId = assessment.getAssessmentMetaDataByLabel(
//        "CONSIDER_USERID"); //
    String userId = assessment.getAssessmentMetaDataByLabel("USERID");
    String password = assessment.getAssessmentMetaDataByLabel("PASSWORD");
    String finalPageUrl = assessment.getAssessmentMetaDataByLabel("FINISH_URL");

    if (//"TRUE".equalsIgnoreCase(considerUserId) &&
        notNullOrEmpty(userId) && notNullOrEmpty(password))
    {
      control.setUsername(userId);
      control.setPassword(password);
      assessment.getData().addAssessmentMetaData("hasUsernamePassword", "true");
    }
    control.setFinalPageUrl(finalPageUrl);

    assessment.setAssessmentAccessControl(control);
  }

  /**
   * the ip address is in a newline delimited string
   * @param assessment
   */
  private void makeSecuredIPAddressSet(AssessmentFacade assessment)
  {
    Set securedIPAddressSet = (Set) assessment.getSecuredIPAddressSet();
    if (securedIPAddressSet == null)
    {
      securedIPAddressSet = new HashSet();
    }

    String ipList = (String) assessment.getAssessmentMetaDataByLabel("ALLOW_IP");
    if (ipList == null)
      ipList = "";
    String[] ip = ipList.split("\\n");
    for (int j = 0; j < ip.length; j++)
    {
      if (ip[j] != null)
        securedIPAddressSet.add(new SecuredIPAddress(assessment.getData(), null,
            ip[j]));
    }

    assessment.setSecuredIPAddressSet(securedIPAddressSet);
    if (securedIPAddressSet.size()>0)
    {
      assessment.getData().addAssessmentMetaData("hasIpAddress", "true");
//      assessment.getData().addAssessmentMetaData("hasSpecificIP", "true");
    }
  }
  /**
   * Update section from the extracted properties.
   * Note: you need to do a save when you are done.
   * @param section the section, which will  be persisted
   * @param sectionMap the extracted properties
   */
  public void updateSection(SectionFacade section, Map sectionMap)
  {
    log.debug("Updating SectionFacade section<==Map sectionMap");
    log.debug("Section Title: " + sectionMap.get("title"));
    section.setTitle( (String) sectionMap.get("title"));
    section.setDescription( (String) sectionMap.get("description"));
    section.setLastModifiedBy("Sakai Import");
    section.setLastModifiedDate(new Date());
  }

  /**
   * Update item from the extracted properties.
   * Note: you need to do a save when you are done.
   * @param item the item, which will  be persisted
   * @param itemMap the extracted properties
   */
  public void updateItem(ItemFacade item, Map itemMap)
  {
    log.debug("\n*** Updating ItemFacade item<==Map itemMap");
    // type and title
    String title = (String) itemMap.get("title");
    item.setDescription(title);
    // type
    // first, we will try to get item type from meta info
    // but otherwise, we will use the other two
    String qmd = item.getItemMetaDataByLabel("qmd_itemtype");
    String itemIntrospect = (String) itemMap.get("itemIntrospect");
    String itemType = calculateType(item, title, itemIntrospect, qmd);
    Long typeId = getType(itemType); // we use this later...
    log.debug("SETTING TYPE: ItemFacade item<==Map itemMap");
    log.debug("============>item type id: '" + typeId + "'");
    item.setTypeId(typeId);

    // meta data
    List metadataList = (List) itemMap.get("metadata");
    log.debug("\n*** SETTING METADATA: ItemFacade item<==Map itemMap");
    addMetadata(metadataList, item);

    // basic properties
    log.debug("SETTING PROPERTIES: ItemFacade item<==Map itemMap");
    addItemProperties(item, itemMap);

    // feedback
    log.debug("\n*** SETTING FEEDBACK: ItemFacade item<==Map itemMap");
    // correct, incorrect, general
    addFeedback(item, itemMap, typeId);

    // item text and answers
    log.debug("\n*** SETTING TEXT AND ANSWERS: ItemFacade item<==Map itemMap");
    log.debug("\n*** FILL_IN_BLANK=" + TypeIfc.FILL_IN_BLANK.longValue());
    log.debug("\n*** MATCHING=" + TypeIfc.MATCHING.longValue());
    log.debug("\n*** TYPE=" + typeId.longValue());
    if (TypeIfc.FILL_IN_BLANK.longValue() == typeId.longValue())
    {
      addFibTextAndAnswers(item, itemMap);
    }
    else if (TypeIfc.MATCHING.longValue() == typeId.longValue())
    {
      addMatchTextAndAnswers(item, itemMap);
    }
    else
    {
      addTextAndAnswers(item, itemMap);
    }

  }

  /**
   *
   * @param item
   * @param title
   * @param itemIntrospect
   * @param qmdType
   * @return
   */
  private String calculateType(ItemFacade item, String title,
                               String itemIntrospect,
                               String qmdType)
  {
    String itemType = qmdType;

    if (itemType == null)
    {
      if (title != null) // it is also very common to put an item type in the title
      {
        itemType = getTypeFromTitle(title);
      }
      else // OK try to figure out from item structure
      {
        itemType = itemIntrospect;
      }
    }
    return itemType;
  }

  /**
   *
   * @param item
   * @param itemMap
   */
  private void addItemProperties(ItemFacade item, Map itemMap)
  {
    String duration = (String) itemMap.get("duration");
    String triesAllowed = (String) itemMap.get("triesAllowed");
    String score = (String) itemMap.get("score");
    String hasRationale = (String) itemMap.get("hasRationale");
    String status = (String) itemMap.get("status");
    String createdBy = (String) itemMap.get("createdBy");

    // not being set yet
    String instruction = (String) itemMap.get("instruction");
    String hint = (String) itemMap.get("hint");

    // created by is not nullable
    if (createdBy == null)
    {
      createdBy = "Imported by Sakai";
    }

    String createdDate = (String) itemMap.get("createdDate");

    if (notNullOrEmpty(duration))
    {
      item.setDuration(new Integer(duration));
    }
    if (notNullOrEmpty(triesAllowed))
    {
      item.setTriesAllowed(new Integer(triesAllowed));
    }
    item.setInstruction( (String) itemMap.get("instruction"));
    if (notNullOrEmpty(score))
    {
      item.setScore(new Float(score));
    }
    item.setHint( (String) itemMap.get("hint"));
    if (notNullOrEmpty(hasRationale))
    {
      item.setHasRationale(new Boolean(hasRationale));
    }
    if (notNullOrEmpty(status))
    {
      item.setStatus(new Integer(status));
    }
    item.setCreatedBy(createdBy);
    try
    {
      Iso8601DateFormat iso = new Iso8601DateFormat();
      Calendar cal = iso.parse(createdDate);
      item.setCreatedDate(cal.getTime());
    }
    catch (Exception ex)
    {
      item.setCreatedDate(new Date());
    }
    item.setLastModifiedBy("Sakai Import");
    item.setLastModifiedDate(new Date());
  }

  /**
   * add feedback
   * @param item
   * @param itemMap
   * @param typeId
   */
  private void addFeedback(ItemFacade item, Map itemMap, Long typeId)
  {
    // write the map out
    Iterator iter = itemMap.keySet().iterator();
    while (iter.hasNext())
    {
      String key = (String) iter.next();
      Object o = itemMap.get(key);
      System.out.println("itemMap: " + key + "=" + itemMap.get(key));
    }
    log.debug("\n*** calculating item feedback from" + itemMap);
    String correctItemFeedback = (String) itemMap.get(
        "correctItemFeedback");
    String incorrectItemFeedback = (String) itemMap.get(
        "incorrectItemFeedback");
    String generalItemFeedback = (String) itemMap.get(
        "generalItemFeedback");
    // now if this is an Audio, File Upload or Short Answer question just
    // get whatever feedback is there and put it in general, note: in
    //  some versions of Navigo the general feedback is exported as "InCorrect"!
    if (TypeIfc.AUDIO_RECORDING.longValue() == typeId.longValue() ||
        TypeIfc.FILE_UPLOAD.longValue() == typeId.longValue() ||
        TypeIfc.ESSAY_QUESTION.longValue() == typeId.longValue())
    {
      generalItemFeedback = "";
      if (notNullOrEmpty(incorrectItemFeedback))
      {
        generalItemFeedback += " " + incorrectItemFeedback;
      }
      if (notNullOrEmpty(correctItemFeedback))
      {
        generalItemFeedback += " " + correctItemFeedback;
      }
    }

    log.debug("\n*** correctItemFeedback=" + correctItemFeedback);
    log.debug("\n*** incorrectItemFeedback=" + incorrectItemFeedback);
    log.debug("\n*** generalItemFeedback=" + generalItemFeedback);

    if (notNullOrEmpty(correctItemFeedback))
    {
      log.debug("\n*** setting correctItemFeedback");
      item.setCorrectItemFeedback(correctItemFeedback);
    }
    if (notNullOrEmpty(incorrectItemFeedback))
    {
      log.debug("\n*** setting incorrectItemFeedback");
      item.setInCorrectItemFeedback(incorrectItemFeedback);
    }
    if (notNullOrEmpty(generalItemFeedback))
    {
      log.debug("\n*** setting generalItemFeedback");
      item.setGeneralItemFeedback(generalItemFeedback);
    }

  }

  /**
   * create the answer feedback set for an answer
   * @param item
   * @param itemMap
   */
  private void addAnswerFeedback(Answer answer, String value)
  {
    HashSet answerFeedbackSet = new HashSet();
    answerFeedbackSet.add(new AnswerFeedback(answer,
                                             AnswerFeedbackIfc.ANSWER_FEEDBACK,
                                             value));
    answer.setAnswerFeedbackSet(answerFeedbackSet);
  }

  /**
   * matching only: create the answer feedback set for an answer
   * @param answer
   * @param correct
   * @param incorrect
   */
  private void addMatchingAnswerFeedback(Answer answer,
                                         String correct, String incorrect)
  {
    HashSet answerFeedbackSet = new HashSet();
    answerFeedbackSet.add(new AnswerFeedback(answer,
                                             AnswerFeedbackIfc.CORRECT_FEEDBACK,
                                             correct));
    answerFeedbackSet.add(new AnswerFeedback(answer,
                                             AnswerFeedbackIfc.
                                             INCORRECT_FEEDBACK, incorrect));
  }

  /**
   * @param item
   * @param itemMap
   */
  private void addTextAndAnswers(ItemFacade item, Map itemMap)
  {
    List itemTextList = (List) itemMap.get("itemText");
    HashSet itemTextSet = new HashSet();
    for (int i = 0; i < itemTextList.size(); i++)
    {
      ItemText itemText = new ItemText();
      String text = (String) itemTextList.get(i);
      // should be allow this or, continue??
      // for now, empty string OK, setting to empty string if null
      if (text == null)
      {
        text = "";
      }
      itemText.setText(text);
      itemText.setItem(item.getData());
      itemText.setSequence(new Long(i + 1));
      List answerList = new ArrayList();
      List aList = (List) itemMap.get("itemAnswer");
      answerList = aList == null ? answerList : aList;
      HashSet answerSet = new HashSet();
      char answerLabel = 'A';
      List answerFeedbackList = (List) itemMap.get("itemFeedback");

      ArrayList correctLabels;
      correctLabels = (ArrayList) itemMap.get("itemAnswerCorrectLabel");
      if (correctLabels == null)
      {
        correctLabels = new ArrayList();
      }
      for (int a = 0; a < answerList.size(); a++)
      {
        Answer answer = new Answer();
        String answerText = (String) answerList.get(a);
        log.debug("EXTRACT ANSWER DEBUG: " + answerText);
        // these are not supposed to be empty
        if (notNullOrEmpty(answerText))
        {
          String label = "" + answerLabel++;
          answer.setLabel(label); // up to 26, is this a problem?
          // if label matches correct answer it is correct
          if (isCorrectLabel(label, correctLabels))
          {
            answer.setIsCorrect(new Boolean(true));
          }
          else
          {
            answer.setIsCorrect(new Boolean(false));
          }
          answer.setText(answerText);
          answer.setItemText(itemText);
          answer.setItem(item.getData());
          int sequence = a + 1;
          answer.setSequence(new Long(sequence));
          // prepare answer feedback - daisyf added this on 2/21/05
          // need to check if this works for question type other than
          // MC
          HashSet set = new HashSet();
          if (answerFeedbackList != null)
          {
            log.debug("\n*** extracting answer: " + a);
            AnswerFeedback answerFeedback = new AnswerFeedback();
            answerFeedback.setAnswer(answer);
            answerFeedback.setTypeId(AnswerFeedbackIfc.GENERAL_FEEDBACK);
            if (answerFeedbackList.get(sequence - 1) != null)
            {
              answerFeedback.setText( (String) answerFeedbackList.get(sequence -
                  1));
              set.add(answerFeedback);
              answer.setAnswerFeedbackSet(set);
            }
          }

          answerSet.add(answer);
        }
      }
      itemText.setAnswerSet(answerSet);
      itemTextSet.add(itemText);
    }
    item.setItemTextSet(itemTextSet);
  }

  /**
   * Check to find out it response label is in the list of correct responses
   * @param testLabel response label
   * @param labels the list of correct responses
   * @return
   */
  private boolean isCorrectLabel(String testLabel, ArrayList labels)
  {
    if (testLabel == null || labels == null
        || labels.indexOf(testLabel) == -1)
    {
      return false;
    }

    return true;
  }

  /**
   * FIB questions ONLY
   * @param item
   * @param itemMap
   */
  private void addFibTextAndAnswers(ItemFacade item, Map itemMap)
  {
    List itemTextList = new ArrayList();
    List iList = (List) itemMap.get("itemFibText");
    itemTextList = iList == null ? itemTextList : iList;

    List itemTList = new ArrayList();
    List tList = (List) itemMap.get("itemText");
    itemTList = iList == null ? itemTList : tList;

    HashSet itemTextSet = new HashSet();
    ItemText itemText = new ItemText();
    String itemTextString = "";
    List answerFeedbackList = (List) itemMap.get("itemFeedback");

    List answerList = new ArrayList();
    List aList = (List) itemMap.get("itemFibAnswer");
    answerList = aList == null ? answerList : aList;

    // handle FIB with instructional text
    // sneak it into first text
    if (!itemTList.isEmpty() && !(itemTextList.size()>1))
    {
      try
      {
        String firstFib = (String) itemTextList.get(0);
        String firstText = (String) itemTList.get(0);
        if (firstFib.equals(firstText))
        {
          log.debug("Setting FIB instructional text.");
          itemTextList.remove(0);
          String newFirstFib
            = firstFib + "<br />" + itemTextList.get(0);
          itemTextList.set(0, newFirstFib);
        }
      }
      catch (Exception ex)
      {
        log.warn("Thought we found an instructional text but couldn't put it in."
                + " " + ex);
      }
    }
    // loop through all our extracted FIB texts interposing FIB_BLANK_INDICATOR
    for (int i = 0; i < itemTextList.size(); i++)
    {
      String text = (String) itemTextList.get(i);
      log.debug("\n*** text=" + text);
      // we are assuming non-empty text/answer/non-empty text/answer etc.
      if (text == null || text=="")
      {
        continue;
      }
      itemTextString += text;
      if (i < answerList.size())
      {
        itemTextString += FIB_BLANK_INDICATOR;
      }
    }
    log.debug("\n*** complete text=" + itemTextString);
    itemText.setText(itemTextString);
    itemText.setItem(item.getData());
    itemText.setSequence(new Long(0));
    HashSet answerSet = new HashSet();
    char answerLabel = 'A';

    log.debug("\n*** calculating FIB " + answerList.size() + " answers");
    for (int a = 0; a < answerList.size(); a++)
    {
      Answer answer = new Answer();
      String answerText = (String) answerList.get(a);
      log.debug("EXTRACT ANSWER: " + answerText);
      // these are not supposed to be empty
      if (notNullOrEmpty(answerText))
      {
        String label = "" + answerLabel++;
        answer.setLabel(label); // up to 26, is this a problem?
        answer.setIsCorrect(new Boolean(true));
        answer.setText(answerText);
        answer.setItemText(itemText);
        answer.setItem(item.getData());
        int sequence = a + 1;
        answer.setSequence(new Long(sequence));
        HashSet set = new HashSet();
        if (answerFeedbackList != null)
        {
          log.debug("\n*** extracting answer feedback: " + a);
          AnswerFeedback answerFeedback = new AnswerFeedback();
          answerFeedback.setAnswer(answer);
          answerFeedback.setTypeId(AnswerFeedbackIfc.GENERAL_FEEDBACK);
          if (answerFeedbackList.get(sequence - 1) != null)
          {
            answerFeedback.setText( (String) answerFeedbackList.get(
                sequence - 1));
            set.add(answerFeedback);
            answer.setAnswerFeedbackSet(set);
          }
        }
        answerSet.add(answer);
      }
    }

    log.debug("SET ANSWER SET");
    itemText.setAnswerSet(answerSet);
    log.debug("ADD ITEM TEXT");
    itemTextSet.add(itemText);
    log.debug("SET ITEM TEXT SET");
    item.setItemTextSet(itemTextSet);
  }

  /**
   * MATCHING questions ONLY
   * @param item
   * @param itemMap
   */
  private void addMatchTextAndAnswers(ItemFacade item, Map itemMap)
  {
    log.debug("addMatchTextAndAnswers(ItemFacade item, Map itemMap)");

    List sourceList = (List) itemMap.get("itemMatchSourceText");
    List targetList = (List) itemMap.get("itemMatchTargetText");
    List indexList = (List) itemMap.get("itemMatchIndex");
    List answerFeedbackList = (List) itemMap.get("itemFeedback");
    List itemTextList = (List) itemMap.get("itemText");

    sourceList = sourceList == null ? new ArrayList() : sourceList;
    targetList = targetList == null ? new ArrayList() : targetList;
    indexList = indexList == null ? new ArrayList() : indexList;
    answerFeedbackList =
      answerFeedbackList == null ? new ArrayList() : answerFeedbackList;
    itemTextList =
      itemTextList == null ? new ArrayList() : itemTextList;

    // hopefully will not happen, but this will allow us to get the data
    // that we can find
    if (targetList.size() <indexList.size())
    {
      log.warn("Something is wrong, padding targets with blanks.");
      log.debug("targetList.size(): " + targetList.size());
      log.debug("indexList.size(): " + indexList.size());

      // OK, I am going to NOT do this, see if it works OK
//      for (int i = targetList.size(); i < indexList.size() + 1; i++)
//      {
//        targetList.add("  ");
//      }
    }
    String itemTextString = "";
    if (itemTextList.size()>0)
    {
      itemTextString = (String) itemTextList.get(0);
    }

    HashSet itemTextSet = new HashSet();

    // first, add the question text
    item.setInstruction(itemTextString);
    log.debug("item.setInstruction itemTextString: " + itemTextString);

    // loop through source texts indicating answers (targets)
    for (int i = 0; i < sourceList.size(); i++)
    {
      // create the entry for the matching item (source)
      String sourceText = (String) sourceList.get(i);
      if (sourceText == null) sourceText="";
      log.debug("sourceText: " + sourceText);
      log.debug("sequence: " + i + 1);
      ItemText sourceItemText = new ItemText();
      log.debug("created sourceItemText ");
      sourceItemText.setText(sourceText);
      sourceItemText.setItem(item.getData());
      sourceItemText.setSequence(new Long(i + 1));

      // find the matching answer (target)
      HashSet targetSet = new HashSet();
      log.debug("created targetSet");
      String targetString;
      int targetIndex = 999;// obviously not matching value
      try
      {
        targetIndex = Integer.parseInt( (String) indexList.get(i));
      }
      catch (NumberFormatException ex)
      {
        log.warn("No match for " + sourceText + "."); // default to no match
      }
      catch (IndexOutOfBoundsException ex)
      {
        log.error("Corrupt index list.  Cannot assign match for: " +sourceText + ".");
      }
      // loop through all possible targets (matching answers)
      char answerLabel = 'A';
      for (int a = 0; a < targetList.size(); a++)
      {
        targetString = (String) targetList.get(a);
        if (targetString == null)
        {
          targetString = "";
        }
        log.debug("EXTRACT ANSWER: targetString: " + targetString);
        Answer target = new Answer();

        String label = "" + answerLabel++;
        target.setLabel(label); // up to 26, is this a problem?
        target.setText(targetString);
        target.setItemText(sourceItemText);
        target.setItem(item.getData());
        target.setSequence(new Long(a + 1));
        log.debug("setSequence: " + a + 1);
        log.debug("label: " + label);
        log.debug("source: " + sourceText);


        // if this answer is the indexed one, flag as correct
        if (a + 1 == targetIndex)
        {
          target.setIsCorrect(Boolean.TRUE);
          log.debug("source: " + sourceText + " matches target: " + targetString);
        }
        if (answerFeedbackList != null)
        {
          log.debug("extracting answer feedback");
          Set targetFeedbackSet = new HashSet();
          AnswerFeedback tAnswerFeedback = new AnswerFeedback();
          tAnswerFeedback.setAnswer(target);
          tAnswerFeedback.setTypeId(AnswerFeedbackIfc.GENERAL_FEEDBACK);
          String targetFeedback = "";
          if (answerFeedbackList.size()>0)
          {
            targetFeedback = (String) answerFeedbackList.get(targetIndex);
          }
          if (targetFeedback.length()>0)
          {
            tAnswerFeedback.setText( targetFeedback);
            targetFeedbackSet.add(tAnswerFeedback);
            target.setAnswerFeedbackSet(targetFeedbackSet);
          }
        }
        targetSet.add(target);
        log.debug("ADD TO ANSWER SET: targetSet.add(target)");
      }

      sourceItemText.setAnswerSet(targetSet);
      log.debug("ADD ANSWER SET: sourceItemText.setAnswerSet(answerSet)");
      itemTextSet.add(sourceItemText);
      log.debug("ADD ITEM TEXT: itemTextSet.add(itemText)");
    }

    log.debug("SET ITEM TEXT SET");
    item.setItemTextSet(itemTextSet);
  }


  /**
   * Try to infer the type of imported question from title.
   * @param title
   * @return AuthoringConstantStrings.{type}
   */
  private String getTypeFromTitle(String title)
  {
    String itemType;
    String lower = title.toLowerCase();
    itemType = AuthoringConstantStrings.MCSC;
    if (lower.indexOf("multiple") != -1 &&
        lower.indexOf("response") != -1)
    {
      itemType = AuthoringConstantStrings.MCMC;
    }
    else if (lower.indexOf("true") != -1 ||
             lower.indexOf("tf") != -1)
    {
      itemType = AuthoringConstantStrings.TF;
    }
    else if (lower.indexOf("multiple") != -1 &&
             lower.indexOf("correct") != -1)
    {
      itemType = AuthoringConstantStrings.MCMC;
    }
    else if (lower.indexOf("audio") != -1 ||
             lower.indexOf("recording") != -1)
    {
      itemType = AuthoringConstantStrings.AUDIO;
    }
    else if (lower.indexOf("file") != -1 ||
             lower.indexOf("upload") != -1)
    {
      itemType = AuthoringConstantStrings.FILE;
    }
    else if (lower.indexOf("match") != -1)
    {
      itemType = AuthoringConstantStrings.MATCHING;
    }
    else if (lower.indexOf("fib") != -1 ||
             lower.indexOf("fill") != -1 ||
             lower.indexOf("f.i.b.") != -1
             )
    {
      itemType = AuthoringConstantStrings.FIB;
    }
    else if (lower.indexOf("essay") != -1 ||
             lower.indexOf("short") != -1)
    {
      itemType = AuthoringConstantStrings.ESSAY;
    }

    return itemType;
  }

  /**
   * We use the QTI qmd_itemtype in itemmetadata
   * Note, this may be deprecated in favor of a vocabulary based approach.
   * Need to investigate.  Anyway, this is the form that is backwardly compatible.
   *
   * @todo take hardcode conversion of file upload out for Samigo 2.0!
   *
   * @param metadataMap
   * @return
   */
  private Long getType(String qmd_itemtype)
  {
    String[] typeArray = AuthoringConstantStrings.itemTypes;
    for (int i = 0; i < typeArray.length; i++)
    {
      log.debug("Testing type: Is '" + qmd_itemtype.trim() + "'='" +
                typeArray[i] + "'?...");
      if (qmd_itemtype.trim().equalsIgnoreCase(typeArray[i]))
      {
        return new Long(i);
      }
    }

    log.warn("Unable to set item type: '" + qmd_itemtype + "'.");
    log.warn("guessing item type: '" + typeArray[2] + "'.");
    return new Long(2);
  }

  /**
   * helper method
   * @param s
   * @return
   */
  private boolean notNullOrEmpty(String s)
  {
    return s != null && s.trim().length() > 0 ?
        true : false;
  }

  /**
   * Primarily for testing purposes.
   * @return an overridden path if not null
   */
  public String getOverridePath()
  {
    return overridePath;
  }

  /**
   * Primarily for testing purposes.
   * @param overridePath an overriding path
   */
  public void setOverridePath(String overridePath)
  {
    this.overridePath = overridePath;
  }

  /**
   * Adds extraction-created list of "|" key value pairs
   * to item metadata map, if there are any.
   * Example:<br/>
   * <p>á
       * &lt; metadata type =" list " &gt; TEXT_FORMAT| HTML &lt;/ metadata &gt;<br/> á
   * &lt; metadata type =" list " &gt; ITEM_OBJECTIVE| &lt/ metadata &gt;<br/>
   * Becomes:<br/>
   * TEXT_FORMAT=>HTML etc.
   * </p>
   * @param metadataList extraction-created list of "|" key value pairs
   * @param item the item
   */
  private void addMetadata(List metadataList, ItemDataIfc item)
  {
    if (metadataList == null)
    {
      return; // no metadata found
    }

    for (int i = 0; i < metadataList.size(); i++)
    {
      String meta = (String) metadataList.get(i);
      StringTokenizer st = new StringTokenizer(meta, "|");
      String key = null;
      String value = null;
      if (st.hasMoreTokens())
      {
        key = st.nextToken().trim();
      }
      if (st.hasMoreTokens())
      {
        value = st.nextToken().trim();
        log.debug("==>ITEM METADATA key ='" + key + "'.");
        log.debug("==>ITEM METADATA value length =" + value.length() + ".");
        item.addItemMetaData(key, value);
      }
    }
  }

  /**
   * Adds extraction-created list of "|" key value pairs
   * to assessment metadata map, if there are any.
   * Example:<br/>
   * <p>á
   *
   * &lt; metadata type =" list " &gt; FEEDBACK_SHOW_CORRECT_RESPONSE|True &lt;/ metadata &gt;<br/> á
   * &lt; metadata type =" list " &gt; FEEDBACK_SHOW_STUDENT_SCORE|True &lt/ metadata &gt;<br/>
   * Becomes:<br/>
   * TEXT_FORMAT=>HTML etc.
   * </p>
   * @param metadataList extraction-created list of "|" key value pairs
   * @param assessment the assessment
   */
  private void addMetadata(List metadataList, AssessmentFacade assessment)
  {
    if (metadataList == null)
    {
      return; // no metadata found
    }

    for (int i = 0; i < metadataList.size(); i++)
    {
      String meta = (String) metadataList.get(i);
      StringTokenizer st = new StringTokenizer(meta, "|");
      String key = null;
      String value = null;
      if (st.hasMoreTokens())
      {
        key = st.nextToken().trim();
      }

      // translate XML metadata strings to assessment metadata strings here
      // key to patch up the difference between Daisy's and earlier labels
      // that are compatible with the earlier beta version of Samigo
      if ("AUTHORS".equals(key)) key = AssessmentMetaDataIfc.AUTHORS;
      if ("ASSESSMENT_KEYWORDS".equals(key)) key = AssessmentMetaDataIfc.KEYWORDS;
      if ("ASSESSMENT_OBJECTIVES".equals(key)) key = AssessmentMetaDataIfc.OBJECTIVES;
      if ("ASSESSMENT_RUBRICS".equals(key)) key = AssessmentMetaDataIfc.RUBRICS;
      if ("BGCOLOR".equals(key)) key = AssessmentMetaDataIfc.BGCOLOR;
      if ("BGIMG".equals(key)) key = AssessmentMetaDataIfc.BGIMAGE;
      if ("COLLECT_ITEM_METADATA".equals(key)) key = "hasMetaDataForQuestions";

      if (st.hasMoreTokens())
      {
        value = st.nextToken().trim();
        log.debug("\n\n==>ASSESSMENT METADATA key ='" + key + "'.");
        log.debug("\n\n==>ASSESSMENT METADATA value length =" + value.length() +
                 ".");

        assessment.addAssessmentMetaData(key, value);
      }
    }
  }

  /**
   * Turns on editability for everything (ecept template info),
   * since we don't know if this  metadata is in the assessment or not,
   * or may not want to follow it, even if it is.
   *
   * The importer of the assesment may also be different than the
   * exporter, and may be on a different system or have different
   * templates, or policies, even if using this softwware.
   *
   * @param assessment
   */
  private void makeMetaDataDefaults(AssessmentFacade assessment)
  {
    // turn this off specially, as template settings are meaningless on import
    assessment.addAssessmentMetaData("templateInfo_isInstructorEditable", "false");

    String[] editKeys = {
        "assessmentAuthor_isInstructorEditable",
        "assessmentCreator_isInstructorEditable",
        "description_isInstructorEditable",
        "dueDate_isInstructorEditable",
        "retractDate_isInstructorEditable",
        "anonymousRelease_isInstructorEditable",
        "authenticatedRelease_isInstructorEditable",
        "ipAccessType_isInstructorEditable",
        "passwordRequired_isInstructorEditable",
        "timedAssessment_isInstructorEditable",
        "timedAssessmentAutoSubmit_isInstructorEditable",
        "itemAccessType_isInstructorEditable",
        "displayChunking_isInstructorEditable",
        "displayNumbering_isInstructorEditable",
        "submissionModel_isInstructorEditable",
        "lateHandling_isInstructorEditable",
        "autoSave_isInstructorEditable",
        "submissionMessage_isInstructorEditable",
        "finalPageURL_isInstructorEditable",
        "feedbackType_isInstructorEditable",
        "feedbackComponents_isInstructorEditable",
        "testeeIdentity_isInstructorEditable",
        "toGradebook_isInstructorEditable",
        "recordedScore_isInstructorEditable",
        "bgColor_isInstructorEditable",
        "bgImage_isInstructorEditable",
        "metadataAssess_isInstructorEditable",
        "metadataParts_isInstructorEditable",
        "metadataQuestions_isInstructorEditable",
    };
    for (int i = 0; i < editKeys.length; i++)
    {
      assessment.addAssessmentMetaData(editKeys[i], "true");
    }


  }

}
/**********************************************************************************
 *
 * $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/business/entity/helper/ExtractionHelper.java,v 1.96 2005/06/06 22:30:04 esmiley.stanford.edu Exp $
 *
 ***********************************************************************************/
