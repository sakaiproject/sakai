package org.sakaiproject.gradebookng.tool.pages;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;

/**
 * Base page for our app
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class BasePage extends WebPage {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(BasePage.class);

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	Link<Void> gradebookPageLink;
	Link<Void> settingsPageLink;
	Link<Void> importExportPageLink;
	Link<Void> permissionsPageLink;

	final FeedbackPanel feedbackPanel;

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
		this.role = this.businessService.getUserRole();

		//

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
				setResponsePage(new GradebookPage());
			}

			@Override
			public boolean isVisible() {
				return (BasePage.this.role == GbRole.INSTRUCTOR || BasePage.this.role == GbRole.TA);
			}

		};
		this.gradebookPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.gradebookPageLink);

		// settings page
		this.settingsPageLink = new Link<Void>("settingsPageLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new SettingsPage());
			}

			@Override
			public boolean isVisible() {
				return (BasePage.this.role == GbRole.INSTRUCTOR);
			}
		};
		this.settingsPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.settingsPageLink);

		// import/export page
		this.importExportPageLink = new Link<Void>("importExportPageLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new ImportExportPage());
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
				setResponsePage(new PermissionsPage());
			}

			@Override
			public boolean isVisible() {
				return (BasePage.this.role == GbRole.INSTRUCTOR);
			}
		};
		this.permissionsPageLink.add(new Label("screenreaderlabel", getString("link.screenreader.tabnotselected")));
		nav.add(this.permissionsPageLink);

		add(nav);

		// Add a FeedbackPanel for displaying our messages
		this.feedbackPanel = new GbFeedbackPanel("feedback");
		add(this.feedbackPanel);

	}

	/**
	 * Helper to clear the feedbackpanel display.
	 *
	 * @param f FeedBackPanel
	 */
	public void clearFeedback(final FeedbackPanel f) {
		if (!f.hasFeedbackMessage()) {
			f.add(AttributeModifier.remove("class"));
		}
	}

	/**
	 * This block adds the required wrapper markup to style it like a Sakai tool. Add to this any additional CSS or JS references that you
	 * need.
	 *
	 */
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		// get the Sakai skin header fragment from the request attribute
		final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();

		response.render(new PriorityHeaderItem(
				JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference())));

		response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
		response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));

		// Tool additions (at end so we can override if required)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));

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
		flagWithPopover.add(new AttributeModifier("data-toggle", "popover"));
		flagWithPopover.add(new AttributeModifier("data-trigger", "manual"));
		flagWithPopover.add(new AttributeModifier("data-placement", "bottom"));
		flagWithPopover.add(new AttributeModifier("data-html", "true"));
		flagWithPopover.add(new AttributeModifier("data-container", "#gradebookGrades"));
		flagWithPopover.add(new AttributeModifier("data-template",
				"'<div class=\"gb-popover popover\" role=\"tooltip\"><div class=\"arrow\"></div><div class=\"popover-content\"></div></div>'"));
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
}
