package org.sakaiproject.authz.impl;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.test.SakaiKernelTestBase;

public class DummyUserEncodingTest extends SakaiKernelTestBase {

    protected AuthzGroupService _ags;
    protected String _secretPrefix;

    public static Test suite() {
        TestSetup setup = new TestSetup(new TestSuite(DummyUserEncodingTest.class)) {
            protected void setUp() throws Exception {
                oneTimeSetup();
            }
            protected void tearDown() throws Exception {
                oneTimeTearDown();
            }
        };
        return setup;
    }

    public void setUp() {
        _ags = (AuthzGroupService)getService(AuthzGroupService.class.getName());
        // this is different for each instance of AuthzGroupService
        _secretPrefix = _ags.encodeDummyUserForRole("DropMe").replaceAll("DropMe", "");
    }

    public void testInvalidEncoding() {
        try {
            _ags.encodeDummyUserForRole("");
            fail();
        } catch (IllegalArgumentException e) {
            // should get here
        }

        try {
            _ags.encodeDummyUserForRole(null);
            fail();
        } catch(IllegalArgumentException e) {
            //should get here
        }
    }

    public void testInvalidDecoding() {
        try {
            _ags.decodeRoleFromDummyUser(null);
            fail();
        } catch (IllegalArgumentException e) {
            // should get here
        }

        try {
            _ags.decodeRoleFromDummyUser("");
            fail();
        } catch (IllegalArgumentException e) {
            // should get here
        }

        assertNull(_ags.decodeRoleFromDummyUser(_secretPrefix));
        assertNull(_ags.decodeRoleFromDummyUser("YouNeverEncodedMe"));
    }

    public void testCanGetBackToWhereWeStarted() {
        assertEquals(".start.point", _ags.decodeRoleFromDummyUser(_ags.encodeDummyUserForRole(".start.point")));
    }
}
