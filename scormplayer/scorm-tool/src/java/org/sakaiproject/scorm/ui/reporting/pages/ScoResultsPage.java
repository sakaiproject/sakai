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
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.ActivityReport;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.ui.reporting.components.InteractionPanel;
import org.sakaiproject.scorm.ui.reporting.components.ProgressPanel;
import org.sakaiproject.scorm.ui.reporting.components.ScorePanel;
import org.sakaiproject.scorm.ui.reporting.util.InteractionProvider;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.presenter.EnhancedDataPresenter;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.DecoratedPropertyColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ImageLinkColumn;

public class ScoResultsPage extends BaseResultsPage {

	private static final long serialVersionUID = 1L;

	private static ResourceReference PAGE_ICON = new ResourceReference(ScoResultsPage.class, "res/report_picture.png");
	
	private static ResourceReference BLANK_ICON = new ResourceReference(InteractionPanel.class, "res/brick.png");
	private static ResourceReference CORRECT_ICON = new ResourceReference(InteractionPanel.class, "res/tick.png");
	private static ResourceReference INCORRECT_ICON = new ResourceReference(InteractionPanel.class, "res/cross.png");
	private static ResourceReference UNANTICIPATED_ICON = new ResourceReference(InteractionPanel.class, "res/exclamation.png");
	private static ResourceReference NEUTRAL_ICON = new ResourceReference(InteractionPanel.class, "res/page_white_text.png");

	
	@SpringBean
	transient ScormResultService resultService;
	
	public ScoResultsPage(PageParameters pageParams) {
		super(pageParams);
		
	}
	
	@Override
	protected ResourceReference getPageIconReference() {
		return PAGE_ICON;
	}

	@Override
	protected void initializePage(ContentPackage contentPackage, Learner learner, long attemptNumber, PageParameters pageParams) {
		String scoId = pageParams.getString("scoId");
		
		PageParameters uberparentParams = new PageParameters();
		uberparentParams.put("contentPackageId", contentPackage.getContentPackageId());
		
		PageParameters parentParams = new PageParameters();
		parentParams.put("contentPackageId", contentPackage.getContentPackageId());
		parentParams.put("learnerId", learner.getId());
		parentParams.put("attemptNumber", attemptNumber);
		
		ActivityReport report = resultService.getActivityReport(contentPackage.getContentPackageId(), learner.getId(), attemptNumber, scoId);
		
		add(new ScorePanel("scorePanel", report.getScore()));
		
		add(new ProgressPanel("progressPanel", report.getProgress()));
		
		IModel breadcrumbModel = new StringResourceModel("uberparent.breadcrumb", this, new Model(contentPackage));
		addBreadcrumb(breadcrumbModel, ResultsListPage.class, uberparentParams, true);	
		addBreadcrumb(new Model(learner.getDisplayName()), LearnerResultsPage.class, parentParams, true);
		addBreadcrumb(new Model(report.getTitle()), ScoResultsPage.class, pageParams, false);
		
		List<Interaction> interactions = report.getInteractions();
		InteractionProvider dataProvider = new InteractionProvider(interactions);
		dataProvider.setTableTitle("Interactions");
		EnhancedDataPresenter presenter = new EnhancedDataPresenter("interactionPresenter", getColumns(), dataProvider);
		add(presenter);
		
		presenter.setVisible(interactions != null && interactions.size() > 0);
	}
	
	@Override
	protected Link newPreviousLink(String previousId, PageParameters pageParams) {
		PageParameters prevParams = new PageParameters();
		
		long contentPackageId = pageParams.getLong("contentPackageId");
		String learnerId = pageParams.getString("learnerId");
		long attemptNumber = pageParams.getLong("attemptNumber");
		
		prevParams.put("contentPackageId", contentPackageId);
		prevParams.put("learnerId", learnerId);
		prevParams.put("attemptNumber", attemptNumber);
		prevParams.put("scoId", previousId);
		
		Link link = new BookmarkablePageLabeledLink("previousLink", new ResourceModel("previous.link.label"), ScoResultsPage.class, prevParams);
		link.setVisible(previousId.trim().length() > 0);
		return link;
	}
	
	@Override
	protected Link newNextLink(String nextId, PageParameters pageParams) {
		PageParameters nextParams = new PageParameters();
		
		long contentPackageId = pageParams.getLong("contentPackageId");
		String learnerId = pageParams.getString("learnerId");
		long attemptNumber = pageParams.getLong("attemptNumber");
		
		nextParams.put("contentPackageId", contentPackageId);
		nextParams.put("learnerId", learnerId);
		nextParams.put("attemptNumber", attemptNumber);
		nextParams.put("scoId", nextId);
		
		Link link = new BookmarkablePageLabeledLink("nextLink", new ResourceModel("next.link.label"), ScoResultsPage.class, nextParams);

		link.setVisible(nextId.trim().length() > 0);
		return link;
	}
	
	@Override
	protected BookmarkablePageLabeledLink newAttemptNumberLink(long i, PageParameters params) {
		return new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), ScoResultsPage.class, params);
	}
	
	private List<IColumn> getColumns() {
		IModel idHeader = new ResourceModel("column.header.id");
		IModel descriptionHeader = new ResourceModel("column.header.description");
		IModel typeHeader = new ResourceModel("column.header.type");
		IModel resultHeader = new ResourceModel("column.header.result");
		
		List<IColumn> columns = new LinkedList<IColumn>();

		String[] paramPropertyExpressions = {"contentPackageId", "learnerId", "scoId", "attemptNumber", "interactionId"};
		
		ActionColumn actionColumn = new ActionColumn(idHeader, "interactionId", "interactionId");
		actionColumn.addAction(new Action("interactionId", InteractionResultsPage.class, paramPropertyExpressions));
		columns.add(actionColumn);
		columns.add(new PropertyColumn(descriptionHeader, "description", "description"));
		columns.add(new TypePropertyColumn(typeHeader, "type", "type"));
		columns.add(new IconPropertyColumn(resultHeader, InteractionResultsPage.class, paramPropertyExpressions, "result"));
		
		/*ResourceReference resultIconReference = BLANK_ICON;
		String result = interaction.getResult();
		if (result != null) {
			if (result.equalsIgnoreCase("correct"))
				resultIconReference = CORRECT_ICON;
			else if (result.equalsIgnoreCase("incorrect"))
				resultIconReference = INCORRECT_ICON;
			else if (result.equalsIgnoreCase("neutral")) {
				resultIconReference = NEUTRAL_ICON;
				isNeutral = true;
			} else if (result.equalsIgnoreCase("unanticipated"))
				resultIconReference = UNANTICIPATED_ICON;
			else
				showIcon = false;
		} else
			showIcon = false;
		*/
		
		return columns;
	}

	public class IconPropertyColumn extends ImageLinkColumn {

		private static final long serialVersionUID = 1L;

		public IconPropertyColumn(IModel displayModel, Class<?> pageClass,
				String[] paramPropertyExpressions, String iconProperty) {
			super(displayModel, pageClass, paramPropertyExpressions, iconProperty);
		}
		
		protected ResourceReference getIconPropertyReference(String iconPropertyValue) {
			ResourceReference resultIconReference = BLANK_ICON;

			if (iconPropertyValue != null) {
				if (iconPropertyValue.equalsIgnoreCase("correct"))
					resultIconReference = CORRECT_ICON;
				else if (iconPropertyValue.equalsIgnoreCase("incorrect"))
					resultIconReference = INCORRECT_ICON;
				else if (iconPropertyValue.equalsIgnoreCase("neutral")) 
					resultIconReference = NEUTRAL_ICON;
				else if (iconPropertyValue.equalsIgnoreCase("unanticipated"))
					resultIconReference = UNANTICIPATED_ICON;
			}
			
			return resultIconReference;
		}
		
	}
	
	public class TypePropertyColumn extends DecoratedPropertyColumn {

		private static final long serialVersionUID = 1L;

		public TypePropertyColumn(IModel displayModel, String sortProperty,
				String propertyExpression) {
			super(displayModel, sortProperty, propertyExpression);
		}

		@Override
		public Object convertObject(Object object) {
			String key = new StringBuilder("type.").append(object).toString();
			return getLocalizer().getString(key, ScoResultsPage.this);
		}
		
		
	}
	
	
}
