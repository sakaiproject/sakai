/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.tool.pages;

import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbAccessDeniedException;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import lombok.extern.slf4j.Slf4j;

/**
 * Base page for our app
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class BasePage extends WebPage {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	@SpringBean(name = "org.sakaiproject.rubrics.api.RubricsService")
	protected RubricsService rubricsService;

	@SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
	protected ServerConfigurationService serverConfigService;

	@SpringBean(name = "org.sakaiproject.user.api.PreferencesService")
	protected PreferencesService preferencesService;

	@SpringBean(name = "org.sakaiproject.tool.api.ToolManager")
	protected ToolManager toolManager;

	@SpringBean(name = "org.sakaiproject.user.api.UserDirectoryService")
	protected UserDirectoryService userDirectoryService;

	public static final String GB_PREF_KEY = "GBNG-";

	Link<Void> gradebookPageLink;
	Link<Void> settingsPageLink;
	Link<Void> importExportPageLink;
	Link<Void> permissionsPageLink;
	Link<Void> quickEntryPageLink;

	public final GbFeedbackPanel feedbackPanel;

	/**
	 * The current user
	 */
	protected String currentUserUuid;

	/**
	 * The user's role in the site
	 */
	protected GbRole role;

	public BasePage() {
		log.debug("BasePage()");

		// setup some data that can be shared across all pages
		this.currentUserUuid = this.businessService.getCurrentUser().getId();
		role = GbRole.NONE;
		try {
			this.role = this.businessService.getUserRole(getCurrentSiteId());
		} catch (final GbAccessDeniedException e) {
			log.error("Error getting user role", e);
			// do not redirect here, let the subclasses handle this!
		}

		// set locale
		setUserPreferredLocale();

		// nav container
		final WebMarkupContainer nav = new WebMarkupContainer("gradebookPageNav") {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return (BasePage.this.role == GbRole.INSTRUCTOR || BasePage.this.role == GbRole.TA);
			}

		};

		nav.setOutputMarkupId(true);
		nav.setMarkupId("gradebook-navbar");

		// grades page
		this.gradebookPageLink = new BookmarkablePageLink<Void>("gradebookPageLink", GradebookPage.class) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return (BasePage.this.role == GbRole.INSTRUCTOR || BasePage.this.role == GbRole.TA);
			}

		};
		this.gradebookPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.gradebookPageLink);

		// import/export page
		this.importExportPageLink = new BookmarkablePageLink<Void>("importExportPageLink", ImportExportPage.class) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return (businessService.isUserAbleToEditAssessments(getCurrentSiteId()));
			}
		};
		this.importExportPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.importExportPageLink);

		// permissions page
		this.permissionsPageLink = new BookmarkablePageLink<Void>("permissionsPageLink", PermissionsPage.class) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return (BasePage.this.role == GbRole.INSTRUCTOR);
			}
		};
		this.permissionsPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.permissionsPageLink);

		// settings page
		this.settingsPageLink = new BookmarkablePageLink<Void>("settingsPageLink", SettingsPage.class) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return (businessService.isUserAbleToEditAssessments(getCurrentSiteId()));
			}
		};
		this.settingsPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.settingsPageLink);

		// quick entry page
		this.quickEntryPageLink = new BookmarkablePageLink<Void>("quickEntryPageLink", QuickEntryPage.class) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return (businessService.isUserAbleToEditAssessments(getCurrentSiteId()));
			}
		};
		this.quickEntryPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.quickEntryPageLink);

		add(nav);

		// Add a FeedbackPanel for displaying our messages
		this.feedbackPanel = new GbFeedbackPanel("feedback");
		add(this.feedbackPanel);

	}

	/**
	 * Helper to clear the feedback panel display from any child component
	 */
	public void clearFeedback() {
		this.feedbackPanel.clear();
	}

	/**
	 * This block adds the required wrapper markup to style it like a Sakai tool. Add to this any additional CSS or JS references that you
	 * need.
	 *
	 */
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		final String version = PortalUtils.getCDNQuery();

		// get the Sakai skin header fragment from the request attribute
		final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();

		response.render(new PriorityHeaderItem(JavaScriptHeaderItem
				.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference())));

		response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
		response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));

		// Tool additions (at end so we can override if required)
		response.render(StringHeaderItem
				.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));

		// Shared JavaScript and stylesheets
		// Force Wicket to use Sakai's version of jQuery
		response.render(
				new PriorityHeaderItem(
						JavaScriptHeaderItem
								.forUrl(String.format("/library/webjars/jquery/1.12.4/jquery.min.js%s", version))));
		response.render(
				new PriorityHeaderItem(
						JavaScriptHeaderItem
								.forUrl(String.format("/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js%s", version))));											
	}

	/**
	 * Helper to disable a link. Add the Sakai class 'current'.
	 */
	protected final void disableLink(final Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
		l.add(new AttributeModifier("aria-current", "page"));
		l.replace(new Label("screenreaderlabel", getString("link.screenreader.current.page")));
		l.setEnabled(false);
	}

	/**
	 * Helper to build a notification flag with a Bootstrap popover
	 */
	public WebMarkupContainer buildFlagWithPopover(final String componentId, final String message) {
		return buildFlagWithPopover(componentId, message, "manual", "#gradebookGrades");
	}

	public WebMarkupContainer buildFlagWithPopover(final String componentId, final String message,
			final String trigger, final String container) {
		final WebMarkupContainer flagWithPopover = new WebMarkupContainer(componentId);

		flagWithPopover.add(new AttributeModifier("title", message));
		flagWithPopover.add(new AttributeModifier("aria-label", message));
		flagWithPopover.add(new AttributeModifier("data-bs-toggle", "popover"));
		flagWithPopover.add(new AttributeModifier("data-trigger", trigger));
		flagWithPopover.add(new AttributeModifier("data-placement", "bottom"));
		flagWithPopover.add(new AttributeModifier("data-html", "true"));
		flagWithPopover.add(new AttributeModifier("data-container", container));
		flagWithPopover.add(new AttributeModifier("data-template",
				"<div class=\"gb-popover popover\" role=\"tooltip\"><div class=\"arrow\"></div><div class=\"popover-content\"></div></div>"));
		flagWithPopover.add(new AttributeModifier("data-content", generatePopoverContent(message)));
		flagWithPopover.add(new AttributeModifier("tabindex", "0"));

		return flagWithPopover;
	}

	/**
	 * Helper to generate content for a Bootstrap popover with close button
	 */
	public String generatePopoverContent(final String message) {
		final String popoverHTML = "<a href='javascript:void(0);' class='gb-popover-close'></a><ul class='gb-popover-notifications'><li class='text-info'>%s</li></ul>";
		final String wrappedPopoverContent = String.format(popoverHTML, message);

		return wrappedPopoverContent;
	}

	/**
	 * Allow overrides of the user's locale
	 */
	public void setUserPreferredLocale() {
		final Locale locale = this.businessService.getUserPreferredLocale();
		log.debug("User preferred locale: " + locale);
		getSession().setLocale(locale);
	}

	/**
	 * Send a user to the access denied page with a message
	 * 
	 * @param message the message
	 */
	public final void sendToAccessDeniedPage(final String message) {
		final PageParameters params = new PageParameters();
		params.add("message", message);
		log.debug("Redirecting to AccessDeniedPage: " + message);
		throw new RestartResponseException(AccessDeniedPage.class, params);
	}

	public GbRole getCurrentRole() {
		return BasePage.this.role;
	}

	/**
	 * Performs role checks for instructor-only pages and redirects users to appropriate pages based on their role.
	 * No role -> AccessDeniedPage. Student -> StudentPage. TA -> GradebookPage (if ta does not have the gradebook.editAssignments permission)
	 */
	protected final void defaultRoleChecksForInstructorOnlyPage()
	{
		switch (role)
		{
			case NONE:
				sendToAccessDeniedPage(getString("error.role"));
				break;
			case STUDENT:
				throw new RestartResponseException(StudentPage.class);
			default:
				if(businessService.isUserAbleToEditAssessments(getCurrentSiteId())) {
					break;
				}
				throw new RestartResponseException(GradebookPage.class);
		}
	}

	public String getCurrentSiteId() {
		try {
			return this.toolManager.getCurrentPlacement().getContext();
		} catch (final Exception e) {
			return null;
		}
	}

	public String getCurrentGradebookUid() {
		String gradebookUid = getCurrentSiteId();
		Placement placement = toolManager.getCurrentPlacement();
		Properties props = placement.getPlacementConfig();
		if (props.getProperty("gb-group") != null) {
			gradebookUid = props.getProperty("gb-group");
		}

		return gradebookUid;
	}

	public User getCurrentUser() {
		return this.userDirectoryService.getCurrentUser();
	}

	/**
	 * Get the user's custom GbUiSettings from PreferencesService
	 *
	 * @return String
	 */
	public String getUserGbPreference(final String prefName) {
		final String siteId = getCurrentSiteId();
		final String currentUserId = getCurrentUser().getId();
		Preferences userPrefs = preferencesService.getPreferences(currentUserId);
		ResourceProperties rp = userPrefs.getProperties(GB_PREF_KEY + siteId);
		return rp.getProperty(prefName);
	}

	/**
	 * Set the user's custom GbUiSettings in PreferencesService
	 *
	 * @return
	 */
	public void setUserGbPreference(final String prefName, final String prefValue) {
		if (StringUtils.isBlank(prefName)) return;
		String siteId = getCurrentSiteId();
		String userId = getCurrentUser().getId();

		preferencesService.applyEditWithAutoCommit(userId, edit -> {
			String key = GB_PREF_KEY + siteId;
			ResourcePropertiesEdit props = edit.getPropertiesEdit(key);
			if (prefValue != null) {
				props.addProperty(prefName, prefValue);
			} else {
				props.removeProperty(prefName);
			}
		});
	}
}
