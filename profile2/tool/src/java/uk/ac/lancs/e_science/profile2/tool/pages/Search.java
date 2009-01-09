package uk.ac.lancs.e_science.profile2.tool.pages;


import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.api.exception.ProfilePrivacyException;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;
import uk.ac.lancs.e_science.profile2.tool.components.HashMapChoiceRenderer;
import uk.ac.lancs.e_science.profile2.tool.components.IconWithClueTip;


public class Search extends BasePage {

	private transient Logger log = Logger.getLogger(Search.class);

		
	public Search() {
		
		if(log.isDebugEnabled()) log.debug("MyPrivacy()");
		

		//get current user
		String userId = sakaiProxy.getCurrentUserId();

				
		Label heading = new Label("heading", new ResourceModel("heading.search"));
		add(heading);
		
		/*
		//setup form		
		Form form = new Form("form", privacyModel);
		form.setOutputMarkupId(true);
		
		*/
		
		
		//submit button
		/*
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.settings"), form) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show feedback. perhaps redirect back to main page after a short while?
				if(save(form)){
					formFeedback.setModel(new ResourceModel("success.save.ok"));
					formFeedback.add(new AttributeModifier("class", true, new Model("success")));
					target.addComponent(formFeedback);
				} else {
					formFeedback.setModel(new ResourceModel("error.save.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));	
				}
				target.addComponent(formFeedback);
            }
		};
		form.add(submitButton);
		
        
        
        add(form);
        
        */
 
        
		
	}
	
	/*
	//called when the form is to be saved
	private boolean save(Form form) {
		
		//get the backing model - its elems have been updated with the form params
		
		//call function to search
	
	}
	*/
}



