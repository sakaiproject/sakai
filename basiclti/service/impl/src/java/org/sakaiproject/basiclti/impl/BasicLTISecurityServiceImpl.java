/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 Charles Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.basiclti.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.basiclti.SakaiBLTIUtil;
import org.imsglobal.basiclti.BasicLTIUtil;

public class BasicLTISecurityServiceImpl implements EntityProducer {

	private static ResourceLoader rb = new ResourceLoader("basicltisvc");

	public static final String MIME_TYPE_BLTI="ims/basiclti";
	public static final String REFERENCE_ROOT="/basiclti";
	public static final String APPLICATION_ID = "sakai:basiclti";

	// Note: security needs a proper Resource reference

	/*******************************************************************************
	* Dependencies and their setter methods
	*******************************************************************************/

	/** Dependency: a logger component. */
	private Log logger = LogFactory.getLog(BasicLTISecurityServiceImpl.class);
	private ThreadLocalManager threadLocalManager = org.sakaiproject.thread_local.cover.ThreadLocalManager.getInstance();

	/**
	 * Check security for this entity.
	 *
	 * @param ref
	 *        The Reference to the entity.
	 * @return true if allowed, false if not.
	 */
	protected boolean checkSecurity(Reference ref)
	{
		String contextId = ref.getContext();
		try
		{
			Site site = SiteService.getSiteVisit(contextId);
			if ( site != null ) return true;
		}
		catch(IdUnusedException ex)
		{
			return false;
		}
		catch(PermissionException ex)
		{
			return false;
		}
		// System.out.println("ID="+ref.getId());
		// System.out.println("Type="+ref.getType());
		// System.out.println("SubType="+ref.getSubType());

		return false;
	}
	/*******************************************************************************
	* Init and Destroy
	*******************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{


		logger.info(this +".init()");

		try
		{
			// register as an entity producer
			EntityManager.registerEntityProducer(this,REFERENCE_ROOT);
		}
		catch (Throwable t)
		{
			logger.warn("init(): ", t);
		}
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		logger.info(this +".destroy()");
	}

	/**
	 *
	 */
	public BasicLTISecurityServiceImpl() {
		super();

	}


	public boolean isSuperUser(String userId)
	{
		return SecurityService.isSuperUser(userId);
	}

	/**
	 * @return siteId
	 */
	private String getContextSiteId(String reference) {
                  // TODO: Fix this - Chuck
		  return ("/site/" + reference);
	}

		/*******************************************************************************************************************************
		 * EntityProducer
		 ******************************************************************************************************************************/

		/**
		 * {@inheritDoc}
                   /access/basiclti/site/12-siteid-456/98-placement-id
                   /access/basiclti/content/ --- content path ---- (Future)
		 */
		public boolean parseEntityReference(String reference, Reference ref)
		{
			if (reference.startsWith(REFERENCE_ROOT))
			{
				// we will get null, simplelti, site, <context>, <placement>
				// we will store the context, and the ContentHosting reference in our id field.
				String id = null;
				String context = null;
				String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

				if ( parts.length == 5 && parts[2].equals("site") )
				{
					context = parts[3];
					id = parts[4];
					//Should the slashes below be entityseparator
					// id = "/" + StringUtil.unsplit(parts, 2, parts.length - 2, "/");
				}

				ref.set(APPLICATION_ID, "site", id, null, context);

				return true;
			}

			return false;
		}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
					Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
					EntityAccessOverloadException, EntityCopyrightException
			{
				// decide on security
				if (!checkSecurity(ref))
				{
					throw new EntityPermissionException(SessionManager.getCurrentSessionUserId(), "basiclti", ref.getReference());
				}

				// Get the post data for the placement
				String postData = SakaiBLTIUtil.postLaunchHTML(ref.getId(), rb);
				// postData = "\n<pre>\n"+postData.replace("<","&lt;").replace(">","&gt;")+"\n</pre>\n";

				try
				{
					res.setContentType("text/html");
					ServletOutputStream out = res.getOutputStream();
					out.println(postData);
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}

			}
		};
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
		// Since we handle security ourself, we won't support anyone else asking
		return null;
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
	public String getEntityUrl(Reference ref)
	{
		return ServerConfigurationService.getAccessUrl() + ref.getReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "basiclti";
	}

        public boolean willArchiveMerge()
        {
                return false;
        }

        public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
                        Set userListAllowImport)
        {
		return null;
        }

        public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
        {
		return null;
        }

}
