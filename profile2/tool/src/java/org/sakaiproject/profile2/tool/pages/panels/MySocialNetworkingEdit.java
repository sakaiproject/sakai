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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.components.ComponentVisualErrorBehaviour;
import org.sakaiproject.profile2.tool.components.FeedbackLabel;
import org.sakaiproject.profile2.tool.components.IconWithToolTip;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Panel for editing social networking profile data.
 */
@Slf4j
public class MySocialNetworkingEdit extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
	public MySocialNetworkingEdit(final String id, final UserProfile userProfile) {
		super(id);
		
		log.debug("MySocialNetworkingEdit()");
		
		// heading
		add(new Label("heading", new ResourceModel("heading.social.edit")));

		// setup form
		Form form = new Form("form", new Model(userProfile));
		form.setOutputMarkupId(true);

		// form submit feedback
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		form.add(formFeedback);

		// add warning message if superUser and not editing own profile
		Label editWarning = new Label("editWarning");
		editWarning.setVisible(false);
		if (sakaiProxy.isSuperUserAndProxiedToUser(
				userProfile.getUserUuid())) {
			editWarning.setDefaultModel(new StringResourceModel(
					"text.edit.other.warning").setParameters(userProfile.getDisplayName()));
			editWarning.setEscapeModelStrings(false);
			editWarning.setVisible(true);
		}
		form.add(editWarning);
		
		//facebook
		WebMarkupContainer facebookContainer = new WebMarkupContainer("facebookContainer");
		facebookContainer.add(new Label("facebookLabel", new ResourceModel("profile.socialnetworking.facebook.edit")));
		final TextField<String> facebookUrl = new TextField<String>("facebookUrl", new PropertyModel<String>(userProfile, "socialInfo.facebookUrl")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void convertInput() {
				validateUrl(this);
			}
		};
		facebookUrl.setMarkupId("facebookurlinput");
		facebookUrl.setOutputMarkupId(true);
		facebookUrl.add(new UrlValidator());
		facebookContainer.add(facebookUrl);
		facebookContainer.add(new IconWithToolTip("facebookToolTip", ProfileConstants.INFO_ICON, new ResourceModel("text.profile.facebook.tooltip")));
		
		//feedback
        final FeedbackLabel facebookUrlFeedback = new FeedbackLabel("facebookUrlFeedback", facebookUrl);
        facebookUrlFeedback.setOutputMarkupId(true);
        facebookUrlFeedback.setMarkupId("facebookUrlFeedback");
        facebookContainer.add(facebookUrlFeedback);
        facebookUrl.add(new ComponentVisualErrorBehaviour("blur", facebookUrlFeedback));
		
		form.add(facebookContainer);
		
		//linkedin
		WebMarkupContainer linkedinContainer = new WebMarkupContainer("linkedinContainer");
		linkedinContainer.add(new Label("linkedinLabel", new ResourceModel("profile.socialnetworking.linkedin.edit")));
		final TextField<String> linkedinUrl = new TextField<String>("linkedinUrl", new PropertyModel<String>(userProfile, "socialInfo.linkedinUrl")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void convertInput() {
				validateUrl(this);
			}
		};
		linkedinUrl.setMarkupId("linkedinurlinput");
		linkedinUrl.setOutputMarkupId(true);
		linkedinUrl.add(new UrlValidator());
		linkedinContainer.add(linkedinUrl);
		linkedinContainer.add(new IconWithToolTip("linkedinToolTip", ProfileConstants.INFO_ICON, new ResourceModel("text.profile.linkedin.tooltip")));
		
		//feedback
		final FeedbackLabel linkedinUrlFeedback = new FeedbackLabel("linkedinUrlFeedback", linkedinUrl);
        linkedinUrlFeedback.setMarkupId("linkedinUrlFeedback");
		linkedinUrlFeedback.setOutputMarkupId(true);
		linkedinContainer.add(linkedinUrlFeedback);
		linkedinUrl.add(new ComponentVisualErrorBehaviour("blur", linkedinUrlFeedback));
		
		form.add(linkedinContainer);
		
		//instagram
		WebMarkupContainer instagramContainer = new WebMarkupContainer("instagramContainer");
		instagramContainer.add(new Label("instagramLabel", new ResourceModel("profile.socialnetworking.instagram.edit")));
		final TextField<String> instagramUrl = new TextField<String>("instagramUrl", new PropertyModel<String>(userProfile, "socialInfo.instagramUrl")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void convertInput() {
				validateUrl(this);
			}
		};
		instagramUrl.setMarkupId("instagramurlinput");
		instagramUrl.setOutputMarkupId(true);
		instagramUrl.add(new UrlValidator());
		instagramContainer.add(instagramUrl);
		instagramContainer.add(new IconWithToolTip("instagramToolTip", ProfileConstants.INFO_ICON, new ResourceModel("text.profile.instagram.tooltip")));
		
		//feedback
		final FeedbackLabel instagramUrlFeedback = new FeedbackLabel("instagramUrlFeedback", instagramUrl);
        instagramUrlFeedback.setMarkupId("instagramUrlFeedback");
		instagramUrlFeedback.setOutputMarkupId(true);
		instagramContainer.add(instagramUrlFeedback);
		instagramUrl.add(new ComponentVisualErrorBehaviour("blur", instagramUrlFeedback));
		
		form.add(instagramContainer);
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.changes"), form) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(Optional<AjaxRequestTarget> targetOptional) {

				if (save(form)) {

					// post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_SOCIAL_NETWORKING_UPDATE,"/profile/" + userProfile.getUserUuid(), true);

					// repaint panel
					Component newPanel = new MySocialNetworkingDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					MySocialNetworkingEdit.this.replaceWith(newPanel);
					targetOptional.ifPresent(target -> {
						target.add(newPanel);
						// resize iframe
						target.appendJavaScript("setMainFrameHeight(window.name);");
					});

				} else {
					targetOptional.ifPresent(target -> {
						formFeedback.setDefaultModel(new ResourceModel("error.profile.save.business.failed"));
						formFeedback.add(new AttributeModifier("class", new Model<String>("save-failed-error")));
						target.add(formFeedback);
					});
				}
			}
			
			// This is called if the form validation fails, ie Javascript turned off, 
			//or we had preexisting invalid data before this fix was introduced
			@Override
			protected void onError(Optional<AjaxRequestTarget> targetOptional) {
				targetOptional.ifPresent(target -> {
					//check which item didn't validate and update the class and feedback model for that component
					if (!facebookUrl.isValid()) {
						facebookUrl.add(new AttributeAppender("class", new Model<String>("invalid"), " "));
						target.add(facebookUrl);
						target.add(facebookUrlFeedback);
					}
					if (!linkedinUrl.isValid()) {
						linkedinUrl.add(new AttributeAppender("class", new Model<String>("invalid"), " "));
						target.add(linkedinUrl);
						target.add(linkedinUrlFeedback);
					}
					if (!instagramUrl.isValid()) {
						instagramUrl.add(new AttributeAppender("class", new Model<String>("invalid"), " "));
						target.add(instagramUrl);
						target.add(instagramUrlFeedback);
					}
				});
			}
			
			
		};
		form.add(submitButton);

		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel",new ResourceModel("button.cancel"), form) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(Optional<AjaxRequestTarget> targetOptional) {

				Component newPanel = new MySocialNetworkingDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				MySocialNetworkingEdit.this.replaceWith(newPanel);
				targetOptional.ifPresent(target -> {
					target.add(newPanel);
					target.appendJavaScript("setMainFrameHeight(window.name);");
				});

			}
			
		};
		cancelButton.setDefaultFormProcessing(false);
		form.add(cancelButton);
		
		add(form);
	}

	// adds http:// if missing
	private void validateUrl(TextField<String> urlTextField) {
		String input = urlTextField.getInput();

		if (StringUtils.isNotBlank(input)
				&& !(input.startsWith("http://") || input
						.startsWith("https://"))) {
			urlTextField.setConvertedInput("http://" + input);
		} else {
			urlTextField.setConvertedInput(StringUtils.isBlank(input) ? null : input);
		}
	}
	
	// called when the form is to be saved
	private boolean save(Form form) {
		
		// get the backing model
		UserProfile userProfile = (UserProfile) form.getModelObject();
		
		// save social networking information
		SocialNetworkingInfo socialNetworkingInfo = new SocialNetworkingInfo(userProfile.getUserUuid());
		
		String tFacebook = ProfileUtils.truncate(userProfile.getSocialInfo().getFacebookUrl(), 255, false);
		String tLinkedin = ProfileUtils.truncate(userProfile.getSocialInfo().getLinkedinUrl(), 255, false);
		String tInstagram = ProfileUtils.truncate(userProfile.getSocialInfo().getInstagramUrl(), 255, false);

		socialNetworkingInfo.setFacebookUrl(tFacebook);
		socialNetworkingInfo.setLinkedinUrl(tLinkedin);
		socialNetworkingInfo.setInstagramUrl(tInstagram);

		return profileLogic.saveSocialNetworkingInfo(socialNetworkingInfo);
	}
}
