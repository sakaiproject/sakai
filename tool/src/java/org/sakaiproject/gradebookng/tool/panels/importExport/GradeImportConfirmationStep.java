package org.sakaiproject.gradebookng.tool.panels.importExport;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.util.List;

/**
 * Created by chmaurer on 2/10/15.
 */
public class GradeImportConfirmationStep extends Panel {

    private static final Logger log = Logger.getLogger(GradeItemImportSelectionStep.class);

    @SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    protected GradebookNgBusinessService businessService;

    private String panelId;

    public GradeImportConfirmationStep(String id, List<ProcessedGradeItem> itemsToCreate, List<ProcessedGradeItem> itemsToUpdate,
                                       final List<Assignment> assignmentsToCreate) {
        super(id);
        this.panelId = id;

//        final CheckGroup<ImportedGrade> group = new CheckGroup<ImportedGrade>("group", new ArrayList<ImportedGrade>());

        Form<?> form = new Form("form")
        {
            @Override
            protected void onSubmit()
            {
//                info("selected grade(s): " + form.getDefaultModelObjectAsString());
                //Create new GB items
                for (Assignment assignment : assignmentsToCreate) {
                    businessService.addAssignmentToGradebook(assignment);
                }

            }
        };
        add(form);

        form.add(new Button("finishbutton"));

        Button cancel = new Button("backbutton"){
            public void onSubmit() {
                    info("Back was pressed!");
//                setResponsePage(new GradebookPage());
            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);

//        form.add(group);
//        group.add(new CheckGroupSelector("groupselector"));




        final boolean hasItemsToUpdate = !itemsToUpdate.isEmpty();
        WebMarkupContainer gradesUpdateContainer = new WebMarkupContainer ("grades_update_container") {
            public boolean isVisible() { return hasItemsToUpdate; }
        };
        add(gradesUpdateContainer);

        if (hasItemsToUpdate) {
            ListView<ProcessedGradeItem> updateList = new ListView<ProcessedGradeItem>("grades_update", itemsToUpdate) {
              /**
               * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
               */
              @Override
              protected void populateItem(ListItem<ProcessedGradeItem> item) {
                item.add(new Label("itemTitle", new PropertyModel<String>(item.getDefaultModel(), "itemTitle")));
              }
            };
  
            updateList.setReuseItems(true);
            gradesUpdateContainer.add(updateList);
        }

        final boolean hasItemsToCreate = !itemsToCreate.isEmpty();
        WebMarkupContainer gradesCreateContainer = new WebMarkupContainer ("grades_create_container") {
            public boolean isVisible() { return hasItemsToCreate; }
        };
        add(gradesCreateContainer);

        if (hasItemsToCreate) {
            ListView<ProcessedGradeItem> createList = new ListView<ProcessedGradeItem>("grades_create", itemsToCreate) {
              /**
               * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
               */
              @Override
              protected void populateItem(ListItem<ProcessedGradeItem> item) {
                item.add(new Label("itemTitle", new PropertyModel<String>(item.getDefaultModel(), "itemTitle")));
              }
            };
  
            createList.setReuseItems(true);
            gradesCreateContainer.add(createList);
        }
    }

}
