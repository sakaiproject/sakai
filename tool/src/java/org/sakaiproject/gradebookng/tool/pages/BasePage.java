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


/**
 * Base page for our app
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class BasePage extends WebPage implements IHeaderContributor {

	private static final Logger log = Logger.getLogger(BasePage.class); 
	
	//@SpringBean(name="com.steve.logic.SakaiProxy")
	//protected SakaiProxy sakaiProxy;
	
	//@SpringBean(name="com.steve.logic.ProjectLogic")
	//protected ProjectLogic projectLogic;
	
	Link<Void> gradebookPageLink;
	
	FeedbackPanel feedbackPanel;
	
	public BasePage() {
		
		log.debug("BasePage()");
		
		
    	//first link
		gradebookPageLink = new Link<Void>("gradebookPageLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				
				setResponsePage(new GradebookPage());
			}
		};
		add(gradebookPageLink);
		
		// Add a FeedbackPanel for displaying our messages
        feedbackPanel = new FeedbackPanel("feedback"){
        	
        	@Override
        	protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
        		final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

        		if(message.getLevel() == FeedbackMessage.ERROR ||
        			message.getLevel() == FeedbackMessage.DEBUG ||
        			message.getLevel() == FeedbackMessage.FATAL ||
        			message.getLevel() == FeedbackMessage.WARNING){
        			add(AttributeModifier.replace("class", "alertMessage"));
        		} else if(message.getLevel() == FeedbackMessage.INFO){
        			add(AttributeModifier.replace("class", "success"));        			
        		} 

        		return newMessageDisplayComponent;
        	}
        };
        add(feedbackPanel); 
		
    }
	
	/**
	 * Helper to clear the feedbackpanel display.
	 * @param f	FeedBackPanel
	 */
	public void clearFeedback(FeedbackPanel f) {
		if(!f.hasFeedbackMessage()) {
			f.add(AttributeModifier.replace("class", ""));
		}
	}
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * This block adds the required wrapper markup to style it like a Sakai tool. 
	 * Add to this any additional CSS or JS references that you need.
	 * 
	 */
	public void renderHead(IHeaderResponse response) {
		//get the Sakai skin header fragment from the request attribute
		HttpServletRequest request = (HttpServletRequest)getRequest().getContainerRequest();
		
		response.render(StringHeaderItem.forString((String)request.getAttribute("sakai.html.head")));
		response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));
		
		
		//Tool additions (at end so we can override if required)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));
		//response.renderCSSReference("css/my_tool_styles.css");
		//response.renderJavascriptReference("js/my_tool_javascript.js");
	}
	
	
	/** 
	 * Helper to disable a link. Add the Sakai class 'current'.
	 */
	protected void disableLink(Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
		l.setEnabled(false);
	}
	
	
	
}
