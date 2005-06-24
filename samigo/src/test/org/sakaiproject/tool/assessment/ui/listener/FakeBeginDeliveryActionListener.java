/**********************************************************************************
* $HeadURL$
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

package test.org.sakaiproject.tool.assessment.ui.listener;

import java.util.Iterator;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.log4j.Logger;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SecuredIPAddressIfc;
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
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class FakeBeginDeliveryActionListener implements ActionListener
{
  static Logger LOG = Logger.getLogger(FakeBeginDeliveryActionListener.class.
                      getName());

  private static ContextUtil cu;
  private static String ID_TO_TEST = "3";

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();

    // get service
    PublishedAssessmentService publishedAssessmentService = new
      PublishedAssessmentService();
    // get managed bean
    DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");
    // get assessment
    PublishedAssessmentFacade pub = null;
    pub = lookupPublishedAssessment(ID_TO_TEST, publishedAssessmentService);
    System.out.println("** FakeBeginDeliveryActionListener, pub = "+pub);
    System.out.println("** FakeBeginDeliveryActionListener, pub title = "+pub.getTitle());

    // populate backing bean from published assessment
    populateBeanFromPub(delivery, pub);

    // add in course management system info
    CourseManagementBean course = (CourseManagementBean) cu.lookupBean("course");
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
      feed.setShowCorrectResponse(new Boolean(true));
      feed.setShowGraderComments(new Boolean(true));
      feed.setShowQuestionLevelFeedback(new Boolean(true));
      feed.setShowQuestionText(new Boolean(true));
      feed.setShowSelectionLevelFeedback(new Boolean(true));
      feed.setShowStatistics(new Boolean(true));
      feed.setShowStudentScore(new Boolean(true));
      feed.setFeedbackDelivery(feed.FEEDBACK_BY_DATE);
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
      control.setUsername("Groucho");
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
    System.out.println("** 1. FakeBeginDeliveryActionListener, deliveryBean = "+delivery);
    System.out.println("** 2. FakeBeginDeliveryActionListener, pubAssessment = "+pubAssessment);
    System.out.println("** 3. FakeBeginDeliveryActionListener, id= "+pubAssessment.getAssessmentId());
    System.out.println("** 4. FakeBeginDeliveryActionListener, pubId= "+pubAssessment.getPublishedAssessmentId());
    delivery.setAssessmentId((pubAssessment.getPublishedAssessmentId()).toString());
    delivery.setAssessmentTitle(pubAssessment.getTitle());
    delivery.setInstructorMessage(pubAssessment.getDescription());
    delivery.setCreatorName(pubAssessment.getCreatedBy());
    delivery.setSubmitted(false);
    delivery.setGraded(false);
    delivery.setPreviewMode(false);

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
    Integer feedbackDelivery = info.getFeedbackDelivery();
    feedback.setShowDateFeedback(info.FEEDBACK_BY_DATE.equals(feedbackDelivery));
    feedback.setShowImmediate(info.IMMEDIATE_FEEDBACK.equals(feedbackDelivery));
    feedback.setShowNoFeedback(info.NO_FEEDBACK.equals(feedbackDelivery));
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
    settings.setAutoSubmit(control.AUTO_SUBMIT.equals(control.getAutoSubmit()));
    settings.setAutoSave(control.AUTO_SAVE.equals(control.getSubmissionsSaved()));
    settings.setDueDate(control.getDueDate());
    settings.setMaxAttempts(control.getRetryAllowed().intValue());
    settings.setSubmissionMessage(control.getSubmissionMessage());
    settings.setUnlimitedAttempts(
      control.UNLIMITED_SUBMISSIONS_ALLOWED.equals(control.getSubmissionsAllowed()));
    settings.setFeedbackDate(control.getFeedbackDate());
    Integer format = control.getAssessmentFormat();
    settings.setFormatByAssessment(control.BY_ASSESSMENT.equals(format));
    settings.setFormatByPart(control.BY_PART.equals(format));
    settings.setFormatByQuestion(control.BY_QUESTION.equals(format));
    settings.setUsername(control.getUsername());
    settings.setPassword(control.getPassword());
  }

  /**
   * Gets the secured IP address set and converts to a newline delimited String.
   * @param pubAssessment the assessment
   * @return newline delimited String of IP addresses
   */
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


}
