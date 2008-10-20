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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.LearnerExperience;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.ui.NameValuePair;
import org.sakaiproject.scorm.ui.console.components.AccessStatusColumn;
import org.sakaiproject.scorm.ui.console.components.AttemptNumberAction;
import org.sakaiproject.scorm.ui.console.components.ContentPackageDetailPanel;
import org.sakaiproject.scorm.ui.console.components.DecoratedDatePropertyColumn;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;
import org.sakaiproject.wicket.markup.html.repeater.data.presenter.EnhancedDataPresenter;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class ResultsListPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;

	private static ResourceReference PAGE_ICON = new ResourceReference(LearnerResultsPage.class, "res/report.png");
	
	private static Log log = LogFactory.getLog(ResultsListPage.class);
	
	@SpringBean
	ScormContentService contentService;
	@SpringBean
	ScormResultService resultService;
	
	public ResultsListPage(PageParameters pageParams) {
		super(pageParams);
		
		final long contentPackageId = pageParams.getLong("contentPackageId");
		
		ContentPackage contentPackage = contentService.getContentPackage(contentPackageId);
			
		addBreadcrumb(new Model(contentPackage.getTitle()), ResultsListPage.class, new PageParameters(), false);	
		
		AttemptDataProvider dataProvider = new AttemptDataProvider(contentPackageId);
		dataProvider.setFilterConfigurerVisible(true);
		dataProvider.setTableTitle(getLocalizer().getString("table.title", this));

		EnhancedDataPresenter presenter = new EnhancedDataPresenter("attemptPresenter", getColumns(), dataProvider);
		
		add(presenter);
		
		add(new ContentPackageDetailPanel("details", contentPackage));
	}

	
	private List<IColumn> getColumns() {
		IModel learnerNameHeader = new ResourceModel("column.header.learner.name");
		IModel attemptedHeader = new ResourceModel("column.header.attempted");
		IModel statusHeader = new ResourceModel("column.header.status");
		IModel numberOfAttemptsHeader = new ResourceModel("column.header.attempt.number");
		IModel scoreHeader = new ResourceModel("column.header.score");
	
		List<IColumn> columns = new LinkedList<IColumn>();
		
		ActionColumn actionColumn = new ActionColumn(learnerNameHeader, "learnerName", "learnerName");
		
		String[] paramPropertyExpressions = {"contentPackageId", "learnerId"};
		
		Action summaryAction = new Action("learnerName", LearnerResultsPage.class, paramPropertyExpressions);
		actionColumn.addAction(summaryAction);
		columns.add(actionColumn);
		
		columns.add(new DecoratedDatePropertyColumn(attemptedHeader, "lastAttemptDate", "lastAttemptDate"));
		
		columns.add(new AccessStatusColumn(statusHeader, "status"));
		
		
		ActionColumn attemptNumberActionColumn = new ActionColumn(numberOfAttemptsHeader, "numberOfAttempts", "numberOfAttempts");
		attemptNumberActionColumn.addAction(new AttemptNumberAction("numberOfAttempts", LearnerResultsPage.class, paramPropertyExpressions));
		columns.add(attemptNumberActionColumn);
		

		return columns;
	}
	
	public class AttemptDataProvider extends EnhancedDataProvider {

		private static final long serialVersionUID = 1L;

		private final List<LearnerExperience> learnerExperiences;
		
		public AttemptDataProvider(long contentPackageId) {
			this.learnerExperiences = resultService.getLearnerExperiences(contentPackageId);
		}
		
		public Iterator iterator(int first, int last) {
			return learnerExperiences.subList(first, last).iterator();
		}

		public int size() {
			return learnerExperiences.size();
		}
		
		@Override
		public List getFilterList() {
			List<NameValuePair> list = new LinkedList<NameValuePair>();
					
			list.add(new NameValuePair("All Groups / Sections", "ALL_GROUPS"));
			
			return list;
		}
	}
	
	protected ResourceReference getPageIconReference() {
		return PAGE_ICON;
	}
	
	
	
	
}
