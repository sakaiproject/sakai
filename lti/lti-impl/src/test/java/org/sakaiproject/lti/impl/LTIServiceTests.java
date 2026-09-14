/**
 * Copyright (c) 2026 The Apereo Foundation
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
 */
package org.sakaiproject.lti.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;


import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.model.LtiTool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(classes = { LtiTestConfiguration.class })
public class LTIServiceTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private LTIService ltiService;

    @Test
    public void getToolsLaunchableInNavigation() {

        String title1 = "Tool One";
        Long tool1Id = insertTool(title1, LTIService.ADMIN_SITE);
        String title2 = "Tool Two";
        Long tool2Id = insertTool(title2, LTIService.ADMIN_SITE);

        List<Map<String, Object>> tools = ltiService.getToolsLaunchCourseNav("site1", true);
        assertEquals(2, tools.size());
    }

    private Long insertTool(String title, String siteId) {

        Map<String, Object> props = new HashMap<>();
        props.put(LTIService.LTI_TITLE, title);
        props.put(LTIService.LTI_MT_LAUNCH, 1);
        props.put(LTIService.LTI_VISIBLE, 1);
        props.put(LTIService.LTI_PL_COURSENAV, 1);
        props.put(LTIService.LTI_LAUNCH, "https://foo.com/launch");

        Long toolId = (Long) ltiService.insertTool(props, siteId);
        assertNotNull(toolId);
        return toolId;
    }
}
