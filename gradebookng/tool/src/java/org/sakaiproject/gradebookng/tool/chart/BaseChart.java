package org.sakaiproject.gradebookng.tool.chart;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbChartData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * Base panel for gradebook charts. See {@link CourseGradeChart} or {@link AssignmentGradeChart}.
 * 
 * Immediately renders itself with the base data. Subclasses may provide a refresh option.
 */
@Slf4j
public abstract class BaseChart extends WebComponent {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected transient GradebookNgBusinessService businessService;

	public BaseChart(final String id) {
		super(id);
		setOutputMarkupPlaceholderTag(true);
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

		// chart requires ChartJS
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/webjars/chartjs/2.7.0/Chart.min.js?version=%s", version)));

		// our chart functions
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-chart.js?version=%s", version)));

		// render immediately (for all subclasses)
		final GbChartData data = this.getData();
		response.render(OnLoadHeaderItem.forScript("renderChart('" + toJson(data) + "');"));
	}
	
	/**
	 * Get the data for the current instance
	 * 
	 * @return
	 */
	protected abstract GbChartData getData();

	
	/**
	 * Helper to convert data to json
	 *
	 * @param data
	 * @return
	 */
	protected String toJson(final GbChartData data) {
		final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		final String json = gson.toJson(data);
		log.debug(json);
		return json;
	}

}

