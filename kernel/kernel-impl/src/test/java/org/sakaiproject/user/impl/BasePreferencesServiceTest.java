package org.sakaiproject.user.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.mockito.Mockito;

/**
 * For unit testing parts of the base preferences service
 * 
 * @author Matthew Jones (matthew @ longsight.com)
 */
public class BasePreferencesServiceTest extends SakaiKernelTestBase  {
    private static Logger log = LoggerFactory.getLogger(BasePreferencesServiceTest.class);

    private PreferencesService preferencesService;

    /**
     * @throws Exception
     */
	@BeforeClass
	public static void beforeClass() {
		try {
            if (log.isDebugEnabled()) log.debug("starting setup");
            oneTimeSetup();
            UserDirectoryService userService = (UserDirectoryService) getService(UserDirectoryService.class);
            UserEdit prefUser = userService.addUser("prefuser", "prefuser");
            userService.commitEdit(prefUser);
            if (log.isDebugEnabled()) log.debug("finished setup");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}
	
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        log.info("Setting up PreferencesServiceIntegrationTest");

        preferencesService = (PreferencesService) getService(PreferencesService.class);

    }

	private void workAsUser(String eid, String id) {
		SessionManager sessionManager = getService(SessionManager.class);
		Session session = sessionManager.getCurrentSession();
		session.setUserEid(eid);
		session.setUserId(id);
	}

    // TESTS BELOW HERE
    @Test
    public void testPreferencesServiceAdd() {
    	String id = "000";
    	workAsUser("prefuser","prefuser");
    	PreferencesEdit pref=null;
    	try {
    		pref=preferencesService.add(id);
    	}
    	catch (SakaiException e) {
    		log.error("Exception", e);
    	}
        Assert.assertNotNull(pref);
        pref.getPropertiesEdit().addProperty("testprop", "testvalue");
        //Add it
        preferencesService.commit(pref);

        try {
			pref = preferencesService.edit(id);
		} catch (SakaiException e) {
			// TODO Auto-generated catch block
			log.error("Exception", e);
		}
        Assert.assertNotNull(pref);
        String proptest = pref.getProperties().getProperty("testprop");
        Assert.assertEquals(proptest,"testvalue");
    }

    @Test
    public void testPreferencesServiceEdit() {
    	//Try to add a new user 001, edit should be the same as add in this case"
    	String id = "001";
    	workAsUser("prefuser","prefuser");
        PreferencesEdit pref=null;
		try {
			pref = preferencesService.edit(id);
		} catch (SakaiException e) {
			// TODO Auto-generated catch block
			log.error("Exception", e);
		}
        Assert.assertNotNull(pref);
        pref.getPropertiesEdit().addProperty("testprop", "testvalue");
        preferencesService.commit(pref);

        try {
			pref = preferencesService.edit(id);
		} catch (SakaiException e) {
			// TODO Auto-generated catch block
			log.error("Exception", e);
		}
        Assert.assertNotNull(pref);
        String proptest = pref.getProperties().getProperty("testprop");
        Assert.assertEquals(proptest,"testvalue");
    }
}
