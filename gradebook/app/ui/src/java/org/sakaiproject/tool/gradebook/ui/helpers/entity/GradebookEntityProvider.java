/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.tool.gradebook.ui.helpers.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.ui.helpers.entity.model.Category;
import org.sakaiproject.tool.gradebook.ui.helpers.entity.model.GradebookData;
import org.sakaiproject.tool.gradebook.ui.helpers.entity.model.GradebookItem;
import org.sakaiproject.tool.gradebook.ui.helpers.entity.model.StudentGrade;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradebookItemViewParams;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.AuthorizationFailedProducer;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.GradebookItemProducer;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/*
 * This is a provider for looking up and adding/editing Gradebook Items.
 * It is actually passing along to a gradebook UI via RSF and does not provide any rest access to grades data
 */
@Slf4j
public class GradebookEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, CoreEntityProvider,
		EntityViewParamsInferrer, Describeable, Sampleable, ActionsExecutable,
		Outputable {

	public final static String ENTITY_PREFIX = "gradebook";

	@Setter
	private GradebookService gradebookService;

	@Setter
	private SiteService siteService;

	@Setter
	private UserDirectoryService userDirectoryService;
	
	@Setter
	private SecurityService securityService;

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	@Override
	public boolean entityExists(String id) {
		return true;
	}

	@Override
	public String[] getHandledPrefixes() {
		return new String[] { ENTITY_PREFIX };
	}

	@Override
	public Object getSampleEntity() {
		return new GradeAssignmentItemDetail();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable#
	 * getHandledOutputFormats()
	 */
	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	@Override
	public ViewParameters inferDefaultViewParameters(String reference) {
		// IdEntityReference ep = new IdEntityReference(reference);
		// String contextId = ep.id;
		String contextId = new EntityReference(reference).getId();

		if (gradebookService.currentUserHasEditPerm(contextId)) {
			Long gradebookEntryId = null;
			return new GradebookItemViewParams(GradebookItemProducer.VIEW_ID,
					contextId, gradebookEntryId);
		} else {
			return new SimpleViewParameters(AuthorizationFailedProducer.VIEW_ID);
		}
	}

	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public GradeCourse getCourseGradebook(EntityView view) {
		String siteId = view.getPathSegment(2);
		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					String.format(
							"siteId must be set in order to get the gradebook for a site, via the URL /%s/site/{siteId}",
							ENTITY_PREFIX));
		}
		String userId = developerHelperService.getCurrentUserId();
		if (userId == null) {
			throw new SecurityException("Only logged in users can access");
		}

		Site site = this.getSite(siteId);

		// The gradebookUID is the siteId, the gradebookID is a long
		if (!gradebookService.isGradebookDefined(siteId)) {
			throw new IllegalArgumentException("No gradebook found for site: "
					+ siteId);
		}

		if (securityService.isSuperUser() || siteService.allowUpdateSite(siteId)) {
			// admin or instructor
			log.info("Admin or instructor accesssing gradebook of site "
					+ siteId);
			GradeCourse course = new GradeCourse(site);
			Collection<String> students = getStudentList(siteId);
			@SuppressWarnings("unchecked")
			List<Assignment> gbitems = gradebookService.getAssignments(siteId);
			for (Assignment assignment : gbitems) {
				for (String studentId : students) {
					GradeAssignmentItem item = new GradeAssignmentItem(
							assignment);
					item.setUserId(studentId);
					item.setUserName(getUserDisplayName(studentId));
					item.setGrade(gradebookService.getAssignmentScoreString(
							siteId, assignment.getId(), studentId));

					course.getAssignments().add(item);
				}
			}
			return course;

		} else {
			// students or the rest
			GradeCourse course = new GradeCourse(site);

			List<Assignment> gbitems = gradebookService
					.getViewableAssignmentsForCurrentUser(siteId);
			for (Assignment assignment : gbitems) {
				GradeAssignmentItem item = new GradeAssignmentItem(assignment);
				item.setUserId(userId);
				item.setUserName(getUserDisplayName(userId));
				item.setGrade(gradebookService.getAssignmentScoreString(siteId,
						assignment.getId(), userId));

				course.getAssignments().add(item);
			}
			return course;
		}
	}

	private Collection<String> getStudentList(String siteId) {
		// this only works in the post-2.5 gradebook -AZ
		// Let the gradebook tell use how it defines the students The
		// gradebookUID is the siteId
		String gbID = siteId;
		if (!gradebookService.isGradebookDefined(gbID)) {
			throw new IllegalArgumentException(
					"No gradebook found for course ("
							+ siteId
							+ "), gradebook must be installed in each course to use with this");
		}

		ArrayList<String> result = new ArrayList<String>();

		Map<String, String> studentToPoints = gradebookService.getImportCourseGrade(gbID);
		ArrayList<String> eids = new ArrayList<String>(studentToPoints.keySet());
				
		List<User> users = userDirectoryService.getUsersByEids(eids);
		for(User u: users) {
			result.add(u.getId());
		}
		
		Collections.sort(result);

		return result;
	}

	private String getUserDisplayName(String uid) {
		try {
			User user = userDirectoryService.getUser(uid);
			return user.getDisplayName();
		} catch (UserNotDefinedException e) {
			log.warn("Undefined user id (" + uid + ")");
			return null;
		}
	}

	@EntityCustomAction(action = "my", viewKey = EntityView.VIEW_LIST)
	public List<GradeCourse> getMyGradebook(EntityView view) {
		String userId = developerHelperService.getCurrentUserId();
		if (userId == null) {
			throw new SecurityException(
					"Only logged in users can access my gradebook listings");
		}

		List<GradeCourse> r = new ArrayList<GradeCourse>();

		// get list of all sites
		List<Site> sites = siteService.getSites(
				SiteService.SelectionType.ACCESS, null, null, null,
				SiteService.SortType.TITLE_ASC, null);
		// no need to check user can access this site, as the get sites only
		// returned accessible sites

		// get all assignments from each site
		for (Site site : sites) {
			String siteId = site.getId();
			if (!gradebookService.isGradebookDefined(siteId)) {
				continue;
			}

			GradeCourse course = new GradeCourse(site);

			List<Assignment> gbitems = gradebookService
					.getViewableAssignmentsForCurrentUser(siteId);
			for (Assignment assignment : gbitems) {
				GradeAssignmentItem item = new GradeAssignmentItem(assignment);
				item.setUserId(userId);
				item.setUserName(getUserDisplayName(userId));
				item.setGrade(gradebookService.getAssignmentScoreString(siteId,
						assignment.getId(), userId));

				course.getAssignments().add(item);
			}
			r.add(course);
		}

		return r;
	}

	@EntityCustomAction(action = "item", viewKey = EntityView.VIEW_LIST)
	public GradeAssignmentItemDetail getGradeItemDetails(EntityView view) {
		String userId = developerHelperService.getCurrentUserId();
		if (userId == null) {
			throw new SecurityException(
					"Only logged in users can access my gradebook listings");
		}

		String siteId = view.getPathSegment(2);
		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					String.format(
							"siteId must be set in order to get the details of a gradebook item, via the URL /%s/item/{siteId}/{assignmentName}",
							ENTITY_PREFIX));
		}

		String assignmentName = view.getPathSegment(3);
		if (StringUtils.isBlank(assignmentName)) {
			throw new IllegalArgumentException(
					String.format(
							"assignment name must be set in order to get the details of a gradebook item, via the URL /%s/item/{siteId}/{assignmentName}",
							ENTITY_PREFIX));
		}

		if (!gradebookService.isGradebookDefined(siteId)) {
			throw new IllegalArgumentException(String.format(
					"No gradebook for site %s", siteId));
		}

		// linear search, slow, but no API for non-admin/non-instructor to get a
		// single assignment
		List<Assignment> gbitems = gradebookService
				.getViewableAssignmentsForCurrentUser(siteId);
		for (Assignment assignment : gbitems) {
			if (assignment.getName().equals(assignmentName)) {
				CommentDefinition cd = gradebookService
						.getAssignmentScoreComment(siteId,
								assignment.getId(), userId);

				GradeAssignmentItemDetail item = new GradeAssignmentItemDetail(
						assignment, cd);
				item.setUserId(userId);
				item.setUserName(getUserDisplayName(userId));
				item.setGrade(gradebookService.getAssignmentScoreString(siteId,
						assignment.getId(), userId));

				return item;
			}
		}
		throw new IllegalArgumentException(String.format(
				"No assignment %s for site %s", assignmentName, siteId));
	}
	
	/**
	 * Batched provider
	 * 
	 * Note only super user/instructor in each site able to access this.
	 * 
	 * @param ref
	 * @param params
	 * @return
	 */
	@EntityCustomAction(action = "batch", viewKey = EntityView.VIEW_LIST)
	public List<GradebookData> getBatchGradebookData(EntityReference ref, Map<String,Object> params) {
		
		String rawSiteIds = (String) params.get("siteIds");
		
		// check siteIds supplied
		if (StringUtils.isBlank(rawSiteIds)) {
			throw new IllegalArgumentException(
					String.format("siteIds must be set in order to get the gradebooks, via the URL /%s/batch?siteIds={siteIds}",ENTITY_PREFIX));
		}
		
		List<String> siteIds = Arrays.asList(StringUtils.split(rawSiteIds, ','));
		
		String userId = developerHelperService.getCurrentUserId();
		if (userId == null) {
			throw new SecurityException("Only logged in users can access");
		}
		
		List<GradebookData> rval = new ArrayList<>();
		
		// for every passed in site get the students, the assignments and the grades for each student in each assignment, the categories, and map it all together
		siteIds.forEach(siteId -> {
			
			if (securityService.isSuperUser() || siteService.allowUpdateSite(siteId)) {
			
				Site site = this.getSite(siteId);
				Gradebook gradebook = this.getGradebook(siteId);
				
				GradebookData gradebookData = new GradebookData(site);
			
				//get gradeable students
				final List<String> studentUuids = new ArrayList<>(site.getUsersIsAllowed("section.role.student"));
				
				// Note: to make this user centric this could be changed to getViewableAssignmentsForCurrentUser though it does more checks.
				List<Assignment> assignments = this.gradebookService.getAssignments(siteId);
				assignments.forEach(assignment -> {					
					
					GradebookItem gradebookItem = new GradebookItem(assignment);

					final List<GradeDefinition> gradeDefinitions = this.gradebookService.getGradesForStudentsForItem(gradebook.getUid(), assignment.getId(), studentUuids);
					
					gradeDefinitions.forEach(def -> {						
						
						StudentGrade grade = new StudentGrade(def);
						gradebookItem.getGrades().add(grade);
					});
					
					gradebookData.getGradeItems().add(gradebookItem);
				});
				
				// add category info
				List<CategoryDefinition> categories = this.gradebookService.getCategoryDefinitions(gradebook.getUid());
				categories.forEach(def -> {
					Category cat = new Category(def);
					gradebookData.getCategories().add(cat);
				});
				
				rval.add(gradebookData);
			}
			
		});
		
		return rval;
		
	}
	
	/**
	 * Helper to get a gradebook for a site
	 * @param siteId
	 * @return
	 */
	private Gradebook getGradebook(final String siteId) {
		try {
			final Gradebook gradebook = (Gradebook) this.gradebookService.getGradebook(siteId);
			return gradebook;
		} catch (final GradebookNotFoundException e) {
			log.error("No gradebook in site: " + siteId);
			return null;
		}
	}
	
	/**
	 * Helper to get a site
	 * @param siteId
	 * @return
	 */
	private Site getSite(String siteId) {
		try {
			return siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			throw new IllegalArgumentException(String.format("Invalid siteId %s", siteId));
		}
	}
	
}
