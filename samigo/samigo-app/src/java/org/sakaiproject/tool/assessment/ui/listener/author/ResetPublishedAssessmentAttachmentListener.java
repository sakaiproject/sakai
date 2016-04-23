package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class ResetPublishedAssessmentAttachmentListener implements ActionListener {
	private static Logger log = LoggerFactory.getLogger(ResetPublishedAssessmentAttachmentListener.class);

	public ResetPublishedAssessmentAttachmentListener() {
	}

	public void processAction(ActionEvent ae) throws AbortProcessingException {
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();
			PublishedAssessmentSettingsBean publishedAssessmentSettingsBean = (PublishedAssessmentSettingsBean) ContextUtil
					.lookupBean("publishedSettings");
		Long assessmentId = publishedAssessmentSettingsBean.getAssessmentId();
		log.debug("***assessmentId=" + assessmentId);
		ResetAssessmentAttachmentListener resetAssessmentAttachmentListener = new ResetAssessmentAttachmentListener();
		if (assessmentId != null) {
			AssessmentIfc assessment = (AssessmentIfc) assessmentService.getAssessment(assessmentId);
			resetAssessmentAttachmentListener.resetAssessmentAttachment(assessment.getAssessmentAttachmentList(),
					assessmentService);
		} else {
			resetAssessmentAttachmentListener.resetAssessmentAttachment(new ArrayList(), assessmentService);
		}
	}

}
