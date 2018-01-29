/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.site.tool.helper.managegroup.rsf;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.stringutil.StringList;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.rsf.producers.FrameAdjustingProducer;
import org.sakaiproject.rsf.util.SakaiURLUtil;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.tool.helper.managegroup.impl.SiteManageGroupHandler;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.Validator;

/**
 * 
 * @author Dr. WHO?
 *
 */
@Slf4j
public class GroupListProducer 
        implements ViewComponentProducer, ActionResultInterceptor, DefaultView {

    public static final String VIEW_ID = "GroupList";
    public Map siteGroups;
    public SiteManageGroupHandler handler;
    public MessageLocator messageLocator;
    public SessionManager sessionManager;
    public FrameAdjustingProducer frameAdjustingProducer;
    
    public String getViewID() {
        return VIEW_ID;
    }
    
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}
	
	private AuthzGroupService authzGroupService;
	public void setAuthzGroupService(AuthzGroupService authzGroupService)
	{
		this.authzGroupService = authzGroupService;
	}
    
    private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}
	
    public void fillComponents(UIContainer tofill, ViewParameters viewparams,
            ComponentChecker checker) {
    	
    	UIBranchContainer actions = UIBranchContainer.make(tofill,"actions:",Integer.toString(0));
    	UIInternalLink.make(actions,"add",UIMessage.make("group.newgroup"), new GroupEditViewParameters(GroupEditProducer.VIEW_ID, null));

		UIOutput.make(tofill, "group-list-title", messageLocator.getMessage("group.list"));
		
		UIForm deleteForm = UIForm.make(tofill, "delete-group-form");
		
		List<Group> groups;
		groups = handler.getGroups();
      
		if (groups != null && groups.size() > 0)
        {
			StringList deletable = new StringList();
			log.debug(this + "fillComponents: got a list of " + groups.size() + " groups");
			
			// Create a multiple selection control for the tasks to be deleted.
			// We will fill in the options at the loop end once we have collected them.
			UISelect deleteselect = UISelect.makeMultiple(deleteForm, "delete-group",
					null, "#{SiteManageGroupHandler.deleteGroupIds}", new String[] {});

			//get the headers for the table
			UIMessage.make(deleteForm, "group-title-title","group.title");
			UIMessage.make(deleteForm, "group-size-title", "group.number");
			  
			for( Group group : groups )
			{
				String groupId = group.getId();
				UIBranchContainer grouprow = UIBranchContainer.make(deleteForm, "group-row:", group.getId());
				
				String groupTitle = Validator.escapeHtml(group.getTitle());
				
				UIOutput.make(grouprow, "group-title-label", groupTitle);
				UIInput name =
						UIInput.make(grouprow, "group-name-input", "#{SitegroupEditHandler.nil}", groupTitle);
				UIOutput nameLabel =
						UIOutput.make(grouprow, "group-name-label", messageLocator.getMessage("group.title"));
				
				nameLabel.decorate(new UILabelTargetDecorator(name));
				UIOutput.make(grouprow,"group-title", groupTitle);
				int size = 0;
				try
				{
					AuthzGroup g = authzGroupService.getAuthzGroup(group.getReference());
					Set<Member> gMembers = g != null ? g.getMembers():new HashSet<Member>();
					size = gMembers.size();
					if (size > 0)
					{
						for( Member p : gMembers )
						{
							// exclude those user with provided roles and rosters
							String userId = p.getUserId();
							try
							{
								userDirectoryService.getUser(userId);
							}
							catch (Exception e)
							{
								log.debug(this + "fillInComponent: cannot find user with id " + userId, e);
								// need to remove the group member
								size--;
							}
						}
					}
				}
				catch (GroupNotDefinedException e)
				{
					log.debug(this + "fillComponent: cannot find group " + group.getReference(), e);
				}
				UIOutput.make(grouprow,"group-size",String.valueOf(size));
				
				UIInternalLink editLink = UIInternalLink.make(grouprow,"group-revise",messageLocator.getMessage("editgroup.revise"),
																					  new GroupEditViewParameters(GroupEditProducer.VIEW_ID, groupId));
				editLink.decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("group.sorttitleasc")+ ":" + groupTitle));
				deletable.add(group.getId());
				UISelectChoice delete =  UISelectChoice.make(grouprow, "group-select", deleteselect.getFullID(), (deletable.size()-1));
				delete.decorators = new DecoratorList(new UITooltipDecorator(UIMessage.make("delete_group_tooltip", new String[] {groupTitle})));
				UIMessage message = UIMessage.make(grouprow,"delete-label","delete_group_tooltip", new String[] {groupTitle});
				UILabelTargetDecorator.targetLabel(message,delete);
			}
			
			deleteselect.optionlist.setValue(deletable.toStringArray());
			UICommand.make(deleteForm, "delete-groups",  UIMessage.make("editgroup.removechecked"), "#{SiteManageGroupHandler.processConfirmGroupDelete}");
			UICommand.make(deleteForm, "cancel", UIMessage.make("editgroup.cancel"), "#{SiteManageGroupHandler.processCancel}");
			
		}
		else
		{
			UIMessage.make(deleteForm, "no-group","group.nogroup");
			UICommand.make(deleteForm, "cancel", UIMessage.make("editgroup.cancel"), "#{SiteManageGroupHandler.processCancel}");
		}

		
		//process any messages
		tml = handler.messages;
        if (tml.size() > 0) {
			for (int i = 0; i < tml.size(); i ++ ) {
				UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", Integer.toString(i));
				String outString;
 				if (tml.messageAt(i).args != null ) {
 					outString = messageLocator.getMessage(tml.messageAt(i).acquireMessageCode(),tml.messageAt(i).args[0]);
 				} else {
 					outString = tml.messageAt(i).acquireMessageCode();
 				}
 				UIMessage.make(errorRow,"error",outString);
			}
        }
    }

    // new hotness
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        if ("confirm".equals(actionReturn)) {
            result.resultingView = new SimpleViewParameters(GroupDelProducer.VIEW_ID);
        } else if ("done".equals(actionReturn)) {
            Tool tool = handler.getCurrentTool();
            result.resultingView = new RawViewParameters(SakaiURLUtil.getHelperDoneURL(tool, sessionManager));
        }
    }
}
