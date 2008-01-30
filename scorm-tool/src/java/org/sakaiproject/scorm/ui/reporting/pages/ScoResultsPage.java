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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.ActivityReport;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.ui.reporting.util.InteractionProvider;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.presenter.EnhancedDataPresenter;

public class ScoResultsPage extends BaseResultsPage {

	private static final long serialVersionUID = 1L;

	@SpringBean
	transient ScormResultService resultService;
	
	public ScoResultsPage(PageParameters pageParams) {
		super(pageParams);
	
		
		
	}

	@Override
	protected void initializePage(ContentPackage contentPackage, Learner learner, long attemptNumber, PageParameters pageParams) {
		String scoId = pageParams.getString("scoId");
		
		PageParameters uberparentParams = new PageParameters();
		uberparentParams.put("id", contentPackage.getId());
		
		PageParameters parentParams = new PageParameters();
		parentParams.put("contentPackageId", contentPackage.getId());
		parentParams.put("learnerId", learner.getId());
		parentParams.put("attemptNumber", attemptNumber);
		
		ActivityReport report = resultService.getActivityReport(contentPackage.getId(), learner.getId(), attemptNumber, scoId);
		
		IModel breadcrumbModel = new StringResourceModel("uberparent.breadcrumb", this, new Model(contentPackage));
		addBreadcrumb(breadcrumbModel, AttemptListPage.class, uberparentParams, true);	
		addBreadcrumb(new Model(learner.getDisplayName()), LearnerResultsPage.class, parentParams, true);
		addBreadcrumb(new Model(report.getTitle()), ScoResultsPage.class, pageParams, false);
		
		
		InteractionProvider dataProvider = new InteractionProvider(report.getInteractions());
		EnhancedDataPresenter presenter = new EnhancedDataPresenter("interactionPresenter", getColumns(), dataProvider);
		add(presenter);
	}
	
	protected BookmarkablePageLabeledLink newAttemptNumberLink(long i, PageParameters params) {
		return new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), ScoResultsPage.class, params);
	}
	
	private List<IColumn> getColumns() {
		IModel idHeader = new ResourceModel("column.header.id");
		IModel descriptionHeader = new ResourceModel("column.header.description");
		IModel learnerResponseHeader = new ResourceModel("column.header.learner.response");
		IModel resultHeader = new ResourceModel("column.header.result");
		
		List<IColumn> columns = new LinkedList<IColumn>();

		columns.add(new PropertyColumn(idHeader, "id", "id"));
		columns.add(new PropertyColumn(descriptionHeader, "description", "description"));
		//columns.add(new PropertyColumn(learnerResponseHeader, "learnerResponse", "learnerResponse"));
		columns.add(new PropertyColumn(resultHeader, "result", "result"));
		
		return columns;
	}

}
