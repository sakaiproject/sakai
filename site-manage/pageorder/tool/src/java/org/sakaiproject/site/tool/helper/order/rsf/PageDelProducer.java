package org.sakaiproject.site.tool.helper.order.rsf;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
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
public class PageDelProducer implements ViewComponentProducer, ViewParamsReporter {
    
    public static final String VIEW_ID = "PageDel";
    public MessageLocator messageLocator;
    public SitePageEditHandler handler;      
    
    public String getViewID() {    
        return VIEW_ID;
    }

    public void fillComponents(UIContainer arg0, ViewParameters arg1, ComponentChecker arg2) {
        PageEditViewParameters params = null;

        String pageId = null;
        
        UIBranchContainer mode = null;
        
        try {
            params = (PageEditViewParameters) arg1;
            pageId = params.pageId;
            
        }
        catch (Exception e) {
            e.printStackTrace();
            mode = UIBranchContainer.make(arg0, "mode-failed:");
            UIOutput.make(mode, "message", e.getLocalizedMessage());
            return;
        }    
        
        if ("nil".equals(pageId)) {
            mode = UIBranchContainer.make(arg0, "mode-failed:");
            UIOutput.make(mode, "message", messageLocator
                    .getMessage("error_pageid"));
        }
        else {
            try {
                String title = handler.removePage( pageId );

                mode = UIBranchContainer.make(arg0, "mode-pass:");
                UIOutput.make(mode, "pageId", pageId);
                UIOutput.make(mode, "message", title + " " + messageLocator
                        .getMessage("success_removed"));

            } catch (IdUnusedException e) {
                mode = UIBranchContainer.make(arg0, "mode-failed:");
                UIOutput.make(mode, "message", e.getLocalizedMessage());
                e.printStackTrace();
            } catch (PermissionException e) {
                mode = UIBranchContainer.make(arg0, "mode-failed:");
                UIOutput.make(mode, "message", e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }
    
    public ViewParameters getViewParameters() {
        PageEditViewParameters params = new PageEditViewParameters();
        params.pageId = "nil";
        return params;
    }
}
