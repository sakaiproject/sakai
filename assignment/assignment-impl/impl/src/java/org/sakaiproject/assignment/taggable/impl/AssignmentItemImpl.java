/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.assignment.taggable.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.taggable.api.TaggableActivity;
import org.sakaiproject.taggable.api.TaggableItem;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

public class AssignmentItemImpl implements TaggableItem {

	private static final Logger logger = LoggerFactory.getLogger(AssignmentItemImpl.class);

	private static ResourceLoader rb = new ResourceLoader("assignment");

	protected static final String ITEM_REF_SEPARATOR = "@";

	protected AssignmentSubmission submission;

	protected String userId;

	protected TaggableActivity activity;

	protected AssignmentActivityProducerImpl producer;

	public AssignmentItemImpl(AssignmentActivityProducerImpl producer, AssignmentSubmission submission, String userId,
			TaggableActivity activity) {
		this.producer = producer;
		this.submission = submission;
		this.userId = userId;
		this.activity = activity;
	}

	public TaggableActivity getActivity() {
		return activity;
	}

	public String getContent() {
		return submission.getSubmittedText();
	}

	public Object getObject() {
		return submission;
	}

	public String getReference() {
		StringBuilder sb = new StringBuilder();
		sb.append(submission.getReference());
		sb.append(ITEM_REF_SEPARATOR);
		sb.append(userId);
		return sb.toString();
	}

	public String getTitle() {
		StringBuilder sb = new StringBuilder();
		try {
			User user = producer.userDirectoryService.getUser(userId);
			sb.append(user.getFirstName());
			sb.append(' ');
			sb.append(user.getLastName());
			sb.append(' ');
			sb.append(rb.getString("gen.submission"));
		} catch (UserNotDefinedException unde) {
			logger.error(this + ":getTitle " + unde.getMessage());
		}
		return sb.toString();
	}

	public String getUserId() {
		return userId;
	}
	
	public String getItemDetailUrl()
	{
		String subRef = submission.getReference().replaceAll("/", "_");
		String url = producer.serverConfigurationService.getServerUrl() +
			"/direct/assignment/" + submission.getAssignmentId() + "/doView_grade/" + subRef;
		return url;
	}
	
	public String getItemDetailPrivateUrl(){
		String subRef = submission.getReference().replaceAll("/", "_");
		String url = producer.serverConfigurationService.getServerUrl() +
			"/direct/assignment/" + submission.getAssignmentId() + "/doView_grade_private/" + subRef;
		return url;
	}
	
	public String getItemDetailUrlParams() {
		return "?TB_iframe=true";
	}

	public boolean getUseDecoration() {
		return true;
	}

	public String getIconUrl()
	{
		String url = producer.serverConfigurationService.getServerUrl() + "/library/image/silk/page_edit.png";
		return url;
	}
	
	public String getOwner() {
		String owner = null;
		User[] submitters = ((AssignmentSubmission)getObject()).getSubmitters();
		for (User submitter : submitters) {
			if (owner != null)
				owner = owner + ", " + submitter.getDisplayName();
			else
				owner = submitter.getDisplayName();
		}
		return owner;
	}

	public String getSiteTitle() {
		String siteId = ((AssignmentSubmission)getObject()).getAssignment().getContext();
		String title = getSite(siteId).getTitle();
		
		return title;
	}
	
	public Date getLastModifiedDate() {
		return new Date(((AssignmentSubmission)getObject()).getTimeLastModified().getTime());
	}
	
	public String getTypeName() {
		return rb.getString("gen.assig");
	}
	
	private Site getSite(String siteId) {
		Site site = null;
		try {
			site = producer.siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			logger.warn("Failed to find site for ID of: "+ siteId);
		}
		return site;
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof AssignmentItemImpl))
			return false;
		else if (!((TaggableItem) obj).getReference().equals(this.getReference()))
			return false;
		
		return true;
	}

	public int hashCode()
	{
		return this.getReference().hashCode();
	}	
}
