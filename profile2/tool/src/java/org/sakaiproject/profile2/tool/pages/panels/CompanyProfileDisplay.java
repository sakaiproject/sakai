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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.profile2.model.CompanyProfile;

/**
 * Panel for displaying and editing company profile data
 */
public class CompanyProfileDisplay extends Panel {

	private static final long serialVersionUID = 1L;
	
	public CompanyProfileDisplay(String id, CompanyProfile companyProfile) {

		super(id);

		String companyName = companyProfile.getCompanyName();
		String companyWebAddress = companyProfile.getCompanyWebAddress();
		String companyDescription = companyProfile.getCompanyDescription();

		WebMarkupContainer companyNameContainer = new WebMarkupContainer(
				"companyNameContainer");
		companyNameContainer.add(new Label("companyNameLabel",
				new ResourceModel("profile.business.company.name")));
		companyNameContainer.add(new Label("companyName", companyName));

		add(companyNameContainer);

		if (StringUtils.isBlank(companyName)) {
			companyNameContainer.setVisible(false);
		}

		WebMarkupContainer companyWebAddressContainer = new WebMarkupContainer(
				"companyWebAddressContainer");
		companyWebAddressContainer.add(new Label("companyWebAddressLabel",
				new ResourceModel("profile.business.company.web")));
		companyWebAddressContainer.add(new ExternalLink("companyWebAddress",
				companyWebAddress, companyWebAddress));

		add(companyWebAddressContainer);

		if (StringUtils.isBlank(companyWebAddress)) {
			companyWebAddressContainer.setVisible(false);
		}

		WebMarkupContainer companyDescriptionContainer = new WebMarkupContainer(
				"companyDescriptionContainer");
		companyDescriptionContainer.add(new Label("companyDescriptionLabel",
				new ResourceModel("profile.business.company.description")));
		companyDescriptionContainer.add(new Label("companyDescription",
				companyDescription));

		add(companyDescriptionContainer);
		
		if (StringUtils.isBlank(companyDescription)) {
			companyDescriptionContainer.setVisible(false);
		}
	}
}
