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
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.sakaiproject.attendance.model.GradingRule;

import java.text.MessageFormat;
import java.util.List;

/**
 * @author David P. Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class GradingRulesListPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    Form regradeForm;

    private boolean needRegrade;

    public GradingRulesListPanel(String id, FeedbackPanel feedbackPanel, boolean needRegrade) {
        super(id);

        enable(feedbackPanel);

        this.needRegrade = needRegrade;

        final ListDataProvider<GradingRule> rulesProvider = new ListDataProvider<GradingRule>() {
            @Override
            protected List<GradingRule> getData() {
                return attendanceLogic.getGradingRulesForSite(attendanceLogic.getCurrentAttendanceSite());
            }
        };

        final DataView<GradingRule> rules = new DataView<GradingRule>("rules", rulesProvider) {
            @Override
            protected void populateItem(Item<GradingRule> item) {
                GradingRule gradingRule = item.getModelObject();
                final Label rule = new Label("rule",
                        MessageFormat.format(getString("attendance.settings.grading.rule.sentence"),
                                String.valueOf(gradingRule.getPoints()),
                                getStatusString(gradingRule.getStatus()),
                                String.valueOf(gradingRule.getStartRange()),
                                String.valueOf(gradingRule.getEndRange())
                        ));
                rule.setEscapeModelStrings(false);
                item.add(rule);
                final Form<GradingRule> deleteForm = new Form<>("delete-form", item.getModel());
                item.add(deleteForm);

                deleteForm.add(new AjaxButton("delete-rule-button", deleteForm) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        super.onSubmit(target, form);

                        final boolean result = attendanceLogic.deleteGradingRule((GradingRule) form.getModelObject());

                        if (result) {
                            setNeedRegrade(true);
                            target.add(GradingRulesListPanel.this.regradeForm);
                        } else {
                            GradingRulesListPanel.this.pageFeedbackPanel.error(getString("attendance.grading.delete.rule.error"));
                            target.add(GradingRulesListPanel.this.pageFeedbackPanel);
                        }
                        target.add(GradingRulesListPanel.this);
                    }

                    @Override
                    protected void onError(AjaxRequestTarget target, Form<?> form) {
                        target.add(GradingRulesListPanel.this.pageFeedbackPanel);
                    }
                });

            }
        };
        rules.setOutputMarkupId(true);

        add(rules);

        this.regradeForm = new Form("regrade-form1"){
            @Override
            public boolean isVisible() {
                return getNeedRegrade();
            }
        };
        regradeForm.add(new AjaxButton("regrade-submit1", regradeForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);

                attendanceLogic.regradeAll(attendanceLogic.getCurrentAttendanceSite());

                GradingRulesListPanel.this.pageFeedbackPanel.info(getString("attendance.grading.regrade.success"));

                target.add(GradingRulesListPanel.this);
                target.add(GradingRulesListPanel.this.pageFeedbackPanel);
                setNeedRegrade(false);
                target.add(GradingRulesListPanel.this.regradeForm);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(GradingRulesListPanel.this.pageFeedbackPanel);
            }
        });
        this.regradeForm.setOutputMarkupId(true);
        add(regradeForm);
    }

    void setNeedRegrade(boolean needRegrade) {
        this.needRegrade = needRegrade;
    }

    private boolean getNeedRegrade() {
        return this.needRegrade;
    }
}
