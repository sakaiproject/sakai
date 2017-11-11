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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.devutils.debugbar.DebugBar;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.util.ResourceLoader;


public class BasePage extends WebPage implements IHeaderContributor {
	
	private static final long		serialVersionUID	= 1L;
	public static final String		COMMONSCRIPT		= StatsManager.SITESTATS_WEBAPP+"/script/common.js";
	public static final String		JQUERYSCRIPT		= "/library/webjars/jquery/1.12.4/jquery.min.js";
	public static final String		BODY_ONLOAD_ADDTL	= "setMainFrameHeightNoScroll(window.name, 0, 400)";
	public static final String		LAST_PAGE			= "lastSiteStatsPage";
	public static final String		DATEPICKERSCRIPT	= "/library/js/lang-datepicker/lang-datepicker.js";
	public static final String		JQUERYUISCRIPT		= "/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js";

	public BasePage(){
		// Set Sakai Locale
		ResourceLoader rl = new ResourceLoader();
		getSession().setLocale(rl.getLocale());

        TransparentWebMarkupContainer html = new TransparentWebMarkupContainer("html");

        String locale = getSession().getLocale().toString();
        html.add(AttributeModifier.replace("lang", locale));
        html.add(AttributeModifier.replace("xml:lang", locale));

        add(html);
		
		add(new DebugBar("debug"));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		
		//get the Sakai skin header fragment from the request attribute
		HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
		response.render(StringHeaderItem.forString(request.getAttribute("sakai.html.head").toString()));
		response.render(OnLoadHeaderItem.forScript(BODY_ONLOAD_ADDTL));
		response.render(JavaScriptHeaderItem.forUrl(COMMONSCRIPT));

		// include (this) tool style (CSS)
		response.render(CssHeaderItem.forUrl(StatsManager.SITESTATS_WEBAPP+"/css/sitestats.css"));
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
