/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.Iterator;
import java.util.Set;
import java.util.Date;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.cms.CourseManagementBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FeedbackComponent;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SettingsDeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module handles the beginning of the assessment
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class BeginDeliveryActionListener implements ActionListener
{
  private static Log log = LogFactory.getLog(BeginDeliveryActionListener.class);
  private static ContextUtil cu;

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("BeginDeliveryActionListener.processAction() ");

    // get managed bean
    DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");
    delivery.setTimeRunning(true);

    // get service
    PublishedAssessmentService publishedAssessmentService = new
        PublishedAssessmentService();
    PublishedAssessmentFacade pub = null;

    // if this page is access through selectAssessment, publishedId should
    // not be null
    String publishedId = cu.lookupParam("publishedId");

    // if this page is accessed through assessment preview, previewAssessment should be true and assessmentId should not be null
    String previewAssessment = (String)cu.lookupParam("previewAssessment");
    String assessmentId = (String)cu.lookupParam("assessmentId");

    if (previewAssessment != null)
    {
	delivery.setPreviewAssessment(previewAssessment);
    }

    if (publishedId == null || publishedId.trim().equals(""))
    {
	     // this is accessed via assessment preview link
       if ("true".equals(delivery.getPreviewAssessment()) && assessmentId !=null)
       {
        //now always publish and get a new publishedId for preview assessment
         AssessmentService assessmentService = new AssessmentService();
         AssessmentFacade assessment = assessmentService.getAssessment(assessmentId);
         try {
          pub = publishedAssessmentService.publishPreviewAssessment(assessment);
         } catch (Exception e) {
          log.error(e);
          e.printStackTrace();
         }
         publishedId = pub.getPublishedAssessmentId().toString();
       }
       else
       {
         // this is accessed via publishedUrl so pubishedId==null
         pub = delivery.getPublishedAssessment();
         if (pub == null)
           throw new AbortProcessingException(
            "taking: publishedAsessmentId null or blank");
         else
           publishedId = pub.getPublishedAssessmentId().toString();
       }
    }
    delivery.setNotPublished("true"); //always set "true" since will generate a new publisheId for every preview assessment now

    // get assessment, this includes some dummy code as well
    pub = lookupPublishedAssessment(publishedId, publishedAssessmentService);
    delivery.setPublishedAssessment(pub);

    // populate backing bean from published assessment
    populateBeanFromPub(delivery, pub);

    // add in course management system info
    CourseManagementBean course = (CourseManagementBean) cu.lookupBean("course");
    populateBeanFromCourse(pub,delivery, course);

  }

  private PublishedAssessmentFacade lookupPublishedAssessment(String id,
    PublishedAssessmentService publishedAssessmentService
    )
  {
    PublishedAssessmentFacade pub;
    PublishedAssessmentService assessmentService = new PublishedAssessmentService();
    pub = assessmentService.getPublishedAssessment(id);
    if (pub.getAssessmentFeedback()==null)
    {
      AssessmentFeedback feed = new AssessmentFeedback();
      feed.setShowCorrectResponse(new Boolean(false));
      feed.setShowGraderComments(new Boolean(false));
      feed.setShowQuestionLevelFeedback(new Boolean(false));
      feed.setShowQuestionText(new Boolean(true));
      feed.setShowSelectionLevelFeedback(new Boolean(false));
      feed.setShowStatistics(new Boolean(false));
      feed.setShowStudentScore(new Boolean(false));
      feed.setShowStudentQuestionScore(new Boolean(false));
      feed.setFeedbackDelivery(feed.NO_FEEDBACK);
      feed.setFeedbackAuthoring(feed.QUESTIONLEVEL_FEEDBACK);
      pub.setAssessmentFeedback(feed);
    }
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
  public void populateBeanFromPub(DeliveryBean delivery,
    PublishedAssessmentFacade pubAssessment)
  {
    // global information


    delivery.setAssessmentId((pubAssessment.getPublishedAssessmentId()).toString());
    delivery.setAssessmentTitle(pubAssessment.getTitle());
    delivery.setInstructorMessage(pubAssessment.getDescription());
    // for now instructor is the creator 'cos sakai don't have instructor role in 1.5
    delivery.setCourseName(pubAssessment.getOwnerSite());
    delivery.setCreatorName(AgentFacade.getDisplayNameByAgentId(pubAssessment.getCreatedBy()));
    delivery.setInstructorName(AgentFacade.getDisplayNameByAgentId(pubAssessment.getCreatedBy()));
    delivery.setSubmitted(false);
    delivery.setGraded(false);
    delivery.setPreviewMode(false);
    delivery.setPartIndex(0);
    delivery.setQuestionIndex(0);
    delivery.setBeginTime(null);
    delivery.setFeedbackOnDate(false);

    AssessmentAccessControlIfc control = (AssessmentAccessControlIfc)pubAssessment.getAssessmentAccessControl();
    delivery.setDueDate(control.getDueDate());

    // feedback
    FeedbackComponent feedback = new FeedbackComponent();
    populateFeedbackComponent(feedback, pubAssessment);
    delivery.setFeedbackComponent(feedback);
   
    Date currentDate = new Date();
    if (feedback.getShowDateFeedback() && control.getFeedbackDate()!= null && currentDate.after(control.getFeedbackDate()))
    {
        delivery.setFeedbackOnDate(true); 
    }
    
    // settings
    SettingsDeliveryBean settings = new SettingsDeliveryBean();
    populateSettings(settings, pubAssessment, delivery);
    delivery.setSettings(settings);
  }

  /**
   * This takes the course information and puts it in the delivery
   * bean.  Just getting course and instructor for now, could be extended later.
   * @param delivery the delivery bean
   * @param course the course info bean
   */
  private void populateBeanFromCourse(PublishedAssessmentIfc pub, DeliveryBean delivery,
    CourseManagementBean course)
  {
    delivery.setCourseName(pub.getOwnerSite());
    delivery.setInstructorName(AgentFacade.getDisplayNameByAgentId(pub.getCreatedBy()));
  }

  /**
   * This grabs the assessment feedback & puts it in the FeedbackComponent
   * @param feedback
   * @param pubAssessment
   */
  private void populateFeedbackComponent(FeedbackComponent feedback,
    PublishedAssessmentFacade pubAssessment)
  {
    AssessmentFeedbackIfc info =  (AssessmentFeedbackIfc) pubAssessment.getAssessmentFeedback();
    if ( info != null) {
       feedback.setShowCorrectResponse(info.getShowCorrectResponse().booleanValue());
       feedback.setShowGraderComment(info.getShowGraderComments().booleanValue());
       feedback.setShowItemLevel(info.getShowQuestionLevelFeedback().booleanValue());
       feedback.setShowQuestion(info.getShowQuestionText().booleanValue());
       feedback.setShowResponse(info.getShowStudentResponse().booleanValue());
       feedback.setShowSelectionLevel(info.getShowSelectionLevelFeedback().booleanValue());
       feedback.setShowStats(info.getShowStatistics().booleanValue());
       feedback.setShowStudentScore(info.getShowStudentScore().booleanValue());
       if (info.getShowStudentQuestionScore()!=null)
         feedback.setShowStudentQuestionScore(info.getShowStudentQuestionScore().booleanValue());
       else
         feedback.setShowStudentQuestionScore(false);
       Integer feedbackDelivery = info.getFeedbackDelivery();
       feedback.setShowDateFeedback(info.FEEDBACK_BY_DATE.equals(feedbackDelivery));
       feedback.setShowImmediate(info.IMMEDIATE_FEEDBACK.equals(feedbackDelivery));
       feedback.setShowNoFeedback(info.NO_FEEDBACK.equals(feedbackDelivery));
    }
  }

  /**
   * This grabs the assessment and its AssessmentAccessControlIfc &
   * puts it in the SettingsDeliveryBean.
   * @param settings
   * @param pubAssessment
   */
  private void populateSettings(SettingsDeliveryBean settings,
    PublishedAssessmentIfc pubAssessment, DeliveryBean delivery)
  {
    settings.setIpAddresses(pubAssessment.getSecuredIPAddressSet());
    AssessmentAccessControlIfc   control =
      pubAssessment.getAssessmentAccessControl();
    constructControlSettings(delivery,settings, control, pubAssessment.getPublishedAssessmentId());
    if (control.getItemNavigation() == null)
      delivery.setNavigation(AssessmentAccessControl.RANDOM_ACCESS.toString());
    else
      delivery.setNavigation(control.getItemNavigation().toString());

    // check if we need to time the assessment, i.e.hasTimeassessment="true"
    String hasTimeLimit = pubAssessment.getAssessmentMetaDataByLabel("hasTimeAssessment");
    if (hasTimeLimit!=null && hasTimeLimit.equals("true"))
      delivery.setHasTimeLimit(true);
    else
      delivery.setHasTimeLimit(false);

    try
    {
      if (control.getTimeLimit() != null)
      {
           delivery.setTimeLimit(control.getTimeLimit().toString());
           int seconds = control.getTimeLimit().intValue();
           int hour = 0;
           int minute = 0;
           if (seconds>=3600)
	   {
               hour = Math.abs(seconds/3600);
               minute =Math.abs((seconds-hour*3600)/60);
           }
           else
	   {
               minute = Math.abs(seconds/60);
           }
           delivery.setTimeLimit_hour(hour);
           delivery.setTimeLimit_minute(minute);
      }
    } catch (Exception e)
    {
      delivery.setTimeLimit("");
    }
    Set set = pubAssessment.getAssessmentMetaDataSet();
    Iterator iter = set.iterator();
    while (iter.hasNext())
    {
      AssessmentMetaDataIfc data = (AssessmentMetaDataIfc) iter.next();
      if (data.getLabel().equals(AssessmentMetaDataIfc.BGCOLOR))
        settings.setBgcolor(data.getEntry());
      else if (data.getLabel().equals(AssessmentMetaDataIfc.BGIMAGE))
        settings.setBackground(data.getEntry());
    }
  }

  /**
   * Massage control settings into settings bean
   * @param settings target SettingsDeliveryBean
   * @param control the AssessmentAccessControlIfc
   */
  private void constructControlSettings(DeliveryBean delivery,SettingsDeliveryBean settings,
    AssessmentAccessControlIfc  control, Long publishedAssessmentId )
  {
    PublishedAssessmentService service = new PublishedAssessmentService();
    int totalSubmissions = (service.getTotalSubmission(AgentFacade.getAgentString(),
        publishedAssessmentId.toString())).intValue();
    settings.setAutoSubmit(control.AUTO_SUBMIT.equals(control.getAutoSubmit()));
    settings.setAutoSave(control.AUTO_SAVE.equals(control.getSubmissionsSaved()));
    settings.setDueDate(control.getDueDate());
    if ((Boolean.TRUE).equals(control.getUnlimitedSubmissions())){
      settings.setUnlimitedAttempts(true);
    }
    else{
      settings.setUnlimitedAttempts(false);
      if (control.getSubmissionsAllowed() != null) {
        settings.setMaxAttempts(control.getSubmissionsAllowed().intValue());
        delivery.setSubmissionsRemaining(control.getSubmissionsAllowed().
                                         intValue() - totalSubmissions);
      }
    }
    settings.setSubmissionMessage(control.getSubmissionMessage());
    settings.setFeedbackDate(control.getFeedbackDate());
    Integer format = control.getAssessmentFormat();
    if (format == null)
      format = new Integer(1);
    settings.setFormatByAssessment(control.BY_ASSESSMENT.equals(format));
    settings.setFormatByPart(control.BY_PART.equals(format));
    settings.setFormatByQuestion(control.BY_QUESTION.equals(format));
    settings.setUsername(control.getUsername());
    settings.setPassword(control.getPassword());
    settings.setItemNumbering(control.getItemNumbering().toString());
  }
}
