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



package org.sakaiproject.tool.assessment.qti.helper.assessment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AttachmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SecuredIPAddressIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.qti.asi.Assessment;
import org.sakaiproject.tool.assessment.qti.asi.Section;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringHelper;
import org.sakaiproject.tool.assessment.qti.util.Iso8601DateFormat;
import org.sakaiproject.tool.assessment.qti.util.Iso8601TimeInterval;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.xml.sax.SAXException;

/**
 * <p>Copyright: Copyright (c) 2005/p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @author based on some code by: Shastri, Rashmi <rshastri@iupui.edu>
 * @version $Id$
 */
public abstract class AssessmentHelperBase
  implements AssessmentHelperIfc
{
  private static Log log = LogFactory.getLog(AssessmentHelperBase.class);

  abstract protected int getQtiVersion();

  /**
   * Read in assessment XML from input stream
   *
   * @param inputStream XML input stream
   *
   * @return assessment XML
   */
  public Assessment readXMLDocument(
    InputStream inputStream)
  {
    if (log.isDebugEnabled())
    {
      log.debug("readDocument(InputStream " + inputStream);
    }

    Assessment assessXml = null;

    try
    {
      AuthoringHelper authoringHelper = new AuthoringHelper(getQtiVersion());
      assessXml =
        new Assessment(
        authoringHelper.readXMLDocument(inputStream).getDocument());
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

    return assessXml;
  }

  /**
   * Set feedback settings in XML
   * @param assessmentXml
   * @param feedback
   */
  public void updateFeedbackModel(Assessment assessmentXml,
                                  AssessmentFeedbackIfc feedback)
  {
    Integer feedbackDelivery = feedback.getFeedbackDelivery();
    if (feedback.FEEDBACK_BY_DATE.equals(feedbackDelivery))
    {
      assessmentXml.setFieldentry("FEEDBACK_DELIVERY", "DATED");
    }
    else if (feedback.IMMEDIATE_FEEDBACK.equals(feedbackDelivery))
    {
      assessmentXml.setFieldentry("FEEDBACK_DELIVERY", "IMMEDIATE");
    }
    else if (feedback.FEEDBACK_ON_SUBMISSION.equals(feedbackDelivery))
    {
      assessmentXml.setFieldentry("FEEDBACK_DELIVERY", "ON_SUBMISSION");
    }
    else //feedback.NO_FEEDBACK
    {
      assessmentXml.setFieldentry("FEEDBACK_DELIVERY", "NONE");
    }
    
    Integer feedbackComponentOption = feedback.getFeedbackComponentOption();
    if (feedback.SELECT_COMPONENTS.equals(feedbackComponentOption))
    {
    	assessmentXml.setFieldentry("FEEDBACK_COMPONENT_OPTION", "SELECT_COMPONENTS");
    }
    else 
    {
    	assessmentXml.setFieldentry("FEEDBACK_COMPONENT_OPTION", "SHOW_TOTALSCORE_ONLY");
    }

    Integer feedbackAuthoring = feedback.getFeedbackAuthoring();
    if (feedback.QUESTIONLEVEL_FEEDBACK.equals(feedbackAuthoring))
    {
      assessmentXml.setFieldentry("FEEDBACK_AUTHORING", "QUESTION");
    }
    else if (feedback.SECTIONLEVEL_FEEDBACK.equals(feedbackAuthoring))
    {
      assessmentXml.setFieldentry("FEEDBACK_AUTHORING", "SECTION");
    }
    else //feedback.BOTH_FEEDBACK
    {
      assessmentXml.setFieldentry("FEEDBACK_AUTHORING", "BOTH");
    }


    assessmentXml.setFieldentry("FEEDBACK_SHOW_QUESTION",
                                qtiBooleanString(feedback.getShowQuestionText()));
    assessmentXml.setFieldentry("FEEDBACK_SHOW_RESPONSE",
                                qtiBooleanString(feedback.
                                                 getShowStudentResponse()));
    assessmentXml.setFieldentry("FEEDBACK_SHOW_CORRECT_RESPONSE",
                                qtiBooleanString(feedback.
                                                 getShowCorrectResponse()));
    assessmentXml.setFieldentry("FEEDBACK_SHOW_STUDENT_SCORE",
                                qtiBooleanString(feedback.getShowStudentScore()));
    assessmentXml.setFieldentry("FEEDBACK_SHOW_STUDENT_QUESTIONSCORE",
                                qtiBooleanString(feedback.getShowStudentQuestionScore()));
    assessmentXml.setFieldentry("FEEDBACK_SHOW_ITEM_LEVEL",
                                qtiBooleanString(feedback.
                                                 getShowQuestionLevelFeedback()));
    assessmentXml.setFieldentry("FEEDBACK_SHOW_SELECTION_LEVEL",
                                qtiBooleanString(feedback.
                                                 getShowSelectionLevelFeedback()));
    assessmentXml.setFieldentry("FEEDBACK_SHOW_GRADER_COMMENT",
                                qtiBooleanString(feedback.getShowGraderComments()));
    assessmentXml.setFieldentry("FEEDBACK_SHOW_STATS",
                                qtiBooleanString(feedback.getShowStatistics()));
  }

  /**
   * Set evaluation settings in XML.
   * @param assessmentXml
   * @param evaluationModel
   */
  public void updateEvaluationModel(Assessment assessmentXml,
                                    EvaluationModelIfc evaluationModel)
  {
    // debugging
    log.debug("EvaluationModelIfc.ANONYMOUS_GRADING: " +
              EvaluationModelIfc.ANONYMOUS_GRADING);
    log.debug("evaluationModel.getAnonymousGrading(): " +
              evaluationModel.getAnonymousGrading());
    log.debug("EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString(): " +
              EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString());
    log.debug("evaluationModel.getToGradeBook(): " +
              evaluationModel.getToGradeBook());
    log.debug("EvaluationModelIfc.HIGHEST_SCORE: " +
              EvaluationModelIfc.HIGHEST_SCORE);
    log.debug("evaluationModel.getScoringType(): " +
              evaluationModel.getScoringType());

    // anonymous grading
    if (EvaluationModelIfc.ANONYMOUS_GRADING.equals(evaluationModel.
      getAnonymousGrading()))
    {
      assessmentXml.setFieldentry("ANONYMOUS_GRADING", "True");
    }
    else
    {
      assessmentXml.setFieldentry("ANONYMOUS_GRADING", "False");
    }

    // graadebook options
    if (EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString().
        equals(evaluationModel.getToGradeBook()))
    {
      assessmentXml.setFieldentry("GRADEBOOK_OPTIONS", "SELECTED");

    }
    else if (EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString().
             equals(evaluationModel.getToGradeBook()))
    {
      assessmentXml.setFieldentry("GRADEBOOK_OPTIONS", "DEFAULT");
    }
    //SAK-7162
    if (EvaluationModelIfc.NOT_TO_GRADEBOOK.toString().
            equals(evaluationModel.getToGradeBook()))
    {
          assessmentXml.setFieldentry("GRADEBOOK_OPTIONS", "NONE");

 	}


    //highest or last
    if (EvaluationModelIfc.HIGHEST_SCORE.equals(evaluationModel.getScoringType()))
    {
      assessmentXml.setFieldentry("GRADE_SCORE", "HIGHEST_SCORE");
    }
    // not implementing average for now
    else if (EvaluationModelIfc.AVERAGE_SCORE.equals(evaluationModel.
      getScoringType()))
    {
      assessmentXml.setFieldentry("GRADE_SCORE", "AVERAGE_SCORE");
    }
    else if (EvaluationModelIfc.LAST_SCORE.equals(evaluationModel.
      getScoringType()))
    {
      assessmentXml.setFieldentry("GRADE_SCORE", "LAST_SCORE");
    }

  }

  /**
   * Set the assessment description.
   * @param description assessment description
   * @param assessmentXml the xml
   */
  public void setDescriptiveText(String description, Assessment assessmentXml)
  {
    String xpath =
      "questestinterop/assessment/presentation_material/flow_mat/material/mattext";

    List list = assessmentXml.selectNodes(xpath);
    try
    {
      // If nothing is entered into description field, 
      // description will be "" from mysql but it will be null from oracle
      // we need to set it to "" if it is null (SAK-5950)
      if (description == null) {
    	  log.debug("description is null");
    	  description = "";
      }
      description = XmlUtil.convertStrforCDATA(description);
      assessmentXml.update(xpath, description);
    }
    catch (Exception ex)
    {
      log.error(ex.getMessage(), ex);
    }
  }

  /**
   * Set access control settings in XML.
   * @param assessmentXml
   * @param accessControl
   */
  public void updateAccessControl(Assessment assessmentXml,
                                  AssessmentAccessControlIfc accessControl)
  {
    // DATES
    Date dueDate = accessControl.getDueDate();
    Date startDate = accessControl.getStartDate();
    Date scoreDate = accessControl.getScoreDate();
    Date restractDate = accessControl.getRetractDate();
    Date feedbackDate = accessControl.getFeedbackDate();
    assessmentXml.setFieldentry("END_DATE", formatDate(dueDate));
    assessmentXml.setFieldentry("FEEDBACK_DELIVERY_DATE",
                                formatDate(feedbackDate));
    assessmentXml.setFieldentry("RETRACT_DATE", formatDate(restractDate));
    assessmentXml.setFieldentry("START_DATE", formatDate(startDate));

    //MAX_ATTEMPTS
    Integer submissionsAllowed = accessControl.getSubmissionsAllowed();
    // set if unlimited
    Boolean unlimitedSubmissions = accessControl.getUnlimitedSubmissions();
    boolean unlimited = false;
    if (unlimitedSubmissions != null)
    {
      unlimited = unlimitedSubmissions.booleanValue();
    }
    if (unlimited)
    {
      submissionsAllowed = accessControl.UNLIMITED_SUBMISSIONS_ALLOWED;
    }
    assessmentXml.setFieldentry("MAX_ATTEMPTS",
                                submissionsAllowed.toString());

    // OTHER CONTROLS
    Integer autoSubmit = accessControl.getAutoSubmit();
    Integer bookmarking = accessControl.getBookMarkingItem();
    Integer itemNavigation = accessControl.getItemNavigation();
    Integer itemNumbering = accessControl.getItemNumbering();
    Integer displayScores = accessControl.getDisplayScoreDuringAssessments();
    Integer assessmentFormat = accessControl.getAssessmentFormat();
    Integer markForReview = accessControl.getMarkForReview();
    Integer lateHandling = accessControl.getLateHandling();
    Integer retryAllowed = accessControl.getRetryAllowed();
    Integer submissionsSaved = accessControl.getSubmissionsSaved();
    Integer timeLimit = accessControl.getTimeLimit();
    String submissionMessage = accessControl.getSubmissionMessage();
    String finalPageUrl = accessControl.getFinalPageUrl();
    String password = accessControl.getPassword();
    String releaseTo = accessControl.getReleaseTo();
    String userName = accessControl.getUsername();

    assessmentXml.setFieldentry("AUTO_SUBMIT", qtiBooleanString(autoSubmit));

    // getTimedAssessment() does not always tell us
    if (timeLimit != null && timeLimit.intValue() != 0)
    {
      setDuration(timeLimit, assessmentXml);
    }

    // submissions
    if (submissionMessage != null)
    {
      String wrappedSubmissionMessage = XmlUtil.convertStrforCDATA(submissionMessage);
      assessmentXml.setFieldentry("SUBMISSION_MESSAGE", wrappedSubmissionMessage, true);
    }
    if (finalPageUrl != null)
    {
      assessmentXml.setFieldentry("FINISH_URL", finalPageUrl);
    }

    if (accessControl.BY_QUESTION.equals(assessmentFormat))
    {
      assessmentXml.setFieldentry("QUESTION_LAYOUT", "I");
    }
    else if (accessControl.BY_PART.equals(assessmentFormat))
    {
      assessmentXml.setFieldentry("QUESTION_LAYOUT", "S");
    }
    else if (accessControl.BY_ASSESSMENT.equals(assessmentFormat))
    {
      assessmentXml.setFieldentry("QUESTION_LAYOUT", "A");
    }

    if (accessControl.LINEAR_ACCESS.equals(itemNavigation))
    {
      assessmentXml.setFieldentry("NAVIGATION", "LINEAR");
    }
    else if (accessControl.RANDOM_ACCESS.equals(itemNavigation))
    {
      assessmentXml.setFieldentry("NAVIGATION", "RANDOM");
    }

    if (accessControl.CONTINUOUS_NUMBERING.equals(itemNumbering))
    {
      assessmentXml.setFieldentry("QUESTION_NUMBERING", "CONTINUOUS");
    }
    else if (accessControl.RESTART_NUMBERING_BY_PART.equals(itemNumbering))
    {
      assessmentXml.setFieldentry("QUESTION_NUMBERING", "RESTART");
    }
    
    if(accessControl.HIDE_ITEM_SCORE_DURING_ASSESSMENT.equals(displayScores)){
    	assessmentXml.setFieldentry("DISPLAY_SCORES", "HIDE");
    }else{
    	assessmentXml.setFieldentry("DISPLAY_SCORES", "SHOW");
    }

    if (accessControl.MARK_FOR_REVIEW.equals(markForReview))
    {
      assessmentXml.setFieldentry("MARK_FOR_REVIEW", "True");
    }
    else if (accessControl.NOT_MARK_FOR_REVIEW.equals(markForReview))
    {
      assessmentXml.setFieldentry("MARK_FOR_REVIEW", "False");
    }
    
    if (accessControl.ACCEPT_LATE_SUBMISSION.equals(lateHandling))
    {
      assessmentXml.setFieldentry("LATE_HANDLING", "True");
    }
    else if (accessControl.NOT_ACCEPT_LATE_SUBMISSION.equals(lateHandling))
    {
      assessmentXml.setFieldentry("LATE_HANDLING", "False");
    }

    if (password != null)
    {
      assessmentXml.setFieldentry("PASSWORD", password);
    }
    if (releaseTo != null)
    {
      assessmentXml.setFieldentry("ASSESSMENT_RELEASED_TO", releaseTo);
    }
    if (userName != null)
    {
      assessmentXml.setFieldentry("USERID", userName);
    }
  }

  /**
   * If there is IP address set put IP addresses into allowed IP field in XML.
   * @param assessmentXml the XML
   * @param securedIPAddressSet the Set
   */
  public void updateIPAddressSet(Assessment assessmentXml,
                                 Set securedIPAddressSet)
  {
    if (securedIPAddressSet==null || securedIPAddressSet.size()==0)
    {
      return;
    }

    Iterator iter = securedIPAddressSet.iterator();

    StringBuilder ipAddressesbuf = new StringBuilder();
    
    while (iter.hasNext())
    {
      SecuredIPAddressIfc sip = (SecuredIPAddressIfc) iter.next();
      String ipAddress = sip.getIpAddress();
      //ipAddresses += ipAddress + "\n";
      ipAddressesbuf.append(ipAddress + "\n");
    }

    String ipAddresses = ipAddressesbuf.toString();
    assessmentXml.setFieldentry("ALLOW_IP", ipAddresses);

  }

  /**
   * If there are attachments set put them into ATTACHMENT field in XML.
   * @param assessmentXml the XML
   * @param securedIPAddressSet the Set
   */
  public void updateAttachmentSet(Assessment assessmentXml, Set attachmentSet)
  {
    Iterator iter = attachmentSet.iterator();
    AttachmentData attachmentData = null;
    StringBuffer attachment = new StringBuffer();
    while (iter.hasNext())
    {
    	attachmentData = (AttachmentData) iter.next();
    	attachment.append(attachmentData.getResourceId().replaceAll(" ", ""));
    	attachment.append("|");
    	attachment.append(attachmentData.getFilename());
    	attachment.append("|");
    	attachment.append(attachmentData.getMimeType());
    	attachment.append("\n");
    }
    assessmentXml.setFieldentry("ATTACHMENT", attachment.toString());
  }


  /**
   * Look up and set metadata fields
   * @param assessmentXml
   * @param assessment
   */
  public void updateMetaData(Assessment assessmentXml,
                             AssessmentFacade assessment)
  {
    String[] editKeys =
      {
      "templateInfo_isInstructorEditable",
      "assessmentAuthor_isInstructorEditable",
      "assessmentCreator_isInstructorEditable",
      "description_isInstructorEditable",
      "dueDate_isInstructorEditable",
      "retractDate_isInstructorEditable",
      "anonymousRelease_isInstructorEditable",
      "authenticatedRelease_isInstructorEditable",
      "ipAccessType_isInstructorEditable",
      "passwordRequired_isInstructorEditable",
      "lockedBrowser_isInstructorEditable",
      "timedAssessment_isInstructorEditable",
      "timedAssessmentAutoSubmit_isInstructorEditable",
      "itemAccessType_isInstructorEditable",
      "displayChunking_isInstructorEditable",
      "displayNumbering_isInstructorEditable",
      "displayScores_isInstructorEditable",
      "submissionModel_isInstructorEditable",
      "lateHandling_isInstructorEditable",
      "markForReview_isInstructorEditable",
      "automaticSubmission_isInstructorEditable",
      "autoSave_isInstructorEditable",
      "submissionMessage_isInstructorEditable",
      "finalPageURL_isInstructorEditable",
      "feedbackType_isInstructorEditable",
      "feedbackAuthoring_isInstructorEditable",
      "feedbackComponents_isInstructorEditable",
      "testeeIdentity_isInstructorEditable",
      "toGradebook_isInstructorEditable",
      "recordedScore_isInstructorEditable",
      "bgColor_isInstructorEditable",
      "bgImage_isInstructorEditable",
      "metadataAssess_isInstructorEditable",
      "metadataParts_isInstructorEditable",
      "metadataQuestions_isInstructorEditable",
      "honorpledge_isInstructorEditable"
    };

    String key;
    String value;

    for (int i = 0; i < editKeys.length; i++)
    {
      setField(assessmentXml, assessment, editKeys[i]);
    }
    //  item metadata
    setField(assessmentXml, assessment, "hasMetaDataForQuestions",
             "COLLECT_ITEM_METADATA");
  }

  /**
   *
   * @param assessmentXml
   * @param assessment
   * @param translationKey
   * @param key
   */
  private void setField(Assessment assessmentXml, AssessmentFacade assessment,
                        String key, String translationKey)
  {

    String value = assessment.getAssessmentMetaDataByLabel(key);
    log.debug("setField(Assessment assessmentXml, AssessmentFacade assessment, String key, String translationKey)");
    log.debug("key: " + key);
    log.debug("value: " + value);

    if (value == null)
    {
   	  assessmentXml.setFieldentry(translationKey, "false");
      return;
    }

    assessmentXml.setFieldentry(translationKey, value);
  }

  /**
   *
   * @param assessmentXml
   * @param assessment
   * @param key
   */
  private void setField(Assessment assessmentXml, AssessmentFacade assessment,
                        String key)
  {
    setField(assessmentXml, assessment, key, key);
  }

  /**
   * format Iso8601 Date
   * @param date Date object
   * @return Iso8601 date string or "" if not set
   */
  private String formatDate(Date date)
  {
    if (date == null)
    {
      return "";
    }
    Iso8601DateFormat iso = new Iso8601DateFormat();
    return iso.format(date);
  }

  /**
   * Map Boolean to text string
   * @param b Boolean
   * @return "True"|"False"
   */
  public String qtiBooleanString(Boolean b)
  {
    if (b != null && b.booleanValue())
    {
      return "True";
    }
    return "False";
  }

  /**
   * Map Integer to text string
   * @param i Integer
   * @return "True"|"False"
   */
  public String qtiBooleanString(Integer i)
  {
    if (i != null && i.intValue() != 0)
    {
      return "True";
    }
    return "False";
  }

  /**
   * get section XML by title
   *
   * @param assessment section XML
   * @param sectionTitle title
   *
   * @return section XML
   */
  public Section getSectionByTitle(
    Assessment assessment, String sectionTitle)
  {
    if (log.isDebugEnabled())
    {
      log.debug(
        "getSectionByTitle( )");
    }

    Section sectionXml = null;

    Collection secs = assessment.getSections();
    if (secs == null) {
    	return sectionXml;
    }
    Iterator iter = secs.iterator();
    if ( (secs != null) && (secs.size() > 0) && (sectionTitle != null))
    {
      for (int i = 0; i < secs.size(); i++)
      {
        while (iter.hasNext())
        {
          sectionXml = (Section) iter.next();
          String title =
            sectionXml.selectSingleValue("section/@title", "attribute");
          if ( (title != null) && title.equals(sectionTitle))
          {
            break;
          }
        }
      }
    }

    return sectionXml;
  }

  /** Set the assessment duration.
   * @param duration assessment duration in seconds
   * @param assessmentXml the xml
   */
  public void setDuration(Integer duration, Assessment assessmentXml)
  {
    String xpath = "questestinterop/assessment/duration";
    List list = assessmentXml.selectNodes(xpath);
    try
    {
      Iso8601TimeInterval isoTime =
        new Iso8601TimeInterval(1000 * duration.longValue());
      assessmentXml.update(xpath, isoTime.toString());
    }
    catch (Exception ex)
    {
      log.error(ex.getMessage(), ex);
    }
  }

}
