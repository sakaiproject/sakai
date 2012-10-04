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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.UrlValidator;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.tool.components.ComponentVisualErrorBehaviour;
import org.sakaiproject.profile2.tool.components.FeedbackLabel;

/**
 * Panel for displaying and editing company profile data
 */
public class CompanyProfileEdit extends Panel {

	private static final long serialVersionUID = 1L;
	
	public CompanyProfileEdit(String id, CompanyProfile companyProfile) {

		super(id, new Model(companyProfile));

		WebMarkupContainer companyNameContainer = new WebMarkupContainer(
				"companyNameContainer");
		companyNameContainer.add(new Label("companyNameLabel",
				new ResourceModel("profile.business.company.name")));
		TextField companyName = new TextField("companyName",
				new PropertyModel(companyProfile, "companyName"));
		companyName.setOutputMarkupId(true);
		companyNameContainer.add(companyName);
		String companyNameId = companyName.getMarkupId();
		Label companyNameAccessibilityLabel = new Label("companyNameAccessibilityLabel", new ResourceModel("accessibility.profile.companyname.input"));
		companyNameAccessibilityLabel.add(new AttributeAppender("for",new Model(companyNameId)," "));
		companyNameContainer.add(companyNameAccessibilityLabel);

		add(companyNameContainer);

		WebMarkupContainer companyWebAddressContainer = new WebMarkupContainer(
				"companyWebAddressContainer");
		companyWebAddressContainer.add(new Label("companyWebAddressLabel",
				new ResourceModel("profile.business.company.web")));

		TextField companyWebAddress = new TextField("companyWebAddress",
				new PropertyModel(companyProfile, "companyWebAddress")) {

			private static final long serialVersionUID = 1L;

			// add http:// if missing
			@Override
			protected void convertInput() {
				String input = getInput();

				if (StringUtils.isNotBlank(input)
						&& !(input.startsWith("http://") || input
								.startsWith("https://"))) {
					setConvertedInput("http://" + input);
				} else {
					setConvertedInput(StringUtils.isBlank(input) ? null : input);
				}
			}
		};
		companyWebAddress.setOutputMarkupId(true);
		companyWebAddress.add(new UrlValidator());
		companyWebAddressContainer.add(companyWebAddress);
		String companyUrlId = companyWebAddress.getMarkupId();
		Label companyUrlAccessibilityLabel = new Label("companyUrlAccessibilityLabel", new ResourceModel("accessibility.profile.companyurl.input"));
		companyUrlAccessibilityLabel.add(new AttributeAppender("for",new Model(companyUrlId)," "));
		companyWebAddressContainer.add(companyUrlAccessibilityLabel);

		final FeedbackLabel companyWebAddressFeedback = new FeedbackLabel(
				"companyWebAddressFeedback", companyWebAddress);
		companyWebAddressFeedback.setOutputMarkupId(true);
		companyWebAddressContainer.add(companyWebAddressFeedback);
		companyWebAddress.add(new ComponentVisualErrorBehaviour("onblur",
				companyWebAddressFeedback));
		companyWebAddress.add(new AttributeAppender("aria-describedby",new Model(companyWebAddressFeedback.getMarkupId())," "));

		add(companyWebAddressContainer);

		WebMarkupContainer companyDescriptionContainer = new WebMarkupContainer(
				"companyDescriptionContainer");
		companyDescriptionContainer.add(new Label("companyDescriptionLabel",
				new ResourceModel("profile.business.company.description")));
		TextArea companyDescription = new TextArea("companyDescription",
				new PropertyModel(companyProfile, "companyDescription"));
		companyDescription.setOutputMarkupId(true);
		companyDescriptionContainer.add(companyDescription);
		String companyDescriptionId = companyDescription.getMarkupId();
		Label companyDescriptionAccessibilityLabel = new Label("companyDescriptionAccessibilityLabel", new ResourceModel("accessibility.profile.companydescription.input"));
		companyDescriptionAccessibilityLabel.add(new AttributeAppender("for",new Model(companyDescriptionId)," "));
		companyDescriptionContainer.add(companyDescriptionAccessibilityLabel);

		add(companyDescriptionContainer);
	}
}
