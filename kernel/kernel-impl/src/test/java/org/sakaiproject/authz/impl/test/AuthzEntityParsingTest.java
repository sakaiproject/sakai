/**
 * Copyright (c) 2003-2013 The Apereo Foundation
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
package org.sakaiproject.authz.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.sakaiproject.authz.api.AuthzGroupService.APPLICATION_ID;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.authz.impl.BaseAuthzGroupService;
import org.sakaiproject.authz.impl.DbAuthzGroupService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.impl.EntityManagerComponent;
import org.sakaiproject.entity.impl.ReferenceComponent;


/**
 * This just checks that the parsing of entity references by the Authz service works.
 * @author Matthew Buckett
 *
 */
public class AuthzEntityParsingTest {
	private BaseAuthzGroupService authz;

	private Reference ref;

	@Before
	public void setUp() {
		authz = new DbAuthzGroupService();
		ref = new ReferenceComponent(new EntityManagerComponent(), null);
	}

	@Test
	public void testEmpty() {
		assertFalse(authz.parseEntityReference("", ref));
	}
	
	@Test
	public void testLeadingSlash() {
		assertFalse(authz.parseEntityReference("/realm", ref));
	}
	
	@Test
	public void testOtherEntity() {
		assertFalse(authz.parseEntityReference("/site/mercury", ref));
	}
	
	@Test
	public void testNoId() {
		assertTrue(authz.parseEntityReference("/realm/", ref));
		assertEquals(APPLICATION_ID, ref.getType());
		assertNull(ref.getId());
	}

	@Test
	public void testSiteRealm() {
		assertTrue(authz.parseEntityReference("/realm//site/mercury", ref));
		assertRealmReference("/site/mercury");
	}

	@Test
	public void testSpecialSite() {
		assertTrue(authz.parseEntityReference("/realm/!site.helper", ref));
		assertRealmReference("!site.helper");
	}

	private void assertRealmReference(String id) {
		assertEquals(APPLICATION_ID, ref.getType());
		assertEquals(id, ref.getId());
		assertEquals("", ref.getSubType());
		assertNull(ref.getContainer());
		assertNull(ref.getContext());
	}
}
