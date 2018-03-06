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
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.GbGradeLog;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;

/**
 * Panel for the grade log window
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class GradeLogPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	public GradeLogPanel(final String id, final IModel<Map<String, Object>> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		final Long assignmentId = (Long) modelData.get("assignmentId");
		final String studentUuid = (String) modelData.get("studentUuid");

		// get the data
		final List<GbGradeLog> gradeLog = this.businessService.getGradeLog(studentUuid, assignmentId);

		// render list
		final ListView<GbGradeLog> listView = new ListView<GbGradeLog>("log", gradeLog) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<GbGradeLog> item) {

				final GbGradeLog gradeLog = item.getModelObject();

				final String logDate = FormatHelper.formatDateTime(gradeLog.getDateGraded());
				final String grade = gradeLog.getGrade();

				final GbUser grader = GradeLogPanel.this.businessService.getUser(gradeLog.getGraderUuid());
				final String graderDisplayId = (grader != null) ? grader.getDisplayId() : getString("unknown.user.id");

				// add the entry
				item.add(new Label("entry",
						new StringResourceModel("grade.log.entry", null, new Object[] { logDate, grade, graderDisplayId }))
								.setEscapeModelStrings(false));

			}
		};
		add(listView);

		// no entries
		final Label emptyLabel = new Label("empty", new ResourceModel("grade.log.none"));
		emptyLabel.setVisible(gradeLog.isEmpty());
		add(emptyLabel);

		// done button
		add(new GbAjaxLink("done") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				GradeLogPanel.this.window.close(target);
			}
		});

		// heading
		// TODO if user has been deleted since rendering the GradebookPage, handle a null here gracefully
		final GbUser user = this.businessService.getUser(studentUuid);
		GradeLogPanel.this.window.setTitle(
				(new StringResourceModel("heading.gradelog", null,
						new Object[] { user.getDisplayName(), user.getDisplayId() })).getString());

	}

}
