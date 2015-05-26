/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.components;

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
            ajaxRequestTarget.add(formComponent);
        }

        if(updateComponent!=null){
            ajaxRequestTarget.add(updateComponent);
        }
    }
}
