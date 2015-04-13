package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class ConfirmEditPublishedAssessmentListener  implements ActionListener {
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		FacesContext context = FacesContext.getCurrentInstance();

		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		author.setIsEditPendingAssessmentFlow(false);
		String publishedAssessmentId = ContextUtil.lookupParam("publishedAssessmentId");
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");

		PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		PublishedAssessmentData publishedAssessmentData = publishedAssessmentService.getBasicInfoOfPublishedAssessment(publishedAssessmentId);

		AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
		if (!authzBean.isUserAllowedToEditAssessment(publishedAssessmentId, publishedAssessmentData.getCreatedBy(), true)) {
			String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_edit_assessment_error");
			context.addMessage(null,new FacesMessage(err));
			author.setOutcome("author");
			return;
		}

		assessmentBean.setAssessmentId(publishedAssessmentId);
		EventTrackingService.post(EventTrackingService.newEvent("sam.pubassessment.confirm_edit", "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + publishedAssessmentId, true));
	}
}
