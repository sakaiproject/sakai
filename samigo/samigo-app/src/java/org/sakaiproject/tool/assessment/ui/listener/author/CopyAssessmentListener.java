package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class CopyAssessmentListener implements ActionListener {
	private static Logger log = LoggerFactory.getLogger(CopyAssessmentListener.class);

	public void processAction(ActionEvent ae) throws AbortProcessingException {
		log.debug("Enter processAction()");
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		String assessmentId = assessmentBean.getAssessmentId();
		log.debug("assessmentId = " + assessmentId);
		AssessmentService assessmentService = new AssessmentService();
		String apepndCopyTitle = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "append_copy_title");
		assessmentService.copyAssessment(assessmentId, apepndCopyTitle);
	}
}
