package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.StudentGrades;

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
	
	public GradebookPage() {
		disableLink(this.gradebookPageLink);
		
		Form<Void> form = new Form<Void>("form");
		add(form);
		
		final List<StudentGrades> personList = getGradebookItems();
        final ListDataProvider listDataProvider = new ListDataProvider(personList);

        
        List<IGridColumn> cols = new ArrayList<IGridColumn>();
        
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
	}
	
	private List<StudentGrades> getGradebookItems(){
		List<StudentGrades> items = new ArrayList<StudentGrades>();
		items.add(new StudentGrades(1, "Student 1","stdnt1","85", Arrays.asList("67", "", "", "")));
		items.add(new StudentGrades(2, "Student 2","stdnt2","78", Arrays.asList("56", "67", "", "")));
		items.add(new StudentGrades(3, "Student 3","stdnt3","56", Arrays.asList("73", "", "", "")));
		items.add(new StudentGrades(4, "Student 4","stdnt4","42", Arrays.asList("49", "", "", "")));
		items.add(new StudentGrades(5, "Student 5","stdnt5","93", Arrays.asList("44", "", "", "")));
		items.add(new StudentGrades(6, "Student 6","stdnt6","87", Arrays.asList("76", "", "", "")));
		return items;
	}
}
