/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.qti.XMLController;
import org.sakaiproject.tool.assessment.ui.bean.qti.XMLDisplay;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ExportAssessmentListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ExportAssessmentListener.class);

  public ExportAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
	  String assessmentId = (String) ContextUtil.lookupParam("assessmentId");
	  XMLDisplay xmlDisp = (XMLDisplay) ContextUtil.lookupBean("xml");
	  log.info("ExportAssessmentListener assessmentId="+assessmentId);
	  if (!passAuthz(assessmentId)) {
		  xmlDisp.setOutcome("exportDenied");
		  String thisIp = ( (javax.servlet.http.HttpServletRequest) FacesContext.
				  getCurrentInstance().getExternalContext().getRequest()).
				  getRemoteAddr();
		  log.warn("Unauthorized attempt to access /samigo-app/jsf/qti/exportAssessment.xml?exportAssessmentId=" +  assessmentId + " from IP : " + thisIp);   // logging IP , as requested in SAK-17984
		  return;
	  }
	  //update random question pools (if any) before exporting
	  AssessmentService assessmentService = new AssessmentService();
	  int success = assessmentService.updateAllRandomPoolQuestions(assessmentService.getAssessment(assessmentId));
	  if(success == AssessmentService.UPDATE_SUCCESS){

		  XMLController xmlController = (XMLController) ContextUtil.lookupBean(
				  "xmlController");
		  //log.info("ExportAssessmentListener xmlController.setId(assessmentId)");
		  xmlController.setId(assessmentId);
		  // debug
		  // xmlController.setQtiVersion(2);
		  //log.info("xmlController.setQtiVersion(1)");
		  xmlController.setQtiVersion(1);
		  //log.info("ExportAssessmentListener xmlController.displayAssessmentXml");
		  xmlController.displayAssessmentXml();
		  //log.info("ExportAssessmentListener processAction done");
		  xmlDisp.setOutcome("xmlDisplay");
	  }else{
		  FacesContext context = FacesContext.getCurrentInstance();
		  if(success == AssessmentService.UPDATE_ERROR_DRAW_SIZE_TOO_LARGE){  		    		
			  String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_size_too_large");
			  context.addMessage(null,new FacesMessage(err));
		  }else{
			  String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_unknown");
			  context.addMessage(null,new FacesMessage(err));
		  }
		  xmlDisp.setOutcome("poolUpdateError");
		  return;
	  }

  }

  private boolean passAuthz(String assessmentId){
	  AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
	  boolean hasPrivilege_any = authzBean.getEditAnyAssessment();
	  boolean hasPrivilege_own0 = authzBean.getEditOwnAssessment();
	  boolean hasPrivilege_own = (hasPrivilege_own0 && isOwner(assessmentId));
	  boolean hasPrivilege = (hasPrivilege_any || hasPrivilege_own);
	  
	  return hasPrivilege;
  }

  private boolean isOwner(String assessmentId){
	  boolean isOwner = false;
	  String agentId = AgentFacade.getAgentString();
	  AssessmentService assessmentService = new AssessmentService();
	  String ownerId = assessmentService.getAssessmentCreatedBy(assessmentId);
	  isOwner = agentId.equals(ownerId);
	  log.debug("***isOwner="+isOwner);
	  return isOwner;
  }
}
