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
public class PageDelProducer implements ViewComponentProducer, ViewParamsReporter {

    public static final String VIEW_ID = "PageDel";
    public SitePageEditHandler handler;

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewParams, ComponentChecker arg2) {
        PageEditViewParameters params = (PageEditViewParameters) viewParams;

        UIBranchContainer mode;
 
        if (params.pageId == null) {
            mode = UIBranchContainer.make(tofill, "mode-failed:");
            UIMessage.make(mode, "message", "error_pageid");
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
