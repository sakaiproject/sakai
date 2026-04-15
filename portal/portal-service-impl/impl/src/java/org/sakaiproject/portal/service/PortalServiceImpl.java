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
 *		 http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pluto.core.PortletContextManager;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.internal.InternalPortletContext;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.exolab.castor.util.Configuration.Property;
import org.exolab.castor.util.LocalConfiguration;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
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
import org.sakaiproject.portal.api.PortalSubPageNavProvider;
import org.sakaiproject.portal.api.model.PinnedSite;
import org.sakaiproject.portal.api.model.RecentSite;
import org.sakaiproject.portal.api.repository.PinnedSiteRepository;
import org.sakaiproject.portal.api.repository.RecentSiteRepository;
import org.sakaiproject.scheduling.api.SchedulingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Default portal service implementation.
 *
 * All pinned/recent site mutations must go through this service so the
 * per-user in-memory navigation state stays consistent with the backing
 * repositories.
 */
@Slf4j
public class PortalServiceImpl implements PortalService, Observer, DisposableBean, SmartInitializingSingleton
{
	/**
	 * Parameter to force state reset
	 */
	public static final String PARM_STATE_RESET = "sakai.state.reset";

	@Setter private AuthzGroupService authzGroupService;
	@Setter private ContentHostingService contentHostingService;
	@Setter private EditorRegistry editorRegistry;
	@Setter private EventTrackingService eventTrackingService;
	@Setter private PinnedSiteRepository pinnedSiteRepository;
	@Setter private PreferencesService preferencesService;
	@Setter private RecentSiteRepository recentSiteRepository;
	@Setter private SchedulingService schedulingService;
	@Setter private SecurityService securityService;
	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private SessionManager sessionManager;
	@Setter private SiteNeighbourhoodService siteNeighbourhoodService;
	@Setter private SiteService siteService;
	@Setter private UserDirectoryService userDirectoryService;

	private Map<String, Map<String, PortalHandler>> handlerMaps = new ConcurrentHashMap<>();
	private Editor noopEditor = new BaseEditor("noop", "noop", "", "");
	private Map<String, Portal> portals = new ConcurrentHashMap<>();
	private Map<String, PortalRenderEngine> renderEngines = new ConcurrentHashMap<>();
	private Map<String, UserPortalNavContext> portalNavContexts = new ConcurrentHashMap<>();
	private Collection<PortalSubPageNavProvider> portalSubPageNavProviders;
	private ScheduledFuture<?> portalNavContextEvictionTask;

	public static final int DEFAULT_MAX_RECENT_SITES = 3;
	public static final int DEFAULT_MAX_PINNED_SITES = 100;
	private static final int PORTAL_NAV_FLUSH_DELAY_MS = 60 * 1000;
	private static final int PORTAL_NAV_FLUSH_RETRY_DELAY_MS = 60 * 1000;
	private static final int PORTAL_NAV_CONTEXT_IDLE_MS = 15 * 60 * 1000;

	public void init() {
		try {
			// configure the parser for castor, before anything else get a chance
			Properties castorProperties = LocalConfiguration.getDefault();
			String parser = serverConfigurationService.getString(
					"sakai.xml.sax.parser",
					"com.sun.org.apache.xerces.internal.parsers.SAXParser");
			log.info("Configured Castor to use SAX Parser " + parser);
			castorProperties.put(Property.Parser, parser);
		} catch (Exception ex) {
			log.warn("Failed to configure Castor, {}", ex.toString());
		}
		eventTrackingService.addLocalObserver(this);
		portalSubPageNavProviders = new HashSet<>();
	}

	@Override
	public void afterSingletonsInstantiated() {
		portalNavContextEvictionTask = schedulingService.scheduleWithFixedDelay(this::evictIdlePortalNavContexts,
				PORTAL_NAV_CONTEXT_IDLE_MS,
				PORTAL_NAV_CONTEXT_IDLE_MS,
				TimeUnit.MILLISECONDS);
	}

	@Override
	public void destroy() {
		if (eventTrackingService != null) {
			eventTrackingService.deleteObserver(this);
		}
		if (portalNavContextEvictionTask != null) {
			portalNavContextEvictionTask.cancel(false);
			portalNavContextEvictionTask = null;
		}
		portalNavContexts.values().forEach(context -> flushPortalNavContext(context, true));
		portalNavContexts.clear();
	}

	@Override
	public void update(Observable observable, Object o) {

		if (!(o instanceof Event)) return;

		Event event = (Event) o;
		String eventName = event.getEvent();

		switch (eventName) {
			case UsageSessionService.EVENT_LOGIN_CONTAINER:
			case UsageSessionService.EVENT_LOGIN: {
				Session sakaiSession = sessionManager.getCurrentSession();
				boolean justLoggedIn = BooleanUtils.toBoolean((Boolean) sakaiSession.getAttribute(Session.JUST_LOGGED_IN));
				String userId = sakaiSession.getUserId();
				if (justLoggedIn && userId != null && userId.equals(event.getUserId())) {
					syncUserSitesWithPortalNav(userId);
				}
				break;
			}
			case UsageSessionService.EVENT_LOGOUT: {
				String userId = event.getUserId();
				if (StringUtils.isNotBlank(userId)) {
					UserPortalNavContext context = portalNavContexts.get(userId);
					if (context != null) {
						flushPortalNavContext(context, true);
					}
				}
				break;
			}
			case SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD: {
                String userId = userDirectoryService.idFromReference(event.getResource());
                if (canUserUpdateSite(userId, event.getContext()) || isUserActiveMemberInPublishedSite(userId, event.getContext())) {
                    addPinnedSite(userId, event.getContext(), true, false);
                }
				break;
			}
			case SiteService.SECURE_ADD_SITE:
			case SiteService.EVENT_SITE_PUBLISH: {
				final String siteId = siteService.idFromSiteReference(event.getResource());
				try {
					AuthzGroup azg = authzGroupService.getAuthzGroup(event.getResource());
					azg.getUsers().stream()
							.filter(u -> !isSiteUnpinnedByUser(u, siteId) && (canUserUpdateSite(u, siteId) || isUserActiveMemberInSite(u, siteId)))
							.forEach(u -> addPinnedSite(u, siteId, true, false));
				} catch (GroupNotDefinedException gnde) {
					log.warn("Could not access AuthzGroup with id [{}], {}", event.getResource(), gnde.toString());
				}
				break;
			}
			case SiteService.EVENT_SITE_UNPUBLISH: {
				try {
					final String siteId = siteService.idFromSiteReference(event.getResource());
					AuthzGroup azg = authzGroupService.getAuthzGroup(event.getResource());
					azg.getUsers().forEach(u -> {
						if (!canUserUpdateSite(u, siteId)) {
							// Remove pinned site if it actually exists and was not explicitly unpinned
							if (!isSiteUnpinnedByUser(u, siteId)) {
								removePinnedSite(u, siteId, false);
							}

							if (hasRecentSite(u, siteId)) {
								removeRecentSite(u, siteId, false);
							}
						}
					});
				} catch (GroupNotDefinedException gnde) {
					log.warn("Could not access AuthzGroup with id [{}], {}", event.getResource(), gnde.toString());
				}
				break;
			}
			case SiteService.SECURE_UPDATE_SITE_MEMBERSHIP: {
				Set<String> pinnedUserIds = pinnedSiteRepository.findBySiteId(event.getContext()).stream()
						.map(PinnedSite::getUserId)
						.collect(Collectors.toSet());

				Set<String> recentUserIds = recentSiteRepository.findBySiteId(event.getContext()).stream()
						.map(RecentSite::getUserId)
						.collect(Collectors.toSet());

				portalNavContexts.values().forEach(context -> {
					synchronized (context) {
						if (context.portalNavState.pinnedSitesBySiteId.containsKey(event.getContext())) {
							pinnedUserIds.add(context.userId);
						}
						if (context.portalNavState.recentSitesBySiteId.containsKey(event.getContext())) {
							recentUserIds.add(context.userId);
						}
					}
				});

				if (recentUserIds.isEmpty() && pinnedUserIds.isEmpty()) {
					return;
				}

				try {
					Site site = siteService.getSite(event.getContext());
					Set<String> siteUsers = site.getUsers();

					pinnedUserIds.forEach(userId -> {
						if (!siteUsers.contains(userId)) {
							removePinnedSite(userId, event.getContext(), false);
						}
					});
					recentUserIds.forEach(userId -> {
						if (!siteUsers.contains(userId)) {
							removeRecentSite(userId, event.getContext(), false);
						}
					});
					siteUsers.forEach(userId -> {
						if (!pinnedUserIds.contains(userId)
								&& (canUserUpdateSite(userId, event.getContext()) || isUserActiveMemberInPublishedSite(userId, event.getContext()))) {
							addPinnedSite(userId, event.getContext(), true, false);
						}
					});
				} catch (IdUnusedException idue) {
					log.warn("No site for {} while cleaning up pinned sites, {}", event.getContext(), idue.toString());
				}
				break;
			}
			case SiteService.SECURE_REMOVE_SITE:
			case SiteService.SOFT_DELETE_SITE: {
				String siteId = event.getContext();
				List<String> siteIdsToRemove = Collections.singletonList(siteId);
				List<UserPortalNavContext> contextsToFlush = new ArrayList<>();
				portalNavContexts.values().forEach(context -> {
					boolean removed = false;
					synchronized (context) {
						if (context.portalNavState.pinnedSiteIds.contains(siteId)
								|| context.portalNavState.unpinnedSiteIds.contains(siteId)
								|| context.portalNavState.pinnedSitesBySiteId.containsKey(siteId)
								|| context.portalNavState.recentSiteIds.contains(siteId)
								|| context.portalNavState.recentSitesBySiteId.containsKey(siteId)) {
							removeSitesFromPortalNavState(siteIdsToRemove, context.portalNavState);
							context.dirty = true;
							context.version++;
							removed = true;
						}
					}
					if (removed) {
						contextsToFlush.add(context);
					}
				});
				contextsToFlush.forEach(this::schedulePortalNavFlush);
				pinnedSiteRepository.deleteBySiteId(siteId);
				recentSiteRepository.deleteBySiteId(siteId);
				break;
			}
			default:
		}
	}

	@Override
	public StoredState getStoredState()
	{
		Session s = sessionManager.getCurrentSession();
		StoredState ss = (StoredState) s.getAttribute("direct-stored-state");
		log.debug("Got Stored State as [" + ss + "]");
		return ss;
	}

	@Override
	public void setStoredState(StoredState ss)
	{
		Session s = sessionManager.getCurrentSession();
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

	@Override
	public String decodeToolState(Map<String, String[]> params, String placementId)
	{
		String attrname = computeToolStateParameterName(placementId);
		String[] attrval = params.get(attrname);
		return attrval == null ? null : attrval[0];
	}

	@Override
	public Map<String, String[]> encodeToolState(String placementId, String URLstub)
	{
		String attrname = computeToolStateParameterName(placementId);
		Map<String, String[]> togo = new HashMap<String, String[]>();
		// could assemble state from other visible tools here
		togo.put(attrname, new String[] { URLstub });
		return togo;
	}

	// To allow us to retain reset state across redirects
	@Override
	public String getResetState()
	{
		Session s = sessionManager.getCurrentSession();
		String ss = (String) s.getAttribute("reset-stored-state");
		return ss;
	}

	@Override
	public void setResetState(String ss)
	{
		Session s = sessionManager.getCurrentSession();
		if (s.getAttribute("reset-stored-state") == null || ss == null)
		{
			s.setAttribute("reset-stored-state", ss);
		}
	}

	@Override
	public boolean isEnableDirect()
	{
		boolean directEnable = "true".equals(serverConfigurationService.getString(
				"charon.directurl", "true"));
		log.debug("Direct Enable is " + directEnable);
		return directEnable;
	}

	@Override
	public boolean isResetRequested(HttpServletRequest req)
	{
		return "true".equals(req.getParameter(PARM_STATE_RESET))
				|| "true".equals(getResetState());
	}

	@Override
	public String getResetStateParam()
	{
		// TODO Auto-generated method stub
		return PARM_STATE_RESET;
	}

	@Override
	public StoredState newStoredState(String marker, String replacement)
	{
		log.debug("Storing State for Marker=[" + marker + "] replacement=[" + replacement
				+ "]");
		return new StoredStateImpl(marker, replacement);
	}

	@Override
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

	@Override
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

	@Override
	public void addRenderEngine(String context, PortalRenderEngine vengine)
	{

		renderEngines.put(context, vengine);
	}

	@Override
	public void removeRenderEngine(String context, PortalRenderEngine vengine)
	{
		renderEngines.remove(context);
	}

	@Override
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
	
	@Override
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

	@Override
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

	@Override
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
	
	@Override
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

	@Override
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

	@Override
	public void removePortal(Portal portal)
	{
		String portalContext = portal.getPortalContext();
		portals.remove(portalContext);
	}

	@Override
	public String getContentItemUrl(Site site) {

		if ( site == null ) return null;
				ToolConfiguration toolConfig = site.getToolForCommonId("sakai.siteinfo");

				if (toolConfig == null) return null;

		// SAK-32656 For now we always show the cart.
		// Un-comment these lines to make the cart only appear when tools are
		// available at a cost of one SQL query per request/response cycle.

		/*
		// Check if we have any registered ContentItem editor tools
		LTIService ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");

		List<Map<String, Object>> toolsContentItem = ltiService.getToolsContentEditor(placement.getContext());
		if ( toolsContentItem.size() < 1 ) return null;
		*/

		// Now we are in good shape, make the URL
		String helper_url = "/portal/tool/"+toolConfig.getId()+"/sakai.lti.admin.helper.helper?panel=CKEditor";
		return helper_url;
	}

	@Override
	public String getBrowserCollectionId(Placement placement) {
		String collectionId = null;
		if (placement != null) {
			collectionId = contentHostingService.getSiteCollection(placement.getContext());
		}
		if (collectionId == null) {
			collectionId = contentHostingService.getSiteCollection("~" + sessionManager.getCurrentSessionUserId());
		}
		return collectionId;
	}

	@Override
	public Editor getActiveEditor() {
		return getActiveEditor(null);
	}

	@Override
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
		
		Editor editor = editorRegistry.getEditor(activeEditor);
		if (editor == null) {
			// Load a base no-op editor so sakai.editor.launch calls succeed.
			// We may decide to offer some textarea infrastructure as well. In
			// this case, there are editor and launch files being consulted
			// already from /library/, which is easier to patch and deploy.
			editor = editorRegistry.getEditor("textarea");
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

	@Override
	public String getSkinPrefix() {
		return "";
	}

	@Override
	public String getQuickLinksTitle(String siteSkin) {
		//Try the skin .info first, then default to the regular, then if that fails just return an empty string
		//A null siteSkin is fine but this would generally just return the defined default (like morpheus-default) and not return anything
		return serverConfigurationService.getString("portal.quicklink." + siteSkin + ".info", serverConfigurationService.getString("portal.quicklink.info", ""));
	}

	@Override
	public List<Map> getQuickLinks(String siteSkin){
		/* Find the quick links (if they are in the properties file) ready for display in the top navigation bar.
		 * First try with the skin name as there may be different quick links per site, then try with no skin. */
		List<String> linkUrls = null;
		List<String> linkTitles = null;
		List<String>linkNames = null;
		List<String> linkIcons = null;

		//A null check really isn't needed here sin siteSkin should always be set (or it can just turn into the string "null") but it's here anyway)
		if (siteSkin != null) {
			linkUrls = Arrays.asList(ArrayUtils.nullToEmpty(serverConfigurationService.getStrings("portal.quicklink." + siteSkin + ".url")));
			linkTitles = Arrays.asList(ArrayUtils.nullToEmpty(serverConfigurationService.getStrings("portal.quicklink." + siteSkin + ".title")));
			linkNames = Arrays.asList(ArrayUtils.nullToEmpty(serverConfigurationService.getStrings("portal.quicklink." + siteSkin + ".name")));
			linkIcons = Arrays.asList(ArrayUtils.nullToEmpty(serverConfigurationService.getStrings("portal.quicklink." + siteSkin + ".icon")));
		}

		//However if it is null or if the linkUrls was empty from before, just use the default
		if (siteSkin == null || (siteSkin != null && linkUrls.isEmpty())) {
			linkUrls = Arrays.asList(ArrayUtils.nullToEmpty(serverConfigurationService.getStrings("portal.quicklink.url")));
			linkTitles = Arrays.asList(ArrayUtils.nullToEmpty(serverConfigurationService.getStrings("portal.quicklink.title")));
			linkNames = Arrays.asList(ArrayUtils.nullToEmpty(serverConfigurationService.getStrings("portal.quicklink.name")));
			linkIcons = Arrays.asList(ArrayUtils.nullToEmpty(serverConfigurationService.getStrings("portal.quicklink.icon")));
		}

		List<Map> quickLinks = new ArrayList<Map>(linkUrls.size());
		if (!linkUrls.isEmpty()) {
			if (linkUrls.size() != linkTitles.size() || linkUrls.size() != linkNames.size() || linkUrls.size() != linkIcons.size()) {
				log.info("All portal.quicklink variables must be defined and the same size for quick links feature to work. One or more is not configured correctly.");
				return new ArrayList<Map>();
			}
			for (int i = 0; i < linkUrls.size(); i++) {
				String url = linkUrls.get(i);
				String title = linkTitles.get(i);
				String name = linkNames.get(i);
				String icon = linkIcons.get(i);

				if (url != null) {
					Map<String, String> linkDetails = new HashMap<String, String>();
					linkDetails.put("url", url);
					if (name != null) {
						linkDetails.put("name", name);
						if (title != null) {
							linkDetails.put("title", title);
						} else {
							linkDetails.put("title", name);
						}
					} else {
						if (title != null) {
							linkDetails.put("name", title);
							linkDetails.put("title", title);
						} else {
							linkDetails.put("name", url);
							linkDetails.put("title", url);
						}
					}
					if (icon != null) {
						// if the 'portal.quicklink.icon' value has a type and at least one character for the icon name then try to parse it.
						if (icon.length()>3){
							String iconType = icon.substring(0,2);

							if (iconType.equalsIgnoreCase("im")) {
								linkDetails.put("iconType", "image");
								linkDetails.put("imageURI", icon.substring(3));
							}
							else if (iconType.equalsIgnoreCase("cl")){
								linkDetails.put("iconType", "icon");
								linkDetails.put("iconClass", icon.substring(3));
							}
						}
					}

					quickLinks.add(Collections.unmodifiableMap(linkDetails));
				}
			}
		}
		return Collections.unmodifiableList(quickLinks);

	}

	private boolean canUserUpdateSite(String userId, String siteId) {
		return securityService.unlock(userId, SiteService.SECURE_UPDATE_SITE, siteService.siteReference(siteId));
	}


	private boolean isUserActiveMemberInSite(String userId, String siteId) {
		return isUserActiveMemberInSiteImpl(userId, siteId, true);
	}

	private boolean isUserActiveMemberInPublishedSite(String userId, String siteId) {
		return isUserActiveMemberInSiteImpl(userId, siteId, false);
	}

	private boolean isUserActiveMemberInSiteImpl(String userId, String siteId, boolean excludePublishedState) {
		try {
			Site site = siteService.getSite(siteId);
			Member m = site.getMember(userId);
			return (m != null && ((site.isPublished() || excludePublishedState) && m.isActive()));
		} catch (IdUnusedException idue) {
			log.warn("Could not access site with id [{}], {}", siteId, idue.toString());
			return false;
		}
	}

	private PortalNavState readPortalNavState(String userId) {

		List<PinnedSite> pinnedSiteRows = pinnedSiteRepository.findByUserId(userId);
		Map<String, PinnedSite> pinnedSitesBySiteId = new HashMap<>();
		List<String> pinnedSiteIds = new ArrayList<>();
		List<String> unpinnedSiteIds = new ArrayList<>();

		for (PinnedSite pinnedSite : pinnedSiteRows) {
			PinnedSite copy = copyPinnedSite(pinnedSite);
			pinnedSitesBySiteId.put(copy.getSiteId(), copy);
			if (Boolean.TRUE.equals(copy.getHasBeenUnpinned())) {
				unpinnedSiteIds.add(copy.getSiteId());
			} else {
				pinnedSiteIds.add(copy.getSiteId());
			}
		}

		List<RecentSite> recentSites = recentSiteRepository.findByUserId(userId);
		Map<String, RecentSite> recentSitesBySiteId = new HashMap<>();
		List<String> recentSiteIds = new ArrayList<>();
		for (RecentSite recentSite : recentSites) {
			RecentSite copy = copyRecentSite(recentSite);
			recentSitesBySiteId.put(copy.getSiteId(), copy);
			recentSiteIds.add(copy.getSiteId());
		}

		return new PortalNavState(pinnedSitesBySiteId, pinnedSiteIds, unpinnedSiteIds, recentSitesBySiteId, recentSiteIds);
	}

	private UserPortalNavContext getOrCreatePortalNavContext(String userId) {
		UserPortalNavContext context = portalNavContexts.get(userId);
		if (context != null) {
			return context;
		}

		UserPortalNavContext created = new UserPortalNavContext(userId, readPortalNavState(userId));
		UserPortalNavContext existing = portalNavContexts.putIfAbsent(userId, created);
		return existing != null ? existing : created;
	}

	private void mutatePortalNavState(String userId, boolean createContextIfAbsent, Consumer<PortalNavState> mutation) {
		UserPortalNavContext context = portalNavContexts.get(userId);
		if (context == null) {
			if (createContextIfAbsent) {
				context = getOrCreatePortalNavContext(userId);
			} else {
				UserPortalNavContext created = new UserPortalNavContext(userId, readPortalNavState(userId));
				UserPortalNavContext existing = portalNavContexts.putIfAbsent(userId, created);
				context = existing != null ? existing : created;
				mutatePortalNavContext(context, mutation);
				if (existing == null) {
					flushPortalNavContext(context, true);
				} else {
					schedulePortalNavFlush(context);
				}
				return;
			}
		}

		mutatePortalNavContext(context, mutation);
		schedulePortalNavFlush(context);
	}

	private void mutatePortalNavContext(UserPortalNavContext context, Consumer<PortalNavState> mutation) {
		synchronized (context) {
			context.lastAccess = System.currentTimeMillis();
			mutation.accept(context.portalNavState);
			context.dirty = true;
			context.version++;
		}
	}

	private void schedulePortalNavFlush(UserPortalNavContext context) {
		schedulePortalNavFlush(context, PORTAL_NAV_FLUSH_DELAY_MS);
	}

	private void schedulePortalNavFlush(UserPortalNavContext context, int delayMs) {
		boolean flushNow = delayMs <= 0 || schedulingService == null;
		synchronized (context) {
			context.lastAccess = System.currentTimeMillis();
			if (!context.dirty) {
				return;
			}
			if (context.flushInProgress) {
				context.flushRequested = true;
				return;
			}
			if (context.scheduledFlush != null) {
				context.scheduledFlush.cancel(false);
				context.scheduledFlush = null;
			}
			if (!flushNow) {
				context.scheduledFlush = schedulingService.schedule(() -> flushPortalNavContext(context, false), delayMs, TimeUnit.MILLISECONDS);
				return;
			}
		}

		flushPortalNavContext(context, false);
	}

	private void evictIdlePortalNavContexts() {
		long idleBefore = System.currentTimeMillis() - PORTAL_NAV_CONTEXT_IDLE_MS;
		portalNavContexts.values().forEach(context -> {
			synchronized (context) {
				if (context.lastAccess < idleBefore) {
					flushPortalNavContext(context, true);
				}
			}
		});
	}

	private void flushPortalNavContext(UserPortalNavContext context, boolean evictAfterFlush) {
		PortalNavState portalNavState;
		boolean removeFavoriteSiteDataAfterFlush;
		long version;

		synchronized (context) {
			context.lastAccess = System.currentTimeMillis();
			context.evictAfterFlush = context.evictAfterFlush || evictAfterFlush;
			if (context.flushInProgress) {
				context.flushRequested = true;
				return;
			}
			if (context.scheduledFlush != null) {
				context.scheduledFlush.cancel(false);
				context.scheduledFlush = null;
			}
			if (!context.dirty) {
				if (context.evictAfterFlush) {
					portalNavContexts.remove(context.userId, context);
					context.evictAfterFlush = false;
				}
				return;
			}

			context.flushInProgress = true;
			version = context.version;
			portalNavState = copyPortalNavState(context.portalNavState);
			removeFavoriteSiteDataAfterFlush = context.removeFavoriteSiteDataAfterFlush;
		}

		boolean success = false;
		try {
			persistPortalNavState(context.userId, portalNavState);
			success = true;
		} catch (Exception e) {
			log.warn("Could not persist portal navigation state for user [{}], {}", context.userId, e.toString(), e);
		}
		boolean favoriteSiteDataRemoved = false;
		if (success && removeFavoriteSiteDataAfterFlush) {
			try {
				removeFavoriteSiteData(context.userId);
				favoriteSiteDataRemoved = true;
			} catch (Exception e) {
				log.warn("Could not clear legacy favorite site data for user [{}], {}", context.userId, e.toString(), e);
			}
		}

		boolean scheduleAnotherFlush = false;
		boolean removeContext = false;
		int nextDelay = PORTAL_NAV_FLUSH_DELAY_MS;
		synchronized (context) {
			context.flushInProgress = false;
			if (success) {
				mergePersistedStateIdentifiers(context.portalNavState, portalNavState);
				if (context.version == version) {
					context.dirty = false;
				}
				if (removeFavoriteSiteDataAfterFlush) {
					if (favoriteSiteDataRemoved) {
						context.removeFavoriteSiteDataAfterFlush = false;
					} else {
						context.dirty = true;
						nextDelay = PORTAL_NAV_FLUSH_RETRY_DELAY_MS;
					}
				}
			} else {
				context.dirty = true;
				nextDelay = PORTAL_NAV_FLUSH_RETRY_DELAY_MS;
			}

			if (context.dirty || context.flushRequested) {
				context.flushRequested = false;
				scheduleAnotherFlush = true;
			} else if (context.evictAfterFlush) {
				context.evictAfterFlush = false;
				removeContext = true;
			}
		}

		if (removeContext) {
			portalNavContexts.remove(context.userId, context);
			return;
		}

		if (scheduleAnotherFlush) {
			schedulePortalNavFlush(context, nextDelay);
		}
	}

	private PortalNavState persistPortalNavState(String userId, PortalNavState portalNavState) {
		persistPinnedSites(userId, portalNavState);
		persistRecentSites(userId, portalNavState);
		return portalNavState;
	}

	private void persistPinnedSites(String userId, PortalNavState portalNavState) {
		Map<String, PinnedSite> existingPinnedSitesBySiteId = pinnedSiteRepository.findByUserId(userId).stream()
				.collect(Collectors.toMap(PinnedSite::getSiteId, PortalServiceImpl::copyPinnedSite));

		for (PinnedSite pinnedSite : portalNavState.pinnedSitesBySiteId.values()) {
			PinnedSite existing = existingPinnedSitesBySiteId.remove(pinnedSite.getSiteId());
			PinnedSite copy = copyPinnedSite(pinnedSite);
			copy.setUserId(userId);

			if (existing == null) {
				copy.setId(null);
				PinnedSite saved = pinnedSiteRepository.save(copy);
				pinnedSite.setId(saved.getId());
				continue;
			}

			copy.setId(existing.getId());
			pinnedSite.setId(existing.getId());
			if (existing.getPosition() != copy.getPosition()
					|| !Boolean.valueOf(existing.getHasBeenUnpinned()).equals(copy.getHasBeenUnpinned())) {
				PinnedSite saved = pinnedSiteRepository.save(copy);
				pinnedSite.setId(saved.getId());
			}
		}

		existingPinnedSitesBySiteId.values()
				.forEach(pinnedSite -> pinnedSiteRepository.deleteByUserIdAndSiteId(userId, pinnedSite.getSiteId()));
	}

	private void persistRecentSites(String userId, PortalNavState portalNavState) {
		Map<String, RecentSite> existingRecentSitesBySiteId = recentSiteRepository.findByUserId(userId).stream()
				.collect(Collectors.toMap(RecentSite::getSiteId, PortalServiceImpl::copyRecentSite));

		for (String siteId : portalNavState.recentSiteIds) {
			RecentSite recentSite = portalNavState.recentSitesBySiteId.get(siteId);
			if (recentSite == null) {
				continue;
			}

			RecentSite existing = existingRecentSitesBySiteId.remove(siteId);
			RecentSite copy = copyRecentSite(recentSite);
			copy.setUserId(userId);

			if (existing == null) {
				copy.setId(null);
				RecentSite saved = recentSiteRepository.save(copy);
				recentSite.setId(saved.getId());
				continue;
			}

			copy.setId(existing.getId());
			recentSite.setId(existing.getId());
			if (!existing.getCreated().equals(copy.getCreated())) {
				RecentSite saved = recentSiteRepository.save(copy);
				recentSite.setId(saved.getId());
			}
		}

		existingRecentSitesBySiteId.values()
				.forEach(recentSite -> recentSiteRepository.deleteByUserIdAndSiteId(userId, recentSite.getSiteId()));
	}

	private void addRecentSite(String userId, String siteId, PortalNavState portalNavState) {

		if (StringUtils.isAnyBlank(userId, siteId)
				|| siteService.isUserSite(siteId)
				|| SiteService.SITE_ERROR.equals(siteId)) {
			return;
		}

		portalNavState.recentSiteIds.remove(siteId);

		int maxRecentSites = serverConfigurationService.getInt("portal.max.recent.sites", DEFAULT_MAX_RECENT_SITES);
		if (maxRecentSites <= 0) {
			portalNavState.recentSitesBySiteId.remove(siteId);
			return;
		}

		RecentSite recentSite = portalNavState.recentSitesBySiteId.get(siteId);
		if (recentSite == null) {
			recentSite = new RecentSite();
			recentSite.setUserId(userId);
			recentSite.setSiteId(siteId);
			portalNavState.recentSitesBySiteId.put(siteId, recentSite);
		}
		recentSite.setCreated(Instant.now());
		portalNavState.recentSiteIds.add(0, siteId);
		while (portalNavState.recentSiteIds.size() > maxRecentSites) {
			String removedSiteId = portalNavState.recentSiteIds.remove(portalNavState.recentSiteIds.size() - 1);
			portalNavState.recentSitesBySiteId.remove(removedSiteId);
		}
	}

	private void removeRecentSiteFromPortalNavState(String siteId, PortalNavState portalNavState) {
		portalNavState.recentSiteIds.remove(siteId);
		portalNavState.recentSitesBySiteId.remove(siteId);
	}

	private PinnedSite getOrCreatePinnedSite(String userId, String siteId, PortalNavState portalNavState) {

		PinnedSite pinnedSite = portalNavState.pinnedSitesBySiteId.get(siteId);
		if (pinnedSite == null) {
			pinnedSite = new PinnedSite(userId, siteId);
			portalNavState.pinnedSitesBySiteId.put(siteId, pinnedSite);
		}
		return pinnedSite;
	}

	private void persistPinnedSiteOrder(String userId, List<String> pinnedSiteIds, PortalNavState portalNavState) {

		for (int i = 0; i < pinnedSiteIds.size(); i++) {
			String siteId = pinnedSiteIds.get(i);
			PinnedSite pinnedSite = getOrCreatePinnedSite(userId, siteId, portalNavState);
			pinnedSite.setPosition(i);
			pinnedSite.setHasBeenUnpinned(false);
			portalNavState.unpinnedSiteIds.remove(siteId);
		}

		portalNavState.pinnedSiteIds.clear();
		portalNavState.pinnedSiteIds.addAll(pinnedSiteIds);
	}

	private void markSitesUnpinned(String userId, List<String> desiredPinnedSiteIds, PortalNavState portalNavState) {

		List<String> sitesToUnpin = new ArrayList<>(portalNavState.pinnedSiteIds);
		sitesToUnpin.removeIf(desiredPinnedSiteIds::contains);

		for (String siteId : sitesToUnpin) {
			PinnedSite pinnedSite = getOrCreatePinnedSite(userId, siteId, portalNavState);
			pinnedSite.setPosition(PinnedSite.UNPINNED_POSITION);
			pinnedSite.setHasBeenUnpinned(true);
			if (!portalNavState.unpinnedSiteIds.contains(siteId)) {
				portalNavState.unpinnedSiteIds.add(siteId);
			}
			addRecentSite(userId, siteId, portalNavState);
		}
	}

	private void addPinnedSite(String userId, String siteId, boolean isPinned, PortalNavState portalNavState) {
		List<String> pinnedSiteIds = new ArrayList<>(portalNavState.pinnedSiteIds);

		if (isPinned) {
			pinnedSiteIds.remove(siteId);
			pinnedSiteIds.add(siteId);
			savePinnedSites(userId, pinnedSiteIds, portalNavState);
			return;
		}

		PinnedSite pinnedSite = getOrCreatePinnedSite(userId, siteId, portalNavState);
		pinnedSite.setPosition(PinnedSite.UNPINNED_POSITION);
		pinnedSite.setHasBeenUnpinned(true);

		pinnedSiteIds.remove(siteId);
		if (!portalNavState.unpinnedSiteIds.contains(siteId)) {
			portalNavState.unpinnedSiteIds.add(siteId);
		}
		persistPinnedSiteOrder(userId, pinnedSiteIds, portalNavState);
		addRecentSite(userId, siteId, portalNavState);
	}

	private void addPinnedSite(String userId, String siteId, boolean isPinned, boolean createContextIfAbsent) {
		if (!isValidPinnedSiteRequest(userId, siteId)) {
			return;
		}

		mutatePortalNavState(userId, createContextIfAbsent, portalNavState -> addPinnedSite(userId, siteId, isPinned, portalNavState));
	}

	private void unpinSites(String userId, Collection<String> siteIds, PortalNavState portalNavState) {

		List<String> pinnedSiteIds = new ArrayList<>(portalNavState.pinnedSiteIds);
		List<String> newlyUnpinnedSiteIds = new ArrayList<>();

		for (String siteId : siteIds) {
			if (!isValidPinnedSiteRequest(userId, siteId)) {
				continue;
			}

			PinnedSite pinnedSite = getOrCreatePinnedSite(userId, siteId, portalNavState);
			pinnedSite.setPosition(PinnedSite.UNPINNED_POSITION);
			pinnedSite.setHasBeenUnpinned(true);

			pinnedSiteIds.remove(siteId);
			if (!portalNavState.unpinnedSiteIds.contains(siteId)) {
				portalNavState.unpinnedSiteIds.add(siteId);
			}
			newlyUnpinnedSiteIds.add(siteId);
		}

		if (newlyUnpinnedSiteIds.isEmpty()) {
			return;
		}

		persistPinnedSiteOrder(userId, pinnedSiteIds, portalNavState);
		newlyUnpinnedSiteIds.forEach(siteId -> addRecentSite(userId, siteId, portalNavState));
	}

	private void savePinnedSites(String userId, List<String> siteIds, PortalNavState portalNavState) {
		List<String> desiredPinnedSiteIds = normalizePinnedSiteIds(siteIds);

		List<String> currentPinnedSiteIds = new ArrayList<>(portalNavState.pinnedSiteIds);
		List<String> finalPinnedSiteIds = currentPinnedSiteIds.stream()
				.filter(desiredPinnedSiteIds::contains)
				.collect(Collectors.toCollection(ArrayList::new));

		desiredPinnedSiteIds.stream()
				.filter(Predicate.not(finalPinnedSiteIds::contains))
				.forEach(finalPinnedSiteIds::add);

		markSitesUnpinned(userId, desiredPinnedSiteIds, portalNavState);
		persistPinnedSiteOrder(userId, finalPinnedSiteIds, portalNavState);
	}

	private void savePinnedSitesForUser(String userId, List<String> siteIds, PortalNavState portalNavState) {
		List<String> desiredPinnedSiteIds = normalizePinnedSiteIds(siteIds);
		markSitesUnpinned(userId, desiredPinnedSiteIds, portalNavState);
		persistPinnedSiteOrder(userId, desiredPinnedSiteIds, portalNavState);
	}

	private List<String> normalizePinnedSiteIds(List<String> siteIds) {
		List<String> desiredPinnedSiteIds = siteIds == null ? new ArrayList<>() : new ArrayList<>(siteIds);
		desiredPinnedSiteIds.removeIf(siteId -> StringUtils.isBlank(siteId)
				|| siteService.isSpecialSite(siteId)
				|| siteService.isUserSite(siteId));

		int maxPinnedSites = serverConfigurationService.getInt("portal.max.pinned.sites", DEFAULT_MAX_PINNED_SITES);
		if (maxPinnedSites <= 0) {
			return new ArrayList<>();
		}
		if (desiredPinnedSiteIds.size() > maxPinnedSites) {
			desiredPinnedSiteIds = new ArrayList<>(desiredPinnedSiteIds.subList(
					desiredPinnedSiteIds.size() - maxPinnedSites,
					desiredPinnedSiteIds.size()));
		}

		List<String> normalizedPinnedSiteIds = new ArrayList<>();
		desiredPinnedSiteIds.stream()
				.filter(Predicate.not(normalizedPinnedSiteIds::contains))
				.forEach(normalizedPinnedSiteIds::add);
		return normalizedPinnedSiteIds;
	}

	private void removePinnedSiteFromPortalNavState(String userId, String siteId, PortalNavState portalNavState) {
		portalNavState.pinnedSiteIds.remove(siteId);
		portalNavState.unpinnedSiteIds.remove(siteId);
		portalNavState.pinnedSitesBySiteId.remove(siteId);
		persistPinnedSiteOrder(userId, new ArrayList<>(portalNavState.pinnedSiteIds), portalNavState);
	}

	private void removePinnedSite(String userId, String siteId, boolean createContextIfAbsent) {
		if (StringUtils.isBlank(userId)) {
			return;
		}

		mutatePortalNavState(userId, createContextIfAbsent,
				portalNavState -> removePinnedSiteFromPortalNavState(userId, siteId, portalNavState));
	}

	private void removeSitesFromPortalNavState(List<String> siteIds, PortalNavState portalNavState) {
		siteIds.forEach(siteId -> {
			portalNavState.pinnedSiteIds.remove(siteId);
			portalNavState.unpinnedSiteIds.remove(siteId);
			portalNavState.pinnedSitesBySiteId.remove(siteId);
			portalNavState.recentSiteIds.remove(siteId);
			portalNavState.recentSitesBySiteId.remove(siteId);
		});

		for (int i = 0; i < portalNavState.pinnedSiteIds.size(); i++) {
			PinnedSite pinnedSite = portalNavState.pinnedSitesBySiteId.get(portalNavState.pinnedSiteIds.get(i));
			if (pinnedSite != null) {
				pinnedSite.setPosition(i);
				pinnedSite.setHasBeenUnpinned(false);
			}
		}
	}

	private boolean isValidPinnedSiteRequest(String userId, String siteId) {
		return StringUtils.isNoneBlank(userId, siteId) && !siteService.isUserSite(siteId);
	}

	@Transactional
	@Override
	public void addPinnedSite(final String userId, final String siteId, final boolean isPinned) {
		addPinnedSite(userId, siteId, isPinned, true);
	}

	private boolean isSiteUnpinnedByUser(String userId, String siteId) {
		UserPortalNavContext context = portalNavContexts.get(userId);
		if (context != null) {
			synchronized (context) {
				PinnedSite pinnedSite = context.portalNavState.pinnedSitesBySiteId.get(siteId);
				return pinnedSite != null && Boolean.TRUE.equals(pinnedSite.getHasBeenUnpinned());
			}
		}

		return pinnedSiteRepository.findByUserIdAndSiteId(userId, siteId)
				.map(PinnedSite::getHasBeenUnpinned)
				.orElse(false);
	}

	private boolean hasRecentSite(String userId, String siteId) {
		UserPortalNavContext context = portalNavContexts.get(userId);
		if (context != null) {
			synchronized (context) {
				return context.portalNavState.recentSitesBySiteId.containsKey(siteId);
			}
		}

		return recentSiteRepository.findByUserId(userId).stream()
				.anyMatch(recentSite -> StringUtils.equals(recentSite.getSiteId(), siteId));
	}

	@Transactional
	@Override
	public void removePinnedSite(String userId, String siteId) {
		removePinnedSite(userId, siteId, true);
	}

	@Transactional
	@Override
	public void savePinnedSites(String userId, List<String> siteIds) {
		if (StringUtils.isBlank(userId) || siteIds == null) return;

		mutatePortalNavState(userId, true, portalNavState -> savePinnedSites(userId, siteIds, portalNavState));
	}

	@Transactional
	@Override
	public void reorderPinnedSites(String userId, List<String> siteIds) {
		if (StringUtils.isBlank(userId)) return;

		mutatePortalNavState(userId, true, portalNavState -> {
			List<String> siteIdsToPersist = siteIds;
			if (serverConfigurationService.getBoolean("portal.new.pinned.sites.top", false)) {
				List<String> reversedSiteIds = new ArrayList<>(siteIds);
				Collections.reverse(reversedSiteIds);
				siteIdsToPersist = reversedSiteIds;
			}
			savePinnedSitesForUser(userId, siteIdsToPersist, portalNavState);
		});
	}

	@Override
	public List<String> getPinnedSites() {
		String userId = sessionManager.getCurrentSessionUserId();
		return getPinnedSites(userId);
	}

	@Override
	public List<String> getPinnedSites(String userId) {
		if (StringUtils.isBlank(userId)) return Collections.emptyList();

		UserPortalNavContext context = getOrCreatePortalNavContext(userId);
		List<String> pinned;
		synchronized (context) {
			context.lastAccess = System.currentTimeMillis();
			pinned = new ArrayList<>(context.portalNavState.pinnedSiteIds);
		}
		if (serverConfigurationService.getBoolean("portal.new.pinned.sites.top", false)) {
			Collections.reverse(pinned);
		}
		return Collections.unmodifiableList(pinned);
	}

	@Override
	public List<String> getUnpinnedSites() {
		String userId = sessionManager.getCurrentSessionUserId();
		return getUnpinnedSites(userId);
	}

	@Override
	public List<String> getUnpinnedSites(String userId) {
		if (StringUtils.isBlank(userId)) return Collections.emptyList();

		UserPortalNavContext context = getOrCreatePortalNavContext(userId);
		synchronized (context) {
			context.lastAccess = System.currentTimeMillis();
			return Collections.unmodifiableList(new ArrayList<>(context.portalNavState.unpinnedSiteIds));
		}
	}

	@Override
	public List<String> getRecentSites(String userId) {
		if (StringUtils.isBlank(userId)) return Collections.emptyList();

		UserPortalNavContext context = getOrCreatePortalNavContext(userId);
		synchronized (context) {
			context.lastAccess = System.currentTimeMillis();
			return Collections.unmodifiableList(new ArrayList<>(context.portalNavState.recentSiteIds));
		}
	}

	@Transactional
	@Override
	public void addRecentSite(String userId, String siteId) {
		if (StringUtils.isBlank(userId)) {
			return;
		}

		mutatePortalNavState(userId, true, portalNavState -> addRecentSite(userId, siteId, portalNavState));
	}

	@Transactional
	@Override
	public void removeSitesfromPinnedAndRecent(String userId, List<String> siteIds) {

		if (StringUtils.isBlank(userId) || siteIds == null || siteIds.isEmpty()) return;

		mutatePortalNavState(userId, true, portalNavState -> removeSitesFromPortalNavState(siteIds, portalNavState));
	}

	private void removeRecentSite(String userId, String siteId, boolean createContextIfAbsent) {
		if (StringUtils.isAnyBlank(userId, siteId)) return;

		mutatePortalNavState(userId, createContextIfAbsent, portalNavState -> removeRecentSiteFromPortalNavState(siteId, portalNavState));
	}

	@Transactional
	@Override
	public void removeRecentSite(String userId, String siteId) {
		removeRecentSite(userId, siteId, true);
	}

	@Override
	@Transactional
	public void syncUserSitesWithPortalNav(final String userId) {
		if (StringUtils.isBlank(userId) || securityService.isSuperUser(userId)) return;

		UserPortalNavContext context = getOrCreatePortalNavContext(userId);
		synchronized (context) {
			context.lastAccess = System.currentTimeMillis();
			if (syncUserSitesWithPortalNavInternal(userId, context.portalNavState)) {
				context.removeFavoriteSiteDataAfterFlush = true;
			}
			context.dirty = true;
			context.version++;
		}

		schedulePortalNavFlush(context);
	}

	private boolean syncUserSitesWithPortalNavInternal(final String userId, PortalNavState portalNavState) {

		List<String> excludedSites = Collections.emptyList();
		List<String> favoriteSiteIds = Collections.emptyList();
		List<String> seenSiteIds = Collections.emptyList();
		boolean removeFavoriteSiteDataAfterFlush = false;

		// get all site data from preferences
		Preferences prefs = preferencesService.getPreferences(userId);
		if (prefs != null) {
			ResourceProperties props = prefs.getProperties(PreferencesService.SITENAV_PREFS_KEY);
			excludedSites = Optional.ofNullable(props.getPropertyList(PreferencesService.SITENAV_PREFS_EXCLUDE_KEY)).orElse(excludedSites);
			favoriteSiteIds = Optional.ofNullable(props.getPropertyList(FAVORITES_PROPERTY)).orElse(favoriteSiteIds);
			seenSiteIds = Optional.ofNullable(props.getPropertyList(SEEN_SITES_PROPERTY)).orElse(seenSiteIds);
		}

		List<String> pinnedSites = new ArrayList<>(portalNavState.pinnedSiteIds);
		List<String> unPinnedSites =  new ArrayList<>(portalNavState.unpinnedSiteIds);
		List<String> recentSites = new ArrayList<>(portalNavState.recentSiteIds);

		List<String> pinnedList = new ArrayList<>();
		Set<String> pinnedSiteIds = new HashSet<>();
		Set<String> sitesToUnpin = new HashSet<>(unPinnedSites);
		Set<String> sitesToRemove = new HashSet<>(excludedSites);
		Set<String> combinedSiteIds = new LinkedHashSet<>(excludedSites);
		combinedSiteIds.addAll(pinnedSites);
		combinedSiteIds.addAll(unPinnedSites);
		combinedSiteIds.addAll(recentSites);

		// if the user has favorites data in preferences lets migrate
		if (!favoriteSiteIds.isEmpty() || !seenSiteIds.isEmpty()) {
			log.debug("Found favorites data performing migration for user [{}]", userId);
			log.debug("Adding {} sites from favorites to pinned sites for user [{}]", favoriteSiteIds.size(), userId);
			combinedSiteIds.addAll(favoriteSiteIds);

			seenSiteIds.stream()
					.filter(Predicate.not(favoriteSiteIds::contains))
					.forEach(sitesToUnpin::add);

			log.debug("Adding {} sites from unseen to unpinned sites for user [{}]", seenSiteIds.size(), userId);
			combinedSiteIds.addAll(sitesToUnpin);
			removeFavoriteSiteDataAfterFlush = true;
		}

		List<String> userSiteIds = siteService.getSiteIds(SiteService.SelectionType.MEMBER, null, null, null,
				null, SiteService.SortType.NONE, null, userId);
		combinedSiteIds.addAll(userSiteIds);

		Set<String> accessibleSiteIds = new HashSet<>(siteService.getSiteIds(SiteService.SelectionType.ACCESS, null, null, null,
				null, SiteService.SortType.NONE, null, userId));
		for (String id : favoriteSiteIds) {
			if (accessibleSiteIds.contains(id) && !sitesToUnpin.contains(id) && pinnedSiteIds.add(id)) {
				pinnedList.add(id);
			}
		}
		for (String id : combinedSiteIds) {
			if (accessibleSiteIds.contains(id)) {
				if (!sitesToUnpin.contains(id) && pinnedSiteIds.add(id)) {
					pinnedList.add(id);
				}
			}
			else sitesToRemove.add(id);
		}

		savePinnedSites(userId, pinnedList, portalNavState);

		sitesToUnpin.removeAll(portalNavState.unpinnedSiteIds);
		unpinSites(userId, sitesToUnpin, portalNavState);

		combinedSiteIds.stream()
				.filter(siteService::isSpecialSite)
				.forEach(sitesToRemove::add);

		removeSitesFromPortalNavState(new ArrayList<>(sitesToRemove), portalNavState);
		return removeFavoriteSiteDataAfterFlush;
	}

	private static PinnedSite copyPinnedSite(PinnedSite pinnedSite) {
		PinnedSite copy = new PinnedSite();
		copy.setId(pinnedSite.getId());
		copy.setUserId(pinnedSite.getUserId());
		copy.setSiteId(pinnedSite.getSiteId());
		copy.setPosition(pinnedSite.getPosition());
		copy.setHasBeenUnpinned(pinnedSite.getHasBeenUnpinned());
		return copy;
	}

	private static RecentSite copyRecentSite(RecentSite recentSite) {
		RecentSite copy = new RecentSite();
		copy.setId(recentSite.getId());
		copy.setUserId(recentSite.getUserId());
		copy.setSiteId(recentSite.getSiteId());
		copy.setCreated(recentSite.getCreated());
		return copy;
	}

	private static PortalNavState copyPortalNavState(PortalNavState portalNavState) {
		Map<String, PinnedSite> pinnedSitesBySiteId = new HashMap<>();
		portalNavState.pinnedSitesBySiteId.forEach((siteId, pinnedSite) -> pinnedSitesBySiteId.put(siteId, copyPinnedSite(pinnedSite)));

		Map<String, RecentSite> recentSitesBySiteId = new HashMap<>();
		portalNavState.recentSitesBySiteId.forEach((siteId, recentSite) -> recentSitesBySiteId.put(siteId, copyRecentSite(recentSite)));

		return new PortalNavState(
				pinnedSitesBySiteId,
				new ArrayList<>(portalNavState.pinnedSiteIds),
				new ArrayList<>(portalNavState.unpinnedSiteIds),
				recentSitesBySiteId,
				new ArrayList<>(portalNavState.recentSiteIds));
	}

	private static void mergePersistedStateIdentifiers(PortalNavState target, PortalNavState persisted) {
		persisted.pinnedSitesBySiteId.forEach((siteId, pinnedSite) -> {
			PinnedSite existing = target.pinnedSitesBySiteId.get(siteId);
			if (existing != null) {
				existing.setId(pinnedSite.getId());
			}
		});
		persisted.recentSitesBySiteId.forEach((siteId, recentSite) -> {
			RecentSite existing = target.recentSitesBySiteId.get(siteId);
			if (existing != null) {
				existing.setId(recentSite.getId());
			}
		});
	}

	private static final class PortalNavState {

		private final Map<String, PinnedSite> pinnedSitesBySiteId;
		private final List<String> pinnedSiteIds;
		private final List<String> unpinnedSiteIds;
		private final Map<String, RecentSite> recentSitesBySiteId;
		private final List<String> recentSiteIds;

		private PortalNavState(Map<String, PinnedSite> pinnedSitesBySiteId, List<String> pinnedSiteIds,
				List<String> unpinnedSiteIds, Map<String, RecentSite> recentSitesBySiteId, List<String> recentSiteIds) {
			this.pinnedSitesBySiteId = pinnedSitesBySiteId;
			this.pinnedSiteIds = pinnedSiteIds;
			this.unpinnedSiteIds = unpinnedSiteIds;
			this.recentSitesBySiteId = recentSitesBySiteId;
			this.recentSiteIds = recentSiteIds;
		}
	}

	private static final class UserPortalNavContext {

		private final String userId;
		private final PortalNavState portalNavState;
		private long lastAccess = System.currentTimeMillis();
		private boolean dirty;
		private boolean removeFavoriteSiteDataAfterFlush;
		private boolean flushInProgress;
		private boolean flushRequested;
		private boolean evictAfterFlush;
		private long version;
		private ScheduledFuture<?> scheduledFlush;

		private UserPortalNavContext(String userId, PortalNavState portalNavState) {
			this.userId = userId;
			this.portalNavState = portalNavState;
		}
	}

	private void removeFavoriteSiteData(String userId) {
		preferencesService.applyEditWithAutoCommit(userId, edit -> {
			ResourcePropertiesEdit props = edit.getPropertiesEdit(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);
			log.debug("Clearing favorites data from preferences for user [{}]", userId);
			props.removeProperty(FIRST_TIME_PROPERTY);
			props.removeProperty(SEEN_SITES_PROPERTY);
			props.removeProperty(FAVORITES_PROPERTY);
		});
	}

	@Override
	public void registerSubPageNavProvider(PortalSubPageNavProvider portalSubPageNavProvider) {
		if (portalSubPageNavProvider != null) {
			Collection<PortalSubPageNavProvider> providers = new HashSet<>(portalSubPageNavProviders);
			if (providers.contains(portalSubPageNavProvider)) {
				log.debug("Overriding existing SubPageNavProvider [{}]", portalSubPageNavProvider.getName());
			} else {
				log.debug("Registering new SubPageNavProvider [{}]", portalSubPageNavProvider.getName());
			}
			providers.add(portalSubPageNavProvider);
			portalSubPageNavProviders = providers;
		}
	}

	@Override
	public String getSubPageData(String name, String siteId, String userId, Collection<String> pageIds) {
		for (PortalSubPageNavProvider portalSubPageNavProvider : portalSubPageNavProviders) {
			if (portalSubPageNavProvider.getName().equals(name)) {
				String data = portalSubPageNavProvider.getData(siteId, userId, pageIds);
				log.debug("Retrieved sub page nav data from provider [{}], data={}", name, data);
				return data;
			}
		}
		return StringUtils.EMPTY;
	}

}
