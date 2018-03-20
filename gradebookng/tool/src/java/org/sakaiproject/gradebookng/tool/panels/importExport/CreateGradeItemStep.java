package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.gradebookng.tool.panels.AddOrEditGradeItemPanelContent;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;
import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.extern.slf4j.Slf4j;

/**
 * Importer has detected that items need to be created so extract the data and wrap the 'AddOrEditGradeItemPanelContent' panel
 */
@Slf4j
public class CreateGradeItemStep extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final String panelId;
    private final IModel<ImportWizardModel> model;

    public CreateGradeItemStep(final String id, final IModel<ImportWizardModel> importWizardModel) {
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

        // original data
        final ProcessedGradeItem processedGradeItem = importWizardModel.getItemsToCreate().get(step - 1);

        // setup new assignment for populating
        final Assignment assignment = new Assignment();
        assignment.setName(StringUtils.trim(processedGradeItem.getItemTitle()));
        if(StringUtils.isNotBlank(processedGradeItem.getItemPointValue())) {
        	assignment.setPoints(Double.parseDouble(processedGradeItem.getItemPointValue()));
        }

        final Model<Assignment> assignmentModel = new Model<>(assignment);

		final Form<Assignment> form = new Form<Assignment>("form", assignmentModel) {
			private static final long serialVersionUID = 1L;

			@Override
            protected void onSubmit() {

                final Assignment newAssignment = (Assignment)getDefaultModel().getObject();

                log.debug("Assignment: " + newAssignment);

                boolean validated = true;

                // validate name is unique
                final List<Assignment> existingAssignments = CreateGradeItemStep.this.businessService.getGradebookAssignments();
                existingAssignments.addAll(importWizardModel.getAssignmentsToCreate());

                if(!assignmentNameIsUnique(existingAssignments, newAssignment.getName())) {
                	validated = false;
                	error(getString("error.addgradeitem.title"));
                }

                if (validated) {
	                //add to model
	                importWizardModel.getAssignmentsToCreate().add(newAssignment);

	                // sync up the assignment data so we can present it for confirmation
	                processedGradeItem.setAssignmentTitle(newAssignment.getName());
	                processedGradeItem.setAssignmentPoints(newAssignment.getPoints());

	                //Figure out if there are more steps
	                //If so, go to the next step (ie do it all over again)
	                Component newPanel = null;
	                if (step < importWizardModel.getTotalSteps()) {
	                    importWizardModel.setStep(step+1);
	                    newPanel = new CreateGradeItemStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
	                } else {
	                    //If not, continue on in the wizard
	                    newPanel = new GradeImportConfirmationStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
	                }

	                // clear any previous errors
					final ImportExportPage page = (ImportExportPage) getPage();
					page.clearFeedback();

	                newPanel.setOutputMarkupId(true);
	                CreateGradeItemStep.this.replaceWith(newPanel);
                }

            }
        };
        add(form);

        final Button backButton = new Button("backbutton") {
			private static final long serialVersionUID = 1L;

			@Override
            public void onSubmit() {

				// clear any previous errors
				final ImportExportPage page = (ImportExportPage) getPage();
				page.clearFeedback();

                Component newPanel = null;
                if (step > 1) {
                    importWizardModel.setStep(step-1);
                    newPanel = new CreateGradeItemStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
                }
                else {
                    newPanel = new GradeItemImportSelectionStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
                }

                newPanel.setOutputMarkupId(true);
                CreateGradeItemStep.this.replaceWith(newPanel);
            }
        };
        backButton.setDefaultFormProcessing(false);
        form.add(backButton);

        final Button cancelButton = new Button("cancelbutton") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                // clear any previous errors
                final ImportExportPage page = (ImportExportPage) getPage();
                page.clearFeedback();

                setResponsePage(ImportExportPage.class);
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);

        //wrap the form create panel
        form.add(new Label("createItemHeader", new StringResourceModel("importExport.createItem.heading", this, null, step, importWizardModel.getTotalSteps())));
        form.add(new AddOrEditGradeItemPanelContent("subComponents", assignmentModel));

    }

    /**
     * Checks if an assignment is unique given a list of existing assignments
     * @param assignments
     * @param name
     * @return
     */
    private boolean assignmentNameIsUnique(final List<Assignment> assignments, final String name) {
    	return !(assignments
    			.stream()
    			.filter(a -> StringUtils.equals(a.getName(), name))
    			.findFirst()
    			.isPresent());
    }
}
