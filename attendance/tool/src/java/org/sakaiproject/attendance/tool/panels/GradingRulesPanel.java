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
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.GradingRule;
import org.sakaiproject.attendance.model.Status;

import java.util.*;

/**
 * @author David P. Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class GradingRulesPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    private GradingRulesListPanel gradingRulesListPanel;

    public GradingRulesPanel(String id) {
        super(id);

        enable(new FeedbackPanel("rules-feedback"){

            @Override
            protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
                final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

                if(message.getLevel() == FeedbackMessage.ERROR ||
                        message.getLevel() == FeedbackMessage.DEBUG ||
                        message.getLevel() == FeedbackMessage.FATAL ||
                        message.getLevel() == FeedbackMessage.WARNING){
                    add(AttributeModifier.replace("class", "alertMessage"));
                } else if(message.getLevel() == FeedbackMessage.INFO){
                    add(AttributeModifier.replace("class", "messageSuccess"));
                }

                return newMessageDisplayComponent;
            }
        });
        this.pageFeedbackPanel.setOutputMarkupId(true);
        add(this.pageFeedbackPanel);

        // Backing object
        final GradingRule gradingRule = new GradingRule(attendanceLogic.getCurrentAttendanceSite());

        // Form model
        final Model<GradingRule> formModel = new Model<>(gradingRule);

        // Form
        final Form<GradingRule> form = new Form<>("grading-rule-add-form", formModel);

        final AjaxButton addRuleButton = new AjaxButton("add-rule-submit", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

                final GradingRule gradingRule = (GradingRule) form.getModelObject();

                if (gradingRule.getStartRange() < 0) {
                    pageFeedbackPanel.error(getString("attendance.grading.start.range.error"));
                } else if (gradingRule.getEndRange() != null && gradingRule.getEndRange() < 0) {
                    pageFeedbackPanel.error(getString("attendance.grading.end.range.error"));
                } else if (gradingRule.getEndRange() != null && gradingRule.getEndRange() < gradingRule.getStartRange()) {
                    pageFeedbackPanel.error(getString("attendance.grading.end.start.error"));
                } else {
                    attendanceLogic.addGradingRule(gradingRule);
                    pageFeedbackPanel.info(getString("attendance.grading.add.rule.success"));
                    target.add(form);
                    gradingRulesListPanel.setNeedRegrade(true);
                    target.add(gradingRulesListPanel);
                }
                target.add(pageFeedbackPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(pageFeedbackPanel);
            }
        };
        form.add(addRuleButton);

        List<AttendanceStatus> activeAttendanceStatuses = attendanceLogic.getActiveStatusesForCurrentSite();
        activeAttendanceStatuses.sort(Comparator.comparingInt(AttendanceStatus::getSortOrder));
        List<Status> activeStatuses = new ArrayList<>();
        for(AttendanceStatus attendanceStatus : activeAttendanceStatuses) {
            if (attendanceStatus.getStatus() != Status.UNKNOWN) {
                activeStatuses.add(attendanceStatus.getStatus());
            }
        }

        final DropDownChoice<Status> status = new DropDownChoice<>("status", new PropertyModel<>(formModel, "status"), activeStatuses, new EnumChoiceRenderer<>(this));
        status.setRequired(true);
        form.add(status);

        final TextField<Integer> startRange = new TextField<>("start-range", new PropertyModel<Integer>(formModel, "startRange"));
        startRange.setRequired(true);
        form.add(startRange);

        final TextField<Integer> endRange = new TextField<>("end-range", new PropertyModel<Integer>(formModel, "endRange"));
        form.add(endRange);

        final TextField<Double> points = new TextField<>("points", new PropertyModel<Double>(formModel, "points"));
        points.setRequired(true);
        form.add(points);

        add(form);

        gradingRulesListPanel = new GradingRulesListPanel("rules-list", pageFeedbackPanel, false);
        gradingRulesListPanel.setOutputMarkupId(true);

        add(gradingRulesListPanel);


    }
}
