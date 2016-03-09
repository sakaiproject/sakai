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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.pluto.core.PortletContextManager;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.internal.InternalPortletContext;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.util.Configuration.Property;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.portal.api.BaseEditor;
import org.sakaiproject.portal.api.Editor;
import org.sakaiproject.portal.api.EditorRegistry;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandler;
import org.sakaiproject.portal.api.PortalRenderEngine;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortletApplicationDescriptor;
import org.sakaiproject.portal.api.PortletDescriptor;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.api.StyleAbleProvider;
import org.sakaiproject.portal.beans.BullhornAlert;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */

public class PortalServiceImpl implements PortalService
{
	private static final Logger log = LoggerFactory.getLogger(PortalServiceImpl.class);

	/**
	 * Parameter to force state reset
	 */
	public static final String PARM_STATE_RESET = "sakai.state.reset";

	private Map<String, PortalRenderEngine> renderEngines = new ConcurrentHashMap<String, PortalRenderEngine>();

	private Map<String, Map<String, PortalHandler>> handlerMaps = new ConcurrentHashMap<String, Map<String, PortalHandler>>();

	private Map<String, Portal> portals = new ConcurrentHashMap<String, Portal>();
	
	private ServerConfigurationService serverConfigurationService;

	private StyleAbleProvider stylableServiceProvider;

	private SiteNeighbourhoodService siteNeighbourhoodService;
	
	private String m_portalLinks;
	
	private ContentHostingService contentHostingService;
	
	private EditorRegistry editorRegistry;

	private MemoryService memoryService;
	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}

	private SqlService sqlService;
	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	private Editor noopEditor = new BaseEditor("noop", "noop", "", "");

	public void init()
	{
		try
		{
			stylableServiceProvider = (StyleAbleProvider) ComponentManager
					.get(StyleAbleProvider.class.getName());
			serverConfigurationService = (ServerConfigurationService) ComponentManager
					.get(ServerConfigurationService.class.getName());

			try
			{	
				// configure the parser for castor.. before anything else get a
				// chance
				Properties castorProperties = LocalConfiguration.getDefault();
				String parser = serverConfigurationService.getString(
						"sakai.xml.sax.parser",
						"com.sun.org.apache.xerces.internal.parsers.SAXParser");
				log.info("Configured Castor to use SAX Parser " + parser);
				castorProperties.put(Property.Parser, parser);
			}
			catch (Exception ex)
			{
				log.error("Failed to configure Castor", ex);
			}
			
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
		boolean directEnable = "true".equals(serverConfigurationService.getString(
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
	/* optional portal links for portal header (SAK-22912)
	 */
	public String getPortalLinks()
	{
		return m_portalLinks;
	}	
	
	
	public ContentHostingService getContentHostingService() {
		return contentHostingService;
	}
	/**
	 * @param portalLinks the portal icons to set
	 */
	public void setPortalLinks(String portalLinks)
	{
		m_portalLinks = portalLinks;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	public String getBrowserCollectionId(Placement placement) {
		String collectionId = null;
		if (placement != null) {
			collectionId = getContentHostingService().getSiteCollection(placement.getContext());
		}
		if (collectionId == null) {
			collectionId = getContentHostingService().getSiteCollection("~" + SessionManager.getCurrentSessionUserId());
		}
		return collectionId;
	}

	public Editor getActiveEditor() {
		return getActiveEditor(null);
	}

	public Editor getActiveEditor(Placement placement) {
		String systemEditor = serverConfigurationService.getString("wysiwyg.editor", "ckeditor");
		
		String activeEditor = systemEditor;
		if (placement != null) {
			//Allow tool- or user-specific editors?
			try {
				Site site = siteService.getSite(placement.getContext());
				Object o = site.getProperties().get("wysiwyg.editor");
				if (o != null) {
					activeEditor = o.toString();
				}
			}
			catch (IdUnusedException ex) {
				if (log.isDebugEnabled()) {
					log.debug(ex.getMessage());
				}
			}
		}
		
		Editor editor = getEditorRegistry().getEditor(activeEditor);
		if (editor == null) {
			// Load a base no-op editor so sakai.editor.launch calls succeed.
			// We may decide to offer some textarea infrastructure as well. In
			// this case, there are editor and launch files being consulted
			// already from /library/, which is easier to patch and deploy.
			editor = getEditorRegistry().getEditor("textarea");
		}
		if (editor == null) {
			// If, for some reason, our stub editor is null, give an instance
			// that doesn't even try to load files. This will result in script
			// errors because sakai.editor.launch will not be defined, but
			// this way, we can't suffer NPEs. In some cases, this degradation
			// will be graceful enough that the page can function.
			editor = noopEditor;
		}
		
		return editor;
	}

	public EditorRegistry getEditorRegistry() {
		return editorRegistry;
	}

	public void setEditorRegistry(EditorRegistry editorRegistry) {
		this.editorRegistry = editorRegistry;
	}
	
	public String getSkinPrefix() {
		return "";
	}

	public List<BullhornAlert> getSocialAlerts(String userId) {

		List<BullhornAlert> alerts = sqlService.dbRead(
                "SELECT * FROM SOCIAL_ALERTS WHERE TO_USER = ? ORDER BY EVENT_DATE DESC"
				, new Object[] { userId }
				, new SqlReader() {
						public Object readSqlResultRecord(ResultSet rs) {
							return new BullhornAlert(rs);
						}
					}
				);

		for (BullhornAlert alert : alerts) {
			try {
				User fromUser = userDirectoryService.getUser(alert.from);
				alert.fromDisplayName = fromUser.getDisplayName();
			} catch (UserNotDefinedException unde) {
				alert.fromDisplayName = alert.from;
			}
		}

		return alerts;
	}

	public int getSocialAlertCount(String userId) {

		Cache countCache = memoryService.newCache("bullhorn_alert_count_cache");

		Map<String, Integer> cachedCounts = (Map<String, Integer>) countCache.get(userId);

		if (cachedCounts == null) { cachedCounts = new HashMap(); }

		Integer count = cachedCounts.get("social");

		if (count != null) {
			log.debug("bullhorn_alert_count_cache hit");
			return count;
		} else {
			log.debug("bullhorn_alert_count_cache miss");

			List<Integer> counts = sqlService.dbRead("SELECT COUNT(*) FROM SOCIAL_ALERTS WHERE TO_USER = ?"
			, new Object[] { userId }
			, new SqlReader() {
					public Object readSqlResultRecord(ResultSet rs) {

						try {
							return rs.getInt(1);
						} catch (Exception e) {
							log.error("Failed to get social alert count. Returning 0 ..." , e);
							return 0;
						}
					}
				}
			);
			count = counts.get(0);
			cachedCounts.put("social", count);
			countCache.put(userId, cachedCounts);
			return count;
		}
	}

	public boolean clearSocialAlert(String userId, long alertId) {

		sqlService.dbWrite("DELETE FROM SOCIAL_ALERTS WHERE ID = ? AND TO_USER = ?"
						, new Object[] {alertId, userId});

		Cache countCache = memoryService.newCache("bullhorn_alert_count_cache");
		countCache.remove(userId);

		return true;
	}

	public boolean clearAllSocialAlerts(String userId) {

		sqlService.dbWrite("DELETE FROM SOCIAL_ALERTS WHERE TO_USER = ?"
						, new Object[] {userId});

		Cache countCache = memoryService.newCache("bullhorn_alert_count_cache");
		countCache.remove(userId);

		return true;
	}

	public List<BullhornAlert> getAcademicAlerts(String userId) {

		List<BullhornAlert> alerts = sqlService.dbRead(
                "SELECT * FROM ACADEMIC_ALERTS WHERE TO_USER = ? ORDER BY EVENT_DATE DESC"
				, new Object[] { userId }
				, new SqlReader() {
						public Object readSqlResultRecord(ResultSet rs) {
							return new BullhornAlert(rs);
						}
					}
				);

		for (BullhornAlert alert : alerts) {
			try {
				User fromUser = userDirectoryService.getUser(alert.from);
				alert.fromDisplayName = fromUser.getDisplayName();
				if (!StringUtils.isBlank(alert.siteId)) {
					alert.siteTitle = siteService.getSite(alert.siteId).getTitle();
				}
			} catch (UserNotDefinedException unde) {
				alert.fromDisplayName = alert.from;
			} catch (IdUnusedException iue) {
				alert.siteTitle = alert.siteId;
			}
		}

		return alerts;
	}

	public int getAcademicAlertCount(String userId) {

		Cache countCache = memoryService.newCache("bullhorn_alert_count_cache");

		Map<String, Integer> cachedCounts = (Map<String, Integer>) countCache.get(userId);

		if (cachedCounts == null) { cachedCounts = new HashMap(); }

		Integer count = cachedCounts.get("academic");

		if (count != null) {
			log.debug("bullhorn_alert_count_cache hit");
			return count;
		} else {
			log.debug("bullhorn_alert_count_cache miss");
			List<Integer> counts = sqlService.dbRead("SELECT COUNT(*) FROM ACADEMIC_ALERTS WHERE TO_USER = ?"
				, new Object[] { userId }
				, new SqlReader() {
						public Object readSqlResultRecord(ResultSet rs) {

							try {
								return rs.getInt(1);
							} catch (Exception e) {
								log.error("Failed to get social alert count. Returning 0 ..." , e);
								return 0;
							}
						}
					}
				);
			count = counts.get(0);
			cachedCounts.put("academic", count);
			countCache.put(userId, cachedCounts);
			return count;
		}
	}

	public boolean clearAcademicAlert(String userId, long alertId) {

		sqlService.dbWrite("DELETE FROM ACADEMIC_ALERTS WHERE ID = ? AND TO_USER = ?"
						, new Object[] {alertId, userId});

		Cache countCache = memoryService.newCache("bullhorn_alert_count_cache");
		countCache.remove(userId);

		return true;
	}

	public boolean clearAllAcademicAlerts(String userId) {

		sqlService.dbWrite("DELETE FROM ACADEMIC_ALERTS WHERE TO_USER = ?"
						, new Object[] {userId});

		Cache countCache = memoryService.newCache("bullhorn_alert_count_cache");
		countCache.remove(userId);

		return true;
	}
}
