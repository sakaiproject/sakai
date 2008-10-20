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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.Icon;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.table.DecoratedPropertyColumn;

public abstract class BaseResultsPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(BaseResultsPage.class);
	
	private static ResourceReference NEXT_ICON = new ResourceReference(BaseResultsPage.class, "res/arrow_right.png");
	private static ResourceReference PREV_ICON = new ResourceReference(BaseResultsPage.class, "res/arrow_left.png");
	
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
		
		initializePage(contentPackage, learner, attemptNumber, pageParams);
		
		String scoId = pageParams.getString("scoId");
		String interactionId = pageParams.getString("interactionId");
		
		String[] siblingIds = resultService.getSiblingIds(contentPackageId, learnerId, attemptNumber, scoId, interactionId);
		
		Link previousLink = newPreviousLink(siblingIds[0], pageParams);
		Link nextLink = newNextLink(siblingIds[1], pageParams);
		
		Icon previousIcon = new Icon("previousIcon", PREV_ICON);
		Icon nextIcon = new Icon("nextIcon", NEXT_ICON);
		
		previousIcon.setVisible(previousLink.isVisible());
		nextIcon.setVisible(nextLink.isVisible());

		add(previousLink);
		add(previousIcon);
		
		add(nextLink);
		add(nextIcon);
	}
	
	protected Link newPreviousLink(String previousId, PageParameters pageParams) {
		Link link = new PageLink("previousLink", BaseResultsPage.class);
		link.setVisible(false);
		return link;
	}
	
	protected Link newNextLink(String nextId, PageParameters pageParams) {
		Link link = new PageLink("nextLink", BaseResultsPage.class);
		link.setVisible(false);
		return link;
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
