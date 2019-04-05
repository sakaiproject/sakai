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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.tool.chart.AssignmentGradeChart;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.service.gradebook.shared.Assignment;

public class StudentAssignmentStatisticsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;
	private String studentGrade;

	public StudentAssignmentStatisticsPanel(final String id,
			final IModel<Assignment> model, final ModalWindow window,
			String studentGrade) {
		super(id, model);
		this.window = window;
		this.studentGrade = studentGrade;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final Assignment assignment = ((Model<Assignment>) getDefaultModel()).getObject();

		StudentAssignmentStatisticsPanel.this.window.setTitle(
				new StringResourceModel("label.statistics.title.assignment",
						getDefaultModel(), null, assignment.getName()).getString());

		final AssignmentGradeChart chart = new AssignmentGradeChart("chart",
				assignment.getId(), studentGrade);
		add(chart);

		add(new GbAjaxLink<Void>("done") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				StudentAssignmentStatisticsPanel.this.window.close(target);
			}
		});

	}
}