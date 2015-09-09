package org.sakaiproject.gradebookng.tool.panels;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;

/**
 * 
 * Panel for the modal window that allows an instructor to update the ungraded items
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class UpdateUngradedItemsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
		
	private ModalWindow window;
	
	private IModel<Long> model;
			
	private static final double DEFAULT_GRADE = 0;

	private boolean isExtraCreditValue = false;

	public UpdateUngradedItemsPanel(String id, IModel<Long> model, ModalWindow window) {
		super(id);
		this.model = model;
		this.window = window;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		final Long assignmentId = this.model.getObject();
		
		//form model
		GradeOverride override = new GradeOverride();
		override.setGrade(String.valueOf(DEFAULT_GRADE));
		CompoundPropertyModel<GradeOverride> formModel = new CompoundPropertyModel<GradeOverride>(override);

		//build form
		//modal window forms must be submitted via AJAX so we do not specify an onSubmit here
		Form<GradeOverride> form = new Form<GradeOverride>("form", formModel);
		
		AjaxButton submit = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				GradeOverride override = (GradeOverride) form.getModelObject();

				Assignment assignment = businessService.getAssignment(assignmentId);

				try {
					Double overrideValue = Double.valueOf(override.getGrade());

					if (overrideValue > assignment.getPoints()) {

						target.addChildren(form, FeedbackPanel.class);
					} else {
						isExtraCreditValue = false;
					}

					boolean	success = businessService.updateUngradedItems(assignmentId, overrideValue);

					if(success) {
						window.close(target);
						setResponsePage(new GradebookPage());
					} else {
						// InvalidGradeException
						error(getString("grade.notifications.invalid"));
						target.addChildren(form, FeedbackPanel.class);
						target.appendJavaScript("new GradebookUpdateUngraded($(\"#"+getParent().getMarkupId()+"\"));");
					}
				} catch (NumberFormatException e) {
					// InvalidGradeException
					error(getString("grade.notifications.invalid"));
					target.addChildren(form, FeedbackPanel.class);
					target.appendJavaScript("new GradebookUpdateUngraded($(\"#"+getParent().getMarkupId()+"\"));");
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
		
		form.add(new TextField<Double>("grade").setRequired(true));

		add(form);

		FeedbackPanel feedback = new FeedbackPanel("updateGradeFeedback") {
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

		Assignment assignment = businessService.getAssignment(assignmentId);
		WebMarkupContainer hiddenGradePoints = new WebMarkupContainer("gradePoints");
		hiddenGradePoints.add(new AttributeModifier("value", assignment.getPoints()));
		form.add(hiddenGradePoints);
	}
	
	/**
	 * Model for this form
	 */
	class GradeOverride implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		@Getter
		@Setter
		private String grade;
		
	}
	
	
	
}
