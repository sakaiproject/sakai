/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.webapi.controllers.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

import org.adl.sequencer.IValidRequests;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.ScormLaunchService;
import org.sakaiproject.scorm.service.api.launch.ScormLaunchContext;
import org.sakaiproject.scorm.service.api.launch.ScormRuntimeInvocation;
import org.sakaiproject.scorm.service.api.launch.ScormRuntimeResult;
import org.sakaiproject.scorm.service.api.launch.ScormTocEntry;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.controllers.ScormController;

import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebApiTestConfiguration.class })
public class ScormControllerTest extends BaseControllerTests {

    private MockMvc mockMvc;
    private ScormLaunchService scormLaunchService;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Before
    public void setUp() {

        scormLaunchService = mock(ScormLaunchService.class);

        ScormController controller = new ScormController();
        ReflectionTestUtils.setField(controller, "scormLaunchService", scormLaunchService);

        SessionManager sessionManager = mock(SessionManager.class);
        Session session = mock(Session.class);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(session.getUserId()).thenReturn("test-user");

        controller.setSessionManager(sessionManager);
        controller.setPortalService(mock(PortalService.class));
        controller.setSiteService(mock(SiteService.class));

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .apply(documentationConfiguration(this.restDocumentation))
            .build();
    }

    @Test
    public void createSessionReturnsContext() throws Exception {

        ScormLaunchContext context = buildLaunchContext();
        when(scormLaunchService.openSession(anyLong(), any(), any())).thenReturn(context);

        mockMvc.perform(post("/scorm/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contentPackageId\": 7}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value("session-123"))
            .andExpect(jsonPath("$.contentPackageId").value(7))
            .andExpect(jsonPath("$.state").value("READY"))
            .andExpect(jsonPath("$.launchPath").value("contentpackages/resourceName/demo/index.html"))
            .andDo(document("create-scorm-session", preprocessor));
    }

    @Test
    public void runtimeInvocationReturnsPayload() throws Exception {

        ArgumentCaptor<ScormRuntimeInvocation> invocationCaptor = ArgumentCaptor.forClass(ScormRuntimeInvocation.class);

        when(scormLaunchService.runtime(eq("session-123"), invocationCaptor.capture())).thenReturn(ScormRuntimeResult.builder()
            .value("true")
            .errorCode("0")
            .diagnostic(null)
            .launchPath(null)
            .sessionEnded(false)
            .build());

        mockMvc.perform(post("/scorm/sessions/session-123/runtime")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"method\":\"Initialize\",\"arguments\":[\"\"],\"scoId\":\"sco-1\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.value").value("true"))
            .andExpect(jsonPath("$.errorCode").value("0"))
            .andExpect(jsonPath("$.launchPath").doesNotExist())
            .andExpect(jsonPath("$.sessionEnded").value(false))
            .andDo(document("initialize-runtime", preprocessor));

        ScormRuntimeInvocation captured = invocationCaptor.getValue();
        assertEquals("sco-1", captured.getScoId());
    }

    @Test
    public void getSessionReturns404WhenMissing() throws Exception {

        when(scormLaunchService.getSession("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/scorm/sessions/missing"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void closeSessionReturnsNoContent() throws Exception {

        mockMvc.perform(delete("/scorm/sessions/session-123"))
            .andExpect(status().isOk());
    }

    private ScormLaunchContext buildLaunchContext() {

        SessionBean sessionBean = new SessionBean();
        sessionBean.setAttemptNumber(1L);
        sessionBean.setCompletionUrl("/complete");
        sessionBean.setActivityId("sco-1");
        sessionBean.setScoId("sco-1");

        IValidRequests validRequests = mock(IValidRequests.class);
        when(validRequests.isStartEnabled()).thenReturn(true);
        when(validRequests.isResumeEnabled()).thenReturn(false);
        when(validRequests.isPreviousEnabled()).thenReturn(false);
        when(validRequests.isContinueEnabled()).thenReturn(true);
        when(validRequests.isSuspendEnabled()).thenReturn(true);
        when(validRequests.getChoice()).thenReturn(java.util.Collections.emptyMap());
        sessionBean.setNavigationState(validRequests);

        ContentPackage contentPackage = new ContentPackage();
        contentPackage.setContentPackageId(7L);
        contentPackage.setTitle("Sample Package");
        contentPackage.setShowNavBar(true);
        contentPackage.setShowTOC(true);

        return ScormLaunchContext.builder()
            .sessionId("session-123")
            .sessionBean(sessionBean)
            .launchPath("contentpackages/resourceName/demo/index.html")
            .contentPackage(contentPackage)
            .showLegacyControls(true)
            .showToc(true)
            .currentActivityId("sco-1")
            .currentScoId("sco-1")
            .tocEntries(Collections.singletonList(ScormTocEntry.builder()
                .activityId("sco-1")
                .title("Activity One")
                .leaf(true)
                .current(true)
                .build()))
            .build();
    }
}
