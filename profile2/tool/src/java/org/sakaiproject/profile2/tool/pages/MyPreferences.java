/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

package org.sakaiproject.profile2.tool.pages;


import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.profile2.exception.ProfilePreferencesNotDefinedException;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.tool.components.EnablingCheckBox;
import org.sakaiproject.profile2.util.ProfileConstants;


public class MyPreferences extends BasePage{

	private static final Logger log = Logger.getLogger(MyPreferences.class);
	private transient ProfilePreferences profilePreferences;

	public MyPreferences() {
		
		log.debug("MyPreferences()");
		
		//get current user
		final String userUuid = sakaiProxy.getCurrentUserId();

		//get the prefs record for this user from the database, or a default if none exists yet
		profilePreferences = profileLogic.getPreferencesRecordForUser(userUuid);
		
		//if null, throw exception
		if(profilePreferences == null) {
			throw new ProfilePreferencesNotDefinedException("Couldn't create default preferences record for " + userUuid);
		}
		
		//get email address for this user
		String emailAddress = sakaiProxy.getUserEmail(userUuid);
		//if no email, set a message into it fo display
		if(emailAddress == null || emailAddress.length() == 0) {
			emailAddress = new ResourceModel("preferences.email.none").getObject().toString();
		}
				
		Label heading = new Label("heading", new ResourceModel("heading.preferences"));
		add(heading);
		
		//feedback for form submit action
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		final String formFeedbackId = formFeedback.getMarkupId();
		add(formFeedback);
		
				
		//create model
		CompoundPropertyModel<ProfilePreferences> preferencesModel = new CompoundPropertyModel<ProfilePreferences>(profilePreferences);
		
		//setup form		
		Form<ProfilePreferences> form = new Form<ProfilePreferences>("form", preferencesModel);
		form.setOutputMarkupId(true);
		
	
		//EMAIL SECTION
		
		//email settings
		form.add(new Label("emailSectionHeading", new ResourceModel("heading.section.email")));
		form.add(new Label("emailSectionText", new StringResourceModel("preferences.email.message", null, new Object[] { emailAddress })).setEscapeModelStrings(false));
	
		//on/off labels
		form.add(new Label("prefOn", new ResourceModel("preference.option.on")));
		form.add(new Label("prefOff", new ResourceModel("preference.option.off")));

		//request emails
		final RadioGroup<Boolean> emailRequests = new RadioGroup<Boolean>("requestEmailEnabled", new PropertyModel<Boolean>(preferencesModel, "requestEmailEnabled"));
		emailRequests.add(new Radio<Boolean>("requestsOn", new Model<Boolean>(new Boolean(true))));
		emailRequests.add(new Radio<Boolean>("requestsOff", new Model<Boolean>(new Boolean(false))));
		emailRequests.add(new Label("requestsLabel", new ResourceModel("preferences.email.requests")));
		form.add(emailRequests);
		
		//updater
		emailRequests.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//confirm emails
		final RadioGroup<Boolean> emailConfirms = new RadioGroup<Boolean>("confirmEmailEnabled", new PropertyModel<Boolean>(preferencesModel, "confirmEmailEnabled"));
		emailConfirms.add(new Radio<Boolean>("confirmsOn", new Model<Boolean>(new Boolean(true))));
		emailConfirms.add(new Radio<Boolean>("confirmsOff", new Model<Boolean>(new Boolean(false))));
		emailConfirms.add(new Label("confirmsLabel", new ResourceModel("preferences.email.confirms")));
		form.add(emailConfirms);
		
		//updater
		emailConfirms.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//new message emails
		final RadioGroup<Boolean> emailNewMessage = new RadioGroup<Boolean>("messageNewEmailEnabled", new PropertyModel<Boolean>(preferencesModel, "messageNewEmailEnabled"));
		emailNewMessage.add(new Radio<Boolean>("messageNewOn", new Model<Boolean>(new Boolean(true))));
		emailNewMessage.add(new Radio<Boolean>("messageNewOff", new Model<Boolean>(new Boolean(false))));
		emailNewMessage.add(new Label("messageNewLabel", new ResourceModel("preferences.email.message.new")));
		form.add(emailNewMessage);
		
		//updater
		emailNewMessage.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//message reply emails
		final RadioGroup<Boolean> emailReplyMessage = new RadioGroup<Boolean>("messageReplyEmailEnabled", new PropertyModel<Boolean>(preferencesModel, "messageReplyEmailEnabled"));
		emailReplyMessage.add(new Radio<Boolean>("messageReplyOn", new Model<Boolean>(new Boolean(true))));
		emailReplyMessage.add(new Radio<Boolean>("messageReplyOff", new Model<Boolean>(new Boolean(false))));
		emailReplyMessage.add(new Label("messageReplyLabel", new ResourceModel("preferences.email.message.reply")));
		form.add(emailReplyMessage);
		
		//updater
		emailReplyMessage.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
        
		
		// TWITTER SECTION
		WebMarkupContainer tc = new WebMarkupContainer("twitterContainer");
		tc.setOutputMarkupId(true);
				
		//twitter settings
		tc.add(new Label("twitterSectionHeading", new ResourceModel("heading.section.twitter")));
		tc.add(new Label("twitterSectionText", new ResourceModel("preferences.twitter.message")));

		//username
		WebMarkupContainer twitterUsernameContainer = new WebMarkupContainer("twitterUsernameContainer");
		twitterUsernameContainer.add(new Label("twitterUsernameLabel", new ResourceModel("twitter.username")));
		final TextField<String> twitterUsername = new TextField<String>("twitterUsername", new PropertyModel<String>(preferencesModel, "twitterUsername"));        
		twitterUsername.setOutputMarkupId(true);
		twitterUsername.setRequired(false);
		twitterUsernameContainer.add(twitterUsername);
			
		//updater
		twitterUsername.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		tc.add(twitterUsernameContainer);

		
		//password (already decrypted in the object)
		WebMarkupContainer twitterPasswordContainer = new WebMarkupContainer("twitterPasswordContainer");
		twitterPasswordContainer.add(new Label("twitterPasswordLabel", new ResourceModel("twitter.password")));
		final PasswordTextField twitterPassword = new PasswordTextField("twitterPassword", new PropertyModel<String>(preferencesModel, "twitterPasswordDecrypted"));        
		twitterPassword.setOutputMarkupId(true);
		twitterPassword.setRequired(false);
		twitterPassword.setResetPassword(false);
		twitterPasswordContainer.add(twitterPassword);
				
		//updater
		twitterPassword.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		tc.add(twitterPasswordContainer);

		
		//checkbox (needs to update above components)
		WebMarkupContainer twitterEnabledContainer = new WebMarkupContainer("twitterEnabledContainer");
		twitterEnabledContainer.add(new Label("twitterEnabledLabel", new ResourceModel("twitter.enabled")));
		final EnablingCheckBox twitterEnabled = new EnablingCheckBox("twitterEnabled", new PropertyModel<Boolean>(preferencesModel, "twitterEnabled")) {
			private static final long serialVersionUID = 1L;

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

		//updater
		twitterEnabled.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		tc.add(twitterEnabledContainer);

		//check if we can even show the twitter section by checking the sakai property
		if(!sakaiProxy.isTwitterIntegrationEnabledGlobally()) {
			profilePreferences.setTwitterEnabled(false); //set the model false to clear data as well (doesnt really need to do this but we do it to keep things in sync)
			tc.setVisible(false);
		}
	
		form.add(tc);
		
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
		
		
		
		// OFFICIAL IMAGE SECTION
		WebMarkupContainer is = new WebMarkupContainer("imageSettingsContainer");
		is.setOutputMarkupId(true);
				
		//official photo settings
		is.add(new Label("imageSettingsHeading", new ResourceModel("heading.section.image")));
		is.add(new Label("imageSettingsText", new ResourceModel("preferences.image.message")));

		//checkbox
		WebMarkupContainer officialImageContainer = new WebMarkupContainer("officialImageContainer");
		officialImageContainer.add(new Label("officialImageLabel", new ResourceModel("preferences.image.official")));
		CheckBox officialImage = new CheckBox("officialImage", new PropertyModel<Boolean>(preferencesModel, "useOfficialImage"));
		officialImageContainer.add(officialImage);

		//updater
		officialImage.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		is.add(officialImageContainer);
		
		//if using official images but alternate choice isn't allowed
		if(!sakaiProxy.isUsingOfficialImageButAlternateSelectionEnabled()) {
			profilePreferences.setUseOfficialImage(false); //set the model false to clear data as well (doesnt really need to do this but we do it to keep things in sync)
			is.setVisible(false);
		}
		
		form.add(is);
		
		
		
		// WIDGET SECTION
		WebMarkupContainer ws = new WebMarkupContainer("widgetSettingsContainer");
		ws.setOutputMarkupId(true);
		
		//widget settings
		ws.add(new Label("widgetSettingsHeading", new ResourceModel("heading.section.widget")));
		ws.add(new Label("widgetSettingsText", new ResourceModel("preferences.widget.message")));

		//kudos
		WebMarkupContainer kudosContainer = new WebMarkupContainer("kudosContainer");
		kudosContainer.add(new Label("kudosLabel", new ResourceModel("preferences.widget.kudos")));
		CheckBox kudosSetting = new CheckBox("kudosSetting", new PropertyModel<Boolean>(preferencesModel, "showKudos"));
		kudosContainer.add(kudosSetting);

		//updater
		kudosSetting.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		ws.add(kudosContainer);
		
		form.add(ws);
		
		
		
		//submit button
		IndicatingAjaxButton submitButton = new IndicatingAjaxButton("submit", form) {
			private static final long serialVersionUID = 1L;
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				//get the backing model
				ProfilePreferences profilePreferences = (ProfilePreferences) form.getModelObject();
				
				formFeedback.setDefaultModel(new ResourceModel("success.preferences.save.ok"));
				formFeedback.add(new AttributeModifier("class", true, new Model<String>("success")));
				
				//special case. if twitterEnabled is disabled, make sure the model fields are cleared and the form fields updated as well
				if(!profilePreferences.isTwitterEnabled()) {
					//clear data from model
					profilePreferences.setTwitterUsername(null);
					profilePreferences.setTwitterPasswordDecrypted(null);
					//clear input
					twitterUsername.clearInput();
					twitterPassword.clearInput();
					//repaint
					target.addComponent(twitterUsername);
					target.addComponent(twitterPassword);
				} 
				
				//validate twitter fields manually since this is a special form (needs checkbox to set the fields)
				if(profilePreferences.isTwitterEnabled()) {
					twitterUsername.validate();
					twitterPassword.validate();
					if(!twitterUsername.isValid() || !twitterPassword.isValid()) {
						formFeedback.setDefaultModel(new ResourceModel("error.twitter.details.required"));
						formFeedback.add(new AttributeModifier("class", true, new Model<String>("alertMessage")));	
						target.addComponent(formFeedback);
						return;
					}
					
					//PRFL-27 validate actual username/password details with Twitter itself
					String twitterUsernameEntered = twitterUsername.getDefaultModelObjectAsString();
					String twitterPasswordEntered = twitterPassword.getDefaultModelObjectAsString();

					if(!profileLogic.validateTwitterCredentials(twitterUsernameEntered, twitterPasswordEntered)) {
						formFeedback.setDefaultModel(new ResourceModel("error.twitter.details.invalid"));
						formFeedback.add(new AttributeModifier("class", true, new Model<String>("alertMessage")));	
						target.addComponent(formFeedback);
						return;
					}
					
					
				}
				
				//note that the twitter password is encrypted before its saved and decrypted for display, automatically
				
				
				if(profileLogic.savePreferencesRecord(profilePreferences)) {
					formFeedback.setDefaultModel(new ResourceModel("success.preferences.save.ok"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("success")));
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PREFERENCES_UPDATE, "/profile/"+userUuid, true);
					
				} else {
					formFeedback.setDefaultModel(new ResourceModel("error.preferences.save.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("alertMessage")));	
				}
				
				//resize iframe
				target.appendJavascript("setMainFrameHeight(window.name);");
				
				target.addComponent(formFeedback);
            }
			
			
		};
		submitButton.setModel(new ResourceModel("button.save.settings"));
		submitButton.setDefaultFormProcessing(false);
		form.add(submitButton);
		
        add(form);
		
	}
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MyPreferences has been deserialized.");
	}

}



