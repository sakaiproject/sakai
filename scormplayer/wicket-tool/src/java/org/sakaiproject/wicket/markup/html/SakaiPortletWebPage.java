/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.wicket.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

public class SakaiPortletWebPage extends WebPage implements IHeaderContributor
{
	private static final long serialVersionUID = 1L;

	private static final SiteService siteService = (SiteService) ComponentManager.get(SiteService.class);
	private static final SessionManager sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
	private static final ServerConfigurationService serverConfigService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);

	protected static final String HEADSCRIPTS = "/library/js/headscripts.js";
	protected static final String BODY_ONLOAD_ADDTL="setMainFrameHeight( window.name )";

	@Override
	public void renderHead(IHeaderResponse response)
	{
		String skinRepo = serverConfigService.getString("skin.repo");
		String toolCSS = getToolSkinCSS(skinRepo);
		String toolBaseCSS = skinRepo + "/tool_base.css";

		response.render(JavaScriptHeaderItem.forUrl(HEADSCRIPTS));
		response.render(CssHeaderItem.forUrl(toolBaseCSS));
		response.render(CssHeaderItem.forUrl(toolCSS));
		response.render(OnDomReadyHeaderItem.forScript(BODY_ONLOAD_ADDTL));
	}

	protected String getToolSkinCSS(String skinRepo)
	{
		String skin = null;
		try
		{
			skin = siteService.findTool(sessionManager.getCurrentToolSession().getPlacementId()).getSkin();
		}
		catch(Exception e)
		{
			skin = serverConfigService.getString("skin.default");
		}
		
		if(skin == null)
		{
			skin = serverConfigService.getString("skin.default");
		}

		return skinRepo + "/" + skin + "/tool.css";
	}

	protected Label newResourceLabel(String id, Component component)
	{
		return new Label(id, new StringResourceModel(id, component, null));
	}
}
