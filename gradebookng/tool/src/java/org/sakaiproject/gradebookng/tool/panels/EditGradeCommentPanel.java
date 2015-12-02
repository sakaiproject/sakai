package org.sakaiproject.gradebookng.tool.panels;

import java.io.Serializable;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormValidatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.validator.StringValidator;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * 
 * Panel for the modal window that allows an instructor to set/update a comment for a grade
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class EditGradeCommentPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
		
	private ModalWindow window;
	private String comment;
	
	public EditGradeCommentPanel(String id, IModel<Map<String, Object>> model, ModalWindow window) {
		super(id, model);
		this.window = window;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Map<String,Object> modelData = (Map<String,Object>) this.getDefaultModelObject();
		final Long assignmentId = (Long) modelData.get("assignmentId");
		final String studentUuid = (String) modelData.get("studentUuid");
		
		//fetch current comment
		this.comment = this.businessService.getAssignmentGradeComment(assignmentId, studentUuid);

		//form model
		GradeComment gradeComment = new GradeComment();
		gradeComment.setComment(comment);
		CompoundPropertyModel<GradeComment> formModel = new CompoundPropertyModel<GradeComment>(gradeComment);
		
		//build form
		//modal window forms must be submitted via AJAX so we do not specify an onSubmit here
		Form<GradeComment> form = new Form<GradeComment>("form", formModel);
		
		AjaxButton submit = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				GradeComment updatedComment = (GradeComment) form.getModelObject();
				
				boolean success = businessService.updateAssignmentGradeComment(assignmentId, studentUuid, updatedComment.getComment());
								
				if(success) {
					//update member var
					comment = updatedComment.getComment();
					
					//trigger a close
					window.close(target);
				} else {
					
					//TODO need to handle the error here
				}
			}
			
		};
		form.add(submit);
		
		AjaxButton cancel = new AjaxButton("cancel") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
        form.add(cancel);
        
        //heading
        //TODO if user/assignment has been deleted since rendering the GradebookPage, handle nulls here gracefully
        GbUser user = this.businessService.getUser(studentUuid);
        Assignment assignment = this.businessService.getAssignment(assignmentId);
        add(new Label("heading", new StringResourceModel("heading.editcomment", null, new Object[] {user.getDisplayName(), user.getDisplayId(), assignment.getName()})));
      	        
		//textarea
		form.add(new TextArea<String>("comment").add(StringValidator.maximumLength(500)));

		//instant validation
		//AjaxFormValidatingBehavior.addToAllFormComponents(form, "onkeyup", Duration.ONE_SECOND);
		
		add(form);
	}
	
	/**
	 * Getter for the comment string so we can update components on the parent page when the comment is saved here
	 * @return
	 */
	public String getComment() {
		return this.comment;
	}
	
	/**
	 * Model for this form
	 */
	class GradeComment implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		@Getter
		@Setter
		private String comment;
		
	}
	
	
	
}
