package org.sakaiproject.site.tool.helper.order.rsf;

import org.sakaiproject.site.tool.helper.order.impl.SitePageEditHandler;

import uk.org.ponder.messageutil.MessageLocator;
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
public class PageDelProducer implements ViewComponentProducer, ViewParamsReporter {
    
    public static final String VIEW_ID = "PageDel";
    public MessageLocator messageLocator;
    public SitePageEditHandler handler;      
    
    public String getViewID() {    
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewParams, ComponentChecker arg2) {
        PageEditViewParameters params = (PageEditViewParameters) viewParams;

        UIBranchContainer mode = null;
 
        if (params.pageId == null) {
            mode = UIBranchContainer.make(tofill, "mode-failed:");
            UIOutput.make(mode, "message", messageLocator
                    .getMessage("error_pageid"));
        }
        else {
            try {
                String title = handler.removePage(params.pageId);

                mode = UIBranchContainer.make(tofill, "mode-pass:");
                UIOutput.make(mode, "pageId", params.pageId);
                UIMessage.make(mode, "message", "success_removed", new Object[] {title});

            } catch (Exception e) {
                ErrorUtil.renderError(tofill, e);
            }
        }
    }
    
    public ViewParameters getViewParameters() {
        return new PageEditViewParameters();
    }
}
