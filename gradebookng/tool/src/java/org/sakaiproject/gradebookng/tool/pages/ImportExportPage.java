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
package org.sakaiproject.gradebookng.tool.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.ExactLevelFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.gradebookng.tool.panels.importExport.GradeImportUploadStep;

/**
 * Import Export page
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ImportExportPage extends BasePage {

	private static final long serialVersionUID = 1L;

	public WebMarkupContainer container;

	// Confirmation page displays both SUCCESS and ERROR messages.
	// GbFeedbackPanels are styled with a single uniform background colour to represent a single 'error level' state.
	// Since multiple 'error level' states are present, it looks best separated as two different panels
	public final GbFeedbackPanel nonErrorFeedbackPanel = (GbFeedbackPanel) new GbFeedbackPanel("nonErrorFeedbackPanel").setFilter(new IFeedbackMessageFilter() {
		@Override
		public boolean accept(FeedbackMessage message) {
			return FeedbackMessage.ERROR != message.getLevel();
		}
	});

	public final GbFeedbackPanel errorFeedbackPanel = (GbFeedbackPanel) new GbFeedbackPanel("errorFeedbackPanel").setFilter(new ExactLevelFeedbackMessageFilter(FeedbackMessage.ERROR));

	public ImportExportPage() {

		defaultRoleChecksForInstructorOnlyPage();

		disableLink(this.importExportPageLink);

		container = new WebMarkupContainer("gradebookImportExportContainer");
		container.setOutputMarkupId(true);
		container.add(new GradeImportUploadStep("wizard"));
		add(container);

		// hide BasePage's feedback panel and use the error/nonError filtered feedback panels
		feedbackPanel.setVisibilityAllowed(false);
		add(nonErrorFeedbackPanel);
		add(errorFeedbackPanel);
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

		// Include Sakai Date Picker
		response.render(JavaScriptHeaderItem.forUrl(String.format("/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/library/js/lang-datepicker/lang-datepicker.js?version=%s", version)));

		// Gradebook Import/Export styles
		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-importexport.css?version=%s", version)));
	}

	@Override
	public void clearFeedback() {
		feedbackPanel.clear();
		nonErrorFeedbackPanel.clear();
		errorFeedbackPanel.clear();
	}

	public void updateFeedback(AjaxRequestTarget target) {
		target.add(nonErrorFeedbackPanel);
		target.add(errorFeedbackPanel);
	}
}
