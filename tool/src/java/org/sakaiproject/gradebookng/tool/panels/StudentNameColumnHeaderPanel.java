package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

/**
 * 
 * Header panel for the student name/eid
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentNameColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<GbStudentNameSortOrder> model;

	public StudentNameColumnHeaderPanel(String id, IModel<GbStudentNameSortOrder> model) {
		super(id, model);
		this.model = model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//setup model
		final GbStudentNameSortOrder sortType = this.model.getObject();
		
		//title
		add(new Label("title", new ResourceModel("column.header.students")));
		
		//sort by first/last name link
		AjaxLink<GbStudentNameSortOrder> sortByName = new AjaxLink<GbStudentNameSortOrder>("sortByName", model){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				//get current sort
				GbStudentNameSortOrder currentSort = model.getObject();
				
				//get next
				GbStudentNameSortOrder newSort = currentSort.toggle();
				
				//set the sort
				GradebookPage gradebookPage = (GradebookPage) this.getPage();
				GradebookUiSettings settings = gradebookPage.getUiSettings();
				settings.setNameSortOrder(newSort);
				
				//save settings
				gradebookPage.setUiSettings(settings);
				
				//refresh
				setResponsePage(new GradebookPage());
				
			}
		};
		
		
		//the label changes depending on the state so we wrap it in a model
		IModel<String> sortByNameModel = new Model<String>(){
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
								
				//shows the label opposite to the current sort type
				if(sortType == GbStudentNameSortOrder.FIRST_NAME){
					return getString("sortbyname.option.last");
				} else {
					return getString("sortbyname.option.first");
				}
			}
		};
		
		
		sortByName.add(new Label("sortByNameLabel", sortByNameModel));
		add(sortByName);
	}
}
