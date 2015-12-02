package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.text.MessageFormat;
import java.util.Map;

public class DeleteItemPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
		
	private ModalWindow window;
	
	public DeleteItemPanel(String id, IModel<Long> model, ModalWindow window) {
		super(id, model);
		this.window = window;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		final Long assignmentId = (Long) this.getDefaultModelObject();
		
		Form<Long> form = new Form("form", Model.of(assignmentId));
		
		AjaxButton submit = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				Long assignmentIdToDelete = (Long) form.getModelObject();
				Assignment assignment = businessService.getAssignment(assignmentIdToDelete);
				String assignmentTitle = assignment.getName();

				businessService.removeAssignment(assignmentIdToDelete);

				getSession().info(MessageFormat.format(getString("delete.success"), assignmentTitle));
				setResponsePage(new GradebookPage());
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

		add(form);
	}
}
