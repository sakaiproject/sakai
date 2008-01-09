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

import javax.servlet.http.HttpServletRequest;

import org.adl.sequencer.SeqNavRequests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;
import org.sakaiproject.scorm.ui.reporting.components.ReportingActivityTree;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;

public class LearnerExperienceDetailPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(LearnerExperienceDetailPage.class);
	
	//private final RepeatingView fieldGroups;
	
	@SpringBean
	transient LearningManagementSystem lms;
	@SpringBean
	transient ScormContentService contentService;
	@SpringBean
	transient ScormResultService resultService;
	@SpringBean
	transient ScormSequencingService sequencingService;
	
	private final RepeatingView attemptNumberLinks;
	
	public LearnerExperienceDetailPage(PageParameters pageParams) {
		long contentPackageId = pageParams.getLong("contentPackageId");
		String learnerId = pageParams.getString("learnerId");
		
		String learnerName = lms.getLearnerName(learnerId);
		
		ContentPackage contentPackage = contentService.getContentPackage(contentPackageId);
		
		add(new Label("content.package.name", contentPackage.getTitle()));
		add(new Label("learner.name", learnerName));
		
		int numberOfAttempts = resultService.getNumberOfAttempts(contentPackageId, learnerId);
		
		long attemptNumber = 0;
		
		if (pageParams.containsKey("attemptNumber")) 
			attemptNumber = pageParams.getLong("attemptNumber");
		
		if (attemptNumber == 0)
			attemptNumber = numberOfAttempts;
		
		SessionBean sessionBean = sequencingService.newSessionBean(contentPackage.getResourceId(), contentPackageId);
		sessionBean.setCompletionUrl(getCompletionUrl());
		sessionBean.setAttemptNumber(attemptNumber);
		sequencingService.navigate(SeqNavRequests.NAV_NONE, sessionBean, null, null);
		
		add(new ReportingActivityTree("tree", sessionBean, null));
		
		this.attemptNumberLinks = new RepeatingView("attemptNumberLinks");
		add(attemptNumberLinks);
		
		for (long i=numberOfAttempts;i>=1;i--) {
			this.addAttemptNumberLink(i, pageParams, attemptNumberLinks, attemptNumber);
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
		
		BookmarkablePageLabeledLink link = new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), LearnerExperienceDetailPage.class, params);
		//link.setRenderBodyOnly(true);

		if (i == current)
			link.setEnabled(false);
		
		WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
		item.setRenderBodyOnly(true);
		item.add(link);

		container.add(item);
	}
	
	private String getCompletionUrl() {
		RequestCycle cycle = getRequestCycle();
		IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
		WebRequest webRequest = (WebRequest)getRequest();
		HttpServletRequest servletRequest = webRequest.getHttpServletRequest();
		String toolUrl = servletRequest.getContextPath();
		
		Class<?> pageClass = AttemptListPage.class;
			
		CharSequence completionUrl = encoder.encode(cycle, new BookmarkablePageRequestTarget(pageClass, new PageParameters()));
		AppendingStringBuffer url = new AppendingStringBuffer();
		url.append(toolUrl).append("/").append(completionUrl);
		
		return url.toString();
	}
	
}
