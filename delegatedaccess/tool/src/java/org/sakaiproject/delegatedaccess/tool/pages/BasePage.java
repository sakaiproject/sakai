/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.tool.pages;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.delegatedaccess.logic.ProjectLogic;
import org.sakaiproject.delegatedaccess.logic.SakaiProxy;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.util.ResourceLoader;

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
@Slf4j
public class BasePage extends WebPage implements IHeaderContributor {

	private static ResourceLoader rloader = new ResourceLoader();

	@SpringBean(name="org.sakaiproject.delegatedaccess.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;

	@SpringBean(name="org.sakaiproject.delegatedaccess.logic.ProjectLogic")
	protected ProjectLogic projectLogic;
	
	@SpringBean(name="org.sakaiproject.entitybroker.DeveloperHelperService")
	protected DeveloperHelperService developerHelperService;

	Link<Void> accessPageLink;
	Link<Void> shoppingAdminLink;
	Link<Void> shoppingStatsLink;
	Link<Void> searchUsersLink;
	Link<Void> searchAccessLink;
	Link<Void> administrateLink;
	boolean hasShoppingAdmin;
	boolean hasDelegatedAccess;
	boolean hasAccessAdmin;

	FeedbackPanel feedbackPanel;
	
	boolean shoppingPeriodTool = false;

	public BasePage() {

		log.debug("BasePage()");

		shoppingPeriodTool = sakaiProxy.isShoppingTool();

		hasShoppingAdmin = projectLogic.hasShoppingPeriodAdminNodes(sakaiProxy.getCurrentUserId());
		hasDelegatedAccess = projectLogic.hasDelegatedAccessNodes(sakaiProxy.getCurrentUserId());
		hasAccessAdmin = projectLogic.hasAccessAdminNodes(sakaiProxy.getCurrentUserId());
		//access page link
		accessPageLink = new Link<Void>("accessPageLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new UserPage());
			}
			@Override
			public boolean isVisible() {
				return shoppingPeriodTool || (!shoppingPeriodTool && hasDelegatedAccess); 
			}
		};
		if(shoppingPeriodTool){
			accessPageLink.add(new Label("firstLinkLabel",new ResourceModel("link.first.shopping")).setRenderBodyOnly(true));
			accessPageLink.add(new AttributeModifier("title", true, new ResourceModel("link.first.tooltip.shopping")));
		}else{
			accessPageLink.add(new Label("firstLinkLabel",new ResourceModel("link.first")).setRenderBodyOnly(true));
			accessPageLink.add(new AttributeModifier("title", true, new ResourceModel("link.first.tooltip")));
		}
		add(accessPageLink);



		//shopping admin link
		shoppingAdminLink = new Link<Void>("shoppingAdminLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new ShoppingEditPage());
			}
			@Override
			public boolean isVisible() {
				return !shoppingPeriodTool && hasShoppingAdmin;
			}
		};
		shoppingAdminLink.add(new Label("secondLinkLabel",new ResourceModel("link.second")).setRenderBodyOnly(true));
		shoppingAdminLink.add(new AttributeModifier("title", true, new ResourceModel("link.second.tooltip")));
		add(shoppingAdminLink);
		
		//shopping stats link
		shoppingStatsLink = new Link<Void>("shoppingStatsLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new UserPageSiteSearch("", null, true, true));
			}
			@Override
			public boolean isVisible() {
				return !shoppingPeriodTool && hasShoppingAdmin;
			}
		};
		shoppingStatsLink.add(new Label("shoppingStatsLinkLabel",new ResourceModel("link.shoppingStats")).setRenderBodyOnly(true));
		shoppingStatsLink.add(new AttributeModifier("title", true, new ResourceModel("link.shoppingStats.tooltip")));
		add(shoppingStatsLink);

		//search users link
		searchUsersLink = new Link<Void>("searchUsersLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new SearchUsersPage());
			}
			@Override
			public boolean isVisible() {
				return (sakaiProxy.isSuperUser() || hasAccessAdmin) && !shoppingPeriodTool;
			}
		};
		searchUsersLink.add(new Label("thirdLinkLabel",new ResourceModel("link.third")).setRenderBodyOnly(true));
		searchUsersLink.add(new AttributeModifier("title", true, new ResourceModel("link.third.tooltip")));
		add(searchUsersLink);
		
		//search access link
		searchAccessLink = new Link<Void>("searchAccessLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new SearchAccessPage());
			}
			@Override
			public boolean isVisible() {
				return (sakaiProxy.isSuperUser() || hasAccessAdmin) && !shoppingPeriodTool;
			}
		};
		searchAccessLink.add(new Label("searchAccessLinkLabel",new ResourceModel("searchAccessLinkLabel")).setRenderBodyOnly(true));
		searchAccessLink.add(new AttributeModifier("title", true, new ResourceModel("searchAccessLinkLabel.tooltip")));
		add(searchAccessLink);
		
		//administrate link
		administrateLink = new Link<Void>("administrateLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new AdministratePage());
			}
			@Override
			public boolean isVisible() {
				return sakaiProxy.isSuperUser() && !shoppingPeriodTool;
			}
		};
		administrateLink.add(new Label("administrateLinkLabel",new ResourceModel("link.administrate")).setRenderBodyOnly(true));
		administrateLink.add(new AttributeModifier("title", true, new ResourceModel("link.administrate.tooltip")));
		add(administrateLink);

		// Add a FeedbackPanel for displaying our messages
		feedbackPanel = new FeedbackPanel("feedback"){

			@Override
			protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
				final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

				if(message.getLevel() == FeedbackMessage.ERROR ||
						message.getLevel() == FeedbackMessage.DEBUG ||
						message.getLevel() == FeedbackMessage.FATAL ||
						message.getLevel() == FeedbackMessage.WARNING){
					add(new AttributeModifier("class", "alertMessage"));
				} else if(message.getLevel() == FeedbackMessage.INFO){
					add(new AttributeModifier("class", "success"));        			
				} 

				return newMessageDisplayComponent;
			}
		};
		add(feedbackPanel); 
		
		//first check that the user's has been initialized:
		if(!shoppingPeriodTool && 
				sakaiProxy.getCurrentSession().getAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DELEGATED_ACCESS_FLAG) == null){
			//how did we get here?  (here = access to DA but the DA flag isn't set)
			//3 Ideas:
			//1: Admin "Become User", which bypassess the Observer event login
			//2: something screwed up on login (or logged in another way) and bypasses the Observer event login
			//3: Someone added this tool to their Workspace but doesn't have any DA (oh well, just look it up anyways since the tool is useless to them)
			//oh well, we want this to work, so let's retry:
			
			projectLogic.initializeDelegatedAccessSession();
		}
		
	}

	/**
	 * Helper to clear the feedbackpanel display.
	 * @param f	FeedBackPanel
	 */
	public void clearFeedback(FeedbackPanel f) {
		if(!f.hasFeedbackMessage()) {
			f.add(new AttributeModifier("class", ""));
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
		response.render(JavaScriptHeaderItem.forUrl("/library/js/headscripts.js"));
		response.render(CssHeaderItem.forUrl(toolBaseCSS));
		response.render(CssHeaderItem.forUrl(toolCSS));
		response.render(OnDomReadyHeaderItem.forScript("setMainFrameHeight( window.name )"));

		StringBuilder headJs = new StringBuilder();
		headJs.append("var sakai = sakai || {}; sakai.editor = sakai.editor || {}; " +
		"sakai.editor.editors = sakai.editor.editors || {}; " +
		"sakai.editor.editors.ckeditor = sakai.editor.editors.ckeditor || {}; " +
		"sakai.locale = sakai.locale || {};\n");
		headJs.append("sakai.locale.userCountry = '" + rloader.getLocale().getCountry() + "';\n");
		headJs.append("sakai.locale.userLanguage = '" + rloader.getLocale().getLanguage() + "';\n");
		headJs.append("sakai.locale.userLocale = '" + rloader.getLocale().toString() + "';\n");
		response.render(JavaScriptHeaderItem.forScript(headJs, null));

		//Tool additions (at end so we can override if required)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));
		//response.renderCSSReference("css/my_tool_styles.css");
		//response.renderJavascriptReference("js/my_tool_javascript.js");

		//for jQuery
		response.render(JavaScriptHeaderItem.forUrl("/library/webjars/jquery/1.12.4/jquery.min.js"));
		response.render(JavaScriptHeaderItem.forUrl("/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js"));

		//for datepicker
		response.render(CssHeaderItem.forUrl("/library/webjars/jquery-ui/1.12.1/jquery-ui.css"));
		response.render(JavaScriptHeaderItem.forUrl("javascript/jquery.asmselect.js"));
		response.render(CssHeaderItem.forUrl("css/jquery.asmselect.css"));
		response.render(JavaScriptHeaderItem.forUrl("/library/js/lang-datepicker/lang-datepicker.js"));
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
