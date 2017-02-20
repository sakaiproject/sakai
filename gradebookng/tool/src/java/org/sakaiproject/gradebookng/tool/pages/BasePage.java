package org.sakaiproject.gradebookng.tool.pages;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbAccessDeniedException;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;

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

	Link<Void> gradebookPageLink;
	Link<Void> settingsPageLink;
	Link<Void> importExportPageLink;
	Link<Void> permissionsPageLink;

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
		try {
			this.role = this.businessService.getUserRole();
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

		// grades page
		this.gradebookPageLink = new Link<Void>("gradebookPageLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(GradebookPage.class);
			}

			@Override
			public boolean isVisible() {
				return (BasePage.this.role == GbRole.INSTRUCTOR || BasePage.this.role == GbRole.TA);
			}

		};
		this.gradebookPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.gradebookPageLink);

		// import/export page
		this.importExportPageLink = new Link<Void>("importExportPageLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ImportExportPage.class);
			}

			@Override
			public boolean isVisible() {
				return (BasePage.this.role == GbRole.INSTRUCTOR);
			}
		};
		this.importExportPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.importExportPageLink);

		// permissions page
		this.permissionsPageLink = new Link<Void>("permissionsPageLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(PermissionsPage.class);
			}

			@Override
			public boolean isVisible() {
				return (BasePage.this.role == GbRole.INSTRUCTOR);
			}
		};
		this.permissionsPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.permissionsPageLink);

		// settings page
		this.settingsPageLink = new Link<Void>("settingsPageLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(SettingsPage.class);
			}

			@Override
			public boolean isVisible() {
				return (BasePage.this.role == GbRole.INSTRUCTOR);
			}
		};
		this.settingsPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.settingsPageLink);

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

		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

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
					.forUrl(String.format("/library/webjars/jquery/1.11.3/jquery.min.js?version=%s", version))));
		// And pair this instance of jQuery with a Bootstrap version we've tested with
		response.render(
			new PriorityHeaderItem(
				JavaScriptHeaderItem
					.forUrl(String.format("/library/webjars/bootstrap/3.3.7/js/bootstrap.min.js?version=%s", version))));
		// Some global gradebookng styles
		response.render(CssHeaderItem
			.forUrl(String.format("/gradebookng-tool/styles/gradebook-shared.css?version=%s", version)));

	}

	/**
	 * Helper to disable a link. Add the Sakai class 'current'.
	 */
	protected void disableLink(final Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
		l.replace(new Label("screenreaderlabel", getString("link.screenreader.tabselected")));
		l.setEnabled(false);
	}

	/**
	 * Helper to build a notification flag with a Bootstrap popover
	 */
	public WebMarkupContainer buildFlagWithPopover(final String componentId, final String message) {
		final WebMarkupContainer flagWithPopover = new WebMarkupContainer(componentId);

		flagWithPopover.add(new AttributeModifier("title", message));
		flagWithPopover.add(new AttributeModifier("aria-label", message));
		flagWithPopover.add(new AttributeModifier("data-toggle", "popover"));
		flagWithPopover.add(new AttributeModifier("data-trigger", "manual"));
		flagWithPopover.add(new AttributeModifier("data-placement", "bottom"));
		flagWithPopover.add(new AttributeModifier("data-html", "true"));
		flagWithPopover.add(new AttributeModifier("data-container", "#gradebookGrades"));
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
	 * @param message the message
	 */
	public void sendToAccessDeniedPage(final String message){
		final PageParameters params = new PageParameters();
		params.add("message", message);
		log.debug("Redirecting to AccessDeniedPage: " + message);
		throw new RestartResponseException(AccessDeniedPage.class, params);
	}
}
