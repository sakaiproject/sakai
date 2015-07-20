package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.model.GbAssignment;
import org.sakaiproject.gradebookng.tool.model.GbAssignmentModel;
import org.sakaiproject.service.gradebook.shared.*;
import org.sakaiproject.gradebookng.tool.pages.BasePage;

import java.util.List;
import java.lang.Exception;

/**
 * The panel for the add grade item window
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AddGradeItemPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public AddGradeItemPanel(String id, List<CategoryDefinition> categories, final ModalWindow window) {
		super(id);
    
    GbAssignmentModel model = new GbAssignmentModel(new GbAssignment());
    
    Form<?> form = new Form("addGradeItemForm", model);

    AjaxButton submit = new AjaxButton("submit") {
      private static final long serialVersionUID = 1L;
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form) {
        GbAssignment model =  (GbAssignment) form.getModelObject();
        Assignment assignment = model.convert2Assignment();

        boolean success = true;
        try {
          ((BasePage) getPage()).businessService.addAssignment(assignment);
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
          getSession().info(new ResourceModel("notification.addgradeitem.success").getObject());
          setResponsePage(getPage().getPageClass());
        } else {
          target.addChildren(form, FeedbackPanel.class);
        }
        
      }
    };
		form.add(submit);

    form.add(new AddGradeItemPanelContent("subComponents", model, categories));

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

    AjaxButton cancel = new AjaxButton("cancel") {
      private static final long serialVersionUID = 1L;
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form) {
        window.close(target);
      }
    };
    form.add(cancel);
    add(form);
	}


}
