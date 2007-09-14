/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import java.util.List;
import java.util.Set;
import java.util.Iterator;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
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
    FacesContext context = FacesContext.getCurrentInstance();

    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
                                          "assessmentBean");
   

    ItemAuthorBean itemauthorBean = (ItemAuthorBean) ContextUtil.lookupBean(
                                          "itemauthor");

    // #1a - come from authorIndex.jsp, load the assessment
    // goto editAssessment.jsp if successful
    String assessmentId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("assessmentId");
    AssessmentService assessmentService = new AssessmentService();
    AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) ContextUtil.
	    lookupBean("assessmentSettings");

    if (assessmentId == null){
      assessmentId = assessmentSettings.getAssessmentId().toString();
    }
    AssessmentFacade assessment = assessmentService.getAssessment(
        assessmentId);
    assessmentSettings.setAssessment(assessment);

    // testing
    Set sectionSet = assessment.getSectionSet();
    Iterator iter_s = sectionSet.iterator();
    while (iter_s.hasNext()){
      SectionDataIfc s = (SectionDataIfc) iter_s.next();
      Iterator iter = s.getItemSet().iterator();
      while (iter.hasNext()){
        ItemDataIfc item = (ItemDataIfc)iter.next();
        List attachSet = item.getItemAttachmentList();
        Iterator iter_a = attachSet.iterator();
        while (iter_a.hasNext()){
          ItemAttachmentIfc a = (ItemAttachmentIfc) iter_a.next();
	}
      }
    } 

    //#1b - permission checking before proceeding - daisyf
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    author.setOutcome("editAssessment");
    if (!passAuthz(context, assessment.getCreatedBy())){
      author.setOutcome("author");
      return;
    }

    // pass authz, move on
    assessmentBean.setAssessment(assessment);
    itemauthorBean.setTarget(ItemAuthorBean.FROM_ASSESSMENT); // save to assessment
    // initalize the itemtype
    itemauthorBean.setItemType("");
    itemauthorBean.setItemTypeString("");
    
    showPrintLink(assessmentBean);
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
