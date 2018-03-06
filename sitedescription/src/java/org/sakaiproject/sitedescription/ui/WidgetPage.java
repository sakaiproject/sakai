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
package org.sakaiproject.sitedescription.ui;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;

/**
 * Main page for the Site Description widget
 */
@Slf4j
public class WidgetPage extends WebPage {

    private static final long serialVersionUID = 1L;

    @SpringBean(name = "org.sakaiproject.site.api.SiteService")
    private SiteService siteService;

    @SpringBean(name = "org.sakaiproject.tool.api.ToolManager")
    private ToolManager toolManager;

    @SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    private ServerConfigurationService serverConfigurationService;

    public WidgetPage() {
        log.debug("WidgetPage()");
    }

    @Override
    public void onInitialize() {

        super.onInitialize();

        final String currentSiteId = this.toolManager.getCurrentPlacement().getContext();

        try {
            Site site = siteService.getSite(currentSiteId);
            add(new Label("siteinfo", Model.of(site.getDescription())).setEscapeModelStrings(false));
        } catch (IdUnusedException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void renderHead(final IHeaderResponse response) {

        super.renderHead(response);

        final String version = this.serverConfigurationService.getString("portal.cdn.version", "");

        // get the Sakai skin header fragment from the request attribute
        final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();

        response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
        response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));

        // Tool additions (at end so we can override if required)
        response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));

        // render jQuery and the Wicket event library
        // Both must be priority so they are emitted into the head
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forUrl(String.format("/library/webjars/jquery/1.12.4/jquery.min.js?version=%s", version))));
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forUrl(String.format("/site-description/scripts/wicket/wicket-event-jquery.min.js?version=%s", version))));

        // NOTE: All libraries apart from jQuery and Wicket Event must be rendered inline with the application. See WidgetPage.html.
    }
}
