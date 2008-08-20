/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.authz.impl.test;

import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class AuthzIntegrationTest extends SakaiKernelTestBase {
	private static final Log log = LogFactory.getLog(AuthzIntegrationTest.class);
	
	private AuthzGroupService authzGroupService;
	
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(AuthzIntegrationTest.class)) {
			protected void setUp() throws Exception {
				log.debug("starting setup");
				oneTimeSetup(null);
				log.debug("finished setup");
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}

	public void setUp() throws Exception {
		log.debug("Setting up an AuthzIntegrationTest test");
		
		// Connect to the authz service
		authzGroupService = (AuthzGroupService)getService(AuthzGroupService.class.getName());
		
		// This test relies on a GroupProvider that uses '+'  to concatenate provider IDs.
		GroupProvider groupProvider = (GroupProvider)getService(GroupProvider.class.getName());
		Assert.assertNotNull(groupProvider);
		Assert.assertEquals(2, groupProvider.unpackId("this+that").length);
		
		// Log in
		SessionManager sessionManager = (SessionManager)getService(SessionManager.class.getName());
		Session session = sessionManager.getCurrentSession();
		session.setUserId("admin");
		
		// Create some authz groups
		AuthzGroup foo = authzGroupService.addAuthzGroup("internal-foo");
		foo.setProviderGroupId("enterprise-foo1+enterprise-foo2");
		authzGroupService.save(foo);

		AuthzGroup bar = authzGroupService.addAuthzGroup("internal-bar");
		bar.setProviderGroupId("enterprise-bar");
		authzGroupService.save(bar);
	}
	
	public void tearDown() throws Exception {
		log.debug("Tearing down an AuthzIntegrationTest test");
		
		// Remove the authz groups created for testing
		authzGroupService.removeAuthzGroup("internal-foo");
		authzGroupService.removeAuthzGroup("internal-bar");

		// Dereference the service
		authzGroupService = null;
	}

	public void testGetProviderIds() throws Exception {
		Set fooProviderIds = authzGroupService.getProviderIds("internal-foo");
		Assert.assertEquals(2, fooProviderIds.size());
		Assert.assertTrue(fooProviderIds.contains("enterprise-foo1"));
		Assert.assertTrue(fooProviderIds.contains("enterprise-foo2"));
		
		Set barProviderIds = authzGroupService.getProviderIds("internal-bar");
		Assert.assertEquals(1, barProviderIds.size());
		Assert.assertTrue(barProviderIds.contains("enterprise-bar"));
	}
	
	public void testGetAuthzGroupIds() throws Exception {
		Set fooAuthzGroupIds = authzGroupService.getAuthzGroupIds("enterprise-foo1");
		Assert.assertEquals(1, fooAuthzGroupIds.size());
		Assert.assertTrue(fooAuthzGroupIds.contains("internal-foo"));
	}
}
