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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.rsf.producers.FrameAdjustingProducer;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.helper.managegroup.impl.SiteManageGroupHandler;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteComparator;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.SortedIterator;

/**
 * 
 * @author Dr. WHO?
 *
 */
@Slf4j
public class GroupEditProducer implements ViewComponentProducer, ActionResultInterceptor, ViewParamsReporter {

    public SiteManageGroupHandler handler;
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
    	
    	UIForm groupForm = UIForm.make(arg0, "groups-form");

    	 String id = ((GroupEditViewParameters) arg1).id;
    	 if (id != null)
    	 {
    		 try
    		 {
    			 // SAK-29645 - preserve user input
    			 if( handler.messages.size() == 0 )
    			 {
    				 g = siteService.findGroup(id);
    				 groupId = g.getId();
    				 groupTitle = g.getTitle();
    				 groupDescription = g.getDescription();
    				 groupMembers = g.getMembers();
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
    	 
         UIOutput.make(groupForm, "prompt", messageLocator.getMessage("group.newgroup"));
         UIOutput.make(groupForm, "emptyGroupTitleAlert", messageLocator.getMessage("editgroup.titlemissing"));
         UIOutput.make(groupForm, "instructions", messageLocator.getMessage("editgroup.instruction", new Object[]{addUpdateButtonName}));
         
         UIOutput.make(groupForm, "group_title_label", messageLocator.getMessage("group.title"));
         UIInput.make(groupForm, "group_title", "#{SiteManageGroupHandler.title}",groupTitle);
		 
		
		 UIMessage groupDescrLabel = UIMessage.make(arg0, "group_description_label", "group.description"); 
		 UIInput groupDescr = UIInput.make(groupForm, "group_description", "#{SiteManageGroupHandler.description}", groupDescription); 
		 UILabelTargetDecorator.targetLabel(groupDescrLabel, groupDescr);
		 
		 UIOutput.make(groupForm, "membership_label", messageLocator.getMessage("editgroup.membership"));
		 UIOutput.make(groupForm, "membership_site_label", messageLocator.getMessage("editgroup.generallist"));
		 UIOutput.make(groupForm, "membership_group_label", messageLocator.getMessage("editgroup.grouplist"));
		 
		 // for the site members list
		 List<Participant> siteMembers= handler.getSiteParticipant(g);
		 List<String> siteMemberLabels = new ArrayList<>();
		 List<String> siteMemberValues = new ArrayList<>();
		 
		 Iterator<Participant> sIterator = new SortedIterator(siteMembers.iterator(), new SiteComparator(SiteConstants.SORTED_BY_PARTICIPANT_NAME, Boolean.TRUE.toString()));
	     while( sIterator.hasNext() ){
	        	Participant p = (Participant) sIterator.next();
	        	// not in the group yet
	        	if (g == null || g.getMember(p.getUniqname()) == null)
	        	{
					siteMemberLabels.add( p.getName() + " (" + p.getDisplayId() + ")" );
					siteMemberValues.add( p.getUniqname() );
	        	}
	        }

         UISelect.makeMultiple( groupForm, "siteMembers", siteMemberValues.toArray( new String[siteMemberValues.size()]), 
                                siteMemberLabels.toArray( new String[siteMemberLabels.size()]), "#{SiteManageGroupHandler.selectedSiteMembers}", new String[] {} );

	     // for the group members list
		 List<String> groupMemberLabels = new ArrayList<>();
		 List<String> groupMemberValues = new ArrayList<>();
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
	        		// need to remove the group member
	        		groupMembers.remove(p);
	        	}
	        }

	     UISelect.make( groupForm, "groupMembers", groupMemberValues.toArray( new String[groupMemberValues.size()]), 
                        groupMemberLabels.toArray( new String[groupMemberLabels.size()]), null );
	     UICommand.make(groupForm, "save", addUpdateButtonName, "#{SiteManageGroupHandler.processAddGroup}");

         UICommand.make(groupForm, "cancel", messageLocator.getMessage("editgroup.cancel"), "#{SiteManageGroupHandler.processBack}");
         
         UIInput.make(groupForm, "newRight", "#{SiteManageGroupHandler.memberList}", state);
         
         // hidden field for group id
         UIInput.make(groupForm, "groupId", "#{SiteManageGroupHandler.id}", groupId);
         
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
