/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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

import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;

public class RubricGradePanel extends BasePanel {

    private final ModalWindow window;

    public RubricGradePanel(final String id, final IModel<Map<String, Object>> model, final ModalWindow window) {
        super(id, model);
        this.window = window;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        // unpack model
        final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
        final Long assignmentId = (Long) modelData.get("assignmentId");
        final String studentUuid = (String) modelData.get("studentUuid");

        final Form form = new Form("sakaiRubricGradingForm");

        final WebMarkupContainer sakaiRubricGrading = new WebMarkupContainer("sakai-rubric-grading");
        sakaiRubricGrading.add(AttributeModifier.append("id", assignmentId));
        sakaiRubricGrading.add(AttributeModifier.append("tool-id", "sakai.gradebookng"));
        sakaiRubricGrading.add(AttributeModifier.append("entity-id", assignmentId));
        sakaiRubricGrading.add(AttributeModifier.append("evaluated-item-id", assignmentId + "." + studentUuid));
        form.add(sakaiRubricGrading);

        final GbAjaxButton submit = new GbAjaxButton("submit") {
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                rubricsService.saveRubricEvaluation("sakai.gradebookng", assignmentId.toString(), assignmentId + "." + studentUuid, studentUuid, getCurrentUserId(), getRubricParameters(""));
                target.appendJavaScript(String.format("GbGradeTable.instance.setDataAtCell(rubricGradingRow, rubricGradingCol, rubricGradingPoints.toString());", studentUuid, assignmentId));
                RubricGradePanel.this.window.close(target);
            }
        };
        form.add(submit);

        final GbAjaxButton cancel = new GbAjaxButton("cancel") {
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                RubricGradePanel.this.window.close(target);
            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);

        add(form);

        this.window.setInitialWidth(1100);
        RubricGradePanel.this.window.setTitle(new ResourceModel("rubrics.option.graderubric"));
    }
}
