package org.sakaiproject.profile2.tool.pages;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
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


public class MyPrivacy extends BasePage {

	private static final Logger log = Logger.getLogger(MyPrivacy.class);
	private transient ProfilePrivacy profilePrivacy;
		
	public MyPrivacy() {
		
		log.debug("MyPrivacy()");

		//get current user
		final String userId = sakaiProxy.getCurrentUserId();

		//get the privacy object for this user from the database
		profilePrivacy = profileLogic.getPrivacyRecordForUser(userId);
		
		//if null, create one
		if(profilePrivacy == null) {
			profilePrivacy = profileLogic.createDefaultPrivacyRecord(userId);
			//if its still null, throw exception
			
			if(profilePrivacy == null) {
				throw new ProfilePrivacyNotDefinedException("Couldn't create default privacy record for " + userId);
			}
			
			//post create event
			sakaiProxy.postEvent(ProfileConstants.EVENT_PRIVACY_NEW, "/profile/"+userId, true);
			
		}
		
		Label heading = new Label("heading", new ResourceModel("heading.privacy"));
		add(heading);
		
		//feedback for form submit action
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		final String formFeedbackId = formFeedback.getMarkupId();
		add(formFeedback);
		
		
		
		//create model
		CompoundPropertyModel privacyModel = new CompoundPropertyModel(profilePrivacy);
		
		//setup form		
		Form form = new Form("form", privacyModel);
		form.setOutputMarkupId(true);
		
		
		
		//setup LinkedHashMap of privacy options for strict things
		final LinkedHashMap<String, String> privacySettingsStrict = new LinkedHashMap<String, String>();
		privacySettingsStrict.put("0", new StringResourceModel("privacy.option.everyone", this,null).getString());
		privacySettingsStrict.put("1", new StringResourceModel("privacy.option.onlyfriends", this,null).getString());
		privacySettingsStrict.put("2", new StringResourceModel("privacy.option.onlyme", this,null).getString());
		
		//model that wraps our options
		IModel dropDownModelStrict = new Model() {
			public Object getObject() {
				 return new ArrayList(privacySettingsStrict.keySet());
			} 
		};
		
		//setup LinkedHashMap of privacy options for more relaxed things
		final LinkedHashMap<String, String> privacySettingsRelaxed = new LinkedHashMap<String, String>();
		privacySettingsRelaxed.put("0", new StringResourceModel("privacy.option.everyone", this,null).getString());
		privacySettingsRelaxed.put("1", new StringResourceModel("privacy.option.onlyfriends", this,null).getString());
		
		//model that wraps our options
		IModel dropDownModelRelaxed = new Model() {
			public Object getObject() {
				 return new ArrayList(privacySettingsRelaxed.keySet());
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
		profileImageContainer.add(profileImageChoice);
		//tooltip
		profileImageContainer.add(new IconWithClueTip("profileImageToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.profileimage.tooltip")));
		form.add(profileImageContainer);
		//updater
		profileImageChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		
		
		//basicInfo privacy
		WebMarkupContainer basicInfoContainer = new WebMarkupContainer("basicInfoContainer");
		basicInfoContainer.add(new Label("basicInfoLabel", new ResourceModel("privacy.basicinfo")));
		DropDownChoice basicInfoChoice = new DropDownChoice("basicInfo", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		basicInfoContainer.add(basicInfoChoice);
		//tooltip
		basicInfoContainer.add(new IconWithClueTip("basicInfoToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.basicinfo.tooltip")));
		form.add(basicInfoContainer);
		//updater
		basicInfoChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//contactInfo privacy
		WebMarkupContainer contactInfoContainer = new WebMarkupContainer("contactInfoContainer");
		contactInfoContainer.add(new Label("contactInfoLabel", new ResourceModel("privacy.contactinfo")));
		DropDownChoice contactInfoChoice = new DropDownChoice("contactInfo", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		contactInfoContainer.add(contactInfoChoice);
		//tooltip
		contactInfoContainer.add(new IconWithClueTip("contactInfoToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.contactinfo.tooltip")));
		form.add(contactInfoContainer);
		//updater
		contactInfoChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//academicInfo privacy
		WebMarkupContainer academicInfoContainer = new WebMarkupContainer("academicInfoContainer");
		academicInfoContainer.add(new Label("academicInfoLabel", new ResourceModel("privacy.academicinfo")));
		DropDownChoice academicInfoChoice = new DropDownChoice("academicInfo", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		academicInfoContainer.add(academicInfoChoice);
		//tooltip
		academicInfoContainer.add(new IconWithClueTip("academicInfoToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.academicinfo.tooltip")));
		form.add(academicInfoContainer);
		//updater
		academicInfoChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//personalInfo privacy
		WebMarkupContainer personalInfoContainer = new WebMarkupContainer("personalInfoContainer");
		personalInfoContainer.add(new Label("personalInfoLabel", new ResourceModel("privacy.personalinfo")));
		DropDownChoice personalInfoChoice = new DropDownChoice("personalInfo", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		personalInfoContainer.add(personalInfoChoice);
		//tooltip
		personalInfoContainer.add(new IconWithClueTip("personalInfoToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.personalinfo.tooltip")));
		form.add(personalInfoContainer);
		//updater
		personalInfoChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//birthYear privacy
		WebMarkupContainer birthYearContainer = new WebMarkupContainer("birthYearContainer");
		birthYearContainer.add(new Label("birthYearLabel", new ResourceModel("privacy.birthyear")));
		CheckBox birthYearCheckbox = new CheckBox("birthYear", new PropertyModel(privacyModel, "showBirthYear"));
		birthYearContainer.add(birthYearCheckbox);
		//tooltip
		birthYearContainer.add(new IconWithClueTip("birthYearToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.birthyear.tooltip")));
		form.add(birthYearContainer);
		//updater
		birthYearCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//search privacy
		WebMarkupContainer searchContainer = new WebMarkupContainer("searchContainer");
		searchContainer.add(new Label("searchLabel", new ResourceModel("privacy.search")));
		DropDownChoice searchChoice = new DropDownChoice("search", dropDownModelRelaxed, new HashMapChoiceRenderer(privacySettingsRelaxed));
		searchContainer.add(searchChoice);
		//tooltip
		searchContainer.add(new IconWithClueTip("searchToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.search.tooltip")));
		form.add(searchContainer);
		//updater
		searchChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		
		//myFriends privacy
		WebMarkupContainer myFriendsContainer = new WebMarkupContainer("myFriendsContainer");
		myFriendsContainer.add(new Label("myFriendsLabel", new ResourceModel("privacy.myfriends")));
		DropDownChoice myFriendsChoice = new DropDownChoice("myFriends", dropDownModelStrict, new HashMapChoiceRenderer(privacySettingsStrict));
		myFriendsContainer.add(myFriendsChoice);
		//tooltip
		myFriendsContainer.add(new IconWithClueTip("myFriendsToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.myfriends.tooltip")));
		form.add(myFriendsContainer);
		//updater
		myFriendsChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		//myStatus privacy
		WebMarkupContainer myStatusContainer = new WebMarkupContainer("myStatusContainer");
		myStatusContainer.add(new Label("myStatusLabel", new ResourceModel("privacy.mystatus")));
		DropDownChoice myStatusChoice = new DropDownChoice("myStatus", dropDownModelRelaxed, new HashMapChoiceRenderer(privacySettingsRelaxed));
		myStatusContainer.add(myStatusChoice);
		//tooltip
		myStatusContainer.add(new IconWithClueTip("myStatusToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.privacy.mystatus.tooltip")));
		form.add(myStatusContainer);
		//updater
		myStatusChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	target.appendJavascript("$('#" + formFeedbackId + "').fadeOut();");
            }
        });
		
		
		
		//submit button
		IndicatingAjaxButton submitButton = new IndicatingAjaxButton("submit", form) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show feedback. perhaps redirect back to main page after a short while?
				if(save(form)){
					formFeedback.setModel(new ResourceModel("success.privacy.save.ok"));
					formFeedback.add(new AttributeModifier("class", true, new Model("success")));
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PRIVACY_UPDATE, "/profile/"+userId, true);

				} else {
					formFeedback.setModel(new ResourceModel("error.privacy.save.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));	
				}
				target.addComponent(formFeedback);
            }
		};
		submitButton.setModel(new ResourceModel("button.save.settings"));
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
		ProfilePrivacy profilePrivacy = (ProfilePrivacy) form.getModelObject();

		if(profileLogic.savePrivacyRecord(profilePrivacy)) {
			log.info("Saved ProfilePrivacy for: " + profilePrivacy.getUserUuid());
			return true;
		} else {
			log.info("Couldn't save ProfilePrivacy for: " + profilePrivacy.getUserUuid());
			return false;
		}
	
	}
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MyPrivacy has been deserialized.");
		//re-init our transient objects
		profileLogic = getProfileLogic();
		sakaiProxy = getSakaiProxy();
	}
	
	
}



