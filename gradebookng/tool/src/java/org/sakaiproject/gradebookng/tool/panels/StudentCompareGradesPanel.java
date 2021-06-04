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

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradingType;
import org.sakaiproject.user.api.User;

public class StudentCompareGradesPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    private final ModalWindow window;

    public StudentCompareGradesPanel(final String id, final IModel<Assignment> model, final ModalWindow window) {
        super(id, model);
        this.window = window;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
            final Assignment assignment = ((Model<Assignment>) getDefaultModel()).getObject();

            final GradingType gradingType = GradingType.valueOf(getGradebook().getGrade_type());

            User currentUser = this.businessService.getCurrentUser();
            final List<GbStudentGradeInfo> gradeInfo = this.businessService
                    .buildMatrixForGradeComparison(assignment);

            StudentCompareGradesPanel.this.window.setTitle(
                    new StringResourceModel("comparegrades.modal.title.student.name", null, new Object[] { currentUser.getDisplayName() })
            );

            Label gradeItemLabel = new Label("gradeItemLabel", assignment.getName());
            add(gradeItemLabel);

            if(getSettings().isComparingRandomizeDisplayedData()){
                Collections.shuffle(gradeInfo);
            }
            boolean isComparingAndDisplayingFullName = getSettings()
                                    .isComparingDisplayStudentNames() && 
                           getSettings()
                                   .isComparingDisplayStudentSurnames();

            boolean isComparingOrDisplayingFullName = getSettings()
                                    .isComparingDisplayStudentNames() || 
                           getSettings()
                                   .isComparingDisplayStudentSurnames();

            // Table headers

            Label studentNameHeaderLabel = new Label("studentNameHeaderLabel", new ResourceModel("comparegrades.modal.table.header.student.name")){
                @Override
                public boolean isVisible() {
                    return isComparingOrDisplayingFullName;
                }
            };
            add(studentNameHeaderLabel);

            Label teacherCommentHeaderLabel = new Label("teacherCommentHeaderLabel" ,new ResourceModel("comparegrades.modal.table.header.teacher.comment")){
                @Override
                public boolean isVisible() {
                    return getSettings().isComparingDisplayTeacherComments();
                }
            };
            add(teacherCommentHeaderLabel);

            Label gradeHeaderLabel = new Label("gradeHeaderLabel" ,new ResourceModel("comparegrades.modal.table.header.grade")){
                @Override
                public boolean isVisible() {
                    return getSettings().isComparingDisplayGrades();
                }

            };
            add(gradeHeaderLabel);

            // Table rows

            ListView<GbStudentGradeInfo> gradeItemsTable = new ListView<GbStudentGradeInfo>("gradeItemRows", Model.ofList(gradeInfo)){
            @Override
            protected void populateItem(ListItem<GbStudentGradeInfo> li) {
                GbStudentGradeInfo auxItem = li.getModelObject();
                GbGradeInfo gradeInfo = auxItem.getGrades()
                        .values()
                        .stream()
                        .findFirst()
                        .orElseGet(() -> {
                            GradeDefinition auxGd = new GradeDefinition();
                            auxGd.setGrade("");
                            return new GbGradeInfo(auxGd);
                        });

                String studentDisplayName = String.format(
                        "%s%s%s", 
                        getSettings().isComparingDisplayStudentNames() ? auxItem.getStudentFirstName() : "",
                        isComparingAndDisplayingFullName ? " " : "",
                        getSettings().isComparingDisplayStudentSurnames()? auxItem.getStudentLastName() : ""
                );
                
                Label studentNameLabel = new Label("studentNameLabel", studentDisplayName){
                    @Override
                    public boolean isVisible() {
                        return isComparingOrDisplayingFullName;
                    }
                };
                li.add(studentNameLabel);
                
                Label teacherCommentLabel = new Label("teacherCommentLabel", gradeInfo.getGradeComment()){
                    @Override
                    public boolean isVisible() {
                        return getSettings().isComparingDisplayTeacherComments();
                    }
                };
                li.add(teacherCommentLabel);
                Label gradeLabel = new Label("gradeLabel", FormatHelper.formatGrade(gradeInfo.getGrade()) + (
                            GradingType.PERCENTAGE.equals(gradingType) ? "%" : ""
                        )
                ){
                    @Override
                    public boolean isVisible() {
                        return getSettings().isComparingDisplayGrades();
                    }
                };
                li.add(gradeLabel);
            }};

            add(gradeItemsTable);

    }

}
