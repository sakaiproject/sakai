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
package org.sakaiproject.user.impl;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.api.FormattedText;

/**
 * For unit testing parts of the base user directory service
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Slf4j
public class BaseUserDirectoryServiceTest extends SakaiKernelTestBase  {
    private static String USER_SOURCE_PROPERTY = "user.source";

    private static Map<String, String> eidToId = new HashMap<String, String>();

    private BaseUserDirectoryService userDirectoryService;

    /**
     * Because framework service calls create so many irreversible side-effects,
     * we don't want to keep a single instance of the component manager alive
     * for all test classes. On the other hand, we'd rather not take the
     * overhead of starting up the component manager for every method in the
     * test class. As a compromise, we run a test suite consisting only of the
     * class itself.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void beforeClass() {
        try {
            if (log.isDebugEnabled()) log.debug("starting setup");
            oneTimeSetup();
            oneTimeSetupAfter();
            if (log.isDebugEnabled()) log.debug("finished setup");
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }
	
    private static void oneTimeSetupAfter() throws Exception {
        // This is a workaround until we can make it easier to load sakai.properties
        // for specific integration tests.
        DbUserService dbUserService = (DbUserService)getService(UserDirectoryService.class.getName());

        // Sakai user services very helpfully lowercase input EIDs rather than leaving them alone.
        addUserWithEid(dbUserService, "localuser");
        addUserWithEid(dbUserService, "localfromauthn");
    }

    private static User addUserWithEid(UserDirectoryService userDirectoryService, String eid) throws Exception {
        BaseResourceProperties props = new BaseResourceProperties();
        props.addProperty(USER_SOURCE_PROPERTY, "local");
        User user = userDirectoryService.addUser(null, eid, "J. " + eid, "de " + eid, eid + "@somewhere.edu", eid + "pwd", "Guest", props);
        eidToId.put(eid, user.getId());
        log.debug("addUser eid=" + eid + ", id=" + user.getId());
        return user;
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        log.debug("Setting up UserDirectoryServiceIntegrationTest");
        userDirectoryService = new BaseUserDirectoryService() {
            protected TimeService timeService() {
                return (TimeService)getService(TimeService.class.getName());
            }
            protected ThreadLocalManager threadLocalManager() {
                return (ThreadLocalManager)getService(ThreadLocalManager.class.getName());
            }
            protected SessionManager sessionManager() {
                return (SessionManager)getService(SessionManager.class.getName());
            }
            protected ServerConfigurationService serverConfigurationService() {
                return (ServerConfigurationService)getService(ServerConfigurationService.class.getName());
            }
            protected SecurityService securityService() {
                return (SecurityService)getService(SecurityService.class.getName());
            }
            protected Storage newStorage() {
                return (Storage)getService(Storage.class.getName());
            }
            protected MemoryService memoryService() {
                return (MemoryService)getService(MemoryService.class.getName());
            }
            protected IdManager idManager() {
                return (IdManager)getService(IdManager.class.getName());
            }
            protected FunctionManager functionManager() {
                return (FunctionManager)getService(FunctionManager.class.getName());
            }
            protected FormattedText formattedText() {
                return (FormattedText)getService(FormattedText.class.getName());
            }
            protected EventTrackingService eventTrackingService() {
                return (EventTrackingService)getService(EventTrackingService.class.getName());
            }
            protected EntityManager entityManager() {
                return (EntityManager)getService(EntityManager.class.getName());
            }
            protected AuthzGroupService authzGroupService() {
                return (AuthzGroupService)getService(AuthzGroupService.class.getName());
            }
        };
    }

    // TESTS BELOW HERE

    /**
     * Test method for {@link org.sakaiproject.user.impl.BaseUserDirectoryService#cleanId(java.lang.String)}.
     */
    @Test
    public void testCleanId() {
        String id = "asdfghjkl";
        String cleaned = userDirectoryService.cleanId(id);
        Assert.assertNotNull(cleaned);
        Assert.assertEquals(id, cleaned);

        id = "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
        cleaned = userDirectoryService.cleanId(id);
        Assert.assertNotNull(cleaned);
        Assert.assertEquals("012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345...", cleaned);

        // invalid ids
        id = "";
        cleaned = userDirectoryService.cleanId(id);
        Assert.assertNull(cleaned);

        id = null;
        cleaned = userDirectoryService.cleanId(id);
        Assert.assertNull(cleaned);
    }

    /**
     * Test method for {@link org.sakaiproject.user.impl.BaseUserDirectoryService#cleanEid(java.lang.String)}.
     */
    @Test
    public void testCleanEid() {
        String eid = "asdfghjkl";
        String cleaned = userDirectoryService.cleanEid(eid);
        Assert.assertNotNull(cleaned);
        Assert.assertEquals(eid, cleaned);

        eid = "azeckoski@unicon.net";
        cleaned = userDirectoryService.cleanEid(eid);
        Assert.assertNotNull(cleaned);
        Assert.assertEquals(eid, cleaned);

        eid = "<script>alert('XSS');</script>";
        cleaned = userDirectoryService.cleanEid(eid);
        Assert.assertNotNull(cleaned);
        Assert.assertEquals("scriptalert('xss')script", cleaned);

        // empty cases
        eid = "";
        cleaned = userDirectoryService.cleanEid(eid);
        Assert.assertNull(cleaned);

        eid = null;
        cleaned = userDirectoryService.cleanEid(eid);
        Assert.assertNull(cleaned);
    }

}
