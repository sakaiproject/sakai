package org.sakaiproject.gradebookng.tool.panels.importExport;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chmaurer on 2/10/15.
 */
public class GradeImportConfirmationStep extends Panel {

    private static final Logger LOG = Logger.getLogger(GradeImportConfirmationStep.class);

    @SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    protected GradebookNgBusinessService businessService;

    private String panelId;
    private IModel<ImportWizardModel> model;

    public GradeImportConfirmationStep(String id, IModel<ImportWizardModel> importWizardModel) {
        super(id);
        this.panelId = id;
        this.model = importWizardModel;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        //unpack model
        final ImportWizardModel importWizardModel = this.model.getObject();

        final List<ProcessedGradeItem> itemsToCreate = importWizardModel.getItemsToCreate();
        final List<ProcessedGradeItem> itemsToUpdate = importWizardModel.getItemsToUpdate();
        final List<Assignment> assignmentsToCreate = importWizardModel.getAssignmentsToCreate();

        Form<?> form = new Form("form")
        {
            @Override
            protected void onSubmit()
            {
                boolean errors = false;
                //Create new GB items
                for (Assignment assignment : assignmentsToCreate) {
                    businessService.addAssignment(assignment);
                }

                List<ProcessedGradeItem> itemsToSave = new ArrayList<ProcessedGradeItem>();
                itemsToSave.addAll(itemsToUpdate);
                itemsToSave.addAll(itemsToCreate);
                for (ProcessedGradeItem processedGradeItem : itemsToSave) {
                    LOG.debug("Looping through items to save");
                    //TODO this loops through grades and comments separately...need to figure out a better way
                    for (ProcessedGradeItemDetail processedGradeItemDetail : processedGradeItem.getProcessedGradeItemDetails()) {
                        LOG.debug("Looping through detail items to save");
                        GradeSaveResponse saved = businessService.saveGrade(processedGradeItem.getItemId(), processedGradeItemDetail.getStudentUuid(),
                                processedGradeItemDetail.getGrade(), processedGradeItemDetail.getComment());
                        //Anything other than OK and NO_CHANGE is bad
                        if (!(saved == GradeSaveResponse.OK || saved == GradeSaveResponse.NO_CHANGE)) {
                            errors = true;
                        }
                        LOG.info("Saving grade: " + saved + ", " + processedGradeItem.getItemId() + ", " + processedGradeItemDetail.getStudentEid() + ", " +
                                processedGradeItemDetail.getGrade() + ", " + processedGradeItemDetail.getComment());
                    }
                }

                if (!errors) {
                    getSession().info(getString("importExport.confirmation.success"));
                    setResponsePage(new GradebookPage());
                } else {
                    getSession().error(getString("importExport.confirmation.failure"));
                }
            }
        };
        add(form);

        Button backButton = new Button("backbutton") {
            @Override
            public void onSubmit() {
                LOG.debug("Clicking back button...");
                Component newPanel = null;
                if (assignmentsToCreate.size() > 0)
                    newPanel = new CreateGradeItemStep(panelId, Model.of(importWizardModel));
                else
                    newPanel = new GradeItemImportSelectionStep(panelId, Model.of(importWizardModel));
                newPanel.setOutputMarkupId(true);
                GradeImportConfirmationStep.this.replaceWith(newPanel);


            }
        };
        backButton.setDefaultFormProcessing(false);
        form.add(backButton);

        form.add(new Button("finishbutton"));

        final boolean hasItemsToUpdate = !itemsToUpdate.isEmpty();
        WebMarkupContainer gradesUpdateContainer = new WebMarkupContainer ("grades_update_container") {
            public boolean isVisible() { return hasItemsToUpdate; }
        };
        add(gradesUpdateContainer);

        if (hasItemsToUpdate) {
            ListView<ProcessedGradeItem> updateList = makeListView("grades_update", itemsToUpdate);

            updateList.setReuseItems(true);
            gradesUpdateContainer.add(updateList);
        }

        final boolean hasItemsToCreate = !itemsToCreate.isEmpty();
        WebMarkupContainer gradesCreateContainer = new WebMarkupContainer ("grades_create_container") {
            public boolean isVisible() { return hasItemsToCreate; }
        };
        add(gradesCreateContainer);

        if (hasItemsToCreate) {
            ListView<ProcessedGradeItem> createList = makeListView("grades_create", itemsToCreate);

            createList.setReuseItems(true);
            gradesCreateContainer.add(createList);
        }
    }

    private ListView<ProcessedGradeItem> makeListView(String componentName, List<ProcessedGradeItem> itemList) {
        return new ListView<ProcessedGradeItem>(componentName, itemList) {
            /**
             * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
             */
            @Override
            protected void populateItem(ListItem<ProcessedGradeItem> item) {
                item.add(new Label("itemTitle", new PropertyModel<String>(item.getDefaultModel(), "itemTitle")));
                String naString = getString("importExport.selection.pointValue.na", new Model(), "N/A");
                if (naString.equals(item.getModelObject().getItemPointValue()))
                    item.add(new AttributeModifier("class", "comment"));
            }
        };
    }

}
