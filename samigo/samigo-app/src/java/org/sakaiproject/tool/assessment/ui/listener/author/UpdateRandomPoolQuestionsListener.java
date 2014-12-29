package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class UpdateRandomPoolQuestionsListener  implements ActionListener{
	public void processAction(ActionEvent arg0) throws AbortProcessingException {
		String sectionId = (String) FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterMap().get("assesssmentForm:randomQuestionsSectionId");

		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		int success = assessmentBean.updateRandomPoolQuestions(sectionId);
		if(success != AssessmentService.UPDATE_SUCCESS){
			FacesContext context = FacesContext.getCurrentInstance();
			if(success == AssessmentService.UPDATE_ERROR_DRAW_SIZE_TOO_LARGE){  		    		
				String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_size_too_large");
				context.addMessage(null,new FacesMessage(err));
			}else{
				String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_unknown");
				context.addMessage(null,new FacesMessage(err));
			}
		}
	}

}
