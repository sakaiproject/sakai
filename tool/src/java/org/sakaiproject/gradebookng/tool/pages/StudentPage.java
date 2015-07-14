package org.sakaiproject.gradebookng.tool.pages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
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
        
        //get course grade
        CourseGrade cg = this.businessService.getCourseGrade(u.getId());
        
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
        //if entered grade show only that grade (as the percentage value will be for the calculated grade)
        //if mapped grade show both mapped grade and calculated percentage
  
        if(StringUtils.isBlank(cg.getEnteredGrade()) && StringUtils.isBlank(cg.getMappedGrade())) {
        	add(new Label("courseGrade", new ResourceModel("label.studentsummary.coursegrade.none")));
        } else if(StringUtils.isNotBlank(cg.getEnteredGrade())){
        	add(new Label("courseGrade", cg.getEnteredGrade()));
        } else {
        	add(new Label("courseGrade", new StringResourceModel("label.studentsummary.coursegrade.display", null, new Object[] { cg.getMappedGrade(), formatPercentage(cg.getCalculatedGrade()) } )));
        }
           		
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
	
	/**
	 * Format a percentage to two decimal places
	 * @param percentage
	 * @return
	 */
	private String formatPercentage(String percentage) {
		
		if(StringUtils.isBlank(percentage)) {
			return null;
		}
		
		BigDecimal rval = new BigDecimal(percentage);
		rval = rval.setScale(2, RoundingMode.HALF_DOWN); //same as GradebookService
		return rval.toString() + "%";
		
	}
	
	
	
}
