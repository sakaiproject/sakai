/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.sakaiproject.attendance.tool.pages;


import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.tool.panels.AttendanceCommentFormPanel;
import org.sakaiproject.attendance.tool.panels.AttendanceStatusFormPanel;

/**
 * SettingsPage gets various settings which affect the whole AttendanceSite
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class SettingsPage extends BasePage {
	private static final long serialVersionUID = 1L;
	
	public SettingsPage() {
		disableLink(settingsLink);

		if(this.role != null && this.role.equals("Student")) {
			throw new RestartResponseException(StudentView.class);
		}

		Label headerSettings = new Label("header-settings",	new ResourceModel("attendance.settings.header"));
		add(headerSettings);

		Model<AttendanceSite> siteModel = new Model<>(attendanceLogic.getCurrentAttendanceSite());
		Form<AttendanceSite> settingsForm = new Form<>("settings-form", siteModel);


		settingsForm.add(createEditStatusesPanel(siteModel));
		settingsForm.add(createEditCommentPanel(siteModel));

		AjaxSubmitLink submit = new AjaxSubmitLink("submit-link") {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				target.add(feedbackPanel);
			}

		};
		settingsForm.add(submit);

		add(settingsForm);

	}

	private WebMarkupContainer createEditStatusesPanel(Model<AttendanceSite> siteModel) {

		WebMarkupContainer allStatusesContainer = new WebMarkupContainer("all-statuses-container");
		allStatusesContainer.add(new AttendanceStatusFormPanel("edit-status-panel", feedbackPanel, siteModel));
		return allStatusesContainer;
	}

	private WebMarkupContainer createEditCommentPanel(Model<AttendanceSite> siteModel) {
		WebMarkupContainer commentContainer = new WebMarkupContainer("comment-container");
		commentContainer.add(new AttendanceCommentFormPanel("edit-comment-panel", feedbackPanel, siteModel));
		return commentContainer;
	}
}
