package org.sakaiproject.gradebookng.tool.panels;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Gradebook;

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

        final Gradebook gradebook = businessService.getGradebook();

        add(new TextField<String>("title", new PropertyModel<String>(assignment, "name")));
        add(new TextField<Double>("points", new PropertyModel<Double>(assignment, "points")));
        add(new DateTextField("duedate", new PropertyModel<Date>(assignment, "dueDate"), "MM/dd/yyyy")); //TODO needs to come from i18n

        final List<CategoryDefinition> categories = businessService.getGradebookCategories();

        final Map<Long, CategoryDefinition> categoryMap = new HashMap<>();
        for (CategoryDefinition category : categories) {
            categoryMap.put(category.getId(), category);
        }

        final DropDownChoice<Long> categoryDropDown = new DropDownChoice<Long>("category", new PropertyModel<Long>(assignment, "categoryId"), new ArrayList<Long>(categoryMap.keySet()), new IChoiceRenderer<Long>() {
			private static final long serialVersionUID = 1L;

			public Object getDisplayValue(Long value) {
                CategoryDefinition category = categoryMap.get(value);
                if (GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY == gradebook.getCategory_type()) {
                    String weight = FormatHelper.formatDoubleAsPercentage(category.getWeight() * 100);
                    return MessageFormat.format(getString("label.addgradeitem.categorywithweight"), category.getName(), weight);
                } else {
                    return category.getName();
                }
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

        //if an extra credit category is selected, this will be unchecked and disabled
        final AjaxCheckBox extraCredit = new AjaxCheckBox("extraCredit", new PropertyModel<Boolean>(assignment, "extraCredit")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        extraCredit.setOutputMarkupId(true);
        add(extraCredit);
        
        final AjaxCheckBox released = new AjaxCheckBox("released", new PropertyModel<Boolean>(assignment, "released")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        released.setOutputMarkupId(true);
        released.setEnabled(!assignment.getObject().isCounted());
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
        
        //behaviour for when a category is chosen. If the category is extra credit, deselect and disable extra credit checkbox
        categoryDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				
				Long selected = (Long) categoryDropDown.getModelObject();
				
				//if extra credit, deselect and disable the 'extraCredit' checkbox
				CategoryDefinition category = categoryMap.get(selected);
				
				if(category.isExtraCredit()) {
					extraCredit.setModelObject(false);
					extraCredit.setEnabled(false);
				} else {
					extraCredit.setEnabled(true);
				}
				target.add(extraCredit);
			}
			
		});
        
        
    }
}
