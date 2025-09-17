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
import java.util.List;

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

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.grading.api.Assignment;

public class RubricPreviewPanel extends BasePanel {

    @SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    private GradebookNgBusinessService businessService;

    private final ModalWindow window;

    public RubricPreviewPanel(String id, IModel<Long> model, ModalWindow window) {

        super(id, model);

        this.window = window;
    }

    @Override
    public void onInitialize() {

        super.onInitialize();
        final Long assignmentId = (Long) getDefaultModelObject();
        final WebMarkupContainer sakaiRubricPreview = new WebMarkupContainer("sakai-rubric-student");
        sakaiRubricPreview.add(AttributeModifier.append("site-id", getCurrentSiteId()));
        Assignment assignmentNow = businessService.getAssignment(currentGradebookUid, currentSiteId, assignmentId);
        if (assignmentNow != null && assignmentNow.getExternallyMaintained()) {  //this is an externally-maintained item from Assignments
            sakaiRubricPreview.add(AttributeModifier.append("entity-id", extractAssignmentId(assignmentNow.getExternalId())));  //rubric association needs Assignment id, not Gradebook id
            sakaiRubricPreview.add(AttributeModifier.append("tool-id", AssignmentConstants.TOOL_ID));
        } else {
            sakaiRubricPreview.add(AttributeModifier.append("entity-id", assignmentId));
            sakaiRubricPreview.add(AttributeModifier.append("tool-id", RubricsConstants.RBCS_TOOL_GRADEBOOKNG));
        }
        sakaiRubricPreview.add(AttributeModifier.append("instructor", "true"));
        add(sakaiRubricPreview);
        final GbAjaxButton done = new GbAjaxButton("done") {
            @Override
            public void onSubmit(final AjaxRequestTarget target) {
                RubricPreviewPanel.this.window.close(target);
            }
        };
        done.setDefaultFormProcessing(false);
        add(done);
        this.window.setInitialWidth(1100);
        RubricPreviewPanel.this.window.setTitle(this.getString("label.rubric.preview") + assignmentNow.getName());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        String version = PortalUtils.getCDNQuery();
        response.render(StringHeaderItem.forString(
                "<script type=\"module\" src=\"/webcomponents/bundles/rubric-association-requirements.js" + version + "\"></script>"));
    }

    private String extractAssignmentId(String externalId) {

        if (externalId == null) { //make sure we have an ID that exists
            return "";
        }

        String[] splitArray = externalId.split("/");    //go ahead and split it since we know it won't break.
        if (splitArray.length < 5) {  //make sure the expected position of the external ID exists.
            return "";
        }
        return splitArray[4];   //we assume the external ID to be in the fourth position
    }
}
