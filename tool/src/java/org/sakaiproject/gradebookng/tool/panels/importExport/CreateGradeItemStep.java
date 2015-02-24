package org.sakaiproject.gradebookng.tool.panels.importExport;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.tool.panels.AddGradeItemPanelContent;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.util.List;

/**
 * Created by chmaurer on 2/15/15.
 */
public class CreateGradeItemStep extends Panel {

    private static final Logger LOG = Logger.getLogger(CreateGradeItemStep.class);

    private String panelId;

    public CreateGradeItemStep(String id, final int step, final int totalSteps, final List<ProcessedGradeItem> gbItemsToCreate,
                               final List<ProcessedGradeItem> itemsToCreate, final List<ProcessedGradeItem> itemsToUpdate,
                               final List<Assignment> assignmentsToCreate) {
        super(id);
        this.panelId = id;

        Form<?> form = new Form("form")
        {
            @Override
            protected void onSubmit()
            {
                LOG.debug("onSubmit()");
                Assignment assignment = (Assignment)getDefaultModel();
                assignmentsToCreate.add(assignment);
//                info("selected grade(s): " + form.getDefaultModelObjectAsString());
                //Figure out if there are more steps
                //If so, go to the next step
                Component newPanel = null;
                if (step < totalSteps) {
                    newPanel = new CreateGradeItemStep(panelId, step+1, totalSteps, gbItemsToCreate, itemsToCreate, itemsToUpdate, assignmentsToCreate);
                } else {
                    //TODO - capture gb items to create
                    //If not, continue on in the wizard
                    newPanel = new GradeImportConfirmationStep(panelId, itemsToCreate, itemsToUpdate, assignmentsToCreate);
                }
                    newPanel.setOutputMarkupId(true);
                    CreateGradeItemStep.this.replaceWith(newPanel);

            }
        };
        add(form);

        form.add(new Button("nextbutton"));

        Button cancel = new Button("backbutton"){
            public void onSubmit() {
                info("Back was pressed!");
//                setResponsePage(new GradebookPage());
            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);


        form.add(new Label("createItemHeader", new StringResourceModel("importExport.createItem.heading", this, null, step, totalSteps)));

        ProcessedGradeItem processedGradeItem = gbItemsToCreate.get(step - 1);
        AddGradeItemPanelContent gradePanelContent = new AddGradeItemPanelContent("subComponents", form,
                processedGradeItem.getItemTitle(), processedGradeItem.getItemPointValue());

        form.add(gradePanelContent);




    }
}
