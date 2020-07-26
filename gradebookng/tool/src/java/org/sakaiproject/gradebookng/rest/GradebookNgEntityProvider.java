/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.gradebookng.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbAccessDeniedException;
import org.sakaiproject.gradebookng.business.model.GbGradeCell;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This entity provider is to support some of the Javascript front end pieces. It never was built to support third party access, and never
 * will support that use case.
 *
 * The data you need for Gradebook integrations should already be available in the standard gradebook entityprovider
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class GradebookNgEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable,
		Outputable, Describeable {

	private static final String MESSAGE_UNGRADED = "1";
	private static final String MESSAGE_GRADED = "2";

	@Setter
	private AuthzGroupService authzGroupService;

	@Setter
	private EmailService emailService;

	@Setter
	private SiteService siteService;

	@Setter
	private SessionManager sessionManager;

	@Setter
	private SecurityService securityService;

	@Setter
	private ServerConfigurationService serverConfigurationService;

	@Setter
	private GradebookNgBusinessService businessService;

	@Setter
	private GradebookService gradebookService;

	@Setter
	private UserDirectoryService userDirectoryService;

	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON };
	}

	@Override
	public String getEntityPrefix() {
		return "gbng";
	}

	/**
	 * Update the order of an assignment in the gradebook This is a per site setting.
	 *
	 * @param ref
	 * @param params map, must include: siteId assignmentId new order
	 *
	 *            an assignmentorder object will be created and saved as a list in the XML property 'gbng_assignment_order'
	 */
	@SuppressWarnings("unused")
	@EntityCustomAction(action = "assignment-order", viewKey = EntityView.VIEW_NEW)
	public void updateAssignmentOrder(final EntityReference ref, final Map<String, Object> params) {

		// get params
		final String siteId = (String) params.get("siteId");
		final long assignmentId = NumberUtils.toLong((String) params.get("assignmentId"));
		final int order = NumberUtils.toInt((String) params.get("order"));

		// check params supplied are valid
		if (StringUtils.isBlank(siteId) || assignmentId == 0 || order < 0) {
			throw new IllegalArgumentException(
					"Request data was missing / invalid");
		}
		checkValidSite(siteId);

		// check instructor or TA
		checkInstructorOrTA(siteId);

		// update the order
		this.businessService.updateAssignmentOrder(siteId, assignmentId, order);
	}

	/**
	 * Endpoint for getting the list of cells that have been edited. This is designed to be polled on a regular basis so must be lightweight
	 *
	 * @param view
	 * @return
	 */
	@EntityCustomAction(action = "isotheruserediting", viewKey = EntityView.VIEW_LIST)
	public List<GbGradeCell> isAnotherUserEditing(final EntityView view, final Map<String, Object> params) {

		// get siteId
		final String siteId = view.getPathSegment(2);

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"Site ID must be set in order to access GBNG data.");
		}
		checkValidSite(siteId);

		// check instructor or TA
		checkInstructorOrTA(siteId);

		if (!params.containsKey("since")) {
			throw new IllegalArgumentException(
					"Since timestamp (in milliseconds) must be set in order to access GBNG data.");
		}

		final long millis = NumberUtils.toLong((String) params.get("since"));
		final Date since = new Date(millis);

		return this.businessService.getEditingNotifications(siteId, since);
	}

	@SuppressWarnings("unused")
	@EntityCustomAction(action = "categorized-assignment-order", viewKey = EntityView.VIEW_NEW)
	public void updateCategorizedAssignmentOrder(final EntityReference ref, final Map<String, Object> params) {

		// get params
		final String siteId = (String) params.get("siteId");
		final long assignmentId = NumberUtils.toLong((String) params.get("assignmentId"));
		final int order = NumberUtils.toInt((String) params.get("order"));

		// check params supplied are valid
		if (StringUtils.isBlank(siteId) || assignmentId == 0 || order < 0) {
			throw new IllegalArgumentException(
					"Request data was missing / invalid");
		}
		checkValidSite(siteId);

		// check instructor or TA
		checkInstructorOrTA(siteId);

		// update the order
		try {
			this.businessService.updateAssignmentCategorizedOrder(siteId, assignmentId, order);
		} catch (final IdUnusedException e) {
			log.error(e.getMessage(), e);
		} catch (final PermissionException e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unused")
	@EntityCustomAction(action = "ping", viewKey = EntityView.VIEW_LIST)
	public String ping(final EntityView view) {
		return "pong";
	}

	@SuppressWarnings("unused")
	@EntityCustomAction(action = "comments", viewKey = EntityView.VIEW_LIST)
	public String getComments(final EntityView view, final Map<String, Object> params) {
		// get params
		final String siteId = (String) params.get("siteId");
		final long assignmentId = NumberUtils.toLong((String) params.get("assignmentId"));
		final String studentUuid = (String) params.get("studentUuid");

		// check params supplied are valid
		if (StringUtils.isBlank(siteId) || assignmentId == 0 || StringUtils.isBlank(studentUuid)) {
			throw new IllegalArgumentException(
					"Request data was missing / invalid");
		}

		checkValidSite(siteId);
		checkInstructorOrTA(siteId);

		return this.businessService.getAssignmentGradeComment(siteId, assignmentId, studentUuid);
	}

	private Set<String> getRecipients(Map<String, Object> params) {

		final String siteId = (String) params.get("siteId");
		final long assignmentId = NumberUtils.toLong((String) params.get("assignmentId"));
		final String action = (String) params.get("action");
		final String groupRef = (String) params.get("groupRef");
		final String minScoreString  = (String) params.get("minScore");
		final String maxScoreString = (String) params.get("maxScore");

		// check params supplied are valid
		if (StringUtils.isBlank(siteId) || assignmentId == 0 || StringUtils.isEmpty(groupRef)) {
			throw new IllegalArgumentException("You must supply siteId, groupRef and assignmentId");
		}

		checkInstructorOrTA(siteId);

		final Double minScore = (!StringUtils.isEmpty(minScoreString)) ? Double.valueOf(minScoreString) : null;
		final Double maxScore = (!StringUtils.isEmpty(maxScoreString)) ? Double.valueOf(maxScoreString) : null;

		Set<String> recipients = null;
		try {
			AuthzGroup authzGroup = authzGroupService.getAuthzGroup(groupRef);
			recipients = authzGroup.getUsers();
			// Remove the instructors
			recipients.removeAll(authzGroup.getUsersIsAllowed(Authz.PERMISSION_GRADE_ALL));
			recipients.removeAll(authzGroup.getUsersIsAllowed(Authz.PERMISSION_GRADE_SECTION));
		} catch (GroupNotDefinedException gnde) {
			throw new IllegalArgumentException("No group defined for " + groupRef);
		}

		List<GradeDefinition> grades
			= gradebookService.getGradesForStudentsForItem(siteId, assignmentId, new ArrayList<String>(recipients));

		if (MESSAGE_GRADED.equals(action)) {
			// We want to message graded students. Filter by min and max score, if needed.
			if (minScore != null) {
				grades = grades.stream().filter(g -> {
					try {
						return Double.valueOf(g.getGrade()).compareTo(minScore) >= 0;
					} catch (NumberFormatException nfe) {
						return false;
					}
				}).collect(Collectors.toList());
			}

			if (maxScore != null) {
				grades = grades.stream().filter(g -> {
					try {
						return Double.valueOf(g.getGrade()).compareTo(maxScore) <= 0;
					} catch (NumberFormatException nfe) {
						return false;
					}
				}).collect(Collectors.toList());
			}

			recipients = grades.stream().filter(g -> g.getDateRecorded() != null)
				.map(g -> g.getStudentUid()).collect(Collectors.toSet());
		} else if (MESSAGE_UNGRADED.equals(action)) {
			recipients.removeAll(grades.stream().filter(g -> g.getDateRecorded() != null).map(g -> g.getStudentUid()).collect(Collectors.toSet()));
		}

		return recipients;
	}

	@EntityCustomAction(action = "listMessageRecipients", viewKey = EntityView.VIEW_NEW)
	public ActionReturn listMessageRecipients(final EntityView view, final Map<String, Object> params) {

		Set<String> recipients = getRecipients(params);

		if (!recipients.isEmpty()) {
			List<User> users = recipients.stream().map(s -> {
				try {
					return userDirectoryService.getUser(s);
				} catch (UserNotDefinedException unde) {
					return null;
				}
			}).collect(Collectors.toList());

			if (users.contains(null)) {
				String errorMsg = "At least one of the students to message is null. No messsages sent.";
				log.warn(errorMsg);
				throw new EntityException(errorMsg, "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} else {
				// Cache the users in the session. The client needs to show the users to the caller, so they can
				// confirm, but we don't want to call this logic again for no reason.
				List<BasicUser> basicUsers = users.stream().map(BasicUser::new).collect(Collectors.toList());
				return new ActionReturn(basicUsers);
			}
		} else {
			Map<String, Object> data = new HashMap<>();
			data.put("result", "SUCCESS");
			return new ActionReturn(data);
		}
	}

	@EntityCustomAction(action = "messageStudents", viewKey = EntityView.VIEW_NEW)
	public ActionReturn messageStudents(final EntityView view, final Map<String, Object> params) {

		Set<String> recipients = getRecipients(params);

		if (!recipients.isEmpty()) {
			recipients.add(getCurrentUserId());

			List<User> users = recipients.stream().map(s -> {
				try {
					return userDirectoryService.getUser(s);
				} catch (UserNotDefinedException unde) {
					return null;
				}
			}).collect(Collectors.toList());

			if (users.contains(null)) {
				String errorMsg = "At least one of the students to message is null. No messsages sent.";
				log.warn(errorMsg);
				throw new EntityException(errorMsg, "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} else {
				String from = serverConfigurationService.getString("setup.request", "no-reply@" + serverConfigurationService.getServerName());
				List<String> headers = new ArrayList<>();
				String subject = (String) params.get("subject");
				headers.add("Subject: " + subject);
				headers.add("From: " + "\"" + serverConfigurationService.getString("ui.service", "Sakai") + "\" <" + from + ">");
				users.forEach(u -> emailService.send(from, u.getEmail(), subject, (String) params.get("body"), null, null, headers));
				Map<String, Object> data = new HashMap<>();
				data.put("result", "SUCCESS");
				return new ActionReturn(data);
			}
		} else {
			Map<String, Object> data = new HashMap<>();
			data.put("result", "SUCCESS");
			return new ActionReturn(data);
		}
	}

	/**
	 * Helper to check if the user is an instructor. Throws IllegalArgumentException if not. We don't currently need the value that this
	 * produces so we don't return it.
	 *
	 * @param siteId
	 * @return
	 * @throws SecurityException if error in auth/role
	 */
	private void checkInstructor(final String siteId) {

		final String currentUserId = getCurrentUserId();

		if (StringUtils.isBlank(currentUserId)) {
			throw new SecurityException("You must be logged in to access GBNG data");
		}

		final GbRole role = getUserRole(siteId);

		if (role != GbRole.INSTRUCTOR) {
			throw new SecurityException("You do not have instructor-type permissions in this site.");
		}
	}

	/**
	 * Helper to check if the user is an instructor or a TA. Throws IllegalArgumentException if not. We don't currently need the value that
	 * this produces so we don't return it.
	 *
	 * @param siteId
	 * @return
	 * @throws SecurityException
	 */
	private void checkInstructorOrTA(final String siteId) {

		final String currentUserId = getCurrentUserId();

		if (StringUtils.isBlank(currentUserId)) {
			throw new SecurityException("You must be logged in to access GBNG data");
		}

		final GbRole role = getUserRole(siteId);

		if (role != GbRole.INSTRUCTOR && role != GbRole.TA) {
			throw new SecurityException("You do not have instructor or TA-type permissions in this site.");
		}
	}

	/**
	 * Helper to get current user id
	 *
	 * @return
	 */
	private String getCurrentUserId() {
		return this.sessionManager.getCurrentSessionUserId();
	}

	/**
	 * Helper to check a site ID is valid. Throws IllegalArgumentException if not. We don't currently need the site that this produces so we
	 * don't return it.
	 *
	 * @param siteId
	 */
	private void checkValidSite(final String siteId) {
		try {
			this.siteService.getSite(siteId);
		} catch (final IdUnusedException e) {
			throw new IllegalArgumentException("Invalid site id");
		}
	}

	/**
	 * Get role for current user in given site
	 *
	 * @param siteId
	 * @return
	 */
	private GbRole getUserRole(final String siteId) {
		GbRole role;
		try {
			role = this.businessService.getUserRole(siteId);
		} catch (final GbAccessDeniedException e) {
			throw new SecurityException("Your role could not be checked properly. This may be a role configuration issue in this site.");
		}
		return role;
	}

    public class BasicUser {

        public String id;
        public String displayName;

        public BasicUser(User u) {

            super();

            this.id = u.getId();
            this.displayName = u.getDisplayName();
        }
    }
}
