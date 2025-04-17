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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.Status;

import java.util.*;

/**
 * AttendanceStatusFormPanel a panel which allows for setting various AttendanceStatus settings (which ones are active
 * and their display order).
 *
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class AttendanceStatusFormPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private Model<AttendanceSite> attendanceSiteModel;
    private boolean subForm = true;

    public AttendanceStatusFormPanel(String id, FeedbackPanel feedbackPanel) {
        super(id);

        this.attendanceSiteModel = new Model<>(attendanceLogic.getCurrentAttendanceSite());
        init(feedbackPanel);
    }

    public AttendanceStatusFormPanel(String id, FeedbackPanel feedbackPanel, Model<AttendanceSite> attendanceSiteModel) {
        super(id, attendanceSiteModel);
        
        this.attendanceSiteModel = attendanceSiteModel;
        init(feedbackPanel);
    }

    public void setSubForm(boolean val) {
        this.subForm = val;
    }

    private void init(FeedbackPanel panel) {
        enable(panel);

        Form<AttendanceSite> editStatusSettingsForm = new Form<AttendanceSite>("edit-status-settings-form", new CompoundPropertyModel<>(this.attendanceSiteModel)) {
            @Override
            protected void onSubmit() {
                AttendanceSite aS = (AttendanceSite) getDefaultModelObject();
                boolean result = attendanceLogic.updateAttendanceSite(aS);
                if(result){
                    getSession().info(getString("attendance.settings.edit.status.save.success"));
                } else {
                    getSession().error(getString("attendance.settings.edit.status.save.error"));
                }
            }
        };
        add(editStatusSettingsForm);

        final IModel<List<AttendanceStatus>> listModel = new PropertyModel<List<AttendanceStatus>>(this.attendanceSiteModel, "attendanceStatuses") {
            @Override
            public List<AttendanceStatus> getObject() {
                List<AttendanceStatus> attendanceStatuses = new ArrayList((Set)super.getObject());
                Collections.sort(attendanceStatuses, new Comparator<AttendanceStatus>() {
                    @Override
                    public int compare(AttendanceStatus o1, AttendanceStatus o2) {
                        return o1.getSortOrder() - o2.getSortOrder();
                    }
                });
                return attendanceStatuses;
            }
        };
        editStatusSettingsForm.add(new ListView<AttendanceStatus>("all-statuses", listModel) {
            @Override
            protected void populateItem(ListItem<AttendanceStatus> item) {
                String statusName = getStatusString(item.getModelObject().getStatus());
                final CheckBox isActive = new CheckBox("is-active", new PropertyModel<Boolean>(item.getModelObject(), "isActive"));
                item.add(isActive);
                item.add(new Label("status", statusName));
                item.add(new TextField<Integer>("sort-order", new PropertyModel<Integer>(item.getModelObject(), "sortOrder")));
                if(item.getModelObject().getStatus() == Status.UNKNOWN) {
                    item.setVisible(false);
                }
            }
        });

        AjaxSubmitLink submit = new AjaxSubmitLink("submit-link") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                target.add(pageFeedbackPanel);
            }

            @Override
            public boolean isVisible() {
                return !subForm;
            }
        };

        editStatusSettingsForm.add(submit);
    }
}
