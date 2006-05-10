/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.util.Iterator;
//import java.util.ResourceBundle;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentMetaData;
import org.sakaiproject.tool.assessment.ui.bean.author.TemplateBean;
import org.sakaiproject.tool.assessment.ui.bean.author.IndexBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;

/**
 * <p>Description: Action Listener to edit a new or existing template</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class EditTemplateListener
    extends TemplateBaseListener implements ActionListener
{
  private static Log log = LogFactory.getLog(EditTemplateListener.class);
  /**
   * Standard processAction.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    //log.info("EDIT TEMPLATE LISTENER.");

    TemplateBean templateBean = (TemplateBean)ContextUtil.lookupBean("template");
    templateBean.setOutcome("newTemplate");

    //log.info("Editing new template.");
    String tempName=templateBean.getNewName();
    AssessmentService assessmentService = new AssessmentService();
    IndexBean templateIndex = (IndexBean) ContextUtil.lookupBean(
                       "templateIndex");

    ArrayList templates = new ArrayList();
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
    templateBean.setValueMap(getMetaDataMap());
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
      String value ="true";
      h.put(label, "true");
    }
    return h;
  }
}
