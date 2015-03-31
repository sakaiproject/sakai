package org.sakaiproject.gradebookng.tool.panels;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
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
		//AjaxLink menu = new AjaxLink("menu", "http://google.com");
		//link.add(new Label("menuLabel"));
		//add(link);

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
