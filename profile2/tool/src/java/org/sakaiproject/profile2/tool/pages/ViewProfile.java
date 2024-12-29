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
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.AttributeModifier;

import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.tool.components.ProfileImage;
import org.sakaiproject.profile2.tool.pages.panels.ViewProfilePanel;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.user.api.User;

import java.util.Objects;

@Slf4j
public class ViewProfile extends BasePage {

	public ViewProfile(final String userUuid, final String tab) {

		log.debug("ViewProfile()");

		// get current user info
		final User currentUser = this.sakaiProxy.getUserQuietly(this.sakaiProxy.getCurrentUserId());
		final String currentUserId = currentUser.getId();
		final String currentUserType = currentUser.getType();

		// double check, if somehow got to own ViewPage, redirect to MyProfile instead
		if (userUuid.equals(currentUserId)) {
            log.warn("ViewProfile: user {} accessed ViewProfile for self. Redirecting...", userUuid);
			throw new RestartResponseException(new MyProfile());
		}

		// check if super user, to grant editing rights to another user's profile
		if (this.sakaiProxy.isSuperUser()) {
            log.warn("ViewProfile: superUser {} accessed ViewProfile for {}. Redirecting to allow edit.", currentUserId, userUuid);
			throw new RestartResponseException(new MyProfile(userUuid));
		}

		preferencesLink.setVisible(false);

		// post view event
		this.sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_VIEW_OTHER, "/profile/" + userUuid, false);

		// get some values from User
		final User user = this.sakaiProxy.getUserQuietly(userUuid);
		if (user == null) {
			throw new IllegalArgumentException("User does not exist: " + userUuid);
		}
		final String userDisplayName = user.getDisplayName();
		otherProfileLink.get("otherProfileLabel").setDefaultModel(
			new StringResourceModel("link.other.profile").setParameters(userDisplayName));
		otherProfileLink.setVisible(true);
		otherProfileLink.setEnabled(false);
		otherProfileContainer.add(new AttributeModifier("class", "current"));
		final String userType = user.getType();

		final ProfilePreferences prefs = this.preferencesLogic.getPreferencesRecordForUser(userUuid);

		/* IMAGE */
		ProfileImage profileImage = new ProfileImage("photo", new Model<String>(userUuid));
		if (getPage().getPageParameters() != null) {
			String fromSiteId = getParamValue(getPage().getPageParameters(), "fromSiteId");
			if (fromSiteId != null) {
				profileImage.setSiteId(fromSiteId);
			}
		}
		add(profileImage);

		/* NAME */
		final Label profileName = new Label("profileName", userDisplayName);
		add(profileName);

		add(new ViewProfilePanel("viewProfilePanel", userUuid, currentUserId));
	}

	/**
	 * This constructor is called if we have a pageParameters object containing the userUuid as an id parameter Just redirects to normal
	 * ViewProfile(String userUuid)
     */
	public ViewProfile(final PageParameters parameters) {
		this(Objects.requireNonNull(getParamValue(parameters, ProfileConstants.WICKET_PARAM_USERID)), getParamValue(parameters, ProfileConstants.WICKET_PARAM_TAB));
	}

	public ViewProfile(final String userUuid) {
		this(userUuid, null);
	}

}
