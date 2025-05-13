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

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.profile2.logic.ProfileExternalIntegrationLogic;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfilePreferencesLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.util.ProfileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BasePage extends WebPage implements IHeaderContributor {

	@SpringBean(name = "org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileLogic")
	protected ProfileLogic profileLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	protected ProfilePreferencesLogic preferencesLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileImageLogic")
	protected ProfileImageLogic imageLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileExternalIntegrationLogic")
	protected ProfileExternalIntegrationLogic externalIntegrationLogic;

	Link<Void> myProfileLink;
	Link<Void> otherProfileLink;
	Link<Void> preferencesLink;
	
	WebMarkupContainer myProfileContainer;
	WebMarkupContainer otherProfileContainer;
	WebMarkupContainer preferencesContainer;

	public BasePage() {
		// super();

		log.debug("BasePage()");

		// set Locale - all pages will inherit this.
		setUserPreferredLocale();

		// PRFL-791 set base HTML lang attribute
		// add(new LocaleAwareHtmlTag("html"));

		// get currentUserUuid
		final String currentUserUuid = this.sakaiProxy.getCurrentUserId();

		// my profile link and container
		myProfileContainer = new WebMarkupContainer("myProfileContainer");
		this.myProfileLink = new Link<Void>("myProfileLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new MyProfile());
			}
		};
		this.myProfileLink.add(new Label("myProfileLabel", new ResourceModel("link.my.profile")));
		this.myProfileLink.add(new AttributeModifier("title", new ResourceModel("link.my.profile.tooltip")));
		myProfileContainer.add(this.myProfileLink);

		if (!this.sakaiProxy.isMenuEnabledGlobally()) {
			this.myProfileLink.setVisible(false);
		}

		add(myProfileContainer);

		// other profile link and container
		otherProfileContainer = new WebMarkupContainer("otherProfileContainer");
		this.otherProfileLink = new Link<Void>("otherProfileLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
			}
		};
		this.otherProfileLink.add(new Label("otherProfileLabel", new Model("INVISIBLE")));
		this.otherProfileLink.setVisible(false);
		otherProfileContainer.add(this.otherProfileLink);
		add(otherProfileContainer);

		// preferences link and container
		preferencesContainer = new WebMarkupContainer("preferencesContainer");
		this.preferencesLink = new Link<Void>("preferencesLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new MyPreferences());
			}
		};
		this.preferencesLink.add(new Label("preferencesLabel", new ResourceModel("link.my.preferences")));
		this.preferencesLink.add(new AttributeModifier("title", new ResourceModel("link.my.preferences.tooltip")));
		preferencesContainer.add(this.preferencesLink);

		if (!this.sakaiProxy.isPreferenceEnabledGlobally()) {
			this.preferencesLink.setVisible(false);
		}
		add(preferencesContainer);


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

		String version = PortalUtils.getCDNQuery();

		// Tool additions (at end so we can override if required)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/profile2-tool/javascript/profile2.js%s", version)));

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
        log.debug("User preferred locale: {}", locale);
		getSession().setLocale(locale);
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
