/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.sakaiproject.test;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * This is just a simple example of an service-integrated test based on the SakaiTestBase class.
 * For more complex examples, see the integration-test projects in "component", "user", "provider", etc.
 */
public class SampleSakaiTest extends SakaiTestBase {
	private static final String GUEST_EID = "joeuser";
	private UserDirectoryService userDirectoryService;
	
	// Required boilerplate.
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(SampleSakaiTest.class)) {
			protected void setUp() throws Exception {
				oneTimeSetup();
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}

	public void setUp() throws Exception {
		userDirectoryService = getService(UserDirectoryService.class);
		userDirectoryService.addUser(null, GUEST_EID, "J. " + GUEST_EID, "de " + GUEST_EID, 
			GUEST_EID + "@somewhere.edu", GUEST_EID + "pwd", "Guest", null);

	}

	public void testUserStorage() throws Exception {
		Assert.assertNotNull(userDirectoryService);
		User user = userDirectoryService.getUserByEid(GUEST_EID);
		Assert.assertEquals("Guest", user.getType());
	}

}
