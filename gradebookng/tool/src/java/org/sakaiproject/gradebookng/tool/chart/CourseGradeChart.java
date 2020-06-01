/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.chart;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.sakaiproject.gradebookng.business.util.MessageHelper;
import org.sakaiproject.gradebookng.tool.model.GbChartData;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradeMappingDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;

/**
 * Panel that renders the course grade chart for a site.
 */
public class CourseGradeChart extends BaseChart {

	private static final long serialVersionUID = 1L;

	private final String siteId;

	private CourseGrade studentGrade;

	public CourseGradeChart(final String id, final String siteId, CourseGrade studentGrade) {
		super(id);
		this.siteId = siteId;
		this.studentGrade = studentGrade;
	}

	/**
	 * Refresh the chart
	 *
	 * @param target the ajax target
	 * @param schema the schema to pass in so we can perform the refresh against that schema
	 */
	public void refresh(final AjaxRequestTarget target, final Map<String, Double> schema) {
		final GbChartData data = this.getData(schema);
		target.appendJavaScript("renderChartData('" + toJson(data) + "')");
	}

	/**
	 * Get chart data for this site
	 *
	 * @return
	 */
	@Override
	protected GbChartData getData() {
		final GradebookInformation info = this.businessService.getGradebookSettings(this.siteId);
		final Map<String, Double> gradingSchema = info.getSelectedGradingScaleBottomPercents();

		final GbChartData data = getData(gradingSchema);

		data.setChartTitle(MessageHelper.getString("settingspage.gradingschema.chart.heading"));
		data.setXAxisLabel(MessageHelper.getString("settingspage.gradingschema.chart.xaxis"));
		data.setYAxisLabel(MessageHelper.getString("settingspage.gradingschema.chart.yaxis"));
		data.setChartType("horizontalBar");
		data.setChartId(this.getMarkupId());
		if (this.studentGrade != null) {
			data.setStudentGradeRange(this.studentGrade.getDisplayGrade());
		}

		return data;
	}

	/**
	 * Get chart data for this site with specified grading schema
	 *
	 * @param schema
	 * @return
	 */
	private GbChartData getData(final Map<String, Double> gradingSchema) {

		// ensure schema is sorted so the grade mapping works correctly
		final Map<String, Double> schema = GradeMappingDefinition.sortGradeMapping(gradingSchema);

		// get the course grades and re-map. Also sorts the data so it is ready for the consumer to use
		final Map<String, CourseGrade> courseGrades = this.businessService.getCourseGrades(this.siteId, schema);
		final GbChartData data = reMap(courseGrades, gradingSchema.keySet());

		return data;
	}

	/**
	 * Re-map the course grades returned from the business service into our {@link GbChartData} object for returning on the REST API.
	 *
	 * @param courseGrades map of student to course grade
	 * @param gradingSchema the grading schema that has the order
	 * @return
	 */
	private GbChartData reMap(final Map<String, CourseGrade> courseGrades, final Set<String> order) {
		final GbChartData data = new GbChartData();
		courseGrades.forEach((k, v) -> {
			data.add(v.getDisplayGrade());
		});

		// sort the map based on the ordered schema
		final Map<String, Integer> originalData = data.getDataset();
		final Map<String, Integer> sortedData = new LinkedHashMap<>();
		order.forEach(o -> {
			// data set must contain everything in the grading schema
			Integer value = originalData.get(o);
			if (value == null) {
				value = 0;
			}
			sortedData.put(o, value);
		});
		data.setDataset(sortedData);

		return data;
	}

}

