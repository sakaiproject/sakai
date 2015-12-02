package org.sakaiproject.gradebookng.tool.panels.importExport;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.panels.AddOrEditGradeItemPanelContent;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chmaurer on 2/15/15.
 */
public class CreateGradeItemStep extends Panel {

    private static final Logger LOG = Logger.getLogger(CreateGradeItemStep.class);

    private String panelId;
    private IModel<ImportWizardModel> model;

    public CreateGradeItemStep(String id, IModel<ImportWizardModel> importWizardModel) {
        super(id);
        this.panelId = id;
        this.model = importWizardModel;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        //unpack model
        final ImportWizardModel importWizardModel = this.model.getObject();

        final int step = importWizardModel.getStep();


        ProcessedGradeItem processedGradeItem = importWizardModel.getGbItemsToCreate().get(step - 1);
        Assignment assignment = new Assignment();
        assignment.setName(processedGradeItem.getItemTitle());
        assignment.setPoints(Double.parseDouble(processedGradeItem.getItemPointValue()));

        Model assignmentModel = new Model(assignment);

        Form form = new Form("form", assignmentModel)
        {
            @Override
            protected void onSubmit()
            {
                List<Assignment> assignmentsToCreate = new ArrayList<Assignment>();
                Model submittedModel = (Model)getDefaultModel();
                Assignment assignment = (Assignment)submittedModel.getObject();

                if (assignment != null)
                    assignmentsToCreate.add(assignment);
                LOG.debug("Assignment: " + assignment);
//                info("assignment: " + getDefaultModelObjectAsString());
                //Figure out if there are more steps
                //If so, go to the next step
                Component newPanel = null;
                importWizardModel.setAssignmentsToCreate(assignmentsToCreate);

                if (step < importWizardModel.getTotalSteps()) {
                    importWizardModel.setStep(step+1);
                    newPanel = new CreateGradeItemStep(panelId, Model.of(importWizardModel));
                } else {
                    //If not, continue on in the wizard
                    newPanel = new GradeImportConfirmationStep(panelId, Model.of(importWizardModel));
                }
                    newPanel.setOutputMarkupId(true);
                    CreateGradeItemStep.this.replaceWith(newPanel);

            }
        };
        add(form);

        Button backButton = new Button("backbutton") {
            @Override
            public void onSubmit() {
                LOG.debug("Clicking back button...");
                Component newPanel = null;
                if (step > 1) {
                    importWizardModel.setStep(step-1);
                    newPanel = new CreateGradeItemStep(panelId, Model.of(importWizardModel));
                }
                else
                    newPanel = new GradeItemImportSelectionStep(panelId, Model.of(importWizardModel));
                newPanel.setOutputMarkupId(true);
                CreateGradeItemStep.this.replaceWith(newPanel);


            }
        };
        backButton.setDefaultFormProcessing(false);
        form.add(backButton);

        form.add(new Label("createItemHeader", new StringResourceModel("importExport.createItem.heading", this, null, step, importWizardModel.getTotalSteps())));

        AddOrEditGradeItemPanelContent gradePanelContent = new AddOrEditGradeItemPanelContent("subComponents", assignmentModel);

        form.add(gradePanelContent);

    }
}
