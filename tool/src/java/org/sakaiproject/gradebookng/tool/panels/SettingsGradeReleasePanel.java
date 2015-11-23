package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbSettings;

public class SettingsGradeReleasePanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<GbSettings> model;
	
	public SettingsGradeReleasePanel(String id, IModel<GbSettings> model) {
		super(id, model);
		this.model = model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
				
		//display released items to students
        final AjaxCheckBox displayReleased = new AjaxCheckBox("displayReleased", new PropertyModel<Boolean>(model, "gradebookInformation.displayReleasedGradeItemsToStudents")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        displayReleased.setOutputMarkupId(true);
        add(displayReleased);
        
        //display course grade
        final AjaxCheckBox displayCourseGrade = new AjaxCheckBox("displayCourseGrade", new PropertyModel<Boolean>(model, "gradebookInformation.courseGradeDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        displayCourseGrade.setOutputMarkupId(true);
        add(displayCourseGrade);
        
        //course grade type container
        final WebMarkupContainer courseGradeType = new WebMarkupContainer("courseGradeType") {
			private static final long serialVersionUID = 1L;

			@Override
        	public boolean isVisible() {
        		return displayCourseGrade.getModelObject();
        	}
        	
        };
        courseGradeType.setOutputMarkupPlaceholderTag(true);
        add(courseGradeType);
        
        //letter grade
        final AjaxCheckBox letterGrade = new AjaxCheckBox("letterGrade", new PropertyModel<Boolean>(model, "gradebookInformation.courseLetterGradeDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        letterGrade.setOutputMarkupId(true);
        courseGradeType.add(letterGrade);
        
        //percentage
        final AjaxCheckBox percentage = new AjaxCheckBox("percentage", new PropertyModel<Boolean>(model, "gradebookInformation.courseAverageDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        percentage.setOutputMarkupId(true);
        courseGradeType.add(percentage);
        
        //points
        final AjaxCheckBox points = new AjaxCheckBox("points", new PropertyModel<Boolean>(model, "gradebookInformation.coursePointsDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        points.setOutputMarkupId(true);
        courseGradeType.add(points);
        
        //behaviour for when the 'display course grade' checkbox is changed
        displayCourseGrade.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				
				boolean checked = displayCourseGrade.getModelObject();
				courseGradeType.setVisible(checked);
				target.add(courseGradeType);
				
			}
		});
		
	}
}
