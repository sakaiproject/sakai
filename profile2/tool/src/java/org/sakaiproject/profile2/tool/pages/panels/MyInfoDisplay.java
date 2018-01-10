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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

@Slf4j
public class MyInfoDisplay extends Panel {

	private static final long serialVersionUID = 1L;
	private int visibleFieldCount = 0;
	private String birthday = ""; 
	private String birthdayDisplay = "";
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	private ProfilePrivacyLogic privacyLogic;
	
	
	public MyInfoDisplay(final String id, final UserProfile userProfile) {
		super(id);
		
		log.debug("MyInfoDisplay()");
		
		//this panel stuff
		final Component thisPanel = this;
		
		
		
		//get userId of this profile
		String userId = userProfile.getUserUuid();
		
		//get info from userProfile since we need to validate it and turn things off if not set.
		//otherwise we could just use a propertymodel
		/*
		String firstName = userProfile.getFirstName();
		String middleName = userProfile.getMiddleName();
		String lastName = userProfile.getLastName();
		*/
		String nickname = userProfile.getNickname();
		String personalSummary = userProfile.getPersonalSummary();
		
		Date dateOfBirth = userProfile.getDateOfBirth();
		if(dateOfBirth != null) {
			
			//full value contains year regardless of privacy settings
			// Passing null as the format parameter forces a user locale based format
			birthday = ProfileUtils.convertDateToString(dateOfBirth, null);
			
			//get privacy on display of birthday year and format accordingly
			//note that this particular method doesn't need the second userId param but we send for completeness
			if(privacyLogic.isBirthYearVisible(userId)) {
				birthdayDisplay = birthday;
			} else {
				birthdayDisplay = ProfileUtils.convertDateToString(dateOfBirth, ProfileConstants.DEFAULT_DATE_FORMAT_HIDE_YEAR);
			}
			
			//set both values as they are used differently
			userProfile.setBirthdayDisplay(birthdayDisplay);
			userProfile.setBirthday(birthday);

		}
		
		//heading
		add(new Label("heading", new ResourceModel("heading.basic")));
		
		//firstName
		/*
		WebMarkupContainer firstNameContainer = new WebMarkupContainer("firstNameContainer");
		firstNameContainer.add(new Label("firstNameLabel", new ResourceModel("profile.name.first")));
		firstNameContainer.add(new Label("firstName", firstName));
		add(firstNameContainer);
		if(StringUtils.isBlank(firstName)) {
			firstNameContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		*/
		
		//middleName
		/*
		WebMarkupContainer middleNameContainer = new WebMarkupContainer("middleNameContainer");
		middleNameContainer.add(new Label("middleNameLabel", new ResourceModel("profile.name.middle")));
		middleNameContainer.add(new Label("middleName", middleName));
		add(middleNameContainer);
		if(StringUtils.isBlank(middleName)) {
			middleNameContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		*/
		
		//lastName
		/*
		WebMarkupContainer lastNameContainer = new WebMarkupContainer("lastNameContainer");
		lastNameContainer.add(new Label("lastNameLabel", new ResourceModel("profile.name.last")));
		lastNameContainer.add(new Label("lastName", lastName));
		add(lastNameContainer);
		if(StringUtils.isBlank(lastName)) {
			lastNameContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		*/
		
		
		//nickname
		WebMarkupContainer nicknameContainer = new WebMarkupContainer("nicknameContainer");
		nicknameContainer.add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
		nicknameContainer.add(new Label("nickname", nickname));
		add(nicknameContainer);
		if(StringUtils.isBlank(nickname)) {
			nicknameContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//birthday
		WebMarkupContainer birthdayContainer = new WebMarkupContainer("birthdayContainer");
		birthdayContainer.add(new Label("birthdayLabel", new ResourceModel("profile.birthday")));
		birthdayContainer.add(new Label("birthday", birthdayDisplay));
		add(birthdayContainer);
		if(StringUtils.isBlank(birthdayDisplay)) {
			birthdayContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}

		//personal summary
		WebMarkupContainer personalSummaryContainer = new WebMarkupContainer("personalSummaryContainer");
		personalSummaryContainer.add(new Label("personalSummaryLabel", new ResourceModel("profile.summary")));
		personalSummaryContainer.add(new Label("personalSummary", ProfileUtils.processHtml(personalSummary)).setEscapeModelStrings(false));
		add(personalSummaryContainer);
		if(StringUtils.isBlank(personalSummary)) {
			personalSummaryContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//edit button
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton", new ResourceModel("button.edit")) {
			
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				Component newPanel = new MyInfoEdit(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.add(newPanel);
					//resize iframe
					target.appendJavaScript("setMainFrameHeight(window.name);");
				}
			}
						
		};
		editButton.add(new Label("editButtonLabel", new ResourceModel("button.edit")));
		editButton.setOutputMarkupId(true);
		
		if(userProfile.isLocked() && !sakaiProxy.isSuperUser()) {
			editButton.setVisible(false);
		}
		
		add(editButton);
		
		//no fields message
		Label noFieldsMessage = new Label("noFieldsMessage", new ResourceModel("text.no.fields"));
		add(noFieldsMessage);
		if(visibleFieldCount > 0) {
			noFieldsMessage.setVisible(false);
		}
	}
	
}
