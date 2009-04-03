/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebooktest;

import java.util.Date;

import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookService;

public class GradebookBean {
	private static final Log log = LogFactory.getLog(GradebookBean.class);

	private String uid;
	private String assignmentName;
	private boolean uidFound;
	private GradebookFrameworkService gradebookFrameworkService;
	private GradebookExternalAssessmentService gradebookExternalAssessmentService;
	private GradebookService gradebookService;

	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public boolean isUidFound() {
		return uidFound;
	}

	public String getAssignmentName() {
		return assignmentName;
	}
	public void setAssignmentName(String assignmentName) {
		this.assignmentName = assignmentName;
	}

	public void addAssignment(ActionEvent event) {
		getGradebookService().addExternalAssessment(uid, "External-" + assignmentName, null, assignmentName, new Double(10), new Date(), "Gradebook Service Test", new Boolean(false));
	}

	public void addAssignmentExternal(ActionEvent event) {
		//getGradebookExternalAssessmentService().addExternalAssessment(uid, "External-" + assignmentName, null, assignmentName, 10, new Date(), "Gradebook Service Test");
		getGradebookExternalAssessmentService().addExternalAssessment(uid, "External-" + assignmentName, null, assignmentName, new Double(10), new Date(), "Gradebook Service Test", new Boolean(false));
	}

	public void search(ActionEvent event) {
		uidFound = getGradebookFrameworkService().isGradebookDefined(uid);
		log.info("search uid=" + uid + ", uidFound=" + uidFound);
	}

	public void create(ActionEvent event) {
		getGradebookService().addGradebook(uid, "Gradebook " + uid);
		log.info("created Gradebook with uid=" + uid);
	}

	public GradebookService getGradebookService() {
		log.info("getGradebookService " + gradebookService);
		return gradebookService;
	}
	public void setGradebookService(GradebookService gradebookService) {
		log.info("setGradebookService " + gradebookService);
		this.gradebookService = gradebookService;
	}

	public void createFramework(ActionEvent event) {
		getGradebookFrameworkService().addGradebook(uid, "Gradebook " + uid);
		log.info("created Gradebook with uid=" + uid);
	}

	public GradebookFrameworkService getGradebookFrameworkService() {
		log.info("getGradebookFrameworkService " + gradebookFrameworkService);
		return gradebookFrameworkService;
	}
	public void setGradebookFrameworkService(GradebookFrameworkService gradebookFrameworkService) {
		log.info("setGradebookFrameworkService " + gradebookFrameworkService);
		this.gradebookFrameworkService = gradebookFrameworkService;
	}

	public GradebookExternalAssessmentService getGradebookExternalAssessmentService() {
		log.info("getGradebookExternalAssessmentService " + gradebookExternalAssessmentService);
		return gradebookExternalAssessmentService;
	}
	public void setGradebookExternalAssessmentService(GradebookExternalAssessmentService gradebookExternalAssessmentService) {
		log.info("setGradebookExternalAssessmentService " + gradebookExternalAssessmentService);
		this.gradebookExternalAssessmentService = gradebookExternalAssessmentService;
	}

}
