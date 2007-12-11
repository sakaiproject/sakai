package org.sakaiproject.scorm.ui.console.pages;

import java.util.LinkedList;
import java.util.List;

import org.adl.datamodels.IDataManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.ui.console.components.DecoratedDatePropertyColumn;
import org.sakaiproject.scorm.ui.player.pages.PlayerPage;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
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
		final String courseId = pageParams.getString("courseId");
		final String contentPackageName = pageParams.getString("contentPackageName");
				
		List<IColumn> columns = new LinkedList<IColumn>();
		columns.add(new PropertyColumn(new StringResourceModel("column.header.learner.name", this, null), "learnerName", "learnerName"));
		
		
		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.begin.date", this, null), "beginDate", "beginDate"));
		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.last.modified.date", this, null), "lastModifiedDate", "lastModifiedDate"));
		
		String[] paramPropertyExpressions = {"courseId", "learnerId", "attemptNumber"};
		
		ActionColumn actionColumn = new ActionColumn(new StringResourceModel("column.header.attempt.number", this, null), "attemptNumber", "attemptNumber");
		Action detailAction = new Action("attemptNumber", AttemptDetailPage.class, paramPropertyExpressions);
		actionColumn.addAction(detailAction);
		
		columns.add(actionColumn);
		
		List<Attempt> attempts = resultService.getAttempts(courseId);
		
		BasicDataTable table = new BasicDataTable("attemptTable", columns, attempts);

		add(table);
		
		
		
	/*	List<IDataManager> dataManagers = clientFacade.getContentPackageDataManagers(contentPackageId);
		
		add(new ListView("dataManagers", dataManagers) {
			private static final long serialVersionUID = 1L;
			
		 	public void populateItem(final ListItem item) {
		 		IDataManager manager = (IDataManager)item.getModelObject();
		 		String id = manager.getCourseId();
		 		String[] parts = id.split("/");
		 		
		 		String userId = manager.getUserId();
		 		String userName = "Undefined";
		 		try {
		 			User user = clientFacade.getUser(userId);
		 			userName = user.getDisplayName();
		 		} catch (UserNotDefinedException unde) {
		 			log.warn("This user is not defined: " + userId);
		 		}
		 		
		 		if (null != parts && parts.length > 0) {
		 			final String fileName = parts[parts.length - 1];
		 		
		 			PageParameters newParams = new PageParameters();
		 			newParams.add("contentPackageName", contentPackageName);
		 			newParams.add("courseId", manager.getCourseId());
		 			newParams.add("learnerName", userName);
		 			newParams.add("learnerId", manager.getUserId());
		 			newParams.add("attemptNumber", "1");
		 			
		 			item.add(new Label("learnerName", userName));
		 			item.add(new Label("learnerId", userId));
		 			
		 			BookmarkablePageLink resultsPageLink = new BookmarkablePageLink("resultsLink", AttemptDetailPage.class, newParams);
		 			resultsPageLink.add(new Label("attemptNum", "1"));

		 			item.add(resultsPageLink);
		 		}
		 	}
		});*/
	}
	
	
	
}
