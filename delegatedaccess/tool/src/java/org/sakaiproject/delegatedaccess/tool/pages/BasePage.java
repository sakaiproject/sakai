package org.sakaiproject.delegatedaccess.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.delegatedaccess.logic.ProjectLogic;
import org.sakaiproject.delegatedaccess.logic.SakaiProxy;



/**
 * This is our base page for Delegated Access. It sets up the containing markup and top navigation.
 * All top level pages should extend from this page so as to keep the same navigation. The content for those pages will
 * be rendered in the main area below the top nav.
 * 
 * <p>It also allows us to setup the API injection and any other common methods, which are then made available in the other pages.
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class BasePage extends WebPage implements IHeaderContributor {

	private static final Logger log = Logger.getLogger(BasePage.class); 

	@SpringBean(name="org.sakaiproject.delegatedaccess.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;

	@SpringBean(name="org.sakaiproject.delegatedaccess.logic.ProjectLogic")
	protected ProjectLogic projectLogic;

	Link<Void> firstLink;
	Link<Void> secondLink;
	Link<Void> thirdLink;

	FeedbackPanel feedbackPanel;
	
	boolean shoppingPeriodTool = false;

	public BasePage() {

		log.debug("BasePage()");

		shoppingPeriodTool = sakaiProxy.isShoppingTool();

		//first link
		firstLink = new Link<Void>("firstLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new UserPage());
			}
		};
		if(shoppingPeriodTool){
			firstLink.add(new Label("firstLinkLabel",new ResourceModel("link.first.shopping")).setRenderBodyOnly(true));
			firstLink.add(new AttributeModifier("title", true, new ResourceModel("link.first.tooltip.shopping")));
		}else{
			firstLink.add(new Label("firstLinkLabel",new ResourceModel("link.first")).setRenderBodyOnly(true));
			firstLink.add(new AttributeModifier("title", true, new ResourceModel("link.first.tooltip")));
		}
		add(firstLink);



		//second link
		secondLink = new Link<Void>("secondLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new ShoppingEditPage());
			}
			@Override
			public boolean isVisible() {
				return !shoppingPeriodTool;
			}
		};
		secondLink.add(new Label("secondLinkLabel",new ResourceModel("link.second")).setRenderBodyOnly(true));
		secondLink.add(new AttributeModifier("title", true, new ResourceModel("link.second.tooltip")));
		add(secondLink);

		//third link
		thirdLink = new Link<Void>("thirdLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new SearchUsersPage());
			}
			@Override
			public boolean isVisible() {
				return sakaiProxy.isSuperUser() && !shoppingPeriodTool;
			}
		};
		thirdLink.add(new Label("thirdLinkLabel",new ResourceModel("link.third")).setRenderBodyOnly(true));
		thirdLink.add(new AttributeModifier("title", true, new ResourceModel("link.third.tooltip")));
		add(thirdLink);

		// Add a FeedbackPanel for displaying our messages
		feedbackPanel = new FeedbackPanel("feedback"){

			@Override
			protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
				final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

				if(message.getLevel() == FeedbackMessage.ERROR ||
						message.getLevel() == FeedbackMessage.DEBUG ||
						message.getLevel() == FeedbackMessage.FATAL ||
						message.getLevel() == FeedbackMessage.WARNING){
					add(new SimpleAttributeModifier("class", "alertMessage"));
				} else if(message.getLevel() == FeedbackMessage.INFO){
					add(new SimpleAttributeModifier("class", "success"));        			
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
			f.add(new SimpleAttributeModifier("class", ""));
		}
	}

	/**
	 * This block adds the required wrapper markup to style it like a Sakai tool. 
	 * Add to this any additional CSS or JS references that you need.
	 * 
	 */
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

		//Tool additions (at end so we can override if required)
		response.renderString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
		//response.renderCSSReference("css/my_tool_styles.css");
		//response.renderJavascriptReference("js/my_tool_javascript.js");

		//for jQuery
		response.renderJavascriptReference("/library/js/jquery-latest.min.js");

		//for datepicker
		response.renderCSSReference("css/flora.datepicker.css");
		response.renderJavascriptReference("javascript/jquery.ui.core-1.5.2.min.js");
		response.renderJavascriptReference("javascript/jquery.datepicker-1.5.2.min.js");

	}


	/** 
	 * Helper to disable a link. Add the Sakai class 'current'.
	 */
	protected void disableLink(Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
		l.setRenderBodyOnly(true);
		l.setEnabled(false);
	}

	protected boolean isShoppingPeriodTool(){
		return shoppingPeriodTool;
	}
}
