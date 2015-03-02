/*
 * #%L
 * OAuth Tool
 * %%
 * Copyright (C) 2009 - 2013 The Sakai Foundation
 * %%
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
 * #L%
 */
package org.sakaiproject.oauth.tool.pages;

import org.apache.wicket.*;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

/**
 * @author Colin Hebert
 */
public abstract class SakaiPage extends WebPage implements IHeaderContributor {
    @SpringBean
    private ServerConfigurationService serverConfigurationService;
    @SpringBean
    private SiteService siteService;
    @SpringBean
    private SessionManager sessionManager;
    private RepeatingView menu;

    protected SakaiPage() {
        init();
    }

    protected SakaiPage(IModel<?> model) {
        super(model);
        init();
    }

    protected SakaiPage(IPageMap pageMap) {
        super(pageMap);
        init();
    }

    protected SakaiPage(IPageMap pageMap, IModel<?> model) {
        super(pageMap, model);
        init();
    }

    protected SakaiPage(PageParameters parameters) {
        super(parameters);
        init();
    }

    protected SakaiPage(IPageMap pageMap, PageParameters parameters) {
        super(pageMap, parameters);
        init();
    }

    private void init() {
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback") {
            @Override
            protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
                if (message.getLevel() == FeedbackMessage.INFO)
                    add(new SimpleAttributeModifier("class", "success"));
                else
                    add(new SimpleAttributeModifier("class", "alertMessage"));

                return super.newMessageDisplayComponent(id, message);
            }

            // If we don't link up visibility to having messages, then when changing the filter after displaying
            // the message, the message disappears but they surrounding box remains.
            public boolean isVisible() {
                return anyMessage();
            }
        };
        feedbackPanel.setFilter(new IFeedbackMessageFilter() {
            public boolean accept(FeedbackMessage message) {
                return !message.isRendered();
            }
        });
        add(feedbackPanel);
        menu = new RepeatingView("menu") {
            /**
             * Automatically hide the repeating view if there is no element present in there.
             * @return true if there is at least one child element, false otherwise
             */
            @Override
            public boolean isVisible() {
                return iterator().hasNext() && super.isVisible();
            }
        };
        add(menu);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        // get Sakai skin
        String skinRepo = serverConfigurationService.getString("skin.repo");
        String skin = siteService.findTool(sessionManager.getCurrentToolSession().getPlacementId()).getSkin();
        if (skin == null) {
            skin = serverConfigurationService.getString("skin.default");
        }
        String toolCSS = skinRepo + "/" + skin + "/tool.css";
        String toolBaseCSS = skinRepo + "/tool_base.css";

        // Sakai additions
        response.renderJavascriptReference("/library/js/headscripts.js");
        response.renderCSSReference(toolBaseCSS);
        response.renderCSSReference(toolCSS);
        response.renderOnLoadJavascript("\nif (typeof setMainFrameHeight !== 'undefined'){\n"
                + "setMainFrameHeight( window.name );\n"
                + "}");

        // Tool additions (at end so we can override if required)
        response.renderString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
        response.renderCSSReference(new ResourceReference(getClass(), "style.css"));
    }

    /**
     * Add a menu entry linking to a class page.
     * <p>
     * Automatically disable the link if it's to the current page.
     * </p>
     *
     * @param clazz classPage
     * @param text  Link's text
     * @param title Title attribute for the link
     */
    protected void addMenuLink(Class<? extends Page> clazz, IModel<String> text, IModel<String> title) {
        Link<Page> link = new BookmarkablePageLink<Page>("menuItem", clazz);
        link.setEnabled(!getClass().equals(clazz));
        addMenuLink(link, text, title);
    }

    /**
     * Add a menu entry with a custom link.
     *
     * @param link  Link to add to the menu bar
     * @param text  Link's text
     * @param title Title attribute for the link
     */
    protected void addMenuLink(Link<Page> link, IModel<String> text, IModel<String> title) {
        WebMarkupContainer parent = new WebMarkupContainer(menu.newChildId());
        menu.add(parent);
        link.add(new Label("menuItemText", text).setRenderBodyOnly(true));
        if (title != null)
            link.add(new AttributeModifier("title", true, title));

        parent.add(link);
    }
}
