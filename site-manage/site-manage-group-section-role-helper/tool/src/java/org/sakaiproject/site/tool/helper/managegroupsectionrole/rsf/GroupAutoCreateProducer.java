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
package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.rsf.copies.Web;
import org.sakaiproject.rsf.producers.FrameAdjustingProducer;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.SiteManageGroupSectionRoleHandler;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * 
 * @author
 *
 */
@Slf4j
public class GroupAutoCreateProducer implements ViewComponentProducer, ActionResultInterceptor, ViewParamsReporter {

    public SiteManageGroupSectionRoleHandler handler;
    public static final String VIEW_ID = "GroupAutoCreate";
    public MessageLocator messageLocator;
    public FrameAdjustingProducer frameAdjustingProducer;
    public SiteService siteService = null;

    public String getViewID() {
        return VIEW_ID;
    }
    
    private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}
	
	public UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

    public void fillComponents(UIContainer arg0, ViewParameters arg1, ComponentChecker arg2) {
    	
    	// title for group
    	String groupTitle = null;
    	
    	UIForm groupForm = UIForm.make(arg0, "groups-form");

    	 String id = ((GroupEditViewParameters) arg1).id;
    	 if (id != null)
    	 {
    		 try
    		 {
    			 Group g = siteService.findGroup(id);
    			 groupTitle = g.getTitle();
    		 }
    		 catch (Exception e)
    		 {
    			 log.debug(this + "fillComponents: cannot get group id=" + id, e);
    		 }
    	 }
    	 else
    	 {
    		 handler.resetParams();
    	 }
    	 

         UIOutput.make(groupForm, "prompt", messageLocator.getMessage("group.autocreate.newgroups"));
         
         UIOutput.make(groupForm, "group_label", messageLocator.getMessage("group.title"));
         UIInput.make(groupForm, "group_title", "#{SiteManageGroupSectionRoleHandler.title}",groupTitle);
		 
		 // for the site rosters list
		 List<String> siteRosters= handler.getSiteRosters(null);
		 String[] optionValues = new String[] { "1", "2" };
		 if (siteRosters != null && siteRosters.size() > 0)
		 {
			 UIBranchContainer rosterOptions = UIBranchContainer.make(groupForm, "roster_options:");
			 UIMessage.make(arg0, "roster-select-header", "table.roster_select");
			 UIMessage.make(arg0, "roster-title-header", "table.roster_title");
			 for (String roster: siteRosters) {
				 UIBranchContainer tablerow = UIBranchContainer.make(rosterOptions, "roster-row:");
				 UIBoundBoolean checkbox = UIBoundBoolean.make(tablerow, "roster-checkbox", "#{SiteManageGroupSectionRoleHandler.selectedRosters." + roster.replaceAll("\\.", "-_p_-") + "}");
				 UIOutput rosterTitle = UIOutput.make(tablerow, "roster-title", handler.getRosterLabel(roster));
				 rosterTitle.decorate(new UITooltipDecorator(roster));
				 UILabelTargetDecorator.targetLabel(rosterTitle, checkbox);
				 
				 // check whether there is already a group with this roster
				 if (handler.existRosterGroup(roster))
				 {
					 UIMessage.make(tablerow, "exist-group-roster", "exist.group.roster");
				 }
			 }

			// SAK-28373 - auto group options for rosters
			String[] rosterOptionLabels = new String[]
			{
				messageLocator.getMessage( "rosterOptionLabel" ),
				messageLocator.getMessage( "rosterRandomOptionLabel" )
			};

			UISelect rosterOptionSelect = UISelect.make( groupForm, "roster-option-radios", 
				optionValues, rosterOptionLabels, "SiteManageGroupSectionRoleHandler.rosterOptionAssign" );
			rosterOptionSelect.optionnames = UIOutputMany.make( rosterOptionLabels );
			String rosterOptionSelectID = rosterOptionSelect.getFullID();

			UISelectLabel rosterLabel = UISelectLabel.make( arg0, "rosterOptionLabel", rosterOptionSelectID, 0 );
			UISelectChoice rosterChoice = UISelectChoice.make( arg0, "rosterOption", rosterOptionSelectID, 0 );
			UILabelTargetDecorator.targetLabel( rosterLabel, rosterChoice );

			UISelectLabel rosterLabel2 = UISelectLabel.make( arg0, "rosterRandomOptionLabel", rosterOptionSelectID, 1 );
			UISelectChoice rosterChoice2 = UISelectChoice.make( arg0, "rosterRandomOption", rosterOptionSelectID, 1 );
			UILabelTargetDecorator.targetLabel( rosterLabel2, rosterChoice2 );

			UILabelTargetDecorator.targetLabel( UIMessage.make( groupForm, "roster-group-title-group", "group.title" ), 
				UIInput.make( groupForm, "roster-groupTitle-group", "SiteManageGroupSectionRoleHandler.rosterGroupTitleGroup") );
			UILabelTargetDecorator.targetLabel( UIMessage.make( groupForm, "roster-group-unit", "group.unit" ),
				UIInput.make( groupForm, "roster-numToSplit-group", "SiteManageGroupSectionRoleHandler.rosterNumToSplitGroup" ) );
			UILabelTargetDecorator.targetLabel( UIMessage.make( groupForm, "roster-group-title-user", "group.title" ),
				UIInput.make( groupForm, "roster-groupTitle-user", "SiteManageGroupSectionRoleHandler.rosterGroupTitleUser" ) );
			UILabelTargetDecorator.targetLabel( UIMessage.make( groupForm, "roster-user-unit", "user.unit" ),
				UIInput.make( groupForm, "roster-numToSplit-user", "SiteManageGroupSectionRoleHandler.rosterNumToSplitUser" ) );

			String[] rosterGradingValues = new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString() };
			String[] rosterGradingLabels = new String[]
			{
				messageLocator.getMessage( "splitByGroupsLabel" ),
				messageLocator.getMessage( "splitByUsersLabel" )
			};

			UISelect rosterGradingSelect = UISelect.make( groupForm, "roster-graded-radios", rosterGradingValues, rosterGradingLabels, "SiteManageGroupSectionRoleHandler.rosterGroupSplit" );
			String rosterGradingSelectID = rosterGradingSelect.getFullID();

			UISelectLabel rosterSplitLabel = UISelectLabel.make( arg0, "rosterSplitByGroupsLabel", rosterGradingSelectID, 0 );
				UISelectChoice rosterSplitChoice = UISelectChoice.make( arg0, "rosterGroupSplit", rosterGradingSelectID, 0 );
			UILabelTargetDecorator.targetLabel(  rosterSplitLabel, rosterSplitChoice );

			UISelectLabel rosterSplitLabel2 = UISelectLabel.make( arg0, "rosterSplitByUsersLabel", rosterGradingSelectID, 1 );
			UISelectChoice rosterSplitChoice2 = UISelectChoice.make( arg0, "rosterUserSplit", rosterGradingSelectID, 1 );
			UILabelTargetDecorator.targetLabel( rosterSplitLabel2, rosterSplitChoice2 );
		 }
		 
		 // for the site roles list
		 List<Role> siteRoles= handler.getSiteRoles(null);
		 if (siteRoles != null && siteRoles.size() > 0)
		 {
			 UIBranchContainer roleOptions = UIBranchContainer.make(groupForm, "role_options:");
			 UIMessage.make(arg0, "role-select-header", "table.role_select");
			 UIMessage.make(arg0, "role-title-header", "table.role_title");
			 UIMessage.make(arg0, "instruction-role", "instruction.role");
			 for (Role role: siteRoles) {
				 UIBranchContainer tablerow = UIBranchContainer.make(roleOptions, "role-row:");
				 UIBoundBoolean checkbox = UIBoundBoolean.make(tablerow, "role-checkbox", "#{SiteManageGroupSectionRoleHandler.selectedRoles." + role.getId() + "}");
				 UILabelTargetDecorator.targetLabel(UIOutput.make(tablerow, "role-title", role.getId()), checkbox);
				// check whether there is already a group with this role
				 if (handler.existRoleGroup(role.getId()))
				 {
					 UIMessage.make(tablerow, "exist-group-role", "exist.group.role");
				 }
			 }
		 
			 //random or by roles options:
			 
			//Radio Buttons for assigning options
	         String [] optionLabels = new String[] {
	        		 messageLocator.getMessage("roleOptionLabel"),
	        		 messageLocator.getMessage("randomOptionLabel")
	         };
	         UISelect option_select = UISelect.make(groupForm, "option-radios", 
	        		 optionValues, optionLabels, "SiteManageGroupSectionRoleHandler.optionAssign");
	         option_select.optionnames = UIOutputMany.make(optionLabels);
	         String option_select_id = option_select.getFullID();
	         
	         UISelectLabel lb = UISelectLabel.make(arg0, "roleOptionLabel", option_select_id, 0);
	         UISelectChoice choice =UISelectChoice.make(arg0, "roleOption", option_select_id, 0);
	         UILabelTargetDecorator.targetLabel(lb, choice);
	         
	         UISelectLabel lb2 = UISelectLabel.make(arg0, "randomOptionLabel", option_select_id, 1);
	         UISelectChoice choice2 =UISelectChoice.make(arg0, "randomOption", option_select_id, 1);
	         UILabelTargetDecorator.targetLabel(lb2, choice2);
			 
			 UIMessage.make(arg0, "randomGroupsLegend", "randomGroupsLegend");
			 
			 // group inputs
			 UILabelTargetDecorator.targetLabel(UIMessage.make(groupForm, "group-title-group", "group.title"), UIInput.make(groupForm, "groupTitle-group", "SiteManageGroupSectionRoleHandler.groupTitleGroup"));
			 UILabelTargetDecorator.targetLabel(UIMessage.make(groupForm, "group-unit", "group.unit"), UIInput.make(groupForm, "numToSplit-group", "SiteManageGroupSectionRoleHandler.numToSplitGroup"));
			 UILabelTargetDecorator.targetLabel(UIMessage.make(groupForm, "group-title-user", "group.title"), UIInput.make(groupForm, "groupTitle-user", "SiteManageGroupSectionRoleHandler.groupTitleUser"));
			 UILabelTargetDecorator.targetLabel(UIMessage.make(groupForm, "user-unit", "user.unit"), UIInput.make(groupForm, "numToSplit-user", "SiteManageGroupSectionRoleHandler.numToSplitUser"));
		 
			 //Radio Buttons
	         String [] grading_values = new String[] {
	                 Boolean.TRUE.toString(), Boolean.FALSE.toString()
	         };
	         String [] grading_labels = new String[] {
	        		 messageLocator.getMessage("splitByGroupsLabel"),
	        		 messageLocator.getMessage("splitByUsersLabel")	        		 
	         };
	         UISelect grading_select = UISelect.make(groupForm, "graded-radios", 
	                 grading_values, grading_labels, "SiteManageGroupSectionRoleHandler.groupSplit");
	         String grading_select_id = grading_select.getFullID();
	         
	         UISelectLabel split_lb = UISelectLabel.make(arg0, "splitByGroupsLabel", grading_select_id, 0);
	         UISelectChoice split_choice =UISelectChoice.make(arg0, "groupSplit", grading_select_id, 0);
	         UILabelTargetDecorator.targetLabel(split_lb, split_choice);
	         
	         UISelectLabel split_lb2 = UISelectLabel.make(arg0, "splitByUsersLabel", grading_select_id, 1);
	         UISelectChoice split_choice2 =UISelectChoice.make(arg0, "userSplit", grading_select_id, 1);
	         UILabelTargetDecorator.targetLabel(split_lb2, split_choice2);
		 
		 }
		 
    	 UICommand.make(groupForm, "save", messageLocator.getMessage("editgroup.new"), "#{SiteManageGroupSectionRoleHandler.processAutoCreateGroup}");

         UICommand cancel = UICommand.make(groupForm, "cancel", messageLocator.getMessage("cancel"), "#{SiteManageGroupSectionRoleHandler.processBack}");
         cancel.parameters.add(new UIDeletionBinding("#{destroyScope.resultScope}"));
         
         int i;
         //process any messages
         tml = handler.messages;
         if (tml.size() > 0) {
 			for (i = 0; i < tml.size(); i ++ ) {
 				UIBranchContainer errorRow = UIBranchContainer.make(arg0,"error-row:", Integer.toString(i));
 				String outString;
 				if (tml.messageAt(i).args != null ) {
 					outString = messageLocator.getMessage(tml.messageAt(i).acquireMessageCode(),tml.messageAt(i).args[0]);
 				} else {
 					outString = messageLocator.getMessage(tml.messageAt(i).acquireMessageCode());
 				}
 				UIOutput.make(errorRow,"error",outString);
 		    		
 			}
         }
         
         //frameAdjustingProducer.fillComponents(arg0, "resize", "resetFrame");
         UIVerbatim.make(arg0, "resize", "\n<!-- \n\tfunction " + "resetFrame" + "()"
        	        + " {\n\t\tsetMainFrameHeight('"
        	        + deriveFrameTitle(ToolManager.getCurrentPlacement().getId())
        	        + "');\n\t\t}\n//-->\n");
    }
    
    public static final String deriveFrameTitle(String placementID) {
    	return Web.escapeJavascript("Main" + placementID);
    }
    
    public ViewParameters getViewParameters() {
        GroupEditViewParameters params = new GroupEditViewParameters();

        params.id = null;
        return params;
    }
    
    // new hotness
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        if ("done".equals(actionReturn) || "cancel".equals(actionReturn)) {
            result.resultingView = new SimpleViewParameters(GroupListProducer.VIEW_ID);
        }
    }
}
