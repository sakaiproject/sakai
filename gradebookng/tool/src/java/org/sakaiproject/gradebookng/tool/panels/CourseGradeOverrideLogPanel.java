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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import org.sakaiproject.service.gradebook.shared.CourseGrade;

/**
 * Panel for the course grade override log window
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class CourseGradeOverrideLogPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	public CourseGradeOverrideLogPanel(final String id, final IModel<String> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final String studentUuid = (String) getDefaultModelObject();

		// heading
		// TODO if user has been deleted since rendering the GradebookPage, handle a null here gracefully
		final GbUser user = this.businessService.getUser(studentUuid);
		CourseGradeOverrideLogPanel.this.window.setTitle(
				(new StringResourceModel("heading.coursegradelog", null,
						new Object[] { user.getDisplayName(), user.getDisplayId() })).getString());

		// get the course grade
		final CourseGrade courseGrade = this.businessService.getCourseGrade(studentUuid);

		// get the events
		List<GbGradeLog> gradeLog;

		// if course grade is null we don't have any override events to show
		if (courseGrade.getId() == null) {
			gradeLog = Collections.emptyList();
		} else {
			gradeLog = this.businessService.getGradeLog(studentUuid, courseGrade.getId());
		}

		// render list
		final ListView<GbGradeLog> listView = new ListView<GbGradeLog>("log", gradeLog) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<GbGradeLog> item) {

				final GbGradeLog gradeLog = item.getModelObject();

				// add the entry
				item.add(new Label("entry", formatLogEntry(gradeLog)).setEscapeModelStrings(false));
			}
		};
		add(listView);

		// no entries
		final Label emptyLabel = new Label("empty", new ResourceModel("coursegrade.log.none"));
		emptyLabel.setVisible(gradeLog.isEmpty());
		add(emptyLabel);

		// done button
		add(new GbAjaxLink("done") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				CourseGradeOverrideLogPanel.this.window.close(target);
			}
		});
	}

	/**
	 * Helper to format a grade log entry
	 *
	 * @param gradeLog
	 * @return
	 */
	private String formatLogEntry(final GbGradeLog gradeLog) {

		final String logDate = FormatHelper.formatDateTime(gradeLog.getDateGraded());
		final String grade = gradeLog.getGrade();

		final GbUser grader = CourseGradeOverrideLogPanel.this.businessService.getUser(gradeLog.getGraderUuid());
		final String graderDisplayId = (grader != null) ? grader.getDisplayId() : getString("unknown.user.id");

		String rval;

		// if no grade, it is a reset
		if (StringUtils.isNotBlank(grade)) {
			rval = new StringResourceModel("coursegrade.log.entry.set", null, new Object[] { logDate, grade, graderDisplayId }).getString();
		} else {
			rval = new StringResourceModel("coursegrade.log.entry.unset", null, new Object[] { logDate, graderDisplayId }).getString();
		}

		return rval;

	}

}
