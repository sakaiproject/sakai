/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedSectionFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id$
 */

public class ReorderPartsListener implements ValueChangeListener {
	private static Log log = LogFactory.getLog(ReorderPartsListener.class);

	/**
	 * Standard process action method.
	 * @param ae ValueChangeEvent
	 * @throws AbortProcessingException
	 */
	public void processValueChange(ValueChangeEvent ae)
			throws AbortProcessingException {
		//log.info("ReorderQuestionsListener valueChangeLISTENER.");
		//SectionContentsBean partBean  = (SectionContentsBean) ContextUtil.lookupBean("partBean");
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		boolean isEditPendingAssessmentFlow = author
				.getIsEditPendingAssessmentFlow();
		log.debug("**** isEditPendingAssessmentFlow : " + isEditPendingAssessmentFlow);
		String oldPos = ae.getOldValue().toString();
		log.debug("**** ae.getOldValue : " + oldPos);
		String newPos = ae.getNewValue().toString();
		log.debug("**** ae.getNewValue : " + newPos);

		// get sections with oldPos
		if (isEditPendingAssessmentFlow) {
			setPropertiesForAssessment(newPos, oldPos);
		} else {
			setPropertiesForPublishedAssessment(newPos, oldPos);
		}
	}

	private void setPropertiesForAssessment(String newPos, String oldPos) {
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");
		AssessmentFacade assessment = (AssessmentFacade) assessmentBean
				.getAssessment();
		SectionFacade section1 = (SectionFacade) assessment
				.getSection(new Long(oldPos));
		SectionFacade section2 = (SectionFacade) assessment
				.getSection(new Long(newPos));
		if (section1 != null && section2 != null) {
			section1.setSequence(new Integer(newPos));
			section2.setSequence(new Integer(oldPos));
			AssessmentService service = new AssessmentService();
			service.saveOrUpdateSection(section1);
			service.saveOrUpdateSection(section2);
			service.updateAssessmentLastModifiedInfo(assessment);
		}

		// goto editAssessment.jsp, so reset assessmentBean
		assessmentBean.setAssessment(assessment);
	}

	private void setPropertiesForPublishedAssessment(String newPos, String oldPos) {
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");
		PublishedAssessmentFacade publishedAssessment = (PublishedAssessmentFacade) assessmentBean
				.getAssessment();
		PublishedSectionData section1 = (PublishedSectionData) publishedAssessment
				.getSection(new Long(oldPos));
		PublishedSectionData section2 = (PublishedSectionData) publishedAssessment
				.getSection(new Long(newPos));
		if (section1 != null && section2 != null) {
			section1.setSequence(new Integer(newPos));
			section2.setSequence(new Integer(oldPos));
			PublishedAssessmentService service = new PublishedAssessmentService();
			service.saveOrUpdateSection(new PublishedSectionFacade(section1));
			service.saveOrUpdateSection(new PublishedSectionFacade(section2));
			service.updateAssessmentLastModifiedInfo(publishedAssessment);
		}
		/*
		AssessmentService assessmentService = new AssessmentService();
		AssessmentFacade assessment = (AssessmentFacade) assessmentService.getAssessment(publishedAssessment.getAssessmentId());
		section1 = (SectionFacade) assessment.getSection(new Long(oldPos));
		section2 = (SectionFacade) assessment.getSection(new Long(newPos));
		if (section1 != null && section2 != null) {
			section1.setSequence(new Integer(newPos));
			section2.setSequence(new Integer(oldPos));
			AssessmentService service = new AssessmentService();
			service.saveOrUpdateSection(section1);
			service.saveOrUpdateSection(section2);
			service.updateAssessmentLastModifiedInfo(assessment);
		}
		*/
		// goto editAssessment.jsp, so reset assessmentBean
		assessmentBean.setAssessment(publishedAssessment);
	}
}
