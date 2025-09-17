package org.sakaiproject.gradebookng.tool.actions;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.EditCourseGradeCommentPanel;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EditCourseGradeCommentAction extends InjectableAction implements Serializable{
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
    public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
        final String courseGradeId = params.get("courseGradeId").asText();
        final String studentUuid = params.get("studentId").asText();
        final String gradebookId = params.get("gradebookId").asText();
        final Map<String, Object> model = new HashMap<>();
        model.put("courseGradeId", Long.valueOf(courseGradeId));
        model.put("studentUuid", studentUuid);
        model.put("gradebookId", Long.valueOf(gradebookId));
        final GradebookPage gradebookPage = (GradebookPage) target.getPage();
        final GbModalWindow window = gradebookPage.getGradeCommentWindow();
        final EditCourseGradeCommentPanel panel = new EditCourseGradeCommentPanel(window.getContentId(), Model.ofMap(model), window);
        window.setContent(panel);
        window.showUnloadConfirmation(false);
        window.clearWindowClosedCallbacks();
        window.setAssignmentToReturnFocusTo(courseGradeId);
        window.setStudentToReturnFocusTo(studentUuid);
        window.addWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClose(final AjaxRequestTarget target) {
                final String comment = panel.getComment();
                target.appendJavaScript(
                    String.format("GbGradeTable.updateCourseGradeComment('%s', '%s', '%s');",
                        courseGradeId,
                        studentUuid,
                        comment == null ? "" : comment));
            }
        });
        window.show(target);
        return new EditCourseGradeCommentAction.EmptyOkResponse();
    }

}
