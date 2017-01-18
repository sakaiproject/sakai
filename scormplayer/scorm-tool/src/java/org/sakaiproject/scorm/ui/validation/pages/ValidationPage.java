/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.validation.pages;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;
import org.sakaiproject.wicket.markup.html.repeater.data.table.AjaxImageLinkColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.BasicDataTable;

public class ValidationPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(ValidationPage.class);
	
	private static ResourceReference PAGE_ICON = new ResourceReference(ConsoleBasePage.class, "res/table_link.png");

	private static final ResourceReference validateIconReference = new ResourceReference(ValidationPage.class, "res/add.png");
	private static final ResourceReference refreshIconReference = new ResourceReference(ValidationPage.class, "res/arrow_refresh.png");
	
	
	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormContentService")
	ScormContentService contentService;
	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormResourceService")
	ScormResourceService resourceService;
	
	private final BasicDataTable table;
	
	
	public ValidationPage(PageParameters params) {
		List<Archive> resources = resourceService.getUnvalidatedArchives();

		List<IColumn> columns = new LinkedList<IColumn>();
		
		IModel titleColumnHeader = new ResourceModel("column.title.header");
		IModel actionColumnHeader = new ResourceModel("column.action.header");
		
		columns.add(new PropertyColumn(titleColumnHeader, "title", "title"));
		
		columns.add(new ValidateLinkColumn(actionColumnHeader));
		
		table = new BasicDataTable("archiveTable", columns, resources);
		table.setOutputMarkupId(true);
		add(table);
	}
	
	@Override
	protected ResourceReference getPageIconReference() {
		return PAGE_ICON;
	}
	
	public class ValidateLinkColumn extends AjaxImageLinkColumn {

		private static final long serialVersionUID = 1L;

		public ValidateLinkColumn(IModel displayModel) {
			super(displayModel);
		}

		@Override
		public void onClick(Object bean, AjaxRequestTarget target) {
			final Archive archive = (Archive)bean;
			
			try {
				contentService.validate(archive.getResourceId(), ServerConfigurationService.getString("scorm.zip.encoding", "UTF-8"));
				target.addComponent(table);
				
			} catch (Exception e) {
				log.error("Caught an exception validating ", e);
			}
		}

		@Override
		public ResourceReference getIconReference(Object bean) {
			final Archive archive = (Archive)bean;
			
			if (archive.isValidated())
				return refreshIconReference;
				
			return validateIconReference;
		}
		
	}
	
	
}
