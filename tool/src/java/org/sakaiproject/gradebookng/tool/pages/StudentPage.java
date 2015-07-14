package org.sakaiproject.gradebookng.tool.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			
	public StudentPage() {

		//get userId
		User u = this.businessService.getCurrentUser();
		
        //get grades
        final Map<Assignment, GbGradeInfo> grades = this.businessService.getGradesForStudent(u.getId());
        
        //TODO coursegrade
        //String courseGrade = this.businessService.getCourseGrade(u.getId());
        String courseGrade = "X";
        
		//assignment list
        List<Assignment> assignments = new ArrayList<Assignment>(grades.keySet());
		ListDataProvider<Assignment> listDataProvider = new ListDataProvider<Assignment>(assignments);
        
        DataView<Assignment> dataView = new DataView<Assignment>("rows", listDataProvider) {

			private static final long serialVersionUID = 1L;

			@Override 
        	protected void populateItem(Item<Assignment> item) { 
        		Assignment assignment = item.getModelObject(); 
	    		RepeatingView repeatingView = new RepeatingView("dataRow");
	
	    		GbGradeInfo gradeInfo = grades.get(assignment.getId());
	    		
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
	    		repeatingView.add(new Label(repeatingView.newChildId(), formatDueDate(assignment.getDueDate())));
	    		repeatingView.add(new Label(repeatingView.newChildId(), formatGrade(rawGrade))); 
	    		repeatingView.add(new Label(repeatingView.newChildId(), assignment.getWeight()));
	    		repeatingView.add(new Label(repeatingView.newChildId(), comment)); 

	    		item.add(repeatingView);
    		} 
        }; 
        add(dataView);

      //heading
      add(new Label("heading", new StringResourceModel("heading.studentpage", null, new Object[]{ u.getDisplayName() })));
		
      //course grade
      add(new Label("courseGrade", courseGrade));
       		
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
