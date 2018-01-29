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

import java.util.HashMap;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentMetaData;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.IndexBean;
import org.sakaiproject.tool.assessment.ui.bean.author.TemplateBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Description: Action Listener to edit a new or existing template</p>
 */
@Slf4j
public class EditTemplateListener
    extends TemplateBaseListener implements ActionListener
{
  /**
   * Standard processAction.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {

    TemplateBean templateBean = (TemplateBean)ContextUtil.lookupBean("template");
    templateBean.setOutcome("newTemplate");

    String tempName=templateBean.getNewName();
    AssessmentService assessmentService = new AssessmentService();
    //IndexBean templateIndex = (IndexBean) ContextUtil.lookupBean(                       "templateIndex");

    //ArrayList templates = new ArrayList();
    // id=0 => new template
    boolean isUnique=assessmentService.assessmentTitleIsUnique("0",tempName,true);
     FacesContext context = FacesContext.getCurrentInstance();
    if(tempName!=null && (tempName.trim()).equals("")){
     	String err1=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.TemplateMessages","templateName_empty");
	context.addMessage(null,new FacesMessage(err1));
        templateBean.setOutcome("template");
	return;
    }
    if (!isUnique){
      String error=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.TemplateMessages","duplicateName_error");
      context.addMessage(null,new FacesMessage(error));
      templateBean.setOutcome("template");
      return;
    }
    templateBean.setTemplateName(tempName);
    templateBean.setIdString("0"); //new template
    templateBean.setTypeId(null); //new template
    templateBean.setValueMap(getMetaDataMap());
    templateBean.setMarkForReview(Boolean.FALSE);

  }

  // meta data contains the list of "can edit" option and we want to set
  // them all to "true". This is the requirement for version 2.1.2, see SAK-3171.
  // - daisyf
  private HashMap getMetaDataMap(){
    HashMap h = new HashMap();
    AssessmentService service = new AssessmentService();
    Iterator iter = service.getDefaultMetaDataSet().iterator();
    while (iter.hasNext()){
      String label = (String) ((AssessmentMetaData)iter.next()).getLabel();
      if (("releaseTo").equals(label)){
	  h.put(label,"SITE_MEMBERS");
      }
      else if (("automaticSubmission_isInstructorEditable").equals(label)){
    	  IndexBean templateIndex = (IndexBean)ContextUtil.lookupBean("templateIndex");
    	  if (templateIndex.getAutomaticSubmissionEnabled()) {
    		  h.put(label, "true");
    	  }
    	  else {
    		  h.put(label, "false");
    	  }
      }
      else{
        h.put(label, "true");
      }
    }
    return h;
  }
}
