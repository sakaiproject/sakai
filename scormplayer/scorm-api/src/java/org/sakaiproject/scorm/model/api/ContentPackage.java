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
import java.util.Date;

public class ContentPackage implements Serializable {

	private static final int NUMBER_OF_TRIES_UNLIMITED = -1;
	
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String context;
	private String title;
	private String courseId;
	private String url;
	private Date releaseOn;
	private Date dueOn;
	private Date acceptUntil;
	private Date createdOn;
	private String createdBy;
	private Date modifiedOn;
	private String modifiedBy;
	private int numberOfTries = NUMBER_OF_TRIES_UNLIMITED;
	
	public ContentPackage() {
		
	}
	
	public ContentPackage(String title, String courseId) {
		this.title = title;
		this.courseId = courseId;
	}
	
	public ContentPackage(String title, String courseId, String url) {
		this.title = title;
		this.courseId = courseId;
		this.url = url;
	}
	
	public boolean isReleased() {
		Date now = new Date();
		
		return now.after(releaseOn);
	}
	
	public Date getAcceptUntil() {
		return acceptUntil;
	}
	
	public void setAcceptUntil(Date acceptUntil) {
		this.acceptUntil = acceptUntil;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getCourseId() {
		return courseId;
	}
	
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public Date getReleaseOn() {
		return releaseOn;
	}


	public void setReleaseOn(Date releaseOn) {
		this.releaseOn = releaseOn;
	}


	public Date getDueOn() {
		return dueOn;
	}


	public void setDueOn(Date dueOn) {
		this.dueOn = dueOn;
	}

	public Date getCreatedOn() {
		return createdOn;
	}
	
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	public Date getModifiedOn() {
		return modifiedOn;
	}
	
	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}
	
	public String getModifiedBy() {
		return modifiedBy;
	}
	
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}

	public int getNumberOfTries() {
		return numberOfTries;
	}

	public void setNumberOfTries(int numberOfTries) {
		this.numberOfTries = numberOfTries;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getStatus() {
		return "Open";
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

}
