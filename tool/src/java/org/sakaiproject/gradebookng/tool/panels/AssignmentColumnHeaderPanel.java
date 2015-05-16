package org.sakaiproject.gradebookng.tool.panels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.pages.EditGradebookItemPage;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.apache.wicket.AttributeModifier;

/**
 * 
 * Header panel for each assignment column in the UI
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AssignmentColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private IModel<Assignment> modelData;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;

	public AssignmentColumnHeaderPanel(String id, IModel<Assignment> modelData) {
		super(id);
		
		this.modelData = modelData;
		
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Assignment assignment = this.modelData.getObject();
				
		Label assignmentTitle = new Label("title", new Model<String>(assignment.getName()));
		assignmentTitle.add(new AttributeModifier("title", assignment.getName()));
		add(assignmentTitle);
		
		WebMarkupContainer averageGradeSection = new WebMarkupContainer("averageGradeSection");
		averageGradeSection.add(new Label("averagePoints", new Model("TODO")));
		averageGradeSection.add(new Label("totalPoints", new Model<Double>(assignment.getPoints())));
		averageGradeSection.setVisible(true);
		add(averageGradeSection);
		
		add(new Label("dueDate", new Model<String>(getDueDate(assignment.getDueDate()))));

		WebMarkupContainer externalAppFlag = new WebMarkupContainer("externalAppFlag");
		if (assignment.getExternalAppName() == null) {
			externalAppFlag.setVisible(false);
		} else {
			externalAppFlag.setVisible(true);
			externalAppFlag.add(new AttributeModifier("title", getString("label.gradeitem.externalAppPrefix") + " " + assignment.getExternalAppName()));
			String iconClass = "icon-sakai";
			if ("Assignments".equals(assignment.getExternalAppName())) {
				iconClass = "icon-sakai-assignment-grades";
			} else if ("Tests & Quizzes".equals(assignment.getExternalAppName())) {
				iconClass = "icon-sakai-samigo";
			} else if ("Lesson Builder".equals(assignment.getExternalAppName())) {
				iconClass = "icon-sakai-lessonbuildertool";
			}
			externalAppFlag.add(new AttributeModifier("class", "gb-external-app-flag " + iconClass));
		}
		add(externalAppFlag);

		add(new WebMarkupContainer("extraCreditFlag").setVisible(assignment.isExtraCredit()));
		add(new WebMarkupContainer("isCountedFlag").setVisible(assignment.isCounted()));
		add(new WebMarkupContainer("notCountedFlag").setVisible(!assignment.isCounted()));
		add(new WebMarkupContainer("isReleasedFlag").setVisible(assignment.isReleased()));
		add(new WebMarkupContainer("notReleasedFlag").setVisible(!assignment.isReleased()));

		add(new AttributeModifier("data-assignmentId", assignment.getId()));
		add(new AttributeModifier("data-category", assignment.getCategoryName()));

		//menu
		add(new Link<Long>("editAssignmentDetails", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new EditGradebookItemPage(this.getModel()));
			}
		});
		
		add(new Link<Long>("viewAssignmentGradeStatistics", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new GradebookPage());
			}
		});
		
		add(new Link<Long>("moveAssignmentLeft", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				//given the id, get the assignment, get the sort order, then update and refresh
				//note that we cannot use the passed in assignment sort order in here
				//as we may have had an async reorder on the front end but not had the model data updated, 
				//so we just make sure we get it fresh
				
				long assignmentId = this.getModelObject();
				
				int order = businessService.getAssignmentSortOrder(assignmentId);
				businessService.updateAssignmentOrder(assignmentId, (order-1));

				setResponsePage(new GradebookPage());
			}
		});
		
				
		add(new Link<Long>("moveAssignmentRight", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				
				long assignmentId = this.getModelObject();
				
				int order = businessService.getAssignmentSortOrder(assignmentId);
				businessService.updateAssignmentOrder(assignmentId, (order+1));
				
				setResponsePage(new GradebookPage());
			}
		});
		
		
		add(new AjaxLink<Long>("hideAssignment", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			public void onClick(AjaxRequestTarget target) {
				long assignmentId = this.getModelObject();
				target.appendJavaScript("sakai.gradebookng.spreadsheet.hideGradeItemAndSyncToolbar('" + assignmentId + "');");
			}
			
			
		});
		

	}
	
	
	private String getDueDate(Date assignmentDueDate) {
		//TODO locale formatting via ResourceLoader
		
		if(assignmentDueDate == null) {
			return getString("label.noduedate");
		}
		
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
    	return df.format(assignmentDueDate);
	}

}
