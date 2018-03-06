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
package org.sakaiproject.content.impl.test;

import lombok.extern.slf4j.Slf4j;

import org.junit.*;

import org.sakaiproject.authz.api.*;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.*;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import static org.junit.Assert.*;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

@FixMethodOrder(NAME_ASCENDING)
@Slf4j
public class RoleViewTest extends SakaiKernelTestBase {
    protected static final String PHOTOS_COLLECTION = "/private/images/photos/";
    protected static final String TEST_ROLE         = "com.roles.test";

    protected ContentHostingService _chs;
    protected AuthzGroupService _ags;

    @BeforeClass
    public static void beforeClass() throws Exception
    {
        log.debug("starting oneTimeSetup");
        oneTimeSetup(null);
        log.debug("finished oneTimeSetup");
    }

    @AfterClass
    public static void afterClass() throws Exception
    {
        log.debug("starting tearDown");
        oneTimeTearDown();
        log.debug("finished tearDown");
    }

    @Before
    public void setUp() throws IdUsedException, IdInvalidException, InconsistentException, PermissionException {
        _chs = (ContentHostingService)getService(ContentHostingService.class.getName());
        _ags = (AuthzGroupService)getService(AuthzGroupService.class.getName());

        SessionManager sm = (SessionManager)getService(SessionManager.class.getName());
        Session session = sm.getCurrentSession();
        session.setUserEid("admin");
        session.setUserId("admin");
        ContentCollectionEdit collectionEdit = _chs.addCollection(PHOTOS_COLLECTION);
        _chs.commitCollection(collectionEdit);
    }

    @After
    public void tearDown() throws IdUnusedException, PermissionException, InUseException, TypeException, ServerOverloadException, AuthzPermissionException {
        _chs.removeCollection(PHOTOS_COLLECTION);
        _ags.removeAuthzGroup(_chs.getReference(PHOTOS_COLLECTION));
    }

    @Test
    public void testSetPubView() {
        _chs.setPubView(PHOTOS_COLLECTION, true);
        assertTrue(hasRealmAndRole(PHOTOS_COLLECTION, AuthzGroupService.ANON_ROLE));
    }

    @Test
    public void testSetRoleView() throws AuthzPermissionException {
        _chs.setRoleView(PHOTOS_COLLECTION, TEST_ROLE, true);
        assertTrue(hasRealmAndRole(PHOTOS_COLLECTION, TEST_ROLE));
    }

    private boolean hasRealmAndRole(String contentId, String roleId) {
        AuthzGroup realm = null;
        try {
            realm = _ags.getAuthzGroup(_chs.getReference(contentId));
        } catch (GroupNotDefinedException e) {
            fail("Group is not defined for content " + e);
        }

        Role role = realm.getRole(roleId);
        if (role == null) {
            fail("Role is not defined for the content realm " + realm.getId());
        }

        if (!role.isAllowed(ContentHostingService.AUTH_RESOURCE_READ)) {
            fail("Read access is not defined for the role");
        }
        return true;
    }

    @Test
    public void testPubView() {
        assertFalse(_chs.isPubView(PHOTOS_COLLECTION));
        assertTrue(_chs.getRoleViews(PHOTOS_COLLECTION).isEmpty());

        _chs.setPubView(PHOTOS_COLLECTION, true);
        assertTrue(_chs.getRoleViews(PHOTOS_COLLECTION).contains(AuthzGroupService.ANON_ROLE));
        assertTrue(_chs.isPubView(PHOTOS_COLLECTION));
        assertTrue(_chs.isRoleView(PHOTOS_COLLECTION, AuthzGroupService.ANON_ROLE));

        _chs.setPubView(PHOTOS_COLLECTION, false);
        assertTrue(_chs.getRoleViews(PHOTOS_COLLECTION).isEmpty());
        assertFalse(_chs.isPubView(PHOTOS_COLLECTION));
        assertFalse(_chs.isRoleView(PHOTOS_COLLECTION, AuthzGroupService.ANON_ROLE));
    }

    @Test
    public void testRoleView() throws AuthzPermissionException {
        _chs.setPubView(PHOTOS_COLLECTION, true);
        assertFalse(_chs.isRoleView(PHOTOS_COLLECTION, TEST_ROLE));
        assertTrue(_chs.getRoleViews(PHOTOS_COLLECTION).size() == 1);

        _chs.setRoleView(PHOTOS_COLLECTION, TEST_ROLE, true);
        assertTrue(_chs.getRoleViews(PHOTOS_COLLECTION).contains(TEST_ROLE));
        assertTrue(_chs.isRoleView(PHOTOS_COLLECTION, TEST_ROLE));

        _chs.setRoleView(PHOTOS_COLLECTION, TEST_ROLE, false);
        assertTrue(_chs.getRoleViews(PHOTOS_COLLECTION).size() == 1);
        assertFalse(_chs.isRoleView(PHOTOS_COLLECTION, TEST_ROLE));
    }

}
