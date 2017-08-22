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
package org.sakaiproject.lessonbuildertool.tool.producers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.tool.cover.SessionManager;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIParameter;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import javax.faces.component.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neelam on 8/19/2015.
 * Displays folders to user to add in Lessons tool
 */
public class FolderPickerProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    private SimplePageBean simplePageBean;
    public MessageLocator messageLocator;
    public LocaleGetter localeGetter;
    private ContentHostingService contentHostingService;
    public static final String VIEW_ID = "FolderPicker";
    public String getViewID() {
        return VIEW_ID;
    }
    private static Log log = LogFactory.getLog(FolderPickerProducer.class);

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        GeneralViewParameters gparams = (GeneralViewParameters) viewparams;

        UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
                .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));

        if (gparams.getSendingPage() != -1) {
            // will fail if page not in this site
            // security then depends upon making sure that we only deal with this page
            try {
                simplePageBean.updatePageObject(gparams.getSendingPage());
            } catch (Exception e) {
                log.error("FolderPicker permission exception " + e);
                return;
            }
        }
        Long itemId = ((GeneralViewParameters) viewparams).getItemId();
        simplePageBean.setItemId(itemId);
        
        if (simplePageBean.canEditPage()) {
            SimplePage page = simplePageBean.getCurrentPage();
            SimplePageItem i = simplePageBean.findItem(itemId);
            // if itemid is null, we'll append to current page, so it's ok
            if (itemId != null && itemId != -1) {
                if (i == null){
                    return; 
                }
                // trying to hack on item not on this page
                if (i.getPageId() != page.getPageId()){
                    return;
                }
            }
            UIOutput.make(tofill, "title-label", messageLocator.getMessage("simplepage.adding-folder"));
            UIOutput.make(tofill, "top-folder", simplePageBean.getCurrentSite().getTitle());
            UIOutput.make(tofill, "page-title", simplePageBean.getCurrentPage().getTitle());

            UIForm form = UIForm.make(tofill, "folder-picker");
            Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
            if (sessionToken != null){
                UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());
            }

	        boolean isPrerequisite = false;
	        if (itemId != null && itemId != -1) {
		        isPrerequisite = i.isPrerequisite();
	        }
	        UIOutput.make(form, "prerequisite-block");
	        UIBoundBoolean.make(form, "question-prerequisite", "#{simplePageBean.prerequisite}", isPrerequisite);

            //Check the size of collection for the site
            String id = simplePageBean.getCurrentSiteId();
            String path = contentHostingService.getSiteCollection(id);
            try {
                if(contentHostingService.getCollectionSize(path) > 0){
                    //get site entity-id
                    UIInput.make(form, "folder-path", "#{simplePageBean.folderPath}", path);
                    UIInput.make(form, "add-before", "#{simplePageBean.addBefore}", ((GeneralViewParameters) viewparams).getAddBefore());
                    UIInput.make(form, "item-id", "#{simplePageBean.itemId}");
                    UIOutput.make(form, "choose-label", messageLocator.getMessage("simplepage.choose.folder"));
                    UICommand.make(form, "submit", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.folderPickerSubmit}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.save_message")));
                    //If user has chosen edit option for folder listing
                    if (itemId != null && itemId > 0) {
                        form.parameters.add(new UIELBinding("#{simplePageBean.itemId}", gparams.getItemId()));
                        String dataDirectory = i.getAttribute("dataDirectory");
                        if(dataDirectory != null && !dataDirectory.equals("") ){
                            String defaultPath = contentHostingService.getSiteCollection(simplePageBean.getCurrentSiteId());
                            String[] folderPath = dataDirectory.split(defaultPath);
                            UIParameter parameter = new UIParameter("edit-folder-path", folderPath[1]);
                            form.addParameter(parameter);
                        }

                        UICommand.make(form, "delete", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.delete")));
                    }
                }
                //there are no resources in the site to add, display error message
                else{
                    UIOutput.make(tofill, "error-div");
                    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.no_resource_folder"));
                }
            } catch (SakaiException se) {
                log.warn("Failed to get size for collection: " + id, se);
            }
            UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel_message"), "#{simplePageBean.cancel}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.cancel_message")));
        }
        else {
            UIBranchContainer error = UIBranchContainer.make(tofill, "error");
            UIOutput.make(error, "message", messageLocator.getMessage("simplepage.not_available"));
        }
    }
    

    public void setSimplePageBean(SimplePageBean simplePageBean) {
        this.simplePageBean = simplePageBean;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public ViewParameters getViewParameters() {
        return new GeneralViewParameters();
    }

    public List reportNavigationCases() {
        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("failure", new SimpleViewParameters(FolderPickerProducer.VIEW_ID)));
        togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));

        return togo;
    }



}
