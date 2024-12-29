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

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.exception.ProfilePrototypeNotDefinedException;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Container for viewing the profile of someone else.
 */
@Slf4j
public class ViewProfilePanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	protected ProfileLogic profileLogic;
	
	public ViewProfilePanel(final String id, final String userUuid, final String currentUserId) {
		
		super(id);
		
		//get SakaiPerson for the person who's profile we are viewing
		//SakaiPerson returns NULL strings if value is not set, not blank ones
		final SakaiPerson sakaiPerson;
		//if null, they have no profile so just get a prototype
		if(sakaiProxy.getSakaiPerson(userUuid) == null) {
			log.info("No SakaiPerson for " + userUuid);
			sakaiPerson = sakaiProxy.getSakaiPersonPrototype();
			//if its still null, throw exception
			if(sakaiPerson == null) {
				throw new ProfilePrototypeNotDefinedException("Couldn't create a SakaiPerson prototype for " + userUuid);
			}
		} else {
			sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		}
		
		//holds number of profile containers that are visible
		int visibleContainerCount = 0;

		/* BASIC INFO */
		WebMarkupContainer basicInfoContainer = new WebMarkupContainer("mainSectionContainer_basic");
		basicInfoContainer.setOutputMarkupId(true);
		
		//get info
		String nickname = sakaiPerson.getNickname();
		String personalSummary = sakaiPerson.getNotes();
		
		//heading
		basicInfoContainer.add(new Label("mainSectionHeading_basic", new ResourceModel("heading.basic")));

        int visibleFieldCount_basic = 0;
		
		//nickname
		WebMarkupContainer nicknameContainer = new WebMarkupContainer("nicknameContainer");
		nicknameContainer.add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
		nicknameContainer.add(new Label("nickname", nickname));
		basicInfoContainer.add(nicknameContainer);
		if (StringUtils.isBlank(nickname)) {
			nicknameContainer.setVisible(false);
		} else {
			visibleFieldCount_basic++;
		}
		
		//personal summary
		WebMarkupContainer personalSummaryContainer = new WebMarkupContainer("personalSummaryContainer");
		personalSummaryContainer.add(new Label("personalSummaryLabel", new ResourceModel("profile.summary")));
		personalSummaryContainer.add(new Label("personalSummary", ProfileUtils.processHtml(personalSummary)).setEscapeModelStrings(false));
		basicInfoContainer.add(personalSummaryContainer);
		if(StringUtils.isBlank(personalSummary)) {
			personalSummaryContainer.setVisible(false);
		} else {
			visibleFieldCount_basic++;
		}
		
		//name pronunciation
		WebMarkupContainer namePronunciationContainer = new WebMarkupContainer("namePronunciationContainer");
		namePronunciationContainer.add(new Label("namePronunciationLabel", new ResourceModel("heading.name.pronunciation")));
		if (sakaiProxy.isNamePronunciationProfileEnabled()) {
			UserProfile userProfile = profileLogic.getUserProfile(userUuid);
			namePronunciationContainer.add(new MyNamePronunciationDisplay("namePronunciationDisplay", userProfile));
			basicInfoContainer.add(namePronunciationContainer);
		} else {
			namePronunciationContainer.setVisible(false);
			basicInfoContainer.add(namePronunciationContainer);
		}
		
		add(basicInfoContainer);
		
		//if nothing/not allowed, hide whole panel
		if(visibleFieldCount_basic == 0) {
			basicInfoContainer.setVisible(false);
		} else {
			visibleContainerCount++;
		}
		
		/* CONTACT INFO */
		WebMarkupContainer contactInfoContainer = new WebMarkupContainer("mainSectionContainer_contact");
		contactInfoContainer.setOutputMarkupId(true);
		
		//get info
		String email = sakaiProxy.getUserEmail(userUuid); //must come from SakaiProxy
		String mobilephone = sakaiPerson.getMobile();
		
		int visibleFieldCount_contact = 0;
		
		//heading
		contactInfoContainer.add(new Label("mainSectionHeading_contact", new ResourceModel("heading.contact")));
		
		//email
		WebMarkupContainer emailContainer = new WebMarkupContainer("emailContainer");
		emailContainer.add(new Label("emailLabel", new ResourceModel("profile.email")));
		emailContainer.add(new Label("email", email));
		contactInfoContainer.add(emailContainer);
		if(StringUtils.isBlank(email)) {
			emailContainer.setVisible(false);
		} else {
			visibleFieldCount_contact++;
		}
		
		//mobile phone
		WebMarkupContainer mobilephoneContainer = new WebMarkupContainer("mobilephoneContainer");
		mobilephoneContainer.add(new Label("mobilephoneLabel", new ResourceModel("profile.phone.mobile")));
		mobilephoneContainer.add(new Label("mobilephone", mobilephone));
		contactInfoContainer.add(mobilephoneContainer);
		if(StringUtils.isBlank(mobilephone)) {
			mobilephoneContainer.setVisible(false);
		} else {
			visibleFieldCount_contact++;
		}
		
		add(contactInfoContainer);
		
		//if nothing/not allowed, hide whole panel
		if(visibleFieldCount_contact == 0) {
			contactInfoContainer.setVisible(false);
		} else {
			visibleContainerCount++;
		}
		
		/* SOCIAL NETWORKING */
		WebMarkupContainer socialNetworkingInfoContainer = new WebMarkupContainer("mainSectionContainer_socialNetworking");
		socialNetworkingInfoContainer.setOutputMarkupId(true);
		
		//heading
		socialNetworkingInfoContainer.add(new Label("mainSectionHeading_socialNetworking", new ResourceModel("heading.social")));
		
		SocialNetworkingInfo socialNetworkingInfo = profileLogic.getSocialNetworkingInfo(userUuid);
		if (null == socialNetworkingInfo) {
			socialNetworkingInfo = profileLogic.getSocialNetworkingInfo(userUuid);
		}
		String facebookUsername = socialNetworkingInfo.getFacebookUrl();
		String linkedinUsername = socialNetworkingInfo.getLinkedinUrl();
		String instagramUsername = socialNetworkingInfo.getInstagramUrl();

		int visibleFieldCount_socialNetworking = 0;
		
		//facebook
		WebMarkupContainer facebookContainer = new WebMarkupContainer("facebookContainer");
		facebookContainer.add(new Label("facebookLabel", new ResourceModel("profile.socialnetworking.facebook")));
		facebookContainer.add(new ExternalLink("facebookLink", facebookUsername, facebookUsername));
		socialNetworkingInfoContainer.add(facebookContainer);
		if(StringUtils.isBlank(facebookUsername)) {
			facebookContainer.setVisible(false);
		} else {
			visibleFieldCount_socialNetworking++;
		}
		
		//linkedin
		WebMarkupContainer linkedinContainer = new WebMarkupContainer("linkedinContainer");
		linkedinContainer.add(new Label("linkedinLabel", new ResourceModel("profile.socialnetworking.linkedin")));
		linkedinContainer.add(new ExternalLink("linkedinLink", linkedinUsername, linkedinUsername));
		socialNetworkingInfoContainer.add(linkedinContainer);
		if(StringUtils.isBlank(linkedinUsername)) {
			linkedinContainer.setVisible(false);
		} else {
			visibleFieldCount_socialNetworking++;
		}
		
		//Instagram
		WebMarkupContainer instagramContainer = new WebMarkupContainer("instagramContainer");
				instagramContainer.add(new Label("instagramLabel", new ResourceModel("profile.socialnetworking.instagram")));
				instagramContainer.add(new ExternalLink("instagramLink", instagramUsername, instagramUsername));
				socialNetworkingInfoContainer.add(instagramContainer);
				if(StringUtils.isBlank(instagramUsername)) {
					instagramContainer.setVisible(false);
				} else {
					visibleFieldCount_socialNetworking++;
				}
				
		add(socialNetworkingInfoContainer);
		
		//if nothing/not allowed, hide whole panel
		if(visibleFieldCount_socialNetworking == 0) {
			socialNetworkingInfoContainer.setVisible(false);
		} else {
			visibleContainerCount++;
		}
		
		/* NO INFO VISIBLE MESSAGE (hide if some visible) */
		Label noContainersVisible = new Label ("noContainersVisible", new ResourceModel("text.view.profile.nothing"));
		noContainersVisible.setOutputMarkupId(true);
		add(noContainersVisible);
		
		if(visibleContainerCount > 0) {
			noContainersVisible.setVisible(false);
		}
	}
}
