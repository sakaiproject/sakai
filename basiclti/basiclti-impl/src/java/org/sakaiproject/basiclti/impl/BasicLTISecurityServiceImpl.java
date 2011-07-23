/**
 * $URL$
 * $Id$
 * 
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.basiclti.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.SecurityService;
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
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.lti.api.LTIService;
//import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.component.cover.ComponentManager;

import org.sakaiproject.util.foorm.SakaiFoorm;

import org.sakaiproject.basiclti.LocalEventTrackingService;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;


@SuppressWarnings("deprecation")
public class BasicLTISecurityServiceImpl implements EntityProducer {

	private static ResourceLoader rb = new ResourceLoader("basicltisvc");

	public static final String MIME_TYPE_BLTI="ims/basiclti";
	public static final String REFERENCE_ROOT="/basiclti";
	public static final String APPLICATION_ID = "sakai:basiclti";
	public static final String EVENT_BASICLTI_LAUNCH = "basiclti.launch";

	protected static SakaiFoorm foorm = new SakaiFoorm();

	// Note: security needs a proper Resource reference

	/*******************************************************************************
	* Dependencies and their setter methods
	*******************************************************************************/

	/** Dependency: a logger component. */
	private Log logger = LogFactory.getLog(BasicLTISecurityServiceImpl.class);

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
	/** A service */
	protected static LTIService ltiService = null; 

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
                if ( ltiService == null ) ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
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
			@SuppressWarnings("unchecked")
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
					Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
					EntityAccessOverloadException, EntityCopyrightException
			{
				// decide on security
				if (!checkSecurity(ref))
				{
					throw new EntityPermissionException(SessionManager.getCurrentSessionUserId(), "basiclti", ref.getReference());
				}

				String refId = ref.getId();
				String [] retval = null;
				if ( refId.startsWith("content:") && refId.length() > 8 ) 
				{
					Map<String,Object> content = null;
					Map<String,Object> tool = null;

					String contentStr = refId.substring(8);
					Long contentKey = foorm.getLongKey(contentStr);
					if ( contentKey >= 0 )
					{
						content = ltiService.getContentNoAuthz(contentKey);
						if ( content != null ) 
						{
							String siteId = (String) content.get("SITE_ID");
							if ( siteId == null || ! siteId.equals(ref.getContext()) )  
							{
								content = null;
							}
						}
						if ( content != null ) 
						{
							Long toolKey = foorm.getLongKey(content.get("tool_id"));
							if ( toolKey > 0 ) tool = ltiService.getToolNoAuthz(toolKey);
							if ( tool != null ) 
							{
								// SITE_ID can be null for the tool
								String siteId = (String) tool.get("SITE_ID");
								if ( siteId != null && ! siteId.equals(ref.getContext()) ) 
								{
									tool = null;
								}
							}
						}

						// Adjust the content items based on the tool items
						if ( tool != null || content != null ) 
						{
							ltiService.filterContent(content, tool);
						}
					}
					retval = SakaiBLTIUtil.postLaunchHTML(content, tool, rb);
				}
				else
				{
					// Get the post data for the placement
					retval = SakaiBLTIUtil.postLaunchHTML(refId, rb);
				}

				try
				{
					res.setContentType("text/html; charset=UTF-8");
					res.setCharacterEncoding("utf-8");
					res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
					res.addDateHeader("Last-Modified", System.currentTimeMillis());
					res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
					res.addHeader("Pragma", "no-cache");
					ServletOutputStream out = res.getOutputStream();

					out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
					out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
					out.println("<html>\n<head>");
					out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
					out.println("</head>\n<body>");
					out.println(retval[0]);
					out.println("</body>\n</html>");
					String refstring = ref.getReference();
					if ( retval.length > 1 ) refstring = retval[1];
					// Cool 2.6 Event call
                                        Event event = LocalEventTrackingService.newEvent(EVENT_BASICLTI_LAUNCH, refstring, ref.getContext(),  false, NotificationService.NOTI_OPTIONAL);
					// 2.5 Event call
                                        // Event event = EventTrackingService.newEvent(EVENT_BASICLTI_LAUNCH, refstring, false);
                                        LocalEventTrackingService.post(event);

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
	public Collection<String> getEntityAuthzGroups(Reference ref, String userId)
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

        @SuppressWarnings("unchecked")
		public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
                        Set userListAllowImport)
        {
		return null;
        }

        @SuppressWarnings("unchecked")
		public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
        {
		return null;
        }

}
