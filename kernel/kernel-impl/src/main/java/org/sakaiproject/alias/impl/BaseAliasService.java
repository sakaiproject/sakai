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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.alias.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasEdit;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.*;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.*;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.*;

/**
 * <p>
 * BaseAliasService is ...
 * </p>
 */
@Slf4j
public abstract class BaseAliasService implements AliasService, SingleStorageUser
{
	/** Storage manager for this service. */
	protected Storage m_storage = null;

	/** The initial portion of a relative access point URL. */
	protected String m_relativeAccessPoint = null;

	/** A cache of calls to the service and the results. */
	/** The # seconds to cache gets. 0 disables the cache. */
	protected int m_cacheSeconds = 0;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc.
	 *********************************************************************************************************************************************************************************************************************************************************/
	/** The # seconds to cache gets. 0 disables the cache. */
	protected int m_cacheCleanerSeconds = 0;
	private List<String> prohibited_aliases = null;

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

	} // getAccessPoint

	/**
	 * Access the internal reference which can be used to access the resource from within the system.
	 *
	 * @param id
	 *        The alias id string.
	 * @return The the internal reference which can be used to access the resource from within the system.
	 */
	public String aliasReference(String id)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + id;

	} // aliasReference

	/**
	 * Access the alias id extracted from a alias reference.
	 *
	 * @param ref
	 *        The alias reference string.
	 * @return The the alias id extracted from a alias reference.
	 */
	protected String aliasId(String ref)
	{
		String start = getAccessPoint(true) + Entity.SEPARATOR;
		int i = ref.indexOf(start);
		if (i == -1) return ref;
		String id = ref.substring(i + start.length());
		return id;

	} // aliasId

	/**
	 * Check security permission.
	 *
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if allowed, false if not
	 */
	protected boolean unlockCheck(String lock, String resource)
	{
		if (!securityService().unlock(lock, resource))
		{
			return false;
		}

		return true;

	} // unlockCheck

	/**
	 * Check security permission.
	 *
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access
	 */
	protected void unlock(String lock, String resource) throws PermissionException
	{
		if (!unlockCheck(lock, resource))
		{
			throw new PermissionException(sessionManager().getCurrentSessionUserId(), lock, resource);
		}

	} // unlock

	/**
	 * Check security permission, target modify based.
	 *
	 * @param target
	 *        The target resource reference string.
	 * @return true if allowed, false if not
	 */
	protected boolean unlockTargetCheck(String target)
	{
		// check the target for modify access.
		// TODO: this is setup only for sites and mail archive channels, we need an Entity Model based generic "allowModify()" -ggolden.
		Reference ref = entityManager().newReference(target);
		if (ref.getType().equals(SiteService.APPLICATION_ID))
		{
			// For all site references (page/site/group check against the actual site. We don't use the context because
			// site references have a context of null.
			return siteService().allowUpdateSite(ref.getContainer());
		}

		// TODO: fake this dependency (MailArchiveService.APPLICATION_ID) to keep the mailarchive dependencies away -ggolden
		else if (ref.getType().equals("sakai:mailarchive"))
		{
			// base this on site update, too
			log.debug("checing allow update on " + ref.getContext());
			//due to a bug in the mailarchive entity manager the context may be the strign null
			if (ref.getContext() != null && !ref.getContext().equals("null"))
			{
				log.debug("Checking allow update on " + ref.getContext() + " with lenght: " + ref.getContext().length());
				return siteService().allowUpdateSite(ref.getContext());
			}
			else
			{
				boolean ret = siteService().allowAddSite(null);
				log.debug("Cheking site.add permission returning: " + ret);
				return ret;
			}
		}

		// TODO: fake this dependency (CalendarService.APPLICATION_ID) to keep the calendar dependencies away
		else if (ref.getType().equals("sakai:calendar"))
		{
			// base this on site update, too
			return siteService().allowUpdateSite(ref.getContext());
		}

		// TODO: fake this dependency (AnnouncementService.APPLICATION_ID) to keep the announcement dependencies away
		else if (ref.getType().equals("sakai:announcement"))
		{
			// base this on site update, too
			return siteService().allowUpdateSite(ref.getContext());
		}

		return false;

	} // unlockTargetCheck

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Create the live properties for the user.
	 */
	protected void addLiveProperties(ResourcePropertiesEdit props)
	{
		String current = sessionManager().getCurrentSessionUserId();

		props.addProperty(ResourceProperties.PROP_CREATOR, current);
		props.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

		String now = timeService().newTime().toString();
		props.addProperty(ResourceProperties.PROP_CREATION_DATE, now);
		props.addProperty(ResourceProperties.PROP_MODIFIED_DATE, now);

	} // addLiveProperties

	/**
	 * Update the live properties for a user for when modified.
	 */
	protected void addLiveUpdateProperties(ResourcePropertiesEdit props)
	{
		String current = sessionManager().getCurrentSessionUserId();

		props.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);
		props.addProperty(ResourceProperties.PROP_MODIFIED_DATE, timeService().newTime().toString());

	} // addLiveUpdateProperties

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract MemoryService memoryService();

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

	/**
	 * @return the EntityManager collaborator.
	 */
	protected abstract EntityManager entityManager();

	/**
	 * @return the SecurityService collaborator.
	 */
	protected abstract SecurityService securityService();

	/**
	 * @return the SessionManager collaborator.
	 */
	protected abstract SessionManager sessionManager();

	/**
	 * @return the SiteService collaborator.
	 */
	protected abstract SiteService siteService();

	/**
	 * @return the TimeService collaborator.
	 */
	protected abstract TimeService timeService();

	/**
	 * @return the FunctionManager collaborator.
	 */
	protected abstract FunctionManager functionManager();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected abstract EventTrackingService eventTrackingService();

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	protected abstract UserDirectoryService userDirectoryService();

	/**
	 * Set the # minutes to cache a get.
	 *
	 * @param time
	 *        The # minutes to cache a get (as an integer string).
	 */
	public void setCacheMinutes(String time)
	{
		m_cacheSeconds = Integer.parseInt(time) * 60;
	}

	/**
	 * Set the # minutes between cache cleanings.
	 * 
	 * @param time
	 *        The # minutes between cache cleanings. (as an integer string).
	 */
	public void setCacheCleanerMinutes(String time)
	{
		m_cacheCleanerSeconds = Integer.parseInt(time) * 60;
	}

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
			functionManager().registerFunction(SECURE_ADD_ALIAS);
			functionManager().registerFunction(SECURE_UPDATE_ALIAS);
			functionManager().registerFunction(SECURE_REMOVE_ALIAS);

			prohibited_aliases = Arrays.asList(serverConfigurationService().getString("mail.prohibitedaliases",
 					"postmaster").trim().toLowerCase().split("\\s*,\\s*"));

		}
		catch (Exception t)
		{
			log.warn("init(): ", t);
		}

	} // init

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		m_storage.close();
		m_storage = null;

		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AliasService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Check if the current user has permission to set this alias.
	 * 
	 * @param alias
	 *        The alias.
	 * @param target
	 *        The resource reference string alias target.
	 * @return true if the current user has permission to set this alias, false if not.
	 */
	public boolean allowSetAlias(String alias, String target)
	{
		if (!securityService().isSuperUser() && 
		    prohibited_aliases.contains(alias.toLowerCase()))
			return false;
		return unlockTargetCheck(target);

	} // allowSetAlias

	/**
	 * Allocate an alias for a resource
	 * 
	 * @param alias
	 *        The alias.
	 * @param target
	 *        The resource reference string alias target.
	 * @throws IdUsedException
	 *         if the alias is already used.
	 * @throws IdInvalidException
	 *         if the alias id is invalid.
	 * @throws PermissionException
	 *         if the current user does not have permission to set this alias.
	 */
	public void setAlias(String alias, String target) throws IdUsedException, IdInvalidException, PermissionException
	{
		if (alias!=null && alias.length()>99){ //KNL-454
			log.warn("The length of the alias: \""+alias+"\" cannot be greater than 99 characters");
			throw new IdInvalidException(alias);
		} 
		// check for a valid alias name
		Validator.checkResourceId(alias);

		if ((!securityService().isSuperUser() && 
		     prohibited_aliases.contains(alias.toLowerCase())) ||
		    !unlockTargetCheck(target))
		{
			throw new PermissionException(sessionManager().getCurrentSessionUserId(), SECURE_ADD_ALIAS, target);
		}

		// attempt to register this alias with storage - if it's in use, this will return null
		AliasEdit a = m_storage.put(alias);
		if (a == null)
		{
			throw new IdUsedException(alias);
		}
		a.setTarget(target);

		// update the properties
		addLiveProperties(a.getPropertiesEdit());

		// complete the edit
		m_storage.commit(a);

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(SECURE_ADD_ALIAS, aliasReference(alias), true));

	} // setAlias

	/**
	 * Check to see if the current user can remove this alias.
	 * 
	 * @param alias
	 *        The alias.
	 * @return true if the current user can remove this alias, false if not.
	 */
	public boolean allowRemoveAlias(String alias)
	{
		return unlockCheck(SECURE_REMOVE_ALIAS, aliasReference(alias));

	} // allowRemoveAlias

	/**
	 * Remove an alias.
	 * 
	 * @param alias
	 *        The alias.
	 * @exception IdUnusedException
	 *            if not found.
	 * @exception PermissionException
	 *            if the current user does not have permission to remove this alias.
	 * @exception InUseException
	 *            if the Alias object is locked by someone else.
	 */
	public void removeAlias(String alias) throws IdUnusedException, PermissionException, InUseException
	{
		AliasEdit a = edit(alias);
		remove(a);

	} // removeAlias

	/**
	 * Check to see if the current user can remove these aliasese for this target resource reference.
	 * 
	 * @param target
	 *        The target resource reference string.
	 * @return true if the current user can remove these aliasese for this target resource reference, false if not.
	 */
	public boolean allowRemoveTargetAliases(String target)
	{
		return unlockTargetCheck(target);

	} // allowRemoveTargetAliases

	/**
	 * Remove all aliases for this target resource reference, if any.
	 * 
	 * @param target
	 *        The target resource reference string.
	 * @throws PermissionException
	 *         if the current user does not have permission to remove these aliases.
	 */
	public void removeTargetAliases(String target) throws PermissionException
	{
		if (!unlockTargetCheck(target))
		{
			throw new PermissionException(sessionManager().getCurrentSessionUserId(), SECURE_REMOVE_ALIAS, target);
		}

		List<Alias> all = getAliases(target);
		for (Iterator<Alias> iAll = all.iterator(); iAll.hasNext();)
		{
			Alias alias = (Alias) iAll.next();
			try
			{
				AliasEdit a = m_storage.edit(alias.getId());
				if (a != null)
				{
					// complete the edit
					m_storage.remove(a);

					// track it
					eventTrackingService().post(eventTrackingService().newEvent(SECURE_REMOVE_ALIAS, a.getReference(), true));
				}
			}
			catch (Exception ignore)
			{
			}
		}

	} // removeTargetAliases

	/**
	 * Find the target resource reference string associated with this alias.
	 * 
	 * @param alias
	 *        The alias.
	 * @return The target resource reference string associated with this alias.
	 * @throws IdUnusedException
	 *         if the alias is not defined.
	 */
	public String getTarget(String alias) throws IdUnusedException
	{
		// check the cache
		String ref = aliasReference(alias);

		BaseAliasEdit a = (BaseAliasEdit) m_storage.get(alias);
		if (a == null) throw new IdUnusedException(alias);

		return a.getTarget();

	} // getTarget

	/**
	 * Find all the aliases defined for this target.
	 * 
	 * @param target
	 *        The target resource reference string.
	 * @return A list (Alias) of all the aliases defined for this target.
	 */
	public List<Alias> getAliases(String target)
	{
		List<Alias> allForTarget = m_storage.getAll(target);

		return allForTarget;

	} // getAliases

	/**
	 * Find all the aliases defined for this target, within the record range given (sorted by id).
	 * 
	 * @param target
	 *        The target resource reference string.
	 * @param first
	 *        The first record position to return.
	 * @param last
	 *        The last record position to return.
	 * @return A list (Alias) of all the aliases defined for this target, within the record range given (sorted by id).
	 */
	public List<Alias> getAliases(String target, int first, int last)
	{
		List<Alias> allForTarget = m_storage.getAll(target, first, last);

		return allForTarget;

	} // getAliases

	/**
	 * Find all the aliases within the record range given (sorted by id).
	 * 
	 * @param first
	 *        The first record position to return.
	 * @param last
	 *        The last record position to return.
	 * @return A list (Alias) of all the aliases within the record range given (sorted by id).
	 */
	public List<Alias> getAliases(int first, int last)
	{
		List<Alias> all = m_storage.getAll(first, last);

		return all;

	} // getAliases

	/**
	 * {@inheritDoc}
	 */
	public int countAliases()
	{
		return m_storage.count();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Alias> searchAliases(String criteria, int first, int last)
	{
		return m_storage.search(criteria, first, last);
	}

	/**
	 * {@inheritDoc}
	 */
	public int countSearchAliases(String criteria)
	{
		return m_storage.countSearch(criteria);
	}

	/**
	 * Check to see if the current user can add an alias.
	 * 
	 * @return true if the current user can add an alias, false if not.
	 */
	public boolean allowAdd()
	{
		return unlockCheck(SECURE_ADD_ALIAS, aliasReference(""));

	} // allowAdd

	/**
	 * Add a new alias. Must commit() to make official, or cancel() when done!
	 * 
	 * @param id
	 *        The alias id.
	 * @return A locked AliasEdit object (reserving the id).
	 * @exception IdInvalidException
	 *            if the alias id is invalid.
	 * @exception IdUsedException
	 *            if the alias id is already used.
	 * @exception PermissionException
	 *            if the current user does not have permission to add an alias.
	 */
	public AliasEdit add(String id) throws IdInvalidException, IdUsedException, PermissionException
	{
		if (id!=null && id.length()>99){ //KNL-454
			log.warn("The length of the alias: \""+id+"\" cannot be greater than 99 characters");
			throw new IdInvalidException(id);
		} 
		// check for a valid user name
		Validator.checkResourceId(id);

		// check security (throws if not permitted)
		unlock(SECURE_ADD_ALIAS, aliasReference(id));

		// reserve an alias with this id from the info store - if it's in use, this will return null
		AliasEdit a = m_storage.put(id);
		if (a == null)
		{
			throw new IdUsedException(id);
		}

		((BaseAliasEdit) a).setEvent(SECURE_ADD_ALIAS);

		return a;

	} // add

	/**
	 * Check to see if the current user can edit this alias.
	 * 
	 * @param id
	 *        The alias id string.
	 * @return true if the current user can edit this alias, false if not.
	 */
	public boolean allowEdit(String id)
	{
		return unlockCheck(SECURE_UPDATE_ALIAS, aliasReference(id));

	} // allowEdit

	/**
	 * Get a locked alias object for editing. Must commit() to make official, or cancel() (or remove()) when done!
	 * 
	 * @param id
	 *        The alias id string.
	 * @return An AliasEdit object for editing.
	 * @exception IdUnusedException
	 *            if not found.
	 * @exception PermissionException
	 *            if the current user does not have permission to mess with this alias.
	 * @exception InUseException
	 *            if the Alias object is locked by someone else.
	 */
	public AliasEdit edit(String id) throws IdUnusedException, PermissionException, InUseException
	{
		if (id == null) throw new IdUnusedException("null");

		// check security (throws if not permitted)
		unlock(SECURE_UPDATE_ALIAS, aliasReference(id));

		// check for existance
		if (!m_storage.check(id))
		{
			throw new IdUnusedException(id);
		}

		// ignore the cache - get the user with a lock from the info store
		AliasEdit a = m_storage.edit(id);
		if (a == null) throw new InUseException(id);

		((BaseAliasEdit) a).setEvent(SECURE_UPDATE_ALIAS);

		return a;

	} // edit

	/**
	 * Commit the changes made to a AliasEdit object, and release the lock. The AliasEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The AliasEdit object to commit.
	 */
	public void commit(AliasEdit edit)
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn("commit(): closed AliasEdit", e);
			}
			return;
		}

		// If we're doing an update just change the modification
		if (SECURE_UPDATE_ALIAS.equals(((BaseAliasEdit)edit).getEvent()))
		{
			addLiveUpdateProperties(edit.getPropertiesEdit());
		}
		else
		{
			addLiveProperties(edit.getPropertiesEdit());
		}

		// complete the edit
		m_storage.commit(edit);

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(((BaseAliasEdit) edit).getEvent(), edit.getReference(), true));

		// close the edit object
		((BaseAliasEdit) edit).closeEdit();

	} // commit

	/**
	 * Cancel the changes made to a AliasEdit object, and release the lock. The AliasEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The AliasEdit object to commit.
	 */
	public void cancel(AliasEdit edit)
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn("cancel(): closed AliasEdit", e);
			}
			return;
		}

		// release the edit lock
		m_storage.cancel(edit);

		// close the edit object
		((BaseAliasEdit) edit).closeEdit();

	} // cancel

	/**
	 * Remove this alias information - it must be a user with a lock from edit(). The AliasEdit is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The locked AliasEdit object to remove.
	 * @exception PermissionException
	 *            if the current user does not have permission to remove this alias.
	 */
	public void remove(AliasEdit edit) throws PermissionException
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn("remove(): closed AliasEdit", e);
			}
			return;
		}

		// check security (throws if not permitted)
		unlock(SECURE_REMOVE_ALIAS, edit.getReference());

		// complete the edit
		m_storage.remove(edit);

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(SECURE_REMOVE_ALIAS, edit.getReference(), true));

		// close the edit object
		((BaseAliasEdit) edit).closeEdit();

	} // remove

	/**********************************************************************************************************************************************************************************************************************************************************
	 * EntityProducer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "alias";
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
		// for preferences access
		if (reference.startsWith(REFERENCE_ROOT))
		{
			String id = null;

			// we will get null, service, userId
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			if (parts.length > 2)
			{
				id = parts[2];
			}

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
		// for alias access %%% ? what realm? -ggolden
		return null;
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
	 * Alias implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a new resource given just an id.
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
		return new BaseAliasEdit(id);
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
		return new BaseAliasEdit(element);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation (no container)
	 *********************************************************************************************************************************************************************************************************************************************************/

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
		return new BaseAliasEdit((BaseAliasEdit) other);
	}

	/**
	 * Construct a new resource given just an id.
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
		BaseAliasEdit e = new BaseAliasEdit(id);
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
		BaseAliasEdit e = new BaseAliasEdit(element);
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
		BaseAliasEdit e = new BaseAliasEdit((BaseAliasEdit) other);
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

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Open.
		 */
		public void open();

		/**
		 * Close.
		 */
		public void close();

		/**
		 * Check if an alias with this id exists.
		 *
		 * @param id
		 *        The alias id (case insensitive).
		 * @return true if an alias by this id exists, false if not.
		 */
		public boolean check(String id);

		/**
		 * Get the alias with this id, or null if not found.
		 *
		 * @param id
		 *        The alias id (case insensitive).
		 * @return The alias with this id, or null if not found.
		 */
		public AliasEdit get(String id);

		/**
		 * Get all the alias.
		 *
		 * @return The List (BaseAliasEdit) of all alias.
		 */
		public List getAll();

		/**
		 * Get all the alias in record range.
		 *
		 * @param first
		 *        The first record position to return.
		 * @param last
		 *        The last record position to return.
		 * @return The List (BaseAliasEdit) of all alias.
		 */
		public List getAll(int first, int last);

		/**
		 * Count all the aliases.
		 *
		 * @return The count of all aliases.
		 */
		public int count();

		/**
		 * Search for aliases with id or target matching criteria, in range.
		 *
		 * @param criteria
		 *        The search criteria.
		 * @param first
		 *        The first record position to return.
		 * @param last
		 *        The last record position to return.
		 * @return The List (BaseAliasEdit) of all alias.
		 */
		public List search(String criteria, int first, int last);

		/**
		 * Count all the aliases with id or target matching criteria.
		 *
		 * @param criteria
		 *        The search criteria.
		 * @return The count of all aliases with id or target matching criteria.
		 */
		public int countSearch(String criteria);

		/**
		 * Get all the alias that point at this target.
		 *
		 * @return The List (BaseAliasEdit) of all alias that point at this target
		 */
		public List getAll(String target);

		/**
		 * Get all the alias that point at this target, in record range.
		 *
		 * @param first
		 *        The first record position to return.
		 * @param last
		 *        The last record position to return.
		 * @return The List (BaseAliasEdit) of all alias that point at this target, in record range.
		 */
		public List getAll(String target, int first, int last);

		/**
		 * Add a new alias with this id.
		 *
		 * @param id
		 *        The alias id.
		 * @return The locked Alias object with this id, or null if the id is in use.
		 */
		public AliasEdit put(String id);

		/**
		 * Get a lock on the alias with this id, or null if a lock cannot be gotten.
		 *
		 * @param id
		 *        The alias id (case insensitive).
		 * @return The locked Alias with this id, or null if this records cannot be locked.
		 */
		public AliasEdit edit(String id);

		/**
		 * Commit the changes and release the lock.
		 *
		 * @param user
		 *        The alias to commit.
		 */
		public void commit(AliasEdit alias);

		/**
		 * Cancel the changes and release the lock.
		 *
		 * @param user
		 *        The alias to commit.
		 */
		public void cancel(AliasEdit alias);

		/**
		 * Remove this alias.
		 *
		 * @param user
		 *        The alias to remove.
		 */
		public void remove(AliasEdit alias);

		/**
		 * Read properties from storage into the edit's properties.
		 *
		 * @param edit
		 *        The user to read properties for.
		 */
		public void readProperties(AliasEdit edit, ResourcePropertiesEdit props);

	} // Storage

	/**
	 * <p>
	 * BaseAlias is an implementation of the CHEF Alias object.
	 * </p>
	 *
	 */
	public class BaseAliasEdit implements AliasEdit, SessionBindingListener
	{
		/** The alias id. */
		protected String m_id = null;

		/** The alias target. */
		protected String m_target = null;

		/** The properties. */
		protected ResourcePropertiesEdit m_properties = null;

		/** The created user id. */
		protected String m_createdUserId = null;

		/** The last modified user id. */
		protected String m_lastModifiedUserId = null;

		/** The time created. */
		protected Time m_createdTime = null;

		/** The time last modified. */
		protected Time m_lastModifiedTime = null;

		/** The event code for this edit. */
		protected String m_event = null;

		/** Active flag. */
		protected boolean m_active = false;

		/**
		 * Construct.
		 *
		 * @param id
		 *        The id.
		 */
		public BaseAliasEdit(String id)
		{
			m_id = id;

			// setup for properties
			ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
			m_properties = props;

			// if not a reconstruction, add properties
			if ((m_id != null) && (m_id.length() > 0)) addLiveProperties(props);

		} // BaseAlias

		/**
		 * ReConstruct.
		 *
		 * @param id
		 *        The id.
		 * @param target
		 *        The target.
		 * @param createdBy
		 *        The createdBy property.
		 * @param createdOn
		 *        The createdOn property.
		 * @param modifiedBy
		 *        The modified by property.
		 * @param modifiedOn
		 *        The modified on property.
		 */
		public BaseAliasEdit(String id, String target, String createdBy, Time createdOn, String modifiedBy, Time modifiedOn)
		{
			m_id = id;
			m_target = target;
			m_createdUserId = createdBy;
			m_lastModifiedUserId = modifiedBy;
			m_createdTime = createdOn;
			m_lastModifiedTime = modifiedOn;

			// setup for properties, but mark them lazy since we have not yet established them from data
			BaseResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
			props.setLazy(true);
			m_properties = props;

		} // BaseAlias

		/**
		 * Construct from another Alias object.
		 *
		 * @param alias
		 *        The alias object to use for values.
		 */
		public BaseAliasEdit(BaseAliasEdit alias)
		{
			setAll(alias);

		} // BaseAlias

		/**
		 * Construct from information in XML.
		 *
		 * @param el
		 *        The XML DOM Element definining the alias.
		 */
		public BaseAliasEdit(Element el)
		{
			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

			m_id = el.getAttribute("id");
			m_target = el.getAttribute("target");

			m_createdUserId = StringUtils.trimToNull(el.getAttribute("created-id"));
			m_lastModifiedUserId = StringUtils.trimToNull(el.getAttribute("modified-id"));

			String time = StringUtils.trimToNull(el.getAttribute("created-time"));
			if (time != null)
			{
				m_createdTime = timeService().newTimeGmt(time);
			}

			time = StringUtils.trimToNull(el.getAttribute("modified-time"));
			if (time != null)
			{
				m_lastModifiedTime = timeService().newTimeGmt(time);
			}

			// the children (properties)
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for properties
				if (element.getTagName().equals("properties"))
				{
					// re-create properties
					m_properties = new BaseResourcePropertiesEdit(element);

					// pull out some properties into fields to convert old (pre 1.18) versions
					if (m_createdUserId == null)
					{
						m_createdUserId = m_properties.getProperty("CHEF:creator");
					}
					if (m_lastModifiedUserId == null)
					{
						m_lastModifiedUserId = m_properties.getProperty("CHEF:modifiedby");
					}
					if (m_createdTime == null)
					{
						try
						{
							m_createdTime = m_properties.getTimeProperty("DAV:creationdate");
						}
						catch (Exception ignore)
						{
						}
					}
					if (m_lastModifiedTime == null)
					{
						try
						{
							m_lastModifiedTime = m_properties.getTimeProperty("DAV:getlastmodified");
						}
						catch (Exception ignore)
						{
						}
					}
					m_properties.removeProperty("CHEF:creator");
					m_properties.removeProperty("CHEF:modifiedby");
					m_properties.removeProperty("DAV:creationdate");
					m_properties.removeProperty("DAV:getlastmodified");
				}
			}

		} // BaseAlias

		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancel(this);
			}

		} // finalize

		/**
		 * Take all values from this object.
		 *
		 * @param alias
		 *        The alias object to take values from.
		 */
		protected void setAll(BaseAliasEdit alias)
		{
			m_id = alias.m_id;
			m_target = alias.m_target;
			m_createdUserId = ((BaseAliasEdit) alias).m_createdUserId;
			m_lastModifiedUserId = ((BaseAliasEdit) alias).m_lastModifiedUserId;
			if (((BaseAliasEdit) alias).m_createdTime != null)
				m_createdTime = (Time) ((BaseAliasEdit) alias).m_createdTime.clone();
			if (((BaseAliasEdit) alias).m_lastModifiedTime != null)
				m_lastModifiedTime = (Time) ((BaseAliasEdit) alias).m_lastModifiedTime.clone();

			m_properties = new BaseResourcePropertiesEdit();
			m_properties.addAll(alias.getProperties());
			((BaseResourcePropertiesEdit) m_properties).setLazy(((BaseResourceProperties) alias.getProperties()).isLazy());

		} // setAll

		/**
		 * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
		 *
		 * @param doc
		 *        The DOM doc to contain the XML (or null for a string return).
		 * @param stack
		 *        The DOM elements, the top of which is the containing element of the new "resource" element.
		 * @return The newly added element.
		 */
		public Element toXml(Document doc, Stack stack)
		{
			Element alias = doc.createElement("alias");

			if (stack.isEmpty())
			{
				doc.appendChild(alias);
			}
			else
			{
				((Element) stack.peek()).appendChild(alias);
			}

			stack.push(alias);

			alias.setAttribute("id", m_id);
			alias.setAttribute("target", m_target);
			alias.setAttribute("created-id", m_createdUserId);
			alias.setAttribute("modified-id", m_lastModifiedUserId);
			alias.setAttribute("created-time", m_createdTime.toString());
			alias.setAttribute("modified-time", m_lastModifiedTime.toString());

			// properties
			getProperties().toXml(doc, stack);

			stack.pop();

			return alias;

		} // toXml

		/**
		 * Access the alias id.
		 *
		 * @return The alias id string.
		 */
		public String getId()
		{
			return m_id;

		} // getId

		/**
		 * {@inheritDoc}
		 */
		public User getCreatedBy()
		{
			try
			{
				return userDirectoryService().getUser(m_createdUserId);
			}
			catch (Exception e)
			{
				return userDirectoryService().getAnonymousUser();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public User getModifiedBy()
		{
			try
			{
				return userDirectoryService().getUser(m_lastModifiedUserId);
			}
			catch (Exception e)
			{
				return userDirectoryService().getAnonymousUser();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public Time getCreatedTime()
		{
			return m_createdTime;
		}

		/**
		 * {@inheritDoc}
		 */
		public Time getModifiedTime()
		{
			return m_lastModifiedTime;
		}


		public Date getDateCreated() {
			return new Date(m_createdTime.getTime());
		}

		public Date getDateModified() {
			return new Date(m_lastModifiedTime.getTime());

		}


		/**
		 * Access the alias target.
		 *
		 * @return The alias target.
		 */
		public String getTarget()
		{
			return m_target;

		} // getTarget

		/**
		 * Set the alias target.
		 *
		 * @param target
		 *        The alias target.
		 */
		public void setTarget(String target)
		{
			m_target = target;

		} // setTarget

		/**
		 * Access the URL which can be used to access the resource.
		 *
		 * @return The URL which can be used to access the resource.
		 */
		public String getUrl()
		{
			return getAccessPoint(false) + m_id;

		} // getUrl

		/**
		 * Access the internal reference which can be used to access the resource from within the system.
		 *
		 * @return The the internal reference which can be used to access the resource from within the system.
		 */
		public String getReference()
		{
			return aliasReference(m_id);

		} // getReference

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/**
		 * Access the resources's properties.
		 *
		 * @return The resources's properties.
		 */
		public ResourceProperties getProperties()
		{
			// if lazy, resolve
			if (((BaseResourceProperties) m_properties).isLazy())
			{
				((BaseResourcePropertiesEdit) m_properties).setLazy(false);
				m_storage.readProperties(this, m_properties);
			}

			return m_properties;

		} // getProperties

		/**
		 * Are these objects equal? If they are both Alias objects, and they have matching id's, they are.
		 *
		 * @return true if they are equal, false if not.
		 */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof BaseAliasEdit)) return false;
			return ((BaseAliasEdit) obj).getId().equals(getId());

		} // equals

		/**
		 * Make a hash code that reflects the equals() logic as well. We want two objects, even if different instances, if they have the same id to hash the same.
		 */
		public int hashCode()
		{
			return getId().hashCode();

		} // hashCode

		/**
		 * Compare this object with the specified object for order.
		 *
		 * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
		 */
		public int compareTo(Object obj)
		{
			if (!(obj instanceof BaseAliasEdit)) throw new ClassCastException();

			// if the object are the same, say so
			if (obj == this) return 0;

			// sort based on (unique) id
			int compare = getId().compareTo(((BaseAliasEdit) obj).getId());

			return compare;

		} // compareTo

		/**
		 * Access the event code for this edit.
		 *
		 * @return The event code for this edit.
		 */
		protected String getEvent()
		{
			return m_event;
		}

		/**
		 * Set the event code for this edit.
		 *
		 * @param event
		 *        The event code for this edit.
		 */
		protected void setEvent(String event)
		{
			m_event = event;
		}

		/**
		 * Access the resource's properties for modification
		 *
		 * @return The resource's properties.
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			// if lazy, resolve
			if (((BaseResourceProperties) m_properties).isLazy())
			{
				((BaseResourcePropertiesEdit) m_properties).setLazy(false);
				m_storage.readProperties(this, m_properties);
			}

			return m_properties;

		} // getPropertiesEdit

		/**
		 * Enable editing.
		 */
		protected void activate()
		{
			m_active = true;

		} // activate

		/**
		 * Check to see if the edit is still active, or has already been closed.
		 *
		 * @return true if the edit is active, false if it's been closed.
		 */
		public boolean isActiveEdit()
		{
			return m_active;

		} // isActiveEdit

		/**
		 * Close the edit object - it cannot be used after this.
		 */
		protected void closeEdit()
		{
			m_active = false;

		} // closeEdit

		/**
		 * @return a description of the item this alias's target applies to.
		 */
		public String getDescription()
		{
			try
			{
				// the rest are references to some resource
				Reference ref = entityManager().newReference(getTarget());
				return ref.getDescription();
			}
			catch (Exception any)
			{
				return "unknown";
			}

		} // getDescription

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		public void valueBound(SessionBindingEvent event)
		{
		}

		public void valueUnbound(SessionBindingEvent event)
		{
			if (log.isDebugEnabled()) log.debug("valueUnbound()");

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancel(this);
			}

		} // valueUnbound


	} // BaseAliasEdit
} // BaseAliasService
