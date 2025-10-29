package org.sakaiproject.webapi.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.adl.sequencer.IValidRequests;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.ScormLaunchService;
import org.sakaiproject.scorm.service.api.launch.ScormLaunchContext;
import org.sakaiproject.scorm.service.api.launch.ScormRuntimeResult;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ScormControllerTest
{
    private MockMvc mockMvc;
    private ScormLaunchService scormLaunchService;

    @Before
    public void setUp()
    {
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

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void createSessionReturnsContext() throws Exception
    {
        ScormLaunchContext context = buildLaunchContext();
        when(scormLaunchService.openSession(anyLong(), any(), any())).thenReturn(context);

        mockMvc.perform(post("/scorm/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contentPackageId\": 7}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value("session-123"))
            .andExpect(jsonPath("$.contentPackageId").value(7))
            .andExpect(jsonPath("$.state").value("READY"))
            .andExpect(jsonPath("$.launchPath").value("contentpackages/resourceName/demo/index.html"));
    }

    @Test
    public void runtimeInvocationReturnsPayload() throws Exception
    {
        when(scormLaunchService.runtime(eq("session-123"), any())).thenReturn(ScormRuntimeResult.builder()
            .value("true")
            .errorCode("0")
            .diagnostic(null)
            .launchPath(null)
            .sessionEnded(false)
            .build());

        mockMvc.perform(post("/scorm/sessions/session-123/runtime")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"method\":\"Initialize\",\"arguments\":[\"\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.value").value("true"))
            .andExpect(jsonPath("$.errorCode").value("0"))
            .andExpect(jsonPath("$.launchPath").doesNotExist())
            .andExpect(jsonPath("$.sessionEnded").value(false));
    }

    @Test
    public void getSessionReturns404WhenMissing() throws Exception
    {
        when(scormLaunchService.getSession("missing")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/scorm/sessions/missing"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void closeSessionReturnsNoContent() throws Exception
    {
        mockMvc.perform(delete("/scorm/sessions/session-123"))
            .andExpect(status().isOk());
    }

    private ScormLaunchContext buildLaunchContext()
    {
        SessionBean sessionBean = new SessionBean();
        sessionBean.setAttemptNumber(1L);
        sessionBean.setCompletionUrl("/complete");

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
            .build();
    }
}
