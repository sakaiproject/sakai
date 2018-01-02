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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.wicket.model.IModel;
import org.sakaiproject.gradebookng.tool.model.GbGradeTableData;
import org.sakaiproject.component.cover.ServerConfigurationService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import org.sakaiproject.gradebookng.tool.model.GbGradebookData;
import org.sakaiproject.gradebookng.tool.actions.Action;
import java.util.HashMap;
import org.sakaiproject.gradebookng.tool.actions.ActionResponse;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

public class GbGradeTable extends Panel implements IHeaderContributor {

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

	public void renderHead(final IHeaderResponse response) {
		final GbGradeTableData gbGradeTableData = (GbGradeTableData) getDefaultModelObject();

		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-gbgrade-table.js?version=%s", version)));

		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/webjars/handsontable/0.26.1/dist/handsontable.full.min.js?version=%s", version)));

		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/webjars/handsontable/0.26.1/dist/handsontable.full.min.css?version=%s", version)));

		final GbGradebookData gradebookData = new GbGradebookData(
				gbGradeTableData,
				this);

		response.render(OnDomReadyHeaderItem.forScript(String.format("var tableData = %s", gradebookData.toScript())));

		response.render(OnDomReadyHeaderItem.forScript(String.format("GbGradeTable.renderTable('%s', tableData)",
				component.getMarkupId())));
	}
}
