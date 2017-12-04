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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.DownloadFileUtil;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class DownloadFileSubmissionsBean implements Serializable {

	private String assessmentId;
	private String assessmentName;
	private String firstTargetSelected;
	private List<String> sectionsSelected;
	private ArrayList<ItemDataIfc> fileUploadQuestionList;
	private HashMap<Long, ItemDataIfc> fileUploadQuestionMap;
	private List<SelectItem> availableSectionItems;
	private String publishedAssessmentId;
	private HashMap<String, String> sectionUuidNameMap;
	public static String SELECTED_SECTIONS_GROUPS = "sections";
	public static String ONE_SECTION_GROUP = "one";
	public static String SITE = "site";

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
		return availableSectionItems.size();
	}

	public SelectItem[] getSiteSectionItems(){
		TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
		List sectionList = totalScores.getSectionFilterSelectItems();
		int numSection = availableSectionItems.size();
		SelectItem[] target = new SelectItem[2];
		ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
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
		return fileUploadQuestionList.size();
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

		ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
		TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
		StringBuilder zipFilename = new StringBuilder();
		zipFilename.append(totalScores.getAssessmentName());
		DownloadFileUtil downloadFileUtil = new DownloadFileUtil();

		ArrayList<ItemDataIfc> fileUPloadQuestionList = getFileUploadQuestionList();
		ArrayList<ItemDataIfc> idataList = new ArrayList<ItemDataIfc>();

		if (fileUPloadQuestionList.size() == 1) {
			ItemDataIfc idata = (ItemDataIfc) fileUPloadQuestionList.get(0);
			idataList.add(idata);
		}
		else {
			ArrayList<String> selectedQuestions = ContextUtil.paramArrayValueLike("questionCheckbox");
			Iterator iter = selectedQuestions.iterator();
			while (iter.hasNext()) {
				Long itemId = Long.valueOf((String) iter.next());
				idataList.add((ItemDataIfc) fileUploadQuestionMap.get(itemId));
			}
		}

		if (availableSectionItems.size() == 0) {
			ArrayList<String> userUidList = getUserUidList(totalScores, null);
			downloadFileUtil.processWholeSiteOrOneSection(req, res, idataList, userUidList);
		}
		else {
			if (this.SITE.equals(firstTargetSelected)) {
				ArrayList<String> userUidList = getUserUidList(totalScores, null);
				downloadFileUtil.processWholeSiteOrOneSection(req, res, idataList, userUidList);
			}
			else if (this.SELECTED_SECTIONS_GROUPS.equals(firstTargetSelected)) {
				if (sectionsSelected.size() == 1) {
					String sectionUuid = sectionsSelected.get(0);
					String sectionName = (String) sectionUuidNameMap.get(sectionUuid);
					ArrayList<String> userUidList = getUserUidList(totalScores, sectionUuid);
					downloadFileUtil.processWholeSiteOrOneSection(req, res, idataList, userUidList, sectionName);
				}
				else {
					HashMap sectionUsersMap = getSectionUsersMap(totalScores, sectionsSelected);
					downloadFileUtil.processMultipleSection(req, res, idataList, sectionUsersMap);
				}
			}
			else {
				SelectItem item = (SelectItem) availableSectionItems.get(0);
				ArrayList<String> userUidList = getUserUidList(totalScores, (String) item.getValue());
				if (item != null) {
					downloadFileUtil.processWholeSiteOrOneSection(req, res, idataList, userUidList, item.getLabel());
				}
			}
		}
		context.responseComplete();  
	}

	private ArrayList<String> getUserUidList(TotalScoresBean totalScores, String sectionUuid) {
		List<EnrollmentRecord> enrollments = null;
		ArrayList<String> userUidList = new ArrayList<String>();
		if (sectionUuid != null) {
			enrollments = totalScores.getSectionEnrollments(sectionUuid);
			for (Iterator iter = enrollments.iterator(); iter.hasNext(); ) {
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				userUidList.add(enr.getUser().getUserUid());
			}
		}
		else {
			enrollments = totalScores.getAvailableEnrollments(false);
			for (Iterator iter = enrollments.iterator(); iter.hasNext(); ) {
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				userUidList.add(enr.getUser().getUserUid());
			}
		}
		return userUidList;
	}

	private HashMap getSectionUsersMap(TotalScoresBean totalScores, List sectionList) {
		List<EnrollmentRecord> enrollments = null;;
		HashMap<String, List<String>> sectionUsersMap = new HashMap<String, List<String>>();
		if (sectionList != null) {
			Iterator iter = sectionList.iterator();
			while (iter.hasNext()) {
				String sectionUuid = (String) iter.next();
				sectionUsersMap.put(sectionUuid, getUserUidList(totalScores, sectionUuid));
			}
		}
		return sectionUsersMap;
	}

}
