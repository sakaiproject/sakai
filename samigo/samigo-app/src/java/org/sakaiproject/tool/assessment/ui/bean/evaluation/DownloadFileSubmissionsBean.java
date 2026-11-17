/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/bean/evaluation/DownloadFileSubmissions.java $
 * $Id: DownloadFileSubmissions.java 29431 2007-04-22 05:20:55Z ktsao@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jakarta.faces.bean.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.model.SelectItem;
import jakarta.faces.bean.ManagedBean;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.DownloadFileUtil;
import org.sakaiproject.util.ResourceLoader;

/* For evaluation: Download File Submissions backing bean */
@Slf4j
@ManagedBean(name="downloadFileSubmissions")
@SessionScoped
public class DownloadFileSubmissionsBean implements Serializable {

	private static final ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
	private String assessmentId;
	private String assessmentName;
	private String firstTargetSelected;
	private List<String> sectionsSelected;
	private ArrayList<ItemDataIfc> fileUploadQuestionList;
	private HashMap<Long, ItemDataIfc> fileUploadQuestionMap;
	private List<SelectItem> availableSectionItems;
	private String publishedAssessmentId;
	private HashMap<String, String> sectionUuidNameMap;
	public static final String SELECTED_SECTIONS_GROUPS = "sections";
	public static final String ONE_SECTION_GROUP = "one";
	public static final String SITE = "site";

	/**
	 * Creates a new TotalScoresBean object.
	 */
	public DownloadFileSubmissionsBean() {
		log.debug("Creating a new DownloadFileSubmissionsBean");
	}

	public List<String> getSectionsSelected() {
		return sectionsSelected;
	}

	public void setSectionsSelected(List<String> groupSetected) {
		this.sectionsSelected = groupSetected;
	}

	public List<SelectItem> getAvailableSectionItems() {
		return availableSectionItems;
	}

	public void setAvailableSectionItems(List<SelectItem> availableSectionItems) {
		this.availableSectionItems = availableSectionItems;
	}

	public int getAvailableSectionSize() {
		return availableSectionItems == null ? 0 : availableSectionItems.size();
	}

	public SelectItem[] getSiteSectionItems(){
		TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
		List sectionList = totalScores.getSectionFilterSelectItems();
		int numSection = availableSectionItems.size();
		SelectItem[] target = new SelectItem[2];

		target[0] = new SelectItem(this.SITE, rb.getString("for_all_sections_groups"));

		if (numSection == 1) {
			SelectItem sectionItem = (SelectItem) availableSectionItems.get(0);
			target[1] = new SelectItem(this.ONE_SECTION_GROUP, sectionItem.getLabel());
		}
		else if (numSection > 1) {
			target[1] = new SelectItem(this.SELECTED_SECTIONS_GROUPS, rb.getString("for_selected_sections_groups"));
		}
		return target;
	}

	public void setFirstTargetSelected(String firstTargetSelected){
		this.firstTargetSelected = firstTargetSelected.trim();
	}

	public String getFirstTargetSelected(){
		return firstTargetSelected;
	}

	public void setFileUploadQuestionList(ArrayList<ItemDataIfc> fileUploadQuestionList){
		this.fileUploadQuestionList = fileUploadQuestionList;
		this.fileUploadQuestionList.sort(new Comparator<ItemDataIfc>() {
			//We need to compare to order sections and, in each section its questions
			@Override
			public int compare(ItemDataIfc it1, ItemDataIfc it2) {
				if(it1 == null) return -1;
				if(it2 == null) return 1;
				if(it1 == it2 ) return 0;
				if(it1.getSection().getSequence() > it2.getSection().getSequence()) return 1;
				else if(it1.getSection().getSequence().equals(it2.getSection().getSequence())) {
					return it1.getSequence().compareTo(it2.getSequence());
				}
				return -1;
			}
		});
	}

	public ArrayList<ItemDataIfc> getFileUploadQuestionList(){
		return fileUploadQuestionList;
	}

	public void setFileUploadQuestionMap(HashMap<Long, ItemDataIfc> fileUploadQuestionMap){
		this.fileUploadQuestionMap = fileUploadQuestionMap;
	}

	public HashMap<Long, ItemDataIfc> getFileUploadQuestionMap(){
		return fileUploadQuestionMap;
	}

	public int getFileUploadQuestionListSize(){
		return fileUploadQuestionList == null ? 0 : fileUploadQuestionList.size();
	}

	public String getPublishedAssessmentId(){
		return publishedAssessmentId;
	}

	public void setPublishedAssessmentId(String publishedAssessmentId){
		this.publishedAssessmentId = publishedAssessmentId;
	}

	public HashMap getSectionUuidNameMap(){
		return sectionUuidNameMap;
	}

	public void setSectionUuidNameMap(HashMap<String, String> sectionUuidNameMap){
		this.sectionUuidNameMap = sectionUuidNameMap;
	}

	public void downloadFiles(ActionEvent event){
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
		HttpServletResponse res = (HttpServletResponse) context.getExternalContext().getResponse();
		String siteId = AgentFacade.getCurrentSiteId();
		List<String> selectedQuestions = ContextUtil.paramArrayValueLike("questionCheckbox");
		downloadFiles(req, res, siteId, firstTargetSelected, sectionsSelected, selectedQuestions);
		context.responseComplete();
	}

	/**
	 * Build and stream the ZIP of file-upload submissions.
	 * @return true if a ZIP response was started
	 */
	public boolean downloadFiles(HttpServletRequest req, HttpServletResponse res, String siteId,
			String targetSelected, List<String> sectionUuids, List<String> selectedQuestionIds) {
		TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
		DownloadFileUtil downloadFileUtil = new DownloadFileUtil();

		ArrayList<ItemDataIfc> idataList = new ArrayList<ItemDataIfc>();
		ArrayList<ItemDataIfc> allQuestions = getFileUploadQuestionList();
		if (allQuestions != null && allQuestions.size() == 1) {
			idataList.add(allQuestions.get(0));
		} else if (selectedQuestionIds != null) {
			for (String selectedQuestionId : selectedQuestionIds) {
				ItemDataIfc item = fileUploadQuestionMap.get(Long.valueOf(selectedQuestionId));
				if (item != null) {
					idataList.add(item);
				}
			}
		}

		if (idataList.isEmpty()) {
			log.warn("No file upload questions selected for download, publishedAssessmentId={}", publishedAssessmentId);
			return false;
		}

		List<SelectItem> sections = availableSectionItems;
		if (sections == null || sections.isEmpty()) {
			ArrayList<String> userUidList = getUserUidList(totalScores, null, siteId);
			downloadFileUtil.processWholeSiteOrOneSection(req, res, idataList, userUidList);
			return true;
		}

		String target = targetSelected != null ? targetSelected : firstTargetSelected;
		if (SITE.equals(target)) {
			ArrayList<String> userUidList = getUserUidList(totalScores, null, siteId);
			downloadFileUtil.processWholeSiteOrOneSection(req, res, idataList, userUidList);
		} else if (SELECTED_SECTIONS_GROUPS.equals(target)) {
			List<String> selectedSections = sectionUuids != null ? sectionUuids : sectionsSelected;
			if (selectedSections == null || selectedSections.isEmpty()) {
				log.warn("No sections selected for download, publishedAssessmentId={}", publishedAssessmentId);
				return false;
			}
			if (selectedSections.size() == 1) {
				String sectionUuid = selectedSections.get(0);
				String sectionName = sectionUuidNameMap != null ? sectionUuidNameMap.get(sectionUuid) : null;
				ArrayList<String> userUidList = getUserUidList(totalScores, sectionUuid, siteId);
				downloadFileUtil.processWholeSiteOrOneSection(req, res, idataList, userUidList, sectionName);
			} else {
				HashMap sectionUsersMap = getSectionUsersMap(totalScores, selectedSections, siteId);
				downloadFileUtil.processMultipleSection(req, res, idataList, sectionUsersMap);
			}
		} else {
			SelectItem item = sections.get(0);
			ArrayList<String> userUidList = getUserUidList(totalScores, (String) item.getValue(), siteId);
			downloadFileUtil.processWholeSiteOrOneSection(req, res, idataList, userUidList, item.getLabel());
		}
		return true;
	}

	private ArrayList<String> getUserUidList(TotalScoresBean totalScores, String sectionUuid, String siteId) {
		List<EnrollmentRecord> enrollments;
		ArrayList<String> userUidList = new ArrayList<String>();
		if (sectionUuid != null) {
			enrollments = totalScores.getSectionEnrollments(sectionUuid, siteId);
		} else {
			enrollments = totalScores.getAvailableEnrollments(false, siteId);
		}
		if (enrollments != null) {
			for (EnrollmentRecord enr : enrollments) {
				userUidList.add(enr.getUser().getUserUid());
			}
		}
		return userUidList;
	}

	private HashMap getSectionUsersMap(TotalScoresBean totalScores, List sectionList, String siteId) {
		HashMap<String, List<String>> sectionUsersMap = new HashMap<String, List<String>>();
		if (sectionList != null) {
			Iterator iter = sectionList.iterator();
			while (iter.hasNext()) {
				String sectionUuid = (String) iter.next();
				sectionUsersMap.put(sectionUuid, getUserUidList(totalScores, sectionUuid, siteId));
			}
		}
		return sectionUsersMap;
	}

}
