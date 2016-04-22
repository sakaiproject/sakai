/********************************************************************************** 
 * $URL$ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.tool.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import org.sakaiproject.dash.app.DashboardCommonLogic;
import org.sakaiproject.dash.app.DashboardConfig;
import org.sakaiproject.dash.app.SakaiProxy;


/**
 * This is our base page for our Sakai app. It sets up the containing markup and top navigation.
 * All top level pages should extend from this page so as to keep the same navigation. The content for those pages will
 * be rendered in the main area below the top nav.
 * 
 * <p>It also allows us to setup the API injection and any other common methods, which are then made available in the other pages.
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class BasePage extends WebPage implements IHeaderContributor {

	private static final Logger logger = LoggerFactory.getLogger(BasePage.class); 
	
	@SpringBean(name="org.sakaiproject.dash.app.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.dash.app.DashboardCommonLogic")
	protected DashboardCommonLogic dashboardCommonLogic;
	
	@SpringBean(name="org.sakaiproject.dash.app.DashboardConfig")
	protected DashboardConfig dashboardConfig;
	
	Link<Void> dashboardLink;
	Link<Void> optionsLink;
	
	FeedbackPanel feedbackPanel;
	
	public BasePage() {
		
		logger.debug("BasePage()");
		
		
    	//dashboard link
		dashboardLink = new Link<Void>("dashboardLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new DashboardPage());
			}
		};
		dashboardLink.add(new Label("dashboardLinkLabel",new ResourceModel("link.dashboard")).setRenderBodyOnly(true));
		dashboardLink.add(new AttributeModifier("title", true, new ResourceModel("link.dashboard.tooltip")));
		add(dashboardLink);
		
		
		
		//options link
		optionsLink = new Link<Void>("optionsLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new OptionsPage());
			}
		};
		optionsLink.add(new Label("optionsLinkLabel",new ResourceModel("link.options")).setRenderBodyOnly(true));
		optionsLink.add(new AttributeModifier("title", true, new ResourceModel("link.options.tooltip")));
		add(optionsLink);
		
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
		response.renderCSSReference("css/dashboard.css");
		
	}
	
	/** 
	 * Helper to disable a link. Add the Sakai class 'current'.
	 */
	protected void disableLink(Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
		l.setRenderBodyOnly(true);
		l.setEnabled(false);
	}
	
}
