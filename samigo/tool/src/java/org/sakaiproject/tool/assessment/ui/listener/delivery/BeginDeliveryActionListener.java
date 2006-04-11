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
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedFeedback;
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
import org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentThread;
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

    // get managed bean and set its action accordingly
    DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");
    String actionString = cu.lookupParam("actionString");
    if (actionString != null) {
      // if actionString is null, likely that action & actionString has been set already, 
      // e.g. take assessment via url, actionString is set by LoginServlet.
      // preview and take assessment is set by the parameter in the jsp pages
      delivery.setActionString(actionString);
    }
    int action = delivery.getActionMode();
    System.out.println("**** BeginDeliveryActionListener:actionString"+delivery.getActionString());
    System.out.println("**** BeginDeliveryActionListener:action"+delivery.getActionMode());

    // reset timer before begin
    delivery.setTimeElapse("0");
    delivery.setTimeElapseAfterFileUpload(null);
    delivery.setLastTimer(0);
    delivery.setTimeLimit("0");
    delivery.setBeginAssessment(true);
    delivery.setTimeStamp((new Date()).getTime());

    PublishedAssessmentFacade pub = getPublishedAssessmentBasedOnAction(action, delivery);
    delivery.setPublishedAssessment(pub);

    // populate backing bean from published assessment
    populateBeanFromPub(delivery, pub);

    // add in course management system info
    CourseManagementBean course = (CourseManagementBean) cu.lookupBean("course");
    populateBeanFromCourse(pub, delivery, course);

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
      pub.setAssessmentFeedback(new PublishedFeedback());
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
    AssessmentAccessControlIfc control = (AssessmentAccessControlIfc)pubAssessment.getAssessmentAccessControl();

    // populate deliveryBean, settingsBean and feedbackComponent .
    // deliveryBean contains settingsBean & feedbackComponent)
    populateDelivery(delivery, pubAssessment);

    SettingsDeliveryBean settings = populateSettings(pubAssessment);
    delivery.setSettings(settings);

    // feedback
    FeedbackComponent component = populateFeedbackComponent(pubAssessment);
    delivery.setFeedbackComponent(component);

    // important: set feedbackOnDate last
    Date currentDate = new Date();
    if (component.getShowDateFeedback() && control.getFeedbackDate()!= null && currentDate.after(control.getFeedbackDate())) {
      delivery.setFeedbackOnDate(true); 
    }
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
  private FeedbackComponent populateFeedbackComponent(PublishedAssessmentFacade pubAssessment)
  {
    FeedbackComponent component = new FeedbackComponent();
    AssessmentFeedbackIfc info =  (AssessmentFeedbackIfc) pubAssessment.getAssessmentFeedback();
    if ( info != null) {
      component.setAssessmentFeedback(info);
    }
    return component;
  }

  /**
   * This grabs the assessment and its AssessmentAccessControlIfc &
   * puts it in the SettingsDeliveryBean.
   * @param settings
   * @param pubAssessment
   */
  private SettingsDeliveryBean populateSettings(PublishedAssessmentIfc pubAssessment)
  {
    // #1 - poplulate control properties such as dueDate, feedbackDate, autoSubmit, autoSave
    //      max. no. of attempt, display format, username & password - mostly info 
    //      on the BeginAssessment page. And deliveryBean contains settingsBean
    SettingsDeliveryBean settings = new SettingsDeliveryBean();
    settings.setAssessmentAccessControl(pubAssessment);
    return settings;
  }

  /**
   * Massage control settings into settings bean
   * @param settings target SettingsDeliveryBean
   * @param control the AssessmentAccessControlIfc
   */
  private void populateDelivery(DeliveryBean delivery, PublishedAssessmentIfc pubAssessment){

    Long publishedAssessmentId = pubAssessment.getPublishedAssessmentId();
    AssessmentAccessControlIfc control = pubAssessment.getAssessmentAccessControl();
    PublishedAssessmentService service = new PublishedAssessmentService();

    // #0 - global information
    delivery.setAssessmentId((pubAssessment.getPublishedAssessmentId()).toString());
    delivery.setAssessmentTitle(pubAssessment.getTitle());
    delivery.setInstructorMessage(pubAssessment.getDescription());
    // for now instructor is the creator 'cos sakai don't have instructor role in 1.5
    delivery.setCourseName(pubAssessment.getOwnerSite());
    delivery.setCreatorName(AgentFacade.getDisplayNameByAgentId(pubAssessment.getCreatedBy()));
    delivery.setInstructorName(AgentFacade.getDisplayNameByAgentId(pubAssessment.getCreatedBy()));
    delivery.setSubmitted(false);
    delivery.setGraded(false);
    delivery.setPartIndex(0);
    delivery.setQuestionIndex(0);
    delivery.setBeginTime(null);
    delivery.setFeedbackOnDate(false);
    delivery.setDueDate(control.getDueDate());

    // #1 - set submission remains
    int totalSubmissions = (service.getTotalSubmission(AgentFacade.getAgentString(),
        publishedAssessmentId.toString())).intValue();
    if (!(Boolean.TRUE).equals(control.getUnlimitedSubmissions())){
      delivery.setSubmissionsRemaining(control.getSubmissionsAllowed().intValue() - totalSubmissions);
    }

    // #2 - check if TOC should be made avaliable
    if (control.getItemNavigation() == null)
      delivery.setNavigation(AssessmentAccessControl.RANDOM_ACCESS.toString());
    else
      delivery.setNavigation(control.getItemNavigation().toString());

    // #3 - if this is a timed assessment, set the time limit in hr, min & sec.
    setTimedAssessment(delivery, pubAssessment);

  }

  private void setTimedAssessment(DeliveryBean delivery, PublishedAssessmentIfc pubAssessment){

    AssessmentAccessControlIfc control = pubAssessment.getAssessmentAccessControl();
    // check if we need to time the assessment, i.e.hasTimeassessment="true"
    String hasTimeLimit = pubAssessment.getAssessmentMetaDataByLabel("hasTimeAssessment");
    if (hasTimeLimit!=null && hasTimeLimit.equals("true"))
      delivery.setHasTimeLimit(true);
    else
      delivery.setHasTimeLimit(false);

    try {
      if (control.getTimeLimit() != null) {
        delivery.setTimeLimit(control.getTimeLimit().toString());
        int seconds = control.getTimeLimit().intValue();
        int hour = 0;
        int minute = 0;
        if (seconds>=3600) {
          hour = Math.abs(seconds/3600);
          minute =Math.abs((seconds-hour*3600)/60);
        }
        else {
          minute = Math.abs(seconds/60);
        }
        delivery.setTimeLimit_hour(hour);
        delivery.setTimeLimit_minute(minute);
      }
    } catch (Exception e)
    {
      delivery.setTimeLimit("");
    }
  }

  public PublishedAssessmentFacade getPublishedAssessmentBasedOnAction(int action, DeliveryBean delivery){
    AssessmentService assessmentService = new AssessmentService();
    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    PublishedAssessmentFacade pub = null;
    String publishedId = cu.lookupParam("publishedId");
    String assessmentId = (String)cu.lookupParam("assessmentId");

    switch (action){
    case 2: // delivery.PREVIEW_ASSESSMENT
        // we would publish to create the publishedAssessment which we would use to populate
        // properties in delivery. However, for previewing, we do not need to keep this 
        // publishedAssessment record in DB at all, so we would delete it from DB right away.
        AssessmentFacade assessment = assessmentService.getAssessment(assessmentId);
        try {
          PublishedAssessmentFacade tempPub = publishedAssessmentService.publishPreviewAssessment(assessment);
          publishedId = tempPub.getPublishedAssessmentId().toString();
          // clone pub from tempPub, clone is not in anyway bound to the DB session
          pub = tempPub.clonePublishedAssessment();
          log.info("****publishedId="+publishedId);
          log.info("****clone publishedId="+pub.getPublishedAssessmentId());
          RemovePublishedAssessmentThread thread = new RemovePublishedAssessmentThread(publishedId);
          thread.start();
        } 
        catch (Exception e) {
          log.error(e);
          e.printStackTrace();
        }
        break;

    case 5: //delivery.TAKE_ASSESSMENT_VIA_URL:
        // this is accessed via publishedUrl so pubishedId==null
        pub = delivery.getPublishedAssessment();
        if (pub == null)
          throw new AbortProcessingException(
             "taking: publishedAsessmentId null or blank");
        else
          publishedId = pub.getPublishedAssessmentId().toString();
        break;

    case 1: //delivery.TAKE_ASSESSMENT
    case 3: //delivery.REVIEW_ASSESSMENT
        pub = lookupPublishedAssessment(publishedId, publishedAssessmentService);
        break;

    default: 
        break;
    }   
    return pub;
  }

}
