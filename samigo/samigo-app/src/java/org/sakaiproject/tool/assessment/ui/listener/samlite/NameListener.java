package org.sakaiproject.tool.assessment.ui.listener.samlite;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.samlite.SamLiteBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class NameListener implements ActionListener {
	
	
	public NameListener() {}
	
	public void processAction(ActionEvent ae) {
		FacesContext context = FacesContext.getCurrentInstance();
		
		SamLiteBean samLiteBean = (SamLiteBean) ContextUtil.lookupBean("samLiteBean");
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		
		author.setOutcome("samLiteEntry");
	    if (!passAuthz(context)){
	    	author.setOutcome("author");
	      	return;
	    }
	    
	    String assessmentTitle = ContextUtil.lookupParam("title");
	    
	    if (null == assessmentTitle || "".equals(assessmentTitle.trim())) {
	        String err1=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_empty");
	        context.addMessage(null,new FacesMessage(err1));
	        author.setOutcome("author");
	        return;
	    }
	    
	    samLiteBean.setName(assessmentTitle);
	    samLiteBean.setDescription(author.getAssessmentDescription());
	    
	    String templateId = ContextUtil.lookupParam("assessmentTemplate");

	    if (templateId == null){
	      templateId = AssessmentTemplateFacade.DEFAULTTEMPLATE.toString();
	    }
		
	    
	    samLiteBean.setAssessmentTemplateId(templateId);
	    
	}
	
	private boolean passAuthz(FacesContext context){
	    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean(
	                         "authorization");
	    boolean hasPrivilege = authzBean.getCreateAssessment();
	    if (!hasPrivilege){
	      String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
	                  "denied_create_assessment_error");
	      context.addMessage(null,new FacesMessage(err));
	    }
	    return hasPrivilege;
	}
		
}
