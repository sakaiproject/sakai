/**
 * Copyright (c) 2003-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.tool.panels.importExport;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;

/**
 * This panel provides a table previewing the grades that will be imported for the current item in the {@link ImportWizardModel}.
 *
 * @author bjones86
 */
public class PreviewImportedGradesPanel extends Panel
{
    IModel<ImportWizardModel> model;

    public PreviewImportedGradesPanel( final String id, final IModel<ImportWizardModel> importWizardModel )
    {
        super( id, importWizardModel );
        model = importWizardModel;
    }

    @Override
    public void onInitialize()
    {
        super.onInitialize();
        final ImportWizardModel importWizardModel = model.getObject();
        final ProcessedGradeItem processedGradeItem = importWizardModel.getItemsToCreate().get( importWizardModel.getStep() - 1 );

        // Create the accordion header
        final Label previewGradesHeader = new Label( "previewGradesHeader", new StringResourceModel( "importExport.selection.previewGrades.header",
                null, new Object[] {processedGradeItem.getItemTitle()} ) );

        // Add the accordion collapse/expand behaviour
        final WebMarkupContainer previewGradesPanel = new WebMarkupContainer( "previewGradesPanel" );
        previewGradesPanel.add( new AjaxEventBehavior( "shown.bs.collapse" )
        {
            @Override
            protected void onEvent( final AjaxRequestTarget ajaxRequestTarget )
            {
                previewGradesPanel.add( new AttributeModifier( "class", "panel-collapse collapse in" ) );
            }
        });
        previewGradesPanel.add( new AjaxEventBehavior( "hidden.bs.collapse" )
        {
            @Override
            protected void onEvent( final AjaxRequestTarget ajaxRequestTarget )
            {
                previewGradesPanel.add( new AttributeModifier( "class", "panel-collapse collapse" ) );
            }
        });

        // Create and populate the list of grades that will be imported for the current item
        final WebMarkupContainer previewGradesContainer = new WebMarkupContainer( "previewGradesContainer" );
        final ListView<ProcessedGradeItemDetail> previewGrades = new ListView<ProcessedGradeItemDetail>( "previewGrades", processedGradeItem.getProcessedGradeItemDetails() )
        {
            @Override
            protected void populateItem( final ListItem<ProcessedGradeItemDetail> item )
            {
                final ProcessedGradeItemDetail details = item.getModelObject();
                final GbUser user = details.getUser();
                item.add( new Label( "studentID", user.getDisplayId() ) );
                item.add( new Label( "studentName", user.getDisplayName() ) );

                // Convert back to user's locale for display/validation purposes
                String grade = FormatHelper.formatGradeForDisplay( details.getGrade() );
                item.add( new Label( "studentGrade", grade ) );
            }
        };

        // Add the components to the page
        previewGradesContainer.add( previewGrades );
        previewGradesPanel.add( previewGradesContainer );
        add( previewGradesHeader );
        add( previewGradesPanel );
    }
}
