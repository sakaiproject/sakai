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
package org.sakaiproject.scorm.ui.console.pages;

import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.apache.wicket.Component;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;

import static org.sakaiproject.scorm.api.ScormConstants.*;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.ui.console.components.DecoratedDatePropertyColumn;
import org.sakaiproject.scorm.ui.player.pages.ScormPlayerPage;
import org.sakaiproject.scorm.ui.reporting.pages.LearnerResultsPage;
import org.sakaiproject.scorm.ui.reporting.pages.ResultsListPage;
import org.sakaiproject.wicket.ajax.markup.html.table.SakaiDataTable;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ImageLinkColumn;

@Slf4j
public class PackageListPage extends ConsoleBasePage
{
	private static final PackageResourceReference DELETE_ICON = new PackageResourceReference(PackageListPage.class, "res/delete.png");

	@SpringBean
	LearningManagementSystem lms;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormContentService")
	ScormContentService contentService;

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		listLink.disable();
	}

	public PackageListPage(PageParameters params)
	{
		final String context = lms.currentContext();
		List<ContentPackage> contentPackages = contentService.getContentPackages(context);

		final boolean canConfigure = lms.canConfigure(context);
		final boolean canGrade = lms.canGrade(context);
		final boolean canViewResults = lms.canViewResults(context);
		final boolean canDelete = lms.canDelete(context);

		StringValue uploadSuccessMessage = params.get("uploadSuccess");
		if (uploadSuccessMessage != null && StringUtils.isNotBlank(uploadSuccessMessage.toString()))
		{
			success(uploadSuccessMessage);
		}

		List<IColumn> columns = new LinkedList<>();
		ActionColumn actionColumn = new ActionColumn(new StringResourceModel("column.header.content.package.name", this, null), "title", "title");
		String[] paramPropertyExpressions = {"contentPackageId", "resourceId", "title"};

		Action launchAction = new Action("title", ScormPlayerPage.class, paramPropertyExpressions)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Component newLink(String id, Object bean)
			{
				IModel<String> labelModel;
				if (displayModel != null)
				{
					labelModel = displayModel;
				}
				else
				{
					String labelValue = String.valueOf(PropertyResolver.getValue(labelPropertyExpression, bean));
					labelModel = new Model<>(labelValue);
				}

				PageParameters params = buildPageParameters(paramPropertyExpressions, bean);
				Link link = new BookmarkablePageLabeledLink(id, labelModel, pageClass, params);

				if (popupWindowName != null)
				{
					PopupSettings popupSettings = new PopupSettings(popupWindowName, PopupSettings.RESIZABLE);
					popupSettings.setWidth(1020);
					popupSettings.setHeight(740);
					popupSettings.setWindowName(popupWindowName);

					link.setPopupSettings(popupSettings);
				}

				link.setEnabled(isEnabled() && lms.canLaunch((ContentPackage) bean));
				link.setVisible(isVisible());
				return link;
			}
		};

		actionColumn.addAction(launchAction);

		if (lms.canLaunchNewWindow())
		{
			launchAction.setPopupWindowName(new ResourceModel("popup.window.name").toString());
		}

		if (canConfigure)
		{
			actionColumn.addAction(new Action(new ResourceModel("column.action.edit.label"), PackageConfigurationPage.class, paramPropertyExpressions));
		}
		if (canGrade)
		{
			actionColumn.addAction(new Action(new StringResourceModel("column.action.grade.label", this, null), ResultsListPage.class, paramPropertyExpressions));
		}
		else if (canViewResults)
		{
			actionColumn.addAction(new Action(new StringResourceModel("column.action.grade.label", this, null), LearnerResultsPage.class, paramPropertyExpressions));
		}

		columns.add(actionColumn);
		columns.add(new StatusColumn(new StringResourceModel("column.header.status", this, null), "status"));
		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.releaseOn", this, null), "releaseOn", "releaseOn"));
		columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.dueOn", this, null), "dueOn", "dueOn"));

		if (canConfigure)
		{
			columns.add(new DisplayNameColumn(new StringResourceModel("column.header.lastModifiedBy", this, null), "modifiedBy"));
			columns.add(new DecoratedDatePropertyColumn(new StringResourceModel("column.header.lastModifiedOn", this, null), "modifiedOn", "modifiedOn"));
		}

		if (canDelete)
		{
			columns.add(new ImageLinkColumn(new Model("Remove"), PackageRemovePage.class, paramPropertyExpressions, DELETE_ICON));
		}

		SakaiDataTable table = new SakaiDataTable("cpTable", columns, new ContentPackageDataProvider(contentPackages), true);
		add(table);
	}

	public class DisplayNameColumn extends AbstractColumn<ContentPackage, String>
	{
		private static final long serialVersionUID = 1L;

		public DisplayNameColumn(IModel<String> displayModel, String sortProperty)
		{
			super(displayModel, sortProperty);
		}

		@Override
		public void populateItem(Item<ICellPopulator<ContentPackage>> item, String componentID, IModel<ContentPackage> model)
		{
			item.add(new Label(componentID, getLearnerName(model)));
		}

		protected String getLearnerName(IModel<ContentPackage> embeddedModel)
		{
			ContentPackage pkg = embeddedModel.getObject();
			switch (getSortProperty())
			{
				case "modifiedBy":
					return lms.getLearnerName(pkg.getModifiedBy());
				default:
					return lms.getLearnerName(pkg.getCreatedBy());
			}
		}
	}

	public class StatusColumn extends AbstractColumn<ContentPackage, String>
	{
		private static final long serialVersionUID = 1L;

		public StatusColumn(IModel<String> displayModel, String sortProperty)
		{
			super(displayModel, sortProperty);
		}

		@Override
		public void populateItem(Item<ICellPopulator<ContentPackage>> item, String componentId, IModel<ContentPackage> model)
		{
			item.add(new Label(componentId, createLabelModel(model)));
		}

		protected IModel<String> createLabelModel(IModel<ContentPackage> embeddedModel)
		{
			String resourceId = "status.unknown";
			Object target = embeddedModel.getObject();

			if (target instanceof ContentPackage)
			{
				ContentPackage contentPackage = (ContentPackage)target;
				int status = contentService.getContentPackageStatus(contentPackage);

				switch (status)
				{
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
}
