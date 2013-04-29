package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import java.util.Collection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.SiteManageGroupSectionRoleHandler;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteComparator;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.ac.cam.caret.sakai.rsf.copies.Web;
import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBound;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundString;
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
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.stringutil.StringList;

/**
 * 
 * @author
 *
 */
public class GroupAutoCreateProducer implements ViewComponentProducer, ActionResultInterceptor, ViewParamsReporter {

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(GroupAutoCreateProducer.class);
	
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
    	
    	String state="";
    	
    	// id for group
    	String groupId = null;
    	// title for group
    	String groupTitle = null;
    	// description for group
    	String groupDescription = null;
    	// member list for group
    	Collection<Member> groupMembers = new Vector<Member>();
    	
    	UIForm groupForm = UIForm.make(arg0, "groups-form");

    	 String id = ((GroupEditViewParameters) arg1).id;
    	 if (id != null)
    	 {
    		 try
    		 {
    			 Group g = siteService.findGroup(id);
    			 groupId = g.getId();
    			 groupTitle = g.getTitle();
    			 groupDescription = g.getDescription();
    			 groupMembers = g.getMembers();
    		 }
    		 catch (Exception e)
    		 {
    			 M_log.debug(this + "fillComponents: cannot get group id=" + id);
    		 }
    	 }
    	 else
    	 {
    		 handler.resetParams();
    	 }
    	 

         UIOutput.make(groupForm, "prompt", messageLocator.getMessage("group.autocreate.newgroups"));
         UIOutput.make(groupForm, "instructions_roster", messageLocator.getMessage("group.autocreate.instruction_roster"));
         UIOutput.make(groupForm, "instructions_role", messageLocator.getMessage("group.autocreate.instruction_role"));
         
         UIOutput.make(groupForm, "group_label", messageLocator.getMessage("group.title"));
         UIInput titleTextIn = UIInput.make(groupForm, "group_title", "#{SiteManageGroupSectionRoleHandler.title}",groupTitle);
		 
		 // for the site rosters list
		 List<String> siteRosters= handler.getSiteRosters(null);
		 if (siteRosters != null && siteRosters.size() > 0)
		 {
			 UIBranchContainer rosterOptions = UIBranchContainer.make(groupForm, "roster_options:");
			 UIMessage.make(arg0, "roster-select-header", "table.roster_select");
			 UIMessage.make(arg0, "roster-title-header", "table.roster_title");
			 UIMessage.make(arg0, "instruction-roster", "instruction.roster");
			 for (String roster: siteRosters) {
				 UIBranchContainer tablerow = UIBranchContainer.make(rosterOptions, "roster-row:");
				 UIBoundBoolean checkbox = UIBoundBoolean.make(tablerow, "roster-checkbox", "#{SiteManageGroupSectionRoleHandler.selectedRosters." + roster.replaceAll("\\.", "-_p_-") + "}");
				 UILabelTargetDecorator.targetLabel(UIOutput.make(tablerow, "roster-title", roster), checkbox);
				 
				 // check whether there is already a group with this roster
				 if (handler.existRosterGroup(roster))
				 {
					 UIMessage.make(tablerow, "exist-group-roster", "exist.group.roster");
				 }
			 }
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
	         String [] optionValues = new String[] {
	                 Integer.toString(1), Integer.toString(2)
	         };
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
		 
    	 UICommand.make(groupForm, "save", messageLocator.getMessage("update"), "#{SiteManageGroupSectionRoleHandler.processAutoCreateGroup}");

         UICommand.make(groupForm, "cancel", messageLocator.getMessage("cancel"), "#{SiteManageGroupSectionRoleHandler.processBack}");
         
         int i = 0;
         //process any messages
         tml = handler.messages;
         if (tml.size() > 0) {
 			for (i = 0; i < tml.size(); i ++ ) {
 				UIBranchContainer errorRow = UIBranchContainer.make(arg0,"error-row:", Integer.valueOf(i).toString());
 				String outString = "";
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
