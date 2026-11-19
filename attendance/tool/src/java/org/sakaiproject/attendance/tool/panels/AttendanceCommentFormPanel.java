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

package org.sakaiproject.attendance.tool.panels;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.*;
import org.sakaiproject.attendance.model.AttendanceSite;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Slf4j
public class AttendanceCommentFormPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    private Model<AttendanceSite> attendanceSiteModel;

    public AttendanceCommentFormPanel(String id, FeedbackPanel feedbackPanel) {
        super(id);

        this.attendanceSiteModel = new Model<>(attendanceLogic.getCurrentAttendanceSite());
        init(feedbackPanel);
    }

    public AttendanceCommentFormPanel(String id, FeedbackPanel feedbackPanel, Model<AttendanceSite> siteModel) {
        super(id, siteModel);

        this.attendanceSiteModel = siteModel;
        init(feedbackPanel);
    }

    private void init(FeedbackPanel panel) {
        enable(panel);

        Form<AttendanceSite> editCommentSettingsForm = new Form<AttendanceSite>("edit-comment-settings-form", new CompoundPropertyModel<>(this.attendanceSiteModel)) {
            @Override
            protected void onSubmit() {
                final AttendanceSite aS = (AttendanceSite) getDefaultModelObject();
                boolean result = attendanceLogic.updateAttendanceSite(aS);

                if(result) {
                    getSession().success(getString("attendance.settings.edit.comment.save.success"));
                } else {
                    getSession().error(getString("attendance.settings.edit.comment.save.error"));
                }

            }
        };

        editCommentSettingsForm.add(new CheckBox("show-comments-to-students", new PropertyModel<>(this.attendanceSiteModel, "showCommentsToStudents")));
        add(editCommentSettingsForm);
    }
}
