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

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.tool.panels.util.GradebookItemNameValidator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AttendanceGradeFormPanel is a Panel is used to get AttendanceSite settings for Grades.
 * Such as to send it to the Gradebook, show grades to students, and the maximum points allowed.
 * A non-null value for Maximum points turns on grading.
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class AttendanceGradeFormPanel extends BasePanel {
    private static final    long            serialVersionUID = 1L;

    private                 boolean         previousSendToGradebook;
    private                 String          previousName;
    private                 Double          previousMaxGrade;
    private        final    IModel<Boolean> useAutoGrading              = new Model<>();
    private        final    IModel<Boolean> autoGradeBySubtraction      = new Model<>();
    private                 GradingRulesPanel gradingRulesPanel;
    private                 WebMarkupContainer autoGradingTypeContainer;
    private                 String          previousCategory;
    private                 WebMarkupContainer  gradebookCategories;

    public AttendanceGradeFormPanel(String id, FeedbackPanel pg) {
        super(id);
        enable(pg);

        init();
    }

    private void init() {
        add(createSettingsForm());

        // Grade rules container
        this.gradingRulesPanel = new GradingRulesPanel("grading-rules") {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                Boolean autoGrading = useAutoGrading.getObject();
                return autoGrading != null && autoGrading;
            }
        };
        this.gradingRulesPanel.setOutputMarkupPlaceholderTag(true);
        add(this.gradingRulesPanel);
    }

    private Form<AttendanceSite> createSettingsForm() {
        final AttendanceSite aS = attendanceLogic.getCurrentAttendanceSite();
        this.previousSendToGradebook = aS.getSendToGradebook();
        this.previousName = aS.getGradebookItemName();
        this.previousMaxGrade = aS.getMaximumGrade();
        this.useAutoGrading.setObject(aS.getUseAutoGrading());
        this.autoGradeBySubtraction.setObject(aS.getAutoGradeBySubtraction());
        this.previousCategory = null;
        if(attendanceGradebookProvider.doesGradebookHaveCategories(aS.getSiteID()) && attendanceGradebookProvider.getCategoryForItem(aS.getSiteID(), aS.getId())!=null){
            this.previousCategory = String.valueOf(attendanceGradebookProvider.getCategoryForItem(aS.getSiteID(), aS.getId()));
        }
        Form<AttendanceSite> aSForm = new Form<AttendanceSite>("settings", new CompoundPropertyModel<>(aS)) {
            @Override
            public void onSubmit() {
                AttendanceSite aS = (AttendanceSite) getDefaultModelObject();
                CategoryParts catNow = null;
                String categoryId = null;
                if(attendanceGradebookProvider.doesGradebookHaveCategories(aS.getSiteID())){
                    catNow = (CategoryParts) gradebookCategories.getDefaultModelObject();
                    categoryId = catNow.getCategoryId();
                }
                aS.setUseAutoGrading(useAutoGrading.getObject());
                aS.setAutoGradeBySubtraction(autoGradeBySubtraction.getObject());

                if(aS.getMaximumGrade() == null && previousMaxGrade != null) {
                    aS.setSendToGradebook(false);
                    aS.setUseAutoGrading(false);
                }

                boolean result = attendanceLogic.updateAttendanceSite(aS);

                if (result) {
                    if(aS.getSendToGradebook()){
                        if(previousSendToGradebook) { // if previously true, see if any relevant values have changed
                            if(!previousName.equals(aS.getGradebookItemName()) || !previousMaxGrade.equals(aS.getMaximumGrade()) || (categoryId!=null && !previousCategory.equals(categoryId))){
                                attendanceGradebookProvider.update(aS, categoryId);
                            }
                            if(attendanceGradebookProvider.doesGradebookHaveCategories(aS.getSiteID()) && attendanceGradebookProvider.getCategoryForItem(aS.getSiteID(), aS.getId())!=null) {
                                previousCategory = String.valueOf(attendanceGradebookProvider.getCategoryForItem(aS.getSiteID(), aS.getId()));
                            }
                            previousName = aS.getGradebookItemName();
                        } else {
                            attendanceGradebookProvider.create(aS, categoryId);
                        }
                    } else {
                        if(previousSendToGradebook) {
                            attendanceGradebookProvider.remove(aS);
                        }
                    }

                    previousMaxGrade = aS.getMaximumGrade();
                    previousSendToGradebook = aS.getSendToGradebook();

                    // Successful Save - Regrade All if Auto Grade is set to true and maximum points is set
                    if (aS.getUseAutoGrading() != null && aS.getUseAutoGrading() && aS.getMaximumGrade() > 0) {
                        attendanceLogic.regradeAll(aS);
                    }

                    getSession().info(getString("attendance.settings.grading.success"));
                } else {
                    getSession().error(getString("attendance.settings.grading.failure"));
                }

            }
        };

        final WebMarkupContainer grading = new WebMarkupContainer("grading") {
            @Override
            public boolean isVisible() {
                return !(aS.getMaximumGrade() == null);
            }
        };
        grading.setOutputMarkupPlaceholderTag(true);

        Label maxGradeLabel = new Label("maximum-grade-label", new ResourceModel("attendance.settings.grading.max.points.possible"));
        NumberTextField<Double> maximum = new NumberTextField<Double>("maximumGrade");
        maximum.setMinimum(0.1);
        maximum.setStep(0.1);
        maximum.add(new AjaxFormComponentUpdatingBehavior("oninput") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(grading);
            }
        });
        aSForm.add(maximum);
        aSForm.add(maxGradeLabel);



        Label isGradeShownLabel = new Label("is-grade-shown-label", new ResourceModel("attendance.settings.grading.is.grade.shown"));
        CheckBox isGradeShown = new CheckBox("isGradeShown");
        grading.add(isGradeShown);
        grading.add(isGradeShownLabel);

        final WebMarkupContainer gradebook = new WebMarkupContainer("gradebook") {
            @Override
            public boolean isVisible() {
                return aS.getSendToGradebook();
            }
        };
        gradebook.setOutputMarkupPlaceholderTag(true);
        Label gbItemName = new Label("gradebook-item-name", new ResourceModel("attendance.settings.grading.gradebook.item.name"));
        TextField<String> gradebookItemName = new TextField<String>("gradebookItemName");
        gradebookItemName.add(new GradebookItemNameValidator(aS, aS.getGradebookItemName()));
        gradebookItemName.setRequired(true);
        gradebook.add(gbItemName);
        gradebook.add(gradebookItemName);
        grading.add(gradebook);

        final AjaxCheckBox sendToGradebook = new AjaxCheckBox("sendToGradebook") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(gradebook);
            }

            @Override
            public boolean isVisible() {
                return true;

            }
        };
        Label sendToGBLabel = new Label("send-to-gradebook", new ResourceModel("attendance.settings.grading.send.to.gradebook")) {
            @Override
            public boolean isVisible() {
                return true;
            }
        };
        grading.add(sendToGradebook);
        grading.add(sendToGBLabel);
        final boolean gradebookHasCategories = attendanceGradebookProvider.doesGradebookHaveCategories(aS.getSiteID());
        Map<String,String> categories = attendanceGradebookProvider.getGradebookCategories(aS.getSiteID());
        final List<CategoryParts> categoryParts = categoryListLoader(categories);
        if(gradebookHasCategories){
            gradebookCategories = new DropDownChoice<CategoryParts>("gradebookCategory",new Model<CategoryParts>(new CategoryParts(this.previousCategory,categories.get(this.previousCategory))),categoryParts,new ChoiceRenderer<CategoryParts>(){
                private static final long serialVersionUID = 1L;
                @Override
                public Object getDisplayValue(final CategoryParts a) {
                    return a.getCategoryName();
                }
                @Override
                public String getIdValue(final CategoryParts a,final int index) {
                    return a.getCategoryId();
                }
            });
        } else {
            gradebookCategories = new WebMarkupContainer("gradebookCategory");
        }
        gradebookCategories.setVisible(gradebookHasCategories);
        Label categoriesLabel = new Label("gradebookCategoryLabel", new ResourceModel("attendance.settings.grading.gradebook.category")){
            @Override
            public boolean isVisible(){
                return gradebookHasCategories;
            }
        };
        gradebook.add(gradebookCategories);
        gradebook.add(categoriesLabel);
        final RadioGroup<Boolean> useAutoGradingGroup = new RadioGroup<>("use-auto-grading-group", this.useAutoGrading);
        useAutoGradingGroup.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(gradingRulesPanel);
                target.add(autoGradingTypeContainer);
            }
        });
        useAutoGradingGroup.setRenderBodyOnly(false);
        grading.add(useAutoGradingGroup);

        Radio<Boolean> manualGrading = new Radio<>("manual-grading", Model.of(Boolean.FALSE));
        Radio<Boolean> autoGrading = new Radio<>("auto-grading", Model.of(Boolean.TRUE));

        useAutoGradingGroup.add(manualGrading);
        useAutoGradingGroup.add(autoGrading);

        this.autoGradingTypeContainer = new WebMarkupContainer("auto-grading-type") {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                Boolean autoGrading = useAutoGrading.getObject();
                return autoGrading != null && autoGrading;
            }
        };
        this.autoGradingTypeContainer.setOutputMarkupPlaceholderTag(true);
        grading.add(this.autoGradingTypeContainer);

        final RadioGroup<Boolean> autoGradeType = new RadioGroup<>("auto-grading-type-group", this.autoGradeBySubtraction);
        autoGradeType.setRenderBodyOnly(false);
        this.autoGradingTypeContainer.add(autoGradeType);

        Radio<Boolean> subtractGrading = new Radio<>("subtract-grading", Model.of(Boolean.TRUE));
        Radio<Boolean> addGrading = new Radio<>("add-grading", Model.of(Boolean.FALSE));

        autoGradeType.add(subtractGrading);
        autoGradeType.add(addGrading);

        aSForm.add(grading);

        AjaxSubmitLink submit = new AjaxSubmitLink("submit") {
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(pageFeedbackPanel);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                target.add(pageFeedbackPanel);
            }
        };
        submit.add(new AttributeModifier("value", new ResourceModel("attendance.settings.grading.save")));
        aSForm.add(submit);

        return aSForm;
    }
    private List<CategoryParts> categoryListLoader(Map<String,String> categoryMap){
        List<CategoryParts> parts = new ArrayList<>();
        if(categoryMap == null){
            return parts;
        }
        for(String idNow: categoryMap.keySet()){
            CategoryParts categoryNow = new CategoryParts(idNow,categoryMap.get(idNow));
            parts.add(categoryNow);
        }
        return parts;
    }

    private class CategoryParts implements Serializable {
        @Getter @Setter private String categoryId;
        @Getter @Setter private String categoryName;
        public CategoryParts(String id, String name){
            this.categoryName = name;
            this.categoryId = id;
        }
    }
}
