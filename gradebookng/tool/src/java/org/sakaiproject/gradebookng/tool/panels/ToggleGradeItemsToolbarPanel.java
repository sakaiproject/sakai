package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.xmlbeans.impl.piccolo.xml.Piccolo;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class ToggleGradeItemsToolbarPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public ToggleGradeItemsToolbarPanel(String id, final List<Assignment> assignments) {
    super(id);

    final List<String> categories = new ArrayList<String>();
    final Map<String, List<Assignment>> categoriesToAssignments = new HashMap<String, List<Assignment>>();
    
    Iterator<Assignment> assignmentIterator = assignments.iterator();
    while (assignmentIterator.hasNext()) {
      Assignment assignment = assignmentIterator.next();
      String category = assignment.getCategoryName() == null ? GradebookPage.UNCATEGORIZED : assignment.getCategoryName();
      
      if (!categoriesToAssignments.containsKey(category)) {
        categories.add(category);
        categoriesToAssignments.put(category, new ArrayList<Assignment>());
      }

      categoriesToAssignments.get(category).add(assignment);
    }

    Collections.sort(categories);

    add(new ListView<String>("categoriesList", categories) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void populateItem(ListItem<String> categoryItem) {
        final String category = categoryItem.getModelObject();

        final GradebookPage gradebookPage = (GradebookPage) this.getPage();

        categoryItem.add(new Label("category", category));

        CheckBox categoryCheckbox = new CheckBox("categoryCheckbox");
        categoryCheckbox.add(new AttributeModifier("value", category));
        categoryCheckbox.add(new AttributeModifier("checked", "checked"));
        categoryItem.add(categoryCheckbox);

        categoryItem.add(new ListView<Assignment>("assignmentsForCategory", categoriesToAssignments.get(category)) {
          private static final long serialVersionUID = 1L;

          @Override
          protected void populateItem(ListItem<Assignment> assignmentItem) {
            final Assignment assignment = assignmentItem.getModelObject();

            GradebookUiSettings settings = gradebookPage.getUiSettings();
            if (settings == null) {
              settings = new GradebookUiSettings();
              gradebookPage.setUiSettings(settings);
            }

            assignmentItem.add(new Label("assignmentTitle", assignment.getName()));

            CheckBox assignmentCheckbox = new AjaxCheckBox("assignmentCheckbox", Model.of(Boolean.valueOf(settings.isAssignmentVisible(assignment.getId())))) {
              @Override
              protected void onUpdate(AjaxRequestTarget target) {
                  GradebookUiSettings settings = gradebookPage.getUiSettings();
                  if (settings == null) {
                      settings = new GradebookUiSettings();
                  }

                  Boolean value = settings.isAssignmentVisible(assignment.getId());
                  settings.setAssignmentVisibility(assignment.getId(), !value);

                  gradebookPage.setUiSettings(settings);
              }
            };
            assignmentCheckbox.add(new AttributeModifier("value", assignment.getId().toString()));
            assignmentCheckbox.add(new AttributeModifier("data-colidx", assignments.indexOf(assignment)));
            assignmentItem.add(assignmentCheckbox);
          }
        });

        WebMarkupContainer categoryScoreFilter = new WebMarkupContainer("categoryScore");
        categoryScoreFilter.setVisible(category != GradebookPage.UNCATEGORIZED);
        categoryScoreFilter.add(new Label("categoryScoreLabel", new StringResourceModel("label.toolbar.categoryscorelabel", null, new String[] {category})));

        GradebookUiSettings settings = gradebookPage.getUiSettings();
        if (settings == null) {
          settings = new GradebookUiSettings();
          gradebookPage.setUiSettings(settings);
        }


          CheckBox categoryScoreCheckbox = new AjaxCheckBox("categoryScoreCheckbox", new Model<Boolean>(settings.isCategoryScoreVisible(category))) {//Model.of(Boolean.valueOf(settings.isCategoryScoreVisible(category)))) {
          @Override
          protected void onUpdate(AjaxRequestTarget target) {
            GradebookUiSettings settings = gradebookPage.getUiSettings();
            if (settings == null) {
              settings = new GradebookUiSettings();
            }
      
            Boolean value = settings.isCategoryScoreVisible(category);
            settings.setCategoryScoreVisibility(category, !value);
      
            gradebookPage.setUiSettings(settings);
          }
        };
        categoryScoreCheckbox.add(new AttributeModifier("value", category));
        categoryScoreFilter.add(categoryScoreCheckbox);

        categoryItem.add(categoryScoreFilter);
      }
    });
  }
}
