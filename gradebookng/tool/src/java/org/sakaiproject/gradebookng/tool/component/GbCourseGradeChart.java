package org.sakaiproject.gradebookng.tool.component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.sakaiproject.gradebookng.business.util.MessageHelper;
import org.sakaiproject.gradebookng.tool.model.GbChartData;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradeMappingDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;

/**
 * Panel that renders the course grade chart for a site.
 */
public class GbCourseGradeChart extends GbBaseChart {

	private static final long serialVersionUID = 1L;

	private final String siteId;

	public GbCourseGradeChart(final String id, final String siteId) {
		super(id);
		this.siteId = siteId;
	}

	/**
	 * Refresh the chart
	 *
	 * @param target the ajax target
	 * @param schema the schema to pass in so we can perform the refresh against that schema
	 */
	public void refresh(final AjaxRequestTarget target, final Map<String, Double> schema) {
		final GbChartData data = this.getData(schema);
		target.appendJavaScript("renderChart('" + toJson(data) + "')");
	}



	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		// render immediately
		final GbChartData data = this.getData();
		response.render(OnLoadHeaderItem.forScript("renderChart('" + toJson(data) + "');"));
	}

	/**
	 * Get chart data for this site
	 *
	 * @return
	 */
	private GbChartData getData() {
		final GradebookInformation info = this.businessService.getGradebookSettings(this.siteId);
		final Map<String, Double> gradingSchema = info.getSelectedGradingScaleBottomPercents();
		return getData(gradingSchema);
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

		// get the course grades and re-map to summary. Also sorts the data so it is ready for the consumer to use
		final Map<String, CourseGrade> courseGrades = this.businessService.getCourseGrades(this.siteId, schema);
		final GbChartData data = reMap(courseGrades, gradingSchema.keySet());

		// chart config
		data.setChartTitle(MessageHelper.getString("settingspage.gradingschema.chart.heading"));
		data.setXAxisLabel(MessageHelper.getString("settingspage.gradingschema.chart.xaxis"));
		data.setYAxisLabel(MessageHelper.getString("settingspage.gradingschema.chart.yaxis"));
		data.setChartType("horizontalBar");
		data.setChartId(this.getMarkupId());

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

