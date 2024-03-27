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

import java.util.ArrayList;
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
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import org.sakaiproject.scorm.model.api.ActivityReport;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.model.api.Progress;
import org.sakaiproject.scorm.model.api.Score;
import org.sakaiproject.scorm.ui.reporting.components.InteractionPanel;
import org.sakaiproject.scorm.ui.reporting.components.ProgressPanel;
import org.sakaiproject.scorm.ui.reporting.components.ScorePanel;
import org.sakaiproject.scorm.ui.reporting.util.InteractionProvider;
import org.sakaiproject.wicket.ajax.markup.html.table.SakaiDataTable;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.DecoratedPropertyColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ImageLinkColumn;

public class ScoResultsPage extends BaseResultsPage
{
	private static final long serialVersionUID = 1L;

	private static final ResourceReference BLANK_ICON = new PackageResourceReference(InteractionPanel.class, "res/brick.png");
	private static final ResourceReference CORRECT_ICON = new PackageResourceReference(InteractionPanel.class, "res/tick.png");
	private static final ResourceReference INCORRECT_ICON = new PackageResourceReference(InteractionPanel.class, "res/cross.png");
	private static final ResourceReference UNANTICIPATED_ICON = new PackageResourceReference(InteractionPanel.class, "res/exclamation.png");
	private static final ResourceReference NEUTRAL_ICON = new PackageResourceReference(InteractionPanel.class, "res/page_white_text.png");

	public ScoResultsPage(PageParameters pageParams)
	{
		super(pageParams);
	}

	@Override
	protected void initializePage(ContentPackage contentPackage, Learner learner, long attemptNumber, PageParameters pageParams)
	{
		String scoId = pageParams.get("scoId").toString();

		PageParameters uberparentParams = new PageParameters();
		uberparentParams.add("contentPackageId", contentPackage.getContentPackageId());

		PageParameters parentParams = new PageParameters();
		parentParams.add("contentPackageId", contentPackage.getContentPackageId());
		parentParams.add("learnerId", learner.getId());
		parentParams.add("attemptNumber", attemptNumber);

		// SCO-94 - deny users who do not have scorm.view.results permission
		String context = lms.currentContext();
		boolean canViewResults = lms.canViewResults( context );
		Label heading = new Label( "heading2", new ResourceModel( "page.heading.notAllowed" ) );
		add( heading );
		if( !canViewResults )
		{
			heading.setVisibilityAllowed( true );
			add( new WebMarkupContainer( "scorePanel" ) );
			add( new WebMarkupContainer( "progressPanel" ) );
			add( new WebMarkupContainer( "interactionPresenter" ) );
		}
		else
		{
			// SCO-94
			heading.setVisibilityAllowed( false );

			IModel breadcrumbModel = new StringResourceModel("uberparent.breadcrumb", this, new Model(contentPackage));
			addBreadcrumb(breadcrumbModel, ResultsListPage.class, uberparentParams, true);	
			addBreadcrumb(new Model(learner.getDisplayName()), LearnerResultsPage.class, parentParams, true);

			ActivityReport report = resultService.getActivityReport(contentPackage.getContentPackageId(), learner.getId(), attemptNumber, scoId);

			List<Interaction> interactions;

			if (report != null)
			{
				addBreadcrumb(new Model(report.getTitle()), ScoResultsPage.class, pageParams, false);

				add(new ScorePanel("scorePanel", report.getScore()));
				add(new ProgressPanel("progressPanel", report.getProgress()));

				interactions = report.getInteractions();
			}
			else
			{
				addBreadcrumb(new Model("[no module]"), ScoResultsPage.class, pageParams, false);

				add(new ScorePanel("scorePanel", new Score()));
				add(new ProgressPanel("progressPanel", new Progress()));

				interactions = new ArrayList<>();
			}

			InteractionProvider dataProvider = new InteractionProvider(interactions);
			dataProvider.setTableTitle("Interactions");

			SakaiDataTable table = new SakaiDataTable("scoResultsTable", getColumns(), dataProvider, true);
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

		prevParams.add("contentPackageId", contentPackageId);
		prevParams.add("learnerId", learnerId);
		prevParams.add("attemptNumber", attemptNumber);
		prevParams.add("scoId", previousId);

		Link link = new BookmarkablePageLabeledLink("previousLink", new ResourceModel("previous.link.label"), ScoResultsPage.class, prevParams);
		link.setVisible(StringUtils.isNotBlank(previousId));
		return link;
	}

	@Override
	protected boolean attemptExists(long attemptId, String scoId, String learnerId, long contentPackageId)
	{
		return resultService.existsActivityReport(contentPackageId, learnerId, attemptId, scoId);
	}

	@Override
	protected boolean isPreviousLinkVisible(String[] siblingIds)
	{
		return StringUtils.isNotEmpty(siblingIds[0]);
	}

	@Override
	protected Link newNextLink(String nextId, PageParameters pageParams)
	{
		PageParameters nextParams = new PageParameters();

		long contentPackageId = pageParams.get("contentPackageId").toLong();
		String learnerId = pageParams.get("learnerId").toString();
		long attemptNumber = pageParams.get("attemptNumber").toLong();

		nextParams.add("contentPackageId", contentPackageId);
		nextParams.add("learnerId", learnerId);
		nextParams.add("attemptNumber", attemptNumber);
		nextParams.add("scoId", nextId);

		Link link = new BookmarkablePageLabeledLink("nextLink", new ResourceModel("next.link.label"), ScoResultsPage.class, nextParams);
		link.setVisible(StringUtils.isNotBlank(nextId));
		return link;
	}

	@Override
	protected boolean isNextLinkVisible(String[] siblingIds)
	{
		return siblingIds[1] != null && !siblingIds[1].isEmpty();
	}

	@Override
	protected BookmarkablePageLabeledLink newAttemptNumberLink(long i, PageParameters params)
	{
		return new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), ScoResultsPage.class, params);
	}

	private List<IColumn> getColumns()
	{
		IModel idHeader = new ResourceModel("column.header.id");
		IModel descriptionHeader = new ResourceModel("column.header.description");
		IModel typeHeader = new ResourceModel("column.header.type");
		IModel resultHeader = new ResourceModel("column.header.result");

		List<IColumn> columns = new LinkedList<>();

		String[] paramPropertyExpressions = {"contentPackageId", "learnerId", "scoId", "attemptNumber", "interactionId"};

		ActionColumn actionColumn = new ActionColumn(idHeader, "interactionId", "interactionId");
		actionColumn.addAction(new Action("interactionId", InteractionResultsPage.class, paramPropertyExpressions));
		columns.add(actionColumn);
		columns.add(new PropertyColumn(descriptionHeader, "description", "description"));
		columns.add(new TypePropertyColumn(typeHeader, "type", "type"));
		columns.add(new IconPropertyColumn(resultHeader, InteractionResultsPage.class, paramPropertyExpressions, "result", "result"));

		return columns;
	}

	public class IconPropertyColumn extends ImageLinkColumn
	{
		private static final long serialVersionUID = 1L;

		public IconPropertyColumn(IModel displayModel, Class<?> pageClass, String[] paramPropertyExpressions, String iconProperty, String sortProperty)
		{
			super(displayModel, pageClass, paramPropertyExpressions, iconProperty, sortProperty);
		}

		@Override
		protected ResourceReference getIconPropertyReference(String iconPropertyValue)
		{
			ResourceReference resultIconReference = BLANK_ICON;

			if (iconPropertyValue != null)
			{
				if (iconPropertyValue.equalsIgnoreCase("correct"))
				{
					resultIconReference = CORRECT_ICON;
				}
				else if (iconPropertyValue.equalsIgnoreCase("incorrect"))
				{
					resultIconReference = INCORRECT_ICON;
				}
				else if (iconPropertyValue.equalsIgnoreCase("neutral")) 
				{
					resultIconReference = NEUTRAL_ICON;
				}
				else if (iconPropertyValue.equalsIgnoreCase("unanticipated"))
				{
					resultIconReference = UNANTICIPATED_ICON;
				}
			}

			return resultIconReference;
		}
	}

	public class TypePropertyColumn extends DecoratedPropertyColumn
	{
		private static final long serialVersionUID = 1L;

		public TypePropertyColumn(IModel displayModel, String sortProperty, String propertyExpression)
		{
			super(displayModel, sortProperty, propertyExpression);
		}

		@Override
		public Object convertObject(Object object)
		{
			if (object == null)
			{
				return "";
			}

			String key = new StringBuilder("type.").append(object).toString();
			return getLocalizer().getString(key, ScoResultsPage.this);
		}
	}
}
