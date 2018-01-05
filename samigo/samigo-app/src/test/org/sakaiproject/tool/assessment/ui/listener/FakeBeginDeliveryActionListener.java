/**
 * Copyright (c) 2005-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.assessment.ui.listener;

import java.util.Date;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.cms.CourseManagementBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FeedbackComponent;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SettingsDeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module fakes the link  of published assessment
 * <p>Description: Sakai Assessment Manager</p>
 */
@Slf4j
public class FakeBeginDeliveryActionListener implements ActionListener
{
  private static String ID_TO_TEST = "3";

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    // get service
    PublishedAssessmentService publishedAssessmentService = new
      PublishedAssessmentService();
    // get managed bean
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    // get assessment
    PublishedAssessmentFacade pub = null;
    pub = lookupPublishedAssessment(ID_TO_TEST, publishedAssessmentService);
    log.info("** FakeBeginDeliveryActionListener, pub = "+pub);
    log.info("** FakeBeginDeliveryActionListener, pub title = "+pub.getTitle());

    // populate backing bean from published assessment
    populateBeanFromPub(delivery, pub);

    // add in course management system info
    CourseManagementBean course = (CourseManagementBean) ContextUtil.lookupBean("course");
    populateBeanFromCourse(delivery, course);
  }

  private PublishedAssessmentFacade lookupPublishedAssessment(String id,
    PublishedAssessmentService publishedAssessmentService
    )
  {
    PublishedAssessmentFacade pub;
//    try
//    {
//      pub =
//        publishedAssessmentService.getPublishedAssessment(id);
//    }
//    catch (Exception ex)
//    {
      // debug values for now...
    PublishedAssessmentService assessmentService = new PublishedAssessmentService();
    pub = assessmentService.getPublishedAssessment("3");
    /*
      pub = new PublishedAssessmentFacade();
      pub.setAssessmentId(Long.getLong("123456"));
      pub.setTitle("This is a test title");
      pub.setDescription("This is a test description.");
      pub.setCreatedBy("A wizard.");
    */
      AssessmentFeedback feed = new AssessmentFeedback();
      feed.setShowCorrectResponse(Boolean.TRUE);
      feed.setShowGraderComments(Boolean.TRUE);
      feed.setShowQuestionLevelFeedback(Boolean.TRUE);
      feed.setShowQuestionText(Boolean.TRUE);
      feed.setShowSelectionLevelFeedback(Boolean.TRUE);
      feed.setShowStatistics(Boolean.TRUE);
      feed.setShowStudentScore(Boolean.TRUE);
      feed.setShowStudentQuestionScore(Boolean.TRUE);
      feed.setFeedbackDelivery(AssessmentFeedbackIfc.FEEDBACK_BY_DATE);
      feed.setFeedbackAuthoring(AssessmentFeedbackIfc.QUESTIONLEVEL_FEEDBACK);
      pub.setAssessmentFeedback(feed);
      /*
      PublishedAccessControl control = new PublishedAccessControl();
      //
      control.setAutoSubmit(control.AUTO_SUBMIT);
      control.setSubmissionsSaved(control.SAVE_ON_CLICK);
      control.setRetryAllowed(control.CONTINUOUS_NUMBERING);
      control.setDueDate( new Date());
      control.setSubmissionMessage("Yo. Submitted.");
      control.setSubmissionsAllowed(control.UNLIMITED_SUBMISSIONS_ALLOWED);
      control.setFeedbackDate(new Date());
      control.setAssessmentFormat(control.BY_PART);
      control.setPassword("swordfish");
      pub.setAssessmentAccessControl(new PublishedAccessControl());
//    }
*/
    return pub;
  }

  /**
   * This takes the published assessment information and puts it in the delivery
   * bean.  This is primarily the information that needs to be set up for the
   * begin assessment page.  Additional properties will be set when the student
   * elects to begin taking assessment.
   * @param delivery
   * @param pubAssessment
   */
  private void populateBeanFromPub(DeliveryBean delivery,
    PublishedAssessmentFacade pubAssessment)
  {
    // global information
    log.info("** 1. FakeBeginDeliveryActionListener, deliveryBean = "+delivery);
    log.info("** 2. FakeBeginDeliveryActionListener, pubAssessment = "+pubAssessment);
    log.info("** 3. FakeBeginDeliveryActionListener, id= "+pubAssessment.getAssessmentId());
    log.info("** 4. FakeBeginDeliveryActionListener, pubId= "+pubAssessment.getPublishedAssessmentId());
    delivery.setAssessmentId((pubAssessment.getPublishedAssessmentId()).toString());
    delivery.setAssessmentTitle(pubAssessment.getTitle());
    delivery.setHonorPledge(pubAssessment.getAssessmentAccessControl().getHonorPledge());
    delivery.setInstructorMessage(pubAssessment.getDescription());
    delivery.setCreatorName(pubAssessment.getCreatedBy());
    delivery.setSubmitted(false);
    delivery.setGraded(false);
    //delivery.setPreviewMode(false);
    delivery.setFeedbackOnDate(false);

    // initialize current position in taking assessment to start
    /**
     * @todo RESTART ON PARTIALLY FINISHED ASSESSMENT (non-zero)
     */
    delivery.setItemIndex(0);
    delivery.setSectionIndex(0);

    // feedback
    FeedbackComponent feedback = new FeedbackComponent();
    populateFeedbackComponent(feedback, pubAssessment);
    delivery.setFeedbackComponent(feedback);

    AssessmentAccessControlIfc control = (AssessmentAccessControlIfc)pubAssessment.getAssessmentAccessControl();
    Date currentDate = new Date();
    if (feedback.getShowDateFeedback() && control.getFeedbackDate()!= null && currentDate.after(control.getFeedbackDate()))
    {
        delivery.setFeedbackOnDate(true); 
    }

    // settings
    SettingsDeliveryBean settings = new SettingsDeliveryBean();
    populateSettings(settings, pubAssessment);
    delivery.setSettings(settings);
  }

  /**
   * This takes the course information and puts it in the delivery
   * bean.  Just getting course and instructor for now, could be extended later.
   * @param delivery the delivery bean
   * @param course the course info bean
   */
  private void populateBeanFromCourse(DeliveryBean delivery,
    CourseManagementBean course)
  {
    delivery.setCourseName(course.getCourseName());
    delivery.setInstructorName(course.getInstructor());
  }

  /**
   * This grabs the assessment feedback & puts it in the FeedbackComponent
   * @param feedback
   * @param pubAssessment
   */
  private void populateFeedbackComponent(FeedbackComponent feedback,
    PublishedAssessmentIfc pubAssessment)
  {
    AssessmentFeedback info =  (AssessmentFeedback) pubAssessment.getAssessmentFeedback();
    feedback.setShowCorrectResponse(info.getShowCorrectResponse().booleanValue());
    feedback.setShowGraderComment(info.getShowGraderComments().booleanValue());
    feedback.setShowItemLevel(info.getShowQuestionLevelFeedback().booleanValue());
    feedback.setShowQuestion(info.getShowQuestionText().booleanValue());
    feedback.setShowResponse(info.getShowCorrectResponse().booleanValue());//???
    feedback.setShowSelectionLevel(info.getShowSelectionLevelFeedback().booleanValue());
    feedback.setShowStats(info.getShowStatistics().booleanValue());
    feedback.setShowStudentScore(info.getShowStudentScore().booleanValue());
    feedback.setShowStudentQuestionScore(info.getShowStudentQuestionScore().booleanValue());
    Integer feedbackDelivery = info.getFeedbackDelivery();
    feedback.setShowDateFeedback(AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(feedbackDelivery));
    feedback.setShowImmediate(AssessmentFeedbackIfc.IMMEDIATE_FEEDBACK.equals(feedbackDelivery));
    feedback.setShowNoFeedback(AssessmentFeedbackIfc.NO_FEEDBACK.equals(feedbackDelivery));
  }

  /**
   * This grabs the assessment and its AssessmentAccessControlIfc &
   * puts it in the SettingsDeliveryBean.
   * @param settings
   * @param pubAssessment
   */
  private void populateSettings(SettingsDeliveryBean settings,
    PublishedAssessmentIfc pubAssessment)
  {
    //settings.setIpAddresses(constructIpLines(pubAssessment));
    AssessmentAccessControlIfc   control =
      pubAssessment.getAssessmentAccessControl();
    if (control != null)
      constructControlSettings(settings, control);
  }

  /**
   * Massage control settings into settings bean
   * @param settings target SettingsDeliveryBean
   * @param control the AssessmentAccessControlIfc
   */
  private void constructControlSettings(SettingsDeliveryBean settings,
    AssessmentAccessControlIfc  control )
  {
    settings.setAutoSubmit(AssessmentAccessControlIfc.AUTO_SUBMIT.equals(control.getAutoSubmit()));
    settings.setAutoSave(AssessmentAccessControlIfc.AUTO_SAVE.equals(control.getSubmissionsSaved()));
    settings.setDueDate(control.getDueDate());
    settings.setMaxAttempts(control.getRetryAllowed().intValue());
    settings.setSubmissionMessage(control.getSubmissionMessage());
    settings.setUnlimitedAttempts(
      AssessmentAccessControlIfc.UNLIMITED_SUBMISSIONS_ALLOWED.equals(control.getSubmissionsAllowed()));
    settings.setFeedbackDate(control.getFeedbackDate());
    Integer format = control.getAssessmentFormat();
    settings.setFormatByAssessment(AssessmentAccessControlIfc.BY_ASSESSMENT.equals(format));
    settings.setFormatByPart(AssessmentAccessControlIfc.BY_PART.equals(format));
    settings.setFormatByQuestion(AssessmentAccessControlIfc.BY_QUESTION.equals(format));
    settings.setPassword(control.getPassword());
  }

  /**
   * Gets the secured IP address set and converts to a newline delimited String.
   * @param pubAssessment the assessment
   * @return newline delimited String of IP addresses
   */
  /*
  private String constructIpLines(PublishedAssessmentIfc pubAssessment)
  {
    String ipLines = "";

    Set ipSet = pubAssessment.getSecuredIPAddressSet();
    if (ipSet != null){
      Iterator iter = ipSet.iterator();
      String ipSep = "\n";
      while(iter.hasNext())
      {
        SecuredIPAddressIfc ipAddr = (SecuredIPAddressIfc)
          iter.next();
        ipLines += ipAddr.getIpAddress() + ipSep;
      }
    }
    return ipLines;
  }
  */

}
