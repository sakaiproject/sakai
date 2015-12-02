package org.sakaiproject.gradebookng.tool.panels.importExport;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                Map<String, Long> assignmentMap = new HashMap<>();
                //Create new GB items
                for (Assignment assignment : assignmentsToCreate) {
                    Long assignmentId = businessService.addAssignment(assignment);
                    assignmentMap.put(assignment.getName(), assignmentId);
                }

                List<ProcessedGradeItem> itemsToSave = new ArrayList<ProcessedGradeItem>();
                itemsToSave.addAll(itemsToUpdate);
                itemsToSave.addAll(itemsToCreate);
                for (ProcessedGradeItem processedGradeItem : itemsToSave) {
                    LOG.debug("Looping through items to save");
                    for (ProcessedGradeItemDetail processedGradeItemDetail : processedGradeItem.getProcessedGradeItemDetails()) {
                        LOG.debug("Looping through detail items to save");
                        Long assignmentId = processedGradeItem.getItemId();
                        if (assignmentId == null) {
                            //Should be a newly created gn item
                            assignmentId = assignmentMap.get(processedGradeItem.getItemTitle());
                        }
                        GradeSaveResponse saved = businessService.saveGrade(assignmentId, processedGradeItemDetail.getStudentUuid(),
                                processedGradeItemDetail.getGrade(), processedGradeItemDetail.getComment());

                        if (saved == GradeSaveResponse.NO_CHANGE) {
                            //Check for changed comments
                            String currentComment = businessService.getAssignmentGradeComment(assignmentId,
                                    processedGradeItemDetail.getStudentUuid());

                            currentComment = StringUtils.trimToNull(currentComment);
                            String newComment = StringUtils.trimToNull(processedGradeItemDetail.getComment());
                            if (!StringUtils.equals(currentComment, newComment)) {
                                boolean success = businessService.updateAssignmentGradeComment(assignmentId,
                                        processedGradeItemDetail.getStudentUuid(), newComment);
                                LOG.info("Saving comment: " + success + ", " + assignmentId + ", " + processedGradeItemDetail.getStudentEid() + ", " +
                                        processedGradeItemDetail.getComment());
                                if (!success) {
                                    errors = true;
                                }
                            }
                        } else if (saved != GradeSaveResponse.OK) {
                            //Anything other than OK is bad
                            errors = true;
                        }
                        LOG.info("Saving grade: " + saved + ", " + assignmentId + ", " + processedGradeItemDetail.getStudentEid() + ", " +
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

                PropertyModel<String> commentLabelProp = new PropertyModel<String>(item.getDefaultModel(), "commentLabel");
                final String commentLabel = commentLabelProp.getObject();

                item.add(new Behavior() {
                    @Override
                    public void afterRender(Component component) {
                        super.afterRender(component);
                        if(commentLabel != null){
                            component.getResponse().write(
                                "<tr class=\"comment\"><td class=\"item_title\"><span>" + commentLabel + "</span></td></tr>"
                            );
                        }
                    }
                });
            }
        };
    }

}
