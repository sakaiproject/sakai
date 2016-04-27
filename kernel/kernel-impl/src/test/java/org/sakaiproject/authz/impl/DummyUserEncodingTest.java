package org.sakaiproject.authz.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.test.SakaiKernelTestBase;

public class DummyUserEncodingTest extends SakaiKernelTestBase {
	private static Logger log = LoggerFactory.getLogger(DummyUserEncodingTest.class);
    protected AuthzGroupService _ags;
    protected String _secretPrefix;

	@BeforeClass
	public static void beforeClass() {
		try {
            log.debug("starting oneTimeSetup");
			oneTimeSetup();
            log.debug("finished oneTimeSetup");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	@Before
    public void setUp() {
        _ags = (AuthzGroupService)getService(AuthzGroupService.class.getName());
        // this is different for each instance of AuthzGroupService
        _secretPrefix = _ags.encodeDummyUserForRole("DropMe").replaceAll("DropMe", "");
    }

	@Test
    public void testInvalidEncoding() {
        try {
            _ags.encodeDummyUserForRole("");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // should get here
        }

        try {
            _ags.encodeDummyUserForRole(null);
            Assert.fail();
        } catch(IllegalArgumentException e) {
            //should get here
        }
    }

	@Test
    public void testInvalidDecoding() {
        try {
            _ags.decodeRoleFromDummyUser(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // should get here
        }

        try {
            _ags.decodeRoleFromDummyUser("");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // should get here
        }

        Assert.assertNull(_ags.decodeRoleFromDummyUser(_secretPrefix));
        Assert.assertNull(_ags.decodeRoleFromDummyUser("YouNeverEncodedMe"));
    }

	@Test
    public void testCanGetBackToWhereWeStarted() {
    	Assert.assertEquals(".start.point", _ags.decodeRoleFromDummyUser(_ags.encodeDummyUserForRole(".start.point")));
    }
}
