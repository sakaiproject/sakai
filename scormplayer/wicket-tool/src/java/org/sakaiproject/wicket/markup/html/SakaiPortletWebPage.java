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
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.wicket.util.Utils;

/**
 * This is the base page for all tool pages (package list, configuration, upload, etc.)
 */
public class SakaiPortletWebPage extends WebPage implements IHeaderContributor
{
	private static final long serialVersionUID = 1L;

	@SpringBean( name = "org.sakaiproject.component.api.ServerConfigurationService" )
	protected ServerConfigurationService serverConfigService;

	protected static final String JS_JQUERY = "/library/webjars/jquery/1.12.4/jquery.min.js";

	@Override
	public void renderHead(IHeaderResponse response)
	{
		String portalCdnVersion = StringUtils.trimToEmpty(serverConfigService.getString("portal.cdn.version"));

		// Override Wicket's jQuery to use Sakai's jQuery (this pattern is copied from GradebookNG)
		final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
		response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference())));
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forUrl(Utils.setCdnVersion(JS_JQUERY, portalCdnVersion))));
	}
}
