/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.wicket.component.SakaiAjaxButton;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles bulk edits
 */
@Slf4j
public class BulkEditItemsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	private List<Long> deletableItemsList = new ArrayList<Long>();

	public List<Long> getDeletableItemsList () {
		return this.deletableItemsList;
	}

	public void clearDeletableItemsList () {
		this.deletableItemsList.clear();
	}

	public BulkEditItemsPanel(final String id, final IModel<String> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final String siteId = (String) getDefaultModelObject();

		final List<Assignment> assignments = this.businessService.getGradebookAssignments(siteId);

		final IModel<List<Assignment>> model = new ListModel<>(assignments);

		final Form<List<Assignment>> form = new Form<>("form", model);
		form.add(new GradebookItemView("listView", model.getObject()));
		form.add(new SubmitButton("submit").setWillRenderOnClick(true));
		form.add(new CancelButton("cancel"));
		form.add(new Label("releaseToggleAllLabel", getString("label.addgradeitem.toggle.all")));
		form.add(new Label("includeToggleAllLabel", getString("label.addgradeitem.toggle.all")));
		form.add(new Label("deleteToggleAllLabel", getString("label.addgradeitem.toggle.all")));
		add(form);

	}

	class GradebookItemView extends ListView<Assignment> {

		private static final long serialVersionUID = 1L;

		public GradebookItemView(final String id, final List<Assignment> assignments) {
			super(id, assignments);
		}

		@Override
		protected void populateItem(final ListItem<Assignment> item) {

			final Assignment assignment = item.getModelObject();

			item.add(new Label("itemTitle", assignment.getName()));

			final ReleaseCheckbox release = new ReleaseCheckbox("release", new PropertyModel<Boolean>(assignment, "released"));
			final IncludeCheckbox include = new IncludeCheckbox("include", new PropertyModel<Boolean>(assignment, "counted"));
			final AjaxCheckBox delete = new AjaxCheckBox("delete", Model.of(Boolean.FALSE)){
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					updateModel();
					if (this.getModel().getObject()) {  // if the checkbox has just been checked, this will be True.
						BulkEditItemsPanel.this.deletableItemsList.add(item.getModelObject().getId());
					} else {    // this means the checkbox has been unchecked.
						BulkEditItemsPanel.this.deletableItemsList.remove(item.getModelObject().getId());
					}
				}
			};

			// Are there categories in this Gradebook? If so, and this item is not in a category, disabled grade
			// calculation inclusion.
			List<CategoryDefinition> categories = businessService.getGradebookCategories();
			if (categories != null && categories.size() > 0 && StringUtils.isBlank(assignment.getCategoryName())) {
				include.setEnabled(false);
			}
			if (assignment.isExternallyMaintained()){	//don't allow External items to be deleted.
				delete.setEnabled(false);
			}
			item.add(release);
			item.add(include);
			item.add(delete);
		}

	}

	class CancelButton extends SakaiAjaxButton {

		private static final long serialVersionUID = 1L;

		public CancelButton(final String id) {
			super(id);
			setDefaultFormProcessing(false);
		}

		@Override
		public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
			BulkEditItemsPanel.this.clearDeletableItemsList();
			BulkEditItemsPanel.this.window.close(target);
		}

	}

	class SubmitButton extends SakaiAjaxButton {

		private static final long serialVersionUID = 1L;

		public SubmitButton(final String id) {
			super(id);
			setDefaultFormProcessing(false);
		}

		@Override
		public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

			final List<Assignment> assignments = (List<Assignment>) form.getModelObject();

			try {
				for (final Assignment a : assignments) {

					log.debug("Bulk edit assignment: {}", a);
					BulkEditItemsPanel.this.businessService.updateAssignment(a);
				}
				List<Long> deletableItems = BulkEditItemsPanel.this.getDeletableItemsList();
				int deleteCount = deletableItems.size();
				for (int count=0; count < deleteCount; count++) {
					BulkEditItemsPanel.this.businessService.removeAssignment(deletableItems.get(count));
				}
				getSession().success(getString("bulkedit.update.success"));
				if (deleteCount > 0) {
					String deletedList = assignments.stream().filter(a -> deletableItems.contains(a.getId())).map(Assignment::getName)
						.collect(Collectors.joining(", "));
					getSession().success(new StringResourceModel("bulkedit.delete.success", null, new Object[] { deletedList }).getString());
				}
				BulkEditItemsPanel.this.clearDeletableItemsList();
			}
			catch (final Exception e) {
				getSession().error(getString("bulkedit.update.error"));
				log.warn("An error occurred updating the assignment", e);
			}
			setResponsePage(GradebookPage.class);
		}

	}

	/**
	 * Checkbox for the release option
	 */
	class ReleaseCheckbox extends AjaxCheckBox {

		private static final long serialVersionUID = 1L;

		public ReleaseCheckbox(final String id, final IModel<Boolean> model) {
			super(id, model);
			setOutputMarkupId(true);
		}

		@Override
		protected void onUpdate(final AjaxRequestTarget target) {
			// intentionally left blank since we are in an ajax form
		}

	}

	/**
	 * Checkbox for the include option
	 */
	class IncludeCheckbox extends AjaxCheckBox {

		private static final long serialVersionUID = 1L;

		public IncludeCheckbox(final String id, final IModel<Boolean> model) {
			super(id, model);
			setOutputMarkupId(true);
		}

		@Override
		protected void onUpdate(final AjaxRequestTarget target) {
			// intentionally left blank since we are in an ajax form
		}

	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		final String version = PortalUtils.getCDNQuery();
		response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-bulk-edit.js%s", version)));

	}
}
