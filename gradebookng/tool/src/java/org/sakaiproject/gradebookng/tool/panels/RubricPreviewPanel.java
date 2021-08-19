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

import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.rubrics.logic.RubricsConstants;
import org.sakaiproject.service.gradebook.shared.Assignment;

public class RubricPreviewPanel extends BasePanel {

    @SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    private GradebookNgBusinessService businessService;

    private final ModalWindow window;

    public RubricPreviewPanel(final String id, final IModel<Map<String, Object>> model, final ModalWindow window) {
        super(id, model);
        this.window = window;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
        final Long assignmentId = (Long) modelData.get("assignmentId");
        final WebMarkupContainer sakaiRubricPreview = new WebMarkupContainer("sakai-rubric-student");
        Assignment assignmentNow = businessService.getAssignment(assignmentId);
        if(assignmentNow!=null && assignmentNow.isExternallyMaintained()){  //this is an externally-maintained item from Assignments
            sakaiRubricPreview.add(AttributeModifier.append("entity-id", extractAssignmentId(assignmentNow.getExternalId())));  //rubric association needs Assignment id, not Gradebook id
            sakaiRubricPreview.add(AttributeModifier.append("tool-id", RubricsConstants.RBCS_TOOL_ASSIGNMENT));
        } else {
            sakaiRubricPreview.add(AttributeModifier.append("entity-id", assignmentId));
            sakaiRubricPreview.add(AttributeModifier.append("tool-id", RubricsConstants.RBCS_TOOL_GRADEBOOKNG));
        }
        sakaiRubricPreview.add(AttributeModifier.append("token", rubricsService.generateJsonWebToken(RubricsConstants.RBCS_TOOL_GRADEBOOKNG)));
        sakaiRubricPreview.add(AttributeModifier.append("instructor", "true"));
        add(sakaiRubricPreview);
        final GbAjaxButton done = new GbAjaxButton("done") {
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                RubricPreviewPanel.this.window.close(target);
            }
        };
        done.setDefaultFormProcessing(false);
        add(done);
        this.window.setInitialWidth(1100);
        Assignment assignment = businessService.getAssignment(assignmentId);
        RubricPreviewPanel.this.window.setTitle(this.getString("label.rubric.preview") + assignment.getName());
    }

    public void renderHead(final IHeaderResponse response) {
        final String version = PortalUtils.getCDNQuery();
        response.render(StringHeaderItem.forString(
                "<script src=\"/webcomponents/rubrics/sakai-rubrics-utils.js" + version + "\"></script>"));
        response.render(StringHeaderItem.forString(
                "<script type=\"module\" src=\"/webcomponents/rubrics/rubric-association-requirements.js" + version + "\"></script>"));
    }

    private String extractAssignmentId(String externalId){
        if (externalId==null || !externalId.contains("/")){ //make sure we have an ID that exists and contains slashes
            return "";
        }
        String[] splitArray = externalId.split("/");    //go ahead and split it since we know it won't break.
        if(splitArray.length < 5){  //make sure the expected position of the external ID exists.
            return "";
        }
        return splitArray[4];   //we assume the external ID to be in the fourth position
    }
}
