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


import java.util.ArrayList;
import java.util.LinkedHashMap;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
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

import org.sakaiproject.profile2.exception.ProfilePrivacyNotDefinedException;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.components.HashMapChoiceRenderer;
import org.sakaiproject.profile2.tool.components.IconWithClueTip;
import org.sakaiproject.profile2.util.ProfileConstants;

@Slf4j
public class MyPrivacy extends BasePage {

	private transient ProfilePrivacy profilePrivacy;
		
	public MyPrivacy() {
		
		log.debug("MyPrivacy()");

		disableLink(myPrivacyLink);
		
		//get current user
		final String userUuid = sakaiProxy.getCurrentUserId();

		//get the privacy record for this user from the database, or a default if none exists
		profilePrivacy = privacyLogic.getPrivacyRecordForUser(userUuid, false);
		
		//if null, throw exception
		if(profilePrivacy == null) {
			throw new ProfilePrivacyNotDefinedException("Couldn't retrieve privacy record for " + userUuid);
		}
		
		Label heading = new Label("heading", new ResourceModel("heading.privacy"));
		add(heading);
		
		Label infoLocked = new Label("infoLocked");
		infoLocked.setOutputMarkupPlaceholderTag(true);
		infoLocked.setVisible(false);
		add(infoLocked);
		
		//feedback for form submit action
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		final String formFeedbackId = formFeedback.getMarkupId();
		add(formFeedback);
		
		
		
		//create model
		CompoundPropertyModel<ProfilePrivacy> privacyModel = new CompoundPropertyModel<ProfilePrivacy>(profilePrivacy);
		
		//setup form		
		Form<ProfilePrivacy> form = new Form<ProfilePrivacy>("form", privacyModel);
		form.setOutputMarkupId(true);
		
		
		//setup LinkedHashMap of privacy options for strict things
		final LinkedHashMap<Integer, String> privacySettingsStrict = new LinkedHashMap<Integer, String>();
		privacySettingsStrict.put(ProfileConstants.PRIVACY_OPTION_EVERYONE, new StringResourceModel("privacy.option.everyone", this,null).getString());
		privacySettingsStrict.put(ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS, new StringResourceModel("privacy.option.onlyfriends", this,null).getString());
		privacySettingsStrict.put(ProfileConstants.PRIVACY_OPTION_ONLYME, new StringResourceModel("privacy.option.onlyme", this,null).getString());
		
		//model that wraps our options
		IModel dropDownModelStrict = new Model() {
			public ArrayList<Integer> getObject() {
				 return new ArrayList(privacySettingsStrict.keySet());
			} 
		};
		
		//setup LinkedHashMap of privacy options for more relaxed things
		final LinkedHashMap<Integer, String> privacySettingsRelaxed = new LinkedHashMap<Integer, String>();
		privacySettingsRelaxed.put(ProfileConstants.PRIVACY_OPTION_EVERYONE, new StringResourceModel("privacy.option.everyone", this,null).getString());
		privacySettingsRelaxed.put(ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS, new StringResourceModel("privacy.option.onlyfriends", this,null).getString());
		
		//model that wraps our options
		IModel dropDownModelRelaxed = new Model() {
			public ArrayList<Integer> getObject() {
				 return new ArrayList(privacySettingsRelaxed.keySet());
			} 
		};
		
		//setup LinkedHashMap of privacy options for super duper strict things!
		final LinkedHashMap<Integer, String> privacySettingsSuperStrict = new LinkedHashMap<Integer, String>();
		privacySettingsSuperStrict.put(ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS, new StringResourceModel("privacy.option.onlyfriends", this,null).getString());
		privacySettingsSuperStrict.put(ProfileConstants.PRIVACY_OPTION_NOBODY, new StringResourceModel("privacy.option.nobody", this,null).getString());
		
		//model that wraps our options
		IModel dropDownModelSuperStrict = new Model() {
			public ArrayList<Integer> getObject() {
				 return new ArrayList(privacySettingsSuperStrict.keySet());
			} 
		};
		
		//when using DDC with a compoundPropertyModel we use this constructor: DDC<T>(String,IModel<List<T>>,IChoiceRenderer<T>)
		//and the ID of the DDC field maps to the field in the CompoundPropertyModel
		
		//the AjaxFormComponentUpdatingBehavior is to allow the DDC and checkboxes to fadeaway any error/success message
		//that might be visible since the form has changed and it needs to be submitted again for it to take effect
		
		//profile image privacy
		WebMarkupContainer profileImageContainer = new WebMarkupContainer("profileImageContainer");
		profileImageContainer.add(new Label("profileImageLabel", new ResourceModel("privacy.profileimage")));
		DropDownChoice profileImageChoice = new DropDownChoice("profileImage", dropDownModelRelaxed, new HashMapChoiceRenderer(privacySettingsRelaxed));             
		profileImageChoice.setMarkupId("imageprivacyinput");
		profileImageChoice.setOutputMarkupId(true);
		profileImageContainer.add(profileImageChoice);
		//tooltip
		profileImageContainer.add(new IconWithClueTip("profileImageToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.profileimage.tooltip")));
		form.add(profileImageContainer);
		//updater
		profileImageChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		
		
		//basicInfo privacy
		WebMarkupContainer basicInfoContainer = new WebMarkupContainer("basicInfoContainer");
		basicInfoContainer.add(new Label("basicInfoLabel", new ResourceModel("privacy.basicinfo")));
		DropDownChoice basicInfoChoice = new DropDownChoice("basicInfo", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		basicInfoChoice.setMarkupId("basicinfoprivacyinput");
		basicInfoChoice.setOutputMarkupId(true);
		basicInfoContainer.add(basicInfoChoice);
		//tooltip
		basicInfoContainer.add(new IconWithClueTip("basicInfoToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.basicinfo.tooltip")));
		form.add(basicInfoContainer);
		//updater
		basicInfoChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//contactInfo privacy
		WebMarkupContainer contactInfoContainer = new WebMarkupContainer("contactInfoContainer");
		contactInfoContainer.add(new Label("contactInfoLabel", new ResourceModel("privacy.contactinfo")));
		DropDownChoice contactInfoChoice = new DropDownChoice("contactInfo", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		contactInfoChoice.setMarkupId("contactinfoprivacyinput");
		contactInfoChoice.setOutputMarkupId(true);
		contactInfoContainer.add(contactInfoChoice);
		//tooltip
		contactInfoContainer.add(new IconWithClueTip("contactInfoToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.contactinfo.tooltip")));
		form.add(contactInfoContainer);
		//updater
		contactInfoChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//staffInfo privacy
		WebMarkupContainer staffInfoContainer = new WebMarkupContainer("staffInfoContainer");
		staffInfoContainer.add(new Label("staffInfoLabel", new ResourceModel("privacy.staffinfo")));
		DropDownChoice staffInfoChoice = new DropDownChoice("staffInfo", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		staffInfoChoice.setMarkupId("staffinfoprivacyinput");
		staffInfoChoice.setOutputMarkupId(true);
		staffInfoContainer.add(staffInfoChoice);
		//tooltip
		staffInfoContainer.add(new IconWithClueTip("staffInfoToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.staff.tooltip")));
		form.add(staffInfoContainer);
		//updater
		staffInfoChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//studentInfo privacy
		WebMarkupContainer studentInfoContainer = new WebMarkupContainer("studentInfoContainer");
		studentInfoContainer.add(new Label("studentInfoLabel", new ResourceModel("privacy.studentinfo")));
		DropDownChoice studentInfoChoice = new DropDownChoice("studentInfo", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		studentInfoChoice.setMarkupId("studentinfoprivacyinput");
		studentInfoChoice.setOutputMarkupId(true);
		studentInfoContainer.add(studentInfoChoice);
		//tooltip
		studentInfoContainer.add(new IconWithClueTip("studentInfoToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.student.tooltip")));
		form.add(studentInfoContainer);
		//updater
		studentInfoChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//businesInfo privacy
		WebMarkupContainer businessInfoContainer = new WebMarkupContainer("businessInfoContainer");
		businessInfoContainer.add(new Label("businessInfoLabel", new ResourceModel("privacy.businessinfo")));
		DropDownChoice businessInfoChoice = new DropDownChoice("businessInfo", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		businessInfoChoice.setMarkupId("businessinfoprivacyinput");
		businessInfoChoice.setOutputMarkupId(true);
		businessInfoContainer.add(businessInfoChoice);
		//tooltip
		businessInfoContainer.add(new IconWithClueTip("businessInfoToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.businessinfo.tooltip")));
		form.add(businessInfoContainer);
		//updater
		businessInfoChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		businessInfoContainer.setVisible(sakaiProxy.isBusinessProfileEnabled());
		
		//socialNetworkingInfo privacy
		WebMarkupContainer socialNetworkingInfoContainer = new WebMarkupContainer("socialNetworkingInfoContainer");
		socialNetworkingInfoContainer.add(new Label("socialNetworkingInfoLabel", new ResourceModel("privacy.socialinfo")));
		DropDownChoice socialNetworkingInfoChoice = new DropDownChoice("socialNetworkingInfo", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		socialNetworkingInfoChoice.setMarkupId("socialinfoprivacyinput");
		socialNetworkingInfoChoice.setOutputMarkupId(true);
		socialNetworkingInfoContainer.add(socialNetworkingInfoChoice);
		//tooltip
		socialNetworkingInfoContainer.add(new IconWithClueTip("socialNetworkingInfoToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.socialinfo.tooltip")));
		form.add(socialNetworkingInfoContainer);
		//updater
		socialNetworkingInfoChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		
		//personalInfo privacy
		WebMarkupContainer personalInfoContainer = new WebMarkupContainer("personalInfoContainer");
		personalInfoContainer.add(new Label("personalInfoLabel", new ResourceModel("privacy.personalinfo")));
		DropDownChoice personalInfoChoice = new DropDownChoice("personalInfo", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		personalInfoChoice.setMarkupId("personalinfoprivacyinput");
		personalInfoChoice.setOutputMarkupId(true);
		personalInfoContainer.add(personalInfoChoice);
		//tooltip
		personalInfoContainer.add(new IconWithClueTip("personalInfoToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.personalinfo.tooltip")));
		form.add(personalInfoContainer);
		//updater
		personalInfoChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//birthYear privacy
		WebMarkupContainer birthYearContainer = new WebMarkupContainer("birthYearContainer");
		birthYearContainer.add(new Label("birthYearLabel", new ResourceModel("privacy.birthyear")));
		CheckBox birthYearCheckbox = new CheckBox("birthYear", new PropertyModel(privacyModel, "showBirthYear"));
		birthYearCheckbox.setMarkupId("birthyearprivacyinput");
		birthYearCheckbox.setOutputMarkupId(true);
		birthYearContainer.add(birthYearCheckbox);
		//tooltip
		birthYearContainer.add(new IconWithClueTip("birthYearToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.birthyear.tooltip")));
		form.add(birthYearContainer);
		//updater
		birthYearCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
				
		//myFriends privacy
		WebMarkupContainer myFriendsContainer = new WebMarkupContainer("myFriendsContainer");
		myFriendsContainer.add(new Label("myFriendsLabel", new ResourceModel("privacy.myfriends")));
		DropDownChoice myFriendsChoice = new DropDownChoice("myFriends", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		myFriendsChoice.setMarkupId("friendsprivacyinput");
		myFriendsChoice.setOutputMarkupId(true);
		myFriendsContainer.add(myFriendsChoice);
		//tooltip
		myFriendsContainer.add(new IconWithClueTip("myFriendsToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.myfriends.tooltip")));
		form.add(myFriendsContainer);
		//updater
		myFriendsChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		myFriendsContainer.setVisible(sakaiProxy.isConnectionsEnabledGlobally());

		//myStatus privacy
		WebMarkupContainer myStatusContainer = new WebMarkupContainer("myStatusContainer");
		myStatusContainer.add(new Label("myStatusLabel", new ResourceModel("privacy.mystatus")));
		DropDownChoice myStatusChoice = new DropDownChoice("myStatus", dropDownModelRelaxed, new HashMapChoiceRenderer(privacySettingsRelaxed));
		myStatusChoice.setMarkupId("statusprivacyinput");
		myStatusChoice.setOutputMarkupId(true);
		myStatusContainer.add(myStatusChoice);
		//tooltip
		myStatusContainer.add(new IconWithClueTip("myStatusToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.mystatus.tooltip")));
		form.add(myStatusContainer);
		//updater
		myStatusChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		myStatusContainer.setVisible(sakaiProxy.isProfileStatusEnabled());

		// gallery privacy
		WebMarkupContainer myPicturesContainer = new WebMarkupContainer("myPicturesContainer");
		myPicturesContainer.add(new Label("myPicturesLabel", new ResourceModel("privacy.mypictures")));
		DropDownChoice myPicturesChoice = new DropDownChoice("myPictures", dropDownModelRelaxed, new HashMapChoiceRenderer(privacySettingsRelaxed));             
		myPicturesChoice.setMarkupId("picturesprivacyinput");
		myPicturesChoice.setOutputMarkupId(true);
		myPicturesContainer.add(myPicturesChoice);
		myPicturesContainer.add(new IconWithClueTip("myPicturesToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.mypictures.tooltip")));
		form.add(myPicturesContainer);

		myPicturesChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		myPicturesContainer.setVisible(sakaiProxy.isProfileGalleryEnabledGlobally());
		
		// messages privacy
		WebMarkupContainer messagesContainer = new WebMarkupContainer("messagesContainer");
		messagesContainer.add(new Label("messagesLabel", new ResourceModel("privacy.messages")));
		DropDownChoice messagesChoice = new DropDownChoice("messages", dropDownModelSuperStrict, new HashMapChoiceRenderer(privacySettingsSuperStrict));             
		messagesChoice.setMarkupId("messagesprivacyinput");
		messagesChoice.setOutputMarkupId(true);
		messagesContainer.add(messagesChoice);
		messagesContainer.add(new IconWithClueTip("messagesToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.messages.tooltip")));
		form.add(messagesContainer);

		messagesChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		messagesContainer.setVisible(sakaiProxy.isMessagingEnabledGlobally());
		
		// kudos privacy
		WebMarkupContainer myKudosContainer = new WebMarkupContainer("myKudosContainer");
		myKudosContainer.add(new Label("myKudosLabel", new ResourceModel("privacy.mykudos")));
		DropDownChoice kudosChoice = new DropDownChoice("myKudos", dropDownModelRelaxed, new HashMapChoiceRenderer(privacySettingsRelaxed));             
		kudosChoice.setMarkupId("kudosprivacyinput");
		kudosChoice.setOutputMarkupId(true);
		myKudosContainer.add(kudosChoice);
		myKudosContainer.add(new IconWithClueTip("myKudosToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.mykudos.tooltip")));
		form.add(myKudosContainer);

		kudosChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		myKudosContainer.setVisible(sakaiProxy.isMyKudosEnabledGlobally());

		// wall privacy
		WebMarkupContainer myWallContainer = new WebMarkupContainer("myWallContainer");
		myWallContainer.add(new Label("myWallLabel", new ResourceModel("privacy.mywall")));
		DropDownChoice myWallChoice = new DropDownChoice("myWall", dropDownModelRelaxed, new HashMapChoiceRenderer(privacySettingsRelaxed));             
		myWallChoice.setMarkupId("wallprivacyinput");
		myWallChoice.setOutputMarkupId(true);
		myWallContainer.add(myWallChoice);
		myWallContainer.add(new IconWithClueTip("myWallToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.mywall.tooltip")));
		form.add(myWallContainer);

		myWallChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		myWallContainer.setVisible(sakaiProxy.isWallEnabledGlobally());

		
		// online status privacy
		WebMarkupContainer onlineStatusContainer = new WebMarkupContainer("onlineStatusContainer");
		onlineStatusContainer.add(new Label("onlineStatusLabel", new ResourceModel("privacy.onlinestatus")));
		DropDownChoice onlineStatusChoice = new DropDownChoice("onlineStatus", dropDownModelRelaxed, new HashMapChoiceRenderer(privacySettingsRelaxed));             
		onlineStatusChoice.setMarkupId("onlinestatusprivacyinput");
		onlineStatusChoice.setOutputMarkupId(true);
		onlineStatusContainer.add(onlineStatusChoice);
		onlineStatusContainer.add(new IconWithClueTip("onlineStatusToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.onlinestatus.tooltip")));
		form.add(onlineStatusContainer);

		onlineStatusChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavaScript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		onlineStatusContainer.setVisible(sakaiProxy.isOnlineStatusEnabledGlobally());
		
		//submit button
		IndicatingAjaxButton submitButton = new IndicatingAjaxButton("submit", form) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show feedback. perhaps redirect back to main page after a short while?
				if(save(form)){
					formFeedback.setDefaultModel(new ResourceModel("success.privacy.save.ok"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("success")));
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PRIVACY_UPDATE, "/profile/"+userUuid, true);

				} else {
					formFeedback.setDefaultModel(new ResourceModel("error.privacy.save.failed"));
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
		submitButton.setOutputMarkupId(true);
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
		
		if(!sakaiProxy.isPrivacyChangeAllowedGlobally()){
			infoLocked.setDefaultModel(new ResourceModel("text.privacy.cannot.modify"));
			infoLocked.setVisible(true);

			profileImageChoice.setEnabled(false);
			basicInfoChoice.setEnabled(false);
			contactInfoChoice.setEnabled(false);
			studentInfoChoice.setEnabled(false);
			businessInfoChoice.setEnabled(false);
			personalInfoChoice.setEnabled(false);
			birthYearCheckbox.setEnabled(false);
			myFriendsChoice.setEnabled(false);
			myStatusChoice.setEnabled(false);
			myPicturesChoice.setEnabled(false);
			messagesChoice.setEnabled(false);
			myWallChoice.setEnabled(false);
			onlineStatusChoice.setEnabled(false);
			
			submitButton.setEnabled(false);
			submitButton.setVisible(false);
			
			form.setEnabled(false);
		}
        
        add(form);
	}
	
	
	//called when the form is to be saved
	private boolean save(Form<ProfilePrivacy> form) {
		
		//get the backing model - its elems have been updated with the form params
		ProfilePrivacy profilePrivacy = (ProfilePrivacy) form.getModelObject();

		if(privacyLogic.savePrivacyRecord(profilePrivacy)) {
			log.info("Saved ProfilePrivacy for: " + profilePrivacy.getUserUuid());
			return true;
		} else {
			log.info("Couldn't save ProfilePrivacy for: " + profilePrivacy.getUserUuid());
			return false;
		}
	
	}
	
}



