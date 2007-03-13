package org.sakaiproject.portal.service;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.core.PortletContextManager;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.internal.InternalPortletContext;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandler;
import org.sakaiproject.portal.api.PortalRenderEngine;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortletApplicationDescriptor;
import org.sakaiproject.portal.api.PortletDescriptor;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

public class PortalServiceImpl implements PortalService
{
	private static final Log log = LogFactory.getLog(PortalServiceImpl.class);

	/**
	 * Parameter to force state reset
	 */
	public static final String PARM_STATE_RESET = "sakai.state.reset";

	private Map<String, PortalRenderEngine> renderEngines = new HashMap<String, PortalRenderEngine>();

	private Map<String, Map<String, PortalHandler>> handlerMaps = new HashMap<String, Map<String, PortalHandler>>();

	private Map<String, Portal> portals = new HashMap<String, Portal>();

	public StoredState getStoredState()
	{
		Session s = SessionManager.getCurrentSession();
		StoredState ss = (StoredState) s.getAttribute("direct-stored-state");
		return ss;
	}

	public void setStoredState(StoredState ss)
	{
		Session s = SessionManager.getCurrentSession();
		if (s.getAttribute("direct-stored-state") == null || ss == null)
		{
			s.setAttribute("direct-stored-state", ss);
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
		return "true".equals(ServerConfigurationService.getString("charon.directurl",
				"true"));
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

		return (PortalRenderEngine) safeGet(renderEngines, context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#addRenderEngine(org.sakaiproject.portal.api.PortalRenderEngine)
	 */
	public void addRenderEngine(String context, PortalRenderEngine vengine)
	{

		safePut(renderEngines, context, vengine);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#removeRenderEngine(org.sakaiproject.portal.api.PortalRenderEngine)
	 */
	public void removeRenderEngine(String context, PortalRenderEngine vengine)
	{
		safePut(renderEngines, context, null);
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
		PortalHandler ph = (PortalHandler) safeGet(handlerMap, urlFragment);
		if (ph != null)
		{
			handler.deregister(portal);
			log.warn("Handler Present on  " + urlFragment + " will replace " + ph
					+ " with " + handler);
		}
		handler.register(portal, this, portal.getServletContext());
		safePut(handlerMap, urlFragment, handler);

		log.info("URL " + portalContext + ":/" + urlFragment + " will be handled by "
				+ handler);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#getHandlerMap(java.lang.String)
	 */
	public Map<String, PortalHandler> getHandlerMap(Portal portal)
	{
		return getHandlerMap(portal, true);
	}

	@SuppressWarnings("unchecked")
	private Map<String, PortalHandler> getHandlerMap(Portal portal, boolean create)
	{
		String portalContext = portal.getPortalContext();
		Map<String, PortalHandler> handlerMap = (Map<String, PortalHandler>) safeGet(
				handlerMaps, portalContext);
		if (create && handlerMap == null)
		{
			handlerMap = new HashMap<String, PortalHandler>();
			safePut(handlerMaps, portalContext, handlerMap);
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
		Map<String, PortalHandler> handlerMap = getHandlerMap(portal, false);
		if (handlerMap != null)
		{
			PortalHandler ph = (PortalHandler) safeGet(handlerMap, urlFragment);
			if (ph != null)
			{
				ph.deregister(portal);
				safePut(handlerMap, urlFragment, null);
				log.warn("Handler Present on  " + urlFragment + " " + ph
						+ " will be removed ");
			}
		}
	}

	/**
	 * @param handlerMap
	 * @param urlFragment
	 * @param object
	 */
	@SuppressWarnings("unchecked")
	private void safePut(Map map, String key, Object object)
	{
		int i = 3;
		while (i > 0)
		{
			try
			{
				map.put(key, object);
				i = -1;
			}
			catch (ConcurrentModificationException ex)
			{
				i--;
			}
		}
		if (i != -1)
		{
			map.put(key, object);
		}
	}

	private Object safeGet(Map map, String key)
	{
		int i = 3;
		while (i > 0)
		{
			try
			{
				return map.get(key);
			}
			catch (ConcurrentModificationException ex)
			{

				i--;
			}
		}
		return map.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalService#addPortal(org.sakaiproject.portal.api.Portal)
	 */
	public void addPortal(Portal portal)
	{
		String portalContext = portal.getPortalContext();
		safePut(portals, portalContext, portal);
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
		safePut(portals, portalContext, null);
	}

}
