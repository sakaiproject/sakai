package org.sakaiproject.scorm.ui.console.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.CMIFieldGroup;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;

public class AttemptDetailPage extends ConsoleBasePage {
	
	private static final long serialVersionUID = 1L;

	@SpringBean
	ScormResultService resultService;
	@SpringBean
	ScormContentService contentService;
	
	public AttemptDetailPage(PageParameters pageParams) {
		String courseId = pageParams.getString("courseId");
		String learnerName = pageParams.getString("learnerName");
		String learnerId = pageParams.getString("learnerId");
		long attemptNumber = pageParams.getLong("attemptNumber");
		long id = pageParams.getLong("id");
		
		String[] fields = {"cmi.completion_status", "cmi.score.scaled", "cmi.success_status" };
		
		Attempt attempt = resultService.getAttempt(id);
		CMIFieldGroup fieldGroup = resultService.getAttemptResults(attempt);
		
		ContentPackage contentPackage = contentService.getContentPackage(attempt.getContentPackageId());
		
		add(new Label("content.package.name", contentPackage.getTitle()));
		add(new Label("learner.name", learnerName));
		
		
	}

	
	
	
	
}
