/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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


package org.sakaiproject.lessonbuildertool;

import java.util.Date;

public class SimplePageLogEntryImpl implements SimplePageLogEntry {
	private long id;
	private Date lastViewed;
	private Date firstViewed;
	private String userId;
	private long itemId;
	private boolean complete;
    // dummy is for a page that hasn't been accessed yet.
    // the record is to indicate that the user has permission to access it
    // firstViewed will also be null, but I'm worried that in some databases
    // that may not be possible, so an explicit flag seems safer
	private boolean dummy;
	private String path;
	private String toolId;
	
	private Long studentPageId;

	public SimplePageLogEntryImpl() {}

	public SimplePageLogEntryImpl(String userId, long itemId, Long studentPageId) {
		firstViewed = new Date();
		this.userId = userId;
		this.itemId = itemId;
		this.studentPageId = studentPageId;
		complete = false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getLastViewed() {
		return lastViewed;
	}

	public void setLastViewed(Date lastViewed) {
		this.lastViewed = lastViewed;
	}

	public Date getFirstViewed() {
		return firstViewed;
	}

	public void setFirstViewed(Date firstViewed) {
		this.firstViewed = firstViewed;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean c) {
		complete = c;
	}

	public boolean getDummy() {
		return dummy;
	}

	public void setDummy(Boolean d) {
		if (d == null)
		    dummy = false;
		else
		    dummy = d;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

    // note that toolId is the tool from which this entry was
    // actually made. Because the same page can be accessed
    // from different locations, there's no static way to be
    // sure which tool a page was displayed in. We need to know in order
    // to find the last page accessed in a specific tool

	public String getToolId() {
		return toolId;
	}

	public void setToolId(String toolId) {
		this.toolId = toolId;
	}

	public Long getStudentPageId() {
		return studentPageId;
	}
	
	public void setStudentPageId(Long studentPageId) {
		this.studentPageId = studentPageId;
	}
}
