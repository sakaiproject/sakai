package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbChartData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * Base panel for gradebook charts. See {@link GbCourseGradeChart} or {@link GbAssignmentGradeChart}.
 */
@Slf4j
public abstract class GbBaseChart extends WebComponent {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected transient GradebookNgBusinessService businessService;

	public GbBaseChart(final String id) {
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

	}

	/**
	 * Convert data to json
	 *
	 * @param data
	 * @return
	 */
	protected String toJson(final GbChartData data) {
		final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		final String json = gson.toJson(data);
		
		log.info(json);
		
		return json;
	}

}

