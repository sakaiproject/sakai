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
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.BasePage;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

/**
 *
 * Wrapper for the student grade summary tabs
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentGradeSummaryPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	public StudentGradeSummaryPanel(final String id, final IModel<Map<String, Object>> model, final GbModalWindow window) {
		super(id, model);

		this.window = window;
		setOutputMarkupId(true);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// done button
		add(new GbAjaxLink("done") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				StudentGradeSummaryPanel.this.window.close(target);
			}
		});

		final WebMarkupContainer studentNavigation = new WebMarkupContainer("studentNavigation");
		studentNavigation.setOutputMarkupPlaceholderTag(true);
		add(studentNavigation);

		final List<ITab> tabs = new ArrayList<>();

		tabs.add(new AbstractTab(new Model<String>(getString("label.studentsummary.instructorviewtab"))) {
			private static final long serialVersionUID = 1L;

			@Override
			public Panel getPanel(final String panelId) {
				return new InstructorGradeSummaryGradesPanel(panelId, (IModel<Map<String, Object>>) getDefaultModel());
			}
		});

		// Disable Student View for TAs as they most likely won't have the access
		// to view the grade data for every student
		if (((BasePage) getPage()).getCurrentRole() == GbRole.INSTRUCTOR) {
			tabs.add(new AbstractTab(new Model<String>(getString("label.studentsummary.studentviewtab"))) {
				private static final long serialVersionUID = 1L;

				@Override
				public Panel getPanel(final String panelId) {
					return new StudentGradeSummaryGradesPanel(panelId, (IModel<Map<String, Object>>) getDefaultModel());
				}
			});
		}

		add(new AjaxBootstrapTabbedPanel("tabs", tabs) {
			@Override
			protected String getTabContainerCssClass() {
				return "nav nav-tabs";
			}

			@Override
			protected void onAjaxUpdate(final AjaxRequestTarget target) {
				super.onAjaxUpdate(target);

				final boolean showingInstructorView = (getSelectedTab() == 0);
				final boolean showingStudentView = (getSelectedTab() == 1);

				studentNavigation.setVisible(showingInstructorView);
				target.add(studentNavigation);

				target.appendJavaScript(
						String.format("new GradebookGradeSummary($(\"#%s\"), %s);",
								getParent().getMarkupId(),
								showingStudentView));
			}
		});
	}

}
