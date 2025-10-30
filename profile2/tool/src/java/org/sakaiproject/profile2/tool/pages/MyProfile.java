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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.exception.ProfileNotDefinedException;
import org.sakaiproject.profile2.model.MyProfilePanelState;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.components.ProfileImage;
import org.sakaiproject.profile2.tool.pages.panels.MyProfilePanel;
import org.sakaiproject.profile2.util.ProfileConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyProfile extends BasePage {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor if viewing own
	 */
	public MyProfile()   {
		log.debug("MyProfile()");

		//get user for this profile and render it
		String userUuid = sakaiProxy.getCurrentUserId();
		renderMyProfile(userUuid);
	}

	/**
	 * This constructor is called if we are viewing someone elses but in edit mode.
	 * This will only be called if we were a superuser editing someone else's profile.
	 * An additional catch is also in place.
	 * @param userUuid		uuid of other user 
	 */
	public MyProfile(final String userUuid)   {
		log.debug("MyProfile(" + userUuid +")");
		
		//double check only super users
		if(!sakaiProxy.isSuperUser()) {
			log.error("MyProfile: user " + sakaiProxy.getCurrentUserId() + " attempted to access MyProfile for " + userUuid + ". Redirecting...");
			throw new RestartResponseException(new MyProfile());
		}
		//render for given user
		renderMyProfile(userUuid);
	}

	/**
	 * Does the actual rendering of the page
	 * @param userUuid
	 */
	private void renderMyProfile(final String userUuid) {

		//don't do this for super users viewing other people's profiles as otherwise there is no way back to own profile
		if (!sakaiProxy.isSuperUserAndProxiedToUser(userUuid)) {
			myProfileLink.setEnabled(false);
			myProfileContainer.add(new AttributeModifier("class", "current"));
		}

		//add the feedback panel for any error messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		add(feedbackPanel);
		feedbackPanel.setVisible(false); //hide by default

		//get SakaiPerson for this user
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		//if null, throw exception
		if(sakaiPerson == null) {
			throw new ProfileNotDefinedException("Couldn't get a SakaiPerson for " + userUuid);
		} 

		//post view event
		sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_VIEW_OWN, "/profile/"+userUuid, false);

		//get some values from SakaiPerson or SakaiProxy if empty
		//SakaiPerson returns NULL strings if value is not set, not blank ones

		//these must come from Account to keep it all in sync
		//we *could* get a User object here and get the values.
		String userDisplayName = sakaiProxy.getUserDisplayName(userUuid);
		/*
		String userFirstName = sakaiProxy.getUserFirstName(userId);
		String userLastName = sakaiProxy.getUserLastName(userId);
		*/

		String userEmail = sakaiProxy.getUserEmail(userUuid);

		//create instance of the UserProfile class
		//we then pass the userProfile in the constructor to the child panels
		final UserProfile userProfile = new UserProfile();

		//get rest of values from SakaiPerson and setup UserProfile
		userProfile.setUserUuid(userUuid);

		userProfile.setNickname(sakaiPerson.getNickname());
		userProfile.setPersonalSummary(sakaiPerson.getNotes());
		userProfile.setDisplayName(userDisplayName);

		userProfile.setEmail(userEmail);
		userProfile.setMobilephone(sakaiPerson.getMobile());

		userProfile.setPhoneticPronunciation(sakaiPerson.getPhoneticPronunciation());
		userProfile.setPronouns(sakaiPerson.getPronouns());

		// social networking fields
		SocialNetworkingInfo socialInfo = profileLogic.getSocialNetworkingInfo(userProfile.getUserUuid());
		if(socialInfo == null){
			socialInfo = new SocialNetworkingInfo();
		}
		userProfile.setSocialInfo(socialInfo);

		//PRFL-97 workaround. SakaiPerson table needs to be upgraded so locked is not null, but this handles it if not upgraded.
		if(sakaiPerson.getLocked() == null) {
			userProfile.setLocked(false);
			this.setLocked(false);
		} else {
			this.setLocked(sakaiPerson.getLocked());
			userProfile.setLocked(this.isLocked());
		}

		//add the current picture
		add(new ProfileImage("photo", new Model<String>(userUuid)));

		//change profile image button
		final Button changePicButton = new Button("changePicButton");

		changePicButton.add(new Label("changePictureLabel", new ResourceModel("link.change.profile.picture")));

		if(!imageLogic.isPicEditorEnabled()) {
			changePicButton.setEnabled(false);
			changePicButton.setVisible(false);
		}

		add(changePicButton);

		/* SIDELINKS */
		WebMarkupContainer sideLinks = new WebMarkupContainer("sideLinks");
		int visibleSideLinksCount = 0;
			
        //ADMIN: LOCK/UNLOCK A PROFILE
        WebMarkupContainer lockProfileContainer = new WebMarkupContainer("lockProfileContainer");
        final Label lockProfileLabel = new Label("lockProfileLabel");
        
        final AjaxLink<Void> lockProfileLink = new AjaxLink<Void>("lockProfileLink") {
            private static final long serialVersionUID = 1L;

            public void onClick(AjaxRequestTarget target) {
                //toggle it to be opposite of what it currently is, update labels and icons
                boolean locked = isLocked();
                if(sakaiProxy.toggleProfileLocked(userUuid, !locked)) {
                    setLocked(!locked);
                    log.info("MyProfile(): SuperUser toggled lock status of profile for " + userUuid + " to " + !locked);
                    lockProfileLabel.setDefaultModel(new ResourceModel("link.profile.locked." + isLocked()));
                    add(new AttributeModifier("title", new ResourceModel("text.profile.locked." + isLocked())));
                    if(isLocked()){
                        add(new AttributeModifier("class", new Model<String>("locked")));
                    } else {
                        add(new AttributeModifier("class", new Model<String>("unlocked")));
                    }
                    target.add(this);
                }
            }
        };
			
        //set init icon for locked
        if(isLocked()){
            lockProfileLink.add(new AttributeModifier("class", new Model<String>("locked")));
        } else {
            lockProfileLink.add(new AttributeModifier("class", new Model<String>("unlocked")));
        }
        
        lockProfileLink.add(lockProfileLabel);
                
        //setup link/label and windows with special property based on locked status
        lockProfileLabel.setDefaultModel(new ResourceModel("link.profile.locked." + isLocked()));
        lockProfileLink.add(new AttributeModifier("title", new ResourceModel("text.profile.locked." + isLocked())));
        
        lockProfileContainer.add(lockProfileLink);
        
        sideLinks.add(lockProfileContainer);

		// Hide the lock button until we work out to use it
		lockProfileContainer.setVisible(false);
        
        visibleSideLinksCount++;
		
		//hide entire list if no links to show
		if(visibleSideLinksCount == 0) {
			sideLinks.setVisible(false);
		}
		
		add(sideLinks);

		MyProfilePanelState panelState = new MyProfilePanelState();
	   	panelState.showSocialNetworkingDisplay = sakaiProxy.isSocialProfileEnabled();
		panelState.showNamePronunciationDisplay = sakaiProxy.isNamePronunciationProfileEnabled();
		
		add(new MyProfilePanel("myProfilePanel", userProfile, panelState));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(StringHeaderItem.forString("<script>includeWebjarLibrary('recordrtc');</script>"));
		response.render(StringHeaderItem.forString("<script>includeWebjarLibrary('webrtc-adapter');</script>"));
	}

	private boolean locked;
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
}
