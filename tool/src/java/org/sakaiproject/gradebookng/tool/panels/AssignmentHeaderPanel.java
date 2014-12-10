package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class AssignmentHeaderPanel extends Panel {

	public AssignmentHeaderPanel(String id) {
		super(id);
		
		add(new Label("grade", new Model("123123")));
		
		add(new Label("menu", new Model("sdsadasdas")));

	}

}
