/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
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
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.components.IconWithClueTip;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Panel for editing social networking profile data.
 */
public class MySocialNetworkingEdit extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MySocialNetworkingEdit.class);
	
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
		facebookContainer.add(new TextField("facebookUrl", new PropertyModel(userProfile, "socialInfo.facebookUrl")));
		facebookContainer.add(new IconWithClueTip("facebookToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.profile.facebook.tooltip")));
		form.add(facebookContainer);
		
		//linkedin
		WebMarkupContainer linkedinContainer = new WebMarkupContainer("linkedinContainer");
		linkedinContainer.add(new Label("linkedinLabel", new ResourceModel("profile.socialnetworking.linkedin.edit")));
		linkedinContainer.add(new TextField("linkedinUrl", new PropertyModel(userProfile, "socialInfo.linkedinUrl")));
		linkedinContainer.add(new IconWithClueTip("linkedinToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.profile.linkedin.tooltip")));
		form.add(linkedinContainer);
		
		//myspace
		WebMarkupContainer myspaceContainer = new WebMarkupContainer("myspaceContainer");
		myspaceContainer.add(new Label("myspaceLabel", new ResourceModel("profile.socialnetworking.myspace.edit")));
		myspaceContainer.add(new TextField("myspaceUrl", new PropertyModel(userProfile, "socialInfo.myspaceUrl")));
		myspaceContainer.add(new IconWithClueTip("myspaceToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.profile.myspace.tooltip")));
		form.add(myspaceContainer);
		
		//twitter
		WebMarkupContainer twitterContainer = new WebMarkupContainer("twitterContainer");
		twitterContainer.add(new Label("twitterLabel", new ResourceModel("profile.socialnetworking.twitter.edit")));
		twitterContainer.add(new TextField("twitterUrl", new PropertyModel(userProfile, "socialInfo.twitterUrl")));
		twitterContainer.add(new IconWithClueTip("twitterToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.profile.twitter.tooltip")));
		form.add(twitterContainer);
		
		//skype
		WebMarkupContainer skypeContainer = new WebMarkupContainer("skypeContainer");
		skypeContainer.add(new Label("skypeLabel", new ResourceModel("profile.socialnetworking.skype.edit")));
		skypeContainer.add(new TextField("skypeUsername", new PropertyModel(userProfile, "socialInfo.skypeUsername")));
		form.add(skypeContainer);
			
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.changes"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {

				if (save(form)) {

					// post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_SOCIAL_NETWORKING_UPDATE,"/profile/" + userProfile.getUserUuid(), true);

					//post to wall if enabled
					if (true == sakaiProxy.isWallEnabledGlobally()) {
						wallLogic.addEventToWalls(ProfileConstants.EVENT_PROFILE_SOCIAL_NETWORKING_UPDATE, sakaiProxy.getCurrentUserId());
					}
					
					// repaint panel
					Component newPanel = new MySocialNetworkingDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					MySocialNetworkingEdit.this.replaceWith(newPanel);
					if (target != null) {
						target.addComponent(newPanel);
						// resize iframe
						target.appendJavascript("setMainFrameHeight(window.name);");
					}

				} else {
					formFeedback.setDefaultModel(new ResourceModel("error.profile.save.business.failed"));
					formFeedback.add(new AttributeModifier("class", true,new Model<String>("save-failed-error")));
					target.addComponent(formFeedback);
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
					target.addComponent(newPanel);
					target.appendJavascript("setMainFrameHeight(window.name);");
				}

			}
		};
		cancelButton.setDefaultFormProcessing(false);
		form.add(cancelButton);
		
		add(form);
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
