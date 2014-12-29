package org.sakaiproject.tool.assessment.ui.listener.samlite;

import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.ui.bean.samlite.SamLiteBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.w3c.dom.Document;

public class AssessmentListener implements ActionListener {
	private static Log log = LogFactory.getLog(AssessmentListener.class);
	
	public AssessmentListener() {}
	
	public void processAction(ActionEvent ae) {
		SamLiteBean samLiteBean = (SamLiteBean) ContextUtil.lookupBean("samLiteBean");
		
		Document doc = samLiteBean.createDocument();
 
		AssessmentFacade assessment = createImportedAssessment(doc, QTIVersion.VERSION_1_2, samLiteBean.getAssessmentTemplateId());
		
		String templateId = samLiteBean.getAssessmentTemplateId();
		if (null != templateId && !"".equals(templateId)) {
			try {
				assessment.setAssessmentTemplateId(Long.valueOf(templateId));
			} catch (NumberFormatException nfe) {
				// Don't worry about it.
				log.warn("Unable to set the assessment template id ", nfe);
			}
		}
		
		samLiteBean.createAssessment(assessment);
		samLiteBean.setData("");
		EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.create", "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId(), true));
	}
	
	public AssessmentFacade createImportedAssessment(Document document, int qti, String templateId) {
	    QTIService qtiService = new QTIService();
	    return qtiService.createImportedAssessment(document, qti, null, templateId);
	}

}
