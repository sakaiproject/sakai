package org.sakaiproject.gradebookng.tool.component;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.util.FormatHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Panel that renders the course grade chart
 */
public class GbCourseGradeChart extends WebComponent {

	private static final long serialVersionUID = 1L;

	private final String siteId;

	public GbCourseGradeChart(final String id, final String siteId) {
		super(id);
		this.siteId = siteId;
		setOutputMarkupPlaceholderTag(true);
	}

	/**
	 * Refresh the chart
	 *
	 * @param target the ajax target
	 * @param schema the schema to pass in so we can perform the refresh against that schema
	 */
	public void refresh(final AjaxRequestTarget target, final Map<String, Double> schema) {
		final String schemaJson = toJson(schema);
		target.appendJavaScript("renderChart('" + this.siteId + "', '" + schemaJson + "')");
	}

	/**
	 * Convert map to json (encoded for a HTTP request)
	 *
	 * @param schema
	 * @return
	 */
	private String toJson(final Map<String, Double> schema) {
		final Gson gson = new GsonBuilder().create();
		final String json = gson.toJson(schema);
		return FormatHelper.encode(json);
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

		// chart requires ChartJS
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/webjars/chartjs/2.7.0/Chart.min.js?version=%s", version)));

		// our chart functions
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-coursegrade-chart.js?version=%s", version)));

		// do the initial rendering
		response.render(OnLoadHeaderItem.forScript("renderChart('" + this.siteId + "');"));
	}

}

