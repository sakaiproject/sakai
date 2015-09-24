package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.tool.gradebook.Gradebook;

public class CourseGradeColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public CourseGradeColumnHeaderPanel(String id) {
		super(id);

		add(new Label("title", new ResourceModel("column.header.coursegrade")));

		Gradebook gradebook = businessService.getGradebook();

		add(new WebMarkupContainer("isReleasedFlag").setVisible(gradebook.isCourseGradeDisplayed()));
		add(new WebMarkupContainer("notReleasedFlag").setVisible(!gradebook.isCourseGradeDisplayed()));
	}
}
