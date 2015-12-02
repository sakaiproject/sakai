package org.sakaiproject.gradebookng.tool.pages;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;


/**
 * Base page for our app
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class BasePage extends WebPage implements IHeaderContributor {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(BasePage.class); 
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
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
		
		//get current user
		currentUserUuid = this.businessService.getCurrentUser().getId();
		
		//role check
		role = this.businessService.getUserRole();
		
		//nav container
		WebMarkupContainer nav = new WebMarkupContainer("gradebookPageNav") {
			
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return (role == GbRole.INSTRUCTOR || role == GbRole.TA);
			}
			
		};
	
    	//grades page
		gradebookPageLink = new Link<Void>("gradebookPageLink") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick() {
				setResponsePage(new GradebookPage());
			}
			
			@Override
			public boolean isVisible() {
				return (role == GbRole.INSTRUCTOR || role == GbRole.TA);
			}
			
		};
		nav.add(gradebookPageLink);
		
		//settings page
		settingsPageLink = new Link<Void>("settingsPageLink") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick() {
				setResponsePage(new SettingsPage());
			}
			
			@Override
			public boolean isVisible() {
				return (role == GbRole.INSTRUCTOR);
			}
		};
		nav.add(settingsPageLink);
		
		//import/export page
		importExportPageLink = new Link<Void>("importExportPageLink") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick() {
				setResponsePage(new ImportExportPage());
			}
			
			@Override
			public boolean isVisible() {
				return (role == GbRole.INSTRUCTOR);
			}
		};
		nav.add(importExportPageLink);
		
		//permissions page
		permissionsPageLink = new Link<Void>("permissionsPageLink") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick() {
				setResponsePage(new PermissionsPage());
			}
			
			@Override
			public boolean isVisible() {
				return (role == GbRole.INSTRUCTOR);
			}
		};
		nav.add(permissionsPageLink);
		
		add(nav);

		// Add a FeedbackPanel for displaying our messages
        feedbackPanel = new FeedbackPanel("feedback"){
        	
			private static final long serialVersionUID = 1L;

			@Override
        	protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
        		final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

        		if(message.getLevel() == FeedbackMessage.ERROR ||
        			message.getLevel() == FeedbackMessage.DEBUG ||
        			message.getLevel() == FeedbackMessage.FATAL ||
        			message.getLevel() == FeedbackMessage.WARNING){
        			newMessageDisplayComponent.add(AttributeModifier.replace("class", "messageError"));
        		} else if(message.getLevel() == FeedbackMessage.INFO){
        			newMessageDisplayComponent.add(AttributeModifier.replace("class", "messageSuccess"));
        		} 

        		return newMessageDisplayComponent;
        	}
        };
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel); 
		
    }
	
	/**
	 * Helper to clear the feedbackpanel display.
	 * @param f	FeedBackPanel
	 */
	public void clearFeedback(FeedbackPanel f) {
		if(!f.hasFeedbackMessage()) {
			f.add(AttributeModifier.remove("class"));
		}
	}
	
	
	
	/**
	 * This block adds the required wrapper markup to style it like a Sakai tool. 
	 * Add to this any additional CSS or JS references that you need.
	 * 
	 */
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		//get the Sakai skin header fragment from the request attribute
		HttpServletRequest request = (HttpServletRequest)getRequest().getContainerRequest();
		
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference())));

		response.render(StringHeaderItem.forString((String)request.getAttribute("sakai.html.head")));
		response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));
		
		//Tool additions (at end so we can override if required)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));
		
	}
	
	
	/** 
	 * Helper to disable a link. Add the Sakai class 'current'.
	 */
	protected void disableLink(Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
		l.setEnabled(false);
	}
	
	
	/**
	 * Helper to build a notification flag with a Bootstrap popover 
	 */
	public WebMarkupContainer buildFlagWithPopover(String componentId, String popoverContent) {
		WebMarkupContainer flagWithPopover = new WebMarkupContainer(componentId);

		flagWithPopover.add(new AttributeModifier("data-toggle", "popover"));
		flagWithPopover.add(new AttributeModifier("data-trigger", "focus"));
		flagWithPopover.add(new AttributeModifier("data-placement", "bottom"));
		flagWithPopover.add(new AttributeModifier("data-html", "true"));
		flagWithPopover.add(new AttributeModifier("data-content", popoverContent));
		flagWithPopover.add(new AttributeModifier("tabindex", "0"));

		return flagWithPopover;
	}
}
