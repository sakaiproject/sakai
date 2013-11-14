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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer;
import org.sakaiproject.taggable.api.TaggableActivity;
import org.sakaiproject.taggable.api.TaggableItem;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.taggable.api.TaggingProvider;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

public class AssignmentActivityProducerImpl implements
		AssignmentActivityProducer {

	private static final Log logger = LogFactory
			.getLog(AssignmentActivityProducerImpl.class);

	private static ResourceLoader rb = new ResourceLoader("assignment");

	protected AssignmentService assignmentService;

	protected EntityManager entityManager;

	protected TaggingManager taggingManager;

	protected SiteService siteService;

	protected SecurityService securityService;

	protected UserDirectoryService userDirectoryService;

	public boolean allowGetItems(TaggableActivity activity,
			TaggingProvider provider, boolean allowGetItems, String taggedItem) {
		// We aren't picky about the provider, so ignore that argument.
		// Only allow this if the user can grade submissions
		return assignmentService.allowGradeSubmission(activity.getReference());
	}

	public boolean allowRemoveTags(TaggableActivity activity) {
		return securityService.unlock(
				AssignmentService.SECURE_REMOVE_ASSIGNMENT, activity
						.getReference());
	}

	public boolean allowRemoveTags(TaggableItem item) {
		return securityService.unlock(
				AssignmentService.SECURE_REMOVE_ASSIGNMENT_SUBMISSION,
				parseSubmissionRef(item.getReference()));
	}

	public boolean allowTransferCopyTags(TaggableActivity activity) {
		return securityService.unlock(SiteService.SECURE_UPDATE_SITE,
				siteService.siteReference(activity.getContext()));
	}

	public boolean checkReference(String ref) {
		return ref.startsWith(AssignmentService.REFERENCE_ROOT);
	}

	public List<TaggableActivity> getActivities(String context,
			TaggingProvider provider) {
		// We aren't picky about the provider, so ignore that argument.
		List<TaggableActivity> activities = new ArrayList<TaggableActivity>();
		List<Assignment> assignments = assignmentService
				.getListAssignmentsForContext(context);
		for (Assignment assignment : assignments) {
			activities.add(getActivity(assignment));
		}
		return activities;
	}

	public TaggableActivity getActivity(Assignment assignment) {
		return new AssignmentActivityImpl(assignment, this);
	}

	public TaggableActivity getActivity(String activityRef,
			TaggingProvider provider, String taggedItem) {
		// We aren't picky about the provider, so ignore that argument.
		TaggableActivity activity = null;
		if (checkReference(activityRef)) {
			try {
				Assignment assignment = assignmentService.getAssignment(activityRef);
				if (assignment != null)
					activity = new AssignmentActivityImpl(assignment, this);
			} catch (IdUnusedException iue) {
				logger.error(iue.getMessage(), iue);
			} catch (PermissionException pe) {
				logger.error(pe.getMessage(), pe);
			}
		}
		return activity;
	}

	public String getContext(String ref) {
		return entityManager.newReference(ref).getContext();
	}

	public String getId() {
		return PRODUCER_ID;
	}

	public TaggableItem getItem(AssignmentSubmission assignmentSubmission,
			String userId) {
		return new AssignmentItemImpl(assignmentSubmission, userId,
				new AssignmentActivityImpl(
						assignmentSubmission.getAssignment(), this));
	}

	public TaggableItem getItem(String itemRef, TaggingProvider provider, boolean getMyItemsOnly, String taggedItem) {
		// We aren't picky about the provider, so ignore that argument.
		TaggableItem item = null;
		if (checkReference(itemRef)) {
			try {
				AssignmentSubmission submission = assignmentService
						.getSubmission(parseSubmissionRef(itemRef));
				item = new AssignmentItemImpl(submission, parseAuthor(itemRef),
						new AssignmentActivityImpl(submission.getAssignment(),
								this));
			} catch (IdUnusedException iue) {
				logger.error(iue.getMessage(), iue);
			} catch (PermissionException pe) {
				logger.error(pe.getMessage(), pe);
			}
		}
		return item;
	}

	public List<TaggableItem> getItems(TaggableActivity activity,
			String userId, TaggingProvider provider, boolean getMyItemsOnly, String taggedItem) {
		// We aren't picky about the provider, so ignore that argument.
		List<TaggableItem> returned = new ArrayList<TaggableItem>();
		try {
			Assignment assignment = (Assignment) activity.getObject();
			AssignmentSubmission submission = assignmentService.getSubmission(
					assignment.getReference(), userDirectoryService
							.getUser(userId));
			if (submission != null && submission.getSubmitted() && submission.getTimeSubmitted() != null) {
				TaggableItem item = new AssignmentItemImpl(submission, userId,
						activity);
				returned.add(item);
			}
		} catch (UserNotDefinedException unde) {
			logger.error(unde.getMessage(), unde);
		}
		return returned;
	}

	public List<TaggableItem> getItems(TaggableActivity activity,
			TaggingProvider provider, boolean getMyItemsOnly, String taggedItem) {
		// We aren't picky about the provider, so ignore that argument.
		List<TaggableItem> items = new ArrayList<TaggableItem>();
		Assignment assignment = (Assignment) activity.getObject();
		/*
		 * If you're not allowed to grade submissions, you shouldn't be able to
		 * look at submission items. It seems that anybody is allowed to get any
		 * submissions.
		 */
		if (assignmentService.allowGradeSubmission(assignment.getReference())) {
			for (Iterator<AssignmentSubmission> i = assignmentService
					.getSubmissions(assignment).iterator(); i.hasNext();) {
				AssignmentSubmission submission = i.next();
				if (submission != null && submission.getSubmitted() && submission.getTimeSubmitted() != null) {
					for (Object submitterId : submission.getSubmitterIds()) {
						items.add(new AssignmentItemImpl(submission,
								(String) submitterId, activity));
					}
				}
			}
		}
		return items;
	}

	public String getItemPermissionOverride() {
		return AssignmentService.SECURE_ACCESS_ASSIGNMENT;
	}

	public String getName() {
		return rb.getString("gen.assig");
	}

	public void init() {
		logger.info("init()");

		taggingManager.registerProducer(this);
	}

	protected String parseAuthor(String itemRef) {
		return itemRef.split(AssignmentItemImpl.ITEM_REF_SEPARATOR)[1];
	}

	protected String parseSubmissionRef(String itemRef) {
		return itemRef.split(AssignmentItemImpl.ITEM_REF_SEPARATOR)[0];
	}

	public void setAssignmentService(AssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setTaggingManager(TaggingManager taggingManager) {
		this.taggingManager = taggingManager;
	}

	public void setUserDirectoryService(
			UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public boolean hasSubmissions(TaggableActivity activity,
			TaggingProvider provider, boolean getMyItemsOnly, String taggedItem) {
		List<TaggableItem> items = getItems(activity, provider, getMyItemsOnly, taggedItem);
		return items.size() > 0;
	}
	
	public boolean hasSubmissions(TaggableActivity activity, String userId,
			TaggingProvider provider, boolean getMyItemsOnly, String taggedItem) {
		List<TaggableItem> items = getItems(activity, userId, provider, getMyItemsOnly, taggedItem);
		return items.size() > 0;
	}
}
