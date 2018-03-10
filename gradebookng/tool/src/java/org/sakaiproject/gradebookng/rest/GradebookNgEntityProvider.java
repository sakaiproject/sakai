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

import java.lang.reflect.Type;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbAccessDeniedException;
import org.sakaiproject.gradebookng.business.model.GbGradeCell;
import org.sakaiproject.gradebookng.rest.model.CourseGradeSummary;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradeMappingDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

		// check instructor
		checkInstructor(siteId);

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

		// check instructor
		checkInstructor(siteId);

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

	@SuppressWarnings("unused")
	@EntityCustomAction(action = "course-grades", viewKey = EntityView.VIEW_LIST)
	public CourseGradeSummary getCourseGradeSummary(final EntityView view, final Map<String, Object> params) {

		// get params
		final String siteId = (String) params.get("siteId");
		final String schema = (String) params.get("schema");

		log.debug("Schema json:" + schema);

		checkValidSite(siteId);
		checkInstructor(siteId);

		// if we have a schema provided, deserialise
		Map<String, Double> gradingSchema = null;
		if (StringUtils.isNotBlank(schema)) {
			final Gson gson = new Gson();
			final Type mappingType = new TypeToken<LinkedHashMap<String, Double>>() {
			}.getType();
			gradingSchema = gson.fromJson(schema, mappingType);

			log.debug("provided gradeMap:" + gradingSchema);

			if (gradingSchema == null) {
				throw new IllegalArgumentException("Grading schema data was missing / invalid");
			}
		}

		// if still null, use the persistent one for this gradebook
		if (gradingSchema == null) {
			log.debug("gradeMap not provided, using persistent one");
			final GradebookInformation info = this.businessService.getGradebookSettings(siteId);
			gradingSchema = info.getSelectedGradingScaleBottomPercents();
			log.debug("persistent gradeMap:" + gradingSchema);
		}

		// ensure grading schema is sorted so the grade mapping works correctly
		gradingSchema = GradeMappingDefinition.sortGradeMapping(gradingSchema);

		// get the course grades and re-map to summary. Also sorts the data so it is ready for the consumer to use
		final Map<String, CourseGrade> courseGrades = this.businessService.getCourseGrades(siteId, gradingSchema);

		return reMap(courseGrades, gradingSchema.keySet());
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

	/**
	 * Re-map the course grades returned from the business service into our CourseGradeSummary object for returning on the REST API.
	 *
	 * @param courseGrades map of student to course grade
	 * @param gradingSchema the grading schema that has the order
	 * @return
	 */
	private CourseGradeSummary reMap(final Map<String, CourseGrade> courseGrades, final Set<String> order) {
		final CourseGradeSummary summary = new CourseGradeSummary();
		courseGrades.forEach((k,v) -> {
			summary.add(v.getDisplayGrade());
		});

		//sort the map based on the ordered schema
		final Map<String, Integer> originalData = summary.getDataset();
		final Map<String, Integer> sortedData = new LinkedHashMap<>();
		order.forEach(o -> {
			// data set must contain everything in the grading schema
			Integer value = originalData.get(o);
			if (value == null) {
				value = 0;
			}
			sortedData.put(o, value);
		});
		summary.setDataset(sortedData);

		return summary;
	}

	@Setter
	private SiteService siteService;

	@Setter
	private SessionManager sessionManager;

	@Setter
	private SecurityService securityService;

	@Setter
	private GradebookNgBusinessService businessService;

}
