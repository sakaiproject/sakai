package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class ResetPublishedAssessmentAttachmentListener implements ActionListener {
	private static Log log = LogFactory.getLog(ResetPublishedAssessmentAttachmentListener.class);

	public ResetPublishedAssessmentAttachmentListener() {
	}

	public void processAction(ActionEvent ae) throws AbortProcessingException {
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();
			PublishedAssessmentSettingsBean publishedAssessmentSettingsBean = (PublishedAssessmentSettingsBean) ContextUtil
					.lookupBean("publishedSettings");
		Long assessmentId = publishedAssessmentSettingsBean.getAssessmentId();
		log.debug("***assessmentId=" + assessmentId);
		ResetAssessmentAttachmentListener resetAssessmentAttachmentListener = new ResetAssessmentAttachmentListener();
		if (assessmentId != null && !("").equals(assessmentId)) {
			AssessmentIfc assessment = (AssessmentIfc) assessmentService.getAssessment(assessmentId);
			resetAssessmentAttachmentListener.resetAssessmentAttachment(assessment.getAssessmentAttachmentList(),
					assessmentService);
		} else {
			resetAssessmentAttachmentListener.resetAssessmentAttachment(new ArrayList(), assessmentService);
		}
	}

}
