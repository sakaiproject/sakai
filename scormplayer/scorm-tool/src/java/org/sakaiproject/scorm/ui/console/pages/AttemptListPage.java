package org.sakaiproject.scorm.ui.console.pages;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.ui.console.components.ContentPackageDetailPanel;
import org.sakaiproject.scorm.ui.console.components.DecoratedDatePropertyColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.presenter.EnhancedDataPresenter;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class AttemptListPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(AttemptListPage.class);
	
	@SpringBean
	ScormContentService contentService;
	@SpringBean
	ScormResultService resultService;
	
	public AttemptListPage(PageParameters pageParams) {
		super(pageParams);
		
		final long contentPackageId = pageParams.getLong("id");
		
		ContentPackage contentPackage = contentService.getContentPackage(contentPackageId);
		
		AttemptDataProvider dataProvider = new AttemptDataProvider(contentPackageId);
		
		add(new EnhancedDataPresenter("attemptPresenter", getColumns(), dataProvider));
		
		add(new ContentPackageDetailPanel("details", contentPackage));
	}
	
	@Override
	protected Label newPageTitleLabel(PageParameters params) {
		final long contentPackageId = params.getLong("id");
		
		ContentPackage contentPackage = contentService.getContentPackage(contentPackageId);

		return new Label("page.title", new StringResourceModel("page.title", this, new Model(contentPackage)));
	}
	
	private List<IColumn> getColumns() {
		List<IColumn> columns = new LinkedList<IColumn>();
		columns.add(new PropertyColumn(new ResourceModel("column.header.learner.name"), "learnerName", "learnerName"));
		
		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.begin.date", this, null), "beginDate", "beginDate"));
		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.last.modified.date", this, null), "lastModifiedDate", "lastModifiedDate"));
		
		String[] paramPropertyExpressions = {"learnerId", "attemptNumber", "id"};
		
		ActionColumn actionColumn = new ActionColumn(new StringResourceModel("column.header.attempt.number", this, null), "attemptNumber", "attemptNumber");
		Action detailAction = new Action("attemptNumber", AttemptDetailPage.class, paramPropertyExpressions);
		actionColumn.addAction(detailAction);
		
		columns.add(actionColumn);
		
		return columns;
	}
	
	public class AttemptDataProvider extends EnhancedDataProvider {

		private static final long serialVersionUID = 1L;

		private final List<Attempt> attempts;
		
		public AttemptDataProvider(long contentPackageId) {
			this.attempts = resultService.getAttempts(contentPackageId);
		}
		
		public Iterator iterator(int first, int last) {
			return attempts.subList(first, last).iterator();
		}

		public int size() {
			return attempts.size();
		}
		
	}
	
	
	
	
	
	
}
