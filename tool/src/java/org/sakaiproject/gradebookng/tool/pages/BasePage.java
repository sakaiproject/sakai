package org.sakaiproject.gradebookng.tool.pages;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
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
	
	public BasePage() {
		
		log.debug("BasePage()");
		
    	//grades page
		gradebookPageLink = new Link<Void>("gradebookPageLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new GradebookPage());
			}
		};
		add(gradebookPageLink);
		
		//settings page
		settingsPageLink = new Link<Void>("settingsPageLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new SettingsPage());
			}
		};
		add(settingsPageLink);
		
		//settings page
		importExportPageLink = new Link<Void>("importExportPageLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new ImportExportPage());
			}
		};
		add(importExportPageLink);
		
		//permissions page
		permissionsPageLink = new Link<Void>("permissionsPageLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new PermissionsPage());
			}
		};
		add(permissionsPageLink);

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
        			add(AttributeModifier.replace("class", "messageError"));
        			feedbackPanel.add(AttributeModifier.append("class", "feedback"));
        		} else if(message.getLevel() == FeedbackMessage.INFO){
        			add(AttributeModifier.replace("class", "messageSuccess"));   
        			feedbackPanel.add(AttributeModifier.append("class", "feedback"));
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
	
	
	
}
