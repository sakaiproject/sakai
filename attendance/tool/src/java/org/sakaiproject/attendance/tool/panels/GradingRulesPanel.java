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
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.GradingRule;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.util.AttendanceConstants;

import java.util.*;

/**
 * @author David P. Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class GradingRulesPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    private IModel<Integer> selectedGradingMethodModel = new Model<>(AttendanceConstants.GRADING_METHOD_NONE); // Initialize the model with the default grading method;
    private GradingRulesListPanel gradingRulesListPanel;

    public GradingRulesPanel(String id, IModel<Integer> selectedGradingMethodModel, IModel<AttendanceSite> siteModel) {
        super(id);
        this.selectedGradingMethodModel = selectedGradingMethodModel;
        setOutputMarkupPlaceholderTag(true);

        // Add a behavior to control visibility based on selectedGradingMethodModel
        add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);
                Integer selectedMethod = selectedGradingMethodModel.getObject();
                component.setVisible(selectedMethod != null && selectedMethod != AttendanceConstants.GRADING_METHOD_NONE);
            }
        });

        FeedbackPanel rulesFeedbackPanel = new FeedbackPanel("rules-feedback") {
            @Override
            protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
                final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

                if (message.getLevel() == FeedbackMessage.ERROR ||
                        message.getLevel() == FeedbackMessage.DEBUG ||
                        message.getLevel() == FeedbackMessage.FATAL ||
                        message.getLevel() == FeedbackMessage.WARNING) {
                    add(AttributeModifier.replace("class", "alertMessage"));
                } else if (message.getLevel() == FeedbackMessage.INFO) {
                    add(AttributeModifier.replace("class", "messageSuccess"));
                }

                return newMessageDisplayComponent;
            }
        };

        enable(rulesFeedbackPanel);
        rulesFeedbackPanel.setOutputMarkupId(true);
        add(rulesFeedbackPanel);

        // Backing object
        final GradingRule gradingRule = new GradingRule(attendanceLogic.getCurrentAttendanceSite());

        // Form model
        final Model<GradingRule> formModel = new Model<>(gradingRule);

        // Form
        final Form<GradingRule> form = new Form<>("grading-rule-add-form", formModel);

        final AjaxButton addRuleButton = new AjaxButton("add-rule-submit", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target) {

                final GradingRule gradingRule = (GradingRule) getForm().getModelObject();

                // We are sharing the rules table so just fake the start and end range for multiply
                if (selectedGradingMethodModel != null && selectedGradingMethodModel.getObject().equals(AttendanceConstants.GRADING_METHOD_MULTIPLY)) {
                    gradingRule.setStartRange(1);
                    gradingRule.setEndRange(999);
                }

                if (gradingRule.getStartRange() < 0) {
                    rulesFeedbackPanel.error(getString("attendance.grading.start.range.error"));
                } else if (gradingRule.getEndRange() != null && gradingRule.getEndRange() < 0) {
                    rulesFeedbackPanel.error(getString("attendance.grading.end.range.error"));
                } else if (gradingRule.getEndRange() != null && gradingRule.getEndRange() < gradingRule.getStartRange()) {
                    rulesFeedbackPanel.error(getString("attendance.grading.end.start.error"));
                } else {
                    // Check for duplicate rules
                    boolean isDuplicate = false;
                    List<GradingRule> existingRules = attendanceLogic.getGradingRulesForSite(attendanceLogic.getCurrentAttendanceSite());
                    for (GradingRule existingRule : existingRules) {
                        if (existingRule.getStatus().equals(gradingRule.getStatus()) &&
                                existingRule.getStartRange().equals(gradingRule.getStartRange()) &&
                                Objects.equals(existingRule.getEndRange(), gradingRule.getEndRange())) {
                            isDuplicate = true;
                            break; // No need to continue checking once a duplicate is found
                        }
                    }

                    if (isDuplicate) {
                        rulesFeedbackPanel.error(getString("attendance.grading.rule.duplicate"));
                    } else if (attendanceLogic.addGradingRule(gradingRule)) {
                        rulesFeedbackPanel.info(getString("attendance.grading.add.rule.success"));
                        target.add(form);
                        gradingRulesListPanel.setNeedRegrade(true);
                        target.add(gradingRulesListPanel);
                    } else {
                        rulesFeedbackPanel.error(getString("attendance.grading.add.rule.error"));
                    }
                }
                target.add(rulesFeedbackPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(rulesFeedbackPanel);
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
        form.add(status);

        // Container for the start-range field
        final WebMarkupContainer startRangeContainer = new WebMarkupContainer("start-range-container");
        startRangeContainer.setOutputMarkupId(true);
        startRangeContainer.setOutputMarkupPlaceholderTag(true);
        form.add(startRangeContainer);

        // FROM
        final TextField<Integer> startRange = new TextField<>("start-range", new PropertyModel<Integer>(formModel, "startRange"));
        startRangeContainer.add(startRange);

        startRangeContainer.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);

                Integer selectedMethod = selectedGradingMethodModel.getObject();
                startRangeContainer.setVisible(selectedMethod == null || !selectedMethod.equals(AttendanceConstants.GRADING_METHOD_MULTIPLY));
            }
        });

        // Container for the end-range field
        final WebMarkupContainer endRangeContainer = new WebMarkupContainer("end-range-container");
        endRangeContainer.setOutputMarkupId(true);
        endRangeContainer.setOutputMarkupPlaceholderTag(true);
        form.add(endRangeContainer);

        // TO
        final TextField<Integer> endRange = new TextField<>("end-range", new PropertyModel<Integer>(formModel, "endRange"));
        endRangeContainer.add(endRange);

        // Add behavior to show/hide endRangeContainer based on selectedGradingMethodModel
        endRangeContainer.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);

                Integer selectedMethod = selectedGradingMethodModel.getObject();
                endRangeContainer.setVisible(selectedMethod == null || !selectedMethod.equals(AttendanceConstants.GRADING_METHOD_MULTIPLY));
            }
        });

        final TextField<Double> points = new TextField<>("points", new PropertyModel<Double>(formModel, "points"));
        form.add(points);

        add(form);

        gradingRulesListPanel = new GradingRulesListPanel("rules-list", siteModel, rulesFeedbackPanel, false, selectedGradingMethodModel);
        gradingRulesListPanel.setOutputMarkupId(true);

        add(gradingRulesListPanel);
    }
}