/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.authz.impl;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupAlreadyDefinedException;
import org.sakaiproject.authz.api.GroupFullException;
import org.sakaiproject.authz.api.GroupIdInvalidException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.StorageUser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * BaseAuthzGroupService is a Sakai azGroup service implementation.
 * </p>
 * <p>
 * To support the public view feature, an AuthzGroup named TEMPLATE_PUBVIEW must exist, with a role named ROLE_PUBVIEW - all the abilities in this role become the public view abilities for any resource.
 * </p>
 */
public abstract class BaseAuthzGroupService implements AuthzGroupService, StorageUser
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseAuthzGroupService.class);

	/** Storage manager for this service. */
	protected Storage m_storage = null;

	/** The initial portion of a relative access point URL. */
	protected String m_relativeAccessPoint = null;

	/** A provider of additional Abilities for a userId. */
	protected GroupProvider m_provider = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct storage for this service.
	 */
	protected abstract Storage newStorage();

	/**
	 * Access the partial URL that forms the root of resource URLs.
	 * 
	 * @param relative
	 *        if true, form within the access path only (i.e. starting with /content)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : serverConfigurationService().getAccessUrl()) + m_relativeAccessPoint;
	}

	/**
	 * Access the azGroup id extracted from an AuthzGroup reference.
	 * 
	 * @param ref
	 *        The azGroup reference string.
	 * @return The the azGroup id extracted from an AuthzGroup reference.
	 */
	protected String authzGroupId(String ref)
	{
		String start = getAccessPoint(true) + Entity.SEPARATOR;
		int i = ref.indexOf(start);
		if (i == -1) return ref;
		String id = ref.substring(i + start.length());
		return id;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if allowd, false if not
	 */
	protected boolean unlockCheck(String lock, String resource)
	{
		if (!securityService().unlock(lock, resource))
		{
			return false;
		}

		return true;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the azGroup does not have access
	 */
	protected void unlock(String lock, String resource) throws AuthzPermissionException
	{
		if (!unlockCheck(lock, resource))
		{
			throw new AuthzPermissionException(sessionManager().getCurrentSessionUserId(), lock, resource);
		}
	}

	/**
	 * Create the live properties for the azGroup.
	 */
	protected void addLiveProperties(BaseAuthzGroup azGroup)
	{
		String current = sessionManager().getCurrentSessionUserId();

		azGroup.m_createdUserId = current;
		azGroup.m_lastModifiedUserId = current;

		Time now = timeService().newTime();
		azGroup.m_createdTime = now;
		azGroup.m_lastModifiedTime = (Time) now.clone();
	}

	/**
	 * Update the live properties for an AuthzGroup for when modified.
	 */
	protected void addLiveUpdateProperties(BaseAuthzGroup azGroup)
	{
		String current = sessionManager().getCurrentSessionUserId();

		azGroup.m_lastModifiedUserId = current;
		azGroup.m_lastModifiedTime = timeService().newTime();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Provider configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Configuration: set the azGroup provider helper service.
	 * 
	 * @param provider
	 *        the azGroup provider helper service.
	 */
	public void setProvider(GroupProvider provider)
	{
		m_provider = provider;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

	/**
	 * @return the EntityManager collaborator.
	 */
	protected abstract EntityManager entityManager();

	/**
	 * @return the FunctionManager collaborator.
	 */
	protected abstract FunctionManager functionManager();

	/**
	 * @return the SecurityService collaborator.
	 */
	protected abstract SecurityService securityService();

	/**
	 * @return the TimeService collaborator.
	 */
	protected abstract TimeService timeService();

	/**
	 * @return the SessionManager collaborator.
	 */
	protected abstract SessionManager sessionManager();

	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected abstract EventTrackingService eventTrackingService();

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract UserDirectoryService userDirectoryService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			m_relativeAccessPoint = REFERENCE_ROOT;

			// construct storage and read
			m_storage = newStorage();
			m_storage.open();

			// register as an entity producer
			entityManager().registerEntityProducer(this, REFERENCE_ROOT);

			// register functions
			functionManager().registerFunction(SECURE_ADD_AUTHZ_GROUP);
			functionManager().registerFunction(SECURE_REMOVE_AUTHZ_GROUP);
			functionManager().registerFunction(SECURE_UPDATE_AUTHZ_GROUP);
			functionManager().registerFunction(SECURE_UPDATE_OWN_AUTHZ_GROUP);

			// if no provider was set, see if we can find one
			if (m_provider == null)
			{
				m_provider = (GroupProvider) ComponentManager.get(GroupProvider.class.getName());
			}

			M_log.info("init(): provider: " + ((m_provider == null) ? "none" : m_provider.getClass().getName()));
		}
		catch (Throwable t)
		{
			M_log.warn("init(); ", t);
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		m_storage.close();
		m_storage = null;

		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AuthzGroupService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public List getAuthzGroups(String criteria, PagingPosition page)
	{
		return m_storage.getAuthzGroups(criteria, page);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getAuthzUserGroupIds(ArrayList authzGroupIds, String userid)
	{
		return m_storage.getAuthzUserGroupIds(authzGroupIds, userid);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int countAuthzGroups(String criteria)
	{
		return m_storage.countAuthzGroups(criteria);
	}
	
	/**
	 * {@inheritDoc} 
	 */
	public Set getAuthzGroupIds(String providerId)
	{
		return m_storage.getAuthzGroupIds(providerId);
	}

	/**
	 * {@inheritDoc} 
	 */
	public Set getProviderIds(String authzGroupId)
	{
		return m_storage.getProviderIds(authzGroupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public AuthzGroup getAuthzGroup(String id) throws GroupNotDefinedException
	{
		// Note: since this is a "read" operations, we do NOT refresh (i.e. write) the provider info.
		if (id == null) throw new GroupNotDefinedException("<null>");

		AuthzGroup azGroup = m_storage.get(id);

		// if not found
		if (azGroup == null)
		{
			throw new GroupNotDefinedException(id);
		}

		return azGroup;
	}

	/**
	 * {@inheritDoc}
	 */
	public void joinGroup(String authzGroupId, String roleId) throws GroupNotDefinedException, AuthzPermissionException
	{
		joinGroup(authzGroupId, roleId, 0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void joinGroup(String authzGroupId, String roleId, int maxSize) throws GroupNotDefinedException, AuthzPermissionException, GroupFullException
	{
		String user = sessionManager().getCurrentSessionUserId();
		if (user == null) throw new AuthzPermissionException(user, SECURE_UPDATE_OWN_AUTHZ_GROUP, authzGroupId);

		// check security (throws if not permitted)
		unlock(SECURE_UPDATE_OWN_AUTHZ_GROUP, authzGroupId);

		// get the AuthzGroup
		AuthzGroup azGroup = m_storage.get(authzGroupId);
		if (azGroup == null)
		{
			throw new GroupNotDefinedException(authzGroupId);
		}

		// check that the role exists
		Role role = azGroup.getRole(roleId);
		if (role == null)
		{
			throw new GroupNotDefinedException(roleId);
		}

		((BaseAuthzGroup) azGroup).setEvent(SECURE_UPDATE_OWN_AUTHZ_GROUP);

		// see if already a member
		BaseMember grant = (BaseMember) azGroup.getMember(user);
		
		if (grant == null)
			addMemberToGroup(azGroup, user, roleId, maxSize);
		else		
			// if inactive, deny permission to join
			if (!grant.active) 
				throw new AuthzPermissionException(user, SECURE_UPDATE_OWN_AUTHZ_GROUP, authzGroupId); 
			
		// If the user is already in the group and active, or is already in the group and active but
		// with a different role, no action will be taken
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void unjoinGroup(String authzGroupId) throws GroupNotDefinedException, AuthzPermissionException
	{
		String user = sessionManager().getCurrentSessionUserId();
		if (user == null) throw new AuthzPermissionException(user, SECURE_UPDATE_OWN_AUTHZ_GROUP, authzGroupId);

		// check security (throws if not permitted)
		unlock(SECURE_UPDATE_OWN_AUTHZ_GROUP, authzGroupId);

		// get the AuthzGroup
		AuthzGroup azGroup = m_storage.get(authzGroupId);
		if (azGroup == null)
		{
			throw new GroupNotDefinedException(authzGroupId);
		}

		// if not joined (no grant), we are done
		BaseMember grant = (BaseMember) azGroup.getMember(user);
		if (grant == null)
		{
			return;
		}

		// if the user currently is the only maintain role user, disallow the unjoin
		if (grant.getRole().getId().equals(azGroup.getMaintainRole()))
		{
			Set maintainers = azGroup.getUsersHasRole(azGroup.getMaintainRole());
			if (maintainers.size() <= 1)
			{
				throw new AuthzPermissionException(user, SECURE_UPDATE_OWN_AUTHZ_GROUP, authzGroupId);
			}
		}

		// if the grant is provided, disallow the unjoin. There would be no point in 
		// allowing the user to unjoin, since the user will rejoin the realm the next
		// time it is updated or he/she logs in.
		
		if (grant.isProvided())
		{
			throw new AuthzPermissionException(user, SECURE_UPDATE_OWN_AUTHZ_GROUP, authzGroupId);
		}

		((BaseAuthzGroup) azGroup).setEvent(SECURE_UPDATE_OWN_AUTHZ_GROUP);

		removeMemberFromGroup(azGroup, user);

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean allowJoinGroup(String authzGroupId)
	{
		String user = sessionManager().getCurrentSessionUserId();
		if (user == null) return false;

		// check security (throws if not permitted)
		if (!unlockCheck(SECURE_UPDATE_OWN_AUTHZ_GROUP, authzGroupId))
				return false;
		
		// get the AuthzGroup
		AuthzGroup azGroup = m_storage.get(authzGroupId);
		if (azGroup == null)
				return false;
		
		// If already a member and inactive, disallow join
		BaseMember grant = (BaseMember) azGroup.getMember(user);
		
		if ((grant != null) && (!grant.active))
			return false;
		
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean allowUnjoinGroup(String authzGroupId)
	{
		String user = sessionManager().getCurrentSessionUserId();
		if (user == null)
		{
			return false;
		}

		// check security (throws if not permitted)
		if (!unlockCheck(SECURE_UPDATE_OWN_AUTHZ_GROUP, authzGroupId)) return false;

		// get the azGroup
		AuthzGroup azGroup = m_storage.get(authzGroupId);
		if (azGroup == null)
		{
			return false;
		}

		// if not joined (no grant), unable to unjoin
		BaseMember grant = (BaseMember) azGroup.getMember(user);
		if (grant == null)
		{
			return false;
		}

		// if the grant is provider, unable to unjoin
		else if (grant.isProvided())
		{
			return false;
		}

		// if the user currently is the only maintain role user, disallow the unjoin
		if (grant.getRole().getId().equals(azGroup.getMaintainRole()))
		{
			Set maintainers = azGroup.getUsersHasRole(azGroup.getMaintainRole());
			if (maintainers.size() <= 1)
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean allowUpdate(String id)
	{
		return unlockCheck(SECURE_UPDATE_AUTHZ_GROUP, authzGroupReference(id));
	}

	/**
	 * {@inheritDoc}
	 */
	public void save(AuthzGroup azGroup) throws GroupNotDefinedException, AuthzPermissionException
	{
		if (azGroup.getId() == null) throw new GroupNotDefinedException("<null>");

		Reference ref = entityManager().newReference(azGroup.getId());
		if (!SiteService.allowUpdateSiteMembership(ref.getId()))
		{
			// check security (throws if not permitted)
			unlock(SECURE_UPDATE_AUTHZ_GROUP, authzGroupReference(azGroup.getId()));
		}

		// make sure it's in storage
		if (!m_storage.check(azGroup.getId()))
		{
			// if this was new, create it in storage
			if (((BaseAuthzGroup) azGroup).m_isNew)
			{
				// reserve an AuthzGroup with this id from the info store - if it's in use, this will return null
				AuthzGroup newAzg = m_storage.put(azGroup.getId());
				if (newAzg == null)
				{
					M_log.warn("saveUsingSecurity, storage.put for a new returns null");
				}
			}
			else
			{
				throw new GroupNotDefinedException(azGroup.getId());
			}
		}

		// complete the save
		completeSave(azGroup);
	}

	/**
	 * Complete the saving of the group, once id and security checks have been cleared.
	 * 
	 * @param azGroup
	 */
	protected void completeSave(AuthzGroup azGroup)
	{
		// update the properties
		addLiveUpdateProperties((BaseAuthzGroup) azGroup);

		// complete the azGroup
		m_storage.save(azGroup);

		// track it
		String event = ((BaseAuthzGroup) azGroup).getEvent();
		if (event == null) event = SECURE_UPDATE_AUTHZ_GROUP;
		eventTrackingService().post(eventTrackingService().newEvent(event, azGroup.getReference(), true));

		// close the azGroup object
		((BaseAuthzGroup) azGroup).closeEdit();

		// update the db with latest provider, and site security with the latest changes, using the updated azGroup
		BaseAuthzGroup updatedRealm = (BaseAuthzGroup) m_storage.get(azGroup.getId());
		updateSiteSecurity(updatedRealm);

		// clear the event for next time
		((BaseAuthzGroup) azGroup).setEvent(null);
	}

	/**
	 * Add member to a group, once id and security checks have been cleared.
	 * 
	 * @param azGroup
	 */
	protected void addMemberToGroup(AuthzGroup azGroup, String userId, String roleId, int maxSize) throws GroupFullException
	{
		 // update the properties (sets last modified time and modified-by)
        addLiveUpdateProperties((BaseAuthzGroup) azGroup);

		// add user to the azGroup
		m_storage.addNewUser(azGroup, userId, roleId, maxSize);

		// track it
		String event = ((BaseAuthzGroup) azGroup).getEvent();
		if (event == null) event = SECURE_UPDATE_AUTHZ_GROUP;
		eventTrackingService().post(eventTrackingService().newEvent(event, azGroup.getReference(), true));

		// close the azGroup object
		((BaseAuthzGroup) azGroup).closeEdit();

		// update the db with latest provider, and site security with the latest changes, using the updated azGroup
		BaseAuthzGroup updatedRealm = (BaseAuthzGroup) m_storage.get(azGroup.getId());
		updateSiteSecurity(updatedRealm);

		// clear the event for next time
		((BaseAuthzGroup) azGroup).setEvent(null);
	}


	/**
	 * Add member to a group, once id and security checks have been cleared.
	 * 
	 * @param azGroup
	 */
	protected void removeMemberFromGroup(AuthzGroup azGroup, String userId) 
	{
		 // update the properties (sets last modified time and modified-by)
        addLiveUpdateProperties((BaseAuthzGroup) azGroup);

		// remove user from the azGroup
		m_storage.removeUser(azGroup, userId);

		// track it
		String event = ((BaseAuthzGroup) azGroup).getEvent();
		if (event == null) event = SECURE_UPDATE_AUTHZ_GROUP;
		eventTrackingService().post(eventTrackingService().newEvent(event, azGroup.getReference(), true));

		// close the azGroup object
		((BaseAuthzGroup) azGroup).closeEdit();

		// update the db with latest provider, and site security with the latest changes, using the updated azGroup
		BaseAuthzGroup updatedRealm = (BaseAuthzGroup) m_storage.get(azGroup.getId());
		updateSiteSecurity(updatedRealm);

		// clear the event for next time
		((BaseAuthzGroup) azGroup).setEvent(null);
	}


	
	/**
	 * {@inheritDoc}
	 */
	public boolean allowAdd(String id)
	{
		return unlockCheck(SECURE_ADD_AUTHZ_GROUP, authzGroupReference(id));
	}

	/**
	 * {@inheritDoc}
	 */
	public AuthzGroup addAuthzGroup(String id) throws GroupIdInvalidException, GroupAlreadyDefinedException,
			AuthzPermissionException
	{
		// check security (throws if not permitted)
		unlock(SECURE_ADD_AUTHZ_GROUP, authzGroupReference(id));

		// reserve an AuthzGroup with this id from the info store - if it's in use, this will return null
		AuthzGroup azGroup = m_storage.put(id);
		if (azGroup == null)
		{
			throw new GroupAlreadyDefinedException(id);
		}

		((BaseAuthzGroup) azGroup).setEvent(SECURE_ADD_AUTHZ_GROUP);

		// update the properties
		addLiveProperties((BaseAuthzGroup) azGroup);

		// save
		completeSave(azGroup);

		return azGroup;
	}

	/**
	 * {@inheritDoc}
	 */
	public AuthzGroup addAuthzGroup(String id, AuthzGroup other, String userId) throws GroupIdInvalidException,
			GroupAlreadyDefinedException, AuthzPermissionException
	{
		// make the new AuthzGroup
		AuthzGroup azGroup = addAuthzGroup(id);

		// move in the values from the old AuthzGroup (this includes the id, which we restore
		((BaseAuthzGroup) azGroup).set(other);
		((BaseAuthzGroup) azGroup).m_id = id;

		// give the user the "maintain" role
		String roleName = azGroup.getMaintainRole();
		if ((roleName != null) && (userId != null))
		{
			azGroup.addMember(userId, roleName, true, false);
		}

		// update the properties
		addLiveProperties((BaseAuthzGroup) azGroup);

		// save
		completeSave(azGroup);

		return azGroup;
	}

	/**
	 * {@inheritDoc}
	 */
	public AuthzGroup newAuthzGroup(String id, AuthzGroup other, String userId) throws GroupAlreadyDefinedException
	{
		// make the new AuthzGroup
		BaseAuthzGroup azGroup = new BaseAuthzGroup(this,id);
		azGroup.m_isNew = true;

		// move in the values from the old AuthzGroup (this includes the id, which we restore)
		if (other != null)
		{
			azGroup.set(other);
			azGroup.m_id = id;
		}

		// give the user the "maintain" role
		String roleName = azGroup.getMaintainRole();
		if ((roleName != null) && (userId != null))
		{
			azGroup.addMember(userId, roleName, true, false);
		}

		return azGroup;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean allowRemove(String id)
	{
		return unlockCheck(SECURE_REMOVE_AUTHZ_GROUP, authzGroupReference(id));
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAuthzGroup(AuthzGroup azGroup) throws AuthzPermissionException
	{
		// check security (throws if not permitted)
		unlock(SECURE_REMOVE_AUTHZ_GROUP, azGroup.getReference());

		// complete the azGroup
		m_storage.remove(azGroup);

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(SECURE_REMOVE_AUTHZ_GROUP, azGroup.getReference(), true));

		// close the azGroup object
		((BaseAuthzGroup) azGroup).closeEdit();

		// clear any site security based on this (if a site) azGroup
		removeSiteSecurity(azGroup);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAuthzGroup(String azGroupId) throws AuthzPermissionException
	{
		if (azGroupId == null) return;

		// check for existance
		AuthzGroup azGroup = m_storage.get(azGroupId);
		if (azGroup == null)
		{
			return;
		}

		// check security (throws if not permitted)
		unlock(SECURE_REMOVE_AUTHZ_GROUP, authzGroupReference(azGroupId));

		// complete the azGroup
		m_storage.remove(azGroup);

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(SECURE_REMOVE_AUTHZ_GROUP, azGroup.getReference(), true));

		// close the azGroup object
		((BaseAuthzGroup) azGroup).closeEdit();

		// clear any site security based on this (if a site) azGroup
		removeSiteSecurity(azGroup);
	}

	/**
	 * {@inheritDoc}
	 */
	public String authzGroupReference(String id)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + id;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAllowed(String user, String function, String azGroupId)
	{
		return m_storage.isAllowed(user, function, azGroupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAllowed(String user, String function, Collection azGroups)
	{
		return m_storage.isAllowed(user, function, azGroups);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set getUsersIsAllowed(String function, Collection azGroups)
	{
		return m_storage.getUsersIsAllowed(function, azGroups);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Set<String[]> getUsersIsAllowedByGroup(String function, Collection<String> azGroups)
	{
		return m_storage.getUsersIsAllowedByGroup(function, azGroups);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String,Integer> getUserCountIsAllowed(String function, Collection<String> azGroups)
	{
		return m_storage.getUserCountIsAllowed(function, azGroups);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Set getAllowedFunctions(String role, Collection azGroups)
	{
		return m_storage.getAllowedFunctions(role, azGroups);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set getAuthzGroupsIsAllowed(String userId, String function, Collection azGroups)
	{
		return m_storage.getAuthzGroupsIsAllowed(userId, function, azGroups);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserRole(String userId, String azGroupId)
	{
		return m_storage.getUserRole(userId, azGroupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map getUsersRole(Collection userIds, String azGroupId)
	{
		return m_storage.getUsersRole(userIds, azGroupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public void refreshUser(String userId)
	{
		if ((m_provider == null) || (userId == null)) return;

		try
		{
			String eid = userDirectoryService().getUserEid(userId);

			// wrap the provided map in our special map that will deal with compound provider ids
			Map providerGrants = new ProviderMap(m_provider, m_provider.getGroupRolesForUser(eid));

			m_storage.refreshUser(userId, providerGrants);

			// update site security for this user - get the user's realms for the three site locks
			Set updAuthzGroups = getAuthzGroupsIsAllowed(userId, SiteService.SECURE_UPDATE_SITE, null);
			Set unpAuthzGroups = getAuthzGroupsIsAllowed(userId, SiteService.SITE_VISIT_UNPUBLISHED, null);
			Set visitAuthzGroups = getAuthzGroupsIsAllowed(userId, SiteService.SITE_VISIT, null);

			// convert from azGroup ids (potential site references) to site ids for those that are site,
			// skipping special and user sites other than our user's
			Set updSites = new HashSet();
			for (Iterator i = updAuthzGroups.iterator(); i.hasNext();)
			{
				String azGroupId = (String) i.next();
				Reference ref = entityManager().newReference(azGroupId);
				if ((SiteService.APPLICATION_ID.equals(ref.getType())) && SiteService.SITE_SUBTYPE.equals(ref.getSubType())
						&& !SiteService.isSpecialSite(ref.getId())
						&& (!SiteService.isUserSite(ref.getId()) || userId.equals(SiteService.getSiteUserId(ref.getId()))))
				{
					updSites.add(ref.getId());
				}
			}

			Set unpSites = new HashSet();
			for (Iterator i = unpAuthzGroups.iterator(); i.hasNext();)
			{
				String azGroupId = (String) i.next();
				Reference ref = entityManager().newReference(azGroupId);
				if ((SiteService.APPLICATION_ID.equals(ref.getType())) && SiteService.SITE_SUBTYPE.equals(ref.getSubType())
						&& !SiteService.isSpecialSite(ref.getId())
						&& (!SiteService.isUserSite(ref.getId()) || userId.equals(SiteService.getSiteUserId(ref.getId()))))
				{
					unpSites.add(ref.getId());
				}
			}

			Set visitSites = new HashSet();
			for (Iterator i = visitAuthzGroups.iterator(); i.hasNext();)
			{
				String azGroupId = (String) i.next();
				Reference ref = entityManager().newReference(azGroupId);
				if ((SiteService.APPLICATION_ID.equals(ref.getType())) && SiteService.SITE_SUBTYPE.equals(ref.getSubType())
						&& !SiteService.isSpecialSite(ref.getId())
						&& (!SiteService.isUserSite(ref.getId()) || userId.equals(SiteService.getSiteUserId(ref.getId()))))
				{
					visitSites.add(ref.getId());
				}
			}

			SiteService.setUserSecurity(userId, updSites, unpSites, visitSites);
		}
		catch (UserNotDefinedException e)
		{
			M_log.warn("refreshUser: cannot find eid for user: " + userId);
		}
	}

	/**
	 * Update the site security based on the values in the AuthzGroup, if it is a site AuthzGroup.
	 * 
	 * @param azGroup
	 *        The AuthzGroup.
	 */
	protected void updateSiteSecurity(AuthzGroup azGroup)
	{
		// Special code for the site service
		Reference ref = entityManager().newReference(azGroup.getId());
		if (SiteService.APPLICATION_ID.equals(ref.getType()) && SiteService.SITE_SUBTYPE.equals(ref.getSubType()))
		{
			// collect the users
			Set updUsers = azGroup.getUsersIsAllowed(SiteService.SECURE_UPDATE_SITE);
			Set unpUsers = azGroup.getUsersIsAllowed(SiteService.SITE_VISIT_UNPUBLISHED);
			Set visitUsers = azGroup.getUsersIsAllowed(SiteService.SITE_VISIT);

			SiteService.setSiteSecurity(ref.getId(), updUsers, unpUsers, visitUsers);
		}
	}

	/**
	 * Update the site security when an AuthzGroup is deleted, if it is a site AuthzGroup.
	 * 
	 * @param azGroup
	 *        The AuthzGroup.
	 */
	protected void removeSiteSecurity(AuthzGroup azGroup)
	{
		// Special code for the site service
		Reference ref = entityManager().newReference(azGroup.getId());
		if (SiteService.APPLICATION_ID.equals(ref.getType()) && SiteService.SITE_SUBTYPE.equals(ref.getSubType()))
		{
			// no azGroup, no users
			Set empty = new HashSet();

			SiteService.setSiteSecurity(ref.getId(), empty, empty, empty);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * EntityProducer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "authzGroup";
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		// for azGroup access
		if (reference.startsWith(REFERENCE_ROOT))
		{
			// the azGroup id may have separators - we use everything after "/realm/"
			String id = reference.substring(REFERENCE_ROOT.length() + 1, reference.length());

			ref.set(APPLICATION_ID, null, id, null, null);

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		Collection rv = new Vector();

		// if the reference is an AuthzGroup, and not a special one
		// get the list of realms for the azGroup-referenced resource
		if ((ref.getId() != null) && (ref.getId().length() > 0) && (!ref.getId().startsWith("!")))
		{
			// add the current user's azGroup (for what azGroup stuff everyone can do, i.e. add)
			ref.addUserAuthzGroup(rv, sessionManager().getCurrentSessionUserId());

			// make a new reference on the azGroup's id
			Reference refnew = entityManager().newReference(ref.getId());
			rv.addAll(refnew.getAuthzGroups(userId));
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		return "";
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Open.
		 */
		void open();

		/**
		 * Close.
		 */
		void close();

		/**
		 * Check if an AuthzGroup by this id exists.
		 * 
		 * @param id
		 *        The AuthzGroup id.
		 * @return true if an AuthzGroup by this id exists, false if not.
		 */
		boolean check(String id);

		/**
		 * Get the AuthzGroup with this id, or null if not found.
		 * 
		 * @param id
		 *        The AuthzGroup id.
		 * @return The AuthzGroup with this id, or null if not found.
		 */
		AuthzGroup get(String id);

		/**
		 * Add a new AuthzGroup with this id.
		 * 
		 * @param id
		 *        The AuthzGroup id.
		 * @return The new AuthzGroup, or null if the id is in use.
		 */
		AuthzGroup put(String id);

		/**
		 * Save the changes to the AuthzGroup
		 * 
		 * @param azGroup
		 *        The AuthzGroup to save.
		 */
		void save(AuthzGroup azGroup);

		/**
		 * Add a user to the AuthzGroup
		 * 
		 * @param azGroup
		 *        The AuthzGroup to which the user is being added
		 * @param userId
		 *        The user to add
		 * @param roleId
		 *        The user's role
		 * @param maxSize
		 *        The maximum size of the group.
		 */
		void addNewUser(AuthzGroup azGroup, String userId, String roleId, int maxSize) throws GroupFullException;

		/**
		 * Remove a user from the AuthzGroup
		 * 
		 * @param azGroup
		 *        The AuthzGroup to which the user is being added
		 * @param userId
		 *        The user to remove
		 */
		void removeUser(AuthzGroup azGroup, String userId);
		
		/**
		 * Remove this AuthzGroup.
		 * 
		 * @param azGroup
		 *        The azGroup to remove.
		 */
		void remove(AuthzGroup azGroup);

		/**
		 * Access a list of AuthzGroups that meet specified criteria, naturally sorted.
		 * 
		 * @param criteria
		 *        Selection criteria: AuthzGroups returned will match this string somewhere in their id, or provider group id.
		 * @param page
		 *        The PagePosition subset of items to return.
		 * @return The List (AuthzGroup) of AuthzGroups that meet specified criteria.
		 */
		List getAuthzGroups(String criteria, PagingPosition page);

		/**
		 * Access a list of AuthzGroups that meet specified criteria for a specified user_id
		 * 
		 * @param authzGroupIds
		 *        AuthzGroup selection criteria (list of authz group ids)
		 * @param user_id
		 *        Return only groups with user_id as a member
		 * @return The List (AuthzGroup) that meet specified criteria.
		 */
		List getAuthzUserGroupIds(ArrayList authzGroupIds, String user_id);

		/**
		 * Count the AuthzGroup objets that meet specified criteria.
		 * 
		 * @param criteria
		 *        Selection criteria: realms returned will match this string somewhere in their id, or provider group id.
		 * @return The count of AuthzGroups that meet specified criteria.
		 */
		int countAuthzGroups(String criteria);

		/**
		 * Get the provider IDs for an AuthzGroup
		 * 
		 * @param authzGroupId The ID of the AuthzGroup
		 * @return The Set (String) of provider IDs
		 */
		public Set getProviderIds(String authzGroupId);

		/**
		 * Get the AuthzGroup IDs associated with a provider ID.
		 * 
		 * @param providerId The provider id
		 * @return The Set (String) of AuthzGroup IDs
		 */
		public Set getAuthzGroupIds(String providerId);

		/**
		 * Complete the read process once the basic AuthzGroup info has been read.
		 * 
		 * @param azGroup
		 *        The AuthzGroup to complete.
		 */
		void completeGet(BaseAuthzGroup azGroup);

		/**
		 * Test if this user is allowed to perform the function in the named AuthzGroup.
		 * 
		 * @param userId
		 *        The user id.
		 * @param function
		 *        The function to open.
		 * @param azGroupId
		 *        The AuthzGroup id to consult, if it exists.
		 * @return true if this user is allowed to perform the function in the named AuthzGroup, false if not.
		 */
		boolean isAllowed(String userId, String function, String azGroupId);

		/**
		 * Test if this user is allowed to perform the function in the named AuthzGroups.
		 * 
		 * @param userId
		 *        The user id.
		 * @param function
		 *        The function to open.
		 * @param azGroups
		 *        A collection of AuthzGroup ids to consult.
		 * @return true if this user is allowed to perform the function in the named AuthzGroups, false if not.
		 */
		boolean isAllowed(String userId, String function, Collection realms);

		/**
		 * Get the set of user ids of users who are allowed to perform the function in the named AuthzGroups.
		 * 
		 * @param function
		 *        The function to check.
		 * @param azGroups
		 *        A collection of the ids of AuthzGroups to consult.
		 * @return the Set (String) of user ids of users who are allowed to perform the function in the named AuthzGroups.
		 */
		Set getUsersIsAllowed(String function, Collection azGroups);

		/**
		 * Get the set of user ids per group of users who are allowed to perform the function in the named AuthzGroups.
		 * 
		 * @param function
		 *        The function to check.
		 * @param azGroups
		 *        A collection of the ids of AuthzGroups to consult.
		 * @return A Set of String arrays (userid, realm) with user ids per group who are allowed to perform the function in the named AuthzGroups.
		 */
		Set<String[]> getUsersIsAllowedByGroup(String function, Collection<String> azGroups);

		
		/**
		 * Get the number of users per group who are allowed to perform the function in the given AuthzGroups.
		 * 
		 * @param function
		 *        The function to check.
		 * @param azGroups
		 *        A collection of the ids of AuthzGroups to consult.
		 * @return A Map (authzgroupid (String) -> user count (Integer) ) of the number of users who are allowed to perform the function in the given AuthzGroups.
		 */
		Map<String,Integer> getUserCountIsAllowed(String function, Collection<String> azGroups);

		
		/**
		 * Get the set of functions that users with this role in these AuthzGroups are allowed to perform.
		 * 
		 * @param role
		 *        The role name.
		 * @param azGroups
		 *        A collection of AuthzGroup ids to consult.
		 * @return the Set (String) of functions that users with this role in these AuthzGroups are allowed to perform
		 */
		Set getAllowedFunctions(String role, Collection azGroups);

		/**
		 * Get the set of AuthzGroup ids in which this user is allowed to perform this function.
		 * 
		 * @param userId
		 *        The user id.
		 * @param function
		 *        The function to check.
		 * @param azGroups
		 *        The Collection of AuthzGroup ids to search; if null, search them all.
		 * @return the Set (String) of AuthzGroup ids in which this user is allowed to perform this function.
		 */
		Set getAuthzGroupsIsAllowed(String userId, String function, Collection azGroups);

		/**
		 * Get the role name for this user in this AuthzGroup.
		 * 
		 * @param userId
		 *        The user id.
		 * @param function
		 *        The function to open.
		 * @param azGroupId
		 *        The AuthzGroup id to consult, if it exists.
		 * @return the role name for this user in this AuthzGroup, if the user has active membership, or null if not.
		 */
		String getUserRole(String userId, String azGroupId);

		/**
		 * Get the role name for each user in the userIds Collection in this AuthzGroup.
		 * 
		 * @param userId
		 *        The user id.
		 * @param function
		 *        The function to open.
		 * @param azGroupId
		 *        The AuthzGroup id to consult, if it exists.
		 * @return A Map (userId -> role name) of role names for each user who have active membership; if the user does not, it will not be in the Map.
		 */
		Map getUsersRole(Collection userIds, String azGroupId);

		/**
		 * Refresh this user's roles in any AuthzGroup that has an entry in the map; the user's new role is in the map.
		 * 
		 * @param userId
		 *        The user id
		 * @param providerMembership
		 *        The Map of external group id -> role id.
		 */
		void refreshUser(String userId, Map providerMembership);

		/**
		 * Refresh the external user - role membership for this AuthzGroup
		 * 
		 * @param azGroup
		 *        The azGroup to refresh.
		 */
		void refreshAuthzGroup(BaseAuthzGroup azGroup);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation (no container)
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a new continer given just an id.
	 * 
	 * @param id
	 *        The id for the new object.
	 * @return The new containe Resource.
	 */
	public Entity newContainer(String ref)
	{
		return null;
	}

	/**
	 * Construct a new container resource, from an XML element.
	 * 
	 * @param element
	 *        The XML.
	 * @return The new container resource.
	 */
	public Entity newContainer(Element element)
	{
		return null;
	}

	/**
	 * Construct a new container resource, as a copy of another
	 * 
	 * @param other
	 *        The other contianer to copy.
	 * @return The new container resource.
	 */
	public Entity newContainer(Entity other)
	{
		return null;
	}

	/**
	 * Construct a new rsource given just an id.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param id
	 *        The id for the new object.
	 * @param others
	 *        (options) array of objects to load into the Resource's fields.
	 * @return The new resource.
	 */
	public Entity newResource(Entity container, String id, Object[] others)
	{
		return new BaseAuthzGroup(this,id);
	}

	/**
	 * Construct a new resource, from an XML element.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param element
	 *        The XML.
	 * @return The new resource from the XML.
	 */
	public Entity newResource(Entity container, Element element)
	{
		return new BaseAuthzGroup(this,element);
	}

	/**
	 * Construct a new resource from another resource of the same type.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param other
	 *        The other resource.
	 * @return The new resource as a copy of the other.
	 */
	public Entity newResource(Entity container, Entity other)
	{
		return new BaseAuthzGroup(this,(AuthzGroup) other);
	}

	/**
	 * Construct a new continer given just an id.
	 * 
	 * @param id
	 *        The id for the new object.
	 * @return The new containe Resource.
	 */
	public Edit newContainerEdit(String ref)
	{
		return null;
	}

	/**
	 * Construct a new container resource, from an XML element.
	 * 
	 * @param element
	 *        The XML.
	 * @return The new container resource.
	 */
	public Edit newContainerEdit(Element element)
	{
		return null;
	}

	/**
	 * Construct a new container resource, as a copy of another
	 * 
	 * @param other
	 *        The other contianer to copy.
	 * @return The new container resource.
	 */
	public Edit newContainerEdit(Entity other)
	{
		return null;
	}

	/**
	 * Construct a new rsource given just an id.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param id
	 *        The id for the new object.
	 * @param others
	 *        (options) array of objects to load into the Resource's fields.
	 * @return The new resource.
	 */
	public Edit newResourceEdit(Entity container, String id, Object[] others)
	{
		BaseAuthzGroup e = new BaseAuthzGroup(this,id);
		e.activate();
		return e;
	}

	/**
	 * Construct a new resource, from an XML element.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param element
	 *        The XML.
	 * @return The new resource from the XML.
	 */
	public Edit newResourceEdit(Entity container, Element element)
	{
		BaseAuthzGroup e = new BaseAuthzGroup(this,element);
		e.activate();
		return e;
	}

	/**
	 * Construct a new resource from another resource of the same type.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param other
	 *        The other resource.
	 * @return The new resource as a copy of the other.
	 */
	public Edit newResourceEdit(Entity container, Entity other)
	{
		BaseAuthzGroup e = new BaseAuthzGroup(this,(AuthzGroup) other);
		e.activate();
		return e;
	}

	/**
	 * Collect the fields that need to be stored outside the XML (for the resource).
	 * 
	 * @return An array of field values to store in the record outside the XML (for the resource).
	 */
	public Object[] storageFields(Entity r)
	{
		return null;
	}

	/**
	 * Check if this resource is in draft mode.
	 * 
	 * @param r
	 *        The resource.
	 * @return true if the resource is in draft mode, false if not.
	 */
	public boolean isDraft(Entity r)
	{
		return false;
	}

	/**
	 * Access the resource owner user id.
	 * 
	 * @param r
	 *        The resource.
	 * @return The resource owner user id.
	 */
	public String getOwnerId(Entity r)
	{
		return null;
	}

	/**
	 * Access the resource date.
	 * 
	 * @param r
	 *        The resource.
	 * @return The resource date.
	 */
	public Time getDate(Entity r)
	{
		return null;
	}
	
	public class ProviderMap implements Map
	{
		protected Map m_wrapper = null;
		protected GroupProvider m_provider = null;

		public ProviderMap(GroupProvider provider, Map wrapper)
		{
			m_provider = provider;
			m_wrapper = wrapper;
		}

		public void clear()
		{
			m_wrapper.clear();
		}

		public boolean containsKey(Object key)
		{
			return m_wrapper.containsKey(key);
		}

		public boolean containsValue(Object value)
		{
			return m_wrapper.containsValue(value);
		}

		public Set entrySet()
		{
			return m_wrapper.entrySet();
		}

		public Object get(Object key)
		{
			// if we have this key exactly, use it
			Object value = m_wrapper.get(key);
			if (value != null) return value;

			// otherwise break up key as a compound id and find what values we have for these
			// the values are roles, and we prefer "maintain" to "access"
			String rv = null;
			String[] ids = m_provider.unpackId((String) key);
			for (int i = 0; i < ids.length; i++)
			{
				// try this one
				value = m_wrapper.get(ids[i]);
				
				// if we found one already, ask the provider which to keep
				if (value != null)
				{
					rv = m_provider.preferredRole((String)value, rv);
				}
			}

			return rv;
		}

		public boolean isEmpty()
		{
			return m_wrapper.isEmpty();
		}

		public Set keySet()
		{
			return m_wrapper.keySet();
		}

		public Object put(Object key, Object value)
		{
			return m_wrapper.put(key, value);
		}

		public void putAll(Map t)
		{
			m_wrapper.putAll(t);
		}

		public Object remove(Object key)
		{
			return m_wrapper.remove(key);
		}

		public int size()
		{
			return m_wrapper.size();
		}

		public Collection values()
		{
			return m_wrapper.values();
		}		
	}
}
