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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.CompanyProfile;

/**
 * Panel for displaying business profile information.
 */
public class ViewBusiness extends Panel {

	private static final long serialVersionUID = 1L;
	private int visibleFieldCount_business = 0;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
	public ViewBusiness(String id, String userUuid, SakaiPerson sakaiPerson,
			boolean isBusinessInfoAllowed) {

		super(id);

		WebMarkupContainer businessInfoContainer = new WebMarkupContainer(
				"mainSectionContainer_business");
		businessInfoContainer.setOutputMarkupId(true);
		businessInfoContainer.add(new Label("mainSectionHeading_business",
				new ResourceModel("heading.business")));
		add(businessInfoContainer);

		WebMarkupContainer businessBiographyContainer = new WebMarkupContainer(
				"businessBiographyContainer");

		businessBiographyContainer.add(new Label("businessBiographyLabel",
				new ResourceModel("profile.business.bio")));
		businessBiographyContainer.add(new Label("businessBiography",
				sakaiPerson.getBusinessBiography()));

		businessInfoContainer.add(businessBiographyContainer);

		if (StringUtils.isBlank(sakaiPerson.getBusinessBiography())) {
			businessBiographyContainer.setVisible(false);
		} else {
			visibleFieldCount_business++;
		}

		WebMarkupContainer companyProfilesContainer = new WebMarkupContainer(
				"companyProfilesContainer");

		companyProfilesContainer.add(new Label("companyProfilesLabel",
				new ResourceModel("profile.business.company.profiles")));

		List<CompanyProfile> companyProfiles = profileLogic.getCompanyProfiles(userUuid);

		List<ITab> tabs = new ArrayList<ITab>();
		if (null != companyProfiles) {

			for (final CompanyProfile companyProfile : companyProfiles) {

				visibleFieldCount_business++;

				tabs.add(new AbstractTab(new ResourceModel(
						"profile.business.company.profile")) {

					private static final long serialVersionUID = 1L;

					@Override
					public Panel getPanel(String panelId) {

						return new CompanyProfileDisplay(panelId,
								companyProfile);
					}

				});
			}
		}

		companyProfilesContainer.add(new AjaxTabbedPanel("companyProfiles", tabs));
		businessInfoContainer.add(companyProfilesContainer);

		if (0 == tabs.size()) {
			companyProfilesContainer.setVisible(false);
		}

		// if nothing/not allowed, hide whole panel
		if (visibleFieldCount_business == 0 || !isBusinessInfoAllowed) {
			businessInfoContainer.setVisible(false);
		}
	}

	public int getVisibleFieldCount() {

		return visibleFieldCount_business;
	}
}
