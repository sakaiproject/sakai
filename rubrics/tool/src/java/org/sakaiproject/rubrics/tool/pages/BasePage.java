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
package org.sakaiproject.rubrics.tool.pages;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.head.*;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.rubrics.logic.api.RubricsService;


/**
 * This is our base page for our Sakai app. It sets up the containing markup and top navigation.
 * All top level pages should extend from this page so as to keep the same navigation. The content for those pages will
 * be rendered in the main area below the top nav.
 *
 * <p>It also allows us to setup the API injection and any other common methods, which are then made available in the other pages.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class BasePage extends WebPage implements IHeaderContributor {

	private static final Logger log = Logger.getLogger(BasePage.class);

	@SpringBean(name="org.sakaiproject.rubrics.logic.api.RubricsService")
	protected RubricsService rubricsService;

	//Link<Void> firstLink;
	//Link<Void> secondLink;
	//Link<Void> thirdLink;

	//FeedbackPanel feedbackPanel;

	public BasePage() {

		log.debug("BasePage()");

        String token = rubricsService.generateJsonWebToken("sakai.rubrics");
		add(new Label("script", "var rbcstoken  = \"" + token + "\";").setEscapeModelStrings(false));
    }
    	//first link
//		firstLink = new Link<Void>("firstLink") {
//			private static final long serialVersionUID = 1L;
//			public void onClick() {
//
//				setResponsePage(new FirstPage());
//			}
//		};
//		firstLink.add(new Label("firstLinkLabel",new ResourceModel("link.first")).setRenderBodyOnly(true));
//		firstLink.add(new AttributeModifier("title", true, new ResourceModel("link.first.tooltip")));
//		add(firstLink);



		//second link
//		secondLink = new Link<Void>("secondLink") {
//			private static final long serialVersionUID = 1L;
//			public void onClick() {
//				setResponsePage(new SecondPage());
//			}
//		};
//		secondLink.add(new Label("secondLinkLabel",new ResourceModel("link.second")).setRenderBodyOnly(true));
//		secondLink.add(new AttributeModifier("title", true, new ResourceModel("link.second.tooltip")));
//		add(secondLink);



		//third link
//		thirdLink = new Link<Void>("thirdLink") {
//			private static final long serialVersionUID = 1L;
//			public void onClick() {
//				setResponsePage(new ThirdPage());
//			}
//		};
//		thirdLink.add(new Label("thirdLinkLabel",new StringResourceModel("link.third", null, new String[] {"3"})).setRenderBodyOnly(true));
//		thirdLink.add(new AttributeModifier("title", true, new ResourceModel("link.third.tooltip")));
//		add(thirdLink);


		// Add a FeedbackPanel for displaying our messages
 //       feedbackPanel = new FeedbackPanel("feedback"){

//        	@Override
  //      	protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
   //     		final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);
//
//        		if(message.getLevel() == FeedbackMessage.ERROR ||
//        			message.getLevel() == FeedbackMessage.DEBUG ||
//        			message.getLevel() == FeedbackMessage.FATAL ||
//        			message.getLevel() == FeedbackMessage.WARNING){
//        			add(AttributeModifier.replace("class", "alertMessage"));
//        		} else if(message.getLevel() == FeedbackMessage.INFO){
//        			add(AttributeModifier.replace("class", "success"));
//        		}
//
//        		return newMessageDisplayComponent;
//        	}
//        };
//        add(feedbackPanel);

//    }

//	/**
//	 * Helper to clear the feedbackpanel display.
//	 * @param f	FeedBackPanel
//	 */
//	public void clearFeedback(FeedbackPanel f) {
//		if(!f.hasFeedbackMessage()) {
//			f.add(AttributeModifier.replace("class", ""));
//		}
//	}

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
		// response.render(JavaScriptUrlReferenceHeaderItem.forUrl("/rubrics-service/bower_components/webcomponentsjs/webcomponents.min.js"));
		// Alternate
		// response.render(StringHeaderItem.forString("<script src=\"/rubrics-service/bower_components/webcomponentsjs/webcomponents.min.js\"></script>"));
		// response.render(StringHeaderItem.forString("<link rel=\"import\" href=\"/rubrics-service/imports/sakai-rubrics.html\">"));
	}


//	/**
//	 * Helper to disable a link. Add the Sakai class 'current'.
//	 */
//	protected void disableLink(Link<Void> l) {
//		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
//		l.setEnabled(false);
//	}



}
