package org.sakaiproject.gradebookng.tool.panels;

import java.text.MessageFormat;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * The panel for the add and edit grade item window
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AddOrEditGradeItemPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<Long> model;
	
	/**
	 * How this panel is rendered
	 */
	enum Mode {
		ADD,
		EDIT;
	}
	
	Mode mode;

	public AddOrEditGradeItemPanel(String id, final ModalWindow window, IModel<Long> model) {
		super(id);
		this.model = model;
				
		//determine mode
		if(model != null) {
			mode = Mode.EDIT;
		} else {
			mode = Mode.ADD;
		}
		
		//setup the backing object
		Assignment assignment;
		
		if(mode == Mode.EDIT) {
			Long assignmentId = this.model.getObject();
			assignment = this.businessService.getAssignment(assignmentId);
			
			//TODO if we are in edit mode and don't have an assignment, need to error here
			
		} else {
			//Mode.ADD
			assignment = new Assignment();
			// Default released to true
			assignment.setReleased(true);
			// If no categories, then default counted to true
			Gradebook gradebook = businessService.getGradebook();
			assignment.setCounted(GradebookService.CATEGORY_TYPE_NO_CATEGORY == gradebook.getCategory_type());
		}
		
		//form model
		Model<Assignment> formModel = new Model<Assignment>(assignment);

		//form
		Form<Assignment> form = new Form<Assignment>("addOrEditGradeItemForm", formModel);

		AjaxButton submit = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Assignment assignment =  (Assignment) form.getModelObject();

				if(mode == Mode.EDIT) {
					
					//TODO validation of the fields here
					
					boolean success = businessService.updateAssignment(assignment);
					
					if (success) {
						getSession().info(MessageFormat.format(getString("message.edititem.success"), assignment.getName()));
						setResponsePage(getPage().getPageClass());
					} else {
						error(new ResourceModel("message.edititem.error").getObject());
						target.addChildren(form, FeedbackPanel.class);
					}
					
				} else {
				
					Long assignmentId = null;
	
					boolean success = true;
					try {
						assignmentId = businessService.addAssignment(assignment);
					} catch (AssignmentHasIllegalPointsException e) {
						error(new ResourceModel("error.addgradeitem.points").getObject());
						success = false;
					} catch (ConflictingAssignmentNameException e) {
						error(new ResourceModel("error.addgradeitem.title").getObject());
						success = false;
					} catch (ConflictingExternalIdException e) {
						error(new ResourceModel("error.addgradeitem.exception").getObject());
						success = false;
					} catch (Exception e) {
						error(new ResourceModel("error.addgradeitem.exception").getObject());
						success = false;
					}
					if (success) {
						getSession().info(MessageFormat.format(getString("notification.addgradeitem.success"), assignment.getName()));
						setResponsePage(getPage().getPageClass(), new PageParameters().add(GradebookPage.CREATED_ASSIGNMENT_ID_PARAM, assignmentId));
					} else {
						target.addChildren(form, FeedbackPanel.class);
					}
				}
				
			}
		};
		
		//submit button label
		submit.add(new Label("submitLabel", this.getSubmitButtonLabel()));
		form.add(submit);
		
		//heading
		form.add(new Label("heading", this.getHeadingLabel()));

		//add the common components
		form.add(new AddOrEditGradeItemPanelContent("subComponents", formModel));

		//feedback panel
		FeedbackPanel feedback = new FeedbackPanel("addGradeFeedback") {
			private static final long serialVersionUID = 1L;

			@Override
			protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
				final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

				if(message.getLevel() == FeedbackMessage.ERROR ||
								message.getLevel() == FeedbackMessage.DEBUG ||
								message.getLevel() == FeedbackMessage.FATAL ||
								message.getLevel() == FeedbackMessage.WARNING){
					add(AttributeModifier.replace("class", "messageError"));
					add(AttributeModifier.append("class", "feedback"));
				} else if(message.getLevel() == FeedbackMessage.INFO){
					add(AttributeModifier.replace("class", "messageSuccess"));
					add(AttributeModifier.append("class", "feedback"));
				}

				return newMessageDisplayComponent;
			}
		};
		feedback.setOutputMarkupId(true);
		form.add(feedback);

		//cancel button
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
	
	/**
	 * Helper to get the model for the button
	 * @return
	 */
	private ResourceModel getSubmitButtonLabel() {
		if(mode == Mode.EDIT) {
			return new ResourceModel("button.savechanges");
		} else {
			return new ResourceModel("button.create");
		}
	}
	
	/**
	 * Helper to get the model for the heading
	 * @return
	 */
	private ResourceModel getHeadingLabel() {
		if(mode == Mode.EDIT) {
			return new ResourceModel("heading.editgradeitem");
		} else {
			return new ResourceModel("heading.addgradeitem");
		}
	}
	
}
