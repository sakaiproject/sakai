package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.EditGradeCommentPanel;
import org.sakaiproject.gradebookng.tool.panels.GradeLogPanel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EditCommentAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    private GradebookNgBusinessService businessService;

    public EditCommentAction(GradebookNgBusinessService businessService) {
        this.businessService = businessService;
    }

    private class EmptyOkResponse implements ActionResponse {
        public EmptyOkResponse() {
        }

        public String getStatus() {
            return "OK";
        }

        public String toJson() {
            return "{}";
        }
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        String assignmentId = params.get("assignmentId").asText();
        String studentUuid = params.get("studentId").asText();

        Map<String, Object> model = new HashMap<>();
        model.put("assignmentId", Long.valueOf(assignmentId));
        model.put("studentUuid", studentUuid);

        final GradebookPage gradebookPage = (GradebookPage) target.getPage();
        final GbModalWindow window = gradebookPage.getGradeCommentWindow();

        final EditGradeCommentPanel panel = new EditGradeCommentPanel(
            window.getContentId(),
            Model.ofMap(model),
            window);

        window.setContent(panel);
        window.showUnloadConfirmation(false);
        window.clearWindowClosedCallbacks();
        window.setAssignmentToReturnFocusTo(assignmentId);
        window.setStudentToReturnFocusTo(studentUuid);
        window.addWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClose(final AjaxRequestTarget target) {
                String comment = panel.getComment();

                target.appendJavaScript(
                    String.format("GbGradeTable.updateComment('%s', '%s', '%s');",
                        assignmentId,
                        studentUuid,
                        comment == null ? "" : comment));
            }
        });
        window.show(target);

        return new EmptyOkResponse();
    }
}
