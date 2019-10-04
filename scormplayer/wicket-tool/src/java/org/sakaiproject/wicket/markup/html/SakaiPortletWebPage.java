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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.wicket.util.Utils;

/**
 * This is the base page for all tool pages (package list, configuration, upload, etc.)
 */
public class SakaiPortletWebPage extends WebPage implements IHeaderContributor
{
	private static final long serialVersionUID = 1L;

	@SpringBean( name = "org.sakaiproject.component.api.ServerConfigurationService" )
	protected ServerConfigurationService serverConfigService;

	@SpringBean( name = "org.sakaiproject.site.api.SiteService" )
	protected SiteService siteService;

	@SpringBean( name = "org.sakaiproject.tool.api.SessionManager" )
	protected SessionManager sessionManager;

	protected static final String JS_HEADSCRIPTS = "/library/js/headscripts.js";
	protected static final String JS_JQUERY = "/library/webjars/jquery/1.12.4/jquery.min.js";
	protected static final String JS_BOOTSTRAP = "/library/webjars/bootstrap/3.3.7/js/bootstrap.min.js";

	// This is needed to resize the iframe so that we don't get internal scroll bars when the page content changes (ie, AJAX feedback messages making the page longer)
	// We can't target the parent iframe from SCORM's CSS files because... iframes
	protected static final String JS_RESIZE_IFRAME = "setMainFrameHeightNow(window.name, -1);";

	@Override
	public void renderHead(IHeaderResponse response)
	{
		String skinRepo = serverConfigService.getString("skin.repo");
		String toolCSS = getToolSkinCSS(skinRepo);
		String toolBaseCSS = skinRepo + "/tool_base.css";
		String portalCdnVersion = StringUtils.trimToEmpty(serverConfigService.getString("portal.cdn.version"));

		// Override Wicket's jQuery to use Sakai's jQuery (this pattern is copied from GradebookNG)
		final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
		response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference())));
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forUrl(Utils.setCdnVersion(JS_JQUERY, portalCdnVersion))));
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forUrl(Utils.setCdnVersion(JS_BOOTSTRAP, portalCdnVersion))));

		response.render(JavaScriptHeaderItem.forUrl(Utils.setCdnVersion(JS_HEADSCRIPTS, portalCdnVersion)));
		response.render(CssHeaderItem.forUrl(Utils.setCdnVersion(toolBaseCSS, portalCdnVersion)));
		response.render(CssHeaderItem.forUrl(Utils.setCdnVersion(toolCSS, portalCdnVersion)));
		response.render(OnDomReadyHeaderItem.forScript(JS_RESIZE_IFRAME));
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
