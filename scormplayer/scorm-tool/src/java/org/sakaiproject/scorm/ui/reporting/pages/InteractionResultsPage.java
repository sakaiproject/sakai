/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.ui.reporting.pages;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.model.api.Objective;
import org.sakaiproject.scorm.ui.reporting.components.InteractionPanel;
import org.sakaiproject.scorm.ui.reporting.util.ObjectiveProvider;
import org.sakaiproject.wicket.ajax.markup.html.table.SakaiDataTable;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;

public class InteractionResultsPage extends BaseResultsPage
{
	private static final long serialVersionUID = 1L;

	public InteractionResultsPage(PageParameters pageParams)
	{
		super(pageParams);
	}

	@Override
	protected void initializePage(ContentPackage contentPackage, Learner learner, long attemptNumber, PageParameters pageParams)
	{
		String scoId = pageParams.get("scoId").toString();

		PageParameters uberuberparentParams = new PageParameters();
		uberuberparentParams.add("contentPackageId", contentPackage.getContentPackageId());

		PageParameters parentParams = new PageParameters();
		parentParams.add("contentPackageId", contentPackage.getContentPackageId());
		parentParams.add("learnerId", learner.getId());
		parentParams.add("attemptNumber", attemptNumber);

		// SCO-94 - deny users who do not have scorm.view.results permission
		String context = lms.currentContext();
		boolean canViewResults = lms.canViewResults( context );
		Label heading = new Label( "heading3", new ResourceModel( "page.heading.notAllowed" ) );
		add( heading );
		if( !canViewResults )
		{
			heading.setVisibilityAllowed( true );
			add( new WebMarkupContainer( "interactionPanel" ) );
			add( new WebMarkupContainer( "objectivePresenter" ) );
		}
		else
		{
			// SCO-94
			heading.setVisibilityAllowed( false );

			String interactionId = pageParams.get("interactionId").toString();

			Interaction interaction = resultService.getInteraction(contentPackage.getContentPackageId(), learner.getId(), attemptNumber, scoId, interactionId);
			add(new InteractionPanel("interactionPanel", interaction));

			IModel breadcrumbModel = new StringResourceModel("uberuberparent.breadcrumb", this, new Model(contentPackage));
			addBreadcrumb(breadcrumbModel, ResultsListPage.class, uberuberparentParams, true);	
			addBreadcrumb(new Model(learner.getDisplayName()), LearnerResultsPage.class, parentParams, true);
			addBreadcrumb(new Model(interaction.getActivityTitle()), ScoResultsPage.class, pageParams, true);
			addBreadcrumb(new Model(interaction.getInteractionId()), InteractionResultsPage.class, pageParams, false);

			List<Objective> objectives = interaction.getObjectives();
			ObjectiveProvider dataProvider = new ObjectiveProvider(objectives);
			dataProvider.setTableTitle("Objectives");

			SakaiDataTable table = new SakaiDataTable("interactionTable", getColumns(), dataProvider, true);
			add(table);
		}
	}

	@Override
	protected Link newPreviousLink(String previousId, PageParameters pageParams)
	{
		PageParameters prevParams = new PageParameters();

		long contentPackageId = pageParams.get("contentPackageId").toLong();
		String learnerId = pageParams.get("learnerId").toString();
		long attemptNumber = pageParams.get("attemptNumber").toLong();
		String scoId = pageParams.get("scoId").toString();

		prevParams.add("contentPackageId", contentPackageId);
		prevParams.add("learnerId", learnerId);
		prevParams.add("attemptNumber", attemptNumber);
		prevParams.add("scoId", scoId);
		prevParams.add("interactionId", previousId);

		Link link = new BookmarkablePageLabeledLink("previousLink", new ResourceModel("previous.link.label"), InteractionResultsPage.class, prevParams);
		link.setVisible(StringUtils.isNotBlank(previousId));
		return link;
	}

	@Override
	protected Link newNextLink(String nextId, PageParameters pageParams)
	{
		PageParameters nextParams = new PageParameters();

		long contentPackageId = pageParams.get("contentPackageId").toLong();
		String learnerId = pageParams.get("learnerId").toString();
		long attemptNumber = pageParams.get("attemptNumber").toLong();
		String scoId = pageParams.get("scoId").toString();

		nextParams.add("contentPackageId", contentPackageId);
		nextParams.add("learnerId", learnerId);
		nextParams.add("attemptNumber", attemptNumber);
		nextParams.add("scoId", scoId);
		nextParams.add("interactionId", nextId);

		Link link = new BookmarkablePageLabeledLink("nextLink", new ResourceModel("next.link.label"), InteractionResultsPage.class, nextParams);
		link.setVisible(StringUtils.isNotBlank(nextId));
		return link;
	}

	@Override
	protected BookmarkablePageLabeledLink newAttemptNumberLink(long i, PageParameters params)
	{
		return new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), InteractionResultsPage.class, params);
	}

	private List<IColumn> getColumns()
	{
		IModel idHeader = new ResourceModel("column.header.id");
		IModel descriptionHeader = new ResourceModel("column.header.description");
		IModel completionStatusHeader = new ResourceModel("column.header.completion.status");
		IModel successStatusHeader = new ResourceModel("column.header.success.status");

		List<IColumn> columns = new LinkedList<>();

		columns.add(new PropertyColumn(idHeader, "id", "id"));
		columns.add(new PropertyColumn(descriptionHeader, "description", "description"));
		columns.add(new PropertyColumn(completionStatusHeader, "completionStatus", "completionStatus"));
		columns.add(new PropertyColumn(successStatusHeader, "successStatus", "successStatus"));

		return columns;
	}
}
