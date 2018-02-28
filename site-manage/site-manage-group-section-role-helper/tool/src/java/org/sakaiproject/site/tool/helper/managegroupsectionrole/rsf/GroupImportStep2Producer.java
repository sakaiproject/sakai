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
package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.components.decorators.UICSSDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.rsf.producers.FrameAdjustingProducer;
import org.sakaiproject.rsf.util.SakaiURLUtil;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.ImportedGroup;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.SiteManageGroupSectionRoleHandler;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;

/**
 * Producer for page 2 of the group import
 */
@Slf4j
public class GroupImportStep2Producer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter, ActionResultInterceptor {

    public SiteManageGroupSectionRoleHandler handler;
    public static final String VIEW_ID = "GroupImportStep2";
    public MessageLocator messageLocator;
    public FrameAdjustingProducer frameAdjustingProducer;
    public SessionManager sessionManager;

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewParams, ComponentChecker checker) {
    	
    	GroupImportViewParameters params = (GroupImportViewParameters) viewParams;
    	
    	UIBranchContainer content = UIBranchContainer.make(tofill, "content:");

        List<ImportedGroup> importedGroups = handler.getImportedGroups();
        
        boolean badData = false;
        
        //get list of groups already in this site
        List<Group> existingGroups = handler.getGroups();
        
        //print each group
        for(ImportedGroup importedGroup: importedGroups) {
            UIBranchContainer branch = UIBranchContainer.make(content,"groups:", importedGroup.getGroupTitle());
            
            boolean groupExists = false;
            Group existingGroup = null;
            
            //add title
            UIOutput.make(branch,"title", messageLocator.getMessage("import2.grouptitle") + " " + importedGroup.getGroupTitle());
            
            //check if group already exists
            for(Group g : existingGroups) {
            	if(StringUtils.equals(g.getTitle(), importedGroup.getGroupTitle())) {
            		existingGroup = g;
            		groupExists = true; 
            		UIOutput.make(branch,"groupexistsmsg:",messageLocator.getMessage("import2.groupexists"));
            	}
            }
            
            //if group already exists, get the users that are already in the group,
            //merge so we get one list, then display the new or merged ones appropriately.
            Set<String> userIds = importedGroup.getUserIds();
            List<String> existingUserIds = new ArrayList<>();
            if(groupExists) {
                existingUserIds = handler.getGroupUserIds(existingGroup);
                userIds.addAll(existingUserIds);
            }
            
            //print each user
            SortedSet<String> foundUserIds = new TreeSet<>();

            boolean existingFlag = false;
            for(String userId: importedGroup.getUserIds()) {
                //check user is valid
                String foundUserId = handler.lookupUser(userId);
                if(foundUserId != null && handler.isValidSiteUser(foundUserId)){
                    String userSortName = handler.getUserSortName(userId)+ " ( " + userId + " )";
                    //is user existing?
                    if(existingUserIds.contains(userId)) {
                        existingFlag = true;
                        UIOutput outputExisting = UIOutput.make(branch,"existmember:", userSortName);
                    } else {
                        if (foundUserIds.isEmpty()) {
                            UIOutput.make(branch, "newmemberheading:", messageLocator.getMessage("import2.newmemberheading"));
                        }
                        foundUserIds.add(foundUserId);
                        UIOutput outputNew = UIOutput.make(branch, "newmember:", userSortName);
                    }
                } else {
                    badData = true;
                    UIOutput outputInvalid = UIOutput.make(branch,"invalidmember:", userId);
                }
            }

            if (existingFlag) {
                UIOutput.make(branch, "existmemberheading:", messageLocator.getMessage("import2.existmemberheading"));
            }
            if (badData) {
                UIOutput.make(branch, "invalidmemberheading:", messageLocator.getMessage("import2.invalidmemberheading"));
            }

            importedGroup.setUserIds(foundUserIds);
        }
        
        UIForm createForm = UIForm.make(content, "form");
        
        if(badData) {
            UIMessage.make(content, "import2.error", "import2.error");
        } else {
            UIMessage.make(content, "import2.okay", "import2.okay");
            UICommand.make(createForm, "continue", messageLocator.getMessage("import2.continue"), "#{SiteManageGroupSectionRoleHandler.processImportedGroups}");
        }
        UICommand.make(createForm, "back", messageLocator.getMessage("back"));
        UICommand cancel = UICommand.make(createForm, "cancel", messageLocator.getMessage("cancel"), "#{SiteManageGroupSectionRoleHandler.processCancelGroups}");
        cancel.parameters.add(new UIDeletionBinding("#{destroyScope.resultScope}"));
        
        if(StringUtils.equals(params.status, "error")){
        	UIMessage.make(content, "import2.couldntimportgroups", "import2.couldntimportgroups");
        }
       
        frameAdjustingProducer.fillComponents(tofill, "resize", "resetFrame");
    }

    public List<NavigationCase> reportNavigationCases() {
        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase("success", new SimpleViewParameters(GroupListProducer.VIEW_ID)));
        togo.add(new NavigationCase("returnToGroupList", new SimpleViewParameters(GroupListProducer.VIEW_ID)));
        togo.add(new NavigationCase("error", new GroupImportViewParameters(GroupImportStep2Producer.VIEW_ID, "error")));
        return togo;
    }

    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        if ("done".equals(actionReturn)) {
        	handler.resetParams();
            Tool tool = handler.getCurrentTool();
            result.resultingView = new RawViewParameters(SakaiURLUtil.getHelperDoneURL(tool, sessionManager));
        }
    }
    
    public ViewParameters getViewParameters() {
        return new GroupImportViewParameters();
    }

}
