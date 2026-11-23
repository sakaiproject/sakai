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

package org.sakaiproject.tool.assessment.ui.listener.author;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.GradeItemNameTooLongException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.InvalidGradeItemNameException;
import org.sakaiproject.samigo.impl.SamigoAvailableNotificationServiceImpl;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.SamigoAppTestConfiguration;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.ExtendedTimeFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.helper.impl.AgentHelperImpl;
import org.sakaiproject.tool.assessment.integration.helper.impl.CalendarServiceHelperImpl;
import org.sakaiproject.tool.assessment.integration.helper.impl.GradebookServiceHelperImpl;
import org.sakaiproject.tool.assessment.shared.impl.assessment.SecureDeliveryServiceImpl;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SavePublishedSettingsListenerTest {
    private static final MockServletContext servletContext = new MockServletContext();
    private static AnnotationConfigWebApplicationContext context;
    private static ContextLoader contextLoader;
    private GradingService gradebook;
    private FacesContext facesContext;

    @BeforeClass
    public static void startContext() {
        ComponentManager.testingMode = true;
        context = new AnnotationConfigWebApplicationContext();
        context.register(TestConfiguration.class);
        contextLoader = new ContextLoader(context);
        contextLoader.initWebApplicationContext(servletContext);
    }

    @AfterClass
    public static void stopContext() {
        contextLoader.closeWebApplicationContext(servletContext);
    }

    @Before
    public void setup() {
        gradebook = context.getBean(GradingService.class);
        facesContext = context.getBean(FacesContext.class);
        ToolManager toolManager = context.getBean(ToolManager.class);
        reset(gradebook, facesContext, toolManager);
        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn("site");
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        ComponentManager.loadComponent(ToolManager.class, toolManager);
        ComponentManager.loadComponent(ThreadLocalManager.class, context.getBean(ThreadLocalManager.class));
        ComponentManager.loadComponent(GradingService.class, gradebook);
        ComponentManager.loadComponent("agentHelper", context.getBean(AgentHelperImpl.class));
        when(gradebook.isExternalAssignmentDefined("site", "101")).thenReturn(true);
    }

    @Test
    public void duplicateTitleDoesNotDisableExport() {
        assertRejectedTitle(new ConflictingAssignmentNameException("duplicate"),
                EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString());
    }

    @Test
    public void overlongTitleDoesNotDisableExport() {
        assertRejectedTitle(new GradeItemNameTooLongException("too long"),
                EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString());
        verify(facesContext).addMessage(isNull(), argThat(message -> message.getSummary().contains("255 characters or fewer")));
    }

    @Test
    public void invalidNameKeepsExistingExportFailureBehavior() {
        assertRejectedTitle(new InvalidGradeItemNameException("reserved prefix"), "0");
    }

    private void assertRejectedTitle(RuntimeException rejection, String expectedExport) {
        PublishedAssessmentData data = new PublishedAssessmentData();
        data.setPublishedAssessmentId(101L);
        data.setTitle("Title");
        data.setStatus(AssessmentBaseIfc.ACTIVE_STATUS);
        data.setCategoryId(10L);
        data.setSectionSet(new java.util.HashSet<>());
        data.setAssessmentAccessControl(new PublishedAccessControl());
        PublishedEvaluationModel evaluation = new PublishedEvaluationModel();
        evaluation.setAssessmentBase(data);
        data.setEvaluationModel(evaluation);
        PublishedAssessmentFacade assessment = new PublishedAssessmentFacade(data);
        PublishedAssessmentSettingsBean settings = new PublishedAssessmentSettingsBean();
        settings.setToDefaultGradebook(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString());
        doThrow(rejection).when(gradebook).updateExternalAssessment(anyString(), anyString(), any(), any(),
                anyString(), any(), any(), any(), any());

        assertFalse(context.getBean(SavePublishedSettingsListener.class)
                .updateGB(settings, assessment, assessment, true, false, facesContext));
        assertEquals(expectedExport, evaluation.getToGradeBook());
        verify(gradebook).updateExternalAssessment(eq("site"), eq("101"), any(), any(), eq("Title"), any(), any(), any(), any());
        verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    @Configuration
    @Import(SamigoAppTestConfiguration.class)
    public static class TestConfiguration {
        @Bean public SavePublishedSettingsListener listener() { return new SavePublishedSettingsListener(); }
        @Bean(name = "org.sakaiproject.grading.api.GradingService")
        public GradingService gradebook() { return mock(GradingService.class); }
        @Bean(name = "org.sakaiproject.tool.api.ToolManager") public ToolManager toolManager() { return mock(ToolManager.class); }
        @Bean public ThreadLocalManager threadLocalManager() { return mock(ThreadLocalManager.class); }
        @Bean public FacesContext facesContext() { return mock(FacesContext.class); }
        @Bean(name = "org.sakaiproject.tool.api.SessionManager")
        public org.sakaiproject.tool.api.SessionManager sessions() { return mock(org.sakaiproject.tool.api.SessionManager.class); }
        @Bean(name = "org.sakaiproject.time.api.UserTimeService")
        public org.sakaiproject.time.api.UserTimeService userTime() { return mock(org.sakaiproject.time.api.UserTimeService.class); }
        @Bean(name = "org.sakaiproject.section.api.SectionAwareness")
        public org.sakaiproject.section.api.SectionAwareness sections() { return mock(org.sakaiproject.section.api.SectionAwareness.class); }
        @Bean(name = "AuthzQueriesFacade")
        public org.sakaiproject.tool.assessment.facade.authz.AuthzQueriesFacade authz() {
            return new org.sakaiproject.tool.assessment.facade.authz.AuthzQueriesFacade();
        }
        @Bean(name = "org.sakaiproject.event.api.EventTrackingService")
        public EventTrackingService events() { return mock(EventTrackingService.class); }
        @Bean(name = "gradebookServiceHelper")
        public GradebookServiceHelperImpl gradebookHelper() { return new GradebookServiceHelperImpl(); }
        @Bean public AgentHelperImpl agentHelper() { return new AgentHelperImpl(); }
        @Bean(name = "calendarServiceHelper")
        public CalendarServiceHelperImpl calendarHelper() { return new CalendarServiceHelperImpl(); }
        @Bean(name = "org.sakaiproject.tool.assessment.facade.ExtendedTimeFacade")
        public ExtendedTimeFacade extendedTime() { return new ExtendedTimeFacade(); }
        @Bean(name = "SecureDeliveryServiceAPI")
        public SecureDeliveryServiceImpl secureDelivery() { return new SecureDeliveryServiceImpl(); }
        @Bean(name = "org.sakaiproject.samigo.api.SamigoAvailableNotificationService")
        public SamigoAvailableNotificationServiceImpl notifications() { return new SamigoAvailableNotificationServiceImpl(); }
    }
}
