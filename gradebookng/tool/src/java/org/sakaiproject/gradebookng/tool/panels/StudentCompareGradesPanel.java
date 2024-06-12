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

import com.google.gson.Gson;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.GbGradeComparisonItem;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.user.api.User;

public class StudentCompareGradesPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    private final ModalWindow window;
    private final Assignment assignment;

    public StudentCompareGradesPanel(final String id, final IModel<Assignment> model, final ModalWindow window) {
        super(id, model);
        this.window = window;
        this.assignment = model.getObject();
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

            User currentUser = this.businessService.getCurrentUser();

            StudentCompareGradesPanel.this.window.setTitle(
                    new StringResourceModel("comparegrades.modal.title.student.name").setParameters(currentUser.getDisplayName())
            );

            Label gradeItemLabel = new Label("gradeItemLabel", this.assignment.getName());
            add(gradeItemLabel);

            boolean isComparingOrDisplayingFullName = getSettings()
                                    .getComparingDisplayStudentNames() || 
                           getSettings()
                                   .getComparingDisplayStudentSurnames();

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
                    return getSettings().getComparingDisplayTeacherComments();
                }
            };
            add(teacherCommentHeaderLabel);

            Label gradeHeaderLabel = new Label("gradeHeaderLabel" ,new ResourceModel("comparegrades.modal.table.header.grade"));
            add(gradeHeaderLabel);

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        Gson gson = new Gson();

        List<GbGradeComparisonItem> data = StudentCompareGradesPanel.this.businessService
                    .buildMatrixForGradeComparison(currentGradebookUid, currentSiteId,
                            this.assignment,
                            getGradebook().getGradeType(),
                            getSettings()
                    );

        String dataJson = gson.toJson(data);

        response.render(JavaScriptHeaderItem.forScript("window.GbComparisonData = "+dataJson+";", null));
    }


}
