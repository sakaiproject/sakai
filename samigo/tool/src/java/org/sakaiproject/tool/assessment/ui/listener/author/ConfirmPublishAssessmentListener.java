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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.Date;
import java.util.Map;


import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ConfirmPublishAssessmentListener
    implements ActionListener {

  private static Log log = LogFactory.getLog(ConfirmPublishAssessmentListener.class);
  private static ContextUtil cu;

  public ConfirmPublishAssessmentListener() {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext extContext = context.getExternalContext();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    // ResourceBundle rb=ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", context.getViewRoot().getLocale());
    AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) cu.
        lookupBean(
        "assessmentSettings");
   //Huong's adding
    SaveAssessmentSettings s = new SaveAssessmentSettings();
    Object time=assessmentSettings.getValueMap().get("hasTimeAssessment");
    boolean isTime=false;
    String err="";
    String assessmentName=assessmentSettings.getTitle();
    if (time!=null)
      isTime=((Boolean)time).booleanValue();

    if((!((isTime)&&((assessmentSettings.getTimeLimit().intValue())==0)))&&(s.notEmptyAndNotDub(assessmentName,false))){
        System.out.println("SAVESETTINGSANDCONFIRM Success");
	assessmentSettings.setOutcomePublish("publish_success");	
	AssessmentFacade assessment = s.save(assessmentSettings);
	// System.out.println("ASSESSMENT SETTINGSAVED");
	assessmentSettings.setAssessment(assessment);

    //  we need a publishedUrl, this is the url used by anonymous user
	String releaseTo = assessment.getAssessmentAccessControl().getReleaseTo();
	if (releaseTo != null) {
           // generate an alias to the pub assessment
	    String alias = AgentFacade.getAgentString() + (new Date()).getTime();
	    assessmentSettings.setAlias(alias);
           //log.info("servletPath=" + extContext.getRequestServletPath());
            String server = ( (javax.servlet.http.HttpServletRequest) extContext.
			getRequest()).getRequestURL().toString();
	    int index = server.indexOf(extContext.getRequestContextPath() + "/"); // "/samigo/"
	    server = server.substring(0, index);
      //log.info("servletPath=" + server);
	    String url = server + extContext.getRequestContextPath();
	    assessmentSettings.setPublishedUrl(url + "/servlet/Login?id=" + alias);

	}

	//## - permission checking before proceeding - daisyf
	if (!passAuthz(context, assessment.getCreatedBy())){
	    assessmentSettings.setOutcomePublish("editAssessmentSettings");
	}
       
    }
    else{
       
        if(!s.notEmptyAndNotDub(assessmentName,false)){
	    //  err=(String)rb.getObject("emptyAssessment_error");
           err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_error");
	   
	}
	else{//error in time select
            err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","timeSelect_error");
	}
	context.addMessage(null,new FacesMessage(err));
	assessmentSettings.setOutcomePublish("publish_fail");
	    
    }

  }

    public boolean passAuthz(FacesContext context, String ownerId){
	AuthorizationBean authzBean = (AuthorizationBean) cu.lookupBean("authorization");
	boolean hasPrivilege_any = authzBean.getPublishAnyAssessment();
	boolean hasPrivilege_own0 = authzBean.getPublishOwnAssessment();
	boolean hasPrivilege_own = (hasPrivilege_own0 && isOwner(ownerId));
	boolean hasPrivilege = (hasPrivilege_any || hasPrivilege_own);
	if (!hasPrivilege){
	    String err=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
						     "denied_publish_assessment_error");
	    context.addMessage("authorIndexForm:publish_assessment_denied",new FacesMessage(err));
	}
	return hasPrivilege;
    }

    public boolean isOwner(String ownerId){
	boolean isOwner = false;
	String agentId = AgentFacade.getAgentString();
	isOwner = agentId.equals(ownerId);
	return isOwner;
    }
  
}
