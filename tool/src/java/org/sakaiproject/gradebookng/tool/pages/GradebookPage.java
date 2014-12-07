package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.XmlMarshaller;
import org.sakaiproject.gradebookng.business.dto.GradebookUserPreferences;
import org.sakaiproject.gradebookng.tool.model.StudentGrades;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.user.api.User;

import com.inmethod.grid.DataProviderAdapter;
import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.column.PropertyColumn;
import com.inmethod.grid.column.editable.EditablePropertyColumn;
import com.inmethod.grid.datagrid.DataGrid;
import com.inmethod.grid.datagrid.DefaultDataGrid;

/**
 * GB3 tester
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradebookPage extends BasePage {
	
	private static final long serialVersionUID = 1L;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public GradebookPage() {
		disableLink(this.gradebookPageLink);
		
		String currentUserUuid = this.businessService.getCurrentUserUuid();
		
		Form<Void> form = new Form<Void>("form");
		add(form);
		
		final List<StudentGrades> grades = businessService.buildGradeMatrix();
        final ListDataProvider<StudentGrades> listDataProvider = new ListDataProvider<StudentGrades>(grades);

        
        List<IGridColumn> cols = new ArrayList<IGridColumn>();
        
        //these properties need to match the studentgrades model
        cols.add(new PropertyColumn(new Model("Student Name"), "studentName", SortOrder.ASCENDING).setReorderable(false));
        cols.add(new PropertyColumn(new Model("Student ID"), "studentEid").setReorderable(false));
        cols.add(new PropertyColumn(new Model("Course Grade"), "courseGrade").setReorderable(false));
        
        
        //TODO lookup how many assignments we have and iterate here
        cols.add(new EditablePropertyColumn(new Model("Assignment 1"), "assignments.0"));
        cols.add(new EditablePropertyColumn(new Model("Assignment 2"), "assignments.1"));
        cols.add(new EditablePropertyColumn(new Model("Mid Term"), "assignments.2"));
        
        
        EditablePropertyColumn test1 = new EditablePropertyColumn(new Model("Assignment 4"), "assignments.3");
        cols.add(test1);

        //cols.add(new SubmitCancelColumn("form", Model.of("")));

        
        DataGrid grid = new DefaultDataGrid("grid", new DataProviderAdapter(listDataProvider), cols);
        form.add(grid);
        
        grid.setAllowSelectMultiple(false);
		grid.setSelectToEdit(false);
		grid.setClickRowToSelect(true);
		grid.setClickRowToDeselect(true);
	
		//testing the save and load
		GradebookUserPreferences prefs = new GradebookUserPreferences(currentUserUuid);
		prefs.setSortOrder(3);
		
		this.businessService.saveUserPrefs(prefs);
		
		GradebookUserPreferences crap = businessService.getUserPrefs();
		
	}
	
}
