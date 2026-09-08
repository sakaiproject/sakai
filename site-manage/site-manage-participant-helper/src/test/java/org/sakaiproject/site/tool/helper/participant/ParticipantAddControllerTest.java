/**
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.site.tool.helper.participant;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.helper.participant.impl.ParticipantConstants;
import org.sakaiproject.site.tool.helper.participant.impl.ParticipantHelperTestConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration("src/webapp")
@ContextConfiguration(classes = {ParticipantHelperWebMvcConfiguration.class, ParticipantHelperTestConfiguration.class})
public class ParticipantAddControllerTest {
    @Autowired private WebApplicationContext context;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;
    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private LocaleService localeService;
    private MockMvc mvc;
    @BeforeClass
    public static void initializeKernelBoundary() {
        ComponentManager.testingMode = true;
    }

    @AfterClass
    public static void closeKernelBoundary() {
        ComponentManager.shutdown();
        ComponentManager.testingMode = false;
    }

    @Before
    public void setUp() throws Exception {
        ComponentManager.loadComponent(ServerConfigurationService.class, context.getBean(ServerConfigurationService.class));
        ComponentManager.loadComponent(SessionManager.class, sessionManager);
        ComponentManager.loadComponent(ThreadLocalManager.class,
                context.getBean(ThreadLocalManager.class));
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.ENGLISH);
        Session session = mock(Session.class);
        ToolSession toolSession = mock(ToolSession.class);
        Map<String, Object> state = new HashMap<>();
        state.put(ParticipantConstants.HELPER_SITE_ID_ATTRIBUTE, "test-site");
        when(toolSession.getAttribute(anyString())).thenAnswer(call -> state.get(call.getArgument(0)));
        doAnswer(call -> { state.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(toolSession).setAttribute(anyString(), any());
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(sessionManager.getCurrentToolSession()).thenReturn(toolSession);
        Map<String, Object> sessionState = new HashMap<>();
        sessionState.put(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE, "token");
        when(session.getAttribute(anyString())).thenAnswer(call -> sessionState.get(call.getArgument(0)));
        doAnswer(call -> { sessionState.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(session).setAttribute(anyString(), any());
        Site site = mock(Site.class);
        when(site.getProperties()).thenReturn(mock(ResourceProperties.class));
        when(site.getId()).thenReturn("test-site");
        when(site.getTitle()).thenReturn("Test site");
        when(site.getType()).thenReturn("project");
        when(siteService.getSite("test-site")).thenReturn(site);
        when(siteService.siteReference("test-site")).thenReturn("/site/test-site");
        when(siteService.allowUpdateSiteMembership("test-site")).thenReturn(true);
        AuthzGroup realm = mock(AuthzGroup.class);
        when(realm.getRoles()).thenReturn(Set.of());
        when(authzGroupService.getAuthzGroup("/site/test-site")).thenReturn(realm);
        for (String eid : new String[] {"existing", "new-user"}) {
            User user = mock(User.class);
            when(user.getId()).thenReturn(eid);
            when(user.getEid()).thenReturn(eid);
            when(user.getDisplayId()).thenReturn(eid);
            when(user.getSortName()).thenReturn(eid);
            when(userDirectoryService.getUserByEid(eid)).thenReturn(user);
        }
        when(site.getUserRole("existing")).thenReturn(mock(Role.class));
    }

    @Test
    public void keepsExistingMemberWarningAcrossRoleRedirect() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/add").header("Sec-Fetch-Site", "same-origin").session(session).param("csrfToken", "token")
                .param("officialAccountParticipant", "existing\r\nnew-user").param("statusChoice", "active"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/roles"));
        MvcResult roles = mvc.perform(get("/roles").session(session)).andExpect(status().isOk()).andReturn();
        String html = roles.getResponse().getContentAsString();
        assertTrue(html, html.contains("sak-banner-warn"));
        assertTrue(html, html.contains("existing"));
        assertTrue(html, html.contains("new-user"));
        String refreshed = mvc.perform(get("/roles").session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertFalse(refreshed, refreshed.contains("sak-banner-warn"));
    }
}
