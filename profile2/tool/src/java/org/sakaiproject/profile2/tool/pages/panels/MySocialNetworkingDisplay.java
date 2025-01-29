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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileUtils;

import java.util.Optional;

/**
 * Panel for displaying social networking profile data.
 */
@Slf4j
public class MySocialNetworkingDisplay extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;

	public MySocialNetworkingDisplay(final String id, final UserProfile userProfile) {
		super(id);

		log.debug("MySocialNetworkingDisplay()");

		add(new Label("heading", new ResourceModel("heading.social")));

		// social networking
		String facebookUrl = userProfile.getSocialInfo().getFacebookUrl();
		String linkedinUrl = userProfile.getSocialInfo().getLinkedinUrl();
		String instagramUrl = userProfile.getSocialInfo().getInstagramUrl();

		int visibleFieldCount = 0;

		//facebook
		WebMarkupContainer facebookContainer = new WebMarkupContainer("facebookContainer");
		facebookContainer.add(new Label("facebookLabel", new ResourceModel("profile.socialnetworking.facebook")));
		facebookContainer.add(new ExternalLink("facebookLink", facebookUrl, facebookUrl));
		add(facebookContainer);
		if(StringUtils.isBlank(facebookUrl)) {
			facebookContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}

		//linkedin
		WebMarkupContainer linkedinContainer = new WebMarkupContainer("linkedinContainer");
		linkedinContainer.add(new Label("linkedinLabel", new ResourceModel("profile.socialnetworking.linkedin")));
		linkedinContainer.add(new ExternalLink("linkedinLink", linkedinUrl, linkedinUrl));
		add(linkedinContainer);
		if(StringUtils.isBlank(linkedinUrl)) {
			linkedinContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}

		//instagram
		WebMarkupContainer instagramContainer = new WebMarkupContainer("instagramContainer");
		instagramContainer.add(new Label("instagramLabel", new ResourceModel("profile.socialnetworking.instagram")));
		instagramContainer.add(new ExternalLink("instagramLink", instagramUrl, instagramUrl));
		add(instagramContainer);
		if(StringUtils.isBlank(instagramUrl)) {
			instagramContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}

		//edit button
		AjaxFallbackLink<Void> editButton = new AjaxFallbackLink<>("editButton") {
			@Override
			public void onClick(Optional<AjaxRequestTarget> targetOptional) {
				Component newPanel = new MySocialNetworkingEdit(id, userProfile);
				newPanel.setOutputMarkupId(true);
				MySocialNetworkingDisplay.this.replaceWith(newPanel);
				targetOptional.ifPresent(target -> {
					target.add(newPanel);
					// resize iframe
					target.appendJavaScript("setMainFrameHeight(window.name);");
				});
			}
		};
		editButton.add(new Label("editButtonLabel", new ResourceModel("button.edit")));
		editButton.add(new AttributeModifier("aria-label", new ResourceModel("accessibility.edit.social")));
		editButton.setOutputMarkupId(true);

		if (userProfile.isLocked() && !sakaiProxy.isSuperUser()) {
			editButton.setVisible(false);
		}

		add(editButton);

		// no fields message
		Label noFieldsMessage = new Label("noFieldsMessage", new ResourceModel("text.no.fields"));
		add(noFieldsMessage);
		if (visibleFieldCount > 0) {
			noFieldsMessage.setVisible(false);
		}
	}
}
