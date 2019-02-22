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

import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.ui.console.pages.DisplayDesignatedPackage;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;
import org.sakaiproject.scorm.ui.reporting.util.SummaryProvider;
import org.sakaiproject.wicket.ajax.markup.html.table.SakaiDataTable;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;

public class LearnerResultsPage extends BaseResultsPage
{
	private static final long serialVersionUID = 1L;

	public LearnerResultsPage(PageParameters pageParams)
	{
		super(pageParams);
	}

	@Override
	protected void initializePage(ContentPackage contentPackage, Learner learner, long attemptNumber, PageParameters pageParams)
	{
		PageParameters uberparentParams = new PageParameters();
		uberparentParams.add("contentPackageId", contentPackage.getContentPackageId());

		PageParameters parentParams = new PageParameters();
		parentParams.add("contentPackageId", contentPackage.getContentPackageId());
		parentParams.add("learnerId", learner.getId());
		parentParams.add("attemptNumber", attemptNumber);

		// SCO-94 - deny users who do not have scorm.view.results permission
		String context = lms.currentContext();
		boolean canViewResults = lms.canViewResults( context );
		Label heading = new Label( "heading1", new ResourceModel( "page.heading.notAllowed" ) );
		add( heading );
		if( !canViewResults )
		{
			heading.setVisibilityAllowed( true );
			add( new WebMarkupContainer( "summaryPresenter" ) );
		}
		else
		{
			// SCO-94
			heading.setVisibilityAllowed( false );

			IModel breadcrumbModel = new StringResourceModel("parent.breadcrumb", this, new Model(contentPackage));

			// MvH
			if (isSinglePackageTool())
			{
				if (lms.canGrade(lms.currentContext()))
				{
					addBreadcrumb(breadcrumbModel, ResultsListPage.class, uberparentParams, true);
				}
				else
				{
					addBreadcrumb(breadcrumbModel, DisplayDesignatedPackage.class, uberparentParams, true);
				}
			}
			else
			{
				if (lms.canGrade(lms.currentContext()))
				{
					addBreadcrumb(breadcrumbModel, ResultsListPage.class, uberparentParams, true);
				}
				else
				{
					addBreadcrumb(breadcrumbModel, PackageListPage.class, uberparentParams, true);
				}
			}
			addBreadcrumb(new Model(learner.getDisplayName()), LearnerResultsPage.class, parentParams, false);

			List<ActivitySummary> summaries = resultService.getActivitySummaries(contentPackage.getContentPackageId(), learner.getId(), attemptNumber);
			SummaryProvider dataProvider = new SummaryProvider(summaries);
			dataProvider.setTableTitle(getLocalizer().getString("table.title", this));

			SakaiDataTable table = new SakaiDataTable("learnerTable", getColumns(), dataProvider, true);
			add(table);
		}
	}

	@Override
	protected Link newPreviousLink(String previousId, PageParameters pageParams)
	{
		PageParameters prevParams = new PageParameters();

		long contentPackageId = pageParams.get("contentPackageId").toLong();

		prevParams.add("contentPackageId", contentPackageId);
		prevParams.add("learnerId", previousId);

		Link link = new BookmarkablePageLabeledLink("previousLink", new ResourceModel("previous.link.label"), LearnerResultsPage.class, prevParams);
		link.setVisible(StringUtils.isNotEmpty(previousId));
		return link;
	}

	@Override
	protected Link newNextLink(String nextId, PageParameters pageParams)
	{
		PageParameters nextParams = new PageParameters();

		long contentPackageId = pageParams.get("contentPackageId").toLong();

		nextParams.add("contentPackageId", contentPackageId);
		nextParams.add("learnerId", nextId);

		Link link = new BookmarkablePageLabeledLink("nextLink", new ResourceModel("next.link.label"), LearnerResultsPage.class, nextParams);
		link.setVisible(StringUtils.isNotBlank(nextId));
		return link;
	}

	@Override
	protected BookmarkablePageLabeledLink newAttemptNumberLink(long i, PageParameters params)
	{
		return new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), LearnerResultsPage.class, params);
	}

	private List<IColumn> getColumns()
	{
		IModel titleHeader = new ResourceModel("column.header.title");
		IModel scoreHeader = new ResourceModel("column.header.score");
		IModel completedHeader = new ResourceModel("column.header.completed");
		IModel successHeader = new ResourceModel("column.header.success");

		List<IColumn> columns = new LinkedList<>();
		String[] paramPropertyExpressions = {"contentPackageId", "learnerId", "scoId", "attemptNumber"};

		ActionColumn actionColumn = new ActionColumn(titleHeader, "title", "title");
		actionColumn.addAction(new Action("title", ScoResultsPage.class, paramPropertyExpressions));
		columns.add(actionColumn);

		columns.add(new PercentageColumn(scoreHeader, "scaled", "scaled"));
		columns.add(new PropertyColumn(completedHeader, "completionStatus", "completionStatus"));
		columns.add(new PropertyColumn(successHeader, "successStatus", "successStatus"));

		return columns;
	}
}
