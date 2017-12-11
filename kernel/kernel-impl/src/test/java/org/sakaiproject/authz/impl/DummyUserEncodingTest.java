/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.test.SakaiKernelTestBase;

@Slf4j
public class DummyUserEncodingTest extends SakaiKernelTestBase {
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
