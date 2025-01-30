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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.components.IconWithToolTip;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyInfoEdit extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;

    @SpringBean(name="org.sakaiproject.component.api.ServerConfigurationService")
    private ServerConfigurationService serverConfigurationService;
	
	public MyInfoEdit(final String id, final UserProfile userProfile) {
		super(id);
		
        log.debug("MyInfoEdit()");
       
		final Component thisPanel = this;
		
		final String userId = userProfile.getUserUuid();
		
		//heading
		add(new Label("heading", new ResourceModel("heading.basic.edit")));
				
		//setup form		
		Form<UserProfile> form = new Form<>("form", new Model<>(userProfile));
		form.setOutputMarkupId(true);
		
		//form submit feedback
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		form.add(formFeedback);
		
		//add warning message if superUser and not editing own profile
		Label editWarning = new Label("editWarning");
		editWarning.setVisible(false);
		if(sakaiProxy.isSuperUserAndProxiedToUser(userId)) {
			editWarning.setDefaultModel(new StringResourceModel("text.edit.other.warning").setParameters(userProfile.getDisplayName()));
			editWarning.setEscapeModelStrings(false);
			editWarning.setVisible(true);
		}
		form.add(editWarning);
		
		//nickname
		WebMarkupContainer nicknameContainer = new WebMarkupContainer("nicknameContainer");
		nicknameContainer.add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
		TextField<String> nickname = new TextField<>("nickname", new PropertyModel<>(userProfile, "nickname"));
		nickname.setMarkupId("nicknameinput");
		nickname.setOutputMarkupId(true);
		nicknameContainer.add(nickname);
		form.add(nicknameContainer);

        ResourceLoader messages = new ResourceLoader("ProfileApplication");

		List<String> pronounOptions
            = Stream.of(messages.getString("profile.pronouns.options").split(",")).map(String::trim)
                .collect(Collectors.toList());

        pronounOptions.add(messages.getString("profile.pronouns.usemyname"));
        String enterMyOwn = messages.getString("profile.pronouns.entermyown");
        pronounOptions.add(enterMyOwn);
        pronounOptions.add(messages.getString("profile.pronouns.prefernottosay"));
        String pronounsUnknown = messages.getString("profile.pronouns.unknown");
        pronounOptions.add(pronounsUnknown);
        boolean ownEntered = false;
        if (pronounOptions.contains(userProfile.getPronouns())) {
            userProfile.setPronounsSelected(userProfile.getPronouns());
        } else if (StringUtils.isNotBlank(userProfile.getPronouns())) {
            userProfile.setPronounsInput(userProfile.getPronouns());
            userProfile.setPronounsSelected(enterMyOwn);
            ownEntered = true;
        } else {
            userProfile.setPronounsSelected(pronounsUnknown);
        }

        WebMarkupContainer pronounsContainer = new WebMarkupContainer("pronounsContainer");
        pronounsContainer.add(new Label("pronounsLabel", new ResourceModel("profile.pronouns")));
        DropDownChoice<String> pronounsSelect = new DropDownChoice<>("pronounsSelect", new PropertyModel<>(userProfile, "pronounsSelected"), pronounOptions);
        pronounsSelect.setOutputMarkupId(true);
        pronounsSelect.add(new AttributeAppender("data-entermyown", new Model<String>(enterMyOwn)));
        pronounsContainer.add(pronounsSelect);
        TextField<String> pronouns = new TextField<>("pronounsInput", new PropertyModel<>(userProfile, "pronounsInput"));
        pronouns.setOutputMarkupId(true);
        if (ownEntered) {
            pronouns.add(new AttributeAppender("style", new Model<String>("display: inline !important;")));
        }
        pronounsContainer.add(pronouns);
        pronounsContainer.setVisible(serverConfigurationService.getBoolean("profile2.profile.pronouns.enabled", true));
        form.add(pronounsContainer);
		
		//personal summary
		WebMarkupContainer personalSummaryContainer = new WebMarkupContainer("personalSummaryContainer");
		personalSummaryContainer.add(new Label("personalSummaryLabel", new ResourceModel("profile.summary")));
		TextArea<String> personalSummary = new TextArea<>("personalSummary", new PropertyModel<>(userProfile, "personalSummary"));
		personalSummary.setMarkupId("summaryinput");
		//personalSummary.setEditorConfig(CKEditorConfig.createCkConfig());
		personalSummary.setOutputMarkupId(true);		
		personalSummaryContainer.add(personalSummary);
		form.add(personalSummaryContainer);
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", form) {
			@Override
			protected void onSubmit(Optional<AjaxRequestTarget> targetOptional) {
				//save() form, show message, then load display panel

				if(save(form)) {
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_INFO_UPDATE, "/profile/"+userId, true);
					
					//repaint panel
					Component newPanel = new MyInfoDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					thisPanel.replaceWith(newPanel);
					targetOptional.ifPresent(target -> {
						target.add(newPanel);
						//resize iframe
						target.appendJavaScript("setMainFrameHeight(window.name);");
					});
				
				} else {
					targetOptional.ifPresent(target -> {
						formFeedback.setDefaultModel(new ResourceModel("error.profile.save.info.failed"));
						formFeedback.add(new AttributeModifier("class", new Model<String>("save-failed-error")));
						target.add(formFeedback);
					});
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

			@Override
			protected void onSubmit(Optional<AjaxRequestTarget> targetOptional) {
            	Component newPanel = new MyInfoDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				targetOptional.ifPresent(target -> {
					target.add(newPanel);
					//resize iframe
					target.appendJavaScript("setMainFrameHeight(window.name);");
				});
            	
            }
			
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
		
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

        String pronounsInput = userProfile.getPronounsInput();
        if (StringUtils.isNotBlank(pronounsInput)) {
            sakaiPerson.setPronouns(pronounsInput);
        } else {
            sakaiPerson.setPronouns(userProfile.getPronounsSelected());
        }
        userProfile.setPronouns(sakaiPerson.getPronouns());
		
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
            log.info("Couldn't save SakaiPerson for: {}", userId);
			return false;
		}
	}
	
}
