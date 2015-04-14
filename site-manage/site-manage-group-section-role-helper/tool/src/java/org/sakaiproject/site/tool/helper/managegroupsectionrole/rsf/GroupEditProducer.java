package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import java.util.Collection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.components.decorators.UICSSDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
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
public class GroupEditProducer implements ViewComponentProducer, ActionResultInterceptor, ViewParamsReporter{

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(GroupEditProducer.class);
	
	private String SECTION_PREFIX = "Section: ";
	private String ROLE_PREFIX = "Role: ";
	
    public SiteManageGroupSectionRoleHandler handler;
    public static final String VIEW_ID = "GroupEdit";
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
    	
    	// group
    	Group g = null;
    	// id for group
    	String groupId = null;
    	// title for group
    	String groupTitle = null;
    	// description for group
    	String groupDescription = null;
    	// member list for group
    	Collection<Member> groupMembers = new Vector<Member>();
    	// group provider id
    	String groupProviderId = null;
    	// list of group role provider ids
    	List<String> groupRoleProviderRoles = null;
    	
    	UIForm groupForm = UIForm.make(arg0, "groups-form");

    	 String id = ((GroupEditViewParameters) arg1).id;
    	 if (id != null)
    	 {
    		 try
    		 {
    			 g = siteService.findGroup(id);
    			 groupId = g.getId();
    			 groupTitle = g.getTitle();
    			 groupDescription = g.getDescription();
    			 handler.allowViewMembership = Boolean.valueOf(g.getProperties().getProperty(g.GROUP_PROP_VIEW_MEMBERS));
    			 groupMembers = g.getMembers();
    			 groupProviderId = g.getProviderGroupId();
    			 groupRoleProviderRoles = handler.getGroupProviderRoles(g);
    			 String joinableSet = g.getProperties().getProperty(g.GROUP_PROP_JOINABLE_SET);
    			 if(joinableSet != null && !"".equals(joinableSet.trim())){
    				 handler.joinableSetName = joinableSet;
    				 handler.joinableSetNameOrig = joinableSet;
    				 handler.joinableSetNumOfMembers = g.getProperties().getProperty(g.GROUP_PROP_JOINABLE_SET_MAX);
    				 handler.allowPreviewMembership = Boolean.valueOf(g.getProperties().getProperty(g.GROUP_PROP_JOINABLE_SET_PREVIEW));
    				 //set unjoinable.  Since you can't change this value at the group edit page, all groups will have the same
    				 //value in the set.  Find another group in the same set (if exist) and set it to the same value.
    				 for(Group group : handler.site.getGroups()){
    		        		String joinableSetName = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);
    		        		if(joinableSetName != null && joinableSetName.equals(joinableSet)){
    		        			//we only need to find the first one since all are the same
    		        			handler.unjoinable = Boolean.valueOf(group.getProperties().getProperty(g.GROUP_PROP_JOINABLE_UNJOINABLE));
    		        			break;
    		        		}
    				 }
    			 }else{
    				 handler.joinableSetName = "";
    				 handler.joinableSetNameOrig = "";
    				 handler.joinableSetNumOfMembers = "";
    				 handler.allowPreviewMembership = false;
    				 handler.unjoinable = false;
    			 }
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
    	 
    	 // action button name: Add for adding new group, Update for editing exist group
    	 String addUpdateButtonName = id != null?messageLocator.getMessage("editgroup.update"):messageLocator.getMessage("editgroup.new");
    	 String headerText = id == null ? messageLocator.getMessage("group.newgroup") : messageLocator.getMessage("group.editgroup");
    	 
         UIOutput.make(groupForm, "prompt", headerText);
         UIOutput.make(groupForm, "emptyGroupTitleAlert", messageLocator.getMessage("editgroup.titlemissing"));
         UIOutput.make(groupForm, "instructions", messageLocator.getMessage("editgroup.instruction", new Object[]{addUpdateButtonName}));
         UIOutput.make(groupForm, "removal-warning", messageLocator.getMessage("editgroup.removal-warning"));
         
         UIOutput.make(groupForm, "group_title_label", messageLocator.getMessage("group.title"));
         UIInput titleTextIn = UIInput.make(groupForm, "group_title", "#{SiteManageGroupSectionRoleHandler.title}",groupTitle);
		 
		
		 UIMessage groupDescrLabel = UIMessage.make(groupForm, "group_description_label", "group.description"); 
		 UIInput groupDescr = UIInput.make(groupForm, "group_description", "#{SiteManageGroupSectionRoleHandler.description}", groupDescription); 
		 UILabelTargetDecorator.targetLabel(groupDescrLabel, groupDescr);
		 
		 //allow view membership:
		 UIBoundBoolean viewMemCheckbox = UIBoundBoolean.make(groupForm, "allowViewMembership", "#{SiteManageGroupSectionRoleHandler.allowViewMembership}");
		 UILabelTargetDecorator.targetLabel(UIMessage.make(groupForm, "allowViewMembership-label", "group.allow.view.membership"), viewMemCheckbox);
		 
		 //Joinable Set:
		 UIMessage joinableSetLabel = UIMessage.make(groupForm, "group_joinable_set_label", "group.joinable.set");
		 List<String> joinableSetValuesSet = new ArrayList<String>();
		 for(Group group : handler.site.getGroups()){
			 String joinableSet = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);
			 if(joinableSet != null && !joinableSetValuesSet.contains(joinableSet)){
				 joinableSetValuesSet.add(joinableSet);
			 }
		 }
		 Collections.sort(joinableSetValuesSet);
		 List<String> joinableSetValues = new ArrayList<String>();
		 List<String> joinableSetNames = new ArrayList<String>();
		 joinableSetValues.add("");
		 joinableSetNames.add(messageLocator.getMessage("none"));
		 joinableSetValues.addAll(joinableSetValuesSet);
		 joinableSetNames.addAll(joinableSetValuesSet);
		 
		 String[] joinableSetNamesArr = joinableSetNames.toArray(new String[joinableSetNames.size()]);
		 String[] joinableSetValuesArr = joinableSetValues.toArray(new String[joinableSetValues.size()]);
		 UISelect joinableSetSelect = UISelect.make(groupForm, "joinable-set", joinableSetValuesArr,
				 joinableSetNamesArr, "SiteManageGroupSectionRoleHandler.joinableSetName");
		 UILabelTargetDecorator.targetLabel(joinableSetLabel, joinableSetSelect);
		 //joinable div:
		 UIBranchContainer joinableDiv = UIBranchContainer.make(groupForm, "joinable-set-div:");
		 if(handler.joinableSetName == null || "".equals(handler.joinableSetName)){
			 Map<String, String> hidden = new HashMap<String, String>();
			 hidden.put("display", "none");
			 joinableDiv.decorate(new UICSSDecorator(hidden));
		 }
		//Max members Row:
		 UIMessage.make(joinableDiv, "group-max-members", "group.joinable.maxMembers2");
		 UIInput.make(joinableDiv, "num-max-members", "SiteManageGroupSectionRoleHandler.joinableSetNumOfMembers");
		 //allow preview row:
		 UIBoundBoolean checkbox = UIBoundBoolean.make(joinableDiv, "allowPreviewMembership", "#{SiteManageGroupSectionRoleHandler.allowPreviewMembership}");
		 UILabelTargetDecorator.targetLabel(UIMessage.make(joinableDiv, "allowPreviewMembership-label", "group.joinable.allowPreview"), checkbox);


		 UIOutput.make(groupForm, "membership_label", messageLocator.getMessage("editgroup.membership"));
		 UIOutput.make(groupForm, "membership_site_label", messageLocator.getMessage("editgroup.generallist"));
		 UIOutput.make(groupForm, "membership_group_label", messageLocator.getMessage("editgroup.grouplist"));
		 
		 /********************** for the site members list **************************/
		 List<String> siteRosters= handler.getSiteRosters(g);
		 List<Role> siteRoles= handler.getSiteRoles(g);
		 List<Participant> siteMembers= handler.getSiteParticipant(g);
		 int totalListSize = siteRosters.size() + siteRoles.size() + siteMembers.size();
		 String[] siteMemberLabels = new String[totalListSize];
		 String[] siteMemberValues = new String[totalListSize];
		 UISelect siteMember = UISelect.makeMultiple(groupForm,"siteMembers",siteMemberValues,siteMemberLabels,"#{SiteManageGroupSectionRoleHandler.selectedSiteMembers}", new String[] {});
		 
		 int i =0;
		 // add site roster
		 for (String roster:siteRosters)
		 {
			 // not include in the group yet
			 if (groupProviderId == null || !groupProviderId.contains(roster))
			 {
				 siteMemberLabels[i] = SECTION_PREFIX + roster;
				 siteMemberValues[i] = roster;
				 i++;
			 }
		 }
		 // add site role
		 for (Role role:siteRoles)
		 {
			 // not include in the group yet
			 if (groupRoleProviderRoles == null || !groupRoleProviderRoles.contains(role.getId()))
			 {
				 siteMemberLabels[i] = ROLE_PREFIX + role.getId();
				 siteMemberValues[i] = role.getId();
				 i++;
			 }
		 }
		 // add site members to the list
		 Iterator<Participant> sIterator = new SortedIterator(siteMembers.iterator(), new SiteComparator(SiteConstants.SORTED_BY_PARTICIPANT_NAME, Boolean.TRUE.toString()));
	     for (; sIterator.hasNext();i++){
	        	Participant p = (Participant) sIterator.next();
	        	// not in the group yet
	        	if (g == null || g.getMember(p.getUniqname()) == null)
	        	{
					siteMemberLabels[i] = p.getName() + " (" + p.getDisplayId() + ")";
					siteMemberValues[i] = p.getUniqname();
	        	}
	        }
	     
	     
	     /********************** for the group members list **************************/
	     // rosters
	     List<String> groupRosters = handler.getGroupRosters(g);
	     if (groupRosters != null)
	     {
	    	 totalListSize = groupRosters.size();
	     }
	     // roles
	     List<String> groupProviderRoles = handler.getGroupProviderRoles(g);
	     if (groupProviderRoles != null)
	     {
	    	 totalListSize += groupProviderRoles.size();
	     }
	     // group members
	     List<Member> groupMembersCopy = new Vector<Member>();
	     groupMembersCopy.addAll(groupMembers);
	     for (Iterator<Member> gItr=groupMembersCopy.iterator(); gItr.hasNext();){
        	Member p = (Member) gItr.next();
        	
        	// exclude those user with provided roles and rosters
        	String userId = p.getUserId();
        	try{
        		// get user
        		User u = userDirectoryService.getUser(userId);
        		if (handler.isUserFromProvider(u.getEid(), userId, g, groupRosters, groupProviderRoles))
	        	{
	        		groupMembers.remove(p);
	        	}
        	}
        	catch (Exception e)
        	{
        		M_log.debug(this + "fillInComponent: cannot find user with id " + userId);
        		// need to remove the group member
        		groupMembers.remove(p);
        	}
	     }
	     if (groupMembers != null)
	     {
	    	 totalListSize +=groupMembers.size();
	     }
	     
		 String[] groupMemberLabels = new String[totalListSize];
		 String[] groupMemberValues = new String[totalListSize];
		 UISelect groupMember = UISelect.make(groupForm,"groupMembers",groupMemberValues,groupMemberLabels,null);
		 i =0;
		 // add the rosters first
		 if (groupRosters != null)
		 {
			 for (String groupRoster:groupRosters)
			 {
				 groupMemberLabels[i] = SECTION_PREFIX + groupRoster;
				 groupMemberValues[i] = groupRoster;
				 i++;
			 }
		 }
		 // add the roles next
		 if (groupProviderRoles != null)
		 {
			 for (String groupProviderRole:groupProviderRoles)
			 {
				 groupMemberLabels[i] = ROLE_PREFIX + groupProviderRole;
				 groupMemberValues[i] = groupProviderRole;
				 i++;
			 }
		 }
		 // add the members last
		 if (groupMembers != null)
		 {
			 Iterator<Member> gIterator = new SortedIterator(groupMembers.iterator(), new SiteComparator(SiteConstants.SORTED_BY_MEMBER_NAME, Boolean.TRUE.toString()));
			 for (; gIterator.hasNext();i++){
				 Member p = (Member) gIterator.next();
				 String userId = p.getUserId();
				 try
				 {
					 User u = userDirectoryService.getUser(userId);
					 groupMemberLabels[i] = u.getSortName() + " (" + u.getDisplayId() + ")";
				 }
				 catch (Exception e)
				 {
					 M_log.debug(this + ":fillComponents: cannot find user " + userId);
				 }
				 groupMemberValues[i] = userId;
			 }
		 }
	        
    	 UICommand.make(groupForm, "save", addUpdateButtonName, "#{SiteManageGroupSectionRoleHandler.processAddGroup}");

         UICommand cancel = UICommand.make(groupForm, "cancel", messageLocator.getMessage("editgroup.cancel"), "#{SiteManageGroupSectionRoleHandler.processBack}");
         cancel.parameters.add(new UIDeletionBinding("#{destroyScope.resultScope}"));
         
         UIInput.make(groupForm, "newRight", "#{SiteManageGroupSectionRoleHandler.memberList}", state);
         
         // hidden field for group id
         UIInput.make(groupForm, "groupId", "#{SiteManageGroupSectionRoleHandler.id}", groupId);
         
         //process any messages
         tml = handler.messages;
         if (tml.size() > 0) {
 			for (i = 0; i < tml.size(); i ++ ) {
 				UIBranchContainer errorRow = UIBranchContainer.make(arg0,"error-row:", Integer.valueOf(i).toString());
 				TargettedMessage msg = tml.messageAt(i);
		    	if (msg.args != null ) 
		    	{
		    		UIMessage.make(errorRow,"error", msg.acquireMessageCode(), (Object[]) msg.args);
		    	} 
		    	else 
		    	{
		    		UIMessage.make(errorRow,"error", msg.acquireMessageCode());
		    	}	
 			}
         }
         
         frameAdjustingProducer.fillComponents(arg0, "resize", "resetFrame");
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
