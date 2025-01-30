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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyInfoDisplay extends Panel {

	private static final long serialVersionUID = 1L;
	private int visibleFieldCount = 0;
	private String birthday = ""; 
	private String birthdayDisplay = "";

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;

    @SpringBean(name="org.sakaiproject.component.api.ServerConfigurationService")
    private ServerConfigurationService serverConfigurationService;

	public MyInfoDisplay(final String id, final UserProfile userProfile) {
		super(id);

		log.debug("MyInfoDisplay()");

		//this panel stuff
		final Component thisPanel = this;

		//get userId of this profile
		String userId = userProfile.getUserUuid();
		
		//get info from userProfile since we need to validate it and turn things off if not set.
		//otherwise we could just use a propertymodel
		String nickname = userProfile.getNickname();
		String personalSummary = userProfile.getPersonalSummary();

		//heading
		add(new Label("heading", new ResourceModel("heading.basic")));

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

		WebMarkupContainer pronounsContainer = new WebMarkupContainer("pronounsContainer");
		pronounsContainer.add(new Label("pronounsLabel", new ResourceModel("profile.pronouns")));
		pronounsContainer.add(new Label("pronouns", ProfileUtils.processHtml(userProfile.getPronouns())).setEscapeModelStrings(false));
		pronounsContainer.setVisible(serverConfigurationService.getBoolean("profile2.profile.pronouns.enabled", true));
		add(pronounsContainer);
		if (StringUtils.isBlank(userProfile.getPronouns())) {
			pronounsContainer.setVisible(false);
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
		AjaxFallbackLink<Void> editButton = new AjaxFallbackLink<Void>("editButton") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Optional<AjaxRequestTarget> targetOptional) {
				Component newPanel = new MyInfoEdit(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				targetOptional.ifPresent(target -> {
					target.add(newPanel);
					//resize iframe
					target.appendJavaScript("setMainFrameHeight(window.name);");
				});
			}

		};
		editButton.add(new Label("editButtonLabel", new ResourceModel("button.edit")));
		editButton.add(new AttributeModifier("aria-label", new ResourceModel("accessibility.edit.basic")));
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
