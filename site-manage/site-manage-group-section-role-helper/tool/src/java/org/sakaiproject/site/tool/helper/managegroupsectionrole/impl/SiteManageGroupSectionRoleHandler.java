package org.sakaiproject.site.tool.helper.managegroupsectionrole.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteConstants;
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

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIOutput;
import au.com.bytecode.opencsv.CSVReader;
/**
 * 
 * @author 
 *
 */
public class SiteManageGroupSectionRoleHandler {
	
	private static final String REQ_ATTR_GROUPFILE = "groupfile";

    /** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SiteManageGroupSectionRoleHandler.class);
	
	private List<Member> groupMembers;
    private GroupComparator groupComparator = new GroupComparator();
	
    public Site site = null;
    public SiteService siteService = null;
    public AuthzGroupService authzGroupService = null;
    public ToolManager toolManager = null;
    public SessionManager sessionManager = null;
    public ServerConfigurationService serverConfigurationService;
    private List<Group> groups = null;
    private Set unhideables = null;
    public String memberList = "";
    public boolean update = false;
    public boolean done = false;
    
    public String[] selectedGroupMembers = new String[]{};
    public String[] selectedSiteMembers = new String[]{};
    
    // selected rosters for autocreate groups
    public Map<String, Boolean> selectedRosters = new HashMap<String, Boolean>();
    
    // selected roles for autocreate groups
    public Map<String, Boolean> selectedRoles = new HashMap<String, Boolean>();
       
    private static final int OPTION_ASSIGN_BY_ROLES = 1;
    private static final int OPTION_ASSIGN_RANDOM = 2;
    public int optionAssign = OPTION_ASSIGN_BY_ROLES;
    
    public boolean groupSplit = true;
    public String numToSplitGroup = "";
    public String numToSplitUser = "";
    
    public String groupTitleGroup = "";
    public String groupTitleUser = "";
    
    private String NULL_STRING = "";
    
    private final String TOOL_CFG_FUNCTIONS = "functions.require";
    private final String TOOL_CFG_MULTI = "allowMultiple";
    private final String SITE_UPD = "site.upd";
    private final String HELPER_ID = "sakai.tool.helper.id";
    private final String GROUP_ADD = "group.add";
    private final String GROUP_DELETE = "group.delete";
    private final String GROUP_RENAME = "group.rename";
    private final String GROUP_SHOW = "group.show";
    private final String GROUP_HIDE = "group.hide";
    private final String SITE_REORDER = "group.reorder";
    private final String SITE_RESET = "group.reset";
    
    public String joinableSetName = "";
    public String joinableSetNameOrig = "";
    public String joinableSetNumOfGroups = "";
    public String joinableSetNumOfMembers = "";
    public boolean allowPreviewMembership = false;
    public boolean allowViewMembership = false;
    public boolean unjoinable = false;
    public boolean unjoinableOrig = false;

    // Tool session attribute name used to schedule a whole page refresh.
    public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh"; 

    // SAK-23016 - added CSV types from http://filext.com/file-extension/CSV
    private static final String CSV_FILE_EXTENSION="csv";
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
	
	private org.sakaiproject.authz.api.GroupProvider groupProvider = (org.sakaiproject.authz.api.GroupProvider) ComponentManager.get(org.sakaiproject.authz.api.GroupProvider.class);
	
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
	
	// for those to be deleted groups
	public String[] deleteGroupIds;
	
	/**
	 * reset the variables
	 */
	public void resetParams()
	{
		id = "";
		title = "";
		description ="";
		deleteGroupIds=new String[]{};
		selectedGroupMembers = new String[]{};
	    selectedSiteMembers = new String[]{};
	    selectedRosters = new HashMap<String, Boolean>();
	    selectedRoles = new HashMap<String, Boolean>();
	    
	    optionAssign=OPTION_ASSIGN_BY_ROLES;
	    groupSplit = true;
	    numToSplitUser = "";
	    numToSplitGroup = "";
	    groupTitleUser = "";
	    groupTitleGroup = "";
	    
	    importedGroups = null;
	    
	    joinableSetName = "";
	    joinableSetNameOrig = "";
	    joinableSetNumOfGroups = "";
	    joinableSetNumOfMembers = "";
	    allowPreviewMembership = false;
	    allowViewMembership = false;
	    unjoinable = false;
	    unjoinableOrig = false;
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
            groups = new Vector<Group>();
            if (site != null)
            {   
                // only show groups created by WSetup tool itself
    			Collection allGroups = (Collection) site.getGroups();
    			for (Iterator gIterator = allGroups.iterator(); gIterator.hasNext();) {
    				Group gNext = (Group) gIterator.next();
    				String gProp = gNext.getProperties().getProperty(
    						gNext.GROUP_PROP_WSETUP_CREATED);
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
     * @return List of roster ids
     */
    public List<String> getSiteRosters(Group group) {
        if (site == null) {
            init();
        }
        List<String> providerIds = null;
        
        if (update) {
            providerIds = new Vector<String>();
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
     * @return List of roster ids
     */
    public List<String> getGroupRosters(Group g) {
    	
        List<String> providerIds = null;
        
        if (update) {
            providerIds = new Vector<String>();
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
     * Gets the roles for the current site excluding the group
     * @return Map of groups (id, group)
     */
    public List<Role> getSiteRoles(Group group) {
        if (site == null) {
            init();
        }
        List<Role> roles = null;
        
        if (update) {
            roles = new Vector<Role>();
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
            		M_log.debug(this + ".getRoles: no authzgroup found for " + siteReference);
            	}
            	
            	if (group != null)
            	{
            		String roleProviderId = group.getProperties().getProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID);
            		if (roleProviderId != null)
            		{
            			if (groupProvider != null)
            			{
	            			String[] groupProvidedRoles = groupProvider.unpackId(roleProviderId);
	            			for(int i=0; i<groupProvidedRoles.length;i++)
	            			{
	            				roles.remove(group.getRole(groupProvidedRoles[i]));
	            			}
            			}
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
    	List<String> rv = new Vector<String>();
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
    
    public boolean isUserFromProvider(String userEId, String userId, Group g, List<String> rosterIds, List<String> roleIds)
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
    		for (int i = 0; !rv && i < roleIds.size(); i++)
	    	{
    			String roleId = roleIds.get(i);
		    	if (g.getUserRole(userId).getId().equals(roleId))
		    	{
		    		rv =  true;
		    	}
	    	}
    	}
    	
    	return rv;
    }
    
    /**
     * Gets the roles for the group
     * @return Map of groups (id, group)
     */
    public List<String> getGroupProviderRoles(Group g) {
        List<String> rv = null;
        
        if (update) {
            rv = new Vector<String>();
            if (g != null)
            {   
                // get the authz group
            	String roleProviderId = g.getProperties().getProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID);
            	if (roleProviderId != null)
            	{
            		if (groupProvider != null)
            		{
	            		String[] roleStrings = groupProvider.unpackId(roleProviderId);
	            		for(String roleString:roleStrings)
	            		{
	            			rv.add(roleString);
	            		}
            		}
            	}
            }
        }
        return rv;
    }
    
    /**
     * Initialization method, just gets the current site in preperation for other calls
     *
     */
    public void init() {
        if (site == null) {
            String siteId = null;
            try {
                siteId = sessionManager.getCurrentToolSession()
                        .getAttribute(HELPER_ID + ".siteId").toString();
            }
            catch (java.lang.NullPointerException npe) {
                // Site ID wasn't set in the helper call!!
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
                e.printStackTrace();
            }
        }
        title = "";

        if (groupMembers == null)
        {
        	groupMembers = new Vector<Member>();
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
    	List<Participant> rv = new Vector<Participant>();
    	if (site != null)
    	{
    		String siteId = site.getId();
    		String realmId = siteService.siteReference(siteId);

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
        catch (IdUnusedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    	
        Group group = null;
        
        // those added user into group
        List<String> addedGroupMember = new Vector<String>();
        
        // those deleted user from group
        List<String> removedGroupMember = new Vector<String>();
        
        id = StringUtils.trimToNull(id);
        
    	String siteReference = siteService.siteReference(site.getId());
    	
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
    	
    	int joinableSetNumOfMembersInt = -1;
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
	        group.getProperties().addProperty(group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
		}
		
		if (group != null)
		{
			group.setTitle(title);
            group.setDescription(description);
            group.getProperties().addProperty(group.GROUP_PROP_VIEW_MEMBERS, Boolean.toString(allowViewMembership));
            if(joinableSetName != null && !"".equals(joinableSetName.trim())){
            	group.getProperties().addProperty(group.GROUP_PROP_JOINABLE_SET, joinableSetName);
            	group.getProperties().addProperty(group.GROUP_PROP_JOINABLE_SET_MAX, joinableSetNumOfMembers);
            	group.getProperties().addProperty(group.GROUP_PROP_JOINABLE_SET_PREVIEW,Boolean.toString(allowPreviewMembership));
            	group.getProperties().addProperty(group.GROUP_PROP_JOINABLE_UNJOINABLE, Boolean.toString(unjoinable));
            }else{
            	group.getProperties().removeProperty(group.GROUP_PROP_JOINABLE_SET);
            	group.getProperties().removeProperty(group.GROUP_PROP_JOINABLE_SET_MAX);
            	group.getProperties().removeProperty(group.GROUP_PROP_JOINABLE_SET_PREVIEW);
            	group.getProperties().removeProperty(group.GROUP_PROP_JOINABLE_UNJOINABLE);
            }
            
            boolean found = false;
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
					group.removeMember(mId);
					removedGroupMember.add("uid=" + mId + ";groupId=" + group.getId());
				}
			}

			// add those selected members
			List<String> siteRosters = getSiteRosters(null);
			List<String> siteRoles = getSiteRoleIds();
			List<String> selectedRosters = new Vector<String>();
			List<String> selectedRoles = new Vector<String>();
			for (int i = 0; i < membersSelected.length; i++) {
				String memberId = membersSelected[i];
				
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
    					group.addMember(roleUserId, memberId, member.isActive(), false);
    					addedGroupMember.add("uid=" + roleUserId + ";role=" + member.getRole().getId() + ";active=" + member.isActive() + ";provided=false;groupId=" + group.getId());
    				}
    				selectedRoles.add(memberId);
				}
				else
				{
					// normal user id
					memberId = StringUtils.trimToNull(memberId);
					if (memberId != null && group.getUserRole(memberId) == null) {
						Role r = site.getUserRole(memberId);
						Member m = site.getMember(memberId);
						Role memberRole = m != null ? m.getRole() : null;
						// for every member added through the "Manage
						// Groups" interface, he should be defined as
						// non-provided
						// get role first from site definition. 
						// However, if the user is inactive, getUserRole would return null; then use member role instead
						String roleString = r != null ? r.getId(): memberRole != null? memberRole.getId() : "";
						boolean active = m != null ? m.isActive() : true;
						group.addMember(memberId, roleString, active,false);
						addedGroupMember.add("uid=" + memberId + ";role=" + roleString + ";active=" + active + ";provided=false;groupId=" + group.getId());
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
				group.setProviderGroupId(getProviderString(selectedRosters));
			}
			else
			{
				// clear the provider id
				group.setProviderGroupId(null);
			}
			if (!selectedRoles.isEmpty())
			{
				// pack the role provider id and add to property
    			group.getProperties().addProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID, getProviderString(selectedRoles));
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
	        catch (IdUnusedException e) {
	        	M_log.warn(this + ".processAddGroup: cannot find site " + site.getId(), e);
	            return null;
	        } 
	        catch (PermissionException e) {
	        	M_log.warn(this + ".processAddGroup: cannot find site " + site.getId(), e);
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
    		M_log.debug(this + ".processConfirmGroupDelete: no group chosen to be deleted.");
    		messages.addMessage(new TargettedMessage("delete_group_nogroup","no group chosen"));
    		return null;
    	}
    	else
    	{
    		List<Group> groups = new Vector<Group>();
    		
	    	for (int i = 0; i < deleteGroupIds.length; i ++) {
	    		String groupId = deleteGroupIds[i];
	    		//
	    		try
	    		{
	    			Group g = site.getGroup(groupId);
	    			groups.add(g);
	    		}
	    		catch (Exception e)
	    		{
	    			
	    		}
	    	}
	    	return "confirm";
    	}
    }
    
    public String processDeleteGroups()
    {
    	// reset the warning messages
    	resetTargettedMessageList();
    	
    	if (site != null)
    	{
	    	for (int i = 0; i < deleteGroupIds.length; i ++) {
		    	String groupId = deleteGroupIds[i];
		    	Group g = site.getGroup(groupId);
				if (g != null) {
					site.removeGroup(g);
				}
			}
			try {
				siteService.save(site);
			} catch (IdUnusedException e) {
				messages.addMessage(new TargettedMessage("editgroup.site.notfound.alert","cannot find site"));
				M_log.warn(this + ".processDeleteGroups: Problem of saving site after group removal: site id =" + site.getId(), e);
			} catch (PermissionException e) {
				messages.addMessage(new TargettedMessage("editgroup.site.permission.alert","not allowed to find site"));
				M_log.warn(this + ".processDeleteGroups: Permission problem of saving site after group removal: site id=" + site.getId(), e);
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
    
    /**
     * atuo create group(s) based on selected roster(s) or role(s)
     *
     */
    public String processAutoCreateGroup() {
    	// reset the warning messages
    	resetTargettedMessageList();
    			
    	//check if fields are correct:
    	int intToSplit=-1;
    	
    	if(OPTION_ASSIGN_RANDOM == optionAssign){
    		String numToSplit = groupSplit ? numToSplitGroup : numToSplitUser;
        	String groupTitle = groupSplit ? groupTitleGroup : groupTitleUser;
    		if(numToSplit == null){
    			if(groupSplit){
    				messages.addMessage(new TargettedMessage("numToSplit.group.empty.alert","numToSplit"));	
    			}else{
    				messages.addMessage(new TargettedMessage("numToSplit.user.empty.alert","numToSplit"));
    			}    			
    			return null;
    		}else{
    			try {
    				intToSplit = Integer.parseInt(numToSplit);
    				if(intToSplit <= 0){
    					if(groupSplit){
    	    				messages.addMessage(new TargettedMessage("numToSplit.group.notanumber.alert","numToSplit"));	
    	    			}else{
    	    				messages.addMessage(new TargettedMessage("numToSplit.user.notanumber.alert","numToSplit"));
    	    			}
    					return null;
    				}
				} catch (Exception e) {
					if(groupSplit){
	    				messages.addMessage(new TargettedMessage("numToSplit.group.notanumber.alert","numToSplit"));	
	    			}else{
	    				messages.addMessage(new TargettedMessage("numToSplit.user.notanumber.alert","numToSplit"));
	    			}
					return null;
				}
    		}
    		
    		if(groupTitle == null || "".equals(groupTitle)){
    			messages.addMessage(new TargettedMessage("groupTitle.empty.alert","groupTitle"));
    			return null;
    		}
    	}
    	    	    	
    	List<String> rosterList = new Vector<String>();
    	if (!selectedRosters.isEmpty())
    	{
    		for (Iterator<String> iterRosters= selectedRosters.keySet().iterator(); iterRosters.hasNext(); ) {
    			String roster = iterRosters.next();
    			if (Boolean.TRUE.equals(selectedRosters.get(roster)))
    			{
    				// selected roster
    				rosterList.add(roster);
    			}
    		}
    	}
    	
    	List<String> roleList = new Vector<String>();
    	if (!selectedRoles.isEmpty())
    	{
    		for (Iterator<String> iterRoles = selectedRoles.keySet().iterator(); iterRoles.hasNext(); ) {
    			String role = iterRoles.next();
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
    		// go and create the new group
    		if (!rosterList.isEmpty())
    		{
	    		for (String roster:rosterList)
	    		{
	    			Group group = site.addGroup();
        			group.getProperties().addProperty(group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
		        		
		        	// roster provider string
        			//rsf doesn't like "."'s, so these have been escaped.  Now unescape them
        			roster = roster.replaceAll("-_p_-", ".");
		        	group.setProviderGroupId(roster);
		        		
		        	String title = truncateGroupTitle(roster);
		        	group.setTitle(title);
	    		}
    		}
	        	
        	// role based
        	if (!roleList.isEmpty())
        	{
        		if(OPTION_ASSIGN_BY_ROLES == optionAssign){
	        		for(String role:roleList)
	        		{
	        			Group group = site.addGroup();
	        			// make the provider id as of SITEID_ROLEID
	        			//group.setProviderGroupId(site.getId() + "_" + role);
	        			group.getProperties().addProperty(group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
	        			group.getProperties().addProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID, role);
	
			        	String title = truncateGroupTitle(role);
			        	group.setTitle(title);
	        			
	        			// get the authz group
	                	String siteReference = siteService.siteReference(site.getId());
	                	try
	                	{
	                		AuthzGroup siteGroup = authzGroupService.getAuthzGroup(siteReference);
	                		Set<String> usersHasRole = siteGroup.getUsersHasRole(role);
	                		if (usersHasRole != null)
	                		{
	                			for (Iterator<String> uIterator = usersHasRole.iterator(); uIterator.hasNext();)
	                			{
	                				String userId = uIterator.next();
	                				Member member = site.getMember(userId);
	            					group.addMember(userId, role, member.isActive(), false);
	                			}
	                		}
	                	}
	                	catch (GroupNotDefinedException e)
	                	{
	                		M_log.debug(this + ".processAutoCreateGroup: no authzgroup found for " + siteReference);
	                	}
	        		}
        		}else{
        			createRandomGroups(roleList, intToSplit);
        		}
        	}
        		
    		// save the changes
    		try
    		{
    			siteService.save(site);
    			// reset the form params
    			resetParams();
	        } 
	        catch (IdUnusedException e) {
	        	M_log.warn(this + ".processAutoCreateGroup: cannot find site " + site.getId(), e);
	            return null;
	        } 
	        catch (PermissionException e) {
	        	M_log.warn(this + ".processAutoCreateGroup: cannot find site " + site.getId(), e);
	            return null;
	        }
        	
    	}

        return "done";
    }
    
    private void createRandomGroups(List<String> roleList, int unit){
    	String groupTitle = groupSplit ? groupTitleGroup : groupTitleUser;
    	
    	if(groupTitle != null && !"".equals(groupTitle)){
    		//get list of all users:

    		List<String> usersList = new ArrayList<String>();

    		for(String role:roleList)
    		{
    			// get the authz group
    			String siteReference = siteService.siteReference(site.getId());

    			try
    			{
    				AuthzGroup siteGroup = authzGroupService.getAuthzGroup(siteReference);
    				Set<String> usersHasRole = siteGroup.getUsersHasRole(role);
    				if (usersHasRole != null)
    				{
    					for (Iterator<String> uIterator = usersHasRole.iterator(); uIterator.hasNext();)
    					{
    						String userId = uIterator.next();
    						if(!usersList.contains(userId)){
    							usersList.add(userId);
    						}
    					}
    				}
    			}
    			catch (GroupNotDefinedException e)
    			{
    				M_log.debug(this + ".processAutoCreateGroup: no authzgroup found for " + siteReference);
    			}
    		}

    		//split users into random groups:

    		int numOfGroups=-1;
    		int numOfUsersPerGroup=-1;
    		if(groupSplit){
    			numOfGroups = (unit > usersList.size()) ? usersList.size() : unit;
				if(numOfGroups > 0){
	    			numOfUsersPerGroup = usersList.size()/numOfGroups;
				}
    		}else{
    			numOfUsersPerGroup = (unit > usersList.size()) ? usersList.size() : unit;
				if(numOfUsersPerGroup > 0){
	    			numOfGroups = usersList.size()/numOfUsersPerGroup;
				}
    		}

    		int groupCount = 0;
    		List<Group> gList = new ArrayList<Group>();
    		while(groupCount < numOfGroups){
    			Group group = site.addGroup();
    			group.getProperties().addProperty(group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());

    			//Title
    			StringBuffer title = new StringBuffer();

    			title.append(groupTitle);

    			title.append("-");
    			title.append(groupCount+1);
    			group.setTitle(title.toString());

    			int userCount = 0;

    			while(userCount < numOfUsersPerGroup && usersList.size() > 0){
    				int index = (int)(Math.random() * (usersList.size() - 1));
    				String userId = usersList.get(index);
    				Member member = site.getMember(userId);
    				group.addMember(userId, member.getRole().getId(), member.isActive(), false);

    				//remove this user now:
    				usersList.remove(index);

    				userCount++;
    			}       		

    			groupCount++;
    			
    			// add the group object to list
    			gList.add(group);
    		}
    		
			// all the groups has been created, but there are some users still left to be assigned
			while (usersList.size() > 0)
			{
				// pick a random user
				int index = (int)(Math.random() * (usersList.size() - 1));
				String userId = usersList.get(index);
				// pick a random group
				int gIndex = (int)(Math.random() * (gList.size() - 1));
				Group group = gList.get(gIndex);
				// add user to group
				Member member = site.getMember(userId);
				group.addMember(userId, member.getRole().getId(), member.isActive(), false);
				//remove this user now:
				usersList.remove(index);
    		}
    	}

    }

    /**
     * check whether the title string is within length limit, truncate it if necessary
     * @param oTitle
     * @return
     */
	private String truncateGroupTitle(String oTitle) {
		String title = oTitle;
		if (title.length() > SiteConstants.SITE_GROUP_TITLE_LIMIT)
		{
			title = title.substring(0, SiteConstants.SITE_GROUP_TITLE_LIMIT);
		}
		return title.trim();
	}
    
    /**
     * Return a single string representing the provider id list
     * @param idsList
     */
    private String getProviderString(List<String> idsList)
    {
    	String[] sArray = new String[idsList.size()];
		sArray = (String[]) idsList.toArray(sArray);
		if (groupProvider != null)
		{
			return groupProvider.packId(sArray);
		}
		else
		{
			// simply concat strings
			StringBuffer rv = new StringBuffer();
			for(String sArrayString:sArray)
			{
				rv.append(" ").append(sArrayString);
			}
			return rv.toString();
		}
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
		List<Group> rv = new Vector<Group>();
		if (deleteGroupIds != null && deleteGroupIds.length > 0 && site != null)
		{
			for (int i = 0; i<deleteGroupIds.length; i++)
			{
				String groupId = deleteGroupIds[i];
				try
				{
					Group g = site.getGroup(groupId);
					rv.add(g);
				}
				catch (Exception e)
				{
					M_log.debug(this + ":getSelectedGroups: cannot get group with id " + groupId);
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
    		String groupWSetupCreated = group.getProperties().getProperty(group.GROUP_PROP_WSETUP_CREATED);
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
     * check whether there is already a group within the site containing the role id
     * @param roleId
     * @return
     */
    public boolean existRoleGroup(String roleId)
    {
    	boolean rv = false;
    	
    	Collection<Group> groups = site.getGroups();
    	
    	for(Group group:groups)
    	{
    		// check if there is one group with this roster id already
    		String groupWSetupCreated = group.getProperties().getProperty(group.GROUP_PROP_WSETUP_CREATED);
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
     ** Comparator for sorting Group objects
     **/
    private class GroupComparator implements Comparator {
       public int compare(Object o1, Object o2) {
          return ((Group)o1).getTitle().compareToIgnoreCase( ((Group)o2).getTitle() );
       }
    }
    
    /**
     * Grabs the uploaded file from the groupfile request attribute and extracts the group details
     * from it, adding them to the importedGroups list as it goes. Expects at least three columns,
     * the first three being first name, last name and email respectively.
     */
    public String processUploadAndCheck() {
        String uploadsDone = (String) httpServletRequest.getAttribute(RequestFilter.ATTR_UPLOADS_DONE);

        FileItem usersFileItem;

        if (uploadsDone != null && uploadsDone.equals(RequestFilter.ATTR_UPLOADS_DONE)) {

            try {
                usersFileItem = (FileItem) httpServletRequest.getAttribute(REQ_ATTR_GROUPFILE);

                if(usersFileItem != null && usersFileItem.getSize() > 0) {

                    String mimetype = usersFileItem.getContentType();
                    String filename = usersFileItem.getName();

                    if (ArrayUtils.contains(CSV_MIME_TYPES, mimetype) 
                            || StringUtils.endsWith(filename, "csv")) {
                        if (processCsvFile(usersFileItem)) {
                            return "success"; // SHORT CIRCUIT
                        }
                    } else {
                        M_log.error("Invalid file type: " + mimetype);
                        return "error"; // SHORT CIRCUIT
                    }
                }
            }
            catch (Exception e){
                M_log.error(e.getClass() + " : " + e.getMessage());
                return "error"; // SHORT CIRCUIT
            } finally {
                // clear the groupfile attribute so the tool does not have to be reset
                httpServletRequest.removeAttribute(REQ_ATTR_GROUPFILE);
            }
        }

        return "error";
    }

	/**
	 * Helper to process the uploaded CSV file
	 * 
	 * @param fileItem
	 * @return
	 */
	private boolean processCsvFile(FileItem fileItem){
		
		M_log.debug("CSV file uploaded");
		
		importedGroups = new ArrayList<ImportedGroup>();
		
		CSVReader reader;
		try {
			reader = new CSVReader(new InputStreamReader(fileItem.getInputStream()));
			List<String[]> lines = reader.readAll();
			
			//maintain a map of the groups and their titles in case the CSV file is unordered
			//this way we can still lookup the group and add members to it
			Map<String, ImportedGroup> groupMap = new HashMap<String, ImportedGroup>();
			
			for(String[] line: lines){
								
	            String groupTitle = StringUtils.trim(line[0]);
	            String userId = StringUtils.trim(line[1]);
	            
	            //if we already have an occurrence of this group, get the group and update the user list within it
	            if(groupMap.containsKey(groupTitle)){
	            	ImportedGroup group = groupMap.get(groupTitle);
	            	group.addUser(userId);
	            } else {
	            	ImportedGroup group = new ImportedGroup(groupTitle, userId);
	            	groupMap.put(groupTitle, group);
	            }
			}
			
			 //extract all of the imported groups from the map
            importedGroups.addAll(groupMap.values());
			
		} catch (IOException ioe) {
			M_log.error(ioe.getClass() + " : " + ioe.getMessage());
			return false;
		} catch (ArrayIndexOutOfBoundsException ae){
			M_log.error(ae.getClass() + " : " + ae.getMessage());
			return false;
		}
	    
		return true;
	}
	
	/**
	 * Helper to check for a valid user in a site, given an eid
	 * @param eid	eid of user,v eg jsmith26
	 * @return
	 */
	public boolean isValidSiteUser(String eid) {
		try {
			User u = userDirectoryService.getUserByEid(eid);
			if(u != null){
				
				Member m = site.getMember(u.getId());
				if(m != null) {
					return true;
				}
			}
		} catch (UserNotDefinedException e) {
			//not a valid user
			return false;
		}
		return false;
	}
	
	/**
	 * Helper to get a userId given an eid
	 * @param eid	eid of user,v eg jsmith26
	 * @return
	 */
	public String getUserId(String eid) {
		try {
			return userDirectoryService.getUserId(eid);
		} catch (UserNotDefinedException e) {
			M_log.error("The eid: " + eid + "is invalid.");
			return null;
		}
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
    	        group.getProperties().addProperty(group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
    	        group.setTitle(importedGroup.getGroupTitle());
			}
			
			//add all of the imported members to the group
    		for(String eid: importedGroup.getUserIds()){
    			this.addUserToGroup(eid, group);
    		}
    		
    		try {
    			siteService.save(site);
    			// post event about the participant update
    			EventTrackingService.post(EventTrackingService.newEvent(SiteService.SECURE_UPDATE_GROUP_MEMBERSHIP, group.getId(),true));
    		
    		} catch (Exception e) {
            	M_log.error("processImportedGroups failed for site: " + site.getId(), e);
            	return "error";
    		}
		}
		
		return "success";
	}
	
	/**
	 * Helper to get a list of user eids in a group
	 * @param g	the group
	 * @return
	 */
	public List<String> getGroupUserIds(Group g) {
		
		List<String> userIds = new ArrayList<String>();

		if(g == null) {
			return userIds;
		}
		
		Set<Member> members= g.getMembers();
		for(Member m: members) {
			userIds.add(m.getUserEid());
		}
		return userIds;
	}
	
	
	/**
	 * Helper to add a user to a group. Takes care of the role selection.
	 * @param eid	eid of the user eg jsmith26
	 * @param g		the group
	 */
	private void addUserToGroup(String eid, Group g) {
		
		//is this a valid site user?
		if(!isValidSiteUser(eid)){
			return;
		}
		
		//get userId
		String userId = getUserId(eid);
		if(StringUtils.isBlank(userId)) {
			return;
		}
		
		//is user already in the group?
		if(g.getUserRole(userId) != null) {
			return;
		}
		
		//add user to group with correct role. This is the same logic as above
		Role r = site.getUserRole(userId);
		Member m = site.getMember(userId);
		Role memberRole = m != null ? m.getRole() : null;
		g.addMember(userId, r != null ? r.getId() : memberRole != null? memberRole.getId() : "", m != null ? m.isActive() : true, false);
		
	}
	
	
	
	@Setter
	private UserDirectoryService userDirectoryService;
	
	 /**
     * We need this to get the uploaded file as snaffled by the request filter.
     */
	@Setter
    private HttpServletRequest httpServletRequest;
   
    
    /**
     * As we import groups we store the details here for use in further stages
     * of the import sequence.
     */
    @Getter
    private List<ImportedGroup> importedGroups;
   
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
    	if(joinableSetName == null || "".equals(joinableSetName.trim())){
			messages.addMessage(new TargettedMessage("groupTitle.empty.alert","groupTitle-group"));
			return null;
		}
    	int joinableSetNumOfGroupsInt = -1;
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
    	int joinableSetNumOfMembersInt = -1;
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
    	int groupsCreated = 0;
    	Collection siteGroups = site.getGroups();
    	Set<String> groupTitles = new HashSet<String>();
		if (siteGroups != null && siteGroups.size() > 0)
		{
			for (Iterator iGroups = siteGroups.iterator(); iGroups.hasNext();) {
				Group iGroup = (Group) iGroups.next();
				groupTitles.add(iGroup.getTitle());
				if(newSet){
					//check that this joinable group name doesn't already exist, if so,
					//warn the user:
					String joinableSetProp = iGroup.getProperties().getProperty(iGroup.GROUP_PROP_JOINABLE_SET);
					if(joinableSetProp != null && !"".equals(joinableSetProp) && joinableSetProp.equalsIgnoreCase(joinableSetName)){
						//already have a joinable set named this:
						messages.addMessage(new TargettedMessage("joinableset.duplicate.alert","num-max-members"));
		    			return null;
					}
				}
			}
		}
    	for(int i = 1; groupsCreated < joinableSetNumOfGroupsInt && i < 1000; i++){
    		String groupTitle = joinableSetName + "-" + i;
    		if(!groupTitles.contains(groupTitle)){
    			Group g = site.addGroup();
    			g.getProperties().addProperty(g.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
    			g.getProperties().addProperty(g.GROUP_PROP_JOINABLE_SET, joinableSetName);
    			g.getProperties().addProperty(g.GROUP_PROP_JOINABLE_SET_MAX, joinableSetNumOfMembers);
    			g.getProperties().addProperty(g.GROUP_PROP_JOINABLE_SET_PREVIEW,Boolean.toString(allowPreviewMembership));
    			g.getProperties().addProperty(g.GROUP_PROP_VIEW_MEMBERS, Boolean.toString(allowViewMembership));
    			g.getProperties().addProperty(g.GROUP_PROP_JOINABLE_UNJOINABLE, Boolean.toString(unjoinable));
    			g.setTitle(joinableSetName + "-" + i);
    			try{
    				siteService.save(site);
    				groupsCreated++;
    			}catch (Exception e) {
    			}
    		}
    	}
    	
    	return "success";
    }
    
    public String processDeleteJoinableSet(){
    	// reset the warning messages
    	resetTargettedMessageList();
    	
    	boolean updated = false;
    	for(Group group : site.getGroups()){
    		String joinableSet = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);
    		if(joinableSet != null && joinableSet.equals(joinableSetNameOrig)){
    			group.getProperties().removeProperty(group.GROUP_PROP_JOINABLE_SET);
    			group.getProperties().removeProperty(group.GROUP_PROP_JOINABLE_SET_MAX);
    			group.getProperties().removeProperty(group.GROUP_PROP_JOINABLE_SET_PREVIEW);
    			group.getProperties().removeProperty(group.GROUP_PROP_JOINABLE_UNJOINABLE);
    			updated = true;
    		}
    	}
    	if(updated){
    		try{
    			siteService.save(site);
    		}catch (Exception e) {
    		}
    	}
    	
    	resetParams();
    	
    	return "success";
    }
   
    public String processChangeJoinableSetName(){
    	// reset the warning messages
    	resetTargettedMessageList();
    	
    	String returnVal = processChangeJoinableSetNameHelper();
    	
    	if(returnVal == null){
    		return null;
    	}else{
    		resetParams();
    		return returnVal;
    	}
    }
    
    private String processChangeJoinableSetNameHelper(){
    	if(joinableSetName == null || "".equals(joinableSetName.trim())){
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
    					String joinableSetProp = iGroup.getProperties().getProperty(iGroup.GROUP_PROP_JOINABLE_SET);
    					if(joinableSetProp != null && !"".equals(joinableSetProp) && joinableSetProp.equals(joinableSetName)){
    						//already have a joinable set named this:
    						messages.addMessage(new TargettedMessage("joinableset.duplicate.alert","num-max-members"));
    						return null;
    					}
    				}
    			}
    		}
    		boolean updated = false;
    		for(Group group : site.getGroups()){
        		String joinableSet = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);
        		if(joinableSet != null && joinableSet.equals(joinableSetNameOrig)){
        			group.getProperties().addProperty(group.GROUP_PROP_JOINABLE_SET, joinableSetName);
        			group.getProperties().addProperty(group.GROUP_PROP_JOINABLE_UNJOINABLE, Boolean.toString(unjoinable));
        			updated = true;
        		}
        	}
    		if(updated){
    			try{
    				siteService.save(site);
    			}catch (Exception e) {
    			}
    		}
    	}
    	return "success";
    }
    
    public String processGenerateJoinableSet(){
    	// reset the warning messages
    	resetTargettedMessageList();
    	//since the user could have changed the values and this action doesn't save the changes, reset to the orig values:
    	joinableSetName = joinableSetNameOrig;
    	unjoinable = unjoinableOrig;
    	//generate the new groups since it will check all the required fields
    	String returnVal = processCreateJoinableSetHelper(false);
    	if(returnVal != null && "success".equals(returnVal)){
    		resetParams();
    	}
    	return returnVal;
    }
}

