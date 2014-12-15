package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class GradeItemCellPanel extends Panel {

	public GradeItemCellPanel(String id) {
		super(id);
		
		add(new Label("grade", new Model("1")));
		
		//menu
		

	}

}
