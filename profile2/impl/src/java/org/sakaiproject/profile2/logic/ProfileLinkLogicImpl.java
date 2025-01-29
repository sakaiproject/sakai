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
package org.sakaiproject.profile2.logic;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.profile2.util.ProfileConstants;

import lombok.Setter;

/**
 * Implementation of ProfileLinkLogic API
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfileLinkLogicImpl implements ProfileLinkLogic {

	@Override
	public String getInternalDirectUrlToUserProfile() {
		final String currentUserUuid = this.sakaiProxy.getCurrentUserId();
		if (currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		// TODO this could be an enum
		return this.sakaiProxy.getDirectUrlToProfileComponent(currentUserUuid, "profile", null);
	}

	@Override
	public String getInternalDirectUrlToUserProfile(final String userUuid) {
		if (this.sakaiProxy.getCurrentUserId() == null) {
			throw new SecurityException("Must be logged in.");
		}
		return this.sakaiProxy.getDirectUrlToProfileComponent(userUuid, "viewprofile", null);
	}

	@Override
	public String getInternalDirectUrlToUserProfile(final String viewerUuid, final String viewedUuid) {
		return this.sakaiProxy.getDirectUrlToProfileComponent(viewerUuid, viewedUuid, "viewprofile", null);
	}

	@Override
	public String getEntityLinkToProfileHome(final String userUuid) {
		final StringBuilder url = new StringBuilder();
		url.append(getEntityLinkBase());
		url.append(ProfileConstants.LINK_ENTITY_PROFILE);
		if (StringUtils.isNotBlank(userUuid)) {
			url.append("/");
			url.append(userUuid);
		}
		return url.toString();
	}

	/**
	 * Special method that mimics the urlFor method from Wicket for a class. Since we can't use that outside of Wicket, we need to copy it's
	 * behaviour here. This must be tested in Wicket upgrades but should be stable since the idea is that they are bookmarkable links and
	 * shouldn't change.
	 *
	 * <p>
	 * The return is not URL encoded as it is encoded in SakaiProxy
	 * </p>
	 *
	 * @param pageClass page class to be used, see ProfileConstants.WICKET_PAGE_PROFILE for example
	 * @param params key,value pair of any additional params required for the URL
	 * @return
	 *
	 * 		@deprecated. This is no longer safe to use. Bookmarkable page mounts should be used instead as we can predict the URLs. See
	 *         {@link ProfileApplication for the mounts}
	 */
	@Deprecated
	private String getFormattedStateParamForWicketTool(final String pageClass, final Map<String, String> vars) {

		// %3Fwicket%3AbookmarkablePage%3D%3Aorg.sakaiproject.profile2.tool.pages.MyFriends
		// ?wicket:bookmarkablePage=:org.sakaiproject.profile2.tool.pages.MyFriends
		// %3Fwicket%3AbookmarkablePage%3D%3Aorg.sakaiproject.profile2.tool.pages.MyMessageView%26thread%3D99eb8904-e4a5-4569-bbda-bef4be6803aa
		// &thread=99eb8904-e4a5-4569-bbda-bef4be6803aa

		final StringBuilder params = new StringBuilder();
		params.append("?wicket:bookmarkablePage=:");
		params.append(pageClass);

		if (vars != null) {
			for (final Map.Entry<String, String> var : vars.entrySet()) {
				params.append("&");
				params.append(var.getKey());
				params.append("=");
				params.append(var.getValue());
			}
		}

		return params.toString();
	}

	/**
	 * Helper method to create the link base. We then append more onto it to get the full link.
	 *
	 * @return
	 */
	private String getEntityLinkBase() {
		final StringBuilder base = new StringBuilder();
		base.append(this.sakaiProxy.getServerUrl());
		base.append(ProfileConstants.ENTITY_BROKER_PREFIX);
		base.append(ProfileConstants.LINK_ENTITY_PREFIX);
		return base.toString();
	}

	@Setter
	private SakaiProxy sakaiProxy;

}
