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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.sakaiproject.portal.api.model.PinnedSite;
import org.sakaiproject.portal.api.model.RecentSite;
import org.sakaiproject.portal.api.repository.PinnedSiteRepository;
import org.sakaiproject.portal.api.repository.RecentSiteRepository;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortalServiceImpl implements PortalService, Observer
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
	@Setter private SecurityService securityService;
	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private SessionManager sessionManager;
	@Setter private SiteNeighbourhoodService siteNeighbourhoodService;
	@Setter private SiteService siteService;

	private Map<String, Map<String, PortalHandler>> handlerMaps = new ConcurrentHashMap<>();
	private Editor noopEditor = new BaseEditor("noop", "noop", "", "");
	private Map<String, Portal> portals = new ConcurrentHashMap<>();
	private Map<String, PortalRenderEngine> renderEngines = new ConcurrentHashMap<>();

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
					syncUserSitesWithFavoriteSites(userId);
				}
				break;
			}
			case SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD: {
				String userInfo = event.getResource();
				String[] parts = userInfo.split(";");
				if (parts.length > 1) {
					String userPart = parts[0];
					String[] userParts = userPart.split("=");
					if (userParts.length == 2) {
						String userId = userParts[1];
						if (canUserUpdateSite(userId, event.getContext()) || isUserActiveMemberInPublishedSite(userId, event.getContext())) {
							addPinnedSite(userId, event.getContext(), true);
						}
					}
				}
				break;
			}
			case SiteService.EVENT_SITE_PUBLISH: {
				final String siteId = siteService.idFromSiteReference(event.getResource());
				try {
					AuthzGroup azg = authzGroupService.getAuthzGroup(event.getResource());
					azg.getUsers().stream()
							.filter(u -> !isSiteUnpinnedByUser(u, siteId) && (canUserUpdateSite(u, siteId) || isUserActiveMemberInSite(u, siteId)))
							.forEach(u -> addPinnedSite(u, siteId, true));
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
								removePinnedSite(u, siteId);
							}

							List<RecentSite> recentSites = recentSiteRepository.findByUserId(u);
							for (RecentSite recentSite : recentSites) {
								if (StringUtils.equals(recentSite.getSiteId(), siteId)) {
									recentSiteRepository.deleteByUserIdAndSiteId(u, siteId);
									break;
								}
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

				if (recentUserIds.isEmpty() && pinnedUserIds.isEmpty()) {
					return;
				}

				try {
					Site site = siteService.getSite(event.getContext());
					Set<String> siteUsers = site.getUsers();

					pinnedUserIds.forEach(userId -> {
						if (!siteUsers.contains(userId)) {
							pinnedSiteRepository.deleteByUserIdAndSiteId(userId, event.getContext());
						}
					});
					recentUserIds.forEach(userId -> {
						if (!siteUsers.contains(userId)) {
							recentSiteRepository.deleteByUserIdAndSiteId(userId, event.getContext());
						}
					});
					siteUsers.forEach(userId -> {
						if (!pinnedUserIds.contains(userId)
								&& (canUserUpdateSite(userId, event.getContext()) || isUserActiveMemberInPublishedSite(userId, event.getContext()))) {
							addPinnedSite(userId, event.getContext(), true);
						}
					});
				} catch (IdUnusedException idue) {
					log.warn("No site for {} while cleaning up pinned sites, {}", event.getContext(), idue.toString());
				}
				break;
			}
			case SiteService.SOFT_DELETE_SITE: {
				pinnedSiteRepository.deleteBySiteId(event.getContext());
				recentSiteRepository.deleteBySiteId(event.getContext());
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

	@Transactional
	@Override
	public void addPinnedSite(final String userId, final String siteId, final boolean isPinned) {

		if (StringUtils.isBlank(userId)) return;

		PinnedSite pin = pinnedSiteRepository.findByUserIdAndSiteId(userId, siteId)
			.orElseGet(() -> new PinnedSite(userId, siteId));


		int position = PinnedSite.UNPINNED_POSITION;
		if (isPinned) {
			List<PinnedSite> pinned = pinnedSiteRepository.findByUserIdOrderByPosition(userId);
			position = !pinned.isEmpty() ? pinned.get(pinned.size() - 1).getPosition() + 1 : 1;
		}

		pin.setPosition(position);
		pin.setHasBeenUnpinned(!isPinned);
		pinnedSiteRepository.save(pin);

		if (!isPinned) {
			addRecentSite(siteId);
			List<PinnedSite> pinnedSites = pinnedSiteRepository.findByUserIdOrderByPosition(userId);
			for (int i = 0; i < pinnedSites.size(); i++) {
				PinnedSite pinnedSite = pinnedSites.get(i);
				pinnedSite.setPosition(i);
			}
		}
	}

	private boolean isSiteUnpinnedByUser(String userId, String siteId) {

		// Only return true if a pinned site record is found, and it explicitly hasBeenUnpinned
		return pinnedSiteRepository.findByUserIdAndSiteId(userId, siteId)
				.map(PinnedSite::getHasBeenUnpinned)
				.orElse(false);
	}

	@Transactional
	@Override
	public void removePinnedSite(String userId, String siteId) {

		if (StringUtils.isBlank(userId)) {
			return;
		}

		pinnedSiteRepository.deleteByUserIdAndSiteId(userId, siteId);

		List<PinnedSite> pinnedSites = pinnedSiteRepository.findByUserIdOrderByPosition(userId);
		for (int i = 0; i < pinnedSites.size(); i++) {
			PinnedSite pinnedSite = pinnedSites.get(i);
			pinnedSite.setPosition(i);
		}
	}

	@Transactional
	@Override
	public void savePinnedSites(List<String> siteIds) {

		String userId = sessionManager.getCurrentSessionUserId();

		if (StringUtils.isBlank(userId)) {
			return;
		}

		// We never want the user's home site to be pinned. This is just a backup for that as the 
		// UI should not be presenting a pin icon for the home site.
		siteIds.remove(siteService.getUserSiteId(userId));

		List<String> currentPinned = getPinnedSites();
		currentPinned.forEach(cp -> {

			// Check if the user has unpinned a previously pinned site
			if (!siteIds.contains(cp)) {
				addPinnedSite(userId, cp, false);
			}
		});

		// We've unpinned, now removed the currently pinned from the requested siteIds. This
		// will leave only the newly requested sites.
		siteIds.removeAll(currentPinned);

		// Now, siteIds will contain only the newly pinned sites, so we can add them on the top
		// of the remaining pinned sites.
		int start = getPinnedSites().size();
		for (int i = 0; i < siteIds.size(); i++) {
			String siteId = siteIds.get(i);
			PinnedSite pin = pinnedSiteRepository.findByUserIdAndSiteId(userId, siteId).orElseGet(() -> new PinnedSite(userId, siteId));
			pin.setPosition(i + start);
			pin.setHasBeenUnpinned(false);
			pinnedSiteRepository.save(pin);
		}
	}

	@Transactional
	@Override
	public void reorderPinnedSites(List<String> siteIds) {

		String userId = sessionManager.getCurrentSessionUserId();

		if (StringUtils.isBlank(userId)) {
			return;
		}

		pinnedSiteRepository.deleteByUserId(userId);

		for (int i = 0; i < siteIds.size(); i++) {

			PinnedSite pin = new PinnedSite();
			pin.setUserId(userId);
			pin.setSiteId(siteIds.get(i));
			pin.setPosition(i);
			pinnedSiteRepository.save(pin);
		}
	}

	@Override
	public List<String> getPinnedSites() {
		String userId = sessionManager.getCurrentSessionUserId();
		return getPinnedSites(userId);
	}

	@Override
	public List<String> getPinnedSites(String userId) {
		if (StringUtils.isBlank(userId)) return Collections.emptyList();

		return pinnedSiteRepository.findByUserIdOrderByPosition(userId).stream()
				.map(PinnedSite::getSiteId)
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public List<String> getUnpinnedSites() {
		String userId = sessionManager.getCurrentSessionUserId();
		return getUnpinnedSites(userId);
	}

	@Override
	public List<String> getUnpinnedSites(String userId) {
		if (StringUtils.isBlank(userId)) return Collections.emptyList();

		return pinnedSiteRepository.findByUserIdAndHasBeenUnpinnedOrderByPosition(userId, true).stream()
				.map(PinnedSite::getSiteId)
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public List<String> getRecentSites() {

		String userId = sessionManager.getCurrentSessionUserId();

		if (StringUtils.isBlank(userId)) {
			return Collections.<String>emptyList();
		}

		return recentSiteRepository.findByUserId(userId).stream()
				.map(RecentSite::getSiteId)
				.collect(Collectors.toUnmodifiableList());
	}

	@Transactional
	@Override
	public void addRecentSite(String siteId) {

		if (SiteService.SITE_ERROR.equals(siteId)) {
			return;
		}

		String userId = sessionManager.getCurrentSessionUserId();

		if (StringUtils.isBlank(userId)
				|| siteId.equals(siteService.getUserSiteId(userId))
				|| !(canUserUpdateSite(userId, siteId) || isUserActiveMemberInPublishedSite(userId, siteId))) {
			return;
		}

		recentSiteRepository.deleteByUserIdAndSiteId(userId, siteId);

		List<String> current = getRecentSites();

		if (current.size() == 3) {
			// Oldest is last
			String last = current.toArray(new String[] {})[current.size() - 1];
			recentSiteRepository.deleteByUserIdAndSiteId(userId, last);
		}

		RecentSite recentSite = new RecentSite();
		recentSite.setUserId(userId);
		recentSite.setSiteId(siteId);
		recentSite.setCreated(Instant.now());
		recentSiteRepository.save(recentSite);
	}

	private void syncUserSitesWithFavoriteSites(final String userId) {
		if (StringUtils.isBlank(userId) || securityService.isSuperUser(userId)) return;

		List<String> excludedSites = Collections.emptyList();
		List<String> favoriteSiteIds = Collections.emptyList();
		List<String> seenSiteIds = Collections.emptyList();

		Preferences prefs = preferencesService.getPreferences(userId);
		if (prefs != null) {
			ResourceProperties props = prefs.getProperties(PreferencesService.SITENAV_PREFS_KEY);
			excludedSites = Optional.ofNullable(props.getPropertyList(PreferencesService.SITENAV_PREFS_EXCLUDE_KEY)).orElse(excludedSites);
			favoriteSiteIds = Optional.ofNullable(props.getPropertyList(FAVORITES_PROPERTY)).orElse(favoriteSiteIds);
			seenSiteIds = Optional.ofNullable(props.getPropertyList(SEEN_SITES_PROPERTY)).orElse(seenSiteIds);
		}

		List<String> pinnedSites = getPinnedSites(userId);
		List<String> unPinnedSites =  getUnpinnedSites(userId);
		Set<String> combinedSiteIds = Stream.concat(pinnedSites.stream(), unPinnedSites.stream()).collect(Collectors.toSet());

		// when the user has no sites it is most likely their first login since pinning was introduced
		if (combinedSiteIds.isEmpty()) {
			log.debug("User has no pinned site data performing favorites migration for user [{}]", userId);
			// look to the users favorites stored in preferences
			favoriteSiteIds.stream()
					.filter(siteId -> canAccessSite(siteId, userId))
					.forEach(siteId -> {
						log.debug("Adding site [{}] from favorites to pinned sites for user [{}]", siteId, userId);
						addPinnedSite(userId, siteId, true);
						combinedSiteIds.add(siteId);
					});

			seenSiteIds.stream()
					.filter(Predicate.not(favoriteSiteIds::contains))
					.filter(siteId -> canAccessSite(siteId, userId))
					.forEach(siteId -> {
						log.debug("Adding site [{}] from unseen to unpinned sites for user [{}]", siteId, userId);
						addPinnedSite(userId, siteId, false);
						combinedSiteIds.add(siteId);
					});

			if (!favoriteSiteIds.isEmpty() || !seenSiteIds.isEmpty()) {
				removeFavoriteSiteData(userId);
			}
		}

		// Remove newly hidden sites from pinned and unpinned sites
		combinedSiteIds.stream().filter(excludedSites::contains).forEach(siteId -> removePinnedSite(userId, siteId));
		combinedSiteIds.addAll(excludedSites);

		// This should not call getUserSites(boolean, boolean) because the property is variable, while the call is cacheable otherwise
		List<String> userSiteIds = siteService.getSiteIds(SiteService.SelectionType.MEMBER, null, null, null, SiteService.SortType.CREATED_ON_DESC, null);
		userSiteIds.stream()
				.filter(Predicate.not(combinedSiteIds::contains))
				.filter(siteId -> canAccessSite(siteId, userId))
				.peek(id -> log.debug("Adding pinned site [{}] for user [{}]", id, userId))
				.forEach(id -> addPinnedSite(userId, id, true));
	}

	/**
	 * Check that the user can access the site
	 *
	 * @param siteId the id of the site
	 * @param userId the id of the user
	 * @return true if access is allowed to the site, otherwise false
	 */
	private boolean canAccessSite(String siteId, String userId) {
		boolean access = false;
		try {
			// use getSiteVisit as it performs proper access checks
			Site site = siteService.getSiteVisit(siteId);
			access = site.getMember(userId).isActive() || site.isAllowed(userId, SiteService.SECURE_UPDATE_SITE);
		} catch (IdUnusedException | PermissionException e) {
			log.debug("User [{}] doesn't have access to site [{}], {}", userId, siteId, e.toString());
		}
		return access;
	}

	private void removeFavoriteSiteData(String userId) {
		PreferencesEdit edit = null;
		try {
			edit = preferencesService.edit(userId);
		} catch (Exception e) {
			log.warn("Could not get the preferences for user [{}], {}", userId, e.toString());
		}

		if (edit != null) {
			try {
				ResourcePropertiesEdit props = edit.getPropertiesEdit(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);
				log.debug("Clearing favorites data from preferences for user [{}]", userId);

				props.removeProperty(FIRST_TIME_PROPERTY);
				props.removeProperty(SEEN_SITES_PROPERTY);
				props.removeProperty(FAVORITES_PROPERTY);
			} catch (Exception e) {
				log.warn("Could not remove favorites data for user [{}], {}", userId, e.toString());
				preferencesService.cancel(edit);
				edit = null; // set to null since it was cancelled, prevents commit in finally
			} finally {
				if (edit != null) preferencesService.commit(edit);
			}
		}
	}

}
