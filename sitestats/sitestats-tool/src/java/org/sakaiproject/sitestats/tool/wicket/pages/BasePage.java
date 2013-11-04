/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.pages;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;


public class BasePage extends WebPage implements IHeaderContributor {
	
	private static final long		serialVersionUID	= 1L;
	public static final String		COMMONSCRIPT		= StatsManager.SITESTATS_WEBAPP+"/script/common.js";
	public static final String		JQUERYSCRIPT		= "/library/js/jquery/jquery-1.9.1.min.js";
	public static final String		BODY_ONLOAD_ADDTL	= "setMainFrameHeightNoScroll(window.name, 0, 400)";
	public static final String		LAST_PAGE			= "lastSiteStatsPage";

	public BasePage(){
		// Set Sakai Locale
		ResourceLoader rl = new ResourceLoader();
		getSession().setLocale(rl.getLocale());
	}

	public void renderHead(IHeaderResponse response) {
		
		//get the Sakai skin header fragment from the request attribute
		HttpServletRequest request = getWebRequestCycle().getWebRequest().getHttpServletRequest();
		response.renderString((String)request.getAttribute("sakai.html.head"));

		// include sakai headscripts and resize iframe on load
		response.renderJavascriptReference(COMMONSCRIPT);		
		response.renderOnLoadJavascript(BODY_ONLOAD_ADDTL);
		//response.renderOnDomReadyJavascript(BODY_ONLOAD_ADDTL);

		// include (this) tool style (CSS)
		response.renderCSSReference(StatsManager.SITESTATS_WEBAPP+"/css/sitestats.css");
	}
	
	@Override
	protected void onBeforeRender() {
		/** Component used for debugging pagemaps
		// WARNING: produce unexpected results - use only for debugging!
		PageView componentTree = new PageView("componentTree", this);
		add(componentTree);
		*/
		super.onBeforeRender();
	}

	protected Label newResourceLabel(String id, Component component) {
		return new Label(id, new StringResourceModel(id, component, null));
	}

	public String getResourceModel(String resourceKey, IModel model) {
		return new StringResourceModel(resourceKey, this, model).getString();
	}
}
