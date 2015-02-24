package org.sakaiproject.gradebookng.tool.panels.importExport;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.gradebookng.business.model.ImportedGrade;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chmaurer on 1/22/15.
 */
public class GradeItemImportSelectionStep extends Panel {

    private static final Logger log = Logger.getLogger(GradeItemImportSelectionStep.class);

    private String panelId;

    public GradeItemImportSelectionStep(String id, List<ProcessedGradeItem> processedGradeItems) {
        super(id);
        this.panelId = id;

        final CheckGroup<ImportedGrade> group = new CheckGroup<ImportedGrade>("group", new ArrayList<ImportedGrade>());

        Form<?> form = new Form("form")
        {
            @Override
            protected void onSubmit()
            {
                info("selected grade(s): " + group.getDefaultModelObjectAsString());

                List<ProcessedGradeItem> processedGradeItems = (List<ProcessedGradeItem>)group.getDefaultModelObject();


                //Process the selected items into the create/update lists
                List<ProcessedGradeItem> itemsToUpdate = filterListByStatus(processedGradeItems,
                        Arrays.asList(new Integer[]{ProcessedGradeItem.STATUS_UPDATE, ProcessedGradeItem.STATUS_NA}));
                List<ProcessedGradeItem> itemsToCreate = filterListByStatus(processedGradeItems,
                        Arrays.asList(new Integer[] {ProcessedGradeItem.STATUS_NEW}));

                List<ProcessedGradeItem> gbItemsToCreate = new ArrayList<ProcessedGradeItem>();
                for (ProcessedGradeItem item : itemsToCreate) {
                    if (!"N/A".equals(item.getItemPointValue())) {
                        gbItemsToCreate.add(item);
                    }
                }

                //repaint panel
//                Component newPanel = new GradeImportConfirmationStep(panelId, (List<ProcessedGradeItem>)group.getDefaultModelObject());

                List<Assignment> assignmentsToCreate = new ArrayList<Assignment>();

                Component newPanel = new CreateGradeItemStep(panelId, 1, gbItemsToCreate.size(), gbItemsToCreate, itemsToCreate, itemsToUpdate, assignmentsToCreate);
                newPanel.setOutputMarkupId(true);
                GradeItemImportSelectionStep.this.replaceWith(newPanel);



            }
        };
        add(form);
        form.add(group);

        group.add(new Button("nextbutton"));

//        Button back = new Button("backbutton"){
//            public void onSubmit() {
//                //TODO - Can I get the state of the form back, including the uploaded file?  This just starts the page over.
//                setResponsePage(new ImportExportPage());
//            }
//        };
//        back.setDefaultFormProcessing(false);
//        group.add(back);


        group.add(new CheckGroupSelector("groupselector"));
        ListView<ProcessedGradeItem> gradeList = new ListView<ProcessedGradeItem>("grades",
                processedGradeItems)
        {
            /**
             * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
             */
            @Override
            protected void populateItem(ListItem<ProcessedGradeItem> item)
            {

                item.add(new Check<ProcessedGradeItem>("checkbox", item.getModel()));
                item.add(new Label("itemTitle",
                        new PropertyModel<String>(item.getDefaultModel(), "itemTitle")));
                item.add(new Label("itemPointValue", new PropertyModel<String>(item.getDefaultModel(),
                        "itemPointValue")));

                //Use the status code to look up the text representation
                PropertyModel<Integer> statusProp = new PropertyModel<Integer>(item.getDefaultModel(), "status");
                Integer status = statusProp.getObject();
                String statusValue = getString("importExport.status." + status);
                item.add(new Label("status", statusValue));
            }

        };

        gradeList.setReuseItems(true);
        group.add(gradeList);

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
