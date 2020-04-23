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

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbChartData;
import org.sakaiproject.portal.util.PortalUtils;

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

	@SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
	protected ServerConfigurationService serverConfigService;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected transient GradebookNgBusinessService businessService;

	private transient SecurityAdvisor advisor;

	public BaseChart(final String id) {
		super(id);
		setOutputMarkupPlaceholderTag(true);
	}

	@Override
	public void renderHead(final IHeaderResponse response) {

		final String version = PortalUtils.getCDNQuery();

		// chart requires ChartJS
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/webjars/chartjs/2.7.0/Chart.min.js%s", version)));

		// our chart functions
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-chart.js%s", version)));

		// render immediately (for all subclasses)
		final GbChartData data = getData();
		response.render(OnLoadHeaderItem.forScript("renderChart('" + toJson(data) + "');"));
	}

	/**
	 * Get the data for the current instance
	 *
	 * @return
	 */
	protected abstract GbChartData getData();


	protected final void addAdvisor() {
		this.advisor = this.businessService.addSecurityAdvisor();
	}

	protected final void removeAdvisor() {
		this.businessService.removeSecurityAdvisor(this.advisor);
	}

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

