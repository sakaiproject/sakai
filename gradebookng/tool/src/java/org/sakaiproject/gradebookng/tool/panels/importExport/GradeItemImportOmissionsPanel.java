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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

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

import org.sakaiproject.gradebookng.business.importExport.UserIdentificationReport;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;

/**
 * This panel provides two tables displaying students in the Gradebook that were not found in the imported file, and students found in the imported file
 * but are not in the Gradebook. The tables are conditionally displayed based on the count of users in each table. If neither table has data to present,
 * the entire panel is hidden.
 *
 * @author bjones86
 */
public class GradeItemImportOmissionsPanel extends Panel
{
    IModel<ImportWizardModel> model;

    public GradeItemImportOmissionsPanel( final String id, final IModel<ImportWizardModel> importWizardModel )
    {
        super( id, importWizardModel );
        model = importWizardModel;
    }

    @Override
    public void onInitialize()
    {
        super.onInitialize();
        UserIdentificationReport report = model.getObject().getUserReport();
        final WebMarkupContainer omissionsContainer = new WebMarkupContainer( "omissionsContainer" );

        // Create the accordion headers and unknown users supplemental information message
        final Label missingUsersHeader;
        final Label unknownUsersHeader;
        final Label unknownUsersInfo;
        String missingUsersHeaderMsgKey = "importExport.selection.omissions.missingUsers.header.singular";
        String unknownUsersHeaderMsgKey = "importExport.selection.omissions.unknownUsers.header.singular";
        String unknownUsersInfoMsgKey = "importExport.selection.omissions.unknownUsers.info.singular";
        final int numMissingUsers = report.getMissingUsers().size();
        final int numUnknownUsers = report.getUnknownUsers().size();
        if( numMissingUsers > 1 )
        {
            missingUsersHeaderMsgKey = "importExport.selection.omissions.missingUsers.header.plural";
        }
        if( numUnknownUsers > 1 )
        {
            unknownUsersHeaderMsgKey = "importExport.selection.omissions.unknownUsers.header.plural";
            unknownUsersInfoMsgKey = "importExport.selection.omissions.unknownUsers.info.plural";
        }
        missingUsersHeader = new Label( "missingUsersHeader", new StringResourceModel( missingUsersHeaderMsgKey, null, numMissingUsers ) );
        unknownUsersHeader = new Label( "unknownUsersHeader", new StringResourceModel( unknownUsersHeaderMsgKey, null, numUnknownUsers ) );
        unknownUsersInfo = new Label( "unknownUsersInfo", new StringResourceModel( unknownUsersInfoMsgKey, null, new Object[] {} ) );

        // Missing users accordion and collapse/expand behaviour
        final WebMarkupContainer missingUsersContainer = new WebMarkupContainer( "missingUsersContainer" );
        missingUsersContainer.add( new AjaxEventBehavior( "shown.bs.collapse" )
        {
            @Override
            protected void onEvent( final AjaxRequestTarget ajaxRequestTarget )
            {
                missingUsersContainer.add( new AttributeModifier( "class", "panel-collapse collapse in" ) );
            }
        });
        missingUsersContainer.add( new AjaxEventBehavior( "hidden.bs.collapse" )
        {
            @Override
            protected void onEvent( final AjaxRequestTarget ajaxRequestTarget )
            {
                missingUsersContainer.add( new AttributeModifier( "class", "panel-collapse collapse" ) );
            }
        });

        // Unknown users accordion and collapse/expand behaviour
        final WebMarkupContainer unknownUsersContainer = new WebMarkupContainer( "unknownUsersContainer" );
        unknownUsersContainer.add( new AjaxEventBehavior( "shown.bs.collapse" )
        {
            @Override
            protected void onEvent( final AjaxRequestTarget ajaxRequestTarget )
            {
                unknownUsersContainer.add( new AttributeModifier( "class", "panel-collapse collapse in" ) );
            }
        });
        unknownUsersContainer.add( new AjaxEventBehavior( "hidden.bs.collapse" )
        {
            @Override
            protected void onEvent( final AjaxRequestTarget ajaxRequestTarget )
            {
                unknownUsersContainer.add( new AttributeModifier( "class", "panel-collapse collapse" ) );
            }
        });

        // Sort the omission lists alphabetically
        List<GbUser> missingUsersSorted = new ArrayList<>( report.getMissingUsers() );
        List<GbUser> unknownUsersSorted = new ArrayList<>( report.getUnknownUsers() );
        Collections.sort( missingUsersSorted );
        Collections.sort( unknownUsersSorted );

        // Create and populate the list of missing users
        final ListView<GbUser> missingUsers = new ListView<GbUser>( "missingUsers", missingUsersSorted )
        {
            @Override
            protected void populateItem( final ListItem<GbUser> item )
            {
                final GbUser user = item.getModelObject();
                item.add( new Label( "missingUser", user.getDisplayId() + " (" + user.getDisplayName() + ")" ) );
            }
        };

        // Create and populate the list of unknown users
        final ListView<GbUser> unknownUsers = new ListView<GbUser>( "unknownUsers", unknownUsersSorted )
        {
            @Override
            protected void populateItem( final ListItem<GbUser> item )
            {
                final GbUser user = item.getModelObject();
                String userDisplay = user.getDisplayId();
                String displayName = user.getDisplayName().trim();
                if( StringUtils.isNotBlank( displayName ))
                {
                    userDisplay += " (" + displayName + ")";
                }
                item.add( new Label( "unknownUser", userDisplay ) );
            }
        };

        // Conditionally hide sub-sections or entire container based on counts
        missingUsersContainer.setVisible( numMissingUsers > 0 );
        unknownUsersContainer.setVisible( numUnknownUsers > 0 );
        omissionsContainer.setVisible( report.getOmittedUserCount() > 0 );

        // Add the components to the page
        missingUsersContainer.add( missingUsersHeader );
        missingUsersContainer.add( missingUsers );
        unknownUsersContainer.add( unknownUsersHeader );
        unknownUsersContainer.add( unknownUsersInfo );
        unknownUsersContainer.add( unknownUsers );
        omissionsContainer.add( missingUsersContainer );
        omissionsContainer.add( unknownUsersContainer );
        add( omissionsContainer );
    }
}
