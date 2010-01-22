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

package org.sakaiproject.profile2.tool.pages;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.SessionManager;

import wicket.contrib.tinymce.settings.TinyMCESettings;


public class BasePage extends WebPage implements IHeaderContributor {

	private static final Logger log = Logger.getLogger(BasePage.class); 
	protected transient SakaiProxy sakaiProxy;
	protected transient ProfileLogic profileLogic;
	
	public BasePage() {
		//super();
		
		log.debug("BasePage()");
		
		//get SakaiProxy API
		sakaiProxy = getSakaiProxy();
		
		//get ProfileLogic API
		profileLogic = getProfileLogic();
		
		//set Locale - all pages will inherit this.
		setUserPreferredLocale();
		
		//get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		
		
    	//profile link
    	Link<Void> myProfileLink = new Link<Void>("myProfileLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MyProfile());
			}
		};
		myProfileLink.add(new Label("myProfileLabel",new ResourceModel("link.my.profile")));
		add(myProfileLink);
		
		
		//my pictures link
		Link<Void> myPicturesLink = new Link<Void>("myPicturesLink") {
			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new MyPictures());
			}
		};
		myPicturesLink.add(new Label("myPicturesLabel", new ResourceModel(
				"link.my.pictures")));

		if (!sakaiProxy.isProfileGalleryEnabledGlobally()) {
			myPicturesLink.setVisible(false);
		}

		add(myPicturesLink);
		
		
		//my friends link
    	Link<Void> myFriendsLink = new Link<Void>("myFriendsLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MyFriends());
			}
		};
		myFriendsLink.add(new Label("myFriendsLabel",new ResourceModel("link.my.friends")));
		
		//get count of new connection requests
		int newRequestsCount = profileLogic.getConnectionRequestsForUserCount(currentUserUuid);
		Label newRequestsLabel = new Label("newRequestsLabel", new Model<Integer>(newRequestsCount));
		myFriendsLink.add(newRequestsLabel);

		if(newRequestsCount == 0) {
			newRequestsLabel.setVisible(false);
		}
		add(myFriendsLink);
		
		
		//messages link
    	Link<Void> myMessagesLink = new Link<Void>("myMessagesLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MyMessageThreads());
			}
		};
		myMessagesLink.add(new Label("myMessagesLabel",new ResourceModel("link.my.messages")));
		
		//get count of new messages grouped by thread
		int newMessagesCount = profileLogic.getThreadsWithUnreadMessagesCount(currentUserUuid);
		Label newMessagesLabel = new Label("newMessagesLabel", new Model<Integer>(newMessagesCount));
		myMessagesLink.add(newMessagesLabel);

		if(newMessagesCount == 0) {
			newMessagesLabel.setVisible(false);
		}
		add(myMessagesLink);
		

		//privacy link
    	Link<Void> myPrivacyLink = new Link<Void>("myPrivacyLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MyPrivacy());
			}
		};
		myPrivacyLink.add(new Label("myPrivacyLabel",new ResourceModel("link.my.privacy")));
		add(myPrivacyLink);
		
		
		//search link
    	Link<Void> searchLink = new Link<Void>("searchLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MySearch());
			}
		};
		searchLink.add(new Label("searchLabel",new ResourceModel("link.search")));
		add(searchLink);
		
		
		//preferences link
    	Link<Void> preferencesLink = new Link<Void>("preferencesLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MyPreferences());
			}
		};
		preferencesLink.add(new Label("preferencesLabel",new ResourceModel("link.preferences")));
		add(preferencesLink);
			
		
		//rss link
		/*
		ContextImage icon = new ContextImage("icon",new Model(ProfileImageManager.RSS_IMG));
		Link rssLink = new Link("rssLink") {
			public void onClick() {
			}
		};
		rssLink.add(icon);
		rssLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.rss")));
		icon.setVisible(true);
		add(rssLink);
		*/
		
    }
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	//Style it like a Sakai tool
	protected static final String HEADSCRIPTS = "/library/js/headscripts.js";
	protected static final String BODY_ONLOAD_ADDTL="setMainFrameHeight( window.name )";
	
	public void renderHead(IHeaderResponse response) {
		//get Sakai skin
		String skinRepo = ServerConfigurationService.getString("skin.repo");
		String toolCSS = getToolSkinCSS(skinRepo);
		String toolBaseCSS = skinRepo + "/tool_base.css";
		
		//Sakai additions
		response.renderJavascriptReference(HEADSCRIPTS);
		response.renderCSSReference(toolBaseCSS);
		response.renderCSSReference(toolCSS);
		response.renderOnLoadJavascript(BODY_ONLOAD_ADDTL);
		
		//for jQuery
		response.renderJavascriptReference("javascript/jquery-1.2.5.min.js");
			
		//for datepicker
		response.renderCSSReference("css/flora.datepicker.css");
		response.renderJavascriptReference("javascript/jquery.ui.core-1.5.2.min.js");
		response.renderJavascriptReference("javascript/jquery.datepicker-1.5.2.min.js");

		//for cluetip
		response.renderCSSReference("css/jquery.cluetip.css");
		response.renderJavascriptReference("javascript/jquery.dimensions.js");
		response.renderJavascriptReference("javascript/jquery.hoverIntent.js");
		response.renderJavascriptReference("javascript/jquery.cluetip.js");
		
		//for color plugin
		//response.renderJavascriptReference("javascript/jquery.color.js");
		
		//wicketstuff TinyMCE
		response.renderJavascriptReference(TinyMCESettings.javaScriptReference());

		//response.renderJavascriptReference("javascript/iframe.js");
		//resize the iframe to fit the contents
		//response.renderOnLoadJavascript("setMainFrameHeight(window.name);");
		
		//add live plugin to listen for markup added after the DOM is ready
		//response.renderJavascriptReference("javascript/jquery.livequery.js");
		
		//Tool additions (at end so we can override if required)
		response.renderString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
		response.renderCSSReference("css/profile2.css");
		response.renderJavascriptReference("javascript/profile2.js");
		
	}
	
	protected String getToolSkinCSS(String skinRepo) {
		String skin = null;
		try {
			skin = SiteService.findTool(SessionManager.getCurrentToolSession().getPlacementId()).getSkin();			
		}
		catch(Exception e) {
			skin = ServerConfigurationService.getString("skin.default");
		}
		
		if(skin == null) {
			skin = ServerConfigurationService.getString("skin.default");
		}
		
		return skinRepo + "/" + skin + "/tool.css";
	}
	
	/*
	protected Label newResourceLabel(String id, Component component) {
		return new Label(id, new StringResourceModel(id, component, null));
	}
	
	public String getResourceModel(String resourceKey, IModel model) {
		return new StringResourceModel(resourceKey, this, model).getString();
	}
	*/
	
	public BasePage getBasePage() {
		return this;
	}

	
	/* helper methods for our child pages to get at the API's */
	protected SakaiProxy getSakaiProxy() {
		return Locator.getSakaiProxy();
	}

	protected ProfileLogic getProfileLogic() {
		return Locator.getProfileLogic();
	}

	
	/* disable caching
	protected void setHeaders(WebResponse response) { 
		response.setHeader("Pragma", "no-cache"); 
		response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store"); 
    } 
	*/
	
	public void setUserPreferredLocale() {
		Locale locale = ProfileUtils.getUserPreferredLocale();
		log.debug("User preferred locale: " + locale);
		getSession().setLocale(locale);
	}
	
	
	
	
	
}
