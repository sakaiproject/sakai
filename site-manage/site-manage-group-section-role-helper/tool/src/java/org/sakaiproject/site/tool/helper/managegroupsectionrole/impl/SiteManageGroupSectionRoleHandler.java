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
package org.sakaiproject.site.tool.helper.managegroupsectionrole.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;

import au.com.bytecode.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteGroupHelper;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.site.util.GroupHelper;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ResourceLoader;

/**
 * 
 * @author 
 *
 */
@Slf4j
public class SiteManageGroupSectionRoleHandler {

	private static final String REQ_ATTR_GROUPFILE = "groupfile";

	private List<Member> groupMembers;
    private final GroupComparator groupComparator = new GroupComparator();

    public MessageLocator messageLocator;
	
    public Site site = null;
    public SiteService siteService = null;
    public AuthzGroupService authzGroupService = null;
    public ToolManager toolManager = null;
    public SessionManager sessionManager = null;
    public ServerConfigurationService serverConfigurationService;
    public CourseManagementService cms = null;
    private List<Group> groups = null;
    public String memberList = "";
    public boolean update = false;
    public boolean done = false;
    
    public String[] selectedGroupMembers = new String[]{};
    public String[] selectedSiteMembers = new String[]{};
    
    // selected rosters for autocreate groups
    public Map<String, Boolean> selectedRosters = new HashMap<>();
    
    // selected roles for autocreate groups
    public Map<String, Boolean> selectedRoles = new HashMap<>();
       
    private static final int OPTION_ASSIGN_BY_ROLES_OR_ROSTER = 1;
    private static final int OPTION_ASSIGN_RANDOM = 2;
    public int optionAssign = OPTION_ASSIGN_BY_ROLES_OR_ROSTER;
    public boolean groupSplit = true;
    public String numToSplitGroup = "";
    public String numToSplitUser = "";
    public String groupTitleGroup = "";
    public String groupTitleUser = "";
    
    // SAK-29373
    public int rosterOptionAssign = OPTION_ASSIGN_BY_ROLES_OR_ROSTER;
    public boolean rosterGroupSplit = true;
    public String rosterNumToSplitGroup = "";
    public String rosterNumToSplitUser = "";
    public String rosterGroupTitleGroup = "";
    public String rosterGroupTitleUser = "";
    
    private final String HELPER_ID = "sakai.tool.helper.id";
    private final String GROUP_DELETE = "group.delete";
    private final String SITE_RESET = "group.reset";
    
    public String joinableSetName = "";
    public String joinableSetNameOrig = "";
    public String joinableSetNumOfGroups = "";
    public String joinableSetNumOfMembers = "";
    public boolean allowPreviewMembership = false;
    public boolean allowViewMembership = false;
    public boolean unjoinable = false;
    public boolean unjoinableOrig = false;
    private int groupsCreated = 0;
    public List<String> pendingGroupTitles = new ArrayList<>();

    // Tool session attribute name used to schedule a whole page refresh.
    public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh"; 

    // SAK-23016 - added CSV types from http://filext.com/file-extension/CSV
    private static final String[] CSV_MIME_TYPES = {
        "application/csv", 
        "application/excel", 
        "application/vnd.ms-excel", 
        "application/vnd.msexcel", 
        "text/anytext", 
        "text/comma-separated-values", 
        "text/csv"
    };

	public TargettedMessageList messages;
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}
	
    private void resetTargettedMessageList()
    {
    	this.messages = new TargettedMessageList();
    }
	
	private final GroupProvider groupProvider = ComponentManager.get(GroupProvider.class);
	
	// the group title
	private String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
	// group title
	private String title;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	// group description
	private String description;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// group upload textarea
	private String groupUploadTextArea;

	public String getGroupUploadTextArea() {
		return groupUploadTextArea;
	}

	public void setGroupUploadTextArea(String groupUploadTextArea) {
		this.groupUploadTextArea = groupUploadTextArea;
	}
	
	// for those to be deleted groups
	public String[] deleteGroupIds;
	
	/**
	 * reset the variables
	 */
	public void resetParams()
	{
        // SAK-29645 - don't reset the params if an error message was posted (preserve user input)
        if( messages.size() == 0 )
        {
            id = "";
            title = "";
            description ="";
            deleteGroupIds=new String[]{};
            selectedGroupMembers = new String[]{};
            selectedSiteMembers = new String[]{};
            selectedRosters = new HashMap<>();
            selectedRoles = new HashMap<>();
            memberList = new String();

            optionAssign=OPTION_ASSIGN_BY_ROLES_OR_ROSTER;
            groupSplit = true;
            numToSplitUser = "";
            numToSplitGroup = "";
            groupTitleUser = "";
            groupTitleGroup = "";

            // SAK-29373
            rosterOptionAssign = OPTION_ASSIGN_BY_ROLES_OR_ROSTER;
            rosterGroupSplit = true;
            rosterNumToSplitUser = "";
            rosterNumToSplitGroup = "";
            rosterGroupTitleUser = "";
            rosterGroupTitleGroup = "";

            importedGroups = null;
			setGroupUploadTextArea("");

            joinableSetName = "";
            joinableSetNameOrig = "";
            unjoinable = false;
            unjoinableOrig = false;
            pendingGroupTitles.clear();
            resetJoinableSetGroupParams();
        }
	}

    /**
     * Utility method to clear our the params for generating additional groups for a joinable set
     */
    public void resetJoinableSetGroupParams()
    {
        joinableSetNumOfMembers = "";
        allowPreviewMembership = false;
        allowViewMembership = false;
        joinableSetNumOfGroups = "";
        groupsCreated = 0;
    }

    /**
     * Gets the groups for the current site
     * @return Map of groups (id, group)
     */
    public List<Group> getGroups() {
        if (site == null) {
            init();
        }
        if (update) {
            groups = new ArrayList<>();
            if (site != null)
            {   
                // only show groups created by WSetup tool itself
    			Collection allGroups = (Collection) site.getGroups();
    			for (Iterator gIterator = allGroups.iterator(); gIterator.hasNext();) {
    				Group gNext = (Group) gIterator.next();
    				String gProp = gNext.getProperties().getProperty(
    						Group.GROUP_PROP_WSETUP_CREATED);
    				if (gProp != null && gProp.equals(Boolean.TRUE.toString())) {
    					groups.add(gNext);
    				}
    			}
            }
        }
        Collections.sort(groups, groupComparator);
        return groups;
    }
    
    /**
     * Gets the rosters for the current site excluding the group
     * @param group the group to be excluded from the results
     * @return List of roster ids
     */
    public List<String> getSiteRosters(Group group) {
        if (site == null) {
            init();
        }
        List<String> providerIds = null;
        
        if (update) {
            providerIds = new ArrayList<>();
            if (site != null)
            {   
                // get all provider ids
            	Set pIds = authzGroupService.getProviderIds(siteService.siteReference(site.getId()));
            	providerIds.addAll(pIds);
            	if (group != null)
            	{
            		Set groupPIds = authzGroupService.getProviderIds(siteService.siteGroupReference(site.getId(), group.getId()));
            		providerIds.removeAll(groupPIds);
            	}
            }
        }
        return providerIds;
    }
    
    /**
     * Gets the rosters for the group
     * @param g the group for which rosters are being requested
     * @return List of roster ids
     */
    public List<String> getGroupRosters(Group g) {
    	
        List<String> providerIds = null;
        
        if (update) {
            providerIds = new ArrayList<>();
            if (g != null)
            {   
            	
                // get all provider ids
            	Set pIds = authzGroupService.getProviderIds(siteService.siteGroupReference(site.getId(), g.getId()));
            	providerIds.addAll(pIds);
            }
        }
        return providerIds;
    }

    /**
     * Get the user facing label text for a given roster ID (for auto groups UI).
     * The label will be in the format of "<rosterTitle> (<rosterID>)"
     * @param rosterID the internal ID of the roster
     * @return the user facing label for the given roster
     */
    public String getRosterLabel( String rosterID )
    {
        String label = rosterID;
        try
        {
            Section s = cms.getSection( rosterID );
            if( s != null )
            {
                label = StringUtils.defaultIfBlank(s.getTitle(), rosterID);
            }
        }
        catch( IdNotFoundException ex )
        {
            log.debug( this + ".getRosterLabel: no section found for " + rosterID, ex );
        }

        return label;
    }

    /**
     * Gets the roles for the current site excluding the group
     * @param group the group to be excluded from the results
     * @return Map of groups (id, group)
     */
    public List<Role> getSiteRoles(Group group) {
        if (site == null) {
            init();
        }
        List<Role> roles = null;
        
        if (update) {
            roles = new ArrayList<>();
            if (site != null)
            {   
                // get the authz group
            	String siteReference = siteService.siteReference(site.getId());
            	try
            	{
            		AuthzGroup siteGroup = authzGroupService.getAuthzGroup(siteReference);
            		roles.addAll(siteGroup.getRoles());
            	}
            	catch (GroupNotDefinedException e)
            	{
            		log.debug(this + ".getRoles: no authzgroup found for " + siteReference, e);
            	}
            	
            	if (group != null)
            	{
            		String roleProviderId = group.getProperties().getProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID);
	            	Collection<String> groupProvidedRoles = SiteGroupHelper.unpack(roleProviderId);
	            	for(String role: groupProvidedRoles)
	            	{
	            		roles.remove(group.getRole(role));
	            	}
            	}
            }
        }
        return roles;
    }
    
    /**
     * Gets the role ids for the current site
     * @return Map of groups (id, group)
     */
    public List<String> getSiteRoleIds() {
    	List<String> rv = new ArrayList<>();
        List<Role> roles = getSiteRoles(null);
        if (roles != null)
        {
        	for(Role r:roles)
        	{
        		rv.add(r.getId());
        	}
        }
        return rv;
    }
    
    public boolean isUserFromProvider(String userEId, String userId, Group g, List<String> rosterIds, Collection<String> roleIds)
    {
    	boolean rv = false;
    	
    	// check roster first
    	if (rosterIds != null)
    	{
	    	for (int i = 0; !rv && i < rosterIds.size(); i++)
	    	{
	    		if (groupProvider != null)
	    		{
		    		String providerId = rosterIds.get(i);
			    	Map userRole = groupProvider.getUserRolesForGroup(providerId);
			    	if (userRole.containsKey(userEId))
			    	{
			    		rv =  true;
			    	}
	    		}
	    	}
    	}
    	
    	// check role next
		if (!rv && roleIds != null)
		{
			for (String roleId : roleIds)
			{
				if (g.getUserRole(userId).getId().equals(roleId))
				{
					rv =  true;
					break;
				}
			}
		}
    	
    	return rv;
    }
    
    /**
     * Gets the roles for the group
     * @param g the group for which roles are being requested
     * @return Map of groups (id, group)
     */
    public Collection<String> getGroupProviderRoles(Group g) {
        Collection<String> rv = null;
        
        if (update) {
            rv = new ArrayList<>();
            if (g != null)
            {   
                // Get the group roles
                String roleProviderId = g.getProperties().getProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID);
                rv = SiteGroupHelper.unpack(roleProviderId);
            }
        }
        return rv;
    }
    
    /**
     * Initialization method, just gets the current site in preparation for other calls
     *
     */
    public void init() {
        if (site == null) {
            String siteId = null;
            try {
                siteId = sessionManager.getCurrentToolSession()
                        .getAttribute(HELPER_ID + ".siteId").toString();
            }
            catch (NullPointerException npe) {
                // Site ID wasn't set in the helper call!!
                log.warn(npe.getMessage());
            }
            
            if (siteId == null) {
                siteId = toolManager.getCurrentPlacement().getContext();
            }
            
            try {    
                site = siteService.getSite(siteId);
                // allow update site or group membership
                update = siteService.allowUpdateSite(site.getId()) || siteService.allowUpdateGroupMembership(site.getId());
            
            } catch (IdUnusedException e) {
                // The siteId we were given was bogus
                log.warn(e.getMessage());
            }
        }
        title = "";

        if (groupMembers == null)
        {
        	groupMembers = new ArrayList<>();
        }
    }
    
    /**
     * Wrapper around siteService to save a site
     * @param site
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public void saveSite(Site site) throws IdUnusedException, PermissionException {
        siteService.save(site);
    }
    
    public List<Participant> getSiteParticipant(Group group)
    {
    	List<Participant> rv = new ArrayList<>();
    	if (site != null)
    	{
    		String siteId = site.getId();

    		List<String> providerCourseList = SiteParticipantHelper.getProviderCourseList(siteId);
    		Collection<Participant> rvCopy = SiteParticipantHelper.prepareParticipants(siteId, providerCourseList);
    		
    		// check with group attendents
    		if (group != null)
    		{
    			// need to remove those inside group already
	    		for(Participant p:rvCopy)
	    		{
	    			if (p.getUniqname() != null && group.getMember(p.getUniqname()) == null)
	    			{
	    				rv.add(p);
	    			}
	    		}
    		}
    		else
    		{
    			// if the group is null, add all site members
    			rv.addAll(rvCopy);
    		}
    	}
    	
    	return rv;
    }
    
    public List<Member> getGroupParticipant()
    {
    	
    	return groupMembers;
    }
    
    /**
     * Allows the Cancel button to return control to the tool calling this helper
     *
     * @return status
     */
    public String processCancel() {
    	// reset the warning messages
    	resetTargettedMessageList();
    	
        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);

        return "done";
    }
    
    /**
     * Cancel out of the current action and go back to main view
     * 
     * @return status
     */
    public String processBack() {
    	// reset the warning messages
    	resetTargettedMessageList();
    	
    	return "cancel";
    }
    
    public String reset() {
        try {
            siteService.save(site);
            EventTrackingService.post(
                EventTrackingService.newEvent(SITE_RESET, "/site/" + site.getId(), false));

        } 
        catch (IdUnusedException | PermissionException e) {
            log.warn(e.getMessage());
        }

        return "";
    }
    
    /**
     * Adds a new group to the current site, or edits an existing group
     * @return "success" if group was added/edited, null if something went wrong
     */
    public String processAddGroup () {

    	// reset the warning message
    	resetTargettedMessageList();
    	
        Group group;
        
        // those added user into group
        List<String> addedGroupMember = new ArrayList<>();
        
        // those deleted user from group
        List<String> removedGroupMember = new ArrayList<>();
        
        id = StringUtils.trimToNull(id);
    	
        title = StringUtils.trimToNull(title);
    	if (title == null) 
    	{
    		messages.addMessage(new TargettedMessage("editgroup.titlemissing",new Object[]{}, TargettedMessage.SEVERITY_ERROR));
    		return null;
    	}
    	else if (title.length() > SiteConstants.SITE_GROUP_TITLE_LIMIT)
    	{
    		messages.addMessage(new TargettedMessage("site_group_title_length_limit",new Object[] { String.valueOf(SiteConstants.SITE_GROUP_TITLE_LIMIT) }, TargettedMessage.SEVERITY_ERROR));
    		return null;
    	}
    	else
    	{
            String sameTitleGroupId = GroupHelper.getSiteGroupByTitle(site, title);
            // "id" will be null if adding a new group, otherwise it will be the id of the group being edited
            if (!sameTitleGroupId.isEmpty() && (id == null || !sameTitleGroupId.equals(id)))
            {
                messages.addMessage(new TargettedMessage("group.title.same", null, TargettedMessage.SEVERITY_ERROR));
                return null;
            }
    	}
    	
    	int joinableSetNumOfMembersInt;
    	if(joinableSetName != null && !"".equals(joinableSetName.trim())){
    		if(joinableSetNumOfMembers == null || "".equals(joinableSetNumOfMembers)){
    			messages.addMessage(new TargettedMessage("maxMembers.empty.alert","num-groups"));
    			return null;
    		}else{
    			try{
    				joinableSetNumOfMembersInt = Integer.parseInt(joinableSetNumOfMembers);
    				if(joinableSetNumOfMembersInt <= 0){
        				messages.addMessage(new TargettedMessage("maxMembers.zero.alert","num-max-members"));
            			return null;
        			}
    			}catch (Exception e) {
    				messages.addMessage(new TargettedMessage("maxMembers.empty.alert","num-max-members"));
    				return null;
    			}
    		}
    	}

		if (id != null)
		{
			// editing existing group
			group = site.getGroup(id);
		}
		else
		{
			// adding a new group
	        group= site.addGroup();
	        group.getProperties().addProperty(Group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
		}
		
		if (group != null)
		{
			log.debug("Check if the group is locked : {}", group.isLocked());
			if(group.isLocked()) {
				messages.addMessage(new TargettedMessage("editgroup.group.locked",new Object[]{}, TargettedMessage.SEVERITY_ERROR));
				return null;
			}
			
			group.setTitle(title);
            group.setDescription(description);
            group.getProperties().addProperty(Group.GROUP_PROP_VIEW_MEMBERS, Boolean.toString(allowViewMembership));
            if(joinableSetName != null && !"".equals(joinableSetName.trim())){
            	group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET, joinableSetName);
            	group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET_MAX, joinableSetNumOfMembers);
            	group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET_PREVIEW,Boolean.toString(allowPreviewMembership));
            	group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE, Boolean.toString(unjoinable));
            }else{
            	group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET);
            	group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET_MAX);
            	group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET_PREVIEW);
            	group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE);
            }
            
            boolean found;
            // remove those no longer included in the group
			Set members = group.getMembers();
			String[] membersSelected = (memberList != null && memberList.length() > 0) ? memberList.split("##"):new String[0];
			for (Iterator iMembers = members.iterator(); iMembers
					.hasNext();) {
				found = false;
				String mId = ((Member) iMembers.next()).getUserId();
				for (int i = 0; !found && i < membersSelected.length; i++)
				{
					if (mId.equals(membersSelected[i])) {
						found = true;
					}

				}
				if (!found) {
					try {
						group.deleteMember(mId);
						removedGroupMember.add("uid=" + mId + ";groupId=" + group.getId());
					} catch (IllegalStateException e) {
						log.error(".processAddGroup: User with id {} cannot be deleted from group with id {} because the group is locked", mId, group.getId());
						return null;
					}
				}
			}

			// add those selected members
			List<String> siteRosters = getSiteRosters(null);
			List<String> siteRoles = getSiteRoleIds();
			List<String> selectedRosters = new ArrayList<>();
			List<String> selectedRoles = new ArrayList<>();
            for( String memberId : membersSelected )
            {
                if (siteRosters.contains(memberId))
                {
                    // this is a roster
                    selectedRosters.add(memberId);
                    // TODO: log event for each individual user?
                }
                else if (siteRoles.contains(memberId))
                {
                    // this is a role
                    Set roleUsers = site.getUsersHasRole(memberId);
                    for (Iterator iRoleUsers = roleUsers.iterator(); iRoleUsers.hasNext();)
                    {
                        String roleUserId = (String) iRoleUsers.next();
                        Member member = site.getMember(roleUserId);
                        try {
                            group.insertMember(roleUserId, memberId, member.isActive(), false);
                            addedGroupMember.add("uid=" + roleUserId + ";role=" + member.getRole().getId() + ";active=" + member.isActive() + ";provided=false;groupId=" + group.getId());
                        } catch (IllegalStateException e) {
                            log.error(".processAddGroup: User with id {} cannot be inserted in group with id {} because the group is locked", roleUserId, group.getId());
                            return null;
                        }
                    }
                    selectedRoles.add(memberId);
                }
                else
                {
                    // normal user id
                    String userId = StringUtils.trimToNull(memberId);
                    if (userId != null && group.getUserRole(userId) == null) {
                        Member m = site.getMember(userId);
                        // User isn't a member of the site, refusing to add them to the group.
                        if (m != null) {
                            Role memberRole = m.getRole();
                            try {
                                group.addMember(userId, memberRole.getId(), m.isActive(), false);
                                addedGroupMember.add("uid=" + userId + ";role=" + memberRole.getId() + ";active=" + m.isActive() + ";provided=false;groupId=" + group.getId());
                            } catch (IllegalStateException e) {
                                log.error(".processAddGroup: User with id {} cannot be inserted in group with id {} because the group is locked", memberId, group.getId());
                                return null;
                            }
                        } else {
                            String displayName;
                            try {
                                displayName = userDirectoryService.getUser(userId).getDisplayName();
                            } catch (UserNotDefinedException e) {
                                displayName = messageLocator.getMessage("user.unknown");
                            }
                            messages.addMessage(new TargettedMessage("user.not.member.alert", new String[]{displayName}, "groupMembers-selection"));
                            return null;
                        }
                    }
                }
            }
			if (!selectedRosters.isEmpty())
			{
				//since RSF doesn't like "."s, they have been escaped.  Now unescape them
				for(String s : selectedRoles){
					s = s.replaceAll("-_p_-", ".");
				}
				// set provider id
				group.setProviderGroupId(groupProvider.packId(selectedRosters.toArray(new String[selectedRosters.size()])));
			}
			else
			{
				// clear the provider id
				group.setProviderGroupId(null);
			}
			if (!selectedRoles.isEmpty())
			{
				// pack the role provider id and add to property
    			group.getProperties().addProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID, SiteGroupHelper.pack(selectedRoles));
			}
			else
			{
				// clear the role provider id
				group.getProperties().removeProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID);
			}
	            
    		// save the changes
    		try
    		{
    			siteService.save(site);
    			
    			// post event about the participant update
				EventTrackingService.post(EventTrackingService.newEvent(SiteService.SECURE_UPDATE_GROUP_MEMBERSHIP, group.getId(),true));
			
				if (serverConfigurationService.getBoolean(SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, false))
				{
					// added members
					for(String addedMemberString : addedGroupMember)
					{
						// an event for each individual member add
						EventTrackingService.post(EventTrackingService.newEvent(SiteService.EVENT_USER_GROUP_MEMBERSHIP_ADD, addedMemberString, true/*update event*/));
					}
					
					// removed members
					for(String removedMemberString : removedGroupMember)
					{
						// an event for each individual member remove
						EventTrackingService.post(EventTrackingService.newEvent(SiteService.EVENT_USER_GROUP_MEMBERSHIP_REMOVE, removedMemberString, true/*update event*/));
				
					}
				}
    			// reset the form params
    			resetParams();
	        } 
	        catch (IdUnusedException | PermissionException e) {
	        	log.error(this + ".processAddGroup: cannot find site " + site.getId(), e);
	            return null;
	        }
    	}
        
        return "success";
    }
    
    public String processConfirmGroupDelete()
    {

    	// reset the warning messages
    	resetTargettedMessageList();
    	
    	if (deleteGroupIds == null || deleteGroupIds.length == 0)
    	{
    		// no group chosen to be deleted
    		log.debug(this + ".processConfirmGroupDelete: no group chosen to be deleted.");
    		messages.addMessage(new TargettedMessage("delete_group_nogroup","no group chosen"));
    		return null;
    	}
    	else
    	{
    		List<Group> groups = new ArrayList<>();
    		
            for( String groupId : deleteGroupIds )
            {
                try
                {
                    Group g = site.getGroup(groupId);
                    groups.add(g);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage());
                }
            }
	    	return "confirm";
    	}
    }
    
    public String processDeleteGroups()
    {
    	// reset the warning messages
    	resetTargettedMessageList();

    	// Trick to refresh site
    	site = null;
    	this.init();
    	
    	if (site != null)
    	{
            List<String> notDeletedGroupsTitles = new ArrayList<String>();
            for( String groupId : deleteGroupIds )
            {
                Group g = site.getGroup(groupId);
                if (g != null) {
                    try {
                        site.deleteGroup(g);
                    } catch (IllegalStateException e) {
                        notDeletedGroupsTitles.add(g.getTitle());
                        log.error(".processDeleteGroups: Group with id {} cannot be removed because is locked", g.getId());
                    }
                }
            }
            if (!notDeletedGroupsTitles.isEmpty()) {
                StringJoiner groupsTitles = new StringJoiner(", ");
                for (String groupTitle:notDeletedGroupsTitles) {
                    groupsTitles.add(groupTitle.toString());
                }
                messages.addMessage(new TargettedMessage("deletegroup.notallowed.groups.remove", new Object[]{groupsTitles.toString()}, TargettedMessage.SEVERITY_ERROR));
            }
			try {
				siteService.save(site);
			} catch (IdUnusedException e) {
				messages.addMessage(new TargettedMessage("editgroup.site.notfound.alert","cannot find site"));
				log.error(this + ".processDeleteGroups: Problem of saving site after group removal: site id =" + site.getId(), e);
			} catch (PermissionException e) {
				messages.addMessage(new TargettedMessage("editgroup.site.permission.alert","not allowed to find site"));
				log.error(this + ".processDeleteGroups: Permission problem of saving site after group removal: site id=" + site.getId(), e);
			}
	    	
	    }
    	return "success";
    }
    
    public String processCancelDelete()
    {
    	// reset the warning messages
    	resetTargettedMessageList();
    	
    	return "cancel";
    }
    
    public String processCancelGroups()
    {
        // reset the warning messages
        resetTargettedMessageList();

        return "returnToGroupList";
    }
    
    /**
     * SAK-29373 - Common validation algorithm for both roster and role based random groups
     * 
     * @param option flag denoting split by random options, or create group(s) for roster and/or role (1:1)
     * @param isGroupSplit true = split by number of groups, false = split by number of users per group
     * @param numToSplitForGroups the number of groups requested
     * @param numToSplitForUsers the number of users per group requested
     * @param groupTitleForGroups title requested for split by groups
     * @param groupTitleForUsers title requested for split by number of users per group
     * @return 0 if not for random groups, -1 if for random groups and validation fails. Otherwise, returns the 
     * integer value of the number of groups or number of users per group requested.
     */
    private int validateAutoGroupsFields( int option, boolean isGroupSplit, String numToSplitForGroups, String numToSplitForUsers,
                                          String groupTitleForGroups, String groupTitleForUsers)
    {
        int intToSplit = 0;
        if( OPTION_ASSIGN_RANDOM == option )
        {
            String numToSplit = isGroupSplit ? numToSplitForGroups : numToSplitForUsers;
            String groupTitle = isGroupSplit ? groupTitleForGroups : groupTitleForUsers;
            if( numToSplit == null )
            {
                if( isGroupSplit )
                {
                    messages.addMessage( new TargettedMessage( "numToSplit.group.empty.alert", "numToSplit" ) );
                }
                else
                {
                    messages.addMessage( new TargettedMessage( "numToSplit.user.empty.alert", "numToSplit" ) );
                }
                return -1;
            }
            else
            {
                try
                {
                    intToSplit = Integer.parseInt( numToSplit );
                    if( intToSplit <= 0 )
                    {
                        return addAppropriateNotNumberError( isGroupSplit );
                    }
                }
                catch( NumberFormatException ex )
                {
                    return addAppropriateNotNumberError( isGroupSplit );
                }
            }

            if( groupTitle == null || "".equals( groupTitle ) )
            {
                messages.addMessage( new TargettedMessage( "groupTitle.empty.alert", "groupTitle" ) );
                return -1;
            }
        }
        
        return intToSplit;
    }

     /**
     * Add the appropriate 'is not a number' error to the UI
     * 
     * @param isGroupSplit true for split on # of groups, false for split on # of users per group
     * @return -1 indicating validation failure
     */
    private int addAppropriateNotNumberError( boolean isGroupSplit )
    {
        if( isGroupSplit )
        {
            messages.addMessage( new TargettedMessage( "numToSplit.group.notanumber.alert", "numToSplit" ) );
        }
        else
        {
            messages.addMessage( new TargettedMessage( "numToSplit.user.notanumber.alert", "numToSplit" ) );
        }
        return -1;
    }

    /**
     * auto create group(s) based on selected roster(s) or role(s)
     *
     * @return status
     */
    public String processAutoCreateGroup() {
        // reset the warning messages
        resetTargettedMessageList();

        // Check if fields are valid
        int intToSplit = validateAutoGroupsFields( optionAssign, groupSplit, numToSplitGroup, numToSplitUser, groupTitleGroup, groupTitleUser );
        int rosterIntToSplit = validateAutoGroupsFields( rosterOptionAssign, rosterGroupSplit, rosterNumToSplitGroup, rosterNumToSplitUser, rosterGroupTitleGroup, rosterGroupTitleUser );
        if( intToSplit == -1 || rosterIntToSplit == -1 )
        {
            return null;
        }

        List<String> rosterList = new ArrayList<>();
        if( selectedRosters != null )
        {
            for( String roster : selectedRosters.keySet() )
            {
                if (Boolean.TRUE.equals(selectedRosters.get(roster)))
                {
                    // RSF doesn't like periods, so these have been escaped; now unescape them
                    roster = roster.replaceAll("-_p_-", ".");
                    rosterList.add(roster);
                }
            }
        }

        List<String> roleList = new ArrayList<>();
        if( selectedRoles != null )
        {
            for( String role : selectedRoles.keySet() )
            {
                if (Boolean.TRUE.equals(selectedRoles.get(role)))
                {
                    // selected role
                    roleList.add(role);
                }
            }
        }

        if (rosterList.isEmpty() && roleList.isEmpty())
        {
            // nothing selected, generate alert
            messages.addMessage(new TargettedMessage("group.autocreate.selectrosterorrole","Please select at lease one roster or role."));
        }
        else
        {
            // Roster based
            if (!rosterList.isEmpty())
            {
                // SAK-29373
                if( OPTION_ASSIGN_RANDOM == rosterOptionAssign )
                {
                    createRandomGroupsForRoster( rosterList.get( 0 ), rosterIntToSplit );
                }
                else
                {
                    for (String roster:rosterList)
                    {
                        Group group = site.addGroup();
                        group.getProperties().addProperty(Group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
                        group.setProviderGroupId(roster);

                        String groupTitle = truncateGroupTitle( getRosterLabel( roster ) + " " + messageLocator.getMessage( "group.autocreate.section.postfix" ) );
                        group.setTitle(groupTitle);
                    }
                }
            }

            // role based
            if (!roleList.isEmpty())
            {
                if(OPTION_ASSIGN_BY_ROLES_OR_ROSTER == optionAssign){
                    for(String role:roleList)
                    {
                        Group group = site.addGroup();
                        // make the provider id as of SITEID_ROLEID
                        //group.setProviderGroupId(site.getId() + "_" + role);
                        group.getProperties().addProperty(Group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
                        group.getProperties().addProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID, role);

                        String groupTitle = truncateGroupTitle(role);
                        group.setTitle(groupTitle);

                        // get the authz group
                        String siteReference = siteService.siteReference(site.getId());
                        try
                        {
                            AuthzGroup siteGroup = authzGroupService.getAuthzGroup(siteReference);
                            Set<String> usersHasRole = siteGroup.getUsersHasRole(role);
                            if (usersHasRole != null)
                            {
                                for( String userId : usersHasRole )
                                {
                                    Member member = site.getMember(userId);
                                    try {
                                        group.insertMember(userId, role, member.isActive(), false);
                                    } catch (IllegalStateException e) {
                                        log.error(".processAutoCreateGroup: User with id {} cannot be inserted in group with id {} because the group is locked", userId, group.getId());
                                    }
                                }
                            }
                        }
                        catch (GroupNotDefinedException e)
                        {
                            log.debug(this + ".processAutoCreateGroup: no authzgroup found for " + siteReference, e);
                        }
                    }
                }else{
                    createRandomGroupsForRole( roleList.get( 0 ), intToSplit );
                }
            }

            // save the changes
            try
            {
                siteService.save(site);
                // reset the form params
                resetParams();
            } 
            catch (IdUnusedException | PermissionException e) {
                log.error(this + ".processAutoCreateGroup: cannot find site " + site.getId(), e);
                return null;
            }
        }

        return "done";
    }
    
    /**
     * SAK-29373 - Create random groups of users from the given roster.
     * This could be random number of users in specified number of groups,
     * or vice versa.
     * 
     * @param providerID the provider ID selected
     * @param unit the number of groups or users per group requested
     */
    private void createRandomGroupsForRoster( String providerID, int unit )
    {
        String groupTitle = rosterGroupSplit ? rosterGroupTitleGroup: rosterGroupTitleUser;
        if( StringUtils.isNotBlank( groupTitle ) && StringUtils.isNotBlank( providerID ) )
        {
            Set<String> userSet = new HashSet<>();
            List<AuthzGroup> realms = authzGroupService.getAuthzGroups( providerID, null );
            for( AuthzGroup realm : realms )
            {
                if( providerID.equals( realm.getProviderGroupId() ) )
                {
                    Set<Member> members = realm.getMembers();
                    for( Member member : members )
                    {
                        userSet.add( member.getUserId() );
                    }
                }
            }

            createRandomGroups( rosterGroupSplit, new ArrayList<>( userSet ), groupTitle, unit );
        }
    }

    /**
     * Common algorithm called to create random groups.
     * 
     * @param isGroupSplit true = split by # of groups, false = split by # of users per group
     * @param userIDs list of user IDs to include in the groups
     * @param groupTitle the title prefix of the groups requested
     * @param unit the amount of groups or users per group requested
     */
    private void createRandomGroups( boolean isGroupSplit, List<String> userIDs, String groupTitle, int unit )
    {
        int numOfGroups = -1;
        int numOfUsersPerGroup = -1;
        int totalNumOfUsers = userIDs.size();
        
        // Determine number of users per group and number of groups to create
        if( isGroupSplit )
        {
            numOfGroups = unit > totalNumOfUsers ? totalNumOfUsers : unit;
            if( numOfGroups > 0 )
            {
                numOfUsersPerGroup = totalNumOfUsers / numOfGroups;
            }
        }
        else
        {
            numOfUsersPerGroup = unit > totalNumOfUsers ? totalNumOfUsers : unit;
            if( numOfUsersPerGroup > 0 )
            {
                numOfGroups = totalNumOfUsers / numOfUsersPerGroup;
            }
        }

        int groupCount = 0;
        List<Group> groupList = new ArrayList<>();
        while( groupCount < numOfGroups )
        {
            // Create the group
            Group group = site.addGroup();
            group.getProperties().addProperty( Group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString() );
            group.setTitle( groupTitle + "-" + (groupCount + 1) );

            int userCount = 0;
            while( userCount < numOfUsersPerGroup && userIDs.size() > 0 )
            {
                // Pick a random user
                int index = (int)(Math.random() * (userIDs.size() - 1));
                String userID = userIDs.get( index );
                Member member = site.getMember( userID );

                // Add the user to the group
                try {
                    group.insertMember( userID, member.getRole().getId(), member.isActive(), false );
                } catch (IllegalStateException e) {
                    log.error(".createRandomGroups: User with id {} cannot be inserted in group with id {} because the group is locked", userID, group.getId());
                }
                userIDs.remove( index );
                userCount++;
            }

            groupCount++;
            groupList.add( group );
        }

        // All the groups have been created, but there are some users still left to be assigned (remainders)
        while( userIDs.size() > 0 )
        {
            // Pick a random user
            int userIndex = (int)(Math.random() * (userIDs.size() - 1));
            String userID = userIDs.get( userIndex );

            // Pick a random group
            int groupIndex = (int)(Math.random() * (groupList.size() - 1));
            Group group = groupList.get( groupIndex );

            // Add the user to the group
            Member member = site.getMember( userID );
            try {
                group.insertMember( userID, member.getRole().getId(), member.isActive(), false );
            } catch (IllegalStateException e) {
                log.error(".createRandomGroups: User with id {} cannot be inserted in group with id {} because the group is locked", userID, group.getId());
            }
            userIDs.remove( userIndex );
        }
    }

    /**
     * Create random groups of users from the given role.
     * This could be random number of users in specified number of groups,
     * or vice versa.
     * 
     * @param role the desired role to create groups for
     * @param unit the number of groups or users per group requested
     */
    private void createRandomGroupsForRole( String role, int unit )
    {
        String groupTitle = groupSplit ? groupTitleGroup : groupTitleUser;
        if( StringUtils.isNotBlank( groupTitle ) && StringUtils.isNotBlank( role ) )
        {
            List<String> usersList = new ArrayList<>();
            String siteReference = siteService.siteReference( site.getId() );

            try
            {
                // Build the user ID list
                AuthzGroup siteGroup = authzGroupService.getAuthzGroup( siteReference );
                Set<String> usersWithRole = siteGroup.getUsersHasRole( role );
                if( usersWithRole != null )
                {
                    for( String userID : usersWithRole )
                    {
                        usersList.add( userID );
                    }
                }
            }
            catch( GroupNotDefinedException ex )
            {
                log.debug( this + ".processAutoCreateGroup: no authzgroup found for " + siteReference, ex );
            }

            createRandomGroups( groupSplit, usersList, groupTitle, unit );
        }
    }

    /**
     * check whether the title string is within length limit, truncate it if necessary
     * @param oTitle
     * @return
     */
	private String truncateGroupTitle(String oTitle) {
		if (oTitle.length() > SiteConstants.SITE_GROUP_TITLE_LIMIT)
		{
			oTitle = oTitle.substring(0, SiteConstants.SITE_GROUP_TITLE_LIMIT);
		}
		return oTitle.trim();
	}

    /**
     * Removes a group from the site
     * 
     * @param groupId
     * @return title of page removed
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public String removeGroup(String groupId)
                            throws IdUnusedException, PermissionException {
        Group group = site.getGroup(groupId);
        site.removeGroup(group);
        saveSite(site);

        EventTrackingService.post(
            EventTrackingService.newEvent(GROUP_DELETE, "/site/" + site.getId() +
                                          "/group/" + group.getId(), false));
        
        return group.getTitle();
    }

	public String[] getDeleteGroupIds() {
		return deleteGroupIds;
	}
	
	public List<Group> getSelectedGroups()
	{
		List<Group> rv = new ArrayList<>();
		if (deleteGroupIds != null && deleteGroupIds.length > 0 && site != null)
		{
            for( String groupId : deleteGroupIds )
            {
                try
                {
                    Group g = site.getGroup(groupId);
                    rv.add(g);
                }
                catch (Exception e)
                {
                    log.debug(this + ":getSelectedGroups: cannot get group with id " + groupId);
                }
            }
		}
		return rv;
	}

	public void setDeleteGroupIds(String[] deleteGroupIds) {
		this.deleteGroupIds = deleteGroupIds;
	}
	
    /**
     * Gets the current tool
     * @return Tool
     */
    public Tool getCurrentTool() {
        return toolManager.getCurrentTool();
    }
    
    /**
     * check whether there is already a group within the site containing the roster id
     * @param rosterId
     * @return
     */
    public boolean existRosterGroup(String rosterId)
    {
    	boolean rv = false;
    	
    	Collection<Group> groups = site.getGroups();
    	
    	for(Group group:groups)
    	{
    		// check if there is one group with this roster id already
    		String groupWSetupCreated = group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED);
			if (groupWSetupCreated != null && groupWSetupCreated.equalsIgnoreCase(Boolean.TRUE.toString()))
			{
				if (group.getProviderGroupId() != null && group.getProviderGroupId().equals(rosterId))
				{
					rv = true;
					break;
				}
			}
    	}
		
    	return rv;
    }
    
    /**
     * Check whether there is already a group within the site containing the role id
     * @param roleId This role to check the site groups against. eg: access.
     * @return <code>true</code> if this a group for this role already exists.
     */
    public boolean existRoleGroup(String roleId)
    {
    	boolean rv = false;
    	
    	Collection<Group> groups = site.getGroups();
    	
    	for(Group group:groups)
    	{
    		// check if there is one group with this roster id already
    		String groupWSetupCreated = group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED);
			if (groupWSetupCreated != null && groupWSetupCreated.equalsIgnoreCase(Boolean.TRUE.toString()))
			{
				String groupRole = group.getProperties().getProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID);
				if (groupRole != null && groupRole.equals(roleId))
				{
					rv = true;
					break;
				}
			}
    	}
		
    	return rv;
    }

    /**
     * Find all users in the current site who are not a member of any of a set's
     * given groups
     * @param setGroups all existing groups for the set
     * @return a list of all users who are not in any of the set's groups
     */
    public List<User> getUsersNotInJoinableSet( List<Group> setGroups )
    {
        // Build a set of all user IDs in any of the set's groups
        Set<String> usersInSet = new HashSet<>();
        for( Group setGroup : setGroups )
        {
            usersInSet.addAll( setGroup.getUsers() );
        }

        // Find users of the site with specified role(s) who are not in any of the set's groups
        List<User> usersNotInSet = new ArrayList<>();
        for( String userID : site.getUsers() )
        {
            if( !usersInSet.contains( userID ) )
            {
                try
                {
                    usersNotInSet.add( userDirectoryService.getUser( userID ) );
                }
                catch( UserNotDefinedException ex )
                {
                    log.debug( this + ".getUsersNotInJoinableSet: can't find user for " + userID, ex );
                }
            }
        }

        // Return sorted list of users not in the set
        Collections.sort( usersNotInSet, new UserComparator() );
        return usersNotInSet;
    }

    /**
    * Sorts users by last name, first name, display id
    * 
    * @author <a href="mailto:carl.hall@et.gatech.edu">Carl Hall</a>
    */
    private static class UserComparator implements Comparator<User>
    {
        public int compare(User user1, User user2)
        {
            String displayName1 = user1.getLastName() + ", " + user1.getFirstName() + " ("
                    + user1.getDisplayId() + ")";
            String displayName2 = user2.getLastName() + ", " + user2.getFirstName() + " ("
                    + user2.getDisplayId() + ")";
            return displayName1.compareTo( displayName2 );
        }
    }

    /** 
     ** Comparator for sorting Group objects
     **/
    private class GroupComparator implements Comparator {
       public int compare(Object o1, Object o2) {
          return ((Group)o1).getTitle().compareToIgnoreCase( ((Group)o2).getTitle() );
       }
    }
    
    /**
     * Grabs the uploaded file from the groupfile request attribute, as well as any data in the HTML textarea,
     * and extracts any group details from them, adding them to the importedGroups list as it goes.
     * Expects at two columns, the groupname and the username/email address.
     * @return status
     */
    public String processUploadAndCheck() {
        HttpServletRequest request = (HttpServletRequest) ComponentManager.get(ThreadLocalManager.class).get(RequestFilter.CURRENT_HTTP_REQUEST);
        String uploadsDone = (String) request.getAttribute(RequestFilter.ATTR_UPLOADS_DONE);

        FileItem usersFileItem;
        String processingFlag = "success";

        if (uploadsDone != null && uploadsDone.equals(RequestFilter.ATTR_UPLOADS_DONE)) {

            try {
                usersFileItem = (FileItem) request.getAttribute(REQ_ATTR_GROUPFILE);
                // Check for nothing to upload.
                if (getGroupUploadTextArea().length() == 0 && usersFileItem.getSize() == 0) {
                    messages.addMessage(new TargettedMessage("import1.error.no.content", null, TargettedMessage.SEVERITY_ERROR));
                    return null;
                }

                importedGroups = new ArrayList<>();
                List<String[]> lines;

                // Process any data in the uploaded file.
                if(usersFileItem != null && usersFileItem.getSize() > 0) {

                    String mimetype = usersFileItem.getContentType();
                    String filename = usersFileItem.getName();

                    if (ArrayUtils.contains(CSV_MIME_TYPES, mimetype) 
                            || StringUtils.endsWith(filename, "csv")) {
                        log.debug("CSV file uploaded");

                        CSVReader reader;
                        lines = new ArrayList<>();

                        try {
                            reader = new CSVReader(new InputStreamReader(usersFileItem.getInputStream()));
                            lines = reader.readAll();
                        } catch (IOException ioe) {
                            log.error(ioe.getClass() + " : " + ioe.getMessage());
                            processingFlag = "error";
                        }

                        if (processUploadGroupLines(lines)) {
                            processingFlag = "success"; // SHORT CIRCUIT
                        }
                    } else {
                        log.error("Invalid file type: " + mimetype);
                        messages.addMessage(new TargettedMessage("import1.error.file.type.invalid", null, TargettedMessage.SEVERITY_ERROR));
                        processingFlag = null;
                    }
                }

                // Process any data in the HTML text area.
                if (getGroupUploadTextArea().length() > 0) {
                    String[] splitLines = getGroupUploadTextArea().split("\r\n");
                    lines = new ArrayList<>();
                    for (String s: splitLines) {
                        lines.add(s.split(","));
                    }
                    
                    if (processUploadGroupLines(lines)) {
                        processingFlag = "success"; // SHORT CIRCUIT
                    }

                }
            }
            catch (Exception e){
                log.error(e.getClass() + " : " + e.getMessage());
                processingFlag =  "error"; // SHORT CIRCUIT
            }
        }

        return processingFlag;
    }

	/**
	 * Helper to process each line of group values.
	 * 
	 * @param lines
	 * @return
	 */
    private boolean processUploadGroupLines(List<String[]> lines){

		//maintain a map of the groups and their titles in case the group lines are unordered
		//this way we can still lookup the group and add members to it if they are not already there.
		Map<String, ImportedGroup> groupMap = new HashMap<>();
		for (ImportedGroup g: importedGroups) {
			groupMap.put(g.getGroupTitle(), g);
		}

		for(String[] line: lines){
			if (line.length < 2) {
				messages.addMessage(new TargettedMessage("import1.error.invalid.data.format", null, TargettedMessage.SEVERITY_ERROR));
				return false;
			}
			String groupTitle = StringUtils.trimToNull(line[0]);
			String userId = StringUtils.trimToNull(line[1]);
			if (groupTitle == null || userId == null) {
				messages.addMessage(new TargettedMessage("import1.error.invalid.data.format", null, TargettedMessage.SEVERITY_ERROR));
				return false;
			}
			// if we already have an occurrence of this group add the user, otherwise create a new group.
			if(groupMap.containsKey(groupTitle)){
				ImportedGroup group = groupMap.get(groupTitle);
				group.addUser(userId);
			} else {
				ImportedGroup group = new ImportedGroup(groupTitle, userId);
				groupMap.put(groupTitle, group);
			}
		}

		//extract all of the imported groups from the map, clear out current ones first to avoid dupes.
		importedGroups.clear();
		importedGroups.addAll(groupMap.values());

		return true;
	}

	/**
	 * Attempt to find a user, searches by EID, AID and email.
	 *
	 * @param search The search.
	 * @return A user ID or <code>null</code> if none or multiple were found.
	 */
	public String lookupUser(String search) {
		User user = null;
		try {
			user = userDirectoryService.getUserByAid(search);
		} catch (UserNotDefinedException e) {
				if (search.contains("@")) {
					Collection<User> users = userDirectoryService.findUsersByEmail(search);
					Iterator<User> usersIterator = users.iterator();
					if (usersIterator.hasNext()) {
						user = usersIterator.next();
						if (usersIterator.hasNext()) {
							// Too many matches
							user = null;
						}
					}
				}

			}
		return (user == null)? null : user.getId();
	}
	
	/**
	 * Helper to check for a valid user in a site, given an eid
	 * @param userId the user's ID.
	 * @return <code>true</code> if the user is a member of the site.
	 */
	public boolean isValidSiteUser(String userId) {
		Member m = site.getMember(userId);
		return m != null;
	}

	/**
	 * Does the actual import of the groups into the site.
	 * @return
	 */
	public String processImportedGroups() {
		
		//get current groups in this site
		List<Group> existingGroups = getGroups();
		
		//for each imported group...
		for(ImportedGroup importedGroup: importedGroups) {
			
			Group group = null;
			
			//check if the groups already exists
			for(Group g : existingGroups) {
            	if(StringUtils.equals(g.getTitle(), importedGroup.getGroupTitle())) {
            		//use existing group
            		group = g;
            	}
			}
			
			if(group == null){
        		//create new group
    	        group= site.addGroup();
    	        group.getProperties().addProperty(Group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
    	        group.setTitle(importedGroup.getGroupTitle());
			}
			
    		// add all of the imported members to the group
    		for(String userId : importedGroup.getUserIds()) {
    			this.addUserToGroup(userId, group);
    		}

    		try {
    			siteService.save(site);
    		} catch (IdUnusedException | PermissionException e) {
            	log.error("processImportedGroups failed for site: " + site.getId(), e);
            	return "error";
    		}
		}
		
		resetParams();
		return "success";
	}
	
	/**
	 * Helper to get a list of user display ids in a group
	 * @param g	the group
	 * @return
	 */
	public List<String> getGroupUserIds(Group g) {
		
		List<String> userIds = new ArrayList<>();

		if(g == null) {
			return userIds;
		}
		
		Set<Member> members= g.getMembers();
		for(Member m: members) {
			userIds.add(m.getUserDisplayId());
		}
		return userIds;
	}
	/**
	 * Helper to get a user's name for display in the format surname, first name.
	 * @param userId	authentication ID of the user
	 * @return
	 */
	public String getUserSortName(String userId) {
		// Return the userId if the user does not exist.
		String sortName = userId;
		try
		{
			sortName = userDirectoryService.getUserByAid(userId).getSortName();
		}
		catch( UserNotDefinedException ex )
		{
			log.debug( this + ".getUserSortName: can't find user for " + userId, ex );
		}
		return sortName;
	}
	/**
	 * Helper to add a user to a group. Takes care of the role selection.
	 * @param id	eid of the user eg jsmith26
	 * @param g		the group
	 */
	private void addUserToGroup(String id, Group g) {
		
		//is this a valid site user?
		if(!isValidSiteUser(id)){
			return;
		}
		
		//is user already in the group?
		if(g.getUserRole(id) != null) {
			return;
		}
		
		//add user to group with correct role. This is the same logic as above
		Role r = site.getUserRole(id);
		Member m = site.getMember(id);
		Role memberRole = m != null ? m.getRole() : null;
		try {
			g.insertMember(id, r != null ? r.getId() : memberRole != null? memberRole.getId() : "", m != null ? m.isActive() : true, false);
		} catch (IllegalStateException e) {
			log.error(".addUserToGroup: User with id {} cannot be inserted in group with id {} because the group is locked", id, g.getId());
		}
		
	}
	
	
	
	@Setter
	private UserDirectoryService userDirectoryService;
	
    
    /**
     * As we import groups we store the details here for use in further stages
     * of the import sequence.
     */
    @Getter
    private List<ImportedGroup> importedGroups = new ArrayList<>();
   
    public String processCreateJoinableSet() {
    	// reset the warning messages
    	resetTargettedMessageList();
    	
    	String returnVal = processCreateJoinableSetHelper(true);
    	if(returnVal == null){
    		return null;
    	}else{
    		resetParams();
    		return returnVal;
    	}
    }
    
    private String processCreateJoinableSetHelper(boolean newSet){
    	joinableSetName = StringUtils.trimToEmpty( joinableSetName );
    	if( StringUtils.isEmpty( joinableSetName ) ){
			messages.addMessage(new TargettedMessage("groupTitle.empty.alert","groupTitle-group"));
			return null;
		}
    	int joinableSetNumOfGroupsInt;
    	joinableSetNumOfGroups = StringUtils.trimToEmpty( joinableSetNumOfGroups );
    	if(joinableSetNumOfGroups == null || "".equals(joinableSetNumOfGroups)){
    		messages.addMessage(new TargettedMessage("numGroups.empty.alert","num-groups"));
			return null;
    	}else{
    		try{
    			joinableSetNumOfGroupsInt = Integer.parseInt(joinableSetNumOfGroups);
    			if(joinableSetNumOfGroupsInt > 1000){
    				messages.addMessage(new TargettedMessage("maxGroups.alert","num-groups"));
        			return null;
    			}
    			if(joinableSetNumOfGroupsInt <= 0){
    				messages.addMessage(new TargettedMessage("numGroups.zero.alert","num-groups"));
    			}
    		}catch (Exception e) {
    			messages.addMessage(new TargettedMessage("numGroups.empty.alert","num-groups"));
    			return null;
			}
    	}
    	int joinableSetNumOfMembersInt;
    	joinableSetNumOfMembers = StringUtils.trimToEmpty( joinableSetNumOfMembers );
    	if(joinableSetNumOfMembers == null || "".equals(joinableSetNumOfMembers)){
    		messages.addMessage(new TargettedMessage("maxMembers.empty.alert","num-groups"));
			return null;
    	}else{
    		try{
    			joinableSetNumOfMembersInt = Integer.parseInt(joinableSetNumOfMembers);
    			if(joinableSetNumOfMembersInt <= 0){
    				messages.addMessage(new TargettedMessage("maxMembers.zero.alert","num-max-members"));
        			return null;
    			}
    		}catch (Exception e) {
    			messages.addMessage(new TargettedMessage("maxMembers.empty.alert","num-max-members"));
    			return null;
			}
    	}

    	Collection siteGroups = site.getGroups();
    	Set<String> groupTitles = new HashSet<>();
		if (siteGroups != null && siteGroups.size() > 0)
		{
			for (Iterator iGroups = siteGroups.iterator(); iGroups.hasNext();) {
				Group iGroup = (Group) iGroups.next();
				groupTitles.add(iGroup.getTitle());
				if(newSet){
					//check that this joinable group name doesn't already exist, if so,
					//warn the user:
					String joinableSetProp = iGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
					if(joinableSetProp != null && !"".equals(joinableSetProp) && joinableSetProp.equalsIgnoreCase(joinableSetName)){
						//already have a joinable set named this:
						messages.addMessage(new TargettedMessage("joinableset.duplicate.alert","num-max-members"));
		    			return null;
					}
				}
			}
		}
    	for(int i = 1; groupsCreated < joinableSetNumOfGroupsInt && i < 1000; i++){
    		String groupTitle = joinableSetName + " " + i;
    		if(!groupTitles.contains(groupTitle)){
    			Group g = site.addGroup();
    			g.getProperties().addProperty(Group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
    			g.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET, joinableSetName);
    			g.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET_MAX, joinableSetNumOfMembers);
    			g.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET_PREVIEW,Boolean.toString(allowPreviewMembership));
    			g.getProperties().addProperty(Group.GROUP_PROP_VIEW_MEMBERS, Boolean.toString(allowViewMembership));
    			g.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE, Boolean.toString(unjoinable));
    			g.setTitle(groupTitle);
    			groupsCreated++;
    			pendingGroupTitles.add( groupTitle );
    		}
    	}

    	if( newSet )
    	{
    		saveSite();
     	}
    	else
    	{
    		resetJoinableSetGroupParams();
    	}
    	
    	return "success";
    }

    /**
     * Utility method to save the site
     */
    private void saveSite()
    {
        try
        {
            siteService.save( site );
        }
        catch( IdUnusedException | PermissionException ex )
        {
            log.error(ex.getMessage());
        }
    }

    public String processDeleteJoinableSet(){
    	// reset the warning messages
    	resetTargettedMessageList();
    	
    	boolean updated = false;
    	for(Group group : site.getGroups()){
    		String joinableSet = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
    		if(joinableSet != null && joinableSet.equals(joinableSetNameOrig)){
    			group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET);
    			group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET_MAX);
    			group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET_PREVIEW);
    			group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE);
    			updated = true;
    		}
    	}
    	if(updated){
    		saveSite();
    	}
    	
    	resetParams();
    	
    	return "success";
    }
   
    public String processUpdateJoinableSet(){
    	// reset the warning messages
    	resetTargettedMessageList();
    	
    	String returnVal = processChangeJoinableSetNameHelper( true );
    	if( returnVal != null && returnVal.equals( "success" ) )
    	{
    		resetParams();
    	}

    	return returnVal;
    }
    
    private String processChangeJoinableSetNameHelper( boolean save ){
    	joinableSetName = StringUtils.trimToEmpty( joinableSetName );
    	if( StringUtils.isEmpty( joinableSetName ) ){
			messages.addMessage(new TargettedMessage("groupTitle.empty.alert","groupTitle-group"));
			return null;
		}
    	if(!joinableSetName.equals(joinableSetNameOrig) || unjoinable != unjoinableOrig){
    		if(!joinableSetName.equals(joinableSetNameOrig)){
    			//check that the new joinable set name doesn't already exist
    			Collection siteGroups = site.getGroups();
    			if (siteGroups != null && siteGroups.size() > 0)
    			{
    				for (Iterator iGroups = siteGroups.iterator(); iGroups.hasNext();) {
    					Group iGroup = (Group) iGroups.next();
    					//check that this joinable group name doesn't already exist, if so,
    					//warn the user:
    					String joinableSetProp = iGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
    					if(joinableSetProp != null && !"".equals(joinableSetProp) && joinableSetProp.equals(joinableSetName)){
    						//already have a joinable set named this:
    						messages.addMessage(new TargettedMessage("joinableset.duplicate.alert","num-max-members"));
    						return null;
    					}
    				}
    			}
    		}

    		for(Group group : site.getGroups()){
        		String joinableSet = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
        		if(joinableSet != null && joinableSet.equals(joinableSetNameOrig)){
        			group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET, joinableSetName);
        			group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE, Boolean.toString(unjoinable));
        		}
        	}
    	}

    	if( save )
    	{
        	saveSite();
    	}

    	return "success";
    }
    
    public String processGenerateJoinableSet(){
    	// reset the warning messages
    	resetTargettedMessageList();

    	String returnVal = processChangeJoinableSetNameHelper(false);
    	if(returnVal != null && "success".equals(returnVal))
    	{
        	returnVal = processCreateJoinableSetHelper(false);
        	if(returnVal != null && "success".equals(returnVal))
        	{
        		return null;
        	}
        }

    	return returnVal;
    }
}
