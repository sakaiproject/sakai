/**
 * Copyright (c) 2003-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *						 http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.authz.impl;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IllegalSecurityAdvisorException;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

import java.util.Stack;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdvisorTest {
	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock private FunctionManager functionManager;
	@Mock private AuthzGroupService authzGroupService;
	@Mock private EntityManager entityManager;
	@Mock private MemoryService memoryService;
	@Mock private ServerConfigurationService serverConfigurationService;
	@Mock private EventTrackingService eventTrackingService;
	@Mock private ThreadLocalManager threadLocalManager;

	private SakaiSecurity securityService;
	private Stack<SecurityAdvisor> advisorStack;

	@Before
	public void setUp() {
		advisorStack = new Stack<>();
		when(threadLocalManager.get(anyString())).thenReturn(advisorStack);

		securityService = new SakaiSecurity();
		securityService.setFunctionManager(functionManager);
		securityService.setAuthzGroupService(authzGroupService);
		securityService.setEntityManager(entityManager);
		securityService.setMemoryService(memoryService);
		securityService.setServerConfigurationService(serverConfigurationService);
		securityService.setEventTrackingService(eventTrackingService);
		securityService.setThreadLocalManager(threadLocalManager);
	}

	@Test
	public void testAdvisorOrder() throws Exception {

		SecurityAdvisor siteUpdAdvisor = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				if (function.equals("site.upd") && reference.equals("/site/mercury")) {
					return SecurityAdvice.ALLOWED;
				}
				else {
					return SecurityAdvice.PASS;
				}
			}
		};
					
		SecurityAdvisor passAllAdvisor = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvice.ALLOWED;
			}
		};
					

		// Push one advisor at a time
		securityService.pushAdvisor(passAllAdvisor);
		SecurityAdvisor sa2 = securityService.popAdvisor(passAllAdvisor);

		securityService.pushAdvisor(siteUpdAdvisor);
		SecurityAdvisor sa1 = securityService.popAdvisor(siteUpdAdvisor);

		if (!sa1.equals(siteUpdAdvisor)) {
			fail("The siteUpdAdvisor was not popped");
		}
		if (!sa2.equals(passAllAdvisor)) {
			fail("The siteUpdAdvisor was not popped");
		}

		// Push two advisors and then pop in correct LIFO order
		securityService.pushAdvisor(passAllAdvisor);
		securityService.pushAdvisor(siteUpdAdvisor);

		SecurityAdvisor sa3 = securityService.popAdvisor(siteUpdAdvisor);
		SecurityAdvisor sa4 = securityService.popAdvisor(passAllAdvisor);

		if (!sa3.equals(siteUpdAdvisor)) {
			fail("The siteUpdAdvisor was not popped");
		}
		if (!sa4.equals(passAllAdvisor)) {
			fail("The siteUpdAdvisor was not popped");
		}

		// Push two advisors and then try to pop in wrong order 
		securityService.pushAdvisor(passAllAdvisor);
		securityService.pushAdvisor(siteUpdAdvisor);

		try {
			securityService.popAdvisor(passAllAdvisor);
			securityService.popAdvisor(siteUpdAdvisor);
		} catch (IllegalSecurityAdvisorException e) {
			// Out of order advisors should result in a destroyed stack and an exception
		}
		catch (Exception e) {
			fail("Wrong type of exception thrown");
		}
	}
}
