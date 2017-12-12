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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UICSSDecorator;
import uk.org.ponder.rsf.components.decorators.UIDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.rsf.producers.FrameAdjustingProducer;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.SiteManageGroupSectionRoleHandler;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteComparator;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * 
 * @author
 *
 */
@Slf4j
public class GroupEditProducer implements ViewComponentProducer, ActionResultInterceptor, ViewParamsReporter{

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
    	Set<Member> groupMembers = new HashSet<>();
    	// group provider id
    	String groupProviderId = null;
    	// list of group role provider ids
    	Collection<String> groupRoleProviderRoles = null;
    	
    	UIForm groupForm = UIForm.make(arg0, "groups-form");

    	 String id = ((GroupEditViewParameters) arg1).id;
    	 if (id != null)
    	 {
    		 try
    		 {
    			 // SAK-29645
    			 if( handler.messages.size() == 0 )
    			 {
    				 g = siteService.findGroup(id);
    				 groupId = g.getId();
    				 groupTitle = g.getTitle();
    				 groupDescription = g.getDescription();
    				 handler.allowViewMembership = Boolean.valueOf(g.getProperties().getProperty(Group.GROUP_PROP_VIEW_MEMBERS));
    				 groupMembers = g.getMembers();
    				 groupProviderId = g.getProviderGroupId();
    				 groupRoleProviderRoles = handler.getGroupProviderRoles(g);
    				 String joinableSet = g.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
    				 if(joinableSet != null && !"".equals(joinableSet.trim())){
    					 handler.joinableSetName = joinableSet;
    					 handler.joinableSetNameOrig = joinableSet;
    					 handler.joinableSetNumOfMembers = g.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET_MAX);
    					 handler.allowPreviewMembership = Boolean.valueOf(g.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET_PREVIEW));
    					 //set unjoinable.  Since you can't change this value at the group edit page, all groups will have the same
    					 //value in the set.  Find another group in the same set (if exist) and set it to the same value.
    					 for(Group group : handler.site.getGroups()){
    			        		String joinableSetName = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
    			        		if(joinableSetName != null && joinableSetName.equals(joinableSet)){
    			        			//we only need to find the first one since all are the same
    			        			handler.unjoinable = Boolean.valueOf(group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE));
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
    	 
    	 // action button name: Add for adding new group, Update for editing exist group
    	 String addUpdateButtonName = id != null?messageLocator.getMessage("editgroup.update"):messageLocator.getMessage("editgroup.new");
    	 String headerText = id == null ? messageLocator.getMessage("group.newgroup") : messageLocator.getMessage("group.editgroup");
    	 
         UIOutput.make(groupForm, "prompt", headerText);
         UIOutput.make(groupForm, "emptyGroupTitleAlert", messageLocator.getMessage("editgroup.titlemissing"));
         
         if (g != null && g.isLocked()) {
            UIOutput.make(groupForm, "instructions", messageLocator.getMessage("editgroup.notallowed", null)); 
         } else {
            UIOutput.make(groupForm, "instructions", messageLocator.getMessage("editgroup.instruction", new Object[]{addUpdateButtonName}));
         }
         
         UIOutput.make(groupForm, "group_title_label", messageLocator.getMessage("group.title"));
         UIInput groupTitleInput = UIInput.make(groupForm, "group_title", "#{SiteManageGroupSectionRoleHandler.title}",groupTitle);     
		 
		
		 UIMessage groupDescrLabel = UIMessage.make(groupForm, "group_description_label", "group.description"); 
		 UIInput groupDescr = UIInput.make(groupForm, "group_description", "#{SiteManageGroupSectionRoleHandler.description}", groupDescription); 
		 UILabelTargetDecorator.targetLabel(groupDescrLabel, groupDescr);
		 
		 //allow view membership:
		 UIBoundBoolean viewMemCheckbox = UIBoundBoolean.make(groupForm, "allowViewMembership", "#{SiteManageGroupSectionRoleHandler.allowViewMembership}");
		 UILabelTargetDecorator.targetLabel(UIMessage.make(groupForm, "allowViewMembership-label", "group.allow.view.membership"), viewMemCheckbox);
		 
		 //Joinable Set:
		 UIMessage joinableSetLabel = UIMessage.make(groupForm, "group_joinable_set_label", "group.joinable.set");
		 List<String> joinableSetValuesSet = new ArrayList<>();
		 for(Group group : handler.site.getGroups()){
			 String joinableSet = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
			 if(joinableSet != null && !joinableSetValuesSet.contains(joinableSet)){
				 joinableSetValuesSet.add(joinableSet);
			 }
		 }
		 Collections.sort(joinableSetValuesSet);
		 List<String> joinableSetValues = new ArrayList<>();
		 List<String> joinableSetNames = new ArrayList<>();
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
			 Map<String, String> hidden = new HashMap<>();
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
		 List<String> siteMemberLabels = new ArrayList<>();
		 List<String> siteMemberValues = new ArrayList<>();
		 List<String> membersSelected;
		 if( handler.memberList != null && handler.memberList.length() > 0 )
		 {
			 membersSelected = Arrays.asList( handler.memberList.split( "##" ) );
		 }
		 else
		 {
			 membersSelected = new ArrayList<>();
		 }

		 // add site roster
		 for (String roster:siteRosters)
		 {
			 // not include in the group yet
			 if ((groupProviderId == null || !groupProviderId.contains(roster)) && !membersSelected.contains( roster ))
			 {
				 siteMemberLabels.add( messageLocator.getMessage("group.section_prefix") + handler.getRosterLabel(roster) + " (" + roster + ")");
				 siteMemberValues.add( roster );
			 }
		 }
		 // add site role
		 for (Role role:siteRoles)
		 {
			 // not include in the group yet
			 if ((groupRoleProviderRoles == null || !groupRoleProviderRoles.contains(role.getId())) && !membersSelected.contains( role.getId() ))
			 {
				 siteMemberLabels.add( messageLocator.getMessage("group.role_prefix") + role.getId() );
				 siteMemberValues.add( role.getId() );
			 }
		 }
		 // add site members to the list
		 Iterator<Participant> sIterator = new SortedIterator(siteMembers.iterator(), new SiteComparator(SiteConstants.SORTED_BY_PARTICIPANT_NAME, Boolean.TRUE.toString()));
	     while( sIterator.hasNext() ){
	        	Participant p = (Participant) sIterator.next();
	        	// not in the group yet
	        	if ((g == null || g.getMember(p.getUniqname()) == null) && !membersSelected.contains( p.getUniqname() ))
	        	{
					siteMemberLabels.add( p.getName() + " (" + p.getDisplayId() + ")" );
					siteMemberValues.add( p.getUniqname() );
	        	}
	        }

	     UISelect siteMembersSelect = UISelect.makeMultiple( groupForm, "siteMembers", siteMemberValues.toArray( new String[siteMemberValues.size()] ), 
	     	     	     	     siteMemberLabels.toArray( new String[siteMemberLabels.size()] ), 
	     	     	     	     "#{SiteManageGroupSectionRoleHandler.selectedSiteMembers}", new String[] {} );

	     /********************** for the group members list **************************/
	     List<String> groupRosters = handler.getGroupRosters(g);
	     Collection<String> groupProviderRoles = handler.getGroupProviderRoles(g);
	     List<Member> groupMembersCopy = new ArrayList<>();
	     groupMembersCopy.addAll(groupMembers);
	     for( Member p : groupMembersCopy )
	     {
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
        		log.debug(this + "fillInComponent: cannot find user with id " + userId, e);
        		// need to remove the group member
        		groupMembers.remove(p);
        	}
	     }

		 // SAK-29645
		 List<String> groupMemberLabels = new ArrayList<>();
		 List<String> groupMemberValues = new ArrayList<>();

		 // add the rosters first
		 if (groupRosters != null)
		 {
			 for (String groupRoster:groupRosters)
			 {
				 groupMemberLabels.add( messageLocator.getMessage("group.section_prefix") + handler.getRosterLabel(groupRoster) + " (" + groupRoster + ")" );
				 groupMemberValues.add( groupRoster );
			 }
		 }
		 // add the roles next
		 if (groupProviderRoles != null)
		 {
			 for (String groupProviderRole:groupProviderRoles)
			 {
				 groupMemberLabels.add( messageLocator.getMessage("group.role_prefix") + groupProviderRole );
				 groupMemberValues.add( groupProviderRole );
			 }
		 }
		 // add the members last
		 if (groupMembers != null)
		 {
			 Iterator<Member> gIterator = new SortedIterator(groupMembers.iterator(), new SiteComparator(SiteConstants.SORTED_BY_MEMBER_NAME, Boolean.TRUE.toString()));
			 while( gIterator.hasNext() ){
				 Member p = (Member) gIterator.next();
				 String userId = p.getUserId();
				 try
				 {
					 User u = userDirectoryService.getUser(userId);
					 groupMemberLabels.add( u.getSortName() + " (" + u.getDisplayId() + ")" );
					 groupMemberValues.add( userId );
				 }
				 catch (Exception e)
				 {
					 log.debug(this + ":fillComponents: cannot find user " + userId, e);
				 }
			 }
		 }

        // SAK-29645 - preserve user selected values
        if( !membersSelected.isEmpty() )
        {
            siteRosters = handler.getSiteRosters( null );
            List<String> siteRoleIDs = handler.getSiteRoleIds();
            for( String memberID : membersSelected )
            {
                // Selected roster...
                if( siteRosters.contains( memberID ) )
                {
                    groupMemberLabels.add( messageLocator.getMessage("group.section_prefix") + handler.getRosterLabel(memberID) + " (" + memberID + ")" );
                    groupMemberValues.add( memberID );
                }

                // Selected role...
                else if( siteRoleIDs.contains( memberID ) )
                {
                    groupMemberLabels.add( messageLocator.getMessage("group.role_prefix") + memberID );
                    groupMemberValues.add( memberID );
                }

                // Selected member...
                else if( groupMembers != null )
                {
                    sIterator = new SortedIterator(siteMembers.iterator(), new SiteComparator(SiteConstants.SORTED_BY_PARTICIPANT_NAME, Boolean.TRUE.toString()));
                    while( sIterator.hasNext() )
                    {
                        Participant p = (Participant) sIterator.next();
                        String userID = p.getUniqname();
                        if( StringUtils.isNotBlank( userID ) && userID.equals( memberID ) )
                        {
                            groupMemberLabels.add( p.getName() + " (" + p.getDisplayId() + ")" );
                            groupMemberValues.add( userID );
                        }
                    }
                }
            }
        }

         UISelect groupMembersSelect = UISelect.make( groupForm, "groupMembers", groupMemberValues.toArray( new String[groupMemberValues.size()] ),
                                                groupMemberLabels.toArray( new String[groupMemberLabels.size()] ), null );
    	 UICommand saveButton = UICommand.make(groupForm, "save", addUpdateButtonName, "#{SiteManageGroupSectionRoleHandler.processAddGroup}");

         UICommand cancel = UICommand.make(groupForm, "cancel", messageLocator.getMessage("editgroup.cancel"), "#{SiteManageGroupSectionRoleHandler.processBack}");
         cancel.parameters.add(new UIDeletionBinding("#{destroyScope.resultScope}"));
         
         UIInput.make(groupForm, "newRight", "#{SiteManageGroupSectionRoleHandler.memberList}", state);
         
         // hidden field for group id
         UIInput.make(groupForm, "groupId", "#{SiteManageGroupSectionRoleHandler.id}", groupId);
         
         if (g != null && g.isLocked()) {
            UIDisabledDecorator disable = new UIDisabledDecorator(true);
            groupTitleInput.decorate(disable);
            groupDescr.decorate(disable);
            viewMemCheckbox.decorate(disable);
            joinableSetSelect.decorate(disable);
            siteMembersSelect.decorate(disable);
            groupMembersSelect.decorate(disable);
            saveButton.decorate(disable);
         }
         
         //process any messages
         tml = handler.messages;
         if (tml.size() > 0) {
 			for (int i = 0; i < tml.size(); i ++ ) {
 				UIBranchContainer errorRow = UIBranchContainer.make(arg0,"error-row:", Integer.toString(i));
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
