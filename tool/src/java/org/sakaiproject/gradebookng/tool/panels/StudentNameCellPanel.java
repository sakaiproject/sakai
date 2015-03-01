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
	
	DetailsWindow detailsWindow;

	public StudentNameCellPanel(String id, String name, String eid) {
		super(id);
		
		//change this to be a model passed in wrapping the details for the student
		
		// also need to have the config passed in
		
		//link
		add(new DetailsLink("link"));
		
		//eid
		add(new EidLabel("eid", eid));

		//details window
		detailsWindow = new DetailsWindow("details");
		add(detailsWindow);
		
	}
	
	private class DetailsLink extends AjaxLink {

		private static final long serialVersionUID = 1L;

		public DetailsLink(String id) {
			super(id);
			
			//TODO need a model passed in wrapping the details for the user
			add(new Label("name", new Model<String>("123")));
			
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			detailsWindow.show(target);
			
			//when this appears we want to alter the css so it is
			//opacity: 0.8; 
			//filter: alpha(opacity=80);  
			
			
		}
		
	}
	
	/**
	 * Label for showing a user's eid. Configurable.
	 *
	 */
	private class EidLabel extends Label {

		public EidLabel(String id, String label) {
			super(id, label);

		}
		
		public boolean isVisible() {
			return true; //TODO use config
		}
		
	}
	
	/**
	 * Window for viewing a student's grades
	 */
	private class DetailsWindow extends ModalWindow {

		public DetailsWindow(String componentId) {
			super(componentId);
			
			//TODO point to correct data
			this.setContent(new AddGradeItemPanel(this.getContentId()));
			this.setUseInitialHeight(false);

		}
		
	}
	
	
	
}
