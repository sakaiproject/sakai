/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.test.emailtemplateservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.api.model.EmailTemplate;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EmailTemplateToolTestConfiguration.class)
@WebAppConfiguration("src/main/webapp")
public class EmailTemplateAdministrationTest {

    private static final String[] PAGES = {"/", "/index", "/index/true/validate.newUser/default", "/new", "/edit/42", "/edit/999999"};
    private static final String[] SUBMISSIONS = {"/new/formsubmit", "/edit/42/formsubmit", "/edit/999999/formsubmit"};
    private static boolean previousTestingMode;

    @Autowired private WebApplicationContext context;
    @Autowired private EmailTemplateService emailTemplateService;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private LocaleService localeService;

    private MockMvc mvc;
    private EmailTemplate existing;
    private final List<EmailTemplate> savedTemplates = new ArrayList<>();

    @BeforeClass
    public static void enableKernelTestMode() {
        previousTestingMode = ComponentManager.testingMode;
        ComponentManager.testingMode = true;
    }

    @AfterClass
    public static void closeKernelTestMode() {
        ComponentManager.shutdown();
        ComponentManager.testingMode = previousTestingMode;
    }

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        reset(emailTemplateService, securityService, localeService);
        clearInvocations(sessionManager);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("admin");
        when(securityService.isSuperUser("admin")).thenReturn(true);
        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.US);
        savedTemplates.clear();
        existing = new EmailTemplate();
        existing.setId(42L);
        existing.setOwner("original-owner");
        existing.setVersion(7);
        existing.setLastModified(Date.from(Instant.parse("2026-01-01T00:00:00Z")));
        existing.setFrom("original@example.edu");
        existing.setDefaultType("original-type");
        existing.setKey("validate.newUser");
        existing.setLocale(EmailTemplate.DEFAULT_LOCALE);
        existing.setSubject("Original subject");
        existing.setMessage("Original body ${url}");
        existing.setHtmlMessage("<p>Original body ${url}</p>");
        when(emailTemplateService.getEmailTemplateById(42L)).thenReturn(existing);
        when(emailTemplateService.getEmailTemplates(0, 0)).thenReturn(new ArrayList<>(List.of(existing)));
        when(emailTemplateService.saveTemplate(any(EmailTemplate.class))).thenAnswer(call -> {
            EmailTemplate template = call.getArgument(0);
            savedTemplates.add(template);
            return template;
        });
    }

    @Test
    public void rejectsAnonymousRequestsBeforeReadingTemplates() throws Exception {
        when(sessionManager.getCurrentSessionUserId()).thenReturn(null);
        assertDeniedRoutes();
        verifyNoInteractions(securityService);
    }

    @Test
    public void rejectsOrdinaryUsersEvenForSameOriginSubmissions() throws Exception {
        when(sessionManager.getCurrentSessionUserId()).thenReturn("instructor1");
        assertDeniedRoutes();
    }

    @Test
    public void rejectsBlankSessionUsers() throws Exception {
        when(sessionManager.getCurrentSessionUserId()).thenReturn(" ");
        assertDeniedRoutes();
        verifyNoInteractions(securityService);
    }

    private void assertDeniedRoutes() throws Exception {
        for (String path : PAGES) {
            mvc.perform(get(path)).andExpect(status().isForbidden());
        }
        for (String path : SUBMISSIONS) {
            MvcResult result = mvc.perform(submission(path, "{\"subject\":\"marker\"}"))
                    .andExpect(status().isForbidden()).andReturn();
            assertTrue(result.getResponse().getContentType().startsWith("application/json"));
            assertTrue(result.getResponse().getContentAsString().contains("\"status\":\"ERROR\""));
        }
        verifyNoInteractions(emailTemplateService);
        assertTrue(savedTemplates.isEmpty());
        assertEquals("Original subject", existing.getSubject());
    }

    @Test
    public void rendersAllAdministratorPages() throws Exception {
        for (String path : List.of("/", "/index", "/index/true/validate.newUser/default", "/new", "/edit/42")) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
        String edit = mvc.perform(get("/edit/42")).andReturn().getResponse().getContentAsString();
        assertTrue(edit.contains("Original subject"));
        assertTrue(edit.contains("Original body ${url}"));
    }

    @Test
    public void returnsNotFoundForMissingAdministratorEdit() throws Exception {
        mvc.perform(get("/edit/999999")).andExpect(status().isNotFound());
    }

    @Test
    public void rejectsCrossSiteSameSiteNoneAndMissingOriginSubmissions() throws Exception {
        for (String path : SUBMISSIONS) {
            for (String fetchSite : List.of("cross-site", "same-site", "none")) {
                mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                        .header("Sec-Fetch-Site", fetchSite).content("{\"subject\":\"marker\"}"))
                        .andExpect(status().isForbidden());
            }
            mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"subject\":\"marker\"}")).andExpect(status().isForbidden());
            mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                    .header("Origin", "https://evil.example").content("{}")).andExpect(status().isForbidden());
        }
        verifyNoInteractions(emailTemplateService);
        assertEquals("Original subject", existing.getSubject());
    }

    @Test
    public void acceptsSameOriginAndRefererFallbacks() throws Exception {
        for (String header : List.of("Origin", "Referer")) {
            mvc.perform(post("/edit/42/formsubmit").contentType(MediaType.APPLICATION_JSON)
                    .header(header, "http://localhost").content("{\"subject\":\"Updated\"}"))
                    .andExpect(status().isOk());
        }
        assertEquals(2, savedTemplates.size());
    }

    @Test
    public void updatesOnlySubmittedEditableFields() throws Exception {
        mvc.perform(submission("/edit/42/formsubmit", "{\"subject\":\"Updated subject\",\"from\":\"ignored@example.edu\"}"))
                .andExpect(status().isOk());
        EmailTemplate saved = savedTemplates.get(0);
        assertNotSame(existing, saved);
        assertEquals("Updated subject", saved.getSubject());
        assertEquals(existing.getMessage(), saved.getMessage());
        assertEquals(existing.getHtmlMessage(), saved.getHtmlMessage());
        assertEquals(existing.getKey(), saved.getKey());
        assertEquals(existing.getLocale(), saved.getLocale());
        assertEquals(existing.getId(), saved.getId());
        assertEquals(existing.getOwner(), saved.getOwner());
        assertEquals(existing.getVersion(), saved.getVersion());
        assertEquals(existing.getFrom(), saved.getFrom());
        assertEquals(existing.getDefaultType(), saved.getDefaultType());
        assertEquals("Original subject", existing.getSubject());
    }

    @Test
    public void rejectsMetadataAndUnknownFieldsWithoutChangingTheOriginal() throws Exception {
        for (String field : List.of("owner", "version", "id", "lastModified", "defaultType", "unknown")) {
            String body = "{\"subject\":\"marker\",\"" + field + "\":null}";
            mvc.perform(submission("/edit/42/formsubmit", body)).andExpect(status().isBadRequest());
            mvc.perform(submission("/new/formsubmit", body)).andExpect(status().isBadRequest());
            assertEquals("Original subject", existing.getSubject());
            assertEquals("original-owner", existing.getOwner());
            assertEquals(Integer.valueOf(7), existing.getVersion());
        }
        assertTrue(savedTemplates.isEmpty());
    }

    @Test
    public void rejectsMalformedAndInvalidInputWithoutChangingTheOriginal() throws Exception {
        for (String body : List.of("[]", "null", "{", "{} {}", "{\"subject\":\"\"}",
                "{\"subject\":\"marker\",\"locale\":\"invalid_locale_value\"}")) {
            mvc.perform(submission("/edit/42/formsubmit", body)).andExpect(status().isBadRequest());
            assertEquals("Original subject", existing.getSubject());
            assertEquals("default", existing.getLocale());
        }
        assertTrue(savedTemplates.isEmpty());
    }

    @Test
    public void preservesDuplicateKeyValidation() throws Exception {
        when(emailTemplateService.templateExistsWithDifferentId("duplicate", null, 42L)).thenReturn(true);
        mvc.perform(submission("/edit/42/formsubmit", "{\"subject\":\"marker\",\"key\":\"duplicate\"}"))
                .andExpect(status().isBadRequest());
        assertTrue(savedTemplates.isEmpty());
        assertEquals("validate.newUser", existing.getKey());
    }

    @Test
    public void createsATemplateOwnedByTheCurrentAdministrator() throws Exception {
        mvc.perform(submission("/new/formsubmit", "{\"subject\":\"New subject\",\"key\":\"custom.template\",\"locale\":\"\",\"message\":\"Body ${url}\",\"messagehtml\":\"<p>${url}</p>\"}"))
                .andExpect(status().isOk());
        EmailTemplate saved = savedTemplates.get(0);
        assertNull(saved.getId());
        assertEquals("admin", saved.getOwner());
        assertEquals("custom.template", saved.getKey());
        assertEquals("default", saved.getLocale());
        assertEquals("Body ${url}", saved.getMessage());
        assertEquals("<p>${url}</p>", saved.getHtmlMessage());
    }

    @Test
    public void rejectsUnexpectedMethodsAndContentTypes() throws Exception {
        for (String path : List.of("/", "/index", "/new", "/edit/42")) {
            mvc.perform(post(path)).andExpect(status().isMethodNotAllowed());
        }
        mvc.perform(put("/edit/42/formsubmit")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/edit/42/formsubmit").contentType(MediaType.TEXT_PLAIN).content("{}"))
                .andExpect(status().isUnsupportedMediaType());
        verifyNoInteractions(emailTemplateService);
    }

    @Test
    public void usesSakaiLocaleForRenderingAndErrors() throws Exception {
        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.FRANCE);
        MvcResult result = mvc.perform(submission("/edit/42/formsubmit", "{\"subject\":\"\"}"))
                .andExpect(status().isBadRequest()).andReturn();
        assertTrue(result.getResponse().getContentAsString(StandardCharsets.UTF_8).contains("Veuillez préciser un sujet"));
        assertEquals(Locale.FRANCE, result.getRequest().getSession().getAttribute(
                org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME));
        assertTrue(savedTemplates.isEmpty());
    }

    private MockHttpServletRequestBuilder submission(String path, String body) {
        return post(path).contentType(MediaType.APPLICATION_JSON).header("Sec-Fetch-Site", "same-origin").content(body);
    }
}
