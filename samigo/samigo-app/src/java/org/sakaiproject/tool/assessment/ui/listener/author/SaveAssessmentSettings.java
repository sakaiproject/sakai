/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.SecuredIPAddress;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.authz.integrated.AuthzQueriesFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class SaveAssessmentSettings
{
  private static Log log = LogFactory.getLog(SaveAssessmentSettings.class);

  public AssessmentFacade save(AssessmentSettingsBean assessmentSettings)
  {
    // create an assessment based on the title entered and the assessment
    // template selected
    // #1 - set Assessment
    Long assessmentId = assessmentSettings.getAssessmentId();
    //log.info("**** save assessment assessmentId ="+assessmentId.toString());
    ItemAuthorBean iAuthor=new ItemAuthorBean();
    //System.out.println("assessmentSettings.getFeedbackAuthoring: "+assessmentSettings.getFeedbackAuthoring());
    iAuthor.setShowFeedbackAuthoring(assessmentSettings.getFeedbackAuthoring());
    //System.out.println("iAuthor.getShowFeedbackAuthoring :"+iAuthor.getShowFeedbackAuthoring());
    AssessmentService assessmentService = new AssessmentService();
    AssessmentFacade assessment = assessmentService.getAssessment(
        assessmentId.toString());
    //log.info("** assessment = "+assessment);
    assessment.setTitle(ContextUtil.processFormattedText(log, assessmentSettings.getTitle()));
    assessment.setDescription(assessmentSettings.getDescription()); // No need to call processFormattedText() because this is done in RichTextEditArea.java
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.AUTHORS, ContextUtil.processFormattedText(log, assessmentSettings.getAuthors()));

    // #2 - set AssessmentAccessControl
    AssessmentAccessControl control = (AssessmentAccessControl)assessment.getAssessmentAccessControl();
    if (control == null){
      control = new AssessmentAccessControl();
      // need to fix accessControl so it can take AssessmentFacade later
      control.setAssessmentBase(assessment.getData());
    }
    // a. LATER set dueDate, retractDate, startDate, releaseTo
    control.setStartDate(assessmentSettings.getStartDate());
    control.setDueDate(assessmentSettings.getDueDate());
    control.setRetractDate(assessmentSettings.getRetractDate());
    control.setFeedbackDate(assessmentSettings.getFeedbackDate());
    control.setReleaseTo(assessmentSettings.getReleaseTo());
    //log.info("control RELEASETO ="+control.getReleaseTo());
    //log.info("settings RELEASETO ="+assessmentSettings.getReleaseTo());

    // b. set Timed Assessment
    //log.info("** Time limit update to = "+assessmentSettings.getTimeLimit().intValue());
    control.setTimeLimit(assessmentSettings.getTimeLimit());
    if (assessmentSettings.getTimedAssessment())
      control.setTimedAssessment(AssessmentAccessControl.TIMED_ASSESSMENT);
    else
      control.setTimedAssessment(AssessmentAccessControl.DO_NOT_TIMED_ASSESSMENT);

    if (assessmentSettings.getAutoSubmit())
      control.setAutoSubmit(AssessmentAccessControl.AUTO_SUBMIT);
    else
      control.setAutoSubmit(AssessmentAccessControl.DO_NOT_AUTO_SUBMIT);

    // c. set Assessment Orgainzation
    if (assessmentSettings.getItemNavigation()!=null ) {
    	String nav = assessmentSettings.getItemNavigation();
    	if (nav.equals("1")) {
    		assessmentSettings.setAssessmentFormat("1");
    	}
    	control.setItemNavigation(new Integer(nav));
    }
    if (assessmentSettings.getItemNumbering()!=null)
      control.setItemNumbering(new Integer(assessmentSettings.getItemNumbering()));
    if (assessmentSettings.getAssessmentFormat()!=null )
     control.setAssessmentFormat(new Integer(assessmentSettings.getAssessmentFormat()));

    if (assessmentSettings.getIsMarkForReview())
        control.setMarkForReview(AssessmentAccessControl.MARK_FOR_REVIEW);
    else {
    	control.setMarkForReview(AssessmentAccessControl.NOT_MARK_FOR_REVIEW);
    }

    // d. set Submissions
    if (assessmentSettings.getUnlimitedSubmissions()!=null){
      if (!assessmentSettings.getUnlimitedSubmissions().
          equals(AssessmentAccessControlIfc.UNLIMITED_SUBMISSIONS.toString())) {
        control.setUnlimitedSubmissions(Boolean.FALSE);
        if (assessmentSettings.getSubmissionsAllowed() != null)
          control.setSubmissionsAllowed(new Integer(assessmentSettings.
              getSubmissionsAllowed()));
        else
          control.setSubmissionsAllowed(new Integer("1"));
      }
      else {
        control.setUnlimitedSubmissions(Boolean.TRUE);
        control.setSubmissionsAllowed(null);
      }
    }
    //log.info("**unlimited submission="+assessmentSettings.getUnlimitedSubmissions());
    //log.info("**allowed="+control.getSubmissionsAllowed());

    if (assessmentSettings.getLateHandling()!=null){
      control.setLateHandling(new Integer(assessmentSettings.
                                                getLateHandling()));
    }
    if (assessmentSettings.getSubmissionsSaved()!=null){
      control.setSubmissionsSaved(new Integer(assessmentSettings.getSubmissionsSaved()));
    }
    
    if (assessmentSettings.getAutoSubmit())
        control.setAutoSubmit(AssessmentAccessControl.AUTO_SUBMIT);
    else {
    	control.setAutoSubmit(AssessmentAccessControl.DO_NOT_AUTO_SUBMIT);
    }
    assessment.setAssessmentAccessControl(control);

    // e. set Submission Messages
    control.setSubmissionMessage(assessmentSettings.getSubmissionMessage());
    // f. set username
    control.setUsername(assessmentSettings.getUsername());
    // g. set password
    control.setPassword(assessmentSettings.getPassword());
    // h. set finalPageUrl
    String finalPageUrl = assessmentSettings.getFinalPageUrl();
    if (finalPageUrl != null) {
    	finalPageUrl = ContextUtil.processFormattedText(log, finalPageUrl.trim());
    	if (finalPageUrl.length() != 0 && !finalPageUrl.toLowerCase().startsWith("http")) {
    		finalPageUrl = "http://" + finalPageUrl;
    	}
    }
    control.setFinalPageUrl(finalPageUrl);

    //#3 Feedback
    AssessmentFeedback feedback = (AssessmentFeedback)assessment.getAssessmentFeedback();
    if (feedback == null){
      feedback = new AssessmentFeedback();
      // need to fix feeback so it can take AssessmentFacade later
      feedback.setAssessmentBase(assessment.getData());
    }
    if (assessmentSettings.getFeedbackDelivery()!=null)
     feedback.setFeedbackDelivery(new Integer(assessmentSettings.getFeedbackDelivery()));
    if (assessmentSettings.getFeedbackAuthoring()!=null)
     feedback.setFeedbackAuthoring(new Integer(assessmentSettings.getFeedbackAuthoring()));
    feedback.setShowQuestionText(Boolean.valueOf(assessmentSettings.getShowQuestionText()));
    feedback.setShowStudentResponse(Boolean.valueOf(assessmentSettings.getShowStudentResponse()));
    feedback.setShowCorrectResponse(Boolean.valueOf(assessmentSettings.getShowCorrectResponse()));
    feedback.setShowStudentScore(Boolean.valueOf(assessmentSettings.getShowStudentScore()));
    feedback.setShowStudentQuestionScore(Boolean.valueOf(assessmentSettings.getShowStudentQuestionScore()));
    feedback.setShowQuestionLevelFeedback(Boolean.valueOf(assessmentSettings.getShowQuestionLevelFeedback()));
    feedback.setShowSelectionLevelFeedback(Boolean.valueOf(assessmentSettings.getShowSelectionLevelFeedback()));
    feedback.setShowGraderComments(Boolean.valueOf(assessmentSettings.getShowGraderComments()));
    feedback.setShowStatistics(Boolean.valueOf(assessmentSettings.getShowStatistics()));
    assessment.setAssessmentFeedback(feedback);

    // g. set Grading
    EvaluationModel evaluation = (EvaluationModel) assessment.getEvaluationModel();
    if (evaluation == null){
      evaluation = new EvaluationModel();
      // need to fix evaluation so it can take AssessmentFacade later
      evaluation.setAssessmentBase(assessment.getData());
    }
    if (assessmentSettings.getAnonymousGrading()!=null)
      evaluation.setAnonymousGrading(new Integer(assessmentSettings.getAnonymousGrading()));
    
    // If there is value set for toDefaultGradebook, we reset it
    // Otherwise, do nothing
    if (assessmentSettings.getToDefaultGradebook() != null ) {
    	String firstTargetSelected = assessmentSettings.getFirstTargetSelected();
    	if (firstTargetSelected.equals("Anonymous Users")) {
    		evaluation.setToGradeBook("2");
    	}
    	else {
    		evaluation.setToGradeBook(assessmentSettings.getToDefaultGradebook());
    	}
    }
    
    if (assessmentSettings.getScoringType()!=null)
      evaluation.setScoringType(new Integer(assessmentSettings.getScoringType()));
    assessment.setEvaluationModel(evaluation);


    // h. update ValueMap: it contains value for teh checkboxes in
    // authorSettings.jsp for: hasAvailableDate, hasDueDate,
    // hasRetractDate, hasAnonymous, hasAuthenticatedUser, hasIpAddress,
    // hasUsernamePassword,
    // hasTimeAssessment,hasAutoSubmit, hasPartMetaData, hasQuestionMetaData
    HashMap h = assessmentSettings.getValueMap();
    updateMetaWithValueMap(assessment, h);

    // i. set Graphics
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.BGCOLOR, ContextUtil.processFormattedText(log, assessmentSettings.getBgColor()));
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.BGIMAGE, ContextUtil.processFormattedText(log, assessmentSettings.getBgImage()));

    // j. set objectives,rubrics,keywords
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.KEYWORDS, ContextUtil.processFormattedText(log, assessmentSettings.getKeywords()));
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.OBJECTIVES, ContextUtil.processFormattedText(log, assessmentSettings.getObjectives()));
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.RUBRICS, ContextUtil.processFormattedText(log, assessmentSettings.getRubrics()));

    // jj. save assessment first, then deal with ip
    assessmentService.saveAssessment(assessment);
    assessmentService.deleteAllSecuredIP(assessment);

    // k. set ipAddresses
   
    HashSet ipSet = new HashSet();
    String ipAddresses = assessmentSettings.getIpAddresses();
    if (ipAddresses == null)
      ipAddresses = "";
    
    String[] ip = ipAddresses.split("\\n");
    for (int j=0; j<ip.length;j++){
      if (ip[j]!=null && !ip[j].equals("\r")) {
    	  
        ipSet.add(new SecuredIPAddress(assessment.getData(),null,ip[j]));
      }
    }
    assessment.setSecuredIPAddressSet(ipSet);

    // l. FINALLY: save the assessment
    assessmentService.saveAssessment(assessment);

    // added by daisyf, 10/10/06
    updateAttachment(assessment.getAssessmentAttachmentList(), assessmentSettings.getAttachmentList(),(AssessmentIfc)assessment.getData(), true);
    EventTrackingService.post(EventTrackingService.newEvent("sam.setting.edit", "assessmentId=" + assessmentSettings.getAssessmentId(), true));
    
    //added by gopalrc, 6 Nov 2007
    AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();
    if (assessmentSettings.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
        authz.removeAuthorizationByQualifierAndFunction(assessmentId.toString(), "TAKE_ASSESSMENT");
    	String[] groupsAuthorized = assessmentSettings.getGroupsAuthorizedToSave();//getGroupsAuthorized();
    	if (groupsAuthorized != null && groupsAuthorized.length > 0) {
    		for (int i=0; i<groupsAuthorized.length; i++){
    			authz.createAuthorization(groupsAuthorized[i], "TAKE_ASSESSMENT", assessmentId.toString());
    		}
    	}
    }
    else { // releaseTo is not "Selected Groups" - clean up old/orphaned group permissions if necessary
    	Collection groups = null;
    	try {
    		Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
    		groups = site.getGroups();
    	}
		catch (IdUnusedException ex) {
			// No site available
		}
		if (groups != null && groups.size() > 0) {
			Iterator groupIter = groups.iterator();
			while (groupIter.hasNext()) {
				Group group = (Group) groupIter.next();
				//try {
					authz.removeAuthorizationByAgentQualifierAndFunction(group.getId(), assessmentId.toString(), "TAKE_ASSESSMENT");
				//}
				//catch (Exception ex) {
					// No authz permission data for the group
				//}
    		}
    	}
    }
    
    assessment = assessmentService.getAssessment(assessmentId.toString());
    return assessment;
  }


  public void updateMetaWithValueMap(AssessmentIfc assessment, HashMap map){
    //log.info("** map size ="+map.size());
    if (map!=null && map.keySet()!=null){
        Iterator iter = map.keySet().iterator();
        // loop through our valueMap "can edit" & "hasXXX" properties
        while (iter.hasNext()) {
          // get label from metadata set
          String label = (String) iter.next();
          String value="";
          if (map.get(label)!=null){
            value = (String) map.get(label).toString();
            //log.info("get Label: " + label + ", Value: " + value);
          }
          assessment.updateAssessmentMetaData(label, value);
        }
    }
  }

    public boolean isIpValid(String ipString){
      if (ipString.endsWith(".")) {
    	  return false;
      }
      String[] parts=ipString.split("\\.");
      int l=parts.length;
      if(l != 4)
    	  return false;
      for(int i=0;i<l;i++){
	  String s=parts[i];
          if(s.trim().equals(""))
	      return false;
	   
	   int index = 0;
	   while(index < s.length()){
	       char c = s.charAt(index);
	       if(!((Character.isDigit(c))||(Character.toString(c).equals("*")))){
		       return false;
	       }
	       index++;
	   }//end while
	   
	   // to filter out 1*3 or *6  
	   if (s.length() > 1) {
		   if (s.indexOf("*") != -1) {
			   return false;
		   }
	   }
	   
	   // if it is an number, it has to between 0 - 255
	   if (!s.equals("*")) {
		   int num = Integer.parseInt(s);	   
		   if ( num > 255 || num < 0) {
			   return false;
		   }
	   }
	}//end for	
      
	return true;
    }
  
  public void updateAttachment(List oldList, List newList, AssessmentIfc assessment, boolean isAuthorSettings){
    if ((oldList == null || oldList.size() == 0 ) && (newList == null || newList.size() == 0)) return;
    List list = new ArrayList();
    HashMap map = getAttachmentIdHash(oldList);
    for (int i=0; i<newList.size(); i++){
      AssessmentAttachmentIfc a = (AssessmentAttachmentIfc)newList.get(i);
      if (map.get(a.getAttachmentId())!=null){
        // exist already, remove it from map
        map.remove(a.getAttachmentId());
      }
      else{
        // new attachments
        a.setAssessment(assessment);
        list.add(a);
      }
    }      
    // save new ones
    AssessmentService assessmentService = null;
    if (isAuthorSettings) {
		assessmentService = new AssessmentService();
	}
	else {
		assessmentService = new PublishedAssessmentService();
	}
    assessmentService.saveOrUpdateAttachments(list);

    // remove old ones
    Set set = map.keySet();
    Iterator iter = set.iterator();
    while (iter.hasNext()){
      Long attachmentId = (Long)iter.next();
      assessmentService.removeAssessmentAttachment(attachmentId.toString());
    }
  }

  private HashMap getAttachmentIdHash(List list){
    HashMap map = new HashMap();
    for (int i=0; i<list.size(); i++){
      AssessmentAttachmentIfc a = (AssessmentAttachmentIfc)list.get(i);
      map.put(a.getAttachmentId(), a);
    }
    return map;
  }
}
