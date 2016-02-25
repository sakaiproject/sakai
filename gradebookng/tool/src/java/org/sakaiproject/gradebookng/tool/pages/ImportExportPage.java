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
		response.render(JavaScriptHeaderItem.forUrl(String.format("/library/webjars/jquery-ui/1.11.3/jquery-ui.min.js?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/library/js/lang-datepicker/lang-datepicker.js?version=%s", version)));

		// Gradebook Import/Export styles
		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-importexport.css?version=%s", version)));
	}
}
