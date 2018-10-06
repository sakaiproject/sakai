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
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * Handles bulk edits
 */
public class BulkEditItemsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	private Form form;
	private AjaxCheckBox release;
	private AjaxCheckBox include;

	public BulkEditItemsPanel(final String id, final IModel<String> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final String siteId = (String) getDefaultModelObject();

		final List<Assignment> assignments = this.businessService.getGradebookAssignments(siteId);

		add(new GradebookItemView("listView", assignments));

		this.form = new Form<Void>("form");
		add(this.form);

		this.release = new ReleaseCheckbox("release", Model.of(Boolean.TRUE));
		this.form.add(this.release);

		this.include = new IncludeCheckbox("include", Model.of(Boolean.TRUE));
		this.form.add(this.include);

		this.form.add(new CancelButton("cancel"));

	}

	class BulkEditForm extends Form<Void> {

		private static final long serialVersionUID = 1L;

		public BulkEditForm(final String id) {
			super(id);
		}
	}

	class GradebookItemView extends ListView<Assignment> {

		private static final long serialVersionUID = 1L;

		public GradebookItemView(final String id, final List<Assignment> assignments) {
			super(id, assignments);
		}

		@Override
		protected void populateItem(final ListItem<Assignment> item) {

			final Assignment entry = item.getModelObject();

			item.add(new Label("itemTitle", entry.getName()));
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

	class ReleaseCheckbox extends AjaxCheckBox {

		private static final long serialVersionUID = 1L;

		public ReleaseCheckbox(final String id, final IModel<Boolean> model) {
			super(id, model);
			setOutputMarkupId(true);
		}

		@Override
		protected void onUpdate(final AjaxRequestTarget target) {
			if (!getModelObject()) {
				BulkEditItemsPanel.this.include.setModelObject(false);
				target.add(BulkEditItemsPanel.this.include);
			}
		}

	}

	class IncludeCheckbox extends AjaxCheckBox {

		private static final long serialVersionUID = 1L;

		public IncludeCheckbox(final String id, final IModel<Boolean> model) {
			super(id, model);
			setOutputMarkupId(true);
		}

		@Override
		protected void onUpdate(final AjaxRequestTarget target) {
			if (!getModelObject()) {
				BulkEditItemsPanel.this.release.setModelObject(true);
			}
			target.add(BulkEditItemsPanel.this.release);
		}

	}

}
