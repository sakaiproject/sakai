/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingSectionAwareServiceAPI;
import org.sakaiproject.tool.assessment.shared.impl.grading.GradingSectionAwareServiceImpl;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.TimeUtil;
import org.sakaiproject.util.FormattedText;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class AuthorActionListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(AuthorActionListener.class);
  private HashMap hm = new HashMap();
  private String display_dateFormat= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","output_data_picker_w_sec");
  private SimpleDateFormat displayFormat = new SimpleDateFormat(display_dateFormat);
  private TimeUtil tu = new TimeUtil();

  public AuthorActionListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    log.debug("*****Log: inside AuthorActionListener =debugging ActionEvent: " + ae);

    // get service and managed bean
    AssessmentService assessmentService = new AssessmentService();
    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    GradingService gradingService = new GradingService();
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean(
                       "author");
    author.setProtocol(ContextUtil.getProtocol());
    
    //#1 - prepare active template list. Note that we only need the title. We don't need the
    // full template object - be cheap.
    String showAssessmentTypes = ServerConfigurationService.getString("samigo.showAssessmentTypes");
    if ("false".equalsIgnoreCase(showAssessmentTypes)) {
    	author.setShowTemplateList(Boolean.FALSE);
    }
    else {
    	author.setShowTemplateList(Boolean.TRUE);
    }
    ArrayList templateList = assessmentService.getTitleOfAllActiveAssessmentTemplates();
    // get the managed bean, author and set the list
    if (templateList.size()==1){   //<= only contains Default Template
	author.setShowTemplateList(false);
    }
    else{
      // remove Default Template
      removeDefaultTemplate(templateList);
      author.setAssessmentTemplateList(templateList);
    }

    author.setAssessCreationMode("1");
    prepareAssessmentsList(author, assessmentService, gradingService, publishedAssessmentService);
    
    String s = ServerConfigurationService.getString("samigo.editPubAssessment.restricted");
	if (s != null && s.toLowerCase().equals("false")) {
		author.setEditPubAssessmentRestricted(false);
	}
	else {
		author.setEditPubAssessmentRestricted(true);
	}
	
	AuthorizationBean authorizationBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
	author.setIsGradeable(authorizationBean.getGradeAnyAssessment() || authorizationBean.getGradeOwnAssessment());
	author.setIsEditable(authorizationBean.getEditAnyAssessment() || authorizationBean.getEditOwnAssessment());
  }

  public void prepareAssessmentsList(AuthorBean author, AssessmentService assessmentService, GradingService gradingService, PublishedAssessmentService publishedAssessmentService) {
		// #2 - prepare core assessment list
		author.setCoreAssessmentOrderBy(AssessmentFacadeQueries.TITLE);
		ArrayList assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(
						AssessmentFacadeQueries.TITLE, author.isCoreAscending());
		Iterator iter = assessmentList.iterator();
		while (iter.hasNext()) {
			AssessmentFacade assessmentFacade= (AssessmentFacade) iter.next();
			assessmentFacade.setTitle(FormattedText.convertFormattedTextToPlaintext(assessmentFacade.getTitle()));
			try {
				String lastModifiedDateDisplay = tu.getDisplayDateTime(displayFormat, assessmentFacade.getLastModifiedDate());
				assessmentFacade.setLastModifiedDateForDisplay(lastModifiedDateDisplay);  
			}
			catch (Exception ex) {
				log.warn("Unable to format date: " + ex.getMessage());
			}
		}
		// get the managed bean, author and set the list
		author.setAssessments(assessmentList);

		ArrayList publishedAssessmentList = publishedAssessmentService.getBasicInfoOfAllPublishedAssessments2(
				  PublishedAssessmentFacadeQueries.TITLE, true, AgentFacade.getCurrentSiteId());
		prepareAllPublishedAssessmentsList(author, gradingService, publishedAssessmentList);
  }
  
  public void prepareAllPublishedAssessmentsList(AuthorBean author, GradingService gradingService, ArrayList publishedAssessmentList) {
	  try {
		  Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
		  Set siteStudentRoles = site.getRolesIsAllowed(SectionAwareness.STUDENT_MARKER);
		  ArrayList siteUsersIdList = new ArrayList();
		  if(siteStudentRoles != null && !siteStudentRoles.isEmpty()) {
			  for(Iterator iter = siteStudentRoles.iterator(); iter.hasNext();) {
				  String role = (String) iter.next();
				  siteUsersIdList.addAll(site.getUsersHasRole(role));
			  }
			  if (siteUsersIdList.size() != 0) {
				  String siteId = site.getId();
				  hm.put(siteId, siteUsersIdList);
			  }
		  }
		  
		  Collection groups = site.getGroups();
		  Iterator iter = groups.iterator();
		  
		  while (iter.hasNext()) {
			  Group group = (Group) iter.next();
			  Set groupStudentRoles = group.getRolesIsAllowed(SectionAwareness.STUDENT_MARKER);
			  ArrayList groupUsersIdList = new ArrayList();
			  if(groupStudentRoles != null && !groupStudentRoles.isEmpty()) {
				  for(Iterator iter2 = groupStudentRoles.iterator(); iter2.hasNext();) {
					  String role = (String) iter2.next();
					  groupUsersIdList.addAll(group.getUsersHasRole(role));
				  }
				  if (groupUsersIdList.size() != 0) {
					  String groupId = group.getId();
					  hm.put(groupId, groupUsersIdList);
				  }
			  }
		  }
	  }
	  catch (IdUnusedException e) {
		  log.warn("IdUnusedException: " + e.getMessage());
	  }
	  
	  ArrayList dividedPublishedAssessmentList = getTakeableList(publishedAssessmentList, gradingService);
	  
	  //prepareActivePublishedAssessmentsList(author, (ArrayList) dividedPublishedAssessmentList.get(0));
	  prepareRetractWarningText(author, (ArrayList) dividedPublishedAssessmentList.get(1)); 
	  author.setPublishedAssessments(publishedAssessmentList);
  }
  /*
  public void prepareActivePublishedAssessmentsList(AuthorBean author, ArrayList<PublishedAssessmentFacade> activePublishedList) {
	  author.setPublishedAssessments(activePublishedList);  
  }
  */
  public void prepareRetractWarningText(AuthorBean author, ArrayList inactivePublishedList) {	  
	  author.setInactivePublishedAssessments(inactivePublishedList);
	  boolean isAnyAssessmentRetractForEdit = false;
	  Iterator iter = inactivePublishedList.iterator();
	  while (iter.hasNext()) {
		  PublishedAssessmentFacade publishedAssessmentFacade = (PublishedAssessmentFacade) iter.next();
		  if (Integer.valueOf(3).equals(publishedAssessmentFacade.getStatus())) {
			  isAnyAssessmentRetractForEdit = true;
			  break;
		  }
	  }
	  if (isAnyAssessmentRetractForEdit) {
		  author.setIsAnyAssessmentRetractForEdit(true);
	  }
	  else {
		  author.setIsAnyAssessmentRetractForEdit(false);
	  }
  }
  
  private void removeDefaultTemplate(ArrayList templateList){
    for (int i=0; i<templateList.size();i++){
      AssessmentTemplateFacade a = (AssessmentTemplateFacade) templateList.get(i);
      if ((a.getAssessmentBaseId()).equals(new Long("1"))){
        templateList.remove(a);
        return;
      }
    }
  }

  public ArrayList getTakeableList(ArrayList assessmentList, GradingService gradingService) {
	  ArrayList list = new ArrayList();
	  ArrayList activeList = new ArrayList();
	  ArrayList inActiveList = new ArrayList();
	  String siteId = AgentFacade.getCurrentSiteId();
	  HashMap submissionCountHash = gradingService.getSiteSubmissionCountHash(siteId);
	  HashMap inProgressCountHash = gradingService.getSiteInProgressCountHash(siteId);
	  HashMap numberRetakeHash = gradingService.getSiteNumberRetakeHash(siteId);
	  HashMap actualNumberRetake = gradingService.getSiteActualNumberRetakeHash(siteId);
	  List needResubmitList = gradingService.getSiteNeedResubmitList(siteId);

	  for (int i = 0; i < assessmentList.size(); i++) {
		  PublishedAssessmentFacade f = (PublishedAssessmentFacade)assessmentList.get(i);
		  f.setTitle(FormattedText.convertFormattedTextToPlaintext(f.getTitle()));
		  Long publishedAssessmentId = f.getPublishedAssessmentId();
		  if (isActive(f, (HashMap) submissionCountHash.get(publishedAssessmentId), (HashMap) inProgressCountHash.get(publishedAssessmentId), 
				  (HashMap) numberRetakeHash.get(publishedAssessmentId), (HashMap) actualNumberRetake.get(publishedAssessmentId), needResubmitList)) {
			  f.setActiveStatus(true);
			  activeList.add(f);
		  }
		  else {
			  f.setActiveStatus(false);
			  inActiveList.add(f);
		  }
		  try {
				String lastModifiedDateDisplay = tu.getDisplayDateTime(displayFormat, f.getLastModifiedDate());
				f.setLastModifiedDateForDisplay(lastModifiedDateDisplay);  
			}
			catch (Exception ex) {
				log.warn("Unable to format date: " + ex.getMessage());
			}
	  }
	  list.add(activeList);
	  list.add(inActiveList);
	  return list;
  }

  public boolean isActive(PublishedAssessmentFacade f, HashMap submissionCountHash, HashMap inProgressCountHash, HashMap numberRetakeHash, 
		  HashMap actualNumberRetakeHash, List needResubmitList) {
	  boolean returnValue = false;
	  //1. prepare our significant parameters
	  Integer status = f.getStatus();
	  Date currentDate = new Date();
	  Date startDate = f.getStartDate();
	  Date retractDate = f.getRetractDate();
	  Date dueDate = f.getDueDate();
	  
	  boolean acceptLateSubmission = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(f.getLateHandling());
	  int maxSubmissionsAllowed = 9999;
	  if ((Boolean.FALSE).equals(f.getUnlimitedSubmissions())){
		  maxSubmissionsAllowed = f.getSubmissionsAllowed().intValue();
	  }
	  
	  ArrayList userIdList = new ArrayList();
	  if (f.getReleaseTo() != null && !("").equals(f.getReleaseTo())) {
		  if (f.getReleaseTo().indexOf("Anonymous Users") >= 0) {
			  if (submissionCountHash != null) {
				  f.setSubmittedCount(submissionCountHash.size());
			  }
			  else {
				  f.setSubmittedCount(0);
			  }
			  if (inProgressCountHash != null) {
				  f.setInProgressCount(inProgressCountHash.size());
			  }
			  else {
				  f.setInProgressCount(0);
			  }
			  if (dueDate != null && dueDate.before(currentDate)) {
				  return false;
			  }
			  else {
				  return true;
			  }
		  }
		  else {
			  if (AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS.equals(f.getReleaseTo())) {
				  PublishedAssessmentSettingsBean publishedAssessmentSettingsBean = (PublishedAssessmentSettingsBean) ContextUtil.lookupBean("publishedSettings");
				  publishedAssessmentSettingsBean.setAssessmentId(f.getPublishedAssessmentId());
				  String [] groupsAuthorized = publishedAssessmentSettingsBean.getGroupsAuthorized();
				  for (int i = 0; i < groupsAuthorized.length; i++) {
					  if (hm.get(groupsAuthorized[i]) != null) {
						  userIdList.addAll((ArrayList) hm.get(groupsAuthorized[i]));
					  }
				  }
			  }
			  else {
				  userIdList = (ArrayList) hm.get(AgentFacade.getCurrentSiteId());
			  }
			  
			  int submittedCounts = 0;
			  int inProgressCounts = 0;
			  if (userIdList != null) {
				  Iterator iter = userIdList.iterator();
				  String userId = null;
				  boolean isStillAvailable = false;
				  while(iter.hasNext()) {
					  userId = (String) iter.next();
					  int totalSubmitted = 0;
					  int totalInProgress = 0;
					  if (submissionCountHash != null && submissionCountHash.get(userId) != null){
						  totalSubmitted = ( (Integer) submissionCountHash.get(userId)).intValue();
						  if (totalSubmitted > 0) {
							  submittedCounts++;
						  }
					  }
					  if (inProgressCountHash != null && inProgressCountHash.get(userId) != null){
						  totalInProgress = ( (Integer) inProgressCountHash.get(userId)).intValue();
						  if (totalInProgress > 0) {
							  inProgressCounts++;
						  }
					  }

					  if (!returnValue) {
						  isStillAvailable = isStillAvailable(totalSubmitted, numberRetakeHash, actualNumberRetakeHash, 
								  userId, currentDate, dueDate, acceptLateSubmission, maxSubmissionsAllowed);
						  if (isStillAvailable) {
							  returnValue = true;
						  }
					  }
				  }
			  }
			  else {
				  returnValue = true;
			  }
			  f.setSubmittedCount(submittedCounts);
			  f.setInProgressCount(inProgressCounts);
			  if ((submittedCounts + inProgressCounts) > 0) {
				  f.setHasAssessmentGradingData(true);
			  }
			  else {
				  f.setHasAssessmentGradingData(false);
			  }
		  }

		  if (!Integer.valueOf(1).equals(status)) {
			  returnValue = false;
		  }

		  if (startDate != null && startDate.after(currentDate)) {
			  returnValue = false;
		  }

		  if (retractDate != null && retractDate.before(currentDate)) {
			  returnValue = false;
		  }

		  if (needResubmitList.contains(f.getPublishedAssessmentId())) {
			  returnValue = true;
		  }
	  }
	  else {
		  // should not come to here. but if this happens
		  returnValue = false;
	  }
	  return returnValue;
  }

  private boolean isStillAvailable(int totalSubmitted, HashMap numberRetakeHash, HashMap actualNumberRetakeHash,
		  String userId, Date currentDate, Date dueDate, 
		  boolean acceptLateSubmission, int maxSubmissionsAllowed) {
	  boolean isStillAvailable = false;
	  boolean hasSubmittedAtLeastOnce = false;

	  int numberRetake = 0;
	  if (numberRetakeHash != null && numberRetakeHash.get(userId) != null) {
		  numberRetake = ((Integer) numberRetakeHash.get(userId)).intValue();
	  }

	  //2. time to go through all the criteria
	  if (dueDate != null && dueDate.before(currentDate)) {
		  if (acceptLateSubmission) {
			  if (totalSubmitted == 0) {
				  return true;
			  }
		  }
		  int actualNumberRetake = 0;
		  if (actualNumberRetakeHash != null && actualNumberRetakeHash.get(userId) != null) {
			  actualNumberRetake = ((Integer) actualNumberRetakeHash.get(userId)).intValue();
		  }
		  if (actualNumberRetake < numberRetake) {
			  isStillAvailable = true;
		  }
	  }
	  else {
		  if (totalSubmitted < maxSubmissionsAllowed + numberRetake) {
			  isStillAvailable = true;
		  }
	  }
	  return isStillAvailable;
  }
}
