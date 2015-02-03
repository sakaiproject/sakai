package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * 
 * Cell panel for the student name and eid
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentNameCellPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public StudentNameCellPanel(String id, String name, String eid) {
		super(id);
		
		//name
		add(new Label("name", new Model<String>(name)));
		
		//eid
		//TODO make this configurable
		add(new Label("eid", new Model<String>(eid)));

		
		
	}
	
	
	
}
