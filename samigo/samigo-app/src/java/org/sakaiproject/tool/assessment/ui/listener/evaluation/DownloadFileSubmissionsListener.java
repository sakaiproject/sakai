/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/evaluation/QuestionScoreListener.java $
 * $Id: QuestionScoreListener.java 11438 2006-06-30 20:06:03Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.DownloadFileSubmissionsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

@Slf4j
public class DownloadFileSubmissionsListener implements ActionListener {

	/**
	 * Standard process action method.
	 * 
	 * @param event
	 *            ActionEvent
	 * @throws AbortProcessingException
	 */
	public void processAction(ActionEvent event)
	throws AbortProcessingException {
		log.debug("DownloadFileSubmissionsListener");
		TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
		DownloadFileSubmissionsBean downloadFileSubmissionsBean = (DownloadFileSubmissionsBean) ContextUtil
		.lookupBean("downloadFileSubmissions");

		String publishedId = ContextUtil.lookupParam("publishedId");
		downloadFileSubmissionsBean.setPublishedAssessmentId(publishedId);
		PublishedAssessmentService pubService = new PublishedAssessmentService();
		PublishedAssessmentIfc publishedAssessment = totalScores.getPublishedAssessment();
		if (publishedAssessment == null) {
			publishedAssessment = pubService.getPublishedAssessment(publishedId);
		}

		downloadFileSubmissionsBean.setSectionsSelected(new ArrayList());
		Map publishedItemHash = pubService.preparePublishedItemHash(publishedAssessment);
		Iterator iter = publishedAssessment.getSectionArraySorted().iterator();
		HashMap<Long, ItemDataIfc> fileUploadQuestionMap = new HashMap<Long, ItemDataIfc>();
		while (iter.hasNext()) {
			SectionDataIfc sdata = (SectionDataIfc) iter.next();
			Iterator iter2 = sdata.getItemArraySortedForGrading().iterator();
			while (iter2.hasNext()) {
				ItemDataIfc idata = (ItemDataIfc) iter2.next();
				if (TypeIfc.FILE_UPLOAD.equals(idata.getTypeId())) {
					fileUploadQuestionMap.put(idata.getItemId(), idata);
				}
			}
		}
		downloadFileSubmissionsBean.setFileUploadQuestionMap(fileUploadQuestionMap);
		downloadFileSubmissionsBean.setFileUploadQuestionList(new ArrayList(fileUploadQuestionMap.values()));

		List availableSectionItems = new ArrayList();
		HashMap<String, String> sectionUuidNameMap = new HashMap<String, String>();
		List availableSections = totalScores.getAvailableSections();
		if (availableSections != null) {
			downloadFileSubmissionsBean.setFirstTargetSelected(downloadFileSubmissionsBean.SITE);
			for (int i = 0; i < availableSections.size(); i++) {
				CourseSection section = (CourseSection)availableSections.get(i);
				availableSectionItems.add(new SelectItem(section.getUuid(), section.getTitle()));
				sectionUuidNameMap.put(section.getUuid(), section.getTitle());
			}
			downloadFileSubmissionsBean.setSectionUuidNameMap(sectionUuidNameMap);
		}
		downloadFileSubmissionsBean.setAvailableSectionItems(availableSectionItems);
	}
}
