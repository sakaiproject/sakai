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
package org.sakaiproject.profile2.tool.pages;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.cookies.CookieDefaults;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.string.StringValue;

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

@Slf4j
public class BasePage extends WebPage implements IHeaderContributor {

	@SpringBean(name = "org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileLogic")
	protected ProfileLogic profileLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	protected ProfilePreferencesLogic preferencesLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	protected ProfilePrivacyLogic privacyLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	protected ProfileConnectionsLogic connectionsLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileMessagingLogic")
	protected ProfileMessagingLogic messagingLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileImageLogic")
	protected ProfileImageLogic imageLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileKudosLogic")
	protected ProfileKudosLogic kudosLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileExternalIntegrationLogic")
	protected ProfileExternalIntegrationLogic externalIntegrationLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileWallLogic")
	protected ProfileWallLogic wallLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileSearchLogic")
	protected ProfileSearchLogic searchLogic;

	Link<Void> myPicturesLink;
	Link<Void> myProfileLink;
	Link<Void> otherProfileLink;
	Link<Void> myFriendsLink;
	Link<Void> myMessagesLink;
	Link<Void> myPrivacyLink;
	Link<Void> searchLink;
	Link<Void> preferencesLink;

	public BasePage() {
		// super();

		log.debug("BasePage()");

		// set Locale - all pages will inherit this.
		setUserPreferredLocale();

		// PRFL-791 set base HTML lang attribute
		// add(new LocaleAwareHtmlTag("html"));

		// get currentUserUuid
		final String currentUserUuid = this.sakaiProxy.getCurrentUserId();

		// my profile link
		this.myProfileLink = new Link<Void>("myProfileLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new MyProfile());
			}
		};
		this.myProfileLink.add(new Label("myProfileLabel", new ResourceModel("link.my.profile")));
		this.myProfileLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.profile.tooltip")));

		if (!this.sakaiProxy.isMenuEnabledGlobally()) {
			this.myProfileLink.setVisible(false);
		}

		add(this.myProfileLink);

		// other profile link
		this.otherProfileLink = new Link<Void>("otherProfileLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
			}
		};
		this.otherProfileLink.add(new Label("otherProfileLabel", new Model("INVISIBLE")));
		this.otherProfileLink.setVisible(false);
		add(this.otherProfileLink);

		// my pictures link
		this.myPicturesLink = new Link<Void>("myPicturesLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setEnabled(false);
				setResponsePage(new MyPictures());
			}
		};
		this.myPicturesLink.add(new Label("myPicturesLabel", new ResourceModel("link.my.pictures")));
		this.myPicturesLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.pictures.tooltip")));

		if (!this.sakaiProxy.isProfileGalleryEnabledGlobally()) {
			this.myPicturesLink.setVisible(false);
		}

		add(this.myPicturesLink);

		// my friends link
		this.myFriendsLink = new Link<Void>("myFriendsLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new MyFriends());
			}
		};
		this.myFriendsLink.add(new Label("myFriendsLabel", new ResourceModel("link.my.friends")));
		this.myFriendsLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.friends.tooltip")));

		// get count of new connection requests
		final int newRequestsCount = this.connectionsLogic.getConnectionRequestsForUserCount(currentUserUuid);
		final Label newRequestsLabel = new Label("newRequestsLabel", new Model<Integer>(newRequestsCount));
		this.myFriendsLink.add(newRequestsLabel);

		if (newRequestsCount == 0) {
			newRequestsLabel.setVisible(false);
		}

		if (!this.sakaiProxy.isConnectionsEnabledGlobally()) {
			this.myFriendsLink.setVisible(false);
		}

		add(this.myFriendsLink);

		// messages link
		this.myMessagesLink = new Link<Void>("myMessagesLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new MyMessages());
			}
		};
		this.myMessagesLink.add(new Label("myMessagesLabel", new ResourceModel("link.my.messages")));
		this.myMessagesLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.messages.tooltip")));

		// get count of new messages grouped by thread
		final int newMessagesCount = this.messagingLogic.getThreadsWithUnreadMessagesCount(currentUserUuid);
		final Label newMessagesLabel = new Label("newMessagesLabel", new Model<Integer>(newMessagesCount));
		this.myMessagesLink.add(newMessagesLabel);

		if (newMessagesCount == 0) {
			newMessagesLabel.setVisible(false);
		}

		if (!this.sakaiProxy.isMessagingEnabledGlobally()) {
			this.myMessagesLink.setVisible(false);
		}
		add(this.myMessagesLink);

		// privacy link
		this.myPrivacyLink = new Link<Void>("myPrivacyLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new MyPrivacy());
			}
		};
		this.myPrivacyLink.add(new Label("myPrivacyLabel", new ResourceModel("link.my.privacy")));
		this.myPrivacyLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.privacy.tooltip")));

		if (!this.sakaiProxy.isPrivacyEnabledGlobally()) {
			this.myPrivacyLink.setVisible(false);
		}

		add(this.myPrivacyLink);

		// search link
		this.searchLink = new Link<Void>("searchLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new MySearch());
			}
		};
		this.searchLink.add(new Label("searchLabel", new ResourceModel("link.my.search")));
		this.searchLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.search.tooltip")));

		if (!this.sakaiProxy.isSearchEnabledGlobally()) {
			this.searchLink.setVisible(false);
		}

		add(this.searchLink);

		// preferences link
		this.preferencesLink = new Link<Void>("preferencesLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new MyPreferences());
			}
		};
		this.preferencesLink.add(new Label("preferencesLabel", new ResourceModel("link.my.preferences")));
		this.preferencesLink.add(new AttributeModifier("title", true, new ResourceModel("link.my.preferences.tooltip")));

		if (!this.sakaiProxy.isPreferenceEnabledGlobally()) {
			this.preferencesLink.setVisible(false);
		}
		add(this.preferencesLink);

		// rss link
		/*
		 * ContextImage icon = new ContextImage("icon",new Model(ProfileImageManager.RSS_IMG)); Link rssLink = new Link("rssLink") { public
		 * void onClick() { } }; rssLink.add(icon); rssLink.add(new AttributeModifier("title", true,new ResourceModel("link.title.rss")));
		 * icon.setVisible(true); add(rssLink);
		 */

	}

	// Style it like a Sakai tool
	@Override
	public void renderHead(final IHeaderResponse response) {

		// get the Sakai skin header fragment from the request attribute
		final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();

		final String sakaiHtmlHead = (String) request.getAttribute("sakai.html.head");
		if (StringUtils.isNotBlank(sakaiHtmlHead)) {
			response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
		}
		response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));

		// Tool additions (at end so we can override if required)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));
		response.render(CssHeaderItem.forUrl("/profile2-tool/css/profile2.css"));
		response.render(JavaScriptHeaderItem.forUrl("/profile2-tool/javascript/profile2.js"));

	}

	/*
	 * disable caching protected void setHeaders(WebResponse response) { response.setHeader("Pragma", "no-cache");
	 * response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store"); }
	 */

	/**
	 * Allow overrides of the user's locale
	 */
	public void setUserPreferredLocale() {
		final Locale locale = ProfileUtils.getUserPreferredLocale();
		log.debug("User preferred locale: " + locale);
		getSession().setLocale(locale);
	}

	/**
	 * Allow Pages to set the title
	 * 
	 * @param model
	 */
	/*
	 * protected void setPageTitle(IModel model) { get("pageTitle").setDefaultModel(model); }
	 */

	/**
	 * Disable a page nav link (PRFL-468)
	 */
	protected void disableLink(final Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
		l.setEnabled(false);
	}

	/**
	 * Set the cookie that stores the current tab index.
	 *
	 * @param tabIndex the current tab index.
	 */
	protected void setTabCookie(final int tabIndex) {

		final CookieDefaults defaults = new CookieDefaults();
		defaults.setMaxAge(-1);

		final CookieUtils utils = new CookieUtils(defaults);
		utils.save(ProfileConstants.TAB_COOKIE, String.valueOf(tabIndex));
	}

	/**
	 * Parse a param
	 *
	 * @param parameters
	 * @param name
	 * @return
	 */
	static String getParamValue(final PageParameters parameters, final String name) {
		final StringValue value = parameters.get(name);
		return (value != null) ? value.toString() : null;
	}

}
