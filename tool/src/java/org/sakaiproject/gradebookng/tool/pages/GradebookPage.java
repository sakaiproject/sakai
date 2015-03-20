package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.gradebookng.business.StudentSortOrder;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;
import org.sakaiproject.gradebookng.tool.panels.AddGradeItemPanel;
import org.sakaiproject.gradebookng.tool.panels.AssignmentColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.GradeItemCellPanel;
import org.sakaiproject.gradebookng.tool.panels.SectionColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.StudentNameCellPanel;
import org.sakaiproject.gradebookng.tool.panels.StudentNameColumnHeaderPanel;
import org.sakaiproject.service.gradebook.shared.Assignment;

//import com.inmethod.grid.DataProviderAdapter;
//import com.inmethod.grid.IGridColumn;
//import com.inmethod.grid.column.AbstractColumn;
//import com.inmethod.grid.column.PropertyColumn;
//import com.inmethod.grid.datagrid.DataGrid;
//import com.inmethod.grid.datagrid.DefaultDataGrid;

/**
 * Grades page
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradebookPage extends BasePage {
	
	private static final long serialVersionUID = 1L;
	
	AddGradeItemWindow addGradeItemWindow;
	Form<Void> form;

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public GradebookPage() {
		disableLink(this.gradebookPageLink);
		
		String currentUserUuid = this.businessService.getCurrentUserUuid();
		
		
		form = new Form<Void>("form");
		add(form);
		
		
		form.add(new AddGradeItemButton("addGradeItem"));
		
		addGradeItemWindow = new AddGradeItemWindow("addGradeItemWindow");
		form.add(addGradeItemWindow);
		
		
        //get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
        final List<Assignment> assignments = this.businessService.getGradebookAssignments();
        
        //get the grade matrix
        final List<StudentGradeInfo> grades = businessService.buildGradeMatrix(assignments);
        
        //get the list of sections
        final List<Section> sections = this.businessService.getSiteSections();
        
        final ListDataProvider<StudentGradeInfo> studentGradeMatrix = new ListDataProvider<StudentGradeInfo>(grades);
        List<IColumn> cols = new ArrayList<IColumn>();
        
        
        //student name column
        AbstractColumn studentNameColumn = new AbstractColumn(new Model("")) {

        	@Override
        	public Component getHeader(String componentId) {
        		return new StudentNameColumnHeaderPanel(componentId, StudentSortOrder.LAST_NAME);
        	}
        	
        	@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				StudentGradeInfo studentGradeInfo = (StudentGradeInfo) rowModel.getObject();
				
				Map<String,String> modelData = new HashMap<>();
				modelData.put("userId", studentGradeInfo.getStudentUuid());
				modelData.put("eid", studentGradeInfo.getStudentEid());
				modelData.put("name", studentGradeInfo.getStudentName());
				
				cellItem.add(new StudentNameCellPanel(componentId, Model.ofMap(modelData)));
				cellItem.add(new AttributeModifier("data-studentUuid", studentGradeInfo.getStudentUuid()));
				cellItem.add(new AttributeModifier("class", "gb-student-cell"));
			}

        };
        
        cols.add(studentNameColumn);
        
        
        
        //section column (only rendered if we have sections)
      
        if(!sections.isEmpty()){
	        AbstractColumn sectionColumn = new AbstractColumn(new ResourceModel("column.header.section")) {
	
	        	@Override
	        	public Component getHeader(String componentId) {
	        		SectionColumnHeaderPanel panel = new SectionColumnHeaderPanel(componentId, sections);
					return panel;
	        		
	        	}

				@Override
				public void populateItem(Item cellItem, String componentId, IModel rowModel) {
					cellItem.add(new EmptyPanel(componentId)); //TODO
				}
					
	        };
	        
	        cols.add(sectionColumn);
        }
       
        
        
        // pull from the studentgrades model
        cols.add(new PropertyColumn(new ResourceModel("column.header.coursegrade"), "courseGrade"));
        
        
        //build the rest of the columns based on the assignment list
        //NOTE: the ordering of newly created assignments and grouping by category will come into play here
       
        for(final Assignment assignment: assignments) {
        	
        	AbstractColumn column = new AbstractColumn(new Model("")) {

            	@Override
            	public Component getHeader(String componentId) {
            		AssignmentColumnHeaderPanel panel = new AssignmentColumnHeaderPanel(componentId, assignment);
    				return panel;
            	}

						@Override
						public String getCssClass() {
							return "gb-grade-item-header";
						}
            	
            	@Override
				public void populateItem(Item cellItem, String componentId, IModel rowModel) {
            		StudentGradeInfo studentsGrades = (StudentGradeInfo) rowModel.getObject();
            		cellItem.add(new GradeItemCellPanel(componentId, assignment.getId(), studentsGrades));
				}
            	
    			
    			//TODO since we are now using a custom cell, we still need this to be editable, it will be done in the panel itself.
            	
            };
            
            cols.add(column);
        	
        }
       
        
        //TODO make this AjaxFallbackDefaultDataTable
        DataTable table = new DataTable("table", cols, studentGradeMatrix, 8);
        table.addBottomToolbar(new NavigationToolbar(table));
        table.addTopToolbar(new HeadersToolbar(table, null));
        table.add(new AttributeModifier("data-siteid", this.businessService.getCurrentSiteId()));
        form.add(table);
       
        
        
	
		//testing the save and load
		//GradebookUserPreferences prefs = new GradebookUserPreferences(currentUserUuid);
		//prefs.setSortOrder(3);
		//this.businessService.saveUserPrefs(prefs);
				
	}
	
	/**
	 * Add grade button
	 */
	private class AddGradeItemButton extends AjaxButton {

		private static final long serialVersionUID = 1L;

		public AddGradeItemButton(String componentId) {
			super(componentId);
			this.setDefaultFormProcessing(false);			
		}
		
		
		@Override
		public void onSubmit(AjaxRequestTarget target, Form form) {
			
			//open window
			addGradeItemWindow.show(target);
		}
		
	}
	
	/**
	 * Window for adding a grade item
	 *
	 */
	private class AddGradeItemWindow extends ModalWindow {

		public AddGradeItemWindow(String componentId) {
			super(componentId);
			
			this.setContent(new AddGradeItemPanel(this.getContentId()));
			this.setUseInitialHeight(false);

		}
		
	}
	
	
	
}
