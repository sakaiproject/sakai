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
package org.sakaiproject.site.tool.helper.managegroup.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
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
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;

/**
 * 
 * @author 
 *
 */
@Slf4j
public class SiteManageGroupHandler {

    private Collection<Member> groupMembers;
    private final GroupComparator groupComparator = new GroupComparator();
	
    public Site site = null;
    public SiteService siteService = null;
    public AuthzGroupService authzGroupService = null;
    public ToolManager toolManager = null;
    public SessionManager sessionManager = null;
    public ServerConfigurationService serverConfigurationService;
    private List<Group> groups = null;
    public String memberList = "";
    public boolean update = false;
    public boolean done = false;
    
    public String[] selectedGroupMembers = new String[]{};
    public String[] selectedSiteMembers = new String[]{};
    
    private final String HELPER_ID = "sakai.tool.helper.id";
    private final String GROUP_DELETE = "group.delete";
    private final String SITE_RESET = "group.reset";

    // Tool session attribute name used to schedule a whole page refresh.
    public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh"; 
	
	public TargettedMessageList messages;
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}
	
	/**
	 * reset the message list
	 */
    private void resetTargettedMessageList()
    {
    	this.messages = new TargettedMessageList();
    }
	
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
		// SAK-29645 - preserve user input
		if( messages.size() == 0 )
		{
			id = "";
			title = "";
			description ="";
			deleteGroupIds=new String[]{};
			selectedGroupMembers = new String[]{};
			selectedSiteMembers = new String[]{};
			memberList = new String();
		}
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
                  String gProp = gNext.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED);
                  if (gProp != null && gProp.equals(Boolean.TRUE.toString())) {
                     groups.add(gNext);
                  }
               }
            }
        }
        
        Collections.sort( groups, groupComparator );
        return groups;
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
    
    public Collection<Member> getGroupParticipant()
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

    	// reset the warning messages
    	resetTargettedMessageList();
    	
        Group group;
        
        id = StringUtils.trimToNull(id);
        
    	title = StringUtils.trimToNull(title);
    	if (title == null)
    	{
    		//we need something in the title field SAK-21517
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
			group.setTitle(title);
            group.setDescription(description);   
            
            boolean found;
            // remove those no longer included in the group
			Set members = group.getMembers();
			String[] membersSelected = memberList.split("##");
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
					} catch (IllegalStateException e) {
						log.error(".processAddGroup: User with id {} cannot be deleted from group with id {} because the group is locked", mId, group.getId());
						return null;
					}
				}
			}

            // add those seleted members
            for( String memberId : membersSelected )
            {
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
                    try {
                        group.insertMember(memberId, r != null ? r.getId()
                                              : memberRole != null? memberRole.getId() : "", m != null ? m.isActive() : true,
                                              false);
                    } catch (IllegalStateException e) {
                        log.error(".processAddGroup: User with id {} cannot be inserted in group with id {} because the group is locked", memberId, group.getId());
                        return null;
                    }
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
	        	log.warn(this + ".processAddGroup: cannot find site " + site.getId(), e);
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
                //
                Group g = site.getGroup(groupId);
                groups.add(g);
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
				log.warn(this + ".processDeleteGroups: Problem of saving site after group removal: site id =" + site.getId(), e);
			} catch (PermissionException e) {
				messages.addMessage(new TargettedMessage("editgroup.site.permission.alert","not allowed to find site"));
				log.warn(this + ".processDeleteGroups: Permission problem of saving site after group removal: site id=" + site.getId(), e);
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
                Group g = site.getGroup(groupId);
                rv.add(g);
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
    ** Comparator for sorting Group objects
    **/
   private class GroupComparator implements Comparator {
      public int compare(Object o1, Object o2) {
         return ((Group)o1).getTitle().compareToIgnoreCase( ((Group)o2).getTitle() );
      }
   }
}
