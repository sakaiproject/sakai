package org.sakaiproject.portal.charon.site;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.mockito.MockedStatic;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;

public class PortalSiteHelperImplTest extends TestCase {

	public void testGetContextSitesWithPagesBulkLoadsSitePermissions() throws Exception {

		AuthzGroupService authzGroupService = mock(AuthzGroupService.class);
		SecurityService securityService = mock(SecurityService.class);
		ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
		PortalService portalService = mock(PortalService.class);
		PreferencesService preferencesService = mock(PreferencesService.class);
		SessionManager sessionManager = mock(SessionManager.class);
		SiteNeighbourhoodService siteNeighbourhoodService = mock(SiteNeighbourhoodService.class);
		SiteService siteService = mock(SiteService.class);
		UserDirectoryService userDirectoryService = mock(UserDirectoryService.class);
		ResourceProperties siteNavProperties = mock(ResourceProperties.class);
		Preferences preferences = mock(Preferences.class);
		User user = mock(User.class);

		String homeSiteRef = "/site/~student1";
		String site1Ref = "/site/site-1";
		String site2Ref = "/site/site-2";
		String instructorMarker = "section.role.instructor";
		Site homeSite = mockSite("~student1", "Home");
		Site site1 = mockSite("site-1", "Site 1");
		Site site2 = mockSite("site-2", "Site 2");
		List<String> siteRefs = List.of(homeSiteRef, site1Ref, site2Ref);

		when(securityService.isSuperUser()).thenReturn(false);
		when(securityService.isUserRoleSwapped()).thenReturn(false);
		when(sessionManager.getCurrentSessionUserId()).thenReturn("student1");
		when(siteService.getUserSiteId("student1")).thenReturn("~student1");
		when(siteService.getSite("~student1")).thenReturn(homeSite);
		when(siteService.getSite("site-1")).thenReturn(site1);
		when(siteService.getSite("site-2")).thenReturn(site2);
		when(siteService.isUserSite(anyString())).thenReturn(false);
		when(siteNeighbourhoodService.parseSiteAlias(anyString())).thenReturn(null);
		when(preferencesService.getPreferences("student1")).thenReturn(preferences);
		when(preferences.getProperties(PreferencesService.SITENAV_PREFS_KEY)).thenReturn(siteNavProperties);
		when(siteNavProperties.getPropertyList("exclude")).thenReturn(Collections.emptyList());
		when(portalService.getPinnedSites("student1")).thenReturn(List.of("site-1", "site-2"));
		when(portalService.getRecentSites("student1")).thenReturn(Collections.emptyList());
		when(serverConfigurationService.getString("portal.includesubsites", "false")).thenReturn("false");
		when(authzGroupService.getAuthzGroupsIsAllowed("student1", SiteService.SECURE_UPDATE_SITE, siteRefs))
			.thenReturn(Set.of(site1Ref));
		when(authzGroupService.getAuthzGroupsIsAllowed("student1", instructorMarker, siteRefs))
			.thenReturn(Collections.emptySet());
		when(authzGroupService.getRoleFunctions(siteRefs)).thenReturn(Collections.emptyMap());
		when(userDirectoryService.getCurrentUser()).thenReturn(user);
		when(user.getId()).thenReturn("student1");

		try (MockedStatic<ComponentManager> componentManager = mockStatic(ComponentManager.class)) {
			componentManager.when(ComponentManager::isTestingMode).thenReturn(true);
			componentManager.when(() -> ComponentManager.get(AliasService.class)).thenReturn(mock(AliasService.class));
			componentManager.when(() -> ComponentManager.get(AuthzGroupService.class)).thenReturn(authzGroupService);
			componentManager.when(() -> ComponentManager.get(EntityManager.class)).thenReturn(mock(EntityManager.class));
			componentManager.when(() -> ComponentManager.get(FormattedText.class)).thenReturn(mock(FormattedText.class));
			componentManager.when(() -> ComponentManager.get(PortalService.class)).thenReturn(portalService);
			componentManager.when(() -> ComponentManager.get(PreferencesService.class)).thenReturn(preferencesService);
			componentManager.when(() -> ComponentManager.get(SecurityService.class)).thenReturn(securityService);
			componentManager.when(() -> ComponentManager.get(ServerConfigurationService.class)).thenReturn(serverConfigurationService);
			componentManager.when(() -> ComponentManager.get(SessionManager.class)).thenReturn(sessionManager);
			componentManager.when(() -> ComponentManager.get(SiteNeighbourhoodService.class)).thenReturn(siteNeighbourhoodService);
			componentManager.when(() -> ComponentManager.get(SiteService.class)).thenReturn(siteService);
			componentManager.when(() -> ComponentManager.get(ThreadLocalManager.class)).thenReturn(mock(ThreadLocalManager.class));
			componentManager.when(() -> ComponentManager.get(ToolManager.class)).thenReturn(mock(ToolManager.class));
			componentManager.when(() -> ComponentManager.get(UserDirectoryService.class)).thenReturn(userDirectoryService);

			PortalSiteHelperImpl helper = new PortalSiteHelperImpl(mock(Portal.class), false);
			Map<String, Object> contextSites = helper.getContextSitesWithPages(mock(HttpServletRequest.class), "site-1", null, true);

			assertNotNull(contextSites.get("homeSite"));
			verify(authzGroupService).getAuthzGroupsIsAllowed("student1", SiteService.SECURE_UPDATE_SITE, siteRefs);
			verify(authzGroupService).getAuthzGroupsIsAllowed("student1", instructorMarker, siteRefs);
			verify(authzGroupService).getRoleFunctions(siteRefs);
			verify(securityService, never()).unlock(eq(SiteService.SECURE_UPDATE_SITE), anyString());
			verify(securityService, never()).unlock(eq(instructorMarker), anyString());
		}
	}

	public void testGetContextSitesWithPagesBulkLoadsNonMaintainerRoleFunctions() throws Exception {

		AuthzGroupService authzGroupService = mock(AuthzGroupService.class);
		SecurityService securityService = mock(SecurityService.class);
		ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
		PortalService portalService = mock(PortalService.class);
		PreferencesService preferencesService = mock(PreferencesService.class);
		SessionManager sessionManager = mock(SessionManager.class);
		SiteNeighbourhoodService siteNeighbourhoodService = mock(SiteNeighbourhoodService.class);
		SiteService siteService = mock(SiteService.class);
		ToolManager toolManager = mock(ToolManager.class);
		UserDirectoryService userDirectoryService = mock(UserDirectoryService.class);
		ResourceProperties siteNavProperties = mock(ResourceProperties.class);
		Preferences preferences = mock(Preferences.class);
		User user = mock(User.class);

		String homeSiteRef = "/site/~student1";
		String site1Ref = "/site/site-1";
		Site homeSite = mockSite("~student1", "Home");
		Site site1 = mockSite("site-1", "Site 1");
		SitePage page = mock(SitePage.class);
		ToolConfiguration toolConfiguration = mock(ToolConfiguration.class);
		List<String> siteRefs = List.of(homeSiteRef, site1Ref);

		when(site1.getOrderedPages()).thenReturn(List.of(page));
		when(page.getTools()).thenReturn(List.of(toolConfiguration));
		when(page.getUrl()).thenReturn("/portal/site/site-1/page/page-1");
		when(page.getId()).thenReturn("page-1");
		when(page.getTitle()).thenReturn("Assignments");
		when(page.isPopUp()).thenReturn(false);
		when(toolConfiguration.getId()).thenReturn("tool-1");
		when(toolConfiguration.getToolId()).thenReturn("sakai.assignment.grades");
		when(toolConfiguration.getConfig()).thenReturn(new Properties());
		when(toolManager.allowTool(site1, toolConfiguration)).thenReturn(true);
		when(toolManager.isHidden(toolConfiguration)).thenReturn(false);
		when(toolManager.getRequiredPermissions(toolConfiguration)).thenReturn(List.of(Set.of("asn.read")));

		when(securityService.isSuperUser()).thenReturn(false);
		when(securityService.isUserRoleSwapped()).thenReturn(false);
		when(sessionManager.getCurrentSessionUserId()).thenReturn("student1");
		when(siteService.getUserSiteId("student1")).thenReturn("~student1");
		when(siteService.getSite("~student1")).thenReturn(homeSite);
		when(siteService.getSite("site-1")).thenReturn(site1);
		when(siteService.isUserSite(anyString())).thenReturn(false);
		when(siteNeighbourhoodService.parseSiteAlias(anyString())).thenReturn(null);
		when(preferencesService.getPreferences("student1")).thenReturn(preferences);
		when(preferences.getProperties(PreferencesService.SITENAV_PREFS_KEY)).thenReturn(siteNavProperties);
		when(siteNavProperties.getPropertyList("exclude")).thenReturn(Collections.emptyList());
		when(portalService.getPinnedSites("student1")).thenReturn(List.of("site-1"));
		when(portalService.getRecentSites("student1")).thenReturn(Collections.emptyList());
		when(serverConfigurationService.getString("portal.includesubsites", "false")).thenReturn("false");
		when(authzGroupService.getAuthzGroupsIsAllowed("student1", SiteService.SECURE_UPDATE_SITE, siteRefs))
			.thenReturn(Collections.emptySet());
		when(authzGroupService.getAuthzGroupsIsAllowed("student1", "section.role.instructor", siteRefs))
			.thenReturn(Collections.emptySet());
		when(authzGroupService.getRoleFunctions(siteRefs)).thenReturn(Map.of(
			homeSiteRef, Collections.emptyMap(),
			site1Ref, Map.of("Student", Set.of("asn.read"))));
		when(userDirectoryService.getCurrentUser()).thenReturn(user);
		when(user.getId()).thenReturn("student1");

		try (MockedStatic<ComponentManager> componentManager = mockStatic(ComponentManager.class)) {
			componentManager.when(ComponentManager::isTestingMode).thenReturn(true);
			componentManager.when(() -> ComponentManager.get(AliasService.class)).thenReturn(mock(AliasService.class));
			componentManager.when(() -> ComponentManager.get(AuthzGroupService.class)).thenReturn(authzGroupService);
			componentManager.when(() -> ComponentManager.get(EntityManager.class)).thenReturn(mock(EntityManager.class));
			componentManager.when(() -> ComponentManager.get(FormattedText.class)).thenReturn(mock(FormattedText.class));
			componentManager.when(() -> ComponentManager.get(PortalService.class)).thenReturn(portalService);
			componentManager.when(() -> ComponentManager.get(PreferencesService.class)).thenReturn(preferencesService);
			componentManager.when(() -> ComponentManager.get(SecurityService.class)).thenReturn(securityService);
			componentManager.when(() -> ComponentManager.get(ServerConfigurationService.class)).thenReturn(serverConfigurationService);
			componentManager.when(() -> ComponentManager.get(SessionManager.class)).thenReturn(sessionManager);
			componentManager.when(() -> ComponentManager.get(SiteNeighbourhoodService.class)).thenReturn(siteNeighbourhoodService);
			componentManager.when(() -> ComponentManager.get(SiteService.class)).thenReturn(siteService);
			componentManager.when(() -> ComponentManager.get(ThreadLocalManager.class)).thenReturn(mock(ThreadLocalManager.class));
			componentManager.when(() -> ComponentManager.get(ToolManager.class)).thenReturn(toolManager);
			componentManager.when(() -> ComponentManager.get(UserDirectoryService.class)).thenReturn(userDirectoryService);

			PortalSiteHelperImpl helper = new PortalSiteHelperImpl(mock(Portal.class), false);
			helper.getContextSitesWithPages(mock(HttpServletRequest.class), "site-1", null, true);

			verify(authzGroupService).getRoleFunctions(siteRefs);
			verify(site1, never()).getRoles();
			verify(toolManager, never()).isFirstToolVisibleToAnyNonMaintainerRole(page);
		}
	}

	public void testCheckGradebookVisibilityDoesNotLoadRoleFunctions() throws Exception {

		AuthzGroupService authzGroupService = mock(AuthzGroupService.class);
		SecurityService securityService = mock(SecurityService.class);
		ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
		SessionManager sessionManager = mock(SessionManager.class);
		Site site = mockSite("site-1", "Site 1");
		ToolConfiguration toolConfiguration = mock(ToolConfiguration.class);

		String siteRef = "/site/site-1";
		List<String> siteRefs = List.of(siteRef);

		when(securityService.isSuperUser()).thenReturn(false);
		when(securityService.isUserRoleSwapped()).thenReturn(false);
		when(sessionManager.getCurrentSessionUserId()).thenReturn("student1");
		when(authzGroupService.getAuthzGroupsIsAllowed("student1", SiteService.SECURE_UPDATE_SITE, siteRefs))
			.thenReturn(Collections.emptySet());
		when(authzGroupService.getAuthzGroupsIsAllowed("student1", "section.role.instructor", siteRefs))
			.thenReturn(Collections.emptySet());
		when(toolConfiguration.getToolId()).thenReturn("sakai.assignment.grades");

		try (MockedStatic<ComponentManager> componentManager = mockStatic(ComponentManager.class)) {
			componentManager.when(ComponentManager::isTestingMode).thenReturn(true);
			componentManager.when(() -> ComponentManager.get(AliasService.class)).thenReturn(mock(AliasService.class));
			componentManager.when(() -> ComponentManager.get(AuthzGroupService.class)).thenReturn(authzGroupService);
			componentManager.when(() -> ComponentManager.get(EntityManager.class)).thenReturn(mock(EntityManager.class));
			componentManager.when(() -> ComponentManager.get(FormattedText.class)).thenReturn(mock(FormattedText.class));
			componentManager.when(() -> ComponentManager.get(PortalService.class)).thenReturn(mock(PortalService.class));
			componentManager.when(() -> ComponentManager.get(PreferencesService.class)).thenReturn(mock(PreferencesService.class));
			componentManager.when(() -> ComponentManager.get(SecurityService.class)).thenReturn(securityService);
			componentManager.when(() -> ComponentManager.get(ServerConfigurationService.class)).thenReturn(serverConfigurationService);
			componentManager.when(() -> ComponentManager.get(SessionManager.class)).thenReturn(sessionManager);
			componentManager.when(() -> ComponentManager.get(SiteNeighbourhoodService.class)).thenReturn(mock(SiteNeighbourhoodService.class));
			componentManager.when(() -> ComponentManager.get(SiteService.class)).thenReturn(mock(SiteService.class));
			componentManager.when(() -> ComponentManager.get(ThreadLocalManager.class)).thenReturn(mock(ThreadLocalManager.class));
			componentManager.when(() -> ComponentManager.get(ToolManager.class)).thenReturn(mock(ToolManager.class));
			componentManager.when(() -> ComponentManager.get(UserDirectoryService.class)).thenReturn(mock(UserDirectoryService.class));

			PortalSiteHelperImpl helper = new PortalSiteHelperImpl(mock(Portal.class), false);
			assertTrue(helper.checkGradebookVisibility(toolConfiguration, site));

			verify(authzGroupService).getAuthzGroupsIsAllowed("student1", SiteService.SECURE_UPDATE_SITE, siteRefs);
			verify(authzGroupService).getAuthzGroupsIsAllowed("student1", "section.role.instructor", siteRefs);
			verify(authzGroupService, never()).getRoleFunctions(any());
		}
	}

	private Site mockSite(String siteId, String title) {

		Site site = mock(Site.class);
		ResourceProperties properties = mock(ResourceProperties.class);

		when(site.getId()).thenReturn(siteId);
		when(site.getReference()).thenReturn("/site/" + siteId);
		when(site.getTitle()).thenReturn(title);
		when(site.getType()).thenReturn("course");
		when(site.getUrl()).thenReturn("/portal/site/" + siteId);
		when(site.getDescription()).thenReturn(null);
		when(site.getShortDescription()).thenReturn(null);
		when(site.getProperties()).thenReturn(properties);
		when(site.getOrderedPages()).thenReturn(Collections.emptyList());
		when(site.isEmpty()).thenReturn(false);
		when(properties.getProperty(anyString())).thenReturn(null);

		return site;
	}
}
