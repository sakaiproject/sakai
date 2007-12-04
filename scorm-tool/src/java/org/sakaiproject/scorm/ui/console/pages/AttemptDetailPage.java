package org.sakaiproject.scorm.ui.console.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.service.api.ScormResultService;

public class AttemptDetailPage extends ConsoleBasePage {
	
	private static final long serialVersionUID = 1L;

	@SpringBean
	ScormResultService resultService;

	public AttemptDetailPage(PageParameters pageParams) {
		//super(new StringBuilder().append(pageParams.getString("contentPackageName")).append(" Attempt Detail for")
		//	.append(pageParams.getString("learnerName")).toString());
		
		String title = new StringBuilder().append(pageParams.getString("contentPackageName")).append(" Attempt Detail for")
		.append(pageParams.getString("learnerName")).toString();
		
		String contentPackageName = pageParams.getString("contentPackageName");
		String courseId = pageParams.getString("courseId");
		String learnerName = pageParams.getString("learnerName");
		String learnerId = pageParams.getString("learnerId");
		int attemptNumber = pageParams.getInt("attemptNumber");
		
		add(new Label("content.package.name", contentPackageName));
		add(new Label("learner.name", learnerName));
		
		Attempt attempt = resultService.lookupAttempt(courseId, learnerId, attemptNumber);
		
		add(new Label("completion.status", new PropertyModel(attempt, "completionStatus")));
		add(new Label("score.scaled", new PropertyModel(attempt, "scoreScaled")));
		add(new Label("success.status", new PropertyModel(attempt, "successStatus")));
		
		//add(new Label("courseId", courseId));
		//add(new Label("userId", userId));
		
		//IDataManager dataManager = clientFacade.getContentPackageDataManger(courseId, userId);
		
		//String value = getValue(dataManager, "");
	}

	
	
	
	
}
