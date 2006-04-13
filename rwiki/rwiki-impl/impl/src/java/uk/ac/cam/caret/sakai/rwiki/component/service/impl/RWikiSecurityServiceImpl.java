/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.cover.SessionManager;

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
public class RWikiSecurityServiceImpl implements RWikiSecurityService
{
	private static Log log = LogFactory.getLog(RWikiSecurityServiceImpl.class);

	public void init()
	{
		List l = FunctionManager.getRegisteredFunctions("rwiki.");
		if (!l.contains(SECURE_READ))
			FunctionManager.registerFunction(SECURE_READ);
		if (!l.contains(SECURE_UPDATE))
			FunctionManager.registerFunction(SECURE_UPDATE);
		if (!l.contains(SECURE_CREATE))
			FunctionManager.registerFunction(SECURE_CREATE);
		if (!l.contains(SECURE_SUPER_ADMIN))
			FunctionManager.registerFunction(SECURE_SUPER_ADMIN);
		if (!l.contains(SECURE_ADMIN))
			FunctionManager.registerFunction(SECURE_ADMIN);
	}

	private PortalService portalService;

	private SecurityService securityService;

	private SiteService siteService;

	/**
	 * {@inheritDoc}
	 * 
	 * @return
	 */
	public String getSiteReference()
	{
		try
		{
			Site currentSite = siteService.getSite(portalService
					.getCurrentSiteId());
			return currentSite.getReference();
		}
		catch (IdUnusedException e)
		{
			throw new PermissionException(
					"You must access the RWiki through a proper site");
		}
	}

	public String getSiteId()
	{
		return portalService.getCurrentSiteId();
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

	public PortalService getPortalService()
	{
		return portalService;
	}

	public void setPortalService(PortalService portalService)
	{
		this.portalService = portalService;
	}

	public SecurityService getSecurityService()
	{
		return securityService;
	}

	public void setSecurityService(SecurityService securityService)
	{
		this.securityService = securityService;
	}

	public SiteService getSiteService()
	{
		return siteService;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public String createPermissionsReference(String pageSpace)
	{
		// Page space is assumed to be a ppage space reference
		// Turn into an entity and then get a reference
		Reference ref = EntityManager
				.newReference(RWikiObjectService.REFERENCE_ROOT + pageSpace
						+ ".");
		return ref.getReference();
	}

	public boolean checkRead(RWikiEntity rwe)
	{
		RWikiObject rwo = rwe.getRWikiObject();
		String progress = "";
		long start = System.currentTimeMillis();
		try
		{

			String user = SessionManager.getCurrentSessionUserId();

			if (log.isDebugEnabled())
			{
				log.debug("checkRead for " + rwo.getName() + " by user: "
						+ user);
			}

			if (user != null && user.equals(rwo.getOwner())
					&& (rwo.getOwnerRead() || rwo.getOwnerAdmin()))
			{
				if (log.isDebugEnabled())
				{
					log.debug("User is owner and allowed to read");
				}
				progress = progress + "1";
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
					log.debug("User is in group and allowed to read");
				}
				progress = progress + "2";
				return true;
			}

			if (rwo.getPublicRead())
			{
				if (log.isDebugEnabled())
				{
					log.debug("Object is public read");
				}
				progress = progress + "3";
				return true;
			}

			if (checkSuperAdminPermission(permissionsReference))
			{
				if (log.isDebugEnabled())
				{
					log
							.debug("User is SuperAdmin for Realm thus default allowed to update");
				}
				progress = progress + "4";
				return true;
			}

			if (log.isDebugEnabled())
			{
				log.debug("Permission denied to read " + rwo.getName()
						+ " by user: " + user);
			}
			progress = progress + "5";
			return false;
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("canRead: " + progress, start, finish);
		}
	}

	public boolean checkUpdate(RWikiEntity rwe)
	{
		String user = SessionManager.getCurrentSessionUserId();
		RWikiObject rwo = rwe.getRWikiObject();
		if (log.isDebugEnabled())
		{
			log.debug("checkUpdate for " + rwo.getName() + " by user: " + user);
		}
		if (user != null && user.equals(rwo.getOwner())
				&& (rwo.getOwnerWrite() || rwo.getOwnerAdmin()))
		{
			if (log.isDebugEnabled())
			{
				log.debug("User is owner and allowed to update");
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
				log.debug("User is in group and allowed to update");
			}
			return true;
		}

		if (rwo.getPublicWrite())
		{
			if (log.isDebugEnabled())
			{
				log.debug("Object is public write");
			}
			return true;
		}

		if (checkSuperAdminPermission(permissionsReference))
		{
			if (log.isDebugEnabled())
			{
				log
						.debug("User is SuperAdmin for Realm thus default allowed to update");
			}
			return true;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Permission denied to update " + rwo.getName()
					+ " by user: " + user);
		}
		return false;
	}

	public boolean checkAdmin(RWikiEntity rwe)
	{
		String user = SessionManager.getCurrentSessionUserId();
		RWikiObject rwo = rwe.getRWikiObject();

		if (log.isDebugEnabled())
		{
			log.debug("checkAdmin for " + rwo.getName() + " by user: " + user);
		}
		if (user != null && user.equals(rwo.getOwner()) && rwo.getOwnerAdmin())
		{
			if (log.isDebugEnabled())
			{
				log.debug("User is owner and allowed to admin");
			}
			return true;
		}

		String permissionsReference = rwe.getReference();
		if (rwo.getGroupAdmin() && checkAdminPermission(permissionsReference))
		{
			if (log.isDebugEnabled())
			{
				log.debug("User is in group and allowed to admin");
			}
			return true;
		}

		if (checkSuperAdminPermission(permissionsReference))
		{
			if (log.isDebugEnabled())
			{
				log
						.debug("User is Super Admin for Realm thus default allowed to admin");
			}
			return true;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Permission denied to admin " + rwo.getName()
					+ " by user: " + user);
		}
		return false;
	}

}
