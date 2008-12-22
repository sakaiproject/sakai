package org.sakaiproject.site.tool.helper.managegroupsectionrole.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
/**
 * 
 * @author 
 *
 */
public class SiteManageGroupSectionRoleHandler {
	
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SiteManageGroupSectionRoleHandler.class);
	
	private List<Member> groupMembers;
	
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
    
    private String NULL_STRING = "";
    
    private final String TOOL_CFG_FUNCTIONS = "functions.require";
    private final String TOOL_CFG_MULTI = "allowMultiple";
    private final String SITE_UPD = "site.upd";
    private final String HELPER_ID = "sakai.tool.helper.id";
    private final String UNHIDEABLES_CFG = "poh.unhideables";
    private final String GROUP_ADD = "group.add";
    private final String GROUP_DELETE = "group.delete";
    private final String GROUP_RENAME = "group.rename";
    private final String GROUP_SHOW = "group.show";
    private final String GROUP_HIDE = "group.hide";
    private final String SITE_REORDER = "group.reorder";
    private final String SITE_RESET = "group.reset";

    // Tool session attribute name used to schedule a whole page refresh.
    public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh"; 
	
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
    						SiteConstants.GROUP_PROP_WSETUP_CREATED);
    				if (gProp != null && gProp.equals(Boolean.TRUE.toString())) {
    					groups.add(gNext);
    				}
    			}
            }
        }
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
            
            } catch (IdUnusedException e) {
                // The siteId we were given was bogus
                e.printStackTrace();
            }
        }
        update = siteService.allowUpdateSite(site.getId());
        title = "";
        
        String conf = serverConfigurationService.getString(UNHIDEABLES_CFG);
        if (conf != null) {
            unhideables = new HashSet();
            String[] toolIds = conf.split(",");
            for (int i = 0; i < toolIds.length; i++) {
                unhideables.add(toolIds[i].trim());
            }
        }
        
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
    		rv.addAll(rvCopy);
    		
    		// check with group attendents
    		if (group != null)
    		{
    			// need to remove those inside group already
	    		for(Participant p:rvCopy)
	    		{
	    			if (group.getUserRole(p.getUniqname()) != null)
	    			{
	    				rv.remove(p);
	    			}
	    		}
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
     * Adds a new group to the current site
     * @param toolId
     * @param title
     * @return the newly added Group
     */
    public String processAddGroup () {

    	// reset the warning message
    	resetTargettedMessageList();
    	
        Group group = null;
        
        id = StringUtil.trimToNull(id);
        
    	String siteReference = siteService.siteReference(site.getId());
    	
    	if (title == null || title.length() == 0)
    	{
    		M_log.debug(this + ".processAddGroup: no title specified");
    		messages.addMessage(new TargettedMessage("editgroup.titlemissing", null, TargettedMessage.SEVERITY_ERROR));
    		return null;
    	}
    	else if (title.length() > SiteConstants.SITE_GROUP_TITLE_LIMIT)
    	{
    		messages.addMessage(new TargettedMessage("site_group_title_length_limit",new Object[] { String.valueOf(SiteConstants.SITE_GROUP_TITLE_LIMIT) }, TargettedMessage.SEVERITY_ERROR));
    		return null;
    	}
    	else if (id == null)
    	{
    		Collection siteGroups = site.getGroups();
    		if (siteGroups != null && siteGroups.size() > 0)
    		{
	    		// when adding a group, check whether the group title has
				// been used already
				boolean titleExist = false;
				for (Iterator iGroups = siteGroups.iterator(); !titleExist
						&& iGroups.hasNext();) {
					Group iGroup = (Group) iGroups.next();
					if (title.equals(iGroup.getTitle())) {
						// found same title
						titleExist = true;
					}
				}
				if (titleExist) {
					messages.addMessage(new TargettedMessage("group.title.same", null, TargettedMessage.SEVERITY_ERROR));
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
	        group.getProperties().addProperty(SiteConstants.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
		}
		
		if (group != null)
		{
			group.setTitle(title);
            group.setDescription(description);   
            
            boolean found = false;
            // remove those no longer included in the group
			Set members = group.getMembers();
			String[] membersSelected = memberList.split(",");
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
				}
			}

			// add those seleted members
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
    				}
    				selectedRoles.add(memberId);
				}
				else
				{
					// normal user id
					if (group.getUserRole(memberId) == null) {
						Role r = site.getUserRole(memberId);
						Member m = site.getMember(memberId);
						// for every member added through the "Manage
						// Groups" interface, he should be defined as
						// non-provided
						group.addMember(memberId, r != null ? r.getId()
								: "", m != null ? m.isActive() : true,
								false);
					}
				}
			}
			if (!selectedRosters.isEmpty())
			{
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
    	
    	List<String> rosterList = new Vector<String>();
    	if (!selectedRosters.isEmpty())
    	{
    		for (Iterator<String> iterRosters= selectedRosters.keySet().iterator(); iterRosters.hasNext(); ) {
    			String roster = iterRosters.next();
    			if (selectedRosters.get(roster) == Boolean.TRUE)
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
    			if (selectedRoles.get(role) == Boolean.TRUE)
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
        			group.getProperties().addProperty(SiteConstants.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
		        		
		        	// roster provider string
		        	group.setProviderGroupId(roster);
		        		
		        	// set title
		        	group.setTitle(roster);
	    		}
    		}
	        	
        	// role based
        	if (!roleList.isEmpty())
        	{
        		for(String role:roleList)
        		{
        			Group group = site.addGroup();
        			group.getProperties().addProperty(SiteConstants.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
        			group.getProperties().addProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID, role);
        			
        			// add users with role selected into group
        			Set roleUsers = site.getUsersHasRole(role);
    				for (Iterator iRoleUsers = roleUsers.iterator(); iRoleUsers.hasNext();)
    				{
    					String roleUserId = (String) iRoleUsers.next();
        				Member member = site.getMember(roleUserId);
    					group.addMember(roleUserId, role, member.isActive(), false);
    				}
        			
        			group.setTitle(role);
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
			String rv = "";
			for(String sArrayString:sArray)
			{
				rv = rv + " " + sArrayString;
			}
			return rv;
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
    		String groupWSetupCreated = group.getProperties().getProperty(SiteConstants.GROUP_PROP_WSETUP_CREATED);
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
    		String groupWSetupCreated = group.getProperties().getProperty(SiteConstants.GROUP_PROP_WSETUP_CREATED);
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
   
}

