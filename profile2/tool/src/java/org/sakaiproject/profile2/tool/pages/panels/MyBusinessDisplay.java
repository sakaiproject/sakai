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

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Panel for displaying business profile data.
 */
@Slf4j
public class MyBusinessDisplay extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	public MyBusinessDisplay(final String id, final UserProfile userProfile) {
		super(id);

		log.debug("MyBusinessDisplay()");

		setDefaultModel(new Model<String>("businessDisplayModel"));

		int visibleFieldCount = 0;

		add(new Label("heading", new ResourceModel("heading.business")));

		visibleFieldCount = addBusinessBiography(userProfile, visibleFieldCount);

		visibleFieldCount = addCompanyProfiles(userProfile, visibleFieldCount);

		addEditButton(id, userProfile);

		// no fields message
		Label noFieldsMessage = new Label("noFieldsMessage", new ResourceModel(
				"text.no.fields"));
		add(noFieldsMessage);
		if (visibleFieldCount > 0) {
			noFieldsMessage.setVisible(false);
		}
	}

	private int addBusinessBiography(final UserProfile userProfile,
			int visibleFieldCount) {

		WebMarkupContainer businessBiographyContainer = new WebMarkupContainer(
				"businessBiographyContainer");
		businessBiographyContainer.add(new Label("businessBiographyLabel",
				new ResourceModel("profile.business.bio")));
		businessBiographyContainer.add(new Label("businessBiography",
				ProfileUtils.processHtml(userProfile.getBusinessBiography()))
				.setEscapeModelStrings(false));
		add(businessBiographyContainer);

		if (StringUtils.isBlank(userProfile.getBusinessBiography())) {
			businessBiographyContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		return visibleFieldCount;
	}

	private int addCompanyProfiles(final UserProfile userProfile,
			int visibleFieldCount) {
		
		WebMarkupContainer companyProfilesContainer = new WebMarkupContainer(
				"companyProfilesContainer");

		companyProfilesContainer.add(new Label("companyProfilesLabel",
				new ResourceModel("profile.business.company.profiles")));
		
		List<ITab> tabs = new ArrayList<ITab>();

		if (null != userProfile.getCompanyProfiles()) {

			for (final CompanyProfile companyProfile : userProfile
					.getCompanyProfiles()) {

				tabs.add(new AbstractTab(new ResourceModel("profile.business.company.profile")) {

					private static final long serialVersionUID = 1L;

					@Override
					public Panel getPanel(String panelId) {

						return new CompanyProfileDisplay(panelId, companyProfile);
					}

				});
			}
		}

		companyProfilesContainer.add(new AjaxTabbedPanel("companyProfiles", tabs));
		add(companyProfilesContainer);
		
		if (0 == tabs.size()) {			
			companyProfilesContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}

		return visibleFieldCount;
	}

	private void addEditButton(final String id, final UserProfile userProfile) {
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton",
				new ResourceModel("button.edit")) {

			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				Component newPanel = new MyBusinessEdit(id, userProfile);
				newPanel.setOutputMarkupId(true);
				MyBusinessDisplay.this.replaceWith(newPanel);
				if (target != null) {
					target.add(newPanel);
					// resize iframe
					target.appendJavaScript("setMainFrameHeight(window.name);");
				}

			}

		};
		editButton.add(new Label("editButtonLabel", new ResourceModel(
				"button.edit")));
		editButton.setOutputMarkupId(true);

		if (userProfile.isLocked() && !sakaiProxy.isSuperUser()) {
			editButton.setVisible(false);
		}

		add(editButton);
	}
	
}
