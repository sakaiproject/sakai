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
package org.sakaiproject.authz.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

public class SakaiSecurityTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock private FunctionManager functionManager;
    @Mock private AuthzGroupService authzGroupService;
    @Mock private EntityManager entityManager;
    @Mock private MemoryService memoryService;
    @Mock private ServerConfigurationService serverConfigurationService;
    @Mock private EventTrackingService eventTrackingService;


    private SakaiSecurity sakaiSecurity;

    @Before
    public void setUp() {
        SakaiSecurityConcrete sakaiSecurity = new SakaiSecurityConcrete();
        sakaiSecurity.setFunctionManager(functionManager);
        sakaiSecurity.setAuthzGroupService(authzGroupService);
        sakaiSecurity.setEntityManager(entityManager);
        sakaiSecurity.setMemoryService(memoryService);
        sakaiSecurity.setServerConfigurationService(serverConfigurationService);
        sakaiSecurity.setEventTrackingService(eventTrackingService);

        // Always return default
        when(serverConfigurationService.getString(anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
        when(serverConfigurationService.getBoolean(anyString(), anyBoolean())).thenAnswer(invocation -> invocation.getArgument(1));
        when(serverConfigurationService.getBoolean(anyString(), anyBoolean())).thenAnswer(invocation -> invocation.getArgument(1));


        this.sakaiSecurity = sakaiSecurity;

    }

    @Test
    public void testCacheRealmPermsChangedSimple() throws GroupNotDefinedException {

        Cache cache = mock(Cache.class);
        when(memoryService.getCache("org.sakaiproject.authz.api.SecurityService.cache")).thenReturn(cache);
        sakaiSecurity.init();

        AuthzGroup group = new AuthzGroupBuilder(authzGroupService, "/site/1")
                .addMember("1", "role", true)
                .build();

        // This collects all the flushes
        Set<String> flushed = new HashSet<>();
        doAnswer(s -> flushed.addAll(s.getArgument(0))).when(cache).removeAll(any());

        sakaiSecurity.cacheRealmPermsChanged("/realm//site/1", singleton("role"), singleton("function"));

        assertThat(flushed, containsInAnyOrder("unlock@1@@function@/site/1"));
    }


    @Test
    public void testCacheRealmPermsChangedMultiple() throws GroupNotDefinedException {

        Cache cache = mock(Cache.class);
        when(memoryService.getCache("org.sakaiproject.authz.api.SecurityService.cache")).thenReturn(cache);
        sakaiSecurity.init();

        AuthzGroup group = new AuthzGroupBuilder(authzGroupService, "/site/1")
                .addMember("user1", "role1", true)
                .addMember("user2", "role2", true)
                .build();

        // This collects all the flushes
        Set<String> flushed = new HashSet<>();
        doAnswer(s -> flushed.addAll(s.getArgument(0))).when(cache).removeAll(any());

        sakaiSecurity.cacheRealmPermsChanged("/realm//site/1", singleton("role"), new HashSet<>(Arrays.asList("function1", "function2")));

        assertThat(flushed, containsInAnyOrder(
                "unlock@user1@@function1@/site/1",
                "unlock@user2@@function1@/site/1",
                "unlock@user1@@function2@/site/1",
                "unlock@user2@@function2@/site/1"
                ));
    }

    @Test
    public void testCacheRealmPermsChangedSimpleSV() throws GroupNotDefinedException {

        Cache cache = mock(Cache.class);
        when(memoryService.getCache("org.sakaiproject.authz.api.SecurityService.cache")).thenReturn(cache);
        when(serverConfigurationService.getString(eq("studentview.roles"), anyString())).thenReturn("student");
        sakaiSecurity.init();

        AuthzGroup group = new AuthzGroupBuilder(authzGroupService, "/site/1")
                .addMember("1", "role", true)
                .addMember("2", "student", true)
                .build();

        // This collects all the flushes
        Set<String> flushed = new HashSet<>();
        doAnswer(s -> flushed.addAll(s.getArgument(0))).when(cache).removeAll(any());

        sakaiSecurity.cacheRealmPermsChanged("/realm//site/1", singleton("role"), singleton("function"));

        assertThat(flushed, containsInAnyOrder("unlock@1@@function@/site/1", "unlock@2@@function@/site/1"));
    }

}
