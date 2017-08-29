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

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.tool.panels.importExport.GradeImportUploadStep;

/**
 * Import Export page
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ImportExportPage extends BasePage {

	private static final long serialVersionUID = 1L;

	public ImportExportPage() {
		disableLink(this.importExportPageLink);

		add(new GradeImportUploadStep("wizard"));
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

		// Include Sakai Date Picker
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/library/js/lang-datepicker/lang-datepicker.js?version=%s", version)));

		// Gradebook Import/Export styles
		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-importexport.css?version=%s", version)));
	}
}
