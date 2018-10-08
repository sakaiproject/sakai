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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

public class RubricStudentPanel extends BasePanel {

    private final ModalWindow window;
    private String assignmentId;
    private String studentUuid;
	private String toolId;

    public RubricStudentPanel(final String id, final IModel<Map<String, Object>> model, final ModalWindow window) {
        super(id, model);
        this.window = window;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        final WebMarkupContainer sakaiRubricStudent = new WebMarkupContainer("sakai-rubric-student");
        sakaiRubricStudent.add(AttributeModifier.append("tool-id", toolId));
        sakaiRubricStudent.add(AttributeModifier.append("entity-id", assignmentId));//this only works for Assignments atm
        sakaiRubricStudent.add(AttributeModifier.append("evaluated-item-id", studentUuid));
        add(sakaiRubricStudent);

        this.window.setInitialWidth(1100);
        this.window.setCssClassName(window.getCssClassName() + " wicket-top-modal");
        RubricStudentPanel.this.window.setTitle(new ResourceModel("rubrics.grading_criteria"));
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public void setStudentUuid(String studentUuid) {
        this.studentUuid = studentUuid;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }
}
