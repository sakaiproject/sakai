package org.sakaiproject.gradebookng.tool.pages;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.user.api.User;

/**
 * 
 * The page that students get. Similar to the student grade summary panel that instructors see.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentPage extends BasePage {

	private static final long serialVersionUID = 1L;
	
	private GbStudentGradeInfo gradeInfo;
		
	public StudentPage() {

		//get userId
		User u = this.businessService.getCurrentUser();
		
		//build the grade matrix for the user
        final List<Assignment> assignments = this.businessService.getGradebookAssignments();
        
		//TODO catch if this is null, the get(0) will throw an exception
        //TODO also catch the GbException
        this.gradeInfo = this.businessService.buildGradeMatrix(assignments, Collections.singletonList(u.getId())).get(0);
		
		//assignment list
		ListDataProvider<Assignment> listDataProvider = new ListDataProvider<Assignment>(assignments);
        
        DataView<Assignment> dataView = new DataView<Assignment>("rows", listDataProvider) {

			private static final long serialVersionUID = 1L;

			@Override 
        	protected void populateItem(Item<Assignment> item) { 
        		Assignment assignment = item.getModelObject(); 
	    		RepeatingView repeatingView = new RepeatingView("dataRow");
	
	    		GbGradeInfo gradeInfo = StudentPage.this.gradeInfo.getGrades().get(assignment.getId());
	    		//TODO exception handling in here
	    		
	    		//note, gradeInfo may be null
	    		String rawGrade;
	    		String comment;
	    		if(gradeInfo != null) {
	    			rawGrade = gradeInfo.getGrade();
	    			comment = gradeInfo.getGradeComment();
	    		} else {
	    			rawGrade = "";
	    			comment = "";
	    		}
	    		
	    		repeatingView.add(new Label(repeatingView.newChildId(), assignment.getName()));
	    		repeatingView.add(new Label(repeatingView.newChildId(), StudentPage.this.formatDueDate(assignment.getDueDate())));
	    		repeatingView.add(new Label(repeatingView.newChildId(), StudentPage.this.formatGrade(rawGrade))); 
	    		repeatingView.add(new Label(repeatingView.newChildId(), assignment.getWeight()));
	    		repeatingView.add(new Label(repeatingView.newChildId(), comment)); 

	    		item.add(repeatingView);
    		} 
        }; 
        add(dataView);

      //heading
      add(new Label("heading", new StringResourceModel("heading.studentpage", this, Model.of(u.getDisplayName()))));
		
      //course grade
      add(new Label("courseGrade", this.gradeInfo.getCourseGrade()));
       		
	}
	
	/**
	 * Format a due date
	 * 
	 * @param assignmentDueDate
	 * @return
	 */
	private String formatDueDate(Date date) {
		//TODO locale formatting via ResourceLoader
		
		if(date == null) {
			return getString("label.studentsummary.noduedate");
		}
		
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
    	return df.format(date);
	}
	
	/**
	 * Format a grade to remove the .0 if present.
	 * @param grade
	 * @return
	 */
	private String formatGrade(String grade) {
		return StringUtils.removeEnd(grade, ".0");		
	}
	
	
	
	
}
