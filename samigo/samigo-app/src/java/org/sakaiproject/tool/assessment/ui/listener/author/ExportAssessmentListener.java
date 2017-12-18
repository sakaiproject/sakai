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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
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
@Slf4j
public class ExportAssessmentListener implements ActionListener
{
  public ExportAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
	  String assessmentId = (String) ContextUtil.lookupParam("assessmentId");
	  XMLDisplay xmlDisp = (XMLDisplay) ContextUtil.lookupBean("xml");
	  log.info("ExportAssessmentListener assessmentId="+assessmentId);
	  
	  AssessmentService assessmentService = new AssessmentService();
	  AssessmentFacade assessmentFacade = assessmentService.getAssessment(assessmentId);
	  
	  AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
	  if (!authzBean.isUserAllowedToEditAssessment(assessmentId, assessmentFacade.getCreatedBy(), false)) {
		  xmlDisp.setOutcome("exportDenied");
		  String thisIp = ( (javax.servlet.http.HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
		  // logging IP , as requested in SAK-17984
		  log.warn("Unauthorized attempt to access /samigo-app/jsf/qti/exportAssessment.xml?exportAssessmentId=" +  assessmentId + " from IP : " + thisIp);
		  return;
	  }


	  //update random question pools (if any) before exporting
	  int success = assessmentService.updateAllRandomPoolQuestions(assessmentFacade);
	  if(success == AssessmentService.UPDATE_SUCCESS){

		  XMLController xmlController = (XMLController) ContextUtil.lookupBean(
				  "xmlController");
		  xmlController.setId(assessmentId);
		  xmlController.setQtiVersion(1);
		  xmlController.displayAssessmentXml();
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

}
