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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.jcr.api.JCRSecurityConverter;
import org.sakaiproject.jcr.api.JCRSecurityServiceAdapter;

/**
 * @author ieb
 */
public class JCRSecurityServiceAdapterImpl implements JCRSecurityServiceAdapter
{

	private static final Log log = LogFactory.getLog(JCRSecurityServiceAdapterImpl.class);

	private SecurityService securityService;

	private List<JCRSecurityConverter> securityConverters = new ArrayList<JCRSecurityConverter>();

	private String privateWorkspaces = "none";

	private FunctionManager functionManager;

	private boolean enabled = false;

	private ServerConfigurationService serverConfigurationService;

	@SuppressWarnings("unchecked")
	public void init()
	{
		enabled = serverConfigurationService.getBoolean("jcr.experimental",false);
		if ( !enabled  ) return;

		boolean error = false;
		try
		{

			if (securityService == null)
			{
				log.error("securityService has not been set ");
				error = true;
			}
			if (functionManager == null)
			{
				log.error("functionManager has not been set");
			}
			else
			{

				List<String> l = functionManager.getRegisteredFunctions("jcr."); //$NON-NLS-1$
				if (!l.contains(JCR_ADD))
				{
					functionManager.registerFunction(JCR_ADD);
				}
				if (!l.contains(JCR_GET))
				{
					functionManager.registerFunction(JCR_GET);
				}
				if (!l.contains(JCR_REMOVE))
				{
					functionManager.registerFunction(JCR_REMOVE);
				}
				if (!l.contains(JCR_UPDATE))
				{
					functionManager.registerFunction(JCR_UPDATE);
				}
			}
		}
		catch (Throwable t)
		{
			log.warn("Failed init(): ", t);
			error = true;
		}
		finally
		{
			if (error)
			{
				throw new RuntimeException(
						"Fatal error initialising JCRSecurityServiceAdapter... (see previous logged ERROR for details)");
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.jcr.api.internal.JCRAuthZGroupsAdapter#allowAdd(java.lang.String)
	 */
	public boolean allowAdd(String userId, String internalPath)
	{
		return allow(userId, JCR_ADD, internalPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.jcr.api.internal.JCRAuthZGroupsAdapter#allowGet(java.lang.String)
	 */
	public boolean allowGet(String userId, String internalPath)
	{
		return allow(userId, JCR_GET, internalPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.jcr.api.internal.JCRAuthZGroupsAdapter#allowRemove(java.lang.String)
	 */
	public boolean allowRemove(String userId, String internalPath)
	{
		return allow(userId, JCR_REMOVE, internalPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.jcr.api.internal.JCRAuthZGroupsAdapter#allowUpdate(java.lang.String)
	 */
	public boolean allowUpdate(String userId, String internalPath)
	{
		return allow(userId, JCR_UPDATE, internalPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.jcr.api.internal.JCRAuthZGroupsAdapter#beginSecureCalls()
	 */
	public void beginSecureCalls()
	{
		securityService.pushAdvisor(new SecurityAdvisor()
		{

			public SecurityAdvice isAllowed(String userId, String function,
					String reference)
			{
				// probably need to filter to limit to questions that we known
				// about
				return SecurityAdvice.ALLOWED;
			}

		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.jcr.api.internal.JCRAuthZGroupsAdapter#endSecureStackCalls()
	 */
	public void endSecureStackCalls()
	{
		securityService.popAdvisor();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.jcr.api.internal.JCRAuthZGroupsAdapter#getSakaiRealm(java.lang.String)
	 */
	public String getSakaiRealm(String internalPath)
	{
		for (Iterator<JCRSecurityConverter> i = securityConverters.iterator(); i
				.hasNext();)
		{
			String convertedRealm = i.next().convertRealm(internalPath);
			if (convertedRealm != null)
			{
				return convertedRealm;
			}
		}
		return internalPath;
	}

	/**
	 * @param jcr_add2
	 * @param internalPath
	 * @return
	 */
	private boolean allow(String userId, String lock, String internalPath)
	{
		if (securityService.isSuperUser(userId))
		{
			return true;
		}
		String reference = getSakaiRealm(internalPath);
		String getLock = getSakaiLock(lock, internalPath);
		if ( log.isDebugEnabled()) {
			log.info("Doing Unlock " + lock + "(" + reference + ")");
		}
		return securityService.unlock(userId, getLock, reference);
	}

	/**
	 * @param lock
	 * @param internalPath
	 * @return
	 */
	private String getSakaiLock(String lock, String internalPath)
	{
		for (Iterator<JCRSecurityConverter> i = securityConverters.iterator(); i
				.hasNext();)
		{
			String convertedLock = i.next().convertLock(lock, internalPath);
			if (convertedLock != null)
			{
				return convertedLock;
			}
		}
		return lock;
	}

	/**
	 * @return the securityService
	 */
	public SecurityService getSecurityService()
	{
		return securityService;
	}

	/**
	 * @param securityService
	 *        the securityService to set
	 */
	public void setSecurityService(SecurityService securityService)
	{
		this.securityService = securityService;
	}

	public void addSecurityConverter(JCRSecurityConverter o)
	{
		securityConverters.add(o);
	}

	public void removeSecurityConverter(JCRSecurityConverter o)
	{
		securityConverters.remove(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.jcr.api.JCRSecurityServiceAdapter#canAccessWorkspace(java.lang.String)
	 */
	public boolean canAccessWorkspace(String userId, String workspace)
	{
		if (securityService.isSuperUser(userId))
		{
			return true;
		}
		if ((privateWorkspaces.indexOf(workspace) >= 0))
		{
			return false;
		}
		return true;
	}

	/**
	 * @return the privateWorkspaces
	 */
	public String getPrivateWorkspaces()
	{
		return privateWorkspaces;
	}

	/**
	 * @param privateWorkspaces
	 *        the privateWorkspaces to set
	 */
	public void setPrivateWorkspaces(String privateWorkspaces)
	{
		this.privateWorkspaces = privateWorkspaces;
	}

	/**
	 * @return the functionManager
	 */
	public FunctionManager getFunctionManager()
	{
		return functionManager;
	}

	/**
	 * @param functionManager
	 *        the functionManager to set
	 */
	public void setFunctionManager(FunctionManager functionManager)
	{
		this.functionManager = functionManager;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	/**
	 * @param serverConfigurationService the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

}
