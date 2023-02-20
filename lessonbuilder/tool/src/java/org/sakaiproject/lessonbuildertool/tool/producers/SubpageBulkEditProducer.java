/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.tool.producers;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.tool.api.SessionManager;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Setter
public class SubpageBulkEditProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    public static final String VIEW_ID = "SubpageBulkEdit";

    @Override
    public String getViewID() {
        return VIEW_ID;
    }

    private SimplePageBean simplePageBean;
    private SessionManager sessionManager;

    public LocaleGetter localeGetter;
    public MessageLocator messageLocator;

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        if (((GeneralViewParameters) viewparams).getSendingPage() != -1) {
            try {
                simplePageBean.updatePageObject(((GeneralViewParameters) viewparams).getSendingPage());
            } catch (Exception e) {
                log.info("SubpageBulkEdit permission exception " + e);
                return;
            }
        }

        UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
                .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));

        UIOutput.make(tofill, "current-page-title", messageLocator.getMessage("simplepage.bulk-edit-pages.title").replace("{0}", simplePageBean.getPageTitle()));

        UIForm form = UIForm.make(tofill, "bulk-edit-form");
        Object sessionToken = sessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
        if (sessionToken != null) {
            UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());
        }

        UIInput.make(form, "subpage-bulk-edit-json", "simplePageBean.subpageBulkEditJson");

        boolean noPagesToEdit = true;
        if (simplePageBean.canEditPage()) {
            // Get all subpages linked on this page.
            List<SimplePageItem> allItems = simplePageBean.getItemsOnPage(simplePageBean.getCurrentPageId());
            if (allItems != null && !allItems.isEmpty()) {
                for (SimplePageItem item : allItems) {
                    if (item.getType() == SimplePageItem.PAGE) {
                        noPagesToEdit = false;
                        UIBranchContainer pagesContainer = UIBranchContainer.make(form, "subpage:");
                        UIInput.make(pagesContainer, "subpage-item-id", null, String.valueOf(item.getId()));
                        UIOutput.make(pagesContainer, "subpage-item-current-title", item.getName());
                        UIInput.make(pagesContainer, "subpage-item-title", null, item.getName());
                        UIOutput.make(pagesContainer, "subpage-item-current-description", item.getDescription());
                        UIInput.make(pagesContainer, "subpage-item-description", null, item.getDescription());
                    }
                }
            }
        }

        if (noPagesToEdit) {
            UIOutput.make(tofill, "no-pages", messageLocator.getMessage("simplepage.bulk-edit-pages.none"));
        } else {
            UICommand.make(form, "save", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.subpageBulkEditSubmit}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.save_message")));
        }

        UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel_message"), "#{simplePageBean.cancel}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.cancel_message")));

        List<String> errMessages = simplePageBean.errMessages();
        if (errMessages != null) {
            UIOutput.make(tofill, "error-div");
            for (String e: errMessages) {
                UIBranchContainer er = UIBranchContainer.make(tofill, "errors:");
                UIOutput.make(er, "error-message", e);
            }
        }
    }

    public ViewParameters getViewParameters() {
        return new GeneralViewParameters();
    }

    public List reportNavigationCases() {

        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("permission-failed", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("failure", new SimpleViewParameters(SubpageBulkEditProducer.VIEW_ID)));
        togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        return togo;
    }
}
