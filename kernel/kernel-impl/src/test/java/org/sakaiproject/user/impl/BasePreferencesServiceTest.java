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

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;

/**
 * For unit testing parts of the base preferences service
 * 
 * @author Matthew Jones (matthew @ longsight.com)
 */
@Slf4j
public class BasePreferencesServiceTest extends SakaiKernelTestBase  {
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
