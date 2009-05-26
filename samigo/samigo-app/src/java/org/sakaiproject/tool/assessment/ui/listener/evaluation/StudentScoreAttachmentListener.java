package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class StudentScoreAttachmentListener implements ActionListener {

	private static Log log = LogFactory.getLog(StudentScoreAttachmentListener.class);

	public void processAction(ActionEvent event) throws AbortProcessingException {
		StudentScoresBean studentScoresBean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");

		String itemGradingId = ContextUtil.lookupParam("itemGradingId");
		log.debug("itemGradingId = " + itemGradingId);
		studentScoresBean.setItemGradingIdForFilePicker(Long.valueOf(itemGradingId));
	}
}
