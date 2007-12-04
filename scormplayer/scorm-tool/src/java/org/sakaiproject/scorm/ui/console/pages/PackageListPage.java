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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormConstants;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormPermissionService;
import org.sakaiproject.scorm.ui.console.components.DecoratedPropertyColumn;
import org.sakaiproject.scorm.ui.player.pages.PlayerPage;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ImageLinkColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.SakaiBasicDataTable;

public class PackageListPage extends ConsoleBasePage implements ScormConstants {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(PackageListPage.class);
	
	private static final ResourceReference deleteIconReference = new ResourceReference(PackageListPage.class, "res/cross.png");
	
	@SpringBean
	ScormContentService contentService;
	@SpringBean
	ScormPermissionService permissionService;
	
	public PackageListPage(PageParameters params) {
		
		List<ContentPackage> contentPackages = contentService.getSiteContentPackages();
		
		final boolean canConfigure = permissionService.canConfigure();
		final boolean canViewResults = permissionService.canViewResults();
		final boolean canLaunch = permissionService.canLaunch();
		final boolean canDelete = permissionService.canDelete();
		
		List<IColumn> columns = new LinkedList<IColumn>();

		ActionColumn actionColumn = new ActionColumn(new StringResourceModel("column.header.content.package.name", this, null), "title", "title");
			
		String[] paramPropertyExpressions = {"courseId", "title", "id"};
		
		Action launchAction = new Action("title", PlayerPage.class, paramPropertyExpressions, "ScormPlayer");
		launchAction.setEnabled(canLaunch);
		actionColumn.addAction(launchAction);
		
		if (canConfigure)
			actionColumn.addAction(new Action(new StringResourceModel("column.action.edit.label", this, null), PackageConfigurationPage.class, paramPropertyExpressions));
			
		if (canViewResults)
			actionColumn.addAction(new Action(new StringResourceModel("column.action.grade.label", this, null), AttemptListPage.class, paramPropertyExpressions));
				
		columns.add(actionColumn);
		
		
		columns.add(new PropertyColumn(new StringResourceModel("column.header.status", this, null), "status", "status"));
		
		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.releaseOn", this, null), "releaseOn", "releaseOn"));

		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.dueOn", this, null), "dueOn", "dueOn"));

		
		if (canDelete)
			columns.add(new ImageLinkColumn(new Model("Remove"), PackageRemovePage.class, paramPropertyExpressions, deleteIconReference));
		
		
		IModel captionModel = new ResourceModel("table.caption");
		
		SakaiBasicDataTable table = new SakaiBasicDataTable("cpTable", columns, contentPackages, 10, captionModel);
		
		add(table);
	}	
	
	public class DecoratedDatePropertyColumn extends DecoratedPropertyColumn {

		private static final long serialVersionUID = 1L;
		
		private SimpleDateFormat dateFormat;
		
		public DecoratedDatePropertyColumn(IModel displayModel, String sortProperty, String propertyExpression) {
			super(displayModel, sortProperty, propertyExpression);
			this.dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		}

		@Override
		public Object convertObject(Object object) {
			
			if (object instanceof Date)
				return dateFormat.format(object);
			
			return object;
		}
		
	}
	
	
	
}
