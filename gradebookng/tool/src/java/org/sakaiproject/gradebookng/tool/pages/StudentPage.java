package org.sakaiproject.gradebookng.tool.pages;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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

		User u = this.businessService.getCurrentUser();

		Map<String, Object> userData = new HashMap<>();
		userData.put("userId", u.getId());

		add(new Label("heading", new StringResourceModel("heading.studentpage", null, new Object[]{u.getDisplayName()})));
		add(new StudentGradeSummaryGradesPanel("summary", Model.ofMap(userData)));
	}


	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		String version = ServerConfigurationService.getString("portal.cdn.version", "");

		//GradebookNG Grade specific styles and behaviour
		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-grades.css?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-grade-summary.js?version=%s", version)));
	}
}
