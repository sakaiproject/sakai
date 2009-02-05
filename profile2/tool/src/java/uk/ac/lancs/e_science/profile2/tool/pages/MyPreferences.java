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
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.api.exception.ProfilePreferencesNotDefinedException;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePreferences;
import uk.ac.lancs.e_science.profile2.tool.components.HashMapChoiceRenderer;
import uk.ac.lancs.e_science.profile2.tool.components.IconWithClueTip;


public class MyPreferences extends BasePage {

	private transient Logger log = Logger.getLogger(MyPreferences.class);

	private transient ProfilePreferences profilePreferences;
		
	public MyPreferences() {
		
		//get current user
		String userId = sakaiProxy.getCurrentUserId();

		//get the preferences object for this user from the database
		profilePreferences = profile.getPreferencesRecordForUser(userId);
		
		//if null, create one
		if(profilePreferences == null) {
			profilePreferences = profile.createDefaultPreferencesRecord(userId);
			//if its still null, throw exception
			
			if(profilePreferences == null) {
				throw new ProfilePreferencesNotDefinedException("Couldn't create default preferences record for " + userId);
			}
			
		}
		
		Label heading = new Label("heading", new ResourceModel("heading.preferences"));
		add(heading);
		
		//feedback for form submit action
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		final String formFeedbackId = formFeedback.getMarkupId();
		add(formFeedback);
		
		
		
		//create model
		CompoundPropertyModel preferencesModel = new CompoundPropertyModel(profilePreferences);
		
		//setup form		
		Form form = new Form("form", preferencesModel);
		form.setOutputMarkupId(true);
		
		
		//setup LinkedHashMap of email options
		final LinkedHashMap<String, String> emailSettings = new LinkedHashMap<String, String>();
		emailSettings.put("0", new StringResourceModel("email.option.all", this,null).getString());
		emailSettings.put("1", new StringResourceModel("email.option.requestsonly", this,null).getString());
		emailSettings.put("2", new StringResourceModel("email.option.confirmonly", this,null).getString());
		emailSettings.put("3", new StringResourceModel("email.option.none", this,null).getString());
		
		
		//model that wraps our email options
		IModel emailSettingsModel = new Model() {
			public Object getObject() {
				 return new ArrayList(emailSettings.keySet()); //via proxy
			} 
		};
	
		//email settings
		WebMarkupContainer emailContainer = new WebMarkupContainer("emailContainer");
		emailContainer.add(new Label("emailLabel", new ResourceModel("preferences.email")));
		DropDownChoice emailChoice = new DropDownChoice("profile", emailSettingsModel, new HashMapChoiceRenderer(emailSettings));             
		emailContainer.add(emailChoice);
		//tooltip
		emailContainer.add(new IconWithClueTip("profileToolTip", IconWithClueTip.INFO_IMAGE, new ResourceModel("text.preferences.email.tooltip")));
		form.add(emailContainer);
		//updater
		emailChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//twitter settings
		WebMarkupContainer twitterContainer = new WebMarkupContainer("twitterContainer");
		twitterContainer.add(new Label("twitterLabel", new ResourceModel("preferences.twitter")));
		TextField twitterUsername = new TextField("twitterUsername", new PropertyModel(preferencesModel, "twitterUsername"));        
		TextField twitterPassword = new TextField("twitterPassword", new PropertyModel(preferencesModel, "twitterPassword"));        
		emailContainer.add(twitterUsername);
		emailContainer.add(twitterPassword);
		//tooltip
		twitterContainer.add(new IconWithClueTip("twitterToolTip", IconWithClueTip.INFO_IMAGE, new ResourceModel("text.preferences.twitter.tooltip")));
		form.add(twitterContainer);
		//updater
		twitterUsername.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		twitterPassword.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		
		
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.settings"), form) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show feedback. perhaps redirect back to main page after a short while?
				if(save(form)){
					formFeedback.setModel(new ResourceModel("success.privacy.save.ok"));
					formFeedback.add(new AttributeModifier("class", true, new Model("success")));
				} else {
					formFeedback.setModel(new ResourceModel("error.privacy.save.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));	
				}
				target.addComponent(formFeedback);
            }
		};
		form.add(submitButton);
		
        
		//cancel button
		/*
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            protected void onSubmit(AjaxRequestTarget target, Form form) {
				setResponsePage(new MyProfile());
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
		*/
        
        add(form);
        
        
 
        
		
	}
	
	
	//called when the form is to be saved
	private boolean save(Form form) {
		
		//get the backing model - its elems have been updated with the form params
		ProfilePreferences profilePreferences = (ProfilePreferences) form.getModelObject();

		/*
		if(profile.savePrivacyRecord(profilePrivacy)) {
			log.info("Saved ProfilePrivacy for: " + profilePrivacy.getUserUuid());
			return true;
		} else {
			log.info("Couldn't save ProfilePrivacy for: " + profilePrivacy.getUserUuid());
			return false;
		}
		*/
		return true;
	
	}
	
}



