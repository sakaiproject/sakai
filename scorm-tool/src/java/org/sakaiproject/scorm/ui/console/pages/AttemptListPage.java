package org.sakaiproject.scorm.ui.console.pages;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.ui.console.components.DecoratedDatePropertyColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.BasicDataTable;

public class AttemptListPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(AttemptListPage.class);
	
	@SpringBean
	ScormResultService resultService;
	
	public AttemptListPage(PageParameters pageParams) {
		String title = new StringBuilder().append("Attempt Listing - ").append(pageParams.getString("contentPackage")).toString();

		final long contentPackageId = pageParams.getLong("id");
		final String contentPackageName = pageParams.getString("contentPackageName");
				
		List<IColumn> columns = new LinkedList<IColumn>();
		columns.add(new PropertyColumn(new StringResourceModel("column.header.learner.name", this, null), "learnerName", "learnerName"));
		
		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.begin.date", this, null), "beginDate", "beginDate"));
		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.last.modified.date", this, null), "lastModifiedDate", "lastModifiedDate"));
		
		String[] paramPropertyExpressions = {"learnerId", "attemptNumber", "id"};
		
		ActionColumn actionColumn = new ActionColumn(new StringResourceModel("column.header.attempt.number", this, null), "attemptNumber", "attemptNumber");
		Action detailAction = new Action("attemptNumber", AttemptDetailPage.class, paramPropertyExpressions);
		actionColumn.addAction(detailAction);
		
		columns.add(actionColumn);
		
		List<Attempt> attempts = resultService.getAttempts(contentPackageId);
		
		BasicDataTable table = new BasicDataTable("attemptTable", columns, attempts);

		add(table);
	}
	
	
	
}
