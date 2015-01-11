package org.sakaiproject.gradebookng.tool.panels;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * 
 * Header panel for each assignment column in the UI
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AssignmentColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;


	public AssignmentColumnHeaderPanel(String id, Assignment assignment) {
		super(id);
		
		add(new Label("title", new Model<String>(assignment.getName())));
		
		WebMarkupContainer averageGradeSection = new WebMarkupContainer("averageGradeSection");
		averageGradeSection.add(new Label("averagePoints", new Model("TODO")));
		averageGradeSection.add(new Label("totalPoints", new Model<Double>(assignment.getPoints())));
		averageGradeSection.setVisible(true);
		add(averageGradeSection);
		
		add(new Label("dueDate", new Model<String>(getDueDate(assignment.getDueDate()))));
		
		//menu
		//AjaxLink menu = new AjaxLink("menu", "http://google.com");
		//link.add(new Label("menuLabel"));
		//add(link);

	}
	
	
	private String getDueDate(Date assignmentDueDate) {
		//TODO locale formatting via ResourceLoader
		
		if(assignmentDueDate == null) {
			return null;
		}
		
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
    	return df.format(assignmentDueDate);
	}

}
