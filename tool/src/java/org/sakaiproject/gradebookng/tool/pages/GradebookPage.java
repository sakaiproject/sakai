package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.gradebookng.business.dto.GradebookUserPreferences;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;
import org.sakaiproject.gradebookng.tool.panels.AssignmentColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.GradeItemCellPanel;
import org.sakaiproject.gradebookng.tool.panels.SectionColumnHeaderPanel;
import org.sakaiproject.service.gradebook.shared.Assignment;

import com.inmethod.grid.DataProviderAdapter;
import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.column.AbstractColumn;
import com.inmethod.grid.column.PropertyColumn;
import com.inmethod.grid.column.editable.EditablePropertyColumn;
import com.inmethod.grid.datagrid.DataGrid;
import com.inmethod.grid.datagrid.DefaultDataGrid;

/**
 * Grades page
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradebookPage extends BasePage {
	
	private static final long serialVersionUID = 1L;

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public GradebookPage() {
		disableLink(this.gradebookPageLink);
		
		String currentUserUuid = this.businessService.getCurrentUserUuid();
		
		Form<Void> form = new Form<Void>("form");
		add(form);
		
        //get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
        final List<Assignment> assignments = this.businessService.getGradebookAssignments();
        
        //get the grade matrix
        final List<StudentGradeInfo> grades = businessService.buildGradeMatrix();
        
        //get the list of sections
        final List<Section> sections = this.businessService.getSiteSections();
        
        final ListDataProvider<StudentGradeInfo> listDataProvider = new ListDataProvider<StudentGradeInfo>(grades);
        List<IGridColumn> cols = new ArrayList<IGridColumn>();
        
        // match the studentgrades model
        cols.add(new PropertyColumn(new Model("Student Name"), "studentName", SortOrder.ASCENDING).setReorderable(false));
        cols.add(new PropertyColumn(new Model("Student ID"), "studentEid").setReorderable(false));
        
        //section column (only rendered if we have sections)
        if(!sections.isEmpty()){
	        AbstractColumn sectionColumn = new AbstractColumn("SECTION_COLUMN", new ResourceModel("column.header.section")) {
	
	        	@Override
	        	public Component newHeader(String componentId) {
	        		SectionColumnHeaderPanel panel = new SectionColumnHeaderPanel(componentId, sections);
					return panel;
	        		
	        	}
	        	
				@Override
				public Component newCell(WebMarkupContainer parent, String componentId, IModel rowModel) {
					return new EmptyPanel(componentId); //TODO
				}			
	        };
	        
	        cols.add(sectionColumn);
        }
        
        
        // match the studentgrades model
        cols.add(new PropertyColumn(new Model("Course Grade"), "courseGrade").setReorderable(false));
        
        
        //build the rest of the columns based on the assignment list
        for(final Assignment assignment: assignments) {
        	
        	AbstractColumn column = new AbstractColumn(String.valueOf(assignment.getId()), new Model(assignment.getName())) {

            	@Override
            	public Component newHeader(String componentId) {
            		AssignmentColumnHeaderPanel panel = new AssignmentColumnHeaderPanel(componentId, assignment);
    				return panel;
            		
            	}
            	
    			@Override
    			public Component newCell(WebMarkupContainer parent, String componentId, IModel rowModel) {
    				StudentGradeInfo studentsGrades = (StudentGradeInfo) rowModel.getObject();
    				GradeItemCellPanel panel = new GradeItemCellPanel(componentId, assignment.getId(), studentsGrades);
    				return panel;
    			}
    			
    			//TODO since we are now using a custom cell, we still need this to be editable, it will be done in the panel itself.

    			
            	
            };
            
            cols.add(column);
        	
        }
        //TODO lookup how many assignments we have and iterate here
        /*
        cols.add(new EditablePropertyColumn(new Model("Assignment 1"), "assignments.0"));
        cols.add(new EditablePropertyColumn(new Model("Assignment 2"), "assignments.1"));
        cols.add(new EditablePropertyColumn(new Model("Mid Term"), "assignments.2"));
        
        
        EditablePropertyColumn test1 = new EditablePropertyColumn(new Model("Assignment 4"), "assignments.3");
        cols.add(test1);

        AbstractColumn custom = new AbstractColumn("steve", new Model("steve")) {

        	@Override
        	public Component newHeader(String componentId) {
        		AssignmentHeaderPanel panel = new AssignmentHeaderPanel(componentId);
				return panel;
        		
        	}
        	
			@Override
			public Component newCell(WebMarkupContainer parent, String componentId, IModel rowModel) {
								
				//need a panel to represent a grade and the comment etc, add it here
				//pass in the data for the panel construction
				GradeItemCellPanel panel = new GradeItemCellPanel(componentId);
				
				return panel;
			}

			
        	
        };
        
        cols.add(custom);
        */
        
       
       
        
        DataGrid grid = new DefaultDataGrid("grid", new DataProviderAdapter(listDataProvider), cols);
        form.add(grid);
        
        grid.setAllowSelectMultiple(false);
		grid.setSelectToEdit(false);
		grid.setClickRowToSelect(true);
		grid.setClickRowToDeselect(true);
		
	
		//testing the save and load
		//GradebookUserPreferences prefs = new GradebookUserPreferences(currentUserUuid);
		//prefs.setSortOrder(3);
		//this.businessService.saveUserPrefs(prefs);
				
	}
	
	
	
}
