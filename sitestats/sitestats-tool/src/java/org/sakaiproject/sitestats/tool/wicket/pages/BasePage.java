/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.Component;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.debug.PageView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;


public class BasePage extends WebPage implements IHeaderContributor {
	
	private static final long		serialVersionUID	= 1L;
	public static final String		HEADSCRIPTS			= "/library/js/headscripts.js";
	public static final String		COMMONSCRIPT		= "/sakai-sitestats-tool/script/common.js";
	public static final String		JQUERYSCRIPT		= "/sakai-sitestats-tool/script/jquery-1.3.2.min.js";
	public static final String		BODY_ONLOAD_ADDTL	= "setMainFrameHeightNoScroll(window.name, 0, 400)";
	public static final String		LAST_PAGE			= "lastSiteStatsPage";

	public BasePage(){
		// Set Sakai Locale
		ResourceLoader rl = new ResourceLoader();
		getSession().setLocale(rl.getLocale());
	}

	public void renderHead(IHeaderResponse response) {
		// compute sakai skin
		String skinRepo = ServerConfigurationService.getString("skin.repo");
		response.renderCSSReference(skinRepo + "/tool_base.css");
		response.renderCSSReference(getToolSkinCSS(skinRepo));

		// include sakai headscripts and resize iframe on load
		response.renderJavascriptReference(HEADSCRIPTS);
		response.renderJavascriptReference(COMMONSCRIPT);		
		response.renderOnLoadJavascript(BODY_ONLOAD_ADDTL);
		//response.renderOnDomReadyJavascript(BODY_ONLOAD_ADDTL);

		// include (this) tool style (CSS)
		response.renderCSSReference("/sakai-sitestats-tool/css/sitestats.css");
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

	public String getPortalSkinCSS() {
		return getPortalSkinCSS(null);
	}
	
	private String getPortalSkinCSS(String skinRepo) {
		String skin = null;
		if(skinRepo == null) {
			skinRepo = ServerConfigurationService.getString("skin.repo");
		}
		try{
			skin = SiteService.findTool(SessionManager.getCurrentToolSession().getPlacementId()).getSkin();
		}catch(Exception e){
			skin = ServerConfigurationService.getString("skin.default");
		}

		if(skin == null){
			skin = ServerConfigurationService.getString("skin.default");
		}

		return skinRepo + "/" + skin + "/portal.css";
	}

	public String getToolSkinCSS() {
		return getToolSkinCSS(null);
	}

	protected String getToolSkinCSS(String skinRepo) {
		String skin = null;
		if(skinRepo == null) {
			skinRepo = ServerConfigurationService.getString("skin.repo");
		}
		try{
			skin = SiteService.findTool(SessionManager.getCurrentToolSession().getPlacementId()).getSkin();
		}catch(Exception e){
			skin = ServerConfigurationService.getString("skin.default");
		}

		if(skin == null){
			skin = ServerConfigurationService.getString("skin.default");
		}

		return skinRepo + "/" + skin + "/tool.css";
	}

	protected Label newResourceLabel(String id, Component component) {
		return new Label(id, new StringResourceModel(id, component, null));
	}

	public String getResourceModel(String resourceKey, IModel model) {
		return new StringResourceModel(resourceKey, this, model).getString();
	}
}
