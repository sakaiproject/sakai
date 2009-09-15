package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;

public class ConfirmCopyAssessmentListener implements ActionListener {
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		FacesContext context = FacesContext.getCurrentInstance();

		// #1 - read the assessmentId from the form
		String assessmentId = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("assessmentId");

		// #2 -  and use it to set author bean, goto removeAssessment.jsp
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		AssessmentService assessmentService = new AssessmentService();
		AssessmentFacade assessment = assessmentService.getBasicInfoOfAnAssessment(assessmentId);

		// #3 - permission checking before proceeding - daisyf
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		author.setOutcome("confirmCopyAssessment");
		EditAssessmentListener editAssessmentListener = new EditAssessmentListener(); 
		if (!editAssessmentListener.passAuthz(context, assessment.getCreatedBy())) {
			author.setOutcome("author");
			return;
		}

		assessmentBean.setAssessmentId(assessment.getAssessmentBaseId().toString());
		assessmentBean.setTitle(FormattedText.unEscapeHtml(assessment.getTitle()));
	}
}
