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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;
import org.sakaiproject.scorm.ui.reporting.util.SummaryProvider;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.presenter.EnhancedDataPresenter;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.DecoratedPropertyColumn;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class LearnerAttemptSummaryPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;
	
	private static Log log = LogFactory.getLog(LearnerAttemptSummaryPage.class);
	
	@SpringBean
	transient LearningManagementSystem lms;
	@SpringBean
	transient ScormContentService contentService;
	@SpringBean
	transient ScormResultService resultService;
	@SpringBean
	transient ScormSequencingService sequencingService;
	
	private final RepeatingView attemptNumberLinks;
	
	public LearnerAttemptSummaryPage(PageParameters pageParams) {
		long contentPackageId = pageParams.getLong("contentPackageId");
		String learnerId = pageParams.getString("learnerId");
		
		String learnerName = "[name unavailable]";
		
		try {
			Learner learner = lms.getLearner(learnerId);
			
			learnerName = new StringBuilder(learner.getDisplayName()).append(" (")
				.append(learner.getDisplayId()).append(")").toString();
			
		} catch (LearnerNotDefinedException lnde) {
			log.error("Could not find learner for this id: " + learnerId);
		}

		ContentPackage contentPackage = contentService.getContentPackage(contentPackageId);
		
		PageParameters parentParams = new PageParameters();
		parentParams.put("id", contentPackage.getId());
		parentParams.put("learnerId", learnerId);
		
		IModel breadcrumbModel = new StringResourceModel("parent.breadcrumb", this, new Model(contentPackage));
		addBreadcrumb(breadcrumbModel, AttemptListPage.class, parentParams, true);	
		addBreadcrumb(new Model(learnerName), LearnerAttemptSummaryPage.class, pageParams, false);
		
		add(new Label("content.package.name", contentPackage.getTitle()));
		add(new Label("learner.name", learnerName));
		
		int numberOfAttempts = resultService.getNumberOfAttempts(contentPackageId, learnerId);
		
		long attemptNumber = 0;
		
		if (pageParams.containsKey("attemptNumber")) 
			attemptNumber = pageParams.getLong("attemptNumber");
		
		if (attemptNumber == 0)
			attemptNumber = numberOfAttempts;
		
		this.attemptNumberLinks = new RepeatingView("attemptNumberLinks");
		add(attemptNumberLinks);
		
		for (long i=1;i<=numberOfAttempts;i++) {
			this.addAttemptNumberLink(i, pageParams, attemptNumberLinks, attemptNumber);
		}

		SummaryProvider dataProvider = new SummaryProvider(resultService.getActivitySummaries(contentPackageId, learnerId, attemptNumber));
	
		EnhancedDataPresenter presenter = new EnhancedDataPresenter("summaryPresenter", getColumns(), dataProvider);
		
		add(presenter);
	}
	
	
	private List<IColumn> getColumns() {
		IModel titleHeader = new ResourceModel("column.header.title");
		IModel scoreHeader = new ResourceModel("column.header.score");
		IModel completedHeader = new ResourceModel("column.header.completed");
		IModel successHeader = new ResourceModel("column.header.success");
		
		
		List<IColumn> columns = new LinkedList<IColumn>();
		
		columns.add(new PropertyColumn(titleHeader, "title", "title"));
		
		columns.add(new PercentageColumn(scoreHeader, "scaled", "scaled"));
		
		columns.add(new PropertyColumn(completedHeader, "completionStatus", "completionStatus"));
		
		columns.add(new PropertyColumn(successHeader, "successStatus", "successStatus"));
		
		
		
		/*
		ActionColumn actionColumn = new ActionColumn(learnerNameHeader, "learnerName", "learnerName");
		
		String[] paramPropertyExpressions = {"contentPackageId", "learnerId"};
		
		Action detailAction = new Action("learnerName", LearnerAttemptDetailPage.class, paramPropertyExpressions);
		actionColumn.addAction(detailAction);
		
		Action summaryAction = new Action(summaryActionLabel, LearnerAttemptSummaryPage.class, paramPropertyExpressions);
		actionColumn.addAction(summaryAction);
		columns.add(actionColumn);

		columns.add(new PropertyColumn(numberOfAttemptsHeader, "numberOfAttempts", "numberOfAttempts"));
		*/
		
		return columns;
	}
	
	public class PercentageColumn extends DecoratedPropertyColumn {

		private static final long serialVersionUID = 1L;

		public PercentageColumn(IModel displayModel, String sortProperty, String propertyExpression) {
			super(displayModel, sortProperty, propertyExpression);
		}

		@Override
		public Object convertObject(Object object) {
			Double d = (Double)object;
			
			return getPercentageString(d);
		}
		
		private String getPercentageString(double d) {
			
			double p = d * 100.0;
			
			String percentage = "" + p + " %";
			
			if (d < 0.0)
				percentage = "Not available";
			
			return percentage;
		}
	}	
	
	/*
	 * Copied the basic organization of this method from an Apache Wicket class
	 * 	org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
	 * originally authored by Igor Vaynberg (ivaynberg)
	 */
	private void addAttemptNumberLink(long i, PageParameters params, RepeatingView container, long current)
	{
		params.put("attemptNumber", i);
		
		BookmarkablePageLabeledLink link = new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), LearnerAttemptSummaryPage.class, params);
		//link.add(new AttributeModifier("style", new Model("margin-right: 1em")));
		
		if (i == current) {
			link.setEnabled(false);
		}
			
		WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
		item.setRenderBodyOnly(true);
		item.add(link);

		container.add(item);
	}
	
	
}
