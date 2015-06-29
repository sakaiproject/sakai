package org.sakaiproject.gradebookng.tool.panels;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * 
 * Cell panel for the student grade summary
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentGradeSummaryPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private GbStudentGradeInfo gradeInfo;
	private ModalWindow window;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	public StudentGradeSummaryPanel(String id, IModel<Map<String,Object>> model, ModalWindow window) {
		super(id, model);
		
		this.window = window;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Map<String,Object> modelData = (Map<String,Object>) this.getDefaultModelObject();
		String userId = (String) modelData.get("userId");
		String displayName = (String) modelData.get("displayName");
		
		//build the grade matrix for the user
        final List<Assignment> assignments = this.businessService.getGradebookAssignments();
        
		//TODO catch if this is null, the get(0) will throw an exception
        //TODO also catch the GbException
        this.gradeInfo = this.businessService.buildGradeMatrix(assignments, Collections.singletonList(userId)).get(0);
		
		//assignment list
		ListDataProvider<Assignment> listDataProvider = new ListDataProvider<Assignment>(assignments);
        
        DataView<Assignment> dataView = new DataView<Assignment>("rows", listDataProvider) {

			private static final long serialVersionUID = 1L;

			@Override 
        	protected void populateItem(Item<Assignment> item) { 
        		Assignment assignment = item.getModelObject(); 
	    		RepeatingView repeatingView = new RepeatingView("dataRow");
	
	    		GbGradeInfo gradeInfo = StudentGradeSummaryPanel.this.gradeInfo.getGrades().get(assignment.getId());
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
	    		repeatingView.add(new Label(repeatingView.newChildId(), StudentGradeSummaryPanel.this.formatDueDate(assignment.getDueDate())));
	    		repeatingView.add(new Label(repeatingView.newChildId(), StudentGradeSummaryPanel.this.formatGrade(rawGrade))); 
	    		repeatingView.add(new Label(repeatingView.newChildId(), assignment.getWeight()));
	    		repeatingView.add(new Label(repeatingView.newChildId(), comment)); 

	    		item.add(repeatingView);
    		} 
        }; 
        add(dataView);
        
        //done button
        add(new AjaxLink<Void>("done") {
	       
			private static final long serialVersionUID = 1L;

			@Override
	        public void onClick(AjaxRequestTarget target){
	            window.close(target);
	        }
	    });
        
      //name
      add(new Label("name", displayName));

      		
      		
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
