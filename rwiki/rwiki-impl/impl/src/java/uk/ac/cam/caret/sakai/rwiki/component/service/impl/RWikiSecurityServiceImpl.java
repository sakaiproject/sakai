/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

/**
 * @author andrew
 */

// FIXME: Component
@Slf4j
public class RWikiSecurityServiceImpl implements RWikiSecurityService
{

	private FunctionManager functionManager;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		functionManager = (FunctionManager) load(cm, FunctionManager.class
				.getName());

		entityManager = (EntityManager) load(cm, EntityManager.class.getName());
		securityService = (SecurityService) load(cm, SecurityService.class
				.getName());
		sessionManager = (SessionManager) load(cm, SessionManager.class
				.getName());
		siteService = (SiteService) load(cm, SiteService.class.getName());
		toolManager = (ToolManager) load(cm, ToolManager.class.getName());

		List l = functionManager.getRegisteredFunctions("rwiki."); //$NON-NLS-1$
		if (!l.contains(SECURE_READ))
			functionManager.registerFunction(SECURE_READ);
		if (!l.contains(SECURE_UPDATE))
			functionManager.registerFunction(SECURE_UPDATE);
		if (!l.contains(SECURE_CREATE))
			functionManager.registerFunction(SECURE_CREATE);
		if (!l.contains(SECURE_SUPER_ADMIN))
			functionManager.registerFunction(SECURE_SUPER_ADMIN);
		if (!l.contains(SECURE_ADMIN))
			functionManager.registerFunction(SECURE_ADMIN);
	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name); //$NON-NLS-1$
		}
		return o;
	}

	private SecurityService securityService;

	private SiteService siteService;

	private ToolManager toolManager;

	private EntityManager entityManager;

	private SessionManager sessionManager;

	/**
	 * {@inheritDoc}
	 * 
	 * @return
	 */
	public String getSiteReference()
	{
		try
		{
			Site currentSite = siteService.getSite(toolManager
					.getCurrentPlacement().getContext());
			return currentSite.getReference();
		}
		catch (IdUnusedException e)
		{
			throw new PermissionException(
					Messages.getString("RWikiSecurityServiceImpl.2")); //$NON-NLS-1$
		}
	}

	public String getSiteId()
	{
		return toolManager.getCurrentPlacement().getContext();
	}

	public boolean checkGetPermission(String reference)
	{
		return (securityService.unlock(SECURE_READ, reference));
	}

	public boolean checkUpdatePermission(String reference)
	{
		return (securityService.unlock(SECURE_UPDATE, reference));
	}

	public boolean checkAdminPermission(String reference)
	{
		return securityService.unlock(SECURE_ADMIN, reference);
	}

	public boolean checkSuperAdminPermission(String reference)
	{
		return securityService.unlock(SECURE_SUPER_ADMIN, reference);
	}

	public boolean checkCreatePermission(String reference)
	{
		return securityService.unlock(SECURE_CREATE, reference);
	}

	public boolean checkSearchPermission(String reference)
	{
		return securityService.unlock(SECURE_READ, reference);
	}

	

	public String createPermissionsReference(String pageSpace)
	{
		// Page space is assumed to be a ppage space reference
		// Turn into an entity and then get a reference
		Reference ref = entityManager
				.newReference(RWikiObjectService.REFERENCE_ROOT + pageSpace
						+ "."); //$NON-NLS-1$
		return ref.getReference();
	}

	public boolean checkRead(RWikiEntity rwe)
	{
		RWikiObject rwo = rwe.getRWikiObject();
		String progress = ""; //$NON-NLS-1$
		long start = System.currentTimeMillis();
		try
		{

			String user = sessionManager.getCurrentSessionUserId();

			if (log.isDebugEnabled())
			{
				log.debug("checkRead for " + rwo.getName() + " by user: " //$NON-NLS-1$ //$NON-NLS-2$
						+ user);
			}

			if (user != null && user.equals(rwo.getOwner())
					&& (rwo.getOwnerRead() || rwo.getOwnerAdmin()))
			{
				if (log.isDebugEnabled())
				{
					log.debug("User is owner and allowed to read"); //$NON-NLS-1$
				}
				progress = progress + "1"; //$NON-NLS-1$
				return true;
			}

			String permissionsReference = rwe.getReference();
			if ((rwo.getGroupRead() && checkGetPermission(permissionsReference))
					|| (rwo.getGroupWrite() && checkUpdatePermission(permissionsReference))
					|| (rwo.getGroupAdmin())
					&& checkAdminPermission(permissionsReference))
			{
				if (log.isDebugEnabled())
				{
					log.debug("User is in group and allowed to read"); //$NON-NLS-1$
				}
				progress = progress + "2"; //$NON-NLS-1$
				return true;
			}

			if (rwo.getPublicRead())
			{
				if (log.isDebugEnabled())
				{
					log.debug("Object is public read"); //$NON-NLS-1$
				}
				progress = progress + "3"; //$NON-NLS-1$
				return true;
			}

			if (checkSuperAdminPermission(permissionsReference))
			{
				if (log.isDebugEnabled())
				{
					log
							.debug("User is SuperAdmin for Realm thus default allowed to update"); //$NON-NLS-1$
				}
				progress = progress + "4"; //$NON-NLS-1$
				return true;
			}

			if (log.isDebugEnabled())
			{
				log.debug("Permission denied to read " + rwo.getName() //$NON-NLS-1$
						+ " by user: " + user); //$NON-NLS-1$
			}
			progress = progress + "5"; //$NON-NLS-1$
			return false;
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("canRead: " + progress, start, finish); //$NON-NLS-1$
		}
	}

	public boolean checkUpdate(RWikiEntity rwe)
	{
		String user = sessionManager.getCurrentSessionUserId();
		RWikiObject rwo = rwe.getRWikiObject();
		if (log.isDebugEnabled())
		{
			log.debug("checkUpdate for " + rwo.getName() + " by user: " + user); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (user != null && user.equals(rwo.getOwner())
				&& (rwo.getOwnerWrite() || rwo.getOwnerAdmin()))
		{
			if (log.isDebugEnabled())
			{
				log.debug("User is owner and allowed to update"); //$NON-NLS-1$
			}
			return true;
		}

		String permissionsReference = rwe.getReference();
		if ((rwo.getGroupWrite() && checkUpdatePermission(permissionsReference))
				|| (rwo.getGroupAdmin())
				&& checkAdminPermission(permissionsReference))
		{
			if (log.isDebugEnabled())
			{
				log.debug("User is in group and allowed to update"); //$NON-NLS-1$
			}
			return true;
		}

		if (rwo.getPublicWrite())
		{
			if (log.isDebugEnabled())
			{
				log.debug("Object is public write"); //$NON-NLS-1$
			}
			return true;
		}

		if (checkSuperAdminPermission(permissionsReference))
		{
			if (log.isDebugEnabled())
			{
				log
						.debug("User is SuperAdmin for Realm thus default allowed to update"); //$NON-NLS-1$
			}
			return true;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Permission denied to update " + rwo.getName() //$NON-NLS-1$
					+ " by user: " + user); //$NON-NLS-1$
		}
		return false;
	}

	public boolean checkCreate(RWikiEntity rwe)
	{
		String user = sessionManager.getCurrentSessionUserId();
		RWikiObject rwo = rwe.getRWikiObject();
		if (log.isDebugEnabled())
		{
			log.debug("checkCreate for " + rwo.getName() + " by user: " + user); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (user != null && user.equals(rwo.getOwner())
				&& (rwo.getOwnerWrite() || rwo.getOwnerAdmin()))
		{
			if (log.isDebugEnabled())
			{
				log.debug("User is owner and allowed to create"); //$NON-NLS-1$
			}
			return true;
		}

		String permissionsReference = rwe.getReference();
		if ((rwo.getGroupWrite() && checkCreatePermission(permissionsReference))
				|| (rwo.getGroupAdmin())
				&& checkAdminPermission(permissionsReference))
		{
			if (log.isDebugEnabled())
			{
				log.debug("User is in group and allowed to create"); //$NON-NLS-1$
			}
			return true;
		}

		if (rwo.getPublicWrite())
		{
			if (log.isDebugEnabled())
			{
				log.debug("Object is public write"); //$NON-NLS-1$
			}
			return true;
		}

		if (checkSuperAdminPermission(permissionsReference))
		{
			if (log.isDebugEnabled())
			{
				log
						.debug("User is SuperAdmin for Realm thus default allowed to create"); //$NON-NLS-1$
			}
			return true;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Permission denied to create " + rwo.getName() //$NON-NLS-1$
					+ " by user: " + user); //$NON-NLS-1$
		}
		return false;
	}

	public boolean checkAdmin(RWikiEntity rwe)
	{
		String user = sessionManager.getCurrentSessionUserId();
		RWikiObject rwo = rwe.getRWikiObject();

		if (log.isDebugEnabled())
		{
			log.debug("checkAdmin for " + rwo.getName() + " by user: " + user); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (user != null && user.equals(rwo.getOwner()) && rwo.getOwnerAdmin())
		{
			if (log.isDebugEnabled())
			{
				log.debug("User is owner and allowed to admin"); //$NON-NLS-1$
			}
			return true;
		}

		String permissionsReference = rwe.getReference();
		if (rwo.getGroupAdmin() && checkAdminPermission(permissionsReference))
		{
			if (log.isDebugEnabled())
			{
				log.debug("User is in group and allowed to admin"); //$NON-NLS-1$
			}
			return true;
		}

		if (checkSuperAdminPermission(permissionsReference))
		{
			if (log.isDebugEnabled())
			{
				log
						.debug("User is Super Admin for Realm thus default allowed to admin"); //$NON-NLS-1$
			}
			return true;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Permission denied to admin " + rwo.getName() //$NON-NLS-1$
					+ " by user: " + user); //$NON-NLS-1$
		}
		return false;
	}

	
}
