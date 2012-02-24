/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.tool.pages;

import java.util.Locale;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileExternalIntegrationLogic;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.logic.ProfileKudosLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfileMessagingLogic;
import org.sakaiproject.profile2.logic.ProfilePreferencesLogic;
import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.ProfileSearchLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

import wicket.contrib.tinymce.settings.TinyMCESettings;


public class BasePage extends WebPage implements IHeaderContributor {

	private static final Logger log = Logger.getLogger(BasePage.class); 
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	protected ProfileLogic profileLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	protected ProfilePreferencesLogic preferencesLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	protected ProfilePrivacyLogic privacyLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	protected ProfileConnectionsLogic connectionsLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileMessagingLogic")
	protected ProfileMessagingLogic messagingLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileImageLogic")
	protected ProfileImageLogic imageLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileKudosLogic")
	protected ProfileKudosLogic kudosLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileExternalIntegrationLogic")
	protected ProfileExternalIntegrationLogic externalIntegrationLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	protected ProfileWallLogic wallLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileSearchLogic")
	protected ProfileSearchLogic searchLogic;
	
	Link<Void> myPicturesLink;
	Link<Void> myProfileLink;
	Link<Void> myFriendsLink;
	Link<Void> myMessagesLink;
	Link<Void> myPrivacyLink;
	Link<Void> searchLink;
	Link<Void> preferencesLink;
	
	
	public BasePage() {
		//super();
		
		log.debug("BasePage()");
		
		//set Locale - all pages will inherit this.
		setUserPreferredLocale();
		
		//get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		
    	//profile link
    	myProfileLink = new Link<Void>("myProfileLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MyProfile());
			}
		};
		myProfileLink.add(new Label("myProfileLabel",new ResourceModel("link.my.profile")));
		myProfileLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.profile.tooltip")));
		add(myProfileLink);
		
		//my pictures link
		myPicturesLink = new Link<Void>("myPicturesLink") {
			private static final long serialVersionUID = 1L;

			public void onClick() {
				this.setEnabled(false);
				setResponsePage(new MyPictures());
			}
		};
		myPicturesLink.add(new Label("myPicturesLabel", new ResourceModel("link.my.pictures")));
		myPicturesLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.pictures.tooltip")));

		if (!sakaiProxy.isProfileGalleryEnabledGlobally()) {
			myPicturesLink.setVisible(false);
		}

		add(myPicturesLink);
		
		
		//my friends link
    	myFriendsLink = new Link<Void>("myFriendsLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MyFriends());
			}
		};
		myFriendsLink.add(new Label("myFriendsLabel",new ResourceModel("link.my.friends")));
		myFriendsLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.friends.tooltip")));
		
		//get count of new connection requests
		int newRequestsCount = connectionsLogic.getConnectionRequestsForUserCount(currentUserUuid);
		Label newRequestsLabel = new Label("newRequestsLabel", new Model<Integer>(newRequestsCount));
		myFriendsLink.add(newRequestsLabel);

		if(newRequestsCount == 0) {
			newRequestsLabel.setVisible(false);
		}
		add(myFriendsLink);
		
		
		//messages link
    	myMessagesLink = new Link<Void>("myMessagesLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MyMessages());
			}
		};
		myMessagesLink.add(new Label("myMessagesLabel",new ResourceModel("link.my.messages")));
		myMessagesLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.messages.tooltip")));
		
		//get count of new messages grouped by thread
		int newMessagesCount = messagingLogic.getThreadsWithUnreadMessagesCount(currentUserUuid);
		Label newMessagesLabel = new Label("newMessagesLabel", new Model<Integer>(newMessagesCount));
		myMessagesLink.add(newMessagesLabel);

		if(newMessagesCount == 0) {
			newMessagesLabel.setVisible(false);
		}
		add(myMessagesLink);
		

		//privacy link
    	myPrivacyLink = new Link<Void>("myPrivacyLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MyPrivacy());
			}
		};
		myPrivacyLink.add(new Label("myPrivacyLabel",new ResourceModel("link.my.privacy")));
		myPrivacyLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.privacy.tooltip")));
		add(myPrivacyLink);
		
		
		//search link
    	searchLink = new Link<Void>("searchLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MySearch());
			}
		};
		searchLink.add(new Label("searchLabel",new ResourceModel("link.my.search")));
		searchLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.search.tooltip")));
		add(searchLink);
		
		
		//preferences link
    	preferencesLink = new Link<Void>("preferencesLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new MyPreferences());
			}
		};
		preferencesLink.add(new Label("preferencesLabel",new ResourceModel("link.my.preferences")));
		preferencesLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.preferences.tooltip")));
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
	public void renderHead(IHeaderResponse response) {
		//get Sakai skin
		String skinRepo = sakaiProxy.getSkinRepoProperty();
		String toolCSS = sakaiProxy.getToolSkinCSS(skinRepo);
		String toolBaseCSS = skinRepo + "/tool_base.css";
		
		//Sakai additions
		response.renderJavascriptReference("/library/js/headscripts.js");
		response.renderCSSReference(toolBaseCSS);
		response.renderCSSReference(toolCSS);
		response.renderOnLoadJavascript("setMainFrameHeight( window.name )");
		
		//for jQuery
		response.renderJavascriptReference("javascript/jquery-1.4.4.min.js");
			
		//for datepicker
		response.renderCSSReference("css/flora.datepicker.css");
		response.renderJavascriptReference("javascript/jquery.ui.core-1.5.2.min.js");
		response.renderJavascriptReference("javascript/jquery.datepicker-1.5.2.min.js");

		//for cluetip
		response.renderCSSReference("css/jquery.cluetip.css");
		response.renderJavascriptReference("javascript/jquery.dimensions.js");
		response.renderJavascriptReference("javascript/jquery.hoverIntent.min.js");
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
		
		//for i18n plugin
		response.renderJavascriptReference("javascript/jquery.i18n.properties-min.js");
		
		//for text counter
		response.renderJavascriptReference("javascript/jquery.apTextCounter.min.js");
		
		//Tool additions (at end so we can override if required)
		response.renderString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
		response.renderCSSReference("css/profile2.css");
		response.renderJavascriptReference("javascript/profile2.js");
		
	}
	
	/* disable caching
	protected void setHeaders(WebResponse response) { 
		response.setHeader("Pragma", "no-cache"); 
		response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store"); 
    } 
	*/
	
	/**
	 * Allow overrides of the user's locale
	 */
	public void setUserPreferredLocale() {
		Locale locale = ProfileUtils.getUserPreferredLocale();
		log.debug("User preferred locale: " + locale);
		getSession().setLocale(locale);
	}
	
	/**
	 * Allow Pages to set the title
	 * @param model
	 */
	/*
	protected void setPageTitle(IModel model) {  
		get("pageTitle").setDefaultModel(model);  
	} 
	*/ 
	
	/** 
	 * Disable a page nav link (PRFL-468)
	 */
	protected void disableLink(Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current-tab"), " "));
		l.setEnabled(false);
	}
	
	/**
	 * Set the cookie that stores the current tab index.
	 * 
	 * @param tabIndex the current tab index.
	 */
	protected void setTabCookie(int tabIndex) {
		
		Cookie tabCookie = new Cookie(ProfileConstants.TAB_COOKIE, "" + tabIndex);
		// don't persist indefinitely
		tabCookie.setMaxAge(-1);
		getWebRequestCycle().getWebResponse().addCookie(tabCookie);
	}
	
}
