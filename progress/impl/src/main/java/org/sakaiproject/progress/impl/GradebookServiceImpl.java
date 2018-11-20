/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.progress.impl;

import java.io.Serializable;
import java.util.*;

//import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.progress.api.IGradebookService;
import lombok.Setter;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;


public class GradebookServiceImpl implements IGradebookService, Comparable, Serializable {

	@Setter
	private SiteService siteService;

	@Setter
	private GradebookService gradebookService;

	@Setter
	private UserDirectoryService userDirectoryService;

	protected Gradebook gradebook;
	protected String gradebookUid;
	protected Long gradebookId;
	private String siteId;

	@Override
	public void setGradebook(final String uid) throws GradebookNotFoundException {
		gradebook = (Gradebook)gradebookService.getGradebook(uid);
		gradebookUid=gradebook.getUid();
		gradebookId=gradebook.getId();
	}

	@Override
	public Gradebook getGradebook(final String uid){ return this.gradebook;}

	@Override
	public int compareTo(Object o) {
		return 0;
	}

	@Override
	public String getTitle() {
		return gradebook.getName();
	}

	@Override
	public String getContext() {
		return siteId;
	}

	@Override
	public void setContext(String context) {
		siteId = context;
		setGradebook(siteId);
	}

	@Override
	public List<User> getStudents(String siteId) {

		return userDirectoryService.getUsers(this.getStudentsUIDs(siteId));
	}

	public List<String> getStudentsUIDs(String siteID){
		Set<String> userUIDs = new HashSet<String>();
		try {
			userUIDs = siteService.getSite(siteID).getUsersIsAllowed("section.role.student");
		} catch (IdUnusedException e) {
			e.printStackTrace();
		}
		List<String> userIds = new ArrayList<String>();

		for(String id: userUIDs){
			userIds.add(id);
		}

		return userIds;
	}
	@Override
	public Long getId() {
		return gradebookId;
	}

	@Override
	public TreeMap getStudentMap() {
		return null;
	}

	@Override
	public boolean hasStudent(String username) {
		return false;
	}

	@Override
	public String getFirstUploadedUsername() {
		return null;
	}

	@Override
	public void setFirstUploadedUsername(String username) {

	}

	@Override
	public List getUsernames() {
		return null;
	}

	@Override
	public void setUsernames(List<String> usernames) {

	}

	@Override
	public Map<String, CourseGrade> getCourseGrades(String siteID) {
        Map<String, CourseGrade> courseGradeMap;
        
        if(gradebook == null) {
            try {
                this.setGradebook(siteId);
            } catch(GradebookNotFoundException e) {
                return null;
            }
        }

		List<String> studentUuids = this.getStudentsUIDs(siteID);
        
        courseGradeMap = this.gradebookService.getCourseGradeForStudents(gradebook.getUid(), studentUuids);
        return courseGradeMap;
    }
}
