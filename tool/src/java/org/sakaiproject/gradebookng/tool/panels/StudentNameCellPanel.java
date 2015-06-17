package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.gradebookng.business.model.GbStudentSortType;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

/**
 * 
 * Cell panel for the student name and eid. Link shows the student grade summary
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentNameCellPanel extends Panel {

	private static final long serialVersionUID = 1L;
		
	IModel<Map<String,Object>> model;

	public StudentNameCellPanel(String id, IModel<Map<String,Object>> model) {
		super(id, model);
		this.model = model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Map<String,Object> modelData = (Map<String,Object>) this.model.getObject();
		String userId = (String) modelData.get("userId");
		String eid = (String) modelData.get("eid");
		String firstName = (String) modelData.get("firstName");
		String lastName = (String) modelData.get("lastName");
		GbStudentSortType sortType = (GbStudentSortType) modelData.get("sortType");
		
		
		//link
		AjaxLink<String> link = new AjaxLink<String>("link") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				
				GradebookPage gradebookPage = (GradebookPage) this.getPage();
				final ModalWindow window = gradebookPage.getStudentGradeSummaryWindow();
				
				window.setContent(new StudentGradeSummaryPanel(window.getContentId(), model, window));
				window.show(target);
			}
			
		};
		
		//name label
		link.add(new Label("name", getFormattedStudentName(firstName, lastName, sortType)));
		
		//eid label, configurable
		link.add(new Label("eid", eid){
		
			private static final long serialVersionUID = 1L;

			public boolean isVisible() {
				return true; //TODO use config, will need to be passed in the model map
			}
			
		});
		
		add(link);
		
		getParent().add(new AttributeModifier("scope", "row"));
		getParent().add(new AttributeModifier("role", "rowheader"));

	}
	
	
	/**
	 * Helper to format a student name based on the sort type.
	 * 
	 * Sorted by Last Name = Smith, John (jsmith26)
   	 * Sorted by First Name = John Smith (jsmith26)
   	 * 
	 * @param firstName
	 * @param lastName
	 * @param sortType
	 * @return
	 */
	private String getFormattedStudentName(String firstName, String lastName, GbStudentSortType sortType) {
		
		String msg = "formatter.studentname." + sortType.name();
		if(GbStudentSortType.LAST_NAME == sortType) {
			return String.format(getString(msg), lastName, firstName);
		}
		if(GbStudentSortType.FIRST_NAME == sortType) {
			return String.format(getString(msg), firstName, lastName);
		}
		return firstName;
	}
	
	
	
}
