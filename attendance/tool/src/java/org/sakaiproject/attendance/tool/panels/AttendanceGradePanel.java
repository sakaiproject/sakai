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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.*;
import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.AttendanceSite;

/**
 * AttendanceGradePanel allows for inputting of AttendanceGrades
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class AttendanceGradePanel extends BasePanel {
    private static final    long                    serialVersionUID = 1L;
    private                 IModel<AttendanceGrade> agIModel;
    private                 AttendanceSite          attendanceSite;

    public AttendanceGradePanel(String id, AttendanceGrade aG, FeedbackPanel fP) {
        super(id);

        if(aG == null) {
            this.agIModel = new CompoundPropertyModel<>(new AttendanceGrade());
            this.attendanceSite = attendanceLogic.getCurrentAttendanceSite();
        } else {
            this.agIModel = new CompoundPropertyModel<>(aG);
            this.attendanceSite = agIModel.getObject().getAttendanceSite();
        }
        enable(fP);

        init();
    }

    private void init() {
        add(createGradeForm());
    }

    private Form<AttendanceGrade> createGradeForm() {
        Form<AttendanceGrade> gForm = new Form<AttendanceGrade>("attendance-grade", this.agIModel) {
            @Override
            public void onSubmit() {
                AttendanceGrade aG = (AttendanceGrade) getDefaultModelObject();

                boolean result;

                if (Boolean.TRUE.equals(attendanceSite.getUseAutoGrading()) && Boolean.FALSE.equals(aG.getOverride())) {
                    result = attendanceLogic.regrade(aG, true) != null;
                } else {
                    result = attendanceLogic.updateAttendanceGrade(aG);
                }

                String displayName = sakaiProxy.getUserSortName(aG.getUserID());

                if (result) {
                    String grade = aG.getGrade() == null ? "null" : aG.getGrade().toString();
                    getSession().info(new StringResourceModel("attendance.grade.update.success", null, new String[]{grade, displayName}).getString());
                } else {
                    getSession().error(new StringResourceModel("attendance.grade.update.failure", null, new String[]{displayName}).getString());
                }

            }
        };

        final Double maximumGrade = this.attendanceSite.getMaximumGrade();

        NumberTextField<Double> points = new NumberTextField<Double>("grade") {
            @Override
            public boolean isEnabled(){
                return maximumGrade != null && (Boolean.FALSE.equals(attendanceSite.getUseAutoGrading()) || Boolean.TRUE.equals(agIModel.getObject().getOverride()));
            }
        };
        points.setMinimum(0.0);
        points.setStep(0.1);

        points.add(new AjaxFormSubmitBehavior(gForm, "input") {
            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(pageFeedbackPanel);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if(target != null) {
                    target.add(pageFeedbackPanel);
                }
            }
        });

        Label maximum;

        if(maximumGrade == null) {
            maximum = new Label("maximum", "/ -");
            points.add(new AttributeModifier("title", new StringResourceModel("attendance.grade.tooltip.disabled", null, new String[]{new ResourceModel("settings.link.label").getObject()})));
        } else {
            maximum = new Label("maximum", "/ "+ maximumGrade.toString());
        }
        maximum.setVisible(!this.isEnabled());

        final CheckBox override = new CheckBox("override", new PropertyModel<>(gForm.getModelObject(), "override"));
        override.setVisible(this.attendanceSite.getUseAutoGrading() && this.isEnabled());
        override.add(new AjaxFormSubmitBehavior(gForm, "change") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if(target != null) {
                    target.add(pageFeedbackPanel);
                    target.add(points);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(pageFeedbackPanel);
            }
        });
        gForm.add(override);

        final FormComponentLabel overrideLabel = new FormComponentLabel("overrideLabel", override);
        overrideLabel.setVisible(this.attendanceSite.getUseAutoGrading() && this.isEnabled());
        gForm.add(overrideLabel);

        gForm.add(maximum);
        gForm.add(points);

        return gForm;
    }
}
