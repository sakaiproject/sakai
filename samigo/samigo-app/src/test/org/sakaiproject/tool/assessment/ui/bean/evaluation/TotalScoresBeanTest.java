/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.facade.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

@WebAppConfiguration("src/webapp")
@ContextConfiguration(classes = TotalScoresBeanTestConfiguration.class)
public class TotalScoresBeanTest extends AbstractJUnit4SpringContextTests {
    @Autowired private TotalScoresBean bean;
    @Autowired private WebApplicationContext context;
    @Autowired private SectionAwareness sectionAwareness;
    @Autowired private SecurityService securityService;

    @BeforeClass
    public static void initializeComponentManager() {
        ComponentManager.testingMode = true;
    }

    @Before
    public void initializeWebContext() {
        context.getServletContext().removeAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        new ContextLoader(context).initWebApplicationContext(context.getServletContext());
        ComponentManager.loadComponent(SecurityService.class, securityService);
    }

    @Test
    public void flowStateRoundTripPreservesFormDataAndRestoresServiceAccess() {
        bean.setPublishedId("123");
        bean.setSelectedSectionFilterValue(TotalScoresBean.ALL_SECTIONS_SELECT_VALUE);
        when(sectionAwareness.getSiteMembersInRole("test-site", Role.STUDENT)).thenReturn(Collections.emptyList());

        // FlowStateComponent embeds this bean in the serialized JSF view state.
        TotalScoresBean restored = SerializationUtils.roundtrip(bean);

        assertEquals("123", restored.getPublishedId());
        assertEquals(TotalScoresBean.ALL_SECTIONS_SELECT_VALUE, restored.getSelectedSectionFilterValue());
        assertTrue(restored.getUserIdMap(TotalScoresBean.CALLED_FROM_HISTOGRAM_LISTENER_STUDENT, "test-site").isEmpty());
        verify(securityService).unlock(SamigoConstants.AUTHZ_ASSESSMENT_ALL_GROUPS, "/site/test-site");
        verify(sectionAwareness).getSiteMembersInRole("test-site", Role.STUDENT);
    }
}
