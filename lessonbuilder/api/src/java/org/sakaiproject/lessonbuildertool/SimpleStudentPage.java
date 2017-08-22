/**
 * Copyright (c) 2003-2012 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lessonbuildertool;

import java.util.Date;

public interface SimpleStudentPage {
	public long getId();
	public void setId(long id);
	
	public Date getLastUpdated();
	public void setLastUpdated(Date lastUpdated);
	
	public long getItemId();
	public void setItemId(long itemId);
	
	public long getPageId();
	public void setPageId(long pageId);
	
	public String getTitle();
	public void setTitle(String title);
	
	public String getOwner();
	public void setOwner(String owner);
	
        public String getGroup();
        public void setGroup(String group);
	
    // no longer used, but left in to ease transitions
        public boolean isGroupOwned();
        public void setGroupOwned(Boolean g);

	public Long getCommentsSection();
	public void setCommentsSection(Long commentsSection);
	
	public Date getLastCommentChange();
	public void setLastCommentChange(Date lastCommentChange);
	
	public boolean isDeleted();
	public void setDeleted(Boolean deleted);
	
	public Double getPoints();
	public void setPoints(Double points);
}
