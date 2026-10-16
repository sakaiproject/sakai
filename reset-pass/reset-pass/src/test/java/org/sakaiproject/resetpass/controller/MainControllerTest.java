/**********************************************************************************
 *
 * Copyright (c) 2026 The Sakai Foundation
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
package org.sakaiproject.resetpass.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.accountvalidator.api.model.ValidationAccount;
import org.sakaiproject.accountvalidator.api.service.AccountValidationService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.resetpass.test.ResetPassTestConfiguration;
import org.sakaiproject.serialization.MapperFactory;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ResetPassTestConfiguration.class)
@WebAppConfiguration("src/main/webapp")
public class MainControllerTest {

    private static final String EMAIL = "guest@example.test";
    private static final String USER_ID = "guest-id";

    @Autowired private WebApplicationContext context;
    @Autowired private MainController controller;
    @Autowired private ServerConfigurationService configuration;
    @Autowired private LocaleService localeService;
    @Autowired private ToolManager toolManager;
    @Autowired private EventTrackingService events;
    @Autowired private AccountValidationService validations;
    @Autowired private UserDirectoryService users;
    @Autowired private SecurityService permissions;

    private MockMvc mvc;

    @Before
    public void setUp() {
        reset(configuration, localeService, toolManager, events, validations, users, permissions);
        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.US);
        when(configuration.getString(anyString(), nullable(String.class))).thenAnswer(call -> call.getArgument(1));
        when(configuration.getInt(anyString(), anyInt())).thenAnswer(call -> call.getArgument(1));
        User user = mock(User.class);
        when(user.getId()).thenReturn(USER_ID);
        when(user.getReference()).thenReturn("/user/" + USER_ID);
        when(users.findUsersByEmail(EMAIL)).thenReturn(List.of(user));
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void requestsResetLinkWhenNewUserValidationIsDisabled() throws Exception {
        assertResetLinkRequested(false);
    }

    @Test
    public void requestsResetLinkWhenNewUserValidationIsEnabled() throws Exception {
        assertResetLinkRequested(true);
    }

    private void assertResetLinkRequested(boolean validateNewUsers) throws Exception {
        when(configuration.getBoolean(eq("siteManage.validateNewUsers"), anyBoolean())).thenReturn(validateNewUsers);
        JsonNode response = submitReset();
        assertEquals("Sakai has attempted to send a password change email to " + EMAIL + ".", response.get("email_sent_msg").asText());
        verify(validations).createValidationAccount(USER_ID, ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);
        verify(users, never()).editUser(anyString());
        verify(events).newEvent("user.resetpass", "/user/" + USER_ID, true);
    }

    @Test
    public void resendsAnOutstandingResetLink() throws Exception {
        ValidationAccount pending = new ValidationAccount();
        pending.setValidationToken("pending-token");
        when(validations.getValidationAccountByUserId(USER_ID)).thenReturn(pending);
        when(validations.isAccountValidated(USER_ID)).thenReturn(true);

        submitReset();

        verify(validations).resendValidation("pending-token");
        verify(validations, never()).createValidationAccount(anyString(), anyInt());
        verify(users, never()).editUser(anyString());
    }

    @Test
    public void createsANewLinkAfterAnEarlierResetWasCompleted() throws Exception {
        ValidationAccount completed = new ValidationAccount();
        completed.setValidationReceived(Instant.now());
        when(validations.getValidationAccountByUserId(USER_ID)).thenReturn(completed);
        when(validations.isAccountValidated(USER_ID)).thenReturn(true);

        submitReset();

        verify(validations).createValidationAccount(USER_ID, ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);
        verify(users, never()).editUser(anyString());
    }

    @Test
    public void unknownEmailGetsTheSameResetLinkMessage() throws Exception {
        when(users.findUsersByEmail(EMAIL)).thenReturn(List.of());

        JsonNode response = submitReset();

        assertEquals("Sakai has attempted to send a password change email to " + EMAIL + ".", response.get("exception_msg").asText());
        verify(validations, never()).createValidationAccount(anyString(), anyInt());
        verify(users, never()).editUser(anyString());
    }

    @Test
    public void superuserResetRetainsTheGenericResponse() throws Exception {
        when(permissions.isSuperUser(USER_ID)).thenReturn(true);

        JsonNode response = submitReset();

        assertEquals("Sakai has attempted to send a password change email to " + EMAIL + ".", response.get("exception_msg").asText());
        verify(validations, never()).createValidationAccount(anyString(), anyInt());
        verify(users, never()).editUser(anyString());
    }

    @Test
    public void indexDescribesTheResetLink() {
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("form", controller.showIndex(model, new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertFalse(model.containsAttribute("validatingAccounts"));
        assertEquals("Upon submission, an email will be sent to you containing a link where you can securely set your password. You will have 1 hour before this link expires.", model.get("explanation"));
    }

    @Test
    public void getFormattedMinutesDelegatesToDurationFormat() {
        assertEquals("1 hour", controller.getFormattedMinutes(60, Locale.US));
        assertEquals("1 hour and 30 minutes", controller.getFormattedMinutes(90, Locale.US));
        assertEquals("1 heure", controller.getFormattedMinutes(60, Locale.FRANCE));
    }

    private JsonNode submitReset() throws Exception {
        String response = mvc.perform(post("/formsubmit").contentType(MediaType.APPLICATION_JSON).content(EMAIL))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return MapperFactory.createDefaultJsonMapper().readTree(response);
    }
}
