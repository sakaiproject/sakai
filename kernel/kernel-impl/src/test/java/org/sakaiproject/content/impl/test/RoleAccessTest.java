package org.sakaiproject.content.impl.test;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.*;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class RoleAccessTest extends SakaiKernelTestBase {

    private static final Log log = LogFactory.getLog(RoleAccessTest.class);

    protected static final String SITE_ID           = "site-id";
    protected static final String IMAGES_COLLECTION = String.format("/group/%s/images/", SITE_ID);
    protected static final String PHOTOS_COLLECTION = String.format("/group/%s/images/photos/", SITE_ID);
    protected static final String TEST_ROLE         = "com.roles.test";
    protected static final String TEST_ROLE_2       = "net.roles.test";

    protected ContentHostingService _chs;
    protected AuthzGroupService _ags;
    protected SiteService _ss;

    protected ContentCollectionEdit collectionEdit;
    protected String _groupReference;

    @BeforeClass
    public static void beforeClass() throws Exception {
        log.debug("starting oneTimeSetup");
        oneTimeSetup(null);
        log.debug("finished oneTimeSetup");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        log.debug("starting tearDown");
        oneTimeTearDown();
        log.debug("finished tearDown");
    }

    @Before
    public void setUp() throws IdUsedException, IdInvalidException, InconsistentException, PermissionException, IdUnusedException {
        _chs = (ContentHostingService)getService(ContentHostingService.class.getName());
        _ags = (AuthzGroupService)getService(AuthzGroupService.class.getName());

        SessionManager sm = (SessionManager)getService(SessionManager.class.getName());
        Session session = sm.getCurrentSession();
        session.setUserEid("admin");
        session.setUserId("admin");

        _ss = (SiteService)getService(SiteService.class.getName());
        Site newSite = _ss.addSite(SITE_ID, (String) null);
        Group group = newSite.addGroup();
        group.setTitle(".group");
        _groupReference = group.getReference();
        _ss.save(newSite);

        collectionEdit = _chs.addCollection(IMAGES_COLLECTION);
        _chs.commitCollection(collectionEdit);

        collectionEdit = _chs.addCollection(PHOTOS_COLLECTION);
        _chs.commitCollection(collectionEdit);
    }

    @After
    public void tearDown() throws IdUnusedException, PermissionException, InUseException, TypeException, ServerOverloadException, AuthzPermissionException {
        _chs.removeCollection(PHOTOS_COLLECTION);
        _ags.removeAuthzGroup(_chs.getReference(PHOTOS_COLLECTION));
        _chs.removeCollection(IMAGES_COLLECTION);
        _ags.removeAuthzGroup(_chs.getReference(IMAGES_COLLECTION));
        // Mark as deleted.
        _ss.removeSite(_ss.getSite(SITE_ID));
        // Remove the deleted site.
        _ss.removeSite(_ss.getSite(SITE_ID));
    }

    @Test
    public void testAddAndRemoveRoleAccess() throws IdUnusedException, PermissionException, InUseException, TypeException, InconsistentException {
        assertFalse(_chs.isRoleView(PHOTOS_COLLECTION, TEST_ROLE));

        collectionEdit = _chs.editCollection(PHOTOS_COLLECTION);
        collectionEdit.addRoleAccess(TEST_ROLE);
        _chs.commitCollection(collectionEdit);
        assertTrue(_chs.isRoleView(PHOTOS_COLLECTION, TEST_ROLE));

        collectionEdit = _chs.editCollection(PHOTOS_COLLECTION);
        collectionEdit.removeRoleAccess(TEST_ROLE);
        _chs.commitCollection(collectionEdit);
        assertFalse(_chs.isRoleView(PHOTOS_COLLECTION, TEST_ROLE));
    }

    @Test
    public void testGetAccessRoleIds() throws IdUnusedException, PermissionException, InUseException, TypeException, InconsistentException {
        collectionEdit = _chs.editCollection(PHOTOS_COLLECTION);
        collectionEdit.addRoleAccess(TEST_ROLE);
        collectionEdit.addRoleAccess(TEST_ROLE_2);
        _chs.commitCollection(collectionEdit);

        ContentCollection collection = _chs.getCollection(PHOTOS_COLLECTION);
        assertTrue(collection.getRoleAccessIds().contains(TEST_ROLE));
        assertTrue(collection.getRoleAccessIds().contains(TEST_ROLE_2));
    }

    @Test
    public void testGetInheritedAccessRoleIds() throws IdUnusedException, PermissionException, InUseException, TypeException, InconsistentException, IdInvalidException, IdUsedException, ServerOverloadException, AuthzPermissionException {
        collectionEdit = _chs.editCollection(PHOTOS_COLLECTION);
        collectionEdit.addRoleAccess(TEST_ROLE_2);
        _chs.commitCollection(collectionEdit);

        collectionEdit = _chs.editCollection(IMAGES_COLLECTION);
        collectionEdit.addRoleAccess(TEST_ROLE);
        _chs.commitCollection(collectionEdit);

        ContentCollection collection = _chs.getCollection(PHOTOS_COLLECTION);
        assertTrue(collection.getInheritedRoleAccessIds().contains(TEST_ROLE));
        assertFalse(collection.getInheritedRoleAccessIds().contains(TEST_ROLE_2));
    }

    @Test
    public void testRoleAccessFailsWhenAlreadyInherited() throws Exception {
        collectionEdit = _chs.editCollection(IMAGES_COLLECTION);
        collectionEdit.addRoleAccess(TEST_ROLE);
        _chs.commitCollection(collectionEdit);

        collectionEdit = _chs.editCollection(PHOTOS_COLLECTION);
        try {
            collectionEdit.addRoleAccess(TEST_ROLE);
            fail();
        } catch (InconsistentException e) {
            // instead we should go here
        }
        _chs.commitCollection(collectionEdit);
    }

    @Test
    public void testRoleAccessDoesNotFailWhenGeneralRoleAccessIsInherited() throws Exception {
        collectionEdit = _chs.editCollection(IMAGES_COLLECTION);
        collectionEdit.addRoleAccess(TEST_ROLE);
        _chs.commitCollection(collectionEdit);

        collectionEdit = _chs.editCollection(PHOTOS_COLLECTION);
        try {
            collectionEdit.addRoleAccess(TEST_ROLE_2);
        } finally {
            _chs.commitCollection(collectionEdit);
        }
    }

    @Test
    public void testGroupAccessDoesNotFailWhenRoleAccessIsInherited() throws IdUnusedException, TypeException, InUseException, PermissionException, InconsistentException {

        List<String> groupsList = Collections.singletonList(_groupReference);

        collectionEdit = _chs.editCollection(IMAGES_COLLECTION);
        collectionEdit.addRoleAccess(TEST_ROLE);
        _chs.commitCollection(collectionEdit);

        collectionEdit = _chs.editCollection(PHOTOS_COLLECTION);
        try {
            collectionEdit.setGroupAccess(groupsList);
        } finally {
            _chs.commitCollection(collectionEdit);
        }
    }

    @Test
    public void testRoleAccessFailsWhenGroupAccessIsInherited() throws IdUnusedException, TypeException, InUseException, PermissionException, InconsistentException {
        collectionEdit = _chs.editCollection(IMAGES_COLLECTION);
        collectionEdit.setGroupAccess(Collections.singleton(_groupReference));
        _chs.commitCollection(collectionEdit);

        collectionEdit = _chs.editCollection(PHOTOS_COLLECTION);
        try {
            collectionEdit.addRoleAccess(TEST_ROLE);
            fail("Should have triggered an Inconsistent Exception because role access is inherited.");
        } catch (InconsistentException e) {
            // instead we should go here
        }
        _chs.commitCollection(collectionEdit);
    }

}

