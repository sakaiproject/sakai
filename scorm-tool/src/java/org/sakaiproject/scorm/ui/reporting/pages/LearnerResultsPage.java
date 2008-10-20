/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.ui.reporting.pages;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.ui.Icon;
import org.sakaiproject.scorm.ui.reporting.util.SummaryProvider;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.presenter.EnhancedDataPresenter;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;

public class LearnerResultsPage extends BaseResultsPage {

	private static final long serialVersionUID = 1L;

	private static ResourceReference PAGE_ICON = new ResourceReference(LearnerResultsPage.class, "res/report_user.png");

	public LearnerResultsPage(PageParameters pageParams) {
		super(pageParams);		
	}

	
	protected void initializePage(ContentPackage contentPackage, Learner learner, long attemptNumber, PageParameters pageParams) {
		PageParameters uberparentParams = new PageParameters();
		uberparentParams.put("contentPackageId", contentPackage.getContentPackageId());
		
		PageParameters parentParams = new PageParameters();
		parentParams.put("contentPackageId", contentPackage.getContentPackageId());
		parentParams.put("learnerId", learner.getId());
		parentParams.put("attemptNumber", attemptNumber);
		
		IModel breadcrumbModel = new StringResourceModel("parent.breadcrumb", this, new Model(contentPackage));
		addBreadcrumb(breadcrumbModel, ResultsListPage.class, uberparentParams, true);	
		addBreadcrumb(new Model(learner.getDisplayName()), LearnerResultsPage.class, parentParams, false);
		
		List<ActivitySummary> summaries = resultService.getActivitySummaries(contentPackage.getContentPackageId(), learner.getId(), attemptNumber);
		SummaryProvider dataProvider = new SummaryProvider(summaries);
		dataProvider.setTableTitle(getLocalizer().getString("table.title", this));
		EnhancedDataPresenter presenter = new EnhancedDataPresenter("summaryPresenter", getColumns(), dataProvider);
		add(presenter);
		
		presenter.setVisible(summaries != null && summaries.size() > 0);
	}
	
	protected Link newPreviousLink(String previousId, PageParameters pageParams) {
		PageParameters prevParams = new PageParameters();
		
		long contentPackageId = pageParams.getLong("contentPackageId");
		
		prevParams.put("contentPackageId", contentPackageId);
		prevParams.put("learnerId", previousId);
		
		Link link = new BookmarkablePageLabeledLink("previousLink", new ResourceModel("previous.link.label"), LearnerResultsPage.class, prevParams);
		link.setVisible(previousId.trim().length() > 0);
		return link;
		
		/*PageParameters prevParams = new PageParameters();
		
		long contentPackageId = pageParams.getLong("contentPackageId");
		String learnerId = pageParams.getString("learnerId");
		
		prevParams.put("contentPackageId", contentPackageId);
		
		String previousLearnerIds = pageParams.getString("previousLearnerIds");
		String nextLearnerIds = pageParams.getString("nextLearnerIds");
		
		if (previousLearnerIds == null)
			previousLearnerIds = "";
		
		boolean showPrevious = false;

		if (previousLearnerIds.trim().length() > 0) {
			int indexOf = previousLearnerIds.indexOf(',');
			String currentLearnerId = previousLearnerIds.substring(0, indexOf);
			
			prevParams.put("learnerId", currentLearnerId);
			
			if (indexOf + 1 < previousLearnerIds.length()) 
				prevParams.put("previousLearnerIds", previousLearnerIds.substring(indexOf+1));
			
			String nextIds = new StringBuilder().append(learnerId).append(",").append(nextLearnerIds).toString();
		
			prevParams.put("nextLearnerIds", nextIds);
			showPrevious = true;
		}
		
		Link link = new BookmarkablePageLabeledLink("previousLink", new ResourceModel("previous.link.label"), LearnerResultsPage.class, prevParams);
	
		link.setVisible(showPrevious);
		
		return link;*/
	}
	
	protected Link newNextLink(String nextId, PageParameters pageParams) {
		PageParameters nextParams = new PageParameters();
		
		long contentPackageId = pageParams.getLong("contentPackageId");
		
		nextParams.put("contentPackageId", contentPackageId);
		nextParams.put("learnerId", nextId);

		Link link = new BookmarkablePageLabeledLink("nextLink", new ResourceModel("next.link.label"), LearnerResultsPage.class, nextParams);

		link.setVisible(nextId.trim().length() > 0);
		return link;
		
		/*PageParameters nextParams = new PageParameters();
		
		long contentPackageId = pageParams.getLong("contentPackageId");
		String learnerId = pageParams.getString("learnerId");
		
		nextParams.put("contentPackageId", contentPackageId);
		
		String previousLearnerIds = pageParams.getString("previousLearnerIds");
		String nextLearnerIds = pageParams.getString("nextLearnerIds");
		
		if (previousLearnerIds == null)
			previousLearnerIds = "";
		if (nextLearnerIds == null)
			nextLearnerIds = "";

		boolean showNext = false;
			
		if (nextLearnerIds.trim().length() > 0) {
			int indexOf = nextLearnerIds.indexOf(',');
			String currentLearnerId = nextLearnerIds.substring(0, indexOf);
			
			nextParams.put("learnerId", currentLearnerId);
			
			if (indexOf + 1 < nextLearnerIds.length())
				nextParams.put("nextLearnerIds", nextLearnerIds.substring(indexOf + 1));
			
			String prevIds = new StringBuilder().append(learnerId).append(",").append(previousLearnerIds).toString();
			
			nextParams.put("previousLearnerIds", prevIds);
			showNext = true;
		}
		
		
		Link link = new BookmarkablePageLabeledLink("nextLink", new ResourceModel("next.link.label"), LearnerResultsPage.class, nextParams);
	
		link.setVisible(showNext);
		
		return link;*/
	}
	
	
	protected BookmarkablePageLabeledLink newAttemptNumberLink(long i, PageParameters params) {
		return new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), LearnerResultsPage.class, params);
	}
	
	protected ResourceReference getPageIconReference() {
		return PAGE_ICON;
	}
	
	private List<IColumn> getColumns() {
		IModel titleHeader = new ResourceModel("column.header.title");
		IModel scoreHeader = new ResourceModel("column.header.score");
		IModel completedHeader = new ResourceModel("column.header.completed");
		IModel successHeader = new ResourceModel("column.header.success");
		
		
		List<IColumn> columns = new LinkedList<IColumn>();
		
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
