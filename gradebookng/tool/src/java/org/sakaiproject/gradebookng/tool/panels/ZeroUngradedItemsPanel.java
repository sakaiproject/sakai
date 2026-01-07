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
package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.SortType;

/**
 *
 * Panel for the modal window that allows an instructor to zero the ungraded scores for all gradebook items
 *
 */
public class ZeroUngradedItemsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	private static final double ZERO_GRADE = 0;

	public ZeroUngradedItemsPanel(final String id, final ModalWindow window) {
		super(id);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final GbAjaxButton submit = new GbAjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target) {

				GradeType gradeType = ZeroUngradedItemsPanel.this.businessService.getGradebook(currentGradebookUid, currentSiteId).getGradeType();

				// fetch all assignments
				final List<Assignment> assignments = ZeroUngradedItemsPanel.this.businessService.getGradebookAssignments(currentGradebookUid, currentSiteId, SortType.SORT_BY_SORTING);

				for (final Assignment assignment : assignments) {
					final long assignmentId = assignment.getId().longValue();
					ZeroUngradedItemsPanel.this.businessService.updateUngradedItems(currentGradebookUid, currentSiteId, assignmentId, FormatHelper.formatGradeForDisplay(ZERO_GRADE, gradeType), null);
				}

				ZeroUngradedItemsPanel.this.window.close(target);
				setResponsePage(GradebookPage.class);
			}
		};
		add(submit);

		final GbAjaxButton cancel = new GbAjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target) {
				ZeroUngradedItemsPanel.this.window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
		add(cancel);
	}
}
