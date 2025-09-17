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
package org.sakaiproject.gradebookng.tool.actions;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.SortType;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class MoveAssignmentAction extends InjectableAction implements Serializable {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	private int movement;
	public MoveAssignmentAction(int mov) {
		movement = mov;
	}

	public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
		final GradebookPage gradebookPage = (GradebookPage) target.getPage();
		setCurrentGradebookAndSite(gradebookPage.getCurrentGradebookUid(), gradebookPage.getCurrentSiteId());

		final Long assignmentId = Long.valueOf(params.get("assignmentId").asText());

		GradebookUiSettings settings = gradebookPage.getUiSettings();

		if (settings == null) {
			settings = new GradebookUiSettings();
			gradebookPage.setUiSettings(settings);
		}

		if (settings.isCategoriesEnabled() && settings.isGroupedByCategory()) {
			try {
				final Integer order = calculateCurrentCategorizedSortOrder(assignmentId);
				businessService.updateAssignmentCategorizedOrder(currentGradebookUid, currentSiteId, assignmentId,
						(order.intValue() + movement));
			} catch (final Exception e) {
				return new ArgumentErrorResponse("Error reordering within category: " + e.getMessage());
			}
		} else {
			final int order = businessService.getAssignmentSortOrder(currentGradebookUid, currentSiteId, assignmentId.longValue());
			businessService.updateAssignmentOrder(currentGradebookUid, currentSiteId, assignmentId.longValue(), (order + movement));
		}

		// refresh the page
		target.appendJavaScript("location.reload();");

		return new EmptyOkResponse();
	}

	/**
	 * Get the assignment's current sort index within its category. If this value is null in the database, best calculate this index from
	 * the assignments.
	 *
	 * @param assignmentId the id of the assignment
	 * @return the current sort index of the assignment within their category
	 */
	protected Integer calculateCurrentCategorizedSortOrder(final Long assignmentId) {
		final Assignment assignment = businessService.getAssignment(currentGradebookUid, currentSiteId, assignmentId.longValue());
		Integer order = assignment.getCategorizedSortOrder();

		if (order == null) {
			// if no categorized order for assignment, calculate one based on the default sort order
			final List<Assignment> assignments = businessService.getGradebookAssignments(currentGradebookUid, currentSiteId, SortType.SORT_BY_SORTING);
			final List<Long> assignmentIdsInCategory = assignments.stream()
					.filter(a -> (a.getCategoryId() != null) && a.getCategoryId().equals(assignment.getCategoryId()))
					.map(Assignment::getId)
					.collect(Collectors.toList());

			order = assignmentIdsInCategory.indexOf(assignmentId);
		}

		return order;
	}

}
