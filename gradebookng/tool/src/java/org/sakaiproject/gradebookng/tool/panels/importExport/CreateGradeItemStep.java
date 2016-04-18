package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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

import lombok.extern.slf4j.Slf4j;

/**
 * Importer has detected that items need to be created so extract the data and wrap the 'AddOrEditGradeItemPanelContent' panel
 */
@Slf4j
public class CreateGradeItemStep extends Panel {

	private static final long serialVersionUID = 1L;

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

        //setup new assignment for populating
        Assignment assignment = new Assignment();
        assignment.setName(StringUtils.trim(processedGradeItem.getItemTitle()));
        assignment.setPoints(Double.parseDouble(processedGradeItem.getItemPointValue()));

        Model<Assignment> assignmentModel = new Model<>(assignment);

        @SuppressWarnings("unchecked")
		Form<Assignment> form = new Form("form", assignmentModel) {
			private static final long serialVersionUID = 1L;

			@Override
            protected void onSubmit() {
                List<Assignment> assignmentsToCreate = new ArrayList<Assignment>();

                Assignment a = (Assignment)getDefaultModel().getObject();

                if (a != null) {
                    assignmentsToCreate.add(assignment);
                }

                log.debug("Assignment: " + assignment);

                //Figure out if there are more steps
                //If so, go to the next step (ie do it all over again)
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
			private static final long serialVersionUID = 1L;

			@Override
            public void onSubmit() {
                log.debug("Clicking back button...");
                Component newPanel = null;
                if (step > 1) {
                    importWizardModel.setStep(step-1);
                    newPanel = new CreateGradeItemStep(panelId, Model.of(importWizardModel));
                }
                else {
                    newPanel = new GradeItemImportSelectionStep(panelId, Model.of(importWizardModel));
                }
                newPanel.setOutputMarkupId(true);
                CreateGradeItemStep.this.replaceWith(newPanel);


            }
        };
        backButton.setDefaultFormProcessing(false);
        form.add(backButton);

        //wrap the form create panel
        form.add(new Label("createItemHeader", new StringResourceModel("importExport.createItem.heading", this, null, step, importWizardModel.getTotalSteps())));
        form.add(new AddOrEditGradeItemPanelContent("subComponents", assignmentModel));

    }
}
