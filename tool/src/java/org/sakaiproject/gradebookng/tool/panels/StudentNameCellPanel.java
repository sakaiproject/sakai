package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
	
	StudentGradesDetailsWindow studentGradesDetailsWindow;

	public StudentNameCellPanel(String id, String name, String eid) {
		super(id);
		
		//change this to be a model passed in wrapping the details for the student
		
		//link
		StudentGradesDetailsLink detailsLink = new StudentGradesDetailsLink("link");
		add(detailsLink);

		//details
		studentGradesDetailsWindow = new StudentGradesDetailsWindow("details");
		add(studentGradesDetailsWindow);
		
	}
	
	private class StudentGradesDetailsLink extends AjaxLink {

		public StudentGradesDetailsLink(String id) {
			super(id);
			
			//TODO need a model passed in wrapping the details for the user
			
			add(new Label("name", new Model<String>("123")));
			
			//TODO make this configurable
			add(new Label("eid", new Model<String>("123123123")));
			
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			studentGradesDetailsWindow.show(target);
			
			//when this appears we want to alter the css so it is
			//opacity: 0.8; 
			//filter: alpha(opacity=80);  
			
			
		}
		
		
	}
	
	/**
	 * Window for viewing a student's grades
	 */
	private class StudentGradesDetailsWindow extends ModalWindow {

		public StudentGradesDetailsWindow(String componentId) {
			super(componentId);
			
			this.setContent(new AddGradeItemPanel(this.getContentId()));
			this.setUseInitialHeight(false);

		}
		
	}
	
	
	
}
