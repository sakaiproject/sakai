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

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import org.sakaiproject.profile2.logic.ProfileExternalIntegrationLogic;
import org.sakaiproject.profile2.model.ExternalIntegrationInfo;
import org.sakaiproject.profile2.tool.components.AjaxExternalLink;
import org.sakaiproject.profile2.tool.models.StringModel;
import org.sakaiproject.profile2.util.ProfileConstants;

@Slf4j
public class TwitterPrefsPane extends Panel {

	private static final long serialVersionUID = 1L;
	private transient ExternalIntegrationInfo externalIntegrationInfo;
	private transient RequestToken requestToken;
	
	private Fragment currentFragment;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileExternalIntegrationLogic")
	protected ProfileExternalIntegrationLogic externalIntegrationLogic;
	
	public TwitterPrefsPane(String id, String userUuid) {
		super(id);
		
		//get record
		externalIntegrationInfo = externalIntegrationLogic.getExternalIntegrationInfo(userUuid);
		
		//setup Twitter request token
		setTwitterRequestToken();
		
		//setup relevant fragment
		if(externalIntegrationInfo.isTwitterAlreadyConfigured()) {
			currentFragment = linkedFragment();
		} else {
			currentFragment = unlinkedFragment();
		}

		add(currentFragment);
	}
	
	/**
	 * Fragment which returns the components for the unlinked view
	 * @return
	 */
	private Fragment unlinkedFragment() {
		
		Fragment frag = new Fragment("fragmentContainer", "unlinked", this);
		
		//twitter auth form
		StringModel twitterModel = new StringModel();
		final Form<StringModel> twitterForm = new Form<StringModel>("twitterForm", new Model<StringModel>(twitterModel));
		
		//auth code
		final TextField<String> twitterAuthCode = new TextField<String>("twitterAuthCode", new PropertyModel<String>(twitterModel, "string"));        
		twitterAuthCode.setMarkupId("twitterauthcodeinput");
		twitterAuthCode.setOutputMarkupId(true);
		twitterAuthCode.setEnabled(false);
		twitterForm.add(twitterAuthCode);

		//save button
		final IndicatingAjaxButton twitterSubmit = new IndicatingAjaxButton("twitterSubmit", twitterForm) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				StringModel stringModel = (StringModel) form.getModelObject();
				String accessCode = stringModel.getString();
				
				if(StringUtils.isBlank(accessCode)) {
					//TODO change this
					target.appendJavaScript("alert('AccessCode was null.');");
					return;
				}
				
				AccessToken accessToken = getOAuthAccessToken(accessCode);				
				if(accessToken == null) {
					//TODO change this
					target.appendJavaScript("alert('AccessToken was null.');");
					return;
				}
				
				//set
				externalIntegrationInfo.setTwitterToken(accessToken.getToken());
				externalIntegrationInfo.setTwitterSecret(accessToken.getTokenSecret());

				//save, replace fragments
				if(externalIntegrationLogic.updateExternalIntegrationInfo(externalIntegrationInfo)) {
					switchContentFragments(linkedFragment(), target);
				} else {
					target.appendJavaScript("alert('Couldn't save info');");
					return;
				}
				
			}
		};
		twitterSubmit.setEnabled(false);
		twitterSubmit.setModel(new ResourceModel("button.link"));
		twitterForm.add(twitterSubmit);
		
		frag.add(twitterForm);
		
		//auth link/label
		final AjaxExternalLink<String> twitterAuthLink = new AjaxExternalLink<String>("twitterAuthLink", getTwitterAuthorisationUrl()) {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				
				//enable code box and button
				twitterAuthCode.setEnabled(true);
				twitterSubmit.setEnabled(true);
				target.add(twitterAuthCode);
				target.add(twitterSubmit);
				
			}
		};
		Label twitterAuthLabel = new Label("twitterAuthLabel", new ResourceModel("twitter.auth.do"));
		twitterAuthLink.add(twitterAuthLabel);
		frag.add(twitterAuthLink);
		frag.setOutputMarkupId(true);
		
		return frag;
	}

	/**
	 * Fragment which returns the components for the linked view
	 * @return
	 */
	private Fragment linkedFragment() {
		
		Fragment frag = new Fragment("fragmentContainer", "linked", this);
		
		//label
		frag.add(new Label("twitterAuthLabel", new ResourceModel("twitter.auth.linked")));
		
		//screen name
		String twitterName = externalIntegrationLogic.getTwitterName(externalIntegrationInfo);
		Label twitterAuthName = new Label("twitterAuthName", new Model<String>(twitterName));
		
		if(StringUtils.isBlank(twitterName)){
			twitterAuthName.setDefaultModel(new ResourceModel("error.twitter.details.invalid"));
		}
		frag.add(twitterAuthName);

		//remove link
		IndicatingAjaxLink<String> twitterAuthRemoveLink  = new IndicatingAjaxLink<String>("twitterAuthRemoveLink") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				externalIntegrationInfo.setTwitterToken(null);
				externalIntegrationInfo.setTwitterSecret(null);
				
				//remove details
				if(externalIntegrationLogic.updateExternalIntegrationInfo(externalIntegrationInfo)) {
					switchContentFragments(unlinkedFragment(), target);
				} else {
					target.appendJavaScript("alert('Couldn't remove info');");
					return;
				}
			}
		};
		
		ContextImage twitterAuthRemoveIcon = new ContextImage("twitterAuthRemoveIcon",new Model<String>(ProfileConstants.CROSS_IMG));
		twitterAuthRemoveLink.add(twitterAuthRemoveIcon);
		twitterAuthRemoveLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.unlinktwitter")));
		frag.add(twitterAuthRemoveLink);
		
		frag.setOutputMarkupId(true);
		
		return frag;
	}

	/**
	 * Helper to switch content fragments for us
	 * 
	 * @param replacement	replacement Fragment
	 * @param target		AjaxRequestTarget
	 */
	private void switchContentFragments(Fragment replacement, AjaxRequestTarget target) {
		
		replacement.setOutputMarkupId(true);
		currentFragment.replaceWith(replacement);
		if(target != null) {
			target.add(replacement);
			//resize iframe
			target.appendJavaScript("setMainFrameHeight(window.name);");
		}
		
		//must keep reference up to date
		currentFragment=replacement;
	}

	/**
	 * Helper to get and set the Twitter request token we need for linking accounts
	 */
	private void setTwitterRequestToken() {
		
		Map<String,String> config = externalIntegrationLogic.getTwitterOAuthConsumerDetails();
		
		Twitter twitter = new TwitterFactory().getInstance();
	    twitter.setOAuthConsumer(config.get("key"), config.get("secret"));
	    
	    try {
			requestToken = twitter.getOAuthRequestToken();
		} catch (TwitterException e) {
			log.error(e.getMessage(), e);
		}
	   
	}

	/**
	 * Helper to get the user access token from the request token and supplied access code
	 * @param accessCode
	 * @return
	 */
	private AccessToken getOAuthAccessToken(String accessCode) {
		
		Map<String,String> config = externalIntegrationLogic.getTwitterOAuthConsumerDetails();

		Twitter twitter = new TwitterFactory().getInstance();
	    twitter.setOAuthConsumer(config.get("key"), config.get("secret"));
	    
	    try {
			return twitter.getOAuthAccessToken(requestToken, accessCode);
		} catch (TwitterException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Helper to get the Twitter auth url
	 * @return
	 */
	private String getTwitterAuthorisationUrl() {
		
		if(requestToken == null) {
			return null;
		}
		return requestToken.getAuthenticationURL();
	}

}
