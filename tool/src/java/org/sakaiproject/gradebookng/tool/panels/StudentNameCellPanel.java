package org.sakaiproject.gradebookng.tool.panels;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * 
 * Cell panel for the student name and eid. Link shows the student grade summary
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentNameCellPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	String userId;
	String eid;
	String name;
	GradeSummaryWindow detailsWindow;
	
	IModel<Map<String,String>> model;

	public StudentNameCellPanel(String id, IModel<Map<String,String>> model) {
		super(id, model);
		this.model = model;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Map<String,String> modelData = (Map<String,String>) this.model.getObject();
		this.userId = modelData.get("userId");
		this.eid = modelData.get("eid");
		this.name = modelData.get("name");
		
		//link
		AjaxLink link = new AjaxLink("link") {
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				detailsWindow.show(target);
				
				//when this appears we want to alter the css so it is
				//opacity: 0.8; 
				//filter: alpha(opacity=80);  
				
				
			}
			
		};
		
		//name label
		link.add(new Label("name", name));
		
		//eid label, configurable
		link.add(new Label("eid", eid){
		
			public boolean isVisible() {
				return true; //TODO use config
			}
			
		});
		
		add(link);
		
		//details window
		detailsWindow = new GradeSummaryWindow("details", this.model);
		add(detailsWindow);

	}
	
	/**
	 * Window for viewing a student's grades
	 */
	private class GradeSummaryWindow extends ModalWindow {

		public GradeSummaryWindow(String componentId, IModel<Map<String,String>> model) {
			super(componentId);
			
			this.setContent(new StudentGradeSummaryPanel(this.getContentId(), model));
			this.setUseInitialHeight(false);

		}
		
	}
	
	
	
}
