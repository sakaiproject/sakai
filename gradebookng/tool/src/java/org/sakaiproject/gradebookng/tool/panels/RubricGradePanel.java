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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.rubrics.logic.RubricsConstants;

public class RubricGradePanel extends BasePanel {

    @SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    private GradebookNgBusinessService businessService;

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
        final GbUser student = businessService.getUser(studentUuid);

        // Set the JS rubricGradingPoints variable to the current grade, if set.
        Map<Long, GbGradeInfo> grades = businessService.getGradesForStudent(studentUuid);
        String grade = grades.get(assignmentId).getGrade();
        if (grade == null) grade = "0";
        Label initPointsScript = new Label("initPointsScript", "<script>rubricGradingPoints = " + grade + ";</script>");
        initPointsScript.setEscapeModelStrings(false);
        add(initPointsScript);

        final Form form = new Form("sakaiRubricGradingForm");

        final WebMarkupContainer sakaiRubricGrading = new WebMarkupContainer("sakai-rubric-grading");
        sakaiRubricGrading.add(AttributeModifier.append("id", assignmentId));
        sakaiRubricGrading.add(AttributeModifier.append("tool-id", RubricsConstants.RBCS_TOOL_GRADEBOOKNG));
        sakaiRubricGrading.add(AttributeModifier.append("entity-id", assignmentId));
        sakaiRubricGrading.add(AttributeModifier.append("evaluated-item-id", assignmentId + "." + studentUuid));
        sakaiRubricGrading.add(AttributeModifier.append("evaluated-item-owner-id", studentUuid));
        sakaiRubricGrading.add(AttributeModifier.append("token", rubricsService.generateJsonWebToken(RubricsConstants.RBCS_TOOL_GRADEBOOKNG)));
        form.add(sakaiRubricGrading);

        final GbAjaxButton submit = new GbAjaxButton("submit") {
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                target.appendJavaScript(String.format("GbGradeTable.instance.setDataAtCell(rubricGradingRow, rubricGradingCol, rubricGradingPoints.toString());", studentUuid, assignmentId));
                RubricGradePanel.this.window.close(target);
            }
        };
        submit.setOutputMarkupId(true).setMarkupId("saverubric");
        form.add(submit);

        final GbAjaxButton cancel = new GbAjaxButton("cancel") {
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                RubricGradePanel.this.window.close(target);
            }
        };
        cancel.setOutputMarkupId(true).setMarkupId("cancelrubric");
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);

        add(form);

        this.window.setInitialWidth(1100);
        RubricGradePanel.this.window.setTitle(new StringResourceModel("rubrics.option.graderubric.for", null, new Object[] { student.getDisplayName(), student.getDisplayId() }));
    }

	public void renderHead(final IHeaderResponse response) {

		final String version = PortalUtils.getCDNQuery();
		response.render(StringHeaderItem.forString(
			"<script src=\"/webcomponents/rubrics/sakai-rubrics-utils.js" + version + "\"></script>"));
		response.render(StringHeaderItem.forString(
			"<script type=\"module\" src=\"/webcomponents/rubrics/rubric-association-requirements.js" + version + "\"></script>"));
    }
}
