/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;


/**
 * Provides data for the student view of the gradebook.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class StudentViewBean extends ViewByStudentBean implements Serializable {
	private String studentUidToView;
	private String instViewReturnToPage;
	private String instViewAssignmentId;

	public void init() {

		setIsInstructorView(false);
		if (studentUidToView != null && isUserAbleToGradeAll()){
			// we came to this page as an instructor "previewing" the student's view
			setStudentUid(studentUidToView);
		} else {
			setStudentUid(getUserUid());
			// SAK-23566 indicate the student viewed their grades
			EventTrackingService ets = (EventTrackingService) ComponentManager.get(EventTrackingService.class);
			if (ets != null) {
			    ets.post(ets.newEvent("gradebook.studentView", "/gradebook/studentView/"+getUserUid(), false));
			}
		}

		super.init();
	}
	
	/**
	 * If an instructor wants to see "student's view of her grades", this
	 * param will be passed
	 * @param studentUidToView
	 */
	public void setStudentUidToView(String studentUidToView) {
		this.studentUidToView = studentUidToView;
	}
	public String getStudentUidToView() {
		return studentUidToView;
	}
	/**
	 * To return to the inst view, we need to keep track of the original
	 * returnToPage parameter
	 * @param instViewReturnToPage
	 */
	public void setInstViewReturnToPage(String instViewReturnToPage) {
		this.instViewReturnToPage = instViewReturnToPage;
	}
	public String getInstViewReturnToPage() {
		return instViewReturnToPage;
	}
	/**
	 * To return to the inst view, we need to keep track of the original
	 * assignmentId parameter
	 * @param instViewReturnToPage
	 */
	public void setInstViewAssignmentId(String instViewAssignmentId) {
		this.instViewAssignmentId = instViewAssignmentId;
	}
	public String getInstViewAssignmentId() {
		return instViewAssignmentId;
	}
}
