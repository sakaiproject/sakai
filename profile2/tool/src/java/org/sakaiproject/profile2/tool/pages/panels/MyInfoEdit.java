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
package org.sakaiproject.profile2.tool.pages.panels;



import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.components.IconWithClueTip;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;


public class MyInfoEdit extends Panel {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(MyInfoEdit.class);
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
	public MyInfoEdit(final String id, final UserProfile userProfile) {
		super(id);
		
        log.debug("MyInfoEdit()");

       
		//this panel
		final Component thisPanel = this;
		
		//get userId
		final String userId = userProfile.getUserUuid();
		
		
		//updates back to Account for some fields allowed?
		//boolean updateAllowed = sakaiProxy.isAccountUpdateAllowed(userId);
		
		//heading
		add(new Label("heading", new ResourceModel("heading.basic.edit")));
				
		//setup form		
		Form form = new Form("form", new Model(userProfile));
		form.setOutputMarkupId(true);
		
		//form submit feedback
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		form.add(formFeedback);
		
		//add warning message if superUser and not editing own profile
		Label editWarning = new Label("editWarning");
		editWarning.setVisible(false);
		if(sakaiProxy.isSuperUserAndProxiedToUser(userId)) {
			editWarning.setDefaultModel(new StringResourceModel("text.edit.other.warning", null, new Object[]{ userProfile.getDisplayName() } ));
			editWarning.setEscapeModelStrings(false);
			editWarning.setVisible(true);
		}
		form.add(editWarning);
		
		//We don't need to get the info from userProfile, we load it into the form with a property model
	    //just make sure that the form element id's match those in the model
	   		
		//firstName
		/*
		WebMarkupContainer firstNameContainer = new WebMarkupContainer("firstNameContainer");
		firstNameContainer.add(new Label("firstNameLabel", new ResourceModel("profile.name.first")));
		TextField firstName = new TextField("firstName", new PropertyModel(userProfile, "firstName"));
		//readonly view
		Label firstNameReadOnly = new Label("firstNameReadOnly", new PropertyModel(userProfile, "firstName"));
		if(updateAllowed) {
			firstNameReadOnly.setVisible(false);
		} else {
			firstName.setVisible(false);
		}
		firstNameContainer.add(firstName);
		firstNameContainer.add(firstNameReadOnly);
		form.add(firstNameContainer);
		*/
		
		//middleName
		/*
		WebMarkupContainer middleNameContainer = new WebMarkupContainer("middleNameContainer");
		middleNameContainer.add(new Label("middleNameLabel", new ResourceModel("profile.name.middle")));
		TextField middleName = new TextField("middleName", new PropertyModel(userProfile, "middleName"));
		middleNameContainer.add(middleName);
		form.add(middleNameContainer);
		*/
		
		//lastName
		/*
		WebMarkupContainer lastNameContainer = new WebMarkupContainer("lastNameContainer");
		lastNameContainer.add(new Label("lastNameLabel", new ResourceModel("profile.name.last")));
		TextField lastName = new TextField("lastName", new PropertyModel(userProfile, "lastName"));
		//readonly view
		Label lastNameReadOnly = new Label("lastNameReadOnly", new PropertyModel(userProfile, "lastName"));
		if(updateAllowed) {
			lastNameReadOnly.setVisible(false);
		} else {
			lastName.setVisible(false);
		}
		lastNameContainer.add(lastName);
		lastNameContainer.add(lastNameReadOnly);
		form.add(lastNameContainer);
		*/
		
		//nickname
		WebMarkupContainer nicknameContainer = new WebMarkupContainer("nicknameContainer");
		nicknameContainer.add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
		TextField nickname = new TextField("nickname", new PropertyModel(userProfile, "nickname"));
		nickname.setMarkupId("nicknameinput");
		nickname.setOutputMarkupId(true);
		nicknameContainer.add(nickname);
		form.add(nicknameContainer);
		
		//birthday
		WebMarkupContainer birthdayContainer = new WebMarkupContainer("birthdayContainer");
		birthdayContainer.add(new Label("birthdayLabel", new ResourceModel("profile.birthday")));
		TextField birthday = new TextField("birthday", new PropertyModel(userProfile, "birthday"));
		birthday.setMarkupId("birthdayinput");
		birthday.setOutputMarkupId(true);
		birthdayContainer.add(birthday);
		//tooltip
		birthdayContainer.add(new IconWithClueTip("birthdayToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.profile.birthyear.tooltip")));
		form.add(birthdayContainer);

		//personal summary
		WebMarkupContainer personalSummaryContainer = new WebMarkupContainer("personalSummaryContainer");
		personalSummaryContainer.add(new Label("personalSummaryLabel", new ResourceModel("profile.summary")));
		TextArea personalSummary = new TextArea("personalSummary", new PropertyModel(userProfile, "personalSummary"));
		personalSummary.setMarkupId("summaryinput");
		//personalSummary.setEditorConfig(CKEditorConfig.createCkConfig());
		personalSummary.setOutputMarkupId(true);		
		personalSummaryContainer.add(personalSummary);
		form.add(personalSummaryContainer);
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", form) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show message, then load display panel

				if(save(form)) {
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_INFO_UPDATE, "/profile/"+userId, true);
					
					//post to wall if enabled
					if (true == sakaiProxy.isWallEnabledGlobally() && false == sakaiProxy.isSuperUserAndProxiedToUser(userId)) {
						wallLogic.addNewEventToWall(ProfileConstants.EVENT_PROFILE_INFO_UPDATE, sakaiProxy.getCurrentUserId());
					}
					
					//repaint panel
					Component newPanel = new MyInfoDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					thisPanel.replaceWith(newPanel);
					if(target != null) {
						target.add(newPanel);
						//resize iframe
						target.appendJavaScript("setMainFrameHeight(window.name);");
					}
				
				} else {
					//String js = "alert('Failed to save information. Contact your system administrator.');";
					//target.prependJavascript(js);
					
					formFeedback.setDefaultModel(new ResourceModel("error.profile.save.info.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("save-failed-error")));	
					target.add(formFeedback);
				}
				
            }
			
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes){
			    super.updateAjaxAttributes(attributes);
			    AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
			        @Override
			        public CharSequence getBeforeHandler(Component component) {
			        	return "doUpdateCK()";
			        }
			    };
			    attributes.getAjaxCallListeners().add(myAjaxCallListener);
			}
			
		};
		submitButton.setModel(new ResourceModel("button.save.changes"));
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
            	Component newPanel = new MyInfoDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.add(newPanel);
					//resize iframe
					target.appendJavaScript("setMainFrameHeight(window.name);");
				}
            	
            }
			
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
		
        //feedback stuff - make this a class and instance it with diff params
        //WebMarkupContainer formFeedback = new WebMarkupContainer("formFeedback");
		//formFeedback.add(new Label("feedbackMsg", "some message"));
		//formFeedback.add(new AjaxIndicator("feedbackImg"));
		//form.add(formFeedback);
        
        
		
		//add form to page
		add(form);
		
	}
	
	//called when the form is to be saved
	private boolean save(Form form) {
		

		//get the backing model
		UserProfile userProfile = (UserProfile) form.getModelObject();
		
		//get userId from the UserProfile (because admin could be editing), then get existing SakaiPerson for that userId
		String userId = userProfile.getUserUuid();
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
	
		//set the attributes from userProfile that this form dealt with, into sakaiPerson
		//this WILL fail if there is no sakaiPerson for the user however this should have been caught already
		//as a new Sakaiperson for a user is created in MyProfile.java if they don't have one.
		
		//TODO should we set these up as strings and clean them first?
		
		//sakaiPerson.setInitials(userProfile.getMiddleName());
		String tNickname = ProfileUtils.truncate(userProfile.getNickname(), 255, false);
		userProfile.setNickname(tNickname); //update form model
		sakaiPerson.setNickname(tNickname);
		
		if(StringUtils.isNotBlank(userProfile.getBirthday())) {
			Date convertedDate = ProfileUtils.convertStringToDate(userProfile.getBirthday(), ProfileConstants.DEFAULT_DATE_FORMAT);
			userProfile.setDateOfBirth(convertedDate); //set in userProfile which backs the profile
			sakaiPerson.setDateOfBirth(convertedDate); //set into sakaiPerson to be persisted to DB
		} else {
			userProfile.setDateOfBirth(null); //clear both fields
			sakaiPerson.setDateOfBirth(null);
		}

		//PRFL-467 store as given, and process when it is retrieved.
		sakaiPerson.setNotes(userProfile.getPersonalSummary());
		
		if(profileLogic.saveUserProfile(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userId);
			
			//update their name details in their account if allowed
			/*
			if(sakaiProxy.isAccountUpdateAllowed(userId)) {
				sakaiProxy.updateNameForUser(userId, userProfile.getFirstName(), userProfile.getLastName());
			
				//now update displayName in UserProfile
				userProfile.setDisplayName(sakaiProxy.getUserDisplayName(userId));
			
			}
			*/
			
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userId);
			return false;
		}
	}
	
}
