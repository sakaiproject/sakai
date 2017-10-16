/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.site.tool.helper.order.rsf;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.tool.helper.order.impl.SitePageEditHandler;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * 
 * @author Joshua Ryan joshua.ryan@asu.edu
 *
 */
public class PageEditProducer implements ViewComponentProducer, ViewParamsReporter {

    public SitePageEditHandler handler;
    public static final String VIEW_ID = "PageEdit";

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters paramso, ComponentChecker arg2) {
        PageEditViewParameters params = (PageEditViewParameters) paramso;

        UIBranchContainer mode;

        if (params.pageId != null) {
            if (params.newTitle != null) {
               try {
                  if (!"".equals(params.newTitle)) {
                     String oldTitle = handler.setTitle(params.pageId, params.newTitle);
                     
                     mode = UIBranchContainer.make(tofill, "mode-pass:");
                     UIOutput.make(mode, "page-title", params.newTitle);
                     UIMessage.make(mode, "message", "success_changed", new Object[] {oldTitle, params.newTitle});
                  }
                  else {
                     String newTitle = handler.resetTitle(params.pageId);
                     
                     mode = UIBranchContainer.make(tofill, "mode-pass:");
                     UIOutput.make(mode, "page-title", newTitle);
                     UIMessage.make(mode, "message", "success_reset", new Object[] {newTitle});
                  }
               }
               catch (IdUnusedException | PermissionException e) {
                  ErrorUtil.renderError(tofill, e);
               }
            }
            
            Site site = handler.site;
            SitePage page = site.getPage(params.pageId);

            ToolConfiguration tool = null;
            if (page.getTools().size() == 1) {
                tool = (ToolConfiguration) page.getTools().get(0);
            }

            // TODO: Add ability to configure any arbitrary setting
            if (tool != null && "sakai.iframe".equals(tool.getToolId()) && params.newConfig != null) {
                try {
                    handler.setConfig(params.pageId, "source", params.newConfig);
                }
                catch (Exception e) {
                  ErrorUtil.renderError(tofill, e);
               }
            }

            if ("true".equals(params.visible) || "false".equals(params.visible) ||
                "true".equals(params.enabled) || "false".equals(params.enabled) ) {
                try {            
                    if ("true".equals(params.enabled)) {
                        handler.enablePage(params.pageId);
                    }
                    else if ("false".equals(params.enabled)) {
                        handler.disablePage(params.pageId);
                    }

                    if ("true".equals(params.visible)) {
                        handler.showPage(params.pageId);
                    }
                    else if ("false".equals(params.visible)) {
                        handler.hidePage(params.pageId);
                    }

                    String oldTitle = page.getTitle();
                    
                    mode = UIBranchContainer.make(tofill, "mode-pass:");
                    UIOutput.make(mode, "page-title", oldTitle);

                    if ("false".equals(params.enabled)) {
                        UIMessage.make(mode, "message", "success_disabled",
                            new Object[] {oldTitle});
                    } else if ("true".equals(params.visible)) {
                        UIMessage.make(mode, "message", "success_visible",
                            new Object[] {oldTitle});
                    } else if ("false".equals(params.visible)) {
                        UIMessage.make(mode, "message", "success_hidden",
                            new Object[] {oldTitle});
                    }
                    else {
                      UIMessage.make(mode, "message", "success_enabled",
                          new Object[] {oldTitle});
                    }
                } 
                catch (SakaiException e) {
                  ErrorUtil.renderError(tofill, e);
               }
            }

        }
        else {
            mode = UIBranchContainer.make(tofill, "mode-failed:");
            UIMessage.make(mode, "message", "error_pageid");
        }
    }

    public ViewParameters getViewParameters() {
        return new PageEditViewParameters();
    }
}
