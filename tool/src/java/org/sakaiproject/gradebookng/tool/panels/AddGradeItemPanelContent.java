package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.WebMarkupContainer;
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

import java.util.Date;
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
  
    public AddGradeItemPanelContent(String id, Model<Assignment> assignment) {
        super(id, assignment);

        add(new TextField<String>("title", new PropertyModel<String>(assignment, "name")));
        add(new TextField<Double>("points", new PropertyModel<Double>(assignment, "points")));
        add(new DateTextField("duedate", new PropertyModel<Date>(assignment, "dueDate"), "MM/dd/yyyy")); //TODO needs to come from i18n

        final List<CategoryDefinition> categories = businessService.getGradebookCategories();

        final Map<Long, String> categoryMap = new HashMap<>();
        for (CategoryDefinition category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }

        DropDownChoice<Long> categoryDropDown = new DropDownChoice<Long>("category", new PropertyModel<Long>(assignment, "categoryId"), new ArrayList<Long>(categoryMap.keySet()), new IChoiceRenderer<Long>() {
			private static final long serialVersionUID = 1L;

			public Object getDisplayValue(Long value) {
                return categoryMap.get(value);
            }

            public String getIdValue(Long object, int index) {
                return object.toString();
            }

        });
        categoryDropDown.setNullValid(true);
        categoryDropDown.setVisible(!categories.isEmpty());
        add(categoryDropDown);

        add(new WebMarkupContainer("noCategoriesMessage") {
            @Override
            public boolean isVisible() {
                return categories.isEmpty();
            }
        });

        add(new CheckBox("extraCredit", new PropertyModel<Boolean>(assignment, "extraCredit")));
       
        final AjaxCheckBox released = new AjaxCheckBox("released", new PropertyModel<Boolean>(assignment, "released")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        released.setOutputMarkupId(true);
        add(released);
        
        //if checked, release must also be checked and then disabled
        final AjaxCheckBox counted = new AjaxCheckBox("counted", new PropertyModel<Boolean>(assignment, "counted")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if(this.getModelObject()) {
					released.setModelObject(true);
					released.setEnabled(false);
				} else {
					released.setEnabled(true);
				}
				target.add(released);
			}
        };
        
        add(counted);
        
    }
}
