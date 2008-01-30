package org.sakaiproject.scorm.ui.reporting.pages;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.ui.reporting.util.SummaryProvider;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.presenter.EnhancedDataPresenter;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;

public class LearnerResultsPage extends BaseResultsPage {

	private static final long serialVersionUID = 1L;

	public LearnerResultsPage(PageParameters pageParams) {
		super(pageParams);		
	}

	
	protected void initializePage(ContentPackage contentPackage, Learner learner, long attemptNumber, PageParameters pageParams) {
		PageParameters uberparentParams = new PageParameters();
		uberparentParams.put("id", contentPackage.getId());
		
		PageParameters parentParams = new PageParameters();
		parentParams.put("contentPackageId", contentPackage.getId());
		parentParams.put("learnerId", learner.getId());
		parentParams.put("attemptNumber", attemptNumber);
		
		IModel breadcrumbModel = new StringResourceModel("parent.breadcrumb", this, new Model(contentPackage));
		addBreadcrumb(breadcrumbModel, AttemptListPage.class, uberparentParams, true);	
		addBreadcrumb(new Model(learner.getDisplayName()), LearnerResultsPage.class, parentParams, false);
		
		SummaryProvider dataProvider = new SummaryProvider(resultService.getActivitySummaries(contentPackage.getId(), learner.getId(), attemptNumber));
		EnhancedDataPresenter presenter = new EnhancedDataPresenter("summaryPresenter", getColumns(), dataProvider);
		add(presenter);
	}
	
	protected BookmarkablePageLabeledLink newAttemptNumberLink(long i, PageParameters params) {
		return new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), LearnerResultsPage.class, params);
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
