package org.sakaiproject.authz.impl.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sakaiproject.authz.api.AuthzGroupService.APPLICATION_ID;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.impl.AuthzGroupServiceTest;
import org.sakaiproject.authz.impl.BaseAuthzGroupService;
import org.sakaiproject.entity.api.Reference;


/**
 * This just checks that the parsing of entity references by the Authz service works.
 * @author Matthew Buckett
 *
 */
@RunWith(JMock.class)
public class AuthzTestEntityParsing {
	public JUnit4Mockery context = new JUnit4Mockery();


	private BaseAuthzGroupService authz;

	private Reference ref;

	@Before
	public void setUp() {
		authz = new AuthzGroupServiceTest();
		ref = context.mock(Reference.class);
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
		context.checking(new Expectations() {{
			allowing(ref);
		}});
		assertTrue(authz.parseEntityReference("/realm/", ref));
	}
	
	@Test
	public void testSiteRealm() {
		context.checking(new Expectations(){{
			oneOf(ref).set(APPLICATION_ID, null, "/site/mercury", null, null);
		}});
		assertTrue(authz.parseEntityReference("/realm//site/mercury", ref));
	}
	
	@Test
	public void testSpecialSite() {
		context.checking(new Expectations() {{
			oneOf(ref).set(APPLICATION_ID, null, "!site.helper", null, null);
		}});
		assertTrue(authz.parseEntityReference("/realm/!site.helper", ref));
	}

}
