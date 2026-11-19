package org.sakaiproject.attendance.tool.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.GradingRule;
import org.sakaiproject.attendance.util.AttendanceConstants;

import lombok.Setter;

import java.text.MessageFormat;
import java.util.List;

/**
 * @author David P. Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class GradingRulesListPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private final IModel<Integer> selectedGradingMethodModel;

    Form<Void> regradeForm;
    @Setter boolean needRegrade;

    public GradingRulesListPanel(String id, IModel<AttendanceSite> model, FeedbackPanel feedbackPanel, boolean needRegrade, IModel<Integer> selectedGradingMethodModel) {
        super(id, model);

        this.pageFeedbackPanel = feedbackPanel;
        this.needRegrade = needRegrade;
        this.selectedGradingMethodModel = selectedGradingMethodModel;

        final ListDataProvider<GradingRule> rulesProvider = new ListDataProvider<>() {
            @Override
            protected List<GradingRule> getData() {
                return attendanceLogic.getGradingRulesForSite(attendanceLogic.getCurrentAttendanceSite());
            }
        };

        final DataView<GradingRule> rules = new DataView<>("rules", rulesProvider) {
            @Override
            protected void populateItem(Item<GradingRule> item) {
                GradingRule gradingRule = item.getModelObject();
                Label rule = new Label("rule",
                        MessageFormat.format(getString("attendance.settings.grading.rule.sentence"),
                                String.valueOf(gradingRule.getPoints()),
                                getStatusString(gradingRule.getStatus()),
                                String.valueOf(gradingRule.getStartRange()),
                                String.valueOf(gradingRule.getEndRange())
                        ));
                // Different explanation for multiply rules
                if (selectedGradingMethodModel != null && selectedGradingMethodModel.getObject().equals(AttendanceConstants.GRADING_METHOD_MULTIPLY)) {
                    rule.setDefaultModelObject(MessageFormat.format(getString("attendance.settings.grading.rule.sentence.multiply"),
                            String.valueOf(gradingRule.getPoints()),
                            getStatusString(gradingRule.getStatus())
                    ));
                }
                rule.setEscapeModelStrings(false);
                item.add(rule);
                final Form<GradingRule> deleteForm = new Form<>("delete-form", item.getModel());
                item.add(deleteForm);

                deleteForm.add(new AjaxButton("delete-rule-button", deleteForm) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onSubmit(AjaxRequestTarget target) {
                        super.onSubmit(target);

                        final boolean result = attendanceLogic.deleteGradingRule((GradingRule) getForm().getModelObject());

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
                    protected void onError(AjaxRequestTarget target) {
                        target.add(GradingRulesListPanel.this.pageFeedbackPanel);
                    }
                });

            }
        };
        rules.setOutputMarkupId(true);

        add(rules);

        // Use a Form - the markup will be removed
        this.regradeForm = new Form<>("regrade-form1");
        this.regradeForm.setMarkupId("regrade-form");
        this.regradeForm.setOutputMarkupId(true);
        this.regradeForm.setOutputMarkupPlaceholderTag(true);

        this.regradeForm.add(new AjaxButton("regrade-submit1", regradeForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);

                setNeedRegrade(false);

                attendanceLogic.regradeAll(attendanceLogic.getCurrentAttendanceSite());

                GradingRulesListPanel.this.pageFeedbackPanel.info(getString("attendance.grading.regrade.success"));

                target.add(GradingRulesListPanel.this);
                target.add(GradingRulesListPanel.this.pageFeedbackPanel);
                target.add(GradingRulesListPanel.this.regradeForm);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(GradingRulesListPanel.this.pageFeedbackPanel);
            }
        });

        add(regradeForm);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        // Set the form's visibility based on needRegrade during each render cycle
        regradeForm.setVisible(needRegrade);
    }

}