package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbAssignmentModel;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * The panel for the add grade item window
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AddGradeItemPanelContent extends Panel {

	private static final long serialVersionUID = 1L;

    @SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    protected GradebookNgBusinessService businessService;
  
    public AddGradeItemPanelContent(String id, GbAssignmentModel gbAssignmentModel) {
        super(id, gbAssignmentModel);

        add(new TextField("title", new PropertyModel(gbAssignmentModel, "name")));
        add(new TextField("points", new PropertyModel(gbAssignmentModel, "points")));
        add(new DateTextField("duedate", new PropertyModel(gbAssignmentModel, "dueDate"), "MM/DD/yy"));
        //add(new DropDownChoice("category", new Model()));

        List<CategoryDefinition> categories = businessService.getGradebookCategories();
        List<String> categoryNames = new ArrayList<String>();
        for (CategoryDefinition category : categories) {
          categoryNames.add(category.getName());
        }
        add(new DropDownChoice("category", new PropertyModel(gbAssignmentModel, "categoryName"), categoryNames));

    }
}
