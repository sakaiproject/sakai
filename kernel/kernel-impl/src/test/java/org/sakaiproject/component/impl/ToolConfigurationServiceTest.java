/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.component.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class ToolConfigurationServiceTest {

    private ToolConfigurationService service;

    @Before
    public void setUp() {
        service = new ToolConfigurationService();
    }

    @Test
    public void testEmptyInit() {
        service.init();
    }

    @Test
    public void emptyToolOrderFile() {
        service.setToolOrderFile(getClass().getResource("emptyToolOrder.xml").getFile());
        service.init();
    }

    @Test
    public void emptyToolGroupFile() {
        service.setToolOrderFile(getClass().getResource("emptyToolGroups.xml").getFile());
        service.setUseToolGroup(true);
        service.init();
    }

    @Test
    public void simpleToolGroups() {
        service.setToolOrderFile(getClass().getResource("simpleToolGroups.xml").getFile());
        service.setUseToolGroup(true);
        service.init();

        List<String> group1 = service.getToolGroup("group 1");
        assertNotNull(group1);
        assertThat(group1, hasItems("tool1", "tool2", "tool3"));

        List<String> empty = service.getToolGroup("empty");
        assertNotNull(empty);
        assertTrue(empty.isEmpty());

        List<String> project = service.getCategoryGroups("project");
        assertNotNull(project);
        assertThat(project, hasItems("group 1", "empty"));

        assertTrue(service.toolGroupIsRequired("group 1", "tool1"));
        assertFalse(service.toolGroupIsRequired("group 1", "tool2"));
        assertFalse(service.toolGroupIsRequired("group 1", "tool3"));

        assertTrue(service.toolGroupIsSelected("group 1", "tool1"));
        assertFalse(service.toolGroupIsSelected("group 1", "tool2"));
        assertFalse(service.toolGroupIsSelected("group 1", "tool3"));

        assertFalse(service.toolGroupIsSelected("empty", "tool1"));
        assertFalse(service.toolGroupIsRequired("empty", "tool1"));
    }

    @Test
    public void simpleToolOrder() {
        service.setToolOrderFile(getClass().getResource("simpleToolOrder.xml").getFile());
        service.init();

        List<String> group1 = service.getToolOrder("project");
        assertNotNull(group1);
        assertThat(group1, hasItems("tool1", "tool2", "tool3"));

        List<String> empty = service.getToolGroup("course");
        assertNotNull(empty);
        assertTrue(empty.isEmpty());

        List<String> projectRequired = service.getToolsRequired("project");
        assertThat(projectRequired, hasItems("tool1"));
        assertThat(projectRequired, not(hasItems("tool2", "tool3")));

        List<String> projectDefault = service.getDefaultTools("project");
        assertThat(projectDefault, hasItems("tool1"));
        assertThat(projectDefault, not(hasItems("tool2", "tool3")));

        assertTrue(service.getToolsRequired("course").isEmpty());
        assertTrue(service.getDefaultTools("course").isEmpty());
    }

}
