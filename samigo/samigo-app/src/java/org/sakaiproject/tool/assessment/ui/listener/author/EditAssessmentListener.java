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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class EditAssessmentListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(EditAssessmentListener.class);

  public EditAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    // #1a - come from authorIndex.jsp, load the assessment
    // goto editAssessment.jsp if successful
	AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    String editType = ContextUtil.lookupParam("editType");
    if (editType != null) {
    	if ("pendingAssessment".equals(editType)) {
    		setPropertiesForAssessment(author);
    	}
    	else if ("publishedAssessment".equals(editType)) {
    		setPropertiesForPublishedAssessment(author);
    	}
    	else {
        	log.debug("editType is not set - get from authorBean");
        	if (author.getIsEditPendingAssessmentFlow()) {
        		setPropertiesForAssessment(author);
        	}
        	else {
        		setPropertiesForPublishedAssessment(author);
        	}
        }
    }
    else {
    	log.debug("editType is null - get from authorBean");
    	if (author.getIsEditPendingAssessmentFlow()) {
    		setPropertiesForAssessment(author);
    	}
    	else {
    		setPropertiesForPublishedAssessment(author);
    	}
    }
  }

  public void setPropertiesForAssessment(AuthorBean author) {
	    FacesContext context = FacesContext.getCurrentInstance();
	    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
        "assessmentBean");
	    ItemAuthorBean itemauthorBean = (ItemAuthorBean) ContextUtil.lookupBean(
        "itemauthor");
		AssessmentService assessmentService = new AssessmentService();
		AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) ContextUtil
				.lookupBean("assessmentSettings");
	    String assessmentId = ContextUtil.lookupParam("assessmentId");
		if (assessmentId == null || assessmentId.equals("")) {
			assessmentId = assessmentSettings.getAssessmentId().toString();
		}
		AssessmentFacade assessment = assessmentService
				.getAssessment(assessmentId);

		// testing
		/*
		Set sectionSet = assessment.getSectionSet();
		Iterator iter_s = sectionSet.iterator();
		while (iter_s.hasNext()) {
			SectionDataIfc s = (SectionDataIfc) iter_s.next();
			Iterator iter = s.getItemSet().iterator();
			while (iter.hasNext()) {
				ItemDataIfc item = (ItemDataIfc) iter.next();
				List attachSet = item.getItemAttachmentList();
				Iterator iter_a = attachSet.iterator();
				while (iter_a.hasNext()) {
					ItemAttachmentIfc a = (ItemAttachmentIfc) iter_a.next();
				}
			}
		}
		*/
		// #1b - permission checking before proceeding - daisyf
		author.setOutcome("editAssessment");
		if (!passAuthz(context, assessment.getCreatedBy())) {
			author.setOutcome("author");
			return;
		}

		// pass authz, move on
		author.setIsEditPendingAssessmentFlow(true);
		assessmentSettings.setAssessment(assessment);
		assessmentBean.setAssessment(assessment);
		itemauthorBean.setTarget(ItemAuthorBean.FROM_ASSESSMENT); // save to
																	// assessment
		// initalize the itemtype
		itemauthorBean.setItemType("");
		itemauthorBean.setItemTypeString("");
		assessmentBean.setHasGradingData(false);
		
	    showPrintLink(assessmentBean);
	}
    
  public void setPropertiesForPublishedAssessment(AuthorBean author) {
	    FacesContext context = FacesContext.getCurrentInstance();
	    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
        "assessmentBean");
	    ItemAuthorBean itemauthorBean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
	    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		PublishedAssessmentSettingsBean publishedAssessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil
				.lookupBean("publishedSettings");
	    String publishedAssessmentId = assessmentBean.getAssessmentId();
		PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(publishedAssessmentId);
		
		// #1b - permission checking before proceeding - daisyf
		author.setOutcome("editAssessment");
		if (!passAuthz(context, publishedAssessment.getCreatedBy())) {
			author.setOutcome("author");
			return;
		}
		
		GradingService gradingService = new GradingService();
        boolean hasGradingData = gradingService.getHasGradingData(Long.valueOf(publishedAssessmentId));
        if (author.getEditPubAssessmentRestricted() && hasGradingData) {
                author.setOutcome("editPublishedAssessmentError");
                return;
        }
        assessmentBean.setHasGradingData(hasGradingData);
        
		// pass authz, move on
		author.setIsEditPendingAssessmentFlow(false);
		// Retract the published assessment for edit by updating the retract date to now
		//AssessmentAccessControlIfc publishedAccessControl = publishedAssessmentService.loadPublishedAccessControl(Long.valueOf(publishedAssessmentId));
		//publishedAccessControl.setRetractDate(new Date());
		//publishedAssessmentService.saveOrUpdatePublishedAccessControl(publishedAccessControl);
		//Update the retract date in the assessment bean
		//publishedAssessment.setAssessmentAccessControl(publishedAccessControl);
		publishedAssessment.setStatus(AssessmentBaseIfc.RETRACT_FOR_EDIT_STATUS);
		publishedAssessmentService.saveAssessment(publishedAssessment);
		publishedAssessmentSettings.setAssessment(publishedAssessment);
		assessmentBean.setAssessment(publishedAssessment);
		itemauthorBean.setTarget(ItemAuthorBean.FROM_ASSESSMENT); // save to assessment
		// initalize the itemtype
		itemauthorBean.setItemType("");
		itemauthorBean.setItemTypeString("");
  }
  
  public boolean passAuthz(FacesContext context, String ownerId){
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    boolean hasPrivilege_any = authzBean.getEditAnyAssessment();
    boolean hasPrivilege_own0 = authzBean.getEditOwnAssessment();
    boolean hasPrivilege_own = (hasPrivilege_own0 && isOwner(ownerId));
    boolean hasPrivilege = (hasPrivilege_any || hasPrivilege_own);
    if (!hasPrivilege){
       String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
						 "denied_edit_assessment_error");
       context.addMessage(null,new FacesMessage(err));
    }
    return hasPrivilege;
  }

  public boolean isOwner(String ownerId){
    boolean isOwner = false;
    String agentId = AgentFacade.getAgentString();
    isOwner = agentId.equals(ownerId);
    log.debug("***isOwner="+isOwner);
    return isOwner;
  }
  
  private void showPrintLink(AssessmentBean assessmentBean) {
	log.debug("first condition = " + (ToolManager.getTool("sakai.questionbank.printout") != null));
	log.debug("second conditon = " + !ServerConfigurationService.getString("stealthTools@org.sakaiproject.tool.api.ActiveToolManager").contains("sakai.questionbank.printout"));
	log.debug("third condition = " + !ServerConfigurationService.getString("hiddenTools@org.sakaiproject.tool.api.ActiveToolManager").contains("sakai.questionbank.printout"));
	if (ToolManager.getTool("sakai.questionbank.printout") != null
	      && !ServerConfigurationService.getString(
	         "stealthTools@org.sakaiproject.tool.api.ActiveToolManager")
	           .contains("sakai.questionbank.printout")
	       && !ServerConfigurationService.getString(
	         "hiddenTools@org.sakaiproject.tool.api.ActiveToolManager")
	          .contains("sakai.questionbank.printout")) {
		assessmentBean.setShowPrintLink(true);
	}
	else {
		assessmentBean.setShowPrintLink(false);
	}
  }
}
