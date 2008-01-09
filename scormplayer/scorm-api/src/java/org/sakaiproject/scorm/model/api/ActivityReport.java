/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.model.api;


import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ActivityReport implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String activityId;
	private String scoId;
	private String title;
	
	private List<Objective> objectives;
	
	private Progress progress;
	
	private Score score;
	
	private List<CMIData> cmiData;


	public ActivityReport() {
		objectives = new LinkedList<Objective>();
	}
	
	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getScoId() {
		return scoId;
	}

	public void setScoId(String scoId) {
		this.scoId = scoId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Score getScore() {
		return score;
	}

	public void setScore(Score score) {
		this.score = score;
	}

	public List<Objective> getObjectives() {
		return objectives;
	}

	public void setObjectives(List<Objective> objectives) {
		this.objectives = objectives;
	}


	public Progress getProgress() {
		return progress;
	}


	public void setProgress(Progress progress) {
		this.progress = progress;
	}

	public List<CMIData> getCmiData() {
		return cmiData;
	}

	public void setCmiData(List<CMIData> cmiData) {
		this.cmiData = cmiData;
	}
	
	
}
