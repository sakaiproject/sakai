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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import lombok.extern.slf4j.Slf4j;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.stringutil.StringList;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.rsf.producers.FrameAdjustingProducer;
import org.sakaiproject.rsf.util.SakaiURLUtil;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.SiteManageGroupSectionRoleHandler;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.User;

/**
 * 
 * @author
 *
 */
@Slf4j
public class GroupDelProducer 
implements ViewComponentProducer, ActionResultInterceptor{

    public static final String VIEW_ID = "GroupDel";
    public MessageLocator messageLocator;
    public SiteManageGroupSectionRoleHandler handler;
    public FrameAdjustingProducer frameAdjustingProducer;
    public AuthzGroupService authzGroupService;
    
    public String getViewID() {    
        return VIEW_ID;
    }

    private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}
	
    public void fillComponents(UIContainer tofill, ViewParameters arg1, ComponentChecker arg2) {

    	UIOutput.make(tofill, "page-title", messageLocator.getMessage("editgroup.removegroups"));
		
		UIForm deleteForm = UIForm.make(tofill, "delete-confirm-form");
		 
		boolean renderDelete = false;
		
		// Create a multiple selection control for the tasks to be deleted.
		// We will fill in the options at the loop end once we have collected them.
		UISelect deleteselect = UISelect.makeMultiple(deleteForm, "delete-group",
				null, "#{SiteManageGroupSectionRoleHandler.deleteGroupIds}", new String[] {});

		//get the headers for the table
		UIMessage.make(deleteForm, "group-title-title","group.title");
		UIMessage.make(deleteForm, "group-size-title", "group.number");
		UIMessage.make(deleteForm, "group-remove-title", "editgroup.remove");
		
		List<Group> groups = handler.getSelectedGroups();
		
		StringList deletable = new StringList();
		StringList notDeletable = new StringList();
		log.debug(this + "fillComponents: got a list of " + groups.size() + " groups");
      
		if (groups != null && groups.size() > 0)
        {
            for (Iterator<Group> it=groups.iterator(); it.hasNext(); ) {
            	Group group = it.next();
                if (group.isLocked()) {
                    notDeletable.add(group.getTitle());
                } else {
                    String groupId = group.getId();
                    UIBranchContainer grouprow = UIBranchContainer.make(deleteForm, "group-row:", group.getId());

                    UIOutput.make(grouprow,"group-title",group.getTitle());

                    int size = 0;
                    try
                    {
                            size=authzGroupService.getAuthzGroup(group.getReference()).getMembers().size();
                    }
                    catch (GroupNotDefinedException e)
                    {
                            log.debug(this + "fillComponent: cannot find group {}" , group.getReference());
                    }
                    UIOutput.make(grouprow,"group-size",String.valueOf(size));

                    deletable.add(group.getId());
                    UISelectChoice delete =  UISelectChoice.make(grouprow, "group-select", deleteselect.getFullID(), (deletable.size()-1));
                    delete.decorators = new DecoratorList(new UITooltipDecorator(UIMessage.make("delete_group_tooltip", new String[] {group.getTitle()})));
                    UIMessage message = UIMessage.make(grouprow,"delete-label","delete_group_tooltip", new String[] {group.getTitle()});
                    UILabelTargetDecorator.targetLabel(message,delete);
                    log.debug(this + ".fillComponent: this group can be deleted");
                    renderDelete = true;
                }
            }
		}

		deleteselect.optionlist.setValue(deletable.toStringArray());
		UICommand.make(deleteForm, "delete-groups",  UIMessage.make("editgroup.removegroups"), "#{SiteManageGroupSectionRoleHandler.processDeleteGroups}");
		UICommand cancel = UICommand.make(deleteForm, "cancel", UIMessage.make("editgroup.cancel"), "#{SiteManageGroupSectionRoleHandler.processCancelDelete}");
		cancel.parameters.add(new UIDeletionBinding("#{destroyScope.resultScope}"));

		if (!notDeletable.isEmpty()) {
			StringJoiner groupsTitles = new StringJoiner(", ");

			for (Object groupTitle : notDeletable) {
				groupsTitles.add(groupTitle.toString());
			}

			UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:");
			UIMessage.make(errorRow, "error", "deletegroup.notallowed.groups.remove", new String[]{groupsTitles.toString()});
		}
		
		//process any messages
        UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", "0");
		UIMessage.make(errorRow,"error","editgroup.groupdel.alert", new String[]{});
    }
    
    public ViewParameters getViewParameters() {
        GroupEditViewParameters params = new GroupEditViewParameters();
        params.id = null;
        return params;
    }
    
    // new hotness
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        if ("success".equals(actionReturn) || "cancel".equals(actionReturn)) {
            result.resultingView = new SimpleViewParameters(GroupListProducer.VIEW_ID);
        }
    }
}
