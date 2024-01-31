package org.sakaiproject.gradebookng.tool.panels;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import org.sakaiproject.gradebookng.business.importExport.CommentValidator;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;

import lombok.Getter;
import lombok.Setter;

/**
 * Panel for the modal window that allows an instructor to set/update a comment for a course grade
 */
public class EditCourseGradeCommentPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    private final ModalWindow window;
    /**
     * -- GETTER --
     *  Getter for the comment string so we can update components on the parent page when the comment is saved here
     *
     * @return
     */
    @Getter
    private String comment;

    public EditCourseGradeCommentPanel(final String id, final IModel<Map<String, Object>> model, final ModalWindow window) {
        super(id, model);
        this.window = window;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        // unpack model
        final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
        final Long courseGradeId = (Long) modelData.get("courseGradeId");
        final String studentUuid = (String) modelData.get("studentUuid");
        final Long gradebookId = (Long) modelData.get("gradebookId");
        // fetch current comment
        this.comment = this.businessService.getAssignmentGradeComment(currentGradebookUid, courseGradeId, studentUuid);
        // form model
        final GradeComment gradeComment = new GradeComment();
        gradeComment.setGradeComment(this.comment);
        final CompoundPropertyModel<GradeComment> formModel = new CompoundPropertyModel<>(gradeComment);
        // modal window forms must be submitted via AJAX so we do not specify an onSubmit here
        final Form<GradeComment> form = new Form<>("form", formModel);
        final GbAjaxButton submit = new GbAjaxButton("submit") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(final AjaxRequestTarget target) {
                final GradeComment updatedComment = (GradeComment) form.getModelObject();
                final boolean success = businessService.updateAssignmentGradeComment(currentGradebookUid, currentSiteId, businessService.getCourseGradeId(gradebookId), studentUuid, updatedComment.getGradeComment());
                if (success) {
                    // update member var
                    EditCourseGradeCommentPanel.this.comment = updatedComment.getGradeComment();
                    // trigger a close
                    EditCourseGradeCommentPanel.this.window.close(target);
                } else {
                    error("Unable to save course grade comment for course grade id " + courseGradeId + ", student id " + studentUuid);
                }
            }
        };
        form.add(submit);
        final GbAjaxButton cancel = new GbAjaxButton("cancel") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(final AjaxRequestTarget target) {
                EditCourseGradeCommentPanel.this.window.close(target);
            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);
        // heading
        final GbUser user = this.businessService.getUser(studentUuid);
        EditCourseGradeCommentPanel.this.window.setTitle((new StringResourceModel("heading.editcomment")
                .setParameters(user.getDisplayName(), user.getDisplayId(), "Course Grade")).getString())
                .setEscapeModelStrings(false);
        // textarea
        form.add(new TextArea<>("comment", new PropertyModel<>(formModel, "gradeComment")).add(StringValidator.maximumLength(CommentValidator.getMaxCommentLength(serverConfigService))));
        add(form);
    }

    /**
     * Model for this form
     */
    class GradeComment implements Serializable {

        private static final long serialVersionUID = 1L;

        @Getter
        @Setter
        private String gradeComment;
    }

}