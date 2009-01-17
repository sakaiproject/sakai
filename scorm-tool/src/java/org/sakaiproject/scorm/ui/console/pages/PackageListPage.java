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
package org.sakaiproject.scorm.ui.console.pages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.PropertyResolver;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.ui.console.components.DecoratedDatePropertyColumn;
import org.sakaiproject.scorm.ui.player.pages.PlayerPage;
import org.sakaiproject.scorm.ui.reporting.pages.ResultsListPage;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.BasicDataTable;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ImageLinkColumn;

import java.util.LinkedList;
import java.util.List;

public class PackageListPage extends ConsoleBasePage implements ScormConstants {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(PackageListPage.class);
	
	private static ResourceReference PAGE_ICON = new ResourceReference(PackageListPage.class, "res/table.png");
	private static final ResourceReference deleteIconReference = new ResourceReference(PackageListPage.class, "res/delete.png");
	
	@SpringBean
	LearningManagementSystem lms;
	@SpringBean
	ScormContentService contentService;
	
	public PackageListPage(PageParameters params) {
		
		List<ContentPackage> contentPackages = contentService.getContentPackages();
		
		final String context = lms.currentContext();
		final boolean canConfigure = lms.canConfigure(context);
		final boolean canViewResults = lms.canViewResults(context);
		final boolean canLaunch = lms.canLaunch(context);
		final boolean canDelete = lms.canDelete(context);
		
		List<IColumn> columns = new LinkedList<IColumn>();

		ActionColumn actionColumn = new ActionColumn(new StringResourceModel("column.header.content.package.name", this, null), "title", "title");
			
		String[] paramPropertyExpressions = {"contentPackageId", "resourceId", "title"};
		
		Action launchAction = new Action("title", PlayerPage.class, paramPropertyExpressions){
			@Override
			public Component newLink(String id, Object bean) {
				IModel labelModel = null;
				if (displayModel != null) {
					labelModel = displayModel;
		 		} else {
		 			String labelValue = String.valueOf(PropertyResolver.getValue(labelPropertyExpression, bean));
		 			labelModel = new Model(labelValue);
		 		}
				
				PageParameters params = buildPageParameters(paramPropertyExpressions, bean);
				Link link = new BookmarkablePageLabeledLink(id, labelModel, pageClass, params);

				if (popupWindowName != null) {
					PopupSettings popupSettings = new PopupSettings(PageMap.forName(popupWindowName), PopupSettings.RESIZABLE);
					popupSettings.setWidth(1020);
					popupSettings.setHeight(740);
					
					popupSettings.setWindowName(popupWindowName);
					
		 			link.setPopupSettings(popupSettings);
				}
				
				link.setEnabled(isEnabled(bean));
				link.setVisible(isVisible(bean));
					
		 		return link;
			}
			
		};
		launchAction.setEnabled(canLaunch);
		actionColumn.addAction(launchAction);
		
		if (lms.canLaunchNewWindow()) {
			launchAction.setPopupWindowName("ScormPlayer");
		}
		
		if (canConfigure)
			actionColumn.addAction(new Action(new ResourceModel("column.action.edit.label"), PackageConfigurationPage.class, paramPropertyExpressions));
			
		if (canViewResults)
			actionColumn.addAction(new Action(new StringResourceModel("column.action.grade.label", this, null), ResultsListPage.class, paramPropertyExpressions));
				
		columns.add(actionColumn);
		
		columns.add(new StatusColumn(new StringResourceModel("column.header.status", this, null), "status"));
		
		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.releaseOn", this, null), "releaseOn", "releaseOn"));

		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.dueOn", this, null), "dueOn", "dueOn"));

		
		if (canDelete)
			columns.add(new ImageLinkColumn(new Model("Remove"), PackageRemovePage.class, paramPropertyExpressions, deleteIconReference));

		BasicDataTable table = new BasicDataTable("cpTable", columns, contentPackages);
		
		add(table);
	}	
	
	
	public class StatusColumn extends AbstractColumn {

		private static final long serialVersionUID = 1L;

		public StatusColumn(IModel displayModel, String sortProperty) {
			super(displayModel, sortProperty);
		}

		public void populateItem(Item item, String componentId, IModel model) {
			item.add(new Label(componentId, createLabelModel(model)));
		}
		
		protected IModel createLabelModel(IModel embeddedModel)
		{
			String resourceId = "status.unknown";
			Object target = embeddedModel.getObject();
			
			if (target instanceof ContentPackage) {
				ContentPackage contentPackage = (ContentPackage)target;
				
				int status = contentService.getContentPackageStatus(contentPackage);
				
				switch (status) {
				case CONTENT_PACKAGE_STATUS_OPEN:
					resourceId = "status.open";
					break;
				case CONTENT_PACKAGE_STATUS_OVERDUE:
					resourceId = "status.overdue";
					break;
				case CONTENT_PACKAGE_STATUS_CLOSED:
					resourceId = "status.closed";
					break;
				case CONTENT_PACKAGE_STATUS_NOTYETOPEN:
					resourceId = "status.notyetopen";
					break;
				}
			}
			
			return new ResourceModel(resourceId);
		}
	}
	
	protected ResourceReference getPageIconReference() {
		return PAGE_ICON;
	}
	
	
}
