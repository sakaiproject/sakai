package org.sakaiproject.tool.assessment.ui.listener.samlite;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.apachecommons.CommonsLog;

import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.samlite.SamLiteBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;

@CommonsLog
public class ParserListener implements ActionListener {
	
	public ParserListener() {}
	
	public void processAction(ActionEvent ae) {
		SamLiteBean samLiteBean = (SamLiteBean) ContextUtil.lookupBean("samLiteBean");
		
		FacesContext context = FacesContext.getCurrentInstance();
		AssessmentService assessmentService = new AssessmentService();
			    
		String assessmentTitle = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, samLiteBean.getName());
		
		samLiteBean.setOutcome("samLiteValidation");
		//check assessmentTitle and see if it is duplicated, if is not then proceed, else throw error
		if (assessmentTitle!=null && (assessmentTitle.trim()).equals("")){
			String err1=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_empty");
			context.addMessage(null,new FacesMessage(err1));
			samLiteBean.setOutcome("samLiteEntry");
			return;
		}
			    
		boolean isUnique = assessmentService.assessmentTitleIsUnique("0", assessmentTitle, false);
		if (!isUnique){
			String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","duplicateName_error");
			context.addMessage(null,new FacesMessage(err));
			samLiteBean.setOutcome("samLiteEntry");
			return;
		}
			
		samLiteBean.parse();
	}
	
}
