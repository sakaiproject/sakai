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
package org.sakaiproject.gradebookng.tool.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.gradebookng.tool.actions.Action;
import org.sakaiproject.gradebookng.tool.actions.ActionResponse;
import org.sakaiproject.gradebookng.tool.model.GbGradeTableData;
import org.sakaiproject.gradebookng.tool.model.GbGradebookData;
import org.sakaiproject.portal.util.PortalUtils;

public class GbGradeTable extends Panel implements IHeaderContributor {

	@SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
	protected ServerConfigurationService serverConfigService;

	@SpringBean(name = "org.sakaiproject.grading.api.GradingService")
	protected GradingService gradingService;

	private Component component;

	/*
	 * - Students: id, first name, last name, netid - Course grades column: is released?, course grade - course grade value for each student
	 * (letter, percentage, points) - assignment header: id, points, due date, category {id, name, color}, included in course grade?,
	 * external? - categories: enabled? weighted categories? normal categories? handle uncategorized - scores: number, has comments?, extra
	 * credit? (> total points), read only?
	 */

	private Map<String, Action> listeners = new HashMap<String, Action>();

	public void addEventListener(final String event, final Action listener) {
		listeners.put(event, listener);
	}

	public ActionResponse handleEvent(final String event, final JsonNode params, final AjaxRequestTarget target) {
		if (!listeners.containsKey(event)) {
			throw new RuntimeException("Missing AJAX handler");
		}

		return listeners.get(event).handleEvent(params, target);
	}

	public GbGradeTable(final String id, final IModel model) {
		super(id);

		setDefaultModel(model);

		final String version = PortalUtils.getCDNQuery();

		Label messagerLabel = new Label("messagerScript", "");
		messagerLabel.add(new AttributeAppender("src", String.format("/webcomponents/bundles/gradebook.js%s", version)));
		add(messagerLabel);

		component = new WebMarkupContainer("gradeTable").setOutputMarkupId(true);

		component.add(new AjaxEventBehavior("gbgradetable.action") {
			@Override
			protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getDynamicExtraParameters()
						.add("return [{\"name\": \"ajaxParams\", \"value\": JSON.stringify(attrs.event.extraData)}]");
			}

			@Override
			protected void onEvent(final AjaxRequestTarget target) {
				try {
					final ObjectMapper mapper = new ObjectMapper();
					final JsonNode params = mapper.readTree(getRequest().getRequestParameters().getParameterValue("ajaxParams").toString());

					final ActionResponse response = handleEvent(params.get("action").asText(), params, target);

					target.appendJavaScript(String.format("GbGradeTable.ajaxComplete(%d, '%s', %s);",
							params.get("_requestId").intValue(), response.getStatus(), response.toJson()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});

		add(component);
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		final GbGradeTableData gbGradeTableData = (GbGradeTableData) getDefaultModelObject();

		final String version = PortalUtils.getCDNQuery();

		response.render(JavaScriptHeaderItem.forUrl("/library/js/view-preferences.js"));
		response.render(JavaScriptHeaderItem.forUrl("/library/js/sakai-reminder.js"));

		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/webjars/tabulator-tables/6.3.1/dist/js/tabulator.min.js%s", version)));
		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/webjars/tabulator-tables/6.3.1/dist/css/tabulator.min.css%s", version)));

		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-gbgrade-table.js%s", version)));

		final GbGradebookData gradebookData = new GbGradebookData(gbGradeTableData, this, gradingService);

		response.render(OnDomReadyHeaderItem.forScript(String.format("var tableData = %s", gradebookData.toScript())));

		response.render(OnDomReadyHeaderItem.forScript(String.format("GbGradeTable.renderTable('%s', tableData)",
				component.getMarkupId())));

		int sectionsColumnWidth = serverConfigService.getInt("gradebookng.sectionsColumnWidth", 140);
		int studentNumberColumnWidth = serverConfigService.getInt("gradebookng.studentNumberColumnWidth", 140);
		boolean allowColumnResizing = serverConfigService.getBoolean("gradebookng.allowColumnResizing", false);
		StringBuilder sb = new StringBuilder();
		sb.append("var sectionsColumnWidth = ").append(sectionsColumnWidth);
		sb.append(", allowColumnResizing = ").append(allowColumnResizing);
		sb.append(", studentNumberColumnWidth = ").append(studentNumberColumnWidth).append(";");
		response.render(JavaScriptHeaderItem.forScript(sb.toString(), null));
	}
}
