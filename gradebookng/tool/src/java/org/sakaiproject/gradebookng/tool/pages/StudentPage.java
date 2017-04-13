package org.sakaiproject.gradebookng.tool.pages;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.tool.panels.StudentGradeSummaryGradesPanel;
import org.sakaiproject.user.api.User;

/**
 *
 * The page that students get. Similar to the student grade summary panel that instructors see.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentPage extends BasePage {

	private static final long serialVersionUID = 1L;

	public StudentPage() {

		final User u = this.businessService.getCurrentUser();

		final Map<String, Object> userData = new HashMap<>();
		userData.put("userId", u.getId());
		userData.put("groupedByCategoryByDefault", true);

		add(new Label("heading", new StringResourceModel("heading.studentpage", null, new Object[] { u.getDisplayName() })));
		add(new StudentGradeSummaryGradesPanel("summary", Model.ofMap(userData)));
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

		// tablesorted used by student grade summary
		response.render(CssHeaderItem
			.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/css/theme.bootstrap.min.css?version=%s", version)));
		response.render(JavaScriptHeaderItem
			.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/js/jquery.tablesorter.min.js?version=%s", version)));
		response.render(JavaScriptHeaderItem
			.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/js/jquery.tablesorter.widgets.min.js?version=%s", version)));

		// GradebookNG Grade specific styles and behaviour
		response.render(
				CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-grades.css?version=%s", version)));
		response.render(
				CssHeaderItem.forUrl(
						String.format("/gradebookng-tool/styles/gradebook-print.css?version=%s", version),
						"print"));
		response.render(
				JavaScriptHeaderItem.forUrl(
						String.format("/gradebookng-tool/scripts/gradebook-grade-summary.js?version=%s", version)));
	}
}
