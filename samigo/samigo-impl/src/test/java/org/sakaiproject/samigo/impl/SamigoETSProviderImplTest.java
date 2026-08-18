/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.samigo.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.api.RenderedTemplate;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Tests for the manual "grading updated" student notification.
 */
@RunWith(MockitoJUnitRunner.class)
public class SamigoETSProviderImplTest {

    private static final String SITE_ID = "site-1";
    private static final String TITLE = "Essay Quiz";

    @Mock private EmailTemplateService emailTemplateService;
    @Mock private EmailService emailService;
    @Mock private DigestService digestService;
    @Mock private PreferencesService preferencesService;
    @Mock private UserDirectoryService userDirectoryService;
    @Mock private SiteService siteService;
    @Mock private Site site;
    @Mock private ToolConfiguration toolConfiguration;
    @Mock private FormattedText formattedText;
    @Mock private RenderedTemplate renderedTemplate;
    @Mock private Preferences preferences;
    @Mock private ResourceProperties prefProps;

    private SamigoETSProviderImpl provider;
    private final Map<String, User> usersById = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        provider = new SamigoETSProviderImpl();
        provider.setEmailTemplateService(emailTemplateService);
        provider.setEmailService(emailService);
        provider.setDigestService(digestService);
        provider.setPreferencesService(preferencesService);
        provider.setUserDirectoryService(userDirectoryService);
        provider.setSiteService(siteService);
        lenient().when(formattedText.escapeHtml(anyString())).thenAnswer(inv -> inv.getArgument(0));
        provider.setFormattedText(formattedText);

        lenient().when(siteService.getSite(SITE_ID)).thenReturn(site);
        lenient().when(site.getTitle()).thenReturn("SMPL101");
        lenient().when(site.getUrl()).thenReturn("http://server/portal/site/site-1");
        lenient().when(site.getToolForCommonId(SamigoConstants.TOOL_ID)).thenReturn(toolConfiguration);
        lenient().when(toolConfiguration.getId()).thenReturn("placement-9");
        lenient().when(emailTemplateService.getRenderedTemplateForUser(
                eq(SamigoConstants.EMAIL_TEMPLATE_GRADING_UPDATED), anyString(), any())).thenReturn(renderedTemplate);
        lenient().when(renderedTemplate.getRenderedSubject()).thenReturn("subject");
        lenient().when(renderedTemplate.getRenderedMessage()).thenReturn("plain");
        lenient().when(renderedTemplate.getRenderedHtmlMessage()).thenReturn("<p>html</p>");
        lenient().when(preferencesService.getPreferences(anyString())).thenReturn(preferences);
        lenient().when(preferences.getProperties(anyString())).thenReturn(prefProps);
        // bulk user lookup returns the mocked users for the requested ids (unknown ids omitted)
        lenient().when(userDirectoryService.getUsers(anyCollection())).thenAnswer(inv -> {
            Collection<String> ids = inv.getArgument(0);
            List<User> result = new ArrayList<>();
            for (String id : ids) {
                if (usersById.containsKey(id)) result.add(usersById.get(id));
            }
            return result;
        });
    }

    private User mockUser(String id, String email) throws UserNotDefinedException {
        User user = org.mockito.Mockito.mock(User.class);
        lenient().when(user.getId()).thenReturn(id);
        lenient().when(user.getEmail()).thenReturn(email);
        lenient().when(user.getReference()).thenReturn("/user/" + id);
        lenient().when(user.getDisplayName()).thenReturn(id);
        lenient().when(userDirectoryService.getUser(id)).thenReturn(user);
        usersById.put(id, user);
        return user;
    }

    private void setPref(long pref) throws Exception {
        when(prefProps.getLongProperty(anyString())).thenReturn(pref);
    }

    private void setNoPref() throws Exception {
        when(prefProps.getLongProperty(anyString())).thenThrow(new EntityPropertyNotDefinedException());
    }

    @Test
    public void immediatePreferenceSendsEmail() throws Exception {
        mockUser("s1", "s1@x.edu");
        setPref(NotificationService.PREF_IMMEDIATE);

        List<String> delivered = provider.notifyGradingUpdated(Collections.singletonList("s1"), SITE_ID, TITLE);

        assertEquals(Collections.singletonList("s1"), delivered);
        verify(emailService).sendToUser(any(User.class), anyList(), anyString());
        verify(digestService, never()).digest(anyString(), anyString(), anyString());
    }

    @Test
    public void digestPreferenceUsesDigestService() throws Exception {
        mockUser("s1", "s1@x.edu");
        setPref(NotificationService.PREF_DIGEST);

        List<String> delivered = provider.notifyGradingUpdated(Collections.singletonList("s1"), SITE_ID, TITLE);

        assertEquals(Collections.singletonList("s1"), delivered);
        verify(digestService).digest(eq("s1"), anyString(), anyString());
        verify(emailService, never()).sendToUser(any(User.class), anyList(), anyString());
    }

    @Test
    public void ignorePreferenceSendsNothing() throws Exception {
        mockUser("s1", "s1@x.edu");
        setPref(NotificationService.PREF_IGNORE);

        List<String> delivered = provider.notifyGradingUpdated(Collections.singletonList("s1"), SITE_ID, TITLE);

        assertEquals(0, delivered.size());
        verify(emailService, never()).sendToUser(any(User.class), anyList(), anyString());
        verify(digestService, never()).digest(anyString(), anyString(), anyString());
    }

    @Test
    public void defaultPreferenceIsImmediate() throws Exception {
        mockUser("s1", "s1@x.edu");
        setNoPref();

        List<String> delivered = provider.notifyGradingUpdated(Collections.singletonList("s1"), SITE_ID, TITLE);

        assertEquals(Collections.singletonList("s1"), delivered);
        verify(emailService).sendToUser(any(User.class), anyList(), anyString());
    }

    @Test
    public void blankEmailIsSkipped() throws Exception {
        mockUser("s1", "");
        List<String> delivered = provider.notifyGradingUpdated(Collections.singletonList("s1"), SITE_ID, TITLE);

        assertEquals(0, delivered.size());
        verify(emailService, never()).sendToUser(any(User.class), anyList(), anyString());
    }

    @Test
    public void unknownUserIsSkippedOthersStillNotified() throws Exception {
        mockUser("s2", "s2@x.edu");
        setPref(NotificationService.PREF_IMMEDIATE);

        List<String> delivered = provider.notifyGradingUpdated(Arrays.asList("ghost", "s2"), SITE_ID, TITLE);

        assertEquals(Collections.singletonList("s2"), delivered);
    }

    @Test
    public void unknownSiteSendsNothing() throws Exception {
        when(siteService.getSite("nope")).thenThrow(new IdUnusedException("nope"));

        List<String> delivered = provider.notifyGradingUpdated(Collections.singletonList("s1"), "nope", TITLE);

        assertEquals(0, delivered.size());
    }

    @Test
    public void emptyListSendsNothing() {
        assertEquals(0, provider.notifyGradingUpdated(Collections.emptyList(), SITE_ID, TITLE).size());
        assertEquals(0, provider.notifyGradingUpdated(null, SITE_ID, TITLE).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void templateGetsTitleSiteNameAndToolLink() throws Exception {
        mockUser("s1", "s1@x.edu");
        setPref(NotificationService.PREF_IMMEDIATE);

        provider.notifyGradingUpdated(Collections.singletonList("s1"), SITE_ID, TITLE);

        ArgumentCaptor<Map<String, Object>> values = ArgumentCaptor.forClass(Map.class);
        verify(emailTemplateService).getRenderedTemplateForUser(
                eq(SamigoConstants.EMAIL_TEMPLATE_GRADING_UPDATED), eq("/user/s1"), values.capture());
        assertEquals(TITLE, values.getValue().get("assessmentTitle"));
        assertEquals("SMPL101", values.getValue().get("siteName"));
        assertEquals("http://server/portal/site/site-1/tool/placement-9", values.getValue().get("toolUrl"));
    }

    @Test
    public void missingToolPlacementFallsBackToSiteUrl() throws Exception {
        mockUser("s1", "s1@x.edu");
        setPref(NotificationService.PREF_IMMEDIATE);
        when(site.getToolForCommonId(SamigoConstants.TOOL_ID)).thenReturn(null);

        provider.notifyGradingUpdated(Collections.singletonList("s1"), SITE_ID, TITLE);

        ArgumentCaptor<Map<String, Object>> values = ArgumentCaptor.forClass(Map.class);
        verify(emailTemplateService).getRenderedTemplateForUser(anyString(), anyString(), values.capture());
        assertEquals("http://server/portal/site/site-1", values.getValue().get("toolUrl"));
    }
}
