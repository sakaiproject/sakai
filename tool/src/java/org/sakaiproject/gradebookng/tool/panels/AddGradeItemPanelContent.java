package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The panel for the add grade item window
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AddGradeItemPanelContent extends Panel {

	private static final long serialVersionUID = 1L;

	public AddGradeItemPanelContent(String id, Form form) {
        this(id, form, "", "");
	}

    public AddGradeItemPanelContent(String id, Form form, String title, String points) {
        super(id);

        Assignment assignment = new Assignment();
        assignment.setName(title);
        if (points != null && !"".equals(points))
            assignment.setPoints(Double.parseDouble(points));

        add(new TextField("title", new PropertyModel<String>(assignment, "name")));
        add(new TextField("points", new PropertyModel<Double>(assignment, "points")));
        add(new DateTextField("duedate", new PropertyModel<Date>(assignment, "dueDate"), "MM/DD/yy"));
        //add(new DropDownChoice("category", new Model()));
        add(new DropDownChoice("category", new PropertyModel<String>(assignment, "categoryName"), Arrays.asList(new String[] { "A", "B", "C" })));


    }
	
	private class CategorySelectList extends DropDownChoice {

		public CategorySelectList(String id, Model m) {
			super(id);
			
			List categories = Arrays.asList(new String[] { "A", "B", "C" });
			
			this.setChoices(categories);
			
			
		}
		
	}

}
