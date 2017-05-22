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

public class SimplePageImpl implements SimplePage {
	private long pageId;
	private String toolId;
	private String siteId;
	private String title;
    // warning: using parent is probably a bad idea. The same page
    // can be called from different contexts. So the effective parent is different
    // in different cases. When we go to a page we pass the one from which we came
    // as well. That acts as a dynamic parent.
	private Long parent;
	private Long topParent;
	private boolean hidden;
	private Date releaseDate;
	private Double gradebookPoints;
	
	private String owner;       // If this is a student content site, lists the student
        private String group;
	private String groups; // group if group owned, but retain the creator
        private boolean groupOwned = false; // no longer used
	
	private String cssSheet = null; // ID of a resource, if a separate CSS sheet is to be used

	private String folder = null; // ID of a resource

	private boolean owned = false;

	public SimplePageImpl() {}

	public SimplePageImpl(String toolId, String siteId, String title, Long parent, Long topParent) {
		this.toolId = toolId;
		this.siteId = siteId;
		this.title = title;
		this.parent = parent;
		this.topParent = topParent;
		hidden = false;
	}

	public long getPageId() {
		return pageId;
	}

	public void setPageId(long p) {
		pageId = p;
	}

	public String getToolId() {
		return toolId;
	}

	public void setToolId(String toolId) {
		this.toolId = toolId;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String i) {
		siteId = i;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String t) {
		title = t;
	}

    // see warning above about using the parent
	public Long getParent() {
		return parent;
	}

	public void setParent(Long l) {
		parent = l;
	}

	public Long getTopParent() {
		return topParent;
	}

	public void setTopParent(Long l) {
		topParent = l;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public Double getGradebookPoints() {
		return gradebookPoints;
	}

	public void setGradebookPoints(Double points) {
		this.gradebookPoints = points;
	}
        
	public String getOwner() {
		if (owner != null && owner.length() == 0)
			return null;
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getGroup() {
		if (group != null && group.length() == 0)
			return null;
		return group;
	}
	
	public void setGroup(String g) {
		group = g;
	}
	
	public boolean isGroupOwned() {
	    return groupOwned;
	}

	public void setGroupOwned(Boolean g) {
	    if (g == null)
		g = false;
	    groupOwned = g;
	}

	public String getCssSheet() {
		return cssSheet;
	}
	
	public void setCssSheet(String cssSheet) {
		this.cssSheet = cssSheet;
	}

	public String getFolder() {
		return folder;
	}
	
	public void setFolder(String f) {
		this.folder = f;
	}

	public boolean isOwned() {
		return owned;
	}

	public void setOwned(Boolean owned) {
		if(owned == null) {
			owned = false;
		}
		this.owned = owned;
	}
}
