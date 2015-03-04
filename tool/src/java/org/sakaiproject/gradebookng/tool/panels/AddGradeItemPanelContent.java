package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.gradebookng.tool.model.GbAssignmentModel;

import java.util.Arrays;
import java.util.List;

/**
 * The panel for the add grade item window
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AddGradeItemPanelContent extends Panel {

	private static final long serialVersionUID = 1L;

    public AddGradeItemPanelContent(String id, GbAssignmentModel gbAssignmentModel) {
        super(id, gbAssignmentModel);

        add(new TextField("title", new PropertyModel(gbAssignmentModel, "name")));
        add(new TextField("points", new PropertyModel(gbAssignmentModel, "points")));
        add(new DateTextField("duedate", new PropertyModel(gbAssignmentModel, "dueDate"), "MM/DD/yy"));
        //add(new DropDownChoice("category", new Model()));
        add(new DropDownChoice("category", new PropertyModel(gbAssignmentModel, "categoryName"), Arrays.asList(new String[] { "A", "B", "C" })));

    }
	
	private class CategorySelectList extends DropDownChoice {

		public CategorySelectList(String id, Model m) {
			super(id);
			
			List categories = Arrays.asList(new String[] { "A", "B", "C" });
			
			this.setChoices(categories);
			
			
		}
		
	}

}
