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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.tool.chart.AssignmentGradeChart;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.stats.AssignmentStatistics;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradebookInformation;

public class AssignmentStatisticsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	public AssignmentStatisticsPanel(final String id, final IModel<Long> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final Long assignmentId = ((Model<Long>) getDefaultModel()).getObject();

		final Assignment assignment = this.businessService.getAssignment(currentGradebookUid, currentSiteId, assignmentId.longValue());

		AssignmentStatisticsPanel.this.window.setTitle(
				new StringResourceModel("label.statistics.title.assignment").setParameters(assignment.getName()).getString());

		final AssignmentGradeChart chart = new AssignmentGradeChart("gradingSchemaChart", assignmentId, null);
		chart.setCurrentGradebookAndSite(currentGradebookUid, currentSiteId);
		add(chart);

		final AssignmentStatistics stats = new AssignmentStatistics("stats", getData(assignment));
		add(stats);

		add(new GbAjaxLink<Void>("done") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				AssignmentStatisticsPanel.this.window.close(target);
			}
		});

	}

	/**
	 * Get the grade data for the assignment and wrap it
	 *
	 * @param assignment assignment to get data for
	 * @return
	 */
	private IModel<Map<String, Object>> getData(final Assignment assignment) {
		final Map<String, Object> data = new HashMap<>();
		data.put("gradeInfo", this.businessService.buildGradeMatrix(currentGradebookUid, currentSiteId, 
				Arrays.asList(assignment),
				businessService.getGradeableUsers(currentGradebookUid, currentSiteId, null),
				null));
		data.put("assignmentId", assignment.getId());
        GradebookInformation info = businessService.getGradebookSettings(currentGradebookUid, currentSiteId);
        data.put("gradeMap", info.getSelectedGradingScaleBottomPercents());
        data.put("gradeType", info.getGradeType());
		return Model.ofMap(data);
	}

}
