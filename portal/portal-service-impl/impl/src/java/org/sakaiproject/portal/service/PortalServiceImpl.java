/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.core.PortletContextManager;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.internal.InternalPortletContext;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.util.Configuration.Property;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandler;
import org.sakaiproject.portal.api.PortalRenderEngine;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortletApplicationDescriptor;
import org.sakaiproject.portal.api.PortletDescriptor;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.api.StyleAbleProvider;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */

public class PortalServiceImpl implements PortalService
{
	private static final Log log = LogFactory.getLog(PortalServiceImpl.class);

	/**
	 * Parameter to force state reset
	 */
	public static final String PARM_STATE_RESET = "sakai.state.reset";

	private Map<String, PortalRenderEngine> renderEngines = new ConcurrentHashMap<String, PortalRenderEngine>();

	private Map<String, Map<String, PortalHandler>> handlerMaps = new ConcurrentHashMap<String, Map<String, PortalHandler>>();

	private Map<String, Portal> portals = new ConcurrentHashMap<String, Portal>();

	private StyleAbleProvider stylableServiceProvider;

	private SiteNeighbourhoodService siteNeighbourhoodService;

	public void init()
	{
		try
		{
			try
			{
				// configure the parser for castor.. before anything else get a
				// chance
				Properties castorProperties = LocalConfiguration.getDefault();
				String parser = ServerConfigurationService.getString(
						"sakai.xml.sax.parser",
						"com.sun.org.apache.xerces.internal.parsers.SAXParser");
				log.info("Configured Castor to use SAX Parser " + parser);
				castorProperties.put(Property.Parser, parser);
			}
			catch (Exception ex)
			{
				log.error("Failed to configure Castor", ex);
			}
			stylableServiceProvider = (StyleAbleProvider) ComponentManager
					.get(StyleAbleProvider.class.getName());
		}
		catch (Exception ex)
		{
		}
		if (stylableServiceProvider == null)
		{
			log.info("No Styleable Provider found, the portal will not be stylable");
		}
	}

	public StoredState getStoredState()
	{
		Session s = SessionManager.getCurrentSession();
		StoredState ss = (StoredState) s.getAttribute("direct-stored-state");
		log.debug("Got Stored State as [" + ss + "]");
		return ss;
	}

	public void setStoredState(StoredState ss)
	{
		Session s = SessionManager.getCurrentSession();
		if (s.getAttribute("direct-stored-state") == null || ss == null)
		{
			StoredState ssx = (StoredState) s.getAttribute("direct-stored-state");
			log.debug("Removing Stored state " + ssx);
			if (ssx != null)
			{
				Exception ex = new Exception("traceback");
				log.debug("Removing active Stored State Traceback gives location ", ex);
			}

			s.setAttribute("direct-stored-state", ss);
			log.debug(" Set StoredState as [" + ss + "]");
		}
	}

	private static final String TOOLSTATE_PARAM_PREFIX = "toolstate-";

	private static String computeToolStateParameterName(String placementId)
	{
		return TOOLSTATE_PARAM_PREFIX + placementId;
	}

	public String decodeToolState(Map<String, String[]> params, String placementId)
	{
		String attrname = computeToolStateParameterName(placementId);
		String[] attrval = params.get(attrname);
		return attrval == null ? null : attrval[0];
	}

	public Map<String, String[]> encodeToolState(String placementId, String URLstub)
	{
		String attrname = computeToolStateParameterName(placementId);
		Map<String, String[]> togo = new HashMap<String, String[]>();
		// could assemble state from other visible tools here
		togo.put(attrname, new String[] { URLstub });
		return togo;
	}

	// To allow us to retain reset state across redirects
	public String getResetState()
	{
		Session s = SessionManager.getCurrentSession();
		String ss = (String) s.getAttribute("reset-stored-state");
		return ss;
	}

	public void setResetState(String ss)
	{
		Session s = SessionManager.getCurrentSession();
		if (s.getAttribute("reset-stored-state") == null || ss == null)
		{
			s.setAttribute("reset-stored-state", ss);
		}
	}

	public boolean isEnableDirect()
	{
		boolean directEnable = "true".equals(ServerConfigurationService.getString(
				"charon.directurl", "true"));
		log.debug("Direct Enable is " + directEnable);
		return directEnable;
	}

	public boolean isResetRequested(HttpServletRequest req)
	{
		return "true".equals(req.getParameter(PARM_STATE_RESET))
				|| "true".equals(getResetState());
	}

	public String getResetStateParam()
	{
		// TODO Auto-generated method stub
		return PARM_STATE_RESET;
	}

	public StoredState newStoredState(String marker, String replacement)
	{
		log.debug("Storing State for Marker=[" + marker + "] replacement=[" + replacement
				+ "]");
		return new StoredStateImpl(marker, replacement);
	}

	public Iterator<PortletApplicationDescriptor> getRegisteredApplications()
	{
		PortletRegistryService registry = PortletContextManager.getManager();
		final Iterator apps = registry.getRegisteredPortletApplications();
		return new Iterator<PortletApplicationDescriptor>()
		{

			public boolean hasNext()
			{
				return apps.hasNext();
			}

			public PortletApplicationDescriptor next()
			{
				final InternalPortletContext pc = (InternalPortletContext) apps.next();

				final PortletAppDD appDD = pc.getPortletApplicationDefinition();
				return new PortletApplicationDescriptor()
				{

					public String getApplicationContext()
					{
						return pc.getPortletContextName();
					}

					public String getApplicationId()
					{
						return pc.getApplicationId();
					}

					public String getApplicationName()
					{
						return pc.getApplicationId();
					}

					public Iterator<PortletDescriptor> getPortlets()
					{
						if (appDD != null)
						{
							List portlets = appDD.getPortlets();

							final Iterator portletsI = portlets.iterator();
							return new Iterator<PortletDescriptor>()
							{

								public boolean hasNext()
								{
									return portletsI.hasNext();
								}

								public PortletDescriptor next()
								{
									final PortletDD pdd = (PortletDD) portletsI.next();
									return new PortletDescriptor()
									{

										public String getPortletId()
										{
											return pdd.getPortletName();
										}

										public String getPortletName()
										{
											return pdd.getPortletName();
										}

									};
								}

								public void remove()
								{
								}

							};
						}
						else
						{
							log.warn(" Portlet Application has no portlets "
									+ pc.getPortletContextName());
							return new Iterator<PortletDescriptor>()
							{

								public boolean hasNext()
								{
									return false;
								}

								public PortletDescriptor next()
								{
									return null;
								}

								public void remove()
								{
								}

							};
						}
					}

				};
			}

			public void remove()
			{
			}

		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#getRenderEngine(javax.servlet.http.HttpServletRequest)
	 */
	public PortalRenderEngine getRenderEngine(String context, HttpServletRequest request)
	{
		// at this point we ignore request but we might use ut to return more
		// than one render engine

		if (context == null || context.length() == 0)
		{
			context = Portal.DEFAULT_PORTAL_CONTEXT;
		}

		return (PortalRenderEngine) renderEngines.get(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#addRenderEngine(org.sakaiproject.portal.api.PortalRenderEngine)
	 */
	public void addRenderEngine(String context, PortalRenderEngine vengine)
	{

		renderEngines.put(context, vengine);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#removeRenderEngine(org.sakaiproject.portal.api.PortalRenderEngine)
	 */
	public void removeRenderEngine(String context, PortalRenderEngine vengine)
	{
		renderEngines.remove(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#addHandler(java.lang.String,
	 *      org.sakaiproject.portal.api.PortalHandler)
	 */
	public void addHandler(Portal portal, PortalHandler handler)
	{
		String portalContext = portal.getPortalContext();
		Map<String, PortalHandler> handlerMap = getHandlerMap(portal);
		String urlFragment = handler.getUrlFragment();
		PortalHandler ph = handlerMap.get(urlFragment);
		if (ph != null)
		{
			handler.deregister(portal);
			log.warn("Handler Present on  " + urlFragment + " will replace " + ph
					+ " with " + handler);
		}
		handler.register(portal, this, portal.getServletContext());
		handlerMap.put(urlFragment, handler);

		log.info("URL " + portalContext + ":/" + urlFragment + " will be handled by "
				+ handler);

	}
	
	public void addHandler(String portalContext, PortalHandler handler) 
	{
		Portal portal = portals.get(portalContext);
		if (portal == null)
		{
			Map<String, PortalHandler> handlerMap = getHandlerMap(portalContext, true);
			handlerMap.put(handler.getUrlFragment(), handler);
			log.debug("Registered handler ("+ handler+ ") for portal ("+portalContext+ ") that doesn't yet exist.");
		}
		else
		{
			addHandler(portal, handler);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#getHandlerMap(java.lang.String)
	 */
	public Map<String, PortalHandler> getHandlerMap(Portal portal)
	{
		return getHandlerMap(portal.getPortalContext(), true);
	}

	@SuppressWarnings("unchecked")
	private Map<String, PortalHandler> getHandlerMap(String portalContext, boolean create)
	{
		Map<String, PortalHandler> handlerMap = handlerMaps.get(portalContext);
		if (create && handlerMap == null)
		{
			handlerMap = new ConcurrentHashMap<String, PortalHandler>();
			handlerMaps.put(portalContext, handlerMap);
		}
		return handlerMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#removeHandler(java.lang.String,
	 *      java.lang.String) This method it NOT thread safe, but the likelyhood
	 *      of a co
	 */
	public void removeHandler(Portal portal, String urlFragment)
	{
		Map<String, PortalHandler> handlerMap = getHandlerMap(portal.getPortalContext(), false);
		if (handlerMap != null)
		{
			PortalHandler ph = handlerMap.get(urlFragment);
			if (ph != null)
			{
				ph.deregister(portal);
				handlerMap.remove(urlFragment);
				log.warn("Handler Present on  " + urlFragment + " " + ph
						+ " will be removed ");
			}
		}
	}
	
	public void removeHandler(String portalContext, String urlFragment)
	{
		Portal portal = portals.get(portalContext);
		if (portal == null)
		{
			log.warn("Attempted to remove handler("+ urlFragment+ ") from non existent portal ("+portalContext+")");
		}
		else
		{
			removeHandler(portal, urlFragment);
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#addPortal(org.sakaiproject.portal.api.Portal)
	 */
	public void addPortal(Portal portal)
	{
		String portalContext = portal.getPortalContext();
		portals.put(portalContext, portal);
		// reconnect any handlers
		Map<String, PortalHandler> phm = getHandlerMap(portal);
		for (Iterator<PortalHandler> pIterator = phm.values().iterator(); pIterator
				.hasNext();)
		{
			PortalHandler ph = pIterator.next();
			ph.register(portal, this, portal.getServletContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#removePortal(org.sakaiproject.portal.api.Portal)
	 */
	public void removePortal(Portal portal)
	{
		String portalContext = portal.getPortalContext();
		portals.remove(portalContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#getStylableService()
	 */
	public StyleAbleProvider getStylableService()
	{
		return stylableServiceProvider;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.api.PortalService#getSiteNeighbourhoodService()
	 */
	public SiteNeighbourhoodService getSiteNeighbourhoodService()
	{
		return siteNeighbourhoodService;
	}

	/**
	 * @param siteNeighbourhoodService the siteNeighbourhoodService to set
	 */
	public void setSiteNeighbourhoodService(SiteNeighbourhoodService siteNeighbourhoodService)
	{
		this.siteNeighbourhoodService = siteNeighbourhoodService;
	}

}
