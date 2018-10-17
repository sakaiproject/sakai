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

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles bulk edits
 */
@Slf4j
public class BulkEditItemsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

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
		form.add(new SubmitButton("submit"));
		form.add(new CancelButton("cancel"));

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

			item.add(release);
			item.add(include);
		}

	}

	class CancelButton extends GbAjaxButton {

		private static final long serialVersionUID = 1L;

		public CancelButton(final String id) {
			super(id);
			setDefaultFormProcessing(false);
		}

		@Override
		public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
			BulkEditItemsPanel.this.window.close(target);
		}

	}

	class SubmitButton extends GbAjaxButton {

		private static final long serialVersionUID = 1L;

		public SubmitButton(final String id) {
			super(id);
			setDefaultFormProcessing(false);
		}

		@Override
		public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

			final List<Assignment> assignments = (List<Assignment>) form.getModelObject();

			boolean result = false;
			for (final Assignment a : assignments) {

				log.error("Bulk edit assignment: " + a);
				result = BulkEditItemsPanel.this.businessService.updateAssignment(a);
			}

			if (result) {
				getSession().success(getString("bulkedit.update.success"));
			} else {
				getSession().error(getString("bulkedit.update.error"));
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

}
