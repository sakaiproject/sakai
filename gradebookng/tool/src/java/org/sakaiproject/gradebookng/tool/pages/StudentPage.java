package org.sakaiproject.gradebookng.tool.pages;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
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
}
