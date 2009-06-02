package uk.ac.lancs.e_science.profile2.tool.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;


/**
 * Behavior that checks if a {@link FormComponent} is valid. Valid {@link FormComponent} objects get the CSS class
 * 'formcomponent valid' and invalid {@link FormComponent} objects get the CSS class 'formcomponent invalid'.
 *
 * See {@link AjaxFormComponentUpdatingBehavior} for more details over the parent class.
 *
 * You can use this code under Apache 2.0 license, as long as you retain the copyright messages.
 *
 * Tested with Wicket 1.3.4
 * @author Daan, StuQ.nl
 */
public class ComponentVisualErrorBehaviour extends AjaxFormComponentUpdatingBehavior {

    /** Field updateComponent holds the component that must be updated when validation is done.*/
    private Component updateComponent = null;

    /**
     * Constructor.
     *
     * @param event of type {@link String} (for example 'onblur', 'onkeyup', etc.)
     * @param updateComponent is the {@link Component} that must be updated (for example the {@link FeedbackLabel}
     *        containing the error message for this {@link FormComponent})  
     */
    public ComponentVisualErrorBehaviour(String event, Component updateComponent) {
        super(event);
        this.updateComponent=updateComponent;
    }

    /**
     * Listener invoked on the ajax request. This listener is invoked after the {@link Component}'s model has been
     * updated. Handles the change of a css class when an error has occurred.
     *
     * @param ajaxRequestTarget of type AjaxRequestTarget
     * @param e of type RuntimeException
     */
    @Override
    protected void onError(AjaxRequestTarget ajaxRequestTarget, RuntimeException e) {
        changeCssClass(ajaxRequestTarget, false, "invalid");
    }

    /**
     * Listener invoked on the ajax request. This listener is invoked after the {@link Component}'s model has been
     * updated. Handles the change of a css class when validation was succesful.
     *
     * @param ajaxRequestTarget of type AjaxRequestTarget
     */
    @Override
    protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
        changeCssClass(ajaxRequestTarget, true, "valid");
    }

    /**
     * Changes the CSS class of the linked {@link FormComponent} via AJAX.
     *
     * @param ajaxRequestTarget of type AjaxRequestTarget
     * @param valid Was the validation succesful?
     * @param cssClass The CSS class that must be set on the linked {@link FormComponent}
     */
    private void changeCssClass(AjaxRequestTarget ajaxRequestTarget, boolean valid, String cssClass) {
        FormComponent formComponent = getFormComponent();

        if(formComponent.isValid() == valid){
            formComponent.add(new AttributeModifier("class", true, new Model("formInputField " + cssClass)));
            ajaxRequestTarget.addComponent(formComponent);
        }

        if(updateComponent!=null){
            ajaxRequestTarget.addComponent(updateComponent);
        }
    }
}
