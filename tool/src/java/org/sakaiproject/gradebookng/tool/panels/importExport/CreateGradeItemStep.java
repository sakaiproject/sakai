package org.sakaiproject.gradebookng.tool.panels.importExport;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.GbAssignment;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.tool.model.GbAssignmentModel;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.panels.AddGradeItemPanelContent;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chmaurer on 2/15/15.
 */
public class CreateGradeItemStep extends Panel {

    private static final Logger LOG = Logger.getLogger(CreateGradeItemStep.class);

    private String panelId;

    public CreateGradeItemStep(String id, final ImportWizardModel importWizardModel) {
        super(id);
        this.panelId = id;

        final int step = importWizardModel.getStep();


        ProcessedGradeItem processedGradeItem = importWizardModel.getGbItemsToCreate().get(step - 1);
        Assignment assignment = new Assignment();
        assignment.setName(processedGradeItem.getItemTitle());
        assignment.setPoints(Double.parseDouble(processedGradeItem.getItemPointValue()));

        GbAssignmentModel gbAssignmentModel = new GbAssignmentModel(new GbAssignment(assignment));

        Form<GbAssignmentModel> form = new Form<GbAssignmentModel>("form", gbAssignmentModel)
        {
            @Override
            protected void onSubmit()
            {
                List<Assignment> assignmentsToCreate = new ArrayList<Assignment>();
                GbAssignmentModel submittedModel = (GbAssignmentModel)getDefaultModel();
                GbAssignment assignment = (GbAssignment)submittedModel.getObject();

                if (assignment != null)
                    assignmentsToCreate.add(assignment.convert2Assignment());
                LOG.debug("Assignment: " + assignment);
//                info("assignment: " + getDefaultModelObjectAsString());
                //Figure out if there are more steps
                //If so, go to the next step
                Component newPanel = null;
                importWizardModel.setAssignmentsToCreate(assignmentsToCreate);

                if (step < importWizardModel.getTotalSteps()) {
                    importWizardModel.setStep(step+1);
                    newPanel = new CreateGradeItemStep(panelId, importWizardModel);
                } else {
                    //If not, continue on in the wizard
                    newPanel = new GradeImportConfirmationStep(panelId, importWizardModel);
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
                    newPanel = new CreateGradeItemStep(panelId, importWizardModel);
                }
                else
                    newPanel = new GradeItemImportSelectionStep(panelId, importWizardModel);
                newPanel.setOutputMarkupId(true);
                CreateGradeItemStep.this.replaceWith(newPanel);


            }
        };
        backButton.setDefaultFormProcessing(false);
        form.add(backButton);

        form.add(new Label("createItemHeader", new StringResourceModel("importExport.createItem.heading", this, null, step, importWizardModel.getTotalSteps())));

        AddGradeItemPanelContent gradePanelContent = new AddGradeItemPanelContent("subComponents", gbAssignmentModel);

        form.add(gradePanelContent);

    }
}
