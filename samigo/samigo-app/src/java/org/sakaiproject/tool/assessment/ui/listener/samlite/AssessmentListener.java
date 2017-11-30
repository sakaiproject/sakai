/**
 * Copyright (c) 2005-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.ui.listener.samlite;

import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.ui.bean.samlite.SamLiteBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

@Slf4j
public class AssessmentListener implements ActionListener {

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
		EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_CREATE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId(), true));
	}
	
	public AssessmentFacade createImportedAssessment(Document document, int qti, String templateId) {
	    QTIService qtiService = new QTIService();
	    return qtiService.createImportedAssessment(document, qti, null, templateId);
	}

}
