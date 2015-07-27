package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

import java.util.Map;
import java.util.HashMap;
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
  
    public AddGradeItemPanelContent(String id, Model assignment) {
        super(id, assignment);

        add(new TextField("title", new PropertyModel(assignment, "name")));
        add(new TextField("points", new PropertyModel(assignment, "points")));
        add(new DateTextField("duedate", new PropertyModel(assignment, "dueDate"), "MM/DD/yy"));

        List<CategoryDefinition> categories = businessService.getGradebookCategories();

        final Map<Long, String> categoryMap = new HashMap<>();
        for (CategoryDefinition category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }

        DropDownChoice categoryDropDown = new DropDownChoice("category", new PropertyModel(assignment, "categoryId"), new ArrayList<Long>(categoryMap.keySet()), new IChoiceRenderer<Long>() {
            public Object getDisplayValue(Long value) {
                return categoryMap.get(value);
            }

            public String getIdValue(Long object, int index) {
                return object.toString();
            }
        });
        categoryDropDown.setNullValid(true);
        add(categoryDropDown);

        add(new CheckBox("extraCredit", new PropertyModel(assignment, "extraCredit")));
        add(new CheckBox("released", new PropertyModel(assignment, "released")));
        add(new CheckBox("counted", new PropertyModel(assignment, "counted")));
    }
}
