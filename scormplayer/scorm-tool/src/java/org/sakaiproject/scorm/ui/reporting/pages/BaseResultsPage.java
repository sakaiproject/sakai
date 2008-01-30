package org.sakaiproject.scorm.ui.reporting.pages;

import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.table.DecoratedPropertyColumn;

public abstract class BaseResultsPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(BaseResultsPage.class);
	
	@SpringBean
	transient LearningManagementSystem lms;
	@SpringBean
	transient ScormContentService contentService;
	@SpringBean
	transient ScormResultService resultService;
	@SpringBean
	transient ScormSequencingService sequencingService;
	
	private final RepeatingView attemptNumberLinks;
	
	public BaseResultsPage(PageParameters pageParams) {
		long contentPackageId = pageParams.getLong("contentPackageId");
		String learnerId = pageParams.getString("learnerId");
		
		String learnerName = "[name unavailable]";
		
		Learner learner = null;
		
		try {
			learner = lms.getLearner(learnerId);
			
			learnerName = new StringBuilder(learner.getDisplayName()).append(" (")
				.append(learner.getDisplayId()).append(")").toString();
			
		} catch (LearnerNotDefinedException lnde) {
			log.error("Could not find learner for this id: " + learnerId);
			
			learner = new Learner(learnerId, learnerName, "[id unavailable]");
		}

		ContentPackage contentPackage = contentService.getContentPackage(contentPackageId);
						
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
		
		//Attempt attempt = resultService.getAttempt(contentPackageId, learnerId, attemptNumber);
		
		//add(new Label("content.package.name", contentPackage.getTitle()));
		//add(new Label("learner.name", learnerName));
		
		/*SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		
		boolean isSuspended = false;
		boolean isNotExited = false;
		String dateString = "";
		
		if (attempt != null) {
			isSuspended = attempt.isSuspended();
			isNotExited = attempt.isNotExited();
			
			if (attempt.getLastModifiedDate() != null)
				dateString = format.format(attempt.getLastModifiedDate());
		
		}*/
			
		//add(new Label("attempt.number", String.valueOf(attemptNumber)));
		//add(new Label("attempt.date", dateString));
		//add(new Label("attempt.suspended", String.valueOf(isSuspended)));
		//add(new Label("attempt.clean.exit", String.valueOf(!isNotExited)));
		
		initializePage(contentPackage, learner, attemptNumber, pageParams);
	}
	
	protected abstract void initializePage(ContentPackage contentPackage, Learner learner, long attemptNumber, PageParameters pageParams);
	
	
	protected abstract BookmarkablePageLabeledLink newAttemptNumberLink(long i, PageParameters params);
	
	/*
	 * Copied the basic organization of this method from an Apache Wicket class
	 * 	org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
	 * originally authored by Igor Vaynberg (ivaynberg)
	 */
	protected void addAttemptNumberLink(long i, PageParameters params, RepeatingView container, long current)
	{
		params.put("attemptNumber", i);
		
		BookmarkablePageLabeledLink link = newAttemptNumberLink(i, params);

		if (i == current) {
			link.setEnabled(false);
		}
			
		WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
		item.setRenderBodyOnly(true);
		item.add(link);

		container.add(item);
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
}
