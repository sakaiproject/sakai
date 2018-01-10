/**********************************************************************************
 * $URL: $
 * $Id: $
 * **********************************************************************************
 * <p>
 * Author: David Bauer, dbauer1@udayton.edu
 * <p>
 * Copyright (c) 2016 University of Dayton
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.opensource.org/licenses/ECL-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.tool.producers;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.*;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.util.SimplePageItemUtilities;
import org.sakaiproject.tool.cover.SessionManager;

@Slf4j
public class ChecklistProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    private SimplePageBean simplePageBean;
    private ShowPageProducer showPageProducer;

    public MessageLocator messageLocator;
    public LocaleGetter localeGetter;

    public static final String VIEW_ID = "Checklist";

    private static final List restrictedItems = new ArrayList<>(
            Arrays.asList(SimplePageItem.CHECKLIST, SimplePageItem.BREAK, SimplePageItem.MULTIMEDIA, SimplePageItem.TEXT)
    );

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        GeneralViewParameters gparams = (GeneralViewParameters) viewparams;

        if (gparams.getSendingPage() != -1) {
            // will fail if page not in this site
            // security then depends upon making sure that we only deal with this page
            try {
                simplePageBean.updatePageObject(gparams.getSendingPage());
            } catch (Exception e) {
                log.error("Checklist permission exception {} {}", e.getMessage(), e);
                return;
            }
        }

        UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
                .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));

        SimplePage page = simplePageBean.getCurrentPage();

        Long itemId = gparams.getItemId();

        Collection<String> groups = null;

        String itemAttributeString = "";

	SimplePageItem i = null;
        if (itemId != null && itemId != -1) {
	    i = simplePageBean.findItem(itemId);
            if (i.getPageId() != page.getPageId()) {
                log.debug("Checklist asked to edit item not in current page");
                return;
            }
            try {
                groups = simplePageBean.getItemGroups(i, null, true);
                itemAttributeString = i.getAttributeString();
            } catch (IdUnusedException e) {
                // should be impossible for checklist item; underlying object missing
            }
        }

        if (simplePageBean.canEditPage()) {
            simplePageBean.setItemId(itemId);

            UIOutput.make(tofill, "title-label", messageLocator.getMessage("simplepage.adding-checklist"));
            UIOutput.make(tofill, "page-title", simplePageBean.getCurrentPage().getTitle());

            UIForm form = UIForm.make(tofill, "page_form");

            Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
            if (sessionToken != null) {
                UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());
            }

            // Name of the checklist
            UIInput.make(form, "name", "#{simplePageBean.name}");

            // Is the name hidden from students
            UIBoundBoolean.make(form, "isNameHidden", "#{simplePageBean.nameHidden}");
            UIOutput.make(tofill, "nameHidden-label", messageLocator.getMessage("simplepage.hide-name"));

            // Description for checklist
            UIInput instructions = UIInput.make(form, "description", "#{simplePageBean.description}");

            UIOutput.make(form, "checklist-item-del").decorate(new UIFreeAttributeDecorator("alt", messageLocator.getMessage("simplepage.delete")));

            UIInput.make(form, "checklist-item-complete", "#{simplePageBean.addChecklistItemData}");
            UIInput.make(form, "checklist-item-id", null);
            UIInput.make(form, "checklist-item-name", null);
            UIInput.make(form, "checklist-item-link", null);

            form.parameters.add(new UIELBinding("#{simplePageBean.itemId}", itemId));

	    UIInput.make(form, "add-before", "#{simplePageBean.addBefore}", gparams.getAddBefore());

            UIOutput.make(tofill, "attributeString", itemAttributeString);

	    String indentLevel = "0";
	    if (i != null && i.getAttribute(SimplePageItem.INDENT) != null)
		indentLevel = i.getAttribute(SimplePageItem.INDENT);
	    String indentOptions[] = {"0","1","2","3","4","5","6","7","8"};
	    UISelect.make(form, "indent-level", indentOptions, "#{simplePageBean.indentLevel}", indentLevel);

	    String customClass = "";
	    if (i != null && i.getAttribute(SimplePageItem.CUSTOMCSSCLASS) != null)
		customClass = i.getAttribute(SimplePageItem.CUSTOMCSSCLASS);
	    // If current user is an admin show the css class input box
	    UIInput customCssClass = UIInput.make(form, "customCssClass", "#{simplePageBean.customCssClass}", customClass);
	    UIOutput.make(form, "custom-css-label", messageLocator.getMessage("simplepage.custom.css.class"));

            if (!simplePageBean.isStudentPage(page)) {
                showPageProducer.createGroupList(form, groups, "", "#{simplePageBean.selectedGroups}");
            }
            UICommand.make(form, "save", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.addChecklist}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.save_message")));

            UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel_message"), "#{simplePageBean.cancel}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.cancel_message")));

            if (itemId != null && itemId != -1) {
                form.parameters.add(new UIELBinding("#{simplePageBean.itemId}", gparams.getItemId()));
                UICommand.make(form, "delete", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.delete")));
                UIOutput.make(form, "delete-div");
            }

            createExternalLinkDialog(tofill, page);
        } else {
            UIBranchContainer error = UIBranchContainer.make(tofill, "error");
            UIOutput.make(error, "message", messageLocator.getMessage("simplepage.not_available"));
        }
    }

    public void setShowPageProducer(ShowPageProducer showPageProducer) {
        this.showPageProducer = showPageProducer;
    }

    public void setSimplePageBean(SimplePageBean simplePageBean) {
        this.simplePageBean = simplePageBean;
    }


    public ViewParameters getViewParameters() {
        return new GeneralViewParameters();
    }

    public List reportNavigationCases() {
        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));

        return togo;
    }

    private void createExternalLinkDialog(UIContainer toFill, SimplePage currentPage) {
        UIOutput.make(toFill, "dialogDiv");
        UIOutput.make(toFill, "externalLink-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simpleapge.checklist.edit_externalLink")));
        List<SimplePageItem> itemList =  simplePageBean.getItemsOnPage(currentPage.getPageId());
        UIForm form = UIForm.make(toFill, "item-picker");
        for(SimplePageItem item : itemList) {
            if(item.isRequired() && !restrictedItems.contains(item.getType())) {
                // Output item name & id, with a radio button
                // when dialog is "saved" use JS to add the id to the appropriate checklist item attribute string
                // clear the dialog

                UIBranchContainer row = UIBranchContainer.make(form, "item:", String.valueOf(item.getId()));
                UIOutput.make(row, "select", String.valueOf(item.getId()));
                UIOutput.make(row, "name", SimplePageItemUtilities.getDisplayName(item));
            }
        }

    }
}
