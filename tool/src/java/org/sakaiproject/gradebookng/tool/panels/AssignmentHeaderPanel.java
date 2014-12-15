package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class AssignmentHeaderPanel extends Panel {

	public AssignmentHeaderPanel(String id) {
		super(id);
		
		add(new Label("title", new Model("123123")));
		
		WebMarkupContainer averageGradeSection = new WebMarkupContainer("averageGradeSection");
		averageGradeSection.add(new Label("averagePoints", new Model("72")));
		averageGradeSection.add(new Label("totalPoints", new Model("100")));
		add(averageGradeSection);
		
		add(new Label("dueDate", new Model("01/12/14")));
		
		//menu
		//AjaxLink menu = new AjaxLink("menu", "http://google.com");
		//link.add(new Label("menuLabel"));
		//add(link);

	}

}
