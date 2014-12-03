package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class QuestionPoolListener implements ActionListener {

	/**
	 * Standard process action method.
	 * 
	 * @param ae
	 *            ActionEvent
	 * @throws AbortProcessingException
	 */
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		author.setIsEditPendingAssessmentFlow(true);

		QuestionPoolBean qpoolbean = (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
		// If from "Question Pools" link, always get data from db to rebuild the tree 
		if (ae != null && ae.getComponent().getId().equals("questionPoolsLink")) {
			qpoolbean.buildTree();
		}
		//Reset the currentpool in root
		qpoolbean.setCurrentPool(null);
		qpoolbean.setQpDataModelByLevel();
	}
}
