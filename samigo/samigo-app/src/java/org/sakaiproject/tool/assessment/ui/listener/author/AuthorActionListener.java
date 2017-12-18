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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
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
@Slf4j
public class AuthorActionListener
    implements ActionListener
{
  private HashMap<String, ArrayList<String>> groupUsersIdMap = new HashMap<String, ArrayList<String>>();
  private ArrayList<String> siteUsersIdList = new ArrayList<String>();
  private final TimeUtil tu = new TimeUtil();

  // UVa, per SAK-2438 
  private ResourceProperties siteProperties = null;
  
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
    boolean showAssessmentTypes = ServerConfigurationService.getBoolean("samigo.showAssessmentTypes", false);
    author.setShowTemplateList(showAssessmentTypes);

    List templateList = assessmentService.getTitleOfAllActiveAssessmentTemplates();
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
   
    // UVa: per SAK-2438, add a check for the site property 'samigo.editPubAssessment.restricted'.
    //      If this site property exists (Admin user adds it per site), obey it.
    //      Otherwise, use the global sakai-wide property by the same name. 
    //

    // First, get the site object
    Site site = null;
    try {
	site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
    }
    catch (IdUnusedException ex) {
		log.warn(ex.getMessage());
    }

    // Does a site property 'samigo.editPubAssessment.restricted' exist?
    boolean sitePropertyExists = false;
    if (site != null) {
        try {
            // get the site properties
            siteProperties = site.getProperties();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
            
        if (siteProperties != null) {
            // get this property for this site
            String prop = siteProperties.getProperty("samigo.editPubAssessment.restricted");
            if (prop != null) {
                sitePropertyExists = true;

                if (prop.toLowerCase().equals("false")) {
                    // published assessment editting is not restricted
                    author.setEditPubAssessmentRestricted(false);
                } else {
                    // published assessment editting is restricted
                    author.setEditPubAssessmentRestricted(true);
                }
            } else {
                sitePropertyExists = false;
            }
        }
    }

    // If a site property does not exist, go ahead and evaluate the global property
    if (!sitePropertyExists) {

        String s = ServerConfigurationService.getString("samigo.editPubAssessment.restricted");
	if (s != null && s.toLowerCase().equals("false")) {
		author.setEditPubAssessmentRestricted(false);
	}
	else {
		author.setEditPubAssessmentRestricted(true);
	}
    } 

	author.setEditPubAssessmentRestrictedAfterStarted(ServerConfigurationService.getBoolean("samigo.editPubAssessment.restricted.afterStart", false));
	author.setRemovePubAssessmentsRestrictedAfterStarted(ServerConfigurationService.getBoolean("samigo.removePubAssessment.restricted.afterStart", false));

	AuthorizationBean authorizationBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
	author.setIsGradeable(authorizationBean.getGradeAnyAssessment() || authorizationBean.getGradeOwnAssessment());
	author.setIsEditable(authorizationBean.getEditAnyAssessment() || authorizationBean.getEditOwnAssessment());
  }

  public void prepareAssessmentsList(AuthorBean author, AssessmentService assessmentService, GradingService gradingService, PublishedAssessmentService publishedAssessmentService) {
		// #2 - prepare core assessment list
		author.setCoreAssessmentOrderBy(AssessmentFacadeQueries.TITLE);
		List assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(
						AssessmentFacadeQueries.TITLE, author.isCoreAscending());
		Iterator iter = assessmentList.iterator();
		while (iter.hasNext()) {
			AssessmentFacade assessmentFacade= (AssessmentFacade) iter.next();
			assessmentFacade.setTitle(FormattedText.convertFormattedTextToPlaintext(assessmentFacade.getTitle()));
			try {
				String lastModifiedDateDisplay = tu.getIsoDateWithLocalTime(assessmentFacade.getLastModifiedDate());
				assessmentFacade.setLastModifiedDateForDisplay(lastModifiedDateDisplay);  
			}
			catch (Exception ex) {
				log.warn("Unable to format date: " + ex.getMessage());
			}
		}
		// get the managed bean, author and set the list
		author.setAssessments(assessmentList);

		List publishedAssessmentList = publishedAssessmentService.getBasicInfoOfAllPublishedAssessments2(
				  PublishedAssessmentFacadeQueries.TITLE, true, AgentFacade.getCurrentSiteId());
		prepareAllPublishedAssessmentsList(author, gradingService, publishedAssessmentList);
  }
  
  public void prepareAllPublishedAssessmentsList(AuthorBean author, GradingService gradingService, List publishedAssessmentList) {
	  try {
		  Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
		  Set siteStudentRoles = site.getRolesIsAllowed(SectionAwareness.STUDENT_MARKER);
		  if(siteStudentRoles != null && !siteStudentRoles.isEmpty()) {
			  for(Iterator iter = siteStudentRoles.iterator(); iter.hasNext();) {
				  String role = (String) iter.next();
				  siteUsersIdList.addAll(site.getUsersHasRole(role));
			  }
		  }
		  
		  Collection groups = site.getGroups();
		  Iterator iter = groups.iterator();
		  
		  while (iter.hasNext()) {
			  Group group = (Group) iter.next();
			  Set groupStudentRoles = group.getRolesIsAllowed(SectionAwareness.STUDENT_MARKER);
			  ArrayList<String> groupUsersIdList = new ArrayList<String>();
			  if(groupStudentRoles != null && !groupStudentRoles.isEmpty()) {
				  for(Iterator iter2 = groupStudentRoles.iterator(); iter2.hasNext();) {
					  String role = (String) iter2.next();
					  groupUsersIdList.addAll(group.getUsersHasRole(role));
				  }
				  if (!groupUsersIdList.isEmpty()) {
					  String groupId = group.getId();
					  groupUsersIdMap.put(groupId, groupUsersIdList);
				  }
			  }
		  }
	  }
	  catch (IdUnusedException e) {
		  log.warn("IdUnusedException: " + e.getMessage());
	  }
	  
	  List dividedPublishedAssessmentList = getTakeableList(publishedAssessmentList, gradingService);
	  
	  prepareRetractWarningText(author, (List) dividedPublishedAssessmentList.get(1));
	  author.setPublishedAssessments(publishedAssessmentList);
  }

  public void prepareRetractWarningText(AuthorBean author, List inactivePublishedList) {
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
  
  private void removeDefaultTemplate(List templateList){
    for (int i=0; i<templateList.size();i++){
      AssessmentTemplateFacade a = (AssessmentTemplateFacade) templateList.get(i);
      if ((a.getAssessmentBaseId()).equals(new Long("1"))){
        templateList.remove(a);
        return;
      }
    }
  }

  public List getTakeableList(List assessmentList, GradingService gradingService) {
	  List list = new ArrayList();
	  List activeList = new ArrayList();
	  List inActiveList = new ArrayList();
	  String siteId = AgentFacade.getCurrentSiteId();
	  Map<Long, Map<String, Integer>> submissionCountHash = gradingService.getSiteSubmissionCountHash(siteId);
	  Map<Long, Map<String, Long>> inProgressCountHash = gradingService.getSiteInProgressCountHash(siteId);
	  Map<Long, Map<String, Integer>> numberRetakeHash = gradingService.getSiteNumberRetakeHash(siteId);
	  Map<Long, Map<String, Long>> actualNumberRetake = gradingService.getSiteActualNumberRetakeHash(siteId);
	  List needResubmitList = gradingService.getSiteNeedResubmitList(siteId);

	  for( Object assessmentList1 : assessmentList ) {
		  PublishedAssessmentFacade f = (PublishedAssessmentFacade) assessmentList1;
		  f.setTitle(FormattedText.convertFormattedTextToPlaintext(f.getTitle()));
		  Long publishedAssessmentId = f.getPublishedAssessmentId();
		  if (isActive(f, 
				  (Map<String, Integer>) submissionCountHash.get(publishedAssessmentId), 
				  (Map<String, Long>) inProgressCountHash.get(publishedAssessmentId),
				  (Map<String, Integer>) numberRetakeHash.get(publishedAssessmentId),
				  (Map<String, Long>) actualNumberRetake.get(publishedAssessmentId),
				  needResubmitList)) {
			  f.setActiveStatus(true);
			  activeList.add(f);
		  }
		  else {
			  f.setActiveStatus(false);
			  inActiveList.add(f);
		  }
		  try {
			  String lastModifiedDateDisplay = tu.getIsoDateWithLocalTime(f.getLastModifiedDate());
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

  public boolean isActive(PublishedAssessmentFacade f, Map<String, Integer> submissionCountHash, Map<String, Long> inProgressCountHash, Map<String, Integer> numberRetakeHash,
		  Map<String, Long> actualNumberRetakeHash, List needResubmitList) {
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
		  maxSubmissionsAllowed = f.getSubmissionsAllowed();
	  }
	  
	  ArrayList<String> userIdList = new ArrayList<String>();
	  if (f.getReleaseTo() != null && !("").equals(f.getReleaseTo())) {
		  if (f.getReleaseTo().contains( "Anonymous Users" )) {
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
				  //Use a set to avoid duplicated entries in the userList
				  HashSet<String> uuser = new HashSet<String>();                
				  if(groupsAuthorized != null && groupsAuthorized.length > 0) {
					  for( String groupsAuthorized1 : groupsAuthorized ) {
						  if( groupUsersIdMap.get( groupsAuthorized1 ) != null )
						  {
							  for( String userId : groupUsersIdMap.get( groupsAuthorized1 ) )
							  {
								  uuser.add(userId);
							  }
						  }
					  }
				  userIdList = new ArrayList<String>(uuser);
			  }
			  }
			  else {
				  userIdList = siteUsersIdList;
			  }
			  
			  int submittedCounts = 0;
			  int inProgressCounts = 0;
			  if (userIdList != null) {
				  Iterator<String> iter = userIdList.iterator();
				  String userId;
				  boolean isStillAvailable;
				  while(iter.hasNext()) {
					  userId = (String) iter.next();
					  int totalSubmitted = 0;
					  int totalInProgress;
					  if (submissionCountHash != null && submissionCountHash.get(userId) != null){
						  totalSubmitted = submissionCountHash.get(userId);
						  if (totalSubmitted > 0) {
							  submittedCounts++;
						  }
					  }
					  if (inProgressCountHash != null && inProgressCountHash.get(userId) != null){
						  totalInProgress = inProgressCountHash.get(userId).intValue();
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

		  if (acceptLateSubmission
				  && (dueDate != null && dueDate.before(currentDate))
				  && (retractDate == null || retractDate.before(currentDate))) {
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

  private boolean isStillAvailable(int totalSubmitted, Map<String, Integer> numberRetakeHash, Map<String, Long> actualNumberRetakeHash,
		  String userId, Date currentDate, Date dueDate, 
		  boolean acceptLateSubmission, int maxSubmissionsAllowed) {
	  boolean isStillAvailable = false;

	  int numberRetake = 0;
	  if (numberRetakeHash != null && numberRetakeHash.get(userId) != null) {
		  numberRetake = ((Integer) numberRetakeHash.get(userId));
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
			  actualNumberRetake = actualNumberRetakeHash.get(userId).intValue();
		  }
		  if (actualNumberRetake < numberRetake && acceptLateSubmission) {
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
