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

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.model.api.Objective;
import org.sakaiproject.scorm.ui.reporting.components.InteractionPanel;
import org.sakaiproject.scorm.ui.reporting.util.ObjectiveProvider;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.presenter.EnhancedDataPresenter;

public class InteractionResultsPage extends BaseResultsPage {

	private static final long serialVersionUID = 1L;

	private static ResourceReference PAGE_ICON = new ResourceReference(InteractionResultsPage.class, "res/report_magnify.png");
	
	public InteractionResultsPage(PageParameters pageParams) {
		super(pageParams);
	}
	
	@Override
	protected ResourceReference getPageIconReference() {
		return PAGE_ICON;
	}
	
	@Override
	protected void initializePage(ContentPackage contentPackage,
			Learner learner, long attemptNumber, PageParameters pageParams) {
		String scoId = pageParams.getString("scoId");
		
		PageParameters uberuberparentParams = new PageParameters();
		uberuberparentParams.put("contentPackageId", contentPackage.getContentPackageId());
		
		PageParameters parentParams = new PageParameters();
		parentParams.put("contentPackageId", contentPackage.getContentPackageId());
		parentParams.put("learnerId", learner.getId());
		parentParams.put("attemptNumber", attemptNumber);
		
		String interactionId = pageParams.getString("interactionId");
		
		Interaction interaction = resultService.getInteraction(contentPackage.getContentPackageId(), learner.getId(), attemptNumber, scoId, interactionId);
		
		add(new InteractionPanel("interactionPanel", interaction));
		
		IModel breadcrumbModel = new StringResourceModel("uberuberparent.breadcrumb", this, new Model(contentPackage));
		addBreadcrumb(breadcrumbModel, ResultsListPage.class, uberuberparentParams, true);	
		addBreadcrumb(new Model(learner.getDisplayName()), LearnerResultsPage.class, parentParams, true);
		addBreadcrumb(new Model(interaction.getActivityTitle()), ScoResultsPage.class, pageParams, true);
		addBreadcrumb(new Model(interaction.getInteractionId()), InteractionResultsPage.class, pageParams, false);
		
		List<Objective> objectives = interaction.getObjectives();
		ObjectiveProvider dataProvider = new ObjectiveProvider(objectives);
		dataProvider.setTableTitle("Objectives");
		EnhancedDataPresenter presenter = new EnhancedDataPresenter("objectivePresenter", getColumns(), dataProvider);
		add(presenter);	
		
		presenter.setVisible(objectives != null && objectives.size() > 0);
	}

	@Override
	protected Link newPreviousLink(String previousId, PageParameters pageParams) {
		PageParameters prevParams = new PageParameters();
		
		long contentPackageId = pageParams.getLong("contentPackageId");
		String learnerId = pageParams.getString("learnerId");
		long attemptNumber = pageParams.getLong("attemptNumber");
		String scoId = pageParams.getString("scoId");
		
		prevParams.put("contentPackageId", contentPackageId);
		prevParams.put("learnerId", learnerId);
		prevParams.put("attemptNumber", attemptNumber);
		prevParams.put("scoId", scoId);
		prevParams.put("interactionId", previousId);
		
		Link link = new BookmarkablePageLabeledLink("previousLink", new ResourceModel("previous.link.label"), InteractionResultsPage.class, prevParams);
		link.setVisible(previousId.trim().length() > 0);
		return link;
	}
	
	@Override
	protected Link newNextLink(String nextId, PageParameters pageParams) {
		PageParameters nextParams = new PageParameters();
		
		long contentPackageId = pageParams.getLong("contentPackageId");
		String learnerId = pageParams.getString("learnerId");
		long attemptNumber = pageParams.getLong("attemptNumber");
		String scoId = pageParams.getString("scoId");
		
		nextParams.put("contentPackageId", contentPackageId);
		nextParams.put("learnerId", learnerId);
		nextParams.put("attemptNumber", attemptNumber);
		nextParams.put("scoId", scoId);
		nextParams.put("interactionId", nextId);
		
		Link link = new BookmarkablePageLabeledLink("nextLink", new ResourceModel("next.link.label"), InteractionResultsPage.class, nextParams);

		link.setVisible(nextId.trim().length() > 0);
		return link;
	}
	
	
	@Override
	protected BookmarkablePageLabeledLink newAttemptNumberLink(long i, PageParameters params) {
		return new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), InteractionResultsPage.class, params);
	}

	private List<IColumn> getColumns() {
		IModel idHeader = new ResourceModel("column.header.id");
		IModel descriptionHeader = new ResourceModel("column.header.description");
		IModel completionStatusHeader = new ResourceModel("column.header.completion.status");
		IModel successStatusHeader = new ResourceModel("column.header.success.status");
		
		List<IColumn> columns = new LinkedList<IColumn>();

		columns.add(new PropertyColumn(idHeader, "id", "id"));
		columns.add(new PropertyColumn(descriptionHeader, "description", "description"));
		columns.add(new PropertyColumn(completionStatusHeader, "completionStatus", "completionStatus"));
		columns.add(new PropertyColumn(successStatusHeader, "successStatus", "successStatus"));
		
		return columns;
	}
	
}
