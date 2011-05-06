package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class ConfirmEditPublishedAssessmentListener  implements ActionListener {
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		author.setIsEditPendingAssessmentFlow(false);
		String publishedAssessmentId = ContextUtil.lookupParam("publishedAssessmentId");
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
        	"assessmentBean");
		assessmentBean.setAssessmentId(publishedAssessmentId);
		EventTrackingService.post(EventTrackingService.newEvent("sam.pubassessment.confirm_edit", "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + publishedAssessmentId, true));
	}
}
