package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class UpdateAssessmentTotalPointsListener implements ActionListener{

	public void processAction(ActionEvent ae) throws AbortProcessingException
	  {
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		assessmentBean.setQuestionSizeAndTotalScore();
	  }
}
