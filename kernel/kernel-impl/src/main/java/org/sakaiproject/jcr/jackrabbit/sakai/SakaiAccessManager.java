/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.jackrabbit.sakai;

import java.security.Principal;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.ItemId;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.DefaultNamePathResolver;
import org.apache.jackrabbit.spi.commons.conversion.MalformedPathException;
import org.apache.jackrabbit.spi.commons.namespace.NamespaceResolver;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.jcr.api.JCRSecurityServiceAdapter;
import org.sakaiproject.jcr.api.internal.SakaiUserPrincipal;
import org.sakaiproject.jcr.jackrabbit.JCRAnonymousPrincipal;
import org.sakaiproject.jcr.jackrabbit.JCRSystemPrincipal;

/**
 * @author ieb
 */
public class SakaiAccessManager implements AccessManager
{
	private static final Log log = LogFactory.getLog(SakaiAccessManager.class);

	private Subject subject;

	private HierarchyManager hierMgr;

	protected boolean anonymous = false;

	private boolean initialized = false;

	protected boolean sakaisystem = false;

	private JCRSecurityServiceAdapter jCRSecurityServiceAdapter;

	private NamespaceResolver resolver;

	protected String sakaiUserId = ""; // a blank userId is the anon user

	private DefaultNamePathResolver pathResolver;

	public SakaiAccessManager()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jackrabbit.core.security.AccessManager#init(org.apache.jackrabbit.core.security.AMContext)
	 */
	public void init(AMContext context) throws AccessDeniedException, Exception
	{
		if (initialized)
		{
			throw new IllegalStateException("already initialized");
		}

		subject = context.getSubject();
		hierMgr = context.getHierarchyManager();
		resolver = context.getNamespaceResolver();
		pathResolver = new DefaultNamePathResolver(resolver,true);



		anonymous = !subject.getPrincipals(JCRAnonymousPrincipal.class).isEmpty();
		if (!anonymous)
		{
			sakaisystem = !subject.getPrincipals(JCRSystemPrincipal.class).isEmpty();

			Set principals = subject.getPrincipals(SakaiUserPrincipal.class);
			if (principals.size() == 0)
			{
				if (log.isDebugEnabled())
					log.debug("No SakaiUserPrincipal found for context: " + context);
			}
			else if (principals.size() == 1)
			{
				Principal p = (Principal) principals.iterator().next();
				if (p instanceof SakaiUserPrincipal)
				{
					SakaiUserPrincipal sp = (SakaiUserPrincipal) p;
					sakaiUserId = sp.getName();
				}
			}
			else
			{

				for (Iterator i = principals.iterator(); i.hasNext();)
				{
					Principal p = (Principal) i.next();
					if (p instanceof SakaiUserPrincipal)
					{
						SakaiUserPrincipal sp = (SakaiUserPrincipal) p;
						sakaiUserId = sp.getName();
					}
				}
			}
		}

		// TODO check permission to access given workspace based on principals
		initialized = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jackrabbit.core.security.AccessManager#close()
	 */
	public synchronized void close() throws Exception
	{
		if (!initialized)
		{
			throw new IllegalStateException("not initialized");
		}

		initialized = false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jackrabbit.core.security.AccessManager#checkPermission(org.apache.jackrabbit.core.ItemId,
	 *      int)
	 */
	public void checkPermission(ItemId item, int permission)
			throws AccessDeniedException, ItemNotFoundException, RepositoryException
	{
		if (jCRSecurityServiceAdapter == null)
		{
			jCRSecurityServiceAdapter = getJCRSecurityServiceAdapter();
			if (jCRSecurityServiceAdapter == null)
			{
				log.fatal("No JCR to SecurityService Adapter can be found, repository is not available ");
				throw new RepositoryException("No JCR Sakai Adapter Defined");
			}
		}
		try
		{
			if (item != null)
			{

				Path path = hierMgr.getPath(item).getNormalizedPath();
				
				
				String jcrPath = pathResolver.getJCRPath(path);

				if ((AccessManager.WRITE & permission) == AccessManager.WRITE)
				{

					if (!jCRSecurityServiceAdapter.allowUpdate(sakaiUserId, jcrPath)
							&& !jCRSecurityServiceAdapter.allowAdd(sakaiUserId, jcrPath))
					{
						String sakaiRealm = jCRSecurityServiceAdapter
								.getSakaiRealm(jcrPath);
						log.debug("Write Permission denied on " + jcrPath + ":"
								+ sakaiRealm);
						throw new AccessDeniedException("Write Permission denied on "
								+ jcrPath + ":" + sakaiRealm);
					}
				}
				if ((AccessManager.REMOVE & permission) == AccessManager.REMOVE)
				{
					if (!jCRSecurityServiceAdapter.allowRemove(sakaiUserId, jcrPath))
					{
						String sakaiRealm = jCRSecurityServiceAdapter
								.getSakaiRealm(jcrPath);
						log.debug("Remove Permission denined on " + jcrPath + ":"
								+ sakaiRealm);
						throw new AccessDeniedException("Remove Permission denined on "
								+ jcrPath + ":" + sakaiRealm);
					}
				}
				if ((AccessManager.READ & permission) == AccessManager.READ)
				{
					if (!jCRSecurityServiceAdapter.allowGet(sakaiUserId, jcrPath))
					{
						String sakaiRealm = jCRSecurityServiceAdapter
								.getSakaiRealm(jcrPath);
						log.debug("Read Permission denined on " + jcrPath + ":"
								+ sakaiRealm);
						throw new AccessDeniedException("Read Permission denined on "
								+ jcrPath + ":" + sakaiRealm);
					}
				}
			}
			else
			{
				throw new AccessDeniedException("Permission ("+permission+") denied on null item");
			}
		}
		catch (MalformedPathException mfpe)
		{
			throw new AccessDeniedException("Invalid path ", mfpe);
		}

		if ( log.isDebugEnabled() ) {
			log.debug("Permission [" + permission + "]Granted on [" + item + "]");
		}
	}

	/**
	 * @return
	 */
	protected JCRSecurityServiceAdapter getJCRSecurityServiceAdapter()
	{
		return (JCRSecurityServiceAdapter) ComponentManager
				.get(JCRSecurityServiceAdapter.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jackrabbit.core.security.AccessManager#isGranted(org.apache.jackrabbit.core.ItemId,
	 *      int)
	 */
	public boolean isGranted(ItemId item, int permission) throws ItemNotFoundException,
			RepositoryException
	{
		if (sakaisystem)
		{
			return true;
		}
		try
		{
			checkPermission(item, permission);
			log.debug(" Is granted " + item + " perm " + permission);
			return true;
		}
		catch (AccessDeniedException ex)
		{
			log.debug(" Is Denied " + item + " perm " + permission);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jackrabbit.core.security.AccessManager#canAccess(java.lang.String)
	 */
	public boolean canAccess(String workspace) throws NoSuchWorkspaceException,
			RepositoryException
	{
		if (jCRSecurityServiceAdapter == null)
		{
			jCRSecurityServiceAdapter = getJCRSecurityServiceAdapter();
			if (jCRSecurityServiceAdapter == null)
			{
				log
						.fatal("No JCR to SecurityService Adapter can be found, repository is not available ");
				throw new RepositoryException("No JCR Sakai Adapter Defined");
			}
		}
		return jCRSecurityServiceAdapter.canAccessWorkspace(sakaiUserId, workspace);

	}

}
