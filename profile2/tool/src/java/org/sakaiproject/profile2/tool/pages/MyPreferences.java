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
package org.sakaiproject.profile2.tool.pages;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.profile2.exception.ProfilePreferencesNotDefinedException;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.util.ProfileConstants;

@Slf4j
public class MyPreferences extends BasePage{

	private transient ProfilePreferences profilePreferences;

	private CheckBox officialImage;
	private CheckBox gravatarImage;
	
	private boolean officialImageEnabled;
	private boolean gravatarEnabled;
	
	public MyPreferences() {
		
		log.debug("MyPreferences()");
		
		preferencesLink.setEnabled(false);
		preferencesContainer.add(new AttributeModifier("class", "current"));
		
		//get current user
		final String userUuid = sakaiProxy.getCurrentUserId();

		//get the prefs record for this user from the database, or a default if none exists yet
		profilePreferences = preferencesLogic.getPreferencesRecordForUser(userUuid, false);
		
		//if null, throw exception
		if(profilePreferences == null) {
			throw new ProfilePreferencesNotDefinedException("Couldn't retrieve preferences record for " + userUuid);
		}
		
		//get email address for this user
		String emailAddress = sakaiProxy.getUserEmail(userUuid);
		//if no email, set a message into it fo display
		if(emailAddress == null || emailAddress.isEmpty()) {
			emailAddress = new ResourceModel("preferences.email.none").getObject();
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
		
		// IMAGE SECTION
		//only one of these can be selected at a time
		WebMarkupContainer is = new WebMarkupContainer("imageSettingsContainer");
		is.setOutputMarkupId(true);
				
		// headings
		is.add(new Label("imageSettingsHeading", new ResourceModel("heading.section.image")));
		is.add(new Label("imageSettingsText", new ResourceModel("preferences.image.message")));

		officialImageEnabled = sakaiProxy.isUsingOfficialImageButAlternateSelectionEnabled();
		gravatarEnabled = sakaiProxy.isGravatarImageEnabledGlobally();

		//official image
		//checkbox
		WebMarkupContainer officialImageContainer = new WebMarkupContainer("officialImageContainer");
		officialImageContainer.add(new Label("officialImageLabel", new ResourceModel("preferences.image.official")));
		officialImage = new CheckBox("officialImage", new PropertyModel<Boolean>(preferencesModel, "useOfficialImage"));
		officialImage.setMarkupId("officialimageinput");
		officialImage.setOutputMarkupId(true);
		officialImageContainer.add(officialImage);

		//updater
		officialImage.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				
				//set gravatar to false since we can't have both active
				gravatarImage.setModelObject(false);
				if(gravatarEnabled) {
					target.add(gravatarImage);
				}				
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		is.add(officialImageContainer);
		
		//if using official images but alternate choice isn't allowed, hide this section
		if(!officialImageEnabled) {
			profilePreferences.setUseOfficialImage(false); //set the model false to clear data as well (doesnt really need to do this but we do it to keep things in sync)
			officialImageContainer.setVisible(false);
		}
				
		//gravatar
		//checkbox
		WebMarkupContainer gravatarContainer = new WebMarkupContainer("gravatarContainer");
		gravatarImage = new CheckBox("gravatarImage", new PropertyModel<Boolean>(preferencesModel, "useGravatar"));
		gravatarImage.setMarkupId("gravatarimageinput");
		gravatarImage.setOutputMarkupId(true);
		gravatarContainer.add(gravatarImage);

		//updater
		gravatarImage.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				
				//set gravatar to false since we can't have both active
				officialImage.setModelObject(false);
				if(officialImageEnabled) {
					target.add(officialImage);
				}
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		is.add(gravatarContainer);
		
		//if gravatar's are disabled, hide this section
		if(!gravatarEnabled) {
			profilePreferences.setUseGravatar(false); //set the model false to clear data as well (doesnt really need to do this but we do it to keep things in sync)
			gravatarContainer.setVisible(false);
		}
		
		//if official image disabled and gravatar disabled, hide the entire container
		if(!officialImageEnabled && !gravatarEnabled) {
			is.setVisible(false);
		}
		
		form.add(is);
		
		//submit button
		IndicatingAjaxButton submitButton = new IndicatingAjaxButton("submit", form) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				
				//get the backing model
				ProfilePreferences profilePreferences = (ProfilePreferences) form.getModelObject();
				
				formFeedback.setDefaultModel(new ResourceModel("success.preferences.save.ok"));
				formFeedback.add(new AttributeModifier("class", new Model<String>("success")));
				
				//save
				if(preferencesLogic.savePreferencesRecord(profilePreferences)) {
					formFeedback.setDefaultModel(new ResourceModel("success.preferences.save.ok"));
					formFeedback.add(new AttributeModifier("class", new Model<String>("success")));
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PREFERENCES_UPDATE, "/profile/"+userUuid, true);
					
				} else {
					formFeedback.setDefaultModel(new ResourceModel("error.preferences.save.failed"));
					formFeedback.add(new AttributeModifier("class", new Model<String>("alertMessage")));	
				}
				
				//resize iframe
				target.appendJavaScript("setMainFrameHeight(window.name);");
				
				//PRFL-775 - set focus to feedback message so it is announced to screenreaders
				target.appendJavaScript("$('#" + formFeedbackId + "').focus();");
				
				target.add(formFeedback);
            }
		};
		submitButton.setModel(new ResourceModel("button.save.settings"));
		submitButton.setDefaultFormProcessing(false);
		form.add(submitButton);
		
        add(form);
	}
}



