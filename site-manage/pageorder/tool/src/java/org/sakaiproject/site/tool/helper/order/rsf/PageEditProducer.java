package org.sakaiproject.site.tool.helper.order.rsf;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.tool.helper.order.impl.SitePageEditHandler;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
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
    public MessageLocator messageLocator;

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer arg0, ViewParameters arg1, ComponentChecker arg2) {
        PageEditViewParameters params = null;

        String pageId = null;
        String newTitle = null;
        String visible = null;
        String newConfig = null;
 
        UIBranchContainer mode = null;
 
        try {
            params = (PageEditViewParameters) arg1;
            pageId = params.pageId;
            newTitle = params.newTitle;
            visible = params.visible;
            newConfig = params.newConfig;
        }
        catch (Exception e) {
            e.printStackTrace();
            mode = UIBranchContainer.make(arg0, "mode-failed:");
            UIOutput.make(mode, "message", e.getLocalizedMessage());
            return;
        }    

        if (!"nil".equals(pageId)) {
            if (!"nil".equals(newTitle)) {
                if (newTitle != null && !"".equals(newTitle)) {
                    try {
                        Site site = handler.site;
                        SitePage page = site.getPage(pageId);
                        String oldTitle = page.getTitle();
                        page.setTitle(newTitle);

                        // TODO: Find a way to call each tool to ask what fields they need configured
                        // and what methods to use to validate the input..
                        if (page.getTools().size() == 1 && !"nil".equals(newConfig)) {
                            ToolConfiguration tool = (ToolConfiguration) page.getTools().get(0);
                            tool.setTitle(newTitle);
                            if ("sakai.iframe".equals(tool.getToolId())) {
                                tool.getPlacementConfig().setProperty("source", newConfig);

                            }
                        }

                        handler.saveSite(site);
                        mode = UIBranchContainer.make(arg0, "mode-pass:");
                        UIOutput.make(mode, "page-title", newTitle);
                        UIOutput.make(mode, "message", oldTitle + " " + messageLocator
                                .getMessage("success_changed") + " " + newTitle);

                    }
                    catch (IdUnusedException e) {
                        mode = UIBranchContainer.make(arg0, "mode-failed:");
                        UIOutput.make(mode, "message", e.getLocalizedMessage());
                        e.printStackTrace();
                    } 
                    catch (PermissionException e) {
                        mode = UIBranchContainer.make(arg0, "mode-failed:");
                        UIOutput.make(mode, "message", e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
                else {
                    mode = UIBranchContainer.make(arg0, "mode-failed:");
                    UIOutput.make(mode, "message", messageLocator
                        .getMessage("error_title_null"));
                }
            }

            if ("true".equals(visible) || "false".equals(visible)) {
                try {            
                    if ("true".equals(visible)) {
                        handler.showPage(pageId);
                    }
                    else {
                        handler.hidePage(pageId);
                    }
                    Site site = handler.site;
                    SitePage page = site.getPage(pageId);
                    String oldTitle = page.getTitle();
                    
                    mode = UIBranchContainer.make(arg0, "mode-pass:");
                    UIOutput.make(mode, "page-title", oldTitle);
                    if ("true".equals(visible)) {
                        UIOutput.make(mode, "message", oldTitle + " " + messageLocator
                                .getMessage("success_visible")); 
                    }
                    else {
                        UIOutput.make(mode, "message", oldTitle + " " + messageLocator
                                .getMessage("success_hidden")); 
                    }
                } 
                catch (IdUnusedException e) {
                    mode = UIBranchContainer.make(arg0, "mode-failed:");
                    UIOutput.make(mode, "message", e.getLocalizedMessage());
                    e.printStackTrace();
                }
                catch (PermissionException e) {
                    mode = UIBranchContainer.make(arg0, "mode-failed:");
                    UIOutput.make(mode, "message", e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }
        else {
            UIOutput.make(mode, "message", messageLocator
                    .getMessage("error_pageid"));
        }
    }
    
    public ViewParameters getViewParameters() {
        PageEditViewParameters params = new PageEditViewParameters();

        //Bet you can't guess what my first language was? ;-)
        params.pageId = "nil";
        params.newTitle = "nil";
        params.newConfig = "nil";
        return params;
    }

}
