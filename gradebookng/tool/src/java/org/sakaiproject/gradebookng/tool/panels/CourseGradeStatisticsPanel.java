/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.tool.chart.CourseGradeChart;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.stats.CourseGradeStatistics;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;

/**
 * Renders the course grade graph in a modal window
 */
public class CourseGradeStatisticsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	public CourseGradeStatisticsPanel(final String id, final IModel<String> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize() {
		super.onInitialize();

		final String siteId = ((Model<String>) getDefaultModel()).getObject();

		CourseGradeStatisticsPanel.this.window.setTitle(new ResourceModel("label.statistics.title.coursegrade"));

		final CourseGradeChart chart = new CourseGradeChart("gradingSchemaChart", siteId);
		add(chart);

		final CourseGradeStatistics stats = new CourseGradeStatistics("stats", getData(siteId));
		add(stats);

		add(new GbAjaxLink<Void>("done") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				CourseGradeStatisticsPanel.this.window.close(target);
			}
		});
	}

	/**
	 * Get the course grade data for the site and wrap it
	 *
	 * @param siteId siteId to get data for
	 * @return
	 */
	private IModel<Map<String, Object>> getData(final String siteId) {
		final Map<String, Object> data = new HashMap<>();
		data.put("courseGradeMap", this.businessService.getCourseGrades(siteId));

		final GradebookInformation info = this.businessService.getGradebookSettings(siteId);
		data.put("gradingSchemaName", info.getGradeScale());
		data.put("bottomPercents", info.getSelectedGradingScaleBottomPercents());

		return Model.ofMap(data);
	}

}