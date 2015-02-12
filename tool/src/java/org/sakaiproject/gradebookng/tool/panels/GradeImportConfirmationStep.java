package org.sakaiproject.gradebookng.tool.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chmaurer on 2/10/15.
 */
public class GradeImportConfirmationStep extends Panel {

    private static final Logger log = Logger.getLogger(GradeItemImportSelectionStep.class);

    private String panelId;

    public GradeImportConfirmationStep(String id, List<ProcessedGradeItem> processedGradeItems) {
        super(id);
        this.panelId = id;

//        final CheckGroup<ImportedGrade> group = new CheckGroup<ImportedGrade>("group", new ArrayList<ImportedGrade>());

        Form<?> form = new Form("form")
        {
            @Override
            protected void onSubmit()
            {
//                info("selected grade(s): " + form.getDefaultModelObjectAsString());

            }
        };
        add(form);
//        form.add(group);
//        group.add(new CheckGroupSelector("groupselector"));


        List<ProcessedGradeItem> itemsToUpdate = filterListByStatus(processedGradeItems,
                Arrays.asList(new Integer[] {ProcessedGradeItem.STATUS_UPDATE, ProcessedGradeItem.STATUS_NA}));
        List<ProcessedGradeItem> itemsToCreate = filterListByStatus(processedGradeItems,
                Arrays.asList(new Integer[] {ProcessedGradeItem.STATUS_NEW}));

        ListView<ProcessedGradeItem> updateList = new ListView<ProcessedGradeItem>("grades_update", itemsToUpdate)
        {
            /**
             * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
             */
            @Override
            protected void populateItem(ListItem<ProcessedGradeItem> item)
            {
                item.add(new Label("itemTitle", new PropertyModel<String>(item.getDefaultModel(), "itemTitle")));
            }
        };

        updateList.setReuseItems(true);
        add(updateList);

        ListView<ProcessedGradeItem> createList = new ListView<ProcessedGradeItem>("grades_create", itemsToCreate)
        {
            /**
             * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
             */
            @Override
            protected void populateItem(ListItem<ProcessedGradeItem> item)
            {
                item.add(new Label("itemTitle", new PropertyModel<String>(item.getDefaultModel(), "itemTitle")));
            }
        };

        createList.setReuseItems(true);
        add(createList);

    }

    private List<ProcessedGradeItem> filterListByStatus(List<ProcessedGradeItem> gradeList, List<Integer> statuses) {
        List<ProcessedGradeItem> filteredList = new ArrayList<ProcessedGradeItem>();
        for (ProcessedGradeItem gradeItem : gradeList) {
            if (statuses.contains(gradeItem.getStatus()))
                filteredList.add(gradeItem);
        }
        return filteredList;
    }

}
