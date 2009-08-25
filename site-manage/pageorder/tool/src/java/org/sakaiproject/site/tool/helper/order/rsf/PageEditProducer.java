package org.sakaiproject.site.tool.helper.order.rsf;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
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

        UIBranchContainer mode = null;

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
               catch (Exception e) {
                  ErrorUtil.renderError(tofill, e);
               }
            }
            
            if (params.newConfig != null) {
                try {
                    // TODO: Add ability to configure any arbitrary setting
                    handler.setConfig(params.pageId, "source", params.newConfig);
                }
                catch (Exception e) {
                  ErrorUtil.renderError(tofill, e);
               }
            }

            if ("true".equals(params.visible) || "false".equals(params.visible)) {
                try {            
                    if ("true".equals(params.visible)) {
                        handler.showPage(params.pageId);
                    }
                    else {
                        handler.hidePage(params.pageId);
                    }
                    Site site = handler.site;
                    SitePage page = site.getPage(params.pageId);
                    String oldTitle = page.getTitle();
                    
                    mode = UIBranchContainer.make(tofill, "mode-pass:");
                    UIOutput.make(mode, "page-title", oldTitle);
                    if ("true".equals(params.visible)) {
                        UIMessage.make(mode, "message", "success_visible",
                            new Object[] {oldTitle});
                    }
                    else {
                      UIMessage.make(mode, "message", "success_hidden",
                          new Object[] {oldTitle});
                    }
                } 
                catch (Exception e) {
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
