/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/AreaImpl.java $
 * $Id: AreaImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;


import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.Rank;
import org.sakaiproject.api.app.messageforums.RankImage;
import org.sakaiproject.component.app.messageforums.dao.hibernate.util.comparator.AttachmentByCreatedDateDesc;

@Slf4j
public class RankImpl extends MutableEntityImpl implements Rank
{

	private String title;
	private String type;
	private Set<String> assignToIds;
	private String assignToDisplay;
	private String contextId;
	private long minPosts;
	private RankImage rankImage;
	  
	  
	public RankImpl(String title, String type, Set<String> assignToIds, String assignToDisplay,
			String contextId, long minPosts) {
		super();
		this.title = title;
		this.type = type;
		this.assignToIds = assignToIds;
		this.assignToDisplay = assignToDisplay;
		this.contextId = contextId;
		this.minPosts = minPosts;
	}

	public RankImpl() {
		super();
	}

	public String getType() {
		return type;
	}

	public void setType(String ranktype) {
		this.type = ranktype;
	}
 
	public String getTitle() {
		// TODO Auto-generated method stub
		return title;
	}

	public void setTitle(String rannktitle) {
		this.title = rannktitle;
		
	}

	public Set<String> getAssignToIds() {
		return assignToIds;
	}

	public void setAssignToIds(Set<String> assignToIds) {
		this.assignToIds = assignToIds;
	}

	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	public long getMinPosts() {
		 
		return minPosts;
	}

	public void setMinPosts(long number_of_posts) {
		this.minPosts = number_of_posts;
		
	}

	public RankImage getRankImage() {
		return rankImage;
	}

	public void setRankImage(RankImage imageattach) {
		this.rankImage = imageattach;
		
	}

	public String getAssignToDisplay() {
		return assignToDisplay;
	}
	public void setAssignToDisplay(String assigned_to_display) {
		assignToDisplay = assigned_to_display;
		
	}


}
