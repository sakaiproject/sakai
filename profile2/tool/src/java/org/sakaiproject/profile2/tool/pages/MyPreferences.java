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
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.profile2.exception.ProfilePreferencesNotDefinedException;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.tool.components.EnablingCheckBox;
import org.sakaiproject.profile2.tool.components.IconWithClueTip;
import org.sakaiproject.profile2.tool.pages.panels.TwitterPrefsPane;
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
		
		disableLink(preferencesLink);
		
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
		if(emailAddress == null || emailAddress.length() == 0) {
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
		
	
		//EMAIL SECTION
		
		//email settings
		form.add(new Label("emailSectionHeading", new ResourceModel("heading.section.email")));
		form.add(new Label("emailSectionText", new StringResourceModel("preferences.email.message", null, new Object[] { emailAddress })).setEscapeModelStrings(false));
	
		//on/off labels
		form.add(new Label("prefOn", new ResourceModel("preference.option.on")));
		form.add(new Label("prefOff", new ResourceModel("preference.option.off")));

		//request emails
		final RadioGroup<Boolean> emailRequests = new RadioGroup<Boolean>("requestEmailEnabled", new PropertyModel<Boolean>(preferencesModel, "requestEmailEnabled"));
		Radio requestsOn = new Radio<Boolean>("requestsOn", new Model<Boolean>(Boolean.valueOf(true)));
		requestsOn.setMarkupId("requestsoninput");
		requestsOn.setOutputMarkupId(true);
		emailRequests.add(requestsOn);
		Radio requestsOff = new Radio<Boolean>("requestsOff", new Model<Boolean>(Boolean.valueOf(false)));
		requestsOff.setMarkupId("requestsoffinput");
		requestsOff.setOutputMarkupId(true);
		emailRequests.add(requestsOff);
		emailRequests.add(new Label("requestsLabel", new ResourceModel("preferences.email.requests")));
		form.add(emailRequests);
		
		//updater
		emailRequests.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//visibility for request emails
		emailRequests.setVisible(sakaiProxy.isConnectionsEnabledGlobally());

		//confirm emails
		final RadioGroup<Boolean> emailConfirms = new RadioGroup<Boolean>("confirmEmailEnabled", new PropertyModel<Boolean>(preferencesModel, "confirmEmailEnabled"));
		Radio confirmsOn = new Radio<Boolean>("confirmsOn", new Model<Boolean>(Boolean.valueOf(true)));
		confirmsOn.setMarkupId("confirmsoninput");
		confirmsOn.setOutputMarkupId(true);
		emailConfirms.add(confirmsOn);
		Radio confirmsOff = new Radio<Boolean>("confirmsOff", new Model<Boolean>(Boolean.valueOf(false)));
		confirmsOff.setMarkupId("confirmsoffinput");
		confirmsOff.setOutputMarkupId(true);
		emailConfirms.add(confirmsOff);
		emailConfirms.add(new Label("confirmsLabel", new ResourceModel("preferences.email.confirms")));
		form.add(emailConfirms);
		
		//updater
		emailConfirms.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//visibility for confirm emails
		emailConfirms.setVisible(sakaiProxy.isConnectionsEnabledGlobally());

		//new message emails
		final RadioGroup<Boolean> emailNewMessage = new RadioGroup<Boolean>("messageNewEmailEnabled", new PropertyModel<Boolean>(preferencesModel, "messageNewEmailEnabled"));
		Radio messageNewOn = new Radio<Boolean>("messageNewOn", new Model<Boolean>(Boolean.valueOf(true)));
		messageNewOn.setMarkupId("messagenewoninput");
		messageNewOn.setOutputMarkupId(true);
		emailNewMessage.add(messageNewOn);
		Radio messageNewOff = new Radio<Boolean>("messageNewOff", new Model<Boolean>(Boolean.valueOf(false)));
		messageNewOff.setMarkupId("messagenewoffinput");
		messageNewOff.setOutputMarkupId(true);
		emailNewMessage.add(messageNewOff);
		emailNewMessage.add(new Label("messageNewLabel", new ResourceModel("preferences.email.message.new")));
		form.add(emailNewMessage);
		
		//updater
		emailNewMessage.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		emailNewMessage.setVisible(sakaiProxy.isMessagingEnabledGlobally());
		
		//message reply emails
		final RadioGroup<Boolean> emailReplyMessage = new RadioGroup<Boolean>("messageReplyEmailEnabled", new PropertyModel<Boolean>(preferencesModel, "messageReplyEmailEnabled"));
		Radio messageReplyOn = new Radio<Boolean>("messageReplyOn", new Model<Boolean>(Boolean.valueOf(true)));
		messageReplyOn.setMarkupId("messagereplyoninput");
		messageNewOn.setOutputMarkupId(true);
		emailReplyMessage.add(messageReplyOn);
		Radio messageReplyOff = new Radio<Boolean>("messageReplyOff", new Model<Boolean>(Boolean.valueOf(false)));
		messageReplyOff.setMarkupId("messagereplyoffinput");
		messageNewOff.setOutputMarkupId(true);
		emailReplyMessage.add(messageReplyOff);
		emailReplyMessage.add(new Label("messageReplyLabel", new ResourceModel("preferences.email.message.reply")));
		form.add(emailReplyMessage);
		
		//updater
		emailReplyMessage.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		emailReplyMessage.setVisible(sakaiProxy.isMessagingEnabledGlobally());
		
		// new wall item notification emails
		final RadioGroup<Boolean> wallItemNew = new RadioGroup<Boolean>("wallItemNewEmailEnabled", new PropertyModel<Boolean>(preferencesModel, "wallItemNewEmailEnabled"));
		Radio wallItemNewOn = new Radio<Boolean>("wallItemNewOn", new Model<Boolean>(Boolean.valueOf(true)));
		wallItemNewOn.setMarkupId("wallitemnewoninput");
		wallItemNewOn.setOutputMarkupId(true);
		wallItemNew.add(wallItemNewOn);
		Radio wallItemNewOff = new Radio<Boolean>("wallItemNewOff", new Model<Boolean>(Boolean.valueOf(false)));
		wallItemNewOff.setMarkupId("wallitemnewoffinput");
		wallItemNewOff.setOutputMarkupId(true);
		wallItemNew.add(wallItemNewOff);
		wallItemNew.add(new Label("wallItemNewLabel", new ResourceModel("preferences.email.wall.new")));
		form.add(wallItemNew);
		
		//updater
		wallItemNew.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//visibility for wall items
		wallItemNew.setVisible(sakaiProxy.isWallEnabledGlobally());

		// added to new worksite emails
		final RadioGroup<Boolean> worksiteNew = new RadioGroup<Boolean>("worksiteNewEmailEnabled", new PropertyModel<Boolean>(preferencesModel, "worksiteNewEmailEnabled"));
		Radio worksiteNewOn = new Radio<Boolean>("worksiteNewOn", new Model<Boolean>(Boolean.valueOf(true)));
		worksiteNewOn.setMarkupId("worksitenewoninput");
		worksiteNewOn.setOutputMarkupId(true);
		worksiteNew.add(worksiteNewOn);
		Radio worksiteNewOff = new Radio<Boolean>("worksiteNewOff", new Model<Boolean>(Boolean.valueOf(false)));
		worksiteNewOff.setMarkupId("worksitenewoffinput");
		worksiteNewOff.setOutputMarkupId(true);
		worksiteNew.add(worksiteNewOff);
		worksiteNew.add(new Label("worksiteNewLabel", new ResourceModel("preferences.email.worksite.new")));
		form.add(worksiteNew);
		
		//updater
		worksiteNew.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
        
		
		// TWITTER SECTION

		//headings
		WebMarkupContainer twitterSectionHeadingContainer = new WebMarkupContainer("twitterSectionHeadingContainer");
		twitterSectionHeadingContainer.add(new Label("twitterSectionHeading", new ResourceModel("heading.section.twitter")));
		twitterSectionHeadingContainer.add(new Label("twitterSectionText", new ResourceModel("preferences.twitter.message")));
		form.add(twitterSectionHeadingContainer);
		
		//panel
		if(sakaiProxy.isTwitterIntegrationEnabledGlobally()) {
			form.add(new AjaxLazyLoadPanel("twitterPanel"){
				private static final long serialVersionUID = 1L;
	
				@Override
				public Component getLazyLoadComponent(String markupId) {
					return new TwitterPrefsPane(markupId, userUuid);
				}
			});
		} else {
			form.add(new EmptyPanel("twitterPanel"));
			twitterSectionHeadingContainer.setVisible(false);
		}
		
		
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
		officialImage.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;
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
		gravatarContainer.add(new Label("gravatarLabel", new ResourceModel("preferences.image.gravatar")));
		gravatarImage = new CheckBox("gravatarImage", new PropertyModel<Boolean>(preferencesModel, "useGravatar"));
		gravatarImage.setMarkupId("gravatarimageinput");
		gravatarImage.setOutputMarkupId(true);
		gravatarContainer.add(gravatarImage);

		//updater
		gravatarImage.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;
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

		
		// WIDGET SECTION
		WebMarkupContainer ws = new WebMarkupContainer("widgetSettingsContainer");
		ws.setOutputMarkupId(true);
		int visibleWidgetCount = 0;
		
		//widget settings
		ws.add(new Label("widgetSettingsHeading", new ResourceModel("heading.section.widget")));
		ws.add(new Label("widgetSettingsText", new ResourceModel("preferences.widget.message")));

		//kudos
		WebMarkupContainer kudosContainer = new WebMarkupContainer("kudosContainer");
		kudosContainer.add(new Label("kudosLabel", new ResourceModel("preferences.widget.kudos")));
		CheckBox kudosSetting = new CheckBox("kudosSetting", new PropertyModel<Boolean>(preferencesModel, "showKudos"));
		kudosSetting.setMarkupId("kudosinput");
		kudosSetting.setOutputMarkupId(true);
		kudosContainer.add(kudosSetting);
		//tooltip
		kudosContainer.add(new IconWithClueTip("kudosToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("preferences.widget.kudos.tooltip")));
		

		//updater
		kudosSetting.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		ws.add(kudosContainer);
		if(sakaiProxy.isMyKudosEnabledGlobally()) {
			visibleWidgetCount++;
		} else {
			kudosContainer.setVisible(false);
		}

		
		//gallery feed
		WebMarkupContainer galleryFeedContainer = new WebMarkupContainer("galleryFeedContainer");
		galleryFeedContainer.add(new Label("galleryFeedLabel", new ResourceModel("preferences.widget.gallery")));
		CheckBox galleryFeedSetting = new CheckBox("galleryFeedSetting", new PropertyModel<Boolean>(preferencesModel, "showGalleryFeed"));
		galleryFeedSetting.setMarkupId("galleryfeedsettinginput");
		galleryFeedSetting.setOutputMarkupId(true);
		galleryFeedContainer.add(galleryFeedSetting);
		//tooltip
		galleryFeedContainer.add(new IconWithClueTip("galleryFeedToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("preferences.widget.gallery.tooltip")));
		
		//updater
		galleryFeedSetting.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		ws.add(galleryFeedContainer);
		if(sakaiProxy.isProfileGalleryEnabledGlobally()) {
            visibleWidgetCount++;
        } else {
            galleryFeedContainer.setVisible(false);
        }
		
		
		//online status
		WebMarkupContainer onlineStatusContainer = new WebMarkupContainer("onlineStatusContainer");
		onlineStatusContainer.add(new Label("onlineStatusLabel", new ResourceModel("preferences.widget.onlinestatus")));
		CheckBox onlineStatusSetting = new CheckBox("onlineStatusSetting", new PropertyModel<Boolean>(preferencesModel, "showOnlineStatus"));
		onlineStatusSetting.setMarkupId("onlinestatussettinginput");
		onlineStatusSetting.setOutputMarkupId(true);
		onlineStatusContainer.add(onlineStatusSetting);
		//tooltip
		onlineStatusContainer.add(new IconWithClueTip("onlineStatusToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("preferences.widget.onlinestatus.tooltip")));
		
		//updater
		onlineStatusSetting.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;
			protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		ws.add(onlineStatusContainer);		
		
		if(sakaiProxy.isOnlineStatusEnabledGlobally()){
        visibleWidgetCount++;
        } else {
            onlineStatusContainer.setVisible(false);
        }
        
        // Hide widget container if nothing to show
        if(visibleWidgetCount == 0) {
            ws.setVisible(false);
        }

		form.add(ws);
		
		//submit button
		IndicatingAjaxButton submitButton = new IndicatingAjaxButton("submit", form) {
			private static final long serialVersionUID = 1L;
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				//get the backing model
				ProfilePreferences profilePreferences = (ProfilePreferences) form.getModelObject();
				
				formFeedback.setDefaultModel(new ResourceModel("success.preferences.save.ok"));
				formFeedback.add(new AttributeModifier("class", true, new Model<String>("success")));
				
				//save
				if(preferencesLogic.savePreferencesRecord(profilePreferences)) {
					formFeedback.setDefaultModel(new ResourceModel("success.preferences.save.ok"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("success")));
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PREFERENCES_UPDATE, "/profile/"+userUuid, true);
					
				} else {
					formFeedback.setDefaultModel(new ResourceModel("error.preferences.save.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("alertMessage")));	
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



