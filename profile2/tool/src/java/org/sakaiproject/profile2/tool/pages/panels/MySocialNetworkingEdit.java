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
import org.apache.commons.lang.StringUtils;
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
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.components.ComponentVisualErrorBehaviour;
import org.sakaiproject.profile2.tool.components.FeedbackLabel;
import org.sakaiproject.profile2.tool.components.IconWithClueTip;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

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
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
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
					"text.edit.other.warning", null, new Object[] { userProfile
							.getDisplayName() }));
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
			protected void convertInput() {
				validateUrl(this);
			}
		};
		facebookUrl.setMarkupId("facebookurlinput");
		facebookUrl.setOutputMarkupId(true);
		facebookUrl.add(new UrlValidator());
		facebookContainer.add(facebookUrl);
		facebookContainer.add(new IconWithClueTip("facebookToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.profile.facebook.tooltip")));
		
		//feedback
        final FeedbackLabel facebookUrlFeedback = new FeedbackLabel("facebookUrlFeedback", facebookUrl);
        facebookUrlFeedback.setOutputMarkupId(true);
        facebookUrlFeedback.setMarkupId("facebookUrlFeedback");
        facebookContainer.add(facebookUrlFeedback);
        facebookUrl.add(new ComponentVisualErrorBehaviour("onblur", facebookUrlFeedback));
		
		form.add(facebookContainer);
		
		//linkedin
		WebMarkupContainer linkedinContainer = new WebMarkupContainer("linkedinContainer");
		linkedinContainer.add(new Label("linkedinLabel", new ResourceModel("profile.socialnetworking.linkedin.edit")));
		final TextField<String> linkedinUrl = new TextField<String>("linkedinUrl", new PropertyModel<String>(userProfile, "socialInfo.linkedinUrl")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void convertInput() {
				validateUrl(this);
			}
		};
		linkedinUrl.setMarkupId("linkedinurlinput");
		linkedinUrl.setOutputMarkupId(true);
		linkedinUrl.add(new UrlValidator());
		linkedinContainer.add(linkedinUrl);
		linkedinContainer.add(new IconWithClueTip("linkedinToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.profile.linkedin.tooltip")));
		
		//feedback
		final FeedbackLabel linkedinUrlFeedback = new FeedbackLabel("linkedinUrlFeedback", linkedinUrl);
        linkedinUrlFeedback.setMarkupId("linkedinUrlFeedback");
		linkedinUrlFeedback.setOutputMarkupId(true);
		linkedinContainer.add(linkedinUrlFeedback);
		linkedinUrl.add(new ComponentVisualErrorBehaviour("onblur", linkedinUrlFeedback));
		
		form.add(linkedinContainer);
		
		//myspace
		WebMarkupContainer myspaceContainer = new WebMarkupContainer("myspaceContainer");
		myspaceContainer.add(new Label("myspaceLabel", new ResourceModel("profile.socialnetworking.myspace.edit")));
		final TextField<String> myspaceUrl = new TextField<String>("myspaceUrl", new PropertyModel<String>(userProfile, "socialInfo.myspaceUrl")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void convertInput() {
				validateUrl(this);
			}
		};
		myspaceUrl.setMarkupId("myspaceurlinput");
		myspaceUrl.setOutputMarkupId(true);
		myspaceUrl.add(new UrlValidator());
		myspaceContainer.add(myspaceUrl);
		myspaceContainer.add(new IconWithClueTip("myspaceToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.profile.myspace.tooltip")));
		
		//feedback
		final FeedbackLabel myspaceUrlFeedback = new FeedbackLabel("myspaceUrlFeedback", myspaceUrl);
        myspaceUrlFeedback.setMarkupId("myspaceUrlFeedback");
		myspaceUrlFeedback.setOutputMarkupId(true);
		myspaceContainer.add(myspaceUrlFeedback);
		myspaceUrl.add(new ComponentVisualErrorBehaviour("onblur", myspaceUrlFeedback));
		
		form.add(myspaceContainer);
		
		//twitter
		WebMarkupContainer twitterContainer = new WebMarkupContainer("twitterContainer");
		twitterContainer.add(new Label("twitterLabel", new ResourceModel("profile.socialnetworking.twitter.edit")));
		final TextField<String> twitterUrl = new TextField<String>("twitterUrl", new PropertyModel<String>(userProfile, "socialInfo.twitterUrl")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void convertInput() {
				validateUrl(this);
			}
		};
		twitterUrl.setMarkupId("twitterurlinput");
		twitterUrl.setOutputMarkupId(true);
		twitterUrl.add(new UrlValidator());
		twitterContainer.add(twitterUrl);
		twitterContainer.add(new IconWithClueTip("twitterToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.profile.twitter.tooltip")));
		
		//feedback
		final FeedbackLabel twitterUrlFeedback = new FeedbackLabel("twitterUrlFeedback", twitterUrl);
		twitterUrlFeedback.setMarkupId("twitterUrlFeedback");
		twitterUrlFeedback.setOutputMarkupId(true);
		twitterContainer.add(twitterUrlFeedback);
		twitterUrl.add(new ComponentVisualErrorBehaviour("onblur", twitterUrlFeedback));
		
		form.add(twitterContainer);
		
		//skype
		WebMarkupContainer skypeContainer = new WebMarkupContainer("skypeContainer");
		skypeContainer.add(new Label("skypeLabel", new ResourceModel("profile.socialnetworking.skype.edit")));
		TextField skypeUsername = new TextField("skypeUsername", new PropertyModel(userProfile, "socialInfo.skypeUsername"));
		skypeUsername.setMarkupId("skypeusernameinput");
		skypeUsername.setOutputMarkupId(true);
		skypeContainer.add(skypeUsername);
		form.add(skypeContainer);
			
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.changes"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {

				if (save(form)) {

					// post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_SOCIAL_NETWORKING_UPDATE,"/profile/" + userProfile.getUserUuid(), true);

					//post to wall if enabled
					if (true == sakaiProxy.isWallEnabledGlobally() && false == sakaiProxy.isSuperUserAndProxiedToUser(userProfile.getUserUuid())) {
						wallLogic.addNewEventToWall(ProfileConstants.EVENT_PROFILE_SOCIAL_NETWORKING_UPDATE, sakaiProxy.getCurrentUserId());
					}
					
					// repaint panel
					Component newPanel = new MySocialNetworkingDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					MySocialNetworkingEdit.this.replaceWith(newPanel);
					if (target != null) {
						target.add(newPanel);
						// resize iframe
						target.appendJavaScript("setMainFrameHeight(window.name);");
					}

				} else {
					formFeedback.setDefaultModel(new ResourceModel("error.profile.save.business.failed"));
					formFeedback.add(new AttributeModifier("class", true,new Model<String>("save-failed-error")));
					target.add(formFeedback);
				}
			}
			
			// This is called if the form validation fails, ie Javascript turned off, 
			//or we had preexisting invalid data before this fix was introduced
			protected void onError(AjaxRequestTarget target, Form form) {
				
				//check which item didn't validate and update the class and feedback model for that component
				if(!facebookUrl.isValid()) {
					facebookUrl.add(new AttributeAppender("class", new Model<String>("invalid"), " "));
					target.add(facebookUrl);
					target.add(facebookUrlFeedback);
				}
				if(!linkedinUrl.isValid()) {
					linkedinUrl.add(new AttributeAppender("class", new Model<String>("invalid"), " "));
					target.add(linkedinUrl);
					target.add(linkedinUrlFeedback);
				}
				if(!myspaceUrl.isValid()) {
					myspaceUrl.add(new AttributeAppender("class", new Model<String>("invalid"), " "));
					target.add(myspaceUrl);
					target.add(myspaceUrlFeedback);
				}
				if(!twitterUrl.isValid()) {
					twitterUrl.add(new AttributeAppender("class", new Model<String>("invalid"), " "));
					target.add(twitterUrl);
					target.add(twitterUrlFeedback);
				}
			}
			
			
		};
		form.add(submitButton);

		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel",new ResourceModel("button.cancel"), form) {
			
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {

				Component newPanel = new MySocialNetworkingDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				MySocialNetworkingEdit.this.replaceWith(newPanel);
				if (target != null) {
					target.add(newPanel);
					target.appendJavaScript("setMainFrameHeight(window.name);");
				}

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
		String tMyspace = ProfileUtils.truncate(userProfile.getSocialInfo().getMyspaceUrl(), 255, false);
		String tSkype = ProfileUtils.truncate(userProfile.getSocialInfo().getSkypeUsername(), 255, false);
		String tTwitter = ProfileUtils.truncate(userProfile.getSocialInfo().getTwitterUrl(), 255, false);

		socialNetworkingInfo.setFacebookUrl(tFacebook);
		socialNetworkingInfo.setLinkedinUrl(tLinkedin);
		socialNetworkingInfo.setMyspaceUrl(tMyspace);
		socialNetworkingInfo.setSkypeUsername(tSkype);
		socialNetworkingInfo.setTwitterUrl(tTwitter);
		
		return profileLogic.saveSocialNetworkingInfo(socialNetworkingInfo);
		
	}
}
