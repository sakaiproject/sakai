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
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.jasypt.util.text.BasicTextEncryptor;

import uk.ac.lancs.e_science.profile2.api.ProfileIntegrationManager;
import uk.ac.lancs.e_science.profile2.api.exception.ProfilePreferencesNotDefinedException;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePreferences;
import uk.ac.lancs.e_science.profile2.tool.components.EnablingCheckBox;
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
		
		//decrypt the password
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(ProfileIntegrationManager.BASIC_ENCRYPTION_KEY);
		profilePreferences.setTwitterPassword(textEncryptor.decrypt(profilePreferences.getTwitterPassword()));
		
		
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
		form.add(new Label("emailSectionHeading", new ResourceModel("heading.section.email")));
		//tooltip
		form.add(new IconWithClueTip("emailToolTip", IconWithClueTip.INFO_IMAGE, new ResourceModel("text.preferences.email.tooltip")));
		
		WebMarkupContainer emailContainer = new WebMarkupContainer("emailContainer");
		emailContainer.add(new Label("emailLabel", new ResourceModel("preferences.email")));
		DropDownChoice emailChoice = new DropDownChoice("email", emailSettingsModel, new HashMapChoiceRenderer(emailSettings));             
		emailContainer.add(emailChoice);
		form.add(emailContainer);
		//updater
		emailChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
        
		
		//twitter settings
		form.add(new Label("twitterSectionHeading", new ResourceModel("heading.section.twitter")));
		//tooltip
		form.add(new IconWithClueTip("twitterToolTip", IconWithClueTip.INFO_IMAGE, new ResourceModel("text.preferences.twitter.tooltip")));
		
		//username
		WebMarkupContainer twitterUsernameContainer = new WebMarkupContainer("twitterUsernameContainer");
		twitterUsernameContainer.add(new Label("twitterUsernameLabel", new ResourceModel("twitter.username")));
		final TextField twitterUsername = new TextField("twitterUsername", new PropertyModel(preferencesModel, "twitterUsername"));        
		twitterUsername.setOutputMarkupId(true);
		twitterUsername.setRequired(false);
		twitterUsernameContainer.add(twitterUsername);
			
		//updater
		twitterUsername.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		form.add(twitterUsernameContainer);

		
		//password
		WebMarkupContainer twitterPasswordContainer = new WebMarkupContainer("twitterPasswordContainer");
		twitterPasswordContainer.add(new Label("twitterPasswordLabel", new ResourceModel("twitter.password")));
		final PasswordTextField twitterPassword = new PasswordTextField("twitterPassword", new PropertyModel(preferencesModel, "twitterPassword"));        
		twitterPassword.setOutputMarkupId(true);
		twitterPassword.setRequired(false);
		twitterPassword.setResetPassword(false);
		twitterPasswordContainer.add(twitterPassword);
		
		//updater
		twitterPassword.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		form.add(twitterPasswordContainer);

		
		//checkbox (needs to update above components)
		WebMarkupContainer twitterEnabledContainer = new WebMarkupContainer("twitterEnabledContainer");
		twitterEnabledContainer.add(new Label("twitterEnabledLabel", new ResourceModel("twitter.enabled")));
		final EnablingCheckBox twitterEnabled = new EnablingCheckBox("twitterEnabled", new PropertyModel(preferencesModel, "twitterEnabled")) {
			protected void onUpdate(AjaxRequestTarget target) { 
				if(isChecked()) {
					//enable fields
					twitterUsername.setEnabled(true);
					twitterPassword.setEnabled(true);
					
					//make them required
					twitterUsername.setRequired(true);
					twitterPassword.setRequired(true);
					
				} else {
					//disable fields
					twitterUsername.setEnabled(false);
					twitterPassword.setEnabled(false);
					
					//make them not required
					twitterUsername.setRequired(false);
					twitterPassword.setRequired(false);
				}
				
				//repaint
				target.addComponent(twitterUsername);
				target.addComponent(twitterPassword);
			}
		};
		twitterEnabledContainer.add(twitterEnabled);
		form.add(twitterEnabledContainer);
		//updater
		twitterEnabled.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		// set initial required/enabled states on the twitter fields
		if(profilePreferences.isTwitterEnabled()) {
			twitterUsername.setEnabled(true);
			twitterPassword.setEnabled(true);
			twitterUsername.setRequired(true);
			twitterPassword.setRequired(true);
		} else {
			twitterUsername.setEnabled(false);
			twitterPassword.setEnabled(false);
			twitterUsername.setRequired(false);
			twitterPassword.setRequired(false);
		}
		
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.settings"), form) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				//get the backing model
				ProfilePreferences profilePreferences = (ProfilePreferences) form.getModelObject();

				//special case. if twitterEnabled is disabled, make sure the fields are cleared
				//else validate ourselves
				if(!profilePreferences.isTwitterEnabled()) {
					profilePreferences.setTwitterUsername(null);
					profilePreferences.setTwitterPassword(null);
				} 
				
				if(profilePreferences.isTwitterEnabled()) {
					twitterUsername.validate();
					twitterPassword.validate();
					if(!twitterUsername.isValid() || !twitterPassword.isValid()) {
						formFeedback.setModel(new ResourceModel("error.twitter.details.required"));
						formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));	
						target.addComponent(formFeedback);
						return;
					}
					
					//encrypt the password
					BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
					textEncryptor.setPassword(ProfileIntegrationManager.BASIC_ENCRYPTION_KEY);
					profilePreferences.setTwitterPassword(textEncryptor.encrypt(profilePreferences.getTwitterPassword()));
					
				}
						
				
				if(profile.savePreferencesRecord(profilePreferences)) {
					log.info("Saved ProfilePreferences for: " + profilePreferences.getUserUuid());
					formFeedback.setModel(new ResourceModel("success.preferences.save.ok"));
					formFeedback.add(new AttributeModifier("class", true, new Model("success")));
				} else {
					log.info("Couldn't save ProfilePreferences for: " + profilePreferences.getUserUuid());
					formFeedback.setModel(new ResourceModel("error.preferences.save.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));	
				}	
				
				target.addComponent(formFeedback);
            }
			
			
		};
		submitButton.setDefaultFormProcessing(false);
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
	
}



